package wagemaker.uk.network;

import java.util.Map;

/**
 * Message containing incremental world state changes.
 * Used for efficient synchronization after initial world state is sent.
 */
public class WorldStateUpdateMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private Map<String, PlayerState> updatedPlayers;
    private Map<String, TreeState> updatedTrees;
    private Map<String, ItemState> updatedItems;
    
    public WorldStateUpdateMessage() {
        super();
    }
    
    public WorldStateUpdateMessage(String senderId,
                                   Map<String, PlayerState> updatedPlayers,
                                   Map<String, TreeState> updatedTrees,
                                   Map<String, ItemState> updatedItems) {
        super(senderId);
        this.updatedPlayers = updatedPlayers;
        this.updatedTrees = updatedTrees;
        this.updatedItems = updatedItems;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.WORLD_STATE_UPDATE;
    }
    
    public Map<String, PlayerState> getUpdatedPlayers() {
        return updatedPlayers;
    }
    
    public Map<String, TreeState> getUpdatedTrees() {
        return updatedTrees;
    }
    
    public Map<String, ItemState> getUpdatedItems() {
        return updatedItems;
    }
}
