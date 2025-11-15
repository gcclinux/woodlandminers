# Woodlanders - Development Guidelines

## Code Quality Standards

### Naming Conventions
- **Classes**: PascalCase (e.g., `MyGdxGame`, `PlayerState`, `WorldSaveManager`)
- **Methods**: camelCase (e.g., `updateTreeFromState()`, `removeItem()`, `deferOperation()`)
- **Variables**: camelCase (e.g., `playerX`, `treeHealth`, `gameClient`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `CAMERA_WIDTH`, `PLAYER_ATTACK_COOLDOWN`)
- **Packages**: lowercase with dots (e.g., `wagemaker.uk.network`, `wagemaker.uk.player`)

### Code Organization
- One public class per file
- Related classes grouped in same package
- Clear separation of concerns (network, UI, game logic, persistence)
- Logical method ordering: constructors, public methods, private methods, getters/setters

### Documentation Standards
- Comprehensive JavaDoc for public methods and classes
- Inline comments for complex logic and non-obvious implementations
- Parameter descriptions in JavaDoc
- Return value documentation
- Exception documentation where applicable
- Example usage in JavaDoc for complex methods

### Formatting
- 4-space indentation (no tabs)
- 120-character line limit for readability
- Consistent brace placement (opening brace on same line)
- Blank lines between logical sections
- Organized imports (no wildcard imports)

## Architectural Patterns

### Threading Model
**Critical Pattern: Deferred Operations for OpenGL Safety**

The codebase uses a deferred operation pattern for thread-safe OpenGL operations:

```java
// Network thread (unsafe for OpenGL)
Apple apple = apples.remove(itemId);
if (apple != null) {
    // Defer texture disposal to render thread
    deferOperation(() -> apple.dispose());
}

// Render thread processes deferred operations
Runnable operation;
while ((operation = pendingDeferredOperations.poll()) != null) {
    operation.run();
}
```

**Key Points:**
- Immediate state updates on network thread (thread-safe map operations)
- Deferred OpenGL operations to render thread via `deferOperation()`
- Uses `ConcurrentLinkedQueue` for lock-free thread safety
- Prevents OpenGL context violations that cause crashes

### Deterministic World Generation
**Pattern: Seeded Random for Multiplayer Consistency**

All world generation uses deterministic seeding to ensure clients generate identical worlds:

```java
// Set deterministic seed based on world seed + position
random.setSeed(worldSeed + x * 31L + y * 17L);

// Use seeded random for all generation decisions
if (random.nextFloat() < 0.02f) {
    // Generate tree with consistent probability
}
```

**Key Points:**
- World seed synchronized from server to all clients
- Same coordinates always produce same tree types
- Biome-aware generation (bamboo in sand, other trees in grass)
- Eliminates need for per-tree network synchronization

### Message-Driven Networking
**Pattern: Type-Safe Network Messages**

Network communication uses strongly-typed message classes:

```java
// Message types: PlayerMovementMessage, TreeHealthUpdateMessage, etc.
public class PlayerMovementMessage extends NetworkMessage {
    private float x, y;
    private Direction direction;
    private boolean isMoving;
    // Serializable for network transmission
}

// Handler processes messages
gameClient.sendMessage(message);
```

**Key Points:**
- 22+ message types for complete state synchronization
- Server-authoritative validation
- Client prediction for responsiveness
- Heartbeat/ping for connection monitoring

### Separate State Management
**Pattern: Singleplayer vs Multiplayer Inventories**

Separate inventory systems prevent data corruption:

```java
// Singleplayer: local inventory
Inventory singleplayerInventory = inventoryManager.getSingleplayerInventory();

// Multiplayer: server-synchronized inventory
Inventory multiplayerInventory = inventoryManager.getMultiplayerInventory();

// Switch based on game mode
inventoryManager.setMultiplayerMode(isMultiplayer);
```

**Key Points:**
- Independent position saves for SP/MP
- Separate inventory collections
- Prevents accidental data mixing
- Allows seamless mode switching

## Common Code Patterns

### Entity Lifecycle Management
**Pattern: Proper Resource Disposal**

All entities with textures follow disposal pattern:

```java
// Creation
Apple apple = new Apple(x, y);
apples.put(itemId, apple);

// Removal with deferred disposal
Apple removed = apples.remove(itemId);
if (removed != null) {
    deferOperation(() -> removed.dispose());
}

// Cleanup on game exit
for (Apple apple : apples.values()) {
    apple.dispose();
}
apples.clear();
```

### Collision Detection
**Pattern: Bounding Box Collision**

Consistent collision checking across all entities:

```java
// Check if entity collides with player
if (tree.collidesWith(playerX, playerY, 64, 64)) {
    // Collision detected
}

// Implementation uses bounding box intersection
private boolean collidesWith(float otherX, float otherY, float otherWidth, float otherHeight) {
    return !(x + width < otherX || x > otherX + otherWidth ||
             y + height < otherY || y > otherY + otherHeight);
}
```

### State Synchronization
**Pattern: Queued Processing on Main Thread**

Network updates queued for main thread processing:

```java
// Network thread: queue update
pendingPlayerJoins.offer(joinMessage);

// Main thread: process queued updates
PlayerJoinMessage message;
while ((message = pendingPlayerJoins.poll()) != null) {
    RemotePlayer remotePlayer = new RemotePlayer(...);
    remotePlayers.put(playerId, remotePlayer);
}
```

### Configuration Management
**Pattern: Centralized Configuration Classes**

Configuration values in dedicated classes:

```java
public class RainConfig {
    public static final float DEFAULT_INTENSITY = 0.5f;
    public static final int SPAWN_ZONE_ID = 0;
    public static final float SPAWN_ZONE_CENTER_X = 0.0f;
    // All configuration in one place
}
```

## Frequently Used Annotations

### Testing Annotations
- `@Test` - JUnit test method
- `@BeforeEach` - Setup before each test
- `@AfterEach` - Cleanup after each test
- `@Order(n)` - Test execution order
- `@Disabled` - Skip test (with reason)

### Mocking Annotations
- `@Mock` - Create mock object
- `@MockitoAnnotations.openMocks()` - Initialize mocks
- `when(...).thenReturn(...)` - Mock behavior
- `verify(...)` - Assert mock was called

## Performance Considerations

### Chunk-Based Rendering
Only render visible chunks to optimize performance:

```java
// Calculate visible area
float viewWidth = viewport.getWorldWidth();
float viewHeight = viewport.getWorldHeight();

// Only render trees near camera
for (SmallTree tree : trees.values()) {
    if (Math.abs(tree.getX() - camX) < viewWidth && 
        Math.abs(tree.getY() - camY) < viewHeight) {
        batch.draw(tree.getTexture(), tree.getX(), tree.getY());
    }
}
```

### Spatial Partitioning
Efficient collision detection with spatial awareness:

```java
// Check collision only with nearby trees
for (SmallTree tree : trees.values()) {
    if (tree.isInAttackRange(playerX, playerY)) {
        // Only check trees in range
    }
}
```

### Delta-Time Based Physics
Frame-rate independent movement:

```java
// Movement independent of frame rate
float newX = x - speed * deltaTime;
if (!wouldCollide(newX, y)) {
    x = newX;
}
```

## Best Practices

### Error Handling
- Use try-catch for resource operations
- Log errors with context information
- Provide fallback behavior when possible
- Clean up resources in finally blocks

### Logging
- Use `System.out.println()` for info
- Use `System.err.println()` for errors
- Include context (player ID, tree ID, etc.)
- Log state transitions and important events

### Testing
- Unit tests for isolated components
- Integration tests for system interactions
- Performance tests for critical paths
- Mock external dependencies

### Code Review Checklist
- [ ] Follows naming conventions
- [ ] Proper JavaDoc documentation
- [ ] Thread-safe for multiplayer
- [ ] Resources properly disposed
- [ ] Error handling implemented
- [ ] Tests included and passing
- [ ] No hardcoded values (use constants)
- [ ] Performance acceptable

## Common Pitfalls to Avoid

1. **OpenGL from Network Thread**: Always defer to render thread
2. **Resource Leaks**: Always dispose textures and resources
3. **Hardcoded Values**: Use constants for configuration
4. **Missing Null Checks**: Validate before using objects
5. **Inconsistent State**: Keep SP/MP inventories separate
6. **Race Conditions**: Use thread-safe collections
7. **Blocking Operations**: Never block render thread
8. **Incomplete Cleanup**: Always clean up in dispose methods
