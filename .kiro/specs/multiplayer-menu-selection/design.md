# Design Document

## Overview

This design addresses two issues in the Woodlanders game's multiplayer menu system:

1. **Duplicate Event Handling**: The game was handling multiplayer menu selections in two places (GameMenu and MyGdxGame), causing the "Host Server" action to trigger immediately when the multiplayer menu opened.

2. **Player Movement During Menu Navigation**: When any menu or dialog is open, the player character continues to respond to arrow key inputs, causing unwanted movement while navigating menus.

The solution involves proper event delegation between GameMenu and MyGdxGame, and implementing a comprehensive menu state check to pause all game interactions when any UI element is active.

## Architecture

### Component Interaction Flow

```
User Input → GameMenu → MyGdxGame
                ↓
         Menu State Check
                ↓
         Player.update() (only if no menu open)
```

### Key Components

1. **GameMenu**: Manages all UI elements (main menu, multiplayer menu, dialogs) and provides a unified state check
2. **MyGdxGame**: Game loop that checks menu state before updating game entities
3. **Player**: Handles player movement and actions (only when not paused by menu)
4. **MultiplayerMenu**: Submenu for multiplayer options
5. **Dialogs**: Various dialogs (error, connect, server host, name input)

## Components and Interfaces

### GameMenu Enhancements

**New Method:**
```java
public boolean isAnyMenuOpen()
```

This method consolidates all menu/dialog state checks into a single query that returns true if ANY of the following are open:
- Main menu (`isOpen`)
- Name dialog (`nameDialogOpen`)
- Multiplayer menu (`multiplayerMenu.isOpen()`)
- Error dialog (`errorDialog.isVisible()`)
- Connect dialog (`connectDialog.isVisible()`)
- Server host dialog (`serverHostDialog.isVisible()`)

**Existing Enhancement:**
```java
private wagemaker.uk.gdx.MyGdxGame gameInstance;

public void setGameInstance(wagemaker.uk.gdx.MyGdxGame gameInstance) {
    this.gameInstance = gameInstance;
}
```

This allows GameMenu to call back to MyGdxGame methods (like `attemptHostServer()`) when menu options are selected.

### MyGdxGame Modifications

**Initialization:**
```java
gameMenu.setGameInstance(this); // Set reference during create()
```

**Event Handling Cleanup:**
Remove duplicate multiplayer menu handling from render() method. All menu selections should be handled exclusively by GameMenu.

**Game State Update:**
```java
if (!gameMenu.isAnyMenuOpen()) {
    player.update(deltaTime);
    // Update other game entities...
}
```

Replace the current `!gameMenu.isOpen()` check with `!gameMenu.isAnyMenuOpen()` to ensure ALL menus pause the game.

### Player Class

No changes required. The Player class already receives input through `Gdx.input.isKeyPressed()`, which will simply not be processed when `player.update()` is not called.

## Data Models

### Menu State

The menu state is distributed across multiple boolean flags and object states:

```
GameMenu State:
├── isOpen: boolean (main menu)
├── nameDialogOpen: boolean (name input dialog)
├── multiplayerMenu.isOpen(): boolean (multiplayer submenu)
├── errorDialog.isVisible(): boolean (error dialog)
├── connectDialog.isVisible(): boolean (connection dialog)
└── serverHostDialog.isVisible(): boolean (server info dialog)
```

The new `isAnyMenuOpen()` method aggregates these into a single boolean result.

## Error Handling

### Null Safety

The `handleMultiplayerMenuSelection()` method includes null checking:

```java
if (gameInstance != null) {
    gameInstance.attemptHostServer();
} else {
    System.err.println("Cannot host server: game instance not set");
}
```

This prevents crashes if the game instance reference is not properly initialized.

### Event Handling Priority

The GameMenu.update() method processes events in priority order:
1. Error dialog (highest priority)
2. Connect dialog
3. Server host dialog
4. Name dialog
5. Multiplayer menu
6. Main menu (lowest priority)

This ensures that modal dialogs always take precedence over background menus.

## Implementation Details

### Event Delegation Pattern

The design uses a callback pattern where:
1. GameMenu handles all UI input and state
2. GameMenu calls back to MyGdxGame for game-level actions
3. MyGdxGame never directly checks menu input states

