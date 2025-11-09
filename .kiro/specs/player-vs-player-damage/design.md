# Design Document

## Overview

This design implements player-vs-player (PvP) combat functionality in the multiplayer game by extending the existing attack system. Players will be able to damage each other using the spacebar attack action, similar to how they currently attack trees. The implementation leverages the existing network infrastructure, attack messaging system, and health management to create a seamless PvP experience.

The design follows a client-server architecture where the server acts as the authoritative source for all combat interactions, validating attack ranges and applying damage to prevent cheating. Health updates are broadcast to all clients to ensure synchronized visual feedback.

## Architecture

### System Components

The PvP damage system integrates with the following existing components:

1. **Player.java** - Local player controller that detects attack inputs and checks for targets
2. **RemotePlayer.java** - Remote player representation that renders other players and their health bars
3. **GameClient.java** - Client-side network manager that sends attack messages to the server
4. **GameServer.java** - Server-side game state manager that validates and processes attacks
5. **ClientConnection.java** - Server-side client handler that processes individual client messages
6. **AttackActionMessage.java** - Network message for communicating attack actions
7. **PlayerHealthUpdateMessage.java** - Network message for broadcasting health changes

### Message Flow

```
[Local Player] --spacebar--> [Player.java]
                                  |
                                  v
                        Check for Remote Players in range
                                  |
                                  v
                        [GameClient.sendAttackAction(playerId)]
                                  |
                                  v
                        [AttackActionMessage] --network--> [GameServer]
                                                                |
                                                                v
                                                    [ClientConnection.handleAttackAction]
                                                                |
                                                                v
                                                    Validate range & cooldown
                                                                |
                                                                v
                                                        Apply damage to target
                                                                |
                                                                v
                                            [PlayerHealthUpdateMessage] --broadcast--> [All Clients]
                                                                                            |
                                                                                            v
                                                                                [Update health display]
```

### Attack Priority System

When a player presses spacebar, the attack system checks targets in the following order:

1. **Remote Players** - Check all remote players within attack range
2. **Trees** - If no players are in range, check trees (existing behavior)

This ensures that player combat takes precedence over environmental interactions when both are available.

## Components and Interfaces

### 1. Player.java Modifications

**New Fields:**
```java
private Map<String, RemotePlayer> remotePlayers; // Reference to remote players for range checking
private float lastPlayerAttackTime = 0; // Client-side attack cooldown tracking
private static final float PLAYER_ATTACK_COOLDOWN = 0.5f; // 0.5 seconds between attacks
```

**New Methods:**
```java
public void setRemotePlayers(Map<String, RemotePlayer> remotePlayers)
private RemotePlayer findNearestRemotePlayerInRange()
private boolean isPlayerInAttackRange(RemotePlayer remotePlayer)
```

**Modified Methods:**
```java
private void attackNearbyTrees() // Renamed to attackNearbyTargets()
```

**Attack Logic Flow:**
1. Check if enough time has passed since last attack (cooldown)
2. Search for remote players within attack range
3. If remote player found, send attack message with player ID as target
4. If no remote player found, fall back to existing tree attack logic
5. Update last attack time

### 2. RemotePlayer.java Modifications

**No structural changes required** - RemotePlayer already has:
- Health tracking (`health` field)
- Health bar rendering (`renderHealthBar()` method)
- Position tracking for range calculations

The existing health bar rendering will automatically display when a remote player takes damage.

### 3. GameClient.java Modifications

**Modified Methods:**
```java
public void sendAttackAction(String targetId) // Already exists, no changes needed
```

The existing `sendAttackAction()` method already supports sending attack messages with any target ID. We'll use player IDs as target IDs for PvP attacks.

### 4. ClientConnection.java Modifications

**New Fields:**
```java
private Map<String, Long> playerAttackCooldowns; // Track cooldowns per attacker-target pair
private static final long PLAYER_ATTACK_COOLDOWN_MS = 500; // 0.5 seconds
private static final float PLAYER_DAMAGE = 10.0f; // Damage per attack
```

**Modified Methods:**
```java
private void handleAttackAction(AttackActionMessage message)
```

