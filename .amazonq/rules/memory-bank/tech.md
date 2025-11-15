# Woodlanders - Technology Stack

## Programming Language
- **Java 21**: Source and target compatibility set to Java 21
- **Build Tool**: Gradle 9.0.0+ with Java plugin and application plugin

## Core Dependencies

### Game Framework
- **libGDX 1.12.1**: Cross-platform game development framework
  - `gdx`: Core framework
  - `gdx-backend-lwjgl3`: Desktop rendering backend
  - `gdx-platform`: Platform-specific natives
  - `gdx-freetype`: Font rendering support

### Testing
- **JUnit Jupiter 5.10.0**: Unit testing framework
- **Mockito 5.5.0**: Mocking framework for tests
- **libGDX Headless Backend**: Headless testing support

## Build Configuration

### Main Application
- **Main Class**: `wagemaker.uk.desktop.DesktopLauncher`
- **JAR Output**: `woodlanders-client.jar` (full game with rendering)
- **Working Directory**: Project root (for asset access)

### Dedicated Server
- **Main Class**: `wagemaker.uk.server.DedicatedServerLauncher`
- **JAR Output**: `woodlanders-server.jar` (headless, no rendering)
- **Excluded Dependencies**: LWJGL, rendering backends, platform natives, freetype

### Asset Configuration
- **Asset Directory**: `assets/` (configured as resource source)
- **Fonts**: `assets/fonts/` (slkscr.ttf, slkscrb.ttf)
- **Localization**: `assets/localization/` (en.json, pl.json, pt.json, nl.json)
- **Sprites**: `assets/sprites/` (player animations, assets.png)
- **UI Assets**: `assets/ui/` (compass_background.png, compass_needle.png)

## Build Commands

### Development
```bash
./gradlew run                    # Run game client
./gradlew build                  # Build both client and server JARs
./gradlew test                   # Run all tests
./gradlew clean                  # Clean build artifacts
```

### Production
```bash
./gradlew jar                    # Build client JAR
./gradlew serverJar              # Build server JAR
java -jar woodlanders-client.jar # Run client
java -jar woodlanders-server.jar # Run server
```

## Project Version
- **Current Version**: 0.0.8
- **Version Management**: Defined in build.gradle

## Test Configuration
- **Test Framework**: JUnit Platform
- **Test Logging**: Shows passed, skipped, and failed tests with full exception format
- **Test Execution**: `./gradlew test`

## Networking
- **Protocol**: Custom TCP-based protocol
- **Message Format**: Serialized Java objects
- **Default Port**: 25565 (configurable)
- **Max Clients**: 20 (configurable)
- **Heartbeat Interval**: 5 seconds
- **Connection Timeout**: 15 seconds

## Performance Optimizations
- **Chunk-Based Rendering**: Only visible chunks rendered
- **Spatial Partitioning**: Optimized collision detection
- **Message Batching**: Network message rate limiting
- **Delta-Time Physics**: Frame-rate independent movement
- **Texture Atlas**: Sprite management efficiency
- **Memory-Efficient Generation**: Procedural world generation

## Platform Support
- **Windows**: Full support with LWJGL3 backend
- **macOS**: Full support with LWJGL3 backend
- **Linux**: Full support with LWJGL3 backend

## Configuration Files
- **server.properties**: Server configuration (port, max clients, logging)
- **woodlanders.json**: Player configuration (OS-specific config directory)
- **World Saves**: Stored in OS-specific directories
  - Windows: `%APPDATA%/Woodlanders/`
  - macOS: `~/Library/Application Support/Woodlanders/`
  - Linux: `~/.config/woodlanders/`

## Development Environment
- **IDE Support**: IntelliJ IDEA, Eclipse, VS Code (with Java extensions)
- **Gradle Wrapper**: Included for consistent builds
- **Git Integration**: GitHub Actions for CI/CD (release workflows)
