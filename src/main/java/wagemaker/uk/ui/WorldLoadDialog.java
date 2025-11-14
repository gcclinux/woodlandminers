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
import wagemaker.uk.world.WorldSaveInfo;
import wagemaker.uk.world.WorldSaveManager;

import java.util.List;
import java.util.ArrayList;

/**
 * WorldLoadDialog displays available saves with metadata for selection.
 * Shows save timestamps, game mode, world information, and allows save selection.
 * Provides load confirmation and handles cases where no saves are available.
 */
public class WorldLoadDialog implements LanguageChangeListener {
    private boolean isVisible = false;
    private boolean confirmed = false;
    private boolean cancelled = false;
    private boolean showingLoadConfirmation = false;
    private boolean showingLoadProgress = false;
    private Texture woodenPlank;
    private BitmapFont dialogFont;
    private BitmapFont smallFont;
    private List<WorldSaveInfo> availableSaves = new ArrayList<>();
    private int selectedSaveIndex = 0;
    private boolean isMultiplayer = false;
    private String errorMessage = "";
    private static final float DIALOG_WIDTH = 720; // Increased by 20% (600 * 1.2 = 720)
    private static final float DIALOG_HEIGHT = 400;
    private static final int SAVES_PER_PAGE = 6;
    private int scrollOffset = 0;
    
    /**
     * Creates a new WorldLoadDialog with wooden plank background and custom fonts.
     */
    public WorldLoadDialog() {
        woodenPlank = createWoodenPlank();
        createDialogFonts();
        
        // Register for language change notifications
        LocalizationManager.getInstance().addLanguageChangeListener(this);
    }
    
    /**
     * Creates the custom fonts for dialog text using slkscr.ttf.
     */
    private void createDialogFonts() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/slkscr.ttf"));
            
            // Main dialog font
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 16;
            parameter.color = Color.WHITE;
            parameter.borderWidth = 1;
            parameter.borderColor = Color.BLACK;
            parameter.shadowOffsetX = 1;
            parameter.shadowOffsetY = 1;
            parameter.shadowColor = Color.BLACK;
            
            dialogFont = generator.generateFont(parameter);
            
            // Smaller font for details
            parameter.size = 12;
            smallFont = generator.generateFont(parameter);
            
