# Multiplayer Disconnect Feature

## Overview
Added a new menu option that allows players to disconnect from multiplayer sessions and return to singleplayer mode without quitting the game.

## Changes Made

### 1. GameMenu.java
- **Added two menu item arrays**:
  - `singleplayerMenuItems`: ["Player Name", "Multiplayer", "Save", "Exit"]
  - `multiplayerMenuItems`: ["Player Name", "Disconnect", "Exit"]

- **Dynamic menu rendering**: The menu now displays different options based on the current game mode
  - In singleplayer: Shows "Multiplayer" and "Save" options
  - In multiplayer: Shows "Disconnect" option (no "Save" or "Multiplayer" options)

- **New methods**:
  - `getCurrentMenuItems()`: Returns the appropriate menu items based on game mode
  - `disconnectFromMultiplayer()`: Calls the game instance's disconnect method

- **Updated menu navigation**: Menu navigation now uses the current menu items array to ensure proper wrapping

### 2. MyGdxGame.java
- **New method**: `disconnectFromMultiplayer()`
  - Cleanly disconnects the game client
  - Stops the game server (if hosting)
  - Disposes all remote players
  - Clears all pending multiplayer queues
  - Resets player's game client reference
  - Resets connection quality indicator
  - Returns game mode to SINGLEPLAYER
  - Resets world seed to 0
  - Reinitializes rain zones for singleplayer
  - Displays a notification to the player

## Usage
1. While in a multiplayer session, press ESC to open the menu
2. Navigate to "Disconnect" option
3. Press ENTER to disconnect and return to singleplayer mode
4. The game continues running in singleplayer mode at your current position

## Benefits
- No need to quit the entire game to leave multiplayer
- Clean resource cleanup prevents memory leaks
- Seamless transition back to singleplayer mode
- Player position is preserved after disconnect
