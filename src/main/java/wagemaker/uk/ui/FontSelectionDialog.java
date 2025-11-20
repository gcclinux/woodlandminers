package wagemaker.uk.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import wagemaker.uk.localization.LocalizationManager;
import wagemaker.uk.localization.LanguageChangeListener;
import wagemaker.uk.client.PlayerConfig;

/**
 * Dialog for selecting the menu font.
 * Displays a list of available fonts with preview and keyboard navigation.
 */
public class FontSelectionDialog implements LanguageChangeListener {
    private static final int DIALOG_WIDTH = 400;
    private static final int DIALOG_HEIGHT = 300;
    private static final int PADDING = 20;
    private static final int OPTION_HEIGHT = 30; // Reduced from 40 to 30
    
    private boolean isOpen = false;
    private int selectedIndex = 0;
    private FontType[] fonts;
    private FontType originalSelection;
    
    // For keyboard input handling
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean enterPressed = false;
    private boolean escPressed = false;
    
    /**
     * Constructor.
     */
    public FontSelectionDialog() {
        this.fonts = FontType.values();
        LocalizationManager.getInstance().addLanguageChangeListener(this);
        
        // Find the current font and set as selected
        FontType currentFont = FontManager.getInstance().getCurrentFontType();
        for (int i = 0; i < fonts.length; i++) {
            if (fonts[i] == currentFont) {
                selectedIndex = i;
                break;
            }
        }
    }
    
    /**
     * Opens the font selection dialog.
     */
    public void open() {
        isOpen = true;
        originalSelection = FontManager.getInstance().getCurrentFontType();
        
        // Set selected index to current font
        for (int i = 0; i < fonts.length; i++) {
            if (fonts[i] == originalSelection) {
                selectedIndex = i;
                break;
            }
        }
        
        // Reset input states to prevent carryover from previous menu
        upPressed = false;
        downPressed = false;
        enterPressed = Gdx.input.isKeyPressed(Input.Keys.ENTER); // Set to current state to ignore already-pressed keys
        escPressed = Gdx.input.isKeyPressed(Input.Keys.ESCAPE);
    }
    
    /**
     * Closes the font selection dialog.
     */
    public void close() {
        isOpen = false;
    }
    
    /**
     * Checks if the dialog is currently open.
     * @return true if open, false otherwise
     */
    public boolean isOpen() {
        return isOpen;
    }
    
    /**
     * Updates the dialog state based on input.
     */
    public void update() {
        if (!isOpen) return;
        
        // Handle UP key
        boolean upCurrentlyPressed = Gdx.input.isKeyPressed(Input.Keys.UP);
        if (upCurrentlyPressed && !upPressed) {
            selectedIndex--;
            if (selectedIndex < 0) {
                selectedIndex = fonts.length - 1;
            }
        }
        upPressed = upCurrentlyPressed;
        
        // Handle DOWN key
        boolean downCurrentlyPressed = Gdx.input.isKeyPressed(Input.Keys.DOWN);
        if (downCurrentlyPressed && !downPressed) {
            selectedIndex++;
            if (selectedIndex >= fonts.length) {
                selectedIndex = 0;
            }
        }
        downPressed = downCurrentlyPressed;
        
        // Handle ENTER key - confirm selection
        boolean enterCurrentlyPressed = Gdx.input.isKeyPressed(Input.Keys.ENTER);
        if (enterCurrentlyPressed && !enterPressed) {
            FontType selectedFont = fonts[selectedIndex];
            FontManager.getInstance().setFont(selectedFont);
            close();
        }
        enterPressed = enterCurrentlyPressed;
        
        // Handle ESC key - cancel without saving
        boolean escCurrentlyPressed = Gdx.input.isKeyPressed(Input.Keys.ESCAPE);
        if (escCurrentlyPressed && !escPressed) {
            // Restore original font if user changed it while browsing
            if (FontManager.getInstance().getCurrentFontType() != originalSelection) {
                FontManager.getInstance().setFont(originalSelection);
            }
            close();
        }
        escPressed = escCurrentlyPressed;
    }
    
    /**
     * Renders the dialog centered on the current camera position.
     * @param batch Sprite batch used for drawing
     * @param font Font to render text with
     * @param woodenPlank Wooden plank background texture
     * @param camX Current camera X center (world coordinates)
     * @param camY Current camera Y center (world coordinates)
     */
    public void render(SpriteBatch batch, BitmapFont font, com.badlogic.gdx.graphics.Texture woodenPlank, float camX, float camY) {
        if (!isOpen) return;
        
        batch.begin();
        
        int dialogX = (int)(camX - DIALOG_WIDTH / 2f);
        int dialogY = (int)(camY - DIALOG_HEIGHT / 2f);
        
        // Draw dialog background
        batch.draw(woodenPlank, dialogX, dialogY, DIALOG_WIDTH, DIALOG_HEIGHT);
        
        // Draw title
        String title = LocalizationManager.getInstance().getText("font_selection_dialog.title");
        font.draw(batch, title, dialogX + 20, dialogY + DIALOG_HEIGHT - 30);
        
        // Draw current font indicator
        FontType currentFont = FontManager.getInstance().getCurrentFontType();
        String currentText = LocalizationManager.getInstance().getText("font_selection_dialog.current") + ": " + currentFont.getDisplayName();
        font.draw(batch, currentText, dialogX + 20, dialogY + DIALOG_HEIGHT - 60);
        
        // Draw font options
        int optionStartY = dialogY + DIALOG_HEIGHT - 100;
        for (int i = 0; i < fonts.length; i++) {
            String fontName = fonts[i].getDisplayName();
            
            // Highlight selected option
            if (i == selectedIndex) {
                font.setColor(Color.YELLOW);
                fontName = "> " + fontName;
            } else {
                font.setColor(Color.WHITE);
            }
            
            font.draw(batch, fontName, dialogX + 40, optionStartY - i * OPTION_HEIGHT);
        }
        
        // Reset font color
        font.setColor(Color.WHITE);
        
        // Draw instructions
        String selectInstruction = LocalizationManager.getInstance().getText("font_selection_dialog.select_instruction");
        String cancelInstruction = LocalizationManager.getInstance().getText("font_selection_dialog.cancel_instruction");
        
        font.draw(batch, selectInstruction, dialogX + 40, dialogY + 60);
        font.draw(batch, cancelInstruction, dialogX + 40, dialogY + 35);
        
        batch.end();
    }
    
    @Override
    public void onLanguageChanged(String newLanguageCode) {
        // Dialog text will automatically update on next render
    }
    
    /**
     * Disposes of resources.
     */
    public void dispose() {
        LocalizationManager.getInstance().removeLanguageChangeListener(this);
    }
}
