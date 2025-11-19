package wagemaker.uk.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import wagemaker.uk.inventory.Inventory;

/**
 * Renders the inventory UI panel at the bottom-right corner of the screen.
 * Displays item icons and counts for all collected items.
 */
public class InventoryRenderer {
    // Textures for item icons
    private Texture appleIcon;
    private Texture bananaIcon;
    private Texture babyBambooIcon;
    private Texture bambooStackIcon;
    private Texture babyTreeIcon;
    private Texture woodStackIcon;
    private Texture pebbleIcon;
    private Texture palmFiberIcon;
    
    // Background and UI elements
    private Texture woodenBackground;
    private Texture slotBorder;
    
    // Font for rendering item counts
    private BitmapFont countFont;
    
    // ShapeRenderer for selection highlight
    private ShapeRenderer shapeRenderer;
    
    // Layout constants
    private static final int SLOT_SIZE = 40;
    private static final int SLOT_SPACING = 8;
    private static final int PANEL_PADDING = 8;
    private static final int PANEL_WIDTH = (SLOT_SIZE * 8) + (SLOT_SPACING * 7) + (PANEL_PADDING * 2);
    private static final int PANEL_HEIGHT = SLOT_SIZE + (PANEL_PADDING * 2);
    private static final int ICON_SIZE = 32;
    
    // Selection highlight constants
    private static final float HIGHLIGHT_R = 1.0f;
    private static final float HIGHLIGHT_G = 0.84f;
    private static final float HIGHLIGHT_B = 0.0f;
    private static final float HIGHLIGHT_ALPHA = 0.8f;
    private static final int HIGHLIGHT_BORDER_WIDTH = 3;
    
    /**
     * Create a new InventoryRenderer and load all required assets.
     */
    public InventoryRenderer() {
        loadItemIcons();
        createWoodenBackground();
        createSlotBorder();
        initializeFont();
        shapeRenderer = new ShapeRenderer();
    }
    
    /**
     * Load item icon textures from the sprite sheet.
     * Uses the same extraction method as the item classes.
     */
    private void loadItemIcons() {
        // Load apple icon (0, 128, 64x64)
        appleIcon = extractIconFromSpriteSheet(0, 128, 64, 64);
        
        // Load banana icon (64, 128, 64x64)
        bananaIcon = extractIconFromSpriteSheet(64, 128, 64, 64);
        
        // Load baby bamboo icon (192, 128, 64x64)
        babyBambooIcon = extractIconFromSpriteSheet(192, 128, 64, 64);
        
        // Load bamboo stack icon (128, 128, 64x64)
        bambooStackIcon = extractIconFromSpriteSheet(128, 128, 64, 64);
        
        // Load baby tree icon (384, 128, 64x64)
        babyTreeIcon = extractIconFromSpriteSheet(384, 128, 64, 64);
        
        // Load wood stack icon (256, 128, 64x64)
        woodStackIcon = extractIconFromSpriteSheet(256, 128, 64, 64);
        
        // Load pebble icon (320, 128, 64x64)
        pebbleIcon = extractIconFromSpriteSheet(320, 128, 64, 64);
        
        // Load palm fiber icon (448, 128, 64x64)
        palmFiberIcon = extractIconFromSpriteSheet(448, 128, 64, 64);
    }
    
    /**
     * Extract an icon from the sprite sheet at the specified coordinates.
     */
    private Texture extractIconFromSpriteSheet(int srcX, int srcY, int width, int height) {
        Texture spriteSheet = new Texture("sprites/assets.png");
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        spriteSheet.getTextureData().prepare();
        Pixmap sheetPixmap = spriteSheet.getTextureData().consumePixmap();
        
        pixmap.drawPixmap(sheetPixmap, 0, 0, srcX, srcY, width, height);
        
        Texture icon = new Texture(pixmap);
        pixmap.dispose();
        sheetPixmap.dispose();
        spriteSheet.dispose();
        
        return icon;
    }
    
