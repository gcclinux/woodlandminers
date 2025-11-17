package wagemaker.uk.network;

/**
 * Message sent when a player respawns (from death or hunger death).
 */
public class PlayerRespawnMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private float x;
    private float y;
    private float health;
    private float hunger;
    
    public PlayerRespawnMessage() {
        super();
    }
    
    public PlayerRespawnMessage(String senderId, String playerId, float x, float y, float health, float hunger) {
        super(senderId);
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.health = health;
        this.hunger = hunger;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.PLAYER_RESPAWN;
    }
    
    public String getPlayerId() {
        return playerId;
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
    
    public float getHunger() {
        return hunger;
    }
}
