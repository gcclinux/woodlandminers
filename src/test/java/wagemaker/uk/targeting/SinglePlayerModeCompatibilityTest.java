package wagemaker.uk.targeting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for single-player mode compatibility with the planting range configuration system.
 * 
 * Verifies that:
 * - TargetingSystem defaults to unlimited range (-1) when not connected to server
 * - Single-player mode without server connection allows unlimited targeting
 * - Planting works correctly in single-player mode
 * 
 * Requirements: 8.1, 8.2, 8.3
 */
public class SinglePlayerModeCompatibilityTest {
    
    private TargetingSystem targetingSystem;
    private TestCallback callback;
    
    @BeforeEach
    public void setUp() {
        targetingSystem = new TargetingSystem();
        callback = new TestCallback();
    }
    
    // ===== Test Callback Implementation =====
    
    private static class TestCallback implements TargetingCallback {
        private boolean confirmCalled = false;
        private boolean cancelCalled = false;
        private float confirmedX = 0;
        private float confirmedY = 0;
        
        @Override
        public void onTargetConfirmed(float targetX, float targetY) {
            this.confirmCalled = true;
            this.confirmedX = targetX;
            this.confirmedY = targetY;
        }
        
        @Override
        public void onTargetCancelled() {
            this.cancelCalled = true;
        }
        
        public void reset() {
            confirmCalled = false;
            cancelCalled = false;
            confirmedX = 0;
            confirmedY = 0;
        }
    }
    
    // ===== Requirement 8.1: TargetingSystem defaults to unlimited range (-1) =====
    
    @Test
    public void testTargetingSystemDefaultsToUnlimitedRange() {
        // Verify default max range is -1 (unlimited)
        assertEquals(-1, targetingSystem.getMaxRange(), 
            "TargetingSystem should default to unlimited range (-1) when not connected to server");
    }
    
    @Test
    public void testUnlimitedRangeAllowsAnyDistance() {
        // Activate targeting at origin
        targetingSystem.activate(0, 0, TargetingMode.ADJACENT, callback);
        
        // Move target very far away (well beyond any reasonable range limit)
        for (int i = 0; i < 100; i++) {
            targetingSystem.moveTarget(Direction.RIGHT);
        }
        
        float[] coords = targetingSystem.getTargetCoordinates();
        
        // Should be at 6400 pixels (100 tiles * 64 pixels)
        assertEquals(6400, coords[0], 
            "With unlimited range, target should move to 6400 pixels");
        assertEquals(0, coords[1], 
            "Y coordinate should remain at 0");
    }
    
    @Test
    public void testIsWithinMaxRangeReturnsTrueForUnlimitedRange() {
        // Activate targeting at origin
        targetingSystem.activate(0, 0, TargetingMode.ADJACENT, callback);
        
        // Move target very far away
        for (int i = 0; i < 50; i++) {
            targetingSystem.moveTarget(Direction.RIGHT);
            targetingSystem.moveTarget(Direction.UP);
        }
        
        // Should always be within range when range is unlimited
        assertTrue(targetingSystem.isWithinMaxRange(), 
            "isWithinMaxRange should return true for unlimited range regardless of distance");
    }
    
    // ===== Requirement 8.2: Single-player mode allows unlimited targeting =====
    
    @Test
    public void testSinglePlayerModeAllowsUnlimitedTargeting() {
        // Simulate single-player mode: no setMaxRange() call (stays at default -1)
        
        // Activate targeting
        targetingSystem.activate(128, 192, TargetingMode.ADJACENT, callback);
        
        // Move target to extreme distances in all directions
        // Move right 20 tiles (1280 pixels)
        for (int i = 0; i < 20; i++) {
            targetingSystem.moveTarget(Direction.RIGHT);
        }
        
        // Move up 20 tiles (1280 pixels)
        for (int i = 0; i < 20; i++) {
            targetingSystem.moveTarget(Direction.UP);
        }
        
        float[] coords = targetingSystem.getTargetCoordinates();
        
        // Should be at (128 + 1280, 192 + 1280) = (1408, 1472)
        assertEquals(1408, coords[0], 
            "Single-player mode should allow targeting at 1408 pixels");
        assertEquals(1472, coords[1], 
            "Single-player mode should allow targeting at 1472 pixels");
        
        // Verify still within range
        assertTrue(targetingSystem.isWithinMaxRange(), 
            "Target should be within range in single-player mode");
    }
    
