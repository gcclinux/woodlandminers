package wagemaker.uk.biome;

import com.badlogic.gdx.graphics.Texture;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Core biome management system that determines which biome applies at any world coordinate.
 * This class handles biome zone configuration, distance calculations, texture caching,
 * and provides the main API for querying biome information.
 * 
 * The BiomeManager is responsible for:
 * - Calculating distance from spawn point (0,0)
 * - Determining which biome zone applies at any coordinate
 * - Caching and providing textures for each biome type
 * - Managing texture lifecycle and cleanup
 * 
 * Requirements: 1.2 (distance calculation), 1.3 (seamless rendering), 1.5 (mode consistency),
 *               2.1 (performance), 2.2 (consistent depth), 4.1 (coordinate-based), 4.2 (deterministic)
 */
public class BiomeManager {
    
    private final List<BiomeZone> biomeZones;
    private final Map<BiomeType, Texture> textureCache;
    private final BiomeTextureGenerator textureGenerator;
    private boolean initialized;
    private final Random noiseRandom;
    private boolean headlessMode;
    
    /**
     * Creates a new BiomeManager instance.
     * Call initialize() after construction to set up biome zones and generate textures.
     */
    public BiomeManager() {
        this.biomeZones = new ArrayList<>();
        this.textureCache = new HashMap<>();
        this.textureGenerator = new BiomeTextureGenerator();
        this.initialized = false;
        this.noiseRandom = new Random(54321); // Fixed seed for consistent noise pattern
        this.headlessMode = false;
    }
    
    /**
     * Initializes the biome system by setting up zones and generating textures.
     * This method must be called before using any other BiomeManager methods.
     * 
     * Initialization steps:
     * 1. Create default biome zone configuration
     * 2. Generate textures for each biome type (only if on OpenGL thread)
     * 3. Cache textures for fast lookup
     * 
     * Requirements: 1.1 (multiple biome zones), 3.1 (configurable thresholds)
     */
    public void initialize() {
        if (initialized) {
            return; // Already initialized
        }
        
        // Set up biome zones
        initializeBiomeZones();
        
        // Generate and cache textures for each biome type
        // Skip texture generation if not on OpenGL thread (e.g., server-side or tests)
        // Check if we're on the main rendering thread by checking thread name
        String threadName = Thread.currentThread().getName();
        boolean isRenderThread = threadName.contains("LWJGL") || threadName.equals("main");
        
        if (com.badlogic.gdx.Gdx.graphics != null && isRenderThread) {
            try {
                generateAndCacheTextures();
            } catch (Exception e) {
                // Texture generation failed - enter headless mode
                System.err.println("BiomeManager: Texture generation failed, entering headless mode: " + e.getMessage());
                headlessMode = true;
            }
        } else {
            // Not on OpenGL thread (e.g., server-side background thread or tests)
            // Biome zones are still initialized and functional
            headlessMode = true;
            System.out.println("BiomeManager: Skipping texture generation (thread: " + threadName + ")");
        }
        
        initialized = true;
    }
    
    /**
     * Gets the appropriate texture for a given world position.
     * This is the main method used by the rendering system.
     * 
     * @param worldX The x-coordinate in world space
     * @param worldY The y-coordinate in world space
     * @return The texture to use at this position, or grass texture as fallback
     * 
     * Requirements: 1.3 (seamless rendering), 2.1 (performance), 4.1 (coordinate-based)
     */
    public Texture getTextureForPosition(float worldX, float worldY) {
        if (!initialized) {
            throw new IllegalStateException("BiomeManager must be initialized before use. Call initialize() first.");
        }
        
        if (headlessMode) {
            throw new IllegalStateException("Cannot get textures in headless mode (unit tests). Use getBiomeAtPosition() instead.");
        }
        
        BiomeType biomeType = getBiomeAtPosition(worldX, worldY);
        Texture texture = textureCache.get(biomeType);
        
        // Fallback to grass texture if somehow missing
        if (texture == null) {
            texture = textureCache.get(BiomeType.GRASS);
        }
        
        return texture;
    }
    
