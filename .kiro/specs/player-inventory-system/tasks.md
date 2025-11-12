# Implementation Plan

- [x] 1. Create core inventory data structures

  - Create Inventory class with item count fields and getter/setter methods
  - Create ItemType enum with consumable flags and health restore values
  - _Requirements: 3.2, 3.3, 3.4, 3.5_

- [x] 2. Implement InventoryManager class

- [x] 2.1 Create InventoryManager with dual inventory support

  - Implement InventoryManager class with separate single-player and multiplayer Inventory instances
  - Add mode switching logic to select appropriate inventory based on game mode
  - Add getCurrentInventory() method to return active inventory
  - _Requirements: 3.1, 5.1, 5.2, 5.3_

- [x] 2.2 Implement item collection logic with health-based routing

  - Add collectItem() method that checks player health and item type
  - Route consumable items to immediate consumption when health < 100%
  - Route items to storage when health = 100% or item is non-consumable
  - _Requirements: 1.2, 2.1, 2.2, 2.3, 3.3_

- [x] 2.3 Implement auto-consumption system

  - Add tryAutoConsume() method that checks health and available consumables
  - Implement consumeApple() and consumeBanana() methods with health restoration
  - Add logic to decrement inventory counts when consuming
  - _Requirements: 2.4, 2.5, 3.4_

- [x] 3. Integrate inventory system with Player class

- [x] 3.1 Add InventoryManager reference to Player

  - Add inventoryManager field to Player class
  - Add setInventoryManager() method
  - Initialize inventory manager in MyGdxGame during player setup
  - _Requirements: 1.1, 1.2_

- [x] 3.2 Modify item pickup methods to use InventoryManager


  - Update pickupApple() to call inventoryManager.collectItem(APPLE)
  - Update pickupBanana() to call inventoryManager.collectItem(BANANA)
  - Update pickupBambooStack() to call inventoryManager.collectItem(BAMBOO_STACK)
  - Update pickupBabyBamboo() to call inventoryManager.collectItem(BABY_BAMBOO)
  - Update pickupWoodStack() to call inventoryManager.collectItem(WOOD_STACK)
  - Remove direct health restoration from pickup methods
  - _Requirements: 1.2, 1.3, 2.1, 2.2, 2.3_

- [x] 3.3 Add health change detection for auto-consumption

  - Track previousHealth in Player update loop
  - Call inventoryManager.tryAutoConsume() when health decreases
  - _Requirements: 2.4_

- [x] 4. Create inventory UI rendering system

- [x] 4.1 Create InventoryRenderer class with asset loading

  - Create InventoryRenderer class with texture fields for icons and background
  - Load item icon textures from existing item classes
  - Create wooden plank background texture procedurally
  - Initialize BitmapFont for count display
  - _Requirements: 4.1, 4.4, 4.5, 7.1_

- [x] 4.2 Implement inventory panel rendering

  - Calculate bottom-right screen position based on camera
  - Render wooden plank background at calculated position
  - Render slot borders for visual separation
  - _Requirements: 4.1, 4.5, 4.6, 7.2, 7.4_

- [x] 4.3 Implement slot rendering with icons and counts

  - Create renderSlot() method for individual slot rendering
  - Render item icons at 32×32 size centered in slots
  - Render item counts above icons with shadow for readability
  - Display slots in order: Apple, Banana, BabyBamboo, BambooStack, WoodStack
  - _Requirements: 4.2, 4.3, 4.4, 4.7, 4.8, 7.3_

- [x] 4.4 Integrate InventoryRenderer into game render loop

  - Add InventoryRenderer instance to MyGdxGame
  - Call inventoryRenderer.render() in main render method after other UI elements
  - Pass current inventory from InventoryManager to renderer
  - _Requirements: 4.1, 4.7_

- [x] 5. Extend save/load system for inventory persistence

- [x] 5.1 Rename "Save Position" to "Save Player" in menu


  - Update singleplayerMenuItems array in GameMenu to use "Save Player"
  - Update multiplayerMenuItems array in GameMenu to use "Save Player"
  - _Requirements: 6.1_

- [x] 5.2 Extend savePlayerPosition to save inventory data

  - Add InventoryManager reference to GameMenu
  - Extend JSON builder to include singleplayerInventory object with all item counts
  - Extend JSON builder to include multiplayerInventory object with all item counts
  - Maintain backwards compatibility with existing save file structure
  - _Requirements: 6.2, 6.3, 6.6_

- [x] 5.3 Extend loadPlayerPosition to restore inventory data


  - Add parseJsonObjectInt() helper method for parsing integer values from JSON objects
  - Parse singleplayerInventory data and restore to single-player inventory
  - Parse multiplayerInventory data and restore to multiplayer inventory
  - Handle missing inventory data gracefully for backwards compatibility
  - _Requirements: 6.4, 6.5, 6.6_

- [x] 5.4 Wire InventoryManager into save/load flow

  - Pass InventoryManager reference to GameMenu during initialization
  - Ensure inventory is restored before player spawns
  - _Requirements: 6.4, 6.5_

- [x] 6. Handle multiplayer mode switching

- [x] 6.1 Update InventoryManager mode when switching game modes


  - Add setMultiplayerMode() calls when entering multiplayer (host or client)
  - Add setMultiplayerMode() calls when disconnecting from multiplayer
  - Ensure correct inventory is active after mode switch
  - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [x] 6.2 Verify inventory isolation between modes

  - Test that single-player inventory is not affected by multiplayer gameplay
  - Test that multiplayer inventory is not affected by single-player gameplay
  - _Requirements: 5.4_

- [x] 7. Add multiplayer synchronization for inventory

  - Extend network protocol to include inventory sync messages
  - Implement server-side inventory validation
  - Implement client-side inventory update handling
  - Add periodic inventory sync from server to clients
  - _Requirements: 3.1, 5.1, 5.2_

- [x] 8. Create unit tests for core inventory logic

  - Write tests for Inventory add/remove operations
  - Write tests for InventoryManager mode switching
  - Write tests for auto-consumption logic
  - Write tests for health-based item routing
  - _Requirements: All requirements_

- [x] 9. Create integration tests for save/load

  - Write test for saving inventory data to JSON
  - Write test for loading inventory data from JSON
  - Write test for backwards compatibility with old save files
  - Write test for mode-specific inventory persistence
  - _Requirements: 6.2, 6.3, 6.4, 6.5, 6.6_
