package wagemaker.uk.respawn;

import org.junit.jupiter.api.Test;
import wagemaker.uk.network.TreeType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RespawnConfig class.
 * Tests configuration loading, default values, and validation.
 * 
 * Requirements: 4.1, 4.2, 4.3, 4.4
 */
public class RespawnConfigTest {
    
    // ===== Default Configuration Tests =====
    
    @Test
    public void testGetDefaultReturnsValidConfig() {
        RespawnConfig config = RespawnConfig.getDefault();
        assertNotNull(config, "Default config should not be null");
    }
    
    @Test
    public void testDefaultRespawnDuration() {
        RespawnConfig config = RespawnConfig.getDefault();
        assertEquals(900000, config.getDefaultRespawnDuration(), 
                    "Default respawn duration should be 15 minutes (900000ms)");
    }
    
    @Test
    public void testDefaultStoneRespawnDuration() {
        RespawnConfig config = RespawnConfig.getDefault();
        assertEquals(900000, config.getStoneRespawnDuration(), 
                    "Default stone respawn duration should be 15 minutes");
    }
    
    @Test
    public void testDefaultTreeRespawnDurations() {
        RespawnConfig config = RespawnConfig.getDefault();
        
        // All tree types should fall back to default duration
        assertEquals(900000, config.getTreeRespawnDuration(TreeType.APPLE), 
                    "Apple tree should use default duration");
        assertEquals(900000, config.getTreeRespawnDuration(TreeType.BANANA), 
                    "Banana tree should use default duration");
        assertEquals(900000, config.getTreeRespawnDuration(TreeType.COCONUT), 
                    "Coconut tree should use default duration");
        assertEquals(900000, config.getTreeRespawnDuration(TreeType.BAMBOO), 
                    "Bamboo tree should use default duration");
        assertEquals(900000, config.getTreeRespawnDuration(TreeType.SMALL), 
                    "Small tree should use default duration");
        assertEquals(900000, config.getTreeRespawnDuration(TreeType.CACTUS), 
                    "Cactus should use default duration");
    }
    
    @Test
    public void testDefaultVisualIndicatorEnabled() {
        RespawnConfig config = RespawnConfig.getDefault();
        assertTrue(config.isVisualIndicatorEnabled(), 
                  "Visual indicator should be enabled by default");
    }
    
    @Test
    public void testDefaultVisualIndicatorThreshold() {
        RespawnConfig config = RespawnConfig.getDefault();
        assertEquals(60000, config.getVisualIndicatorThreshold(), 
                    "Visual indicator threshold should be 1 minute (60000ms)");
    }
    
    // ===== Configuration Loading Tests =====
    
    @Test
    public void testLoadConfigWithoutFile() {
        // Should return config with default values when file doesn't exist
        RespawnConfig config = RespawnConfig.load();
        assertNotNull(config, "Config should not be null even without file");
        assertEquals(900000, config.getDefaultRespawnDuration(), 
                    "Should use default duration when file not found");
    }
    
    // ===== Setter Tests =====
    
    @Test
    public void testSetDefaultRespawnDuration() {
        RespawnConfig config = RespawnConfig.getDefault();
        config.setDefaultRespawnDuration(600000);
        assertEquals(600000, config.getDefaultRespawnDuration(), 
                    "Should update default respawn duration");
    }
    
    @Test
    public void testSetDefaultRespawnDurationNegativeIgnored() {
        RespawnConfig config = RespawnConfig.getDefault();
        long originalDuration = config.getDefaultRespawnDuration();
        config.setDefaultRespawnDuration(-1000);
        assertEquals(originalDuration, config.getDefaultRespawnDuration(), 
                    "Negative duration should be ignored");
    }
    
    @Test
    public void testSetTreeRespawnDuration() {
        RespawnConfig config = RespawnConfig.getDefault();
        config.setTreeRespawnDuration(TreeType.APPLE, 1200000);
        assertEquals(1200000, config.getTreeRespawnDuration(TreeType.APPLE), 
                    "Should update apple tree respawn duration");
    }
    
    @Test
    public void testSetTreeRespawnDurationNegativeIgnored() {
        RespawnConfig config = RespawnConfig.getDefault();
        long originalDuration = config.getTreeRespawnDuration(TreeType.APPLE);
        config.setTreeRespawnDuration(TreeType.APPLE, -5000);
        assertEquals(originalDuration, config.getTreeRespawnDuration(TreeType.APPLE), 
                    "Negative duration should be ignored");
    }
    
    @Test
    public void testSetStoneRespawnDuration() {
        RespawnConfig config = RespawnConfig.getDefault();
        config.setStoneRespawnDuration(1800000);
        assertEquals(1800000, config.getStoneRespawnDuration(), 
                    "Should update stone respawn duration");
    }
    
    @Test
    public void testSetStoneRespawnDurationNegativeIgnored() {
        RespawnConfig config = RespawnConfig.getDefault();
        long originalDuration = config.getStoneRespawnDuration();
        config.setStoneRespawnDuration(-3000);
        assertEquals(originalDuration, config.getStoneRespawnDuration(), 
                    "Negative duration should be ignored");
    }
    
    @Test
    public void testSetVisualIndicatorEnabled() {
        RespawnConfig config = RespawnConfig.getDefault();
        config.setVisualIndicatorEnabled(false);
        assertFalse(config.isVisualIndicatorEnabled(), 
                   "Should disable visual indicator");
    }
    
    @Test
    public void testSetVisualIndicatorThreshold() {
        RespawnConfig config = RespawnConfig.getDefault();
        config.setVisualIndicatorThreshold(30000);
        assertEquals(30000, config.getVisualIndicatorThreshold(), 
                    "Should update visual indicator threshold");
    }
    
    @Test
    public void testSetVisualIndicatorThresholdNegativeIgnored() {
        RespawnConfig config = RespawnConfig.getDefault();
        long originalThreshold = config.getVisualIndicatorThreshold();
        config.setVisualIndicatorThreshold(-1000);
        assertEquals(originalThreshold, config.getVisualIndicatorThreshold(), 
                    "Negative threshold should be ignored");
    }
    
    // ===== getRespawnDuration Tests =====
    
    @Test
    public void testGetRespawnDurationForTree() {
        RespawnConfig config = RespawnConfig.getDefault();
        config.setTreeRespawnDuration(TreeType.BANANA, 1500000);
        
        long duration = config.getRespawnDuration(ResourceType.TREE, TreeType.BANANA);
        assertEquals(1500000, duration, 
                    "Should return tree-specific duration");
    }
    
    @Test
    public void testGetRespawnDurationForStone() {
        RespawnConfig config = RespawnConfig.getDefault();
        config.setStoneRespawnDuration(2000000);
        
        long duration = config.getRespawnDuration(ResourceType.STONE, null);
        assertEquals(2000000, duration, 
                    "Should return stone duration");
    }
    
    @Test
    public void testGetRespawnDurationFallbackToDefault() {
        RespawnConfig config = RespawnConfig.getDefault();
        
        // Since all tree types now have hardcoded values, test that changing default
        // doesn't affect trees with specific overrides
        long originalCoconutDuration = config.getTreeRespawnDuration(TreeType.COCONUT);
        config.setDefaultRespawnDuration(1000000);
        
        long duration = config.getRespawnDuration(ResourceType.TREE, TreeType.COCONUT);
        assertEquals(originalCoconutDuration, duration, 
                    "Tree with hardcoded override should not use changed default");
    }
}
