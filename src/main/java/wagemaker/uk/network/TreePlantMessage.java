package wagemaker.uk.network;

/**
 * Network message sent when a player plants a baby tree.
 * This message is broadcast to all clients to synchronize the planting action.
 */
public class TreePlantMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private String plantedTreeId;
    private float x;
    private float y;
    
    /**
     * Default constructor for serialization.
     */
    public TreePlantMessage() {
        super();
    }
    
    /**
     * Creates a new tree plant message.
     * @param playerId The ID of the player who planted the tree
     * @param plantedTreeId Unique ID for the planted tree
     * @param x The x-coordinate where the tree was planted (tile-aligned)
     * @param y The y-coordinate where the tree was planted (tile-aligned)
     */
    public TreePlantMessage(String playerId, String plantedTreeId, float x, float y) {
        super(playerId);
        this.playerId = playerId;
        this.plantedTreeId = plantedTreeId;
        this.x = x;
        this.y = y;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.TREE_PLANT;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
    public String getPlantedTreeId() {
        return plantedTreeId;
    }
    
    public void setPlantedTreeId(String plantedTreeId) {
        this.plantedTreeId = plantedTreeId;
    }
    
    public float getX() {
        return x;
    }
    
    public void setX(float x) {
        this.x = x;
    }
    
    public float getY() {
        return y;
    }
    
    public void setY(float y) {
        this.y = y;
    }
}