package wagemaker.uk.weather;

/**
 * Configuration class containing all tunable parameters for the rain system.
 * This class centralizes all constants to make them easily adjustable for future tuning.
 * 
 * All parameters are public static final for easy access throughout the rain system.
 * To adjust rain behavior, modify the values in this class and recompile.
 * 
 * Requirements: 6.2 (modular architecture), 6.5 (extensible interfaces)
 */
public class RainConfig {
    
    // ========== PARTICLE COUNT CONFIGURATION ==========
    
    /**
     * Maximum number of rain particles that can be active simultaneously.
     * Higher values create denser rain but may impact performance.
     * 
     * Performance impact: High
     * Recommended range: 100-300
     * Default: 200
     */
    public static final int MAX_PARTICLES = 200;
    
    /**
     * Minimum number of rain particles at lowest intensity.
     * This ensures rain is visible even at low intensity levels.
     * 
     * Performance impact: Medium
     * Recommended range: 50-150
     * Default: 100
     */
    public static final int MIN_PARTICLES = 100;
    
    // ========== VISUAL PROPERTIES ==========
    
    /**
     * Width of each rain particle in pixels.
     * Wider particles are more visible but may look less realistic.
     * 
     * Visual impact: Medium
     * Recommended range: 1.0-4.0
     * Default: 2.0
     */
    public static final float PARTICLE_WIDTH = 2.0f;
    
    /**
     * Minimum length of rain particles in pixels.
     * Shorter particles look like drizzle, longer particles look like heavy rain.
     * 
     * Visual impact: High
     * Recommended range: 5.0-15.0
     * Default: 10.0
     */
    public static final float MIN_PARTICLE_LENGTH = 10.0f;
    
    /**
     * Maximum length of rain particles in pixels.
     * Creates variation in particle appearance for more realistic rain.
     * 
     * Visual impact: High
     * Recommended range: 10.0-20.0
     * Default: 15.0
     */
    public static final float MAX_PARTICLE_LENGTH = 15.0f;
    
    /**
     * Minimum alpha (transparency) value for rain particles.
     * Lower values make rain more transparent and subtle.
     * Range: 0.0 (fully transparent) to 1.0 (fully opaque)
     * 
     * Visual impact: High
     * Recommended range: 0.2-0.5
     * Default: 0.3
     */
    public static final float MIN_ALPHA = 0.3f;
    
    /**
     * Maximum alpha (transparency) value for rain particles.
     * Creates variation in particle visibility for depth effect.
     * Range: 0.0 (fully transparent) to 1.0 (fully opaque)
     * 
     * Visual impact: High
     * Recommended range: 0.4-0.8
     * Default: 0.6
     */
    public static final float MAX_ALPHA = 0.6f;
    
    /**
     * Red component of rain particle color (0.0-1.0).
     * Default creates a light blue-gray color typical of rain.
     * 
     * Visual impact: Medium
     * Recommended range: 0.5-0.9
     * Default: 0.7
     */
    public static final float RAIN_COLOR_RED = 0.7f;
    
    /**
     * Green component of rain particle color (0.0-1.0).
     * Default creates a light blue-gray color typical of rain.
     * 
     * Visual impact: Medium
     * Recommended range: 0.5-0.9
     * Default: 0.7
     */
    public static final float RAIN_COLOR_GREEN = 0.7f;
    
    /**
     * Blue component of rain particle color (0.0-1.0).
     * Default creates a light blue-gray color typical of rain.
     * Higher values make rain appear more blue.
     * 
     * Visual impact: Medium
     * Recommended range: 0.7-1.0
     * Default: 0.9
     */
    public static final float RAIN_COLOR_BLUE = 0.9f;
    
    // ========== PHYSICS CONFIGURATION ==========
    
    /**
     * Minimum falling velocity of rain particles in pixels per second.
     * Slower velocities create a gentle rain effect.
     * 
     * Visual impact: High
     * Recommended range: 200-500
     * Default: 400
     */
    public static final float MIN_VELOCITY = 400.0f;
    
