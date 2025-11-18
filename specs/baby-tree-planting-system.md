# BabyTree Planting System Implementation Plan

## Overview
Implement a planting system for BabyTree items that follows the same pattern as the existing BabyBamboo planting system. Players will be able to plant BabyTree items from their inventory, which will grow into SmallTree instances after 120 seconds.

## Requirements Analysis

### Core Functionality
1. **Planting Mechanics**: Plant BabyTree items using the existing targeting system
2. **Growth System**: BabyTree grows into SmallTree after 120 seconds (same duration as bamboo)
3. **Inventory Integration**: Reduce BabyTree count when planted
4. **World Persistence**: Save/load planted trees in world saves
5. **Multiplayer Support**: Synchronize planting and growth across clients
6. **Biome Restrictions**: Plant on grass biomes (opposite of bamboo which plants on sand)

### Technical Requirements
1. **PlantedTree Class**: Similar to PlantedBamboo with growth timer
2. **Network Messages**: Plant and transform messages for multiplayer
3. **Targeting System**: Reuse existing system with grass biome validation
4. **World Save Integration**: Include planted trees in save/load operations

## Implementation Plan

### Phase 1: Core Planting Infrastructure

#### 1.1 Create PlantedTree Class
**File**: `/src/main/java/wagemaker/uk/planting/PlantedTree.java`

**Features**:
- Growth timer (120 seconds)
- Shared texture system (like PlantedBamboo)
- Tile-grid snapping
- Update method returning transformation readiness

**Key Methods**:
- `PlantedTree(float x, float y)` - Constructor with grid snapping
- `boolean update(float deltaTime)` - Returns true when ready to transform
- `boolean isReadyToTransform()` - Check transformation status
- `Texture getTexture()` - Get shared texture
- `void dispose()` - Cleanup with instance counting

#### 1.2 Create Network Messages
**Files**: 
- `/src/main/java/wagemaker/uk/network/TreePlantMessage.java`
- `/src/main/java/wagemaker/uk/network/TreeTransformMessage.java`

**TreePlantMessage Features**:
- Player ID, planted tree ID, coordinates
- Serializable for network transmission

**TreeTransformMessage Features**:
- Planted tree ID, small tree ID, coordinates
- Handles transformation from planted to full tree

#### 1.3 Update PlantingSystem
**File**: `/src/main/java/wagemaker/uk/planting/PlantingSystem.java`

**New Methods**:
- `boolean canPlantTree(float x, float y, BiomeManager biomeManager)` - Validate grass biome
- `String plantTree(float x, float y, Map<String, PlantedTree> plantedTrees)` - Plant logic

### Phase 2: Player Integration

#### 2.1 Update Player Class
**File**: `/src/main/java/wagemaker/uk/player/Player.java`

**Additions**:
- `Map<String, PlantedTree> plantedTrees` reference
- Tree planting logic in action handling
- Biome validation for tree planting (grass only)

**Modified Methods**:
- `handleSpacebarAction()` - Add tree planting when BabyTree selected
- `setPlantedTrees()` - Setter for planted trees map

#### 2.2 Update Targeting System
**File**: `/src/main/java/wagemaker/uk/targeting/TargetingSystem.java`

**Enhancements**:
- Biome-aware validation
- Support for both sand (bamboo) and grass (tree) planting
- Method to check valid planting surface based on selected item

### Phase 3: Game Integration

#### 3.1 Update MyGdxGame Class
**File**: `/src/main/java/wagemaker/uk/gdx/MyGdxGame.java`

**Additions**:
- `Map<String, PlantedTree> plantedTrees` field
- Rendering method `drawPlantedTrees()`
- Update loop for planted tree growth
- Transform logic (PlantedTree → SmallTree)
- Network message queues for tree planting

**Modified Methods**:
- `create()` - Initialize planted trees map
- `render()` - Add planted tree updates and rendering
- `dispose()` - Cleanup planted trees

#### 3.2 Update GameMessageHandler
**File**: `/src/main/java/wagemaker/uk/gdx/GameMessageHandler.java`

**New Message Handlers**:
- `handleTreePlantMessage()` - Process remote tree planting
- `handleTreeTransformMessage()` - Process tree transformation

### Phase 4: World Persistence

#### 4.1 Create PlantedTreeState
**File**: `/src/main/java/wagemaker/uk/network/PlantedTreeState.java`

**Features**:
- Serializable state for planted trees
- Growth timer persistence
- Position and ID storage

#### 4.2 Update WorldSaveData
**File**: `/src/main/java/wagemaker/uk/world/WorldSaveData.java`

**Additions**:
- `Map<String, PlantedTreeState> plantedTrees` field
- Getter/setter methods

#### 4.3 Update WorldSaveManager
**File**: `/src/main/java/wagemaker/uk/world/WorldSaveManager.java`

