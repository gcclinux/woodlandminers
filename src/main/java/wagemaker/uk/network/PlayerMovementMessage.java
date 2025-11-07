package wagemaker.uk.network;

/**
 * Message sent when a player moves to update their position and direction.
 */
public class PlayerMovementMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private float x;
    private float y;
    private Direction direction;
    private boolean isMoving;
    
    public PlayerMovementMessage() {
        super();
    }
    
    public PlayerMovementMessage(String senderId, float x, float y, Direction direction, boolean isMoving) {
        super(senderId);
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.isMoving = isMoving;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.PLAYER_MOVEMENT;
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    public boolean isMoving() {
        return isMoving;
    }
}
