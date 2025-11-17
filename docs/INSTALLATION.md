## Requirements
- Java 21+ (OpenJDK 21.0.8 or higher)
- Gradle 9.2.0+
- libGDX 1.12.1

## Technical Details

### Project Information
- **Version**: 0.0.8
- **Game Engine**: libGDX 1.12.1
- **Language**: Java 21
- **Build Tool**: Gradle 9.2.0
- **Architecture**: Client-Server with dedicated server support

### Key Technologies
- **Graphics**: libGDX with LWJGL3 backend
- **Fonts**: FreeType font rendering (slkscr.ttf)
- **Networking**: Java Socket-based TCP networking
- **Serialization**: Java ObjectOutputStream for world saves
- **Concurrency**: Multi-threaded server with client connection pools

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

### Run from Source
```bash
cd <project folder>/Woodlanders
gradle run
```

### Build and Run Client JAR
```bash
cd <project folder>/Woodlanders
gradle clean build -x test
java -jar build/libs/woodlanders-client.jar
```

### Build and Run Dedicated Server
```bash
cd <project folder>/Woodlanders
gradle clean build -x test
java -jar build/libs/woodlanders-server.jar
```

### Server with Custom Configuration
```bash
# Basic server start
java -jar build/libs/woodlanders-server.jar

# With custom port
java -jar build/libs/woodlanders-server.jar --port 30000

# With custom config file
java -jar build/libs/woodlanders-server.jar --config custom.properties

# With memory allocation
java -Xms2G -Xmx2G -jar build/libs/woodlanders-server.jar

# All options combined
java -Xms4G -Xmx4G -jar build/libs/woodlanders-server.jar --port 25565 --config server.properties
```