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
 * Property-based tests for puddle accumulation threshold timing.
 * Feature: rain-water-puddles, Property 1: Accumulation threshold timing
 * Validates: Requirements 1.1
 */
public class PuddleAccumulationThresholdPropertyTest {
    
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
     * Property 1: Accumulation threshold timing
     * For any rain event, puddles should only become visible after rain has been 
     * continuously active for at least 5 seconds.
     * Validates: Requirements 1.1
     * 
     * This property-based test runs 100 trials with random time increments,
     * verifying that puddles only appear after the accumulation threshold.
     */
    @Test
    public void puddlesOnlyAppearAfterAccumulationThreshold() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            PuddleManager manager = new PuddleManager(shapeRenderer);
            manager.initialize();
            
            // Generate random time increments that sum to just before threshold
            float totalTime = 0.0f;
            float targetTime = PuddleConfig.ACCUMULATION_THRESHOLD - 0.1f; // Just before threshold
            
            // Update with rain active, but not reaching threshold
            while (totalTime < targetTime) {
                float deltaTime = 0.01f + random.nextFloat() * 0.1f; // 0.01 to 0.11 seconds
                if (totalTime + deltaTime > targetTime) {
                    deltaTime = targetTime - totalTime;
                }
                
                manager.update(deltaTime, true, 1.0f, camera);
                totalTime += deltaTime;
                
                // Verify puddles have NOT appeared yet
                assertEquals(
                    PuddleState.ACCUMULATING,
                    manager.getCurrentState(),
                    "State should be ACCUMULATING before threshold at time " + totalTime
                );
                
                assertEquals(
                    0,
                    manager.getActivePuddleCount(),
                    "No puddles should be active before threshold at time " + totalTime
                );
            }
            
            // Now cross the threshold
            manager.update(0.2f, true, 1.0f, camera);
            totalTime += 0.2f;
            
            // Verify puddles HAVE appeared
            assertEquals(
                PuddleState.ACTIVE,
                manager.getCurrentState(),
                "State should be ACTIVE after threshold at time " + totalTime
            );
            
            assertTrue(
                manager.getActivePuddleCount() > 0,
                "Puddles should be active after threshold at time " + totalTime
            );
            
