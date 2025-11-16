package wagemaker.uk.targeting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TargetingSystem class.
 * Tests activation/deactivation, target movement, coordinate snapping,
 * callback invocation, and state tracking.
 * 
 * Requirements: All
 */
public class TargetingSystemTest {
    
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
    
    // ===== Activation and Deactivation Tests =====
    
    @Test
    public void testInitialStateIsInactive() {
        assertFalse(targetingSystem.isActive(), "Targeting system should be inactive initially");
    }
    
    @Test
    public void testActivateChangesStateToActive() {
        targetingSystem.activate(100, 200, TargetingMode.ADJACENT, callback);
        assertTrue(targetingSystem.isActive(), "Targeting system should be active after activation");
    }
    
    @Test
    public void testDeactivateChangesStateToInactive() {
        targetingSystem.activate(100, 200, TargetingMode.ADJACENT, callback);
        targetingSystem.deactivate();
        assertFalse(targetingSystem.isActive(), "Targeting system should be inactive after deactivation");
    }
    
    @Test
    public void testActivateInitializesTargetAtPlayerPosition() {
        targetingSystem.activate(128, 192, TargetingMode.ADJACENT, callback);
        float[] coords = targetingSystem.getTargetCoordinates();
        assertEquals(128, coords[0], "Target X should be initialized to player X");
        assertEquals(192, coords[1], "Target Y should be initialized to player Y");
    }
    
    @Test
    public void testActivateSnapsPlayerPositionToGrid() {
        targetingSystem.activate(150, 210, TargetingMode.ADJACENT, callback);
        float[] coords = targetingSystem.getTargetCoordinates();
        assertEquals(128, coords[0], "Target X should be snapped to tile grid (150 -> 128)");
        assertEquals(192, coords[1], "Target Y should be snapped to tile grid (210 -> 192)");
    }
    
    @Test
    public void testActivateWhileActiveCallsCancelOnPreviousCallback() {
        TestCallback firstCallback = new TestCallback();
        TestCallback secondCallback = new TestCallback();
        
        targetingSystem.activate(100, 200, TargetingMode.ADJACENT, firstCallback);
        targetingSystem.activate(300, 400, TargetingMode.ADJACENT, secondCallback);
        
        assertTrue(firstCallback.cancelCalled, "First callback should receive cancellation");
        assertFalse(secondCallback.cancelCalled, "Second callback should not receive cancellation");
    }
    
    // ===== Target Movement Tests =====
    
    @Test
    public void testMoveTargetUp() {
        targetingSystem.activate(128, 192, TargetingMode.ADJACENT, callback);
        targetingSystem.moveTarget(Direction.UP);
        
        float[] coords = targetingSystem.getTargetCoordinates();
        assertEquals(128, coords[0], "Target X should remain unchanged");
        assertEquals(256, coords[1], "Target Y should increase by 64 (192 + 64 = 256)");
    }
    
    @Test
    public void testMoveTargetDown() {
        targetingSystem.activate(128, 192, TargetingMode.ADJACENT, callback);
        targetingSystem.moveTarget(Direction.DOWN);
        
        float[] coords = targetingSystem.getTargetCoordinates();
        assertEquals(128, coords[0], "Target X should remain unchanged");
        assertEquals(128, coords[1], "Target Y should decrease by 64 (192 - 64 = 128)");
    }
    
    @Test
    public void testMoveTargetLeft() {
        targetingSystem.activate(128, 192, TargetingMode.ADJACENT, callback);
        targetingSystem.moveTarget(Direction.LEFT);
        
        float[] coords = targetingSystem.getTargetCoordinates();
        assertEquals(64, coords[0], "Target X should decrease by 64 (128 - 64 = 64)");
        assertEquals(192, coords[1], "Target Y should remain unchanged");
    }
    
    @Test
    public void testMoveTargetRight() {
        targetingSystem.activate(128, 192, TargetingMode.ADJACENT, callback);
        targetingSystem.moveTarget(Direction.RIGHT);
        
        float[] coords = targetingSystem.getTargetCoordinates();
        assertEquals(192, coords[0], "Target X should increase by 64 (128 + 64 = 192)");
        assertEquals(192, coords[1], "Target Y should remain unchanged");
    }
    
