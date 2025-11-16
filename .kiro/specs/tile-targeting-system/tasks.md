# Implementation Plan

- [x] 1. Create core targeting system classes and interfaces
  - Create `src/main/java/wagemaker/uk/targeting` package
  - Create `Direction` enum with UP, DOWN, LEFT, RIGHT values
  - Create `TargetingMode` enum with ADJACENT mode (extensible for future modes)
  - Create `TargetingCallback` interface with `onTargetConfirmed(float x, float y)` and `onTargetCancelled()` methods
  - Create `TargetingSystem` class with state management (isActive, targetX, targetY, playerX, playerY, mode, callback)
  - Implement `activate()` method to initialize targeting at player position
  - Implement `deactivate()` method to clear targeting state
  - Implement `moveTarget(Direction)` method to move target by 64px in specified direction
  - Implement `confirmTarget()` method to invoke callback with current coordinates
  - Implement `getTargetCoordinates()` method to return tile-aligned coordinates
  - Implement `isActive()` method to check targeting state
  - Implement tile grid snapping utility method (64x64 grid alignment)
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 4.1, 4.2, 4.3, 4.4, 5.1, 5.2, 5.3, 5.4, 5.5_

- [x] 2. Create target indicator visual renderer
  - Create `TargetIndicatorRenderer` class in `wagemaker.uk.targeting` package
  - Implement `initialize()` method to create indicator texture (16x16 white circle, 70% opacity)
  - Implement `render(SpriteBatch, float x, float y)` method to draw indicator centered on tile
  - Implement `dispose()` method to clean up texture resources
  - Calculate proper centering offset (targetX + 24, targetY + 24 for 16x16 indicator on 64x64 tile)
  - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [x] 3. Integrate targeting system into Player class
  - Add `TargetingSystem` field to Player class
  - Add `TargetIndicatorRenderer` field to Player class
  - Initialize TargetingSystem and TargetIndicatorRenderer in Player constructor
  - Modify `handlePlantingAction()` to activate targeting mode instead of immediate planting
  - Add `handleTargetingInput()` method to process A/W/D/X keys when targeting is active
  - Implement targeting callback to execute planting at selected coordinates
  - Add ESC key handling to cancel targeting mode
  - Ensure targeting input only processes when targeting is active
  - Ensure planting keys (P) toggle between activate and confirm
  - Add `dispose()` call for TargetIndicatorRenderer in Player cleanup
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 3.1, 3.2, 3.3, 3.4, 4.1, 4.2, 4.3, 4.4_

- [x] 4. Modify PlantingSystem to accept explicit coordinates
  - Update `attemptPlant()` method signature to accept explicit targetX and targetY parameters
  - Remove player position parameter (no longer needed)
  - Update validation logic to use provided coordinates instead of player position
  - Ensure tile grid snapping is applied to provided coordinates
  - Update all callers to pass explicit coordinates
  - _Requirements: 3.1, 3.2, 3.3_

- [x] 5. Integrate target indicator rendering into MyGdxGame
  - Add rendering call for target indicator in `render()` method
  - Position rendering after terrain but before planted bamboos
  - Only render when Player's targeting system is active
  - Pass SpriteBatch and target coordinates to renderer
  - Ensure indicator is only rendered for local player (not remote players)
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 5.1, 5.2, 5.3_

- [x] 6. Update multiplayer synchronization for targeted planting
  - Verify `BambooPlantMessage` already supports explicit coordinates (no changes needed)
  - Update Player's targeting callback to send BambooPlantMessage with target coordinates
  - Ensure GameClient.sendBambooPlant() is called with target coordinates, not player position
  - Verify server-side validation accepts coordinates different from player position
  - Test that planted bamboo appears at target coordinates for all clients
  - _Requirements: 3.1, 3.2, 5.4, 6.1, 6.2, 6.3, 6.4, 6.5_

- [x] 7. Implement single-player mode targeting
  - Update Player's targeting callback to handle single-player mode
  - Ensure PlantingSystem is called with target coordinates in single-player
  - Ensure planted bamboo is added to game world at target coordinates
  - Verify inventory deduction occurs after successful planting
  - Test that targeting works identically in single-player and multiplayer
  - _Requirements: 3.1, 3.2, 3.3, 7.1, 7.2, 7.3, 7.4, 7.5_

- [x] 8. Add validation and error handling
  - Implement validation in TargetingSystem for tile occupancy
  - Add validation for biome type (sand tiles only for bamboo)
  - Add validation for inventory availability before activation
  - Implement visual feedback for invalid target selection (red indicator)
  - Add error handling for network failures in multiplayer
  - Implement state rollback on planting failure
  - _Requirements: 3.4, 1.2, 1.3, 1.4_

- [x] 9. Write unit tests for TargetingSystem
  - Test activation and deactivation state transitions
  - Test target movement in all four directions
  - Test coordinate snapping to tile grid
  - Test callback invocation on confirmation
  - Test callback invocation on cancellation
  - Test isActive() state tracking
  - Test boundary conditions and edge cases
  - _Requirements: All_

- [x] 10. Write integration tests for planting with targeting
  - Test bamboo planting at target coordinates in single-player
  - Test inventory deduction after targeted planting
  - Test planted bamboo appears at correct coordinates
  - Test targeting mode deactivates after planting
  - Test targeting with no baby bamboo in inventory
  - Test targeting on invalid biome types
  - Test targeting on occupied tiles
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 7.1, 7.2, 7.3, 7.4, 7.5_

- [x] 11. Write multiplayer integration tests
  - Test target indicator is client-side only (not visible to other players)
  - Test planted bamboo synchronization across clients
  - Test coordinate consistency across clients
  - Test server validation of target coordinates
  - Test network error handling and state rollback
  - _Requirements: 5.1, 5.2, 5.3, 6.1, 6.2, 6.3, 6.4, 6.5_
