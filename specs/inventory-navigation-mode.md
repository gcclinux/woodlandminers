# Inventory Navigation Mode - Feature Specification

## Overview
Replace the current number key (1-7) inventory selection system with a modal navigation system activated by the "I" key. When active, arrow keys navigate between inventory slots, and player movement is disabled. This enables support for inventories with more than 9 items.

## Problem Statement
The current inventory selection system uses number keys (1-7) to select items directly. This approach:
- Limits inventory to 9 items maximum (keys 1-9)
- Cannot scale beyond single-digit keys
- Conflicts with potential future features that might use number keys
- Doesn't provide a clear visual indication of "inventory mode"

## Proposed Solution
Implement a modal inventory navigation system:
1. Press "I" key to enter inventory navigation mode
2. Player movement stops (arrow keys no longer move player)
3. Arrow keys (UP/DOWN/LEFT/RIGHT) navigate between inventory slots
4. Visual indicator shows current selection
5. Press "I" again or ESC to exit inventory mode and resume movement
6. Works identically in singleplayer and multiplayer modes

## Requirements

### Functional Requirements

#### FR1: Inventory Mode Activation
- **FR1.1**: Pressing "I" key toggles inventory navigation mode ON/OFF
- **FR1.2**: When mode is ON, player movement is disabled
- **FR1.3**: When mode is OFF, player movement resumes normally
- **FR1.4**: ESC key exits inventory mode (in addition to "I" toggle)
- **FR1.5**: Mode state persists until explicitly toggled or cancelled

#### FR2: Arrow Key Navigation
- **FR2.1**: RIGHT arrow moves selection to next slot (wraps to first slot after last)
- **FR2.2**: LEFT arrow moves selection to previous slot (wraps to last slot from first)
- **FR2.3**: UP arrow moves selection up one row (if multi-row layout)
- **FR2.4**: DOWN arrow moves selection down one row (if multi-row layout)
- **FR2.5**: Navigation only works when inventory mode is active

#### FR3: Item Selection Behavior
- **FR3.1**: Navigating to a slot automatically selects that item
- **FR3.2**: Selected item shows visual highlight (yellow box)
- **FR3.3**: Selecting same slot again deselects it
- **FR3.4**: Exiting inventory mode preserves current selection
- **FR3.5**: Auto-deselection when item count reaches 0 still works

#### FR4: Player Movement Control
- **FR4.1**: When inventory mode is ON, arrow keys do NOT move player
- **FR4.2**: When inventory mode is OFF, arrow keys move player normally
- **FR4.3**: Other keys (SPACE, A/W/D/S for targeting) work normally
- **FR4.4**: Menu opening (ESC) takes priority over inventory mode

#### FR5: Multiplayer Compatibility
- **FR5.1**: System works identically in singleplayer and multiplayer
- **FR5.2**: No network synchronization needed (client-side only)
- **FR5.3**: Item selection state syncs via existing inventory system
- **FR5.4**: No impact on existing multiplayer inventory messages

### Non-Functional Requirements

#### NFR1: Performance
- Navigation response time < 50ms
- No frame rate impact when toggling modes
- Minimal memory overhead (single boolean flag)

#### NFR2: Usability
- Clear visual feedback for inventory mode state
- Intuitive navigation (follows standard UI patterns)
- No learning curve for existing players

#### NFR3: Compatibility
- Works with existing targeting system
- Compatible with existing item consumption
- No breaking changes to save files or network protocol

## Technical Design

### Architecture

#### Component: InventoryNavigationMode
**Location**: `Player.java`

**State Variables**:
```java
private boolean inventoryNavigationMode = false;  // Is inventory mode active?
private int currentInventorySlot = -1;            // Currently highlighted slot (-1 = none)
```

**Key Methods**:
```java
// Toggle inventory navigation mode
private void toggleInventoryNavigationMode()

// Handle arrow key navigation in inventory mode
private void handleInventoryNavigation()

// Check if player movement should be blocked
private boolean isMovementBlocked()
```

### Implementation Details

#### Phase 1: Mode Toggle (FR1)
**File**: `Player.java` - `update()` method

**Changes**:
1. Add inventory mode state variable
2. Detect "I" key press to toggle mode
3. Detect ESC key to exit mode (when not in menu)
4. Update mode state flag

**Code Location**: After menu check, before movement handling

#### Phase 2: Movement Blocking (FR4)
**File**: `Player.java` - `update()` method

**Changes**:
1. Wrap arrow key movement code in mode check
2. Skip movement processing when inventory mode is ON
3. Preserve existing collision detection logic

