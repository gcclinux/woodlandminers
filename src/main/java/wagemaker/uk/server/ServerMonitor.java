package wagemaker.uk.server;

import wagemaker.uk.network.GameServer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ServerMonitor periodically logs server status and statistics.
 * It runs in a background thread and provides regular status updates.
 */
public class ServerMonitor {
    private static final int STATUS_LOG_INTERVAL_SECONDS = 60; // Log status every minute
    
    private final GameServer server;
    private final ServerLogger logger;
    private final ScheduledExecutorService scheduler;
    private boolean running;
    
    /**
     * Creates a new ServerMonitor.
     * @param server The GameServer to monitor
     * @param logger The ServerLogger to use for logging
     */
    public ServerMonitor(GameServer server, ServerLogger logger) {
        this.server = server;
        this.logger = logger;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ServerMonitor");
            t.setDaemon(true);
            return t;
        });
        this.running = false;
    }
    
    /**
     * Starts the server monitor.
     * Begins periodic status logging.
     */
    public void start() {
        if (running) {
            return;
        }
        
        running = true;
        
        // Schedule periodic status logging
        scheduler.scheduleAtFixedRate(
            this::logStatus,
            STATUS_LOG_INTERVAL_SECONDS,
            STATUS_LOG_INTERVAL_SECONDS,
            TimeUnit.SECONDS
        );
        
        logger.logInfo("Server monitor started");
    }
    
    /**
     * Stops the server monitor.
     */
    public void stop() {
        if (!running) {
            return;
        }
        
        running = false;
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logger.logInfo("Server monitor stopped");
    }
    
    /**
     * Logs the current server status.
     */
    private void logStatus() {
        if (!server.isRunning()) {
            return;
        }
        
        try {
            int connectedClients = server.getConnectedClientCount();
            int maxClients = server.getMaxClients();
            
            logger.logServerStatus(connectedClients, maxClients);
            
        } catch (Exception e) {
            logger.logWarning("Error logging server status: " + e.getMessage());
        }
    }
}
