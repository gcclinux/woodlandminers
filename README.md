# Woodlanders - A 2D Multiplayer Adventure

## 🎮 The AI-Driven Game Development Story

**Woodlanders** is an ambitious experiment in AI-assisted game development - a fully functional multiplayer 2D adventure game built entirely through conversational AI within the **Kiro IDE**. This project demonstrates what's possible when human creativity meets AI capabilities: no manual coding, just natural language prompts, specifications, and iterative refinement.

### The Vision: Zero-Code Game Development

The goal was simple yet audacious: **build a complete, enjoyable multiplayer game in Java using only AI prompts and requests**. No direct code writing. No manual debugging. Just describing what we wanted, and letting Kiro AI bring it to life.

### The Kiro Contains: Two Powerful Approaches

**Kiro AI Vibe** and **Kiro Spec** are two complementary ways to work with AI in the Kiro IDE:

**🌊 Kiro AI Vibe** - *Conversational Flow Development*
- Natural, chat-based interaction with the AI
- Perfect for rapid prototyping and exploratory development
- Immediate responses to "add this feature" or "fix that bug"
- Ideal for quick iterations, bug fixes, and feature additions
- Think of it as pair programming with an AI that never gets tired

**📋 Kiro Spec** - *Structured Feature Engineering*
- Formalized design and implementation process
- Break down complex features into requirements, design, and tasks
- Incremental development with control and feedback at each stage
- Perfect for large features requiring careful planning
- References external files (OpenAPI specs, GraphQL schemas) for context
- Think of it as having an AI project manager and developer in one

### The Journey: From Concept to Playable Game

Starting with a simple idea - "create a 2D adventure game" - we used:
- **Kiro AI Vibe** for rapid feature additions: player movement, tree chopping, health systems
- **Kiro Spec** for complex systems: multiplayer networking, menu systems, world generation
- Pure natural language to describe mechanics: "make trees regenerate health over time"
- Iterative refinement: "the collision feels off" → instant fixes

The result? A feature-rich game with:
- Infinite procedurally generated worlds
- Smooth multiplayer networking
- Combat and resource systems
- Environmental hazards and health mechanics
- Professional menu systems and UI

### What Makes This Special

This isn't just a game - it's proof of concept that complex software can be built through conversation. Every feature, every bug fix, every optimization came from describing what we wanted in plain English. The AI handled the Java, the libGDX framework, the networking protocols, and the game architecture.

**Woodlanders** stands as a testament to the future of software development: where ideas flow directly from mind to machine, and the barrier between imagination and implementation dissolves.

---

## 🎯 Game Overview

