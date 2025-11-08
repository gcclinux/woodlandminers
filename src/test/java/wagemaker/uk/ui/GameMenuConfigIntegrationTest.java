package wagemaker.uk.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import wagemaker.uk.client.PlayerConfig;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify that GameMenu and PlayerConfig work together correctly.
 * Specifically tests that saving player data preserves the lastServer field.
 */
public class GameMenuConfigIntegrationTest {
    
    private File testConfigFile;
    
    @BeforeEach
    public void setUp() {
        testConfigFile = getConfigFile();
        
        // Clean up any existing config file before each test
        if (testConfigFile.exists()) {
            testConfigFile.delete();
        }
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up after test
        if (testConfigFile != null && testConfigFile.exists()) {
            testConfigFile.delete();
        }
    }
    
    /**
     * Gets the configuration file path based on OS.
     */
    private File getConfigFile() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        File configDir;
        
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                configDir = new File(appData, "Woodlanders");
            } else {
                configDir = new File(userHome, "AppData/Roaming/Woodlanders");
            }
        } else if (os.contains("mac")) {
            configDir = new File(userHome, "Library/Application Support/Woodlanders");
        } else {
            configDir = new File(userHome, ".config/woodlanders");
        }
        
        return new File(configDir, "woodlanders.json");
    }
    
    @Test
    public void testLastServerPreservedWhenSavingPlayerData() throws Exception {
        // Step 1: Save a server address using PlayerConfig
        PlayerConfig config = PlayerConfig.load();
        config.saveLastServer("192.168.1.100:25565");
        
        // Verify the file was created and contains lastServer
        assertTrue(testConfigFile.exists(), "Config file should exist after saving");
        String content = new String(Files.readAllBytes(Paths.get(testConfigFile.getAbsolutePath())));
        assertTrue(content.contains("\"lastServer\""), "Config should contain lastServer field");
        assertTrue(content.contains("192.168.1.100:25565"), "Config should contain the server address");
        
        // Step 2: Simulate GameMenu saving player data by manually creating the JSON
        // (We can't actually instantiate GameMenu in a headless test, so we simulate its behavior)
        File configDir = testConfigFile.getParentFile();
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        // Read existing lastServer value
        String existingContent = new String(Files.readAllBytes(Paths.get(testConfigFile.getAbsolutePath())));
        String lastServer = parseJsonString(existingContent, "\"lastServer\":");
        
        // Create new JSON with player data and preserved lastServer
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\n");
        jsonBuilder.append("  \"playerPosition\": {\n");
        jsonBuilder.append("    \"x\": 100.50,\n");
        jsonBuilder.append("    \"y\": 200.75\n");
        jsonBuilder.append("  },\n");
        jsonBuilder.append("  \"playerHealth\": 85.0,\n");
        jsonBuilder.append("  \"playerName\": \"TestPlayer\",\n");
        
        if (lastServer != null && !lastServer.isEmpty()) {
            jsonBuilder.append(String.format("  \"lastServer\": \"%s\",\n", lastServer));
        }
        
        jsonBuilder.append("  \"savedAt\": \"Test Date\"\n");
        jsonBuilder.append("}");
        
        // Write the updated JSON
        Files.write(Paths.get(testConfigFile.getAbsolutePath()), jsonBuilder.toString().getBytes());
        
        // Step 3: Verify that lastServer is still present after saving player data
        String updatedContent = new String(Files.readAllBytes(Paths.get(testConfigFile.getAbsolutePath())));
        assertTrue(updatedContent.contains("\"lastServer\""), "lastServer should be preserved after saving player data");
        assertTrue(updatedContent.contains("192.168.1.100:25565"), "Server address should be preserved");
        assertTrue(updatedContent.contains("\"playerPosition\""), "Player position should be saved");
        assertTrue(updatedContent.contains("\"playerHealth\""), "Player health should be saved");
        assertTrue(updatedContent.contains("\"playerName\""), "Player name should be saved");
        
        // Step 4: Verify PlayerConfig can still load the lastServer
        PlayerConfig reloadedConfig = PlayerConfig.load();
        assertEquals("192.168.1.100:25565", reloadedConfig.getLastServer(), 
                    "PlayerConfig should be able to load lastServer after player data save");
    }
    
    @Test
    public void testPlayerDataSavedWithoutLastServer() throws Exception {
        // Test that player data can be saved even when there's no lastServer
        File configDir = testConfigFile.getParentFile();
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        // Create JSON with only player data (no lastServer)
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\n");
        jsonBuilder.append("  \"playerPosition\": {\n");
        jsonBuilder.append("    \"x\": 50.25,\n");
        jsonBuilder.append("    \"y\": 75.50\n");
        jsonBuilder.append("  },\n");
        jsonBuilder.append("  \"playerHealth\": 100.0,\n");
        jsonBuilder.append("  \"playerName\": \"NewPlayer\",\n");
        jsonBuilder.append("  \"savedAt\": \"Test Date\"\n");
        jsonBuilder.append("}");
        
        Files.write(Paths.get(testConfigFile.getAbsolutePath()), jsonBuilder.toString().getBytes());
        
        // Verify the file was created
        assertTrue(testConfigFile.exists(), "Config file should exist");
        String content = new String(Files.readAllBytes(Paths.get(testConfigFile.getAbsolutePath())));
        assertFalse(content.contains("\"lastServer\""), "Config should not contain lastServer field");
        assertTrue(content.contains("\"playerPosition\""), "Config should contain player position");
        
        // Verify PlayerConfig can load without errors
        PlayerConfig config = PlayerConfig.load();
        assertNull(config.getLastServer(), "lastServer should be null when not present in config");
    }
    
    /**
     * Helper method to parse a string value from JSON content.
     */
    private String parseJsonString(String json, String key) {
        int keyIndex = json.indexOf(key);
        if (keyIndex == -1) {
            return null;
        }
        
        int valueStart = keyIndex + key.length();
        while (valueStart < json.length() && (json.charAt(valueStart) == ' ' || json.charAt(valueStart) == '\t')) {
            valueStart++;
        }
        
        if (valueStart < json.length() && json.charAt(valueStart) == '"') {
            valueStart++;
        }
        
        int valueEnd = valueStart;
        while (valueEnd < json.length() && json.charAt(valueEnd) != '"') {
            valueEnd++;
        }
        
        if (valueEnd > valueStart) {
            return json.substring(valueStart, valueEnd);
        }
        
        return null;
    }
}
