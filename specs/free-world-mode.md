# Free World Mode - Feature Specification

## Overview

Add a "Free World" mode accessible from the Main Menu that grants players 250 of each item type and disables save functionality to prevent inventory persistence. In multiplayer, only the host can activate this mode, and all connected players receive the items.

## Feature Goals

1. Provide a creative/sandbox mode for players to experiment without resource constraints
2. Prevent exploitation by disabling save functionality when Free World is active
3. Support both singleplayer and multiplayer (host-only activation)
4. Ensure original inventory is restored when game restarts
5. Maintain clean separation between normal and Free World gameplay

## User Stories

### Singleplayer
- **As a player**, I want to select "Free World" from the Load World menu to receive 250 of each item
- **As a player**, I want my save functionality disabled so I can't persist the free items
- **As a player**, I want my original inventory restored when I restart the game

### Multiplayer
- **As a host**, I want to activate "Free World" mode to give all connected players 250 items
- **As a client**, I want to receive 250 items when the host activates Free World mode
- **As a client**, I should NOT see the "Free World" option (host-only feature)
- **As any player**, I want save functionality disabled during Free World sessions

## Requirements

### Functional Requirements

#### FR1: Main Menu Integration
- Add "Free World" menu entry below "Load World" in the main menu
- Entry should be visible in both singleplayer and multiplayer contexts
- In multiplayer client mode, the option should be hidden or disabled

#### FR2: Inventory Grant System
- Grant 250 items for each item type defined in InventoryManager
- Item types to grant:
  - Apples
  - Bananas
  - Baby Bamboo
  - Bamboo Stacks
  - Wood Stacks
  - Pebbles
- Apply to current player immediately in singleplayer
- Apply to all connected players in multiplayer (when host activates)

#### FR3: Save Functionality Disabling
- Disable "Save World" menu option when Free World is active
- Disable auto-save on game exit when Free World is active
- Disable manual save through any UI interaction
- Show visual indicator that saves are disabled (optional)

#### FR4: Inventory Persistence Prevention
- Original player inventory must be preserved in PlayerConfig JSON
- Free World inventory changes must NOT be written to PlayerConfig
- On game restart, player should have their original inventory (pre-Free World)

#### FR5: Multiplayer Synchronization
- Host activates Free World → broadcast message to all clients
- All clients receive 250 items simultaneously
- All clients disable save functionality
- New clients joining after activation should also receive Free World state

#### FR6: Localization
- Add "Free World" translation to all supported languages:
  - **English (en)**: "Free World"
  - **Polish (pl)**: "Wolny Świat"
  - **Portuguese (pt)**: "Mundo Livre"
  - **Dutch (nl)**: "Vrije Wereld"
  - **German (de)**: "Freie Welt"

### Non-Functional Requirements

#### NFR1: Performance
- Inventory grant should be instantaneous (< 100ms)
- No noticeable lag when activating in multiplayer

#### NFR2: Security
- Client cannot spoof Free World activation
- Server validates host status before broadcasting

#### NFR3: User Experience
- Clear visual feedback when Free World is activated
- Clear indication that saves are disabled
- No confusion about inventory persistence

## Technical Design

### Architecture Components

#### 1. Free World Manager
**Class**: `FreeWorldManager.java`
**Package**: `wagemaker.uk.freeworld`

**Responsibilities**:
- Track Free World mode state (active/inactive)
- Grant 250 items to player inventory
- Disable/enable save functionality
- Coordinate with multiplayer for synchronization

**Key Methods**:
```java
public void activateFreeWorld()
public void deactivateFreeWorld()
public boolean isFreeWorldActive()
public void grantFreeWorldItems(Inventory inventory)
```

#### 2. Network Message
**Class**: `FreeWorldActivationMessage.java`
**Package**: `wagemaker.uk.network`

**Purpose**: Broadcast Free World activation from host to all clients

**Fields**:
- `boolean activated` - True when activating, false when deactivating

#### 3. Save System Integration
**Modified Classes**:
- `WorldSaveManager.java` - Check Free World state before saving
- `GameMenu.java` - Disable "Save World" option when Free World active

#### 4. Menu Integration
**Modified Class**: `GameMenu.java`

**Changes**:
- Add "Free World" menu option below "Load World"
- Hide option for multiplayer clients
- Handle activation and show confirmation dialog

#### 5. Inventory Manager Integration
**Modified Class**: `InventoryManager.java`