**New Methods:**
```java
private void handlePlayerAttack(AttackActionMessage message)
private boolean isPlayerAttackOnCooldown(String attackerId, String targetId)
private void updatePlayerAttackCooldown(String attackerId, String targetId)
private String getCooldownKey(String attackerId, String targetId)
```

**Attack Handling Logic:**
1. Check if target ID corresponds to a player (exists in world state players)
2. If player target, delegate to `handlePlayerAttack()`
3. If tree target, use existing tree attack logic

**Player Attack Validation:**
1. Verify target player exists in world state
2. Check attacker-target cooldown (0.5 seconds)
3. Calculate distance between attacker and target positions
4. Validate distance is within attack range (100 pixels)
5. Apply damage to target player's health
6. Update cooldown timestamp
7. Broadcast health update to all clients
8. If target health reaches 0, client handles respawn locally

### 5. AttackActionMessage.java Modifications

**No changes required** - The existing message structure supports both tree and player targets:
```java
private String playerId;  // Attacker ID
private String targetId;  // Target ID (can be tree or player)
private float damage;     // Damage amount
```

### 6. PlayerHealthUpdateMessage.java Usage

**No changes required** - This message already broadcasts health updates:
```java
private String playerId;  // Player whose health changed
private float health;     // New health value
```

The server will broadcast this message after applying PvP damage, and all clients will update their local representation of the affected player.

## Data Models

### Attack Range Calculation

**Existing Attack Range (from Cactus.java):**
- Horizontal: 64 pixels from center
- Vertical: 96 pixels from center

**PvP Attack Range:**
- Use same range as tree attacks: 100 pixels (as defined in ClientConnection.java)
- Calculated using Euclidean distance: `sqrt(dx² + dy²)`

**Range Check Formula:**
```java
float dx = targetX - attackerX;
float dy = targetY - attackerY;
float distance = (float) Math.sqrt(dx * dx + dy * dy);
boolean inRange = distance <= ATTACK_RANGE; // 100 pixels
```

### Cooldown Tracking

**Server-Side Cooldown Map:**
```java
Map<String, Long> playerAttackCooldowns
Key format: "attackerId:targetId"
Value: timestamp of last attack (milliseconds)
```

**Cooldown Check:**
```java
long currentTime = System.currentTimeMillis();
long lastAttackTime = cooldowns.getOrDefault(key, 0L);
boolean onCooldown = (currentTime - lastAttackTime) < PLAYER_ATTACK_COOLDOWN_MS;
```

### Health Management

**Health Values:**
- Maximum health: 100
- PvP damage per attack: 10
- Health restoration from items: 20 (apples/bananas)
- Minimum health: 0 (triggers respawn)

**Health Update Flow:**
1. Server applies damage: `targetHealth -= 10`
2. Server clamps health: `Math.max(0, targetHealth)`
3. Server updates world state player health
4. Server broadcasts `PlayerHealthUpdateMessage` to all clients
5. Clients update local player/remote player health values
6. Clients render health bars if health < 100

## Error Handling

### Client-Side Validation

**Attack Input Validation:**
- Check if game client is connected before sending attack
- Verify remote players map is not null
- Ensure target player exists before sending message
- Log attack attempts for debugging

**Error Messages:**
```java
"Cannot attack: not connected to server"
"No remote players in attack range"
"Attack sent to player: {playerId}"
```

### Server-Side Validation

**Attack Message Validation:**
1. **Null checks:** Validate message and target ID are not null
2. **Player existence:** Verify target player exists in world state
3. **Range validation:** Calculate distance and verify within 100 pixels
4. **Cooldown check:** Ensure 0.5 seconds have elapsed since last attack
5. **Health validation:** Verify health values are within 0-100 range

**Security Violations:**
```java
"Invalid target ID in attack message"
"Player attack out of range: distance={distance}"
"Player attack on cooldown: {attackerId} -> {targetId}"
"Invalid health value: {health}"
```

**Violation Handling:**
- Log security violation with client ID and IP address
- Ignore invalid attack message (do not apply damage)
- Do not disconnect client (allow continued play)
- Track violations for potential future action

### Network Error Handling

**Connection Loss:**
- Client-side: Attack messages queued but not sent
- Server-side: Player removed from world state on disconnect
- Other clients: Receive `PlayerLeaveMessage` and remove remote player

