# Implementation Plan

- [x] 1. Add bamboo item collections to MyGdxGame
  - Add `Map<String, BambooStack> bambooStacks` field declaration after the bananas map (around line 167)
  - Add `Map<String, BabyBamboo> babyBamboos` field declaration after bambooStacks
  - Initialize both collections in the `create()` method with `new HashMap<>()` (around line 238)
  - _Requirements: 2.5_

- [x] 2. Implement bamboo item rendering in MyGdxGame
  - Create `drawBambooStacks()` method that renders BambooStack items at 32x32 pixels with viewport culling
  - Create `drawBabyBamboos()` method that renders BabyBamboo items at 32x32 pixels with viewport culling
  - Add calls to both methods in the `render()` method after `drawBananas()` (around line 451)
  - _Requirements: 2.1, 2.2_

- [x] 3. Wire bamboo item collections to Player class
  - Add `bambooStacks` and `babyBamboos` field declarations in Player class (around line 53)
  - Create `setBambooStacks()` and `setBabyBamboos()` setter methods in Player class (around line 96)
  - Call both setters in MyGdxGame's `create()` method after `player.setBananas()` (around line 270)
  - _Requirements: 2.5_

- [x] 4. Implement dual-item drop logic for BambooTree destruction
  - Modify the bamboo tree destruction block in Player's `attackNearbyTargets()` method (around line 650)
  - Create BambooStack at tree's base position using key `targetKey + "-bamboostack"`
  - Create BabyBamboo at position offset by 8 pixels horizontally using key `targetKey + "-babybamboo"`
  - Add both items to their respective collections
  - Add console logging for both item drops
  - _Requirements: 1.1, 1.2, 1.5_

- [x] 5. Implement BambooStack pickup detection and handling
  - Create `checkBambooStackPickups()` method in Player class that iterates bamboo stacks and checks collision
  - Create `pickupBambooStack()` method that handles single-player pickup (dispose and remove) and multiplayer pickup (send message)
  - Add call to `checkBambooStackPickups()` in Player's `update()` method after `checkBananaPickups()` (around line 298)
  - _Requirements: 3.1, 3.3, 3.4, 3.5_

- [x] 6. Implement BabyBamboo pickup detection and handling
  - Create `checkBabyBambooPickups()` method in Player class that iterates baby bamboos and checks collision
  - Create `pickupBabyBamboo()` method that handles single-player pickup (dispose and remove) and multiplayer pickup (send message)
  - Add call to `checkBabyBambooPickups()` in Player's `update()` method after `checkBambooStackPickups()`
  - _Requirements: 3.2, 3.3, 3.4, 3.5_

- [x] 7. Test bamboo dual-drop functionality
  - Test single-player mode: destroy bamboo tree, verify two items spawn 8 pixels apart
  - Test item pickup: walk over each item, verify pickup and removal
  - Test multiplayer mode: verify items spawn and sync across clients
  - Verify items render at 32x32 pixels
  - Verify console logs show correct positions
  - _Requirements: 1.1, 1.2, 2.1, 2.2, 3.1, 3.2_
