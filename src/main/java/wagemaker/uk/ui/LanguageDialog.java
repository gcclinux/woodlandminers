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
 * LanguageDialog allows players to select their preferred language.
 * Displays all supported languages with native names and updates the UI when language changes.
 */
public class LanguageDialog implements LanguageChangeListener {
    private boolean isVisible = false;
    private boolean confirmed = false;
    private Texture woodenPlank;
    private BitmapFont dialogFont;
    private int selectedIndex = 0;
    private String[] languageCodes;
    private String[] languageDisplayNames;
    private static final float DIALOG_WIDTH = 640; // Increased by 60% (400 * 1.6 = 640)
    private static final float DIALOG_HEIGHT = 350;
    
    /**
     * Creates a new LanguageDialog with wooden plank background and custom font.
     */
    public LanguageDialog() {
        woodenPlank = createWoodenPlank();
        createDialogFont();
        
        // Get supported languages from LocalizationManager
        LocalizationManager locManager = LocalizationManager.getInstance();
        languageCodes = locManager.getSupportedLanguages();
        languageDisplayNames = new String[languageCodes.length];
        
        // Get display names for each language
        for (int i = 0; i < languageCodes.length; i++) {
            languageDisplayNames[i] = locManager.getLanguageDisplayName(languageCodes[i]);
        }
        
        // Register as language change listener
        locManager.addLanguageChangeListener(this);
    }
    
    /**
     * Creates the custom font for dialog text using slkscr.ttf.
     */
    private void createDialogFont() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/slkscr.ttf"));
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
            System.out.println("Could not load custom font for language dialog, using default: " + e.getMessage());
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
     * Shows the language dialog and sets the current language as selected.
     */
    public void show() {
        this.isVisible = true;
        this.confirmed = false;
        
        // Set selected index to current language
        LocalizationManager locManager = LocalizationManager.getInstance();
        String currentLang = locManager.getCurrentLanguage();
        
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentLang)) {
                selectedIndex = i;
                break;
            }
        }
    }
    
    /**
     * Handles keyboard input for language selection.
     * Arrow keys navigate between languages, Enter confirms selection, ESC cancels.
     */
    public void handleInput() {
        if (!isVisible) {
            return;
        }
        
        // Handle up arrow key
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedIndex--;
            if (selectedIndex < 0) {
                selectedIndex = languageCodes.length - 1;
            }
        }
        
        // Handle down arrow key
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedIndex++;
            if (selectedIndex >= languageCodes.length) {
                selectedIndex = 0;
            }
        }
        
        // Handle enter (confirm selection)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            String selectedLanguage = languageCodes[selectedIndex];
            LocalizationManager locManager = LocalizationManager.getInstance();
            
            System.out.println("Language selected: " + selectedLanguage);
            locManager.setLanguage(selectedLanguage);
            
            confirmed = true;
            isVisible = false;
        }
        
        // Handle escape (cancel)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            confirmed = false;
            isVisible = false;
        }
    }
    
    /**
     * Renders the language dialog with language options.
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
        LocalizationManager locManager = LocalizationManager.getInstance();
        String title = locManager.getText("language_dialog.title");
        String currentLangLabel = locManager.getText("language_dialog.current_language", 
                                                     locManager.getLanguageDisplayName(locManager.getCurrentLanguage()));
        String selectInstruction = locManager.getText("language_dialog.select_instruction");
        String cancelInstruction = locManager.getText("language_dialog.cancel_instruction");
        
        // Draw title
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, title, dialogX + 20, dialogY + DIALOG_HEIGHT - 30);
        
        // Draw current language indicator
        dialogFont.setColor(Color.LIGHT_GRAY);
        dialogFont.draw(batch, currentLangLabel, dialogX + 20, dialogY + DIALOG_HEIGHT - 60);
        
        // Draw language options
        float optionY = dialogY + DIALOG_HEIGHT - 110;
        for (int i = 0; i < languageDisplayNames.length; i++) {
            if (i == selectedIndex) {
                dialogFont.setColor(Color.YELLOW);
                // Draw selection indicator
                dialogFont.draw(batch, ">", dialogX + 40, optionY);
            } else {
                dialogFont.setColor(Color.LIGHT_GRAY);
            }
            
            dialogFont.draw(batch, languageDisplayNames[i], dialogX + 70, optionY);
            optionY -= 35;
        }
        
        // Draw instructions
        dialogFont.setColor(Color.LIGHT_GRAY);
        dialogFont.draw(batch, selectInstruction, dialogX + 40, dialogY + 60);
        dialogFont.draw(batch, cancelInstruction, dialogX + 40, dialogY + 35);
        
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
     * Checks if the user confirmed a language selection.
     * 
     * @return true if confirmed, false otherwise
     */
    public boolean isConfirmed() {
        return confirmed;
    }
    
    /**
     * Resets the confirmation state.
     * Should be called after handling a confirmed language change.
     */
    public void resetConfirmation() {
        confirmed = false;
    }
    
    /**
     * Hides the dialog.
     */
    public void hide() {
        isVisible = false;
        confirmed = false;
    }
    
    /**
     * Called when the application language changes.
     * Refreshes the dialog text to display in the new language.
     * 
     * @param newLanguage The new language code
     */
    @Override
    public void onLanguageChanged(String newLanguage) {
        System.out.println("LanguageDialog: Language changed to " + newLanguage);
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
