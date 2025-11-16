# Design Document

## Overview

The Configurable Planting Range System provides a unified, configurable mechanism for controlling the maximum distance at which players can plant items. The system synchronizes client-side targeting constraints with server-side validation to prevent rejected planting attempts and improve user experience. Configuration is centralized in server.properties, allowing server operators to adjust gameplay mechanics without code changes.

### Key Design Principles

1. **Server Authority**: The server is the authoritative source for range configuration and validation
2. **Client Prediction**: The client enforces range limits locally to provide immediate feedback
3. **Fail-Safe Defaults**: Invalid or missing configuration falls back to safe default values
4. **Backward Compatibility**: The system maintains compatibility with existing game behavior
5. **Minimal Network Overhead**: Range synchronization adds only 4 bytes per connection

## Architecture

### System Components

```
┌─────────────────────────────────────────────────────────────┐
│                     Configuration Layer                      │
├─────────────────────────────────────────────────────────────┤
│  server.properties                                           │
│  └─ planting.max.range=512                                  │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                      Server Layer                            │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────────────┐         ┌──────────────────────┐     │
│  │  ServerConfig    │         │  GameServer          │     │
│  │  - maxRange      │◄────────│  - config            │     │
│  │  + load()        │         │  + start()           │     │
│  │  + validate()    │         └──────────────────────┘     │
│  └────────┬─────────┘                                       │
│           │                                                  │
│           ▼                                                  │
│  ┌──────────────────────────────────────────────────┐      │
│  │  ClientConnection                                 │      │
│  │  + handleBambooPlant(message)                    │      │
│  │  + validatePlantingRange(distance)               │      │
│  └──────────────────────────────────────────────────┘      │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ ConnectionAcceptedMessage
                     │ { plantingMaxRange: int }
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                      Client Layer                            │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────────────┐         ┌──────────────────────┐     │
│  │  GameClient      │         │  MyGdxGame           │     │
│  │  - maxRange      │────────►│  + setMaxRange()     │     │
│  │  + onConnected() │         └──────────┬───────────┘     │
│  └──────────────────┘                    │                  │
│                                           ▼                  │
│                              ┌──────────────────────┐       │
│                              │  Player              │       │
│                              │  - targetingSystem   │       │
│                              └──────────┬───────────┘       │
│                                         │                    │
│                                         ▼                    │
│                              ┌──────────────────────┐       │
│                              │  TargetingSystem     │       │
│                              │  - maxRange          │       │
│                              │  + moveTarget()      │       │
│                              │  + clampToRange()    │       │
│                              └──────────────────────┘       │
└─────────────────────────────────────────────────────────────┘
```

### Data Flow

#### Startup and Connection Flow

```
1. Server Startup
   ├─ Load server.properties
   ├─ Parse planting.max.range
   ├─ Validate range (64-1024)
   ├─ Store in ServerConfig
   └─ Log active configuration

2. Client Connection
   ├─ Client sends connection request
   ├─ Server accepts connection
   ├─ Server creates ConnectionAcceptedMessage
   │  └─ Include plantingMaxRange field
   ├─ Client receives message
   ├─ Client stores maxRange
   ├─ Client configures TargetingSystem
   └─ Client logs received configuration

3. Planting Action
   ├─ Player activates targeting mode
   ├─ Player moves cursor (arrow keys)
   │  ├─ TargetingSystem calculates new position
   │  ├─ TargetingSystem checks distance
   │  ├─ If > maxRange: clamp to boundary
   │  └─ Update cursor position
   ├─ Player confirms planting (spacebar)
   ├─ Client sends BambooPlantMessage
   ├─ Server receives message
   ├─ Server validates distance
   │  ├─ If > maxRange: reject and log
   │  └─ If ≤ maxRange: process planting
   └─ Server broadcasts result
```

## Components and Interfaces

### 1. ServerConfig

**Purpose**: Centralized server configuration management