    @Test
    public void testMoveTargetMultipleDirections() {
        targetingSystem.activate(128, 192, TargetingMode.ADJACENT, callback);
        targetingSystem.moveTarget(Direction.RIGHT);
        targetingSystem.moveTarget(Direction.UP);
        targetingSystem.moveTarget(Direction.LEFT);
        
        float[] coords = targetingSystem.getTargetCoordinates();
        assertEquals(128, coords[0], "Target X should be back at original position");
        assertEquals(256, coords[1], "Target Y should be 64 higher than original");
    }
    
    @Test
    public void testMoveTargetWhenInactiveDoesNothing() {
        targetingSystem.moveTarget(Direction.UP);
        
        float[] coords = targetingSystem.getTargetCoordinates();
        assertEquals(0, coords[0], "Target X should remain at default");
        assertEquals(0, coords[1], "Target Y should remain at default");
    }
    
    // ===== Coordinate Snapping Tests =====
    
    @Test
    public void testCoordinateSnappingExactTileBoundary() {
        targetingSystem.activate(128, 192, TargetingMode.ADJACENT, callback);
        float[] coords = targetingSystem.getTargetCoordinates();
        assertEquals(128, coords[0], "Exact tile boundary should remain unchanged");
        assertEquals(192, coords[1], "Exact tile boundary should remain unchanged");
    }
    
    @Test
    public void testCoordinateSnappingPartialTile() {
        targetingSystem.activate(150, 210, TargetingMode.ADJACENT, callback);
        float[] coords = targetingSystem.getTargetCoordinates();
        assertEquals(128, coords[0], "150 should snap down to 128");
        assertEquals(192, coords[1], "210 should snap down to 192");
    }
    
    @Test
    public void testCoordinateSnappingNearUpperBoundary() {
        targetingSystem.activate(191, 255, TargetingMode.ADJACENT, callback);
        float[] coords = targetingSystem.getTargetCoordinates();
        assertEquals(128, coords[0], "191 should snap down to 128");
        assertEquals(192, coords[1], "255 should snap down to 192");
    }
    
    @Test
    public void testCoordinateSnappingZero() {
        targetingSystem.activate(0, 0, TargetingMode.ADJACENT, callback);
        float[] coords = targetingSystem.getTargetCoordinates();
        assertEquals(0, coords[0], "Zero should remain zero");
        assertEquals(0, coords[1], "Zero should remain zero");
    }
    
    @Test
    public void testCoordinateSnappingNegativeValues() {
        targetingSystem.activate(-50, -100, TargetingMode.ADJACENT, callback);
        float[] coords = targetingSystem.getTargetCoordinates();
        assertEquals(-64, coords[0], "-50 should snap down to -64");
        assertEquals(-128, coords[1], "-100 should snap down to -128");
    }
    
    // ===== Callback Invocation Tests =====
    
    @Test
    public void testConfirmTargetInvokesCallback() {
        targetingSystem.activate(128, 192, TargetingMode.ADJACENT, callback);
        targetingSystem.confirmTarget();
        
        assertTrue(callback.confirmCalled, "Callback should be invoked on confirmation");
        assertEquals(128, callback.confirmedX, "Callback should receive correct X coordinate");
        assertEquals(192, callback.confirmedY, "Callback should receive correct Y coordinate");
    }
    
    @Test
    public void testConfirmTargetWithMovedPosition() {
        targetingSystem.activate(128, 192, TargetingMode.ADJACENT, callback);
        targetingSystem.moveTarget(Direction.RIGHT);
        targetingSystem.moveTarget(Direction.UP);
        targetingSystem.confirmTarget();
        
        assertTrue(callback.confirmCalled, "Callback should be invoked on confirmation");
        assertEquals(192, callback.confirmedX, "Callback should receive moved X coordinate");
        assertEquals(256, callback.confirmedY, "Callback should receive moved Y coordinate");
    }
    
    @Test
    public void testConfirmTargetDeactivatesTargeting() {
        targetingSystem.activate(128, 192, TargetingMode.ADJACENT, callback);
        targetingSystem.confirmTarget();
        
        assertFalse(targetingSystem.isActive(), "Targeting should be deactivated after confirmation");
    }
    
    @Test
    public void testConfirmTargetWhenInactiveDoesNothing() {
        targetingSystem.confirmTarget();
        
        assertFalse(callback.confirmCalled, "Callback should not be invoked when inactive");
    }
    
