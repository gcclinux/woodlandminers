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
import wagemaker.uk.world.WorldSaveManager;

/**
 * WorldSaveDialog provides input field for entering a save name.
 * Players can type a save name, confirm overwrite if needed, and see save progress.
 * Validates save names and handles overwrite confirmation for existing saves.
 */
public class WorldSaveDialog {
    private boolean isVisible = false;
    private boolean confirmed = false;
    private boolean cancelled = false;
    private boolean showingOverwriteConfirmation = false;
    private boolean showingSaveProgress = false;
    private Texture woodenPlank;
    private BitmapFont dialogFont;
    private String saveNameBuffer = "";
    private String errorMessage = "";
    private boolean isMultiplayer = false;
    private static final float DIALOG_WIDTH = 450;
    private static final float DIALOG_HEIGHT = 280;
    private static final int MAX_SAVE_NAME_LENGTH = 50;
    
    /**
     * Creates a new WorldSaveDialog with wooden plank background and custom font.
     */
    public WorldSaveDialog() {
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
            System.out.println("Could not load custom font for world save dialog, using default: " + e.getMessage());
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
     * Shows the save dialog and resets the input state.
     * 
     * @param isMultiplayer true if this is a multiplayer save, false for singleplayer
     */
    public void show(boolean isMultiplayer) {
        this.isVisible = true;
        this.confirmed = false;
        this.cancelled = false;
        this.showingOverwriteConfirmation = false;
        this.showingSaveProgress = false;
        this.isMultiplayer = isMultiplayer;
        this.saveNameBuffer = "";
        this.errorMessage = "";
    }
    
    /**
     * Shows the save dialog with a pre-filled save name.
     * 
     * @param isMultiplayer true if this is a multiplayer save, false for singleplayer
     * @param defaultName Default save name to pre-fill
     */
    public void show(boolean isMultiplayer, String defaultName) {
        show(isMultiplayer);
        if (defaultName != null && !defaultName.trim().isEmpty()) {
            this.saveNameBuffer = defaultName.trim();
        }
    }
    
    /**
     * Handles keyboard input for text entry and navigation.
     * Supports alphanumeric characters, spaces, hyphens, underscores, and backspace.
     * Enter confirms the input, ESC cancels.
     */
    public void handleInput() {
        if (!isVisible) {
            return;
        }
        
        if (showingSaveProgress) {
            // During save progress, only allow ESC to cancel
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                cancelled = true;
                isVisible = false;
            }
            return;
        }
        
        if (showingOverwriteConfirmation) {
            handleOverwriteConfirmationInput();
            return;
        }
        
        // Handle text input
        for (int i = 0; i < 256; i++) {
            if (Gdx.input.isKeyJustPressed(i)) {
                char character = getCharFromKeyCode(i);
                if (character != 0 && saveNameBuffer.length() < MAX_SAVE_NAME_LENGTH) {
                    saveNameBuffer += character;
                    errorMessage = ""; // Clear error when typing
                }
            }
        }
        
        // Handle backspace
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            if (saveNameBuffer.length() > 0) {
                saveNameBuffer = saveNameBuffer.substring(0, saveNameBuffer.length() - 1);
                errorMessage = ""; // Clear error when editing
            }
        }
        
