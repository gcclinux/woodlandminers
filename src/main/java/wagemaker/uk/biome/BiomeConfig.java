package wagemaker.uk.biome;

/**
 * Configuration class containing all tunable parameters for the biome system.
 * This class centralizes all constants to make them easily adjustable for future tuning.
 * 
 * All parameters are public static final for easy access throughout the biome system.
 * To adjust biome behavior, modify the values in this class and recompile.
 * 
 * Requirements: 1.1 (multiple biome zones), 3.1 (configurable thresholds), 3.4 (modifiable configuration)
 */
public class BiomeConfig {
    
    // ========== BIOME ZONE DEFINITIONS ==========
    
    /**
     * Radius of the inner grass zone from spawn point (0,0) in pixels.
     * Players within this distance from spawn will see grass terrain.
     * 
     * Gameplay impact: High
     * Recommended range: 5000-15000
     * Default: 10000
     * 
     * Requirement: 1.1 - Configurable distance ranges for biome zones
     */
    public static final float INNER_GRASS_RADIUS = 10000.0f;
    
    /**
     * Width of the sand zone in pixels.
     * This defines how far the sand biome extends beyond the inner grass zone.
     * Sand zone spans from INNER_GRASS_RADIUS to (INNER_GRASS_RADIUS + SAND_ZONE_WIDTH).
     * 
     * Gameplay impact: High
     * Recommended range: 2000-5000
     * Default: 3000
     * 
     * Requirement: 1.1 - Configurable distance ranges for biome zones
     */
    public static final float SAND_ZONE_WIDTH = 3000.0f;
    
    // ========== TEXTURE GENERATION PARAMETERS ==========
    
    /**
     * Size of generated biome textures in pixels (width and height).
     * Textures are square and must tile seamlessly.
     * 
     * Performance impact: Medium
     * Recommended range: 32-128
     * Default: 64
     * 
     * Requirement: 1.4 - Natural-looking variation in tile placement
     */
    public static final int TEXTURE_SIZE = 64;
    
    /**
     * Random seed for grass texture generation.
     * Using a fixed seed ensures consistent grass appearance across game sessions.
     * 
     * Visual impact: Medium
     * Default: 12345 (matches existing grass texture seed)
     */
    public static final int TEXTURE_SEED_GRASS = 12345;
    
    /**
     * Random seed for sand texture generation.
     * Using a fixed seed ensures consistent sand appearance across game sessions.
     * 
     * Visual impact: Medium
     * Default: 54321
     */
    public static final int TEXTURE_SEED_SAND = 54321;
    
    // ========== GRASS TEXTURE COLORS ==========
    
    /**
     * Base grass color (dark green) - RGBA components.
     * This is the primary color for grass terrain.
     * 
     * Visual impact: High
     * Default: Dark green (0.15, 0.5, 0.08, 1.0)
     */
    public static final float[] GRASS_BASE_COLOR = {0.15f, 0.5f, 0.08f, 1.0f};
    
    /**
     * Light grass color (light green) - RGBA components.
     * Used for grass blade highlights and lighter patches.
     * 
     * Visual impact: High
     * Default: Light green (0.25, 0.65, 0.15, 1.0)
     */
    public static final float[] GRASS_LIGHT_COLOR = {0.25f, 0.65f, 0.15f, 1.0f};
    
    /**
     * Medium grass color (medium green) - RGBA components.
     * Used for mid-tone grass variations.
     * 
     * Visual impact: Medium
     * Default: Medium green (0.2, 0.55, 0.12, 1.0)
     */
    public static final float[] GRASS_MEDIUM_COLOR = {0.2f, 0.55f, 0.12f, 1.0f};
    
    /**
     * Brownish grass color (dirt patches) - RGBA components.
     * Used for dirt patches and darker areas in grass.
     * 
     * Visual impact: Medium
     * Default: Brownish green (0.3, 0.4, 0.1, 1.0)
     */
    public static final float[] GRASS_BROWNISH_COLOR = {0.3f, 0.4f, 0.1f, 1.0f};
    
    // ========== SAND TEXTURE COLORS ==========
    
    /**
     * Base sand color (sandy beige) - RGBA components.
     * This is the primary color for sand terrain.
     * 
     * Visual impact: High
     * Default: Sandy beige (0.85, 0.75, 0.55, 1.0)
     */
    public static final float[] SAND_BASE_COLOR = {0.85f, 0.75f, 0.55f, 1.0f};
    
    /**
     * Light sand color (sun-bleached) - RGBA components.
     * Used for highlights and sun-bleached areas in sand.
     * 
     * Visual impact: High
     * Default: Light sandy beige (0.95, 0.85, 0.65, 1.0)
     */
    public static final float[] SAND_LIGHT_COLOR = {0.95f, 0.85f, 0.65f, 1.0f};
    
    /**
     * Dark sand color (shadows/rocks) - RGBA components.
     * Used for darker spots, shadows, and small rocks in sand.
     * 
     * Visual impact: Medium
     * Default: Dark sandy brown (0.75, 0.65, 0.45, 1.0)
     */
    public static final float[] SAND_DARK_COLOR = {0.75f, 0.65f, 0.45f, 1.0f};
    
    // ========== PERFORMANCE SETTINGS ==========
    
    /**
     * Enable natural texture variation in biome textures.
     * When enabled, textures include random patterns and details.
     * When disabled, textures use solid colors (better performance).
     * 
     * Performance impact: Low
     * Visual impact: High
     * Default: true
     */
    public static final boolean ENABLE_TEXTURE_VARIATION = true;
    
    // ========== FUTURE EXTENSIBILITY ==========
    
    /**
     * Enable debug rendering for biome zones (shows zone boundaries).
     * Useful for development and tuning.
     * 
     * Default: false
     */
    public static final boolean DEBUG_RENDER_ZONES = false;
    
    // Private constructor to prevent instantiation
    private BiomeConfig() {
        throw new AssertionError("BiomeConfig is a utility class and should not be instantiated");
    }
}
