# Implementation Plan

- [x] 1. Create core puddle data structures and configuration

  - Create `PuddleConfig.java` with all configuration constants (timing, density, visual properties)
  - Create `PuddleState.java` enum with states: NONE, ACCUMULATING, ACTIVE, EVAPORATING
  - Create `WaterPuddle.java` class with position, size, alpha, rotation, and pooling support
  - _Requirements: 5.1, 5.3, 5.4_

- [x] 1.1 Write property test for configuration effects


  - **Property 10: Configuration affects new puddles**
  - **Validates: Requirements 5.2**

- [x] 2. Implement PuddleRenderer with object pooling

  - Create `PuddleRenderer.java` class
  - Implement puddle pool initialization (pre-allocate MAX_PUDDLES)
  - Implement `spawnPuddles()` method to create puddles within camera viewport with random positions and sizes
  - Implement `updatePuddles()` method to apply alpha multiplier for evaporation
  - Implement `render()` method using ShapeRenderer to draw ellipses
  - Implement `clearAllPuddles()` and `dispose()` methods
  - _Requirements: 1.1, 1.4, 3.1, 3.2, 4.2, 4.3_

- [x] 2.1 Write property test for viewport containment

  - **Property 4: Viewport containment**
  - **Validates: Requirements 1.4**

- [x] 2.2 Write property test for camera movement updates

  - **Property 9: Camera movement updates puddles**
  - **Validates: Requirements 3.4**

- [x] 3. Implement PuddleManager state machine and timing logic

  - Create `PuddleManager.java` class with PuddleRenderer instance
  - Implement state tracking (currentState, accumulationTimer, evaporationTimer)
  - Implement `update()` method with state machine logic:
    - NONE → ACCUMULATING when rain starts
    - ACCUMULATING → ACTIVE after 5 seconds
    - ACTIVE → EVAPORATING when rain stops
    - EVAPORATING → NONE after 5 seconds
  - Implement accumulation progress tracking (0-5 seconds)
  - Implement evaporation progress tracking with alpha calculation
  - Implement puddle spawning when transitioning to ACTIVE state
  - Implement puddle clearing when transitioning to NONE state
  - _Requirements: 1.1, 1.2, 2.1, 2.2, 2.3, 4.1_

- [x] 3.1 Write property test for accumulation threshold timing

  - **Property 1: Accumulation threshold timing**
  - **Validates: Requirements 1.1**

- [x] 3.2 Write property test for puddle stability during rain

  - **Property 2: Puddle stability during rain**
  - **Validates: Requirements 1.2**

- [x] 3.3 Write property test for evaporation timing

  - **Property 5: Evaporation timing**
  - **Validates: Requirements 2.1**

- [x] 3.4 Write property test for monotonic alpha decrease

  - **Property 6: Monotonic alpha decrease**
  - **Validates: Requirements 2.2**

- [x] 3.5 Write property test for complete cleanup after evaporation


  - **Property 7: Complete cleanup after evaporation**
  - **Validates: Requirements 2.3**

- [x] 3.6 Write unit test for rain restart during evaporation


  - Test that rain restarting during evaporation restores puddles to full visibility
  - _Requirements: 2.4_

- [x] 4. Implement intensity-based puddle behavior

  - Add intensity parameter to PuddleManager update method
  - Implement puddle count calculation based on intensity (MIN_PUDDLES to MAX_PUDDLES)
  - Implement puddle alpha adjustment based on intensity
  - Update spawnPuddles to respect intensity-based count
  - _Requirements: 1.3, 3.3_

- [x] 4.1 Write property test for intensity affects visibility

  - **Property 3: Intensity affects visibility**
  - **Validates: Requirements 1.3**

- [x] 4.2 Write property test for puddle count stability

  - **Property 8: Puddle count correlates with intensity**
  - **Validates: Requirements 3.3**

- [x] 5. Integrate PuddleManager with RainSystem

  - Add PuddleManager instance to RainSystem class
  - Initialize PuddleManager in RainSystem.initialize()
  - Update RainSystem.update() to call puddleManager.update() with rain state from DynamicRainManager
  - Update RainSystem.render() to call puddleManager.render() before returning (after rain particles)
  - Update RainSystem.dispose() to call puddleManager.dispose()
  - Pass isRaining() and getRainTimeRemaining() from DynamicRainManager to PuddleManager
  - _Requirements: 4.1, 4.2, 4.3_

- [x] 5.1 Write integration test for puddle system with DynamicRainManager

  - Test full rain cycle: start rain → wait 5s → puddles appear → rain stops → puddles fade
  - Verify state transitions and timing
  - _Requirements: 1.1, 2.1, 4.1_

- [x] 6. Handle edge cases and state transitions

  - Implement logic for rain stopping before 5-second threshold (ACCUMULATING → NONE)
  - Implement logic for rain restarting during evaporation (EVAPORATING → ACTIVE)
  - Add validation to prevent negative timers
  - Add bounds checking for alpha values (0.0 to 1.0)
  - _Requirements: 2.4, 4.4_

- [x] 6.1 Write unit test for rain stops before threshold

  - Test that puddles don't spawn if rain stops before 5 seconds
  - _Requirements: 4.4_

- [x] 7. Add puddle spacing and distribution logic



  - Implement minimum spacing check between puddles (MIN_PUDDLE_SPACING)
  - Add retry logic when spawning puddles to avoid overlaps
  - Implement random rotation for visual variety
  - _Requirements: 3.1, 3.2_

- [x] 8. Checkpoint - Ensure all tests pass

  - Ensure all tests pass, ask the user if questions arise.

- [x] 9. Performance optimization and validation

  - Add puddle count monitoring and logging
  - Implement viewport-based culling (don't render off-screen puddles)
  - Add configuration option to disable puddles (PUDDLES_ENABLED flag)
  - Test with MAX_PUDDLES active and verify performance
  - _Requirements: 4.5_

- [x] 9.1 Write performance test for puddle rendering

  - Measure frame time with MAX_PUDDLES active
  - Verify performance meets requirements
  - _Requirements: 4.5_

- [x] 10. Final integration and polish

  - Verify rendering order (puddles above ground, below player)
  - Test puddle visibility with different camera zoom levels
  - Adjust color and alpha values for optimal visibility
  - Add debug logging for state transitions (optional, can be removed later)
  - _Requirements: 1.5_

- [x] 11. Final Checkpoint - Ensure all tests pass

  - Ensure all tests pass, ask the user if questions arise.
