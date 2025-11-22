# Design Document

## Overview

The flying birds ambient system adds atmospheric life to the game world by periodically spawning formations of 5 birds that fly across the screen in a V-shape pattern. Birds appear at random intervals (3-5 minutes), spawn from random screen edges, fly across the viewport, and despawn when they reach the opposite edge. The system is designed to be lightweight, non-intrusive, and visually appealing.

## Architecture

The system follows a component-based architecture integrated into the existing game loop:

```
MyGdxGame (render loop)
    └── BirdFormationManager
        ├── SpawnTimer (manages spawn intervals)
        ├── BirdFormation (active formation)
        │   ├── Bird (x5 entities)
        │   └── FlightPath (trajectory data)
        └── BirdRenderer (draws birds on top layer)
```

### Integration Points

- **MyGdxGame.create()**: Initialize BirdFormationManager
- **MyGdxGame.update()**: Update bird positions and spawn timer
- **MyGdxGame.render()**: Render birds after all other game objects (top layer)
- **MyGdxGame.dispose()**: Clean up bird textures and resources

## Components and Interfaces

### BirdFormationManager

Central manager class that coordinates spawning, updating, and rendering of bird formations.

```java
public class BirdFormationManager {
    private BirdFormation activeFormation;
    private float spawnTimer;
    private float nextSpawnInterval;
    private Random random;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpawnBoundary lastSpawnBoundary;
    
    public void initialize();
    public void update(float deltaTime, float playerX, float playerY);
    public void render(SpriteBatch batch);
    public void dispose();
    
    private void spawnFormation();
    private float generateRandomInterval(); // Returns 180-300 seconds
    private SpawnPoint selectRandomSpawnPoint();
}
```

### BirdFormation

Represents an active formation of 5 birds flying across the screen.

```java
public class BirdFormation {
    private List<Bird> birds; // Always 5 birds
    private Vector2 velocity;
    private SpawnBoundary spawnBoundary;
    private SpawnBoundary targetBoundary;
    private boolean active;
    
    public BirdFormation(SpawnPoint spawnPoint, Vector2 velocity);
    public void update(float deltaTime);
    public void render(SpriteBatch batch);
    public boolean hasReachedTarget(float viewWidth, float viewHeight);
    public void dispose();
    
    private void initializeVFormation(SpawnPoint spawnPoint);
}
```

### Bird

Individual bird entity with position and animation state.

```java
public class Bird {
    private float x, y;
    private Texture texture;
    private float animationTime;
    
    public Bird(float x, float y);
    public void update(float deltaTime, Vector2 velocity);
    public void render(SpriteBatch batch);
    public void dispose();
}
```

### SpawnPoint

Data class representing a spawn location and direction.

```java
public class SpawnPoint {
    public SpawnBoundary boundary; // TOP, BOTTOM, LEFT, RIGHT
    public float x, y; // Spawn coordinates
    public Vector2 direction; // Normalized flight direction
}
```

### SpawnBoundary

Enum representing screen edges.

```java
public enum SpawnBoundary {
    TOP, BOTTOM, LEFT, RIGHT
}
```

## Data Models

### Bird Formation Layout

V-formation with consistent spacing:

```
        Bird 1 (lead)
       /           \
    Bird 2         Bird 3
   /                     \
Bird 4                   Bird 5
```

- Lead bird at apex
- 2 birds on left wing, 2 on right wing
- Spacing: 40 pixels between birds (diagonal distance)
- Formation angle: 30 degrees from center line

### Spawn Mechanics

**Spawn Interval**: Random value between 180-300 seconds (3-5 minutes)

**Spawn Position Selection**:
1. Randomly select boundary (TOP, BOTTOM, LEFT, RIGHT)
2. For vertical boundaries (LEFT/RIGHT): Random Y within viewport height
3. For horizontal boundaries (TOP/BOTTOM): Random X within viewport width
4. Ensure variation from previous spawn

**Flight Path**:
- Direction: From spawn boundary toward opposite boundary
- Speed: 100 pixels/second (constant)
- Trajectory: Straight line across screen

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Spawn interval bounds

*For any* spawn event, the time since the last spawn should be between 180 and 300 seconds (inclusive)
**Validates: Requirements 1.1**

### Property 2: Spawn position on boundary

*For any* bird formation spawn, the spawn position should be located on one of the four screen boundaries (within 1 pixel tolerance)
**Validates: Requirements 1.2**

### Property 3: Formation reaches opposite boundary

*For any* bird formation with a given spawn boundary and velocity, updating the formation for sufficient time should result in the formation reaching the opposite boundary
**Validates: Requirements 1.3**

### Property 4: Despawn triggers timer reset

*For any* bird formation that reaches the target boundary, the system should despawn the formation and initialize a new spawn timer
**Validates: Requirements 1.4**

### Property 5: Consecutive spawn variation

*For any* two consecutive spawn events, the spawn boundary or spawn position should differ
**Validates: Requirements 1.5**

### Property 6: Formation contains exactly 5 birds

*For any* spawned bird formation, the formation should contain exactly 5 bird entities
**Validates: Requirements 2.1**

