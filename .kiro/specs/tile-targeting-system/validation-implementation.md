# Validation and Error Handling Implementation

## Overview

This document describes the validation and error handling features added to the tile targeting system for bamboo planting.

## Implemented Features

### 1. Target Validation System

**New Classes:**
- `TargetValidator` interface - Defines contract for validating target positions
- `PlantingTargetValidator` class - Implements validation logic for bamboo planting

**Validation Checks:**
1. **Inventory Availability** - Checks if baby bamboo is selected (slot 2) and available in inventory
2. **Biome Type** - Validates that target tile is on sand biome (bamboo can only be planted on sand)
3. **Tile Occupancy** - Ensures tile is not occupied by existing planted bamboo or bamboo trees

### 2. Visual Feedback for Invalid Targets

**Enhanced TargetIndicatorRenderer:**
- Now creates two indicator textures: white for valid targets, red for invalid targets
- `render()` method updated to accept `isValid` parameter
- Automatically displays red indicator when target position fails validation

**Color Scheme:**
- **White indicator** (RGBA: 1.0, 1.0, 1.0, 0.7) - Valid target position
- **Red indicator** (RGBA: 1.0, 0.0, 0.0, 0.7) - Invalid target position

### 3. Real-Time Validation

**TargetingSystem Enhancements:**
- Added `isTargetValid` state flag
- Added `validateCurrentTarget()` method called on every target movement
- `confirmTarget()` now blocks confirmation if target is invalid
- Added `isTargetValid()` getter for rendering system

**Validation Flow:**
1. Player moves target cursor (A/W/D/X keys)
2. `moveTarget()` calls `validateCurrentTarget()`
3. Validator checks all conditions
4. `isTargetValid` flag updated
5. Renderer displays appropriate color indicator

### 4. Error Handling and State Rollback

**Network Error Handling:**
- `executePlanting()` method wrapped in try-catch block
- On network failure:
  - Planted bamboo removed from game world
  - Texture resources disposed properly
  - Baby bamboo returned to inventory
  - Error message logged to console

**State Rollback Process:**
```java
try {
    gameClient.sendBambooPlant(key, tileX, tileY);
    inventoryManager.sendInventoryUpdateToServer();
} catch (Exception e) {
    // Rollback: remove planted bamboo
    plantedBamboos.remove(key);
    plantedBamboo.dispose();
    
    // Rollback: restore inventory
    inventoryManager.getCurrentInventory().addBabyBamboo(1);
}
```

### 5. Integration with Player Class

**Validator Initialization:**
- `updateTargetingValidator()` method creates validator when all dependencies are available
- Called automatically when inventory, biome manager, planted bamboos, or bamboo trees are set
- Ensures validator always has current game state

**Dependencies Required:**
- InventoryManager - for checking baby bamboo availability
- BiomeManager - for checking tile biome type
- PlantedBamboos map - for checking tile occupancy
- BambooTrees map - for checking tile occupancy

## User Experience

### Valid Target Selection
1. Player presses 'P' with baby bamboo selected
2. White indicator appears at player position
3. Player moves cursor with A/W/D/X keys
4. Indicator remains white on valid sand tiles
5. Player presses 'P' to confirm and plant

### Invalid Target Selection
1. Player presses 'P' with baby bamboo selected
2. White indicator appears at player position
3. Player moves cursor to grass tile or occupied tile
4. **Indicator turns red** to show invalid position
5. Player cannot confirm planting (pressing 'P' does nothing)
6. Console message: "Cannot confirm: target position is invalid"

### Network Error Recovery
1. Player plants bamboo successfully (local state updated)
2. Network message fails to send to server
3. **Automatic rollback:**
   - Planted bamboo removed from screen
   - Baby bamboo returned to inventory
   - Player can retry planting
4. Console message: "Planting rolled back due to network error"

## Technical Details

### Validation Performance
- Validation runs on every cursor movement (4 directions)
- Checks are lightweight:
  - Inventory check: O(1) - direct field access
  - Biome check: O(1) - coordinate-based lookup
  - Occupancy check: O(1) - HashMap lookup
- No noticeable performance impact

### Thread Safety
- Validation runs on main render thread
- No concurrent access issues
- State rollback uses same thread as planting

### Memory Usage
- Two 16x16 indicator textures (512 bytes each)
- Validator instance per player (~100 bytes)
- Minimal memory overhead

## Requirements Coverage

This implementation satisfies the following requirements:

- **Requirement 3.4** - Invalid target feedback and prevention
- **Requirement 1.2** - Target movement validation
- **Requirement 1.3** - Confirmation blocked on invalid targets
- **Requirement 1.4** - Visual feedback for invalid selection

## Testing Recommendations

### Manual Testing
1. Test planting on grass tiles (should show red indicator)
2. Test planting on occupied tiles (should show red indicator)
3. Test planting with no baby bamboo (should show red indicator)
4. Test planting on valid sand tiles (should show white indicator)
5. Test network disconnection during planting (should rollback)

### Automated Testing
- Unit tests for `PlantingTargetValidator` validation logic
- Integration tests for state rollback on network failure
- Visual tests for indicator color changes

## Future Enhancements

### Possible Improvements
1. **Audio Feedback** - Play error sound when target becomes invalid
2. **Tooltip Messages** - Show reason for invalid target ("Not on sand", "Tile occupied", etc.)
3. **Validation Caching** - Cache validation results for performance
4. **Custom Validators** - Support different validators for different actions (projectiles, building, etc.)
5. **Server-Side Validation** - Add server validation to prevent cheating in multiplayer

### Extensibility
The validator system is designed to be extensible:
- New validators can implement `TargetValidator` interface
- Multiple validators can be chained together
- Validators can be swapped at runtime for different actions
