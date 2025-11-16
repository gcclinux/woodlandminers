package wagemaker.uk.targeting;

/**
 * Core targeting system that manages tile selection and visual feedback.
 * Handles state management, input processing, and coordinate validation.
 */
public class TargetingSystem {
    private static final int TILE_SIZE = 64;
    
    // State
    private boolean isActive;
    private float targetX;
    private float targetY;
    private float playerX;
    private float playerY;
    private TargetingMode mode;
    private TargetingCallback callback;
    private boolean isTargetValid;
    
    // Configuration
    private int maxRange; // For future ranged targeting
    
    // Validation dependencies (injected)
    private TargetValidator validator;
    
    /**
     * Creates a new TargetingSystem instance.
     */
    public TargetingSystem() {
        this.isActive = false;
        this.maxRange = -1; // Unlimited by default
        this.isTargetValid = true; // Default to valid
        this.validator = null; // No validator by default
    }
    
    /**
     * Set the validator for target validation.
     * 
     * @param validator The validator to use for checking target validity
     */
    public void setValidator(TargetValidator validator) {
        this.validator = validator;
    }
    
    /**
     * Activate targeting mode at the player's current position.
     * 
     * @param playerX Player's current x coordinate
     * @param playerY Player's current y coordinate
     * @param mode Targeting mode (ADJACENT, RANGED, etc.)
     * @param callback Callback to invoke when target is confirmed or cancelled
     */
    public void activate(float playerX, float playerY, TargetingMode mode, TargetingCallback callback) {
        // If already active, cancel previous session
        if (this.isActive && this.callback != null) {
            this.callback.onTargetCancelled();
        }
        
        this.isActive = true;
        this.playerX = snapToTileGrid(playerX);
        this.playerY = snapToTileGrid(playerY);
        this.targetX = this.playerX;
        this.targetY = this.playerY;
        this.mode = mode;
        this.callback = callback;
    }
    
    /**
     * Deactivate targeting mode and clear state.
     */
    public void deactivate() {
        this.isActive = false;
        this.targetX = 0;
        this.targetY = 0;
        this.playerX = 0;
        this.playerY = 0;
        this.mode = null;
        this.callback = null;
    }
    
    /**
     * Process directional input to move the target cursor.
     * Moves the target by one tile (64px) in the specified direction.
     * 
     * @param direction Direction to move (UP, DOWN, LEFT, RIGHT)
     */
    public void moveTarget(Direction direction) {
        if (!isActive) {
            return;
        }
        
        float newX = targetX;
        float newY = targetY;
        
        switch (direction) {
            case UP:
                newY += TILE_SIZE;
                break;
            case DOWN:
                newY -= TILE_SIZE;
                break;
            case LEFT:
                newX -= TILE_SIZE;
                break;
            case RIGHT:
                newX += TILE_SIZE;
                break;
        }
        
        // Snap to tile grid
        newX = snapToTileGrid(newX);
        newY = snapToTileGrid(newY);
        
        // Update target position
        targetX = newX;
        targetY = newY;
        
        // Validate new target position
        validateCurrentTarget();
    }
    
    /**
     * Confirm the current target selection.
     * Invokes the callback with the selected coordinates and deactivates targeting.
     * Only confirms if the target is valid.
     */
    public void confirmTarget() {
        if (!isActive || callback == null) {
            return;
        }
        
        // Validate target before confirming
        validateCurrentTarget();
        
        // Only confirm if target is valid
        if (!isTargetValid) {
            System.out.println("Cannot confirm: target position is invalid");
            return;
        }
        
        // Get tile-aligned coordinates
        float[] coords = getTargetCoordinates();
        
        // Invoke callback
        callback.onTargetConfirmed(coords[0], coords[1]);
        
        // Deactivate targeting
        deactivate();
    }
    
    /**
     * Cancel targeting without confirming.
     * Invokes the cancellation callback and deactivates targeting.
     */
    public void cancel() {
        if (!isActive) {
            return;
        }
        
        if (callback != null) {
            callback.onTargetCancelled();
        }
        
        deactivate();
    }
    
    /**
     * Get the current target tile coordinates.
     * 
     * @return float array [x, y] in world coordinates (tile-aligned)
     */
    public float[] getTargetCoordinates() {
        return new float[] {
            snapToTileGrid(targetX),
            snapToTileGrid(targetY)
        };
    }
    
    /**
     * Check if targeting mode is currently active.
     * 
     * @return true if targeting is active, false otherwise
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Update method for any time-based logic (future use).
     * 
     * @param deltaTime Time elapsed since last update in seconds
     */
    public void update(float deltaTime) {
        // Reserved for future time-based logic
        // (e.g., animation, range validation, etc.)
    }
    
    /**
     * Set the maximum range for targeting (for future ranged modes).
     * 
     * @param maxRange Maximum range in pixels, or -1 for unlimited
     */
    public void setMaxRange(int maxRange) {
        this.maxRange = maxRange;
    }
    
    /**
     * Get the maximum range for targeting.
     * 
     * @return Maximum range in pixels, or -1 for unlimited
     */
    public int getMaxRange() {
        return maxRange;
    }
    
    /**
     * Snap a coordinate to the 64x64 tile grid.
     * 
     * @param coordinate The coordinate to snap
     * @return The snapped coordinate
     */
    private float snapToTileGrid(float coordinate) {
        return (float) (Math.floor(coordinate / (double) TILE_SIZE) * TILE_SIZE);
    }
    
    /**
     * Validate the current target position using the configured validator.
     * Updates the isTargetValid flag based on validation result.
     */
    private void validateCurrentTarget() {
        if (validator == null) {
            // No validator configured - assume valid
            isTargetValid = true;
            return;
        }
        
        float[] coords = getTargetCoordinates();
        isTargetValid = validator.isValidTarget(coords[0], coords[1]);
    }
    
    /**
     * Check if the current target position is valid.
     * 
     * @return true if target is valid, false otherwise
     */
    public boolean isTargetValid() {
        return isTargetValid;
    }
}
