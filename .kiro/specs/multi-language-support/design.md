# Design Document

## Overview

This document describes the design for implementing multi-language support in the Woodlanders game. The system will provide localization for English, Polish, Portuguese, and Dutch languages with automatic language detection based on system locale. The design follows a centralized localization architecture with JSON-based language files and a singleton LocalizationManager for efficient text retrieval.

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Game Application                         │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              UI Components Layer                       │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ │  │
│  │  │ GameMenu │ │ Dialogs  │ │ Messages │ │  Labels  │ │  │
│  │  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ │  │
│  └───────┼────────────┼────────────┼────────────┼────────┘  │
│          │            │            │            │            │
│          └────────────┴────────────┴────────────┘            │
│                           │                                  │
│                           ▼                                  │
│  ┌───────────────────────────────────────────────────────┐  │
│  │         LocalizationManager (Singleton)               │  │
│  │  ┌─────────────────────────────────────────────────┐  │  │
│  │  │  - detectSystemLocale()                         │  │  │
│  │  │  - loadLanguageFile(locale)                     │  │  │
│  │  │  - getText(key)                                 │  │  │
│  │  │  - getText(key, params...)                      │  │  │
│  │  │  - getCurrentLanguage()                         │  │  │
│  │  └─────────────────────────────────────────────────┘  │  │
│  └───────────────────────┬───────────────────────────────┘  │
│                          │                                   │
│                          ▼                                   │
│  ┌───────────────────────────────────────────────────────┐  │
│  │           Language Files (JSON)                       │  │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ │  │
│  │  │  en.json │ │  pl.json │ │  pt.json │ │  nl.json │ │  │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────┘ │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Component Responsibilities

1. **LocalizationManager**: Central singleton responsible for language detection, file loading, and text retrieval
2. **Language Files**: JSON files containing key-value pairs for each supported language
3. **UI Components**: Game menus, dialogs, and labels that request localized text from LocalizationManager

## Components and Interfaces

### LocalizationManager Class

**Location**: `src/main/java/wagemaker/uk/localization/LocalizationManager.java`

**Purpose**: Singleton class that manages language detection, loading, and text retrieval

**Key Methods**:

```java
public class LocalizationManager {
    private static LocalizationManager instance;
    private Map<String, String> translations;
    private String currentLanguage;
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String[] SUPPORTED_LANGUAGES = {"en", "pl", "pt", "nl"};
    private List<LanguageChangeListener> listeners;
    
    // Singleton access
    public static LocalizationManager getInstance()
    
    // Initialize the localization system
    public void initialize()
    
    // Detect system locale and map to supported language
    private String detectSystemLocale()
    
    // Load language file from resources
    private void loadLanguageFile(String languageCode)
    
    // Get translated text by key
    public String getText(String key)
    
    // Get translated text with parameter substitution
    public String getText(String key, Object... params)
    
    // Get current language code
    public String getCurrentLanguage()
    
    // Get all supported languages
    public String[] getSupportedLanguages()
    
    // Get display name for a language code
    public String getLanguageDisplayName(String languageCode)
    
    // Change language manually
    public void setLanguage(String languageCode)
    
    // Save language preference to config
    private void saveLanguagePreference(String languageCode)
    
    // Load saved language preference from config
    private String loadLanguagePreference()
    
    // Register listener for language changes
    public void addLanguageChangeListener(LanguageChangeListener listener)
    
    // Notify all listeners of language change
    private void notifyLanguageChanged()
    
    // Check if a key exists in current language
    public boolean hasKey(String key)
}

// Listener interface for language changes
public interface LanguageChangeListener {
    void onLanguageChanged(String newLanguage);
}
```

**Initialization Flow**:
1. Check for saved language preference in player config
2. If saved preference exists, use it
3. Otherwise, detect system locale using `Locale.getDefault()`
4. Map locale to supported language code (en, pl, pt, nl)
5. Load corresponding JSON language file
6. Parse JSON and populate translations map
7. If loading fails, fall back to English

**Language Change Flow**:
1. User selects new language from Language Selection Dialog
2. LocalizationManager validates language code
3. Load new language file
4. Update currentLanguage
5. Save preference to player config
6. Notify all registered listeners
7. UI components refresh their displayed text

### Language File Structure

**Location**: `assets/localization/`

