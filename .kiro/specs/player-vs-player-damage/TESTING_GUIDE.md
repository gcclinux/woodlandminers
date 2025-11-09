# Player-vs-Player Combat Testing Guide

## Overview
This guide provides step-by-step instructions for manually testing the PvP combat functionality implemented in tasks 1-10.

## Prerequisites
- Java 21+ installed
- Gradle 9.2.0+ installed
- Project built successfully

## Build Instructions

### Step 1: Build the Project
```bash
gradle build
```

This will create two JAR files:
- `build/libs/woodlanders-server.jar` - Dedicated server (headless)
- `build/libs/woodlanders-client.jar` - Game client (with rendering)

## Testing Procedure

### Step 1: Start the Dedicated Server

Open a terminal/command prompt and run:
```bash
java -jar build/libs/woodlanders-server.jar
```

**Expected Output:**
```
[INFO] Woodlanders Dedicated Server v1.0
[INFO] Loading configuration from server.properties
[INFO] Starting server on port 25565
[INFO] Server started successfully
[INFO] Server ready to accept connections
```

**Note:** Keep this terminal window open. The server must remain running for the entire test.

### Step 2: Start First Game Client (Player A)

Open a second terminal/command prompt and run:
```bash
java -jar build/libs/woodlanders-client.jar
```

**In the game:**
1. Press ESC to open the menu
2. Navigate to "Player Name"
3. Enter "PlayerA" (or any name you prefer)
4. Press ESC again to open menu
5. Navigate to "Multiplayer" → "Connect to Server"
6. Enter IP: `localhost` (or `127.0.0.1`)
7. Enter Port: `25565`
8. Press Enter to connect

**Expected Result:** You should see your character spawn in the game world.

### Step 3: Start Second Game Client (Player B)

Open a third terminal/command prompt and run:
```bash
java -jar build/libs/woodlanders-client.jar
```

**In the game:**
1. Press ESC to open the menu
2. Navigate to "Player Name"
3. Enter "PlayerB" (or any different name)
4. Press ESC again to open menu
5. Navigate to "Multiplayer" → "Connect to Server"
6. Enter IP: `localhost` (or `127.0.0.1`)
7. Enter Port: `25565`
8. Press Enter to connect

**Expected Result:** 
- Player B should see their character spawn
- Player A should see Player B appear in their world
- Player B should see Player A in their world

## Test Cases

### Test Case 1: Basic Player Attack
**Objective:** Verify that Player A can attack Player B and deal damage

**Steps:**
1. In Player A's client, use WASD keys to move close to Player B (within 100 pixels)
2. Press SPACEBAR on Player A's client
3. Observe Player B's health

**Expected Results:**
- ✅ Player B's health decreases by 10 (from 100 to 90)
- ✅ A health bar appears above Player B's character
- ✅ Player A's console shows: "Attacking player: {PlayerB's ID}"
- ✅ Player B's console shows: "Taking damage! Health: 90"
- ✅ Server console shows the attack was processed

**Requirements Tested:** 1.1, 1.2, 1.3, 2.1, 3.1, 3.2, 3.4, 3.5, 5.1, 5.4

### Test Case 2: Health Bar Rendering
**Objective:** Verify health bars display correctly for both local and remote players

**Steps:**
1. Continue from Test Case 1 (Player B has taken damage)
2. Observe the health bar above Player B in both clients

**Expected Results:**
- ✅ Player A sees a health bar above Player B (remote player)
- ✅ Player B sees a health bar above their own character (local player)
- ✅ Health bar shows green for current health (90%)
- ✅ Health bar shows red for missing health (10%)
- ✅ Health bar is positioned correctly above the player sprite

**Requirements Tested:** 2.2, 2.3, 5.3, 5.5

### Test Case 3: Attack Cooldown
**Objective:** Verify the 0.5-second cooldown between attacks

**Steps:**
1. Position Player A within attack range of Player B
2. Rapidly press SPACEBAR multiple times (as fast as possible)
3. Observe Player B's health changes

