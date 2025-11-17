package wagemaker.uk.inventory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

import wagemaker.uk.player.Player;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.Gdx;

/**
 * Unit tests for InventoryManager class.
 * Tests mode switching and item collection.
 * 
 * Requirements: 1.2, 2.1, 2.2, 2.3, 2.4, 2.5, 3.1, 5.1, 5.2, 5.3, 5.4
 */
public class InventoryManagerTest {
    
    private Player player;
    private InventoryManager inventoryManager;
    
    @BeforeAll
    public static void checkLibGDXAvailability() {
        // Skip all tests if LibGDX is not initialized (headless environment)
        assumeTrue(Gdx.files != null, "LibGDX not initialized - skipping tests that require graphics context");
    }
    
    @BeforeEach
    public void setUp() {
        OrthographicCamera camera = new OrthographicCamera();
        player = new Player(0, 0, camera);
        player.setHealth(100);
        
        inventoryManager = new InventoryManager(player);
        player.setInventoryManager(inventoryManager);
    }
    
    // ===== Initialization Tests =====
    
    @Test
    public void testInitialModeIsSinglePlayer() {
        assertFalse(inventoryManager.isMultiplayerMode(), "Should start in single-player mode");
    }
    
    @Test
    public void testInitialInventoriesAreEmpty() {
        assertEquals(0, inventoryManager.getSingleplayerInventory().getAppleCount(), 
            "SP inventory should start empty");
        assertEquals(0, inventoryManager.getMultiplayerInventory().getAppleCount(), 
            "MP inventory should start empty");
    }
    
    @Test
    public void testGetCurrentInventoryReturnsSinglePlayerByDefault() {
        Inventory current = inventoryManager.getCurrentInventory();
        assertSame(inventoryManager.getSingleplayerInventory(), current, 
            "Should return single-player inventory by default");
    }
    
    // ===== Mode Switching Tests =====
    
    @Test
    public void testSwitchToMultiplayerMode() {
        inventoryManager.setMultiplayerMode(true);
        assertTrue(inventoryManager.isMultiplayerMode(), "Should be in multiplayer mode");
    }
    
    @Test
    public void testSwitchBackToSinglePlayerMode() {
        inventoryManager.setMultiplayerMode(true);
        inventoryManager.setMultiplayerMode(false);
        assertFalse(inventoryManager.isMultiplayerMode(), "Should be back in single-player mode");
    }
    
    @Test
    public void testGetCurrentInventoryReturnsMultiplayerWhenInMultiplayerMode() {
        inventoryManager.setMultiplayerMode(true);
        Inventory current = inventoryManager.getCurrentInventory();
        assertSame(inventoryManager.getMultiplayerInventory(), current, 
            "Should return multiplayer inventory when in multiplayer mode");
    }
    
    @Test
    public void testModeSwitchingPreservesInventories() {
        // Add items to single-player inventory
        inventoryManager.getCurrentInventory().addApple(5);
        inventoryManager.getCurrentInventory().addBanana(3);
        
        // Switch to multiplayer
        inventoryManager.setMultiplayerMode(true);
        inventoryManager.getCurrentInventory().addApple(10);
        inventoryManager.getCurrentInventory().addWoodStack(7);
        
        // Switch back to single-player
        inventoryManager.setMultiplayerMode(false);
        
        // Verify single-player inventory is preserved
        assertEquals(5, inventoryManager.getSingleplayerInventory().getAppleCount(), 
            "SP apple count should be preserved");
        assertEquals(3, inventoryManager.getSingleplayerInventory().getBananaCount(), 
            "SP banana count should be preserved");
        assertEquals(0, inventoryManager.getSingleplayerInventory().getWoodStackCount(), 
            "SP wood stack should still be 0");
        
        // Verify multiplayer inventory is preserved
        assertEquals(10, inventoryManager.getMultiplayerInventory().getAppleCount(), 
            "MP apple count should be preserved");
        assertEquals(7, inventoryManager.getMultiplayerInventory().getWoodStackCount(), 
            "MP wood stack count should be preserved");
    }
    
    @Test
    public void testMultipleModeSwitches() {
        inventoryManager.setMultiplayerMode(true);
        inventoryManager.setMultiplayerMode(false);
        inventoryManager.setMultiplayerMode(true);
        inventoryManager.setMultiplayerMode(false);
        
        assertFalse(inventoryManager.isMultiplayerMode(), 
            "Should handle multiple mode switches correctly");
    }
    
