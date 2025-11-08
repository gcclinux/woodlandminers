package wagemaker.uk.client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Manages player-specific configuration settings including the last connected server address.
 * Configuration is persisted to $HOME/.config/woodlanders/woodlanders.json.
 */
public class PlayerConfig {
    private String lastServer;
    
    /**
     * Private constructor. Use load() to create instances.
     */
    private PlayerConfig() {
        this.lastServer = null;
    }
    
    /**
     * Gets the configuration directory based on the operating system.
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
     * Gets the configuration file path.
     * 
     * @return The configuration file
     */
    private static File getConfigFile() {
        return new File(getConfigDirectory(), "woodlanders.json");
    }
    
    /**
     * Loads the player configuration from disk.
     * If the configuration file doesn't exist or cannot be read, returns a default configuration.
     * This method also handles migration from the old player.properties format.
     * This method never throws exceptions.
     * 
     * @return PlayerConfig instance with loaded or default values
     */
    public static PlayerConfig load() {
        PlayerConfig config = new PlayerConfig();
        
        try {
            File configFile = getConfigFile();
            
            if (!configFile.exists()) {
                // Try to migrate from old player.properties file
                config = migrateFromOldFormat();
                if (config.lastServer != null) {
                    // Save to new format
                    config.save();
                    System.out.println("Migrated configuration from player.properties to " + configFile.getAbsolutePath());
                } else {
                    System.out.println("Player config file not found. Using defaults.");
                }
                return config;
            }
            
            // Read the JSON file
            String jsonContent = new String(Files.readAllBytes(Paths.get(configFile.getAbsolutePath())));
            
            // Parse the lastServer field
            config.lastServer = parseJsonString(jsonContent, "\"lastServer\":");
            
            System.out.println("Player configuration loaded successfully from " + configFile.getAbsolutePath());
            if (config.lastServer != null) {
                System.out.println("Last server: " + config.lastServer);
            }
            
        } catch (IOException e) {
            // File doesn't exist or cannot be read - return default config
            System.out.println("Player config file not found or cannot be read. Using defaults. Reason: " + e.getMessage());
        } catch (Exception e) {
            // Catch any other unexpected exceptions to ensure load() never throws
            System.err.println("Unexpected error loading player config. Using defaults. Reason: " + e.getMessage());
        }
        
        return config;
    }
    
    /**
     * Migrates configuration from the old player.properties format.
     * 
     * @return PlayerConfig with migrated data, or empty config if migration fails
     */
    private static PlayerConfig migrateFromOldFormat() {
        PlayerConfig config = new PlayerConfig();
        
        try {
            File oldConfigFile = new File("player.properties");
            if (!oldConfigFile.exists()) {
                return config;
            }
            
            // Read the old properties file
            java.util.Properties properties = new java.util.Properties();
            try (java.io.FileInputStream fis = new java.io.FileInputStream(oldConfigFile)) {
                properties.load(fis);
            }
            
            // Get the last server value
            String lastServer = properties.getProperty("multiplayer.last-server");
            if (lastServer != null && !lastServer.isEmpty()) {
                config.lastServer = lastServer;
                System.out.println("Migrated last server from player.properties: " + lastServer);
            }
            
        } catch (Exception e) {
            System.err.println("Failed to migrate from player.properties: " + e.getMessage());
        }
        
        return config;
    }
    
    /**
     * Parses a string value from JSON content.
     * 
     * @param json The JSON content
     * @param key The key to search for
     * @return The parsed string value, or null if not found
     */
    private static String parseJsonString(String json, String key) {
        int keyIndex = json.indexOf(key);
        if (keyIndex == -1) {
            return null; // Key not found
        }
        
        // Find the start of the value (after the colon and any whitespace)
        int valueStart = keyIndex + key.length();
        while (valueStart < json.length() && (json.charAt(valueStart) == ' ' || json.charAt(valueStart) == '\t')) {
            valueStart++;
        }
        
        // Skip opening quote
        if (valueStart < json.length() && json.charAt(valueStart) == '"') {
            valueStart++;
        }
        
        // Find the end of the value (closing quote)
        int valueEnd = valueStart;
        while (valueEnd < json.length() && json.charAt(valueEnd) != '"') {
            valueEnd++;
        }
        
        if (valueEnd > valueStart) {
            return json.substring(valueStart, valueEnd);
        }
        
        return null;
    }
    