**Message Loss:**
- TCP guarantees message delivery
- If connection drops, attack messages are lost
- No retry mechanism needed (player can attack again)

## Testing Strategy

### Unit Testing

**Player.java Tests:**
1. Test `findNearestRemotePlayerInRange()` with various player positions
2. Test attack cooldown enforcement
3. Test attack priority (players before trees)
4. Test attack with no remote players (falls back to trees)
5. Test attack with null remote players map

**ClientConnection.java Tests:**
1. Test `handlePlayerAttack()` with valid attack
2. Test attack range validation (in range vs out of range)
3. Test cooldown enforcement (immediate re-attack blocked)
4. Test cooldown expiration (attack allowed after 0.5s)
5. Test attack on non-existent player
6. Test attack with invalid health values
7. Test cooldown key generation for different attacker-target pairs

### Integration Testing

**Client-Server Communication:**
1. Test attack message sent from client to server
2. Test health update broadcast from server to all clients
3. Test health bar rendering on remote player after damage
4. Test health bar rendering on local player after being attacked
5. Test multiple players attacking same target
6. Test simultaneous attacks between two players

**Multiplayer Scenarios:**
1. **Two-player combat:** Player A attacks Player B, verify health decreases
2. **Three-player combat:** Player A and B both attack Player C
3. **Combat with respawn:** Player A kills Player B, verify respawn
4. **Combat with healing:** Player takes damage, picks up apple, health restored
5. **Combat with trees:** Player attacks tree, then attacks player
6. **Range testing:** Player attempts attack from various distances

### Manual Testing

**Gameplay Testing:**
1. Start server and connect two clients
2. Move players close together (within 100 pixels)
3. Press spacebar on Player A, verify Player B health decreases
4. Verify health bar appears above Player B
5. Verify health bar color (green for health, red for missing)
6. Continue attacking until Player B health reaches 0
7. Verify Player B respawns at new location with full health
8. Test attack cooldown by rapidly pressing spacebar
9. Test attack priority by positioning player near both tree and player
10. Test attack range by moving players apart gradually

**Edge Cases:**
1. Attack while moving
2. Attack while target is moving
3. Attack immediately after target respawns
4. Attack with high latency (simulate with network throttling)
5. Attack with packet loss (simulate with network tools)
6. Multiple players attacking same target simultaneously
7. Player disconnects while being attacked
8. Player disconnects while attacking

### Performance Testing

**Server Load Testing:**
1. Test with 10 players all attacking each other
2. Measure server CPU usage during combat
3. Measure network bandwidth during combat
4. Verify no memory leaks from cooldown map
5. Test cooldown map cleanup (remove old entries)

**Client Performance:**
1. Measure FPS during combat with multiple players
2. Test health bar rendering performance with many players
3. Verify no lag when receiving health update broadcasts

## Implementation Notes

### Code Reuse

The implementation maximizes code reuse:
- **Attack messaging:** Reuse existing `AttackActionMessage` class
- **Health updates:** Reuse existing `PlayerHealthUpdateMessage` class
- **Health bars:** Reuse existing health bar rendering in `RemotePlayer`
- **Range calculation:** Reuse existing distance calculation patterns
- **Validation:** Reuse existing server-side validation patterns

### Minimal Changes

The design minimizes changes to existing code:
- **Player.java:** Add remote player reference and modify attack method
- **ClientConnection.java:** Add player attack handling to existing attack handler
- **Other classes:** No changes required

### Backward Compatibility

The implementation maintains backward compatibility:
- Tree attacks continue to work exactly as before
- Single-player mode unaffected (no remote players to attack)
- Existing network messages unchanged
- Server can handle mixed clients (with/without PvP support)

### Future Enhancements

Potential future improvements:
1. **Configurable damage:** Allow server to configure PvP damage amount
2. **Damage types:** Different weapons with different damage values
3. **Knockback:** Push players back when hit
4. **Invincibility frames:** Brief invincibility after being hit
5. **Kill/death tracking:** Track player combat statistics
6. **Team system:** Prevent friendly fire between team members
7. **Combat log:** Display combat events in UI
8. **Sound effects:** Add audio feedback for attacks and damage