**File Naming**: `{language_code}.json` (e.g., `en.json`, `pl.json`, `pt.json`, `nl.json`)

**JSON Structure**:

```json
{
  "menu": {
    "player_name": "Player Name",
    "save_world": "Save World",
    "load_world": "Load World",
    "multiplayer": "Multiplayer",
    "save_player": "Save Player",
    "language": "Language",
    "disconnect": "Disconnect",
    "exit": "Exit"
  },
  "language_dialog": {
    "title": "Select Language",
    "english": "English",
    "polish": "Polski",
    "portuguese": "Português",
    "dutch": "Nederlands",
    "current_language": "Current: {0}",
    "select_instruction": "Up/Down to select, Enter to confirm",
    "cancel_instruction": "ESC to cancel"
  },
  "multiplayer_menu": {
    "title": "Multiplayer",
    "host_server": "Host Server",
    "connect_to_server": "Connect to Server",
    "back": "Back"
  },
  "player_name_dialog": {
    "title": "Enter Player Name",
    "min_characters": "Min 3 Characters!!!",
    "instructions": "Enter, or Esc to Cancel"
  },
  "connect_dialog": {
    "title": "Connect to Server",
    "ip_address_label": "IP Address:",
    "port_label": "Port Number:",
    "tab_instruction": "Tab to switch fields",
    "confirm_instruction": "Enter to connect, ESC to cancel"
  },
  "server_host_dialog": {
    "title": "Server Started",
    "ip_label": "Server IP Address:",
    "close_instruction": "Press ESC to close"
  },
  "error_dialog": {
    "title": "Error",
    "connection_error": "Connection Error",
    "save_error": "Save Error",
    "load_error": "Load Error",
    "retry": "Retry",
    "cancel": "Cancel",
    "ok": "OK",
    "arrow_instruction": "Arrow keys to select, Enter to confirm",
    "ok_instruction": "Press Enter, Space, or ESC to continue"
  },
  "world_save_dialog": {
    "title": "Save World",
    "singleplayer_save": "Singleplayer Save",
    "multiplayer_save": "Multiplayer Save",
    "save_name_label": "Save Name:",
    "character_count": "{0}/{1}",
    "confirm_instruction": "Enter to save, ESC to cancel",
    "valid_chars_instruction": "Use letters, numbers, spaces, - and _",
    "overwrite_title": "Overwrite Existing Save?",
    "overwrite_warning_1": "This save already exists.",
    "overwrite_warning_2": "Overwrite will replace it forever.",
    "yes_overwrite": "Y - Yes, overwrite",
    "no_go_back": "N - No, go back",
    "confirm_overwrite_instruction": "Press Y to confirm or N to cancel",
    "saving_title": "Saving World...",
    "saving_message_1": "Please wait while your world is being saved.",
    "saving_message_2": "This may take a moment for large worlds.",
    "saving_progress": "Saving",
    "error_empty_name": "Save name cannot be empty",
    "error_invalid_name": "Invalid save name. Use letters, numbers, spaces, - and _"
  },
  "world_load_dialog": {
    "title": "Load World",
    "singleplayer_saves": "Singleplayer Saves",
    "multiplayer_saves": "Multiplayer Saves",
    "saves_found": "{0} save(s) found",
    "no_saves_title": "No saved worlds found.",
    "no_saves_message": "Create a save first using 'Save World'.",
    "select_instruction": "Up/Down to select, Enter to load",
    "scroll_instruction": "Page Up/Down for fast scroll",
    "refresh_instruction": "R to refresh, ESC to cancel",
    "more_above": "^ More above ^",
    "more_below": "v More below v",
    "load_confirm_title": "Load World?",
    "created_label": "Created:",
    "world_seed_label": "World Seed:",
    "trees_label": "Trees:",
    "items_label": "Items:",
    "player_position_label": "Player Position:",
    "file_size_label": "File Size:",
    "load_warning": "Loading will replace your current world!",
    "yes_load": "Y - Yes, load this world",
    "no_go_back": "N - No, go back",
    "confirm_load_instruction": "Press Y to confirm or N to cancel",
    "loading_title": "Loading World...",
    "loading_message_1": "Please wait while your world is being loaded.",
    "loading_message_2": "This may take a moment for large worlds.",
    "loading_progress": "Loading"
  },
  "world_manage_dialog": {
    "title": "Manage Saved Worlds",
    "singleplayer_saves": "Singleplayer Saves",
    "multiplayer_saves": "Multiplayer Saves",
    "no_saves_title": "No saved worlds found.",
    "no_saves_message": "Create saves using 'Save World' option.",
    "select_instruction": "Up/Down to select, Delete key to delete save",
    "scroll_instruction": "Page Up/Down for fast scroll",
    "refresh_instruction": "R to refresh, ESC to close",
    "more_above": "^ More above ^",
    "more_below": "v More below v",
    "summary_title": "--- Summary Statistics ---",
    "total_saves": "Total Saves: {0} | Total Size: {1}",
    "total_stats": "Total Trees: {0} | Total Items: {1} | Total Cleared: {2}",
    "delete_confirm_title": "Delete Save?",
    "save_name_label": "Save Name:",
    "created_label": "Created:",
    "file_size_label": "File Size:",
    "world_seed_label": "World Seed:",
    "contains_label": "Contains: {0} trees, {1} items, {2} cleared areas",
    "delete_warning_1": "WARNING: This action cannot be undone!",
    "delete_warning_2": "The save file will be permanently deleted.",
    "yes_delete": "Y - Yes, delete permanently",
    "no_keep": "N - No, keep the save",
    "confirm_delete_instruction": "Press Y to confirm deletion or N to cancel",
    "deleting_title": "Deleting Save...",
    "deleting_message": "Please wait while the save file is being deleted.",
    "deleting_progress": "Deleting",
    "delete_success": "Save deleted successfully: {0}",
    "delete_failed": "Failed to delete save: {0}"
  },
  "messages": {
    "save_restricted": "Save Restricted",
    "world_save_not_available": "World save is not available: Game not initialized.",
    "world_save_client_restricted": "World save is not available for multiplayer clients. Only the server host can save worlds.",
    "world_save_mode_restricted": "World save is only available in singleplayer mode or when hosting multiplayer games.",
    "save_failed": "Failed to save world. Please try again.",
    "save_error": "Error saving world: {0}",
    "load_failed": "Failed to load world. Save file may not exist or be corrupted.",
    "load_error": "Error loading world: {0}",
    "cannot_save_not_initialized": "Cannot save world: Game or player not initialized.",
    "cannot_load_not_initialized": "Cannot load world: Game not initialized.",
    "failed_extract_state": "Failed to extract world state. Please try again.",
    "failed_restore_state": "Failed to restore world state. Please try again."
  }
}
```

