package wagemaker.uk.respawn;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Visual indicator for resources that are about to respawn.
 * Displays a pulsing green/yellow effect at the resource spawn location.
 * 
 * <h3>Rendering Behavior:</h3>
 * <ul>
 *   <li>Fades in over 1 second when threshold is reached</li>
 *   <li>Pulses with a breathing animation while active</li>
 *   <li>Fades out over 0.5 seconds when resource respawns</li>
 * </ul>
 */
public class RespawnIndicator {
    private static final float FADE_IN_DURATION = 1.0f;  // 1 second fade in
    private static final float FADE_OUT_DURATION = 0.5f; // 0.5 second fade out
    private static final float PULSE_SPEED = 2.0f;       // Pulse cycles per second
    private static final float MIN_ALPHA = 0.3f;         // Minimum alpha during pulse
    private static final float MAX_ALPHA = 0.8f;         // Maximum alpha during pulse
    private static final int INDICATOR_SIZE = 32;        // Size in pixels
    
    private float x;
    private float y;
    private Texture texture;
    private float animationTime;
    private float fadeTime;
    private boolean fadingOut;
    private boolean disposed;
    
    /**
     * Creates a new respawn indicator at the specified location.
     * 
     * @param x World X coordinate
     * @param y World Y coordinate
     */
    public RespawnIndicator(float x, float y) {
        this.x = x;
        this.y = y;
        this.animationTime = 0;
        this.fadeTime = 0;
        this.fadingOut = false;
        this.disposed = false;
        
        // Create a simple circular texture for the indicator
        createTexture();
    }
    
    /**
     * Creates the indicator texture.
     * Generates a circular gradient from green/yellow center to transparent edges.
     */
    private void createTexture() {
        Pixmap pixmap = new Pixmap(INDICATOR_SIZE, INDICATOR_SIZE, Pixmap.Format.RGBA8888);
        
        float centerX = INDICATOR_SIZE / 2f;
        float centerY = INDICATOR_SIZE / 2f;
        float radius = INDICATOR_SIZE / 2f;
        
        // Draw a circular gradient
        for (int px = 0; px < INDICATOR_SIZE; px++) {
            for (int py = 0; py < INDICATOR_SIZE; py++) {
                float dx = px - centerX;
                float dy = py - centerY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                
                if (distance <= radius) {
                    // Calculate alpha based on distance from center
                    float alpha = 1.0f - (distance / radius);
                    alpha = alpha * alpha; // Square for smoother falloff
                    
                    // Green/yellow color (RGB: 0.6, 1.0, 0.2)
                    pixmap.setColor(0.6f, 1.0f, 0.2f, alpha);
                    pixmap.drawPixel(px, py);
                }
            }
        }
        
        texture = new Texture(pixmap);
        pixmap.dispose();
    }
    
    /**
     * Updates the indicator animation.
     * 
     * @param deltaTime Time elapsed since last frame
     */
    public void update(float deltaTime) {
        if (disposed) {
            return;
        }
        
        animationTime += deltaTime;
        
        if (fadingOut) {
            fadeTime += deltaTime;
        } else {
            fadeTime = Math.min(fadeTime + deltaTime, FADE_IN_DURATION);
        }
    }
    
    /**
     * Renders the indicator with pulsing animation.
     * 
     * @param batch SpriteBatch for rendering
     * @param deltaTime Time elapsed since last frame (for animation)
     */
    public void render(SpriteBatch batch, float deltaTime) {
        if (disposed || texture == null) {
            return;
        }
        
        // Calculate fade alpha
        float fadeAlpha;
        if (fadingOut) {
            fadeAlpha = 1.0f - (fadeTime / FADE_OUT_DURATION);
            fadeAlpha = Math.max(0, fadeAlpha);
        } else {
            fadeAlpha = Math.min(1.0f, fadeTime / FADE_IN_DURATION);
        }
        
        // Calculate pulse alpha
        float pulseAlpha = MIN_ALPHA + (MAX_ALPHA - MIN_ALPHA) * 
                          (0.5f + 0.5f * (float) Math.sin(animationTime * PULSE_SPEED * Math.PI * 2));
        
        // Combine fade and pulse
        float finalAlpha = fadeAlpha * pulseAlpha;
        
        // Set batch color with alpha
        Color oldColor = batch.getColor();
        batch.setColor(1.0f, 1.0f, 1.0f, finalAlpha);
        
        // Draw centered on the spawn point
        float drawX = x - INDICATOR_SIZE / 2f;
        float drawY = y - INDICATOR_SIZE / 2f;
        batch.draw(texture, drawX, drawY, INDICATOR_SIZE, INDICATOR_SIZE);
        
        // Restore batch color
        batch.setColor(oldColor);
    }
    
    /**
     * Starts the fade-out animation.
     * Should be called when the resource respawns.
     */
    public void startFadeOut() {
        fadingOut = true;
        fadeTime = 0;
    }
    
    /**
     * Checks if the fade-out animation is complete.
     * 
     * @return true if fade-out is complete, false otherwise
     */
    public boolean isFadeOutComplete() {
        return fadingOut && fadeTime >= FADE_OUT_DURATION;
    }
    
    /**
     * Checks if the indicator is fully faded in.
     * 
     * @return true if fade-in is complete, false otherwise
     */
    public boolean isFadedIn() {
        return !fadingOut && fadeTime >= FADE_IN_DURATION;
    }
    
    /**
     * Gets the X coordinate of the indicator.
     * 
     * @return X coordinate
     */
    public float getX() {
        return x;
    }
    
    /**
     * Gets the Y coordinate of the indicator.
     * 
     * @return Y coordinate
     */
    public float getY() {
        return y;
    }
    
    /**
     * Checks if the indicator has been disposed.
     * 
     * @return true if disposed, false otherwise
     */
    public boolean isDisposed() {
        return disposed;
    }
    
    /**
     * Disposes of the indicator's resources.
     * Must be called on the render thread.
     */
    public void dispose() {
        if (!disposed && texture != null) {
            texture.dispose();
            texture = null;
            disposed = true;
        }
    }
}
