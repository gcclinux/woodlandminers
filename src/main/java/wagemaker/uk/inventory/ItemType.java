package wagemaker.uk.inventory;

/**
 * Enum representing different types of items that can be collected and stored in inventory.
 * Each item type has properties indicating whether it restores health or reduces hunger.
 */
public enum ItemType {
    APPLE(true, 10, false),      // Restores 10% health
    BANANA(false, 0, true),      // Reduces 5% hunger
    BABY_BAMBOO(false, 0, false),
    BAMBOO_STACK(false, 0, false),
    WOOD_STACK(false, 0, false),
    BABY_TREE(false, 0, false),
    PEBBLE(false, 0, false),
    PALM_FIBER(false, 0, false);
    
    private final boolean restoresHealth;
    private final int healthRestore;
    private final boolean reducesHunger;
    
    ItemType(boolean restoresHealth, int healthRestore, boolean reducesHunger) {
        this.restoresHealth = restoresHealth;
        this.healthRestore = healthRestore;
        this.reducesHunger = reducesHunger;
    }
    
    /**
     * Check if this item type restores health when consumed.
     * @return true if the item restores health, false otherwise
     */
    public boolean restoresHealth() {
        return restoresHealth;
    }
    
    /**
     * Get the amount of health this item restores when consumed.
     * @return health percentage restored (0 for items that don't restore health)
     */
    public int getHealthRestore() {
        return healthRestore;
    }
    
    /**
     * Check if this item type reduces hunger when consumed.
     * @return true if the item reduces hunger, false otherwise
     */
    public boolean reducesHunger() {
        return reducesHunger;
    }
}
