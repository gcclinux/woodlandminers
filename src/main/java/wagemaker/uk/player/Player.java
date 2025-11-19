package wagemaker.uk.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import wagemaker.uk.items.Apple;
import wagemaker.uk.items.BabyBamboo;
import wagemaker.uk.items.BabyTree;
import wagemaker.uk.items.Banana;
import wagemaker.uk.items.BambooStack;
import wagemaker.uk.items.Pebble;
import wagemaker.uk.items.WoodStack;
import wagemaker.uk.objects.Stone;
import wagemaker.uk.trees.SmallTree;
import wagemaker.uk.trees.AppleTree;
import wagemaker.uk.trees.BambooTree;
import wagemaker.uk.trees.BananaTree;
import wagemaker.uk.trees.CoconutTree;
import wagemaker.uk.trees.Cactus;
import wagemaker.uk.ui.GameMenu;
import wagemaker.uk.network.GameClient;
import wagemaker.uk.inventory.InventoryManager;
import wagemaker.uk.planting.PlantingSystem;
import wagemaker.uk.planting.PlantedBamboo;
import wagemaker.uk.planting.PlantedTree;
import wagemaker.uk.biome.BiomeManager;
import wagemaker.uk.targeting.TargetingSystem;
import wagemaker.uk.targeting.TargetIndicatorRenderer;
import wagemaker.uk.targeting.TargetingMode;
import wagemaker.uk.targeting.TargetingCallback;
import wagemaker.uk.targeting.PlantingTargetValidator;
import java.util.Map;
import java.util.Random;

public class Player {
    private float x, y;
    private float speed = 200;
    private float animTime = 0;
    private float health = 100; // Player health
    private float lastCactusDamageTime = 0; // To prevent spam damage
    private float previousHealth = 100; // Track previous health for change detection
    
    // Hunger system fields
    private float hunger = 0; // Hunger level (0-100%)
    private float hungerTimer = 0; // Timer accumulator for hunger increase
    private float previousHunger = 0; // Track previous hunger for change detection
    private static final float HUNGER_INTERVAL = 60.0f; // 60 seconds per 1% hunger increase
    
    // Multiplayer fields
    private String playerId; // Unique identifier for multiplayer
    private GameClient gameClient; // Reference for sending network updates
    private boolean isLocalPlayer = true; // Distinguish local vs remote players
    private Map<String, RemotePlayer> remotePlayers; // Reference to remote players for PvP
    private float lastPlayerAttackTime = 0; // Client-side attack cooldown tracking
    private static final float PLAYER_ATTACK_COOLDOWN = 0.5f; // 0.5 seconds between player attacks
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
    private OrthographicCamera camera;
    private Map<String, SmallTree> trees;
    private Map<String, AppleTree> appleTrees;
    private Map<String, CoconutTree> coconutTrees;
    private Map<String, BambooTree> bambooTrees;
    private Map<String, BananaTree> bananaTrees;
    private Map<String, Apple> apples;
    private Map<String, Banana> bananas;
    private Map<String, wagemaker.uk.items.BambooStack> bambooStacks;
    private Map<String, wagemaker.uk.items.BabyBamboo> babyBamboos;
    private Map<String, BabyTree> babyTrees;
    private Map<String, WoodStack> woodStacks;
    private Map<String, Pebble> pebbles;
    private Random random = new Random();
    private Map<String, Stone> stones;
    private Cactus cactus; // Single cactus reference
    private Object gameInstance; // Reference to MyGdxGame for cactus respawning
    private Map<String, Boolean> clearedPositions;
    private GameMenu gameMenu;
    private InventoryManager inventoryManager;
    
    // Planting system fields
    private PlantingSystem plantingSystem;
    private BiomeManager biomeManager;
    private Map<String, PlantedBamboo> plantedBamboos;
    private Map<String, PlantedTree> plantedTrees;
    
    // Targeting system fields
    private TargetingSystem targetingSystem;
    private TargetIndicatorRenderer targetIndicatorRenderer;
    
    // Direction tracking
    private enum Direction { UP, DOWN, LEFT, RIGHT }
    private Direction currentDirection = Direction.DOWN;
    private boolean isMoving = false;
    
    // Inventory navigation mode
    private boolean inventoryNavigationMode = false;

    public Player(float startX, float startY, OrthographicCamera camera) {
        this.x = startX;
        this.y = startY;
        this.camera = camera;
        loadAnimations();
        
        // Initialize targeting system
        this.targetingSystem = new TargetingSystem();
        this.targetIndicatorRenderer = new TargetIndicatorRenderer();
        this.targetIndicatorRenderer.initialize();
    }
    
    public void setTrees(Map<String, SmallTree> trees) {
        this.trees = trees;
        updateTargetingValidator();
    }
    
    public void setAppleTrees(Map<String, AppleTree> appleTrees) {
        this.appleTrees = appleTrees;
        updateTargetingValidator();
    }
    
    public void setCoconutTrees(Map<String, CoconutTree> coconutTrees) {
        this.coconutTrees = coconutTrees;
        updateTargetingValidator();
    }
    
    public void setBambooTrees(Map<String, BambooTree> bambooTrees) {
        this.bambooTrees = bambooTrees;
        updateTargetingValidator();
    }
    
    public void setBananaTrees(Map<String, BananaTree> bananaTrees) {
        this.bananaTrees = bananaTrees;
        updateTargetingValidator();
    }
    
    public void setApples(Map<String, Apple> apples) {
        this.apples = apples;
    }
    
    public void setBananas(Map<String, Banana> bananas) {
        this.bananas = bananas;
    }
    
    public void setBambooStacks(Map<String, wagemaker.uk.items.BambooStack> bambooStacks) {
        this.bambooStacks = bambooStacks;
    }
    
    public void setBabyBamboos(Map<String, wagemaker.uk.items.BabyBamboo> babyBamboos) {
        this.babyBamboos = babyBamboos;
    }
    
    public void setBabyTrees(Map<String, BabyTree> babyTrees) {
        this.babyTrees = babyTrees;
    }
    
    public void setWoodStacks(Map<String, WoodStack> woodStacks) {
        this.woodStacks = woodStacks;
    }
    
    public void setPebbles(Map<String, Pebble> pebbles) {
        this.pebbles = pebbles;
    }
    
