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

## Controls
- **Arrow Keys**: Move character (Up/Down/Left/Right)
- **Spacebar**: Attack nearby trees
- **Escape**: Open/close menu
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
- **Memory Management**: Proper texture disposal and cleanup
- **Performance Optimization**: Only processes visible objects
- **Modular Design**: Separate classes for Player, Tree, AppleTree, and environmental objects
- **Infinite Generation**: Dynamic world expansion without performance loss
- **Cleared Position Tracking**: Prevents tree regeneration at destroyed locations
