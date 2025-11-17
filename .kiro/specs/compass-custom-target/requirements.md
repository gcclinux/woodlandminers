# Requirements Document

## Introduction

This feature extends the existing compass system to allow players to set custom target coordinates. Currently, the compass always points to the world origin (0,0). This enhancement enables players to view their current position and set a new target location, making it easier for players to navigate to specific coordinates or meet up with other players in multiplayer sessions.

## Glossary

- **Compass System**: The existing UI component that displays a compass needle pointing toward a target location
- **Player Location Menu**: A new dialog that displays current player coordinates and allows input of target coordinates
- **Target Coordinates**: The x,y position that the compass needle will point toward
- **World Origin**: The default compass target at coordinates (0,0)
- **Main Menu**: The in-game menu accessed during gameplay that contains options like inventory, settings, etc.

## Requirements

### Requirement 1

**User Story:** As a player, I want to access my current coordinates from the main menu, so that I can share my location with other players

#### Acceptance Criteria

1. WHEN the player opens the main menu, THE Main Menu SHALL display a "Player Location" option below the player name
2. WHEN the player selects "Player Location", THE Player Location Menu SHALL open at the center of the screen
3. THE Player Location Menu SHALL display the player's current x coordinate with label "x:"
4. THE Player Location Menu SHALL display the player's current y coordinate with label "y:"
5. THE Player Location Menu SHALL update the displayed coordinates in real-time while the menu remains open

### Requirement 2

**User Story:** As a player, I want to set custom target coordinates for my compass, so that I can navigate to specific locations or meet up with other players

#### Acceptance Criteria

1. THE Player Location Menu SHALL display two input fields labeled "New Location" for target x and y coordinates
2. WHEN the player enters valid numeric values in both x and y input fields, THE Player Location Menu SHALL enable a confirmation action
3. WHEN the player confirms the new target coordinates, THE Compass System SHALL update its target to point toward the specified coordinates
4. WHEN the player enters invalid or non-numeric values, THE Player Location Menu SHALL display an error indicator and prevent confirmation
5. THE Player Location Menu SHALL allow negative coordinate values for both x and y inputs

### Requirement 3

**User Story:** As a player, I want to reset my compass to point to the world origin, so that I can return to the default navigation behavior

#### Acceptance Criteria

1. THE Player Location Menu SHALL display a "Reset to Origin" button or option
2. WHEN the player selects "Reset to Origin", THE Compass System SHALL update its target to point toward coordinates (0,0)
3. WHEN the compass target is reset to origin, THE Player Location Menu SHALL clear any custom target coordinate values

### Requirement 4

**User Story:** As a player, I want the Player Location Menu to have a visually consistent design, so that it matches the game's aesthetic

#### Acceptance Criteria

1. THE Player Location Menu SHALL display a wooden plank background texture consistent with other game UI elements
2. THE Player Location Menu SHALL center itself on the screen when opened
3. THE Player Location Menu SHALL display a title "Player Coordinates" at the top
4. THE Player Location Menu SHALL organize information with clear visual separation between current location and new location sections
5. WHEN the player clicks outside the menu or presses escape, THE Player Location Menu SHALL close

### Requirement 5

**User Story:** As a player in multiplayer mode, I want my custom compass target to persist across sessions, so that I don't lose my navigation target when reconnecting

#### Acceptance Criteria

1. WHEN the player sets a custom compass target, THE Compass System SHALL save the target coordinates to the player's configuration
2. WHEN the player disconnects and reconnects to a multiplayer session, THE Compass System SHALL restore the previously set custom target coordinates
3. WHEN the player starts a new single-player session, THE Compass System SHALL restore the previously set custom target coordinates
4. WHERE the player has no saved custom target, THE Compass System SHALL default to pointing toward the world origin (0,0)
