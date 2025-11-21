# Design Document: Rain Water Puddles

## Overview

This feature adds visual water puddles that appear on the ground during rain events. Puddles accumulate after 5 seconds of continuous rain and evaporate over 5 seconds after rain stops. The system integrates with the existing `DynamicRainManager` and uses similar rendering patterns to the `RainRenderer` for consistency.

The puddle system follows the same architectural patterns as the existing rain system:
- Object pooling for performance
- Configuration-driven parameters
- Integration with the game's render loop
- Clean separation of concerns (manager, renderer, particle/puddle objects)

## Architecture

The puddle system consists of three main components:

1. **PuddleManager**: Manages puddle lifecycle, timing, and state transitions
2. **PuddleRenderer**: Handles rendering of puddles using ShapeRenderer
3. **WaterPuddle**: Represents individual puddle instances (pooled objects)
4. **PuddleConfig**: Centralized configuration for all puddle parameters

### Component Interaction

```
RainSystem
    └─> DynamicRainManager (existing)
            └─> provides rain state
                    ↓
            PuddleManager
                ├─> manages puddle lifecycle
                ├─> tracks accumulation/evaporation timing
                └─> PuddleRenderer
                        └─> renders WaterPuddle instances
```

### Integration Points

- **DynamicRainManager**: Provides rain state (isRaining, duration)
- **RainSystem**: Coordinates updates and rendering
- **ShapeRenderer**: Shared rendering resource
- **OrthographicCamera**: Provides viewport information for puddle placement

## Components and Interfaces

### PuddleManager

**Responsibilities:**
- Track rain duration to determine when to spawn puddles
- Manage puddle accumulation (5-second threshold)
- Manage puddle evaporation (5-second fade-out)
- Coordinate with PuddleRenderer for rendering
- Handle state transitions (no rain → accumulating → active → evaporating → no puddles)

**Key Methods:**
```java
public void update(float deltaTime, boolean isRaining, float rainDuration, OrthographicCamera camera)
public void render(OrthographicCamera camera)
public void dispose()
public PuddleState getCurrentState()
public float getAccumulationProgress() // 0.0 to 1.0
public float getEvaporationProgress() // 0.0 to 1.0
```

**State Machine:**
- `NONE`: No puddles, rain not active
- `ACCUMULATING`: Rain active but < 5 seconds
- `ACTIVE`: Puddles visible, rain active
- `EVAPORATING`: Rain stopped, puddles fading

### PuddleRenderer

**Responsibilities:**
- Maintain pool of WaterPuddle objects
- Spawn puddles within camera viewport
- Update puddle alpha during evaporation
- Render puddles using ShapeRenderer
- Manage puddle density based on rain intensity

**Key Methods:**
```java
public void initialize()
public void spawnPuddles(OrthographicCamera camera, int count)
public void updatePuddles(float deltaTime, float alphaMultiplier)
public void render(OrthographicCamera camera)
public void clearAllPuddles()
public void dispose()
public int getActivePuddleCount()
```

### WaterPuddle

**Responsibilities:**
- Store puddle position, size, and visual properties
- Support object pooling (reset/reuse)
- Track active state

**Properties:**
```java
private float x, y              // Position
private float width, height     // Size (ellipse dimensions)
private float baseAlpha         // Base transparency
private float rotation          // Slight rotation for variety
private boolean active          // Pool management
```

**Key Methods:**
```java
public void reset(float x, float y, float width, float height, float rotation)
public void setActive(boolean active)
public boolean isActive()
public boolean isInViewport(OrthographicCamera camera)
```

### PuddleConfig

**Configuration Parameters:**
```java
// Timing
public static final float ACCUMULATION_THRESHOLD = 5.0f;  // Seconds before puddles appear
public static final float EVAPORATION_DURATION = 5.0f;    // Seconds to fade out

// Density
public static final int MAX_PUDDLES = 30;                 // Maximum puddles on screen
public static final int MIN_PUDDLES = 15;                 // Minimum puddles at low intensity

// Visual Properties
public static final float MIN_PUDDLE_WIDTH = 20.0f;       // Pixels
public static final float MAX_PUDDLE_WIDTH = 50.0f;       // Pixels
public static final float MIN_PUDDLE_HEIGHT = 15.0f;      // Pixels
public static final float MAX_PUDDLE_HEIGHT = 35.0f;      // Pixels
public static final float PUDDLE_ASPECT_RATIO = 1.5f;     // Width/height ratio

// Color (blue-gray water)
public static final float PUDDLE_COLOR_RED = 0.4f;
public static final float PUDDLE_COLOR_GREEN = 0.5f;
public static final float PUDDLE_COLOR_BLUE = 0.7f;
public static final float PUDDLE_BASE_ALPHA = 0.4f;       // Base transparency
public static final float PUDDLE_MAX_ALPHA = 0.6f;        // Maximum transparency

// Spacing
public static final float MIN_PUDDLE_SPACING = 80.0f;     // Minimum distance between puddles
```

## Data Models

### PuddleState Enum
```java
public enum PuddleState {
    NONE,           // No puddles, no rain
    ACCUMULATING,   // Rain active, waiting for threshold
    ACTIVE,         // Puddles visible, rain active
    EVAPORATING     // Rain stopped, puddles fading
}
```

