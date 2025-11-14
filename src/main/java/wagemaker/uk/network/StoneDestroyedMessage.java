package wagemaker.uk.network;

/**
 * Message broadcast when a stone is destroyed (health reaches zero).
 */
public class StoneDestroyedMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String stoneId;
    private float x;
    private float y;
    
    public StoneDestroyedMessage() {
        super();
    }
    
    public StoneDestroyedMessage(String senderId, String stoneId, float x, float y) {
        super(senderId);
        this.stoneId = stoneId;
        this.x = x;
        this.y = y;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.STONE_DESTROYED;
    }
    
    public String getStoneId() {
        return stoneId;
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
}
