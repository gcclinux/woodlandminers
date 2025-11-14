# Project Structure

## Directory Organization

```
Woodlanders/
├── .amazonq/rules/memory-bank/    # AI context and project memory
├── .github/workflows/             # CI/CD pipelines for releases
├── .kiro/specs/                   # Feature specifications (19 specs)
├── assets/                        # Game resources
│   ├── fonts/                     # Pixel fonts (slkscr.ttf)
│   ├── localization/              # Language files (en, nl, pl, pt)
│   ├── sprites/                   # Game sprites and textures
│   └── ui/                        # UI elements (compass)
├── docs/                          # Technical documentation
├── screenshots/                   # Game screenshots
├── src/main/java/wagemaker/uk/   # Source code
│   ├── biome/                     # Biome generation and management
│   ├── client/                    # Game client networking
│   ├── desktop/                   # Desktop launcher
│   ├── gdx/                       # Main game loop and core logic
│   ├── inventory/                 # Inventory management
│   ├── items/                     # Item definitions and logic
│   ├── localization/              # Localization system
│   ├── network/                   # Network protocol and messages
│   ├── objects/                   # Game objects (stones, pebbles)
│   ├── planting/                  # Planting mechanics
│   ├── player/                    # Player entity and logic
│   ├── server/                    # Dedicated server
│   ├── trees/                     # Tree entities and types
│   ├── ui/                        # User interface components
│   ├── weather/                   # Weather system
│   └── world/                     # World generation and persistence
└── src/test/java/wagemaker/uk/   # Unit and integration tests
```

## Core Components

### Game Core (gdx/)
- **MyGdxGame**: Main game class, render loop, input handling, game state management
- Central coordinator for all game systems
- Manages collections of entities (players, trees, items, stones)
- Handles both singleplayer and multiplayer modes

### Player System (player/)
- **Player**: Player entity with position, health, animation, inventory
- Movement and collision detection
- Attack mechanics
- Network synchronization

### World System (world/)
- **WorldGenerator**: Procedural world generation with biomes
- **WorldSaveManager**: Save/load world state to JSON
- **WorldSaveData**: Data structure for world persistence
- Chunk-based rendering for infinite worlds

### Biome System (biome/)
- **BiomeType**: Enum defining biome types (GRASS, SAND)
- **BiomeManager**: Biome generation and tile management
- Biome-specific textures and generation rules

### Tree System (trees/)
- **Tree**: Base tree entity with health, collision, regeneration
- **TreeType**: Enum for tree varieties (SMALL, REGULAR, APPLE, BANANA, BAMBOO, COCONUT)
- Health bars and damage mechanics
- Item drops on destruction

### Object System (objects/)
- **Stone**: Destructible stone objects that drop pebbles
- **Pebble**: Collectible resource items
- Health and collision mechanics similar to trees

### Inventory System (inventory/)
- **InventoryManager**: Manages player inventory (6 slots)
- **ItemType**: Enum for item types (APPLE, BANANA, BABY_BAMBOO, BAMBOO_STACK, WOOD_STACK, PEBBLE)
- Auto-consumption logic
- Network synchronization

### Items System (items/)
- **Apple, Banana, BabyBamboo, BambooStack, WoodStack**: Collectible item entities
- Pickup mechanics and health restoration
- Sprite rendering

### Planting System (planting/)
- **PlantingManager**: Handles bamboo planting mechanics
- Growth timers and transformation logic
- Biome-specific planting rules (bamboo on sand)

### Weather System (weather/)
- **WeatherManager**: Dynamic rain events
- Random timing and duration
- Zone-based rendering that follows player

### UI System (ui/)
- **MenuManager**: Game menu system with wooden plank theme
- **InventoryRenderer**: Renders inventory slots and items
- **CompassRenderer**: Compass pointing to spawn
- Health bars and HUD elements

### Localization System (localization/)
- **LocalizationManager**: Multi-language support
- JSON-based language files
- Automatic system language detection
- Supports English, Polish, Portuguese, Dutch

### Network System (network/)
- **GameServer**: Dedicated server with client management
- **GameClient**: Client-side networking
- **WorldState**: Complete world state synchronization
- **Message Types**: 22+ message types for full game sync
  - Player messages (position, health, attack)
  - Tree messages (health, destruction)
  - Stone messages (health, destruction)
  - Item messages (spawn, pickup)
  - Inventory messages (update, sync)
  - World messages (state, sync)

### Server System (server/)
- **DedicatedServerLauncher**: Standalone server entry point
- Headless operation (no rendering)
- Configurable via server.properties

### Desktop System (desktop/)
- **DesktopLauncher**: Game client entry point
- Window configuration
- LibGDX initialization

## Architectural Patterns

### Entity-Component Pattern
- Entities (Player, Tree, Stone, Item) have position, sprite, collision
- Components handle specific behaviors (health, inventory, animation)

### Server-Authoritative Networking
- Server validates all actions
- Clients send requests, server broadcasts state
- Prevents cheating and ensures consistency

### Message-Based Communication
- All network communication via typed messages
- Serializable message classes
- Message type enum for routing

### Chunk-Based Rendering
- Only render visible world sections
- Spatial partitioning for collision detection
- Efficient memory usage for infinite worlds

### Save/Load System
- JSON-based world persistence
- Separate saves for singleplayer/multiplayer
- Automatic backups on save
- OS-specific config directories

### Localization Pattern
- Key-based string lookup
- JSON language files
- Fallback to English for missing keys
- Runtime language switching

## Key Relationships

- **MyGdxGame** orchestrates all systems
- **Player** interacts with **InventoryManager**, **WorldGenerator**, **Trees**, **Stones**
- **GameServer** manages **WorldState** and broadcasts to **GameClient** instances
- **MenuManager** controls game flow and triggers save/load via **WorldSaveManager**
- **BiomeManager** influences **WorldGenerator** and **PlantingManager**
- **WeatherManager** affects rendering but not gameplay mechanics
- **LocalizationManager** provides strings to **MenuManager** and **UI** components

## Build Outputs

### Client JAR (woodlanders-client.jar)
- Full game with rendering
- Includes all assets and dependencies
- Main class: DesktopLauncher

### Server JAR (woodlanders-server.jar)
- Headless server only
- Excludes rendering libraries and assets
- Smaller file size
- Main class: DedicatedServerLauncher

## Configuration Files

- **build.gradle**: Build configuration, dependencies, JAR tasks
- **settings.gradle**: Project name
- **server.properties**: Server configuration (port, max clients)
- **woodlanders.json**: Player data (position, health, inventory, name)
- **world-saves/**: World save files (.wld format)
