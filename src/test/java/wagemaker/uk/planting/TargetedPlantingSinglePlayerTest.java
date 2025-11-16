package wagemaker.uk.planting;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import wagemaker.uk.biome.BiomeManager;
import wagemaker.uk.biome.BiomeType;
import wagemaker.uk.inventory.InventoryManager;
import wagemaker.uk.player.Player;
import wagemaker.uk.trees.BambooTree;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.Gdx;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/**
 * Integration test for targeted bamboo planting in single-player mode.
 * NOTE: Disabled - requires OpenGL context not available in headless tests
 */
@Disabled("Requires OpenGL context")
public class TargetedPlantingSinglePlayerTest {
    
    private PlantingSystem plantingSystem;
    private InventoryManager inventoryManager;
    private BiomeManager biomeManager;
    private Map<String, PlantedBamboo> plantedBamboos;
    private Map<String, BambooTree> bambooTrees;
    private Player player;
    
    @BeforeAll
    public static void checkLibGDXAvailability() {
        // Skip all tests if LibGDX is not initialized (headless environment)
        assumeTrue(Gdx.files != null, "LibGDX not initialized - skipping tests that require graphics context");
    }
    
    @BeforeEach
    public void setUp() {
        plantingSystem = new PlantingSystem();
        
        // Create player and inventory manager
        OrthographicCamera camera = new OrthographicCamera();
        player = new Player(0, 0, camera);
        inventoryManager = new InventoryManager(player);
        player.setInventoryManager(inventoryManager);
        
        // Create a simple biome manager that returns SAND for test coordinates
        biomeManager = new BiomeManager() {
            @Override
            public BiomeType getBiomeAtPosition(float x, float y) {
                // Return SAND for our test coordinates
                return BiomeType.SAND;
            }
        };
        
        plantedBamboos = new HashMap<>();
        bambooTrees = new HashMap<>();
        
        // Add baby bamboo to inventory for testing
        inventoryManager.getCurrentInventory().addBabyBamboo(5);
    }
    
    @Test
    public void testPlantingSystemCalledWithTargetCoordinates() {
        // Arrange: Player at (0, 0), target at (64, 64)
        float targetX = 64.0f;
        float targetY = 64.0f;
        
        int initialBabyBambooCount = inventoryManager.getCurrentInventory().getBabyBambooCount();
        
        // Act: Attempt to plant at target coordinates
        PlantedBamboo result = plantingSystem.attemptPlant(
            targetX, targetY,
            inventoryManager,
            biomeManager,
            plantedBamboos,
            bambooTrees
        );
        
        // Assert: Planting should succeed
        assertNotNull(result, "PlantedBamboo should be created");
        assertEquals(64.0f, result.getX(), 0.01f, "Planted bamboo X should match target");
        assertEquals(64.0f, result.getY(), 0.01f, "Planted bamboo Y should match target");
        
        // Verify inventory was deducted
        int finalBabyBambooCount = inventoryManager.getCurrentInventory().getBabyBambooCount();
        assertEquals(initialBabyBambooCount - 1, finalBabyBambooCount, 
            "Baby bamboo count should decrease by 1");
    }
    
    @Test
    public void testPlantedBambooAddedToGameWorld() {
        // Arrange
        float targetX = 128.0f;
        float targetY = 192.0f;
        
        // Act: Plant bamboo and add to game world (simulating Player.executePlanting)
        PlantedBamboo plantedBamboo = plantingSystem.attemptPlant(
            targetX, targetY,
            inventoryManager,
            biomeManager,
            plantedBamboos,
            bambooTrees
        );
        
        // Simulate adding to game world
        if (plantedBamboo != null) {
            float tileX = (float) (Math.floor(targetX / 64.0) * 64.0);
            float tileY = (float) (Math.floor(targetY / 64.0) * 64.0);
            String key = "planted-bamboo-" + (int)tileX + "-" + (int)tileY;
            plantedBamboos.put(key, plantedBamboo);
        }
        
        // Assert: Planted bamboo should be in game world
        assertNotNull(plantedBamboo, "PlantedBamboo should be created");
        assertEquals(1, plantedBamboos.size(), "Game world should have 1 planted bamboo");
        
        String expectedKey = "planted-bamboo-128-192";
        assertTrue(plantedBamboos.containsKey(expectedKey), 
            "Game world should contain planted bamboo at expected key");
        
        PlantedBamboo worldBamboo = plantedBamboos.get(expectedKey);
        assertEquals(128.0f, worldBamboo.getX(), 0.01f, "World bamboo X should match");
        assertEquals(192.0f, worldBamboo.getY(), 0.01f, "World bamboo Y should match");
    }
    
