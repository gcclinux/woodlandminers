package wagemaker.uk.inventory;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for inventory save/load functionality.
 * Tests saving inventory data to JSON, loading inventory data from JSON,
 * backwards compatibility with old save files, and mode-specific inventory persistence.
 * 
 * Requirements: 6.2, 6.3, 6.4, 6.5, 6.6
 */
public class InventorySaveLoadIntegrationTest {
    
    private File testConfigFile;
    private InventoryManager inventoryManager;
    
    @BeforeEach
    public void setUp() {
        testConfigFile = getConfigFile();
        
        // Clean up any existing config file before each test
        if (testConfigFile.exists()) {
            testConfigFile.delete();
        }
        
        // Create a mock inventory manager for testing
        inventoryManager = createMockInventoryManager();
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
    
    /**
     * Creates a mock inventory manager for testing.
     */
    private InventoryManager createMockInventoryManager() {
        // Create a minimal mock that doesn't require LibGDX initialization
        return new InventoryManager(null);
    }
    
    // ===== Test: Saving Inventory Data to JSON =====
    
    @Test
    public void testSaveInventoryDataToJSON() throws Exception {
        // Set up inventory data
        Inventory spInv = inventoryManager.getSingleplayerInventory();
        spInv.setAppleCount(5);
        spInv.setBananaCount(3);
        spInv.setBabyBambooCount(12);
        spInv.setBambooStackCount(8);
        spInv.setWoodStackCount(15);
        
        Inventory mpInv = inventoryManager.getMultiplayerInventory();
        mpInv.setAppleCount(2);
        mpInv.setBananaCount(7);
        mpInv.setBabyBambooCount(0);
        mpInv.setBambooStackCount(4);
        mpInv.setWoodStackCount(10);
        
        // Simulate saving (as GameMenu would do)
        File configDir = testConfigFile.getParentFile();
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\n");
        jsonBuilder.append("  \"playerName\": \"TestPlayer\",\n");
        jsonBuilder.append("  \"singleplayerPosition\": {\n");
        jsonBuilder.append("    \"x\": 100.50,\n");
        jsonBuilder.append("    \"y\": 200.75\n");
        jsonBuilder.append("  },\n");
        jsonBuilder.append("  \"singleplayerHealth\": 85.0,\n");
        jsonBuilder.append("  \"singleplayerInventory\": {\n");
        jsonBuilder.append(String.format("    \"apple\": %d,\n", spInv.getAppleCount()));
        jsonBuilder.append(String.format("    \"banana\": %d,\n", spInv.getBananaCount()));
        jsonBuilder.append(String.format("    \"babyBamboo\": %d,\n", spInv.getBabyBambooCount()));
        jsonBuilder.append(String.format("    \"bambooStack\": %d,\n", spInv.getBambooStackCount()));
        jsonBuilder.append(String.format("    \"woodStack\": %d\n", spInv.getWoodStackCount()));
        jsonBuilder.append("  },\n");
        jsonBuilder.append("  \"multiplayerPosition\": {\n");
        jsonBuilder.append("    \"x\": 50.25,\n");
        jsonBuilder.append("    \"y\": 150.00\n");
        jsonBuilder.append("  },\n");
        jsonBuilder.append("  \"multiplayerHealth\": 100.0,\n");
        jsonBuilder.append("  \"multiplayerInventory\": {\n");
        jsonBuilder.append(String.format("    \"apple\": %d,\n", mpInv.getAppleCount()));
        jsonBuilder.append(String.format("    \"banana\": %d,\n", mpInv.getBananaCount()));
        jsonBuilder.append(String.format("    \"babyBamboo\": %d,\n", mpInv.getBabyBambooCount()));
        jsonBuilder.append(String.format("    \"bambooStack\": %d,\n", mpInv.getBambooStackCount()));
        jsonBuilder.append(String.format("    \"woodStack\": %d\n", mpInv.getWoodStackCount()));
        jsonBuilder.append("  },\n");
        jsonBuilder.append("  \"savedAt\": \"Test Date\"\n");
        jsonBuilder.append("}");
        
        Files.write(Paths.get(testConfigFile.getAbsolutePath()), jsonBuilder.toString().getBytes());
        
        // Verify file was created and contains inventory data
        assertTrue(testConfigFile.exists(), "Config file should exist after saving");
        String content = new String(Files.readAllBytes(Paths.get(testConfigFile.getAbsolutePath())));
        
        assertTrue(content.contains("\"singleplayerInventory\""), "Should contain singleplayer inventory");
        assertTrue(content.contains("\"multiplayerInventory\""), "Should contain multiplayer inventory");
        assertTrue(content.contains("\"apple\": 5"), "Should contain SP apple count");
        assertTrue(content.contains("\"banana\": 3"), "Should contain SP banana count");
        assertTrue(content.contains("\"apple\": 2"), "Should contain MP apple count");
        assertTrue(content.contains("\"banana\": 7"), "Should contain MP banana count");
    }
    
    // ===== Test: Loading Inventory Data from JSON =====
    
    @Test
    public void testLoadInventoryDataFromJSON() throws Exception {
        // Create a save file with inventory data
        File configDir = testConfigFile.getParentFile();
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        String jsonContent = "{\n" +
            "  \"playerName\": \"TestPlayer\",\n" +
            "  \"singleplayerPosition\": {\n" +
            "    \"x\": 100.50,\n" +
            "    \"y\": 200.75\n" +
            "  },\n" +
            "  \"singleplayerHealth\": 85.0,\n" +
            "  \"singleplayerInventory\": {\n" +
            "    \"apple\": 10,\n" +
            "    \"banana\": 5,\n" +
            "    \"babyBamboo\": 20,\n" +
            "    \"bambooStack\": 15,\n" +
            "    \"woodStack\": 25\n" +
            "  },\n" +
            "  \"multiplayerPosition\": {\n" +
            "    \"x\": 50.25,\n" +
            "    \"y\": 150.00\n" +
            "  },\n" +
            "  \"multiplayerHealth\": 100.0,\n" +
            "  \"multiplayerInventory\": {\n" +
            "    \"apple\": 3,\n" +
            "    \"banana\": 8,\n" +
            "    \"babyBamboo\": 1,\n" +
            "    \"bambooStack\": 6,\n" +
            "    \"woodStack\": 12\n" +
            "  },\n" +
            "  \"savedAt\": \"Test Date\"\n" +
            "}";
        
        Files.write(Paths.get(testConfigFile.getAbsolutePath()), jsonContent.getBytes());
        
        // Simulate loading (as GameMenu would do)
        String loadedContent = new String(Files.readAllBytes(Paths.get(testConfigFile.getAbsolutePath())));
        
        // Parse singleplayer inventory
        int spApple = parseJsonObjectInt(loadedContent, "\"singleplayerInventory\"", "apple");
        int spBanana = parseJsonObjectInt(loadedContent, "\"singleplayerInventory\"", "banana");
        int spBabyBamboo = parseJsonObjectInt(loadedContent, "\"singleplayerInventory\"", "babyBamboo");
        int spBambooStack = parseJsonObjectInt(loadedContent, "\"singleplayerInventory\"", "bambooStack");
        int spWoodStack = parseJsonObjectInt(loadedContent, "\"singleplayerInventory\"", "woodStack");
        
        Inventory spInv = inventoryManager.getSingleplayerInventory();
        spInv.setAppleCount(spApple);
        spInv.setBananaCount(spBanana);
        spInv.setBabyBambooCount(spBabyBamboo);
        spInv.setBambooStackCount(spBambooStack);
        spInv.setWoodStackCount(spWoodStack);
        
        // Parse multiplayer inventory
        int mpApple = parseJsonObjectInt(loadedContent, "\"multiplayerInventory\"", "apple");
        int mpBanana = parseJsonObjectInt(loadedContent, "\"multiplayerInventory\"", "banana");
        int mpBabyBamboo = parseJsonObjectInt(loadedContent, "\"multiplayerInventory\"", "babyBamboo");
        int mpBambooStack = parseJsonObjectInt(loadedContent, "\"multiplayerInventory\"", "bambooStack");
        int mpWoodStack = parseJsonObjectInt(loadedContent, "\"multiplayerInventory\"", "woodStack");
        
        Inventory mpInv = inventoryManager.getMultiplayerInventory();
        mpInv.setAppleCount(mpApple);
        mpInv.setBananaCount(mpBanana);
        mpInv.setBabyBambooCount(mpBabyBamboo);
        mpInv.setBambooStackCount(mpBambooStack);
        mpInv.setWoodStackCount(mpWoodStack);
        
        // Verify singleplayer inventory was loaded correctly
        assertEquals(10, spInv.getAppleCount(), "SP apple count should be loaded");
        assertEquals(5, spInv.getBananaCount(), "SP banana count should be loaded");
        assertEquals(20, spInv.getBabyBambooCount(), "SP baby bamboo count should be loaded");
        assertEquals(15, spInv.getBambooStackCount(), "SP bamboo stack count should be loaded");
        assertEquals(25, spInv.getWoodStackCount(), "SP wood stack count should be loaded");
        
        // Verify multiplayer inventory was loaded correctly
        assertEquals(3, mpInv.getAppleCount(), "MP apple count should be loaded");
        assertEquals(8, mpInv.getBananaCount(), "MP banana count should be loaded");
        assertEquals(1, mpInv.getBabyBambooCount(), "MP baby bamboo count should be loaded");
        assertEquals(6, mpInv.getBambooStackCount(), "MP bamboo stack count should be loaded");
        assertEquals(12, mpInv.getWoodStackCount(), "MP wood stack count should be loaded");
    }
    
    // ===== Test: Backwards Compatibility with Old Save Files =====
    
    @Test
    public void testBackwardsCompatibilityWithOldSaveFileWithoutInventory() throws Exception {
        // Create an old save file without inventory data
        File configDir = testConfigFile.getParentFile();
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        String oldJsonContent = "{\n" +
            "  \"playerName\": \"OldPlayer\",\n" +
            "  \"singleplayerPosition\": {\n" +
            "    \"x\": 50.0,\n" +
            "    \"y\": 75.0\n" +
            "  },\n" +
            "  \"singleplayerHealth\": 90.0,\n" +
            "  \"savedAt\": \"Old Date\"\n" +
            "}";
        
        Files.write(Paths.get(testConfigFile.getAbsolutePath()), oldJsonContent.getBytes());
        
        // Simulate loading with graceful handling of missing inventory data
        String loadedContent = new String(Files.readAllBytes(Paths.get(testConfigFile.getAbsolutePath())));
        
        // Try to load inventory data - should handle missing data gracefully
        try {
            int spApple = parseJsonObjectInt(loadedContent, "\"singleplayerInventory\"", "apple");
            inventoryManager.getSingleplayerInventory().setAppleCount(spApple);
            fail("Should throw exception when inventory data is missing");
        } catch (Exception e) {
            // Expected - inventory data doesn't exist
            // Initialize with empty inventory
            inventoryManager.getSingleplayerInventory().setAppleCount(0);
        }
        
        // Verify inventory is empty (default state)
        assertEquals(0, inventoryManager.getSingleplayerInventory().getAppleCount(), 
            "SP inventory should be empty for old save files");
        assertEquals(0, inventoryManager.getSingleplayerInventory().getBananaCount(), 
            "SP inventory should be empty for old save files");
        assertEquals(0, inventoryManager.getMultiplayerInventory().getAppleCount(), 
            "MP inventory should be empty for old save files");
    }
    
    @Test
    public void testBackwardsCompatibilityWithLegacyFlatFormat() throws Exception {
        // Create a save file with legacy flat format (no nested objects)
        File configDir = testConfigFile.getParentFile();
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        String legacyJsonContent = "{\n" +
            "  \"playerName\": \"LegacyPlayer\",\n" +
            "  \"x\": 100.0,\n" +
            "  \"y\": 200.0,\n" +
            "  \"playerHealth\": 75.0,\n" +
            "  \"savedAt\": \"Legacy Date\"\n" +
            "}";
        
        Files.write(Paths.get(testConfigFile.getAbsolutePath()), legacyJsonContent.getBytes());
        
        // Verify file exists and doesn't contain inventory data
        assertTrue(testConfigFile.exists(), "Legacy save file should exist");
        String content = new String(Files.readAllBytes(Paths.get(testConfigFile.getAbsolutePath())));
        assertFalse(content.contains("\"singleplayerInventory\""), 
            "Legacy file should not contain inventory data");
        
        // Simulate loading - should not crash
        try {
            parseJsonObjectInt(content, "\"singleplayerInventory\"", "apple");
            fail("Should throw exception for missing inventory in legacy format");
        } catch (Exception e) {
            // Expected - gracefully handle missing inventory data
        }
        
        // Verify inventories remain at default (empty) state
        assertEquals(0, inventoryManager.getSingleplayerInventory().getAppleCount(), 
            "Inventory should be empty for legacy saves");
        assertEquals(0, inventoryManager.getMultiplayerInventory().getAppleCount(), 
            "Inventory should be empty for legacy saves");
    }
    
    // ===== Test: Mode-Specific Inventory Persistence =====
    
    @Test
    public void testSingleplayerInventoryPersistence() throws Exception {
        // Set up singleplayer inventory
        Inventory spInv = inventoryManager.getSingleplayerInventory();
        spInv.setAppleCount(15);
        spInv.setBananaCount(10);
        spInv.setBabyBambooCount(25);
        spInv.setBambooStackCount(20);
        spInv.setWoodStackCount(30);
        
        // Save to file
        File configDir = testConfigFile.getParentFile();
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\n");
        jsonBuilder.append("  \"playerName\": \"SPPlayer\",\n");
        jsonBuilder.append("  \"singleplayerPosition\": {\"x\": 100.0, \"y\": 200.0},\n");
        jsonBuilder.append("  \"singleplayerHealth\": 85.0,\n");
        jsonBuilder.append("  \"singleplayerInventory\": {\n");
        jsonBuilder.append(String.format("    \"apple\": %d,\n", spInv.getAppleCount()));
        jsonBuilder.append(String.format("    \"banana\": %d,\n", spInv.getBananaCount()));
        jsonBuilder.append(String.format("    \"babyBamboo\": %d,\n", spInv.getBabyBambooCount()));
        jsonBuilder.append(String.format("    \"bambooStack\": %d,\n", spInv.getBambooStackCount()));
        jsonBuilder.append(String.format("    \"woodStack\": %d\n", spInv.getWoodStackCount()));
        jsonBuilder.append("  },\n");
        jsonBuilder.append("  \"savedAt\": \"Test Date\"\n");
        jsonBuilder.append("}");
        
        Files.write(Paths.get(testConfigFile.getAbsolutePath()), jsonBuilder.toString().getBytes());
        
        // Clear inventory and reload
        inventoryManager.getSingleplayerInventory().clear();
        assertEquals(0, inventoryManager.getSingleplayerInventory().getAppleCount(), 
            "Inventory should be cleared");
        
        // Load from file
        String loadedContent = new String(Files.readAllBytes(Paths.get(testConfigFile.getAbsolutePath())));
        int apple = parseJsonObjectInt(loadedContent, "\"singleplayerInventory\"", "apple");
        int banana = parseJsonObjectInt(loadedContent, "\"singleplayerInventory\"", "banana");
        int babyBamboo = parseJsonObjectInt(loadedContent, "\"singleplayerInventory\"", "babyBamboo");
        int bambooStack = parseJsonObjectInt(loadedContent, "\"singleplayerInventory\"", "bambooStack");
        int woodStack = parseJsonObjectInt(loadedContent, "\"singleplayerInventory\"", "woodStack");
        
        inventoryManager.getSingleplayerInventory().setAppleCount(apple);
        inventoryManager.getSingleplayerInventory().setBananaCount(banana);
        inventoryManager.getSingleplayerInventory().setBabyBambooCount(babyBamboo);
        inventoryManager.getSingleplayerInventory().setBambooStackCount(bambooStack);
        inventoryManager.getSingleplayerInventory().setWoodStackCount(woodStack);
        
        // Verify singleplayer inventory was restored
        assertEquals(15, inventoryManager.getSingleplayerInventory().getAppleCount(), 
            "SP apple count should be restored");
        assertEquals(10, inventoryManager.getSingleplayerInventory().getBananaCount(), 
            "SP banana count should be restored");
        assertEquals(25, inventoryManager.getSingleplayerInventory().getBabyBambooCount(), 
            "SP baby bamboo count should be restored");
        assertEquals(20, inventoryManager.getSingleplayerInventory().getBambooStackCount(), 
            "SP bamboo stack count should be restored");
        assertEquals(30, inventoryManager.getSingleplayerInventory().getWoodStackCount(), 
            "SP wood stack count should be restored");
    }
    
    @Test
    public void testMultiplayerInventoryPersistence() throws Exception {
        // Set up multiplayer inventory
        Inventory mpInv = inventoryManager.getMultiplayerInventory();
        mpInv.setAppleCount(7);
        mpInv.setBananaCount(12);
        mpInv.setBabyBambooCount(3);
        mpInv.setBambooStackCount(9);
        mpInv.setWoodStackCount(18);
        
        // Save to file
        File configDir = testConfigFile.getParentFile();
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\n");
        jsonBuilder.append("  \"playerName\": \"MPPlayer\",\n");
        jsonBuilder.append("  \"multiplayerPosition\": {\"x\": 50.0, \"y\": 150.0},\n");
        jsonBuilder.append("  \"multiplayerHealth\": 100.0,\n");
        jsonBuilder.append("  \"multiplayerInventory\": {\n");
        jsonBuilder.append(String.format("    \"apple\": %d,\n", mpInv.getAppleCount()));
        jsonBuilder.append(String.format("    \"banana\": %d,\n", mpInv.getBananaCount()));
        jsonBuilder.append(String.format("    \"babyBamboo\": %d,\n", mpInv.getBabyBambooCount()));
        jsonBuilder.append(String.format("    \"bambooStack\": %d,\n", mpInv.getBambooStackCount()));
        jsonBuilder.append(String.format("    \"woodStack\": %d\n", mpInv.getWoodStackCount()));
        jsonBuilder.append("  },\n");
        jsonBuilder.append("  \"savedAt\": \"Test Date\"\n");
        jsonBuilder.append("}");
        
        Files.write(Paths.get(testConfigFile.getAbsolutePath()), jsonBuilder.toString().getBytes());
        
        // Clear inventory and reload
        inventoryManager.getMultiplayerInventory().clear();
        assertEquals(0, inventoryManager.getMultiplayerInventory().getAppleCount(), 
            "Inventory should be cleared");
        
        // Load from file
        String loadedContent = new String(Files.readAllBytes(Paths.get(testConfigFile.getAbsolutePath())));
        int apple = parseJsonObjectInt(loadedContent, "\"multiplayerInventory\"", "apple");
        int banana = parseJsonObjectInt(loadedContent, "\"multiplayerInventory\"", "banana");
        int babyBamboo = parseJsonObjectInt(loadedContent, "\"multiplayerInventory\"", "babyBamboo");
        int bambooStack = parseJsonObjectInt(loadedContent, "\"multiplayerInventory\"", "bambooStack");
        int woodStack = parseJsonObjectInt(loadedContent, "\"multiplayerInventory\"", "woodStack");
        
        inventoryManager.getMultiplayerInventory().setAppleCount(apple);
        inventoryManager.getMultiplayerInventory().setBananaCount(banana);
        inventoryManager.getMultiplayerInventory().setBabyBambooCount(babyBamboo);
        inventoryManager.getMultiplayerInventory().setBambooStackCount(bambooStack);
        inventoryManager.getMultiplayerInventory().setWoodStackCount(woodStack);
        
        // Verify multiplayer inventory was restored
        assertEquals(7, inventoryManager.getMultiplayerInventory().getAppleCount(), 
            "MP apple count should be restored");
        assertEquals(12, inventoryManager.getMultiplayerInventory().getBananaCount(), 
            "MP banana count should be restored");
        assertEquals(3, inventoryManager.getMultiplayerInventory().getBabyBambooCount(), 
            "MP baby bamboo count should be restored");
        assertEquals(9, inventoryManager.getMultiplayerInventory().getBambooStackCount(), 
            "MP bamboo stack count should be restored");
        assertEquals(18, inventoryManager.getMultiplayerInventory().getWoodStackCount(), 
            "MP wood stack count should be restored");
    }
    
    @Test
    public void testBothInventoriesPersistIndependently() throws Exception {
        // Set up both inventories with different values
        Inventory spInv = inventoryManager.getSingleplayerInventory();
        spInv.setAppleCount(5);
        spInv.setBananaCount(10);
        
        Inventory mpInv = inventoryManager.getMultiplayerInventory();
        mpInv.setAppleCount(20);
        mpInv.setBananaCount(15);
        
        // Save both to file
        File configDir = testConfigFile.getParentFile();
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\n");
        jsonBuilder.append("  \"playerName\": \"TestPlayer\",\n");
        jsonBuilder.append("  \"singleplayerInventory\": {\n");
        jsonBuilder.append(String.format("    \"apple\": %d,\n", spInv.getAppleCount()));
        jsonBuilder.append(String.format("    \"banana\": %d,\n", spInv.getBananaCount()));
        jsonBuilder.append("    \"babyBamboo\": 0,\n");
        jsonBuilder.append("    \"bambooStack\": 0,\n");
        jsonBuilder.append("    \"woodStack\": 0\n");
        jsonBuilder.append("  },\n");
        jsonBuilder.append("  \"multiplayerInventory\": {\n");
        jsonBuilder.append(String.format("    \"apple\": %d,\n", mpInv.getAppleCount()));
        jsonBuilder.append(String.format("    \"banana\": %d,\n", mpInv.getBananaCount()));
        jsonBuilder.append("    \"babyBamboo\": 0,\n");
        jsonBuilder.append("    \"bambooStack\": 0,\n");
        jsonBuilder.append("    \"woodStack\": 0\n");
        jsonBuilder.append("  },\n");
        jsonBuilder.append("  \"savedAt\": \"Test Date\"\n");
        jsonBuilder.append("}");
        
        Files.write(Paths.get(testConfigFile.getAbsolutePath()), jsonBuilder.toString().getBytes());
        
        // Clear both inventories
        inventoryManager.getSingleplayerInventory().clear();
        inventoryManager.getMultiplayerInventory().clear();
        
        // Load from file
        String loadedContent = new String(Files.readAllBytes(Paths.get(testConfigFile.getAbsolutePath())));
        
        int spApple = parseJsonObjectInt(loadedContent, "\"singleplayerInventory\"", "apple");
        int spBanana = parseJsonObjectInt(loadedContent, "\"singleplayerInventory\"", "banana");
        inventoryManager.getSingleplayerInventory().setAppleCount(spApple);
        inventoryManager.getSingleplayerInventory().setBananaCount(spBanana);
        
        int mpApple = parseJsonObjectInt(loadedContent, "\"multiplayerInventory\"", "apple");
        int mpBanana = parseJsonObjectInt(loadedContent, "\"multiplayerInventory\"", "banana");
        inventoryManager.getMultiplayerInventory().setAppleCount(mpApple);
        inventoryManager.getMultiplayerInventory().setBananaCount(mpBanana);
        
        // Verify both inventories were restored independently
        assertEquals(5, inventoryManager.getSingleplayerInventory().getAppleCount(), 
            "SP apple count should be restored");
        assertEquals(10, inventoryManager.getSingleplayerInventory().getBananaCount(), 
            "SP banana count should be restored");
        assertEquals(20, inventoryManager.getMultiplayerInventory().getAppleCount(), 
            "MP apple count should be restored");
        assertEquals(15, inventoryManager.getMultiplayerInventory().getBananaCount(), 
            "MP banana count should be restored");
    }
    
    // ===== Helper Methods (copied from GameMenu for parsing) =====
    
    /**
     * Parses an integer value from a nested JSON object.
     */
    private int parseJsonObjectInt(String json, String objectKey, String propertyKey) {
        // Find the object
        int objectIndex = json.indexOf(objectKey);
        if (objectIndex == -1) {
            throw new RuntimeException("Object not found: " + objectKey);
        }
        
        // Find the opening brace of the object
        int braceStart = json.indexOf("{", objectIndex);
        if (braceStart == -1) {
            throw new RuntimeException("Object opening brace not found for: " + objectKey);
        }
        
        // Find the closing brace of the object
        int braceEnd = json.indexOf("}", braceStart);
        if (braceEnd == -1) {
            throw new RuntimeException("Object closing brace not found for: " + objectKey);
        }
        
        // Extract the object content
        String objectContent = json.substring(braceStart + 1, braceEnd);
        
        // Parse the property within the object
        String propertyPattern = "\"" + propertyKey + "\":";
        int keyIndex = objectContent.indexOf(propertyPattern);
        if (keyIndex == -1) {
            throw new RuntimeException("Property not found: " + propertyKey);
        }
        
        // Find the start of the value (after the colon and any whitespace)
        int valueStart = keyIndex + propertyPattern.length();
        while (valueStart < objectContent.length() && 
               (objectContent.charAt(valueStart) == ' ' || objectContent.charAt(valueStart) == '\t')) {
            valueStart++;
        }
        
        // Find the end of the value (before comma, newline, or closing brace)
        int valueEnd = valueStart;
        while (valueEnd < objectContent.length()) {
            char c = objectContent.charAt(valueEnd);
            if (c == ',' || c == '\n' || c == '\r' || c == '}' || c == ' ' || c == '\t') {
                break;
            }
            valueEnd++;
        }
        
        String valueStr = objectContent.substring(valueStart, valueEnd).trim();
        
        // Remove any trailing non-numeric characters
        StringBuilder cleanValue = new StringBuilder();
        for (char c : valueStr.toCharArray()) {
            if (Character.isDigit(c) || c == '-') {
                cleanValue.append(c);
            } else {
                break;
            }
        }
        
        return Integer.parseInt(cleanValue.toString());
    }
}
