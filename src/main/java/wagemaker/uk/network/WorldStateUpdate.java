package wagemaker.uk.network;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents an incremental update to the world state.
 * Contains only the entities that have changed since a given timestamp.
 * This is used for efficient state synchronization.
 */
public class WorldStateUpdate implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Map<String, PlayerState> updatedPlayers;
    private Map<String, TreeState> updatedTrees;
    private Map<String, ItemState> updatedItems;
    
    public WorldStateUpdate() {
    }
    
    public WorldStateUpdate(Map<String, PlayerState> updatedPlayers,
                           Map<String, TreeState> updatedTrees,
                           Map<String, ItemState> updatedItems) {
        this.updatedPlayers = updatedPlayers;
        this.updatedTrees = updatedTrees;
        this.updatedItems = updatedItems;
    }
    
    public Map<String, PlayerState> getUpdatedPlayers() {
        return updatedPlayers;
    }
    
    public void setUpdatedPlayers(Map<String, PlayerState> updatedPlayers) {
        this.updatedPlayers = updatedPlayers;
    }
    
    public Map<String, TreeState> getUpdatedTrees() {
        return updatedTrees;
    }
    
    public void setUpdatedTrees(Map<String, TreeState> updatedTrees) {
        this.updatedTrees = updatedTrees;
    }
    
    public Map<String, ItemState> getUpdatedItems() {
        return updatedItems;
    }
    
    public void setUpdatedItems(Map<String, ItemState> updatedItems) {
        this.updatedItems = updatedItems;
    }
    
    /**
     * Checks if this update contains any changes.
     */
    public boolean isEmpty() {
        return (updatedPlayers == null || updatedPlayers.isEmpty()) &&
               (updatedTrees == null || updatedTrees.isEmpty()) &&
               (updatedItems == null || updatedItems.isEmpty());
    }
}