This creates a clean separation of concerns:
- **GameMenu**: UI logic and user interaction
- **MyGdxGame**: Game state and multiplayer networking

### Input Blocking

When `isAnyMenuOpen()` returns true:
- Player movement is blocked (update not called)
- Tree updates continue (visual animations)
- Remote player updates continue (multiplayer sync)
- Camera remains at current position
- Rendering continues normally

This creates a "paused" effect where the game world is visible but not interactive.

### Arrow Key Disambiguation

Arrow keys serve dual purposes:
- **Menu Open**: Navigate menu options (UP/DOWN)
- **Menu Closed**: Move player character (UP/DOWN/LEFT/RIGHT)

The disambiguation is handled by the update order:
1. GameMenu.update() processes input first
2. If menu is open, GameMenu consumes the input
3. MyGdxGame checks `isAnyMenuOpen()` before calling player.update()
4. If menu is open, player.update() is never called, so arrow keys only affect menu

## Testing Strategy

### Manual Testing Scenarios

1. **Menu Navigation Without Player Movement**
   - Open main menu (ESC)
   - Press UP/DOWN arrows
   - Verify: Menu selection changes, player does not move
   - Close menu
   - Press UP/DOWN arrows
   - Verify: Player moves, menu does not open

2. **Multiplayer Menu Flow**
   - Open main menu (ESC)
   - Select "Multiplayer" (ENTER)
   - Verify: Multiplayer menu opens, main menu closes
   - Press UP/DOWN to navigate
   - Verify: Menu selection changes, player does not move
   - Select "Host Server" (ENTER)
   - Verify: Server starts, server info dialog appears
   - Press ESC
   - Verify: Dialog closes, returns to game

3. **Dialog Interaction**
   - Open multiplayer menu
   - Select "Connect to Server"
   - Verify: Connect dialog opens
   - Type server address
   - Verify: Player does not move while typing
   - Press ESC to cancel
   - Verify: Returns to multiplayer menu

4. **All Menu States**
   - Test each menu/dialog type:
     - Main menu
     - Multiplayer menu
     - Name dialog
     - Connect dialog
     - Server host dialog
     - Error dialog
   - For each, verify:
     - Arrow keys navigate menu only
     - Player does not move
     - Game world remains visible
     - ESC closes menu appropriately

### Integration Testing

The existing multiplayer integration tests should continue to pass:
- `testClientConnectionFlow()`
- `testWorldStateSynchronization()`
- `testPlayerMovementReplication()`
- etc.

No new tests are required as the changes are UI-focused and don't affect multiplayer networking logic.

### Edge Cases

1. **Rapid Menu Toggling**: Pressing ESC rapidly should not cause state inconsistencies
2. **Multiple Dialogs**: Only one dialog should be visible at a time (handled by priority order)
3. **Menu Open During Multiplayer**: Remote players should continue to move while local player menu is open
4. **Connection Loss During Menu**: Menu should remain functional even if connection is lost

## Performance Considerations

### Minimal Overhead

The `isAnyMenuOpen()` method performs 6 boolean checks, which is negligible overhead:
- Called once per frame in render loop
- No object allocation
- No complex computation
- O(1) time complexity

### Rendering Optimization

When menus are open:
- Game world continues to render (no performance change)
- Entity updates are skipped (slight performance improvement)
- Menu rendering is additive (minimal overhead)

The design maintains 60 FPS even with menus open.

## Future Enhancements

### Potential Improvements

1. **Pause Overlay**: Add a semi-transparent overlay when menus are open to visually indicate paused state
2. **Animation Pause**: Pause tree animations and remote player animations when menu is open
3. **Menu Stack**: Implement a proper menu stack for better back navigation
4. **Input Mapping**: Allow players to rebind keys, including menu navigation keys
5. **Gamepad Support**: Extend menu navigation to support gamepad input

### Refactoring Opportunities

1. **Menu State Manager**: Create a dedicated MenuStateManager class to centralize all menu state logic
2. **Event Bus**: Implement an event bus for menu events instead of direct callbacks
3. **UI Framework**: Consider using a UI framework like Scene2D for more complex menu layouts
