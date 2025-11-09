# Tree Synchronization Fix

## Problem Description

In multiplayer mode, trees were not properly synchronized between players, causing the following issues:

1. **Trees visible to one player but not another** - When players explored different areas, they would see different trees
2. **Trees destructible by one player but not another** - One player could attack and destroy a tree that didn't exist for other players

## Root Cause

The issue was caused by a mismatch in tree generation between the server and clients:

### How It Was Supposed to Work
1. Server generates trees in a 5000x5000 area around spawn using deterministic world seed
2. Clients receive the initial world state with all pre-generated trees
3. Clients don't generate trees locally in multiplayer mode (only server generates)

### What Was Actually Happening
1. Server generated trees only in the initial 5000x5000 spawn area
2. When a player attacked a tree **outside** this area, the server would:
   - Detect the tree doesn't exist in its WorldState
   - Recreate it using deterministic generation (same seed + position)
   - Apply damage to the newly created tree
3. **BUT** the server never broadcast the newly created tree to other clients!
4. Result: Player A sees and attacks a tree, but Player B never receives that tree's state

## The Fix

### Changes Made

#### 1. ClientConnection.java (Server-side)
When the server recreates a tree that wasn't in the initial spawn area, it now broadcasts the tree to all clients:

```java
// Create tree state with full health (100)
tree = new TreeState(targetId, treeType, treeX, treeY, 100.0f, true);
server.getWorldState().addOrUpdateTree(tree);

System.out.println("Created tree state for " + targetId + " (type: " + treeType + ")");

// CRITICAL FIX: Broadcast the newly created tree to ALL clients
// This ensures all players see the same trees, even if they weren't in the initial spawn area
Map<String, TreeState> newTreeMap = new HashMap<>();
newTreeMap.put(targetId, tree);
WorldStateUpdateMessage updateMsg = new WorldStateUpdateMessage("server", 
    new HashMap<>(), // no player updates
    newTreeMap,      // tree update
    new HashMap<>()); // no item updates
server.broadcastToAll(updateMsg);
```

#### 2. GameMessageHandler.java (Client-side)
Added proper handling for WorldStateUpdateMessage to process tree updates:

```java
@Override
protected void handleWorldStateUpdate(WorldStateUpdateMessage message) {
    super.handleWorldStateUpdate(message);
    
    // Process tree updates
    if (message.getUpdatedTrees() != null) {
        for (TreeState treeState : message.getUpdatedTrees().values()) {
            game.updateTreeFromState(treeState);
        }
    }
    
    // Process item updates
    if (message.getUpdatedItems() != null) {
        for (ItemState itemState : message.getUpdatedItems().values()) {
            game.updateItemFromState(itemState);
        }
    }
    
    // Process player updates
    if (message.getUpdatedPlayers() != null) {
        for (wagemaker.uk.network.PlayerState playerState : message.getUpdatedPlayers().values()) {
            // Update remote player if exists
            RemotePlayer remotePlayer = game.getRemotePlayers().get(playerState.getPlayerId());
            if (remotePlayer != null) {
                remotePlayer.updatePosition(
                    playerState.getX(),
                    playerState.getY(),
                    playerState.getDirection(),
                    playerState.isMoving()
                );
                remotePlayer.updateHealth(playerState.getHealth());
            }
        }
    }
}
```

## How It Works Now

1. **Player A** explores an area and encounters a tree at position (1000, 2000)
2. **Player A** attacks the tree
3. **Server** receives the attack:
   - Checks if tree exists in WorldState (it doesn't)
   - Recreates tree using deterministic generation
   - **Broadcasts the new tree to ALL clients** via WorldStateUpdateMessage
4. **Player B** receives the WorldStateUpdateMessage:
   - Creates the tree locally at position (1000, 2000)
   - Now both players see the same tree!
5. **Server** applies damage and broadcasts TreeHealthUpdateMessage
6. Both players see the tree's health decrease

## Testing

To verify the fix works:

1. Start a multiplayer server
2. Connect two clients
3. Have Player A explore far from spawn (beyond 2500 pixels)
4. Have Player A attack a tree
5. Have Player B move to the same location
6. **Expected Result**: Player B should see the same tree that Player A attacked
7. **Expected Result**: Both players should be able to attack and destroy the tree

## Technical Details

### Deterministic Tree Generation
Trees are generated using a deterministic algorithm based on:
- World seed (same for all clients)
- Tree position (x, y coordinates)
- Formula: `Random(worldSeed + x * 31L + y * 17L)`

This ensures that given the same seed and position, all clients and the server will generate the same tree type.

### Message Flow
```
Player A attacks tree at (1000, 2000)
    ↓
Server receives AttackActionMessage
    ↓
Server checks WorldState - tree doesn't exist
    ↓
Server recreates tree using deterministic generation
    ↓
Server broadcasts WorldStateUpdateMessage to ALL clients
    ↓
All clients receive and create the tree locally
    ↓
Server applies damage and broadcasts TreeHealthUpdateMessage
    ↓
All clients update tree health
```

## Future Improvements

1. **Lazy Loading**: Instead of generating all trees in a 5000x5000 area at startup, generate trees on-demand as players explore
2. **Chunk-based Generation**: Divide the world into chunks and generate trees per chunk
3. **Periodic Sync**: Periodically sync all visible trees between clients to catch any missed updates
4. **Tree Registry**: Maintain a registry of all generated trees to avoid recreation logic

## Related Files

- `src/main/java/wagemaker/uk/network/ClientConnection.java` - Server-side attack handling
- `src/main/java/wagemaker/uk/gdx/GameMessageHandler.java` - Client-side message handling
- `src/main/java/wagemaker/uk/network/WorldState.java` - World state management
- `src/main/java/wagemaker/uk/gdx/MyGdxGame.java` - Client-side tree management
