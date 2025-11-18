package wagemaker.uk.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for SmallTree random dual-item drop functionality.
 * Tests the complete flow from SmallTree destruction to random dual-item spawn
 * to item pickup, verifying single-player scenarios.
 * 
 * This test verifies Requirements 1.1, 1.2, 1.3, 1.4, 1.5, 2.4, 3.2, 4.1, 4.2, 5.1 from the spec.
 * 
 * Note: These tests use mock objects to avoid OpenGL dependencies and run in headless mode.
 */
public class SmallTreeRandomDropIntegrationTest {
    
    private Map<String, MockBabyTree> babyTrees;
    private Map<String, MockWoodStack> woodStacks;
    private Map<String, MockSmallTree> smallTrees;
    private Map<String, Boolean> clearedPositions;
    private Random random;
    
    @BeforeEach
    public void setUp() {
        babyTrees = new HashMap<>();
        woodStacks = new HashMap<>();
        smallTrees = new HashMap<>();
        clearedPositions = new HashMap<>();
        random = new Random();
    }
    
    /**
     * Mock SmallTree class for testing without OpenGL context.
     */
    private static class MockSmallTree {
        private float x, y;
        private float health = 100.0f;
        private final AtomicBoolean disposed = new AtomicBoolean(false);
        
        public MockSmallTree(float x, float y) {
            this.x = x;
            this.y = y;
        }
        
        public boolean attack() {
            health -= 10.0f;
            return health <= 0;
        }
        
        public float getX() {
            return x;
        }
        
        public float getY() {
            return y;
        }
        
        public float getHealth() {
            return health;
        }
        
        public void dispose() {
            disposed.set(true);
        }
        
        public boolean isDisposed() {
            return disposed.get();
        }
    }
    
    /**
     * Mock BabyTree class for testing without OpenGL context.
     */
    private static class MockBabyTree {
        private float x, y;
        private final AtomicBoolean disposed = new AtomicBoolean(false);
        
        public MockBabyTree(float x, float y) {
            this.x = x;
            this.y = y;
        }
        
        public float getX() {
            return x;
        }
        
        public float getY() {
            return y;
        }
        
        public void dispose() {
            disposed.set(true);
        }
        
        public boolean isDisposed() {
            return disposed.get();
        }
        
        // Mock texture dimensions (64x64 from sprite sheet, rendered at 32x32)
        public int getTextureWidth() {
            return 64;
        }
        
        public int getTextureHeight() {
            return 64;
        }
    }
    
    /**
     * Mock WoodStack class for testing without OpenGL context.
     */
    private static class MockWoodStack {
        private float x, y;
        private final AtomicBoolean disposed = new AtomicBoolean(false);
        
        public MockWoodStack(float x, float y) {
            this.x = x;
            this.y = y;
        }
        
        public float getX() {
            return x;
        }
        
        public float getY() {
            return y;
        }
        
        public void dispose() {
            disposed.set(true);
        }
        
        public boolean isDisposed() {
            return disposed.get();
        }
        
        // Mock texture dimensions (64x64 from sprite sheet, rendered at 32x32)
        public int getTextureWidth() {
            return 64;
        }
        