### Language Selection Dialog

**Location**: `src/main/java/wagemaker/uk/ui/LanguageDialog.java`

**Purpose**: Dialog for manual language selection

**Key Features**:
- Display all supported languages with native names
- Show current language
- Navigate with arrow keys
- Confirm with Enter, cancel with ESC
- Consistent wooden plank style with other dialogs

**Implementation**:
```java
public class LanguageDialog implements LanguageChangeListener {
    private boolean isVisible;
    private String[] languageCodes = {"en", "pl", "pt", "nl"};
    private int selectedIndex = 0;
    
    public void show() {
        // Open dialog and set current language as selected
    }
    
    public void handleInput() {
        // Handle arrow keys for navigation
        // Handle Enter to confirm selection
        // Handle ESC to cancel
    }
    
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, float camX, float camY) {
        // Render dialog with language options
    }
    
    @Override
    public void onLanguageChanged(String newLanguage) {
        // Refresh dialog text when language changes
    }
}
```

### UI Component Integration

Each UI component will be modified to use LocalizationManager instead of hardcoded strings:

**Before**:
```java
dialogFont.draw(batch, "Enter Player Name", titleX, titleY);
```

**After**:
```java
String title = LocalizationManager.getInstance().getText("player_name_dialog.title");
dialogFont.draw(batch, title, titleX, titleY);
```

**With Parameters**:
```java
String countText = LocalizationManager.getInstance().getText(
    "world_save_dialog.character_count", 
    saveNameBuffer.length(), 
    MAX_SAVE_NAME_LENGTH
);
```

