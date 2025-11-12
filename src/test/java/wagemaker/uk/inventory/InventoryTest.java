package wagemaker.uk.inventory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Inventory class.
 * Tests add/remove operations, boundary conditions, and negative count prevention.
 * 
 * Requirements: 3.2, 3.3, 3.4, 3.5
 */
public class InventoryTest {
    
    private Inventory inventory;
    
    @BeforeEach
    public void setUp() {
        inventory = new Inventory();
    }
    
    // ===== Initialization Tests =====
    
    @Test
    public void testInitialInventoryIsEmpty() {
        assertEquals(0, inventory.getAppleCount(), "Initial apple count should be 0");
        assertEquals(0, inventory.getBananaCount(), "Initial banana count should be 0");
        assertEquals(0, inventory.getBabyBambooCount(), "Initial baby bamboo count should be 0");
        assertEquals(0, inventory.getBambooStackCount(), "Initial bamboo stack count should be 0");
        assertEquals(0, inventory.getWoodStackCount(), "Initial wood stack count should be 0");
    }
    
    // ===== Apple Tests =====
    
    @Test
    public void testAddApple() {
        inventory.addApple(5);
        assertEquals(5, inventory.getAppleCount(), "Should have 5 apples after adding");
    }
    
    @Test
    public void testAddMultipleApples() {
        inventory.addApple(3);
        inventory.addApple(7);
        assertEquals(10, inventory.getAppleCount(), "Should have 10 apples after multiple adds");
    }
    
    @Test
    public void testRemoveAppleSuccess() {
        inventory.addApple(5);
        boolean result = inventory.removeApple(3);
        assertTrue(result, "Should successfully remove 3 apples");
        assertEquals(2, inventory.getAppleCount(), "Should have 2 apples remaining");
    }
    
    @Test
    public void testRemoveAppleInsufficientQuantity() {
        inventory.addApple(2);
        boolean result = inventory.removeApple(5);
        assertFalse(result, "Should fail to remove more apples than available");
        assertEquals(2, inventory.getAppleCount(), "Apple count should remain unchanged");
    }
    
    @Test
    public void testRemoveAppleExactAmount() {
        inventory.addApple(5);
        boolean result = inventory.removeApple(5);
        assertTrue(result, "Should successfully remove exact amount");
        assertEquals(0, inventory.getAppleCount(), "Should have 0 apples remaining");
    }
    
    @Test
    public void testSetAppleCount() {
        inventory.setAppleCount(10);
        assertEquals(10, inventory.getAppleCount(), "Should set apple count to 10");
    }
    
    @Test
    public void testSetAppleCountNegativePrevented() {
        inventory.setAppleCount(-5);
        assertEquals(0, inventory.getAppleCount(), "Negative apple count should be prevented");
    }
    
    // ===== Banana Tests =====
    
    @Test
    public void testAddBanana() {
        inventory.addBanana(8);
        assertEquals(8, inventory.getBananaCount(), "Should have 8 bananas after adding");
    }
    
    @Test
    public void testRemoveBananaSuccess() {
        inventory.addBanana(10);
        boolean result = inventory.removeBanana(4);
        assertTrue(result, "Should successfully remove 4 bananas");
        assertEquals(6, inventory.getBananaCount(), "Should have 6 bananas remaining");
    }
    
    @Test
    public void testRemoveBananaInsufficientQuantity() {
        inventory.addBanana(3);
        boolean result = inventory.removeBanana(5);
        assertFalse(result, "Should fail to remove more bananas than available");
        assertEquals(3, inventory.getBananaCount(), "Banana count should remain unchanged");
    }
    
    @Test
    public void testSetBananaCountNegativePrevented() {
        inventory.setBananaCount(-10);
        assertEquals(0, inventory.getBananaCount(), "Negative banana count should be prevented");
    }
    
    // ===== BabyBamboo Tests =====
    
    @Test
    public void testAddBabyBamboo() {
        inventory.addBabyBamboo(12);
        assertEquals(12, inventory.getBabyBambooCount(), "Should have 12 baby bamboos after adding");
    }
    
    @Test
    public void testRemoveBabyBambooSuccess() {
        inventory.addBabyBamboo(15);
        boolean result = inventory.removeBabyBamboo(7);
        assertTrue(result, "Should successfully remove 7 baby bamboos");
        assertEquals(8, inventory.getBabyBambooCount(), "Should have 8 baby bamboos remaining");
    }
    
    @Test
    public void testRemoveBabyBambooInsufficientQuantity() {
        inventory.addBabyBamboo(5);
        boolean result = inventory.removeBabyBamboo(10);
        assertFalse(result, "Should fail to remove more baby bamboos than available");
        assertEquals(5, inventory.getBabyBambooCount(), "Baby bamboo count should remain unchanged");
    }
    
    @Test
    public void testSetBabyBambooCountNegativePrevented() {
        inventory.setBabyBambooCount(-3);
        assertEquals(0, inventory.getBabyBambooCount(), "Negative baby bamboo count should be prevented");
    }
    
    // ===== BambooStack Tests =====
    
    @Test
    public void testAddBambooStack() {
        inventory.addBambooStack(6);
        assertEquals(6, inventory.getBambooStackCount(), "Should have 6 bamboo stacks after adding");
    }
    
    @Test
    public void testRemoveBambooStackSuccess() {
        inventory.addBambooStack(20);
        boolean result = inventory.removeBambooStack(12);
        assertTrue(result, "Should successfully remove 12 bamboo stacks");
        assertEquals(8, inventory.getBambooStackCount(), "Should have 8 bamboo stacks remaining");
    }
    
