package wagemaker.uk.weather;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Handles rendering of water puddles using LibGDX ShapeRenderer.
 * Uses object pooling for performance optimization.
 * Manages puddle lifecycle: spawning, updating, rendering, and clearing.
 * 
 * Configuration is centralized in PuddleConfig for easy tuning.
 */
public class PuddleRenderer {
    
    private List<WaterPuddle> puddlePool;
    private ShapeRenderer shapeRenderer;
    private Random random;
    private List<TreePosition> treePositions;
    
    /**
     * Creates a new PuddleRenderer with the specified ShapeRenderer.
     * 
     * @param shapeRenderer The ShapeRenderer to use for drawing puddles
     */
    public PuddleRenderer(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
        this.random = new Random();
        this.puddlePool = new ArrayList<>();
        this.treePositions = new ArrayList<>();
    }
    
    /**
     * Initializes the puddle renderer by pre-allocating the puddle pool.
     * This method should be called once during setup to avoid garbage collection
     * during gameplay.
     */
    public void initialize() {
        // Pre-allocate MAX_PUDDLES for object pooling
        puddlePool.clear();
        for (int i = 0; i < PuddleConfig.MAX_PUDDLES; i++) {
            puddlePool.add(new WaterPuddle());
        }
    }
    
    /**
     * Spawns puddles within the camera viewport with random positions and sizes.
     * Uses object pooling to reuse inactive puddles.
     * Implements spacing logic to prevent puddles from overlapping.
     * 
     * @param camera The camera used to determine viewport bounds
     * @param count The number of puddles to spawn
     */
    public void spawnPuddles(OrthographicCamera camera, int count) {
        // Calculate viewport bounds
        float halfWidth = camera.viewportWidth * camera.zoom / 2;
        float halfHeight = camera.viewportHeight * camera.zoom / 2;
        
        float camLeft = camera.position.x - halfWidth;
        float camRight = camera.position.x + halfWidth;
        float camBottom = camera.position.y - halfHeight;
        float camTop = camera.position.y + halfHeight;
        
        int spawned = 0;
        int attempts = 0;
        int maxAttempts = count * 10; // More attempts needed due to strict tree avoidance
        
        while (spawned < count && attempts < maxAttempts) {
            attempts++;
            
            // Find an inactive puddle from the pool
            WaterPuddle puddle = getInactivePuddle();
            if (puddle == null) {
                break; // Pool exhausted
            }
            
            // Generate random position within viewport
            float x = camLeft + random.nextFloat() * (camRight - camLeft);
            float y = camBottom + random.nextFloat() * (camTop - camBottom);
            
            // Generate random size - always horizontal oval (width > height)
            float width = PuddleConfig.MIN_PUDDLE_WIDTH + 
                         random.nextFloat() * (PuddleConfig.MAX_PUDDLE_WIDTH - PuddleConfig.MIN_PUDDLE_WIDTH);
            float height = PuddleConfig.MIN_PUDDLE_HEIGHT + 
                          random.nextFloat() * (PuddleConfig.MAX_PUDDLE_HEIGHT - PuddleConfig.MIN_PUDDLE_HEIGHT);
            
            // Ensure width is always greater than height for horizontal oval
            if (width < height) {
                float temp = width;
                width = height;
                height = temp;
            }
            
            // No rotation - keep puddles horizontal
            float rotation = 0.0f;
            
            // Check spacing with existing active puddles
            if (hasMinimumSpacing(x, y, puddle)) {
                // Reset puddle with new properties
                puddle.reset(x, y, width, height, rotation);
                spawned++;
            } else {
                // Return puddle to pool if spacing check failed
                puddle.setActive(false);
            }
        }
    }
    
    /**
     * Updates all active puddles by applying an alpha multiplier for evaporation effects.
     * 
     * @param deltaTime Time elapsed since last update in seconds
     * @param alphaMultiplier Multiplier for puddle alpha (0.0 to 1.0), used for evaporation
     */
    public void updatePuddles(float deltaTime, float alphaMultiplier) {
        for (WaterPuddle puddle : puddlePool) {
            if (puddle.isActive()) {
                // Apply alpha multiplier for evaporation
                float newAlpha = PuddleConfig.PUDDLE_BASE_ALPHA * alphaMultiplier;
                puddle.setBaseAlpha(newAlpha);
            }
        }
    }
    
