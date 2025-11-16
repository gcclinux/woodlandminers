# Targeting Persistence Fix

## Issue
After planting a baby bamboo, the targeting indicator (white dot) was disappearing even though the item was still selected (yellow box still around inventory slot).

## Root Cause
The `handlePlantingAction()` method was calling `targetingSystem.confirmTarget()`, which internally calls `deactivate()` after executing the placement callback. This caused the targeting to disappear after each plant.

## Solution
Changed the planting action to manually get the target coordinates and call the placement handler directly, without using `confirmTarget()`. This way, targeting remains active after planting.

### Code Changes

**Before:**
```java
private void handlePlantingAction() {
    if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
        if (targetingSystem.isActive()) {
            targetingSystem.confirmTarget(); // This deactivates targeting!
        }
    }
}
```

**After:**
```java
private void handlePlantingAction() {
    if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
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
}
```

## New Behavior

1. **Select item (e.g., press '3' for baby bamboo)** → White targeting dot appears
2. **Move target with A/W/D/S** → Dot moves to different tiles
3. **Press 'P'** → Plants bamboo at current target location
4. **Targeting stays active** → White dot remains visible
5. **Move target again with A/W/D/S** → Can plant another bamboo
6. **Press 'P' again** → Plants another bamboo
7. **Repeat as needed** → Plant multiple bamboos without reactivating targeting
8. **Press '3' again** → Deselects item, targeting disappears

## Benefits

- **Faster planting workflow:** No need to reactivate targeting between plants
- **Consistent with selection:** Targeting visible = item selected
- **More intuitive:** The white dot stays as long as you're in "placement mode"
- **Efficient for multiple placements:** Plant many bamboos quickly

## Testing

All multiplayer integration tests pass:
```bash
./gradlew test --tests "wagemaker.uk.targeting.TargetingMultiplayerIntegrationTest"
```

✅ 7/7 tests passing
- Target indicators remain client-side only
- Planted bamboo synchronizes correctly
- Coordinates consistent across clients
- Server validation works properly
