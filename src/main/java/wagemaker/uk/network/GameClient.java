package wagemaker.uk.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Client-side network manager that handles connection to the game server
 * and manages message sending/receiving.
 */
public class GameClient {
    private Socket socket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Thread receiveThread;
    private String clientId;
    private AtomicBoolean connected;
    private MessageHandler messageHandler;
    
    // Message throttling for position updates
    private long lastPositionUpdateTime;
    private static final long POSITION_UPDATE_INTERVAL_MS = 50; // 20 updates per second
    
    // Message queue for thread-safe sending
    private ConcurrentLinkedQueue<NetworkMessage> sendQueue;
    private Thread sendThread;
    
    // Heartbeat system
    private Thread heartbeatThread;
    private long lastHeartbeatTime;
    private static final long HEARTBEAT_INTERVAL_MS = 5000; // 5 seconds
    
    // Latency measurement system
    private Thread pingThread;
    private long lastPingTime;
    private static final long PING_INTERVAL_MS = 2000; // 2 seconds
    private static final int LATENCY_HISTORY_SIZE = 10;
    private long[] latencyHistory;
    private int latencyHistoryIndex;
    private long currentLatency;
    private long averageLatency;
    
    // Reconnection system
    private String lastServerAddress;
    private int lastServerPort;
    private int reconnectAttempts;
    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    private boolean intentionalDisconnect;
    
    /**
     * Creates a new GameClient instance.
     */
    public GameClient() {
        this.connected = new AtomicBoolean(false);
        this.sendQueue = new ConcurrentLinkedQueue<>();
        this.lastPositionUpdateTime = 0;
        this.lastHeartbeatTime = 0;
        this.reconnectAttempts = 0;
        this.intentionalDisconnect = false;
        this.latencyHistory = new long[LATENCY_HISTORY_SIZE];
        this.latencyHistoryIndex = 0;
        this.currentLatency = 0;
        this.averageLatency = 0;
        this.lastPingTime = 0;
    }
    
    /**
     * Sets the message handler for processing incoming messages.
     * @param handler The message handler implementation
     */
    public void setMessageHandler(MessageHandler handler) {
        this.messageHandler = handler;
    }
    
    /**
     * Connects to the game server at the specified address and port.
     * @param serverAddress The server IP address or hostname
     * @param port The server port
     * @throws IOException If connection fails
     */
    public void connect(String serverAddress, int port) throws IOException {
        if (connected.get()) {
            throw new IllegalStateException("Already connected to a server");
        }
        
        try {
            // Establish socket connection
            socket = new Socket(serverAddress, port);
            
            // Create output stream first (required for ObjectInputStream handshake)
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.flush();
            
            // Create input stream
            inputStream = new ObjectInputStream(socket.getInputStream());
            
            connected.set(true);
            
            // Store connection details for reconnection
            this.lastServerAddress = serverAddress;
            this.lastServerPort = port;
            this.reconnectAttempts = 0;
            this.intentionalDisconnect = false;
            
            // Start message receiving thread
            startReceiveThread();
            
            // Start message sending thread
            startSendThread();
            
            // Start heartbeat thread
            startHeartbeatThread();
            
            // Start ping thread for latency measurement
            startPingThread();
            
            System.out.println("Connected to server at " + serverAddress + ":" + port);
            
        } catch (IOException e) {
            // Clean up on connection failure
            cleanup();
            throw new IOException("Failed to connect to server: " + e.getMessage(), e);
        }
    }
    
    /**
     * Disconnects from the game server.
     */
    public void disconnect() {
        disconnect(true);
    }
    
