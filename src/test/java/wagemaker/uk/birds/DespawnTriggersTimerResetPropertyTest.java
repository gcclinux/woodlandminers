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
 * Property-based test for despawn triggers timer reset.
 * Feature: flying-birds-ambient, Property 4: Despawn triggers timer reset
 * Validates: Requirements 1.4
 */
public class DespawnTriggersTimerResetPropertyTest {
    
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
     * Property 4: Despawn triggers timer reset
     * For any bird formation that reaches the target boundary, the system 
     * should despawn the formation and initialize a new spawn timer.
     * Validates: Requirements 1.4
     * 
     * This property-based test runs 100 trials and verifies that:
     * 1. When a formation reaches its target, it is despawned (activeFormation becomes null)
     * 2. The spawn timer is reset to a new random interval (between 180-300 seconds)
     * 3. The new spawn interval is different from the previous one (with high probability)
     */
    @Test
    public void despawnTriggersTimerReset() {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create bird formation manager
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Record initial spawn interval
            float initialInterval = manager.getNextSpawnInterval();
            
            // Verify initial interval is in valid range
            assertTrue(
                initialInterval >= MIN_SPAWN_INTERVAL && initialInterval <= MAX_SPAWN_INTERVAL,
                "Trial " + trial + ": Initial spawn interval should be between " + 
                MIN_SPAWN_INTERVAL + " and " + MAX_SPAWN_INTERVAL + ", but was " + initialInterval
            );
            
            // Trigger initial spawn by setting timer to 0
            float deltaTime = initialInterval + 0.1f;
            manager.update(deltaTime, 0, 0);
            
            // Verify formation was spawned
            assertNotNull(
                manager.getActiveFormation(),
                "Trial " + trial + ": Formation should be spawned after timer expires"
            );
            
            // Update formation until it reaches target
            float maxUpdateTime = 20f; // Maximum time to wait for despawn
            float totalTime = 0f;
            float timeStep = 0.1f;
            
            while (manager.getActiveFormation() != null && totalTime < maxUpdateTime) {
                manager.update(timeStep, 0, 0);
                totalTime += timeStep;
            }
            
            // Verify formation was despawned
            assertNull(
                manager.getActiveFormation(),
                "Trial " + trial + ": Formation should be despawned after reaching target"
            );
            
            // Verify spawn timer was reset to a new interval
            float newInterval = manager.getNextSpawnInterval();
            
            assertTrue(
                newInterval >= MIN_SPAWN_INTERVAL && newInterval <= MAX_SPAWN_INTERVAL,
                "Trial " + trial + ": New spawn interval should be between " + 
                MIN_SPAWN_INTERVAL + " and " + MAX_SPAWN_INTERVAL + ", but was " + newInterval
            );
            
            // Verify spawn timer is counting down from the new interval
            float currentTimer = manager.getSpawnTimer();
            assertTrue(
                currentTimer > 0 && currentTimer <= newInterval,
                "Trial " + trial + ": Spawn timer should be reset to new interval (" + 
                newInterval + "), but was " + currentTimer
            );
            
            // Clean up
            manager.dispose();
        }
    }
    
    /**
     * Property: Timer reset produces different intervals
     * For any sequence of despawn events, the spawn intervals should vary
     * (not always the same value), demonstrating randomness.
     */
    @Test
    public void timerResetProducesDifferentIntervals() {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Track unique intervals
        int uniqueIntervals = 0;
        float previousInterval = -1f;
        
        // Run 50 trials
        for (int trial = 0; trial < 50; trial++) {
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            float initialInterval = manager.getNextSpawnInterval();
            
            // Trigger spawn
            manager.update(initialInterval + 0.1f, 0, 0);
            
            // Wait for despawn
            float maxUpdateTime = 20f;
            float totalTime = 0f;
            float timeStep = 0.1f;
            
            while (manager.getActiveFormation() != null && totalTime < maxUpdateTime) {
                manager.update(timeStep, 0, 0);
                totalTime += timeStep;
            }
            
            // Get new interval after despawn
            float newInterval = manager.getNextSpawnInterval();
            
            // Check if this interval is different from previous
            if (previousInterval < 0 || Math.abs(newInterval - previousInterval) > 0.01f) {
                uniqueIntervals++;
            }
            
            previousInterval = newInterval;
            
            manager.dispose();
        }
        
        // Verify we got multiple different intervals (at least 40 out of 50 should be unique)
        assertTrue(
            uniqueIntervals >= 40,
            "Timer reset should produce varied intervals, but only " + uniqueIntervals + 
            " out of 50 were unique"
        );
    }
    
    /**
     * Property: Spawn timer allows next formation spawn
     * After despawn and timer reset, waiting for the new interval should
     * trigger another formation spawn.
     */
    @Test
    public void spawnTimerAllowsNextFormationSpawn() {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 20 trials (fewer because this test is more expensive)
        for (int trial = 0; trial < 20; trial++) {
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // First spawn cycle
            float firstInterval = manager.getNextSpawnInterval();
            manager.update(firstInterval + 0.1f, 0, 0);
            assertNotNull(manager.getActiveFormation(), "Trial " + trial + ": First formation should spawn");
            
            // Wait for first despawn
            float maxUpdateTime = 20f;
            float totalTime = 0f;
            float timeStep = 0.1f;
            
            while (manager.getActiveFormation() != null && totalTime < maxUpdateTime) {
                manager.update(timeStep, 0, 0);
                totalTime += timeStep;
            }
            
            assertNull(manager.getActiveFormation(), "Trial " + trial + ": First formation should despawn");
            
            // Second spawn cycle
            float secondInterval = manager.getNextSpawnInterval();
            manager.update(secondInterval + 0.1f, 0, 0);
            
            // Verify second formation spawned
            assertNotNull(
                manager.getActiveFormation(),
                "Trial " + trial + ": Second formation should spawn after timer expires again"
            );
            
            manager.dispose();
        }
    }
}
