# Requirements Document

## Introduction

This feature enables player-vs-player (PvP) combat in multiplayer mode, allowing players to damage each other using the same attack mechanism currently used for damaging trees. Players will be able to attack other players within range using the spacebar, dealing damage similar to how cactus currently damages players. This creates an interactive multiplayer experience where players can engage in combat while maintaining the existing game mechanics.

## Glossary

- **Local Player**: The player controlled by the current game client instance
- **Remote Player**: A player controlled by another game client connected to the same server
- **Attack Range**: The distance within which a player can successfully damage another entity (currently 64px horizontal, 96px vertical from center)
- **Game Client**: The client-side application instance that connects to the Game Server
- **Game Server**: The authoritative server that manages game state and validates all player actions
- **Player State**: The current status of a player including position, health, and movement direction
- **Attack Action**: A player-initiated action to deal damage to a target entity
- **Damage Cooldown**: A time period that must elapse before the same player can be damaged again by the same attacker

## Requirements

### Requirement 1

**User Story:** As a player in multiplayer mode, I want to attack other players using the spacebar, so that I can engage in player-vs-player combat

#### Acceptance Criteria

1. WHEN the Local Player presses the spacebar AND a Remote Player is within Attack Range, THE Game Client SHALL send an Attack Action message to the Game Server targeting the Remote Player
2. WHEN the Game Server receives an Attack Action message targeting a player, THE Game Server SHALL validate the attack range and apply 10 damage to the target player's health
3. WHEN a Remote Player's health is reduced by an attack, THE Game Server SHALL broadcast a health update message to all connected Game Clients
4. WHERE the Local Player attacks AND no Remote Player is within Attack Range, THE Game Client SHALL prioritize tree attacks over player attacks
5. WHEN the Local Player presses spacebar, THE Game Client SHALL check for Remote Players within Attack Range before checking for trees within Attack Range

### Requirement 2

**User Story:** As a player being attacked, I want to see my health decrease and display a health bar, so that I know when I'm taking damage from other players

#### Acceptance Criteria

1. WHEN the Local Player receives damage from another player, THE Game Client SHALL update the Local Player's health value immediately
2. WHEN the Local Player's health is below 100, THE Game Client SHALL render a health bar above the Local Player's sprite
3. WHEN a Remote Player's health is below 100, THE Game Client SHALL render a health bar above the Remote Player's sprite
4. WHEN the Local Player's health reaches 0 or below, THE Game Client SHALL trigger the respawn mechanism
5. WHEN the Local Player respawns after death, THE Game Client SHALL reset the Local Player's health to 100 and position to a safe spawn location

### Requirement 3

**User Story:** As a player, I want the server to validate all player attacks, so that the game remains fair and prevents cheating

#### Acceptance Criteria

1. WHEN the Game Server receives an Attack Action message, THE Game Server SHALL verify the attacker and target are within valid Attack Range before applying damage
2. WHEN the Game Server validates an attack, THE Game Server SHALL calculate the distance between attacker and target using their current server-side positions
3. IF the attacker and target are not within Attack Range, THEN THE Game Server SHALL ignore the Attack Action message
4. WHEN the Game Server applies damage to a player, THE Game Server SHALL update the target player's health in the authoritative game state
5. WHEN the Game Server updates a player's health, THE Game Server SHALL broadcast the updated health to all connected Game Clients

### Requirement 4

**User Story:** As a player, I want a brief cooldown between attacks on the same target, so that combat feels balanced and not spammy

#### Acceptance Criteria

1. WHEN a player attacks another player, THE Game Server SHALL record the timestamp of the attack for that attacker-target pair
2. WHEN the Game Server receives an Attack Action message, THE Game Server SHALL check if 0.5 seconds have elapsed since the last attack from the same attacker to the same target
3. IF less than 0.5 seconds have elapsed since the last attack, THEN THE Game Server SHALL ignore the Attack Action message
4. WHEN 0.5 seconds have elapsed since the last attack, THE Game Server SHALL allow the attack and update the timestamp
5. WHERE different players attack the same target, THE Game Server SHALL track Damage Cooldown independently for each attacker-target pair

### Requirement 5

**User Story:** As a player, I want visual feedback when I attack another player, so that I know my attack registered

#### Acceptance Criteria

1. WHEN the Local Player successfully attacks a Remote Player, THE Game Client SHALL display a console log message indicating the attack
2. WHEN the Local Player attacks but no valid target is in range, THE Game Client SHALL display a console log message indicating no target found
3. WHEN a Remote Player's health bar is visible, THE Game Client SHALL render the health bar with green color for current health and red color for missing health
4. WHEN the Local Player receives damage, THE Game Client SHALL log the damage amount and current health to the console
5. WHEN the Local Player's health changes, THE Game Client SHALL update the health bar rendering on the next frame
