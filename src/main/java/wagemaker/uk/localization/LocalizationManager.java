package wagemaker.uk.localization;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import wagemaker.uk.client.PlayerConfig;

import java.util.*;

/**
 * Singleton class that manages localization for the game.
 * Handles language detection, loading, text retrieval, and language switching.
 */
public class LocalizationManager {
    private static LocalizationManager instance;
    
    private Map<String, String> translations;
    private Map<String, String> fallbackTranslations;
    private String currentLanguage;
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String[] SUPPORTED_LANGUAGES = {"en", "pl", "pt", "nl", "de"};
    private List<LanguageChangeListener> listeners;
    
    // Language display names in their native language
    private static final Map<String, String> LANGUAGE_DISPLAY_NAMES = new HashMap<String, String>() {{
        put("en", "English");
        put("pl", "Polski");
        put("pt", "Português");
        put("nl", "Nederlands");
        put("de", "Deutsch");
    }};
    
    private LocalizationManager() {
        translations = new HashMap<>();
        fallbackTranslations = new HashMap<>();
        listeners = new ArrayList<>();
        currentLanguage = DEFAULT_LANGUAGE;
    }
    
    /**
     * Get the singleton instance of LocalizationManager.
     * 
     * @return The LocalizationManager instance
     */
    public static LocalizationManager getInstance() {
        if (instance == null) {
            instance = new LocalizationManager();
        }
        return instance;
    }
    
    /**
     * Initialize the localization system.
     * Detects system locale, loads saved preference, and loads language files.
     */
    public void initialize() {
        // Load fallback language first
        loadLanguageFile(DEFAULT_LANGUAGE, fallbackTranslations);
        
        // TEMPORARY: Force English as default for testing
        // TODO: Remove this and restore original logic once testing is complete
        String languageToLoad = "en";
        System.out.println("TESTING MODE: Forcing English language");
        
        /* Original code - commented out for testing:
        // Check for saved language preference in PlayerConfig
        PlayerConfig config = PlayerConfig.load();
        String savedLanguage = config.getLanguage();
        
        String languageToLoad;
        if (savedLanguage != null && isSupportedLanguage(savedLanguage)) {
            languageToLoad = savedLanguage;
            System.out.println("Using saved language preference: " + languageToLoad);
        } else {
            // Detect system locale
            languageToLoad = detectSystemLocale();
            System.out.println("Detected system language: " + languageToLoad);
        }
        */
        
        // Load the selected language
        loadLanguageFile(languageToLoad, translations);
        currentLanguage = languageToLoad;
        
        System.out.println("LocalizationManager initialized with language: " + currentLanguage);
    }
    
    /**
     * Detect the system locale and map it to a supported language.
     * 
     * @return Language code (en, pl, pt, nl)
     */
    private String detectSystemLocale() {
        try {
            Locale systemLocale = Locale.getDefault();
            String language = systemLocale.getLanguage().toLowerCase();
            
            // Map locale to supported language
            switch (language) {
                case "en":
                    return "en";
                case "pl":
                    return "pl";
                case "pt":
                    return "pt";
                case "nl":
                    return "nl";
                case "de":
                    return "de";
                default:
                    System.out.println("Unsupported system locale: " + language + ", falling back to English");
                    return DEFAULT_LANGUAGE;
            }
        } catch (Exception e) {
            System.err.println("Error detecting system locale: " + e.getMessage());
            return DEFAULT_LANGUAGE;
        }
    }
    
    /**
     * Load a language file from the assets directory.
     * 
     * @param languageCode The language code (e.g., "en", "pl")
     * @param targetMap The map to load translations into
     */
    private void loadLanguageFile(String languageCode, Map<String, String> targetMap) {
        try {
            String filePath = "localization/" + languageCode + ".json";
            FileHandle file = Gdx.files.internal(filePath);
            
            if (!file.exists()) {
                System.err.println("Language file not found: " + filePath);
                if (!languageCode.equals(DEFAULT_LANGUAGE)) {
                    System.err.println("Falling back to default language: " + DEFAULT_LANGUAGE);
                    loadLanguageFile(DEFAULT_LANGUAGE, targetMap);
                }
                return;
            }
            
            String jsonContent = file.readString();
            parseJsonTranslations(jsonContent, targetMap);
            
            System.out.println("Loaded language file: " + filePath + " (" + targetMap.size() + " keys)");
            
        } catch (Exception e) {
            System.err.println("Error loading language file for " + languageCode + ": " + e.getMessage());
            e.printStackTrace();
            if (!languageCode.equals(DEFAULT_LANGUAGE)) {
                System.err.println("Falling back to default language: " + DEFAULT_LANGUAGE);
                loadLanguageFile(DEFAULT_LANGUAGE, targetMap);
            }
        }
    }
    
