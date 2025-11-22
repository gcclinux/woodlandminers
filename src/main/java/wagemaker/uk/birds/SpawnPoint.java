package wagemaker.uk.birds;

import com.badlogic.gdx.math.Vector2;

/**
 * Data class representing a spawn location and flight direction for a bird formation.
 */
public class SpawnPoint {
    public SpawnBoundary boundary;
    public float x;
    public float y;
    public Vector2 direction;

    public SpawnPoint(SpawnBoundary boundary, float x, float y, Vector2 direction) {
        this.boundary = boundary;
        this.x = x;
        this.y = y;
        this.direction = direction;
    }
}
