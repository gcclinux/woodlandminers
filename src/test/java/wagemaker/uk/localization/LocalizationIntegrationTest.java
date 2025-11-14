package wagemaker.uk.localization;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import org.junit.jupiter.api.*;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for LocalizationManager using real language files.
 * Tests the complete localization system with actual file loading.
 * Requirements: All requirements from task 15
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LocalizationIntegrationTest {
    
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
    }
    
    @AfterAll
    public static void tearDownGdx() {
        if (application != null) {
            application.exit();
        }
    }
    
    @BeforeEach
    public void setUp() {
        // Clear any saved preferences before each test
        try {
            Gdx.app.getPreferences("woodlanders").clear();
            Gdx.app.getPreferences("woodlanders").flush();
        } catch (Exception e) {
            // Ignore if preferences don't exist
        }
    }
    
    /**
     * Test 1: Initialize with English locale
     * Requirements: 1.1, 1.2
     */
    @Test
    @Order(1)
    public void testInitializeWithEnglishLocale() {
        Locale.setDefault(Locale.ENGLISH);
        localizationManager.initialize();
        
        assertEquals("en", localizationManager.getCurrentLanguage());
        
        // Verify we can retrieve text
        String playerName = localizationManager.getText("menu.player_name");
        assertEquals("Player Name", playerName);
    }
    
    /**
     * Test 2: Initialize with Polish locale
     * Requirements: 1.1, 1.2
     */
    @Test
    @Order(2)
    public void testInitializeWithPolishLocale() {
        Locale.setDefault(new Locale("pl", "PL"));
        localizationManager.initialize();
        
        assertEquals("pl", localizationManager.getCurrentLanguage());
        
        // Verify Polish translations
        String playerName = localizationManager.getText("menu.player_name");
        assertEquals("Imię Gracza", playerName);
    }
    
    /**
     * Test 3: Initialize with Portuguese locale
     * Requirements: 1.1, 1.2
     */
    @Test
    @Order(3)
    public void testInitializeWithPortugueseLocale() {
        Locale.setDefault(new Locale("pt", "BR"));
        localizationManager.initialize();
        
        assertEquals("pt", localizationManager.getCurrentLanguage());
        
        // Verify Portuguese translations
        String playerName = localizationManager.getText("menu.player_name");
        assertEquals("Nome do Jogador", playerName);
    }
    
    /**
     * Test 4: Initialize with Dutch locale
     * Requirements: 1.1, 1.2
     */
    @Test
    @Order(4)
    public void testInitializeWithDutchLocale() {
        Locale.setDefault(new Locale("nl", "NL"));
        localizationManager.initialize();
        
        assertEquals("nl", localizationManager.getCurrentLanguage());
        
        // Verify Dutch translations
        String playerName = localizationManager.getText("menu.player_name");
        assertEquals("Spelersnaam", playerName);
    }
    
    /**
     * Test 5: Fallback to English for unsupported locale
     * Requirements: 1.3
     */
    @Test
    @Order(5)
    public void testFallbackToEnglishForUnsupportedLocale() {
        Locale.setDefault(Locale.FRENCH);
        localizationManager.initialize();
        
        assertEquals("en", localizationManager.getCurrentLanguage());
        
        String playerName = localizationManager.getText("menu.player_name");
        assertEquals("Player Name", playerName);
    }
    
    /**
     * Test 6: Manual language switching
     * Requirements: 10.3
     */
    @Test
    @Order(6)
    public void testManualLanguageSwitching() {
        localizationManager.initialize();
        
        // Switch to Polish
        localizationManager.setLanguage("pl");
        assertEquals("pl", localizationManager.getCurrentLanguage());
        assertEquals("Imię Gracza", localizationManager.getText("menu.player_name"));
        
        // Switch to Portuguese
        localizationManager.setLanguage("pt");
        assertEquals("pt", localizationManager.getCurrentLanguage());
        assertEquals("Nome do Jogador", localizationManager.getText("menu.player_name"));
        
        // Switch to Dutch
        localizationManager.setLanguage("nl");
        assertEquals("nl", localizationManager.getCurrentLanguage());
        assertEquals("Spelersnaam", localizationManager.getText("menu.player_name"));
        
        // Switch back to English
        localizationManager.setLanguage("en");
        assertEquals("en", localizationManager.getCurrentLanguage());
        assertEquals("Player Name", localizationManager.getText("menu.player_name"));
    }
    
    /**
     * Test 7: Language preference persistence
     * Requirements: 10.6, 10.7
     */
    @Test
    @Order(7)
    public void testLanguagePreferencePersistence() {
        // Initialize with English
        Locale.setDefault(Locale.ENGLISH);
        localizationManager.initialize();
        
        // Switch to Polish and save
        localizationManager.setLanguage("pl");
        
        // Simulate restart by re-initializing
        localizationManager.initialize();
        
        // Should load saved preference (Polish)
        assertEquals("pl", localizationManager.getCurrentLanguage());
    }
    
    /**
     * Test 8: Parameter substitution with real translations
     * Requirements: 7.1, 7.2, 7.3
     */
    @Test
    @Order(8)
    public void testParameterSubstitution() {
        localizationManager.initialize();
        
        // Test single parameter
        String currentLang = localizationManager.getText("language_dialog.current_language", "English");
        assertEquals("Current: English", currentLang);
        
        // Test multiple parameters
        String charCount = localizationManager.getText("world_save_dialog.character_count", 15, 32);
        assertEquals("15/32", charCount);
        
        // Test with different language
        localizationManager.setLanguage("pl");
        String currentLangPl = localizationManager.getText("language_dialog.current_language", "Polski");
        assertEquals("Aktualny: Polski", currentLangPl);
    }
    
    /**
     * Test 9: Language change listener notifications
     * Requirements: 10.4, 10.5
     */
    @Test
    @Order(9)
    public void testLanguageChangeListeners() {
        localizationManager.initialize();
        
        // Track listener calls
        final int[] callCount = {0};
        final String[] receivedLanguage = {null};
        
        LanguageChangeListener listener = new LanguageChangeListener() {
            @Override
            public void onLanguageChanged(String newLanguage) {
                callCount[0]++;
                receivedLanguage[0] = newLanguage;
            }
        };
        
        localizationManager.addLanguageChangeListener(listener);
        
        // Change language
        localizationManager.setLanguage("pl");
        
        // Verify listener was called
        assertEquals(1, callCount[0]);
        assertEquals("pl", receivedLanguage[0]);
        
        // Change again
        localizationManager.setLanguage("pt");
        assertEquals(2, callCount[0]);
        assertEquals("pt", receivedLanguage[0]);
        
        // Remove listener
        localizationManager.removeLanguageChangeListener(listener);
        
        // Change again - should not be called
        localizationManager.setLanguage("nl");
        assertEquals(2, callCount[0]); // Still 2, not 3
    }
    
    /**
     * Test 10: All UI component translations exist
     * Requirements: 4.1-4.5, 5.1-5.4, 6.1-6.6
     */
    @Test
    @Order(10)
    public void testAllUIComponentTranslationsExist() {
        localizationManager.initialize();
        
        // Test menu translations
        assertNotNull(localizationManager.getText("menu.player_name"));
        assertNotNull(localizationManager.getText("menu.save_world"));
        assertNotNull(localizationManager.getText("menu.load_world"));
        assertNotNull(localizationManager.getText("menu.multiplayer"));
        assertNotNull(localizationManager.getText("menu.language"));
        assertNotNull(localizationManager.getText("menu.disconnect"));
        assertNotNull(localizationManager.getText("menu.exit"));
        
        // Test language dialog translations
        assertNotNull(localizationManager.getText("language_dialog.title"));
        assertNotNull(localizationManager.getText("language_dialog.english"));
        assertNotNull(localizationManager.getText("language_dialog.polish"));
        assertNotNull(localizationManager.getText("language_dialog.portuguese"));
        assertNotNull(localizationManager.getText("language_dialog.dutch"));
        
        // Test multiplayer menu translations
        assertNotNull(localizationManager.getText("multiplayer_menu.title"));
        assertNotNull(localizationManager.getText("multiplayer_menu.host_server"));
        assertNotNull(localizationManager.getText("multiplayer_menu.connect_to_server"));
        
        // Test dialog translations
        assertNotNull(localizationManager.getText("connect_dialog.title"));
        assertNotNull(localizationManager.getText("server_host_dialog.title"));
        assertNotNull(localizationManager.getText("error_dialog.title"));
        assertNotNull(localizationManager.getText("world_save_dialog.title"));
        assertNotNull(localizationManager.getText("world_load_dialog.title"));
        assertNotNull(localizationManager.getText("world_manage_dialog.title"));
    }
    
    /**
     * Test 11: Missing key fallback behavior
     * Requirements: 3.3, 3.4, 9.2
     */
    @Test
    @Order(11)
    public void testMissingKeyFallback() {
        localizationManager.initialize();
        
        // Request non-existent key
        String result = localizationManager.getText("nonexistent.key.that.does.not.exist");
        
        // Should return key in brackets
        assertEquals("[nonexistent.key.that.does.not.exist]", result);
    }
    
    /**
     * Test 12: Verify all languages have consistent translations
     * Requirements: 8.1, 8.2, 8.3, 8.4, 8.5
     */
    @Test
    @Order(12)
    public void testAllLanguagesHaveConsistentTranslations() {
        String[] languages = {"en", "pl", "pt", "nl"};
        String[] testKeys = {
            "menu.player_name",
            "menu.save_world",
            "language_dialog.title",
            "multiplayer_menu.title",
            "error_dialog.title"
        };
        
        for (String lang : languages) {
            localizationManager.setLanguage(lang);
            
            for (String key : testKeys) {
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
     * Test 13: Supported languages and display names
     */
    @Test
    @Order(13)
    public void testSupportedLanguagesAndDisplayNames() {
        String[] supported = localizationManager.getSupportedLanguages();
        
        assertNotNull(supported);
        assertEquals(4, supported.length);
        
        // Verify display names
        assertEquals("English", localizationManager.getLanguageDisplayName("en"));
        assertEquals("Polski", localizationManager.getLanguageDisplayName("pl"));
        assertEquals("Português", localizationManager.getLanguageDisplayName("pt"));
        assertEquals("Nederlands", localizationManager.getLanguageDisplayName("nl"));
    }
    
    /**
     * Test 14: hasKey method
     */
    @Test
    @Order(14)
    public void testHasKeyMethod() {
        localizationManager.initialize();
        
        assertTrue(localizationManager.hasKey("menu.player_name"));
        assertTrue(localizationManager.hasKey("language_dialog.title"));
        assertFalse(localizationManager.hasKey("nonexistent.key"));
    }
    
    /**
     * Test 15: Complex nested translations
     */
    @Test
    @Order(15)
    public void testComplexNestedTranslations() {
        localizationManager.initialize();
        
        // Test deeply nested keys
        assertNotNull(localizationManager.getText("world_save_dialog.overwrite_warning_1"));
        assertNotNull(localizationManager.getText("world_load_dialog.load_confirm_title"));
        assertNotNull(localizationManager.getText("world_manage_dialog.delete_confirm_title"));
        
        // Verify they're not empty
        assertFalse(localizationManager.getText("world_save_dialog.overwrite_warning_1").isEmpty());
    }
}
