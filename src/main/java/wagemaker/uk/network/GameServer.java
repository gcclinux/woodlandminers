package wagemaker.uk.network;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import wagemaker.uk.respawn.RespawnManager;
import wagemaker.uk.respawn.RespawnEntry;
import wagemaker.uk.respawn.ResourceType;

/**
 * GameServer manages the authoritative game state and handles client connections.
 * It accepts incoming connections, manages client sessions, and synchronizes world state.
 */
public class GameServer {
    private static final int DEFAULT_PORT = 25565;
    private static final int DEFAULT_MAX_CLIENTS = 20;
    private static final int THREAD_POOL_SIZE = 10;
    
    private int maxClients;
    
    private ServerSocket serverSocket;
    private Map<String, ClientConnection> connectedClients;
    private WorldState worldState;
    private RespawnManager respawnManager;
    private ExecutorService clientThreadPool;
    private Thread acceptThread;
    private boolean running;
    private int port;
    
    /**
     * Creates a new GameServer with default port.
     */
    public GameServer() {
        this(DEFAULT_PORT, DEFAULT_MAX_CLIENTS);
    }
    
    /**
     * Creates a new GameServer with specified port.
     * @param port The port to bind to
     */
    public GameServer(int port) {
        this(port, DEFAULT_MAX_CLIENTS);
    }
    
    /**
     * Creates a new GameServer with specified port and max clients.
     * @param port The port to bind to
     * @param maxClients The maximum number of concurrent clients
     */
    public GameServer(int port, int maxClients) {
        this(port, maxClients, 0);
    }
    
    /**
     * Creates a new GameServer with specified port, max clients, and world seed.
     * @param port The port to bind to
     * @param maxClients The maximum number of concurrent clients
     * @param worldSeed The world seed (0 for random)
     */
    public GameServer(int port, int maxClients, long worldSeed) {
        this.port = port;
        this.maxClients = maxClients;
        this.connectedClients = new ConcurrentHashMap<>();
        this.clientThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.running = false;
        
        // Initialize world state with specified or random seed
        long seed = (worldSeed == 0) ? System.currentTimeMillis() : worldSeed;
        this.worldState = new WorldState(seed);
        System.out.println("World initialized with seed: " + seed);
    }
    
    /**
     * Starts the server and begins accepting client connections.
     * @throws IOException if the server cannot bind to the port
     */
    public void start() throws IOException {
        start(this.port);
    }
    
    /**
     * Starts the server on the specified port.
     * @param port The port to bind to
     * @throws IOException if the server cannot bind to the port
     */
    public void start(int port) throws IOException {
        if (running) {
            throw new IllegalStateException("Server is already running");
        }
        
        this.port = port;
        this.serverSocket = new ServerSocket(port);
        this.running = true;
        
        System.out.println("GameServer started on port " + port);
        System.out.println("Server IP: " + getPublicIPv4());
        
        // Start accepting clients in a separate thread
        acceptThread = new Thread(this::acceptClients, "ServerAcceptThread");
        acceptThread.start();
    }
    
