# Design Document: Server Connection Memory

## Overview

This feature adds persistent storage of the last successfully connected server address to the game client. When a player successfully connects to a multiplayer server, the server address is saved to a player configuration file. The next time the player opens the connection dialog, this saved address is automatically pre-populated, streamlining the reconnection process.

## Architecture

### Component Overview

The solution consists of three main components:

1. **PlayerConfig** - A new configuration management class that handles reading/writing player preferences
2. **ConnectDialog** - Enhanced to support pre-populating the server address field
3. **GameMessageHandler** - Modified to trigger config save on successful connection

### Data Flow

```
Player Connects → ConnectionAcceptedMessage → GameMessageHandler 
    → PlayerConfig.saveLastServer() → Write to disk

Player Opens Dialog → ConnectDialog.show() → PlayerConfig.getLastServer() 
    → Pre-populate input field
```

## Components and Interfaces

### 1. PlayerConfig Class

**Location**: `src/main/java/wagemaker/uk/client/PlayerConfig.java`

**Purpose**: Manages player-specific configuration settings including the last connected server address.

**Key Methods**:
- `static PlayerConfig load()` - Loads configuration from disk, returns default config if file doesn't exist
- `void saveLastServer(String address)` - Saves the server address and persists to disk
- `String getLastServer()` - Returns the last connected server address, or null if none saved
- `void save()` - Writes the current configuration to disk

**Properties Format**: Uses Java Properties file format for human readability
- File location: `player.properties` in the working directory
- Property key: `multiplayer.last-server`

**Error Handling**:
- If the config file doesn't exist on load, returns a default config without errors
- If save fails (I/O error), logs the error but doesn't crash the application
- Invalid property values are ignored and use defaults

### 2. ConnectDialog Enhancements

**Location**: `src/main/java/wagemaker/uk/ui/ConnectDialog.java`

**Changes**:
- Add `setPrefilledAddress(String address)` method to pre-populate the input field
- Modify `show()` method to accept an optional pre-filled address parameter
- Update `show()` to set `inputBuffer` to the pre-filled value if provided

**Behavior**:
- When `show()` is called with a pre-filled address, the input field displays that address
- Player can still edit or clear the pre-filled address
- Pressing backspace works normally to clear the field
- Empty string or null pre-fill results in empty input field (current behavior)

### 3. GameMessageHandler Integration

**Location**: `src/main/java/wagemaker/uk/gdx/GameMessageHandler.java`

**Changes**:
- Modify `handleConnectionAccepted()` to save the server address after successful connection
- Requires access to the server address that was used for the connection

**Implementation**:
```java
@Override
protected void handleConnectionAccepted(ConnectionAcceptedMessage message) {
    super.handleConnectionAccepted(message);
    
    // Set the client ID
    if (game.getGameClient() != null) {
        game.getGameClient().setClientId(message.getAssignedClientId());
    }
    
    // Save the server address for future connections
    String serverAddress = game.getLastConnectionAddress();
    if (serverAddress != null) {
        PlayerConfig config = PlayerConfig.load();
        config.saveLastServer(serverAddress);
    }
    
    System.out.println("Connected to server. Client ID: " + message.getAssignedClientId());
}
```

### 4. MyGdxGame Integration

**Location**: `src/main/java/wagemaker/uk/gdx/MyGdxGame.java`

**Changes**:
- Add `getLastConnectionAddress()` method to return the address used in the last connection attempt
- Modify the connection dialog opening logic to load and pre-fill the saved server address
- Store the full address string (including port) for the connection attempt

**Connection Dialog Opening**:
```java
// When opening connect dialog
PlayerConfig config = PlayerConfig.load();
String lastServer = config.getLastServer();
if (lastServer != null && !lastServer.isEmpty()) {
    gameMenu.getConnectDialog().setPrefilledAddress(lastServer);
}
gameMenu.getConnectDialog().show();
```

## Data Models

