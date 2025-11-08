# Last Server Persistence Feature

## Overview

The game now stores the last successfully connected multiplayer server address in the client configuration file. When opening the "Connect to Server" dialog, the last server address is automatically pre-filled, making it easier to reconnect to frequently used servers.

## Implementation Details

### Configuration File Location

The last server address is stored in the same configuration file as player data:

- **Windows**: `%APPDATA%/Woodlanders/woodlanders.json`
- **macOS**: `~/Library/Application Support/Woodlanders/woodlanders.json`
- **Linux**: `~/.config/woodlanders/woodlanders.json`

### JSON Structure

The configuration file contains both player data and the last server address:

```json
{
  "playerPosition": {
    "x": 270.08,
    "y": -150.01
  },
  "playerHealth": 70.0,
  "playerName": "Player",
  "lastServer": "192.168.1.100:25565",
  "savedAt": "Sat Nov 08 10:14:11 GMT 2025"
}
```

### Key Components

#### 1. PlayerConfig Class (`src/main/java/wagemaker/uk/client/PlayerConfig.java`)

- Manages the `lastServer` field
- Provides `saveLastServer(String address)` method to save server addresses
- Provides `getLastServer()` method to retrieve the last server
- Handles JSON parsing and file I/O

#### 2. MyGdxGame Class (`src/main/java/wagemaker/uk/gdx/MyGdxGame.java`)

The `attemptConnectToServer` method saves the server address after a successful connection:

```java
public void attemptConnectToServer(String address, int port) {
    try {
        // Store the full address string (with port)
        lastConnectionAddress = address + ":" + port;
        
        joinMultiplayerServer(address, port);
        
        // Save the last server address to config after successful connection
        PlayerConfig config = PlayerConfig.load();
        config.saveLastServer(lastConnectionAddress);
        
        displayNotification("Connected to server!");
    } catch (Exception e) {
        gameMenu.showError("Failed to connect: " + e.getMessage());
    }
}
```

#### 3. GameMenu Class (`src/main/java/wagemaker/uk/ui/GameMenu.java`)

- **Loading**: When opening the connect dialog, loads the last server from `PlayerConfig` and pre-fills it
- **Saving**: When saving player data, preserves the `lastServer` field to avoid overwriting it

The `handleMultiplayerMenuSelection` method loads and pre-fills the last server:

```java
private void handleMultiplayerMenuSelection() {
    String selected = multiplayerMenu.getSelectedOption();
    
    if (selected.equals("Connect to Server")) {
        multiplayerMenu.close();
        
        // Load PlayerConfig and pre-fill the saved server address
        PlayerConfig config = PlayerConfig.load();
        String lastServer = config.getLastServer();
        if (lastServer != null && !lastServer.isEmpty()) {
            connectDialog.setPrefilledAddress(lastServer);
        }
        
        connectDialog.show();
    }
    // ...
}
```

The `savePlayerPosition` method preserves the `lastServer` field:

```java
private void savePlayerPosition() {
    // Read existing lastServer value if file exists
    String lastServer = null;
    if (saveFile.exists()) {
        String existingContent = new String(Files.readAllBytes(...));
        lastServer = parseJsonString(existingContent, "\"lastServer\":");
    }
    
    // Include lastServer in the new JSON if it exists
    if (lastServer != null && !lastServer.isEmpty()) {
        jsonBuilder.append(String.format("  \"lastServer\": \"%s\",\n", lastServer));
    }
    // ...
}
```

#### 4. ConnectDialog Class (`src/main/java/wagemaker/uk/ui/ConnectDialog.java`)

Provides separate input fields for IP address and port number:
- `show(String address)` - Shows the dialog and parses `ip:port` format to pre-fill both fields
- `setPrefilledAddress(String address)` - Pre-fills the fields from `ip:port` format
- `getEnteredAddress()` - Returns the combined address in `ip:port` format
- Supports Tab key to switch between fields
- IP field accepts: letters, numbers, dots, hyphens (for hostnames)
- Port field accepts: numbers only (1-65535)
- Default port: `25565`

## User Experience

1. **First Connection**: User enters IP address (e.g., `192.168.1.100`) and port (e.g., `25565`) in separate fields and connects
2. **Successful Connection**: The server address is automatically saved to the config file in format `ip:port`
3. **Subsequent Connections**: When opening the "Connect to Server" dialog:
   - The IP address field is pre-filled with the last server IP
   - The port field is pre-filled with the last server port (or defaults to `25565`)
   - The IP address field is focused by default
4. **Editing**: 
   - User can edit either field
   - Press `Tab` to switch between IP address and port fields
   - The active field is highlighted in yellow with a cursor
5. **Failed Connections**: Failed connection attempts do NOT save the server address

### Dialog Layout

```
┌────────────────────────────┐
│   Connect to Server        │
│                            │
│   IP Address:              │
│   localhost_               │  ← Active field (yellow)
│   Port Number:             │
│   25565                    │  ← Inactive field (gray)
│                            │
│   Tab to switch fields     │
│   Enter, or ESC to Cancel  │
└────────────────────────────┘
```

## Testing

The feature is covered by comprehensive tests:

### Unit Tests

- `PlayerConfigTest`: Tests the `PlayerConfig` class functionality
  - Save and load round trip
  - Handling of null/empty addresses
  - Corrupted file handling

### Integration Tests

- `GameMenuConfigIntegrationTest`: Tests the integration between `GameMenu` and `PlayerConfig`
  - Verifies that `lastServer` is preserved when saving player data
  - Tests behavior when no `lastServer` exists

- `ConnectionFlowIntegrationTest`: Tests the end-to-end connection flow
  - Verifies server address is saved after successful connection
  - Tests multiple connection cycles
  - Verifies failed connections don't save addresses

All tests pass successfully (41/41 tests passing).

## Benefits

1. **Convenience**: Players don't need to re-type server addresses
2. **Reduced Errors**: Pre-filled addresses reduce typos
3. **Quick Reconnection**: Easy to reconnect to favorite servers
4. **Non-Intrusive**: The feature works seamlessly in the background
5. **Data Preservation**: Player data and server address coexist in the same file without conflicts

## Future Enhancements

Potential improvements for future versions:

1. **Server History**: Store multiple recent servers instead of just the last one
2. **Server Favorites**: Allow users to save and name favorite servers
3. **Auto-Reconnect**: Automatically reconnect to the last server on game start
4. **Server Browser**: Display a list of available servers on the local network
