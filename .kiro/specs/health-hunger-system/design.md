# Design Document

## Overview

This design document outlines the implementation of a comprehensive health and hunger system for the game. The system modifies existing health mechanics to remove automatic consumption, introduces a new hunger mechanic that accumulates over time, and creates a unified visual health bar that displays both health damage (red) and hunger (blue) on a green base.

The design builds upon the existing health system in the `Player` class and the inventory management system in `InventoryManager`, while introducing new components for hunger tracking and unified health bar rendering.

## Architecture

### Component Overview

```
┌─────────────────────────────────────────────────────────────┐
│                         MyGdxGame                            │
│  (Main game loop, rendering coordination)                   │
└────────────┬────────────────────────────────────────────────┘
             │
             ├──────────────────────────────────────────────────┐
             │                                                  │
    ┌────────▼────────┐                              ┌─────────▼────────┐
    │     Player      │                              │  HealthBarUI     │
    │                 │                              │                  │
    │ - health        │◄─────────────────────────────┤ - renderBar()    │
    │ - hunger        │  reads health/hunger         │                  │
    │ - hungerTimer   │                              └──────────────────┘
    └────────┬────────┘
             │
             │ manages
             │
    ┌────────▼────────────┐
    │  InventoryManager   │
    │                     │
    │ - consumeApple()    │
    │ - consumeBanana()   │
    │ - NO auto-consume   │
    └─────────────────────┘
```

### Key Design Decisions

1. **Hunger as Player State**: Hunger is tracked as a float field in the `Player` class (0-100%), similar to health
2. **Separate Consumption Methods**: Apple and banana consumption are handled by separate methods with different effects
3. **Unified Health Bar**: A new `HealthBarUI` class renders a single bar with three color layers (green base, red damage, blue hunger)
4. **Manual Consumption Only**: Remove all automatic consumption logic from `InventoryManager`
5. **Space Bar Context**: Space bar triggers item consumption when an item is selected in inventory

## Components and Interfaces

### 1. Player Class Modifications

**File**: `src/main/java/wagemaker/uk/player/Player.java`

**New Fields**:
```java
private float hunger = 0;  // Hunger level (0-100%)
private float hungerTimer = 0;  // Timer for hunger accumulation
private static final float HUNGER_INTERVAL = 60.0f;  // 60 seconds per 1% hunger
```

**New Methods**:
```java
// Hunger management
public float getHunger()
public void setHunger(float hunger)
public void increaseHunger(float amount)
private void updateHunger(float deltaTime)
private void handleHungerDeath()

// Item consumption (called when space bar pressed with item selected)
public void consumeSelectedItem()
```

**Modified Methods**:
```java
// Update method - add hunger update logic
public void update(float deltaTime) {
    // ... existing code ...
    updateHunger(deltaTime);
    
    // Handle space bar for item consumption
    if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
        if (targetingSystem.isActive()) {
            handleSpacebarPlanting();
        } else if (inventoryManager.getSelectedSlot() != -1) {
            consumeSelectedItem();  // NEW: consume item if selected
        } else {
            attackNearbyTargets();
        }
    }
}

// Respawn method - reset hunger on death
private void respawnPlayer() {
    health = 100;
    hunger = 0;  // NEW: reset hunger
    // ... existing respawn logic ...
}
```

### 2. InventoryManager Class Modifications

**File**: `src/main/java/wagemaker/uk/inventory/InventoryManager.java`

**Removed Methods**:
- `tryAutoConsume()` - Remove automatic consumption entirely
- `consumeApple()` - Move to Player class with new logic
- `consumeBanana()` - Move to Player class with new logic

**Modified Methods**:
```java
// Remove health-based routing - always add to inventory
public void collectItem(ItemType type) {
    if (type == null) {
        return;
    }
    // Always add to inventory (no automatic consumption)
    addItemToInventory(type, 1);
}
```

