package wagemaker.uk.network;

/**
 * Network message sent when a planted tree transforms into a full SmallTree.
 * This message is broadcast to all clients to synchronize the transformation.
 */
public class TreeTransformMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private String plantedTreeId;
    private String smallTreeId;
    private float x;
    private float y;
    
    /**
     * Default constructor for serialization.
     */
    public TreeTransformMessage() {
        super();
    }
    
    /**
     * Creates a new tree transform message.
     * @param playerId The ID of the player who originally planted the tree
     * @param plantedTreeId The ID of the planted tree being transformed
     * @param smallTreeId The ID of the new SmallTree
     * @param x The x-coordinate of the transformation
     * @param y The y-coordinate of the transformation
     */
    public TreeTransformMessage(String playerId, String plantedTreeId, String smallTreeId, float x, float y) {
        super(playerId);
        this.playerId = playerId;
        this.plantedTreeId = plantedTreeId;
        this.smallTreeId = smallTreeId;
        this.x = x;
        this.y = y;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.TREE_TRANSFORM;
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
    
    public String getSmallTreeId() {
        return smallTreeId;
    }
    
    public void setSmallTreeId(String smallTreeId) {
        this.smallTreeId = smallTreeId;
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