# Design Document: Home Compass

## Overview

The Home Compass feature adds a persistent navigation UI element that helps players orient themselves relative to the world spawn point (0.0, 0.0). The compass is rendered as a 64x64 pixel graphic in the bottom-left corner of the screen, with a needle that dynamically rotates to point toward the spawn location. This feature works seamlessly in both single-player and multiplayer modes, providing consistent navigation assistance across all game modes.

The implementation leverages the existing libGDX rendering pipeline, camera system, and viewport management already established in the Woodlanders codebase. The compass will be rendered on the HUD layer after world elements but before menu overlays, ensuring it remains visible during gameplay while not interfering with menu interactions.

## Architecture

### Component Structure

```
MyGdxGame (Main Game Class)
├── Compass (New Component)
│   ├── compassTexture: Texture (background circle)
│   ├── needleTexture: Texture (directional arrow)
│   ├── rotation: float (current angle in degrees)
│   └── Methods:
│       ├── update(playerX, playerY, spawnX, spawnY)
│       ├── render(batch, camera, viewport)
│       └── dispose()
└── Existing Components
    ├── Player (provides position)
    ├── OrthographicCamera (provides viewport info)
    └── SpriteBatch (renders compass)
```

### Integration Points

1. **MyGdxGame.create()**: Initialize compass textures and component
2. **MyGdxGame.render()**: Update compass rotation and render on HUD layer
3. **MyGdxGame.dispose()**: Clean up compass textures
4. **Player class**: Provides current player position for calculations

### Rendering Order

The compass will be rendered in the following sequence within the render loop:

```
1. Clear screen
2. Render world (grass, trees, items)
3. Render player and remote players
4. Render health bars (existing)
5. Render connection quality indicator (existing)
6. → Render compass (NEW - HUD layer)
7. Render menu system (top layer)
8. Render notifications
```

## Components and Interfaces

### Compass Class

**Location**: `src/main/java/wagemaker/uk/ui/Compass.java`

**Responsibilities**:
- Calculate angle between player position and spawn point
- Manage compass rotation state
- Render compass background and needle
- Handle texture lifecycle

**Public Interface**:

```java
package wagemaker.uk.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Compass {
    /**
     * Creates a new compass UI element.
     */
    public Compass();
    
    /**
     * Updates the compass rotation based on player and spawn positions.
     * @param playerX Current player X coordinate
     * @param playerY Current player Y coordinate
     * @param spawnX Spawn point X coordinate (typically 0.0)
     * @param spawnY Spawn point Y coordinate (typically 0.0)
     */
    public void update(float playerX, float playerY, float spawnX, float spawnY);
    
    /**
     * Renders the compass in the bottom-left corner of the screen.
     * @param batch SpriteBatch for rendering
     * @param camera OrthographicCamera for screen positioning
     * @param viewport Viewport for screen dimensions
     */
    public void render(SpriteBatch batch, OrthographicCamera camera, Viewport viewport);
    
    /**
     * Disposes of compass textures.
     */
    public void dispose();
}
```

### MyGdxGame Integration

**Modified Methods**:

1. **create()**: Add compass initialization
   ```java
   compass = new Compass();
   ```

2. **render()**: Add compass update and rendering
   ```java
   // After health bars, before menu
   compass.update(player.getX(), player.getY(), 0.0f, 0.0f);
   compass.render(batch, camera, viewport);
   ```

3. **dispose()**: Add compass cleanup
   ```java
   compass.dispose();
   ```

## Data Models

### Compass State

The compass maintains minimal state:

```java
private Texture compassBackground;  // 64x64 circular background
private Texture compassNeedle;      // Needle/arrow graphic
private float currentRotation;      // Angle in degrees (0-360)
private static final float SPAWN_X = 0.0f;
private static final float SPAWN_Y = 0.0f;
```

### Angle Calculation

The compass rotation is calculated using the arctangent of the vector from player to spawn:

```
deltaX = spawnX - playerX
deltaY = spawnY - playerY
angleRadians = atan2(deltaY, deltaX)
angleDegrees = angleRadians * (180 / PI)
```

LibGDX's coordinate system and rotation conventions:
- 0° points right (east)
- 90° points up (north)
- Rotation is counter-clockwise
- The needle graphic should be designed pointing right (0°) by default

## Error Handling

### Texture Loading Failures

If compass textures fail to load:
- Log error message to console
- Set compass textures to null
- Skip rendering in render() method (null check)
- Game continues without compass (graceful degradation)

```java
try {
    compassBackground = new Texture("ui/compass_background.png");
    compassNeedle = new Texture("ui/compass_needle.png");
} catch (Exception e) {
    System.err.println("Failed to load compass textures: " + e.getMessage());
    compassBackground = null;
    compassNeedle = null;
}
```

### Division by Zero

When player is exactly at spawn point (0.0, 0.0):
- atan2(0, 0) returns 0 in Java
- Compass displays neutral orientation (needle pointing right)
- No special handling required

