# Requirements Document

## Introduction

This feature improves the player character animation system to maintain directional facing when the player stops moving or performs actions like attacking. Currently, the player always faces down (forward) when idle, regardless of their last movement direction. This enhancement will make the character face the direction they were last traveling, creating more natural and contextually appropriate animations.

## Glossary

- **Player Character**: The controllable avatar in the game represented by the Player class
- **Remote Player Character**: A non-controllable avatar representing other players in multiplayer, represented by the RemotePlayer class
- **Idle State**: The state when the player is not moving (isMoving = false)
- **Facing Direction**: The visual orientation of the player character (UP, DOWN, LEFT, RIGHT)
- **Animation Frame**: A single image from the sprite sheet used to render the player
- **Last Movement Direction**: The most recent direction the player was traveling before stopping

## Requirements

### Requirement 1

**User Story:** As a player, I want my character to face the direction I was last moving when I stop, so that the character's orientation feels natural and contextual to my actions

#### Acceptance Criteria

1. WHEN the Player Character stops moving after traveling LEFT, THE Player Character SHALL display the first frame of the LEFT animation
2. WHEN the Player Character stops moving after traveling RIGHT, THE Player Character SHALL display the first frame of the RIGHT animation
3. WHEN the Player Character stops moving after traveling UP, THE Player Character SHALL display the first frame of the UP animation
4. WHEN the Player Character stops moving after traveling DOWN, THE Player Character SHALL display the first frame of the DOWN animation
5. WHILE the Player Character is in Idle State, THE Player Character SHALL maintain the Facing Direction from the Last Movement Direction

### Requirement 2

**User Story:** As a player, I want my character to face the appropriate direction when attacking objects, so that it looks like I'm actually facing what I'm attacking

#### Acceptance Criteria

1. WHEN the Player Character attacks a tree located to their LEFT after approaching from the RIGHT, THE Player Character SHALL face LEFT during the attack
2. WHEN the Player Character attacks a tree located to their RIGHT after approaching from the LEFT, THE Player Character SHALL face RIGHT during the attack
3. WHEN the Player Character performs an attack action, THE Player Character SHALL maintain the current Facing Direction
4. WHILE the Player Character is attacking, THE Player Character SHALL display the first frame of the animation corresponding to their Facing Direction

### Requirement 3

**User Story:** As a player, I want the character's facing direction to persist across different actions, so that the visual presentation remains consistent and believable

#### Acceptance Criteria

1. THE Player Character SHALL store the Last Movement Direction as a persistent state variable
2. WHEN the Player Character transitions from moving to Idle State, THE Player Character SHALL preserve the Facing Direction
3. WHEN the Player Character performs any action in Idle State, THE Player Character SHALL use the stored Facing Direction for visual rendering
4. THE Player Character SHALL update the stored Facing Direction only when actively moving in a new direction

### Requirement 4

**User Story:** As a player in multiplayer mode, I want to see other players facing the correct direction when they stop moving, so that their actions and positions make visual sense

#### Acceptance Criteria

1. WHEN a Remote Player Character stops moving after traveling LEFT, THE Remote Player Character SHALL display the first frame of the LEFT animation
2. WHEN a Remote Player Character stops moving after traveling RIGHT, THE Remote Player Character SHALL display the first frame of the RIGHT animation
3. WHEN a Remote Player Character stops moving after traveling UP, THE Remote Player Character SHALL display the first frame of the UP animation
4. WHEN a Remote Player Character stops moving after traveling DOWN, THE Remote Player Character SHALL display the first frame of the DOWN animation
5. WHILE a Remote Player Character is in Idle State, THE Remote Player Character SHALL maintain the Facing Direction from the Last Movement Direction