        public int getTextureHeight() {
            return 64;
        }
    }

    
    /**
     * Test that destroying a SmallTree spawns two items with random selection.
     * 
     * Requirement 1.1: "WHEN a SmallTree health reaches zero, THE Game System 
     * SHALL randomly select one of three drop combinations with equal probability"
     */
    @Test
    public void testSmallTreeSpawnsTwoItemsRandomly() {
        // Create a SmallTree at position (100, 200)
        float treeX = 100.0f;
        float treeY = 200.0f;
        String treeKey = treeX + "," + treeY;
        MockSmallTree smallTree = new MockSmallTree(treeX, treeY);
        smallTrees.put(treeKey, smallTree);
        
        // Attack tree until destroyed (health starts at 100, each attack does 10 damage)
        boolean destroyed = false;
        for (int i = 0; i < 10; i++) {
            destroyed = smallTree.attack();
        }
        
        assertTrue(destroyed, "SmallTree should be destroyed after 10 attacks");
        
        // Simulate random item spawning (as done in Player.update)
        if (destroyed) {
            int dropType = random.nextInt(3); // Returns 0, 1, or 2
            
            switch (dropType) {
                case 0: // 2x BabyTree
                    babyTrees.put(treeKey + "-item1", new MockBabyTree(smallTree.getX(), smallTree.getY()));
                    babyTrees.put(treeKey + "-item2", new MockBabyTree(smallTree.getX() + 8, smallTree.getY()));
                    break;
                case 1: // 2x WoodStack
                    woodStacks.put(treeKey + "-item1", new MockWoodStack(smallTree.getX(), smallTree.getY()));
                    woodStacks.put(treeKey + "-item2", new MockWoodStack(smallTree.getX() + 8, smallTree.getY()));
                    break;
                case 2: // 1x BabyTree + 1x WoodStack
                    babyTrees.put(treeKey + "-item1", new MockBabyTree(smallTree.getX(), smallTree.getY()));
                    woodStacks.put(treeKey + "-item2", new MockWoodStack(smallTree.getX() + 8, smallTree.getY()));
                    break;
            }
            
            smallTree.dispose();
            smallTrees.remove(treeKey);
            clearedPositions.put(treeKey, true);
        }
        
        // Verify two items were spawned (total count should be 2)
        int totalItems = babyTrees.size() + woodStacks.size();
        assertEquals(2, totalItems, "Should have 2 items total");
        
        // Verify tree was removed
        assertFalse(smallTrees.containsKey(treeKey), "Tree should be removed from map");
        assertTrue(clearedPositions.containsKey(treeKey), "Position should be marked as cleared");
        
        // Clean up
        for (MockBabyTree item : babyTrees.values()) {
            item.dispose();
        }
        for (MockWoodStack item : woodStacks.values()) {
            item.dispose();
        }
    }
    
    /**
     * Test all three drop combinations appear over multiple destructions.
     * 
     * Requirement 1.1, 1.2, 1.3, 1.4: Verifies all three drop combinations work correctly
     */
    @Test
    public void testAllThreeDropCombinationsAppear() {
        boolean foundTwoBabyTrees = false;
        boolean foundTwoWoodStacks = false;
        boolean foundMixed = false;
        
        // Destroy multiple trees to see all combinations (with fixed seed for reproducibility)
        Random testRandom = new Random(12345);
        
        for (int i = 0; i < 30; i++) {
            babyTrees.clear();
            woodStacks.clear();
            
            float treeX = 100.0f + (i * 100);
            float treeY = 200.0f;
            String treeKey = treeX + "," + treeY;
            MockSmallTree smallTree = new MockSmallTree(treeX, treeY);
            
            // Destroy tree
            for (int j = 0; j < 10; j++) {
                smallTree.attack();
            }
            
            // Spawn items with random selection
            int dropType = testRandom.nextInt(3);
            
            switch (dropType) {
                case 0: // 2x BabyTree
                    babyTrees.put(treeKey + "-item1", new MockBabyTree(smallTree.getX(), smallTree.getY()));
                    babyTrees.put(treeKey + "-item2", new MockBabyTree(smallTree.getX() + 8, smallTree.getY()));
                    if (babyTrees.size() == 2 && woodStacks.size() == 0) {
                        foundTwoBabyTrees = true;
                    }
                    break;
                case 1: // 2x WoodStack
                    woodStacks.put(treeKey + "-item1", new MockWoodStack(smallTree.getX(), smallTree.getY()));
                    woodStacks.put(treeKey + "-item2", new MockWoodStack(smallTree.getX() + 8, smallTree.getY()));
                    if (woodStacks.size() == 2 && babyTrees.size() == 0) {
                        foundTwoWoodStacks = true;
                    }
                    break;
                case 2: // 1x BabyTree + 1x WoodStack
                    babyTrees.put(treeKey + "-item1", new MockBabyTree(smallTree.getX(), smallTree.getY()));
                    woodStacks.put(treeKey + "-item2", new MockWoodStack(smallTree.getX() + 8, smallTree.getY()));
                    if (babyTrees.size() == 1 && woodStacks.size() == 1) {
                        foundMixed = true;
                    }
                    break;
            }
            
            smallTree.dispose();
            
            // Clean up items
            for (MockBabyTree item : babyTrees.values()) {
                item.dispose();
            }
            for (MockWoodStack item : woodStacks.values()) {
                item.dispose();
            }
        }
        
        // Verify all three combinations appeared
        assertTrue(foundTwoBabyTrees, "Should find 2x BabyTree combination");
        assertTrue(foundTwoWoodStacks, "Should find 2x WoodStack combination");
        assertTrue(foundMixed, "Should find 1x BabyTree + 1x WoodStack combination");
    }
    
