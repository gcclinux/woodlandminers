package wagemaker.uk.trees;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class CoconutTree {
    private float x, y;
    private Texture texture;
    private int health = 100;
    private boolean showHealthBar = false;
    private float healthBarTimer = 0;

    public CoconutTree(float x, float y) {
        this.x = x;
        this.y = y;
        createTexture();
    }

    private void createTexture() {
        Texture spriteSheet = new Texture("sprites/assets.png");
        Pixmap pixmap = new Pixmap(128, 128, Pixmap.Format.RGBA8888);
        spriteSheet.getTextureData().prepare();
        Pixmap sheetPixmap = spriteSheet.getTextureData().consumePixmap();
        
        pixmap.drawPixmap(sheetPixmap, 0, 0, 0, 0, 128, 128);
        
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
        // Collision box: 16px width (16px left, 0px right)
        float treeCollisionX = x + 48;
        float treeCollisionWidth = 12;
        float treeCollisionY = y + 32;
        float treeCollisionHeight = 64;
        
        return playerX < treeCollisionX + treeCollisionWidth && playerX + playerWidth > treeCollisionX && 
               playerY < treeCollisionY + treeCollisionHeight && playerY + playerHeight > treeCollisionY;
    }

    public boolean attack() {
        health -= 10;
        showHealthBar = true;
        healthBarTimer = 3.0f;
        return health <= 0;
    }
    
    public void update(float deltaTime) {
        if (showHealthBar) {
            healthBarTimer -= deltaTime;
            if (healthBarTimer <= 0) {
                showHealthBar = false;
            }
        }
    }
    
    public boolean shouldShowHealthBar() {
        return showHealthBar;
    }
    
    public float getHealthPercentage() {
        return health / 100.0f;
    }
    
    public boolean isInAttackRange(float playerX, float playerY) {
        // CoconutTree attack range: 64px left/right, 96px down (bottom), 96px up (top) from center
        float treeCenterX = x + 64;
        float treeCenterY = y + 64;
        float playerCenterX = playerX + 32;
        float playerCenterY = playerY + 32;
        
        float dx = Math.abs(treeCenterX - playerCenterX);
        float dy = treeCenterY - playerCenterY;
        
        return dx <= 64 && dy >= -96 && dy <= 96;
    }
    
    public int getHealth() {
        return health;
    }

    public void dispose() {
        if (texture != null) texture.dispose();
    }
}