    @Test
    public void testCancelInvokesCallback() {
        targetingSystem.activate(128, 192, TargetingMode.ADJACENT, callback);
        targetingSystem.cancel();
        
        assertTrue(callback.cancelCalled, "Callback should be invoked on cancellation");
    }
    
    @Test
    public void testCancelDeactivatesTargeting() {
        targetingSystem.activate(128, 192, TargetingMode.ADJACENT, callback);
        targetingSystem.cancel();
        
        assertFalse(targetingSystem.isActive(), "Targeting should be deactivated after cancellation");
    }
    
    @Test
    public void testCancelWhenInactiveDoesNothing() {
        targetingSystem.cancel();
        
        assertFalse(callback.cancelCalled, "Callback should not be invoked when inactive");
    }
    
    // ===== isActive() State Tracking Tests =====
    
    @Test
    public void testIsActiveReturnsFalseInitially() {
        assertFalse(targetingSystem.isActive(), "isActive should return false initially");
    }
    
    @Test
    public void testIsActiveReturnsTrueAfterActivation() {
        targetingSystem.activate(100, 200, TargetingMode.ADJACENT, callback);
        assertTrue(targetingSystem.isActive(), "isActive should return true after activation");
    }
    
    @Test
    public void testIsActiveReturnsFalseAfterDeactivation() {
        targetingSystem.activate(100, 200, TargetingMode.ADJACENT, callback);
        targetingSystem.deactivate();
        assertFalse(targetingSystem.isActive(), "isActive should return false after deactivation");
    }
    
    @Test
    public void testIsActiveReturnsFalseAfterConfirmation() {
        targetingSystem.activate(100, 200, TargetingMode.ADJACENT, callback);
        targetingSystem.confirmTarget();
        assertFalse(targetingSystem.isActive(), "isActive should return false after confirmation");
    }
    
    @Test
    public void testIsActiveReturnsFalseAfterCancellation() {
        targetingSystem.activate(100, 200, TargetingMode.ADJACENT, callback);
        targetingSystem.cancel();
        assertFalse(targetingSystem.isActive(), "isActive should return false after cancellation");
    }
    
    @Test
    public void testIsActiveRemainsTrueAfterMovement() {
        targetingSystem.activate(100, 200, TargetingMode.ADJACENT, callback);
        targetingSystem.moveTarget(Direction.UP);
        targetingSystem.moveTarget(Direction.RIGHT);
        assertTrue(targetingSystem.isActive(), "isActive should remain true after movement");
    }
    
    // ===== Boundary Conditions and Edge Cases =====
    
    @Test
    public void testMultipleActivationsWithoutDeactivation() {
        TestCallback firstCallback = new TestCallback();
        TestCallback secondCallback = new TestCallback();
        
        targetingSystem.activate(100, 200, TargetingMode.ADJACENT, firstCallback);
        targetingSystem.activate(300, 400, TargetingMode.ADJACENT, secondCallback);
        
        assertTrue(targetingSystem.isActive(), "System should remain active");
        float[] coords = targetingSystem.getTargetCoordinates();
        assertEquals(256, coords[0], "Target should be at second activation position");
        assertEquals(384, coords[1], "Target should be at second activation position");
    }
    
    @Test
    public void testConfirmWithNullCallback() {
        targetingSystem.activate(100, 200, TargetingMode.ADJACENT, null);
        // Should not throw exception
        assertDoesNotThrow(() -> targetingSystem.confirmTarget());
    }
    
    @Test
    public void testCancelWithNullCallback() {
        targetingSystem.activate(100, 200, TargetingMode.ADJACENT, null);
        // Should not throw exception
        assertDoesNotThrow(() -> targetingSystem.cancel());
    }
    
    @Test
    public void testGetTargetCoordinatesWhenInactive() {
        float[] coords = targetingSystem.getTargetCoordinates();
        assertNotNull(coords, "Should return coordinates even when inactive");
        assertEquals(2, coords.length, "Should return array of length 2");
    }
    
    @Test
    public void testMoveTargetToNegativeCoordinates() {
        targetingSystem.activate(0, 0, TargetingMode.ADJACENT, callback);
        targetingSystem.moveTarget(Direction.LEFT);
        targetingSystem.moveTarget(Direction.DOWN);
        
        float[] coords = targetingSystem.getTargetCoordinates();
        assertEquals(-64, coords[0], "Target X should be negative");
        assertEquals(-64, coords[1], "Target Y should be negative");
    }
    
