package wagemaker.uk.trees;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class Cactus {
    private float x, y;
    private Texture texture;
    private float health = 200; // Double health compared to other trees
    private float timeSinceLastAttack = 0;

    public Cactus(float x, float y) {
        this.x = x;
        this.y = y;
        createTexture();
    }

    private void createTexture() {
        Texture spriteSheet = new Texture("sprites/assets.png");
        Pixmap pixmap = new Pixmap(64, 128, Pixmap.Format.RGBA8888);
        spriteSheet.getTextureData().prepare();
        Pixmap sheetPixmap = spriteSheet.getTextureData().consumePixmap();
        
        // Cactus coordinates: 0 from left, 192 from top, 64x128 size
        pixmap.drawPixmap(sheetPixmap, 0, 0, 0, 192, 64, 128);
        
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

    public boolean collidesWith(float playerX, float playerY, float playerWidth, float playerHeight) {
        // Collision box based on your specifications:
        // 16px left from center, 16px right from center = 32px width
        // 48px top down from center, 32px down up from center = 80px height
        float cactusCenterX = x + 32; // Cactus center (64px wide / 2)
        float cactusCenterY = y + 64; // Cactus center (128px tall / 2)
        
        float cactusCollisionX = cactusCenterX - 16; // 16px left from center (wider for easier collision)
        float cactusCollisionWidth = 32; // 16px left + 16px right (wider for easier collision)
        float cactusCollisionY = cactusCenterY - 40; // 40px down from center (lower collision box)
        float cactusCollisionHeight = 80; // 40px down + 40px up
        
        return playerX < cactusCollisionX + cactusCollisionWidth && playerX + playerWidth > cactusCollisionX && 
               playerY < cactusCollisionY + cactusCollisionHeight && playerY + playerHeight > cactusCollisionY;
    }

    public boolean attack() {
        health -= 5; // Half damage per attack (5 instead of 10)
        timeSinceLastAttack = 0;
        return health <= 0;
    }
    
    public void update(float deltaTime) {
        if (health < 200) {
            timeSinceLastAttack += deltaTime;
            if (timeSinceLastAttack >= 5.0f) {
                health = Math.min(200, health + deltaTime);
            }
        }
    }
    
    public boolean shouldShowHealthBar() {
        return health < 200; // Show health bar when damaged from max health of 200
    }
    
    public float getHealthPercentage() {
        return Math.min(1.0f, health / 200.0f);
    }
    
    public boolean isInAttackRange(float playerX, float playerY) {
        // Cactus attack range: 96px up (top), 96px down (bottom), 64px left/right from center
        float cactusCenterX = x + 32;
        float cactusCenterY = y + 64;
        float playerCenterX = playerX + 32;
        float playerCenterY = playerY + 32;
        
        float dx = Math.abs(cactusCenterX - playerCenterX);
        float dy = cactusCenterY - playerCenterY;
        
        return dx <= 64 && dy >= -96 && dy <= 96;
    }
    
    public float getHealth() {
        return health;
    }

    public void dispose() {
        if (texture != null) texture.dispose();
    }
}