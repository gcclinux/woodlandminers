# Implementation Plan

- [x] 1. Set up network package structure and core message protocol





  - Create `wagemaker.uk.network` package directory
  - Implement `NetworkMessage` abstract base class with serialization support
  - Implement all message subclasses (PlayerMovementMessage, WorldStateMessage, etc.)
  - Create `MessageType` enum for message identification
  - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1, 6.1_

- [x] 2. Implement world state management system





- [x] 2.1 Create WorldState and related state classes


  - Implement `WorldState` class with serialization
  - Implement `PlayerState` class with player data fields
  - Implement `TreeState` class with tree data fields
  - Implement `ItemState` class with item data fields
  - Add methods for creating snapshots and calculating deltas
  - _Requirements: 4.1, 4.2, 4.3_

- [x] 2.2 Implement world state synchronization logic


  - Add `getDeltaSince()` method to calculate state changes
  - Add `applyUpdate()` method to merge state updates
  - Add `createSnapshot()` method for full state copies
  - Implement timestamp tracking for delta calculations
  - _Requirements: 4.2, 4.3, 4.4_

- [x] 3. Implement GameServer core functionality




- [x] 3.1 Create GameServer class with connection handling


  - Implement `GameServer` class with ServerSocket initialization
  - Add `start()` method to bind to port and accept connections
  - Add `stop()` method for graceful shutdown
  - Implement client acceptance loop with thread pool
  - Add `getPublicIPv4()` method to retrieve server IP address
  - _Requirements: 1.3, 1.4, 7.1, 7.2, 9.1, 9.5_

- [x] 3.2 Implement ClientConnection management


  - Create `ClientConnection` class for individual client handling
  - Implement message receiving loop for each client
  - Add message sending method with output stream
  - Implement heartbeat tracking and timeout detection
  - Add connection cleanup and disposal logic
  - _Requirements: 7.3, 11.1, 11.3, 12.2_

- [x] 3.3 Add server-side game state management


  - Initialize `WorldState` with random seed generation
  - Implement player state tracking in WorldState
  - Add tree state management (health, destruction)
  - Add item state management (spawning, collection)
  - Implement cleared positions tracking
  - _Requirements: 4.1, 4.4, 5.1, 6.2_

- [x] 3.4 Implement server broadcasting logic


  - Add `broadcastToAll()` method for sending to all clients
  - Add `broadcastToAllExcept()` method for excluding sender
  - Implement message queuing for reliable delivery
  - Add error handling for failed broadcasts
  - _Requirements: 3.2, 4.4, 5.2, 6.3, 11.1, 11.3_

- [x] 4. Implement GameClient core functionality




- [x] 4.1 Create GameClient class with connection logic


  - Implement `GameClient` class with Socket connection
  - Add `connect()` method to establish server connection
  - Add `disconnect()` method for clean disconnection
  - Implement message receiving thread
  - Add `isConnected()` status check method
  - _Requirements: 2.2, 2.3, 2.4_

- [x] 4.2 Implement client-side message handling


  - Create `MessageHandler` interface for processing messages
  - Implement handlers for each message type
  - Add `handleMessage()` dispatcher method
  - Implement world state update application
  - Add error handling for malformed messages
  - _Requirements: 3.3, 4.3, 5.3, 6.4, 10.2, 11.2, 11.4_

- [x] 4.3 Add client-side action sending


  - Implement `sendPlayerMovement()` for position updates
  - Implement `sendAttackAction()` for tree attacks
  - Implement `sendItemPickup()` for item collection
  - Add message throttling (20 updates/second)
  - Implement client-side prediction for local player
  - _Requirements: 3.1, 6.1, 5.4, 12.1_

- [x] 5. Implement RemotePlayer rendering system




