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
import wagemaker.uk.client.PlayerConfig;
import wagemaker.uk.localization.LocalizationManager;
import wagemaker.uk.player.Player;

/**
 * PlayerLocationDialog displays the player's current coordinates and allows
 * setting a custom target location for the compass to point toward.
 * Players can enter target X and Y coordinates, then press Enter to set the target,
 * or reset the compass to point to the world origin (0,0).
 */
public class PlayerLocationDialog {
    private boolean isVisible = false;
    private Texture woodenPlank;
    private BitmapFont dialogFont;
    private String targetXInput = "";
    private String targetYInput = "";
    private int activeField = 0; // 0 = targetX, 1 = targetY
    private Player player;
    private Compass compass;
    private PlayerConfig playerConfig;
    
    private static final float DIALOG_WIDTH = 400;
    private static final float DIALOG_HEIGHT = 300;
    private static final int MAX_COORDINATE_LENGTH = 10;
    
    /**
     * Creates a new PlayerLocationDialog with wooden plank background and custom font.
     */
    public PlayerLocationDialog() {
        woodenPlank = createWoodenPlank();
        createDialogFont();
    }
    
    /**
     * Creates the custom font for dialog text using Sancreek-Regular.ttf.
     */
    private void createDialogFont() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/Sancreek-Regular.ttf")
            );
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
            System.out.println("Could not load custom font for player location dialog, using default: " + e.getMessage());
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
     * Shows the player location dialog with the specified player, compass, and config.
     * 
     * @param player The player whose location to display
     * @param compass The compass to update with custom targets
     * @param playerConfig The player configuration for persistence
     */
    public void show(Player player, Compass compass, PlayerConfig playerConfig) {
        this.player = player;
        this.compass = compass;
        this.playerConfig = playerConfig;
        this.isVisible = true;
        this.activeField = 0;
        this.targetXInput = "";
        this.targetYInput = "";
    }
    
    /**
     * Hides the dialog and resets its state.
     */
    public void hide() {
        this.isVisible = false;
        this.player = null;
        this.compass = null;
        this.playerConfig = null;
        this.targetXInput = "";
        this.targetYInput = "";
        this.activeField = 0;
    }
    
    /**
     * Handles keyboard input for coordinate entry.
     * Supports numeric input (digits, decimal point, minus sign), Tab to switch fields,
     * Enter to confirm, ESC to cancel, and Backspace to delete.
     */
    public void handleInput() {
        if (!isVisible) {
            return;
        }
        
        // Handle Tab to switch between fields
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            activeField = (activeField == 0) ? 1 : 0;
            return;
        }
        
        // Handle text input based on which field is active
        for (int i = 0; i < 256; i++) {
            if (Gdx.input.isKeyJustPressed(i)) {
                char character = getCharFromKeyCode(i);
                if (character != 0) {
                    if (activeField == 0 && targetXInput.length() < MAX_COORDINATE_LENGTH) {
                        targetXInput += character;
                    } else if (activeField == 1 && targetYInput.length() < MAX_COORDINATE_LENGTH) {
                        targetYInput += character;
                    }
                }
            }
        }
        
        // Handle backspace
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            if (activeField == 0 && targetXInput.length() > 0) {
                targetXInput = targetXInput.substring(0, targetXInput.length() - 1);
            } else if (activeField == 1 && targetYInput.length() > 0) {
                targetYInput = targetYInput.substring(0, targetYInput.length() - 1);
            }
        }
        
        // Handle enter (confirm)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (validateCoordinates()) {
                applyCustomTarget();
            } else {
                System.out.println("Invalid coordinates");
            }
        }
        
        // Handle escape (cancel)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            hide();
        }
        
        // Handle R key for reset to origin
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            resetToOrigin();
        }
    }
    
    /**
     * Converts a key code to a character suitable for coordinate input.
     * Accepts digits (0-9), decimal point (.), and minus sign (-).
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
        
        // Handle period/dot (.)
        if (keyCode == Input.Keys.PERIOD || keyCode == Input.Keys.NUMPAD_DOT) {
            return '.';
        }
        
        // Handle minus sign (-)
        if (keyCode == Input.Keys.MINUS) {
            return '-';
        }
        
        return 0; // Invalid character
    }
    
    /**
     * Validates that the coordinate inputs are valid numbers.
     * Empty strings are treated as 0.
     * 
     * @return true if both coordinates are valid numbers, false otherwise
     */
    private boolean validateCoordinates() {
        try {
            // Empty strings default to 0
            String xInput = targetXInput.trim();
            String yInput = targetYInput.trim();
            
            // Allow empty inputs (will be treated as 0)
            if (xInput.isEmpty()) {
                xInput = "0";
            }
            if (yInput.isEmpty()) {
                yInput = "0";
            }
            
            // Try to parse as floats
            Float.parseFloat(xInput);
            Float.parseFloat(yInput);
            
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Renders the player location dialog with current coordinates and input fields.
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
        dialogFont.draw(batch, loc.getText("player_location_dialog.title"), dialogX + 20, dialogY + DIALOG_HEIGHT - 30);
        
        // Draw "Current Location" section
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, loc.getText("player_location_dialog.current_location"), dialogX + 20, dialogY + DIALOG_HEIGHT - 70);
        
        // Draw current player coordinates
        if (player != null) {
            dialogFont.setColor(Color.YELLOW);
            String currentX = String.format("x: %.1f", player.getX());
            String currentY = String.format("y: %.1f", player.getY());
            dialogFont.draw(batch, currentX, dialogX + 20, dialogY + DIALOG_HEIGHT - 95);
            dialogFont.draw(batch, currentY, dialogX + 180, dialogY + DIALOG_HEIGHT - 95);
        }
        
        // Draw "New Target Location" section
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, loc.getText("player_location_dialog.new_target"), dialogX + 20, dialogY + DIALOG_HEIGHT - 135);
        
        // Draw target X input field label and value
        dialogFont.setColor(activeField == 0 ? Color.WHITE : Color.LIGHT_GRAY);
        dialogFont.draw(batch, "x:", dialogX + 20, dialogY + DIALOG_HEIGHT - 160);
        
        // Draw target X input field with cursor
        dialogFont.setColor(activeField == 0 ? Color.YELLOW : Color.WHITE);
        String xDisplayText = targetXInput + (activeField == 0 ? "_" : "");
        dialogFont.draw(batch, xDisplayText, dialogX + 50, dialogY + DIALOG_HEIGHT - 160);
        
        // Draw target Y input field label and value
        dialogFont.setColor(activeField == 1 ? Color.WHITE : Color.LIGHT_GRAY);
        dialogFont.draw(batch, "y:", dialogX + 180, dialogY + DIALOG_HEIGHT - 160);
        
        // Draw target Y input field with cursor
        dialogFont.setColor(activeField == 1 ? Color.YELLOW : Color.WHITE);
        String yDisplayText = targetYInput + (activeField == 1 ? "_" : "");
        dialogFont.draw(batch, yDisplayText, dialogX + 210, dialogY + DIALOG_HEIGHT - 160);
        
        // Draw error message if validation fails
        if (!targetXInput.isEmpty() || !targetYInput.isEmpty()) {
            if (!validateCoordinates()) {
                dialogFont.setColor(Color.RED);
                dialogFont.draw(batch, loc.getText("player_location_dialog.invalid_number"), dialogX + 20, dialogY + DIALOG_HEIGHT - 185);
            }
        }
        
        // Draw instructions at bottom
        dialogFont.setColor(Color.LIGHT_GRAY);
        dialogFont.draw(batch, loc.getText("player_location_dialog.tab_instruction"), dialogX + 20, dialogY + 70);
        dialogFont.draw(batch, loc.getText("player_location_dialog.confirm_instruction"), dialogX + 20, dialogY + 50);
        dialogFont.draw(batch, "R to reset to origin", dialogX + 20, dialogY + 30);
        
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
     * Applies the custom target coordinates to the compass.
     * Parses the input fields, sets the compass target, saves to PlayerConfig,
     * and closes the dialog.
     */
    private void applyCustomTarget() {
        try {
            // Parse coordinates (empty strings default to 0)
            String xInput = targetXInput.trim();
            String yInput = targetYInput.trim();
            
            float x = xInput.isEmpty() ? 0.0f : Float.parseFloat(xInput);
            float y = yInput.isEmpty() ? 0.0f : Float.parseFloat(yInput);
            
            // Set the compass target
            if (compass != null) {
                compass.setCustomTarget(x, y);
                System.out.println("Compass target set to (" + x + ", " + y + ")");
            }
            
            // Save to PlayerConfig for persistence
            if (playerConfig != null) {
                playerConfig.saveCompassTarget(x, y);
            }
            
            // Close the dialog
            hide();
            
        } catch (NumberFormatException e) {
            System.err.println("Failed to parse coordinates: " + e.getMessage());
        }
    }
    
    /**
     * Resets the compass to point to the world origin (0, 0).
     * Clears the custom target from the compass, clears it from PlayerConfig,
     * and closes the dialog.
     */
    private void resetToOrigin() {
        // Reset the compass to origin
        if (compass != null) {
            compass.resetToOrigin();
            System.out.println("Compass reset to origin (0, 0)");
        }
        
        // Clear from PlayerConfig
        if (playerConfig != null) {
            playerConfig.clearCompassTarget();
        }
        
        // Close the dialog
        hide();
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
