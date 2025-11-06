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
import wagemaker.uk.items.Apple;
import wagemaker.uk.items.Banana;
import wagemaker.uk.trees.AppleTree;
import wagemaker.uk.trees.BambooTree;
import wagemaker.uk.trees.BananaTree;
import wagemaker.uk.trees.CoconutTree;
import wagemaker.uk.trees.SmallTree;
import wagemaker.uk.trees.Cactus;
import wagemaker.uk.ui.GameMenu;

public class MyGdxGame extends ApplicationAdapter {
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
    GameMenu gameMenu;
    
    // Camera dimensions for infinite world
    static final int CAMERA_WIDTH = 1280;
    static final int CAMERA_HEIGHT = 1024;

    @Override
    public void create() {
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
        random = new Random();

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
        player.setGameMenu(gameMenu);
        
        // Load player position from save file if it exists
        gameMenu.loadPlayerPosition();

        // create realistic grass texture
        grassTexture = createRealisticGrassTexture();
        grassTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);


    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        gameMenu.update();

        if (!gameMenu.isOpen()) {
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
        batch.draw(player.getCurrentFrame(), player.getX(), player.getY());
        drawAppleTrees();
        drawBananaTrees();
        batch.end();
        
        // draw health bars
        drawHealthBars();
        
        // draw menu on top
        gameMenu.render(batch, shapeRenderer, camera.position.x, camera.position.y, viewport.getWorldWidth(), viewport.getWorldHeight());
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
            random.setSeed(x * 31L + y * 17L); // deterministic based on position
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
    }
}
