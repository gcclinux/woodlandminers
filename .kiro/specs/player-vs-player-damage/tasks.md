# Implementation Plan

- [x] 1. Add remote player tracking to Player.java

  - Add `remotePlayers` field to store reference to remote players map
  - Add `lastPlayerAttackTime` field for client-side cooldown tracking
  - Add `PLAYER_ATTACK_COOLDOWN` constant (0.5 seconds)
  - Create `setRemotePlayers()` method to inject remote players reference
  - _Requirements: 1.1, 1.4_

- [x] 2. Implement player attack range detection in Player.java

  - Create `findNearestRemotePlayerInRange()` method to search for attackable players
  - Create `isPlayerInAttackRange()` method to check if specific player is within 100 pixels
  - Use Euclidean distance calculation: `sqrt(dx² + dy²)`
  - Return the nearest remote player within attack range, or null if none found
  - _Requirements: 1.1, 1.5_

- [x] 3. Modify attack logic in Player.java to prioritize player targets


  - Rename `attackNearbyTrees()` method to `attackNearbyTargets()`
  - Add cooldown check at start of method (0.5 seconds since last player attack)
  - Call `findNearestRemotePlayerInRange()` before checking for trees
  - If remote player found, send attack message with player ID as target
  - Update `lastPlayerAttackTime` after player attack
  - If no remote player found, execute existing tree attack logic
  - Add console logging for player attacks ("Attacking player: {playerId}")
  - _Requirements: 1.1, 1.4, 1.5, 5.1, 5.2_

- [x] 4. Update MyGdxGame.java to inject remote players into Player

  - Locate where Player instance is created/initialized
  - Call `player.setRemotePlayers(remotePlayers)` to provide reference
  - Ensure this is called after remote players map is initialized
  - _Requirements: 1.1_

- [x] 5. Add player attack cooldown tracking to ClientConnection.java

  - Add `playerAttackCooldowns` field as `Map<String, Long>`
  - Add `PLAYER_ATTACK_COOLDOWN_MS` constant (500 milliseconds)
  - Add `PLAYER_DAMAGE` constant (10.0f)
  - Initialize cooldown map in constructor
  - _Requirements: 4.1, 4.2_

- [x] 6. Implement player attack validation methods in ClientConnection.java

  - Create `getCooldownKey()` method to generate "attackerId:targetId" key
  - Create `isPlayerAttackOnCooldown()` method to check if attack is on cooldown
  - Create `updatePlayerAttackCooldown()` method to record attack timestamp
  - Use `System.currentTimeMillis()` for timestamp tracking
  - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [x] 7. Implement player attack handling in ClientConnection.java

  - Create `handlePlayerAttack()` method to process player-vs-player attacks
  - Validate target player exists in world state
  - Check cooldown using `isPlayerAttackOnCooldown()`
  - Calculate distance between attacker and target positions
  - Validate distance is within ATTACK_RANGE (100 pixels)
  - Apply 10 damage to target player's health
  - Clamp health to minimum of 0
  - Update target player health in world state
  - Update cooldown timestamp
  - Broadcast `PlayerHealthUpdateMessage` to all clients
  - Log security violations for invalid attacks (out of range, on cooldown)
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 4.3, 4.4, 5.4_

- [x] 8. Modify handleAttackAction in ClientConnection.java to route player attacks


  - Add check at start of `handleAttackAction()` to determine if target is a player
  - Check if target ID exists in `server.getWorldState().getPlayers()`
  - If target is a player, call `handlePlayerAttack()` and return
  - If target is not a player, continue with existing tree attack logic
  - _Requirements: 3.1_

- [x] 9. Update GameMessageHandler to process player health updates for local player

  - Locate where `PlayerHealthUpdateMessage` is handled
  - Check if the health update is for the local player (compare player IDs)
  - If local player, update local player health using `player.setHealth()`
  - Add console logging for local player damage ("Taking damage! Health: {health}")
  - _Requirements: 2.1, 2.4, 5.4_

- [x] 10. Verify health bar rendering for local and remote players

  - Confirm `Player.shouldShowHealthBar()` returns true when health < 100
  - Confirm `RemotePlayer.renderHealthBar()` is called in render loop
  - Confirm health bar colors: green for current health, red for missing health
  - No code changes needed if existing implementation is correct
  - _Requirements: 2.2, 2.3, 5.3, 5.5_

- [x] 11. Test player-vs-player combat in multiplayer


  - Start dedicated server
  - Connect two game clients
  - Position players within 100 pixels of each other
  - Press spacebar on Player A and verify Player B health decreases
  - Verify health bar appears above Player B
  - Verify console logs show attack messages
  - Continue attacking until Player B health reaches 0
  - Verify Player B respawns with full health
  - Test attack cooldown by rapidly pressing spacebar
  - Test attack range by moving players apart
  - Test attack priority by positioning player near both tree and player
  - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.2, 2.3, 2.4, 2.5, 3.1, 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3, 4.4, 5.1, 5.2, 5.3, 5.4, 5.5_