**Location**: `src/main/java/wagemaker/uk/server/ServerConfig.java` (existing file to be enhanced)

**Responsibilities**:
- Load configuration from server.properties
- Validate planting range values
- Provide configuration access to server components
- Log configuration status

**Interface**:
```java
public class ServerConfig {
    // Constants
    private static final int DEFAULT_PLANTING_RANGE = 512;
    private static final int MIN_PLANTING_RANGE = 64;
    private static final int MAX_PLANTING_RANGE = 1024;
    
    // Fields
    private int plantingMaxRange;
    
    // Methods
    public void loadFromProperties(Properties props);
    public int getPlantingMaxRange();
    private void validateAndSetPlantingRange(String value);
    private void logPlantingRangeConfig();
}
```

**Key Algorithms**:
- Range validation with bounds checking
- Fallback to default on invalid input
- Conversion from pixels to tiles for logging

### 2. ConnectionAcceptedMessage

**Purpose**: Network message to transmit server configuration to clients

**Location**: `src/main/java/wagemaker/uk/network/ConnectionAcceptedMessage.java` (existing file to be enhanced)

**Responsibilities**:
- Carry planting range configuration from server to client
- Maintain backward compatibility
- Serialize/deserialize range value

**Interface**:
```java
public class ConnectionAcceptedMessage extends NetworkMessage {
    private String clientId;
    private long worldSeed;
    private int plantingMaxRange;  // NEW FIELD
    
    public ConnectionAcceptedMessage(String clientId, long worldSeed, int plantingMaxRange);
    public int getPlantingMaxRange();
    public void setPlantingMaxRange(int range);
}
```

**Serialization Format**:
```
[MessageType][ClientId][WorldSeed][PlantingMaxRange]
    1 byte    variable    8 bytes      4 bytes
```

### 3. ClientConnection

**Purpose**: Server-side client connection handler with planting validation

**Location**: `src/main/java/wagemaker/uk/network/ClientConnection.java` (existing file to be modified)

**Responsibilities**:
- Validate planting requests against configured range
- Log security violations
- Use ServerConfig for range values

**Modified Methods**:
```java
private void handleBambooPlant(BambooPlantMessage message) {
    // Calculate distance
    float dx = message.getX() - playerState.getX();
    float dy = message.getY() - playerState.getY();
    double distance = Math.sqrt(dx * dx + dy * dy);
    
    // Get configured range (replaces hardcoded 512)
    int maxRange = server.getConfig().getPlantingMaxRange();
    
    // Validate
    if (distance > maxRange) {
        logRangeViolation(distance, maxRange);
        return;
    }
    
    // Continue with planting logic...
}
```

### 4. GameClient

**Purpose**: Client-side server connection manager

**Location**: `src/main/java/wagemaker/uk/network/GameClient.java` (existing file to be enhanced)

**Responsibilities**:
- Receive planting range from server
- Store range for session duration
- Configure game components with range

**Interface**:
```java
public class GameClient {
    private int plantingMaxRange = -1;  // -1 = unlimited (not connected)
    
    private void handleConnectionAccepted(ConnectionAcceptedMessage message);
    public int getPlantingMaxRange();
}
```

### 5. TargetingSystem

**Purpose**: Client-side targeting cursor management with range enforcement

**Location**: `src/main/java/wagemaker/uk/targeting/TargetingSystem.java` (existing file to be enhanced)

**Responsibilities**:
- Enforce maximum targeting range
- Clamp cursor to range boundary
- Provide visual feedback
- Validate target positions

**Interface**:
```java
public class TargetingSystem {
    private int maxRange = -1;  // -1 = unlimited
    private float playerX, playerY;
    private float targetX, targetY;
    
    public void setMaxRange(int maxRange);
    public void moveTarget(Direction direction);
    public boolean isWithinMaxRange();
    private void clampToMaxRange();
    private float snapToTileGrid(float coordinate);
}
```

**Key Algorithms**:

*Range Clamping Algorithm*:
```
Input: proposed target position (newX, newY), player position (playerX, playerY), maxRange
Output: clamped target position

1. Calculate delta: dx = newX - playerX, dy = newY - playerY
2. Calculate distance: d = √(dx² + dy²)
3. If d ≤ maxRange:
   - Return (newX, newY)
4. Else:
   - Calculate angle: θ = atan2(dy, dx)
   - Calculate clamped position:
     - clampedX = playerX + cos(θ) × maxRange
     - clampedY = playerY + sin(θ) × maxRange
   - Snap to tile grid:
     - finalX = round(clampedX / 64) × 64
     - finalY = round(clampedY / 64) × 64
   - Return (finalX, finalY)
```

### 6. MyGdxGame

**Purpose**: Main game coordinator

**Location**: `src/main/java/wagemaker/uk/gdx/MyGdxGame.java` (existing file to be enhanced)

**Responsibilities**:
- Receive range configuration from GameClient
- Configure Player's TargetingSystem
- Coordinate between network and game systems

**Interface**:
```java
public class MyGdxGame {
    public void setPlantingMaxRange(int maxRange);
}
```

## Data Models

### Configuration Data

**server.properties Entry**:
```properties
# Planting Range Configuration (in pixels)
# Maximum distance a player can plant from their position
# Default: 512 (8 tiles at 64px per tile)
# Range: 64-1024 (1-16 tiles)
planting.max.range=512
```

**ServerConfig State**:
```java
{
    plantingMaxRange: int  // 64-1024, default 512
}
```

### Network Message Data

**ConnectionAcceptedMessage**:
```java
{
    clientId: String
    worldSeed: long
    plantingMaxRange: int  // NEW: 64-1024
}
```

### Runtime State

**GameClient State**:
```java
{
    plantingMaxRange: int  // -1 (unlimited) or 64-1024
}
```

**TargetingSystem State**:
```java
{
    maxRange: int          // -1 (unlimited) or 64-1024
    playerX: float         // Player position X
    playerY: float         // Player position Y
    targetX: float         // Current target X
    targetY: float         // Current target Y
    isActive: boolean      // Targeting mode active
}
```

## Error Handling

### Configuration Errors

**Invalid Range Value**:
- Detection: Parse error or out-of-bounds (< 64 or > 1024)
- Response: Use default value (512)
- Logging: Error message with invalid value and default being used
- User Impact: None (graceful degradation)

**Missing Configuration**:
- Detection: Property not found in server.properties
- Response: Use default value (512)
- Logging: Info message about using default
- User Impact: None (expected behavior)

### Runtime Errors

**Range Violation on Server**:
- Detection: Distance > configured maxRange
- Response: Reject planting request, do not consume item
- Logging: Security violation log with client ID and distances
- User Impact: Planting attempt fails (should not occur with proper client enforcement)

**Client-Server Desync**:
- Detection: Client allows targeting beyond server's range
- Response: Server rejects, client learns from rejection
- Logging: Both client and server log the discrepancy
- User Impact: Temporary confusion, self-correcting

### Network Errors

**Connection Lost During Configuration**:
- Detection: Connection drops before ConnectionAcceptedMessage received
- Response: Client remains in "not connected" state with unlimited range
- Logging: Connection error logged
- User Impact: Cannot plant (no server connection)

**Malformed Message**:
- Detection: Cannot parse plantingMaxRange field
- Response: Use default value (-1 for unlimited)
- Logging: Parse error logged
- User Impact: Client may allow targeting beyond server's range (server will reject)

## Testing Strategy

### Unit Tests

**ServerConfig Tests**:
```java
- testLoadValidRange()           // Valid range 64-1024
- testLoadBelowMinimum()         // Range < 64, expect default
- testLoadAboveMaximum()         // Range > 1024, expect default
- testLoadInvalidFormat()        // Non-numeric, expect default
- testLoadMissingProperty()      // Property absent, expect default
- testRangeConversion()          // Pixels to tiles conversion
```

