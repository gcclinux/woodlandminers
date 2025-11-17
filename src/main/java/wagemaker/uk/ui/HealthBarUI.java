package wagemaker.uk.ui;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * A unified health bar UI component that displays both health damage and hunger levels.
 * The health bar uses a three-layer rendering approach:
 * - Green base layer representing full health and no hunger
 * - Red overlay from the right side showing damage taken (decreasing health)
 * - Blue overlay from the right side showing hunger accumulated (decreasing satisfaction)
 * 
 * Both red and blue overlays decrease from the right to show depletion/loss.
 * The bar is rendered in the top-left corner of the screen with fixed dimensions.
 */
public class HealthBarUI {
    private ShapeRenderer shapeRenderer;
    
    // Bar dimensions and offset from screen edge
    private static final float BAR_WIDTH = 200.0f;
    private static final float BAR_HEIGHT = 20.0f;
    private static final float SCREEN_OFFSET_X = 20.0f;  // Offset from left edge
    private static final float SCREEN_OFFSET_Y = 40.0f;  // Offset from top edge
    
    /**
     * Creates a new health bar UI component.
     * 
     * @param shapeRenderer The ShapeRenderer instance used for rendering the health bar
     */
    public HealthBarUI(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
    }
    
    /**
     * Renders the unified health bar showing both health and hunger status.
     * The rendering uses a three-layer approach:
     * 1. Green base layer (full bar width) - represents full health and no hunger
     * 2. Red damage overlay (from right side) - shows health lost (decreasing from right)
     * 3. Blue hunger overlay (from right side) - shows hunger accumulated (decreasing satisfaction from right)
     * 
     * Both red and blue overlays decrease from the right side to show loss/depletion.
     * Only renders when health < 100 OR hunger > 0.
     * 
     * @param health Current health value (0-100)
     * @param hunger Current hunger value (0-100)
     * @param camera The camera for calculating screen position
     * @param viewport The viewport for calculating screen dimensions
     */
    public void render(float health, float hunger, Camera camera, Viewport viewport) {
        // Only show bar when health < 100 OR hunger > 0
        if (health >= 100 && hunger <= 0) {
            return;
        }
        
        // Calculate top-left screen position in world coordinates
        float barX = camera.position.x - viewport.getWorldWidth() / 2 + SCREEN_OFFSET_X;
        float barY = camera.position.y + viewport.getWorldHeight() / 2 - SCREEN_OFFSET_Y;
        
        // Calculate percentages
        float healthPercent = Math.max(0, Math.min(100, health)) / 100.0f;
        float hungerPercent = Math.max(0, Math.min(100, hunger)) / 100.0f;
        
        // Begin filled shape rendering
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Layer 1: Draw green base (full bar)
        shapeRenderer.setColor(0, 1, 0, 1);  // Green (R=0, G=1, B=0, A=1)
        shapeRenderer.rect(barX, barY, BAR_WIDTH, BAR_HEIGHT);
        
        // Layer 2: Draw red damage overlay (from right side - decreasing health)
        float damagePercent = 1.0f - healthPercent;
        if (damagePercent > 0) {
            shapeRenderer.setColor(1, 0, 0, 1);  // Red (R=1, G=0, B=0, A=1)
            float damageWidth = BAR_WIDTH * damagePercent;
            shapeRenderer.rect(barX + BAR_WIDTH - damageWidth, barY, 
                              damageWidth, BAR_HEIGHT);
        }
        
        // Layer 3: Draw blue hunger overlay (from right side - decreasing satisfaction)
        // Hunger represents "not full", so it also decreases from the right
        if (hungerPercent > 0) {
            shapeRenderer.setColor(0, 0, 1, 1);  // Blue (R=0, G=0, B=1, A=1)
            float hungerWidth = BAR_WIDTH * hungerPercent;
            shapeRenderer.rect(barX + BAR_WIDTH - hungerWidth, barY, 
                              hungerWidth, BAR_HEIGHT);
        }
        
        shapeRenderer.end();
        
        // Layer 4: Draw black border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0, 0, 0, 1);  // Black (R=0, G=0, B=0, A=1)
        shapeRenderer.rect(barX, barY, BAR_WIDTH, BAR_HEIGHT);
        shapeRenderer.end();
    }
    
    /**
     * Disposes of resources used by the health bar.
     * Currently, this class uses a shared ShapeRenderer, so no disposal is needed.
     * This method is provided for consistency with other UI components and future extensibility.
     */
    public void dispose() {
        // No resources to dispose - ShapeRenderer is managed externally
    }
}
