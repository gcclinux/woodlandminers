# Design Document: SmallTree WoodStack Drop

## Overview

This design implements a single-item drop system for SmallTree destruction. When a SmallTree is destroyed, it will drop a WoodStack item at the tree's position. The implementation follows the established patterns used by AppleTree (drops Apple) and BananaTree (drops Banana).

The design maintains consistency with the existing codebase architecture:
- WoodStack class follows the same structure as Apple and Banana
- Rendering uses the same batch.draw() pattern with specified dimensions
- Pickup detection uses the same collision detection logic
- Multiplayer synchronization follows the existing item spawn/pickup messaging system

## Architecture

### Component Interaction Flow

```
Player attacks SmallTree
    ↓
SmallTree.attack() returns true (health <= 0)
    ↓
Player detects destruction in update loop
    ↓
[Single-Player Mode]                    [Multiplayer Mode]
    ↓                                       ↓
Create WoodStack at (x, y)             Server handles destruction
Add to woodStacks collection           Server spawns WoodStack
    ↓                                   Broadcasts ItemSpawnMessage
MyGdxGame.render()                          ↓
    ↓                                   Client receives message
drawWoodStacks()                        Adds WoodStack to collection
    ↓                                       ↓
Player walks over WoodStack            MyGdxGame.render()
    ↓                                       ↓
checkWoodStackPickups()                drawWoodStacks()
    ↓                                       ↓
Remove from collection                 Player walks over WoodStack
Dispose texture                             ↓
                                       checkWoodStackPickups()
                                            ↓
                                       Send ItemPickupMessage
                                       Server broadcasts removal
                                       Client removes from collection
```

## Components and Interfaces

### 1. WoodStack Item Class

**Location:** `src/main/java/wagemaker/uk/items/WoodStack.java`

**Status:** Already created by user

**Current Implementation:**
- Extracts texture from sprite sheet at (256, 128) with 64x64 dimensions
- Stores position (x, y)
- Provides getters for texture and position
- Implements dispose() for texture cleanup

**No changes needed** - The existing implementation is correct and follows the Apple/Banana pattern.

### 2. MyGdxGame Class Modifications

**Location:** `src/main/java/wagemaker/uk/gdx/MyGdxGame.java`

**Changes Required:**

#### Add WoodStack Collection (around line 167, with other item collections)
```java
Map<String, WoodStack> woodStacks;
```

#### Import WoodStack (at top of file with other imports)
```java
import wagemaker.uk.items.WoodStack;
```

#### Initialize Collection in create() (around line 238, with other item initializations)
```java
woodStacks = new HashMap<>();
```

#### Add Rendering Method (after drawBabyBamboos(), around line 750)
```java
private void drawWoodStacks() {
    float camX = camera.position.x;
    float camY = camera.position.y;
    float viewWidth = viewport.getWorldWidth() / 2;
    float viewHeight = viewport.getWorldHeight() / 2;
    
    for (WoodStack woodStack : woodStacks.values()) {
        if (Math.abs(woodStack.getX() - camX) < viewWidth && 
            Math.abs(woodStack.getY() - camY) < viewHeight) {
            batch.draw(woodStack.getTexture(), woodStack.getX(), woodStack.getY(), 32, 32);
        }
    }
}
```

#### Call Rendering Method in render() (after drawBabyBamboos(), around line 451)
```java
drawWoodStacks();
```

#### Wire Collection to Player in create() (after player.setBabyBamboos(), around line 270)
```java
player.setWoodStacks(woodStacks);
```

### 3. Player Class Modifications

**Location:** `src/main/java/wagemaker/uk/player/Player.java`

**Changes Required:**

#### Import WoodStack (at top of file with other imports)
```java
import wagemaker.uk.items.WoodStack;
```

#### Add WoodStack Collection Reference (around line 53, with other item collections)
```java
private Map<String, WoodStack> woodStacks;
```

#### Add Setter Method (after setBabyBamboos(), around line 96)
```java
public void setWoodStacks(Map<String, WoodStack> woodStacks) {
    this.woodStacks = woodStacks;
}
```

#### Add SmallTree Destruction Logic in update()

Find the section where trees are attacked (likely in the update method where other tree attacks are handled). Add logic to spawn WoodStack when SmallTree is destroyed:

```java
// Check for SmallTree attacks
if (trees != null) {
    for (Map.Entry<String, SmallTree> entry : trees.entrySet()) {
        SmallTree tree = entry.getValue();
        String treeKey = entry.getKey();
        
        if (tree.isInAttackRange(x, y)) {
            boolean destroyed = tree.attack();
            
            if (destroyed) {
                // Spawn WoodStack at tree position
                woodStacks.put(treeKey + "-woodstack", 
                    new WoodStack(tree.getX(), tree.getY()));
                
                System.out.println("WoodStack dropped at: " + tree.getX() + ", " + tree.getY());
                
                tree.dispose();
                trees.remove(treeKey);
                clearedPositions.put(treeKey, true);
                break;
            }
        }
    }
}
```

