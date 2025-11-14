# Implementation Plan

- [x] 1. Create PlantedBamboo class with growth timer

  - Create `src/main/java/wagemaker/uk/planting/PlantedBamboo.java`
  - Implement constructor that accepts x, y coordinates and snaps to 64x64 tile grid
  - Implement texture loading using existing BabyBamboo sprite coordinates (192, 128, 64x64)
  - Implement `update(float deltaTime)` method that increments growth timer and returns true when >= 120 seconds
  - Implement `isReadyToTransform()` method that checks if growth timer >= 120 seconds
  - Implement position getters `getX()` and `getY()`
  - Implement `getTexture()` method for rendering
  - Implement `dispose()` method for texture cleanup
  - _Requirements: 3.1, 3.2, 3.3, 3.4_

- [x] 2. Create PlantingSystem class with validation logic

  - Create `src/main/java/wagemaker/uk/planting/PlantingSystem.java`
  - Implement `attemptPlant()` method that validates and creates PlantedBamboo
  - Implement validation: check if player has baby bamboo in inventory (slot 2)
  - Implement validation: check if player is standing on sand tile using BiomeManager
  - Implement validation: check if tile is not occupied by existing PlantedBamboo
  - Implement validation: check if tile is not occupied by existing BambooTree
  - Implement `snapToTileGrid()` helper method to align coordinates to 64x64 grid
  - Implement `generatePlantedBambooKey()` method to create unique tile-based keys
  - Deduct 1 baby bamboo from inventory on successful planting
  - Return PlantedBamboo instance on success, null on failure
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 2.3, 5.1, 5.2, 5.3_

- [x] 3. Integrate planting system into Player class

  - Add `PlantingSystem plantingSystem` field to Player class
  - Add `BiomeManager biomeManager` field to Player class
  - Add `Map<String, PlantedBamboo> plantedBamboos` field to Player class
  - Implement setter methods: `setPlantingSystem()`, `setBiomeManager()`, `setPlantedBamboos()`
  - Implement `handlePlantingAction()` method that checks for "p" key press
  - Check if baby bamboo is selected (inventory slot 2) before attempting to plant
  - Call `plantingSystem.attemptPlant()` with player position and required references
  - Add planted bamboo to game world map if planting succeeds
  - Call `handlePlantingAction()` in Player's `update()` method after inventory selection handling
  - Only process planting when game menu is not open
  - _Requirements: 1.1, 1.3, 4.1, 4.2_

- [x] 4. Integrate planted bamboo management into MyGdxGame

  - Add `Map<String, PlantedBamboo> plantedBamboos` field to MyGdxGame
  - Add `PlantingSystem plantingSystem` field to MyGdxGame
  - Initialize `plantedBamboos` HashMap in `create()` method
  - Initialize `plantingSystem` instance in `create()` method
  - Call player setters to inject plantingSystem, biomeManager, and plantedBamboos references
  - Implement planted bamboo update loop in `render()` method
  - Track planted bamboos that are ready to transform (growth timer >= 120s)
  - Transform mature planted bamboos: remove from plantedBamboos map, create BambooTree, add to bambooTrees map
  - Dispose PlantedBamboo textures after transformation
  - Render planted bamboos in `render()` method (after terrain, before trees)
  - Dispose all planted bamboos in `dispose()` method
  - _Requirements: 3.2, 3.3, 4.3_

- [x] 5. Update collision detection to include planted bamboos



  - Modify PlantingSystem validation to check for planted bamboo at target tile
  - Ensure planted bamboos block new planting attempts at same tile location
  - Use tile-based key lookup for O(1) occupancy checking
  - _Requirements: 2.1, 2.2, 2.3_
