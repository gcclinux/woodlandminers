---
title: Configurable Planting Range System
status: draft
created: 2025-11-16
updated: 2025-11-16
---

# Configurable Planting Range System

## Overview

Implement a unified, configurable planting range system that synchronizes the client-side targeting range with server-side validation range. This ensures players cannot target locations that the server will reject, improving user experience and preventing wasted planting attempts.

## Problem Statement

Currently:
- Server-side validation range is hardcoded at 512 pixels in `ClientConnection.java`
- Client-side targeting system has no range limit (unlimited)
- Players can target and attempt to plant at locations the server will reject
- No way to configure the range without code changes
- Inconsistency between client expectations and server validation

This leads to:
- Confusing user experience (targeting shows valid, but planting fails)
- Wasted baby bamboo items when server rejects out-of-range plantings
- Need to rebuild and redeploy to adjust range settings

## Goals

1. **Centralized Configuration**: Single source of truth for planting range in `server.properties`
2. **Client-Server Sync**: Client targeting range matches server validation range
3. **Range Enforcement**: Prevent targeting cursor from moving beyond acceptable range
4. **User Feedback**: Clear visual indication when at maximum range
5. **Backward Compatibility**: Default values maintain current behavior

## Requirements

### 1. Server Configuration (server.properties)

**Requirement 1.1**: Add planting range configuration to `server.properties`

```properties
# Planting Range Configuration (in pixels)
# Maximum distance a player can plant from their position
# Default: 512 (8 tiles at 64px per tile)
# Range: 64-1024 (1-16 tiles)
planting.max.range=512
```

**Requirement 1.2**: Server must load and validate this configuration on startup

**Requirement 1.3**: Invalid values should fall back to default (512) with warning log

**Requirement 1.4**: Configuration should be hot-reloadable (optional enhancement)

### 2. Server-Side Validation Range

**Requirement 2.1**: Replace hardcoded 512 value in `ClientConnection.java` with config value

**Requirement 2.2**: Server must broadcast the planting range to clients on connection

**Requirement 2.3**: Server must log the active planting range on startup

**Requirement 2.4**: Range validation must use Euclidean distance (√(dx² + dy²))

### 3. Client-Side Targeting Range

**Requirement 3.1**: Client must receive planting range from server on connection

**Requirement 3.2**: `TargetingSystem` must enforce maximum range constraint

**Requirement 3.3**: Targeting cursor cannot move beyond the configured range

**Requirement 3.4**: Targeting cursor should "clamp" to maximum range circle

**Requirement 3.5**: Visual feedback when at maximum range (optional: red tint or indicator)

## Design

### Architecture

```
┌─────────────────────┐
│  server.properties  │
│  planting.max.range │
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│   ServerConfig      │
│  loadPlantingRange()│
└──────────┬──────────┘
           │
           ├──────────────────────┐
           ▼                      ▼
┌─────────────────────┐  ┌──────────────────────┐
│  ClientConnection   │  │  ConnectionAccepted  │
│  validatePlanting() │  │  Message             │
└─────────────────────┘  └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │   GameClient         │
                         │   setPlantingRange() │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │  TargetingSystem     │
                         │  enforceMaxRange()   │
                         └──────────────────────┘
```

### Data Flow

1. **Server Startup**:
   - Load `planting.max.range` from `server.properties`
   - Validate and store in `ServerConfig`
   - Log active range

2. **Client Connection**:
   - Server sends `ConnectionAcceptedMessage` with `plantingMaxRange` field
   - Client receives and stores range value
   - Client configures `TargetingSystem` with max range

3. **Planting Action**:
   - Client: User moves targeting cursor
   - Client: `TargetingSystem` clamps cursor to max range
   - Client: User confirms planting
   - Client: Sends `BambooPlantMessage` to server
   - Server: Validates distance ≤ configured range
   - Server: Accepts or rejects planting

### Component Changes

#### 1. ServerConfig (New or Enhanced)

```java
public class ServerConfig {
    private int plantingMaxRange = 512; // Default
    
    public void loadFromProperties(Properties props) {
        String rangeStr = props.getProperty("planting.max.range", "512");
        try {
            int range = Integer.parseInt(rangeStr);
            if (range < 64 || range > 1024) {
                System.err.println("Invalid planting.max.range: " + range + 
                                 ". Must be 64-1024. Using default: 512");
                plantingMaxRange = 512;
            } else {
                plantingMaxRange = range;
                System.out.println("Planting max range set to: " + range + 
                                 " pixels (" + (range/64) + " tiles)");
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid planting.max.range format. Using default: 512");
            plantingMaxRange = 512;
        }
    }
    
    public int getPlantingMaxRange() {
        return plantingMaxRange;
    }
}
```

#### 2. ConnectionAcceptedMessage (Enhanced)

