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
    private int selectedSlot; // 0-5 for slots, -1 for no selection
    
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
     * Collect an item and add it to the current inventory.
     * @param type The type of item being collected
     */
    public void collectItem(ItemType type) {
        if (type == null) {
            return;
        }
        
        // Always add to inventory storage
        addItemToInventory(type, 1);
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
            case PEBBLE:
                inventory.addPebble(amount);
                break;
        }
        
        // Send inventory update to server in multiplayer mode
        sendInventoryUpdate();
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
                inventory.getWoodStackCount(),
                inventory.getPebbleCount()
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
     * @param pebbleCount The pebble count from server
     */
    public void syncFromServer(int appleCount, int bananaCount, int babyBambooCount, 
                                int bambooStackCount, int woodStackCount, int pebbleCount) {
        if (!isMultiplayerMode) {
            return; // Only sync in multiplayer mode
        }
        
        Inventory inventory = getCurrentInventory();
        inventory.setAppleCount(appleCount);
        inventory.setBananaCount(bananaCount);
        inventory.setBabyBambooCount(babyBambooCount);
        inventory.setBambooStackCount(bambooStackCount);
        inventory.setWoodStackCount(woodStackCount);
        inventory.setPebbleCount(pebbleCount);
        
        System.out.println("Inventory synced from server: Apples=" + appleCount +
                         ", Bananas=" + bananaCount +
                         ", BabyBamboo=" + babyBambooCount +
                         ", BambooStack=" + bambooStackCount +
                         ", WoodStack=" + woodStackCount +
                         ", Pebbles=" + pebbleCount);
    }
    
    /**
     * Set the selected inventory slot.
     * @param slot The slot index (0-5 for valid slots, any other value clears selection)
     */
    public void setSelectedSlot(int slot) {
        if (slot >= 0 && slot <= 5) {
            this.selectedSlot = slot;
        } else {
            this.selectedSlot = -1; // Clear selection
        }
    }
    
    /**
     * Get the currently selected inventory slot.
     * @return The selected slot index (0-5), or -1 if no slot is selected
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
            case 5: return ItemType.PEBBLE;
            default: return null;
        }
    }
    
    /**
     * Try to consume the currently selected item.
     * Handles apple consumption (restore 10% health) and banana consumption (reduce 5% hunger).
     * Removes the item from inventory if consumption is successful.
     * Requirements: 1.2, 2.1
     * 
     * @param player The player consuming the item
     * @return true if item was consumed successfully, false otherwise
     */
    public boolean tryConsumeSelectedItem(Player player) {
        if (player == null) {
            return false;
        }
        
        int selectedSlot = getSelectedSlot();
        ItemType itemType = getSelectedItemType();
        
        if (itemType == null) {
            return false;
        }
        
        Inventory inventory = getCurrentInventory();
        
        // Handle apple consumption
        if (itemType == ItemType.APPLE) {
            if (inventory.removeApple(1)) {
                // Restore 10% health (capped at 100%)
                float newHealth = Math.min(100, player.getHealth() + 10);
                player.setHealth(newHealth);
                
                // Send inventory update in multiplayer
                sendInventoryUpdate();
                return true;
            }
        }
        // Handle banana consumption
        else if (itemType == ItemType.BANANA) {
            if (inventory.removeBanana(1)) {
                // Reduce 5% hunger (minimum 0%)
                float newHunger = Math.max(0, player.getHunger() - 5);
                player.setHunger(newHunger);
                
                // Send inventory update in multiplayer
                sendInventoryUpdate();
                return true;
            }
        }
        
        return false;
    }
}

