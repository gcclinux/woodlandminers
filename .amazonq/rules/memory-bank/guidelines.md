# Woodlanders - Development Guidelines

## Code Quality Standards

### Formatting and Structure
- **Indentation**: 4 spaces (no tabs)
- **Line Length**: No strict limit, but keep lines readable (typically under 120 characters)
- **Braces**: Opening brace on same line (K&R style)
- **Spacing**: Space after keywords (if, for, while), no space before method parentheses
- **Blank Lines**: Single blank line between methods, two blank lines between classes

### Naming Conventions
- **Classes**: PascalCase (e.g., `MyGdxGame`, `WorldState`, `PlayerConfig`)
- **Methods**: camelCase (e.g., `updateCamera()`, `checkCactusDamage()`, `isValidSaveName()`)
- **Variables**: camelCase (e.g., `playerX`, `worldSeed`, `bambooTrees`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `CAMERA_WIDTH`, `SAVE_FILE_EXTENSION`, `HUNGER_INTERVAL`)
- **Private Fields**: camelCase with no prefix (e.g., `health`, `gameClient`, `trees`)
- **Boolean Methods**: Prefix with `is`, `has`, `should`, or `can` (e.g., `isMoving()`, `hasInventory()`, `shouldShowHealthBar()`)

### Documentation Standards
- **Class-Level Javadoc**: Required for all public classes, describing purpose and key responsibilities
- **Method-Level Javadoc**: Required for public methods, especially complex or critical ones
- **Inline Comments**: Used sparingly for complex logic, threading concerns, or non-obvious behavior
- **Threading Documentation**: Extensive documentation for multi-threaded code (see MyGdxGame class)
- **Parameter Documentation**: `@param` tags for all parameters in public methods
- **Return Documentation**: `@return` tags for all non-void public methods

### Code Organization
- **Package Structure**: Organized by feature/component (gdx, player, network, inventory, etc.)
- **Single Responsibility**: Each class has a clear, focused purpose
- **Separation of Concerns**: UI, game logic, networking, and persistence are separate
- **Dependency Injection**: Dependencies passed via constructors or setters (e.g., `setTrees()`, `setGameClient()`)

## Architectural Patterns

### Client-Server Architecture
**Pattern**: Server-authoritative with client prediction
```java
// Client sends action request
gameClient.sendAttackAction(targetKey);

// Server validates and broadcasts result
// Client updates local state based on server response
```

**Key Principles**:
- Server is the source of truth for all game state
- Clients predict local actions for responsiveness
- Server validates all actions and broadcasts authoritative updates
- Clients reconcile predictions with server state

### Entity-Component Pattern
**Pattern**: Entities with behavior methods
```java
public class SmallTree {
    private float x, y;
    private float health = 100.0f;
    
    public boolean attack() {
        health -= 10;
        return health <= 0;
    }
    
    public boolean collidesWith(float px, float py, float pw, float ph) {
        // Collision detection logic
    }
}
```

**Key Principles**:
- Entities encapsulate position, state, and behavior
- Collision detection is entity-specific
- Health management is internal to entities
- Entities provide query methods for state

### Message-Based Communication
**Pattern**: Typed message objects with handlers
```java
public class PlayerMovementMessage extends NetworkMessage {
    private String playerId;
    private float x, y;
    private Direction direction;
    private boolean isMoving;
    
    // Constructor, getters, serialization
}

// Handler processes message
public void handlePlayerMovement(PlayerMovementMessage message) {
    RemotePlayer player = remotePlayers.get(message.getPlayerId());
    if (player != null) {
        player.updatePosition(message.getX(), message.getY());
    }
}
```

**Key Principles**:
- Each message type extends `NetworkMessage`
- Messages are immutable after construction
- Handlers are responsible for state updates
- 22+ message types for comprehensive synchronization

