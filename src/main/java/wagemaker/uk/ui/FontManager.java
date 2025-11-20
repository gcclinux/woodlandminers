package wagemaker.uk.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import wagemaker.uk.client.PlayerConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton class that manages font loading and switching.
 * Handles loading fonts, caching them, and notifying listeners of changes.
 */
public class FontManager {
    private static FontManager instance;
    
    private FontType currentFontType;
    private Map<FontType, BitmapFont> fontCache;
    private List<FontChangeListener> listeners;
    
    // Font parameters
    private static final int FONT_SIZE = 16;
    private static final String EXTENDED_CHARS = FreeTypeFontGenerator.DEFAULT_CHARS + 
                                                  "ąćęłńóśźżĄĆĘŁŃÓŚŹŻ" +  // Polish
                                                  "ãõâêôáéíóúàçÃÕÂÊÔÁÉÍÓÚÀÇ" +  // Portuguese
                                                  "äöüßÄÖÜ";  // German
    
    private FontManager() {
        fontCache = new HashMap<>();
        listeners = new ArrayList<>();
        
        // Load saved font preference or use default
        PlayerConfig config = PlayerConfig.load();
        String savedFontName = config.getFontName();
        
        if (savedFontName != null) {
            currentFontType = FontType.fromDisplayName(savedFontName);
            System.out.println("Using saved font preference: " + currentFontType.getDisplayName());
        } else {
            currentFontType = FontType.SANCREEK;  // Default
            System.out.println("Using default font: " + currentFontType.getDisplayName());
        }
    }
    
    /**
     * Gets the singleton instance of FontManager.
     * @return The FontManager instance
     */
    public static FontManager getInstance() {
        if (instance == null) {
            instance = new FontManager();
        }
        return instance;
    }
    
    /**
     * Gets the current font type.
     * @return The current FontType
     */
    public FontType getCurrentFontType() {
        return currentFontType;
    }
    
    /**
     * Gets the current font, loading it if necessary.
     * @return The current BitmapFont
     */
    public BitmapFont getCurrentFont() {
        return getFont(currentFontType);
    }
    
    /**
     * Gets a font of the specified type, loading it if necessary.
     * @param fontType The font type to get
     * @return The BitmapFont
     */
    public BitmapFont getFont(FontType fontType) {
        if (!fontCache.containsKey(fontType)) {
            loadFont(fontType);
        }
        return fontCache.get(fontType);
    }
    
    /**
     * Sets the current font type and saves the preference.
     * @param fontType The new font type
     */
    public void setFont(FontType fontType) {
        if (fontType == currentFontType) {
            System.out.println("Font already set to: " + fontType.getDisplayName());
            return;
        }
        
        System.out.println("Changing font from " + currentFontType.getDisplayName() + 
                          " to " + fontType.getDisplayName());
        
        currentFontType = fontType;
        
        // Save preference (setFontName already calls save internally)
        PlayerConfig config = PlayerConfig.load();
        config.setFontName(fontType.getDisplayName());
        
        // Notify listeners
        notifyFontChanged();
    }
    
    /**
     * Loads a font from file and caches it.
     * @param fontType The font type to load
     */
    private void loadFont(FontType fontType) {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal(fontType.getFilePath())
            );
            
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = 
                new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = FONT_SIZE;
            parameter.characters = EXTENDED_CHARS;
            parameter.color = Color.WHITE;
            parameter.borderWidth = 1;
            parameter.borderColor = Color.BLACK;
            parameter.shadowOffsetX = 1;
            parameter.shadowOffsetY = 1;
            parameter.shadowColor = Color.BLACK;
            
            BitmapFont font = generator.generateFont(parameter);
            generator.dispose();
            
            fontCache.put(fontType, font);
            System.out.println("Loaded font: " + fontType.getDisplayName() + 
                             " from " + fontType.getFilePath());
            
        } catch (Exception e) {
            System.err.println("Error loading font " + fontType.getDisplayName() + ": " + e.getMessage());
            
            // Fallback to default font if loading fails
            if (fontType != FontType.SANCREEK) {
                System.err.println("Falling back to default font");
                BitmapFont fallbackFont = new BitmapFont();
                fallbackFont.getData().setScale(1.2f);
                fallbackFont.setColor(Color.WHITE);
                fontCache.put(fontType, fallbackFont);
            }
        }
    }
    
    /**
     * Adds a font change listener.
     * @param listener The listener to add
     */
    public void addFontChangeListener(FontChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Removes a font change listener.
     * @param listener The listener to remove
     */
    public void removeFontChangeListener(FontChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notifies all listeners that the font has changed.
     */
    private void notifyFontChanged() {
        System.out.println("Notifying " + listeners.size() + " listeners of font change");
        for (FontChangeListener listener : new ArrayList<>(listeners)) {
            listener.onFontChanged(currentFontType);
        }
        System.out.println("Font changed successfully to: " + currentFontType.getDisplayName());
    }
    
    /**
     * Disposes of all cached fonts.
     * Should be called when the game is shutting down.
     */
    public void dispose() {
        for (BitmapFont font : fontCache.values()) {
            if (font != null) {
                font.dispose();
            }
        }
        fontCache.clear();
        listeners.clear();
        System.out.println("FontManager disposed");
    }
}
