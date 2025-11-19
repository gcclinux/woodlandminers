# Woodlanders - Technology Stack

## Programming Languages

### Java 21
- **Version**: Java 21 (LTS)
- **Source Compatibility**: JavaVersion.VERSION_21
- **Target Compatibility**: JavaVersion.VERSION_21
- **Features Used**:
  - Records for data classes
  - Pattern matching
  - Enhanced switch expressions
  - Text blocks for multi-line strings
  - Sealed classes for type hierarchies

## Core Framework

### libGDX 1.12.1
**Purpose**: Cross-platform game development framework

**Components Used**:
- `gdx-core` - Core game loop, rendering, input handling
- `gdx-backend-lwjgl3` - Desktop backend using LWJGL3
- `gdx-platform:natives-desktop` - Native libraries for desktop
- `gdx-freetype` - TrueType font rendering
- `gdx-freetype-platform:natives-desktop` - FreeType native libraries
- `gdx-backend-headless` - Headless backend for server (test only)

**Key Features**:
- 2D sprite rendering with SpriteBatch
- Texture management and atlases
- Input handling (keyboard, mouse)
- Shape rendering for UI and debug
- Camera and viewport management
- File I/O utilities
- JSON serialization

## Build System

### Gradle
**Version**: Managed by Gradle wrapper
**Build File**: `build.gradle`

**Key Configurations**:
```gradle
plugins {
    id 'java'
    id 'application'
}

version = '0.0.8'
sourceCompatibility = JavaVersion.VERSION_21
targetCompatibility = JavaVersion.VERSION_21
```

**Main Class**: `wagemaker.uk.desktop.DesktopLauncher`

**Custom Tasks**:
- `jar` - Creates client JAR with all dependencies and assets
- `serverJar` - Creates headless server JAR without rendering libraries

## Testing Framework

### JUnit 5 (Jupiter)
**Version**: 5.10.0
**Purpose**: Unit testing framework

**Components**:
- `junit-jupiter` - Core JUnit 5 API
- `junit-platform-launcher` - Test execution platform

### Mockito
**Version**: 5.5.0
**Purpose**: Mocking framework for unit tests

**Components**:
- `mockito-core` - Core mocking functionality
- `mockito-junit-jupiter` - JUnit 5 integration

## Networking

### Java NIO (Non-blocking I/O)
**Purpose**: TCP socket communication for multiplayer

**Key Classes**:
- `ServerSocketChannel` - Server socket for accepting connections
- `SocketChannel` - Client socket for network communication
- `ByteBuffer` - Efficient byte buffer for message serialization

**Protocol**:
- Custom TCP-based protocol
- Message format: `[4-byte length][message type][payload]`
- 22+ message types for comprehensive synchronization

## Data Serialization

### JSON (libGDX Json)
**Purpose**: World and player data persistence

**Usage**:
- World save/load system
- Player configuration
- Localization files
- Network message payloads (some messages)

### Custom Binary Protocol
**Purpose**: Network message serialization

**Format**:
- Fixed-size headers for efficiency
- Type-specific serialization methods
- Optimized for low bandwidth usage

## Asset Management

### Texture Atlas
**Purpose**: Efficient sprite management
- Single texture atlas for all sprites
- Reduces draw calls and improves performance
- Managed by libGDX TextureAtlas

### Font Rendering
**Purpose**: Custom font rendering with FreeType
- TrueType font support
- Dynamic font generation at runtime
- Multiple font sizes and styles

### Localization
**Purpose**: Multi-language support
- JSON-based translation files
- 5 supported languages (en, pl, pt, nl, de)
- Runtime language switching

## Development Tools

### IDE Support
- **Primary**: Kiro IDE (AI-assisted development)
- **Compatible**: IntelliJ IDEA, Eclipse, VS Code with Java extensions

### Version Control
- **Git**: Source control
- **GitHub**: Repository hosting and CI/CD

### CI/CD
- **GitHub Actions**: Automated builds and releases
- **Workflows**:
  - `release.yml` - Windows/macOS releases
  - `release-linux.yml` - Linux releases
  - `pages.yml` - GitHub Pages deployment

