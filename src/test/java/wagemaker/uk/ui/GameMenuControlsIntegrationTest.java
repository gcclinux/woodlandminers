package wagemaker.uk.ui;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Controls menu item integration in GameMenu.
 * Validates: Requirements 1.1, 1.6
 * 
 * These tests verify that:
 * 1. The "Controls" menu item translation key exists in localization files
 * 2. The MENU_HEIGHT constant has been increased to 340 to accommodate the new item
 * 3. The menu structure in GameMenu.java includes the Controls item in the correct position
 */
public class GameMenuControlsIntegrationTest {
    
    /**
     * Test that the "menu.controls" translation key exists in the English localization file.
     * Validates: Requirements 1.1
     */
    @Test
    public void controlsMenuItemTranslationKeyExistsInEnglish() throws Exception {
        // Read the English localization file
        String filePath = "assets/localization/en.json";
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }
        
        String jsonContent = content.toString();
        
        // Verify that "menu.controls" key exists
        assertTrue(jsonContent.contains("\"controls\""), 
            "menu.controls translation key should exist in en.json");
        
        // Verify it's in the menu section
        assertTrue(jsonContent.contains("\"menu\""), 
            "menu section should exist in en.json");
    }
    
    /**
     * Test that the MENU_HEIGHT constant has been increased to 340.
     * Validates: Requirements 1.6
     */
    @Test
    public void menuHeightIncreasedTo340() throws Exception {
        // Use reflection to access the private MENU_HEIGHT constant
        Field menuHeightField = GameMenu.class.getDeclaredField("MENU_HEIGHT");
        menuHeightField.setAccessible(true);
        float menuHeight = menuHeightField.getFloat(null);
        
        assertEquals(340.0f, menuHeight, 0.01f, 
            "MENU_HEIGHT should be 340 to accommodate all 9 menu items");
    }
    
    /**
     * Test that the GameMenu source code includes Controls in the singleplayer menu array.
     * Validates: Requirements 1.1
     */
    @Test
    public void singleplayerMenuIncludesControlsInSourceCode() throws Exception {
        // Read the GameMenu.java source file
        String filePath = "src/main/java/wagemaker/uk/ui/GameMenu.java";
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        String sourceCode = content.toString();
        
        // Find the singleplayerMenuItems array definition
        int singleplayerStart = sourceCode.indexOf("singleplayerMenuItems = new String[]");
        assertTrue(singleplayerStart > 0, "singleplayerMenuItems array should exist in GameMenu.java");
        
        // Extract the array definition (up to the closing brace and semicolon)
        int arrayEnd = sourceCode.indexOf("};", singleplayerStart);
        String arrayDefinition = sourceCode.substring(singleplayerStart, arrayEnd + 2);
        
        // Verify that menu.controls is included
        assertTrue(arrayDefinition.contains("menu.controls"), 
            "singleplayerMenuItems should include menu.controls");
        
        // Verify the order: menu.player_location, menu.controls, menu.save_world
        int playerLocationIndex = arrayDefinition.indexOf("menu.player_location");
        int controlsIndex = arrayDefinition.indexOf("menu.controls");
        int saveWorldIndex = arrayDefinition.indexOf("menu.save_world");
        
        assertTrue(playerLocationIndex > 0, "menu.player_location should exist");
        assertTrue(controlsIndex > 0, "menu.controls should exist");
        assertTrue(saveWorldIndex > 0, "menu.save_world should exist");
        
        assertTrue(playerLocationIndex < controlsIndex, 
            "menu.controls should come after menu.player_location");
        assertTrue(controlsIndex < saveWorldIndex, 
            "menu.controls should come before menu.save_world");
    }
    
    /**
     * Test that the GameMenu source code includes Controls in the multiplayer menu array.
     * Validates: Requirements 1.1
     */
    @Test
    public void multiplayerMenuIncludesControlsInSourceCode() throws Exception {
        // Read the GameMenu.java source file
        String filePath = "src/main/java/wagemaker/uk/ui/GameMenu.java";
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        String sourceCode = content.toString();
        
        // Find the multiplayerMenuItems array definition
        int multiplayerStart = sourceCode.indexOf("multiplayerMenuItems = new String[]");
        assertTrue(multiplayerStart > 0, "multiplayerMenuItems array should exist in GameMenu.java");
        
        // Extract the array definition (up to the closing brace and semicolon)
        int arrayEnd = sourceCode.indexOf("};", multiplayerStart);
        String arrayDefinition = sourceCode.substring(multiplayerStart, arrayEnd + 2);
        
        // Verify that menu.controls is included
        assertTrue(arrayDefinition.contains("menu.controls"), 
            "multiplayerMenuItems should include menu.controls");
        
        // Verify the order: menu.player_location, menu.controls, menu.save_world
        int playerLocationIndex = arrayDefinition.indexOf("menu.player_location");
        int controlsIndex = arrayDefinition.indexOf("menu.controls");
        int saveWorldIndex = arrayDefinition.indexOf("menu.save_world");
        
        assertTrue(playerLocationIndex > 0, "menu.player_location should exist");
        assertTrue(controlsIndex > 0, "menu.controls should exist");
        assertTrue(saveWorldIndex > 0, "menu.save_world should exist");
        
        assertTrue(playerLocationIndex < controlsIndex, 
            "menu.controls should come after menu.player_location");
        assertTrue(controlsIndex < saveWorldIndex, 
            "menu.controls should come before menu.save_world");
    }
    
    /**
     * Test that the GameMenu source code includes the openControlsDialog method.
     * Validates: Requirements 1.2
     */
    @Test
    public void gameMenuIncludesOpenControlsDialogMethod() throws Exception {
        // Read the GameMenu.java source file
        String filePath = "src/main/java/wagemaker/uk/ui/GameMenu.java";
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        String sourceCode = content.toString();
        
        // Verify that openControlsDialog method exists
        assertTrue(sourceCode.contains("private void openControlsDialog()"), 
            "GameMenu should have openControlsDialog() method");
        
        // Verify that it calls controlsDialog.show()
        int methodStart = sourceCode.indexOf("private void openControlsDialog()");
        int methodEnd = sourceCode.indexOf("}", methodStart);
        String methodBody = sourceCode.substring(methodStart, methodEnd);
        
        assertTrue(methodBody.contains("controlsDialog.show()"), 
            "openControlsDialog() should call controlsDialog.show()");
    }
}
