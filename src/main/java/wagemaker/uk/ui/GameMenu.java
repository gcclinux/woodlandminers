package wagemaker.uk.ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import wagemaker.uk.client.PlayerConfig;
import wagemaker.uk.player.Player;
import wagemaker.uk.world.WorldSaveManager;
import wagemaker.uk.world.WorldSaveData;
import wagemaker.uk.localization.LocalizationManager;
import wagemaker.uk.localization.LanguageChangeListener;

public class GameMenu implements LanguageChangeListener {
    private boolean isOpen = false;
    private Texture woodenPlank;
    private BitmapFont font;
    private BitmapFont playerNameFont; // Custom font for player name
    private String[] singleplayerMenuItems;
    private String[] multiplayerMenuItems;
    private int selectedIndex = 0;
    private float menuX, menuY;
    private Player player;
    private wagemaker.uk.gdx.MyGdxGame gameInstance;
    private wagemaker.uk.inventory.InventoryManager inventoryManager;
    private static final float MENU_WIDTH = 400; // Increased by 60% (250 * 1.6 = 400)
    private static final float MENU_HEIGHT = 310; // Increased to fit all 8 menu items comfortably
    private static final float NAME_DIALOG_WIDTH = 384; // Increased by 20% (320 * 1.2 = 384)
    private static final float NAME_DIALOG_HEIGHT = 220;
    
    // Player name dialog
    private boolean nameDialogOpen = false;
    private String playerName = "Player";
    private String inputBuffer = "";
    private Texture nameDialogPlank; // Separate texture for name dialog
    
    // Multiplayer components
    private MultiplayerMenu multiplayerMenu;
    private ServerHostDialog serverHostDialog;
    private ConnectDialog connectDialog;
    private ErrorDialog errorDialog;
    
    // World save/load components
    private WorldSaveDialog worldSaveDialog;
    private WorldLoadDialog worldLoadDialog;
    private WorldManageDialog worldManageDialog;
    private WorldSaveManager worldSaveManager;
    
    // Language dialog
    private LanguageDialog languageDialog;
    
    // Player location dialog
    private PlayerLocationDialog playerLocationDialog;
    
    // Compass reference for custom target
    private Compass compass;


    public GameMenu() {
        woodenPlank = createWoodenPlank();
        nameDialogPlank = createNameDialogPlank();
        font = new BitmapFont();
        font.getData().setScale(1.5f);
        font.setColor(Color.WHITE);
        
        // Create custom font for player name
        createPlayerNameFont();
        
        // Initialize multiplayer components
        multiplayerMenu = new MultiplayerMenu();
        serverHostDialog = new ServerHostDialog();
        connectDialog = new ConnectDialog();
        errorDialog = new ErrorDialog();
        
        // Initialize world save/load components
        worldSaveManager = new WorldSaveManager();
        worldSaveDialog = new WorldSaveDialog();
        worldLoadDialog = new WorldLoadDialog();
        worldManageDialog = new WorldManageDialog();
        
        // Initialize language dialog
        languageDialog = new LanguageDialog();
        
        // Initialize player location dialog
        playerLocationDialog = new PlayerLocationDialog();
        
        // Register as language change listener
        LocalizationManager.getInstance().addLanguageChangeListener(this);
        
        // Initialize menu items with localized text
        updateMenuItems();
    }
    
    private void createPlayerNameFont() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Sancreek-Regular.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 16; // Font size for player name
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "ąćęłńóśźżĄĆĘŁŃÓŚŹŻãõâêôáéíóúàçÃÕÂÊÔÁÉÍÓÚÀÇäöüßÄÖÜ";
            parameter.color = Color.WHITE;
            parameter.borderWidth = 1;
            parameter.borderColor = Color.BLACK;
            parameter.shadowOffsetX = 1;
            parameter.shadowOffsetY = 1;
            parameter.shadowColor = Color.BLACK;
            