    /**
     * Saves the current configuration to disk.
     * If save fails, logs an error but doesn't throw an exception.
     */
    public void save() {
        try {
            File configDir = getConfigDirectory();
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
            
            File configFile = getConfigFile();
            
            // Build JSON content, preserving existing fields if the file exists
            String jsonContent;
            
            if (configFile.exists()) {
                // Read existing content
                String existingContent = new String(Files.readAllBytes(Paths.get(configFile.getAbsolutePath())));
                
                // Update or add the lastServer field
                jsonContent = updateJsonField(existingContent, "lastServer", lastServer);
            } else {
                // Create new JSON with just the lastServer field
                jsonContent = buildJsonContent();
            }
            
            // Write to file
            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(jsonContent);
            }
            
            System.out.println("Player configuration saved successfully to " + configFile.getAbsolutePath());
            
        } catch (IOException e) {
            System.err.println("Failed to save player configuration: " + e.getMessage());
            System.err.println("Configuration changes will not persist. Check file permissions and disk space.");
        } catch (Exception e) {
            // Catch any other unexpected exceptions to ensure save() never crashes the application
            System.err.println("Unexpected error saving player configuration: " + e.getMessage());
        }
    }
    
    /**
     * Builds JSON content with the current configuration.
     * 
     * @return The JSON content string
     */
    private String buildJsonContent() {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        
        if (lastServer != null && !lastServer.isEmpty()) {
            json.append("  \"lastServer\": \"").append(lastServer).append("\"\n");
        }
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * Updates a field in existing JSON content.
     * 
     * @param existingJson The existing JSON content
     * @param fieldName The field name to update
     * @param value The new value
     * @return The updated JSON content
     */
    private String updateJsonField(String existingJson, String fieldName, String value) {
        String key = "\"" + fieldName + "\":";
        int keyIndex = existingJson.indexOf(key);
        
        if (keyIndex != -1) {
            // Field exists, update it
            int valueStart = keyIndex + key.length();
            
            // Skip whitespace
            while (valueStart < existingJson.length() && 
                   (existingJson.charAt(valueStart) == ' ' || existingJson.charAt(valueStart) == '\t')) {
                valueStart++;
            }
            
            // Skip opening quote
            if (valueStart < existingJson.length() && existingJson.charAt(valueStart) == '"') {
                valueStart++;
            }
            
            // Find closing quote
            int valueEnd = valueStart;
            while (valueEnd < existingJson.length() && existingJson.charAt(valueEnd) != '"') {
                valueEnd++;
            }
            
            // Replace the value
            String before = existingJson.substring(0, valueStart);
            String after = existingJson.substring(valueEnd);
            
            if (value == null || value.isEmpty()) {
                // Remove the field entirely
                // Find the start of the line
                int lineStart = keyIndex;
                while (lineStart > 0 && existingJson.charAt(lineStart - 1) != '\n') {
                    lineStart--;
                }
                
                // Find the end of the line (including comma if present)
                int lineEnd = valueEnd + 1; // Skip closing quote
                while (lineEnd < existingJson.length() && 
                       (existingJson.charAt(lineEnd) == ',' || existingJson.charAt(lineEnd) == ' ' || 
                        existingJson.charAt(lineEnd) == '\t')) {
                    lineEnd++;
                }
                if (lineEnd < existingJson.length() && existingJson.charAt(lineEnd) == '\n') {
                    lineEnd++;
                }
                
                return existingJson.substring(0, lineStart) + existingJson.substring(lineEnd);
            } else {
                return before + value + after;
            }
        } else {
            // Field doesn't exist, add it
            if (value == null || value.isEmpty()) {
                return existingJson; // Don't add empty field
            }
            
            // Find the last closing brace
            int lastBrace = existingJson.lastIndexOf('}');
            if (lastBrace == -1) {
                // Invalid JSON, rebuild from scratch
                return buildJsonContent();
            }
            
            // Check if there are other fields (look for content before the closing brace)
            String beforeBrace = existingJson.substring(0, lastBrace).trim();
            boolean hasOtherFields = beforeBrace.length() > 1 && !beforeBrace.endsWith("{");
            
            String newField = (hasOtherFields ? ",\n" : "") + 
                            "  \"" + fieldName + "\": \"" + value + "\"\n";
            
            return existingJson.substring(0, lastBrace) + newField + "}";
        }
    }
    
    /**
     * Retrieves the last successfully connected server address.
     * 
     * @return The server address, or null if not set
     */
    public String getLastServer() {
        return lastServer;
    }
    
    /**
     * Saves the server address and persists it to disk.
     * 
     * @param address The server address to save (can be null to clear)
     */
    public void saveLastServer(String address) {
        try {
            if (address == null || address.trim().isEmpty()) {
                this.lastServer = null;
                System.out.println("Cleared last server address from configuration");
            } else {
                this.lastServer = address.trim();
                System.out.println("Saving last server address: " + address.trim());
            }
            save();
        } catch (Exception e) {
            // Catch any unexpected exceptions to ensure this method never crashes the application
            System.err.println("Unexpected error saving server address: " + e.getMessage());
        }
    }
}