    @Test
    public void testMoveTargetToLargeCoordinates() {
        targetingSystem.activate(10000, 10000, TargetingMode.ADJACENT, callback);
        targetingSystem.moveTarget(Direction.RIGHT);
        targetingSystem.moveTarget(Direction.UP);
        
        float[] coords = targetingSystem.getTargetCoordinates();
        assertEquals(10048, coords[0], "Should handle large X coordinates");
        assertEquals(10048, coords[1], "Should handle large Y coordinates");
    }
    
    @Test
    public void testReactivateAfterConfirmation() {
        targetingSystem.activate(128, 192, TargetingMode.ADJACENT, callback);
        targetingSystem.confirmTarget();
        
        callback.reset();
        targetingSystem.activate(256, 320, TargetingMode.ADJACENT, callback);
        
        assertTrue(targetingSystem.isActive(), "Should be able to reactivate after confirmation");
        float[] coords = targetingSystem.getTargetCoordinates();
        assertEquals(256, coords[0], "Should start at new position");
        assertEquals(320, coords[1], "Should start at new position");
    }
    
    @Test
    public void testReactivateAfterCancellation() {
        targetingSystem.activate(128, 192, TargetingMode.ADJACENT, callback);
        targetingSystem.cancel();
        
        callback.reset();
        targetingSystem.activate(256, 320, TargetingMode.ADJACENT, callback);
        
        assertTrue(targetingSystem.isActive(), "Should be able to reactivate after cancellation");
        float[] coords = targetingSystem.getTargetCoordinates();
        assertEquals(256, coords[0], "Should start at new position");
        assertEquals(320, coords[1], "Should start at new position");
    }
    
    @Test
    public void testUpdateMethodDoesNotThrowException() {
        targetingSystem.activate(100, 200, TargetingMode.ADJACENT, callback);
        // Update method is reserved for future use, should not throw
        assertDoesNotThrow(() -> targetingSystem.update(0.016f));
    }
    
    @Test
    public void testMaxRangeGetterSetter() {
        targetingSystem.setMaxRange(320);
        assertEquals(320, targetingSystem.getMaxRange(), "Should return set max range");
    }
    
    @Test
    public void testMaxRangeDefaultValue() {
        assertEquals(-1, targetingSystem.getMaxRange(), "Default max range should be -1 (unlimited)");
    }
    
    @Test
    public void testTargetValidityDefaultsToTrue() {
        targetingSystem.activate(100, 200, TargetingMode.ADJACENT, callback);
        assertTrue(targetingSystem.isTargetValid(), "Target should be valid by default");
    }
    
    @Test
    public void testDeactivationClearsState() {
        targetingSystem.activate(128, 192, TargetingMode.ADJACENT, callback);
        targetingSystem.moveTarget(Direction.RIGHT);
        targetingSystem.deactivate();
        
        // Reactivate and check that state was cleared
        targetingSystem.activate(256, 320, TargetingMode.ADJACENT, callback);
        float[] coords = targetingSystem.getTargetCoordinates();
        assertEquals(256, coords[0], "State should be cleared and reset to new position");
        assertEquals(320, coords[1], "State should be cleared and reset to new position");
    }
    
    @Test
    public void testAllDirectionsFromSameStartingPoint() {
        // Test UP
        targetingSystem.activate(128, 192, TargetingMode.ADJACENT, callback);
        targetingSystem.moveTarget(Direction.UP);
        float[] coords = targetingSystem.getTargetCoordinates();
        assertEquals(256, coords[1], "UP should move to Y=256");
        targetingSystem.deactivate();
        
        // Test DOWN
        targetingSystem.activate(128, 192, TargetingMode.ADJACENT, callback);
        targetingSystem.moveTarget(Direction.DOWN);
        coords = targetingSystem.getTargetCoordinates();
        assertEquals(128, coords[1], "DOWN should move to Y=128");
        targetingSystem.deactivate();
        
        // Test LEFT
        targetingSystem.activate(128, 192, TargetingMode.ADJACENT, callback);
        targetingSystem.moveTarget(Direction.LEFT);
        coords = targetingSystem.getTargetCoordinates();
        assertEquals(64, coords[0], "LEFT should move to X=64");
        targetingSystem.deactivate();
        
        // Test RIGHT
        targetingSystem.activate(128, 192, TargetingMode.ADJACENT, callback);
        targetingSystem.moveTarget(Direction.RIGHT);
        coords = targetingSystem.getTargetCoordinates();
        assertEquals(192, coords[0], "RIGHT should move to X=192");
    }
    