**Changes**:
- Add method to grant all items at once
- Prevent saving when Free World is active

### Data Flow

#### Singleplayer Activation Flow
```
1. Player clicks "Free World" in menu
2. GameMenu calls FreeWorldManager.activateFreeWorld()
3. FreeWorldManager.grantFreeWorldItems(currentInventory)
4. FreeWorldManager sets saveDisabled flag
5. Menu shows confirmation dialog
6. "Save World" option becomes disabled/grayed out
```

#### Multiplayer Activation Flow (Host)
```
1. Host clicks "Free World" in menu
2. GameMenu checks if player is host
3. FreeWorldManager.activateFreeWorld() on host
4. Host grants items to self
5. Host broadcasts FreeWorldActivationMessage to all clients
6. Each client receives message
7. Each client calls FreeWorldManager.activateFreeWorld()
8. Each client grants items to self
9. All players have saves disabled
```

#### Save Prevention Flow
```
1. Player attempts to save (menu or exit)
2. WorldSaveManager.saveWorld() checks FreeWorldManager.isFreeWorldActive()
3. If active, return early without saving
4. Show message: "Saving disabled in Free World mode"
```

#### Restart/Restore Flow
```
1. Player exits game (Free World active)
2. Game closes without saving inventory
3. Player restarts game
4. PlayerConfig loads original inventory from JSON
5. Free World state resets to inactive
6. Player has original inventory restored
```

### State Management

#### Free World State
**Storage**: In-memory only (not persisted)
**Scope**: Per game session
**Reset**: On game restart or disconnect

#### Inventory State
**Normal Mode**: Saved to PlayerConfig JSON on exit
**Free World Mode**: Changes NOT saved, original preserved

### UI Changes

#### Main Menu
**New Option**: "Free World"
**Position**: Below "Load World"
**Visibility**:
- Singleplayer: Always visible
- Multiplayer Host: Visible
- Multiplayer Client: Hidden

#### Save World Option
**Normal State**: Enabled
**Free World State**: Disabled/grayed with tooltip "Disabled in Free World mode"

#### Confirmation Dialog
**Title**: "Free World Activated"
**Message**: "You have received 250 of each item. Saving is now disabled."
**Button**: "OK"

### Localization Keys

Add to all language files (`assets/localization/*.json`):

```json
{
  "menu.free_world": "Free World",
  "dialog.free_world.title": "Free World Activated",
  "dialog.free_world.message": "You have received 250 of each item. Saving is now disabled.",
  "menu.save_disabled": "Saving disabled in Free World mode"
}
```

**Translations**:
- **en.json**: "Free World", "Free World Activated", "You have received 250 of each item. Saving is now disabled.", "Saving disabled in Free World mode"
- **pl.json**: "Wolny Świat", "Wolny Świat Aktywowany", "Otrzymałeś 250 sztuk każdego przedmiotu. Zapisywanie jest teraz wyłączone.", "Zapisywanie wyłączone w trybie Wolnego Świata"
- **pt.json**: "Mundo Livre", "Mundo Livre Ativado", "Você recebeu 250 de cada item. O salvamento está agora desativado.", "Salvamento desativado no modo Mundo Livre"
- **nl.json**: "Vrije Wereld", "Vrije Wereld Geactiveerd", "Je hebt 250 van elk item ontvangen. Opslaan is nu uitgeschakeld.", "Opslaan uitgeschakeld in Vrije Wereld modus"
- **de.json**: "Freie Welt", "Freie Welt Aktiviert", "Du hast 250 von jedem Gegenstand erhalten. Speichern ist jetzt deaktiviert.", "Speichern im Freie Welt Modus deaktiviert"

## Implementation Plan

### Phase 1: Core Infrastructure
**Tasks**:
1. Create `FreeWorldManager.java` class
2. Implement state tracking (active/inactive flag)
3. Implement `grantFreeWorldItems()` method
4. Add unit tests for FreeWorldManager

**Files Created**:
- `src/main/java/wagemaker/uk/freeworld/FreeWorldManager.java`
- `src/test/java/wagemaker/uk/freeworld/FreeWorldManagerTest.java`

### Phase 2: Inventory Integration
**Tasks**:
1. Add method to InventoryManager to grant all items
2. Integrate FreeWorldManager with InventoryManager
3. Test inventory granting in isolation

