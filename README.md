# 2D Adventure Game - libGDX

A 2D top-down adventure game built with libGDX featuring infinite world exploration, animated character, and tree chopping mechanics.

## Requirements
- Java 21+ (OpenJDK 21.0.8)
- Gradle 9.2.0+

## Installation Guide

### Install SDKMAN (for Gradle management)
```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

### Add to ~/.bashrc
```bash
# THIS MUST BE AT THE END OF THE FILE FOR SDKMAN TO WORK!!!
export SDKMAN_DIR="$HOME/.sdkman"
[[ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]] && source "$HOME/.sdkman/bin/sdkman-init.sh"

# Java configuration
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
```

### Install Gradle via SDKMAN
```bash
sdk install gradle 9.2.0
```

## How to Run

```bash
cd /home/ricardowagemaker/Dev/java2d-libGDX
gradle run
```

Or build a fat jar:

```bash
gradle jar
java -jar build/libs/java2d-libGDX.jar
```

## Game Features

### Player Character
- **Animated Human Sprite**: 64x64 pixel character with skin, black hair, blue shirt, brown pants, and black shoes
- **Walking Animation**: Arms and legs animate with opposite motion (realistic walking pattern)
- **Smooth Movement**: 200 pixels/second movement speed with arrow key controls
- **Camera Following**: Camera centers on player and follows movement

### World System
- **Infinite World**: Minecraft-like infinite terrain generation
- **Grass Background**: Seamless tiled grass texture covering the entire world
- **Fixed Viewport**: 800x600 camera view regardless of screen size
- **Chunk-Based Rendering**: Only renders visible areas for optimal performance

### Tree System
- **Two Tree Types**:
  - **Regular Trees**: 64x64 brown trunk with green leaves
  - **Apple Trees**: 128x128 larger trees with red apples scattered on leaves
- **Random Generation**: 5% spawn chance per grass tile (50/50 split between tree types)
- **Collision Detection**: Players cannot walk through trees (optimized collision boxes)
- **Deterministic Placement**: Same trees appear in same locations every time

### Combat System
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

### Controls
- **Arrow Keys**: Move character (Up/Down/Left/Right)
- **Spacebar**: Attack nearby trees
- **Fullscreen**: Maintains proper scaling and collision detection

### Technical Features
- **Collision Detection**: Precise collision boxes for trees and player
- **Individual Attack Ranges**: Each tree type has unique attack collision detection
- **Health Bar Rendering**: Dynamic health visualization using ShapeRenderer
- **Memory Management**: Proper texture disposal and cleanup
- **Performance Optimization**: Only processes visible objects
- **Modular Design**: Separate classes for Player, Tree, and AppleTree
- **Infinite Generation**: Dynamic world expansion without performance loss
- **Cleared Position Tracking**: Prevents tree regeneration at destroyed locations

## Game Classes
- `MyGdxGame.java` - Main game loop and rendering
- `Player.java` - Character movement, animation, and combat
- `Tree.java` - Regular tree implementation
- `AppleTree.java` - Large apple tree with fruit

## Current Status
✅ Complete infinite world system  
✅ Animated player character  
✅ Tree generation and collision  
✅ Combat and destruction mechanics  
✅ Health bar visualization system  
✅ Individual attack range detection  
✅ Camera following system  
✅ Performance optimization  

## Future Enhancements
- Resource collection (wood, apples)
- Inventory system
- Crafting mechanics
- Additional tree types
- Sound effects
- Save/load functionality
