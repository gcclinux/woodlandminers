package wagemaker.uk.world;

import wagemaker.uk.network.WorldState;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Manages world save and load operations.
 * Handles file I/O, directory management, and save file validation for complete world state persistence.
 * Supports both singleplayer and multiplayer save modes with separate directory structures.
 */
public class WorldSaveManager {
    private static final String WORLD_SAVES_DIR = "world-saves";
    private static final String SINGLEPLAYER_DIR = "singleplayer";
    private static final String MULTIPLAYER_DIR = "multiplayer";
    private static final String SAVE_FILE_EXTENSION = ".wld";
    private static final String BACKUP_SUFFIX = ".backup";
    
    // Save name validation pattern - alphanumeric, spaces, hyphens, underscores only
    private static final Pattern VALID_SAVE_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-_]{1,50}$");
    
    /**
     * Gets the configuration directory based on the operating system.
     * Uses the same directory structure as PlayerConfig for consistency.
     * 
     * @return The configuration directory
     */
    private static File getConfigDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        
        if (os.contains("win")) {
            // Windows: %APPDATA%/Woodlanders
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                return new File(appData, "Woodlanders");
            } else {
                return new File(userHome, "AppData/Roaming/Woodlanders");
            }
        } else if (os.contains("mac")) {
            // macOS: ~/Library/Application Support/Woodlanders
            return new File(userHome, "Library/Application Support/Woodlanders");
        } else {
            // Linux/Unix: ~/.config/woodlanders
            return new File(userHome, ".config/woodlanders");
        }
    }
    
    /**
     * Gets the save directory for the specified game mode.
     * 
     * @param isMultiplayer true for multiplayer saves, false for singleplayer
     * @return The save directory
     */
    private static File getSaveDirectory(boolean isMultiplayer) {
        File configDir = getConfigDirectory();
        File worldSavesDir = new File(configDir, WORLD_SAVES_DIR);
        return new File(worldSavesDir, isMultiplayer ? MULTIPLAYER_DIR : SINGLEPLAYER_DIR);
    }
    
    /**
     * Gets the save file for the specified save name and game mode.
     * 
     * @param saveName The name of the save
     * @param isMultiplayer true for multiplayer saves, false for singleplayer
     * @return The save file
     */
    private static File getSaveFile(String saveName, boolean isMultiplayer) {
        File saveDir = getSaveDirectory(isMultiplayer);
        return new File(saveDir, saveName + SAVE_FILE_EXTENSION);
    }
    
    /**
     * Gets the backup file for the specified save name and game mode.
     * 
     * @param saveName The name of the save
     * @param isMultiplayer true for multiplayer saves, false for singleplayer
     * @return The backup file
     */
    private static File getBackupFile(String saveName, boolean isMultiplayer) {
        File saveDir = getSaveDirectory(isMultiplayer);
        return new File(saveDir, saveName + SAVE_FILE_EXTENSION + BACKUP_SUFFIX);
    }
    
    /**
     * Validates a save name to prevent file system conflicts and security issues.
     * 
     * @param saveName The save name to validate
     * @return true if the save name is valid, false otherwise
     */
    public static boolean isValidSaveName(String saveName) {
        if (saveName == null || saveName.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = saveName.trim();
        
        // Check pattern match
        if (!VALID_SAVE_NAME_PATTERN.matcher(trimmed).matches()) {
            return false;
        }
        
        // Check for reserved names (case insensitive)
        String lower = trimmed.toLowerCase();
        if (lower.equals("con") || lower.equals("prn") || lower.equals("aux") || lower.equals("nul") ||
            lower.startsWith("com") || lower.startsWith("lpt")) {
            return false; // Windows reserved names
        }
        
        // Check for directory traversal attempts
        if (trimmed.contains("..") || trimmed.contains("/") || trimmed.contains("\\")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Saves the current world state to a file.
     * Creates a backup of existing save before overwriting.
     * 
     * @param saveName The name for the save file
     * @param worldState The current world state to save
     * @param playerX Player X position at save time
     * @param playerY Player Y position at save time
     * @param playerHealth Player health at save time
     * @param inventory Player inventory at save time (can be null)
     * @param isMultiplayer true if this is a multiplayer save, false for singleplayer
     * @return true if save was successful, false otherwise
     */
    public static boolean saveWorld(String saveName, WorldState worldState, 
                                  float playerX, float playerY, float playerHealth,
                                  wagemaker.uk.inventory.Inventory inventory,
                                  boolean isMultiplayer) {
        try {
            // Validate save name
            if (!isValidSaveName(saveName)) {
                System.err.println("Invalid save name: " + saveName);
                return false;
            }
            
            // Validate world state
            if (worldState == null) {
                System.err.println("Cannot save null world state");
                return false;
            }
            
            // Create save directory if it doesn't exist
            File saveDir = getSaveDirectory(isMultiplayer);
            if (!saveDir.exists()) {
                if (!saveDir.mkdirs()) {
                    System.err.println("Failed to create save directory: " + saveDir.getAbsolutePath());
                    return false;
                }
            }
            
            File saveFile = getSaveFile(saveName, isMultiplayer);
            File backupFile = getBackupFile(saveName, isMultiplayer);
            
            // Create backup of existing save
            if (saveFile.exists()) {
                try {
                    Files.copy(saveFile.toPath(), backupFile.toPath(), 
                              java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Created backup: " + backupFile.getAbsolutePath());
                } catch (IOException e) {
                    System.err.println("Failed to create backup, continuing anyway: " + e.getMessage());
                }
            }
            
            // Create WorldSaveData from current state
            WorldSaveData saveData = new WorldSaveData(
                worldState.getWorldSeed(),
                worldState.getTrees(),
                worldState.getStones(),
                worldState.getItems(),
                worldState.getClearedPositions(),
                worldState.getRainZones(),
                playerX,
                playerY,
                playerHealth,
                saveName,
                isMultiplayer ? "multiplayer" : "singleplayer"
            );
            
            // Set inventory data if provided
            if (inventory != null) {
                saveData.setAppleCount(inventory.getAppleCount());
                saveData.setBananaCount(inventory.getBananaCount());
                saveData.setBabyBambooCount(inventory.getBabyBambooCount());
                saveData.setBambooStackCount(inventory.getBambooStackCount());
                saveData.setWoodStackCount(inventory.getWoodStackCount());
                saveData.setPebbleCount(inventory.getPebbleCount());
            }
            
            // Validate save data before writing
            if (!saveData.isValid()) {
                System.err.println("Save data validation failed");
                return false;
            }
            
            // Write save data to file
            try (FileOutputStream fos = new FileOutputStream(saveFile);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                
                oos.writeObject(saveData);
                oos.flush();
            }
            
            System.out.println("World saved successfully: " + saveFile.getAbsolutePath());
            System.out.println("Save contains " + saveData.getExistingTreeCount() + " trees, " + 
                             saveData.getExistingStoneCount() + " stones, and " + 
                             saveData.getUncollectedItemCount() + " items");
            
            return true;
            
        } catch (IOException e) {
            System.err.println("Failed to save world: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error saving world: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Loads a world state from a save file.
     * Validates the save file before loading and performs integrity checks.
     * 
     * @param saveName The name of the save to load
     * @param isMultiplayer true if this is a multiplayer save, false for singleplayer
     * @return The loaded WorldSaveData, or null if loading failed
     */
    public static WorldSaveData loadWorld(String saveName, boolean isMultiplayer) {
        try {
            // Validate save name
            if (!isValidSaveName(saveName)) {
                System.err.println("Invalid save name: " + saveName);
                return null;
            }
            
            File saveFile = getSaveFile(saveName, isMultiplayer);
            
            // Check if save file exists
            if (!saveFile.exists()) {
                System.err.println("Save file not found: " + saveFile.getAbsolutePath());
                return null;
            }
            
            // Check if file is readable
            if (!saveFile.canRead()) {
                System.err.println("Cannot read save file: " + saveFile.getAbsolutePath());
                return null;
            }
            
            // Load save data from file
            WorldSaveData saveData;
            try (FileInputStream fis = new FileInputStream(saveFile);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                
                Object obj = ois.readObject();
                if (!(obj instanceof WorldSaveData)) {
                    System.err.println("Save file contains invalid data type");
                    return null;
                }
                
                saveData = (WorldSaveData) obj;
            }
            
            // Validate loaded save data
            if (!saveData.isValid()) {
                System.err.println("Loaded save data failed validation");
                return null;
            }
            
            // Check compatibility
            if (!saveData.isCompatible()) {
                System.err.println("Save file is from a newer version and cannot be loaded");
                return null;
            }
            
            System.out.println("World loaded successfully: " + saveFile.getAbsolutePath());
            System.out.println("Loaded " + saveData.getExistingTreeCount() + " trees, " + 
                             saveData.getExistingStoneCount() + " stones, and " + 
                             saveData.getUncollectedItemCount() + " items");
            
            return saveData;
            
        } catch (IOException e) {
            System.err.println("Failed to load world: " + e.getMessage());
            return null;
        } catch (ClassNotFoundException e) {
            System.err.println("Save file format not recognized: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error loading world: " + e.getMessage());
            return null;
        }
    }    
    
/**
     * Lists all available save files for the specified game mode.
     * Returns metadata for each save without loading the full save data.
     * 
     * @param isMultiplayer true for multiplayer saves, false for singleplayer
     * @return List of WorldSaveInfo objects, sorted by timestamp (newest first)
     */
    public static List<WorldSaveInfo> listAvailableSaves(boolean isMultiplayer) {
        List<WorldSaveInfo> saves = new ArrayList<>();
        
        try {
            File saveDir = getSaveDirectory(isMultiplayer);
            
            // Check if save directory exists
            if (!saveDir.exists()) {
                System.out.println("Save directory does not exist: " + saveDir.getAbsolutePath());
                return saves; // Return empty list
            }
            
            // List all .wld files in the directory
            File[] saveFiles = saveDir.listFiles((dir, name) -> 
                name.endsWith(SAVE_FILE_EXTENSION) && !name.endsWith(BACKUP_SUFFIX));
            
            if (saveFiles == null) {
                System.err.println("Failed to list files in save directory: " + saveDir.getAbsolutePath());
                return saves;
            }
            
            // Process each save file
            for (File saveFile : saveFiles) {
                try {
                    // Extract save name from filename
                    String fileName = saveFile.getName();
                    String saveName = fileName.substring(0, fileName.length() - SAVE_FILE_EXTENSION.length());
                    
                    // Get file size
                    long fileSize = saveFile.length();
                    
                    // Try to load save metadata without loading full data
                    WorldSaveInfo saveInfo = loadSaveInfo(saveName, isMultiplayer, fileSize);
                    
                    if (saveInfo != null && saveInfo.isValid()) {
                        saves.add(saveInfo);
                    } else {
                        System.err.println("Skipping invalid save file: " + saveFile.getName());
                    }
                    
                } catch (Exception e) {
                    System.err.println("Error processing save file " + saveFile.getName() + ": " + e.getMessage());
                }
            }
            
            // Sort by timestamp (newest first)
            saves.sort((a, b) -> b.compareByTimestamp(a));
            
            System.out.println("Found " + saves.size() + " valid save files in " + 
                             (isMultiplayer ? "multiplayer" : "singleplayer") + " directory");
            
        } catch (Exception e) {
            System.err.println("Error listing available saves: " + e.getMessage());
        }
        
        return saves;
    }
    
    /**
     * Loads save metadata without loading the full save data.
     * This is more efficient for listing saves.
     * 
     * @param saveName The name of the save
     * @param isMultiplayer true for multiplayer saves, false for singleplayer
     * @param fileSize The size of the save file in bytes
     * @return WorldSaveInfo with metadata, or null if loading failed
     */
    private static WorldSaveInfo loadSaveInfo(String saveName, boolean isMultiplayer, long fileSize) {
        try {
            File saveFile = getSaveFile(saveName, isMultiplayer);
            
            if (!saveFile.exists() || !saveFile.canRead()) {
                return null;
            }
            
            // Load the full save data to extract metadata
            // In a more optimized implementation, we could store metadata separately
            WorldSaveData saveData = loadWorld(saveName, isMultiplayer);
            
            if (saveData == null) {
                return null;
            }
            
            return new WorldSaveInfo(saveData, fileSize);
            
        } catch (Exception e) {
            System.err.println("Error loading save info for " + saveName + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Deletes a save file and its backup.
     * Performs proper cleanup of all associated files.
     * 
     * @param saveName The name of the save to delete
     * @param isMultiplayer true for multiplayer saves, false for singleplayer
     * @return true if deletion was successful, false otherwise
     */
    public static boolean deleteSave(String saveName, boolean isMultiplayer) {
        try {
            // Validate save name
            if (!isValidSaveName(saveName)) {
                System.err.println("Invalid save name: " + saveName);
                return false;
            }
            
            File saveFile = getSaveFile(saveName, isMultiplayer);
            File backupFile = getBackupFile(saveName, isMultiplayer);
            
            boolean success = true;
            
            // Delete main save file
            if (saveFile.exists()) {
                if (saveFile.delete()) {
                    System.out.println("Deleted save file: " + saveFile.getAbsolutePath());
                } else {
                    System.err.println("Failed to delete save file: " + saveFile.getAbsolutePath());
                    success = false;
                }
            }
            
            // Delete backup file if it exists
            if (backupFile.exists()) {
                if (backupFile.delete()) {
                    System.out.println("Deleted backup file: " + backupFile.getAbsolutePath());
                } else {
                    System.err.println("Failed to delete backup file: " + backupFile.getAbsolutePath());
                    // Don't mark as failure since main file deletion is more important
                }
            }
            
            return success;
            
        } catch (Exception e) {
            System.err.println("Error deleting save " + saveName + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if a save with the given name already exists.
     * 
     * @param saveName The name to check
     * @param isMultiplayer true for multiplayer saves, false for singleplayer
     * @return true if a save with this name exists, false otherwise
     */
    public static boolean saveExists(String saveName, boolean isMultiplayer) {
        if (!isValidSaveName(saveName)) {
            return false;
        }
        
        File saveFile = getSaveFile(saveName, isMultiplayer);
        return saveFile.exists();
    }    
    
/**
     * Validates that the save directory has sufficient disk space for a save operation.
     * 
     * @param isMultiplayer true for multiplayer saves, false for singleplayer
     * @param estimatedSizeBytes Estimated size of the save file in bytes
     * @return true if sufficient space is available, false otherwise
     */
    public static boolean hasSufficientDiskSpace(boolean isMultiplayer, long estimatedSizeBytes) {
        try {
            File saveDir = getSaveDirectory(isMultiplayer);
            
            // Create directory if it doesn't exist to check space
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
            
            long freeSpace = saveDir.getFreeSpace();
            long requiredSpace = estimatedSizeBytes + (1024 * 1024); // Add 1MB buffer
            
            if (freeSpace < requiredSpace) {
                System.err.println("Insufficient disk space. Required: " + requiredSpace + 
                                 " bytes, Available: " + freeSpace + " bytes");
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error checking disk space: " + e.getMessage());
            return false; // Assume insufficient space on error
        }
    }
    
    /**
     * Validates that the save directory is writable.
     * 
     * @param isMultiplayer true for multiplayer saves, false for singleplayer
     * @return true if directory is writable, false otherwise
     */
    public static boolean isSaveDirectoryWritable(boolean isMultiplayer) {
        try {
            File saveDir = getSaveDirectory(isMultiplayer);
            
            // Create directory if it doesn't exist
            if (!saveDir.exists()) {
                if (!saveDir.mkdirs()) {
                    System.err.println("Cannot create save directory: " + saveDir.getAbsolutePath());
                    return false;
                }
            }
            
            // Test write permissions by creating a temporary file
            File testFile = new File(saveDir, ".write_test_" + System.currentTimeMillis());
            
            try {
                if (testFile.createNewFile()) {
                    testFile.delete();
                    return true;
                } else {
                    System.err.println("Cannot create files in save directory: " + saveDir.getAbsolutePath());
                    return false;
                }
            } catch (IOException e) {
                System.err.println("Save directory is not writable: " + e.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Error checking save directory permissions: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validates the integrity of a save file without fully loading it.
     * Performs basic checks to ensure the file is not corrupted.
     * 
     * @param saveName The name of the save to validate
     * @param isMultiplayer true for multiplayer saves, false for singleplayer
     * @return true if the save file appears to be valid, false otherwise
     */
    public static boolean validateSaveFileIntegrity(String saveName, boolean isMultiplayer) {
        try {
            File saveFile = getSaveFile(saveName, isMultiplayer);
            
            // Check if file exists and is readable
            if (!saveFile.exists() || !saveFile.canRead()) {
                return false;
            }
            
            // Check file size (should be at least a few bytes for serialized data)
            if (saveFile.length() < 100) {
                System.err.println("Save file is too small, likely corrupted: " + saveFile.getAbsolutePath());
                return false;
            }
            
            // Try to read the file header to validate it's a proper serialized object
            try (FileInputStream fis = new FileInputStream(saveFile);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                
                // Try to read the object without fully deserializing
                Object obj = ois.readObject();
                
                if (!(obj instanceof WorldSaveData)) {
                    System.err.println("Save file does not contain WorldSaveData: " + saveFile.getAbsolutePath());
                    return false;
                }
                
                WorldSaveData saveData = (WorldSaveData) obj;
                
                // Perform basic validation
                if (!saveData.isValid()) {
                    System.err.println("Save data failed validation: " + saveFile.getAbsolutePath());
                    return false;
                }
                
                return true;
                
            } catch (ClassNotFoundException e) {
                System.err.println("Save file format not recognized: " + e.getMessage());
                return false;
            } catch (IOException e) {
                System.err.println("Save file appears to be corrupted: " + e.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Error validating save file integrity: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Attempts to restore a save file from its backup.
     * Used when the main save file is corrupted but a backup exists.
     * 
     * @param saveName The name of the save to restore
     * @param isMultiplayer true for multiplayer saves, false for singleplayer
     * @return true if restoration was successful, false otherwise
     */
    public static boolean restoreFromBackup(String saveName, boolean isMultiplayer) {
        try {
            File saveFile = getSaveFile(saveName, isMultiplayer);
            File backupFile = getBackupFile(saveName, isMultiplayer);
            
            // Check if backup exists
            if (!backupFile.exists()) {
                System.err.println("No backup file found for save: " + saveName);
                return false;
            }
            
            // Validate backup integrity
            if (!validateBackupFile(backupFile)) {
                System.err.println("Backup file is also corrupted: " + backupFile.getAbsolutePath());
                return false;
            }
            
            // Copy backup to main save file
            try {
                Files.copy(backupFile.toPath(), saveFile.toPath(), 
                          java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                System.out.println("Successfully restored save from backup: " + saveName);
                return true;
                
            } catch (IOException e) {
                System.err.println("Failed to restore from backup: " + e.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Error restoring from backup: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validates a backup file's integrity.
     * 
     * @param backupFile The backup file to validate
     * @return true if the backup file is valid, false otherwise
     */
    private static boolean validateBackupFile(File backupFile) {
        try {
            // Check file size
            if (backupFile.length() < 100) {
                return false;
            }
            
            // Try to read the backup file
            try (FileInputStream fis = new FileInputStream(backupFile);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                
                Object obj = ois.readObject();
                
                if (!(obj instanceof WorldSaveData)) {
                    return false;
                }
                
                WorldSaveData saveData = (WorldSaveData) obj;
                return saveData.isValid();
                
            } catch (Exception e) {
                return false;
            }
            
        } catch (Exception e) {
            return false;
        }
    }
}