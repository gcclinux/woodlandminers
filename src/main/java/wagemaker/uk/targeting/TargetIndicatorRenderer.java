package wagemaker.uk.targeting;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Renders a visual indicator at the target tile position.
 * The indicator is a semi-transparent circle that shows where an action will be performed.
 * Color changes based on validity: white for valid, red for invalid.
 */
public class TargetIndicatorRenderer {
    private Texture validIndicatorTexture;
    private Texture invalidIndicatorTexture;
    private static final int INDICATOR_SIZE = 16;
    private static final int TILE_SIZE = 64;
    private static final float INDICATOR_OPACITY = 0.7f;
    
    /**
     * Initialize the renderer by creating the indicator textures.
     * Creates two 16x16 circles with 70% opacity: white for valid, red for invalid.
     */
    public void initialize() {
        // Create valid indicator (white)
        Pixmap validPixmap = new Pixmap(INDICATOR_SIZE, INDICATOR_SIZE, Pixmap.Format.RGBA8888);
        validPixmap.setColor(1.0f, 1.0f, 1.0f, INDICATOR_OPACITY);
        int radius = INDICATOR_SIZE / 2;
        validPixmap.fillCircle(radius, radius, radius);
        validIndicatorTexture = new Texture(validPixmap);
        validPixmap.dispose();
        
        // Create invalid indicator (red)
        Pixmap invalidPixmap = new Pixmap(INDICATOR_SIZE, INDICATOR_SIZE, Pixmap.Format.RGBA8888);
        invalidPixmap.setColor(1.0f, 0.0f, 0.0f, INDICATOR_OPACITY);
        invalidPixmap.fillCircle(radius, radius, radius);
        invalidIndicatorTexture = new Texture(invalidPixmap);
        invalidPixmap.dispose();
    }
    
    /**
     * Render the target indicator at the specified world coordinates.
     * The indicator is automatically centered on the tile.
     * Color changes based on validity: white for valid, red for invalid.
     * 
     * @param batch SpriteBatch for rendering
     * @param x Target tile x coordinate (world space)
     * @param y Target tile y coordinate (world space)
     * @param isValid Whether the target position is valid
     */
    public void render(SpriteBatch batch, float x, float y, boolean isValid) {
        Texture texture = isValid ? validIndicatorTexture : invalidIndicatorTexture;
        
        if (texture == null) {
            return;
        }
        
        // Calculate centering offset: (64 - 16) / 2 = 24
        float centerOffset = (TILE_SIZE - INDICATOR_SIZE) / 2.0f;
        float centeredX = x + centerOffset;
        float centeredY = y + centerOffset;
        
        // Draw the indicator
        batch.draw(texture, centeredX, centeredY, INDICATOR_SIZE, INDICATOR_SIZE);
    }
    
    /**
     * Dispose of graphics resources.
     * Must be called when the renderer is no longer needed.
     */
    public void dispose() {
        if (validIndicatorTexture != null) {
            validIndicatorTexture.dispose();
            validIndicatorTexture = null;
        }
        if (invalidIndicatorTexture != null) {
            invalidIndicatorTexture.dispose();
            invalidIndicatorTexture = null;
        }
    }
}
