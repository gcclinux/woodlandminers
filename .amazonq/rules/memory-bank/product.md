# Woodlanders - Product Overview

## Project Purpose
Woodlanders is a fully functional 2D multiplayer adventure game built with libGDX and Java, demonstrating AI-assisted game development through conversational prompts. The project showcases complete game systems including procedural world generation, multiplayer networking, inventory management, and environmental mechanics.

## Value Proposition
- **Complete Multiplayer Experience**: Full-featured networking with server-client architecture supporting 20+ concurrent players
- **Infinite World Exploration**: Procedurally generated terrain with multiple biomes (grass, sand)
- **Rich Game Systems**: Combat, inventory, health management, weather, and resource collection
- **Cross-Platform**: Runs on Windows, macOS, and Linux with consistent gameplay
- **Localization Support**: Multi-language UI (English, Polish, Portuguese, Dutch)
- **Persistent World**: Save/load system with separate singleplayer and multiplayer saves

## Key Features

### World & Environment
- Infinite procedurally generated world with chunk-based rendering
- Multiple biomes with distinct visual styles and generation patterns
- Dynamic rain system with random events (120s duration, 2-8 minute intervals)
- Compass navigation pointing toward spawn point
- Environmental hazards (cacti) with damage mechanics

### Character & Gameplay
- Animated player character with directional sprites
- Health system with damage and restoration mechanics
- Precise collision detection with optimized hitboxes
- Smooth movement with delta-time based physics
- Auto-consumption of items when health is low

### Trees & Resources
- 6 tree types: Small trees, Apple trees, Banana trees, Bamboo trees, Coconut trees, Cacti
- Combat system with visual health bars
- Health regeneration for damaged trees
- Resource drops (apples, bananas, wood, bamboo)
- Bamboo planting system on sand tiles with growth mechanics

### Inventory & Items
- 5-slot inventory system with item selection
- Collectible items: Apples, Bananas, Baby Bamboo, Bamboo Stacks, Wood Stacks
- Auto-pickup within 32-pixel radius
- Separate inventories for singleplayer and multiplayer modes
- Network synchronization across multiplayer sessions

### Multiplayer
- Dedicated server with configurable port and max clients
- Real-time player synchronization
- 22+ network message types for complete state sync
- Connection quality indicator
- Graceful disconnect/reconnect handling
- Last server memory for quick reconnection

### User Interface
- Wooden plank themed menu system
- In-game menu accessible via ESC key
- World save/load management
- Player name customization (3-15 characters)
- Language selection with auto-detection
- HUD elements: Health bar, inventory, compass, connection status

## Target Users
- Game developers learning multiplayer architecture
- Players interested in exploration and resource gathering
- Developers exploring AI-assisted development workflows
- Educators teaching game development concepts

## Use Cases
- Multiplayer cooperative exploration and resource gathering
- Single-player world exploration and survival
- Testing and demonstrating multiplayer networking
- Learning libGDX game development patterns
- Experimenting with procedural world generation