#### Add Pickup Check Method (after checkBabyBambooPickups(), around line 950)
```java
private void checkWoodStackPickups() {
    if (woodStacks != null) {
        for (Map.Entry<String, WoodStack> entry : woodStacks.entrySet()) {
            WoodStack woodStack = entry.getValue();
            String woodStackKey = entry.getKey();
            
            // Check if player is close enough to pick up (32px range)
            float dx = Math.abs((x + 32) - (woodStack.getX() + 16)); // 32x32 item, center at +16
            float dy = Math.abs((y + 32) - (woodStack.getY() + 16));
            
            if (dx <= 32 && dy <= 32) {
                pickupWoodStack(woodStackKey);
                break;
            }
        }
    }
}

private void pickupWoodStack(String woodStackKey) {
    if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
        gameClient.sendItemPickup(woodStackKey);
    } else {
        // Single-player mode: handle locally
        System.out.println("WoodStack picked up!");
        
        if (woodStacks.containsKey(woodStackKey)) {
            WoodStack woodStack = woodStacks.get(woodStackKey);
            woodStack.dispose();
            woodStacks.remove(woodStackKey);
            System.out.println("WoodStack removed from game");
        }
    }
}
```

#### Call Pickup Check in update() (after checkBabyBambooPickups(), around line 298)
```java
checkWoodStackPickups();
```

## Data Models

### Item Positioning

**WoodStack Position:**
- X: `smallTree.getX()` (tree's base X coordinate)
- Y: `smallTree.getY()` (tree's base Y coordinate)

**Rationale:** Spawning at the tree's base position makes the item easily discoverable and follows the same pattern as Apple and Banana drops.

### Item Identifiers

WoodStack items use the tree's position key with suffix:
- WoodStack: `"{x},{y}-woodstack"`

**Example:** Tree at (128, 256) produces:
- `"128,256-woodstack"`

This ensures unique identifiers and maintains traceability to the source tree.

### Render Dimensions

WoodStack renders at **32x32 pixels** on screen:
- Source texture: 64x64 from sprite sheet at (256, 128)
- Rendered size: 32x32 (50% scale)
- Collision center: +16 pixels from item position

This matches the Apple and Banana item rendering approach and provides appropriate visual size for ground items.

## Error Handling

### Null Safety

All item collection access includes null checks:
```java
if (woodStacks != null) {
    // Access collection
}
```

This prevents NullPointerException if collections aren't initialized.

### Texture Disposal

Items must be disposed when removed:
```java
woodStack.dispose();
woodStacks.remove(key);
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
2. Find and attack a SmallTree until destroyed
3. Verify WoodStack spawns at tree position
4. Verify item renders at 32x32 pixels
5. Walk over WoodStack - verify pickup and removal
6. Verify console logs show correct position and pickup messages
7. Attack multiple SmallTrees to verify multiple WoodStacks can exist

**Multiplayer Mode:**
1. Start server and connect client
2. Client attacks SmallTree until destroyed
3. Verify WoodStack spawns on client
4. Verify item appears on other connected clients
5. Client picks up WoodStack
6. Verify removal on all clients
7. Test with multiple players attacking different SmallTrees

**Visual Verification:**
1. Compare WoodStack sprite to expected sprite sheet coordinates (256, 128)
2. Verify item renders at appropriate size (32x32)
3. Verify item is positioned at tree's base location

### Edge Cases

**Rapid Pickup:**
- Player walks over item quickly
- Expected: Item picked up, no crashes
- Handled by: `break` statement in pickup loop (one item per frame)

**Tree Destruction During Multiplayer Lag:**
- Network delay between destruction and item spawn
- Expected: Item eventually appears when message arrives
- Handled by: Pending item spawn queue in MyGdxGame

**Memory Management:**
- Multiple SmallTrees destroyed
- Expected: All textures properly disposed
- Handled by: dispose() calls in pickup methods

**SmallTree Not Yet Integrated:**
- If SmallTree attack logic doesn't exist yet in Player class
- Expected: Need to add attack detection similar to AppleTree/BananaTree
- Solution: Follow the same pattern as other tree types

## Implementation Notes

### Code Consistency

The implementation maintains consistency with existing patterns:
- WoodStack class matches Apple/Banana structure exactly
- Rendering follows the same batch.draw() pattern
- Pickup detection uses identical collision logic
- Multiplayer messaging reuses existing ItemPickupMessage system

### Performance Considerations

**Texture Loading:**
- Each WoodStack creates its own texture from sprite sheet
- Matches existing Apple/Banana implementation
- Acceptable for typical gameplay scenarios

**Collision Detection:**
- Pickup checks iterate all items each frame
- Acceptable for small-to-medium item counts
- Breaks after first pickup to limit iterations

**Rendering:**
- Viewport culling prevents off-screen rendering
- Only items within camera view are drawn
- Matches existing optimization strategy

### SmallTree Integration Status

Based on the codebase review:
- SmallTree class exists and is imported in Player
- SmallTree collection exists in MyGdxGame
- SmallTree has attack() method that returns boolean when destroyed
- SmallTree has isInAttackRange() method for range checking

**Assumption:** SmallTree attack logic may need to be added to Player.update() if it doesn't already exist. The implementation should follow the same pattern as AppleTree and BananaTree attacks.

### Future Enhancements

**Potential Improvements:**
1. Item stacking/inventory system (currently items just disappear on pickup)
2. Visual pickup animation or sound effect
3. Different wood stack sizes based on tree size
4. Crafting system using WoodStack as material

These enhancements are out of scope for the current implementation but could be added later.
