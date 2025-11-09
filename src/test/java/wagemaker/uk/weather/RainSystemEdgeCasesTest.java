package wagemaker.uk.weather;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for edge cases in the rain system.
 * Tests boundary conditions, overlapping zones, and special scenarios.
 */
public class RainSystemEdgeCasesTest {
    
    @Test
    public void testRainZone_ExactlyAtRadius() {
        // Test behavior exactly at the radius boundary
        RainZone zone = new RainZone("test", 100f, 100f, 200f, 50f, 1.0f);
        
        // Point exactly at radius (200 pixels away)
        float intensity = zone.getIntensityAt(300f, 100f);
        assertEquals(1.0f, intensity, 0.01f, "Intensity exactly at radius should be full (1.0)");
        
        // Just inside radius
        float intensityInside = zone.getIntensityAt(299f, 100f);
        assertEquals(1.0f, intensityInside, 0.01f, "Intensity just inside radius should be full");
        
        // Just outside radius (start of fade zone)
        float intensityOutside = zone.getIntensityAt(301f, 100f);
        assertTrue(intensityOutside < 1.0f, "Intensity just outside radius should start fading");
        assertTrue(intensityOutside > 0.9f, "Intensity just outside radius should still be high");
    }
    
    @Test
    public void testRainZone_ExactlyAtFadeBoundary() {
        // Test behavior exactly at the fade boundary
        RainZone zone = new RainZone("test", 100f, 100f, 200f, 50f, 1.0f);
        
        // Point exactly at fade boundary (radius + fadeDistance = 250 pixels away)
        float intensity = zone.getIntensityAt(350f, 100f);
        assertEquals(0.0f, intensity, 0.01f, "Intensity exactly at fade boundary should be 0");
        
        // Just inside fade boundary
        float intensityInside = zone.getIntensityAt(349f, 100f);
        assertTrue(intensityInside > 0.0f, "Intensity just inside fade boundary should be > 0");
        assertTrue(intensityInside < 0.1f, "Intensity just inside fade boundary should be very low");
        
        // Just outside fade boundary
        float intensityOutside = zone.getIntensityAt(351f, 100f);
        assertEquals(0.0f, intensityOutside, 0.01f, "Intensity just outside fade boundary should be 0");
    }
    
    @Test
    public void testRainZone_ZeroFadeDistance() {
        // Test zone with no fade (instant cutoff)
        RainZone zone = new RainZone("test", 100f, 100f, 200f, 0f, 1.0f);
        
        // Inside radius
        assertEquals(1.0f, zone.getIntensityAt(200f, 100f), 0.01f, 
                     "Intensity inside radius should be full");
        
        // At radius boundary
        assertEquals(1.0f, zone.getIntensityAt(300f, 100f), 0.01f,
                     "Intensity at radius should be full with zero fade");
        
        // Just outside radius
        assertEquals(0.0f, zone.getIntensityAt(301f, 100f), 0.01f,
                     "Intensity outside radius should be 0 with zero fade");
    }
    
    @Test
    public void testRainZoneManager_OverlappingZones_SameLocation() {
        // Test two zones at the exact same location
        RainZoneManager manager = new RainZoneManager();
        manager.addRainZone(new RainZone("zone1", 100f, 100f, 150f, 50f, 0.5f));
        manager.addRainZone(new RainZone("zone2", 100f, 100f, 150f, 50f, 0.5f));
        
        // At center, should get combined intensity (0.5 + 0.5 = 1.0)
        float intensity = manager.getRainIntensityAt(100f, 100f);
        assertEquals(1.0f, intensity, 0.01f, "Overlapping zones should combine intensities");
    }
    
    @Test
    public void testRainZoneManager_OverlappingZones_PartialOverlap() {
        // Test zones that partially overlap
        RainZoneManager manager = new RainZoneManager();
        manager.addRainZone(new RainZone("zone1", 100f, 100f, 100f, 50f, 0.6f));
        manager.addRainZone(new RainZone("zone2", 200f, 100f, 100f, 50f, 0.7f));
        
        // At point (150, 100) - halfway between centers
        float intensity = manager.getRainIntensityAt(150f, 100f);
        
        // Both zones should contribute
        assertTrue(intensity > 0.6f, "Overlapping zones should combine");
        assertTrue(intensity <= 1.0f, "Combined intensity should not exceed 1.0");
    }
    
    @Test
    public void testRainZoneManager_OverlappingZones_ExceedsMaxIntensity() {
        // Test that combined intensity is capped at 1.0
        RainZoneManager manager = new RainZoneManager();
        manager.addRainZone(new RainZone("zone1", 100f, 100f, 150f, 50f, 0.8f));
        manager.addRainZone(new RainZone("zone2", 100f, 100f, 150f, 50f, 0.8f));
        
        // At center, combined would be 1.6, but should be capped at 1.0
        float intensity = manager.getRainIntensityAt(100f, 100f);
        assertEquals(1.0f, intensity, 0.01f, "Combined intensity should be capped at 1.0");
    }
    
