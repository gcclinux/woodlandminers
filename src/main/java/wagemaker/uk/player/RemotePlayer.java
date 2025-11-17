package wagemaker.uk.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import wagemaker.uk.network.Direction;

/**
 * Represents a remote player in a multiplayer session.
 * Handles rendering and state updates for other players.
 */
public class RemotePlayer {
    private String playerId;
    private String playerName;
    private float x;
    private float y;
    private Direction currentDirection;
    private float health;
    private float hunger; // Hunger level (0-100%)
    private boolean isMoving;
    private float animTime;
    
    // Animation components
    private Texture spriteSheet;
    private Animation<TextureRegion> walkUpAnimation;
    private Animation<TextureRegion> walkLeftAnimation;
    private Animation<TextureRegion> walkDownAnimation;
    private Animation<TextureRegion> walkRightAnimation;
    private Animation<TextureRegion> currentAnimation;
    private TextureRegion idleUpFrame;
    private TextureRegion idleDownFrame;
    private TextureRegion idleLeftFrame;
    private TextureRegion idleRightFrame;
    
    // Position interpolation for smooth movement
    private float targetX;
    private float targetY;
    private static final float INTERPOLATION_SPEED = 500f; // Units per second (faster than player speed for responsiveness)
    
    public RemotePlayer(String playerId, String playerName, float x, float y, 
                       Direction direction, float health, boolean isMoving) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.currentDirection = direction != null ? direction : Direction.DOWN;
        this.health = health;
        this.hunger = 0; // Initialize hunger to 0
        this.isMoving = isMoving;
        this.animTime = 0;
        