    @Test
    public void testSinglePlayerModeNeverClampsTargetPosition() {
        // Activate targeting at origin
        targetingSystem.activate(0, 0, TargetingMode.ADJACENT, callback);
        
        // Track positions to ensure no clamping occurs
        float[] previousCoords = targetingSystem.getTargetCoordinates();
        
        // Move in a pattern and verify each move is exactly 64 pixels
        Direction[] moves = {
            Direction.RIGHT, Direction.RIGHT, Direction.RIGHT,
            Direction.UP, Direction.UP, Direction.UP,
            Direction.LEFT, Direction.LEFT,
            Direction.DOWN
        };
        
        for (Direction direction : moves) {
            targetingSystem.moveTarget(direction);
            float[] currentCoords = targetingSystem.getTargetCoordinates();
            
            // Calculate distance moved
            float dx = Math.abs(currentCoords[0] - previousCoords[0]);
            float dy = Math.abs(currentCoords[1] - previousCoords[1]);
            float distanceMoved = (float) Math.sqrt(dx * dx + dy * dy);
            
            // Should always move exactly 64 pixels (one tile)
            assertEquals(64, distanceMoved, 0.01f, 
                "Each move should be exactly 64 pixels (no clamping in single-player mode)");
            
            previousCoords = currentCoords;
        }
    }
    
    @Test
    public void testSinglePlayerModeDiagonalMovementUnrestricted() {
        // Activate targeting at origin
        targetingSystem.activate(0, 0, TargetingMode.ADJACENT, callback);
        
        // Move diagonally to create a large distance
        for (int i = 0; i < 30; i++) {
            targetingSystem.moveTarget(Direction.RIGHT);
            targetingSystem.moveTarget(Direction.UP);
        }
        
        float[] coords = targetingSystem.getTargetCoordinates();
        
        // Should be at (1920, 1920) - diagonal distance ~2715 pixels
        assertEquals(1920, coords[0], "X should be at 1920 pixels");
        assertEquals(1920, coords[1], "Y should be at 1920 pixels");
        
        // Calculate diagonal distance
        double distance = Math.sqrt(coords[0] * coords[0] + coords[1] * coords[1]);
        assertTrue(distance > 2700, 
            "Diagonal distance should exceed 2700 pixels (well beyond any typical range limit)");
        
        // Should still be within range
        assertTrue(targetingSystem.isWithinMaxRange(), 
            "Diagonal movement should be unrestricted in single-player mode");
    }
    
    // ===== Requirement 8.3: Planting works correctly in single-player mode =====
    
    @Test
    public void testPlantingConfirmationWorksInSinglePlayerMode() {
        // Activate targeting
        targetingSystem.activate(128, 192, TargetingMode.ADJACENT, callback);
        
        // Move target to a distant location
        for (int i = 0; i < 10; i++) {
            targetingSystem.moveTarget(Direction.RIGHT);
        }
        
        // Confirm target
        targetingSystem.confirmTarget();
        
        // Verify callback was invoked with correct coordinates
        assertTrue(callback.confirmCalled, 
            "Confirmation callback should be invoked in single-player mode");
        assertEquals(768, callback.confirmedX, 
            "Confirmed X should be 768 (128 + 10*64)");
        assertEquals(192, callback.confirmedY, 
            "Confirmed Y should be 192");
        
        // Verify targeting is deactivated
        assertFalse(targetingSystem.isActive(), 
            "Targeting should be deactivated after confirmation");
    }
    
    @Test
    public void testMultiplePlantingCyclesInSinglePlayerMode() {
        // Simulate multiple planting cycles
        for (int cycle = 0; cycle < 5; cycle++) {
            callback.reset();
            
            // Activate targeting at different positions
            float playerX = cycle * 128;
            float playerY = cycle * 192;
            targetingSystem.activate(playerX, playerY, TargetingMode.ADJACENT, callback);
            
            // Move target away
            for (int i = 0; i < 15; i++) {
                targetingSystem.moveTarget(Direction.RIGHT);
            }
            
            // Confirm
            targetingSystem.confirmTarget();
            
            // Verify each cycle works correctly
            assertTrue(callback.confirmCalled, 
                "Cycle " + cycle + " should invoke confirmation callback");
            assertEquals(playerX + 960, callback.confirmedX, 0.01f, 
                "Cycle " + cycle + " should confirm at correct X");
            assertEquals(playerY, callback.confirmedY, 0.01f, 
                "Cycle " + cycle + " should confirm at correct Y");
        }
    }
    
    @Test
    public void testCancellationWorksInSinglePlayerMode() {
        // Activate targeting
        targetingSystem.activate(0, 0, TargetingMode.ADJACENT, callback);
        
        // Move target far away
        for (int i = 0; i < 20; i++) {
            targetingSystem.moveTarget(Direction.UP);
        }
        
        // Cancel targeting
        targetingSystem.cancel();
        
        // Verify cancellation callback was invoked
        assertTrue(callback.cancelCalled, 
            "Cancellation callback should be invoked in single-player mode");
        assertFalse(callback.confirmCalled, 
            "Confirmation callback should not be invoked on cancellation");
        
        // Verify targeting is deactivated
        assertFalse(targetingSystem.isActive(), 
            "Targeting should be deactivated after cancellation");
    }
    
