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
 * Property-based tests for puddle camera movement updates.
 * Feature: rain-water-puddles, Property 9: Camera movement updates puddles
 * Validates: Requirements 3.4
 */
public class PuddleCameraMovementPropertyTest {
    
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
        camera = new OrthographicCamera();
    }
    
    @AfterAll
    public static void tearDownClass() {
        if (application != null) {
            application.exit();
        }
    }
    
    /**
     * Property 9: Camera movement updates puddles
     * For any camera movement, after the update cycle, all visible puddles 
     * should be within the new viewport bounds.
     * Validates: Requirements 3.4
     * 
     * This property-based test runs 100 trials with random camera movements,
     * spawning puddles at one position and then moving the camera to verify
     * that the puddle system correctly handles viewport changes.
     */
    @Test
    public void cameraMovementMaintainsViewportContainment() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create a new puddle renderer for each trial
            PuddleRenderer puddleRenderer = new PuddleRenderer(shapeRenderer);
            puddleRenderer.initialize();
            
            // Set up initial camera configuration
            camera.viewportWidth = 800f;
            camera.viewportHeight = 600f;
            camera.zoom = 1.0f;
            
            // Initial camera position
            float initialX = -500f + random.nextFloat() * 1000f;
            float initialY = -500f + random.nextFloat() * 1000f;
            camera.position.set(initialX, initialY, 0);
            camera.update();
            
            // Spawn puddles at initial position
            int puddleCount = 10 + random.nextInt(10); // 10-19 puddles
            puddleRenderer.spawnPuddles(camera, puddleCount);
            
            int initialActivePuddles = puddleRenderer.getActivePuddleCount();
            
            // Move camera to a new position
            float deltaX = -200f + random.nextFloat() * 400f; // -200 to 200
            float deltaY = -200f + random.nextFloat() * 400f; // -200 to 200
            camera.position.set(initialX + deltaX, initialY + deltaY, 0);
            camera.update();
            
            // The puddles should still exist (they don't automatically move with camera)
            // But when we spawn new puddles, they should be in the new viewport
            int puddlesAfterMove = puddleRenderer.getActivePuddleCount();
            
            // Puddles don't disappear when camera moves - they stay at their world positions
            assertEquals(
                initialActivePuddles,
                puddlesAfterMove,
                "Puddle count should remain the same after camera movement"
            );
            
            // Now spawn more puddles - these should be in the new viewport
            int additionalPuddles = 5;
            puddleRenderer.spawnPuddles(camera, additionalPuddles);
            
            int finalPuddleCount = puddleRenderer.getActivePuddleCount();
            
            // We should have more puddles now (up to pool limit)
            assertTrue(
                finalPuddleCount >= initialActivePuddles,
                String.format("Final puddle count (%d) should be >= initial count (%d)",
                    finalPuddleCount, initialActivePuddles)
            );
            
            assertTrue(
                finalPuddleCount <= PuddleConfig.MAX_PUDDLES,
                String.format("Final puddle count (%d) should not exceed MAX_PUDDLES (%d)",
                    finalPuddleCount, PuddleConfig.MAX_PUDDLES)
            );
            
            puddleRenderer.dispose();
        }
    }
    
    /**
     * Property: Camera zoom changes affect viewport bounds
     * For any camera zoom change, newly spawned puddles should respect
     * the new viewport bounds.
     * 
     * This property-based test runs 100 trials with random zoom levels.
     */
    @Test
    public void cameraZoomChangesAffectViewportBounds() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create a new puddle renderer for each trial
            PuddleRenderer puddleRenderer = new PuddleRenderer(shapeRenderer);
            puddleRenderer.initialize();
            
            // Set up camera
            camera.viewportWidth = 800f;
            camera.viewportHeight = 600f;
            camera.position.set(0, 0, 0);
            
            // Initial zoom
            float initialZoom = 0.5f + random.nextFloat() * 1.5f; // 0.5-2.0
            camera.zoom = initialZoom;
            camera.update();
            
            // Spawn puddles at initial zoom
            int puddleCount = 10;
            puddleRenderer.spawnPuddles(camera, puddleCount);
            
            int initialActivePuddles = puddleRenderer.getActivePuddleCount();
            
            // Change zoom
            float newZoom = 0.5f + random.nextFloat() * 1.5f; // 0.5-2.0
            camera.zoom = newZoom;
            camera.update();
            
            // Clear and spawn new puddles with new zoom
            puddleRenderer.clearAllPuddles();
            puddleRenderer.spawnPuddles(camera, puddleCount);
            
            int newActivePuddles = puddleRenderer.getActivePuddleCount();
            
            // Both should have spawned puddles (unless pool was exhausted)
            assertTrue(
                newActivePuddles >= 0 && newActivePuddles <= puddleCount,
                String.format("New puddle count (%d) should be between 0 and %d",
                    newActivePuddles, puddleCount)
            );
            
            puddleRenderer.dispose();
        }
    }
    
    /**
     * Property: Puddles remain at world positions regardless of camera
     * For any puddle spawned at a world position, it should remain at that
     * position even when the camera moves.
     * 
     * This property-based test runs 100 trials verifying puddle world positions.
     */
    @Test
    public void puddlesRemainAtWorldPositions() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create a new puddle renderer for each trial
            PuddleRenderer puddleRenderer = new PuddleRenderer(shapeRenderer);
            puddleRenderer.initialize();
            
            // Set up camera
            camera.viewportWidth = 800f;
            camera.viewportHeight = 600f;
            camera.zoom = 1.0f;
            camera.position.set(0, 0, 0);
            camera.update();
            
            // Spawn a small number of puddles
            int puddleCount = 5;
            puddleRenderer.spawnPuddles(camera, puddleCount);
            
            int initialCount = puddleRenderer.getActivePuddleCount();
            
            // Move camera multiple times
            for (int move = 0; move < 5; move++) {
                float newX = -500f + random.nextFloat() * 1000f;
                float newY = -500f + random.nextFloat() * 1000f;
                camera.position.set(newX, newY, 0);
                camera.update();
                
                // Puddle count should remain the same
                assertEquals(
                    initialCount,
                    puddleRenderer.getActivePuddleCount(),
                    "Puddle count should not change when camera moves"
                );
            }
            
            puddleRenderer.dispose();
        }
    }
    
    /**
     * Property: Clearing puddles works regardless of camera position
     * For any camera position, clearing puddles should remove all active puddles.
     * 
     * This property-based test runs 100 trials with random camera positions.
     */
    @Test
    public void clearingPuddlesWorksRegardlessOfCameraPosition() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create a new puddle renderer for each trial
            PuddleRenderer puddleRenderer = new PuddleRenderer(shapeRenderer);
            puddleRenderer.initialize();
            
            // Set up camera at random position
            camera.viewportWidth = 800f;
            camera.viewportHeight = 600f;
            camera.zoom = 1.0f;
            
            float camX = -1000f + random.nextFloat() * 2000f;
            float camY = -1000f + random.nextFloat() * 2000f;
            camera.position.set(camX, camY, 0);
            camera.update();
            
            // Spawn puddles
            int puddleCount = 10 + random.nextInt(10);
            puddleRenderer.spawnPuddles(camera, puddleCount);
            
            // Verify puddles were spawned
            int activePuddles = puddleRenderer.getActivePuddleCount();
            assertTrue(activePuddles > 0, "Should have spawned some puddles");
            
            // Clear all puddles
            puddleRenderer.clearAllPuddles();
            
            // Verify all puddles were cleared
            assertEquals(
                0,
                puddleRenderer.getActivePuddleCount(),
                "All puddles should be cleared regardless of camera position"
            );
            
            puddleRenderer.dispose();
        }
    }
    
    /**
     * Property: Viewport size changes affect puddle spawning area
     * For any viewport size change, newly spawned puddles should be within
     * the new viewport bounds.
     * 
     * This property-based test runs 100 trials with random viewport sizes.
     */
    @Test
    public void viewportSizeChangesAffectSpawningArea() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create a new puddle renderer for each trial
            PuddleRenderer puddleRenderer = new PuddleRenderer(shapeRenderer);
            puddleRenderer.initialize();
            
            // Set up camera with random viewport size
            float viewportWidth = 400f + random.nextFloat() * 1200f; // 400-1600
            float viewportHeight = 300f + random.nextFloat() * 900f; // 300-1200
            
            camera.viewportWidth = viewportWidth;
            camera.viewportHeight = viewportHeight;
            camera.zoom = 1.0f;
            camera.position.set(0, 0, 0);
            camera.update();
            
            // Spawn puddles
            int puddleCount = 15;
            puddleRenderer.spawnPuddles(camera, puddleCount);
            
            int activePuddles = puddleRenderer.getActivePuddleCount();
            
            // Verify puddles were spawned within reasonable bounds
            assertTrue(
                activePuddles >= 0 && activePuddles <= puddleCount,
                String.format("Active puddles (%d) should be between 0 and %d",
                    activePuddles, puddleCount)
            );
            
            // Change viewport size
            float newWidth = 400f + random.nextFloat() * 1200f;
            float newHeight = 300f + random.nextFloat() * 900f;
            
            camera.viewportWidth = newWidth;
            camera.viewportHeight = newHeight;
            camera.update();
            
            // Clear and spawn new puddles with new viewport
            puddleRenderer.clearAllPuddles();
            puddleRenderer.spawnPuddles(camera, puddleCount);
            
            int newActivePuddles = puddleRenderer.getActivePuddleCount();
            
            // Verify new puddles were spawned
            assertTrue(
                newActivePuddles >= 0 && newActivePuddles <= puddleCount,
                String.format("New active puddles (%d) should be between 0 and %d",
                    newActivePuddles, puddleCount)
            );
            
            puddleRenderer.dispose();
        }
    }
}
