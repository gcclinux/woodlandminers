package wagemaker.uk.network;

/**
 * Message sent when a player plants baby bamboo.
 * Broadcasts to all clients to synchronize planted bamboo state.
 */
public class BambooPlantMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String plantedBambooId; // Unique ID for the planted bamboo
    private float x; // Tile-aligned x position
    private float y; // Tile-aligned y position
    private String playerId; // Player who planted it
    
    /**
     * Default constructor for serialization.
     */
    public BambooPlantMessage() {
        super();
    }
    
    /**
     * Creates a new bamboo plant message.
     * @param senderId The ID of the player planting
     * @param plantedBambooId The unique ID for the planted bamboo
     * @param x The tile-aligned x position
     * @param y The tile-aligned y position
     */
    public BambooPlantMessage(String senderId, String plantedBambooId, float x, float y) {
        super(senderId);
        this.plantedBambooId = plantedBambooId;
        this.x = x;
        this.y = y;
        this.playerId = senderId;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.BAMBOO_PLANT;
    }
    
    public String getPlantedBambooId() {
        return plantedBambooId;
    }
    
    public void setPlantedBambooId(String plantedBambooId) {
        this.plantedBambooId = plantedBambooId;
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
    
    public String getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
