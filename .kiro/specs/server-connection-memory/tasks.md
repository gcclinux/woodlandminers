# Implementation Plan

- [x] 1. Create PlayerConfig class for configuration management
  - Create new `PlayerConfig.java` class in `src/main/java/wagemaker/uk/client/` package
  - Implement `load()` static method to read from `player.properties` file, returning default config if file doesn't exist
  - Implement `save()` method to write properties to disk with error handling
  - Implement `getLastServer()` method to retrieve saved server address (returns null if not set)
  - Implement `saveLastServer(String address)` method to update and persist server address
  - Use Java Properties class for file format with key `multiplayer.last-server`
  - _Requirements: 1.1, 1.5, 2.1, 2.2, 2.3, 2.4, 4.1, 4.2, 4.3, 4.4_

- [x] 2. Enhance ConnectDialog to support pre-filled server addresses
  - Add `setPrefilledAddress(String address)` method to ConnectDialog class
  - Modify the method to set `inputBuffer` to the provided address
  - Handle null and empty string cases gracefully (treat as empty input)
  - Ensure pre-filled address can be edited and cleared by the player
  - _Requirements: 1.2, 1.3, 1.4, 3.1, 3.2, 3.3_

- [x] 3. Integrate PlayerConfig with connection dialog opening
  - Modify MyGdxGame to load PlayerConfig when opening the connection dialog
  - Call `setPrefilledAddress()` with the saved server address before showing the dialog
  - Ensure the connection dialog shows empty field if no previous server is saved
  - _Requirements: 1.2, 1.3, 1.4_

- [x] 4. Save server address on successful connection
  - Add `lastConnectionAddress` field to MyGdxGame to track the connection attempt
  - Update `attemptConnectToServer()` method to store the full address string (with port)
  - Add `getLastConnectionAddress()` method to MyGdxGame for retrieval
  - Modify GameMessageHandler's `handleConnectionAccepted()` to save server address via PlayerConfig
  - Ensure save happens only after ConnectionAcceptedMessage is received
  - _Requirements: 1.1, 1.5_

- [x] 5. Add error handling and validation
  - Add try-catch blocks in PlayerConfig for I/O exceptions with console logging
  - Ensure PlayerConfig.load() never throws exceptions (returns default config on error)
  - Verify that save failures don't crash the application
  - Test behavior when config file is missing or corrupted
  - _Requirements: 1.4, 4.3_

- [x] 6. Write unit tests for PlayerConfig
  - Test loading non-existent config file returns default values
  - Test saving and loading server address round-trip
  - Test handling of null and empty server addresses
  - Test behavior with invalid file permissions (if possible to mock)
  - _Requirements: 1.1, 1.5, 2.1, 2.2, 2.3, 2.4_

- [x] 7. Write integration tests for connection flow
  - Test end-to-end flow: connect → save → reopen dialog → verify pre-fill
  - Test connecting to different servers updates the saved address
  - Test that connection failures don't save the address
  - Test behavior when config file doesn't exist initially
  - _Requirements: 1.1, 1.2, 1.3, 2.4, 3.2_
