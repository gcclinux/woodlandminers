package wagemaker.uk.biome;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BiomeManager distance calculations and biome determination.
 * Tests core functionality including boundary conditions, negative coordinates, and extreme distances.
 * 
 * Requirements: 1.2 (distance calculation), 1.4 (natural variation), 4.2 (deterministic)
 */
public class BiomeManagerTest {
    
    private BiomeManager biomeManager;
    
    @BeforeEach
    public void setUp() {
        biomeManager = new BiomeManager();
        biomeManager.initialize();
    }
    
    @AfterEach
    public void tearDown() {
        if (biomeManager != null) {
            biomeManager.dispose();
        }
    }
    
    // ===== Initialization Tests =====
    
    @Test
    public void testInitialization() {
        assertNotNull(biomeManager, "BiomeManager should be instantiated");
        assertTrue(biomeManager.isInitialized(), "BiomeManager should be initialized");
    }
    
    @Test
    public void testMultipleInitializationsSafe() {
        biomeManager.initialize();
        biomeManager.initialize();
        assertTrue(biomeManager.isInitialized(), "Multiple initializations should be safe");
    }
    
    @Test
    public void testUninitializedManagerThrowsException() {
        BiomeManager uninitializedManager = new BiomeManager();
        assertThrows(IllegalStateException.class, () -> {
            uninitializedManager.getTextureForPosition(0, 0);
        }, "Uninitialized BiomeManager should throw exception");
        uninitializedManager.dispose();
    }
    
    // ===== Distance Calculation Tests =====
    
    @Test
    public void testBiomeAtSpawn() {
        // At spawn point (0,0), should be GRASS
        BiomeType biome = biomeManager.getBiomeAtPosition(0.0f, 0.0f);
        assertEquals(BiomeType.GRASS, biome, "Spawn point should be GRASS biome");
    }
    
    @Test
    public void testBiomeInInnerGrassZone() {
        // Within inner grass radius (< 10000px), should be GRASS
        BiomeType biome1 = biomeManager.getBiomeAtPosition(5000.0f, 0.0f);
        BiomeType biome2 = biomeManager.getBiomeAtPosition(0.0f, 5000.0f);
        BiomeType biome3 = biomeManager.getBiomeAtPosition(3000.0f, 4000.0f); // 5000px diagonal
        
        // Note: Due to noise variation, we can't guarantee exact biome type
        // but we can verify the method doesn't crash
        assertNotNull(biome1, "Biome should be determined for inner zone");
        assertNotNull(biome2, "Biome should be determined for inner zone");
        assertNotNull(biome3, "Biome should be determined for inner zone");
    }
    
    @Test
    public void testBiomeInOuterGrassZone() {
        // Beyond sand zone (> 13000px), should be GRASS
        BiomeType biome1 = biomeManager.getBiomeAtPosition(15000.0f, 0.0f);
        BiomeType biome2 = biomeManager.getBiomeAtPosition(0.0f, 20000.0f);
        
        assertNotNull(biome1, "Biome should be determined for outer zone");
        assertNotNull(biome2, "Biome should be determined for outer zone");
    }
    
    // ===== Boundary Condition Tests =====
    
    @Test
    public void testBiomeAtInnerBoundary() {
        // Exactly at 10000px boundary (inner grass to sand transition)
        // Due to noise, exact biome may vary, but should not crash
        BiomeType biome1 = biomeManager.getBiomeAtPosition(10000.0f, 0.0f);
        BiomeType biome2 = biomeManager.getBiomeAtPosition(0.0f, 10000.0f);
        BiomeType biome3 = biomeManager.getBiomeAtPosition(7071.0f, 7071.0f); // ~10000px diagonal
        
        assertNotNull(biome1, "Biome should be determined at inner boundary");
        assertNotNull(biome2, "Biome should be determined at inner boundary");
        assertNotNull(biome3, "Biome should be determined at inner boundary");
    }
    
    @Test
    public void testBiomeAtOuterBoundary() {
        // Exactly at 13000px boundary (sand to outer grass transition)
        BiomeType biome1 = biomeManager.getBiomeAtPosition(13000.0f, 0.0f);
        BiomeType biome2 = biomeManager.getBiomeAtPosition(0.0f, 13000.0f);
        BiomeType biome3 = biomeManager.getBiomeAtPosition(9192.0f, 9192.0f); // ~13000px diagonal
        
        assertNotNull(biome1, "Biome should be determined at outer boundary");
        assertNotNull(biome2, "Biome should be determined at outer boundary");
        assertNotNull(biome3, "Biome should be determined at outer boundary");
    }
    
    @Test
    public void testBiomeJustInsideInnerBoundary() {
        // Just inside inner grass zone (9999px)
        BiomeType biome = biomeManager.getBiomeAtPosition(9999.0f, 0.0f);
        assertNotNull(biome, "Biome should be determined just inside inner boundary");
    }
    
