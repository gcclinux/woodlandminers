package wagemaker.uk.items;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * BabyTree item that can be dropped when a SmallTree is destroyed.
 * Can be collected and used for planting new trees.
 * Follows the same pattern as other collectible items (Apple, BabyBamboo, WoodStack).
 */
public class BabyTree {
    private float x, y;
    private Texture texture;

    public BabyTree(float x, float y) {
        this.x = x;
        this.y = y;
        createTexture();
    }

    private void createTexture() {
        Texture spriteSheet = new Texture("sprites/assets.png");
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        spriteSheet.getTextureData().prepare();
        Pixmap sheetPixmap = spriteSheet.getTextureData().consumePixmap();
        
        // BabyTree coordinates: 384 from left, 128 from top, 64x64 size
        pixmap.drawPixmap(sheetPixmap, 0, 0, 384, 128, 64, 64);
        
        texture = new Texture(pixmap);
        pixmap.dispose();
        sheetPixmap.dispose();
        spriteSheet.dispose();
    }

    public Texture getTexture() {
        return texture;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
