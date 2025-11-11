package wagemaker.uk.biome;

/**
 * Data class representing a biome zone configuration.
 * A biome zone defines a geographic area based on distance ranges from the spawn point,
 * and associates that area with a specific biome type.
 */
public class BiomeZone {
    private final float minDistance;
    private final float maxDistance;
    private final BiomeType biomeType;
    
    /**
     * Creates a new biome zone.
     * 
     * @param minDistance The minimum distance from spawn (inclusive) where this biome starts
     * @param maxDistance The maximum distance from spawn (exclusive) where this biome ends
     * @param biomeType The type of biome for this zone
     */
    public BiomeZone(float minDistance, float maxDistance, BiomeType biomeType) {
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.biomeType = biomeType;
    }
    
    /**
     * Checks if a given distance falls within this biome zone's range.
     * 
     * @param distance The distance from spawn to check
     * @return true if the distance is within this zone's range, false otherwise
     */
    public boolean containsDistance(float distance) {
        return distance >= minDistance && distance < maxDistance;
    }
    
    /**
     * Gets the biome type for this zone.
     * 
     * @return The biome type
     */
    public BiomeType getBiomeType() {
        return biomeType;
    }
    
    /**
     * Gets the minimum distance for this zone.
     * 
     * @return The minimum distance from spawn
     */
    public float getMinDistance() {
        return minDistance;
    }
    
    /**
     * Gets the maximum distance for this zone.
     * 
     * @return The maximum distance from spawn
     */
    public float getMaxDistance() {
        return maxDistance;
    }
}