        // Handle enter (confirm)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            attemptSave();
        }
        
        // Handle escape (cancel)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            cancelled = true;
            isVisible = false;
        }
    }
    
    /**
     * Handles input during overwrite confirmation.
     */
    private void handleOverwriteConfirmationInput() {
        // Y or Enter to confirm overwrite
        if (Gdx.input.isKeyJustPressed(Input.Keys.Y) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            showingOverwriteConfirmation = false;
            proceedWithSave();
        }
        
        // N or ESC to cancel overwrite
        if (Gdx.input.isKeyJustPressed(Input.Keys.N) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            showingOverwriteConfirmation = false;
            errorMessage = ""; // Clear any error message
        }
    }
    
    /**
     * Attempts to save with the current save name.
     * Validates the name and checks for existing saves.
     */
    private void attemptSave() {
        String trimmedName = saveNameBuffer.trim();
        
        // Validate save name
        if (trimmedName.isEmpty()) {
            errorMessage = "Save name cannot be empty";
            return;
        }
        
        if (!WorldSaveManager.isValidSaveName(trimmedName)) {
            errorMessage = "Invalid save name. Use letters, numbers, spaces, - and _";
            return;
        }
        
        // Check if save already exists
        if (WorldSaveManager.saveExists(trimmedName, isMultiplayer)) {
            showingOverwriteConfirmation = true;
            errorMessage = "";
            return;
        }
        
        proceedWithSave();
    }
    
    /**
     * Proceeds with the save operation.
     */
    private void proceedWithSave() {
        showingSaveProgress = true;
        confirmed = true;
        // The actual save operation will be handled by the caller
    }
    
    /**
     * Converts a key code to a character suitable for save name input.
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
        
        // Handle letters (A-Z)
        if (keyCode >= Input.Keys.A && keyCode <= Input.Keys.Z) {
            return (char)('a' + (keyCode - Input.Keys.A));
        }
        
        // Handle space
        if (keyCode == Input.Keys.SPACE) {
            return ' ';
        }
        
        // Handle minus/hyphen (-)
        if (keyCode == Input.Keys.MINUS) {
            return '-';
        }
        
        // Handle underscore (_) - Shift + minus
        if (keyCode == Input.Keys.MINUS && (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT))) {
            return '_';
        }
        
        return 0; // Invalid character
    }
    
    /**
     * Renders the save dialog with input field and validation messages.
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
        
        if (showingSaveProgress) {
            renderSaveProgress(batch, dialogX, dialogY);
        } else if (showingOverwriteConfirmation) {
            renderOverwriteConfirmation(batch, dialogX, dialogY);
        } else {
            renderSaveNameInput(batch, dialogX, dialogY);
        }
        
        batch.end();
    }
    
    /**
     * Renders the save name input interface.
     */
    private void renderSaveNameInput(SpriteBatch batch, float dialogX, float dialogY) {
        // Draw title
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, "Save World", dialogX + 20, dialogY + DIALOG_HEIGHT - 30);
        
        // Draw game mode indicator
        dialogFont.setColor(Color.LIGHT_GRAY);
        String modeText = isMultiplayer ? "Multiplayer Save" : "Singleplayer Save";
        dialogFont.draw(batch, modeText, dialogX + 20, dialogY + DIALOG_HEIGHT - 55);
        
        // Draw save name label
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, "Save Name:", dialogX + 20, dialogY + DIALOG_HEIGHT - 90);
        
        // Draw save name input field with cursor
        dialogFont.setColor(Color.YELLOW);
        String displayText = saveNameBuffer + "_";
        dialogFont.draw(batch, displayText, dialogX + 20, dialogY + DIALOG_HEIGHT - 115);
        
        // Draw character count
        dialogFont.setColor(Color.LIGHT_GRAY);
        String countText = saveNameBuffer.length() + "/" + MAX_SAVE_NAME_LENGTH;
        dialogFont.draw(batch, countText, dialogX + DIALOG_WIDTH - 80, dialogY + DIALOG_HEIGHT - 115);
        
        // Draw error message if any
        if (!errorMessage.isEmpty()) {
            dialogFont.setColor(Color.RED);
            dialogFont.draw(batch, errorMessage, dialogX + 20, dialogY + DIALOG_HEIGHT - 145);
        }
        
        // Draw instructions
        dialogFont.setColor(Color.LIGHT_GRAY);
        dialogFont.draw(batch, "Enter to save, ESC to cancel", dialogX + 20, dialogY + 60);
        dialogFont.draw(batch, "Use letters, numbers, spaces, - and _", dialogX + 20, dialogY + 40);
    }
    
    /**
     * Renders the overwrite confirmation interface.
     */
    private void renderOverwriteConfirmation(SpriteBatch batch, float dialogX, float dialogY) {
        // Draw title
        dialogFont.setColor(Color.YELLOW);
        dialogFont.draw(batch, "Overwrite Existing Save?", dialogX + 20, dialogY + DIALOG_HEIGHT - 30);
        
        // Draw save name
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, "Save Name: " + saveNameBuffer.trim(), dialogX + 20, dialogY + DIALOG_HEIGHT - 70);
        
        // Draw warning message
        dialogFont.setColor(Color.RED);
        dialogFont.draw(batch, "A save with this name already exists.", dialogX + 20, dialogY + DIALOG_HEIGHT - 110);
        dialogFont.draw(batch, "Overwriting will permanently replace it.", dialogX + 20, dialogY + DIALOG_HEIGHT - 135);
        
        // Draw confirmation options
        dialogFont.setColor(Color.YELLOW);
        dialogFont.draw(batch, "Y - Yes, overwrite", dialogX + 50, dialogY + 80);
        dialogFont.draw(batch, "N - No, go back", dialogX + 50, dialogY + 55);
        
        // Draw instructions
        dialogFont.setColor(Color.LIGHT_GRAY);
        dialogFont.draw(batch, "Press Y to confirm or N to cancel", dialogX + 20, dialogY + 25);
    }
    
    /**
     * Renders the save progress interface.
     */
    private void renderSaveProgress(SpriteBatch batch, float dialogX, float dialogY) {
        // Draw title
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, "Saving World...", dialogX + 20, dialogY + DIALOG_HEIGHT - 30);
        
        // Draw save name
        dialogFont.setColor(Color.YELLOW);
        dialogFont.draw(batch, "Save Name: " + saveNameBuffer.trim(), dialogX + 20, dialogY + DIALOG_HEIGHT - 70);
        
        // Draw progress message
        dialogFont.setColor(Color.LIGHT_GRAY);
        dialogFont.draw(batch, "Please wait while your world is being saved.", dialogX + 20, dialogY + DIALOG_HEIGHT - 110);
        dialogFont.draw(batch, "This may take a moment for large worlds.", dialogX + 20, dialogY + DIALOG_HEIGHT - 135);
        
        // Draw animated dots for progress indication
        long time = System.currentTimeMillis();
        int dotCount = (int)((time / 500) % 4); // Change every 500ms, cycle through 0-3 dots
        String dots = "";
        for (int i = 0; i < dotCount; i++) {
            dots += ".";
        }
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, "Saving" + dots, dialogX + 20, dialogY + DIALOG_HEIGHT - 170);
        
        // Draw cancel instruction
        dialogFont.setColor(Color.LIGHT_GRAY);
        dialogFont.draw(batch, "ESC to cancel", dialogX + 20, dialogY + 30);
    }
    
    /**
     * Sets an error message to display in the dialog.
     * 
     * @param message The error message to display
     */
    public void setErrorMessage(String message) {
        this.errorMessage = message != null ? message : "";
        this.showingSaveProgress = false; // Stop showing progress on error
    }
    
    /**
     * Indicates that the save operation completed successfully.
     */
    public void setSaveCompleted() {
        this.confirmed = true;
        this.isVisible = false;
        this.showingSaveProgress = false;
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
     * Checks if the user confirmed the save operation.
     * 
     * @return true if confirmed, false otherwise
     */
    public boolean isConfirmed() {
        return confirmed;
    }
    
    /**
     * Checks if the user cancelled the save operation.
     * 
     * @return true if cancelled, false otherwise
     */
    public boolean isCancelled() {
        return cancelled;
    }
    
    /**
     * Gets the entered save name (trimmed).
     * 
     * @return The entered save name
     */
    public String getSaveName() {
        return saveNameBuffer.trim();
    }
    
    /**
     * Resets the dialog state.
     * Should be called after handling the user's choice.
     */
    public void reset() {
        confirmed = false;
        cancelled = false;
        showingOverwriteConfirmation = false;
        showingSaveProgress = false;
        errorMessage = "";
    }
    
    /**
     * Hides the dialog.
     */
    public void hide() {
        isVisible = false;
        reset();
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