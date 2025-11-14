package wagemaker.uk.inventory;

import wagemaker.uk.player.Player;
import wagemaker.uk.network.GameClient;
import wagemaker.uk.network.InventoryUpdateMessage;

/**
 * Central manager for inventory operations.
 * Manages separate inventories for single-player and multiplayer modes,
 * handles item collection with health-based routing, and provides auto-consumption logic.
 */
public class InventoryManager {
    private Inventory singleplayerInventory;
    private Inventory multiplayerInventory;
    private Player player;
    private boolean isMultiplayerMode;
    private GameClient gameClient;
    private int selectedSlot; // 0-4 for slots, -1 for no selection
    
    /**
     * Create a new InventoryManager for the given player.
     * @param player The player whose inventory this manager controls
     */
    public InventoryManager(Player player) {
        this.player = player;
        this.singleplayerInventory = new Inventory();
        this.multiplayerInventory = new Inventory();
        this.isMultiplayerMode = false;
        this.selectedSlot = -1; // No selection by default
    }
    
    /**
     * Switch between single-player and multiplayer mode.
     * @param isMultiplayer true for multiplayer mode, false for single-player mode
     */
    public void setMultiplayerMode(boolean isMultiplayer) {
        this.isMultiplayerMode = isMultiplayer;
    }
    
    /**
     * Get the currently active inventory based on game mode.
     * @return The active inventory (single-player or multiplayer)
     */
    public Inventory getCurrentInventory() {
        return isMultiplayerMode ? multiplayerInventory : singleplayerInventory;
    }
    
    /**
     * Get the single-player inventory.
     * @return The single-player inventory instance
     */
    public Inventory getSingleplayerInventory() {
        return singleplayerInventory;
    }
    
    /**
     * Get the multiplayer inventory.
     * @return The multiplayer inventory instance
     */
    public Inventory getMultiplayerInventory() {
        return multiplayerInventory;
    }
    
    /**
     * Check if currently in multiplayer mode.
     * @return true if in multiplayer mode, false otherwise
     */
    public boolean isMultiplayerMode() {
        return isMultiplayerMode;
    }
    
    /**
     * Set the game client for network synchronization.
     * @param gameClient The game client instance
     */
    public void setGameClient(GameClient gameClient) {
        this.gameClient = gameClient;
    }
    
    /**
     * Collect an item with health-based routing logic.
     * If the item is consumable and player health is below 100%, consume it immediately.
     * Otherwise, add it to the current inventory.
     * @param type The type of item being collected
     */
    public void collectItem(ItemType type) {
        if (type == null) {
            return;
        }
        
        // Check if item is consumable and player needs health
        if (type.isConsumable() && player.getHealth() < 100) {
            // Consume immediately to restore health
            consumeItem(type);
        } else {
            // Add to inventory storage
            addItemToInventory(type, 1);
        }
    }
    
    /**
     * Add an item to the current inventory.
     * @param type The type of item to add
     * @param amount The quantity to add
     */
    private void addItemToInventory(ItemType type, int amount) {
        Inventory inventory = getCurrentInventory();
        
        switch (type) {
            case APPLE:
                inventory.addApple(amount);
                break;
            case BANANA:
                inventory.addBanana(amount);
                break;
            case BABY_BAMBOO:
                inventory.addBabyBamboo(amount);
                break;
            case BAMBOO_STACK:
                inventory.addBambooStack(amount);
                break;
            case WOOD_STACK:
                inventory.addWoodStack(amount);
                break;
        }
        
        // Send inventory update to server in multiplayer mode
        sendInventoryUpdate();
    }
    
    /**
     * Consume an item immediately to restore health.
     * @param type The type of item to consume
     */
    private void consumeItem(ItemType type) {
        if (!type.isConsumable()) {
            return;
        }
        
        // Restore health
        float currentHealth = player.getHealth();
        float newHealth = Math.min(100, currentHealth + type.getHealthRestore());
        player.setHealth(newHealth);
        
        System.out.println(type.name() + " consumed! Health restored: " + 
                          (newHealth - currentHealth) + " (Health: " + 
                          currentHealth + " → " + newHealth + ")");
    }
    
    /**
     * Try to automatically consume items from inventory when player health is below 100%.
     * Prioritizes apples first, then bananas.
     * Called when player takes damage.
     */
    public void tryAutoConsume() {
        // Only auto-consume if health is below maximum
        if (player.getHealth() >= 100) {
            return;
        }
        
        Inventory inventory = getCurrentInventory();
        
        // Try to consume an apple first
        if (inventory.getAppleCount() > 0) {
            consumeApple();
        } 
        // If no apples, try banana
        else if (inventory.getBananaCount() > 0) {
            consumeBanana();
        }
    }
    