            generator.dispose();
        } catch (Exception e) {
            System.out.println("Could not load custom font for world load dialog, using default: " + e.getMessage());
            // Fallback to default fonts
            dialogFont = new BitmapFont();
            dialogFont.getData().setScale(1.3f);
            dialogFont.setColor(Color.WHITE);
            
            smallFont = new BitmapFont();
            smallFont.getData().setScale(1.0f);
            smallFont.setColor(Color.WHITE);
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
     * Shows the load dialog and loads available saves.
     * 
     * @param isMultiplayer true if loading multiplayer saves, false for singleplayer
     */
    public void show(boolean isMultiplayer) {
        this.isVisible = true;
        this.confirmed = false;
        this.cancelled = false;
        this.showingLoadConfirmation = false;
        this.showingLoadProgress = false;
        this.isMultiplayer = isMultiplayer;
        this.selectedSaveIndex = 0;
        this.scrollOffset = 0;
        this.errorMessage = "";
        
        // Load available saves
        loadAvailableSaves();
    }
    
    /**
     * Loads the list of available saves from the WorldSaveManager.
     */
    private void loadAvailableSaves() {
        try {
            availableSaves = WorldSaveManager.listAvailableSaves(isMultiplayer);
            
            if (availableSaves.isEmpty()) {
                errorMessage = LocalizationManager.getInstance().getText("world_load_dialog.no_saves_title");
            } else {
                // Ensure selected index is valid
                if (selectedSaveIndex >= availableSaves.size()) {
                    selectedSaveIndex = 0;
                }
                errorMessage = "";
            }
        } catch (Exception e) {
            errorMessage = LocalizationManager.getInstance().getText("messages.load_error", e.getMessage());
            availableSaves.clear();
        }
    }
    
    /**
     * Handles keyboard input for navigation and selection.
     * Arrow keys navigate between saves, Enter confirms selection, ESC cancels.
     */
    public void handleInput() {
        if (!isVisible) {
            return;
        }
        
        if (showingLoadProgress) {
            // During load progress, only allow ESC to cancel
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                cancelled = true;
                isVisible = false;
            }
            return;
        }
        
        if (showingLoadConfirmation) {
            handleLoadConfirmationInput();
            return;
        }
        
        // Handle navigation when saves are available
        if (!availableSaves.isEmpty()) {
            // Handle up/down arrow keys for navigation
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                selectedSaveIndex--;
                if (selectedSaveIndex < 0) {
                    selectedSaveIndex = availableSaves.size() - 1;
                }
                updateScrollOffset();
            }
            
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                selectedSaveIndex++;
                if (selectedSaveIndex >= availableSaves.size()) {
                    selectedSaveIndex = 0;
                }
                updateScrollOffset();
            }
            
            // Handle page up/down for faster navigation
            if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_UP)) {
                selectedSaveIndex = Math.max(0, selectedSaveIndex - SAVES_PER_PAGE);
                updateScrollOffset();
            }
            
            if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_DOWN)) {
                selectedSaveIndex = Math.min(availableSaves.size() - 1, selectedSaveIndex + SAVES_PER_PAGE);
                updateScrollOffset();
            }
            
            // Handle enter (confirm selection)
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                if (selectedSaveIndex >= 0 && selectedSaveIndex < availableSaves.size()) {
                    showingLoadConfirmation = true;
                }
            }
        }
        
        // Handle R to refresh save list
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            loadAvailableSaves();
        }
        
        // Handle escape (cancel)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            cancelled = true;
            isVisible = false;
        }
    }
    
    /**
     * Handles input during load confirmation.
     */
    private void handleLoadConfirmationInput() {
        // Y or Enter to confirm load
        if (Gdx.input.isKeyJustPressed(Input.Keys.Y) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            showingLoadConfirmation = false;
            proceedWithLoad();
        }
        
        // N or ESC to cancel load
        if (Gdx.input.isKeyJustPressed(Input.Keys.N) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            showingLoadConfirmation = false;
        }
    }
    
    /**
     * Updates the scroll offset to keep the selected save visible.
     */
    private void updateScrollOffset() {
        if (selectedSaveIndex < scrollOffset) {
            scrollOffset = selectedSaveIndex;
        } else if (selectedSaveIndex >= scrollOffset + SAVES_PER_PAGE) {
            scrollOffset = selectedSaveIndex - SAVES_PER_PAGE + 1;
        }
    }
    
    /**
     * Proceeds with the load operation.
     */
    private void proceedWithLoad() {
        showingLoadProgress = true;
        confirmed = true;
        // The actual load operation will be handled by the caller
    }
    
    /**
     * Renders the load dialog with save list and metadata.
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
        
        if (showingLoadProgress) {
            renderLoadProgress(batch, dialogX, dialogY);
        } else if (showingLoadConfirmation) {
            renderLoadConfirmation(batch, dialogX, dialogY);
        } else {
            renderSaveList(batch, dialogX, dialogY);
        }
        
        batch.end();
    }
    
    /**
     * Renders the save list interface.
     */
    private void renderSaveList(SpriteBatch batch, float dialogX, float dialogY) {
        LocalizationManager loc = LocalizationManager.getInstance();
        
        // Draw title
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, loc.getText("world_load_dialog.title"), dialogX + 20, dialogY + DIALOG_HEIGHT - 30);
        
        // Draw game mode indicator
        smallFont.setColor(Color.LIGHT_GRAY);
        String modeText = isMultiplayer ? 
            loc.getText("world_load_dialog.multiplayer_saves") : 
            loc.getText("world_load_dialog.singleplayer_saves");
        smallFont.draw(batch, modeText, dialogX + 20, dialogY + DIALOG_HEIGHT - 50);
        
        // Draw save count with language-specific positioning
        String countText = loc.getText("world_load_dialog.saves_found", availableSaves.size());
        // Adjust position based on language (move inward/left for longer translations)
        float countTextOffset = 150; // Default for English
        String currentLang = loc.getCurrentLanguage();
        if (currentLang.equals("nl")) {
            countTextOffset = 210; // Dutch:
        } else if (currentLang.equals("pl")) {
            countTextOffset = 210; // Polish:
        } else if (currentLang.equals("pt")) {
            countTextOffset = 280; // Portuguese:
        }
        smallFont.draw(batch, countText, dialogX + DIALOG_WIDTH - countTextOffset, dialogY + DIALOG_HEIGHT - 50);
        
        if (availableSaves.isEmpty()) {
            renderNoSavesMessage(batch, dialogX, dialogY);
        } else {
            renderSaveEntries(batch, dialogX, dialogY);
        }
        
        // Draw instructions
        smallFont.setColor(Color.LIGHT_GRAY);
        if (!availableSaves.isEmpty()) {
            smallFont.draw(batch, loc.getText("world_load_dialog.select_instruction"), dialogX + 20, dialogY + 45);
            smallFont.draw(batch, loc.getText("world_load_dialog.scroll_instruction"), dialogX + 20, dialogY + 30);
        }
        smallFont.draw(batch, loc.getText("world_load_dialog.refresh_instruction"), dialogX + 20, dialogY + 15);
    }
    
    /**
     * Renders the message when no saves are available.
     */
    private void renderNoSavesMessage(SpriteBatch batch, float dialogX, float dialogY) {
        LocalizationManager loc = LocalizationManager.getInstance();
        
        if (!errorMessage.isEmpty()) {
            dialogFont.setColor(Color.RED);
            dialogFont.draw(batch, errorMessage, dialogX + 20, dialogY + DIALOG_HEIGHT / 2);
        } else {
            dialogFont.setColor(Color.LIGHT_GRAY);
            dialogFont.draw(batch, loc.getText("world_load_dialog.no_saves_title"), dialogX + 20, dialogY + DIALOG_HEIGHT / 2 + 20);
            dialogFont.draw(batch, loc.getText("world_load_dialog.no_saves_message"), dialogX + 20, dialogY + DIALOG_HEIGHT / 2 - 10);
        }
    }
    
    /**
     * Renders the list of save entries.
     */
    private void renderSaveEntries(SpriteBatch batch, float dialogX, float dialogY) {
        float startY = dialogY + DIALOG_HEIGHT - 80;
        float entryHeight = 45;
        
        // Draw visible saves
        int endIndex = Math.min(scrollOffset + SAVES_PER_PAGE, availableSaves.size());
        
        for (int i = scrollOffset; i < endIndex; i++) {
            WorldSaveInfo saveInfo = availableSaves.get(i);
            float entryY = startY - (i - scrollOffset) * entryHeight;
            
            // Highlight selected save
            boolean isSelected = (i == selectedSaveIndex);
            
            if (isSelected) {
                // Draw selection background (darker wood)
                dialogFont.setColor(Color.YELLOW);
                dialogFont.draw(batch, "> ", dialogX + 5, entryY);
            }
            
            // Draw save name
            dialogFont.setColor(isSelected ? Color.YELLOW : Color.WHITE);
            dialogFont.draw(batch, saveInfo.getSaveName(), dialogX + 25, entryY);
            
            // Draw timestamp and file size
            smallFont.setColor(isSelected ? Color.LIGHT_GRAY : Color.GRAY);
            String timeAndSize = saveInfo.getFormattedTimestamp() + " (" + saveInfo.getFormattedFileSize() + ")";
            smallFont.draw(batch, timeAndSize, dialogX + 25, entryY - 15);
            
            // Draw world info
            String worldInfo = String.format("Seed: %d | Trees: %d | Items: %d | Player: %s", 
                saveInfo.getWorldSeed(), saveInfo.getTreeCount(), saveInfo.getItemCount(),
                saveInfo.getFormattedPlayerPosition());
            smallFont.draw(batch, worldInfo, dialogX + 25, entryY - 30);
        }
        
        // Draw scroll indicators
        LocalizationManager loc = LocalizationManager.getInstance();
        
        if (scrollOffset > 0) {
            smallFont.setColor(Color.YELLOW);
            smallFont.draw(batch, loc.getText("world_load_dialog.more_above"), dialogX + DIALOG_WIDTH / 2 - 50, startY + 15);
        }
        
        if (scrollOffset + SAVES_PER_PAGE < availableSaves.size()) {
            smallFont.setColor(Color.YELLOW);
            smallFont.draw(batch, loc.getText("world_load_dialog.more_below"), dialogX + DIALOG_WIDTH / 2 - 50, dialogY + 70);
        }
    }
    
    /**
     * Renders the load confirmation interface.
     */
    private void renderLoadConfirmation(SpriteBatch batch, float dialogX, float dialogY) {
        if (selectedSaveIndex < 0 || selectedSaveIndex >= availableSaves.size()) {
            return;
        }
        
        LocalizationManager loc = LocalizationManager.getInstance();
        WorldSaveInfo saveInfo = availableSaves.get(selectedSaveIndex);
        
        // Draw title
        dialogFont.setColor(Color.YELLOW);
        dialogFont.draw(batch, loc.getText("world_load_dialog.load_confirm_title"), dialogX + 20, dialogY + DIALOG_HEIGHT - 30);
        
        // Draw save details
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, saveInfo.getSaveName(), dialogX + 20, dialogY + DIALOG_HEIGHT - 70);
        
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, loc.getText("world_load_dialog.created_label") + " " + saveInfo.getFormattedTimestamp(), 
                      dialogX + 20, dialogY + DIALOG_HEIGHT - 95);
        smallFont.draw(batch, loc.getText("world_load_dialog.world_seed_label") + " " + saveInfo.getWorldSeed(), 
                      dialogX + 20, dialogY + DIALOG_HEIGHT - 115);
        smallFont.draw(batch, loc.getText("world_load_dialog.trees_label") + " " + saveInfo.getTreeCount() + 
                      " | " + loc.getText("world_load_dialog.items_label") + " " + saveInfo.getItemCount(), 
                      dialogX + 20, dialogY + DIALOG_HEIGHT - 135);
        smallFont.draw(batch, loc.getText("world_load_dialog.player_position_label") + " " + saveInfo.getFormattedPlayerPosition(), 
                      dialogX + 20, dialogY + DIALOG_HEIGHT - 155);
        smallFont.draw(batch, loc.getText("world_load_dialog.file_size_label") + " " + saveInfo.getFormattedFileSize(), 
                      dialogX + 20, dialogY + DIALOG_HEIGHT - 175);
        
        // Draw warning
        dialogFont.setColor(Color.ORANGE);
        dialogFont.draw(batch, loc.getText("world_load_dialog.load_warning"), dialogX + 20, dialogY + DIALOG_HEIGHT - 205);
        
        // Draw confirmation options
        dialogFont.setColor(Color.YELLOW);
        dialogFont.draw(batch, loc.getText("world_load_dialog.yes_load"), dialogX + 50, dialogY + 80);
        dialogFont.draw(batch, loc.getText("world_load_dialog.no_go_back"), dialogX + 50, dialogY + 55);
        
        // Draw instructions
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, loc.getText("world_load_dialog.confirm_load_instruction"), dialogX + 20, dialogY + 25);
    }
    
    /**
     * Renders the load progress interface.
     */
    private void renderLoadProgress(SpriteBatch batch, float dialogX, float dialogY) {
        if (selectedSaveIndex < 0 || selectedSaveIndex >= availableSaves.size()) {
            return;
        }
        
        LocalizationManager loc = LocalizationManager.getInstance();
        WorldSaveInfo saveInfo = availableSaves.get(selectedSaveIndex);
        
        // Draw title
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, loc.getText("world_load_dialog.loading_title"), dialogX + 20, dialogY + DIALOG_HEIGHT - 30);
        
        // Draw save name
        dialogFont.setColor(Color.YELLOW);
        dialogFont.draw(batch, saveInfo.getSaveName(), dialogX + 20, dialogY + DIALOG_HEIGHT - 70);
        
        // Draw progress message
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, loc.getText("world_load_dialog.loading_message_1"), dialogX + 20, dialogY + DIALOG_HEIGHT - 110);
        smallFont.draw(batch, loc.getText("world_load_dialog.loading_message_2"), dialogX + 20, dialogY + DIALOG_HEIGHT - 130);
        
        // Draw animated dots for progress indication
        long time = System.currentTimeMillis();
        int dotCount = (int)((time / 500) % 4); // Change every 500ms, cycle through 0-3 dots
        String dots = "";
        for (int i = 0; i < dotCount; i++) {
            dots += ".";
        }
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, loc.getText("world_load_dialog.loading_progress") + dots, dialogX + 20, dialogY + DIALOG_HEIGHT - 170);
        
        // Draw cancel instruction
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, loc.getText("world_load_dialog.refresh_instruction").split(",")[1].trim(), dialogX + 20, dialogY + 30);
    }
    
    /**
     * Sets an error message to display in the dialog.
     * 
     * @param message The error message to display
     */
    public void setErrorMessage(String message) {
        this.errorMessage = message != null ? message : "";
        this.showingLoadProgress = false; // Stop showing progress on error
    }
    
    /**
     * Indicates that the load operation completed successfully.
     */
    public void setLoadCompleted() {
        this.confirmed = true;
        this.isVisible = false;
        this.showingLoadProgress = false;
    }
    
    /**
     * Refreshes the list of available saves.
     */
    public void refreshSaves() {
        loadAvailableSaves();
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
     * Checks if the user confirmed a load operation.
     * 
     * @return true if confirmed, false otherwise
     */
    public boolean isConfirmed() {
        return confirmed;
    }
    
    /**
     * Checks if the user cancelled the load operation.
     * 
     * @return true if cancelled, false otherwise
     */
    public boolean isCancelled() {
        return cancelled;
    }
    
    /**
     * Gets the selected save info.
     * 
     * @return The selected WorldSaveInfo, or null if none selected
     */
    public WorldSaveInfo getSelectedSave() {
        if (selectedSaveIndex >= 0 && selectedSaveIndex < availableSaves.size()) {
            return availableSaves.get(selectedSaveIndex);
        }
        return null;
    }
    
    /**
     * Gets the name of the selected save.
     * 
     * @return The selected save name, or null if none selected
     */
    public String getSelectedSaveName() {
        WorldSaveInfo saveInfo = getSelectedSave();
        return saveInfo != null ? saveInfo.getSaveName() : null;
    }
    
    /**
     * Resets the dialog state.
     * Should be called after handling the user's choice.
     */
    public void reset() {
        confirmed = false;
        cancelled = false;
        showingLoadConfirmation = false;
        showingLoadProgress = false;
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
     * Called when the language changes.
     * Refreshes error messages if needed.
     * 
     * @param newLanguage The new language code
     */
    @Override
    public void onLanguageChanged(String newLanguage) {
        // Refresh error message if it was set
        if (!errorMessage.isEmpty() && availableSaves.isEmpty()) {
            errorMessage = LocalizationManager.getInstance().getText("world_load_dialog.no_saves_title");
        }
        // Note: The dialog text will be automatically refreshed on next render
        // since all text is retrieved from LocalizationManager during rendering
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
        if (smallFont != null) {
            smallFont.dispose();
        }
    }
}