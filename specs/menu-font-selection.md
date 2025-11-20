# Menu Font Selection Feature

## Overview
Add a font selection option to the Player Profile submenu, allowing players to choose between three available fonts for menu display and player name rendering.

## Goals
- Allow players to customize the visual appearance of menus and player names
- Provide three font options: NotoSans, Saira, and Sancreek
- Persist the font selection in player configuration
- Apply the selected font across all menus and player name display

## Available Fonts
1. **NotoSans** - `fonts/NotoSans-BoldItalic.ttf`
2. **Saira** - `fonts/Saira_SemiExpanded-MediumItalic.ttf`
3. **Sancreek** - `fonts/Sancreek-Regular.ttf` (current default)

## Requirements

### Functional Requirements

#### FR1: Font Selection Dialog
- WHEN the player selects "Menu Font" from the Player Profile submenu
- THEN a font selection dialog SHALL display with three options:
  - NotoSans
  - Saira
  - Sancreek
- The currently selected font SHALL be highlighted
- Player can navigate with UP/DOWN arrows
- Player can confirm selection with ENTER
- Player can cancel with ESC

#### FR2: Font Application
- WHEN a player selects a new font
- THEN the font SHALL be applied to:
  - All menu text (main menu, submenus)
  - Player name display above character
  - Dialog text (all existing dialogs)
- The change SHALL be visible immediately

#### FR3: Font Persistence
- WHEN a player selects a font
- THEN the selection SHALL be saved to PlayerConfig
- WHEN the game restarts
- THEN the saved font preference SHALL be loaded and applied

#### FR4: Player Profile Menu Update
- The Player Profile submenu SHALL include a new "Menu Font" option
- Menu items order:
  1. Player Name
  2. Save Player
  3. Language
  4. Menu Font (new)
  5. Back

### Non-Functional Requirements

#### NFR1: Performance
- Font switching SHALL occur within 100ms
- No noticeable lag when changing fonts

#### NFR2: Localization
- Font selection dialog SHALL support all languages (en, de, nl, pl, pt)
- Font names are not translated (NotoSans, Saira, Sancreek)

#### NFR3: Compatibility
- Font selection SHALL work in both singleplayer and multiplayer modes
- Font preference is per-player, not per-game mode

## Design

### Architecture

#### Components
1. **FontSelectionDialog** - New dialog class for font selection
2. **FontManager** - Singleton class to manage font loading and switching
3. **PlayerConfig** - Extended to store font preference
4. **PlayerProfileMenu** - Updated to include Menu Font option
5. **GameMenu** - Updated to use FontManager for all font rendering

### Data Model

#### PlayerConfig Extension
```json
{
  "playerName": "Player",
  "language": "en",
  "fontName": "Sancreek",  // NEW: Font preference
  "singleplayerPosition": { ... },
  ...
}
```

#### Font Configuration
```java
public enum FontType {
    NOTO_SANS("NotoSans", "fonts/NotoSans-BoldItalic.ttf"),
    SAIRA("Saira", "fonts/Saira_SemiExpanded-MediumItalic.ttf"),
    SANCREEK("Sancreek", "fonts/Sancreek-Regular.ttf");
    
    private final String displayName;
    private final String filePath;
}
```

### User Interface

#### Font Selection Dialog
```
┌─────────────────────────────────┐
│  Select Menu Font               │
│                                 │
│  > NotoSans          ◄ Current  │
│    Saira                        │
│    Sancreek                     │
│                                 │
│  Up/Down to select              │
│  Enter to confirm, ESC to cancel│
└─────────────────────────────────┘
```

## Implementation Plan

### Phase 1: Core Infrastructure
1. Create `FontType` enum with font definitions
2. Create `FontManager` singleton class
   - Load and cache fonts
   - Provide method to get current font
   - Provide method to switch fonts
   - Notify listeners of font changes
3. Create `FontChangeListener` interface
4. Update `PlayerConfig` to store/load font preference

### Phase 2: Font Selection Dialog
1. Create `FontSelectionDialog` class
   - Wooden plank background (consistent with other dialogs)
   - List of font options
   - Highlight current selection
   - Handle UP/DOWN/ENTER/ESC input
   - Implement `LanguageChangeListener` for localization
2. Add localization keys to all language files
   - `font_selection_dialog.title`
   - `font_selection_dialog.current`
   - `font_selection_dialog.select_instruction`
   - `font_selection_dialog.cancel_instruction`

