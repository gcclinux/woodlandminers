# Requirements Document

## Introduction

This feature implements a dual-item drop system for BambooTree destruction. When a BambooTree is destroyed, it will drop two distinct items (BambooStack and BabyBamboo) at the tree's position, positioned a few pixels apart. This extends the existing single-item drop pattern used by AppleTree and BananaTree to support multiple item drops from a single tree.

## Glossary

- **Game System**: The Woodlanders game application that manages game state, rendering, and player interactions
- **BambooTree**: A tree entity in the game world that can be attacked and destroyed by players
- **BambooStack**: An item entity representing a stack of bamboo materials, extracted from the sprite sheet at coordinates (128, 128) with 64x64 dimensions
- **BabyBamboo**: An item entity representing a young bamboo plant, extracted from the sprite sheet at coordinates (192, 128) with 64x64 dimensions
- **Item Drop**: The process of spawning item entities at a tree's position when the tree is destroyed
- **Render Size**: The visual dimensions at which an item is displayed on screen (32x32 pixels for bamboo items)
- **Sprite Sheet**: The texture atlas file (assets.png) containing all game sprites
- **Player Class**: The class responsible for handling player actions including tree attacks in single-player mode
- **MyGdxGame Class**: The main game class responsible for rendering items and managing game state
- **Multiplayer Mode**: Game mode where tree destruction is handled by the server
- **Single-Player Mode**: Game mode where tree destruction is handled locally by the Player class

## Requirements

### Requirement 1

**User Story:** As a player, I want BambooTree to drop two items when destroyed, so that I can collect both BambooStack and BabyBamboo resources

#### Acceptance Criteria

1. WHEN a BambooTree health reaches zero, THE Game System SHALL spawn one BambooStack item at the tree's base position
2. WHEN a BambooTree health reaches zero, THE Game System SHALL spawn one BabyBamboo item at the tree's base position offset by 8 pixels horizontally from the BambooStack
3. WHERE the game is in single-player mode, THE Player Class SHALL create both item instances and add them to the game's item collections when a BambooTree is destroyed
4. WHERE the game is in multiplayer mode, THE Game System SHALL handle bamboo item spawning through the server's item spawn messaging system
5. WHEN bamboo items are spawned, THE Game System SHALL use unique identifiers for each item based on the tree's position key with suffixes "-bamboostack" and "-babybamboo"

### Requirement 2

**User Story:** As a player, I want bamboo items to be visually distinct and properly sized, so that I can easily identify and collect them

#### Acceptance Criteria

1. THE MyGdxGame Class SHALL render BambooStack items at 32x32 pixels on screen
2. THE MyGdxGame Class SHALL render BabyBamboo items at 32x32 pixels on screen
3. THE BambooStack Class SHALL extract its texture from sprite sheet coordinates (128, 128) with source dimensions 64x64
4. THE BabyBamboo Class SHALL extract its texture from sprite sheet coordinates (192, 128) with source dimensions 64x64
5. THE MyGdxGame Class SHALL maintain separate collections for BambooStack and BabyBamboo items to enable independent rendering and collision detection

### Requirement 3

**User Story:** As a player, I want to be able to pick up bamboo items by walking over them, so that I can collect the resources

#### Acceptance Criteria

1. WHEN the player's collision box overlaps with a BambooStack item, THE Player Class SHALL remove the BambooStack from the game world
2. WHEN the player's collision box overlaps with a BabyBamboo item, THE Player Class SHALL remove the BabyBamboo from the game world
3. THE Player Class SHALL check for bamboo item pickups during each update cycle using the same collision detection pattern as Apple and Banana items
4. WHEN a bamboo item is picked up in multiplayer mode, THE Game System SHALL send an item pickup message to the server
5. THE Player Class SHALL dispose of bamboo item textures when items are picked up to prevent memory leaks