### Deferred Operations Pattern
**Pattern**: Queue operations for render thread execution
```java
// Network thread receives message
public void handleItemPickup(ItemPickupMessage message) {
    // Immediate state update (thread-safe)
    Apple apple = apples.remove(message.getItemId());
    
    if (apple != null) {
        // Defer OpenGL operation to render thread
        deferOperation(() -> apple.dispose());
    }
}

// Render thread processes queue
public void render() {
    Runnable operation;
    while ((operation = pendingDeferredOperations.poll()) != null) {
        operation.run();
    }
    // ... rest of rendering
}
```

**Key Principles**:
- All OpenGL operations MUST execute on render thread
- State updates happen immediately for responsiveness
- Resource disposal is deferred to avoid threading issues
- Uses `ConcurrentLinkedQueue` for thread-safe queuing

### Manager Pattern
**Pattern**: Centralized control for major systems
```java
public class InventoryManager {
    private Inventory singleplayerInventory;
    private Inventory multiplayerInventory;
    private int selectedSlot = -1;
    
    public void collectItem(ItemType itemType) {
        Inventory current = getCurrentInventory();
        // Add to appropriate inventory
    }
    
    public void setMultiplayerMode(boolean isMultiplayer) {
        // Switch between inventories
    }
}
```

**Key Principles**:
- Managers own and coordinate related state
- Provide high-level operations for subsystems
- Handle mode switching (singleplayer/multiplayer)
- Examples: `InventoryManager`, `BiomeManager`, `RespawnManager`, `WorldSaveManager`

## Common Implementation Patterns

### Deterministic World Generation
**Pattern**: Seeded random generation for consistency
```java
private void generateTreeAt(int x, int y) {
    String key = x + "," + y;
    
    // STEP 1: Set deterministic seed
    random.setSeed(worldSeed + x * 31L + y * 17L);
    
    // STEP 2: Check spawn probability
    if (random.nextFloat() < 0.02f) {
        // STEP 3: Query biome (deterministic)
        BiomeType biome = biomeManager.getBiomeAtPosition(x, y);
        
        // STEP 4: Generate tree based on biome
        TreeType treeType = selectTreeType(biome, random);
        
        // STEP 5: Create tree state
        TreeState tree = new TreeState(key, treeType, x, y, 100.0f, true);
        trees.put(key, tree);
    }
}
```

**Key Principles**:
- Same seed + coordinates = same result
- Biome queries are deterministic (no randomness)
- All clients generate identical worlds
- Server and clients use same algorithm

### Collision Detection
**Pattern**: Entity-specific hitboxes with center-based checks
```java
public boolean collidesWith(float px, float py, float pw, float ph) {
    // Calculate centers
    float treeCenterX = x + 32;
    float treeCenterY = y + 64;
    float playerCenterX = px + 32;
    float playerCenterY = py + 32;
    
    // Check distance thresholds
    float dx = Math.abs(treeCenterX - playerCenterX);
    float dy = Math.abs(treeCenterY - playerCenterY);
    
    return dx <= 64 && dy <= 96;
}
```

**Key Principles**:
- Center-based distance calculations
- Entity-specific collision boxes
- Separate thresholds for X and Y axes
- Used for movement blocking and attack range

### State Synchronization
**Pattern**: Snapshot and delta updates
```java
// Full state snapshot for new clients
public WorldState createSnapshot() {
    WorldState snapshot = new WorldState(this.worldSeed);
    
    // Deep copy all entities
    for (Map.Entry<String, TreeState> entry : this.trees.entrySet()) {
        TreeState copy = new TreeState(/* ... */);
        snapshot.trees.put(entry.getKey(), copy);
    }
    
    return snapshot;
}

// Delta updates for existing clients
public WorldStateUpdate getDeltaSince(long timestamp) {
    Map<String, PlayerState> updatedPlayers = new HashMap<>();
    
    for (Map.Entry<String, PlayerState> entry : this.players.entrySet()) {
        if (entry.getValue().getLastUpdateTime() > timestamp) {
            updatedPlayers.put(entry.getKey(), entry.getValue());
        }
    }
    
    return new WorldStateUpdate(updatedPlayers, /* ... */);
}
```

