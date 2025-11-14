# Development Guidelines

## Code Quality Standards

### Documentation Practices
- **Comprehensive Javadoc**: All public classes and methods include detailed Javadoc with purpose, parameters, return values, and usage examples
- **Threading Documentation**: Critical sections include explicit threading model documentation (see MyGdxGame class header)
- **Pattern Documentation**: Complex patterns like deferred operations include extensive inline documentation with correct/incorrect usage examples
- **Requirement Traceability**: Code comments reference requirement IDs (e.g., `// Requirements: 1.1, 1.2`)

### Code Formatting
- **Indentation**: 4 spaces (no tabs)
- **Line Length**: Generally kept under 120 characters, with exceptions for long strings or complex expressions
- **Braces**: Opening brace on same line, closing brace on new line
- **Spacing**: Single space after keywords (if, for, while), no space before opening parenthesis in method calls

### Naming Conventions
- **Classes**: PascalCase (e.g., `MyGdxGame`, `WorldSaveManager`, `LocalizationManager`)
- **Methods**: camelCase (e.g., `updateTreeFromState`, `processPendingPlayerJoins`)
- **Variables**: camelCase (e.g., `worldSeed`, `playerHealth`, `bambooTrees`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `CAMERA_WIDTH`, `SAVE_FILE_EXTENSION`, `PLAYER_ATTACK_COOLDOWN`)
- **Private Fields**: camelCase with descriptive names (e.g., `pendingDeferredOperations`, `lastCactusDamageTime`)

### Structural Conventions
- **Package Organization**: Clear separation by feature (network, world, player, ui, inventory, trees, items)
- **Field Ordering**: Constants first, then instance fields, grouped by functionality
- **Method Ordering**: Public methods first, then private methods, grouped by related functionality
- **Import Organization**: Standard library imports, then third-party (libGDX), then project imports

## Semantic Patterns

### Threading and Concurrency Patterns

#### Deferred Operations Pattern (Critical)
Used extensively for OpenGL operations in multiplayer mode:

```java
// ✅ CORRECT: Defer OpenGL operations to render thread
public void removeItem(String itemId) {
    Apple apple = apples.remove(itemId);  // Immediate state update
    if (apple != null) {
        deferOperation(() -> apple.dispose());  // Deferred texture disposal
    }
}

// ❌ INCORRECT: Direct OpenGL call from network thread
public void removeItem(String itemId) {
    Apple apple = apples.remove(itemId);
    if (apple != null) {
        apple.dispose();  // WILL CRASH - OpenGL call from wrong thread
    }
}
```

**When to Use**:
- Texture disposal (texture.dispose())
- Resource creation from network messages
- Any OpenGL operation triggered by network events

**Implementation**:
- Use `ConcurrentLinkedQueue` for thread-safe operation queuing
- Process queue at start of render loop
- Separate immediate state updates from deferred resource cleanup

#### Concurrent Collections
```java
// Thread-safe collections for multiplayer state
private Map<String, RemotePlayer> remotePlayers = new ConcurrentHashMap<>();
private ConcurrentLinkedQueue<Runnable> pendingDeferredOperations = new ConcurrentLinkedQueue<>();
private Set<String> clearedPositions = ConcurrentHashMap.newKeySet();
```

### Network Synchronization Patterns

#### Server-Authoritative Architecture
```java
// Client sends request
if (gameClient != null && gameClient.isConnected()) {
    gameClient.sendAttackAction(targetKey);
    // Server handles validation and broadcasts result
}

// Server validates and broadcasts
public void handleAttackAction(String clientId, String targetId) {
    // Validate action
    // Apply changes to authoritative state
    // Broadcast to all clients
}
```

#### Message Queuing Pattern
```java
// Network thread queues operations
public void queuePlayerJoin(PlayerJoinMessage message) {
    pendingPlayerJoins.offer(message);
}

// Render thread processes queue
private void processPendingPlayerJoins() {
    PlayerJoinMessage message;
    while ((message = pendingPlayerJoins.poll()) != null) {
        // Safe to create OpenGL resources here
        RemotePlayer player = new RemotePlayer(...);
        remotePlayers.put(playerId, player);
    }
}
```

### State Management Patterns

#### Deterministic World Generation
```java
// CRITICAL: Same seed + coordinates = same result
private void generateTreeAt(int x, int y) {
    // STEP 1: Set deterministic seed
    random.setSeed(worldSeed + x * 31L + y * 17L);
    
    // STEP 2: Check spawn probability
    if (random.nextFloat() < 0.02f) {
        // STEP 3: Query biome (deterministic)
        BiomeType biome = biomeManager.getBiomeAtPosition(x, y);
        
        // STEP 4: Generate tree based on biome
        // Uses SAME seeded random for consistency
    }
}
```

