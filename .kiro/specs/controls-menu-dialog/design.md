# Design Document

## Overview

The Controls Menu Dialog feature adds an in-game reference for all keyboard controls accessible from the main game menu. The implementation follows the established dialog pattern used by LanguageDialog and WorldSaveDialog, ensuring visual and architectural consistency. The dialog displays all control bindings organized into logical categories, supports multi-language localization, and integrates seamlessly with the existing menu system.

## Architecture

The feature consists of three main components:

1. **ControlsDialog Class**: A new dialog class that manages the display and interaction logic for the controls reference
2. **GameMenu Integration**: Modifications to GameMenu to add the Controls menu item and handle dialog lifecycle
3. **Localization Resources**: Translation keys added to all supported language files (en, de, nl, pl, pt)

The architecture follows the Model-View-Controller pattern where:
- **Model**: Translation keys and control binding data
- **View**: ControlsDialog rendering logic
- **Controller**: GameMenu input handling and dialog state management

## Components and Interfaces

### ControlsDialog

**Location**: `src/main/java/wagemaker/uk/ui/ControlsDialog.java`

**Responsibilities**:
- Render the controls reference dialog with wooden plank background
- Display all control bindings organized by category
- Handle ESC key input to close the dialog
- Implement LanguageChangeListener for localization support
- Manage dialog visibility state

**Key Methods**:
```java
public ControlsDialog()                                    // Constructor
public void show()                                         // Display the dialog
public void hide()                                         // Hide the dialog
public boolean isVisible()                                 // Check visibility state
public void handleInput()                                  // Process keyboard input
public void render(SpriteBatch, ShapeRenderer, float, float) // Render dialog
public void onLanguageChanged(String)                      // Handle language changes
public void dispose()                                      // Clean up resources
```

**Dependencies**:
- LocalizationManager: For retrieving translated text
- LibGDX Graphics: For texture and font rendering
- LanguageChangeListener: Interface implementation

### GameMenu Modifications

**Location**: `src/main/java/wagemaker/uk/ui/GameMenu.java`

**Changes Required**:
1. Add ControlsDialog field and initialization
2. Add "Controls" to menu items arrays (singleplayer and multiplayer)
3. Increase MENU_HEIGHT constant to accommodate new menu item
4. Add dialog visibility check in update() method
5. Add dialog rendering in render methods
6. Add menu selection handler for Controls option
7. Add dialog disposal in dispose() method

### Localization Resources

**Location**: `assets/localization/*.json`

**New Translation Keys**:
```
controls_dialog.title
controls_dialog.movement_header
controls_dialog.movement_up
controls_dialog.movement_down
controls_dialog.movement_left
controls_dialog.movement_right
controls_dialog.inventory_header
controls_dialog.inventory_open
controls_dialog.inventory_navigate_left
controls_dialog.inventory_navigate_right
controls_dialog.item_header
controls_dialog.item_plant_p
controls_dialog.item_plant_space
controls_dialog.item_consume
controls_dialog.targeting_header
controls_dialog.targeting_up
controls_dialog.targeting_down
controls_dialog.targeting_left
controls_dialog.targeting_right
controls_dialog.combat_header
controls_dialog.combat_attack
controls_dialog.system_header
controls_dialog.system_menu
controls_dialog.system_delete_world
controls_dialog.system_compass_target
controls_dialog.close_instruction
menu.controls
```

## Data Models

### Control Binding Structure

Controls are organized into logical categories for display:

1. **Movement Controls**
   - UP Arrow: Move up
   - DOWN Arrow: Move down
   - LEFT Arrow: Move left
   - RIGHT Arrow: Move right

2. **Inventory Controls**
   - I: Open/close inventory
   - LEFT Arrow: Navigate inventory left
   - RIGHT Arrow: Navigate inventory right

3. **Item Controls**
   - P: Plant item
   - SPACE: Plant/consume item
   - SPACE: Consume item

4. **Targeting Controls**
   - W: Target up
   - A: Target left
   - S: Target down
   - D: Target right

5. **Combat Controls**
   - SPACE: Attack

6. **System Controls**
   - ESC: Open menu
   - X: Delete world
   - TAB: Set compass target

### Dialog Dimensions

```java
private static final float DIALOG_WIDTH = 840;   // Increased by 20% to prevent text overlap in wider languages
private static final float DIALOG_HEIGHT = 480;  // Reduced by 20% for better proportions
```

### Menu Dimensions Update

