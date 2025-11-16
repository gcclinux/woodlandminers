package wagemaker.uk.targeting;

import wagemaker.uk.biome.BiomeManager;
import wagemaker.uk.biome.BiomeType;
import wagemaker.uk.inventory.InventoryManager;
import wagemaker.uk.planting.PlantedBamboo;
import wagemaker.uk.trees.BambooTree;
import java.util.Map;

/**
 * Validator for bamboo planting targets.
 * Checks:
 * - Inventory availability (baby bamboo in slot 2)
 * - Biome type (sand tiles only)
 * - Tile occupancy (no existing planted bamboo or bamboo trees)
 * 
 * Requirements: 3.4, 1.2, 1.3, 1.4
 */
public class PlantingTargetValidator implements TargetValidator {
    
    private final InventoryManager inventoryManager;
    private final BiomeManager biomeManager;
    private final Map<String, PlantedBamboo> plantedBamboos;
    private final Map<String, BambooTree> bambooTrees;
    
    /**
     * Creates a new PlantingTargetValidator.
     * 
     * @param inventoryManager The inventory manager for checking baby bamboo availability
     * @param biomeManager The biome manager for checking tile type
     * @param plantedBamboos Map of existing planted bamboos
     * @param bambooTrees Map of existing bamboo trees
     */
    public PlantingTargetValidator(InventoryManager inventoryManager,
                                   BiomeManager biomeManager,
                                   Map<String, PlantedBamboo> plantedBamboos,
                                   Map<String, BambooTree> bambooTrees) {
        this.inventoryManager = inventoryManager;
        this.biomeManager = biomeManager;
        this.plantedBamboos = plantedBamboos;
        this.bambooTrees = bambooTrees;
    }
    
    @Override
    public boolean isValidTarget(float targetX, float targetY) {
        // Validation 1: Check inventory availability
        if (!hasInventoryAvailable()) {
            return false;
        }
        
        // Validation 2: Check biome type (sand only)
        if (!isValidBiome(targetX, targetY)) {
            return false;
        }
        
        // Validation 3: Check tile occupancy
        if (isTileOccupied(targetX, targetY)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if player has baby bamboo in inventory.
     * 
     * @return true if baby bamboo is available, false otherwise
     */
    private boolean hasInventoryAvailable() {
        if (inventoryManager == null) {
            return false;
        }
        
        // Check if baby bamboo is selected (slot 2)
        if (inventoryManager.getSelectedSlot() != 2) {
            return false;
        }
        
        // Check if we have at least 1 baby bamboo
        int babyBambooCount = inventoryManager.getCurrentInventory().getBabyBambooCount();
        return babyBambooCount > 0;
    }
    
    /**
     * Check if the target tile is on sand biome.
     * 
     * @param targetX The target x-coordinate
     * @param targetY The target y-coordinate
     * @return true if tile is sand, false otherwise
     */
    private boolean isValidBiome(float targetX, float targetY) {
        if (biomeManager == null) {
            return false;
        }
        
        BiomeType biomeType = biomeManager.getBiomeAtPosition(targetX, targetY);
        return biomeType == BiomeType.SAND;
    }
    
    /**
     * Check if the target tile is occupied by planted bamboo or bamboo tree.
     * 
     * @param targetX The target x-coordinate
     * @param targetY The target y-coordinate
     * @return true if tile is occupied, false otherwise
     */
    private boolean isTileOccupied(float targetX, float targetY) {
        String tileKey = generateTileKey(targetX, targetY);
        
        // Check for planted bamboo
        if (plantedBamboos != null && plantedBamboos.containsKey(tileKey)) {
            return true;
        }
        
        // Check for bamboo tree
        if (bambooTrees != null && bambooTrees.containsKey(tileKey)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Generate tile key for occupancy checking.
     * 
     * @param x The x-coordinate (tile-aligned)
     * @param y The y-coordinate (tile-aligned)
     * @return Unique tile-based key string
     */
    private String generateTileKey(float x, float y) {
        int tileX = (int) x;
        int tileY = (int) y;
        return "planted-bamboo-" + tileX + "-" + tileY;
    }
}
