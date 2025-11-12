# Requirements Document

## Introduction

This document specifies the requirements for a comprehensive player inventory system that enables players to collect, store, consume, and persist items dropped from trees. The system includes automatic health-based consumption logic, visual inventory UI display, and separate inventory management for single-player and multiplayer modes with persistence capabilities.

## Glossary

- **Player Inventory System**: The complete system managing item collection, storage, consumption, display, and persistence
- **Consumable Item**: An item (Apple or Banana) that can restore player health when consumed
- **Collectable Item**: Any item dropped from trees that can be picked up and stored
- **Inventory Slot**: A visual container displaying one item type with its quantity
- **Inventory UI**: The visual interface displayed at the bottom-right of the screen showing all inventory slots
- **Health Threshold**: The health level below which consumable items are automatically used
- **Game Mode Context**: Either single-player or multiplayer mode, each maintaining separate inventory state
- **Player State**: The complete saved state including position and inventory data
- **Auto-Consumption**: The automatic use of consumable items when player health drops below maximum

## Requirements

### Requirement 1: Item Collection System

**User Story:** As a player, I want to automatically collect items dropped from destroyed trees, so that I can gather resources while playing

#### Acceptance Criteria

1.1 WHEN a tree (AppleTree, BananaTree, Bamboo, SmallTree) is destroyed, THE Player Inventory System SHALL detect the dropped item within collection range

1.2 WHEN a dropped item is within collection range of the player, THE Player Inventory System SHALL add the item to the player's inventory

1.3 WHEN an item is collected, THE Player Inventory System SHALL remove the item entity from the game world

1.4 THE Player Inventory System SHALL maintain separate item counts for each collectable item type (Apple, Banana, BabyBamboo, BambooStack, WoodStack)

### Requirement 2: Health-Based Consumption Logic

**User Story:** As a player, I want consumable items to automatically restore my health when I'm injured, so that I can survive without manual intervention

#### Acceptance Criteria

2.1 WHEN the player's health is below 100% AND the player collects an Apple, THE Player Inventory System SHALL immediately consume the Apple to restore health

2.2 WHEN the player's health is below 100% AND the player collects a Banana, THE Player Inventory System SHALL immediately consume the Banana to restore health

2.3 WHEN the player's health is at 100% AND the player collects a consumable item, THE Player Inventory System SHALL store the item in inventory without consuming it

2.4 WHILE the player's health is below 100%, THE Player Inventory System SHALL automatically consume stored Apples or Bananas from inventory to restore health

2.5 THE Player Inventory System SHALL apply the same health restoration values for consumed items as currently implemented in the game

### Requirement 3: Inventory Storage Management

**User Story:** As a player, I want my collected items to be stored in an organized inventory, so that I can track what resources I have available

#### Acceptance Criteria

3.1 THE Player Inventory System SHALL maintain separate inventory storage for single-player mode and multiplayer mode

3.2 THE Player Inventory System SHALL store item quantities for five item types: Apple, Banana, BabyBamboo, BambooStack, and WoodStack

3.3 WHEN an item is added to inventory, THE Player Inventory System SHALL increment the count for that item type

3.4 WHEN an item is consumed from inventory, THE Player Inventory System SHALL decrement the count for that item type

3.5 THE Player Inventory System SHALL prevent item counts from becoming negative values

### Requirement 4: Inventory UI Display

**User Story:** As a player, I want to see my inventory displayed on screen, so that I know what items and quantities I currently have

#### Acceptance Criteria

4.1 THE Player Inventory System SHALL render an inventory UI panel at the bottom-right corner of the game screen

4.2 THE Player Inventory System SHALL display five inventory slots in a horizontal row within the inventory panel

4.3 WHEN rendering each inventory slot, THE Player Inventory System SHALL display the item count above the item image

4.4 THE Player Inventory System SHALL render 32x32 pixel item images for each inventory slot using existing game textures

4.5 THE Player Inventory System SHALL render a wooden plank background texture behind the inventory panel

4.6 THE Player Inventory System SHALL render border boxes around each inventory slot for visual separation

4.7 THE Player Inventory System SHALL update the displayed item counts in real-time when inventory changes occur

4.8 THE Player Inventory System SHALL display inventory slots in the following order: Apple, Banana, BabyBamboo, BambooStack, WoodStack

### Requirement 5: Game Mode Separation

**User Story:** As a player, I want my single-player inventory to be separate from my multiplayer inventory, so that my progress in each mode is independent

#### Acceptance Criteria

5.1 WHEN playing in single-player mode, THE Player Inventory System SHALL use the single-player inventory storage

5.2 WHEN playing in multiplayer mode, THE Player Inventory System SHALL use the multiplayer inventory storage

5.3 WHEN switching between single-player and multiplayer modes, THE Player Inventory System SHALL load the appropriate inventory for that mode

5.4 THE Player Inventory System SHALL prevent inventory data from being shared or transferred between single-player and multiplayer modes

### Requirement 6: Player State Persistence

**User Story:** As a player, I want my inventory to be saved along with my position, so that I can resume with my collected items when I return to the game

#### Acceptance Criteria

6.1 THE Player Inventory System SHALL rename the existing "Save Position" menu option to "Save Player"

6.2 WHEN the player activates "Save Player" in single-player mode, THE Player Inventory System SHALL save both player position and current single-player inventory to persistent storage

6.3 WHEN the player activates "Save Player" in multiplayer mode, THE Player Inventory System SHALL save both player position and current multiplayer inventory to persistent storage

6.4 WHEN the player loads a saved game in single-player mode, THE Player Inventory System SHALL restore both the saved position and the saved single-player inventory

6.5 WHEN the player loads a saved game in multiplayer mode, THE Player Inventory System SHALL restore both the saved position and the saved multiplayer inventory

6.6 THE Player Inventory System SHALL use the existing save/load mechanism and extend it to include inventory data

### Requirement 7: Visual Consistency and Polish

**User Story:** As a player, I want the inventory UI to look polished and consistent with the game's visual style, so that it feels like a natural part of the game

#### Acceptance Criteria

7.1 THE Player Inventory System SHALL use existing game texture assets for item images (apple, banana, bamboo, wood)

7.2 THE Player Inventory System SHALL render the inventory UI with consistent spacing between slots

7.3 THE Player Inventory System SHALL render item count text in a readable font with sufficient contrast against the background

7.4 THE Player Inventory System SHALL position the inventory UI to avoid overlapping with other game UI elements

7.5 THE Player Inventory System SHALL render the inventory UI at a consistent screen position regardless of window size changes
