package wagemaker.uk.ui;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for Controls Dialog translation key completeness.
 * Feature: controls-menu-dialog, Property 7: Translation keys exist in all languages
 * Validates: Requirements 5.1, 5.2-5.6
 */
public class ControlsDialogTranslationPropertyTest {
    
    private static final String[] SUPPORTED_LANGUAGES = {"en", "de", "nl", "pl", "pt"};
    private static final String REFERENCE_LANGUAGE = "en";
    private static HeadlessApplication application;
    
    // All required translation keys for the controls dialog
    private static final String[] REQUIRED_KEYS = {
        "menu.controls",
        "controls_dialog.title",
        "controls_dialog.movement_header",
        "controls_dialog.movement_up",
        "controls_dialog.movement_down",
        "controls_dialog.movement_left",
        "controls_dialog.movement_right",
        "controls_dialog.inventory_header",
        "controls_dialog.inventory_open",
        "controls_dialog.inventory_navigate_left",
        "controls_dialog.inventory_navigate_right",
        "controls_dialog.item_header",
        "controls_dialog.item_plant_p",
        "controls_dialog.item_plant_space",
        "controls_dialog.item_consume",
        "controls_dialog.targeting_header",
        "controls_dialog.targeting_up",
        "controls_dialog.targeting_down",
        "controls_dialog.targeting_left",
        "controls_dialog.targeting_right",
        "controls_dialog.combat_header",
        "controls_dialog.combat_attack",
        "controls_dialog.system_header",
        "controls_dialog.system_menu",
        "controls_dialog.system_delete_world",
        "controls_dialog.system_compass_target",
        "controls_dialog.close_instruction"
    };
    
    @BeforeAll
    public static void setupGdx() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        application = new HeadlessApplication(new ApplicationAdapter() {}, config);
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    @AfterAll
    public static void tearDownGdx() {
        if (application != null) {
            application.exit();
        }
    }
    
    /**
     * Property 7: Translation keys exist in all languages
     * For any required translation key for the Controls Dialog, that key should exist 
     * in all supported language files (en.json, de.json, nl.json, pl.json, pt.json)
     * Validates: Requirements 5.1, 5.2-5.6
     * 
     * This property-based test runs 100 trials, randomly selecting keys from REQUIRED_KEYS
     * to verify they exist in all language files.
     */
    @Test
    public void translationKeyExistsInAllLanguages() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Randomly select a required key
            String requiredKey = REQUIRED_KEYS[random.nextInt(REQUIRED_KEYS.length)];
            
            // Verify it exists in all languages
            for (String lang : SUPPORTED_LANGUAGES) {
                Set<String> langKeys = loadTranslationKeys(lang);
                
                assertTrue(
                    langKeys.contains(requiredKey),
                    "Translation key '" + requiredKey + "' should exist in " + lang + ".json"
                );
            }
        }
    }
    
    /**
     * Property: Translation values are non-empty
     * For any required translation key, the value should not be empty in any language
     * 
     * This property-based test runs 100 trials, randomly selecting keys from REQUIRED_KEYS
     * to verify their values are non-empty in all language files.
     */
    @Test
    public void translationValuesAreNonEmpty() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Randomly select a required key
            String requiredKey = REQUIRED_KEYS[random.nextInt(REQUIRED_KEYS.length)];
            
            // Verify value is non-empty in all languages
            for (String lang : SUPPORTED_LANGUAGES) {
                Map<String, String> translations = loadAllTranslations(lang);
                
                assertTrue(
                    translations.containsKey(requiredKey),
                    "Translation key '" + requiredKey + "' should exist in " + lang + ".json"
                );
                
                String value = translations.get(requiredKey);
                assertNotNull(
                    value,
                    "Translation value for '" + requiredKey + "' should not be null in " + lang + ".json"
                );
                
                assertFalse(
                    value.trim().isEmpty(),
                    "Translation value for '" + requiredKey + "' should not be empty in " + lang + ".json"
                );
            }
        }
    }
    
    // Helper methods
    
    private Set<String> loadTranslationKeys(String languageCode) {
        FileHandle file = Gdx.files.internal("localization/" + languageCode + ".json");
        String jsonContent = file.readString();
        
        Json json = new Json();
        JsonValue root = json.fromJson(null, jsonContent);
        
        Set<String> keys = new HashSet<>();
        flattenJsonKeys(root, "", keys);
        
        return keys;
    }
    
    private Map<String, String> loadAllTranslations(String languageCode) {
        FileHandle file = Gdx.files.internal("localization/" + languageCode + ".json");
        String jsonContent = file.readString();
        
        Json json = new Json();
        JsonValue root = json.fromJson(null, jsonContent);
        
        Map<String, String> translations = new HashMap<>();
        flattenJsonToMap(root, "", translations);
        
        return translations;
    }
    
    private void flattenJsonKeys(JsonValue jsonValue, String prefix, Set<String> keys) {
        if (jsonValue == null) {
            return;
        }
        
        for (JsonValue child = jsonValue.child; child != null; child = child.next) {
            String key = prefix.isEmpty() ? child.name : prefix + "." + child.name;
            
            if (child.isObject()) {
                flattenJsonKeys(child, key, keys);
            } else if (child.isString()) {
                keys.add(key);
            }
        }
    }
    
    private void flattenJsonToMap(JsonValue jsonValue, String prefix, Map<String, String> map) {
        if (jsonValue == null) {
            return;
        }
        
        for (JsonValue child = jsonValue.child; child != null; child = child.next) {
            String key = prefix.isEmpty() ? child.name : prefix + "." + child.name;
            
            if (child.isObject()) {
                flattenJsonToMap(child, key, map);
            } else if (child.isString()) {
                map.put(key, child.asString());
            }
        }
    }
}