```java
public class ConnectionAcceptedMessage extends NetworkMessage {
    private String clientId;
    private long worldSeed;
    private int plantingMaxRange; // NEW FIELD
    
    // Constructor, getters, setters...
}
```

#### 3. ClientConnection (Modified)

```java
private void handleBambooPlant(BambooPlantMessage message) {
    // ... existing validation ...
    
    // Use configured range instead of hardcoded 512
    int maxRange = server.getConfig().getPlantingMaxRange();
    
    if (distance > maxRange) {
        System.out.println("Bamboo plant out of range from " + clientId + 
                         ": distance=" + distance + ", max=" + maxRange);
        logSecurityViolation("Bamboo plant range check failed: distance=" + 
                           distance + ", max=" + maxRange);
        return;
    }
    
    // ... rest of validation ...
}
```

#### 4. TargetingSystem (Enhanced)

```java
public class TargetingSystem {
    private int maxRange = -1; // -1 = unlimited (default)
    private float playerX, playerY;
    private float targetX, targetY;
    
    /**
     * Set the maximum targeting range.
     * @param maxRange Maximum range in pixels, or -1 for unlimited
     */
    public void setMaxRange(int maxRange) {
        this.maxRange = maxRange;
        System.out.println("[TargetingSystem] Max range set to: " + maxRange + " pixels");
    }
    
    /**
     * Move target in the specified direction.
     * Enforces max range constraint if set.
     */
    public void moveTarget(Direction direction) {
        if (!isActive) return;
        
        float newX = targetX;
        float newY = targetY;
        
        // Calculate new position based on direction
        switch (direction) {
            case UP: newY += TILE_SIZE; break;
            case DOWN: newY -= TILE_SIZE; break;
            case LEFT: newX -= TILE_SIZE; break;
            case RIGHT: newX += TILE_SIZE; break;
        }
        
        // Enforce max range if set
        if (maxRange > 0) {
            float dx = newX - playerX;
            float dy = newY - playerY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            
            if (distance > maxRange) {
                // Clamp to max range circle
                float angle = (float) Math.atan2(dy, dx);
                newX = playerX + (float) Math.cos(angle) * maxRange;
                newY = playerY + (float) Math.sin(angle) * maxRange;
                
                // Snap to tile grid
                newX = snapToTileGrid(newX);
                newY = snapToTileGrid(newY);
                
                System.out.println("[TargetingSystem] Target clamped to max range");
            }
        }
        
        targetX = newX;
        targetY = newY;
        validateTarget();
    }
    
    /**
     * Check if current target is within max range.
     */
    public boolean isWithinMaxRange() {
        if (maxRange <= 0) return true; // No limit
        
        float dx = targetX - playerX;
        float dy = targetY - playerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        return distance <= maxRange;
    }
}
```

#### 5. GameClient (Enhanced)

```java
public class GameClient {
    private int plantingMaxRange = -1; // -1 = unlimited
    
    private void handleConnectionAccepted(ConnectionAcceptedMessage message) {
        this.clientId = message.getClientId();
        this.plantingMaxRange = message.getPlantingMaxRange();
        
        System.out.println("Connected to server. Client ID: " + clientId);
        System.out.println("Planting max range: " + plantingMaxRange + 
                         " pixels (" + (plantingMaxRange/64) + " tiles)");
        
        // Notify game to configure targeting system
        if (game != null) {
            game.setPlantingMaxRange(plantingMaxRange);
        }
    }
    
    public int getPlantingMaxRange() {
        return plantingMaxRange;
    }
}
```

#### 6. MyGdxGame (Enhanced)

```java
public void setPlantingMaxRange(int maxRange) {
    if (player != null && player.getTargetingSystem() != null) {
        player.getTargetingSystem().setMaxRange(maxRange);
        System.out.println("[MyGdxGame] Planting max range configured: " + maxRange);
    }
}
```

## Implementation Plan

### Phase 1: Server Configuration (Priority: High)

**Tasks:**
1. Add `planting.max.range` property to `server.properties`
2. Create or enhance `ServerConfig` class to load and validate range
3. Update `GameServer` to use `ServerConfig`
4. Replace hardcoded 512 in `ClientConnection.java` with config value
5. Add logging for active range on server startup

**Estimated Effort:** 2-3 hours