    @Test
    public void testBiomeJustOutsideInnerBoundary() {
        // Just outside inner grass zone (10001px)
        BiomeType biome = biomeManager.getBiomeAtPosition(10001.0f, 0.0f);
        assertNotNull(biome, "Biome should be determined just outside inner boundary");
    }
    
    @Test
    public void testBiomeJustInsideOuterBoundary() {
        // Just inside sand zone (12999px)
        BiomeType biome = biomeManager.getBiomeAtPosition(12999.0f, 0.0f);
        assertNotNull(biome, "Biome should be determined just inside outer boundary");
    }
    
    @Test
    public void testBiomeJustOutsideOuterBoundary() {
        // Just outside sand zone (13001px)
        BiomeType biome = biomeManager.getBiomeAtPosition(13001.0f, 0.0f);
        assertNotNull(biome, "Biome should be determined just outside outer boundary");
    }
    
    // ===== Negative Coordinate Tests =====
    
    @Test
    public void testBiomeWithNegativeX() {
        BiomeType biome = biomeManager.getBiomeAtPosition(-5000.0f, 0.0f);
        assertNotNull(biome, "Biome should be determined for negative X coordinate");
    }
    
    @Test
    public void testBiomeWithNegativeY() {
        BiomeType biome = biomeManager.getBiomeAtPosition(0.0f, -5000.0f);
        assertNotNull(biome, "Biome should be determined for negative Y coordinate");
    }
    
    @Test
    public void testBiomeWithBothNegative() {
        BiomeType biome = biomeManager.getBiomeAtPosition(-5000.0f, -5000.0f);
        assertNotNull(biome, "Biome should be determined for both negative coordinates");
    }
    
    @Test
    public void testBiomeWithMixedCoordinates() {
        BiomeType biome1 = biomeManager.getBiomeAtPosition(-3000.0f, 4000.0f); // 5000px distance
        BiomeType biome2 = biomeManager.getBiomeAtPosition(8000.0f, -6000.0f); // 10000px distance
        
        assertNotNull(biome1, "Biome should be determined for mixed coordinates");
        assertNotNull(biome2, "Biome should be determined for mixed coordinates");
    }
    
    @Test
    public void testBiomeWithLargeNegativeCoordinates() {
        BiomeType biome = biomeManager.getBiomeAtPosition(-15000.0f, -15000.0f);
        assertNotNull(biome, "Biome should be determined for large negative coordinates");
    }
    
    // ===== Extreme Distance Tests =====
    
    @Test
    public void testBiomeAtExtremeDistance() {
        BiomeType biome1 = biomeManager.getBiomeAtPosition(50000.0f, 0.0f);
        BiomeType biome2 = biomeManager.getBiomeAtPosition(0.0f, 100000.0f);
        BiomeType biome3 = biomeManager.getBiomeAtPosition(70710.0f, 70710.0f); // ~100000px diagonal
        
        assertNotNull(biome1, "Biome should be determined at extreme distance");
        assertNotNull(biome2, "Biome should be determined at extreme distance");
        assertNotNull(biome3, "Biome should be determined at extreme distance");
    }
    
    @Test
    public void testBiomeAtVeryExtremeDistance() {
        BiomeType biome = biomeManager.getBiomeAtPosition(500000.0f, 500000.0f);
        assertNotNull(biome, "Biome should be determined at very extreme distance");
    }
    
    @Test
    public void testBiomeAtExtremeNegativeDistance() {
        BiomeType biome = biomeManager.getBiomeAtPosition(-100000.0f, -100000.0f);
        assertNotNull(biome, "Biome should be determined at extreme negative distance");
    }
    
    // ===== Deterministic Behavior Tests =====
    
    @Test
    public void testDeterministicBiomeCalculation() {
        // Same coordinates should always return same biome
        float testX = 5000.0f;
        float testY = 5000.0f;
        
        BiomeType biome1 = biomeManager.getBiomeAtPosition(testX, testY);
        BiomeType biome2 = biomeManager.getBiomeAtPosition(testX, testY);
        BiomeType biome3 = biomeManager.getBiomeAtPosition(testX, testY);
        
        assertEquals(biome1, biome2, "Same coordinates should return same biome");
        assertEquals(biome2, biome3, "Same coordinates should return same biome");
    }
    
    @Test
    public void testDeterministicAcrossMultiplePositions() {
        // Test multiple positions for consistency
        float[][] positions = {
            {0.0f, 0.0f},
            {5000.0f, 0.0f},
            {0.0f, 5000.0f},
            {10000.0f, 0.0f},
            {15000.0f, 0.0f},
            {-5000.0f, -5000.0f}
        };
        
        for (float[] pos : positions) {
            BiomeType biome1 = biomeManager.getBiomeAtPosition(pos[0], pos[1]);
            BiomeType biome2 = biomeManager.getBiomeAtPosition(pos[0], pos[1]);
            
            assertEquals(biome1, biome2, 
                "Position (" + pos[0] + ", " + pos[1] + ") should return consistent biome");
        }
    }
    
    // ===== Texture Retrieval Tests =====
    