    @Test
    public void testInventoryDeductionAfterSuccessfulPlanting() {
        // Arrange
        float targetX = 256.0f;
        float targetY = 320.0f;
        
        int initialCount = inventoryManager.getCurrentInventory().getBabyBambooCount();
        assertEquals(5, initialCount, "Should start with 5 baby bamboos");
        
        // Act: Plant bamboo
        PlantedBamboo result = plantingSystem.attemptPlant(
            targetX, targetY,
            inventoryManager,
            biomeManager,
            plantedBamboos,
            bambooTrees
        );
        
        // Assert: Inventory should be deducted
        assertNotNull(result, "Planting should succeed");
        int finalCount = inventoryManager.getCurrentInventory().getBabyBambooCount();
        assertEquals(4, finalCount, "Should have 4 baby bamboos after planting");
    }
    
    @Test
    public void testPlantingFailsWithNoInventory() {
        // Arrange: Remove all baby bamboo from inventory
        inventoryManager.getCurrentInventory().removeBabyBamboo(5);
        assertEquals(0, inventoryManager.getCurrentInventory().getBabyBambooCount(), 
            "Should have 0 baby bamboos");
        
        float targetX = 64.0f;
        float targetY = 64.0f;
        
        // Act: Attempt to plant
        PlantedBamboo result = plantingSystem.attemptPlant(
            targetX, targetY,
            inventoryManager,
            biomeManager,
            plantedBamboos,
            bambooTrees
        );
        
        // Assert: Planting should fail
        assertNull(result, "Planting should fail with no baby bamboo in inventory");
    }
    
    @Test
    public void testPlantingFailsOnOccupiedTile() {
        // Arrange: Plant bamboo at target location first
        float targetX = 64.0f;
        float targetY = 64.0f;
        
        PlantedBamboo firstPlant = plantingSystem.attemptPlant(
            targetX, targetY,
            inventoryManager,
            biomeManager,
            plantedBamboos,
            bambooTrees
        );
        
        assertNotNull(firstPlant, "First planting should succeed");
        
        // Add to game world
        String key = "planted-bamboo-64-64";
        plantedBamboos.put(key, firstPlant);
        
        // Act: Attempt to plant at same location
        PlantedBamboo secondPlant = plantingSystem.attemptPlant(
            targetX, targetY,
            inventoryManager,
            biomeManager,
            plantedBamboos,
            bambooTrees
        );
        
        // Assert: Second planting should fail
        assertNull(secondPlant, "Planting should fail on occupied tile");
        
        // Verify inventory was only deducted once
        assertEquals(4, inventoryManager.getCurrentInventory().getBabyBambooCount(), 
            "Should have 4 baby bamboos (only first planting deducted)");
    }
    
    @Test
    public void testMultiplePlantingsAtDifferentLocations() {
        // Arrange: Multiple target locations
        float[][] targets = {
            {64.0f, 64.0f},
            {128.0f, 64.0f},
            {64.0f, 128.0f}
        };
        
        int initialCount = inventoryManager.getCurrentInventory().getBabyBambooCount();
        
        // Act: Plant at each location
        for (float[] target : targets) {
            PlantedBamboo planted = plantingSystem.attemptPlant(
                target[0], target[1],
                inventoryManager,
                biomeManager,
                plantedBamboos,
                bambooTrees
            );
            
            assertNotNull(planted, "Planting should succeed at " + target[0] + ", " + target[1]);
            
            // Add to game world
            float tileX = (float) (Math.floor(target[0] / 64.0) * 64.0);
            float tileY = (float) (Math.floor(target[1] / 64.0) * 64.0);
            String key = "planted-bamboo-" + (int)tileX + "-" + (int)tileY;
            plantedBamboos.put(key, planted);
        }
        
        // Assert: All plantings should be in game world
        assertEquals(3, plantedBamboos.size(), "Should have 3 planted bamboos");
        
        // Verify inventory was deducted correctly
        int finalCount = inventoryManager.getCurrentInventory().getBabyBambooCount();
        assertEquals(initialCount - 3, finalCount, "Should have deducted 3 baby bamboos");
    }
    
    @Test
    public void testPlantingFailsOnInvalidBiomeType() {
        // Arrange: Create a biome manager that returns GRASS (invalid for bamboo)
        BiomeManager grassBiomeManager = new BiomeManager() {
            @Override
            public BiomeType getBiomeAtPosition(float x, float y) {
                return BiomeType.GRASS; // Bamboo can only be planted on SAND
            }
        };
        
        float targetX = 64.0f;
        float targetY = 64.0f;
        
        int initialCount = inventoryManager.getCurrentInventory().getBabyBambooCount();
        
        // Act: Attempt to plant on grass tile
        PlantedBamboo result = plantingSystem.attemptPlant(
            targetX, targetY,
            inventoryManager,
            grassBiomeManager,
            plantedBamboos,
            bambooTrees
        );
        
        // Assert: Planting should fail
        assertNull(result, "Planting should fail on GRASS biome (only SAND is valid)");
        
        // Verify inventory was not deducted
        int finalCount = inventoryManager.getCurrentInventory().getBabyBambooCount();
        assertEquals(initialCount, finalCount, "Inventory should not be deducted on failed planting");
    }
    
