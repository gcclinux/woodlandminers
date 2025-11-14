# Design Document: Stone and Pebble Collection System

## Overview

This design introduces a stone and pebble collection system to the game, following the existing patterns established for trees and items. Stones are destructible environmental objects similar to trees, while pebbles are collectible items similar to apples and bananas. The system integrates with the existing inventory, networking, and world generation systems.

## Architecture

The stone/pebble system follows the established architecture patterns:

1. **Stone Object**: Similar to `SmallTree`, a destructible world object with health, collision detection, and attack mechanics
2. **Pebble Item**: Similar to `WoodStack`, a collectible item that spawns when stones are destroyed
3. **Inventory Integration**: Extends the existing `Inventory` and `InventoryManager` classes to support pebble storage
4. **Network Synchronization**: Uses the existing message-based networking system for multiplayer support
5. **World Generation**: Integrates with the existing random spawning system used for trees

### Key Design Principles

- **Consistency**: Follow existing patterns for trees (SmallTree) and items (WoodStack, Apple)
- **Separation of Concerns**: Stone objects handle collision/health, pebble items handle collection
- **Thread Safety**: Use deferred operations for OpenGL calls from network thread (following MyGdxGame threading guidelines)
- **Mode Separation**: Maintain separate inventories for single-player and multiplayer modes

## Components and Interfaces

### 1. Stone Class

**Location**: `src/main/java/wagemaker/uk/objects/Stone.java`

**Purpose**: Represents a destructible stone object in the game world

**Key Responsibilities**:
- Render stone sprite from assets.png sprite sheet
- Handle collision detection with players
- Track health and damage
- Determine attack range
- Provide health bar display information

**Interface**:
```java
public class Stone {
    // Constructor
    public Stone(float x, float y)
    
    // Rendering
    public Texture getTexture()
    
    // Position
    public float getX()
    public float getY()
    
    // Collision
    public boolean collidesWith(float playerX, float playerY, float playerWidth, float playerHeight)
    public boolean isInAttackRange(float playerX, float playerY)
    
    // Health/Damage
    public boolean attack()  // Returns true if destroyed
    public void update(float deltaTime)
    public float getHealth()
    public void setHealth(float health)
    public float getHealthPercentage()
    public boolean shouldShowHealthBar()
    
    // Cleanup
    public void dispose()
}
```

**Design Details**:
- Initial health: 50 (less than trees since stones are smaller)
- Damage per attack: 10
- Health regeneration: After 5 seconds of no attacks, regenerate slowly
- Collision box: 32x32 pixels (smaller than trees)
- Attack range: 64 pixels horizontally, 64 pixels vertically from center
- Sprite location: Will be extracted from assets.png sprite sheet (coordinates TBD)

### 2. Pebble Class

**Location**: `src/main/java/wagemaker/uk/items/Pebble.java`

**Purpose**: Represents a collectible pebble item dropped from destroyed stones

**Key Responsibilities**:
- Render pebble sprite from assets.png sprite sheet
- Provide position information for collision detection
- Handle texture disposal

**Interface**:
```java
public class Pebble {
    // Constructor
    public Pebble(float x, float y)
    
    // Rendering
    public Texture getTexture()
    
    // Position
    public float getX()
    public float getY()
    
    // Cleanup
    public void dispose()
}
```

**Design Details**:
- Sprite size: 64x64 pixels (consistent with other collectible items)
- Sprite location: Will be extracted from assets.png sprite sheet (coordinates TBD)
- No collision box needed (uses simple distance-based pickup like other items)

### 3. Inventory System Extensions

**Modified Files**:
- `src/main/java/wagemaker/uk/inventory/ItemType.java`
- `src/main/java/wagemaker/uk/inventory/Inventory.java`
- `src/main/java/wagemaker/uk/inventory/InventoryManager.java`

**Changes to ItemType Enum**:
```java
public enum ItemType {
    APPLE(true, 20),
    BANANA(true, 20),
    BABY_BAMBOO(false, 0),
    BAMBOO_STACK(false, 0),
    WOOD_STACK(false, 0),
    PEBBLE(false, 0);  // NEW: Non-consumable resource
    
    // ... existing methods
}
```

