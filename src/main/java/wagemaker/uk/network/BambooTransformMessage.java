package wagemaker.uk.network;

/**
 * Message sent when a planted bamboo transforms into a bamboo tree.
 * Broadcasts to all clients to synchronize the transformation.
 */
public class BambooTransformMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String plantedBambooId; // ID of the planted bamboo being removed
    private String bambooTreeId; // ID of the new bamboo tree
    private float x; // Tile-aligned x position
    private float y; // Tile-aligned y position
    
    /**
     * Default constructor for serialization.
     */
    public BambooTransformMessage() {
        super();
    }
    
    /**
     * Creates a new bamboo transform message.
     * @param senderId The server ID (transformations are server-authoritative)
     * @param plantedBambooId The ID of the planted bamboo being removed
     * @param bambooTreeId The ID of the new bamboo tree
     * @param x The tile-aligned x position
     * @param y The tile-aligned y position
     */
    public BambooTransformMessage(String senderId, String plantedBambooId, String bambooTreeId, float x, float y) {
        super(senderId);
        this.plantedBambooId = plantedBambooId;
        this.bambooTreeId = bambooTreeId;
        this.x = x;
        this.y = y;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.BAMBOO_TRANSFORM;
    }
    
    public String getPlantedBambooId() {
        return plantedBambooId;
    }
    
    public void setPlantedBambooId(String plantedBambooId) {
        this.plantedBambooId = plantedBambooId;
    }
    
    public String getBambooTreeId() {
        return bambooTreeId;
    }
    
    public void setBambooTreeId(String bambooTreeId) {
        this.bambooTreeId = bambooTreeId;
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