    /**
     * Renders all active puddles to the screen as ellipses.
     * Only renders puddles that are within the camera viewport (viewport-based culling).
     * 
     * @param camera The camera used for projection
     */
    public void render(OrthographicCamera camera) {
        int activeCount = getActivePuddleCount();
        if (activeCount == 0) {
            return; // No puddles to render
        }
        
        // Set up ShapeRenderer with camera projection
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        int renderedCount = 0;
        
        // Render each active puddle (with viewport culling)
        for (WaterPuddle puddle : puddlePool) {
            if (puddle.isActive() && puddle.isInViewport(camera)) {
                float x = puddle.getX();
                float y = puddle.getY();
                float width = puddle.getWidth();
                float height = puddle.getHeight();
                float alpha = puddle.getBaseAlpha();
                
                // Render realistic multi-layered puddle
                renderRealisticPuddle(x, y, width, height, alpha);
                renderedCount++;
            }
        }
        
        shapeRenderer.end();
        
        // Log culling efficiency if debug logging is enabled
        if (PuddleConfig.DEBUG_LOGGING_ENABLED && activeCount > 0) {
            int culledCount = activeCount - renderedCount;
            if (culledCount > 0) {
                // Only log when culling actually happens to reduce spam
                // This will be logged occasionally, not every frame
            }
        }
    }
    
    /**
     * Renders a realistic-looking puddle with multiple layers for depth and reflection.
     * Creates a more natural appearance than a simple flat ellipse.
     * 
     * @param x X position of puddle
     * @param y Y position of puddle
     * @param width Width of puddle
     * @param height Height of puddle
     * @param alpha Base alpha transparency
     */
    private void renderRealisticPuddle(float x, float y, float width, float height, float alpha) {
        // Layer 1: Dark outer edge (puddle border/shadow)
        // Slightly larger and darker to create depth
        shapeRenderer.setColor(0.2f, 0.25f, 0.35f, alpha * 0.8f);
        shapeRenderer.ellipse(x - 2, y - 1, width + 4, height + 2);
        
        // Layer 2: Main puddle body (darker blue-gray water)
        shapeRenderer.setColor(
            PuddleConfig.PUDDLE_COLOR_RED * 0.8f,
            PuddleConfig.PUDDLE_COLOR_GREEN * 0.8f,
            PuddleConfig.PUDDLE_COLOR_BLUE * 0.9f,
            alpha
        );
        shapeRenderer.ellipse(x, y, width, height);
        
        // Layer 3: Irregular inner shape for natural look
        // Offset slightly to create asymmetry
        float innerWidth = width * 0.7f;
        float innerHeight = height * 0.6f;
        float innerX = x + width * 0.15f;
        float innerY = y + height * 0.2f;
        shapeRenderer.setColor(
            PuddleConfig.PUDDLE_COLOR_RED * 0.9f,
            PuddleConfig.PUDDLE_COLOR_GREEN * 0.9f,
            PuddleConfig.PUDDLE_COLOR_BLUE,
            alpha * 0.7f
        );
        shapeRenderer.ellipse(innerX, innerY, innerWidth, innerHeight);
        
        // Layer 4: Bright highlight (water reflection/sky reflection)
        // Small, offset to one side to simulate light reflection
        float highlightWidth = width * 0.3f;
        float highlightHeight = height * 0.25f;
        float highlightX = x + width * 0.55f;
        float highlightY = y + height * 0.5f;
        shapeRenderer.setColor(0.6f, 0.7f, 0.85f, alpha * 0.4f);
        shapeRenderer.ellipse(highlightX, highlightY, highlightWidth, highlightHeight);
        
        // Layer 5: Secondary smaller highlight for more realism
        float highlight2Width = width * 0.15f;
        float highlight2Height = height * 0.15f;
        float highlight2X = x + width * 0.25f;
        float highlight2Y = y + height * 0.6f;
        shapeRenderer.setColor(0.65f, 0.75f, 0.9f, alpha * 0.3f);
        shapeRenderer.ellipse(highlight2X, highlight2Y, highlight2Width, highlight2Height);
    }
    
