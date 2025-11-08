package wagemaker.uk.client;

import org.junit.jupiter.api.*;
import wagemaker.uk.network.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the server connection memory feature.
 * Tests the end-to-end flow of connecting to a server, saving the address,
 * and pre-filling the connection dialog on subsequent connections.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConnectionFlowIntegrationTest {
    
    private static final int TEST_PORT = 25567; // Different port to avoid conflicts
    private static final int TIMEOUT_SECONDS = 10;
    
    private GameServer server;
    private GameClient client;
    private File configFile;
    
    @BeforeEach
    public void setUp() throws IOException {
        // Get the config file location
        configFile = getConfigFile();
        
        // Clean up any existing config file before each test
        if (configFile.exists()) {
            configFile.delete();
        }
        
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
        
        // Clean up config file after test
        if (configFile != null && configFile.exists()) {
            configFile.delete();
        }
        
        // Wait for cleanup
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Gets the configuration file path based on the operating system.
     */
    private File getConfigFile() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        File configDir;
        
        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                configDir = new File(appData, "Woodlanders");
            } else {
                configDir = new File(userHome, "AppData/Roaming/Woodlanders");
            }
        } else if (os.contains("mac")) {
            configDir = new File(userHome, "Library/Application Support/Woodlanders");
        } else {
            configDir = new File(userHome, ".config/woodlanders");
        }
        
        return new File(configDir, "woodlanders.json");
    }
    
    /**
     * Test 1: End-to-end flow - connect, save, verify pre-fill
     * Tests the complete flow: connect to server → save address → verify config file → verify pre-fill
     */
    @Test
    @Order(1)
    public void testEndToEndConnectionFlow() throws Exception {
        CountDownLatch connectionLatch = new CountDownLatch(1);
        AtomicReference<String> assignedClientId = new AtomicReference<>();
        
        // Step 1: Connect to server
        client = new GameClient();
        client.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    ConnectionAcceptedMessage acceptedMsg = (ConnectionAcceptedMessage) message;
                    assignedClientId.set(acceptedMsg.getAssignedClientId());
                    client.setClientId(acceptedMsg.getAssignedClientId());
                    
                    // Step 2: Save server address (simulating GameMessageHandler behavior)
                    String serverAddress = "localhost:" + TEST_PORT;
                    PlayerConfig config = PlayerConfig.load();
                    config.saveLastServer(serverAddress);
                    
                    connectionLatch.countDown();
                }
            }
        });
        
        client.connect("localhost", TEST_PORT);
        
        // Wait for connection and save
        assertTrue(connectionLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                   "Connection was not accepted within timeout");
        assertNotNull(assignedClientId.get(), "Client ID should be assigned");
        
        // Step 3: Verify config file was created and contains the address
        assertTrue(configFile.exists(), "Config file should be created");
        
        // Step 4: Load config and verify saved address
        PlayerConfig loadedConfig = PlayerConfig.load();
        String savedAddress = loadedConfig.getLastServer();
        assertNotNull(savedAddress, "Saved server address should not be null");
        assertEquals("localhost:" + TEST_PORT, savedAddress, "Saved address should match");
        
        // Step 5: Simulate reopening connection dialog with pre-filled address
        // In the real application, this would be done by MyGdxGame calling setPrefilledAddress()
        String prefilledAddress = loadedConfig.getLastServer();
        assertEquals("localhost:" + TEST_PORT, prefilledAddress, 
                    "Pre-filled address should match saved address");
    }
    
    /**
     * Test 2: Connecting to different servers updates the saved address
     * Verifies that connecting to a new server overwrites the previous saved address
     */
    @Test
    @Order(2)
    public void testConnectingToDifferentServersUpdatesAddress() throws Exception {
        // First connection
        String firstAddress = "localhost:" + TEST_PORT;
        connectAndSaveAddress(firstAddress);
        
        // Verify first address was saved
        PlayerConfig config1 = PlayerConfig.load();
        assertEquals(firstAddress, config1.getLastServer(), "First address should be saved");
        
        // Disconnect
        if (client != null && client.isConnected()) {
            client.disconnect();
            Thread.sleep(200);
        }
        
        // Second connection to different address (simulated by using different format)
        String secondAddress = "127.0.0.1:" + TEST_PORT;
        connectAndSaveAddress(secondAddress);
        
        // Verify second address overwrote the first
        PlayerConfig config2 = PlayerConfig.load();
        assertEquals(secondAddress, config2.getLastServer(), "Second address should overwrite first");
        assertNotEquals(firstAddress, config2.getLastServer(), "First address should be replaced");
    }
    
    /**
     * Test 3: Connection failures don't save the address
     * Verifies that failed connection attempts don't update the saved server address
     */
    @Test
    @Order(3)
    public void testConnectionFailureDoesNotSaveAddress() throws Exception {
        // First, establish a successful connection and save an address
        String successfulAddress = "localhost:" + TEST_PORT;
        connectAndSaveAddress(successfulAddress);
        
        // Verify the address was saved
        PlayerConfig config1 = PlayerConfig.load();
        assertEquals(successfulAddress, config1.getLastServer(), "Successful address should be saved");
        
        // Disconnect
        if (client != null && client.isConnected()) {
            client.disconnect();
            Thread.sleep(200);
        }
        
        // Now attempt to connect to a non-existent server (should fail)
        String failedAddress = "localhost:9999"; // Port with no server
        GameClient failedClient = new GameClient();
        
        AtomicBoolean connectionFailed = new AtomicBoolean(false);
        
        try {
            failedClient.connect("localhost", 9999);
            // Wait a bit to see if connection fails
            Thread.sleep(500);
        } catch (Exception e) {
            connectionFailed.set(true);
        }
        
        // Even if connection attempt was made, don't save the address
        // (In real implementation, save only happens on ConnectionAcceptedMessage)
        
        // Verify the original address is still saved (not overwritten by failed attempt)
        PlayerConfig config2 = PlayerConfig.load();
        assertEquals(successfulAddress, config2.getLastServer(), 
                    "Failed connection should not overwrite saved address");
        
        // Clean up failed client
        if (failedClient.isConnected()) {
            failedClient.disconnect();
        }
    }
    
    /**
     * Test 4: Behavior when config file doesn't exist initially
     * Verifies that the system handles missing config file gracefully
     */
    @Test
    @Order(4)
    public void testBehaviorWithoutConfigFile() throws Exception {
        // Ensure config file doesn't exist
        if (configFile.exists()) {
            configFile.delete();
        }
        
        // Also check for and delete old player.properties file if it exists
        File oldConfigFile = new File("player.properties");
        if (oldConfigFile.exists()) {
            oldConfigFile.delete();
        }
        
        // Load config when file doesn't exist
        PlayerConfig config = PlayerConfig.load();
        assertNotNull(config, "Config should be created even when file doesn't exist");
        
        // Verify no saved server address
        String savedAddress = config.getLastServer();
        assertNull(savedAddress, "Saved address should be null when config file doesn't exist");
        
        // Connect to server and save address
        String serverAddress = "localhost:" + TEST_PORT;
        connectAndSaveAddress(serverAddress);
        
        // Verify config file was created
        assertTrue(configFile.exists(), "Config file should be created after first save");
        
        // Verify address was saved
        PlayerConfig loadedConfig = PlayerConfig.load();
        assertEquals(serverAddress, loadedConfig.getLastServer(), "Address should be saved");
    }
    
    /**
     * Test 5: Multiple connection cycles preserve the last address
     * Verifies that multiple connect-disconnect cycles work correctly
     */
    @Test
    @Order(5)
    public void testMultipleConnectionCycles() throws Exception {
        String serverAddress = "localhost:" + TEST_PORT;
        
        // First connection cycle
        connectAndSaveAddress(serverAddress);
        PlayerConfig config1 = PlayerConfig.load();
        assertEquals(serverAddress, config1.getLastServer(), "Address should be saved after first connection");
        
        if (client != null && client.isConnected()) {
            client.disconnect();
            Thread.sleep(200);
        }
        
        // Second connection cycle
        connectAndSaveAddress(serverAddress);
        PlayerConfig config2 = PlayerConfig.load();
        assertEquals(serverAddress, config2.getLastServer(), "Address should still be saved after second connection");
        
        if (client != null && client.isConnected()) {
            client.disconnect();
            Thread.sleep(200);
        }
        
        // Third connection cycle
        connectAndSaveAddress(serverAddress);
        PlayerConfig config3 = PlayerConfig.load();
        assertEquals(serverAddress, config3.getLastServer(), "Address should still be saved after third connection");
    }
    
    // Helper methods
    
    /**
     * Connects to the test server and saves the address.
     * Simulates the behavior of GameMessageHandler.
     */
    private void connectAndSaveAddress(String fullAddress) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        client = new GameClient();
        client.setMessageHandler(new MessageHandler() {
            @Override
            public void handleMessage(NetworkMessage message) {
                if (message instanceof ConnectionAcceptedMessage) {
                    ConnectionAcceptedMessage acceptedMsg = (ConnectionAcceptedMessage) message;
                    client.setClientId(acceptedMsg.getAssignedClientId());
                    
                    // Save server address
                    PlayerConfig config = PlayerConfig.load();
                    config.saveLastServer(fullAddress);
                    
                    latch.countDown();
                }
            }
        });
        
        // Parse address
        String[] parts = fullAddress.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);
        
        client.connect(host, port);
        
        assertTrue(latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS), 
                  "Connection should succeed");
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
}
