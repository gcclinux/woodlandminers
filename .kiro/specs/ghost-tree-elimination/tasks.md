# Implementation Plan

- [x] 1. Create TreeRemovalMessage network message class
  - Create new message class that extends NetworkMessage
  - Add treeId field (String) for the position key
  - Add reason field (String) for diagnostic logging
  - Implement constructor, getters, and Serializable interface
  - _Requirements: 1.5, 2.4, 3.4_

- [x] 2. Update MessageType enum with TREE_REMOVAL
  - Add TREE_REMOVAL to the MessageType enum
  - _Requirements: 1.5_

- [x] 3. Implement ghost tree detection and removal on server
  - [x] 3.1 Modify ClientConnection.handleAttackAction() to detect ghost trees
    - Remove the dynamic tree creation logic (lines ~400-470)
    - When tree is null, send TreeRemovalMessage instead of creating tree
    - Add diagnostic logging for ghost tree detection
    - _Requirements: 2.3, 2.4, 2.5, 3.4_
  
  - [x] 3.2 Add ghost tree tracking metrics
    - Add ghostTreeAttempts map to track repeated attempts
    - Implement logGhostTreeAttempt() method
    - Add threshold check for excessive ghost tree attacks (security)
    - _Requirements: 2.4, 3.4_

- [x] 4. Implement client-side world clearing for multiplayer transition
  - [x] 4.1 Create clearLocalWorld() method in MyGdxGame
    - Dispose and clear all tree maps (trees, appleTrees, coconutTrees, bambooTrees, bananaTrees)
    - Dispose and clear all item maps (apples, bananas)
    - Clear clearedPositions map
    - Add error handling with try-catch
    - Add logging for diagnostics
    - _Requirements: 1.2, 1.4, 5.1, 5.2, 5.3_
  
  - [x] 4.2 Call clearLocalWorld() in startMultiplayerHost()
    - Add call before creating game server
    - _Requirements: 1.2, 5.1, 5.2_
  
  - [x] 4.3 Call clearLocalWorld() in joinMultiplayerServer()
    - Add call before connecting to server
    - _Requirements: 1.2, 5.1, 5.2_

- [x] 5. Enhance world state synchronization logging
  - [x] 5.1 Add diagnostic logging to syncWorldState()
    - Log world seed received
    - Log number of trees, players, items to sync
    - Log local tree count after sync
    - _Requirements: 3.1, 3.2, 3.5_
  
  - [x] 5.2 Create getTotalTreeCount() helper method
    - Sum all tree map sizes
    - _Requirements: 3.2_

- [x] 6. Implement client-side ghost tree removal handling
  - [x] 6.1 Add handleTreeRemoval() to DefaultMessageHandler
    - Create method stub with default logging
    - Update message routing in handleMessage() switch statement
    - _Requirements: 4.1, 4.2, 4.3_
  
  - [x] 6.2 Override handleTreeRemoval() in GameMessageHandler
    - Log ghost tree removal with reason
    - Call game.removeTree() to remove the ghost tree
    - _Requirements: 4.1, 4.2, 4.3, 4.4_
  
  - [x] 6.3 Enhance removeTree() to return boolean
    - Modify removeTreeImmediate() to return true if tree was found
    - Log when attempting to remove non-existent tree
    - _Requirements: 4.3, 4.4_

- [x] 7. Add comprehensive logging for diagnostics
  - [x] 7.1 Add connection logging in MyGdxGame
    - Log when clearLocalWorld() is called
    - Log duration of world clearing operation
    - _Requirements: 3.1, 3.2_
  
  - [x] 7.2 Add server-side ghost tree logging
    - Log each ghost tree attack with client ID and position
    - Log when TreeRemovalMessage is sent
    - _Requirements: 2.4, 3.4_
  
  - [x] 7.3 Add client-side tree sync logging
    - Log number of trees received in WorldStateMessage
    - Log number of trees created locally
    - Log ghost tree removals
    - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [x] 8. Wire up TreeRemovalMessage in network message handling
  - Ensure TreeRemovalMessage is properly serialized/deserialized
  - Verify message routing works end-to-end
  - Test that clients receive and process the message
  - _Requirements: 1.5, 4.1, 4.2, 4.3_
