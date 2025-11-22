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
 * Property-based test for flight path toward opposite boundary.
 * Feature: flying-birds-ambient, Property 13: Flight path toward opposite boundary
 * Validates: Requirements 4.4
 */
public class FlightPathTowardOppositeBoundaryPropertyTest {
    
    private static HeadlessApplication application;
    private static Texture mockTexture;
    private static final float BIRD_SPEED = 100f;
    
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
     * Property 13: Flight path toward opposite boundary
     * For any bird formation with spawn boundary B, the flight direction 
     * should point toward the opposite boundary.
     * Validates: Requirements 4.4
     * 
     * This property-based test runs 100 trials with different spawn boundaries
     * and verifies that the flight direction always points toward the opposite boundary.
     */
    @Test
    public void flightPathTowardOppositeBoundary() {
        float viewWidth = 800f;
        float viewHeight = 600f;
        
        SpawnBoundary[] boundaries = SpawnBoundary.values();
        
        // Run 100 trials (25 per boundary)
        for (int trial = 0; trial < 100; trial++) {
            SpawnBoundary spawnBoundary = boundaries[trial % boundaries.length];
            
            // Create spawn point
            SpawnPoint spawnPoint = createSpawnPoint(spawnBoundary, viewWidth, viewHeight);
            
            // Create velocity vector
            Vector2 velocity = spawnPoint.direction.cpy().scl(BIRD_SPEED);
            
            // Create formation
            BirdFormation formation = new BirdFormation(spawnPoint, velocity, mockTexture);
            
            // Get initial position
            List<Bird> birds = formation.getBirds();
            Bird leadBird = birds.get(0);
            float initialX = leadBird.getX();
            float initialY = leadBird.getY();
            
            // Update formation
            float timeStep = 0.5f; // Use larger time step to see clear movement
            formation.update(timeStep);
            
            // Get new position
            float newX = leadBird.getX();
            float newY = leadBird.getY();
            
            // Calculate actual movement direction
            float deltaX = newX - initialX;
            float deltaY = newY - initialY;
            
            // Verify movement is toward opposite boundary
            switch (spawnBoundary) {
                case TOP:
                    // Should move downward (negative Y)
                    assertTrue(
                        deltaY < 0,
                        "Trial " + trial + ": Formation spawned at TOP should move downward. " +
                        "DeltaY: " + deltaY
                    );
                    // X movement should be minimal (within tolerance)
                    assertTrue(
                        Math.abs(deltaX) < 1.0f,
                        "Trial " + trial + ": Formation spawned at TOP should move primarily in Y direction. " +
                        "DeltaX: " + deltaX
                    );
                    break;
                    
                case BOTTOM:
                    // Should move upward (positive Y)
                    assertTrue(
                        deltaY > 0,
                        "Trial " + trial + ": Formation spawned at BOTTOM should move upward. " +
                        "DeltaY: " + deltaY
                    );
                    // X movement should be minimal
                    assertTrue(
                        Math.abs(deltaX) < 1.0f,
                        "Trial " + trial + ": Formation spawned at BOTTOM should move primarily in Y direction. " +
                        "DeltaX: " + deltaX
                    );
                    break;
                    
                case LEFT:
                    // Should move rightward (positive X)
                    assertTrue(
                        deltaX > 0,
                        "Trial " + trial + ": Formation spawned at LEFT should move rightward. " +
                        "DeltaX: " + deltaX
                    );
                    // Y movement should be minimal
                    assertTrue(
                        Math.abs(deltaY) < 1.0f,
                        "Trial " + trial + ": Formation spawned at LEFT should move primarily in X direction. " +
                        "DeltaY: " + deltaY
                    );
                    break;
                    
                case RIGHT:
                    // Should move leftward (negative X)
                    assertTrue(
                        deltaX < 0,
                        "Trial " + trial + ": Formation spawned at RIGHT should move leftward. " +
                        "DeltaX: " + deltaX
                    );
                    // Y movement should be minimal
                    assertTrue(
                        Math.abs(deltaY) < 1.0f,
                        "Trial " + trial + ": Formation spawned at RIGHT should move primarily in X direction. " +
                        "DeltaY: " + deltaY
                    );
                    break;
            }
            
            // Clean up
            formation.dispose();
        }
    }
    
    /**
     * Property: Formation consistently moves toward target boundary
     * For any formation, every update should move it closer to the target boundary.
     */
    @Test
    public void formationConsistentlyMovesTowardTarget() {
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
            
            // Get target boundary
            SpawnBoundary targetBoundary = formation.getTargetBoundary();
            
            // Verify target is opposite of spawn
            SpawnBoundary expectedTarget = getOppositeBoundary(spawnBoundary);
            assertEquals(
                expectedTarget,
                targetBoundary,
                "Trial " + trial + ": Target boundary should be opposite of spawn boundary"
            );
            
            // Track distance to target over multiple updates
            List<Bird> birds = formation.getBirds();
            Bird leadBird = birds.get(0);
            
            float previousDistance = getDistanceToTarget(
                leadBird.getX(), leadBird.getY(), 
                targetBoundary, viewWidth, viewHeight
            );
            
            // Update multiple times and verify distance decreases
            float timeStep = 0.1f;
            for (int update = 0; update < 20; update++) {
                formation.update(timeStep);
                
                float currentDistance = getDistanceToTarget(
                    leadBird.getX(), leadBird.getY(),
                    targetBoundary, viewWidth, viewHeight
                );
                
                // Distance should decrease (or stay same if already at boundary)
                assertTrue(
                    currentDistance <= previousDistance,
                    "Trial " + trial + ", Update " + update + 
                    ": Distance to target should decrease. Previous: " + previousDistance + 
                    ", Current: " + currentDistance
                );
                
                previousDistance = currentDistance;
                
                // Stop if reached target
                if (formation.hasReachedTarget(viewWidth, viewHeight)) {
                    break;
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
    
    /**
     * Helper method to get opposite boundary
     */
    private SpawnBoundary getOppositeBoundary(SpawnBoundary boundary) {
        switch (boundary) {
            case TOP: return SpawnBoundary.BOTTOM;
            case BOTTOM: return SpawnBoundary.TOP;
            case LEFT: return SpawnBoundary.RIGHT;
            case RIGHT: return SpawnBoundary.LEFT;
            default: return SpawnBoundary.BOTTOM;
        }
    }
    
    /**
     * Helper method to calculate distance to target boundary
     */
    private float getDistanceToTarget(float x, float y, SpawnBoundary target, 
                                     float viewWidth, float viewHeight) {
        switch (target) {
            case TOP:
                return viewHeight - y;
            case BOTTOM:
                return y;
            case LEFT:
                return x;
            case RIGHT:
                return viewWidth - x;
            default:
                return 0;
        }
    }
}
