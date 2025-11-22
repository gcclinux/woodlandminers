package wagemaker.uk.birds;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for formation reaches opposite boundary.
 * Feature: flying-birds-ambient, Property 3: Formation reaches opposite boundary
 * Validates: Requirements 1.3
 */
public class FormationReachesOppositeBoundaryPropertyTest {
    
    private static HeadlessApplication application;
    private static Texture mockTexture;
    private static final float BIRD_SPEED = 100f; // pixels per second
    
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
     * Property 3: Formation reaches opposite boundary
     * For any bird formation with a given spawn boundary and velocity, 
     * updating the formation for sufficient time should result in the 
     * formation reaching the opposite boundary.
     * Validates: Requirements 1.3
     * 
     * This property-based test runs 100 trials with different spawn boundaries
     * and verifies that formations always reach their target boundary.
     */
    @Test
    public void formationReachesOppositeBoundary() {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        // Test all four spawn boundaries
        SpawnBoundary[] boundaries = SpawnBoundary.values();
        
        // Run 100 trials (25 per boundary)
        for (int trial = 0; trial < 100; trial++) {
            SpawnBoundary spawnBoundary = boundaries[trial % boundaries.length];
            
            // Create spawn point based on boundary
            SpawnPoint spawnPoint = createSpawnPoint(spawnBoundary, viewWidth, viewHeight);
            
            // Create velocity vector
            Vector2 velocity = spawnPoint.direction.cpy().scl(BIRD_SPEED);
            
            // Create formation
            BirdFormation formation = new BirdFormation(spawnPoint, velocity, mockTexture);
            
            // Calculate maximum time needed to cross screen
            // Diagonal distance is the maximum possible distance
            float maxDistance = (float) Math.sqrt(viewWidth * viewWidth + viewHeight * viewHeight);
            float maxTime = maxDistance / BIRD_SPEED;
            
            // Update formation for sufficient time
            float timeStep = 0.1f;
            float totalTime = 0f;
            boolean reachedTarget = false;
            
            while (totalTime < maxTime * 2) { // Use 2x max time as safety margin
                formation.update(timeStep);
                totalTime += timeStep;
                
                if (formation.hasReachedTarget(viewWidth, viewHeight)) {
                    reachedTarget = true;
                    break;
                }
            }
            
            // Verify formation reached target
            assertTrue(
                reachedTarget,
                "Trial " + trial + ": Formation spawned at " + spawnBoundary + 
                " should reach opposite boundary within " + (maxTime * 2) + " seconds"
            );
            
            // Clean up
            formation.dispose();
        }
    }
    
    /**
     * Property: Formation reaches target in reasonable time
     * For any formation, the time to reach the target should be proportional
     * to the distance traveled divided by the speed.
     */
    @Test
    public void formationReachesTargetInReasonableTime() {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        SpawnBoundary[] boundaries = SpawnBoundary.values();
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            SpawnBoundary spawnBoundary = boundaries[trial % boundaries.length];
            
            // Create spawn point
            SpawnPoint spawnPoint = createSpawnPoint(spawnBoundary, viewWidth, viewHeight);
            
            // Create velocity vector
            Vector2 velocity = spawnPoint.direction.cpy().scl(BIRD_SPEED);
            
            // Create formation
            BirdFormation formation = new BirdFormation(spawnPoint, velocity, mockTexture);
            
            // Calculate expected time based on distance
            float expectedDistance;
            switch (spawnBoundary) {
                case TOP:
                case BOTTOM:
                    expectedDistance = viewHeight;
                    break;
                case LEFT:
                case RIGHT:
                    expectedDistance = viewWidth;
                    break;
                default:
                    expectedDistance = viewWidth;
            }
            
            float expectedTime = expectedDistance / BIRD_SPEED;
            
            // Update formation and measure actual time
            float timeStep = 0.1f;
            float actualTime = 0f;
            
            while (actualTime < expectedTime * 3) { // 3x safety margin
                formation.update(timeStep);
                actualTime += timeStep;
                
                if (formation.hasReachedTarget(viewWidth, viewHeight)) {
                    break;
                }
            }
            
            // Verify time is within reasonable bounds (expected time ± 20%)
            float tolerance = expectedTime * 0.2f;
            assertTrue(
                actualTime >= expectedTime - tolerance && actualTime <= expectedTime + tolerance,
                "Trial " + trial + ": Formation should reach target in approximately " + 
                expectedTime + " seconds, but took " + actualTime + " seconds"
            );
            
            // Clean up
            formation.dispose();
        }
    }
    
    /**
     * Helper method to create a spawn point for a given boundary
     */
    private SpawnPoint createSpawnPoint(SpawnBoundary boundary, float viewWidth, float viewHeight) {
        float x, y;
        Vector2 direction;
        
        switch (boundary) {
            case TOP:
                x = viewWidth / 2;
                y = viewHeight;
                direction = new Vector2(0, -1);
                break;
            case BOTTOM:
                x = viewWidth / 2;
                y = 0;
                direction = new Vector2(0, 1);
                break;
            case LEFT:
                x = 0;
                y = viewHeight / 2;
                direction = new Vector2(1, 0);
                break;
            case RIGHT:
                x = viewWidth;
                y = viewHeight / 2;
                direction = new Vector2(-1, 0);
                break;
            default:
                x = 0;
                y = 0;
                direction = new Vector2(1, 0);
        }
        
        return new SpawnPoint(boundary, x, y, direction);
    }
}