- [x] 5.1 Create RemotePlayer class


  - Implement `RemotePlayer` class with player state fields
  - Load player sprite sheet and create animations
  - Add `updatePosition()` method for state updates
  - Add `updateHealth()` method for health changes
  - Implement animation state management
  - _Requirements: 3.3, 3.4, 3.5, 10.3_

- [x] 5.2 Add RemotePlayer rendering methods


  - Implement `render()` method to draw player sprite
  - Implement `renderHealthBar()` for health visualization
  - Implement `renderNameTag()` for player name display
  - Add position interpolation for smooth movement
  - Implement animation frame selection based on direction
  - _Requirements: 3.3, 3.4, 10.3, 10.4, 10.5_

- [x] 6. Integrate multiplayer into MyGdxGame




- [x] 6.1 Add multiplayer mode support to MyGdxGame


  - Add `GameMode` enum (SINGLEPLAYER, MULTIPLAYER_HOST, MULTIPLAYER_CLIENT)
  - Add `gameMode` field to track current mode
  - Add `gameServer` field for host mode
  - Add `gameClient` field for multiplayer modes
  - Add `remotePlayers` map to track other players
  - _Requirements: 1.3, 1.5, 2.2_

- [x] 6.2 Implement multiplayer initialization methods


  - Add `startMultiplayerHost()` method to launch server and connect
  - Add `joinMultiplayerServer()` method to connect as client
  - Add `syncWorldState()` method to apply server state
  - Modify `create()` to support multiplayer initialization
  - Add cleanup in `dispose()` for multiplayer resources
  - _Requirements: 1.3, 1.4, 1.5, 2.2, 2.3, 4.2_

- [x] 6.3 Add remote player rendering to game loop


  - Add `renderRemotePlayers()` method to draw all remote players
  - Integrate remote player rendering into main render loop
  - Ensure remote players render at correct z-order
  - Add remote player health bars to health bar rendering
  - Add remote player name tags to rendering
  - _Requirements: 3.3, 3.4, 10.3, 10.4_

- [x] 6.4 Implement world state synchronization in game loop


  - Apply received world state updates in render loop
  - Update tree states from server messages
  - Update item states from server messages
  - Handle tree destruction messages
  - Handle item spawn messages
  - _Requirements: 4.3, 4.4, 4.5, 5.3, 6.4, 6.5_

- [x] 7. Modify Player class for multiplayer




- [x] 7.1 Add multiplayer fields to Player class


  - Add `playerId` field for unique identification
  - Add `gameClient` reference field
  - Add `isLocalPlayer` flag to distinguish local vs remote
  - _Requirements: 3.1, 3.2_

- [x] 7.2 Integrate network updates into Player movement


  - Modify `update()` to send position updates when connected
  - Add check for `gameClient` connection before sending
  - Send movement messages with position and direction
  - Implement client-side prediction for local movement
  - _Requirements: 3.1, 12.1_

- [x] 7.3 Integrate network updates into Player actions


  - Modify `attackNearbyTrees()` to send attack messages
  - Modify `pickupApple()` to send pickup requests
  - Modify `pickupBanana()` to send pickup requests
  - Add server response handling for action validation
  - _Requirements: 6.1, 5.4_

- [x] 8. Create multiplayer UI components





- [x] 8.1 Implement MultiplayerMenu class


  - Create `MultiplayerMenu` class with wooden plank background
  - Load slkscr.ttf font for menu text
  - Implement menu options array ["Host Server", "Connect to Server", "Back"]
  - Add keyboard navigation (up/down arrows, enter)
  - Implement yellow highlight for selected option
  - Add `render()` method with wooden plank style
  - _Requirements: 1.1, 1.2, 8.1, 8.2, 8.3_

- [x] 8.2 Implement ServerHostDialog class


  - Create `ServerHostDialog` class for displaying server IP
  - Add wooden plank background rendering
  - Display server IPv4 address in center
  - Add "Press ESC to close" instruction
  - Implement keyboard input handling
  - _Requirements: 1.4, 8.1, 8.4, 8.5_

