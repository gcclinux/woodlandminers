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
 * Property-based tests for complete cleanup after evaporation.
 * Feature: rain-water-puddles, Property 7: Complete cleanup after evaporation
 * Validates: Requirements 2.3
 */
public class PuddleCleanupPropertyTest {
    
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
     * Property 7: Complete cleanup after evaporation
     * For any evaporation cycle, after the evaporation duration completes, 
     * the active puddle count should be zero.
     * Validates: Requirements 2.3
     * 
     * This property-based test runs 100 trials with random time increments,
     * verifying that all puddles are cleaned up after evaporation.
     */
    @Test
    public void allPuddlesCleanedUpAfterEvaporation() {
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
            
            int puddleCountBeforeEvaporation = manager.getActivePuddleCount();
            assertTrue(
                puddleCountBeforeEvaporation > 0,
                "Should have puddles before evaporation"
            );
            
            // Stop rain to start evaporation
            manager.update(0.01f, false, 0.0f, camera);
            assertEquals(PuddleState.EVAPORATING, manager.getCurrentState());
            
            // Continue until evaporation completes
            while (manager.getCurrentState() == PuddleState.EVAPORATING) {
                float deltaTime = 0.01f + random.nextFloat() * 0.1f;
                manager.update(deltaTime, false, 0.0f, camera);
            }
            
            // Verify state is NONE
            assertEquals(
                PuddleState.NONE,
                manager.getCurrentState(),
                "State should be NONE after evaporation completes"
            );
            
            // Verify all puddles are cleaned up
            assertEquals(
                0,
                manager.getActivePuddleCount(),
                String.format(
                    "All puddles should be cleaned up after evaporation. " +
                    "Had %d puddles before, now have %d",
                    puddleCountBeforeEvaporation, manager.getActivePuddleCount()
                )
            );
            
            manager.dispose();
        }
    }
    
    /**
     * Property: Multiple evaporation cycles clean up properly
     * For any sequence of rain/evaporation cycles, each evaporation should
     * completely clean up puddles.
     * 
     * This property-based test runs 100 trials with multiple cycles.
     */
    @Test
    public void multipleEvaporationCyclesCleanUpProperly() {
        Random random = new Random(42);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            PuddleManager manager = new PuddleManager(shapeRenderer);
            manager.initialize();
            
            // Perform multiple rain/evaporation cycles (2-5 cycles)
            int numCycles = 2 + random.nextInt(4);
            
            for (int cycle = 0; cycle < numCycles; cycle++) {
                // Start rain and reach ACTIVE state
                float accumulatedTime = 0.0f;
                while (manager.getCurrentState() != PuddleState.ACTIVE && accumulatedTime < 10.0f) {
                    manager.update(0.1f, true, 1.0f, camera);
                    accumulatedTime += 0.1f;
                }
                
                assertTrue(
                    manager.getActivePuddleCount() > 0,
                    String.format("Should have puddles in cycle %d", cycle)
                );
                
                // Stop rain and complete evaporation
                manager.update(0.01f, false, 0.0f, camera);
                
                while (manager.getCurrentState() == PuddleState.EVAPORATING) {
                    manager.update(0.1f, false, 0.0f, camera);
                }
                
                // Verify cleanup
                assertEquals(
                    PuddleState.NONE,
                    manager.getCurrentState(),
                    String.format("Should be NONE after cycle %d", cycle)
                );
                
                assertEquals(
                    0,
                    manager.getActivePuddleCount(),
                    String.format("Should have no puddles after cycle %d", cycle)
                );
            }
            
            manager.dispose();
        }
    }
    
    /**
     * Property: State transitions to NONE after cleanup
     * For any evaporation completion, the state should transition to NONE
     * and remain there until rain starts again.
     * 
     * This property-based test runs 100 trials.
     */
    @Test
    public void stateTransitionsToNoneAfterCleanup() {
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
            
            // Stop rain and complete evaporation
            manager.update(0.01f, false, 0.0f, camera);
            
            while (manager.getCurrentState() == PuddleState.EVAPORATING) {
                manager.update(0.1f, false, 0.0f, camera);
            }
            
            // Verify state is NONE
            assertEquals(PuddleState.NONE, manager.getCurrentState());
            
            // Continue updating without rain (random number of updates)
            int numUpdates = 10 + random.nextInt(40);
            
            for (int i = 0; i < numUpdates; i++) {
                manager.update(0.1f, false, 0.0f, camera);
                
                // State should remain NONE
                assertEquals(
                    PuddleState.NONE,
                    manager.getCurrentState(),
                    String.format("State should remain NONE at update %d", i)
                );
                
                // Puddle count should remain 0
                assertEquals(
                    0,
                    manager.getActivePuddleCount(),
                    String.format("Puddle count should remain 0 at update %d", i)
                );
            }
            
            manager.dispose();
        }
    }
}
