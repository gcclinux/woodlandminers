package wagemaker.uk.weather;

/**
 * Configuration constants for the water puddle system.
 * Defines timing, density, visual properties, and spacing for puddles.
 */
public class PuddleConfig {
    
    // Timing
    /** Seconds of continuous rain before puddles appear */
    public static final float ACCUMULATION_THRESHOLD = 5.0f;
    
    /** Seconds for puddles to fade out after rain stops */
    public static final float EVAPORATION_DURATION = 5.0f;
    
    // Density
    /** Maximum number of puddles on screen */
    public static final int MAX_PUDDLES = 4;
    
    /** Minimum number of puddles at low intensity */
    public static final int MIN_PUDDLES = 3;
    
    // Visual Properties - Size
    /** Minimum puddle width in pixels (horizontal) */
    public static final float MIN_PUDDLE_WIDTH = 40.0f;
    
    /** Maximum puddle width in pixels (horizontal) */
    public static final float MAX_PUDDLE_WIDTH = 80.0f;
    
    /** Minimum puddle height in pixels (vertical - always less than width) */
    public static final float MIN_PUDDLE_HEIGHT = 20.0f;
    
    /** Maximum puddle height in pixels (vertical - always less than width) */
    public static final float MAX_PUDDLE_HEIGHT = 40.0f;
    
    /** Width to height ratio for puddles */
    public static final float PUDDLE_ASPECT_RATIO = 1.5f;
    
    // Visual Properties - Color (blue-gray water)
    // Adjusted for optimal visibility across different zoom levels
    /** Red component of puddle color (0.0 to 1.0) */
    public static final float PUDDLE_COLOR_RED = 0.35f;
    
    /** Green component of puddle color (0.0 to 1.0) */
    public static final float PUDDLE_COLOR_GREEN = 0.45f;
    
    /** Blue component of puddle color (0.0 to 1.0) */
    public static final float PUDDLE_COLOR_BLUE = 0.75f;
    
    /** Base transparency level - increased for better visibility */
    public static final float PUDDLE_BASE_ALPHA = 0.5f;
    
    /** Maximum transparency level */
    public static final float PUDDLE_MAX_ALPHA = 0.7f;
    
    // Spacing
    /** Minimum distance between puddle centers in pixels */
    public static final float MIN_PUDDLE_SPACING = 150.0f;
    
    /** Minimum distance from tree centers to puddle centers in pixels */
    public static final float MIN_TREE_DISTANCE = 400.0f;
    
    // Performance
    /** Enable or disable the puddle system entirely */
    public static boolean PUDDLES_ENABLED = true;
    
    /** Enable debug logging for puddle count monitoring */
    public static boolean DEBUG_LOGGING_ENABLED = false;
    
    // Private constructor to prevent instantiation
    private PuddleConfig() {
        throw new AssertionError("PuddleConfig is a utility class and should not be instantiated");
    }
}
