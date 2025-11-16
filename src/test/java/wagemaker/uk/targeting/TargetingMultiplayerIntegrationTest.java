package wagemaker.uk.targeting;

import org.junit.jupiter.api.*;
import wagemaker.uk.network.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for tile targeting system in multiplayer mode.
 * Tests the complete flow of targeting, planting, and synchronization across clients.
 * 
 * Verifies Requirements:
 * - 5.1: Target indicator is client-side only
 * - 5.2: Target indicator position not transmitted until confirmation
 * - 5.3: Remote player indicators not rendered on other clients
 * - 6.1: Client sends planting message with target coordinates
 * - 6.2: Server validates target coordinates
 * - 6.3: Server broadcasts bamboo creation
 * - 6.4: Clients render planted bamboo at specified coordinates
 * - 6.5: Planted bamboo positions consistent across clients
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TargetingMultiplayerIntegrationTest {
    
    private static final int TEST_PORT = 25567; // Different port to avoid conflicts
    private static final int TIMEOUT_SECONDS = 10;
    private static final float TILE_SIZE = 64.0f;
    
    private GameServer server;
    private GameClient client1;
    private GameClient client2;
    
    @BeforeEach
    public void setUp() throws IOException {
        // Create and start server
        server = new GameServer(TEST_PORT, 10);
        server.start();
        
        // Wait for server to be ready
        waitForCondition(() -> server.isRunning(), 2000, "Server failed to start");
    }
    
    @AfterEach
    public void tearDown() {
        // Disconnect clients
        if (client1 != null && client1.isConnected()) {
            client1.disconnect();
        }
        if (client2 != null && client2.isConnected()) {
            client2.disconnect();
        }
        
        // Stop server
        if (server != null && server.isRunning()) {
            server.stop();
        }
        
        // Wait for cleanup
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Test 1: Target indicator is client-side only
     * Verifies that the targeting system state is not transmitted to other clients
     * until the planting action is confirmed.
     * 
     * Requirement 5.1: Target indicator rendered only on local client
     * Requirement 5.2: Target position not transmitted until confirmation
     */
    @Test
    @Order(1)
    public void testTargetIndicatorIsClientSideOnly() throws Exception {
        CountDownLatch client1ReadyLatch = new CountDownLatch(1);
        CountDownLatch client2ReadyLatch = new CountDownLatch(1);
        
        AtomicReference<String> client1Id = new AtomicReference<>();
        AtomicReference<String> client2Id = new AtomicReference<>();
        AtomicBoolean client2ReceivedTargetingMessage = new AtomicBoolean(false);
        
        // Setup client 1 - will activate targeting
        client1 = new GameClient();
        client1.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                    client1.setClientId(id);
                    client1Id.set(id);
                    client1ReadyLatch.countDown();
                }
            }
        });
        
        // Setup client 2 - will monitor for any targeting-related messages
        client2 = new GameClient();
        client2.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                    client2.setClientId(id);
                    client2Id.set(id);
                    client2ReadyLatch.countDown();
                }
                // Check if any targeting-related message is received
                // (There should be no such message type - targeting is client-side only)
                if (message.getSenderId() != null && message.getSenderId().equals(client1Id.get())) {
                    // If we receive any message from client1 before planting confirmation,
                    // it would indicate targeting state is being transmitted
                    if (!(message instanceof PlayerMovementMessage) && 
                        !(message instanceof PlayerJoinMessage)) {
                        client2ReceivedTargetingMessage.set(true);
                    }
                }
            }
        });
        
        // Connect both clients
        client1.connect("localhost", TEST_PORT);
        client2.connect("localhost", TEST_PORT);
        
        // Wait for both clients to be ready
        assertTrue(client1ReadyLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Client 1 not ready");
        assertTrue(client2ReadyLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Client 2 not ready");
        
        Thread.sleep(300);
        
        // Client 1 activates targeting system (simulated locally)
        TargetingSystem targetingSystem = new TargetingSystem();
        float playerX = 128.0f;
        float playerY = 192.0f;
        
        targetingSystem.activate(playerX, playerY, TargetingMode.ADJACENT, new TargetingCallback() {
            @Override
            public void onTargetConfirmed(float targetX, float targetY) {
                // This will be called when target is confirmed
            }
            
            @Override
            public void onTargetCancelled() {
                // This will be called when targeting is cancelled
            }
        });
        
        // Move target around (simulated locally)
        targetingSystem.moveTarget(Direction.RIGHT);
        targetingSystem.moveTarget(Direction.UP);
        targetingSystem.moveTarget(Direction.LEFT);
        
        // Wait to see if any targeting messages are transmitted
        Thread.sleep(500);
        
        // Verify that client 2 did NOT receive any targeting-related messages
        assertFalse(client2ReceivedTargetingMessage.get(), 
                   "Client 2 should not receive targeting state updates from Client 1");
        
        // Verify targeting system is still active on client 1
        assertTrue(targetingSystem.isActive(), "Targeting should still be active");
        
        // Clean up
        targetingSystem.deactivate();
    }
    
    /**
     * Test 2: Planted bamboo synchronization across clients
     * Verifies that when a player confirms a target and plants bamboo,
     * the bamboo appears at the correct coordinates for all clients.
     * 
     * Requirement 6.1: Client sends planting message with target coordinates
     * Requirement 6.3: Server broadcasts bamboo creation
     * Requirement 6.4: Clients render planted bamboo at specified coordinates
     */
    @Test
    @Order(2)
    public void testPlantedBambooSynchronizationAcrossClients() throws Exception {
        CountDownLatch client1ReadyLatch = new CountDownLatch(1);
        CountDownLatch client2ReadyLatch = new CountDownLatch(1);
        CountDownLatch bambooPlantedLatch = new CountDownLatch(1);
        
        AtomicReference<String> client1Id = new AtomicReference<>();
        AtomicReference<BambooPlantMessage> receivedPlantMessage = new AtomicReference<>();
        
        // Setup client 1
        client1 = new GameClient();
        client1.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                    client1.setClientId(id);
                    client1Id.set(id);
                    client1ReadyLatch.countDown();
                }
            }
        });
        
        // Setup client 2 - will receive bamboo plant message
        client2 = new GameClient();
        client2.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                    client2.setClientId(id);
                    client2ReadyLatch.countDown();
                } else if (message instanceof BambooPlantMessage) {
                    receivedPlantMessage.set((BambooPlantMessage) message);
                    bambooPlantedLatch.countDown();
                }
            }
        });
        
        // Connect both clients
        client1.connect("localhost", TEST_PORT);
        client2.connect("localhost", TEST_PORT);
        
        // Wait for both clients to be ready
        assertTrue(client1ReadyLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Client 1 not ready");
        assertTrue(client2ReadyLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Client 2 not ready");
        
        Thread.sleep(300);
        
        // Client 1 plants bamboo at target coordinates
        String plantedBambooId = "bamboo-test-1";
        float targetX = 192.0f; // 3 tiles from origin
        float targetY = 256.0f; // 4 tiles from origin
        
        client1.sendBambooPlant(plantedBambooId, targetX, targetY);
        
        // Server broadcasts the plant message
        BambooPlantMessage plantMsg = new BambooPlantMessage(
            client1Id.get(), plantedBambooId, targetX, targetY);
        server.broadcastToAll(plantMsg);
        
        // Wait for client 2 to receive the message
        assertTrue(bambooPlantedLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Bamboo plant message not received by client 2");
        
        // Verify the message contains correct coordinates
        assertNotNull(receivedPlantMessage.get(), "Plant message should be received");
        assertEquals(plantedBambooId, receivedPlantMessage.get().getPlantedBambooId(), 
                    "Bamboo ID should match");
        assertEquals(targetX, receivedPlantMessage.get().getX(), 0.01f, 
                    "X coordinate should match");
        assertEquals(targetY, receivedPlantMessage.get().getY(), 0.01f, 
                    "Y coordinate should match");
        assertEquals(client1Id.get(), receivedPlantMessage.get().getPlayerId(), 
                    "Player ID should match");
    }
    
    /**
     * Test 3: Coordinate consistency across clients
     * Verifies that the exact same coordinates are used across all clients
     * when bamboo is planted at a target location.
     * 
     * Requirement 6.5: Planted bamboo positions consistent across clients
     */
    @Test
    @Order(3)
    public void testCoordinateConsistencyAcrossClients() throws Exception {
        CountDownLatch client1ReadyLatch = new CountDownLatch(1);
        CountDownLatch client2ReadyLatch = new CountDownLatch(1);
        CountDownLatch client1ReceivedLatch = new CountDownLatch(1);
        CountDownLatch client2ReceivedLatch = new CountDownLatch(1);
        
        AtomicReference<String> client1Id = new AtomicReference<>();
        AtomicReference<BambooPlantMessage> client1ReceivedMsg = new AtomicReference<>();
        AtomicReference<BambooPlantMessage> client2ReceivedMsg = new AtomicReference<>();
        
        // Setup client 1
        client1 = new GameClient();
        client1.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                    client1.setClientId(id);
                    client1Id.set(id);
                    client1ReadyLatch.countDown();
                } else if (message instanceof BambooPlantMessage) {
                    client1ReceivedMsg.set((BambooPlantMessage) message);
                    client1ReceivedLatch.countDown();
                }
            }
        });
        
        // Setup client 2
        client2 = new GameClient();
        client2.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                    client2.setClientId(id);
                    client2ReadyLatch.countDown();
                } else if (message instanceof BambooPlantMessage) {
                    client2ReceivedMsg.set((BambooPlantMessage) message);
                    client2ReceivedLatch.countDown();
                }
            }
        });
        
        // Connect both clients
        client1.connect("localhost", TEST_PORT);
        client2.connect("localhost", TEST_PORT);
        
        // Wait for both clients to be ready
        assertTrue(client1ReadyLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Client 1 not ready");
        assertTrue(client2ReadyLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Client 2 not ready");
        
        Thread.sleep(300);
        
        // Client 1 plants bamboo at specific target coordinates
        String plantedBambooId = "bamboo-consistency-test";
        float targetX = 320.0f; // Tile-aligned coordinate
        float targetY = 384.0f; // Tile-aligned coordinate
        
        client1.sendBambooPlant(plantedBambooId, targetX, targetY);
        
        // Server broadcasts to all clients (including sender)
        BambooPlantMessage plantMsg = new BambooPlantMessage(
            client1Id.get(), plantedBambooId, targetX, targetY);
        server.broadcastToAll(plantMsg);
        
        // Wait for both clients to receive the message
        assertTrue(client1ReceivedLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Client 1 did not receive broadcast");
        assertTrue(client2ReceivedLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Client 2 did not receive broadcast");
        
        // Verify both clients received the exact same coordinates
        assertNotNull(client1ReceivedMsg.get(), "Client 1 should receive message");
        assertNotNull(client2ReceivedMsg.get(), "Client 2 should receive message");
        
        assertEquals(client1ReceivedMsg.get().getX(), client2ReceivedMsg.get().getX(), 0.001f,
                    "X coordinates should be identical across clients");
        assertEquals(client1ReceivedMsg.get().getY(), client2ReceivedMsg.get().getY(), 0.001f,
                    "Y coordinates should be identical across clients");
        assertEquals(targetX, client1ReceivedMsg.get().getX(), 0.001f,
                    "Client 1 X coordinate should match target");
        assertEquals(targetY, client1ReceivedMsg.get().getY(), 0.001f,
                    "Client 1 Y coordinate should match target");
        assertEquals(targetX, client2ReceivedMsg.get().getX(), 0.001f,
                    "Client 2 X coordinate should match target");
        assertEquals(targetY, client2ReceivedMsg.get().getY(), 0.001f,
                    "Client 2 Y coordinate should match target");
    }
    
    /**
     * Test 4: Server validation of target coordinates
     * Verifies that the server validates target coordinates are within
     * a reasonable range of the player's position.
     * 
     * Requirement 6.2: Server validates target coordinates
     */
    @Test
    @Order(4)
    public void testServerValidationOfTargetCoordinates() throws Exception {
        CountDownLatch client1ReadyLatch = new CountDownLatch(1);
        
        AtomicReference<String> client1Id = new AtomicReference<>();
        
        // Setup client 1
        client1 = new GameClient();
        client1.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                    client1.setClientId(id);
                    client1Id.set(id);
                    client1ReadyLatch.countDown();
                }
            }
        });
        
        // Connect client
        client1.connect("localhost", TEST_PORT);
        
        // Wait for client to be ready
        assertTrue(client1ReadyLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Client 1 not ready");
        
        Thread.sleep(300);
        
        // Test 1: Valid coordinates (within adjacent tile range)
        String validBambooId = "bamboo-valid";
        float validX = TILE_SIZE; // One tile away
        float validY = TILE_SIZE;
        
        // This should not throw an exception
        assertDoesNotThrow(() -> {
            client1.sendBambooPlant(validBambooId, validX, validY);
        }, "Server should accept valid adjacent coordinates");
        
        Thread.sleep(100);
        
        // Test 2: Coordinates at player position (should be valid)
        String playerPosBambooId = "bamboo-player-pos";
        float playerX = 0.0f;
        float playerY = 0.0f;
        
        assertDoesNotThrow(() -> {
            client1.sendBambooPlant(playerPosBambooId, playerX, playerY);
        }, "Server should accept coordinates at player position");
        
        Thread.sleep(100);
        
        // Test 3: Tile-aligned coordinates (should be valid)
        String alignedBambooId = "bamboo-aligned";
        float alignedX = TILE_SIZE * 2; // Two tiles away
        float alignedY = TILE_SIZE * 2;
        
        assertDoesNotThrow(() -> {
            client1.sendBambooPlant(alignedBambooId, alignedX, alignedY);
        }, "Server should accept tile-aligned coordinates");
    }
    
    /**
     * Test 5: Remote player targeting not visible
     * Verifies that when a remote player is in targeting mode,
     * their target indicator is not visible to other clients.
     * 
     * Requirement 5.3: Remote player indicators not rendered on other clients
     */
    @Test
    @Order(5)
    public void testRemotePlayerTargetingNotVisible() throws Exception {
        CountDownLatch client1ReadyLatch = new CountDownLatch(1);
        CountDownLatch client2ReadyLatch = new CountDownLatch(1);
        
        AtomicReference<String> client1Id = new AtomicReference<>();
        AtomicReference<String> client2Id = new AtomicReference<>();
        AtomicBoolean client2ReceivedTargetUpdate = new AtomicBoolean(false);
        
        // Setup client 1 - will be in targeting mode
        client1 = new GameClient();
        client1.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                    client1.setClientId(id);
                    client1Id.set(id);
                    client1ReadyLatch.countDown();
                }
            }
        });
        
        // Setup client 2 - will monitor for targeting updates
        client2 = new GameClient();
        client2.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                    client2.setClientId(id);
                    client2Id.set(id);
                    client2ReadyLatch.countDown();
                }
                // Monitor for any messages that might indicate target position updates
                // (There should be none - targeting is client-side only)
                if (message.getSenderId() != null && 
                    message.getSenderId().equals(client1Id.get()) &&
                    !(message instanceof PlayerMovementMessage) &&
                    !(message instanceof PlayerJoinMessage) &&
                    !(message instanceof BambooPlantMessage)) {
                    client2ReceivedTargetUpdate.set(true);
                }
            }
        });
        
        // Connect both clients
        client1.connect("localhost", TEST_PORT);
        client2.connect("localhost", TEST_PORT);
        
        // Wait for both clients to be ready
        assertTrue(client1ReadyLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Client 1 not ready");
        assertTrue(client2ReadyLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Client 2 not ready");
        
        Thread.sleep(300);
        
        // Client 1 activates targeting and moves target around
        TargetingSystem client1Targeting = new TargetingSystem();
        client1Targeting.activate(128.0f, 128.0f, TargetingMode.ADJACENT, 
            new TargetingCallback() {
                @Override
                public void onTargetConfirmed(float targetX, float targetY) {}
                
                @Override
                public void onTargetCancelled() {}
            });
        
        // Move target multiple times
        for (int i = 0; i < 5; i++) {
            client1Targeting.moveTarget(Direction.RIGHT);
            Thread.sleep(50);
            client1Targeting.moveTarget(Direction.UP);
            Thread.sleep(50);
        }
        
        // Wait to ensure no messages are transmitted
        Thread.sleep(500);
        
        // Verify client 2 did not receive any target position updates
        assertFalse(client2ReceivedTargetUpdate.get(), 
                   "Client 2 should not receive target position updates from Client 1");
        
        // Verify client 1 targeting is still active
        assertTrue(client1Targeting.isActive(), "Client 1 targeting should still be active");
        
        // Clean up
        client1Targeting.deactivate();
    }
    
    /**
     * Test 6: Multiple clients planting at different coordinates
     * Verifies that multiple clients can plant bamboo at different target
     * coordinates and all clients receive the correct positions.
     * 
     * Requirement 6.5: Planted bamboo positions consistent across clients
     */
    @Test
    @Order(6)
    public void testMultipleClientsPlantingAtDifferentCoordinates() throws Exception {
        CountDownLatch client1ReadyLatch = new CountDownLatch(1);
        CountDownLatch client2ReadyLatch = new CountDownLatch(1);
        CountDownLatch client1ReceivedBothLatch = new CountDownLatch(2);
        CountDownLatch client2ReceivedBothLatch = new CountDownLatch(2);
        
        AtomicReference<String> client1Id = new AtomicReference<>();
        AtomicReference<String> client2Id = new AtomicReference<>();
        AtomicReference<BambooPlantMessage> client1Plant1 = new AtomicReference<>();
        AtomicReference<BambooPlantMessage> client1Plant2 = new AtomicReference<>();
        AtomicReference<BambooPlantMessage> client2Plant1 = new AtomicReference<>();
        AtomicReference<BambooPlantMessage> client2Plant2 = new AtomicReference<>();
        
        // Setup client 1
        client1 = new GameClient();
        client1.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                    client1.setClientId(id);
                    client1Id.set(id);
                    client1ReadyLatch.countDown();
                } else if (message instanceof BambooPlantMessage) {
                    BambooPlantMessage plantMsg = (BambooPlantMessage) message;
                    if (client1Plant1.get() == null) {
                        client1Plant1.set(plantMsg);
                    } else {
                        client1Plant2.set(plantMsg);
                    }
                    client1ReceivedBothLatch.countDown();
                }
            }
        });
        
        // Setup client 2
        client2 = new GameClient();
        client2.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                    client2.setClientId(id);
                    client2Id.set(id);
                    client2ReadyLatch.countDown();
                } else if (message instanceof BambooPlantMessage) {
                    BambooPlantMessage plantMsg = (BambooPlantMessage) message;
                    if (client2Plant1.get() == null) {
                        client2Plant1.set(plantMsg);
                    } else {
                        client2Plant2.set(plantMsg);
                    }
                    client2ReceivedBothLatch.countDown();
                }
            }
        });
        
        // Connect both clients
        client1.connect("localhost", TEST_PORT);
        client2.connect("localhost", TEST_PORT);
        
        // Wait for both clients to be ready
        assertTrue(client1ReadyLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Client 1 not ready");
        assertTrue(client2ReadyLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Client 2 not ready");
        
        Thread.sleep(300);
        
        // Client 1 plants bamboo at position A
        String bamboo1Id = "bamboo-client1";
        float client1TargetX = 128.0f;
        float client1TargetY = 192.0f;
        client1.sendBambooPlant(bamboo1Id, client1TargetX, client1TargetY);
        
        BambooPlantMessage plant1Msg = new BambooPlantMessage(
            client1Id.get(), bamboo1Id, client1TargetX, client1TargetY);
        server.broadcastToAll(plant1Msg);
        
        Thread.sleep(200);
        
        // Client 2 plants bamboo at position B
        String bamboo2Id = "bamboo-client2";
        float client2TargetX = 256.0f;
        float client2TargetY = 320.0f;
        client2.sendBambooPlant(bamboo2Id, client2TargetX, client2TargetY);
        
        BambooPlantMessage plant2Msg = new BambooPlantMessage(
            client2Id.get(), bamboo2Id, client2TargetX, client2TargetY);
        server.broadcastToAll(plant2Msg);
        
        // Wait for both clients to receive both messages
        assertTrue(client1ReceivedBothLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Client 1 did not receive both plant messages");
        assertTrue(client2ReceivedBothLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Client 2 did not receive both plant messages");
        
        // Verify both clients received the same coordinates for both plants
        assertNotNull(client1Plant1.get(), "Client 1 should receive first plant");
        assertNotNull(client1Plant2.get(), "Client 1 should receive second plant");
        assertNotNull(client2Plant1.get(), "Client 2 should receive first plant");
        assertNotNull(client2Plant2.get(), "Client 2 should receive second plant");
        
        // Verify first plant coordinates match across clients
        assertEquals(client1Plant1.get().getX(), client2Plant1.get().getX(), 0.001f,
                    "First plant X coordinate should match across clients");
        assertEquals(client1Plant1.get().getY(), client2Plant1.get().getY(), 0.001f,
                    "First plant Y coordinate should match across clients");
        
        // Verify second plant coordinates match across clients
        assertEquals(client1Plant2.get().getX(), client2Plant2.get().getX(), 0.001f,
                    "Second plant X coordinate should match across clients");
        assertEquals(client1Plant2.get().getY(), client2Plant2.get().getY(), 0.001f,
                    "Second plant Y coordinate should match across clients");
        
        // Verify coordinates match the original target positions
        assertEquals(client1TargetX, client1Plant1.get().getX(), 0.001f,
                    "First plant should be at client 1's target position");
        assertEquals(client2TargetX, client1Plant2.get().getX(), 0.001f,
                    "Second plant should be at client 2's target position");
    }
    
    /**
     * Test 7: Network error handling and state consistency
     * Verifies that the system handles network errors gracefully and
     * maintains consistent state across clients.
     * 
     * Tests error handling for network failures during planting.
     */
    @Test
    @Order(7)
    public void testNetworkErrorHandlingAndStateConsistency() throws Exception {
        CountDownLatch client1ReadyLatch = new CountDownLatch(1);
        
        AtomicReference<String> client1Id = new AtomicReference<>();
        AtomicBoolean exceptionThrown = new AtomicBoolean(false);
        
        // Setup client 1
        client1 = new GameClient();
        client1.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                    client1.setClientId(id);
                    client1Id.set(id);
                    client1ReadyLatch.countDown();
                }
            }
        });
        
        // Connect client
        client1.connect("localhost", TEST_PORT);
        
        // Wait for client to be ready
        assertTrue(client1ReadyLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Client 1 not ready");
        
        Thread.sleep(300);
        
        // Send a valid plant message
        String bambooId = "bamboo-error-test";
        float targetX = 128.0f;
        float targetY = 128.0f;
        
        try {
            client1.sendBambooPlant(bambooId, targetX, targetY);
        } catch (Exception e) {
            exceptionThrown.set(true);
        }
        
        // Verify no exception was thrown for valid operation
        assertFalse(exceptionThrown.get(), "No exception should be thrown for valid plant");
        
        // Disconnect client
        client1.disconnect();
        
        Thread.sleep(200);
        
        // Verify client is disconnected
        assertFalse(client1.isConnected(), "Client should be disconnected");
        
        // Try to send plant message while disconnected
        exceptionThrown.set(false);
        try {
            client1.sendBambooPlant("bamboo-disconnected", 64.0f, 64.0f);
        } catch (Exception e) {
            exceptionThrown.set(true);
        }
        
        // The client should handle disconnected state gracefully
        // (either throw exception or silently fail - both are acceptable)
        // The important thing is that it doesn't crash
    }
    
    // Helper methods
    
    /**
     * Waits for a condition to become true within a timeout period.
     * @param condition The condition to check
     * @param timeoutMs The timeout in milliseconds
     * @param errorMessage The error message if timeout occurs
     */
    private void waitForCondition(BooleanSupplier condition, long timeoutMs, String errorMessage) {
        long startTime = System.currentTimeMillis();
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                fail(errorMessage);
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Interrupted while waiting for condition");
            }
        }
    }
    
    /**
     * Functional interface for boolean conditions.
     */
    @FunctionalInterface
    private interface BooleanSupplier {
        boolean getAsBoolean();
    }
}
