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
 * Property-based test for spawn position on boundary.
 * Feature: flying-birds-ambient, Property 2: Spawn position on boundary
 * Validates: Requirements 1.2
 */
public class SpawnPositionOnBoundaryPropertyTest {
    
    private static HeadlessApplication application;
    private static Texture mockTexture;
    private static final float BOUNDARY_TOLERANCE = 1.0f; // 1 pixel tolerance
    
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
     * Property 2: Spawn position on boundary
     * For any bird formation spawn, the spawn position should be located on one of the 
     * four screen boundaries (within 1 pixel tolerance)
     * Validates: Requirements 1.2
     * 
     * This property-based test runs 100 trials, triggering spawns and verifying
     * that each spawn position is on a screen boundary.
     */
    @Test
    public void spawnPositionIsOnBoundary() {
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            OrthographicCamera camera = new OrthographicCamera();
            camera.setToOrtho(false, 800, 600);
            Viewport viewport = new FitViewport(800, 600, camera);
            
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Trigger spawn by advancing time
            float spawnTime = manager.getSpawnTimer();
            manager.update(spawnTime + 0.1f, 0, 0);
            
            // Get the active formation
            BirdFormation formation = manager.getActiveFormation();
            assertNotNull(formation, "Trial " + trial + ": Formation should be spawned");
            
            // Get the lead bird position (first bird in formation)
            Bird leadBird = formation.getBirds().get(0);
            float x = leadBird.getX();
            float y = leadBird.getY();
            
            float viewWidth = viewport.getWorldWidth();
            float viewHeight = viewport.getWorldHeight();
            
            // Check if position is on one of the four boundaries
            boolean onTopBoundary = Math.abs(y - viewHeight) <= BOUNDARY_TOLERANCE;
            boolean onBottomBoundary = Math.abs(y - 0) <= BOUNDARY_TOLERANCE;
            boolean onLeftBoundary = Math.abs(x - 0) <= BOUNDARY_TOLERANCE;
            boolean onRightBoundary = Math.abs(x - viewWidth) <= BOUNDARY_TOLERANCE;
            
            boolean onAnyBoundary = onTopBoundary || onBottomBoundary || onLeftBoundary || onRightBoundary;
            
            assertTrue(
                onAnyBoundary,
                "Trial " + trial + ": Spawn position (" + x + ", " + y + ") should be on a boundary. " +
                "View dimensions: " + viewWidth + "x" + viewHeight + ". " +
                "Distances - Top: " + Math.abs(y - viewHeight) + ", Bottom: " + Math.abs(y) + 
                ", Left: " + Math.abs(x) + ", Right: " + Math.abs(x - viewWidth)
            );
            
            // Clean up
            manager.dispose();
        }
    }
    
    /**
     * Property: Spawn position coordinates are within viewport bounds
     * For any bird formation spawn, the x-coordinate should be within [0, viewWidth]
     * and y-coordinate should be within [0, viewHeight].
     * 
     * This property-based test runs 100 trials with different viewport sizes.
     */
    @Test
    public void spawnPositionWithinViewportBounds() {
        // Test with different viewport sizes
        float[][] viewportSizes = {
            {800, 600},
            {1920, 1080},
            {1024, 768},
            {640, 480},
            {1280, 720}
        };
        
        for (int sizeIdx = 0; sizeIdx < viewportSizes.length; sizeIdx++) {
            float viewWidth = viewportSizes[sizeIdx][0];
            float viewHeight = viewportSizes[sizeIdx][1];
            
            // Run 20 trials per viewport size (100 total)
            for (int trial = 0; trial < 20; trial++) {
                OrthographicCamera camera = new OrthographicCamera();
                camera.setToOrtho(false, viewWidth, viewHeight);
                Viewport viewport = new FitViewport(viewWidth, viewHeight, camera);
                
                BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
                
                // Trigger spawn
                float spawnTime = manager.getSpawnTimer();
                manager.update(spawnTime + 0.1f, 0, 0);
                
                BirdFormation formation = manager.getActiveFormation();
                assertNotNull(formation, "Formation should be spawned");
                
                // Check lead bird position (first bird) - it should be on boundary
                // Wing birds may extend slightly beyond viewport due to V-formation offset
                Bird leadBird = formation.getBirds().get(0);
                float x = leadBird.getX();
                float y = leadBird.getY();
                
                assertTrue(
                    x >= -BOUNDARY_TOLERANCE && x <= viewWidth + BOUNDARY_TOLERANCE,
                    "Viewport " + viewWidth + "x" + viewHeight + ", Trial " + trial + 
                    ": Lead bird X-coordinate " + x + " should be within [0, " + viewWidth + "]"
                );
                
                assertTrue(
                    y >= -BOUNDARY_TOLERANCE && y <= viewHeight + BOUNDARY_TOLERANCE,
                    "Viewport " + viewWidth + "x" + viewHeight + ", Trial " + trial + 
                    ": Lead bird Y-coordinate " + y + " should be within [0, " + viewHeight + "]"
                );
                
                manager.dispose();
            }
        }
    }
    
    /**
     * Property: Each boundary type produces correct spawn positions
     * For spawns on TOP boundary, y should equal viewHeight
     * For spawns on BOTTOM boundary, y should equal 0
     * For spawns on LEFT boundary, x should equal 0
     * For spawns on RIGHT boundary, x should equal viewWidth
     * 
     * This property-based test runs 400 trials to ensure all boundaries are tested.
     */
    @Test
    public void eachBoundaryTypeProducesCorrectPosition() {
        int[] boundaryHits = new int[4]; // Count hits for each boundary
        
        // Run many trials to ensure we hit all boundaries
        for (int trial = 0; trial < 400; trial++) {
            OrthographicCamera camera = new OrthographicCamera();
            camera.setToOrtho(false, 800, 600);
            Viewport viewport = new FitViewport(800, 600, camera);
            
            BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
            
            // Trigger spawn
            float spawnTime = manager.getSpawnTimer();
            manager.update(spawnTime + 0.1f, 0, 0);
            
            BirdFormation formation = manager.getActiveFormation();
            assertNotNull(formation);
            
            Bird leadBird = formation.getBirds().get(0);
            float x = leadBird.getX();
            float y = leadBird.getY();
            
            float viewWidth = viewport.getWorldWidth();
            float viewHeight = viewport.getWorldHeight();
            
            // Determine which boundary this spawn is on
            if (Math.abs(y - viewHeight) <= BOUNDARY_TOLERANCE) {
                // TOP boundary
                boundaryHits[0]++;
                assertTrue(
                    x >= 0 && x <= viewWidth,
                    "TOP boundary spawn: X should be within viewport width"
                );
            } else if (Math.abs(y - 0) <= BOUNDARY_TOLERANCE) {
                // BOTTOM boundary
                boundaryHits[1]++;
                assertTrue(
                    x >= 0 && x <= viewWidth,
                    "BOTTOM boundary spawn: X should be within viewport width"
                );
            } else if (Math.abs(x - 0) <= BOUNDARY_TOLERANCE) {
                // LEFT boundary
                boundaryHits[2]++;
                assertTrue(
                    y >= 0 && y <= viewHeight,
                    "LEFT boundary spawn: Y should be within viewport height"
                );
            } else if (Math.abs(x - viewWidth) <= BOUNDARY_TOLERANCE) {
                // RIGHT boundary
                boundaryHits[3]++;
                assertTrue(
                    y >= 0 && y <= viewHeight,
                    "RIGHT boundary spawn: Y should be within viewport height"
                );
            }
            
            manager.dispose();
        }
        
        // Verify all boundaries were hit at least once
        for (int i = 0; i < 4; i++) {
            String[] boundaryNames = {"TOP", "BOTTOM", "LEFT", "RIGHT"};
            assertTrue(
                boundaryHits[i] > 0,
                boundaryNames[i] + " boundary should be selected at least once in 400 trials"
            );
        }
    }
}
