package wagemaker.uk.network;

import java.io.Serializable;

/**
 * Represents the state of a stone in the game world.
 */
public class StoneState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String stoneId;
    private float x;
    private float y;
    private float health;
    
    public StoneState() {
    }
    
    public StoneState(String stoneId, float x, float y, float health) {
        this.stoneId = stoneId;
        this.x = x;
        this.y = y;
        this.health = health;
    }
    
    public String getStoneId() {
        return stoneId;
    }
    
    public void setStoneId(String stoneId) {
        this.stoneId = stoneId;
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
}