    /**
     * Clears all active puddles by marking them as inactive.
     * This returns all puddles to the pool for reuse.
     */
    public void clearAllPuddles() {
        for (WaterPuddle puddle : puddlePool) {
            puddle.setActive(false);
        }
    }
    
    /**
     * Cleans up resources used by the renderer.
     * Note: ShapeRenderer is managed externally and should not be disposed here.
     */
    public void dispose() {
        // Puddle pool will be garbage collected
        // ShapeRenderer is managed by the main game class
        puddlePool.clear();
    }
    
    /**
     * Gets the number of currently active puddles.
     * 
     * @return The number of active puddles
     */
    public int getActivePuddleCount() {
        int count = 0;
        for (WaterPuddle puddle : puddlePool) {
            if (puddle.isActive()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Finds an inactive puddle from the pool for reuse.
     * 
     * @return An inactive puddle, or null if all puddles are active
     */
    private WaterPuddle getInactivePuddle() {
        for (WaterPuddle puddle : puddlePool) {
            if (!puddle.isActive()) {
                return puddle;
            }
        }
        return null; // Pool exhausted
    }
    
    /**
     * Updates the list of tree positions for puddle avoidance.
     * Should be called before spawning puddles to ensure trees are avoided.
     * 
     * @param trees List of tree positions to avoid
     */
    public void setTreePositions(List<TreePosition> trees) {
        this.treePositions.clear();
        if (trees != null) {
            this.treePositions.addAll(trees);
        }
    }
    
    /**
     * Checks if a potential puddle position has minimum spacing from existing puddles
     * and is not too close to any trees.
     * 
     * @param x X position of the potential puddle
     * @param y Y position of the potential puddle
     * @param excludePuddle Puddle to exclude from the check (typically the one being positioned)
     * @return true if the position has adequate spacing, false otherwise
     */
    private boolean hasMinimumSpacing(float x, float y, WaterPuddle excludePuddle) {
        float minSpacingSquared = PuddleConfig.MIN_PUDDLE_SPACING * PuddleConfig.MIN_PUDDLE_SPACING;
        
        // Check spacing from other puddles
        for (WaterPuddle puddle : puddlePool) {
            if (puddle.isActive() && puddle != excludePuddle) {
                float dx = puddle.getX() - x;
                float dy = puddle.getY() - y;
                float distanceSquared = dx * dx + dy * dy;
                
                if (distanceSquared < minSpacingSquared) {
                    return false; // Too close to existing puddle
                }
            }
        }
        
        // Check distance from trees (avoid puddles under tree foliage)
        float minTreeDistanceSquared = PuddleConfig.MIN_TREE_DISTANCE * PuddleConfig.MIN_TREE_DISTANCE;
        
        for (TreePosition tree : treePositions) {
            // Calculate distance from puddle to tree center
            // Trees are typically 64x128, so center is at x+32, y+64
            float treeCenterX = tree.x + 32;
            float treeCenterY = tree.y + 64;
            
            float dx = treeCenterX - x;
            float dy = treeCenterY - y;
            float distanceSquared = dx * dx + dy * dy;
            
            if (distanceSquared < minTreeDistanceSquared) {
                return false; // Too close to tree
            }
        }
        
        return true; // Adequate spacing
    }
    
    /**
     * Gets the number of puddles in the pool.
     * 
     * @return Total puddle pool size
     */
    public int getPoolSize() {
        return puddlePool.size();
    }
    
    /**
     * Gets the number of puddles that would be rendered in the current viewport.
     * Used for performance monitoring.
     * 
     * @param camera The camera to check against
     * @return Number of puddles within viewport
     */
    public int getVisiblePuddleCount(OrthographicCamera camera) {
        int count = 0;
        for (WaterPuddle puddle : puddlePool) {
            if (puddle.isActive() && puddle.isInViewport(camera)) {
                count++;
            }
        }
        return count;
    }
    
    @Override
    public String toString() {
        return String.format("PuddleRenderer[active=%d/%d]",
            getActivePuddleCount(), puddlePool.size());
    }
    
    /**
     * Simple class to hold tree position data for puddle avoidance.
     */
    public static class TreePosition {
        public final float x;
        public final float y;
        
        public TreePosition(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