**TargetingSystem Tests**:
```java
- testMoveWithinRange()          // Movement stays within range
- testMoveBeyondRange()          // Movement clamped to range
- testClampingAccuracy()         // Clamped position on boundary
- testTileGridSnapping()         // Clamped position snaps to grid
- testUnlimitedRange()           // maxRange = -1, no clamping
- testDiagonalClamping()         // Diagonal movement clamped correctly
- testIsWithinMaxRange()         // Distance validation
```

**ConnectionAcceptedMessage Tests**:
```java
- testSerialization()            // Message serializes correctly
- testDeserialization()          // Message deserializes correctly
- testBackwardCompatibility()    // Old clients can parse message
```

### Integration Tests

**Client-Server Synchronization**:
```java
- testRangeTransmission()        // Server sends range to client
- testClientConfiguration()      // Client configures targeting system
- testMultipleClients()          // All clients receive same range
- testRangeUpdate()              // Client updates when reconnecting
```

**Planting Validation**:
```java
- testPlantWithinRange()         // Planting succeeds within range
- testPlantAtMaxRange()          // Planting succeeds at exact max range
- testPlantBeyondRange()         // Planting rejected beyond range
- testTargetingEnforcement()     // Cursor cannot move beyond range
```

**Configuration Integration**:
```java
- testServerStartupWithConfig()  // Server loads config on startup
- testServerStartupNoConfig()    // Server uses default when missing
- testInvalidConfigHandling()    // Server handles invalid config gracefully
```

### Manual Testing Scenarios

**Configuration Testing**:
1. Set planting.max.range=256, verify 4-tile limit
2. Set planting.max.range=512, verify 8-tile limit
3. Set planting.max.range=1024, verify 16-tile limit
4. Set planting.max.range=50, verify fallback to 512
5. Set planting.max.range=abc, verify fallback to 512
6. Remove property, verify fallback to 512

**Gameplay Testing**:
1. Start server, connect client, verify range logged
2. Activate targeting, move cursor to max range
3. Attempt to move beyond max range, verify clamping
4. Plant at various distances, verify all succeed
5. Verify no server rejections within range

**Multiplayer Testing**:
1. Connect two clients to same server
2. Verify both receive same range configuration
3. Both players plant at max range
4. Verify no rejections or synchronization issues

**Single Player Testing**:
1. Start single player game (no server)
2. Verify unlimited targeting range
3. Plant at various distances
4. Verify all plantings succeed

## Performance Considerations

### Server Performance

**Configuration Loading**:
- Frequency: Once per server startup
- Cost: Single file read + parse
- Impact: Negligible (< 1ms)

**Range Validation**:
- Frequency: Once per planting attempt
- Cost: One distance calculation (sqrt)
- Impact: Negligible (< 0.1ms per validation)

### Client Performance

**Targeting Range Enforcement**:
- Frequency: Every cursor movement (arrow key press)
- Cost: One distance calculation + potential clamping
- Impact: Negligible (< 0.1ms per movement)

**Typical Load**:
- Cursor movements: ~5-10 per second during active targeting
- Total CPU: < 1ms per second
- Memory: +4 bytes per client

### Network Performance

**Connection Overhead**:
- Additional data: 4 bytes (int32) per connection
- Frequency: Once per client connection
- Impact: Negligible (< 0.001% of typical connection data)

### Optimization Opportunities

1. **Cache Distance Calculations**: Store last calculated distance to avoid recalculation
2. **Squared Distance Comparison**: Use d² < maxRange² to avoid sqrt() when only comparing
3. **Lazy Validation**: Only validate on confirm, not on every cursor move

## Security Considerations

### Server Authority

- Server is authoritative for all planting validation
- Client enforcement is UX optimization only
- Server never trusts client-reported distances
- All violations logged for monitoring

### Range Limits

- Minimum range (64px / 1 tile) prevents zero-range exploits
- Maximum range (1024px / 16 tiles) prevents excessive range abuse
- Bounds enforced at configuration load time
- Invalid values fail safe to default (512px)