    // ===== Edge Cases and Boundary Conditions =====
    
    @Test
    public void testNegativeCoordinatesInSinglePlayerMode() {
        // Activate targeting at origin
        targetingSystem.activate(0, 0, TargetingMode.ADJACENT, callback);
        
        // Move into negative coordinates
        for (int i = 0; i < 25; i++) {
            targetingSystem.moveTarget(Direction.LEFT);
            targetingSystem.moveTarget(Direction.DOWN);
        }
        
        float[] coords = targetingSystem.getTargetCoordinates();
        
        // Should be at (-1600, -1600)
        assertEquals(-1600, coords[0], "Should allow negative X coordinates");
        assertEquals(-1600, coords[1], "Should allow negative Y coordinates");
        
        // Should still be within range
        assertTrue(targetingSystem.isWithinMaxRange(), 
            "Negative coordinates should be within range in single-player mode");
    }
    
    @Test
    public void testExtremeDistancesInSinglePlayerMode() {
        // Activate targeting at origin
        targetingSystem.activate(0, 0, TargetingMode.ADJACENT, callback);
        
        // Move to extreme distance (200 tiles = 12800 pixels)
        for (int i = 0; i < 200; i++) {
            targetingSystem.moveTarget(Direction.RIGHT);
        }
        
        float[] coords = targetingSystem.getTargetCoordinates();
        
        // Should be at 12800 pixels
        assertEquals(12800, coords[0], 
            "Should allow extreme distances in single-player mode");
        
        // Confirm at extreme distance
        targetingSystem.confirmTarget();
        
        assertTrue(callback.confirmCalled, 
            "Should be able to confirm at extreme distances");
        assertEquals(12800, callback.confirmedX, 
            "Should confirm at extreme distance");
    }
    
    @Test
    public void testSwitchingFromUnlimitedToLimitedRange() {
        // Start in single-player mode (unlimited)
        assertEquals(-1, targetingSystem.getMaxRange(), 
            "Should start with unlimited range");
        
        // Activate and move far away
        targetingSystem.activate(0, 0, TargetingMode.ADJACENT, callback);
        for (int i = 0; i < 20; i++) {
            targetingSystem.moveTarget(Direction.RIGHT);
        }
        
        float[] coords1 = targetingSystem.getTargetCoordinates();
        assertEquals(1280, coords1[0], "Should be at 1280 pixels");
        
        // Deactivate
        targetingSystem.deactivate();
        
        // Simulate connecting to server with range limit
        targetingSystem.setMaxRange(512);
        assertEquals(512, targetingSystem.getMaxRange(), 
            "Should now have limited range");
        
        // Activate again and try to move far away
        targetingSystem.activate(0, 0, TargetingMode.ADJACENT, callback);
        for (int i = 0; i < 20; i++) {
            targetingSystem.moveTarget(Direction.RIGHT);
        }
        
        float[] coords2 = targetingSystem.getTargetCoordinates();
        
        // Should be clamped to max range
        assertTrue(coords2[0] <= 512, 
            "Should be clamped to 512 pixels after setting range limit");
    }
    
    @Test
    public void testSwitchingFromLimitedToUnlimitedRange() {
        // Start with limited range (simulating multiplayer)
        targetingSystem.setMaxRange(512);
        
        // Activate and try to move far away
        targetingSystem.activate(0, 0, TargetingMode.ADJACENT, callback);
        for (int i = 0; i < 20; i++) {
            targetingSystem.moveTarget(Direction.RIGHT);
        }
        
        float[] coords1 = targetingSystem.getTargetCoordinates();
        assertTrue(coords1[0] <= 512, "Should be clamped to 512 pixels");
        
        // Deactivate
        targetingSystem.deactivate();
        
        // Simulate disconnecting from server (back to single-player)
        targetingSystem.setMaxRange(-1);
        assertEquals(-1, targetingSystem.getMaxRange(), 
            "Should now have unlimited range");
        
        // Activate again and move far away
        targetingSystem.activate(0, 0, TargetingMode.ADJACENT, callback);
        for (int i = 0; i < 20; i++) {
            targetingSystem.moveTarget(Direction.RIGHT);
        }
        
        float[] coords2 = targetingSystem.getTargetCoordinates();
        assertEquals(1280, coords2[0], 
            "Should allow unlimited movement after removing range limit");
    }
}
