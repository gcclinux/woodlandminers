package wagemaker.uk.weather;

import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * Represents an individual water puddle instance.
 * Supports object pooling for efficient memory management.
 */
public class WaterPuddle {
    
    private float x;
    private float y;
    private float width;
    private float height;
    private float baseAlpha;
    private float rotation;
    private boolean active;
    
    /**
     * Creates a new inactive water puddle.
     */
    public WaterPuddle() {
        this.active = false;
    }
    
    /**
     * Resets this puddle with new properties for reuse from the pool.
     * 
     * @param x X position in world coordinates
     * @param y Y position in world coordinates
     * @param width Width of the puddle ellipse
     * @param height Height of the puddle ellipse
     * @param rotation Rotation angle in degrees for visual variety
     */
    public void reset(float x, float y, float width, float height, float rotation) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.baseAlpha = PuddleConfig.PUDDLE_BASE_ALPHA;
        this.rotation = rotation;
        this.active = true;
    }
    
    /**
     * Sets whether this puddle is active in the pool.
     * 
     * @param active True if active, false if available for reuse
     */
    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     * Checks if this puddle is currently active.
     * 
     * @return True if active, false if available for reuse
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * Checks if this puddle is within the camera's viewport.
     * 
     * @param camera The camera to check against
     * @return True if the puddle is visible in the viewport
     */
    public boolean isInViewport(OrthographicCamera camera) {
        if (!active) {
            return false;
        }
        
        float halfWidth = camera.viewportWidth * camera.zoom / 2;
        float halfHeight = camera.viewportHeight * camera.zoom / 2;
        
        float camLeft = camera.position.x - halfWidth;
        float camRight = camera.position.x + halfWidth;
        float camBottom = camera.position.y - halfHeight;
        float camTop = camera.position.y + halfHeight;
        
        // Check if puddle bounds overlap with camera bounds
        return x + width >= camLeft && x <= camRight &&
               y + height >= camBottom && y <= camTop;
    }
    
    // Getters
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    
    public float getWidth() {
        return width;
    }
    
    public float getHeight() {
        return height;
    }
    
    public float getBaseAlpha() {
        return baseAlpha;
    }
    
    public float getRotation() {
        return rotation;
    }
    
    // Setters for runtime modifications
    
    public void setX(float x) {
        this.x = x;
    }
    
    public void setY(float y) {
        this.y = y;
    }
    
    public void setBaseAlpha(float baseAlpha) {
        this.baseAlpha = baseAlpha;
    }
}