### WaterPuddle Class
```java
public class WaterPuddle {
    private float x;
    private float y;
    private float width;
    private float height;
    private float baseAlpha;
    private float rotation;
    private boolean active;
    
    // Pooling and rendering methods
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Accumulation threshold timing
*For any* rain event, puddles should only become visible after rain has been continuously active for at least 5 seconds.
**Validates: Requirements 1.1**

### Property 2: Puddle stability during rain
*For any* active rain period after accumulation, the set of puddle positions should remain stable (puddles don't randomly disappear or teleport).
**Validates: Requirements 1.2**

### Property 3: Intensity affects visibility
*For any* two rain intensity values A and B where A > B, the puddle visibility (alpha or count) at intensity A should be greater than or equal to that at intensity B.
**Validates: Requirements 1.3**

### Property 4: Viewport containment
*For any* camera position, all rendered puddles should have positions within the camera's viewport bounds.
**Validates: Requirements 1.4**

### Property 5: Evaporation timing
*For any* rain stop event, puddles should take approximately 5 seconds (±0.5s) to completely fade out.
**Validates: Requirements 2.1**

### Property 6: Monotonic alpha decrease
*For any* evaporation sequence, puddle alpha values sampled at times t1 < t2 < t3 should satisfy alpha(t1) ≥ alpha(t2) ≥ alpha(t3).
**Validates: Requirements 2.2**

### Property 7: Complete cleanup after evaporation
*For any* evaporation cycle, after the evaporation duration completes, the active puddle count should be zero.
**Validates: Requirements 2.3**

### Property 8: Puddle count correlates with intensity
*For any* stable rain intensity value, the active puddle count should remain within a consistent range over time.
**Validates: Requirements 3.3**

### Property 9: Camera movement updates puddles
*For any* camera movement, after the update cycle, all visible puddles should be within the new viewport bounds.
**Validates: Requirements 3.4**

### Property 10: Configuration affects new puddles
*For any* configuration change to puddle size, newly spawned puddles should reflect the new size values.
**Validates: Requirements 5.2**

## Error Handling

### Invalid State Transitions
- If rain stops during accumulation (before 5 seconds), transition directly to NONE state without spawning puddles
- If rain restarts during evaporation, transition back to ACTIVE state and restore full alpha

### Resource Exhaustion
- If puddle pool is exhausted, log warning but continue operation
- Implement pool size monitoring to detect if MAX_PUDDLES is too low

### Camera Edge Cases
- Handle camera zoom changes by recalculating viewport bounds
- Handle very small viewports (< 100px) by reducing puddle count proportionally

### Performance Degradation
- If frame rate drops below threshold, reduce MAX_PUDDLES dynamically
- Provide configuration option to disable puddles entirely

## Testing Strategy

### Unit Testing
The unit tests will verify specific examples and edge cases:

- **State transitions**: Test transitions between NONE → ACCUMULATING → ACTIVE → EVAPORATING
- **Edge case**: Rain stops before 5-second threshold (no puddles spawn)
- **Edge case**: Rain restarts during evaporation (puddles restore)
- **Configuration loading**: Verify PuddleConfig values are accessible
- **Resource cleanup**: Verify dispose() clears all puddles

### Property-Based Testing
Property-based tests will verify universal properties across many random inputs using **QuickCheck for Java** (or similar library like **jqwik**). Each test will run a minimum of 100 iterations.

Each property-based test will be tagged with a comment explicitly referencing the correctness property:
- Format: `// Feature: rain-water-puddles, Property {number}: {property_text}`

Property tests to implement:
1. **Accumulation threshold timing** - Generate random rain durations, verify puddles only appear after 5s
2. **Puddle stability** - Track puddle positions over time during rain, verify no unexpected changes
3. **Intensity affects visibility** - Generate random intensity pairs, verify monotonic relationship
4. **Viewport containment** - Generate random camera positions, verify all puddles in bounds
5. **Evaporation timing** - Measure actual evaporation duration across random scenarios
6. **Monotonic alpha decrease** - Sample alpha at multiple times, verify decreasing sequence
7. **Complete cleanup** - Verify zero active puddles after evaporation completes
8. **Puddle count stability** - Verify consistent count for stable intensity over time
9. **Camera movement** - Generate random camera movements, verify puddle updates
10. **Configuration effects** - Change config values, verify new puddles use new values

### Integration Testing
- Test puddle system with actual DynamicRainManager
- Verify rendering order (puddles above ground, below player)
- Test performance with MAX_PUDDLES active
- Verify multiplayer synchronization (if needed)

## Implementation Notes

### Rendering Order
Puddles should be rendered in this sequence within the game loop:
1. Ground/terrain rendering
2. **Puddle rendering** ← Insert here
3. Player and object rendering
4. UI rendering

### Performance Considerations
- Use object pooling (similar to RainParticle)
- Limit puddle count based on viewport size
- Consider spatial hashing if puddle count increases significantly
- Render puddles as simple ellipses (ShapeRenderer.ellipse) for performance

### Visual Design
- Puddles should be subtle (low alpha) to avoid visual clutter
- Use slight rotation variation for natural appearance
- Consider adding a darker outline for better visibility
- Puddles should be elliptical (wider than tall) for realistic water pooling

### Future Enhancements
- Add ripple effects when rain hits puddles
- Implement puddle merging (nearby puddles combine)
- Add reflection effects for nearby objects
- Support different ground types (puddles only on certain terrain)
- Add sound effects (splashing when puddles form)
