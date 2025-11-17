# Design Document

## Overview

This feature extends the existing compass system to support custom target coordinates. Currently, the compass always points to world origin (0,0). This enhancement adds a "Player Location" menu option that opens a dialog displaying the player's current coordinates and allowing them to set a custom target for the compass to point toward.

The design follows the existing UI patterns in the game, using wooden plank backgrounds, the Sancreek font, and the established dialog system architecture.

## Architecture

### Component Structure

```
GameMenu (existing)
├── Player Location Menu Item (new)
└── PlayerLocationDialog (new)
    ├── Display current coordinates
    ├── Input fields for target coordinates
    └── Reset to origin button

Compass (existing - modified)
├── Current rotation logic
└── Custom target support (new)
    ├── Target coordinates (x, y)
    └── Update method with custom target

PlayerConfig (existing - extended)
├── lastServer (existing)
└── compassTarget (new)
    ├── targetX
    └── targetY
```

### Integration Points

1. **GameMenu**: Add new menu item "Player Location" below "Player Name"
2. **Compass**: Extend to support custom target coordinates
3. **PlayerConfig**: Add compass target persistence
4. **Localization**: Add new text keys for the dialog

## Components and Interfaces

### 1. PlayerLocationDialog

A new dialog class following the pattern of existing dialogs (NameDialog, ConnectDialog, etc.).

**Responsibilities:**
- Display current player coordinates (read-only)
- Provide input fields for target X and Y coordinates
- Validate numeric input (including negative values)
- Handle confirmation and cancellation
- Trigger compass target updates

**Key Methods:**
```java
public class PlayerLocationDialog {
    private boolean isVisible;
    private String targetXInput;
    private String targetYInput;
    private Player player;
    private Compass compass;
    private int activeField; // 0 = targetX, 1 = targetY
    
    public void show(Player player, Compass compass);
    public void hide();
    public void handleInput();
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, float camX, float camY);
    public boolean isVisible();
    private boolean validateCoordinates();
    private void applyCustomTarget();
    private void resetToOrigin();
}
```

**UI Layout:**
```
┌────────────────────────────────┐
│     Player Coordinates         │
│                                │
│  Current Location              │
│  x: 12345    y: -12345         │
│                                │
│  New Target Location           │
│  x: [_______]  y: [_______]    │
│                                │
│  [Set Target] [Reset Origin]   │
│                                │
│  Tab to switch, Enter to set   │
│  ESC to cancel                 │
└────────────────────────────────┘
```

**Visual Design:**
- Wooden plank background (400x300 pixels)
- Sancreek font for title and labels
- Yellow highlight for active input field
- White text for current coordinates
- Gray text for instructions

### 2. Compass (Modified)

Extend the existing Compass class to support custom targets.

**New Fields:**
```java
private float targetX = 0.0f;
private float targetY = 0.0f;
private boolean useCustomTarget = false;
```

**Modified Methods:**
```java
// Existing method signature remains the same for backward compatibility
public void update(float playerX, float playerY, float spawnX, float spawnY) {
    float targetX = useCustomTarget ? this.targetX : spawnX;
    float targetY = useCustomTarget ? this.targetY : spawnY;
    
    // Calculate vector from player to target
    float deltaX = targetX - playerX;
    float deltaY = targetY - playerY;
    
    // Calculate angle and update rotation
    float angleRadians = (float) Math.atan2(deltaY, deltaX);
    currentRotation = (float) Math.toDegrees(angleRadians);
}

// New methods
public void setCustomTarget(float x, float y) {
    this.targetX = x;
    this.targetY = y;
    this.useCustomTarget = true;
}

public void resetToOrigin() {
    this.useCustomTarget = false;
}

public boolean hasCustomTarget() {
    return useCustomTarget;
}

public float getTargetX() {
    return useCustomTarget ? targetX : 0.0f;
}

public float getTargetY() {
    return useCustomTarget ? targetY : 0.0f;
}
```

