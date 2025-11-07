package wagemaker.uk.network;

/**
 * Message broadcast when an item is spawned in the world (e.g., from tree destruction).
 */
public class ItemSpawnMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String itemId;
    private ItemType itemType;
    private float x;
    private float y;
    
    public ItemSpawnMessage() {
        super();
    }
    
    public ItemSpawnMessage(String senderId, String itemId, ItemType itemType, float x, float y) {
        super(senderId);
        this.itemId = itemId;
        this.itemType = itemType;
        this.x = x;
        this.y = y;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.ITEM_SPAWN;
    }
    
    public String getItemId() {
        return itemId;
    }
    
    public ItemType getItemType() {
        return itemType;
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
}
