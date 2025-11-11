# Design Document: Bamboo Tree Dual Item Drops

## Overview

This design extends the existing single-item drop system to support dual-item drops from BambooTree destruction. The implementation follows the established patterns used by AppleTree (drops Apple) and BananaTree (drops Banana), but spawns two items instead of one. The items will be positioned with a small horizontal offset to make them visually distinct and collectible.

The design maintains consistency with the existing codebase architecture:
- Item classes follow the same structure as Apple and Banana
- Rendering uses the same batch.draw() pattern with specified dimensions
- Pickup detection uses the same collision detection logic
- Multiplayer synchronization follows the existing item spawn/pickup messaging system

## Architecture

### Component Interaction Flow

```
Player attacks BambooTree
    ↓
BambooTree.attack() returns true (health <= 0)
    ↓
Player.attackNearbyTargets() detects destruction
    ↓
[Single-Player Mode]                    [Multiplayer Mode]
    ↓                                       ↓
Create BambooStack at (x, y)           Server handles destruction
Create BabyBamboo at (x+8, y)          Server spawns items
Add to collections                      Broadcasts ItemSpawnMessage
    ↓                                       ↓
MyGdxGame.render()                     Client receives messages
    ↓                                   Adds items to collections
drawBambooStacks()                          ↓
drawBabyBamboos()                      MyGdxGame.render()
    ↓                                       ↓
Player walks over items                drawBambooStacks()
    ↓                                   drawBabyBamboos()
checkBambooStackPickups()                   ↓
checkBabyBambooPickups()               Player walks over items
    ↓                                       ↓
Remove from collections                checkBambooStackPickups()
Dispose textures                       checkBabyBambooPickups()
                                            ↓
                                       Send ItemPickupMessage
                                       Server broadcasts removal
                                       Client removes from collections
```

## Components and Interfaces

### 1. BambooStack Item Class

**Location:** `src/main/java/wagemaker/uk/items/BambooStack.java`

**Status:** Already created by user

**Current Implementation:**
- Extracts texture from sprite sheet at (128, 128) with 64x64 dimensions
- Stores position (x, y)
- Provides getters for texture and position
- Implements dispose() for texture cleanup

**No changes needed** - The existing implementation is correct.

### 2. BabyBamboo Item Class

**Location:** `src/main/java/wagemaker/uk/items/BabyBamboo.java`

**Status:** Already created by user

**Current Implementation:**
- Extracts texture from sprite sheet at (192, 128) with 64x64 dimensions
- Stores position (x, y)
- Provides getters for texture and position
- Implements dispose() for texture cleanup

**No changes needed** - The existing implementation is correct.

### 3. MyGdxGame Class Modifications

**Location:** `src/main/java/wagemaker/uk/gdx/MyGdxGame.java`

**Changes Required:**

#### Add Item Collections (around line 167)
```java
Map<String, BambooStack> bambooStacks;
Map<String, BabyBamboo> babyBamboos;
```

#### Initialize Collections in create() (around line 238)
```java
bambooStacks = new HashMap<>();
babyBamboos = new HashMap<>();
```

#### Add Rendering Methods (after drawBananas(), around line 725)
```java
private void drawBambooStacks() {
    float camX = camera.position.x;
    float camY = camera.position.y;
    float viewWidth = viewport.getWorldWidth() / 2;
    float viewHeight = viewport.getWorldHeight() / 2;
    
    for (BambooStack bambooStack : bambooStacks.values()) {
        if (Math.abs(bambooStack.getX() - camX) < viewWidth && 
            Math.abs(bambooStack.getY() - camY) < viewHeight) {
            batch.draw(bambooStack.getTexture(), bambooStack.getX(), bambooStack.getY(), 32, 32);
        }
    }
}

private void drawBabyBamboos() {
    float camX = camera.position.x;
    float camY = camera.position.y;
    float viewWidth = viewport.getWorldWidth() / 2;
    float viewHeight = viewport.getWorldHeight() / 2;
    
    for (BabyBamboo babyBamboo : babyBamboos.values()) {
        if (Math.abs(babyBamboo.getX() - camX) < viewWidth && 
            Math.abs(babyBamboo.getY() - camY) < viewHeight) {
            batch.draw(babyBamboo.getTexture(), babyBamboo.getX(), babyBamboo.getY(), 32, 32);
        }
    }
}
```

#### Call Rendering Methods in render() (after drawBananas(), around line 451)
```java
drawBambooStacks();
drawBabyBamboos();
```

