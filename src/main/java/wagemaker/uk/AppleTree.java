package wagemaker.uk;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class AppleTree {
    private float x, y;
    private Texture texture;
    private int health = 100;
    private boolean showHealthBar = false;
    private float healthBarTimer = 0;

    public AppleTree(float x, float y) {
        this.x = x;
        this.y = y;
        createTexture();
    }

    private void createTexture() {
        Pixmap pixmap = new Pixmap(128, 128, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        
        // leaves (green) - at bottom for libGDX
        pixmap.setColor(0.1f, 0.5f, 0.1f, 1);
        pixmap.fillCircle(64, 38, 36);
        
        // trunk (brown) - at top for libGDX
        pixmap.setColor(0.4f, 0.2f, 0.1f, 1);
        pixmap.fillRectangle(56, 68, 16, 60);
        
        // red apples scattered on leaves
        pixmap.setColor(0.8f, 0.1f, 0.1f, 1);
        pixmap.fillCircle(50, 30, 3);
        pixmap.fillCircle(78, 25, 3);
        pixmap.fillCircle(60, 45, 3);
        pixmap.fillCircle(85, 40, 3);
        pixmap.fillCircle(45, 50, 3);
        
        texture = new Texture(pixmap);
        pixmap.dispose();
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
        // Collision box: 48px width centered (reduced by 8px each side), 96px height
        float treeCollisionX = x + 40; // center the 48px collision box (32 + 8)
        float treeCollisionWidth = 48; // reduced from 64px
        float treeCollisionY = y + 32; // 32px down from center
        float treeCollisionHeight = 96; // 64px up + 32px down
        
        return playerX < treeCollisionX + treeCollisionWidth && playerX + playerWidth > treeCollisionX && 
               playerY < treeCollisionY + treeCollisionHeight && playerY + playerHeight > treeCollisionY;
    }

    public boolean attack() {
        health -= 10;
        showHealthBar = true;
        healthBarTimer = 3.0f; // show for 3 seconds
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
        // AppleTree attack range: 128px up/down, 64px left/right from center
        float treeCenterX = x + 64;
        float treeCenterY = y + 64;
        float playerCenterX = playerX + 32;
        float playerCenterY = playerY + 32;
        
        float dx = Math.abs(treeCenterX - playerCenterX);
        float dy = Math.abs(treeCenterY - playerCenterY);
        
        return dx <= 64 && dy <= 128;
    }
    
    public int getHealth() {
        return health;
    }

    public void dispose() {
        if (texture != null) texture.dispose();
    }
}