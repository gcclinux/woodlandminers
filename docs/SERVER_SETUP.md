# Woodlanders Server Setup Guide

## Overview

This guide walks you through setting up a Woodlanders dedicated server, from installation to running your first multiplayer session.

## Prerequisites

### System Requirements

**Minimum**:
- CPU: 2 cores @ 2.0 GHz
- RAM: 1 GB
- Storage: 500 MB
- Network: 1 Mbps upload per 5 clients
- OS: Windows 10+, Linux (Ubuntu 20.04+), macOS 10.15+

**Recommended**:
- CPU: 4 cores @ 3.0 GHz
- RAM: 4 GB
- Storage: 2 GB
- Network: 10 Mbps upload
- OS: Linux (Ubuntu 22.04 LTS)

### Software Requirements

- **Java Runtime Environment (JRE) 21 or higher**
  - Check version: `java -version`
  - Download: https://adoptium.net/

## Installation

### Step 1: Download Server Files

1. Download the latest `woodlanders-server.jar` from the releases page
2. Create a dedicated directory for the server:

```bash
# Linux/macOS
mkdir -p ~/woodlanders-server
cd ~/woodlanders-server

# Windows (PowerShell)
New-Item -ItemType Directory -Path "$HOME\woodlanders-server"
cd "$HOME\woodlanders-server"
```

3. Place `woodlanders-server.jar` in this directory

### Step 2: Initial Configuration

1. Create a `server.properties` file (or let the server generate it):

```bash
# Linux/macOS
touch server.properties

# Windows (PowerShell)
New-Item -ItemType File -Path "server.properties"
```

2. Edit `server.properties` with your preferred settings:

```properties
server.port=25565
server.max-clients=20
world.seed=0
server.heartbeat-interval=5
server.client-timeout=15
server.rate-limit=100
server.debug=false
```

See [Server Configuration Guide](SERVER_CONFIGURATION.md) for detailed property descriptions.

### Step 3: First Run

Start the server to verify installation:

```bash
java -jar woodlanders-server.jar
```

You should see output similar to:

```
[INFO] Woodlanders Dedicated Server v1.0
[INFO] Loading configuration from server.properties
[INFO] Starting server on port 25565
[INFO] World seed: 1234567890
[INFO] Server started successfully
[INFO] Public IPv4: 192.168.1.100
[INFO] Server ready to accept connections
```

Press `Ctrl+C` to stop the server.

## Deployment Options

### Option 1: Local Network (LAN)

Perfect for playing with friends on the same network.

1. Start the server on any computer in your network
2. Note the local IP address displayed (e.g., `192.168.1.100`)
3. Other players connect using this IP address
4. No firewall configuration needed (usually)

**Finding your local IP**:

```bash
# Linux
ip addr show | grep inet

# macOS
ifconfig | grep inet

# Windows (PowerShell)
ipconfig | findstr IPv4
```

### Option 2: Internet Server (Public)

Host a server accessible from anywhere on the internet.

**Requirements**:
- Static IP address or dynamic DNS
- Router with port forwarding capability
- Firewall configuration

**Steps**:

1. Configure port forwarding on your router:
   - Forward external port 25565 to your server's local IP
   - Protocol: TCP
   - See your router's manual for specific instructions

2. Configure firewall (see [Firewall Configuration](FIREWALL_CONFIGURATION.md))

3. Find your public IP address:
   ```bash
   curl ifconfig.me
   ```

4. Share your public IP with players

**Security Note**: Exposing a server to the internet has security risks. Follow all security best practices.

### Option 3: Cloud Hosting

Deploy on a cloud provider for best performance and reliability.

#### AWS EC2

1. Launch an EC2 instance:
   - AMI: Ubuntu 22.04 LTS
   - Instance type: t3.medium (or larger)
   - Storage: 20 GB gp3

2. Configure security group:
   - Allow TCP port 25565 from 0.0.0.0/0
   - Allow SSH port 22 from your IP

3. Connect via SSH and install Java:
   ```bash
   sudo apt update
   sudo apt install -y openjdk-21-jre-headless
   ```

4. Upload server files:
   ```bash
   scp woodlanders-server.jar ubuntu@<instance-ip>:~/
   scp server.properties ubuntu@<instance-ip>:~/
   ```

5. Start server (see "Running as a Service" below)

#### DigitalOcean Droplet

1. Create a droplet:
   - Image: Ubuntu 22.04 LTS
   - Plan: Basic ($12/month or higher)
   - Datacenter: Choose closest to your players

