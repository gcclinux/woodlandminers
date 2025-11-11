package wagemaker.uk.biome;

import com.badlogic.gdx.graphics.Texture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BiomeTextureGenerator texture generation functionality.
 * Tests texture creation, dimensions, and proper resource management.
 * 
 * Requirements: 1.4 (natural variation)
 */
public class BiomeTextureGeneratorTest {
    
    private BiomeTextureGenerator generator;
    private List<Texture> texturesToDispose;
    
    @BeforeEach
    public void setUp() {
        generator = new BiomeTextureGenerator();
        texturesToDispose = new ArrayList<>();
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up all textures created during tests
        for (Texture texture : texturesToDispose) {
            if (texture != null) {
                texture.dispose();
            }
        }
        texturesToDispose.clear();
    }
    
    // ===== Grass Texture Generation Tests =====
    
    @Test
    public void testGenerateGrassTexture() {
        Texture grassTexture = generator.generateGrassTexture();
        texturesToDispose.add(grassTexture);
        
        assertNotNull(grassTexture, "Grass texture should be generated");
    }
    
    @Test
    public void testGrassTextureDimensions() {
        Texture grassTexture = generator.generateGrassTexture();
        texturesToDispose.add(grassTexture);
        
        assertEquals(BiomeConfig.TEXTURE_SIZE, grassTexture.getWidth(), 
            "Grass texture width should match config");
        assertEquals(BiomeConfig.TEXTURE_SIZE, grassTexture.getHeight(), 
            "Grass texture height should match config");
    }
    
    @Test
    public void testGrassTextureIsSquare() {
        Texture grassTexture = generator.generateGrassTexture();
        texturesToDispose.add(grassTexture);
        
        assertEquals(grassTexture.getWidth(), grassTexture.getHeight(), 
            "Grass texture should be square");
    }
    
    @Test
    public void testMultipleGrassTextureGeneration() {
        // Generate multiple grass textures to ensure consistency
        Texture grass1 = generator.generateGrassTexture();
        Texture grass2 = generator.generateGrassTexture();
        Texture grass3 = generator.generateGrassTexture();
        
        texturesToDispose.add(grass1);
        texturesToDispose.add(grass2);
        texturesToDispose.add(grass3);
        
        assertNotNull(grass1, "First grass texture should be generated");
        assertNotNull(grass2, "Second grass texture should be generated");
        assertNotNull(grass3, "Third grass texture should be generated");
        
        // All should have same dimensions
        assertEquals(grass1.getWidth(), grass2.getWidth(), 
            "All grass textures should have same width");
        assertEquals(grass1.getHeight(), grass2.getHeight(), 
            "All grass textures should have same height");
    }
    
    // ===== Sand Texture Generation Tests =====
    
    @Test
    public void testGenerateSandTexture() {
        Texture sandTexture = generator.generateSandTexture();
        texturesToDispose.add(sandTexture);
        
        assertNotNull(sandTexture, "Sand texture should be generated");
    }
    
    @Test
    public void testSandTextureDimensions() {
        Texture sandTexture = generator.generateSandTexture();
        texturesToDispose.add(sandTexture);
        
        assertEquals(BiomeConfig.TEXTURE_SIZE, sandTexture.getWidth(), 
            "Sand texture width should match config");
        assertEquals(BiomeConfig.TEXTURE_SIZE, sandTexture.getHeight(), 
            "Sand texture height should match config");
    }
    
    @Test
    public void testSandTextureIsSquare() {
        Texture sandTexture = generator.generateSandTexture();
        texturesToDispose.add(sandTexture);
        
        assertEquals(sandTexture.getWidth(), sandTexture.getHeight(), 
            "Sand texture should be square");
    }
    
    @Test
    public void testMultipleSandTextureGeneration() {
        // Generate multiple sand textures to ensure consistency
        Texture sand1 = generator.generateSandTexture();
        Texture sand2 = generator.generateSandTexture();
        Texture sand3 = generator.generateSandTexture();
        
        texturesToDispose.add(sand1);
        texturesToDispose.add(sand2);
        texturesToDispose.add(sand3);
        
        assertNotNull(sand1, "First sand texture should be generated");
        assertNotNull(sand2, "Second sand texture should be generated");
        assertNotNull(sand3, "Third sand texture should be generated");
        
        // All should have same dimensions
        assertEquals(sand1.getWidth(), sand2.getWidth(), 
            "All sand textures should have same width");
        assertEquals(sand1.getHeight(), sand2.getHeight(), 
            "All sand textures should have same height");
    }
    
