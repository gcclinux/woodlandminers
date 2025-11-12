package wagemaker.uk.weather;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages rain zone definitions and determines active zones based on player position.
 * Handles zone lifecycle (add, remove, clear) and provides query methods to check
 * if a player is within rain zones and calculate combined rain intensity.
 */
public class RainZoneManager {
    private List<RainZone> rainZones;
    
    /**
     * Creates a new RainZoneManager with an empty zone list.
     */
    public RainZoneManager() {
        this.rainZones = new ArrayList<>();
    }
    
    /**
     * Adds a rain zone to the manager.
     * 
     * @param zone The rain zone to add
     */
    public void addRainZone(RainZone zone) {
        if (zone != null && zone.isValid()) {
            rainZones.add(zone);
        }
    }
    
    /**
     * Removes a rain zone by its unique identifier.
     * 
     * @param zoneId The unique identifier of the zone to remove
     * @return true if a zone was removed, false if no zone with that ID was found
     */
    public boolean removeRainZone(String zoneId) {
        if (zoneId == null) {
            return false;
        }
        
        return rainZones.removeIf(zone -> zoneId.equals(zone.getZoneId()));
    }
    
    /**
     * Removes all rain zones from the manager.
     */
    public void clearAllZones() {
        rainZones.clear();
    }
    
    /**
     * Sets the rain zones to a new list, replacing all existing zones.
     * This method is primarily used for multiplayer synchronization.
     * 
     * @param zones The new list of rain zones
     */
    public void setRainZones(List<RainZone> zones) {
        this.rainZones.clear();
        if (zones != null) {
            for (RainZone zone : zones) {
                if (zone != null && zone.isValid()) {
                    this.rainZones.add(zone);
                }
            }
        }
    }
    
    /**
     * Checks if a player position is within any rain zone.
     * A player is considered "in" a rain zone if they are within the zone's
     * radius plus fade distance.
     * 
     * @param playerX Player's X coordinate
     * @param playerY Player's Y coordinate
     * @return true if the player is in any rain zone, false otherwise
     */
    public boolean isInRainZone(float playerX, float playerY) {
        for (RainZone zone : rainZones) {
            float distance = zone.getDistanceFrom(playerX, playerY);
            float maxDistance = zone.getRadius() + zone.getFadeDistance();
            
            if (distance <= maxDistance) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Calculates the combined rain intensity at a given position.
     * When multiple rain zones overlap, their intensities are combined
     * (capped at maximum configured intensity).
     * 
     * @param playerX Player's X coordinate
     * @param playerY Player's Y coordinate
     * @return Combined rain intensity from 0.0 to configured maximum
     */
    public float getRainIntensityAt(float playerX, float playerY) {
        float totalIntensity = 0.0f;
        
        for (RainZone zone : rainZones) {
            float zoneIntensity = zone.getIntensityAt(playerX, playerY);
            totalIntensity += zoneIntensity;
        }
        
        // Cap at maximum intensity from configuration
        return Math.min(totalIntensity, RainConfig.MAX_COMBINED_INTENSITY);
    }
    
    /**
     * Finds the nearest rain zone to a given position.
     * 
     * @param playerX Player's X coordinate
     * @param playerY Player's Y coordinate
     * @return The nearest rain zone, or null if no zones exist
     */
    public RainZone getNearestRainZone(float playerX, float playerY) {
        if (rainZones.isEmpty()) {
            return null;
        }
        
        RainZone nearest = null;
        float minDistance = Float.MAX_VALUE;
        
        for (RainZone zone : rainZones) {
            float distance = zone.getDistanceFrom(playerX, playerY);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = zone;
            }
        }
        
        return nearest;
    }
    
    /**
     * Initializes default rain zones for single-player mode.
     * Creates a rain zone at the spawn area using configuration values.
     * This matches Requirement 2.1: spawn area rain zone at coordinates (128, 128) with 640px radius.
     */
    public void initializeDefaultZones() {
        clearAllZones();
        
        // Create spawn area rain zone using configuration constants
        RainZone spawnRain = new RainZone(
            RainConfig.SPAWN_ZONE_ID,           // Zone ID
            RainConfig.SPAWN_ZONE_CENTER_X,     // Center X
            RainConfig.SPAWN_ZONE_CENTER_Y,     // Center Y
            RainConfig.SPAWN_ZONE_RADIUS,       // Radius (full intensity)
            RainConfig.DEFAULT_FADE_DISTANCE,   // Fade distance
            RainConfig.DEFAULT_INTENSITY        // Maximum intensity
        );
        
        addRainZone(spawnRain);
    }
    
    /**
     * Gets the list of all rain zones.
     * 
     * @return A copy of the rain zones list
     */
    public List<RainZone> getRainZones() {
        return new ArrayList<>(rainZones);
    }
    
    /**
     * Gets a specific rain zone by its ID.
     * 
     * @param zoneId The unique identifier of the zone
     * @return The rain zone with the specified ID, or null if not found
     */
    public RainZone getRainZone(String zoneId) {
        if (zoneId == null) {
            return null;
        }
        
        for (RainZone zone : rainZones) {
            if (zoneId.equals(zone.getZoneId())) {
                return zone;
            }
        }
        
        return null;
    }
    
    /**
     * Gets the number of rain zones currently managed.
     * 
     * @return The number of rain zones
     */
    public int getZoneCount() {
        return rainZones.size();
    }
    
    @Override
    public String toString() {
        return String.format("RainZoneManager[zones=%d]", rainZones.size());
    }
}
