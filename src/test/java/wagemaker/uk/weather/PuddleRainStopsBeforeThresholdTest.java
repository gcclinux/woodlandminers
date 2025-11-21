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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for rain stopping before accumulation threshold.
 * Validates: Requirements 4.4
 */
public class PuddleRainStopsBeforeThresholdTest {
    
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
     * Test that puddles don't spawn if rain stops before 5 seconds.
     * Validates: Requirements 4.4
     */
    @Test
    public void rainStopsBeforeThresholdNoPuddlesSpawn() {
        PuddleManager manager = new PuddleManager(shapeRenderer);
        manager.initialize();
        
        // Start rain
        manager.update(0.1f, true, 1.0f, camera);
        assertEquals(PuddleState.ACCUMULATING, manager.getCurrentState());
        
        // Let rain accumulate for 3 seconds (less than 5 second threshold)
        manager.update(3.0f, true, 1.0f, camera);
        assertEquals(PuddleState.ACCUMULATING, manager.getCurrentState());
        
        // Verify we're still accumulating and haven't reached threshold
        float progress = manager.getAccumulationProgress();
        assertTrue(progress < 1.0f, "Should not have reached threshold yet. Progress: " + progress);
        assertTrue(progress > 0.5f, "Should have made significant progress. Progress: " + progress);
        
        // Stop rain before threshold
        manager.update(0.1f, false, 0.0f, camera);
        
        // Verify state transitioned to NONE (not EVAPORATING)
        assertEquals(
            PuddleState.NONE,
            manager.getCurrentState(),
            "State should transition to NONE when rain stops before threshold"
        );
        
        // Verify no puddles were spawned
        assertEquals(
            0,
            manager.getActivePuddleCount(),
            "No puddles should be spawned when rain stops before threshold"
        );
        
        // Verify accumulation progress is reset
        assertEquals(
            0.0f,
            manager.getAccumulationProgress(),
            0.001f,
            "Accumulation progress should be reset"
        );
        
        manager.dispose();
    }
    
    /**
     * Test rain stopping at various points before threshold.
     */
    @Test
    public void rainStopsAtVariousPointsBeforeThreshold() {
        // Test durations that are clearly before the 5 second threshold
        float[] testDurations = {0.5f, 1.0f, 2.0f, 3.5f, 4.5f};
        
        for (float duration : testDurations) {
            PuddleManager manager = new PuddleManager(shapeRenderer);
            manager.initialize();
            
            // Start rain and accumulate for specified duration
            manager.update(0.1f, true, 1.0f, camera);
            manager.update(duration, true, 1.0f, camera);
            
            assertEquals(
                PuddleState.ACCUMULATING,
                manager.getCurrentState(),
                "Should be accumulating at " + duration + " seconds"
            );
            
            // Stop rain
            manager.update(0.1f, false, 0.0f, camera);
            
            // Verify no puddles spawned
            assertEquals(
                PuddleState.NONE,
                manager.getCurrentState(),
                "Should be NONE after stopping at " + duration + " seconds"
            );
            
            assertEquals(
                0,
                manager.getActivePuddleCount(),
                "No puddles at " + duration + " seconds"
            );
            
            manager.dispose();
        }
    }
    
    /**
     * Test rain stopping exactly at threshold boundary.
     */
    @Test
    public void rainStopsExactlyAtThreshold() {
        PuddleManager manager = new PuddleManager(shapeRenderer);
        manager.initialize();
        
        // Start rain
        manager.update(0.1f, true, 1.0f, camera);
        
        // Accumulate for exactly 5 seconds
        manager.update(5.0f, true, 1.0f, camera);
        
        // Should have transitioned to ACTIVE and spawned puddles
        assertEquals(PuddleState.ACTIVE, manager.getCurrentState());
        assertTrue(manager.getActivePuddleCount() > 0, "Should have puddles at threshold");
        
        manager.dispose();
    }
    
    /**
     * Test multiple short rain bursts that don't reach threshold.
     */
    @Test
    public void multipleShortRainBurstsNoThreshold() {
        PuddleManager manager = new PuddleManager(shapeRenderer);
        manager.initialize();
        
        // Simulate multiple short rain bursts
        for (int i = 0; i < 5; i++) {
            // Start rain
            manager.update(0.1f, true, 1.0f, camera);
            assertEquals(PuddleState.ACCUMULATING, manager.getCurrentState());
            
            // Rain for 2 seconds
            manager.update(2.0f, true, 1.0f, camera);
            assertEquals(PuddleState.ACCUMULATING, manager.getCurrentState());
            
            // Stop rain
            manager.update(0.1f, false, 0.0f, camera);
            assertEquals(PuddleState.NONE, manager.getCurrentState());
            
            // Verify no puddles
            assertEquals(0, manager.getActivePuddleCount());
            
            // Wait a bit before next burst
            manager.update(1.0f, false, 0.0f, camera);
        }
        
        manager.dispose();
    }
    
    /**
     * Test that accumulation timer resets when rain stops before threshold.
     */
    @Test
    public void accumulationTimerResetsWhenRainStopsEarly() {
        PuddleManager manager = new PuddleManager(shapeRenderer);
        manager.initialize();
        
        // Start rain and accumulate partway
        manager.update(0.1f, true, 1.0f, camera);
        manager.update(3.0f, true, 1.0f, camera);
        
        float progressBefore = manager.getAccumulationProgress();
        assertTrue(progressBefore > 0.5f, "Should have progress");
        
        // Stop rain
        manager.update(0.1f, false, 0.0f, camera);
        assertEquals(PuddleState.NONE, manager.getCurrentState());
        
        // Start rain again
        manager.update(0.1f, true, 1.0f, camera);
        assertEquals(PuddleState.ACCUMULATING, manager.getCurrentState());
        
        // Progress should have reset and be near zero
        float progressAfter = manager.getAccumulationProgress();
        assertTrue(
            progressAfter < 0.1f,
            "Progress should reset after rain stops early. Got: " + progressAfter
        );
        
        manager.dispose();
    }
}
