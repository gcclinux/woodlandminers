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
import wagemaker.uk.localization.LocalizationManager;
import wagemaker.uk.localization.LanguageChangeListener;

/**
 * ConnectDialog provides separate input fields for entering a server IP address and port.
 * Players can type an IP address and port, then press Enter to connect, or ESC to cancel.
 */
public class ConnectDialog implements LanguageChangeListener {
    private boolean isVisible = false;
    private boolean confirmed = false;
    private Texture woodenPlank;
    private BitmapFont dialogFont;
    private String ipAddressBuffer = "";
    private String portBuffer = "25565"; // Default port
    private boolean editingIpAddress = true; // true = editing IP, false = editing port
    private static final float DIALOG_WIDTH = 400;
    private static final float DIALOG_HEIGHT = 250;
    private static final int MAX_IP_LENGTH = 45; // Max length for IPv4 or IPv6
    private static final int MAX_PORT_LENGTH = 5; // Max 5 digits for port
    
    /**
     * Creates a new ConnectDialog with wooden plank background and custom font.
     */
    public ConnectDialog() {
        woodenPlank = createWoodenPlank();
        createDialogFont();
        
        // Register for language change notifications
        LocalizationManager.getInstance().addLanguageChangeListener(this);
    }
    
    /**
     * Creates the custom font for dialog text using slkscr.ttf.
     */
    private void createDialogFont() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Sancreek-Regular.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 16;
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "ąćęłńóśźżĄĆĘŁŃÓŚŹŻãõâêôáéíóúàçÃÕÂÊÔÁÉÍÓÚÀÇäöüßÄÖÜ";
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
        this.editingIpAddress = true; // Start with IP field focused
        // Don't clear buffers here - they may have been pre-filled
        if (this.ipAddressBuffer == null) {
            this.ipAddressBuffer = "";
        }
        if (this.portBuffer == null || this.portBuffer.isEmpty()) {
            this.portBuffer = "25565";
        }
    }
    
    /**
     * Shows the connect dialog with an optional pre-filled address.
     * This is a convenience method that combines show() and setPrefilledAddress().
     * 
     * @param address The server address to pre-fill (format: "ip:port" or just "ip"), or null/empty for defaults
     */
    public void show(String address) {
        this.isVisible = true;
        this.confirmed = false;
        this.editingIpAddress = true; // Start with IP field focused
        
        if (address == null || address.isEmpty()) {
            this.ipAddressBuffer = "";
            this.portBuffer = "25565";
            System.out.println("ConnectDialog: No saved address, using defaults");
        } else {
            // Parse address:port format
            String[] parts = address.split(":");
            this.ipAddressBuffer = parts[0];
            this.portBuffer = parts.length > 1 ? parts[1] : "25565";
            System.out.println("ConnectDialog: Pre-filled with IP=" + this.ipAddressBuffer + ", Port=" + this.portBuffer);
        }
    }
    
    /**
     * Sets a pre-filled address in the input buffers.
     * This allows the dialog to display a previously used server address.
     * Handles null and empty string cases gracefully by treating them as defaults.
     * The pre-filled address can be edited and cleared by the player.
     * 
     * @param address The server address to pre-fill (format: "ip:port" or just "ip"), or null/empty for defaults
     */
    public void setPrefilledAddress(String address) {
        if (address == null || address.isEmpty()) {
            this.ipAddressBuffer = "";
            this.portBuffer = "25565";
        } else {
            // Parse address:port format
            String[] parts = address.split(":");
            this.ipAddressBuffer = parts[0];
            this.portBuffer = parts.length > 1 ? parts[1] : "25565";
        }
    }
    
    /**
     * Handles keyboard input for text entry.
     * Supports alphanumeric characters, dots for IP, numbers for port, and backspace.
     * Tab switches between IP and port fields.
     * Enter confirms the input, ESC cancels.
     */
    public void handleInput() {
        if (!isVisible) {
            return;
        }
        
        // Handle Tab to switch between fields
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            editingIpAddress = !editingIpAddress;
            return;
        }
        
        // Handle text input based on which field is active
        for (int i = 0; i < 256; i++) {
            if (Gdx.input.isKeyJustPressed(i)) {
                char character = getCharFromKeyCode(i, editingIpAddress);
                if (character != 0) {
                    if (editingIpAddress && ipAddressBuffer.length() < MAX_IP_LENGTH) {
                        ipAddressBuffer += character;
                    } else if (!editingIpAddress && portBuffer.length() < MAX_PORT_LENGTH) {
                        portBuffer += character;
                    }
                }
            }
        }
        
        // Handle backspace
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            if (editingIpAddress && ipAddressBuffer.length() > 0) {
                ipAddressBuffer = ipAddressBuffer.substring(0, ipAddressBuffer.length() - 1);
            } else if (!editingIpAddress && portBuffer.length() > 0) {
                portBuffer = portBuffer.substring(0, portBuffer.length() - 1);
            }
        }
        
        // Handle enter (confirm)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (isValidInput()) {
                confirmed = true;
                isVisible = false;
                System.out.println("Connecting to: " + ipAddressBuffer + ":" + portBuffer);
            } else {
                // Validation message is now handled by the caller, not printed here
                System.out.println("Invalid IP address or port");
            }
        }
        
        // Handle escape (cancel)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            confirmed = false;
            isVisible = false;
            ipAddressBuffer = "";
            portBuffer = "25565";
        }
    }
    
    /**
     * Converts a key code to a character suitable for IP address or port input.
     * 
     * @param keyCode The LibGDX key code
     * @param forIpAddress true if for IP address field, false if for port field
     * @return The character, or 0 if invalid
     */
    private char getCharFromKeyCode(int keyCode, boolean forIpAddress) {
        // Handle numbers (0-9) - valid for both IP and port
        if (keyCode >= Input.Keys.NUM_0 && keyCode <= Input.Keys.NUM_9) {
            return (char)('0' + (keyCode - Input.Keys.NUM_0));
        }
        
        // Handle numpad numbers - valid for both IP and port
        if (keyCode >= Input.Keys.NUMPAD_0 && keyCode <= Input.Keys.NUMPAD_9) {
            return (char)('0' + (keyCode - Input.Keys.NUMPAD_0));
        }
        
        // The following are only valid for IP address field
        if (forIpAddress) {
            // Handle letters (for hostnames like "localhost")
            if (keyCode >= Input.Keys.A && keyCode <= Input.Keys.Z) {
                return (char)('a' + (keyCode - Input.Keys.A));
            }
            
            // Handle period/dot (.)
            if (keyCode == Input.Keys.PERIOD || keyCode == Input.Keys.NUMPAD_DOT) {
                return '.';
            }
            
            // Handle minus/hyphen (-) for hostnames
            if (keyCode == Input.Keys.MINUS) {
                return '-';
            }
        }
        
        return 0; // Invalid character
    }
    
    /**
     * Validates the IP address and port input.
     * Accepts IPv4 addresses and hostnames for IP, and valid port numbers (1-65535).
     * 
     * @return true if both IP and port are valid, false otherwise
     */
    private boolean isValidInput() {
        // Validate IP address
        String ip = ipAddressBuffer.trim();
        if (ip.isEmpty()) {
            return false;
        }
        
        // Allow "localhost"
        if (ip.equalsIgnoreCase("localhost")) {
            // Continue to port validation
        } else {
            // Basic IPv4 validation (xxx.xxx.xxx.xxx)
            String[] octets = ip.split("\\.");
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
            } else {
                // Allow hostnames (basic check - contains letters and valid characters)
                if (!ip.matches("[a-zA-Z0-9.-]+")) {
                    return false;
                }
            }
        }
        
        // Validate port
        String port = portBuffer.trim();
        if (port.isEmpty()) {
            return false;
        }
        
        try {
            int portNum = Integer.parseInt(port);
            if (portNum < 1 || portNum > 65535) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Renders the connect dialog with separate IP address and port input fields.
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
        
        LocalizationManager loc = LocalizationManager.getInstance();
        
        batch.begin();
        
        // Center the dialog on screen
        float dialogX = camX - DIALOG_WIDTH / 2;
        float dialogY = camY - DIALOG_HEIGHT / 2;
        
        // Draw wooden plank background
        batch.draw(woodenPlank, dialogX, dialogY, DIALOG_WIDTH, DIALOG_HEIGHT);
        
        // Draw title
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, loc.getText("connect_dialog.title"), dialogX + 20, dialogY + DIALOG_HEIGHT - 30);
        
        // Draw IP Address label
        dialogFont.setColor(editingIpAddress ? Color.WHITE : Color.LIGHT_GRAY);
        dialogFont.draw(batch, loc.getText("connect_dialog.ip_address_label"), dialogX + 20, dialogY + DIALOG_HEIGHT - 70);
        
        // Draw IP Address input field with cursor
        dialogFont.setColor(editingIpAddress ? Color.YELLOW : Color.LIGHT_GRAY);
        String ipDisplayText = ipAddressBuffer + (editingIpAddress ? "_" : "");
        dialogFont.draw(batch, ipDisplayText, dialogX + 20, dialogY + DIALOG_HEIGHT - 95);
        
        // Draw Port label
        dialogFont.setColor(!editingIpAddress ? Color.WHITE : Color.LIGHT_GRAY);
        dialogFont.draw(batch, loc.getText("connect_dialog.port_label"), dialogX + 20, dialogY + DIALOG_HEIGHT - 135);
        
        // Draw Port input field with cursor
        dialogFont.setColor(!editingIpAddress ? Color.YELLOW : Color.LIGHT_GRAY);
        String portDisplayText = portBuffer + (!editingIpAddress ? "_" : "");
        dialogFont.draw(batch, portDisplayText, dialogX + 20, dialogY + DIALOG_HEIGHT - 160);
        
        // Draw instructions
        dialogFont.setColor(Color.LIGHT_GRAY);
        dialogFont.draw(batch, loc.getText("connect_dialog.tab_instruction"), dialogX + 20, dialogY + 60);
        dialogFont.draw(batch, loc.getText("connect_dialog.confirm_instruction"), dialogX + 20, dialogY + 40);
        
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
     * Gets the entered IP address with port in "ip:port" format.
     * 
     * @return The entered address string in format "ip:port"
     */
    public String getEnteredAddress() {
        return ipAddressBuffer.trim() + ":" + portBuffer.trim();
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
        editingIpAddress = true; // Reset to IP field for next time
    }
    
    /**
     * Disposes of resources used by the dialog.
     */
    public void dispose() {
        // Unregister from language change notifications
        LocalizationManager.getInstance().removeLanguageChangeListener(this);
        
        if (woodenPlank != null) {
            woodenPlank.dispose();
        }
        if (dialogFont != null) {
            dialogFont.dispose();
        }
    }
    
    /**
     * Called when the language changes.
     * The dialog will automatically use the new language on next render.
     * 
     * @param newLanguage The new language code
     */
    @Override
    public void onLanguageChanged(String newLanguage) {
        // No need to cache text - it's retrieved fresh on each render
        System.out.println("ConnectDialog: Language changed to " + newLanguage);
    }
}
