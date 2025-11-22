package wagemaker.uk.birds;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an active formation of 5 birds flying across the screen in a V-shape.
 */
public class BirdFormation {
    private List<Bird> birds;
    private Vector2 velocity;
    private SpawnBoundary spawnBoundary;
    private SpawnBoundary targetBoundary;
    private boolean active;

    public BirdFormation(SpawnPoint spawnPoint, Vector2 velocity, Texture birdTexture) {
        this.birds = new ArrayList<>();
        this.velocity = velocity;
        this.spawnBoundary = spawnPoint.boundary;
        this.targetBoundary = getOppositeBoundary(spawnPoint.boundary);
        this.active = true;
        
        initializeVFormation(spawnPoint, birdTexture);
    }

    private void initializeVFormation(SpawnPoint spawnPoint, Texture birdTexture) {
        // V-formation with 1 lead bird and 2 birds on each wing
        // Formation angle: 30 degrees, spacing: 40 pixels
        
        float leadX = spawnPoint.x;
        float leadY = spawnPoint.y;
        
        // Lead bird at the front
        birds.add(new Bird(leadX, leadY, birdTexture));
        
        // Calculate perpendicular direction for wing positioning
        Vector2 perpendicular = new Vector2(-spawnPoint.direction.y, spawnPoint.direction.x);
        
        // Wing spacing and offset
        float spacing = 40f;
        float backwardOffset = 30f; // Birds trail behind at 30-degree angle
        
        // Left wing (2 birds)
        for (int i = 1; i <= 2; i++) {
            float offsetX = perpendicular.x * spacing * i - spawnPoint.direction.x * backwardOffset * i;
            float offsetY = perpendicular.y * spacing * i - spawnPoint.direction.y * backwardOffset * i;
            birds.add(new Bird(leadX + offsetX, leadY + offsetY, birdTexture));
        }
        
        // Right wing (2 birds)
        for (int i = 1; i <= 2; i++) {
            float offsetX = -perpendicular.x * spacing * i - spawnPoint.direction.x * backwardOffset * i;
            float offsetY = -perpendicular.y * spacing * i - spawnPoint.direction.y * backwardOffset * i;
            birds.add(new Bird(leadX + offsetX, leadY + offsetY, birdTexture));
        }
    }

    private SpawnBoundary getOppositeBoundary(SpawnBoundary boundary) {
        switch (boundary) {
            case TOP: return SpawnBoundary.BOTTOM;
            case BOTTOM: return SpawnBoundary.TOP;
            case LEFT: return SpawnBoundary.RIGHT;
            case RIGHT: return SpawnBoundary.LEFT;
            default: return SpawnBoundary.BOTTOM;
        }
    }

    public void update(float deltaTime) {
        for (Bird bird : birds) {
            bird.update(deltaTime, velocity);
        }
    }

    public void render(SpriteBatch batch) {
        for (Bird bird : birds) {
            bird.render(batch);
        }
    }

    public boolean hasReachedTarget(float viewWidth, float viewHeight) {
        if (birds.isEmpty()) {
            return false;
        }
        
        // Check lead bird position against target boundary
        Bird leadBird = birds.get(0);
        float x = leadBird.getX();
        float y = leadBird.getY();
        
        switch (targetBoundary) {
            case TOP:
                return y > viewHeight;
            case BOTTOM:
                return y < 0;
            case LEFT:
                return x < 0;
            case RIGHT:
                return x > viewWidth;
            default:
                return false;
        }
    }

    public void dispose() {
        for (Bird bird : birds) {
            bird.dispose();
        }
        birds.clear();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Bird> getBirds() {
        return birds;
    }

    public SpawnBoundary getSpawnBoundary() {
        return spawnBoundary;
    }

    public SpawnBoundary getTargetBoundary() {
        return targetBoundary;
    }
}
