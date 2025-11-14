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
 * MultiplayerMenu provides the UI for hosting or connecting to multiplayer servers.
 * This menu is accessed from the main game menu and displays options in the
 * wooden plank style consistent with the game's aesthetic.
 */
public class MultiplayerMenu implements LanguageChangeListener {
    private boolean isOpen = false;
    private Texture woodenPlank;
    private BitmapFont menuFont;
    private String[] menuOptions;
    private int selectedIndex = 0;
    private static final float MENU_WIDTH = 350; // Increased by 200% (300 * 3 = 900)
    private static final float MENU_HEIGHT = 200;
    
    /**
     * Creates a new MultiplayerMenu with wooden plank background and custom font.
     */
    public MultiplayerMenu() {
        woodenPlank = createWoodenPlank();
        createMenuFont();
        updateMenuOptions();
        
        // Register for language change notifications
        LocalizationManager.getInstance().addLanguageChangeListener(this);
    }
    
    /**
     * Creates the custom font for menu text using slkscr.ttf.
     */
    private void createMenuFont() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/slkscr.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 18;
            parameter.color = Color.WHITE;
            parameter.borderWidth = 1;
            parameter.borderColor = Color.BLACK;
            parameter.shadowOffsetX = 1;
            parameter.shadowOffsetY = 1;
            parameter.shadowColor = Color.BLACK;
            
            menuFont = generator.generateFont(parameter);
            generator.dispose();
        } catch (Exception e) {
            System.out.println("Could not load custom font for multiplayer menu, using default: " + e.getMessage());
            // Fallback to default font
            menuFont = new BitmapFont();
            menuFont.getData().setScale(1.5f);
            menuFont.setColor(Color.WHITE);
        }
    }
    
    /**
     * Creates a wooden plank texture for the menu background.
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
     * Updates the menu state, handling keyboard navigation.
     */
    public void update() {
        if (!isOpen) {
            return;
        }
        
        // Handle up arrow - move selection up
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedIndex = (selectedIndex - 1 + menuOptions.length) % menuOptions.length;
        }
        
        // Handle down arrow - move selection down
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedIndex = (selectedIndex + 1) % menuOptions.length;
        }
        
        // Handle enter - select current option
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            handleSelection();
        }
        
        // Handle escape - close menu (same as Back option)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            close();
        }
    }
    
    /**
     * Handles the selection of a menu option.
     * This method should be overridden or connected to callbacks for actual functionality.
     */
    private void handleSelection() {
        String selected = menuOptions[selectedIndex];
        System.out.println("Selected: " + selected);
        
        // The actual implementation will be handled by GameMenu integration
        // For now, just close on "Back" (check by index since text is localized)
        if (selectedIndex == 2) { // Back option is always at index 2
            close();
        }
    }
    
    /**
     * Update menu options with localized text.
     */
    private void updateMenuOptions() {
        LocalizationManager loc = LocalizationManager.getInstance();
        menuOptions = new String[] {
            loc.getText("multiplayer_menu.host_server"),
            loc.getText("multiplayer_menu.connect_to_server"),
            loc.getText("multiplayer_menu.back")
        };
    }
    
    /**
     * Renders the multiplayer menu with wooden plank style.
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
        menuFont.draw(batch, loc.getText("multiplayer_menu.title"), menuX + 20, menuY + MENU_HEIGHT - 30);
        
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
     * Opens the multiplayer menu.
     */
    public void open() {
        isOpen = true;
        selectedIndex = 0; // Reset selection
    }
    
    /**
     * Closes the multiplayer menu.
     */
    public void close() {
        isOpen = false;
    }
    
    /**
     * Checks if the multiplayer menu is currently open.
     * 
     * @return true if the menu is open, false otherwise
     */
    public boolean isOpen() {
        return isOpen;
    }
    
    /**
     * Gets the currently selected menu option index.
     * 
     * @return The selected index
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }
    
    /**
     * Gets the currently selected menu option text.
     * 
     * @return The selected option text
     */
    public String getSelectedOption() {
        return menuOptions[selectedIndex];
    }
    
    /**
     * Called when the language changes.
     * Refreshes all menu text with the new language.
     * 
     * @param newLanguage The new language code
     */
    @Override
    public void onLanguageChanged(String newLanguage) {
        System.out.println("MultiplayerMenu: Language changed to " + newLanguage);
        updateMenuOptions();
    }
    
    /**
     * Disposes of resources used by the multiplayer menu.
     */
    public void dispose() {
        // Unregister from language change notifications
        LocalizationManager.getInstance().removeLanguageChangeListener(this);
        
        if (woodenPlank != null) {
            woodenPlank.dispose();
        }
        if (menuFont != null) {
            menuFont.dispose();
        }
    }
}
