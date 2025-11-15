package wagemaker.uk.network;

import wagemaker.uk.respawn.ResourceType;

/**
 * Message sent when a resource respawns after its timer expires.
 * Broadcast to all clients in multiplayer mode to synchronize resource respawn.
 */
public class ResourceRespawnMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String resourceId;
    private ResourceType resourceType;
    private TreeType treeType; // null for stones
    private float x;
    private float y;
    
    /**
     * Default constructor for serialization.
     */
    public ResourceRespawnMessage() {
        super();
    }
    
    /**
     * Constructor for creating a resource respawn message.
     * 
     * @param senderId The sender's unique identifier (typically server ID)
     * @param resourceId Unique identifier for the resource
     * @param resourceType Type of resource (TREE or STONE)
     * @param treeType Type of tree (null for stones)
     * @param x World X coordinate
     * @param y World Y coordinate
     */
    public ResourceRespawnMessage(String senderId, String resourceId, ResourceType resourceType,
                                 TreeType treeType, float x, float y) {
        super(senderId);
        this.resourceId = resourceId;
        this.resourceType = resourceType;
        this.treeType = treeType;
        this.x = x;
        this.y = y;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.RESOURCE_RESPAWN;
    }
    
    public String getResourceId() {
        return resourceId;
    }
    
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    
    public ResourceType getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }
    
    public TreeType getTreeType() {
        return treeType;
    }
    
    public void setTreeType(TreeType treeType) {
        this.treeType = treeType;
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
    
    @Override
    public String toString() {
        return "ResourceRespawnMessage{" +
                "resourceId='" + resourceId + '\'' +
                ", resourceType=" + resourceType +
                ", treeType=" + treeType +
                ", x=" + x +
                ", y=" + y +
                ", senderId='" + senderId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
