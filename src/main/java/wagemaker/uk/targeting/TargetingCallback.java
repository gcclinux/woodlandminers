package wagemaker.uk.targeting;

/**
 * Callback interface for handling targeting confirmation and cancellation events.
 */
public interface TargetingCallback {
    /**
     * Called when the player confirms a target selection.
     * 
     * @param targetX The selected tile's x coordinate (tile-aligned)
     * @param targetY The selected tile's y coordinate (tile-aligned)
     */
    void onTargetConfirmed(float targetX, float targetY);
    
    /**
     * Called when targeting is cancelled by the player.
     */
    void onTargetCancelled();
}
