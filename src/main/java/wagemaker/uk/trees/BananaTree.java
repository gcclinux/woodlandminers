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
        // Collision box: reduced by 8px left and 16px right
        float treeCollisionX = x + 56;
        float treeCollisionWidth = 8;
        float treeCollisionY = y + 28;
        float treeCollisionHeight = 52;
        
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
        // BananaTree attack range: 128px up/down, 64px left/right from center
        float treeCenterX = x + 64;
        float treeCenterY = y + 64;
        float playerCenterX = playerX + 32;
        float playerCenterY = playerY + 32;
        
        float dx = Math.abs(treeCenterX - playerCenterX);
        float dy = Math.abs(treeCenterY - playerCenterY);
        
        return dx <= 64 && dy <= 128;
    }
    
    public float getHealth() {
        return health;
    }

    public void dispose() {
        if (texture != null) texture.dispose();
    }
}
