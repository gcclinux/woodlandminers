package wagemaker.uk.network;

import java.io.Serializable;

/**
 * Represents the state of a planted bamboo in the multiplayer world.
 * Used for synchronizing planted bamboos across clients.
 */
public class PlantedBambooState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String plantedBambooId;
    private float x;
    private float y;
    private float growthTimer; // Time elapsed since planting
    
    /**
     * Default constructor for serialization.
     */
    public PlantedBambooState() {
    }
    
    /**
     * Creates a new planted bamboo state.
     * @param plantedBambooId Unique ID for the planted bamboo
     * @param x The x-coordinate (tile-aligned)
     * @param y The y-coordinate (tile-aligned)
     * @param growthTimer Time elapsed since planting
     */
    public PlantedBambooState(String plantedBambooId, float x, float y, float growthTimer) {
        this.plantedBambooId = plantedBambooId;
        this.x = x;
        this.y = y;
        this.growthTimer = growthTimer;
    }
    
    public String getPlantedBambooId() {
        return plantedBambooId;
    }
    
    public void setPlantedBambooId(String plantedBambooId) {
        this.plantedBambooId = plantedBambooId;
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
