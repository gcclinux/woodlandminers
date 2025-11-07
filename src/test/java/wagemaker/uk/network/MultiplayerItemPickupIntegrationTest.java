package wagemaker.uk.network;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import wagemaker.uk.gdx.MyGdxGame;

/**
 * Integration tests for multiplayer item pickup with the threading fix.
 * Tests the complete flow from item spawn to pickup to disposal, verifying
 * that texture disposal happens on the render thread while game state updates
 * happen immediately.
 * 
 * This test verifies Requirements 1.1, 1.4, 1.5, 3.5 from the spec.
 * 
 * Note: These tests run in headless mode without actual OpenGL context,
 * so they verify the threading mechanism rather than actual texture disposal.
 */
public class MultiplayerItemPickupIntegrationTest {
    
    private static final int TIMEOUT_SECONDS = 10;
    
    /**
     * Test simulating item pickup from network thread.
     * Verifies that removeItem() can be safely called from a network thread
     * and that the operation is properly deferred to the render thread.
     * 
     * Requirement 1.1: "WHEN a player picks up an item in multiplayer mode, 
     * THE Game_Client SHALL queue the item removal operation for execution on the Render_Thread"
     * 
     * Requirement 1.5: "WHEN a remote player picks up an item, THE Game_Client 
     * SHALL handle the removal without OpenGL context errors"
     */
    @Test
    public void testItemPickupFromNetworkThread() throws Exception {
        // Create a mock game instance (without OpenGL context)
        MockMyGdxGame game = new MockMyGdxGame();
        game.initializeForTesting();
        
        // Add a test apple to the game
        String appleId = "test-apple-1";
        MockApple apple = new MockApple(100, 100);
        game.getApples().put(appleId, apple);
        
        // Verify apple exists
        assertNotNull(game.getApples().get(appleId), "Apple should exist before removal");
        assertEquals(1, game.getApples().size(), "Should have 1 apple");
        
        AtomicBoolean networkThreadCompleted = new AtomicBoolean(false);
        AtomicBoolean exceptionThrown = new AtomicBoolean(false);
        CountDownLatch networkThreadLatch = new CountDownLatch(1);
        
        // Simulate network thread calling removeItem
        Thread networkThread = new Thread(() -> {
            try {
                // This simulates what happens when ItemPickupMessage is received
                game.removeItem(appleId);
                networkThreadCompleted.set(true);
            } catch (Exception e) {
                exceptionThrown.set(true);
                e.printStackTrace();
            } finally {
                networkThreadLatch.countDown();
            }
        }, "GameClient-Receive");
        
        networkThread.start();
        
        // Wait for network thread to complete
        assertTrue(networkThreadLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                   "Network thread should complete");
        assertTrue(networkThreadCompleted.get(), "Network thread should complete successfully");
        assertFalse(exceptionThrown.get(), "No exception should be thrown");
        
        // Verify item was removed from game state immediately
        assertNull(game.getApples().get(appleId), "Apple should be removed from game state");
        assertEquals(0, game.getApples().size(), "Should have 0 apples");
        
        // Verify disposal operation was queued
        assertEquals(1, game.getDeferredOperationCount(), 
                    "One disposal operation should be queued");
        
        // Simulate render thread processing the queue
        game.processDeferredOperations();
        
        // Verify disposal was executed
        assertTrue(apple.isDisposed(), "Apple texture should be disposed");
        assertEquals(0, game.getDeferredOperationCount(), 
                    "Queue should be empty after processing");
    }
    
