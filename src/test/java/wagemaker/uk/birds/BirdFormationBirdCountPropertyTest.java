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

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for bird formation bird count.
 * Feature: flying-birds-ambient, Property 6: Formation contains exactly 5 birds
 * Validates: Requirements 2.1
 */
public class BirdFormationBirdCountPropertyTest {
    
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
     * Property 6: Formation contains exactly 5 birds
     * For any spawned bird formation, the formation should contain exactly 5 bird entities
     * Validates: Requirements 2.1
     * 
     * This property-based test runs 100 trials with randomly generated spawn points
     * and velocities, verifying that every formation contains exactly 5 birds.
     */
    @Test
    public void formationContainsExactlyFiveBirds() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Generate random spawn point
            SpawnBoundary boundary = SpawnBoundary.values()[random.nextInt(SpawnBoundary.values().length)];
            float x = random.nextFloat() * 1000f; // Random x position
            float y = random.nextFloat() * 1000f; // Random y position
            
            // Generate random direction (normalized)
            float dirX = random.nextFloat() * 2f - 1f; // Range: -1 to 1
            float dirY = random.nextFloat() * 2f - 1f; // Range: -1 to 1
            Vector2 direction = new Vector2(dirX, dirY).nor();
            
            SpawnPoint spawnPoint = new SpawnPoint(boundary, x, y, direction);
            
            // Generate random velocity
            float speed = 50f + random.nextFloat() * 150f; // Range: 50 to 200 pixels/second
            Vector2 velocity = direction.cpy().scl(speed);
            
            // Create bird formation
            BirdFormation formation = new BirdFormation(spawnPoint, velocity, mockTexture);
            
            // Verify the formation contains exactly 5 birds
            assertNotNull(
                formation.getBirds(),
                "Trial " + trial + ": Formation should have a non-null bird list"
            );
            
            assertEquals(
                5,
                formation.getBirds().size(),
                "Trial " + trial + ": Formation should contain exactly 5 birds " +
                "(boundary=" + boundary + ", x=" + x + ", y=" + y + ", " +
                "direction=" + direction + ", speed=" + speed + ")"
            );
            
            // Verify all birds are non-null
            for (int i = 0; i < formation.getBirds().size(); i++) {
                assertNotNull(
                    formation.getBirds().get(i),
                    "Trial " + trial + ": Bird at index " + i + " should not be null"
                );
            }
            
            // Clean up
            formation.dispose();
        }
    }
    
    /**
     * Property: Formation bird count is invariant
     * For any bird formation, the number of birds should remain constant (5)
     * throughout its lifecycle, even after updates.
     * 
     * This property-based test runs 100 trials, creating formations and updating
     * them multiple times to verify the bird count remains constant.
     */
    @Test
    public void formationBirdCountIsInvariant() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Generate random spawn point
            SpawnBoundary boundary = SpawnBoundary.values()[random.nextInt(SpawnBoundary.values().length)];
            float x = random.nextFloat() * 1000f;
            float y = random.nextFloat() * 1000f;
            Vector2 direction = new Vector2(
                random.nextFloat() * 2f - 1f,
                random.nextFloat() * 2f - 1f
            ).nor();
            
            SpawnPoint spawnPoint = new SpawnPoint(boundary, x, y, direction);
            Vector2 velocity = direction.cpy().scl(100f);
            
            // Create bird formation
            BirdFormation formation = new BirdFormation(spawnPoint, velocity, mockTexture);
            
            // Initial bird count should be 5
            assertEquals(5, formation.getBirds().size(), 
                "Trial " + trial + ": Initial bird count should be 5");
            
            // Update the formation multiple times
            int updateCount = random.nextInt(50) + 10; // 10 to 59 updates
            for (int update = 0; update < updateCount; update++) {
                float deltaTime = random.nextFloat() * 0.1f; // 0 to 0.1 seconds
                formation.update(deltaTime);
                
                // Bird count should remain 5 after each update
                assertEquals(5, formation.getBirds().size(),
                    "Trial " + trial + ", Update " + update + ": Bird count should remain 5 after update");
            }
            
            // Clean up
            formation.dispose();
        }
    }
    
    /**
     * Property: All spawn boundaries produce formations with 5 birds
     * For any spawn boundary (TOP, BOTTOM, LEFT, RIGHT), the resulting formation
     * should contain exactly 5 birds.
     * 
     * This property-based test runs 100 trials, systematically testing all
     * spawn boundaries to ensure they all produce valid 5-bird formations.
     */
    @Test
    public void allSpawnBoundariesProduceFiveBirdFormations() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials (25 per boundary)
        for (int trial = 0; trial < 100; trial++) {
            // Cycle through all boundaries
            SpawnBoundary boundary = SpawnBoundary.values()[trial % SpawnBoundary.values().length];
            
            // Generate random position and direction based on boundary
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
            
            // Verify the formation contains exactly 5 birds
            assertEquals(
                5,
                formation.getBirds().size(),
                "Trial " + trial + ": Formation spawned from " + boundary + 
                " boundary should contain exactly 5 birds"
            );
            
            // Clean up
            formation.dispose();
        }
    }
}
