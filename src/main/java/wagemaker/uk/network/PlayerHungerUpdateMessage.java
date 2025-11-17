package wagemaker.uk.network;

/**
 * Message broadcast when a player's hunger changes.
 */
public class PlayerHungerUpdateMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private float hunger;
    
    public PlayerHungerUpdateMessage() {
        super();
    }
    
    public PlayerHungerUpdateMessage(String senderId, String playerId, float hunger) {
        super(senderId);
        this.playerId = playerId;
        this.hunger = hunger;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.PLAYER_HUNGER_UPDATE;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public float getHunger() {
        return hunger;
    }
}
