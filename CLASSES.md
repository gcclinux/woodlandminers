### Core Game
- `MyGdxGame.java` - Main game loop, rendering, and game state management
- `DesktopLauncher.java` - Desktop application entry point

### Player System
- `Player.java` - Character movement, animation, combat, and health management
- `RemotePlayer.java` - Remote player representation in multiplayer
- `PlayerConfig.java` - Player configuration and persistence

### Trees & Environment
- `SmallTree.java` - Small decorative trees
- `AppleTree.java` - Large apple trees with fruit drops
- `BananaTree.java` - Banana trees with fruit drops
- `BambooTree.java` - Bamboo trees with unique collision
- `CoconutTree.java` - Coconut trees
- `Cactus.java` - Environmental hazard with damage system

### Items & Inventory
- `Apple.java` - Apple item with health restoration
- `Banana.java` - Banana item with health restoration
- `BabyBamboo.java` - Baby bamboo resource
- `BambooStack.java` - Bamboo stack resource
- `WoodStack.java` - Wood stack resource
- `Inventory.java` - Inventory data structure
- `InventoryManager.java` - Inventory operations and auto-consumption
- `ItemType.java` - Item type enumeration

### Planting System
- `PlantingSystem.java` - Core planting logic and validation
- `PlantedBamboo.java` - Planted bamboo entity with growth timer

### Biome System
- `BiomeType.java` - Biome type enumeration (grass, sand)
- `BiomeManager.java` - Biome generation and management
- `BiomeConfig.java` - Biome configuration
- `BiomeTextureGenerator.java` - Dynamic biome texture generation
- `BiomeZone.java` - Biome zone data structure

### Weather System
- `RainSystem.java` - Rain particle system
- `DynamicRainManager.java` - Dynamic rain event management
- `RainZoneManager.java` - Rain zone management
- `RainRenderer.java` - Rain rendering
- `RainParticle.java` - Individual rain particle
- `RainZone.java` - Rain zone data structure
- `RainConfig.java` - Rain configuration

### World Management
- `WorldSaveManager.java` - World save/load operations
- `WorldSaveData.java` - World save data structure
- `WorldSaveInfo.java` - World save metadata

### User Interface
- `GameMenu.java` - Main in-game menu system
- `MultiplayerMenu.java` - Multiplayer connection menu
- `ConnectDialog.java` - Server connection dialog
- `ServerHostDialog.java` - Server hosting dialog
- `ErrorDialog.java` - Error message dialog
- `WorldSaveDialog.java` - World save dialog
- `WorldLoadDialog.java` - World load dialog
- `WorldManageDialog.java` - World management dialog
- `LanguageDialog.java` - Language selection dialog
- `Compass.java` - Compass navigation UI
- `InventoryRenderer.java` - Inventory display renderer
- `ConnectionQualityIndicator.java` - Network status indicator

### Localization
- `LocalizationManager.java` - Multi-language support manager
- `LanguageChangeListener.java` - Language change notification interface

### Networking
- `GameServer.java` - Multiplayer server implementation
- `GameClient.java` - Multiplayer client implementation
- `ClientConnection.java` - Client connection handler
- `NetworkMessage.java` - Base network message class
- `MessageHandler.java` - Message handling interface
- `GameMessageHandler.java` - Game-specific message handler
- `DefaultMessageHandler.java` - Default message handler

#### Network Messages
- `PlayerJoinMessage.java` - Player join notification
- `PlayerLeaveMessage.java` - Player leave notification
- `PlayerMovementMessage.java` - Player position updates
- `PlayerHealthUpdateMessage.java` - Player health sync
- `AttackActionMessage.java` - Attack action sync
- `TreeDestroyedMessage.java` - Tree destruction sync
- `TreeHealthUpdateMessage.java` - Tree health sync
- `TreeRemovalMessage.java` - Tree removal sync
- `ItemSpawnMessage.java` - Item spawn notification
- `ItemPickupMessage.java` - Item pickup sync
- `InventoryUpdateMessage.java` - Inventory state sync
- `InventorySyncMessage.java` - Full inventory sync
- `WorldStateMessage.java` - Complete world state sync
- `WorldStateUpdateMessage.java` - Incremental world updates
- `ConnectionAcceptedMessage.java` - Connection acceptance
- `ConnectionRejectedMessage.java` - Connection rejection
- `HeartbeatMessage.java` - Connection keepalive
- `PingMessage.java` - Latency measurement
- `PongMessage.java` - Ping response
- `PositionCorrectionMessage.java` - Server position correction
- `BambooPlantMessage.java` - Bamboo planting sync
- `BambooTransformMessage.java` - Bamboo transformation sync

#### Network Data Structures
- `PlayerState.java` - Player state data
- `TreeState.java` - Tree state data
- `ItemState.java` - Item state data
- `PlantedBambooState.java` - Planted bamboo state data
- `WorldState.java` - Complete world state
- `WorldStateUpdate.java` - World state update
- `Direction.java` - Movement direction enum
- `MessageType.java` - Message type enumeration
- `TreeType.java` - Network tree type enum
- `ItemType.java` - Network item type enum

### Server
- `DedicatedServerLauncher.java` - Standalone server entry point
- `ServerConfig.java` - Server configuration management
- `ServerLogger.java` - Server logging system
- `ServerMonitor.java` - Server monitoring and statistics