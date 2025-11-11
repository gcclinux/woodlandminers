package wagemaker.uk.biome;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BiomeZone containment logic and zone configuration.
 * Tests zone boundaries, distance containment, and zone properties.
 */
public class BiomeZoneTest {
    
    // ===== Basic Zone Creation Tests =====
    
    @Test
    public void testZoneCreation() {
        BiomeZone zone = new BiomeZone(0.0f, 1000.0f, BiomeType.GRASS);
        
        assertNotNull(zone, "Zone should be created successfully");
        assertEquals(0.0f, zone.getMinDistance(), 0.01f, "Min distance should match");
        assertEquals(1000.0f, zone.getMaxDistance(), 0.01f, "Max distance should match");
        assertEquals(BiomeType.GRASS, zone.getBiomeType(), "Biome type should match");
    }
    
    @Test
    public void testZoneWithDifferentBiomeType() {
        BiomeZone zone = new BiomeZone(1000.0f, 2000.0f, BiomeType.SAND);
        
        assertEquals(BiomeType.SAND, zone.getBiomeType(), "Should support SAND biome type");
    }
    
    // ===== Distance Containment Tests =====
    
    @Test
    public void testContainsDistance_AtMinBoundary() {
        BiomeZone zone = new BiomeZone(1000.0f, 2000.0f, BiomeType.GRASS);
        
        assertTrue(zone.containsDistance(1000.0f), 
            "Zone should contain distance at min boundary (inclusive)");
    }
    
    @Test
    public void testContainsDistance_AtMaxBoundary() {
        BiomeZone zone = new BiomeZone(1000.0f, 2000.0f, BiomeType.GRASS);
        
        assertFalse(zone.containsDistance(2000.0f), 
            "Zone should NOT contain distance at max boundary (exclusive)");
    }
    
    @Test
    public void testContainsDistance_WithinRange() {
        BiomeZone zone = new BiomeZone(1000.0f, 2000.0f, BiomeType.GRASS);
        
        assertTrue(zone.containsDistance(1500.0f), 
            "Zone should contain distance within range");
        assertTrue(zone.containsDistance(1000.1f), 
            "Zone should contain distance just above min");
        assertTrue(zone.containsDistance(1999.9f), 
            "Zone should contain distance just below max");
    }
    
    @Test
    public void testContainsDistance_BelowRange() {
        BiomeZone zone = new BiomeZone(1000.0f, 2000.0f, BiomeType.GRASS);
        
        assertFalse(zone.containsDistance(999.9f), 
            "Zone should NOT contain distance below range");
        assertFalse(zone.containsDistance(0.0f), 
            "Zone should NOT contain distance well below range");
    }
    
    @Test
    public void testContainsDistance_AboveRange() {
        BiomeZone zone = new BiomeZone(1000.0f, 2000.0f, BiomeType.GRASS);
        
        assertFalse(zone.containsDistance(2000.1f), 
            "Zone should NOT contain distance above range");
        assertFalse(zone.containsDistance(5000.0f), 
            "Zone should NOT contain distance well above range");
    }
    
    // ===== Zero-Based Zone Tests =====
    
    @Test
    public void testZoneStartingAtZero() {
        BiomeZone zone = new BiomeZone(0.0f, 1000.0f, BiomeType.GRASS);
        
        assertTrue(zone.containsDistance(0.0f), 
            "Zone starting at 0 should contain 0");
        assertTrue(zone.containsDistance(500.0f), 
            "Zone starting at 0 should contain mid-range values");
        assertFalse(zone.containsDistance(1000.0f), 
            "Zone starting at 0 should not contain max boundary");
    }
    
    @Test
    public void testZoneWithVerySmallRange() {
        BiomeZone zone = new BiomeZone(100.0f, 100.1f, BiomeType.SAND);
        
        assertTrue(zone.containsDistance(100.0f), 
            "Small zone should contain min boundary");
        assertTrue(zone.containsDistance(100.05f), 
            "Small zone should contain mid-range");
        assertFalse(zone.containsDistance(100.1f), 
            "Small zone should not contain max boundary");
    }
    
    // ===== Large Range Zone Tests =====
    
