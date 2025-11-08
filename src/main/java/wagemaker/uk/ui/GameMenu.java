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

public class GameMenu {
    private boolean isOpen = false;
    private Texture woodenPlank;
    private BitmapFont font;
    private BitmapFont playerNameFont; // Custom font for player name
    private String[] menuItems = {"Player Name", "Multiplayer", "Save", "Exit"};
    private int selectedIndex = 0;
    private float menuX, menuY;
    private Player player;
    private wagemaker.uk.gdx.MyGdxGame gameInstance;
    private static final float MENU_WIDTH = 250;
    private static final float MENU_HEIGHT = 220; // Increased height for additional menu item
    
    // Player name dialog
    private boolean nameDialogOpen = false;
    private String playerName = "Player";
    private String inputBuffer = "";
    
    // Multiplayer components
    private MultiplayerMenu multiplayerMenu;
    private ServerHostDialog serverHostDialog;
    private ConnectDialog connectDialog;
    private ErrorDialog errorDialog;


    public GameMenu() {
        woodenPlank = createWoodenPlank();
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
    }
    
    private void createPlayerNameFont() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/slkscr.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 16; // Font size for player name
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
            
            // Simple JSON parsing (extract x, y, health, and name values)
            float x = parseJsonFloat(jsonContent, "\"x\":");
            float y = parseJsonFloat(jsonContent, "\"y\":");
            float health = parseJsonFloat(jsonContent, "\"playerHealth\":");
            String loadedName = parseJsonString(jsonContent, "\"playerName\":");
            
            // Set player position and health
            player.setPosition(x, y);
            player.setHealth(health);
            
