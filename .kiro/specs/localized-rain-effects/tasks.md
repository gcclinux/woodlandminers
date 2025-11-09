# Implementation Plan

- [x] 1. Create core rain system data structures
  - Create RainZone data class with zone properties (id, center coordinates, radius, fade distance, intensity)
  - Implement distance calculation method using Euclidean formula
  - Implement intensity calculation based on distance with fade effect
  - Add Serializable interface for multiplayer network transmission
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [x] 2. Implement RainParticle and object pooling
  - Create RainParticle class with position, velocity, length, alpha, and active state
  - Implement reset() method for particle reuse
  - Implement update() method for falling animation
  - Implement isOffScreen() check for particle recycling
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 5.4_

- [x] 3. Build RainZoneManager for zone management
  - Create RainZoneManager class with zone list storage
  - Implement addRainZone(), removeRainZone(), clearAllZones() methods
  - Implement setRainZones() for multiplayer synchronization
  - Implement isInRainZone() to check if player is in any zone
  - Implement getRainIntensityAt() to calculate combined intensity from all zones
  - Implement initializeDefaultZones() to create spawn area rain zone at (128, 128) with 640px radius
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 6.3, 6.4_

- [x] 4. Implement RainRenderer for particle rendering
  - Create RainRenderer class with ShapeRenderer integration
  - Initialize particle pool with MAX_PARTICLES (200) pre-allocated particles
  - Implement getTargetParticleCount() based on intensity (100-200 range)
  - Implement spawnParticle() with random X position across screen width
  - Implement updateParticles() to animate falling motion at 400-600 px/s
  - Implement render() using ShapeRenderer to draw semi-transparent lines (2px wide, 10-15px long)
  - Implement particle recycling when particles reach bottom of screen
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 4.1, 4.2, 4.3, 4.4, 4.5, 5.1, 5.2, 5.4, 5.5_

- [x] 5. Create RainSystem facade
  - Create RainSystem class that coordinates RainZoneManager and RainRenderer
  - Implement initialize() to set up zone manager and renderer
  - Implement update() to check player position and calculate intensity
  - Implement render() to delegate to RainRenderer
  - Implement setEnabled() for runtime enable/disable
  - Implement syncRainZones() for multiplayer zone synchronization
  - Implement dispose() for cleanup
  - _Requirements: 1.1, 1.2, 1.3, 5.3, 6.1, 6.2_

- [x] 6. Extend WorldState for multiplayer rain zone sync
  - Add rainZones field to WorldState class
  - Implement getRainZones() and setRainZones() methods
  - Add initializeRainZones() to create default spawn rain zone in server world state
  - Update createSnapshot() to include rain zones in world state snapshots
  - _Requirements: 3.1, 3.2, 3.3, 3.5_

- [x] 7. Integrate RainSystem into MyGdxGame
  - Add rainSystem field to MyGdxGame class
  - Initialize RainSystem in create() method with shapeRenderer reference
  - Call initializeDefaultZones() for single-player mode
  - Add rainSystem.update() call in render loop with player position
  - Add rainSystem.render() call after batch.end() but before UI rendering
  - Add rainSystem.dispose() call in dispose() method
  - Ensure rain only updates when game menu is not open
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 5.3_

- [x] 8. Implement multiplayer rain zone synchronization
  - Update GameServer to initialize rain zones in WorldState
  - Update GameClient to receive and sync rain zones from WorldStateMessage
  - Call rainSystem.syncRainZones() when client receives world state
  - Ensure rain zones are included in initial world state transmission
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 9. Add configuration and tuning parameters
  - Create RainConfig class with tunable constants (particle counts, visual properties, physics)
  - Document all configuration parameters with comments
  - Make parameters easily adjustable for future tuning
  - _Requirements: 6.2, 6.5_

- [x] 10. Create unit tests for rain system components
  - [x]* 10.1 Write tests for RainZone distance and intensity calculations
  - [x]* 10.2 Write tests for RainZoneManager zone management and queries
  - [x]* 10.3 Write tests for RainParticle pool reuse and reset functionality
  - [x]* 10.4 Write tests for edge cases (exactly at radius, at fade boundary, overlapping zones)
  - _Requirements: 2.4, 2.5, 5.4, 6.4_

- [x] 11. Perform integration testing
  - [ ]* 11.1 Test rain rendering in single-player mode at spawn area
  - [ ]* 11.2 Test rain fade effect when walking away from cactus
  - [ ]* 11.3 Test rain zone synchronization in multiplayer mode
  - [ ]* 11.4 Verify performance maintains 60 FPS with rain active
  - [ ]* 11.5 Test rain rendering order (above trees, below UI)
  - _Requirements: 1.1, 1.2, 1.3, 1.5, 3.4, 5.1_
