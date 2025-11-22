package wagemaker.uk.birds;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for spawn interval bounds.
 * Feature: flying-birds-ambient, Property 1: Spawn interval bounds
 * Validates: Requirements 1.1
 */
public class SpawnIntervalBoundsPropertyTest {
    
    private static HeadlessApplication application;
    private static Texture mockTexture;
    private static final float MIN_SPAWN_INTERVAL = 60f; // 1 minute
    private static final float MAX_SPAWN_INTERVAL = 180f; // 3 minutes
    
    @BeforeAll
    public static void setupGdx() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        application = new HeadlessApplication(new ApplicationAdapter() {
            @Override
            public void create() {
                Gdx.gl = Mockito.mock(GL20.class);
            }
        }, config);
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Create a mock texture for testing
        mockTexture = Mockito.mock(Texture.class);
    }
    
    @AfterAll
    public static void tearDownGdx() {
        if (application != null) {
            application.exit();
        }
    }
    
    /**
     * Property 1: Spawn interval bounds
     * For any spawn event, the time since the last spawn should be between 180 and 300 seconds (inclusive)
     * Validates: Requirements 1.1
     * 
     * This property-based test runs 100 trials, creating BirdFormationManager instances
     * and verifying that all generated spawn intervals fall within the specified range.
     */
    @Test
    public void spawnIntervalWithinBounds() {
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create a new BirdFormationManager for each trial
            OrthographicCamera camera = new OrthographicCamera();
            camera.setToOrtho(false, 800, 600);
            Viewport viewport = new FitViewport(800, 600, camera);
            
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Get the initial spawn interval
            float spawnInterval = manager.getNextSpawnInterval();
            
            // Verify the interval is within bounds
            assertTrue(
                spawnInterval >= MIN_SPAWN_INTERVAL,
                "Trial " + trial + ": Spawn interval " + spawnInterval + 
                " should be >= " + MIN_SPAWN_INTERVAL + " seconds"
            );
            
            assertTrue(
                spawnInterval <= MAX_SPAWN_INTERVAL,
                "Trial " + trial + ": Spawn interval " + spawnInterval + 
                " should be <= " + MAX_SPAWN_INTERVAL + " seconds"
            );
            
            // Clean up
            manager.dispose();
        }
    }
    
    /**
     * Property: Multiple spawn intervals are all within bounds
     * For any sequence of spawn events, all spawn intervals should be within 180-300 seconds.
     * 
     * This property-based test runs 100 trials, simulating multiple spawn cycles
     * and verifying that each generated interval is within the valid range.
     */
    @Test
    public void multipleSpawnIntervalsWithinBounds() {
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        Viewport viewport = new FitViewport(800, 600, camera);
        
        BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
        
        // Run 100 spawn cycles
        for (int cycle = 0; cycle < 100; cycle++) {
            // Get current spawn interval
            float spawnInterval = manager.getNextSpawnInterval();
            
            // Verify the interval is within bounds
            assertTrue(
                spawnInterval >= MIN_SPAWN_INTERVAL && spawnInterval <= MAX_SPAWN_INTERVAL,
                "Cycle " + cycle + ": Spawn interval " + spawnInterval + 
                " should be between " + MIN_SPAWN_INTERVAL + " and " + MAX_SPAWN_INTERVAL + " seconds"
            );
            
            // Simulate time passing until spawn
            float timeToSpawn = manager.getSpawnTimer();
            manager.update(timeToSpawn + 0.1f, 0, 0);
            
            // Wait for formation to complete (simulate crossing screen)
            // Update multiple times to ensure formation reaches target
            for (int i = 0; i < 200; i++) {
                manager.update(0.1f, 0, 0);
                if (manager.getActiveFormation() == null) {
                    break;
                }
            }
        }
        
        // Clean up
        manager.dispose();
    }
    
    /**
     * Property: Spawn interval distribution covers the full range
     * For a large number of spawn events, the generated intervals should cover
     * the full range from 180 to 300 seconds, not just cluster at one end.
     * 
     * This property-based test runs 1000 trials and verifies that intervals
     * are distributed across the valid range.
     */
    @Test
    public void spawnIntervalDistributionCoversRange() {
        float minObserved = Float.MAX_VALUE;
        float maxObserved = Float.MIN_VALUE;
        
        // Run 1000 trials to get good distribution
        for (int trial = 0; trial < 1000; trial++) {
            OrthographicCamera camera = new OrthographicCamera();
            camera.setToOrtho(false, 800, 600);
            Viewport viewport = new FitViewport(800, 600, camera);
            
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            float spawnInterval = manager.getNextSpawnInterval();
            
            minObserved = Math.min(minObserved, spawnInterval);
            maxObserved = Math.max(maxObserved, spawnInterval);
            
            manager.dispose();
        }
        
        // Verify we observed values close to both bounds
        // With 1000 trials, we should get within 10% of each bound
        float lowerThreshold = MIN_SPAWN_INTERVAL + (MAX_SPAWN_INTERVAL - MIN_SPAWN_INTERVAL) * 0.1f;
        float upperThreshold = MAX_SPAWN_INTERVAL - (MAX_SPAWN_INTERVAL - MIN_SPAWN_INTERVAL) * 0.1f;
        
        assertTrue(
            minObserved < lowerThreshold,
            "Minimum observed interval " + minObserved + 
            " should be close to lower bound " + MIN_SPAWN_INTERVAL
        );
        
        assertTrue(
            maxObserved > upperThreshold,
            "Maximum observed interval " + maxObserved + 
            " should be close to upper bound " + MAX_SPAWN_INTERVAL
        );
    }
}
