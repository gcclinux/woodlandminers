package wagemaker.uk.network;

import java.io.Serializable;

/**
 * Represents the state of a player in the game world.
 */
public class PlayerState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private String playerName;
    private float x;
    private float y;
    private Direction direction;
    private float health;
    private boolean isMoving;
    private long lastUpdateTime;
    
    public PlayerState() {
    }
    
    public PlayerState(String playerId, String playerName, float x, float y, 
                      Direction direction, float health, boolean isMoving) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.health = health;
        this.isMoving = isMoving;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
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
    
    public Direction getDirection() {
        return direction;
    }
    
    public void setDirection(Direction direction) {
        this.direction = direction;
    }
    
    public float getHealth() {
        return health;
    }
    
    public void setHealth(float health) {
        this.health = health;
    }
    
    public boolean isMoving() {
        return isMoving;
    }
    
    public void setMoving(boolean moving) {
        isMoving = moving;
    }
    
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
