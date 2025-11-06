package wagemaker.uk.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import wagemaker.uk.player.Player;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GameMenu {
    private boolean isOpen = false;
    private Texture woodenPlank;
    private BitmapFont font;
    private String[] menuItems = {"Save", "Exit"};
    private int selectedIndex = 0;
    private float menuX, menuY;
    private Player player;
    private static final float MENU_WIDTH = 250;
    private static final float MENU_HEIGHT = 160; // Increased height for additional menu item

    public GameMenu() {
        woodenPlank = createWoodenPlank();
        font = new BitmapFont();
        font.getData().setScale(1.5f);
        font.setColor(Color.WHITE);
    }
    
    public void setPlayer(Player player) {
        this.player = player;
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
            
            // Simple JSON parsing (extract x, y, and health values)
            float x = parseJsonFloat(jsonContent, "\"x\":");
            float y = parseJsonFloat(jsonContent, "\"y\":");
            float health = parseJsonFloat(jsonContent, "\"playerHealth\":");
            
            // Set player position and health
            player.setPosition(x, y);
            player.setHealth(health);
            
            System.out.println("Game loaded from: " + saveFile.getAbsolutePath());
            System.out.println("Player position loaded: (" + x + ", " + y + ")");
            System.out.println("Player health loaded: " + health);
            
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

    public void update() {
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
        if (!isOpen) return;

        menuX = camX - viewWidth / 2 + 25;
        menuY = camY + viewHeight / 2 - 25 - MENU_HEIGHT;

        batch.begin();
        batch.draw(woodenPlank, menuX, menuY, MENU_WIDTH, MENU_HEIGHT);
        
        for (int i = 0; i < menuItems.length; i++) {
            if (i == selectedIndex) {
                font.setColor(Color.YELLOW);
            } else {
                font.setColor(Color.WHITE);
            }
            float textX = menuX + 40;
            float textY = menuY + MENU_HEIGHT - 40 - (i * 30);
            font.draw(batch, menuItems[i], textX, textY);
        }
        batch.end();
    }

    private void executeMenuItem(int index) {
        if (index == 0) { // Save
            savePlayerPosition();
        } else if (index == 1) { // Exit
            Gdx.app.exit();
        }
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
            
            // Create JSON content with player position and health
            String jsonContent = String.format(
                "{\n" +
                "  \"playerPosition\": {\n" +
                "    \"x\": %.2f,\n" +
                "    \"y\": %.2f\n" +
                "  },\n" +
                "  \"playerHealth\": %.1f,\n" +
                "  \"savedAt\": \"%s\"\n" +
                "}",
                player.getX(),
                player.getY(),
                player.getHealth(),
                new java.util.Date().toString()
            );
            
            // Write to file
            try (FileWriter writer = new FileWriter(saveFile)) {
                writer.write(jsonContent);
            }
            
            System.out.println("Game saved to: " + saveFile.getAbsolutePath());
            System.out.println("Player position saved: (" + player.getX() + ", " + player.getY() + ")");
            System.out.println("Player health saved: " + player.getHealth());
            
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
        Pixmap pixmap = new Pixmap(250, 160, Pixmap.Format.RGBA8888); // Increased height
        
        pixmap.setColor(0.4f, 0.25f, 0.1f, 1.0f);
        pixmap.fill();
        
        pixmap.setColor(0.3f, 0.18f, 0.08f, 1.0f);
        for (int y = 10; y < 160; y += 15) { // Updated for new height
            pixmap.drawLine(0, y, 250, y + 5);
        }
        
        pixmap.setColor(0.2f, 0.12f, 0.05f, 1.0f);
        pixmap.drawRectangle(0, 0, 250, 160); // Updated for new height
        pixmap.drawRectangle(2, 2, 246, 156); // Updated for new height
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void dispose() {
        woodenPlank.dispose();
        font.dispose();
    }
}
