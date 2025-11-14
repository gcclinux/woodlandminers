package wagemaker.uk.localization;

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
 * Tests to verify translation completeness across all language files.
 * Ensures all languages have the same keys as the English (reference) file.
 * Requirements: 8.1, 8.2, 8.3, 8.4, 8.5
 */
public class TranslationCompletenessTest {
    
    private static final String[] SUPPORTED_LANGUAGES = {"en", "pl", "pt", "nl"};
    private static final String REFERENCE_LANGUAGE = "en";
    private static HeadlessApplication application;
    
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
    }
    
    @AfterAll
    public static void tearDownGdx() {
        if (application != null) {
            application.exit();
        }
    }
    
    /**
     * Test that all language files exist.
     */
    @Test
    public void testAllLanguageFilesExist() {
        for (String lang : SUPPORTED_LANGUAGES) {
            FileHandle file = Gdx.files.internal("localization/" + lang + ".json");
            assertTrue(file.exists(), 
                    "Language file should exist: " + lang + ".json");
        }
    }
    
    /**
     * Test that all language files have valid JSON syntax.
     */
    @Test
    public void testAllLanguageFilesHaveValidJson() {
        for (String lang : SUPPORTED_LANGUAGES) {
            FileHandle file = Gdx.files.internal("localization/" + lang + ".json");
            
            assertDoesNotThrow(() -> {
                String content = file.readString();
                Json json = new Json();
                json.fromJson(null, content);
            }, "Language file should have valid JSON: " + lang + ".json");
        }
    }
    
    /**
     * Test that all non-English language files have the same keys as English.
     */
    @Test
    public void testTranslationCompleteness() {
        // Load reference (English) keys
        Set<String> referenceKeys = loadTranslationKeys(REFERENCE_LANGUAGE);
        assertFalse(referenceKeys.isEmpty(), "Reference language file should not be empty");
        
        // Check each other language
        for (String lang : SUPPORTED_LANGUAGES) {
            if (lang.equals(REFERENCE_LANGUAGE)) {
                continue;
            }
            
            Set<String> langKeys = loadTranslationKeys(lang);
            
            // Find missing keys
            Set<String> missingKeys = new HashSet<>(referenceKeys);
            missingKeys.removeAll(langKeys);
            
            // Find extra keys
            Set<String> extraKeys = new HashSet<>(langKeys);
            extraKeys.removeAll(referenceKeys);
            
            // Report findings
            if (!missingKeys.isEmpty()) {
                System.out.println("Missing keys in " + lang + ".json:");
                missingKeys.forEach(key -> System.out.println("  - " + key));
            }
            
            if (!extraKeys.isEmpty()) {
                System.out.println("Extra keys in " + lang + ".json:");
                extraKeys.forEach(key -> System.out.println("  - " + key));
            }
            
            // Calculate completeness
            double completeness = 100.0 * langKeys.size() / referenceKeys.size();
            System.out.println(lang + ".json completeness: " + String.format("%.1f%%", completeness) +
                    " (" + langKeys.size() + "/" + referenceKeys.size() + " keys)");
            
            // Assert completeness
            assertTrue(missingKeys.isEmpty(), 
                    lang + ".json is missing keys: " + missingKeys);
            assertTrue(extraKeys.isEmpty(), 
                    lang + ".json has extra keys not in reference: " + extraKeys);
        }
    }
    
    /**
     * Test that all language files have the same structure (nested keys).
     */
    @Test
    public void testTranslationStructureConsistency() {
        // Load reference structure
        Map<String, Set<String>> referenceStructure = loadTranslationStructure(REFERENCE_LANGUAGE);
        
        // Check each other language
        for (String lang : SUPPORTED_LANGUAGES) {
            if (lang.equals(REFERENCE_LANGUAGE)) {
                continue;
            }
            
            Map<String, Set<String>> langStructure = loadTranslationStructure(lang);
            
            // Check that all top-level sections exist
            Set<String> missingSections = new HashSet<>(referenceStructure.keySet());
            missingSections.removeAll(langStructure.keySet());
            
            assertTrue(missingSections.isEmpty(),
                    lang + ".json is missing sections: " + missingSections);
            
            // Check that each section has the same keys
            for (String section : referenceStructure.keySet()) {
                if (!langStructure.containsKey(section)) {
                    continue;
                }
                
                Set<String> refKeys = referenceStructure.get(section);
                Set<String> langKeys = langStructure.get(section);
                
                Set<String> missingKeys = new HashSet<>(refKeys);
                missingKeys.removeAll(langKeys);
                
                assertTrue(missingKeys.isEmpty(),
                        lang + ".json section '" + section + "' is missing keys: " + missingKeys);
            }
        }
    }
    
    /**
     * Test that parameterized strings have the same placeholders across all languages.
     */
    @Test
    public void testParameterizedStringsConsistency() {
        Map<String, Set<String>> referenceParams = loadParameterizedStrings(REFERENCE_LANGUAGE);
        
        for (String lang : SUPPORTED_LANGUAGES) {
            if (lang.equals(REFERENCE_LANGUAGE)) {
                continue;
            }
            
            Map<String, Set<String>> langParams = loadParameterizedStrings(lang);
            
            // Check each parameterized key
            for (String key : referenceParams.keySet()) {
                if (!langParams.containsKey(key)) {
                    fail(lang + ".json is missing parameterized key: " + key);
                }
                
                Set<String> refPlaceholders = referenceParams.get(key);
                Set<String> langPlaceholders = langParams.get(key);
                
                assertEquals(refPlaceholders, langPlaceholders,
                        lang + ".json key '" + key + "' has different placeholders. " +
                        "Expected: " + refPlaceholders + ", Found: " + langPlaceholders);
            }
        }
    }
    
    /**
     * Test that no translation values are empty strings.
     */
    @Test
    public void testNoEmptyTranslations() {
        for (String lang : SUPPORTED_LANGUAGES) {
            Map<String, String> translations = loadAllTranslations(lang);
            
            for (Map.Entry<String, String> entry : translations.entrySet()) {
                assertFalse(entry.getValue().trim().isEmpty(),
                        lang + ".json has empty translation for key: " + entry.getKey());
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
    
    private Map<String, Set<String>> loadTranslationStructure(String languageCode) {
        FileHandle file = Gdx.files.internal("localization/" + languageCode + ".json");
        String jsonContent = file.readString();
        
        Json json = new Json();
        JsonValue root = json.fromJson(null, jsonContent);
        
        Map<String, Set<String>> structure = new HashMap<>();
        
        for (JsonValue section = root.child; section != null; section = section.next) {
            Set<String> sectionKeys = new HashSet<>();
            
            if (section.isObject()) {
                for (JsonValue key = section.child; key != null; key = key.next) {
                    sectionKeys.add(key.name);
                }
            }
            
            structure.put(section.name, sectionKeys);
        }
        
        return structure;
    }
    
    private Map<String, Set<String>> loadParameterizedStrings(String languageCode) {
        Map<String, String> translations = loadAllTranslations(languageCode);
        Map<String, Set<String>> parameterized = new HashMap<>();
        
        for (Map.Entry<String, String> entry : translations.entrySet()) {
            String value = entry.getValue();
            Set<String> placeholders = extractPlaceholders(value);
            
            if (!placeholders.isEmpty()) {
                parameterized.put(entry.getKey(), placeholders);
            }
        }
        
        return parameterized;
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
    
    private Set<String> extractPlaceholders(String text) {
        Set<String> placeholders = new HashSet<>();
        
        // Find all {0}, {1}, etc. patterns
        for (int i = 0; i < 10; i++) {
            String placeholder = "{" + i + "}";
            if (text.contains(placeholder)) {
                placeholders.add(placeholder);
            }
        }
        
        return placeholders;
    }
}
