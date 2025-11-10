package wagemaker.uk.weather;

/**
 * Represents a single rain particle (raindrop).
 * Designed for object pooling to minimize garbage collection.
 * Each particle can be reset and reused multiple times.
 */
public class RainParticle {
    private float x;              // Current X position
    private float y;              // Current Y position
    private float velocityY;      // Falling speed (pixels/second)
    private float length;         // Visual length of raindrop
    private float alpha;          // Transparency (0.0-1.0)
    private boolean active;       // Whether particle is currently in use
    
    /**
     * Creates a new rain particle in an inactive state.
     */
    public RainParticle() {
        this.active = false;
    }
    
    /**
     * Resets this particle with new properties for reuse.
     * This method is called when recycling particles from the pool.
     * 
     * @param startX Starting X position
     * @param startY Starting Y position
     * @param velocity Falling speed in pixels per second
     * @param length Visual length of the raindrop in pixels
     */
    public void reset(float startX, float startY, float velocity, float length) {
        this.x = startX;
        this.y = startY;
        this.velocityY = velocity;
        this.length = length;
        this.alpha = 0.5f; // Default semi-transparent
        this.active = true;
    }
    
    /**
     * Updates the particle's position based on its velocity.
     * Called each frame to animate the falling motion.
     * 
     * @param deltaTime Time elapsed since last update in seconds
     */
    public void update(float deltaTime) {
        if (!active) {
            return;
        }
        
        // Move particle downward based on velocity
        y -= velocityY * deltaTime;
    }
    
    /**
     * Checks if this particle has fallen off the bottom of the screen.
     * Used to determine when a particle should be recycled.
     * 
     * @param screenBottom The Y coordinate of the bottom of the screen
     * @return true if the particle is below the screen bottom, false otherwise
     */
    public boolean isOffScreen(float screenBottom) {
        return y <= screenBottom;
    }
    
    // Getters
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    
    public float getVelocityY() {
        return velocityY;
    }
    
    public float getLength() {
        return length;
    }
    
    public float getAlpha() {
        return alpha;
    }
    
    public boolean isActive() {
        return active;
    }
    
    // Setters
    
    public void setX(float x) {
        this.x = x;
    }
    
    public void setY(float y) {
        this.y = y;
    }
    
    public void setVelocityY(float velocityY) {
        this.velocityY = velocityY;
    }
    
    public void setLength(float length) {
        this.length = length;
    }
    
    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    @Override
    public String toString() {
        return String.format("RainParticle[pos=(%.1f,%.1f), velocity=%.1f, length=%.1f, alpha=%.2f, active=%s]",
            x, y, velocityY, length, alpha, active);
    }
}