    // ===== Texture Comparison Tests =====
    
    @Test
    public void testGrassAndSandTexturesSameDimensions() {
        Texture grassTexture = generator.generateGrassTexture();
        Texture sandTexture = generator.generateSandTexture();
        
        texturesToDispose.add(grassTexture);
        texturesToDispose.add(sandTexture);
        
        assertEquals(grassTexture.getWidth(), sandTexture.getWidth(), 
            "Grass and sand textures should have same width");
        assertEquals(grassTexture.getHeight(), sandTexture.getHeight(), 
            "Grass and sand textures should have same height");
    }
    
    @Test
    public void testGrassAndSandTexturesAreDifferent() {
        Texture grassTexture = generator.generateGrassTexture();
        Texture sandTexture = generator.generateSandTexture();
        
        texturesToDispose.add(grassTexture);
        texturesToDispose.add(sandTexture);
        
        // They should be different texture instances
        assertNotSame(grassTexture, sandTexture, 
            "Grass and sand should be different texture instances");
    }
    
    // ===== Texture Wrapping Tests =====
    
    @Test
    public void testGrassTextureWrapping() {
        Texture grassTexture = generator.generateGrassTexture();
        texturesToDispose.add(grassTexture);
        
        // Textures should have repeat wrapping for seamless tiling
        assertEquals(Texture.TextureWrap.Repeat, grassTexture.getUWrap(), 
            "Grass texture should have repeat U wrapping");
        assertEquals(Texture.TextureWrap.Repeat, grassTexture.getVWrap(), 
            "Grass texture should have repeat V wrapping");
    }
    
    @Test
    public void testSandTextureWrapping() {
        Texture sandTexture = generator.generateSandTexture();
        texturesToDispose.add(sandTexture);
        
        // Textures should have repeat wrapping for seamless tiling
        assertEquals(Texture.TextureWrap.Repeat, sandTexture.getUWrap(), 
            "Sand texture should have repeat U wrapping");
        assertEquals(Texture.TextureWrap.Repeat, sandTexture.getVWrap(), 
            "Sand texture should have repeat V wrapping");
    }
    
    // ===== Deterministic Generation Tests =====
    
    @Test
    public void testDeterministicGrassGeneration() {
        // With fixed seed, grass textures should be deterministic
        // Generate multiple times and verify dimensions are consistent
        Texture grass1 = generator.generateGrassTexture();
        Texture grass2 = generator.generateGrassTexture();
        
        texturesToDispose.add(grass1);
        texturesToDispose.add(grass2);
        
        assertEquals(grass1.getWidth(), grass2.getWidth(), 
            "Grass textures should have consistent width");
        assertEquals(grass1.getHeight(), grass2.getHeight(), 
            "Grass textures should have consistent height");
    }
    
    @Test
    public void testDeterministicSandGeneration() {
        // With fixed seed, sand textures should be deterministic
        // Generate multiple times and verify dimensions are consistent
        Texture sand1 = generator.generateSandTexture();
        Texture sand2 = generator.generateSandTexture();
        
        texturesToDispose.add(sand1);
        texturesToDispose.add(sand2);
        
        assertEquals(sand1.getWidth(), sand2.getWidth(), 
            "Sand textures should have consistent width");
        assertEquals(sand1.getHeight(), sand2.getHeight(), 
            "Sand textures should have consistent height");
    }
    
    // ===== Resource Management Tests =====
    
    @Test
    public void testTextureDisposal() {
        Texture texture = generator.generateGrassTexture();
        
        assertDoesNotThrow(() -> texture.dispose(), 
            "Texture disposal should not throw exceptions");
    }
    
    @Test
    public void testMultipleTextureDisposals() {
        Texture texture = generator.generateGrassTexture();
        
        texture.dispose();
        assertDoesNotThrow(() -> texture.dispose(), 
            "Multiple texture disposals should be safe");
    }
    
    @Test
    public void testGeneratorReusability() {
        // Generator should be reusable for multiple texture generations
        Texture grass1 = generator.generateGrassTexture();
        texturesToDispose.add(grass1);
        
        Texture sand1 = generator.generateSandTexture();
        texturesToDispose.add(sand1);
        
        Texture grass2 = generator.generateGrassTexture();
        texturesToDispose.add(grass2);
        
        Texture sand2 = generator.generateSandTexture();
        texturesToDispose.add(sand2);
        
        assertNotNull(grass1, "First grass generation should succeed");
        assertNotNull(sand1, "First sand generation should succeed");
        assertNotNull(grass2, "Second grass generation should succeed");
        assertNotNull(sand2, "Second sand generation should succeed");
    }
    