    /**
     * Consume an apple from inventory to restore health.
     * Decrements apple count and restores 20 HP.
     */
    private void consumeApple() {
        Inventory inventory = getCurrentInventory();
        
        if (inventory.removeApple(1)) {
            float currentHealth = player.getHealth();
            float newHealth = Math.min(100, currentHealth + 20);
            player.setHealth(newHealth);
            
            System.out.println("Apple auto-consumed from inventory! Health restored: " + 
                              (newHealth - currentHealth) + " (Health: " + 
                              currentHealth + " → " + newHealth + ")");
            
            // Send inventory update to server in multiplayer mode
            sendInventoryUpdate();
        }
    }
    
    /**
     * Consume a banana from inventory to restore health.
     * Decrements banana count and restores 20 HP.
     */
    private void consumeBanana() {
        Inventory inventory = getCurrentInventory();
        
        if (inventory.removeBanana(1)) {
            float currentHealth = player.getHealth();
            float newHealth = Math.min(100, currentHealth + 20);
            player.setHealth(newHealth);
            
            System.out.println("Banana auto-consumed from inventory! Health restored: " + 
                              (newHealth - currentHealth) + " (Health: " + 
                              currentHealth + " → " + newHealth + ")");
            
            // Send inventory update to server in multiplayer mode
            sendInventoryUpdate();
        }
    }
    
    /**
     * Send inventory update to server in multiplayer mode.
     * Only sends if connected to a server and in multiplayer mode.
     */
    private void sendInventoryUpdate() {
        if (isMultiplayerMode && gameClient != null && gameClient.isConnected()) {
            Inventory inventory = getCurrentInventory();
            
            InventoryUpdateMessage message = new InventoryUpdateMessage(
                gameClient.getClientId(),
                gameClient.getClientId(),
                inventory.getAppleCount(),
                inventory.getBananaCount(),
                inventory.getBabyBambooCount(),
                inventory.getBambooStackCount(),
                inventory.getWoodStackCount()
            );
            
            gameClient.sendMessage(message);
        }
    }
    
    /**
     * Public method to send inventory update to server.
     * Used when inventory is modified outside of InventoryManager (e.g., planting system).
     */
    public void sendInventoryUpdateToServer() {
        sendInventoryUpdate();
    }
    
    /**
     * Update inventory from server sync message.
     * Used to synchronize inventory state with authoritative server.
     * @param appleCount The apple count from server
     * @param bananaCount The banana count from server
     * @param babyBambooCount The baby bamboo count from server
     * @param bambooStackCount The bamboo stack count from server
     * @param woodStackCount The wood stack count from server
     */
    public void syncFromServer(int appleCount, int bananaCount, int babyBambooCount, 
                                int bambooStackCount, int woodStackCount) {
        if (!isMultiplayerMode) {
            return; // Only sync in multiplayer mode
        }
        
        Inventory inventory = getCurrentInventory();
        inventory.setAppleCount(appleCount);
        inventory.setBananaCount(bananaCount);
        inventory.setBabyBambooCount(babyBambooCount);
        inventory.setBambooStackCount(bambooStackCount);
        inventory.setWoodStackCount(woodStackCount);
        
        System.out.println("Inventory synced from server: Apples=" + appleCount +
                         ", Bananas=" + bananaCount +
                         ", BabyBamboo=" + babyBambooCount +
                         ", BambooStack=" + bambooStackCount +
                         ", WoodStack=" + woodStackCount);
    }
    
    /**
     * Set the selected inventory slot.
     * @param slot The slot index (0-4 for valid slots, any other value clears selection)
     */
    public void setSelectedSlot(int slot) {
        if (slot >= 0 && slot <= 4) {
            this.selectedSlot = slot;
        } else {
            this.selectedSlot = -1; // Clear selection
        }
    }
    
    /**
     * Get the currently selected inventory slot.
     * @return The selected slot index (0-4), or -1 if no slot is selected
     */
    public int getSelectedSlot() {
        return selectedSlot;
    }
    
    /**
     * Clear the current inventory selection.
     */
    public void clearSelection() {
        this.selectedSlot = -1;
    }
    
    /**
     * Get the item type for the currently selected slot.
     * @return The ItemType for the selected slot, or null if no slot is selected
     */
    public ItemType getSelectedItemType() {
        switch (selectedSlot) {
            case 0: return ItemType.APPLE;
            case 1: return ItemType.BANANA;
            case 2: return ItemType.BABY_BAMBOO;
            case 3: return ItemType.BAMBOO_STACK;
            case 4: return ItemType.WOOD_STACK;
            default: return null;
        }
    }
}

