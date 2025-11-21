package wagemaker.uk.weather;

/**
 * Represents the current state of the puddle system lifecycle.
 * Puddles transition through these states based on rain activity.
 */
public enum PuddleState {
    /** No puddles present, no rain active */
    NONE,
    
    /** Rain is active but hasn't reached the accumulation threshold yet */
    ACCUMULATING,
    
    /** Puddles are visible and rain is active */
    ACTIVE,
    
    /** Rain has stopped and puddles are fading out */
    EVAPORATING
}