    /**
     * Stops the server and disconnects all clients gracefully.
     */
    public void stop() {
        if (!running) {
            return;
        }
        
        System.out.println("Stopping GameServer...");
        running = false;
        
        // Disconnect all clients
        for (ClientConnection client : connectedClients.values()) {
            try {
                client.close();
            } catch (Exception e) {
                System.err.println("Error closing client connection: " + e.getMessage());
            }
        }
        connectedClients.clear();
        
        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
        
        // Shutdown thread pool
        clientThreadPool.shutdown();
        try {
            if (!clientThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                clientThreadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            clientThreadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("GameServer stopped");
    }
    
    /**
     * Checks if the server is currently running.
     * @return true if the server is running, false otherwise
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Gets the current world state.
     * @return The authoritative world state
     */
    public WorldState getWorldState() {
        return worldState;
    }
    
    /**
     * Updates the world state with the given update.
     * @param update The world state update to apply
     */
    public void updateWorldState(WorldStateUpdate update) {
        if (update != null) {
            worldState.applyUpdate(update);
        }
    }
    
    /**
     * Sets the respawn manager for this server.
     * The respawn manager handles resource respawn timers and synchronization.
     * @param respawnManager The respawn manager instance
     */
    public void setRespawnManager(RespawnManager respawnManager) {
        this.respawnManager = respawnManager;
        System.out.println("[GameServer] RespawnManager set");
    }
    
    /**
     * Gets the respawn manager for this server.
     * @return The respawn manager instance, or null if not set
     */
    public RespawnManager getRespawnManager() {
        return respawnManager;
    }
    
    /**
     * Gets the public IPv4 address of this server.
     * Attempts to determine the external IP address for clients to connect to.
     * @return The public IPv4 address as a string, or "Unknown" if it cannot be determined
     */
    public String getPublicIPv4() {
        try {
            // Try to get the local address first
            InetAddress localAddress = InetAddress.getLocalHost();
            String hostAddress = localAddress.getHostAddress();
            
            // Check if it's a private address
            if (isPrivateAddress(hostAddress)) {
                // Try to get public IP from external service
                try {
                    URI uri = new URI("http://checkip.amazonaws.com");
                    URL whatismyip = uri.toURL();
                    BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
                    String ip = in.readLine();
                    in.close();
                    return ip;
                } catch (Exception e) {
                    // If external service fails, return local address anyway
                    return hostAddress;
                }
            }
            
            return hostAddress;
        } catch (UnknownHostException e) {
            System.err.println("Unable to determine server IP: " + e.getMessage());
            return "Unknown";
        }
    }
    
    /**
     * Checks if an IP address is a private address.
     * @param ip The IP address to check
     * @return true if the address is private, false otherwise
     */
    private boolean isPrivateAddress(String ip) {
        return ip.startsWith("192.168.") || 
               ip.startsWith("10.") || 
               ip.startsWith("172.16.") ||
               ip.equals("127.0.0.1");
    }
    
    /**
     * Accepts incoming client connections in a loop.
     * This method runs in a separate thread.
     */
    private void acceptClients() {
        System.out.println("Server accepting connections...");
        
        while (running && !serverSocket.isClosed()) {
            try {
                Socket clientSocket = serverSocket.accept();
                
                // Check if we've reached max clients
                if (connectedClients.size() >= maxClients) {
                    System.out.println("[SECURITY] Max clients reached (" + maxClients + 
                                     "), rejecting connection from " + clientSocket.getInetAddress());
                    sendRejectionMessage(clientSocket, "Server is full");
                    clientSocket.close();
                    continue;
                }
                
                // Handle the new client connection
                handleClientConnection(clientSocket);
                
            } catch (SocketException e) {
                // Socket was closed, this is expected when stopping
                if (running) {
                    System.err.println("Socket error: " + e.getMessage());
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting client: " + e.getMessage());
                }
            }
        }
        
        System.out.println("Server stopped accepting connections");
    }
    
    /**
     * Handles a new client connection.
     * Creates a ClientConnection and adds it to the connected clients map.
     * @param clientSocket The socket for the new client
     */
    private void handleClientConnection(Socket clientSocket) {
        try {
            String clientId = UUID.randomUUID().toString();
            System.out.println("New client connecting: " + clientId + 
                             " from " + clientSocket.getInetAddress());
            
            // Create client connection
            ClientConnection clientConnection = new ClientConnection(
                clientSocket, clientId, this
            );
            
            connectedClients.put(clientId, clientConnection);
            
            // Start handling this client in the thread pool
            clientThreadPool.execute(clientConnection);
            
            System.out.println("Client connected: " + clientId + 
                             " (Total clients: " + connectedClients.size() + ")");
            
        } catch (IOException e) {
            System.err.println("Error setting up client connection: " + e.getMessage());
            try {
                clientSocket.close();
            } catch (IOException ex) {
                // Ignore
            }
        }
    }
    
    /**
     * Sends a rejection message to a client and closes the connection.
     * @param clientSocket The client socket
     * @param reason The reason for rejection
     */
    private void sendRejectionMessage(Socket clientSocket, String reason) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.writeObject(new ConnectionRejectedMessage("server", reason));
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending rejection message: " + e.getMessage());
        }
    }
    