### Logging and Monitoring

- All range violations logged with client ID
- Configuration errors logged at startup
- Repeated violations may indicate cheating attempt
- Logs provide audit trail for investigation

### Attack Vectors and Mitigations

**Modified Client**:
- Attack: Client removes range enforcement
- Mitigation: Server validates all requests
- Detection: Server logs violations
- Impact: Minimal (server rejects invalid requests)

**Configuration Tampering**:
- Attack: Unauthorized modification of server.properties
- Mitigation: File system permissions
- Detection: Unexpected range values in logs
- Impact: Gameplay balance affected, no security breach

**Network Message Tampering**:
- Attack: Modify ConnectionAcceptedMessage in transit
- Mitigation: Server validates all planting requests regardless
- Detection: Client-server desync logged
- Impact: Minimal (server authority maintained)

## Backward Compatibility

### Existing Behavior Preservation

- Default range (512px) matches current hardcoded value
- No changes to planting logic beyond range source
- Existing clients without range enforcement still work (server validates)
- Message format extends existing ConnectionAcceptedMessage

### Migration Path

1. Deploy server changes first
2. Server sends range to all clients (new and old)
3. Old clients ignore new field, continue with unlimited targeting
4. Server validates all clients equally
5. Deploy client changes
6. New clients enforce range locally
7. Improved UX for new clients, no breaking changes

### Version Compatibility Matrix

| Server Version | Client Version | Behavior |
|----------------|----------------|----------|
| Old | Old | Hardcoded 512px, no client enforcement |
| New | Old | Configured range, no client enforcement |
| Old | New | Hardcoded 512px, client uses unlimited |
| New | New | Configured range, client enforces |

## Future Enhancements

### Per-Player Range Modifiers

Allow different ranges based on player attributes:
- Player level or experience
- Equipped items or tools
- Player permissions or roles
- Temporary buffs or debuffs

### Item-Specific Ranges

Different ranges for different plantable items:
- Baby bamboo: 512px
- Seeds: 256px
- Saplings: 768px

### Dynamic Range Adjustment

Adjust range based on runtime conditions:
- Network latency (higher latency = shorter range)
- Server load (high load = shorter range)
- Player density (crowded areas = shorter range)

### Admin Commands

Runtime configuration without restart:
```
/setrange 512          # Set global range
/setrange player1 768  # Set per-player range
/getrange              # Query current range
```

### Visual Feedback Enhancements

- Range circle overlay showing maximum distance
- Color-coded cursor (green = within range, red = at limit)
- Distance indicator showing current target distance
- Audio feedback when reaching range limit

## Success Metrics

### Functional Metrics

- ✅ Server loads range from configuration file
- ✅ Client receives range from server on connection
- ✅ Targeting cursor enforces maximum range
- ✅ Server validates using configured range
- ✅ Zero planting rejections within configured range
- ✅ Configuration changes take effect on server restart

### Quality Metrics

- ✅ < 1ms overhead per planting validation
- ✅ < 1ms overhead per cursor movement
- ✅ < 5 bytes network overhead per connection
- ✅ 100% backward compatibility maintained
- ✅ Zero crashes or errors from invalid configuration

### User Experience Metrics

- ✅ Clear feedback when at maximum range
- ✅ No confusing "planting failed" messages within range
- ✅ Consistent behavior across single and multiplayer
- ✅ Server operators can adjust range without code changes

## References

### Existing Code

- `ClientConnection.java` line 1069: Current hardcoded range validation
- `TargetingSystem.java`: Client-side targeting implementation
- `ServerConfig.java`: Existing server configuration management
- `server.properties`: Server configuration file

### Related Features

- Bamboo planting system
- Multiplayer synchronization
- Targeting system
- Network protocol

### External Resources

- LibGDX documentation for coordinate systems
- Java Properties file format
- Network message serialization patterns