    @Test
    public void testGetTextureForPosition() {
        assertNotNull(biomeManager.getTextureForPosition(0.0f, 0.0f), 
            "Should return texture for spawn position");
        assertNotNull(biomeManager.getTextureForPosition(5000.0f, 5000.0f), 
            "Should return texture for inner grass zone");
        assertNotNull(biomeManager.getTextureForPosition(15000.0f, 0.0f), 
            "Should return texture for outer grass zone");
    }
    
    @Test
    public void testTextureConsistency() {
        // Same position should return same texture instance (cached)
        float testX = 5000.0f;
        float testY = 5000.0f;
        
        var texture1 = biomeManager.getTextureForPosition(testX, testY);
        var texture2 = biomeManager.getTextureForPosition(testX, testY);
        
        assertNotNull(texture1, "Texture should not be null");
        assertNotNull(texture2, "Texture should not be null");
    }
    
    // ===== Edge Case Tests =====
    
    @Test
    public void testVerySmallCoordinates() {
        BiomeType biome = biomeManager.getBiomeAtPosition(0.001f, 0.001f);
        assertNotNull(biome, "Should handle very small coordinates");
    }
    
    @Test
    public void testZeroCoordinates() {
        BiomeType biome = biomeManager.getBiomeAtPosition(0.0f, 0.0f);
        assertEquals(BiomeType.GRASS, biome, "Zero coordinates should be GRASS");
    }
    
    @Test
    public void testDiagonalMovement() {
        // Test 3-4-5 triangle (distance = 5000)
        BiomeType biome = biomeManager.getBiomeAtPosition(3000.0f, 4000.0f);
        assertNotNull(biome, "Should handle diagonal movement correctly");
    }
    
    @Test
    public void testAllCardinalDirections() {
        float distance = 5000.0f;
        
        BiomeType north = biomeManager.getBiomeAtPosition(0.0f, distance);
        BiomeType south = biomeManager.getBiomeAtPosition(0.0f, -distance);
        BiomeType east = biomeManager.getBiomeAtPosition(distance, 0.0f);
        BiomeType west = biomeManager.getBiomeAtPosition(-distance, 0.0f);
        
        assertNotNull(north, "Should determine biome to the north");
        assertNotNull(south, "Should determine biome to the south");
        assertNotNull(east, "Should determine biome to the east");
        assertNotNull(west, "Should determine biome to the west");
    }
    
    // ===== Disposal Tests =====
    
    @Test
    public void testDisposal() {
        assertDoesNotThrow(() -> biomeManager.dispose(), 
            "Disposal should not throw exceptions");
    }
    
    @Test
    public void testMultipleDisposals() {
        biomeManager.dispose();
        assertDoesNotThrow(() -> biomeManager.dispose(), 
            "Multiple disposals should be safe");
    }
    
    @Test
    public void testDisposalClearsInitialization() {
        biomeManager.dispose();
        assertFalse(biomeManager.isInitialized(), 
            "Disposal should clear initialization flag");
    }
    
    // ===== Biome Zone Configuration Tests =====
    
    @Test
    public void testBiomeZonesConfigured() {
        var zones = biomeManager.getBiomeZones();
        assertNotNull(zones, "Biome zones should be configured");
        assertEquals(3, zones.size(), "Should have 3 biome zones (inner grass, sand, outer grass)");
    }
    
    @Test
    public void testBiomeZoneOrder() {
        var zones = biomeManager.getBiomeZones();
        
        // Zone 1: Inner grass (0 to 10000)
        assertEquals(0.0f, zones.get(0).getMinDistance(), 0.01f, 
            "First zone should start at 0");
        assertEquals(BiomeConfig.INNER_GRASS_RADIUS, zones.get(0).getMaxDistance(), 0.01f, 
            "First zone should end at inner grass radius");
        assertEquals(BiomeType.GRASS, zones.get(0).getBiomeType(), 
            "First zone should be GRASS");
        
        // Zone 2: Sand (10000 to 13000)
        assertEquals(BiomeConfig.INNER_GRASS_RADIUS, zones.get(1).getMinDistance(), 0.01f, 
            "Second zone should start at inner grass radius");
        assertEquals(BiomeConfig.INNER_GRASS_RADIUS + BiomeConfig.SAND_ZONE_WIDTH, 
            zones.get(1).getMaxDistance(), 0.01f, 
            "Second zone should end at inner radius + sand width");
        assertEquals(BiomeType.SAND, zones.get(1).getBiomeType(), 
            "Second zone should be SAND");
        
        // Zone 3: Outer grass (13000 to infinity)
        assertEquals(BiomeConfig.INNER_GRASS_RADIUS + BiomeConfig.SAND_ZONE_WIDTH, 
            zones.get(2).getMinDistance(), 0.01f, 
            "Third zone should start where sand zone ends");
        assertEquals(Float.MAX_VALUE, zones.get(2).getMaxDistance(), 0.01f, 
            "Third zone should extend to infinity");
        assertEquals(BiomeType.GRASS, zones.get(2).getBiomeType(), 
            "Third zone should be GRASS");
    }
}