    /**
     * Disconnects a client from the server.
     * @param clientId The ID of the client to disconnect
     */
    public void disconnectClient(String clientId) {
        ClientConnection client = connectedClients.remove(clientId);
        if (client != null) {
            try {
                client.close();
                System.out.println("Client disconnected: " + clientId + 
                                 " (Total clients: " + connectedClients.size() + ")");
            } catch (Exception e) {
                System.err.println("Error disconnecting client: " + e.getMessage());
            }
        }
    }
    
    /**
     * Gets the number of connected clients.
     * @return The number of connected clients
     */
    public int getConnectedClientCount() {
        return connectedClients.size();
    }
    
    /**
     * Gets a client connection by ID.
     * @param clientId The client ID
     * @return The client connection, or null if not found
     */
    public ClientConnection getClient(String clientId) {
        return connectedClients.get(clientId);
    }
    
    /**
     * Gets all connected clients.
     * @return A collection of all connected clients
     */
    public Collection<ClientConnection> getAllClients() {
        return connectedClients.values();
    }
    
    /**
     * Gets the maximum number of clients allowed.
     * @return The maximum client limit
     */
    public int getMaxClients() {
        return maxClients;
    }
    
    /**
     * Sets the maximum number of clients allowed.
     * @param maxClients The maximum client limit
     */
    public void setMaxClients(int maxClients) {
        if (maxClients < 1) {
            throw new IllegalArgumentException("Max clients must be at least 1");
        }
        this.maxClients = maxClients;
        System.out.println("Max clients set to: " + maxClients);
    }
    
    /**
     * Broadcasts a message to all connected clients.
     * Messages are queued and sent reliably via TCP.
     * @param message The message to broadcast
     */
    public void broadcastToAll(NetworkMessage message) {
        if (message == null) {
            return;
        }
        
        List<String> failedClients = new ArrayList<>();
        int successCount = 0;
        
        for (ClientConnection client : connectedClients.values()) {
            try {
                if (client.isAlive()) {
                    client.sendMessage(message);
                    successCount++;
                } else {
                    failedClients.add(client.getClientId());
                }
            } catch (Exception e) {
                System.err.println("Error broadcasting to client " + 
                                 client.getClientId() + ": " + e.getMessage());
                failedClients.add(client.getClientId());
            }
        }
        
        // Log broadcast statistics for bamboo plant messages
        if (message.getType() == MessageType.BAMBOO_PLANT) {
            System.out.println("[GameServer] Broadcast BambooPlantMessage to " + successCount + " clients (failed: " + failedClients.size() + ")");
        }
        
        // Clean up failed clients
        for (String clientId : failedClients) {
            disconnectClient(clientId);
        }
    }
    
    /**
     * Broadcasts a message to all connected clients except the specified one.
     * This is useful for broadcasting player actions to other players.
     * @param message The message to broadcast
     * @param excludeClientId The client ID to exclude from the broadcast
     */
    public void broadcastToAllExcept(NetworkMessage message, String excludeClientId) {
        if (message == null) {
            return;
        }
        
        List<String> failedClients = new ArrayList<>();
        
        for (ClientConnection client : connectedClients.values()) {
            // Skip the excluded client
            if (client.getClientId().equals(excludeClientId)) {
                continue;
            }
            
            try {
                if (client.isAlive()) {
                    client.sendMessage(message);
                } else {
                    failedClients.add(client.getClientId());
                }
            } catch (Exception e) {
                System.err.println("Error broadcasting to client " + 
                                 client.getClientId() + ": " + e.getMessage());
                failedClients.add(client.getClientId());
            }
        }
        
        // Clean up failed clients
        for (String clientId : failedClients) {
            disconnectClient(clientId);
        }
    }
    
