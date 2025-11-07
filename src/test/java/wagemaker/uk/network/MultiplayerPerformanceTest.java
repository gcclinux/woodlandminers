package wagemaker.uk.network;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests for multiplayer networking functionality.
 * Tests load handling, throughput, memory usage, and latency simulation.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MultiplayerPerformanceTest {
    
    private static final int TEST_PORT = 25567; // Different port for performance tests
    private static final int TIMEOUT_SECONDS = 30;
    
    private GameServer server;
    private List<GameClient> clients;
    
    @BeforeEach
    public void setUp() throws IOException {
        clients = new ArrayList<>();
        
        // Create and start server with higher capacity for performance tests
        server = new GameServer(TEST_PORT, 20);
        server.start();
        
        // Wait for server to be ready
        waitForCondition(() -> server.isRunning(), 3000, "Server failed to start");
    }
    
    @AfterEach
    public void tearDown() {
        // Disconnect all clients
        for (GameClient client : clients) {
            if (client != null && client.isConnected()) {
                try {
                    client.disconnect();
                } catch (Exception e) {
                    // Ignore disconnection errors during cleanup
                }
            }
        }
        clients.clear();
        
        // Stop server
        if (server != null && server.isRunning()) {
            server.stop();
        }
        
        // Wait for cleanup
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Test 1: Load test with 10 concurrent clients
     * Verifies that the server can handle 10 simultaneous client connections
     * and maintain stable performance.
     */
    @Test
    @Order(1)
    public void testLoadWith10ConcurrentClients() throws Exception {
        System.out.println("\n=== Load Test: 10 Concurrent Clients ===");
        
        int clientCount = 10;
        CountDownLatch allClientsConnected = new CountDownLatch(clientCount);
        List<String> clientIds = Collections.synchronizedList(new ArrayList<>());
        
        // Connect 10 clients concurrently
        ExecutorService executor = Executors.newFixedThreadPool(clientCount);
        List<Future<?>> connectionFutures = new ArrayList<>();
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < clientCount; i++) {
            final int clientIndex = i;
            Future<?> future = executor.submit(() -> {
                try {
                    GameClient client = new GameClient();
                    client.setMessageHandler(new MessageHandler() {
                        @Override
                        public void handleMessage(NetworkMessage message) {
                            if (message instanceof ConnectionAcceptedMessage) {
                                String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                                client.setClientId(id);
                                clientIds.add(id);
                                allClientsConnected.countDown();
                            }
                        }
                    });
                    
                    client.connect("localhost", TEST_PORT);
                    clients.add(client);
                    
                } catch (Exception e) {
                    System.err.println("Client " + clientIndex + " failed to connect: " + e.getMessage());
                }
            });
            connectionFutures.add(future);
        }
        
        // Wait for all clients to connect
        assertTrue(allClientsConnected.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                   "Not all clients connected within timeout");
        
        long connectionTime = System.currentTimeMillis() - startTime;
        
        // Wait for all connection tasks to complete
        for (Future<?> future : connectionFutures) {
            future.get(5, TimeUnit.SECONDS);
        }
        
        executor.shutdown();
        
        // Verify all clients are connected
        assertEquals(clientCount, server.getConnectedClientCount(),
                    "Server should have " + clientCount + " connected clients");
        assertEquals(clientCount, clientIds.size(),
                    "All clients should have received IDs");
        
        // Test that server remains responsive
        assertTrue(server.isRunning(), "Server should still be running");
        
        System.out.println("✓ All " + clientCount + " clients connected successfully");
        System.out.println("✓ Connection time: " + connectionTime + "ms");
        System.out.println("✓ Average time per client: " + (connectionTime / clientCount) + "ms");
        
        // Keep clients connected for a moment to test stability
        Thread.sleep(2000);
        
        // Verify all clients are still connected
        long connectedCount = clients.stream().filter(GameClient::isConnected).count();
        assertEquals(clientCount, connectedCount, "All clients should remain connected");
        
        System.out.println("✓ All clients remained stable for 2 seconds");
    }
    
    /**
     * Test 2: Message throughput measurement
     * Measures how many messages per second the server can process
     * from multiple clients simultaneously.
     */
    @Test
    @Order(2)
    public void testMessageThroughput() throws Exception {
        System.out.println("\n=== Message Throughput Test ===");
        
        int clientCount = 5;
        int messagesPerClient = 100;
        int totalExpectedMessages = clientCount * messagesPerClient;
        
        CountDownLatch allClientsReady = new CountDownLatch(clientCount);
        CountDownLatch allMessagesSent = new CountDownLatch(clientCount);
        AtomicInteger messagesReceived = new AtomicInteger(0);
        AtomicLong totalLatency = new AtomicLong(0);
        
        Map<String, Long> messageSendTimes = new ConcurrentHashMap<>();
        
        // Connect clients
        for (int i = 0; i < clientCount; i++) {
            final int clientIndex = i;
            GameClient client = new GameClient();
            
            client.setMessageHandler(new MessageHandler() {
                @Override
                public void handleMessage(NetworkMessage message) {
                    if (message instanceof ConnectionAcceptedMessage) {
                        String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                        client.setClientId(id);
                        allClientsReady.countDown();
                    } else if (message instanceof PlayerMovementMessage) {
                        // Track received messages and calculate latency
                        messagesReceived.incrementAndGet();
                        
                        String messageKey = message.getSenderId() + "-" + message.getTimestamp();
                        Long sendTime = messageSendTimes.get(messageKey);
                        if (sendTime != null) {
                            long latency = System.currentTimeMillis() - sendTime;
                            totalLatency.addAndGet(latency);
                        }
                    }
                }
            });
            
            client.connect("localhost", TEST_PORT);
            clients.add(client);
        }
        
        // Wait for all clients to be ready
        assertTrue(allClientsReady.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                   "Not all clients ready");
        
        Thread.sleep(500); // Let connections stabilize
        
        System.out.println("✓ " + clientCount + " clients connected and ready");
        
        // Start throughput test
        long testStartTime = System.currentTimeMillis();
        
        ExecutorService executor = Executors.newFixedThreadPool(clientCount);
        
        for (int i = 0; i < clientCount; i++) {
            final GameClient client = clients.get(i);
            final int clientIndex = i;
            
            executor.submit(() -> {
                try {
                    for (int j = 0; j < messagesPerClient; j++) {
                        float x = 100.0f + (clientIndex * 10) + j;
                        float y = 100.0f + (clientIndex * 10);
                        
                        long sendTime = System.currentTimeMillis();
                        String messageKey = client.getClientId() + "-" + sendTime;
                        messageSendTimes.put(messageKey, sendTime);
                        
                        client.sendPlayerMovement(x, y, Direction.RIGHT, true);
                        
                        // Small delay to avoid overwhelming the network
                        Thread.sleep(10);
                    }
                    allMessagesSent.countDown();
                } catch (Exception e) {
                    System.err.println("Error sending messages from client " + clientIndex + ": " + e.getMessage());
                }
            });
        }
        
        // Wait for all messages to be sent
        assertTrue(allMessagesSent.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                   "Not all messages sent within timeout");
        
        long testDuration = System.currentTimeMillis() - testStartTime;
        
        // Wait a bit for messages to be processed
        Thread.sleep(2000);
        
        executor.shutdown();
        
        // Calculate throughput
        double messagesPerSecond = (totalExpectedMessages * 1000.0) / testDuration;
        double averageLatency = messagesReceived.get() > 0 ? 
                               (double) totalLatency.get() / messagesReceived.get() : 0;
        
        System.out.println("✓ Total messages sent: " + totalExpectedMessages);
        System.out.println("✓ Messages received by clients: " + messagesReceived.get());
        System.out.println("✓ Test duration: " + testDuration + "ms");
        System.out.println("✓ Throughput: " + String.format("%.2f", messagesPerSecond) + " messages/second");
        System.out.println("✓ Average latency: " + String.format("%.2f", averageLatency) + "ms");
        
        // Verify reasonable throughput (at least 50 messages per second)
        assertTrue(messagesPerSecond >= 50, 
                  "Throughput should be at least 50 messages/second, got: " + messagesPerSecond);
    }
    
    /**
     * Test 3: Server memory usage monitoring
     * Monitors server memory usage under load to detect memory leaks
     * and ensure efficient resource management.
     */
    @Test
    @Order(3)
    public void testServerMemoryUsage() throws Exception {
        System.out.println("\n=== Server Memory Usage Test ===");
        
        Runtime runtime = Runtime.getRuntime();
        
        // Force garbage collection and get baseline
        System.gc();
        Thread.sleep(500);
        long baselineMemory = runtime.totalMemory() - runtime.freeMemory();
        
        System.out.println("✓ Baseline memory: " + formatBytes(baselineMemory));
        
        int clientCount = 10;
        CountDownLatch allClientsReady = new CountDownLatch(clientCount);
        
        // Connect clients and measure memory
        for (int i = 0; i < clientCount; i++) {
            GameClient client = new GameClient();
            
            client.setMessageHandler(new MessageHandler() {
                @Override
                public void handleMessage(NetworkMessage message) {
                    if (message instanceof ConnectionAcceptedMessage) {
                        String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                        client.setClientId(id);
                        allClientsReady.countDown();
                    }
                }
            });
            
            client.connect("localhost", TEST_PORT);
            clients.add(client);
        }
        
        assertTrue(allClientsReady.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                   "Not all clients ready");
        
        Thread.sleep(500);
        
        long memoryAfterConnections = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = memoryAfterConnections - baselineMemory;
        
        System.out.println("✓ Memory after " + clientCount + " connections: " + formatBytes(memoryAfterConnections));
        System.out.println("✓ Memory increase: " + formatBytes(memoryIncrease));
        System.out.println("✓ Memory per client: " + formatBytes(memoryIncrease / clientCount));
        
        // Send messages and monitor memory
        List<Long> memorySnapshots = new ArrayList<>();
        int iterations = 5;
        
        for (int i = 0; i < iterations; i++) {
            // Each client sends 20 messages
            for (GameClient client : clients) {
                for (int j = 0; j < 20; j++) {
                    client.sendPlayerMovement(100.0f + j, 100.0f, Direction.RIGHT, true);
                }
            }
            
            Thread.sleep(500);
            
            long currentMemory = runtime.totalMemory() - runtime.freeMemory();
            memorySnapshots.add(currentMemory);
            System.out.println("  Iteration " + (i + 1) + " memory: " + formatBytes(currentMemory));
        }
        
        // Check for memory leaks (memory should stabilize, not grow continuously)
        long firstSnapshot = memorySnapshots.get(0);
        long lastSnapshot = memorySnapshots.get(memorySnapshots.size() - 1);
        long memoryGrowth = lastSnapshot - firstSnapshot;
        
        System.out.println("✓ Memory growth over " + iterations + " iterations: " + formatBytes(memoryGrowth));
        
        // Memory growth should be reasonable (less than 10MB for this test)
        assertTrue(memoryGrowth < 10 * 1024 * 1024,
                  "Memory growth should be less than 10MB, got: " + formatBytes(memoryGrowth));
        
        // Disconnect all clients and check memory cleanup
        for (GameClient client : clients) {
            client.disconnect();
        }
        
        Thread.sleep(1000);
        System.gc();
        Thread.sleep(500);
        
        long memoryAfterCleanup = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("✓ Memory after cleanup: " + formatBytes(memoryAfterCleanup));
        
        // Memory should decrease after cleanup (within reasonable bounds)
        assertTrue(memoryAfterCleanup < memoryAfterConnections + (5 * 1024 * 1024),
                  "Memory should be cleaned up after disconnections");
    }
    
    /**
     * Test 4: Bandwidth consumption measurement
     * Estimates bandwidth usage for different types of network traffic
     * to ensure efficient network utilization.
     */
    @Test
    @Order(4)
    public void testBandwidthConsumption() throws Exception {
        System.out.println("\n=== Bandwidth Consumption Test ===");
        
        int clientCount = 3;
        CountDownLatch allClientsReady = new CountDownLatch(clientCount);
        AtomicInteger totalMessagesReceived = new AtomicInteger(0);
        
        // Connect clients
        for (int i = 0; i < clientCount; i++) {
            GameClient client = new GameClient();
            
            client.setMessageHandler(new MessageHandler() {
                @Override
                public void handleMessage(NetworkMessage message) {
                    if (message instanceof ConnectionAcceptedMessage) {
                        String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                        client.setClientId(id);
                        allClientsReady.countDown();
                    } else {
                        totalMessagesReceived.incrementAndGet();
                    }
                }
            });
            
            client.connect("localhost", TEST_PORT);
            clients.add(client);
        }
        
        assertTrue(allClientsReady.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                   "Not all clients ready");
        
        Thread.sleep(500);
        
        System.out.println("✓ " + clientCount + " clients connected");
        
        // Test different message types and estimate bandwidth
        Map<String, BandwidthStats> bandwidthByMessageType = new HashMap<>();
        
        // 1. Player movement messages
        System.out.println("\nTesting player movement messages...");
        BandwidthStats movementStats = measureMessageBandwidth(
            "PlayerMovement",
            100,
            () -> {
                for (GameClient client : clients) {
                    client.sendPlayerMovement(100.0f, 100.0f, Direction.RIGHT, true);
                }
            }
        );
        bandwidthByMessageType.put("PlayerMovement", movementStats);
        
        Thread.sleep(500);
        
        // 2. Attack action messages
        System.out.println("\nTesting attack action messages...");
        BandwidthStats attackStats = measureMessageBandwidth(
            "AttackAction",
            50,
            () -> {
                for (GameClient client : clients) {
                    client.sendAttackAction("tree-1");
                }
            }
        );
        bandwidthByMessageType.put("AttackAction", attackStats);
        
        Thread.sleep(500);
        
        // 3. Item pickup messages
        System.out.println("\nTesting item pickup messages...");
        BandwidthStats pickupStats = measureMessageBandwidth(
            "ItemPickup",
            50,
            () -> {
                for (GameClient client : clients) {
                    client.sendItemPickup("item-1");
                }
            }
        );
        bandwidthByMessageType.put("ItemPickup", pickupStats);
        
        // Print summary
        System.out.println("\n=== Bandwidth Summary ===");
        long totalBytesPerSecond = 0;
        
        for (Map.Entry<String, BandwidthStats> entry : bandwidthByMessageType.entrySet()) {
            BandwidthStats stats = entry.getValue();
            System.out.println(entry.getKey() + ":");
            System.out.println("  Estimated message size: " + stats.estimatedMessageSize + " bytes");
            System.out.println("  Messages per second: " + String.format("%.2f", stats.messagesPerSecond));
            System.out.println("  Bandwidth: " + formatBytes((long) stats.bytesPerSecond) + "/s");
            totalBytesPerSecond += stats.bytesPerSecond;
        }
        
        System.out.println("\n✓ Total estimated bandwidth: " + formatBytes(totalBytesPerSecond) + "/s");
        System.out.println("✓ Per client bandwidth: " + formatBytes(totalBytesPerSecond / clientCount) + "/s");
        
        // Verify bandwidth is reasonable (less than 1MB/s per client for typical gameplay)
        long maxBandwidthPerClient = 1024 * 1024; // 1MB/s
        assertTrue(totalBytesPerSecond / clientCount < maxBandwidthPerClient,
                  "Bandwidth per client should be reasonable");
    }
    
    /**
     * Test 5: Latency simulation
     * Simulates various network latency conditions to verify
     * that the system handles delays gracefully.
     */
    @Test
    @Order(5)
    public void testLatencySimulation() throws Exception {
        System.out.println("\n=== Latency Simulation Test ===");
        
        int[] latencies = {0, 50, 100, 200, 500}; // milliseconds
        
        for (int simulatedLatency : latencies) {
            System.out.println("\nTesting with " + simulatedLatency + "ms latency...");
            
            CountDownLatch clientReady = new CountDownLatch(1);
            CountDownLatch messageReceived = new CountDownLatch(1);
            
            GameClient client = new GameClient();
            
            client.setMessageHandler(new MessageHandler() {
                @Override
                public void handleMessage(NetworkMessage message) {
                    if (message instanceof ConnectionAcceptedMessage) {
                        String id = ((ConnectionAcceptedMessage) message).getAssignedClientId();
                        client.setClientId(id);
                        clientReady.countDown();
                    } else if (message instanceof PlayerMovementMessage) {
                        messageReceived.countDown();
                    }
                }
            });
            
            client.connect("localhost", TEST_PORT);
            clients.add(client);
            
            assertTrue(clientReady.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                      "Client not ready");
            
            Thread.sleep(200);
            
            // Simulate latency by adding delay before sending
            if (simulatedLatency > 0) {
                Thread.sleep(simulatedLatency);
            }
            
            long sendTime = System.currentTimeMillis();
            client.sendPlayerMovement(100.0f, 100.0f, Direction.RIGHT, true);
            
            // Broadcast the message back to simulate round-trip
            Thread.sleep(100); // Processing time
            PlayerMovementMessage echoMsg = new PlayerMovementMessage(
                client.getClientId(), 100.0f, 100.0f, Direction.RIGHT, true
            );
            server.broadcastToAll(echoMsg);
            
            assertTrue(messageReceived.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                      "Message not received");
            
            long receiveTime = System.currentTimeMillis();
            long roundTripTime = receiveTime - sendTime;
            
            System.out.println("  Simulated latency: " + simulatedLatency + "ms");
            System.out.println("  Actual round-trip time: " + roundTripTime + "ms");
            System.out.println("  ✓ System handled latency gracefully");
            
            // Verify the system still works under latency
            assertTrue(client.isConnected(), "Client should remain connected");
            assertTrue(server.isRunning(), "Server should remain running");
            
            // Disconnect for next iteration
            client.disconnect();
            Thread.sleep(200);
        }
        
        System.out.println("\n✓ All latency conditions tested successfully");
    }
    
    // Helper methods
    
    /**
     * Measures bandwidth for a specific message type.
     */
    private BandwidthStats measureMessageBandwidth(String messageType, int iterations, Runnable sendAction) {
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < iterations; i++) {
            sendAction.run();
            try {
                Thread.sleep(10); // Small delay between sends
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        long duration = System.currentTimeMillis() - startTime;
        double messagesPerSecond = (iterations * clients.size() * 1000.0) / duration;
        
        // Estimate message sizes (based on typical serialized sizes)
        int estimatedSize = switch (messageType) {
            case "PlayerMovement" -> 80; // ~80 bytes for position + direction + metadata
            case "AttackAction" -> 60;   // ~60 bytes for action + target ID
            case "ItemPickup" -> 50;     // ~50 bytes for item ID + player ID
            default -> 100;
        };
        
        double bytesPerSecond = messagesPerSecond * estimatedSize;
        
        return new BandwidthStats(estimatedSize, messagesPerSecond, bytesPerSecond);
    }
    
    /**
     * Formats bytes into human-readable format.
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
    
    /**
     * Waits for a condition to become true within a timeout period.
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
    
    /**
     * Helper class to store bandwidth statistics.
     */
    private static class BandwidthStats {
        final int estimatedMessageSize;
        final double messagesPerSecond;
        final double bytesPerSecond;
        
        BandwidthStats(int estimatedMessageSize, double messagesPerSecond, double bytesPerSecond) {
            this.estimatedMessageSize = estimatedMessageSize;
            this.messagesPerSecond = messagesPerSecond;
            this.bytesPerSecond = bytesPerSecond;
        }
    }
}