    public void setStones(Map<String, Stone> stones) {
        this.stones = stones;
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
    
    public void setInventoryManager(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
        updateTargetingValidator();
    }
    
    public void setPlantingSystem(PlantingSystem plantingSystem) {
        this.plantingSystem = plantingSystem;
    }
    
    public void setBiomeManager(BiomeManager biomeManager) {
        this.biomeManager = biomeManager;
        updateTargetingValidator();
    }
    
    public void setPlantedBamboos(Map<String, PlantedBamboo> plantedBamboos) {
        this.plantedBamboos = plantedBamboos;
        updateTargetingValidator();
    }
    
    public void setPlantedTrees(Map<String, PlantedTree> plantedTrees) {
        this.plantedTrees = plantedTrees;
        updateTargetingValidator();
    }
    
    /**
     * Update the targeting system validator with current dependencies.
     * Called whenever planting-related dependencies are updated.
     */
    private void updateTargetingValidator() {
        if (inventoryManager != null && biomeManager != null && 
            plantedBamboos != null && bambooTrees != null) {
            PlantingTargetValidator validator = new PlantingTargetValidator(
                inventoryManager, biomeManager, plantedBamboos, bambooTrees
            );
            
            // Set all tree maps if available
            if (plantedTrees != null && trees != null && appleTrees != null && 
                coconutTrees != null && bananaTrees != null) {
                validator.setTreeMaps(plantedTrees, trees, appleTrees, coconutTrees, bananaTrees);
            }
            
            targetingSystem.setValidator(validator);
        }
    }
    
    // Multiplayer getters and setters
    public String getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
    public GameClient getGameClient() {
        return gameClient;
    }
    
    public void setGameClient(GameClient gameClient) {
        this.gameClient = gameClient;
    }
    
    public boolean isLocalPlayer() {
        return isLocalPlayer;
    }
    
    public void setLocalPlayer(boolean isLocalPlayer) {
        this.isLocalPlayer = isLocalPlayer;
    }
    
    public void setRemotePlayers(Map<String, RemotePlayer> remotePlayers) {
        this.remotePlayers = remotePlayers;
    }
    
    /**
     * Find the nearest remote player within attack range (100 pixels).
     * @return The nearest remote player in range, or null if none found
     */
    private RemotePlayer findNearestRemotePlayerInRange() {
        if (remotePlayers == null || remotePlayers.isEmpty()) {
            return null;
        }
        
        RemotePlayer nearestPlayer = null;
        float nearestDistance = Float.MAX_VALUE;
        
        for (RemotePlayer remotePlayer : remotePlayers.values()) {
            if (isPlayerInAttackRange(remotePlayer)) {
                float distance = calculateDistance(remotePlayer);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestPlayer = remotePlayer;
                }
            }
        }
        
        return nearestPlayer;
    }
    
    /**
     * Check if a specific remote player is within attack range (100 pixels).
     * Uses Euclidean distance calculation: sqrt(dx² + dy²)
     * @param remotePlayer The remote player to check
     * @return true if player is within 100 pixels, false otherwise
     */
    private boolean isPlayerInAttackRange(RemotePlayer remotePlayer) {
        if (remotePlayer == null) {
            return false;
        }
        
        float distance = calculateDistance(remotePlayer);
        return distance <= 100; // 100 pixel attack range
    }
    
    /**
     * Calculate Euclidean distance between this player and a remote player.
     * @param remotePlayer The remote player to calculate distance to
     * @return The distance in pixels
     */
    private float calculateDistance(RemotePlayer remotePlayer) {
        // Calculate center positions
        float playerCenterX = x + 32; // Player is 64x64, center is +32
        float playerCenterY = y + 32;
        float remoteCenterX = remotePlayer.getX() + 32;
        float remoteCenterY = remotePlayer.getY() + 32;
        
        // Calculate distance using Euclidean formula: sqrt(dx² + dy²)
        float dx = remoteCenterX - playerCenterX;
        float dy = remoteCenterY - playerCenterY;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public void update(float deltaTime) {
        isMoving = false;
        
        // Handle inventory navigation mode toggle (only when menu is not open)
        if (gameMenu != null && !gameMenu.isAnyMenuOpen()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
                toggleInventoryNavigationMode();
            }
            
            // ESC exits inventory mode (if active and menu not open)
            if (inventoryNavigationMode && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                inventoryNavigationMode = false;
                if (inventoryManager != null) {
                    inventoryManager.clearSelection();
                }
                System.out.println("Inventory navigation mode: OFF (ESC pressed, deselected)");
            }
        }
        
        // Exit inventory mode when menu opens
        if (gameMenu != null && gameMenu.isAnyMenuOpen() && inventoryNavigationMode) {
            inventoryNavigationMode = false;
            if (inventoryManager != null) {
                inventoryManager.clearSelection();
            }
            System.out.println("Inventory navigation mode: OFF (menu opened, deselected)");
        }
        
        // Track movement in both directions
        boolean movingLeft = false;
        boolean movingRight = false;
        boolean movingUp = false;
        boolean movingDown = false;
        
        // handle input with collision detection
        float newX = x;
        float newY = y;
        
        // Only process movement if inventory navigation mode is OFF
        if (!inventoryNavigationMode) {
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
        
        // Send position updates to server in multiplayer mode (client-side prediction)
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            wagemaker.uk.network.Direction networkDirection = convertToNetworkDirection(currentDirection);
            gameClient.sendPlayerMovement(x, y, networkDirection, isMoving);
        }

        // Handle inventory navigation or targeting input (only when menu is not open)
        if (gameMenu != null && !gameMenu.isAnyMenuOpen()) {
            if (inventoryNavigationMode) {
                handleInventoryNavigation();
            }
            handleTargetingInput();
            handlePlantingAction();
        }

        // Handle spacebar - context-sensitive action
        // Priority 1: If targeting is active: plant item at target
        // Priority 2: If item is selected (consumable): consume the item
        // Priority 3: Otherwise: attack nearby targets
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (targetingSystem.isActive()) {
                // Targeting mode: use spacebar to plant
                handleSpacebarPlanting();
            } else if (inventoryManager != null && inventoryManager.getSelectedSlot() != -1) {
                // Item selected: try to consume it
                consumeSelectedItem();
            } else {
                // Normal mode: use spacebar to attack
                attackNearbyTargets();
            }
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
        
        // Update hunger system
        updateHunger(deltaTime);
        
        // Check for apple pickups
        checkApplePickups();
        
        // Check for banana pickups
        checkBananaPickups();
        
        // Check for bamboo stack pickups
        checkBambooStackPickups();
        
        // Check for baby bamboo pickups
        checkBabyBambooPickups();
        
        // Check for baby tree pickups
        checkBabyTreePickups();
        
        // Check for wood stack pickups
        checkWoodStackPickups();
        
        // Check for pebble pickups
        checkPebblePickups();
        
        // Track previous health for change detection
        previousHealth = health;
        
        // Send health updates to server in multiplayer mode
        checkAndSendHealthUpdate();
        
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
        
        // Create directional idle frames (first frame of each animation)
        idleUpFrame = new TextureRegion(spriteSheet, 0, 512, 64, 64);    // First UP frame
        idleLeftFrame = new TextureRegion(spriteSheet, 0, 576, 64, 64);  // First LEFT frame
        idleDownFrame = new TextureRegion(spriteSheet, 0, 640, 64, 64);  // First DOWN frame
        idleRightFrame = new TextureRegion(spriteSheet, 0, 704, 64, 64); // First RIGHT frame
        
        // Set default animation to LEFT (Row 3 - character facing camera/standing still)
        currentAnimation = walkLeftAnimation;
    }

