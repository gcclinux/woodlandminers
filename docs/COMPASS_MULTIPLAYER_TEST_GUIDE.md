# Compass Multiplayer Testing Guide

## Overview

This guide provides step-by-step instructions for testing the Home Compass feature in multiplayer mode. The compass should point to the spawn point (0, 0) for both host and client players.

## Prerequisites

- Game built and ready to run: `gradle build`
- Two terminal windows available
- Compass feature implemented and working in single-player mode

## Test Setup

### Terminal 1 - Host Player

1. Open first terminal window
2. Navigate to project directory
3. Run: `gradle run`
4. Wait for game to launch

### Terminal 2 - Client Player

1. Open second terminal window
2. Navigate to project directory
3. Run: `gradle run`
4. Wait for game to launch

## Test 5.1: Compass in Multiplayer Host Mode

### Setup Host

1. In Terminal 1 game window:
   - Press `ESC` to open menu
   - Select "Multiplayer"
   - Select "Host Server"
   - Note the displayed IP address (should show localhost/127.0.0.1)
   - Server will start and you'll return to gameplay

### Verify Compass Appears

**Expected**: Compass should be visible in bottom-left corner
- [ ] Compass background (64x64 circular graphic) is visible
- [ ] Compass needle (red arrow) is visible
- [ ] Compass is positioned 10 pixels from left edge
- [ ] Compass is positioned 10 pixels from bottom edge
- [ ] Compass does not overlap with other UI elements

### Test Compass Points to Spawn (0, 0)

**Initial Position Test**:
- [ ] At spawn point (0, 0), compass shows neutral orientation
- [ ] Needle is visible and not flickering

**Movement Tests**:

1. **Move North** (press W or UP):
   - Walk to approximately (0, 500)
   - [ ] Compass needle points DOWN (south, toward spawn)
   - [ ] Needle rotation is smooth during movement

2. **Move South** (press S or DOWN):
   - Walk to approximately (0, -500)
   - [ ] Compass needle points UP (north, toward spawn)
   - [ ] Needle rotation updates correctly

3. **Move East** (press D or RIGHT):
   - Walk to approximately (500, 0)
   - [ ] Compass needle points LEFT (west, toward spawn)
   - [ ] Needle rotation is accurate

4. **Move West** (press A or LEFT):
   - Walk to approximately (-500, 0)
   - [ ] Compass needle points RIGHT (east, toward spawn)
   - [ ] Needle rotation is accurate

5. **Move Diagonally**:
   - Walk to approximately (500, 500) - northeast
   - [ ] Compass needle points SOUTHWEST (toward spawn)
   - [ ] Angle appears correct (approximately 225°)

6. **Return to Spawn**:
   - Walk back to (0, 0)
   - [ ] Compass returns to neutral orientation
   - [ ] No errors or visual glitches

### Test Compass Functionality

- [ ] Compass remains visible during all movement
- [ ] Compass updates every frame (smooth rotation)
- [ ] Compass is not affected by camera movement
- [ ] Compass stays in bottom-left corner regardless of player position

### Test Compass with Menu

1. Press `ESC` to open menu
   - [ ] Compass remains visible during menu
   - [ ] Compass stops updating rotation (player not moving)

2. Close menu (press `ESC` again)
   - [ ] Compass resumes updating when player moves

### Performance Check

- [ ] No noticeable FPS drop with compass enabled
- [ ] Compass rendering is smooth
- [ ] No lag or stuttering during rotation updates

---

## Test 5.2: Compass in Multiplayer Client Mode

### Connect Client to Host

1. In Terminal 2 game window:
   - Press `ESC` to open menu
   - Select "Multiplayer"
   - Select "Connect to Server"
   - Enter: `localhost:25565` (or just `localhost`)
   - Press `ENTER`
   - Wait for connection confirmation

### Verify Compass Appears

**Expected**: Compass should be visible in bottom-left corner
- [ ] Compass background (64x64 circular graphic) is visible
- [ ] Compass needle (red arrow) is visible
- [ ] Compass is positioned 10 pixels from left edge
- [ ] Compass is positioned 10 pixels from bottom edge
- [ ] Compass does not overlap with other UI elements

### Test Compass Points to Server Spawn (0, 0)

**Initial Position Test**:
- [ ] Client spawns at or near (0, 0)
- [ ] Compass shows neutral orientation or points correctly if not at exact spawn

**Movement Tests**:

1. **Move North** (press W or UP):
   - Walk to approximately (0, 500)
   - [ ] Compass needle points DOWN (south, toward spawn)
   - [ ] Needle rotation is smooth during movement

2. **Move South** (press S or DOWN):
   - Walk to approximately (0, -500)
   - [ ] Compass needle points UP (north, toward spawn)
   - [ ] Needle rotation updates correctly

3. **Move East** (press D or RIGHT):
   - Walk to approximately (500, 0)
   - [ ] Compass needle points LEFT (west, toward spawn)
   - [ ] Needle rotation is accurate

4. **Move West** (press A or LEFT):
   - Walk to approximately (-500, 0)
   - [ ] Compass needle points RIGHT (east, toward spawn)
   - [ ] Needle rotation is accurate