            manager.dispose();
        }
    }
    
    /**
     * Property: Accumulation progress increases monotonically
     * For any sequence of updates during accumulation, the accumulation progress
     * should increase monotonically from 0.0 to 1.0.
     * 
     * This property-based test runs 100 trials with random time increments,
     * verifying monotonic progress increase.
     */
    @Test
    public void accumulationProgressIncreasesMonotonically() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            PuddleManager manager = new PuddleManager(shapeRenderer);
            manager.initialize();
            
            float previousProgress = 0.0f;
            float totalTime = 0.0f;
            
            // Update until threshold is reached
            while (totalTime < PuddleConfig.ACCUMULATION_THRESHOLD + 0.5f) {
                float deltaTime = 0.01f + random.nextFloat() * 0.1f; // 0.01 to 0.11 seconds
                
                manager.update(deltaTime, true, 1.0f, camera);
                totalTime += deltaTime;
                
                float currentProgress = manager.getAccumulationProgress();
                
                // Only check monotonic increase while still accumulating
                if (manager.getCurrentState() == PuddleState.ACCUMULATING) {
                    // Verify progress is monotonically increasing
                    assertTrue(
                        currentProgress >= previousProgress,
                        String.format(
                            "Accumulation progress should increase monotonically. " +
                            "Previous: %.3f, Current: %.3f at time %.3f",
                            previousProgress, currentProgress, totalTime
                        )
                    );
                    
                    // Verify progress is in valid range
                    assertTrue(
                        currentProgress >= 0.0f && currentProgress <= 1.0f,
                        "Accumulation progress should be in range [0.0, 1.0]. Got: " + currentProgress
                    );
                    
                    previousProgress = currentProgress;
                } else if (manager.getCurrentState() == PuddleState.ACTIVE) {
                    // Transitioned to ACTIVE, stop checking
                    break;
                }
            }
            
            // After threshold, progress should be 0 (state changed to ACTIVE)
            assertEquals(
                PuddleState.ACTIVE,
                manager.getCurrentState(),
                "State should be ACTIVE after threshold"
            );
            
            assertEquals(
                0.0f,
                manager.getAccumulationProgress(),
                0.001f,
                "Accumulation progress should be 0 after transitioning to ACTIVE"
            );
            
            manager.dispose();
        }
    }
    
    /**
     * Property: Exact threshold timing
     * For any rain event that lasts exactly the accumulation threshold,
     * puddles should appear.
     * 
     * This property-based test runs 100 trials with different time step patterns,
     * all summing to exactly the threshold.
     */
    @Test
    public void puddlesAppearAtExactThreshold() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            PuddleManager manager = new PuddleManager(shapeRenderer);
            manager.initialize();
            
            // Generate random number of steps that sum to threshold
            int numSteps = 10 + random.nextInt(40); // 10 to 49 steps
            float stepSize = PuddleConfig.ACCUMULATION_THRESHOLD / numSteps;
            
            float totalTime = 0.0f;
            
            // Update in steps
            for (int step = 0; step < numSteps - 1; step++) {
                manager.update(stepSize, true, 1.0f, camera);
                totalTime += stepSize;
                
                // Should still be accumulating
                assertEquals(
                    PuddleState.ACCUMULATING,
                    manager.getCurrentState(),
                    "Should be ACCUMULATING at step " + step
                );
            }
            
            // Final step to reach exactly the threshold
            manager.update(stepSize, true, 1.0f, camera);
            totalTime += stepSize;
            
            // Should now be ACTIVE (or very close to threshold if floating point error)
            // If still ACCUMULATING due to floating point error, do one more tiny update
            if (manager.getCurrentState() == PuddleState.ACCUMULATING) {
                manager.update(0.01f, true, 1.0f, camera);
                totalTime += 0.01f;
            }
            
            assertEquals(
                PuddleState.ACTIVE,
                manager.getCurrentState(),
                "Should be ACTIVE after approximately " + totalTime + " seconds"
            );
            
            assertTrue(
                manager.getActivePuddleCount() > 0,
                "Should have active puddles after threshold"
            );
            
            // Verify total time is approximately the threshold (allow for floating point error)
            assertEquals(
                PuddleConfig.ACCUMULATION_THRESHOLD,
                totalTime,
                0.01f,
                "Total time should approximately equal threshold"
            );
            
            manager.dispose();
        }
    }
    
    /**
     * Property: Rain stopping before threshold prevents puddles
     * For any rain event that stops before the accumulation threshold,
     * no puddles should appear.
     * 
     * This property-based test runs 100 trials with random durations
     * less than the threshold.
     */
    @Test
    public void rainStoppingBeforeThresholdPreventsPuddles() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            PuddleManager manager = new PuddleManager(shapeRenderer);
            manager.initialize();
            
            // Generate random duration less than threshold
            float rainDuration = random.nextFloat() * (PuddleConfig.ACCUMULATION_THRESHOLD - 0.1f);
            
            float totalTime = 0.0f;
            
            // Update with rain active
            while (totalTime < rainDuration) {
                float deltaTime = 0.01f + random.nextFloat() * 0.1f;
                if (totalTime + deltaTime > rainDuration) {
                    deltaTime = rainDuration - totalTime;
                }
                
                manager.update(deltaTime, true, 1.0f, camera);
                totalTime += deltaTime;
            }
            
            // Stop rain
            manager.update(0.01f, false, 0.0f, camera);
            
            // Verify no puddles appeared
            assertEquals(
                PuddleState.NONE,
                manager.getCurrentState(),
                "State should be NONE after rain stops before threshold"
            );
            
            assertEquals(
                0,
                manager.getActivePuddleCount(),
                "No puddles should be active when rain stops before threshold"
            );
            
            manager.dispose();
        }
    }
}
