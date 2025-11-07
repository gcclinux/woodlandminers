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
    private boolean isMoving;
    private float animTime;
    
    // Animation components
    private Texture spriteSheet;
    private Animation<TextureRegion> walkUpAnimation;
    private Animation<TextureRegion> walkLeftAnimation;
    private Animation<TextureRegion> walkDownAnimation;
    private Animation<TextureRegion> walkRightAnimation;
    private Animation<TextureRegion> currentAnimation;
    private TextureRegion idleFrame;
    
    // Position interpolation for smooth movement
    private float targetX;
    private float targetY;
    private static final float INTERPOLATION_SPEED = 10f; // Units per second
    
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
        spriteSheet = new Texture("sprites/player/man_start.png");
        
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
        
        // Create idle frame (same as local player)
        idleFrame = new TextureRegion(spriteSheet, 0, 640, 64, 64);
        
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
     * Update animation and interpolate position.
     */
    public void update(float deltaTime) {
        // Interpolate position for smooth movement
        float dx = targetX - x;
        float dy = targetY - y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distance > 0.5f) {
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
            return idleFrame;
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
    
    public boolean isMoving() {
        return isMoving;
    }
    
    /**
     * Render the remote player sprite.
     */
    public void render(SpriteBatch batch) {
        TextureRegion currentFrame = getCurrentFrame();
        batch.draw(currentFrame, x, y, 64, 64);
    }
    
    /**
     * Render health bar above the player when health is below 100.
     */
    public void renderHealthBar(ShapeRenderer shapeRenderer) {
        if (health < 100) {
            float healthBarWidth = 64;
            float healthBarHeight = 6;
            float healthBarX = x;
            float healthBarY = y + 70; // Above player sprite
            
            // Draw background (red)
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);
            
            // Draw health (green)
            float healthPercentage = health / 100.0f;
            shapeRenderer.setColor(Color.GREEN);
            shapeRenderer.rect(healthBarX, healthBarY, healthBarWidth * healthPercentage, healthBarHeight);
        }
    }
    
    /**
     * Render player name tag above the player.
     */
    public void renderNameTag(SpriteBatch batch, BitmapFont font) {
        if (playerName != null && !playerName.isEmpty()) {
            // Calculate text position (centered above player)
            float nameTagY = y + 85; // Above health bar
            
            // Get text width for centering
            com.badlogic.gdx.graphics.g2d.GlyphLayout layout = 
                new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, playerName);
            float textWidth = layout.width;
            float nameTagX = x + (64 - textWidth) / 2; // Center on player sprite
            
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