### Property 7: V-shape arrangement

*For any* bird formation, the relative positions of the 5 birds should form a V-shape with one lead bird and two trailing birds on each side
**Validates: Requirements 2.2**

### Property 8: V-shape invariant during flight

*For any* bird formation at any point during its flight, the relative positions of the birds should maintain the V-shape pattern
**Validates: Requirements 2.3**

### Property 9: Consistent bird spacing

*For any* bird formation, the distance between adjacent birds in the formation should remain constant throughout the flight
**Validates: Requirements 2.4**

### Property 10: Boundary selection distribution

*For any* sequence of spawn events, all four boundaries (TOP, BOTTOM, LEFT, RIGHT) should be selected over time
**Validates: Requirements 4.1**

### Property 11: Vertical boundary Y-coordinate variation

*For any* sequence of spawns on vertical boundaries (LEFT or RIGHT), the Y-coordinates should vary across the viewport height
**Validates: Requirements 4.2**

### Property 12: Horizontal boundary X-coordinate variation

*For any* sequence of spawns on horizontal boundaries (TOP or BOTTOM), the X-coordinates should vary across the viewport width
**Validates: Requirements 4.3**

### Property 13: Flight path toward opposite boundary

*For any* bird formation with spawn boundary B, the flight direction should point toward the opposite boundary
**Validates: Requirements 4.4**

### Property 14: No rendering when not visible

*For any* game state where no bird formation is active, no bird rendering operations should be performed
**Validates: Requirements 5.1**

### Property 15: Resource cleanup on despawn

*For any* bird formation that despawns, all associated textures and resources should be properly disposed
**Validates: Requirements 5.4**

## Error Handling

### Texture Loading Failures

- **Issue**: Bird texture file missing or corrupted
- **Handling**: Log error, disable bird system gracefully, continue game without birds
- **Recovery**: Check for texture on next spawn attempt

### Invalid Spawn Positions

- **Issue**: Calculated spawn position outside viewport bounds
- **Handling**: Clamp position to valid boundary coordinates
- **Fallback**: Use center of boundary if clamping fails

### Formation Update Errors

- **Issue**: Exception during bird position update
- **Handling**: Log error, despawn current formation, reset spawn timer
- **Recovery**: Next spawn attempt proceeds normally

## Testing Strategy

### Unit Tests

- Test spawn interval generation (180-300 second range)
- Test spawn position calculation for each boundary
- Test V-formation initialization with correct bird positions
- Test flight path calculation toward opposite boundary
- Test boundary detection for despawning
- Test resource disposal on formation cleanup

### Property-Based Tests

Property-based tests will use Java's jqwik library for property testing. Each test will run a minimum of 100 iterations with randomly generated inputs.

- **Property 1**: Generate random spawn times, verify all are within 180-300 seconds
- **Property 2**: Generate random spawn points, verify all are on screen boundaries
- **Property 3**: Simulate formation flight, verify it reaches opposite boundary
- **Property 4**: Test despawn triggers timer reset correctly
- **Property 5**: Generate consecutive spawns, verify variation in position/boundary
- **Property 6**: Test all formations contain exactly 5 birds
- **Property 7**: Verify V-shape geometry at spawn time
- **Property 8**: Verify V-shape maintained during flight (invariant)
- **Property 9**: Verify consistent spacing throughout flight
- **Property 10**: Test boundary selection distribution over many spawns
- **Property 11**: Test Y-coordinate variation on vertical boundaries
- **Property 12**: Test X-coordinate variation on horizontal boundaries
- **Property 13**: Verify flight direction points to opposite boundary
- **Property 14**: Verify no rendering when formation is null
- **Property 15**: Verify texture disposal on despawn

### Integration Tests

- Test bird system integration with game render loop
- Test birds render on top of all other game objects
- Test bird system performance with multiple game objects
- Test bird spawning during gameplay (player movement, menu open/close)

## Implementation Notes

### Rendering Layer Priority

Birds must render after all other game objects to appear on top. In MyGdxGame.render():

```java
// Existing rendering order:
// 1. Ground/terrain
// 2. Puddles
// 3. Planted bamboos/trees
// 4. Trees, stones, items
// 5. Player and remote players
// 6. Apple/banana trees (foliage)
// 7. Rain effects
// 8. Player name tags
// 9. Health bars
// 10. Compass
// 11. Inventory UI
// 12. Game menu

// NEW: Birds render here (after rain, before UI)
if (birdFormationManager != null) {
    batch.begin();
    birdFormationManager.render(batch);
    batch.end();
}
```

### Performance Considerations

- **Texture Sharing**: All birds share a single texture instance
- **Culling**: Only render birds when formation is active
- **Update Frequency**: Birds update every frame but spawn infrequently
- **Memory**: Minimal footprint (5 birds × 2 floats + shared texture)

### Camera Independence

Birds spawn relative to camera/viewport position, not world coordinates. This ensures they appear regardless of player location in the infinite world.

### Multiplayer Considerations

Birds are client-side only (not synchronized). Each client spawns birds independently based on their local timer. This is acceptable for ambient effects that don't affect gameplay.
