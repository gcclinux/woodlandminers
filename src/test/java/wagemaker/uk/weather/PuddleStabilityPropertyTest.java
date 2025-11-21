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
 * Property-based tests for puddle stability during rain.
 * Feature: rain-water-puddles, Property 2: Puddle stability during rain
 * Validates: Requirements 1.2
 */
public class PuddleStabilityPropertyTest {
    
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
     * Property 2: Puddle stability during rain
     * For any active rain period after accumulation, the set of puddle positions 
     * should remain stable (puddles don't randomly disappear or teleport).
     * Validates: Requirements 1.2
     * 
     * This property-based test runs 100 trials with random rain durations,
     * verifying that puddle count remains stable during active rain.
     */
    @Test
    public void puddleCountRemainsStableDuringRain() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            PuddleManager manager = new PuddleManager(shapeRenderer);
            manager.initialize();
            
            // Accumulate to threshold
            float accumulationTime = 0.0f;
            while (accumulationTime < PuddleConfig.ACCUMULATION_THRESHOLD) {
                float deltaTime = 0.1f;
                manager.update(deltaTime, true, 1.0f, camera);
                accumulationTime += deltaTime;
            }
            
            // Ensure we're in ACTIVE state
            manager.update(0.1f, true, 1.0f, camera);
            assertEquals(
                PuddleState.ACTIVE,
                manager.getCurrentState(),
                "Should be in ACTIVE state after accumulation"
            );
            
            // Record initial puddle count
            int initialCount = manager.getActivePuddleCount();
            assertTrue(
                initialCount > 0,
                "Should have puddles after accumulation"
            );
            
            // Continue rain for random duration (1-10 seconds)
            float rainDuration = 1.0f + random.nextFloat() * 9.0f;
            float totalTime = 0.0f;
            
            while (totalTime < rainDuration) {
                float deltaTime = 0.01f + random.nextFloat() * 0.1f; // 0.01 to 0.11 seconds
                
                manager.update(deltaTime, true, 1.0f, camera);
                totalTime += deltaTime;
                
                // Verify puddle count remains stable
                int currentCount = manager.getActivePuddleCount();
                assertEquals(
                    initialCount,
                    currentCount,
                    String.format(
                        "Puddle count should remain stable during rain. " +
                        "Initial: %d, Current: %d at time %.3f",
                        initialCount, currentCount, totalTime
                    )
                );
                
                // Verify state remains ACTIVE
                assertEquals(
                    PuddleState.ACTIVE,
                    manager.getCurrentState(),
                    "State should remain ACTIVE during rain"
                );
            }
            
            manager.dispose();
        }
    }
    
    /**
     * Property: Puddle count stability across multiple update cycles
     * For any sequence of updates during active rain, the puddle count
     * should not change.
     * 
     * This property-based test runs 100 trials with varying update patterns.
     */
    @Test
    public void puddleCountStableAcrossMultipleUpdates() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            PuddleManager manager = new PuddleManager(shapeRenderer);
            manager.initialize();
            
            // Reach ACTIVE state by accumulating in smaller steps
            float accumulatedTime = 0.0f;
            while (manager.getCurrentState() != PuddleState.ACTIVE && accumulatedTime < 10.0f) {
                manager.update(0.1f, true, 1.0f, camera);
                accumulatedTime += 0.1f;
            }
            
            assertEquals(
                PuddleState.ACTIVE,
                manager.getCurrentState(),
                "Should be in ACTIVE state after accumulation"
            );
            
            int initialCount = manager.getActivePuddleCount();
            
            // Perform random number of updates (10-100)
            int numUpdates = 10 + random.nextInt(90);
            
            for (int i = 0; i < numUpdates; i++) {
                float deltaTime = 0.01f + random.nextFloat() * 0.2f;
                manager.update(deltaTime, true, 1.0f, camera);
                
                int currentCount = manager.getActivePuddleCount();
                assertEquals(
                    initialCount,
                    currentCount,
                    String.format(
                        "Puddle count should remain stable. " +
                        "Initial: %d, Current: %d at update %d",
                        initialCount, currentCount, i
                    )
                );
            }
            
            manager.dispose();
        }
    }
    
    /**
     * Property: State remains ACTIVE during continuous rain
     * For any continuous rain period after accumulation, the state
     * should remain ACTIVE and not transition unexpectedly.
     * 
     * This property-based test runs 100 trials with random rain durations.
     */
    @Test
    public void stateRemainsActiveDuringContinuousRain() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            PuddleManager manager = new PuddleManager(shapeRenderer);
            manager.initialize();
            
            // Reach ACTIVE state by accumulating in smaller steps
            float accumulatedTime = 0.0f;
            while (manager.getCurrentState() != PuddleState.ACTIVE && accumulatedTime < 10.0f) {
                manager.update(0.1f, true, 1.0f, camera);
                accumulatedTime += 0.1f;
            }
            
            assertEquals(
                PuddleState.ACTIVE,
                manager.getCurrentState(),
                "Should be in ACTIVE state after accumulation"
            );
            
            // Continue rain for random duration (1-20 seconds)
            float rainDuration = 1.0f + random.nextFloat() * 19.0f;
            float totalTime = 0.0f;
            
            while (totalTime < rainDuration) {
                float deltaTime = 0.05f + random.nextFloat() * 0.15f;
                
                manager.update(deltaTime, true, 1.0f, camera);
                totalTime += deltaTime;
                
                // Verify state remains ACTIVE
                assertEquals(
                    PuddleState.ACTIVE,
                    manager.getCurrentState(),
                    String.format(
                        "State should remain ACTIVE during continuous rain at time %.3f",
                        totalTime
                    )
                );
            }
            
            manager.dispose();
        }
    }
    
    /**
     * Property: Puddles don't disappear during active rain
     * For any puddle that exists when rain is active, it should continue
     * to exist as long as rain continues.
     * 
     * This property-based test runs 100 trials, verifying puddle persistence.
     */
    @Test
    public void puddlesDontDisappearDuringActiveRain() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            PuddleManager manager = new PuddleManager(shapeRenderer);
            manager.initialize();
            
            // Reach ACTIVE state by accumulating in smaller steps
            float accumulatedTime = 0.0f;
            while (manager.getCurrentState() != PuddleState.ACTIVE && accumulatedTime < 10.0f) {
                manager.update(0.1f, true, 1.0f, camera);
                accumulatedTime += 0.1f;
            }
            
            int initialCount = manager.getActivePuddleCount();
            assertTrue(
                initialCount > 0,
                "Should have puddles in ACTIVE state after accumulation"
            );
            
            // Random number of update cycles (50-200)
            int numCycles = 50 + random.nextInt(150);
            
            for (int i = 0; i < numCycles; i++) {
                float deltaTime = 0.01f + random.nextFloat() * 0.05f;
                manager.update(deltaTime, true, 1.0f, camera);
                
                int currentCount = manager.getActivePuddleCount();
                
                // Puddles should never decrease during active rain
                assertTrue(
                    currentCount >= initialCount,
                    String.format(
                        "Puddle count should not decrease during rain. " +
                        "Initial: %d, Current: %d at cycle %d",
                        initialCount, currentCount, i
                    )
                );
                
                // In fact, for this implementation, count should be exactly the same
                assertEquals(
                    initialCount,
                    currentCount,
                    String.format(
                        "Puddle count should remain exactly the same. " +
                        "Initial: %d, Current: %d at cycle %d",
                        initialCount, currentCount, i
                    )
                );
            }
            
            manager.dispose();
        }
    }
}