    /**
     * Determines which biome type applies at a given world position.
     * Uses multiple random sand patches scattered around the world.
     * Sand patches appear at distances between 7000-15000px from spawn.
     * 
     * @param worldX The x-coordinate in world space
     * @param worldY The y-coordinate in world space
     * @return The biome type at this position
     * 
     * Requirements: 1.2 (distance calculation), 4.1 (coordinate-based), 4.2 (deterministic)
     */
    public BiomeType getBiomeAtPosition(float worldX, float worldY) {
        // Check if position is in a sand patch
        if (isInSandPatch(worldX, worldY)) {
            return BiomeType.SAND;
        }
        
        // Default to grass
        return BiomeType.GRASS;
    }
    
    /**
     * Checks if a position is within a sand patch.
     * Uses multi-octave noise to create organic, irregular sand patches
     * scattered throughout the world at various distances from spawn.
     * 
     * @param worldX The x-coordinate in world space
     * @param worldY The y-coordinate in world space
     * @return true if position is in sand, false otherwise
     */
    private boolean isInSandPatch(float worldX, float worldY) {
        float distance = calculateDistanceFromSpawn(worldX, worldY);
        
        // Don't spawn sand too close to spawn (keep spawn area grass)
        if (distance < 1000) {
            return false;
        }
        
        // Use multi-octave noise to create organic sand patches
        // Scale coordinates for noise sampling
        float noiseScale1 = 0.00015f; // Large features (major patch locations)
        float noiseScale2 = 0.0006f;  // Medium features (patch shapes)
        float noiseScale3 = 0.0015f;  // Small features (edges and details)
        
        // Sample noise at different scales
        float noise1 = simplexNoise(worldX * noiseScale1, worldY * noiseScale1);
        float noise2 = simplexNoise(worldX * noiseScale2, worldY * noiseScale2);
        float noise3 = simplexNoise(worldX * noiseScale3, worldY * noiseScale3);
        
        // Combine noise octaves with different weights
        float combinedNoise = noise1 * 0.5f + noise2 * 0.35f + noise3 * 0.15f;
        
        // Add periodic variation based on distance to create "rings" of varying sand density
        // This creates areas with more/less sand as you travel outward
        float distancePhase = (float) Math.sin(distance * 0.0003f) * 0.15f;
        
        // Normalize combined noise to 0-1 range and add distance variation
        float sandProbability = (combinedNoise * 0.5f + 0.5f) + distancePhase;
        
        // Threshold for sand (adjust to control sand coverage)
        // 0.6 means roughly 40% of the world will be sand patches
        return sandProbability > 0.6f;
    }
    
    /**
     * Calculates the Euclidean distance from the spawn point (0,0) to a given position.
     * 
     * Formula: distance = sqrt(x² + y²)
     * 
     * @param x The x-coordinate in world space
     * @param y The y-coordinate in world space
     * @return The distance from spawn point in pixels
     * 
     * Requirements: 1.2 (distance calculation), 4.1 (coordinate-based)
     */
    private float calculateDistanceFromSpawn(float x, float y) {
        return (float) Math.sqrt(x * x + y * y);
    }
    
    /**
     * Calculates a noise-based offset to add natural variation to biome boundaries.
     * Uses multiple octaves of simplex-like noise to create organic, wavy boundaries
     * instead of perfect circles.
     * 
     * The noise is deterministic based on world coordinates, ensuring consistent
     * biome boundaries across multiple game sessions.
     * 
     * @param x The x-coordinate in world space
     * @param y The y-coordinate in world space
     * @return A noise offset value to add to the distance calculation (typically -1500 to +1500)
     * 
     * Requirements: 1.4 (natural variation), 4.2 (deterministic)
     */
    private float calculateNoiseOffset(float x, float y) {
        // Use multiple octaves of noise for more natural variation
        // Scale down coordinates for larger noise features
        float scale1 = 0.0003f; // Large-scale features (wavy boundaries)
        float scale2 = 0.001f;  // Medium-scale features (smaller indentations)
        float scale3 = 0.003f;  // Small-scale features (fine detail)
        
        // Calculate noise at different scales
        float noise1 = simplexNoise(x * scale1, y * scale1) * 1000.0f;  // ±1000px variation
        float noise2 = simplexNoise(x * scale2, y * scale2) * 400.0f;   // ±400px variation
        float noise3 = simplexNoise(x * scale3, y * scale3) * 100.0f;   // ±100px variation
        
        // Combine octaves for natural-looking boundaries
        return noise1 + noise2 + noise3;
    }
    