    @Test
    public void testRainZoneManager_ThreeOverlappingZones() {
        // Test three zones overlapping at one point
        RainZoneManager manager = new RainZoneManager();
        manager.addRainZone(new RainZone("zone1", 100f, 100f, 100f, 50f, 0.4f));
        manager.addRainZone(new RainZone("zone2", 100f, 100f, 100f, 50f, 0.4f));
        manager.addRainZone(new RainZone("zone3", 100f, 100f, 100f, 50f, 0.4f));
        
        // At center, combined would be 1.2, should be capped at 1.0
        float intensity = manager.getRainIntensityAt(100f, 100f);
        assertEquals(1.0f, intensity, 0.01f, "Three overlapping zones should be capped at 1.0");
    }
    
    @Test
    public void testRainZone_VerySmallRadius() {
        // Test zone with very small radius (1 pixel)
        RainZone zone = new RainZone("tiny", 100f, 100f, 1f, 1f, 1.0f);
        
        assertTrue(zone.isValid(), "Zone with 1 pixel radius should be valid");
        assertEquals(1.0f, zone.getIntensityAt(100f, 100f), 0.01f, 
                     "Center should have full intensity");
        assertEquals(0.0f, zone.getIntensityAt(103f, 100f), 0.01f,
                     "Point 3 pixels away should have no intensity");
    }
    
    @Test
    public void testRainZone_VeryLargeRadius() {
        // Test zone with very large radius
        RainZone zone = new RainZone("huge", 0f, 0f, 10000f, 1000f, 1.0f);
        
        assertTrue(zone.isValid(), "Zone with large radius should be valid");
        assertEquals(1.0f, zone.getIntensityAt(5000f, 0f), 0.01f,
                     "Point within large radius should have full intensity");
    }
    
    @Test
    public void testRainZone_NegativeCoordinates() {
        // Test zone with negative center coordinates
        RainZone zone = new RainZone("negative", -100f, -100f, 200f, 50f, 1.0f);
        
        assertTrue(zone.isValid(), "Zone with negative coordinates should be valid");
        assertEquals(1.0f, zone.getIntensityAt(-100f, -100f), 0.01f,
                     "Center at negative coordinates should work");
        assertEquals(1.0f, zone.getIntensityAt(0f, -100f), 0.01f,
                     "Point within radius should have full intensity");
    }
    
    @Test
    public void testRainParticle_ZeroVelocity() {
        // Test particle with zero velocity
        RainParticle particle = new RainParticle();
        particle.reset(100f, 200f, 0f, 12f);
        
        particle.update(1.0f);
        
        // Position should not change with zero velocity
        assertEquals(200f, particle.getY(), 0.01f, "Particle with zero velocity should not move");
    }
    
    @Test
    public void testRainParticle_VeryHighVelocity() {
        // Test particle with very high velocity
        RainParticle particle = new RainParticle();
        particle.reset(100f, 1000f, 10000f, 12f);
        
        particle.update(0.1f);
        
        // Should move 1000 pixels in 0.1 seconds
        assertEquals(0f, particle.getY(), 0.01f, "Particle with high velocity should move far");
    }
    
    @Test
    public void testRainParticle_NegativePosition() {
        // Test particle at negative position
        RainParticle particle = new RainParticle();
        particle.reset(-100f, -200f, 500f, 12f);
        
        assertEquals(-100f, particle.getX(), 0.01f, "Particle should support negative X");
        assertEquals(-200f, particle.getY(), 0.01f, "Particle should support negative Y");
        
        particle.update(0.1f);
        assertEquals(-250f, particle.getY(), 0.01f, "Particle should update from negative position");
    }
    
    @Test
    public void testRainZoneManager_EmptyZoneList() {
        // Test manager with no zones
        RainZoneManager manager = new RainZoneManager();
        
        assertFalse(manager.isInRainZone(0f, 0f), "Empty manager should return false");
        assertEquals(0.0f, manager.getRainIntensityAt(0f, 0f), 0.01f,
                     "Empty manager should return 0 intensity");
    }
    
    @Test
    public void testRainZoneManager_NullZoneList() {
        // Test setting null zone list
        RainZoneManager manager = new RainZoneManager();
        manager.addRainZone(new RainZone("zone1", 100f, 100f, 100f, 50f, 1.0f));
        
        // Set null list (should clear zones)
        manager.setRainZones(null);
        
        assertFalse(manager.isInRainZone(100f, 100f), "Null zone list should clear zones");
    }
}
