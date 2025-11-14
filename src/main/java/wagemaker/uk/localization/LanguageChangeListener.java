package wagemaker.uk.localization;

/**
 * Interface for components that need to be notified when the language changes.
 * Implementing classes should refresh their displayed text when onLanguageChanged is called.
 */
public interface LanguageChangeListener {
    /**
     * Called when the active language changes.
     * 
     * @param newLanguage The new language code (e.g., "en", "pl", "pt", "nl")
     */
    void onLanguageChanged(String newLanguage);
}