**Language Change Listener**:
```java
public class GameMenu implements LanguageChangeListener {
    public GameMenu() {
        // ... existing initialization ...
        LocalizationManager.getInstance().addLanguageChangeListener(this);
    }
    
    @Override
    public void onLanguageChanged(String newLanguage) {
        // Refresh menu items array with new translations
        updateMenuItems();
    }
    
    private void updateMenuItems() {
        LocalizationManager loc = LocalizationManager.getInstance();
        singleplayerMenuItems = new String[] {
            loc.getText("menu.player_name"),
            loc.getText("menu.save_world"),
            loc.getText("menu.load_world"),
            loc.getText("menu.multiplayer"),
            loc.getText("menu.save_player"),
            loc.getText("menu.language"),
            loc.getText("menu.exit")
        };
        // Similar for multiplayerMenuItems
    }
}

## Data Models

### Translation Entry

**Structure**: Key-value pair in JSON
- **Key**: Hierarchical dot-notation string (e.g., "menu.player_name")
- **Value**: Localized text string

### Language Mapping

**System Locale to Language Code**:
- `en`, `en_US`, `en_GB`, etc. → `en` (English)
- `pl`, `pl_PL` → `pl` (Polish)
- `pt`, `pt_BR`, `pt_PT` → `pt` (Portuguese)
- `nl`, `nl_NL`, `nl_BE` → `nl` (Dutch)
- All others → `en` (Fallback)

### Translation Cache

**Structure**: `Map<String, String>`
- Loaded once at startup
- Kept in memory for fast access
- No runtime reloading (requires game restart for language changes)

## Error Handling

### Missing Translation Keys

**Behavior**:
1. Check if key exists in current language
2. If not found, check English (fallback)
3. If still not found, return the key itself as text
4. Log warning with missing key information

**Example**:
```java
public String getText(String key) {
    if (translations.containsKey(key)) {
        return translations.get(key);
    }
    
    // Try fallback language
    if (!currentLanguage.equals(DEFAULT_LANGUAGE)) {
        String fallbackText = getFallbackText(key);
        if (fallbackText != null) {
            System.out.println("Warning: Missing translation for key '" + key + 
                             "' in language '" + currentLanguage + "', using fallback");
            return fallbackText;
        }
    }
    
    // Return key itself if no translation found
    System.err.println("Error: Missing translation key '" + key + "' in all languages");
    return "[" + key + "]";
}
```

### Language File Loading Errors

**Scenarios**:
1. **File not found**: Log error, fall back to English
2. **Invalid JSON**: Log parse error, fall back to English
3. **Empty file**: Log error, fall back to English
4. **IO Exception**: Log exception, fall back to English

**Implementation**:
```java
private void loadLanguageFile(String languageCode) {
    try {
        FileHandle file = Gdx.files.internal("localization/" + languageCode + ".json");
        if (!file.exists()) {
            System.err.println("Language file not found: " + languageCode + ".json");
            if (!languageCode.equals(DEFAULT_LANGUAGE)) {
                loadLanguageFile(DEFAULT_LANGUAGE);
            }
            return;
        }
        
        String jsonContent = file.readString();
        parseJsonTranslations(jsonContent);
        currentLanguage = languageCode;
        System.out.println("Loaded language: " + languageCode);
        
    } catch (Exception e) {
        System.err.println("Error loading language file: " + e.getMessage());
        if (!languageCode.equals(DEFAULT_LANGUAGE)) {
            loadLanguageFile(DEFAULT_LANGUAGE);
        }
    }
}
```

### Language Preference Persistence

**Storage Location**: Player configuration file (woodlanders.json)

**Implementation**:
```java
private void saveLanguagePreference(String languageCode) {
    try {
        File configDir = getConfigDirectory();
        File configFile = new File(configDir, "woodlanders.json");
        
        // Read existing config
        Map<String, Object> config = loadConfig(configFile);
        
        // Update language preference
        config.put("language", languageCode);
        
        // Write back to file
        saveConfig(configFile, config);
        
        System.out.println("Language preference saved: " + languageCode);
    } catch (Exception e) {
        System.err.println("Error saving language preference: " + e.getMessage());
    }
}

