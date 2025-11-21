package wagemaker.uk.freeworld;

import wagemaker.uk.inventory.Inventory;

/**
 * Manages Free World mode state and operations.
 * Free World mode grants players 250 of each item type and disables save functionality.
 */
public class FreeWorldManager {
    private static boolean freeWorldActive = false;
    
    /**
     * Activate Free World mode.
     */
    public static void activateFreeWorld() {
        freeWorldActive = true;
        System.out.println("Free World mode activated - saves disabled");
    }
    
    /**
     * Deactivate Free World mode.
     */
    public static void deactivateFreeWorld() {
        freeWorldActive = false;
        System.out.println("Free World mode deactivated - saves enabled");
    }
    
    /**
     * Check if Free World mode is currently active.
     * @return true if Free World is active, false otherwise
     */
    public static boolean isFreeWorldActive() {
        return freeWorldActive;
    }
    
    /**
     * Grant 250 of each item type to the specified inventory.
     * @param inventory The inventory to grant items to
     */
    public static void grantFreeWorldItems(Inventory inventory) {
        if (inventory == null) {
            return;
        }
        
        inventory.setAppleCount(250);
        inventory.setBananaCount(250);
        inventory.setBabyBambooCount(250);
        inventory.setBambooStackCount(250);
        inventory.setBabyTreeCount(250);
        inventory.setWoodStackCount(250);
        inventory.setPebbleCount(250);
        inventory.setPalmFiberCount(250);
        
        System.out.println("Granted 250 of each item type");
    }
}
