# Woodlanders Firewall Configuration Guide

## Overview

This guide covers firewall configuration for hosting a Woodlanders server. Proper firewall setup is essential for security while allowing legitimate game traffic.

## Port Requirements

The Woodlanders server requires **one TCP port** for all game traffic:

- **Default Port**: 25565 (TCP)
- **Protocol**: TCP only (no UDP required)
- **Direction**: Inbound connections from clients

## Operating System Firewalls

### Windows Firewall

#### Using Windows Defender Firewall GUI

1. Open Windows Defender Firewall:
   - Press `Win + R`
   - Type `wf.msc` and press Enter

2. Click "Inbound Rules" in left panel

3. Click "New Rule..." in right panel

4. Configure rule:
   - Rule Type: **Port**
   - Protocol: **TCP**
   - Specific local ports: **25565**
   - Action: **Allow the connection**
   - Profile: Check all (Domain, Private, Public)
   - Name: **Woodlanders Server**

5. Click "Finish"

#### Using PowerShell (Administrator)

```powershell
# Allow inbound TCP on port 25565
New-NetFirewallRule -DisplayName "Woodlanders Server" `
                    -Direction Inbound `
                    -Protocol TCP `
                    -LocalPort 25565 `
                    -Action Allow `
                    -Profile Any

# Verify rule was created
Get-NetFirewallRule -DisplayName "Woodlanders Server"
```

#### Using Command Prompt (Administrator)

```cmd
netsh advfirewall firewall add rule name="Woodlanders Server" dir=in action=allow protocol=TCP localport=25565
```

#### Remove Rule

```powershell
# PowerShell
Remove-NetFirewallRule -DisplayName "Woodlanders Server"

# Command Prompt
netsh advfirewall firewall delete rule name="Woodlanders Server"
```

### Linux Firewall (UFW)

UFW (Uncomplicated Firewall) is the default on Ubuntu and Debian.

#### Enable UFW

```bash
# Enable firewall
sudo ufw enable

# Check status
sudo ufw status
```

#### Allow Woodlanders Port

```bash
# Allow TCP port 25565
sudo ufw allow 25565/tcp

# Allow with comment
sudo ufw allow 25565/tcp comment 'Woodlanders Server'

# Verify rule
sudo ufw status numbered
```

#### Allow from Specific IP

```bash
# Allow only from specific IP
sudo ufw allow from 192.168.1.100 to any port 25565 proto tcp

# Allow from subnet
sudo ufw allow from 192.168.1.0/24 to any port 25565 proto tcp
```

#### Remove Rule

```bash
# List rules with numbers
sudo ufw status numbered

# Delete by number
sudo ufw delete <number>

# Or delete by specification
sudo ufw delete allow 25565/tcp
```

### Linux Firewall (iptables)

For systems using iptables directly (CentOS, RHEL, older systems).

#### Allow Woodlanders Port

```bash
# Allow TCP port 25565
sudo iptables -A INPUT -p tcp --dport 25565 -j ACCEPT

# Save rules (Ubuntu/Debian)
sudo iptables-save | sudo tee /etc/iptables/rules.v4

# Save rules (CentOS/RHEL)
sudo service iptables save
```

#### Allow from Specific IP

```bash
# Allow only from specific IP
sudo iptables -A INPUT -p tcp -s 192.168.1.100 --dport 25565 -j ACCEPT

# Allow from subnet
sudo iptables -A INPUT -p tcp -s 192.168.1.0/24 --dport 25565 -j ACCEPT
```

#### View Rules

```bash
# List all rules
sudo iptables -L -n -v

# List with line numbers
sudo iptables -L INPUT --line-numbers
```

#### Remove Rule

```bash
# Delete by line number
sudo iptables -D INPUT <line-number>

# Delete by specification
sudo iptables -D INPUT -p tcp --dport 25565 -j ACCEPT
```

### Linux Firewall (firewalld)

Used on Fedora, CentOS 7+, RHEL 7+.

#### Allow Woodlanders Port

```bash
# Allow TCP port 25565
sudo firewall-cmd --permanent --add-port=25565/tcp

# Reload firewall
sudo firewall-cmd --reload

# Verify
sudo firewall-cmd --list-ports
```

#### Allow from Specific IP

```bash
# Create rich rule for specific IP
sudo firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="192.168.1.100" port protocol="tcp" port="25565" accept'

# Reload
sudo firewall-cmd --reload
```

#### Remove Rule

```bash
# Remove port
sudo firewall-cmd --permanent --remove-port=25565/tcp

