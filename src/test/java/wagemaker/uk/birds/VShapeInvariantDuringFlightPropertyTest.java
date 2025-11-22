package wagemaker.uk.birds;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for V-shape invariant during flight.
 * Feature: flying-birds-ambient, Property 8: V-shape invariant during flight
 * Validates: Requirements 2.3
 */
public class VShapeInvariantDuringFlightPropertyTest {
    
    private static HeadlessApplication application;
    private static Texture mockTexture;
    private static final float BIRD_SPEED = 100f;
    private static final float TOLERANCE = 2.0f; // Allow 2 pixel tolerance for floating point errors
    
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
        
        mockTexture = Mockito.mock(Texture.class);
    }
    
    @AfterAll
    public static void tearDownGdx() {
        if (application != null) {
            application.exit();
        }
    }
    
    /**
     * Property 8: V-shape invariant during flight
     * For any bird formation at any point during its flight, the relative 
     * positions of the birds should maintain the V-shape pattern.
     * Validates: Requirements 2.3
     * 
     * This property-based test runs 100 trials, creating formations and
     * verifying that the V-shape is maintained throughout the flight.
     */
    @Test
    public void vShapeInvariantDuringFlight() {
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
            
            // Capture initial relative positions
            List<Bird> birds = formation.getBirds();
            assertEquals(5, birds.size(), "Formation should have 5 birds");
            
            Vector2[] initialRelativePositions = new Vector2[5];
            Bird leadBird = birds.get(0);
            for (int i = 0; i < 5; i++) {
                Bird bird = birds.get(i);
                initialRelativePositions[i] = new Vector2(
                    bird.getX() - leadBird.getX(),
                    bird.getY() - leadBird.getY()
                );
            }
            
            // Update formation multiple times and check invariant
            float timeStep = 0.1f;
            int updateCount = 50; // Check at 50 different points during flight
            
            for (int update = 0; update < updateCount; update++) {
                formation.update(timeStep);
                
                // Check that relative positions are maintained
                Bird currentLeadBird = birds.get(0);
                for (int i = 0; i < 5; i++) {
                    Bird bird = birds.get(i);
                    float relativeX = bird.getX() - currentLeadBird.getX();
                    float relativeY = bird.getY() - currentLeadBird.getY();
                    
                    float expectedX = initialRelativePositions[i].x;
                    float expectedY = initialRelativePositions[i].y;
                    
                    float deltaX = Math.abs(relativeX - expectedX);
                    float deltaY = Math.abs(relativeY - expectedY);
                    
                    assertTrue(
                        deltaX <= TOLERANCE,
                        "Trial " + trial + ", Update " + update + ", Bird " + i + 
                        ": X relative position should be maintained. Expected: " + expectedX + 
                        ", Actual: " + relativeX + ", Delta: " + deltaX
                    );
                    
                    assertTrue(
                        deltaY <= TOLERANCE,
                        "Trial " + trial + ", Update " + update + ", Bird " + i + 
                        ": Y relative position should be maintained. Expected: " + expectedY + 
                        ", Actual: " + relativeY + ", Delta: " + deltaY
                    );
                }
            }
            
            // Clean up
            formation.dispose();
        }
    }
    
    /**
     * Property: V-shape maintained across entire flight path
     * For any formation, the V-shape should be maintained from spawn to despawn.
     */
    @Test
    public void vShapeMaintainedAcrossEntireFlightPath() {
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
            
            // Capture initial relative positions
            List<Bird> birds = formation.getBirds();
            Vector2[] initialRelativePositions = new Vector2[5];
            Bird leadBird = birds.get(0);
            for (int i = 0; i < 5; i++) {
                Bird bird = birds.get(i);
                initialRelativePositions[i] = new Vector2(
                    bird.getX() - leadBird.getX(),
                    bird.getY() - leadBird.getY()
                );
            }
            
            // Update until formation reaches target
            float timeStep = 0.1f;
            float maxTime = 20f; // Maximum time to prevent infinite loop
            float totalTime = 0f;
            
            while (totalTime < maxTime && !formation.hasReachedTarget(viewWidth, viewHeight)) {
                formation.update(timeStep);
                totalTime += timeStep;
                
                // Check relative positions every update
                Bird currentLeadBird = birds.get(0);
                for (int i = 0; i < 5; i++) {
                    Bird bird = birds.get(i);
                    float relativeX = bird.getX() - currentLeadBird.getX();
                    float relativeY = bird.getY() - currentLeadBird.getY();
                    
                    float expectedX = initialRelativePositions[i].x;
                    float expectedY = initialRelativePositions[i].y;
                    
                    float deltaX = Math.abs(relativeX - expectedX);
                    float deltaY = Math.abs(relativeY - expectedY);
                    
                    assertTrue(
                        deltaX <= TOLERANCE && deltaY <= TOLERANCE,
                        "Trial " + trial + ", Time " + totalTime + ", Bird " + i + 
                        ": Relative position should be maintained throughout flight"
                    );
                }
            }
            
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