A 2D top-down multiplayer adventure game built with libGDX featuring infinite world exploration, animated characters, tree chopping mechanics, and real-time multiplayer gameplay.

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
cd <project folder>/Woodlander
gradle run
```

Or build a fat jar:

```bash
cd <project folder>/Woodlander
gradle build
java -jar .\build\libs\woodlanders-client.jar
```

## Menu Navigation

### Main Menu (Press "Esc" in-game)
Access the main menu at any time by pressing the Escape key. From here you can configure multiplayer settings, change your player name, save your progress, or exit the game.

**"Esc" → Menu → Multiplayer**
```
┌────────────────────────────┐
│   Multiplayer              │
│                            │
│   Host Server              │
│   Connect to Server        │
│                            │
│                            │
│   Back                     │
└────────────────────────────┘
```
*Access multiplayer options to either host your own server or connect to an existing server. Select "Back" to return to the main menu.*

**"Esc" → Menu → Multiplayer → Connect to Server**
```
┌────────────────────────────┐
│   Connect to Server        │
│                            │
│   IP Address:              │
│   ______________________   │
│   Port Number:             │
│   ________                 │
│                            │
│   Enter, or Esc to Cancel  │
└────────────────────────────┘
```
*Enter the server IP address and port to join a multiplayer game. Press Enter to connect or Esc to cancel.*

**"Esc" → Menu → Player Name**
```
┌────────────────────────────┐
│     Enter Player Name:     │
│                            │
│   ______________________   │
│                            │
│                            │
│     Min 3 Characters!!!    │
│   Enter, or Esc to Cancel  │
└────────────────────────────┘
```
*Set your player name (minimum 3 characters required). This name will be visible to other players in multiplayer mode.*

**"Esc" → Menu → Save**

*Saves your current game progress including player position, health, world state, destroyed trees, and player settings. Your game is automatically saved when you exit, but you can manually save at any time from this menu option.*

**"Esc" → Menu → Exit**

*Exits the game and returns to desktop. The game automatically saves all player data including your position, health, inventory, and world changes before closing. All progress is preserved for your next session.*

## Game Features

### Player Character
- **Animated Human Sprite**: 64x64 pixel character with skin, black hair, blue shirt, brown pants, and black shoes
- **Walking Animation**: Arms and legs animate with opposite motion (realistic walking pattern)
- **Smooth Movement**: 200 pixels/second movement speed with arrow key controls
- **Camera Following**: Camera centers on player and follows movement
- **Health System**: Player health can be damaged and restored through gameplay

### World System
- **Infinite World**: Minecraft-like infinite terrain generation
- **Grass Background**: Seamless tiled grass texture covering the entire world
- **Fixed Viewport**: 800x600 camera view regardless of screen size
- **Chunk-Based Rendering**: Only renders visible areas for optimal performance

### Environmental Hazards
- **Cactus Damage**: Walking into cacti damages the player, adding environmental danger to exploration
- **Collision Detection**: Cacti have collision boxes that trigger damage on contact

### Tree System
- **Three Tree Types**:
  - **Regular Trees**: 64x64 brown trunk with green leaves
  - **Apple Trees**: 128x128 larger trees with red apples scattered on leaves - restore player health when harvested
  - **Banana Trees**: Restore player health when harvested
- **Random Generation**: 5% spawn chance per grass tile with distribution across tree types
- **Collision Detection**: Players cannot walk through trees (optimized collision boxes)
- **Deterministic Placement**: Same trees appear in same locations every time
- **Destructible**: Players can destroy trees by attacking them
- **Health Regeneration**: Trees that are damaged but not fully destroyed will slowly regain health over time

### Combat & Interaction System
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

### Controls
- **Arrow Keys**: Move character (Up/Down/Left/Right)
- **Spacebar**: Attack nearby trees
- **Escape**: Open/close menu
- **Fullscreen**: Maintains proper scaling and collision detection

### Multiplayer Features
- **Dedicated Server**: Run standalone server for multiplayer gameplay
- **Client Connection**: Connect to servers via IP address and port
- **Player Names**: Customizable player names (minimum 3 characters)
- **Server Configuration**: Configurable via server.properties file
- **Network Synchronization**: Real-time player position and action updates

### Technical Features
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

## Game Classes
- `MyGdxGame.java` - Main game loop and rendering
- `Player.java` - Character movement, animation, combat, and health management
- `Tree.java` - Regular tree implementation with health regeneration
- `AppleTree.java` - Large apple tree with fruit and health restoration
- `BananaTree.java` - Banana tree with health restoration
- `Cactus.java` - Environmental hazard with damage system
- `NetworkClient.java` - Multiplayer client connection handling
- `MenuSystem.java` - In-game menu and UI management

## Current Status
✅ Complete infinite world system  
✅ Animated player character  
✅ Tree generation and collision  
✅ Combat and destruction mechanics  
✅ Health bar visualization system  
✅ Individual attack range detection  
✅ Camera following system  
✅ Performance optimization  
✅ Multiplayer support with dedicated server  
✅ Player health system  
✅ Environmental hazards (cacti)  
✅ Health restoration (apple and banana trees)  
✅ Tree health regeneration system  
✅ Menu system with ESC key  

## Future Enhancements
- Resource collection (wood, apples, bananas)
- Inventory system
- Crafting mechanics
- Additional environmental features
- Sound effects
- Save/load functionality
- More multiplayer features

### 🚀 [Woodland Documentation](./docs/README.md)