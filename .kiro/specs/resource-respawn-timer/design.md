# Design Document: Resource Respawn Timer System

## Overview

This design implements a respawn timer system for destructible resources (trees and rocks) in the Woodlanders game. When a resource is destroyed, it is removed from the world and tracked by a respawn system that recreates it after a configurable timer (default 15 minutes). The system must work seamlessly in both single-player and multiplayer modes, with proper state persistence and network synchronization.

## Architecture

### High-Level Architecture

The respawn system follows a centralized authority pattern:

- **Single-Player Mode**: The local game instance acts as the authority
- **Multiplayer Mode**: The GameServer acts as the authority and broadcasts respawn events to all clients

```
┌─────────────────────────────────────────────────────────┐
│                   Respawn System                        │
│  ┌───────────────────────────────────────────────────┐ │
│  │         RespawnManager (Core Logic)               │ │
│  │  - Tracks pending respawns                        │ │
│  │  - Updates timers                                 │ │
│  │  - Triggers respawn events                        │ │
│  └───────────────────────────────────────────────────┘ │
│                          │                              │
│         ┌────────────────┼────────────────┐            │
│         │                │                │            │
│  ┌──────▼──────┐  ┌──────▼──────┐  ┌─────▼──────┐    │
│  │  Respawn    │  │   Respawn   │  │  Respawn   │    │
│  │   Config    │  │   Tracker   │  │  Executor  │    │
│  └─────────────┘  └─────────────┘  └────────────┘    │
└─────────────────────────────────────────────────────────┘
         │                    │                  │
         │                    │                  │
    ┌────▼────┐      ┌────────▼────────┐   ┌────▼────┐
    │ Config  │      │  WorldSaveData  │   │ MyGdxGame│
    │  File   │      │  (Persistence)  │   │ (World)  │
    └─────────┘      └─────────────────┘   └──────────┘
```

### Threading Considerations

Following the existing game's threading model:

- **Render Thread**: Handles all OpenGL operations (texture creation/disposal)
- **Network Thread**: Processes network messages
- **Respawn Timer Thread**: Updates respawn timers (uses deferred operations for resource creation)

All resource creation/disposal operations must use the `deferOperation()` pattern to ensure thread safety.

## Components and Interfaces

### 1. RespawnEntry

Represents a single pending respawn with all necessary data.

```java
public class RespawnEntry implements Serializable {
    private String resourceId;
    private ResourceType resourceType;
    private float x;
    private float y;
    private long destructionTimestamp;
    private long respawnDuration; // milliseconds
    private TreeType treeType; // null for stones
    
    // Getters, setters, and helper methods
    public long getRemainingTime();
    public boolean isReadyToRespawn();
}
```

### 2. ResourceType Enum

```java
public enum ResourceType {
    TREE,
    STONE
}
```

### 3. RespawnConfig

Configuration for respawn timers, loaded from properties file.

```java
public class RespawnConfig {
    private long defaultRespawnDuration; // milliseconds
    private Map<TreeType, Long> treeRespawnDurations;
    private long stoneRespawnDuration;
    private boolean visualIndicatorEnabled;
    private long visualIndicatorThreshold; // show indicator when < this time remaining
    
    // Load from file or use defaults
    public static RespawnConfig load();
    public static RespawnConfig getDefault();
}
```

### 4. RespawnManager

Core component that manages all respawn logic.

```java
public class RespawnManager {
    private Map<String, RespawnEntry> pendingRespawns;
    private RespawnConfig config;
    private MyGdxGame game; // reference to game instance
    private boolean isServer; // true if running as server or single-player
    
    // Core methods
    public void registerDestruction(String resourceId, ResourceType type, 
                                   float x, float y, TreeType treeType);
    public void update(float deltaTime);
    public void loadFromSaveData(List<RespawnEntry> entries);
    public List<RespawnEntry> getSaveData();
    
    // Respawn execution
    private void executeRespawn(RespawnEntry entry);
    private void createTree(String treeId, TreeType type, float x, float y);
    private void createStone(String stoneId, float x, float y);
    
    // Network synchronization (multiplayer)
    public void handleRespawnMessage(ResourceRespawnMessage message);
    public void sendRespawnState(ClientConnection client);
}
```

### 5. Network Messages

New network messages for respawn synchronization.