### Phase 3: Integration
1. Update `PlayerProfileMenu`
   - Add "Menu Font" option (4th item)
   - Update menu height if needed
2. Update `GameMenu`
   - Add `FontSelectionDialog` instance
   - Integrate font selection in Player Profile menu handler
   - Replace hardcoded font creation with `FontManager.getInstance().getCurrentFont()`
   - Implement `FontChangeListener` to refresh fonts
3. Update all dialog classes to use `FontManager`
   - `MultiplayerMenu`
   - `PlayerProfileMenu`
   - `LanguageDialog`
   - `ControlsDialog`
   - `ErrorDialog`
   - `WorldSaveDialog`
   - `WorldLoadDialog`
   - etc.

### Phase 4: Testing
1. Test font switching in all menus
2. Test font persistence across restarts
3. Test in both singleplayer and multiplayer modes
4. Test with all languages
5. Test player name rendering with different fonts

## Localization Keys

### English (en.json)
```json
{
  "font_selection_dialog": {
    "title": "Select Menu Font",
    "current": "Current",
    "select_instruction": "Up/Down to select, Enter to confirm",
    "cancel_instruction": "ESC to cancel"
  },
  "player_profile_menu": {
    "menu_font": "Menu Font"
  }
}
```

### German (de.json)
```json
{
  "font_selection_dialog": {
    "title": "Menüschrift Wählen",
    "current": "Aktuell",
    "select_instruction": "Hoch/Runter zum Auswählen, Enter zum Bestätigen",
    "cancel_instruction": "ESC zum Abbrechen"
  },
  "player_profile_menu": {
    "menu_font": "Menüschrift"
  }
}
```

### Dutch (nl.json)
```json
{
  "font_selection_dialog": {
    "title": "Selecteer Menulettertype",
    "current": "Huidig",
    "select_instruction": "Omhoog/Omlaag om te selecteren, Enter om te bevestigen",
    "cancel_instruction": "ESC om te annuleren"
  },
  "player_profile_menu": {
    "menu_font": "Menulettertype"
  }
}
```

### Polish (pl.json)
```json
{
  "font_selection_dialog": {
    "title": "Wybierz Czcionkę Menu",
    "current": "Aktualny",
    "select_instruction": "Góra/Dół aby wybrać, Enter aby potwierdzić",
    "cancel_instruction": "ESC aby anulować"
  },
  "player_profile_menu": {
    "menu_font": "Czcionka Menu"
  }
}
```

### Portuguese (pt.json)
```json
{
  "font_selection_dialog": {
    "title": "Selecionar Fonte do Menu",
    "current": "Atual",
    "select_instruction": "Cima/Baixo para selecionar, Enter para confirmar",
    "cancel_instruction": "ESC para cancelar"
  },
  "player_profile_menu": {
    "menu_font": "Fonte do Menu"
  }
}
```

## Testing Checklist

- [ ] Font selection dialog displays correctly
- [ ] All three fonts can be selected
- [ ] Current font is highlighted in dialog
- [ ] ESC closes dialog without changing font
- [ ] ENTER confirms font selection
- [ ] Font changes apply immediately to all menus
- [ ] Font changes apply to player name display
- [ ] Font preference is saved to PlayerConfig
- [ ] Font preference loads correctly on restart
- [ ] Works in singleplayer mode
- [ ] Works in multiplayer mode
- [ ] All languages display correctly
- [ ] No performance issues when switching fonts
- [ ] No memory leaks (old fonts are properly disposed)

## File Changes

### New Files
- `src/main/java/wagemaker/uk/ui/FontManager.java`
- `src/main/java/wagemaker/uk/ui/FontChangeListener.java`
- `src/main/java/wagemaker/uk/ui/FontSelectionDialog.java`
- `src/main/java/wagemaker/uk/ui/FontType.java`

### Modified Files
- `src/main/java/wagemaker/uk/client/PlayerConfig.java`
- `src/main/java/wagemaker/uk/ui/GameMenu.java`
- `src/main/java/wagemaker/uk/ui/PlayerProfileMenu.java`
- `src/main/java/wagemaker/uk/ui/MultiplayerMenu.java`
- `assets/localization/en.json`
- `assets/localization/de.json`
- `assets/localization/nl.json`
- `assets/localization/pl.json`
- `assets/localization/pt.json`

## Future Enhancements
- Add font size selection
- Add more font options
- Preview text in font selection dialog
- Font selection for different UI elements separately (menu vs. dialogs)
