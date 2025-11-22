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
 * Property-based test for consecutive spawn variation.
 * Feature: flying-birds-ambient, Property 5: Consecutive spawn variation
 * Validates: Requirements 1.5
 */
public class ConsecutiveSpawnVariationPropertyTest {
    
    private static HeadlessApplication application;
    private static Texture mockTexture;
    private static final float POSITION_VARIATION_THRESHOLD = 50f; // Minimum variation in pixels
    
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
     * Property 5: Consecutive spawn variation
     * For any two consecutive spawn events, the spawn boundary or spawn position should differ
     * Validates: Requirements 1.5
     * 
     * This property-based test runs 100 pairs of consecutive spawns and verifies
     * that each pair shows variation in boundary or position.
     */
    @Test
    public void consecutiveSpawnsShowVariation() {
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        Viewport viewport = new FitViewport(800, 600, camera);
        
        BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
        
        // Run 100 consecutive spawn pairs
        for (int pair = 0; pair < 100; pair++) {
            // Trigger first spawn
            float spawnTime1 = manager.getSpawnTimer();
            manager.update(spawnTime1 + 0.1f, 0, 0);
            
            BirdFormation formation1 = manager.getActiveFormation();
            assertNotNull(formation1, "Pair " + pair + ": First formation should spawn");
            
            // Record first spawn data
            SpawnBoundary boundary1 = formation1.getSpawnBoundary();
            Bird leadBird1 = formation1.getBirds().get(0);
            float x1 = leadBird1.getX();
            float y1 = leadBird1.getY();
            
            // Wait for formation to complete
            for (int i = 0; i < 200; i++) {
                manager.update(0.1f, 0, 0);
                if (manager.getActiveFormation() == null) {
                    break;
                }
            }
            
            // Trigger second spawn
            float spawnTime2 = manager.getSpawnTimer();
            manager.update(spawnTime2 + 0.1f, 0, 0);
            
            BirdFormation formation2 = manager.getActiveFormation();
            assertNotNull(formation2, "Pair " + pair + ": Second formation should spawn");
            
            // Record second spawn data
            SpawnBoundary boundary2 = formation2.getSpawnBoundary();
            Bird leadBird2 = formation2.getBirds().get(0);
            float x2 = leadBird2.getX();
            float y2 = leadBird2.getY();
            
            // Check for variation: either boundary differs OR position differs significantly
            boolean boundaryDiffers = boundary1 != boundary2;
            boolean positionDiffers = Math.abs(x1 - x2) > POSITION_VARIATION_THRESHOLD || 
                                     Math.abs(y1 - y2) > POSITION_VARIATION_THRESHOLD;
            
            boolean hasVariation = boundaryDiffers || positionDiffers;
            
            assertTrue(
                hasVariation,
                "Pair " + pair + ": Consecutive spawns should show variation. " +
                "First spawn: boundary=" + boundary1 + ", pos=(" + x1 + ", " + y1 + "). " +
                "Second spawn: boundary=" + boundary2 + ", pos=(" + x2 + ", " + y2 + ")"
            );
            
            // Wait for second formation to complete
            for (int i = 0; i < 200; i++) {
                manager.update(0.1f, 0, 0);
                if (manager.getActiveFormation() == null) {
                    break;
                }
            }
        }
        
        manager.dispose();
    }
    
    /**
     * Property: Consecutive spawns on same boundary have different positions
     * When two consecutive spawns occur on the same boundary, their positions
     * along that boundary should differ.
     * 
     * This property-based test runs many spawn cycles and checks position variation
     * when the same boundary is selected consecutively.
     */
    @Test
    public void consecutiveSpawnsOnSameBoundaryHaveDifferentPositions() {
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        Viewport viewport = new FitViewport(800, 600, camera);
        
        BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
        
        int sameBoundaryPairs = 0;
        int sameBoundaryWithVariation = 0;
        
        SpawnBoundary previousBoundary = null;
        float previousX = 0;
        float previousY = 0;
        
        // Run many spawn cycles to find cases where same boundary is selected
        for (int cycle = 0; cycle < 200; cycle++) {
            // Trigger spawn
            float spawnTime = manager.getSpawnTimer();
            manager.update(spawnTime + 0.1f, 0, 0);
            
            BirdFormation formation = manager.getActiveFormation();
            if (formation == null) continue;
            
            SpawnBoundary currentBoundary = formation.getSpawnBoundary();
            Bird leadBird = formation.getBirds().get(0);
            float currentX = leadBird.getX();
            float currentY = leadBird.getY();
            
            // Check if this is a consecutive spawn on the same boundary
            if (previousBoundary != null && previousBoundary == currentBoundary) {
                sameBoundaryPairs++;
                
                // Check for position variation
                boolean positionDiffers = Math.abs(currentX - previousX) > 10f || 
                                         Math.abs(currentY - previousY) > 10f;
                
                if (positionDiffers) {
                    sameBoundaryWithVariation++;
                }
            }
            
            previousBoundary = currentBoundary;
            previousX = currentX;
            previousY = currentY;
            
            // Wait for formation to complete
            for (int i = 0; i < 200; i++) {
                manager.update(0.1f, 0, 0);
                if (manager.getActiveFormation() == null) {
                    break;
                }
            }
        }
        
        // If we found cases where same boundary was selected consecutively,
        // verify that positions varied
        if (sameBoundaryPairs > 0) {
            double variationRate = (double) sameBoundaryWithVariation / sameBoundaryPairs;
            assertTrue(
                variationRate > 0.8, // At least 80% should have position variation
                "When same boundary is selected consecutively, positions should vary. " +
                "Found " + sameBoundaryWithVariation + " with variation out of " + 
                sameBoundaryPairs + " same-boundary pairs (rate: " + variationRate + ")"
            );
        }
        
        manager.dispose();
    }
    
    /**
     * Property: No three consecutive spawns from same boundary
     * The system should ensure sufficient variation that we don't see
     * three consecutive spawns from the exact same boundary.
     * 
     * This property-based test runs 300 spawn cycles and verifies no
     * three-in-a-row same boundary occurrences.
     */
    @Test
    public void noThreeConsecutiveSpawnsFromSameBoundary() {
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        Viewport viewport = new FitViewport(800, 600, camera);
        
        BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
        
        SpawnBoundary boundary1 = null;
        SpawnBoundary boundary2 = null;
        
        // Run 300 spawn cycles
        for (int cycle = 0; cycle < 300; cycle++) {
            // Trigger spawn
            float spawnTime = manager.getSpawnTimer();
            manager.update(spawnTime + 0.1f, 0, 0);
            
            BirdFormation formation = manager.getActiveFormation();
            if (formation == null) continue;
            
            SpawnBoundary boundary3 = formation.getSpawnBoundary();
            
            // Check if we have three consecutive same boundaries
            if (boundary1 != null && boundary2 != null) {
                boolean threeInARow = (boundary1 == boundary2) && (boundary2 == boundary3);
                
                assertFalse(
                    threeInARow,
                    "Cycle " + cycle + ": Should not have three consecutive spawns from " + 
                    boundary3 + " boundary"
                );
            }
            
            // Shift boundaries
            boundary1 = boundary2;
            boundary2 = boundary3;
            
            // Wait for formation to complete
            for (int i = 0; i < 200; i++) {
                manager.update(0.1f, 0, 0);
                if (manager.getActiveFormation() == null) {
                    break;
                }
            }
        }
        
        manager.dispose();
    }
}