**Changes to Inventory Class**:
- Add `pebbleCount` field
- Add getter/setter methods: `getPebbleCount()`, `setPebbleCount(int)`
- Add manipulation methods: `addPebble(int)`, `removePebble(int)`
- Update `clear()` method to reset pebble count

**Changes to InventoryManager Class**:
- Update `collectItem()` to handle PEBBLE type
- Update `addItemToInventory()` switch statement to include PEBBLE case
- Update `getSelectedItemType()` to return PEBBLE for slot 5 (key 6)
- Update `setSelectedSlot()` to accept 0-5 range (was 0-4)

### 4. Network Synchronization

**Modified Files**:
- `src/main/java/wagemaker/uk/network/ItemType.java` (network enum)
- `src/main/java/wagemaker/uk/network/InventoryUpdateMessage.java`
- `src/main/java/wagemaker/uk/network/InventorySyncMessage.java`

**New Message Types** (if needed):
- Reuse existing `ItemSpawnMessage` for pebble spawns
- Reuse existing `ItemPickupMessage` for pebble collection
- May need `StoneDestroyedMessage` similar to `TreeDestroyedMessage`

**Changes to Network ItemType Enum**:
```java
public enum ItemType implements Serializable {
    APPLE,
    BANANA,
    BAMBOO_STACK,
    BABY_BAMBOO,
    WOOD_STACK,
    PEBBLE  // NEW
}
```

**Changes to InventoryUpdateMessage**:
- Add `pebbleCount` field
- Update constructor and getters

**Stone Synchronization**:
- Stones will be synchronized similar to trees using world state messages
- Stone health updates will use a pattern similar to `TreeHealthUpdateMessage`
- Stone destruction will broadcast to all clients similar to tree destruction

### 5. World Generation Integration

**Modified File**: `src/main/java/wagemaker/uk/gdx/MyGdxGame.java`

**Stone Spawning Logic**:
- Add `List<Stone> stones` collection to MyGdxGame
- Add `Map<String, Stone> stoneMap` for multiplayer stone tracking
- Spawn stones during world generation (in `create()` or similar method)
- Distribution: Random placement across all biomes
- Density: Approximately 1 stone per 500x500 pixel area (configurable)
- Minimum distance from trees: 100 pixels to avoid overlap

**Stone Management**:
- Render stones in the game loop
- Update stone health regeneration
- Handle stone collision detection
- Process stone attacks from player
- Spawn pebbles when stones are destroyed
- Clean up destroyed stones

### 6. UI Integration

**Modified File**: `src/main/java/wagemaker/uk/ui/InventoryRenderer.java`

**Inventory Display Changes**:
- Extend inventory UI to show 6 slots (was 5)
- Add pebble sprite icon for slot 6
- Display pebble count in slot 6
- Highlight slot 6 when key "6" is pressed
- Show pebble sprite when slot 6 is selected

**Key Binding**:
- Key "6" selects pebble inventory slot (slot index 5)
- Follows existing pattern for keys 1-5

## Data Models

### Stone State

```java
{
    float x;              // World X position
    float y;              // World Y position
    float health;         // Current health (0-50)
    float timeSinceLastAttack;  // For health regeneration
    Texture texture;      // Rendered sprite
}
```

### Pebble State

```java
{
    float x;              // World X position
    float y;              // World Y position
    Texture texture;      // Rendered sprite
}
```

### Network Stone State

```java
{
    String stoneId;       // Unique identifier
    float x;              // World X position
    float y;              // World Y position
    float health;         // Current health
}
```

### Inventory State (Extended)

```java
{
    int appleCount;
    int bananaCount;
    int babyBambooCount;
    int bambooStackCount;
    int woodStackCount;
    int pebbleCount;      // NEW
}
```

## Error Handling

### Stone Collision Edge Cases

1. **Multiple Players Attacking Same Stone**: 
   - Server is authoritative for stone health
   - Clients send attack requests, server validates and broadcasts health updates
   - Prevents double-destruction

2. **Stone Destroyed During Attack**:
   - Check if stone still exists before applying damage
   - Gracefully handle null references

3. **Network Desync**:
   - Periodic stone health synchronization from server
   - Client-side prediction with server correction

### Pebble Collection Edge Cases

1. **Multiple Players Collecting Same Pebble**:
   - First pickup request wins (server-side)
   - Server removes pebble and broadcasts to all clients
   - Other clients ignore pickup attempts for non-existent pebbles