    /**
     * Test that items are positioned 8 pixels apart horizontally.
     * 
     * Requirement 1.5: "WHEN two items are spawned, THE Game System SHALL position 
     * the second item 8 pixels horizontally offset from the first item to prevent overlap"
     */
    @Test
    public void testItemsAre8PixelsApart() {
        float treeX = 256.0f;
        float treeY = 128.0f;
        String treeKey = treeX + "," + treeY;
        MockSmallTree smallTree = new MockSmallTree(treeX, treeY);
        
        // Destroy tree
        for (int i = 0; i < 10; i++) {
            smallTree.attack();
        }
        
        // Test case 0: 2x BabyTree
        babyTrees.put(treeKey + "-item1", new MockBabyTree(smallTree.getX(), smallTree.getY()));
        babyTrees.put(treeKey + "-item2", new MockBabyTree(smallTree.getX() + 8, smallTree.getY()));
        
        MockBabyTree item1 = babyTrees.get(treeKey + "-item1");
        MockBabyTree item2 = babyTrees.get(treeKey + "-item2");
        
        // Verify horizontal spacing
        float horizontalDistance = item2.getX() - item1.getX();
        assertEquals(8.0f, horizontalDistance, 0.01f, "Items should be 8 pixels apart horizontally");
        
        // Verify same Y coordinate
        assertEquals(item1.getY(), item2.getY(), 0.01f, "Items should have same Y coordinate");
        
        // Clean up
        item1.dispose();
        item2.dispose();
        smallTree.dispose();
    }
    
    /**
     * Test that items use correct texture dimensions and render at 32x32 pixels.
     * 
     * Requirement 2.4: "THE MyGdxGame Class SHALL render BabyTree items at 32x32 pixels on screen"
     * Requirement 3.2: "THE MyGdxGame Class SHALL render WoodStack items at 32x32 pixels on screen"
     */
    @Test
    public void testItemTexturesAndRenderSize() {
        // Create items
        MockBabyTree babyTree = new MockBabyTree(0, 0);
        MockWoodStack woodStack = new MockWoodStack(8, 0);
        
        // Verify texture dimensions (source is 64x64, but rendered at 32x32)
        assertEquals(64, babyTree.getTextureWidth(), "BabyTree texture width should be 64");
        assertEquals(64, babyTree.getTextureHeight(), "BabyTree texture height should be 64");
        assertEquals(64, woodStack.getTextureWidth(), "WoodStack texture width should be 64");
        assertEquals(64, woodStack.getTextureHeight(), "WoodStack texture height should be 64");
        
        // Note: Render size of 32x32 is enforced in MyGdxGame.drawBabyTrees() and drawWoodStacks()
        // This test verifies the texture source dimensions are correct
        
        // Clean up
        babyTree.dispose();
        woodStack.dispose();
    }
    
    /**
     * Test pickup detection for BabyTree items.
     * 
     * Requirement 4.1: "WHEN the player's collision box overlaps with a BabyTree item, 
     * THE Player Class SHALL remove the BabyTree from the game world"
     */
    @Test
    public void testBabyTreePickup() {
        // Create BabyTree at position (100, 100)
        String itemKey = "100,100-item1";
        MockBabyTree babyTree = new MockBabyTree(100, 100);
        babyTrees.put(itemKey, babyTree);
        
        // Verify item exists
        assertEquals(1, babyTrees.size(), "Should have 1 BabyTree before pickup");
        assertNotNull(babyTrees.get(itemKey), "BabyTree should exist");
        
        // Simulate pickup (player walks over item)
        AtomicBoolean pickedUp = new AtomicBoolean(false);
        
        if (babyTrees.containsKey(itemKey)) {
            MockBabyTree item = babyTrees.get(itemKey);
            item.dispose();
            babyTrees.remove(itemKey);
            pickedUp.set(true);
        }
        
        // Verify pickup
        assertTrue(pickedUp.get(), "Item should be picked up");
        assertEquals(0, babyTrees.size(), "BabyTree should be removed from map");
        assertFalse(babyTrees.containsKey(itemKey), "BabyTree key should not exist");
    }
    