### 4. Player Class Modifications

**Location:** `src/main/java/wagemaker/uk/player/Player.java`

**Changes Required:**

#### Add Item Collection References (around line 53)
```java
private Map<String, BambooStack> bambooStacks;
private Map<String, BabyBamboo> babyBamboos;
```

#### Add Setter Methods (after setBananas(), around line 96)
```java
public void setBambooStacks(Map<String, BambooStack> bambooStacks) {
    this.bambooStacks = bambooStacks;
}

public void setBabyBamboos(Map<String, BabyBamboo> babyBamboos) {
    this.babyBamboos = babyBamboos;
}
```

#### Modify attackNearbyTargets() - Bamboo Tree Section (around line 640)

Replace the bamboo tree destruction block with:
```java
if (destroyed) {
    // Spawn BambooStack at tree position
    bambooStacks.put(targetKey + "-bamboostack", 
        new BambooStack(targetBambooTree.getX(), targetBambooTree.getY()));
    
    // Spawn BabyBamboo offset by 8 pixels horizontally
    babyBamboos.put(targetKey + "-babybamboo", 
        new BabyBamboo(targetBambooTree.getX() + 8, targetBambooTree.getY()));
    
    System.out.println("BambooStack dropped at: " + targetBambooTree.getX() + ", " + targetBambooTree.getY());
    System.out.println("BabyBamboo dropped at: " + (targetBambooTree.getX() + 8) + ", " + targetBambooTree.getY());
    
    targetBambooTree.dispose();
    bambooTrees.remove(targetKey);
    clearedPositions.put(targetKey, true);
}
```

#### Add Pickup Check Methods (after checkBananaPickups(), around line 910)
```java
private void checkBambooStackPickups() {
    if (bambooStacks != null) {
        for (Map.Entry<String, BambooStack> entry : bambooStacks.entrySet()) {
            BambooStack bambooStack = entry.getValue();
            String bambooStackKey = entry.getKey();
            
            // Check if player is close enough to pick up (32px range)
            float dx = Math.abs((x + 32) - (bambooStack.getX() + 16)); // 32x32 item, center at +16
            float dy = Math.abs((y + 32) - (bambooStack.getY() + 16));
            
            if (dx <= 32 && dy <= 32) {
                pickupBambooStack(bambooStackKey);
                break;
            }
        }
    }
}

private void pickupBambooStack(String bambooStackKey) {
    if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
        gameClient.sendItemPickup(bambooStackKey);
    } else {
        // Single-player mode: handle locally
        System.out.println("BambooStack picked up!");
        
        if (bambooStacks.containsKey(bambooStackKey)) {
            BambooStack bambooStack = bambooStacks.get(bambooStackKey);
            bambooStack.dispose();
            bambooStacks.remove(bambooStackKey);
            System.out.println("BambooStack removed from game");
        }
    }
}

private void checkBabyBambooPickups() {
    if (babyBamboos != null) {
        for (Map.Entry<String, BabyBamboo> entry : babyBamboos.entrySet()) {
            BabyBamboo babyBamboo = entry.getValue();
            String babyBambooKey = entry.getKey();
            
            // Check if player is close enough to pick up (32px range)
            float dx = Math.abs((x + 32) - (babyBamboo.getX() + 16)); // 32x32 item, center at +16
            float dy = Math.abs((y + 32) - (babyBamboo.getY() + 16));
            
            if (dx <= 32 && dy <= 32) {
                pickupBabyBamboo(babyBambooKey);
                break;
            }
        }
    }
}

private void pickupBabyBamboo(String babyBambooKey) {
    if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
        gameClient.sendItemPickup(babyBambooKey);
    } else {
        // Single-player mode: handle locally
        System.out.println("BabyBamboo picked up!");
        
        if (babyBamboos.containsKey(babyBambooKey)) {
            BabyBamboo babyBamboo = babyBamboos.get(babyBambooKey);
            babyBamboo.dispose();
            babyBamboos.remove(babyBambooKey);
            System.out.println("BabyBamboo removed from game");
        }
    }
}
```

#### Call Pickup Checks in update() (after checkBananaPickups(), around line 298)
```java
checkBambooStackPickups();
checkBabyBambooPickups();
```

#### Wire Collections in MyGdxGame.create() (after player.setBananas(), around line 270)
```java
player.setBambooStacks(bambooStacks);
player.setBabyBamboos(babyBamboos);
```

## Data Models

### Item Positioning