**Key Principles**:
- Full snapshots for initial synchronization
- Delta updates for ongoing synchronization
- Timestamp-based change tracking
- Deep copies to prevent mutation issues

### Save/Load System
**Pattern**: Serialization with validation and backup
```java
public static boolean saveWorld(String saveName, WorldState worldState, /* ... */) {
    // Validate inputs
    if (!isValidSaveName(saveName)) {
        return false;
    }
    
    // Create backup of existing save
    if (saveFile.exists()) {
        Files.copy(saveFile.toPath(), backupFile.toPath(), REPLACE_EXISTING);
    }
    
    // Create save data
    WorldSaveData saveData = new WorldSaveData(/* ... */);
    
    // Validate before writing
    if (!saveData.isValid()) {
        return false;
    }
    
    // Write to file
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile))) {
        oos.writeObject(saveData);
    }
    
    return true;
}
```

**Key Principles**:
- Validate all inputs before processing
- Create backups before overwriting
- Validate data before writing
- Separate singleplayer/multiplayer saves
- OS-specific save directories

### Inventory Management
**Pattern**: Dual inventory with mode switching
```java
public class InventoryManager {
    private Inventory singleplayerInventory;
    private Inventory multiplayerInventory;
    private boolean isMultiplayerMode;
    
    public Inventory getCurrentInventory() {
        return isMultiplayerMode ? multiplayerInventory : singleplayerInventory;
    }
    
    public void setMultiplayerMode(boolean isMultiplayer) {
        this.isMultiplayerMode = isMultiplayer;
        // Switch active inventory
    }
}
```

**Key Principles**:
- Separate inventories for SP/MP
- Mode switching preserves both inventories
- Network synchronization in MP mode
- Auto-deselection when items depleted

## Threading and Concurrency

### Thread Safety Rules
1. **OpenGL Context**: All OpenGL operations on render thread only
2. **State Updates**: Immediate updates on calling thread (thread-safe collections)
3. **Resource Disposal**: Always defer to render thread via `deferOperation()`
4. **Network Messages**: Processed on network thread, deferred operations for graphics

### Thread-Safe Collections
```java
// Use ConcurrentHashMap for shared state
private Map<String, RemotePlayer> remotePlayers = new ConcurrentHashMap<>();
private Set<String> clearedPositions = ConcurrentHashMap.newKeySet();

// Use ConcurrentLinkedQueue for message passing
private ConcurrentLinkedQueue<Runnable> pendingDeferredOperations = new ConcurrentLinkedQueue<>();
```

### Deferred Operation Usage
**When to Use**:
- Texture disposal (`texture.dispose()`)
- Sprite disposal (`sprite.dispose()`)
- Any OpenGL state changes
- Resource creation from network messages

**When NOT to Use**:
- Map operations (add/remove from collections)
- Logging or printing
- Network operations
- Pure data processing

## Error Handling

### Validation Pattern
```java
public boolean saveWorld(String saveName, /* ... */) {
    // Validate inputs
    if (!isValidSaveName(saveName)) {
        System.err.println("Invalid save name: " + saveName);
        return false;
    }
    
    if (worldState == null) {
        System.err.println("Cannot save null world state");
        return false;
    }
    
    // Proceed with operation
    try {
        // ... save logic
        return true;
    } catch (IOException e) {
        System.err.println("Failed to save world: " + e.getMessage());
        return false;
    }
}
```

### Rollback Pattern
```java
public boolean restoreWorldState(WorldSaveData saveData) {
    // Create rollback point
    WorldState rollbackState = createRollbackPoint();
    
    try {
        // Attempt restoration
        cleanupExistingState();
        applyNewState(saveData);
        
        if (!validateRestoredState()) {
            throw new Exception("Validation failed");
        }
        
        return true;
    } catch (Exception e) {
        // Rollback on failure
        rollbackToState(rollbackState);
        return false;
    }
}
```

## Performance Optimizations