**New Methods**:
```java
// Manual consumption triggered by player
public boolean tryConsumeSelectedItem(Player player) {
    int selectedSlot = getSelectedSlot();
    ItemType itemType = getSelectedItemType();
    
    if (itemType == null) {
        return false;
    }
    
    Inventory inventory = getCurrentInventory();
    
    switch (itemType) {
        case APPLE:
            if (inventory.removeApple(1)) {
                player.setHealth(Math.min(100, player.getHealth() + 10));
                sendInventoryUpdate();
                return true;
            }
            break;
        case BANANA:
            if (inventory.removeBanana(1)) {
                player.setHunger(Math.max(0, player.getHunger() - 5));
                sendInventoryUpdate();
                return true;
            }
            break;
    }
    
    return false;
}
```

### 3. ItemType Enum Modifications

**File**: `src/main/java/wagemaker/uk/inventory/ItemType.java`

**Modified Values**:
```java
public enum ItemType {
    APPLE(true, 10, false),      // Restores 10% health, not for hunger
    BANANA(false, 0, true),      // Reduces 5% hunger, not for health
    BABY_BAMBOO(false, 0, false),
    BAMBOO_STACK(false, 0, false),
    WOOD_STACK(false, 0, false),
    PEBBLE(false, 0, false);
    
    private final boolean restoresHealth;
    private final int healthRestore;
    private final boolean reducesHunger;
    
    ItemType(boolean restoresHealth, int healthRestore, boolean reducesHunger) {
        this.restoresHealth = restoresHealth;
        this.healthRestore = healthRestore;
        this.reducesHunger = reducesHunger;
    }
    
    public boolean restoresHealth() { return restoresHealth; }
    public int getHealthRestore() { return healthRestore; }
    public boolean reducesHunger() { return reducesHunger; }
}
```

### 4. New HealthBarUI Class

**File**: `src/main/java/wagemaker/uk/ui/HealthBarUI.java`

**Purpose**: Render unified health bar showing health damage (red) and hunger (blue) on green base

**Fields**:
```java
private ShapeRenderer shapeRenderer;
private static final float BAR_WIDTH = 200;
private static final float BAR_HEIGHT = 20;
private static final float BAR_X = 10;  // Screen position
private static final float BAR_Y = 10;
```

**Methods**:
```java
public HealthBarUI(ShapeRenderer shapeRenderer)
public void render(float health, float hunger)
public void dispose()
```

**Rendering Logic**:
```java
public void render(float health, float hunger) {
    // Calculate percentages
    float healthPercent = health / 100.0f;
    float hungerPercent = hunger / 100.0f;
    
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    
    // 1. Draw green base (full bar)
    shapeRenderer.setColor(0, 1, 0, 1);  // Green
    shapeRenderer.rect(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT);
    
    // 2. Draw red damage overlay (from right side)
    float damagePercent = 1.0f - healthPercent;
    if (damagePercent > 0) {
        shapeRenderer.setColor(1, 0, 0, 1);  // Red
        float damageWidth = BAR_WIDTH * damagePercent;
        shapeRenderer.rect(BAR_X + BAR_WIDTH - damageWidth, BAR_Y, 
                          damageWidth, BAR_HEIGHT);
    }
    
    // 3. Draw blue hunger overlay (from left side)
    if (hungerPercent > 0) {
        shapeRenderer.setColor(0, 0, 1, 1);  // Blue
        float hungerWidth = BAR_WIDTH * hungerPercent;
        shapeRenderer.rect(BAR_X, BAR_Y, hungerWidth, BAR_HEIGHT);
    }
    
    // 4. Draw border
    shapeRenderer.end();
    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    shapeRenderer.setColor(0, 0, 0, 1);  // Black border
    shapeRenderer.rect(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT);
    shapeRenderer.end();
}
```

### 5. AppleTree Modifications

**File**: `src/main/java/wagemaker/uk/trees/AppleTree.java`

**Modified Behavior**: When destroyed, immediately restore 10% health to player

**Implementation in Player.attackNearbyTargets()**:
```java
if (destroyed) {
    // Immediate health restoration (10%)
    float currentHealth = player.getHealth();
    player.setHealth(Math.min(100, currentHealth + 10));
    System.out.println("Apple tree destroyed! Health restored: 10%");
    
    // Spawn apple at tree position (for inventory)
    apples.put(targetKey, new Apple(targetAppleTree.getX(), targetAppleTree.getY()));
    
    // ... existing cleanup code ...
}
```

## Data Models

