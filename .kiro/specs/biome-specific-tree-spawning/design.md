# Design Document

## Overview

This design implements biome-aware tree spawning by integrating the existing BiomeManager with the tree generation logic in MyGdxGame. The solution modifies the `generateTreeAt()` method to query the biome type at each spawn location and filter available tree types accordingly. Bamboo trees will only spawn in sand biomes, while all other tree types will only spawn in grass biomes.

The design maintains the existing deterministic spawning behavior, ensuring multiplayer consistency, and preserves all current spawning rules (2% probability, 256px minimum distance, player view exclusion).

## Architecture

### Component Interaction

```
MyGdxGame.generateTreeAt()
    ↓
    1. Check biome type via BiomeManager.getBiomeAtPosition(x, y)
    ↓
    2. Filter available tree types based on biome
    ↓
    3. Generate tree from filtered list using existing random logic
```

### Key Components

1. **MyGdxGame.generateTreeAt()**: Modified to query biome type and filter tree types
2. **BiomeManager**: Existing component that provides biome type for any world coordinate
3. **Tree Classes**: No changes required (BambooTree, SmallTree, AppleTree, CoconutTree, BananaTree)

## Components and Interfaces

### Modified Method: generateTreeAt()

**Current Behavior:**
- Checks if location is valid for tree spawning
- Uses 2% probability with deterministic random seed
- Randomly selects from 5 tree types with equal 20% probability each
- Places tree in appropriate collection

**New Behavior:**
- Query biome type at spawn location using `biomeManager.getBiomeAtPosition(x, y)`
- Filter available tree types based on biome:
  - **Sand biome**: Only bamboo trees eligible
  - **Grass biome**: Only non-bamboo trees (small, apple, coconut, banana) eligible
- Adjust probability distribution based on filtered tree types:
  - **Sand biome**: 100% bamboo (since it's the only option)
  - **Grass biome**: 25% each for the 4 non-bamboo tree types
- Place tree using existing logic

### BiomeManager Integration

**Existing Method Used:**
```java
public BiomeType getBiomeAtPosition(float worldX, float worldY)
```

This method:
- Returns BiomeType.GRASS or BiomeType.SAND
- Is deterministic based on world coordinates
- Already initialized in MyGdxGame
- Works in both single-player and multiplayer modes

## Data Models

### Tree Type Filtering Logic

**Sand Biome Tree Types:**
- BambooTree (100% probability when spawning in sand)

**Grass Biome Tree Types:**
- SmallTree (25% probability when spawning in grass)
- AppleTree (25% probability when spawning in grass)
- CoconutTree (25% probability when spawning in grass)
- BananaTree (25% probability when spawning in grass)

### Probability Distribution

The tree type selection will use conditional probability:

```
P(tree spawns) = 0.02 (unchanged)

If biome == SAND:
    P(bamboo | spawn) = 1.0
    P(other trees | spawn) = 0.0

If biome == GRASS:
    P(bamboo | spawn) = 0.0
    P(small tree | spawn) = 0.25
    P(apple tree | spawn) = 0.25
    P(coconut tree | spawn) = 0.25
    P(banana tree | spawn) = 0.25
```

## Implementation Details

### Modified generateTreeAt() Pseudocode

```java
private void generateTreeAt(int x, int y) {
    String key = x + "," + y;
    
    // Existing validation checks
    if (location already has tree or is cleared) return;
    
    // Existing deterministic random seed
    random.setSeed(worldSeed + x * 31L + y * 17L);
    
    // Existing 2% spawn probability
    if (random.nextFloat() < 0.02f) {
        // Existing proximity and view checks
        if (isTreeNearby(x, y, 256)) return;
        if (isWithinPlayerView(x, y)) return;
        
        // NEW: Query biome type
        BiomeType biome = biomeManager.getBiomeAtPosition(x, y);
        
        // NEW: Generate tree based on biome
        float treeType = random.nextFloat();
        
        if (biome == BiomeType.SAND) {
            // Only bamboo trees in sand
            bambooTrees.put(key, new BambooTree(x, y));
        } else {
            // Only non-bamboo trees in grass (4 types, 25% each)
            if (treeType < 0.25f) {
                trees.put(key, new SmallTree(x, y));
            } else if (treeType < 0.5f) {
                appleTrees.put(key, new AppleTree(x, y));
            } else if (treeType < 0.75f) {
                coconutTrees.put(key, new CoconutTree(x, y));
            } else {
                bananaTrees.put(key, new BananaTree(x, y));
            }
        }
    }
}
```

## Error Handling

### Biome Query Failures

**Scenario**: BiomeManager returns null or unexpected value
**Handling**: Default to GRASS biome behavior (spawn non-bamboo trees)
**Rationale**: Grass is the default biome and most common, minimizing impact of edge cases

### BiomeManager Not Initialized

**Scenario**: BiomeManager.getBiomeAtPosition() called before initialization
**Handling**: BiomeManager already throws IllegalStateException with clear message
**Rationale**: This is a critical error that should be caught during development/testing

### Multiplayer Synchronization

**Scenario**: Clients might have different biome calculations
**Handling**: No special handling needed - BiomeManager is deterministic based on coordinates
**Rationale**: All clients will calculate the same biome for the same coordinates

## Testing Strategy

### Unit Testing

Not required for this feature as it modifies existing procedural generation logic that is inherently visual and integration-based.

### Integration Testing

**Test 1: Bamboo Trees Only Spawn in Sand**
- Generate trees across sand biome areas
- Verify all spawned trees in sand are bamboo trees
- Verify no non-bamboo trees spawn in sand

**Test 2: Non-Bamboo Trees Only Spawn in Grass**
- Generate trees across grass biome areas
- Verify no bamboo trees spawn in grass
- Verify all four non-bamboo tree types can spawn in grass

**Test 3: Deterministic Multiplayer Spawning**
- Start two clients with same world seed
- Navigate to same coordinates in both clients
- Verify identical tree types and positions in both clients
- Test across biome boundaries

**Test 4: Existing Spawn Rules Preserved**
- Verify 2% spawn probability maintained
- Verify 256px minimum tree distance maintained
- Verify trees don't spawn in player's initial view
- Verify deterministic generation based on world seed

### Manual Testing

**Visual Verification:**
- Explore sand biomes and verify only bamboo trees appear
- Explore grass biomes and verify bamboo trees never appear
- Walk along biome boundaries to verify clean transitions
- Test in both single-player and multiplayer modes

**Edge Cases:**
- Test at exact biome boundary coordinates
- Test in areas with noise-based biome variation
- Test with different world seeds

## Requirements Coverage

- **Requirement 1**: Bamboo trees spawn only in sand biomes (1.1, 1.2, 1.3, 1.4)
- **Requirement 2**: Non-bamboo trees spawn only in grass biomes (2.1, 2.2, 2.3, 2.4)
- **Requirement 3**: Deterministic multiplayer behavior maintained (3.1, 3.2, 3.3, 3.4)
- **Requirement 4**: Existing spawn rules preserved (4.1, 4.2, 4.3, 4.4)
