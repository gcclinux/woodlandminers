# Design Document

## Overview

The bamboo planting system enables players to plant baby bamboo items from their inventory onto sand tiles. The system integrates with the existing inventory, biome, and tree management systems. When planted, baby bamboo remains visible for 120 seconds before automatically transforming into a mature bamboo tree.

This feature extends the existing game mechanics by:
- Adding a new player action (planting with "p" key)
- Introducing a growth lifecycle for planted items
- Integrating with the biome system to validate sand tiles
- Managing planted bamboo state across single-player and multiplayer modes

## Architecture

### System Components

The bamboo planting system consists of four main components:

1. **PlantingSystem**: Core planting logic and validation
2. **PlantedBamboo**: Represents a planted baby bamboo with growth timer
3. **Player Integration**: Handles "p" key input and planting action
4. **MyGdxGame Integration**: Manages planted bamboo collection and rendering

### Component Interaction Flow

```
Player presses "p" key
    ↓
Player.handlePlantingAction()
    ↓
PlantingSystem.attemptPlant()
    ↓
Validates: inventory, tile type, tile occupancy
    ↓
If valid: Creates PlantedBamboo instance
    ↓
PlantedBamboo added to game world
    ↓
Update loop: PlantedBamboo.update(deltaTime)
    ↓
After 120 seconds: Transform to BambooTree
```

### Data Flow

- **Input**: Player position, selected inventory item, "p" key press
- **Validation**: BiomeManager checks tile type, PlantingSystem checks occupancy
- **State Change**: Inventory deduction, PlantedBamboo creation
- **Growth**: Timer-based transformation to BambooTree
- **Cleanup**: PlantedBamboo removal, BambooTree spawning

## Components and Interfaces

### 1. PlantingSystem

**Purpose**: Centralized planting logic and validation

**Location**: `src/main/java/wagemaker/uk/planting/PlantingSystem.java`

**Key Methods**:
```java
public class PlantingSystem {
    // Attempt to plant baby bamboo at player's current position
    public boolean attemptPlant(Player player, BiomeManager biomeManager, 
                                Map<String, PlantedBamboo> plantedBamboos,
                                Map<String, BambooTree> bambooTrees);
    
    // Check if a tile is valid for planting (sand and unoccupied)
    private boolean isValidPlantingLocation(float x, float y, 
                                           BiomeManager biomeManager,
                                           Map<String, PlantedBamboo> plantedBamboos,
                                           Map<String, BambooTree> bambooTrees);
    
    // Generate unique key for planted bamboo based on tile coordinates
    private String generatePlantedBambooKey(float x, float y);
    
    // Snap coordinates to 64x64 tile grid
    private float snapToTileGrid(float coordinate);
}
```

**Responsibilities**:
- Validate player has baby bamboo in inventory
- Check if player is standing on sand tile using BiomeManager
- Verify tile is not already occupied by planted bamboo or bamboo tree
- Deduct baby bamboo from inventory
- Create and return PlantedBamboo instance
- Generate unique keys for planted bamboo based on tile position

**Integration Points**:
- `Player`: Receives player position and inventory manager
- `BiomeManager`: Queries biome type at player position
- `InventoryManager`: Checks and deducts baby bamboo
- `PlantedBamboo`: Creates new instances

### 2. PlantedBamboo

**Purpose**: Represents a planted baby bamboo with growth lifecycle

**Location**: `src/main/java/wagemaker/uk/planting/PlantedBamboo.java`

**Key Properties**:
```java
public class PlantedBamboo {
    private float x, y;              // World position (tile-aligned)
    private Texture texture;         // Visual representation
    private float growthTimer;       // Time since planting (seconds)
    private static final float GROWTH_DURATION = 120.0f; // 120 seconds
}
```

**Key Methods**:
```java
public class PlantedBamboo {
    // Update growth timer, returns true when ready to transform
    public boolean update(float deltaTime);
    
    // Check if growth is complete
    public boolean isReadyToTransform();
    
    // Get position for spawning bamboo tree
    public float getX();
    public float getY();
    
    // Cleanup resources
    public void dispose();
}
```

**Responsibilities**:
- Track growth timer (0 to 120 seconds)
- Render baby bamboo sprite at tile position
- Signal when transformation to bamboo tree should occur
- Provide position for bamboo tree spawning
- Manage texture lifecycle

**Integration Points**:
- `MyGdxGame`: Updated in main game loop
- `BambooTree`: Spawned at same position after growth completes
- Reuses existing `BabyBamboo` texture loading logic

### 3. Player Integration

**Modifications to**: `src/main/java/wagemaker/uk/player/Player.java`

**New Method**:
```java
private void handlePlantingAction() {
    if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
        if (plantingSystem != null && inventoryManager != null) {
            // Check if baby bamboo is selected (item slot 2, index 2)
            if (inventoryManager.getSelectedSlot() == 2) {
                plantingSystem.attemptPlant(this, biomeManager, 
                                           plantedBamboos, bambooTrees);
            }
        }
    }
}
```

