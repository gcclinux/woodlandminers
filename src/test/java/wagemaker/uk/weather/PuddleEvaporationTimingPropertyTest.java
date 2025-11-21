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
 * Property-based tests for puddle evaporation timing.
 * Feature: rain-water-puddles, Property 5: Evaporation timing
 * Validates: Requirements 2.1
 */
public class PuddleEvaporationTimingPropertyTest {
    
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
     * Property 5: Evaporation timing
     * For any rain stop event, puddles should take approximately 5 seconds (±0.5s) 
     * to completely fade out.
     * Validates: Requirements 2.1
     * 
     * This property-based test runs 100 trials with random time increments,
     * verifying that evaporation completes within the expected timeframe.
     */
    @Test
    public void puddlesEvaporateInExpectedTime() {
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
            
            assertEquals(PuddleState.ACTIVE, manager.getCurrentState());
            int initialCount = manager.getActivePuddleCount();
            assertTrue(initialCount > 0, "Should have puddles");
            
            // Stop rain
            manager.update(0.01f, false, 0.0f, camera);
            assertEquals(
                PuddleState.EVAPORATING,
                manager.getCurrentState(),
                "Should transition to EVAPORATING when rain stops"
            );
            
            // Track evaporation time
            float evaporationTime = 0.01f; // Already did one update
            
            // Continue updating until puddles are gone
            while (manager.getCurrentState() == PuddleState.EVAPORATING) {
                float deltaTime = 0.01f + random.nextFloat() * 0.1f;
                manager.update(deltaTime, false, 0.0f, camera);
                evaporationTime += deltaTime;
                
                // Safety check to prevent infinite loop
                if (evaporationTime > 10.0f) {
                    fail("Evaporation took too long (> 10 seconds)");
                }
            }
            
            // Verify final state is NONE
            assertEquals(
                PuddleState.NONE,
                manager.getCurrentState(),
                "Should transition to NONE after evaporation"
            );
            
            // Verify all puddles are gone
            assertEquals(
                0,
                manager.getActivePuddleCount(),
                "All puddles should be gone after evaporation"
            );
            
            // Verify evaporation time is approximately 5 seconds (±0.5s)
            assertTrue(
                evaporationTime >= PuddleConfig.EVAPORATION_DURATION - 0.5f &&
                evaporationTime <= PuddleConfig.EVAPORATION_DURATION + 0.5f,
                String.format(
                    "Evaporation should take approximately %.1f seconds (±0.5s). " +
                    "Actual: %.3f seconds",
                    PuddleConfig.EVAPORATION_DURATION, evaporationTime
                )
            );
            
            manager.dispose();
        }
    }
    
    /**
     * Property: Evaporation progress increases monotonically
     * For any evaporation sequence, the evaporation progress should increase
     * from 0.0 to 1.0 monotonically.
     * 
     * This property-based test runs 100 trials with random time increments.
     */
    @Test
    public void evaporationProgressIncreasesMonotonically() {
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
            
            float previousProgress = 0.0f;
            
            // Track progress during evaporation
            while (manager.getCurrentState() == PuddleState.EVAPORATING) {
                float deltaTime = 0.01f + random.nextFloat() * 0.1f;
                manager.update(deltaTime, false, 0.0f, camera);
                
                // Only check progress if still evaporating
                if (manager.getCurrentState() == PuddleState.EVAPORATING) {
                    float currentProgress = manager.getEvaporationProgress();
                    
                    // Verify progress is monotonically increasing
                    assertTrue(
                        currentProgress >= previousProgress,
                        String.format(
                            "Evaporation progress should increase monotonically. " +
                            "Previous: %.3f, Current: %.3f",
                            previousProgress, currentProgress
                        )
                    );
                    
                    // Verify progress is in valid range
                    assertTrue(
                        currentProgress >= 0.0f && currentProgress <= 1.0f,
                        "Evaporation progress should be in range [0.0, 1.0]. Got: " + currentProgress
                    );
                    
                    previousProgress = currentProgress;
                }
            }
            
            manager.dispose();
        }
    }
}