            playerNameFont = generator.generateFont(parameter);
            generator.dispose();
        } catch (Exception e) {
            System.out.println("Could not load custom font, using default: " + e.getMessage());
            // Fallback to default font if custom font fails
            playerNameFont = new BitmapFont();
            playerNameFont.getData().setScale(1.2f);
            playerNameFont.setColor(Color.WHITE);
        }
    }
    
    public void setPlayer(Player player) {
        this.player = player;
    }
    
    public void setGameInstance(wagemaker.uk.gdx.MyGdxGame gameInstance) {
        this.gameInstance = gameInstance;
    }
    
    public void setInventoryManager(wagemaker.uk.inventory.InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }
    
    public void setCompass(Compass compass) {
        this.compass = compass;
    }
    
    /**
     * Updates menu items with localized text.
     * Called on initialization and when language changes.
     */
    private void updateMenuItems() {
        LocalizationManager loc = LocalizationManager.getInstance();
        
        singleplayerMenuItems = new String[] {
            loc.getText("menu.player_name"),
            loc.getText("menu.player_location"),
            loc.getText("menu.save_world"),
            loc.getText("menu.load_world"),
            loc.getText("menu.multiplayer"),
            loc.getText("menu.save_player"),
            loc.getText("menu.language"),
            loc.getText("menu.exit")
        };
        
        multiplayerMenuItems = new String[] {
            loc.getText("menu.player_name"),
            loc.getText("menu.player_location"),
            loc.getText("menu.save_world"),
            loc.getText("menu.load_world"),
            loc.getText("menu.save_player"),
            loc.getText("menu.disconnect"),
            loc.getText("menu.language"),
            loc.getText("menu.exit")
        };
    }
    
    /**
     * Called when the application language changes.
     * Refreshes menu items to display in the new language.
     * 
     * @param newLanguage The new language code
     */
    @Override
    public void onLanguageChanged(String newLanguage) {
        System.out.println("GameMenu: Language changed to " + newLanguage);
        updateMenuItems();
    }
    
    private void openNameDialog() {
        nameDialogOpen = true;
        inputBuffer = playerName; // Start with current name
    }
    
    private void handleNameDialogInput() {
        // Handle text input
        for (int i = 0; i < 256; i++) {
            if (Gdx.input.isKeyJustPressed(i)) {
                char character = getCharFromKeyCode(i);
                if (character != 0 && inputBuffer.length() < 15) { // Max 15 characters
                    inputBuffer += character;
                }
            }
        }
        
        // Handle backspace
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) && inputBuffer.length() > 0) {
            inputBuffer = inputBuffer.substring(0, inputBuffer.length() - 1);
        }
        
        // Handle enter (confirm)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (inputBuffer.length() >= 3) {
                playerName = inputBuffer;
                nameDialogOpen = false;
                System.out.println("Player name set to: " + playerName);
            } else {
                // Validation message is displayed in the dialog, just log it
                System.out.println("Name must be at least 3 characters long");
            }
        }
        
        // Handle escape (cancel)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            nameDialogOpen = false;
            inputBuffer = playerName; // Reset to original name
        }
    }
    
    private char getCharFromKeyCode(int keyCode) {
        // Handle letters
        if (keyCode >= Input.Keys.A && keyCode <= Input.Keys.Z) {
            char letter = (char)('a' + (keyCode - Input.Keys.A));
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                return Character.toUpperCase(letter);
            }
            return letter;
        }
        
        // Handle numbers
        if (keyCode >= Input.Keys.NUM_0 && keyCode <= Input.Keys.NUM_9) {
            return (char)('0' + (keyCode - Input.Keys.NUM_0));
        }
        
        // Handle space
        if (keyCode == Input.Keys.SPACE) {
            return ' ';
        }
        
        return 0; // Invalid character
    }
    
    public boolean loadPlayerPosition() {
        if (player == null) {
            System.out.println("Cannot load: Player reference not set");
            return false;
        }
        
        try {
            File configDir = getConfigDirectory();
            File saveFile = new File(configDir, "woodlanders.json");
            
            if (!saveFile.exists()) {
                System.out.println("No save file found, starting at default position (0, 0)");
                return false;
            }
            
            // Read the JSON file
            String jsonContent = new String(Files.readAllBytes(Paths.get(saveFile.getAbsolutePath())));
            
            // Determine which position to load based on current game mode
            boolean isMultiplayer = (gameInstance != null && 
                                    gameInstance.getGameMode() != wagemaker.uk.gdx.MyGdxGame.GameMode.SINGLEPLAYER);
            
            float x, y, health;
            
            if (isMultiplayer) {
                // Load multiplayer position from object format
                try {
                    x = parseJsonObjectFloat(jsonContent, "\"multiplayerPosition\"", "x");
                    y = parseJsonObjectFloat(jsonContent, "\"multiplayerPosition\"", "y");
                    health = parseJsonFloat(jsonContent, "\"multiplayerHealth\":");
                    System.out.println("Loading multiplayer position from object format");
                } catch (Exception e) {

                    // Fallback to flat format for backwards compatibility
                    try {
                        x = parseJsonFloat(jsonContent, "\"multiplayerX\":");
                        y = parseJsonFloat(jsonContent, "\"multiplayerY\":");
                        health = parseJsonFloat(jsonContent, "\"multiplayerHealth\":");
                        System.out.println("Loading multiplayer position from legacy flat format");
                    } catch (Exception e2) {
                        // Fallback to spawn if multiplayer position doesn't exist
                        System.out.println("No multiplayer position found, using spawn (0, 0)");
                        x = 0;
                        y = 0;
                        health = 100.0f;
                    }
                }
            } else {
                // Load singleplayer position from object format
                try {
                    x = parseJsonObjectFloat(jsonContent, "\"singleplayerPosition\"", "x");
                    y = parseJsonObjectFloat(jsonContent, "\"singleplayerPosition\"", "y");
                    health = parseJsonFloat(jsonContent, "\"singleplayerHealth\":");
                    System.out.println("Loading singleplayer position from object format");
                } catch (Exception e) {
                    // Fallback to flat format for backwards compatibility
                    try {
                        x = parseJsonFloat(jsonContent, "\"singleplayerX\":");
                        y = parseJsonFloat(jsonContent, "\"singleplayerY\":");
                        health = parseJsonFloat(jsonContent, "\"singleplayerHealth\":");
                        System.out.println("Loading singleplayer position from legacy flat format");
                    } catch (Exception e2) {
                        // Fallback to old format for backwards compatibility
                        try {
                            x = parseJsonFloat(jsonContent, "\"x\":");
                            y = parseJsonFloat(jsonContent, "\"y\":");
                            health = parseJsonFloat(jsonContent, "\"playerHealth\":");
                            System.out.println("Loading position from legacy format");
                        } catch (Exception e3) {
                            System.out.println("No singleplayer position found, using default (0, 0)");
                            x = 0;
                            y = 0;
                            health = 100.0f;
                        }
                    }
                }
            }
            
            // Load player name (shared between modes)
            String loadedName = parseJsonString(jsonContent, "\"playerName\":");
            
            // Set player position and health
            player.setPosition(x, y);
            player.setHealth(health);
            
            // Set player name if found
            if (loadedName != null && !loadedName.isEmpty()) {
                playerName = loadedName;
            }
            
            // Load inventory data if available
            if (inventoryManager != null) {
                try {
                    // Load singleplayer inventory
                    int spApple = parseJsonObjectInt(jsonContent, "\"singleplayerInventory\"", "apple");
                    int spBanana = parseJsonObjectInt(jsonContent, "\"singleplayerInventory\"", "banana");
                    int spBabyBamboo = parseJsonObjectInt(jsonContent, "\"singleplayerInventory\"", "babyBamboo");
                    int spBambooStack = parseJsonObjectInt(jsonContent, "\"singleplayerInventory\"", "bambooStack");
                    int spWoodStack = parseJsonObjectInt(jsonContent, "\"singleplayerInventory\"", "woodStack");
                    int spPebble = 0;
                    try {
                        spPebble = parseJsonObjectInt(jsonContent, "\"singleplayerInventory\"", "pebble");
                    } catch (Exception e) {
                        // Pebble field doesn't exist in old saves
                    }
                    
                    wagemaker.uk.inventory.Inventory spInv = inventoryManager.getSingleplayerInventory();
                    spInv.setAppleCount(spApple);
                    spInv.setBananaCount(spBanana);
                    spInv.setBabyBambooCount(spBabyBamboo);
                    spInv.setBambooStackCount(spBambooStack);
                    spInv.setWoodStackCount(spWoodStack);
                    spInv.setPebbleCount(spPebble);
                    
                    System.out.println("Singleplayer inventory loaded: Apple=" + spApple + 
                                      ", Banana=" + spBanana + ", BabyBamboo=" + spBabyBamboo + 
                                      ", BambooStack=" + spBambooStack + ", WoodStack=" + spWoodStack +
                                      ", Pebble=" + spPebble);
                } catch (Exception e) {
                    System.out.println("No singleplayer inventory data found, starting with empty inventory");
                }
                
                try {
                    // Load multiplayer inventory
                    int mpApple = parseJsonObjectInt(jsonContent, "\"multiplayerInventory\"", "apple");
                    int mpBanana = parseJsonObjectInt(jsonContent, "\"multiplayerInventory\"", "banana");
                    int mpBabyBamboo = parseJsonObjectInt(jsonContent, "\"multiplayerInventory\"", "babyBamboo");
                    int mpBambooStack = parseJsonObjectInt(jsonContent, "\"multiplayerInventory\"", "bambooStack");
                    int mpWoodStack = parseJsonObjectInt(jsonContent, "\"multiplayerInventory\"", "woodStack");
                    int mpPebble = 0;
                    try {
                        mpPebble = parseJsonObjectInt(jsonContent, "\"multiplayerInventory\"", "pebble");
                    } catch (Exception e) {
                        // Pebble field doesn't exist in old saves
                    }
                    
                    wagemaker.uk.inventory.Inventory mpInv = inventoryManager.getMultiplayerInventory();
                    mpInv.setAppleCount(mpApple);
                    mpInv.setBananaCount(mpBanana);
                    mpInv.setBabyBambooCount(mpBabyBamboo);
                    mpInv.setBambooStackCount(mpBambooStack);
                    mpInv.setWoodStackCount(mpWoodStack);
                    mpInv.setPebbleCount(mpPebble);
                    
                    System.out.println("Multiplayer inventory loaded: Apple=" + mpApple + 
                                      ", Banana=" + mpBanana + ", BabyBamboo=" + mpBabyBamboo + 
                                      ", BambooStack=" + mpBambooStack + ", WoodStack=" + mpWoodStack +
                                      ", Pebble=" + mpPebble);
                } catch (Exception e) {
                    System.out.println("No multiplayer inventory data found, starting with empty inventory");
                }
            }
            
            System.out.println("Game loaded from: " + saveFile.getAbsolutePath());
            System.out.println("Player position loaded: (" + x + ", " + y + ")");
            System.out.println("Player health loaded: " + health);
            System.out.println("Player name loaded: " + playerName);
            
            return true;
            
        } catch (IOException e) {
            System.out.println("Error loading game: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("Error parsing save file: " + e.getMessage());
            return false;
        }
    }
    
    private float parseJsonFloat(String json, String key) {
        int keyIndex = json.indexOf(key);
        if (keyIndex == -1) {
            throw new RuntimeException("Key not found: " + key);
        }
        
        // Find the start of the value (after the colon and any whitespace)
        int valueStart = keyIndex + key.length();
        while (valueStart < json.length() && (json.charAt(valueStart) == ' ' || json.charAt(valueStart) == '\t')) {
            valueStart++;
        }
        
        // Find the end of the value (before comma, newline, or closing brace)
        int valueEnd = valueStart;
        while (valueEnd < json.length()) {
            char c = json.charAt(valueEnd);
            if (c == ',' || c == '\n' || c == '\r' || c == '}' || c == ' ' || c == '\t') {
                break;
            }
            valueEnd++;
        }
        
        String valueStr = json.substring(valueStart, valueEnd).trim();
        
        // Remove any trailing non-numeric characters
        StringBuilder cleanValue = new StringBuilder();
        for (char c : valueStr.toCharArray()) {
            if (Character.isDigit(c) || c == '.' || c == '-') {
                cleanValue.append(c);
            } else {
                break; // Stop at first non-numeric character
            }
        }
        
        return Float.parseFloat(cleanValue.toString());
    }
    
    private String parseJsonString(String json, String key) {
        int keyIndex = json.indexOf(key);
        if (keyIndex == -1) {
            return null; // Key not found
        }
        
        // Find the start of the value (after the colon and any whitespace)
        int valueStart = keyIndex + key.length();
        while (valueStart < json.length() && (json.charAt(valueStart) == ' ' || json.charAt(valueStart) == '\t')) {
            valueStart++;
        }
        
        // Skip opening quote
        if (valueStart < json.length() && json.charAt(valueStart) == '"') {
            valueStart++;
        }
        
        // Find the end of the value (closing quote)
        int valueEnd = valueStart;
        while (valueEnd < json.length() && json.charAt(valueEnd) != '"') {
            valueEnd++;
        }
        
        if (valueEnd > valueStart) {
            return json.substring(valueStart, valueEnd);
        }
        
        return null;
    }
    
    private float parseJsonObjectFloat(String json, String objectKey, String propertyKey) {
        // Find the object
        int objectIndex = json.indexOf(objectKey);
        if (objectIndex == -1) {
            throw new RuntimeException("Object not found: " + objectKey);
        }
        
        // Find the opening brace of the object
        int braceStart = json.indexOf("{", objectIndex);
        if (braceStart == -1) {
            throw new RuntimeException("Object opening brace not found for: " + objectKey);
        }
        
        // Find the closing brace of the object
        int braceEnd = json.indexOf("}", braceStart);
        if (braceEnd == -1) {
            throw new RuntimeException("Object closing brace not found for: " + objectKey);
        }
        
        // Extract the object content
        String objectContent = json.substring(braceStart + 1, braceEnd);
        
        // Parse the property within the object
        String propertyPattern = "\"" + propertyKey + "\":";
        return parseJsonFloat(objectContent, propertyPattern);
    }
    
    private int parseJsonObjectInt(String json, String objectKey, String propertyKey) {
        // Find the object
        int objectIndex = json.indexOf(objectKey);
        if (objectIndex == -1) {
            throw new RuntimeException("Object not found: " + objectKey);
        }
        
        // Find the opening brace of the object
        int braceStart = json.indexOf("{", objectIndex);
        if (braceStart == -1) {
            throw new RuntimeException("Object opening brace not found for: " + objectKey);
        }
        
        // Find the closing brace of the object
        int braceEnd = json.indexOf("}", braceStart);
        if (braceEnd == -1) {
            throw new RuntimeException("Object closing brace not found for: " + objectKey);
        }
        
        // Extract the object content
        String objectContent = json.substring(braceStart + 1, braceEnd);
        
        // Parse the property within the object
        String propertyPattern = "\"" + propertyKey + "\":";
        int keyIndex = objectContent.indexOf(propertyPattern);
        if (keyIndex == -1) {
            throw new RuntimeException("Property not found: " + propertyKey);
        }
        
        // Find the start of the value (after the colon and any whitespace)
        int valueStart = keyIndex + propertyPattern.length();
        while (valueStart < objectContent.length() && 
               (objectContent.charAt(valueStart) == ' ' || objectContent.charAt(valueStart) == '\t')) {
            valueStart++;
        }
        
        // Find the end of the value (before comma, newline, or closing brace)
        int valueEnd = valueStart;
        while (valueEnd < objectContent.length()) {
            char c = objectContent.charAt(valueEnd);
            if (c == ',' || c == '\n' || c == '\r' || c == '}' || c == ' ' || c == '\t') {
                break;
            }
            valueEnd++;
        }
        
        String valueStr = objectContent.substring(valueStart, valueEnd).trim();
        
        // Remove any trailing non-numeric characters
        StringBuilder cleanValue = new StringBuilder();
        for (char c : valueStr.toCharArray()) {
            if (Character.isDigit(c) || c == '-') {
                cleanValue.append(c);
            } else {
                break; // Stop at first non-numeric character
            }
        }
        
        return Integer.parseInt(cleanValue.toString());
    }

    public void update() {
        // Handle dialogs first (highest priority)
        if (errorDialog.isVisible()) {
            errorDialog.handleInput();
            return;
        }
        
        if (worldSaveDialog.isVisible()) {
            worldSaveDialog.handleInput();
            handleWorldSaveDialogResult();
            return;
        }
        
        if (worldLoadDialog.isVisible()) {
            worldLoadDialog.handleInput();
            handleWorldLoadDialogResult();
            return;
        }
        
        if (worldManageDialog.isVisible()) {
            worldManageDialog.handleInput();
            return;
        }
        
        if (connectDialog.isVisible()) {
            connectDialog.handleInput();
            return;
        }
        
        if (serverHostDialog.isVisible()) {
            serverHostDialog.handleInput();
            return;
        }
        
        if (languageDialog.isVisible()) {
            languageDialog.handleInput();
            handleLanguageDialogResult();
            return;
        }
        
        if (playerLocationDialog.isVisible()) {
            playerLocationDialog.handleInput();
            handlePlayerLocationDialogResult();
            return;
        }
        
        if (nameDialogOpen) {
            handleNameDialogInput();
            return;
        }
        
        // Handle multiplayer menu
        if (multiplayerMenu.isOpen()) {
            multiplayerMenu.update();
            // Check if user selected an option
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                handleMultiplayerMenuSelection();
            }
            return;
        }
        
        // Handle main menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            isOpen = !isOpen;
            if (isOpen) {
                ensureValidMenuSelection();
            }
        }

        if (isOpen) {
            String[] currentMenuItems = getCurrentMenuItems();
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                do {
                    selectedIndex = (selectedIndex - 1 + currentMenuItems.length) % currentMenuItems.length;
                } while (isMenuItemDisabled(currentMenuItems[selectedIndex]));
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                do {
                    selectedIndex = (selectedIndex + 1) % currentMenuItems.length;
                } while (isMenuItemDisabled(currentMenuItems[selectedIndex]));
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                // Only execute if the item is not disabled
                if (!isMenuItemDisabled(currentMenuItems[selectedIndex])) {
                    executeMenuItem(selectedIndex);
                }
            }
        }
    }

    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, float camX, float camY, float viewWidth, float viewHeight) {
        // Render dialogs (highest priority)
        if (errorDialog.isVisible()) {
            errorDialog.render(batch, shapeRenderer, camX, camY);
            return;
        }
        
        if (worldSaveDialog.isVisible()) {
            worldSaveDialog.render(batch, shapeRenderer, camX, camY);
            return;
        }
        
        if (worldLoadDialog.isVisible()) {
            worldLoadDialog.render(batch, shapeRenderer, camX, camY);
            return;
        }
        
        if (worldManageDialog.isVisible()) {
            worldManageDialog.render(batch, shapeRenderer, camX, camY);
            return;
        }
        
        if (connectDialog.isVisible()) {
            connectDialog.render(batch, shapeRenderer, camX, camY);
            return;
        }
        
        if (serverHostDialog.isVisible()) {
            serverHostDialog.render(batch, shapeRenderer, camX, camY);
            return;
        }
        
        if (languageDialog.isVisible()) {
            languageDialog.render(batch, shapeRenderer, camX, camY);
            return;
        }
        
        if (playerLocationDialog.isVisible()) {
            playerLocationDialog.render(batch, shapeRenderer, camX, camY);
            return;
        }
        
        if (!isOpen && !nameDialogOpen && !multiplayerMenu.isOpen()) return;

        batch.begin();
        
        if (nameDialogOpen) {
            // Render name editor on wooden plank - centered on screen
            float centerX = camX - NAME_DIALOG_WIDTH / 2;
            float centerY = camY - NAME_DIALOG_HEIGHT / 2;

            batch.draw(nameDialogPlank, centerX, centerY, NAME_DIALOG_WIDTH, NAME_DIALOG_HEIGHT);
            
            // Get localized strings
            LocalizationManager loc = LocalizationManager.getInstance();
            String title = loc.getText("player_name_dialog.title");
            String minCharsText = loc.getText("player_name_dialog.min_characters");
            String instructionsText = loc.getText("player_name_dialog.instructions");
            
            // Title - centered (removed colon)
            playerNameFont.setColor(Color.WHITE);
            com.badlogic.gdx.graphics.g2d.GlyphLayout titleLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
            titleLayout.setText(playerNameFont, title);
            float titleX = centerX + (NAME_DIALOG_WIDTH - titleLayout.width) / 2;
            playerNameFont.draw(batch, title, titleX, centerY + NAME_DIALOG_HEIGHT - 30);
            
            // Input text - centered with cursor
            playerNameFont.setColor(Color.YELLOW);
            String inputText = inputBuffer + "_";
            com.badlogic.gdx.graphics.g2d.GlyphLayout inputLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
            inputLayout.setText(playerNameFont, inputText);
            float inputX = centerX + (NAME_DIALOG_WIDTH - inputLayout.width) / 2;
            playerNameFont.draw(batch, inputText, inputX, centerY + NAME_DIALOG_HEIGHT - 80);
            
            // Min characters warning - centered
            playerNameFont.setColor(Color.LIGHT_GRAY);
            com.badlogic.gdx.graphics.g2d.GlyphLayout minCharsLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
            minCharsLayout.setText(playerNameFont, minCharsText);
            float minCharsX = centerX + (NAME_DIALOG_WIDTH - minCharsLayout.width) / 2;
            playerNameFont.draw(batch, minCharsText, minCharsX, centerY + 70);
            
            // Instructions - centered
            com.badlogic.gdx.graphics.g2d.GlyphLayout instructionsLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
            instructionsLayout.setText(playerNameFont, instructionsText);
            float instructionsX = centerX + (NAME_DIALOG_WIDTH - instructionsLayout.width) / 2;
            playerNameFont.draw(batch, instructionsText, instructionsX, centerY + 40);
            
        } else if (isOpen) {
            // Render main menu
            menuX = camX - viewWidth / 2 + 25;
            menuY = camY + viewHeight / 2 - 25 - MENU_HEIGHT;

            batch.draw(woodenPlank, menuX, menuY, MENU_WIDTH, MENU_HEIGHT);
            
            String[] currentMenuItems = getCurrentMenuItems();
            for (int i = 0; i < currentMenuItems.length; i++) {
                String menuItem = currentMenuItems[i];
                
                // Check if this menu item should be disabled
                boolean isDisabled = isMenuItemDisabled(menuItem);
                
                if (isDisabled) {
                    playerNameFont.setColor(Color.GRAY);
                } else if (i == selectedIndex) {
                    playerNameFont.setColor(Color.YELLOW);
                } else {
                    playerNameFont.setColor(Color.WHITE);
                }
                
                float textX = menuX + 40;
                float textY = menuY + MENU_HEIGHT - 40 - (i * 30);
                
                // Display the menu item (disabled items are shown in gray color)
                playerNameFont.draw(batch, menuItem, textX, textY);
            }
        }
        
        batch.end();
        
        // Render multiplayer menu if open
        if (multiplayerMenu.isOpen()) {
            multiplayerMenu.render(batch, shapeRenderer, camX, camY, viewWidth, viewHeight);
        }
    }

    private void executeMenuItem(int index) {
        String[] currentMenuItems = getCurrentMenuItems();
        String selectedItem = currentMenuItems[index];
        
        LocalizationManager loc = LocalizationManager.getInstance();
        
        if (selectedItem.equals(loc.getText("menu.player_name"))) {
            openNameDialog();
        } else if (selectedItem.equals(loc.getText("menu.player_location"))) {
            openPlayerLocationDialog();
        } else if (selectedItem.equals(loc.getText("menu.save_world"))) {
            openWorldSaveDialog();
        } else if (selectedItem.equals(loc.getText("menu.load_world"))) {
            openWorldLoadDialog();
        } else if (selectedItem.equals(loc.getText("menu.multiplayer"))) {
            openMultiplayerMenu();
        } else if (selectedItem.equals(loc.getText("menu.disconnect"))) {
            disconnectFromMultiplayer();
        } else if (selectedItem.equals(loc.getText("menu.language"))) {
            openLanguageDialog();
        } else if (selectedItem.equals(loc.getText("menu.save_player"))) {
            savePlayerPosition();
        } else if (selectedItem.equals(loc.getText("menu.exit"))) {
            savePlayerPosition(); // Auto-save before exit
            Gdx.app.exit();
        }
    }
    
    /**
     * Opens the language selection dialog.
     */
    private void openLanguageDialog() {
        isOpen = false; // Close main menu
        languageDialog.show();
    }
    
    /**
     * Opens the player location dialog.
     */
    private void openPlayerLocationDialog() {
        isOpen = false; // Close main menu
        PlayerConfig config = PlayerConfig.load();
        playerLocationDialog.show(player, compass, config);
    }
    
    /**
     * Handles the result of the player location dialog.
     */
    private void handlePlayerLocationDialogResult() {
        if (!playerLocationDialog.isVisible()) {
            // Dialog was closed
            isOpen = true; // Return to main menu
        }
    }
    
    /**
     * Gets the current menu items based on game mode.
     * @return The appropriate menu items array
     */
    private String[] getCurrentMenuItems() {
        if (gameInstance != null && gameInstance.getGameMode() != wagemaker.uk.gdx.MyGdxGame.GameMode.SINGLEPLAYER) {
            return multiplayerMenuItems;
        }
        return singleplayerMenuItems;
    }
    
    /**
     * Disconnects from multiplayer and returns to singleplayer mode.
     */
    private void disconnectFromMultiplayer() {
        if (gameInstance != null) {
            gameInstance.disconnectFromMultiplayer();
            isOpen = false; // Close menu after disconnect
        }
    }
    
    /**
     * Opens the multiplayer menu.
     */
    private void openMultiplayerMenu() {
        isOpen = false; // Close main menu
        multiplayerMenu.open();
    }
    
    /**
     * Opens the world save dialog if save functionality is available.
     */
    private void openWorldSaveDialog() {
        if (!isWorldSaveAllowed()) {
            LocalizationManager loc = LocalizationManager.getInstance();
            String errorMessage = getWorldSaveRestrictionMessage();
            String errorTitle = loc.getText("messages.save_restricted");
            showError(errorMessage, errorTitle);
            return;
        }
        
        isOpen = false; // Close main menu
        boolean isMultiplayer = isCurrentlyMultiplayer();
        worldSaveDialog.show(isMultiplayer);
    }
    
    /**
     * Gets the appropriate error message for world save restrictions.
     * @return The error message explaining why world save is not available
     */
    private String getWorldSaveRestrictionMessage() {
        LocalizationManager loc = LocalizationManager.getInstance();
        
        if (gameInstance == null) {
            return loc.getText("messages.world_save_not_available");
        }
        
        wagemaker.uk.gdx.MyGdxGame.GameMode currentMode = gameInstance.getGameMode();
        
        if (currentMode == wagemaker.uk.gdx.MyGdxGame.GameMode.MULTIPLAYER_CLIENT) {
            return loc.getText("messages.world_save_client_restricted");
        }
        
        return loc.getText("messages.world_save_mode_restricted");
    }
    
    /**
     * Checks if a menu item should be disabled based on current game state.
     * @param menuItem The menu item to check
     * @return true if the menu item should be disabled, false otherwise
     */
    private boolean isMenuItemDisabled(String menuItem) {
        LocalizationManager loc = LocalizationManager.getInstance();
        
        if (menuItem.equals(loc.getText("menu.save_world"))) {
            return !isWorldSaveAllowed();
        }
        
        // Other menu items are always enabled
        return false;
    }
    
    /**
     * Ensures that the currently selected menu item is not disabled.
     * If it is disabled, moves to the next available item.
     */
    private void ensureValidMenuSelection() {
        String[] currentMenuItems = getCurrentMenuItems();
        if (currentMenuItems.length == 0) {
            return;
        }
        
        // If current selection is disabled, find the next enabled item
        if (isMenuItemDisabled(currentMenuItems[selectedIndex])) {
            int originalIndex = selectedIndex;
            do {
                selectedIndex = (selectedIndex + 1) % currentMenuItems.length;
            } while (isMenuItemDisabled(currentMenuItems[selectedIndex]) && selectedIndex != originalIndex);
        }
    }
    
    /**
     * Opens the world load dialog.
     */
    private void openWorldLoadDialog() {
        isOpen = false; // Close main menu
        boolean isMultiplayer = isCurrentlyMultiplayer();
        worldLoadDialog.show(isMultiplayer);
    }
    
    /**
     * Checks if the current game mode is multiplayer.
     * @return true if in multiplayer mode, false otherwise
     */
    private boolean isCurrentlyMultiplayer() {
        if (gameInstance == null) {
            return false;
        }
        
        return gameInstance.getGameMode() != wagemaker.uk.gdx.MyGdxGame.GameMode.SINGLEPLAYER;
    }
    
    /**
     * Handles the result of the language dialog.
     */
    private void handleLanguageDialogResult() {
        if (languageDialog.isConfirmed()) {
            languageDialog.hide();
            isOpen = true; // Return to main menu
        } else if (!languageDialog.isVisible()) {
            // Dialog was cancelled
            isOpen = true; // Return to main menu
        }
    }
    
    /**
     * Handles the result of the world save dialog.
     */
    private void handleWorldSaveDialogResult() {
        if (worldSaveDialog.isConfirmed()) {
            String saveName = worldSaveDialog.getSaveName();
            if (saveName != null && !saveName.trim().isEmpty()) {
                performWorldSave(saveName.trim());
            }
            worldSaveDialog.hide();
        } else if (worldSaveDialog.isCancelled()) {
            worldSaveDialog.hide();
            isOpen = true; // Return to main menu
        }
    }
    
    /**
     * Handles the result of the world load dialog.
     */
    private void handleWorldLoadDialogResult() {
        if (worldLoadDialog.isConfirmed()) {
            String saveName = worldLoadDialog.getSelectedSaveName();
            if (saveName != null && !saveName.trim().isEmpty()) {
                performWorldLoad(saveName.trim());
            }
            worldLoadDialog.hide();
        } else if (worldLoadDialog.isCancelled()) {
            worldLoadDialog.hide();
            isOpen = true; // Return to main menu
        }
    }
    
    /**
     * Performs the actual world save operation.
     * @param saveName The name of the save
     */
    private void performWorldSave(String saveName) {
        LocalizationManager loc = LocalizationManager.getInstance();
        
        if (gameInstance == null || player == null) {
            showError(loc.getText("messages.cannot_save_not_initialized"), 
                     loc.getText("error_dialog.save_error"));
            return;
        }
        
        try {
            System.out.println("World save requested for: " + saveName);
            
            // Extract current world state from the game
            wagemaker.uk.network.WorldState currentWorldState = gameInstance.extractCurrentWorldState();
            
            if (currentWorldState == null) {
                showError(loc.getText("messages.failed_extract_state"), 
                         loc.getText("error_dialog.save_error"));
                return;
            }
            
            boolean isMultiplayer = isCurrentlyMultiplayer();
            
            // Get current inventory from inventory manager
            wagemaker.uk.inventory.Inventory currentInventory = null;
            if (inventoryManager != null) {
                currentInventory = inventoryManager.getCurrentInventory();
            }
            
            boolean success = WorldSaveManager.saveWorld(
                saveName, 
                currentWorldState,
                player.getX(), 
                player.getY(), 
                player.getHealth(),
                currentInventory,
                isMultiplayer
            );
            
            if (success) {
                // World saved successfully - no confirmation dialog needed, just continue
                System.out.println("World '" + saveName + "' saved successfully");
            } else {
                showError(loc.getText("messages.save_failed"), 
                         loc.getText("error_dialog.save_error"));
                System.err.println("World save failed for: " + saveName);
            }
        } catch (Exception e) {
            System.err.println("Error saving world: " + e.getMessage());
            showError(loc.getText("messages.save_error", e.getMessage()), 
                     loc.getText("error_dialog.save_error"));
        }
    }
    
    /**
     * Performs the actual world load operation.
     * @param saveName The name of the save to load
     */
    private void performWorldLoad(String saveName) {
        LocalizationManager loc = LocalizationManager.getInstance();
        
        if (gameInstance == null) {
            showError(loc.getText("messages.cannot_load_not_initialized"), 
                     loc.getText("error_dialog.load_error"));
            return;
        }
        
        try {
            boolean isMultiplayer = isCurrentlyMultiplayer();
            
            // Load the world save data
            WorldSaveData saveData = WorldSaveManager.loadWorld(saveName, isMultiplayer);
            
            if (saveData != null) {
                System.out.println("World save data loaded: " + saveName);
                System.out.println("World seed: " + saveData.getWorldSeed());
                System.out.println("Trees: " + saveData.getTrees().size());
                System.out.println("Items: " + saveData.getItems().size());
                
                // Restore the world state in the game
                boolean success = gameInstance.restoreWorldState(saveData);
                
                if (success) {
                    // Update player position and health from save data
                    if (player != null) {
                        player.setPosition(saveData.getPlayerX(), saveData.getPlayerY());
                        player.setHealth(saveData.getPlayerHealth());
                    }
                    
                    // World loaded successfully - no confirmation dialog needed, just continue
                    System.out.println("World '" + saveName + "' loaded successfully");
                } else {
                    showError(loc.getText("messages.failed_restore_state"), 
                             loc.getText("error_dialog.load_error"));
                    System.err.println("World state restoration failed for: " + saveName);
                }
            } else {
                showError(loc.getText("messages.load_failed"), 
                         loc.getText("error_dialog.load_error"));
            }
        } catch (Exception e) {
            System.err.println("Error loading world: " + e.getMessage());
            showError(loc.getText("messages.load_error", e.getMessage()), 
                     loc.getText("error_dialog.load_error"));
        }
    }
    
    /**
     * Checks if world save functionality is allowed based on current game mode.
     * @return true if world save is allowed, false otherwise
     */
    private boolean isWorldSaveAllowed() {
        if (gameInstance == null) {
            return true; // Default to allowing save if game instance not set
        }
        
        wagemaker.uk.gdx.MyGdxGame.GameMode currentMode = gameInstance.getGameMode();
        
        // Allow save in singleplayer mode
        if (currentMode == wagemaker.uk.gdx.MyGdxGame.GameMode.SINGLEPLAYER) {
            return true;
        }
        
        // Allow save when hosting multiplayer (server mode)
        if (currentMode == wagemaker.uk.gdx.MyGdxGame.GameMode.MULTIPLAYER_HOST) {
            return true;
        }
        
        // Disable save for multiplayer clients
        if (currentMode == wagemaker.uk.gdx.MyGdxGame.GameMode.MULTIPLAYER_CLIENT) {
            return false;
        }
        
        // Default to false for any unknown modes
        return false;
    }
    
    /**
     * Handles selection in the multiplayer menu.
     */
    private void handleMultiplayerMenuSelection() {
        int selectedIndex = multiplayerMenu.getSelectedIndex();
        LocalizationManager loc = LocalizationManager.getInstance();
        
        if (selectedIndex == 0) { // Host Server
            multiplayerMenu.close();
            if (gameInstance != null) {
                gameInstance.attemptHostServer();
            } else {
                System.err.println("Cannot host server: game instance not set");
            }
        } else if (selectedIndex == 1) { // Connect to Server
            multiplayerMenu.close();
            
            // Load PlayerConfig and pre-fill the saved server address
            PlayerConfig config = PlayerConfig.load();
            String lastServer = config.getLastServer();
            
            // Show dialog with pre-filled address (or empty if no last server)
            connectDialog.show(lastServer);
        } else if (selectedIndex == 2) { // Back
            multiplayerMenu.close();
            isOpen = true; // Return to main menu
        }
    }
    
    /**
     * Shows an error dialog with the specified message.
     * @param message The error message to display
     */
    public void showError(String message) {
        LocalizationManager loc = LocalizationManager.getInstance();
        
        // Determine appropriate title based on message content
        String title = loc.getText("error_dialog.title");
        if (message.toLowerCase().contains("connect") || message.toLowerCase().contains("server")) {
            title = loc.getText("error_dialog.connection_error");
        } else if (message.toLowerCase().contains("save") || message.toLowerCase().contains("load")) {
            title = loc.getText("error_dialog.title");
        }
        
        errorDialog.show(message, title);
    }
    
    /**
     * Shows an error dialog with the specified message and custom title.
     * @param message The error message to display
     * @param title The title for the dialog
     */
    public void showError(String message, String title) {
        errorDialog.show(message, title);
    }
    
    /**
     * Shows a success dialog with the specified message and title.
     * @param message The success message to display
     * @param title The title for the dialog
     */
    public void showSuccess(String message, String title) {
        errorDialog.showSuccess(message, title);
    }
    
    /**
     * Returns to the multiplayer menu.
     */
    public void returnToMultiplayerMenu() {
        isOpen = false;
        multiplayerMenu.open();
    }
    
    public void savePlayerPosition() {
        if (player == null) {
            System.out.println("Cannot save: Player reference not set");
            return;
        }
        
        try {
            // Get the appropriate config directory based on OS
            File configDir = getConfigDirectory();
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
            
            File saveFile = new File(configDir, "woodlanders.json");
            
            // Read existing values if file exists
            String lastServer = null;
            Float singleplayerX = null, singleplayerY = null, singleplayerHealth = null;
            Float multiplayerX = null, multiplayerY = null, multiplayerHealth = null;
            
            if (saveFile.exists()) {
                try {
                    String existingContent = new String(Files.readAllBytes(Paths.get(saveFile.getAbsolutePath())));
                    lastServer = parseJsonString(existingContent, "\"lastServer\":");
                    
                    // Try to read existing positions from object format first
                    try {
                        singleplayerX = parseJsonObjectFloat(existingContent, "\"singleplayerPosition\"", "x");
                        singleplayerY = parseJsonObjectFloat(existingContent, "\"singleplayerPosition\"", "y");
                        singleplayerHealth = parseJsonFloat(existingContent, "\"singleplayerHealth\":");
                    } catch (Exception e) {
                        // Fallback to flat format for backwards compatibility
                        try {
                            singleplayerX = parseJsonFloat(existingContent, "\"singleplayerX\":");
                            singleplayerY = parseJsonFloat(existingContent, "\"singleplayerY\":");
                            singleplayerHealth = parseJsonFloat(existingContent, "\"singleplayerHealth\":");
                        } catch (Exception e2) {
                            // Singleplayer position doesn't exist yet
                        }
                    }
                    
                    try {
                        multiplayerX = parseJsonObjectFloat(existingContent, "\"multiplayerPosition\"", "x");
                        multiplayerY = parseJsonObjectFloat(existingContent, "\"multiplayerPosition\"", "y");
                        multiplayerHealth = parseJsonFloat(existingContent, "\"multiplayerHealth\":");
                    } catch (Exception e) {
                        // Fallback to flat format for backwards compatibility
                        try {
                            multiplayerX = parseJsonFloat(existingContent, "\"multiplayerX\":");
                            multiplayerY = parseJsonFloat(existingContent, "\"multiplayerY\":");
                            multiplayerHealth = parseJsonFloat(existingContent, "\"multiplayerHealth\":");
                        } catch (Exception e2) {
                            // Multiplayer position doesn't exist yet
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Could not read existing save data: " + e.getMessage());
                }
            }
            
            // Determine which position to update based on current game mode
            boolean isMultiplayer = (gameInstance != null && 
                                    gameInstance.getGameMode() != wagemaker.uk.gdx.MyGdxGame.GameMode.SINGLEPLAYER);
            
            if (isMultiplayer) {
                // Update multiplayer position
                multiplayerX = player.getX();
                multiplayerY = player.getY();
                multiplayerHealth = player.getHealth();
                System.out.println("Saving multiplayer position");
            } else {
                // Update singleplayer position
                singleplayerX = player.getX();
                singleplayerY = player.getY();
                singleplayerHealth = player.getHealth();
                System.out.println("Saving singleplayer position");
            }
            
            // Create JSON content with separate positions for singleplayer and multiplayer
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\n");
            jsonBuilder.append(String.format("  \"playerName\": \"%s\",\n", playerName));
            
            // Singleplayer position
            if (singleplayerX != null && singleplayerY != null && singleplayerHealth != null) {
                jsonBuilder.append("  \"singleplayerPosition\": {\n");
                jsonBuilder.append(String.format("    \"x\": %.2f,\n", singleplayerX));
                jsonBuilder.append(String.format("    \"y\": %.2f\n", singleplayerY));
                jsonBuilder.append("  },\n");
                jsonBuilder.append(String.format("  \"singleplayerHealth\": %.1f,\n", singleplayerHealth));
            }
            
            // Singleplayer inventory
            if (inventoryManager != null) {
                wagemaker.uk.inventory.Inventory spInv = inventoryManager.getSingleplayerInventory();
                jsonBuilder.append("  \"singleplayerInventory\": {\n");
                jsonBuilder.append(String.format("    \"apple\": %d,\n", spInv.getAppleCount()));
                jsonBuilder.append(String.format("    \"banana\": %d,\n", spInv.getBananaCount()));
                jsonBuilder.append(String.format("    \"babyBamboo\": %d,\n", spInv.getBabyBambooCount()));
                jsonBuilder.append(String.format("    \"bambooStack\": %d,\n", spInv.getBambooStackCount()));
                jsonBuilder.append(String.format("    \"woodStack\": %d,\n", spInv.getWoodStackCount()));
                jsonBuilder.append(String.format("    \"pebble\": %d\n", spInv.getPebbleCount()));
                jsonBuilder.append("  },\n");
            }
            
            // Multiplayer position
            if (multiplayerX != null && multiplayerY != null && multiplayerHealth != null) {
                jsonBuilder.append("  \"multiplayerPosition\": {\n");
                jsonBuilder.append(String.format("    \"x\": %.2f,\n", multiplayerX));
                jsonBuilder.append(String.format("    \"y\": %.2f\n", multiplayerY));
                jsonBuilder.append("  },\n");
                jsonBuilder.append(String.format("  \"multiplayerHealth\": %.1f,\n", multiplayerHealth));
            }
            
            // Multiplayer inventory
            if (inventoryManager != null) {
                wagemaker.uk.inventory.Inventory mpInv = inventoryManager.getMultiplayerInventory();
                jsonBuilder.append("  \"multiplayerInventory\": {\n");
                jsonBuilder.append(String.format("    \"apple\": %d,\n", mpInv.getAppleCount()));
                jsonBuilder.append(String.format("    \"banana\": %d,\n", mpInv.getBananaCount()));
                jsonBuilder.append(String.format("    \"babyBamboo\": %d,\n", mpInv.getBabyBambooCount()));
                jsonBuilder.append(String.format("    \"bambooStack\": %d,\n", mpInv.getBambooStackCount()));
                jsonBuilder.append(String.format("    \"woodStack\": %d,\n", mpInv.getWoodStackCount()));
                jsonBuilder.append(String.format("    \"pebble\": %d\n", mpInv.getPebbleCount()));
                jsonBuilder.append("  },\n");
            }
            
            // Include lastServer if it exists
            if (lastServer != null && !lastServer.isEmpty()) {
                jsonBuilder.append(String.format("  \"lastServer\": \"%s\",\n", lastServer));
            }
            
            jsonBuilder.append(String.format("  \"savedAt\": \"%s\"\n", new java.util.Date().toString()));
            jsonBuilder.append("}");
            
            // Write to file
            try (FileWriter writer = new FileWriter(saveFile)) {
                writer.write(jsonBuilder.toString());
            }
            
            System.out.println("Game saved to: " + saveFile.getAbsolutePath());
            System.out.println("Player position saved: (" + player.getX() + ", " + player.getY() + ")");
            System.out.println("Player health saved: " + player.getHealth());
            System.out.println("Player name saved: " + playerName);
            System.out.println("Mode: " + (isMultiplayer ? "Multiplayer" : "Singleplayer"));
            if (lastServer != null) {
                System.out.println("Last server preserved: " + lastServer);
            }
            
        } catch (IOException e) {
            System.out.println("Error saving game: " + e.getMessage());
        }
    }
    
    private File getConfigDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        
        if (os.contains("win")) {
            // Windows: %APPDATA%/Woodlanders
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                return new File(appData, "Woodlanders");
            } else {
                return new File(userHome, "AppData/Roaming/Woodlanders");
            }
        } else if (os.contains("mac")) {
            // macOS: ~/Library/Application Support/Woodlanders
            return new File(userHome, "Library/Application Support/Woodlanders");
        } else {
            // Linux/Unix: ~/.config/woodlanders
            return new File(userHome, ".config/woodlanders");
        }
    }
    


    private Texture createWoodenPlank() {
        Pixmap pixmap = new Pixmap((int)MENU_WIDTH, (int)MENU_HEIGHT, Pixmap.Format.RGBA8888);
        
        pixmap.setColor(0.4f, 0.25f, 0.1f, 1.0f);
        pixmap.fill();
        
        pixmap.setColor(0.3f, 0.18f, 0.08f, 1.0f);
        for (int y = 10; y < MENU_HEIGHT; y += 15) {
            pixmap.drawLine(0, y, (int)MENU_WIDTH, y + 5);
        }
        
        pixmap.setColor(0.2f, 0.12f, 0.05f, 1.0f);
        pixmap.drawRectangle(0, 0, (int)MENU_WIDTH, (int)MENU_HEIGHT);
        pixmap.drawRectangle(2, 2, (int)MENU_WIDTH - 4, (int)MENU_HEIGHT - 4);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createNameDialogPlank() {
        Pixmap pixmap = new Pixmap((int)NAME_DIALOG_WIDTH, (int)NAME_DIALOG_HEIGHT, Pixmap.Format.RGBA8888);
        
        pixmap.setColor(0.4f, 0.25f, 0.1f, 1.0f);
        pixmap.fill();
        
        pixmap.setColor(0.3f, 0.18f, 0.08f, 1.0f);
        for (int y = 10; y < NAME_DIALOG_HEIGHT; y += 15) {
            pixmap.drawLine(0, y, (int)NAME_DIALOG_WIDTH, y + 5);
        }
        
        pixmap.setColor(0.2f, 0.12f, 0.05f, 1.0f);
        pixmap.drawRectangle(0, 0, (int)NAME_DIALOG_WIDTH, (int)NAME_DIALOG_HEIGHT);
        pixmap.drawRectangle(2, 2, (int)NAME_DIALOG_WIDTH - 4, (int)NAME_DIALOG_HEIGHT - 4);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public void renderPlayerNameTag(SpriteBatch batch) {
        if (player == null) return;
        
        batch.begin();
        
        // Calculate position above player (centered)
        // Player sprite is 100x100 pixels as seen in MyGdxGame.java
        float playerCenterX = player.getX() + 50; // 100/2 = 50
        float playerTopY = player.getY() + 100 + 2; // 100 height + 2 pixels above player
        
        // Calculate text width for centering using custom font
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        layout.setText(playerNameFont, playerName);
        float textWidth = layout.width;
        
        // Center the text above the player
        float textX = playerCenterX - textWidth / 2;
        
        // Render player name using custom font (already has shadow/border built-in)
        playerNameFont.draw(batch, playerName, textX, playerTopY);
        
        batch.end();
    }

    public boolean isOpen() {
        return isOpen;
    }
    
    /**
     * Checks if any menu or dialog is currently open.
     * This includes the main menu, multiplayer menu, name dialog, and all other dialogs.
     * 
     * @return true if any menu or dialog is open, false otherwise
     */
    public boolean isAnyMenuOpen() {
        return isOpen || 
               nameDialogOpen || 
               multiplayerMenu.isOpen() || 
               errorDialog.isVisible() || 
               connectDialog.isVisible() || 
               serverHostDialog.isVisible() ||
               worldSaveDialog.isVisible() ||
               worldLoadDialog.isVisible() ||
               worldManageDialog.isVisible() ||
               languageDialog.isVisible() ||
               playerLocationDialog.isVisible();
    }
    
    /**
     * Gets the player name font for rendering text.
     * @return The player name font
     */
    public BitmapFont getFont() {
        return playerNameFont;
    }

    /**
     * Gets the multiplayer menu instance.
     * @return The multiplayer menu
     */
    public MultiplayerMenu getMultiplayerMenu() {
        return multiplayerMenu;
    }
    
    /**
     * Gets the server host dialog instance.
     * @return The server host dialog
     */
    public ServerHostDialog getServerHostDialog() {
        return serverHostDialog;
    }
    
    /**
     * Gets the connect dialog instance.
     * @return The connect dialog
     */
    public ConnectDialog getConnectDialog() {
        return connectDialog;
    }
    
    /**
     * Gets the error dialog instance.
     * @return The error dialog
     */
    public ErrorDialog getErrorDialog() {
        return errorDialog;
    }
    
    /**
     * Gets the world save dialog instance.
     * @return The world save dialog
     */
    public WorldSaveDialog getWorldSaveDialog() {
        return worldSaveDialog;
    }
    
    /**
     * Gets the world load dialog instance.
     * @return The world load dialog
     */
    public WorldLoadDialog getWorldLoadDialog() {
        return worldLoadDialog;
    }
    
    /**
     * Gets the world manage dialog instance.
     * @return The world manage dialog
     */
    public WorldManageDialog getWorldManageDialog() {
        return worldManageDialog;
    }
    
    /**
     * Gets the world save manager instance.
     * @return The world save manager
     */
    public WorldSaveManager getWorldSaveManager() {
        return worldSaveManager;
    }
    
    /**
     * Gets the language dialog instance.
     * @return The language dialog
     */
    public LanguageDialog getLanguageDialog() {
        return languageDialog;
    }

    public void dispose() {
        woodenPlank.dispose();
        if (nameDialogPlank != null) {
            nameDialogPlank.dispose();
        }
        font.dispose();
        if (playerNameFont != null) {
            playerNameFont.dispose();
        }
        if (multiplayerMenu != null) {
            multiplayerMenu.dispose();
        }
        if (serverHostDialog != null) {
            serverHostDialog.dispose();
        }
        if (connectDialog != null) {
            connectDialog.dispose();
        }
        if (errorDialog != null) {
            errorDialog.dispose();
        }
        if (worldSaveDialog != null) {
            worldSaveDialog.dispose();
        }
        if (worldLoadDialog != null) {
            worldLoadDialog.dispose();
        }
        if (worldManageDialog != null) {
            worldManageDialog.dispose();
        }
        if (languageDialog != null) {
            languageDialog.dispose();
        }
        if (playerLocationDialog != null) {
            playerLocationDialog.dispose();
        }
        
        // Unregister from language change listener
        LocalizationManager.getInstance().removeLanguageChangeListener(this);
    }
}