    /**
     * Maximum falling velocity of rain particles in pixels per second.
     * Faster velocities create a heavy rain or storm effect.
     * 
     * Visual impact: High
     * Recommended range: 400-800
     * Default: 600
     */
    public static final float MAX_VELOCITY = 600.0f;
    
    /**
     * Horizontal wind effect on rain particles (pixels per second).
     * Positive values push rain to the right, negative to the left.
     * Currently unused but reserved for future wind effects.
     * 
     * Visual impact: Medium (when implemented)
     * Recommended range: -200 to 200
     * Default: 0.0 (no wind)
     */
    public static final float WIND_EFFECT = 0.0f;
    
    /**
     * Vertical offset above the screen where particles spawn (pixels).
     * Ensures particles don't suddenly appear at the top edge of the screen.
     * 
     * Visual impact: Low
     * Recommended range: 20-100
     * Default: 50
     */
    public static final float SPAWN_OFFSET_Y = 50.0f;
    
    // ========== ZONE DEFAULT CONFIGURATION ==========
    
    /**
     * Default fade distance for rain zones in pixels.
     * This is the distance over which rain intensity fades from full to zero
     * when moving away from a rain zone.
     * 
     * Gameplay impact: Medium
     * Recommended range: 50-200
     * Default: 100
     */
    public static final float DEFAULT_FADE_DISTANCE = 100.0f;
    
    /**
     * Default maximum intensity for rain zones.
     * Range: 0.0 (no rain) to 1.0 (full intensity)
     * 
     * Gameplay impact: Medium
     * Recommended range: 0.5-1.0
     * Default: 1.0
     */
    public static final float DEFAULT_INTENSITY = 1.0f;
    
    // ========== SPAWN AREA ZONE CONFIGURATION ==========
    
    /**
     * X coordinate of the spawn area rain zone center.
     * This is where the cactus is located in the game world.
     * 
     * Requirement: 2.1 - Rain zone centered at (128, 128)
     */
    public static final float SPAWN_ZONE_CENTER_X = 128.0f;
    
    /**
     * Y coordinate of the spawn area rain zone center.
     * This is where the cactus is located in the game world.
     * 
     * Requirement: 2.1 - Rain zone centered at (128, 128)
     */
    public static final float SPAWN_ZONE_CENTER_Y = 128.0f;
    
    /**
     * Radius of the spawn area rain zone in pixels.
     * Players within this distance from the center experience full rain intensity.
     * 
     * Requirement: 2.1 - Rain zone with 640px radius
     * Gameplay impact: High
     * Recommended range: 400-1000
     * Default: 640
     */
    public static final float SPAWN_ZONE_RADIUS = 640.0f;
    
    /**
     * Unique identifier for the spawn area rain zone.
     * Used for zone management and debugging.
     */
    public static final String SPAWN_ZONE_ID = "spawn_rain";
    
    // ========== PERFORMANCE TUNING ==========
    
    /**
     * Maximum intensity value that can be achieved by combining multiple zones.
     * Prevents excessive particle counts when zones overlap.
     * 
     * Performance impact: Medium
     * Recommended range: 1.0-2.0
     * Default: 1.0
     */
    public static final float MAX_COMBINED_INTENSITY = 1.0f;
    
    /**
     * Minimum intensity threshold below which rain is not rendered.
     * Helps performance by skipping rendering when rain is barely visible.
     * 
     * Performance impact: Low
     * Recommended range: 0.0-0.1
     * Default: 0.0 (always render if any intensity)
     */
    public static final float MIN_RENDER_INTENSITY = 0.0f;
    
    // ========== FUTURE EXTENSIBILITY ==========
    
    /**
     * Enable debug rendering for rain zones (shows zone boundaries).
     * Useful for development and tuning.
     * 
     * Default: false
     */
    public static final boolean DEBUG_RENDER_ZONES = false;
    
    /**
     * Enable performance logging for rain system.
     * Logs particle counts and render times to console.
     * 
     * Default: false
     */
    public static final boolean DEBUG_PERFORMANCE = false;
    
    // Private constructor to prevent instantiation
    private RainConfig() {
        throw new AssertionError("RainConfig is a utility class and should not be instantiated");
    }
}
