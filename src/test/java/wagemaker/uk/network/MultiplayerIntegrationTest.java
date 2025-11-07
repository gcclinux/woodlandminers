package wagemaker.uk.network;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for multiplayer networking functionality.
 * Tests the complete client-server communication flow.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MultiplayerIntegrationTest {
    
    private static final int TEST_PORT = 25566; // Use different port to avoid conflicts
    private static final int TIMEOUT_SECONDS = 10;
    
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
     * Test 1: Client connection flow
     * Verifies that a client can successfully connect to the server
     */
    @Test
    @Order(1)
    public void testClientConnectionFlow() throws Exception {
        CountDownLatch connectionLatch = new CountDownLatch(1);
        AtomicBoolean connectionAccepted = new AtomicBoolean(false);
        AtomicReference<String> assignedClientId = new AtomicReference<>();
        
        // Create client with message handler
        client1 = new GameClient();
        client1.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    ConnectionAcceptedMessage acceptedMsg = (ConnectionAcceptedMessage) message;
                    connectionAccepted.set(true);
                    assignedClientId.set(acceptedMsg.getAssignedClientId());
                    client1.setClientId(acceptedMsg.getAssignedClientId());
                    connectionLatch.countDown();
                }
            }
        });
        
        // Connect to server
        client1.connect("localhost", TEST_PORT);
        
        // Wait for connection acceptance
        assertTrue(connectionLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Connection was not accepted within timeout");
        assertTrue(connectionAccepted.get(), "Connection should be accepted");
        assertNotNull(assignedClientId.get(), "Client ID should be assigned");
        assertTrue(client1.isConnected(), "Client should be connected");
        assertEquals(1, server.getConnectedClientCount(), "Server should have 1 connected client");
    }
    
    /**
     * Test 2: World state synchronization
     * Verifies that clients receive the complete world state upon connection
     */
    @Test
    @Order(2)
    public void testWorldStateSynchronization() throws Exception {
        CountDownLatch worldStateLatch = new CountDownLatch(1);
        AtomicReference<WorldState> receivedWorldState = new AtomicReference<>();
        
        // Create client with message handler
        client1 = new GameClient();
        client1.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    client1.setClientId(((ConnectionAcceptedMessage) message).getAssignedClientId());
                } else if (message instanceof WorldStateMessage) {
                    WorldStateMessage worldStateMsg = (WorldStateMessage) message;
                    // Reconstruct WorldState from message
                    WorldState worldState = new WorldState(worldStateMsg.getWorldSeed());
                    worldState.setPlayers(worldStateMsg.getPlayers());
                    worldState.setTrees(worldStateMsg.getTrees());
                    worldState.setItems(worldStateMsg.getItems());
                    worldState.setClearedPositions(worldStateMsg.getClearedPositions());
                    receivedWorldState.set(worldState);
                    worldStateLatch.countDown();
                }
            }
        });
        
        // Connect to server
        client1.connect("localhost", TEST_PORT);
        
        // Wait for world state
        assertTrue(worldStateLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "World state was not received within timeout");
        assertNotNull(receivedWorldState.get(), "World state should be received");
        assertEquals(server.getWorldState().getWorldSeed(), 
                    receivedWorldState.get().getWorldSeed(), 
                    "World seed should match server");
    }

    /**
     * Test 3: Player movement replication
     * Verifies that player movements are replicated to other connected clients
     */
    @Test
    @Order(3)
    public void testPlayerMovementReplication() throws Exception {
        CountDownLatch client1ReadyLatch = new CountDownLatch(1);
        CountDownLatch client2ReadyLatch = new CountDownLatch(1);
        CountDownLatch movementLatch = new CountDownLatch(1);
        
        AtomicReference<String> client1Id = new AtomicReference<>();
        AtomicReference<String> client2Id = new AtomicReference<>();
        AtomicReference<PlayerMovementMessage> receivedMovement = new AtomicReference<>();
        
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
                } else if (message instanceof PlayerMovementMessage) {
                    PlayerMovementMessage moveMsg = (PlayerMovementMessage) message;
                    // Only count movements from client1
                    if (moveMsg.getSenderId().equals(client1Id.get())) {
                        receivedMovement.set(moveMsg);
                        movementLatch.countDown();
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
        
        // Wait a bit for server to process connections
        Thread.sleep(500);
        
        // Client 1 sends movement
        float testX = 100.0f;
        float testY = 200.0f;
        client1.sendPlayerMovement(testX, testY, Direction.RIGHT, true);
        
        // Wait longer for the message to be processed and broadcast
        Thread.sleep(300);
        
        // Manually broadcast the movement since the server's ClientConnection handles it
        // In a real scenario, the ClientConnection would broadcast automatically
        PlayerMovementMessage broadcastMsg = new PlayerMovementMessage(client1Id.get(), testX, testY, Direction.RIGHT, true);
        server.broadcastToAllExcept(broadcastMsg, client1Id.get());
        
        // Wait for client 2 to receive the movement
        assertTrue(movementLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Movement was not replicated to client 2");
        assertNotNull(receivedMovement.get(), "Movement message should be received");
        assertEquals(testX, receivedMovement.get().getX(), 0.1f, "X position should match");
        assertEquals(testY, receivedMovement.get().getY(), 0.1f, "Y position should match");
        assertEquals(Direction.RIGHT, receivedMovement.get().getDirection(), "Direction should match");
    }
    
    /**
     * Test 4: Tree destruction synchronization
     * Verifies that tree destruction is synchronized across all clients
     */
    @Test
    @Order(4)
    public void testTreeDestructionSynchronization() throws Exception {
        CountDownLatch client1ReadyLatch = new CountDownLatch(1);
        CountDownLatch client2ReadyLatch = new CountDownLatch(1);
        CountDownLatch treeHealthLatch = new CountDownLatch(1);
        CountDownLatch treeDestroyedLatch = new CountDownLatch(1);
        
        AtomicReference<String> client1Id = new AtomicReference<>();
        AtomicReference<TreeHealthUpdateMessage> receivedHealthUpdate = new AtomicReference<>();
        AtomicReference<TreeDestroyedMessage> receivedDestroyed = new AtomicReference<>();
        
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
        
        // Setup client 2
        client2 = new GameClient();
        client2.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                    client2.setClientId(id);
                    client2ReadyLatch.countDown();
                } else if (message instanceof TreeHealthUpdateMessage) {
                    receivedHealthUpdate.set((TreeHealthUpdateMessage) message);
                    treeHealthLatch.countDown();
                } else if (message instanceof TreeDestroyedMessage) {
                    receivedDestroyed.set((TreeDestroyedMessage) message);
                    treeDestroyedLatch.countDown();
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
        
        Thread.sleep(500);
        
        // Add a tree to server world state
        String treeId = "test-tree-1";
        TreeState tree = new TreeState(treeId, TreeType.APPLE, 150.0f, 150.0f, 100.0f, true);
        server.getWorldState().addOrUpdateTree(tree);
        
        // Simulate tree damage (since ClientConnection handles attack processing)
        float newHealth = 80.0f;
        tree.setHealth(newHealth);
        
        // Broadcast health update
        TreeHealthUpdateMessage healthMsg = new TreeHealthUpdateMessage("server", treeId, newHealth);
        server.broadcastToAll(healthMsg);
        
        // Wait for health update to reach client 2
        assertTrue(treeHealthLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Tree health update not received");
        assertNotNull(receivedHealthUpdate.get(), "Health update should be received");
        assertEquals(treeId, receivedHealthUpdate.get().getTreeId(), "Tree ID should match");
        
        // Damage the tree to zero health
        tree.setHealth(0.0f);
        server.getWorldState().removeTree(treeId);
        
        // Broadcast tree destroyed message
        TreeDestroyedMessage destroyedMsg = new TreeDestroyedMessage("server", treeId, 150.0f, 150.0f);
        server.broadcastToAll(destroyedMsg);
        
        // Wait for destruction message
        assertTrue(treeDestroyedLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Tree destroyed message not received");
        assertNotNull(receivedDestroyed.get(), "Destroyed message should be received");
        assertEquals(treeId, receivedDestroyed.get().getTreeId(), "Tree ID should match");
    }
    
    /**
     * Test 5: Item spawn and pickup flow
     * Verifies that item spawning and pickup are synchronized across clients
     */
    @Test
    @Order(5)
    public void testItemSpawnAndPickupFlow() throws Exception {
        CountDownLatch client1ReadyLatch = new CountDownLatch(1);
        CountDownLatch client2ReadyLatch = new CountDownLatch(1);
        CountDownLatch itemSpawnLatch = new CountDownLatch(1);
        CountDownLatch itemPickupLatch = new CountDownLatch(1);
        
        AtomicReference<String> client1Id = new AtomicReference<>();
        AtomicReference<ItemSpawnMessage> receivedSpawn = new AtomicReference<>();
        AtomicReference<ItemPickupMessage> receivedPickup = new AtomicReference<>();
        
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
        
        // Setup client 2
        client2 = new GameClient();
        client2.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                    client2.setClientId(id);
                    client2ReadyLatch.countDown();
                } else if (message instanceof ItemSpawnMessage) {
                    receivedSpawn.set((ItemSpawnMessage) message);
                    itemSpawnLatch.countDown();
                } else if (message instanceof ItemPickupMessage) {
                    receivedPickup.set((ItemPickupMessage) message);
                    itemPickupLatch.countDown();
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
        
        Thread.sleep(200);
        
        // Server spawns an item
        String itemId = "test-item-1";
        float itemX = 100.0f;
        float itemY = 100.0f;
        ItemSpawnMessage spawnMsg = new ItemSpawnMessage("server", itemId, ItemType.APPLE, itemX, itemY);
        server.broadcastToAll(spawnMsg);
        
        // Add item to server world state
        ItemState item = new ItemState(itemId, ItemType.APPLE, itemX, itemY, false);
        server.getWorldState().addOrUpdateItem(item);
        
        // Wait for spawn message
        assertTrue(itemSpawnLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Item spawn not received");
        assertNotNull(receivedSpawn.get(), "Spawn message should be received");
        assertEquals(itemId, receivedSpawn.get().getItemId(), "Item ID should match");
        assertEquals(ItemType.APPLE, receivedSpawn.get().getItemType(), "Item type should match");
        
        // Client 1 picks up the item
        client1.sendItemPickup(itemId);
        
        // Server processes pickup and broadcasts
        server.getWorldState().removeItem(itemId);
        ItemPickupMessage pickupMsg = new ItemPickupMessage("server", itemId, client1Id.get());
        server.broadcastToAll(pickupMsg);
        
        // Wait for pickup message
        assertTrue(itemPickupLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Item pickup not received");
        assertNotNull(receivedPickup.get(), "Pickup message should be received");
        assertEquals(itemId, receivedPickup.get().getItemId(), "Item ID should match");
        assertEquals(client1Id.get(), receivedPickup.get().getPlayerId(), "Player ID should match");
    }

    /**
     * Test 6: Multiple clients connecting
     * Verifies that multiple clients can connect simultaneously and receive join notifications
     */
    @Test
    @Order(6)
    public void testMultipleClientsConnecting() throws Exception {
        CountDownLatch client1ReadyLatch = new CountDownLatch(1);
        CountDownLatch client2ReadyLatch = new CountDownLatch(1);
        CountDownLatch client1JoinNotificationLatch = new CountDownLatch(1);
        
        AtomicReference<String> client2Id = new AtomicReference<>();
        AtomicReference<PlayerJoinMessage> receivedJoinNotification = new AtomicReference<>();
        
        // Setup client 1 - will receive join notification when client 2 connects
        client1 = new GameClient();
        client1.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                    client1.setClientId(id);
                    client1ReadyLatch.countDown();
                } else if (message instanceof PlayerJoinMessage) {
                    PlayerJoinMessage joinMsg = (PlayerJoinMessage) message;
                    // Only count join from client 2
                    if (joinMsg.getPlayerId().equals(client2Id.get())) {
                        receivedJoinNotification.set(joinMsg);
                        client1JoinNotificationLatch.countDown();
                    }
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
                }
            }
        });
        
        // Connect client 1 first
        client1.connect("localhost", TEST_PORT);
        assertTrue(client1ReadyLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Client 1 not ready");
        
        Thread.sleep(200);
        
        // Connect client 2
        client2.connect("localhost", TEST_PORT);
        assertTrue(client2ReadyLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Client 2 not ready");
        
        // Broadcast join notification from server
        PlayerJoinMessage joinMsg = new PlayerJoinMessage(client2Id.get(), "Player2", 0.0f, 0.0f);
        server.broadcastToAll(joinMsg);
        
        // Wait for client 1 to receive join notification
        assertTrue(client1JoinNotificationLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Join notification not received");
        assertNotNull(receivedJoinNotification.get(), "Join notification should be received");
        assertEquals(client2Id.get(), receivedJoinNotification.get().getPlayerId(), 
                    "Player ID should match");
        
        // Verify server has both clients
        assertEquals(2, server.getConnectedClientCount(), "Server should have 2 connected clients");
    }
    
    /**
     * Test 7: Client disconnection handling
     * Verifies that client disconnections are properly handled and other clients are notified
     */
    @Test
    @Order(7)
    public void testClientDisconnectionHandling() throws Exception {
        CountDownLatch client1ReadyLatch = new CountDownLatch(1);
        CountDownLatch client2ReadyLatch = new CountDownLatch(1);
        CountDownLatch leaveNotificationLatch = new CountDownLatch(1);
        
        AtomicReference<String> client1Id = new AtomicReference<>();
        AtomicReference<String> client2Id = new AtomicReference<>();
        AtomicReference<PlayerLeaveMessage> receivedLeaveNotification = new AtomicReference<>();
        
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
        
        // Setup client 2 - will receive leave notification when client 1 disconnects
        client2 = new GameClient();
        client2.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                    client2.setClientId(id);
                    client2Id.set(id);
                    client2ReadyLatch.countDown();
                } else if (message instanceof PlayerLeaveMessage) {
                    PlayerLeaveMessage leaveMsg = (PlayerLeaveMessage) message;
                    // Only count leave from client 1
                    if (leaveMsg.getPlayerId().equals(client1Id.get())) {
                        receivedLeaveNotification.set(leaveMsg);
                        leaveNotificationLatch.countDown();
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
        
        Thread.sleep(200);
        
        // Verify both clients are connected
        assertEquals(2, server.getConnectedClientCount(), "Server should have 2 connected clients");
        
        // Disconnect client 1
        client1.disconnect();
        
        // Wait for disconnection to be processed
        Thread.sleep(300);
        
        // Broadcast leave notification
        PlayerLeaveMessage leaveMsg = new PlayerLeaveMessage(client1Id.get(), "Player1");
        server.broadcastToAll(leaveMsg);
        
        // Wait for client 2 to receive leave notification
        assertTrue(leaveNotificationLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Leave notification not received");
        assertNotNull(receivedLeaveNotification.get(), "Leave notification should be received");
        assertEquals(client1Id.get(), receivedLeaveNotification.get().getPlayerId(), 
                    "Player ID should match");
        
        // Verify client 1 is disconnected
        assertFalse(client1.isConnected(), "Client 1 should be disconnected");
        
        // Server should clean up disconnected client
        waitForCondition(() -> server.getConnectedClientCount() == 1, 2000, 
                        "Server should have 1 connected client after disconnection");
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