**Files Modified**:
- `src/main/java/wagemaker/uk/inventory/InventoryManager.java`

### Phase 3: Save System Integration
**Tasks**:
1. Modify WorldSaveManager to check Free World state
2. Prevent saving when Free World is active
3. Add logging for save prevention
4. Test save prevention

**Files Modified**:
- `src/main/java/wagemaker/uk/world/WorldSaveManager.java`

### Phase 4: Menu Integration
**Tasks**:
1. Add "Free World" option to GameMenu
2. Implement activation handler
3. Add confirmation dialog
4. Disable "Save World" option when active
5. Test menu interactions

**Files Modified**:
- `src/main/java/wagemaker/uk/ui/GameMenu.java`

### Phase 5: Localization
**Tasks**:
1. Add translation keys to all language files
2. Update LocalizationManager if needed
3. Test all languages

**Files Modified**:
- `assets/localization/en.json`
- `assets/localization/pl.json`
- `assets/localization/pt.json`
- `assets/localization/nl.json`
- `assets/localization/de.json`

### Phase 6: Multiplayer Support
**Tasks**:
1. Create `FreeWorldActivationMessage.java`
2. Add message handler in GameMessageHandler
3. Implement host-only activation logic
4. Broadcast to all clients
5. Test multiplayer synchronization

**Files Created**:
- `src/main/java/wagemaker/uk/network/FreeWorldActivationMessage.java`

**Files Modified**:
- `src/main/java/wagemaker/uk/gdx/GameMessageHandler.java`
- `src/main/java/wagemaker/uk/network/MessageType.java`
- `src/main/java/wagemaker/uk/network/GameServer.java`

### Phase 7: Integration Testing
**Tasks**:
1. Test singleplayer activation and save prevention
2. Test multiplayer host activation
3. Test multiplayer client reception
4. Test inventory restoration on restart
5. Test edge cases (disconnect, reconnect, etc.)

### Phase 8: Documentation
**Tasks**:
1. Update README.md with Free World feature
2. Update FEATURES.md with detailed description
3. Add to user documentation
4. Update class documentation

**Files Modified**:
- `README.md`
- `docs/FEATURES.md`

## Testing Strategy

### Unit Tests

#### FreeWorldManager Tests
- `testActivateFreeWorld()` - Verify state changes to active
- `testDeactivateFreeWorld()` - Verify state changes to inactive
- `testGrantFreeWorldItems()` - Verify 250 items granted for each type
- `testIsFreeWorldActive()` - Verify state query

#### InventoryManager Tests
- `testGrantAllItems()` - Verify all item types receive 250 count

#### WorldSaveManager Tests
- `testSavePreventedWhenFreeWorldActive()` - Verify save returns false
- `testSaveAllowedWhenFreeWorldInactive()` - Verify normal save works

### Integration Tests

#### Singleplayer Tests
1. Activate Free World → verify 250 items in inventory
2. Attempt save → verify save is prevented
3. Exit game → verify inventory not saved
4. Restart game → verify original inventory restored

#### Multiplayer Tests
1. Host activates Free World → verify all clients receive items
2. Client attempts to activate → verify option hidden/disabled
3. New client joins after activation → verify receives Free World state
4. All players attempt save → verify all saves prevented

### Manual Testing Checklist
- [ ] Free World option appears in menu (singleplayer)
- [ ] Free World option appears for host (multiplayer)
- [ ] Free World option hidden for client (multiplayer)
- [ ] 250 items granted for all types (singleplayer)
- [ ] 250 items granted for all types (multiplayer, all players)
- [ ] Save World option disabled when active
- [ ] Save on exit prevented when active
- [ ] Confirmation dialog displays correctly
- [ ] All translations display correctly
- [ ] Original inventory restored on restart
- [ ] No inventory persistence in Free World mode

## Edge Cases & Considerations

### Edge Case 1: Mid-Game Activation
**Scenario**: Player has existing items, then activates Free World
**Behavior**: Set each item count to 250 (overwrite existing counts)
**Rationale**: Simpler implementation, clear behavior

### Edge Case 2: Client Joins After Activation
**Scenario**: Host activates Free World, then new client joins
**Behavior**: New client receives Free World state in initial sync
**Implementation**: Include Free World flag in WorldState snapshot

### Edge Case 3: Host Disconnects
**Scenario**: Host activates Free World, then disconnects
**Behavior**: All clients retain Free World state until they disconnect
**Rationale**: Prevents confusion, maintains session consistency

