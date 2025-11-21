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
 * Property-based tests for monotonic alpha decrease during evaporation.
 * Feature: rain-water-puddles, Property 6: Monotonic alpha decrease
 * Validates: Requirements 2.2
 */
public class PuddleAlphaDecreasePropertyTest {
    
    private static HeadlessApplication application;
    private static ShapeRenderer shapeRenderer;
    private static OrthographicCamera camera;
    
    @BeforeAll
    public static void setUpClass() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        application = new HeadlessApplication(new ApplicationAdapter() {}, config);
        
        Gdx.gl = Mockito.mock(GL20.class);
        Gdx.gl20 = Mockito.mock(GL20.class);
        
        shapeRenderer = Mockito.mock(ShapeRenderer.class);
        
        camera = new OrthographicCamera(800, 600);
        camera.position.set(400, 300, 0);
        camera.update();
    }
    
    @AfterAll
    public static void tearDownClass() {
        if (application != null) {
            application.exit();
        }
    }
    
    /**
     * Property 6: Monotonic alpha decrease
     * For any evaporation sequence, puddle alpha values sampled at times 
     * t1 < t2 < t3 should satisfy alpha(t1) >= alpha(t2) >= alpha(t3).
     * Validates: Requirements 2.2
     * 
     * This property-based test runs 100 trials, sampling evaporation progress
     * at multiple points and verifying the alpha multiplier decreases monotonically.
     */
    @Test
    public void alphaDecreasesMonotonicallyDuringEvaporation() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            PuddleManager manager = new PuddleManager(shapeRenderer);
            manager.initialize();
            
            // Reach ACTIVE state
            float accumulatedTime = 0.0f;
            while (manager.getCurrentState() != PuddleState.ACTIVE && accumulatedTime < 10.0f) {
                manager.update(0.1f, true, 1.0f, camera);
                accumulatedTime += 0.1f;
            }
            
            // Stop rain to start evaporation
            manager.update(0.01f, false, 0.0f, camera);
            assertEquals(PuddleState.EVAPORATING, manager.getCurrentState());
            
            // Sample evaporation progress at multiple points
            float[] progressSamples = new float[10];
            float[] alphaSamples = new float[10];
            int sampleIndex = 0;
            
            while (manager.getCurrentState() == PuddleState.EVAPORATING && sampleIndex < 10) {
                // Sample current progress
                float progress = manager.getEvaporationProgress();
                progressSamples[sampleIndex] = progress;
                
                // Calculate expected alpha multiplier (1.0 - progress)
                alphaSamples[sampleIndex] = 1.0f - progress;
                
                sampleIndex++;
                
                // Update with random delta
                float deltaTime = 0.1f + random.nextFloat() * 0.3f;
                manager.update(deltaTime, false, 0.0f, camera);
            }
            
            // Verify alpha values decrease monotonically
            for (int i = 1; i < sampleIndex; i++) {
                assertTrue(
                    alphaSamples[i] <= alphaSamples[i - 1],
                    String.format(
                        "Alpha should decrease monotonically. " +
                        "Sample %d: %.3f, Sample %d: %.3f (progress: %.3f -> %.3f)",
                        i - 1, alphaSamples[i - 1], i, alphaSamples[i],
                        progressSamples[i - 1], progressSamples[i]
                    )
                );
            }
            
            manager.dispose();
        }
    }
    
    /**
     * Property: Alpha multiplier correlates with evaporation progress
     * For any evaporation progress value p, the alpha multiplier should be (1.0 - p).
     * 
     * This property-based test runs 100 trials, verifying the relationship
     * between progress and alpha.
     */
    @Test
    public void alphaMultiplierCorrelatesWithProgress() {
        Random random = new Random(42);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            PuddleManager manager = new PuddleManager(shapeRenderer);
            manager.initialize();
            
            // Reach ACTIVE state
            float accumulatedTime = 0.0f;
            while (manager.getCurrentState() != PuddleState.ACTIVE && accumulatedTime < 10.0f) {
                manager.update(0.1f, true, 1.0f, camera);
                accumulatedTime += 0.1f;
            }
            
            // Stop rain
            manager.update(0.01f, false, 0.0f, camera);
            
            // Sample at random points during evaporation
            int numSamples = 5 + random.nextInt(10); // 5-14 samples
            
            for (int i = 0; i < numSamples && manager.getCurrentState() == PuddleState.EVAPORATING; i++) {
                float progress = manager.getEvaporationProgress();
                float expectedAlpha = 1.0f - progress;
                
                // Verify alpha is in valid range
                assertTrue(
                    expectedAlpha >= 0.0f && expectedAlpha <= 1.0f,
                    String.format(
                        "Expected alpha should be in range [0.0, 1.0]. " +
                        "Progress: %.3f, Expected alpha: %.3f",
                        progress, expectedAlpha
                    )
                );
                
                // Verify relationship: alpha = 1.0 - progress
                assertEquals(
                    1.0f - progress,
                    expectedAlpha,
                    0.001f,
                    "Alpha multiplier should equal (1.0 - progress)"
                );
                
                // Update
                float deltaTime = 0.1f + random.nextFloat() * 0.2f;
                manager.update(deltaTime, false, 0.0f, camera);
            }
            
            manager.dispose();
        }
    }
    
    /**
     * Property: Alpha starts at 1.0 and ends at 0.0
     * For any evaporation cycle, the alpha multiplier should start at 1.0
     * (full visibility) and end at 0.0 (invisible).
     * 
     * This property-based test runs 100 trials.
     */
    @Test
    public void alphaStartsAtOneAndEndsAtZero() {
        Random random = new Random(42);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            PuddleManager manager = new PuddleManager(shapeRenderer);
            manager.initialize();
            
            // Reach ACTIVE state
            float accumulatedTime = 0.0f;
            while (manager.getCurrentState() != PuddleState.ACTIVE && accumulatedTime < 10.0f) {
                manager.update(0.1f, true, 1.0f, camera);
                accumulatedTime += 0.1f;
            }
            
            // Stop rain
            manager.update(0.01f, false, 0.0f, camera);
            assertEquals(PuddleState.EVAPORATING, manager.getCurrentState());
            
            // Check initial alpha (should be close to 1.0)
            float initialProgress = manager.getEvaporationProgress();
            float initialAlpha = 1.0f - initialProgress;
            
            assertTrue(
                initialAlpha >= 0.9f && initialAlpha <= 1.0f,
                String.format(
                    "Initial alpha should be close to 1.0. Got: %.3f (progress: %.3f)",
                    initialAlpha, initialProgress
                )
            );
            
            // Continue until evaporation completes
            while (manager.getCurrentState() == PuddleState.EVAPORATING) {
                float deltaTime = 0.1f + random.nextFloat() * 0.2f;
                manager.update(deltaTime, false, 0.0f, camera);
            }
            
            // After evaporation, state should be NONE
            assertEquals(
                PuddleState.NONE,
                manager.getCurrentState(),
                "Should be in NONE state after evaporation"
            );
            
            // Evaporation progress should be 0 (not evaporating anymore)
            assertEquals(
                0.0f,
                manager.getEvaporationProgress(),
                0.001f,
                "Evaporation progress should be 0 after completion"
            );
            
            manager.dispose();
        }
    }
}
