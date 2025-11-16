package wagemaker.uk.targeting;

/**
 * Defines the different targeting modes available.
 * Currently supports ADJACENT mode, with extensibility for future modes.
 */
public enum TargetingMode {
    /**
     * Only adjacent tiles (up, down, left, right) can be targeted.
     */
    ADJACENT,
    
    /**
     * Tiles within a range can be targeted (future implementation).
     */
    RANGED,
    
    /**
     * Tiles in a line can be targeted (future implementation).
     */
    LINE,
    
    /**
     * Area of effect targeting (future implementation).
     */
    AREA
}
