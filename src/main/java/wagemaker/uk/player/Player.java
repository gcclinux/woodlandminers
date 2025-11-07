package wagemaker.uk.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import wagemaker.uk.items.Apple;
import wagemaker.uk.items.Banana;
import wagemaker.uk.trees.SmallTree;
import wagemaker.uk.trees.AppleTree;
import wagemaker.uk.trees.BambooTree;
import wagemaker.uk.trees.BananaTree;
import wagemaker.uk.trees.CoconutTree;
import wagemaker.uk.trees.Cactus;
import wagemaker.uk.ui.GameMenu;
import java.util.Map;

public class Player {
    private float x, y;
    private float speed = 200;
    private float animTime = 0;
    private float health = 100; // Player health
    private float lastCactusDamageTime = 0; // To prevent spam damage
    private Texture spriteSheet;
    private Animation<TextureRegion> walkUpAnimation;
    private Animation<TextureRegion> walkLeftAnimation;
    private Animation<TextureRegion> walkDownAnimation;
    private Animation<TextureRegion> walkRightAnimation;
    private Animation<TextureRegion> currentAnimation;
    private TextureRegion idleFrame;
    private OrthographicCamera camera;
    private Map<String, SmallTree> trees;
    private Map<String, AppleTree> appleTrees;
    private Map<String, CoconutTree> coconutTrees;
    private Map<String, BambooTree> bambooTrees;
    private Map<String, BananaTree> bananaTrees;
    private Map<String, Apple> apples;
    private Map<String, Banana> bananas;
    private Cactus cactus; // Single cactus reference
    private Object gameInstance; // Reference to MyGdxGame for cactus respawning
    private Map<String, Boolean> clearedPositions;
    private GameMenu gameMenu;
    
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
    
    public void setTrees(Map<String, SmallTree> trees) {
        this.trees = trees;
    }
    
    public void setAppleTrees(Map<String, AppleTree> appleTrees) {
        this.appleTrees = appleTrees;
    }
    
    public void setCoconutTrees(Map<String, CoconutTree> coconutTrees) {
        this.coconutTrees = coconutTrees;
    }
    
    public void setBambooTrees(Map<String, BambooTree> bambooTrees) {
        this.bambooTrees = bambooTrees;
    }
    
    public void setBananaTrees(Map<String, BananaTree> bananaTrees) {
        this.bananaTrees = bananaTrees;
    }
    
    public void setApples(Map<String, Apple> apples) {
        this.apples = apples;
    }
    
    public void setBananas(Map<String, Banana> bananas) {
        this.bananas = bananas;
    }
    
    public void setCactus(Cactus cactus) {
        this.cactus = cactus;
    }
    
    public void setGameInstance(Object gameInstance) {
        this.gameInstance = gameInstance;
    }
    
    public void setClearedPositions(Map<String, Boolean> clearedPositions) {
        this.clearedPositions = clearedPositions;
    }
    
    public void setGameMenu(GameMenu gameMenu) {
        this.gameMenu = gameMenu;
    }

    public void update(float deltaTime) {
        isMoving = false;
        
        // Track movement in both directions
        boolean movingLeft = false;
        boolean movingRight = false;
        boolean movingUp = false;
        boolean movingDown = false;
        
        // handle input with collision detection
        float newX = x;
        float newY = y;
        
        // Check horizontal movement
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) { 
            float testX = x - speed * deltaTime;
            if (!wouldCollide(testX, y)) {
                newX = testX;
                movingLeft = true;
                isMoving = true;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) { 
            float testX = x + speed * deltaTime;
            if (!wouldCollide(testX, y)) {
                newX = testX;
                movingRight = true;
                isMoving = true;
            }
        }
        
        // Check vertical movement
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) { 
            float testY = y + speed * deltaTime;
            if (!wouldCollide(newX, testY)) { // Use newX in case of diagonal movement
                newY = testY;
                movingUp = true;
                isMoving = true;
            }
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) { 
            float testY = y - speed * deltaTime;
            if (!wouldCollide(newX, testY)) { // Use newX in case of diagonal movement
                newY = testY;
                movingDown = true;
                isMoving = true;
            }
        }
        
        // Apply movement
        x = newX;
        y = newY;
        
        // Determine animation based on movement priority:
        // For diagonal movement, prioritize horizontal direction (LEFT/RIGHT)
        // Only use vertical animations (UP/DOWN) when moving purely vertically
        if (movingLeft) {
            currentDirection = Direction.LEFT;
            currentAnimation = walkLeftAnimation;
        } else if (movingRight) {
            currentDirection = Direction.RIGHT;
            currentAnimation = walkRightAnimation;
        } else if (movingUp) {
            currentDirection = Direction.UP;
            currentAnimation = walkUpAnimation;
        } else if (movingDown) {
            currentDirection = Direction.DOWN;
            currentAnimation = walkDownAnimation;
        }

