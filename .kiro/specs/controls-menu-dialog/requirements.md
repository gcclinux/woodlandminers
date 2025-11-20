# Requirements Document

## Introduction

This document specifies the requirements for a Controls Menu Dialog feature in the Woodlanders game. The feature will provide players with an in-game reference for all available keyboard controls through a dedicated menu option accessible from the main game menu. The dialog will display all control bindings in a clear, organized format using the game's existing visual style (wooden plank background and custom fonts).

## Glossary

- **Game Menu**: The main pause menu accessed by pressing ESC, containing options like Player Name, Save World, Multiplayer, etc.
- **Controls Dialog**: A modal dialog window that displays all keyboard control bindings to the player
- **Menu Item**: A selectable option in the Game Menu list
- **Wooden Plank Background**: The brown textured background used consistently across all game dialogs
- **Dialog Font**: The custom Sancreek-Regular.ttf font used for dialog text rendering
- **Control Binding**: A mapping between a game action and its associated keyboard key(s)
- **LocalizationManager**: The system component responsible for managing multi-language text translations

## Requirements

### Requirement 1

**User Story:** As a player, I want to access a controls reference from the main menu, so that I can learn or remind myself of the available keyboard controls without leaving the game.

#### Acceptance Criteria

1. WHEN the player opens the Game Menu THEN the system SHALL display a "Controls" menu item in the menu list
2. WHEN the player navigates to the Controls menu item and presses Enter THEN the system SHALL open the Controls Dialog
3. WHEN the Controls Dialog is open THEN the system SHALL prevent game input from affecting gameplay
4. WHEN the player presses ESC while the Controls Dialog is open THEN the system SHALL close the dialog and return to the Game Menu
5. WHERE the game supports multiple languages THEN the system SHALL display control labels in the currently selected language
6. WHEN the Controls menu item is added to the Game Menu THEN the system SHALL increase the Game Menu background height to accommodate the additional menu item
7. WHEN the Game Menu is rendered with the Controls item THEN the system SHALL maintain proper spacing between all menu items

### Requirement 2

**User Story:** As a player, I want to see all available controls organized clearly, so that I can quickly find the information I need.

#### Acceptance Criteria

1. WHEN the Controls Dialog is displayed THEN the system SHALL show all player movement controls (UP, DOWN, LEFT, RIGHT arrows)
2. WHEN the Controls Dialog is displayed THEN the system SHALL show the inventory control (I key)
3. WHEN the Controls Dialog is displayed THEN the system SHALL show item navigation controls (LEFT, RIGHT arrows)
4. WHEN the Controls Dialog is displayed THEN the system SHALL show item selection controls (P key for planting, SPACE BAR for planting/consuming)
5. WHEN the Controls Dialog is displayed THEN the system SHALL show targeting system controls (W, A, S, D keys)
6. WHEN the Controls Dialog is displayed THEN the system SHALL show the attack control (SPACE BAR)
7. WHEN the Controls Dialog is displayed THEN the system SHALL show the menu control (ESC key)
8. WHEN the Controls Dialog is displayed THEN the system SHALL show the world deletion control (X key)
9. WHEN the Controls Dialog is displayed THEN the system SHALL show the compass target selection control (TAB key)
10. WHEN the Controls Dialog is displayed THEN the system SHALL organize controls into logical groups for readability

### Requirement 3

**User Story:** As a player, I want the Controls Dialog to match the game's visual style, so that it feels like a natural part of the game interface.

#### Acceptance Criteria

1. WHEN the Controls Dialog is rendered THEN the system SHALL use the wooden plank texture as the background
2. WHEN the Controls Dialog is rendered THEN the system SHALL use the Sancreek-Regular.ttf font for all text
3. WHEN the Controls Dialog is rendered THEN the system SHALL apply consistent text colors matching other dialogs (white for labels, yellow for highlights)
4. WHEN the Controls Dialog is rendered THEN the system SHALL center the dialog on the screen relative to the camera position
5. WHEN the Controls Dialog is rendered THEN the system SHALL use wood grain lines and border styling consistent with other dialogs

### Requirement 4

**User Story:** As a developer, I want the Controls Dialog to integrate seamlessly with existing menu systems, so that maintenance and future updates are straightforward.

#### Acceptance Criteria

1. WHEN implementing the Controls Dialog THEN the system SHALL follow the same architectural pattern as LanguageDialog and WorldSaveDialog
2. WHEN implementing the Controls Dialog THEN the system SHALL implement the LanguageChangeListener interface for localization support
3. WHEN the Controls Dialog is created THEN the system SHALL register itself with the LocalizationManager
4. WHEN the Controls Dialog is disposed THEN the system SHALL unregister itself from the LocalizationManager
5. WHEN the language changes THEN the system SHALL update all displayed control labels to the new language

### Requirement 5

**User Story:** As a player who speaks a different language, I want all control labels and descriptions to be available in my language, so that I can understand the controls in my native language.

#### Acceptance Criteria

1. WHEN translation keys are defined THEN the system SHALL add all Controls Dialog text to each supported language file (en.json, de.json, nl.json, pl.json, pt.json)
2. WHEN translation keys are defined THEN the system SHALL include a translation key for the Controls menu item label
3. WHEN translation keys are defined THEN the system SHALL include translation keys for the dialog title
4. WHEN translation keys are defined THEN the system SHALL include translation keys for each control category header
5. WHEN translation keys are defined THEN the system SHALL include translation keys for each control action description
6. WHEN translation keys are defined THEN the system SHALL include translation keys for dialog instructions (e.g., "Press ESC to close")
7. WHEN the Controls Dialog renders text THEN the system SHALL retrieve all text from LocalizationManager using the defined translation keys