    @Test
    public void testRemoveBambooStackInsufficientQuantity() {
        inventory.addBambooStack(4);
        boolean result = inventory.removeBambooStack(8);
        assertFalse(result, "Should fail to remove more bamboo stacks than available");
        assertEquals(4, inventory.getBambooStackCount(), "Bamboo stack count should remain unchanged");
    }
    
    @Test
    public void testSetBambooStackCountNegativePrevented() {
        inventory.setBambooStackCount(-7);
        assertEquals(0, inventory.getBambooStackCount(), "Negative bamboo stack count should be prevented");
    }
    
    // ===== WoodStack Tests =====
    
    @Test
    public void testAddWoodStack() {
        inventory.addWoodStack(15);
        assertEquals(15, inventory.getWoodStackCount(), "Should have 15 wood stacks after adding");
    }
    
    @Test
    public void testRemoveWoodStackSuccess() {
        inventory.addWoodStack(25);
        boolean result = inventory.removeWoodStack(10);
        assertTrue(result, "Should successfully remove 10 wood stacks");
        assertEquals(15, inventory.getWoodStackCount(), "Should have 15 wood stacks remaining");
    }
    
    @Test
    public void testRemoveWoodStackInsufficientQuantity() {
        inventory.addWoodStack(7);
        boolean result = inventory.removeWoodStack(15);
        assertFalse(result, "Should fail to remove more wood stacks than available");
        assertEquals(7, inventory.getWoodStackCount(), "Wood stack count should remain unchanged");
    }
    
    @Test
    public void testSetWoodStackCountNegativePrevented() {
        inventory.setWoodStackCount(-20);
        assertEquals(0, inventory.getWoodStackCount(), "Negative wood stack count should be prevented");
    }
    
    // ===== Clear Tests =====
    
    @Test
    public void testClearEmptyInventory() {
        inventory.clear();
        assertEquals(0, inventory.getAppleCount(), "Apple count should be 0 after clear");
        assertEquals(0, inventory.getBananaCount(), "Banana count should be 0 after clear");
        assertEquals(0, inventory.getBabyBambooCount(), "Baby bamboo count should be 0 after clear");
        assertEquals(0, inventory.getBambooStackCount(), "Bamboo stack count should be 0 after clear");
        assertEquals(0, inventory.getWoodStackCount(), "Wood stack count should be 0 after clear");
    }
    
    @Test
    public void testClearPopulatedInventory() {
        inventory.addApple(10);
        inventory.addBanana(5);
        inventory.addBabyBamboo(20);
        inventory.addBambooStack(8);
        inventory.addWoodStack(15);
        
        inventory.clear();
        
        assertEquals(0, inventory.getAppleCount(), "Apple count should be 0 after clear");
        assertEquals(0, inventory.getBananaCount(), "Banana count should be 0 after clear");
        assertEquals(0, inventory.getBabyBambooCount(), "Baby bamboo count should be 0 after clear");
        assertEquals(0, inventory.getBambooStackCount(), "Bamboo stack count should be 0 after clear");
        assertEquals(0, inventory.getWoodStackCount(), "Wood stack count should be 0 after clear");
    }
    
    // ===== Boundary Condition Tests =====
    
    @Test
    public void testRemoveZeroItems() {
        inventory.addApple(5);
        boolean result = inventory.removeApple(0);
        assertTrue(result, "Should successfully remove 0 items");
        assertEquals(5, inventory.getAppleCount(), "Count should remain unchanged");
    }
    
    @Test
    public void testAddZeroItems() {
        inventory.addApple(5);
        inventory.addApple(0);
        assertEquals(5, inventory.getAppleCount(), "Adding 0 should not change count");
    }
    
    @Test
    public void testRemoveFromEmptyInventory() {
        boolean result = inventory.removeApple(1);
        assertFalse(result, "Should fail to remove from empty inventory");
        assertEquals(0, inventory.getAppleCount(), "Count should remain 0");
    }
    
    @Test
    public void testLargeQuantities() {
        inventory.addApple(1000);
        inventory.addBanana(999);
        inventory.addWoodStack(5000);
        
        assertEquals(1000, inventory.getAppleCount(), "Should handle large apple quantity");
        assertEquals(999, inventory.getBananaCount(), "Should handle large banana quantity");
        assertEquals(5000, inventory.getWoodStackCount(), "Should handle large wood stack quantity");
    }
    
    // ===== Mixed Operations Tests =====
    
    @Test
    public void testMixedAddRemoveOperations() {
        inventory.addApple(10);
        inventory.removeApple(3);
        inventory.addApple(5);
        inventory.removeApple(2);
        
        assertEquals(10, inventory.getAppleCount(), "Should have correct count after mixed operations");
    }
    
    @Test
    public void testMultipleItemTypesIndependent() {
        inventory.addApple(5);
        inventory.addBanana(10);
        inventory.addWoodStack(15);
        
        inventory.removeApple(2);
        
        assertEquals(3, inventory.getAppleCount(), "Apple count should be affected");
        assertEquals(10, inventory.getBananaCount(), "Banana count should be independent");
        assertEquals(15, inventory.getWoodStackCount(), "Wood stack count should be independent");
    }
    
    @Test
    public void testSetCountOverwritesPreviousValue() {
        inventory.addApple(10);
        inventory.setAppleCount(5);
        assertEquals(5, inventory.getAppleCount(), "Set should overwrite previous value");
    }
}