### 3. GameMenu (Modified)

Add the new menu item and integrate the PlayerLocationDialog.

**Changes:**
1. Add "Player Location" to menu items arrays (after "Player Name")
2. Create PlayerLocationDialog instance
3. Add dialog handling in update() and render() methods
4. Add executeMenuItem case for "Player Location"
5. Pass Compass reference to GameMenu via setter

**New Methods:**
```java
private PlayerLocationDialog playerLocationDialog;

public void setCompass(Compass compass) {
    this.compass = compass;
}

private void openPlayerLocationDialog() {
    isOpen = false;
    playerLocationDialog.show(player, compass);
}
```

### 4. PlayerConfig (Extended)

Add compass target persistence to the existing configuration system.

**New Fields:**
```java
private Float compassTargetX;
private Float compassTargetY;
```

**New Methods:**
```java
public Float getCompassTargetX() {
    return compassTargetX;
}

public Float getCompassTargetY() {
    return compassTargetY;
}

public void saveCompassTarget(Float x, Float y) {
    this.compassTargetX = x;
    this.compassTargetY = y;
    save();
}

public void clearCompassTarget() {
    this.compassTargetX = null;
    this.compassTargetY = null;
    save();
}
```

**JSON Format:**
```json
{
  "lastServer": "192.168.1.100:25565",
  "compassTarget": {
    "x": 12345.0,
    "y": -6789.0
  }
}
```

### 5. MyGdxGame (Modified)

Initialize and wire up the compass target on game start.

**Changes:**
1. Pass Compass reference to GameMenu
2. Load compass target from PlayerConfig on initialization
3. Apply saved target to Compass if it exists

**Initialization Logic:**
```java
// In create() or initialization method
PlayerConfig config = PlayerConfig.load();
Float targetX = config.getCompassTargetX();
Float targetY = config.getCompassTargetY();

if (targetX != null && targetY != null) {
    compass.setCustomTarget(targetX, targetY);
}

gameMenu.setCompass(compass);
```

## Data Models

### CompassTarget

Represents a custom compass target coordinate.

```java
public class CompassTarget {
    private float x;
    private float y;
    
    public CompassTarget(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public float getX() { return x; }
    public float getY() { return y; }
}
```

Note: This is a simple data structure that may be represented directly as fields rather than a separate class, depending on implementation preference.

## Error Handling

### Input Validation

1. **Empty Input**: Allow empty fields (treated as 0)
2. **Non-Numeric Input**: Display error message, prevent confirmation
3. **Out of Range**: Accept any float value (no artificial limits)
4. **Negative Values**: Fully supported

**Validation Logic:**
```java
private boolean validateCoordinates() {
    try {
        // Empty strings default to 0
        float x = targetXInput.isEmpty() ? 0 : Float.parseFloat(targetXInput);
        float y = targetYInput.isEmpty() ? 0 : Float.parseFloat(targetYInput);
        return true;
    } catch (NumberFormatException e) {
        return false;
    }
}
```

### Error Messages

Display inline error indicators:
- Red text color for invalid input
- Error message: "Invalid number" below input field
- Disable "Set Target" button when validation fails

### Configuration Persistence Errors

Follow existing PlayerConfig error handling:
- Log errors to console
- Continue operation even if save fails
- Display warning to user if critical

## Testing Strategy

### Unit Tests

1. **Compass Target Logic**
   - Test setCustomTarget() updates target coordinates
   - Test resetToOrigin() clears custom target
   - Test update() calculates correct angle with custom target
   - Test update() uses origin when no custom target set

2. **Input Validation**
   - Test valid integer inputs
   - Test valid decimal inputs
   - Test negative values
   - Test invalid inputs (letters, symbols)
   - Test empty inputs (should default to 0)

3. **PlayerConfig Persistence**
   - Test saving compass target
   - Test loading compass target
   - Test clearing compass target
   - Test backward compatibility (config without compass target)