### Player State
```java
class Player {
    // Health system
    float health;           // 0-100, damage taken
    float previousHealth;   // For change detection
    
    // Hunger system (NEW)
    float hunger;           // 0-100, hunger level
    float hungerTimer;      // Accumulator for 60-second intervals
    
    // Constants
    static final float HUNGER_INTERVAL = 60.0f;  // Seconds per 1% hunger
}
```

### Health Bar Visual Model
```
┌────────────────────────────────────────┐
│ GREEN (base - full health/no hunger)   │
│ RED (damage overlay from right)        │
│ BLUE (hunger overlay from left)        │
└────────────────────────────────────────┘

Example: 70% health, 30% hunger
┌────────────────────────────────────────┐
│BLUE│      GREEN      │      RED        │
│30% │      40%        │      30%        │
└────────────────────────────────────────┘
```

### Item Consumption Model
```
Apple:
  - Selected in inventory (slot 0)
  - Space bar pressed
  - Remove 1 apple from inventory
  - Restore 10% health (capped at 100%)
  - No effect on hunger

Banana:
  - Selected in inventory (slot 1)
  - Space bar pressed
  - Remove 1 banana from inventory
  - Reduce 5% hunger (minimum 0%)
  - No effect on health
```

## Error Handling

### Hunger Death
```java
private void handleHungerDeath() {
    System.out.println("Player died from hunger! Respawning...");
    
    // Reset both health and hunger
    health = 100;
    hunger = 0;
    
    // Respawn at origin (0, 0)
    x = 0;
    y = 0;
    
    // Send respawn message in multiplayer
    if (gameClient != null && gameClient.isConnected()) {
        gameClient.sendPlayerRespawn(x, y);
    }
    
    System.out.println("Player respawned at origin!");
}
```

### Invalid Consumption Attempts
```java
// No item selected
if (inventoryManager.getSelectedSlot() == -1) {
    return;  // Silent fail, no action
}

// Item not consumable (e.g., baby bamboo)
if (!itemType.restoresHealth() && !itemType.reducesHunger()) {
    System.out.println("Selected item cannot be consumed");
    return;
}

// No items in inventory
if (!inventory.hasItem(itemType)) {
    System.out.println("No " + itemType.name() + " in inventory");
    return;
}
```

### Multiplayer Synchronization
```java
// Send hunger updates to server (similar to health updates)
private void checkAndSendHungerUpdate() {
    if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
        if (hunger != previousHunger) {
            gameClient.sendPlayerHunger(hunger);
            previousHunger = hunger;
        }
    }
}
```

## Testing Strategy

### Unit Tests

1. **Hunger Accumulation Test**
   - Verify hunger increases by 1% every 60 seconds
   - Test timer reset after each 1% increase
   - Verify hunger caps at 100%

2. **Apple Consumption Test**
   - Verify apple restores 10% health
   - Verify apple does not affect hunger
   - Verify apple is removed from inventory
   - Verify health caps at 100%

3. **Banana Consumption Test**
   - Verify banana reduces 5% hunger
   - Verify banana does not affect health
   - Verify banana is removed from inventory
   - Verify hunger floors at 0%

4. **Hunger Death Test**
   - Verify player dies at 100% hunger
   - Verify respawn at (0, 0)
   - Verify health and hunger reset to 100% and 0%

5. **Apple Tree Destruction Test**
   - Verify immediate 10% health restoration
   - Verify apple item spawns for inventory
   - Verify health restoration occurs before item spawn

### Integration Tests

1. **Health Bar Rendering Test**
   - Verify green base renders at full width
   - Verify red damage overlay renders from right
   - Verify blue hunger overlay renders from left
   - Verify overlays combine correctly
   - Test edge cases: 0% health, 100% hunger, both at extremes

2. **Item Collection Flow Test**
   - Destroy apple tree → verify immediate heal + apple spawn
   - Pick up apple → verify added to inventory
   - Select apple → verify space bar consumes it
   - Verify no automatic consumption on pickup

3. **Multiplayer Synchronization Test**
   - Verify hunger updates broadcast to server
   - Verify hunger death broadcasts respawn
   - Verify item consumption syncs inventory

### Manual Testing Scenarios

1. **Hunger Survival Challenge**
   - Start game, wait 100 minutes (100% hunger)
   - Verify player dies and respawns at origin
   - Verify health and hunger reset

