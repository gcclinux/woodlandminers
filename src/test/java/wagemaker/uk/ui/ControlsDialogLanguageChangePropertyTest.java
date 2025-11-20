package wagemaker.uk.ui;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import wagemaker.uk.localization.LocalizationManager;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for Controls Dialog language change behavior.
 * Feature: controls-menu-dialog, Property 6: Language changes update text
 * Validates: Requirements 1.5, 4.5
 */
public class ControlsDialogLanguageChangePropertyTest {
    
    private static final String[] SUPPORTED_LANGUAGES = {"en", "de", "nl", "pl", "pt"};
    private static HeadlessApplication application;
    
    @BeforeAll
    public static void setupGdx() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        application = new HeadlessApplication(new ApplicationAdapter() {}, config);
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Initialize localization
        LocalizationManager.getInstance().initialize();
    }
    
    @AfterAll
    public static void tearDownGdx() {
        if (application != null) {
            application.exit();
        }
    }
    
    /**
     * Property 6: Language changes update text
     * For any supported language, when setLanguage() is called on LocalizationManager, 
     * the Controls Dialog should retrieve and display text in the new language
     * Validates: Requirements 1.5, 4.5
     * 
     * This property-based test runs 100 trials with random language selections.
     */
    @Test
    public void languageChangesUpdateText() {
        Random random = new Random(42); // Fixed seed for reproducibility
        LocalizationManager locManager = LocalizationManager.getInstance();
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Randomly select a language
            String targetLanguage = SUPPORTED_LANGUAGES[random.nextInt(SUPPORTED_LANGUAGES.length)];
            
            // Change language
            locManager.setLanguage(targetLanguage);
            
            // Verify current language is set correctly
            assertEquals(
                targetLanguage,
                locManager.getCurrentLanguage(),
                "LocalizationManager should report correct current language"
            );
            
            // Verify that getText() returns text in the new language
            // We can't verify the actual translation, but we can verify the key exists
            String title = locManager.getText("controls_dialog.title");
            assertNotNull(title, "Title text should not be null");
            assertFalse(title.isEmpty(), "Title text should not be empty");
            assertFalse(
                title.startsWith("[") && title.endsWith("]"),
                "Title text should not be a missing key indicator for language: " + targetLanguage
            );
            
            // Verify other required keys exist in the new language
            String[] requiredKeys = {
                "controls_dialog.movement_header",
                "controls_dialog.inventory_header",
                "controls_dialog.item_header",
                "controls_dialog.targeting_header",
                "controls_dialog.combat_header",
                "controls_dialog.system_header",
                "controls_dialog.close_instruction"
            };
            
            for (String key : requiredKeys) {
                String text = locManager.getText(key);
                assertNotNull(text, "Text for key '" + key + "' should not be null");
                assertFalse(text.isEmpty(), "Text for key '" + key + "' should not be empty");
                assertFalse(
                    text.startsWith("[") && text.endsWith("]"),
                    "Text for key '" + key + "' should not be a missing key indicator for language: " + targetLanguage
                );
            }
        }
    }
    
    /**
     * Property: Language changes are reflected immediately
     * For any sequence of language changes, each change should immediately affect
     * the text returned by LocalizationManager.getText().
     * 
     * This property-based test runs 100 trials with random language change sequences.
     */
    @Test
    public void languageChangesAreReflectedImmediately() {
        Random random = new Random(42); // Fixed seed for reproducibility
        LocalizationManager locManager = LocalizationManager.getInstance();
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Generate a random sequence of language changes (2 to 5 changes)
            int changeCount = random.nextInt(4) + 2;
            
            for (int i = 0; i < changeCount; i++) {
                String targetLanguage = SUPPORTED_LANGUAGES[random.nextInt(SUPPORTED_LANGUAGES.length)];
                
                // Change language
                locManager.setLanguage(targetLanguage);
                
                // Immediately verify the change took effect
                assertEquals(
                    targetLanguage,
                    locManager.getCurrentLanguage(),
                    "Language change should take effect immediately"
                );
                
                // Verify getText() returns text in the new language
                String title = locManager.getText("controls_dialog.title");
                assertFalse(
                    title.startsWith("[") && title.endsWith("]"),
                    "getText() should return valid text immediately after language change to: " + targetLanguage
                );
            }
        }
    }
    
    /**
     * Property: All supported languages have complete translations
     * For any supported language, all required translation keys should exist
     * and have non-empty values.
     * 
     * This property-based test verifies translation completeness across all languages.
     */
    @Test
    public void allSupportedLanguagesHaveCompleteTranslations() {
        LocalizationManager locManager = LocalizationManager.getInstance();
        
        String[] requiredKeys = {
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
        
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials, randomly selecting language-key combinations
        for (int trial = 0; trial < 100; trial++) {
            String language = SUPPORTED_LANGUAGES[random.nextInt(SUPPORTED_LANGUAGES.length)];
            String key = requiredKeys[random.nextInt(requiredKeys.length)];
            
            // Set language
            locManager.setLanguage(language);
            
            // Verify key exists and has non-empty value
            String text = locManager.getText(key);
            assertNotNull(
                text,
                "Key '" + key + "' should exist in language: " + language
            );
            assertFalse(
                text.isEmpty(),
                "Key '" + key + "' should have non-empty value in language: " + language
            );
            assertFalse(
                text.startsWith("[") && text.endsWith("]"),
                "Key '" + key + "' should not be missing in language: " + language
            );
        }
    }
}