            // Set player name if found
            if (loadedName != null && !loadedName.isEmpty()) {
                playerName = loadedName;
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

    public void update() {
        // Handle dialogs first (highest priority)
        if (errorDialog.isVisible()) {
            errorDialog.handleInput();
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
        }

        if (isOpen) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                selectedIndex = (selectedIndex - 1 + menuItems.length) % menuItems.length;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                selectedIndex = (selectedIndex + 1) % menuItems.length;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                executeMenuItem(selectedIndex);
            }
        }
    }

    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, float camX, float camY, float viewWidth, float viewHeight) {
        // Render dialogs (highest priority)
        if (errorDialog.isVisible()) {
            errorDialog.render(batch, shapeRenderer, camX, camY);
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
        
        if (!isOpen && !nameDialogOpen && !multiplayerMenu.isOpen()) return;

        batch.begin();
        
        if (nameDialogOpen) {
            // Render name editor on wooden plank - centered on screen
            float centerX = camX - MENU_WIDTH / 2;
            float centerY = camY - MENU_HEIGHT / 2;

            batch.draw(woodenPlank, centerX, centerY, MENU_WIDTH, MENU_HEIGHT);
            
            // Title
            playerNameFont.setColor(Color.WHITE);
            playerNameFont.draw(batch, "Enter Player Name:", centerX + 20, centerY + MENU_HEIGHT - 30);
            
            // Input text (no solid background) - added more space
            playerNameFont.setColor(Color.YELLOW);
            playerNameFont.draw(batch, inputBuffer + "_", centerX + 20, centerY + MENU_HEIGHT - 80);
            
            // Instructions
            playerNameFont.setColor(Color.LIGHT_GRAY);
            playerNameFont.draw(batch, "Min 3 chars - Enter to confirm", centerX + 20, centerY + 50);
            playerNameFont.draw(batch, "ESC to cancel", centerX + 20, centerY + 30);
            
        } else if (isOpen) {
            // Render main menu
            menuX = camX - viewWidth / 2 + 25;
            menuY = camY + viewHeight / 2 - 25 - MENU_HEIGHT;

            batch.draw(woodenPlank, menuX, menuY, MENU_WIDTH, MENU_HEIGHT);
            
            for (int i = 0; i < menuItems.length; i++) {
                if (i == selectedIndex) {
                    playerNameFont.setColor(Color.YELLOW);
                } else {
                    playerNameFont.setColor(Color.WHITE);
                }
                float textX = menuX + 40;
                float textY = menuY + MENU_HEIGHT - 40 - (i * 30);
                playerNameFont.draw(batch, menuItems[i], textX, textY);
            }
        }
        
        batch.end();
        
        // Render multiplayer menu if open
        if (multiplayerMenu.isOpen()) {
            multiplayerMenu.render(batch, shapeRenderer, camX, camY, viewWidth, viewHeight);
        }
    }

    private void executeMenuItem(int index) {
        if (index == 0) { // Player Name
            openNameDialog();
        } else if (index == 1) { // Multiplayer
            openMultiplayerMenu();
        } else if (index == 2) { // Save
            savePlayerPosition();
        } else if (index == 3) { // Exit
            savePlayerPosition(); // Auto-save before exit
            Gdx.app.exit();
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
     * Handles selection in the multiplayer menu.
     */
    private void handleMultiplayerMenuSelection() {
        String selected = multiplayerMenu.getSelectedOption();
        
        if (selected.equals("Host Server")) {
            multiplayerMenu.close();
            if (gameInstance != null) {
                gameInstance.attemptHostServer();
            } else {
                System.err.println("Cannot host server: game instance not set");
            }
        } else if (selected.equals("Connect to Server")) {
            multiplayerMenu.close();
            
            // Load PlayerConfig and pre-fill the saved server address
            PlayerConfig config = PlayerConfig.load();
            String lastServer = config.getLastServer();
            if (lastServer != null && !lastServer.isEmpty()) {
                connectDialog.setPrefilledAddress(lastServer);
            }
            
            connectDialog.show();
        } else if (selected.equals("Back")) {
            multiplayerMenu.close();
            isOpen = true; // Return to main menu
        }
    }
    
    /**
     * Shows an error dialog with the specified message.
     * @param message The error message to display
     */
    public void showError(String message) {
        errorDialog.show(message);
    }
    
    /**
     * Returns to the multiplayer menu.
     */
    public void returnToMultiplayerMenu() {
        isOpen = false;
        multiplayerMenu.open();
    }
    
    private void savePlayerPosition() {
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
            
            // Create JSON content with player position, health, and name
            String jsonContent = String.format(
                "{\n" +
                "  \"playerPosition\": {\n" +
                "    \"x\": %.2f,\n" +
                "    \"y\": %.2f\n" +
                "  },\n" +
                "  \"playerHealth\": %.1f,\n" +
                "  \"playerName\": \"%s\",\n" +
                "  \"savedAt\": \"%s\"\n" +
                "}",
                player.getX(),
                player.getY(),
                player.getHealth(),
                playerName,
                new java.util.Date().toString()
            );
            
            // Write to file
            try (FileWriter writer = new FileWriter(saveFile)) {
                writer.write(jsonContent);
            }
            
            System.out.println("Game saved to: " + saveFile.getAbsolutePath());
            System.out.println("Player position saved: (" + player.getX() + ", " + player.getY() + ")");
            System.out.println("Player health saved: " + player.getHealth());
            System.out.println("Player name saved: " + playerName);
            
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
        Pixmap pixmap = new Pixmap(250, 220, Pixmap.Format.RGBA8888); // Increased height for multiplayer option
        
        pixmap.setColor(0.4f, 0.25f, 0.1f, 1.0f);
        pixmap.fill();
        
        pixmap.setColor(0.3f, 0.18f, 0.08f, 1.0f);
        for (int y = 10; y < 220; y += 15) { // Updated for new height
            pixmap.drawLine(0, y, 250, y + 5);
        }
        
        pixmap.setColor(0.2f, 0.12f, 0.05f, 1.0f);
        pixmap.drawRectangle(0, 0, 250, 220); // Updated for new height
        pixmap.drawRectangle(2, 2, 246, 216); // Updated for new height
        
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
               serverHostDialog.isVisible();
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

    public void dispose() {
        woodenPlank.dispose();
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
    }
}