**Code Pattern**:
```java
if (!inventoryNavigationMode) {
    // Existing movement code
    if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) { ... }
    if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) { ... }
    // etc.
}
```

#### Phase 3: Navigation Logic (FR2, FR3)
**File**: `Player.java` - new method `handleInventoryNavigation()`

**Changes**:
1. Create new method for inventory navigation
2. Detect arrow key presses (isKeyJustPressed for single-step)
3. Calculate next slot based on direction
4. Update inventoryManager selection
5. Handle wrap-around logic

**Navigation Logic**:
- Current layout: 7 slots in single row
- RIGHT: `(currentSlot + 1) % 7`
- LEFT: `(currentSlot - 1 + 7) % 7`
- UP/DOWN: Reserved for future multi-row support

#### Phase 4: Remove Number Keys (FR3)
**File**: `Player.java` - `handleInventorySelection()` method

**Changes**:
1. Remove all number key detection (NUM_1 through NUM_7)
2. Keep toggle logic for selection/deselection
3. Preserve targeting system activation
4. Keep auto-deselection logic

### Data Flow

```
User presses "I"
    ↓
Toggle inventoryNavigationMode = true
    ↓
Player movement blocked
    ↓
User presses RIGHT arrow
    ↓
handleInventoryNavigation() called
    ↓
Calculate next slot: (current + 1) % 7
    ↓
inventoryManager.setSelectedSlot(nextSlot)
    ↓
Visual highlight updates (existing code)
    ↓
Targeting system activates if plantable (existing code)
    ↓
User presses "I" again
    ↓
Toggle inventoryNavigationMode = false
    ↓
Player movement resumes
```

### Edge Cases

#### EC1: Menu Interaction
**Scenario**: Player opens menu while in inventory mode
**Behavior**: Inventory mode exits automatically when menu opens
**Implementation**: Check `gameMenu.isAnyMenuOpen()` before processing inventory mode

#### EC2: Item Depletion
**Scenario**: Selected item count reaches 0 during inventory mode
**Behavior**: Auto-deselection works normally, inventory mode stays active
**Implementation**: No changes needed - existing auto-deselection handles this

#### EC3: Targeting Active
**Scenario**: Player enters inventory mode while targeting is active
**Behavior**: Targeting continues to work with A/W/D/S keys
**Implementation**: Targeting input handling is separate from movement

#### EC4: Multiplayer Disconnect
**Scenario**: Player disconnects while in inventory mode
**Behavior**: Mode resets on reconnect
**Implementation**: Mode is client-side only, no persistence needed

## Testing Strategy

### Unit Tests
**File**: `PlayerTest.java` (new)

**Test Cases**:
1. `testInventoryModeToggle()` - Verify "I" key toggles mode
2. `testMovementBlockedInInventoryMode()` - Verify arrow keys don't move player
3. `testNavigationInInventoryMode()` - Verify arrow keys navigate slots
4. `testWrapAroundNavigation()` - Verify wrap-around at boundaries
5. `testEscExitsInventoryMode()` - Verify ESC exits mode
6. `testSelectionPersistsAfterExit()` - Verify selection preserved

### Integration Tests
**Manual Testing**:

1. **Basic Navigation**
   - Press "I" → mode activates
   - Press RIGHT 7 times → cycles through all slots
   - Press LEFT 7 times → cycles backward
   - Press "I" → mode deactivates

2. **Movement Blocking**
   - Enter inventory mode
   - Press arrow keys → player doesn't move
   - Exit inventory mode
   - Press arrow keys → player moves normally

3. **Item Selection**
   - Navigate to slot with item
   - Item highlights and targeting activates (if plantable)
   - Navigate to different slot
   - Previous item deselects, new item selects

4. **Menu Interaction**
   - Enter inventory mode
   - Press ESC → menu opens, inventory mode exits
   - Close menu → inventory mode stays off

5. **Multiplayer**
   - Connect to server
   - Enter inventory mode
   - Navigate and select items
   - Verify behavior identical to singleplayer

### Acceptance Criteria

✅ **AC1**: Pressing "I" toggles inventory navigation mode
✅ **AC2**: Arrow keys navigate inventory when mode is ON
✅ **AC3**: Arrow keys move player when mode is OFF
✅ **AC4**: ESC exits inventory mode
✅ **AC5**: Selection persists after exiting mode
✅ **AC6**: Works identically in singleplayer and multiplayer
✅ **AC7**: No number keys (1-7) for inventory selection
✅ **AC8**: Visual feedback shows current selection
✅ **AC9**: Targeting system works with selected items
✅ **AC10**: Auto-deselection works when item count reaches 0