**Integration in `update()` method**:
- Call `handlePlantingAction()` after inventory selection handling
- Only process when game menu is not open

**New Fields**:
```java
private PlantingSystem plantingSystem;
private BiomeManager biomeManager;
private Map<String, PlantedBamboo> plantedBamboos;
```

**New Setters**:
```java
public void setPlantingSystem(PlantingSystem plantingSystem);
public void setBiomeManager(BiomeManager biomeManager);
public void setPlantedBamboos(Map<String, PlantedBamboo> plantedBamboos);
```

### 4. MyGdxGame Integration

**Modifications to**: `src/main/java/wagemaker/uk/gdx/MyGdxGame.java`

**New Fields**:
```java
private Map<String, PlantedBamboo> plantedBamboos;
private PlantingSystem plantingSystem;
```

**Initialization in `create()`**:
```java
plantedBamboos = new HashMap<>();
plantingSystem = new PlantingSystem();
player.setPlantingSystem(plantingSystem);
player.setBiomeManager(biomeManager);
player.setPlantedBamboos(plantedBamboos);
```

**Update Logic in `render()`**:
```java
// Update planted bamboos and check for transformations
List<String> bambooToTransform = new ArrayList<>();
for (Map.Entry<String, PlantedBamboo> entry : plantedBamboos.entrySet()) {
    PlantedBamboo planted = entry.getValue();
    if (planted.update(deltaTime)) {
        bambooToTransform.add(entry.getKey());
    }
}

// Transform mature planted bamboos into bamboo trees
for (String key : bambooToTransform) {
    PlantedBamboo planted = plantedBamboos.remove(key);
    BambooTree tree = new BambooTree(planted.getX(), planted.getY());
    bambooTrees.put(key, tree);
    planted.dispose();
}
```

**Rendering in `render()`**:
```java
// Render planted bamboos (after terrain, before trees)
for (PlantedBamboo planted : plantedBamboos.values()) {
    batch.draw(planted.getTexture(), planted.getX(), planted.getY(), 64, 64);
}
```

**Cleanup in `dispose()`**:
```java
for (PlantedBamboo planted : plantedBamboos.values()) {
    planted.dispose();
}
```

## Data Models

### PlantedBamboo State

```java
{
    x: float,              // World X coordinate (tile-aligned to 64px grid)
    y: float,              // World Y coordinate (tile-aligned to 64px grid)
    texture: Texture,      // Baby bamboo sprite
    growthTimer: float,    // Elapsed time since planting (0-120 seconds)
    GROWTH_DURATION: 120.0f // Constant: time to mature
}
```

### Tile Grid System

- World is divided into 64x64 pixel tiles
- Planted bamboo positions are snapped to tile boundaries
- Tile key format: `"planted-bamboo-{tileX}-{tileY}"`
- Example: Player at (1234, 5678) → Tile (1216, 5632) → Key "planted-bamboo-1216-5632"

### Inventory Slot Mapping

