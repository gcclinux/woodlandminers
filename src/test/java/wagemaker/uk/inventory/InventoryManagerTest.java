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
 * Tests mode switching, auto-consumption logic, and health-based item routing.
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
    
    // ===== Health-Based Item Routing Tests =====
    
    @Test
    public void testCollectConsumableWithFullHealthStoresItem() {
        player.setHealth(100);
        inventoryManager.collectItem(ItemType.APPLE);
        
        assertEquals(1, inventoryManager.getCurrentInventory().getAppleCount(), 
            "Apple should be stored when health is full");
        assertEquals(100, player.getHealth(), 0.01f, 
            "Health should remain at 100");
    }
    
    @Test
    public void testCollectConsumableWithLowHealthConsumesImmediately() {
        player.setHealth(50);
        inventoryManager.collectItem(ItemType.APPLE);
        
        assertEquals(0, inventoryManager.getCurrentInventory().getAppleCount(), 
            "Apple should not be stored when health is low");
        assertEquals(70, player.getHealth(), 0.01f, 
            "Health should be restored by 20 (50 + 20 = 70)");
    }
    
    @Test
    public void testCollectBananaWithLowHealthConsumesImmediately() {
        player.setHealth(60);
        inventoryManager.collectItem(ItemType.BANANA);
        
        assertEquals(0, inventoryManager.getCurrentInventory().getBananaCount(), 
            "Banana should not be stored when health is low");
        assertEquals(80, player.getHealth(), 0.01f, 
            "Health should be restored by 20 (60 + 20 = 80)");
    }
    
    @Test
    public void testCollectConsumableWithAlmostFullHealthConsumesImmediately() {
        player.setHealth(99);
        inventoryManager.collectItem(ItemType.APPLE);
        
        assertEquals(0, inventoryManager.getCurrentInventory().getAppleCount(), 
            "Apple should be consumed even when health is 99");
        assertEquals(100, player.getHealth(), 0.01f, 
            "Health should be capped at 100 (99 + 20 = 100, not 119)");
    }
    
    @Test
    public void testCollectNonConsumableAlwaysStores() {
        player.setHealth(50);
        inventoryManager.collectItem(ItemType.WOOD_STACK);
        
        assertEquals(1, inventoryManager.getCurrentInventory().getWoodStackCount(), 
            "Wood stack should be stored even when health is low");
        assertEquals(50, player.getHealth(), 0.01f, 
            "Health should not change for non-consumable items");
    }
    
    @Test
    public void testCollectBabyBambooAlwaysStores() {
        player.setHealth(30);
        inventoryManager.collectItem(ItemType.BABY_BAMBOO);
        
        assertEquals(1, inventoryManager.getCurrentInventory().getBabyBambooCount(), 
            "Baby bamboo should be stored even when health is low");
        assertEquals(30, player.getHealth(), 0.01f, 
            "Health should not change for non-consumable items");
    }
    
    @Test
    public void testCollectBambooStackAlwaysStores() {
        player.setHealth(20);
        inventoryManager.collectItem(ItemType.BAMBOO_STACK);
        
        assertEquals(1, inventoryManager.getCurrentInventory().getBambooStackCount(), 
            "Bamboo stack should be stored even when health is low");
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
    
    // ===== Auto-Consumption Tests =====
    
    @Test
    public void testAutoConsumeWithApplesInInventory() {
        player.setHealth(100);
        inventoryManager.getCurrentInventory().addApple(3);
        
        player.setHealth(60);
        inventoryManager.tryAutoConsume();
        
        assertEquals(80, player.getHealth(), 0.01f, 
            "Health should be restored by 20 (60 + 20 = 80)");
        assertEquals(2, inventoryManager.getCurrentInventory().getAppleCount(), 
            "Apple count should be decremented by 1");
    }
    
    @Test
    public void testAutoConsumeWithBananasInInventory() {
        player.setHealth(100);
        inventoryManager.getCurrentInventory().addBanana(5);
        
        player.setHealth(70);
        inventoryManager.tryAutoConsume();
        
        assertEquals(90, player.getHealth(), 0.01f, 
            "Health should be restored by 20 (70 + 20 = 90)");
        assertEquals(4, inventoryManager.getCurrentInventory().getBananaCount(), 
            "Banana count should be decremented by 1");
    }
    
    @Test
    public void testAutoConsumePrioritizesApplesOverBananas() {
        player.setHealth(100);
        inventoryManager.getCurrentInventory().addApple(2);
        inventoryManager.getCurrentInventory().addBanana(3);
        
        player.setHealth(50);
        inventoryManager.tryAutoConsume();
        
        assertEquals(70, player.getHealth(), 0.01f, "Health should be restored");
        assertEquals(1, inventoryManager.getCurrentInventory().getAppleCount(), 
            "Apple should be consumed first");
        assertEquals(3, inventoryManager.getCurrentInventory().getBananaCount(), 
            "Banana should not be consumed when apples are available");
    }
    
    @Test
    public void testAutoConsumeUsesBananasWhenNoApples() {
        player.setHealth(100);
        inventoryManager.getCurrentInventory().addBanana(4);
        
        player.setHealth(40);
        inventoryManager.tryAutoConsume();
        
        assertEquals(60, player.getHealth(), 0.01f, "Health should be restored");
        assertEquals(0, inventoryManager.getCurrentInventory().getAppleCount(), 
            "No apples should be present");
        assertEquals(3, inventoryManager.getCurrentInventory().getBananaCount(), 
            "Banana should be consumed when no apples available");
    }
    
    @Test
    public void testAutoConsumeDoesNothingWhenHealthIsFull() {
        player.setHealth(100);
        inventoryManager.getCurrentInventory().addApple(5);
        
        inventoryManager.tryAutoConsume();
        
        assertEquals(100, player.getHealth(), 0.01f, "Health should remain at 100");
        assertEquals(5, inventoryManager.getCurrentInventory().getAppleCount(), 
            "Apple count should not change when health is full");
    }
    
    @Test
    public void testAutoConsumeDoesNothingWhenInventoryIsEmpty() {
        player.setHealth(50);
        inventoryManager.tryAutoConsume();
        
        assertEquals(50, player.getHealth(), 0.01f, 
            "Health should not change when inventory is empty");
    }
    
    @Test
    public void testAutoConsumeDoesNothingWhenOnlyNonConsumablesInInventory() {
        player.setHealth(50);
        inventoryManager.getCurrentInventory().addWoodStack(10);
        inventoryManager.getCurrentInventory().addBabyBamboo(5);
        
        inventoryManager.tryAutoConsume();
        
        assertEquals(50, player.getHealth(), 0.01f, 
            "Health should not change with only non-consumables");
        assertEquals(10, inventoryManager.getCurrentInventory().getWoodStackCount(), 
            "Wood stack count should not change");
        assertEquals(5, inventoryManager.getCurrentInventory().getBabyBambooCount(), 
            "Baby bamboo count should not change");
    }
    
    @Test
    public void testAutoConsumeHealthCappedAt100() {
        player.setHealth(100);
        inventoryManager.getCurrentInventory().addApple(3);
        
        player.setHealth(95);
        inventoryManager.tryAutoConsume();
        
        assertEquals(100, player.getHealth(), 0.01f, 
            "Health should be capped at 100 (95 + 20 = 100, not 115)");
        assertEquals(2, inventoryManager.getCurrentInventory().getAppleCount(), 
            "Apple should still be consumed");
    }
    
    @Test
    public void testAutoConsumeConsumesOnlyOneItemPerCall() {
        player.setHealth(100);
        inventoryManager.getCurrentInventory().addApple(5);
        
        player.setHealth(10);
        inventoryManager.tryAutoConsume();
        
        assertEquals(30, player.getHealth(), 0.01f, 
            "Only one apple should be consumed per call (10 + 20 = 30)");
        assertEquals(4, inventoryManager.getCurrentInventory().getAppleCount(), 
            "Only one apple should be consumed");
    }
    
    // ===== Combined Scenario Tests =====
    
    @Test
    public void testCollectMultipleItemsWithVaryingHealth() {
        // Start with full health - items should be stored
        player.setHealth(100);
        inventoryManager.collectItem(ItemType.APPLE);
        inventoryManager.collectItem(ItemType.BANANA);
        
        assertEquals(1, inventoryManager.getCurrentInventory().getAppleCount(), 
            "Apple should be stored at full health");
        assertEquals(1, inventoryManager.getCurrentInventory().getBananaCount(), 
            "Banana should be stored at full health");
        
        // Take damage - next consumable should be consumed immediately
        player.setHealth(50);
        inventoryManager.collectItem(ItemType.APPLE);
        
        assertEquals(1, inventoryManager.getCurrentInventory().getAppleCount(), 
            "Apple count should not increase when consumed immediately");
        assertEquals(70, player.getHealth(), 0.01f, 
            "Health should be restored");
    }
    
    @Test
    public void testAutoConsumeInMultiplayerMode() {
        inventoryManager.setMultiplayerMode(true);
        inventoryManager.getCurrentInventory().addApple(3);
        
        player.setHealth(60);
        inventoryManager.tryAutoConsume();
        
        assertEquals(80, player.getHealth(), 0.01f, 
            "Auto-consume should work in multiplayer mode");
        assertEquals(2, inventoryManager.getMultiplayerInventory().getAppleCount(), 
            "Multiplayer inventory should be affected");
        assertEquals(0, inventoryManager.getSingleplayerInventory().getAppleCount(), 
            "Single-player inventory should not be affected");
    }
    
    @Test
    public void testHealthBasedRoutingInMultiplayerMode() {
        inventoryManager.setMultiplayerMode(true);
        
        player.setHealth(100);
        inventoryManager.collectItem(ItemType.APPLE);
        assertEquals(1, inventoryManager.getMultiplayerInventory().getAppleCount(), 
            "Apple should be stored in MP inventory at full health");
        
        player.setHealth(50);
        inventoryManager.collectItem(ItemType.BANANA);
        assertEquals(0, inventoryManager.getMultiplayerInventory().getBananaCount(), 
            "Banana should be consumed immediately in MP mode");
        assertEquals(70, player.getHealth(), 0.01f, 
            "Health should be restored in MP mode");
    }
    
    @Test
    public void testSwitchModesDoesNotAffectAutoConsume() {
        // Add apples to single-player inventory
        inventoryManager.getCurrentInventory().addApple(5);
        
        // Switch to multiplayer
        inventoryManager.setMultiplayerMode(true);
        inventoryManager.getCurrentInventory().addBanana(3);
        
        // Take damage in multiplayer mode
        player.setHealth(60);
        inventoryManager.tryAutoConsume();
        
        // Should consume from multiplayer inventory (banana)
        assertEquals(80, player.getHealth(), 0.01f, "Health should be restored");
        assertEquals(2, inventoryManager.getMultiplayerInventory().getBananaCount(), 
            "Banana should be consumed from MP inventory");
        assertEquals(5, inventoryManager.getSingleplayerInventory().getAppleCount(), 
            "SP inventory should not be affected");
    }
}