### Chunk-Based Rendering
```java
private void drawInfiniteGrass() {
    float camX = camera.position.x;
    float camY = camera.position.y;
    float viewWidth = viewport.getWorldWidth();
    float viewHeight = viewport.getWorldHeight();
    
    // Only render visible area
    int startX = (int)((camX - viewWidth) / 64) * 64;
    int endX = (int)((camX + viewWidth) / 64) * 64 + 64;
    
    for (int x = startX; x <= endX; x += 64) {
        // Render only visible tiles
    }
}
```

### Viewport Culling
```java
private void drawTrees() {
    float camX = camera.position.x;
    float camY = camera.position.y;
    float viewWidth = viewport.getWorldWidth();
    float viewHeight = viewport.getWorldHeight();
    
    for (SmallTree tree : trees.values()) {
        // Only draw trees near camera
        if (Math.abs(tree.getX() - camX) < viewWidth && 
            Math.abs(tree.getY() - camY) < viewHeight) {
            batch.draw(tree.getTexture(), tree.getX(), tree.getY());
        }
    }
}
```

### Object Reuse
```java
// Reuse texture instances
private static Texture sharedTexture;

public static void initializeSharedTexture() {
    if (sharedTexture == null) {
        sharedTexture = new Texture("path/to/texture.png");
    }
}

public static void disposeSharedTexture() {
    if (sharedTexture != null) {
        sharedTexture.dispose();
        sharedTexture = null;
    }
}
```

## Testing Patterns

### Unit Test Structure
```java
@Test
public void testInventoryAddItem() {
    // Arrange
    Inventory inventory = new Inventory();
    
    // Act
    inventory.addApple(5);
    
    // Assert
    assertEquals(5, inventory.getAppleCount());
}
```

### Mock Usage
```java
@Test
public void testPlayerMovement() {
    // Mock dependencies
    OrthographicCamera mockCamera = mock(OrthographicCamera.class);
    
    // Create player with mock
    Player player = new Player(0, 0, mockCamera);
    
    // Test behavior
    player.update(0.016f);
    
    // Verify interactions
    verify(mockCamera, times(1)).update();
}
```

## Common Idioms

### Null Safety
```java
// Check for null before operations
if (gameClient != null && gameClient.isConnected()) {
    gameClient.sendMessage(message);
}

// Use null-safe getters
public GameClient getGameClient() {
    return gameClient; // May return null
}
```

### Clamping Values
```java
// Clamp health between 0 and 100
public void setHealth(float health) {
    this.health = Math.max(0, Math.min(100, health));
}

// Clamp inventory counts to non-negative
public void setAppleCount(int count) {
    this.appleCount = Math.max(0, count);
}
```

### Delta Time Usage
```java
public void update(float deltaTime) {
    // Use delta time for frame-independent movement
    float newX = x + speed * deltaTime;
    
    // Use delta time for timers
    hungerTimer += deltaTime;
    if (hungerTimer >= HUNGER_INTERVAL) {
        hunger += 1;
        hungerTimer -= HUNGER_INTERVAL;
    }
}
```

### Resource Disposal
```java
@Override
public void dispose() {
    // Dispose all resources in reverse order of creation
    if (batch != null) batch.dispose();
    if (shapeRenderer != null) shapeRenderer.dispose();
    if (player != null) player.dispose();
    
    // Dispose collections
    for (SmallTree tree : trees.values()) {
        tree.dispose();
    }
    trees.clear();
    
    // Dispose shared resources last
    PlantedBamboo.disposeSharedTexture();
}
```

## Frequently Used Annotations

This codebase does not use annotations extensively. The primary annotations are:
- `@Override` - Used consistently for overridden methods
- `@Test` - JUnit test methods
- No custom annotations defined

## Best Practices Summary

1. **Always validate inputs** before processing
2. **Use deferred operations** for OpenGL calls from non-render threads
3. **Prefer composition over inheritance** (manager pattern)
4. **Document threading concerns** extensively
5. **Separate singleplayer and multiplayer state** where needed
6. **Use deterministic algorithms** for world generation
7. **Validate and backup** before destructive operations
8. **Clamp values** to prevent invalid state
9. **Use delta time** for frame-independent behavior
10. **Dispose resources** properly in reverse order
