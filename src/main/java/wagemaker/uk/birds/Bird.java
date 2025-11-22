package wagemaker.uk.birds;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * Individual bird entity with position and animation state.
 */
public class Bird {
    private float x;
    private float y;
    private Texture texture;
    private float animationTime;

    public Bird(float x, float y, Texture texture) {
        this.x = x;
        this.y = y;
        this.texture = texture;
        this.animationTime = 0f;
    }

    public void update(float deltaTime, Vector2 velocity) {
        this.x += velocity.x * deltaTime;
        this.y += velocity.y * deltaTime;
        this.animationTime += deltaTime;
    }

    public void render(SpriteBatch batch) {
        if (texture != null) {
            batch.draw(texture, x, y);
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void dispose() {
        // Individual birds don't own the texture, so no disposal needed here
    }
}
