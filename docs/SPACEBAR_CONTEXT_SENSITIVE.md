# Context-Sensitive Spacebar Implementation

## Overview
The spacebar is now context-sensitive, serving dual purposes:
- **When targeting is active:** Plants items at the target location
- **When targeting is not active:** Attacks nearby trees/objects

This makes the spacebar a universal "action" key that adapts to the current mode.

## Implementation

### Code Changes

**Location:** `Player.java` - `update()` method

**Before:**
```java
// handle attack
if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
    attackNearbyTargets();
}
```

**After:**
```java
// Handle spacebar - context-sensitive action
// If targeting is active: plant item at target
// If targeting is not active: attack nearby targets
if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
    if (targetingSystem.isActive()) {
        // Targeting mode: use spacebar to plant
        handleSpacebarPlanting();
    } else {
        // Normal mode: use spacebar to attack
        attackNearbyTargets();
    }
}
```

**New Method:** `handleSpacebarPlanting()`
```java
/**
 * Handle planting action when spacebar is pressed while targeting is active.
 * This provides an alternative to the 'P' key for planting.
 * Spacebar is context-sensitive: plants when targeting, attacks when not targeting.
 */
private void handleSpacebarPlanting() {
    // If targeting is active and target is valid, place the item
    if (targetingSystem.isActive() && targetingSystem.isTargetValid()) {
        // Get current target coordinates
        float[] coords = targetingSystem.getTargetCoordinates();
        
        // Place the item at target location
        handleItemPlacement(coords[0], coords[1]);
        
        // Targeting remains active - don't deactivate
        // It will only deactivate when the player deselects the item
    }
}
```

## User Experience

### Normal Mode (No Item Selected)
1. Walk near a tree
2. Press **SPACEBAR** → Attacks the tree
3. Keep pressing **SPACEBAR** → Continues attacking until tree is destroyed

### Targeting Mode (Item Selected)
1. Press **'3'** to select baby bamboo → White targeting dot appears
2. Use **A/W/D/S** to move the target
3. Press **SPACEBAR** → Plants bamboo at target location
4. Target remains active, move to new location
5. Press **SPACEBAR** again → Plants another bamboo
6. Press **'3'** to deselect → Returns to normal mode

## Benefits

1. **Intuitive Controls:** One action key for all primary actions
2. **Muscle Memory:** No need to switch between keys for different actions
3. **Faster Workflow:** Spacebar is easier to press repeatedly than 'P'
4. **Context-Aware:** The game automatically knows what you want to do
5. **Backward Compatible:** 'P' key still works for planting

## Control Summary

### Planting Controls (When Item Selected)
- **SPACEBAR** - Plant item at current target (NEW!)
- **P** - Plant item at current target (original)
- **A** - Move target left
- **W** - Move target up
- **D** - Move target right
- **S** - Move target down
- **ESC** - Cancel targeting
- **Press item key again** - Deselect item

### Attack Controls (No Item Selected)
- **SPACEBAR** - Attack nearby trees/objects

## Testing

All multiplayer integration tests pass:
```bash
./gradlew test --tests "wagemaker.uk.targeting.TargetingMultiplayerIntegrationTest"
```

✅ 7/7 tests passing

## Design Rationale

### Why Context-Sensitive?
1. **Reduces Cognitive Load:** Players don't need to remember different keys for similar actions
2. **Natural Mapping:** Spacebar = "do the thing" in most games
3. **Prevents Conflicts:** Can't accidentally attack while planting or vice versa
4. **Ergonomic:** Spacebar is the largest, easiest key to press

### Why Keep 'P' Key?
1. **Backward Compatibility:** Existing players are used to 'P'
2. **Explicit Intent:** Some players prefer explicit planting key
3. **Flexibility:** Gives players choice in control scheme
4. **Accessibility:** Some players may find 'P' easier than spacebar

## Future Enhancements

This pattern can be extended to other context-sensitive actions:
- **With pebbles selected:** Spacebar could throw pebbles
- **With food selected:** Spacebar could consume food
- **Near interactive objects:** Spacebar could interact with them

The context-sensitive pattern makes controls more intuitive as the game grows.
