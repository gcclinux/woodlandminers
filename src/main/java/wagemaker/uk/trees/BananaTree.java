package wagemaker.uk.trees;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class BananaTree {
    private float x, y;
    private Texture texture;
    private float health = 100;
    private float timeSinceLastAttack = 0;

    public BananaTree(float x, float y) {
        this.x = x;
        this.y = y;
        createTexture();
    }

    private void createTexture() {
        Texture spriteSheet = new Texture("sprites/assets.png");
        Pixmap pixmap = new Pixmap(128, 128, Pixmap.Format.RGBA8888);
        spriteSheet.getTextureData().prepare();
        Pixmap sheetPixmap = spriteSheet.getTextureData().consumePixmap();
        
        pixmap.drawPixmap(sheetPixmap, 0, 0, 384, 0, 128, 128);
        
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
        // Collision box for banana tree trunk (tree sprite is 64x128, center at 32,64)
        float treeCollisionX = x + 16;      // 16px from left edge (16px left of horizontal center)
        float treeCollisionWidth = 16;      // 16px width (ends at horizontal center)
        float treeCollisionY = y - 16;      // -16px from bottom (16px above vertical center)
        float treeCollisionHeight = 84;     // 64px height (ends 48px above vertical center)
        
        return playerX < treeCollisionX + treeCollisionWidth && playerX + playerWidth > treeCollisionX && 
               playerY < treeCollisionY + treeCollisionHeight && playerY + playerHeight > treeCollisionY;
    }

    public boolean attack() {
        health -= 10;
        timeSinceLastAttack = 0;
        return health <= 0;
    }
    
    public void update(float deltaTime) {
        if (health < 100) {
            timeSinceLastAttack += deltaTime;
            if (timeSinceLastAttack >= 5.0f) {
                health = Math.min(100, health + deltaTime);
            }
        }
    }
    
    public boolean shouldShowHealthBar() {
        return health < 100;
    }
    
    public float getHealthPercentage() {
        return Math.min(1.0f, health / 100.0f);
    }
    
    public boolean isInAttackRange(float playerX, float playerY) {
        // BananaTree attack range: 128px up/down, asymmetric left/right
        float treeCenterX = x + 32;  // Tree horizontal center
        float treeCenterY = y + 64;  // Tree vertical center
        float playerCenterX = playerX + 28;
        float playerCenterY = playerY + 28;
        
        float dx = playerCenterX - treeCenterX;  // Positive = player right of tree
        float dy = Math.abs(treeCenterY - playerCenterY);
        
        return dx >= -96 && dx <= 32 && dy <= 128;  // 96px left, 32px right, 128px up/down
    }
    
    public float getHealth() {
        return health;
    }
    
    public void setHealth(float health) {
        this.health = Math.max(0, Math.min(100, health));
    }

    public void dispose() {
        if (texture != null) texture.dispose();
    }
}