## Runtime Dependencies

### Client Dependencies
```gradle
implementation "com.badlogicgames.gdx:gdx:1.12.1"
implementation "com.badlogicgames.gdx:gdx-backend-lwjgl3:1.12.1"
implementation "com.badlogicgames.gdx:gdx-platform:1.12.1:natives-desktop"
implementation "com.badlogicgames.gdx:gdx-freetype:1.12.1"
implementation "com.badlogicgames.gdx:gdx-freetype-platform:1.12.1:natives-desktop"
```

### Server Dependencies
- Core libGDX only (no rendering backends)
- Excludes LWJGL, platform natives, and FreeType
- Minimal footprint for headless operation

### Test Dependencies
```gradle
testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
testImplementation 'org.mockito:mockito-core:5.5.0'
testImplementation 'org.mockito:mockito-junit-jupiter:5.5.0'
testImplementation "com.badlogicgames.gdx:gdx-backend-headless:1.12.1"
```

## Platform Support

### Operating Systems
- **Windows**: Primary development and testing platform
- **macOS**: Supported via LWJGL3 backend
- **Linux**: Supported via LWJGL3 backend

### Architecture
- **x64**: Primary architecture
- **ARM64**: Supported on macOS (Apple Silicon)

## Performance Optimizations

### Rendering
- **Chunk-based rendering**: Only visible chunks rendered
- **Texture atlases**: Minimize draw calls
- **SpriteBatch**: Efficient sprite batching
- **Viewport culling**: Skip off-screen entities

### Memory Management
- **Object pooling**: Reuse objects where possible
- **Lazy loading**: Load assets on demand
- **Chunk unloading**: Unload distant chunks
- **Texture disposal**: Proper resource cleanup

### Networking
- **Message batching**: Combine multiple messages
- **Delta compression**: Send only changes
- **Rate limiting**: Prevent message flooding
- **Heartbeat system**: Efficient connection monitoring

## Development Commands

### Build Commands
```bash
# Build both client and server JARs
gradle build

# Run the game client
gradle run

# Run unit tests
gradle test

# Create client JAR only
gradle jar

# Create server JAR only
gradle serverJar

# Clean build artifacts
gradle clean
```

### Server Commands
```bash
# Start server (Windows)
start-server.bat

# Start server (Unix/Linux/macOS)
./start-server.sh

# Start server with custom settings (Unix/Linux/macOS)
./start-server-advanced.sh
```

### Test Commands
```bash
# Run all tests
gradle test

# Run tests with verbose output
gradle test --info

# Run specific test class
gradle test --tests "ClassName"
```

## Configuration Files

### `build.gradle`
- Project metadata and version
- Dependencies and repositories
- Build tasks and configurations
- Source sets and resource directories

### `settings.gradle`
- Root project name: `Woodlanders`

### `server.properties`
- Server port (default: 25565)
- Max clients (default: 20)
- Server-specific settings

### `jvm.properties`
- JVM arguments
- Memory settings
- Performance tuning

## Asset Formats

### Images
- **PNG**: Sprites, textures, UI elements
- **Format**: RGBA, various sizes
- **Atlas**: Packed into single texture atlas

### Fonts
- **TTF**: TrueType fonts
- **Rendering**: FreeType for dynamic generation
- **Sizes**: Multiple sizes generated at runtime

### Localization
- **JSON**: Translation files
- **Encoding**: UTF-8
- **Structure**: Key-value pairs for all UI text

## External Resources

### Documentation
- **libGDX Wiki**: https://libgdx.com/wiki/
- **Java 21 Docs**: https://docs.oracle.com/en/java/javase/21/
- **Gradle Docs**: https://docs.gradle.org/

### Community
- **GitHub Repository**: https://github.com/gcclinux/Woodlanders
- **Issue Tracker**: GitHub Issues
- **Discussions**: GitHub Discussions

### Support
- **Buy Me A Coffee**: https://www.buymeacoffee.com/gcclinux
- **GitHub Sponsors**: https://github.com/sponsors/gcclinux