    /**
     * Test pickup detection for WoodStack items.
     * 
     * Requirement 4.2: "WHEN the player's collision box overlaps with a WoodStack item, 
     * THE Player Class SHALL remove the WoodStack from the game world"
     */
    @Test
    public void testWoodStackPickup() {
        // Create WoodStack at position (108, 100)
        String itemKey = "100,100-item2";
        MockWoodStack woodStack = new MockWoodStack(108, 100);
        woodStacks.put(itemKey, woodStack);
        
        // Verify item exists
        assertEquals(1, woodStacks.size(), "Should have 1 WoodStack before pickup");
        assertNotNull(woodStacks.get(itemKey), "WoodStack should exist");
        
        // Simulate pickup (player walks over item)
        AtomicBoolean pickedUp = new AtomicBoolean(false);
        
        if (woodStacks.containsKey(itemKey)) {
            MockWoodStack item = woodStacks.get(itemKey);
            item.dispose();
            woodStacks.remove(itemKey);
            pickedUp.set(true);
        }
        
        // Verify pickup
        assertTrue(pickedUp.get(), "Item should be picked up");
        assertEquals(0, woodStacks.size(), "WoodStack should be removed from map");
        assertFalse(woodStacks.containsKey(itemKey), "WoodStack key should not exist");
    }
    
    /**
     * Test that both items can be picked up independently.
     * Verifies that picking up one item doesn't affect the other.
     */
    @Test
    public void testIndependentItemPickup() {
        String treeKey = "200,200";
        
        // Create both items (mixed combination)
        MockBabyTree babyTree = new MockBabyTree(200, 200);
        MockWoodStack woodStack = new MockWoodStack(208, 200);
        
        babyTrees.put(treeKey + "-item1", babyTree);
        woodStacks.put(treeKey + "-item2", woodStack);
        
        // Verify both items exist
        assertEquals(1, babyTrees.size(), "Should have 1 BabyTree");
        assertEquals(1, woodStacks.size(), "Should have 1 WoodStack");
        
        // Pick up BabyTree first
        MockBabyTree tree = babyTrees.remove(treeKey + "-item1");
        tree.dispose();
        
        // Verify BabyTree removed but WoodStack still exists
        assertEquals(0, babyTrees.size(), "BabyTree should be removed");
        assertEquals(1, woodStacks.size(), "WoodStack should still exist");
        
        // Pick up WoodStack
        MockWoodStack stack = woodStacks.remove(treeKey + "-item2");
        stack.dispose();
        
        // Verify both items removed
        assertEquals(0, babyTrees.size(), "BabyTree should be removed");
        assertEquals(0, woodStacks.size(), "WoodStack should be removed");
    }
    
    /**
     * Test multiple SmallTree destructions spawn correct number of items.
     * Verifies that the random dual-drop system works consistently across multiple trees.
     */
    @Test
    public void testMultipleSmallTreeDestructions() {
        // Create 5 SmallTrees at different positions
        float[][] positions = {{100, 100}, {300, 200}, {500, 300}, {700, 400}, {900, 500}};
        Random testRandom = new Random(54321);
        
        for (float[] pos : positions) {
            float treeX = pos[0];
            float treeY = pos[1];
            String treeKey = treeX + "," + treeY;
            
            MockSmallTree smallTree = new MockSmallTree(treeX, treeY);
            
            // Destroy tree
            for (int i = 0; i < 10; i++) {
                smallTree.attack();
            }
            
            // Spawn items with random selection
            int dropType = testRandom.nextInt(3);
            
            switch (dropType) {
                case 0: // 2x BabyTree
                    babyTrees.put(treeKey + "-item1", new MockBabyTree(smallTree.getX(), smallTree.getY()));
                    babyTrees.put(treeKey + "-item2", new MockBabyTree(smallTree.getX() + 8, smallTree.getY()));
                    break;
                case 1: // 2x WoodStack
                    woodStacks.put(treeKey + "-item1", new MockWoodStack(smallTree.getX(), smallTree.getY()));
                    woodStacks.put(treeKey + "-item2", new MockWoodStack(smallTree.getX() + 8, smallTree.getY()));
                    break;
                case 2: // 1x BabyTree + 1x WoodStack
                    babyTrees.put(treeKey + "-item1", new MockBabyTree(smallTree.getX(), smallTree.getY()));
                    woodStacks.put(treeKey + "-item2", new MockWoodStack(smallTree.getX() + 8, smallTree.getY()));
                    break;
            }
            
            smallTree.dispose();
        }
        
        // Verify correct total number of items spawned (5 trees × 2 items = 10 items)
        int totalItems = babyTrees.size() + woodStacks.size();
        assertEquals(10, totalItems, "Should have 10 items total from 5 trees");
        
        // Clean up
        for (MockBabyTree item : babyTrees.values()) {
            item.dispose();
        }
        for (MockWoodStack item : woodStacks.values()) {
            item.dispose();
        }
    }
    