2. **Pebble Spawns Outside World Bounds**:
   - Validate spawn position before creating pebble
   - Clamp to valid world coordinates

3. **Inventory Overflow**:
   - No maximum limit (consistent with existing items)
   - Count stored as integer (max ~2 billion)

### Threading Safety

Following MyGdxGame threading guidelines:

1. **Network Thread Operations**:
   - Immediately update game state (remove from maps)
   - Defer OpenGL operations (texture disposal) using `deferOperation()`

2. **Render Thread Operations**:
   - All texture creation and disposal
   - All rendering operations
   - Process deferred operations at frame start

3. **Example Pattern**:
```java
// Network message handler (Network Thread)
public void handleStoneDestroyed(StoneDestroyedMessage message) {
    // ✅ Immediate state update
    Stone stone = stoneMap.remove(message.getStoneId());
    if (stone != null) {
        // ✅ Defer OpenGL operation
        deferOperation(() -> stone.dispose());
        
        // ✅ Spawn pebble (state update)
        spawnPebbleAt(stone.getX(), stone.getY());
    }
}
```

## Testing Strategy

### Unit Tests

1. **Stone Class Tests**:
   - Collision detection accuracy
   - Health management (damage, regeneration)
   - Attack range calculation
   - Destruction threshold

2. **Pebble Class Tests**:
   - Texture loading
   - Position getters

3. **Inventory Tests**:
   - Pebble add/remove operations
   - Count validation (no negatives)
   - Clear operation includes pebbles

4. **InventoryManager Tests**:
   - Pebble collection routing
   - Slot selection for slot 5 (key 6)
   - Network synchronization with pebbles

### Integration Tests

1. **Stone-Pebble Lifecycle**:
   - Stone spawns correctly
   - Stone takes damage from attacks
   - Stone destruction spawns pebble
   - Pebble can be collected
   - Pebble adds to inventory

2. **Multiplayer Synchronization**:
   - Stone health syncs across clients
   - Stone destruction broadcasts to all clients
   - Pebble spawn syncs across clients
   - Pebble collection updates all inventories
   - First pickup wins for contested pebbles

3. **Inventory UI Integration**:
   - Slot 6 displays correctly
   - Key "6" selects pebble slot
   - Pebble count updates in UI
   - Pebble sprite shows when selected

4. **World Generation**:
   - Stones spawn at appropriate density
   - Stones don't overlap with trees
   - Stones spawn in all biomes

### Manual Testing Scenarios

1. **Single-Player Flow**:
   - Start new game
   - Find and attack stone
   - Collect spawned pebble
   - Verify inventory count
   - Press key "6" to select pebbles
   - Save and load game (verify pebbles persist)

2. **Multiplayer Flow**:
   - Host server
   - Connect client
   - Both players attack same stone
   - Verify synchronized destruction
   - One player collects pebble
   - Verify other player sees collection
   - Verify inventory syncs

3. **Edge Cases**:
   - Attack stone from maximum range
   - Collect pebble while moving
   - Destroy multiple stones rapidly
   - Switch between inventory slots quickly

## Implementation Notes

### Sprite Asset Requirements

Two new sprites need to be added to `assets/sprites/assets.png`:

1. **Stone Sprite**: 64x64 pixels
   - Represents intact stone object
   - Gray/brown color scheme
   - Should look distinct from trees and other objects

2. **Pebble Sprite**: 64x64 pixels
   - Represents collectible pebble item
   - Smaller visual appearance than stone
   - Should match stone color scheme

**Sprite Coordinates** (to be determined during implementation):
- Stone: TBD (e.g., 384, 0, 64, 64)
- Pebble: TBD (e.g., 448, 0, 64, 64)

### Performance Considerations

1. **Stone Count**: Limit to reasonable number (~50-100 stones per world)
2. **Collision Checks**: Only check stones near player (spatial partitioning if needed)
3. **Network Traffic**: Batch stone health updates when possible
4. **Memory**: Dispose stone/pebble textures properly to avoid leaks

### Future Enhancements

Potential future additions (not in current scope):

1. Different stone types (small, medium, large)
2. Stone-specific tools (pickaxe)
3. Pebble crafting recipes
4. Stone respawning over time
5. Biome-specific stone variants
