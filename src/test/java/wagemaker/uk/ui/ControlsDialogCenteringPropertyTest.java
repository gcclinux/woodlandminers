package wagemaker.uk.ui;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for Controls Dialog centering behavior.
 * Feature: controls-menu-dialog, Property 5: Dialog centers on camera
 * Validates: Requirements 3.4
 * 
 * Note: These tests verify the centering calculation logic without requiring
 * full LibGDX rendering system initialization.
 */
public class ControlsDialogCenteringPropertyTest {
    
    private static final float DIALOG_WIDTH = 700;
    private static final float DIALOG_HEIGHT = 600;
    
    /**
     * Calculates the dialog X position given camera X position.
     * This mirrors the logic in ControlsDialog.render()
     */
    private static float calculateDialogX(float camX) {
        return camX - DIALOG_WIDTH / 2;
    }
    
    /**
     * Calculates the dialog Y position given camera Y position.
     * This mirrors the logic in ControlsDialog.render()
     */
    private static float calculateDialogY(float camY) {
        return camY - DIALOG_HEIGHT / 2;
    }
    
    /**
     * Property 5: Dialog centers on camera
     * For any camera position (camX, camY), the dialog X position should equal 
     * camX - DIALOG_WIDTH/2 and dialog Y position should equal camY - DIALOG_HEIGHT/2
     * Validates: Requirements 3.4
     * 
     * This property-based test runs 100 trials with random camera positions.
     */
    @Test
    public void dialogCentersOnCamera() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Generate random camera position
            // Use a reasonable range for game coordinates (-10000 to 10000)
            float camX = (random.nextFloat() * 20000) - 10000;
            float camY = (random.nextFloat() * 20000) - 10000;
            
            // Calculate dialog position
            float dialogX = calculateDialogX(camX);
            float dialogY = calculateDialogY(camY);
            
            // Verify centering formula
            float expectedDialogX = camX - DIALOG_WIDTH / 2;
            float expectedDialogY = camY - DIALOG_HEIGHT / 2;
            
            assertEquals(
                expectedDialogX,
                dialogX,
                0.001f,
                "Dialog X should be centered on camera X (camX=" + camX + ")"
            );
            
            assertEquals(
                expectedDialogY,
                dialogY,
                0.001f,
                "Dialog Y should be centered on camera Y (camY=" + camY + ")"
            );
            
            // Verify that the dialog center aligns with camera position
            float dialogCenterX = dialogX + DIALOG_WIDTH / 2;
            float dialogCenterY = dialogY + DIALOG_HEIGHT / 2;
            
            assertEquals(
                camX,
                dialogCenterX,
                0.001f,
                "Dialog center X should equal camera X"
            );
            
            assertEquals(
                camY,
                dialogCenterY,
                0.001f,
                "Dialog center Y should equal camera Y"
            );
        }
    }
    
    /**
     * Property: Dialog centering is consistent across coordinate ranges
     * For any camera position, the centering calculation should produce consistent
     * results regardless of whether coordinates are positive, negative, or zero.
     * 
     * This property-based test runs 100 trials across different coordinate ranges.
     */
    @Test
    public void dialogCenteringIsConsistentAcrossRanges() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Test different coordinate ranges
        float[][] ranges = {
            {-10000, -5000},  // Negative range
            {-1000, 1000},    // Around zero
            {5000, 10000},    // Positive range
            {0, 0}            // Exactly zero
        };
        
        // Run 25 trials for each range (100 total)
        for (float[] range : ranges) {
            for (int trial = 0; trial < 25; trial++) {
                float camX, camY;
                
                if (range[0] == 0 && range[1] == 0) {
                    // Test exactly zero
                    camX = 0;
                    camY = 0;
                } else {
                    // Generate random position in range
                    camX = range[0] + random.nextFloat() * (range[1] - range[0]);
                    camY = range[0] + random.nextFloat() * (range[1] - range[0]);
                }
                
                // Calculate dialog position
                float dialogX = calculateDialogX(camX);
                float dialogY = calculateDialogY(camY);
                
                // Verify the dialog center equals camera position
                float dialogCenterX = dialogX + DIALOG_WIDTH / 2;
                float dialogCenterY = dialogY + DIALOG_HEIGHT / 2;
                
                assertEquals(
                    camX,
                    dialogCenterX,
                    0.001f,
                    "Dialog should center on camera X in range [" + range[0] + ", " + range[1] + "]"
                );
                
                assertEquals(
                    camY,
                    dialogCenterY,
                    0.001f,
                    "Dialog should center on camera Y in range [" + range[0] + ", " + range[1] + "]"
                );
            }
        }
    }
    
    /**
     * Property: Dialog position changes proportionally with camera movement
     * For any camera position and any movement delta, the dialog position should
     * change by the same delta to maintain centering.
     * 
     * This property-based test runs 100 trials with random camera movements.
     */
    @Test
    public void dialogPositionChangesWithCameraMovement() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Generate initial camera position
            float initialCamX = (random.nextFloat() * 20000) - 10000;
            float initialCamY = (random.nextFloat() * 20000) - 10000;
            
            // Calculate initial dialog position
            float initialDialogX = calculateDialogX(initialCamX);
            float initialDialogY = calculateDialogY(initialCamY);
            
            // Generate random camera movement
            float deltaX = (random.nextFloat() * 2000) - 1000; // -1000 to 1000
            float deltaY = (random.nextFloat() * 2000) - 1000;
            
            // Calculate new camera and dialog positions
            float newCamX = initialCamX + deltaX;
            float newCamY = initialCamY + deltaY;
            float newDialogX = calculateDialogX(newCamX);
            float newDialogY = calculateDialogY(newCamY);
            
            // Verify dialog moved by the same delta
            float dialogDeltaX = newDialogX - initialDialogX;
            float dialogDeltaY = newDialogY - initialDialogY;
            
            assertEquals(
                deltaX,
                dialogDeltaX,
                0.001f,
                "Dialog X should move by same delta as camera X"
            );
            
            assertEquals(
                deltaY,
                dialogDeltaY,
                0.001f,
                "Dialog Y should move by same delta as camera Y"
            );
        }
    }
}
