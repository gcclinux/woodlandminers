# Testing Multiplayer on Same Computer

Run multiple instances of Woodlanders on the same computer to test multiplayer functionality locally.

## Quick Start (All Platforms)

### Terminal 1 - Start Server:
```bash
java -jar build/libs/woodlanders-server.jar
```

### Terminal 2 - Start Player 1:
```bash
java -jar build/libs/woodlanders-client.jar
```

### Terminal 3 - Start Player 2:
```bash
java -jar build/libs/woodlanders-client.jar
```

Both clients will connect to `localhost:25565` and you'll see both players in the game world.

---

## Option 1: Different Player Configs (Linux/Mac)

To have different player names and positions for each instance:

### Terminal 1 - Server:
```bash
java -jar build/libs/woodlanders-server.jar
```

### Terminal 2 - Player 1:
```bash
XDG_CONFIG_HOME=/tmp/player1 java -jar build/libs/woodlanders-client.jar
```

### Terminal 3 - Player 2:
```bash
XDG_CONFIG_HOME=/tmp/player2 java -jar build/libs/woodlanders-client.jar
```

Each player will have separate config directories, allowing different names and positions.

---

## Option 2: Different Player Configs (Windows)

### Terminal 1 - Server:
```cmd
java -jar build/libs/woodlanders-server.jar
```

### Terminal 2 - Player 1:
```cmd
java -jar build/libs/woodlanders-client.jar
```

### Terminal 3 - Player 2:
```cmd
set APPDATA=%TEMP%\player2 && java -jar build/libs/woodlanders-client.jar
```

Each player will have separate config directories on Windows.

---

## Batch Script (Windows)

Create `run_multiplayer.bat` in the project root:

```batch
@echo off
echo Starting Woodlanders Multiplayer Test...
echo.

echo [1/3] Starting Server...
start "Woodlanders Server" java -jar build/libs/woodlanders-server.jar
timeout /t 2 /nobreak

echo [2/3] Starting Player 1...
start "Woodlanders Player 1" java -jar build/libs/woodlanders-client.jar
timeout /t 1 /nobreak

echo [3/3] Starting Player 2...
start "Woodlanders Player 2" cmd /c "set APPDATA=%TEMP%\player2 && java -jar build/libs/woodlanders-client.jar"

echo.
echo All instances started! Check the windows above.
pause
```

Run it:
```cmd
run_multiplayer.bat
```

---

## Shell Script (Linux/Mac)

Create `run_multiplayer.sh` in the project root:

```bash
#!/bin/bash
echo "Starting Woodlanders Multiplayer Test..."
echo ""

echo "[1/3] Starting Server..."
java -jar build/libs/woodlanders-server.jar &
SERVER_PID=$!
sleep 2

echo "[2/3] Starting Player 1..."
java -jar build/libs/woodlanders-client.jar &
PLAYER1_PID=$!
sleep 1

echo "[3/3] Starting Player 2..."
XDG_CONFIG_HOME=/tmp/player2 java -jar build/libs/woodlanders-client.jar &
PLAYER2_PID=$!

echo ""
echo "All instances started!"
echo "Server PID: $SERVER_PID"
echo "Player 1 PID: $PLAYER1_PID"
echo "Player 2 PID: $PLAYER2_PID"
echo ""
echo "Press Ctrl+C to stop all instances"

wait
```

Run it:
```bash
chmod +x run_multiplayer.sh
./run_multiplayer.sh
```

---

## Testing Checklist

- [ ] Both players visible in game world
- [ ] Player movement synchronized
- [ ] Tree destruction synchronized
- [ ] Item pickup synchronized
- [ ] Inventory updates synchronized
- [ ] Health changes synchronized
- [ ] Bamboo planting synchronized
- [ ] Disconnect/reconnect handling

---

## Troubleshooting

**Port already in use:**
- Change server port in `server.properties` (default: 25565)
- Update client connection to match new port

**Players not seeing each other:**
- Verify both clients connected to same server
- Check server console for connection messages
- Ensure firewall allows localhost connections

**Config conflicts:**
- Use separate config directories (Option 2)
- Or delete config files between runs to reset state