    /**
     * Parse JSON content and populate the translations map.
     * Flattens nested JSON structure into dot-notation keys.
     * 
     * @param jsonContent The JSON string content
     * @param targetMap The map to populate with translations
     */
    private void parseJsonTranslations(String jsonContent, Map<String, String> targetMap) {
        try {
            Json json = new Json();
            JsonValue root = json.fromJson(null, jsonContent);
            
            targetMap.clear();
            flattenJson(root, "", targetMap);
            
        } catch (Exception e) {
            System.err.println("Error parsing JSON translations: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Recursively flatten nested JSON structure into dot-notation keys.
     * 
     * @param jsonValue The current JSON value
     * @param prefix The current key prefix
     * @param targetMap The map to populate
     */
    private void flattenJson(JsonValue jsonValue, String prefix, Map<String, String> targetMap) {
        if (jsonValue == null) {
            return;
        }
        
        for (JsonValue child = jsonValue.child; child != null; child = child.next) {
            String key = prefix.isEmpty() ? child.name : prefix + "." + child.name;
            
            if (child.isObject()) {
                // Recursively flatten nested objects
                flattenJson(child, key, targetMap);
            } else if (child.isString()) {
                // Add string value to map
                targetMap.put(key, child.asString());
            }
        }
    }
    
    /**
     * Get translated text by key.
     * Falls back to English if key not found in current language.
     * 
     * @param key The translation key (e.g., "menu.player_name")
     * @return The translated text, or the key itself if not found
     */
    public String getText(String key) {
        if (translations.containsKey(key)) {
            return translations.get(key);
        }
        
        // Try fallback language
        if (!currentLanguage.equals(DEFAULT_LANGUAGE) && fallbackTranslations.containsKey(key)) {
            System.out.println("Warning: Missing translation for key '" + key + 
                             "' in language '" + currentLanguage + "', using fallback");
            return fallbackTranslations.get(key);
        }
        
        // Return key itself if no translation found
        System.err.println("Error: Missing translation key '" + key + "' in all languages");
        return "[" + key + "]";
    }
    
    /**
     * Get translated text with parameter substitution.
     * Replaces {0}, {1}, etc. with provided parameters.
     * 
     * @param key The translation key
     * @param params Parameters to substitute into the text
     * @return The translated text with parameters substituted
     */
    public String getText(String key, Object... params) {
        String text = getText(key);
        
        if (params == null || params.length == 0) {
            return text;
        }
        
        // Replace {0}, {1}, etc. with parameters
        for (int i = 0; i < params.length; i++) {
            String placeholder = "{" + i + "}";
            if (text.contains(placeholder)) {
                text = text.replace(placeholder, String.valueOf(params[i]));
            }
        }
        
        return text;
    }
    
    /**
     * Get the current language code.
     * 
     * @return The current language code (e.g., "en", "pl")
     */
    public String getCurrentLanguage() {
        return currentLanguage;
    }
    
    /**
     * Get all supported language codes.
     * 
     * @return Array of supported language codes
     */
    public String[] getSupportedLanguages() {
        return SUPPORTED_LANGUAGES.clone();
    }
    
    /**
     * Get the display name for a language code in its native language.
     * 
     * @param languageCode The language code
     * @return The display name (e.g., "English", "Polski")
     */
    public String getLanguageDisplayName(String languageCode) {
        return LANGUAGE_DISPLAY_NAMES.getOrDefault(languageCode, languageCode);
    }
    
    /**
     * Check if a language code is supported.
     * 
     * @param languageCode The language code to check
     * @return true if supported, false otherwise
     */
    private boolean isSupportedLanguage(String languageCode) {
        for (String lang : SUPPORTED_LANGUAGES) {
            if (lang.equals(languageCode)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if a translation key exists in the current language.
     * 
     * @param key The translation key
     * @return true if the key exists, false otherwise
     */
    public boolean hasKey(String key) {
        return translations.containsKey(key);
    }
    
    /**
     * Change the current language.
     * Loads the new language file and notifies all listeners.
     * 
     * @param languageCode The new language code
     */
    public void setLanguage(String languageCode) {
        if (!isSupportedLanguage(languageCode)) {
            System.err.println("Unsupported language code: " + languageCode);
            return;
        }
        
        if (languageCode.equals(currentLanguage)) {
            System.out.println("Language already set to: " + languageCode);
            return;
        }
        
        System.out.println("Changing language from " + currentLanguage + " to " + languageCode);
        
        // Load new language file
        loadLanguageFile(languageCode, translations);
        currentLanguage = languageCode;
        
        // Save preference to PlayerConfig
        PlayerConfig config = PlayerConfig.load();
        config.saveLanguage(languageCode);
        
        // Notify listeners
        notifyLanguageChanged();
        
        System.out.println("Language changed successfully to: " + currentLanguage);
    }
    
    /**
     * Register a listener to be notified when the language changes.
     * 
     * @param listener The listener to register
     */
    public void addLanguageChangeListener(LanguageChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Unregister a language change listener.
     * 
     * @param listener The listener to unregister
     */
    public void removeLanguageChangeListener(LanguageChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notify all registered listeners that the language has changed.
     */
    private void notifyLanguageChanged() {
        System.out.println("Notifying " + listeners.size() + " listeners of language change");
        for (LanguageChangeListener listener : listeners) {
            try {
                listener.onLanguageChanged(currentLanguage);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    

}
