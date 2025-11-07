package wagemaker.uk.trees;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class SmallTree {
    private float x, y;
    private Texture texture;
    private float health = 100;
    private float timeSinceLastAttack = 0;

    public SmallTree(float x, float y) {
        this.x = x;
        this.y = y;
        createTexture();
    }

    private void createTexture() {
        Texture spriteSheet = new Texture("sprites/assets.png");
        Pixmap pixmap = new Pixmap(64, 128, Pixmap.Format.RGBA8888);
        spriteSheet.getTextureData().prepare();
        Pixmap sheetPixmap = spriteSheet.getTextureData().consumePixmap();
        
        pixmap.drawPixmap(sheetPixmap, 0, 0, 320, 0, 64, 128);
        
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
        // Collision box: 32px width centered (16px left, 16px right)
        float treeCollisionX = x + 16;
        float treeCollisionWidth = 32;
        float treeCollisionY = y + 24;
        float treeCollisionHeight = 72;
        
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
        // Tree attack range: 96px up (top), 96px down (bottom), 64px left/right from center
        float treeCenterX = x + 32;
        float treeCenterY = y + 64;
        float playerCenterX = playerX + 32;
        float playerCenterY = playerY + 32;
        
        float dx = Math.abs(treeCenterX - playerCenterX);
        float dy = treeCenterY - playerCenterY;
        
        return dx <= 64 && dy >= -96 && dy <= 96;
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