2. **Item Management Flow**
   - Collect apples and bananas
   - Take damage from cactus
   - Verify no automatic healing
   - Select apple, press space bar
   - Verify manual healing works

3. **Visual Health Bar Test**
   - Take damage → verify red increases from right
   - Wait for hunger → verify blue increases from left
   - Consume items → verify colors update correctly
   - Test with various health/hunger combinations

## Implementation Notes

### Removal of Auto-Consumption

The current system has automatic consumption logic in `InventoryManager.tryAutoConsume()` that is called when player health decreases. This must be completely removed:

1. Remove `tryAutoConsume()` method
2. Remove call to `tryAutoConsume()` in `Player.update()`
3. Remove health-based routing in `collectItem()`

### Apple Tree Immediate Healing

When an apple tree is destroyed, the player receives immediate 10% health restoration. This happens in the `attackNearbyTargets()` method before the apple item is spawned:

```java
if (destroyed) {
    // FIRST: Immediate health restoration
    player.setHealth(Math.min(100, player.getHealth() + 10));
    
    // THEN: Spawn apple item for inventory
    apples.put(targetKey, new Apple(...));
}
```

### Space Bar Context Sensitivity

The space bar has three contexts:
1. **Targeting active**: Plant item at target location
2. **Item selected (no targeting)**: Consume selected item
3. **No item selected**: Attack nearby targets

This requires careful ordering in the input handling logic.

### Hunger Timer Precision

The hunger timer uses delta time accumulation to ensure consistent behavior regardless of frame rate:

```java
hungerTimer += deltaTime;
if (hungerTimer >= HUNGER_INTERVAL) {
    hunger = Math.min(100, hunger + 1);
    hungerTimer -= HUNGER_INTERVAL;  // Preserve remainder
}
```

### Network Message Extensions

New network messages needed for multiplayer:
- `PlayerHungerUpdateMessage`: Broadcast hunger changes
- `PlayerRespawnMessage`: Broadcast hunger death respawn (extends existing respawn logic)

These follow the same pattern as existing `PlayerHealthUpdateMessage`.

## Multiplayer Considerations

### Server Authority Model

The hunger and health system follows the existing client-server architecture:

1. **Client-Side Prediction**: Local player updates hunger/health immediately for responsive gameplay
2. **Server Authority**: Server validates and broadcasts state changes to all clients
3. **Remote Player Sync**: Remote players receive hunger/health updates via network messages

### Hunger Synchronization

**Client Behavior**:
```java
// In Player.updateHunger()
private void updateHunger(float deltaTime) {
    hungerTimer += deltaTime;
    if (hungerTimer >= HUNGER_INTERVAL) {
        hunger = Math.min(100, hunger + 1);
        hungerTimer -= HUNGER_INTERVAL;
        
        // Send update to server in multiplayer
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            gameClient.sendPlayerHunger(hunger);
        }
    }
    
    // Check for hunger death
    if (hunger >= 100) {
        handleHungerDeath();
    }
}
```

**Server Behavior**:
```java
// In GameServer message handler
public void handlePlayerHungerUpdate(PlayerHungerUpdateMessage message) {
    String playerId = message.getPlayerId();
    float hunger = message.getHunger();
    
    // Update server-side player state
    PlayerState state = playerStates.get(playerId);
    if (state != null) {
        state.setHunger(hunger);
        
        // Broadcast to all other clients
        broadcastToOthers(message, playerId);
    }
}
```

**Remote Player Display**:
```java
// In RemotePlayer class
public void updateHunger(float hunger) {
    this.hunger = Math.max(0, Math.min(100, hunger));
}

// In RemotePlayer.renderHealthBar()
public void renderHealthBar(ShapeRenderer shapeRenderer) {
    // Render unified health bar showing both health and hunger
    // Same rendering logic as local player
}
```

### Item Consumption in Multiplayer

**Client Request**:
```java
// In Player.consumeSelectedItem()
public void consumeSelectedItem() {
    if (inventoryManager.tryConsumeSelectedItem(this)) {
        // Send consumption to server
        if (gameClient != null && gameClient.isConnected()) {
            ItemType itemType = inventoryManager.getSelectedItemType();
            gameClient.sendItemConsumption(itemType);
        }
    }
}
```

