package wagemaker.uk.network;

import java.io.Serializable;

/**
 * Represents the state of a tree in the game world.
 */
public class TreeState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String treeId;
    private TreeType type;
    private float x;
    private float y;
    private float health;
    private boolean exists;
    
    public TreeState() {
    }
    
    public TreeState(String treeId, TreeType type, float x, float y, float health, boolean exists) {
        this.treeId = treeId;
        this.type = type;
        this.x = x;
        this.y = y;
        this.health = health;
        this.exists = exists;
    }
    
    public String getTreeId() {
        return treeId;
    }
    
    public void setTreeId(String treeId) {
        this.treeId = treeId;
    }
    
    public TreeType getType() {
        return type;
    }
    
    public void setType(TreeType type) {
        this.type = type;
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
    
    public float getHealth() {
        return health;
    }
    
    public void setHealth(float health) {
        this.health = health;
    }
    
    public boolean isExists() {
        return exists;
    }
    
    public void setExists(boolean exists) {
        this.exists = exists;
    }
}
