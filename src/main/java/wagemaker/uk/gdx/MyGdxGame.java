package wagemaker.uk.gdx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import wagemaker.uk.items.Apple;
import wagemaker.uk.items.BabyBamboo;
import wagemaker.uk.items.BabyTree;
import wagemaker.uk.items.BambooStack;
import wagemaker.uk.items.Banana;
import wagemaker.uk.items.PalmFiber;
import wagemaker.uk.items.Pebble;
import wagemaker.uk.items.WoodStack;
import wagemaker.uk.localization.LocalizationManager;
import wagemaker.uk.planting.PlantedBamboo;
import wagemaker.uk.planting.PlantedTree;
import wagemaker.uk.planting.PlantingSystem;
import wagemaker.uk.network.GameClient;
import wagemaker.uk.network.GameServer;
import wagemaker.uk.network.ItemState;
import wagemaker.uk.network.ItemType;
import wagemaker.uk.network.PlayerJoinMessage;
import wagemaker.uk.network.StoneState;
import wagemaker.uk.network.TreeState;
import wagemaker.uk.network.TreeType;
import wagemaker.uk.network.WorldState;
import wagemaker.uk.player.Player;
import wagemaker.uk.player.RemotePlayer;
import wagemaker.uk.objects.Stone;
import wagemaker.uk.trees.AppleTree;
import wagemaker.uk.trees.BambooTree;
import wagemaker.uk.trees.BananaTree;
import wagemaker.uk.trees.Cactus;
import wagemaker.uk.trees.CoconutTree;
import wagemaker.uk.trees.SmallTree;
import wagemaker.uk.biome.BiomeManager;
import wagemaker.uk.ui.Compass;
import wagemaker.uk.ui.GameMenu;
import wagemaker.uk.ui.HealthBarUI;
import wagemaker.uk.weather.RainSystem;
import wagemaker.uk.world.WorldSaveData;
import wagemaker.uk.world.WorldSaveManager;
import wagemaker.uk.inventory.InventoryManager;
import wagemaker.uk.respawn.RespawnManager;
import wagemaker.uk.respawn.ResourceType;
import wagemaker.uk.targeting.TargetingSystem;

/**
 * Main game class for Woodlanders multiplayer game.
 * 
 * <h2>MULTIPLAYER THREADING GUIDELINES</h2>
 * 
 * <p>This game uses a multi-threaded architecture for multiplayer networking. Understanding
 * the threading model is critical to avoid crashes and ensure stability.</p>
 * 
 * <h3>Threading Model:</h3>
 * <ul>
 *   <li><b>Render Thread (Main Thread):</b> Owns the OpenGL context and handles all rendering operations.
 *       This is the only thread that can safely call OpenGL functions.</li>
 *   <li><b>Network Thread (GameClient-Receive):</b> Receives and processes network messages from the server.
 *       This thread runs in the background and must NOT call OpenGL functions directly.</li>
 * </ul>
 * 
 * <h3>Critical Rule:</h3>
 * <p><b>ALL OpenGL operations MUST execute on the Render Thread.</b> This includes:</p>
 * <ul>
 *   <li>Texture creation and disposal (texture.dispose())</li>
 *   <li>Shader operations</li>
 *   <li>Buffer operations</li>
 *   <li>Any LibGDX graphics calls</li>
 * </ul>
 * 
 * <h3>The Deferred Operation Pattern:</h3>
 * <p>When network message handlers need to perform OpenGL operations, they must use the
 * deferred operation pattern via the {@link #deferOperation(Runnable)} method.</p>
 * 
 * <h4>How It Works:</h4>
 * <ol>
 *   <li>Network thread receives a message (e.g., item pickup)</li>
 *   <li>Handler immediately updates game state (remove from maps)</li>
 *   <li>Handler defers OpenGL operations (texture disposal) to render thread</li>
 *   <li>Render thread processes deferred operations at start of next frame</li>
 * </ol>
 * 
 * <h4>Example - CORRECT Usage:</h4>
 * <pre>{@code
 * // In network message handler (Network Thread)
 * public void handleItemPickup(ItemPickupMessage message) {
 *     // ✅ CORRECT: Immediate state update (thread-safe)
 *     Apple apple = apples.remove(message.getItemId());
 *     if (apple != null) {
 *         // ✅ CORRECT: Defer OpenGL operation to render thread
 *         deferOperation(() -> apple.dispose());
 *     }
 * }
 * }</pre>
 * 
 * <h4>Example - INCORRECT Usage:</h4>
 * <pre>{@code
 * // In network message handler (Network Thread)
 * public void handleItemPickup(ItemPickupMessage message) {
 *     Apple apple = apples.remove(message.getItemId());
 *     if (apple != null) {
 *         // ❌ INCORRECT: OpenGL call from network thread - WILL CRASH!
 *         apple.dispose();  // This calls texture.dispose() internally
 *     }
 * }
 * }</pre>
 * 
 * <h3>When to Use deferOperation():</h3>
 * <ul>
 *   <li>Disposing textures, sprites, or any graphics resources</li>
 *   <li>Creating new graphics resources from network messages</li>
 *   <li>Any operation that touches OpenGL state</li>
 *   <li>When in doubt, defer it!</li>
 * </ul>
 * 
 * <h3>When NOT to Use deferOperation():</h3>
 * <ul>
 *   <li>Updating game state (maps, lists, variables)</li>
 *   <li>Logging or printing</li>
 *   <li>Network operations</li>
 *   <li>Pure data processing</li>
 * </ul>
 * 
 * <h3>Thread Safety Notes:</h3>
 * <ul>
 *   <li>The deferred operation queue uses {@link java.util.concurrent.ConcurrentLinkedQueue}
 *       which is lock-free and thread-safe for concurrent add/poll operations.</li>
 *   <li>Game state updates (map operations) should happen immediately on the network thread
 *       to ensure responsive gameplay.</li>
 *   <li>Only resource disposal should be deferred to avoid visual artifacts.</li>
 * </ul>
 * 
 * <h3>Debugging Threading Issues:</h3>
 * <p>If you encounter OpenGL errors or crashes:</p>
 * <ol>
 *   <li>Check if the crash occurs during multiplayer item pickup or player join/leave</li>
 *   <li>Look for OpenGL calls (dispose(), texture operations) in network message handlers</li>
 *   <li>Ensure all such operations are wrapped in deferOperation()</li>
 *   <li>Enable debug logging to see deferred operation queue activity</li>
 * </ol>
 * 
 * @see #deferOperation(Runnable)
 * @see #removeItem(String)
 */
public class MyGdxGame extends ApplicationAdapter {
    /**
     * Enum representing the current game mode.
     */
    public enum GameMode {
        SINGLEPLAYER,       // Local single-player game
        MULTIPLAYER_HOST,   // Hosting a multiplayer server and playing
        MULTIPLAYER_CLIENT  // Connected to a multiplayer server as client
    }
    
    SpriteBatch batch;
    ShapeRenderer shapeRenderer;
    BiomeManager biomeManager;
    Player player;
    InventoryManager inventoryManager;
    wagemaker.uk.ui.InventoryRenderer inventoryRenderer;
    OrthographicCamera camera;
    Viewport viewport;
    Map<String, SmallTree> trees;
    Map<String, AppleTree> appleTrees;
    Map<String, CoconutTree> coconutTrees;
    Map<String, BambooTree> bambooTrees;
    Map<String, BananaTree> bananaTrees;
    Map<String, Apple> apples;
    Map<String, Banana> bananas;
    Map<String, BambooStack> bambooStacks;
    Map<String, BabyBamboo> babyBamboos;
    Map<String, BabyTree> babyTrees;
    Map<String, WoodStack> woodStacks;
    Map<String, Pebble> pebbles;
    Map<String, PalmFiber> palmFibers;
    Map<String, PlantedBamboo> plantedBamboos;
    Map<String, PlantedTree> plantedTrees;
    Map<String, Stone> stones;
    Map<String, Stone> stoneMap;
    Cactus cactus; // Single cactus near spawn
    Map<String, Boolean> clearedPositions;
    PlantingSystem plantingSystem;
    Random random;
    long worldSeed; // World seed for deterministic generation
    GameMenu gameMenu;
    RainSystem rainSystem; // Weather system for localized rain effects
    wagemaker.uk.weather.DynamicRainManager dynamicRainManager; // Dynamic rain event manager
    RespawnManager respawnManager; // Resource respawn timer system
    
    // Multiplayer fields
    private GameMode gameMode;
    private GameServer gameServer;  // Only used in MULTIPLAYER_HOST mode
    private GameClient gameClient;  // Used in both MULTIPLAYER_HOST and MULTIPLAYER_CLIENT modes
    private Map<String, RemotePlayer> remotePlayers;  // Other players in multiplayer
    private wagemaker.uk.ui.ConnectionQualityIndicator connectionQualityIndicator;
    
    // UI components
    private Compass compass;
    private HealthBarUI healthBarUI;
    
    // Notification system
    private String currentNotification;
    private float notificationTimer;
    
    // Connection state
    private String pendingConnectionAddress;
    private int pendingConnectionPort;
    private boolean isHosting;
    private String lastConnectionAddress; // Full address with port for saving to config
    
    // Pending player joins (to be processed on main thread)
    private java.util.concurrent.ConcurrentLinkedQueue<PlayerJoinMessage> pendingPlayerJoins;
    private java.util.concurrent.ConcurrentLinkedQueue<String> pendingPlayerLeaves;
    private java.util.concurrent.ConcurrentLinkedQueue<ItemState> pendingItemSpawns;
    private java.util.concurrent.ConcurrentLinkedQueue<String> pendingTreeRemovals;
    private java.util.concurrent.ConcurrentLinkedQueue<TreeState> pendingTreeCreations;
    private java.util.concurrent.ConcurrentLinkedQueue<wagemaker.uk.network.BambooPlantMessage> pendingBambooPlants;
    private java.util.concurrent.ConcurrentLinkedQueue<wagemaker.uk.network.BambooTransformMessage> pendingBambooTransforms;
    private java.util.concurrent.ConcurrentLinkedQueue<wagemaker.uk.network.TreePlantMessage> pendingTreePlants;
    private java.util.concurrent.ConcurrentLinkedQueue<wagemaker.uk.network.TreeTransformMessage> pendingTreeTransforms;
    
    // Queue for operations that must execute on the render thread (e.g., OpenGL operations)
    private java.util.concurrent.ConcurrentLinkedQueue<Runnable> pendingDeferredOperations;
    
    // World loading state management
    private WorldSaveData pendingWorldLoad;
    private WorldState previousWorldState;
    private boolean worldLoadInProgress;
    
    // Camera dimensions for infinite world
    static final int CAMERA_WIDTH = 1280;
    static final int CAMERA_HEIGHT = 1024;

