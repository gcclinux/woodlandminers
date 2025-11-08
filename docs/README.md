# Woodlanders Server Documentation

Welcome to the Woodlanders dedicated server documentation. This guide will help you set up, configure, and maintain a multiplayer server for the Woodlanders game.

## Game Documentation

### 🎮 [Game Features](FEATURES.md)
Complete list of all game features, mechanics, and technical details.

**Topics covered**:
- Player character and animations
- World system and generation
- Environmental hazards
- Tree system and types
- Combat and interaction mechanics
- Controls
- Multiplayer features
- Technical implementation details

**Start here if**: You want to understand the game's features and mechanics.

---

## Server Documentation Overview

### 🚀 [Server Setup Guide](SERVER_SETUP.md)
Complete installation and deployment instructions for running a Woodlanders server.

**Topics covered**:
- System requirements
- Installation steps
- Deployment options (LAN, Internet, Cloud)
- Running as a service
- Server management
- Performance tuning
- Backup and restore

**Start here if**: You're setting up a server for the first time.

### ⚙️ [Server Configuration Guide](SERVER_CONFIGURATION.md)
Detailed reference for all server configuration options.

**Topics covered**:
- Configuration file format (server.properties)
- Command-line arguments
- JVM configuration
- Configuration best practices
- Environment variables
- Logging configuration
- Configuration templates

**Start here if**: You need to customize server settings or optimize performance.

### 🔒 [Firewall Configuration Guide](FIREWALL_CONFIGURATION.md)
Network security and firewall setup for all platforms.

**Topics covered**:
- Port requirements
- Operating system firewalls (Windows, Linux, macOS)
- Router/gateway configuration
- Cloud provider firewalls (AWS, GCP, Azure, DigitalOcean)
- Security best practices
- Testing firewall configuration

**Start here if**: You need to configure network access or troubleshoot connection issues.

### 🔧 [Troubleshooting Guide](TROUBLESHOOTING.md)
Solutions for common issues and debugging techniques.

**Topics covered**:
- Quick diagnostics
- Common issues and solutions
- Debugging tools
- Log analysis
- Getting help
- Preventive maintenance

**Start here if**: You're experiencing problems with your server.

### 🏠 [Localhost Testing Guide](LOCALHOST.md)
Instructions for running multiple game instances on the same computer for testing.

**Topics covered**:
- Running 2 instances for multiplayer testing
- Using Gradle for local testing
- Using built JAR files for testing
- Host and client setup

**Start here if**: You want to test multiplayer functionality locally before deploying.

### 📝 [Last Server Persistence](LASTSERVER_PERSISTENCE.md)
Documentation on the last server connection feature.

**Topics covered**:
- Configuration file location
- JSON structure
- Auto-fill functionality
- Cross-platform support

**Start here if**: You want to understand how the game remembers server connections.

### 🔄 [Multiplayer Configuration Update](MULTIPLAYER_CONFIG_UPDATE.md)
Technical documentation on the multiplayer configuration system changes.

**Topics covered**:
- Storage format changes (Properties to JSON)
- Migration support
- Platform-specific paths
- Implementation details

**Start here if**: You're a developer working on the configuration system.

### ✅ [Test Execution Summary](TEST_EXECUTION_SUMMARY.md)
Summary of automated testing for multiplayer threading fixes.

**Topics covered**:
- Test approach and strategy
- Test results
- Threading fix verification
- Quality assurance

**Start here if**: You want to understand the testing methodology and results.

### 🧪 [Multiplayer Test Results](MULTIPLAYER_TEST_RESULTS.md)
Detailed test results for multiplayer functionality.

**Topics covered**:
- Threading fix integration tests
- Item pickup testing
- Deferred operation verification
- Test environment details

**Start here if**: You need detailed test case results and verification data.

## Quick Start

### 1. Install Java

Ensure you have Java 21 or higher installed:

```bash
java -version
```

