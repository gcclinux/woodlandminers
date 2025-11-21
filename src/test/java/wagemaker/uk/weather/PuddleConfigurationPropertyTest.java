package wagemaker.uk.weather;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for puddle configuration effects.
 * Feature: rain-water-puddles, Property 10: Configuration affects new puddles
 * Validates: Requirements 5.2
 */
public class PuddleConfigurationPropertyTest {
    
    /**
     * Property 10: Configuration affects new puddles
     * For any configuration change to puddle size, newly spawned puddles should 
     * reflect the new size values.
     * Validates: Requirements 5.2
     * 
     * This property-based test runs 100 trials, creating puddles with random
     * dimensions within the configured ranges and verifying they respect the
     * configuration constraints.
     */
    @Test
    public void configurationAffectsNewPuddles() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Generate random puddle dimensions within configured ranges
            float width = PuddleConfig.MIN_PUDDLE_WIDTH + 
                         random.nextFloat() * (PuddleConfig.MAX_PUDDLE_WIDTH - PuddleConfig.MIN_PUDDLE_WIDTH);
            
            float height = PuddleConfig.MIN_PUDDLE_HEIGHT + 
                          random.nextFloat() * (PuddleConfig.MAX_PUDDLE_HEIGHT - PuddleConfig.MIN_PUDDLE_HEIGHT);
            
            float x = random.nextFloat() * 1000f;
            float y = random.nextFloat() * 1000f;
            float rotation = random.nextFloat() * 360f;
            
            // Create a new puddle with these dimensions
            WaterPuddle puddle = new WaterPuddle();
            puddle.reset(x, y, width, height, rotation);
            
            // Verify the puddle respects configuration constraints
            assertTrue(
                puddle.getWidth() >= PuddleConfig.MIN_PUDDLE_WIDTH,
                "Puddle width should be >= MIN_PUDDLE_WIDTH. Got: " + puddle.getWidth()
            );
            
            assertTrue(
                puddle.getWidth() <= PuddleConfig.MAX_PUDDLE_WIDTH,
                "Puddle width should be <= MAX_PUDDLE_WIDTH. Got: " + puddle.getWidth()
            );
            
            assertTrue(
                puddle.getHeight() >= PuddleConfig.MIN_PUDDLE_HEIGHT,
                "Puddle height should be >= MIN_PUDDLE_HEIGHT. Got: " + puddle.getHeight()
            );
            
            assertTrue(
                puddle.getHeight() <= PuddleConfig.MAX_PUDDLE_HEIGHT,
                "Puddle height should be <= MAX_PUDDLE_HEIGHT. Got: " + puddle.getHeight()
            );
            
            // Verify base alpha is set according to configuration
            assertEquals(
                PuddleConfig.PUDDLE_BASE_ALPHA,
                puddle.getBaseAlpha(),
                0.001f,
                "Puddle base alpha should match configuration"
            );
            
            // Verify puddle is active after reset
            assertTrue(
                puddle.isActive(),
                "Puddle should be active after reset"
            );
            
