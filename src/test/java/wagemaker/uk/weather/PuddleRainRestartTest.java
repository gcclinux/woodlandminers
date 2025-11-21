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
 * Unit tests for rain restart during evaporation.
 * Validates: Requirements 2.4
 */
public class PuddleRainRestartTest {
    
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
     * Test that rain restarting during evaporation restores puddles to full visibility.
     * Validates: Requirements 2.4
     */
    @Test
    public void rainRestartDuringEvaporationRestoresPuddles() {
        PuddleManager manager = new PuddleManager(shapeRenderer);
        manager.initialize();
        
        // Reach ACTIVE state
        float accumulatedTime = 0.0f;
        while (manager.getCurrentState() != PuddleState.ACTIVE && accumulatedTime < 10.0f) {
            manager.update(0.1f, true, 1.0f, camera);
            accumulatedTime += 0.1f;
        }
        
        assertEquals(PuddleState.ACTIVE, manager.getCurrentState());
        int initialPuddleCount = manager.getActivePuddleCount();
        assertTrue(initialPuddleCount > 0, "Should have puddles in ACTIVE state");
        
        // Stop rain to start evaporation
        manager.update(0.1f, false, 0.0f, camera);
        assertEquals(PuddleState.EVAPORATING, manager.getCurrentState());
        
        // Let evaporation progress partway (about 2 seconds out of 5)
        manager.update(2.0f, false, 0.0f, camera);
        assertEquals(PuddleState.EVAPORATING, manager.getCurrentState());
        
        // Verify evaporation has progressed
        float evaporationProgress = manager.getEvaporationProgress();
        assertTrue(
            evaporationProgress > 0.3f && evaporationProgress < 0.5f,
            "Evaporation should have progressed partway. Got: " + evaporationProgress
        );
        
        // Restart rain
        manager.update(0.1f, true, 1.0f, camera);
        
        // Verify state transitioned back to ACTIVE
        assertEquals(
            PuddleState.ACTIVE,
            manager.getCurrentState(),
            "State should transition back to ACTIVE when rain restarts"
        );
        
        // Verify puddle count is preserved
        assertEquals(
            initialPuddleCount,
            manager.getActivePuddleCount(),
            "Puddle count should be preserved when rain restarts"
        );
        
        // Verify evaporation progress is reset
        assertEquals(
            0.0f,
            manager.getEvaporationProgress(),
            0.001f,
            "Evaporation progress should be reset when rain restarts"
        );
        
        manager.dispose();
    }
    
    /**
     * Test that rain restarting early in evaporation works correctly.
     */
    @Test
    public void rainRestartEarlyInEvaporation() {
        PuddleManager manager = new PuddleManager(shapeRenderer);
        manager.initialize();
        
        // Reach ACTIVE state
        float accumulatedTime = 0.0f;
        while (manager.getCurrentState() != PuddleState.ACTIVE && accumulatedTime < 10.0f) {
            manager.update(0.1f, true, 1.0f, camera);
            accumulatedTime += 0.1f;
        }
        
        int initialPuddleCount = manager.getActivePuddleCount();
        
        // Stop rain
        manager.update(0.1f, false, 0.0f, camera);
        assertEquals(PuddleState.EVAPORATING, manager.getCurrentState());
        
        // Evaporate for just 0.5 seconds
        manager.update(0.5f, false, 0.0f, camera);
        
        // Restart rain immediately
        manager.update(0.1f, true, 1.0f, camera);
        
        // Verify restoration
        assertEquals(PuddleState.ACTIVE, manager.getCurrentState());
        assertEquals(initialPuddleCount, manager.getActivePuddleCount());
        
        manager.dispose();
    }
    
    /**
     * Test that rain restarting late in evaporation works correctly.
     */
    @Test
    public void rainRestartLateInEvaporation() {
        PuddleManager manager = new PuddleManager(shapeRenderer);
        manager.initialize();
        
        // Reach ACTIVE state
        float accumulatedTime = 0.0f;
        while (manager.getCurrentState() != PuddleState.ACTIVE && accumulatedTime < 10.0f) {
            manager.update(0.1f, true, 1.0f, camera);
            accumulatedTime += 0.1f;
        }
        
        int initialPuddleCount = manager.getActivePuddleCount();
        
        // Stop rain
        manager.update(0.1f, false, 0.0f, camera);
        assertEquals(PuddleState.EVAPORATING, manager.getCurrentState());
        
        // Evaporate for 4.5 seconds (almost complete)
        manager.update(4.5f, false, 0.0f, camera);
        assertEquals(PuddleState.EVAPORATING, manager.getCurrentState());
        
        // Restart rain just before completion
        manager.update(0.1f, true, 1.0f, camera);
        
        // Verify restoration
        assertEquals(PuddleState.ACTIVE, manager.getCurrentState());
        assertEquals(initialPuddleCount, manager.getActivePuddleCount());
        
        manager.dispose();
    }
    
    /**
     * Test multiple rain restart cycles.
     */
    @Test
    public void multipleRainRestartCycles() {
        PuddleManager manager = new PuddleManager(shapeRenderer);
        manager.initialize();
        
        // Reach ACTIVE state
        float accumulatedTime = 0.0f;
        while (manager.getCurrentState() != PuddleState.ACTIVE && accumulatedTime < 10.0f) {
            manager.update(0.1f, true, 1.0f, camera);
            accumulatedTime += 0.1f;
        }
        
        int initialPuddleCount = manager.getActivePuddleCount();
        
        // Perform multiple stop/restart cycles
        for (int cycle = 0; cycle < 3; cycle++) {
            // Stop rain
            manager.update(0.1f, false, 0.0f, camera);
            assertEquals(PuddleState.EVAPORATING, manager.getCurrentState());
            
            // Evaporate partway
            manager.update(1.0f + cycle * 0.5f, false, 0.0f, camera);
            assertEquals(PuddleState.EVAPORATING, manager.getCurrentState());
            
            // Restart rain
            manager.update(0.1f, true, 1.0f, camera);
            
            // Verify restoration
            assertEquals(
                PuddleState.ACTIVE,
                manager.getCurrentState(),
                "Should be ACTIVE after restart in cycle " + cycle
            );
            
            assertEquals(
                initialPuddleCount,
                manager.getActivePuddleCount(),
                "Puddle count should be preserved in cycle " + cycle
            );
            
            // Continue rain for a bit
            manager.update(1.0f, true, 1.0f, camera);
            assertEquals(PuddleState.ACTIVE, manager.getCurrentState());
        }
        
        manager.dispose();
    }
}