    @Test
    public void testTargetingModeDeactivatesAfterPlanting() {
        // Arrange: Activate targeting mode
        float playerX = 0.0f;
        float playerY = 0.0f;
        float targetX = 64.0f;
        float targetY = 64.0f;
        
        wagemaker.uk.targeting.TargetingSystem targetingSystem = player.getTargetingSystem();
        
        // Track if callback was invoked
        final boolean[] callbackInvoked = {false};
        final PlantedBamboo[] plantedBamboo = {null};
        
        // Activate targeting mode with callback
        targetingSystem.activate(playerX, playerY, wagemaker.uk.targeting.TargetingMode.ADJACENT, 
            new wagemaker.uk.targeting.TargetingCallback() {
                @Override
                public void onTargetConfirmed(float confirmedX, float confirmedY) {
                    callbackInvoked[0] = true;
                    
                    // Execute planting at target coordinates
                    plantedBamboo[0] = plantingSystem.attemptPlant(
                        confirmedX, confirmedY,
                        inventoryManager,
                        biomeManager,
                        plantedBamboos,
                        bambooTrees
                    );
                }
                
                @Override
                public void onTargetCancelled() {
                    // Not used in this test
                }
            });
        
        assertTrue(targetingSystem.isActive(), "Targeting should be active after activation");
        
        // Move target to (64, 64)
        targetingSystem.moveTarget(wagemaker.uk.targeting.Direction.RIGHT);
        targetingSystem.moveTarget(wagemaker.uk.targeting.Direction.UP);
        
        // Act: Confirm target (this should trigger planting and deactivate targeting)
        targetingSystem.confirmTarget();
        
        // Assert: Targeting mode should be deactivated
        assertFalse(targetingSystem.isActive(), "Targeting should be deactivated after confirmation");
        assertTrue(callbackInvoked[0], "Callback should have been invoked");
        assertNotNull(plantedBamboo[0], "Bamboo should have been planted");
        assertEquals(64.0f, plantedBamboo[0].getX(), 0.01f, "Planted bamboo X should match target");
        assertEquals(64.0f, plantedBamboo[0].getY(), 0.01f, "Planted bamboo Y should match target");
    }
    
    @Test
    public void testTargetingModeDeactivatesOnCancellation() {
        // Arrange: Activate targeting mode
        float playerX = 0.0f;
        float playerY = 0.0f;
        
        wagemaker.uk.targeting.TargetingSystem targetingSystem = player.getTargetingSystem();
        
        // Track if callback was invoked
        final boolean[] cancelCallbackInvoked = {false};
        
        // Activate targeting mode with callback
        targetingSystem.activate(playerX, playerY, wagemaker.uk.targeting.TargetingMode.ADJACENT, 
            new wagemaker.uk.targeting.TargetingCallback() {
                @Override
                public void onTargetConfirmed(float confirmedX, float confirmedY) {
                    // Not used in this test
                }
                
                @Override
                public void onTargetCancelled() {
                    cancelCallbackInvoked[0] = true;
                }
            });
        
        assertTrue(targetingSystem.isActive(), "Targeting should be active after activation");
        
        // Act: Cancel targeting
        targetingSystem.cancel();
        
        // Assert: Targeting mode should be deactivated
        assertFalse(targetingSystem.isActive(), "Targeting should be deactivated after cancellation");
        assertTrue(cancelCallbackInvoked[0], "Cancel callback should have been invoked");
    }
    
    @Test
    public void testPlantingOnOccupiedTileWithBambooTree() {
        // Arrange: Add a bamboo tree at target location
        float targetX = 64.0f;
        float targetY = 64.0f;
        
        BambooTree bambooTree = new BambooTree(targetX, targetY);
        String key = "planted-bamboo-64-64";
        bambooTrees.put(key, bambooTree);
        
        int initialCount = inventoryManager.getCurrentInventory().getBabyBambooCount();
        
        // Act: Attempt to plant at same location
        PlantedBamboo result = plantingSystem.attemptPlant(
            targetX, targetY,
            inventoryManager,
            biomeManager,
            plantedBamboos,
            bambooTrees
        );
        
        // Assert: Planting should fail
        assertNull(result, "Planting should fail on tile occupied by bamboo tree");
        
        // Verify inventory was not deducted
        int finalCount = inventoryManager.getCurrentInventory().getBabyBambooCount();
        assertEquals(initialCount, finalCount, "Inventory should not be deducted on failed planting");
    }
}
