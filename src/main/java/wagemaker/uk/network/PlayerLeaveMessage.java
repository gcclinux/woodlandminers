package wagemaker.uk.network;

/**
 * Message sent when a player leaves the server.
 * This message is broadcast to all remaining clients to notify them of the player's departure.
 */
public class PlayerLeaveMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private String playerName;
    
    /**
     * Default constructor for serialization.
     */
    public PlayerLeaveMessage() {
        super();
    }
    
    /**
     * Creates a new player leave message.
     * @param playerId The unique ID of the leaving player
     * @param playerName The name of the leaving player
     */
    public PlayerLeaveMessage(String playerId, String playerName) {
        super(playerId);
        this.playerId = playerId;
        this.playerName = playerName;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.PLAYER_LEAVE;
    }
    
    /**
     * Gets the player ID of the leaving player.
     * @return The player ID
     */
    public String getPlayerId() {
        return playerId;
    }
    
    /**
     * Gets the name of the leaving player.
     * @return The player name
     */
    public String getPlayerName() {
        return playerName;
    }
}
