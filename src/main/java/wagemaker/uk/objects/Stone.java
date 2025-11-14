package wagemaker.uk.objects;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * Stone object that can be destroyed to collect pebbles.
 * Follows the SmallTree pattern for collision and health mechanics.
 * 
 * Sprite coordinates in assets.png: 384, 0, 64, 64 (TBD - placeholder coordinates)
 */
public class Stone {
    private float x, y;
    private Texture texture;
    private float health = 50;
    private float timeSinceLastAttack = 0;

    public Stone(float x, float y) {
        this.x = x;
        this.y = y;
        createTexture();
    }

    private void createTexture() {
        Texture spriteSheet = new Texture("sprites/assets.png");
        Pixmap pixmap = new Pixmap(128, 128, Pixmap.Format.RGBA8888);
        spriteSheet.getTextureData().prepare();
        Pixmap sheetPixmap = spriteSheet.getTextureData().consumePixmap();
        
        // TODO: Update sprite coordinates when final stone sprite is added to assets.png
        // Current placeholder coordinates: 384, 0, 64, 64
        pixmap.drawPixmap(sheetPixmap, 0, 0, 64, 192, 128, 128);
        
        texture = new Texture(pixmap);
        pixmap.dispose();
        sheetPixmap.dispose();
        spriteSheet.dispose();
    }

    public Texture getTexture() {
        return texture;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    /**
     * Checks collision with player using a 24x48 pixel collision box.
     * Collision box is offset to the right to reduce left-side collision.
     */
    public boolean collidesWith(float playerX, float playerY, float playerWidth, float playerHeight) {
        // Collision box: 24x48 pixels, offset 20px from left edge
        float stoneCollisionX = x + 8;  // Offset from left (minimal left collision)
        float stoneCollisionY = y + 8;   // Center vertically (64-48)/2 = 8
        float stoneCollisionWidth = 24;
        float stoneCollisionHeight = 48;
        
        return playerX < stoneCollisionX + stoneCollisionWidth && 
               playerX + playerWidth > stoneCollisionX && 
               playerY < stoneCollisionY + stoneCollisionHeight && 
               playerY + playerHeight > stoneCollisionY;
    }

    /**
     * Checks if player is within attack range of the stone.
     * Attack range: 80 pixels from stone center.
     */
    public boolean isInAttackRange(float playerX, float playerY) {
        float stoneCenterX = x + 32;  // Center of 64x64 sprite
        float stoneCenterY = y + 32;
        float playerCenterX = playerX + 50;  // Center of 100x100 player sprite
        float playerCenterY = playerY + 50;
        
        float dx = stoneCenterX - playerCenterX;
        float dy = stoneCenterY - playerCenterY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        return distance <= 80;
    }

    /**
     * Attacks the stone, reducing health by 10.
     * @return true if the stone is destroyed (health <= 0), false otherwise
     */
    public boolean attack() {
        health -= 10;
        timeSinceLastAttack = 0;
        return health <= 0;
    }
    
    /**
     * Updates stone state, handling health regeneration.
     * Health regenerates after 5 seconds of no attacks.
     */
    public void update(float deltaTime) {
        if (health < 50) {
            timeSinceLastAttack += deltaTime;
            if (timeSinceLastAttack >= 5.0f) {
                health = Math.min(50, health + deltaTime);
            }
        }
    }
    
    /**
     * @return true if health bar should be displayed (stone is damaged)
     */
    public boolean shouldShowHealthBar() {
        return health < 50;
    }
    
    /**
     * @return health as a percentage (0.0 to 1.0)
     */
    public float getHealthPercentage() {
        return Math.min(1.0f, health / 50.0f);
    }
    
    public float getHealth() {
        return health;
    }
    
    public void setHealth(float health) {
        this.health = Math.max(0, Math.min(50, health));
    }

    /**
     * Disposes of texture resources.
     */
    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
