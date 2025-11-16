# Server Launcher Scripts

This directory contains launcher scripts that make it easy to start the Woodlanders server with custom JVM settings.

## Quick Start

### Linux/Mac

```bash
# Basic usage with default settings
./start-server.sh

# With server arguments
./start-server.sh --port 25565 --max-clients 50

# With custom JVM settings
JVM_MEMORY_MIN=8G JVM_MEMORY_MAX=8G ./start-server.sh --max-clients 100
```

### Windows

```batch
REM Basic usage
start-server.bat

REM With server arguments
start-server.bat --port 25565 --max-clients 50

REM With custom JVM settings
set JVM_MEMORY_MIN=8G
set JVM_MEMORY_MAX=8G
start-server.bat --max-clients 100
```

## Available Scripts

### 1. `start-server.sh` / `start-server.bat` (Simple)

Basic launcher that uses environment variables for JVM configuration.

**Environment Variables:**
- `JVM_MEMORY_MIN` - Minimum heap size (default: 4G)
- `JVM_MEMORY_MAX` - Maximum heap size (default: 4G)
- `JVM_GC` - Garbage collector (default: G1GC)
- `JVM_GC_PAUSE` - Max GC pause in milliseconds (default: 50)
- `JVM_PARALLEL_REF` - Enable parallel reference processing (default: true)

**Example:**
```bash
export JVM_MEMORY_MIN=8G
export JVM_MEMORY_MAX=8G
export JVM_GC_PAUSE=30
./start-server.sh --port 25565 --max-clients 100
```

### 2. `start-server-advanced.sh` (Config File)

Advanced launcher that reads JVM settings from `jvm.properties` file.

**Configuration File:** `jvm.properties`

Edit this file to set your JVM preferences:
```properties
jvm.memory.min=4G
jvm.memory.max=4G
jvm.gc=G1GC
jvm.gc.max-pause-millis=50
jvm.parallel-ref-processing=true
jvm.additional-args=
```

**Example:**
```bash
# Edit jvm.properties first, then run:
./start-server-advanced.sh --port 25565 --max-clients 50
```

## JVM Settings Explained

### Memory Settings

- **Xms/Xmx (Min/Max Heap)**: Set both to the same value to avoid heap resizing overhead
  - Small servers (1-10 players): 2G-4G
  - Medium servers (10-30 players): 4G-8G
  - Large servers (30+ players): 8G-16G+

### Garbage Collection

- **G1GC (Recommended)**: Best for servers with large heaps (>4GB)
  - Low latency, predictable pause times
  - Good for game servers

- **ParallelGC**: Good for throughput, but longer pause times
  - Better for batch processing
  - Not recommended for real-time games

### GC Pause Time

- **MaxGCPauseMillis**: Target maximum pause time
  - Lower = more frequent but shorter pauses
  - Recommended: 50-200ms for game servers
  - Too low may increase GC overhead

### Parallel Reference Processing

- **ParallelRefProcEnabled**: Speeds up GC by processing references in parallel
  - Recommended: Always enable for servers
  - Reduces GC pause times

## Server Arguments

All server arguments are passed through to the Java application:

- `--port <number>` - Server port (default: 25565)
- `--max-clients <number>` - Maximum concurrent clients (default: 20)
- `--seed <number>` - World generation seed (default: 0 for random)
- `--debug` - Enable debug logging
- `--config <file>` - Custom server.properties file
- `--help` - Show help message

## Examples

### Development Server (Low Memory)
```bash
JVM_MEMORY_MIN=2G JVM_MEMORY_MAX=2G ./start-server.sh --debug
```

### Production Server (High Performance)
```bash
JVM_MEMORY_MIN=8G JVM_MEMORY_MAX=8G JVM_GC_PAUSE=30 ./start-server.sh --port 25565 --max-clients 100
```

### Custom Configuration
```bash
# Edit jvm.properties to set:
# jvm.memory.min=16G
# jvm.memory.max=16G
# jvm.gc.max-pause-millis=30

./start-server-advanced.sh --max-clients 200 --seed 123456789
```

## Monitoring

To monitor your server's JVM performance:

```bash
# View GC logs
java -Xms4G -Xmx4G -XX:+UseG1GC -Xlog:gc*:file=gc.log -jar build/libs/woodlanders-server.jar

# Use JConsole (GUI)
jconsole

# Use VisualVM (GUI)
jvisualvm
```

## Troubleshooting

### Out of Memory Errors
Increase `JVM_MEMORY_MAX`:
```bash
JVM_MEMORY_MAX=8G ./start-server.sh
```

### Long GC Pauses
Decrease `JVM_GC_PAUSE` target:
```bash
JVM_GC_PAUSE=30 ./start-server.sh
```

### High CPU Usage
Check if GC is running too frequently. Increase heap size or adjust GC settings.
