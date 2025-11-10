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
 * ErrorDialog displays error messages with retry and cancel options.
 * Used for connection failures and other error scenarios.
 */
public class ErrorDialog {
    private boolean isVisible = false;
    private boolean retrySelected = false;
    private boolean cancelled = false;
    private boolean okSelected = false;
    private Texture woodenPlank;
    private BitmapFont dialogFont;
    private String errorMessage = "";
    private String dialogTitle = "Error";
    private boolean isSuccessMode = false; // true for success messages, false for errors
    private int selectedOption = 0; // 0 = Retry/OK, 1 = Cancel (only in error mode)
    private static final float DIALOG_WIDTH = 500;
    private static final float DIALOG_HEIGHT = 250;
    
    /**
     * Creates a new ErrorDialog with wooden plank background and custom font.
     */
    public ErrorDialog() {
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
            System.out.println("Could not load custom font for error dialog, using default: " + e.getMessage());
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
     * Shows the error dialog with the specified error message.
     * 
     * @param message The error message to display
     */
    public void show(String message) {
        this.isVisible = true;
        this.retrySelected = false;
        this.cancelled = false;
        this.okSelected = false;
        this.errorMessage = message;
        this.dialogTitle = "Error"; // Default title
        this.isSuccessMode = false; // Default to error mode
        this.selectedOption = 0;
    }
    
    /**
     * Shows the error dialog with the specified error message and title.
     * 
     * @param message The error message to display
     * @param title The title for the dialog
     */
    public void show(String message, String title) {
        this.isVisible = true;
        this.retrySelected = false;
        this.cancelled = false;
        this.okSelected = false;
        this.errorMessage = message;
        this.dialogTitle = title;
        this.isSuccessMode = false; // Default to error mode
        this.selectedOption = 0;
    }
    
    /**
     * Shows the dialog as a success message with OK button.
     * 
     * @param message The success message to display
     * @param title The title for the dialog
     */
    public void showSuccess(String message, String title) {
        this.isVisible = true;
        this.retrySelected = false;
        this.cancelled = false;
        this.okSelected = false;
        this.errorMessage = message;
        this.dialogTitle = title;
        this.isSuccessMode = true; // Success mode shows only OK button
        this.selectedOption = 0;
    }
    
    /**
     * Handles keyboard input for option selection.
     * Arrow keys navigate between options, Enter confirms selection.
     */
    public void handleInput() {
        if (!isVisible) {
            return;
        }
        
        if (isSuccessMode) {
            // Success mode: only OK button, any key closes the dialog
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || 
                Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) ||
                Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                okSelected = true;
                isVisible = false;
                System.out.println("OK selected");
            }
        } else {
            // Error mode: Retry/Cancel buttons
            // Handle up/down arrow keys for navigation
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                selectedOption = 0; // Retry
            }
            
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                selectedOption = 1; // Cancel
            }
            
            // Handle enter (confirm selection)
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                if (selectedOption == 0) {
                    retrySelected = true;
                    isVisible = false;
                    System.out.println("Retry selected");
                } else {
                    cancelled = true;
                    isVisible = false;
                    System.out.println("Cancel selected");
                }
            }
            
            // Handle escape (same as cancel)
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                cancelled = true;
                isVisible = false;
            }
        }
    }
    
    /**
     * Renders the error dialog with the error message and options.
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
        dialogFont.setColor(Color.RED);
        dialogFont.draw(batch, dialogTitle, dialogX + 20, dialogY + DIALOG_HEIGHT - 30);
        
        // Draw error message (wrap text if too long)
        dialogFont.setColor(Color.WHITE);
        String wrappedMessage = wrapText(errorMessage, 60);
        float messageY = dialogY + DIALOG_HEIGHT - 80;
        for (String line : wrappedMessage.split("\n")) {
            dialogFont.draw(batch, line, dialogX + 20, messageY);
            messageY -= 25;
        }
        
        // Draw options based on mode
        float optionY = dialogY + 80;
        
        if (isSuccessMode) {
            // Success mode: only OK button (centered)
            dialogFont.setColor(Color.YELLOW);
            dialogFont.draw(batch, "OK", dialogX + DIALOG_WIDTH / 2 - 10, optionY);
            
            // Draw instructions for success mode
            dialogFont.setColor(Color.LIGHT_GRAY);
            dialogFont.draw(batch, "Press Enter, Space, or ESC to continue", dialogX + 40, dialogY + 30);
        } else {
            // Error mode: Retry/Cancel buttons
            // Retry option
            if (selectedOption == 0) {
                dialogFont.setColor(Color.YELLOW);
            } else {
                dialogFont.setColor(Color.LIGHT_GRAY);
            }
            dialogFont.draw(batch, "Retry", dialogX + 150, optionY);
            
            // Cancel option
            if (selectedOption == 1) {
                dialogFont.setColor(Color.YELLOW);
            } else {
                dialogFont.setColor(Color.LIGHT_GRAY);
            }
            dialogFont.draw(batch, "Cancel", dialogX + 300, optionY);
            
            // Draw instructions for error mode
            dialogFont.setColor(Color.LIGHT_GRAY);
            dialogFont.draw(batch, "Arrow keys to select, Enter to confirm", dialogX + 80, dialogY + 30);
        }
        
        batch.end();
    }
    
    /**
     * Wraps text to fit within a specified character width.
     * 
     * @param text The text to wrap
     * @param maxCharsPerLine Maximum characters per line
     * @return The wrapped text with newlines
     */
    private String wrapText(String text, int maxCharsPerLine) {
        if (text.length() <= maxCharsPerLine) {
            return text;
        }
        
        StringBuilder wrapped = new StringBuilder();
        String[] words = text.split(" ");
        int currentLineLength = 0;
        
        for (String word : words) {
            if (currentLineLength + word.length() + 1 > maxCharsPerLine) {
                wrapped.append("\n");
                currentLineLength = 0;
            }
            
            if (currentLineLength > 0) {
                wrapped.append(" ");
                currentLineLength++;
            }
            
            wrapped.append(word);
            currentLineLength += word.length();
        }
        
        return wrapped.toString();
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
     * Checks if the user selected retry.
     * 
     * @return true if retry was selected, false otherwise
     */
    public boolean isRetrySelected() {
        return retrySelected;
    }
    
    /**
     * Checks if the user cancelled.
     * 
     * @return true if cancelled, false otherwise
     */
    public boolean isCancelled() {
        return cancelled;
    }
    
    /**
     * Checks if the user selected OK (in success mode).
     * 
     * @return true if OK was selected, false otherwise
     */
    public boolean isOkSelected() {
        return okSelected;
    }
    
    /**
     * Resets the dialog state.
     * Should be called after handling the user's choice.
     */
    public void reset() {
        retrySelected = false;
        cancelled = false;
        okSelected = false;
    }
    
    /**
     * Hides the dialog.
     */
    public void hide() {
        isVisible = false;
        retrySelected = false;
        cancelled = false;
        okSelected = false;
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
