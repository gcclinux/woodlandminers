# Implementation Plan

- [x] 1. Update Player class to use directional idle frames





- [x] 1.1 Replace single idle frame with directional idle frames in Player class


  - Remove the single `idleFrame` field from the Player class
  - Add four new fields: `idleUpFrame`, `idleDownFrame`, `idleLeftFrame`, `idleRightFrame`
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 3.1_

- [x] 1.2 Update Player.loadAnimations() to create directional idle frames


  - Extract the first frame from each directional animation (UP, DOWN, LEFT, RIGHT)
  - Assign each extracted frame to the corresponding idle frame field
  - Use the correct sprite sheet coordinates for each direction's first frame
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 3.1_

- [x] 1.3 Modify Player.getCurrentFrame() to use directional idle frames


  - Implement switch statement on `currentDirection` for idle state
  - Return the appropriate idle frame based on current direction
  - Add default case returning idleDownFrame as fallback
  - Keep existing logic for returning animated frames when moving
  - _Requirements: 1.5, 2.3, 2.4, 3.2, 3.3_

- [x] 2. Update RemotePlayer class to use directional idle frames




- [x] 2.1 Replace single idle frame with directional idle frames in RemotePlayer class


  - Remove the single `idleFrame` field from the RemotePlayer class
  - Add four new fields: `idleUpFrame`, `idleDownFrame`, `idleLeftFrame`, `idleRightFrame`
  - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [x] 2.2 Update RemotePlayer.loadAnimations() to create directional idle frames


  - Extract the first frame from each directional animation (UP, DOWN, LEFT, RIGHT)
  - Assign each extracted frame to the corresponding idle frame field
  - Use the correct sprite sheet coordinates for each direction's first frame
  - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [x] 2.3 Modify RemotePlayer.getCurrentFrame() to use directional idle frames


  - Implement switch statement on `currentDirection` for idle state
  - Return the appropriate idle frame based on current direction
  - Add default case returning idleDownFrame as fallback
  - Keep existing logic for returning animated frames when moving
  - _Requirements: 4.5_

- [x] 3. Manual testing and verification





  - Test local player facing after stopping in each direction (UP, DOWN, LEFT, RIGHT)
  - Test local player facing when attacking trees from different approach directions
  - Test remote player facing in multiplayer mode
  - Verify direction persistence across different actions
  - Test diagonal movement scenarios for both local and remote players
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.1, 2.2, 2.3, 2.4, 3.2, 3.3, 3.4, 4.1, 4.2, 4.3, 4.4, 4.5_
