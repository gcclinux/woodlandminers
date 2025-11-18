package wagemaker.uk.planting;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class PlantedTree {
    private float x, y;
    private float growthTimer;
    private static final float GROWTH_DURATION = 120.0f; // 120 seconds
    
    // Shared texture for all PlantedTree instances to avoid repeated texture creation
    // This fixes multiplayer synchronization issues where rapid texture creation causes rendering failures
    private static Texture sharedTexture = null;
    private static int instanceCount = 0;

    public PlantedTree(float x, float y) {
        this.x = snapToTileGrid(x);
        this.y = snapToTileGrid(y);
        this.growthTimer = 0.0f;
        
        // Create shared texture on first instance
        if (sharedTexture == null) {
            createSharedTexture();
        }
        instanceCount++;
    }

    private float snapToTileGrid(float coordinate) {
        return (float) (Math.floor(coordinate / 64.0) * 64.0);
    }

    /**
     * Creates the shared texture used by all PlantedTree instances.
     * This is called once when the first PlantedTree is created.
     * Using a shared texture prevents issues with rapid texture creation in multiplayer.
     */
    private static synchronized void createSharedTexture() {
        if (sharedTexture != null) {
            return; // Already created
        }
        
        try {
            Texture spriteSheet = new Texture("sprites/assets.png");
            Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
            spriteSheet.getTextureData().prepare();
            Pixmap sheetPixmap = spriteSheet.getTextureData().consumePixmap();
            
            // BabyTree coordinates: 384 from left, 128 from top, 64x64 size
            pixmap.drawPixmap(sheetPixmap, 0, 0, 384, 128, 64, 64);
            
            sharedTexture = new Texture(pixmap);
            pixmap.dispose();
            sheetPixmap.dispose();
            spriteSheet.dispose();
            
            System.out.println("[PlantedTree] Shared texture created successfully");
        } catch (Exception e) {
            System.err.println("[PlantedTree] Failed to create shared texture: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean update(float deltaTime) {
        growthTimer += deltaTime;
        return growthTimer >= GROWTH_DURATION;
    }

    public boolean isReadyToTransform() {
        return growthTimer >= GROWTH_DURATION;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Texture getTexture() {
        return sharedTexture;
    }

    public void dispose() {
        // Decrement instance count
        instanceCount--;
        
        // Only dispose shared texture when all instances are gone
        // In practice, we keep it loaded for the game session
        // This prevents texture disposal/recreation cycles
        if (instanceCount <= 0 && sharedTexture != null) {
            System.out.println("[PlantedTree] All instances disposed, keeping shared texture for reuse");
            // Don't actually dispose - keep it for future plantings
            // sharedTexture.dispose();
            // sharedTexture = null;
        }
    }
    
    /**
     * Manually dispose the shared texture when shutting down the game.
     * Call this from the game's dispose() method.
     */
    public static void disposeSharedTexture() {
        if (sharedTexture != null) {
            sharedTexture.dispose();
            sharedTexture = null;
            instanceCount = 0;
            System.out.println("[PlantedTree] Shared texture disposed");
        }
    }
}