5. **Move Diagonally**:
   - Walk to approximately (-500, -500) - southwest
   - [ ] Compass needle points NORTHEAST (toward spawn)
   - [ ] Angle appears correct (approximately 45°)

6. **Return to Spawn**:
   - Walk back to (0, 0)
   - [ ] Compass returns to neutral orientation
   - [ ] No errors or visual glitches

### Test Compass During Network Activity

**Simultaneous Movement Test**:
1. Have both host and client move around simultaneously
   - [ ] Client compass continues functioning correctly
   - [ ] Client compass always points to (0, 0) regardless of host position
   - [ ] No interference between players

**Network Latency Test**:
1. Simulate network activity (both players moving, chopping trees, picking items)
   - [ ] Compass continues functioning smoothly
   - [ ] Compass rotation is not affected by network messages
   - [ ] No stuttering or lag in compass updates

**Connection Quality Test**:
1. Monitor connection quality indicator (top-right corner)
   - [ ] Compass functions correctly with "Good" connection
   - [ ] Compass functions correctly with "Fair" connection
   - [ ] Compass functions correctly with "Poor" connection

### Test Compass Persistence

**Menu Toggle Test**:
1. Press `ESC` to open menu
   - [ ] Compass remains visible
   - [ ] Compass stops updating (player not moving)

2. Close menu
   - [ ] Compass resumes updating immediately

**Reconnection Test** (Optional):
1. Disconnect client (close Terminal 2 game)
2. Restart client and reconnect
   - [ ] Compass appears immediately after connection
   - [ ] Compass functions correctly after reconnection

### Performance Check

- [ ] No noticeable FPS drop with compass enabled
- [ ] Compass rendering is smooth on client
- [ ] No lag or stuttering during rotation updates
- [ ] Compass performance matches single-player mode

---

## Visual Verification Checklist

### Compass Appearance
- [ ] Compass size is exactly 64x64 pixels
- [ ] Compass background is circular and semi-transparent
- [ ] Compass needle is clearly visible (red arrow)
- [ ] Compass has good contrast against grass terrain
- [ ] Compass remains visible during rain effects

### Compass Position
- [ ] Bottom-left corner placement is consistent
- [ ] 10 pixels from left edge (measure if needed)
- [ ] 10 pixels from bottom edge (measure if needed)
- [ ] Does not overlap health bars
- [ ] Does not overlap connection quality indicator
- [ ] Does not overlap player name tags

### Compass Rotation
- [ ] Rotation is smooth (no jarring jumps)
- [ ] Rotation direction is correct (points toward spawn)
- [ ] Rotation updates at consistent frame rate
- [ ] No visual artifacts during rotation

---

## Troubleshooting

### Compass Not Appearing

**Check**:
1. Verify compass textures exist:
   - `assets/ui/compass_background.png`
   - `assets/ui/compass_needle.png`
2. Check console for texture loading errors
3. Verify compass initialization in `MyGdxGame.create()`

### Compass Not Rotating

**Check**:
1. Verify `compass.update()` is called in render loop
2. Check that update is called with correct player position
3. Verify spawn point is (0.0, 0.0)

### Compass Rotation Incorrect

**Check**:
1. Verify needle texture is designed pointing right (0°)
2. Check angle calculation in `Compass.update()`
3. Test at cardinal directions (N, S, E, W) first

### Connection Issues

**Check**:
1. Host started server before client connects
2. Using correct address: `localhost:25565`
3. Port 25565 is not blocked by firewall
4. Check console output for connection errors

---

## Test Results Template

Copy this template to record your test results:

```
## Test Execution Date: [DATE]
## Tester: [NAME]

### Test 5.1: Multiplayer Host Mode
- Compass Appears: [ ] PASS [ ] FAIL
- Points to Spawn: [ ] PASS [ ] FAIL
- Rotation Accuracy: [ ] PASS [ ] FAIL
- Smooth Updates: [ ] PASS [ ] FAIL
- Performance: [ ] PASS [ ] FAIL

Notes:
[Add any observations or issues]

### Test 5.2: Multiplayer Client Mode
- Compass Appears: [ ] PASS [ ] FAIL
- Points to Spawn: [ ] PASS [ ] FAIL
- Rotation Accuracy: [ ] PASS [ ] FAIL
- Network Latency: [ ] PASS [ ] FAIL
- Performance: [ ] PASS [ ] FAIL

Notes:
[Add any observations or issues]

### Overall Result
- [ ] All tests passed
- [ ] Some tests failed (see notes)
- [ ] Major issues found

Issues Found:
1. [Issue description]
2. [Issue description]

Screenshots:
- [Attach screenshots if issues found]
```

---

## Success Criteria

All tests pass when:
1. ✅ Compass appears in both host and client modes
2. ✅ Compass always points to spawn point (0, 0)
3. ✅ Compass rotation is accurate in all directions
4. ✅ Compass updates smoothly without lag
5. ✅ Compass functions during network activity
6. ✅ No performance degradation
7. ✅ No visual glitches or artifacts

---

## Next Steps

After completing these tests:
1. Mark tasks 5.1 and 5.2 as complete in `tasks.md`
2. Document any issues found
3. Proceed to Test 6: Visual quality and performance testing
4. Update test results in this document or create a summary

