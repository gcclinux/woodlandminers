package wagemaker.uk.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * ConnectDialog provides an input field for entering a server IP address.
 * Players can type an IP address and press Enter to connect, or ESC to cancel.
 */
public class ConnectDialog {
    private boolean isVisible = false;
    private boolean confirmed = false;
    private Texture woodenPlank;
    private BitmapFont dialogFont;
    private String inputBuffer = "";
    private static final float DIALOG_WIDTH = 400;
    private static final float DIALOG_HEIGHT = 200;
    private static final int MAX_IP_LENGTH = 45; // Max length for IPv4 or IPv6
    
    /**
     * Creates a new ConnectDialog with wooden plank background and custom font.
     */
    public ConnectDialog() {
        woodenPlank = createWoodenPlank();
        createDialogFont();
    }
    
    /**
     * Creates the custom font for dialog text using slkscr.ttf.
     */
    private void createDialogFont() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/slkscr.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 16;
            parameter.color = Color.WHITE;
            parameter.borderWidth = 1;
            parameter.borderColor = Color.BLACK;
            parameter.shadowOffsetX = 1;
            parameter.shadowOffsetY = 1;
            parameter.shadowColor = Color.BLACK;
            
            dialogFont = generator.generateFont(parameter);
            generator.dispose();
        } catch (Exception e) {
            System.out.println("Could not load custom font for connect dialog, using default: " + e.getMessage());
            // Fallback to default font
            dialogFont = new BitmapFont();
            dialogFont.getData().setScale(1.3f);
            dialogFont.setColor(Color.WHITE);
        }
    }
    
    /**
     * Creates a wooden plank texture for the dialog background.
     * 
     * @return The wooden plank texture
     */
    private Texture createWoodenPlank() {
        Pixmap pixmap = new Pixmap((int)DIALOG_WIDTH, (int)DIALOG_HEIGHT, Pixmap.Format.RGBA8888);
        
        // Base wood color
        pixmap.setColor(0.4f, 0.25f, 0.1f, 1.0f);
        pixmap.fill();
        
        // Wood grain lines
        pixmap.setColor(0.3f, 0.18f, 0.08f, 1.0f);
        for (int y = 10; y < DIALOG_HEIGHT; y += 15) {
            pixmap.drawLine(0, y, (int)DIALOG_WIDTH, y + 5);
        }
        
        // Border
        pixmap.setColor(0.2f, 0.12f, 0.05f, 1.0f);
        pixmap.drawRectangle(0, 0, (int)DIALOG_WIDTH, (int)DIALOG_HEIGHT);
        pixmap.drawRectangle(2, 2, (int)DIALOG_WIDTH - 4, (int)DIALOG_HEIGHT - 4);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
    
    /**
     * Shows the connect dialog and resets the input state.
     */
    public void show() {
        this.isVisible = true;
        this.confirmed = false;
        this.inputBuffer = "";
    }
    
    /**
     * Sets a pre-filled address in the input buffer.
     * This allows the dialog to display a previously used server address.
     * Handles null and empty string cases gracefully by treating them as empty input.
     * The pre-filled address can be edited and cleared by the player.
     * 
     * @param address The server address to pre-fill, or null/empty for no pre-fill
     */
    public void setPrefilledAddress(String address) {
        if (address == null || address.isEmpty()) {
            this.inputBuffer = "";
        } else {
            this.inputBuffer = address;
        }
    }
    
    /**
     * Handles keyboard input for text entry.
     * Supports alphanumeric characters, dots, colons, and backspace.
     * Enter confirms the input, ESC cancels.
     */
    public void handleInput() {
        if (!isVisible) {
            return;
        }
        
        // Handle text input for IP address
        for (int i = 0; i < 256; i++) {
            if (Gdx.input.isKeyJustPressed(i)) {
                char character = getCharFromKeyCode(i);
                if (character != 0 && inputBuffer.length() < MAX_IP_LENGTH) {
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
            if (isValidIpFormat(inputBuffer)) {
                confirmed = true;
                isVisible = false;
                System.out.println("Connecting to: " + inputBuffer);
            } else {
                System.out.println("Invalid IP address format");
            }
        }
        
        // Handle escape (cancel)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            confirmed = false;
            isVisible = false;
            inputBuffer = "";
        }
    }
    
    /**
     * Converts a key code to a character suitable for IP address input.
     * Supports numbers, dots, colons, and letters (for hostnames).
     * 
     * @param keyCode The LibGDX key code
     * @return The character, or 0 if invalid
     */
    private char getCharFromKeyCode(int keyCode) {
        // Handle numbers (0-9)
        if (keyCode >= Input.Keys.NUM_0 && keyCode <= Input.Keys.NUM_9) {
            return (char)('0' + (keyCode - Input.Keys.NUM_0));
        }
        
        // Handle numpad numbers
        if (keyCode >= Input.Keys.NUMPAD_0 && keyCode <= Input.Keys.NUMPAD_9) {
            return (char)('0' + (keyCode - Input.Keys.NUMPAD_0));
        }
        
        // Handle letters (for hostnames like "localhost")
        if (keyCode >= Input.Keys.A && keyCode <= Input.Keys.Z) {
            return (char)('a' + (keyCode - Input.Keys.A));
        }
        
        // Handle period/dot (.)
        if (keyCode == Input.Keys.PERIOD || keyCode == Input.Keys.NUMPAD_DOT) {
            return '.';
        }
        
        // Handle colon (:) for port specification
        if (keyCode == Input.Keys.COLON) {
            return ':';
        }
        
        // Handle minus/hyphen (-) for hostnames
        if (keyCode == Input.Keys.MINUS) {
            return '-';
        }
        
        return 0; // Invalid character
    }
    
    /**
     * Validates the IP address format.
     * Accepts IPv4 addresses, hostnames, and addresses with port numbers.
     * 
     * @param input The input string to validate
     * @return true if the format is valid, false otherwise
     */
    private boolean isValidIpFormat(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        
        input = input.trim();
        
        // Allow "localhost"
        if (input.equalsIgnoreCase("localhost")) {
            return true;
        }
        
        // Split by colon to handle port
        String[] parts = input.split(":");
        String ipPart = parts[0];
        
        // Validate port if present
        if (parts.length > 1) {
            try {
                int port = Integer.parseInt(parts[1]);
                if (port < 1 || port > 65535) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        
        // Basic IPv4 validation (xxx.xxx.xxx.xxx)
        String[] octets = ipPart.split("\\.");
        if (octets.length == 4) {
            for (String octet : octets) {
                try {
                    int value = Integer.parseInt(octet);
                    if (value < 0 || value > 255) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            return true;
        }
        
        // Allow hostnames (basic check - contains letters)
        if (ipPart.matches("[a-zA-Z0-9.-]+")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Renders the connect dialog with the input field.
     * 
     * @param batch The sprite batch for rendering
     * @param shapeRenderer The shape renderer (unused but kept for consistency)
     * @param camX Camera X position
     * @param camY Camera Y position
     */
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, float camX, float camY) {
        if (!isVisible) {
            return;
        }
        
        batch.begin();
        
        // Center the dialog on screen
        float dialogX = camX - DIALOG_WIDTH / 2;
        float dialogY = camY - DIALOG_HEIGHT / 2;
        
        // Draw wooden plank background
        batch.draw(woodenPlank, dialogX, dialogY, DIALOG_WIDTH, DIALOG_HEIGHT);
        
        // Draw title
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, "Connect to Server", dialogX + 20, dialogY + DIALOG_HEIGHT - 30);
        
        // Draw input label
        dialogFont.setColor(Color.LIGHT_GRAY);
        dialogFont.draw(batch, "Enter IP Address:", dialogX + 20, dialogY + DIALOG_HEIGHT - 70);
        
        // Draw input field with cursor
        dialogFont.setColor(Color.YELLOW);
        String displayText = inputBuffer + "_";
        dialogFont.draw(batch, displayText, dialogX + 20, dialogY + DIALOG_HEIGHT - 100);
        
        // Draw instructions
        dialogFont.setColor(Color.LIGHT_GRAY);
        dialogFont.draw(batch, "Enter to connect, ESC to cancel", dialogX + 20, dialogY + 40);
        
        batch.end();
    }
    
    /**
     * Checks if the dialog is currently visible.
     * 
     * @return true if the dialog is visible, false otherwise
     */
    public boolean isVisible() {
        return isVisible;
    }
    
    /**
     * Checks if the user confirmed the input by pressing Enter.
     * 
     * @return true if confirmed, false otherwise
     */
    public boolean isConfirmed() {
        return confirmed;
    }
    
    /**
     * Gets the entered IP address.
     * 
     * @return The entered IP address string
     */
    public String getEnteredAddress() {
        return inputBuffer.trim();
    }
    
    /**
     * Resets the confirmation state.
     * Should be called after handling a confirmed connection attempt.
     */
    public void resetConfirmation() {
        confirmed = false;
    }
    
    /**
     * Hides the dialog.
     */
    public void hide() {
        isVisible = false;
        confirmed = false;
    }
    
    /**
     * Disposes of resources used by the dialog.
     */
    public void dispose() {
        if (woodenPlank != null) {
            woodenPlank.dispose();
        }
        if (dialogFont != null) {
            dialogFont.dispose();
        }
    }
}
