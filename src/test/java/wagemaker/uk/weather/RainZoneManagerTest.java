package wagemaker.uk.weather;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RainZoneManager zone management and queries.
 */
public class RainZoneManagerTest {
    
    private RainZoneManager manager;
    
    @BeforeEach
    public void setUp() {
        manager = new RainZoneManager();
    }
    
    @Test
    public void testAddRainZone() {
        RainZone zone = new RainZone("zone1", 100f, 100f, 200f, 50f, 1.0f);
        manager.addRainZone(zone);
        
        // Verify zone was added by checking if player is in zone
        assertTrue(manager.isInRainZone(100f, 100f), "Player at zone center should be in rain zone");
    }
    
    @Test
    public void testRemoveRainZone() {
        RainZone zone = new RainZone("zone1", 100f, 100f, 200f, 50f, 1.0f);
        manager.addRainZone(zone);
        
        // Verify zone exists
        assertTrue(manager.isInRainZone(100f, 100f), "Zone should exist before removal");
        
        // Remove zone
        manager.removeRainZone("zone1");
        
        // Verify zone was removed
        assertFalse(manager.isInRainZone(100f, 100f), "Zone should not exist after removal");
    }
    
    @Test
    public void testClearAllZones() {
        manager.addRainZone(new RainZone("zone1", 100f, 100f, 200f, 50f, 1.0f));
        manager.addRainZone(new RainZone("zone2", 500f, 500f, 200f, 50f, 1.0f));
        
        // Verify zones exist
        assertTrue(manager.isInRainZone(100f, 100f), "Zone1 should exist");
        assertTrue(manager.isInRainZone(500f, 500f), "Zone2 should exist");
        
        // Clear all zones
        manager.clearAllZones();
        
        // Verify all zones were removed
        assertFalse(manager.isInRainZone(100f, 100f), "Zone1 should be cleared");
        assertFalse(manager.isInRainZone(500f, 500f), "Zone2 should be cleared");
    }
    
    @Test
    public void testSetRainZones() {
        List<RainZone> zones = new ArrayList<>();
        zones.add(new RainZone("zone1", 100f, 100f, 200f, 50f, 1.0f));
        zones.add(new RainZone("zone2", 500f, 500f, 200f, 50f, 1.0f));
        
        manager.setRainZones(zones);
        
        // Verify zones were set
        assertTrue(manager.isInRainZone(100f, 100f), "Zone1 should be set");
        assertTrue(manager.isInRainZone(500f, 500f), "Zone2 should be set");
    }
    
    @Test
    public void testIsInRainZone_InsideZone() {
        manager.addRainZone(new RainZone("zone1", 128f, 128f, 640f, 100f, 1.0f));
        
        // Test point inside the zone
        assertTrue(manager.isInRainZone(128f, 128f), "Player at center should be in zone");
        assertTrue(manager.isInRainZone(400f, 128f), "Player within radius should be in zone");
    }
    
    @Test
    public void testIsInRainZone_OutsideZone() {
        manager.addRainZone(new RainZone("zone1", 128f, 128f, 640f, 100f, 1.0f));
        
        // Test point outside the zone (beyond radius + fade)
        assertFalse(manager.isInRainZone(1000f, 128f), "Player far from zone should not be in zone");
    }
    
    @Test
    public void testIsInRainZone_NoZones() {
        // Test with no zones added
        assertFalse(manager.isInRainZone(100f, 100f), "Should return false when no zones exist");
    }
    
    @Test
    public void testGetRainIntensityAt_InsideZone() {
        manager.addRainZone(new RainZone("zone1", 128f, 128f, 640f, 100f, 1.0f));
        
        // Test intensity at center
        float intensity = manager.getRainIntensityAt(128f, 128f);
        assertEquals(1.0f, intensity, 0.01f, "Intensity at center should be 1.0");
    }
    
    @Test
    public void testGetRainIntensityAt_InFadeZone() {
        manager.addRainZone(new RainZone("zone1", 128f, 128f, 640f, 100f, 1.0f));
        
        // Test intensity in fade zone (radius + 50 pixels)
        float intensity = manager.getRainIntensityAt(818f, 128f);
        assertEquals(0.5f, intensity, 0.01f, "Intensity in middle of fade zone should be 0.5");
    }
    
    @Test
    public void testGetRainIntensityAt_OutsideZone() {
        manager.addRainZone(new RainZone("zone1", 128f, 128f, 640f, 100f, 1.0f));
        
        // Test intensity outside zone
        float intensity = manager.getRainIntensityAt(1000f, 128f);
        assertEquals(0.0f, intensity, 0.01f, "Intensity outside zone should be 0");
    }
    
    @Test
    public void testGetRainIntensityAt_OverlappingZones() {
        // Create two overlapping zones
        manager.addRainZone(new RainZone("zone1", 100f, 100f, 150f, 50f, 0.6f));
        manager.addRainZone(new RainZone("zone2", 200f, 100f, 150f, 50f, 0.8f));
        
        // Test at a point that overlaps both zones (150, 100)
        float intensity = manager.getRainIntensityAt(150f, 100f);
        
        // Should get combined intensity (capped at 1.0)
        assertTrue(intensity > 0.6f, "Overlapping zones should combine intensities");
        assertTrue(intensity <= 1.0f, "Combined intensity should not exceed 1.0");
    }
    
    @Test
    public void testGetRainIntensityAt_NoZones() {
        // Test with no zones
        float intensity = manager.getRainIntensityAt(100f, 100f);
        assertEquals(0.0f, intensity, 0.01f, "Intensity should be 0 when no zones exist");
    }
    
    @Test
    public void testInitializeDefaultZones() {
        manager.initializeDefaultZones();
        
        // Verify default spawn zone was created at (128, 128)
        assertTrue(manager.isInRainZone(128f, 128f), "Default spawn zone should exist at (128, 128)");
        
        // Verify intensity at spawn
        float intensity = manager.getRainIntensityAt(128f, 128f);
        assertEquals(1.0f, intensity, 0.01f, "Default zone should have full intensity at center");
    }
    
    @Test
    public void testMultipleZones() {
        // Add multiple zones
        manager.addRainZone(new RainZone("zone1", 100f, 100f, 100f, 50f, 1.0f));
        manager.addRainZone(new RainZone("zone2", 500f, 500f, 100f, 50f, 1.0f));
        manager.addRainZone(new RainZone("zone3", 1000f, 1000f, 100f, 50f, 1.0f));
        
        // Verify each zone works independently
        assertTrue(manager.isInRainZone(100f, 100f), "Zone1 should be active");
        assertTrue(manager.isInRainZone(500f, 500f), "Zone2 should be active");
        assertTrue(manager.isInRainZone(1000f, 1000f), "Zone3 should be active");
        
        // Verify points between zones have no rain
        assertFalse(manager.isInRainZone(300f, 300f), "Point between zones should have no rain");
    }
}
