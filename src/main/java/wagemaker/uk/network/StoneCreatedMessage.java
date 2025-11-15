package wagemaker.uk.network;

public class StoneCreatedMessage extends NetworkMessage {
    private String stoneId;
    private float x;
    private float y;
    private float health;
    
    public StoneCreatedMessage(String senderId, String stoneId, float x, float y, float health) {
        super(senderId);
        this.stoneId = stoneId;
        this.x = x;
        this.y = y;
        this.health = health;
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
    
    public float getHealth() {
        return health;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.STONE_CREATED;
    }
}
