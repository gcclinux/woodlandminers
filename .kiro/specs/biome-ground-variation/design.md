# Design Document: Biome Ground Variation

## Overview

This design introduces a biome system that varies ground textures based on distance from the spawn point (0,0). The system will replace the current uniform grass texture with multiple biome zones, starting with grass and sand textures. The implementation follows the existing codebase patterns, particularly the configuration approach used in `RainConfig.java`, and integrates seamlessly with the current infinite world rendering system in `MyGdxGame.java`.

The biome system is purely visual and client-side, requiring no network synchronization since all clients can deterministically calculate the correct biome for any world coordinate.

## Architecture

### Component Structure

```
wagemaker.uk.biome/
├── BiomeConfig.java          # Configuration constants for biome system
├── BiomeType.java            # Enum defining available biome types
├── BiomeZone.java            # Data class representing a biome zone
├── BiomeManager.java         # Core logic for biome determination
└── BiomeTextureGenerator.java # Procedural texture generation
```

### Integration Points

1. **MyGdxGame.java**: 
   - Replace `grassTexture` field with `BiomeManager` instance
   - Modify `drawInfiniteGrass()` to use `BiomeManager.getTextureForPosition()`
   - Initialize `BiomeManager` in `create()` method

2. **Texture Management**:
   - Extend existing `createRealisticGrassTexture()` pattern
   - Add `createRealisticSandTexture()` method
   - Cache textures in `BiomeManager` to avoid recreation

3. **World Generation**:
   - Biome determination is independent of `worldSeed`
   - Uses only spawn point (0,0) and current coordinates
   - No changes needed to tree generation logic

## Components and Interfaces

### BiomeConfig.java

Configuration class following the pattern established by `RainConfig.java`:

```java
public class BiomeConfig {
    // Biome zone definitions (configurable)
    public static final float INNER_GRASS_RADIUS = 10000.0f;
    public static final float SAND_ZONE_WIDTH = 3000.0f;
    
    // Texture generation parameters
    public static final int TEXTURE_SIZE = 64;
    public static final int TEXTURE_SEED_GRASS = 12345;
    public static final int TEXTURE_SEED_SAND = 54321;
    
    // Grass texture colors (existing values from MyGdxGame)
    public static final float[] GRASS_BASE_COLOR = {0.15f, 0.5f, 0.08f, 1.0f};
    public static final float[] GRASS_LIGHT_COLOR = {0.25f, 0.65f, 0.15f, 1.0f};
    
    // Sand texture colors
    public static final float[] SAND_BASE_COLOR = {0.85f, 0.75f, 0.55f, 1.0f};
    public static final float[] SAND_LIGHT_COLOR = {0.95f, 0.85f, 0.65f, 1.0f};
    public static final float[] SAND_DARK_COLOR = {0.75f, 0.65f, 0.45f, 1.0f};
    
    // Performance settings
    public static final boolean ENABLE_TEXTURE_VARIATION = true;
}
```

### BiomeType.java

Enum defining available biome types:

```java
public enum BiomeType {
    GRASS,
    SAND;
    
    public String getDisplayName() {
        return name().toLowerCase();
    }
}
```

### BiomeZone.java

Data class representing a biome zone configuration:

```java
public class BiomeZone {
    private final float minDistance;
    private final float maxDistance;
    private final BiomeType biomeType;
    
    public BiomeZone(float minDistance, float maxDistance, BiomeType biomeType);
    public boolean containsDistance(float distance);
    public BiomeType getBiomeType();
}
```

### BiomeManager.java

Core biome determination logic:

```java
public class BiomeManager {
    private final List<BiomeZone> biomeZones;
    private final Map<BiomeType, Texture> textureCache;
    private final BiomeTextureGenerator textureGenerator;
    
    public BiomeManager();
    public void initialize();
    public Texture getTextureForPosition(float worldX, float worldY);
    public BiomeType getBiomeAtPosition(float worldX, float worldY);
    public void dispose();
    
    private float calculateDistanceFromSpawn(float x, float y);
    private void initializeBiomeZones();
}
```

**Key Methods:**

- `getTextureForPosition(x, y)`: Returns the appropriate texture for world coordinates
- `getBiomeAtPosition(x, y)`: Determines biome type based on distance from spawn
- `calculateDistanceFromSpawn(x, y)`: Uses Euclidean distance formula
- `initializeBiomeZones()`: Sets up the default biome zone configuration

### BiomeTextureGenerator.java

Procedural texture generation:

```java
public class BiomeTextureGenerator {
    public Texture generateGrassTexture();
    public Texture generateSandTexture();
    
    private Texture createTextureFromPixmap(Pixmap pixmap);
    private void addNaturalVariation(Pixmap pixmap, Random random, float[] colors);
}
```

## Data Models

### Biome Zone Configuration

Default configuration (based on requirements):

```
Zone 1: Distance 0 - 10000px → GRASS
Zone 2: Distance 10000 - 13000px → SAND  
Zone 3: Distance 13000+ → GRASS
```

Represented as:
```java
List<BiomeZone> zones = Arrays.asList(
    new BiomeZone(0, 10000, BiomeType.GRASS),
    new BiomeZone(10000, 13000, BiomeType.SAND),
    new BiomeZone(13000, Float.MAX_VALUE, BiomeType.GRASS)
);
```

### Texture Cache Structure

```java
Map<BiomeType, Texture> textureCache = new HashMap<>();
// Populated during initialization:
// GRASS → Texture (generated from existing createRealisticGrassTexture logic)
// SAND → Texture (new sand texture generation)
```

## Error Handling

### Texture Generation Failures

