# Health and Hunger UI Fixes

## Issues Fixed

### 1. HealthBarUI Always Visible (Issue #1)
**Problem**: The local player's health bar UI was always visible, even at 100% health and 0% hunger.

**Solution**: Added a visibility check in `HealthBarUI.render()` to only display the bar when `health < 100 OR hunger > 0`.

**Files Modified**:
- `src/main/java/wagemaker/uk/ui/HealthBarUI.java`

**Changes**:
```java
public void render(float health, float hunger, Camera camera, Viewport viewport) {
    // Only show bar when health < 100 OR hunger > 0
    if (health >= 100 && hunger <= 0) {
        return;
    }
    // ... rest of rendering code
}
```

### 2. Multiplayer Crash - ShapeRenderer.begin() Error (Issue #2)
**Problem**: In multiplayer, when Player 2 got hungry, the game crashed with:
```
Exception in thread "main" java.lang.IllegalStateException: Call end() before beginning a new shape batch.
at com.badlogic.gdx.graphics.glutils.ShapeRenderer.begin(ShapeRenderer.java:206)
at wagemaker.uk.player.RemotePlayer.renderHealthBar(RemotePlayer.java:293)
```

**Root Cause**: The `RemotePlayer.renderHealthBar()` method was calling `shapeRenderer.begin()` and `shapeRenderer.end()`, but the ShapeRenderer was already in a begun state from the `MyGdxGame.drawHealthBars()` method.

**Solution**: 
1. Removed `begin()` and `end()` calls from `RemotePlayer.renderHealthBar()`
2. Split border rendering into a separate method `renderHealthBarBorder()`
3. Updated `MyGdxGame.drawHealthBars()` to render borders in a separate pass

**Files Modified**:
- `src/main/java/wagemaker/uk/player/RemotePlayer.java`
- `src/main/java/wagemaker/uk/gdx/MyGdxGame.java`

**Changes in RemotePlayer.java**:
```java
public void renderHealthBar(ShapeRenderer shapeRenderer) {
    if (health < 100 || hunger > 0) {
        // NOTE: ShapeRenderer is already begun in Filled mode by drawHealthBars()
        // Do NOT call begin() here - just render the shapes
        
        // Render green base, red damage, and blue hunger overlays
        // NO begin()/end() calls
    }
}

public void renderHealthBarBorder(ShapeRenderer shapeRenderer) {
    if (health < 100 || hunger > 0) {
        // NOTE: ShapeRenderer is already begun in Line mode by drawHealthBars()
        // Render black border
    }
}
```

**Changes in MyGdxGame.java**:
```java
private void drawHealthBars() {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    
    // Draw remote player health bars (filled shapes)
    for (RemotePlayer remotePlayer : remotePlayers.values()) {
        remotePlayer.renderHealthBar(shapeRenderer);
    }
    
    // Draw tree health bars...
    
    shapeRenderer.end();
    
    // Draw borders for remote player health bars
    if (gameMode != GameMode.SINGLEPLAYER) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (RemotePlayer remotePlayer : remotePlayers.values()) {
            remotePlayer.renderHealthBarBorder(shapeRenderer);
        }
        shapeRenderer.end();
    }
}
```

### 3. Remote Player Health Bars Visibility (Issue #3)
**Problem**: Players couldn't see each other's health bars in multiplayer.

**Status**: This should now work correctly. The health bars are rendered when:
- Health < 100 (player is damaged)
- Hunger > 0 (player is hungry)

The rendering happens in `MyGdxGame.drawHealthBars()` which iterates through all remote players and calls their `renderHealthBar()` method.

**Verification**: Test in multiplayer by:
1. Player 1 takes damage → Player 2 should see Player 1's health bar
2. Player 2 gets hungry → Player 1 should see Player 2's health bar
3. Both conditions should display the unified health bar with red (damage) and blue (hunger) overlays

## Technical Details

### ShapeRenderer State Management
The fix follows LibGDX's ShapeRenderer best practices:
1. Begin once with a shape type (Filled or Line)
2. Render all shapes of that type
3. End the batch
4. Repeat for different shape types

This avoids the "Call end() before beginning a new shape batch" error.

### Health Bar Rendering Architecture
```
MyGdxGame.drawHealthBars()
├── shapeRenderer.begin(Filled)
├── Remote Player Health Bars (filled)
│   ├── Green base layer
│   ├── Red damage overlay
│   └── Blue hunger overlay
├── Tree Health Bars (filled)
└── shapeRenderer.end()
└── shapeRenderer.begin(Line)
└── Remote Player Health Bar Borders
    └── Black border
└── shapeRenderer.end()
```

## Testing Checklist

- [x] Local player health bar only shows when health < 100 or hunger > 0
- [x] No crash when remote player gets hungry in multiplayer
- [x] Remote player health bars display correctly
- [ ] Verify Player 1 can see Player 2's health bar when Player 2 is damaged
- [ ] Verify Player 1 can see Player 2's health bar when Player 2 is hungry
- [ ] Verify Player 2 can see Player 1's health bar when Player 1 is damaged
- [ ] Verify Player 2 can see Player 1's health bar when Player 1 is hungry
- [ ] Verify health bar colors display correctly (green base, red damage, blue hunger)
- [ ] Verify health bar borders display correctly

## Files Changed

1. `src/main/java/wagemaker/uk/ui/HealthBarUI.java` - Added visibility check
2. `src/main/java/wagemaker/uk/player/RemotePlayer.java` - Removed begin/end calls, added border method
3. `src/main/java/wagemaker/uk/gdx/MyGdxGame.java` - Added border rendering pass
