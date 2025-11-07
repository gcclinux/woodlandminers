package wagemaker.uk.network;

/**
 * Message sent when a player picks up an item.
 * Can be used as both a request (client to server) and confirmation (server to clients).
 */
public class ItemPickupMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String itemId;
    private String playerId;
    
    public ItemPickupMessage() {
        super();
    }
    
    public ItemPickupMessage(String senderId, String itemId, String playerId) {
        super(senderId);
        this.itemId = itemId;
        this.playerId = playerId;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.ITEM_PICKUP;
    }
    
    public String getItemId() {
        return itemId;
    }
    
    public String getPlayerId() {
        return playerId;
    }
}
