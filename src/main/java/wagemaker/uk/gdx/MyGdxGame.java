package wagemaker.uk.gdx;

import java.util.HashMap;
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

import wagemaker.uk.player.Player;
import wagemaker.uk.player.RemotePlayer;
import wagemaker.uk.items.Apple;
import wagemaker.uk.items.Banana;
import wagemaker.uk.trees.AppleTree;
import wagemaker.uk.trees.BambooTree;
import wagemaker.uk.trees.BananaTree;
import wagemaker.uk.trees.CoconutTree;
import wagemaker.uk.trees.SmallTree;
import wagemaker.uk.trees.Cactus;
import wagemaker.uk.ui.GameMenu;
import wagemaker.uk.network.GameServer;
import wagemaker.uk.network.GameClient;
import wagemaker.uk.network.WorldState;
import wagemaker.uk.network.TreeState;
import wagemaker.uk.network.ItemState;
import wagemaker.uk.network.TreeType;
import wagemaker.uk.network.ItemType;

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
    Texture grassTexture;
    Player player;
    OrthographicCamera camera;
    Viewport viewport;
    Map<String, SmallTree> trees;
    Map<String, AppleTree> appleTrees;
    Map<String, CoconutTree> coconutTrees;
    Map<String, BambooTree> bambooTrees;
    Map<String, BananaTree> bananaTrees;
    Map<String, Apple> apples;
    Map<String, Banana> bananas;
    Cactus cactus; // Single cactus near spawn
    Map<String, Boolean> clearedPositions;
    Random random;
    long worldSeed; // World seed for deterministic generation
    GameMenu gameMenu;
    
    // Multiplayer fields
    private GameMode gameMode;
    private GameServer gameServer;  // Only used in MULTIPLAYER_HOST mode
    private GameClient gameClient;  // Used in both MULTIPLAYER_HOST and MULTIPLAYER_CLIENT modes
    private Map<String, RemotePlayer> remotePlayers;  // Other players in multiplayer
    private wagemaker.uk.ui.ConnectionQualityIndicator connectionQualityIndicator;
    
    // Notification system
    private String currentNotification;
    private float notificationTimer;
    
    // Connection state
    private String pendingConnectionAddress;
    private int pendingConnectionPort;
    private boolean isHosting;
    
    // Camera dimensions for infinite world
    static final int CAMERA_WIDTH = 1280;
    static final int CAMERA_HEIGHT = 1024;

    @Override
    public void create() {
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
        clearedPositions = new HashMap<>();
        remotePlayers = new HashMap<>();
        random = new Random();
        worldSeed = 0; // Will be set by server in multiplayer, or remain 0 for single-player

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
        player.setCactus(cactus);
        player.setGameInstance(this);
        player.setClearedPositions(clearedPositions);

        gameMenu = new GameMenu();
        gameMenu.setPlayer(player); // Set player reference for saving
        gameMenu.setGameInstance(this); // Set game instance reference for multiplayer
        player.setGameMenu(gameMenu);
        
        // Load player position from save file if it exists
        gameMenu.loadPlayerPosition();
        
        // Initialize connection quality indicator (will be set when connecting)
        connectionQualityIndicator = new wagemaker.uk.ui.ConnectionQualityIndicator(null, gameMenu.getFont());

        // create realistic grass texture
        grassTexture = createRealisticGrassTexture();
        grassTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);


    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        gameMenu.update();
        
        // Handle error dialog actions
        if (gameMenu.getErrorDialog().isRetrySelected()) {
            gameMenu.getErrorDialog().reset();
            retryConnection();
        } else if (gameMenu.getErrorDialog().isCancelled()) {
            gameMenu.getErrorDialog().reset();
            cancelConnection();
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
        
        // update cactus
        if (cactus != null) {
            cactus.update(deltaTime);
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
        // draw trees
        drawTrees();
        drawCoconutTrees();
        drawBambooTrees();
        drawApples();
        drawBananas();
        drawCactus();
        // draw player before apple trees so foliage appears in front
        batch.draw(player.getCurrentFrame(), player.getX(), player.getY(), 100, 100);
        // draw remote players at same z-order as local player
        renderRemotePlayers();
        drawAppleTrees();
        drawBananaTrees();
        batch.end();
        
        // draw player name tag above player
        gameMenu.renderPlayerNameTag(batch);
        
        // draw remote player name tags in multiplayer mode
        if (gameMode != GameMode.SINGLEPLAYER) {
            renderRemotePlayerNameTags();
        }
        
        // draw health bars
        drawHealthBars();
        
        // draw connection quality indicator in multiplayer mode
        if (gameMode != GameMode.SINGLEPLAYER && connectionQualityIndicator != null) {
            float screenX = camera.position.x + viewport.getWorldWidth() / 2 - 20;
            float screenY = camera.position.y + viewport.getWorldHeight() / 2 - 20;
            connectionQualityIndicator.render(batch, shapeRenderer, screenX, screenY);
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
                batch.draw(grassTexture, x, y, 64, 64);
                // randomly generate trees
                generateTreeAt(x, y);
            }
        }
    }
    
    private void generateTreeAt(int x, int y) {
        String key = x + "," + y;
        if (!trees.containsKey(key) && !appleTrees.containsKey(key) && !coconutTrees.containsKey(key) && !bambooTrees.containsKey(key) && !bananaTrees.containsKey(key) && !clearedPositions.containsKey(key)) {
            // 2% chance to generate a tree at this grass tile
            // Use world seed combined with position for deterministic generation across clients
            random.setSeed(worldSeed + x * 31L + y * 17L); // deterministic based on world seed and position
            if (random.nextFloat() < 0.02f) {
                // Check if any tree is within 256px distance
                if (isTreeNearby(x, y, 256)) {
                    return;
                }
                
                // Don't spawn trees within player's visible area
                if (isWithinPlayerView(x, y)) {
                    return;
                }
                
                // 20% chance each for small tree, apple tree, coconut tree, bamboo tree, banana tree
                float treeType = random.nextFloat();
                if (treeType < 0.2f) {
                    trees.put(key, new SmallTree(x, y));
                } else if (treeType < 0.4f) {
                    appleTrees.put(key, new AppleTree(x, y));
                } else if (treeType < 0.6f) {
                    coconutTrees.put(key, new CoconutTree(x, y));
                } else if (treeType < 0.8f) {
                    bambooTrees.put(key, new BambooTree(x, y));
                } else {
                    bananaTrees.put(key, new BananaTree(x, y));
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

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    private void drawHealthBars() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Draw remote player health bars in multiplayer mode
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
        
        // Draw player health bar (fixed position on screen)
        if (player.shouldShowHealthBar()) {
            // Health bar in top-left corner of screen
            float screenX = camera.position.x - viewport.getWorldWidth() / 2 + 20;
            float screenY = camera.position.y + viewport.getWorldHeight() / 2 - 40;
            float barWidth = 200;
            float barHeight = 20;
            
            // Black background
            shapeRenderer.setColor(0, 0, 0, 0.8f);
            shapeRenderer.rect(screenX - 2, screenY - 2, barWidth + 4, barHeight + 4);
            
            // Red background (full bar)
            shapeRenderer.setColor(0.8f, 0.2f, 0.2f, 1);
            shapeRenderer.rect(screenX, screenY, barWidth, barHeight);
            
            // Green foreground (current health)
            float healthPercent = player.getHealthPercentage();
            shapeRenderer.setColor(0.2f, 0.8f, 0.2f, 1);
            shapeRenderer.rect(screenX, screenY, barWidth * healthPercent, barHeight);
            
            // Health text would go here if we had font rendering
        }
        
        shapeRenderer.end();
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

    private Texture createRealisticGrassTexture() {
        Pixmap grassPixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        Random grassRandom = new Random(12345); // Fixed seed for consistent grass pattern
        
        // Base grass colors (different shades of green)
        float[] baseGreen = {0.15f, 0.5f, 0.08f, 1.0f}; // Dark green
        float[] lightGreen = {0.25f, 0.65f, 0.15f, 1.0f}; // Light green
        float[] mediumGreen = {0.2f, 0.55f, 0.12f, 1.0f}; // Medium green
        float[] brownish = {0.3f, 0.4f, 0.1f, 1.0f}; // Brownish green (dirt patches)
        
        // Fill with base grass color
        grassPixmap.setColor(baseGreen[0], baseGreen[1], baseGreen[2], baseGreen[3]);
        grassPixmap.fill();
        
        // Add grass blade patterns and texture variations
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                float noise = grassRandom.nextFloat();
                
                // Create grass blade patterns (vertical lines with variations)
                if (x % 3 == 0 && noise > 0.3f) {
                    // Lighter grass blades
                    grassPixmap.setColor(lightGreen[0], lightGreen[1], lightGreen[2], lightGreen[3]);
                    grassPixmap.drawPixel(x, y);
                    if (y > 0 && grassRandom.nextFloat() > 0.5f) {
                        grassPixmap.drawPixel(x, y - 1); // Extend blade
                    }
                } else if (x % 4 == 1 && noise > 0.6f) {
                    // Medium grass blades
                    grassPixmap.setColor(mediumGreen[0], mediumGreen[1], mediumGreen[2], mediumGreen[3]);
                    grassPixmap.drawPixel(x, y);
                } else if (noise > 0.85f) {
                    // Random dirt/brown patches for realism
                    grassPixmap.setColor(brownish[0], brownish[1], brownish[2], brownish[3]);
                    grassPixmap.drawPixel(x, y);
                } else if (noise > 0.75f) {
                    // Darker grass areas (shadows)
                    grassPixmap.setColor(baseGreen[0] * 0.8f, baseGreen[1] * 0.8f, baseGreen[2] * 0.8f, baseGreen[3]);
                    grassPixmap.drawPixel(x, y);
                }
            }
        }
        
        // Add some scattered small details (seeds, small stones, etc.)
        for (int i = 0; i < 8; i++) {
            int x = grassRandom.nextInt(64);
            int y = grassRandom.nextInt(64);
            
            if (grassRandom.nextFloat() > 0.5f) {
                // Small brown spots (seeds/dirt)
                grassPixmap.setColor(0.4f, 0.3f, 0.2f, 1.0f);
                grassPixmap.drawPixel(x, y);
            } else {
                // Tiny light spots (small flowers/highlights)
                grassPixmap.setColor(0.6f, 0.8f, 0.3f, 1.0f);
                grassPixmap.drawPixel(x, y);
            }
        }
        
        // Add some diagonal grass patterns for more natural look
        for (int i = 0; i < 64; i += 8) {
            for (int j = 0; j < 64; j += 6) {
                if (grassRandom.nextFloat() > 0.4f) {
                    // Diagonal grass blade pattern
                    grassPixmap.setColor(lightGreen[0], lightGreen[1], lightGreen[2], lightGreen[3]);
                    if (i + 1 < 64 && j + 1 < 64) {
                        grassPixmap.drawPixel(i, j);
                        grassPixmap.drawPixel(i + 1, j + 1);
                    }
                }
            }
        }
        
        Texture texture = new Texture(grassPixmap);
        grassPixmap.dispose();
        return texture;
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
            
            // Update connection quality indicator
            connectionQualityIndicator.setGameClient(gameClient);
            
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
            // Create and connect the game client
            gameClient = new GameClient();
            gameClient.setMessageHandler(new GameMessageHandler(this));
            gameClient.connect(serverAddress, port);
            
            // Set game mode to client
            gameMode = GameMode.MULTIPLAYER_CLIENT;
            
            // Update connection quality indicator
            connectionQualityIndicator.setGameClient(gameClient);
            
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
        
        // Sync tree states
        if (state.getTrees() != null) {
            for (TreeState treeState : state.getTrees().values()) {
                updateTreeFromState(treeState);
            }
        }
        
        // Sync item states
        if (state.getItems() != null) {
            for (ItemState itemState : state.getItems().values()) {
                updateItemFromState(itemState);
            }
        }
        
        System.out.println("World state synchronized");
    }
    
    /**
     * Updates or creates a tree based on server tree state.
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
        
        switch (type) {
            case SMALL:
                SmallTree smallTree = trees.get(treeId);
                if (smallTree != null) {
                    smallTree.setHealth(health);
                }
                break;
            case APPLE:
                AppleTree appleTree = appleTrees.get(treeId);
                if (appleTree != null) {
                    appleTree.setHealth(health);
                }
                break;
            case COCONUT:
                CoconutTree coconutTree = coconutTrees.get(treeId);
                if (coconutTree != null) {
                    coconutTree.setHealth(health);
                }
                break;
            case BAMBOO:
                BambooTree bambooTree = bambooTrees.get(treeId);
                if (bambooTree != null) {
                    bambooTree.setHealth(health);
                }
                break;
            case BANANA:
                BananaTree bananaTree = bananaTrees.get(treeId);
                if (bananaTree != null) {
                    bananaTree.setHealth(health);
                }
                break;
        }
    }
    
    /**
     * Removes a tree from the game world.
     * 
     * @param treeId The tree ID to remove
     */
    public void removeTree(String treeId) {
        // Try to remove from all tree maps
        SmallTree smallTree = trees.remove(treeId);
        if (smallTree != null) {
            smallTree.dispose();
            clearedPositions.put(treeId, true);
            return;
        }
        
        AppleTree appleTree = appleTrees.remove(treeId);
        if (appleTree != null) {
            appleTree.dispose();
            clearedPositions.put(treeId, true);
            return;
        }
        
        CoconutTree coconutTree = coconutTrees.remove(treeId);
        if (coconutTree != null) {
            coconutTree.dispose();
            clearedPositions.put(treeId, true);
            return;
        }
        
        BambooTree bambooTree = bambooTrees.remove(treeId);
        if (bambooTree != null) {
            bambooTree.dispose();
            clearedPositions.put(treeId, true);
            return;
        }
        
        BananaTree bananaTree = bananaTrees.remove(treeId);
        if (bananaTree != null) {
            bananaTree.dispose();
            clearedPositions.put(treeId, true);
        }
    }
    
    /**
     * Updates or creates an item based on server item state.
     * 
     * @param itemState The item state from the server
     */
    public void updateItemFromState(ItemState itemState) {
        if (itemState == null) {
            return;
        }
        
        String itemId = itemState.getItemId();
        
        // If item is collected, remove it
        if (itemState.isCollected()) {
            removeItem(itemId);
            return;
        }
        
        // Create item if it doesn't exist
        ItemType type = itemState.getType();
        float x = itemState.getX();
        float y = itemState.getY();
        
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
        }
    }
    
    /**
     * Removes an item from the game world.
     * 
     * @param itemId The item ID to remove
     */
    public void removeItem(String itemId) {
        Apple apple = apples.remove(itemId);
        if (apple != null) {
            apple.dispose();
            return;
        }
        
        Banana banana = bananas.remove(itemId);
        if (banana != null) {
            banana.dispose();
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
            
            joinMultiplayerServer(address, port);
            
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

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        player.dispose();
        grassTexture.dispose();
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
        if (cactus != null) {
            cactus.dispose();
        }
        gameMenu.dispose();
        
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
    }
}
