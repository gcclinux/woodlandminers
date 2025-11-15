# Resource Respawn System

This package contains the resource respawn system for Woodlanders, which manages the automatic respawning of destroyed resources (trees and stones) after a configured duration.

## Configuration

Resource respawn behavior is configured with **hardcoded values** in the `RespawnConfig.java` class. All configuration constants are defined at the top of the class.

### Default Configuration

- **Default respawn duration**: 15 minutes (900,000 ms)
- **Visual indicator threshold**: 1 minute (60,000 ms) before respawn
- **Visual indicators**: Enabled by default

### Customizing Respawn Durations

To customize respawn behavior:

1. Open `src/main/java/wagemaker/uk/respawn/RespawnConfig.java`
2. Modify the constant values in the "RESPAWN DURATION CONFIGURATION" section
3. Recompile the game: `./gradlew build`
4. Run the updated game

### Time Conversions

All durations are in milliseconds:

| Duration | Milliseconds |
|----------|--------------|
| 1 second | 1,000 |
| 1 minute | 60,000 |
| 5 minutes | 300,000 |
| 10 minutes | 600,000 |
| 15 minutes | 900,000 (default) |
| 30 minutes | 1,800,000 |
| 1 hour | 3,600,000 |

### Example Customizations

#### Fast Respawn (5 minutes for all resources)
```java
private static final long DEFAULT_RESPAWN_DURATION = 300000;
```

#### Quick Bamboo Respawn (2 minutes)
```java
private static final long BAMBOO_TREE_RESPAWN_DURATION = 120000;
```

#### Show Indicator 2 Minutes Before Respawn
```java
private static final long VISUAL_INDICATOR_THRESHOLD = 120000;
```

#### Disable Visual Indicators
```java
private static final boolean VISUAL_INDICATOR_ENABLED = false;
```

#### Mixed Respawn Rates
```java
private static final long DEFAULT_RESPAWN_DURATION = 900000;  // 15 minutes
private static final long BAMBOO_TREE_RESPAWN_DURATION = 300000;  // 5 minutes
private static final long APPLE_TREE_RESPAWN_DURATION = 1200000;  // 20 minutes
private static final long STONE_RESPAWN_DURATION = 600000;  // 10 minutes
```

## Classes

### RespawnConfig
Manages configuration for respawn timers with hardcoded default values. Provides methods to get respawn durations for different resource types.

### RespawnManager
Manages active respawn timers for destroyed resources. Tracks when resources should respawn and handles the respawn logic.

### RespawnEntry
Represents a single resource waiting to respawn. Contains information about the resource type, location, and remaining time.

### RespawnIndicator
Renders visual indicators at resource locations when they are about to respawn (within the configured threshold).

### ResourceType
Enum defining the types of resources that can respawn (TREE, STONE).

## Usage

The respawn system is automatically initialized when the game starts:

```java
// In game initialization
RespawnConfig config = RespawnConfig.load();
RespawnManager respawnManager = new RespawnManager(config);

// When a resource is destroyed
respawnManager.scheduleRespawn(resourceType, treeType, x, y);

// In game update loop
respawnManager.update(deltaTime);
```

## Multiplayer

In multiplayer mode, only the server manages respawn timers. The server synchronizes respawn events to all connected clients using the `ResourceRespawnMessage`.

## Persistence

Respawn timers are saved and loaded with the world state, ensuring that resources continue their respawn countdown even after the game is closed and reopened.
