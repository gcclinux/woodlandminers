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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for vertical boundary Y-coordinate variation.
 * Feature: flying-birds-ambient, Property 11: Vertical boundary Y-coordinate variation
 * Validates: Requirements 4.2
 */
public class VerticalBoundaryYVariationPropertyTest {
    
    private static HeadlessApplication application;
    private static Texture mockTexture;
    
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
     * Property 11: Vertical boundary Y-coordinate variation
     * For any sequence of spawns on vertical boundaries (LEFT or RIGHT), 
     * the Y-coordinates should vary across the viewport height
     * Validates: Requirements 4.2
     * 
     * This property-based test runs many spawn cycles and verifies that
     * Y-coordinates on vertical boundaries show variation.
     */
    @Test
    public void verticalBoundaryYCoordinatesVary() {
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        Viewport viewport = new FitViewport(800, 600, camera);
        
        BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
        
        List<Float> leftBoundaryYCoords = new ArrayList<>();
        List<Float> rightBoundaryYCoords = new ArrayList<>();
        
        // Run many spawn cycles to collect vertical boundary spawns
        for (int cycle = 0; cycle < 200; cycle++) {
            // Trigger spawn
            float spawnTime = manager.getSpawnTimer();
            manager.update(spawnTime + 0.1f, 0, 0);
            
            BirdFormation formation = manager.getActiveFormation();
            if (formation != null) {
                SpawnBoundary boundary = formation.getSpawnBoundary();
                Bird leadBird = formation.getBirds().get(0);
                float y = leadBird.getY();
                
                if (boundary == SpawnBoundary.LEFT) {
                    leftBoundaryYCoords.add(y);
                } else if (boundary == SpawnBoundary.RIGHT) {
                    rightBoundaryYCoords.add(y);
                }
            }
            
            // Wait for formation to complete
            for (int i = 0; i < 200; i++) {
                manager.update(0.1f, 0, 0);
                if (manager.getActiveFormation() == null) {
                    break;
                }
            }
        }
        
        // Verify LEFT boundary Y-coordinates vary
        if (leftBoundaryYCoords.size() >= 2) {
            float minY = Float.MAX_VALUE;
            float maxY = Float.MIN_VALUE;
            
            for (float y : leftBoundaryYCoords) {
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
            
            float range = maxY - minY;
            float viewHeight = viewport.getWorldHeight();
            
            assertTrue(
                range > viewHeight * 0.3f,
                "LEFT boundary Y-coordinates should vary across at least 30% of viewport height. " +
                "Range: " + range + ", ViewHeight: " + viewHeight + 
                " (" + leftBoundaryYCoords.size() + " samples)"
            );
        }
        
        // Verify RIGHT boundary Y-coordinates vary
        if (rightBoundaryYCoords.size() >= 2) {
            float minY = Float.MAX_VALUE;
            float maxY = Float.MIN_VALUE;
            
            for (float y : rightBoundaryYCoords) {
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
            
            float range = maxY - minY;
            float viewHeight = viewport.getWorldHeight();
            
            assertTrue(
                range > viewHeight * 0.3f,
                "RIGHT boundary Y-coordinates should vary across at least 30% of viewport height. " +
                "Range: " + range + ", ViewHeight: " + viewHeight + 
                " (" + rightBoundaryYCoords.size() + " samples)"
            );
        }
        
        manager.dispose();
    }
}
