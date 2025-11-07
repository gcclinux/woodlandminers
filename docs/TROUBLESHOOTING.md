# Woodlanders Server Troubleshooting Guide

## Overview

This guide helps diagnose and resolve common issues with the Woodlanders dedicated server.

## Quick Diagnostics

Run these checks first when experiencing issues:

```bash
# 1. Check if server is running
ps aux | grep woodlanders-server

# 2. Check if port is listening
netstat -tuln | grep 25565

# 3. Check recent logs
tail -n 50 server.log

# 4. Check server connectivity
telnet localhost 25565
```

## Common Issues

### Server Won't Start

#### Issue: "Address already in use"

**Symptoms**:
```
[ERROR] Failed to start server: Address already in use
java.net.BindException: Address already in use
```

**Cause**: Another process is using port 25565

**Solutions**:

1. Find and stop the conflicting process:
   ```bash
   # Linux/macOS
   sudo lsof -i :25565
   sudo kill -9 <PID>
   
   # Windows (PowerShell)
   netstat -ano | findstr :25565
   taskkill /PID <PID> /F
   ```

2. Use a different port:
   ```bash
   java -jar woodlanders-server.jar --port 25566
   ```

3. Wait a few minutes (port may be in TIME_WAIT state)

#### Issue: "Permission denied"

**Symptoms**:
```
[ERROR] Failed to bind to port 25565: Permission denied
```

**Cause**: Ports below 1024 require root privileges on Linux/macOS

**Solutions**:

1. Use a port above 1024:
   ```properties
   server.port=25565
   ```

2. Or grant Java permission (not recommended):
   ```bash
   sudo setcap 'cap_net_bind_service=+ep' /usr/bin/java
   ```

#### Issue: "Java not found"

**Symptoms**:
```
'java' is not recognized as an internal or external command
```

**Cause**: Java not installed or not in PATH

**Solutions**:

1. Install Java 21 or higher:
   ```bash
   # Ubuntu/Debian
   sudo apt install openjdk-21-jre-headless
   
   # macOS (using Homebrew)
   brew install openjdk@21
   
   # Windows: Download from https://adoptium.net/
   ```

2. Verify installation:
   ```bash
   java -version
   ```

#### Issue: "OutOfMemoryError"

**Symptoms**:
```
java.lang.OutOfMemoryError: Java heap space
```

**Cause**: Insufficient memory allocated to JVM

**Solutions**:

1. Increase heap size:
   ```bash
   # Allocate 4GB
   java -Xms4G -Xmx4G -jar woodlanders-server.jar
   ```

2. Check available system memory:
   ```bash
   # Linux
   free -h
   
   # macOS
   vm_stat
   
   # Windows
   systeminfo | findstr Memory
   ```

3. Reduce max clients if memory is limited:
   ```properties
   server.max-clients=10
   ```

### Connection Issues

#### Issue: "Connection refused"

**Symptoms**:
- Client shows "Connection refused" error
- Can't connect to server

**Diagnosis**:
```bash
# Check if server is running
ps aux | grep woodlanders-server

# Check if port is listening
netstat -tuln | grep 25565

# Test local connection
telnet localhost 25565
```

**Solutions**:

1. Ensure server is running:
   ```bash
   java -jar woodlanders-server.jar
   ```

2. Verify correct port in client and server

3. Check firewall isn't blocking port:
   ```bash
   # Linux
   sudo ufw status
   sudo ufw allow 25565/tcp
   
   # Windows
   netsh advfirewall firewall add rule name="Woodlanders" dir=in action=allow protocol=TCP localport=25565
   ```

#### Issue: "Connection timeout"

**Symptoms**:
- Client hangs on "Connecting..."
- Eventually times out

**Diagnosis**:
```bash
# Test connectivity
ping <server-ip>

# Test port
telnet <server-ip> 25565

# Check for packet loss
ping -c 100 <server-ip>
```

**Solutions**:

1. Check router port forwarding (if connecting over internet)

2. Verify public IP address:
   ```bash
   curl ifconfig.me
   ```

3. Test from external network (not same LAN)

4. Check ISP doesn't block port

5. Increase client timeout (if network is slow)

#### Issue: "Server full"

