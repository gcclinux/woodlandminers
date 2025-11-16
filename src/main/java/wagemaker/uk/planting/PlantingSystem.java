package wagemaker.uk.planting;

import wagemaker.uk.biome.BiomeManager;
import wagemaker.uk.biome.BiomeType;
import wagemaker.uk.inventory.InventoryManager;
import wagemaker.uk.player.Player;
import wagemaker.uk.trees.BambooTree;
import java.util.Map;

/**
 * Core planting logic and validation system.
 * Handles baby bamboo planting with comprehensive validation:
 * - Inventory checks (baby bamboo availability)
 * - Biome validation (sand tiles only)
 * - Tile occupancy checks (no duplicate planting)
 * 
 * Requirements: 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 2.3, 5.1, 5.2, 5.3
 */
public class PlantingSystem {
    
    /**
     * Attempt to plant baby bamboo at the specified target coordinates.
     * Performs all validation checks and creates PlantedBamboo on success.
     * 
     * Validation steps:
     * 1. Check if player has baby bamboo in inventory (slot 2)
     * 2. Check if target tile is sand
     * 3. Check if tile is not occupied by existing PlantedBamboo
     * 4. Check if tile is not occupied by existing BambooTree
     * 
     * @param targetX The target x-coordinate for planting
     * @param targetY The target y-coordinate for planting
     * @param inventoryManager The inventory manager for checking and deducting items
     * @param biomeManager The biome manager for tile type checking
     * @param plantedBamboos Map of existing planted bamboos
     * @param bambooTrees Map of existing bamboo trees
     * @return PlantedBamboo instance on success, null on failure
     */
    public PlantedBamboo attemptPlant(float targetX, float targetY, InventoryManager inventoryManager,
                                      BiomeManager biomeManager,
                                      Map<String, PlantedBamboo> plantedBamboos,
                                      Map<String, BambooTree> bambooTrees) {
        
        // Validation 1: Check if player has baby bamboo in inventory
        if (inventoryManager == null) {
            return null; // No inventory manager
        }
        
        int babyBambooCount = inventoryManager.getCurrentInventory().getBabyBambooCount();
        if (babyBambooCount <= 0) {
            return null; // No baby bamboo in inventory
        }
        
        // Snap target coordinates to tile grid
        float tileX = snapToTileGrid(targetX);
        float tileY = snapToTileGrid(targetY);
        
        // Validation 2: Check if player is standing on sand tile
        if (!isValidPlantingLocation(tileX, tileY, biomeManager, plantedBamboos, bambooTrees)) {
            return null; // Invalid location (not sand or occupied)
        }
        
        // All validations passed - deduct baby bamboo from inventory
        boolean removed = inventoryManager.getCurrentInventory().removeBabyBamboo(1);
        if (!removed) {
            return null; // Failed to remove item (shouldn't happen after count check)
        }
        
        // Create and return PlantedBamboo instance
        PlantedBamboo plantedBamboo = new PlantedBamboo(tileX, tileY);
        
        System.out.println("Baby bamboo planted at tile: (" + tileX + ", " + tileY + ")");
        
        return plantedBamboo;
    }
    
    /**
     * Check if a tile is valid for planting.
     * Validates biome type (sand) and tile occupancy.
     * 
     * @param x The x-coordinate (tile-aligned)
     * @param y The y-coordinate (tile-aligned)
     * @param biomeManager The biome manager for tile type checking
     * @param plantedBamboos Map of existing planted bamboos
     * @param bambooTrees Map of existing bamboo trees
     * @return true if tile is valid for planting, false otherwise
     */
    private boolean isValidPlantingLocation(float x, float y, BiomeManager biomeManager,
                                           Map<String, PlantedBamboo> plantedBamboos,
                                           Map<String, BambooTree> bambooTrees) {
        
        // Check if biome manager is available
        if (biomeManager == null) {
            return false;
        }
        
        // Check if tile is sand
        BiomeType biomeType = biomeManager.getBiomeAtPosition(x, y);
        if (biomeType != BiomeType.SAND) {
            return false; // Not on sand tile
        }
        
        // Generate tile key for occupancy checking
        String tileKey = generatePlantedBambooKey(x, y);
        
        // Check if tile is occupied by planted bamboo
        if (plantedBamboos != null && plantedBamboos.containsKey(tileKey)) {
            return false; // Tile already has planted bamboo
        }
        
        // Check if tile is occupied by bamboo tree
        if (bambooTrees != null && bambooTrees.containsKey(tileKey)) {
            return false; // Tile already has bamboo tree
        }
        
        return true; // Tile is valid for planting
    }
    
    /**
     * Generate unique key for planted bamboo based on tile coordinates.
     * Format: "planted-bamboo-{tileX}-{tileY}"
     * 
     * @param x The x-coordinate (tile-aligned)
     * @param y The y-coordinate (tile-aligned)
     * @return Unique tile-based key string
     */
    private String generatePlantedBambooKey(float x, float y) {
        int tileX = (int) x;
        int tileY = (int) y;
        return "planted-bamboo-" + tileX + "-" + tileY;
    }
    
    /**
     * Snap coordinates to 64x64 tile grid.
     * Ensures planted bamboos align with tile boundaries.
     * 
     * @param coordinate The world coordinate to snap
     * @return The snapped coordinate (tile-aligned)
     */
    private float snapToTileGrid(float coordinate) {
        return (float) (Math.floor(coordinate / 64.0) * 64.0);
    }
}
