# Game Features

## Player Character
- **Animated Human Sprite**: 64x64 pixel character with skin, black hair, blue shirt, brown pants, and black shoes
- **Walking Animation**: Arms and legs animate with opposite motion (realistic walking pattern)
- **Smooth Movement**: 200 pixels/second movement speed with arrow key controls
- **Camera Following**: Camera centers on player and follows movement
- **Health System**: Player health can be damaged and restored through gameplay

## World System
- **Infinite World**: Minecraft-like infinite terrain generation
- **Grass Background**: Seamless tiled grass texture covering the entire world
- **Fixed Viewport**: 800x600 camera view regardless of screen size
- **Chunk-Based Rendering**: Only renders visible areas for optimal performance

## Environmental Hazards
- **Cactus Damage**: Walking into cacti damages the player, adding environmental danger to exploration
- **Collision Detection**: Cacti have collision boxes that trigger damage on contact

## Tree System
- **Three Tree Types**:
  - **Regular Trees**: 64x64 brown trunk with green leaves
  - **Apple Trees**: 128x128 larger trees with red apples scattered on leaves - restore player health when harvested
  - **Banana Trees**: Restore player health when harvested
- **Random Generation**: 5% spawn chance per grass tile with distribution across tree types
- **Collision Detection**: Players cannot walk through trees (optimized collision boxes)
- **Deterministic Placement**: Same trees appear in same locations every time
- **Destructible**: Players can destroy trees by attacking them
- **Health Regeneration**: Trees that are damaged but not fully destroyed will slowly regain health over time

## Combat & Interaction System
- **Tree Chopping**: Attack trees with spacebar key
- **Health System**: Each tree has 100 health, loses 10 per attack (10 hits to destroy)
- **Attack Range**: 
  - **Regular Trees**: 64px in all directions from center (128x128 attack area)
  - **Apple Trees**: 64px left/right, 128px up/down from center
- **Health Bars**: 
  - Appear above trees when attacked (3-second visibility)
  - Green background with red overlay showing damage percentage
  - Half the width of each tree type (32px for trees, 64px for apple trees)
- **Permanent Destruction**: Destroyed trees never regenerate
- **Collision Removal**: Destroyed trees no longer block movement
- **Health Restoration**: Apple and banana trees provide health recovery when harvested

## Tile Targeting System
- **Visual Indicator**: White circular indicator (16x16 pixels, 70% opacity) shows target tile location
- **Automatic Activation**: Targeting activates automatically when a placeable item is selected from inventory
- **Persistent Targeting**: Indicator remains visible as long as an item is selected, allowing multiple placements
- **Tile-Based Movement**: Target moves in 64-pixel increments aligned to the game's tile grid
- **WASD Controls**: 
  - **A**: Move target left
  - **W**: Move target up
  - **D**: Move target right
  - **S**: Move target down
- **Placement Actions**:
  - **Spacebar**: Context-sensitive - plants item when targeting is active
  - **P Key**: Alternative planting key
- **Target Validation**: Indicator changes color based on validity:
  - **White**: Valid placement location
  - **Red**: Invalid placement location (occupied tile, wrong biome, etc.)
- **Cancellation**: Press ESC to cancel targeting without placing
- **Deactivation**: Press the item key again to deselect and hide targeting
- **Client-Side Only**: Target indicator is not visible to other players in multiplayer
- **Coordinate Synchronization**: Planted items appear at exact same coordinates for all clients
- **Server Validation**: Server validates placement coordinates before accepting

## Planting System
- **Baby Bamboo Planting**: Plant baby bamboo on sand tiles using the targeting system
- **Growth Mechanics**: Planted bamboo grows over 120 seconds into a harvestable bamboo tree
- **Biome Restrictions**: Can only plant on sand biome tiles
- **Tile Occupation Check**: Cannot plant on tiles already occupied by trees or other objects
- **Inventory Integration**: Automatically deducts baby bamboo from inventory on successful planting
- **Visual Feedback**: Planted bamboo sprite appears immediately at target location
- **Multiplayer Sync**: Planted bamboo synchronized across all connected clients
- **Network Validation**: Server validates planting location and inventory before accepting
- **State Rollback**: Failed network operations rollback local state to maintain consistency

## Controls
- **Arrow Keys**: Move character (Up/Down/Left/Right)
- **Spacebar**: Context-sensitive action:
  - **No item selected**: Attack nearby trees
  - **Item selected (targeting active)**: Plant item at target location
- **A/W/D/S**: Move targeting indicator (when targeting is active)
- **P Key**: Plant item at target location (when targeting is active)
- **1-6 Keys**: Select/deselect inventory slots (toggle selection)
  - Selecting a placeable item activates targeting
  - Deselecting an item deactivates targeting
- **ESC**: Cancel targeting or open/close menu
- **Tab**: Toggle inventory display
- **Fullscreen**: Maintains proper scaling and collision detection

## Multiplayer Features
- **Dedicated Server**: Run standalone server for multiplayer gameplay
- **Client Connection**: Connect to servers via IP address and port
- **Player Names**: Customizable player names (minimum 3 characters)
- **Server Configuration**: Configurable via server.properties file
- **Network Synchronization**: Real-time player position and action updates

## Technical Features
- **Collision Detection**: Precise collision boxes for trees, cacti, and player
- **Individual Attack Ranges**: Each tree type has unique attack collision detection
- **Health Bar Rendering**: Dynamic health visualization using ShapeRenderer
- **Health Regeneration System**: Damaged trees slowly recover health over time
- **Environmental Damage**: Cactus collision detection and damage system
- **Tile Grid System**: 64x64 pixel tile-based world with coordinate snapping
- **Targeting Validation**: Real-time validation of target positions with visual feedback
- **Context-Sensitive Input**: Spacebar adapts behavior based on game state (attack vs plant)
- **Client-Side Rendering**: Targeting indicators rendered locally without network transmission
- **Server Authority**: Server validates all placement actions before accepting
- **State Synchronization**: Planted objects synchronized across all clients with coordinate consistency
- **Memory Management**: Proper texture disposal and cleanup
- **Performance Optimization**: Only processes visible objects
- **Modular Design**: Separate classes for Player, Tree, AppleTree, TargetingSystem, and environmental objects
- **Infinite Generation**: Dynamic world expansion without performance loss
- **Cleared Position Tracking**: Prevents tree regeneration at destroyed locations
