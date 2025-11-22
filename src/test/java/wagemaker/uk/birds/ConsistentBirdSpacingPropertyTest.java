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
 * Property-based test for consistent bird spacing in formations.
 * Feature: flying-birds-ambient, Property 9: Consistent bird spacing
 * Validates: Requirements 2.4
 */
public class ConsistentBirdSpacingPropertyTest {
    
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
     * Helper method to calculate distance between two birds.
     */
    private float calculateDistance(Bird bird1, Bird bird2) {
        float dx = bird2.getX() - bird1.getX();
        float dy = bird2.getY() - bird1.getY();
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Helper method to calculate distances from lead bird to all wing birds.
     */
    private float[] calculateWingDistances(List<Bird> birds) {
        Bird leadBird = birds.get(0);
        float[] distances = new float[4];
        
        for (int i = 0; i < 4; i++) {
            distances[i] = calculateDistance(leadBird, birds.get(i + 1));
        }
        
        return distances;
    }
    
    /**
     * Property 9: Consistent bird spacing
     * For any bird formation, the distance between adjacent birds in the formation
     * should remain constant throughout the flight.
     * Validates: Requirements 2.4
     * 
     * This property-based test runs 100 trials with randomly generated spawn points
     * and verifies that bird spacing remains consistent during flight.
     */
    @Test
    public void birdSpacingRemainsConsistentDuringFlight() {
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
            
            // Record initial distances from lead bird to each wing bird
            float[] initialDistances = calculateWingDistances(birds);
            
            // Update the formation multiple times
            int updateCount = random.nextInt(50) + 10; // 10 to 59 updates
            for (int update = 0; update < updateCount; update++) {
                float deltaTime = random.nextFloat() * 0.1f; // 0 to 0.1 seconds
                formation.update(deltaTime);
                
                // Calculate current distances
                float[] currentDistances = calculateWingDistances(birds);
                
                // Verify distances remain constant (within tolerance)
                for (int i = 0; i < 4; i++) {
                    assertEquals(initialDistances[i], currentDistances[i], 0.1f,
                        "Trial " + trial + ", Update " + update + ": " +
                        "Distance from lead bird to wing bird " + (i + 1) + 
                        " should remain constant during flight");
                }
            }
            
            // Clean up
            formation.dispose();
        }
    }
    
    /**
     * Property: Adjacent wing birds maintain consistent spacing
     * For any bird formation, the distance between adjacent wing birds on the same side
     * should remain constant throughout the flight.
     * 
     * This property-based test runs 100 trials and verifies that adjacent wing birds
     * maintain their relative spacing during flight.
     */
    @Test
    public void adjacentWingBirdsMaintainSpacing() {
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
            
            // Record initial distances between adjacent wing birds
            // Left wing: bird 1 to bird 2
            float initialLeftWingDistance = calculateDistance(birds.get(1), birds.get(2));
            // Right wing: bird 3 to bird 4
            float initialRightWingDistance = calculateDistance(birds.get(3), birds.get(4));
            
            // Update the formation multiple times
            int updateCount = random.nextInt(50) + 10; // 10 to 59 updates
            for (int update = 0; update < updateCount; update++) {
                float deltaTime = random.nextFloat() * 0.1f; // 0 to 0.1 seconds
                formation.update(deltaTime);
                
                // Calculate current distances
                float currentLeftWingDistance = calculateDistance(birds.get(1), birds.get(2));
                float currentRightWingDistance = calculateDistance(birds.get(3), birds.get(4));
                
                // Verify distances remain constant (within tolerance)
                assertEquals(initialLeftWingDistance, currentLeftWingDistance, 0.1f,
                    "Trial " + trial + ", Update " + update + ": " +
                    "Distance between left wing birds should remain constant");
                
                assertEquals(initialRightWingDistance, currentRightWingDistance, 0.1f,
                    "Trial " + trial + ", Update " + update + ": " +
                    "Distance between right wing birds should remain constant");
            }
            
            // Clean up
            formation.dispose();
        }
    }
    
    /**
     * Property: Wing symmetry is maintained
     * For any bird formation, the distances from the lead bird to corresponding
     * left and right wing birds should be equal throughout the flight.
     * 
     * This property-based test runs 100 trials and verifies that the formation
     * maintains symmetry during flight.
     */
    @Test
    public void wingSymmetryMaintainedDuringFlight() {
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
            
            // Update the formation multiple times
            int updateCount = random.nextInt(50) + 10; // 10 to 59 updates
            for (int update = 0; update < updateCount; update++) {
                float deltaTime = random.nextFloat() * 0.1f; // 0 to 0.1 seconds
                formation.update(deltaTime);
                
                // Verify symmetry: corresponding left and right wing birds should be
                // equidistant from the lead bird
                Bird leadBird = birds.get(0);
                
                for (int i = 1; i <= 2; i++) {
                    float leftDistance = calculateDistance(leadBird, birds.get(i));
                    float rightDistance = calculateDistance(leadBird, birds.get(i + 2));
                    
                    assertEquals(leftDistance, rightDistance, 0.1f,
                        "Trial " + trial + ", Update " + update + ": " +
                        "Wing bird " + i + " distances should be symmetric");
                }
            }
            
            // Clean up
            formation.dispose();
        }
    }
    
    /**
     * Property: Spacing is consistent across all spawn boundaries
     * For any spawn boundary (TOP, BOTTOM, LEFT, RIGHT), the bird spacing
     * should be consistent and maintained during flight.
     * 
     * This property-based test runs 100 trials, systematically testing all
     * spawn boundaries to ensure consistent spacing behavior.
     */
    @Test
    public void spacingConsistentAcrossAllBoundaries() {
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
            
            // Record initial distances
            float[] initialDistances = calculateWingDistances(birds);
            
            // Update the formation several times
            for (int update = 0; update < 20; update++) {
                formation.update(0.05f); // 50ms per update
                
                // Verify distances remain constant
                float[] currentDistances = calculateWingDistances(birds);
                
                for (int i = 0; i < 4; i++) {
                    assertEquals(initialDistances[i], currentDistances[i], 0.1f,
                        "Trial " + trial + " (boundary=" + boundary + "), Update " + update + ": " +
                        "Distance to wing bird " + (i + 1) + " should remain constant");
                }
            }
            
            // Clean up
            formation.dispose();
        }
    }
}