    /**
     * Broadcasts a resource respawn event to all connected clients.
     * Called by the respawn manager when a resource respawns.
     * @param resourceId Unique identifier for the resource
     * @param resourceType Type of resource (TREE or STONE)
     * @param treeType Type of tree (null for stones)
     * @param x World X coordinate
     * @param y World Y coordinate
     */
    public void broadcastResourceRespawn(String resourceId, ResourceType resourceType, 
                                        TreeType treeType, float x, float y) {
        ResourceRespawnMessage message = new ResourceRespawnMessage(
            "server", resourceId, resourceType, treeType, x, y
        );
        
        System.out.println("[GameServer] Broadcasting resource respawn: " + message);
        broadcastToAll(message);
    }
    
    /**
     * Sends the current respawn state to a specific client.
     * Called when a client joins to synchronize pending respawn timers.
     * @param client The client connection to send the state to
     */
    public void sendRespawnStateToClient(ClientConnection client) {
        if (respawnManager == null) {
            System.out.println("[GameServer] No respawn manager, skipping respawn state sync");
            return;
        }
        
        List<RespawnEntry> pendingRespawns = respawnManager.getSaveData();
        RespawnStateMessage message = new RespawnStateMessage("server", pendingRespawns);
        
        System.out.println("[GameServer] Sending respawn state to client " + client.getClientId() + 
                         " (" + pendingRespawns.size() + " pending respawns)");
        
        try {
            client.sendMessage(message);
        } catch (Exception e) {
            System.err.println("[GameServer] Error sending respawn state to client " + 
                             client.getClientId() + ": " + e.getMessage());
        }
    }
    
    /**
     * Generates chunks around all players.
     * Called when players move to ensure trees/stones exist in their vicinity.
     * Only generates entities that don't already exist.
     */
    public void generateChunksAroundPlayers() {
        if (worldState == null) {
            return;
        }
        
        Map<String, PlayerState> players = worldState.getPlayers();
        if (players == null || players.isEmpty()) {
            return;
        }
        
        // Generate chunks around each player
        for (PlayerState player : players.values()) {
            generateChunksAroundPosition(player.getX(), player.getY());
        }
    }
    
    /**
     * Generates chunks around a specific position.
     * Creates trees and stones in a grid pattern around the position.
     * 
     * @param centerX The center X coordinate
     * @param centerY The center Y coordinate
     */
    private void generateChunksAroundPosition(float centerX, float centerY) {
        // Generate in a 3x3 chunk area around player (approximately 1920x1920 pixels)
        int chunkSize = 64; // Same as grass tile size
        int chunksRadius = 15; // Generate 15 chunks in each direction (960 pixels)
        
        int startX = ((int)centerX / chunkSize - chunksRadius) * chunkSize;
        int startY = ((int)centerY / chunkSize - chunksRadius) * chunkSize;
        int endX = ((int)centerX / chunkSize + chunksRadius) * chunkSize;
        int endY = ((int)centerY / chunkSize + chunksRadius) * chunkSize;
        
        for (int x = startX; x <= endX; x += chunkSize) {
            for (int y = startY; y <= endY; y += chunkSize) {
                // Generate tree at this position (if it should exist)
                TreeState tree = worldState.generateTreeAt(x, y);
                if (tree != null && tree.isExists()) {
                    // Broadcast new tree to all clients (only if it exists)
                    TreeCreatedMessage message = new TreeCreatedMessage(
                        "server",
                        tree.getTreeId(),
                        tree.getType(),
                        tree.getX(),
                        tree.getY(),
                        tree.getHealth()
                    );
                    broadcastToAll(message);
                }
                
                // Generate stone at this position (if it should exist)
                StoneState stone = worldState.generateStoneAt(x, y, centerX, centerY);
                if (stone != null && stone.getHealth() > 0) {
                    // Broadcast new stone to all clients (only if not destroyed)
                    StoneCreatedMessage message = new StoneCreatedMessage(
                        "server",
                        stone.getStoneId(),
                        stone.getX(),
                        stone.getY(),
                        stone.getHealth()
                    );
                    broadcastToAll(message);
                }
            }
        }
    }
}
