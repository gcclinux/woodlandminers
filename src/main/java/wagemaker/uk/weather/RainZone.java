package wagemaker.uk.weather;

import java.io.Serializable;

/**
 * Represents a geographical area where rain occurs.
 * Serializable for network transmission in multiplayer mode.
 */
public class RainZone implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String zoneId;        // Unique identifier
    private float centerX;        // World X coordinate of zone center
    private float centerY;        // World Y coordinate of zone center
    private float radius;         // Radius in pixels (full intensity)
    private float fadeDistance;   // Distance over which rain fades (pixels)
    private float intensity;      // Rain intensity 0.0-1.0 (affects particle count)
    
    /**
     * Creates a new rain zone.
     * 
     * @param zoneId Unique identifier for this zone
     * @param centerX World X coordinate of zone center
     * @param centerY World Y coordinate of zone center
     * @param radius Full intensity radius in pixels
     * @param fadeDistance Distance over which rain fades out (pixels)
     * @param intensity Maximum rain intensity (0.0-1.0)
     */
    public RainZone(String zoneId, float centerX, float centerY, float radius, float fadeDistance, float intensity) {
        this.zoneId = zoneId;
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.fadeDistance = fadeDistance;
        this.intensity = intensity;
    }
    
    /**
     * Calculates the Euclidean distance from a point to the center of this rain zone.
     * 
     * @param x X coordinate of the point
     * @param y Y coordinate of the point
     * @return Distance in pixels from the point to the zone center
     */
    public float getDistanceFrom(float x, float y) {
        float dx = x - centerX;
        float dy = y - centerY;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Calculates the rain intensity at a given point based on distance from zone center.
     * Returns full intensity within the radius, fades linearly in the fade zone, and 0 beyond.
     * 
     * @param x X coordinate of the point
     * @param y Y coordinate of the point
     * @return Intensity value between 0.0 and 1.0
     */
    public float getIntensityAt(float x, float y) {
        float distance = getDistanceFrom(x, y);
        
        // Full intensity within the radius
        if (distance <= radius) {
            return intensity;
        }
        
        // Fade zone: linear fade from full intensity to 0
        float fadeStart = radius;
        float fadeEnd = radius + fadeDistance;
        
        if (distance <= fadeEnd) {
            // Calculate fade factor (1.0 at fadeStart, 0.0 at fadeEnd)
            float fadeFactor = 1.0f - ((distance - fadeStart) / fadeDistance);
            return intensity * fadeFactor;
        }
        
        // Beyond fade zone: no rain
        return 0.0f;
    }
    
    /**
     * Validates that this rain zone has valid properties.
     * 
     * @return true if the zone is valid, false otherwise
     */
    public boolean isValid() {
        return zoneId != null && !zoneId.isEmpty()
            && radius > 0
            && fadeDistance >= 0
            && intensity >= 0.0f && intensity <= 1.0f;
    }
    
    // Getters
    
    public String getZoneId() {
        return zoneId;
    }
    
    public float getCenterX() {
        return centerX;
    }
    
    public float getCenterY() {
        return centerY;
    }
    
    public float getRadius() {
        return radius;
    }
    
    public float getFadeDistance() {
        return fadeDistance;
    }
    
    public float getIntensity() {
        return intensity;
    }
    
    // Setters
    
    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }
    
    public void setCenterX(float centerX) {
        this.centerX = centerX;
    }
    
    public void setCenterY(float centerY) {
        this.centerY = centerY;
    }
    
    public void setRadius(float radius) {
        this.radius = radius;
    }
    
    public void setFadeDistance(float fadeDistance) {
        this.fadeDistance = fadeDistance;
    }
    
    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }
    
    @Override
    public String toString() {
        return String.format("RainZone[id=%s, center=(%.1f,%.1f), radius=%.1f, fade=%.1f, intensity=%.2f]",
            zoneId, centerX, centerY, radius, fadeDistance, intensity);
    }
}
