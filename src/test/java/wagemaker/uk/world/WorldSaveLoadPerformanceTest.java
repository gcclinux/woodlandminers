package wagemaker.uk.world;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import wagemaker.uk.network.WorldState;
import wagemaker.uk.network.TreeState;
import wagemaker.uk.network.TreeType;
import wagemaker.uk.network.ItemState;
import wagemaker.uk.network.ItemType;
import wagemaker.uk.weather.RainZone;

import java.util.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * Performance tests for world save/load system with large datasets.
 * Tests save/load performance with large numbers of trees and items,
 * memory usage during serialization operations, and UI responsiveness with many save files.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WorldSaveLoadPerformanceTest {
    
    private static final long PERFORMANCE_WORLD_SEED = 555666777L;
    
    // Performance thresholds (in milliseconds)
    private static final long SMALL_WORLD_SAVE_THRESHOLD = 1000;   // 1 second
    private static final long SMALL_WORLD_LOAD_THRESHOLD = 1000;   // 1 second
    private static final long MEDIUM_WORLD_SAVE_THRESHOLD = 3000;  // 3 seconds
    private static final long MEDIUM_WORLD_LOAD_THRESHOLD = 3000;  // 3 seconds
    private static final long LARGE_WORLD_SAVE_THRESHOLD = 10000;  // 10 seconds
    private static final long LARGE_WORLD_LOAD_THRESHOLD = 10000;  // 10 seconds
    
    // Memory thresholds (in MB)
    private static final long MEMORY_USAGE_THRESHOLD = 100; // 100 MB
    
    private MemoryMXBean memoryBean;
    
    @BeforeEach
    public void setUp() {
        memoryBean = ManagementFactory.getMemoryMXBean();
        
        // Clean up any existing performance test saves
        cleanupPerformanceTestSaves();
        
        // Force garbage collection before tests
        System.gc();
        try {
            Thread.sleep(100); // Give GC time to complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up performance test saves
        cleanupPerformanceTestSaves();
        
        // Force garbage collection after tests
        System.gc();
    }
    
    /**
     * Cleans up performance test save files.
     */
    private void cleanupPerformanceTestSaves() {
        try {
            String[] testPrefixes = {"perf-small", "perf-medium", "perf-large", "perf-memory", "perf-ui"};
            
            for (String prefix : testPrefixes) {
                for (int i = 0; i < 100; i++) { // Clean up numbered saves
                    String saveName = prefix + "-" + i;
                    if (WorldSaveManager.saveExists(saveName, false)) {
                        WorldSaveManager.deleteSave(saveName, false);
                    }
                    if (WorldSaveManager.saveExists(saveName, true)) {
                        WorldSaveManager.deleteSave(saveName, true);
                    }
                }
                
                // Clean up base names
                if (WorldSaveManager.saveExists(prefix, false)) {
                    WorldSaveManager.deleteSave(prefix, false);
                }
                if (WorldSaveManager.saveExists(prefix, true)) {
                    WorldSaveManager.deleteSave(prefix, true);
                }
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
    
    /**
     * Creates a world state with the specified number of entities.
     */
    private WorldState createWorldStateWithSize(int treeCount, int itemCount, int clearedCount, int rainZoneCount) {
        WorldState worldState = new WorldState(PERFORMANCE_WORLD_SEED);
        
        // Clear generated trees to have predictable test data
        worldState.getTrees().clear();
        
        // Add trees
        for (int i = 0; i < treeCount; i++) {
            TreeType treeType = TreeType.values()[i % TreeType.values().length];
            float health = (i % 2 == 0) ? 100.0f : 50.0f + (i % 50);
            boolean exists = i % 10 != 0; // 10% destroyed trees
            
            TreeState tree = new TreeState("perf_tree_" + i, treeType, 
                                         i * 5.0f, (i / 10) * 5.0f, health, exists);
            worldState.addOrUpdateTree(tree);
        }
        
        // Add items
        for (int i = 0; i < itemCount; i++) {
            ItemType itemType = ItemType.values()[i % ItemType.values().length];
            boolean collected = i % 3 == 0; // 33% collected items
            
            ItemState item = new ItemState("perf_item_" + i, itemType, 
                                         i * 7.0f, (i / 8) * 7.0f, collected);
            worldState.addOrUpdateItem(item);
        }
        
        // Add cleared positions
        for (int i = 0; i < clearedCount; i++) {
            worldState.getClearedPositions().add((i * 10) + "_" + (i * 10));
        }
        
        // Add rain zones
        List<RainZone> rainZones = new ArrayList<>();
        for (int i = 0; i < rainZoneCount; i++) {
            float centerX = i * 50.0f;
            float centerY = i * 50.0f;
            float radius = 50.0f;
            float fadeDistance = 25.0f;
            float intensity = 0.1f + ((i % 9) * 0.1f);
            
            rainZones.add(new RainZone("perf_zone_" + i, centerX, centerY, radius, fadeDistance, intensity));
        }
        worldState.setRainZones(rainZones);
        
        return worldState;
    }
    
    /**
     * Measures memory usage before and after an operation.
     */
    private MemoryUsage getMemoryUsage() {
        return memoryBean.getHeapMemoryUsage();
    }
    
    /**
     * Converts bytes to megabytes.
     */
    private long bytesToMB(long bytes) {
        return bytes / (1024 * 1024);
    }
    
    // Test 1: Small world performance (baseline)
    
    @Test
    @Order(1)
    public void testSmallWorldPerformance() {
        // Small world: 100 trees, 50 items, 20 cleared positions, 5 rain zones
        WorldState smallWorld = createWorldStateWithSize(100, 50, 20, 5);
        String saveName = "perf-small";
        
        // Measure save performance
        MemoryUsage beforeSave = getMemoryUsage();
        long saveStartTime = System.currentTimeMillis();
        
        boolean saveResult = WorldSaveManager.saveWorld(saveName, smallWorld, 
                                                       100, 100, 80, false);
        
        long saveEndTime = System.currentTimeMillis();
        MemoryUsage afterSave = getMemoryUsage();
        
        assertTrue(saveResult, "Small world save should succeed");
        
        long saveTime = saveEndTime - saveStartTime;
        long saveMemoryUsed = bytesToMB(afterSave.getUsed() - beforeSave.getUsed());
        
        System.out.println("Small World Save Performance:");
        System.out.println("  Time: " + saveTime + "ms");
        System.out.println("  Memory used: " + saveMemoryUsed + "MB");
        
        assertTrue(saveTime < SMALL_WORLD_SAVE_THRESHOLD, 
                  "Small world save should complete within " + SMALL_WORLD_SAVE_THRESHOLD + "ms, took " + saveTime + "ms");
        
        // Measure load performance
        System.gc(); // Clean up before load test
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        MemoryUsage beforeLoad = getMemoryUsage();
        long loadStartTime = System.currentTimeMillis();
        
        WorldSaveData loadedData = WorldSaveManager.loadWorld(saveName, false);
        
        long loadEndTime = System.currentTimeMillis();
        MemoryUsage afterLoad = getMemoryUsage();
        
        assertNotNull(loadedData, "Small world should load successfully");
        
        long loadTime = loadEndTime - loadStartTime;
        long loadMemoryUsed = bytesToMB(afterLoad.getUsed() - beforeLoad.getUsed());
        
        System.out.println("Small World Load Performance:");
        System.out.println("  Time: " + loadTime + "ms");
        System.out.println("  Memory used: " + loadMemoryUsed + "MB");
        
        assertTrue(loadTime < SMALL_WORLD_LOAD_THRESHOLD, 
                  "Small world load should complete within " + SMALL_WORLD_LOAD_THRESHOLD + "ms, took " + loadTime + "ms");
        
        // Verify data integrity
        assertEquals(100, loadedData.getTrees().size(), "Should have 100 trees");
        assertEquals(50, loadedData.getItems().size(), "Should have 50 items");
        assertEquals(20, loadedData.getClearedPositions().size(), "Should have 20 cleared positions");
        assertEquals(5, loadedData.getRainZones().size(), "Should have 5 rain zones");
    }
    
    // Test 2: Medium world performance
    
    @Test
    @Order(2)
    public void testMediumWorldPerformance() {
        // Medium world: 1000 trees, 500 items, 200 cleared positions, 20 rain zones
        WorldState mediumWorld = createWorldStateWithSize(1000, 500, 200, 20);
        String saveName = "perf-medium";
        
        // Measure save performance
        MemoryUsage beforeSave = getMemoryUsage();
        long saveStartTime = System.currentTimeMillis();
        
        boolean saveResult = WorldSaveManager.saveWorld(saveName, mediumWorld, 
                                                       500, 500, 85, false);
        
        long saveEndTime = System.currentTimeMillis();
        MemoryUsage afterSave = getMemoryUsage();
        
        assertTrue(saveResult, "Medium world save should succeed");
        
        long saveTime = saveEndTime - saveStartTime;
        long saveMemoryUsed = bytesToMB(afterSave.getUsed() - beforeSave.getUsed());
        
        System.out.println("Medium World Save Performance:");
        System.out.println("  Time: " + saveTime + "ms");
        System.out.println("  Memory used: " + saveMemoryUsed + "MB");
        
        assertTrue(saveTime < MEDIUM_WORLD_SAVE_THRESHOLD, 
                  "Medium world save should complete within " + MEDIUM_WORLD_SAVE_THRESHOLD + "ms, took " + saveTime + "ms");
        
        // Measure load performance
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        MemoryUsage beforeLoad = getMemoryUsage();
        long loadStartTime = System.currentTimeMillis();
        
        WorldSaveData loadedData = WorldSaveManager.loadWorld(saveName, false);
        
        long loadEndTime = System.currentTimeMillis();
        MemoryUsage afterLoad = getMemoryUsage();
        
        assertNotNull(loadedData, "Medium world should load successfully");
        
        long loadTime = loadEndTime - loadStartTime;
        long loadMemoryUsed = bytesToMB(afterLoad.getUsed() - beforeLoad.getUsed());
        
        System.out.println("Medium World Load Performance:");
        System.out.println("  Time: " + loadTime + "ms");
        System.out.println("  Memory used: " + loadMemoryUsed + "MB");
        
        assertTrue(loadTime < MEDIUM_WORLD_LOAD_THRESHOLD, 
                  "Medium world load should complete within " + MEDIUM_WORLD_LOAD_THRESHOLD + "ms, took " + loadTime + "ms");
        
        // Verify data integrity
        assertEquals(1000, loadedData.getTrees().size(), "Should have 1000 trees");
        assertEquals(500, loadedData.getItems().size(), "Should have 500 items");
        assertEquals(200, loadedData.getClearedPositions().size(), "Should have 200 cleared positions");
        assertEquals(20, loadedData.getRainZones().size(), "Should have 20 rain zones");
    }
    
    // Test 3: Large world performance
    
    @Test
    @Order(3)
    public void testLargeWorldPerformance() {
        // Large world: 5000 trees, 2000 items, 1000 cleared positions, 50 rain zones
        WorldState largeWorld = createWorldStateWithSize(5000, 2000, 1000, 50);
        String saveName = "perf-large";
        
        // Measure save performance
        MemoryUsage beforeSave = getMemoryUsage();
        long saveStartTime = System.currentTimeMillis();
        
        boolean saveResult = WorldSaveManager.saveWorld(saveName, largeWorld, 
                                                       1000, 1000, 90, false);
        
        long saveEndTime = System.currentTimeMillis();
        MemoryUsage afterSave = getMemoryUsage();
        
        assertTrue(saveResult, "Large world save should succeed");
        
        long saveTime = saveEndTime - saveStartTime;
        long saveMemoryUsed = bytesToMB(afterSave.getUsed() - beforeSave.getUsed());
        
        System.out.println("Large World Save Performance:");
        System.out.println("  Time: " + saveTime + "ms");
        System.out.println("  Memory used: " + saveMemoryUsed + "MB");
        
        assertTrue(saveTime < LARGE_WORLD_SAVE_THRESHOLD, 
                  "Large world save should complete within " + LARGE_WORLD_SAVE_THRESHOLD + "ms, took " + saveTime + "ms");
        
        // Check memory usage is reasonable
        assertTrue(saveMemoryUsed < MEMORY_USAGE_THRESHOLD, 
                  "Save memory usage should be under " + MEMORY_USAGE_THRESHOLD + "MB, used " + saveMemoryUsed + "MB");
        
        // Measure load performance
        System.gc();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        MemoryUsage beforeLoad = getMemoryUsage();
        long loadStartTime = System.currentTimeMillis();
        
        WorldSaveData loadedData = WorldSaveManager.loadWorld(saveName, false);
        
        long loadEndTime = System.currentTimeMillis();
        MemoryUsage afterLoad = getMemoryUsage();
        
        assertNotNull(loadedData, "Large world should load successfully");
        
        long loadTime = loadEndTime - loadStartTime;
        long loadMemoryUsed = bytesToMB(afterLoad.getUsed() - beforeLoad.getUsed());
        
        System.out.println("Large World Load Performance:");
        System.out.println("  Time: " + loadTime + "ms");
        System.out.println("  Memory used: " + loadMemoryUsed + "MB");
        
        assertTrue(loadTime < LARGE_WORLD_LOAD_THRESHOLD, 
                  "Large world load should complete within " + LARGE_WORLD_LOAD_THRESHOLD + "ms, took " + loadTime + "ms");
        
        assertTrue(loadMemoryUsed < MEMORY_USAGE_THRESHOLD, 
                  "Load memory usage should be under " + MEMORY_USAGE_THRESHOLD + "MB, used " + loadMemoryUsed + "MB");
        
        // Verify data integrity
        assertEquals(5000, loadedData.getTrees().size(), "Should have 5000 trees");
        assertEquals(2000, loadedData.getItems().size(), "Should have 2000 items");
        assertEquals(1000, loadedData.getClearedPositions().size(), "Should have 1000 cleared positions");
        assertEquals(50, loadedData.getRainZones().size(), "Should have 50 rain zones");
        
        // Verify statistics calculation performance
        long statsStartTime = System.currentTimeMillis();
        int existingTrees = loadedData.getExistingTreeCount();
        int uncollectedItems = loadedData.getUncollectedItemCount();
        long statsEndTime = System.currentTimeMillis();
        
        long statsTime = statsEndTime - statsStartTime;
        System.out.println("Statistics calculation time: " + statsTime + "ms");
        
        assertTrue(statsTime < 1000, "Statistics calculation should be fast, took " + statsTime + "ms");
        assertTrue(existingTrees > 0, "Should have existing trees");
        assertTrue(uncollectedItems > 0, "Should have uncollected items");
    }
    
    // Test 4: Memory usage during serialization
    
    @Test
    @Order(4)
    public void testMemoryUsageDuringSerialization() {
        // Create multiple world states of different sizes
        WorldState[] worlds = {
            createWorldStateWithSize(100, 50, 20, 5),    // Small
            createWorldStateWithSize(500, 250, 100, 10), // Medium-small
            createWorldStateWithSize(1000, 500, 200, 20) // Medium
        };
        
        String[] saveNames = {"perf-memory-1", "perf-memory-2", "perf-memory-3"};
        
        for (int i = 0; i < worlds.length; i++) {
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            MemoryUsage beforeOperation = getMemoryUsage();
            long baselineMemory = beforeOperation.getUsed();
            
            // Perform save operation
            boolean saveResult = WorldSaveManager.saveWorld(saveNames[i], worlds[i], 
                                                          100 * (i + 1), 100 * (i + 1), 80, false);
            assertTrue(saveResult, "Memory test save " + i + " should succeed");
            
            MemoryUsage afterSave = getMemoryUsage();
            long peakMemoryAfterSave = afterSave.getUsed();
            
            // Perform load operation
            WorldSaveData loadedData = WorldSaveManager.loadWorld(saveNames[i], false);
            assertNotNull(loadedData, "Memory test load " + i + " should succeed");
            
            MemoryUsage afterLoad = getMemoryUsage();
            long peakMemoryAfterLoad = afterLoad.getUsed();
            
            // Calculate memory usage
            long saveMemoryIncrease = bytesToMB(peakMemoryAfterSave - baselineMemory);
            long loadMemoryIncrease = bytesToMB(peakMemoryAfterLoad - baselineMemory);
            
            System.out.println("Memory Test " + i + " (World size: " + worlds[i].getTrees().size() + " trees):");
            System.out.println("  Save memory increase: " + saveMemoryIncrease + "MB");
            System.out.println("  Load memory increase: " + loadMemoryIncrease + "MB");
            
            // Memory usage should be reasonable relative to world size
            assertTrue(saveMemoryIncrease < MEMORY_USAGE_THRESHOLD, 
                      "Save memory increase should be reasonable: " + saveMemoryIncrease + "MB");
            assertTrue(loadMemoryIncrease < MEMORY_USAGE_THRESHOLD, 
                      "Load memory increase should be reasonable: " + loadMemoryIncrease + "MB");
        }
    }
    
    // Test 5: UI responsiveness with many save files
    
    @Test
    @Order(5)
    public void testUIResponsivenessWithManySaveFiles() {
        // Create many save files to test listing performance
        int saveFileCount = 50;
        WorldState testWorld = createWorldStateWithSize(100, 50, 20, 5);
        
        System.out.println("Creating " + saveFileCount + " save files for UI responsiveness test...");
        
        // Create many save files
        long createStartTime = System.currentTimeMillis();
        for (int i = 0; i < saveFileCount; i++) {
            String saveName = "perf-ui-" + i;
            boolean saveResult = WorldSaveManager.saveWorld(saveName, testWorld, 
                                                          i * 10.0f, i * 10.0f, 80, false);
            assertTrue(saveResult, "UI test save " + i + " should succeed");
        }
        long createEndTime = System.currentTimeMillis();
        
        long createTime = createEndTime - createStartTime;
        System.out.println("Created " + saveFileCount + " saves in " + createTime + "ms");
        
        // Test save listing performance
        MemoryUsage beforeListing = getMemoryUsage();
        long listStartTime = System.currentTimeMillis();
        
        List<WorldSaveInfo> saves = WorldSaveManager.listAvailableSaves(false);
        
        long listEndTime = System.currentTimeMillis();
        MemoryUsage afterListing = getMemoryUsage();
        
        assertNotNull(saves, "Save list should not be null");
        
        // Count our test saves
        int foundTestSaves = 0;
        for (WorldSaveInfo save : saves) {
            if (save.getSaveName().startsWith("perf-ui-")) {
                foundTestSaves++;
                
                // Verify save info is complete and valid
                assertTrue(save.isValid(), "Save info should be valid");
                assertNotNull(save.getFormattedTimestamp(), "Formatted timestamp should not be null");
                assertNotNull(save.getFormattedFileSize(), "Formatted file size should not be null");
                assertNotNull(save.getSummary(), "Summary should not be null");
            }
        }
        
        assertEquals(saveFileCount, foundTestSaves, "Should find all test saves");
        
        long listTime = listEndTime - listStartTime;
        long listMemoryUsed = bytesToMB(afterListing.getUsed() - beforeListing.getUsed());
        
        System.out.println("Save Listing Performance (" + saveFileCount + " files):");
        System.out.println("  Time: " + listTime + "ms");
        System.out.println("  Memory used: " + listMemoryUsed + "MB");
        
        // UI should remain responsive
        assertTrue(listTime < 2000, "Save listing should complete within 2 seconds, took " + listTime + "ms");
        assertTrue(listMemoryUsed < 50, "Listing memory usage should be reasonable: " + listMemoryUsed + "MB");
        
        // Test individual save info loading performance
        long infoStartTime = System.currentTimeMillis();
        for (WorldSaveInfo save : saves) {
            if (save.getSaveName().startsWith("perf-ui-")) {
                // Access formatted properties to test performance
                save.getFormattedTimestamp();
                save.getFormattedFileSize();
                save.getSummary();
                save.getFormattedPlayerPosition();
            }
        }
        long infoEndTime = System.currentTimeMillis();
        
        long infoTime = infoEndTime - infoStartTime;
        System.out.println("Save info formatting time: " + infoTime + "ms");
        
        assertTrue(infoTime < 1000, "Save info formatting should be fast, took " + infoTime + "ms");
    }
    
    // Test 6: Stress test with extreme world size
    
    @Test
    @Order(6)
    @Disabled("Stress test - enable manually for performance validation")
    public void testExtremeWorldSizeStressTest() {
        // Extreme world: 10000 trees, 5000 items, 2000 cleared positions, 100 rain zones
        // This test is disabled by default as it may take significant time and memory
        
        System.out.println("Starting extreme world size stress test...");
        
        WorldState extremeWorld = createWorldStateWithSize(10000, 5000, 2000, 100);
        String saveName = "perf-extreme";
        
        // Measure save performance
        MemoryUsage beforeSave = getMemoryUsage();
        long saveStartTime = System.currentTimeMillis();
        
        boolean saveResult = WorldSaveManager.saveWorld(saveName, extremeWorld, 
                                                       2000, 2000, 95, false);
        
        long saveEndTime = System.currentTimeMillis();
        MemoryUsage afterSave = getMemoryUsage();
        
        assertTrue(saveResult, "Extreme world save should succeed");
        
        long saveTime = saveEndTime - saveStartTime;
        long saveMemoryUsed = bytesToMB(afterSave.getUsed() - beforeSave.getUsed());
        
        System.out.println("Extreme World Save Performance:");
        System.out.println("  Time: " + saveTime + "ms");
        System.out.println("  Memory used: " + saveMemoryUsed + "MB");
        
        // More lenient thresholds for extreme test
        assertTrue(saveTime < 30000, "Extreme world save should complete within 30 seconds");
        assertTrue(saveMemoryUsed < 500, "Extreme save memory usage should be under 500MB");
        
        // Measure load performance
        System.gc();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        MemoryUsage beforeLoad = getMemoryUsage();
        long loadStartTime = System.currentTimeMillis();
        
        WorldSaveData loadedData = WorldSaveManager.loadWorld(saveName, false);
        
        long loadEndTime = System.currentTimeMillis();
        MemoryUsage afterLoad = getMemoryUsage();
        
        assertNotNull(loadedData, "Extreme world should load successfully");
        
        long loadTime = loadEndTime - loadStartTime;
        long loadMemoryUsed = bytesToMB(afterLoad.getUsed() - beforeLoad.getUsed());
        
        System.out.println("Extreme World Load Performance:");
        System.out.println("  Time: " + loadTime + "ms");
        System.out.println("  Memory used: " + loadMemoryUsed + "MB");
        
        assertTrue(loadTime < 30000, "Extreme world load should complete within 30 seconds");
        assertTrue(loadMemoryUsed < 500, "Extreme load memory usage should be under 500MB");
        
        // Verify data integrity
        assertEquals(10000, loadedData.getTrees().size(), "Should have 10000 trees");
        assertEquals(5000, loadedData.getItems().size(), "Should have 5000 items");
        assertEquals(2000, loadedData.getClearedPositions().size(), "Should have 2000 cleared positions");
        assertEquals(100, loadedData.getRainZones().size(), "Should have 100 rain zones");
        
        System.out.println("Extreme world stress test completed successfully");
    }
    
    // Test 7: Performance regression detection
    
    @Test
    @Order(7)
    public void testPerformanceRegression() {
        // This test establishes baseline performance metrics
        // In a CI environment, these could be compared against historical data
        
        WorldState baselineWorld = createWorldStateWithSize(1000, 500, 200, 20);
        String saveName = "perf-baseline";
        
        // Perform multiple iterations to get average performance
        int iterations = 3;
        long totalSaveTime = 0;
        long totalLoadTime = 0;
        
        for (int i = 0; i < iterations; i++) {
            String iterationSaveName = saveName + "-" + i;
            
            // Save performance
            long saveStart = System.currentTimeMillis();
            boolean saveResult = WorldSaveManager.saveWorld(iterationSaveName, baselineWorld, 
                                                          100, 100, 80, false);
            long saveEnd = System.currentTimeMillis();
            
            assertTrue(saveResult, "Baseline save iteration " + i + " should succeed");
            totalSaveTime += (saveEnd - saveStart);
            
            // Load performance
            long loadStart = System.currentTimeMillis();
            WorldSaveData loadedData = WorldSaveManager.loadWorld(iterationSaveName, false);
            long loadEnd = System.currentTimeMillis();
            
            assertNotNull(loadedData, "Baseline load iteration " + i + " should succeed");
            totalLoadTime += (loadEnd - loadStart);
            
            // Clean up iteration save
            WorldSaveManager.deleteSave(iterationSaveName, false);
        }
        
        long avgSaveTime = totalSaveTime / iterations;
        long avgLoadTime = totalLoadTime / iterations;
        
        System.out.println("Performance Baseline (1000 trees, 500 items, " + iterations + " iterations):");
        System.out.println("  Average save time: " + avgSaveTime + "ms");
        System.out.println("  Average load time: " + avgLoadTime + "ms");
        
        // These thresholds represent acceptable performance for the baseline world
        assertTrue(avgSaveTime < 2000, "Average save time should be under 2 seconds: " + avgSaveTime + "ms");
        assertTrue(avgLoadTime < 2000, "Average load time should be under 2 seconds: " + avgLoadTime + "ms");
        
        // Store these metrics for future regression testing
        // In a real CI environment, you would save these to a file or database
        System.setProperty("baseline.save.time", String.valueOf(avgSaveTime));
        System.setProperty("baseline.load.time", String.valueOf(avgLoadTime));
    }
}