package wagemaker.uk;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.Map;

public class Player {
    private float x, y;
    private float speed = 200;
    private float animTime = 0;
    private Texture spriteSheet;
    private Animation<TextureRegion> walkUpAnimation;
    private Animation<TextureRegion> walkLeftAnimation;
    private Animation<TextureRegion> walkDownAnimation;
    private Animation<TextureRegion> walkRightAnimation;
    private Animation<TextureRegion> currentAnimation;
    private TextureRegion idleFrame;
    private OrthographicCamera camera;
    private Map<String, Tree> trees;
    private Map<String, AppleTree> appleTrees;
    private Map<String, Boolean> clearedPositions;
    
    // Direction tracking
    private enum Direction { UP, DOWN, LEFT, RIGHT }
    private Direction currentDirection = Direction.DOWN;
    private boolean isMoving = false;

    public Player(float startX, float startY, OrthographicCamera camera) {
        this.x = startX;
        this.y = startY;
        this.camera = camera;
        loadAnimations();
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
        isMoving = false;
        
        // handle input with collision detection
        float newX = x;
        float newY = y;
        
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) { 
            newX = x - speed * deltaTime;
            if (!wouldCollide(newX, y)) {
                x = newX;
                isMoving = true;
                currentDirection = Direction.LEFT;
                currentAnimation = walkLeftAnimation;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) { 
            newX = x + speed * deltaTime;
            if (!wouldCollide(newX, y)) {
                x = newX;
                isMoving = true;
                currentDirection = Direction.RIGHT;
                currentAnimation = walkRightAnimation;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) { 
            newY = y + speed * deltaTime;
            if (!wouldCollide(x, newY)) {
                y = newY;
                isMoving = true;
                currentDirection = Direction.UP;
                currentAnimation = walkUpAnimation;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) { 
            newY = y - speed * deltaTime;
            if (!wouldCollide(x, newY)) {
                y = newY;
                isMoving = true;
                currentDirection = Direction.DOWN;
                currentAnimation = walkDownAnimation;
            }
        }

        // handle attack
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            System.out.println("Attack pressed at player position: " + x + ", " + y);
            attackNearbyTrees();
        }

        // update animation time
        if (isMoving) {
            animTime += deltaTime;
        } else {
            // Reset to first frame when not moving (idle pose)
            animTime = 0;
        }
        
        // update camera to follow player
        updateCamera();
    }

    private void loadAnimations() {
        // Load the sprite sheet
        spriteSheet = new Texture("sprites/player/man_start.png");
        
        // Get sprite sheet dimensions
        int spriteSheetHeight = spriteSheet.getHeight();
        
        // Create animation frames for each direction
        TextureRegion[] walkUpFrames = new TextureRegion[9];
        TextureRegion[] walkLeftFrames = new TextureRegion[9];
        TextureRegion[] walkDownFrames = new TextureRegion[9];
        TextureRegion[] walkRightFrames = new TextureRegion[9];
        
        // Your coordinates are bottom-left, LibGDX needs top-left
        // Convert bottom-left Y to top-left Y: topY = spriteSheetHeight - bottomY - height
        
        // Fixed mapping based on your feedback:
        // RIGHT uses UP row, LEFT uses DOWN row, DOWN uses LEFT row, UP uses RIGHT row
        // So the actual mapping is:
        // Row 1 (Y=512-576): Walking RIGHT (not UP)
        // Row 2 (Y=576-640): Walking UP (not LEFT)  
        // Row 3 (Y=640-704): Walking LEFT (not DOWN)
        // Row 4 (Y=704-768): Walking DOWN (not RIGHT)
        
        // UP frames: 1st row (Y=512 in LibGDX coordinates)
        // Using coordinates: 0,512 | 64,512 | 128,512 | 192,512 | 256,512 | 320,512 | 384,512 | 448,512 | 512,512
        int upTopY = 512;
        int[] upXCoords = {0, 64, 128, 192, 256, 320, 384, 448, 512};
        for (int i = 0; i < 9; i++) {
            walkUpFrames[i] = new TextureRegion(spriteSheet, upXCoords[i], upTopY, 64, 64);
        }
        
        // LEFT frames: 2nd row (Y=576 in LibGDX coordinates)
        // Using coordinates: 0,576 | 64,576 | 128,576 | 192,576 | 256,576 | 320,576 | 384,576 | 448,576 | 512,576
        int leftTopY = 576;
        int[] leftXCoords = {0, 64, 128, 192, 256, 320, 384, 448, 512};
        for (int i = 0; i < 9; i++) {
            walkLeftFrames[i] = new TextureRegion(spriteSheet, leftXCoords[i], leftTopY, 64, 64);
        }
        
        // DOWN frames: 3rd row (Y=640 in LibGDX coordinates)
        // Using coordinates: 0,640 | 64,640 | 128,640 | 192,640 | 256,640 | 320,640 | 384,640 | 448,640 | 512,640
        int downTopY = 640;
        int[] downXCoords = {0, 64, 128, 192, 256, 320, 384, 448, 512};
        for (int i = 0; i < 9; i++) {
            walkDownFrames[i] = new TextureRegion(spriteSheet, downXCoords[i], downTopY, 64, 64);
        }
        
        // RIGHT frames: 4th row (Y=704 in LibGDX coordinates)
        // Using coordinates: 0,704 | 64,704 | 128,704 | 192,704 | 256,704 | 320,704 | 384,704 | 448,704 | 512,704
        int rightTopY = 704;
        int[] rightXCoords = {0, 64, 128, 192, 256, 320, 384, 448, 512};
        for (int i = 0; i < 9; i++) {
            walkRightFrames[i] = new TextureRegion(spriteSheet, rightXCoords[i], rightTopY, 64, 64);
        }
        
        // Create animations (0.1f = 100ms per frame, gives smooth 10 FPS animation)
        walkUpAnimation = new Animation<>(0.1f, walkUpFrames);
        walkLeftAnimation = new Animation<>(0.1f, walkLeftFrames);
        walkDownAnimation = new Animation<>(0.1f, walkDownFrames);
        walkRightAnimation = new Animation<>(0.1f, walkRightFrames);
        
        // Set all animations to loop
        walkUpAnimation.setPlayMode(Animation.PlayMode.LOOP);
        walkLeftAnimation.setPlayMode(Animation.PlayMode.LOOP);
        walkDownAnimation.setPlayMode(Animation.PlayMode.LOOP);
        walkRightAnimation.setPlayMode(Animation.PlayMode.LOOP);
        
        // Create idle frame: 1st image on 3rd row of red rectangle
        // 0px from left, 640px from top (LibGDX coordinates)
        // This corresponds to (0, 704) in bottom-left coordinates
        idleFrame = new TextureRegion(spriteSheet, 0, 640, 64, 64);
        
        // Set default animation to LEFT (Row 3 - character facing camera/standing still)
        currentAnimation = walkLeftAnimation;
    }

    public TextureRegion getCurrentFrame() {
        if (isMoving) {
            return currentAnimation.getKeyFrame(animTime);
        } else {
            // Return idle frame when not moving
            return idleFrame;
        }
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
        
        // Attack trees within range (individual collision)
        if (trees != null) {
            Tree targetTree = null;
            String targetKey = null;
            
            for (Map.Entry<String, Tree> entry : trees.entrySet()) {
                Tree tree = entry.getValue();
                
                if (tree.isInAttackRange(x, y)) {
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
        
        // Attack apple trees within range (individual collision)
        if (appleTrees != null && !attackedSomething) {
            AppleTree targetAppleTree = null;
            String targetKey = null;
            
            for (Map.Entry<String, AppleTree> entry : appleTrees.entrySet()) {
                AppleTree appleTree = entry.getValue();
                
                if (appleTree.isInAttackRange(x, y)) {
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
        if (spriteSheet != null) spriteSheet.dispose();
    }
}