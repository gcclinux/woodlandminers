package wagemaker.uk.network;

import java.io.Serializable;

/**
 * Represents the state of a dropped item in the game world.
 */
public class ItemState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String itemId;
    private ItemType type;
    private float x;
    private float y;
    private boolean collected;
    
    public ItemState() {
    }
    
    public ItemState(String itemId, ItemType type, float x, float y, boolean collected) {
        this.itemId = itemId;
        this.type = type;
        this.x = x;
        this.y = y;
        this.collected = collected;
    }
    
    public String getItemId() {
        return itemId;
    }
    
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
    
    public ItemType getType() {
        return type;
    }
    
    public void setType(ItemType type) {
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
    
    public boolean isCollected() {
        return collected;
    }
    
    public void setCollected(boolean collected) {
        this.collected = collected;
    }
}
