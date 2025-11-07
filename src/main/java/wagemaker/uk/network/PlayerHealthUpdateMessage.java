package wagemaker.uk.network;

/**
 * Message broadcast when a player's health changes.
 */
public class PlayerHealthUpdateMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private float health;
    
    public PlayerHealthUpdateMessage() {
        super();
    }
    
    public PlayerHealthUpdateMessage(String senderId, String playerId, float health) {
        super(senderId);
        this.playerId = playerId;
        this.health = health;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.PLAYER_HEALTH_UPDATE;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public float getHealth() {
        return health;
    }
}
