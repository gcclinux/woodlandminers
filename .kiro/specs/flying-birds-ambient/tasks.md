# Implementation Plan

- [x] 1. Create core bird system classes

  - Create SpawnBoundary enum with TOP, BOTTOM, LEFT, RIGHT values
  - Create SpawnPoint data class with boundary, x, y, and direction fields
  - Create Bird class with position, texture, animation time, update and render methods
  - Create BirdFormation class with list of 5 birds, velocity, spawn/target boundaries
  - Create BirdFormationManager class with spawn timer, active formation, and random generator
  - _Requirements: 1.1, 1.2, 2.1, 2.2_

- [x] 1.1 Write property test for formation bird count

  - **Property 6: Formation contains exactly 5 birds**
  - **Validates: Requirements 2.1**

- [x] 2. Implement spawn mechanics

  - Implement generateRandomInterval() to return 180-300 seconds
  - Implement selectRandomSpawnPoint() to choose random boundary and position
  - Implement spawn position calculation for each boundary type (TOP, BOTTOM, LEFT, RIGHT)
  - Ensure spawn position variation from previous spawn
  - _Requirements: 1.1, 1.2, 1.5, 4.1, 4.2, 4.3_

- [x] 2.1 Write property test for spawn interval bounds


  - **Property 1: Spawn interval bounds**
  - **Validates: Requirements 1.1**

- [x] 2.2 Write property test for spawn position on boundary


  - **Property 2: Spawn position on boundary**
  - **Validates: Requirements 1.2**

- [x] 2.3 Write property test for consecutive spawn variation


  - **Property 5: Consecutive spawn variation**
  - **Validates: Requirements 1.5**

- [x] 2.4 Write property test for boundary selection distribution


  - **Property 10: Boundary selection distribution**
  - **Validates: Requirements 4.1**

- [x] 2.5 Write property test for vertical boundary Y-coordinate variation


  - **Property 11: Vertical boundary Y-coordinate variation**
  - **Validates: Requirements 4.2**

- [x] 2.6 Write property test for horizontal boundary X-coordinate variation


  - **Property 12: Horizontal boundary X-coordinate variation**
  - **Validates: Requirements 4.3**

- [ ] 3. Implement V-formation positioning
  - Implement initializeVFormation() to position 5 birds in V-shape
  - Calculate lead bird position at spawn point
  - Calculate wing bird positions with 40px spacing and 30-degree angle
  - Ensure consistent spacing between all birds in formation
  - _Requirements: 2.2, 2.4_

- [ ] 3.1 Write property test for V-shape arrangement
  - **Property 7: V-shape arrangement**
  - **Validates: Requirements 2.2**

- [ ] 3.2 Write property test for consistent bird spacing
  - **Property 9: Consistent bird spacing**
  - **Validates: Requirements 2.4**

- [ ] 4. Implement flight mechanics
  - Calculate flight direction from spawn boundary to opposite boundary
  - Implement Bird.update() to move bird along velocity vector
  - Implement BirdFormation.update() to update all birds with same velocity
  - Implement hasReachedTarget() to detect when formation crosses opposite boundary
  - _Requirements: 1.3, 1.4, 4.4_

- [ ] 4.1 Write property test for formation reaches opposite boundary
  - **Property 3: Formation reaches opposite boundary**
  - **Validates: Requirements 1.3**

- [ ] 4.2 Write property test for V-shape invariant during flight
  - **Property 8: V-shape invariant during flight**
  - **Validates: Requirements 2.3**

- [ ] 4.3 Write property test for flight path toward opposite boundary
  - **Property 13: Flight path toward opposite boundary**
  - **Validates: Requirements 4.4**

- [ ] 5. Implement spawn timer and lifecycle management
  - Implement BirdFormationManager.update() to handle spawn timer countdown
  - Trigger spawnFormation() when timer reaches zero
  - Reset timer with new random interval after spawn
  - Detect when formation reaches target and despawn it
  - Reset spawn timer after despawn
  - _Requirements: 1.1, 1.4, 1.5_

- [ ] 5.1 Write property test for despawn triggers timer reset
  - **Property 4: Despawn triggers timer reset**
  - **Validates: Requirements 1.4**

- [ ] 6. Implement rendering system
  - Load bird sprite texture in BirdFormationManager.initialize()
  - Implement Bird.render() to draw bird sprite at current position
  - Implement BirdFormation.render() to render all 5 birds
  - Implement BirdFormationManager.render() to render active formation only
  - Add null check to skip rendering when no formation is active
  - _Requirements: 3.1, 5.1_

- [ ] 6.1 Write property test for no rendering when not visible
  - **Property 14: No rendering when not visible**
  - **Validates: Requirements 5.1**

- [ ] 7. Implement resource management
  - Implement Bird.dispose() to clean up bird resources
  - Implement BirdFormation.dispose() to dispose all 5 birds
  - Call formation.dispose() when despawning
  - Implement BirdFormationManager.dispose() to clean up shared texture
  - _Requirements: 5.4_

- [ ] 7.1 Write property test for resource cleanup on despawn
  - **Property 15: Resource cleanup on despawn**
  - **Validates: Requirements 5.4**

- [ ] 8. Integrate with MyGdxGame
  - Add BirdFormationManager field to MyGdxGame
  - Initialize BirdFormationManager in MyGdxGame.create()
  - Call birdFormationManager.update() in MyGdxGame.render() before rendering
  - Call birdFormationManager.render() after rain effects but before UI elements
  - Call birdFormationManager.dispose() in MyGdxGame.dispose()
  - _Requirements: 3.1, 3.2, 3.3_

- [ ] 9. Create bird sprite asset
  - Create or source a simple bird sprite texture (32x32 pixels recommended)
  - Place texture in assets/sprites/ directory
  - Update Bird class to load texture from correct path
  - Test texture loading and rendering
  - _Requirements: 3.1_

- [ ] 10. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
