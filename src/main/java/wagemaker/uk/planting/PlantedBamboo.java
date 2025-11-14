package wagemaker.uk.planting;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class PlantedBamboo {
    private float x, y;
    private Texture texture;
    private float growthTimer;
    private static final float GROWTH_DURATION = 120.0f; // 120 seconds

    public PlantedBamboo(float x, float y) {
        this.x = snapToTileGrid(x);
        this.y = snapToTileGrid(y);
        this.growthTimer = 0.0f;
        createTexture();
    }

    private float snapToTileGrid(float coordinate) {
        return (float) (Math.floor(coordinate / 64.0) * 64.0);
    }

    private void createTexture() {
        Texture spriteSheet = new Texture("sprites/assets.png");
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        spriteSheet.getTextureData().prepare();
        Pixmap sheetPixmap = spriteSheet.getTextureData().consumePixmap();
        
        // BabyBamboo coordinates: 192 from left, 128 from top, 64x64 size
        pixmap.drawPixmap(sheetPixmap, 0, 0, 192, 128, 64, 64);
        
        texture = new Texture(pixmap);
        pixmap.dispose();
        sheetPixmap.dispose();
        spriteSheet.dispose();
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
        return texture;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
