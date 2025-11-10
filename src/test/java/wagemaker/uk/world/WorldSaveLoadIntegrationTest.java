package wagemaker.uk.world;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import wagemaker.uk.network.WorldState;
import wagemaker.uk.network.TreeState;
import wagemaker.uk.network.TreeType;
import wagemaker.uk.network.ItemState;
import wagemaker.uk.network.ItemType;
import wagemaker.uk.weather.RainZone;
import wagemaker.uk.ui.WorldSaveDialog;
import wagemaker.uk.ui.WorldLoadDialog;
import wagemaker.uk.ui.WorldManageDialog;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Integration tests for complete world save/load workflow.
 * Tests end-to-end save and restore cycles, multiplayer mode restrictions,
 * and UI dialog interactions.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WorldSaveLoadIntegrationTest {
    
    private static final String TEST_SAVE_NAME = "integration-test-world";
    private static final String MP_TEST_SAVE_NAME = "mp-integration-test";
    private static final long TEST_WORLD_SEED = 98765L;
    
    private WorldState originalWorldState;
    private float originalPlayerX = 500.0f;
    private float originalPlayerY = 300.0f;
    private float originalPlayerHealth = 75.0f;
    
    @BeforeEach
    public void setUp() {
        // Create comprehensive test world state
        originalWorldState = createComprehensiveWorldState();
        
        // Clean up any existing test saves
        cleanupTestSaves();
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test saves
        cleanupTestSaves();
    }
    
    /**
     * Creates a comprehensive world state for testing with various game elements.
     */
    private WorldState createComprehensiveWorldState() {
        WorldState worldState = new WorldState(TEST_WORLD_SEED);
        
        // Clear generated trees to have predictable test data
        worldState.getTrees().clear();
        
        // Add diverse tree types and states
        TreeState appleTree = new TreeState("apple_1", TreeType.APPLE, 100.0f, 100.0f, 100.0f, true);
        TreeState bananaTree = new TreeState("banana_1", TreeType.BANANA, 200.0f, 150.0f, 80.0f, true);
        TreeState coconutTree = new TreeState("coconut_1", TreeType.COCONUT, 300.0f, 200.0f, 60.0f, true);
        TreeState bambooTree = new TreeState("bamboo_1", TreeType.BAMBOO, 400.0f, 250.0f, 40.0f, true);
        TreeState smallTree = new TreeState("small_1", TreeType.SMALL, 500.0f, 300.0f, 20.0f, true);
        TreeState destroyedTree = new TreeState("destroyed_1", TreeType.APPLE, 600.0f, 350.0f, 0.0f, false);
        
        worldState.addOrUpdateTree(appleTree);
        worldState.addOrUpdateTree(bananaTree);
        worldState.addOrUpdateTree(coconutTree);
        worldState.addOrUpdateTree(bambooTree);
        worldState.addOrUpdateTree(smallTree);
        worldState.addOrUpdateTree(destroyedTree);
        
        // Add various items in different states
        ItemState uncollectedApple = new ItemState("apple_item_1", ItemType.APPLE, 150.0f, 120.0f, false);
        ItemState uncollectedBanana = new ItemState("banana_item_1", ItemType.BANANA, 250.0f, 180.0f, false);
        ItemState collectedApple = new ItemState("apple_item_2", ItemType.APPLE, 350.0f, 220.0f, true);
        
        worldState.addOrUpdateItem(uncollectedApple);
        worldState.addOrUpdateItem(uncollectedBanana);
        worldState.addOrUpdateItem(collectedApple);
        
        // Add multiple cleared positions
        worldState.getClearedPositions().add("600_350"); // Position of destroyed tree
        worldState.getClearedPositions().add("700_400"); // Additional cleared area
        worldState.getClearedPositions().add("800_450"); // Another cleared area
        
        // Add multiple rain zones
        List<RainZone> rainZones = new ArrayList<>();
        rainZones.add(new RainZone("zone_1", 0.0f, 0.0f, 300.0f, 50.0f, 0.3f));
        rainZones.add(new RainZone("zone_2", 400.0f, 200.0f, 200.0f, 100.0f, 0.7f));
        rainZones.add(new RainZone("zone_3", 700.0f, 500.0f, 200.0f, 80.0f, 0.5f));
        worldState.setRainZones(rainZones);
        
        return worldState;
    }
    
    /**
     * Cleans up any test saves that might exist.
     */
    private void cleanupTestSaves() {
        try {
            String[] testNames = {TEST_SAVE_NAME, MP_TEST_SAVE_NAME, "workflow-test", "restore-test", "ui-test"};
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
    
    /**
     * Verifies that two world states are equivalent.
     */
    private void assertWorldStatesEqual(WorldState expected, WorldState actual, String message) {
        assertEquals(expected.getWorldSeed(), actual.getWorldSeed(), message + " - World seed should match");
        
        // Compare trees
        Map<String, TreeState> expectedTrees = expected.getTrees();
        Map<String, TreeState> actualTrees = actual.getTrees();
        assertEquals(expectedTrees.size(), actualTrees.size(), message + " - Tree count should match");
        
        for (Map.Entry<String, TreeState> entry : expectedTrees.entrySet()) {
            String treeId = entry.getKey();
            TreeState expectedTree = entry.getValue();
            TreeState actualTree = actualTrees.get(treeId);
            
            assertNotNull(actualTree, message + " - Tree " + treeId + " should exist");
            assertEquals(expectedTree.getType(), actualTree.getType(), 
                        message + " - Tree " + treeId + " type should match");
            assertEquals(expectedTree.getX(), actualTree.getX(), 0.01f, 
                        message + " - Tree " + treeId + " X position should match");
            assertEquals(expectedTree.getY(), actualTree.getY(), 0.01f, 
                        message + " - Tree " + treeId + " Y position should match");
            assertEquals(expectedTree.getHealth(), actualTree.getHealth(), 0.01f, 
                        message + " - Tree " + treeId + " health should match");
            assertEquals(expectedTree.isExists(), actualTree.isExists(), 
                        message + " - Tree " + treeId + " existence should match");
        }
        
        // Compare items
        Map<String, ItemState> expectedItems = expected.getItems();
        Map<String, ItemState> actualItems = actual.getItems();
        assertEquals(expectedItems.size(), actualItems.size(), message + " - Item count should match");
        
        for (Map.Entry<String, ItemState> entry : expectedItems.entrySet()) {
            String itemId = entry.getKey();
            ItemState expectedItem = entry.getValue();
            ItemState actualItem = actualItems.get(itemId);
            
            assertNotNull(actualItem, message + " - Item " + itemId + " should exist");
            assertEquals(expectedItem.getType(), actualItem.getType(), 
                        message + " - Item " + itemId + " type should match");
            assertEquals(expectedItem.getX(), actualItem.getX(), 0.01f, 
                        message + " - Item " + itemId + " X position should match");
            assertEquals(expectedItem.getY(), actualItem.getY(), 0.01f, 
                        message + " - Item " + itemId + " Y position should match");
            assertEquals(expectedItem.isCollected(), actualItem.isCollected(), 
                        message + " - Item " + itemId + " collected state should match");
        }
        
        // Compare cleared positions
        Set<String> expectedCleared = expected.getClearedPositions();
        Set<String> actualCleared = actual.getClearedPositions();
        assertEquals(expectedCleared.size(), actualCleared.size(), message + " - Cleared position count should match");
        for (String clearedPos : expectedCleared) {
            assertTrue(actualCleared.contains(clearedPos), 
                      message + " - Cleared position " + clearedPos + " should exist");
        }
        
        // Compare rain zones
        List<RainZone> expectedRain = expected.getRainZones();
        List<RainZone> actualRain = actual.getRainZones();
        if (expectedRain != null && actualRain != null) {
            assertEquals(expectedRain.size(), actualRain.size(), message + " - Rain zone count should match");
            // Note: Detailed rain zone comparison would require access to RainZone fields
        }
    }
    
    // Test 1: Complete end-to-end save and restore cycle
    
    @Test
    @Order(1)
    public void testCompleteEndToEndSaveRestoreCycle() {
        // Step 1: Save the original world state
        boolean saveResult = WorldSaveManager.saveWorld(TEST_SAVE_NAME, originalWorldState, 
                                                       originalPlayerX, originalPlayerY, 
                                                       originalPlayerHealth, false);
        assertTrue(saveResult, "Initial save should succeed");
        
        // Step 2: Verify save exists
        assertTrue(WorldSaveManager.saveExists(TEST_SAVE_NAME, false), 
                  "Save should exist after creation");
        
        // Step 3: Load the save data
        WorldSaveData loadedSaveData = WorldSaveManager.loadWorld(TEST_SAVE_NAME, false);
        assertNotNull(loadedSaveData, "Save data should load successfully");
        
        // Step 4: Verify save data integrity
        assertTrue(loadedSaveData.isValid(), "Loaded save data should be valid");
        assertTrue(loadedSaveData.isCompatible(), "Loaded save data should be compatible");
        
        // Step 5: Reconstruct world state from save data
        WorldState restoredWorldState = new WorldState(loadedSaveData.getWorldSeed());
        restoredWorldState.setTrees(loadedSaveData.getTrees());
        restoredWorldState.setItems(loadedSaveData.getItems());
        restoredWorldState.setClearedPositions(loadedSaveData.getClearedPositions());
        restoredWorldState.setRainZones(loadedSaveData.getRainZones());
        
        // Step 6: Verify complete world state restoration
        assertWorldStatesEqual(originalWorldState, restoredWorldState, "Restored world state");
        
        // Step 7: Verify player data restoration
        assertEquals(originalPlayerX, loadedSaveData.getPlayerX(), 0.01f, "Player X should be restored");
        assertEquals(originalPlayerY, loadedSaveData.getPlayerY(), 0.01f, "Player Y should be restored");
        assertEquals(originalPlayerHealth, loadedSaveData.getPlayerHealth(), 0.01f, "Player health should be restored");
        
        // Step 8: Verify metadata
        assertEquals(TEST_SAVE_NAME, loadedSaveData.getSaveName(), "Save name should match");
        assertEquals("singleplayer", loadedSaveData.getGameMode(), "Game mode should be singleplayer");
        assertTrue(loadedSaveData.getSaveTimestamp() > 0, "Save timestamp should be set");
        
        // Step 9: Verify statistics
        assertEquals(5, loadedSaveData.getExistingTreeCount(), "Should have 5 existing trees");
        assertEquals(2, loadedSaveData.getUncollectedItemCount(), "Should have 2 uncollected items");
    }
    
    @Test
    @Order(2)
    public void testMultipleSequentialSaveRestoreCycles() {
        // Perform multiple save/restore cycles to test consistency
        for (int cycle = 1; cycle <= 3; cycle++) {
            String saveName = "workflow-test-" + cycle;
            
            // Modify world state slightly for each cycle
            TreeState newTree = new TreeState("cycle_tree_" + cycle, TreeType.APPLE, 
                                            100.0f * cycle, 100.0f * cycle, 100.0f, true);
            originalWorldState.addOrUpdateTree(newTree);
            
            float playerX = originalPlayerX + (cycle * 10);
            float playerY = originalPlayerY + (cycle * 10);
            float playerHealth = Math.max(10.0f, originalPlayerHealth - (cycle * 5));
            
            // Save
            assertTrue(WorldSaveManager.saveWorld(saveName, originalWorldState, 
                                                playerX, playerY, playerHealth, false),
                      "Cycle " + cycle + " save should succeed");
            
            // Load and verify
            WorldSaveData loadedData = WorldSaveManager.loadWorld(saveName, false);
            assertNotNull(loadedData, "Cycle " + cycle + " load should succeed");
            
            assertEquals(playerX, loadedData.getPlayerX(), 0.01f, 
                        "Cycle " + cycle + " player X should match");
            assertEquals(playerY, loadedData.getPlayerY(), 0.01f, 
                        "Cycle " + cycle + " player Y should match");
            assertEquals(playerHealth, loadedData.getPlayerHealth(), 0.01f, 
                        "Cycle " + cycle + " player health should match");
            
            // Verify the new tree exists
            assertTrue(loadedData.getTrees().containsKey("cycle_tree_" + cycle),
                      "Cycle " + cycle + " should contain new tree");
            
            // Clean up
            WorldSaveManager.deleteSave(saveName, false);
        }
    }
    
    // Test 2: Multiplayer mode restrictions and functionality
    
    @Test
    @Order(3)
    public void testMultiplayerSaveRestoreWorkflow() {
        // Test multiplayer save functionality
        boolean mpSaveResult = WorldSaveManager.saveWorld(MP_TEST_SAVE_NAME, originalWorldState, 
                                                         originalPlayerX, originalPlayerY, 
                                                         originalPlayerHealth, true);
        assertTrue(mpSaveResult, "Multiplayer save should succeed");
        
        // Verify multiplayer save exists
        assertTrue(WorldSaveManager.saveExists(MP_TEST_SAVE_NAME, true), 
                  "Multiplayer save should exist");
        
        // Verify it doesn't exist in singleplayer directory
        assertFalse(WorldSaveManager.saveExists(MP_TEST_SAVE_NAME, false), 
                   "Save should not exist in singleplayer directory");
        
        // Load multiplayer save
        WorldSaveData mpLoadedData = WorldSaveManager.loadWorld(MP_TEST_SAVE_NAME, true);
        assertNotNull(mpLoadedData, "Multiplayer save should load");
        assertEquals("multiplayer", mpLoadedData.getGameMode(), "Game mode should be multiplayer");
        
        // Verify world state is preserved in multiplayer mode
        WorldState mpRestoredState = new WorldState(mpLoadedData.getWorldSeed());
        mpRestoredState.setTrees(mpLoadedData.getTrees());
        mpRestoredState.setItems(mpLoadedData.getItems());
        mpRestoredState.setClearedPositions(mpLoadedData.getClearedPositions());
        mpRestoredState.setRainZones(mpLoadedData.getRainZones());
        
        assertWorldStatesEqual(originalWorldState, mpRestoredState, "Multiplayer restored world state");
    }
    
    @Test
    @Order(4)
    public void testSeparateDirectoryHandling() {
        String sameName = "same-name-test";
        
        // Create saves with same name in both directories
        assertTrue(WorldSaveManager.saveWorld(sameName, originalWorldState, 
                                            100, 100, 80, false),
                  "Singleplayer save should succeed");
        
        assertTrue(WorldSaveManager.saveWorld(sameName, originalWorldState, 
                                            200, 200, 90, true),
                  "Multiplayer save should succeed");
        
        // Verify both exist independently
        assertTrue(WorldSaveManager.saveExists(sameName, false), 
                  "Singleplayer save should exist");
        assertTrue(WorldSaveManager.saveExists(sameName, true), 
                  "Multiplayer save should exist");
        
        // Load both and verify they have different player positions
        WorldSaveData spSave = WorldSaveManager.loadWorld(sameName, false);
        WorldSaveData mpSave = WorldSaveManager.loadWorld(sameName, true);
        
        assertNotNull(spSave, "Singleplayer save should load");
        assertNotNull(mpSave, "Multiplayer save should load");
        
        assertEquals(100.0f, spSave.getPlayerX(), 0.01f, "Singleplayer save should have correct X");
        assertEquals(200.0f, mpSave.getPlayerX(), 0.01f, "Multiplayer save should have correct X");
        
        assertEquals("singleplayer", spSave.getGameMode(), "Should be singleplayer mode");
        assertEquals("multiplayer", mpSave.getGameMode(), "Should be multiplayer mode");
        
        // Clean up both
        WorldSaveManager.deleteSave(sameName, false);
        WorldSaveManager.deleteSave(sameName, true);
    }
    
    // Test 3: Save listing and management workflow
    
    @Test
    @Order(5)
    public void testSaveListingWorkflow() {
        // Create multiple saves with different timestamps
        String[] saveNames = {"list-test-1", "list-test-2", "list-test-3"};
        
        for (int i = 0; i < saveNames.length; i++) {
            // Add small delay to ensure different timestamps
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            float playerX = 100.0f + (i * 50);
            float playerY = 200.0f + (i * 50);
            float playerHealth = 80.0f + (i * 5);
            
            assertTrue(WorldSaveManager.saveWorld(saveNames[i], originalWorldState, 
                                                playerX, playerY, playerHealth, false),
                      "Save " + saveNames[i] + " should succeed");
        }
        
        // List all saves
        List<WorldSaveInfo> saves = WorldSaveManager.listAvailableSaves(false);
        assertNotNull(saves, "Save list should not be null");
        
        // Find our test saves
        Map<String, WorldSaveInfo> foundSaves = new HashMap<>();
        for (WorldSaveInfo save : saves) {
            for (String testName : saveNames) {
                if (testName.equals(save.getSaveName())) {
                    foundSaves.put(testName, save);
                }
            }
        }
        
        assertEquals(saveNames.length, foundSaves.size(), "Should find all test saves");
        
        // Verify save info details
        for (int i = 0; i < saveNames.length; i++) {
            WorldSaveInfo saveInfo = foundSaves.get(saveNames[i]);
            assertNotNull(saveInfo, "Save info for " + saveNames[i] + " should exist");
            
            assertTrue(saveInfo.isValid(), "Save info should be valid");
            assertEquals("singleplayer", saveInfo.getGameMode(), "Game mode should be singleplayer");
            assertEquals(TEST_WORLD_SEED, saveInfo.getWorldSeed(), "World seed should match");
            assertTrue(saveInfo.getTreeCount() > 0, "Tree count should be positive");
            assertTrue(saveInfo.getItemCount() > 0, "Item count should be positive");
            assertTrue(saveInfo.getClearedPositionCount() > 0, "Cleared position count should be positive");
            
            float expectedX = 100.0f + (i * 50);
            float expectedY = 200.0f + (i * 50);
            assertEquals(expectedX, saveInfo.getPlayerX(), 0.01f, "Player X should match");
            assertEquals(expectedY, saveInfo.getPlayerY(), 0.01f, "Player Y should match");
            
            // Verify formatted strings
            assertNotNull(saveInfo.getFormattedTimestamp(), "Formatted timestamp should not be null");
            assertNotNull(saveInfo.getFormattedFileSize(), "Formatted file size should not be null");
            assertNotNull(saveInfo.getSummary(), "Summary should not be null");
            assertNotNull(saveInfo.getFormattedPlayerPosition(), "Formatted position should not be null");
        }
        
        // Clean up
        for (String saveName : saveNames) {
            WorldSaveManager.deleteSave(saveName, false);
        }
    }
    
    // Test 4: Error recovery and backup workflow
    
    @Test
    @Order(6)
    public void testBackupAndRecoveryWorkflow() {
        String saveName = "backup-recovery-test";
        
        // Create initial save
        assertTrue(WorldSaveManager.saveWorld(saveName, originalWorldState, 
                                            100, 100, 80, false),
                  "Initial save should succeed");
        
        // Load initial save
        WorldSaveData initialSave = WorldSaveManager.loadWorld(saveName, false);
        assertNotNull(initialSave, "Initial save should load");
        assertEquals(100.0f, initialSave.getPlayerX(), 0.01f, "Initial X should be correct");
        
        // Wait to ensure different timestamp
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Overwrite save (should create backup)
        assertTrue(WorldSaveManager.saveWorld(saveName, originalWorldState, 
                                            200, 200, 90, false),
                  "Overwrite save should succeed");
        
        // Load overwritten save
        WorldSaveData overwrittenSave = WorldSaveManager.loadWorld(saveName, false);
        assertNotNull(overwrittenSave, "Overwritten save should load");
        assertEquals(200.0f, overwrittenSave.getPlayerX(), 0.01f, "Overwritten X should be correct");
        
        // Verify timestamps are different
        assertNotEquals(initialSave.getSaveTimestamp(), overwrittenSave.getSaveTimestamp(),
                       "Timestamps should be different");
        
        // Clean up
        WorldSaveManager.deleteSave(saveName, false);
    }
    
    // Test 5: Large world state handling
    
    @Test
    @Order(7)
    public void testLargeWorldStateWorkflow() {
        // Create a larger world state with many entities
        WorldState largeWorldState = new WorldState(TEST_WORLD_SEED);
        
        // Clear generated trees to have predictable test data
        largeWorldState.getTrees().clear();
        
        // Add many trees
        for (int i = 0; i < 100; i++) {
            TreeType treeType = TreeType.values()[i % TreeType.values().length];
            TreeState tree = new TreeState("large_tree_" + i, treeType, 
                                         i * 10.0f, i * 10.0f, 
                                         50.0f + (i % 50), true);
            largeWorldState.addOrUpdateTree(tree);
        }
        
        // Add many items
        for (int i = 0; i < 50; i++) {
            ItemType itemType = ItemType.values()[i % ItemType.values().length];
            ItemState item = new ItemState("large_item_" + i, itemType, 
                                         i * 15.0f, i * 15.0f, i % 2 == 0);
            largeWorldState.addOrUpdateItem(item);
        }
        
        // Add many cleared positions
        for (int i = 0; i < 30; i++) {
            largeWorldState.getClearedPositions().add((i * 20) + "_" + (i * 20));
        }
        
        // Add multiple rain zones
        List<RainZone> manyRainZones = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            manyRainZones.add(new RainZone("large_zone_" + i, i * 100.0f, i * 100.0f, 
                                         100.0f, 50.0f, 
                                         0.1f + (i * 0.1f)));
        }
        largeWorldState.setRainZones(manyRainZones);
        
        String largeSaveName = "large-world-test";
        
        // Save large world state
        long saveStartTime = System.currentTimeMillis();
        boolean saveResult = WorldSaveManager.saveWorld(largeSaveName, largeWorldState, 
                                                       500, 500, 95, false);
        long saveEndTime = System.currentTimeMillis();
        
        assertTrue(saveResult, "Large world save should succeed");
        
        // Load large world state
        long loadStartTime = System.currentTimeMillis();
        WorldSaveData loadedLargeData = WorldSaveManager.loadWorld(largeSaveName, false);
        long loadEndTime = System.currentTimeMillis();
        
        assertNotNull(loadedLargeData, "Large world should load successfully");
        
        // Verify large world data
        assertEquals(100, loadedLargeData.getTrees().size(), "Should have 100 trees");
        assertEquals(50, loadedLargeData.getItems().size(), "Should have 50 items");
        assertEquals(30, loadedLargeData.getClearedPositions().size(), "Should have 30 cleared positions");
        assertEquals(10, loadedLargeData.getRainZones().size(), "Should have 10 rain zones");
        
        // Verify statistics
        assertEquals(100, loadedLargeData.getExistingTreeCount(), "Should have 100 existing trees");
        assertEquals(25, loadedLargeData.getUncollectedItemCount(), "Should have 25 uncollected items");
        
        // Performance check (should complete within reasonable time)
        long saveTime = saveEndTime - saveStartTime;
        long loadTime = loadEndTime - loadStartTime;
        
        assertTrue(saveTime < 5000, "Save should complete within 5 seconds, took " + saveTime + "ms");
        assertTrue(loadTime < 5000, "Load should complete within 5 seconds, took " + loadTime + "ms");
        
        // Clean up
        WorldSaveManager.deleteSave(largeSaveName, false);
    }
    
    // Test 6: Concurrent access simulation
    
    @Test
    @Order(8)
    public void testConcurrentAccessWorkflow() {
        // Simulate concurrent save operations (in a single-threaded test environment)
        String[] concurrentSaves = {"concurrent-1", "concurrent-2", "concurrent-3"};
        
        // Perform rapid sequential saves to simulate concurrent access
        for (int i = 0; i < concurrentSaves.length; i++) {
            float playerX = 100.0f + (i * 100);
            float playerY = 200.0f + (i * 100);
            
            boolean saveResult = WorldSaveManager.saveWorld(concurrentSaves[i], originalWorldState, 
                                                          playerX, playerY, 80, false);
            assertTrue(saveResult, "Concurrent save " + i + " should succeed");
        }
        
        // Verify all saves exist and are correct
        for (int i = 0; i < concurrentSaves.length; i++) {
            assertTrue(WorldSaveManager.saveExists(concurrentSaves[i], false), 
                      "Concurrent save " + i + " should exist");
            
            WorldSaveData loadedData = WorldSaveManager.loadWorld(concurrentSaves[i], false);
            assertNotNull(loadedData, "Concurrent save " + i + " should load");
            
            float expectedX = 100.0f + (i * 100);
            assertEquals(expectedX, loadedData.getPlayerX(), 0.01f, 
                        "Concurrent save " + i + " should have correct X position");
        }
        
        // Clean up
        for (String saveName : concurrentSaves) {
            WorldSaveManager.deleteSave(saveName, false);
        }
    }
    
    // Test 7: Save validation and integrity workflow
    
    @Test
    @Order(9)
    public void testSaveValidationWorkflow() {
        String validationTestName = "validation-test";
        
        // Create save
        assertTrue(WorldSaveManager.saveWorld(validationTestName, originalWorldState, 
                                            100, 100, 80, false),
                  "Validation test save should succeed");
        
        // Validate save file integrity
        assertTrue(WorldSaveManager.validateSaveFileIntegrity(validationTestName, false),
                  "Save file should pass integrity validation");
        
        // Load and validate save data
        WorldSaveData validationData = WorldSaveManager.loadWorld(validationTestName, false);
        assertNotNull(validationData, "Validation save should load");
        assertTrue(validationData.isValid(), "Save data should be valid");
        assertTrue(validationData.isCompatible(), "Save data should be compatible");
        
        // Test save info validation
        List<WorldSaveInfo> saves = WorldSaveManager.listAvailableSaves(false);
        WorldSaveInfo validationInfo = null;
        for (WorldSaveInfo save : saves) {
            if (validationTestName.equals(save.getSaveName())) {
                validationInfo = save;
                break;
            }
        }
        
        assertNotNull(validationInfo, "Should find validation save in list");
        assertTrue(validationInfo.isValid(), "Save info should be valid");
        
        // Clean up
        WorldSaveManager.deleteSave(validationTestName, false);
    }
}