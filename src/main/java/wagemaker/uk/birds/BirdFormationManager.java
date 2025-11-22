package wagemaker.uk.birds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

/**
 * Central manager class that coordinates spawning, updating, and rendering of bird formations.
 */
public class BirdFormationManager {
    private BirdFormation activeFormation;
    private float spawnTimer;
    private float nextSpawnInterval;
    private Random random;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpawnBoundary lastSpawnBoundary;
    private Texture birdTexture;
    
    private static final float MIN_SPAWN_INTERVAL = 180f; // 3 minutes
    private static final float MAX_SPAWN_INTERVAL = 300f; // 5 minutes
    private static final float BIRD_SPEED = 100f; // pixels per second

    public BirdFormationManager(OrthographicCamera camera, Viewport viewport) {
        this.camera = camera;
        this.viewport = viewport;
        this.random = new Random();
        this.activeFormation = null;
        this.lastSpawnBoundary = null;
    }

    /**
     * Constructor for testing that accepts a pre-loaded texture
     */
    public BirdFormationManager(OrthographicCamera camera, Viewport viewport, Texture texture) {
        this.camera = camera;
        this.viewport = viewport;
        this.random = new Random();
        this.activeFormation = null;
        this.lastSpawnBoundary = null;
        this.birdTexture = texture;
        
        // Initialize spawn timer with first random interval
        nextSpawnInterval = generateRandomInterval();
        spawnTimer = nextSpawnInterval;
    }

    public void initialize() {
        // Load bird texture
        try {
            birdTexture = new Texture(Gdx.files.internal("assets/sprites/bird.png"));
        } catch (Exception e) {
            System.err.println("Failed to load bird texture: " + e.getMessage());
            birdTexture = null;
        }
        
        // Initialize spawn timer with first random interval
        nextSpawnInterval = generateRandomInterval();
        spawnTimer = nextSpawnInterval;
    }

    public void update(float deltaTime, float playerX, float playerY) {
        if (birdTexture == null) {
            return; // Bird system disabled if texture failed to load
        }
        
        // Update active formation
        if (activeFormation != null) {
            activeFormation.update(deltaTime);
            
            // Check if formation has reached target
            if (activeFormation.hasReachedTarget(viewport.getWorldWidth(), viewport.getWorldHeight())) {
                despawnFormation();
            }
        }
        
        // Update spawn timer
        if (activeFormation == null) {
            spawnTimer -= deltaTime;
            
            if (spawnTimer <= 0) {
                spawnFormation();
            }
        }
    }

    public void render(SpriteBatch batch) {
        if (activeFormation != null && birdTexture != null) {
            activeFormation.render(batch);
        }
    }

    public void dispose() {
        if (activeFormation != null) {
            activeFormation.dispose();
        }
        if (birdTexture != null) {
            birdTexture.dispose();
        }
    }

    private void spawnFormation() {
        SpawnPoint spawnPoint = selectRandomSpawnPoint();
        Vector2 velocity = spawnPoint.direction.cpy().scl(BIRD_SPEED);
        
        activeFormation = new BirdFormation(spawnPoint, velocity, birdTexture);
        lastSpawnBoundary = spawnPoint.boundary;
        
        // Reset timer for next spawn
        nextSpawnInterval = generateRandomInterval();
        spawnTimer = nextSpawnInterval;
    }

    private void despawnFormation() {
        if (activeFormation != null) {
            activeFormation.dispose();
            activeFormation = null;
        }
        
        // Reset timer for next spawn
        nextSpawnInterval = generateRandomInterval();
        spawnTimer = nextSpawnInterval;
    }

    private float generateRandomInterval() {
        return MIN_SPAWN_INTERVAL + random.nextFloat() * (MAX_SPAWN_INTERVAL - MIN_SPAWN_INTERVAL);
    }

    private SpawnPoint selectRandomSpawnPoint() {
        float viewWidth = viewport.getWorldWidth();
        float viewHeight = viewport.getWorldHeight();
        
        // Select random boundary
        SpawnBoundary[] boundaries = SpawnBoundary.values();
        SpawnBoundary boundary = boundaries[random.nextInt(boundaries.length)];
        
        // Ensure variation from last spawn
        if (lastSpawnBoundary != null && boundaries.length > 1) {
            int attempts = 0;
            while (boundary == lastSpawnBoundary && attempts < 10) {
                boundary = boundaries[random.nextInt(boundaries.length)];
                attempts++;
            }
        }
        
        float x, y;
        Vector2 direction;
        
        switch (boundary) {
            case TOP:
                x = random.nextFloat() * viewWidth;
                y = viewHeight;
                direction = new Vector2(0, -1); // Fly downward
                break;
            case BOTTOM:
                x = random.nextFloat() * viewWidth;
                y = 0;
                direction = new Vector2(0, 1); // Fly upward
                break;
            case LEFT:
                x = 0;
                y = random.nextFloat() * viewHeight;
                direction = new Vector2(1, 0); // Fly right
                break;
            case RIGHT:
                x = viewWidth;
                y = random.nextFloat() * viewHeight;
                direction = new Vector2(-1, 0); // Fly left
                break;
            default:
                x = 0;
                y = 0;
                direction = new Vector2(1, 0);
        }
        
        return new SpawnPoint(boundary, x, y, direction);
    }

    public BirdFormation getActiveFormation() {
        return activeFormation;
    }

    public float getSpawnTimer() {
        return spawnTimer;
    }

    public float getNextSpawnInterval() {
        return nextSpawnInterval;
    }
}
