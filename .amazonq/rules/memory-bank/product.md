# Product Overview

## Project Purpose

Woodlanders is a 2D multiplayer adventure game built entirely through AI-assisted development. It serves as both an entertaining game and a proof-of-concept demonstrating that complex software can be built through conversational AI without manual coding. The project showcases the capabilities of AI-driven development using the Kiro IDE.

## Value Proposition

- **Zero-Code Development**: Complete game built using only natural language prompts and AI assistance
- **Multiplayer Experience**: Real-time networked gameplay with dedicated server support
- **Infinite Exploration**: Procedurally generated worlds with multiple biomes
- **Resource Management**: Inventory, crafting, and survival mechanics
- **Cross-Platform**: Java-based game runs on Windows, macOS, and Linux

## Key Features

### World & Environment
- Infinite procedurally generated worlds with chunk-based rendering
- Multiple biomes (grass, sand) with distinct visual styles
- Dynamic weather system with random rain events
- Compass navigation pointing to spawn point
- Complete world save/load system with separate singleplayer/multiplayer saves

### Character & Gameplay
- Animated player character with directional sprites
- Health system with damage and restoration mechanics
- Precise collision detection for all game objects
- Combat system for attacking trees and environmental objects
- Planting system for bamboo cultivation

### Resources & Inventory
- 6-slot inventory system with multiple item types
- Collectible items: apples, bananas, baby bamboo, bamboo stacks, wood stacks, pebbles
- Auto-consumption of items when health is low
- Item drops from destroyed trees
- Network-synchronized inventory across multiplayer sessions

### Trees & Objects
- 6 tree types: small trees, regular trees, apple trees, banana trees, bamboo trees, coconut trees
- Health bars and regeneration for damaged trees
- Environmental hazards (cacti) that damage players
- Stone objects that can be destroyed to collect pebbles
- Growth mechanics for planted bamboo

### Multiplayer
- Dedicated server with configurable settings (port, max clients)
- Real-time synchronization of player positions, actions, and world state
- 22+ network message types for complete game state sync
- Connection quality indicator
- Separate player positions for singleplayer and multiplayer modes
- Graceful disconnect/reconnect with last server memory

### User Interface
- Wooden plank themed menu system
- Player name customization (3-15 characters)
- HUD elements: health bar, inventory display, compass, connection status
- Multi-language support: English, Polish, Portuguese, Dutch
- Automatic system language detection
- World management: save, load, and manage multiple world saves

## Target Users

### Primary Users
- **Gamers**: Players looking for a casual 2D multiplayer adventure experience
- **Indie Game Enthusiasts**: Players interested in procedurally generated worlds and survival mechanics
- **Multiplayer Fans**: Groups wanting to play together in a shared world

### Secondary Users
- **AI Development Researchers**: Studying AI-assisted software development capabilities
- **Game Developers**: Learning from an AI-built game architecture
- **Educators**: Teaching game development concepts through a complete example
- **Open Source Contributors**: Extending and improving the game

## Use Cases

### Single Player
- Explore infinite procedurally generated worlds
- Gather resources and manage inventory
- Plant and harvest bamboo
- Survive environmental hazards
- Save and load multiple worlds

### Multiplayer
- Host a server for friends to join
- Connect to remote servers
- Collaborate on resource gathering
- Share a persistent world state
- See other players in real-time

### Development & Learning
- Study AI-assisted game development techniques
- Examine libGDX framework implementation
- Learn multiplayer networking patterns
- Understand procedural generation algorithms
- Contribute new features through AI prompts

## Technical Highlights

- **Performance**: Chunk-based rendering, spatial partitioning, texture atlases
- **Network**: Custom TCP protocol with server-authoritative architecture
- **Architecture**: Clean separation of concerns (client, server, network, world, UI)
- **Testing**: JUnit 5 with Mockito for unit and integration tests
- **Build**: Gradle with separate client and server JAR outputs
- **Version**: Java 21 with libGDX 1.12.1