2. SSH into droplet and install Java:
   ```bash
   apt update
   apt install -y openjdk-21-jre-headless
   ```

3. Upload and configure server files

4. Configure firewall:
   ```bash
   ufw allow 25565/tcp
   ufw enable
   ```

#### Other Providers

Similar steps apply for:
- Google Cloud Platform (GCP)
- Microsoft Azure
- Linode
- Vultr
- OVH

## Running the Server

### Manual Start (Development)

Simple command-line execution:

```bash
java -jar woodlanders-server.jar
```

**Pros**: Easy to start/stop, see output immediately
**Cons**: Stops when terminal closes, no automatic restart

### Using Screen (Linux/macOS)

Keep server running after disconnecting from SSH:

```bash
# Start a screen session
screen -S woodlanders

# Start server
java -Xms2G -Xmx2G -jar woodlanders-server.jar

# Detach: Press Ctrl+A, then D

# Reattach later
screen -r woodlanders

# List sessions
screen -ls
```

### Using tmux (Linux/macOS)

Alternative to screen with more features:

```bash
# Start tmux session
tmux new -s woodlanders

# Start server
java -Xms2G -Xmx2G -jar woodlanders-server.jar

# Detach: Press Ctrl+B, then D

# Reattach later
tmux attach -t woodlanders

# List sessions
tmux ls
```

### Running as a Service (Linux)

Create a systemd service for automatic startup and restart:

1. Create service file:

```bash
sudo nano /etc/systemd/system/woodlanders.service
```

2. Add configuration:

```ini
[Unit]
Description=Woodlanders Dedicated Server
After=network.target

[Service]
Type=simple
User=woodlanders
WorkingDirectory=/home/woodlanders/server
ExecStart=/usr/bin/java -Xms2G -Xmx2G -jar woodlanders-server.jar
Restart=always
RestartSec=10
StandardOutput=append:/var/log/woodlanders/server.log
StandardError=append:/var/log/woodlanders/error.log

[Install]
WantedBy=multi-user.target
```

3. Create dedicated user:

```bash
sudo useradd -r -m -d /home/woodlanders woodlanders
sudo mkdir -p /home/woodlanders/server
sudo mkdir -p /var/log/woodlanders
sudo chown -R woodlanders:woodlanders /home/woodlanders
sudo chown -R woodlanders:woodlanders /var/log/woodlanders
```

4. Copy server files:

```bash
sudo cp woodlanders-server.jar /home/woodlanders/server/
sudo cp server.properties /home/woodlanders/server/
sudo chown woodlanders:woodlanders /home/woodlanders/server/*
```

5. Enable and start service:

```bash
sudo systemctl daemon-reload
sudo systemctl enable woodlanders
sudo systemctl start woodlanders
```

6. Manage service:

```bash
# Check status
sudo systemctl status woodlanders

# Stop server
sudo systemctl stop woodlanders

# Restart server
sudo systemctl restart woodlanders

# View logs
sudo journalctl -u woodlanders -f
```

### Running as a Service (Windows)

Use NSSM (Non-Sucking Service Manager):

1. Download NSSM from https://nssm.cc/download

2. Install service:

```powershell
nssm install Woodlanders "C:\Program Files\Java\jdk-21\bin\java.exe" "-Xms2G -Xmx2G -jar C:\woodlanders-server\woodlanders-server.jar"
```

3. Configure service:

```powershell
nssm set Woodlanders AppDirectory C:\woodlanders-server
nssm set Woodlanders AppStdout C:\woodlanders-server\logs\server.log
nssm set Woodlanders AppStderr C:\woodlanders-server\logs\error.log
```

4. Start service:

```powershell
nssm start Woodlanders
```

5. Manage service:

```powershell
# Stop
nssm stop Woodlanders

# Restart
nssm restart Woodlanders

# Remove
nssm remove Woodlanders confirm
```

## Server Management

### Starting the Server

```bash
# Basic start
java -jar woodlanders-server.jar

# With memory allocation
java -Xms2G -Xmx2G -jar woodlanders-server.jar

# With custom port
java -jar woodlanders-server.jar --port 7777

# With all options
java -Xms4G -Xmx4G -XX:+UseG1GC -jar woodlanders-server.jar --port 25565 --max-clients 50
```

### Stopping the Server

**Graceful shutdown** (recommended):
- Press `Ctrl+C` in the terminal
- Server will disconnect all clients cleanly
- World state is saved

