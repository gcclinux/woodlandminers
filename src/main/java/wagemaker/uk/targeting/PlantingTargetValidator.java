package wagemaker.uk.targeting;

import wagemaker.uk.biome.BiomeManager;
import wagemaker.uk.biome.BiomeType;
import wagemaker.uk.inventory.InventoryManager;
import wagemaker.uk.planting.PlantedBamboo;
import wagemaker.uk.planting.PlantedTree;
import wagemaker.uk.trees.BambooTree;
import wagemaker.uk.trees.SmallTree;
import wagemaker.uk.trees.AppleTree;
import wagemaker.uk.trees.CoconutTree;
import wagemaker.uk.trees.BananaTree;
import java.util.Map;

/**
 * Validator for planting targets (bamboo and trees).
 * Checks:
 * - Inventory availability (baby bamboo in slot 2, baby tree in slot 4)
 * - Biome type (sand for bamboo, grass for trees)
 * - Tile occupancy (no existing planted items or trees)
 * 
 * Requirements: 3.4, 1.2, 1.3, 1.4
 */
public class PlantingTargetValidator implements TargetValidator {
    
    private final InventoryManager inventoryManager;
    private final BiomeManager biomeManager;
    private final Map<String, PlantedBamboo> plantedBamboos;
    private final Map<String, BambooTree> bambooTrees;
    private Map<String, PlantedTree> plantedTrees;
    private Map<String, SmallTree> smallTrees;
    private Map<String, AppleTree> appleTrees;
    private Map<String, CoconutTree> coconutTrees;
    private Map<String, BananaTree> bananaTrees;
    
