package wagemaker.uk.world;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Metadata for save file listing and display.
 * Contains summary information about a world save without loading the full save data.
 * Used for displaying save lists in the UI and managing save files.
 */
public class WorldSaveInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String saveName;
    private long saveTimestamp;
    private String gameMode;
    private long worldSeed;
    private int treeCount;
    private int itemCount;
    private int clearedPositionCount;
    private float playerX;
    private float playerY;
    private float playerHealth;
    private long fileSizeBytes;
    private int saveFormatVersion;
    
    /**
     * Default constructor for serialization.
     */
    public WorldSaveInfo() {
    }
    
    /**
     * Creates WorldSaveInfo from WorldSaveData.
     * Extracts metadata without requiring the full save data to be kept in memory.
     * 
     * @param saveData The complete save data to extract metadata from
     * @param fileSizeBytes Size of the save file in bytes
     */
    public WorldSaveInfo(WorldSaveData saveData, long fileSizeBytes) {
        if (saveData != null) {
            this.saveName = saveData.getSaveName();
            this.saveTimestamp = saveData.getSaveTimestamp();
            this.gameMode = saveData.getGameMode();
            this.worldSeed = saveData.getWorldSeed();
            this.treeCount = saveData.getExistingTreeCount();
            this.itemCount = saveData.getUncollectedItemCount();
            this.clearedPositionCount = saveData.getClearedPositions() != null ? 
                saveData.getClearedPositions().size() : 0;
            this.playerX = saveData.getPlayerX();
            this.playerY = saveData.getPlayerY();
            this.playerHealth = saveData.getPlayerHealth();
            this.saveFormatVersion = saveData.getSaveFormatVersion();
        }
        this.fileSizeBytes = fileSizeBytes;
    }
    
    /**
     * Creates WorldSaveInfo with all parameters.
     * 
     * @param saveName Name of the save
     * @param saveTimestamp When the save was created
     * @param gameMode Game mode ("singleplayer" or "multiplayer")
     * @param worldSeed World generation seed
     * @param treeCount Number of existing trees
     * @param itemCount Number of uncollected items
     * @param clearedPositionCount Number of cleared positions
     * @param playerX Player X position
     * @param playerY Player Y position
     * @param playerHealth Player health
     * @param fileSizeBytes Size of save file in bytes
     * @param saveFormatVersion Save format version
     */
    public WorldSaveInfo(String saveName, long saveTimestamp, String gameMode, long worldSeed,
                        int treeCount, int itemCount, int clearedPositionCount,
                        float playerX, float playerY, float playerHealth,
                        long fileSizeBytes, int saveFormatVersion) {
        this.saveName = saveName;
        this.saveTimestamp = saveTimestamp;
        this.gameMode = gameMode;
        this.worldSeed = worldSeed;
        this.treeCount = treeCount;
        this.itemCount = itemCount;
        this.clearedPositionCount = clearedPositionCount;
        this.playerX = playerX;
        this.playerY = playerY;
        this.playerHealth = playerHealth;
        this.fileSizeBytes = fileSizeBytes;
        this.saveFormatVersion = saveFormatVersion;
    }
    
    /**
     * Gets a formatted timestamp string for display.
     * 
     * @return Human-readable timestamp string
     */
    public String getFormattedTimestamp() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(new Date(saveTimestamp));
    }
    
    /**
     * Gets a formatted file size string for display.
     * 
     * @return Human-readable file size (e.g., "1.2 MB", "345 KB")
     */
    public String getFormattedFileSize() {
        if (fileSizeBytes < 1024) {
            return fileSizeBytes + " B";
        } else if (fileSizeBytes < 1024 * 1024) {
            return String.format("%.1f KB", fileSizeBytes / 1024.0);
        } else {
            return String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024.0));
        }
    }
    
    /**
     * Gets a summary description of the save for display.
     * 
     * @return Summary string with key save information
     */
    public String getSummary() {
        return String.format("%s - %d trees, %d items, %d cleared areas",
            getFormattedTimestamp(), treeCount, itemCount, clearedPositionCount);
    }
    
    /**
     * Gets the player position as a formatted string.
     * 
     * @return Player position string (e.g., "(123, 456)")
     */
    public String getFormattedPlayerPosition() {
        return String.format("(%.0f, %.0f)", playerX, playerY);
    }
    
    /**
     * Checks if this save info represents a valid save.
     * 
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return saveName != null && !saveName.trim().isEmpty()
            && gameMode != null && (gameMode.equals("singleplayer") || gameMode.equals("multiplayer"))
            && saveTimestamp > 0
            && playerHealth >= 0 && playerHealth <= 100
            && treeCount >= 0 && itemCount >= 0 && clearedPositionCount >= 0;
    }
    
    /**
     * Compares save info by timestamp for sorting (newest first).
     * 
     * @param other The other WorldSaveInfo to compare to
     * @return Comparison result for sorting
     */
    public int compareByTimestamp(WorldSaveInfo other) {
        if (other == null) {
            return -1;
        }
        return Long.compare(other.saveTimestamp, this.saveTimestamp); // Reverse order (newest first)
    }
    
    /**
     * Compares save info by name for sorting (alphabetical).
     * 
     * @param other The other WorldSaveInfo to compare to
     * @return Comparison result for sorting
     */
    public int compareByName(WorldSaveInfo other) {
        if (other == null) {
            return -1;
        }
        if (this.saveName == null) {
            return other.saveName == null ? 0 : 1;
        }
        return this.saveName.compareToIgnoreCase(other.saveName);
    }
    
    // Getters and setters
    
    public String getSaveName() {
        return saveName;
    }
    
    public void setSaveName(String saveName) {
        this.saveName = saveName;
    }
    
    public long getSaveTimestamp() {
        return saveTimestamp;
    }
    
    public void setSaveTimestamp(long saveTimestamp) {
        this.saveTimestamp = saveTimestamp;
    }
    
    public String getGameMode() {
        return gameMode;
    }
    
    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }
    
    public long getWorldSeed() {
        return worldSeed;
    }
    
    public void setWorldSeed(long worldSeed) {
        this.worldSeed = worldSeed;
    }
    
    public int getTreeCount() {
        return treeCount;
    }
    
    public void setTreeCount(int treeCount) {
        this.treeCount = treeCount;
    }
    
    public int getItemCount() {
        return itemCount;
    }
    
    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }
    
    public int getClearedPositionCount() {
        return clearedPositionCount;
    }
    
    public void setClearedPositionCount(int clearedPositionCount) {
        this.clearedPositionCount = clearedPositionCount;
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
    
    public long getFileSizeBytes() {
        return fileSizeBytes;
    }
    
    public void setFileSizeBytes(long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }
    
    public int getSaveFormatVersion() {
        return saveFormatVersion;
    }
    
    public void setSaveFormatVersion(int saveFormatVersion) {
        this.saveFormatVersion = saveFormatVersion;
    }
    
    @Override
    public String toString() {
        return String.format("WorldSaveInfo[name=%s, mode=%s, timestamp=%s, trees=%d, items=%d, size=%s]",
            saveName, gameMode, getFormattedTimestamp(), treeCount, itemCount, getFormattedFileSize());
    }
}