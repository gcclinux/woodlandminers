# Implementation Plan: Biome Ground Variation

- [x] 1. Create biome package structure and core data models
  - Create `wagemaker.uk.biome` package
  - Implement `BiomeType` enum with GRASS and SAND values
  - Implement `BiomeZone` data class with distance range and biome type
  - _Requirements: 1.1, 3.1_

- [x] 2. Implement BiomeConfig configuration class
  - Create `BiomeConfig.java` following the `RainConfig` pattern
  - Define configurable distance thresholds (10000px inner grass, 3000px sand zone)
  - Define texture generation parameters (size, seeds, colors)
  - Add grass color constants from existing `MyGdxGame.createRealisticGrassTexture()`
  - Add sand color constants for new sand texture
  - _Requirements: 1.1, 3.1, 3.4_

- [x] 3. Implement BiomeTextureGenerator for procedural texture creation
  - Create `BiomeTextureGenerator.java` class
  - Extract and refactor existing grass texture generation from `MyGdxGame.createRealisticGrassTexture()`
  - Implement `generateGrassTexture()` method using extracted logic
  - Implement `generateSandTexture()` method with sandy beige colors and natural variation
  - Add helper methods for natural variation patterns (grain, spots, highlights)
  - Ensure textures are 64x64 and tile seamlessly
  - _Requirements: 1.1, 1.4_

- [x] 4. Implement BiomeManager core logic
  - Create `BiomeManager.java` class
  - Implement `initialize()` method to set up biome zones and generate textures
  - Implement `calculateDistanceFromSpawn(x, y)` using Euclidean distance formula
  - Implement `getBiomeAtPosition(x, y)` to determine biome type from distance
  - Implement `getTextureForPosition(x, y)` to return cached texture for coordinates
  - Implement `initializeBiomeZones()` to create default zone configuration
  - Implement texture caching in `Map<BiomeType, Texture>`
  - Implement `dispose()` method for texture cleanup
  - Add error handling for invalid distances and missing zones
  - _Requirements: 1.2, 1.3, 1.5, 2.1, 2.2, 4.1, 4.2_

- [x] 5. Integrate BiomeManager into MyGdxGame
  - Add `BiomeManager` field to `MyGdxGame` class
  - Initialize `BiomeManager` in `create()` method
  - Modify `drawInfiniteGrass()` to use `biomeManager.getTextureForPosition(x, y)` instead of single `grassTexture`
  - Remove old `grassTexture` field and `createRealisticGrassTexture()` method (logic moved to BiomeTextureGenerator)
  - Add `biomeManager.dispose()` call in `dispose()` method
  - Verify tree generation logic remains unchanged
  - _Requirements: 1.1, 1.3, 1.5, 2.1, 2.3_

- [x] 6. Write unit tests for biome system
  - Create `BiomeManagerTest.java` to test distance calculations and biome determination
  - Test boundary conditions (exactly at 10000px and 13000px transitions)
  - Test negative coordinates and extreme distances
  - Create `BiomeZoneTest.java` to test zone containment logic
  - Create `BiomeTextureGeneratorTest.java` to verify texture generation
  - _Requirements: 1.2, 1.4, 4.2_

- [x] 7. Create integration test for biome rendering
  - Create `BiomeRenderingIntegrationTest.java`
  - Test texture changes when moving between biome zones
  - Test that single-player and multiplayer modes produce identical biomes for same coordinates
  - Test texture caching works correctly (same texture instance returned for same biome)
  - _Requirements: 1.5, 2.1, 2.2, 2.3, 4.1, 4.2, 4.3_
