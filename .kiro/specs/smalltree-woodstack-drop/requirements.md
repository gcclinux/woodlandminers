# Requirements Document

## Introduction

This feature implements a dual-item random drop system for SmallTree destruction. When a SmallTree is destroyed, it will drop two items randomly selected from three possible combinations: 2x BabyTree, 2x WoodStack, or 1x BabyTree + 1x WoodStack. Each combination has an equal 33% probability. This follows the same random drop pattern used by BambooTree (which drops BambooStack and BabyBamboo).

## Glossary

- **Game System**: The Woodlanders game application that manages game state, rendering, and player interactions
- **SmallTree**: A tree entity in the game world that can be attacked and destroyed by players, rendered at 64x128 pixels
- **WoodStack**: An item entity representing a stack of wood materials, extracted from the sprite sheet at coordinates (256, 128) with 64x64 dimensions
- **BabyTree**: An item entity representing a young tree sapling that can be planted, extracted from the sprite sheet with 64x64 dimensions, rendered at 32x32 pixels
- **Item Drop**: The process of spawning item entities at a tree's position when the tree is destroyed
- **Random Drop**: A drop system where the specific items dropped are determined randomly from a set of possible combinations
- **Drop Combination**: One of three possible item pairs that can drop from SmallTree destruction
- **Render Size**: The visual dimensions at which an item is displayed on screen (32x32 pixels for both items)
- **Sprite Sheet**: The texture atlas file (assets.png) containing all game sprites
- **Player Class**: The class responsible for handling player actions including tree attacks in single-player mode
- **MyGdxGame Class**: The main game class responsible for rendering items and managing game state
- **Multiplayer Mode**: Game mode where tree destruction is handled by the server
- **Single-Player Mode**: Game mode where tree destruction is handled locally by the Player class
- **ItemType Enum**: Network enumeration defining item types for multiplayer synchronization
- **Inventory System**: The player inventory system that stores collected items and persists them in save files

## Requirements

### Requirement 1

**User Story:** As a player, I want SmallTree to randomly drop two items when destroyed, so that I can collect different combinations of wood resources

#### Acceptance Criteria

1. WHEN a SmallTree health reaches zero, THE Game System SHALL randomly select one of three drop combinations with equal probability
2. THE Game System SHALL spawn two BabyTree items at the tree's base position when the first drop combination is selected
3. THE Game System SHALL spawn two WoodStack items at the tree's base position when the second drop combination is selected
4. THE Game System SHALL spawn one BabyTree item and one WoodStack item at the tree's base position when the third drop combination is selected
5. WHEN two items are spawned, THE Game System SHALL position the second item 8 pixels horizontally offset from the first item to prevent overlap
6. WHERE the game is in single-player mode, THE Player Class SHALL create both item instances and add them to the game's item collections when a SmallTree is destroyed
7. WHERE the game is in multiplayer mode, THE Game System SHALL handle item spawning through the server's item spawn messaging system
8. WHEN items are spawned, THE Game System SHALL use unique identifiers based on the tree's position key with suffixes "-item1" and "-item2"

### Requirement 2

**User Story:** As a player, I want to see a new BabyTree item that can be collected and used for planting, so that I can grow new trees

#### Acceptance Criteria

1. THE BabyTree Class SHALL extract its texture from the sprite sheet with source dimensions 64x64
2. THE BabyTree Class SHALL provide getters for texture and position coordinates
3. THE BabyTree Class SHALL implement a dispose method for texture cleanup
4. THE MyGdxGame Class SHALL render BabyTree items at 32x32 pixels on screen
5. THE MyGdxGame Class SHALL maintain a collection for BabyTree items to enable rendering and collision detection
6. THE MyGdxGame Class SHALL render BabyTree items within the camera viewport using the same culling optimization as other items

### Requirement 3

**User Story:** As a player, I want WoodStack items to be properly sized and rendered, so that I can easily identify and collect them

#### Acceptance Criteria

1. THE WoodStack Class SHALL extract its texture from sprite sheet coordinates (256, 128) with source dimensions 64x64
2. THE MyGdxGame Class SHALL render WoodStack items at 32x32 pixels on screen
3. THE MyGdxGame Class SHALL maintain a collection for WoodStack items to enable rendering and collision detection
4. THE MyGdxGame Class SHALL render WoodStack items within the camera viewport using the same culling optimization as other items
5. THE WoodStack Class SHALL provide getters for texture and position coordinates
6. THE WoodStack Class SHALL implement a dispose method for texture cleanup

### Requirement 4

**User Story:** As a player, I want to be able to pick up both BabyTree and WoodStack items by walking over them, so that I can collect the resources

#### Acceptance Criteria

1. WHEN the player's collision box overlaps with a BabyTree item, THE Player Class SHALL remove the BabyTree from the game world
2. WHEN the player's collision box overlaps with a WoodStack item, THE Player Class SHALL remove the WoodStack from the game world
3. THE Player Class SHALL check for BabyTree and WoodStack pickups during each update cycle using the same collision detection pattern as other items
4. WHEN a BabyTree or WoodStack is picked up in multiplayer mode, THE Game System SHALL send an item pickup message to the server
5. THE Player Class SHALL dispose of item textures when items are picked up to prevent memory leaks
6. THE Player Class SHALL use a 32-pixel pickup range from the player's center to the item's center

### Requirement 5

**User Story:** As a player, I want BabyTree items to be added to my inventory when picked up, so that I can use them for planting later

#### Acceptance Criteria

1. WHEN a BabyTree item is picked up, THE Inventory System SHALL add one BabyTree to the player's inventory
2. THE Inventory System SHALL support the BabyTree item type in the ItemType enumeration
3. THE Inventory System SHALL display the BabyTree count in the inventory UI
4. THE Inventory System SHALL allow the player to select BabyTree from the inventory for planting
5. WHEN the player saves the game, THE World Save System SHALL persist the BabyTree inventory count
6. WHEN the player loads a saved game, THE World Save System SHALL restore the BabyTree inventory count

### Requirement 6

**User Story:** As a multiplayer client, I want BabyTree items to synchronize correctly across all players, so that everyone sees the same game state

#### Acceptance Criteria

1. THE ItemType Network Enum SHALL include a BABY_TREE entry for network synchronization
2. WHEN a BabyTree is spawned in multiplayer mode, THE Game Server SHALL broadcast an ItemSpawnMessage with type BABY_TREE
3. WHEN a BabyTree is picked up in multiplayer mode, THE Game Server SHALL broadcast an ItemPickupMessage for the BabyTree
4. WHEN a client receives a BabyTree spawn message, THE Game Client SHALL create a BabyTree instance at the specified position
5. WHEN a client receives a BabyTree pickup message, THE Game Client SHALL remove the BabyTree from the local collection