    @Override
    public void create() {
        // Initialize localization system first (before any UI components)
        LocalizationManager.getInstance().initialize();
        
        // Initialize game mode to single-player by default
        gameMode = GameMode.SINGLEPLAYER;
        
        // Initialize notification system
        currentNotification = null;
        notificationTimer = 0;
        
        // setup camera with screen viewport to match window size
        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        viewport.apply();
        camera.position.set(0, 0, 0);
        camera.update();
        
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        trees = new HashMap<>();
        appleTrees = new HashMap<>();
        coconutTrees = new HashMap<>();
        bambooTrees = new HashMap<>();
        bananaTrees = new HashMap<>();
        apples = new HashMap<>();
        bananas = new HashMap<>();
        bambooStacks = new HashMap<>();
        babyBamboos = new HashMap<>();
        babyTrees = new HashMap<>();
        woodStacks = new HashMap<>();
        pebbles = new HashMap<>();
        palmFibers = new HashMap<>();
        plantedBamboos = new HashMap<>();
        plantedTrees = new HashMap<>();
        stones = new HashMap<>();
        stoneMap = new HashMap<>();
        clearedPositions = new HashMap<>();
        remotePlayers = new HashMap<>();
        random = new Random();
        plantingSystem = new PlantingSystem();
        worldSeed = 0; // Will be set by server in multiplayer, or remain 0 for single-player
        
        // Initialize pending player queues
        pendingPlayerJoins = new java.util.concurrent.ConcurrentLinkedQueue<>();
        pendingPlayerLeaves = new java.util.concurrent.ConcurrentLinkedQueue<>();
        pendingItemSpawns = new java.util.concurrent.ConcurrentLinkedQueue<>();
        pendingTreeRemovals = new java.util.concurrent.ConcurrentLinkedQueue<>();
        pendingTreeCreations = new java.util.concurrent.ConcurrentLinkedQueue<>();
        pendingBambooPlants = new java.util.concurrent.ConcurrentLinkedQueue<>();
        pendingBambooTransforms = new java.util.concurrent.ConcurrentLinkedQueue<>();
        pendingTreePlants = new java.util.concurrent.ConcurrentLinkedQueue<>();
        pendingTreeTransforms = new java.util.concurrent.ConcurrentLinkedQueue<>();
        
        // Initialize deferred operations queue
        pendingDeferredOperations = new java.util.concurrent.ConcurrentLinkedQueue<>();
        
        // Initialize world loading state
        pendingWorldLoad = null;
        previousWorldState = null;
        worldLoadInProgress = false;

        // create single cactus near player spawn (128px away)
        cactus = new Cactus(128, 128);

        // create player at origin
        player = new Player(0, 0, camera);
        player.setTrees(trees);
        player.setAppleTrees(appleTrees);
        player.setCoconutTrees(coconutTrees);
        player.setBambooTrees(bambooTrees);
        player.setBananaTrees(bananaTrees);
        player.setApples(apples);
        player.setBananas(bananas);
        player.setBambooStacks(bambooStacks);
        player.setBabyBamboos(babyBamboos);
        player.setBabyTrees(babyTrees);
        player.setWoodStacks(woodStacks);
        player.setPebbles(pebbles);
        player.setPalmFibers(palmFibers);
        player.setStones(stones);
        player.setCactus(cactus);
        player.setGameInstance(this);
        player.setClearedPositions(clearedPositions);
        player.setRemotePlayers(remotePlayers);
        
        // Initialize inventory manager
        inventoryManager = new InventoryManager(player);
        player.setInventoryManager(inventoryManager);
        
        // Initialize inventory renderer
        inventoryRenderer = new wagemaker.uk.ui.InventoryRenderer();

        gameMenu = new GameMenu();
        gameMenu.setPlayer(player); // Set player reference for saving
        gameMenu.setGameInstance(this); // Set game instance reference for multiplayer
        gameMenu.setInventoryManager(inventoryManager); // Set inventory manager for save/load
        player.setGameMenu(gameMenu);
        
        // Load player position from save file if it exists
        gameMenu.loadPlayerPosition();
        
        // Initialize connection quality indicator (will be set when connecting)
        connectionQualityIndicator = new wagemaker.uk.ui.ConnectionQualityIndicator(null, gameMenu.getFont());
        
        // Initialize compass
        compass = new Compass();
        
        // Load compass target from PlayerConfig and apply if it exists
        wagemaker.uk.client.PlayerConfig playerConfig = wagemaker.uk.client.PlayerConfig.load();
        Float targetX = playerConfig.getCompassTargetX();
        Float targetY = playerConfig.getCompassTargetY();
        
        if (targetX != null && targetY != null) {
            compass.setCustomTarget(targetX, targetY);
            System.out.println("Restored compass target from config: (" + targetX + ", " + targetY + ")");
        }
        
        // Pass Compass reference to GameMenu
        gameMenu.setCompass(compass);
        
        // Initialize health bar UI
        healthBarUI = new HealthBarUI(shapeRenderer);

        // Initialize biome manager for ground texture variation
        biomeManager = new BiomeManager();
        biomeManager.initialize();
        
        // Set planting system references on player
        player.setPlantingSystem(plantingSystem);
        player.setBiomeManager(biomeManager);
        player.setPlantedBamboos(plantedBamboos);
        player.setPlantedTrees(plantedTrees);

        // Initialize rain system
        rainSystem = new RainSystem(shapeRenderer);
        rainSystem.initialize();
        
        // Initialize dynamic rain manager for random rain events
        dynamicRainManager = new wagemaker.uk.weather.DynamicRainManager(rainSystem.getZoneManager());
        
        // Note: We no longer initialize default rain zones
        // Rain will be managed dynamically by DynamicRainManager
        
        // Initialize respawn manager (starts in single-player mode)
        respawnManager = new RespawnManager(this, true);

    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        
        // Process all pending deferred operations (must run on render thread)
        Runnable operation;
        while ((operation = pendingDeferredOperations.poll()) != null) {
            try {
                // Optional: Log for debugging
                if (Gdx.app.getLogLevel() >= com.badlogic.gdx.Application.LOG_DEBUG) {
                    System.out.println("[DEBUG] Executing deferred operation on render thread");
                }
                operation.run();
            } catch (Exception e) {
                System.err.println("Error executing deferred operation: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Process pending player joins on main thread (for OpenGL context)
        processPendingPlayerJoins();
        processPendingPlayerLeaves();
        processPendingItemSpawns();
        processPendingTreeRemovals();
        processPendingBambooPlants();
        processPendingBambooTransforms();
        processPendingTreePlants();
        processPendingTreeTransforms();
        processPendingTreeCreations();
        
        // Process pending world load operations on main thread (for OpenGL context)
        processPendingWorldLoad();

        gameMenu.update();
        
        // Handle error dialog actions
        if (gameMenu.getErrorDialog().isRetrySelected()) {
            gameMenu.getErrorDialog().reset();
            retryConnection();
        } else if (gameMenu.getErrorDialog().isCancelled()) {
            gameMenu.getErrorDialog().reset();
            cancelConnection();
        } else if (gameMenu.getErrorDialog().isOkSelected()) {
            gameMenu.getErrorDialog().reset();
            // For success messages, just close the dialog - no additional action needed
        }
        
        // Handle multiplayer menu selections - delegated to GameMenu
        // GameMenu will call back to attemptHostServer() when needed
        
        // Handle connect dialog confirmation
        if (gameMenu.getConnectDialog().isConfirmed()) {
            String address = gameMenu.getConnectDialog().getEnteredAddress();
            gameMenu.getConnectDialog().resetConfirmation();
            
            // Parse address and port
            String[] parts = address.split(":");
            String serverAddress = parts[0];
            int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 25565;
            
            attemptConnectToServer(serverAddress, port);
        }
        
        // Update notification timer
        if (currentNotification != null) {
            notificationTimer -= deltaTime;
            if (notificationTimer <= 0) {
                currentNotification = null;
            }
        }
        
        // Check for connection loss in multiplayer mode
        if (gameMode != GameMode.SINGLEPLAYER && gameClient != null) {
            if (!gameClient.isConnected() && !gameClient.isReconnecting()) {
                // Connection lost and not reconnecting
                displayNotification("Connection Lost");
                
                // Return to single player mode
                gameMode = GameMode.SINGLEPLAYER;
                
                // Switch inventory back to single-player mode
                if (inventoryManager != null) {
                    inventoryManager.setMultiplayerMode(false);
                }
                
                // Clean up multiplayer resources
                if (gameServer != null) {
                    gameServer.stop();
                    gameServer = null;
                }
                
                remotePlayers.clear();
                
            } else if (gameClient.isReconnecting()) {
                // Show reconnection attempt
                displayNotification("Reconnecting... (" + gameClient.getReconnectAttempts() + "/3)");
            }
        }

        // Update rain system with player position (always update, even when menu is open)
        float playerCenterX = player.getX() + 50; // Player sprite is 100x100, center at +50
        float playerCenterY = player.getY() + 50;
        
        // Update dynamic rain manager (handles random rain events)
        dynamicRainManager.update(deltaTime, playerCenterX, playerCenterY);
        
        // Update rain system rendering
        rainSystem.update(deltaTime, playerCenterX, playerCenterY, camera);
        
        // Update respawn manager (check for expired timers)
        if (respawnManager != null) {
            respawnManager.update(deltaTime);
        }
        
        if (!gameMenu.isAnyMenuOpen()) {
            // update player and camera
            player.update(deltaTime);
        
        // update trees
        for (SmallTree tree : trees.values()) {
            tree.update(deltaTime);
        }
        for (AppleTree appleTree : appleTrees.values()) {
            appleTree.update(deltaTime);
        }
        for (CoconutTree coconutTree : coconutTrees.values()) {
            coconutTree.update(deltaTime);
        }
        for (BambooTree bambooTree : bambooTrees.values()) {
            bambooTree.update(deltaTime);
        }
        for (BananaTree bananaTree : bananaTrees.values()) {
            bananaTree.update(deltaTime);
        }
        
        // update planted bamboos and check for transformations
        List<String> bambooToTransform = new ArrayList<>();
        for (Map.Entry<String, PlantedBamboo> entry : plantedBamboos.entrySet()) {
            PlantedBamboo planted = entry.getValue();
            if (planted.update(deltaTime)) {
                bambooToTransform.add(entry.getKey());
            }
        }
        
        // transform mature planted bamboos into bamboo trees
        for (String key : bambooToTransform) {
            PlantedBamboo planted = plantedBamboos.remove(key);
            float x = planted.getX();
            float y = planted.getY();
            
            // Generate bamboo tree ID (reuse the planted bamboo key)
            String bambooTreeId = key;
            
            BambooTree tree = new BambooTree(x, y);
            bambooTrees.put(bambooTreeId, tree);
            planted.dispose();
            
            // Send transformation message in multiplayer
            if (gameClient != null && gameClient.isConnected()) {
                wagemaker.uk.network.BambooTransformMessage message = 
                    new wagemaker.uk.network.BambooTransformMessage(
                        gameClient.getClientId(), 
                        key, 
                        bambooTreeId, 
                        x, 
                        y
                    );
                gameClient.sendMessage(message);
            }
        }
        
        // update planted trees and check for transformations
        List<String> treesToTransform = new ArrayList<>();
        for (Map.Entry<String, PlantedTree> entry : plantedTrees.entrySet()) {
            PlantedTree planted = entry.getValue();
            if (planted.update(deltaTime)) {
                treesToTransform.add(entry.getKey());
            }
        }
        
        // transform mature planted trees into small trees
        for (String key : treesToTransform) {
            PlantedTree planted = plantedTrees.remove(key);
            float x = planted.getX();
            float y = planted.getY();
            
            // Generate small tree ID (reuse the planted tree key)
            String smallTreeId = key;
            
            SmallTree tree = new SmallTree(x, y);
            trees.put(smallTreeId, tree);
            planted.dispose();
            
            // Send transformation message in multiplayer
            if (gameClient != null && gameClient.isConnected()) {
                wagemaker.uk.network.TreeTransformMessage message = 
                    new wagemaker.uk.network.TreeTransformMessage(
                        gameClient.getClientId(), 
                        key, 
                        smallTreeId, 
                        x, 
                        y
                    );
                gameClient.sendMessage(message);
            }
        }
        
        // update cactus
        if (cactus != null) {
            cactus.update(deltaTime);
        }
        
        // update stones
        for (Stone stone : stones.values()) {
            stone.update(deltaTime);
        }
        
        // update remote players in multiplayer mode
        if (gameMode != GameMode.SINGLEPLAYER) {
            for (RemotePlayer remotePlayer : remotePlayers.values()) {
                remotePlayer.update(deltaTime);
            }
        }
        }
        
        camera.update();

        Gdx.gl.glClearColor(0.1f, 0.12f, 0.16f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // apply viewport and camera
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        // draw infinite grass background around camera
        drawInfiniteGrass();
        // draw target indicator (after terrain, before planted bamboos)
        // Only render for local player when targeting is active
        if (player.getTargetingSystem().isActive()) {
            float[] targetCoords = player.getTargetingSystem().getTargetCoordinates();
            boolean isValid = player.getTargetingSystem().isTargetValid();
            player.getTargetIndicatorRenderer().render(batch, targetCoords[0], targetCoords[1], isValid);
        }
        // draw planted bamboos (after terrain, before trees)
        drawPlantedBamboos();
        // draw planted trees (after terrain, before trees)
        drawPlantedTrees();
        // draw respawn indicators (after terrain, before trees)
        if (respawnManager != null) {
            respawnManager.renderIndicators(batch, deltaTime, 
                                          camera.position.x, camera.position.y,
                                          viewport.getWorldWidth(), viewport.getWorldHeight());
        }
        // draw trees
        drawTrees();
        drawCoconutTrees();
        drawBambooTrees();
        drawStones();
        drawApples();
        drawBananas();
        drawBambooStacks();
        drawBabyBamboos();
        drawBabyTrees();
        drawWoodStacks();
        drawPebbles();
        drawPalmFibers();
        drawCactus();
        // draw player before apple trees so foliage appears in front
        batch.draw(player.getCurrentFrame(), player.getX(), player.getY(), 100, 100);
        // draw remote players at same z-order as local player
        renderRemotePlayers();
        drawAppleTrees();
        drawBananaTrees();
        batch.end();
        
        // Render rain effects after batch.end() but before UI
        rainSystem.render(camera);
        
        // draw player name tag above player
        gameMenu.renderPlayerNameTag(batch);
        
        // draw remote player name tags in multiplayer mode
        if (gameMode != GameMode.SINGLEPLAYER) {
            renderRemotePlayerNameTags();
        }
        
        // draw health bars
        drawHealthBars();
        
        // render unified health bar UI for local player
        if (healthBarUI != null) {
            // Set up screen-space projection for UI rendering
            shapeRenderer.setProjectionMatrix(camera.combined);
            healthBarUI.render(player.getHealth(), player.getHunger(), camera, viewport);
        }
        
        // draw connection quality indicator in multiplayer mode
        if (gameMode != GameMode.SINGLEPLAYER && connectionQualityIndicator != null) {
            float screenX = camera.position.x + viewport.getWorldWidth() / 2 - 20;
            float screenY = camera.position.y + viewport.getWorldHeight() / 2 - 20;
            connectionQualityIndicator.render(batch, shapeRenderer, screenX, screenY);
        }
        
        // update and render compass (only when menu is not open)
        if (!gameMenu.isAnyMenuOpen()) {
            compass.update(player.getX(), player.getY(), 0.0f, 0.0f);
        }
        batch.begin();
        compass.render(batch, camera, viewport);
        batch.end();
        
        // render inventory UI
        if (inventoryRenderer != null && inventoryManager != null) {
            int selectedSlot = inventoryManager.getSelectedSlot();
            inventoryRenderer.render(batch, inventoryManager.getCurrentInventory(), 
                                    camera.position.x, camera.position.y, 
                                    viewport.getWorldWidth(), viewport.getWorldHeight(),
                                    selectedSlot);
        }
        
        // draw menu on top
        gameMenu.render(batch, shapeRenderer, camera.position.x, camera.position.y, viewport.getWorldWidth(), viewport.getWorldHeight());
        
        // draw notification if active
        if (currentNotification != null) {
            renderNotification();
        }
    }
    
    private void drawInfiniteGrass() {
        // Calculate visible area around camera
        float camX = camera.position.x;
        float camY = camera.position.y;
        
        // Use actual viewport dimensions for grass rendering
        float viewWidth = viewport.getWorldWidth();
        float viewHeight = viewport.getWorldHeight();
        
        // Draw grass tiles covering camera view + buffer
        int startX = (int)((camX - viewWidth) / 64) * 64;
        int startY = (int)((camY - viewHeight) / 64) * 64;
        int endX = (int)((camX + viewWidth) / 64) * 64 + 64;
        int endY = (int)((camY + viewHeight) / 64) * 64 + 64;
        
        for (int x = startX; x <= endX; x += 64) {
            for (int y = startY; y <= endY; y += 64) {
                // Get appropriate texture for this position based on biome
                Texture texture = biomeManager.getTextureForPosition(x, y);
                batch.draw(texture, x, y, 64, 64);
                
                // Only generate trees/stones in singleplayer mode
                // In multiplayer, server is authoritative and sends all entities
                if (gameMode == GameMode.SINGLEPLAYER) {
                    generateTreeAt(x, y);
                    generateStoneAt(x, y);
                }
            }
        }
    }
    
    /**
     * Generates a tree at the specified world coordinates using deterministic procedural generation.
     * 
     * <p><b>DETERMINISTIC BEHAVIOR GUARANTEE:</b></p>
     * <p>This method ensures that the same world seed and coordinates will ALWAYS produce the same
     * tree type at the same location across all game sessions and multiplayer clients. This is
     * critical for multiplayer synchronization.</p>
     * 
     * <p><b>Order of Operations (CRITICAL for determinism):</b></p>
     * <ol>
     *   <li>Set deterministic random seed based on worldSeed + position</li>
     *   <li>Check spawn probability (2%) using seeded random</li>
     *   <li>Validate spawn location (distance checks, view checks)</li>
     *   <li>Query biome type (deterministic, based on coordinates only)</li>
     *   <li>Select tree type using seeded random + biome filtering</li>
     * </ol>
     * 
     * <p><b>Randomness Sources:</b></p>
     * <ul>
     *   <li>Spawn probability: {@code random.nextFloat() < 0.005f} (seeded)</li>
     *   <li>Tree type selection: {@code random.nextFloat()} (seeded)</li>
     *   <li>Biome query: {@code biomeManager.getBiomeAtPosition(x, y)} (deterministic, NO randomness)</li>
     * </ul>
     * 
     * <p><b>Multiplayer Synchronization:</b></p>
     * <p>All clients use the same worldSeed value synchronized from the server. Combined with
     * deterministic biome queries and seeded random generation, this ensures all clients see
     * identical trees at identical positions without requiring per-tree network synchronization.</p>
     * 
     * @param x The x-coordinate in world space (must be aligned to 64px grid)
     * @param y The y-coordinate in world space (must be aligned to 64px grid)
     */
    private void generateTreeAt(int x, int y) {
        // In multiplayer mode, use deterministic generation based on world seed
        // This ensures all clients generate the same trees at the same positions
        // without needing server synchronization for every tree
        
        String key = x + "," + y;
        if (!trees.containsKey(key) && !appleTrees.containsKey(key) && !coconutTrees.containsKey(key) && !bambooTrees.containsKey(key) && !bananaTrees.containsKey(key) && !clearedPositions.containsKey(key)) {
            // STEP 1: Set deterministic random seed
            // Combines world seed with position using prime number multipliers to ensure unique,
            // reproducible seeds for each coordinate pair across all clients
            random.setSeed(worldSeed + x * 31L + y * 17L);
            
            // STEP 2: Check spawn probability (2% chance)
            // Uses the seeded random to ensure same coordinates always get same spawn decision
            if (random.nextFloat() < 0.005f) {
                // STEP 3: Add random offset to break grid pattern (±32px in each direction)
                // Try multiple times to find a position without overlapping trees
                float treeX = 0, treeY = 0;
                boolean validPosition = false;
                int maxAttempts = 5;
                
                for (int attempt = 0; attempt < maxAttempts; attempt++) {
                    float offsetX = (random.nextFloat() - 0.5f) * 64; // -32 to +32
                    float offsetY = (random.nextFloat() - 0.5f) * 64; // -32 to +32
                    treeX = x + offsetX;
                    treeY = y + offsetY;
                    
                    // Check biome type first to determine minimum distance
                    wagemaker.uk.biome.BiomeType biome = biomeManager.getBiomeAtPosition(treeX, treeY);
                    float minDistance = (biome == wagemaker.uk.biome.BiomeType.SAND) ? 50f : 192f;
                    
                    // Check if any tree is too close (192px for grass, 50px for sand)
                    if (!isTreeTooClose(treeX, treeY, minDistance)) {
                        validPosition = true;
                        break;
                    }
                }
                
                // If no valid position found after attempts, skip this tree
                if (!validPosition) {
                    return;
                }
                
                // STEP 4: Validate spawn location (deterministic checks only)
                // Don't spawn trees too close to spawn point (within 200px)
                float distanceFromSpawn = (float) Math.sqrt(treeX * treeX + treeY * treeY);
                if (distanceFromSpawn < 200) {
                    return;
                }
                
                // STEP 5: Query biome type again for tree type selection
                // BiomeManager returns consistent biome types based solely on world coordinates
                wagemaker.uk.biome.BiomeType biome = biomeManager.getBiomeAtPosition(treeX, treeY);
                
                // STEP 6: Generate tree based on biome type
                // Tree type selection uses the SAME seeded random instance from Step 1
                if (biome == wagemaker.uk.biome.BiomeType.SAND) {
                    // Sand biomes: bamboo trees with 30% spawn rate (reduced by 70%)
                    if (random.nextFloat() < 0.3f) {
                        bambooTrees.put(key, new BambooTree(treeX, treeY));
                    }
                } else {
                    // Grass biomes: adjusted tree type distribution
                    // SmallTree: 42.5% (increased by 30% from 32.5%)
                    // AppleTree: 12.5% (reduced by 50% from 25%)
                    // CoconutTree: 32.5% (unchanged)
                    // BananaTree: 12.5% (reduced by 50% from 25%)
                    float treeType = random.nextFloat();
                    if (treeType < 0.425f) {
                        trees.put(key, new SmallTree(treeX, treeY));
                    } else if (treeType < 0.55f) {
                        appleTrees.put(key, new AppleTree(treeX, treeY));
                    } else if (treeType < 0.875f) {
                        coconutTrees.put(key, new CoconutTree(treeX, treeY));
                    } else {
                        bananaTrees.put(key, new BananaTree(treeX, treeY));
                    }
                }
            }
        }
    }
    
    private boolean isTreeNearby(int x, int y, int minDistance) {
        for (SmallTree tree : trees.values()) {
            float dx = tree.getX() - x;
            float dy = tree.getY() - y;
            if (Math.sqrt(dx * dx + dy * dy) < minDistance) {
                return true;
            }
        }
        for (AppleTree tree : appleTrees.values()) {
            float dx = tree.getX() - x;
            float dy = tree.getY() - y;
            if (Math.sqrt(dx * dx + dy * dy) < minDistance) {
                return true;
            }
        }
        for (CoconutTree tree : coconutTrees.values()) {
            float dx = tree.getX() - x;
            float dy = tree.getY() - y;
            if (Math.sqrt(dx * dx + dy * dy) < minDistance) {
                return true;
            }
        }
        for (BambooTree tree : bambooTrees.values()) {
            float dx = tree.getX() - x;
            float dy = tree.getY() - y;
            if (Math.sqrt(dx * dx + dy * dy) < minDistance) {
                return true;
            }
        }
        for (BananaTree tree : bananaTrees.values()) {
            float dx = tree.getX() - x;
            float dy = tree.getY() - y;
            if (Math.sqrt(dx * dx + dy * dy) < minDistance) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if a tree position is too close to any existing tree.
     * Uses float coordinates for precise overlap detection.
     * 
     * @param x The x-coordinate to check
     * @param y The y-coordinate to check
     * @param minDistance Minimum distance required between trees
     * @return true if too close to another tree, false otherwise
     */
    private boolean isTreeTooClose(float x, float y, float minDistance) {
        for (SmallTree tree : trees.values()) {
            float dx = tree.getX() - x;
            float dy = tree.getY() - y;
            if (Math.sqrt(dx * dx + dy * dy) < minDistance) {
                return true;
            }
        }
        for (AppleTree tree : appleTrees.values()) {
            float dx = tree.getX() - x;
            float dy = tree.getY() - y;
            if (Math.sqrt(dx * dx + dy * dy) < minDistance) {
                return true;
            }
        }
        for (CoconutTree tree : coconutTrees.values()) {
            float dx = tree.getX() - x;
            float dy = tree.getY() - y;
            if (Math.sqrt(dx * dx + dy * dy) < minDistance) {
                return true;
            }
        }
        for (BambooTree tree : bambooTrees.values()) {
            float dx = tree.getX() - x;
            float dy = tree.getY() - y;
            if (Math.sqrt(dx * dx + dy * dy) < minDistance) {
                return true;
            }
        }
        for (BananaTree tree : bananaTrees.values()) {
            float dx = tree.getX() - x;
            float dy = tree.getY() - y;
            if (Math.sqrt(dx * dx + dy * dy) < minDistance) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Generates a stone at the specified world coordinates using deterministic procedural generation.
     * Stones spawn only on sand biomes with approximately 1 stone per 500x500 pixels.
     * Ensures minimum 100 pixel distance from trees to avoid overlap.
     * 
     * @param x The x-coordinate in world space (aligned to 64px grid)
     * @param y The y-coordinate in world space (aligned to 64px grid)
     */
    private void generateStoneAt(int x, int y) {
        String key = x + "," + y;
        
        // Don't spawn stones where they already exist or where positions are cleared
        if (stones.containsKey(key) || stoneMap.containsKey(key) || clearedPositions.containsKey(key)) {
            return;
        }
        
        // Set deterministic random seed based on world seed and position
        random.setSeed(worldSeed + x * 37L + y * 23L);
        
        // Stone spawn probability: consistent for both singleplayer and multiplayer
        // Single-player: 0.001 (0.1%), Multiplayer: 0.005 (0.5%)
        float spawnRate = 0.001f;
        if (random.nextFloat() < spawnRate) {
            // Add random offset to break grid pattern
            float offsetX = (random.nextFloat() - 0.5f) * 64; // -32 to +32
            float offsetY = (random.nextFloat() - 0.5f) * 64; // -32 to +32
            float stoneX = x + offsetX;
            float stoneY = y + offsetY;
            
            // Only spawn stones on sand biomes
            wagemaker.uk.biome.BiomeType biome = biomeManager.getBiomeAtPosition(stoneX, stoneY);
            if (biome != wagemaker.uk.biome.BiomeType.SAND) {
                return;
            }
            
            // Don't spawn stones too close to spawn point (within 200px)
            float distanceFromSpawn = (float) Math.sqrt(stoneX * stoneX + stoneY * stoneY);
            if (distanceFromSpawn < 200) {
                return;
            }
            
            // Don't spawn stones near player (512px minimum distance)
            float distFromPlayer = (float) Math.sqrt((stoneX - player.getX()) * (stoneX - player.getX()) + (stoneY - player.getY()) * (stoneY - player.getY()));
            if (distFromPlayer < 512) {
                return;
            }
            
            // Ensure minimum 100 pixel distance from trees
            if (isTreeTooClose(stoneX, stoneY, 100f)) {
                return;
            }
            
            // Check if any existing stone is too close (minimum 100 pixels)
            for (Stone existingStone : stones.values()) {
                float dx = existingStone.getX() - stoneX;
                float dy = existingStone.getY() - stoneY;
                if (Math.sqrt(dx * dx + dy * dy) < 100) {
                    return;
                }
            }
            for (Stone existingStone : stoneMap.values()) {
                float dx = existingStone.getX() - stoneX;
                float dy = existingStone.getY() - stoneY;
                if (Math.sqrt(dx * dx + dy * dy) < 100) {
                    return;
                }
            }
            
            // Create stone at the calculated position
            Stone stone = new Stone(stoneX, stoneY);
            stones.put(key, stone);
            stoneMap.put(key, stone);
        }
    }
    
    private boolean isWithinPlayerView(int x, int y) {
        // Get player position and camera view dimensions
        float playerX = player.getX();
        float playerY = player.getY();
        float viewWidth = viewport.getWorldWidth();
        float viewHeight = viewport.getWorldHeight();
        
        // Calculate camera center (follows player)
        float camCenterX = playerX + 32; // player center
        float camCenterY = playerY + 32; // player center
        
        // Calculate view boundaries with some buffer to prevent trees appearing at screen edges
        float buffer = 128; // Extra buffer beyond visible area
        float leftBound = camCenterX - (viewWidth / 2) - buffer;
        float rightBound = camCenterX + (viewWidth / 2) + buffer;
        float bottomBound = camCenterY - (viewHeight / 2) - buffer;
        float topBound = camCenterY + (viewHeight / 2) + buffer;
        
        // Check if tree position is within the buffered view area
        return (x >= leftBound && x <= rightBound && y >= bottomBound && y <= topBound);
    }
    
    private void drawTrees() {
        float camX = camera.position.x;
        float camY = camera.position.y;
        float viewWidth = viewport.getWorldWidth();
        float viewHeight = viewport.getWorldHeight();
        
        for (SmallTree tree : trees.values()) {
            // only draw trees near camera
            if (Math.abs(tree.getX() - camX) < viewWidth && 
                Math.abs(tree.getY() - camY) < viewHeight) {
                batch.draw(tree.getTexture(), tree.getX(), tree.getY());
            }
        }
    }
    
    private void drawAppleTrees() {
        float camX = camera.position.x;
        float camY = camera.position.y;
        float viewWidth = viewport.getWorldWidth();
        float viewHeight = viewport.getWorldHeight();
        
        for (AppleTree appleTree : appleTrees.values()) {
            // only draw apple trees near camera
            if (Math.abs(appleTree.getX() - camX) < viewWidth && 
                Math.abs(appleTree.getY() - camY) < viewHeight) {
                batch.draw(appleTree.getTexture(), appleTree.getX(), appleTree.getY());
            }
        }
    }
    
    private void drawCoconutTrees() {
        float camX = camera.position.x;
        float camY = camera.position.y;
        float viewWidth = viewport.getWorldWidth();
        float viewHeight = viewport.getWorldHeight();
        
        for (CoconutTree coconutTree : coconutTrees.values()) {
            // only draw coconut trees near camera
            if (Math.abs(coconutTree.getX() - camX) < viewWidth && 
                Math.abs(coconutTree.getY() - camY) < viewHeight) {
                batch.draw(coconutTree.getTexture(), coconutTree.getX(), coconutTree.getY());
            }
        }
    }
    
    private void drawBambooTrees() {
        float camX = camera.position.x;
        float camY = camera.position.y;
        float viewWidth = viewport.getWorldWidth();
        float viewHeight = viewport.getWorldHeight();
        
        for (BambooTree bambooTree : bambooTrees.values()) {
            // only draw bamboo trees near camera
            if (Math.abs(bambooTree.getX() - camX) < viewWidth && 
                Math.abs(bambooTree.getY() - camY) < viewHeight) {
                batch.draw(bambooTree.getTexture(), bambooTree.getX(), bambooTree.getY());
            }
        }
    }
    
    private void drawBananaTrees() {
        float camX = camera.position.x;
        float camY = camera.position.y;
        float viewWidth = viewport.getWorldWidth();
        float viewHeight = viewport.getWorldHeight();
        
        for (BananaTree bananaTree : bananaTrees.values()) {
            // only draw banana trees near camera
            if (Math.abs(bananaTree.getX() - camX) < viewWidth && 
                Math.abs(bananaTree.getY() - camY) < viewHeight) {
                batch.draw(bananaTree.getTexture(), bananaTree.getX(), bananaTree.getY());
            }
        }
    }
    
    private void drawApples() {
        float camX = camera.position.x;
        float camY = camera.position.y;
        float viewWidth = viewport.getWorldWidth();
        float viewHeight = viewport.getWorldHeight();
        
        for (Apple apple : apples.values()) {
            // only draw apples near camera
            if (Math.abs(apple.getX() - camX) < viewWidth && 
                Math.abs(apple.getY() - camY) < viewHeight) {
                batch.draw(apple.getTexture(), apple.getX(), apple.getY(), 24, 24);
            }
        }
    }
    
    private void drawBananas() {
        float camX = camera.position.x;
        float camY = camera.position.y;
        float viewWidth = viewport.getWorldWidth();
        float viewHeight = viewport.getWorldHeight();
        
        for (Banana banana : bananas.values()) {
            // only draw bananas near camera
            if (Math.abs(banana.getX() - camX) < viewWidth && 
                Math.abs(banana.getY() - camY) < viewHeight) {
                batch.draw(banana.getTexture(), banana.getX(), banana.getY(), 32, 32);
            }
        }
    }
    
    private void drawBambooStacks() {
        float camX = camera.position.x;
        float camY = camera.position.y;
        float viewWidth = viewport.getWorldWidth() / 2;
        float viewHeight = viewport.getWorldHeight() / 2;
        
        for (BambooStack bambooStack : bambooStacks.values()) {
            if (Math.abs(bambooStack.getX() - camX) < viewWidth && 
                Math.abs(bambooStack.getY() - camY) < viewHeight) {
                batch.draw(bambooStack.getTexture(), bambooStack.getX(), bambooStack.getY(), 32, 32);
            }
        }
    }
    
    private void drawBabyBamboos() {
        float camX = camera.position.x;
        float camY = camera.position.y;
        float viewWidth = viewport.getWorldWidth() / 2;
        float viewHeight = viewport.getWorldHeight() / 2;
        
        for (BabyBamboo babyBamboo : babyBamboos.values()) {
            if (Math.abs(babyBamboo.getX() - camX) < viewWidth && 
                Math.abs(babyBamboo.getY() - camY) < viewHeight) {
                batch.draw(babyBamboo.getTexture(), babyBamboo.getX(), babyBamboo.getY(), 32, 32);
            }
        }
    }
    
    private void drawBabyTrees() {
        float camX = camera.position.x;
        float camY = camera.position.y;
        float viewWidth = viewport.getWorldWidth() / 2;
        float viewHeight = viewport.getWorldHeight() / 2;
        
        for (BabyTree babyTree : babyTrees.values()) {
            if (Math.abs(babyTree.getX() - camX) < viewWidth && 
                Math.abs(babyTree.getY() - camY) < viewHeight) {
                batch.draw(babyTree.getTexture(), babyTree.getX(), babyTree.getY(), 32, 32);
            }
        }
    }
    
    private void drawWoodStacks() {
        float camX = camera.position.x;
        float camY = camera.position.y;
        float viewWidth = viewport.getWorldWidth() / 2;
        float viewHeight = viewport.getWorldHeight() / 2;
        
        for (WoodStack woodStack : woodStacks.values()) {
            if (Math.abs(woodStack.getX() - camX) < viewWidth && 
                Math.abs(woodStack.getY() - camY) < viewHeight) {
                batch.draw(woodStack.getTexture(), woodStack.getX(), woodStack.getY(), 32, 32);
            }
        }
    }
    
    private void drawPlantedBamboos() {
        float camX = camera.position.x;
        float camY = camera.position.y;
        float viewWidth = viewport.getWorldWidth() / 2;
        float viewHeight = viewport.getWorldHeight() / 2;
        
        for (PlantedBamboo planted : plantedBamboos.values()) {
            if (Math.abs(planted.getX() - camX) < viewWidth && 
                Math.abs(planted.getY() - camY) < viewHeight) {
                Texture texture = planted.getTexture();
                if (texture != null) {
                    batch.draw(texture, planted.getX(), planted.getY(), 64, 64);
                } else {
                    System.err.println("[RENDER] PlantedBamboo at (" + planted.getX() + ", " + planted.getY() + ") has null texture!");
                }
            }
        }
    }
    
    private void drawPlantedTrees() {
        float camX = camera.position.x;
        float camY = camera.position.y;
        float viewWidth = viewport.getWorldWidth() / 2;
        float viewHeight = viewport.getWorldHeight() / 2;
        
        for (PlantedTree planted : plantedTrees.values()) {
            if (Math.abs(planted.getX() - camX) < viewWidth && 
                Math.abs(planted.getY() - camY) < viewHeight) {
                Texture texture = planted.getTexture();
                if (texture != null) {
                    batch.draw(texture, planted.getX(), planted.getY(), 64, 64);
                } else {
                    System.err.println("[RENDER] PlantedTree at (" + planted.getX() + ", " + planted.getY() + ") has null texture!");
                }
            }
        }
    }
    
    private void drawCactus() {
        if (cactus != null) {
            float camX = camera.position.x;
            float camY = camera.position.y;
            float viewWidth = viewport.getWorldWidth();
            float viewHeight = viewport.getWorldHeight();
            
            // only draw cactus if near camera
            if (Math.abs(cactus.getX() - camX) < viewWidth && 
                Math.abs(cactus.getY() - camY) < viewHeight) {
                batch.draw(cactus.getTexture(), cactus.getX(), cactus.getY());
            }
        }
    }
    
    private void drawStones() {
        float camX = camera.position.x;
        float camY = camera.position.y;
        float viewWidth = viewport.getWorldWidth();
        float viewHeight = viewport.getWorldHeight();
        
        for (Stone stone : stones.values()) {
            // only draw stones near camera
            if (Math.abs(stone.getX() - camX) < viewWidth && 
                Math.abs(stone.getY() - camY) < viewHeight) {
                batch.draw(stone.getTexture(), stone.getX(), stone.getY(), 64, 64);
            }
        }
    }
    
    private void drawPebbles() {
        float camX = camera.position.x;
        float camY = camera.position.y;
        float viewWidth = viewport.getWorldWidth() / 2;
        float viewHeight = viewport.getWorldHeight() / 2;
        
        for (Pebble pebble : pebbles.values()) {
            if (Math.abs(pebble.getX() - camX) < viewWidth && 
                Math.abs(pebble.getY() - camY) < viewHeight) {
                batch.draw(pebble.getTexture(), pebble.getX(), pebble.getY(), 32, 32);
            }
        }
    }
    
    private void drawPalmFibers() {
        float camX = camera.position.x;
        float camY = camera.position.y;
        float viewWidth = viewport.getWorldWidth() / 2;
        float viewHeight = viewport.getWorldHeight() / 2;
        
        for (PalmFiber palmFiber : palmFibers.values()) {
            if (Math.abs(palmFiber.getX() - camX) < viewWidth && 
                Math.abs(palmFiber.getY() - camY) < viewHeight) {
                batch.draw(palmFiber.getTexture(), palmFiber.getX(), palmFiber.getY(), 32, 32);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    private void drawHealthBars() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Draw remote player health bars in multiplayer mode (filled shapes)
        if (gameMode != GameMode.SINGLEPLAYER) {
            float camX = camera.position.x;
            float camY = camera.position.y;
            float viewWidth = viewport.getWorldWidth();
            float viewHeight = viewport.getWorldHeight();
            
            for (RemotePlayer remotePlayer : remotePlayers.values()) {
                if (Math.abs(remotePlayer.getX() - camX) < viewWidth && 
                    Math.abs(remotePlayer.getY() - camY) < viewHeight) {
                    remotePlayer.renderHealthBar(shapeRenderer);
                }
            }
        }
        
        // Draw small tree health bars
        for (SmallTree tree : trees.values()) {
            if (tree.shouldShowHealthBar()) {
                float barWidth = 32;
                float barHeight = 4;
                float barX = tree.getX() + 16;
                float barY = tree.getY() + 134;
                
                // Green background
                shapeRenderer.setColor(0, 1, 0, 1);
                shapeRenderer.rect(barX, barY, barWidth, barHeight);
                
                // Red overlay based on damage
                float damagePercent = 1.0f - tree.getHealthPercentage();
                shapeRenderer.setColor(1, 0, 0, 1);
                shapeRenderer.rect(barX, barY, barWidth * damagePercent, barHeight);
            }
        }
        
        // Draw apple tree health bars
        for (AppleTree appleTree : appleTrees.values()) {
            if (appleTree.shouldShowHealthBar()) {
                float barWidth = 64; // half of apple tree width
                float barHeight = 6;
                float barX = appleTree.getX() + 32; // center above tree
                float barY = appleTree.getY() + 134; // above tree
                
                // Green background
                shapeRenderer.setColor(0, 1, 0, 1);
                shapeRenderer.rect(barX, barY, barWidth, barHeight);
                
                // Red overlay based on damage
                float damagePercent = 1.0f - appleTree.getHealthPercentage();
                shapeRenderer.setColor(1, 0, 0, 1);
                shapeRenderer.rect(barX, barY, barWidth * damagePercent, barHeight);
            }
        }
        
        // Draw coconut tree health bars
        for (CoconutTree coconutTree : coconutTrees.values()) {
            if (coconutTree.shouldShowHealthBar()) {
                float barWidth = 64;
                float barHeight = 6;
                float barX = coconutTree.getX() + 32;
                float barY = coconutTree.getY() + 134;
                
                // Green background
                shapeRenderer.setColor(0, 1, 0, 1);
                shapeRenderer.rect(barX, barY, barWidth, barHeight);
                
                // Red overlay based on damage
                float damagePercent = 1.0f - coconutTree.getHealthPercentage();
                shapeRenderer.setColor(1, 0, 0, 1);
                shapeRenderer.rect(barX, barY, barWidth * damagePercent, barHeight);
            }
        }
        
        // Draw bamboo tree health bars
        for (BambooTree bambooTree : bambooTrees.values()) {
            if (bambooTree.shouldShowHealthBar()) {
                float barWidth = 32;
                float barHeight = 4;
                float barX = bambooTree.getX() + 16;
                float barY = bambooTree.getY() + 134;
                
                // Green background
                shapeRenderer.setColor(0, 1, 0, 1);
                shapeRenderer.rect(barX, barY, barWidth, barHeight);
                
                // Red overlay based on damage
                float damagePercent = 1.0f - bambooTree.getHealthPercentage();
                shapeRenderer.setColor(1, 0, 0, 1);
                shapeRenderer.rect(barX, barY, barWidth * damagePercent, barHeight);
            }
        }
        
        // Draw banana tree health bars
        for (BananaTree bananaTree : bananaTrees.values()) {
            if (bananaTree.shouldShowHealthBar()) {
                float barWidth = 64;
                float barHeight = 6;
                float barX = bananaTree.getX() + 32;
                float barY = bananaTree.getY() + 134;
                
                // Green background
                shapeRenderer.setColor(0, 1, 0, 1);
                shapeRenderer.rect(barX, barY, barWidth, barHeight);
                
                // Red overlay based on damage
                float damagePercent = 1.0f - bananaTree.getHealthPercentage();
                shapeRenderer.setColor(1, 0, 0, 1);
                shapeRenderer.rect(barX, barY, barWidth * damagePercent, barHeight);
            }
        }
        
        // Draw cactus health bar
        if (cactus != null && cactus.shouldShowHealthBar()) {
            float barWidth = 32;
            float barHeight = 4;
            float barX = cactus.getX() + 16;
            float barY = cactus.getY() + 134;
            
            // Green background
            shapeRenderer.setColor(0, 1, 0, 1);
            shapeRenderer.rect(barX, barY, barWidth, barHeight);
            
            // Red overlay based on damage
            float damagePercent = 1.0f - cactus.getHealthPercentage();
            shapeRenderer.setColor(1, 0, 0, 1);
            shapeRenderer.rect(barX, barY, barWidth * damagePercent, barHeight);
        }
        
        // Draw stone health bars
        for (Stone stone : stones.values()) {
            if (stone.shouldShowHealthBar()) {
                float barWidth = 32;
                float barHeight = 4;
                float barX = stone.getX() + 16;
                float barY = stone.getY() + 70;  // Position above stone sprite
                
                // Green background
                shapeRenderer.setColor(0, 1, 0, 1);
                shapeRenderer.rect(barX, barY, barWidth, barHeight);
                
                // Red overlay based on damage
                float damagePercent = 1.0f - stone.getHealthPercentage();
                shapeRenderer.setColor(1, 0, 0, 1);
                shapeRenderer.rect(barX, barY, barWidth * damagePercent, barHeight);
            }
        }
        
        shapeRenderer.end();
        
        // Draw borders for remote player health bars
        if (gameMode != GameMode.SINGLEPLAYER) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            float camX = camera.position.x;
            float camY = camera.position.y;
            float viewWidth = viewport.getWorldWidth();
            float viewHeight = viewport.getWorldHeight();
            
            for (RemotePlayer remotePlayer : remotePlayers.values()) {
                if (Math.abs(remotePlayer.getX() - camX) < viewWidth && 
                    Math.abs(remotePlayer.getY() - camY) < viewHeight) {
                    remotePlayer.renderHealthBarBorder(shapeRenderer);
                }
            }
            shapeRenderer.end();
        }
    }

    public void spawnNewCactus() {
        if (cactus != null) {
            cactus.dispose();
        }
        
        // Generate random position away from player
        float playerX = player.getX();
        float playerY = player.getY();
        float newX, newY;
        
        // Keep trying until we find a position far enough from player
        do {
            // Random position within a large area around the player
            newX = playerX + (random.nextFloat() - 0.5f) * 2000; // ±1000px from player
            newY = playerY + (random.nextFloat() - 0.5f) * 2000; // ±1000px from player
            
            float distance = (float)Math.sqrt((newX - playerX) * (newX - playerX) + (newY - playerY) * (newY - playerY));
            
            // Ensure cactus spawns at least 400px away from player
            if (distance >= 400) {
                break;
            }
        } while (true);
        
        // Create new cactus at the random position
        cactus = new Cactus(newX, newY);
        player.setCactus(cactus);
        
        System.out.println("New cactus spawned at: " + newX + ", " + newY + " (distance from player: " + 
                          Math.sqrt((newX - playerX) * (newX - playerX) + (newY - playerY) * (newY - playerY)) + ")");
    }

    /**
     * Renders all remote players in multiplayer mode.
     * This method draws remote player sprites and their name tags.
     */
    private void renderRemotePlayers() {
        if (gameMode == GameMode.SINGLEPLAYER || remotePlayers.isEmpty()) {
            return;
        }
        
        float camX = camera.position.x;
        float camY = camera.position.y;
        float viewWidth = viewport.getWorldWidth();
        float viewHeight = viewport.getWorldHeight();
        
        // Only render remote players near the camera
        for (RemotePlayer remotePlayer : remotePlayers.values()) {
            if (Math.abs(remotePlayer.getX() - camX) < viewWidth && 
                Math.abs(remotePlayer.getY() - camY) < viewHeight) {
                remotePlayer.render(batch);
            }
        }
    }
    
    /**
     * Renders name tags for all remote players.
     * This is called after the main batch rendering.
     */
    private void renderRemotePlayerNameTags() {
        if (gameMode == GameMode.SINGLEPLAYER || remotePlayers.isEmpty()) {
            return;
        }
        
        float camX = camera.position.x;
        float camY = camera.position.y;
        float viewWidth = viewport.getWorldWidth();
        float viewHeight = viewport.getWorldHeight();
        
        batch.begin();
        for (RemotePlayer remotePlayer : remotePlayers.values()) {
            if (Math.abs(remotePlayer.getX() - camX) < viewWidth && 
                Math.abs(remotePlayer.getY() - camY) < viewHeight) {
                remotePlayer.renderNameTag(batch, gameMenu.getFont());
            }
        }
        batch.end();
    }
    
    /**
     * Starts a multiplayer server and connects to it as the host player.
     * This method launches a GameServer in the background and then connects
     * the local client to it.
     * 
     * @throws Exception if server startup or connection fails
     */
    public void startMultiplayerHost() throws Exception {
        if (gameMode != GameMode.SINGLEPLAYER) {
            throw new IllegalStateException("Already in multiplayer mode");
        }
        
        System.out.println("Starting multiplayer host...");
        
        try {
            // Save current singleplayer position before switching modes
            if (gameMenu != null) {
                gameMenu.savePlayerPosition();
            }
            
            // Clear local world before starting multiplayer
            clearLocalWorld();
            
            // Create and start the game server
            gameServer = new GameServer();
            gameServer.start();
            
            // Wait a moment for server to fully initialize
            Thread.sleep(100);
            
            // Connect to our own server as a client
            gameClient = new GameClient();
            gameClient.setMessageHandler(new GameMessageHandler(this));
            gameClient.connect("localhost", 25565);
            
            // Set game mode to host
            gameMode = GameMode.MULTIPLAYER_HOST;
            
            // Switch inventory to multiplayer mode
            if (inventoryManager != null) {
                inventoryManager.setMultiplayerMode(true);
                inventoryManager.setGameClient(gameClient);
            }
            
            // Update connection quality indicator
            connectionQualityIndicator.setGameClient(gameClient);
            
            // Set the player's game client reference for sending updates
            player.setGameClient(gameClient);
            
            // Load multiplayer position (or use spawn if none exists)
            if (gameMenu != null) {
                gameMenu.loadPlayerPosition();
            } else {
                // Fallback to spawn point if no menu
                player.setPosition(0, 0);
            }
            
            // Apply the server's world seed to the host client
            this.worldSeed = gameServer.getWorldState().getWorldSeed();
            
            System.out.println("Multiplayer host started successfully");
            System.out.println("Server IP: " + gameServer.getPublicIPv4());
            System.out.println("World seed: " + this.worldSeed);
            
        } catch (Exception e) {
            // Clean up on failure
            if (gameClient != null) {
                try {
                    gameClient.disconnect();
                } catch (Exception ex) {
                    // Ignore cleanup errors
                }
                gameClient = null;
            }
            
            if (gameServer != null) {
                try {
                    gameServer.stop();
                } catch (Exception ex) {
                    // Ignore cleanup errors
                }
                gameServer = null;
            }
            
            // Log the error
            System.err.println("Failed to start multiplayer host: " + e.getMessage());
            e.printStackTrace();
            
            // Re-throw to be handled by caller
            throw new Exception("Failed to start server: " + e.getMessage(), e);
        }
    }
    
    /**
     * Connects to a multiplayer server as a client.
     * 
     * @param serverAddress The IP address or hostname of the server
     * @param port The server port
     * @throws Exception if connection fails
     */
    public void joinMultiplayerServer(String serverAddress, int port) throws Exception {
        if (gameMode != GameMode.SINGLEPLAYER) {
            throw new IllegalStateException("Already in multiplayer mode");
        }
        
        System.out.println("Connecting to server at " + serverAddress + ":" + port);
        
        try {
            // Save current singleplayer position before switching modes
            if (gameMenu != null) {
                gameMenu.savePlayerPosition();
            }
            
            // Clear local world before joining multiplayer
            clearLocalWorld();
            
            // Create and connect the game client
            gameClient = new GameClient();
            gameClient.setMessageHandler(new GameMessageHandler(this));
            gameClient.connect(serverAddress, port);
            
            // Set game mode to client
            gameMode = GameMode.MULTIPLAYER_CLIENT;
            
            // Switch inventory to multiplayer mode
            if (inventoryManager != null) {
                inventoryManager.setMultiplayerMode(true);
                inventoryManager.setGameClient(gameClient);
            }
            
            // Update connection quality indicator
            connectionQualityIndicator.setGameClient(gameClient);
            
            // Set the player's game client reference for sending updates
            player.setGameClient(gameClient);
            
            // Load multiplayer position (or use spawn if none exists)
            if (gameMenu != null) {
                gameMenu.loadPlayerPosition();
            } else {
                // Fallback to spawn point if no menu
                player.setPosition(0, 0);
            }
            
            System.out.println("Connected to multiplayer server successfully");
            
        } catch (Exception e) {
            // Clean up on failure
            if (gameClient != null) {
                try {
                    gameClient.disconnect();
                } catch (Exception ex) {
                    // Ignore cleanup errors
                }
                gameClient = null;
            }
            
            // Log the error
            System.err.println("Failed to connect to server: " + e.getMessage());
            e.printStackTrace();
            
            // Re-throw to be handled by caller
            throw new Exception("Connection failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Synchronizes the local game state with the server's world state.
     * This is called when receiving a world state update from the server.
     * 
     * @param state The world state from the server
     */
    public void syncWorldState(WorldState state) {
        if (state == null) {
            return;
        }
        
        System.out.println("Synchronizing world state...");
        System.out.println("  World seed: " + (state.getWorldSeed() != 0 ? state.getWorldSeed() : "not set"));
        System.out.println("  Trees to sync: " + (state.getTrees() != null ? state.getTrees().size() : 0));
        System.out.println("  Players to sync: " + (state.getPlayers() != null ? state.getPlayers().size() : 0));
        System.out.println("  Items to sync: " + (state.getItems() != null ? state.getItems().size() : 0));
        
        // Apply world seed for deterministic world generation
        if (state.getWorldSeed() != 0) {
            this.worldSeed = state.getWorldSeed();
            System.out.println("Applied world seed: " + state.getWorldSeed());
        }
        
        // Sync cleared positions
        if (state.getClearedPositions() != null) {
            clearedPositions.clear();
            for (String position : state.getClearedPositions()) {
                clearedPositions.put(position, true);
            }
        }
        
        // Sync player states (create remote players for existing players)
        if (state.getPlayers() != null) {
            for (wagemaker.uk.network.PlayerState playerState : state.getPlayers().values()) {
                // Don't create a remote player for ourselves
                if (gameClient != null && playerState.getPlayerId().equals(gameClient.getClientId())) {
                    continue;
                }
                
                // Create PlayerJoinMessage and queue it for main thread processing
                wagemaker.uk.network.PlayerJoinMessage joinMessage = new wagemaker.uk.network.PlayerJoinMessage(
                    playerState.getPlayerId(),
                    playerState.getPlayerName(),
                    playerState.getX(),
                    playerState.getY()
                );
                queuePlayerJoin(joinMessage);
            }
        }
        
        // Sync tree states and remove ghost trees
        if (state.getTrees() != null) {
            // First, remove any local trees that don't exist on the server (ghost trees)
            removeGhostTrees(state.getTrees());
            
            // Then sync the server's trees
            for (TreeState treeState : state.getTrees().values()) {
                updateTreeFromState(treeState);
            }
        }
        
        // Sync stone states
        if (state.getStones() != null) {
            System.out.println("  Stones to sync: " + state.getStones().size());
            for (StoneState stoneState : state.getStones().values()) {
                updateStoneFromState(stoneState);
            }
        }
        
        // Sync item states
        if (state.getItems() != null) {
            for (ItemState itemState : state.getItems().values()) {
                updateItemFromState(itemState);
            }
        }
        
        System.out.println("World state synchronized");
        System.out.println("  Local trees after sync: " + getTotalTreeCount());
    }
    
    /**
     * Removes ghost trees - trees that exist locally but not on the server.
     * This prevents desync issues where clients see trees that don't exist in the server's world state.
     * Queues removals to be processed on the main thread to avoid OpenGL context issues.
     * 
     * @param serverTrees Map of tree IDs to TreeState from the server
     */
    private void removeGhostTrees(Map<String, TreeState> serverTrees) {
        int queuedCount = 0;
        
        // Check small trees
        for (String treeId : trees.keySet()) {
            if (!serverTrees.containsKey(treeId)) {
                pendingTreeRemovals.offer(treeId);
                queuedCount++;
                System.out.println("Queued ghost small tree for removal: " + treeId);
            }
        }
        
        // Check apple trees
        for (String treeId : appleTrees.keySet()) {
            if (!serverTrees.containsKey(treeId)) {
                pendingTreeRemovals.offer(treeId);
                queuedCount++;
                System.out.println("Queued ghost apple tree for removal: " + treeId);
            }
        }
        
        // Check coconut trees
        for (String treeId : coconutTrees.keySet()) {
            if (!serverTrees.containsKey(treeId)) {
                pendingTreeRemovals.offer(treeId);
                queuedCount++;
                System.out.println("Queued ghost coconut tree for removal: " + treeId);
            }
        }
        
        // Check bamboo trees
        for (String treeId : bambooTrees.keySet()) {
            if (!serverTrees.containsKey(treeId)) {
                pendingTreeRemovals.offer(treeId);
                queuedCount++;
                System.out.println("Queued ghost bamboo tree for removal: " + treeId);
            }
        }
        
        // Check banana trees
        for (String treeId : bananaTrees.keySet()) {
            if (!serverTrees.containsKey(treeId)) {
                pendingTreeRemovals.offer(treeId);
                queuedCount++;
                System.out.println("Queued ghost banana tree for removal: " + treeId);
            }
        }
        
        if (queuedCount > 0) {
            System.out.println("Total ghost trees queued for removal: " + queuedCount);
        }
    }
    
    /**
     * Helper method to get the total count of all trees across all tree maps.
     * Used for diagnostic logging and monitoring.
     * 
     * @return The total number of trees in all tree collections
     */
    private int getTotalTreeCount() {
        return trees.size() + appleTrees.size() + coconutTrees.size() + 
               bambooTrees.size() + bananaTrees.size();
    }
    
    /**
     * Clears all locally generated trees and items.
     * Called when transitioning to multiplayer mode to ensure the client
     * starts with a clean slate before receiving the server's authoritative world state.
     * 
     * This prevents ghost trees (trees that exist on the client but not on the server)
     * by removing all entities that were generated during single-player mode.
     */
    private void clearLocalWorld() {
        long startTime = System.currentTimeMillis();
        System.out.println("Clearing local world state for multiplayer...");
        
        try {
            // Dispose and clear all tree maps
            for (SmallTree tree : trees.values()) {
                try {
                    tree.dispose();
                } catch (Exception e) {
                    System.err.println("Error disposing small tree: " + e.getMessage());
                }
            }
            trees.clear();
            
            for (AppleTree tree : appleTrees.values()) {
                try {
                    tree.dispose();
                } catch (Exception e) {
                    System.err.println("Error disposing apple tree: " + e.getMessage());
                }
            }
            appleTrees.clear();
            
            for (CoconutTree tree : coconutTrees.values()) {
                try {
                    tree.dispose();
                } catch (Exception e) {
                    System.err.println("Error disposing coconut tree: " + e.getMessage());
                }
            }
            coconutTrees.clear();
            
            for (BambooTree tree : bambooTrees.values()) {
                try {
                    tree.dispose();
                } catch (Exception e) {
                    System.err.println("Error disposing bamboo tree: " + e.getMessage());
                }
            }
            bambooTrees.clear();
            
            for (BananaTree tree : bananaTrees.values()) {
                try {
                    tree.dispose();
                } catch (Exception e) {
                    System.err.println("Error disposing banana tree: " + e.getMessage());
                }
            }
            bananaTrees.clear();
            
            // Dispose and clear all item maps
            for (Apple apple : apples.values()) {
                try {
                    apple.dispose();
                } catch (Exception e) {
                    System.err.println("Error disposing apple: " + e.getMessage());
                }
            }
            apples.clear();
            
            for (Banana banana : bananas.values()) {
                try {
                    banana.dispose();
                } catch (Exception e) {
                    System.err.println("Error disposing banana: " + e.getMessage());
                }
            }
            bananas.clear();
            
            for (PalmFiber palmFiber : palmFibers.values()) {
                try {
                    palmFiber.dispose();
                } catch (Exception e) {
                    System.err.println("Error disposing palm fiber: " + e.getMessage());
                }
            }
            palmFibers.clear();
            
            // CRITICAL FIX: Clear planted trees and bamboos to prevent singleplayer planted items
            // from appearing in multiplayer mode
            for (PlantedTree plantedTree : plantedTrees.values()) {
                try {
                    plantedTree.dispose();
                } catch (Exception e) {
                    System.err.println("Error disposing planted tree: " + e.getMessage());
                }
            }
            plantedTrees.clear();
            
            for (PlantedBamboo plantedBamboo : plantedBamboos.values()) {
                try {
                    plantedBamboo.dispose();
                } catch (Exception e) {
                    System.err.println("Error disposing planted bamboo: " + e.getMessage());
                }
            }
            plantedBamboos.clear();
            
            // Clear cleared positions map
            clearedPositions.clear();
            
            long duration = System.currentTimeMillis() - startTime;
            System.out.println("Local world cleared successfully in " + duration + "ms");
            System.out.println("Cleared planted trees and bamboos to prevent singleplayer items in multiplayer");
            
        } catch (Exception e) {
            System.err.println("Error clearing local world: " + e.getMessage());
            e.printStackTrace();
            // Continue anyway - server state will override
        }
    }
    
    /**
     * Updates or creates a tree based on server tree state.
     * Queues tree creation to be processed on main thread for OpenGL context safety.
     * 
     * @param treeState The tree state from the server
     */
    public void updateTreeFromState(TreeState treeState) {
        if (treeState == null) {
            return;
        }
        
        String treeId = treeState.getTreeId();
        
        // If tree doesn't exist, it was already destroyed
        if (!treeState.isExists()) {
            removeTree(treeId);
            return;
        }
        
        // Update health of existing tree
        TreeType type = treeState.getType();
        float health = treeState.getHealth();
        
        // If type is null (from TreeHealthUpdateMessage), try to find the tree in all maps
        if (type == null) {
            // Try each tree type map
            SmallTree smallTree = trees.get(treeId);
            if (smallTree != null) {
                smallTree.setHealth(health);
                return;
            }
            
            AppleTree appleTree = appleTrees.get(treeId);
            if (appleTree != null) {
                appleTree.setHealth(health);
                return;
            }
            
            CoconutTree coconutTree = coconutTrees.get(treeId);
            if (coconutTree != null) {
                coconutTree.setHealth(health);
                return;
            }
            
            BambooTree bambooTree = bambooTrees.get(treeId);
            if (bambooTree != null) {
                bambooTree.setHealth(health);
                return;
            }
            
            BananaTree bananaTree = bananaTrees.get(treeId);
            if (bananaTree != null) {
                bananaTree.setHealth(health);
                return;
            }
            
            // Tree doesn't exist on client - this is normal if it hasn't been generated yet
            return;
        }
        
        // Check if tree already exists
        boolean treeExists = false;
        switch (type) {
            case SMALL:
                treeExists = trees.containsKey(treeId);
                if (treeExists) {
                    trees.get(treeId).setHealth(health);
                }
                break;
            case APPLE:
                treeExists = appleTrees.containsKey(treeId);
                if (treeExists) {
                    appleTrees.get(treeId).setHealth(health);
                }
                break;
            case COCONUT:
                treeExists = coconutTrees.containsKey(treeId);
                if (treeExists) {
                    coconutTrees.get(treeId).setHealth(health);
                }
                break;
            case BAMBOO:
                treeExists = bambooTrees.containsKey(treeId);
                if (treeExists) {
                    bambooTrees.get(treeId).setHealth(health);
                }
                break;
            case BANANA:
                treeExists = bananaTrees.containsKey(treeId);
                if (treeExists) {
                    bananaTrees.get(treeId).setHealth(health);
                }
                break;
        }
        
        // If tree doesn't exist, queue creation for main thread
        if (!treeExists) {
            pendingTreeCreations.offer(treeState);
        }
    }
    
    /**
     * Removes a tree from the game world.
     * Queues removal to be processed on main thread for OpenGL context safety.
     * 
     * @param treeId The tree ID to remove
     */
    public void removeTree(String treeId) {
        // Queue tree removal to be processed on main thread
        pendingTreeRemovals.offer(treeId);
    }
    
    /**
     * Actually removes a tree from the game world (called on main thread).
     * 
     * @param treeId The tree ID to remove
     * @return true if the tree was found and removed, false otherwise
     */
    private boolean removeTreeImmediate(String treeId) {
        System.out.println("[DEBUG] Attempting to remove tree: " + treeId);
        
        // Try to remove from all tree maps
        SmallTree smallTree = trees.remove(treeId);
        if (smallTree != null) {
            smallTree.dispose();
            clearedPositions.put(treeId, true);
            return true;
        }
        
        AppleTree appleTree = appleTrees.remove(treeId);
        if (appleTree != null) {
            appleTree.dispose();
            clearedPositions.put(treeId, true);
            return true;
        }
        
        CoconutTree coconutTree = coconutTrees.remove(treeId);
        if (coconutTree != null) {
            coconutTree.dispose();
            clearedPositions.put(treeId, true);
            return true;
        }
        
        BambooTree bambooTree = bambooTrees.remove(treeId);
        if (bambooTree != null) {
            System.out.println("[DEBUG] Bamboo tree removed: " + treeId);
            bambooTree.dispose();
            clearedPositions.put(treeId, true);
            return true;
        }
        
        BananaTree bananaTree = bananaTrees.remove(treeId);
        if (bananaTree != null) {
            bananaTree.dispose();
            clearedPositions.put(treeId, true);
            return true;
        }
        
        // Tree not found in any map
        System.out.println("[DEBUG] Tree not found in any map: " + treeId);
        System.out.println("[DEBUG] Current tree counts - Small: " + trees.size() + 
                         ", Apple: " + appleTrees.size() + 
                         ", Coconut: " + coconutTrees.size() + 
                         ", Bamboo: " + bambooTrees.size() + 
                         ", Banana: " + bananaTrees.size());
        return false;
    }
    
    /**
     * Updates a stone's health from server state or creates it if it doesn't exist.
     * 
     * @param stoneState The stone state from the server
     */
    public void updateStoneFromState(StoneState stoneState) {
        if (stoneState == null) {
            return;
        }
        
        String stoneId = stoneState.getStoneId();
        float health = stoneState.getHealth();
        
        // Check if stone already exists
        Stone stone = stoneMap.get(stoneId);
        if (stone != null) {
            // Update health of existing stone
            stone.setHealth(health);
        } else {
            // Queue stone creation to render thread (texture creation requires OpenGL context)
            deferOperation(() -> {
                // Double-check it wasn't created while waiting for render thread
                if (!stoneMap.containsKey(stoneId)) {
                    Stone newStone = new Stone(stoneState.getX(), stoneState.getY());
                    newStone.setHealth(health);
                    stones.put(stoneId, newStone);
                    stoneMap.put(stoneId, newStone);
                    System.out.println("[STONE] Created stone on client: " + stoneId + " at (" + stoneState.getX() + ", " + stoneState.getY() + ")");
                }
            });
        }
    }
    
    /**
     * Removes a stone from the game world.
     * Uses deferred operations for OpenGL context safety.
     * 
     * @param stoneId The stone ID to remove
     */
    public void removeStone(String stoneId) {
        // Remove from both maps immediately (thread-safe)
        Stone stone = stoneMap.remove(stoneId);
        stones.remove(stoneId);
        
        // Add to cleared positions to prevent regeneration
        clearedPositions.put(stoneId, true);
        
        if (stone != null) {
            // Defer texture disposal to main thread
            deferOperation(() -> {
                stone.dispose();
            });
            System.out.println("Stone removed: " + stoneId);
        }
    }
    
    /**
     * Updates or creates an item based on server item state.
     * This method queues item creation to be processed on the main thread.
     * 
     * @param itemState The item state from the server
     */
    public void updateItemFromState(ItemState itemState) {
        if (itemState == null) {
            return;
        }
        
        String itemId = itemState.getItemId();
        
        // If item is collected, remove it (safe to do from any thread)
        if (itemState.isCollected()) {
            removeItem(itemId);
            return;
        }
        
        // Queue item spawn to be processed on main thread (for OpenGL context)
        pendingItemSpawns.offer(itemState);
    }
    
    /**
     * Removes an item from the game world.
     * 
     * <p>This method demonstrates the correct pattern for handling resource cleanup in a
     * multi-threaded environment. It separates immediate game state updates from deferred
     * OpenGL operations to ensure thread safety while maintaining responsive gameplay.</p>
     * 
     * <h3>Thread Safety Design:</h3>
     * <ol>
     *   <li><b>Immediate State Update:</b> The item is removed from the game state maps
     *       immediately on the calling thread. This ensures the item disappears from
     *       gameplay instantly, providing responsive multiplayer experience.</li>
     *   <li><b>Deferred Resource Disposal:</b> The texture disposal is queued for execution
     *       on the render thread via {@link #deferOperation(Runnable)}. This prevents
     *       OpenGL threading violations that would cause crashes.</li>
     * </ol>
     * 
     * <h3>Why This Pattern Works:</h3>
     * <ul>
     *   <li><b>Responsiveness:</b> Item removal is immediate - no waiting for render thread</li>
     *   <li><b>Safety:</b> OpenGL operations only happen on the correct thread</li>
     *   <li><b>No Visual Artifacts:</b> Item is removed from maps before disposal, so it
     *       won't be rendered even if disposal is delayed by one frame</li>
     * </ul>
     * 
     * <h3>Call Context:</h3>
     * <p>This method can be safely called from:</p>
     * <ul>
     *   <li>Network message handlers (GameClient-Receive thread)</li>
     *   <li>Render thread (main game loop)</li>
     *   <li>Any other thread that needs to remove items</li>
     * </ul>
     * 
     * <h3>Usage Example:</h3>
     * <pre>{@code
     * // In GameMessageHandler (Network Thread)
     * public void handleItemPickup(ItemPickupMessage message) {
     *     String itemId = message.getItemId();
     *     
     *     // This is safe to call from network thread
     *     game.removeItem(itemId);
     *     
     *     // Item is immediately removed from game state
     *     // Texture disposal happens on next render frame
     * }
     * }</pre>
     * 
     * <h3>Implementation Pattern:</h3>
     * <pre>{@code
     * // Step 1: Immediate state update (thread-safe map operation)
     * Apple apple = apples.remove(itemId);
     * 
     * // Step 2: Defer OpenGL operation if item existed
     * if (apple != null) {
     *     deferOperation(() -> apple.dispose());
     * }
     * }</pre>
     * 
     * <h3>Supported Item Types:</h3>
     * <ul>
     *   <li>{@link wagemaker.uk.items.Apple} - Dropped by AppleTrees</li>
     *   <li>{@link wagemaker.uk.items.Banana} - Dropped by BananaTrees</li>
     * </ul>
     * 
     * <h3>Related Methods:</h3>
     * <p>This same pattern is used throughout the codebase for thread-safe resource management:</p>
     * <ul>
     *   <li>{@link #removeTree(String)} - For tree removal</li>
     *   <li>{@link #queuePlayerLeave(String)} - For remote player cleanup</li>
     * </ul>
     * 
     * @param itemId The unique identifier of the item to remove. If the item doesn't exist,
     *               this method does nothing (safe to call with non-existent IDs).
     * 
     * @see #deferOperation(Runnable)
     * @see #updateItemFromState(ItemState)
     */
    /**
     * Gets the type of an item by its ID.
     * This method checks all item collections to determine the item type.
     * 
     * @param itemId The ID of the item to look up
     * @return The ItemType of the item, or null if not found
     */
    public wagemaker.uk.inventory.ItemType getItemType(String itemId) {
        if (apples.containsKey(itemId)) {
            return wagemaker.uk.inventory.ItemType.APPLE;
        }
        if (bananas.containsKey(itemId)) {
            return wagemaker.uk.inventory.ItemType.BANANA;
        }
        if (bambooStacks.containsKey(itemId)) {
            return wagemaker.uk.inventory.ItemType.BAMBOO_STACK;
        }
        if (babyBamboos.containsKey(itemId)) {
            return wagemaker.uk.inventory.ItemType.BABY_BAMBOO;
        }
        if (babyTrees.containsKey(itemId)) {
            return wagemaker.uk.inventory.ItemType.BABY_TREE;
        }
        if (woodStacks.containsKey(itemId)) {
            return wagemaker.uk.inventory.ItemType.WOOD_STACK;
        }
        if (pebbles.containsKey(itemId)) {
            return wagemaker.uk.inventory.ItemType.PEBBLE;
        }
        if (palmFibers.containsKey(itemId)) {
            return wagemaker.uk.inventory.ItemType.PALM_FIBER;
        }
        return null;
    }
    
    public void removeItem(String itemId) {
        // Immediately remove from game state (thread-safe map operations)
        Apple apple = apples.remove(itemId);
        if (apple != null) {
            // Defer texture disposal to render thread
            deferOperation(() -> apple.dispose());
            return;
        }
        
        Banana banana = bananas.remove(itemId);
        if (banana != null) {
            // Defer texture disposal to render thread
            deferOperation(() -> banana.dispose());
            return;
        }
        
        BambooStack bambooStack = bambooStacks.remove(itemId);
        if (bambooStack != null) {
            // Defer texture disposal to render thread
            deferOperation(() -> bambooStack.dispose());
            return;
        }
        
        BabyBamboo babyBamboo = babyBamboos.remove(itemId);
        if (babyBamboo != null) {
            // Defer texture disposal to render thread
            deferOperation(() -> babyBamboo.dispose());
            return;
        }
        
        BabyTree babyTree = babyTrees.remove(itemId);
        if (babyTree != null) {
            // Defer texture disposal to render thread
            deferOperation(() -> babyTree.dispose());
            return;
        }
        
        WoodStack woodStack = woodStacks.remove(itemId);
        if (woodStack != null) {
            // Defer texture disposal to render thread
            deferOperation(() -> woodStack.dispose());
            return;
        }
        
        Pebble pebble = pebbles.remove(itemId);
        if (pebble != null) {
            // Defer texture disposal to render thread
            deferOperation(() -> pebble.dispose());
            return;
        }
        
        PalmFiber palmFiber = palmFibers.remove(itemId);
        if (palmFiber != null) {
            // Defer texture disposal to render thread
            deferOperation(() -> palmFiber.dispose());
        }
    }
    
    /**
     * Gets the current game mode.
     * 
     * @return The current game mode
     */
    public GameMode getGameMode() {
        return gameMode;
    }
    
    /**
     * Gets the game server (only available in host mode).
     * 
     * @return The game server, or null if not hosting
     */
    public GameServer getGameServer() {
        return gameServer;
    }
    
    /**
     * Gets the game client (available in multiplayer modes).
     * 
     * @return The game client, or null if in single-player mode
     */
    public GameClient getGameClient() {
        return gameClient;
    }
    
    /**
     * Gets the map of remote players.
     * 
     * @return The map of remote players by player ID
     */
    public Map<String, RemotePlayer> getRemotePlayers() {
        return remotePlayers;
    }
    
    /**
     * Gets the local player instance.
     * 
     * @return The local player
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Gets the last connection address (with port) used for connecting to a server.
     * This is used to save the server address after a successful connection.
     * 
     * @return The last connection address in format "address:port", or null if no connection attempted
     */
    public String getLastConnectionAddress() {
        return lastConnectionAddress;
    }
    
    /**
     * Defers an operation to be executed on the render thread.
     * 
     * <p>This method is the cornerstone of thread-safe OpenGL operations in multiplayer mode.
     * It allows network message handlers (running on the Network Thread) to schedule OpenGL
     * operations for execution on the Render Thread, which owns the OpenGL context.</p>
     * 
     * <h3>Why This Is Necessary:</h3>
     * <p>OpenGL has a strict threading requirement: all OpenGL calls must be made from the
     * thread that created the OpenGL context (the Render Thread). Violating this rule causes
     * immediate crashes with errors like "INVALID_OPERATION" or "context not current".</p>
     * 
     * <h3>When to Use This Method:</h3>
     * <ul>
     *   <li><b>Texture Disposal:</b> When removing items, players, or any entity with textures</li>
     *   <li><b>Resource Creation:</b> When spawning entities from network messages</li>
     *   <li><b>Shader Operations:</b> Any shader compilation or uniform updates</li>
     *   <li><b>Buffer Operations:</b> VBO/IBO creation or disposal</li>
     *   <li><b>General Rule:</b> If it touches graphics, defer it!</li>
     * </ul>
     * 
     * <h3>Usage Examples:</h3>
     * 
     * <h4>Example 1: Item Disposal (Most Common)</h4>
     * <pre>{@code
     * // In network message handler
     * public void handleItemPickup(String itemId) {
     *     Apple apple = apples.remove(itemId);  // Immediate state update
     *     if (apple != null) {
     *         deferOperation(() -> apple.dispose());  // Deferred texture disposal
     *     }
     * }
     * }</pre>
     * 
     * <h4>Example 2: Remote Player Cleanup</h4>
     * <pre>{@code
     * // In network message handler
     * public void handlePlayerLeave(String playerId) {
     *     RemotePlayer player = remotePlayers.remove(playerId);
     *     if (player != null) {
     *         deferOperation(() -> player.dispose());  // Deferred texture disposal
     *     }
     * }
     * }</pre>
     * 
     * <h4>Example 3: Multiple Operations</h4>
     * <pre>{@code
     * // In network message handler
     * public void handleGameEnd() {
     *     // Defer multiple cleanup operations
     *     deferOperation(() -> {
     *         for (Apple apple : apples.values()) {
     *             apple.dispose();
     *         }
     *         apples.clear();
     *     });
     * }
     * }</pre>
     * 
     * <h3>What NOT to Defer:</h3>
     * <pre>{@code
     * // ❌ DON'T defer simple state updates
     * deferOperation(() -> apples.remove(itemId));  // Unnecessary!
     * 
     * // ✅ DO update state immediately, defer only OpenGL operations
     * Apple apple = apples.remove(itemId);
     * if (apple != null) {
     *     deferOperation(() -> apple.dispose());
     * }
     * }</pre>
     * 
     * <h3>Thread Safety:</h3>
     * <p>This method is thread-safe and can be called from any thread. Operations are queued
     * in a {@link java.util.concurrent.ConcurrentLinkedQueue} and processed at the start of
     * each render frame in FIFO order.</p>
     * 
     * <h3>Performance Considerations:</h3>
     * <ul>
     *   <li>Operations are executed synchronously during the render loop</li>
     *   <li>Typical queue size: 0-10 operations per frame</li>
     *   <li>Warning logged if queue exceeds 100 operations (possible memory leak)</li>
     *   <li>No blocking or waiting - network thread continues immediately</li>
     * </ul>
     * 
     * <h3>Error Handling:</h3>
     * <p>If an operation throws an exception, it is caught and logged, but other queued
     * operations continue to execute. This prevents one failed disposal from blocking
     * the entire queue.</p>
     * 
     * @param operation The operation to execute on the render thread. Must not be null.
     *                  Typically a lambda expression like {@code () -> texture.dispose()}
     * 
     * @see #removeItem(String)
     * @see #render()
     */
    public void deferOperation(Runnable operation) {
        if (operation == null) {
            return;
        }
        
        pendingDeferredOperations.add(operation);
        
        // Optional: Log for debugging
        if (Gdx.app.getLogLevel() >= com.badlogic.gdx.Application.LOG_DEBUG) {
            System.out.println("[DEBUG] Deferred operation queued. Queue size: " + 
                              pendingDeferredOperations.size());
        }
        
        // Optional: Warn if queue is getting large
        if (pendingDeferredOperations.size() > 100) {
            System.err.println("[WARNING] Deferred operation queue size exceeds 100. " +
                              "Possible memory leak or render thread stall.");
        }
    }
    
    /**
     * Displays a notification message to the player.
     * The notification will be shown for 3 seconds.
     * 
     * @param message The notification message to display
     */
    public void displayNotification(String message) {
        this.currentNotification = message;
        this.notificationTimer = 3.0f; // Display for 3 seconds
    }
    
    /**
     * Attempts to host a multiplayer server with error handling.
     * Shows error dialog on failure.
     */
    public void attemptHostServer() {
        try {
            isHosting = true;
            startMultiplayerHost();
            
            // Show server IP dialog on success
            if (gameServer != null) {
                gameMenu.getServerHostDialog().show(gameServer.getPublicIPv4());
            }
            
        } catch (Exception e) {
            // Show error dialog
            String errorMsg = "Failed to start server: " + e.getMessage();
            gameMenu.showError(errorMsg);
            isHosting = false;
        }
    }
    
    /**
     * Attempts to connect to a server with error handling.
     * Shows error dialog on failure.
     * 
     * @param address The server address
     * @param port The server port
     */
    public void attemptConnectToServer(String address, int port) {
        try {
            pendingConnectionAddress = address;
            pendingConnectionPort = port;
            
            // Store the full address string (with port) for saving after successful connection
            lastConnectionAddress = address + ":" + port;
            
            joinMultiplayerServer(address, port);
            
            // Save the last server address to config after successful connection
            wagemaker.uk.client.PlayerConfig config = wagemaker.uk.client.PlayerConfig.load();
            config.saveLastServer(lastConnectionAddress);
            
            // Show success notification
            displayNotification("Connected to server!");
            
        } catch (Exception e) {
            // Show error dialog
            String errorMsg = "Failed to connect: " + e.getMessage();
            gameMenu.showError(errorMsg);
        }
    }
    
    /**
     * Handles retry action from error dialog.
     * Retries the last connection attempt.
     */
    public void retryConnection() {
        if (isHosting) {
            attemptHostServer();
        } else if (pendingConnectionAddress != null) {
            attemptConnectToServer(pendingConnectionAddress, pendingConnectionPort);
        }
    }
    
    /**
     * Handles cancel action from error dialog.
     * Returns to multiplayer menu.
     */
    public void cancelConnection() {
        isHosting = false;
        pendingConnectionAddress = null;
        pendingConnectionPort = 0;
        gameMenu.returnToMultiplayerMenu();
    }
    
    /**
     * Disconnects from multiplayer and returns to singleplayer mode.
     * This method cleanly shuts down all multiplayer connections and resources,
     * allowing the player to continue in singleplayer mode.
     */
    public void disconnectFromMultiplayer() {
        if (gameMode == GameMode.SINGLEPLAYER) {
            System.out.println("Already in singleplayer mode");
            return;
        }
        
        System.out.println("Disconnecting from multiplayer...");
        
        // Save current multiplayer position before disconnecting
        if (gameMenu != null) {
            gameMenu.savePlayerPosition();
        }
        
        // Disconnect client
        if (gameClient != null) {
            try {
                gameClient.disconnect();
            } catch (Exception e) {
                System.err.println("Error disconnecting client: " + e.getMessage());
            }
            gameClient = null;
        }
        
        // Stop server if hosting
        if (gameServer != null) {
            try {
                gameServer.stop();
            } catch (Exception e) {
                System.err.println("Error stopping server: " + e.getMessage());
            }
            gameServer = null;
        }
        
        // Clean up remote players
        for (RemotePlayer remotePlayer : remotePlayers.values()) {
            try {
                remotePlayer.dispose();
            } catch (Exception e) {
                System.err.println("Error disposing remote player: " + e.getMessage());
            }
        }
        remotePlayers.clear();
        
        // Clear pending queues
        pendingPlayerJoins.clear();
        pendingPlayerLeaves.clear();
        pendingItemSpawns.clear();
        pendingTreeRemovals.clear();
        pendingTreeCreations.clear();
        pendingBambooPlants.clear();
        pendingBambooTransforms.clear();
        pendingTreePlants.clear();
        pendingTreeTransforms.clear();
        pendingDeferredOperations.clear();
        
        // Reset player's game client reference
        if (player != null) {
            player.setGameClient(null);
        }
        
        // Reset connection quality indicator
        if (connectionQualityIndicator != null) {
            connectionQualityIndicator.setGameClient(null);
        }
        
        // Return to singleplayer mode
        gameMode = GameMode.SINGLEPLAYER;
        
        // Switch inventory back to single-player mode
        if (inventoryManager != null) {
            inventoryManager.setMultiplayerMode(false);
        }
        
        // Reset world seed to 0 for singleplayer
        worldSeed = 0;
        
        // Clear rain zones (dynamic rain manager will handle rain events)
        if (rainSystem != null) {
            rainSystem.getZoneManager().clearAllZones();
        }
        
        // Load singleplayer position
        if (gameMenu != null) {
            gameMenu.loadPlayerPosition();
        }
        
        // Display notification
        displayNotification("Disconnected from multiplayer");
        
        System.out.println("Successfully disconnected from multiplayer");
    }
    
    /**
     * Corrects the local player's position with smooth interpolation.
     * Called when the server detects position desynchronization.
     * 
     * @param correctedX The corrected X position
     * @param correctedY The corrected Y position
     * @param correctedDirection The corrected direction
     */
    public void correctPlayerPosition(float correctedX, float correctedY, wagemaker.uk.network.Direction correctedDirection) {
        if (player == null) {
            return;
        }
        
        // Calculate distance to corrected position
        float dx = correctedX - player.getX();
        float dy = correctedY - player.getY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        // Log the correction
        System.err.println("Position desynchronization detected!");
        System.err.println("  Current position: (" + player.getX() + ", " + player.getY() + ")");
        System.err.println("  Corrected position: (" + correctedX + ", " + correctedY + ")");
        System.err.println("  Distance: " + distance + " pixels");
        
        // If distance is small, snap immediately
        if (distance < 10) {
            player.setPosition(correctedX, correctedY);
        } else {
            // For larger distances, use smooth interpolation
            // We'll interpolate over the next few frames
            // This is a simple approach - could be enhanced with a proper interpolation system
            
            // For now, just snap to the corrected position
            // A more sophisticated system would interpolate over time
            player.setPosition(correctedX, correctedY);
        }
        
        // Update direction if needed
        // Note: Direction enum from network package needs to be converted to player direction
        // This would require mapping between the two direction systems
    }
    
    /**
     * Configures the planting maximum range for the targeting system.
     * Called when the client receives the range configuration from the server.
     * 
     * @param maxRange The maximum planting range in pixels, or -1 for unlimited
     */
    public void setPlantingMaxRange(int maxRange) {
        // Validate player exists
        if (player == null) {
            System.err.println("Cannot set planting max range: player is null");
            return;
        }
        
        // Get the player's targeting system
        TargetingSystem targetingSystem = player.getTargetingSystem();
        if (targetingSystem == null) {
            System.err.println("Cannot set planting max range: targeting system is null");
            return;
        }
        
        // Configure the targeting system with the received range
        targetingSystem.setMaxRange(maxRange);
        
        // Log the configuration
        if (maxRange > 0) {
            int tiles = maxRange / 64; // Convert pixels to tiles (64 pixels per tile)
            System.out.println("Targeting system configured with planting range:");
            System.out.println("  - Max range: " + maxRange + " pixels (" + tiles + " tiles)");
        } else {
            System.out.println("Targeting system configured with unlimited planting range");
        }
    }
    
    /**
     * Renders the current notification message on screen.
     */
    private void renderNotification() {
        if (currentNotification == null) {
            return;
        }
        
        batch.begin();
        
        // Calculate position (top center of screen)
        float screenX = camera.position.x;
        float screenY = camera.position.y + viewport.getWorldHeight() / 2 - 80;
        
        // Draw notification text
        gameMenu.getFont().setColor(1, 1, 0, 1); // Yellow color
        gameMenu.getFont().draw(batch, currentNotification, 
            screenX - gameMenu.getFont().getSpaceXadvance() * currentNotification.length() / 4, 
            screenY);
        
        batch.end();
    }
    
    /**
     * Extracts the current world state for saving.
     * Creates a complete snapshot of all game entities including trees, items, 
     * cleared positions, and rain zones.
     * 
     * @return WorldState containing complete current game state
     */
    public WorldState extractCurrentWorldState() {
        WorldState worldState = new WorldState(worldSeed);
        
        // Extract tree states
        Map<String, TreeState> treeStates = new HashMap<>();
        
        // Add small trees
        for (Map.Entry<String, SmallTree> entry : trees.entrySet()) {
            SmallTree tree = entry.getValue();
            TreeState treeState = new TreeState(
                entry.getKey(),
                TreeType.SMALL,
                tree.getX(),
                tree.getY(),
                tree.getHealth(),
                true
            );
            treeStates.put(entry.getKey(), treeState);
        }
        
        // Add apple trees
        for (Map.Entry<String, AppleTree> entry : appleTrees.entrySet()) {
            AppleTree tree = entry.getValue();
            TreeState treeState = new TreeState(
                entry.getKey(),
                TreeType.APPLE,
                tree.getX(),
                tree.getY(),
                tree.getHealth(),
                true
            );
            treeStates.put(entry.getKey(), treeState);
        }
        
        // Add coconut trees
        for (Map.Entry<String, CoconutTree> entry : coconutTrees.entrySet()) {
            CoconutTree tree = entry.getValue();
            TreeState treeState = new TreeState(
                entry.getKey(),
                TreeType.COCONUT,
                tree.getX(),
                tree.getY(),
                tree.getHealth(),
                true
            );
            treeStates.put(entry.getKey(), treeState);
        }
        
        // Add bamboo trees
        for (Map.Entry<String, BambooTree> entry : bambooTrees.entrySet()) {
            BambooTree tree = entry.getValue();
            TreeState treeState = new TreeState(
                entry.getKey(),
                TreeType.BAMBOO,
                tree.getX(),
                tree.getY(),
                tree.getHealth(),
                true
            );
            treeStates.put(entry.getKey(), treeState);
        }
        
        // Add banana trees
        for (Map.Entry<String, BananaTree> entry : bananaTrees.entrySet()) {
            BananaTree tree = entry.getValue();
            TreeState treeState = new TreeState(
                entry.getKey(),
                TreeType.BANANA,
                tree.getX(),
                tree.getY(),
                tree.getHealth(),
                true
            );
            treeStates.put(entry.getKey(), treeState);
        }
        
        worldState.setTrees(treeStates);
        
        // Extract item states
        Map<String, ItemState> itemStates = new HashMap<>();
        
        // Add apples
        for (Map.Entry<String, Apple> entry : apples.entrySet()) {
            Apple apple = entry.getValue();
            ItemState itemState = new ItemState(
                entry.getKey(),
                ItemType.APPLE,
                apple.getX(),
                apple.getY(),
                false
            );
            itemStates.put(entry.getKey(), itemState);
        }
        
        // Add bananas
        for (Map.Entry<String, Banana> entry : bananas.entrySet()) {
            Banana banana = entry.getValue();
            ItemState itemState = new ItemState(
                entry.getKey(),
                ItemType.BANANA,
                banana.getX(),
                banana.getY(),
                false
            );
            itemStates.put(entry.getKey(), itemState);
        }
        
        worldState.setItems(itemStates);
        
        // Extract stone states
        Map<String, StoneState> stoneStates = new HashMap<>();
        for (Map.Entry<String, Stone> entry : stones.entrySet()) {
            Stone stone = entry.getValue();
            StoneState stoneState = new StoneState(
                entry.getKey(),
                stone.getX(),
                stone.getY(),
                stone.getHealth()
            );
            stoneStates.put(entry.getKey(), stoneState);
        }
        worldState.setStones(stoneStates);
        
        // Extract cleared positions
        worldState.setClearedPositions(new HashSet<>(clearedPositions.keySet()));
        
        // Extract rain zones
        if (rainSystem != null && rainSystem.getZoneManager() != null) {
            worldState.setRainZones(rainSystem.getZoneManager().getRainZones());
        }
        
        // Extract planted trees
        Map<String, wagemaker.uk.network.PlantedTreeState> plantedTreeStates = new HashMap<>();
        for (Map.Entry<String, PlantedTree> entry : plantedTrees.entrySet()) {
            PlantedTree plantedTree = entry.getValue();
            wagemaker.uk.network.PlantedTreeState treeState = new wagemaker.uk.network.PlantedTreeState(
                entry.getKey(),
                plantedTree.getX(),
                plantedTree.getY(),
                0.0f // Growth timer - PlantedTree doesn't expose this, so use 0
            );
            plantedTreeStates.put(entry.getKey(), treeState);
        }
        worldState.setPlantedTrees(plantedTreeStates);
        
        // Extract planted bamboos
        Map<String, wagemaker.uk.network.PlantedBambooState> plantedBambooStates = new HashMap<>();
        for (Map.Entry<String, PlantedBamboo> entry : plantedBamboos.entrySet()) {
            PlantedBamboo plantedBamboo = entry.getValue();
            wagemaker.uk.network.PlantedBambooState bambooState = new wagemaker.uk.network.PlantedBambooState(
                entry.getKey(),
                plantedBamboo.getX(),
                plantedBamboo.getY(),
                0.0f // Growth timer - PlantedBamboo doesn't expose this, so use 0
            );
            plantedBambooStates.put(entry.getKey(), bambooState);
        }
        worldState.setPlantedBamboos(plantedBambooStates);
        
        return worldState;
    }
    
    /**
     * Restores the world state from WorldSaveData.
     * This method safely restores all game entities while ensuring proper cleanup
     * of existing state and thread-safe operations.
     * 
     * @param saveData The world save data to restore from
     * @return true if restoration was successful, false otherwise
     */
    public boolean restoreWorldState(WorldSaveData saveData) {
        if (saveData == null) {
            System.err.println("Cannot restore world state: save data is null");
            return false;
        }
        
        try {
            System.out.println("Restoring world state from save: " + saveData.getSaveName());
            
            // Clean up existing world state first
            cleanupExistingWorldState();
            
            // Restore world seed
            this.worldSeed = saveData.getWorldSeed();
            System.out.println("Restored world seed: " + worldSeed);
            
            // Restore trees
            if (saveData.getTrees() != null) {
                restoreTreesFromSave(saveData.getTrees());
                System.out.println("Restored " + saveData.getTrees().size() + " trees");
            }
            
            // Restore stones
            if (saveData.getStones() != null) {
                restoreStonesFromSave(saveData.getStones());
                System.out.println("Restored " + saveData.getStones().size() + " stones");
            }
            
            // Restore items
            if (saveData.getItems() != null) {
                restoreItemsFromSave(saveData.getItems());
                System.out.println("Restored " + saveData.getItems().size() + " items");
            }
            
            // Restore cleared positions
            if (saveData.getClearedPositions() != null) {
                clearedPositions.clear();
                for (String position : saveData.getClearedPositions()) {
                    clearedPositions.put(position, true);
                }
                System.out.println("Restored " + saveData.getClearedPositions().size() + " cleared positions");
            }
            
            // Restore rain zones
            if (saveData.getRainZones() != null && rainSystem != null) {
                rainSystem.getZoneManager().setRainZones(saveData.getRainZones());
                System.out.println("Restored " + saveData.getRainZones().size() + " rain zones");
            }
            
            // Restore planted trees
            if (saveData.getPlantedTrees() != null) {
                restorePlantedTreesFromSave(saveData.getPlantedTrees());
                System.out.println("Restored " + saveData.getPlantedTrees().size() + " planted trees");
            }
            
            // Restore planted bamboos
            if (saveData.getPlantedBamboos() != null) {
                restorePlantedBamboosFromSave(saveData.getPlantedBamboos());
                System.out.println("Restored " + saveData.getPlantedBamboos().size() + " planted bamboos");
            }
            
            // Restore player position and health from world save
            // Note: This will be overridden by the existing player position save system
            // if a separate player position save exists
            if (player != null) {
                player.setPosition(saveData.getPlayerX(), saveData.getPlayerY());
                player.setHealth(saveData.getPlayerHealth());
                System.out.println("Restored player position from world save: (" + saveData.getPlayerX() + ", " + saveData.getPlayerY() + ")");
                System.out.println("Restored player health from world save: " + saveData.getPlayerHealth());
                System.out.println("Note: Position may be overridden by existing player position save system");
            }
            
            // Restore inventory from world save
            if (inventoryManager != null) {
                wagemaker.uk.inventory.Inventory targetInventory = inventoryManager.getCurrentInventory();
                if (targetInventory != null) {
                    targetInventory.setAppleCount(saveData.getAppleCount());
                    targetInventory.setBananaCount(saveData.getBananaCount());
                    targetInventory.setBabyBambooCount(saveData.getBabyBambooCount());
                    targetInventory.setBambooStackCount(saveData.getBambooStackCount());
                    targetInventory.setBabyTreeCount(saveData.getBabyTreeCount());
                    targetInventory.setWoodStackCount(saveData.getWoodStackCount());
                    targetInventory.setPebbleCount(saveData.getPebbleCount());
                    System.out.println("Restored inventory from world save (pebbles: " + saveData.getPebbleCount() + ")");
                }
            }
            
            System.out.println("World state restoration completed successfully");
            return true;
            
        } catch (Exception e) {
            System.err.println("Error restoring world state: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Cleans up existing world state before loading a new one.
     * Ensures proper disposal of resources and prevents memory leaks.
     */
    private void cleanupExistingWorldState() {
        System.out.println("Cleaning up existing world state...");
        
        try {
            // Dispose and clear all tree maps
            for (SmallTree tree : trees.values()) {
                try {
                    tree.dispose();
                } catch (Exception e) {
                    System.err.println("Error disposing small tree: " + e.getMessage());
                }
            }
            trees.clear();
            
            for (AppleTree tree : appleTrees.values()) {
                try {
                    tree.dispose();
                } catch (Exception e) {
                    System.err.println("Error disposing apple tree: " + e.getMessage());
                }
            }
            appleTrees.clear();
            
            for (CoconutTree tree : coconutTrees.values()) {
                try {
                    tree.dispose();
                } catch (Exception e) {
                    System.err.println("Error disposing coconut tree: " + e.getMessage());
                }
            }
            coconutTrees.clear();
            
            for (BambooTree tree : bambooTrees.values()) {
                try {
                    tree.dispose();
                } catch (Exception e) {
                    System.err.println("Error disposing bamboo tree: " + e.getMessage());
                }
            }
            bambooTrees.clear();
            
            for (BananaTree tree : bananaTrees.values()) {
                try {
                    tree.dispose();
                } catch (Exception e) {
                    System.err.println("Error disposing banana tree: " + e.getMessage());
                }
            }
            bananaTrees.clear();
            
            // Dispose and clear all item maps
            for (Apple apple : apples.values()) {
                try {
                    apple.dispose();
                } catch (Exception e) {
                    System.err.println("Error disposing apple: " + e.getMessage());
                }
            }
            apples.clear();
            
            for (Banana banana : bananas.values()) {
                try {
                    banana.dispose();
                } catch (Exception e) {
                    System.err.println("Error disposing banana: " + e.getMessage());
                }
            }
            bananas.clear();
            
            // Dispose and clear planted trees
            for (PlantedTree plantedTree : plantedTrees.values()) {
                try {
                    plantedTree.dispose();
                } catch (Exception e) {
                    System.err.println("Error disposing planted tree: " + e.getMessage());
                }
            }
            plantedTrees.clear();
            
            // Dispose and clear planted bamboos
            for (PlantedBamboo plantedBamboo : plantedBamboos.values()) {
                try {
                    plantedBamboo.dispose();
                } catch (Exception e) {
                    System.err.println("Error disposing planted bamboo: " + e.getMessage());
                }
            }
            plantedBamboos.clear();
            
            // Clear cleared positions
            clearedPositions.clear();
            
            System.out.println("Existing world state cleaned up successfully");
            
        } catch (Exception e) {
            System.err.println("Error during world state cleanup: " + e.getMessage());
            e.printStackTrace();
            // Continue anyway - restoration will override
        }
    }
    
    /**
     * Restores trees from save data.
     * Creates tree instances based on the saved tree states.
     */
    private void restoreTreesFromSave(Map<String, TreeState> savedTrees) {
        for (Map.Entry<String, TreeState> entry : savedTrees.entrySet()) {
            String treeId = entry.getKey();
            TreeState treeState = entry.getValue();
            
            if (!treeState.isExists()) {
                // Tree was destroyed, add to cleared positions
                clearedPositions.put(treeId, true);
                continue;
            }
            
            try {
                switch (treeState.getType()) {
                    case SMALL:
                        SmallTree smallTree = new SmallTree(treeState.getX(), treeState.getY());
                        smallTree.setHealth(treeState.getHealth());
                        trees.put(treeId, smallTree);
                        break;
                        
                    case APPLE:
                        AppleTree appleTree = new AppleTree(treeState.getX(), treeState.getY());
                        appleTree.setHealth(treeState.getHealth());
                        appleTrees.put(treeId, appleTree);
                        break;
                        
                    case COCONUT:
                        CoconutTree coconutTree = new CoconutTree(treeState.getX(), treeState.getY());
                        coconutTree.setHealth(treeState.getHealth());
                        coconutTrees.put(treeId, coconutTree);
                        break;
                        
                    case BAMBOO:
                        BambooTree bambooTree = new BambooTree(treeState.getX(), treeState.getY());
                        bambooTree.setHealth(treeState.getHealth());
                        bambooTrees.put(treeId, bambooTree);
                        break;
                        
                    case BANANA:
                        BananaTree bananaTree = new BananaTree(treeState.getX(), treeState.getY());
                        bananaTree.setHealth(treeState.getHealth());
                        bananaTrees.put(treeId, bananaTree);
                        break;
                }
            } catch (Exception e) {
                System.err.println("Error restoring tree " + treeId + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Restores stones from save data.
     * Creates stone instances based on the saved stone states.
     */
    private void restoreStonesFromSave(Map<String, StoneState> savedStones) {
        for (Map.Entry<String, StoneState> entry : savedStones.entrySet()) {
            String stoneId = entry.getKey();
            StoneState stoneState = entry.getValue();
            
            try {
                Stone stone = new Stone(stoneState.getX(), stoneState.getY());
                stone.setHealth(stoneState.getHealth());
                stones.put(stoneId, stone);
                stoneMap.put(stoneId, stone);
            } catch (Exception e) {
                System.err.println("Error restoring stone " + stoneId + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Restores items from save data.
     * Creates item instances based on the saved item states.
     */
    private void restoreItemsFromSave(Map<String, ItemState> savedItems) {
        for (Map.Entry<String, ItemState> entry : savedItems.entrySet()) {
            String itemId = entry.getKey();
            ItemState itemState = entry.getValue();
            
            if (itemState.isCollected()) {
                // Item was collected, don't restore it
                continue;
            }
            
            try {
                switch (itemState.getType()) {
                    case APPLE:
                        Apple apple = new Apple(itemState.getX(), itemState.getY());
                        apples.put(itemId, apple);
                        break;
                        
                    case BANANA:
                        Banana banana = new Banana(itemState.getX(), itemState.getY());
                        bananas.put(itemId, banana);
                        break;
                        
                    case PEBBLE:
                        Pebble pebble = new Pebble(itemState.getX(), itemState.getY());
                        pebbles.put(itemId, pebble);
                        break;
                }
            } catch (Exception e) {
                System.err.println("Error restoring item " + itemId + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Restores planted trees from save data.
     * Creates PlantedTree instances based on the saved planted tree states.
     */
    private void restorePlantedTreesFromSave(Map<String, wagemaker.uk.network.PlantedTreeState> savedPlantedTrees) {
        for (Map.Entry<String, wagemaker.uk.network.PlantedTreeState> entry : savedPlantedTrees.entrySet()) {
            String plantedTreeId = entry.getKey();
            wagemaker.uk.network.PlantedTreeState treeState = entry.getValue();
            
            try {
                PlantedTree plantedTree = new PlantedTree(treeState.getX(), treeState.getY());
                plantedTrees.put(plantedTreeId, plantedTree);
            } catch (Exception e) {
                System.err.println("Error restoring planted tree " + plantedTreeId + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Restores planted bamboos from save data.
     * Creates PlantedBamboo instances based on the saved planted bamboo states.
     */
    private void restorePlantedBamboosFromSave(Map<String, wagemaker.uk.network.PlantedBambooState> savedPlantedBamboos) {
        for (Map.Entry<String, wagemaker.uk.network.PlantedBambooState> entry : savedPlantedBamboos.entrySet()) {
            String plantedBambooId = entry.getKey();
            wagemaker.uk.network.PlantedBambooState bambooState = entry.getValue();
            
            try {
                PlantedBamboo plantedBamboo = new PlantedBamboo(bambooState.getX(), bambooState.getY());
                plantedBamboos.put(plantedBambooId, plantedBamboo);
            } catch (Exception e) {
                System.err.println("Error restoring planted bamboo " + plantedBambooId + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Safely loads a world save using the deferred operations pattern.
     * This method ensures that world loading happens on the render thread to avoid
     * OpenGL context issues while providing validation and rollback capabilities.
     * 
     * @param saveName The name of the save to load
     * @return true if the load was initiated successfully, false otherwise
     */
    public boolean loadWorldSafe(String saveName) {
        if (worldLoadInProgress) {
            System.err.println("World load already in progress");
            return false;
        }
        
        if (saveName == null || saveName.trim().isEmpty()) {
            System.err.println("Invalid save name provided");
            return false;
        }
        
        try {
            // Load the save data (this is safe to do on any thread)
            // Determine if we're in multiplayer mode for loading
            boolean isMultiplayer = (gameMode != GameMode.SINGLEPLAYER);
            WorldSaveData saveData = WorldSaveManager.loadWorld(saveName, isMultiplayer);
            
            if (saveData == null) {
                System.err.println("Failed to load save data for: " + saveName);
                return false;
            }
            
            // Validate the save data before proceeding
            if (!validateWorldSaveData(saveData)) {
                System.err.println("Save data validation failed for: " + saveName);
                return false;
            }
            
            // Create backup of current world state for rollback
            previousWorldState = extractCurrentWorldState();
            
            // Queue the world loading operation for the render thread
            pendingWorldLoad = saveData;
            worldLoadInProgress = true;
            
            System.out.println("World load queued for render thread: " + saveName);
            return true;
            
        } catch (Exception e) {
            System.err.println("Error initiating world load: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Validates world save data before loading.
     * Checks for data integrity and compatibility issues.
     * 
     * @param saveData The save data to validate
     * @return true if the save data is valid, false otherwise
     */
    private boolean validateWorldSaveData(WorldSaveData saveData) {
        if (saveData == null) {
            System.err.println("Save data is null");
            return false;
        }
        
        // Check for required fields
        if (saveData.getSaveName() == null || saveData.getSaveName().trim().isEmpty()) {
            System.err.println("Save data missing save name");
            return false;
        }
        
        // Validate world seed
        if (saveData.getWorldSeed() < 0) {
            System.err.println("Invalid world seed: " + saveData.getWorldSeed());
            return false;
        }
        
        // Validate player position (reasonable bounds check)
        float playerX = saveData.getPlayerX();
        float playerY = saveData.getPlayerY();
        if (Math.abs(playerX) > 1000000 || Math.abs(playerY) > 1000000) {
            System.err.println("Player position out of reasonable bounds: (" + playerX + ", " + playerY + ")");
            return false;
        }
        
        // Validate player health
        float playerHealth = saveData.getPlayerHealth();
        if (playerHealth < 0 || playerHealth > 100) {
            System.err.println("Invalid player health: " + playerHealth);
            return false;
        }
        
        // Validate tree data
        if (saveData.getTrees() != null) {
            for (Map.Entry<String, TreeState> entry : saveData.getTrees().entrySet()) {
                TreeState tree = entry.getValue();
                if (tree.getHealth() < 0 || tree.getHealth() > 100) {
                    System.err.println("Invalid tree health for tree " + entry.getKey() + ": " + tree.getHealth());
                    return false;
                }
            }
        }
        
        // Validate item data
        if (saveData.getItems() != null) {
            for (Map.Entry<String, ItemState> entry : saveData.getItems().entrySet()) {
                ItemState item = entry.getValue();
                if (item.getType() == null) {
                    System.err.println("Invalid item type for item " + entry.getKey());
                    return false;
                }
            }
        }
        
        System.out.println("Save data validation passed for: " + saveData.getSaveName());
        return true;
    }
    
    /**
     * Processes pending world load on the render thread.
     * This method is called during the render loop to safely execute world loading
     * operations that require OpenGL context.
     */
    private void processPendingWorldLoad() {
        if (!worldLoadInProgress || pendingWorldLoad == null) {
            return;
        }
        
        try {
            System.out.println("Processing world load on render thread: " + pendingWorldLoad.getSaveName());
            
            // Perform the actual world state restoration
            boolean success = restoreWorldState(pendingWorldLoad);
            
            if (success) {
                System.out.println("World load completed successfully: " + pendingWorldLoad.getSaveName());
                displayNotification("World loaded: " + pendingWorldLoad.getSaveName());
                
                // Clear the backup since load was successful
                previousWorldState = null;
            } else {
                System.err.println("World load failed, initiating rollback");
                rollbackWorldLoad();
            }
            
        } catch (Exception e) {
            System.err.println("Error during world load processing: " + e.getMessage());
            e.printStackTrace();
            rollbackWorldLoad();
        } finally {
            // Clean up loading state
            pendingWorldLoad = null;
            worldLoadInProgress = false;
        }
    }
    
    /**
     * Rolls back a failed world load operation.
     * Restores the previous world state to ensure the game remains in a consistent state.
     */
    private void rollbackWorldLoad() {
        if (previousWorldState == null) {
            System.err.println("Cannot rollback: no previous world state available");
            displayNotification("World load failed - no backup available");
            return;
        }
        
        try {
            System.out.println("Rolling back failed world load...");
            
            // Create a temporary WorldSaveData from the previous state for restoration
            WorldSaveData rollbackData = createSaveDataFromWorldState(previousWorldState);
            
            // Restore the previous state
            boolean rollbackSuccess = restoreWorldState(rollbackData);
            
            if (rollbackSuccess) {
                System.out.println("Rollback completed successfully");
                displayNotification("World load failed - restored previous state");
            } else {
                System.err.println("Rollback failed - game state may be inconsistent");
                displayNotification("Critical error - world state corrupted");
            }
            
        } catch (Exception e) {
            System.err.println("Error during rollback: " + e.getMessage());
            e.printStackTrace();
            displayNotification("Critical error during rollback");
        } finally {
            // Clear the backup
            previousWorldState = null;
        }
    }
    
    /**
     * Creates WorldSaveData from a WorldState for rollback purposes.
     * This is a utility method to convert WorldState back to WorldSaveData format.
     */
    private WorldSaveData createSaveDataFromWorldState(WorldState worldState) {
        WorldSaveData saveData = new WorldSaveData();
        
        saveData.setSaveName("__rollback__");
        saveData.setWorldSeed(worldState.getWorldSeed());
        saveData.setTrees(worldState.getTrees());
        saveData.setStones(worldState.getStones());
        saveData.setItems(worldState.getItems());
        saveData.setClearedPositions(worldState.getClearedPositions());
        saveData.setRainZones(worldState.getRainZones());
        saveData.setSaveTimestamp(System.currentTimeMillis());
        saveData.setGameMode(gameMode == GameMode.SINGLEPLAYER ? "singleplayer" : "multiplayer");
        
        // Use current player position and health for rollback
        if (player != null) {
            saveData.setPlayerX(player.getX());
            saveData.setPlayerY(player.getY());
            saveData.setPlayerHealth(player.getHealth());
        }
        
        return saveData;
    }
    
    /**
     * Checks if a world load operation is currently in progress.
     * 
     * @return true if world loading is in progress, false otherwise
     */
    public boolean isWorldLoadInProgress() {
        return worldLoadInProgress;
    }
    
    /**
     * Loads a world save while preserving the existing player position save functionality.
     * This method ensures that world saves and player position saves remain separate systems.
     * After loading the world, it updates the player position using the existing system.
     * 
     * @param saveName The name of the world save to load
     * @return true if the load was initiated successfully, false otherwise
     */
    public boolean loadWorldPreservingPlayerPosition(String saveName) {
        if (worldLoadInProgress) {
            System.err.println("World load already in progress");
            return false;
        }
        
        // Save current player position before loading world (preserves existing system)
        if (gameMenu != null) {
            gameMenu.savePlayerPosition();
            System.out.println("Saved current player position before world load");
        }
        
        // Initiate the world load
        boolean loadInitiated = loadWorldSafe(saveName);
        
        if (loadInitiated) {
            // Schedule player position update after world load completes
            deferOperation(() -> updatePlayerPositionAfterWorldLoad());
        }
        
        return loadInitiated;
    }
    
    /**
     * Updates player position after world load completion.
     * This method is called after a world load to ensure the player position
     * is properly restored using the existing player position save system.
     */
    private void updatePlayerPositionAfterWorldLoad() {
        if (gameMenu != null && player != null) {
            // Load the appropriate player position based on current game mode
            // This uses the existing loadPlayerPosition system which handles
            // separate singleplayer/multiplayer positions
            boolean positionLoaded = gameMenu.loadPlayerPosition();
            
            if (positionLoaded) {
                System.out.println("Player position updated after world load using existing save system");
            } else {
                System.out.println("No saved player position found, keeping world save position");
                // If no position save exists, the player keeps the position from the world save
                // This maintains backward compatibility
            }
        }
    }
    
    /**
     * Saves the current world state while preserving player position save separation.
     * This method ensures that world saves don't interfere with the existing
     * player position save functionality.
     * 
     * @param saveName The name to save the world as
     * @return true if the save was successful, false otherwise
     */
    public boolean saveWorldPreservingPlayerPosition(String saveName) {
        if (saveName == null || saveName.trim().isEmpty()) {
            System.err.println("Invalid save name provided");
            return false;
        }
        
        try {
            // Extract current world state
            WorldState currentState = extractCurrentWorldState();
            
            // Create save data from world state
            WorldSaveData saveData = new WorldSaveData();
            saveData.setSaveName(saveName);
            saveData.setWorldSeed(currentState.getWorldSeed());
            saveData.setTrees(currentState.getTrees());
            saveData.setItems(currentState.getItems());
            saveData.setClearedPositions(currentState.getClearedPositions());
            saveData.setRainZones(currentState.getRainZones());
            saveData.setSaveTimestamp(System.currentTimeMillis());
            
            // Set game mode
            String gameMode = this.gameMode == GameMode.SINGLEPLAYER ? "singleplayer" : "multiplayer";
            saveData.setGameMode(gameMode);
            
            // Include current player position in world save (for world restoration)
            // but don't interfere with the separate player position save system
            if (player != null) {
                saveData.setPlayerX(player.getX());
                saveData.setPlayerY(player.getY());
                saveData.setPlayerHealth(player.getHealth());
            }
            
            // Save the world using WorldSaveManager
            // Determine if we're in multiplayer mode for saving
            boolean isMultiplayer = (this.gameMode != GameMode.SINGLEPLAYER);
            
            // Get current inventory from inventory manager
            wagemaker.uk.inventory.Inventory currentInventory = null;
            if (inventoryManager != null) {
                currentInventory = inventoryManager.getCurrentInventory();
            }
            
            boolean saveSuccess = WorldSaveManager.saveWorld(
                saveName, 
                currentState, 
                saveData.getPlayerX(), 
                saveData.getPlayerY(), 
                saveData.getPlayerHealth(),
                currentInventory,
                isMultiplayer
            );
            
            if (saveSuccess) {
                System.out.println("World saved successfully: " + saveName);
                displayNotification("World saved: " + saveName);
                
                // Also save current player position using existing system
                // This maintains the separation between world saves and player position saves
                if (gameMenu != null) {
                    gameMenu.savePlayerPosition();
                    System.out.println("Player position saved separately using existing system");
                }
                
                return true;
            } else {
                System.err.println("Failed to save world: " + saveName);
                displayNotification("Failed to save world: " + saveName);
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Error saving world: " + e.getMessage());
            e.printStackTrace();
            displayNotification("Error saving world: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Queues a player join to be processed on the main thread.
     * This is necessary because RemotePlayer creation involves OpenGL operations.
     * 
     * @param message The player join message
     */
    public void queuePlayerJoin(wagemaker.uk.network.PlayerJoinMessage message) {
        pendingPlayerJoins.offer(message);
    }
    
    /**
     * Queues a player leave to be processed on the main thread.
     * 
     * @param playerId The ID of the player leaving
     */
    public void queuePlayerLeave(String playerId) {
        pendingPlayerLeaves.offer(playerId);
    }
    
    /**
     * Processes pending player joins on the main render thread.
     * This ensures OpenGL operations happen in the correct context.
     */
    private void processPendingPlayerJoins() {
        wagemaker.uk.network.PlayerJoinMessage message;
        while ((message = pendingPlayerJoins.poll()) != null) {
            String playerId = message.getPlayerId();
            String playerName = message.getPlayerName();
            
            // Don't create a remote player for ourselves
            if (gameClient != null && playerId.equals(gameClient.getClientId())) {
                continue;
            }
            
            // Create new remote player (safe on main thread)
            // Default direction is DOWN since PlayerJoinMessage doesn't include direction
            RemotePlayer remotePlayer = new RemotePlayer(
                playerId, 
                playerName, 
                message.getX(), 
                message.getY(),
                wagemaker.uk.network.Direction.DOWN,
                100.0f,
                false
            );
            
            remotePlayers.put(playerId, remotePlayer);
            
            // Display join notification
            System.out.println("Player joined: " + playerName);
            displayNotification(playerName + " joined the game");
        }
    }
    
    /**
     * Processes pending player leaves on the main render thread.
     */
    private void processPendingPlayerLeaves() {
        String playerId;
        while ((playerId = pendingPlayerLeaves.poll()) != null) {
            RemotePlayer remotePlayer = remotePlayers.remove(playerId);
            if (remotePlayer != null) {
                remotePlayer.dispose();
                System.out.println("Player left: " + remotePlayer.getPlayerName());
                displayNotification(remotePlayer.getPlayerName() + " left the game");
            }
        }
    }
    
    /**
     * Processes pending item spawns on the main render thread.
     * This ensures OpenGL operations happen in the correct context.
     */
    private void processPendingItemSpawns() {
        ItemState itemState;
        while ((itemState = pendingItemSpawns.poll()) != null) {
            String itemId = itemState.getItemId();
            ItemType type = itemState.getType();
            float x = itemState.getX();
            float y = itemState.getY();
            
            // Create item on main thread (safe for OpenGL context)
            switch (type) {
                case APPLE:
                    if (!apples.containsKey(itemId)) {
                        apples.put(itemId, new Apple(x, y));
                    }
                    break;
                case BANANA:
                    if (!bananas.containsKey(itemId)) {
                        bananas.put(itemId, new Banana(x, y));
                    }
                    break;
                case BAMBOO_STACK:
                    if (!bambooStacks.containsKey(itemId)) {
                        bambooStacks.put(itemId, new BambooStack(x, y));
                    }
                    break;
                case BABY_BAMBOO:
                    if (!babyBamboos.containsKey(itemId)) {
                        babyBamboos.put(itemId, new BabyBamboo(x, y));
                    }
                    break;
                case BABY_TREE:
                    if (!babyTrees.containsKey(itemId)) {
                        babyTrees.put(itemId, new BabyTree(x, y));
                    }
                    break;
                case WOOD_STACK:
                    if (!woodStacks.containsKey(itemId)) {
                        woodStacks.put(itemId, new WoodStack(x, y));
                    }
                    break;
                case PEBBLE:
                    if (!pebbles.containsKey(itemId)) {
                        pebbles.put(itemId, new Pebble(x, y));
                    }
                    break;
                case PALM_FIBER:
                    if (!palmFibers.containsKey(itemId)) {
                        palmFibers.put(itemId, new PalmFiber(x, y));
                    }
                    break;
            }
        }
    }
    
    /**
     * Processes pending tree removals on the main render thread.
     * This ensures OpenGL operations happen in the correct context.
     */
    private void processPendingTreeRemovals() {
        String treeId;
        while ((treeId = pendingTreeRemovals.poll()) != null) {
            removeTreeImmediate(treeId);
        }
    }
    
    /**
     * Processes pending tree creations on the main render thread.
     * This ensures OpenGL operations happen in the correct context.
     */
    private void processPendingTreeCreations() {
        TreeState treeState;
        while ((treeState = pendingTreeCreations.poll()) != null) {
            String treeId = treeState.getTreeId();
            TreeType type = treeState.getType();
            float x = treeState.getX();
            float y = treeState.getY();
            float health = treeState.getHealth();
            
            // Create tree on main thread (safe for OpenGL context)
            switch (type) {
                case SMALL:
                    if (!trees.containsKey(treeId)) {
                        SmallTree smallTree = new SmallTree(x, y);
                        smallTree.setHealth(health);
                        trees.put(treeId, smallTree);
                    }
                    break;
                case APPLE:
                    if (!appleTrees.containsKey(treeId)) {
                        AppleTree appleTree = new AppleTree(x, y);
                        appleTree.setHealth(health);
                        appleTrees.put(treeId, appleTree);
                    }
                    break;
                case COCONUT:
                    if (!coconutTrees.containsKey(treeId)) {
                        CoconutTree coconutTree = new CoconutTree(x, y);
                        coconutTree.setHealth(health);
                        coconutTrees.put(treeId, coconutTree);
                    }
                    break;
                case BAMBOO:
                    if (!bambooTrees.containsKey(treeId)) {
                        BambooTree bambooTree = new BambooTree(x, y);
                        bambooTree.setHealth(health);
                        bambooTrees.put(treeId, bambooTree);
                    }
                    break;
                case BANANA:
                    if (!bananaTrees.containsKey(treeId)) {
                        BananaTree bananaTree = new BananaTree(x, y);
                        bananaTree.setHealth(health);
                        bananaTrees.put(treeId, bananaTree);
                    }
                    break;
            }
        }
    }

    /**
     * Queues a bamboo plant to be processed on the main thread.
     * @param message The bamboo plant message
     */
    public void queueBambooPlant(wagemaker.uk.network.BambooPlantMessage message) {
        pendingBambooPlants.offer(message);
    }
    
    /**
     * Queues a bamboo transform to be processed on the main thread.
     * @param message The bamboo transform message
     */
    public void queueBambooTransform(wagemaker.uk.network.BambooTransformMessage message) {
        pendingBambooTransforms.offer(message);
    }
    
    /**
     * Queues a tree plant to be processed on the main thread.
     * @param message The tree plant message
     */
    public void queueTreePlant(wagemaker.uk.network.TreePlantMessage message) {
        pendingTreePlants.offer(message);
    }
    
    /**
     * Queues a tree transform to be processed on the main thread.
     * @param message The tree transform message
     */
    public void queueTreeTransform(wagemaker.uk.network.TreeTransformMessage message) {
        pendingTreeTransforms.offer(message);
    }
    
    /**
     * Processes pending bamboo plants on the main render thread.
     * This ensures OpenGL operations happen in the correct context.
     */
    private void processPendingBambooPlants() {
        int processedCount = 0;
        wagemaker.uk.network.BambooPlantMessage message;
        while ((message = pendingBambooPlants.poll()) != null) {
            processedCount++;
            String plantedBambooId = message.getPlantedBambooId();
            float x = message.getX();
            float y = message.getY();
            
            System.out.println("[MyGdxGame] Processing pending bamboo plant #" + processedCount + ":");
            System.out.println("  - Planted Bamboo ID: " + plantedBambooId);
            System.out.println("  - Position: (" + x + ", " + y + ")");
            System.out.println("  - Already exists: " + plantedBamboos.containsKey(plantedBambooId));
            System.out.println("  - Current plantedBamboos count: " + plantedBamboos.size());
            
            // Create planted bamboo if it doesn't exist
            if (!plantedBamboos.containsKey(plantedBambooId)) {
                PlantedBamboo plantedBamboo = new PlantedBamboo(x, y);
                Texture texture = plantedBamboo.getTexture();
                System.out.println("  - PlantedBamboo created, texture is: " + (texture != null ? "valid" : "NULL"));
                
                plantedBamboos.put(plantedBambooId, plantedBamboo);
                System.out.println("  - SUCCESS: Remote player planted bamboo at: " + plantedBambooId);
                System.out.println("  - New plantedBamboos count: " + plantedBamboos.size());
            } else {
                System.out.println("  - SKIPPED: Bamboo already exists at this location");
            }
        }
        
        if (processedCount > 0) {
            System.out.println("[MyGdxGame] Processed " + processedCount + " bamboo plants this frame. Total in map: " + plantedBamboos.size());
        }
    }
    
    /**
     * Processes pending bamboo transforms on the main render thread.
     * This ensures OpenGL operations happen in the correct context.
     */
    private void processPendingBambooTransforms() {
        wagemaker.uk.network.BambooTransformMessage message;
        while ((message = pendingBambooTransforms.poll()) != null) {
            String plantedBambooId = message.getPlantedBambooId();
            String bambooTreeId = message.getBambooTreeId();
            float x = message.getX();
            float y = message.getY();
            
            // Remove planted bamboo
            PlantedBamboo plantedBamboo = plantedBamboos.remove(plantedBambooId);
            if (plantedBamboo != null) {
                plantedBamboo.dispose();
            }
            
            // Create bamboo tree
            if (!bambooTrees.containsKey(bambooTreeId)) {
                BambooTree bambooTree = new BambooTree(x, y);
                bambooTrees.put(bambooTreeId, bambooTree);
                System.out.println("Bamboo transformed to tree: " + bambooTreeId);
            }
        }
    }
    
    /**
     * Processes pending tree plants on the main render thread.
     * This ensures OpenGL operations happen in the correct context.
     */
    private void processPendingTreePlants() {
        int processedCount = 0;
        wagemaker.uk.network.TreePlantMessage message;
        while ((message = pendingTreePlants.poll()) != null) {
            processedCount++;
            String plantedTreeId = message.getPlantedTreeId();
            float x = message.getX();
            float y = message.getY();
            
            System.out.println("[MyGdxGame] Processing pending tree plant #" + processedCount + ":");
            System.out.println("  - Planted Tree ID: " + plantedTreeId);
            System.out.println("  - Position: (" + x + ", " + y + ")");
            System.out.println("  - Already exists: " + plantedTrees.containsKey(plantedTreeId));
            System.out.println("  - Current plantedTrees count: " + plantedTrees.size());
            
            // Create planted tree if it doesn't exist
            if (!plantedTrees.containsKey(plantedTreeId)) {
                PlantedTree plantedTree = new PlantedTree(x, y);
                Texture texture = plantedTree.getTexture();
                System.out.println("  - PlantedTree created, texture is: " + (texture != null ? "valid" : "NULL"));
                
                plantedTrees.put(plantedTreeId, plantedTree);
                System.out.println("  - SUCCESS: Remote player planted tree at: " + plantedTreeId);
                System.out.println("  - New plantedTrees count: " + plantedTrees.size());
            } else {
                System.out.println("  - SKIPPED: Tree already exists at this location");
            }
        }
        
        if (processedCount > 0) {
            System.out.println("[MyGdxGame] Processed " + processedCount + " tree plants this frame. Total in map: " + plantedTrees.size());
        }
    }
    
    /**
     * Processes pending tree transforms on the main render thread.
     * This ensures OpenGL operations happen in the correct context.
     */
    private void processPendingTreeTransforms() {
        wagemaker.uk.network.TreeTransformMessage message;
        while ((message = pendingTreeTransforms.poll()) != null) {
            String plantedTreeId = message.getPlantedTreeId();
            String smallTreeId = message.getSmallTreeId();
            float x = message.getX();
            float y = message.getY();
            
            // Remove planted tree
            PlantedTree plantedTree = plantedTrees.remove(plantedTreeId);
            if (plantedTree != null) {
                plantedTree.dispose();
            }
            
            // Create small tree
            if (!trees.containsKey(smallTreeId)) {
                SmallTree smallTree = new SmallTree(x, y);
                trees.put(smallTreeId, smallTree);
                System.out.println("Tree transformed from planted to small tree: " + smallTreeId);
            }
        }
    }

    /**
     * Gets the inventory manager instance.
     * @return The inventory manager
     */
    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }
    
    /**
     * Gets the planted trees map.
     * @return The planted trees map
     */
    public Map<String, PlantedTree> getPlantedTrees() {
        return plantedTrees;
    }
    
    /**
     * Creates a tree for respawn system.
     * This method is called by RespawnManager when a tree needs to be respawned.
     * Must be called on the render thread (via deferOperation).
     * 
     * @param treeId Unique identifier for the tree
     * @param treeType Type of tree to create
     * @param x World X coordinate
     * @param y World Y coordinate
     */
    public void createTreeForRespawn(String treeId, TreeType treeType, float x, float y) {
        // Check if tree already exists (avoid duplicates)
        switch (treeType) {
            case SMALL:
                if (!trees.containsKey(treeId)) {
                    SmallTree smallTree = new SmallTree(x, y);
                    trees.put(treeId, smallTree);
                }
                break;
            case APPLE:
                if (!appleTrees.containsKey(treeId)) {
                    AppleTree appleTree = new AppleTree(x, y);
                    appleTrees.put(treeId, appleTree);
                }
                break;
            case COCONUT:
                if (!coconutTrees.containsKey(treeId)) {
                    CoconutTree coconutTree = new CoconutTree(x, y);
                    coconutTrees.put(treeId, coconutTree);
                }
                break;
            case BAMBOO:
                if (!bambooTrees.containsKey(treeId)) {
                    BambooTree bambooTree = new BambooTree(x, y);
                    bambooTrees.put(treeId, bambooTree);
                }
                break;
            case BANANA:
                if (!bananaTrees.containsKey(treeId)) {
                    BananaTree bananaTree = new BananaTree(x, y);
                    bananaTrees.put(treeId, bananaTree);
                }
                break;
            case CACTUS:
                // Cactus is handled separately as a single instance
                if (cactus == null) {
                    cactus = new Cactus(x, y);
                    player.setCactus(cactus);
                    System.out.println("[MyGdxGame] Cactus respawned at: " + x + ", " + y);
                }
                break;
        }
    }
    
    /**
     * Creates a stone for respawn system.
     * This method is called by RespawnManager when a stone needs to be respawned.
     * Must be called on the render thread (via deferOperation).
     * 
     * @param stoneId Unique identifier for the stone
     * @param x World X coordinate
     * @param y World Y coordinate
     */
    public void createStoneForRespawn(String stoneId, float x, float y) {
        // Check if stone already exists (avoid duplicates)
        if (!stones.containsKey(stoneId) && !stoneMap.containsKey(stoneId)) {
            Stone stone = new Stone(x, y);
            stones.put(stoneId, stone);
            stoneMap.put(stoneId, stone);
            // Remove from cleared positions so it can be interacted with again
            clearedPositions.remove(stoneId);
        }
    }
    
    /**
     * Generates a unique resource ID for a tree or stone.
     * Uses the resource's position as the key (same format as existing tree/stone keys).
     * 
     * @param x World X coordinate
     * @param y World Y coordinate
     * @return Unique resource identifier
     */
    public String generateResourceId(float x, float y) {
        // Use grid-aligned coordinates for consistency with existing tree generation
        int gridX = (int)(x / 64) * 64;
        int gridY = (int)(y / 64) * 64;
        return gridX + "," + gridY;
    }
    
    /**
     * Determines the TreeType for a given tree object.
     * 
     * @param tree The tree object (SmallTree, AppleTree, etc.)
     * @return The corresponding TreeType enum value
     */
    public TreeType getTreeType(Object tree) {
        if (tree instanceof wagemaker.uk.trees.SmallTree) {
            return TreeType.SMALL;
        } else if (tree instanceof wagemaker.uk.trees.AppleTree) {
            return TreeType.APPLE;
        } else if (tree instanceof wagemaker.uk.trees.CoconutTree) {
            return TreeType.COCONUT;
        } else if (tree instanceof wagemaker.uk.trees.BambooTree) {
            return TreeType.BAMBOO;
        } else if (tree instanceof wagemaker.uk.trees.BananaTree) {
            return TreeType.BANANA;
        } else if (tree instanceof wagemaker.uk.trees.Cactus) {
            return TreeType.CACTUS;
        }
        return null;
    }
    
    /**
     * Gets the RespawnManager instance.
     * 
     * @return The respawn manager
     */
    public RespawnManager getRespawnManager() {
        return respawnManager;
    }
    
    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        player.dispose();
        
        // Dispose biome manager
        if (biomeManager != null) {
            biomeManager.dispose();
        }
        
        for (SmallTree tree : trees.values()) {
            tree.dispose();
        }
        for (AppleTree appleTree : appleTrees.values()) {
            appleTree.dispose();
        }
        for (CoconutTree coconutTree : coconutTrees.values()) {
            coconutTree.dispose();
        }
        for (BambooTree bambooTree : bambooTrees.values()) {
            bambooTree.dispose();
        }
        for (BananaTree bananaTree : bananaTrees.values()) {
            bananaTree.dispose();
        }
        for (Apple apple : apples.values()) {
            apple.dispose();
        }
        for (Banana banana : bananas.values()) {
            banana.dispose();
        }
        for (PlantedBamboo planted : plantedBamboos.values()) {
            planted.dispose();
        }
        for (PlantedTree planted : plantedTrees.values()) {
            planted.dispose();
        }
        for (Stone stone : stones.values()) {
            stone.dispose();
        }
        for (Pebble pebble : pebbles.values()) {
            pebble.dispose();
        }
        for (PalmFiber palmFiber : palmFibers.values()) {
            palmFiber.dispose();
        }
        if (cactus != null) {
            cactus.dispose();
        }
        gameMenu.dispose();
        
        // Dispose compass
        if (compass != null) {
            compass.dispose();
        }
        
        // Dispose health bar UI
        if (healthBarUI != null) {
            healthBarUI.dispose();
        }
        
        // Dispose inventory renderer
        if (inventoryRenderer != null) {
            inventoryRenderer.dispose();
        }
        
        // Dispose rain system
        if (rainSystem != null) {
            rainSystem.dispose();
        }
        
        // Clean up multiplayer resources
        if (gameClient != null) {
            gameClient.disconnect();
            gameClient = null;
        }
        
        if (gameServer != null) {
            gameServer.stop();
            gameServer = null;
        }
        
        // Dispose remote players
        for (RemotePlayer remotePlayer : remotePlayers.values()) {
            remotePlayer.dispose();
        }
        remotePlayers.clear();
        
        // Dispose respawn indicators
        if (respawnManager != null) {
            respawnManager.disposeIndicators();
        }
        
        // Dispose shared textures
        PlantedBamboo.disposeSharedTexture();
        PlantedTree.disposeSharedTexture();
    }
}