# Remove rich rule
sudo firewall-cmd --permanent --remove-rich-rule='rule family="ipv4" source address="192.168.1.100" port protocol="tcp" port="25565" accept'

# Reload
sudo firewall-cmd --reload
```

### macOS Firewall

macOS firewall is application-based by default.

#### Using System Preferences

1. Open System Preferences → Security & Privacy
2. Click "Firewall" tab
3. Click lock icon and authenticate
4. Click "Firewall Options"
5. Click "+" and add Java
6. Set to "Allow incoming connections"

#### Using Command Line (pf)

```bash
# Edit pf configuration
sudo nano /etc/pf.conf

# Add rule (before any block rules)
pass in proto tcp from any to any port 25565

# Load configuration
sudo pfctl -f /etc/pf.conf

# Enable pf
sudo pfctl -e

# Check status
sudo pfctl -s rules
```

## Router/Gateway Configuration

### Port Forwarding

To host a server accessible from the internet, configure port forwarding on your router.

#### General Steps

1. Find your server's local IP address:
   ```bash
   # Linux/macOS
   ip addr show
   
   # Windows
   ipconfig
   ```

2. Access router admin panel:
   - Usually at `192.168.1.1` or `192.168.0.1`
   - Check router label for default gateway

3. Navigate to Port Forwarding section:
   - May be under "Advanced", "NAT", or "Virtual Server"

4. Create port forwarding rule:
   - **Service Name**: Woodlanders
   - **External Port**: 25565
   - **Internal Port**: 25565
   - **Internal IP**: Your server's local IP
   - **Protocol**: TCP

5. Save and apply settings

#### Common Router Interfaces

**TP-Link**:
- Advanced → NAT Forwarding → Virtual Servers

**Netgear**:
- Advanced → Advanced Setup → Port Forwarding/Port Triggering

**Linksys**:
- Security → Apps and Gaming → Single Port Forwarding

**ASUS**:
- WAN → Virtual Server/Port Forwarding

**D-Link**:
- Advanced → Port Forwarding

### UPnP (Universal Plug and Play)

Some routers support automatic port forwarding via UPnP.

**Pros**:
- Automatic configuration
- No manual setup needed

**Cons**:
- Security risk if enabled globally
- Not all routers support it
- May not work reliably

**Enable UPnP** (if desired):
1. Access router admin panel
2. Find UPnP settings (usually under Advanced)
3. Enable UPnP
4. Restart router

**Note**: For security, prefer manual port forwarding over UPnP.

## Cloud Provider Firewalls

### AWS Security Groups

```bash
# Using AWS CLI
aws ec2 authorize-security-group-ingress \
    --group-id sg-xxxxxxxxx \
    --protocol tcp \
    --port 25565 \
    --cidr 0.0.0.0/0
```

**Using AWS Console**:
1. EC2 → Security Groups
2. Select your security group
3. Inbound Rules → Edit
4. Add Rule:
   - Type: Custom TCP
   - Port: 25565
   - Source: 0.0.0.0/0 (or specific IPs)

### Google Cloud Platform (GCP)

```bash
# Using gcloud CLI
gcloud compute firewall-rules create woodlanders-server \
    --allow tcp:25565 \
    --source-ranges 0.0.0.0/0 \
    --description "Woodlanders game server"
```

**Using GCP Console**:
1. VPC Network → Firewall
2. Create Firewall Rule:
   - Name: woodlanders-server
   - Direction: Ingress
   - Action: Allow
   - Targets: All instances
   - Source IP ranges: 0.0.0.0/0
   - Protocols and ports: tcp:25565

### Azure Network Security Groups

```bash
# Using Azure CLI
az network nsg rule create \
    --resource-group myResourceGroup \
    --nsg-name myNSG \
    --name WoodlandersServer \
    --protocol tcp \
    --priority 1000 \
    --destination-port-range 25565 \
    --access Allow
```

**Using Azure Portal**:
1. Network Security Groups → Your NSG
2. Inbound security rules → Add
3. Configure:
   - Source: Any
   - Source port ranges: *
   - Destination: Any
   - Destination port ranges: 25565
   - Protocol: TCP
   - Action: Allow
   - Priority: 1000
   - Name: WoodlandersServer

### DigitalOcean Firewall

**Using DigitalOcean Control Panel**:
1. Networking → Firewalls
2. Create Firewall or edit existing
3. Inbound Rules → New Rule:
   - Type: Custom
   - Protocol: TCP
   - Port Range: 25565
   - Sources: All IPv4, All IPv6

## Testing Firewall Configuration

### Test from Server

```bash
# Check if port is listening
netstat -tuln | grep 25565