```java
// Sent when a resource respawns
public class ResourceRespawnMessage extends NetworkMessage {
    private String resourceId;
    private ResourceType resourceType;
    private TreeType treeType; // null for stones
    private float x;
    private float y;
}

// Sent to new clients joining multiplayer
public class RespawnStateMessage extends NetworkMessage {
    private List<RespawnEntry> pendingRespawns;
}
```

### 6. WorldSaveData Extension

Extend existing WorldSaveData to include respawn timer data.

```java
// Add to WorldSaveData class:
private List<RespawnEntry> pendingRespawns;

public List<RespawnEntry> getPendingRespawns() {
    return pendingRespawns;
}

public void setPendingRespawns(List<RespawnEntry> pendingRespawns) {
    this.pendingRespawns = pendingRespawns;
}
```

## Data Models

### Respawn Entry Data Structure

```
RespawnEntry {
    resourceId: String          // Unique identifier (e.g., "tree_123", "stone_456")
    resourceType: ResourceType  // TREE or STONE
    x: float                    // World X coordinate
    y: float                    // World Y coordinate
    destructionTimestamp: long  // System.currentTimeMillis() when destroyed
    respawnDuration: long       // Duration in milliseconds (e.g., 900000 for 15 min)
    treeType: TreeType         // Type of tree (null for stones)
}
```

### Configuration File Format

`respawn-config.properties`:

```properties
# Default respawn duration (milliseconds)
default.respawn.duration=900000

# Tree-specific durations (optional overrides)
tree.apple.respawn.duration=900000
tree.banana.respawn.duration=900000
tree.coconut.respawn.duration=900000
tree.bamboo.respawn.duration=900000
tree.small.respawn.duration=900000
tree.cactus.respawn.duration=900000

# Stone respawn duration
stone.respawn.duration=900000

# Visual indicator settings
visual.indicator.enabled=true
visual.indicator.threshold=60000
```

## Integration Points

### 1. MyGdxGame Integration

Modify the existing tree and stone destruction handling:

```java
// In MyGdxGame class
private RespawnManager respawnManager;

// Initialize in create()
respawnManager = new RespawnManager(this, isServer());

// When a tree is destroyed
if (tree.attack()) {
    String treeId = getTreeId(tree);
    TreeType treeType = getTreeType(tree);
    
    // Register for respawn
    respawnManager.registerDestruction(treeId, ResourceType.TREE, 
                                      tree.getX(), tree.getY(), treeType);
    
    // Existing destruction logic...
    trees.remove(treeId);
    deferOperation(() -> tree.dispose());
    
    // Broadcast in multiplayer
    if (isMultiplayer && isServer) {
        broadcastTreeDestroyed(treeId, tree.getX(), tree.getY());
    }
}

// Update respawn manager in render loop
respawnManager.update(deltaTime);
```

### 2. WorldSaveManager Integration

Extend save/load operations to include respawn data:

```java
// In WorldSaveManager.saveWorld()
List<RespawnEntry> respawnData = respawnManager.getSaveData();
worldSaveData.setPendingRespawns(respawnData);

// In WorldSaveManager.loadWorld()
List<RespawnEntry> respawnData = worldSaveData.getPendingRespawns();
if (respawnData != null) {
    respawnManager.loadFromSaveData(respawnData);
}
```

### 3. GameServer Integration

Add respawn state synchronization for multiplayer:

```java
// In GameServer.handleClientJoin()
RespawnStateMessage respawnState = new RespawnStateMessage(
    respawnManager.getSaveData()
);
client.send(respawnState);

// In GameServer message handling
case RESOURCE_RESPAWN:
    // Broadcast respawn to all clients
    broadcastToAllClients(message);
    break;
```

### 4. GameClient Integration

Handle respawn messages from server:

```java
// In GameClient message handler
case RESOURCE_RESPAWN:
    ResourceRespawnMessage respawnMsg = (ResourceRespawnMessage) message;
    respawnManager.handleRespawnMessage(respawnMsg);
    break;

case RESPAWN_STATE:
    RespawnStateMessage stateMsg = (RespawnStateMessage) message;
    respawnManager.loadFromSaveData(stateMsg.getPendingRespawns());
    break;
```

## Error Handling

### Configuration Errors

- **Missing config file**: Use default values (15 minutes for all resources)
- **Invalid duration values**: Log warning and use default value
- **Negative durations**: Validate and reject, use default value

### Save/Load Errors

- **Corrupted respawn data**: Log error and skip corrupted entries
- **Missing respawn data in save file**: Initialize empty respawn manager
- **Version mismatch**: Attempt to migrate or discard incompatible data

### Network Errors

