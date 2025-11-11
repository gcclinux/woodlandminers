package wagemaker.uk.biome;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for biome rendering system.
 * Tests biome determination when moving between zones, consistency across game modes,
 * and deterministic behavior.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BiomeRenderingIntegrationTest {
    
    private BiomeManager biomeManager;
    
    @BeforeEach
    public void setUp() {
        biomeManager = new BiomeManager();
        initializeBiomeZonesOnly();
    }
    
    @AfterEach
    public void tearDown() {
        if (biomeManager != null && biomeManager.isInitialized()) {
            biomeManager.dispose();
        }
    }
    
    @Test
    @Order(1)
    public void testBiomeChangesAcrossBiomeZones() {
        BiomeType spawnBiome = biomeManager.getBiomeAtPosition(0.0f, 0.0f);
        assertEquals(BiomeType.GRASS, spawnBiome, "Spawn should be in grass biome");
        
        BiomeType innerGrassBiome = biomeManager.getBiomeAtPosition(5000.0f, 0.0f);
        assertNotNull(innerGrassBiome, "Inner grass position should have a biome");
        
        BiomeType sandBiome = biomeManager.getBiomeAtPosition(11500.0f, 0.0f);
        assertNotNull(sandBiome, "Sand zone position should have a biome");
        
        BiomeType outerGrassBiome = biomeManager.getBiomeAtPosition(20000.0f, 0.0f);
        assertNotNull(outerGrassBiome, "Outer grass position should have a biome");
    }
    
    @Test
    @Order(2)
    public void testBiomeTransitionsInAllDirections() {
        float[] distances = {5000.0f, 11500.0f, 15000.0f};
        
        for (float distance : distances) {
            assertNotNull(biomeManager.getBiomeAtPosition(0.0f, distance), "North");
            assertNotNull(biomeManager.getBiomeAtPosition(0.0f, -distance), "South");
            assertNotNull(biomeManager.getBiomeAtPosition(distance, 0.0f), "East");
            assertNotNull(biomeManager.getBiomeAtPosition(-distance, 0.0f), "West");
        }
    }
    
    @Test
    @Order(3)
    public void testContinuousMovementAcrossZones() {
        float startX = 0.0f;
        float endX = 20000.0f;
        float stepSize = 500.0f;
        
        BiomeType previousBiome = null;
        int biomeChangeCount = 0;
        
        for (float x = startX; x <= endX; x += stepSize) {
            BiomeType currentBiome = biomeManager.getBiomeAtPosition(x, 0.0f);
            assertNotNull(currentBiome, "Biome at position " + x + " should not be null");
            
            if (previousBiome != null && currentBiome != previousBiome) {
                biomeChangeCount++;
            }
            previousBiome = currentBiome;
        }
        
        assertTrue(biomeChangeCount > 0, "Should encounter biome changes");
    }
    
    @Test
    @Order(4)
    public void testSinglePlayerMultiplayerConsistency() {
        BiomeManager spBiomeManager = new BiomeManager();
        BiomeManager mpBiomeManager = new BiomeManager();
        
        try {
            initializeBiomeZonesOnly(spBiomeManager);
            initializeBiomeZonesOnly(mpBiomeManager);
            
            float[][] testPositions = {
                {0.0f, 0.0f},
                {5000.0f, 0.0f},
                {11500.0f, 0.0f},
                {15000.0f, 0.0f},
                {-5000.0f, -5000.0f}
            };
            
            for (float[] pos : testPositions) {
                BiomeType spBiome = spBiomeManager.getBiomeAtPosition(pos[0], pos[1]);
                BiomeType mpBiome = mpBiomeManager.getBiomeAtPosition(pos[0], pos[1]);
                
                assertEquals(spBiome, mpBiome, 
                    "Biome at (" + pos[0] + ", " + pos[1] + ") should match in SP and MP");
            }
            
        } finally {
            if (spBiomeManager.isInitialized()) spBiomeManager.dispose();
            if (mpBiomeManager.isInitialized()) mpBiomeManager.dispose();
        }
    }
    
    @Test
    @Order(5)
    public void testDeterministicBiomeCalculationAcrossSessions() {
        float[][] testPositions = {
            {1234.5f, 5678.9f},
            {9876.5f, 4321.1f},
            {11111.1f, 2222.2f}
        };
        
        BiomeType[] firstSessionBiomes = new BiomeType[testPositions.length];
        
        BiomeManager session1 = new BiomeManager();
        initializeBiomeZonesOnly(session1);
        for (int i = 0; i < testPositions.length; i++) {
            firstSessionBiomes[i] = session1.getBiomeAtPosition(testPositions[i][0], testPositions[i][1]);
        }
        session1.dispose();
        
        BiomeManager session2 = new BiomeManager();
        initializeBiomeZonesOnly(session2);
        for (int i = 0; i < testPositions.length; i++) {
            BiomeType secondBiome = session2.getBiomeAtPosition(testPositions[i][0], testPositions[i][1]);
            assertEquals(firstSessionBiomes[i], secondBiome, "Biomes should be consistent across sessions");
        }
        session2.dispose();
    }
    
    @Test
    @Order(6)
    public void testNoNetworkSynchronizationRequired() {
        BiomeManager manager1 = new BiomeManager();
        BiomeManager manager2 = new BiomeManager();
        BiomeManager manager3 = new BiomeManager();
        
        try {
            initializeBiomeZonesOnly(manager1);
            initializeBiomeZonesOnly(manager2);
            initializeBiomeZonesOnly(manager3);
            
            float[][] positions = {
                {0.0f, 0.0f},
                {5000.0f, 5000.0f},
                {11500.0f, 0.0f}
            };
            
            for (float[] pos : positions) {
                BiomeType b1 = manager1.getBiomeAtPosition(pos[0], pos[1]);
                BiomeType b2 = manager2.getBiomeAtPosition(pos[0], pos[1]);
                BiomeType b3 = manager3.getBiomeAtPosition(pos[0], pos[1]);
                
                assertEquals(b1, b2, "Independent managers should produce identical biomes");
                assertEquals(b2, b3, "Independent managers should produce identical biomes");
            }
            
        } finally {
            manager1.dispose();
            manager2.dispose();
            manager3.dispose();
        }
    }
    
    @Test
    @Order(7)
    public void testBiomeDeterminationPerformance() {
        int queryCount = 1000;
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < queryCount; i++) {
            float x = (i % 100) * 200.0f;
            float y = (i / 100) * 200.0f;
            BiomeType biome = biomeManager.getBiomeAtPosition(x, y);
            assertNotNull(biome);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 100, "1000 queries should complete in < 100ms, took " + duration + "ms");
    }
    
    @Test
    @Order(8)
    public void testMultipleBiomeZonesVisibleSimultaneously() {
        float centerX = 10000.0f;
        float centerY = 0.0f;
        float viewportWidth = 4000.0f;
        float viewportHeight = 4000.0f;
        
        int samplesPerAxis = 10;
        float stepX = viewportWidth / samplesPerAxis;
        float stepY = viewportHeight / samplesPerAxis;
        
        int grassCount = 0;
        int sandCount = 0;
        
        for (int i = 0; i < samplesPerAxis; i++) {
            for (int j = 0; j < samplesPerAxis; j++) {
                float x = centerX - (viewportWidth / 2) + (i * stepX);
                float y = centerY - (viewportHeight / 2) + (j * stepY);
                
                BiomeType biome = biomeManager.getBiomeAtPosition(x, y);
                assertNotNull(biome);
                
                if (biome == BiomeType.GRASS) grassCount++;
                else if (biome == BiomeType.SAND) sandCount++;
            }
        }
        
        assertTrue(grassCount > 0, "Should see grass biome");
        assertTrue(sandCount > 0, "Should see sand biome");
    }
    
    @Test
    @Order(9)
    public void testAllThreeBiomeZonesVisibleInLargeViewport() {
        float centerX = 11500.0f;
        float centerY = 0.0f;
        float viewportRadius = 5000.0f;
        
        boolean seenInnerGrass = false;
        boolean seenSand = false;
        boolean seenOuterGrass = false;
        
        int sampleCount = 100;
        for (int i = 0; i < sampleCount; i++) {
            double angle = (2 * Math.PI * i) / sampleCount;
            float x = centerX + (float)(Math.cos(angle) * viewportRadius);
            float y = centerY + (float)(Math.sin(angle) * viewportRadius);
            
            BiomeType biome = biomeManager.getBiomeAtPosition(x, y);
            assertNotNull(biome);
            
            float distance = (float)Math.sqrt(x * x + y * y);
            
            if (biome == BiomeType.GRASS && distance < 10500.0f) {
                seenInnerGrass = true;
            } else if (biome == BiomeType.SAND) {
                seenSand = true;
            } else if (biome == BiomeType.GRASS && distance > 12500.0f) {
                seenOuterGrass = true;
            }
        }
        
        assertTrue(seenInnerGrass, "Should see inner grass zone");
        assertTrue(seenSand, "Should see sand zone");
        assertTrue(seenOuterGrass, "Should see outer grass zone");
    }
    
    private void initializeBiomeZonesOnly() {
        initializeBiomeZonesOnly(biomeManager);
    }
    
    private void initializeBiomeZonesOnly(BiomeManager manager) {
        try {
            manager.initialize();
        } catch (UnsatisfiedLinkError e) {
            // Expected in headless environment - biome zones are still initialized
        }
    }
}