### Multiplayer Considerations

**Single-player mode**:
- Spawn point is always (0.0, 0.0)
- Compass updates based on local player position

**Multiplayer mode**:
- Server defines spawn point (currently hardcoded to 0.0, 0.0)
- Each client calculates compass independently
- No network synchronization required (client-side only)
- Compass continues functioning during network latency

## Testing Strategy

### Unit Testing

**CompassTest.java** - Test compass angle calculations:

```java
@Test
public void testAngleCalculation_PlayerNorth() {
    // Player at (0, 100), spawn at (0, 0)
    // Expected: needle points down (270°)
}

@Test
public void testAngleCalculation_PlayerEast() {
    // Player at (100, 0), spawn at (0, 0)
    // Expected: needle points left (180°)
}

@Test
public void testAngleCalculation_PlayerAtSpawn() {
    // Player at (0, 0), spawn at (0, 0)
    // Expected: neutral orientation (0°)
}

@Test
public void testAngleCalculation_DiagonalPosition() {
    // Player at (100, 100), spawn at (0, 0)
    // Expected: needle points southwest (225°)
}
```

### Integration Testing

**Manual Testing Checklist**:

1. **Visual Positioning**
   - Compass appears in bottom-left corner
   - 10 pixels from left edge, 10 pixels from bottom edge
   - Size is exactly 64x64 pixels
   - Does not overlap with other UI elements

2. **Rotation Accuracy**
   - Walk north: needle points south (down)
   - Walk south: needle points north (up)
   - Walk east: needle points west (left)
   - Walk west: needle points east (right)
   - Diagonal movement: needle points correctly

3. **Smooth Rotation**
   - No jarring jumps during movement
   - Smooth interpolation during rapid direction changes
   - Consistent update rate with game FPS

4. **Mode Compatibility**
   - Works in single-player mode
   - Works in multiplayer host mode
   - Works in multiplayer client mode
   - Persists across menu open/close

5. **Edge Cases**
   - Compass visible at spawn point (0, 0)
   - Compass visible at extreme distances (10000, 10000)
   - Compass visible during camera zoom/resize
   - Compass visible with all terrain types

### Performance Testing

- Verify no FPS impact (compass rendering is lightweight)
- Test with multiple players in multiplayer (each client renders independently)
- Confirm no memory leaks (proper texture disposal)

## Visual Design

### Compass Background

- 64x64 pixel circular graphic
- Semi-transparent dark background (alpha ~0.7)
- Subtle border or rim for definition
- Cardinal direction markers (N, S, E, W) optional

### Compass Needle

- Red arrow or pointer graphic
- Clearly visible against background
- Designed pointing right (0° orientation)
- Slight glow or outline for visibility

### Asset Files

Required texture assets:
- `assets/ui/compass_background.png` (64x64)
- `assets/ui/compass_needle.png` (64x64, centered, pointing right)

### Rendering Details

```java
// Position in bottom-left corner
float compassX = camera.position.x - viewport.getWorldWidth() / 2 + 10;
float compassY = camera.position.y - viewport.getWorldHeight() / 2 + 10;

// Render background (no rotation)
batch.draw(compassBackground, compassX, compassY, 64, 64);

// Render needle (with rotation around center)
batch.draw(compassNeedle, 
    compassX, compassY,           // position
    32, 32,                        // origin (center of 64x64)
    64, 64,                        // size
    1, 1,                          // scale
    currentRotation,               // rotation in degrees
    0, 0,                          // source position
    64, 64,                        // source size
    false, false);                 // flip x, flip y
```

## Implementation Notes

### Existing Patterns to Follow

1. **Texture Management**: Follow the pattern used for player sprites and tree textures
2. **HUD Rendering**: Similar to ConnectionQualityIndicator positioning
3. **Disposal**: Add to dispose() method like other game resources
4. **Camera Positioning**: Use viewport.getWorldWidth/Height() for screen-relative positioning

### Dependencies

- libGDX core (already in project)
- No new external dependencies required
- Reuses existing SpriteBatch, OrthographicCamera, Viewport

### Configuration

No configuration required:
- Spawn point is hardcoded to (0.0, 0.0)
- Compass size is fixed at 64x64
- Position is fixed at bottom-left corner
- Future enhancement: Make configurable via GameMenu settings

## Future Enhancements

Potential improvements for future iterations:

1. **Configurable Spawn Point**: Allow server to define custom spawn locations
2. **Multiple Waypoints**: Support marking custom waypoints beyond spawn
3. **Distance Indicator**: Show numeric distance to spawn point
4. **Minimap Integration**: Expand compass into full minimap
5. **Customizable Position**: Allow players to move compass to different corners
6. **Toggle Visibility**: Add menu option to show/hide compass
7. **Compass Styles**: Multiple visual themes for compass appearance
