package wagemaker.uk.network;

/**
 * Message sent when a new player joins the server.
 * This message is broadcast to all existing clients to notify them of the new player.
 */
public class PlayerJoinMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private String playerName;
    private float x;
    private float y;
    
    /**
     * Default constructor for serialization.
     */
    public PlayerJoinMessage() {
        super();
    }
    
    /**
     * Creates a new player join message.
     * @param playerId The unique ID of the joining player
     * @param playerName The name of the joining player
     * @param x The initial x position of the player
     * @param y The initial y position of the player
     */
    public PlayerJoinMessage(String playerId, String playerName, float x, float y) {
        super(playerId);
        this.playerId = playerId;
        this.playerName = playerName;
        this.x = x;
        this.y = y;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.PLAYER_JOIN;
    }
    
    /**
     * Gets the player ID of the joining player.
     * @return The player ID
     */
    public String getPlayerId() {
        return playerId;
    }
    
    /**
     * Gets the name of the joining player.
     * @return The player name
     */
    public String getPlayerName() {
        return playerName;
    }
    
    /**
     * Gets the initial x position of the joining player.
     * @return The x position
     */
    public float getX() {
        return x;
    }
    
    /**
     * Gets the initial y position of the joining player.
     * @return The y position
     */
    public float getY() {
        return y;
    }
}
