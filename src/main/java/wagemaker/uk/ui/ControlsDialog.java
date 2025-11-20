package wagemaker.uk.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import wagemaker.uk.localization.LanguageChangeListener;
import wagemaker.uk.localization.LocalizationManager;

/**
 * ControlsDialog displays all keyboard controls in an organized reference dialog.
 * Shows movement, inventory, item, targeting, combat, and system controls.
 * Supports multi-language localization and follows the game's wooden plank dialog style.
 */
public class ControlsDialog implements LanguageChangeListener {
    private boolean isVisible = false;
    private Texture woodenPlank;
    private BitmapFont dialogFont;
    private static final float DIALOG_WIDTH = 840;  // Increased by 20% (700 * 1.20) to prevent text overlap
    private static final float DIALOG_HEIGHT = 480; // Reduced by 20% (600 * 0.80) for better proportions
    
    /**
     * Creates a new ControlsDialog with wooden plank background and custom font.
     */
    public ControlsDialog() {
        woodenPlank = createWoodenPlank();
        createDialogFont();
        
        // Register as language change listener
        LocalizationManager.getInstance().addLanguageChangeListener(this);
    }
    
    /**
     * Creates the custom font for dialog text using Sancreek-Regular.ttf.
     */
    private void createDialogFont() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Sancreek-Regular.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 16;
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "ąćęłńóśźżĄĆĘŁŃÓŚŹŻãõâêôáéíóúàçÃÕÂÊÔÁÉÍÓÚÀÇäöüßÄÖÜ";
            parameter.color = Color.WHITE;
            parameter.borderWidth = 1;
            parameter.borderColor = Color.BLACK;
            parameter.shadowOffsetX = 1;
            parameter.shadowOffsetY = 1;
            parameter.shadowColor = Color.BLACK;
            
