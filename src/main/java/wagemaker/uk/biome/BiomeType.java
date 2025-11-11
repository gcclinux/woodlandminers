package wagemaker.uk.biome;

/**
 * Enum defining available biome types in the game world.
 * Each biome type represents a distinct ground texture and visual style.
 */
public enum BiomeType {
    GRASS,
    SAND;
    
    /**
     * Returns a human-readable display name for the biome type.
     * @return The biome type name in lowercase
     */
    public String getDisplayName() {
        return name().toLowerCase();
    }
}
