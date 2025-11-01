package com.example.game;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class Tree {
    private float x, y;
    private Texture texture;
    private int health = 100;
    private boolean showHealthBar = false;
    private float healthBarTimer = 0;

    public Tree(float x, float y) {
        this.x = x;
        this.y = y;
        createTexture();
    }

    private void createTexture() {
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        
        // leaves (green) - at bottom for libGDX
        pixmap.setColor(0.1f, 0.5f, 0.1f, 1);
        pixmap.fillCircle(32, 19, 18);
        
        // trunk (brown) - at top for libGDX
        pixmap.setColor(0.4f, 0.2f, 0.1f, 1);
        pixmap.fillRectangle(28, 34, 8, 30);
        
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
        // Collision box: 16px width centered (reduced by 8px each side), 48px height
        float treeCollisionX = x + 24; // center the 16px collision box (16 + 8)
        float treeCollisionWidth = 16; // reduced from 32px
        float treeCollisionY = y + 16; // 16px down from center
        float treeCollisionHeight = 48; // 32px up + 16px down
        
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
        // Tree attack range: 64px up/down, 64px left/right from center
        float treeCenterX = x + 32;
        float treeCenterY = y + 32;
        float playerCenterX = playerX + 32;
        float playerCenterY = playerY + 32;
        
        float dx = Math.abs(treeCenterX - playerCenterX);
        float dy = Math.abs(treeCenterY - playerCenterY);
        
        return dx <= 64 && dy <= 64;
    }
    
    public int getHealth() {
        return health;
    }

    public void dispose() {
        if (texture != null) texture.dispose();
    }
}