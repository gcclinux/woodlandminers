# Woodlanders Server Configuration Guide

## Overview

This guide covers all configuration options for the Woodlanders dedicated server. The server can be configured through the `server.properties` file and command-line arguments.

## Configuration File: server.properties

The `server.properties` file should be placed in the same directory as the server JAR file. If the file doesn't exist, the server will create one with default values on first startup.

### Configuration Properties

#### server.port

- **Type**: Integer (1-65535)
- **Default**: 25565
- **Description**: The TCP port the server listens on for client connections
- **Example**: `server.port=25565`
- **Notes**: 
  - Ensure this port is not already in use by another application
  - You must configure your firewall to allow incoming connections on this port
  - Common alternative ports: 25566, 25567, 7777, 8080

#### server.max-clients

- **Type**: Integer (1-1000)
- **Default**: 20
- **Description**: Maximum number of concurrent client connections allowed
- **Example**: `server.max-clients=20`
- **Notes**:
  - Higher values require more server resources (CPU, memory, bandwidth)
  - Recommended values:
    - Small server (2GB RAM): 10-20 clients
    - Medium server (4GB RAM): 20-50 clients
    - Large server (8GB+ RAM): 50-100 clients

#### world.seed

- **Type**: Long integer
- **Default**: 0 (random)
- **Description**: Seed value for deterministic world generation
- **Example**: `world.seed=123456789`
- **Notes**:
  - Set to 0 to generate a random seed on each server start
  - Use the same seed to recreate identical worlds
  - All clients will generate the same tree positions using this seed

#### server.heartbeat-interval

- **Type**: Integer (1-60 seconds)
- **Default**: 5
- **Description**: How often clients send heartbeat messages to maintain connection
- **Example**: `server.heartbeat-interval=5`
- **Notes**:
  - Lower values detect disconnections faster but increase network traffic
  - Higher values reduce traffic but delay disconnect detection
  - Recommended: 3-10 seconds

#### server.client-timeout

- **Type**: Integer (5-300 seconds)
- **Default**: 15
- **Description**: Time to wait before disconnecting unresponsive clients
- **Example**: `server.client-timeout=15`
- **Notes**:
  - Should be at least 3x the heartbeat interval
  - Lower values disconnect laggy clients faster
  - Higher values are more tolerant of network issues
  - Recommended: 15-30 seconds

#### server.rate-limit

- **Type**: Integer (10-10000 messages/second)
- **Default**: 100
- **Description**: Maximum messages per second allowed from each client
- **Example**: `server.rate-limit=100`
- **Notes**:
  - Protects against denial-of-service attacks
  - Normal gameplay generates 20-40 messages/second
  - Clients exceeding this limit are automatically disconnected
  - Don't set below 50 or legitimate clients may be kicked

#### server.debug

- **Type**: Boolean (true/false)
- **Default**: false
- **Description**: Enable verbose debug logging
- **Example**: `server.debug=true`
- **Notes**:
  - Logs all network messages and state changes
  - Useful for troubleshooting but generates large log files
  - May impact performance with many clients
  - Only enable when debugging issues

## Command-Line Arguments

The server can be configured via command-line arguments, which override values in `server.properties`.

### Usage

```bash
java -jar woodlanders-server.jar [OPTIONS]
```

### Available Arguments

#### --port <number>

- **Description**: Set the server port
- **Example**: `java -jar woodlanders-server.jar --port 25566`
- **Overrides**: `server.port` property

#### --max-clients <number>

- **Description**: Set maximum concurrent clients
- **Example**: `java -jar woodlanders-server.jar --max-clients 50`
- **Overrides**: `server.max-clients` property

#### --seed <number>

- **Description**: Set world generation seed
- **Example**: `java -jar woodlanders-server.jar --seed 123456789`
- **Overrides**: `world.seed` property

#### --debug

- **Description**: Enable debug logging
- **Example**: `java -jar woodlanders-server.jar --debug`
- **Overrides**: `server.debug` property

#### --help

- **Description**: Display help information and exit
- **Example**: `java -jar woodlanders-server.jar --help`

### Combined Example

```bash
java -jar woodlanders-server.jar --port 7777 --max-clients 30 --seed 987654321 --debug
```

## JVM Configuration

You can optimize server performance by configuring JVM parameters:

### Memory Allocation

```bash
# Allocate 2GB of RAM
java -Xms2G -Xmx2G -jar woodlanders-server.jar

# Allocate 4GB of RAM
java -Xms4G -Xmx4G -jar woodlanders-server.jar
```

**Recommendations**:
- Small server (5-10 clients): 1-2GB
- Medium server (10-30 clients): 2-4GB
- Large server (30+ clients): 4-8GB

### Garbage Collection

