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
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for V-shape arrangement of bird formations.
 * Feature: flying-birds-ambient, Property 7: V-shape arrangement
 * Validates: Requirements 2.2
 */
public class VShapeArrangementPropertyTest {
    
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
     * Property 7: V-shape arrangement
     * For any bird formation, the relative positions of the 5 birds should form a V-shape
     * with one lead bird and two trailing birds on each side.
     * Validates: Requirements 2.2
     * 
     * This property-based test runs 100 trials with randomly generated spawn points
     * and verifies that the birds are arranged in a V-shape pattern.
     */
    @Test
    public void birdsArrangedInVShape() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Generate random spawn point
            SpawnBoundary boundary = SpawnBoundary.values()[random.nextInt(SpawnBoundary.values().length)];
            float x = random.nextFloat() * 1000f;
            float y = random.nextFloat() * 1000f;
            
            // Generate random direction (normalized)
            float dirX = random.nextFloat() * 2f - 1f;
            float dirY = random.nextFloat() * 2f - 1f;
            Vector2 direction = new Vector2(dirX, dirY).nor();
            
            SpawnPoint spawnPoint = new SpawnPoint(boundary, x, y, direction);
            Vector2 velocity = direction.cpy().scl(100f);
            
            // Create bird formation
            BirdFormation formation = new BirdFormation(spawnPoint, velocity, mockTexture);
            List<Bird> birds = formation.getBirds();
            
            // Verify we have exactly 5 birds
            assertEquals(5, birds.size(), 
                "Trial " + trial + ": Formation should have 5 birds");
            
            // Bird 0 is the lead bird
            Bird leadBird = birds.get(0);
            float leadX = leadBird.getX();
            float leadY = leadBird.getY();
            
            // Verify lead bird is at spawn point
            assertEquals(spawnPoint.x, leadX, 0.1f,
                "Trial " + trial + ": Lead bird X should be at spawn point X");
            assertEquals(spawnPoint.y, leadY, 0.1f,
                "Trial " + trial + ": Lead bird Y should be at spawn point Y");
            
            // Calculate perpendicular direction for wing verification
            Vector2 perpendicular = new Vector2(-direction.y, direction.x);
            
            // Verify left wing birds (indices 1 and 2)
            for (int i = 1; i <= 2; i++) {
                Bird leftWingBird = birds.get(i);
                
                // Calculate expected position
                float spacing = 40f;
                float backwardOffset = 30f;
                float expectedOffsetX = perpendicular.x * spacing * i - direction.x * backwardOffset * i;
                float expectedOffsetY = perpendicular.y * spacing * i - direction.y * backwardOffset * i;
                float expectedX = leadX + expectedOffsetX;
                float expectedY = leadY + expectedOffsetY;
                
                // Verify position (with tolerance for floating point)
                assertEquals(expectedX, leftWingBird.getX(), 0.1f,
                    "Trial " + trial + ": Left wing bird " + i + " X position incorrect");
                assertEquals(expectedY, leftWingBird.getY(), 0.1f,
                    "Trial " + trial + ": Left wing bird " + i + " Y position incorrect");
                
                // Verify bird is behind the lead bird (in direction of flight)
                Vector2 toWingBird = new Vector2(
                    leftWingBird.getX() - leadX,
                    leftWingBird.getY() - leadY
                );
                float dotProduct = toWingBird.dot(direction);
                assertTrue(dotProduct < 0,
                    "Trial " + trial + ": Left wing bird " + i + " should be behind lead bird");
            }
            