        loadAnimations();
        updateCurrentAnimation();
    }
    
    /**
     * Load player sprite sheet and create animations.
     */
    private void loadAnimations() {
        // Load the same sprite sheet as the local player
        spriteSheet = new Texture("sprites/player/remote_start.png");
        
        // Create animation frames for each direction
        TextureRegion[] walkUpFrames = new TextureRegion[9];
        TextureRegion[] walkLeftFrames = new TextureRegion[9];
        TextureRegion[] walkDownFrames = new TextureRegion[9];
        TextureRegion[] walkRightFrames = new TextureRegion[9];
        
        // UP frames: 1st row (Y=512 in LibGDX coordinates)
        int upTopY = 512;
        int[] upXCoords = {0, 64, 128, 192, 256, 320, 384, 448, 512};
        for (int i = 0; i < 9; i++) {
            walkUpFrames[i] = new TextureRegion(spriteSheet, upXCoords[i], upTopY, 64, 64);
        }
        
        // LEFT frames: 2nd row (Y=576 in LibGDX coordinates)
        int leftTopY = 576;
        int[] leftXCoords = {0, 64, 128, 192, 256, 320, 384, 448, 512};
        for (int i = 0; i < 9; i++) {
            walkLeftFrames[i] = new TextureRegion(spriteSheet, leftXCoords[i], leftTopY, 64, 64);
        }
        
        // DOWN frames: 3rd row (Y=640 in LibGDX coordinates)
        int downTopY = 640;
        int[] downXCoords = {0, 64, 128, 192, 256, 320, 384, 448, 512};
        for (int i = 0; i < 9; i++) {
            walkDownFrames[i] = new TextureRegion(spriteSheet, downXCoords[i], downTopY, 64, 64);
        }
        
        // RIGHT frames: 4th row (Y=704 in LibGDX coordinates)
        int rightTopY = 704;
        int[] rightXCoords = {0, 64, 128, 192, 256, 320, 384, 448, 512};
        for (int i = 0; i < 9; i++) {
            walkRightFrames[i] = new TextureRegion(spriteSheet, rightXCoords[i], rightTopY, 64, 64);
        }
        
        // Create animations (0.1f = 100ms per frame)
        walkUpAnimation = new Animation<>(0.1f, walkUpFrames);
        walkLeftAnimation = new Animation<>(0.1f, walkLeftFrames);
        walkDownAnimation = new Animation<>(0.1f, walkDownFrames);
        walkRightAnimation = new Animation<>(0.1f, walkRightFrames);
        
        // Set all animations to loop
        walkUpAnimation.setPlayMode(Animation.PlayMode.LOOP);
        walkLeftAnimation.setPlayMode(Animation.PlayMode.LOOP);
        walkDownAnimation.setPlayMode(Animation.PlayMode.LOOP);
        walkRightAnimation.setPlayMode(Animation.PlayMode.LOOP);
        
        // Create directional idle frames (first frame of each animation)
        idleUpFrame = new TextureRegion(spriteSheet, 0, 512, 64, 64);    // First UP frame
        idleLeftFrame = new TextureRegion(spriteSheet, 0, 576, 64, 64);  // First LEFT frame
        idleDownFrame = new TextureRegion(spriteSheet, 0, 640, 64, 64);  // First DOWN frame
        idleRightFrame = new TextureRegion(spriteSheet, 0, 704, 64, 64); // First RIGHT frame
        
        // Set default animation
        currentAnimation = walkDownAnimation;
    }
    
    /**
     * Update the current animation based on direction.
     */
    private void updateCurrentAnimation() {
        switch (currentDirection) {
            case UP:
                currentAnimation = walkUpAnimation;
                break;
            case DOWN:
                currentAnimation = walkDownAnimation;
                break;
            case LEFT:
                currentAnimation = walkLeftAnimation;
                break;
            case RIGHT:
                currentAnimation = walkRightAnimation;
                break;
        }
    }
    
    /**
     * Update player position and movement state.
     */
    public void updatePosition(float x, float y, Direction direction, boolean moving) {
        this.targetX = x;
        this.targetY = y;
        this.isMoving = moving;
        
        if (direction != null && direction != this.currentDirection) {
            this.currentDirection = direction;
            updateCurrentAnimation();
        }
    }
    
    /**
     * Update player health.
     */
    public void updateHealth(float health) {
        this.health = Math.max(0, Math.min(100, health));
    }
    
    /**
     * Update player hunger.
     * @param hunger The new hunger level (0-100%)
     */
    public void updateHunger(float hunger) {
        this.hunger = Math.max(0, Math.min(100, hunger));
    }
    
    /**
     * Update animation and interpolate position.
     */
    public void update(float deltaTime) {
        // Interpolate position for smooth movement
        float dx = targetX - x;
        float dy = targetY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        // If distance is large (more than 50 pixels), snap immediately to avoid lag
        // This happens when updates are delayed or player moves quickly
        if (distance > 50f) {
            x = targetX;
            y = targetY;
        } else if (distance > 0.5f) {
            // For small distances, use fast interpolation for smooth movement
            float moveAmount = Math.min(INTERPOLATION_SPEED * deltaTime, distance);
            x += (dx / distance) * moveAmount;
            y += (dy / distance) * moveAmount;
        } else {
            x = targetX;
            y = targetY;
        }
        
        // Update animation time
        if (isMoving) {
            animTime += deltaTime;
        } else {
            animTime = 0;
        }
    }
    
    /**
     * Get the current animation frame.
     */
    private TextureRegion getCurrentFrame() {
        if (isMoving) {
            return currentAnimation.getKeyFrame(animTime);
        } else {
            // Return directional idle frame based on last movement direction
            switch (currentDirection) {
                case UP:
                    return idleUpFrame;
                case DOWN:
                    return idleDownFrame;
                case LEFT:
                    return idleLeftFrame;
                case RIGHT:
                    return idleRightFrame;
                default:
                    return idleDownFrame; // Fallback
            }
        }
    }
    
    // Getters
    public String getPlayerId() {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    
    public Direction getCurrentDirection() {
        return currentDirection;
    }
    
    public float getHealth() {
        return health;
    }
    
    public float getHunger() {
        return hunger;
    }
    
    public boolean isMoving() {
        return isMoving;
    }
    
    /**
     * Render the remote player sprite.
     */
    public void render(SpriteBatch batch) {
        TextureRegion currentFrame = getCurrentFrame();
        batch.draw(currentFrame, x, y, 100, 100);
    }
    
    /**
     * Render unified health bar above the player showing both health damage and hunger.
     * Uses the same three-layer rendering approach as the local player's health bar:
     * - Green base layer representing full health and no hunger
     * - Red overlay from the right side showing damage taken (decreasing health)
     * - Blue overlay from the right side showing hunger accumulated (decreasing satisfaction)
     * 
     * Both red and blue overlays decrease from the right to show depletion/loss.
     * The bar is shown when health < 100 OR hunger > 0.
     */
    public void renderHealthBar(ShapeRenderer shapeRenderer) {
        // Show bar when health < 100 OR hunger > 0
        if (health < 100 || hunger > 0) {
            float healthBarWidth = 100;
            float healthBarHeight = 6;
            float healthBarX = x;
            float healthBarY = y + 110; // Above player sprite (100px sprite + 10px gap)
            
            // Calculate percentages
            float healthPercent = Math.max(0, Math.min(100, health)) / 100.0f;
            float hungerPercent = Math.max(0, Math.min(100, hunger)) / 100.0f;
            
            // Begin filled shape rendering
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            
            // Layer 1: Draw green base (full bar)
            shapeRenderer.setColor(0, 1, 0, 1);  // Green
            shapeRenderer.rect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);
            
            // Layer 2: Draw red damage overlay (from right side - decreasing health)
            float damagePercent = 1.0f - healthPercent;
            if (damagePercent > 0) {
                shapeRenderer.setColor(1, 0, 0, 1);  // Red
                float damageWidth = healthBarWidth * damagePercent;
                shapeRenderer.rect(healthBarX + healthBarWidth - damageWidth, healthBarY, 
                                  damageWidth, healthBarHeight);
            }
            
            // Layer 3: Draw blue hunger overlay (from right side - decreasing satisfaction)
            // Hunger represents "not full", so it also decreases from the right
            if (hungerPercent > 0) {
                shapeRenderer.setColor(0, 0, 1, 1);  // Blue
                float hungerWidth = healthBarWidth * hungerPercent;
                shapeRenderer.rect(healthBarX + healthBarWidth - hungerWidth, healthBarY, 
                                  hungerWidth, healthBarHeight);
            }
            
            shapeRenderer.end();
            
            // Layer 4: Draw black border
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0, 0, 0, 1);  // Black
            shapeRenderer.rect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);
            shapeRenderer.end();
        }
    }
    
    /**
     * Render player name tag above the player.
     */
    public void renderNameTag(SpriteBatch batch, BitmapFont font) {
        if (playerName != null && !playerName.isEmpty()) {
            // Calculate text position (centered above player)
            float nameTagY = y + 115; // Above health bar (100px sprite + 2px gap + 6px health bar + 7px gap)
            
            // Get text width for centering
            com.badlogic.gdx.graphics.g2d.GlyphLayout layout = 
                new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, playerName);
            float textWidth = layout.width;
            float nameTagX = x + (100 - textWidth) / 2; // Center on player sprite
            
            // Draw text with shadow for better visibility
            font.setColor(Color.BLACK);
            font.draw(batch, playerName, nameTagX + 1, nameTagY - 1);
            
            font.setColor(Color.WHITE);
            font.draw(batch, playerName, nameTagX, nameTagY);
        }
    }
    
    /**
     * Clean up resources.
     */
    public void dispose() {
        if (spriteSheet != null) {
            spriteSheet.dispose();
        }
    }
}