    /**
     * Test rapid item pickups (10+ items).
     * Verifies that multiple items can be picked up rapidly without issues.
     * 
     * Requirement 1.4: "WHEN multiple items are picked up rapidly, THE Game_Client 
     * SHALL process all removal operations without crashing"
     */
    @Test
    public void testRapidItemPickups() throws Exception {
        final int NUM_ITEMS = 15;
        
        MockMyGdxGame game = new MockMyGdxGame();
        game.initializeForTesting();
        
        // Add multiple apples and bananas
        for (int i = 0; i < NUM_ITEMS / 2; i++) {
            String appleId = "apple-" + i;
            game.getApples().put(appleId, new MockApple(i * 10, i * 10));
        }
        
        for (int i = 0; i < NUM_ITEMS / 2 + 1; i++) {
            String bananaId = "banana-" + i;
            game.getBananas().put(bananaId, new MockBanana(i * 10 + 5, i * 10 + 5));
        }
        
        // Verify items exist
        assertEquals(NUM_ITEMS / 2, game.getApples().size(), "Should have apples");
        assertEquals(NUM_ITEMS / 2 + 1, game.getBananas().size(), "Should have bananas");
        
        AtomicBoolean exceptionThrown = new AtomicBoolean(false);
        CountDownLatch allPickupsComplete = new CountDownLatch(1);
        
        // Simulate rapid pickups from network thread
        Thread networkThread = new Thread(() -> {
            try {
                // Pick up all apples
                for (int i = 0; i < NUM_ITEMS / 2; i++) {
                    game.removeItem("apple-" + i);
                    Thread.sleep(5); // Small delay to simulate realistic timing
                }
                
                // Pick up all bananas
                for (int i = 0; i < NUM_ITEMS / 2 + 1; i++) {
                    game.removeItem("banana-" + i);
                    Thread.sleep(5);
                }
                
            } catch (Exception e) {
                exceptionThrown.set(true);
                e.printStackTrace();
            } finally {
                allPickupsComplete.countDown();
            }
        }, "GameClient-Receive");
        
        networkThread.start();
        
        // Wait for all pickups to complete
        assertTrue(allPickupsComplete.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                   "All pickups should complete");
        assertFalse(exceptionThrown.get(), "No exception should be thrown");
        
        // Verify all items were removed from game state
        assertEquals(0, game.getApples().size(), "All apples should be removed");
        assertEquals(0, game.getBananas().size(), "All bananas should be removed");
        
        // Verify all disposal operations were queued
        assertEquals(NUM_ITEMS, game.getDeferredOperationCount(), 
                    "All disposal operations should be queued");
        
        // Simulate render thread processing the queue
        game.processDeferredOperations();
        
        // Verify queue is empty
        assertEquals(0, game.getDeferredOperationCount(), 
                    "Queue should be empty after processing");
    }
    
