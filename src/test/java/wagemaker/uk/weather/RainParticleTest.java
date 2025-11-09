package wagemaker.uk.weather;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RainParticle pool reuse and reset functionality.
 */
public class RainParticleTest {
    
    private RainParticle particle;
    
    @BeforeEach
    public void setUp() {
        particle = new RainParticle();
    }
    
    @Test
    public void testInitialState() {
        // New particle should be inactive
        assertFalse(particle.isActive(), "New particle should be inactive");
    }
    
    @Test
    public void testReset() {
        // Reset particle with specific values
        particle.reset(100f, 200f, 500f, 12f);
        
        // Verify all properties were set correctly
        assertTrue(particle.isActive(), "Particle should be active after reset");
        assertEquals(100f, particle.getX(), 0.01f, "X position should be set");
        assertEquals(200f, particle.getY(), 0.01f, "Y position should be set");
        assertEquals(500f, particle.getVelocityY(), 0.01f, "Velocity should be set");
        assertEquals(12f, particle.getLength(), 0.01f, "Length should be set");
        assertEquals(0.5f, particle.getAlpha(), 0.01f, "Alpha should be set to default 0.5");
    }
    
    @Test
    public void testResetMultipleTimes() {
        // Reset particle multiple times to test reuse
        particle.reset(100f, 200f, 500f, 12f);
        assertEquals(100f, particle.getX(), 0.01f, "First reset X should be correct");
        
        particle.reset(300f, 400f, 600f, 15f);
        assertEquals(300f, particle.getX(), 0.01f, "Second reset X should be correct");
        assertEquals(400f, particle.getY(), 0.01f, "Second reset Y should be correct");
        assertEquals(600f, particle.getVelocityY(), 0.01f, "Second reset velocity should be correct");
        assertEquals(15f, particle.getLength(), 0.01f, "Second reset length should be correct");
        assertTrue(particle.isActive(), "Particle should remain active after multiple resets");
    }
    
    @Test
    public void testUpdate() {
        // Reset particle and update position
        particle.reset(100f, 500f, 400f, 12f);
        
        // Update with 0.1 second delta time
        // Expected Y change: 400 pixels/second * 0.1 seconds = 40 pixels
        particle.update(0.1f);
        
        assertEquals(100f, particle.getX(), 0.01f, "X position should not change");
        assertEquals(460f, particle.getY(), 0.01f, "Y position should decrease by velocity * deltaTime");
    }
    
    @Test
    public void testUpdateMultipleFrames() {
        // Reset particle
        particle.reset(100f, 600f, 500f, 12f);
        
        // Simulate multiple frame updates
        particle.update(0.016f); // ~60 FPS frame
        particle.update(0.016f);
        particle.update(0.016f);
        
        // Expected Y change: 500 * 0.016 * 3 = 24 pixels
        float expectedY = 600f - (500f * 0.016f * 3);
        assertEquals(expectedY, particle.getY(), 0.1f, "Y position should accumulate over multiple updates");
    }
    
    @Test
    public void testUpdateInactiveParticle() {
        // Inactive particle should not update
        assertFalse(particle.isActive(), "Particle should be inactive");
        
        float initialY = particle.getY();
        particle.update(0.1f);
        
        assertEquals(initialY, particle.getY(), 0.01f, "Inactive particle should not move");
    }
    
    @Test
    public void testIsOffScreen() {
        particle.reset(100f, 50f, 500f, 12f);
        
        // Particle at Y=50 should be off screen if screen bottom is at Y=100
        assertTrue(particle.isOffScreen(100f), "Particle below screen bottom should be off screen");
        
        // Particle at Y=50 should be on screen if screen bottom is at Y=0
        assertFalse(particle.isOffScreen(0f), "Particle above screen bottom should be on screen");
    }
    
    @Test
    public void testIsOffScreen_ExactlyAtBoundary() {
        particle.reset(100f, 100f, 500f, 12f);
        
        // Particle exactly at screen bottom
        assertTrue(particle.isOffScreen(100f), "Particle at screen bottom should be considered off screen");
    }
    
    @Test
    public void testParticleLifecycle() {
        // Simulate full particle lifecycle: create, reset, update, check off-screen, deactivate, reuse
        
        // 1. Create (already done in setUp)
        assertFalse(particle.isActive(), "New particle should be inactive");
        
        // 2. Reset (activate)
        particle.reset(100f, 600f, 500f, 12f);
        assertTrue(particle.isActive(), "Particle should be active after reset");
        
        // 3. Update until off screen
        while (!particle.isOffScreen(0f)) {
            particle.update(0.016f);
        }
        assertTrue(particle.isOffScreen(0f), "Particle should eventually go off screen");
        
        // 4. Deactivate
        particle.setActive(false);
        assertFalse(particle.isActive(), "Particle should be deactivated");
        
        // 5. Reuse (reset again)
        particle.reset(200f, 700f, 450f, 14f);
        assertTrue(particle.isActive(), "Particle should be reactivated");
        assertEquals(200f, particle.getX(), 0.01f, "Reused particle should have new position");
    }
    
    @Test
    public void testSettersAndGetters() {
        // Test all setters and getters
        particle.setX(150f);
        assertEquals(150f, particle.getX(), 0.01f, "X setter/getter should work");
        
        particle.setY(250f);
        assertEquals(250f, particle.getY(), 0.01f, "Y setter/getter should work");
        
        particle.setVelocityY(550f);
        assertEquals(550f, particle.getVelocityY(), 0.01f, "VelocityY setter/getter should work");
        
        particle.setLength(13f);
        assertEquals(13f, particle.getLength(), 0.01f, "Length setter/getter should work");
        
        particle.setAlpha(0.7f);
        assertEquals(0.7f, particle.getAlpha(), 0.01f, "Alpha setter/getter should work");
        
        particle.setActive(true);
        assertTrue(particle.isActive(), "Active setter/getter should work");
    }
}
