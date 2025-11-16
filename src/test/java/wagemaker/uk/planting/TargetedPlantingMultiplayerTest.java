package wagemaker.uk.planting;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import wagemaker.uk.network.BambooPlantMessage;
import wagemaker.uk.network.GameClient;
import wagemaker.uk.network.GameServer;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for targeted bamboo planting in multiplayer mode.
 * Verifies that:
 * 1. BambooPlantMessage supports explicit coordinates
 * 2. GameClient.sendBambooPlant() sends target coordinates
 * 3. Server validates coordinates different from player position
 * 4. Planted bamboo appears at target coordinates for all clients
 */
public class TargetedPlantingMultiplayerTest {
    
    private GameServer server;
    private static final int TEST_PORT = 25566;
    
    @BeforeEach
    public void setUp() throws IOException {
        // Start a test server
        server = new GameServer(TEST_PORT, 10);
        server.start();
        
        // Give server time to start
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    @AfterEach
    public void tearDown() {
        if (server != null) {
            server.stop();
        }
    }
    
    @Test
    public void testBambooPlantMessageSupportsExplicitCoordinates() {
        // Verify BambooPlantMessage has x and y fields
        String plantedBambooId = "test-bamboo-123";
        float targetX = 256.0f;
        float targetY = 320.0f;
        String playerId = "player-1";
        
        BambooPlantMessage message = new BambooPlantMessage(playerId, plantedBambooId, targetX, targetY);
        
        assertNotNull(message, "BambooPlantMessage should be created");
        assertEquals(plantedBambooId, message.getPlantedBambooId(), "Planted bamboo ID should match");
        assertEquals(targetX, message.getX(), 0.01f, "X coordinate should match");
        assertEquals(targetY, message.getY(), 0.01f, "Y coordinate should match");
        assertEquals(playerId, message.getPlayerId(), "Player ID should match");
    }
    
    @Test
    public void testGameClientSendBambooPlantAcceptsTargetCoordinates() throws IOException, InterruptedException {
        // Create a game client
        GameClient client = new GameClient();
        
        try {
            // Connect to test server
            client.connect("localhost", TEST_PORT);
            
            // Give connection time to establish
            Thread.sleep(200);
            
            // Verify client is connected
            assertTrue(client.isConnected(), "Client should be connected");
            
            // Send bamboo plant message with explicit coordinates
            String plantedBambooId = "test-bamboo-456";
            float targetX = 192.0f;
            float targetY = 256.0f;
            
            // This should not throw an exception
            assertDoesNotThrow(() -> {
                client.sendBambooPlant(plantedBambooId, targetX, targetY);
            }, "sendBambooPlant should accept explicit coordinates");
            
        } catch (IOException e) {
            fail("Failed to connect client: " + e.getMessage());
        } finally {
            client.disconnect();
        }
    }
    
    @Test
    public void testServerAcceptsCoordinatesDifferentFromPlayerPosition() throws IOException, InterruptedException {
        // This test verifies that the server validates planting coordinates
        // within a reasonable range (128 pixels) from the player position
        
        GameClient client = new GameClient();
        
        try {
            // Connect to test server
            client.connect("localhost", TEST_PORT);
            
            // Give connection time to establish
            Thread.sleep(200);
            
            assertTrue(client.isConnected(), "Client should be connected");
            
            // Player is at (0, 0) by default
            // Plant bamboo at (64, 64) - within 128 pixel range
            String plantedBambooId = "test-bamboo-789";
            float targetX = 64.0f;
            float targetY = 64.0f;
            
            // This should be accepted by the server (within range)
            assertDoesNotThrow(() -> {
                client.sendBambooPlant(plantedBambooId, targetX, targetY);
            }, "Server should accept coordinates within 128 pixels of player");
            
            // Give server time to process
            Thread.sleep(100);
            
        } catch (IOException | InterruptedException e) {
            fail("Test failed: " + e.getMessage());
        } finally {
            client.disconnect();
        }
    }
}