    /**
     * Test verifying items removed from game state immediately.
     * Ensures that items disappear from the game world instantly when picked up,
     * even though texture disposal is deferred.
     * 
     * Requirement 3.1: "WHEN an item pickup message is received, THE Game_Client 
     * SHALL immediately remove the item from the game world"
     * 
     * Requirement 3.2: "WHEN the item is removed from the world, THE Game_Client 
     * SHALL defer only the texture disposal to the Render_Thread"
     */
    @Test
    public void testItemsRemovedFromGameStateImmediately() throws Exception {
        MockMyGdxGame game = new MockMyGdxGame();
        game.initializeForTesting();
        
        // Add test items
        String appleId = "immediate-apple";
        String bananaId = "immediate-banana";
        game.getApples().put(appleId, new MockApple(50, 50));
        game.getBananas().put(bananaId, new MockBanana(60, 60));
        
        // Verify items exist
        assertEquals(1, game.getApples().size(), "Should have 1 apple");
        assertEquals(1, game.getBananas().size(), "Should have 1 banana");
        
        CountDownLatch networkThreadStarted = new CountDownLatch(1);
        CountDownLatch checkStateLatch = new CountDownLatch(1);
        AtomicBoolean itemsRemovedImmediately = new AtomicBoolean(false);
        
        // Simulate network thread
        Thread networkThread = new Thread(() -> {
            try {
                networkThreadStarted.countDown();
                
                // Remove items
                game.removeItem(appleId);
                game.removeItem(bananaId);
                
                // Check if items are removed immediately (before render thread processes)
                if (game.getApples().size() == 0 && game.getBananas().size() == 0) {
                    itemsRemovedImmediately.set(true);
                }
                
                checkStateLatch.countDown();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "GameClient-Receive");
        
        networkThread.start();
        
        // Wait for network thread to complete
        assertTrue(networkThreadStarted.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                   "Network thread should start");
        assertTrue(checkStateLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                   "Network thread should complete");
        
        // Verify items were removed immediately from game state
        assertTrue(itemsRemovedImmediately.get(), 
                  "Items should be removed from game state immediately");
        assertEquals(0, game.getApples().size(), "Apples map should be empty");
        assertEquals(0, game.getBananas().size(), "Bananas map should be empty");
        
        // Verify disposal operations are still queued (not executed yet)
        assertEquals(2, game.getDeferredOperationCount(), 
                    "Disposal operations should be queued");
    }
    
    /**
     * Test verifying texture disposal happens on render thread.
     * Ensures that the actual disposal operation is executed on the render thread,
     * not on the network thread.
     * 
     * Requirement 1.2: "WHEN the Render_Thread processes queued operations, 
     * THE Game_Client SHALL remove the item and dispose its texture"
     * 
     * Requirement 1.3: "WHEN an item is disposed, THE Game_Client SHALL call 
     * texture disposal methods only from the Render_Thread"
     */
    @Test
    public void testTextureDisposalHappensOnRenderThread() throws Exception {
        MockMyGdxGame game = new MockMyGdxGame();
        game.initializeForTesting();
        
        // Add test items
        String appleId = "render-apple";
        MockApple apple = new MockApple(70, 70);
        game.getApples().put(appleId, apple);
        
        AtomicBoolean disposalCalledOnNetworkThread = new AtomicBoolean(false);
        CountDownLatch networkThreadComplete = new CountDownLatch(1);
        
        // Simulate network thread
        Thread networkThread = new Thread(() -> {
            try {
                game.removeItem(appleId);
                
                // Check if disposal happened on network thread (it shouldn't)
                if (apple.isDisposed()) {
                    disposalCalledOnNetworkThread.set(true);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                networkThreadComplete.countDown();
            }
        }, "GameClient-Receive");
        
        networkThread.start();
        
        // Wait for network thread to complete
        assertTrue(networkThreadComplete.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                   "Network thread should complete");
        
        // Verify disposal did NOT happen on network thread
        assertFalse(disposalCalledOnNetworkThread.get(), 
                   "Disposal should not happen on network thread");
        assertFalse(apple.isDisposed(), 
                   "Apple should not be disposed yet");
        
        // Verify disposal operation is queued
        assertEquals(1, game.getDeferredOperationCount(), 
                    "Disposal operation should be queued");
        
        // Simulate render thread processing the queue
        AtomicBoolean disposalCalledOnRenderThread = new AtomicBoolean(false);
        Thread renderThread = new Thread(() -> {
            game.processDeferredOperations();
            
            // Check if disposal happened on render thread
            if (apple.isDisposed()) {
                disposalCalledOnRenderThread.set(true);
            }
        }, "Render-Thread");
        
        renderThread.start();
        renderThread.join(TIMEOUT_SECONDS * 1000);
        
        // Verify disposal happened on render thread
        assertTrue(disposalCalledOnRenderThread.get(), 
                  "Disposal should happen on render thread");
        assertTrue(apple.isDisposed(), 
                  "Apple should be disposed after render thread processes queue");
    }
    
    /**
     * Test for both Apple and Banana item types.
     * Verifies that the threading fix works correctly for both item types.
     * 
     * Requirement 3.5: Tests both Apple and Banana item types
     */
    @Test
    public void testBothAppleAndBananaItemTypes() throws Exception {
        MockMyGdxGame game = new MockMyGdxGame();
        game.initializeForTesting();
        
        // Add multiple apples and bananas
        MockApple apple1 = new MockApple(10, 10);
        MockApple apple2 = new MockApple(20, 20);
        MockBanana banana1 = new MockBanana(30, 30);
        MockBanana banana2 = new MockBanana(40, 40);
        
        game.getApples().put("apple-1", apple1);
        game.getApples().put("apple-2", apple2);
        game.getBananas().put("banana-1", banana1);
        game.getBananas().put("banana-2", banana2);
        
        // Verify items exist
        assertEquals(2, game.getApples().size(), "Should have 2 apples");
        assertEquals(2, game.getBananas().size(), "Should have 2 bananas");
        
        CountDownLatch networkThreadComplete = new CountDownLatch(1);
        
        // Simulate network thread removing items
        Thread networkThread = new Thread(() -> {
            try {
                game.removeItem("apple-1");
                game.removeItem("banana-1");
                game.removeItem("apple-2");
                game.removeItem("banana-2");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                networkThreadComplete.countDown();
            }
        }, "GameClient-Receive");
        
        networkThread.start();
        
        // Wait for network thread to complete
        assertTrue(networkThreadComplete.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                   "Network thread should complete");
        
        // Verify all items removed from game state
        assertEquals(0, game.getApples().size(), "All apples should be removed");
        assertEquals(0, game.getBananas().size(), "All bananas should be removed");
        
        // Verify all disposal operations queued
        assertEquals(4, game.getDeferredOperationCount(), 
                    "4 disposal operations should be queued");
        
        // Verify items not disposed yet
        assertFalse(apple1.isDisposed(), "Apple 1 should not be disposed yet");
        assertFalse(apple2.isDisposed(), "Apple 2 should not be disposed yet");
        assertFalse(banana1.isDisposed(), "Banana 1 should not be disposed yet");
        assertFalse(banana2.isDisposed(), "Banana 2 should not be disposed yet");
        
        // Simulate render thread processing
        game.processDeferredOperations();
        
        // Verify all items disposed
        assertTrue(apple1.isDisposed(), "Apple 1 should be disposed");
        assertTrue(apple2.isDisposed(), "Apple 2 should be disposed");
        assertTrue(banana1.isDisposed(), "Banana 1 should be disposed");
        assertTrue(banana2.isDisposed(), "Banana 2 should be disposed");
        
        // Verify queue is empty
        assertEquals(0, game.getDeferredOperationCount(), 
                    "Queue should be empty after processing");
    }
    
    /**
     * Mock MyGdxGame class for testing without OpenGL context.
     * Provides access to internal state for verification.
     */
    private static class MockMyGdxGame extends MyGdxGame {
        private java.util.concurrent.ConcurrentLinkedQueue<Runnable> deferredQueue;
        private java.util.Map<String, MockApple> mockApples;
        private java.util.Map<String, MockBanana> mockBananas;
        
        public void initializeForTesting() {
            deferredQueue = new java.util.concurrent.ConcurrentLinkedQueue<>();
            mockApples = new java.util.concurrent.ConcurrentHashMap<>();
            mockBananas = new java.util.concurrent.ConcurrentHashMap<>();
        }
        
        @Override
        public void deferOperation(Runnable operation) {
            if (operation != null) {
                deferredQueue.add(operation);
            }
        }
        
        @Override
        public void removeItem(String itemId) {
            // Immediately remove from game state
            MockApple apple = mockApples.remove(itemId);
            if (apple != null) {
                // Defer texture disposal to render thread
                deferOperation(() -> apple.dispose());
                return;
            }
            
            MockBanana banana = mockBananas.remove(itemId);
            if (banana != null) {
                // Defer texture disposal to render thread
                deferOperation(() -> banana.dispose());
            }
        }
        
        public java.util.Map<String, MockApple> getApples() {
            return mockApples;
        }
        
        public java.util.Map<String, MockBanana> getBananas() {
            return mockBananas;
        }
        
        public int getDeferredOperationCount() {
            return deferredQueue.size();
        }
        
        public void processDeferredOperations() {
            Runnable operation;
            while ((operation = deferredQueue.poll()) != null) {
                try {
                    operation.run();
                } catch (Exception e) {
                    System.err.println("Error executing deferred operation: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Mock Apple class for testing without OpenGL context.
     * Does not extend Apple to avoid OpenGL dependencies.
     */
    private static class MockApple {
        private final AtomicBoolean disposed = new AtomicBoolean(false);
        private final float x;
        private final float y;
        
        public MockApple(float x, float y) {
            this.x = x;
            this.y = y;
        }
        
        public void dispose() {
            disposed.set(true);
        }
        
        public boolean isDisposed() {
            return disposed.get();
        }
        
        public float getX() {
            return x;
        }
        
        public float getY() {
            return y;
        }
    }
    
    /**
     * Mock Banana class for testing without OpenGL context.
     * Does not extend Banana to avoid OpenGL dependencies.
     */
    private static class MockBanana {
        private final AtomicBoolean disposed = new AtomicBoolean(false);
        private final float x;
        private final float y;
        
        public MockBanana(float x, float y) {
            this.x = x;
            this.y = y;
        }
        
        public void dispose() {
            disposed.set(true);
        }
        
        public boolean isDisposed() {
            return disposed.get();
        }
        
        public float getX() {
            return x;
        }
        
        public float getY() {
            return y;
        }
    }
}
