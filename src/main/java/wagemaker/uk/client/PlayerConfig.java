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
    private Float compassTargetX;
    private Float compassTargetY;
    
    /**
     * Private constructor. Use load() to create instances.
     */
    private PlayerConfig() {
        this.lastServer = null;
        this.compassTargetX = null;
        this.compassTargetY = null;
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
            
            // Parse the compassTarget fields
            String compassTargetXStr = parseJsonNumber(jsonContent, "\"compassTarget\":", "\"x\":");
            String compassTargetYStr = parseJsonNumber(jsonContent, "\"compassTarget\":", "\"y\":");
            
            if (compassTargetXStr != null) {
                try {
                    config.compassTargetX = Float.parseFloat(compassTargetXStr);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid compassTarget.x value: " + compassTargetXStr);
                }
            }
            
            if (compassTargetYStr != null) {
                try {
                    config.compassTargetY = Float.parseFloat(compassTargetYStr);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid compassTarget.y value: " + compassTargetYStr);
                }
            }
            
            System.out.println("Player configuration loaded successfully from " + configFile.getAbsolutePath());
            if (config.lastServer != null) {
                System.out.println("Last server: " + config.lastServer);
            }
            if (config.compassTargetX != null && config.compassTargetY != null) {
                System.out.println("Compass target: (" + config.compassTargetX + ", " + config.compassTargetY + ")");
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
     * Parses a number value from a nested JSON object.
     * 
     * @param json The JSON content
     * @param parentKey The parent object key to search for
     * @param childKey The child key within the parent object
     * @return The parsed number value as a string, or null if not found
     */
    private static String parseJsonNumber(String json, String parentKey, String childKey) {
        int parentIndex = json.indexOf(parentKey);
        if (parentIndex == -1) {
            return null; // Parent key not found
        }
        
        // Find the opening brace of the parent object
        int objectStart = parentIndex + parentKey.length();
        while (objectStart < json.length() && json.charAt(objectStart) != '{') {
            objectStart++;
        }
        
        if (objectStart >= json.length()) {
            return null; // No opening brace found
        }
        
        // Find the closing brace of the parent object
        int objectEnd = objectStart + 1;
        int braceCount = 1;
        while (objectEnd < json.length() && braceCount > 0) {
            if (json.charAt(objectEnd) == '{') {
                braceCount++;
            } else if (json.charAt(objectEnd) == '}') {
                braceCount--;
            }
            objectEnd++;
        }
        
        // Extract the parent object content
        String objectContent = json.substring(objectStart, objectEnd);
        
        // Find the child key within the object
        int childIndex = objectContent.indexOf(childKey);
        if (childIndex == -1) {
            return null; // Child key not found
        }
        
        // Find the start of the value (after the colon and any whitespace)
        int valueStart = childIndex + childKey.length();
        while (valueStart < objectContent.length() && 
               (objectContent.charAt(valueStart) == ' ' || 
                objectContent.charAt(valueStart) == '\t' || 
                objectContent.charAt(valueStart) == ':')) {
            valueStart++;
        }
        
        // Find the end of the value (comma, closing brace, or newline)
        int valueEnd = valueStart;
        while (valueEnd < objectContent.length() && 
               objectContent.charAt(valueEnd) != ',' && 
               objectContent.charAt(valueEnd) != '}' && 
               objectContent.charAt(valueEnd) != '\n' &&
               objectContent.charAt(valueEnd) != '\r') {
            valueEnd++;
        }
        
        if (valueEnd > valueStart) {
            return objectContent.substring(valueStart, valueEnd).trim();
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
                
                // Update or add the compassTarget object
                jsonContent = updateCompassTargetField(jsonContent);
            } else {
                // Create new JSON with all fields
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
        
        boolean hasFields = false;
        
        if (lastServer != null && !lastServer.isEmpty()) {
            json.append("  \"lastServer\": \"").append(lastServer).append("\"");
            hasFields = true;
        }
        
        if (compassTargetX != null && compassTargetY != null) {
            if (hasFields) {
                json.append(",\n");
            }
            json.append("  \"compassTarget\": {\n");
            json.append("    \"x\": ").append(compassTargetX).append(",\n");
            json.append("    \"y\": ").append(compassTargetY).append("\n");
            json.append("  }");
            hasFields = true;
        }
        
        if (hasFields) {
            json.append("\n");
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
    
    /**
     * Retrieves the compass target X coordinate.
     * 
     * @return The X coordinate, or null if not set
     */
    public Float getCompassTargetX() {
        return compassTargetX;
    }
    
    /**
     * Retrieves the compass target Y coordinate.
     * 
     * @return The Y coordinate, or null if not set
     */
    public Float getCompassTargetY() {
        return compassTargetY;
    }
    
    /**
     * Saves the compass target coordinates and persists them to disk.
     * 
     * @param x The target X coordinate
     * @param y The target Y coordinate
     */
    public void saveCompassTarget(Float x, Float y) {
        try {
            this.compassTargetX = x;
            this.compassTargetY = y;
            save();
            if (x != null && y != null) {
                System.out.println("Saved compass target: (" + x + ", " + y + ")");
            }
        } catch (Exception e) {
            // Catch any unexpected exceptions to ensure this method never crashes the application
            System.err.println("Unexpected error saving compass target: " + e.getMessage());
        }
    }
    
    /**
     * Clears the compass target coordinates and persists the change to disk.
     */
    public void clearCompassTarget() {
        try {
            this.compassTargetX = null;
            this.compassTargetY = null;
            save();
            System.out.println("Cleared compass target from configuration");
        } catch (Exception e) {
            // Catch any unexpected exceptions to ensure this method never crashes the application
            System.err.println("Unexpected error clearing compass target: " + e.getMessage());
        }
    }
    
    /**
     * Updates the compassTarget nested object in existing JSON content.
     * 
     * @param existingJson The existing JSON content
     * @return The updated JSON content
     */
    private String updateCompassTargetField(String existingJson) {
        String key = "\"compassTarget\":";
        int keyIndex = existingJson.indexOf(key);
        
        if (keyIndex != -1) {
            // compassTarget object exists, need to replace it
            int objectStart = keyIndex + key.length();
            
            // Skip whitespace to find opening brace
            while (objectStart < existingJson.length() && 
                   (existingJson.charAt(objectStart) == ' ' || existingJson.charAt(objectStart) == '\t')) {
                objectStart++;
            }
            
            if (objectStart < existingJson.length() && existingJson.charAt(objectStart) == '{') {
                // Find the closing brace
                int objectEnd = objectStart + 1;
                int braceCount = 1;
                while (objectEnd < existingJson.length() && braceCount > 0) {
                    if (existingJson.charAt(objectEnd) == '{') {
                        braceCount++;
                    } else if (existingJson.charAt(objectEnd) == '}') {
                        braceCount--;
                    }
                    objectEnd++;
                }
                
                if (compassTargetX == null || compassTargetY == null) {
                    // Remove the compassTarget field entirely
                    // Find the start of the line
                    int lineStart = keyIndex;
                    while (lineStart > 0 && existingJson.charAt(lineStart - 1) != '\n') {
                        lineStart--;
                    }
                    
                    // Find the end (including comma if present)
                    int lineEnd = objectEnd;
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
                    // Replace the object
                    String newObject = "{\n    \"x\": " + compassTargetX + ",\n    \"y\": " + compassTargetY + "\n  }";
                    return existingJson.substring(0, objectStart) + newObject + existingJson.substring(objectEnd);
                }
            }
        } else {
            // compassTarget doesn't exist, add it if values are not null
            if (compassTargetX == null || compassTargetY == null) {
                return existingJson; // Don't add if values are null
            }
            
            // Find the last closing brace
            int lastBrace = existingJson.lastIndexOf('}');
            if (lastBrace == -1) {
                // Invalid JSON, rebuild from scratch
                return buildJsonContent();
            }
            
            // Check if there are other fields
            String beforeBrace = existingJson.substring(0, lastBrace).trim();
            boolean hasOtherFields = beforeBrace.length() > 1 && !beforeBrace.endsWith("{");
            
            String newField = (hasOtherFields ? ",\n" : "") + 
                            "  \"compassTarget\": {\n" +
                            "    \"x\": " + compassTargetX + ",\n" +
                            "    \"y\": " + compassTargetY + "\n" +
                            "  }\n";
            
            return existingJson.substring(0, lastBrace) + newField + "}";
        }
        
        return existingJson;
    }
}