    @Test
    public void testZoneWithLargeRange() {
        BiomeZone zone = new BiomeZone(0.0f, 100000.0f, BiomeType.GRASS);
        
        assertTrue(zone.containsDistance(0.0f), "Large zone should contain min");
        assertTrue(zone.containsDistance(50000.0f), "Large zone should contain middle");
        assertTrue(zone.containsDistance(99999.9f), "Large zone should contain near max");
        assertFalse(zone.containsDistance(100000.0f), "Large zone should not contain max");
    }
    
    @Test
    public void testZoneWithInfiniteMax() {
        BiomeZone zone = new BiomeZone(10000.0f, Float.MAX_VALUE, BiomeType.GRASS);
        
        assertTrue(zone.containsDistance(10000.0f), 
            "Infinite zone should contain min boundary");
        assertTrue(zone.containsDistance(100000.0f), 
            "Infinite zone should contain large distances");
        assertTrue(zone.containsDistance(1000000.0f), 
            "Infinite zone should contain very large distances");
        // Note: Float.MAX_VALUE - 1.0f equals Float.MAX_VALUE due to float precision
        // so we test with a smaller value instead
        assertTrue(zone.containsDistance(Float.MAX_VALUE / 2.0f), 
            "Infinite zone should contain half of max float value");
    }
    
    // ===== Realistic Game Configuration Tests =====
    
    @Test
    public void testInnerGrassZone() {
        // Zone 1: Inner grass (0 to 10000)
        BiomeZone zone = new BiomeZone(0.0f, BiomeConfig.INNER_GRASS_RADIUS, BiomeType.GRASS);
        
        assertTrue(zone.containsDistance(0.0f), "Should contain spawn point");
        assertTrue(zone.containsDistance(5000.0f), "Should contain mid-range");
        assertTrue(zone.containsDistance(9999.9f), "Should contain just before boundary");
        assertFalse(zone.containsDistance(10000.0f), "Should not contain boundary");
        assertFalse(zone.containsDistance(10001.0f), "Should not contain beyond boundary");
    }
    
    @Test
    public void testSandZone() {
        // Zone 2: Sand (10000 to 13000)
        float sandStart = BiomeConfig.INNER_GRASS_RADIUS;
        float sandEnd = BiomeConfig.INNER_GRASS_RADIUS + BiomeConfig.SAND_ZONE_WIDTH;
        BiomeZone zone = new BiomeZone(sandStart, sandEnd, BiomeType.SAND);
        
        assertFalse(zone.containsDistance(9999.9f), "Should not contain before zone");
        assertTrue(zone.containsDistance(10000.0f), "Should contain start boundary");
        assertTrue(zone.containsDistance(11500.0f), "Should contain mid-range");
        assertTrue(zone.containsDistance(12999.9f), "Should contain just before end");
        assertFalse(zone.containsDistance(13000.0f), "Should not contain end boundary");
        assertFalse(zone.containsDistance(13001.0f), "Should not contain beyond zone");
    }
    
    @Test
    public void testOuterGrassZone() {
        // Zone 3: Outer grass (13000 to infinity)
        float outerStart = BiomeConfig.INNER_GRASS_RADIUS + BiomeConfig.SAND_ZONE_WIDTH;
        BiomeZone zone = new BiomeZone(outerStart, Float.MAX_VALUE, BiomeType.GRASS);
        
        assertFalse(zone.containsDistance(12999.9f), "Should not contain before zone");
        assertTrue(zone.containsDistance(13000.0f), "Should contain start boundary");
        assertTrue(zone.containsDistance(20000.0f), "Should contain far distances");
        assertTrue(zone.containsDistance(100000.0f), "Should contain very far distances");
    }
    
    // ===== Zone Boundary Precision Tests =====
    
    @Test
    public void testPreciseBoundaryBehavior() {
        BiomeZone zone = new BiomeZone(1000.0f, 2000.0f, BiomeType.GRASS);
        
        // Test very close to boundaries
        assertTrue(zone.containsDistance(1000.0f), "Exactly at min should be included");
        assertTrue(zone.containsDistance(1000.0001f), "Just above min should be included");
        assertTrue(zone.containsDistance(1999.9999f), "Just below max should be included");
        assertFalse(zone.containsDistance(2000.0f), "Exactly at max should be excluded");
        assertFalse(zone.containsDistance(2000.0001f), "Just above max should be excluded");
        assertFalse(zone.containsDistance(999.9999f), "Just below min should be excluded");
    }
    
    // ===== Multiple Zone Interaction Tests =====
    