    // ===== Range Enforcement Tests =====
    
    @Test
    public void testCursorMovementWithinRange() {
        // Set max range to 256 pixels (4 tiles)
        targetingSystem.setMaxRange(256);
        targetingSystem.activate(512, 512, TargetingMode.ADJACENT, callback);
        
        // Move 2 tiles right (128 pixels) - within range
        targetingSystem.moveTarget(Direction.RIGHT);
        targetingSystem.moveTarget(Direction.RIGHT);
        
        float[] coords = targetingSystem.getTargetCoordinates();
        assertEquals(640, coords[0], "Target X should move freely within range");
        assertEquals(512, coords[1], "Target Y should remain unchanged");
        assertTrue(targetingSystem.isWithinMaxRange(), "Target should be within max range");
    }
    
    @Test
    public void testCursorMovementBeyondRangeShouldClamp() {
        // Set max range to 192 pixels (3 tiles)
        targetingSystem.setMaxRange(192);
        targetingSystem.activate(512, 512, TargetingMode.ADJACENT, callback);
        
        // Try to move 5 tiles right (320 pixels) - beyond range
        targetingSystem.moveTarget(Direction.RIGHT);
        targetingSystem.moveTarget(Direction.RIGHT);
        targetingSystem.moveTarget(Direction.RIGHT);
        targetingSystem.moveTarget(Direction.RIGHT);
        targetingSystem.moveTarget(Direction.RIGHT);
        
        float[] coords = targetingSystem.getTargetCoordinates();
        
        // Calculate expected clamped position
        float dx = coords[0] - 512;
        float dy = coords[1] - 512;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        assertTrue(distance <= 192, "Target should be clamped to max range (192 pixels)");
        assertTrue(coords[0] > 512, "Target should still be to the right of player");
    }
    
    @Test
    public void testClampingAccuracyOnBoundary() {
        // Set max range to 256 pixels
        targetingSystem.setMaxRange(256);
        targetingSystem.activate(512, 512, TargetingMode.ADJACENT, callback);
        
        // Try to move far beyond range
        for (int i = 0; i < 10; i++) {
            targetingSystem.moveTarget(Direction.RIGHT);
        }
        
        float[] coords = targetingSystem.getTargetCoordinates();
        float dx = coords[0] - 512;
        float dy = coords[1] - 512;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // Distance should be at or very close to max range (within tile size tolerance)
        assertTrue(distance <= 256, "Clamped distance should not exceed max range");
        assertTrue(distance >= 192, "Clamped distance should be near max range boundary");
    }
    
    @Test
    public void testTileGridSnappingAfterClamping() {
        // Set max range to 200 pixels (between 3 and 4 tiles)
        targetingSystem.setMaxRange(200);
        targetingSystem.activate(512, 512, TargetingMode.ADJACENT, callback);
        
        // Try to move beyond range
        for (int i = 0; i < 6; i++) {
            targetingSystem.moveTarget(Direction.RIGHT);
        }
        
        float[] coords = targetingSystem.getTargetCoordinates();
        
        // Verify coordinates are snapped to tile grid (multiples of 64)
        assertEquals(0, coords[0] % 64, "Clamped X coordinate should be snapped to tile grid");
        assertEquals(0, coords[1] % 64, "Clamped Y coordinate should be snapped to tile grid");
    }
    
    @Test
    public void testUnlimitedRangeMode() {
        // Set max range to -1 (unlimited)
        targetingSystem.setMaxRange(-1);
        targetingSystem.activate(512, 512, TargetingMode.ADJACENT, callback);
        
        // Move very far from player
        for (int i = 0; i < 20; i++) {
            targetingSystem.moveTarget(Direction.RIGHT);
        }
        
        float[] coords = targetingSystem.getTargetCoordinates();
        
        // Should be able to move 20 tiles (1280 pixels) without clamping
        assertEquals(1792, coords[0], "Target should move freely with unlimited range");
        assertEquals(512, coords[1], "Target Y should remain unchanged");
        assertTrue(targetingSystem.isWithinMaxRange(), "Should always be within range when unlimited");
    }
    
