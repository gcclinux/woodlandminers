package wagemaker.uk.weather;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for puddle count stability with constant intensity.
 * Feature: rain-water-puddles, Property 8: Puddle count correlates with intensity
 * Validates: Requirements 3.3
 */
public class PuddleCountStabilityPropertyTest {
    
    private static HeadlessApplication application;
    private static ShapeRenderer shapeRenderer;
    private static OrthographicCamera camera;
    
    @BeforeAll
    public static void setUpClass() {
        // Initialize LibGDX headless application
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        application = new HeadlessApplication(new ApplicationAdapter() {}, config);
        
        // Mock GL20 for headless testing
        Gdx.gl = Mockito.mock(GL20.class);
        Gdx.gl20 = Mockito.mock(GL20.class);
        
        // Mock ShapeRenderer since we don't need actual rendering for these tests
        shapeRenderer = Mockito.mock(ShapeRenderer.class);
        
        camera = new OrthographicCamera(800, 600);
        camera.position.set(400, 300, 0);
        camera.update();
    }
    
    @AfterAll
    public static void tearDownClass() {
        // Don't dispose mocked ShapeRenderer
        if (application != null) {
            application.exit();
        }
    }
    
    /**
     * Property 8: Puddle count correlates with intensity
     * For any stable rain intensity value, the active puddle count should remain 
     * within a consistent range over time.
     * Validates: Requirements 3.3
     * 
     * This property-based test runs 100 trials with random intensities,
     * verifying that puddle count remains stable during active rain.
     */
    @Test
    public void puddleCountRemainsStableWithConstantIntensity() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Generate random intensity
            float intensity = random.nextFloat(); // 0.0 to 1.0
            
            PuddleManager manager = new PuddleManager(shapeRenderer);
            manager.initialize();
            
            // Accumulate rain to reach ACTIVE state
            float accumulatedTime = 0.0f;
            while (accumulatedTime < PuddleConfig.ACCUMULATION_THRESHOLD + 0.1f) {
                manager.update(0.1f, true, intensity, camera);
                accumulatedTime += 0.1f;
            }
            
            // Verify ACTIVE state
            assertEquals(PuddleState.ACTIVE, manager.getCurrentState(),
                "Manager should be in ACTIVE state");
            
            // Record initial puddle count
            int initialCount = manager.getActivePuddleCount();
            
            // Update multiple times with same intensity
            int numUpdates = 20 + random.nextInt(30); // 20 to 49 updates
            for (int i = 0; i < numUpdates; i++) {
                float deltaTime = 0.01f + random.nextFloat() * 0.2f;
                manager.update(deltaTime, true, intensity, camera);
                
                int currentCount = manager.getActivePuddleCount();
                
                // Verify count remains stable (should be exactly the same)
                assertEquals(initialCount, currentCount,
                    String.format("Puddle count should remain stable with constant intensity. " +
                        "Intensity=%.3f, Initial=%d, Current=%d at update %d",
                        intensity, initialCount, currentCount, i));
            }
            
