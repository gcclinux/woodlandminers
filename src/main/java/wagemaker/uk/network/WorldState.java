package wagemaker.uk.network;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the complete authoritative game state.
 * This class manages all synchronized entities in the multiplayer world.
 */
public class WorldState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private long worldSeed;
    private Map<String, PlayerState> players;
    private Map<String, TreeState> trees;
    private Map<String, ItemState> items;
    private Set<String> clearedPositions;
    private long lastUpdateTimestamp;
    
    public WorldState() {
        this.players = new ConcurrentHashMap<>();
        this.trees = new ConcurrentHashMap<>();
        this.items = new ConcurrentHashMap<>();
        this.clearedPositions = ConcurrentHashMap.newKeySet();
        this.lastUpdateTimestamp = System.currentTimeMillis();
    }
    
    public WorldState(long worldSeed) {
        this();
        this.worldSeed = worldSeed;
    }
    
    /**
     * Creates a complete snapshot of the current world state.
     * This is used when a new client connects to get the full state.
     * 
     * @return A deep copy of the current world state
     */
    public WorldState createSnapshot() {
        WorldState snapshot = new WorldState(this.worldSeed);
        
        // Deep copy players
        for (Map.Entry<String, PlayerState> entry : this.players.entrySet()) {
            PlayerState original = entry.getValue();
            PlayerState copy = new PlayerState(
                original.getPlayerId(),
                original.getPlayerName(),
                original.getX(),
                original.getY(),
                original.getDirection(),
                original.getHealth(),
                original.isMoving()
            );
            copy.setLastUpdateTime(original.getLastUpdateTime());
            snapshot.players.put(entry.getKey(), copy);
        }
        
        // Deep copy trees
        for (Map.Entry<String, TreeState> entry : this.trees.entrySet()) {
            TreeState original = entry.getValue();
            TreeState copy = new TreeState(
                original.getTreeId(),
                original.getType(),
                original.getX(),
                original.getY(),
                original.getHealth(),
                original.isExists()
            );
            snapshot.trees.put(entry.getKey(), copy);
        }
        
        // Deep copy items
        for (Map.Entry<String, ItemState> entry : this.items.entrySet()) {
            ItemState original = entry.getValue();
            ItemState copy = new ItemState(
                original.getItemId(),
                original.getType(),
                original.getX(),
                original.getY(),
                original.isCollected()
            );
            snapshot.items.put(entry.getKey(), copy);
        }
        
        // Copy cleared positions
        snapshot.clearedPositions.addAll(this.clearedPositions);
        
        snapshot.lastUpdateTimestamp = this.lastUpdateTimestamp;
        
        return snapshot;
    }
    
    /**
     * Calculates the delta (changes) since the given timestamp.
     * This is used for efficient state synchronization - only sending what changed.
     * 
     * @param timestamp The timestamp to calculate changes from
     * @return A WorldStateUpdate containing only the entities that changed
     */
    public WorldStateUpdate getDeltaSince(long timestamp) {
        Map<String, PlayerState> updatedPlayers = new HashMap<>();
        Map<String, TreeState> updatedTrees = new HashMap<>();
        Map<String, ItemState> updatedItems = new HashMap<>();
        
        // Find players that changed since timestamp
        for (Map.Entry<String, PlayerState> entry : this.players.entrySet()) {
            PlayerState player = entry.getValue();
            if (player.getLastUpdateTime() > timestamp) {
                updatedPlayers.put(entry.getKey(), player);
            }
        }
        
        // For trees and items, we check if they were modified
        // Since TreeState and ItemState don't have timestamps, we include all recent changes
        // In a real implementation, you might want to add timestamps to these as well
        for (Map.Entry<String, TreeState> entry : this.trees.entrySet()) {
            TreeState tree = entry.getValue();
            // Include trees that don't exist (were destroyed) or have changed
            if (!tree.isExists() || this.lastUpdateTimestamp > timestamp) {
                updatedTrees.put(entry.getKey(), tree);
            }
        }
        
        for (Map.Entry<String, ItemState> entry : this.items.entrySet()) {
            ItemState item = entry.getValue();
            // Include items that were collected or recently added
            if (item.isCollected() || this.lastUpdateTimestamp > timestamp) {
                updatedItems.put(entry.getKey(), item);
            }
        }
        
        return new WorldStateUpdate(updatedPlayers, updatedTrees, updatedItems);
    }
    
    /**
     * Applies an incremental update to the current world state.
     * This merges the changes from the update into the current state.
     * 
     * @param update The update containing changed entities
     */
    public void applyUpdate(WorldStateUpdate update) {
        if (update == null) {
            return;
        }
        
        // Apply player updates
        if (update.getUpdatedPlayers() != null) {
            for (Map.Entry<String, PlayerState> entry : update.getUpdatedPlayers().entrySet()) {
                this.players.put(entry.getKey(), entry.getValue());
            }
        }
        
        // Apply tree updates
        if (update.getUpdatedTrees() != null) {
            for (Map.Entry<String, TreeState> entry : update.getUpdatedTrees().entrySet()) {
                TreeState tree = entry.getValue();
                if (!tree.isExists()) {
                    // Tree was destroyed, remove it
                    this.trees.remove(entry.getKey());
                    this.clearedPositions.add(entry.getKey());
                } else {
                    this.trees.put(entry.getKey(), tree);
                }
            }
        }
        
        // Apply item updates
        if (update.getUpdatedItems() != null) {
            for (Map.Entry<String, ItemState> entry : update.getUpdatedItems().entrySet()) {
                ItemState item = entry.getValue();
                if (item.isCollected()) {
                    // Item was collected, remove it
                    this.items.remove(entry.getKey());
                } else {
                    this.items.put(entry.getKey(), item);
                }
            }
        }
        
        this.lastUpdateTimestamp = System.currentTimeMillis();
    }
    
    // Getters and setters
    
    public long getWorldSeed() {
        return worldSeed;
    }
    
    public void setWorldSeed(long worldSeed) {
        this.worldSeed = worldSeed;
    }
    
    public Map<String, PlayerState> getPlayers() {
        return players;
    }
    
    public void setPlayers(Map<String, PlayerState> players) {
        this.players = players;
    }
    
    public Map<String, TreeState> getTrees() {
        return trees;
    }
    
    public void setTrees(Map<String, TreeState> trees) {
        this.trees = trees;
    }
    
    public Map<String, ItemState> getItems() {
        return items;
    }
    
    public void setItems(Map<String, ItemState> items) {
        this.items = items;
    }
    
    public Set<String> getClearedPositions() {
        return clearedPositions;
    }
    
    public void setClearedPositions(Set<String> clearedPositions) {
        this.clearedPositions = clearedPositions;
    }
    
    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }
    
    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }
    
    // Convenience methods for managing state
    
    /**
     * Adds or updates a player in the world state.
     */
    public void addOrUpdatePlayer(PlayerState player) {
        if (player != null) {
            player.setLastUpdateTime(System.currentTimeMillis());
            this.players.put(player.getPlayerId(), player);
            this.lastUpdateTimestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Removes a player from the world state.
     */
    public void removePlayer(String playerId) {
        this.players.remove(playerId);
        this.lastUpdateTimestamp = System.currentTimeMillis();
    }
    
    /**
     * Adds or updates a tree in the world state.
     */
    public void addOrUpdateTree(TreeState tree) {
        if (tree != null) {
            this.trees.put(tree.getTreeId(), tree);
            this.lastUpdateTimestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Removes a tree from the world state (marks it as destroyed).
     */
    public void removeTree(String treeId) {
        TreeState tree = this.trees.get(treeId);
        if (tree != null) {
            tree.setExists(false);
            this.clearedPositions.add(treeId);
            this.lastUpdateTimestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Adds or updates an item in the world state.
     */
    public void addOrUpdateItem(ItemState item) {
        if (item != null) {
            this.items.put(item.getItemId(), item);
            this.lastUpdateTimestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Removes an item from the world state (marks it as collected).
     */
    public void removeItem(String itemId) {
        this.items.remove(itemId);
        this.lastUpdateTimestamp = System.currentTimeMillis();
    }
}