    @Test
    public void testDiagonalMovementClamping() {
        // Set max range to 256 pixels
        targetingSystem.setMaxRange(256);
        targetingSystem.activate(512, 512, TargetingMode.ADJACENT, callback);
        
        // Move diagonally (right and up) beyond range
        for (int i = 0; i < 6; i++) {
            targetingSystem.moveTarget(Direction.RIGHT);
            targetingSystem.moveTarget(Direction.UP);
        }
        
        float[] coords = targetingSystem.getTargetCoordinates();
        float dx = coords[0] - 512;
        float dy = coords[1] - 512;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // Diagonal distance should be clamped to max range
        assertTrue(distance <= 256, "Diagonal movement should be clamped to max range");
        assertTrue(coords[0] > 512, "Target should be to the right of player");
        assertTrue(coords[1] > 512, "Target should be above player");
    }
    
    @Test
    public void testIsWithinMaxRangeValidation() {
        targetingSystem.setMaxRange(256);
        targetingSystem.activate(512, 512, TargetingMode.ADJACENT, callback);
        
        // Initially at player position - within range
        assertTrue(targetingSystem.isWithinMaxRange(), "Target at player position should be within range");
        
        // Move within range
        targetingSystem.moveTarget(Direction.RIGHT);
        targetingSystem.moveTarget(Direction.RIGHT);
        assertTrue(targetingSystem.isWithinMaxRange(), "Target within range should return true");
        
        // Try to move beyond range (will be clamped)
        for (int i = 0; i < 10; i++) {
            targetingSystem.moveTarget(Direction.RIGHT);
        }
        
        // After clamping, should still be within range
        assertTrue(targetingSystem.isWithinMaxRange(), "Clamped target should be within range");
    }
    
    @Test
    public void testIsWithinMaxRangeWithUnlimitedRange() {
        targetingSystem.setMaxRange(-1);
        targetingSystem.activate(512, 512, TargetingMode.ADJACENT, callback);
        
        // Move very far
        for (int i = 0; i < 50; i++) {
            targetingSystem.moveTarget(Direction.RIGHT);
        }
        
        // Should always be within range when unlimited
        assertTrue(targetingSystem.isWithinMaxRange(), "Should always be within range when maxRange is -1");
    }
    
    @Test
    public void testIsWithinMaxRangeWithZeroRange() {
        targetingSystem.setMaxRange(0);
        targetingSystem.activate(512, 512, TargetingMode.ADJACENT, callback);
        
        // Zero or negative range should be treated as unlimited
        assertTrue(targetingSystem.isWithinMaxRange(), "Zero range should be treated as unlimited");
    }
    
    @Test
    public void testRangeEnforcementInAllDirections() {
        targetingSystem.setMaxRange(192);
        targetingSystem.activate(512, 512, TargetingMode.ADJACENT, callback);
        
        // Test clamping in each direction
        
        // UP
        for (int i = 0; i < 10; i++) {
            targetingSystem.moveTarget(Direction.UP);
        }
        float[] coords = targetingSystem.getTargetCoordinates();
        float dy = coords[1] - 512;
        assertTrue(Math.abs(dy) <= 192, "UP movement should be clamped to max range");
        
        // Reset
        targetingSystem.deactivate();
        targetingSystem.activate(512, 512, TargetingMode.ADJACENT, callback);
        
        // DOWN
        for (int i = 0; i < 10; i++) {
            targetingSystem.moveTarget(Direction.DOWN);
        }
        coords = targetingSystem.getTargetCoordinates();
        dy = coords[1] - 512;
        assertTrue(Math.abs(dy) <= 192, "DOWN movement should be clamped to max range");
        
        // Reset
        targetingSystem.deactivate();
        targetingSystem.activate(512, 512, TargetingMode.ADJACENT, callback);
        
        // LEFT
        for (int i = 0; i < 10; i++) {
            targetingSystem.moveTarget(Direction.LEFT);
        }
        coords = targetingSystem.getTargetCoordinates();
        float dx = coords[0] - 512;
        assertTrue(Math.abs(dx) <= 192, "LEFT movement should be clamped to max range");
        
        // Reset
        targetingSystem.deactivate();
        targetingSystem.activate(512, 512, TargetingMode.ADJACENT, callback);
        
        // RIGHT
        for (int i = 0; i < 10; i++) {
            targetingSystem.moveTarget(Direction.RIGHT);
        }
        coords = targetingSystem.getTargetCoordinates();
        dx = coords[0] - 512;
        assertTrue(Math.abs(dx) <= 192, "RIGHT movement should be clamped to max range");
    }
}