**Symptoms**:
```
[ERROR] Connection rejected: Server is full
```

**Cause**: Maximum clients reached

**Solutions**:

1. Increase max clients:
   ```properties
   server.max-clients=50
   ```

2. Kick idle players (future feature)

3. Upgrade server resources

#### Issue: Frequent disconnections

**Symptoms**:
- Players randomly disconnect
- "Connection lost" messages

**Diagnosis**:
```bash
# Check server logs for patterns
grep "disconnect" server.log

# Monitor network stability
ping -c 1000 <server-ip>

# Check server resource usage
top
```

**Solutions**:

1. Increase timeout values:
   ```properties
   server.heartbeat-interval=10
   server.client-timeout=30
   ```

2. Check network stability:
   ```bash
   # Test for packet loss
   ping -c 100 <server-ip>
   ```

3. Verify server isn't overloaded:
   ```bash
   # Check CPU and memory
   top
   htop
   ```

4. Check for rate limiting issues:
   ```properties
   server.rate-limit=150
   ```

### Performance Issues

#### Issue: High latency / lag

**Symptoms**:
- Delayed player movement
- Actions take time to register
- High ping times

**Diagnosis**:
```bash
# Check server CPU usage
top -p $(pgrep -f woodlanders-server)

# Check memory usage
free -h

# Check network latency
ping <server-ip>

# Monitor server logs
tail -f server.log | grep "WARNING"
```

**Solutions**:

1. Optimize JVM settings:
   ```bash
   java -Xms4G -Xmx4G \
        -XX:+UseG1GC \
        -XX:MaxGCPauseMillis=50 \
        -jar woodlanders-server.jar
   ```

2. Reduce max clients:
   ```properties
   server.max-clients=20
   ```

3. Upgrade server hardware (more CPU/RAM)

4. Use server closer to players geographically

5. Check for network congestion:
   ```bash
   iftop  # Monitor bandwidth usage
   ```

#### Issue: Server crashes

**Symptoms**:
- Server stops unexpectedly
- No error message
- Process terminates

**Diagnosis**:
```bash
# Check system logs
sudo journalctl -xe

# Check for OOM killer
dmesg | grep -i "killed process"

# Check Java crash logs
ls -la hs_err_pid*.log
```

**Solutions**:

1. Increase memory allocation:
   ```bash
   java -Xms4G -Xmx4G -jar woodlanders-server.jar
   ```

2. Enable crash logging:
   ```bash
   java -XX:+HeapDumpOnOutOfMemoryError \
        -XX:HeapDumpPath=/var/log/woodlanders/ \
        -jar woodlanders-server.jar
   ```

3. Run as service with auto-restart:
   ```ini
   # systemd service
   Restart=always
   RestartSec=10
   ```

4. Monitor for memory leaks:
   ```bash
   # Use jconsole or VisualVM
   jconsole
   ```

#### Issue: High CPU usage

**Symptoms**:
- Server process using 100% CPU
- System becomes unresponsive
- Lag for all players

**Diagnosis**:
```bash
# Check CPU usage
top -p $(pgrep -f woodlanders-server)

# Profile with jstack
jstack <PID> > thread_dump.txt

# Check for infinite loops in logs
grep "WARNING" server.log
```

**Solutions**:

1. Reduce max clients:
   ```properties
   server.max-clients=15
   ```

2. Optimize tick rate (if configurable)

3. Upgrade CPU or use multi-core optimization

4. Check for misbehaving clients:
   ```bash
   grep "rate limit" server.log
   ```

### Configuration Issues

#### Issue: Configuration not loading

**Symptoms**:
- Server uses default values
- Changes to server.properties ignored

**Diagnosis**:
```bash
# Check file exists
ls -la server.properties

# Check file permissions
ls -l server.properties

# Check file location
pwd
```

**Solutions**:

1. Ensure server.properties is in same directory as JAR:
   ```bash
   ls -la
   # Should show both files
   ```

2. Check file permissions:
   ```bash
   chmod 644 server.properties
   ```

3. Verify file format (no BOM, UTF-8):
   ```bash
   file server.properties
   ```

4. Check for syntax errors:
   ```properties
   # Correct
   server.port=25565
   
   # Wrong
   server.port = 25565  # No spaces around =
   ```