# Linux - check if port is open
sudo ss -tulpn | grep 25565

# Test with telnet (from another machine)
telnet <server-ip> 25565
```

### Test from External Network

```bash
# Using netcat
nc -zv <server-ip> 25565

# Using telnet
telnet <server-ip> 25565

# Using nmap
nmap -p 25565 <server-ip>
```

### Online Port Checkers

Use online tools to verify port is accessible:
- https://www.yougetsignal.com/tools/open-ports/
- https://canyouseeme.org/
- https://portchecker.co/

**Steps**:
1. Ensure server is running
2. Enter your public IP and port 25565
3. Click "Check Port"
4. Should show "Port is open"

## Security Best Practices

### 1. Principle of Least Privilege

Only open ports that are absolutely necessary:

```bash
# Good: Only allow game port
sudo ufw allow 25565/tcp

# Bad: Allow all ports
sudo ufw allow from any to any
```

### 2. Restrict Source IPs (When Possible)

If you know player IPs, restrict access:

```bash
# Allow only specific IPs
sudo ufw allow from 203.0.113.0/24 to any port 25565 proto tcp

# Allow multiple IPs
sudo ufw allow from 203.0.113.10 to any port 25565 proto tcp
sudo ufw allow from 198.51.100.20 to any port 25565 proto tcp
```

### 3. Use Non-Standard Ports

Consider using a non-standard port to reduce automated attacks:

```properties
# server.properties
server.port=27015
```

Then configure firewall for the custom port.

### 4. Enable Connection Rate Limiting

Protect against connection floods:

```bash
# iptables rate limiting
sudo iptables -A INPUT -p tcp --dport 25565 -m state --state NEW -m recent --set
sudo iptables -A INPUT -p tcp --dport 25565 -m state --state NEW -m recent --update --seconds 60 --hitcount 10 -j DROP
```

### 5. Log Dropped Packets

Monitor blocked connection attempts:

```bash
# UFW logging
sudo ufw logging on

# iptables logging
sudo iptables -A INPUT -p tcp --dport 25565 -j LOG --log-prefix "Woodlanders: "
```

### 6. Regular Firewall Audits

Periodically review firewall rules:

```bash
# List all rules
sudo ufw status verbose

# Check for unnecessary rules
sudo iptables -L -n -v
```

### 7. Fail2Ban Integration

Automatically ban IPs with suspicious activity:

```bash
# Install fail2ban
sudo apt install fail2ban

# Create filter for Woodlanders
sudo nano /etc/fail2ban/filter.d/woodlanders.conf
```

```ini
[Definition]
failregex = ^.*\[WARNING\] Rate limit exceeded for client <HOST>.*$
            ^.*\[WARNING\] Invalid message from <HOST>.*$
ignoreregex =
```

```bash
# Configure jail
sudo nano /etc/fail2ban/jail.local
```

```ini
[woodlanders]
enabled = true
port = 25565
protocol = tcp
filter = woodlanders
logpath = /var/log/woodlanders/server.log
maxretry = 5
bantime = 3600
findtime = 600
```

```bash
# Restart fail2ban
sudo systemctl restart fail2ban
```

## Troubleshooting

### Port Already in Use

```bash
# Find what's using the port
sudo lsof -i :25565
sudo netstat -tulpn | grep 25565

# Kill the process
sudo kill -9 <PID>
```

### Firewall Blocking Legitimate Traffic

```bash
# Temporarily disable firewall for testing
sudo ufw disable

# Test connection
# If it works, firewall is the issue

# Re-enable firewall
sudo ufw enable

# Review and fix rules
```

### Can't Connect from Internet

1. Verify server is running: `netstat -tuln | grep 25565`
2. Check local firewall: `sudo ufw status`
3. Check router port forwarding configuration
4. Verify public IP: `curl ifconfig.me`
5. Test with online port checker
6. Check ISP doesn't block port (some ISPs block common ports)

### Connection Timeout

- Increase timeout in client
- Check for packet loss: `ping <server-ip>`
- Verify no intermediate firewalls blocking traffic
- Check server logs for connection attempts

## See Also

- [Server Setup Guide](SERVER_SETUP.md) - Installation and deployment
- [Server Configuration](SERVER_CONFIGURATION.md) - Configuration options
- [Troubleshooting Guide](TROUBLESHOOTING.md) - Common issues and solutions