**BambooStack Position:**
- X: `bambooTree.getX()` (tree's base X coordinate)
- Y: `bambooTree.getY()` (tree's base Y coordinate)

**BabyBamboo Position:**
- X: `bambooTree.getX() + 8` (8 pixels right of BambooStack)
- Y: `bambooTree.getY()` (same Y as BambooStack)

**Rationale:** 8-pixel horizontal offset provides visual separation while keeping both items close to the tree's base position. This makes both items easily discoverable and collectible.

### Item Identifiers

Items use the tree's position key with suffixes:
- BambooStack: `"{x},{y}-bamboostack"`
- BabyBamboo: `"{x},{y}-babybamboo"`

**Example:** Tree at (128, 256) produces:
- `"128,256-bamboostack"`
- `"128,256-babybamboo"`

This ensures unique identifiers and maintains traceability to the source tree.

### Render Dimensions

Both items render at **32x32 pixels** on screen:
- Source texture: 64x64 from sprite sheet
- Rendered size: 32x32 (50% scale)
- Collision center: +16 pixels from item position

This matches the Banana item's rendering approach and provides appropriate visual size for ground items.

## Error Handling

### Null Safety

All item collection access includes null checks:
```java
if (bambooStacks != null) {
    // Access collection
}
```

This prevents NullPointerException if collections aren't initialized.

### Texture Disposal

Items must be disposed when removed:
```java
bambooStack.dispose();
bambooStacks.remove(key);
```

Failure to dispose causes memory leaks as textures remain in GPU memory.

### Multiplayer Synchronization

In multiplayer mode:
- Client sends pickup request via `gameClient.sendItemPickup(key)`
- Server validates and broadcasts removal
- Client waits for server confirmation before removing item

This prevents desync where items appear picked up locally but still exist on server.

## Testing Strategy

### Manual Testing Checklist

**Single-Player Mode:**
1. Start single-player game
2. Find and attack a BambooTree until destroyed
3. Verify two items spawn at tree position
4. Verify items are positioned 8 pixels apart horizontally
5. Verify items render at 32x32 pixels
6. Walk over BambooStack - verify pickup and removal
7. Walk over BabyBamboo - verify pickup and removal
8. Verify console logs show correct positions and pickup messages

**Multiplayer Mode:**
1. Start server and connect client
2. Client attacks BambooTree until destroyed
3. Verify both items spawn on client
4. Verify items appear on other connected clients
5. Client picks up BambooStack
6. Verify removal on all clients
7. Client picks up BabyBamboo
8. Verify removal on all clients

**Visual Verification:**
1. Compare BambooStack sprite to expected sprite sheet coordinates (128, 128)
2. Compare BabyBamboo sprite to expected sprite sheet coordinates (192, 128)
3. Verify 8-pixel horizontal spacing between items
4. Verify items render at appropriate size (32x32)

### Edge Cases

**Rapid Pickup:**
- Player walks over both items quickly
- Expected: Both items picked up, no crashes
- Handled by: `break` statement in pickup loops (one item per frame)

**Tree Destruction During Multiplayer Lag:**
- Network delay between destruction and item spawn
- Expected: Items eventually appear when message arrives
- Handled by: Pending item spawn queue in MyGdxGame

**Memory Management:**
- Multiple bamboo trees destroyed
- Expected: All textures properly disposed
- Handled by: dispose() calls in pickup methods

## Implementation Notes

### Code Consistency

The implementation maintains consistency with existing patterns:
- Item classes match Apple/Banana structure exactly
- Rendering follows the same batch.draw() pattern
- Pickup detection uses identical collision logic
- Multiplayer messaging reuses existing ItemPickupMessage system

### Performance Considerations

**Texture Loading:**
- Each item creates its own texture from sprite sheet
- Consider texture caching if many items spawn simultaneously
- Current approach matches existing Apple/Banana implementation

**Collision Detection:**
- Pickup checks iterate all items each frame
- Acceptable for small-to-medium item counts
- Breaks after first pickup to limit iterations

**Rendering:**
- Viewport culling prevents off-screen rendering
- Only items within camera view are drawn
- Matches existing optimization strategy

### Future Enhancements

**Potential Improvements:**
1. Configurable item offset distance (currently hardcoded to 8 pixels)
2. Random offset direction (currently always horizontal right)
3. Item stacking/inventory system (currently items just disappear on pickup)
4. Visual pickup animation or sound effect
5. Different health restoration values per item type

These enhancements are out of scope for the current implementation but could be added later.