    @Test
    public void testNonOverlappingZones() {
        BiomeZone zone1 = new BiomeZone(0.0f, 1000.0f, BiomeType.GRASS);
        BiomeZone zone2 = new BiomeZone(1000.0f, 2000.0f, BiomeType.SAND);
        BiomeZone zone3 = new BiomeZone(2000.0f, 3000.0f, BiomeType.GRASS);
        
        // Test that zones don't overlap
        assertTrue(zone1.containsDistance(999.9f), "Zone 1 should contain 999.9");
        assertFalse(zone1.containsDistance(1000.0f), "Zone 1 should not contain 1000");
        
        assertFalse(zone2.containsDistance(999.9f), "Zone 2 should not contain 999.9");
        assertTrue(zone2.containsDistance(1000.0f), "Zone 2 should contain 1000");
        assertTrue(zone2.containsDistance(1999.9f), "Zone 2 should contain 1999.9");
        assertFalse(zone2.containsDistance(2000.0f), "Zone 2 should not contain 2000");
        
        assertFalse(zone3.containsDistance(1999.9f), "Zone 3 should not contain 1999.9");
        assertTrue(zone3.containsDistance(2000.0f), "Zone 3 should contain 2000");
    }
    
    @Test
    public void testCompleteZoneCoverage() {
        // Test that the three zones cover all possible distances
        BiomeZone zone1 = new BiomeZone(0.0f, 10000.0f, BiomeType.GRASS);
        BiomeZone zone2 = new BiomeZone(10000.0f, 13000.0f, BiomeType.SAND);
        BiomeZone zone3 = new BiomeZone(13000.0f, Float.MAX_VALUE, BiomeType.GRASS);
        
        // Test various distances are covered by exactly one zone
        float[] testDistances = {0.0f, 5000.0f, 9999.9f, 10000.0f, 11500.0f, 
                                 12999.9f, 13000.0f, 20000.0f, 100000.0f};
        
        for (float distance : testDistances) {
            int containingZones = 0;
            if (zone1.containsDistance(distance)) containingZones++;
            if (zone2.containsDistance(distance)) containingZones++;
            if (zone3.containsDistance(distance)) containingZones++;
            
            assertEquals(1, containingZones, 
                "Distance " + distance + " should be in exactly one zone");
        }
    }
    
    // ===== Negative Distance Tests =====
    
    @Test
    public void testNegativeDistances() {
        BiomeZone zone = new BiomeZone(0.0f, 1000.0f, BiomeType.GRASS);
        
        // Negative distances should not be contained (distance is always positive)
        assertFalse(zone.containsDistance(-1.0f), 
            "Zone should not contain negative distance");
        assertFalse(zone.containsDistance(-100.0f), 
            "Zone should not contain negative distance");
    }
    
    // ===== Getter Tests =====
    
    @Test
    public void testGetters() {
        float minDist = 1000.0f;
        float maxDist = 2000.0f;
        BiomeType type = BiomeType.SAND;
        
        BiomeZone zone = new BiomeZone(minDist, maxDist, type);
        
        assertEquals(minDist, zone.getMinDistance(), 0.01f, 
            "getMinDistance should return correct value");
        assertEquals(maxDist, zone.getMaxDistance(), 0.01f, 
            "getMaxDistance should return correct value");
        assertEquals(type, zone.getBiomeType(), 
            "getBiomeType should return correct value");
    }
    
    // ===== Edge Case Tests =====
    
    @Test
    public void testZeroWidthZone() {
        // Edge case: zone with same min and max (zero width)
        BiomeZone zone = new BiomeZone(1000.0f, 1000.0f, BiomeType.GRASS);
        
        // No distance should be contained in a zero-width zone
        assertFalse(zone.containsDistance(999.9f), "Zero-width zone should not contain below");
        assertFalse(zone.containsDistance(1000.0f), "Zero-width zone should not contain exact");
        assertFalse(zone.containsDistance(1000.1f), "Zero-width zone should not contain above");
    }
    
    @Test
    public void testVeryLargeDistances() {
        BiomeZone zone = new BiomeZone(0.0f, Float.MAX_VALUE, BiomeType.GRASS);
        
        assertTrue(zone.containsDistance(1000000.0f), 
            "Zone should contain very large distances");
        assertTrue(zone.containsDistance(Float.MAX_VALUE / 2.0f), 
            "Zone should contain half of max float");
    }
}
