package wagemaker.uk;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MyGdxGame extends ApplicationAdapter {
    SpriteBatch batch;
    ShapeRenderer shapeRenderer;
    Texture grassTexture;
    Player player;
    OrthographicCamera camera;
    Viewport viewport;
    Map<String, Tree> trees;
    Map<String, AppleTree> appleTrees;
    Map<String, Boolean> clearedPositions;
    Random random;
    
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
        clearedPositions = new HashMap<>();
        random = new Random();

        // create player at origin
        player = new Player(0, 0, camera);
        player.setTrees(trees);
        player.setAppleTrees(appleTrees);
        player.setClearedPositions(clearedPositions);

        // create grass texture
        Pixmap grassPixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        grassPixmap.setColor(0.2f, 0.6f, 0.1f, 1);
        grassPixmap.fill();
        grassTexture = new Texture(grassPixmap);
        grassTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        grassPixmap.dispose();


    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        // update player and camera
        player.update(deltaTime);
        
        // update trees
        for (Tree tree : trees.values()) {
            tree.update(deltaTime);
        }
        for (AppleTree appleTree : appleTrees.values()) {
            appleTree.update(deltaTime);
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
        drawAppleTrees();
        batch.draw(player.getCurrentFrame(), player.getX(), player.getY());
        batch.end();
        
        // draw health bars
        drawHealthBars();
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
        if (!trees.containsKey(key) && !appleTrees.containsKey(key) && !clearedPositions.containsKey(key)) {
            // 5% chance to generate a tree at this grass tile
            random.setSeed(x * 31L + y * 17L); // deterministic based on position
            if (random.nextFloat() < 0.05f) {
                // 50% chance for regular tree, 50% for apple tree
                if (random.nextFloat() < 0.5f) {
                    trees.put(key, new Tree(x, y));
                } else {
                    appleTrees.put(key, new AppleTree(x, y));
                }
            }
        }
    }
    
    private void drawTrees() {
        float camX = camera.position.x;
        float camY = camera.position.y;
        float viewWidth = viewport.getWorldWidth();
        float viewHeight = viewport.getWorldHeight();
        
        for (Tree tree : trees.values()) {
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

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    private void drawHealthBars() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Draw tree health bars
        for (Tree tree : trees.values()) {
            if (tree.shouldShowHealthBar()) {
                float barWidth = 32; // half of tree width
                float barHeight = 4;
                float barX = tree.getX() + 16; // center above tree
                float barY = tree.getY() + 70; // above tree
                
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
        
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        player.dispose();
        grassTexture.dispose();
        for (Tree tree : trees.values()) {
            tree.dispose();
        }
        for (AppleTree appleTree : appleTrees.values()) {
            appleTree.dispose();
        }
    }
}