    public TextureRegion getCurrentFrame() {
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
        
        // Check collision with stones
        if (stones != null) {
            for (Stone stone : stones.values()) {
                if (stone.collidesWith(newX, newY, 64, 64)) {
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

    private void attackNearbyTargets() {
        // Priority 1: Check for remote players in range
        RemotePlayer targetPlayer = findNearestRemotePlayerInRange();
        if (targetPlayer != null && gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            // TODO: Cooldown for player attacks (currently disabled for testing)
            // Check cooldown for player attacks only (0.5 seconds since last player attack)
            // float currentTime = System.currentTimeMillis() / 1000.0f; // Convert to seconds
            // if (currentTime - lastPlayerAttackTime < PLAYER_ATTACK_COOLDOWN) {
            //     // Still on cooldown for player attacks
            //     System.out.println("Player attack on cooldown");
            //     return;
            // }
            
            // Attack the remote player
            String targetPlayerId = targetPlayer.getPlayerId();
            gameClient.sendAttackAction(targetPlayerId);
            // lastPlayerAttackTime = currentTime; // Disabled with cooldown
            System.out.println("Attacking player: " + targetPlayerId);
            return; // Don't attack trees if we attacked a player
        }
        
        // Priority 2: Attack trees if no players in range (no cooldown for trees)
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
                // Send attack action to server in multiplayer mode
                if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
                    gameClient.sendAttackAction(targetKey);
                    // In multiplayer, server handles tree destruction
                    attackedSomething = true;
                } else {
                    // Single-player mode: handle locally
                    float healthBefore = targetTree.getHealth();
                    boolean destroyed = targetTree.attack();
                    if (destroyed) {
                        System.out.println("Attacking tree, health before: " + healthBefore);
                        System.out.println("Tree health after attack: " + targetTree.getHealth() + ", destroyed: " + destroyed);
                    }
                    attackedSomething = true;
                    
                    if (destroyed) {
                        // Random drop selection (0, 1, or 2)
                        int dropType = random.nextInt(3);
                        float treeX = targetTree.getX();
                        float treeY = targetTree.getY();
                        
                        switch (dropType) {
                            case 0: // 2x BabyTree
                                babyTrees.put(targetKey + "-item1", new BabyTree(treeX, treeY));
                                babyTrees.put(targetKey + "-item2", new BabyTree(treeX + 8, treeY));
                                System.out.println("Dropped 2x BabyTree at: " + treeX + ", " + treeY);
                                break;
                            case 1: // 2x WoodStack
                                woodStacks.put(targetKey + "-item1", new WoodStack(treeX, treeY));
                                woodStacks.put(targetKey + "-item2", new WoodStack(treeX + 8, treeY));
                                System.out.println("Dropped 2x WoodStack at: " + treeX + ", " + treeY);
                                break;
                            case 2: // 1x BabyTree + 1x WoodStack
                                babyTrees.put(targetKey + "-item1", new BabyTree(treeX, treeY));
                                woodStacks.put(targetKey + "-item2", new WoodStack(treeX + 8, treeY));
                                System.out.println("Dropped 1x BabyTree + 1x WoodStack at: " + treeX + ", " + treeY);
                                break;
                        }
                        
                        // Register for respawn before removing
                        if (gameInstance != null && gameInstance instanceof wagemaker.uk.gdx.MyGdxGame) {
                            wagemaker.uk.gdx.MyGdxGame game = (wagemaker.uk.gdx.MyGdxGame) gameInstance;
                            wagemaker.uk.respawn.RespawnManager respawnManager = game.getRespawnManager();
                            if (respawnManager != null) {
                                respawnManager.registerDestruction(
                                    targetKey,
                                    wagemaker.uk.respawn.ResourceType.TREE,
                                    targetTree.getX(),
                                    targetTree.getY(),
                                    wagemaker.uk.network.TreeType.SMALL
                                );
                            }
                        }
                        
                        targetTree.dispose();
                        trees.remove(targetKey);
                        clearedPositions.put(targetKey, true);
                        System.out.println("Tree removed from world");
                    }
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
                // Send attack action to server in multiplayer mode
                if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
                    gameClient.sendAttackAction(targetKey);
                    // In multiplayer, server handles tree destruction, healing, and item spawning
                    attackedSomething = true;
                } else {
                    // Single-player mode: handle locally
                    float healthBefore = targetAppleTree.getHealth();
                    boolean destroyed = targetAppleTree.attack();
                    if (destroyed) {
                        System.out.println("Attacking apple tree, health before: " + healthBefore);
                        System.out.println("Apple tree health after attack: " + targetAppleTree.getHealth() + ", destroyed: " + destroyed);
                    }
                    attackedSomething = true;
                    
                    if (destroyed) {
                        // FIRST: Immediate health restoration (10%)
                        float currentHealth = health;
                        health = Math.min(100, currentHealth + 10);
                        System.out.println("Apple tree destroyed! Health restored: 10% (from " + currentHealth + " to " + health + ")");
                        
                        // THEN: Spawn apple at tree position
                        apples.put(targetKey, new Apple(targetAppleTree.getX(), targetAppleTree.getY()));
                        System.out.println("Apple dropped at: " + targetAppleTree.getX() + ", " + targetAppleTree.getY());
                        
                        // Register for respawn before removing
                        if (gameInstance != null && gameInstance instanceof wagemaker.uk.gdx.MyGdxGame) {
                            wagemaker.uk.gdx.MyGdxGame game = (wagemaker.uk.gdx.MyGdxGame) gameInstance;
                            wagemaker.uk.respawn.RespawnManager respawnManager = game.getRespawnManager();
                            if (respawnManager != null) {
                                respawnManager.registerDestruction(
                                    targetKey,
                                    wagemaker.uk.respawn.ResourceType.TREE,
                                    targetAppleTree.getX(),
                                    targetAppleTree.getY(),
                                    wagemaker.uk.network.TreeType.APPLE
                                );
                            }
                        }
                        
                        targetAppleTree.dispose();
                        appleTrees.remove(targetKey);
                        clearedPositions.put(targetKey, true);
                    }
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
                // Send attack action to server in multiplayer mode
                if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
                    gameClient.sendAttackAction(targetKey);
                    // In multiplayer, server handles tree destruction
                    attackedSomething = true;
                } else {
                    // Single-player mode: handle locally
                    float healthBefore = targetCoconutTree.getHealth();
                    boolean destroyed = targetCoconutTree.attack();
                    if (destroyed) {
                        System.out.println("Attacking coconut tree, health before: " + healthBefore);
                        System.out.println("Coconut tree health after attack: " + targetCoconutTree.getHealth() + ", destroyed: " + destroyed);
                    }
                    attackedSomething = true;
                    
                    if (destroyed) {
                        // Register for respawn before removing
                        if (gameInstance != null && gameInstance instanceof wagemaker.uk.gdx.MyGdxGame) {
                            wagemaker.uk.gdx.MyGdxGame game = (wagemaker.uk.gdx.MyGdxGame) gameInstance;
                            wagemaker.uk.respawn.RespawnManager respawnManager = game.getRespawnManager();
                            if (respawnManager != null) {
                                respawnManager.registerDestruction(
                                    targetKey,
                                    wagemaker.uk.respawn.ResourceType.TREE,
                                    targetCoconutTree.getX(),
                                    targetCoconutTree.getY(),
                                    wagemaker.uk.network.TreeType.COCONUT
                                );
                            }
                        }
                        
                        targetCoconutTree.dispose();
                        coconutTrees.remove(targetKey);
                        clearedPositions.put(targetKey, true);
                        System.out.println("Coconut tree removed from world");
                    }
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
                // Send attack action to server in multiplayer mode
                if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
                    gameClient.sendAttackAction(targetKey);
                    // In multiplayer, server handles tree destruction
                    attackedSomething = true;
                } else {
                    // Single-player mode: handle locally
                    float healthBefore = targetBambooTree.getHealth();
                    boolean destroyed = targetBambooTree.attack();
                    if (destroyed) {
                        System.out.println("Attacking bamboo tree, health before: " + healthBefore);
                        System.out.println("Bamboo tree health after attack: " + targetBambooTree.getHealth() + ", destroyed: " + destroyed);
                    }
                    attackedSomething = true;
                    
                    if (destroyed) {
                        // Randomly choose drop pattern: 
                        // 33% chance: 1 BambooStack + 1 BabyBamboo
                        // 33% chance: 2 BambooStack
                        // 33% chance: 2 BabyBamboo
                        float dropRoll = (float) Math.random();
                        
                        if (dropRoll < 0.33f) {
                            // Drop 1 BambooStack + 1 BabyBamboo (original behavior)
                            bambooStacks.put(targetKey + "-bamboostack", 
                                new BambooStack(targetBambooTree.getX(), targetBambooTree.getY()));
                            babyBamboos.put(targetKey + "-babybamboo", 
                                new BabyBamboo(targetBambooTree.getX() + 8, targetBambooTree.getY()));
                            System.out.println("BambooStack dropped at: " + targetBambooTree.getX() + ", " + targetBambooTree.getY());
                            System.out.println("BabyBamboo dropped at: " + (targetBambooTree.getX() + 8) + ", " + targetBambooTree.getY());
                        } else if (dropRoll < 0.66f) {
                            // Drop 2 BambooStack
                            bambooStacks.put(targetKey + "-bamboostack1", 
                                new BambooStack(targetBambooTree.getX(), targetBambooTree.getY()));
                            bambooStacks.put(targetKey + "-bamboostack2", 
                                new BambooStack(targetBambooTree.getX() + 8, targetBambooTree.getY()));
                            System.out.println("2x BambooStack dropped at: " + targetBambooTree.getX() + ", " + targetBambooTree.getY());
                        } else {
                            // Drop 2 BabyBamboo
                            babyBamboos.put(targetKey + "-babybamboo1", 
                                new BabyBamboo(targetBambooTree.getX(), targetBambooTree.getY()));
                            babyBamboos.put(targetKey + "-babybamboo2", 
                                new BabyBamboo(targetBambooTree.getX() + 8, targetBambooTree.getY()));
                            System.out.println("2x BabyBamboo dropped at: " + targetBambooTree.getX() + ", " + targetBambooTree.getY());
                        }
                        
                        // Register for respawn before removing
                        if (gameInstance != null && gameInstance instanceof wagemaker.uk.gdx.MyGdxGame) {
                            wagemaker.uk.gdx.MyGdxGame game = (wagemaker.uk.gdx.MyGdxGame) gameInstance;
                            wagemaker.uk.respawn.RespawnManager respawnManager = game.getRespawnManager();
                            if (respawnManager != null) {
                                respawnManager.registerDestruction(
                                    targetKey,
                                    wagemaker.uk.respawn.ResourceType.TREE,
                                    targetBambooTree.getX(),
                                    targetBambooTree.getY(),
                                    wagemaker.uk.network.TreeType.BAMBOO
                                );
                            }
                        }
                        
                        targetBambooTree.dispose();
                        bambooTrees.remove(targetKey);
                        clearedPositions.put(targetKey, true);
                    }
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
                // Send attack action to server in multiplayer mode
                if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
                    gameClient.sendAttackAction(targetKey);
                    // In multiplayer, server handles tree destruction and item spawning
                    attackedSomething = true;
                } else {
                    // Single-player mode: handle locally
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
                        
                        // Register for respawn before removing
                        if (gameInstance != null && gameInstance instanceof wagemaker.uk.gdx.MyGdxGame) {
                            wagemaker.uk.gdx.MyGdxGame game = (wagemaker.uk.gdx.MyGdxGame) gameInstance;
                            wagemaker.uk.respawn.RespawnManager respawnManager = game.getRespawnManager();
                            if (respawnManager != null) {
                                respawnManager.registerDestruction(
                                    targetKey,
                                    wagemaker.uk.respawn.ResourceType.TREE,
                                    targetBananaTree.getX(),
                                    targetBananaTree.getY(),
                                    wagemaker.uk.network.TreeType.BANANA
                                );
                            }
                        }
                        
                        targetBananaTree.dispose();
                        bananaTrees.remove(targetKey);
                        clearedPositions.put(targetKey, true);
                    }
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
                    System.out.println("Cactus destroyed! Will respawn after timer...");
                    
                    // Register for respawn before removing
                    if (gameInstance != null && gameInstance instanceof wagemaker.uk.gdx.MyGdxGame) {
                        wagemaker.uk.gdx.MyGdxGame game = (wagemaker.uk.gdx.MyGdxGame) gameInstance;
                        wagemaker.uk.respawn.RespawnManager respawnManager = game.getRespawnManager();
                        if (respawnManager != null) {
                            // Use a fixed key for the single cactus
                            String cactusKey = "cactus-128,128";
                            respawnManager.registerDestruction(
                                cactusKey,
                                wagemaker.uk.respawn.ResourceType.TREE,
                                cactus.getX(),
                                cactus.getY(),
                                wagemaker.uk.network.TreeType.CACTUS
                            );
                        }
                        
                        // Dispose the cactus
                        try {
                            final wagemaker.uk.trees.Cactus cactusToDispose = cactus;
                            gameInstance.getClass().getMethod("deferOperation", Runnable.class)
                                .invoke(gameInstance, (Runnable) () -> cactusToDispose.dispose());
                        } catch (Exception e) {
                            cactus.dispose();
                        }
                        
                        // Set cactus to null so it's not rendered
                        setCactus(null);
                    }
                }
                attackedSomething = true;
            }
        }
        
        // Attack stones within range (individual collision)
        if (stones != null && !attackedSomething) {
            Stone targetStone = null;
            String targetKey = null;
            
            for (Map.Entry<String, Stone> entry : stones.entrySet()) {
                Stone stone = entry.getValue();
                
                if (stone.isInAttackRange(x, y)) {
                    targetStone = stone;
                    targetKey = entry.getKey();
                    break;
                }
            }
            
            if (targetStone != null) {
                // Send attack action to server in multiplayer mode
                if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
                    gameClient.sendAttackAction(targetKey);
                    // In multiplayer, server handles stone destruction and pebble spawning
                    attackedSomething = true;
                } else {
                    // Single-player mode: handle locally
                    float healthBefore = targetStone.getHealth();
                    boolean destroyed = targetStone.attack();
                    if (destroyed) {
                        System.out.println("Attacking stone, health before: " + healthBefore);
                        System.out.println("Stone health after attack: " + targetStone.getHealth() + ", destroyed: " + destroyed);
                    }
                    attackedSomething = true;
                    
                    if (destroyed) {
                        // Spawn pebble at stone position
                        pebbles.put(targetKey + "-pebble", new Pebble(targetStone.getX(), targetStone.getY()));
                        System.out.println("Pebble dropped at: " + targetStone.getX() + ", " + targetStone.getY());
                        
                        // Register for respawn before removing
                        if (gameInstance != null && gameInstance instanceof wagemaker.uk.gdx.MyGdxGame) {
                            wagemaker.uk.gdx.MyGdxGame game = (wagemaker.uk.gdx.MyGdxGame) gameInstance;
                            wagemaker.uk.respawn.RespawnManager respawnManager = game.getRespawnManager();
                            if (respawnManager != null) {
                                respawnManager.registerDestruction(
                                    targetKey,
                                    wagemaker.uk.respawn.ResourceType.STONE,
                                    targetStone.getX(),
                                    targetStone.getY(),
                                    null  // stones don't have a tree type
                                );
                            }
                        }
                        
                        // Use deferred operation for texture disposal (threading safety)
                        if (gameInstance != null) {
                            try {
                                final Stone stoneToDispose = targetStone;
                                gameInstance.getClass().getMethod("deferOperation", Runnable.class)
                                    .invoke(gameInstance, (Runnable) () -> stoneToDispose.dispose());
                            } catch (Exception e) {
                                // Fallback: dispose immediately if deferOperation not available
                                targetStone.dispose();
                            }
                        } else {
                            targetStone.dispose();
                        }
                        
                        stones.remove(targetKey);
                        clearedPositions.put(targetKey, true);
                        System.out.println("Stone removed from world");
                    }
                }
            }
        }
        