            // Verify right wing birds (indices 3 and 4)
            for (int i = 1; i <= 2; i++) {
                Bird rightWingBird = birds.get(i + 2);
                
                // Calculate expected position
                float spacing = 40f;
                float backwardOffset = 30f;
                float expectedOffsetX = -perpendicular.x * spacing * i - direction.x * backwardOffset * i;
                float expectedOffsetY = -perpendicular.y * spacing * i - direction.y * backwardOffset * i;
                float expectedX = leadX + expectedOffsetX;
                float expectedY = leadY + expectedOffsetY;
                
                // Verify position (with tolerance for floating point)
                assertEquals(expectedX, rightWingBird.getX(), 0.1f,
                    "Trial " + trial + ": Right wing bird " + i + " X position incorrect");
                assertEquals(expectedY, rightWingBird.getY(), 0.1f,
                    "Trial " + trial + ": Right wing bird " + i + " Y position incorrect");
                
                // Verify bird is behind the lead bird (in direction of flight)
                Vector2 toWingBird = new Vector2(
                    rightWingBird.getX() - leadX,
                    rightWingBird.getY() - leadY
                );
                float dotProduct = toWingBird.dot(direction);
                assertTrue(dotProduct < 0,
                    "Trial " + trial + ": Right wing bird " + i + " should be behind lead bird");
            }
            
            // Verify symmetry: left and right wing birds should be equidistant from lead
            for (int i = 1; i <= 2; i++) {
                Bird leftWingBird = birds.get(i);
                Bird rightWingBird = birds.get(i + 2);
                
                float leftDistance = (float) Math.sqrt(
                    Math.pow(leftWingBird.getX() - leadX, 2) +
                    Math.pow(leftWingBird.getY() - leadY, 2)
                );
                
                float rightDistance = (float) Math.sqrt(
                    Math.pow(rightWingBird.getX() - leadX, 2) +
                    Math.pow(rightWingBird.getY() - leadY, 2)
                );
                
                assertEquals(leftDistance, rightDistance, 0.1f,
                    "Trial " + trial + ": Wing bird " + i + " distances should be symmetric");
            }
            
            // Clean up
            formation.dispose();
        }
    }
    
    /**
     * Property: V-shape arrangement for all spawn boundaries
     * For any spawn boundary (TOP, BOTTOM, LEFT, RIGHT), the resulting formation
     * should have birds arranged in a V-shape.
     * 
     * This property-based test runs 100 trials, systematically testing all
     * spawn boundaries to ensure they all produce valid V-shape formations.
     */
    @Test
    public void vShapeArrangementForAllBoundaries() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials (25 per boundary)
        for (int trial = 0; trial < 100; trial++) {
            // Cycle through all boundaries
            SpawnBoundary boundary = SpawnBoundary.values()[trial % SpawnBoundary.values().length];
            
            // Generate position and direction based on boundary
            float x, y;
            Vector2 direction;
            
            switch (boundary) {
                case TOP:
                    x = random.nextFloat() * 1000f;
                    y = 1000f;
                    direction = new Vector2(0, -1);
                    break;
                case BOTTOM:
                    x = random.nextFloat() * 1000f;
                    y = 0f;
                    direction = new Vector2(0, 1);
                    break;
                case LEFT:
                    x = 0f;
                    y = random.nextFloat() * 1000f;
                    direction = new Vector2(1, 0);
                    break;
                case RIGHT:
                    x = 1000f;
                    y = random.nextFloat() * 1000f;
                    direction = new Vector2(-1, 0);
                    break;
                default:
                    x = 0f;
                    y = 0f;
                    direction = new Vector2(1, 0);
            }
            
            SpawnPoint spawnPoint = new SpawnPoint(boundary, x, y, direction);
            Vector2 velocity = direction.cpy().scl(100f);
            
            // Create bird formation
            BirdFormation formation = new BirdFormation(spawnPoint, velocity, mockTexture);
            List<Bird> birds = formation.getBirds();
            
            // Verify V-shape: lead bird at front, 2 on each wing behind
            Bird leadBird = birds.get(0);
            
            // All wing birds should be behind the lead bird
            for (int i = 1; i < 5; i++) {
                Bird wingBird = birds.get(i);
                Vector2 toWingBird = new Vector2(
                    wingBird.getX() - leadBird.getX(),
                    wingBird.getY() - leadBird.getY()
                );
                float dotProduct = toWingBird.dot(direction);
                assertTrue(dotProduct < 0,
                    "Trial " + trial + " (boundary=" + boundary + "): " +
                    "Wing bird " + i + " should be behind lead bird");
            }
            
            // Clean up
            formation.dispose();
        }
    }
}