        // handle attack
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            attackNearbyTrees();
        }

        // update animation time
        if (isMoving) {
            animTime += deltaTime;
        } else {
            // Reset to first frame when not moving (idle pose)
            animTime = 0;
        }
        
        // Check for cactus damage
        checkCactusDamage(deltaTime);
        
        // Check for apple pickups
        checkApplePickups();
        
        // Check for banana pickups
        checkBananaPickups();
        
        // update camera to follow player
        updateCamera();
    }

    private void loadAnimations() {
        // Load the sprite sheet
        spriteSheet = new Texture("sprites/player/man_start.png");
        
        // Get sprite sheet dimensions
        // int spriteSheetHeight = spriteSheet.getHeight();
        
        // Create animation frames for each direction
        TextureRegion[] walkUpFrames = new TextureRegion[9];
        TextureRegion[] walkLeftFrames = new TextureRegion[9];
        TextureRegion[] walkDownFrames = new TextureRegion[9];
        TextureRegion[] walkRightFrames = new TextureRegion[9];
        
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
            for (SmallTree tree : trees.values()) {
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
        
        // Check collision with coconut trees
        if (coconutTrees != null) {
            for (CoconutTree coconutTree : coconutTrees.values()) {
                if (coconutTree.collidesWith(newX, newY, 64, 64)) {
                    return true;
                }
            }
        }
        
        // Check collision with bamboo trees
        if (bambooTrees != null) {
            for (BambooTree bambooTree : bambooTrees.values()) {
                if (bambooTree.collidesWith(newX, newY, 64, 64)) {
                    return true;
                }
            }
        }
        
        // Check collision with banana trees
        if (bananaTrees != null) {
            for (BananaTree bananaTree : bananaTrees.values()) {
                if (bananaTree.collidesWith(newX, newY, 64, 64)) {
                    return true;
                }
            }
        }
        
        // Check collision with cactus
        if (cactus != null) {
            if (cactus.collidesWith(newX, newY, 64, 64)) {
                return true;
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
            SmallTree targetTree = null;
            String targetKey = null;
            
            for (Map.Entry<String, SmallTree> entry : trees.entrySet()) {
                SmallTree tree = entry.getValue();
                
                if (tree.isInAttackRange(x, y)) {
                    targetTree = tree;
                    targetKey = entry.getKey();
                    break;
                }
            }
            
            if (targetTree != null) {
                float healthBefore = targetTree.getHealth();
                boolean destroyed = targetTree.attack();
                if (destroyed) {
                    System.out.println("Attacking tree, health before: " + healthBefore);
                    System.out.println("Tree health after attack: " + targetTree.getHealth() + ", destroyed: " + destroyed);
                }
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
                float healthBefore = targetAppleTree.getHealth();
                boolean destroyed = targetAppleTree.attack();
                if (destroyed) {
                    System.out.println("Attacking apple tree, health before: " + healthBefore);
                    System.out.println("Apple tree health after attack: " + targetAppleTree.getHealth() + ", destroyed: " + destroyed);
                }
                attackedSomething = true;
                
                if (destroyed) {
                    // Spawn apple at tree position
                    apples.put(targetKey, new Apple(targetAppleTree.getX(), targetAppleTree.getY()));
                    System.out.println("Apple dropped at: " + targetAppleTree.getX() + ", " + targetAppleTree.getY());
                    targetAppleTree.dispose();
                    appleTrees.remove(targetKey);
                    clearedPositions.put(targetKey, true);
                }
            }
        }
        
        // Attack coconut trees within range (individual collision)
        if (coconutTrees != null && !attackedSomething) {
            CoconutTree targetCoconutTree = null;
            String targetKey = null;
            
            for (Map.Entry<String, CoconutTree> entry : coconutTrees.entrySet()) {
                CoconutTree coconutTree = entry.getValue();
                
                if (coconutTree.isInAttackRange(x, y)) {
                    targetCoconutTree = coconutTree;
                    targetKey = entry.getKey();
                    break;
                }
            }
            
            if (targetCoconutTree != null) {
                float healthBefore = targetCoconutTree.getHealth();
                boolean destroyed = targetCoconutTree.attack();
                if (destroyed) {
                    System.out.println("Attacking coconut tree, health before: " + healthBefore);
                    System.out.println("Coconut tree health after attack: " + targetCoconutTree.getHealth() + ", destroyed: " + destroyed);
                }
                attackedSomething = true;
                
                if (destroyed) {
                    targetCoconutTree.dispose();
                    coconutTrees.remove(targetKey);
                    clearedPositions.put(targetKey, true);
                    System.out.println("Coconut tree removed from world");
                }
            }
        }
        
        // Attack bamboo trees within range (individual collision)
        if (bambooTrees != null && !attackedSomething) {
            BambooTree targetBambooTree = null;
            String targetKey = null;
            
            for (Map.Entry<String, BambooTree> entry : bambooTrees.entrySet()) {
                BambooTree bambooTree = entry.getValue();
                
                if (bambooTree.isInAttackRange(x, y)) {
                    targetBambooTree = bambooTree;
                    targetKey = entry.getKey();
                    break;
                }
            }
            
            if (targetBambooTree != null) {
                float healthBefore = targetBambooTree.getHealth();
                boolean destroyed = targetBambooTree.attack();
                if (destroyed) {
                    System.out.println("Attacking bamboo tree, health before: " + healthBefore);
                    System.out.println("Bamboo tree health after attack: " + targetBambooTree.getHealth() + ", destroyed: " + destroyed);
                }
                attackedSomething = true;
                
                if (destroyed) {
                    targetBambooTree.dispose();
                    bambooTrees.remove(targetKey);
                    clearedPositions.put(targetKey, true);
                    System.out.println("Bamboo tree removed from world");
                }
            }
        }
        
        // Attack banana trees within range (individual collision)
        if (bananaTrees != null && !attackedSomething) {
            BananaTree targetBananaTree = null;
            String targetKey = null;
            
            for (Map.Entry<String, BananaTree> entry : bananaTrees.entrySet()) {
                BananaTree bananaTree = entry.getValue();
                
                if (bananaTree.isInAttackRange(x, y)) {
                    targetBananaTree = bananaTree;
                    targetKey = entry.getKey();
                    break;
                }
            }
            
            if (targetBananaTree != null) {
                float healthBefore = targetBananaTree.getHealth();
                boolean destroyed = targetBananaTree.attack();
                if (destroyed) {
                    System.out.println("Attacking banana tree, health before: " + healthBefore);
                    System.out.println("Banana tree health after attack: " + targetBananaTree.getHealth() + ", destroyed: " + destroyed);
                }
                attackedSomething = true;
                
                if (destroyed) {
                    // Spawn banana at tree position
                    bananas.put(targetKey, new Banana(targetBananaTree.getX(), targetBananaTree.getY()));
                    System.out.println("Banana dropped at: " + targetBananaTree.getX() + ", " + targetBananaTree.getY());
                    targetBananaTree.dispose();
                    bananaTrees.remove(targetKey);
                    clearedPositions.put(targetKey, true);
                }
            }
        }
        
        // Attack cactus within range
        if (cactus != null && !attackedSomething) {
            if (cactus.isInAttackRange(x, y)) {
                float healthBefore = cactus.getHealth();
                boolean destroyed = cactus.attack();
                
                if (destroyed) {
                    System.out.println("Attacking cactus, health before: " + healthBefore);
                    System.out.println("Cactus health after attack: " + cactus.getHealth() + ", destroyed: " + destroyed);
                    System.out.println("Cactus destroyed! Spawning new cactus...");
                    
                    // Spawn new cactus at random location
                    if (gameInstance != null) {
                        try {
                            // Use reflection to call spawnNewCactus method
                            gameInstance.getClass().getMethod("spawnNewCactus").invoke(gameInstance);
                        } catch (Exception e) {
                            System.out.println("Error spawning new cactus: " + e.getMessage());
                        }
                    }
                }
                attackedSomething = true;
            }
        }
        
        if (!attackedSomething) {
            System.out.println("No trees in range to attack");
        }
    }

    private void checkCactusDamage(float deltaTime) {
        if (cactus != null) {
            // Use the same logic as attack range for consistency
            float cactusCenterX = cactus.getX() + 32;
            float cactusCenterY = cactus.getY() + 64;
            float playerCenterX = x + 32;
            float playerCenterY = y + 32;
            
            float dx = Math.abs(cactusCenterX - playerCenterX);
            float dy = Math.abs(cactusCenterY - playerCenterY);
            
            // Use same range as attack range: 64px left/right, 96px up/down
            boolean inDamageRange = dx <= 64 && dy <= 96;
            
            if (inDamageRange) {

                lastCactusDamageTime += deltaTime;
                
                // Take damage every 0.5 seconds while in range
                if (lastCactusDamageTime >= 0.5f) {
                    health -= 10; // 10 damage per half second
                    lastCactusDamageTime = 0;
                    
                    System.out.println("Player taking cactus damage! Health: " + health);
                    
                    // Check if player died
                    if (health <= 0) {
                        respawnPlayer();
                    }
                }
            } else {
                // Reset damage timer when not in range
                lastCactusDamageTime = 0;
            }
        }
    }
    
    private void respawnPlayer() {
        System.out.println("Player died! Respawning...");
        
        // Reset health
        health = 100;
        
        // Generate random respawn position far from cactus
        float newX, newY;
        if (cactus != null) {
            do {
                // Random position in a large area
                newX = (float)(Math.random() - 0.5) * 2000; // ±1000px
                newY = (float)(Math.random() - 0.5) * 2000; // ±1000px
                
                float distance = (float)Math.sqrt((newX - cactus.getX()) * (newX - cactus.getX()) + 
                                                 (newY - cactus.getY()) * (newY - cactus.getY()));
                
                // Ensure respawn is at least 500px away from cactus
                if (distance >= 500) {
                    break;
                }
            } while (true);
        } else {
            // If no cactus, respawn at origin
            newX = 0;
            newY = 0;
        }
        
        // Set new position
        x = newX;
        y = newY;
        
        System.out.println("Player respawned!");
    }
    
    public float getHealth() {
        return health;
    }
    
    public void setHealth(float health) {
        this.health = Math.max(0, Math.min(100, health)); // Clamp between 0 and 100
    }
    
    public float getHealthPercentage() {
        return health / 100.0f;
    }
    
    private void checkApplePickups() {
        if (apples != null) {
            // Check all apples for pickup
            for (Map.Entry<String, Apple> entry : apples.entrySet()) {
                Apple apple = entry.getValue();
                String appleKey = entry.getKey();
                
                // Check if player is close enough to pick up apple (32px range)
                float dx = Math.abs((x + 32) - (apple.getX() + 12)); // Player center to apple center
                float dy = Math.abs((y + 32) - (apple.getY() + 12)); // Apple is 24x24, so center is +12
                
                if (dx <= 32 && dy <= 32) {
                    // Pick up the apple
                    pickupApple(appleKey);
                    break; // Only pick up one apple per frame
                }
            }
        }
    }
    
    private void pickupApple(String appleKey) {
        // Restore 20% health (20 HP)
        float healthBefore = health;
        health = Math.min(100, health + 20); // Cap at 100 HP
        float healthRestored = health - healthBefore;
        
        System.out.println("Apple picked up! Health restored: " + healthRestored + " (Health: " + healthBefore + " → " + health + ")");
        
        // Remove apple from game
        if (apples.containsKey(appleKey)) {
            Apple apple = apples.get(appleKey);
            apple.dispose();
            apples.remove(appleKey);
            System.out.println("Apple removed from game");
        }
    }
    
    private void checkBananaPickups() {
        if (bananas != null) {
            // Check all bananas for pickup
            for (Map.Entry<String, Banana> entry : bananas.entrySet()) {
                Banana banana = entry.getValue();
                String bananaKey = entry.getKey();
                
                // Check if player is close enough to pick up banana (32px range)
                float dx = Math.abs((x + 32) - (banana.getX() + 16)); // Player center to banana center
                float dy = Math.abs((y + 32) - (banana.getY() + 16)); // Banana is 32x32, so center is +16
                
                if (dx <= 32 && dy <= 32) {
                    // Pick up the banana
                    pickupBanana(bananaKey);
                    break; // Only pick up one banana per frame
                }
            }
        }
    }
    
    private void pickupBanana(String bananaKey) {
        // Restore 20% health (20 HP)
        float healthBefore = health;
        health = Math.min(100, health + 20); // Cap at 100 HP
        float healthRestored = health - healthBefore;
        
        System.out.println("Banana picked up! Health restored: " + healthRestored + " (Health: " + healthBefore + " → " + health + ")");
        
        // Remove banana from game
        if (bananas.containsKey(bananaKey)) {
            Banana banana = bananas.get(bananaKey);
            banana.dispose();
            bananas.remove(bananaKey);
            System.out.println("Banana removed from game");
        }
    }

    public boolean shouldShowHealthBar() {
        return true; // Always show for testing
        // return health < 100; // Original logic
    }

    public void dispose() {
        if (spriteSheet != null) spriteSheet.dispose();
    }
}