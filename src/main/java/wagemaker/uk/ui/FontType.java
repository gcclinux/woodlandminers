package wagemaker.uk.ui;

/**
 * Enum representing available font types for the game UI.
 * Each font type has a display name and file path.
 */
public enum FontType {
    NOTO_SANS("NotoSans", "fonts/NotoSans-BoldItalic.ttf"),
    SAIRA("Saira", "fonts/Saira_SemiExpanded-MediumItalic.ttf"),
    SANCREEK("Sancreek", "fonts/Sancreek-Regular.ttf"),
    STACK_SAN("StackSan", "fonts/StackSansText-Regular.ttf");
    
    private final String displayName;
    private final String filePath;
    
    FontType(String displayName, String filePath) {
        this.displayName = displayName;
        this.filePath = filePath;
    }
    
    /**
     * Gets the display name of the font.
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the file path of the font.
     * @return The file path
     */
    public String getFilePath() {
        return filePath;
    }
    
    /**
     * Gets a FontType by its display name.
     * @param displayName The display name to search for
     * @return The matching FontType, or SANCREEK as default
     */
    public static FontType fromDisplayName(String displayName) {
        for (FontType type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        return SANCREEK; // Default fallback
    }
    
    /**
     * Gets all available font display names.
     * @return Array of font display names
     */
    public static String[] getAllDisplayNames() {
        FontType[] types = values();
        String[] names = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            names[i] = types[i].displayName;
        }
        return names;
    }
}
