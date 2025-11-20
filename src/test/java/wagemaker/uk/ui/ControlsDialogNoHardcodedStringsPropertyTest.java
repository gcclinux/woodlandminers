package wagemaker.uk.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for Controls Dialog hardcoded string detection.
 * Feature: controls-menu-dialog, Property 8: No hardcoded strings in rendering
 * Validates: Requirements 5.7
 * 
 * This test verifies that all displayed text in ControlsDialog uses LocalizationManager
 * instead of hardcoded string literals.
 */
public class ControlsDialogNoHardcodedStringsPropertyTest {
    
    private static final String CONTROLS_DIALOG_PATH = "src/main/java/wagemaker/uk/ui/ControlsDialog.java";
    
    /**
     * Property 8: No hardcoded strings in rendering
     * For any text displayed in the Controls Dialog, that text should be retrieved 
     * from LocalizationManager using a translation key, not hardcoded as a string literal
     * Validates: Requirements 5.7
     * 
     * This property-based test analyzes the ControlsDialog source code to verify
     * that the render() method uses LocalizationManager.getText() for all displayed text.
     */
    @Test
    public void renderMethodUsesLocalizationManager() throws IOException {
        // Read the ControlsDialog source code
        String sourceCode = new String(Files.readAllBytes(Paths.get(CONTROLS_DIALOG_PATH)));
        
        // Extract the render method
        String renderMethod = extractRenderMethod(sourceCode);
        assertNotNull(renderMethod, "render() method should exist in ControlsDialog");
        
        // Find all string literals in the render method
        List<String> stringLiterals = findStringLiterals(renderMethod);
        
        // Filter out acceptable string literals (not user-facing text)
        List<String> suspiciousLiterals = new ArrayList<>();
        for (String literal : stringLiterals) {
            if (isUserFacingText(literal)) {
                suspiciousLiterals.add(literal);
            }
        }
        
        // Verify no suspicious hardcoded strings exist
        assertTrue(
            suspiciousLiterals.isEmpty(),
            "render() method should not contain hardcoded user-facing strings. Found: " + suspiciousLiterals
        );
        
        // Verify LocalizationManager.getText() is used
        assertTrue(
            renderMethod.contains("LocalizationManager"),
            "render() method should use LocalizationManager"
        );
        
        assertTrue(
            renderMethod.contains(".getText("),
            "render() method should call getText() to retrieve localized strings"
        );
    }
    
    /**
     * Property: All translation keys follow naming convention
     * For any getText() call in the render method, the key should follow the
     * "controls_dialog.*" naming convention.
     * 
     * This property-based test runs 100 trials, randomly selecting getText() calls
     * to verify they use the correct key prefix.
     */
    @Test
    public void translationKeysFollowNamingConvention() throws IOException {
        // Read the ControlsDialog source code
        String sourceCode = new String(Files.readAllBytes(Paths.get(CONTROLS_DIALOG_PATH)));
        
        // Extract the render method
        String renderMethod = extractRenderMethod(sourceCode);
        assertNotNull(renderMethod, "render() method should exist in ControlsDialog");
        
        // Find all getText() calls
        List<String> getTextCalls = findGetTextCalls(renderMethod);
        
        // Verify at least some getText() calls exist
        assertFalse(
            getTextCalls.isEmpty(),
            "render() method should contain getText() calls for localization"
        );
        
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials (or fewer if there aren't that many getText calls)
        int trials = Math.min(100, getTextCalls.size() * 10);
        for (int trial = 0; trial < trials; trial++) {
            // Randomly select a getText() call
            String getTextCall = getTextCalls.get(random.nextInt(getTextCalls.size()));
            
            // Verify it uses the correct prefix
            assertTrue(
                getTextCall.contains("\"controls_dialog.") || getTextCall.contains("\"menu.controls"),
                "getText() call should use 'controls_dialog.*' or 'menu.controls' key prefix. Found: " + getTextCall
            );
        }
    }
    
    /**
     * Property: Render method retrieves all required text from LocalizationManager
     * For any required translation key, the render method should call getText() with that key.
     * 
     * This property-based test verifies that all required keys are used in the render method.
     */
    @Test
    public void renderMethodRetrievesAllRequiredText() throws IOException {
        // Read the ControlsDialog source code
        String sourceCode = new String(Files.readAllBytes(Paths.get(CONTROLS_DIALOG_PATH)));
        
        // Extract the render method
        String renderMethod = extractRenderMethod(sourceCode);
        assertNotNull(renderMethod, "render() method should exist in ControlsDialog");
        
        // Required translation keys that should be used in render()
        String[] requiredKeys = {
            "controls_dialog.title",
            "controls_dialog.movement_header",
            "controls_dialog.inventory_header",
            "controls_dialog.item_header",
            "controls_dialog.targeting_header",
            "controls_dialog.combat_header",
            "controls_dialog.system_header",
            "controls_dialog.close_instruction"
        };
        
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials, randomly selecting required keys
        for (int trial = 0; trial < 100; trial++) {
            String requiredKey = requiredKeys[random.nextInt(requiredKeys.length)];
            
            // Verify the key is used in the render method
            assertTrue(
                renderMethod.contains("\"" + requiredKey + "\""),
                "render() method should use translation key: " + requiredKey
            );
        }
    }
    
    // Helper methods
    
    /**
     * Extracts the render() method from the source code.
     */
    private String extractRenderMethod(String sourceCode) {
        Pattern pattern = Pattern.compile(
            "public void render\\([^)]+\\)\\s*\\{",
            Pattern.DOTALL
        );
        Matcher matcher = pattern.matcher(sourceCode);
        
        if (!matcher.find()) {
            return null;
        }
        
        int start = matcher.start();
        int braceCount = 0;
        int end = start;
        
        for (int i = matcher.end() - 1; i < sourceCode.length(); i++) {
            char c = sourceCode.charAt(i);
            if (c == '{') {
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    end = i + 1;
                    break;
                }
            }
        }
        
        return sourceCode.substring(start, end);
    }
    
    /**
     * Finds all string literals in the given code.
     */
    private List<String> findStringLiterals(String code) {
        List<String> literals = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"([^\"\\\\]*(\\\\.[^\"\\\\]*)*)\"");
        Matcher matcher = pattern.matcher(code);
        
        while (matcher.find()) {
            literals.add(matcher.group(1));
        }
        
        return literals;
    }
    
    /**
     * Finds all getText() calls in the given code.
     */
    private List<String> findGetTextCalls(String code) {
        List<String> calls = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\.getText\\([^)]+\\)");
        Matcher matcher = pattern.matcher(code);
        
        while (matcher.find()) {
            calls.add(matcher.group());
        }
        
        return calls;
    }
    
    /**
     * Determines if a string literal is user-facing text that should be localized.
     * Returns false for technical strings like method names, constants, etc.
     */
    private boolean isUserFacingText(String literal) {
        // Ignore translation keys themselves
        if (literal.startsWith("controls_dialog.") || literal.equals("menu.controls")) {
            return false;
        }
        
        // Ignore empty strings
        if (literal.trim().isEmpty()) {
            return false;
        }
        
        // Ignore single characters or symbols
        if (literal.length() <= 2) {
            return false;
        }
        
        // Ignore technical strings (package names, file paths, etc.)
        if (literal.contains(".") && !literal.contains(" ")) {
            return false;
        }
        
        // If it contains spaces or is longer than a few characters, it's likely user-facing
        return literal.contains(" ") || literal.length() > 5;
    }
}