#### Save/Load with Validation
```java
public boolean saveWorld(String saveName, WorldState worldState, ...) {
    // 1. Validate inputs
    if (!isValidSaveName(saveName)) return false;
    if (worldState == null) return false;
    
    // 2. Create backup
    if (saveFile.exists()) {
        Files.copy(saveFile.toPath(), backupFile.toPath(), REPLACE_EXISTING);
    }
    
    // 3. Validate save data
    if (!saveData.isValid()) return false;
    
    // 4. Write atomically
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile))) {
        oos.writeObject(saveData);
    }
    
    return true;
}
```

#### Rollback Pattern
```java
public boolean loadWorldSafe(String saveName) {
    // Create backup before loading
    previousWorldState = extractCurrentWorldState();
    
    try {
        // Attempt load
        boolean success = restoreWorldState(saveData);
        if (!success) {
            rollbackWorldLoad();  // Restore previous state
        }
    } catch (Exception e) {
        rollbackWorldLoad();
    }
}
```

### Resource Management Patterns

#### Disposal Pattern
```java
@Override
public void dispose() {
    // Dispose in reverse order of creation
    batch.dispose();
    shapeRenderer.dispose();
    player.dispose();
    
    // Dispose collections
    for (SmallTree tree : trees.values()) {
        tree.dispose();
    }
    trees.clear();
    
    // Dispose managers
    if (biomeManager != null) {
        biomeManager.dispose();
    }
}
```

#### Texture Management
```java
public class Apple {
    private static Texture sharedTexture;  // Shared across instances
    
    public Apple(float x, float y) {
        if (sharedTexture == null) {
            sharedTexture = new Texture("sprites/apple.png");
        }
    }
    
    public void dispose() {
        // Only dispose if last instance
        // Or use reference counting
    }
}
```

### Input Handling Patterns

#### Toggle Selection Pattern
```java
private void handleInventorySelection() {
    int currentSelection = inventoryManager.getSelectedSlot();
    
    if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
        // Toggle: deselect if already selected
        inventoryManager.setSelectedSlot(currentSelection == 0 ? -1 : 0);
    }
}
```

#### Attack Priority System
```java
private void attackNearbyTargets() {
    // Priority 1: Check for players
    RemotePlayer targetPlayer = findNearestRemotePlayerInRange();
    if (targetPlayer != null) {
        attackPlayer(targetPlayer);
        return;  // Don't attack trees if we attacked a player
    }
    
    // Priority 2: Check for trees
    // Only executed if no player was in range
}
```

### Testing Patterns

#### Mock Setup Pattern
```java
@BeforeEach
public void setUp() {
    mocks = MockitoAnnotations.openMocks(this);
    
    // Setup Gdx mocks
    Gdx.app = mockApp;
    Gdx.files = mock(com.badlogic.gdx.Files.class);
    
    // Setup behavior
    when(mockApp.getPreferences(anyString())).thenReturn(mockPreferences);
}

@AfterEach
public void tearDown() throws Exception {
    if (mocks != null) {
        mocks.close();
    }
    Gdx.app = null;
}
```

#### Test Organization
```java
/**
 * Test 1: Language detection for supported locales
 * Requirements: 1.1, 1.2
 */
@Test
public void testEnglishLocaleDetection() {
    // Setup
    Locale.setDefault(Locale.ENGLISH);
    setupMockLanguageFile("en", createSampleEnglishJson());
    
    // Execute
    localizationManager.initialize();
    
    // Verify
    assertEquals("en", localizationManager.getCurrentLanguage(),
            "Should detect and load English locale");
}
```

## Frequently Used Idioms

### Null Safety Checks
```java
if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
    gameClient.sendPlayerMovement(x, y, direction, isMoving);
}
```

### Collection Iteration with Removal
```java
// Use iterator for safe removal during iteration
Iterator<Map.Entry<String, Apple>> iterator = apples.entrySet().iterator();
while (iterator.hasNext()) {
    Map.Entry<String, Apple> entry = iterator.next();
    if (shouldRemove(entry.getValue())) {
        iterator.remove();
    }
}
```

### Distance Calculation
```java
// Euclidean distance for collision/range checks
float dx = targetX - playerX;
float dy = targetY - playerY;
float distance = (float) Math.sqrt(dx * dx + dy * dy);
if (distance <= ATTACK_RANGE) {
    // In range
}
```

