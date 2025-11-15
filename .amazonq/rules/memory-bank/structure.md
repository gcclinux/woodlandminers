# Woodlanders - Project Structure

## Directory Organization

```
Woodlanders/
├── src/main/java/wagemaker/uk/
│   ├── biome/              # Biome generation and management
│   ├── client/             # Client-side configuration
│   ├── desktop/            # Desktop launcher entry point
│   ├── gdx/                # Core game loop and rendering
│   ├── inventory/          # Inventory system
│   ├── items/              # Item definitions and types
│   ├── localization/       # Multi-language support
│   ├── network/            # Networking and message protocol
│   ├── objects/            # Game objects (stones, etc.)
│   ├── planting/           # Bamboo planting system
│   ├── player/             # Player and remote player logic
│   ├── server/             # Dedicated server implementation
│   ├── trees/              # Tree types and mechanics
│   ├── ui/                 # Menu and UI systems
│   ├── weather/            # Rain and weather systems
│   └── world/              # World save/load management
├── src/test/java/          # Unit and integration tests
├── assets/
│   ├── fonts/              # Game fonts (slkscr.ttf, slkscrb.ttf)
│   ├── localization/       # Language JSON files (en, pl, pt, nl)
│   ├── sprites/            # Player and entity sprites
│   ├── textures/           # Terrain and object textures
│   └── ui/                 # UI assets (compass, etc.)
├── docs/                   # Documentation and guides
├── .kiro/specs/            # Kiro AI specification files
├── .amazonq/rules/         # Amazon Q rules and memory bank
├── build.gradle            # Gradle build configuration
├── settings.gradle         # Gradle settings
└── server.properties       # Server configuration
```

## Core Components

### Game Loop (gdx/)
- **MyGdxGame.java**: Main game loop, rendering pipeline, game state management
- **GameMessageHandler.java**: Handles incoming network messages during gameplay

### Player System (player/)
- **Player.java**: Local player with movement, animation, combat, health
- **RemotePlayer.java**: Remote player representation in multiplayer

### World Generation (biome/)
- **BiomeManager.java**: Generates and manages biome zones
- **BiomeType.java**: Enum for grass and sand biomes
- **BiomeTextureGenerator.java**: Dynamic texture generation per biome
- **BiomeConfig.java**: Biome configuration parameters

### Tree System (trees/)
- **SmallTree.java**: Small decorative trees
- **AppleTree.java**: Fruit-bearing trees with apple drops
- **BananaTree.java**: Fruit-bearing trees with banana drops
- **BambooTree.java**: Bamboo with unique collision
- **CoconutTree.java**: Coconut trees
- **Cactus.java**: Environmental hazard with damage

### Inventory & Items (inventory/, items/)
- **Inventory.java**: 5-slot inventory data structure
- **InventoryManager.java**: Inventory operations and auto-consumption
- **ItemType.java**: Item type enumeration
- **Apple.java, Banana.java, BabyBamboo.java, BambooStack.java, WoodStack.java**: Item implementations

### Planting System (planting/)
- **PlantingSystem.java**: Core planting logic and validation
- **PlantedBamboo.java**: Planted bamboo entity with growth timer

### Weather System (weather/)
- **DynamicRainManager.java**: Rain event management and scheduling
- **RainSystem.java**: Rain particle system
- **RainRenderer.java**: Rain rendering
- **RainZoneManager.java**: Rain zone tracking
- **RainConfig.java**: Rain configuration

### Networking (network/)
- **GameServer.java**: Multiplayer server implementation
- **GameClient.java**: Multiplayer client implementation
- **ClientConnection.java**: Individual client connection handler
- **NetworkMessage.java**: Base message class
- **MessageType.java**: Enum of 22+ message types
- **PlayerState.java, TreeState.java, ItemState.java, WorldState.java**: Network data structures

### User Interface (ui/)
- **GameMenu.java**: Main in-game menu system
- **MultiplayerMenu.java**: Multiplayer connection options
- **ConnectDialog.java**: Server connection dialog
- **ServerHostDialog.java**: Server hosting dialog
- **WorldSaveDialog.java, WorldLoadDialog.java**: World management
- **Compass.java**: Navigation compass UI
- **InventoryRenderer.java**: Inventory display
- **ConnectionQualityIndicator.java**: Network status display

### World Persistence (world/)
- **WorldSaveManager.java**: Save/load operations
- **WorldSaveData.java**: Complete world state structure
- **WorldSaveInfo.java**: Save metadata

### Server (server/)
- **DedicatedServerLauncher.java**: Standalone server entry point
- **ServerConfig.java**: Server configuration management
- **ServerLogger.java**: Server logging
- **ServerMonitor.java**: Server statistics and monitoring

### Localization (localization/)
- **LocalizationManager.java**: Multi-language support
- **LanguageChangeListener.java**: Language change notifications

## Architectural Patterns

### Message-Driven Architecture
Network communication uses a message-based protocol with 22+ message types:
- Player movement, health, attacks
- Tree creation, destruction, health updates
- Item spawning and pickup
- Inventory synchronization
- World state updates
- Connection management (heartbeat, ping/pong)

### Chunk-Based Rendering
Only visible world chunks are rendered, optimizing performance for infinite worlds.

### Server-Authoritative Design
Server validates all actions and broadcasts state changes to clients.

### Separate State Management
- Singleplayer: Local world state
- Multiplayer: Server-managed world state with client prediction

### Component-Based Entity System
Trees, items, and players are independent components with collision and rendering.

## Data Flow

### Singleplayer
```
Player Input → MyGdxGame → Local World State → Rendering
```

### Multiplayer
```
Player Input → GameClient → NetworkMessage → GameServer → 
World State Update → Broadcast to Clients → GameClient → 
Local Rendering
```

## Configuration Files
- **build.gradle**: Java 21, libGDX 1.12.1, JUnit 5, Mockito
- **server.properties**: Server port, max clients, logging level
- **assets/localization/*.json**: Language strings
