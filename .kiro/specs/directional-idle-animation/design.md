# Design Document

## Overview

This design modifies the Player class animation system to maintain directional facing when idle or performing actions. The solution involves storing directional idle frames and using the current direction state to select the appropriate frame when not moving.

## Architecture

The change affects both the Player and RemotePlayer classes and involves:
1. Storing four directional idle frames (one for each direction) instead of a single idle frame
2. Modifying the `getCurrentFrame()` method to select the appropriate idle frame based on `currentDirection`
3. Ensuring `currentDirection` persists when the player stops moving

Both classes share the same animation structure and will receive identical modifications.

### Current Behavior

- Player has a single `idleFrame` (first frame of DOWN animation)
- When `isMoving = false`, `getCurrentFrame()` always returns this single idle frame
- `currentDirection` is updated during movement but not used for idle state

### New Behavior

- Player has four idle frames: `idleUpFrame`, `idleDownFrame`, `idleLeftFrame`, `idleRightFrame`
- When `isMoving = false`, `getCurrentFrame()` returns the idle frame matching `currentDirection`
- `currentDirection` persists and determines which idle frame to display

## Components and Interfaces

### Modified Fields in Player and RemotePlayer Classes

Both classes will have the same field changes:

```java
// Replace single idle frame with directional idle frames
private TextureRegion idleUpFrame;
private TextureRegion idleDownFrame;
private TextureRegion idleLeftFrame;
private TextureRegion idleRightFrame;
```

### Modified Methods

Both Player and RemotePlayer classes will have identical modifications to these methods:

#### `loadAnimations()`

Extract the first frame from each directional animation to create idle frames:

```java
// Create directional idle frames (first frame of each animation)
idleUpFrame = new TextureRegion(spriteSheet, 0, 512, 64, 64);    // First UP frame
idleLeftFrame = new TextureRegion(spriteSheet, 0, 576, 64, 64);  // First LEFT frame
idleDownFrame = new TextureRegion(spriteSheet, 0, 640, 64, 64);  // First DOWN frame
idleRightFrame = new TextureRegion(spriteSheet, 0, 704, 64, 64); // First RIGHT frame
```

#### `getCurrentFrame()` (Player class - public method)

Select idle frame based on current direction:

```java
public TextureRegion getCurrentFrame() {
    if (isMoving) {
        return currentAnimation.getKeyFrame(animTime);
    } else {
        // Return directional idle frame based on last movement direction
        switch (currentDirection) {
            case UP:
                return idleUpFrame;
            case DOWN:
                return idleDownFrame;
            case LEFT:
                return idleLeftFrame;
            case RIGHT:
                return idleRightFrame;
            default:
                return idleDownFrame; // Fallback
        }
    }
}
```

#### `getCurrentFrame()` (RemotePlayer class - private method)

Same logic as Player class:

```java
private TextureRegion getCurrentFrame() {
    if (isMoving) {
        return currentAnimation.getKeyFrame(animTime);
    } else {
        // Return directional idle frame based on last movement direction
        switch (currentDirection) {
            case UP:
                return idleUpFrame;
            case DOWN:
                return idleDownFrame;
            case LEFT:
                return idleLeftFrame;
            case RIGHT:
                return idleRightFrame;
            default:
                return idleDownFrame; // Fallback
        }
    }
}
```

### No Changes Required

#### Player Class
The `update()` method already:
- Tracks `currentDirection` during movement
- Preserves `currentDirection` when not moving (only updates when keys are pressed)
- Sets `isMoving` flag appropriately

#### RemotePlayer Class
The `updatePosition()` and `update()` methods already:
- Track `currentDirection` based on network updates
- Preserve `currentDirection` when not moving
- Set `isMoving` flag appropriately

This means the direction state naturally persists when players stop, which is exactly what we need.

## Data Models

No new data structures required. The existing `Direction` enum and `currentDirection` field provide all necessary state tracking.

## Error Handling

- Default case in switch statement returns `idleDownFrame` as a safe fallback
- No null checks needed as all idle frames are initialized in `loadAnimations()`
- Sprite sheet coordinates are hardcoded and validated by existing implementation

## Testing Strategy

### Manual Testing

1. **Directional Idle Test**: Move in each direction (UP, DOWN, LEFT, RIGHT) and release keys - verify character faces that direction when idle
2. **Attack Facing Test**: Approach a tree from different directions and attack - verify character faces the tree appropriately
3. **Direction Persistence Test**: Move in one direction, stop, perform other actions (like attacking) - verify facing direction remains consistent
4. **Diagonal Movement Test**: Move diagonally (e.g., UP+LEFT) and stop - verify character faces the last prioritized direction (LEFT in this case, based on existing priority logic)

### Visual Verification

Compare idle frames for each direction:
- UP idle: Character facing away/upward
- DOWN idle: Character facing forward/downward  
- LEFT idle: Character facing left
- RIGHT idle: Character facing right

### Edge Cases

- Starting the game: Both local and remote characters should face DOWN (default `currentDirection`)
- Rapid direction changes: Characters should face the last direction before stopping
- Multiplayer synchronization: Remote players receive direction updates from the server and will display the correct facing direction
- Network lag: RemotePlayer interpolation ensures smooth visual transitions even with delayed updates