5. Use command-line args to override:
   ```bash
   java -jar woodlanders-server.jar --port 25565
   ```

#### Issue: Invalid configuration values

**Symptoms**:
```
[WARNING] Invalid value for server.port: abc, using default
```

**Cause**: Non-numeric value for numeric property

**Solutions**:

1. Fix invalid values:
   ```properties
   # Wrong
   server.port=abc
   
   # Correct
   server.port=25565
   ```

2. Check value ranges:
   ```properties
   server.port=25565          # 1-65535
   server.max-clients=20      # 1-1000
   server.heartbeat-interval=5  # 1-60
   server.client-timeout=15   # 5-300
   server.rate-limit=100      # 10-10000
   ```

### World Generation Issues

#### Issue: Different worlds on each client

**Symptoms**:
- Trees in different positions
- Terrain doesn't match

**Cause**: World seed not synchronized

**Diagnosis**:
```bash
# Check server logs for seed
grep "World seed" server.log

# Check if clients receive seed
grep "WorldStateMessage" server.log
```

**Solutions**:

1. Ensure server sends world state on connect

2. Set fixed seed for testing:
   ```properties
   world.seed=123456789
   ```

3. Verify clients apply received seed

4. Check for connection issues during initial sync

### Security Issues

#### Issue: Rate limit exceeded

**Symptoms**:
```
[WARNING] Rate limit exceeded for client 192.168.1.100
[INFO] Disconnecting client due to rate limit violation
```

**Cause**: Client sending too many messages

**Solutions**:

1. Check if legitimate client:
   - May be network issue causing retransmits
   - May be buggy client

2. Increase rate limit if needed:
   ```properties
   server.rate-limit=150
   ```

3. Investigate client behavior:
   ```bash
   grep "192.168.1.100" server.log
   ```

4. Block malicious IPs:
   ```bash
   sudo ufw deny from 192.168.1.100
   ```

#### Issue: Suspicious connection attempts

**Symptoms**:
```
[WARNING] Invalid message from 203.0.113.50
[WARNING] Malformed packet from 203.0.113.50
```

**Cause**: Port scanning or attack attempts

**Solutions**:

1. Enable fail2ban (see [Firewall Configuration](FIREWALL_CONFIGURATION.md))

2. Use non-standard port:
   ```properties
   server.port=27015
   ```

3. Restrict access by IP:
   ```bash
   sudo ufw allow from 192.168.1.0/24 to any port 25565
   ```

4. Monitor logs regularly:
   ```bash
   grep "WARNING" server.log | tail -n 100
   ```

## Debugging Tools

### Enable Debug Logging

```properties
server.debug=true
```

Restart server to see detailed logs:
```bash
tail -f server.log
```

### Network Debugging

```bash
# Monitor network traffic
sudo tcpdump -i any port 25565

# Check connections
netstat -an | grep 25565

# Monitor bandwidth
iftop -i eth0
```

### Java Debugging

```bash
# Enable JMX monitoring
java -Dcom.sun.management.jmxremote \
     -Dcom.sun.management.jmxremote.port=9010 \
     -Dcom.sun.management.jmxremote.authenticate=false \
     -Dcom.sun.management.jmxremote.ssl=false \
     -jar woodlanders-server.jar

# Connect with jconsole
jconsole localhost:9010
```

### Thread Dumps

```bash
# Get thread dump
jstack <PID> > thread_dump.txt

# Or send SIGQUIT (Linux)
kill -3 <PID>
```

### Heap Dumps

```bash
# Manual heap dump
jmap -dump:format=b,file=heap.bin <PID>

# Automatic on OOM
java -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=/tmp/ \
     -jar woodlanders-server.jar
```

## Log Analysis

### Common Log Messages

#### INFO Messages (Normal)

```
[INFO] Server started on port 25565
[INFO] Client connected: player123 (192.168.1.100)
[INFO] Client disconnected: player123
[INFO] World seed: 1234567890
```

#### WARNING Messages (Investigate)

```
[WARNING] Rate limit exceeded for client 192.168.1.100
[WARNING] Invalid position update from player123
[WARNING] Client timeout: player456
```

#### ERROR Messages (Action Required)

