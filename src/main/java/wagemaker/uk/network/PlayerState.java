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
    private float hunger;
    private boolean isMoving;
    private long lastUpdateTime;
    
    // Inventory fields
    private int appleCount;
    private int bananaCount;
    private int babyBambooCount;
    private int bambooStackCount;
    private int babyTreeCount;
    private int woodStackCount;
    private int pebbleCount;
    private int palmFiberCount;
    
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
    
    public float getHunger() {
        return hunger;
    }
    
    public void setHunger(float hunger) {
        this.hunger = hunger;
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
    
    // Inventory getters and setters
    public int getAppleCount() {
        return appleCount;
    }
    
    public void setAppleCount(int appleCount) {
        this.appleCount = appleCount;
    }
    
    public int getBananaCount() {
        return bananaCount;
    }
    
    public void setBananaCount(int bananaCount) {
        this.bananaCount = bananaCount;
    }
    
    public int getBabyBambooCount() {
        return babyBambooCount;
    }
    
    public void setBabyBambooCount(int babyBambooCount) {
        this.babyBambooCount = babyBambooCount;
    }
    
    public int getBambooStackCount() {
        return bambooStackCount;
    }
    
    public void setBambooStackCount(int bambooStackCount) {
        this.bambooStackCount = bambooStackCount;
    }
    
    public int getBabyTreeCount() {
        return babyTreeCount;
    }
    
    public void setBabyTreeCount(int babyTreeCount) {
        this.babyTreeCount = babyTreeCount;
    }
    
    public int getWoodStackCount() {
        return woodStackCount;
    }
    
    public void setWoodStackCount(int woodStackCount) {
        this.woodStackCount = woodStackCount;
    }
    
    public int getPebbleCount() {
        return pebbleCount;
    }
    
    public void setPebbleCount(int pebbleCount) {
        this.pebbleCount = pebbleCount;
    }
    
    public int getPalmFiberCount() {
        return palmFiberCount;
    }
    
    public void setPalmFiberCount(int palmFiberCount) {
        this.palmFiberCount = palmFiberCount;
    }
}
