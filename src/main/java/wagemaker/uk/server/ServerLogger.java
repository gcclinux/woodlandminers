package wagemaker.uk.server;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ServerLogger handles logging for the dedicated server.
 * It logs connection events, errors, world state changes, and periodic status updates.
 */
public class ServerLogger {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String LOG_FILE = "server.log";
    
    private final boolean debugMode;
    private final PrintWriter logWriter;
    private final long startTime;
    
    /**
     * Creates a new ServerLogger.
     * @param debugMode Whether to enable debug logging
     */
    public ServerLogger(boolean debugMode) {
        this.debugMode = debugMode;
        this.startTime = System.currentTimeMillis();
        
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileWriter(LOG_FILE, true), true);
            writer.println();
            writer.println("===========================================");
            writer.println("Server started at " + getCurrentTimestamp());
            writer.println("===========================================");
        } catch (IOException e) {
            System.err.println("Failed to open log file: " + e.getMessage());
            System.err.println("Logging to console only.");
        }
        this.logWriter = writer;
    }
    
    /**
     * Logs a client connection event.
     * @param clientId The client ID
     * @param address The client's IP address
     */
    public void logClientJoin(String clientId, String address) {
        String message = String.format("[CONNECTION] Client joined: %s from %s", clientId, address);
        log(message);
    }
    
    /**
     * Logs a client disconnection event.
     * @param clientId The client ID
     * @param reason The reason for disconnection (optional)
     */
    public void logClientLeave(String clientId, String reason) {
        String message;
        if (reason != null && !reason.isEmpty()) {
            message = String.format("[CONNECTION] Client left: %s (Reason: %s)", clientId, reason);
        } else {
            message = String.format("[CONNECTION] Client left: %s", clientId);
        }
        log(message);
    }
    
    /**
     * Logs a network error.
     * @param message The error message
     * @param exception The exception that occurred (optional)
     */
    public void logNetworkError(String message, Exception exception) {
        String logMessage = "[ERROR] Network error: " + message;
        if (exception != null) {
            logMessage += " - " + exception.getMessage();
        }
        log(logMessage);
        
        if (debugMode && exception != null) {
            logException(exception);
        }
    }
    
    /**
     * Logs a tree destruction event.
     * @param treeId The tree ID
     * @param clientId The client who destroyed the tree
     */
    public void logTreeDestruction(String treeId, String clientId) {
        if (debugMode) {
            String message = String.format("[WORLD] Tree destroyed: %s by client %s", treeId, clientId);
            log(message);
        }
    }
    
    /**
     * Logs an item spawn event.
     * @param itemId The item ID
     * @param itemType The type of item
     * @param x The x position
     * @param y The y position
     */
    public void logItemSpawn(String itemId, String itemType, float x, float y) {
        if (debugMode) {
            String message = String.format("[WORLD] Item spawned: %s (%s) at (%.1f, %.1f)", 
                                         itemId, itemType, x, y);
            log(message);
        }
    }
    
    /**
     * Logs an item pickup event.
     * @param itemId The item ID
     * @param clientId The client who picked up the item
     */
    public void logItemPickup(String itemId, String clientId) {
        if (debugMode) {
            String message = String.format("[WORLD] Item picked up: %s by client %s", itemId, clientId);
            log(message);
        }
    }
    
    /**
     * Logs a player action.
     * @param clientId The client ID
     * @param action The action performed
     */
    public void logPlayerAction(String clientId, String action) {
        if (debugMode) {
            String message = String.format("[ACTION] Client %s: %s", clientId, action);
            log(message);
        }
    }
    
    /**
     * Logs periodic server status.
     * @param connectedClients The number of connected clients
     * @param maxClients The maximum number of clients
     */
    public void logServerStatus(int connectedClients, int maxClients) {
        long uptimeSeconds = (System.currentTimeMillis() - startTime) / 1000;
        long hours = uptimeSeconds / 3600;
        long minutes = (uptimeSeconds % 3600) / 60;
        long seconds = uptimeSeconds % 60;
        
        String uptime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        String message = String.format("[STATUS] Uptime: %s | Clients: %d/%d", 
                                      uptime, connectedClients, maxClients);
        log(message);
    }
    
    /**
     * Logs a security event.
     * @param message The security message
     */
    public void logSecurityEvent(String message) {
        String logMessage = "[SECURITY] " + message;
        log(logMessage);
    }
    
    /**
     * Logs a debug message (only if debug mode is enabled).
     * @param message The debug message
     */
    public void logDebug(String message) {
        if (debugMode) {
            String logMessage = "[DEBUG] " + message;
            log(logMessage);
        }
    }
    
    /**
     * Logs an informational message.
     * @param message The info message
     */
    public void logInfo(String message) {
        String logMessage = "[INFO] " + message;
        log(logMessage);
    }
    
    /**
     * Logs a warning message.
     * @param message The warning message
     */
    public void logWarning(String message) {
        String logMessage = "[WARNING] " + message;
        log(logMessage);
    }
    
    /**
     * Logs an exception with stack trace.
     * @param exception The exception to log
     */
    private void logException(Exception exception) {
        if (logWriter != null) {
            exception.printStackTrace(logWriter);
        }
        exception.printStackTrace();
    }
    
    /**
     * Logs a message to both console and file.
     * @param message The message to log
     */
    private void log(String message) {
        String timestampedMessage = getCurrentTimestamp() + " " + message;
        
        // Log to console
        System.out.println(timestampedMessage);
        
        // Log to file
        if (logWriter != null) {
            logWriter.println(timestampedMessage);
        }
    }
    
    /**
     * Gets the current timestamp as a formatted string.
     * @return The current timestamp
     */
    private String getCurrentTimestamp() {
        return "[" + DATE_FORMAT.format(new Date()) + "]";
    }
    
    /**
     * Closes the logger and releases resources.
     */
    public void close() {
        if (logWriter != null) {
            logWriter.println("===========================================");
            logWriter.println("Server stopped at " + getCurrentTimestamp());
            logWriter.println("===========================================");
            logWriter.close();
        }
    }
}
