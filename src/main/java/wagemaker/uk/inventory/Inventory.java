package wagemaker.uk.inventory;

/**
 * Represents a player's inventory storage for collected items.
 * Maintains separate counts for each item type and provides methods to add/remove items.
 * Prevents item counts from becoming negative.
 */
public class Inventory {
    private int appleCount;
    private int bananaCount;
    private int babyBambooCount;
    private int bambooStackCount;
    private int woodStackCount;
    
    public Inventory() {
        this.appleCount = 0;
        this.bananaCount = 0;
        this.babyBambooCount = 0;
        this.bambooStackCount = 0;
        this.woodStackCount = 0;
    }
    
    // Apple methods
    public int getAppleCount() {
        return appleCount;
    }
    
    public void setAppleCount(int count) {
        this.appleCount = Math.max(0, count);
    }
    
    public void addApple(int amount) {
        this.appleCount += amount;
    }
    
    public boolean removeApple(int amount) {
        if (appleCount >= amount) {
            appleCount -= amount;
            return true;
        }
        return false;
    }
    
    // Banana methods
    public int getBananaCount() {
        return bananaCount;
    }
    
    public void setBananaCount(int count) {
        this.bananaCount = Math.max(0, count);
    }
    
    public void addBanana(int amount) {
        this.bananaCount += amount;
    }
    
    public boolean removeBanana(int amount) {
        if (bananaCount >= amount) {
            bananaCount -= amount;
            return true;
        }
        return false;
    }
    
    // BabyBamboo methods
    public int getBabyBambooCount() {
        return babyBambooCount;
    }
    
    public void setBabyBambooCount(int count) {
        this.babyBambooCount = Math.max(0, count);
    }
    
    public void addBabyBamboo(int amount) {
        this.babyBambooCount += amount;
    }
    
    public boolean removeBabyBamboo(int amount) {
        if (babyBambooCount >= amount) {
            babyBambooCount -= amount;
            return true;
        }
        return false;
    }
    
    // BambooStack methods
    public int getBambooStackCount() {
        return bambooStackCount;
    }
    
    public void setBambooStackCount(int count) {
        this.bambooStackCount = Math.max(0, count);
    }
    
    public void addBambooStack(int amount) {
        this.bambooStackCount += amount;
    }
    
    public boolean removeBambooStack(int amount) {
        if (bambooStackCount >= amount) {
            bambooStackCount -= amount;
            return true;
        }
        return false;
    }
    
    // WoodStack methods
    public int getWoodStackCount() {
        return woodStackCount;
    }
    
    public void setWoodStackCount(int count) {
        this.woodStackCount = Math.max(0, count);
    }
    
    public void addWoodStack(int amount) {
        this.woodStackCount += amount;
    }
    
    public boolean removeWoodStack(int amount) {
        if (woodStackCount >= amount) {
            woodStackCount -= amount;
            return true;
        }
        return false;
    }
    
    /**
     * Clear all items from inventory, resetting all counts to 0.
     */
    public void clear() {
        this.appleCount = 0;
        this.bananaCount = 0;
        this.babyBambooCount = 0;
        this.bambooStackCount = 0;
        this.woodStackCount = 0;
    }
}