- [x] 8.3 Implement ConnectDialog class


  - Create `ConnectDialog` class for IP input
  - Add wooden plank background rendering
  - Implement text input field for IP address
  - Add input validation for IP format
  - Add "Enter to connect, ESC to cancel" instructions
  - Handle keyboard input for text entry
  - _Requirements: 2.1, 8.1, 8.4_

- [x] 8.4 Integrate multiplayer menu into GameMenu


  - Add "Multiplayer" option to main game menu
  - Add `multiplayerMenu` field to GameMenu class
  - Implement menu navigation to multiplayer submenu
  - Handle back navigation from multiplayer menu
  - Update menu rendering to show multiplayer option
  - _Requirements: 1.1, 1.2_

- [x] 9. Implement server-side validation and security




- [x] 9.1 Add input validation to GameServer


  - Implement position validation (max speed check)
  - Implement attack range validation
  - Implement item pickup distance validation
  - Add message size validation (max 64KB)
  - Reject malformed or invalid messages
  - _Requirements: 6.2, 12.2, 12.3_

- [x] 9.2 Implement rate limiting and DoS protection


  - Add message rate counter per client (100 msg/sec limit)
  - Implement maximum client limit (default 20)
  - Add connection timeout logic (15 seconds)
  - Disconnect clients exceeding rate limits
  - Log security violations
  - _Requirements: 9.3_

- [x] 10. Implement dedicated server launcher





- [x] 10.1 Create DedicatedServerLauncher class


  - Create `wagemaker.uk.server` package
  - Implement `DedicatedServerLauncher` main class
  - Add command-line argument parsing for port
  - Initialize and start GameServer
  - Display server IP and port in console
  - Add shutdown hook for graceful termination
  - _Requirements: 7.1, 7.2, 7.4, 7.5_

- [x] 10.2 Add server configuration file support


  - Create `server.properties` file format
  - Implement configuration file parser
  - Add properties for port, max clients, world seed
  - Add properties for heartbeat and timeout intervals
  - Load configuration on server startup
  - _Requirements: 9.5_

- [x] 10.3 Add server logging and monitoring


  - Implement connection logging (join/leave events)
  - Add error logging for network issues
  - Log world state changes (tree destruction, item spawns)
  - Add periodic status logging (connected clients, uptime)
  - _Requirements: 7.4_

- [x] 11. Implement player join/leave notifications





- [x] 11.1 Add join notification system


  - Create `PlayerJoinMessage` class
  - Send join message when client connects
  - Broadcast join notification to all existing clients
  - Display join message in game UI
  - Add new RemotePlayer to client's player map
  - _Requirements: 11.1, 11.2_


- [x] 11.2 Add leave notification system

  - Create `PlayerLeaveMessage` class
  - Send leave message when client disconnects
  - Broadcast leave notification to remaining clients
  - Display leave message in game UI
  - Remove RemotePlayer from client's player map
  - _Requirements: 11.3, 11.4, 11.5_

- [x] 12. Implement network error handling and recovery





- [x] 12.1 Add connection error handling


  - Implement try-catch blocks for connection failures
  - Display error dialog on connection failure
  - Add retry option in error dialog
  - Return to multiplayer menu on failure
  - Log connection errors for debugging
  - _Requirements: 2.4_

- [x] 12.2 Implement heartbeat and timeout system


  - Add heartbeat message sending every 5 seconds
  - Track last heartbeat time on server
  - Disconnect clients with >15 second timeout
  - Display connection lost message to client
  - Attempt automatic reconnection (3 attempts)
  - _Requirements: 12.5_

- [x] 12.3 Add position correction system


  - Implement position validation on server
  - Send correction message for invalid positions
  - Implement smooth interpolation to corrected position
  - Log desynchronization events
  - _Requirements: 12.2, 12.3, 12.4_

- [x] 13. Add Gradle build configuration for server