**Expected Results:**
- ✅ Player B's health decreases at most once every 0.5 seconds
- ✅ Not every spacebar press results in damage
- ✅ Server logs show some attacks are ignored due to cooldown
- ✅ After waiting 0.5 seconds, the next attack succeeds

**Requirements Tested:** 4.1, 4.2, 4.3, 4.4

### Test Case 4: Attack Range Validation
**Objective:** Verify attacks only work within 100 pixels

**Steps:**
1. Position Player A close to Player B (within 100 pixels)
2. Press SPACEBAR - attack should succeed
3. Move Player A away from Player B (more than 100 pixels)
4. Press SPACEBAR - attack should fail

**Expected Results:**
- ✅ Attack succeeds when players are close (within 100 pixels)
- ✅ Attack fails when players are far apart (beyond 100 pixels)
- ✅ Player A's console shows "No target found" when out of range
- ✅ Server validates range and ignores out-of-range attacks
- ✅ Server logs show "Player attack out of range" for invalid attempts

**Requirements Tested:** 1.1, 3.1, 3.2, 3.3

### Test Case 5: Player Death and Respawn
**Objective:** Verify player respawns with full health after death

**Steps:**
1. Position Player A within attack range of Player B
2. Press SPACEBAR repeatedly on Player A's client
3. Continue attacking until Player B's health reaches 0

**Expected Results:**
- ✅ Player B's health decreases by 10 with each successful attack
- ✅ Health bar updates after each attack
- ✅ When health reaches 0, Player B respawns at a new location
- ✅ Player B's health is restored to 100 after respawn
- ✅ Player B's health bar disappears (health is full)
- ✅ Player B can move and play normally after respawn

**Requirements Tested:** 2.4, 2.5

### Test Case 6: Attack Priority (Player vs Tree)
**Objective:** Verify players are prioritized over trees when both are in range

**Steps:**
1. Find a tree in the game world
2. Position Player A next to the tree (within attack range)
3. Position Player B on the opposite side of the tree (also within attack range of Player A)
4. Ensure both the tree AND Player B are within 100 pixels of Player A
5. Press SPACEBAR on Player A's client

**Expected Results:**
- ✅ Player B takes damage (not the tree)
- ✅ Player A's console shows "Attacking player: {PlayerB's ID}"
- ✅ The tree's health does NOT decrease
- ✅ Player attacks are prioritized over tree attacks

**Requirements Tested:** 1.4, 1.5, 5.2

### Test Case 7: Bidirectional Combat
**Objective:** Verify both players can attack each other

**Steps:**
1. Position Player A and Player B within attack range of each other
2. Press SPACEBAR on Player A's client
3. Wait 0.5 seconds
4. Press SPACEBAR on Player B's client
5. Observe both players' health

**Expected Results:**
- ✅ Player B takes damage from Player A's attack
- ✅ Player A takes damage from Player B's attack
- ✅ Both players see health bars above each other
- ✅ Both players see health bars above themselves
- ✅ Console logs show attacks in both directions

**Requirements Tested:** 1.1, 1.2, 2.1, 2.2, 3.1, 3.4, 3.5

### Test Case 8: Multiple Attacks to Death
**Objective:** Verify complete combat sequence from full health to respawn

**Steps:**
1. Note Player B's starting health (should be 100)
2. Attack Player B 10 times with Player A (waiting for cooldown between attacks)
3. Observe the complete sequence

**Expected Results:**
- ✅ Attack 1: Health 100 → 90
- ✅ Attack 2: Health 90 → 80
- ✅ Attack 3: Health 80 → 70
- ✅ Attack 4: Health 70 → 60
- ✅ Attack 5: Health 60 → 50
- ✅ Attack 6: Health 50 → 40
- ✅ Attack 7: Health 40 → 30
- ✅ Attack 8: Health 30 → 20
- ✅ Attack 9: Health 20 → 10
- ✅ Attack 10: Health 10 → 0 (respawn triggered)
- ✅ After respawn: Health = 100

**Requirements Tested:** All requirements (comprehensive test)

## Console Log Verification

### Player A Console (Attacker)
Look for these messages:
```
Attacking player: <PlayerB's ID>
```

