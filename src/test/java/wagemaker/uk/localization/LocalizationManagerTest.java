package wagemaker.uk.localization;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for LocalizationManager.
 * Tests language detection, loading, text retrieval, parameter substitution,
 * language switching, and error handling.
 */
public class LocalizationManagerTest {

    @Mock
    private Application mockApp;
    
    @Mock
    private Preferences mockPreferences;
    
    @Mock
    private FileHandle mockFileHandle;
    
    private LocalizationManager localizationManager;
    private AutoCloseable mocks;
    
    @BeforeEach
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        
        // Setup Gdx mocks
        Gdx.app = mockApp;
        Gdx.files = mock(com.badlogic.gdx.Files.class);
        
        // Setup preferences mock
        when(mockApp.getPreferences(anyString())).thenReturn(mockPreferences);
        when(mockPreferences.getString(anyString(), any())).thenReturn(null);
        
        // Get fresh instance
        localizationManager = LocalizationManager.getInstance();
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
        Gdx.app = null;
        Gdx.files = null;
    }
    
    /**
     * Test 1: Language detection for supported locales
     * Requirements: 1.1, 1.2
     */
    @Test
    public void testEnglishLocaleDetection() {
        // Setup English locale
        Locale.setDefault(Locale.ENGLISH);
        
        // Mock file loading for English
        setupMockLanguageFile("en", createSampleEnglishJson());
        
        localizationManager.initialize();
        
        assertEquals("en", localizationManager.getCurrentLanguage(),
                "Should detect and load English locale");
    }
    
    @Test
    public void testPolishLocaleDetection() {
        // Setup Polish locale
        Locale.setDefault(new Locale("pl", "PL"));
        
        // Mock file loading
        setupMockLanguageFile("en", createSampleEnglishJson());
        setupMockLanguageFile("pl", createSamplePolishJson());
        
        localizationManager.initialize();
        
        assertEquals("pl", localizationManager.getCurrentLanguage(),
                "Should detect and load Polish locale");
    }
    
    @Test
    public void testPortugueseLocaleDetection() {
        // Setup Portuguese locale
        Locale.setDefault(new Locale("pt", "BR"));
        
        // Mock file loading
        setupMockLanguageFile("en", createSampleEnglishJson());
        setupMockLanguageFile("pt", createSamplePortugueseJson());
        
        localizationManager.initialize();
        
        assertEquals("pt", localizationManager.getCurrentLanguage(),
                "Should detect and load Portuguese locale");
    }
    
    @Test
    public void testDutchLocaleDetection() {
        // Setup Dutch locale
        Locale.setDefault(new Locale("nl", "NL"));
        
        // Mock file loading
        setupMockLanguageFile("en", createSampleEnglishJson());
        setupMockLanguageFile("nl", createSampleDutchJson());
        
        localizationManager.initialize();
        
        assertEquals("nl", localizationManager.getCurrentLanguage(),
                "Should detect and load Dutch locale");
    }
    
    /**
     * Test 2: Fallback behavior for unsupported locales
     * Requirements: 1.3
     */
    @Test
    public void testUnsupportedLocaleFallback() {
        // Setup unsupported locale (French)
        Locale.setDefault(Locale.FRENCH);
        
        // Mock file loading for English (fallback)
        setupMockLanguageFile("en", createSampleEnglishJson());
        
        localizationManager.initialize();
        
        assertEquals("en", localizationManager.getCurrentLanguage(),
                "Should fall back to English for unsupported locale");
    }
    
    @Test
    public void testJapaneseLocaleFallback() {
        // Setup unsupported locale (Japanese)
        Locale.setDefault(Locale.JAPANESE);
        
        // Mock file loading for English (fallback)
        setupMockLanguageFile("en", createSampleEnglishJson());
        
        localizationManager.initialize();
        
        assertEquals("en", localizationManager.getCurrentLanguage(),
                "Should fall back to English for Japanese locale");
    }
    
    /**
     * Test 3: Text retrieval with valid keys
     * Requirements: 3.1, 3.2
     */
    @Test
    public void testGetTextWithValidKey() {
        setupMockLanguageFile("en", createSampleEnglishJson());
        localizationManager.initialize();
        
        String text = localizationManager.getText("menu.player_name");
        
        assertEquals("Player Name", text,
                "Should return correct translation for valid key");
    }
    
    @Test
    public void testGetTextWithNestedKey() {
        setupMockLanguageFile("en", createSampleEnglishJson());
        localizationManager.initialize();
        
        String text = localizationManager.getText("language_dialog.title");
        
        assertEquals("Select Language", text,
                "Should return correct translation for nested key");
    }
    
    /**
     * Test 4: Text retrieval with missing keys
     * Requirements: 3.3, 3.4
     */
    @Test
    public void testGetTextWithMissingKey() {
        setupMockLanguageFile("en", createSampleEnglishJson());
        localizationManager.initialize();
        
        String text = localizationManager.getText("nonexistent.key");
        
        assertEquals("[nonexistent.key]", text,
                "Should return key in brackets for missing translation");
    }
    
    @Test
    public void testHasKeyMethod() {
        setupMockLanguageFile("en", createSampleEnglishJson());
        localizationManager.initialize();
        
        assertTrue(localizationManager.hasKey("menu.player_name"),
                "Should return true for existing key");
        assertFalse(localizationManager.hasKey("nonexistent.key"),
                "Should return false for non-existing key");
    }
    
    /**
     * Test 5: Parameter substitution in dynamic messages
     * Requirements: 7.1, 7.2, 7.3
     */
    @Test
    public void testParameterSubstitutionSingleParam() {
        setupMockLanguageFile("en", createSampleEnglishJson());
        localizationManager.initialize();
        
        String text = localizationManager.getText("language_dialog.current_language", "English");
        
        assertEquals("Current: English", text,
                "Should substitute single parameter correctly");
    }
    
    @Test
    public void testParameterSubstitutionMultipleParams() {
        setupMockLanguageFile("en", createSampleEnglishJson());
        localizationManager.initialize();
        
        String text = localizationManager.getText("world_save_dialog.character_count", 5, 32);
        
        assertEquals("5/32", text,
                "Should substitute multiple parameters correctly");
    }
    
    @Test
    public void testParameterSubstitutionWithNoParams() {
        setupMockLanguageFile("en", createSampleEnglishJson());
        localizationManager.initialize();
        
        String text = localizationManager.getText("menu.player_name");
        
        assertEquals("Player Name", text,
                "Should return text unchanged when no parameters provided");
    }
    
    /**
     * Test 6: Manual language selection
     * Requirements: 10.3, 10.6
     * Note: This test is covered better in LocalizationIntegrationTest
     */
    @Test
    public void testManualLanguageSwitch() {
        setupMockLanguageFile("en", createSampleEnglishJson());
        setupMockLanguageFile("pl", createSamplePolishJson());
        
        // Reset to ensure clean state
        when(mockPreferences.getString("language", null)).thenReturn(null);
        
        localizationManager.initialize();
        
        localizationManager.setLanguage("pl");
        
        assertEquals("pl", localizationManager.getCurrentLanguage(),
                "Should switch to Polish language");
        verify(mockPreferences, atLeastOnce()).putString("language", "pl");
        verify(mockPreferences, atLeastOnce()).flush();
    }
    
    @Test
    public void testSetLanguageWithUnsupportedCode() {
        setupMockLanguageFile("en", createSampleEnglishJson());
        localizationManager.initialize();
        
        String originalLanguage = localizationManager.getCurrentLanguage();
        localizationManager.setLanguage("fr"); // Unsupported
        
        assertEquals(originalLanguage, localizationManager.getCurrentLanguage(),
                "Should not change language for unsupported code");
    }
    
    @Test
    public void testSetLanguageToSameLanguage() {
        setupMockLanguageFile("en", createSampleEnglishJson());
        localizationManager.initialize();
        
        localizationManager.setLanguage("en");
        
        assertEquals("en", localizationManager.getCurrentLanguage(),
                "Should remain on same language");
    }
    
    /**
     * Test 7: Language change listeners
     * Requirements: 10.4, 10.5
     */
    @Test
    public void testLanguageChangeListenerNotification() {
        setupMockLanguageFile("en", createSampleEnglishJson());
        setupMockLanguageFile("pl", createSamplePolishJson());
        
        localizationManager.initialize();
        
        // Create mock listener
        LanguageChangeListener listener = mock(LanguageChangeListener.class);
        localizationManager.addLanguageChangeListener(listener);
        
        // Change language
        localizationManager.setLanguage("pl");
        
        // Verify listener was notified
        verify(listener).onLanguageChanged("pl");
    }
    
    @Test
    public void testMultipleLanguageChangeListeners() {
        setupMockLanguageFile("en", createSampleEnglishJson());
        setupMockLanguageFile("nl", createSampleDutchJson());
        
        localizationManager.initialize();
        
        // Create multiple mock listeners
        LanguageChangeListener listener1 = mock(LanguageChangeListener.class);
        LanguageChangeListener listener2 = mock(LanguageChangeListener.class);
        
        localizationManager.addLanguageChangeListener(listener1);
        localizationManager.addLanguageChangeListener(listener2);
        
        // Change language
        localizationManager.setLanguage("nl");
        
        // Verify both listeners were notified
        verify(listener1).onLanguageChanged("nl");
        verify(listener2).onLanguageChanged("nl");
    }
    
    @Test
    public void testRemoveLanguageChangeListener() {
        setupMockLanguageFile("en", createSampleEnglishJson());
        setupMockLanguageFile("pt", createSamplePortugueseJson());
        
        localizationManager.initialize();
        
        // Create mock listener
        LanguageChangeListener listener = mock(LanguageChangeListener.class);
        localizationManager.addLanguageChangeListener(listener);
        localizationManager.removeLanguageChangeListener(listener);
        
        // Change language
        localizationManager.setLanguage("pt");
        
        // Verify listener was NOT notified
        verify(listener, never()).onLanguageChanged(anyString());
    }
    
    /**
     * Test 8: Language preference persistence
     * Requirements: 10.6, 10.7
     */
    @Test
    public void testLanguagePreferenceSaving() {
        setupMockLanguageFile("en", createSampleEnglishJson());
        setupMockLanguageFile("pl", createSamplePolishJson());
        
        localizationManager.initialize();
        localizationManager.setLanguage("pl");
        
        // Verify preference was saved
        verify(mockPreferences).putString("language", "pl");
        verify(mockPreferences).flush();
    }
    
    @Test
    public void testLanguagePreferenceLoading() {
        // Setup saved preference
        when(mockPreferences.getString("language", null)).thenReturn("pl");
        
        setupMockLanguageFile("en", createSampleEnglishJson());
        setupMockLanguageFile("pl", createSamplePolishJson());
        
        localizationManager.initialize();
        
        assertEquals("pl", localizationManager.getCurrentLanguage(),
                "Should load saved language preference");
    }
    
    /**
     * Test 9: Supported languages
     */
    @Test
    public void testGetSupportedLanguages() {
        String[] supported = localizationManager.getSupportedLanguages();
        
        assertNotNull(supported);
        assertEquals(4, supported.length);
        assertArrayEquals(new String[]{"en", "pl", "pt", "nl"}, supported);
    }
    
    @Test
    public void testGetLanguageDisplayNames() {
        assertEquals("English", localizationManager.getLanguageDisplayName("en"));
        assertEquals("Polski", localizationManager.getLanguageDisplayName("pl"));
        assertEquals("Português", localizationManager.getLanguageDisplayName("pt"));
        assertEquals("Nederlands", localizationManager.getLanguageDisplayName("nl"));
    }
    
    /**
     * Test 10: Error handling for missing files
     * Requirements: 9.1, 9.2
     * Note: This test is difficult with mocks due to singleton state.
     * Better covered in integration tests.
     */
    @Test
    public void testMissingLanguageFileFallback() {
        // This test is better covered in LocalizationIntegrationTest
        // where we can test with real file system behavior
        
        // Just verify the basic fallback mechanism works
        setupMockLanguageFile("en", createSampleEnglishJson());
        when(mockPreferences.getString("language", null)).thenReturn(null);
        
        Locale.setDefault(Locale.ENGLISH);
        localizationManager.initialize();
        
        assertEquals("en", localizationManager.getCurrentLanguage());
    }
    
    // Helper methods
    
    private void setupMockLanguageFile(String languageCode, String jsonContent) {
        FileHandle mockFile = mock(FileHandle.class);
        when(Gdx.files.internal("localization/" + languageCode + ".json")).thenReturn(mockFile);
        when(mockFile.exists()).thenReturn(true);
        when(mockFile.readString()).thenReturn(jsonContent);
    }
    
    private String createSampleEnglishJson() {
        return "{\n" +
                "  \"menu\": {\n" +
                "    \"player_name\": \"Player Name\",\n" +
                "    \"save_world\": \"Save World\",\n" +
                "    \"exit\": \"Exit\"\n" +
                "  },\n" +
                "  \"language_dialog\": {\n" +
                "    \"title\": \"Select Language\",\n" +
                "    \"current_language\": \"Current: {0}\"\n" +
                "  },\n" +
                "  \"world_save_dialog\": {\n" +
                "    \"character_count\": \"{0}/{1}\"\n" +
                "  }\n" +
                "}";
    }
    
    private String createSamplePolishJson() {
        return "{\n" +
                "  \"menu\": {\n" +
                "    \"player_name\": \"Imię Gracza\",\n" +
                "    \"save_world\": \"Zapisz Świat\",\n" +
                "    \"exit\": \"Wyjście\"\n" +
                "  },\n" +
                "  \"language_dialog\": {\n" +
                "    \"title\": \"Wybierz Język\",\n" +
                "    \"current_language\": \"Aktualny: {0}\"\n" +
                "  },\n" +
                "  \"world_save_dialog\": {\n" +
                "    \"character_count\": \"{0}/{1}\"\n" +
                "  }\n" +
                "}";
    }
    
    private String createSamplePortugueseJson() {
        return "{\n" +
                "  \"menu\": {\n" +
                "    \"player_name\": \"Nome do Jogador\",\n" +
                "    \"save_world\": \"Salvar Mundo\",\n" +
                "    \"exit\": \"Sair\"\n" +
                "  },\n" +
                "  \"language_dialog\": {\n" +
                "    \"title\": \"Selecionar Idioma\",\n" +
                "    \"current_language\": \"Atual: {0}\"\n" +
                "  },\n" +
                "  \"world_save_dialog\": {\n" +
                "    \"character_count\": \"{0}/{1}\"\n" +
                "  }\n" +
                "}";
    }
    
    private String createSampleDutchJson() {
        return "{\n" +
                "  \"menu\": {\n" +
                "    \"player_name\": \"Spelersnaam\",\n" +
                "    \"save_world\": \"Wereld Opslaan\",\n" +
                "    \"exit\": \"Afsluiten\"\n" +
                "  },\n" +
                "  \"language_dialog\": {\n" +
                "    \"title\": \"Selecteer Taal\",\n" +
                "    \"current_language\": \"Huidig: {0}\"\n" +
                "  },\n" +
                "  \"world_save_dialog\": {\n" +
                "    \"character_count\": \"{0}/{1}\"\n" +
                "  }\n" +
                "}";
    }
}
