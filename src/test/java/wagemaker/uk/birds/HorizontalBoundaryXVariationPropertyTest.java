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
 * Property-based test for horizontal boundary X-coordinate variation.
 * Feature: flying-birds-ambient, Property 12: Horizontal boundary X-coordinate variation
 * Validates: Requirements 4.3
 */
public class HorizontalBoundaryXVariationPropertyTest {
    
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
     * Property 12: Horizontal boundary X-coordinate variation
     * For any sequence of spawns on horizontal boundaries (TOP or BOTTOM), 
     * the X-coordinates should vary across the viewport width
     * Validates: Requirements 4.3
     * 
     * This property-based test runs many spawn cycles and verifies that
     * X-coordinates on horizontal boundaries show variation.
     */
    @Test
    public void horizontalBoundaryXCoordinatesVary() {
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        Viewport viewport = new FitViewport(800, 600, camera);
        
        BirdFormationManager manager = new BirdFormationManager(camera, viewport, mockTexture);
        
        List<Float> topBoundaryXCoords = new ArrayList<>();
        List<Float> bottomBoundaryXCoords = new ArrayList<>();
        
        // Run many spawn cycles to collect horizontal boundary spawns
        for (int cycle = 0; cycle < 200; cycle++) {
            // Trigger spawn
            float spawnTime = manager.getSpawnTimer();
            manager.update(spawnTime + 0.1f, 0, 0);
            
            BirdFormation formation = manager.getActiveFormation();
            if (formation != null) {
                SpawnBoundary boundary = formation.getSpawnBoundary();
                Bird leadBird = formation.getBirds().get(0);
                float x = leadBird.getX();
                
                if (boundary == SpawnBoundary.TOP) {
                    topBoundaryXCoords.add(x);
                } else if (boundary == SpawnBoundary.BOTTOM) {
                    bottomBoundaryXCoords.add(x);
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
        
        // Verify TOP boundary X-coordinates vary
        if (topBoundaryXCoords.size() >= 2) {
            float minX = Float.MAX_VALUE;
            float maxX = Float.MIN_VALUE;
            
            for (float x : topBoundaryXCoords) {
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
            }
            
            float range = maxX - minX;
            float viewWidth = viewport.getWorldWidth();
            
            assertTrue(
                range > viewWidth * 0.3f,
                "TOP boundary X-coordinates should vary across at least 30% of viewport width. " +
                "Range: " + range + ", ViewWidth: " + viewWidth + 
                " (" + topBoundaryXCoords.size() + " samples)"
            );
        }
        
        // Verify BOTTOM boundary X-coordinates vary
        if (bottomBoundaryXCoords.size() >= 2) {
            float minX = Float.MAX_VALUE;
            float maxX = Float.MIN_VALUE;
            
            for (float x : bottomBoundaryXCoords) {
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
            }
            
            float range = maxX - minX;
            float viewWidth = viewport.getWorldWidth();
            
            assertTrue(
                range > viewWidth * 0.3f,
                "BOTTOM boundary X-coordinates should vary across at least 30% of viewport width. " +
                "Range: " + range + ", ViewWidth: " + viewWidth + 
                " (" + bottomBoundaryXCoords.size() + " samples)"
            );
        }
        
        manager.dispose();
    }
}
