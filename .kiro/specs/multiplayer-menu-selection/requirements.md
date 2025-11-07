# Requirements Document

## Introduction

This feature improves the multiplayer menu flow in the Woodlanders game. Currently, when a player presses ESC and selects "Multiplayer" from the main menu, the game automatically attempts to start a multiplayer server. This feature will modify the behavior so that selecting "Multiplayer" opens a submenu where the player can choose to either host a new server or connect to an existing server, providing better control over multiplayer functionality.

## Glossary

- **Main Menu**: The primary game menu accessed by pressing ESC, containing options like Player Name, Multiplayer, Save, and Exit
- **Multiplayer Menu**: A submenu that displays options for multiplayer gameplay (Host Server, Connect to Server, Back)
- **Host Server**: The action of starting a local multiplayer server and connecting to it as the host player
- **Connect to Server**: The action of joining an existing multiplayer server by entering its address
- **Game Instance**: The MyGdxGame class that manages the overall game state and multiplayer connections
- **GameMenu**: The UI component that manages all menu interactions and displays

## Requirements

### Requirement 1

**User Story:** As a player, I want to see multiplayer options when I select "Multiplayer" from the main menu, so that I can choose whether to host or join a server

#### Acceptance Criteria

1. WHEN the player selects "Multiplayer" from the Main Menu, THE Game Instance SHALL display the Multiplayer Menu with options for "Host Server", "Connect to Server", and "Back"
2. WHILE the Multiplayer Menu is displayed, THE Game Instance SHALL allow the player to navigate between options using arrow keys
3. WHEN the player selects "Back" from the Multiplayer Menu, THE Game Instance SHALL return to the Main Menu
4. THE Multiplayer Menu SHALL use the same wooden plank visual style as the Main Menu for consistency

### Requirement 2

**User Story:** As a player, I want to host a multiplayer server from the multiplayer menu, so that other players can join my game

#### Acceptance Criteria

1. WHEN the player selects "Host Server" from the Multiplayer Menu, THE Game Instance SHALL attempt to start a multiplayer server
2. IF the server starts successfully, THEN THE Game Instance SHALL display the server IP address in a dialog
3. IF the server fails to start, THEN THE Game Instance SHALL display an error message with retry and cancel options
4. WHEN the server starts successfully, THE Game Instance SHALL automatically connect the local player as the host

### Requirement 3

**User Story:** As a player, I want to connect to an existing multiplayer server from the multiplayer menu, so that I can join games hosted by other players

#### Acceptance Criteria

1. WHEN the player selects "Connect to Server" from the Multiplayer Menu, THE Game Instance SHALL display a connection dialog prompting for server address
2. WHEN the player enters a server address and confirms, THE Game Instance SHALL attempt to connect to the specified server
3. IF the connection succeeds, THEN THE Game Instance SHALL display a success notification
4. IF the connection fails, THEN THE Game Instance SHALL display an error message with retry and cancel options
5. THE Game Instance SHALL support server addresses in the format "address:port" or "address" with default port 25565

### Requirement 4

**User Story:** As a player, I want the multiplayer menu to remain open until I make a selection, so that I have time to decide which option to choose

#### Acceptance Criteria

1. WHILE the Multiplayer Menu is open, THE Game Instance SHALL not automatically start any multiplayer connections
2. WHEN the player presses ESC while the Multiplayer Menu is open, THE Game Instance SHALL close the Multiplayer Menu and return to the Main Menu
3. THE Multiplayer Menu SHALL remain visible until the player selects an option or presses ESC

### Requirement 5

**User Story:** As a player, I want the game to pause when any menu is open, so that I can navigate menus without affecting my character or game state

#### Acceptance Criteria

1. WHILE the Main Menu is open, THE Game Instance SHALL prevent the player character from moving or performing actions
2. WHILE the Multiplayer Menu is open, THE Game Instance SHALL prevent the player character from moving or performing actions
3. WHILE any dialog is open, THE Game Instance SHALL prevent the player character from moving or performing actions
4. WHEN the player presses arrow keys while a menu is open, THE Game Instance SHALL only navigate the menu and SHALL not move the player character
5. WHILE any menu or dialog is open, THE Game Instance SHALL continue rendering the game world but SHALL not update player position or game state