```java
// In GameMenu.java
private static final float MENU_HEIGHT = 340;  // Increased from 310 to fit Controls item
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Dialog visibility toggles correctly
*For any* dialog state, when show() is called the dialog should become visible, and when hide() is called the dialog should become not visible
**Validates: Requirements 1.2, 1.4**

### Property 2: ESC key closes dialog
*For any* dialog state where isVisible() returns true, simulating ESC key input should result in isVisible() returning false
**Validates: Requirements 1.4**

### Property 3: Dialog prevents game input
*For any* game state where the Controls Dialog is visible, game input handlers should not process player movement or action inputs
**Validates: Requirements 1.3**

### Property 4: All controls are displayed
*For any* rendering of the Controls Dialog, the rendered text should contain descriptions for all defined control bindings (movement, inventory, items, targeting, combat, system)
**Validates: Requirements 2.1-2.9**

### Property 5: Dialog centers on camera
*For any* camera position (camX, camY), the dialog X position should equal camX - DIALOG_WIDTH/2 and dialog Y position should equal camY - DIALOG_HEIGHT/2
**Validates: Requirements 3.4**

### Property 6: Language changes update text
*For any* supported language, when setLanguage() is called on LocalizationManager, the Controls Dialog should retrieve and display text in the new language
**Validates: Requirements 1.5, 4.5**

### Property 7: Translation keys exist in all languages
*For any* translation key defined for the Controls Dialog, that key should exist in all supported language files (en.json, de.json, nl.json, pl.json, pt.json)
**Validates: Requirements 5.1, 5.2-5.6**

### Property 8: No hardcoded strings in rendering
*For any* text displayed in the Controls Dialog, that text should be retrieved from LocalizationManager using a translation key, not hardcoded as a string literal
**Validates: Requirements 5.7**

### Property 9: Listener registration lifecycle
*For any* ControlsDialog instance, after construction the dialog should be registered with LocalizationManager, and after dispose() the dialog should be unregistered
**Validates: Requirements 4.3, 4.4**

## Error Handling

### Input Validation
- No user input validation required (dialog is read-only)
- ESC key handling is fail-safe (always closes dialog)

### Resource Loading
- Font loading failure: Fall back to default LibGDX BitmapFont
- Texture creation failure: Log error and use fallback solid color background
- Translation key missing: LocalizationManager returns key wrapped in brackets [key]

### State Management
- Dialog visibility state is boolean (no invalid states possible)
- Multiple show() calls are idempotent (no side effects)
- Multiple hide() calls are idempotent (no side effects)

### Disposal
- Texture disposal is guarded by null checks
- Font disposal is guarded by null checks
- Listener unregistration is safe even if not registered

## Testing Strategy

### Unit Tests

Unit tests will verify specific examples and edge cases:

1. **Dialog Construction Test**
   - Verify dialog initializes with isVisible() = false
   - Verify wooden plank texture is created
   - Verify font is loaded or fallback is used

2. **Show/Hide Test**
   - Verify show() sets isVisible() to true
   - Verify hide() sets isVisible() to false
   - Verify multiple show() calls are idempotent

3. **Translation Key Existence Test**
   - Verify all required translation keys exist in en.json
   - Verify all required translation keys exist in all language files
   - Verify no translation keys are missing

4. **Menu Integration Test**
   - Verify "Controls" menu item is added to singleplayer menu
   - Verify "Controls" menu item is added to multiplayer menu
   - Verify MENU_HEIGHT is increased appropriately

### Property-Based Tests

Property-based tests will verify universal properties across all inputs:

**Testing Framework**: We will use [junit-quickcheck](https://github.com/pholser/junit-quickcheck) for Java property-based testing, which integrates with JUnit and provides generators for common types.

**Configuration**: Each property-based test will run a minimum of 100 iterations to ensure thorough coverage of the input space.

**Test Tagging**: Each property-based test will include a comment tag in the format:
```java
// Feature: controls-menu-dialog, Property N: [property description]
```

1. **Property Test: Dialog Visibility Toggle**
   - Generate random sequences of show()/hide() calls
   - Verify isVisible() state matches expected state after each call
   - **Validates Property 1**

2. **Property Test: ESC Key Closes Dialog**
   - Generate random dialog states (visible/hidden)
   - When visible, simulate ESC input and verify dialog closes
   - **Validates Property 2**

3. **Property Test: Dialog Centering**
   - Generate random camera positions
   - Verify dialog X/Y coordinates are correctly centered
   - **Validates Property 5**

4. **Property Test: Language Change Updates**
   - Generate random language selections from supported languages
   - Verify dialog text changes when language changes
   - **Validates Property 6**

5. **Property Test: Translation Key Completeness**
   - For each required translation key
   - Verify key exists in all language files
   - **Validates Property 7**

6. **Property Test: No Hardcoded Strings**
   - Parse ControlsDialog source code
   - Verify all displayed text uses LocalizationManager.getText()
   - **Validates Property 8**

### Integration Tests

Integration tests will verify the feature works correctly with other system components:

1. **Menu Navigation Integration Test**
   - Open game menu
   - Navigate to Controls item
   - Press Enter and verify dialog opens
   - Press ESC and verify dialog closes

2. **Localization Integration Test**
   - Open Controls Dialog
   - Change language via Language Dialog
   - Verify Controls Dialog text updates automatically

3. **Multi-Dialog Integration Test**
   - Open Controls Dialog
   - Verify other dialogs cannot be opened simultaneously
   - Verify game menu remains accessible after closing dialog

## Implementation Notes

### Visual Consistency

The ControlsDialog must match the visual style of existing dialogs:
- Use same wooden plank texture generation algorithm
- Use same font (Sancreek-Regular.ttf) with size 16
- Use same color scheme (white for labels, yellow for highlights, light gray for instructions)
- Use same border styling (dark brown, 2px thick)

### Performance Considerations

- Texture and font are created once in constructor, not per-frame
- Translation text is retrieved once per render, cached in local variables
- No complex calculations in render loop
- Dialog only renders when visible

### Accessibility

- All text uses high-contrast colors (white/yellow on dark brown)
- Font size is 16pt for readability
- Controls are organized into clear categories with headers
- Simple keyboard navigation (only ESC key required)

### Future Extensibility

The design allows for future enhancements:
- Adding rebindable controls (would require additional UI for key binding)
- Adding controller support (would add new control categories)
- Adding tooltips or help text for complex controls
- Adding search/filter functionality for large control lists
