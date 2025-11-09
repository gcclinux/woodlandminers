package wagemaker.uk.network;

import org.junit.jupiter.api.*;

import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for TreeRemovalMessage network message handling.
 * Tests serialization, deserialization, and end-to-end message routing.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TreeRemovalMessageTest {
    
    private static final int TEST_PORT = 25567; // Use different port to avoid conflicts
    private static final int TIMEOUT_SECONDS = 10;
    
    private GameServer server;
    private GameClient client;
    
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
        // Disconnect client
        if (client != null && client.isConnected()) {
            client.disconnect();
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
     * Test 1: TreeRemovalMessage serialization and deserialization
     * Verifies that the message can be properly serialized and deserialized
     */
    @Test
    @Order(1)
    public void testTreeRemovalMessageSerialization() throws Exception {
        String senderId = "test-server";
        String treeId = "100,200";
        String reason = "Tree does not exist in server world state";
        
        // Create message
        TreeRemovalMessage originalMessage = new TreeRemovalMessage(senderId, treeId, reason);
        
        // Serialize to byte array
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
        objectOut.writeObject(originalMessage);
        objectOut.flush();
        
        byte[] serializedData = byteOut.toByteArray();
        
        // Verify data was written
        assertTrue(serializedData.length > 0, "Serialized data should not be empty");
        
        // Deserialize from byte array
        ByteArrayInputStream byteIn = new ByteArrayInputStream(serializedData);
        ObjectInputStream objectIn = new ObjectInputStream(byteIn);
        Object deserializedObject = objectIn.readObject();
        
        // Verify deserialization
        assertNotNull(deserializedObject, "Deserialized object should not be null");
        assertTrue(deserializedObject instanceof TreeRemovalMessage, 
                  "Deserialized object should be TreeRemovalMessage");
        
        TreeRemovalMessage deserializedMessage = (TreeRemovalMessage) deserializedObject;
        
        // Verify message content
        assertEquals(MessageType.TREE_REMOVAL, deserializedMessage.getType(), 
                    "Message type should be TREE_REMOVAL");
        assertEquals(senderId, deserializedMessage.getSenderId(), 
                    "Sender ID should match");
        assertEquals(treeId, deserializedMessage.getTreeId(), 
                    "Tree ID should match");
        assertEquals(reason, deserializedMessage.getReason(), 
                    "Reason should match");
        
        // Clean up
        objectOut.close();
        objectIn.close();
    }
    
    /**
     * Test 2: TreeRemovalMessage routing through DefaultMessageHandler
     * Verifies that the message is properly routed to the handleTreeRemoval method
     */
    @Test
    @Order(2)
    public void testTreeRemovalMessageRouting() throws Exception {
        CountDownLatch handlerLatch = new CountDownLatch(1);
        AtomicBoolean handlerCalled = new AtomicBoolean(false);
        AtomicReference<TreeRemovalMessage> receivedMessage = new AtomicReference<>();
        
        // Create custom message handler
        DefaultMessageHandler handler = new DefaultMessageHandler() {
            @Override
            protected void handleTreeRemoval(TreeRemovalMessage message) {
                handlerCalled.set(true);
                receivedMessage.set(message);
                handlerLatch.countDown();
            }
        };
        
        // Create test message
        String treeId = "256,512";
        String reason = "Ghost tree detected";
        TreeRemovalMessage testMessage = new TreeRemovalMessage("server", treeId, reason);
        
        // Handle the message
        handler.handleMessage(testMessage);
        
        // Wait for handler to be called
        assertTrue(handlerLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                  "Handler should be called within timeout");
        assertTrue(handlerCalled.get(), "handleTreeRemoval should be called");
        assertNotNull(receivedMessage.get(), "Message should be received");
        assertEquals(treeId, receivedMessage.get().getTreeId(), "Tree ID should match");
        assertEquals(reason, receivedMessage.get().getReason(), "Reason should match");
    }
    
    /**
     * Test 3: End-to-end TreeRemovalMessage flow
     * Verifies that clients receive and process TreeRemovalMessage from server
     */
    @Test
    @Order(3)
    public void testTreeRemovalMessageEndToEnd() throws Exception {
        CountDownLatch connectionLatch = new CountDownLatch(1);
        CountDownLatch removalLatch = new CountDownLatch(1);
        AtomicReference<String> clientId = new AtomicReference<>();
        AtomicReference<TreeRemovalMessage> receivedRemoval = new AtomicReference<>();
        
        // Create client with message handler
        client = new GameClient();
        client.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                    client.setClientId(id);
                    clientId.set(id);
                    connectionLatch.countDown();
                } else if (message instanceof TreeRemovalMessage) {
                    receivedRemoval.set((TreeRemovalMessage) message);
                    removalLatch.countDown();
                }
            }
        });
        
        // Connect to server
        client.connect("localhost", TEST_PORT);
        
        // Wait for connection
        assertTrue(connectionLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                  "Client should connect within timeout");
        assertNotNull(clientId.get(), "Client ID should be assigned");
        
        // Wait for connection to stabilize
        Thread.sleep(200);
        
        // Server sends TreeRemovalMessage to client
        String treeId = "-512,576";
        String reason = "Tree does not exist in server world state";
        TreeRemovalMessage removalMessage = new TreeRemovalMessage("server", treeId, reason);
        server.broadcastToAll(removalMessage);
        
        // Wait for client to receive the message
        assertTrue(removalLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                  "TreeRemovalMessage should be received within timeout");
        assertNotNull(receivedRemoval.get(), "Removal message should be received");
        assertEquals(treeId, receivedRemoval.get().getTreeId(), 
                    "Tree ID should match");
        assertEquals(reason, receivedRemoval.get().getReason(), 
                    "Reason should match");
        assertEquals(MessageType.TREE_REMOVAL, receivedRemoval.get().getType(), 
                    "Message type should be TREE_REMOVAL");
    }
    
    /**
     * Test 4: Multiple TreeRemovalMessages
     * Verifies that multiple removal messages can be sent and received correctly
     */
    @Test
    @Order(4)
    public void testMultipleTreeRemovalMessages() throws Exception {
        CountDownLatch connectionLatch = new CountDownLatch(1);
        CountDownLatch removalLatch = new CountDownLatch(3); // Expect 3 messages
        AtomicReference<String> clientId = new AtomicReference<>();
        AtomicReference<Integer> messageCount = new AtomicReference<>(0);
        
        // Create client with message handler
        client = new GameClient();
        client.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                    client.setClientId(id);
                    clientId.set(id);
                    connectionLatch.countDown();
                } else if (message instanceof TreeRemovalMessage) {
                    messageCount.updateAndGet(v -> v + 1);
                    removalLatch.countDown();
                }
            }
        });
        
        // Connect to server
        client.connect("localhost", TEST_PORT);
        
        // Wait for connection
        assertTrue(connectionLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                  "Client should connect within timeout");
        
        Thread.sleep(200);
        
        // Server sends multiple TreeRemovalMessages
        server.broadcastToAll(new TreeRemovalMessage("server", "100,100", "Ghost tree 1"));
        Thread.sleep(50);
        server.broadcastToAll(new TreeRemovalMessage("server", "200,200", "Ghost tree 2"));
        Thread.sleep(50);
        server.broadcastToAll(new TreeRemovalMessage("server", "300,300", "Ghost tree 3"));
        
        // Wait for all messages to be received
        assertTrue(removalLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                  "All TreeRemovalMessages should be received within timeout");
        assertEquals(3, messageCount.get().intValue(), 
                    "Should receive exactly 3 removal messages");
    }
    
    /**
     * Test 5: TreeRemovalMessage with invalid data
     * Verifies that the system handles messages with null or empty fields gracefully
     */
    @Test
    @Order(5)
    public void testTreeRemovalMessageWithInvalidData() throws Exception {
        CountDownLatch connectionLatch = new CountDownLatch(1);
        CountDownLatch removalLatch = new CountDownLatch(1);
        AtomicReference<TreeRemovalMessage> receivedRemoval = new AtomicReference<>();
        
        // Create client with message handler
        client = new GameClient();
        client.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                    client.setClientId(id);
                    connectionLatch.countDown();
                } else if (message instanceof TreeRemovalMessage) {
                    receivedRemoval.set((TreeRemovalMessage) message);
                    removalLatch.countDown();
                }
            }
        });
        
        // Connect to server
        client.connect("localhost", TEST_PORT);
        
        // Wait for connection
        assertTrue(connectionLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                  "Client should connect within timeout");
        
        Thread.sleep(200);
        
        // Server sends TreeRemovalMessage with null reason (should still work)
        String treeId = "400,400";
        TreeRemovalMessage removalMessage = new TreeRemovalMessage("server", treeId, null);
        server.broadcastToAll(removalMessage);
        
        // Wait for message
        assertTrue(removalLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                  "Message should be received even with null reason");
        assertNotNull(receivedRemoval.get(), "Message should be received");
        assertEquals(treeId, receivedRemoval.get().getTreeId(), "Tree ID should match");
        assertNull(receivedRemoval.get().getReason(), "Reason should be null");
    }
    
    /**
     * Test 6: TreeRemovalMessage broadcast to multiple clients
     * Verifies that removal messages are properly broadcast to all connected clients
     */
    @Test
    @Order(6)
    public void testTreeRemovalMessageBroadcast() throws Exception {
        CountDownLatch client1ConnectionLatch = new CountDownLatch(1);
        CountDownLatch client2ConnectionLatch = new CountDownLatch(1);
        CountDownLatch client1RemovalLatch = new CountDownLatch(1);
        CountDownLatch client2RemovalLatch = new CountDownLatch(1);
        
        AtomicReference<TreeRemovalMessage> client1Received = new AtomicReference<>();
        AtomicReference<TreeRemovalMessage> client2Received = new AtomicReference<>();
        
        // Create first client
        client = new GameClient();
        client.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                    client.setClientId(id);
                    client1ConnectionLatch.countDown();
                } else if (message instanceof TreeRemovalMessage) {
                    client1Received.set((TreeRemovalMessage) message);
                    client1RemovalLatch.countDown();
                }
            }
        });
        
        // Create second client
        GameClient client2 = new GameClient();
        client2.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                    client2.setClientId(id);
                    client2ConnectionLatch.countDown();
                } else if (message instanceof TreeRemovalMessage) {
                    client2Received.set((TreeRemovalMessage) message);
                    client2RemovalLatch.countDown();
                }
            }
        });
        
        try {
            // Connect both clients
            client.connect("localhost", TEST_PORT);
            client2.connect("localhost", TEST_PORT);
            
            // Wait for both connections
            assertTrue(client1ConnectionLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                      "Client 1 should connect");
            assertTrue(client2ConnectionLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                      "Client 2 should connect");
            
            Thread.sleep(200);
            
            // Verify both clients are connected
            assertEquals(2, server.getConnectedClientCount(), 
                        "Server should have 2 connected clients");
            
            // Server broadcasts TreeRemovalMessage to all clients
            String treeId = "500,500";
            String reason = "Broadcast ghost tree removal";
            TreeRemovalMessage removalMessage = new TreeRemovalMessage("server", treeId, reason);
            server.broadcastToAll(removalMessage);
            
            // Wait for both clients to receive the message
            assertTrue(client1RemovalLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                      "Client 1 should receive removal message");
            assertTrue(client2RemovalLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                      "Client 2 should receive removal message");
            
            // Verify both clients received the same message
            assertNotNull(client1Received.get(), "Client 1 should receive message");
            assertNotNull(client2Received.get(), "Client 2 should receive message");
            assertEquals(treeId, client1Received.get().getTreeId(), 
                        "Client 1 tree ID should match");
            assertEquals(treeId, client2Received.get().getTreeId(), 
                        "Client 2 tree ID should match");
            assertEquals(reason, client1Received.get().getReason(), 
                        "Client 1 reason should match");
            assertEquals(reason, client2Received.get().getReason(), 
                        "Client 2 reason should match");
            
        } finally {
            // Clean up second client
            if (client2.isConnected()) {
                client2.disconnect();
            }
        }
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