### Edge Case 4: Inventory Full
**Scenario**: Player activates Free World with full inventory
**Behavior**: Set all item counts to 250 regardless of current state
**Rationale**: Free World overrides normal inventory limits

### Edge Case 5: Multiple Activations
**Scenario**: Player clicks "Free World" multiple times
**Behavior**: First click activates, subsequent clicks do nothing (already active)
**Implementation**: Check `isFreeWorldActive()` before processing

## Security Considerations

### Multiplayer Security
- **Client Validation**: Server must verify host status before broadcasting
- **Message Validation**: Validate FreeWorldActivationMessage structure
- **State Synchronization**: Ensure all clients have consistent Free World state

### Save System Security
- **Prevent Bypass**: Ensure all save paths check Free World state
- **File System**: No direct file writes when Free World active
- **Config Protection**: Original PlayerConfig must remain unmodified

## Performance Considerations

- **Inventory Grant**: O(1) operation per item type (6 types = 6 operations)
- **Network Broadcast**: Single message to all clients (minimal overhead)
- **State Check**: Simple boolean flag check (negligible performance impact)
- **Memory**: No additional memory overhead (single boolean flag)

## Future Enhancements

### Potential Additions
1. **Configurable Item Counts**: Allow host to specify item count (100, 250, 500, 1000)
2. **Selective Items**: Choose which items to grant (checkboxes)
3. **Free World Indicator**: HUD element showing "Free World Mode Active"
4. **Deactivation Option**: Allow disabling Free World mid-session
5. **Free World Worlds**: Separate save category for Free World sessions

### Not In Scope (Current Version)
- Custom item counts
- Partial item grants
- Free World-specific worlds
- Persistent Free World mode across sessions

## Success Criteria

### Definition of Done
- [ ] "Free World" option appears in main menu
- [ ] Clicking activates Free World and grants 250 items
- [ ] Save functionality is disabled when active
- [ ] Original inventory restored on game restart
- [ ] Multiplayer host can activate for all players
- [ ] Multiplayer clients cannot activate
- [ ] All translations implemented
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual testing checklist complete
- [ ] Documentation updated

### Acceptance Criteria
1. Player can activate Free World from menu
2. Player receives exactly 250 of each item type
3. Save World option is disabled/grayed when active
4. Game exit does not save inventory when active
5. Original inventory is restored on restart
6. Multiplayer host activation affects all clients
7. Multiplayer clients cannot see/use the option
8. All UI text displays in correct language

## Risks & Mitigations

### Risk 1: Save System Bypass
**Risk**: Player finds way to save inventory despite Free World
**Mitigation**: Check Free World state in ALL save paths (menu, exit, auto-save)
**Severity**: High

### Risk 2: Inventory Persistence Bug
**Risk**: Free World items accidentally saved to PlayerConfig
**Mitigation**: Thorough testing of save prevention, code review
**Severity**: High

### Risk 3: Multiplayer Desync
**Risk**: Clients don't receive Free World state consistently
**Mitigation**: Reliable message broadcasting, include in WorldState sync
**Severity**: Medium

### Risk 4: UI Confusion
**Risk**: Players don't understand why saves are disabled
**Mitigation**: Clear confirmation dialog, visual indicators
**Severity**: Low

## Open Questions

1. **Should Free World be deactivatable mid-session?**
   - Current spec: No, active until disconnect/restart
   - Alternative: Add "Deactivate Free World" option

2. **Should there be a visual indicator in HUD?**
   - Current spec: No HUD indicator
   - Alternative: Add "Free World Mode" text to HUD

3. **Should Free World worlds be saved separately?**
   - Current spec: No, same world save system
   - Alternative: Separate "Free World Saves" category

4. **Should item counts be configurable?**
   - Current spec: Fixed at 250
   - Alternative: Dropdown with 100/250/500/1000 options

## References

- **Inventory System**: `src/main/java/wagemaker/uk/inventory/InventoryManager.java`
- **Save System**: `src/main/java/wagemaker/uk/world/WorldSaveManager.java`
- **Menu System**: `src/main/java/wagemaker/uk/ui/GameMenu.java`
- **Network Messages**: `src/main/java/wagemaker/uk/network/`
- **Localization**: `assets/localization/*.json`

## Version History

- **v1.0** (2025-01-XX) - Initial specification created