    /**
     * Create a wooden plank background texture procedurally.
     */
    private void createWoodenBackground() {
        Pixmap pixmap = new Pixmap(PANEL_WIDTH, PANEL_HEIGHT, Pixmap.Format.RGBA8888);
        
        // Fill with brown wood color
        pixmap.setColor(new Color(0.55f, 0.35f, 0.2f, 0.9f));
        pixmap.fill();
        
        // Add darker border for depth
        pixmap.setColor(new Color(0.35f, 0.2f, 0.1f, 0.9f));
        pixmap.drawRectangle(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        pixmap.drawRectangle(1, 1, PANEL_WIDTH - 2, PANEL_HEIGHT - 2);
        
        woodenBackground = new Texture(pixmap);
        pixmap.dispose();
    }
    
    /**
     * Create a slot border texture for visual separation.
     */
    private void createSlotBorder() {
        Pixmap pixmap = new Pixmap(SLOT_SIZE, SLOT_SIZE, Pixmap.Format.RGBA8888);
        
        // Draw dark brown border
        pixmap.setColor(new Color(0.3f, 0.15f, 0.05f, 0.8f));
        pixmap.drawRectangle(0, 0, SLOT_SIZE, SLOT_SIZE);
        
        slotBorder = new Texture(pixmap);
        pixmap.dispose();
    }
    
    /**
     * Initialize the font for rendering item counts.
     */
    private void initializeFont() {
        countFont = new BitmapFont();
        countFont.getData().setScale(1.0f);
    }
    
    /**
     * Render the inventory UI panel at the bottom-right corner of the screen.
     * 
     * @param batch The SpriteBatch to use for rendering
     * @param inventory The inventory to display
     * @param camX Camera X position
     * @param camY Camera Y position
     * @param viewWidth Viewport width
     * @param viewHeight Viewport height
     * @param selectedSlot The currently selected slot index (0-6), or -1 for no selection
     */
    public void render(SpriteBatch batch, Inventory inventory, 
                      float camX, float camY, float viewWidth, float viewHeight, int selectedSlot) {
        if (inventory == null) {
            return;
        }
        
        // Calculate bottom-right position
        float panelX = camX + viewWidth / 2 - PANEL_WIDTH - 20;
        float panelY = camY - viewHeight / 2 + 20;
        
        batch.begin();
        
        // Draw wooden background
        batch.draw(woodenBackground, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);
        
        // Draw slot borders for visual separation
        float slotX = panelX + PANEL_PADDING;
        float slotY = panelY + PANEL_PADDING;
        
        for (int i = 0; i < 8; i++) {
            float x = slotX + i * (SLOT_SIZE + SLOT_SPACING);
            batch.draw(slotBorder, x, slotY, SLOT_SIZE, SLOT_SIZE);
        }
        
        // Render slots with icons and counts in order: Apple, Banana, BabyBamboo, BambooStack, BabyTree, WoodStack, Pebble, PalmFiber
        renderSlot(batch, appleIcon, inventory.getAppleCount(), slotX, slotY, selectedSlot == 0);
        renderSlot(batch, bananaIcon, inventory.getBananaCount(), slotX + (SLOT_SIZE + SLOT_SPACING), slotY, selectedSlot == 1);
        renderSlot(batch, babyBambooIcon, inventory.getBabyBambooCount(), slotX + 2 * (SLOT_SIZE + SLOT_SPACING), slotY, selectedSlot == 2);
        renderSlot(batch, bambooStackIcon, inventory.getBambooStackCount(), slotX + 3 * (SLOT_SIZE + SLOT_SPACING), slotY, selectedSlot == 3);
        renderSlot(batch, babyTreeIcon, inventory.getBabyTreeCount(), slotX + 4 * (SLOT_SIZE + SLOT_SPACING), slotY, selectedSlot == 4);
        renderSlot(batch, woodStackIcon, inventory.getWoodStackCount(), slotX + 5 * (SLOT_SIZE + SLOT_SPACING), slotY, selectedSlot == 5);
        renderSlot(batch, pebbleIcon, inventory.getPebbleCount(), slotX + 6 * (SLOT_SIZE + SLOT_SPACING), slotY, selectedSlot == 6);
        renderSlot(batch, palmFiberIcon, inventory.getPalmFiberCount(), slotX + 7 * (SLOT_SIZE + SLOT_SPACING), slotY, selectedSlot == 7);
        
        batch.end();
    }
    
    /**
     * Render an individual inventory slot with icon and count.
     * 
     * @param batch The SpriteBatch to use for rendering
     * @param icon The item icon texture
     * @param count The item count to display
     * @param x The X position of the slot
     * @param y The Y position of the slot
     * @param isSelected Whether this slot is currently selected
     */
    private void renderSlot(SpriteBatch batch, Texture icon, int count, float x, float y, boolean isSelected) {
        // Draw selection highlight if this slot is selected
        if (isSelected) {
            // Get the current projection matrix from the batch before ending it
            com.badlogic.gdx.math.Matrix4 projectionMatrix = batch.getProjectionMatrix().cpy();
            
            batch.end(); // End batch to use ShapeRenderer
            
            // Set ShapeRenderer to use the same projection matrix as the batch
            shapeRenderer.setProjectionMatrix(projectionMatrix);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(HIGHLIGHT_R, HIGHLIGHT_G, HIGHLIGHT_B, HIGHLIGHT_ALPHA);
            Gdx.gl.glLineWidth(HIGHLIGHT_BORDER_WIDTH);
            
            // Draw highlight border (44x44) around selected slot
            shapeRenderer.rect(x - 2, y - 2, SLOT_SIZE + 4, SLOT_SIZE + 4);
            
            shapeRenderer.end();
            Gdx.gl.glLineWidth(1); // Reset line width
            
            batch.begin(); // Resume batch rendering
        }
        
        // Calculate centered position for 32x32 icon in 40x40 slot
        float iconX = x + (SLOT_SIZE - ICON_SIZE) / 2;
        float iconY = y + (SLOT_SIZE - ICON_SIZE) / 2;
        
        // Draw item icon at 32x32 size
        batch.draw(icon, iconX, iconY, ICON_SIZE, ICON_SIZE);
        
        // Draw item count above icon with shadow for readability
        String countText = String.valueOf(count);
        
        // Use GlyphLayout to properly measure text width for centering
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        layout.setText(countFont, countText);
        float textX = x + (SLOT_SIZE - layout.width) / 2;
        float textY = y + SLOT_SIZE + 2;
        
        // Draw shadow
        countFont.setColor(Color.BLACK);
        countFont.draw(batch, countText, textX + 1, textY - 1);
        
        // Draw main text
        countFont.setColor(Color.WHITE);
        countFont.draw(batch, countText, textX, textY);
    }
    
    /**
     * Dispose of all textures and resources.
     */
    public void dispose() {
        if (appleIcon != null) appleIcon.dispose();
        if (bananaIcon != null) bananaIcon.dispose();
        if (babyBambooIcon != null) babyBambooIcon.dispose();
        if (bambooStackIcon != null) bambooStackIcon.dispose();
        if (babyTreeIcon != null) babyTreeIcon.dispose();
        if (woodStackIcon != null) woodStackIcon.dispose();
        if (pebbleIcon != null) pebbleIcon.dispose();
        if (palmFiberIcon != null) palmFiberIcon.dispose();
        if (woodenBackground != null) woodenBackground.dispose();
        if (slotBorder != null) slotBorder.dispose();
        if (countFont != null) countFont.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}
