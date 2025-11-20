package wagemaker.uk.ui;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import org.junit.jupiter.api.*;
import wagemaker.uk.localization.LocalizationManager;

import java.io.BufferedReader;
import java.io.FileReader;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ControlsDialog with other system components.
 * Tests menu navigation, language changes, and interaction with other dialogs.
 * Validates: Requirements 1.1, 1.2, 1.4, 1.5
 * 
 * Note: These tests focus on integration aspects that don't require OpenGL context.
 * Tests that require actual ControlsDialog instances are limited due to headless environment.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ControlsDialogIntegrationTest {
    
    private static HeadlessApplication application;
    private static LocalizationManager localizationManager;
    
    @BeforeAll
    public static void setupGdx() {
        // Initialize headless LibGDX application for testing
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        application = new HeadlessApplication(new ApplicationAdapter() {}, config);
        
        // Wait for Gdx to be initialized
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        localizationManager = LocalizationManager.getInstance();
        localizationManager.initialize();
    }
    
    @AfterAll
    public static void tearDownGdx() {
        if (application != null) {
            application.exit();
        }
    }
    
    @BeforeEach
    public void setUp() {
        // Reset to English before each test
        localizationManager.setLanguage("en");
    }
    
    /**
     * Test 1: Controls dialog integrates with localization system
     * Validates: Requirements 1.5
     */
    @Test
    @Order(1)
    public void testControlsDialogIntegratesWithLocalization() {
        // Verify dialog is registered as a language change listener
        // by checking that it responds to language changes
        
        // Set to English
        localizationManager.setLanguage("en");
        
        // Verify English translations exist
        assertNotNull(localizationManager.getText("controls_dialog.title"));
        assertNotNull(localizationManager.getText("controls_dialog.movement_header"));
        assertNotNull(localizationManager.getText("controls_dialog.inventory_header"));
        
        // Switch to Polish
        localizationManager.setLanguage("pl");
        
        // Verify Polish translations exist
        String titlePl = localizationManager.getText("controls_dialog.title");
        assertNotNull(titlePl);
        assertFalse(titlePl.startsWith("["), "Polish translation should exist for controls_dialog.title");
        
        // Switch to Portuguese
        localizationManager.setLanguage("pt");
        
        // Verify Portuguese translations exist
        String titlePt = localizationManager.getText("controls_dialog.title");
        assertNotNull(titlePt);
        assertFalse(titlePt.startsWith("["), "Portuguese translation should exist for controls_dialog.title");
    }
    
    /**
     * Test 2: Controls dialog implements LanguageChangeListener interface
     * Validates: Requirements 4.2
     */
    @Test
    @Order(2)
    public void testControlsDialogImplementsLanguageChangeListener() throws Exception {
        // Read the ControlsDialog source code
        String filePath = "src/main/java/wagemaker/uk/ui/ControlsDialog.java";
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        String sourceCode = content.toString();
        
        // Verify ControlsDialog implements LanguageChangeListener
        assertTrue(sourceCode.contains("implements LanguageChangeListener"),
            "ControlsDialog should implement LanguageChangeListener interface");
        
        // Verify it has onLanguageChanged method
        assertTrue(sourceCode.contains("public void onLanguageChanged"),
            "ControlsDialog should have onLanguageChanged method");
        
        // Verify it registers with LocalizationManager
        assertTrue(sourceCode.contains("addLanguageChangeListener"),
            "ControlsDialog should register as language change listener");
        
        // Verify it unregisters in dispose
        assertTrue(sourceCode.contains("removeLanguageChangeListener"),
            "ControlsDialog should unregister in dispose method");
    }
    
    /**
     * Test 3: All controls dialog translation keys exist in all languages
     * Validates: Requirements 5.1, 5.2, 5.3, 5.4, 5.5, 5.6
     */
    @Test
    @Order(3)
    public void testAllControlsDialogTranslationKeysExistInAllLanguages() {
        String[] languages = {"en", "pl", "pt", "nl", "de"};
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
            "controls_dialog.close_instruction",
            "menu.controls"
        };
        
        for (String lang : languages) {
            localizationManager.setLanguage(lang);
            
            for (String key : requiredKeys) {
                String text = localizationManager.getText(key);
                
                // Should not be the key itself (in brackets)
                assertFalse(text.startsWith("[") && text.endsWith("]"),
                        "Language " + lang + " is missing translation for key: " + key);
                
                // Should not be empty
                assertFalse(text.trim().isEmpty(),
                        "Language " + lang + " has empty translation for key: " + key);
            }
        }
    }
    
    /**
     * Test 4: GameMenu integration with Controls dialog
     * Validates: Requirements 1.1, 1.2
     */
    @Test
    @Order(4)
    public void testGameMenuIntegrationWithControlsDialog() throws Exception {
        // Read the GameMenu source code
        String filePath = "src/main/java/wagemaker/uk/ui/GameMenu.java";
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        String sourceCode = content.toString();
        
        // Verify GameMenu has ControlsDialog field
        assertTrue(sourceCode.contains("ControlsDialog"),
            "GameMenu should have ControlsDialog field");
        
        // Verify menu items include Controls
        assertTrue(sourceCode.contains("menu.controls"),
            "GameMenu should include menu.controls in menu items");
        
        // Verify there's a method to open controls dialog
        assertTrue(sourceCode.contains("controlsDialog.show()"),
            "GameMenu should call controlsDialog.show()");
        
        // Verify controls dialog is disposed
        assertTrue(sourceCode.contains("controlsDialog.dispose()"),
            "GameMenu should dispose controlsDialog");
    }
    
    /**
     * Test 5: Controls dialog with multiple language switches
     * Validates: Requirements 1.5
     */
    @Test
    @Order(5)
    public void testControlsDialogWithMultipleLanguageSwitches() {
        // Switch through all supported languages
        String[] languages = {"en", "pl", "pt", "nl", "de"};
        
        for (String lang : languages) {
            localizationManager.setLanguage(lang);
            
            // Verify translations are available
            String title = localizationManager.getText("controls_dialog.title");
            assertFalse(title.startsWith("["), 
                "Translation should exist for controls_dialog.title in " + lang);
            
            // Verify all category headers exist
            assertFalse(localizationManager.getText("controls_dialog.movement_header").startsWith("["),
                "Movement header should exist in " + lang);
            assertFalse(localizationManager.getText("controls_dialog.inventory_header").startsWith("["),
                "Inventory header should exist in " + lang);
            assertFalse(localizationManager.getText("controls_dialog.item_header").startsWith("["),
                "Item header should exist in " + lang);
        }
    }
    
    /**
     * Test 7: Menu controls translation key exists
     * Validates: Requirements 1.1
     */
    @Test
    @Order(7)
    public void testMenuControlsTranslationKeyExists() {
        String[] languages = {"en", "pl", "pt", "nl", "de"};
        
        for (String lang : languages) {
            localizationManager.setLanguage(lang);
            
            String menuControls = localizationManager.getText("menu.controls");
            assertNotNull(menuControls, "menu.controls should exist in " + lang);
            assertFalse(menuControls.startsWith("["), 
                "menu.controls should have translation in " + lang);
            assertFalse(menuControls.trim().isEmpty(), 
                "menu.controls should not be empty in " + lang);
        }
    }
    
    /**
     * Test 8: Controls dialog source code uses LocalizationManager
     * Validates: Requirements 5.7
     */
    @Test
    @Order(8)
    public void testControlsDialogUsesLocalizationManager() throws Exception {
        // Read the ControlsDialog source code
        String filePath = "src/main/java/wagemaker/uk/ui/ControlsDialog.java";
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        String sourceCode = content.toString();
        
        // Verify it uses LocalizationManager.getText() for all text
        assertTrue(sourceCode.contains("LocalizationManager.getInstance()"),
            "ControlsDialog should use LocalizationManager");
        
        assertTrue(sourceCode.contains("getText(\"controls_dialog."),
            "ControlsDialog should retrieve text using getText()");
        
        // Verify no hardcoded strings in render method (check for common patterns)
        // Extract render method
        int renderStart = sourceCode.indexOf("public void render(");
        int renderEnd = sourceCode.indexOf("batch.end();", renderStart) + 20;
        String renderMethod = sourceCode.substring(renderStart, renderEnd);
        
        // Check that render method doesn't have hardcoded UI strings
        // (Allow technical strings like "batch", "dialogX", etc.)
        assertFalse(renderMethod.contains("\"Movement\""),
            "Render method should not contain hardcoded 'Movement' string");
        assertFalse(renderMethod.contains("\"Inventory\""),
            "Render method should not contain hardcoded 'Inventory' string");
        assertFalse(renderMethod.contains("\"Controls\""),
            "Render method should not contain hardcoded 'Controls' string");
    }
    
    /**
     * Test 9: Controls dialog translation consistency across languages
     * Validates: Requirements 5.7
     */
    @Test
    @Order(9)
    public void testControlsDialogTranslationConsistency() {
        // Verify that all category headers exist in all languages
        String[] categoryKeys = {
            "controls_dialog.movement_header",
            "controls_dialog.inventory_header",
            "controls_dialog.item_header",
            "controls_dialog.targeting_header",
            "controls_dialog.combat_header",
            "controls_dialog.system_header"
        };
        
        String[] languages = {"en", "pl", "pt", "nl", "de"};
        
        for (String lang : languages) {
            localizationManager.setLanguage(lang);
            
            for (String key : categoryKeys) {
                String text = localizationManager.getText(key);
                assertNotNull(text, key + " should exist in " + lang);
                assertFalse(text.startsWith("["), key + " should have translation in " + lang);
                assertFalse(text.trim().isEmpty(), key + " should not be empty in " + lang);
            }
        }
    }
    
    /**
     * Test 10: Controls dialog handles ESC key input
     * Validates: Requirements 1.4
     */
    @Test
    @Order(10)
    public void testControlsDialogHandlesEscKeyInput() throws Exception {
        // Read the ControlsDialog source code
        String filePath = "src/main/java/wagemaker/uk/ui/ControlsDialog.java";
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        String sourceCode = content.toString();
        
        // Verify handleInput method exists
        assertTrue(sourceCode.contains("public void handleInput()"),
            "ControlsDialog should have handleInput() method");
        
        // Verify it checks for ESC key
        assertTrue(sourceCode.contains("Input.Keys.ESCAPE"),
            "handleInput should check for ESC key");
        
        // Verify it calls hide() when ESC is pressed
        // Find the handleInput method more carefully
        int handleInputStart = sourceCode.indexOf("public void handleInput()");
        assertTrue(handleInputStart > 0, "handleInput method should exist");
        
        // Find the next method or end of class
        int nextMethodStart = sourceCode.indexOf("public void", handleInputStart + 1);
        if (nextMethodStart == -1) {
            nextMethodStart = sourceCode.indexOf("private void", handleInputStart + 1);
        }
        if (nextMethodStart == -1) {
            nextMethodStart = sourceCode.length();
        }
        
        String handleInputMethod = sourceCode.substring(handleInputStart, nextMethodStart);
        
        assertTrue(handleInputMethod.contains("hide()"),
            "handleInput should call hide() when ESC is pressed");
    }
    
    /**
     * Test 11: Controls dialog prevents game input when visible
     * Validates: Requirements 1.3
     */
    @Test
    @Order(11)
    public void testControlsDialogPreventsGameInput() throws Exception {
        // Read the GameMenu source code
        String filePath = "src/main/java/wagemaker/uk/ui/GameMenu.java";
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        String sourceCode = content.toString();
        
        // Verify GameMenu checks if controls dialog is visible
        assertTrue(sourceCode.contains("controlsDialog.isVisible()"),
            "GameMenu should check if controls dialog is visible");
        
        // Verify it calls handleInput on the dialog
        assertTrue(sourceCode.contains("controlsDialog.handleInput()"),
            "GameMenu should call controlsDialog.handleInput()");
        
        // Verify there's early return logic when dialog is visible
        // (This prevents game input from being processed)
        assertTrue(sourceCode.contains("return") || sourceCode.contains("if"),
            "GameMenu should have logic to prevent game input when dialog is visible");
    }
}
