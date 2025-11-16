# Implementation Plan

- [x] 1. Enhance ServerConfig to load and validate planting range configuration
  - Add constants for default (512), minimum (64), and maximum (1024) planting range values
  - Add plantingMaxRange field to store the configured range
  - Implement loadFromProperties() method to read "planting.max.range" from server.properties
  - Implement validation logic to ensure range is between 64 and 1024, falling back to 512 for invalid values
  - Implement logging for the active planting range in both pixels and tiles
  - Add getPlantingMaxRange() accessor method
  - _Requirements: 1.1, 1.2, 1.3, 1.5, 6.1, 6.2, 6.3, 6.4, 7.1_

- [x] 2. Add planting range property to server.properties configuration file
  - Add "planting.max.range=512" property with descriptive comments
  - Include documentation about valid range (64-1024) and tile conversion
  - _Requirements: 1.1_

- [x] 3. Update GameServer to initialize and use ServerConfig
  - Ensure ServerConfig is instantiated and loaded during server startup
  - Verify ServerConfig.loadFromProperties() is called with server.properties
  - Add logging to confirm configuration loaded successfully
  - _Requirements: 1.1, 1.2, 1.5_

- [x] 4. Extend ConnectionAcceptedMessage to include planting range
  - Add plantingMaxRange field (int) to the message class
  - Update constructor to accept plantingMaxRange parameter
  - Add getPlantingMaxRange() and setPlantingMaxRange() methods
  - Update message serialization to include the new field
  - Update message deserialization to read the new field
  - Ensure backward compatibility with existing message structure
  - _Requirements: 3.1, 5.1, 5.2, 5.4_

- [x] 5. Update GameServer to send planting range in connection accepted message
  - Modify connection acceptance logic to retrieve planting range from ServerConfig
  - Pass plantingMaxRange to ConnectionAcceptedMessage constructor
  - Verify the range value is transmitted to connecting clients
  - _Requirements: 3.1, 5.2_

- [x] 6. Replace hardcoded range validation in ClientConnection with configured value
  - Locate the hardcoded 512 value in handleBambooPlant() method (around line 1069)
  - Replace with call to server.getConfig().getPlantingMaxRange()
  - Update distance validation logic to use the configured range
  - Enhance logging to include both attempted distance and maximum allowed distance
  - Ensure security violation logging includes client ID and distance values
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 7.2_

- [x] 7. Update GameClient to receive and store planting range from server
  - Add plantingMaxRange field initialized to -1 (unlimited/not connected)
  - Update handleConnectionAccepted() to extract plantingMaxRange from ConnectionAcceptedMessage
  - Store the received range value for the session duration
  - Add logging to display received range in pixels and tiles
  - Add getPlantingMaxRange() accessor method
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 7.3_

- [x] 8. Enhance TargetingSystem to enforce maximum planting range
  - Add maxRange field initialized to -1 (unlimited)
  - Add playerX and playerY fields to track player position
  - Implement setMaxRange() method to configure the maximum range
  - Update moveTarget() to calculate Euclidean distance for proposed target position
  - Implement range clamping logic when distance exceeds maxRange
  - Implement snapToTileGrid() helper method for grid alignment
  - Add isWithinMaxRange() validation method
  - Add logging when target position is clamped to range boundary
  - Handle unlimited range case (maxRange = -1) by skipping enforcement
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 7.4_

- [x] 9. Wire GameClient to configure TargetingSystem with received range
  - Update handleConnectionAccepted() in GameClient to notify MyGdxGame of the range
  - Implement setPlantingMaxRange() method in MyGdxGame
  - In setPlantingMaxRange(), retrieve Player's TargetingSystem and call setMaxRange()
  - Add null checks for player and targetingSystem
  - Add logging to confirm targeting system configuration
  - _Requirements: 3.2, 3.4_

- [x] 10. Ensure single-player mode compatibility
  - Verify TargetingSystem defaults to unlimited range (-1) when not connected to server
  - Test that single-player mode without server connection allows unlimited targeting
  - Test that single-player mode with local server uses the configured range
  - Verify planting works correctly in both scenarios
  - _Requirements: 8.1, 8.2, 8.3_

- [x] 11. Create unit tests for ServerConfig range validation
  - Test loading valid range values (64, 512, 1024)
  - Test loading below minimum value (expect fallback to 512)
  - Test loading above maximum value (expect fallback to 512)
  - Test loading non-numeric value (expect fallback to 512)
  - Test missing property (expect fallback to 512)
  - Test logging output for each scenario
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 6.1, 6.2, 6.3, 6.4_

- [x] 12. Create unit tests for TargetingSystem range enforcement
  - Test cursor movement within range (should succeed)
  - Test cursor movement beyond range (should clamp)
  - Test clamping accuracy (clamped position on boundary)
  - Test tile grid snapping after clamping
  - Test unlimited range mode (maxRange = -1)
  - Test diagonal movement clamping
  - Test isWithinMaxRange() validation method
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [x] 13. Create integration tests for client-server range synchronization
  - Test server sends range to client on connection
  - Test client configures targeting system with received range
  - Test multiple clients receive same range configuration
  - Test planting within configured range succeeds
  - Test planting at exact max range succeeds
  - Test server rejects planting beyond configured range
  - Test targeting cursor cannot move beyond configured range
  - _Requirements: 2.1, 2.2, 2.3, 3.1, 3.2, 3.3, 4.1, 4.2_

- [x] 14. Create integration tests for configuration loading and validation
  - Test server startup with valid configuration
  - Test server startup with missing configuration (uses default)
  - Test server startup with invalid configuration (uses default)
  - Test configuration values are correctly applied to validation logic
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 6.1, 6.2, 6.3, 6.4_
