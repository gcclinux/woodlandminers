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
 * Property-based tests for intensity affecting puddle visibility.
 * Feature: rain-water-puddles, Property 3: Intensity affects visibility
 * Validates: Requirements 1.3
 */
public class PuddleIntensityVisibilityPropertyTest {
    
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
     * Property 3: Intensity affects visibility
     * For any two rain intensity values A and B where A > B, the puddle visibility 
     * (alpha or count) at intensity A should be greater than or equal to that at intensity B.
     * Validates: Requirements 1.3
     * 
     * This property-based test runs 100 trials with random intensity pairs,
     * verifying that higher intensity results in more visible puddles.
     */
    @Test
    public void higherIntensityResultsInMoreVisiblePuddles() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Generate two random intensities where intensityA > intensityB
            float intensityB = random.nextFloat() * 0.5f; // 0.0 to 0.5
            float intensityA = intensityB + 0.1f + random.nextFloat() * (1.0f - intensityB - 0.1f); // intensityB + 0.1 to 1.0
            
            // Ensure intensityA > intensityB
            assertTrue(intensityA > intensityB, 
                String.format("Test setup error: intensityA (%.3f) should be > intensityB (%.3f)", 
                    intensityA, intensityB));
            
            // Create two managers with same initial conditions
            PuddleManager managerA = new PuddleManager(shapeRenderer);
            PuddleManager managerB = new PuddleManager(shapeRenderer);
            managerA.initialize();
            managerB.initialize();
            
            // Accumulate rain to reach ACTIVE state for both managers
            float accumulatedTime = 0.0f;
            while (accumulatedTime < PuddleConfig.ACCUMULATION_THRESHOLD + 0.1f) {
                managerA.update(0.1f, true, intensityA, camera);
                managerB.update(0.1f, true, intensityB, camera);
                accumulatedTime += 0.1f;
            }
            
            // Verify both are in ACTIVE state
            assertEquals(PuddleState.ACTIVE, managerA.getCurrentState(),
                "Manager A should be in ACTIVE state");
            assertEquals(PuddleState.ACTIVE, managerB.getCurrentState(),
                "Manager B should be in ACTIVE state");
            
            // Get puddle counts
            int countA = managerA.getActivePuddleCount();
            int countB = managerB.getActivePuddleCount();
            
            // Verify that higher intensity results in more or equal puddles
            assertTrue(countA >= countB,
                String.format("Higher intensity (%.3f) should result in >= puddles than lower intensity (%.3f). " +
                    "Got countA=%d, countB=%d", intensityA, intensityB, countA, countB));
            
            // If intensities are significantly different, expect different counts
            if (intensityA - intensityB > 0.3f) {
                assertTrue(countA > countB,
                    String.format("Significantly higher intensity (%.3f vs %.3f) should result in more puddles. " +
                        "Got countA=%d, countB=%d", intensityA, intensityB, countA, countB));
            }
            
            managerA.dispose();
            managerB.dispose();
        }
    }
    
    /**
     * Property: Puddle count scales linearly with intensity
     * For any intensity value, the puddle count should be approximately
     * MIN_PUDDLES + (MAX_PUDDLES - MIN_PUDDLES) * intensity.
     * 
     * This property-based test runs 100 trials with random intensities,
     * verifying linear scaling.
     */
    @Test
    public void puddleCountScalesLinearlyWithIntensity() {
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
            
            // Calculate expected puddle count
            int range = PuddleConfig.MAX_PUDDLES - PuddleConfig.MIN_PUDDLES;
            int expectedCount = PuddleConfig.MIN_PUDDLES + (int)(range * intensity);
            
            // Get actual puddle count
            int actualCount = manager.getActivePuddleCount();
            
            // Verify count is within expected range (allow for spacing constraints)
            assertTrue(actualCount >= expectedCount - 3 && actualCount <= expectedCount + 1,
                String.format("Puddle count should scale approximately linearly with intensity. " +
                    "Intensity=%.3f, Expected=%d, Actual=%d", intensity, expectedCount, actualCount));
            
            manager.dispose();
        }
    }
    
    /**
     * Property: Extreme intensities produce expected counts
     * For intensity 0.0, should get MIN_PUDDLES.
     * For intensity 1.0, should get MAX_PUDDLES.
     * 
     * This property-based test runs multiple trials to verify edge cases.
     */
    @Test
    public void extremeIntensitiesProduceExpectedCounts() {
        // Test minimum intensity (0.0)
        for (int trial = 0; trial < 10; trial++) {
            PuddleManager manager = new PuddleManager(shapeRenderer);
            manager.initialize();
            
            // Accumulate rain with minimum intensity
            float accumulatedTime = 0.0f;
            while (accumulatedTime < PuddleConfig.ACCUMULATION_THRESHOLD + 0.1f) {
                manager.update(0.1f, true, 0.0f, camera);
                accumulatedTime += 0.1f;
            }
            
            assertEquals(PuddleState.ACTIVE, manager.getCurrentState());
            assertEquals(PuddleConfig.MIN_PUDDLES, manager.getActivePuddleCount(),
                "Intensity 0.0 should produce MIN_PUDDLES");
            
            manager.dispose();
        }
        
        // Test maximum intensity (1.0)
        for (int trial = 0; trial < 10; trial++) {
            PuddleManager manager = new PuddleManager(shapeRenderer);
            manager.initialize();
            
            // Accumulate rain with maximum intensity
            float accumulatedTime = 0.0f;
            while (accumulatedTime < PuddleConfig.ACCUMULATION_THRESHOLD + 0.1f) {
                manager.update(0.1f, true, 1.0f, camera);
                accumulatedTime += 0.1f;
            }
            
            assertEquals(PuddleState.ACTIVE, manager.getCurrentState());
            int actualCount = manager.getActivePuddleCount();
            // Allow for spacing constraints - should be close to MAX_PUDDLES
            assertTrue(actualCount >= PuddleConfig.MAX_PUDDLES - 5,
                String.format("Intensity 1.0 should produce approximately MAX_PUDDLES. Got %d, expected ~%d",
                    actualCount, PuddleConfig.MAX_PUDDLES));
            
            manager.dispose();
        }
    }
}