        if (!attackedSomething) {
            System.out.println("No trees in range to attack");
        }
    }

    /**
     * Toggle inventory navigation mode on/off.
     * When ON, arrow keys navigate inventory slots instead of moving player.
     * When OFF, arrow keys move player normally.
     */
    private void toggleInventoryNavigationMode() {
        inventoryNavigationMode = !inventoryNavigationMode;
        
        if (inventoryNavigationMode) {
            // Entering mode: auto-select first slot
            if (inventoryManager != null) {
                inventoryManager.setSelectedSlot(0);
                updateTargetingForSelection(0);
            }
            System.out.println("Inventory navigation mode: ON (slot 0 selected)");
        } else {
            // Exiting mode: deselect everything
            if (inventoryManager != null) {
                inventoryManager.clearSelection();
            }
            System.out.println("Inventory navigation mode: OFF (deselected)");
        }
    }
    
    /**
     * Handle arrow key navigation in inventory mode.
     * RIGHT/LEFT cycle through slots with wrap-around.
     * UP/DOWN reserved for future multi-row support.
     */
    private void handleInventoryNavigation() {
        if (inventoryManager == null) {
            return;
        }
        
        int currentSelection = inventoryManager.getSelectedSlot();
        int previousSelection = currentSelection;
        int newSelection = currentSelection;
        
        // RIGHT arrow: move to next slot (wrap around)
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            if (currentSelection == -1) {
                newSelection = 0; // Start at first slot
            } else {
                newSelection = (currentSelection + 1) % 7; // Wrap to 0 after slot 6
            }
        }
        // LEFT arrow: move to previous slot (wrap around)
        else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            if (currentSelection == -1) {
                newSelection = 6; // Start at last slot
            } else {
                newSelection = (currentSelection - 1 + 7) % 7; // Wrap to 6 from slot 0
            }
        }
        // UP/DOWN: Reserved for future multi-row layout
        else if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            // No-op for now, reserved for future enhancement
            return;
        }
        
        // Apply selection change if different
        if (newSelection != previousSelection) {
            inventoryManager.setSelectedSlot(newSelection);
            
            // Update targeting system based on new selection
            updateTargetingForSelection(newSelection);
        }
    }
    
    /**
     * Update targeting system when inventory selection changes.
     * Activates targeting for plantable items, deactivates for consumables.
     */
    private void updateTargetingForSelection(int slot) {
        if (slot == -1) {
            // Item deselected - deactivate targeting
            if (targetingSystem.isActive()) {
                targetingSystem.deactivate();
            }
        } else {
            // Item selected - check if it's a plantable item
            wagemaker.uk.inventory.ItemType selectedItemType = inventoryManager.getSelectedItemType();
            
            // Only activate targeting for plantable items (not consumables)
            boolean isPlantable = selectedItemType != null && 
                                 !selectedItemType.restoresHealth() && 
                                 !selectedItemType.reducesHunger();
            
            if (isPlantable) {
                // Plantable item - activate targeting at player position
                if (!targetingSystem.isActive()) {
                    targetingSystem.activate(x, y, TargetingMode.ADJACENT, new TargetingCallback() {
                        @Override
                        public void onTargetConfirmed(float targetX, float targetY) {
                            // Handle planting based on selected item
                            handleItemPlacement(targetX, targetY);
                        }
                        
                        @Override
                        public void onTargetCancelled() {
                            System.out.println("Item placement cancelled");
                        }
                    });
                }
            } else {
                // Consumable item - deactivate targeting if active
                if (targetingSystem.isActive()) {
                    targetingSystem.deactivate();
                }
            }
        }
    }
    
    /**
     * Handle item placement at the confirmed target coordinates.
     * Called when the player confirms a target position (presses P).
     * Handles different item types based on the currently selected inventory slot.
     */
    private void handleItemPlacement(float targetX, float targetY) {
        if (inventoryManager == null) {
            return;
        }
        
        int selectedSlot = inventoryManager.getSelectedSlot();
        
        // Handle baby bamboo planting (slot 2)
        if (selectedSlot == 2) {
            if (inventoryManager.getCurrentInventory().getBabyBambooCount() > 0) {
                executePlanting(targetX, targetY);
            } else {
                System.out.println("No baby bamboo in inventory");
            }
        }
        // Handle baby tree planting (slot 4)
        else if (selectedSlot == 4) {
            if (inventoryManager.getCurrentInventory().getBabyTreeCount() > 0) {
                executeTreePlanting(targetX, targetY);
            } else {
                System.out.println("No baby tree in inventory");
            }
        }
        // Add handling for other items here in the future
        else {
            System.out.println("Selected item cannot be placed");
        }
    }
    
    /**
     * Handle targeting input when targeting mode is active.
     * Processes A/W/D/S keys for directional movement and ESC for cancellation.
     * Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 4.1, 4.2, 4.3, 4.4
     */
    private void handleTargetingInput() {
        if (!targetingSystem.isActive()) {
            return;
        }
        
        // Handle directional input (A/W/D/S)
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            targetingSystem.moveTarget(wagemaker.uk.targeting.Direction.LEFT);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            targetingSystem.moveTarget(wagemaker.uk.targeting.Direction.UP);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            targetingSystem.moveTarget(wagemaker.uk.targeting.Direction.RIGHT);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            targetingSystem.moveTarget(wagemaker.uk.targeting.Direction.DOWN);
        }
        
        // Handle cancellation (ESC key)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            targetingSystem.cancel();
        }
    }
    
    /**
     * Handle planting action when "p" key is pressed.
     * Places item at current target position without deactivating targeting.
     * Targeting stays active as long as an item is selected.
     * Requirements: 1.1, 1.3, 4.1, 4.2
     */
    private void handlePlantingAction() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            // If targeting is active and target is valid, place the item
            if (targetingSystem.isActive() && targetingSystem.isTargetValid()) {
                // Get current target coordinates
                float[] coords = targetingSystem.getTargetCoordinates();
                
                // Place the item at target location
                handleItemPlacement(coords[0], coords[1]);
                
                // Targeting remains active - don't deactivate
                // It will only deactivate when the player deselects the item
            }
        }
    }
    
    /**
     * Handle planting action when spacebar is pressed while targeting is active.
     * This provides an alternative to the 'P' key for planting.
     * Spacebar is context-sensitive: plants when targeting, attacks when not targeting.
     */
    private void handleSpacebarPlanting() {
        // If targeting is active and target is valid, place the item
        if (targetingSystem.isActive() && targetingSystem.isTargetValid()) {
            // Get current target coordinates
            float[] coords = targetingSystem.getTargetCoordinates();
            
            // Place the item at target location
            handleItemPlacement(coords[0], coords[1]);
            
            // Targeting remains active - don't deactivate
            // It will only deactivate when the player deselects the item
        }
    }
    
    /**
     * Execute planting at the specified target coordinates.
     * Called by the targeting system callback when target is confirmed.
     * Includes error handling and state rollback on failure.
     */
    private void executePlanting(float targetX, float targetY) {
        // Store initial inventory state for potential rollback
        int initialBabyBambooCount = inventoryManager.getCurrentInventory().getBabyBambooCount();
        
        // Attempt to plant baby bamboo at target coordinates
        PlantedBamboo plantedBamboo = plantingSystem.attemptPlant(
            targetX, targetY, 
            inventoryManager, 
            biomeManager, 
            plantedBamboos, 
            bambooTrees
        );
        
        // Add planted bamboo to game world map if planting succeeds
        if (plantedBamboo != null) {
            // Generate unique key for the planted bamboo
            float tileX = (float) (Math.floor(targetX / 64.0) * 64.0);
            float tileY = (float) (Math.floor(targetY / 64.0) * 64.0);
            String key = "planted-bamboo-" + (int)tileX + "-" + (int)tileY;
            plantedBamboos.put(key, plantedBamboo);
            System.out.println("Planted bamboo added to game world at: " + key);
            
            // Send planting message to server in multiplayer
            if (gameClient != null && gameClient.isConnected()) {
                try {
                    gameClient.sendBambooPlant(key, tileX, tileY);
                    
                    // Send inventory update after planting (baby bamboo was deducted)
                    inventoryManager.sendInventoryUpdateToServer();
                } catch (Exception e) {
                    // Network error - rollback state
                    System.err.println("Failed to send planting message to server: " + e.getMessage());
                    
                    // Remove planted bamboo from local state
                    plantedBamboos.remove(key);
                    plantedBamboo.dispose();
                    
                    // Restore inventory (add baby bamboo back)
                    inventoryManager.getCurrentInventory().addBabyBamboo(1);
                    
                    System.out.println("Planting rolled back due to network error");
                }
            } else {
                // Single-player mode: check for auto-deselection
                inventoryManager.checkAndAutoDeselect();
            }
        } else {
            System.out.println("Planting failed: invalid location or no baby bamboo in inventory");
        }
    }
    
    /**
     * Execute tree planting at the specified target coordinates.
     * Called by the targeting system callback when target is confirmed for baby trees.
     * Includes error handling and state rollback on failure.
     */
    private void executeTreePlanting(float targetX, float targetY) {
        // Store initial inventory state for potential rollback
        int initialBabyTreeCount = inventoryManager.getCurrentInventory().getBabyTreeCount();
        
        // Validate grass biome for tree planting
        if (!plantingSystem.canPlantTree(targetX, targetY, biomeManager)) {
            System.out.println("Tree planting failed: can only plant trees on grass biomes");
            return;
        }
        
        // Attempt to plant baby tree at target coordinates
        String plantedTreeId = plantingSystem.plantTree(targetX, targetY, plantedTrees);
        
        if (plantedTreeId != null) {
            // Deduct baby tree from inventory
            boolean removed = inventoryManager.getCurrentInventory().removeBabyTree(1);
            if (!removed) {
                // Failed to remove item - rollback planting
                plantedTrees.remove(plantedTreeId);
                System.out.println("Tree planting failed: could not deduct baby tree from inventory");
                return;
            }
            
            System.out.println("Baby tree planted successfully at: " + plantedTreeId);
            
            // Send planting message to server in multiplayer
            if (gameClient != null && gameClient.isConnected()) {
                try {
                    // Extract coordinates from planted tree
                    PlantedTree plantedTree = plantedTrees.get(plantedTreeId);
                    if (plantedTree != null) {
                        gameClient.sendTreePlant(plantedTreeId, plantedTree.getX(), plantedTree.getY());
                        
                        // Send inventory update after planting (baby tree was deducted)
                        inventoryManager.sendInventoryUpdateToServer();
                    }
                } catch (Exception e) {
                    // Network error - rollback state
                    System.err.println("Failed to send tree planting message to server: " + e.getMessage());
                    
                    // Remove planted tree from local state
                    PlantedTree plantedTree = plantedTrees.remove(plantedTreeId);
                    if (plantedTree != null) {
                        plantedTree.dispose();
                    }
                    
                    // Restore inventory (add baby tree back)
                    inventoryManager.getCurrentInventory().addBabyTree(1);
                    
                    System.out.println("Tree planting rolled back due to network error");
                }
            } else {
                // Single-player mode: check for auto-deselection
                inventoryManager.checkAndAutoDeselect();
            }
        } else {
            System.out.println("Tree planting failed: invalid location or tile already occupied");
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
        
        // Reset health and hunger
        health = 100;
        hunger = 0;
        
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
    
    /**
     * Get the current hunger level (0-100%).
     * @return Current hunger level
     */
    public float getHunger() {
        return hunger;
    }
    
    /**
     * Set the hunger level (clamped between 0 and 100).
     * @param hunger New hunger level
     */
    public void setHunger(float hunger) {
        this.hunger = Math.max(0, Math.min(100, hunger)); // Clamp between 0 and 100
    }
    
    /**
     * Update hunger system - increases hunger by 1% every 60 seconds.
     * When hunger reaches 100%, player dies and respawns.
     * Sends hunger updates to server in multiplayer mode.
     * Requirements: 4.1, 4.2, 4.3, 5.1, 5.2, 5.3, 5.4
     * 
     * @param deltaTime Time elapsed since last frame in seconds
     */
    private void updateHunger(float deltaTime) {
        // Accumulate time
        hungerTimer += deltaTime;
        
        // Check if 60 seconds have passed
        if (hungerTimer >= HUNGER_INTERVAL) {
            // Increase hunger by 1%
            hunger = Math.min(100, hunger + 1);
            
            // Subtract interval from timer (preserve remainder for precision)
            hungerTimer -= HUNGER_INTERVAL;
            
            System.out.println("Hunger increased to: " + hunger + "%");
            
            // Send hunger update to server in multiplayer mode
            checkAndSendHungerUpdate();
            
            // Check for hunger death
            if (hunger >= 100) {
                handleHungerDeath();
            }
        }
    }
    
    /**
     * Handle player death from hunger.
     * Respawns player at origin (0, 0) with full health and no hunger.
     * Requirements: 5.1, 5.2, 5.3, 5.4
     */
    private void handleHungerDeath() {
        System.out.println("Player died from hunger! Respawning...");
        
        // Reset health and hunger
        health = 100;
        hunger = 0;
        hungerTimer = 0; // Reset timer as well
        
        // Respawn at origin (0, 0)
        x = 0;
        y = 0;
        
        // Send respawn message in multiplayer with hunger reset
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            gameClient.sendPlayerRespawn(x, y, health, hunger);
            System.out.println("Sent respawn message to server with hunger reset");
        }
        
        System.out.println("Player respawned at origin (0, 0)!");
    }
    
    /**
     * Consume the currently selected item from inventory.
     * Handles apple consumption (restore 10% health) and banana consumption (reduce 5% hunger).
     * Called when space bar is pressed with a consumable item selected.
     * Requirements: 1.2, 2.1
     */
    private void consumeSelectedItem() {
        if (inventoryManager == null) {
            return;
        }
        
        // Get the selected item type
        wagemaker.uk.inventory.ItemType selectedItemType = inventoryManager.getSelectedItemType();
        
        if (selectedItemType == null) {
            return;
        }
        
        // Check if the item is consumable
        if (!selectedItemType.restoresHealth() && !selectedItemType.reducesHunger()) {
            System.out.println("Selected item (" + selectedItemType.name() + ") cannot be consumed");
            return;
        }
        
        // Try to consume the item via inventory manager
        boolean consumed = inventoryManager.tryConsumeSelectedItem(this);
        
        if (consumed) {
            // Log successful consumption
            if (selectedItemType.restoresHealth()) {
                System.out.println("Consumed " + selectedItemType.name() + " - restored " + 
                                 selectedItemType.getHealthRestore() + "% health");
            } else if (selectedItemType.reducesHunger()) {
                System.out.println("Consumed " + selectedItemType.name() + " - reduced 5% hunger");
            }
            
            // Send consumption to server in multiplayer mode
            if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
                // Convert inventory ItemType to network ItemType
                wagemaker.uk.network.ItemType networkItemType = convertToNetworkItemType(selectedItemType);
                if (networkItemType != null) {
                    gameClient.sendItemConsumption(networkItemType);
                }
            }
        } else {
            System.out.println("Failed to consume " + selectedItemType.name() + " - none in inventory");
        }
    }
    
    /**
     * Convert inventory ItemType to network ItemType.
     * @param inventoryItemType The inventory item type
     * @return The corresponding network item type, or null if not found
     */
    private wagemaker.uk.network.ItemType convertToNetworkItemType(wagemaker.uk.inventory.ItemType inventoryItemType) {
        if (inventoryItemType == null) {
            return null;
        }
        
        switch (inventoryItemType) {
            case APPLE:
                return wagemaker.uk.network.ItemType.APPLE;
            case BANANA:
                return wagemaker.uk.network.ItemType.BANANA;
            case BABY_BAMBOO:
                return wagemaker.uk.network.ItemType.BABY_BAMBOO;
            case BAMBOO_STACK:
                return wagemaker.uk.network.ItemType.BAMBOO_STACK;
            case BABY_TREE:
                return wagemaker.uk.network.ItemType.BABY_TREE;
            case WOOD_STACK:
                return wagemaker.uk.network.ItemType.WOOD_STACK;
            case PEBBLE:
                return wagemaker.uk.network.ItemType.PEBBLE;
            default:
                return null;
        }
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
        // Send pickup request to server in multiplayer mode
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            gameClient.sendItemPickup(appleKey);
            // In multiplayer, server handles item removal and health restoration
            // The server will broadcast the pickup to all clients
        } else {
            // Single-player mode: handle locally via inventory manager
            if (inventoryManager != null) {
                inventoryManager.collectItem(wagemaker.uk.inventory.ItemType.APPLE);
            }
            
            // Remove apple from game
            if (apples.containsKey(appleKey)) {
                Apple apple = apples.get(appleKey);
                apple.dispose();
                apples.remove(appleKey);
                System.out.println("Apple removed from game");
            }
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
        // Send pickup request to server in multiplayer mode
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            gameClient.sendItemPickup(bananaKey);
            // In multiplayer, server handles item removal and health restoration
            // The server will broadcast the pickup to all clients
        } else {
            // Single-player mode: handle locally via inventory manager
            if (inventoryManager != null) {
                inventoryManager.collectItem(wagemaker.uk.inventory.ItemType.BANANA);
            }
            
            // Remove banana from game
            if (bananas.containsKey(bananaKey)) {
                Banana banana = bananas.get(bananaKey);
                banana.dispose();
                bananas.remove(bananaKey);
                System.out.println("Banana removed from game");
            }
        }
    }
    
    private void checkBambooStackPickups() {
        if (bambooStacks != null) {
            // Check all bamboo stacks for pickup
            for (Map.Entry<String, BambooStack> entry : bambooStacks.entrySet()) {
                BambooStack bambooStack = entry.getValue();
                String bambooStackKey = entry.getKey();
                
                // Check if player is close enough to pick up bamboo stack (32px range)
                float dx = Math.abs((x + 32) - (bambooStack.getX() + 16)); // Player center to bamboo stack center
                float dy = Math.abs((y + 32) - (bambooStack.getY() + 16)); // BambooStack is 32x32, so center is +16
                
                if (dx <= 32 && dy <= 32) {
                    // Pick up the bamboo stack
                    pickupBambooStack(bambooStackKey);
                    break; // Only pick up one bamboo stack per frame
                }
            }
        }
    }
    
    private void pickupBambooStack(String bambooStackKey) {
        // Send pickup request to server in multiplayer mode
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            gameClient.sendItemPickup(bambooStackKey);
            // In multiplayer, server handles item removal
            // The server will broadcast the pickup to all clients
        } else {
            // Single-player mode: handle locally via inventory manager
            if (inventoryManager != null) {
                inventoryManager.collectItem(wagemaker.uk.inventory.ItemType.BAMBOO_STACK);
            }
            
            // Remove bamboo stack from game
            if (bambooStacks.containsKey(bambooStackKey)) {
                BambooStack bambooStack = bambooStacks.get(bambooStackKey);
                bambooStack.dispose();
                bambooStacks.remove(bambooStackKey);
                System.out.println("BambooStack removed from game");
            }
        }
    }
    
    private void checkBabyBambooPickups() {
        if (babyBamboos != null) {
            // Check all baby bamboos for pickup
            for (Map.Entry<String, BabyBamboo> entry : babyBamboos.entrySet()) {
                BabyBamboo babyBamboo = entry.getValue();
                String babyBambooKey = entry.getKey();
                
                // Check if player is close enough to pick up baby bamboo (32px range)
                float dx = Math.abs((x + 32) - (babyBamboo.getX() + 16)); // Player center to baby bamboo center
                float dy = Math.abs((y + 32) - (babyBamboo.getY() + 16)); // BabyBamboo is 32x32, so center is +16
                
                if (dx <= 32 && dy <= 32) {
                    // Pick up the baby bamboo
                    pickupBabyBamboo(babyBambooKey);
                    break; // Only pick up one baby bamboo per frame
                }
            }
        }
    }
    
    private void pickupBabyBamboo(String babyBambooKey) {
        // Send pickup request to server in multiplayer mode
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            gameClient.sendItemPickup(babyBambooKey);
            // In multiplayer, server handles item removal
            // The server will broadcast the pickup to all clients
        } else {
            // Single-player mode: handle locally via inventory manager
            if (inventoryManager != null) {
                inventoryManager.collectItem(wagemaker.uk.inventory.ItemType.BABY_BAMBOO);
            }
            
            // Remove baby bamboo from game
            if (babyBamboos.containsKey(babyBambooKey)) {
                BabyBamboo babyBamboo = babyBamboos.get(babyBambooKey);
                babyBamboo.dispose();
                babyBamboos.remove(babyBambooKey);
                System.out.println("BabyBamboo removed from game");
            }
        }
    }
    
    private void checkBabyTreePickups() {
        if (babyTrees != null) {
            // Check all baby trees for pickup
            for (Map.Entry<String, BabyTree> entry : babyTrees.entrySet()) {
                BabyTree babyTree = entry.getValue();
                String babyTreeKey = entry.getKey();
                
                // Check if player is close enough to pick up baby tree (32px range)
                float dx = Math.abs((x + 32) - (babyTree.getX() + 16)); // Player center to baby tree center
                float dy = Math.abs((y + 32) - (babyTree.getY() + 16)); // BabyTree is 32x32, so center is +16
                
                if (dx <= 32 && dy <= 32) {
                    // Pick up the baby tree
                    pickupBabyTree(babyTreeKey);
                    break; // Only pick up one baby tree per frame
                }
            }
        }
    }
    
    private void pickupBabyTree(String babyTreeKey) {
        // Send pickup request to server in multiplayer mode
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            gameClient.sendItemPickup(babyTreeKey);
            // In multiplayer, server handles item removal
            // The server will broadcast the pickup to all clients
        } else {
            // Single-player mode: handle locally via inventory manager
            if (inventoryManager != null) {
                inventoryManager.collectItem(wagemaker.uk.inventory.ItemType.BABY_TREE);
            }
            
            // Remove baby tree from game
            if (babyTrees.containsKey(babyTreeKey)) {
                BabyTree babyTree = babyTrees.get(babyTreeKey);
                babyTree.dispose();
                babyTrees.remove(babyTreeKey);
                System.out.println("BabyTree removed from game");
            }
        }
    }
    
    private void checkWoodStackPickups() {
        if (woodStacks != null) {
            // Check all wood stacks for pickup
            for (Map.Entry<String, WoodStack> entry : woodStacks.entrySet()) {
                WoodStack woodStack = entry.getValue();
                String woodStackKey = entry.getKey();
                
                // Check if player is close enough to pick up wood stack (32px range)
                float dx = Math.abs((x + 32) - (woodStack.getX() + 16)); // Player center to wood stack center
                float dy = Math.abs((y + 32) - (woodStack.getY() + 16)); // WoodStack is 32x32, so center is +16
                
                if (dx <= 32 && dy <= 32) {
                    // Pick up the wood stack
                    pickupWoodStack(woodStackKey);
                    break; // Only pick up one wood stack per frame
                }
            }
        }
    }
    
    private void pickupWoodStack(String woodStackKey) {
        // Send pickup request to server in multiplayer mode
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            gameClient.sendItemPickup(woodStackKey);
            // In multiplayer, server handles item removal
            // The server will broadcast the pickup to all clients
        } else {
            // Single-player mode: handle locally via inventory manager
            if (inventoryManager != null) {
                inventoryManager.collectItem(wagemaker.uk.inventory.ItemType.WOOD_STACK);
            }
            
            // Remove wood stack from game
            if (woodStacks.containsKey(woodStackKey)) {
                WoodStack woodStack = woodStacks.get(woodStackKey);
                woodStack.dispose();
                woodStacks.remove(woodStackKey);
                System.out.println("WoodStack removed from game");
            }
        }
    }

    private void checkPebblePickups() {
        if (pebbles != null) {
            // Check all pebbles for pickup
            for (Map.Entry<String, Pebble> entry : pebbles.entrySet()) {
                Pebble pebble = entry.getValue();
                String pebbleKey = entry.getKey();
                
                // Check if player is close enough to pick up pebble (32px range)
                float dx = Math.abs((x + 32) - (pebble.getX() + 16)); // Player center to pebble center
                float dy = Math.abs((y + 32) - (pebble.getY() + 16)); // Pebble is 32x32, so center is +16
                
                if (dx <= 32 && dy <= 32) {
                    // Pick up the pebble
                    pickupPebble(pebbleKey);
                    break; // Only pick up one pebble per frame
                }
            }
        }
    }
    
    private void pickupPebble(String pebbleKey) {
        // Send pickup request to server in multiplayer mode
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            gameClient.sendItemPickup(pebbleKey);
            // In multiplayer, server handles item removal
            // The server will broadcast the pickup to all clients
        } else {
            // Single-player mode: handle locally via inventory manager
            if (inventoryManager != null) {
                inventoryManager.collectItem(wagemaker.uk.inventory.ItemType.PEBBLE);
            }
            
            // Remove pebble from game
            if (pebbles.containsKey(pebbleKey)) {
                Pebble pebble = pebbles.get(pebbleKey);
                pebble.dispose();
                pebbles.remove(pebbleKey);
                System.out.println("Pebble removed from game");
            }
        }
    }

    public boolean shouldShowHealthBar() {
        return health < 100;
    }
    
    /**
     * Convert Player's internal Direction enum to network Direction enum.
     */
    private wagemaker.uk.network.Direction convertToNetworkDirection(Direction direction) {
        switch (direction) {
            case UP:
                return wagemaker.uk.network.Direction.UP;
            case DOWN:
                return wagemaker.uk.network.Direction.DOWN;
            case LEFT:
                return wagemaker.uk.network.Direction.LEFT;
            case RIGHT:
                return wagemaker.uk.network.Direction.RIGHT;
            default:
                return wagemaker.uk.network.Direction.DOWN;
        }
    }
    
    /**
     * Check if health has changed and send update to server in multiplayer mode.
     */
    private void checkAndSendHealthUpdate() {
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            // Only send update if health has changed
            if (health != previousHealth) {
                gameClient.sendPlayerHealth(health);
                previousHealth = health;
            }
        }
    }
    
    /**
     * Check if hunger has changed and send update to server in multiplayer mode.
     */
    private void checkAndSendHungerUpdate() {
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            // Only send update if hunger has changed
            if (hunger != previousHunger) {
                gameClient.sendPlayerHunger(hunger);
                previousHunger = hunger;
            }
        }
    }
    
    /**
     * Get the targeting system instance.
     * Used by MyGdxGame for rendering the target indicator.
     * 
     * @return The targeting system instance
     */
    public TargetingSystem getTargetingSystem() {
        return targetingSystem;
    }
    
    /**
     * Get the target indicator renderer instance.
     * Used by MyGdxGame for rendering the target indicator.
     * 
     * @return The target indicator renderer instance
     */
    public TargetIndicatorRenderer getTargetIndicatorRenderer() {
        return targetIndicatorRenderer;
    }

    public void dispose() {
        if (spriteSheet != null) spriteSheet.dispose();
        if (targetIndicatorRenderer != null) targetIndicatorRenderer.dispose();
    }
}