# Implementation Plan

- [x] 1. Modify ItemType enum to support new consumption model
  - Update enum values to distinguish health restoration vs hunger reduction
  - Change APPLE to restore 10% health (not 20%)
  - Change BANANA to reduce 5% hunger (not restore health)
  - Add methods: `restoresHealth()`, `reducesHunger()`
  - Remove old `isConsumable()` and `getHealthRestore()` methods
  - _Requirements: 1.1, 1.2, 2.1, 2.3_

- [x] 2. Remove automatic consumption from InventoryManager
  - Remove `tryAutoConsume()` method completely
  - Remove `consumeApple()` private method
  - Remove `consumeBanana()` private method
  - Modify `collectItem()` to always add items to inventory (remove health-based routing)
  - Remove call to `tryAutoConsume()` from Player.update()
  - _Requirements: 1.3, 2.2_

- [x] 3. Add hunger system to Player class
  - Add hunger field (float, 0-100)
  - Add hungerTimer field (float, accumulator)
  - Add HUNGER_INTERVAL constant (60.0f seconds)
  - Implement `updateHunger(float deltaTime)` method with 1% per 60 seconds logic
  - Implement `handleHungerDeath()` method for 100% hunger death
  - Add hunger update call in `update()` method
  - Add hunger reset in `respawnPlayer()` method
  - Add getters/setters: `getHunger()`, `setHunger(float)`
  - _Requirements: 4.1, 4.2, 4.3, 5.1, 5.2, 5.3, 5.4_

- [x] 4. Implement manual item consumption in Player class
  - Add `consumeSelectedItem()` method
  - Integrate with space bar input handling (context-sensitive)
  - Call `inventoryManager.tryConsumeSelectedItem()` for consumption
  - Handle apple consumption: restore 10% health
  - Handle banana consumption: reduce 5% hunger
  - Add logging for consumption events
  - _Requirements: 1.2, 2.1_

- [x] 5. Add manual consumption support to InventoryManager
  - Implement `tryConsumeSelectedItem(Player player)` method
  - Check selected slot and item type
  - Remove item from inventory if available
  - Apply effect to player (health or hunger)
  - Send inventory update in multiplayer mode
  - Return success/failure boolean
  - _Requirements: 1.2, 2.1_

- [x] 6. Modify apple tree destruction for immediate healing
  - Update `attackNearbyTargets()` in Player class
  - Add immediate 10% health restoration when apple tree destroyed
  - Ensure healing happens before apple item spawn
  - Apply in both single-player and multiplayer modes
  - Add logging for immediate heal event
  - _Requirements: 1.1_

- [x] 7. Create HealthBarUI class for unified health bar rendering
  - Create new file: `src/main/java/wagemaker/uk/ui/HealthBarUI.java`
  - Add fields: shapeRenderer, bar dimensions, position constants
  - Implement constructor taking ShapeRenderer
  - Implement `render(float health, float hunger)` method
  - Render green base layer (full bar)
  - Render red damage overlay from right side
  - Render blue hunger overlay from left side
  - Render black border
  - Implement `dispose()` method
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 8. Integrate HealthBarUI into MyGdxGame
  - Add HealthBarUI field to MyGdxGame
  - Initialize HealthBarUI in `create()` method
  - Call `healthBarUI.render()` in game render loop
  - Pass player health and hunger values
  - Call `healthBarUI.dispose()` in cleanup
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 9. Add multiplayer network messages for hunger system
  - Create `PlayerHungerUpdateMessage` class
  - Create `ItemConsumptionMessage` class
  - Extend `PlayerRespawnMessage` to include hunger field
  - Register new message types in network protocol
  - _Requirements: 4.1, 5.1, 5.2_

- [x] 10. Implement client-side hunger synchronization
  - Add hunger update sending in `Player.updateHunger()`
  - Add hunger field to `RemotePlayer` class
  - Implement `updateHunger()` method in RemotePlayer
  - Send hunger updates via GameClient
  - Track previous hunger for change detection
  - _Requirements: 4.1, 4.2_

- [x] 11. Implement server-side hunger handling
  - Add hunger field to PlayerState on server
  - Handle `PlayerHungerUpdateMessage` in GameServer
  - Broadcast hunger updates to other clients
  - Validate hunger values (0-100 range)
  - _Requirements: 4.1, 4.2_

- [x] 12. Implement multiplayer item consumption synchronization
  - Handle `ItemConsumptionMessage` in GameServer
  - Validate player has item in inventory
  - Apply consumption effect (health or hunger)
  - Broadcast inventory, health, and hunger updates
  - Handle consumption in GameClient message handler
  - _Requirements: 1.2, 2.1_

- [x] 13. Implement multiplayer apple tree healing synchronization
  - Modify server attack handler for apple tree destruction
  - Apply immediate 10% health restoration on server
  - Broadcast health update to all clients
  - Ensure healing happens before item spawn
  - _Requirements: 1.1_

- [x] 14. Implement multiplayer hunger death synchronization
  - Send respawn message with hunger reset in `handleHungerDeath()`
  - Handle respawn message on server with hunger field
  - Broadcast respawn to all clients including hunger state
  - Update RemotePlayer on respawn with hunger reset
  - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [x] 15. Add unified health bar rendering for remote players
  - Modify `RemotePlayer.renderHealthBar()` method
  - Use same three-layer rendering as local player
  - Show bar when health < 100 OR hunger > 0
  - Position bar above remote player sprite
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 16. Write integration tests for health and hunger system
- [x] 16.1 Test hunger accumulation over time
  - Verify 1% hunger increase every 60 seconds
  - Verify hunger caps at 100%
  - Verify timer precision with delta time
  - _Requirements: 4.1, 4.2, 4.3_

- [x] 16.2 Test apple consumption
  - Verify apple restores 10% health
  - Verify apple does not affect hunger
  - Verify apple removed from inventory
  - Verify health caps at 100%
  - _Requirements: 1.2_

- [x] 16.3 Test banana consumption
  - Verify banana reduces 5% hunger
  - Verify banana does not affect health
  - Verify banana removed from inventory
  - Verify hunger floors at 0%
  - _Requirements: 2.1_

- [x] 16.4 Test hunger death and respawn
  - Verify player dies at 100% hunger
  - Verify respawn at coordinates (0, 0)
  - Verify health reset to 100%
  - Verify hunger reset to 0%
  - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [x] 16.5 Test apple tree immediate healing
  - Verify 10% health restoration on tree destruction
  - Verify apple item spawns after healing
  - Verify healing works in single-player and multiplayer
  - _Requirements: 1.1_

- [x] 16.6 Test unified health bar rendering
  - Verify green base renders correctly
  - Verify red damage overlay from right
  - Verify blue hunger overlay from left
  - Test edge cases: 0% health, 100% hunger, both extremes
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 16.7 Test multiplayer hunger synchronization
  - Verify hunger updates broadcast to server
  - Verify remote players receive hunger updates
  - Verify hunger death broadcasts respawn
  - Verify item consumption syncs across clients
  - _Requirements: 4.1, 4.2, 5.1, 5.2_
