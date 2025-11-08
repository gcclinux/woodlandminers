# Multiplayer Server Configuration Update

## Summary

Updated the multiplayer server connection configuration to save to `$HOME/.config/woodlanders/woodlanders.json` instead of `player.properties`.

## Changes Made

### 1. PlayerConfig.java
- **Changed storage format**: From Java Properties to JSON
- **Changed storage location**: From `player.properties` (working directory) to `$HOME/.config/woodlanders/woodlanders.json`
- **Added migration support**: Automatically migrates from old `player.properties` format on first load
- **Platform-specific paths**:
  - Linux: `~/.config/woodlanders/woodlanders.json`
  - macOS: `~/Library/Application Support/Woodlanders/woodlanders.json`
  - Windows: `%APPDATA%/Woodlanders/woodlanders.json`

### 2. MyGdxGame.java
- **Added server save**: After successful connection, saves the server address to config using `PlayerConfig.saveLastServer()`
- The server address is saved in format `address:port` (e.g., `localhost:25565`)

### 3. GameMenu.java
- **Pre-fill support**: Already loads the last server from `PlayerConfig` and pre-fills the connect dialog
- This functionality was already implemented and continues to work with the new JSON format

### 4. PlayerConfigTest.java
- **Updated tests**: Modified to work with JSON format instead of Properties format
- Tests now use the actual config directory structure

## How It Works

1. **First Connection**: When you connect to a multiplayer server for the first time:
   - The connection is made
   - If successful, the server address is saved to `woodlanders.json`
   
2. **Subsequent Connections**: When you open the "Connect to Server" dialog:
   - The last server address is automatically loaded from `woodlanders.json`
   - The address is pre-filled in the input field
   - You can edit it or press Enter to connect

3. **Migration**: If you have an existing `player.properties` file:
   - On first load, the configuration is automatically migrated to the new JSON format
   - The old file is left in place (not deleted) for safety

## JSON Format

The configuration file uses a simple JSON structure:

```json
{
  "lastServer": "localhost:25565",
  "playerPosition": {
    "x": 100.00,
    "y": 200.00
  },
  "playerHealth": 100.0,
  "playerName": "Player",
  "savedAt": "Sat Nov 08 12:34:56 GMT 2025"
}
```

The `lastServer` field is added/updated when connecting to a multiplayer server, while other fields are managed by the game save system.

## Testing

To test the changes:

1. Delete any existing `woodlanders.json` file from your config directory
2. Run the game and connect to a multiplayer server
3. Check that `~/.config/woodlanders/woodlanders.json` (Linux) contains the `lastServer` field
4. Close and reopen the game
5. Open the "Connect to Server" dialog and verify the last server is pre-filled

## Backward Compatibility

- Old `player.properties` files are automatically migrated on first load
- The migration is one-way (old format → new format)
- The old `player.properties` file is not deleted to avoid data loss
