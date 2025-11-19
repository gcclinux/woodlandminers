# Woodlanders - Project Structure

## Directory Organization

```
Woodlanders/
├── src/main/java/wagemaker/uk/     # Core game source code
├── src/test/java/                   # Unit tests
├── assets/                          # Game resources (sprites, fonts, localization)
├── docs/                            # Comprehensive documentation
├── specs/                           # Feature specifications
├── .kiro/specs/                     # Kiro AI feature specifications
├── .github/workflows/               # CI/CD pipelines
├── www/                             # GitHub Pages website
├── screenshots/                     # Game screenshots
├── build.gradle                     # Gradle build configuration
└── server.properties                # Server configuration
```

## Core Package Structure

### `wagemaker.uk.gdx` - Game Core
**Purpose**: Main game loop, rendering, and message handling
- `MyGdxGame.java` - Primary game class, rendering loop, input handling
- `GameMessageHandler.java` - Processes network messages and updates game state

### `wagemaker.uk.player` - Player System
**Purpose**: Player character management and remote player synchronization
- `Player.java` - Local player with movement, health, hunger, inventory
- `RemotePlayer.java` - Network-synchronized remote players

### `wagemaker.uk.world` - World Management
**Purpose**: World generation, persistence, and save/load functionality
- `WorldSaveManager.java` - Handles world serialization and file I/O
- `WorldSaveData.java` - Data structure for world state
- `WorldSaveInfo.java` - Metadata for saved worlds

### `wagemaker.uk.biome` - Biome System
**Purpose**: Terrain generation and biome-specific features
- `BiomeManager.java` - Manages biome zones and generation
- `BiomeType.java` - Enum defining biome types (GRASS, SAND)
- `BiomeZone.java` - Represents a biome region
- `BiomeTextureGenerator.java` - Generates biome-specific ground textures
- `BiomeConfig.java` - Configuration for biome generation

### `wagemaker.uk.network` - Networking Layer
**Purpose**: Client-server communication and state synchronization
- `GameServer.java` - Dedicated server managing clients and world state
- `GameClient.java` - Client connection and message handling
- `ClientConnection.java` - Server-side client connection management
- `NetworkMessage.java` - Base class for all network messages
- `MessageType.java` - Enum of 22+ message types
- `WorldState.java` - Complete world state for synchronization
- **Message Classes**: 30+ specialized message types for different game events

### `wagemaker.uk.inventory` - Inventory System
**Purpose**: Item management and inventory UI
- `Inventory.java` - Player inventory with 6 slots
- `InventoryManager.java` - Handles item consumption and selection
- `ItemType.java` - Enum defining all item types

### `wagemaker.uk.items` - Item Entities
**Purpose**: Collectible items in the game world
- `Apple.java`, `Banana.java` - Consumable items
- `BabyBamboo.java`, `BabyTree.java` - Plantable items
- `BambooStack.java`, `WoodStack.java` - Resource items
- `Pebble.java` - Stone resource

### `wagemaker.uk.trees` - Tree Entities
**Purpose**: Tree types with unique properties and collision
- `SmallTree.java` - Basic small tree
- `AppleTree.java`, `BananaTree.java` - Fruit-bearing trees
- `BambooTree.java`, `CoconutTree.java` - Special trees
- `Cactus.java` - Hazardous plant

### `wagemaker.uk.objects` - World Objects
**Purpose**: Interactive world objects
- `Stone.java` - Destructible stone objects

### `wagemaker.uk.planting` - Planting System
**Purpose**: Plant growth and transformation mechanics
- `PlantingSystem.java` - Manages planting logic and growth timers
- `PlantedBamboo.java` - Planted bamboo with growth state
- `PlantedTree.java` - Generic planted tree

### `wagemaker.uk.targeting` - Targeting System
**Purpose**: Tile-based targeting for item placement
- `TargetingSystem.java` - Manages targeting state and movement
- `TargetIndicatorRenderer.java` - Renders targeting visual
- `PlantingTargetValidator.java` - Validates planting locations
- `TargetingMode.java` - Enum for targeting modes

### `wagemaker.uk.weather` - Weather System
**Purpose**: Dynamic weather effects
- `RainSystem.java` - Main rain system controller
- `DynamicRainManager.java` - Manages rain events and timing
- `RainZoneManager.java` - Handles rain zones that follow player
- `RainRenderer.java` - Renders rain particles
- `RainParticle.java` - Individual rain particle
- `RainConfig.java` - Weather configuration

### `wagemaker.uk.respawn` - Resource Respawn
**Purpose**: Manages resource regeneration timers
- `RespawnManager.java` - Tracks and manages respawn timers
- `RespawnEntry.java` - Individual respawn entry
- `RespawnIndicator.java` - Visual respawn indicators
- `RespawnConfig.java` - Respawn timing configuration

