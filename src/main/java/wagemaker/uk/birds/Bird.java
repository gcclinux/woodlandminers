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
    private Texture texture1;
    private Texture texture2;
    private float animationTime;
    private static final float ANIMATION_FRAME_DURATION = 0.5f; // 0.5 seconds per frame

    public Bird(float x, float y, Texture texture1, Texture texture2) {
        this.x = x;
        this.y = y;
        this.texture1 = texture1;
        this.texture2 = texture2;
        this.animationTime = 0f;
    }
    
    /**
     * Legacy constructor for single texture (backwards compatibility)
     */
    public Bird(float x, float y, Texture texture) {
        this(x, y, texture, texture);
    }

    public void update(float deltaTime, Vector2 velocity) {
        this.x += velocity.x * deltaTime;
        this.y += velocity.y * deltaTime;
        this.animationTime += deltaTime;
    }

    public void render(SpriteBatch batch) {
        // Alternate between textures based on animation time
        Texture currentTexture = getCurrentTexture();
        if (currentTexture != null) {
            batch.draw(currentTexture, x, y);
        }
    }
    
    /**
     * Get the current texture based on animation time.
     * Alternates between texture1 and texture2 every 0.5 seconds.
     */
    private Texture getCurrentTexture() {
        if (texture1 == null) return texture2;
        if (texture2 == null) return texture1;
        
        // Calculate which frame we're on (0 or 1)
        int frame = (int) (animationTime / ANIMATION_FRAME_DURATION) % 2;
        return frame == 0 ? texture1 : texture2;
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