### Integration Tests

1. **Dialog Flow**
   - Open dialog from menu
   - Enter coordinates and confirm
   - Verify compass updates
   - Verify config saves

2. **Reset Functionality**
   - Set custom target
   - Reset to origin
   - Verify compass points to (0,0)
   - Verify config clears target

3. **Session Persistence**
   - Set custom target
   - Save and exit game
   - Restart game
   - Verify compass still points to custom target

### Manual Testing

1. **UI/UX Testing**
   - Verify dialog centers on screen
   - Verify wooden plank background renders correctly
   - Verify text is readable and properly aligned
   - Verify input fields respond to keyboard input
   - Verify Tab switches between fields

2. **Multiplayer Testing**
   - Verify custom target works in multiplayer
   - Verify each player can have different targets
   - Verify target persists across reconnects

3. **Edge Cases**
   - Very large coordinate values (999999)
   - Very small coordinate values (-999999)
   - Decimal precision (12345.67)
   - Rapid target changes

## Localization

### New Text Keys

Add to all localization files (en.json, de.json, nl.json, pl.json, pt.json):

```json
{
  "menu": {
    "player_location": "Player Location"
  },
  "player_location_dialog": {
    "title": "Player Coordinates",
    "current_location": "Current Location",
    "new_target": "New Target Location",
    "set_target": "Set Target",
    "reset_origin": "Reset to Origin",
    "tab_instruction": "Tab to switch fields",
    "confirm_instruction": "Enter to set, ESC to cancel",
    "invalid_number": "Invalid number",
    "target_set": "Compass target set to ({0}, {1})",
    "target_reset": "Compass reset to origin (0, 0)"
  }
}
```

## UI Specifications

### Dialog Dimensions
- Width: 400 pixels
- Height: 300 pixels
- Position: Centered on screen

### Font Specifications
- Title: Sancreek 18pt, White
- Labels: Sancreek 14pt, White
- Current Coordinates: Sancreek 16pt, Yellow
- Input Fields: Sancreek 16pt, Yellow (active), White (inactive)
- Instructions: Sancreek 12pt, Light Gray
- Error Text: Sancreek 12pt, Red

### Spacing
- Title margin top: 30px
- Section spacing: 40px
- Label to value spacing: 10px
- Input field width: 120px
- Button spacing: 20px
- Bottom instruction margin: 20px

### Input Field Behavior
- Active field: Yellow text with underscore cursor
- Inactive field: White text
- Tab key: Switch between X and Y fields
- Backspace: Delete last character
- Enter: Confirm and apply target
- ESC: Cancel and close dialog
- Accept: digits, decimal point, minus sign
- Max length: 10 characters per field

## Performance Considerations

### Rendering
- Dialog only renders when visible (no performance impact when closed)
- Reuse existing wooden plank texture generation pattern
- No additional texture loading required

### Compass Updates
- No performance impact - same calculation complexity
- Custom target check is a simple boolean flag
- No additional memory allocation per frame

### Configuration I/O
- Save only when target changes (not every frame)
- Async save operation (non-blocking)
- Minimal JSON size increase (~50 bytes)

## Backward Compatibility

### Configuration Migration
- Existing configs without compassTarget continue to work
- Missing compassTarget treated as "use origin"
- No migration script needed

### Compass Behavior
- Default behavior unchanged (points to origin)
- Custom target is opt-in feature
- Existing compass update calls work without modification

## Future Enhancements

Potential future improvements (not in current scope):

1. **Waypoint System**: Save multiple named waypoints
2. **Distance Display**: Show distance to target on compass
3. **Waypoint Sharing**: Share waypoints with other players via chat
4. **Map Integration**: Visual map showing player and target locations
5. **Quick Waypoints**: Hotkey to set current location as waypoint
6. **Compass Modes**: Toggle between different display modes (minimal, detailed)