- **Respawn message lost**: Server maintains authority; clients will sync on next state update
- **Client desync**: Full respawn state sent on client join
- **Server crash**: Respawn timers lost unless world is saved; acceptable trade-off

### Resource Creation Errors

- **Texture loading failure**: Log error, mark respawn as failed, retry on next cycle
- **Position conflict**: Check if position is occupied, offset slightly if needed
- **Memory constraints**: Limit maximum pending respawns (e.g., 10,000 entries)

## Testing Strategy

### Unit Tests

1. **RespawnEntry Tests**
   - Test remaining time calculation
   - Test ready-to-respawn logic
   - Test serialization/deserialization

2. **RespawnConfig Tests**
   - Test default configuration loading
   - Test custom configuration parsing
   - Test validation of invalid values

3. **RespawnManager Tests**
   - Test registration of destroyed resources
   - Test timer updates and respawn triggering
   - Test save/load data conversion

### Integration Tests

1. **Single-Player Persistence**
   - Destroy resources, save game, load game
   - Verify timers resume correctly
   - Verify resources respawn after timer expires

2. **Multiplayer Synchronization**
   - Start server, connect two clients
   - Destroy resource on client A
   - Verify client B sees destruction
   - Wait for respawn, verify both clients see respawn

3. **Client Join During Respawn**
   - Start server, destroy resources
   - Connect new client mid-respawn
   - Verify client receives pending respawn state
   - Verify client sees respawns when timers expire

### Manual Testing

1. **Visual Indicator Test**
   - Destroy resource, wait until < 1 minute remaining
   - Verify visual indicator appears
   - Verify indicator disappears on respawn

2. **Performance Test**
   - Destroy 100+ resources
   - Monitor frame rate and memory usage
   - Verify no performance degradation

3. **Edge Cases**
   - Destroy resource, immediately save and quit
   - Load game after 15+ minutes real time
   - Verify resource respawns immediately on load

## Performance Considerations

### Memory Usage

- Each RespawnEntry: ~100 bytes
- 1,000 pending respawns: ~100 KB
- Acceptable overhead for typical gameplay

### CPU Usage

- Update loop: O(n) where n = pending respawns
- Optimization: Use priority queue sorted by respawn time
- Only check entries near respawn time each frame

### Network Bandwidth

- ResourceRespawnMessage: ~50 bytes
- Respawn events are infrequent (every 15+ minutes per resource)
- Negligible network impact

## Visual Indicator Design

### Indicator Appearance

- Particle effect or pulsing sprite at resource location
- Color: Green/yellow to indicate imminent respawn
- Size: 32x32 pixels, centered on resource spawn point

### Rendering

- Render in world space, behind player but in front of terrain
- Fade in over 1 second when threshold reached
- Fade out over 0.5 seconds when resource respawns

### Implementation

```java
public class RespawnIndicator {
    private float x, y;
    private Texture texture;
    private float alpha;
    private float animationTime;
    
    public void render(SpriteBatch batch, float deltaTime);
    public void dispose();
}
```

## Configuration Management

### Default Configuration

All resources respawn after 15 minutes (900,000 milliseconds) by default.

### Custom Configuration

Players can create `respawn-config.properties` in the game's config directory to customize:

- Per-resource-type respawn durations
- Visual indicator settings
- Enable/disable respawn system entirely

### Configuration Location

- Windows: `%APPDATA%/Woodlanders/respawn-config.properties`
- macOS: `~/Library/Application Support/Woodlanders/respawn-config.properties`
- Linux: `~/.config/woodlanders/respawn-config.properties`

## Future Enhancements

### Phase 2 Considerations (Out of Scope)

1. **Biome-Specific Respawn Rates**: Different durations based on biome type
2. **Player Proximity Detection**: Prevent respawn if player is too close
3. **Resource Scarcity System**: Limit total resources in world, respawn only when below threshold
4. **Admin Commands**: Server commands to force respawn or clear respawn queue
5. **Respawn Notifications**: Chat message or UI notification when resource respawns nearby

## Migration Strategy

### Backward Compatibility

- Old save files without respawn data: Initialize empty respawn manager
- New save files loaded in old version: Respawn data ignored (graceful degradation)
- Increment WorldSaveData serialVersionUID to handle version differences

### Deployment

1. Add respawn system code
2. Update WorldSaveData with new fields
3. Test with existing save files
4. Release with clear documentation about new feature
5. Existing worlds continue working, respawn system activates for newly destroyed resources
