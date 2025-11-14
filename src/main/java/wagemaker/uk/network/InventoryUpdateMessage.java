package wagemaker.uk.network;

/**
 * Message sent when a player's inventory changes.
 * Sent from client to server when items are collected or consumed.
 */
public class InventoryUpdateMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private int appleCount;
    private int bananaCount;
    private int babyBambooCount;
    private int bambooStackCount;
    private int woodStackCount;
    private int pebbleCount;
    
    public InventoryUpdateMessage() {
        super();
    }
    
    public InventoryUpdateMessage(String senderId, String playerId, 
                                   int appleCount, int bananaCount, 
                                   int babyBambooCount, int bambooStackCount, 
                                   int woodStackCount, int pebbleCount) {
        super(senderId);
        this.playerId = playerId;
        this.appleCount = appleCount;
        this.bananaCount = bananaCount;
        this.babyBambooCount = babyBambooCount;
        this.bambooStackCount = bambooStackCount;
        this.woodStackCount = woodStackCount;
        this.pebbleCount = pebbleCount;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.INVENTORY_UPDATE;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public int getAppleCount() {
        return appleCount;
    }
    
    public int getBananaCount() {
        return bananaCount;
    }
    
    public int getBabyBambooCount() {
        return babyBambooCount;
    }
    
    public int getBambooStackCount() {
        return bambooStackCount;
    }
    
    public int getWoodStackCount() {
        return woodStackCount;
    }
    
    public int getPebbleCount() {
        return pebbleCount;
    }
}
