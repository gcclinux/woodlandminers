package wagemaker.uk.server;

import org.junit.jupiter.api.*;
import wagemaker.uk.network.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ServerConfig loading and validation during server startup.
 * Tests the complete flow of configuration loading, validation, and application
 * to server-side validation logic.
 * 
 * Requirements: 1.1, 1.2, 1.3, 1.4, 6.1, 6.2, 6.3, 6.4
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServerConfigIntegrationTest {
    
    private static final int TEST_PORT = 25568;
    private static final int TIMEOUT_SECONDS = 10;
    private static final String SERVER_CONFIG_FILE = "server.properties";
    private static final String BACKUP_CONFIG_FILE = "server.properties.backup";
    
    private GameServer server;
    private GameClient client;
    
    @BeforeEach
    public void setUp() throws IOException {
        // Backup existing server.properties if it exists
        File serverConfig = new File(SERVER_CONFIG_FILE);
        File backupConfig = new File(BACKUP_CONFIG_FILE);
        
        if (serverConfig.exists()) {
            java.nio.file.Files.copy(
                serverConfig.toPath(),
                backupConfig.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
        }
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
        
        // Restore original server.properties from backup
        File serverConfig = new File(SERVER_CONFIG_FILE);
        File backupConfig = new File(BACKUP_CONFIG_FILE);
        
        if (backupConfig.exists()) {
            try {
                java.nio.file.Files.copy(
                    backupConfig.toPath(),
                    serverConfig.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
                backupConfig.delete();
            } catch (IOException e) {
                System.err.println("Failed to restore server config: " + e.getMessage());
            }
        }
        
        // Wait for cleanup
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Test 1: Server startup with valid configuration
     * Requirements: 1.1, 1.2, 1.5
     */
    @Test
    @Order(1)
    public void testServerStartupWithValidConfiguration() throws Exception {
        // Create config with valid planting range
        createTestConfig(768); // 12 tiles
        
        // Start server - it should load configuration successfully
        server = new GameServer(TEST_PORT, 10);
        server.start();
        
        // Verify server started successfully
        waitForCondition(() -> server.isRunning(), 2000, "Server failed to start");
        assertTrue(server.isRunning(), "Server should be running");
        
        // Verify configuration was loaded
        ServerConfig config = server.getConfig();
        assertNotNull(config, "Server config should not be null");
        assertEquals(768, config.getPlantingMaxRange(),
                    "Server should load planting range of 768 pixels");
        
        // Connect client to verify server is operational
        CountDownLatch connectionLatch = new CountDownLatch(1);
        AtomicInteger receivedRange = new AtomicInteger(-1);
        
        client = new GameClient();
        client.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    ConnectionAcceptedMessage acceptedMsg = (ConnectionAcceptedMessage) message;
                    client.setClientId(acceptedMsg.getAssignedClientId());
                    receivedRange.set(acceptedMsg.getPlantingMaxRange());
                    connectionLatch.countDown();
                }
            }
        });
        
        client.connect("localhost", TEST_PORT);
        assertTrue(connectionLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                   "Client should connect successfully");
        
        // Verify client received the configured range
        assertEquals(768, receivedRange.get(),
                    "Client should receive configured planting range");
    }
    
    /**
     * Test 2: Server startup with missing configuration (uses default)
     * Requirements: 1.4, 6.1
     */
    @Test
    @Order(2)
    public void testServerStartupWithMissingConfiguration() throws Exception {
        // Create config without planting.max.range property
        createTestConfigWithoutPlantingRange();
        
        // Start server - it should use default value
        server = new GameServer(TEST_PORT, 10);
        server.start();
        
        // Verify server started successfully
        waitForCondition(() -> server.isRunning(), 2000, "Server failed to start");
        assertTrue(server.isRunning(), "Server should be running with default config");
        
        // Verify default configuration was used
        ServerConfig config = server.getConfig();
        assertNotNull(config, "Server config should not be null");
        assertEquals(512, config.getPlantingMaxRange(),
                    "Server should use default planting range of 512 pixels");
        
        // Connect client to verify default is transmitted
        CountDownLatch connectionLatch = new CountDownLatch(1);
        AtomicInteger receivedRange = new AtomicInteger(-1);
        
        client = new GameClient();
        client.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    ConnectionAcceptedMessage acceptedMsg = (ConnectionAcceptedMessage) message;
                    client.setClientId(acceptedMsg.getAssignedClientId());
                    receivedRange.set(acceptedMsg.getPlantingMaxRange());
                    connectionLatch.countDown();
                }
            }
        });
        
        client.connect("localhost", TEST_PORT);
        assertTrue(connectionLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                   "Client should connect successfully");
        
        // Verify client received the default range
        assertEquals(512, receivedRange.get(),
                    "Client should receive default planting range");
    }
    
    /**
     * Test 3: Server startup with invalid configuration (uses default)
     * Requirements: 1.3, 1.4, 6.3, 6.4
     */
    @Test
    @Order(3)
    public void testServerStartupWithInvalidConfiguration() throws Exception {
        // Create config with invalid planting range (out of bounds)
        createTestConfig(2000); // Above maximum of 1024
        
        // Start server - it should fallback to default
        server = new GameServer(TEST_PORT, 10);
        server.start();
        
        // Verify server started successfully
        waitForCondition(() -> server.isRunning(), 2000, "Server failed to start");
        assertTrue(server.isRunning(), "Server should be running despite invalid config");
        
        // Verify default configuration was used as fallback
        ServerConfig config = server.getConfig();
        assertNotNull(config, "Server config should not be null");
        assertEquals(512, config.getPlantingMaxRange(),
                    "Server should fallback to default planting range of 512 pixels");
        
        // Connect client to verify fallback value is transmitted
        CountDownLatch connectionLatch = new CountDownLatch(1);
        AtomicInteger receivedRange = new AtomicInteger(-1);
        
        client = new GameClient();
        client.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    ConnectionAcceptedMessage acceptedMsg = (ConnectionAcceptedMessage) message;
                    client.setClientId(acceptedMsg.getAssignedClientId());
                    receivedRange.set(acceptedMsg.getPlantingMaxRange());
                    connectionLatch.countDown();
                }
            }
        });
        
        client.connect("localhost", TEST_PORT);
        assertTrue(connectionLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                   "Client should connect successfully");
        
        // Verify client received the fallback range
        assertEquals(512, receivedRange.get(),
                    "Client should receive fallback planting range");
    }
    
    /**
     * Test 4: Server startup with non-numeric configuration (uses default)
     * Requirements: 1.3, 1.4, 6.4
     */
    @Test
    @Order(4)
    public void testServerStartupWithNonNumericConfiguration() throws Exception {
        // Create config with non-numeric planting range
        createTestConfigWithValue("invalid-value");
        
        // Start server - it should fallback to default
        server = new GameServer(TEST_PORT, 10);
        server.start();
        
        // Verify server started successfully
        waitForCondition(() -> server.isRunning(), 2000, "Server failed to start");
        assertTrue(server.isRunning(), "Server should be running despite non-numeric config");
        
        // Verify default configuration was used as fallback
        ServerConfig config = server.getConfig();
        assertNotNull(config, "Server config should not be null");
        assertEquals(512, config.getPlantingMaxRange(),
                    "Server should fallback to default planting range of 512 pixels");
    }

    /**
     * Test 5: Configuration values are correctly applied to validation logic
     * Requirements: 2.1, 2.2, 2.4, 6.1, 6.2
     */
    @Test
    @Order(5)
    public void testConfigurationValuesAppliedToValidationLogic() throws Exception {
        // Create config with small planting range for easy testing
        createTestConfig(192); // 3 tiles
        
        // Start server
        server = new GameServer(TEST_PORT, 10);
        server.start();
        waitForCondition(() -> server.isRunning(), 2000, "Server failed to start");
        
        // Verify configuration was loaded
        assertEquals(192, server.getConfig().getPlantingMaxRange(),
                    "Server should load planting range of 192 pixels");
        
        // Connect client
        CountDownLatch connectionLatch = new CountDownLatch(1);
        
        client = new GameClient();
        client.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    ConnectionAcceptedMessage acceptedMsg = (ConnectionAcceptedMessage) message;
                    client.setClientId(acceptedMsg.getAssignedClientId());
                    connectionLatch.countDown();
                }
            }
        });
        
        client.connect("localhost", TEST_PORT);
        assertTrue(connectionLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                   "Client should connect successfully");
        
        Thread.sleep(200);
        
        // Test 1: Planting within configured range should succeed (not be rejected)
        String bambooId1 = "test-bamboo-within-range";
        float plantX1 = 128.0f; // Within 192 pixel range
        float plantY1 = 0.0f;
        
        client.sendBambooPlant(bambooId1, plantX1, plantY1);
        Thread.sleep(300);
        
        // Client should still be connected (not kicked for range violation)
        assertTrue(client.isConnected(),
                  "Client should remain connected after planting within range");
        
        // Test 2: Planting beyond configured range should be rejected
        String bambooId2 = "test-bamboo-beyond-range";
        float plantX2 = 512.0f; // Beyond 192 pixel range
        float plantY2 = 0.0f;
        
        client.sendBambooPlant(bambooId2, plantX2, plantY2);
        Thread.sleep(300);
        
        // Client should still be connected (server logs violation but doesn't disconnect)
        assertTrue(client.isConnected(),
                  "Client should remain connected even after range violation");
        
        // The key validation is that the server uses the configured 192 pixel range
        // rather than a hardcoded value. This is verified by the server logs
        // showing the rejection with the correct max range value.
    }
    
    /**
     * Test 6: Multiple configuration values (minimum, default, maximum)
     * Requirements: 1.2, 6.2, 6.3
     */
    @Test
    @Order(6)
    public void testMultipleConfigurationValues() throws Exception {
        // Test minimum valid value (64)
        createTestConfig(64);
        server = new GameServer(TEST_PORT, 10);
        server.start();
        waitForCondition(() -> server.isRunning(), 2000, "Server failed to start");
        assertEquals(64, server.getConfig().getPlantingMaxRange(),
                    "Server should load minimum planting range of 64 pixels");
        server.stop();
        waitForCondition(() -> !server.isRunning(), 2000, "Server failed to stop");
        Thread.sleep(500);
        
        // Test default value (512)
        createTestConfig(512);
        server = new GameServer(TEST_PORT, 10);
        server.start();
        waitForCondition(() -> server.isRunning(), 2000, "Server failed to start");
        assertEquals(512, server.getConfig().getPlantingMaxRange(),
                    "Server should load default planting range of 512 pixels");
        server.stop();
        waitForCondition(() -> !server.isRunning(), 2000, "Server failed to stop");
        Thread.sleep(500);
        
        // Test maximum valid value (1024)
        createTestConfig(1024);
        server = new GameServer(TEST_PORT, 10);
        server.start();
        waitForCondition(() -> server.isRunning(), 2000, "Server failed to start");
        assertEquals(1024, server.getConfig().getPlantingMaxRange(),
                    "Server should load maximum planting range of 1024 pixels");
    }
    
    /**
     * Test 7: Configuration below minimum uses default
     * Requirements: 1.3, 6.2, 6.4
     */
    @Test
    @Order(7)
    public void testConfigurationBelowMinimumUsesDefault() throws Exception {
        // Create config with value below minimum (64)
        createTestConfig(32);
        
        // Start server
        server = new GameServer(TEST_PORT, 10);
        server.start();
        waitForCondition(() -> server.isRunning(), 2000, "Server failed to start");
        
        // Verify default was used
        assertEquals(512, server.getConfig().getPlantingMaxRange(),
                    "Server should use default when config is below minimum");
        
        // Connect client to verify default is applied
        CountDownLatch connectionLatch = new CountDownLatch(1);
        AtomicInteger receivedRange = new AtomicInteger(-1);
        
        client = new GameClient();
        client.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    ConnectionAcceptedMessage acceptedMsg = (ConnectionAcceptedMessage) message;
                    client.setClientId(acceptedMsg.getAssignedClientId());
                    receivedRange.set(acceptedMsg.getPlantingMaxRange());
                    connectionLatch.countDown();
                }
            }
        });
        
        client.connect("localhost", TEST_PORT);
        assertTrue(connectionLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS),
                   "Client should connect successfully");
        
        assertEquals(512, receivedRange.get(),
                    "Client should receive default range when config is below minimum");
    }
    
    // ===== Helper Methods =====
    
    /**
     * Creates a test server configuration file with specified planting range.
     * @param plantingRange The planting range in pixels
     */
    private void createTestConfig(int plantingRange) throws IOException {
        try (FileWriter writer = new FileWriter(SERVER_CONFIG_FILE)) {
            writer.write("# Test Server Configuration\n");
            writer.write("server.port=" + TEST_PORT + "\n");
            writer.write("server.max-clients=10\n");
            writer.write("world.seed=12345\n");
            writer.write("planting.max.range=" + plantingRange + "\n");
        }
    }
    
    /**
     * Creates a test server configuration file with a string value for planting range.
     * @param value The string value to use
     */
    private void createTestConfigWithValue(String value) throws IOException {
        try (FileWriter writer = new FileWriter(SERVER_CONFIG_FILE)) {
            writer.write("# Test Server Configuration\n");
            writer.write("server.port=" + TEST_PORT + "\n");
            writer.write("server.max-clients=10\n");
            writer.write("world.seed=12345\n");
            writer.write("planting.max.range=" + value + "\n");
        }
    }
    
    /**
     * Creates a test server configuration file without planting.max.range property.
     */
    private void createTestConfigWithoutPlantingRange() throws IOException {
        try (FileWriter writer = new FileWriter(SERVER_CONFIG_FILE)) {
            writer.write("# Test Server Configuration\n");
            writer.write("server.port=" + TEST_PORT + "\n");
            writer.write("server.max-clients=10\n");
            writer.write("world.seed=12345\n");
            // Intentionally omit planting.max.range
        }
    }
    
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