### `wagemaker.uk.ui` - User Interface
**Purpose**: All UI components and dialogs
- `GameMenu.java` - Main in-game menu
- `HealthBarUI.java` - Health and hunger display
- `InventoryRenderer.java` - Inventory UI rendering
- `Compass.java` - Navigation compass
- `ConnectionQualityIndicator.java` - Network status display
- **Dialog Classes**: 8+ specialized dialogs for various interactions

### `wagemaker.uk.localization` - Internationalization
**Purpose**: Multi-language support
- `LocalizationManager.java` - Manages language loading and switching
- `LanguageChangeListener.java` - Interface for language change events

### `wagemaker.uk.server` - Dedicated Server
**Purpose**: Standalone server launcher and monitoring
- `DedicatedServerLauncher.java` - Server entry point
- `ServerConfig.java` - Server configuration
- `ServerLogger.java` - Server logging system
- `ServerMonitor.java` - Server monitoring and statistics

### `wagemaker.uk.desktop` - Desktop Launcher
**Purpose**: Desktop application entry point
- `DesktopLauncher.java` - Main application launcher

### `wagemaker.uk.client` - Client Configuration
**Purpose**: Client-side configuration
- `PlayerConfig.java` - Player settings and preferences

## Asset Organization

### `assets/sprites/`
- `player/` - Player character sprite sheets with directional animations
- `assets.png` - Main texture atlas

### `assets/fonts/`
- Custom TTF fonts for UI rendering
- Retro pixel fonts for authentic game feel

### `assets/localization/`
- `en.json` - English translations
- `pl.json` - Polish translations
- `pt.json` - Portuguese translations
- `nl.json` - Dutch translations
- `de.json` - German translations

### `assets/ui/`
- `compass_background.png` - Compass background texture
- `compass_needle.png` - Compass needle texture

## Architectural Patterns

### Client-Server Architecture
- **Server-Authoritative**: Server validates all actions and maintains canonical state
- **Client Prediction**: Clients predict movement for smooth gameplay
- **State Synchronization**: Regular world state updates keep clients in sync

### Entity-Component Pattern
- Trees, items, and players are entities with specific components
- Collision detection, rendering, and behavior are component-based
- Allows flexible entity composition and extension

### Message-Based Communication
- All network communication uses typed message objects
- Message handlers process incoming messages and update state
- Supports 22+ message types for comprehensive synchronization

### Chunk-Based World
- World divided into chunks for efficient rendering
- Only visible chunks are rendered and updated
- Enables infinite world generation with bounded memory

### Manager Pattern
- Dedicated managers for major systems (Biome, Rain, Respawn, Inventory)
- Centralized control and state management
- Clean separation of concerns

### Save/Load System
- JSON-based serialization for world and player data
- Separate save directories for singleplayer and multiplayer
- Automatic backup creation before overwriting saves

## Build System

### Gradle Configuration
- **Java 21**: Modern Java with latest language features
- **libGDX 1.12.1**: Game framework for 2D rendering and input
- **JUnit 5 + Mockito**: Comprehensive testing infrastructure
- **Dual JAR Output**: Separate client and server JARs

### Build Tasks
- `gradle build` - Builds both client and server JARs
- `gradle run` - Runs the game client
- `gradle test` - Runs unit tests
- `gradle jar` - Creates client JAR
- `gradle serverJar` - Creates headless server JAR

## Configuration Files

### `build.gradle`
- Project dependencies and versions
- Build tasks for client and server
- Test configuration

### `server.properties`
- Server port and max clients
- Network configuration
- Server-specific settings

### `jvm.properties`
- JVM arguments and memory settings

## Documentation Structure

### `docs/` Directory
- **FEATURES.md** - Comprehensive feature list
- **INSTALLATION.md** - Setup and installation guide
- **CLASSES.md** - Class documentation
- **SCREENSHOTS.md** - Visual showcase
- **SERVER_SETUP.md** - Server configuration guide
- **TROUBLESHOOTING.md** - Common issues and solutions
- **Feature-specific docs** - Detailed documentation for major features

### `.kiro/specs/` Directory
- Kiro AI feature specifications
- 24+ feature specs documenting AI-driven development process
- Requirements, design, and implementation details for each feature

## Deployment

### GitHub Actions Workflows
- **release.yml** - Automated releases for Windows/macOS
- **release-linux.yml** - Linux release builds
- **pages.yml** - GitHub Pages deployment

### Distribution
- Client JAR with all dependencies and assets
- Server JAR optimized for headless operation
- Cross-platform support (Windows, macOS, Linux)
