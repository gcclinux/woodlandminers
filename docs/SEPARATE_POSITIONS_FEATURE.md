# Separate Singleplayer and Multiplayer Positions

## Overview
The game now maintains separate player positions for singleplayer and multiplayer modes. When you switch between modes, your position in each mode is preserved independently.

## How It Works

### Saving Positions
- **In Singleplayer Mode**: When you save (or auto-save on exit), your position is stored as `singleplayerPosition`
- **In Multiplayer Mode**: Your position is stored as `multiplayerPosition`
- Both positions are maintained in the same save file (`woodlanders.json`)

### Loading Positions
- **Starting Singleplayer**: Loads your last singleplayer position
- **Joining Multiplayer**: Loads your last multiplayer position (or spawns at 0,0 if no multiplayer position exists)
- **Disconnecting from Multiplayer**: Automatically saves your multiplayer position and loads your singleplayer position

### Automatic Position Management
The game automatically handles position switching:
1. When you join a multiplayer server, your current singleplayer position is saved
2. Your last multiplayer position is loaded (or you spawn at 0,0)
3. When you disconnect, your multiplayer position is saved
4. Your singleplayer position is restored

## Save File Format

The new save file format includes separate position data:

```json
{
  "playerName": "YourName",
  "singleplayerPosition": {
    "x": 123.45,
    "y": 678.90
  },
  "singleplayerHealth": 100.0,
  "multiplayerPosition": {
    "x": 0.00,
    "y": 0.00
  },
  "multiplayerHealth": 100.0,
  "lastServer": "localhost:25565",
  "savedAt": "Sun Nov 09 12:34:56 UTC 2025"
}
```

## Backwards Compatibility

The system maintains backwards compatibility with old save files:
- If loading a position and the object format doesn't exist, it falls back to the flat format (`singleplayerX`, `singleplayerY`, etc.)
- For singleplayer, it also falls back to the original format (`x`, `y`, and `playerHealth` fields)
- If no position data exists for a mode, the player spawns at the default position (0, 0)

## Benefits

1. **Independent Progress**: Your singleplayer and multiplayer progress are kept separate
2. **No Position Loss**: Switching modes doesn't lose your position in either mode
3. **Seamless Transitions**: Positions are automatically saved and loaded when switching modes
4. **Shared Player Name**: Your player name is shared across both modes

## Technical Details

### Modified Files
- `GameMenu.java`:
  - Updated `savePlayerPosition()` to save separate positions based on game mode
  - Updated `loadPlayerPosition()` to load the appropriate position based on game mode
  - Made `savePlayerPosition()` public for access from MyGdxGame

- `MyGdxGame.java`:
  - `startMultiplayerHost()` now saves singleplayer position before hosting
  - `joinMultiplayerServer()` now saves singleplayer position before connecting
  - `disconnectFromMultiplayer()` now saves multiplayer position and loads singleplayer position

### Position Tracking
- Positions are now stored using nested objects (`singleplayerPosition`, `multiplayerPosition`) for better organization
- The old flat format (`singleplayerX`, `singleplayerY`, etc.) is supported for backwards compatibility but no longer written to new saves
- Health is tracked separately for each mode using flat fields (`singleplayerHealth`, `multiplayerHealth`)
