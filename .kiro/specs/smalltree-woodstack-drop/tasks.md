# Implementation Plan

- [x] 1. Verify and update BabyTree item class

  - Verify BabyTree.java exists in src/main/java/wagemaker/uk/items/
  - Confirm texture extraction from sprite sheet with correct coordinates
  - Verify getters for texture and position (getX(), getY(), getTexture())
  - Verify dispose() method for texture cleanup
  - Update sprite sheet coordinates if needed based on actual assets.png layout
  - _Requirements: 2.1, 2.2, 2.3_

- [x] 2. Add BabyTree and WoodStack collections to MyGdxGame

  - Add BabyTree and WoodStack import statements at top of MyGdxGame.java
  - Add `Map<String, BabyTree> babyTrees` field declaration with other item collections
  - Add `Map<String, WoodStack> woodStacks` field declaration with other item collections
  - Initialize `babyTrees = new HashMap<>()` in create() method
  - Initialize `woodStacks = new HashMap<>()` in create() method
  - Wire babyTrees collection to player with `player.setBabyTrees(babyTrees)` in create()
  - Wire woodStacks collection to player with `player.setWoodStacks(woodStacks)` in create()
  - _Requirements: 2.4, 2.5, 3.3_

- [x] 3. Implement BabyTree and WoodStack rendering in MyGdxGame

  - Create `drawBabyTrees()` method with viewport culling logic
  - Render BabyTree items at 32x32 pixels using batch.draw()
  - Create `drawWoodStacks()` method with viewport culling logic
  - Render WoodStack items at 32x32 pixels using batch.draw()
  - Call `drawBabyTrees()` in render() method after other item rendering
  - Call `drawWoodStacks()` in render() method after drawBabyTrees()
  - _Requirements: 2.4, 3.2, 3.4_

- [x] 4. Add BabyTree and WoodStack support to Player class

  - Add BabyTree and WoodStack import statements at top of Player.java
  - Add Random import statement for random drop selection
  - Add `Map<String, BabyTree> babyTrees` field declaration
  - Add `Map<String, WoodStack> woodStacks` field declaration
  - Add `Random random = new Random()` field declaration
  - Implement `setBabyTrees()` setter method
  - Implement `setWoodStacks()` setter method
  - _Requirements: 1.6, 4.1, 4.2_

- [x] 5. Implement SmallTree random drop logic

  - Add SmallTree attack detection in Player.update() method
  - Check if SmallTree is in attack range using `isInAttackRange()`
  - Call `tree.attack()` and check if destroyed (returns true)
  - Generate random drop type using `random.nextInt(3)` (returns 0, 1, or 2)
  - Implement case 0: spawn 2x BabyTree at tree position and position + 8px
  - Implement case 1: spawn 2x WoodStack at tree position and position + 8px
  - Implement case 2: spawn 1x BabyTree and 1x WoodStack at tree position and position + 8px
  - Add items to collections with keys "{x},{y}-item1" and "{x},{y}-item2"
  - Remove destroyed tree from collection and add to clearedPositions
  - Add console logging for drop type verification
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.8_

- [x] 6. Implement BabyTree pickup mechanics

  - Create `checkBabyTreePickups()` method in Player class
  - Implement collision detection with 32-pixel pickup range
  - Create `pickupBabyTree()` method to handle pickup logic
  - Handle single-player mode: dispose texture, remove from collection, add to inventory
  - Handle multiplayer mode: send ItemPickupMessage to server
  - Call `checkBabyTreePickups()` in Player.update() method
  - _Requirements: 4.1, 4.3, 4.4, 4.5, 5.1_

- [x] 7. Implement WoodStack pickup mechanics

  - Create `checkWoodStackPickups()` method in Player class
  - Implement collision detection with 32-pixel pickup range
  - Create `pickupWoodStack()` method to handle pickup logic
  - Handle single-player mode: dispose texture and remove from collection
  - Handle multiplayer mode: send ItemPickupMessage to server
  - Call `checkWoodStackPickups()` in Player.update() method
  - _Requirements: 4.2, 4.3, 4.4, 4.5_

- [x] 8. Add BABY_TREE to inventory ItemType enum

  - Open src/main/java/wagemaker/uk/inventory/ItemType.java
  - Add BABY_TREE entry to the ItemType enum
  - Verify enum ordering is consistent with other item types
  - _Requirements: 5.2_

- [x] 9. Add BABY_TREE to network ItemType enum

  - Open src/main/java/wagemaker/uk/network/ItemType.java
  - Add BABY_TREE entry to the network ItemType enum
  - Verify enum ordering matches inventory ItemType enum
  - _Requirements: 6.1_

- [x] 10. Update multiplayer item spawn handling for BabyTree

  - Locate server-side tree destruction handler (likely in GameServer or message handler)
  - Add SmallTree destruction case with random drop logic
  - Broadcast ItemSpawnMessage for each spawned item with correct ItemType
  - Locate client-side ItemSpawnMessage handler
  - Add BABY_TREE case to create BabyTree instances from spawn messages
  - Add items to appropriate collections (babyTrees or woodStacks)
  - _Requirements: 6.2, 6.3, 6.4_

- [x] 11. Update multiplayer item pickup handling for BabyTree

  - Locate server-side ItemPickupMessage handler
  - Verify it handles BABY_TREE pickup messages generically
  - Locate client-side pickup message handler
  - Add BABY_TREE case to remove BabyTree from collection and add to inventory
  - Verify inventory synchronization for BABY_TREE items
  - _Requirements: 6.5, 5.1_

- [x] 12. Verify world save/load support for BabyTree inventory

  - Open src/main/java/wagemaker/uk/world/WorldSaveData.java
  - Verify inventory save includes all ItemType entries (including BABY_TREE)
  - Test save game with BabyTree items in inventory
  - Test load game and verify BabyTree count is restored
  - _Requirements: 5.5, 5.6_

- [x] 13. Test single-player random drop functionality

  - Start single-player game
  - Attack and destroy 10+ SmallTrees
  - Verify all three drop combinations appear (2x BabyTree, 2x WoodStack, 1x each)
  - Verify items render at 32x32 pixels
  - Verify items are positioned 8 pixels apart
  - Verify console logs show correct drop types
  - Walk over BabyTree items and verify pickup + inventory increase
  - Walk over WoodStack items and verify pickup
  - Save game and verify BabyTree count persists
  - Load game and verify BabyTree count restored
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.4, 3.2, 4.1, 4.2, 5.1, 5.5, 5.6_

- [x] 14. Test multiplayer random drop synchronization

  - Start server and connect 2+ clients
  - Client attacks SmallTree until destroyed
  - Verify same drop combination appears on all clients
  - Verify items render correctly on all clients
  - Verify items are positioned correctly on all clients
  - Client picks up items
  - Verify removal on all clients
  - Verify inventory synchronization across clients
  - Test with multiple players attacking different SmallTrees simultaneously
  - _Requirements: 1.7, 6.2, 6.3, 6.4, 6.5_
