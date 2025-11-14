package wagemaker.uk.network;

/**
 * Message sent when a stone's health changes due to player attacks.
 */
public class StoneHealthUpdateMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String stoneId;
    private float health;
    
    public StoneHealthUpdateMessage() {
        super();
    }
    
    public StoneHealthUpdateMessage(String senderId, String stoneId, float health) {
        super(senderId);
        this.stoneId = stoneId;
        this.health = health;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.STONE_HEALTH_UPDATE;
    }
    
    public String getStoneId() {
        return stoneId;
    }
    
    public float getHealth() {
        return health;
    }
}
