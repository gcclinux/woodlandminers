package wagemaker.uk.inventory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import wagemaker.uk.player.Player;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.Gdx;

/**
 * Test to verify inventory isolation between single-player and multiplayer modes.
 * Ensures that single-player inventory is not affected by multiplayer gameplay
 * and vice versa.
 */
public class InventoryModeSwitchingTest {
    
    private Player player;
    private InventoryManager inventoryManager;
    
    @BeforeAll
    public static void checkLibGDXAvailability() {
        // Skip all tests if LibGDX is not initialized (headless environment)
        assumeTrue(Gdx.files != null, "LibGDX not initialized - skipping tests that require graphics context");
    }
    
    @BeforeEach
    public void setUp() {
        // Create a minimal player instance for testing
        OrthographicCamera camera = new OrthographicCamera();
        player = new Player(0, 0, camera);
        player.setHealth(100); // Start with full health
        
        // Create inventory manager
        inventoryManager = new InventoryManager(player);
        player.setInventoryManager(inventoryManager);
    }
    
    @Test
    public void testSinglePlayerInventoryNotAffectedByMultiplayerGameplay() {
        // Start in single-player mode (default)
        assertFalse(inventoryManager.isMultiplayerMode(), "Should start in single-player mode");
        
        // Add items to single-player inventory
        inventoryManager.getCurrentInventory().addApple(5);
        inventoryManager.getCurrentInventory().addBanana(3);
        inventoryManager.getCurrentInventory().addWoodStack(10);
        
        // Verify single-player inventory has items
        assertEquals(5, inventoryManager.getSingleplayerInventory().getAppleCount(), "SP should have 5 apples");
        assertEquals(3, inventoryManager.getSingleplayerInventory().getBananaCount(), "SP should have 3 bananas");
        assertEquals(10, inventoryManager.getSingleplayerInventory().getWoodStackCount(), "SP should have 10 wood stacks");
        
        // Switch to multiplayer mode
        inventoryManager.setMultiplayerMode(true);
        assertTrue(inventoryManager.isMultiplayerMode(), "Should be in multiplayer mode");
        
        // Verify multiplayer inventory is empty
        assertEquals(0, inventoryManager.getMultiplayerInventory().getAppleCount(), "MP should have 0 apples");
        assertEquals(0, inventoryManager.getMultiplayerInventory().getBananaCount(), "MP should have 0 bananas");
        assertEquals(0, inventoryManager.getMultiplayerInventory().getWoodStackCount(), "MP should have 0 wood stacks");
        
        // Add different items to multiplayer inventory
        inventoryManager.getCurrentInventory().addApple(2);
        inventoryManager.getCurrentInventory().addBambooStack(7);
        
        // Verify multiplayer inventory has new items
        assertEquals(2, inventoryManager.getMultiplayerInventory().getAppleCount(), "MP should have 2 apples");
        assertEquals(7, inventoryManager.getMultiplayerInventory().getBambooStackCount(), "MP should have 7 bamboo stacks");
        
        // Switch back to single-player mode
        inventoryManager.setMultiplayerMode(false);
        assertFalse(inventoryManager.isMultiplayerMode(), "Should be back in single-player mode");
        
        // Verify single-player inventory is unchanged
        assertEquals(5, inventoryManager.getSingleplayerInventory().getAppleCount(), "SP should still have 5 apples");
        assertEquals(3, inventoryManager.getSingleplayerInventory().getBananaCount(), "SP should still have 3 bananas");
        assertEquals(10, inventoryManager.getSingleplayerInventory().getWoodStackCount(), "SP should still have 10 wood stacks");
        assertEquals(0, inventoryManager.getSingleplayerInventory().getBambooStackCount(), "SP should still have 0 bamboo stacks");
    }
    
    @Test
    public void testMultiplayerInventoryNotAffectedBySinglePlayerGameplay() {
        // Start in multiplayer mode
        inventoryManager.setMultiplayerMode(true);
        assertTrue(inventoryManager.isMultiplayerMode(), "Should be in multiplayer mode");
        
        // Add items to multiplayer inventory
        inventoryManager.getCurrentInventory().addBanana(8);
        inventoryManager.getCurrentInventory().addBabyBamboo(15);
        
        // Verify multiplayer inventory has items
        assertEquals(8, inventoryManager.getMultiplayerInventory().getBananaCount(), "MP should have 8 bananas");
        assertEquals(15, inventoryManager.getMultiplayerInventory().getBabyBambooCount(), "MP should have 15 baby bamboos");
        
        // Switch to single-player mode
        inventoryManager.setMultiplayerMode(false);
        assertFalse(inventoryManager.isMultiplayerMode(), "Should be in single-player mode");
        
        // Verify single-player inventory is empty
        assertEquals(0, inventoryManager.getSingleplayerInventory().getBananaCount(), "SP should have 0 bananas");
        assertEquals(0, inventoryManager.getSingleplayerInventory().getBabyBambooCount(), "SP should have 0 baby bamboos");
        
        // Add different items to single-player inventory
        inventoryManager.getCurrentInventory().addApple(12);
        inventoryManager.getCurrentInventory().addWoodStack(6);
        
        // Verify single-player inventory has new items
        assertEquals(12, inventoryManager.getSingleplayerInventory().getAppleCount(), "SP should have 12 apples");
        assertEquals(6, inventoryManager.getSingleplayerInventory().getWoodStackCount(), "SP should have 6 wood stacks");
        
        // Switch back to multiplayer mode
        inventoryManager.setMultiplayerMode(true);
        assertTrue(inventoryManager.isMultiplayerMode(), "Should be back in multiplayer mode");
        
        // Verify multiplayer inventory is unchanged
        assertEquals(8, inventoryManager.getMultiplayerInventory().getBananaCount(), "MP should still have 8 bananas");
        assertEquals(15, inventoryManager.getMultiplayerInventory().getBabyBambooCount(), "MP should still have 15 baby bamboos");
        assertEquals(0, inventoryManager.getMultiplayerInventory().getAppleCount(), "MP should still have 0 apples");
        assertEquals(0, inventoryManager.getMultiplayerInventory().getWoodStackCount(), "MP should still have 0 wood stacks");
    }
    
    @Test
    public void testGetCurrentInventoryReturnsCorrectInventory() {
        // In single-player mode, getCurrentInventory should return single-player inventory
        inventoryManager.setMultiplayerMode(false);
        inventoryManager.getCurrentInventory().addApple(5);
        assertEquals(5, inventoryManager.getSingleplayerInventory().getAppleCount(), 
                    "Current inventory should be SP inventory");
        assertEquals(0, inventoryManager.getMultiplayerInventory().getAppleCount(), 
                    "MP inventory should be empty");
        
        // In multiplayer mode, getCurrentInventory should return multiplayer inventory
        inventoryManager.setMultiplayerMode(true);
        inventoryManager.getCurrentInventory().addBanana(3);
        assertEquals(3, inventoryManager.getMultiplayerInventory().getBananaCount(), 
                    "Current inventory should be MP inventory");
        assertEquals(0, inventoryManager.getSingleplayerInventory().getBananaCount(), 
                    "SP inventory should not have bananas");
    }
}
