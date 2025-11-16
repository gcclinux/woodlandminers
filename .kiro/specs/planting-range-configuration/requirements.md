# Requirements Document

## Introduction

This document specifies the requirements for a Configurable Planting Range System that synchronizes client-side targeting range with server-side validation range. The system ensures players cannot target locations that the server will reject, improving user experience and preventing wasted planting attempts. The system provides a centralized configuration mechanism through server.properties, enabling server operators to control planting range without code changes.

## Glossary

- **Planting System**: The game subsystem that handles placement of plantable items (e.g., baby bamboo) in the game world
- **Targeting System**: The client-side subsystem that manages the visual cursor indicating where a player intends to plant
- **Server Config**: The server-side configuration management component that loads and validates settings from server.properties
- **Client Connection**: The server-side component that handles individual client connections and validates client actions
- **Game Client**: The client-side component that manages connection to the game server
- **Euclidean Distance**: The straight-line distance between two points calculated as √(dx² + dy²)
- **Tile**: A 64x64 pixel grid unit in the game world
- **Max Range**: The maximum distance in pixels from the player position where planting is allowed
- **Range Clamping**: The process of constraining a target position to remain within the maximum allowed distance

## Requirements

### Requirement 1: Server Configuration Management

**User Story:** As a server operator, I want to configure the maximum planting range through server.properties, so that I can control gameplay mechanics without modifying code.

#### Acceptance Criteria

1. WHEN the Server Config loads configuration files, THE Server Config SHALL read the property "planting.max.range" from server.properties
2. WHERE the property "planting.max.range" has a numeric value between 64 and 1024 inclusive, THE Server Config SHALL store the value as the active planting range
3. IF the property "planting.max.range" has a value less than 64 or greater than 1024, THEN THE Server Config SHALL set the active planting range to 512 and log a warning message
4. IF the property "planting.max.range" is not numeric or is missing, THEN THE Server Config SHALL set the active planting range to 512 and log a warning message
5. WHEN the Server Config completes loading, THE Server Config SHALL log the active planting range value in pixels and tiles

### Requirement 2: Server-Side Range Validation

**User Story:** As a server operator, I want the server to validate planting attempts using the configured range, so that players cannot plant beyond the allowed distance.

#### Acceptance Criteria

1. WHEN the Client Connection receives a planting request, THE Client Connection SHALL calculate the Euclidean Distance between the player position and the target position
2. IF the calculated distance exceeds the configured Max Range, THEN THE Client Connection SHALL reject the planting request and log a security violation
3. WHILE the calculated distance is less than or equal to the configured Max Range, THE Client Connection SHALL proceed with planting validation
4. THE Client Connection SHALL use the Max Range value from Server Config instead of hardcoded values

### Requirement 3: Client-Server Range Synchronization

**User Story:** As a player, I want my client to know the server's planting range limits, so that my targeting cursor accurately reflects what the server will accept.

#### Acceptance Criteria

1. WHEN the Game Client establishes a connection to the server, THE Game Client SHALL receive the Max Range value from the server
2. WHEN the Game Client receives the Max Range value, THE Game Client SHALL configure the Targeting System with the received Max Range
3. THE Game Client SHALL log the received Max Range value in pixels and tiles
4. WHERE a connection is established, THE Game Client SHALL store the Max Range value for the duration of the session

### Requirement 4: Client-Side Targeting Range Enforcement

**User Story:** As a player, I want the targeting cursor to stay within the allowed planting range, so that I don't waste time targeting locations the server will reject.

#### Acceptance Criteria

1. WHEN the Targeting System receives a cursor movement command, THE Targeting System SHALL calculate the Euclidean Distance from the player position to the proposed target position
2. IF the calculated distance exceeds the Max Range, THEN THE Targeting System SHALL clamp the target position to the nearest valid position on the Max Range boundary
3. WHEN the Targeting System clamps a target position, THE Targeting System SHALL snap the clamped position to the tile grid
4. WHILE the Max Range is set to negative one, THE Targeting System SHALL allow unlimited targeting range
5. THE Targeting System SHALL validate that the final target position is within the Max Range before confirming placement

### Requirement 5: Network Protocol Extension

**User Story:** As a developer, I want the connection protocol to transmit the planting range, so that clients receive the configuration automatically.

#### Acceptance Criteria

1. THE Connection Accepted Message SHALL include a field for the Max Range value as a 32-bit integer
2. WHEN the server sends a Connection Accepted Message, THE server SHALL populate the Max Range field with the value from Server Config
3. WHEN the Game Client receives a Connection Accepted Message, THE Game Client SHALL extract the Max Range value from the message
4. THE Connection Accepted Message SHALL maintain backward compatibility with existing message structure

### Requirement 6: Configuration Validation and Defaults

**User Story:** As a server operator, I want sensible defaults and validation, so that the system works correctly even with misconfiguration.

#### Acceptance Criteria

1. WHERE the property "planting.max.range" is not present in server.properties, THE Server Config SHALL use 512 as the default Max Range
2. THE Server Config SHALL enforce a minimum Max Range of 64 pixels
3. THE Server Config SHALL enforce a maximum Max Range of 1024 pixels
4. WHEN the Server Config detects an invalid configuration value, THE Server Config SHALL log a descriptive error message including the invalid value and the default value being used

### Requirement 7: Logging and Observability

**User Story:** As a server operator, I want clear logging of range configuration and violations, so that I can monitor and troubleshoot the system.

#### Acceptance Criteria

1. WHEN the Server Config loads the Max Range, THE Server Config SHALL log the configured value in both pixels and tiles
2. WHEN the Client Connection rejects a planting request due to range violation, THE Client Connection SHALL log the client identifier, attempted distance, and maximum allowed distance
3. WHEN the Game Client receives the Max Range from the server, THE Game Client SHALL log the received value
4. WHEN the Targeting System clamps a target position, THE Targeting System SHALL log that clamping occurred

### Requirement 8: Single Player Mode Compatibility

**User Story:** As a player in single-player mode, I want the planting system to work correctly, so that I have a consistent experience across game modes.

#### Acceptance Criteria

1. WHERE the game is running in single-player mode without a server connection, THE Targeting System SHALL use unlimited range (negative one)
2. WHERE the game is running in single-player mode with a local server, THE Targeting System SHALL use the configured Max Range from the local server
3. THE Planting System SHALL function correctly in both single-player and multiplayer modes