### Player B Console (Victim)
Look for these messages:
```
Taking damage! Health: 90
Taking damage! Health: 80
Taking damage! Health: 70
...
```

### Server Console
Look for these messages:
```
[INFO] Player <PlayerA's ID> attacked player <PlayerB's ID>
[INFO] Player <PlayerB's ID> health updated: 90
[INFO] Broadcasting health update to all clients
```

For invalid attacks, look for:
```
[WARN] Player attack out of range: distance=150.5
[WARN] Player attack on cooldown: <attackerId> -> <targetId>
```

## Troubleshooting

### Issue: Players don't see each other
**Solution:** 
- Ensure both clients are connected to the same server
- Check server console for connection messages
- Try moving around to trigger position updates

### Issue: Attacks don't deal damage
**Solution:**
- Verify players are within 100 pixels of each other
- Check console logs for error messages
- Ensure 0.5 seconds have passed since last attack
- Restart server and clients if needed

### Issue: Health bar doesn't appear
**Solution:**
- Verify player health is below 100
- Check that health update messages are being broadcast
- Look for rendering errors in console

### Issue: Server not starting
**Solution:**
- Check if port 25565 is already in use
- Verify Java 21+ is installed: `java -version`
- Check server.properties file exists and is valid

### Issue: Client can't connect
**Solution:**
- Verify server is running
- Check IP address and port are correct
- Ensure firewall allows connections on port 25565
- Try using `127.0.0.1` instead of `localhost`

## Test Results Template

Use this template to document your test results:

```
# PvP Combat Test Results
Date: ___________
Tester: ___________

## Environment
- OS: ___________
- Java Version: ___________
- Build Version: ___________

## Test Case Results

### Test Case 1: Basic Player Attack
- [ ] Player B health decreases by 10
- [ ] Health bar appears above Player B
- [ ] Console logs show attack messages
- [ ] Server processes attack correctly
Notes: ___________

### Test Case 2: Health Bar Rendering
- [ ] Health bar visible on remote player
- [ ] Health bar visible on local player
- [ ] Colors correct (green/red)
- [ ] Position correct above sprite
Notes: ___________

### Test Case 3: Attack Cooldown
- [ ] Cooldown enforced (0.5 seconds)
- [ ] Rapid attacks blocked
- [ ] Attack succeeds after cooldown
Notes: ___________

### Test Case 4: Attack Range Validation
- [ ] Attack succeeds within range
- [ ] Attack fails beyond range
- [ ] Server validates range
Notes: ___________

### Test Case 5: Player Death and Respawn
- [ ] Health reaches 0
- [ ] Player respawns
- [ ] Health restored to 100
- [ ] Player functional after respawn
Notes: ___________

### Test Case 6: Attack Priority
- [ ] Player prioritized over tree
- [ ] Tree not damaged
- [ ] Console shows player attack
Notes: ___________

### Test Case 7: Bidirectional Combat
- [ ] Both players can attack
- [ ] Both players take damage
- [ ] Health bars show for both
Notes: ___________

### Test Case 8: Multiple Attacks to Death
- [ ] All 10 attacks successful
- [ ] Health decreases correctly
- [ ] Respawn triggered at 0 health
Notes: ___________

## Overall Result
- [ ] All tests passed
- [ ] Some tests failed (see notes)
- [ ] Major issues found

## Issues Found
1. ___________
2. ___________
3. ___________

## Recommendations
___________
```

## Success Criteria

All test cases must pass for the PvP combat feature to be considered complete:
- ✅ Players can attack each other within range
- ✅ Damage is applied correctly (10 per attack)
- ✅ Health bars render properly
- ✅ Attack cooldown works (0.5 seconds)
- ✅ Range validation works (100 pixels)
- ✅ Players respawn after death
- ✅ Attack priority works (players before trees)
- ✅ Server validates all attacks
- ✅ Console logs provide feedback
- ✅ Bidirectional combat works

## Next Steps

After completing all tests:
1. Document any issues found
2. Create bug reports for failures
3. Mark task 11 as complete if all tests pass
4. Celebrate successful PvP implementation! 🎉
