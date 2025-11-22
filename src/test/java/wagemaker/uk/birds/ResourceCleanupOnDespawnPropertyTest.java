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
import static org.mockito.Mockito.*;

/**
 * Property-based test for resource cleanup on despawn.
 * Feature: flying-birds-ambient, Property 15: Resource cleanup on despawn
 * Validates: Requirements 5.4
 */
public class ResourceCleanupOnDespawnPropertyTest {
    
    private static HeadlessApplication application;
    
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
    }
    
    @AfterAll
    public static void tearDownGdx() {
        if (application != null) {
            application.exit();
        }
    }
    
    /**
     * Property 15: Resource cleanup on despawn
     * For any bird formation that despawns, all associated textures and resources 
     * should be properly disposed.
     * Validates: Requirements 5.4
     * 
     * This property-based test runs 100 trials and verifies that:
     * 1. When a formation despawns, the formation's dispose() method is called
     * 2. The formation is set to null (no longer active)
     * 3. The birds list is cleared (no memory leaks)
     * 4. The manager can continue to spawn new formations after cleanup
     */
    @Test
    public void resourceCleanupOnDespawn() {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create a mock texture that we can verify disposal on
            Texture mockTexture = Mockito.mock(Texture.class);
            
            // Create bird formation manager
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Trigger spawn
            float initialInterval = manager.getNextSpawnInterval();
            manager.update(initialInterval + 0.1f, 0, 0);
            
            // Verify formation was spawned
            BirdFormation formation = manager.getActiveFormation();
            assertNotNull(
                formation,
                "Trial " + trial + ": Formation should be spawned"
            );
            
            // Verify formation has 5 birds
            assertEquals(
                5,
                formation.getBirds().size(),
                "Trial " + trial + ": Formation should have 5 birds before despawn"
            );
            
            // Update formation until it reaches target (despawns)
            float maxUpdateTime = 20f;
            float totalTime = 0f;
            float timeStep = 0.1f;
            
            while (manager.getActiveFormation() != null && totalTime < maxUpdateTime) {
                manager.update(timeStep, 0, 0);
                totalTime += timeStep;
            }
            
            // Verify formation was despawned (set to null)
            assertNull(
                manager.getActiveFormation(),
                "Trial " + trial + ": Formation should be null after despawn (resource cleanup)"
            );
            
            // Verify the formation's birds list was cleared during dispose
            assertEquals(
                0,
                formation.getBirds().size(),
                "Trial " + trial + ": Formation's birds list should be cleared after dispose"
            );
            
            // Clean up manager
            manager.dispose();
            
            // Verify manager's texture disposal was called (twice - once for each animation frame)
            verify(mockTexture, times(2)).dispose();
        }
    }
    
    /**
     * Property: Manager dispose cleans up all resources
     * For any manager state (with or without active formation), calling dispose()
     * should clean up all resources including the shared texture.
     */
    @Test
    public void managerDisposeCleanupAllResources() {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 50 trials
        for (int trial = 0; trial < 50; trial++) {
            Texture mockTexture = Mockito.mock(Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Randomly decide whether to spawn a formation before disposing
            boolean shouldSpawn = (trial % 2 == 0);
            
            if (shouldSpawn) {
                // Trigger spawn
                float initialInterval = manager.getNextSpawnInterval();
                manager.update(initialInterval + 0.1f, 0, 0);
                
                // Verify formation exists
                assertNotNull(
                    manager.getActiveFormation(),
                    "Trial " + trial + ": Formation should exist before manager disposal"
                );
            }
            
            // Dispose manager
            manager.dispose();
            
            // Verify texture was disposed twice (once for each animation frame)
            verify(mockTexture, times(2)).dispose();
            
            // If there was an active formation, verify it was cleaned up
            if (shouldSpawn) {
                BirdFormation formation = manager.getActiveFormation();
                if (formation != null) {
                    assertEquals(
                        0,
                        formation.getBirds().size(),
                        "Trial " + trial + ": Formation birds should be cleared after manager disposal"
                    );
                }
            }
        }
    }
    
    /**
     * Property: Multiple spawn-despawn cycles don't leak resources
     * For any sequence of spawn-despawn cycles, resources should be properly
     * cleaned up each time without accumulation.
     */
    @Test
    public void multipleSpawnDespawnCyclesNoResourceLeaks() {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 20 trials (fewer because each trial does multiple cycles)
        for (int trial = 0; trial < 20; trial++) {
            Texture mockTexture = Mockito.mock(Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Perform 3 spawn-despawn cycles
            for (int cycle = 0; cycle < 3; cycle++) {
                // Trigger spawn
                float interval = manager.getNextSpawnInterval();
                manager.update(interval + 0.1f, 0, 0);
                
                BirdFormation formation = manager.getActiveFormation();
                assertNotNull(
                    formation,
                    "Trial " + trial + ", Cycle " + cycle + ": Formation should spawn"
                );
                
                // Verify formation has 5 birds
                assertEquals(
                    5,
                    formation.getBirds().size(),
                    "Trial " + trial + ", Cycle " + cycle + ": Formation should have 5 birds"
                );
                
                // Wait for despawn
                float maxUpdateTime = 20f;
                float totalTime = 0f;
                float timeStep = 0.1f;
                
                while (manager.getActiveFormation() != null && totalTime < maxUpdateTime) {
                    manager.update(timeStep, 0, 0);
                    totalTime += timeStep;
                }
                
                // Verify despawn and cleanup
                assertNull(
                    manager.getActiveFormation(),
                    "Trial " + trial + ", Cycle " + cycle + ": Formation should despawn"
                );
                
                assertEquals(
                    0,
                    formation.getBirds().size(),
                    "Trial " + trial + ", Cycle " + cycle + ": Birds list should be cleared"
                );
            }
            
            // Final cleanup
            manager.dispose();
            
            // Verify texture disposed twice (once for each animation frame, shared across all cycles)
            verify(mockTexture, times(2)).dispose();
        }
    }
    
    /**
     * Property: Formation dispose is idempotent
     * For any formation, calling dispose() multiple times should be safe
     * and not cause errors.
     */
    @Test
    public void formationDisposeIsIdempotent() {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, viewWidth, viewHeight);
        Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
        
        // Run 50 trials
        for (int trial = 0; trial < 50; trial++) {
            Texture mockTexture = Mockito.mock(Texture.class);
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Trigger spawn
            float interval = manager.getNextSpawnInterval();
            manager.update(interval + 0.1f, 0, 0);
            
            BirdFormation formation = manager.getActiveFormation();
            assertNotNull(formation, "Trial " + trial + ": Formation should spawn");
            
            // Call dispose multiple times
            formation.dispose();
            formation.dispose();
            formation.dispose();
            
            // Verify birds list is cleared and stays cleared
            assertEquals(
                0,
                formation.getBirds().size(),
                "Trial " + trial + ": Birds list should remain cleared after multiple dispose calls"
            );
            
            // Manager cleanup
            manager.dispose();
        }
    }
}
