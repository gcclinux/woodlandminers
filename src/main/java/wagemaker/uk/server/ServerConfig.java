package wagemaker.uk.server;

import java.io.*;
import java.util.Properties;

/**
 * ServerConfig manages server configuration loaded from a properties file.
 * It provides default values and validates configuration parameters.
 */
public class ServerConfig {
    private static final String DEFAULT_CONFIG_FILE = "server.properties";
    
    // Default values
    private static final int DEFAULT_PORT = 25565;
    private static final int DEFAULT_MAX_CLIENTS = 20;
    private static final long DEFAULT_WORLD_SEED = 0; // 0 means random
    private static final int DEFAULT_HEARTBEAT_INTERVAL = 5;
    private static final int DEFAULT_CLIENT_TIMEOUT = 15;
    private static final int DEFAULT_RATE_LIMIT = 100;
    private static final boolean DEFAULT_DEBUG = false;
    
    // Planting range configuration
    private static final int DEFAULT_PLANTING_RANGE = 512;
    private static final int MIN_PLANTING_RANGE = 64;
    private static final int MAX_PLANTING_RANGE = 1024;
    
    // Configuration properties
    private int port;
    private int maxClients;
    private long worldSeed;
    private int heartbeatInterval;
    private int clientTimeout;
    private int rateLimit;
    private boolean debug;
    private int plantingMaxRange;
    
    /**
     * Creates a ServerConfig with default values.
     */
    public ServerConfig() {
        this.port = DEFAULT_PORT;
        this.maxClients = DEFAULT_MAX_CLIENTS;
        this.worldSeed = DEFAULT_WORLD_SEED;
        this.heartbeatInterval = DEFAULT_HEARTBEAT_INTERVAL;
        this.clientTimeout = DEFAULT_CLIENT_TIMEOUT;
        this.rateLimit = DEFAULT_RATE_LIMIT;
        this.debug = DEFAULT_DEBUG;
        this.plantingMaxRange = DEFAULT_PLANTING_RANGE;
    }
    
    /**
     * Loads configuration from the default server.properties file.
     * If the file doesn't exist, uses default values and creates a template file.
     * @return A ServerConfig instance with loaded or default values
     */
    public static ServerConfig load() {
        return load(DEFAULT_CONFIG_FILE);
    }
    
    /**
     * Loads configuration from the specified properties file.
     * If the file doesn't exist, uses default values and creates a template file.
     * @param configFile The path to the configuration file
     * @return A ServerConfig instance with loaded or default values
     */
    public static ServerConfig load(String configFile) {
        ServerConfig config = new ServerConfig();
        File file = new File(configFile);
        
        if (!file.exists()) {
            System.out.println("Configuration file not found: " + configFile);
            System.out.println("Creating default configuration file...");
            config.createDefaultConfigFile(configFile);
            System.out.println("Using default configuration values.");
            return config;
        }
        
        try (FileInputStream fis = new FileInputStream(file)) {
            Properties props = new Properties();
            props.load(fis);
            
            config.port = parseIntProperty(props, "server.port", DEFAULT_PORT, 1, 65535);
            config.maxClients = parseIntProperty(props, "server.max-clients", DEFAULT_MAX_CLIENTS, 1, 1000);
            config.worldSeed = parseLongProperty(props, "world.seed", DEFAULT_WORLD_SEED);
            config.heartbeatInterval = parseIntProperty(props, "server.heartbeat-interval", DEFAULT_HEARTBEAT_INTERVAL, 1, 60);
            config.clientTimeout = parseIntProperty(props, "server.client-timeout", DEFAULT_CLIENT_TIMEOUT, 5, 300);
            config.rateLimit = parseIntProperty(props, "server.rate-limit", DEFAULT_RATE_LIMIT, 10, 10000);
            config.debug = parseBooleanProperty(props, "server.debug", DEFAULT_DEBUG);
            config.plantingMaxRange = parseIntProperty(props, "planting.max.range", DEFAULT_PLANTING_RANGE, MIN_PLANTING_RANGE, MAX_PLANTING_RANGE);
            
            System.out.println("Configuration loaded from: " + configFile);
            config.logPlantingRangeConfig();
            
        } catch (IOException e) {
            System.err.println("Error loading configuration file: " + e.getMessage());
            System.out.println("Using default configuration values.");
        }
        
        return config;
    }
    
