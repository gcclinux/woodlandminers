package wagemaker.uk.items;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * Pebble item that is collected when a Stone is destroyed.
 * Follows the WoodStack pattern for collectible items.
 * 
 * Sprite coordinates in assets.png: 448, 0, 64, 64 (TBD - placeholder coordinates)
 */
public class Pebble {
    private float x, y;
    private Texture texture;

    public Pebble(float x, float y) {
        this.x = x;
        this.y = y;
        createTexture();
    }

    private void createTexture() {
        Texture spriteSheet = new Texture("sprites/assets.png");
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        spriteSheet.getTextureData().prepare();
        Pixmap sheetPixmap = spriteSheet.getTextureData().consumePixmap();
        
        // TODO: Update sprite coordinates when final pebble sprite is added to assets.png
        // Current placeholder coordinates: 448, 0, 64, 64
        pixmap.drawPixmap(sheetPixmap, 0, 0, 320, 128, 64, 64);
        
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