private String loadLanguagePreference() {
    try {
        File configDir = getConfigDirectory();
        File configFile = new File(configDir, "woodlanders.json");
        
        if (!configFile.exists()) {
            return null;
        }
        
        Map<String, Object> config = loadConfig(configFile);
        return (String) config.get("language");
        
    } catch (Exception e) {
        System.err.println("Error loading language preference: " + e.getMessage());
        return null;
    }
}
```

### System Locale Detection Errors

**Behavior**:
1. Attempt to get system locale using `Locale.getDefault()`
2. If exception occurs, default to English
3. Log the error for debugging

## Testing Strategy

### Unit Tests

**LocalizationManager Tests**:
1. Test language detection for each supported locale
2. Test fallback to English for unsupported locales
3. Test text retrieval with valid keys
4. Test text retrieval with missing keys
5. Test parameterized text substitution
6. Test language file loading
7. Test error handling for missing files
8. Test error handling for invalid JSON

**Test Cases**:
```java
@Test
public void testEnglishLocaleDetection() {
    // Set system locale to English
    // Verify LocalizationManager loads English
}

@Test
public void testPolishLocaleDetection() {
    // Set system locale to Polish
    // Verify LocalizationManager loads Polish
}

@Test
public void testMissingKeyFallback() {
    // Request a key that doesn't exist
    // Verify fallback to English or key itself
}

@Test
public void testParameterSubstitution() {
    // Request text with parameters
    // Verify parameters are correctly substituted
}
```

### Integration Tests

**UI Component Tests**:
1. Test GameMenu displays correct language
2. Test all dialogs display correct language
3. Test language consistency across all UI components
4. Test parameter substitution in dynamic messages

### Manual Testing

**Test Scenarios**:
1. Start game with English system locale → Verify English UI
2. Start game with Polish system locale → Verify Polish UI
3. Start game with Portuguese system locale → Verify Portuguese UI
4. Start game with Dutch system locale → Verify Dutch UI
5. Start game with unsupported locale → Verify English fallback
6. Navigate through all menus → Verify all text is translated
7. Trigger all dialogs → Verify all text is translated
8. Test error messages → Verify translations
9. Test dynamic messages with parameters → Verify correct formatting

### Translation Completeness Verification

**Process**:
1. Extract all translation keys from English file
2. For each supported language:
   - Load language file
   - Compare keys with English file
   - Report missing keys
   - Report extra keys (not in English)
3. Generate completeness report

**Tool** (optional):
```java
public class TranslationValidator {
    public static void main(String[] args) {
        Map<String, String> englishKeys = loadKeys("en.json");
        
        for (String lang : new String[]{"pl", "pt", "nl"}) {
            Map<String, String> langKeys = loadKeys(lang + ".json");
            
            Set<String> missing = new HashSet<>(englishKeys.keySet());
            missing.removeAll(langKeys.keySet());
            
            Set<String> extra = new HashSet<>(langKeys.keySet());
            extra.removeAll(englishKeys.keySet());
            
            System.out.println("Language: " + lang);
            System.out.println("Missing keys: " + missing);
            System.out.println("Extra keys: " + extra);
            System.out.println("Completeness: " + 
                (100.0 * langKeys.size() / englishKeys.size()) + "%");
        }
    }
}
```

## Implementation Notes

### Performance Considerations

1. **Startup Time**: Language file loading adds ~50-100ms to startup time
2. **Memory Usage**: Each language file ~10-20KB in memory
3. **Text Retrieval**: O(1) HashMap lookup, <1ms per call
4. **No Runtime Overhead**: Translations cached at startup

### Extensibility

**Adding New Languages**:
1. Create new JSON file (e.g., `fr.json` for French)
2. Add language code to `SUPPORTED_LANGUAGES` array
3. Add locale mapping in `detectSystemLocale()`
4. Translate all keys from English file

**Adding New Text**:
1. Add key-value pair to English file
2. Add same key to all other language files
3. Update UI component to use new key
4. Run translation completeness check

### Maintenance

**Translation Updates**:
- Centralized in JSON files
- No code changes required for text updates
- Version control tracks translation changes
- Easy to outsource translations

**Quality Assurance**:
- Native speakers review translations
- Context provided for each key
- Screenshots for visual context
- Glossary for consistent terminology

## File Organization

```
assets/
└── localization/
    ├── en.json          # English (default/fallback)
    ├── pl.json          # Polish
    ├── pt.json          # Portuguese
    └── nl.json          # Dutch

src/main/java/wagemaker/uk/
└── localization/
    └── LocalizationManager.java
```

## Dependencies

**Required Libraries**:
- LibGDX core (already included)
- Java standard library (Locale, Map, etc.)
- JSON parsing (use LibGDX's built-in JSON support or lightweight parser)

**No Additional Dependencies Required**: The implementation uses only existing project dependencies.