**Force kill** (not recommended):
```bash
# Find process
ps aux | grep woodlanders-server

# Kill process
kill -9 <PID>
```

### Monitoring

#### Check if Server is Running

```bash
# Linux/macOS
ps aux | grep woodlanders-server
netstat -tuln | grep 25565

# Windows (PowerShell)
Get-Process | Where-Object {$_.ProcessName -like "*java*"}
netstat -an | findstr 25565
```

#### Monitor Resource Usage

```bash
# Linux - top
top -p $(pgrep -f woodlanders-server)

# Linux - htop (more user-friendly)
htop -p $(pgrep -f woodlanders-server)

# Windows - Task Manager
# Open Task Manager and find java.exe process
```

#### View Logs

```bash
# If using systemd
sudo journalctl -u woodlanders -f

# If redirecting to file
tail -f server.log

# View last 100 lines
tail -n 100 server.log

# Search logs
grep "ERROR" server.log
```

### Backup and Restore

#### Backup

```bash
#!/bin/bash
# backup.sh

BACKUP_DIR="/backups/woodlanders"
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p $BACKUP_DIR

# Stop server
sudo systemctl stop woodlanders

# Create backup
tar -czf $BACKUP_DIR/woodlanders_$DATE.tar.gz \
    /home/woodlanders/server/server.properties \
    /home/woodlanders/server/woodlanders-server.jar

# Start server
sudo systemctl start woodlanders

# Keep only last 7 backups
ls -t $BACKUP_DIR/woodlanders_*.tar.gz | tail -n +8 | xargs rm -f
```

#### Restore

```bash
#!/bin/bash
# restore.sh

BACKUP_FILE=$1

# Stop server
sudo systemctl stop woodlanders

# Extract backup
tar -xzf $BACKUP_FILE -C /

# Start server
sudo systemctl start woodlanders
```

### Updating the Server

1. Stop the server:
   ```bash
   sudo systemctl stop woodlanders
   ```

2. Backup current version:
   ```bash
   cp woodlanders-server.jar woodlanders-server.jar.backup
   ```

3. Download new version:
   ```bash
   wget https://example.com/woodlanders-server-v1.1.jar
   mv woodlanders-server-v1.1.jar woodlanders-server.jar
   ```

4. Start server:
   ```bash
   sudo systemctl start woodlanders
   ```

5. Verify update:
   ```bash
   sudo journalctl -u woodlanders -n 50
   ```

## Performance Tuning

### JVM Tuning

```bash
# Recommended for 4GB server
java -Xms4G -Xmx4G \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=50 \
     -XX:+ParallelRefProcEnabled \
     -XX:+UnlockExperimentalVMOptions \
     -XX:G1NewSizePercent=30 \
     -XX:G1MaxNewSizePercent=40 \
     -jar woodlanders-server.jar
```

### Network Tuning (Linux)

```bash
# Increase network buffer sizes
sudo sysctl -w net.core.rmem_max=16777216
sudo sysctl -w net.core.wmem_max=16777216
sudo sysctl -w net.ipv4.tcp_rmem="4096 87380 16777216"
sudo sysctl -w net.ipv4.tcp_wmem="4096 65536 16777216"

# Make permanent
echo "net.core.rmem_max=16777216" | sudo tee -a /etc/sysctl.conf
echo "net.core.wmem_max=16777216" | sudo tee -a /etc/sysctl.conf
```

### Disk I/O Tuning

```bash
# Use SSD for server files
# Enable noatime mount option
sudo mount -o remount,noatime /home/woodlanders
```

## Security Best Practices

1. **Run as non-root user**: Never run server as root
2. **Use firewall**: Only allow necessary ports
3. **Keep Java updated**: Install security patches promptly
4. **Monitor logs**: Watch for suspicious activity
5. **Rate limiting**: Keep default rate limits enabled
6. **Regular backups**: Automate backup process
7. **Strong passwords**: If adding authentication in future
8. **Disable debug mode**: In production environments

## Troubleshooting

For common issues and solutions, see the [Troubleshooting Guide](TROUBLESHOOTING.md).

## Next Steps

- [Configure Firewall](FIREWALL_CONFIGURATION.md)
- [Optimize Server Configuration](SERVER_CONFIGURATION.md)
- [Troubleshooting Common Issues](TROUBLESHOOTING.md)

## Support

- GitHub Issues: https://github.com/yourusername/woodlanders/issues
- Community Discord: [Your Discord Link]
- Documentation: https://github.com/yourusername/woodlanders/wiki
