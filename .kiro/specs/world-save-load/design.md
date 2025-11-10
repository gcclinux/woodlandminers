# World Save/Load System Design

## Overview

The World Save/Load System extends the existing game architecture to provide complete world state persistence. It builds upon the current player position save system and WorldState class to enable saving and loading of entire game worlds including trees, items, cleared areas, and world seeds.

The system integrates seamlessly with the existing GameMenu and maintains compatibility with both singleplayer and multiplayer modes while respecting the current threading model and OpenGL constraints.

## Architecture

### High-Level Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   GameMenu      │    │ WorldSaveManager │    │ WorldSaveData   │
│                 │    │                  │    │                 │
│ - Save World    │───▶│ - saveWorld()    │───▶│ - worldSeed     │
│ - Load World    │    │ - loadWorld()    │    │ - trees         │
│ - Manage Saves  │    │ - listSaves()    │    │ - items         │
└─────────────────┘    │ - deleteSave()   │    │ - clearedPos    │
                       └──────────────────┘    │ - timestamp     │
                                ▲              └─────────────────┘
                                │
                       ┌──────────────────┐
                       │   WorldState     │
                       │                  │
                       │ - Current game   │
                       │   state source   │
                       └──────────────────┘
```

### Component Integration

The system integrates with existing components:

- **GameMenu**: Extended with new menu options for world save/load operations
- **WorldState**: Used as the primary data source for save operations and target for load operations  
- **MyGdxGame**: Modified to support world state restoration and maintain threading safety
- **File System**: Leverages existing config directory structure for save file storage

## Components and Interfaces

### WorldSaveManager

The core component responsible for all save/load operations:

```java
public class WorldSaveManager {
    private static final String WORLD_SAVES_DIR = "world-saves";
    private static final String SAVE_FILE_EXTENSION = ".wld";
    
    // Core operations
    public boolean saveWorld(String saveName, WorldState worldState, float playerX, float playerY, float playerHealth)
    public WorldSaveData loadWorld(String saveName)
    public List<WorldSaveInfo> listAvailableSaves()
    public boolean deleteSave(String saveName)
    
    // Utility methods
    private File getSaveDirectory(boolean isMultiplayer)
    private File getSaveFile(String saveName, boolean isMultiplayer)
    private boolean isValidSaveName(String saveName)
}
```

### WorldSaveData

Data structure for serialized world state:

```java
public class WorldSaveData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private long worldSeed;
    private Map<String, TreeState> trees;
    private Map<String, ItemState> items;
    private Set<String> clearedPositions;
    private List<RainZone> rainZones;
    private float playerX;
    private float playerY;
    private float playerHealth;
    private long saveTimestamp;
    private String saveName;
    private String gameMode; // "singleplayer" or "multiplayer"
}
```

### WorldSaveInfo

Metadata for save file listing:

```java
public class WorldSaveInfo {
    private String saveName;
    private long saveTimestamp;
    private String gameMode;
    private long worldSeed;
    private int treeCount;
    private int itemCount;
}
```

### Enhanced GameMenu

Extended menu system with world save/load options:

```java
// New menu items added to existing arrays
private String[] singleplayerMenuItems = {
    "Player Name", "Save World", "Load World", "Multiplayer", "Save Position", "Exit"
};

private String[] multiplayerMenuItems = {
    "Player Name", "Save World", "Load World", "Save Position", "Disconnect", "Exit"
};

// New dialog components
private WorldSaveDialog worldSaveDialog;
private WorldLoadDialog worldLoadDialog;
private WorldManageDialog worldManageDialog;
```

## Data Models

### Save File Structure

World saves are stored as serialized Java objects in the following directory structure:

```
~/.config/woodlanders/
├── woodlanders.json          # Existing player config
└── world-saves/
    ├── singleplayer/
    │   ├── my-world.wld
    │   ├── test-world.wld
    │   └── backup-world.wld
    └── multiplayer/
        ├── server-world-1.wld
        └── server-world-2.wld
```

### Save File Format

Each `.wld` file contains a serialized `WorldSaveData` object with:

- **Header**: Save format version, game mode, timestamp
- **World Data**: Seed, trees, items, cleared positions, rain zones
- **Player Data**: Position and health at save time
- **Metadata**: Save name, creation date, statistics

### Compatibility

The system maintains backward compatibility by:
- Preserving existing player position save functionality
- Using separate directories for world saves vs player config
- Supporting both singleplayer and multiplayer modes
- Graceful handling of missing or corrupted save files

## Error Handling

### Save Operation Errors

1. **Insufficient Disk Space**: Display error dialog with disk space information
2. **Permission Denied**: Show error with instructions for file permissions
3. **Invalid Save Name**: Validate names and show specific error messages
4. **Serialization Failure**: Log detailed error and show generic user message

### Load Operation Errors

1. **Save File Not Found**: Display available saves and suggest alternatives
2. **Corrupted Save File**: Offer to delete corrupted file or restore from backup
3. **Version Incompatibility**: Show upgrade/downgrade instructions
4. **Missing Dependencies**: Check for required game components

### Error Recovery

- **Automatic Backups**: Create backup copies before overwriting saves
- **Validation**: Verify save file integrity before loading
- **Rollback**: Restore previous state if load operation fails
- **User Feedback**: Clear error messages with actionable solutions

## Testing Strategy

### Unit Tests

1. **WorldSaveManager Tests**
   - Save/load operations with various world states
   - File system error handling
   - Save name validation
   - Directory creation and permissions

2. **WorldSaveData Tests**
   - Serialization/deserialization integrity
   - Large world state handling
   - Backward compatibility with older save formats

3. **GameMenu Integration Tests**
   - Menu navigation and dialog interactions
   - Save/load workflow validation
   - Error dialog display and handling

### Integration Tests

1. **End-to-End Save/Load Tests**
   - Complete world save and restore cycles
   - Player position preservation
   - World seed consistency verification
   - Tree and item state accuracy

2. **Multiplayer Compatibility Tests**
   - Host-only save functionality
   - Client mode restrictions
   - World state synchronization after load

3. **File System Tests**
   - Cross-platform directory handling
   - Large save file performance
   - Concurrent access scenarios

### Performance Tests

1. **Large World Handling**
   - Save/load times for worlds with 10,000+ trees
   - Memory usage during serialization
   - File size optimization

2. **UI Responsiveness**
   - Menu performance with many save files
   - Background save operations
   - Progress indication for long operations

### Manual Testing Scenarios

1. **User Workflow Testing**
   - Create multiple saves with different world states
   - Load saves and verify complete restoration
   - Delete saves and confirm cleanup

2. **Error Condition Testing**
   - Fill disk space during save operation
   - Corrupt save files manually
   - Test with read-only directories

3. **Cross-Platform Testing**
   - Verify save file portability between OS
   - Test directory path handling
   - Validate file permissions on different systems

## Implementation Notes

### Threading Considerations

The system respects the existing threading model:
- Save operations execute on the render thread to safely access WorldState
- File I/O operations are performed synchronously to ensure data integrity
- UI updates are deferred to the render thread using the existing deferred operation pattern

### Memory Management

- WorldSaveData objects are created temporarily and disposed after use
- Large world states are streamed during serialization to minimize memory usage
- Save file validation is performed without loading entire files into memory

### Security Considerations

- Save file names are validated to prevent directory traversal attacks
- File permissions are set to user-only access
- Save files are validated before deserialization to prevent malicious code execution