package wagemaker.uk.respawn;

import wagemaker.uk.network.TreeType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Configuration for respawn timers with hardcoded default values.
 * All respawn durations and settings are defined as constants in this class.
 * 
 * <h2>Default Configuration:</h2>
 * <ul>
 *   <li>Default respawn duration: 15 minutes (900,000 ms)</li>
 *   <li>Visual indicator threshold: 1 minute (60,000 ms)</li>
 *   <li>Visual indicators: Enabled</li>
 * </ul>
 * 
 * <h2>Customization Instructions:</h2>
 * To customize respawn behavior:
 * <ol>
 *   <li>Modify the constant values in this class (see RESPAWN DURATION CONFIGURATION section)</li>
 *   <li>Recompile the game using: <code>./gradlew build</code></li>
 *   <li>Run the updated game</li>
 * </ol>
 * 
 * <h2>Time Conversions:</h2>
 * <ul>
 *   <li>1 second = 1,000 milliseconds</li>
 *   <li>1 minute = 60,000 milliseconds</li>
 *   <li>5 minutes = 300,000 milliseconds</li>
 *   <li>10 minutes = 600,000 milliseconds</li>
 *   <li>15 minutes = 900,000 milliseconds (default)</li>
 *   <li>30 minutes = 1,800,000 milliseconds</li>
 *   <li>1 hour = 3,600,000 milliseconds</li>
 * </ul>
 * 
 * <h2>Example Customizations:</h2>
 * <pre>
 * // Fast respawn (5 minutes for all resources)
 * private static final long DEFAULT_RESPAWN_DURATION = 300000;
 * 
 * // Quick bamboo respawn (2 minutes)
 * private static final long BAMBOO_TREE_RESPAWN_DURATION = 120000;
 * 
 * // Show indicator 2 minutes before respawn
 * private static final long VISUAL_INDICATOR_THRESHOLD = 120000;
 * 
 * // Disable visual indicators
 * private static final boolean VISUAL_INDICATOR_ENABLED = false;
 * </pre>
 */
public class RespawnConfig {
    // ============================================================================
    // RESPAWN DURATION CONFIGURATION
    // ============================================================================
    
    /**
     * Default respawn duration for all resources (15 minutes).
     * This value is used when no specific override is defined for a resource type.
     */
    private static final long DEFAULT_RESPAWN_DURATION = 900000; // 15 minutes in milliseconds
    
    /**
     * Respawn duration for Apple trees (15 minutes).
     */
    private static final long APPLE_TREE_RESPAWN_DURATION = 900000;
    
    /**
     * Respawn duration for Banana trees (15 minutes).
     */
    private static final long BANANA_TREE_RESPAWN_DURATION = 900000;
    
    /**
     * Respawn duration for Coconut trees (15 minutes).
     */
    private static final long COCONUT_TREE_RESPAWN_DURATION = 900000;
    
    /**
     * Respawn duration for Bamboo trees (15 minutes).
     */
    private static final long BAMBOO_TREE_RESPAWN_DURATION = 900000;
    
    /**
     * Respawn duration for Small trees (15 minutes).
     */
    private static final long SMALL_TREE_RESPAWN_DURATION = 900000;
    
    /**
     * Respawn duration for Cactus (15 minutes).
     */
    private static final long CACTUS_RESPAWN_DURATION = 900000;
    
    /**
     * Respawn duration for Stones (15 minutes).
     */
    private static final long STONE_RESPAWN_DURATION = 900000;
    
    // ============================================================================
    // VISUAL INDICATOR CONFIGURATION
    // ============================================================================
    
    /**
     * Whether visual respawn indicators are enabled.
     * When true, a visual effect appears at resource locations before they respawn.
     */
    private static final boolean VISUAL_INDICATOR_ENABLED = true;
    
    /**
     * Time threshold for showing visual indicators (1 minute).
     * The indicator appears when remaining respawn time is less than this value.
     */
    private static final long VISUAL_INDICATOR_THRESHOLD = 60000; // 1 minute in milliseconds
    
    // ============================================================================
    // INSTANCE FIELDS
    // ============================================================================
    
    private long defaultRespawnDuration;
    private Map<TreeType, Long> treeRespawnDurations;
    private long stoneRespawnDuration;
    private boolean visualIndicatorEnabled;
    private long visualIndicatorThreshold;
    
    /**
     * Private constructor for creating a RespawnConfig instance with hardcoded values.
     */
    private RespawnConfig() {
        this.defaultRespawnDuration = DEFAULT_RESPAWN_DURATION;
        this.treeRespawnDurations = new EnumMap<>(TreeType.class);
        
        // Initialize tree-specific respawn durations
        this.treeRespawnDurations.put(TreeType.APPLE, APPLE_TREE_RESPAWN_DURATION);
        this.treeRespawnDurations.put(TreeType.BANANA, BANANA_TREE_RESPAWN_DURATION);
        this.treeRespawnDurations.put(TreeType.COCONUT, COCONUT_TREE_RESPAWN_DURATION);
        this.treeRespawnDurations.put(TreeType.BAMBOO, BAMBOO_TREE_RESPAWN_DURATION);
        this.treeRespawnDurations.put(TreeType.SMALL, SMALL_TREE_RESPAWN_DURATION);
        this.treeRespawnDurations.put(TreeType.CACTUS, CACTUS_RESPAWN_DURATION);
        
        this.stoneRespawnDuration = STONE_RESPAWN_DURATION;
        this.visualIndicatorEnabled = VISUAL_INDICATOR_ENABLED;
        this.visualIndicatorThreshold = VISUAL_INDICATOR_THRESHOLD;
        
        System.out.println("[RespawnConfig] Initialized with hardcoded configuration values");
        System.out.println("[RespawnConfig] Default respawn duration: " + defaultRespawnDuration + "ms (" + (defaultRespawnDuration / 60000) + " minutes)");
        System.out.println("[RespawnConfig] Visual indicators: " + (visualIndicatorEnabled ? "enabled" : "disabled"));
        System.out.println("[RespawnConfig] Visual indicator threshold: " + visualIndicatorThreshold + "ms (" + (visualIndicatorThreshold / 1000) + " seconds)");
    }
    