Based on existing `InventoryRenderer.java`:
- Slot 0: Apple
- Slot 1: Banana
- Slot 2: Baby Bamboo (item 3 in user's description)
- Slot 3: Bamboo Stack
- Slot 4: Wood Stack

## Error Handling

### Validation Failures

**No Baby Bamboo Selected**:
- Action: Silent failure (no action taken)
- User feedback: None (expected behavior)

**No Baby Bamboo in Inventory**:
- Action: Silent failure (inventory check prevents deduction)
- User feedback: None (player can see inventory count is 0)

**Not Standing on Sand**:
- Action: Silent failure (biome check prevents planting)
- User feedback: None (visual feedback: player can see terrain type)

**Tile Already Occupied**:
- Action: Silent failure (occupancy check prevents duplicate planting)
- User feedback: None (visual feedback: player can see existing bamboo/tree)

### Edge Cases

**Player Moves During Growth**:
- Planted bamboo remains at original tile
- Growth continues independently of player position

**Multiple Rapid Planting Attempts**:
- `isKeyJustPressed()` prevents multiple triggers per frame
- Each tile can only have one planted bamboo

**Game Save/Load**:
- Initial implementation: Planted bamboos are not persisted
- Future enhancement: Add to WorldSaveData

**Multiplayer Synchronization**:
- Initial implementation: Single-player only
- Future enhancement: Add network messages for planting and growth

## Testing Strategy

### Unit Testing

**PlantingSystem Tests**:
- Test valid planting on sand tile with baby bamboo in inventory
- Test rejection when not on sand tile
- Test rejection when tile is occupied by planted bamboo
- Test rejection when tile is occupied by bamboo tree
- Test rejection when no baby bamboo in inventory
- Test tile grid snapping (various coordinates → correct tile alignment)
- Test key generation uniqueness

**PlantedBamboo Tests**:
- Test growth timer increments correctly
- Test transformation signal at exactly 120 seconds
- Test transformation signal not triggered before 120 seconds
- Test position getters return correct values

### Integration Testing

**Player Integration**:
- Test "p" key triggers planting when baby bamboo selected
- Test "p" key does nothing when other items selected
- Test "p" key does nothing when no item selected
- Test inventory deduction after successful planting

**Game Loop Integration**:
- Test planted bamboo appears at correct position
- Test planted bamboo transforms to tree after 120 seconds
- Test multiple planted bamboos can grow simultaneously
- Test planted bamboo removed from map after transformation
- Test bamboo tree added to map after transformation

### Manual Testing Scenarios

1. **Basic Planting Flow**:
   - Start game, collect baby bamboo from destroyed bamboo tree
   - Walk to sand area
   - Select baby bamboo (press "3")
   - Press "p" to plant
   - Verify baby bamboo appears on ground
   - Verify inventory count decreases by 1

2. **Growth and Transformation**:
   - Plant baby bamboo on sand
   - Wait 120 seconds (or adjust timer for testing)
   - Verify baby bamboo disappears
   - Verify bamboo tree appears at same location

3. **Multiple Planting**:
   - Plant baby bamboo on sand tile A
   - Move to adjacent sand tile B
   - Plant another baby bamboo
   - Verify both bamboos grow independently
   - Verify both transform to trees at correct times

4. **Validation Testing**:
   - Try planting on grass (should fail silently)
   - Try planting on occupied sand tile (should fail silently)
   - Try planting with no baby bamboo (should fail silently)
   - Try planting with different item selected (should fail silently)

5. **Edge Case Testing**:
   - Plant bamboo, walk away, verify it still grows
   - Plant multiple bamboos in quick succession
   - Plant bamboo at world boundaries (far from spawn)

## Implementation Notes

### Tile Grid Alignment

The game uses a 64x64 pixel tile grid. To ensure planted bamboos align with tiles:

```java
private float snapToTileGrid(float coordinate) {
    return (float) (Math.floor(coordinate / 64.0) * 64.0);
}
```

This ensures:
- Player at (1234, 5678) plants at tile (1216, 5632)
- Consistent positioning regardless of player's exact position within tile
- Prevents multiple bamboos in same tile

### Growth Timer Implementation

```java
public boolean update(float deltaTime) {
    growthTimer += deltaTime;
    return growthTimer >= GROWTH_DURATION;
}
```

- `deltaTime` is time since last frame (typically 0.016s at 60 FPS)
- Timer accumulates until reaching 120 seconds
- Returns true when transformation should occur

### Biome Integration


Uses existing `BiomeManager.getBiomeAtPosition()`:

```java
BiomeType biome = biomeManager.getBiomeAtPosition(playerX, playerY);
if (biome == BiomeType.SAND) {
    // Valid planting location
}
```

- BiomeManager uses noise-based sand patches
- Sand appears at various distances from spawn (>1000px)
- Deterministic: same coordinates always return same biome

### Texture Reuse

PlantedBamboo reuses the existing BabyBamboo texture loading logic:

```java
private void createTexture() {
    Texture spriteSheet = new Texture("sprites/assets.png");
    Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
    spriteSheet.getTextureData().prepare();
    Pixmap sheetPixmap = spriteSheet.getTextureData().consumePixmap();
    
    // BabyBamboo coordinates: 192 from left, 128 from top, 64x64 size
    pixmap.drawPixmap(sheetPixmap, 0, 0, 192, 128, 64, 64);
    
    texture = new Texture(pixmap);
    pixmap.dispose();
    sheetPixmap.dispose();
    spriteSheet.dispose();
}
```

This ensures visual consistency with baby bamboo items dropped from trees.

### Performance Considerations

- Planted bamboos stored in HashMap for O(1) lookup by tile key
- Update loop iterates all planted bamboos (expected: <100 at any time)
- Transformation list created per frame (expected: 0-2 transformations per frame)
- No performance impact on rendering (simple sprite draw)

### Future Enhancements

**Multiplayer Support**:
- Add `PlantBambooMessage` network message
- Add `BambooGrowthMessage` for synchronization
- Server-authoritative planting validation
- Broadcast planting and growth to all clients

**Persistence**:
- Add planted bamboo data to `WorldSaveData`
- Save: position, growth timer
- Load: recreate PlantedBamboo instances with saved timer

**Visual Feedback**:
- Add planting animation or sound effect
- Add growth progress indicator (visual timer)
- Add particle effects during transformation
- Add notification message for failed planting attempts

**Gameplay Enhancements**:
- Variable growth time based on biome or weather
- Fertilizer items to speed up growth
- Bamboo groves (multiple bamboos grow faster when adjacent)
- Bamboo harvesting before maturity (cancel growth, recover item)