### PlayerConfig Properties File Format

```properties
# Woodlanders Player Configuration
# Last connected multiplayer server
multiplayer.last-server=192.168.1.100:25565
```

### PlayerConfig Class Structure

```java
public class PlayerConfig {
    private static final String CONFIG_FILE = "player.properties";
    private static final String KEY_LAST_SERVER = "multiplayer.last-server";
    
    private Properties properties;
    
    // Constructor, load, save methods
    public static PlayerConfig load() { ... }
    public void save() { ... }
    
    // Server address methods
    public String getLastServer() { ... }
    public void saveLastServer(String address) { ... }
}
```

## Error Handling

### File I/O Errors

**Scenario**: Config file cannot be read or written
- **Handling**: Log error to console, continue with default values
- **User Impact**: No saved server address, but application continues normally
- **Example**: Disk full, permission denied

### Invalid Server Address Format

**Scenario**: Saved server address is malformed
- **Handling**: Validation occurs in ConnectDialog (existing logic), invalid addresses are rejected on connection attempt
- **User Impact**: Connection fails with existing error dialog
- **Prevention**: No validation on save - trust the address that successfully connected

### Missing Config File

**Scenario**: First time running the game or config file deleted
- **Handling**: `PlayerConfig.load()` returns default config with null last server
- **User Impact**: Empty connection dialog (current behavior)
- **Recovery**: Automatic - file created on first successful connection

### Concurrent Access

**Scenario**: Multiple game instances running
- **Handling**: Last write wins (Properties file is not locked)
- **User Impact**: Minimal - each instance saves its own last connection
- **Mitigation**: Not required for single-player focused game

## Testing Strategy

### Unit Tests

1. **PlayerConfig Load/Save**
   - Test loading non-existent file returns default config
   - Test saving and loading server address
   - Test handling of I/O errors (mock file system)
   - Test empty/null server address handling

2. **ConnectDialog Pre-fill**
   - Test pre-filling with valid address
   - Test pre-filling with empty/null address
   - Test editing pre-filled address
   - Test clearing pre-filled address with backspace

### Integration Tests

1. **End-to-End Connection Flow**
   - Connect to server successfully
   - Verify config file created with correct address
   - Close and reopen connection dialog
   - Verify address is pre-filled
   - Connect to different server
   - Verify config updated with new address

2. **Error Scenarios**
   - Connection failure should not save address
   - Invalid address format should not crash on load
   - Missing config file should not prevent connection

### Manual Testing

1. **First Connection**
   - Open connection dialog (should be empty)
   - Enter server address and connect
   - Verify connection successful
   - Check player.properties file created

2. **Subsequent Connections**
   - Open connection dialog (should show last server)
   - Verify can edit the pre-filled address
   - Connect to same server (should work)
   - Connect to different server (should update config)

3. **Edge Cases**
   - Delete player.properties file while game running
   - Connect with localhost vs IP address
   - Connect with and without port number
   - Very long server addresses

## Implementation Notes

### Platform Compatibility

- Config file location uses working directory (platform-independent)
- Properties file format is cross-platform
- No platform-specific APIs required

### Performance Considerations

- Config file is small (< 1KB), I/O impact negligible
- Load config only when opening connection dialog (lazy loading)
- Save config asynchronously after connection (non-blocking)

### Future Enhancements

Potential future improvements (not in scope):
- Save multiple recent servers (history list)
- Save server nicknames/aliases
- Encrypt saved server addresses
- Cloud sync of player config
- Per-server player preferences

## Dependencies

### Existing Classes Used
- `java.util.Properties` - Configuration storage
- `java.io.FileInputStream/FileOutputStream` - File I/O
- `ConnectDialog` - UI component
- `GameMessageHandler` - Network message handling
- `MyGdxGame` - Main game class

### New Classes Created
- `PlayerConfig` - Configuration management

### No External Libraries Required
- Uses only Java standard library and existing game classes