    /**
     * Simple 2D simplex-like noise function for creating natural variation.
     * This is a simplified noise implementation that provides deterministic
     * pseudo-random values based on coordinates.
     * 
     * @param x The x-coordinate (scaled)
     * @param y The y-coordinate (scaled)
     * @return A noise value between -1.0 and 1.0
     */
    private float simplexNoise(float x, float y) {
        // Use a hash-based approach for deterministic noise
        // This creates smooth, continuous variation across the world
        
        // Get integer coordinates
        int xi = (int) Math.floor(x);
        int yi = (int) Math.floor(y);
        
        // Get fractional parts
        float xf = x - xi;
        float yf = y - yi;
        
        // Smooth interpolation (smoothstep function)
        float u = xf * xf * (3.0f - 2.0f * xf);
        float v = yf * yf * (3.0f - 2.0f * yf);
        
        // Get noise values at grid corners
        float n00 = hash2D(xi, yi);
        float n10 = hash2D(xi + 1, yi);
        float n01 = hash2D(xi, yi + 1);
        float n11 = hash2D(xi + 1, yi + 1);
        
        // Bilinear interpolation
        float nx0 = lerp(n00, n10, u);
        float nx1 = lerp(n01, n11, u);
        
        return lerp(nx0, nx1, v);
    }
    
    /**
     * Hash function to generate deterministic pseudo-random values for noise.
     * 
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @return A pseudo-random value between -1.0 and 1.0
     */
    private float hash2D(int x, int y) {
        // Use a simple hash function for deterministic randomness
        int n = x + y * 57;
        n = (n << 13) ^ n;
        int nn = (n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff;
        return 1.0f - ((float) nn / 1073741824.0f);
    }
    
    /**
     * Linear interpolation between two values.
     * 
     * @param a The first value
     * @param b The second value
     * @param t The interpolation factor (0.0 to 1.0)
     * @return The interpolated value
     */
    private float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }
    
    /**
     * Initializes the default biome zone configuration.
     * 
     * Default zones:
     * - Zone 1: 0 to 10000px → GRASS (inner spawn area)
     * - Zone 2: 10000 to 13000px → SAND (desert ring)
     * - Zone 3: 13000+ → GRASS (outer areas)
     * 
     * Requirements: 1.1 (multiple biome zones), 3.1 (configurable thresholds)
     */
    private void initializeBiomeZones() {
        biomeZones.clear();
        
        // Zone 1: Inner grass zone (spawn area)
        biomeZones.add(new BiomeZone(
            0.0f,
            BiomeConfig.INNER_GRASS_RADIUS,
            BiomeType.GRASS
        ));
        
        // Zone 2: Sand zone (desert ring)
        float sandZoneEnd = BiomeConfig.INNER_GRASS_RADIUS + BiomeConfig.SAND_ZONE_WIDTH;
        biomeZones.add(new BiomeZone(
            BiomeConfig.INNER_GRASS_RADIUS,
            sandZoneEnd,
            BiomeType.SAND
        ));
        
        // Zone 3: Outer grass zone (far areas)
        biomeZones.add(new BiomeZone(
            sandZoneEnd,
            Float.MAX_VALUE,
            BiomeType.GRASS
        ));
    }
    
    /**
     * Generates textures for all biome types and caches them.
     * This is called during initialization to prepare all textures.
     * 
     * Requirements: 1.4 (natural variation), 2.1 (performance)
     */
    private void generateAndCacheTextures() {
        // Generate grass texture
        Texture grassTexture = textureGenerator.generateGrassTexture();
        textureCache.put(BiomeType.GRASS, grassTexture);
        
        // Generate sand texture
        Texture sandTexture = textureGenerator.generateSandTexture();
        textureCache.put(BiomeType.SAND, sandTexture);
    }
    
    /**
     * Disposes of all cached textures and cleans up resources.
     * This method should be called when the BiomeManager is no longer needed,
     * typically in the game's dispose() method.
     * 
     * After calling dispose(), this BiomeManager instance should not be used again.
     * 
     * Requirements: Resource cleanup, memory management
     */
    public void dispose() {
        // Dispose all cached textures
        for (Texture texture : textureCache.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }
        
        textureCache.clear();
        biomeZones.clear();
        initialized = false;
    }
    
    /**
     * Checks if the BiomeManager has been initialized.
     * 
     * @return true if initialized, false otherwise
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Gets the list of configured biome zones.
     * Useful for debugging and testing.
     * 
     * @return An unmodifiable view of the biome zones
     */
    public List<BiomeZone> getBiomeZones() {
        return new ArrayList<>(biomeZones);
    }
}