```
[ERROR] Failed to start server: Address already in use
[ERROR] Out of memory
[ERROR] Failed to accept client connection
```

### Useful Log Queries

```bash
# Count connections per IP
grep "Client connected" server.log | awk '{print $6}' | sort | uniq -c

# Find disconnection reasons
grep "disconnect" server.log

# Find errors
grep "ERROR" server.log

# Find rate limit violations
grep "rate limit" server.log

# Monitor in real-time
tail -f server.log | grep --color=auto "ERROR\|WARNING"
```

## Getting Help

### Information to Provide

When asking for help, include:

1. **Server version**: Check server startup logs
2. **Java version**: `java -version`
3. **Operating system**: `uname -a` or `systeminfo`
4. **Configuration**: Contents of `server.properties`
5. **Error logs**: Last 50-100 lines of logs
6. **Steps to reproduce**: What you did before the issue
7. **Expected vs actual behavior**: What should happen vs what happens

### Log Collection Script

```bash
#!/bin/bash
# collect_logs.sh

OUTPUT="woodlanders_debug_$(date +%Y%m%d_%H%M%S).tar.gz"

mkdir -p debug_info
cd debug_info

# System info
uname -a > system_info.txt
java -version 2> java_version.txt
free -h > memory_info.txt
df -h > disk_info.txt

# Server info
cp ../server.properties .
tail -n 500 ../server.log > server_log.txt
ps aux | grep woodlanders > process_info.txt
netstat -tuln | grep 25565 > network_info.txt

# Firewall info
sudo ufw status verbose > firewall_info.txt 2>&1

cd ..
tar -czf $OUTPUT debug_info/
rm -rf debug_info

echo "Debug information collected: $OUTPUT"
```

### Support Channels

- **GitHub Issues**: https://github.com/yourusername/woodlanders/issues
- **Community Discord**: [Your Discord Link]
- **Documentation**: https://github.com/yourusername/woodlanders/wiki
- **Email Support**: support@example.com

## Preventive Maintenance

### Regular Tasks

1. **Monitor logs daily**:
   ```bash
   grep "ERROR\|WARNING" server.log | tail -n 50
   ```

2. **Check resource usage**:
   ```bash
   top -p $(pgrep -f woodlanders-server)
   ```

3. **Backup configuration weekly**:
   ```bash
   cp server.properties server.properties.backup.$(date +%Y%m%d)
   ```

4. **Update Java regularly**:
   ```bash
   sudo apt update && sudo apt upgrade openjdk-21-jre-headless
   ```

5. **Review firewall rules monthly**:
   ```bash
   sudo ufw status verbose
   ```

6. **Test backups monthly**:
   ```bash
   # Restore to test environment and verify
   ```

### Health Check Script

```bash
#!/bin/bash
# health_check.sh

echo "=== Woodlanders Server Health Check ==="
echo

# Check if running
if pgrep -f woodlanders-server > /dev/null; then
    echo "✓ Server is running"
else
    echo "✗ Server is NOT running"
    exit 1
fi

# Check port
if netstat -tuln | grep -q ":25565"; then
    echo "✓ Port 25565 is listening"
else
    echo "✗ Port 25565 is NOT listening"
fi

# Check memory
MEM_USAGE=$(ps aux | grep woodlanders-server | grep -v grep | awk '{print $4}')
echo "Memory usage: ${MEM_USAGE}%"

# Check CPU
CPU_USAGE=$(ps aux | grep woodlanders-server | grep -v grep | awk '{print $3}')
echo "CPU usage: ${CPU_USAGE}%"

# Check recent errors
ERROR_COUNT=$(grep "ERROR" server.log | tail -n 100 | wc -l)
echo "Recent errors (last 100 lines): $ERROR_COUNT"

# Check connected clients
CLIENT_COUNT=$(grep "Client connected" server.log | tail -n 100 | wc -l)
echo "Recent connections: $CLIENT_COUNT"

echo
echo "=== Health Check Complete ==="
```

## See Also

- [Server Setup Guide](SERVER_SETUP.md) - Installation and deployment
- [Server Configuration](SERVER_CONFIGURATION.md) - Configuration options
- [Firewall Configuration](FIREWALL_CONFIGURATION.md) - Network security
