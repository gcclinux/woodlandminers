package wagemaker.uk.ui;

/**
 * Interface for components that need to be notified when the font changes.
 */
public interface FontChangeListener {
    /**
     * Called when the game font is changed.
     * @param newFont The new font type
     */
    void onFontChanged(FontType newFont);
}
