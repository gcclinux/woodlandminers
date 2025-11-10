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
import wagemaker.uk.world.WorldSaveInfo;
import wagemaker.uk.world.WorldSaveManager;

import java.util.List;
import java.util.ArrayList;

/**
 * WorldManageDialog provides save management functionality.
 * Displays saves with detailed statistics and allows deletion with confirmation.
 * Shows file sizes, creation dates, and world statistics for save management.
 */
public class WorldManageDialog {
    private boolean isVisible = false;
    private boolean actionCompleted = false;
    private boolean cancelled = false;
    private boolean showingDeleteConfirmation = false;
    private boolean showingDeleteProgress = false;
    private Texture woodenPlank;
    private BitmapFont dialogFont;
    private BitmapFont smallFont;
    private List<WorldSaveInfo> availableSaves = new ArrayList<>();
    private int selectedSaveIndex = 0;
    private boolean isMultiplayer = false;
    private String errorMessage = "";
    private String statusMessage = "";
    private static final float DIALOG_WIDTH = 650;
    private static final float DIALOG_HEIGHT = 450;
    private static final int SAVES_PER_PAGE = 6;
    private int scrollOffset = 0;
    
    /**
     * Creates a new WorldManageDialog with wooden plank background and custom fonts.
     */
    public WorldManageDialog() {
        woodenPlank = createWoodenPlank();
        createDialogFonts();
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
            parameter.size = 11;
            smallFont = generator.generateFont(parameter);
            
            generator.dispose();
        } catch (Exception e) {
            System.out.println("Could not load custom font for world manage dialog, using default: " + e.getMessage());
            // Fallback to default fonts
            dialogFont = new BitmapFont();
            dialogFont.getData().setScale(1.3f);
            dialogFont.setColor(Color.WHITE);
            
            smallFont = new BitmapFont();
            smallFont.getData().setScale(0.9f);
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
     * Shows the manage dialog and loads available saves.
     * 
     * @param isMultiplayer true if managing multiplayer saves, false for singleplayer
     */
    public void show(boolean isMultiplayer) {
        this.isVisible = true;
        this.actionCompleted = false;
        this.cancelled = false;
        this.showingDeleteConfirmation = false;
        this.showingDeleteProgress = false;
        this.isMultiplayer = isMultiplayer;
        this.selectedSaveIndex = 0;
        this.scrollOffset = 0;
        this.errorMessage = "";
        this.statusMessage = "";
        
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
                statusMessage = "No saved worlds found";
            } else {
                // Ensure selected index is valid
                if (selectedSaveIndex >= availableSaves.size()) {
                    selectedSaveIndex = 0;
                }
                statusMessage = availableSaves.size() + " save(s) found";
            }
            errorMessage = "";
        } catch (Exception e) {
            errorMessage = "Error loading saves: " + e.getMessage();
            statusMessage = "";
            availableSaves.clear();
        }
    }
    
    /**
     * Handles keyboard input for navigation and actions.
     * Arrow keys navigate between saves, Delete key deletes selected save, ESC cancels.
     */
    public void handleInput() {
        if (!isVisible) {
            return;
        }
        
        if (showingDeleteProgress) {
            // During delete progress, only allow ESC to cancel
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                cancelled = true;
                isVisible = false;
            }
            return;
        }
        
        if (showingDeleteConfirmation) {
            handleDeleteConfirmationInput();
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
            
            // Handle delete key (delete selected save)
            if (Gdx.input.isKeyJustPressed(Input.Keys.FORWARD_DEL) || Gdx.input.isKeyJustPressed(Input.Keys.DEL)) {
                if (selectedSaveIndex >= 0 && selectedSaveIndex < availableSaves.size()) {
                    showingDeleteConfirmation = true;
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
     * Handles input during delete confirmation.
     */
    private void handleDeleteConfirmationInput() {
        // Y or Enter to confirm delete
        if (Gdx.input.isKeyJustPressed(Input.Keys.Y) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            showingDeleteConfirmation = false;
            proceedWithDelete();
        }
        
        // N or ESC to cancel delete
        if (Gdx.input.isKeyJustPressed(Input.Keys.N) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            showingDeleteConfirmation = false;
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
     * Proceeds with the delete operation.
     */
    private void proceedWithDelete() {
        if (selectedSaveIndex < 0 || selectedSaveIndex >= availableSaves.size()) {
            return;
        }
        
        WorldSaveInfo saveInfo = availableSaves.get(selectedSaveIndex);
        showingDeleteProgress = true;
        
        // Perform the actual delete operation
        boolean success = WorldSaveManager.deleteSave(saveInfo.getSaveName(), isMultiplayer);
        
        if (success) {
            statusMessage = "Save deleted successfully: " + saveInfo.getSaveName();
            errorMessage = "";
            
            // Remove from list and adjust selection
            availableSaves.remove(selectedSaveIndex);
            if (selectedSaveIndex >= availableSaves.size() && selectedSaveIndex > 0) {
                selectedSaveIndex--;
            }
            updateScrollOffset();
            
            actionCompleted = true;
        } else {
            errorMessage = "Failed to delete save: " + saveInfo.getSaveName();
            statusMessage = "";
        }
        
        showingDeleteProgress = false;
    }
    
    /**
     * Renders the manage dialog with save list and statistics.
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
        
        if (showingDeleteProgress) {
            renderDeleteProgress(batch, dialogX, dialogY);
        } else if (showingDeleteConfirmation) {
            renderDeleteConfirmation(batch, dialogX, dialogY);
        } else {
            renderManageInterface(batch, dialogX, dialogY);
        }
        
        batch.end();
    }
    
    /**
     * Renders the main manage interface.
     */
    private void renderManageInterface(SpriteBatch batch, float dialogX, float dialogY) {
        // Draw title
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, "Manage Saved Worlds", dialogX + 20, dialogY + DIALOG_HEIGHT - 30);
        
        // Draw game mode indicator
        smallFont.setColor(Color.LIGHT_GRAY);
        String modeText = isMultiplayer ? "Multiplayer Saves" : "Singleplayer Saves";
        smallFont.draw(batch, modeText, dialogX + 20, dialogY + DIALOG_HEIGHT - 50);
        
        // Draw status or error message
        if (!errorMessage.isEmpty()) {
            smallFont.setColor(Color.RED);
            smallFont.draw(batch, errorMessage, dialogX + 20, dialogY + DIALOG_HEIGHT - 70);
        } else if (!statusMessage.isEmpty()) {
            smallFont.setColor(Color.LIGHT_GRAY);
            smallFont.draw(batch, statusMessage, dialogX + 20, dialogY + DIALOG_HEIGHT - 70);
        }
        
        if (availableSaves.isEmpty()) {
            renderNoSavesMessage(batch, dialogX, dialogY);
        } else {
            renderSaveEntries(batch, dialogX, dialogY);
            renderSummaryStatistics(batch, dialogX, dialogY);
        }
        
        // Draw instructions
        smallFont.setColor(Color.LIGHT_GRAY);
        if (!availableSaves.isEmpty()) {
            smallFont.draw(batch, "Up/Down to select, Delete key to delete save", dialogX + 20, dialogY + 60);
            smallFont.draw(batch, "Page Up/Down for fast scroll", dialogX + 20, dialogY + 45);
        }
        smallFont.draw(batch, "R to refresh, ESC to close", dialogX + 20, dialogY + 30);
    }
    
    /**
     * Renders the message when no saves are available.
     */
    private void renderNoSavesMessage(SpriteBatch batch, float dialogX, float dialogY) {
        dialogFont.setColor(Color.LIGHT_GRAY);
        dialogFont.draw(batch, "No saved worlds found.", dialogX + 20, dialogY + DIALOG_HEIGHT / 2 + 20);
        dialogFont.draw(batch, "Create saves using 'Save World' option.", dialogX + 20, dialogY + DIALOG_HEIGHT / 2 - 10);
    }
    
    /**
     * Renders the list of save entries with detailed information.
     */
    private void renderSaveEntries(SpriteBatch batch, float dialogX, float dialogY) {
        float startY = dialogY + DIALOG_HEIGHT - 100;
        float entryHeight = 50;
        
        // Draw visible saves
        int endIndex = Math.min(scrollOffset + SAVES_PER_PAGE, availableSaves.size());
        
        for (int i = scrollOffset; i < endIndex; i++) {
            WorldSaveInfo saveInfo = availableSaves.get(i);
            float entryY = startY - (i - scrollOffset) * entryHeight;
            
            // Highlight selected save
            boolean isSelected = (i == selectedSaveIndex);
            
            if (isSelected) {
                // Draw selection indicator
                dialogFont.setColor(Color.YELLOW);
                dialogFont.draw(batch, "> ", dialogX + 5, entryY);
            }
            
            // Draw save name
            dialogFont.setColor(isSelected ? Color.YELLOW : Color.WHITE);
            dialogFont.draw(batch, saveInfo.getSaveName(), dialogX + 25, entryY);
            
            // Draw timestamp and file size
            smallFont.setColor(isSelected ? Color.LIGHT_GRAY : Color.GRAY);
            String timeAndSize = saveInfo.getFormattedTimestamp() + " | " + saveInfo.getFormattedFileSize();
            smallFont.draw(batch, timeAndSize, dialogX + 25, entryY - 15);
            
            // Draw detailed world statistics
            String worldStats = String.format("Seed: %d | Trees: %d | Items: %d | Cleared: %d | Player: %s (%.0f HP)", 
                saveInfo.getWorldSeed(), saveInfo.getTreeCount(), saveInfo.getItemCount(),
                saveInfo.getClearedPositionCount(), saveInfo.getFormattedPlayerPosition(), 
                saveInfo.getPlayerHealth());
            smallFont.draw(batch, worldStats, dialogX + 25, entryY - 30);
        }
        
        // Draw scroll indicators
        if (scrollOffset > 0) {
            smallFont.setColor(Color.YELLOW);
            smallFont.draw(batch, "^ More above ^", dialogX + DIALOG_WIDTH / 2 - 50, startY + 15);
        }
        
        if (scrollOffset + SAVES_PER_PAGE < availableSaves.size()) {
            smallFont.setColor(Color.YELLOW);
            smallFont.draw(batch, "v More below v", dialogX + DIALOG_WIDTH / 2 - 50, dialogY + 120);
        }
    }
    
    /**
     * Renders summary statistics for all saves.
     */
    private void renderSummaryStatistics(SpriteBatch batch, float dialogX, float dialogY) {
        if (availableSaves.isEmpty()) {
            return;
        }
        
        // Calculate totals
        long totalSize = 0;
        int totalTrees = 0;
        int totalItems = 0;
        int totalCleared = 0;
        
        for (WorldSaveInfo saveInfo : availableSaves) {
            totalSize += saveInfo.getFileSizeBytes();
            totalTrees += saveInfo.getTreeCount();
            totalItems += saveInfo.getItemCount();
            totalCleared += saveInfo.getClearedPositionCount();
        }
        
        // Format total size
        String totalSizeFormatted;
        if (totalSize < 1024 * 1024) {
            totalSizeFormatted = String.format("%.1f KB", totalSize / 1024.0);
        } else {
            totalSizeFormatted = String.format("%.1f MB", totalSize / (1024.0 * 1024.0));
        }
        
        // Draw summary box
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, "--- Summary Statistics ---", dialogX + 20, dialogY + 110);
        smallFont.draw(batch, String.format("Total Saves: %d | Total Size: %s", 
                      availableSaves.size(), totalSizeFormatted), dialogX + 20, dialogY + 95);
        smallFont.draw(batch, String.format("Total Trees: %d | Total Items: %d | Total Cleared: %d", 
                      totalTrees, totalItems, totalCleared), dialogX + 20, dialogY + 80);
    }
    
    /**
     * Renders the delete confirmation interface.
     */
    private void renderDeleteConfirmation(SpriteBatch batch, float dialogX, float dialogY) {
        if (selectedSaveIndex < 0 || selectedSaveIndex >= availableSaves.size()) {
            return;
        }
        
        WorldSaveInfo saveInfo = availableSaves.get(selectedSaveIndex);
        
        // Draw title
        dialogFont.setColor(Color.RED);
        dialogFont.draw(batch, "Delete Save?", dialogX + 20, dialogY + DIALOG_HEIGHT - 30);
        
        // Draw save details
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, "Save Name: " + saveInfo.getSaveName(), dialogX + 20, dialogY + DIALOG_HEIGHT - 70);
        
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, "Created: " + saveInfo.getFormattedTimestamp(), dialogX + 20, dialogY + DIALOG_HEIGHT - 95);
        smallFont.draw(batch, "File Size: " + saveInfo.getFormattedFileSize(), dialogX + 20, dialogY + DIALOG_HEIGHT - 115);
        smallFont.draw(batch, "World Seed: " + saveInfo.getWorldSeed(), dialogX + 20, dialogY + DIALOG_HEIGHT - 135);
        smallFont.draw(batch, String.format("Contains: %d trees, %d items, %d cleared areas", 
                      saveInfo.getTreeCount(), saveInfo.getItemCount(), saveInfo.getClearedPositionCount()),
                      dialogX + 20, dialogY + DIALOG_HEIGHT - 155);
        
        // Draw warning
        dialogFont.setColor(Color.RED);
        dialogFont.draw(batch, "WARNING: This action cannot be undone!", dialogX + 20, dialogY + DIALOG_HEIGHT - 185);
        dialogFont.draw(batch, "The save file will be permanently deleted.", dialogX + 20, dialogY + DIALOG_HEIGHT - 210);
        
        // Draw confirmation options
        dialogFont.setColor(Color.YELLOW);
        dialogFont.draw(batch, "Y - Yes, delete permanently", dialogX + 50, dialogY + 80);
        dialogFont.draw(batch, "N - No, keep the save", dialogX + 50, dialogY + 55);
        
        // Draw instructions
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, "Press Y to confirm deletion or N to cancel", dialogX + 20, dialogY + 25);
    }
    
    /**
     * Renders the delete progress interface.
     */
    private void renderDeleteProgress(SpriteBatch batch, float dialogX, float dialogY) {
        if (selectedSaveIndex < 0 || selectedSaveIndex >= availableSaves.size()) {
            return;
        }
        
        WorldSaveInfo saveInfo = availableSaves.get(selectedSaveIndex);
        
        // Draw title
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, "Deleting Save...", dialogX + 20, dialogY + DIALOG_HEIGHT - 30);
        
        // Draw save name
        dialogFont.setColor(Color.YELLOW);
        dialogFont.draw(batch, "Deleting: " + saveInfo.getSaveName(), dialogX + 20, dialogY + DIALOG_HEIGHT - 70);
        
        // Draw progress message
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, "Please wait while the save file is being deleted.", dialogX + 20, dialogY + DIALOG_HEIGHT - 110);
        
        // Draw animated dots for progress indication
        long time = System.currentTimeMillis();
        int dotCount = (int)((time / 500) % 4); // Change every 500ms, cycle through 0-3 dots
        String dots = "";
        for (int i = 0; i < dotCount; i++) {
            dots += ".";
        }
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, "Deleting" + dots, dialogX + 20, dialogY + DIALOG_HEIGHT - 150);
        
        // Draw cancel instruction
        smallFont.setColor(Color.LIGHT_GRAY);
        smallFont.draw(batch, "ESC to cancel", dialogX + 20, dialogY + 30);
    }
    
    /**
     * Sets an error message to display in the dialog.
     * 
     * @param message The error message to display
     */
    public void setErrorMessage(String message) {
        this.errorMessage = message != null ? message : "";
        this.statusMessage = "";
        this.showingDeleteProgress = false; // Stop showing progress on error
    }
    
    /**
     * Sets a status message to display in the dialog.
     * 
     * @param message The status message to display
     */
    public void setStatusMessage(String message) {
        this.statusMessage = message != null ? message : "";
        this.errorMessage = "";
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
     * Checks if an action was completed (like deleting a save).
     * 
     * @return true if an action was completed, false otherwise
     */
    public boolean isActionCompleted() {
        return actionCompleted;
    }
    
    /**
     * Checks if the user cancelled the operation.
     * 
     * @return true if cancelled, false otherwise
     */
    public boolean isCancelled() {
        return cancelled;
    }
    
    /**
     * Gets the currently selected save info.
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
     * Gets the total number of available saves.
     * 
     * @return The number of available saves
     */
    public int getSaveCount() {
        return availableSaves.size();
    }
    
    /**
     * Resets the dialog state.
     * Should be called after handling the user's choice.
     */
    public void reset() {
        actionCompleted = false;
        cancelled = false;
        showingDeleteConfirmation = false;
        showingDeleteProgress = false;
        errorMessage = "";
        statusMessage = "";
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
        if (smallFont != null) {
            smallFont.dispose();
        }
    }
}