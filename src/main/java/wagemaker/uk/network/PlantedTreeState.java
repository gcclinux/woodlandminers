package wagemaker.uk.network;

import java.io.Serializable;

/**
 * Represents the state of a planted tree in the multiplayer world.
 * Used for synchronizing planted trees across clients and for world persistence.
 */
public class PlantedTreeState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String plantedTreeId;
    private float x;
    private float y;
    private float growthTimer; // Time elapsed since planting
    
    /**
     * Default constructor for serialization.
     */
    public PlantedTreeState() {
    }
    
    /**
     * Creates a new planted tree state.
     * @param plantedTreeId Unique ID for the planted tree
     * @param x The x-coordinate (tile-aligned)
     * @param y The y-coordinate (tile-aligned)
     * @param growthTimer Time elapsed since planting
     */
    public PlantedTreeState(String plantedTreeId, float x, float y, float growthTimer) {
        this.plantedTreeId = plantedTreeId;
        this.x = x;
        this.y = y;
        this.growthTimer = growthTimer;
    }
    
    public String getPlantedTreeId() {
        return plantedTreeId;
    }
    
    public void setPlantedTreeId(String plantedTreeId) {
        this.plantedTreeId = plantedTreeId;
    }
    
    public float getX() {
        return x;
    }
    
    public void setX(float x) {
        this.x = x;
    }
    
    public float getY() {
        return y;
    }
    
    public void setY(float y) {
        this.y = y;
    }
    
    public float getGrowthTimer() {
        return growthTimer;
    }
    
    public void setGrowthTimer(float growthTimer) {
        this.growthTimer = growthTimer;
    }
}