**Enhancements**:
- Save/load planted trees in world data
- Include planted trees in world state extraction

### Phase 5: Multiplayer Support

#### 5.1 Update GameServer
**File**: `/src/main/java/wagemaker/uk/network/GameServer.java`

**Additions**:
- Handle TreePlantMessage broadcasting
- Handle TreeTransformMessage broadcasting
- Include planted trees in world state sync

#### 5.2 Update GameClient
**File**: `/src/main/java/wagemaker/uk/network/GameClient.java`

**Additions**:
- Send tree plant messages
- Send tree transform messages
- Handle incoming tree messages

## Implementation Details

### Growth and Transformation Logic
```java
// In MyGdxGame.render()
List<String> treesToTransform = new ArrayList<>();
for (Map.Entry<String, PlantedTree> entry : plantedTrees.entrySet()) {
    PlantedTree planted = entry.getValue();
    if (planted.update(deltaTime)) {
        treesToTransform.add(entry.getKey());
    }
}

// Transform mature planted trees into small trees
for (String key : treesToTransform) {
    PlantedTree planted = plantedTrees.remove(key);
    float x = planted.getX();
    float y = planted.getY();
    
    SmallTree tree = new SmallTree(x, y);
    trees.put(key, tree);
    planted.dispose();
    
    // Send transformation message in multiplayer
    if (gameClient != null && gameClient.isConnected()) {
        TreeTransformMessage message = new TreeTransformMessage(
            gameClient.getClientId(), key, key, x, y
        );
        gameClient.sendMessage(message);
    }
}
```

### Biome Validation
```java
// In PlantingSystem
public boolean canPlantTree(float x, float y, BiomeManager biomeManager) {
    BiomeType biome = biomeManager.getBiomeAtPosition(x, y);
    return biome == BiomeType.GRASS; // Trees plant on grass, bamboo on sand
}
```

### Inventory Integration
```java
// In Player.handleSpacebarAction()
if (selectedSlot == 5) { // BabyTree slot
    Inventory inventory = inventoryManager.getCurrentInventory();
    if (inventory.getBabyTreeCount() > 0) {
        // Validate grass biome
        if (plantingSystem.canPlantTree(targetX, targetY, biomeManager)) {
            // Plant the tree
            String plantedId = plantingSystem.plantTree(targetX, targetY, plantedTrees);
            if (plantedId != null) {
                inventory.setBabyTreeCount(inventory.getBabyTreeCount() - 1);
                // Send network message if multiplayer
            }
        }
    }
}
```

## Testing Strategy

### Unit Tests
1. **PlantedTree Growth**: Verify 120-second growth timer
2. **Biome Validation**: Test grass-only planting restriction
3. **Network Messages**: Validate serialization/deserialization
4. **World Persistence**: Test save/load of planted trees

### Integration Tests
1. **Planting Flow**: End-to-end planting from inventory to SmallTree
2. **Multiplayer Sync**: Verify cross-client synchronization
3. **World Save/Load**: Test persistence across game sessions

### Manual Testing
1. **Gameplay Flow**: Plant trees and verify growth
2. **Biome Restrictions**: Attempt planting on sand (should fail)
3. **Inventory Updates**: Verify count decreases on planting
4. **Multiplayer**: Test with multiple clients

## Success Criteria

### Functional Requirements
- ✅ BabyTree items can be planted from inventory
- ✅ Planted trees grow into SmallTree after 120 seconds
- ✅ Planting only works on grass biomes
- ✅ Inventory count decreases when planting
- ✅ Planted trees persist in world saves
- ✅ Multiplayer synchronization works correctly

### Technical Requirements
- ✅ No memory leaks from texture management
- ✅ Thread-safe multiplayer operations
- ✅ Consistent behavior across game modes
- ✅ Proper cleanup on game exit

## Risk Mitigation

### Potential Issues
1. **Texture Memory**: Use shared texture pattern like PlantedBamboo
2. **Network Desync**: Implement proper message queuing
3. **Save Corruption**: Validate planted tree data on load
4. **Performance**: Limit planted tree count per area

### Mitigation Strategies
1. **Shared Textures**: Implement instance counting for disposal
2. **Deferred Operations**: Use render thread for OpenGL operations
3. **Data Validation**: Check bounds and types on load
4. **Spatial Limits**: Prevent excessive planting in small areas

## Implementation Order

1. **Core Classes**: PlantedTree, network messages
2. **Game Integration**: MyGdxGame updates, rendering
3. **Player Integration**: Planting logic, targeting
4. **World Persistence**: Save/load functionality
5. **Multiplayer**: Network synchronization
6. **Testing**: Unit and integration tests
7. **Polish**: Error handling, edge cases

This plan ensures a systematic implementation that follows the established patterns in the codebase while providing the same functionality as the BabyBamboo system but for trees on grass biomes.