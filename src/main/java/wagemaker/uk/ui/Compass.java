package wagemaker.uk.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * A compass UI component that displays a directional indicator pointing toward the spawn point.
 * The compass appears as a 64x64 pixel element in the bottom-left corner of the screen,
 * with a needle that dynamically rotates to always point toward the world spawn location (0.0, 0.0).
 */
public class Compass {
    private static final int COMPASS_SIZE = 64;
    private static final int COMPASS_OFFSET = 10; // pixels from screen edges
    private static final int COMPASS_CENTER = COMPASS_SIZE / 2; // 32 pixels
    
    private Texture compassBackground;
    private Texture compassNeedle;
    private float currentRotation; // angle in degrees
    private float targetX = 0.0f;
    private float targetY = 0.0f;
    private boolean useCustomTarget = false;
    
    /**
     * Creates a new compass UI element.
     * Loads the compass background and needle textures from the assets/ui directory.
     * If texture loading fails, the compass will gracefully degrade and not render.
     */
    public Compass() {
        this.currentRotation = 0.0f;
        
        try {
            compassBackground = new Texture("ui/compass_background.png");
            compassNeedle = new Texture("ui/compass_needle.png");
            System.out.println("Compass textures loaded successfully");
        } catch (Exception e) {
            System.err.println("Failed to load compass textures: " + e.getMessage());
            e.printStackTrace();
            compassBackground = null;
            compassNeedle = null;
        }
    }
    
    /**
     * Updates the compass rotation based on player and spawn positions.
     * Calculates the angle between the player's current position and the spawn point,
     * then updates the needle rotation to point toward the spawn.
     * If a custom target is set, the compass will point toward the custom target instead.
     * 
     * @param playerX Current player X coordinate in world space
     * @param playerY Current player Y coordinate in world space
     * @param spawnX Spawn point X coordinate (typically 0.0)
     * @param spawnY Spawn point Y coordinate (typically 0.0)
     */
    public void update(float playerX, float playerY, float spawnX, float spawnY) {
        // Use custom target if set, otherwise use spawn point
        float targetX = useCustomTarget ? this.targetX : spawnX;
        float targetY = useCustomTarget ? this.targetY : spawnY;
        
        // Calculate vector from player to target
        float deltaX = targetX - playerX;
        float deltaY = targetY - playerY;
        
        // Calculate angle in radians using atan2
        // atan2 returns angle in range [-PI, PI]
        float angleRadians = (float) Math.atan2(deltaY, deltaX);
        
        // Convert radians to degrees for libGDX rotation
        // libGDX uses counter-clockwise rotation where 0° points right (east)
        float angleDegrees = (float) Math.toDegrees(angleRadians);
        
        // Update current rotation
        // When player is at spawn (0, 0), atan2(0, 0) returns 0, giving neutral orientation
        currentRotation = angleDegrees;
    }
    
    /**
     * Renders the compass in the bottom-left corner of the screen.
     * The compass is rendered on the HUD layer with fixed screen positioning,
     * independent of camera movement. If textures failed to load, rendering is skipped.
     * 
     * @param batch SpriteBatch for rendering (must be begun before calling)
     * @param camera OrthographicCamera for screen positioning calculations
     * @param viewport Viewport for screen dimension calculations
     */
    public void render(SpriteBatch batch, OrthographicCamera camera, Viewport viewport) {
        // Skip rendering if textures failed to load
        if (compassBackground == null || compassNeedle == null) {
            return;
        }
        
        // Calculate bottom-left screen position (10 pixels from edges)
        // Convert from world coordinates to screen coordinates
        float compassX = camera.position.x - viewport.getWorldWidth() / 2 + COMPASS_OFFSET;
        float compassY = camera.position.y - viewport.getWorldHeight() / 2 + COMPASS_OFFSET;
        
        // Render compass background (no rotation)
        batch.draw(
            compassBackground,
            compassX,
            compassY,
            COMPASS_SIZE,
            COMPASS_SIZE
        );
        
        // Render compass needle with rotation around center point (32, 32)
        batch.draw(
            compassNeedle,
            compassX,                    // x position
            compassY,                    // y position
            COMPASS_CENTER,              // origin x (center of 64x64 texture)
            COMPASS_CENTER,              // origin y (center of 64x64 texture)
            COMPASS_SIZE,                // width
            COMPASS_SIZE,                // height
            1.0f,                        // scale x
            1.0f,                        // scale y
            currentRotation,             // rotation in degrees
            0,                           // source x
            0,                           // source y
            COMPASS_SIZE,                // source width
            COMPASS_SIZE,                // source height
            false,                       // flip x
            false                        // flip y
        );
    }
    
    /**
     * Sets a custom target for the compass to point toward.
     * When a custom target is set, the compass will point toward these coordinates
     * instead of the world spawn point.
     * 
     * @param x Target X coordinate in world space
     * @param y Target Y coordinate in world space
     */
    public void setCustomTarget(float x, float y) {
        this.targetX = x;
        this.targetY = y;
        this.useCustomTarget = true;
    }
    
    /**
     * Resets the compass to point toward the world origin (0, 0).
     * Clears any custom target that was previously set.
     */
    public void resetToOrigin() {
        this.useCustomTarget = false;
    }
    
    /**
     * Checks if the compass is currently using a custom target.
     * 
     * @return true if a custom target is set, false if pointing to origin
     */
    public boolean hasCustomTarget() {
        return useCustomTarget;
    }
    
    /**
     * Gets the current target X coordinate.
     * 
     * @return Target X coordinate if custom target is set, otherwise 0.0
     */
    public float getTargetX() {
        return useCustomTarget ? targetX : 0.0f;
    }
    
    /**
     * Gets the current target Y coordinate.
     * 
     * @return Target Y coordinate if custom target is set, otherwise 0.0
     */
    public float getTargetY() {
        return useCustomTarget ? targetY : 0.0f;
    }
    
    /**
     * Disposes of compass textures to free up resources.
     * Should be called when the compass is no longer needed,
     * typically in the game's dispose() method.
     */
    public void dispose() {
        if (compassBackground != null) {
            compassBackground.dispose();
            compassBackground = null;
        }
        
        if (compassNeedle != null) {
            compassNeedle.dispose();
            compassNeedle = null;
        }
    }
}
