package wagemaker.uk.weather;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RainZone distance and intensity calculations.
 */
public class RainZoneTest {
    
    private RainZone zone;
    
    @BeforeEach
    public void setUp() {
        // Create a test rain zone centered at (128, 128) with radius 640 and fade distance 100
        zone = new RainZone("test_zone", 128f, 128f, 640f, 100f, 1.0f);
    }
    
    @Test
    public void testDistanceCalculation_AtCenter() {
        // Test distance at the exact center of the zone
        float distance = zone.getDistanceFrom(128f, 128f);
        assertEquals(0f, distance, 0.01f, "Distance at center should be 0");
    }
    
    @Test
    public void testDistanceCalculation_OnRadius() {
        // Test distance at exactly the radius boundary (640 pixels to the right)
        float distance = zone.getDistanceFrom(768f, 128f);
        assertEquals(640f, distance, 0.01f, "Distance at radius boundary should equal radius");
    }
    
    @Test
    public void testDistanceCalculation_Diagonal() {
        // Test Euclidean distance calculation with diagonal movement
        // Moving 300 pixels right and 400 pixels up should give distance of 500 (3-4-5 triangle)
        float distance = zone.getDistanceFrom(428f, 528f);
        assertEquals(500f, distance, 0.01f, "Diagonal distance should use Euclidean formula");
    }
    
    @Test
    public void testIntensityCalculation_AtCenter() {
        // Test full intensity at center
        float intensity = zone.getIntensityAt(128f, 128f);
        assertEquals(1.0f, intensity, 0.01f, "Intensity at center should be maximum (1.0)");
    }
    
    @Test
    public void testIntensityCalculation_WithinRadius() {
        // Test full intensity within the radius
        float intensity = zone.getIntensityAt(400f, 128f);
        assertEquals(1.0f, intensity, 0.01f, "Intensity within radius should be maximum (1.0)");
    }
    
    @Test
    public void testIntensityCalculation_ExactlyAtRadius() {
        // Test intensity exactly at the radius boundary
        float intensity = zone.getIntensityAt(768f, 128f);
        assertEquals(1.0f, intensity, 0.01f, "Intensity at radius boundary should be maximum (1.0)");
    }
    
    @Test
    public void testIntensityCalculation_InFadeZone() {
        // Test intensity in the middle of the fade zone (50 pixels into fade)
        // At radius + 50, should be 50% intensity
        float intensity = zone.getIntensityAt(818f, 128f);
        assertEquals(0.5f, intensity, 0.01f, "Intensity at middle of fade zone should be 0.5");
    }
    
    @Test
    public void testIntensityCalculation_AtFadeBoundary() {
        // Test intensity exactly at the fade boundary (radius + fadeDistance)
        float intensity = zone.getIntensityAt(868f, 128f);
        assertEquals(0.0f, intensity, 0.01f, "Intensity at fade boundary should be 0");
    }
    
    @Test
    public void testIntensityCalculation_BeyondFadeZone() {
        // Test intensity beyond the fade zone
        float intensity = zone.getIntensityAt(1000f, 128f);
        assertEquals(0.0f, intensity, 0.01f, "Intensity beyond fade zone should be 0");
    }
    
    @Test
    public void testIntensityCalculation_WithPartialIntensity() {
        // Test with a zone that has 0.8 max intensity
        RainZone partialZone = new RainZone("partial", 0f, 0f, 100f, 50f, 0.8f);
        
        // At center, should get 0.8
        assertEquals(0.8f, partialZone.getIntensityAt(0f, 0f), 0.01f, 
                     "Intensity at center should match zone's max intensity");
        
        // At middle of fade zone, should get 0.4 (50% of 0.8)
        assertEquals(0.4f, partialZone.getIntensityAt(125f, 0f), 0.01f,
                     "Intensity in fade zone should scale with zone's max intensity");
    }
    
    @Test
    public void testValidation_ValidZone() {
        assertTrue(zone.isValid(), "Zone with valid properties should pass validation");
    }
    
    @Test
    public void testValidation_NullZoneId() {
        RainZone invalidZone = new RainZone(null, 0f, 0f, 100f, 50f, 1.0f);
        assertFalse(invalidZone.isValid(), "Zone with null ID should fail validation");
    }
    
    @Test
    public void testValidation_EmptyZoneId() {
        RainZone invalidZone = new RainZone("", 0f, 0f, 100f, 50f, 1.0f);
        assertFalse(invalidZone.isValid(), "Zone with empty ID should fail validation");
    }
    
    @Test
    public void testValidation_NegativeRadius() {
        RainZone invalidZone = new RainZone("test", 0f, 0f, -100f, 50f, 1.0f);
        assertFalse(invalidZone.isValid(), "Zone with negative radius should fail validation");
    }
    
    @Test
    public void testValidation_NegativeFadeDistance() {
        RainZone invalidZone = new RainZone("test", 0f, 0f, 100f, -50f, 1.0f);
        assertFalse(invalidZone.isValid(), "Zone with negative fade distance should fail validation");
    }
    
    @Test
    public void testValidation_IntensityOutOfRange() {
        RainZone invalidZone1 = new RainZone("test", 0f, 0f, 100f, 50f, -0.1f);
        assertFalse(invalidZone1.isValid(), "Zone with negative intensity should fail validation");
        
        RainZone invalidZone2 = new RainZone("test", 0f, 0f, 100f, 50f, 1.5f);
        assertFalse(invalidZone2.isValid(), "Zone with intensity > 1.0 should fail validation");
    }
}