```bash
# Use G1 garbage collector (recommended for servers)
java -XX:+UseG1GC -Xms2G -Xmx2G -jar woodlanders-server.jar

# Aggressive GC for low-latency
java -XX:+UseG1GC -XX:MaxGCPauseMillis=50 -Xms2G -Xmx2G -jar woodlanders-server.jar
```

### Complete Production Example

```bash
java -Xms4G -Xmx4G \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=50 \
     -XX:+ParallelRefProcEnabled \
     -jar woodlanders-server.jar \     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=50 \
     -XX:+ParallelRefProcEnabled \
     --port 25565 \
     --max-clients 50
```

## Configuration Best Practices

### Security

1. **Never expose server to internet without firewall**: Always use a firewall to control access
2. **Use non-standard ports**: Consider using ports other than 25565 to reduce automated attacks
3. **Set reasonable rate limits**: Keep `server.rate-limit` at 100 or lower
4. **Monitor logs**: Regularly check logs for suspicious activity
5. **Limit max clients**: Don't set `server.max-clients` higher than your server can handle

### Performance

1. **Match heartbeat to network quality**: 
   - LAN: 3-5 seconds
   - Good internet: 5-10 seconds
   - Poor internet: 10-15 seconds

2. **Adjust timeout based on player base**:
   - Competitive players: 10-15 seconds (kick laggy players)
   - Casual players: 20-30 seconds (more tolerant)

3. **Allocate sufficient memory**: Use `-Xms` and `-Xmx` with same value to prevent heap resizing

4. **Use G1GC**: The G1 garbage collector provides better latency for server applications

### Reliability

1. **Use process managers**: Run server with systemd, screen, or tmux for automatic restart
2. **Enable logging**: Keep `server.debug=false` in production but log to file
3. **Monitor resources**: Watch CPU, memory, and network usage
4. **Regular backups**: Back up `server.properties` and world data

## Environment Variables

You can use environment variables for sensitive configuration:

```bash
# Set port via environment variable
export SERVER_PORT=25565
java -jar woodlanders-server.jar --port $SERVER_PORT

# Use in scripts
#!/bin/bash
SERVER_PORT=${SERVER_PORT:-25565}
MAX_CLIENTS=${MAX_CLIENTS:-20}
java -jar woodlanders-server.jar --port $SERVER_PORT --max-clients $MAX_CLIENTS
```

## Configuration Validation

The server validates all configuration values on startup:

- **Invalid port**: Server will fail to start and log error
- **Port in use**: Server will fail to start and suggest alternative port
- **Invalid ranges**: Values outside allowed ranges will be clamped to min/max
- **Invalid types**: Non-numeric values will use defaults and log warning

## Logging Configuration

Server logs are written to:
- **Console**: Standard output (stdout)
- **Error logs**: Standard error (stderr)

### Log Levels

- **INFO**: Normal operation (connections, disconnections)
- **WARNING**: Recoverable issues (rate limit exceeded, invalid messages)
- **ERROR**: Critical issues (connection failures, crashes)
- **DEBUG**: Detailed information (only when `server.debug=true`)

### Redirecting Logs to File

```bash
# Redirect all output to file
java -jar woodlanders-server.jar > server.log 2>&1

# Separate stdout and stderr
java -jar woodlanders-server.jar > server.log 2> error.log

# Append to existing log
java -jar woodlanders-server.jar >> server.log 2>&1
```

## Hot Reloading

Currently, configuration changes require server restart. To apply new settings:

1. Stop the server gracefully (Ctrl+C)
2. Edit `server.properties`
3. Restart the server

**Note**: Future versions may support hot-reloading certain properties without restart.

## Configuration Templates

### Minimal Configuration (LAN Party)

```properties
server.port=25565
server.max-clients=10
world.seed=0
server.heartbeat-interval=3
server.client-timeout=10
server.rate-limit=100
server.debug=false
```

### Standard Configuration (Public Server)

```properties
server.port=25565
server.max-clients=20
world.seed=0
server.heartbeat-interval=5
server.client-timeout=15
server.rate-limit=100
server.debug=false
```

### High-Performance Configuration (Dedicated Server)

```properties
server.port=25565
server.max-clients=50
world.seed=0
server.heartbeat-interval=5
server.client-timeout=20
server.rate-limit=150
server.debug=false
```

### Debug Configuration (Development)

```properties
server.port=25565
server.max-clients=5
world.seed=123456789
server.heartbeat-interval=10
server.client-timeout=30
server.rate-limit=1000
server.debug=true
```

## See Also

- [Server Setup Guide](SERVER_SETUP.md) - Installation and deployment instructions
- [Firewall Configuration](FIREWALL_CONFIGURATION.md) - Network security setup
- [Troubleshooting Guide](TROUBLESHOOTING.md) - Common issues and solutions