    /**
     * Disconnects from the game server.
     * @param intentional Whether this is an intentional disconnect (no reconnection)
     */
    private void disconnect(boolean intentional) {
        if (!connected.get()) {
            return;
        }
        
        this.intentionalDisconnect = intentional;
        connected.set(false);
        
        // Stop threads
        if (receiveThread != null && receiveThread.isAlive()) {
            receiveThread.interrupt();
        }
        if (sendThread != null && sendThread.isAlive()) {
            sendThread.interrupt();
        }
        if (heartbeatThread != null && heartbeatThread.isAlive()) {
            heartbeatThread.interrupt();
        }
        if (pingThread != null && pingThread.isAlive()) {
            pingThread.interrupt();
        }
        
        // Clean up resources
        cleanup();
        
        System.out.println("Disconnected from server");
        
        // Attempt reconnection if not intentional
        if (!intentional && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            attemptReconnection();
        }
    }
    
    /**
     * Checks if the client is currently connected to a server.
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected.get() && socket != null && !socket.isClosed();
    }
    
    /**
     * Gets the assigned client ID from the server.
     * @return The client ID, or null if not yet assigned
     */
    public String getClientId() {
        return clientId;
    }
    
    /**
     * Sets the client ID (called when receiving connection accepted message).
     * @param clientId The assigned client ID
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    /**
     * Sends a message to the server.
     * Messages are queued and sent by a dedicated thread.
     * @param message The message to send
     */
    public void sendMessage(NetworkMessage message) {
        if (!isConnected()) {
            System.err.println("Cannot send message: not connected to server");
            return;
        }
        
        // Set sender ID if not already set
        if (message.getSenderId() == null && clientId != null) {
            message.setSenderId(clientId);
        }
        
        sendQueue.offer(message);
    }
    
    /**
     * Starts the thread that receives messages from the server.
     */
    private void startReceiveThread() {
        receiveThread = new Thread(() -> {
            while (connected.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    // Read message from server
                    Object obj = inputStream.readObject();
                    
                    if (obj instanceof NetworkMessage) {
                        NetworkMessage message = (NetworkMessage) obj;
                        handleIncomingMessage(message);
                    } else {
                        System.err.println("Received unknown object type: " + obj.getClass().getName());
                    }
                    
                } catch (IOException e) {
                    if (connected.get()) {
                        System.err.println("Connection lost: " + e.getMessage());
                        disconnect(false); // Trigger reconnection
                    }
                    break;
                } catch (ClassNotFoundException e) {
                    System.err.println("Received message with unknown class: " + e.getMessage());
                }
            }
        }, "GameClient-Receive");
        
