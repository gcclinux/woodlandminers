package wagemaker.uk.targeting;

/**
 * Interface for validating target positions.
 * Implementations can check for various conditions like tile occupancy,
 * biome type, inventory availability, etc.
 */
public interface TargetValidator {
    /**
     * Check if a target position is valid for the current action.
     * 
     * @param targetX The target x-coordinate (tile-aligned)
     * @param targetY The target y-coordinate (tile-aligned)
     * @return true if the target is valid, false otherwise
     */
    boolean isValidTarget(float targetX, float targetY);
}
