# PvP Combat Test Checklist

## Quick Reference
- **Server Command:** `java -jar build\libs\woodlanders-server.jar`
- **Client Command:** `java -jar build\libs\woodlanders-client.jar`
- **Attack Key:** SPACEBAR
- **Movement:** WASD keys
- **Menu:** ESC key

## Pre-Test Setup
- [ ] Build completed successfully (`gradle build`)
- [ ] Server JAR exists: `build/libs/woodlanders-server.jar`
- [ ] Client JAR exists: `build/libs/woodlanders-client.jar`
- [ ] Three terminal windows ready (1 server, 2 clients)

## Test Execution

### Setup Phase
- [ ] Server started and running on port 25565
- [ ] Player A client connected (name: PlayerA)
- [ ] Player B client connected (name: PlayerB)
- [ ] Both players visible to each other in game

### Test Case 1: Basic Player Attack ✅
**Requirements: 1.1, 1.2, 1.3, 2.1, 3.1, 3.2, 3.4, 3.5, 5.1, 5.4**
- [ ] Moved Player A within 100 pixels of Player B
- [ ] Pressed SPACEBAR on Player A
- [ ] Player B health decreased by 10 (100 → 90)
- [ ] Health bar appeared above Player B
- [ ] Player A console shows: "Attacking player: {ID}"
- [ ] Player B console shows: "Taking damage! Health: 90"
- [ ] Server console shows attack processed

**Notes:** _______________________________________________

### Test Case 2: Health Bar Rendering ✅
**Requirements: 2.2, 2.3, 5.3, 5.5**
- [ ] Health bar visible above Player B (remote player view)
- [ ] Health bar visible above Player B (local player view)
- [ ] Green bar shows current health (90%)
- [ ] Red bar shows missing health (10%)
- [ ] Health bar positioned correctly above sprite

**Notes:** _______________________________________________

### Test Case 3: Attack Cooldown ✅
**Requirements: 4.1, 4.2, 4.3, 4.4**
- [ ] Rapidly pressed SPACEBAR multiple times
- [ ] Only one attack per 0.5 seconds registered
- [ ] Some attacks ignored due to cooldown
- [ ] After waiting 0.5s, next attack succeeded
- [ ] Server logs show cooldown enforcement

**Notes:** _______________________________________________

### Test Case 4: Attack Range Validation ✅
**Requirements: 1.1, 3.1, 3.2, 3.3**
- [ ] Attack succeeded when within 100 pixels
- [ ] Moved Player A beyond 100 pixels from Player B
- [ ] Attack failed when out of range
- [ ] Player A console shows "No target found"
- [ ] Server logs show "Player attack out of range"

**Notes:** _______________________________________________

### Test Case 5: Player Death and Respawn ✅
**Requirements: 2.4, 2.5**
- [ ] Attacked Player B repeatedly (10 times)
- [ ] Health decreased: 100→90→80→70→60→50→40→30→20→10→0
- [ ] Player B respawned at new location
- [ ] Player B health restored to 100
- [ ] Health bar disappeared (full health)
- [ ] Player B can move normally after respawn

**Notes:** _______________________________________________

### Test Case 6: Attack Priority (Player vs Tree) ✅
**Requirements: 1.4, 1.5, 5.2**
- [ ] Found a tree in the game world
- [ ] Positioned Player A next to tree
- [ ] Positioned Player B on opposite side (both in range)
- [ ] Pressed SPACEBAR on Player A
- [ ] Player B took damage (not the tree)
- [ ] Console shows "Attacking player: {ID}"
- [ ] Tree health unchanged

**Notes:** _______________________________________________

### Test Case 7: Bidirectional Combat ✅
**Requirements: 1.1, 1.2, 2.1, 2.2, 3.1, 3.4, 3.5**
- [ ] Positioned both players within attack range
- [ ] Player A attacked Player B successfully
- [ ] Waited 0.5 seconds
- [ ] Player B attacked Player A successfully
- [ ] Both players show health bars
- [ ] Console logs show attacks in both directions

**Notes:** _______________________________________________

### Test Case 8: Complete Combat Sequence ✅
**Requirements: All (comprehensive test)**
- [ ] Started with Player B at 100 health
- [ ] Attack 1: 100 → 90 ✓
- [ ] Attack 2: 90 → 80 ✓
- [ ] Attack 3: 80 → 70 ✓
- [ ] Attack 4: 70 → 60 ✓
- [ ] Attack 5: 60 → 50 ✓
- [ ] Attack 6: 50 → 40 ✓
- [ ] Attack 7: 40 → 30 ✓
- [ ] Attack 8: 30 → 20 ✓
- [ ] Attack 9: 20 → 10 ✓
- [ ] Attack 10: 10 → 0 (respawn) ✓
- [ ] After respawn: Health = 100 ✓

**Notes:** _______________________________________________

## Console Log Verification

### Player A Console (Attacker)
- [ ] Shows "Attacking player: {PlayerB ID}"
- [ ] Shows "No target found" when out of range

### Player B Console (Victim)
- [ ] Shows "Taking damage! Health: X" for each hit
- [ ] Health values decrease correctly

### Server Console
- [ ] Shows player attack messages
- [ ] Shows health update broadcasts
- [ ] Shows "out of range" warnings for invalid attacks
- [ ] Shows "on cooldown" warnings for rapid attacks

## Issues Found
1. _______________________________________________
2. _______________________________________________
3. _______________________________________________

## Overall Test Result
- [ ] ✅ ALL TESTS PASSED - Feature is complete!
- [ ] ⚠️ SOME TESTS FAILED - See issues above
- [ ] ❌ MAJOR ISSUES - Feature needs fixes

## Sign-off
**Tester:** _______________________________________________
**Date:** _______________________________________________
**Time Spent:** _______________________________________________

## Next Steps
- [ ] Document any bugs found
- [ ] Create GitHub issues for failures
- [ ] Mark task 11 as complete (if all tests pass)
- [ ] Celebrate! 🎉