If not installed, download from [Adoptium](https://adoptium.net/).

### 2. Download Server

Download `woodlanders-server.jar` from the releases page.

### 3. Start Server

```bash
java -jar woodlanders-server.jar
```

The server will:
- Create a default `server.properties` file
- Start listening on port 25565
- Display your public IP address

### 4. Configure Firewall

Allow TCP port 25565 through your firewall:

```bash
# Linux (UFW)
sudo ufw allow 25565/tcp

# Windows (PowerShell - Administrator)
New-NetFirewallRule -DisplayName "Woodlanders Server" -Direction Inbound -Protocol TCP -LocalPort 25565 -Action Allow
```

### 5. Connect

Players can now connect using your IP address and port 25565.

## Common Tasks

### Change Server Port

Edit `server.properties`:
```properties
server.port=7777
```

Or use command-line argument:
```bash
java -jar woodlanders-server.jar --port 7777
```

### Increase Max Players

Edit `server.properties`:
```properties
server.max-clients=50
```

### Allocate More Memory

```bash
java -Xms4G -Xmx4G -jar woodlanders-server.jar
```

### Run in Background (Linux)

```bash
screen -S woodlanders
java -jar woodlanders-server.jar
# Press Ctrl+A, then D to detach
```

### View Logs

```bash
tail -f server.log
```

### Stop Server

Press `Ctrl+C` in the terminal running the server.

## Server Requirements by Player Count

| Players | CPU Cores | RAM | Upload Speed |
|---------|-----------|-----|--------------|
| 5-10    | 2         | 1GB | 2 Mbps       |
| 10-20   | 2-4       | 2GB | 5 Mbps       |
| 20-50   | 4         | 4GB | 10 Mbps      |
| 50+     | 8+        | 8GB | 20 Mbps      |

## Configuration Quick Reference

### server.properties

```properties
# Server port (default: 25565)
server.port=25565

# Maximum concurrent clients (default: 20)
server.max-clients=20

# World seed - 0 for random (default: 0)
world.seed=0

# Heartbeat interval in seconds (default: 5)
server.heartbeat-interval=5

# Client timeout in seconds (default: 15)
server.client-timeout=15

# Message rate limit per client (default: 100)
server.rate-limit=100

# Enable debug logging (default: false)
server.debug=false
```

### Command-Line Arguments

```bash
# Basic usage
java -jar woodlanders-server.jar

# Custom port
java -jar woodlanders-server.jar --port 7777

# Multiple options
java -jar woodlanders-server.jar --port 7777 --max-clients 50 --debug

# With JVM options
java -Xms4G -Xmx4G -XX:+UseG1GC -jar woodlanders-server.jar --port 25565
```

## Deployment Scenarios

### Home Server (LAN Party)

**Best for**: Playing with friends on the same network

- No port forwarding needed
- Use local IP address (192.168.x.x)
- Minimal configuration required

**Setup**: [Server Setup Guide - Local Network](SERVER_SETUP.md#option-1-local-network-lan)

### Public Internet Server

**Best for**: Playing with friends over the internet

- Requires port forwarding
- Need public IP address
- Firewall configuration required

**Setup**: [Server Setup Guide - Internet Server](SERVER_SETUP.md#option-2-internet-server-public)

### Cloud Hosted Server

**Best for**: Reliable 24/7 server with good performance

- Professional hosting
- No home network configuration
- Scalable resources

**Setup**: [Server Setup Guide - Cloud Hosting](SERVER_SETUP.md#option-3-cloud-hosting)

### Dedicated Server Machine

**Best for**: Large player counts, always-on server

- Runs without game client
- Optimized for server workload
- Can run on minimal hardware

**Setup**: [Server Setup Guide - Running as a Service](SERVER_SETUP.md#running-as-a-service-linux)

## Security Checklist

- [ ] Firewall configured to only allow necessary ports
- [ ] Server running as non-root user (Linux)
- [ ] Rate limiting enabled (default: 100 msg/sec)
- [ ] Max clients set to reasonable value
- [ ] Debug logging disabled in production
- [ ] Regular log monitoring in place
- [ ] Backup system configured
- [ ] Java kept up to date

## Performance Optimization

### JVM Tuning

```bash
# Recommended for 4GB server
java -Xms4G -Xmx4G \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=50 \
     -XX:+ParallelRefProcEnabled \
     -jar woodlanders-server.jar
```

### Configuration Tuning

```properties
# Adjust based on network quality
server.heartbeat-interval=5
server.client-timeout=15

# Adjust based on server capacity
server.max-clients=20
server.rate-limit=100
```

### System Tuning (Linux)

```bash
# Increase network buffer sizes
sudo sysctl -w net.core.rmem_max=16777216
sudo sysctl -w net.core.wmem_max=16777216
```

## Monitoring

### Check Server Status

```bash
# Is server running?
ps aux | grep woodlanders-server

# Is port listening?
netstat -tuln | grep 25565

# Resource usage
top -p $(pgrep -f woodlanders-server)
```

### Monitor Logs

```bash
# Real-time log monitoring
tail -f server.log

# Filter for errors
grep "ERROR" server.log

# Filter for warnings
grep "WARNING" server.log

# Count recent connections
grep "Client connected" server.log | tail -n 100 | wc -l
```

## Getting Help

### Before Asking for Help

1. Check the [Troubleshooting Guide](TROUBLESHOOTING.md)
2. Review server logs for error messages
3. Verify your configuration is correct
4. Test with minimal configuration

### When Asking for Help

Include:
- Server version
- Java version (`java -version`)
- Operating system
- Configuration file (`server.properties`)
- Relevant log excerpts
- Steps to reproduce the issue

### Support Channels

- **Repository**: https://wagemaker.uk:3000/ricardo/Woodlanders
- **Issues**: https://wagemaker.uk:3000/ricardo/Woodlanders/issues
- **Documentation**: https://wagemaker.uk:3000/ricardo/Woodlanders/wiki

## Additional Resources

### External Documentation

- [Java SE Documentation](https://docs.oracle.com/en/java/)
- [Adoptium JDK Downloads](https://adoptium.net/)
- [UFW Firewall Guide](https://help.ubuntu.com/community/UFW)
- [systemd Service Guide](https://www.freedesktop.org/software/systemd/man/systemd.service.html)

### Tools

- **jconsole**: Monitor JVM performance
- **VisualVM**: Profile Java applications
- **htop**: Monitor system resources (Linux)
- **screen/tmux**: Terminal multiplexers
- **fail2ban**: Automatic IP banning

## Contributing

Found an issue with the documentation? Want to add more examples?

1. Fork the repository
2. Make your changes
3. Submit a pull request

## License

This documentation is part of the Woodlanders project and is licensed under the same terms as the main project.

---

**Last Updated**: 2025-11-08  
**Server Version**: 1.0  
**Documentation Version**: 1.0