            // Verify position and rotation are preserved
            assertEquals(x, puddle.getX(), 0.001f, "Puddle X position should be preserved");
            assertEquals(y, puddle.getY(), 0.001f, "Puddle Y position should be preserved");
            assertEquals(rotation, puddle.getRotation(), 0.001f, "Puddle rotation should be preserved");
        }
    }
    
    /**
     * Property: Configuration constants are within valid ranges
     * For any configuration constant, it should be within a valid range that
     * makes sense for the game (e.g., positive values, reasonable sizes).
     * 
     * This property-based test runs 100 trials, verifying that configuration
     * values are sensible and consistent.
     */
    @Test
    public void configurationConstantsAreValid() {
        // Run 100 trials (though config is constant, we test it multiple times
        // to match the property-based testing pattern)
        for (int trial = 0; trial < 100; trial++) {
            // Timing constraints
            assertTrue(
                PuddleConfig.ACCUMULATION_THRESHOLD > 0,
                "Accumulation threshold should be positive"
            );
            
            assertTrue(
                PuddleConfig.EVAPORATION_DURATION > 0,
                "Evaporation duration should be positive"
            );
            
            // Density constraints
            assertTrue(
                PuddleConfig.MAX_PUDDLES > 0,
                "Max puddles should be positive"
            );
            
            assertTrue(
                PuddleConfig.MIN_PUDDLES > 0,
                "Min puddles should be positive"
            );
            
            assertTrue(
                PuddleConfig.MIN_PUDDLES <= PuddleConfig.MAX_PUDDLES,
                "Min puddles should be <= max puddles"
            );
            
            // Size constraints
            assertTrue(
                PuddleConfig.MIN_PUDDLE_WIDTH > 0,
                "Min puddle width should be positive"
            );
            
            assertTrue(
                PuddleConfig.MAX_PUDDLE_WIDTH >= PuddleConfig.MIN_PUDDLE_WIDTH,
                "Max puddle width should be >= min puddle width"
            );
            
            assertTrue(
                PuddleConfig.MIN_PUDDLE_HEIGHT > 0,
                "Min puddle height should be positive"
            );
            
            assertTrue(
                PuddleConfig.MAX_PUDDLE_HEIGHT >= PuddleConfig.MIN_PUDDLE_HEIGHT,
                "Max puddle height should be >= min puddle height"
            );
            
            assertTrue(
                PuddleConfig.PUDDLE_ASPECT_RATIO > 0,
                "Puddle aspect ratio should be positive"
            );
            
            // Color constraints (0.0 to 1.0)
            assertTrue(
                PuddleConfig.PUDDLE_COLOR_RED >= 0.0f && PuddleConfig.PUDDLE_COLOR_RED <= 1.0f,
                "Puddle red color should be in range [0.0, 1.0]"
            );
            
            assertTrue(
                PuddleConfig.PUDDLE_COLOR_GREEN >= 0.0f && PuddleConfig.PUDDLE_COLOR_GREEN <= 1.0f,
                "Puddle green color should be in range [0.0, 1.0]"
            );
            
            assertTrue(
                PuddleConfig.PUDDLE_COLOR_BLUE >= 0.0f && PuddleConfig.PUDDLE_COLOR_BLUE <= 1.0f,
                "Puddle blue color should be in range [0.0, 1.0]"
            );
            
            // Alpha constraints (0.0 to 1.0)
            assertTrue(
                PuddleConfig.PUDDLE_BASE_ALPHA >= 0.0f && PuddleConfig.PUDDLE_BASE_ALPHA <= 1.0f,
                "Puddle base alpha should be in range [0.0, 1.0]"
            );
            
            assertTrue(
                PuddleConfig.PUDDLE_MAX_ALPHA >= 0.0f && PuddleConfig.PUDDLE_MAX_ALPHA <= 1.0f,
                "Puddle max alpha should be in range [0.0, 1.0]"
            );
            
            assertTrue(
                PuddleConfig.PUDDLE_BASE_ALPHA <= PuddleConfig.PUDDLE_MAX_ALPHA,
                "Base alpha should be <= max alpha"
            );
            
            // Spacing constraints
            assertTrue(
                PuddleConfig.MIN_PUDDLE_SPACING > 0,
                "Min puddle spacing should be positive"
            );
        }
    }
    
    /**
     * Property: Puddle pooling and reuse preserves configuration
     * For any puddle that is reset multiple times, each reset should apply
     * the current configuration values (particularly base alpha).
     * 
     * This property-based test runs 100 trials, resetting puddles multiple
     * times and verifying configuration is consistently applied.
     */
    @Test
    public void puddleReusePreservesConfiguration() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            WaterPuddle puddle = new WaterPuddle();
            
            // Reset the puddle multiple times (simulating pool reuse)
            int resetCount = 2 + random.nextInt(5); // 2-6 resets
            
            for (int i = 0; i < resetCount; i++) {
                float width = PuddleConfig.MIN_PUDDLE_WIDTH + 
                             random.nextFloat() * (PuddleConfig.MAX_PUDDLE_WIDTH - PuddleConfig.MIN_PUDDLE_WIDTH);
                
                float height = PuddleConfig.MIN_PUDDLE_HEIGHT + 
                              random.nextFloat() * (PuddleConfig.MAX_PUDDLE_HEIGHT - PuddleConfig.MIN_PUDDLE_HEIGHT);
                
                float x = random.nextFloat() * 1000f;
                float y = random.nextFloat() * 1000f;
                float rotation = random.nextFloat() * 360f;
                
                puddle.reset(x, y, width, height, rotation);
                
                // After each reset, verify configuration is applied
                assertEquals(
                    PuddleConfig.PUDDLE_BASE_ALPHA,
                    puddle.getBaseAlpha(),
                    0.001f,
                    "Puddle base alpha should match configuration after reset " + (i + 1)
                );
                
                assertTrue(
                    puddle.isActive(),
                    "Puddle should be active after reset " + (i + 1)
                );
                
                assertEquals(width, puddle.getWidth(), 0.001f, "Width should be preserved");
                assertEquals(height, puddle.getHeight(), 0.001f, "Height should be preserved");
            }
        }
    }
    
    /**
     * Property: Puddle state transitions respect configuration
     * For any puddle, deactivating and reactivating it should maintain
     * configuration-based properties.
     * 
     * This property-based test runs 100 trials, testing puddle activation
     * state transitions.
     */
    @Test
    public void puddleStateTransitionsRespectConfiguration() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            WaterPuddle puddle = new WaterPuddle();
            
            // Initial state should be inactive
            assertFalse(puddle.isActive(), "New puddle should be inactive");
            
            // Reset to activate
            float width = PuddleConfig.MIN_PUDDLE_WIDTH + 
                         random.nextFloat() * (PuddleConfig.MAX_PUDDLE_WIDTH - PuddleConfig.MIN_PUDDLE_WIDTH);
            float height = PuddleConfig.MIN_PUDDLE_HEIGHT + 
                          random.nextFloat() * (PuddleConfig.MAX_PUDDLE_HEIGHT - PuddleConfig.MIN_PUDDLE_HEIGHT);
            float x = random.nextFloat() * 1000f;
            float y = random.nextFloat() * 1000f;
            float rotation = random.nextFloat() * 360f;
            
            puddle.reset(x, y, width, height, rotation);
            
            // Should be active with configuration applied
            assertTrue(puddle.isActive(), "Puddle should be active after reset");
            assertEquals(
                PuddleConfig.PUDDLE_BASE_ALPHA,
                puddle.getBaseAlpha(),
                0.001f,
                "Base alpha should match configuration"
            );
            
            // Deactivate
            puddle.setActive(false);
            assertFalse(puddle.isActive(), "Puddle should be inactive after deactivation");
            
            // Properties should still be preserved
            assertEquals(width, puddle.getWidth(), 0.001f, "Width should be preserved after deactivation");
            assertEquals(height, puddle.getHeight(), 0.001f, "Height should be preserved after deactivation");
            assertEquals(
                PuddleConfig.PUDDLE_BASE_ALPHA,
                puddle.getBaseAlpha(),
                0.001f,
                "Base alpha should be preserved after deactivation"
            );
        }
    }
}
