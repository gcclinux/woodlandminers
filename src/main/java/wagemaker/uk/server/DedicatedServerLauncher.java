package wagemaker.uk.server;

import wagemaker.uk.network.GameServer;

import java.io.IOException;

/**
 * DedicatedServerLauncher is the main entry point for running a standalone game server.
 * It can be launched from the command line to host a server without a game client.
 * 
 * Usage: java -jar woodlanders-server.jar [--port PORT]
 */
public class DedicatedServerLauncher {
    private static final int DEFAULT_PORT = 25565;
    
    /**
     * Main entry point for the dedicated server.
     * @param args Command-line arguments (--port PORT, --config CONFIG_FILE)
     */
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  Woodlanders Dedicated Server");
        System.out.println("===========================================");
        System.out.println();
        
        // Parse command-line arguments
        String configFile = parseConfigFile(args);
        
        // Load configuration
        ServerConfig config = ServerConfig.load(configFile);
        System.out.println();
        config.printConfig();
        System.out.println();
        
        // Override port from command line if specified
        int port = parsePort(args);
        if (port != DEFAULT_PORT) {
            System.out.println("Port overridden by command line: " + port);
        } else {
            port = config.getPort();
        }
        
        // Override max clients from command line if specified
        int maxClients = parseMaxClients(args);
        if (maxClients != -1) {
            System.out.println("Max clients overridden by command line: " + maxClients);
        } else {
            maxClients = config.getMaxClients();
        }
        
        // Override world seed from command line if specified
        long worldSeed = parseSeed(args);
        if (worldSeed != -1) {
            System.out.println("World seed overridden by command line: " + worldSeed);
        } else {
            worldSeed = config.getWorldSeed();
        }
        
        // Override debug mode from command line if specified
        boolean debug = parseDebug(args) || config.isDebug();
        
        // Initialize logger
        ServerLogger logger = new ServerLogger(debug);
        
        // Create and start the server with configuration
        GameServer server = new GameServer(port, maxClients, worldSeed);
        server.setMaxClients(maxClients);
        
        // Create and start server monitor
        ServerMonitor monitor = new ServerMonitor(server, logger);
        
        // Setup shutdown hook for graceful termination
        setupShutdownHook(server, logger, monitor);
        
