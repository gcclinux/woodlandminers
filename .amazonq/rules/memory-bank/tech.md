# Technology Stack

## Programming Languages

### Java 21
- **Version**: Java 21 (LTS)
- **Source Compatibility**: JavaVersion.VERSION_21
- **Target Compatibility**: JavaVersion.VERSION_21
- Modern Java features and performance improvements

## Core Framework

### libGDX 1.12.1
- **Purpose**: Cross-platform game development framework
- **Components Used**:
  - `gdx-core`: Core game engine
  - `gdx-backend-lwjgl3`: Desktop backend (LWJGL 3)
  - `gdx-platform`: Native libraries for desktop
  - `gdx-freetype`: TrueType font rendering
  - `gdx-backend-headless`: Headless backend for server and testing

### Key libGDX Features
- Sprite rendering and texture management
- Input handling (keyboard, mouse)
- Asset management
- Batch rendering for performance
- Delta-time based game loop

## Build System

### Gradle
- **Build Tool**: Gradle with Groovy DSL
- **Plugins**:
  - `java`: Java compilation
  - `application`: Application packaging
- **Version**: Project version 0.0.8

### Build Configuration
```gradle
sourceCompatibility = JavaVersion.VERSION_21
targetCompatibility = JavaVersion.VERSION_21
```

## Testing Framework

### JUnit 5 (Jupiter)
- **Version**: 5.10.0
- **Purpose**: Unit and integration testing
- **Test Platform**: junit-platform-launcher

### Mockito
- **Version**: 5.5.0
- **Purpose**: Mocking framework for unit tests
- **Extensions**: mockito-junit-jupiter for JUnit 5 integration

### Test Configuration
```gradle
test {
    useJUnitPlatform()
    testLogging {
        events = ["passed", "skipped", "failed"]
        exceptionFormat = "full"
    }
}
```

## Dependencies

### Runtime Dependencies
```gradle
implementation "com.badlogicgames.gdx:gdx:1.12.1"
implementation "com.badlogicgames.gdx:gdx-backend-lwjgl3:1.12.1"
implementation "com.badlogicgames.gdx:gdx-platform:1.12.1:natives-desktop"
implementation "com.badlogicgames.gdx:gdx-freetype:1.12.1"
implementation "com.badlogicgames.gdx:gdx-freetype-platform:1.12.1:natives-desktop"
```

### Test Dependencies
```gradle
testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
testImplementation 'org.mockito:mockito-core:5.5.0'
testImplementation 'org.mockito:mockito-junit-jupiter:5.5.0'
testImplementation "com.badlogicgames.gdx:gdx-backend-headless:1.12.1"
testImplementation "com.badlogicgames.gdx:gdx-platform:1.12.1:natives-desktop"
```

## Networking

### Custom TCP Protocol
- **Transport**: Java Socket API (TCP)
- **Architecture**: Server-authoritative with client prediction
- **Message Format**: Custom serializable message classes
- **Heartbeat**: 5-second keepalive with 15-second timeout

### Network Components
- `java.net.ServerSocket`: Server listening
- `java.net.Socket`: Client connections
- `java.io.DataInputStream/DataOutputStream`: Message serialization
- Thread-based client handling

## Data Formats

### JSON
- **Library**: Built-in Java JSON handling (likely using libGDX's JSON utilities)
- **Usage**:
  - World save files (.wld)
  - Player configuration (woodlanders.json)
  - Localization files (en.json, nl.json, pl.json, pt.json)

### Binary
- **Format**: Custom binary protocol for network messages
- **Serialization**: DataInputStream/DataOutputStream

## Asset Management

### Sprites & Textures
- **Format**: PNG images
- **Main Atlas**: assets.png (sprite sheet)
- **Organization**: Texture regions for efficient rendering
- **Player Sprites**: Directional animation frames

### Fonts
- **Format**: TrueType Font (.ttf)
- **Font**: Silkscreen (slkscr.ttf, slkscrb.ttf)
- **Rendering**: libGDX FreeType for runtime font rendering
- **Style**: Retro pixel font for authentic game feel

### Localization
- **Format**: JSON
- **Languages**: English (en), Polish (pl), Portuguese (pt), Dutch (nl)
- **Structure**: Key-value pairs for UI strings

## Development Commands

### Build Commands
```bash
# Build the project (creates both client and server JARs)
./gradlew build

# Build only client JAR
./gradlew jar

# Build only server JAR
./gradlew serverJar

# Clean build artifacts
./gradlew clean
```

### Run Commands
```bash
# Run the game client
./gradlew run

# Run with specific working directory
./gradlew run --args="<arguments>"
```

### Test Commands
```bash
# Run all tests
./gradlew test

# Run tests with detailed output
./gradlew test --info

# Run specific test class
./gradlew test --tests "wagemaker.uk.localization.LocalizationManagerTest"

# Run tests continuously
./gradlew test --continuous
```

### JAR Execution
```bash
# Run client JAR
java -jar build/libs/woodlanders-client.jar

# Run server JAR
java -jar build/libs/woodlanders-server.jar
```

### Gradle Wrapper
```bash
# Update Gradle wrapper
./gradlew wrapper --gradle-version=9.2.0

# Check Gradle version
./gradlew --version
```

## Development Environment

### IDE Support
- **Primary**: Kiro IDE (AI-assisted development)
- **Compatible**: IntelliJ IDEA, Eclipse, VS Code with Java extensions

### Version Control
- **System**: Git
- **Hosting**: GitHub
- **CI/CD**: GitHub Actions for releases

### Operating Systems
- **Development**: Linux, macOS, Windows
- **Target**: Cross-platform (Java)

## Configuration Directories

### Player Data Storage
- **Windows**: `%APPDATA%/Woodlanders/`
- **macOS**: `~/Library/Application Support/Woodlanders/`
- **Linux**: `~/.config/woodlanders/`

### Files
- `woodlanders.json`: Player configuration
- `world-saves/singleplayer/`: Singleplayer world saves
- `world-saves/multiplayer/`: Multiplayer world saves

## Performance Optimizations

### Rendering
- Chunk-based rendering (only visible areas)
- Texture atlases for sprite batching
- Efficient sprite batch management
- Delta-time based animations

### Collision Detection
- Spatial partitioning
- Bounding box optimization
- Distance-based checks before detailed collision

### Networking
- Message batching
- Rate limiting per client
- Efficient serialization
- Heartbeat-based connection management

### Memory Management
- Texture disposal on entity removal
- Deferred operations for thread safety
- Efficient world generation (chunk-based)

## Build Artifacts

### Client JAR
- **Name**: woodlanders-client.jar
- **Size**: ~30-40 MB (includes all dependencies and assets)
- **Main Class**: wagemaker.uk.desktop.DesktopLauncher
- **Includes**: All rendering libraries, assets, fonts, sprites

### Server JAR
- **Name**: woodlanders-server.jar
- **Size**: ~5-10 MB (minimal dependencies)
- **Main Class**: wagemaker.uk.server.DedicatedServerLauncher
- **Excludes**: Rendering libraries, LWJGL, native libraries, assets

## External Resources

### Repositories
- **Maven Central**: Primary dependency repository
- **GitHub**: Source code hosting

### Documentation
- libGDX Wiki: https://libgdx.com/wiki/
- Java 21 Documentation: https://docs.oracle.com/en/java/javase/21/
- JUnit 5 User Guide: https://junit.org/junit5/docs/current/user-guide/