- **Issue**: Pixmap creation fails due to memory constraints
- **Handling**: Log error, fall back to solid color texture
- **Recovery**: Continue with degraded visuals rather than crash

### Invalid Distance Calculations

- **Issue**: NaN or Infinity from distance calculation
- **Handling**: Default to GRASS biome (spawn area)
- **Prevention**: Validate coordinates before calculation

### Missing Biome Configuration

- **Issue**: No biome zone matches calculated distance
- **Handling**: Default to GRASS biome
- **Prevention**: Ensure final zone has Float.MAX_VALUE as max distance

### Texture Disposal

- **Issue**: Texture disposed while still in use
- **Handling**: Only dispose textures in `BiomeManager.dispose()` called from `MyGdxGame.dispose()`
- **Prevention**: Never dispose individual biome textures during gameplay

## Testing Strategy

### Unit Tests

1. **BiomeManagerTest**
   - Test distance calculation accuracy
   - Test biome determination for known coordinates
   - Test boundary conditions (exactly at zone transitions)
   - Test negative coordinates
   - Test extreme distances (very far from spawn)

2. **BiomeTextureGeneratorTest**
   - Test texture generation produces valid textures
   - Test texture dimensions match configuration
   - Test texture disposal doesn't cause errors

3. **BiomeZoneTest**
   - Test zone containment logic
   - Test overlapping zones (should not occur)
   - Test zone ordering

### Integration Tests

1. **BiomeRenderingIntegrationTest**
   - Test texture changes when moving between biomes
   - Test performance with multiple biome zones visible
   - Test texture caching works correctly
   - Test single-player and multiplayer modes produce same results

### Manual Testing

1. **Visual Verification**
   - Walk from spawn (0,0) outward in all directions
   - Verify grass → sand → grass transitions occur at correct distances
   - Check for visual artifacts at biome boundaries
   - Verify textures tile seamlessly

2. **Performance Testing**
   - Monitor FPS while moving through biome transitions
   - Test with multiple players in different biomes (multiplayer)
   - Verify no memory leaks from texture generation

3. **Multiplayer Consistency**
   - Two clients at same coordinates should see same biome
   - Host and client should have identical ground rendering
   - Biome rendering should work in all three game modes

## Implementation Notes

### Texture Generation Details

**Sand Texture Algorithm:**
```
1. Fill base with sandy beige color
2. Add random darker spots (small rocks/shadows)
3. Add lighter highlights (sun-bleached areas)
4. Add subtle grain pattern for texture
5. Add occasional small details (pebbles, shells)
```

**Natural Variation:**
- Use per-pixel randomization with fixed seed for consistency
- Vary color slightly across pixels for organic look
- Add directional patterns (wind-swept sand)
- Ensure seamless tiling at 64x64 boundaries

### Performance Considerations

1. **Texture Caching**: Generate each biome texture once, reuse for all tiles
2. **Distance Calculation**: Simple Euclidean distance, computed per tile
3. **No Network Traffic**: Biome determination is purely client-side
4. **Memory Usage**: 2-3 textures in memory (64x64 RGBA = ~16KB each)

### Rendering Integration

Modify `MyGdxGame.drawInfiniteGrass()`:

```java
private void drawInfiniteGrass() {
    float camX = camera.position.x;
    float camY = camera.position.y;
    float viewWidth = viewport.getWorldWidth();
    float viewHeight = viewport.getWorldHeight();
    
    int startX = (int)((camX - viewWidth) / 64) * 64;
    int startY = (int)((camY - viewHeight) / 64) * 64;
    int endX = (int)((camX + viewWidth) / 64) * 64 + 64;
    int endY = (int)((camY + viewHeight) / 64) * 64 + 64;
    
    for (int x = startX; x <= endX; x += 64) {
        for (int y = startY; y <= endY; y += 64) {
            // Get appropriate texture for this position
            Texture texture = biomeManager.getTextureForPosition(x, y);
            batch.draw(texture, x, y, 64, 64);
            
            // Tree generation remains unchanged
            generateTreeAt(x, y);
        }
    }
}
```

### Future Extensibility

The design supports easy addition of new biomes:

1. Add new enum value to `BiomeType`
2. Add texture generation method to `BiomeTextureGenerator`
3. Add color configuration to `BiomeConfig`
4. Update `initializeBiomeZones()` with new zone definitions

No changes needed to core biome determination logic.

## Design Decisions and Rationales

### Decision 1: Client-Side Biome Determination

**Rationale**: Since biome is purely based on distance from spawn (0,0), all clients can independently calculate the correct biome for any coordinate. This eliminates network traffic and synchronization complexity.

**Trade-off**: Cannot have server-controlled or randomized biome placement without network sync.

### Decision 2: Configuration-Based Zones

**Rationale**: Following the `RainConfig` pattern makes biome zones easily adjustable without code changes. Developers can tune distances by modifying constants.

**Trade-off**: More complex biome patterns (noise-based, procedural) would require code changes.

### Decision 3: Texture Caching

**Rationale**: Generating textures procedurally for every tile would be prohibitively expensive. Cache one texture per biome type and reuse.

**Trade-off**: Less variation in ground appearance, but acceptable for 64x64 tiling textures.

### Decision 4: Euclidean Distance

**Rationale**: Simple, intuitive, and creates circular biome zones. Matches user's mental model of "distance from spawn."

**Trade-off**: Creates circular zones rather than more organic shapes, but this is acceptable for initial implementation.

### Decision 5: No Transition Blending

**Rationale**: Sharp biome boundaries are simpler to implement and perform better. The 64x64 tile size makes transitions less jarring.

**Trade-off**: Could add smooth blending in future if desired, but adds complexity.