        try {
            server.start();
            System.out.println();
            System.out.println("Server Information:");
            System.out.println("  Port: " + port);
            System.out.println("  Public IP: " + server.getPublicIPv4());
            System.out.println("  Max Clients: " + server.getMaxClients());
            System.out.println();
            System.out.println("Server is running. Press Ctrl+C to stop.");
            System.out.println("===========================================");
            System.out.println();
            
            // Start monitoring
            monitor.start();
            
            logger.logInfo("Server started successfully");
            
            // Keep server running
            while (server.isRunning()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    logger.logInfo("Server interrupted, shutting down...");
                    break;
                }
            }
            
        } catch (IOException e) {
            logger.logNetworkError("Failed to start server: " + e.getMessage(), e);
            System.err.println("Make sure port " + port + " is not already in use.");
            logger.close();
            System.exit(1);
        } catch (Exception e) {
            logger.logNetworkError("Server error: " + e.getMessage(), e);
            e.printStackTrace();
            logger.close();
            System.exit(1);
        }
    }
    
    /**
     * Parses the configuration file path from command-line arguments.
     * Supports --config CONFIG_FILE format.
     * @param args Command-line arguments
     * @return The configuration file path, or "server.properties" if not specified
     */
    private static String parseConfigFile(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--config") || args[i].equals("-c")) {
                if (i + 1 < args.length) {
                    return args[i + 1];
                }
            }
        }
        return "server.properties";
    }
    
    /**
     * Parses the port number from command-line arguments.
     * Supports --port PORT format.
     * @param args Command-line arguments
     * @return The port number, or DEFAULT_PORT if not specified
     */
    private static int parsePort(String[] args) {
        int port = DEFAULT_PORT;
        
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--port") || args[i].equals("-p")) {
                if (i + 1 < args.length) {
                    try {
                        port = Integer.parseInt(args[i + 1]);
                        if (port < 1 || port > 65535) {
                            System.err.println("Invalid port number: " + port);
                            System.err.println("Port must be between 1 and 65535. Using default: " + DEFAULT_PORT);
                            port = DEFAULT_PORT;
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid port format: " + args[i + 1]);
                        System.err.println("Using default port: " + DEFAULT_PORT);
                    }
                }
            } else if (args[i].equals("--help") || args[i].equals("-h")) {
                printUsage();
                System.exit(0);
            }
        }
        
        return port;
    }
    
    /**
     * Sets up a shutdown hook to gracefully stop the server when the JVM terminates.
     * This ensures all clients are properly disconnected and resources are cleaned up.
     * @param server The GameServer instance to shutdown
     * @param logger The ServerLogger instance to close
     * @param monitor The ServerMonitor instance to stop
     */
    private static void setupShutdownHook(GameServer server, ServerLogger logger, ServerMonitor monitor) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println();
            System.out.println("===========================================");
            System.out.println("Shutdown signal received...");
            System.out.println("Stopping server gracefully...");
            
            logger.logInfo("Shutdown signal received");
            
            // Stop monitor first
            if (monitor != null) {
                monitor.stop();
            }
            
            // Stop server
            if (server.isRunning()) {
                server.stop();
            }
            
            logger.logInfo("Server stopped successfully");
            
            // Close logger
            if (logger != null) {
                logger.close();
            }
            
            System.out.println("Server stopped successfully.");
            System.out.println("===========================================");
        }, "ServerShutdownHook"));
    }
    
    /**
     * Parses the max clients from command-line arguments.
     * Supports --max-clients NUMBER format.
     * @param args Command-line arguments
     * @return The max clients number, or -1 if not specified
     */
    private static int parseMaxClients(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--max-clients")) {
                if (i + 1 < args.length) {
                    try {
                        int maxClients = Integer.parseInt(args[i + 1]);
                        if (maxClients < 1 || maxClients > 1000) {
                            System.err.println("Invalid max clients: " + maxClients);
                            System.err.println("Max clients must be between 1 and 1000. Using config value.");
                            return -1;
                        }
                        return maxClients;
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid max clients format: " + args[i + 1]);
                        System.err.println("Using config value.");
                        return -1;
                    }
                }
            }
        }
        return -1;
    }
    
    /**
     * Parses the world seed from command-line arguments.
     * Supports --seed NUMBER format.
     * @param args Command-line arguments
     * @return The world seed, or -1 if not specified
     */
    private static long parseSeed(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--seed")) {
                if (i + 1 < args.length) {
                    try {
                        return Long.parseLong(args[i + 1]);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid seed format: " + args[i + 1]);
                        System.err.println("Using config value.");
                        return -1;
                    }
                }
            }
        }
        return -1;
    }
    
    /**
     * Parses the debug flag from command-line arguments.
     * Supports --debug format.
     * @param args Command-line arguments
     * @return true if debug flag is present, false otherwise
     */
    private static boolean parseDebug(String[] args) {
        for (String arg : args) {
            if (arg.equals("--debug")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Prints usage information for the dedicated server.
     */
    private static void printUsage() {
        System.out.println("Usage: java -jar woodlanders-server.jar [OPTIONS]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --port, -p PORT       Specify the server port (default: 25565)");
        System.out.println("  --max-clients NUMBER  Specify maximum concurrent clients (default: 20)");
        System.out.println("  --seed NUMBER         Specify world seed (default: 0 for random)");
        System.out.println("  --debug               Enable debug logging");
        System.out.println("  --config, -c FILE     Specify the configuration file (default: server.properties)");
        System.out.println("  --help, -h            Display this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar woodlanders-server.jar");
        System.out.println("  java -jar woodlanders-server.jar --port 30000");
        System.out.println("  java -jar woodlanders-server.jar --port 25565 --max-clients 50");
        System.out.println("  java -jar woodlanders-server.jar --seed 123456789 --debug");
        System.out.println("  java -jar woodlanders-server.jar --config custom.properties");
    }
}
