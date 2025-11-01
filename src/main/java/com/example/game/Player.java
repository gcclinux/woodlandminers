package com.example.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import java.util.Map;

public class Player {
    private float x, y;
    private float speed = 200;
    private float animTime = 0;
    private Texture texture;
    private OrthographicCamera camera;
    private Map<String, Tree> trees;
    private Map<String, AppleTree> appleTrees;
    private Map<String, Boolean> clearedPositions;

    public Player(float startX, float startY, OrthographicCamera camera) {
        this.x = startX;
        this.y = startY;
        this.camera = camera;
    }
    
    public void setTrees(Map<String, Tree> trees) {
        this.trees = trees;
    }
    
    public void setAppleTrees(Map<String, AppleTree> appleTrees) {
        this.appleTrees = appleTrees;
    }
    
    public void setClearedPositions(Map<String, Boolean> clearedPositions) {
        this.clearedPositions = clearedPositions;
    }

    public void update(float deltaTime) {
        boolean isMoving = false;
        
        // handle input with collision detection
        float newX = x;
        float newY = y;
        
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) { 
            newX = x - speed * deltaTime;
            if (!wouldCollide(newX, y)) {
                x = newX;
                isMoving = true;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) { 
            newX = x + speed * deltaTime;
            if (!wouldCollide(newX, y)) {
                x = newX;
                isMoving = true;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) { 
            newY = y + speed * deltaTime;
            if (!wouldCollide(x, newY)) {
                y = newY;
                isMoving = true;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) { 
            newY = y - speed * deltaTime;
            if (!wouldCollide(x, newY)) {
                y = newY;
                isMoving = true;
            }
        }

        // handle attack
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            System.out.println("Attack pressed at player position: " + x + ", " + y);
            attackNearbyTrees();
        }

        // update animation
        if (isMoving) animTime += deltaTime * 4;
        
        // update camera to follow player
        updateCamera();

        // update texture
        updateTexture();
    }

    private void updateTexture() {
        if (texture != null) texture.dispose();
        
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        
        // head
        pixmap.setColor(0.9f, 0.7f, 0.5f, 1);
        pixmap.fillCircle(32, 12, 10);
        
        // hair (black)
        pixmap.setColor(0.1f, 0.1f, 0.1f, 1);
        pixmap.fillCircle(32, 12, 8);
        
        // animated arms (next to body)
        pixmap.setColor(0.9f, 0.7f, 0.5f, 1);
        boolean leftLong = (int)(animTime) % 2 == 0;
        int leftArmLength = leftLong ? 16 : 10;
        int rightArmLength = leftLong ? 10 : 16;
        pixmap.fillRectangle(16, 25, 4, leftArmLength);
        pixmap.fillRectangle(44, 25, 4, rightArmLength);
        
        // body
        pixmap.setColor(0.2f, 0.4f, 0.8f, 1);
        pixmap.fillRectangle(20, 22, 24, 20);
        
        // animated legs
        pixmap.setColor(0.4f, 0.2f, 0.1f, 1);
        int leftLegLength = leftLong ? 14 : 18;
        int rightLegLength = leftLong ? 18 : 14;
        pixmap.fillRectangle(22, 42, 6, leftLegLength);
        pixmap.fillRectangle(36, 42, 6, rightLegLength);
        
        // animated feet
        pixmap.setColor(0.1f, 0.1f, 0.1f, 1);
        int leftFootY = leftLong ? 56 : 60;
        int rightFootY = leftLong ? 60 : 56;
        pixmap.fillRectangle(20, leftFootY, 8, 6);
        pixmap.fillRectangle(36, rightFootY, 8, 6);
        
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

    private void updateCamera() {
        // Center camera on player (infinite world)
        float cameraX = x + 32; // player center
        float cameraY = y + 32; // player center
        
        camera.position.set(cameraX, cameraY, 0);
    }

    private boolean wouldCollide(float newX, float newY) {
        // Check collision with regular trees
        if (trees != null) {
            for (Tree tree : trees.values()) {
                if (tree.collidesWith(newX, newY, 64, 64)) {
                    return true;
                }
            }
        }
        
        // Check collision with apple trees
        if (appleTrees != null) {
            for (AppleTree appleTree : appleTrees.values()) {
                if (appleTree.collidesWith(newX, newY, 64, 64)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public void setPosition(float newX, float newY) {
        this.x = newX;
        this.y = newY;
    }

    private void attackNearbyTrees() {
        boolean attackedSomething = false;
        
        // Attack trees within range (128px)
        if (trees != null) {
            Tree targetTree = null;
            String targetKey = null;
            
            for (Map.Entry<String, Tree> entry : trees.entrySet()) {
                Tree tree = entry.getValue();
                float dx = tree.getX() - x;
                float dy = tree.getY() - y;
                float distance = (float)Math.sqrt(dx * dx + dy * dy);
                
                if (distance < 128) {
                    targetTree = tree;
                    targetKey = entry.getKey();
                    break;
                }
            }
            
            if (targetTree != null) {
                System.out.println("Attacking tree, health before: " + targetTree.getHealth());
                boolean destroyed = targetTree.attack();
                System.out.println("Tree health after attack: " + targetTree.getHealth() + ", destroyed: " + destroyed);
                attackedSomething = true;
                
                if (destroyed) {
                    targetTree.dispose();
                    trees.remove(targetKey);
                    clearedPositions.put(targetKey, true);
                    System.out.println("Tree removed from world");
                }
            }
        }
        
        // Attack apple trees within range (128px)
        if (appleTrees != null && !attackedSomething) {
            AppleTree targetAppleTree = null;
            String targetKey = null;
            
            for (Map.Entry<String, AppleTree> entry : appleTrees.entrySet()) {
                AppleTree appleTree = entry.getValue();
                float dx = appleTree.getX() - x;
                float dy = appleTree.getY() - y;
                float distance = (float)Math.sqrt(dx * dx + dy * dy);
                
                if (distance < 128) {
                    targetAppleTree = appleTree;
                    targetKey = entry.getKey();
                    break;
                }
            }
            
            if (targetAppleTree != null) {
                System.out.println("Attacking apple tree, health before: " + targetAppleTree.getHealth());
                boolean destroyed = targetAppleTree.attack();
                System.out.println("Apple tree health after attack: " + targetAppleTree.getHealth() + ", destroyed: " + destroyed);
                attackedSomething = true;
                
                if (destroyed) {
                    targetAppleTree.dispose();
                    appleTrees.remove(targetKey);
                    clearedPositions.put(targetKey, true);
                    System.out.println("Apple tree removed from world");
                }
            }
        }
        
        if (!attackedSomething) {
            System.out.println("No trees in range to attack");
        }
    }

    public void dispose() {
        if (texture != null) texture.dispose();
    }
}