# Implementation Plan

- [x] 1. Modify tree spawning logic to query biome type
  - Update the `generateTreeAt()` method in MyGdxGame.java to call `biomeManager.getBiomeAtPosition(x, y)` before tree type selection
  - Store the returned BiomeType in a local variable for use in tree filtering logic
  - _Requirements: 1.1, 2.1, 3.2_

- [x] 2. Implement biome-based tree type filtering
- [x] 2.1 Add sand biome tree spawning logic
  - Replace the existing tree type selection logic with conditional logic based on biome type
  - For sand biomes, spawn only bamboo trees (100% probability)
  - Remove the random tree type selection for sand biomes since only one type is available
  - _Requirements: 1.1, 1.2, 1.3, 1.4_

- [x] 2.2 Add grass biome tree spawning logic
  - For grass biomes, adjust probability distribution to 25% for each of the 4 non-bamboo tree types
  - Update the treeType threshold checks: 0-0.25 (SmallTree), 0.25-0.5 (AppleTree), 0.5-0.75 (CoconutTree), 0.75-1.0 (BananaTree)
  - Ensure bamboo trees are completely excluded from grass biome spawning
  - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [x] 3. Verify deterministic behavior is preserved
  - Ensure the biome query happens after the random seed is set but before tree type selection
  - Verify that the same world seed and coordinates produce identical tree types across multiple runs
  - Confirm that no additional randomness is introduced that could break multiplayer synchronization
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 4.4_

- [ ] 4. Create integration test for biome-specific spawning
  - Write test that generates trees in known sand biome coordinates and verifies only bamboo trees spawn
  - Write test that generates trees in known grass biome coordinates and verifies no bamboo trees spawn
  - Write test that verifies all four non-bamboo tree types can spawn in grass biomes
  - Write test that verifies deterministic spawning with same world seed produces identical results
  - _Requirements: 1.1, 1.2, 2.1, 2.2, 3.1, 3.2_