**Files to Modify:**
- `server.properties`
- `src/main/java/wagemaker/uk/network/GameServer.java`
- `src/main/java/wagemaker/uk/network/ClientConnection.java`
- Create: `src/main/java/wagemaker/uk/config/ServerConfig.java` (if doesn't exist)

### Phase 2: Client-Server Synchronization (Priority: High)

**Tasks:**
1. Add `plantingMaxRange` field to `ConnectionAcceptedMessage`
2. Update server to send range in connection accepted message
3. Update client to receive and store range
4. Add `setPlantingMaxRange()` method to `MyGdxGame`

**Estimated Effort:** 2-3 hours

**Files to Modify:**
- `src/main/java/wagemaker/uk/network/ConnectionAcceptedMessage.java`
- `src/main/java/wagemaker/uk/network/GameServer.java`
- `src/main/java/wagemaker/uk/network/GameClient.java`
- `src/main/java/wagemaker/uk/gdx/MyGdxGame.java`

### Phase 3: Targeting Range Enforcement (Priority: Medium)

**Tasks:**
1. Add `maxRange` field to `TargetingSystem`
2. Add `setMaxRange()` method
3. Implement range clamping in `moveTarget()`
4. Add `isWithinMaxRange()` validation method
5. Wire up client to configure targeting system on connection

**Estimated Effort:** 3-4 hours

**Files to Modify:**
- `src/main/java/wagemaker/uk/targeting/TargetingSystem.java`
- `src/main/java/wagemaker/uk/player/Player.java`

### Phase 4: Visual Feedback (Priority: Low - Optional)

**Tasks:**
1. Add visual indicator when at maximum range
2. Change cursor color/tint when clamped
3. Add UI text showing current range

**Estimated Effort:** 2-3 hours

**Files to Modify:**
- `src/main/java/wagemaker/uk/targeting/TargetIndicatorRenderer.java`
- `src/main/java/wagemaker/uk/ui/` (various UI components)

## Testing Plan

### Unit Tests

1. **ServerConfig Range Validation**
   - Test valid range (64-1024)
   - Test invalid range (< 64, > 1024)
   - Test invalid format (non-numeric)
   - Test missing property (default to 512)

2. **TargetingSystem Range Enforcement**
   - Test movement within range
   - Test movement beyond range (should clamp)
   - Test diagonal movement clamping
   - Test unlimited range (-1)

### Integration Tests

1. **Client-Server Sync**
   - Server sends range to client on connection
   - Client configures targeting system correctly
   - Multiple clients receive same range

2. **Planting Validation**
   - Plant within range (should succeed)
   - Plant at max range (should succeed)
   - Plant beyond range (should fail on server)
   - Targeting cursor cannot move beyond range

### Manual Testing

1. **Configuration Testing**
   - Set range to 256 (4 tiles) - verify targeting limited
   - Set range to 512 (8 tiles) - verify targeting limited
   - Set range to 1024 (16 tiles) - verify targeting limited
   - Set invalid range - verify fallback to 512

2. **Gameplay Testing**
   - Plant bamboo at various distances
   - Verify cursor stops at max range
   - Verify server accepts all valid plantings
   - Verify server rejects out-of-range plantings

3. **Multiplayer Testing**
   - Two players with different network latencies
   - Rapid planting sequences
   - Verify no rejections within configured range

## Configuration Examples

### Conservative (Close Range)
```properties
# Good for high-latency networks or strict gameplay
planting.max.range=256
```

### Balanced (Default)
```properties
# Good balance between usability and security
planting.max.range=512
```

### Permissive (Long Range)
```properties
# Good for low-latency networks or relaxed gameplay
planting.max.range=1024
```

## Security Considerations

1. **Range Limits**: Min 64 (1 tile), Max 1024 (16 tiles) prevents abuse
2. **Server Authority**: Server always validates, client enforcement is UX only
3. **Logging**: All out-of-range attempts logged for monitoring
4. **Backward Compatibility**: Default 512 maintains current behavior

## Performance Impact

- **Server**: Negligible (one config load, one comparison per planting)
- **Client**: Negligible (one distance calculation per cursor move)
- **Network**: +4 bytes per connection (int32 for range)

## Future Enhancements

1. **Per-Player Ranges**: Different ranges based on player level/permissions
2. **Item-Specific Ranges**: Different ranges for different plantable items
3. **Dynamic Ranges**: Adjust range based on network latency
4. **Admin Commands**: Change range without restart (`/setrange 512`)
5. **Client-Side Prediction**: Show estimated server validation result

## Success Criteria

✅ Server loads planting range from `server.properties`
✅ Client receives range from server on connection
✅ Targeting cursor cannot move beyond configured range
✅ Server validates plantings using configured range
✅ No planting rejections within configured range
✅ Clear logging of active range
✅ Backward compatible (defaults to 512)

## References

- Current implementation: `ClientConnection.java` line 1069
- Targeting system: `src/main/java/wagemaker/uk/targeting/TargetingSystem.java`
- Server config: `server.properties`
- Related issue: Baby bamboo multiplayer synchronization fix

## Notes

- This spec addresses the root cause of the bamboo synchronization issue
- Improves user experience by preventing invalid planting attempts
- Provides server operators control over gameplay mechanics
- Foundation for future range-based features