            dialogFont = generator.generateFont(parameter);
            generator.dispose();
        } catch (Exception e) {
            System.out.println("Could not load custom font for controls dialog, using default: " + e.getMessage());
            // Fallback to default font
            dialogFont = new BitmapFont();
            dialogFont.getData().setScale(1.3f);
            dialogFont.setColor(Color.WHITE);
        }
    }
    
    /**
     * Creates a wooden plank texture for the dialog background.
     * 
     * @return The wooden plank texture
     */
    private Texture createWoodenPlank() {
        Pixmap pixmap = new Pixmap((int)DIALOG_WIDTH, (int)DIALOG_HEIGHT, Pixmap.Format.RGBA8888);
        
        // Base wood color
        pixmap.setColor(0.4f, 0.25f, 0.1f, 1.0f);
        pixmap.fill();
        
        // Wood grain lines
        pixmap.setColor(0.3f, 0.18f, 0.08f, 1.0f);
        for (int y = 10; y < DIALOG_HEIGHT; y += 15) {
            pixmap.drawLine(0, y, (int)DIALOG_WIDTH, y + 5);
        }
        
        // Border
        pixmap.setColor(0.2f, 0.12f, 0.05f, 1.0f);
        pixmap.drawRectangle(0, 0, (int)DIALOG_WIDTH, (int)DIALOG_HEIGHT);
        pixmap.drawRectangle(2, 2, (int)DIALOG_WIDTH - 4, (int)DIALOG_HEIGHT - 4);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
    
    /**
     * Shows the controls dialog.
     */
    public void show() {
        this.isVisible = true;
    }
    
    /**
     * Hides the controls dialog.
     */
    public void hide() {
        this.isVisible = false;
    }
    
    /**
     * Checks if the dialog is currently visible.
     * 
     * @return true if the dialog is visible, false otherwise
     */
    public boolean isVisible() {
        return isVisible;
    }
    
    /**
     * Handles keyboard input for the dialog.
     * ESC key closes the dialog.
     */
    public void handleInput() {
        if (!isVisible) {
            return;
        }
        
        // Handle escape (close dialog)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            hide();
        }
    }
    
    /**
     * Renders the controls dialog with all control bindings.
     * 
     * @param batch The sprite batch for rendering
     * @param shapeRenderer The shape renderer (unused but kept for consistency)
     * @param camX Camera X position
     * @param camY Camera Y position
     */
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, float camX, float camY) {
        if (!isVisible) {
            return;
        }
        
        batch.begin();
        
        // Center the dialog on screen
        float dialogX = camX - DIALOG_WIDTH / 2;
        float dialogY = camY - DIALOG_HEIGHT / 2;
        
        // Draw wooden plank background
        batch.draw(woodenPlank, dialogX, dialogY, DIALOG_WIDTH, DIALOG_HEIGHT);
        
        // Get localized strings
        LocalizationManager loc = LocalizationManager.getInstance();
        
        // Draw title
        dialogFont.setColor(Color.WHITE);
        String title = loc.getText("controls_dialog.title");
        dialogFont.draw(batch, title, dialogX + 20, dialogY + DIALOG_HEIGHT - 30);
        
        float currentY = dialogY + DIALOG_HEIGHT - 70;
        float leftColumnX = dialogX + 30;
        float rightColumnX = dialogX + 492;  // Moved further right to utilize increased width and prevent overlap
        
        // Movement Controls (Left Column)
        dialogFont.setColor(Color.YELLOW);
        dialogFont.draw(batch, loc.getText("controls_dialog.movement_header"), leftColumnX, currentY);
        currentY -= 25;
        
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, loc.getText("controls_dialog.movement_up"), leftColumnX, currentY);
        currentY -= 20;
        dialogFont.draw(batch, loc.getText("controls_dialog.movement_down"), leftColumnX, currentY);
        currentY -= 20;
        dialogFont.draw(batch, loc.getText("controls_dialog.movement_left"), leftColumnX, currentY);
        currentY -= 20;
        dialogFont.draw(batch, loc.getText("controls_dialog.movement_right"), leftColumnX, currentY);
        currentY -= 30;
        
        // Inventory Controls (Left Column)
        dialogFont.setColor(Color.YELLOW);
        dialogFont.draw(batch, loc.getText("controls_dialog.inventory_header"), leftColumnX, currentY);
        currentY -= 25;
        
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, loc.getText("controls_dialog.inventory_open"), leftColumnX, currentY);
        currentY -= 20;
        dialogFont.draw(batch, loc.getText("controls_dialog.inventory_navigate_left"), leftColumnX, currentY);
        currentY -= 20;
        dialogFont.draw(batch, loc.getText("controls_dialog.inventory_navigate_right"), leftColumnX, currentY);
        currentY -= 30;
        
        // Item Controls (Left Column)
        dialogFont.setColor(Color.YELLOW);
        dialogFont.draw(batch, loc.getText("controls_dialog.item_header"), leftColumnX, currentY);
        currentY -= 25;
        
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, loc.getText("controls_dialog.item_plant_p"), leftColumnX, currentY);
        currentY -= 20;
        dialogFont.draw(batch, loc.getText("controls_dialog.item_plant_space"), leftColumnX, currentY);
        currentY -= 20;
        dialogFont.draw(batch, loc.getText("controls_dialog.item_consume"), leftColumnX, currentY);
        
        // Reset Y for right column
        currentY = dialogY + DIALOG_HEIGHT - 70;
        
        // Targeting Controls (Right Column)
        dialogFont.setColor(Color.YELLOW);
        dialogFont.draw(batch, loc.getText("controls_dialog.targeting_header"), rightColumnX, currentY);
        currentY -= 25;
        
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, loc.getText("controls_dialog.targeting_up"), rightColumnX, currentY);
        currentY -= 20;
        dialogFont.draw(batch, loc.getText("controls_dialog.targeting_down"), rightColumnX, currentY);
        currentY -= 20;
        dialogFont.draw(batch, loc.getText("controls_dialog.targeting_left"), rightColumnX, currentY);
        currentY -= 20;
        dialogFont.draw(batch, loc.getText("controls_dialog.targeting_right"), rightColumnX, currentY);
        currentY -= 30;
        
        // Combat Controls (Right Column)
        dialogFont.setColor(Color.YELLOW);
        dialogFont.draw(batch, loc.getText("controls_dialog.combat_header"), rightColumnX, currentY);
        currentY -= 25;
        
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, loc.getText("controls_dialog.combat_attack"), rightColumnX, currentY);
        currentY -= 30;
        
        // System Controls (Right Column)
        dialogFont.setColor(Color.YELLOW);
        dialogFont.draw(batch, loc.getText("controls_dialog.system_header"), rightColumnX, currentY);
        currentY -= 25;
        
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, loc.getText("controls_dialog.system_menu"), rightColumnX, currentY);
        currentY -= 20;
        dialogFont.draw(batch, loc.getText("controls_dialog.system_delete_world"), rightColumnX, currentY);
        currentY -= 20;
        dialogFont.draw(batch, loc.getText("controls_dialog.system_compass_target"), rightColumnX, currentY);
        
        // Draw close instruction at bottom
        dialogFont.setColor(Color.LIGHT_GRAY);
        String closeInstruction = loc.getText("controls_dialog.close_instruction");
        dialogFont.draw(batch, closeInstruction, dialogX + 20, dialogY + 30);
        
        batch.end();
    }
    
    /**
     * Called when the application language changes.
     * The dialog will automatically use the new language on next render.
     * 
     * @param newLanguage The new language code
     */
    @Override
    public void onLanguageChanged(String newLanguage) {
        System.out.println("ControlsDialog: Language changed to " + newLanguage);
        // The dialog will automatically use the new language on next render
        // because it calls LocalizationManager.getText() each time
    }
    
    /**
     * Disposes of resources used by the dialog.
     */
    public void dispose() {
        if (woodenPlank != null) {
            woodenPlank.dispose();
        }
        if (dialogFont != null) {
            dialogFont.dispose();
        }
        
        // Unregister from language change listener
        LocalizationManager.getInstance().removeLanguageChangeListener(this);
    }
}