    /**
     * Creates a default configuration file with comments explaining each property.
     * @param configFile The path to create the configuration file
     */
    private void createDefaultConfigFile(String configFile) {
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write("# Woodlanders Dedicated Server Configuration\n");
            writer.write("# This file contains server settings for the multiplayer game.\n");
            writer.write("\n");
            writer.write("# Server port (1-65535)\n");
            writer.write("# Default: 25565\n");
            writer.write("server.port=" + DEFAULT_PORT + "\n");
            writer.write("\n");
            writer.write("# Maximum number of concurrent clients (1-1000)\n");
            writer.write("# Default: 20\n");
            writer.write("server.max-clients=" + DEFAULT_MAX_CLIENTS + "\n");
            writer.write("\n");
            writer.write("# World seed for deterministic world generation\n");
            writer.write("# Set to 0 for random seed generation\n");
            writer.write("# Default: 0\n");
            writer.write("world.seed=" + DEFAULT_WORLD_SEED + "\n");
            writer.write("\n");
            writer.write("# Heartbeat interval in seconds (1-60)\n");
            writer.write("# How often clients send heartbeat messages\n");
            writer.write("# Default: 5\n");
            writer.write("server.heartbeat-interval=" + DEFAULT_HEARTBEAT_INTERVAL + "\n");
            writer.write("\n");
            writer.write("# Client timeout in seconds (5-300)\n");
            writer.write("# Disconnect clients that don't send heartbeat within this time\n");
            writer.write("# Default: 15\n");
            writer.write("server.client-timeout=" + DEFAULT_CLIENT_TIMEOUT + "\n");
            writer.write("\n");
            writer.write("# Message rate limit per client (messages/second) (10-10000)\n");
            writer.write("# Clients exceeding this rate will be disconnected\n");
            writer.write("# Default: 100\n");
            writer.write("server.rate-limit=" + DEFAULT_RATE_LIMIT + "\n");
            writer.write("\n");
            writer.write("# Enable debug logging (true/false)\n");
            writer.write("# Default: false\n");
            writer.write("server.debug=" + DEFAULT_DEBUG + "\n");
            writer.write("\n");
            writer.write("# Planting Range Configuration (in pixels)\n");
            writer.write("# Maximum distance a player can plant from their position\n");
            writer.write("# Default: 512 (8 tiles at 64px per tile)\n");
            writer.write("# Range: 64-1024 (1-16 tiles)\n");
            writer.write("planting.max.range=" + DEFAULT_PLANTING_RANGE + "\n");
            
            System.out.println("Created default configuration file: " + configFile);
            
        } catch (IOException e) {
            System.err.println("Error creating default configuration file: " + e.getMessage());
        }
    }
    
    /**
     * Parses an integer property with validation.
     * @param props The properties object
     * @param key The property key
     * @param defaultValue The default value if not found or invalid
     * @param min Minimum allowed value
     * @param max Maximum allowed value
     * @return The parsed and validated integer value
     */
    private static int parseIntProperty(Properties props, String key, int defaultValue, int min, int max) {
        String value = props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            int intValue = Integer.parseInt(value.trim());
            if (intValue < min || intValue > max) {
                System.err.println("Property " + key + " out of range [" + min + "-" + max + "]: " + intValue);
                System.err.println("Using default value: " + defaultValue);
                return defaultValue;
            }
            return intValue;
        } catch (NumberFormatException e) {
            System.err.println("Invalid integer value for " + key + ": " + value);
            System.err.println("Using default value: " + defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Parses a long property.
     * @param props The properties object
     * @param key The property key
     * @param defaultValue The default value if not found or invalid
     * @return The parsed long value
     */
    private static long parseLongProperty(Properties props, String key, long defaultValue) {
        String value = props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid long value for " + key + ": " + value);
            System.err.println("Using default value: " + defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Parses a boolean property.
     * @param props The properties object
     * @param key The property key
     * @param defaultValue The default value if not found or invalid
     * @return The parsed boolean value
     */
    private static boolean parseBooleanProperty(Properties props, String key, boolean defaultValue) {
        String value = props.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        return Boolean.parseBoolean(value.trim());
    }
    
    // Getters
    
    public int getPort() {
        return port;
    }
    
    public int getMaxClients() {
        return maxClients;
    }
    
    public long getWorldSeed() {
        return worldSeed;
    }
    
    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }
    
    public int getClientTimeout() {
        return clientTimeout;
    }
    
    public int getRateLimit() {
        return rateLimit;
    }
    
    public boolean isDebug() {
        return debug;
    }
    
    public int getPlantingMaxRange() {
        return plantingMaxRange;
    }
    
    /**
     * Logs the active planting range configuration in both pixels and tiles.
     */
    private void logPlantingRangeConfig() {
        int tiles = plantingMaxRange / 64;
        System.out.println("Planting Range Configuration:");
        System.out.println("  Max Range: " + plantingMaxRange + " pixels (" + tiles + " tiles)");
    }
    
    /**
     * Prints the current configuration to the console.
     */
    public void printConfig() {
        System.out.println("Server Configuration:");
        System.out.println("  Port: " + port);
        System.out.println("  Max Clients: " + maxClients);
        System.out.println("  World Seed: " + (worldSeed == 0 ? "Random" : worldSeed));
        System.out.println("  Heartbeat Interval: " + heartbeatInterval + "s");
        System.out.println("  Client Timeout: " + clientTimeout + "s");
        System.out.println("  Rate Limit: " + rateLimit + " msg/s");
        System.out.println("  Debug Mode: " + debug);
        System.out.println("  Planting Max Range: " + plantingMaxRange + " pixels (" + (plantingMaxRange / 64) + " tiles)");
    }
}
