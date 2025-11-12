# Implementation Plan

- [x] 1. Add WoodStack support to MyGdxGame class

  - Add WoodStack import statement at top of file
  - Add `Map<String, WoodStack> woodStacks` field declaration with other item collections
  - Initialize `woodStacks = new HashMap<>()` in create() method
  - Wire woodStacks collection to player with `player.setWoodStacks(woodStacks)` in create()
  - _Requirements: 1.1, 2.2_

- [x] 2. Implement WoodStack rendering in MyGdxGame

  - Create `drawWoodStacks()` method with viewport culling logic
  - Render WoodStack items at 32x32 pixels using batch.draw()
  - Call `drawWoodStacks()` in render() method after other item rendering
  - _Requirements: 2.1, 2.3_

- [x] 3. Add WoodStack support to Player class

  - Add WoodStack import statement at top of file
  - Add `Map<String, WoodStack> woodStacks` field declaration
  - Implement `setWoodStacks()` setter method
  - _Requirements: 1.2, 2.2_

- [x] 4. Implement SmallTree destruction and WoodStack drop logic

  - Add SmallTree attack detection in Player.update() method
  - Check if SmallTree is in attack range using `isInAttackRange()`
  - Call `tree.attack()` and check if destroyed (returns true)
  - Spawn WoodStack at tree position when destroyed
  - Add WoodStack to collection with key format "{x},{y}-woodstack"
  - Remove destroyed tree from collection and add to clearedPositions
  - _Requirements: 1.1, 1.2, 1.4_

- [x] 5. Implement WoodStack pickup mechanics

  - Create `checkWoodStackPickups()` method in Player class
  - Implement collision detection with 32-pixel pickup range
  - Create `pickupWoodStack()` method to handle pickup logic
  - Handle single-player mode: dispose texture and remove from collection
  - Handle multiplayer mode: send ItemPickupMessage to server
  - Call `checkWoodStackPickups()` in Player.update() method
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_