    // ===== Item Collection Tests =====
    
    @Test
    public void testCollectAppleStoresItem() {
        player.setHealth(100);
        inventoryManager.collectItem(ItemType.APPLE);
        
        assertEquals(1, inventoryManager.getCurrentInventory().getAppleCount(), 
            "Apple should be stored in inventory");
        assertEquals(100, player.getHealth(), 0.01f, 
            "Health should remain unchanged");
    }
    
    @Test
    public void testCollectAppleWithLowHealthStoresItem() {
        player.setHealth(50);
        inventoryManager.collectItem(ItemType.APPLE);
        
        assertEquals(1, inventoryManager.getCurrentInventory().getAppleCount(), 
            "Apple should be stored in inventory");
        assertEquals(50, player.getHealth(), 0.01f, 
            "Health should remain unchanged (no auto-consumption)");
    }
    
    @Test
    public void testCollectBananaStoresItem() {
        player.setHealth(60);
        inventoryManager.collectItem(ItemType.BANANA);
        
        assertEquals(1, inventoryManager.getCurrentInventory().getBananaCount(), 
            "Banana should be stored in inventory");
        assertEquals(60, player.getHealth(), 0.01f, 
            "Health should remain unchanged");
    }
    
    @Test
    public void testCollectWoodStackStoresItem() {
        player.setHealth(50);
        inventoryManager.collectItem(ItemType.WOOD_STACK);
        
        assertEquals(1, inventoryManager.getCurrentInventory().getWoodStackCount(), 
            "Wood stack should be stored in inventory");
        assertEquals(50, player.getHealth(), 0.01f, 
            "Health should not change for non-consumable items");
    }
    
    @Test
    public void testCollectBabyBambooStoresItem() {
        player.setHealth(30);
        inventoryManager.collectItem(ItemType.BABY_BAMBOO);
        
        assertEquals(1, inventoryManager.getCurrentInventory().getBabyBambooCount(), 
            "Baby bamboo should be stored in inventory");
        assertEquals(30, player.getHealth(), 0.01f, 
            "Health should not change for non-consumable items");
    }
    
    @Test
    public void testCollectBambooStackStoresItem() {
        player.setHealth(20);
        inventoryManager.collectItem(ItemType.BAMBOO_STACK);
        
        assertEquals(1, inventoryManager.getCurrentInventory().getBambooStackCount(), 
            "Bamboo stack should be stored in inventory");
        assertEquals(20, player.getHealth(), 0.01f, 
            "Health should not change for non-consumable items");
    }
    
    @Test
    public void testCollectNullItemTypeDoesNothing() {
        player.setHealth(50);
        inventoryManager.collectItem(null);
        
        assertEquals(50, player.getHealth(), 0.01f, "Health should not change");
        assertEquals(0, inventoryManager.getCurrentInventory().getAppleCount(), 
            "Inventory should remain empty");
    }
    
    // ===== Combined Scenario Tests =====
    
    @Test
    public void testCollectMultipleItems() {
        player.setHealth(100);
        inventoryManager.collectItem(ItemType.APPLE);
        inventoryManager.collectItem(ItemType.BANANA);
        inventoryManager.collectItem(ItemType.WOOD_STACK);
        
        assertEquals(1, inventoryManager.getCurrentInventory().getAppleCount(), 
            "Apple should be stored");
        assertEquals(1, inventoryManager.getCurrentInventory().getBananaCount(), 
            "Banana should be stored");
        assertEquals(1, inventoryManager.getCurrentInventory().getWoodStackCount(), 
            "Wood stack should be stored");
        assertEquals(100, player.getHealth(), 0.01f, 
            "Health should remain unchanged");
    }
    
    @Test
    public void testCollectItemsInMultiplayerMode() {
        inventoryManager.setMultiplayerMode(true);
        
        inventoryManager.collectItem(ItemType.APPLE);
        inventoryManager.collectItem(ItemType.BANANA);
        
        assertEquals(1, inventoryManager.getMultiplayerInventory().getAppleCount(), 
            "Apple should be stored in MP inventory");
        assertEquals(1, inventoryManager.getMultiplayerInventory().getBananaCount(), 
            "Banana should be stored in MP inventory");
        assertEquals(0, inventoryManager.getSingleplayerInventory().getAppleCount(), 
            "Single-player inventory should not be affected");
    }
}