            manager.dispose();
        }
    }
    
    /**
     * Property: Puddle count is consistent within range for given intensity
     * For any intensity value, multiple managers with the same intensity
     * should produce puddle counts within a small range (due to spacing constraints).
     * 
     * This property-based test runs 100 trials with random intensities,
     * verifying consistent behavior within acceptable variance.
     */
    @Test
    public void puddleCountIsConsistentForGivenIntensity() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Generate random intensity
            float intensity = random.nextFloat(); // 0.0 to 1.0
            
            // Create multiple managers with same intensity
            int numManagers = 5;
            int[] counts = new int[numManagers];
            
            for (int m = 0; m < numManagers; m++) {
                PuddleManager manager = new PuddleManager(shapeRenderer);
                manager.initialize();
                
                // Accumulate rain to reach ACTIVE state
                float accumulatedTime = 0.0f;
                while (accumulatedTime < PuddleConfig.ACCUMULATION_THRESHOLD + 0.1f) {
                    manager.update(0.1f, true, intensity, camera);
                    accumulatedTime += 0.1f;
                }
                
                assertEquals(PuddleState.ACTIVE, manager.getCurrentState());
                counts[m] = manager.getActivePuddleCount();
                
                manager.dispose();
            }
            
            // Calculate expected count and acceptable range
            int range = PuddleConfig.MAX_PUDDLES - PuddleConfig.MIN_PUDDLES;
            int expectedCount = PuddleConfig.MIN_PUDDLES + (int)(range * intensity);
            
            // Verify all counts are within acceptable range (±5 due to spacing constraints and randomness)
            for (int m = 0; m < numManagers; m++) {
                assertTrue(counts[m] >= expectedCount - 5 && counts[m] <= expectedCount + 2,
                    String.format("Puddle count should be consistent for intensity %.3f. " +
                        "Expected~%d, Got=%d for manager %d",
                        intensity, expectedCount, counts[m], m));
            }
        }
    }
    
    /**
     * Property: Puddle count remains in valid range
     * For any intensity value, the puddle count should always be between
     * MIN_PUDDLES and MAX_PUDDLES (inclusive).
     * 
     * This property-based test runs 100 trials with random intensities,
     * verifying bounds are respected.
     */
    @Test
    public void puddleCountRemainsInValidRange() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Generate random intensity
            float intensity = random.nextFloat(); // 0.0 to 1.0
            
            PuddleManager manager = new PuddleManager(shapeRenderer);
            manager.initialize();
            
            // Accumulate rain to reach ACTIVE state
            float accumulatedTime = 0.0f;
            while (accumulatedTime < PuddleConfig.ACCUMULATION_THRESHOLD + 0.1f) {
                manager.update(0.1f, true, intensity, camera);
                accumulatedTime += 0.1f;
            }
            
            assertEquals(PuddleState.ACTIVE, manager.getCurrentState());
            
            int count = manager.getActivePuddleCount();
            
            // Verify count is within valid range
            assertTrue(count >= PuddleConfig.MIN_PUDDLES,
                String.format("Puddle count (%d) should be >= MIN_PUDDLES (%d) for intensity %.3f",
                    count, PuddleConfig.MIN_PUDDLES, intensity));
            
            assertTrue(count <= PuddleConfig.MAX_PUDDLES,
                String.format("Puddle count (%d) should be <= MAX_PUDDLES (%d) for intensity %.3f",
                    count, PuddleConfig.MAX_PUDDLES, intensity));
            
            manager.dispose();
        }
    }
    
    /**
     * Property: Puddle count stability across camera movements
     * For any stable rain intensity, puddle count should remain stable
     * even when camera moves (as long as we're in ACTIVE state).
     * 
     * This property-based test runs 100 trials with random intensities
     * and camera movements.
     */
    @Test
    public void puddleCountStableAcrossCameraMovements() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Generate random intensity
            float intensity = random.nextFloat(); // 0.0 to 1.0
            
            PuddleManager manager = new PuddleManager(shapeRenderer);
            manager.initialize();
            
            // Accumulate rain to reach ACTIVE state
            float accumulatedTime = 0.0f;
            while (accumulatedTime < PuddleConfig.ACCUMULATION_THRESHOLD + 0.1f) {
                manager.update(0.1f, true, intensity, camera);
                accumulatedTime += 0.1f;
            }
            
            assertEquals(PuddleState.ACTIVE, manager.getCurrentState());
            
            // Record initial puddle count
            int initialCount = manager.getActivePuddleCount();
            
            // Move camera and update multiple times
            int numMoves = 10 + random.nextInt(10); // 10 to 19 moves
            for (int i = 0; i < numMoves; i++) {
                // Move camera to random position
                float newX = random.nextFloat() * 2000.0f;
                float newY = random.nextFloat() * 2000.0f;
                camera.position.set(newX, newY, 0);
                camera.update();
                
                // Update manager
                manager.update(0.1f, true, intensity, camera);
                
                int currentCount = manager.getActivePuddleCount();
                
                // Verify count remains stable despite camera movement
                assertEquals(initialCount, currentCount,
                    String.format("Puddle count should remain stable despite camera movement. " +
                        "Intensity=%.3f, Initial=%d, Current=%d at move %d",
                        intensity, initialCount, currentCount, i));
            }
            
            manager.dispose();
        }
    }
}
