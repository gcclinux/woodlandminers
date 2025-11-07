# Implementation Plan

- [ ] 1. Add deferred operation queue infrastructure to MyGdxGame
  - Add `pendingDeferredOperations` field as ConcurrentLinkedQueue<Runnable> to MyGdxGame class
  - Initialize the queue in the `create()` method
  - Add `deferOperation(Runnable operation)` helper method with null check and optional logging
  - Add queue size warning when size exceeds 100 operations
  - _Requirements: 2.1, 2.3, 7.1, 7.4_

- [ ] 2. Integrate deferred operation processing into render loop
  - Add operation processing at the start of `render()` method before any rendering
  - Poll all operations from queue using while loop
  - Execute each operation with try-catch for error handling
  - Log errors with stack trace when operation execution fails
  - _Requirements: 2.2, 2.4, 7.3_

- [ ] 3. Modify removeItem() method to defer texture disposal
  - Update `removeItem()` method to keep immediate map removal
  - Replace direct `apple.dispose()` call with `deferOperation(() -> apple.dispose())`
  - Replace direct `banana.dispose()` call with `deferOperation(() -> banana.dispose())`
  - Add JavaDoc comments explaining thread safety and deferred disposal
  - _Requirements: 1.1, 1.2, 1.3, 3.1, 3.2, 3.3, 5.1, 5.2_

- [ ] 4. Add debug logging for deferred operations
  - Add debug-level log when operation is queued in `deferOperation()`
  - Include queue size in debug log message
  - Add debug-level log when operation is executed in `render()`
  - Use LibGDX log level check to avoid performance impact in production
  - _Requirements: 7.1, 7.2, 7.5_

- [ ] 5. Test the fix with multiplayer item pickup scenarios
  - Start dedicated server
  - Connect two clients (Windows and Linux if possible)
  - Destroy AppleTree and verify apple pickup works without crash
  - Destroy BananaTree and verify banana pickup works without crash
  - Test rapid item pickups from multiple trees
  - Verify both clients remain stable throughout testing
  - _Requirements: 1.4, 1.5, 3.4, 6.1, 6.2, 6.3_

- [ ] 6. Write unit tests for deferred operation mechanism
  - Write test for single deferred operation execution
  - Write test for multiple operations processed in FIFO order
  - Write test for exception handling (operation fails but others continue)
  - Write test for null operation handling
  - Write test for queue size warning threshold
  - _Requirements: 2.5, 5.4, 7.3_

- [ ] 7. Write integration tests for multiplayer item pickup
  - Write test simulating item pickup from network thread
  - Write test for rapid item pickups (10+ items)
  - Write test verifying items removed from game state immediately
  - Write test verifying texture disposal happens on render thread
  - Write test for both Apple and Banana item types
  - _Requirements: 1.1, 1.4, 1.5, 3.5_

- [ ] 8. Add developer documentation for threading guidelines
  - Add code comments to `deferOperation()` explaining when to use it
  - Add code comments to `removeItem()` explaining thread safety
  - Document the pattern in a comment block at top of MyGdxGame
  - Include example of correct vs incorrect usage
  - _Requirements: 5.3, 5.5_