    /**
     * Creates and returns a RespawnConfig instance with hardcoded values.
     * This method replaces the previous file-based configuration loading.
     * 
     * @return RespawnConfig instance with hardcoded configuration values
     */
    public static RespawnConfig load() {
        return new RespawnConfig();
    }
    
    /**
     * Creates a RespawnConfig with default values.
     * 
     * @return RespawnConfig instance with all default values
     */
    public static RespawnConfig getDefault() {
        return new RespawnConfig();
    }
    
    /**
     * Gets the respawn duration for a specific tree type.
     * Falls back to default duration if no specific override is configured.
     * 
     * @param treeType Type of tree
     * @return Respawn duration in milliseconds
     */
    public long getTreeRespawnDuration(TreeType treeType) {
        return treeRespawnDurations.getOrDefault(treeType, defaultRespawnDuration);
    }
    
    /**
     * Gets the respawn duration for stones.
     * 
     * @return Respawn duration in milliseconds
     */
    public long getStoneRespawnDuration() {
        return stoneRespawnDuration;
    }
    
    /**
     * Gets the default respawn duration.
     * 
     * @return Default respawn duration in milliseconds
     */
    public long getDefaultRespawnDuration() {
        return defaultRespawnDuration;
    }
    
    /**
     * Gets the respawn duration for a specific resource type.
     * 
     * @param resourceType Type of resource
     * @param treeType Type of tree (null for stones)
     * @return Respawn duration in milliseconds
     */
    public long getRespawnDuration(ResourceType resourceType, TreeType treeType) {
        if (resourceType == ResourceType.TREE && treeType != null) {
            return getTreeRespawnDuration(treeType);
        } else if (resourceType == ResourceType.STONE) {
            return getStoneRespawnDuration();
        }
        return defaultRespawnDuration;
    }
    
    /**
     * Checks if visual indicators are enabled.
     * 
     * @return true if enabled, false otherwise
     */
    public boolean isVisualIndicatorEnabled() {
        return visualIndicatorEnabled;
    }
    
    /**
     * Gets the threshold for showing visual indicators.
     * Indicators are shown when remaining time is less than this value.
     * 
     * @return Threshold in milliseconds
     */
    public long getVisualIndicatorThreshold() {
        return visualIndicatorThreshold;
    }
    
    /**
     * Sets the default respawn duration.
     * 
     * @param defaultRespawnDuration Duration in milliseconds (must be positive)
     */
    public void setDefaultRespawnDuration(long defaultRespawnDuration) {
        if (defaultRespawnDuration > 0) {
            this.defaultRespawnDuration = defaultRespawnDuration;
        }
    }
    
    /**
     * Sets the respawn duration for a specific tree type.
     * 
     * @param treeType Type of tree
     * @param duration Duration in milliseconds (must be positive)
     */
    public void setTreeRespawnDuration(TreeType treeType, long duration) {
        if (duration > 0) {
            this.treeRespawnDurations.put(treeType, duration);
        }
    }
    
    /**
     * Sets the respawn duration for stones.
     * 
     * @param stoneRespawnDuration Duration in milliseconds (must be positive)
     */
    public void setStoneRespawnDuration(long stoneRespawnDuration) {
        if (stoneRespawnDuration > 0) {
            this.stoneRespawnDuration = stoneRespawnDuration;
        }
    }
    
    /**
     * Sets whether visual indicators are enabled.
     * 
     * @param visualIndicatorEnabled true to enable, false to disable
     */
    public void setVisualIndicatorEnabled(boolean visualIndicatorEnabled) {
        this.visualIndicatorEnabled = visualIndicatorEnabled;
    }
    
    /**
     * Sets the threshold for showing visual indicators.
     * 
     * @param visualIndicatorThreshold Threshold in milliseconds (must be positive)
     */
    public void setVisualIndicatorThreshold(long visualIndicatorThreshold) {
        if (visualIndicatorThreshold > 0) {
            this.visualIndicatorThreshold = visualIndicatorThreshold;
        }
    }
    
    @Override
    public String toString() {
        return "RespawnConfig{" +
                "defaultRespawnDuration=" + defaultRespawnDuration +
                ", treeRespawnDurations=" + treeRespawnDurations +
                ", stoneRespawnDuration=" + stoneRespawnDuration +
                ", visualIndicatorEnabled=" + visualIndicatorEnabled +
                ", visualIndicatorThreshold=" + visualIndicatorThreshold +
                '}';
    }
}
