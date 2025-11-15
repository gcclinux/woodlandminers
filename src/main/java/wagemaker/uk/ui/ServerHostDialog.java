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
import wagemaker.uk.localization.LocalizationManager;
import wagemaker.uk.localization.LanguageChangeListener;

/**
 * ServerHostDialog displays the server's IPv4 address after successfully hosting a server.
 * Players can share this IP address with others to allow them to connect.
 */
public class ServerHostDialog implements LanguageChangeListener {
    private boolean isVisible = false;
    private Texture woodenPlank;
    private BitmapFont dialogFont;
    private String serverIpAddress = "";
    private static final float DIALOG_WIDTH = 400;
    private static final float DIALOG_HEIGHT = 180;
    
    /**
     * Creates a new ServerHostDialog with wooden plank background and custom font.
     */
    public ServerHostDialog() {
        woodenPlank = createWoodenPlank();
        createDialogFont();
        
        // Register for language change notifications
        LocalizationManager.getInstance().addLanguageChangeListener(this);
    }
    
    /**
     * Creates the custom font for dialog text using slkscr.ttf.
     */
    private void createDialogFont() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Saira_SemiExpanded-MediumItalic.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 16;
            parameter.color = Color.WHITE;
            parameter.borderWidth = 1;
            parameter.borderColor = Color.BLACK;
            parameter.shadowOffsetX = 1;
            parameter.shadowOffsetY = 1;
            parameter.shadowColor = Color.BLACK;
            
            dialogFont = generator.generateFont(parameter);
            generator.dispose();
        } catch (Exception e) {
            System.out.println("Could not load custom font for server host dialog, using default: " + e.getMessage());
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
     * Shows the dialog with the specified server IP address.
     * 
     * @param ipAddress The server's IPv4 address to display
     */
    public void show(String ipAddress) {
        this.serverIpAddress = ipAddress;
        this.isVisible = true;
    }
    
    /**
     * Handles keyboard input for the dialog.
     * Pressing ESC closes the dialog.
     */
    public void handleInput() {
        if (!isVisible) {
            return;
        }
        
        // Handle escape - close dialog
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            isVisible = false;
        }
    }
    
    /**
     * Renders the server host dialog with the IP address.
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
        
        // Get localized text
        LocalizationManager loc = LocalizationManager.getInstance();
        String title = loc.getText("server_host_dialog.title");
        String ipLabel = loc.getText("server_host_dialog.ip_label");
        String closeInstruction = loc.getText("server_host_dialog.close_instruction");
        
        // Draw title
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, title, dialogX + 20, dialogY + DIALOG_HEIGHT - 30);
        
        // Draw server IP label
        dialogFont.setColor(Color.LIGHT_GRAY);
        dialogFont.draw(batch, ipLabel, dialogX + 20, dialogY + DIALOG_HEIGHT - 70);
        
        // Draw server IP address in center (highlighted in yellow)
        dialogFont.setColor(Color.YELLOW);
        // Center the IP address
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        layout.setText(dialogFont, serverIpAddress);
        float ipWidth = layout.width;
        float ipX = dialogX + (DIALOG_WIDTH - ipWidth) / 2;
        dialogFont.draw(batch, serverIpAddress, ipX, dialogY + DIALOG_HEIGHT / 2);
        
        // Draw instructions
        dialogFont.setColor(Color.LIGHT_GRAY);
        dialogFont.draw(batch, closeInstruction, dialogX + 20, dialogY + 30);
        
        batch.end();
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
     * Hides the dialog.
     */
    public void hide() {
        isVisible = false;
    }
    
    /**
     * Called when the language changes.
     * The dialog will automatically use the new language on next render.
     * 
     * @param newLanguage The new language code
     */
    @Override
    public void onLanguageChanged(String newLanguage) {
        // No need to cache strings - they are retrieved fresh on each render
        System.out.println("ServerHostDialog: Language changed to " + newLanguage);
    }
    
    /**
     * Disposes of resources used by the dialog.
     */
    public void dispose() {
        // Unregister from language change notifications
        LocalizationManager.getInstance().removeLanguageChangeListener(this);
        
        if (woodenPlank != null) {
            woodenPlank.dispose();
        }
        if (dialogFont != null) {
            dialogFont.dispose();
        }
    }
}
