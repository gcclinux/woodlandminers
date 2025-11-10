package wagemaker.uk.network;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

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
        this.port = port;
        this.maxClients = maxClients;
        this.connectedClients = new ConcurrentHashMap<>();
        this.clientThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.running = false;
        
        // Initialize world state with random seed
        long seed = System.currentTimeMillis();
        this.worldState = new WorldState(seed);
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
        
        for (ClientConnection client : connectedClients.values()) {
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
}
