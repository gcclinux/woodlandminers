package wagemaker.uk.world;

import wagemaker.uk.network.TreeState;
import wagemaker.uk.network.ItemState;
import wagemaker.uk.weather.RainZone;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.List;

/**
 * Data structure for serialized world state.
 * Contains complete world information that can be saved to and loaded from disk.
 * Implements version compatibility for future save format changes.
 */
public class WorldSaveData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Version for save format compatibility
    private static final int SAVE_FORMAT_VERSION = 1;
    
    // Core world data
    private long worldSeed;
    private Map<String, TreeState> trees;
    private Map<String, ItemState> items;
    private Set<String> clearedPositions;
    private List<RainZone> rainZones;
    
    // Player data at save time
    private float playerX;
    private float playerY;
    private float playerHealth;
    
    // Save metadata
    private long saveTimestamp;
    private String saveName;
    private String gameMode; // "singleplayer" or "multiplayer"
    private int saveFormatVersion;
    
    /**
     * Default constructor for serialization.
     */
    public WorldSaveData() {
        this.saveFormatVersion = SAVE_FORMAT_VERSION;
        this.saveTimestamp = System.currentTimeMillis();
    }
    
    /**
     * Creates a new WorldSaveData with complete world state.
     * 
     * @param worldSeed The world generation seed
     * @param trees Map of all tree states
     * @param items Map of all item states
     * @param clearedPositions Set of cleared tree positions
     * @param rainZones List of rain zones
     * @param playerX Player X position at save time
     * @param playerY Player Y position at save time
     * @param playerHealth Player health at save time
     * @param saveName Name of this save
     * @param gameMode Game mode ("singleplayer" or "multiplayer")
     */
    public WorldSaveData(long worldSeed, Map<String, TreeState> trees, Map<String, ItemState> items,
                        Set<String> clearedPositions, List<RainZone> rainZones,
                        float playerX, float playerY, float playerHealth,
                        String saveName, String gameMode) {
        this();
        this.worldSeed = worldSeed;
        this.trees = trees;
        this.items = items;
        this.clearedPositions = clearedPositions;
        this.rainZones = rainZones;
        this.playerX = playerX;
        this.playerY = playerY;
        this.playerHealth = playerHealth;
        this.saveName = saveName;
        this.gameMode = gameMode;
    }
    
    /**
     * Validates the save data integrity.
     * Checks for required fields and data consistency.
     * 
     * @return true if save data is valid, false otherwise
     */
    public boolean isValid() {
        // Check required fields
        if (saveName == null || saveName.trim().isEmpty()) {
            return false;
        }
        
        if (gameMode == null || (!gameMode.equals("singleplayer") && !gameMode.equals("multiplayer"))) {
            return false;
        }
        
        // Check data collections are not null
        if (trees == null || items == null || clearedPositions == null) {
            return false;
        }
        
        // Check player health is valid
        if (playerHealth < 0 || playerHealth > 100) {
            return false;
        }
        
        // Check save format version compatibility
        if (saveFormatVersion > SAVE_FORMAT_VERSION) {
            return false; // Future version, cannot load
        }
        
        return true;
    }
    
    /**
     * Gets the number of existing trees in this save.
     * 
     * @return Count of trees that exist (not destroyed)
     */
    public int getExistingTreeCount() {
        if (trees == null) {
            return 0;
        }
        
        return (int) trees.values().stream()
            .filter(tree -> tree != null && tree.isExists())
            .count();
    }
    
    /**
     * Gets the number of uncollected items in this save.
     * 
     * @return Count of items that are not collected
     */
    public int getUncollectedItemCount() {
        if (items == null) {
            return 0;
        }
        
        return (int) items.values().stream()
            .filter(item -> item != null && !item.isCollected())
            .count();
    }
    
    /**
     * Checks if this save is compatible with the current game version.
     * 
     * @return true if compatible, false if version mismatch
     */
    public boolean isCompatible() {
        return saveFormatVersion <= SAVE_FORMAT_VERSION;
    }
    
    // Getters and setters
    
    public long getWorldSeed() {
        return worldSeed;
    }
    
    public void setWorldSeed(long worldSeed) {
        this.worldSeed = worldSeed;
    }
    
    public Map<String, TreeState> getTrees() {
        return trees;
    }
    
    public void setTrees(Map<String, TreeState> trees) {
        this.trees = trees;
    }
    
    public Map<String, ItemState> getItems() {
        return items;
    }
    
    public void setItems(Map<String, ItemState> items) {
        this.items = items;
    }
    
    public Set<String> getClearedPositions() {
        return clearedPositions;
    }
    
    public void setClearedPositions(Set<String> clearedPositions) {
        this.clearedPositions = clearedPositions;
    }
    
    public List<RainZone> getRainZones() {
        return rainZones;
    }
    
    public void setRainZones(List<RainZone> rainZones) {
        this.rainZones = rainZones;
    }
    
    public float getPlayerX() {
        return playerX;
    }
    
    public void setPlayerX(float playerX) {
        this.playerX = playerX;
    }
    
    public float getPlayerY() {
        return playerY;
    }
    
    public void setPlayerY(float playerY) {
        this.playerY = playerY;
    }
    
    public float getPlayerHealth() {
        return playerHealth;
    }
    
    public void setPlayerHealth(float playerHealth) {
        this.playerHealth = playerHealth;
    }
    
    public long getSaveTimestamp() {
        return saveTimestamp;
    }
    
    public void setSaveTimestamp(long saveTimestamp) {
        this.saveTimestamp = saveTimestamp;
    }
    
    public String getSaveName() {
        return saveName;
    }
    
    public void setSaveName(String saveName) {
        this.saveName = saveName;
    }
    
    public String getGameMode() {
        return gameMode;
    }
    
    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }
    
    public int getSaveFormatVersion() {
        return saveFormatVersion;
    }
    
    public void setSaveFormatVersion(int saveFormatVersion) {
        this.saveFormatVersion = saveFormatVersion;
    }
    
    @Override
    public String toString() {
        return String.format("WorldSaveData[name=%s, mode=%s, seed=%d, trees=%d, items=%d, timestamp=%d]",
            saveName, gameMode, worldSeed, 
            trees != null ? trees.size() : 0,
            items != null ? items.size() : 0,
            saveTimestamp);
    }
}