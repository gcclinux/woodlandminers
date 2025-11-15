package wagemaker.uk.respawn;

import wagemaker.uk.network.TreeType;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.IOException;

/**
 * Represents a single pending respawn with all necessary data.
 * Tracks a destroyed resource and its respawn timer information.
 */
public class RespawnEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String resourceId;
    private ResourceType resourceType;
    private float x;
    private float y;
    private long destructionTimestamp;
    private long respawnDuration; // milliseconds
    private TreeType treeType; // null for stones
    
    /**
     * Default constructor for serialization.
     */
    public RespawnEntry() {
    }
    
    /**
     * Constructor for creating a new respawn entry.
     * 
     * @param resourceId Unique identifier for the resource
     * @param resourceType Type of resource (TREE or STONE)
     * @param x World X coordinate
     * @param y World Y coordinate
     * @param destructionTimestamp Timestamp when resource was destroyed
     * @param respawnDuration Duration in milliseconds until respawn
     * @param treeType Type of tree (null for stones)
     */
    public RespawnEntry(String resourceId, ResourceType resourceType, float x, float y,
                       long destructionTimestamp, long respawnDuration, TreeType treeType) {
        this.resourceId = resourceId;
        this.resourceType = resourceType;
        this.x = x;
        this.y = y;
        this.destructionTimestamp = destructionTimestamp;
        this.respawnDuration = respawnDuration;
        this.treeType = treeType;
    }
    
    /**
     * Calculates the remaining time until respawn.
     * 
     * @return Remaining time in milliseconds, or 0 if ready to respawn
     */
    public long getRemainingTime() {
        try {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - destructionTimestamp;
            long remaining = respawnDuration - elapsedTime;
            return Math.max(0, remaining);
        } catch (Exception e) {
            System.err.println("[RespawnEntry] ERROR: Failed to calculate remaining time: " + e.getMessage());
            return 0; // Assume ready to respawn on error
        }
    }
    
    /**
     * Checks if the resource is ready to respawn.
     * 
     * @return true if the respawn timer has expired, false otherwise
     */
    public boolean isReadyToRespawn() {
        try {
            return getRemainingTime() == 0;
        } catch (Exception e) {
            System.err.println("[RespawnEntry] ERROR: Failed to check if ready to respawn: " + e.getMessage());
            return true; // Assume ready to respawn on error
        }
    }
    
    /**
     * Validates this respawn entry to ensure all data is valid.
     * 
     * @return true if valid, false if corrupted
     */
    public boolean isValid() {
        try {
            if (resourceId == null || resourceId.trim().isEmpty()) {
                return false;
            }
            
            if (resourceType == null) {
                return false;
            }
            
            if (Float.isNaN(x) || Float.isInfinite(x) || Float.isNaN(y) || Float.isInfinite(y)) {
                return false;
            }
            
            if (destructionTimestamp <= 0 || respawnDuration <= 0) {
                return false;
            }
            
            if (resourceType == ResourceType.TREE && treeType == null) {
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("[RespawnEntry] ERROR: Exception during validation: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Custom deserialization to handle corrupted data gracefully.
     * 
     * @param in ObjectInputStream to read from
     * @throws IOException if I/O error occurs
     * @throws ClassNotFoundException if class not found
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        try {
            in.defaultReadObject();
            
            // Validate deserialized data
            if (!isValid()) {
                System.err.println("[RespawnEntry] WARNING: Deserialized corrupted respawn entry: " + this);
            }
            
        } catch (Exception e) {
            System.err.println("[RespawnEntry] ERROR: Failed to deserialize respawn entry: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to deserialize RespawnEntry", e);
        }
    }
    
    // Getters and setters
    
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
    
    public long getDestructionTimestamp() {
        return destructionTimestamp;
    }
    
    public void setDestructionTimestamp(long destructionTimestamp) {
        this.destructionTimestamp = destructionTimestamp;
    }
    
    public long getRespawnDuration() {
        return respawnDuration;
    }
    
    public void setRespawnDuration(long respawnDuration) {
        this.respawnDuration = respawnDuration;
    }
    
    public TreeType getTreeType() {
        return treeType;
    }
    
    public void setTreeType(TreeType treeType) {
        this.treeType = treeType;
    }
    
    @Override
    public String toString() {
        return "RespawnEntry{" +
                "resourceId='" + resourceId + '\'' +
                ", resourceType=" + resourceType +
                ", x=" + x +
                ", y=" + y +
                ", destructionTimestamp=" + destructionTimestamp +
                ", respawnDuration=" + respawnDuration +
                ", treeType=" + treeType +
                ", remainingTime=" + getRemainingTime() +
                '}';
    }
}