**Server Validation**:
```java
// In GameServer
public void handleItemConsumption(ItemConsumptionMessage message) {
    String playerId = message.getPlayerId();
    ItemType itemType = message.getItemType();
    
    // Validate player has item in inventory
    PlayerState state = playerStates.get(playerId);
    if (state != null && state.hasItem(itemType)) {
        // Remove item from server inventory
        state.removeItem(itemType);
        
        // Apply effect
        if (itemType == ItemType.APPLE) {
            state.setHealth(Math.min(100, state.getHealth() + 10));
        } else if (itemType == ItemType.BANANA) {
            state.setHunger(Math.max(0, state.getHunger() - 5));
        }
        
        // Broadcast inventory and health/hunger updates
        broadcastInventoryUpdate(playerId, state.getInventory());
        broadcastHealthUpdate(playerId, state.getHealth());
        broadcastHungerUpdate(playerId, state.getHunger());
    }
}
```

### Apple Tree Destruction in Multiplayer

**Client Attack**:
```java
// In Player.attackNearbyTargets()
if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
    gameClient.sendAttackAction(targetKey);
    // Server handles tree destruction and health restoration
} else {
    // Single-player: immediate health restoration
    player.setHealth(Math.min(100, player.getHealth() + 10));
    // ... spawn apple and cleanup ...
}
```

**Server Processing**:
```java
// In GameServer.handleAttackAction()
if (target instanceof AppleTree && destroyed) {
    // Restore health to attacking player
    PlayerState attackerState = playerStates.get(attackerId);
    attackerState.setHealth(Math.min(100, attackerState.getHealth() + 10));
    
    // Broadcast health update
    broadcastHealthUpdate(attackerId, attackerState.getHealth());
    
    // Spawn apple item
    spawnItem(ItemType.APPLE, treeX, treeY);
    
    // Broadcast tree destruction
    broadcastTreeDestroyed(targetKey);
}
```

### Hunger Death in Multiplayer

**Client Death**:
```java
private void handleHungerDeath() {
    health = 100;
    hunger = 0;
    x = 0;
    y = 0;
    
    // Notify server of respawn
    if (gameClient != null && gameClient.isConnected()) {
        gameClient.sendPlayerRespawn(x, y, health, hunger);
    }
}
```

**Server Broadcast**:
```java
// In GameServer
public void handlePlayerRespawn(PlayerRespawnMessage message) {
    String playerId = message.getPlayerId();
    
    // Update server state
    PlayerState state = playerStates.get(playerId);
    state.setPosition(0, 0);
    state.setHealth(100);
    state.setHunger(0);
    
    // Broadcast to all clients
    broadcastPlayerRespawn(playerId, 0, 0, 100, 0);
}
```

### Health Bar Rendering for Remote Players

Remote players display the same unified health bar above their character:

```java
// In RemotePlayer.renderHealthBar()
public void renderHealthBar(ShapeRenderer shapeRenderer) {
    // Only show if health < 100 or hunger > 0
    if (health < 100 || hunger > 0) {
        float barWidth = 100;
        float barHeight = 6;
        float barX = x;
        float barY = y + 110;  // Above player sprite
        
        // Same three-layer rendering as local player
        // 1. Green base
        // 2. Red damage overlay
        // 3. Blue hunger overlay
    }
}
```

### Network Message Definitions

**New Messages**:

1. `PlayerHungerUpdateMessage`:
   - `playerId`: String
   - `hunger`: float (0-100)

2. `ItemConsumptionMessage`:
   - `playerId`: String
   - `itemType`: ItemType enum

3. `PlayerRespawnMessage` (extend existing):
   - `playerId`: String
   - `x`: float
   - `y`: float
   - `health`: float
   - `hunger`: float (NEW)

### Synchronization Edge Cases

1. **Network Lag**: Client-side prediction ensures responsive gameplay; server corrections applied smoothly
2. **Disconnection**: On reconnect, server sends full state sync including hunger
3. **Join Mid-Game**: New players receive current hunger state of all existing players
4. **Hunger Death During Lag**: Server validates death condition; client respawn may be corrected if out of sync
