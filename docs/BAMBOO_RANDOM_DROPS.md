# Bamboo Tree Random Drop Feature

## Overview
Modified bamboo tree drops to randomly spawn one of three possible item combinations when destroyed.

## Drop Patterns
When a bamboo tree is destroyed, it will randomly drop one of the following:

1. **33% chance**: 1x BambooStack + 1x BabyBamboo (original behavior)
2. **33% chance**: 2x BambooStack
3. **33% chance**: 2x BabyBamboo

## Implementation Details

### Client-Side (Single Player)
**File**: `src/main/java/wagemaker/uk/player/Player.java`

Modified the `attackNearbyTargets()` method to use `Math.random()` to determine which drop pattern to use when a bamboo tree is destroyed.

### Server-Side (Multiplayer)
**File**: `src/main/java/wagemaker/uk/network/ClientConnection.java`

Modified the `handleAttackAction()` method to use the same random drop logic for multiplayer synchronization.

## Technical Notes

- Both items in each drop pattern are spawned at the tree's base position, with the second item offset by 8 pixels horizontally
- The random selection uses `Math.random()` which generates a float between 0.0 and 1.0
- Drop pattern thresholds:
  - 0.00 - 0.33: Pattern 1 (mixed)
  - 0.33 - 0.66: Pattern 2 (2x BambooStack)
  - 0.66 - 1.00: Pattern 3 (2x BabyBamboo)

## Testing Notes

Existing tests in `BambooDualDropIntegrationTest.java` expect the original behavior (always 1 BambooStack + 1 BabyBamboo). These tests will need to be updated to account for the random drop behavior if test coverage is required.