- [x] 13.1 Create server JAR build task


  - Add `serverJar` task to build.gradle
  - Configure main class as DedicatedServerLauncher
  - Include all dependencies in server JAR
  - Exclude LibGDX rendering dependencies
  - Test server JAR execution
  - _Requirements: 7.1_

- [x] 14. Implement deterministic world generation




- [x] 14.1 Add world seed synchronization


  - Generate random world seed on server startup
  - Include world seed in WorldStateMessage
  - Apply world seed to client's Random instance
  - Ensure tree generation uses seed-based positioning
  - Verify identical world generation across clients
  - _Requirements: 4.1, 4.2_

- [x] 15. Add connection quality indicators




- [x] 15.1 Implement latency measurement


  - Add ping message type for latency testing
  - Send ping every 2 seconds
  - Calculate round-trip time from pong response
  - Store average latency over last 10 pings
  - _Requirements: 12.5_

- [x] 15.2 Display connection quality UI


  - Add connection quality indicator to HUD
  - Display latency in milliseconds
  - Show green/yellow/red indicator based on latency
  - Display "Connection Lost" when disconnected
  - _Requirements: 12.5_

- [x] 16. Implement health synchronization





- [x] 16.1 Add player health broadcasting


  - Send health update when local player health changes
  - Broadcast health updates to all clients
  - Update RemotePlayer health on receiving update
  - _Requirements: 10.1, 10.2_

- [x] 16.2 Add remote player health bar rendering


  - Render health bar above remote players when health < 100
  - Use same health bar style as local player
  - Update health bar in real-time
  - _Requirements: 10.3, 10.4, 10.5_

- [x] 17. Add item synchronization





- [x] 17.1 Implement item spawn synchronization


  - Send item spawn message when tree is destroyed
  - Broadcast spawn to all clients
  - Create item in client's item map
  - Render spawned items
  - _Requirements: 5.1, 5.2, 5.3_

- [x] 17.2 Implement item pickup synchronization


  - Send pickup request to server
  - Validate pickup distance on server
  - Broadcast pickup confirmation to all clients
  - Remove item from all clients
  - Apply health restoration to picking player
  - _Requirements: 5.4, 5.5_

- [x] 18. Implement tree destruction synchronization




- [x] 18.1 Add tree attack synchronization


  - Send attack action to server
  - Validate attack range on server
  - Update tree health in server WorldState
  - Broadcast health update to all clients
  - Update tree health bar on all clients
  - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [x] 18.2 Add tree destruction broadcasting


  - Detect tree destruction (health <= 0) on server
  - Remove tree from server WorldState
  - Broadcast destruction message to all clients
  - Remove tree from all clients' tree maps
  - Add position to cleared positions
  - _Requirements: 6.5, 4.5_

- [x] 19. Add bandwidth optimization






- [x] 19.1 Implement position update throttling

  - Limit position updates to 20 per second
  - Track last update time per client
  - Skip updates if within throttle interval
  - _Requirements: 3.1_


- [x] 19.2 Implement position quantization

  - Round positions to nearest pixel
  - Reduce floating point precision in messages
  - Minimize message size
  - _Requirements: 3.1_

- [x] 20. Create integration tests





  - Write test for client connection flow
  - Write test for world state synchronization
  - Write test for player movement replication
  - Write test for tree destruction synchronization
  - Write test for item spawn and pickup flow
  - Write test for multiple clients connecting
  - Write test for client disconnection handling
  - _Requirements: All_

- [x] 21. Create performance tests





  - Write load test for 10 concurrent clients
  - Measure message throughput
  - Monitor server memory usage
  - Test bandwidth consumption
  - Simulate various latency conditions
  - _Requirements: All_

- [x] 22. Add server configuration documentation





  - Document server.properties format
  - Document command-line arguments
  - Create server setup guide
  - Document firewall configuration
  - Create troubleshooting guide
  - _Requirements: 7.1, 7.2, 9.5_