        receiveThread.setDaemon(true);
        receiveThread.start();
    }
    
    /**
     * Starts the thread that sends queued messages to the server.
     */
    private void startSendThread() {
        sendThread = new Thread(() -> {
            while (connected.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    NetworkMessage message = sendQueue.poll();
                    
                    if (message != null) {
                        outputStream.writeObject(message);
                        outputStream.flush();
                    } else {
                        // No messages to send, sleep briefly
                        Thread.sleep(10);
                    }
                    
                } catch (IOException e) {
                    if (connected.get()) {
                        System.err.println("Failed to send message: " + e.getMessage());
                        disconnect(false); // Trigger reconnection
                    }
                    break;
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "GameClient-Send");
        
        sendThread.setDaemon(true);
        sendThread.start();
    }
    
    /**
     * Handles an incoming message from the server.
     * @param message The received message
     */
    private void handleIncomingMessage(NetworkMessage message) {
        if (messageHandler != null) {
            try {
                messageHandler.handleMessage(message);
            } catch (Exception e) {
                System.err.println("Error handling message: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("No message handler set, ignoring message: " + message.getType());
        }
    }
    
    /**
     * Sends a player movement update to the server with throttling and quantization.
     * Position updates are limited to 20 per second to reduce bandwidth.
     * Positions are quantized to the nearest pixel to reduce floating point precision.
     * @param x The player's x position
     * @param y The player's y position
     * @param direction The player's facing direction
     * @param isMoving Whether the player is currently moving
     */
    public void sendPlayerMovement(float x, float y, Direction direction, boolean isMoving) {
        // Don't send if client ID not set yet
        if (clientId == null) {
            return;
        }
        
        // Throttle position updates to 20 per second
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPositionUpdateTime < POSITION_UPDATE_INTERVAL_MS) {
            return; // Skip this update
        }
        
        lastPositionUpdateTime = currentTime;
        
        // Quantize positions to nearest pixel to reduce message size
        float quantizedX = quantizePosition(x);
        float quantizedY = quantizePosition(y);
        
        PlayerMovementMessage message = new PlayerMovementMessage(clientId, quantizedX, quantizedY, direction, isMoving);
        sendMessage(message);
    }
    
    /**
     * Quantizes a position value to the nearest pixel.
     * This reduces floating point precision and minimizes message size.
     * @param position The position value to quantize
     * @return The quantized position rounded to the nearest pixel
     */
    private float quantizePosition(float position) {
        return Math.round(position);
    }
    
    /**
     * Sends an attack action to the server.
     * @param targetId The ID of the target being attacked (e.g., tree ID)
     */
    public void sendAttackAction(String targetId) {
        if (clientId == null) {
            System.err.println("Cannot send attack action: client ID not set");
            return;
        }
        
        AttackActionMessage message = new AttackActionMessage(clientId, clientId, targetId);
        sendMessage(message);
    }
    
    /**
     * Sends an item pickup request to the server.
     * @param itemId The ID of the item to pick up
     */
    public void sendItemPickup(String itemId) {
        if (clientId == null) {
            System.err.println("Cannot send item pickup: client ID not set");
            return;
        }
        
        ItemPickupMessage message = new ItemPickupMessage(clientId, itemId, clientId);
        sendMessage(message);
    }
    
    /**
     * Sends a player health update to the server.
     * @param health The player's current health value
     */
    public void sendPlayerHealth(float health) {
        if (clientId == null) {
            System.err.println("Cannot send health update: client ID not set");
            return;
        }
        
        PlayerHealthUpdateMessage message = new PlayerHealthUpdateMessage(clientId, clientId, health);
        sendMessage(message);
    }
    
    /**
     * Sends a bamboo planting action to the server.
     * @param plantedBambooId The unique ID for the planted bamboo
     * @param x The tile-aligned x position
     * @param y The tile-aligned y position
     */
    public void sendBambooPlant(String plantedBambooId, float x, float y) {
        if (clientId == null) {
            System.err.println("Cannot send bamboo plant: client ID not set");
            return;
        }
        
        BambooPlantMessage message = new BambooPlantMessage(clientId, plantedBambooId, x, y);
        sendMessage(message);
    }
    
    /**
     * Sends a heartbeat message to the server to maintain connection.
     */
    public void sendHeartbeat() {
        HeartbeatMessage message = new HeartbeatMessage(clientId);
        sendMessage(message);
        lastHeartbeatTime = System.currentTimeMillis();
    }
    
    /**
     * Starts the heartbeat thread that sends periodic heartbeat messages.
     */
    private void startHeartbeatThread() {
        heartbeatThread = new Thread(() -> {
            while (connected.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    long currentTime = System.currentTimeMillis();
                    
                    // Send heartbeat if interval has passed
                    if (currentTime - lastHeartbeatTime >= HEARTBEAT_INTERVAL_MS) {
                        sendHeartbeat();
                    }
                    
                    // Sleep for a short time
                    Thread.sleep(1000);
                    
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "GameClient-Heartbeat");
        
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
    }
    
    /**
     * Sends a ping message to the server to measure latency.
     */
    public void sendPing() {
        PingMessage message = new PingMessage(clientId);
        sendMessage(message);
        lastPingTime = System.currentTimeMillis();
    }
    
    /**
     * Handles a pong response from the server.
     * Calculates round-trip time and updates latency statistics.
     * @param pongMessage The pong message from the server
     */
    public void handlePong(PongMessage pongMessage) {
        long currentTime = System.currentTimeMillis();
        long roundTripTime = currentTime - pongMessage.getPingTimestamp();
        
        // Store in history
        latencyHistory[latencyHistoryIndex] = roundTripTime;
        latencyHistoryIndex = (latencyHistoryIndex + 1) % LATENCY_HISTORY_SIZE;
        
        // Update current latency
        currentLatency = roundTripTime;
        
        // Calculate average latency over last 10 pings
        long sum = 0;
        int count = 0;
        for (long latency : latencyHistory) {
            if (latency > 0) {
                sum += latency;
                count++;
            }
        }
        
        if (count > 0) {
            averageLatency = sum / count;
        }
    }
    
    /**
     * Gets the current latency in milliseconds.
     * @return The most recent round-trip time
     */
    public long getCurrentLatency() {
        return currentLatency;
    }
    
    /**
     * Gets the average latency over the last 10 pings.
     * @return The average latency in milliseconds
     */
    public long getAverageLatency() {
        return averageLatency;
    }
    
    /**
     * Starts the ping thread that sends periodic ping messages for latency measurement.
     */
    private void startPingThread() {
        pingThread = new Thread(() -> {
            while (connected.get() && !Thread.currentThread().isInterrupted()) {
                try {
                    long currentTime = System.currentTimeMillis();
                    
                    // Send ping if interval has passed
                    if (currentTime - lastPingTime >= PING_INTERVAL_MS) {
                        sendPing();
                    }
                    
                    // Sleep for a short time
                    Thread.sleep(500);
                    
                } catch (InterruptedException e) {
                    break;
                }
            }
        }, "GameClient-Ping");
        
        pingThread.setDaemon(true);
        pingThread.start();
    }
    
    /**
     * Attempts to reconnect to the server after connection loss.
     */
    private void attemptReconnection() {
        if (lastServerAddress == null || intentionalDisconnect) {
            return;
        }
        
        reconnectAttempts++;
        System.out.println("Attempting reconnection " + reconnectAttempts + "/" + MAX_RECONNECT_ATTEMPTS + "...");
        
        // Notify handler about reconnection attempt
        if (messageHandler != null) {
            try {
                // Create a custom message to notify about reconnection
                System.out.println("Reconnection attempt " + reconnectAttempts);
            } catch (Exception e) {
                System.err.println("Error notifying handler: " + e.getMessage());
            }
        }
        
        // Wait a bit before reconnecting
        try {
            Thread.sleep(2000); // 2 second delay
        } catch (InterruptedException e) {
            return;
        }
        
        // Try to reconnect
        try {
            connect(lastServerAddress, lastServerPort);
            System.out.println("Reconnection successful!");
            reconnectAttempts = 0; // Reset on success
            
        } catch (IOException e) {
            System.err.println("Reconnection attempt " + reconnectAttempts + " failed: " + e.getMessage());
            
            if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
                System.err.println("Max reconnection attempts reached. Connection lost.");
                
                // Notify handler about connection loss
                if (messageHandler != null) {
                    try {
                        System.err.println("Connection lost after " + MAX_RECONNECT_ATTEMPTS + " attempts");
                    } catch (Exception ex) {
                        System.err.println("Error notifying handler: " + ex.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * Requests the complete world state from the server.
     * Typically called after initial connection.
     */
    public void requestWorldState() {
        // The server automatically sends world state on connection
        // This method is provided for explicit requests if needed
        System.out.println("World state request - server sends automatically on connect");
    }
    
    /**
     * Gets the number of reconnection attempts made.
     * @return The reconnection attempt count
     */
    public int getReconnectAttempts() {
        return reconnectAttempts;
    }
    
    /**
     * Checks if the client is attempting to reconnect.
     * @return true if reconnecting, false otherwise
     */
    public boolean isReconnecting() {
        return !connected.get() && !intentionalDisconnect && reconnectAttempts > 0 && reconnectAttempts < MAX_RECONNECT_ATTEMPTS;
    }
    
    /**
     * Cleans up network resources.
     */
    private void cleanup() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            // Ignore
        }
        
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            // Ignore
        }
        
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // Ignore
        }
        
        inputStream = null;
        outputStream = null;
        socket = null;
    }
}
