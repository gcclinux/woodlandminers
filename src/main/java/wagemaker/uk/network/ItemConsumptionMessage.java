package wagemaker.uk.network;

/**
 * Message sent when a player consumes an item (apple or banana).
 */
public class ItemConsumptionMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private ItemType itemType;
    
    public ItemConsumptionMessage() {
        super();
    }
    
    public ItemConsumptionMessage(String senderId, String playerId, ItemType itemType) {
        super(senderId);
        this.playerId = playerId;
        this.itemType = itemType;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.ITEM_CONSUMPTION;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public ItemType getItemType() {
        return itemType;
    }
}