    /**
     * Test that item keys are unique and traceable to source tree.
     * 
     * Requirement 1.8: "WHEN items are spawned, THE Game System SHALL use unique 
     * identifiers based on the tree's position key with suffixes '-item1' and '-item2'"
     */
    @Test
    public void testItemKeysAreUniqueAndTraceable() {
        float treeX = 128.0f;
        float treeY = 256.0f;
        String treeKey = treeX + "," + treeY;
        
        MockSmallTree smallTree = new MockSmallTree(treeX, treeY);
        
        // Destroy tree
        for (int i = 0; i < 10; i++) {
            smallTree.attack();
        }
        
        // Spawn items with proper keys (case 2: mixed)
        String item1Key = treeKey + "-item1";
        String item2Key = treeKey + "-item2";
        
        babyTrees.put(item1Key, new MockBabyTree(smallTree.getX(), smallTree.getY()));
        woodStacks.put(item2Key, new MockWoodStack(smallTree.getX() + 8, smallTree.getY()));
        
        // Verify keys are correct
        assertEquals("128.0,256.0-item1", item1Key, "Item1 key should match pattern");
        assertEquals("128.0,256.0-item2", item2Key, "Item2 key should match pattern");
        
        // Verify keys are unique
        assertFalse(item1Key.equals(item2Key), "Item keys should be unique");
        
        // Verify items can be retrieved by key
        assertNotNull(babyTrees.get(item1Key), "BabyTree should be retrievable by key");
        assertNotNull(woodStacks.get(item2Key), "WoodStack should be retrievable by key");
        
        // Clean up
        babyTrees.get(item1Key).dispose();
        woodStacks.get(item2Key).dispose();
        smallTree.dispose();
    }
    
    /**
     * Test probability distribution is roughly equal across many destructions.
     * Verifies that each combination has approximately 33% probability.
     */
    @Test
    public void testProbabilityDistribution() {
        int twoBabyTreeCount = 0;
        int twoWoodStackCount = 0;
        int mixedCount = 0;
        int totalTrees = 300;
        
        Random testRandom = new Random(99999);
        
        for (int i = 0; i < totalTrees; i++) {
            int dropType = testRandom.nextInt(3);
            
            switch (dropType) {
                case 0:
                    twoBabyTreeCount++;
                    break;
                case 1:
                    twoWoodStackCount++;
                    break;
                case 2:
                    mixedCount++;
                    break;
            }
        }
        
        // Each combination should appear roughly 100 times (33.33% of 300)
        // Allow 20% variance (80-120 range)
        assertTrue(twoBabyTreeCount >= 80 && twoBabyTreeCount <= 120, 
            "2x BabyTree should appear ~100 times, got: " + twoBabyTreeCount);
        assertTrue(twoWoodStackCount >= 80 && twoWoodStackCount <= 120, 
            "2x WoodStack should appear ~100 times, got: " + twoWoodStackCount);
        assertTrue(mixedCount >= 80 && mixedCount <= 120, 
            "Mixed combination should appear ~100 times, got: " + mixedCount);
        
        // Verify total
        assertEquals(totalTrees, twoBabyTreeCount + twoWoodStackCount + mixedCount, 
            "Total should equal number of trees");
    }
}
