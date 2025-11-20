package wagemaker.uk.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import wagemaker.uk.localization.LocalizationManager;
import wagemaker.uk.localization.LanguageChangeListener;

/**
 * Player Profile submenu that displays player-related options.
 * This menu appears when "Player Profile" is selected from the main menu.
 * It contains options for Player Name, Save Player, and Language.
 */
public class PlayerProfileMenu implements LanguageChangeListener {
    private boolean isOpen = false;
    private int selectedIndex = 0;
    private String[] menuOptions;
    
    // Visual properties
    private static final float MENU_WIDTH = 400;
    private static final float MENU_HEIGHT = 240;
    
    // Wooden plank background
    private Texture woodenPlank;
    
    // Font for rendering
    private BitmapFont menuFont;
    
    /**
     * Creates a new PlayerProfileMenu instance.
     */
    public PlayerProfileMenu() {
        woodenPlank = createWoodenPlank();
        createMenuFont();
        
        // Register as language change listener
        LocalizationManager.getInstance().addLanguageChangeListener(this);
        
        // Initialize menu options with localized text
        updateMenuOptions();
    }
    
    /**
     * Creates a custom font for the menu.
     */
    private void createMenuFont() {
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
            
            menuFont = generator.generateFont(parameter);
            generator.dispose();
        } catch (Exception e) {
            System.out.println("Could not load custom font for PlayerProfileMenu, using default: " + e.getMessage());
            menuFont = new BitmapFont();
            menuFont.getData().setScale(1.2f);
            menuFont.setColor(Color.WHITE);
        }
    }
    
    /**
     * Updates menu options with localized text.
     * Called on initialization and when language changes.
     */
    private void updateMenuOptions() {
        LocalizationManager loc = LocalizationManager.getInstance();
        menuOptions = new String[] {
            loc.getText("player_profile_menu.player_name"),
            loc.getText("player_profile_menu.save_player"),
            loc.getText("player_profile_menu.language"),
            loc.getText("player_profile_menu.back")
        };
    }
    
    /**
     * Called when the application language changes.
     * Refreshes menu options to display in the new language.
     * 
     * @param newLanguage The new language code
     */
    @Override
    public void onLanguageChanged(String newLanguage) {
        System.out.println("PlayerProfileMenu: Language changed to " + newLanguage);
        updateMenuOptions();
    }
    
    /**
     * Creates the wooden plank background texture.
     * 
     * @return The wooden plank texture
     */
    private Texture createWoodenPlank() {
        Pixmap pixmap = new Pixmap((int)MENU_WIDTH, (int)MENU_HEIGHT, Pixmap.Format.RGBA8888);
        
        // Base wood color
        pixmap.setColor(0.4f, 0.25f, 0.1f, 1.0f);
        pixmap.fill();
        
        // Wood grain lines
        pixmap.setColor(0.3f, 0.18f, 0.08f, 1.0f);
        for (int y = 10; y < MENU_HEIGHT; y += 15) {
            pixmap.drawLine(0, y, (int)MENU_WIDTH, y + 5);
        }
        
        // Border
        pixmap.setColor(0.2f, 0.12f, 0.05f, 1.0f);
        pixmap.drawRectangle(0, 0, (int)MENU_WIDTH, (int)MENU_HEIGHT);
        pixmap.drawRectangle(2, 2, (int)MENU_WIDTH - 4, (int)MENU_HEIGHT - 4);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
    
    /**
     * Updates the menu state based on user input.
     * Handles up/down navigation through menu options.
     */
    public void update() {
        if (!isOpen) {
            return;
        }
        
        // Handle up/down navigation
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedIndex = (selectedIndex - 1 + menuOptions.length) % menuOptions.length;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedIndex = (selectedIndex + 1) % menuOptions.length;
        }
        
        // Handle escape - close menu (same as Back option)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            close();
        }
    }
    
    /**
     * Renders the player profile menu with wooden plank style.
     * 
     * @param batch The sprite batch for rendering
     * @param shapeRenderer The shape renderer (unused but kept for consistency)
     * @param camX Camera X position
     * @param camY Camera Y position
     * @param viewWidth Viewport width
     * @param viewHeight Viewport height
     */
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, float camX, float camY, float viewWidth, float viewHeight) {
        if (!isOpen) {
            return;
        }
        
        batch.begin();
        
        // Center the menu on screen
        float menuX = camX - MENU_WIDTH / 2;
        float menuY = camY - MENU_HEIGHT / 2;
        
        // Draw wooden plank background
        batch.draw(woodenPlank, menuX, menuY, MENU_WIDTH, MENU_HEIGHT);
        
        // Draw title
        LocalizationManager loc = LocalizationManager.getInstance();
        menuFont.setColor(Color.WHITE);
        menuFont.draw(batch, loc.getText("player_profile_menu.title"), menuX + 20, menuY + MENU_HEIGHT - 30);
        
        // Draw menu options
        for (int i = 0; i < menuOptions.length; i++) {
            // Highlight selected option in yellow
            if (i == selectedIndex) {
                menuFont.setColor(Color.YELLOW);
            } else {
                menuFont.setColor(Color.WHITE);
            }
            
            float textX = menuX + 40;
            float textY = menuY + MENU_HEIGHT - 80 - (i * 35);
            menuFont.draw(batch, menuOptions[i], textX, textY);
        }
        
        batch.end();
    }
    
    /**
     * Opens the player profile menu.
     */
    public void open() {
        isOpen = true;
        selectedIndex = 0; // Reset selection
    }
    
    /**
     * Closes the player profile menu.
     */
    public void close() {
        isOpen = false;
    }
    
    /**
     * Checks if the player profile menu is currently open.
     * 
     * @return true if the menu is open, false otherwise
     */
    public boolean isOpen() {
        return isOpen;
    }
    
    /**
     * Gets the currently selected menu option index.
     * 
     * @return The selected index (0-3)
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }
    
    /**
     * Disposes of resources used by the menu.
     */
    public void dispose() {
        if (woodenPlank != null) {
            woodenPlank.dispose();
        }
        if (menuFont != null) {
            menuFont.dispose();
        }
        
        // Unregister from language change listener
        LocalizationManager.getInstance().removeLanguageChangeListener(this);
    }
}
