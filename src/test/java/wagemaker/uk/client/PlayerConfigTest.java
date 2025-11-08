package wagemaker.uk.client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for PlayerConfig error handling and validation.
 */
public class PlayerConfigTest {
    
    private File testConfigDir;
    private File testConfigFile;
    
    @BeforeEach
    public void setUp() {
        // Create a temporary test config directory
        String userHome = System.getProperty("user.home");
        testConfigDir = new File(userHome, ".config/woodlanders-test");
        testConfigFile = new File(testConfigDir, "woodlanders.json");
        
        // Clean up any existing config file before each test
        deleteConfigFile();
        
        // Override the config directory for testing
        // Note: This would require modifying PlayerConfig to support test mode
        // For now, we'll use the actual config directory
        testConfigDir = getConfigDirectory();
        testConfigFile = new File(testConfigDir, "woodlanders.json");
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up config file after each test
        deleteConfigFile();
    }
    
    private File getConfigDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                return new File(appData, "Woodlanders");
            } else {
                return new File(userHome, "AppData/Roaming/Woodlanders");
            }
        } else if (os.contains("mac")) {
            return new File(userHome, "Library/Application Support/Woodlanders");
        } else {
            return new File(userHome, ".config/woodlanders");
        }
    }
    
    private void deleteConfigFile() {
        if (testConfigFile != null && testConfigFile.exists()) {
            testConfigFile.delete();
        }
    }
    
    @Test
    public void testLoadNonExistentFile() {
        // Test that loading a non-existent file returns default config without throwing
        PlayerConfig config = PlayerConfig.load();
        assertNotNull(config, "Config should not be null");
        assertNull(config.getLastServer(), "Last server should be null for default config");
    }
    
    @Test
    public void testLoadCorruptedFile() throws IOException {
        // Ensure config directory exists
        if (!testConfigDir.exists()) {
            testConfigDir.mkdirs();
        }
        
        // Create a corrupted JSON file
        try (FileWriter writer = new FileWriter(testConfigFile)) {
            writer.write("This is not valid JSON\n");
            writer.write("{ broken syntax }\n");
        }
        
        // Test that loading corrupted file returns default config without throwing
        PlayerConfig config = PlayerConfig.load();
        assertNotNull(config, "Config should not be null even with corrupted file");
        assertNull(config.getLastServer(), "Last server should be null for corrupted config");
    }
    
    @Test
    public void testSaveAndLoadRoundTrip() {
        // Test normal save and load cycle
        PlayerConfig config = PlayerConfig.load();
        config.saveLastServer("192.168.1.100:25565");
        
        // Load again and verify
        PlayerConfig loadedConfig = PlayerConfig.load();
        assertEquals("192.168.1.100:25565", loadedConfig.getLastServer(), 
                     "Loaded server address should match saved address");
    }
    
    @Test
    public void testSaveNullAddress() {
        // First save a valid address
        PlayerConfig config = PlayerConfig.load();
        config.saveLastServer("192.168.1.100:25565");
        
        // Then save null to clear it
        config.saveLastServer(null);
        
        // Load and verify it's cleared
        PlayerConfig loadedConfig = PlayerConfig.load();
        assertNull(loadedConfig.getLastServer(), "Server address should be null after saving null");
    }
    
    @Test
    public void testSaveEmptyAddress() {
        // First save a valid address
        PlayerConfig config = PlayerConfig.load();
        config.saveLastServer("192.168.1.100:25565");
        
        // Then save empty string to clear it
        config.saveLastServer("");
        
        // Load and verify it's cleared
        PlayerConfig loadedConfig = PlayerConfig.load();
        assertNull(loadedConfig.getLastServer(), "Server address should be null after saving empty string");
    }
    
    @Test
    public void testSaveAddressWithWhitespace() {
        // Test that whitespace is trimmed
        PlayerConfig config = PlayerConfig.load();
        config.saveLastServer("  192.168.1.100:25565  ");
        
        // Load and verify whitespace was trimmed
        PlayerConfig loadedConfig = PlayerConfig.load();
        assertEquals("192.168.1.100:25565", loadedConfig.getLastServer(), 
                     "Server address should be trimmed");
    }
}