    /**
     * Creates a new PlantingTargetValidator.
     * 
     * @param inventoryManager The inventory manager for checking item availability
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
    
    /**
     * Sets the planted trees and all tree type maps for comprehensive planting validation.
     * 
     * @param plantedTrees Map of existing planted trees
     * @param smallTrees Map of existing small trees
     * @param appleTrees Map of existing apple trees
     * @param coconutTrees Map of existing coconut trees
     * @param bananaTrees Map of existing banana trees
     */
    public void setTreeMaps(Map<String, PlantedTree> plantedTrees, Map<String, SmallTree> smallTrees,
                           Map<String, AppleTree> appleTrees, Map<String, CoconutTree> coconutTrees,
                           Map<String, BananaTree> bananaTrees) {
        this.plantedTrees = plantedTrees;
        this.smallTrees = smallTrees;
        this.appleTrees = appleTrees;
        this.coconutTrees = coconutTrees;
        this.bananaTrees = bananaTrees;
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
     * Check if player has the required item in inventory.
     * 
     * @return true if required item is available, false otherwise
     */
    private boolean hasInventoryAvailable() {
        if (inventoryManager == null) {
            return false;
        }
        
        int selectedSlot = inventoryManager.getSelectedSlot();
        
        // Check baby bamboo (slot 2)
        if (selectedSlot == 2) {
            int babyBambooCount = inventoryManager.getCurrentInventory().getBabyBambooCount();
            return babyBambooCount > 0;
        }
        
        // Check baby tree (slot 4)
        if (selectedSlot == 4) {
            int babyTreeCount = inventoryManager.getCurrentInventory().getBabyTreeCount();
            return babyTreeCount > 0;
        }
        
        return false;
    }
    
    /**
     * Check if the target tile is on the correct biome for the selected item.
     * 
     * @param targetX The target x-coordinate
     * @param targetY The target y-coordinate
     * @return true if tile biome matches item requirements, false otherwise
     */
    private boolean isValidBiome(float targetX, float targetY) {
        if (biomeManager == null || inventoryManager == null) {
            return false;
        }
        
        BiomeType biomeType = biomeManager.getBiomeAtPosition(targetX, targetY);
        int selectedSlot = inventoryManager.getSelectedSlot();
        
        // Baby bamboo requires sand biome (slot 2)
        if (selectedSlot == 2) {
            return biomeType == BiomeType.SAND;
        }
        
        // Baby tree requires grass biome (slot 4)
        if (selectedSlot == 4) {
            return biomeType == BiomeType.GRASS;
        }
        
        return false;
    }
    
    /**
     * Check if the target tile is occupied by any planted item or tree.
     * 
     * @param targetX The target x-coordinate
     * @param targetY The target y-coordinate
     * @return true if tile is occupied, false otherwise
     */
    private boolean isTileOccupied(float targetX, float targetY) {
        // Snap to tile grid for consistent key generation
        float tileX = (float) (Math.floor(targetX / 64.0) * 64.0);
        float tileY = (float) (Math.floor(targetY / 64.0) * 64.0);
        
        String bambooKey = generateBambooTileKey(tileX, tileY);
        String treeKey = generateTreeTileKey(tileX, tileY);
        String coordinateKey = (int)tileX + "," + (int)tileY;
        
        // Check for planted bamboo
        if (plantedBamboos != null && plantedBamboos.containsKey(bambooKey)) {
            return true;
        }
        
        // Check for planted tree
        if (plantedTrees != null && plantedTrees.containsKey(treeKey)) {
            return true;
        }
        
        // Check for existing trees at this location using proximity check
        // This handles trees that might not have exact coordinate keys
        if (isTreeNearLocation(tileX, tileY)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if any tree is near the specified location (within 32 pixels).
     * This handles cases where trees might not align exactly with tile coordinates.
     * 
     * @param tileX The tile x-coordinate
     * @param tileY The tile y-coordinate
     * @return true if any tree is within 32 pixels of the location
     */
    private boolean isTreeNearLocation(float tileX, float tileY) {
        final float PROXIMITY_THRESHOLD = 32.0f; // Half a tile size
        
        // Check small trees
        if (smallTrees != null) {
            for (SmallTree tree : smallTrees.values()) {
                float dx = Math.abs(tree.getX() - tileX);
                float dy = Math.abs(tree.getY() - tileY);
                if (dx < PROXIMITY_THRESHOLD && dy < PROXIMITY_THRESHOLD) {
                    return true;
                }
            }
        }
        
        // Check apple trees
        if (appleTrees != null) {
            for (AppleTree tree : appleTrees.values()) {
                float dx = Math.abs(tree.getX() - tileX);
                float dy = Math.abs(tree.getY() - tileY);
                if (dx < PROXIMITY_THRESHOLD && dy < PROXIMITY_THRESHOLD) {
                    return true;
                }
            }
        }
        
        // Check coconut trees
        if (coconutTrees != null) {
            for (CoconutTree tree : coconutTrees.values()) {
                float dx = Math.abs(tree.getX() - tileX);
                float dy = Math.abs(tree.getY() - tileY);
                if (dx < PROXIMITY_THRESHOLD && dy < PROXIMITY_THRESHOLD) {
                    return true;
                }
            }
        }
        
        // Check bamboo trees
        if (bambooTrees != null) {
            for (BambooTree tree : bambooTrees.values()) {
                float dx = Math.abs(tree.getX() - tileX);
                float dy = Math.abs(tree.getY() - tileY);
                if (dx < PROXIMITY_THRESHOLD && dy < PROXIMITY_THRESHOLD) {
                    return true;
                }
            }
        }
        
        // Check banana trees
        if (bananaTrees != null) {
            for (BananaTree tree : bananaTrees.values()) {
                float dx = Math.abs(tree.getX() - tileX);
                float dy = Math.abs(tree.getY() - tileY);
                if (dx < PROXIMITY_THRESHOLD && dy < PROXIMITY_THRESHOLD) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Generate bamboo tile key for occupancy checking.
     * 
     * @param x The x-coordinate (tile-aligned)
     * @param y The y-coordinate (tile-aligned)
     * @return Unique bamboo tile-based key string
     */
    private String generateBambooTileKey(float x, float y) {
        int tileX = (int) x;
        int tileY = (int) y;
        return "planted-bamboo-" + tileX + "-" + tileY;
    }
    
    /**
     * Generate tree tile key for occupancy checking.
     * 
     * @param x The x-coordinate (tile-aligned)
     * @param y The y-coordinate (tile-aligned)
     * @return Unique tree tile-based key string
     */
    private String generateTreeTileKey(float x, float y) {
        int tileX = (int) x;
        int tileY = (int) y;
        return "planted-tree-" + tileX + "-" + tileY;
    }
}