## Implementation Plan

### Deliverables

#### D1: Core Mode Toggle
**Files**: `Player.java`
**Estimated Effort**: 30 minutes
**Tasks**:
- Add `inventoryNavigationMode` boolean field
- Add `toggleInventoryNavigationMode()` method
- Detect "I" key press in `update()`
- Detect ESC key to exit mode

#### D2: Movement Blocking
**Files**: `Player.java`
**Estimated Effort**: 15 minutes
**Tasks**:
- Wrap arrow key movement in mode check
- Add `isMovementBlocked()` helper method
- Test movement blocking

#### D3: Navigation Logic
**Files**: `Player.java`
**Estimated Effort**: 45 minutes
**Tasks**:
- Create `handleInventoryNavigation()` method
- Implement RIGHT/LEFT navigation with wrap-around
- Implement UP/DOWN (reserved for future)
- Call from `update()` when mode is active

#### D4: Remove Number Keys
**Files**: `Player.java`
**Estimated Effort**: 15 minutes
**Tasks**:
- Remove NUM_1 through NUM_7 detection
- Remove toggle logic for number keys
- Preserve targeting activation logic

#### D5: Testing & Polish
**Files**: `Player.java`, manual testing
**Estimated Effort**: 30 minutes
**Tasks**:
- Test all navigation scenarios
- Test multiplayer compatibility
- Test edge cases (menu, targeting, depletion)
- Verify no regressions

**Total Estimated Effort**: 2 hours 15 minutes

### Rollout Plan

#### Phase 1: Development (Week 1)
- Implement D1-D4
- Internal testing

#### Phase 2: Testing (Week 1)
- Complete D5
- Multiplayer testing
- Edge case validation

#### Phase 3: Release (Week 1)
- Merge to main branch
- Update documentation
- Release notes

## Success Metrics

### Quantitative
- 0 regressions in existing inventory functionality
- 0 multiplayer desync issues
- < 50ms navigation response time
- 100% test coverage for new code

### Qualitative
- Players can navigate inventory without number keys
- System feels intuitive and responsive
- No confusion about when inventory mode is active
- Scales to support future inventory expansion (10+ items)

## Future Enhancements

### Multi-Row Layout
When inventory exceeds 7 items:
- Arrange in 2+ rows
- UP/DOWN arrows navigate between rows
- Maintain column position when changing rows

### Visual Mode Indicator
Add UI element showing inventory mode state:
- Border around inventory panel
- "INVENTORY MODE" text overlay
- Different background color

### Quick-Select Shortcuts
Hybrid approach:
- "I" + number key for direct selection
- Maintains modal navigation for 10+ items
- Best of both worlds

## Dependencies

### Internal
- `InventoryManager.java` - Selection state management
- `InventoryRenderer.java` - Visual highlight rendering
- `TargetingSystem.java` - Targeting activation
- `GameMenu.java` - Menu state checking

### External
- libGDX Input API - Key detection
- No new external dependencies

## Risks & Mitigation

### Risk 1: Player Confusion
**Risk**: Players don't understand when inventory mode is active
**Mitigation**: Add visual indicator (future enhancement)
**Severity**: Low

### Risk 2: Muscle Memory
**Risk**: Existing players used to number keys
**Mitigation**: Clear release notes, tutorial update
**Severity**: Medium

### Risk 3: Targeting Conflicts
**Risk**: Arrow keys conflict with targeting (A/W/D/S)
**Mitigation**: Targeting uses different keys, no conflict
**Severity**: Low

### Risk 4: Multiplayer Desync
**Risk**: Mode state causes inventory desync
**Mitigation**: Mode is client-side only, no network impact
**Severity**: Very Low

## Documentation Updates

### User-Facing
- **README.md**: Update Controls section
- **docs/FEATURES.md**: Update Inventory section
- In-game tutorial: Add inventory mode explanation

### Developer-Facing
- **docs/CLASSES.md**: Document new Player methods
- **guidelines.md**: Add inventory navigation pattern
- Code comments: Document mode state and navigation logic

## Conclusion

This specification provides a complete blueprint for implementing modal inventory navigation. The system is:
- **Scalable**: Supports unlimited inventory slots
- **Intuitive**: Follows standard UI navigation patterns
- **Compatible**: Works with all existing systems
- **Performant**: Minimal overhead, client-side only
- **Testable**: Clear acceptance criteria and test cases

The implementation is straightforward, with minimal risk and clear deliverables. Total effort is estimated at 2-3 hours for a complete, tested implementation.