### Delta Time Updates
```java
public void update(float deltaTime) {
    // Use delta time for frame-independent updates
    animTime += deltaTime;
    lastDamageTime += deltaTime;
    
    if (lastDamageTime >= DAMAGE_INTERVAL) {
        applyDamage();
        lastDamageTime = 0;
    }
}
```

## Common Annotations

### Serialization
```java
public class WorldState implements Serializable {
    private static final long serialVersionUID = 1L;
    // Serializable fields
}
```

### Testing
```java
@BeforeEach
public void setUp() { }

@AfterEach  
public void tearDown() { }

@Test
public void testFeature() { }

@Mock
private Application mockApp;
```

### Override
```java
@Override
public void render() { }

@Override
public void dispose() { }
```

## Error Handling Patterns

### Try-Catch with Logging
```java
try {
    // Risky operation
    saveWorld(saveName, worldState);
} catch (IOException e) {
    System.err.println("Failed to save world: " + e.getMessage());
    e.printStackTrace();
    return false;
} catch (Exception e) {
    System.err.println("Unexpected error: " + e.getMessage());
    return false;
}
```

### Validation Before Operations
```java
public boolean saveWorld(String saveName, ...) {
    // Validate all inputs first
    if (!isValidSaveName(saveName)) {
        System.err.println("Invalid save name: " + saveName);
        return false;
    }
    
    if (worldState == null) {
        System.err.println("Cannot save null world state");
        return false;
    }
    
    // Proceed with operation
}
```

### Graceful Degradation
```java
try {
    biomeManager = new BiomeManager();
    biomeManager.initialize();
    useBiomeAwareness = true;
} catch (Exception e) {
    System.err.println("BiomeManager failed, using fallback: " + e.getMessage());
    useBiomeAwareness = false;
    biomeManager = null;
}
```

## Performance Considerations

### Chunk-Based Rendering
```java
// Only render entities near camera
float camX = camera.position.x;
float camY = camera.position.y;
float viewWidth = viewport.getWorldWidth();
float viewHeight = viewport.getWorldHeight();

for (Tree tree : trees.values()) {
    if (Math.abs(tree.getX() - camX) < viewWidth && 
        Math.abs(tree.getY() - camY) < viewHeight) {
        batch.draw(tree.getTexture(), tree.getX(), tree.getY());
    }
}
```

### Spatial Partitioning
```java
// Use grid-based keys for efficient lookup
String key = x + "," + y;
if (!trees.containsKey(key)) {
    // Generate tree at this position
}
```

### Batch Operations
```java
// Process all pending operations in one frame
Runnable operation;
while ((operation = pendingDeferredOperations.poll()) != null) {
    operation.run();
}
```

## Security Patterns

### Input Validation
```java
private static final Pattern VALID_SAVE_NAME_PATTERN = 
    Pattern.compile("^[a-zA-Z0-9\\s\\-_]{1,50}$");

public static boolean isValidSaveName(String saveName) {
    if (saveName == null || saveName.trim().isEmpty()) {
        return false;
    }
    
    // Check pattern
    if (!VALID_SAVE_NAME_PATTERN.matcher(saveName).matches()) {
        return false;
    }
    
    // Check for directory traversal
    if (saveName.contains("..") || saveName.contains("/")) {
        return false;
    }
    
    return true;
}
```

### Path Sanitization
```java
private static File getSaveFile(String saveName, boolean isMultiplayer) {
    // Always construct paths programmatically
    File saveDir = getSaveDirectory(isMultiplayer);
    return new File(saveDir, saveName + SAVE_FILE_EXTENSION);
}
```

## Multiplayer Best Practices

1. **Always validate on server**: Never trust client input
2. **Use deferred operations**: For any OpenGL calls from network thread
3. **Separate state updates from rendering**: State changes immediate, rendering deferred
4. **Implement rollback**: For failed operations that modify state
5. **Log extensively**: Network operations need detailed logging for debugging
6. **Handle disconnections gracefully**: Clean up resources, notify other clients
7. **Synchronize deterministically**: Use seeds for world generation consistency

## Code Review Checklist

- [ ] All public methods have Javadoc
- [ ] Threading concerns documented for concurrent code
- [ ] OpenGL operations properly deferred in multiplayer code
- [ ] Input validation for all external data
- [ ] Proper resource disposal in dispose() methods
- [ ] Error handling with meaningful messages
- [ ] Tests for new functionality
- [ ] No hardcoded paths or magic numbers
- [ ] Consistent naming conventions
- [ ] Requirement IDs referenced in comments