    // ===== Performance Tests =====
    
    @Test
    public void testGrassGenerationPerformance() {
        // Texture generation should complete quickly
        long startTime = System.nanoTime();
        
        Texture texture = generator.generateGrassTexture();
        texturesToDispose.add(texture);
        
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        
        // Texture generation should complete in reasonable time (< 100ms)
        assertTrue(durationMs < 100, 
            "Grass texture generation should complete in less than 100ms, took: " + durationMs + "ms");
    }
    
    @Test
    public void testSandGenerationPerformance() {
        // Texture generation should complete quickly
        long startTime = System.nanoTime();
        
        Texture texture = generator.generateSandTexture();
        texturesToDispose.add(texture);
        
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        
        // Texture generation should complete in reasonable time (< 100ms)
        assertTrue(durationMs < 100, 
            "Sand texture generation should complete in less than 100ms, took: " + durationMs + "ms");
    }
    
    @Test
    public void testBulkTextureGeneration() {
        // Test generating multiple textures in sequence
        long startTime = System.nanoTime();
        
        for (int i = 0; i < 10; i++) {
            Texture grass = generator.generateGrassTexture();
            Texture sand = generator.generateSandTexture();
            texturesToDispose.add(grass);
            texturesToDispose.add(sand);
        }
        
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        
        // 20 texture generations should complete quickly (< 1 second)
        assertTrue(durationMs < 1000, 
            "20 texture generations should complete in less than 1 second, took: " + durationMs + "ms");
    }
    
    // ===== Edge Case Tests =====
    
    @Test
    public void testGeneratorWithoutDisposal() {
        // Test that generator can create textures without immediate disposal
        Texture grass = generator.generateGrassTexture();
        Texture sand = generator.generateSandTexture();
        
        assertNotNull(grass, "Grass texture should be created");
        assertNotNull(sand, "Sand texture should be created");
        
        // Clean up
        grass.dispose();
        sand.dispose();
    }
    
    @Test
    public void testMultipleGeneratorInstances() {
        // Test that multiple generator instances can coexist
        BiomeTextureGenerator generator2 = new BiomeTextureGenerator();
        BiomeTextureGenerator generator3 = new BiomeTextureGenerator();
        
        Texture grass1 = generator.generateGrassTexture();
        Texture grass2 = generator2.generateGrassTexture();
        Texture grass3 = generator3.generateGrassTexture();
        
        texturesToDispose.add(grass1);
        texturesToDispose.add(grass2);
        texturesToDispose.add(grass3);
        
        assertNotNull(grass1, "Generator 1 should create texture");
        assertNotNull(grass2, "Generator 2 should create texture");
        assertNotNull(grass3, "Generator 3 should create texture");
        
        // All should have same dimensions
        assertEquals(grass1.getWidth(), grass2.getWidth(), 
            "All generators should produce same width");
        assertEquals(grass1.getWidth(), grass3.getWidth(), 
            "All generators should produce same width");
    }
    
    @Test
    public void testTextureGenerationConsistency() {
        // Test that textures maintain consistent properties across generations
        List<Texture> grassTextures = new ArrayList<>();
        List<Texture> sandTextures = new ArrayList<>();
        
        for (int i = 0; i < 5; i++) {
            grassTextures.add(generator.generateGrassTexture());
            sandTextures.add(generator.generateSandTexture());
        }
        
        texturesToDispose.addAll(grassTextures);
        texturesToDispose.addAll(sandTextures);
        
        // Verify all grass textures have same dimensions
        int grassWidth = grassTextures.get(0).getWidth();
        int grassHeight = grassTextures.get(0).getHeight();
        
        for (Texture grass : grassTextures) {
            assertEquals(grassWidth, grass.getWidth(), 
                "All grass textures should have consistent width");
            assertEquals(grassHeight, grass.getHeight(), 
                "All grass textures should have consistent height");
        }
        
        // Verify all sand textures have same dimensions
        int sandWidth = sandTextures.get(0).getWidth();
        int sandHeight = sandTextures.get(0).getHeight();
        
        for (Texture sand : sandTextures) {
            assertEquals(sandWidth, sand.getWidth(), 
                "All sand textures should have consistent width");
            assertEquals(sandHeight, sand.getHeight(), 
                "All sand textures should have consistent height");
        }
    }
}
