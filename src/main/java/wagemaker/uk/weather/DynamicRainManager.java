package wagemaker.uk.weather;

import java.util.Random;

/**
 * Manages dynamic rain events that appear randomly near the player.
 * Rain events last for 120 seconds and occur randomly between 300-1200 seconds apart.
 */
public class DynamicRainManager {
    private static final float RAIN_DURATION = 120f; // 120 seconds
    private static final float MIN_INTERVAL = 120f; // 2 minute 
    private static final float MAX_INTERVAL = 500f; // 3 minutes
    private static final float RAIN_RADIUS = 800f; // Rain zone radius
    private static final float RAIN_INTENSITY = 1.0f; // Full intensity
    
    private RainZoneManager zoneManager;
    private Random random;
    
    private boolean isRaining;
    private float rainTimer;
    private float nextRainIn;
    private String currentRainZoneId;
    
    private float statusLogTimer = 0f;
    private static final float STATUS_LOG_INTERVAL = 30f; // Log status every 30 seconds
    
    /**
     * Creates a new DynamicRainManager.
     * 
     * @param zoneManager The RainZoneManager to control
     */
    public DynamicRainManager(RainZoneManager zoneManager) {
        this.zoneManager = zoneManager;
        this.random = new Random();
        this.isRaining = false;
        this.rainTimer = 0f;
        this.nextRainIn = getRandomInterval();
        this.currentRainZoneId = "dynamic_rain";
        this.statusLogTimer = 0f;
        
        System.out.println("DynamicRainManager initialized. First rain in " + (int)nextRainIn + " seconds");
    }
    
    /**
     * Updates the dynamic rain system.
     * 
     * @param deltaTime Time elapsed since last update in seconds
     * @param playerX Player's X coordinate
     * @param playerY Player's Y coordinate
     */
    public void update(float deltaTime, float playerX, float playerY) {
        // Periodic status logging
        statusLogTimer += deltaTime;
        if (statusLogTimer >= STATUS_LOG_INTERVAL) {
            statusLogTimer = 0f;
            if (isRaining) {
                System.out.println("[Rain] Currently raining. Time remaining: " + (int)rainTimer + "s");
            } else {
                System.out.println("[Rain] No rain. Next rain in: " + (int)nextRainIn + "s");
            }
        }
        
        if (isRaining) {
            // Rain is active, count down duration
            rainTimer -= deltaTime;
            
            // Update rain zone position to follow player
            updateRainZonePosition(playerX, playerY);
            
            if (rainTimer <= 0) {
                // Rain duration ended, stop rain
                stopRain();
                nextRainIn = getRandomInterval();
            }
        } else {
            // No rain, count down to next rain event
            nextRainIn -= deltaTime;
            
            if (nextRainIn <= 0) {
                // Time for rain!
                startRain(playerX, playerY);
            }
        }
    }
    
    /**
     * Starts a rain event at the player's position.
     */
    private void startRain(float playerX, float playerY) {
        isRaining = true;
        rainTimer = RAIN_DURATION;
        
        // Create rain zone near player
        RainZone rainZone = new RainZone(
            currentRainZoneId,
            playerX,
            playerY,
            RAIN_RADIUS,
            200f, // Fade distance
            RAIN_INTENSITY
        );
        
        // Clear existing zones and add new one
        zoneManager.clearAllZones();
        zoneManager.addRainZone(rainZone);
        
        System.out.println("Dynamic rain started at (" + playerX + ", " + playerY + ") for " + RAIN_DURATION + " seconds");
    }
    
    /**
     * Stops the current rain event.
     */
    private void stopRain() {
        isRaining = false;
        rainTimer = 0f;
        
        // Remove rain zone
        zoneManager.clearAllZones();
        
        System.out.println("Dynamic rain stopped. Next rain in " + (int)nextRainIn + " seconds");
    }
    
    /**
     * Updates the rain zone position to follow the player.
     */
    private void updateRainZonePosition(float playerX, float playerY) {
        // Get current rain zone
        RainZone currentZone = zoneManager.getRainZone(currentRainZoneId);
        
        if (currentZone != null) {
            // Remove old zone
            zoneManager.removeRainZone(currentRainZoneId);
            
            // Create new zone at player position
            RainZone newZone = new RainZone(
                currentRainZoneId,
                playerX,
                playerY,
                RAIN_RADIUS,
                200f,
                RAIN_INTENSITY
            );
            
            zoneManager.addRainZone(newZone);
        }
    }
    
    /**
     * Gets a random interval between MIN_INTERVAL and MAX_INTERVAL.
     */
    private float getRandomInterval() {
        return MIN_INTERVAL + random.nextFloat() * (MAX_INTERVAL - MIN_INTERVAL);
    }
    
    /**
     * Checks if it's currently raining.
     */
    public boolean isRaining() {
        return isRaining;
    }
    
    /**
     * Gets the time remaining until next rain event (in seconds).
     */
    public float getTimeUntilNextRain() {
        return isRaining ? 0 : nextRainIn;
    }
    
    /**
     * Gets the time remaining for current rain event (in seconds).
     */
    public float getRainTimeRemaining() {
        return isRaining ? rainTimer : 0;
    }
    
    /**
     * Forces rain to start immediately (for testing/debugging).
     */
    public void forceRain(float playerX, float playerY) {
        if (!isRaining) {
            startRain(playerX, playerY);
        }
    }
    
    /**
     * Forces rain to stop immediately (for testing/debugging).
     */
    public void forceStopRain() {
        if (isRaining) {
            stopRain();
            nextRainIn = getRandomInterval();
        }
    }
}
