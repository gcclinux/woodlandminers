package wagemaker.uk.world;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import wagemaker.uk.network.WorldState;
import wagemaker.uk.network.TreeState;
import wagemaker.uk.network.TreeType;
import wagemaker.uk.network.ItemState;
import wagemaker.uk.network.ItemType;
import wagemaker.uk.weather.RainZone;
import wagemaker.uk.respawn.RespawnEntry;
import wagemaker.uk.respawn.RespawnManager;
import wagemaker.uk.respawn.ResourceType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Unit tests for WorldSaveManager.
 * Tests save/load operations, file system error handling, and save name validation.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WorldSaveManagerTest {
    
    private static final String TEST_SAVE_NAME = "test-world";
    private static final String INVALID_SAVE_NAME = "../invalid/path";
    private static final long TEST_WORLD_SEED = 12345L;
    
    private WorldState testWorldState;
    private File tempTestDir;
    
    @BeforeEach
    public void setUp() throws IOException {
        // Create a temporary directory for test saves
        tempTestDir = Files.createTempDirectory("worldsave-test").toFile();
        
        // Create test world state
        testWorldState = createTestWorldState();
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test files
        if (tempTestDir != null && tempTestDir.exists()) {
            deleteDirectory(tempTestDir);
        }
        
        // Clean up any test saves in actual save directory
        cleanupTestSaves();
    }
    
    /**
     * Creates a test world state with sample data.
     */
    private WorldState createTestWorldState() {
        WorldState worldState = new WorldState(TEST_WORLD_SEED);
        
        // Clear generated trees to have predictable test data
        worldState.getTrees().clear();
        
        // Add test trees
        TreeState tree1 = new TreeState("tree1", TreeType.APPLE, 100.0f, 100.0f, 80.0f, true);
        TreeState tree2 = new TreeState("tree2", TreeType.BANANA, 200.0f, 150.0f, 100.0f, true);
        TreeState tree3 = new TreeState("tree3", TreeType.COCONUT, 300.0f, 200.0f, 0.0f, false); // Destroyed tree
        
        worldState.addOrUpdateTree(tree1);
        worldState.addOrUpdateTree(tree2);
        worldState.addOrUpdateTree(tree3);
        
        // Add test items
        ItemState item1 = new ItemState("item1", ItemType.APPLE, 150.0f, 120.0f, false);
        ItemState item2 = new ItemState("item2", ItemType.BANANA, 250.0f, 180.0f, true); // Collected item
        
        worldState.addOrUpdateItem(item1);
        worldState.addOrUpdateItem(item2);
        
        // Add cleared positions
        worldState.getClearedPositions().add("300_200"); // Position of destroyed tree
        
        // Add rain zones
        List<RainZone> rainZones = new ArrayList<>();
        rainZones.add(new RainZone("test_zone_1", 0.0f, 0.0f, 500.0f, 100.0f, 0.5f));
        worldState.setRainZones(rainZones);
        
        return worldState;
    }
    
    /**
     * Recursively deletes a directory and all its contents.
     */
    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }
    
    /**
     * Cleans up any test saves that might have been created in the actual save directory.
     */
    private void cleanupTestSaves() {
        try {
            // Clean up singleplayer test saves
            if (WorldSaveManager.saveExists(TEST_SAVE_NAME, false)) {
                WorldSaveManager.deleteSave(TEST_SAVE_NAME, false);
            }
            
            // Clean up multiplayer test saves
            if (WorldSaveManager.saveExists(TEST_SAVE_NAME, true)) {
                WorldSaveManager.deleteSave(TEST_SAVE_NAME, true);
            }
            
            // Clean up any other test saves
            String[] testNames = {"test-save-1", "test-save-2", "overwrite-test", "backup-test"};
            for (String name : testNames) {
                if (WorldSaveManager.saveExists(name, false)) {
                    WorldSaveManager.deleteSave(name, false);
                }
                if (WorldSaveManager.saveExists(name, true)) {
                    WorldSaveManager.deleteSave(name, true);
                }
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
    
    // Test 1: Save name validation
    
    @Test
    @Order(1)
    public void testValidSaveNames() {
        assertTrue(WorldSaveManager.isValidSaveName("valid-name"), "Simple name should be valid");
        assertTrue(WorldSaveManager.isValidSaveName("Valid Name 123"), "Name with spaces and numbers should be valid");
        assertTrue(WorldSaveManager.isValidSaveName("test_world"), "Name with underscores should be valid");
        assertTrue(WorldSaveManager.isValidSaveName("My-World-Save"), "Name with hyphens should be valid");
        assertTrue(WorldSaveManager.isValidSaveName("a"), "Single character should be valid");
    }
    
    @Test
    @Order(2)
    public void testInvalidSaveNames() {
        assertFalse(WorldSaveManager.isValidSaveName(null), "Null name should be invalid");
        assertFalse(WorldSaveManager.isValidSaveName(""), "Empty name should be invalid");
        assertFalse(WorldSaveManager.isValidSaveName("   "), "Whitespace-only name should be invalid");
        assertFalse(WorldSaveManager.isValidSaveName("../invalid"), "Path traversal should be invalid");
        assertFalse(WorldSaveManager.isValidSaveName("invalid/path"), "Forward slash should be invalid");
        assertFalse(WorldSaveManager.isValidSaveName("invalid\\path"), "Backslash should be invalid");
        assertFalse(WorldSaveManager.isValidSaveName("con"), "Windows reserved name should be invalid");
        assertFalse(WorldSaveManager.isValidSaveName("CON"), "Windows reserved name (uppercase) should be invalid");
        assertFalse(WorldSaveManager.isValidSaveName("prn"), "Windows reserved name PRN should be invalid");
        assertFalse(WorldSaveManager.isValidSaveName("com1"), "Windows reserved name COM1 should be invalid");
        
        // Test very long name
        String longName = "a".repeat(100);
        assertFalse(WorldSaveManager.isValidSaveName(longName), "Very long name should be invalid");
        
        // Test special characters
        assertFalse(WorldSaveManager.isValidSaveName("test@world"), "Special characters should be invalid");
        assertFalse(WorldSaveManager.isValidSaveName("test*world"), "Asterisk should be invalid");
        assertFalse(WorldSaveManager.isValidSaveName("test?world"), "Question mark should be invalid");
    }
    
    // Test 2: Basic save and load operations
    
    @Test
    @Order(3)
    public void testBasicSaveAndLoad() {
        float playerX = 123.5f;
        float playerY = 456.7f;
        float playerHealth = 85.0f;
        
        // Test singleplayer save
        assertTrue(WorldSaveManager.saveWorld(TEST_SAVE_NAME, testWorldState, 
                                            playerX, playerY, playerHealth, false),
                  "Save operation should succeed");
        
        // Verify save exists
        assertTrue(WorldSaveManager.saveExists(TEST_SAVE_NAME, false), 
                  "Save should exist after saving");
        
        // Load the save
        WorldSaveData loadedData = WorldSaveManager.loadWorld(TEST_SAVE_NAME, false);
        assertNotNull(loadedData, "Loaded data should not be null");
        
        // Verify loaded data
        assertEquals(TEST_WORLD_SEED, loadedData.getWorldSeed(), "World seed should match");
        assertEquals(playerX, loadedData.getPlayerX(), 0.01f, "Player X should match");
        assertEquals(playerY, loadedData.getPlayerY(), 0.01f, "Player Y should match");
        assertEquals(playerHealth, loadedData.getPlayerHealth(), 0.01f, "Player health should match");
        assertEquals(TEST_SAVE_NAME, loadedData.getSaveName(), "Save name should match");
        assertEquals("singleplayer", loadedData.getGameMode(), "Game mode should be singleplayer");
        
        // Verify tree data (includes generated trees plus our test trees)
        assertNotNull(loadedData.getTrees(), "Trees should not be null");
        assertTrue(loadedData.getTrees().size() >= 3, "Should have at least 3 trees (including generated ones)");
        assertTrue(loadedData.getTrees().containsKey("tree1"), "Should contain tree1");
        assertTrue(loadedData.getTrees().containsKey("tree2"), "Should contain tree2");
        assertTrue(loadedData.getTrees().containsKey("tree3"), "Should contain tree3");
        
        // Verify item data
        assertNotNull(loadedData.getItems(), "Items should not be null");
        assertEquals(2, loadedData.getItems().size(), "Should have 2 items");
        assertTrue(loadedData.getItems().containsKey("item1"), "Should contain item1");
        assertTrue(loadedData.getItems().containsKey("item2"), "Should contain item2");
        
        // Verify cleared positions
        assertNotNull(loadedData.getClearedPositions(), "Cleared positions should not be null");
        assertEquals(1, loadedData.getClearedPositions().size(), "Should have 1 cleared position");
        assertTrue(loadedData.getClearedPositions().contains("300_200"), "Should contain cleared position");
        
        // Verify rain zones
        assertNotNull(loadedData.getRainZones(), "Rain zones should not be null");
        assertEquals(1, loadedData.getRainZones().size(), "Should have 1 rain zone");
    }
    
    @Test
    @Order(4)
    public void testMultiplayerSaveAndLoad() {
        float playerX = 789.1f;
        float playerY = 234.5f;
        float playerHealth = 95.0f;
        
        // Test multiplayer save
        assertTrue(WorldSaveManager.saveWorld(TEST_SAVE_NAME, testWorldState, 
                                            playerX, playerY, playerHealth, true),
                  "Multiplayer save operation should succeed");
        
        // Verify save exists
        assertTrue(WorldSaveManager.saveExists(TEST_SAVE_NAME, true), 
                  "Multiplayer save should exist after saving");
        
        // Load the save
        WorldSaveData loadedData = WorldSaveManager.loadWorld(TEST_SAVE_NAME, true);
        assertNotNull(loadedData, "Loaded multiplayer data should not be null");
        
        // Verify game mode
        assertEquals("multiplayer", loadedData.getGameMode(), "Game mode should be multiplayer");
        
        // Verify player data
        assertEquals(playerX, loadedData.getPlayerX(), 0.01f, "Player X should match");
        assertEquals(playerY, loadedData.getPlayerY(), 0.01f, "Player Y should match");
        assertEquals(playerHealth, loadedData.getPlayerHealth(), 0.01f, "Player health should match");
    }
    
    // Test 3: Error handling
    
    @Test
    @Order(5)
    public void testSaveWithInvalidData() {
        // Test with null world state
        assertFalse(WorldSaveManager.saveWorld(TEST_SAVE_NAME, null, 0, 0, 100, false),
                   "Save with null world state should fail");
        
        // Test with invalid save name
        assertFalse(WorldSaveManager.saveWorld(INVALID_SAVE_NAME, testWorldState, 0, 0, 100, false),
                   "Save with invalid name should fail");
        
        // Test with empty save name
        assertFalse(WorldSaveManager.saveWorld("", testWorldState, 0, 0, 100, false),
                   "Save with empty name should fail");
    }
    
    @Test
    @Order(6)
    public void testLoadNonExistentSave() {
        WorldSaveData loadedData = WorldSaveManager.loadWorld("non-existent-save", false);
        assertNull(loadedData, "Loading non-existent save should return null");
    }
    
    @Test
    @Order(7)
    public void testLoadWithInvalidName() {
        WorldSaveData loadedData = WorldSaveManager.loadWorld(INVALID_SAVE_NAME, false);
        assertNull(loadedData, "Loading with invalid name should return null");
    }
    
    // Test 4: Save file management
    
    @Test
    @Order(8)
    public void testSaveOverwrite() {
        float playerX1 = 100.0f;
        float playerY1 = 200.0f;
        float playerHealth1 = 80.0f;
        
        // Create first save
        assertTrue(WorldSaveManager.saveWorld("overwrite-test", testWorldState, 
                                            playerX1, playerY1, playerHealth1, false),
                  "First save should succeed");
        
        // Load first save
        WorldSaveData firstSave = WorldSaveManager.loadWorld("overwrite-test", false);
        assertNotNull(firstSave, "First save should load successfully");
        assertEquals(playerX1, firstSave.getPlayerX(), 0.01f, "First save player X should match");
        
        // Wait a moment to ensure different timestamp
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Create second save with different data
        float playerX2 = 300.0f;
        float playerY2 = 400.0f;
        float playerHealth2 = 90.0f;
        
        assertTrue(WorldSaveManager.saveWorld("overwrite-test", testWorldState, 
                                            playerX2, playerY2, playerHealth2, false),
                  "Overwrite save should succeed");
        
        // Load overwritten save
        WorldSaveData secondSave = WorldSaveManager.loadWorld("overwrite-test", false);
        assertNotNull(secondSave, "Overwritten save should load successfully");
        assertEquals(playerX2, secondSave.getPlayerX(), 0.01f, "Overwritten save player X should match new value");
        assertNotEquals(firstSave.getSaveTimestamp(), secondSave.getSaveTimestamp(), 
                       "Timestamps should be different");
    }
    
    @Test
    @Order(9)
    public void testDeleteSave() {
        // Create a save to delete
        assertTrue(WorldSaveManager.saveWorld("delete-test", testWorldState, 0, 0, 100, false),
                  "Save creation should succeed");
        
        // Verify it exists
        assertTrue(WorldSaveManager.saveExists("delete-test", false), 
                  "Save should exist before deletion");
        
        // Delete the save
        assertTrue(WorldSaveManager.deleteSave("delete-test", false),
                  "Delete operation should succeed");
        
        // Verify it no longer exists
        assertFalse(WorldSaveManager.saveExists("delete-test", false), 
                   "Save should not exist after deletion");
        
        // Try to load deleted save
        WorldSaveData deletedSave = WorldSaveManager.loadWorld("delete-test", false);
        assertNull(deletedSave, "Loading deleted save should return null");
    }
    
    @Test
    @Order(10)
    public void testDeleteNonExistentSave() {
        // Try to delete a save that doesn't exist
        boolean result = WorldSaveManager.deleteSave("non-existent-save", false);
        // This should return true since the goal (save not existing) is achieved
        assertTrue(result, "Deleting non-existent save should succeed");
    }
    
    @Test
    @Order(11)
    public void testDeleteWithInvalidName() {
        assertFalse(WorldSaveManager.deleteSave(INVALID_SAVE_NAME, false),
                   "Delete with invalid name should fail");
    }
    
    // Test 5: Save listing
    
    @Test
    @Order(12)
    public void testListAvailableSaves() {
        // Create multiple saves
        assertTrue(WorldSaveManager.saveWorld("list-test-1", testWorldState, 100, 100, 80, false),
                  "First save should succeed");
        
        assertTrue(WorldSaveManager.saveWorld("list-test-2", testWorldState, 200, 200, 90, false),
                  "Second save should succeed");
        
        // List saves
        List<WorldSaveInfo> saves = WorldSaveManager.listAvailableSaves(false);
        assertNotNull(saves, "Save list should not be null");
        
        // Find our test saves
        boolean found1 = false, found2 = false;
        for (WorldSaveInfo save : saves) {
            if ("list-test-1".equals(save.getSaveName())) {
                found1 = true;
                assertEquals("singleplayer", save.getGameMode(), "Game mode should be singleplayer");
                assertEquals(TEST_WORLD_SEED, save.getWorldSeed(), "World seed should match");
                assertTrue(save.getTreeCount() > 0, "Tree count should be positive");
                assertTrue(save.getItemCount() > 0, "Item count should be positive");
            } else if ("list-test-2".equals(save.getSaveName())) {
                found2 = true;
            }
        }
        
        assertTrue(found1, "Should find first test save");
        assertTrue(found2, "Should find second test save");
        
        // Clean up
        WorldSaveManager.deleteSave("list-test-1", false);
        WorldSaveManager.deleteSave("list-test-2", false);
    }
    
    @Test
    @Order(13)
    public void testListEmptyDirectory() {
        // List saves from empty directory (multiplayer in this case, assuming it's empty)
        List<WorldSaveInfo> saves = WorldSaveManager.listAvailableSaves(true);
        assertNotNull(saves, "Save list should not be null even for empty directory");
        // Note: We can't assert it's empty because there might be existing saves
    }
    
    // Test 6: Directory and file system operations
    
    @Test
    @Order(14)
    public void testSaveDirectoryWritable() {
        // Test that save directories are writable
        assertTrue(WorldSaveManager.isSaveDirectoryWritable(false), 
                  "Singleplayer save directory should be writable");
        assertTrue(WorldSaveManager.isSaveDirectoryWritable(true), 
                  "Multiplayer save directory should be writable");
    }
    
    @Test
    @Order(15)
    public void testSufficientDiskSpace() {
        // Test disk space check with reasonable size
        assertTrue(WorldSaveManager.hasSufficientDiskSpace(false, 1024), 
                  "Should have sufficient space for small save");
        assertTrue(WorldSaveManager.hasSufficientDiskSpace(true, 1024), 
                  "Should have sufficient space for small multiplayer save");
        
        // Test with very large size (this might fail on systems with limited space)
        // We'll use a more reasonable size that should still pass on most systems
        long largeSize = 100 * 1024 * 1024; // 100 MB
        // Don't assert this as it depends on actual disk space
        WorldSaveManager.hasSufficientDiskSpace(false, largeSize);
    }
    
    @Test
    @Order(16)
    public void testSaveFileIntegrityValidation() {
        // Create a valid save
        assertTrue(WorldSaveManager.saveWorld("integrity-test", testWorldState, 0, 0, 100, false),
                  "Save creation should succeed");
        
        // Validate the save file integrity
        assertTrue(WorldSaveManager.validateSaveFileIntegrity("integrity-test", false),
                  "Valid save file should pass integrity check");
        
        // Test with non-existent save
        assertFalse(WorldSaveManager.validateSaveFileIntegrity("non-existent", false),
                   "Non-existent save should fail integrity check");
        
        // Clean up
        WorldSaveManager.deleteSave("integrity-test", false);
    }
    
    // Test 7: Backup functionality
    
    @Test
    @Order(17)
    public void testBackupCreation() {
        // Create initial save
        assertTrue(WorldSaveManager.saveWorld("backup-test", testWorldState, 100, 100, 80, false),
                  "Initial save should succeed");
        
        // Overwrite with new data (should create backup)
        assertTrue(WorldSaveManager.saveWorld("backup-test", testWorldState, 200, 200, 90, false),
                  "Overwrite save should succeed and create backup");
        
        // Load the current save
        WorldSaveData currentSave = WorldSaveManager.loadWorld("backup-test", false);
        assertNotNull(currentSave, "Current save should load");
        assertEquals(200.0f, currentSave.getPlayerX(), 0.01f, "Should have new player X position");
        
        // Clean up
        WorldSaveManager.deleteSave("backup-test", false);
    }
    
    // Test 8: Edge cases and boundary conditions
    
    @Test
    @Order(18)
    public void testSaveWithEmptyWorldState() {
        // Create world state with minimal data (WorldState constructor generates initial trees)
        WorldState minimalWorld = new WorldState(TEST_WORLD_SEED);
        // Clear the generated trees to make it truly empty
        minimalWorld.getTrees().clear();
        
        // Should still be able to save minimal world
        assertTrue(WorldSaveManager.saveWorld("empty-test", minimalWorld, 0, 0, 100, false),
                  "Should be able to save minimal world state");
        
        // Load and verify
        WorldSaveData loadedData = WorldSaveManager.loadWorld("empty-test", false);
        assertNotNull(loadedData, "Minimal world save should load");
        assertEquals(TEST_WORLD_SEED, loadedData.getWorldSeed(), "World seed should match");
        assertNotNull(loadedData.getTrees(), "Trees map should not be null");
        assertNotNull(loadedData.getItems(), "Items map should not be null");
        assertNotNull(loadedData.getClearedPositions(), "Cleared positions should not be null");
        assertEquals(0, loadedData.getTrees().size(), "Should have no trees");
        assertEquals(0, loadedData.getItems().size(), "Should have no items");
        assertEquals(0, loadedData.getClearedPositions().size(), "Should have no cleared positions");
        
        // Clean up
        WorldSaveManager.deleteSave("empty-test", false);
    }
    
    @Test
    @Order(19)
    public void testSaveWithExtremePlayerValues() {
        // Test with boundary player values
        float minX = -Float.MAX_VALUE;
        float maxY = Float.MAX_VALUE;
        float minHealth = 0.0f;
        
        assertTrue(WorldSaveManager.saveWorld("extreme-test", testWorldState, 
                                            minX, maxY, minHealth, false),
                  "Should handle extreme player values");
        
        WorldSaveData loadedData = WorldSaveManager.loadWorld("extreme-test", false);
        assertNotNull(loadedData, "Extreme values save should load");
        assertEquals(minX, loadedData.getPlayerX(), 0.01f, "Extreme X should be preserved");
        assertEquals(maxY, loadedData.getPlayerY(), 0.01f, "Extreme Y should be preserved");
        assertEquals(minHealth, loadedData.getPlayerHealth(), 0.01f, "Min health should be preserved");
        
        // Clean up
        WorldSaveManager.deleteSave("extreme-test", false);
    }
    
    @Test
    @Order(20)
    public void testConcurrentSaveOperations() {
        // This test simulates potential concurrent access issues
        // In a real scenario, the game should handle synchronization
        
        String saveName1 = "concurrent-test-1";
        String saveName2 = "concurrent-test-2";
        
        // Perform multiple save operations
        assertTrue(WorldSaveManager.saveWorld(saveName1, testWorldState, 100, 100, 80, false),
                  "First concurrent save should succeed");
        
        assertTrue(WorldSaveManager.saveWorld(saveName2, testWorldState, 200, 200, 90, false),
                  "Second concurrent save should succeed");
        
        // Verify both saves exist and are correct
        WorldSaveData save1 = WorldSaveManager.loadWorld(saveName1, false);
        WorldSaveData save2 = WorldSaveManager.loadWorld(saveName2, false);
        
        assertNotNull(save1, "First save should load");
        assertNotNull(save2, "Second save should load");
        assertEquals(100.0f, save1.getPlayerX(), 0.01f, "First save should have correct data");
        assertEquals(200.0f, save2.getPlayerX(), 0.01f, "Second save should have correct data");
        
        // Clean up
        WorldSaveManager.deleteSave(saveName1, false);
        WorldSaveManager.deleteSave(saveName2, false);
    }
    
    // Test 9: Respawn data integration
    
    @Test
    @Order(21)
    public void testSaveAndLoadWithRespawnData() {
        // Create mock respawn manager with test data
        RespawnManager mockRespawnManager = new RespawnManager(null, true);
        
        // Register some destroyed resources
        long currentTime = System.currentTimeMillis();
        mockRespawnManager.registerDestruction("tree_123", ResourceType.TREE, 100.0f, 200.0f, TreeType.APPLE);
        mockRespawnManager.registerDestruction("stone_456", ResourceType.STONE, 300.0f, 400.0f, null);
        
        // Save world with respawn data
        assertTrue(WorldSaveManager.saveWorld("respawn-test", testWorldState, 
                                            100, 200, 85, null, false, mockRespawnManager),
                  "Save with respawn data should succeed");
        
        // Load the save
        WorldSaveData loadedData = WorldSaveManager.loadWorld("respawn-test", false);
        assertNotNull(loadedData, "Loaded data should not be null");
        
        // Verify respawn data was saved
        List<RespawnEntry> respawnData = loadedData.getPendingRespawns();
        assertNotNull(respawnData, "Respawn data should not be null");
        assertEquals(2, respawnData.size(), "Should have 2 pending respawns");
        
        // Verify respawn entries
        boolean foundTree = false;
        boolean foundStone = false;
        
        for (RespawnEntry entry : respawnData) {
            if ("tree_123".equals(entry.getResourceId())) {
                foundTree = true;
                assertEquals(ResourceType.TREE, entry.getResourceType(), "Resource type should be TREE");
                assertEquals(100.0f, entry.getX(), 0.01f, "Tree X position should match");
                assertEquals(200.0f, entry.getY(), 0.01f, "Tree Y position should match");
                assertEquals(TreeType.APPLE, entry.getTreeType(), "Tree type should be APPLE");
            } else if ("stone_456".equals(entry.getResourceId())) {
                foundStone = true;
                assertEquals(ResourceType.STONE, entry.getResourceType(), "Resource type should be STONE");
                assertEquals(300.0f, entry.getX(), 0.01f, "Stone X position should match");
                assertEquals(400.0f, entry.getY(), 0.01f, "Stone Y position should match");
                assertNull(entry.getTreeType(), "Stone should have null tree type");
            }
        }
        
        assertTrue(foundTree, "Should find tree respawn entry");
        assertTrue(foundStone, "Should find stone respawn entry");
        
        // Clean up
        WorldSaveManager.deleteSave("respawn-test", false);
    }
    
    @Test
    @Order(22)
    public void testSaveWithoutRespawnData() {
        // Save without respawn manager (backward compatibility)
        assertTrue(WorldSaveManager.saveWorld("no-respawn-test", testWorldState, 
                                            100, 200, 85, null, false, null),
                  "Save without respawn data should succeed");
        
        // Load the save
        WorldSaveData loadedData = WorldSaveManager.loadWorld("no-respawn-test", false);
        assertNotNull(loadedData, "Loaded data should not be null");
        
        // Verify respawn data is null or empty
        List<RespawnEntry> respawnData = loadedData.getPendingRespawns();
        assertTrue(respawnData == null || respawnData.isEmpty(), 
                  "Respawn data should be null or empty when not provided");
        
        // Clean up
        WorldSaveManager.deleteSave("no-respawn-test", false);
    }
    
    @Test
    @Order(23)
    public void testLoadWithExpiredRespawnTimers() {
        // Create mock respawn manager with expired timer
        RespawnManager mockRespawnManager = new RespawnManager(null, true);
        
        // Register a resource that was destroyed in the past (expired timer)
        long pastTime = System.currentTimeMillis() - (20 * 60 * 1000); // 20 minutes ago
        RespawnEntry expiredEntry = new RespawnEntry(
            "expired_tree",
            ResourceType.TREE,
            150.0f,
            250.0f,
            pastTime,
            15 * 60 * 1000, // 15 minute respawn duration
            TreeType.BANANA
        );
        
        // Manually add expired entry to respawn manager's save data
        List<RespawnEntry> testEntries = new ArrayList<>();
        testEntries.add(expiredEntry);
        
        // Save with respawn manager first to create the file
        assertTrue(WorldSaveManager.saveWorld("expired-respawn-test", testWorldState, 
                                            100, 200, 85, null, false, mockRespawnManager),
                  "Save should succeed");
        
        // Load and update with expired entry
        WorldSaveData loadedForUpdate = WorldSaveManager.loadWorld("expired-respawn-test", false);
        assertNotNull(loadedForUpdate, "Initial load should succeed");
        loadedForUpdate.setPendingRespawns(testEntries);
        
        // Get the config directory path
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        File configDir;
        
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            configDir = appData != null ? new File(appData, "Woodlanders") : new File(userHome, "AppData/Roaming/Woodlanders");
        } else if (os.contains("mac")) {
            configDir = new File(userHome, "Library/Application Support/Woodlanders");
        } else {
            configDir = new File(userHome, ".config/woodlanders");
        }
        
        File saveDir = new File(new File(configDir, "world-saves"), "singleplayer");
        File saveFile = new File(saveDir, "expired-respawn-test.wld");
        
        // Re-save with expired entry
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(saveFile);
             java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(fos)) {
            oos.writeObject(loadedForUpdate);
        } catch (Exception e) {
            fail("Failed to manually save expired respawn data: " + e.getMessage());
        }
        
        // Load the save - should detect expired timer
        WorldSaveData loadedData = WorldSaveManager.loadWorld("expired-respawn-test", false);
        assertNotNull(loadedData, "Loaded data should not be null");
        
        List<RespawnEntry> respawnData = loadedData.getPendingRespawns();
        assertNotNull(respawnData, "Respawn data should not be null");
        assertEquals(1, respawnData.size(), "Should have 1 respawn entry");
        
        // Verify the entry is marked as ready to respawn
        RespawnEntry entry = respawnData.get(0);
        assertTrue(entry.isReadyToRespawn(), "Entry should be ready to respawn");
        
        // Clean up
        WorldSaveManager.deleteSave("expired-respawn-test", false);
    }
    
    @Test
    @Order(24)
    public void testBackwardCompatibilityWithOldSaves() {
        // Test loading old saves that don't have respawn data
        // This simulates loading a save file from before respawn system was added
        
        // Create a save without respawn data using the old method signature
        assertTrue(WorldSaveManager.saveWorld("old-save-test", testWorldState, 
                                            100, 200, 85, false),
                  "Old-style save should succeed");
        
        // Load the save
        WorldSaveData loadedData = WorldSaveManager.loadWorld("old-save-test", false);
        assertNotNull(loadedData, "Old save should load successfully");
        
        // Verify basic data is intact
        assertEquals(TEST_WORLD_SEED, loadedData.getWorldSeed(), "World seed should match");
        assertEquals(100.0f, loadedData.getPlayerX(), 0.01f, "Player X should match");
        
        // Verify respawn data is handled gracefully (null or empty)
        List<RespawnEntry> respawnData = loadedData.getPendingRespawns();
        assertTrue(respawnData == null || respawnData.isEmpty(), 
                  "Old saves should have null or empty respawn data");
        
        // Clean up
        WorldSaveManager.deleteSave("old-save-test", false);
    }
    
    @Test
    @Order(20)
    public void testBabyTreeInventorySaveLoad() {
        // Create inventory with BabyTree items
        wagemaker.uk.inventory.Inventory testInventory = new wagemaker.uk.inventory.Inventory();
        testInventory.setAppleCount(5);
        testInventory.setBananaCount(3);
        testInventory.setBabyBambooCount(7);
        testInventory.setBambooStackCount(4);
        testInventory.setBabyTreeCount(10); // Set BabyTree count
        testInventory.setWoodStackCount(6);
        testInventory.setPebbleCount(2);
        
        // Save world with inventory
        boolean saveResult = WorldSaveManager.saveWorld(
            "babytree-inventory-test",
            testWorldState,
            100.0f,
            200.0f,
            85.0f,
            testInventory,
            false
        );
        
        assertTrue(saveResult, "Save with BabyTree inventory should succeed");
        
        // Load the save
        WorldSaveData loadedData = WorldSaveManager.loadWorld("babytree-inventory-test", false);
        assertNotNull(loadedData, "Loaded save data should not be null");
        
        // Verify all inventory counts are restored correctly
        assertEquals(5, loadedData.getAppleCount(), "Apple count should be restored");
        assertEquals(3, loadedData.getBananaCount(), "Banana count should be restored");
        assertEquals(7, loadedData.getBabyBambooCount(), "BabyBamboo count should be restored");
        assertEquals(4, loadedData.getBambooStackCount(), "BambooStack count should be restored");
        assertEquals(10, loadedData.getBabyTreeCount(), "BabyTree count should be restored");
        assertEquals(6, loadedData.getWoodStackCount(), "WoodStack count should be restored");
        assertEquals(2, loadedData.getPebbleCount(), "Pebble count should be restored");
        
        // Clean up
        WorldSaveManager.deleteSave("babytree-inventory-test", false);
    }
}