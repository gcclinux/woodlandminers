# Requirements Document

## Introduction

This feature implements an item drop system for SmallTree destruction. When a SmallTree is destroyed, it will drop a WoodStack item at the tree's position. This follows the existing single-item drop pattern used by AppleTree (drops Apple) and BananaTree (drops Banana).

## Glossary

- **Game System**: The Woodlanders game application that manages game state, rendering, and player interactions
- **SmallTree**: A tree entity in the game world that can be attacked and destroyed by players, rendered at 64x128 pixels
- **WoodStack**: An item entity representing a stack of wood materials, extracted from the sprite sheet at coordinates (256, 128) with 64x64 dimensions
- **Item Drop**: The process of spawning item entities at a tree's position when the tree is destroyed
- **Render Size**: The visual dimensions at which an item is displayed on screen (32x32 pixels for WoodStack)
- **Sprite Sheet**: The texture atlas file (assets.png) containing all game sprites
- **Player Class**: The class responsible for handling player actions including tree attacks in single-player mode
- **MyGdxGame Class**: The main game class responsible for rendering items and managing game state
- **Multiplayer Mode**: Game mode where tree destruction is handled by the server
- **Single-Player Mode**: Game mode where tree destruction is handled locally by the Player class

## Requirements

### Requirement 1

**User Story:** As a player, I want SmallTree to drop a WoodStack when destroyed, so that I can collect wood resources

#### Acceptance Criteria

1. WHEN a SmallTree health reaches zero, THE Game System SHALL spawn one WoodStack item at the tree's base position
2. WHERE the game is in single-player mode, THE Player Class SHALL create the WoodStack instance and add it to the game's item collection when a SmallTree is destroyed
3. WHERE the game is in multiplayer mode, THE Game System SHALL handle WoodStack spawning through the server's item spawn messaging system
4. WHEN a WoodStack is spawned, THE Game System SHALL use a unique identifier based on the tree's position key with suffix "-woodstack"
5. THE WoodStack Class SHALL extract its texture from sprite sheet coordinates (256, 128) with source dimensions 64x64

### Requirement 2

**User Story:** As a player, I want WoodStack items to be properly sized and rendered, so that I can easily identify and collect them

#### Acceptance Criteria

1. THE MyGdxGame Class SHALL render WoodStack items at 32x32 pixels on screen
2. THE MyGdxGame Class SHALL maintain a collection for WoodStack items to enable rendering and collision detection
3. THE MyGdxGame Class SHALL render WoodStack items within the camera viewport using the same culling optimization as other items
4. THE WoodStack Class SHALL provide getters for texture and position coordinates
5. THE WoodStack Class SHALL implement a dispose method for texture cleanup

### Requirement 3

**User Story:** As a player, I want to be able to pick up WoodStack items by walking over them, so that I can collect the wood resources

#### Acceptance Criteria

1. WHEN the player's collision box overlaps with a WoodStack item, THE Player Class SHALL remove the WoodStack from the game world
2. THE Player Class SHALL check for WoodStack pickups during each update cycle using the same collision detection pattern as Apple and Banana items
3. WHEN a WoodStack is picked up in multiplayer mode, THE Game System SHALL send an item pickup message to the server
4. THE Player Class SHALL dispose of WoodStack textures when items are picked up to prevent memory leaks
5. THE Player Class SHALL use a 32-pixel pickup range from the player's center to the item's center
