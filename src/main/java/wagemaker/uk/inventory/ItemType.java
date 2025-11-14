package wagemaker.uk.inventory;

/**
 * Enum representing different types of items that can be collected and stored in inventory.
 * Each item type has properties indicating whether it's consumable and how much health it restores.
 */
public enum ItemType {
    APPLE(true, 20),
    BANANA(true, 20),
    BABY_BAMBOO(false, 0),
    BAMBOO_STACK(false, 0),
    WOOD_STACK(false, 0),
    PEBBLE(false, 0);
    
    private final boolean consumable;
    private final int healthRestore;
    
    ItemType(boolean consumable, int healthRestore) {
        this.consumable = consumable;
        this.healthRestore = healthRestore;
    }
    
    /**
     * Check if this item type can be consumed to restore health.
     * @return true if the item is consumable, false otherwise
     */
    public boolean isConsumable() {
        return consumable;
    }
    
    /**
     * Get the amount of health this item restores when consumed.
     * @return health points restored (0 for non-consumable items)
     */
    public int getHealthRestore() {
        return healthRestore;
    }
}
