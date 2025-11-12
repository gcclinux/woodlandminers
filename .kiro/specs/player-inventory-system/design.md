# Design Document

## Overview

The Player Inventory System is a comprehensive feature that extends the existing item collection mechanics to include storage, automatic consumption based on health status, visual UI display, and persistence across game sessions. The system maintains separate inventories for single-player and multiplayer modes, ensuring that player progress is independent between these contexts.

### Key Design Principles

1. **Minimal Disruption**: Leverage existing item pickup and health systems with minimal changes to core game logic
2. **Mode Separation**: Maintain completely separate inventory state for single-player vs multiplayer
3. **Auto-Consumption**: Intelligently consume health-restoring items when needed, store when not
4. **Visual Clarity**: Provide clear, unobtrusive UI showing current inventory state
5. **Persistence**: Extend existing save/load mechanism to include inventory data

## Architecture

### Component Overview

```
┌─────────────────────────────────────────────────────────────┐
│                      MyGdxGame (Main)                       │
│  - Manages game loop and rendering                          │
│  - Owns Player and InventoryManager instances               │
└────────────────┬────────────────────────────────────────────┘
                 │
        ┌────────┴────────┐
        │                 │
┌───────▼──────┐  ┌──────▼────────┐
│    Player    │  │   Inventory   │
│              │  │    Manager    │
│ - Position   │  │               │
│ - Health     │  │ - SP Inv      │
│ - Pickup     │  │ - MP Inv      │
│   Logic      │  │ - Auto-       │
│              │  │   Consume     │
└──────┬───────┘  └───────┬───────┘
       │                  │
       │                  │
┌──────▼──────────────────▼───────┐
│      InventoryRenderer          │
│  - UI Display at bottom-right   │
│  - Item icons and counts        │
└─────────────────────────────────┘
```


### Data Flow

1. **Item Collection Flow**:
   - Player destroys tree → Item entity spawns
   - Player moves near item → Pickup detection triggers
   - Check player health:
     - If health < 100% AND item is consumable → Consume immediately, restore health
     - If health = 100% OR item is non-consumable → Add to inventory
   - Remove item entity from world
   - Update inventory UI

2. **Auto-Consumption Flow**:
   - Player takes damage → Health drops below 100%
   - InventoryManager checks for consumable items in inventory
   - If consumables available → Consume one, restore health, decrement count
   - Repeat until health = 100% or no consumables remain
   - Update inventory UI

3. **Save/Load Flow**:
   - Player triggers "Save Player" → Determine current mode (SP/MP)
   - Save position + health + appropriate inventory to JSON
   - On load → Restore position + health + appropriate inventory
   - Inventory UI updates automatically

## Components and Interfaces

### 1. Inventory Class

Represents a single inventory (either single-player or multiplayer).

```java
public class Inventory {
    private int appleCount;
    private int bananaCount;
    private int babyBambooCount;
    private int bambooStackCount;
    private int woodStackCount;
    
    // Getters and setters for each item type
    public int getAppleCount() { return appleCount; }
    public void addApple(int amount) { appleCount += amount; }
    public boolean removeApple(int amount) { /* decrement if available */ }
    
    // Similar methods for other items...
    
    public void clear() { /* reset all counts to 0 */ }
}
```

### 2. InventoryManager Class

Central manager for inventory operations, handles both SP and MP inventories.

```java
public class InventoryManager {
    private Inventory singleplayerInventory;
    private Inventory multiplayerInventory;
    private Player player;
    private boolean isMultiplayerMode;
    
    public InventoryManager(Player player) { /* initialize */ }
    
    public void setMultiplayerMode(boolean isMultiplayer) { /* switch mode */ }
    
    public Inventory getCurrentInventory() { 
        return isMultiplayerMode ? multiplayerInventory : singleplayerInventory;
    }
    
    // Item collection with auto-consumption logic
    public void collectItem(ItemType type) {
        if (isConsumable(type) && player.getHealth() < 100) {
            consumeItem(type);
        } else {
            getCurrentInventory().addItem(type, 1);
        }
    }
    
    // Auto-consume when health drops
    public void tryAutoConsume() {
        if (player.getHealth() < 100) {
            Inventory inv = getCurrentInventory();
            if (inv.getAppleCount() > 0) {
                consumeApple();
            } else if (inv.getBananaCount() > 0) {
                consumeBanana();
            }
        }
    }
    
    private void consumeApple() { /* restore 20 HP, decrement count */ }
    private void consumeBanana() { /* restore 20 HP, decrement count */ }
}
```


### 3. InventoryRenderer Class

Handles visual display of inventory UI.

```java
public class InventoryRenderer {
    private Texture woodenBackground;
    private Texture appleIcon;
    private Texture bananaIcon;
    private Texture babyBambooIcon;
    private Texture bambooStackIcon;
    private Texture woodStackIcon;
    private BitmapFont countFont;
    
    private static final int SLOT_SIZE = 40;
    private static final int SLOT_SPACING = 8;
    private static final int PANEL_PADDING = 8;
    
    public void render(SpriteBatch batch, Inventory inventory, 
                      float camX, float camY, float viewWidth, float viewHeight) {
        // Calculate bottom-right position
        float panelX = camX + viewWidth/2 - PANEL_WIDTH - 20;
        float panelY = camY - viewHeight/2 + 20;
        
        // Draw wooden background
        batch.draw(woodenBackground, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);
        
        // Draw each slot with icon and count
        renderSlot(batch, appleIcon, inventory.getAppleCount(), 
                  panelX + PANEL_PADDING, panelY + PANEL_PADDING);
        renderSlot(batch, bananaIcon, inventory.getBananaCount(), 
                  panelX + PANEL_PADDING + (SLOT_SIZE + SLOT_SPACING), panelY + PANEL_PADDING);
        // ... render other slots
    }
    
    private void renderSlot(SpriteBatch batch, Texture icon, int count, float x, float y) {
        // Draw slot border
        // Draw icon
        // Draw count text above icon
    }
}
```

### 4. Modified Player Class

Extend existing Player class with inventory integration.

**Changes to Player.java**:
- Add reference to InventoryManager
- Modify pickup methods (pickupApple, pickupBanana, etc.) to use InventoryManager
- Add health change detection to trigger auto-consumption
- Remove direct health restoration from pickup methods

```java
// In Player class
private InventoryManager inventoryManager;

public void setInventoryManager(InventoryManager manager) {
    this.inventoryManager = manager;
}

// Modified pickup method example
private void pickupApple(String appleKey) {
    if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
        gameClient.sendItemPickup(appleKey);
    } else {
        // Single-player: use inventory manager
        if (inventoryManager != null) {
            inventoryManager.collectItem(ItemType.APPLE);
        }
        
        // Remove apple from game
        if (apples.containsKey(appleKey)) {
            Apple apple = apples.get(appleKey);
            apple.dispose();
            apples.remove(appleKey);
        }
    }
}

// In update method, after damage is taken
public void update(float deltaTime) {
    // ... existing update logic ...
    
    // Check for auto-consumption after any health changes
    if (inventoryManager != null && health < previousHealth) {
        inventoryManager.tryAutoConsume();
    }
    previousHealth = health;
}
```


### 5. Modified GameMenu Class

Update save/load functionality to include inventory data.

**Changes to GameMenu.java**:
- Rename "Save Position" to "Save Player"
- Extend savePlayerPosition() to include inventory data
- Extend loadPlayerPosition() to restore inventory data

```java
// Modified savePlayerPosition method
public void savePlayerPosition() {
    // ... existing position/health save logic ...
    
    // Add inventory data to JSON
    if (inventoryManager != null) {
        Inventory spInv = inventoryManager.getSingleplayerInventory();
        Inventory mpInv = inventoryManager.getMultiplayerInventory();
        
        jsonBuilder.append("  \"singleplayerInventory\": {\n");
        jsonBuilder.append(String.format("    \"apple\": %d,\n", spInv.getAppleCount()));
        jsonBuilder.append(String.format("    \"banana\": %d,\n", spInv.getBananaCount()));
        jsonBuilder.append(String.format("    \"babyBamboo\": %d,\n", spInv.getBabyBambooCount()));
        jsonBuilder.append(String.format("    \"bambooStack\": %d,\n", spInv.getBambooStackCount()));
        jsonBuilder.append(String.format("    \"woodStack\": %d\n", spInv.getWoodStackCount()));
        jsonBuilder.append("  },\n");
        
        // Similar for multiplayer inventory
    }
}

// Modified loadPlayerPosition method
public boolean loadPlayerPosition() {
    // ... existing position/health load logic ...
    
    // Load inventory data
    if (inventoryManager != null) {
        try {
            int appleCount = parseJsonObjectInt(jsonContent, "\"singleplayerInventory\"", "apple");
            int bananaCount = parseJsonObjectInt(jsonContent, "\"singleplayerInventory\"", "banana");
            // ... load other items
            
            Inventory spInv = inventoryManager.getSingleplayerInventory();
            spInv.setAppleCount(appleCount);
            spInv.setBananaCount(bananaCount);
            // ... set other items
            
            // Similar for multiplayer inventory
        } catch (Exception e) {
            // Inventory data doesn't exist (backwards compatibility)
        }
    }
}
```

## Data Models

### Inventory Data Structure

```java
public class Inventory {
    private int appleCount = 0;
    private int bananaCount = 0;
    private int babyBambooCount = 0;
    private int bambooStackCount = 0;
    private int woodStackCount = 0;
}
```

### ItemType Enum

```java
public enum ItemType {
    APPLE(true, 20),      // consumable, restores 20 HP
    BANANA(true, 20),     // consumable, restores 20 HP
    BABY_BAMBOO(false, 0),
    BAMBOO_STACK(false, 0),
    WOOD_STACK(false, 0);
    
    private final boolean consumable;
    private final int healthRestore;
    
    ItemType(boolean consumable, int healthRestore) {
        this.consumable = consumable;
        this.healthRestore = healthRestore;
    }
    
    public boolean isConsumable() { return consumable; }
    public int getHealthRestore() { return healthRestore; }
}
```


### Save File Format (Extended)

```json
{
  "playerName": "Player",
  "singleplayerPosition": {
    "x": 100.50,
    "y": 200.75
  },
  "singleplayerHealth": 85.0,
  "singleplayerInventory": {
    "apple": 3,
    "banana": 5,
    "babyBamboo": 12,
    "bambooStack": 8,
    "woodStack": 15
  },
  "multiplayerPosition": {
    "x": 50.25,
    "y": 150.00
  },
  "multiplayerHealth": 100.0,
  "multiplayerInventory": {
    "apple": 1,
    "banana": 2,
    "babyBamboo": 0,
    "bambooStack": 3,
    "woodStack": 7
  },
  "lastServer": "192.168.1.100:9999",
  "savedAt": "Wed Nov 12 14:30:00 PST 2025"
}
```

## Error Handling

### Inventory Operations

1. **Negative Count Prevention**: All decrement operations check for sufficient quantity before proceeding
2. **Null Safety**: All inventory operations check for null references before accessing
3. **Backwards Compatibility**: Load operations gracefully handle missing inventory data in old save files

```java
public boolean removeApple(int amount) {
    if (appleCount >= amount) {
        appleCount -= amount;
        return true;
    }
    return false; // Not enough apples
}
```

### Save/Load Operations

1. **Missing Inventory Data**: When loading old save files without inventory data, initialize with empty inventory
2. **Corrupted Data**: Catch parsing exceptions and default to empty inventory
3. **File Access Errors**: Log errors and continue with default values

```java
try {
    int appleCount = parseJsonObjectInt(jsonContent, "\"singleplayerInventory\"", "apple");
    inventory.setAppleCount(appleCount);
} catch (Exception e) {
    System.out.println("No inventory data found, starting with empty inventory");
    inventory.setAppleCount(0);
}
```

### Multiplayer Synchronization

1. **Client-Side Prediction**: Clients immediately update local inventory UI for responsiveness
2. **Server Authority**: Server validates and broadcasts actual inventory changes
3. **Desync Recovery**: Periodic inventory sync messages from server to clients

## Testing Strategy

### Unit Testing Focus Areas

1. **Inventory Class**:
   - Test add/remove operations
   - Test boundary conditions (negative counts, overflow)
   - Test clear operation

2. **InventoryManager**:
   - Test mode switching (SP ↔ MP)
   - Test auto-consumption logic
   - Test collection with health-based routing

3. **Save/Load**:
   - Test saving inventory data
   - Test loading inventory data
   - Test backwards compatibility with old save files


### Integration Testing Focus Areas

1. **Item Collection Flow**:
   - Destroy tree → Item spawns → Player collects → Inventory updates
   - Test with health at 100% (should store)
   - Test with health < 100% (should consume if consumable)

2. **Auto-Consumption Flow**:
   - Player takes damage → Health drops → Auto-consume triggers
   - Test with consumables in inventory
   - Test with empty inventory (no crash)

3. **UI Rendering**:
   - Verify inventory UI renders at correct position
   - Verify counts update in real-time
   - Verify UI doesn't overlap with other elements

4. **Mode Switching**:
   - Start in SP → Collect items → Switch to MP → Verify separate inventory
   - Verify inventories don't cross-contaminate

5. **Persistence**:
   - Save in SP with items → Load → Verify inventory restored
   - Save in MP with items → Load → Verify inventory restored
   - Load old save file → Verify no crash, empty inventory

### Manual Testing Checklist

- [ ] Collect apple with full health → Stored in inventory
- [ ] Collect apple with low health → Health restored immediately
- [ ] Take damage with apples in inventory → Auto-consume works
- [ ] Collect all 5 item types → All display correctly in UI
- [ ] Switch SP → MP → Inventories are separate
- [ ] Save and load in SP → Inventory persists
- [ ] Save and load in MP → Inventory persists
- [ ] UI displays at bottom-right without overlap
- [ ] Item counts display correctly (0-999+)
- [ ] Menu shows "Save Player" instead of "Save Position"

## UI Design Specifications

### Inventory Panel Layout

```
┌─────────────────────────────────────────────────────────────┐
│  Wooden Plank Background (240px × 56px)                     │
│  ┌────────┬────────┬────────┬────────┬────────┐            │
│  │   3    │   5    │   12   │   8    │   15   │  ← Counts  │
│  ├────────┼────────┼────────┼────────┼────────┤            │
│  │ [🍎]   │ [🍌]   │ [🎋]   │ [🎍]   │ [🪵]   │  ← Icons   │
│  └────────┴────────┴────────┴────────┴────────┘            │
└─────────────────────────────────────────────────────────────┘
```

### Visual Specifications

- **Panel Size**: 240px wide × 56px tall
- **Slot Size**: 40px × 40px
- **Slot Spacing**: 8px between slots
- **Panel Padding**: 8px on all sides
- **Icon Size**: 32px × 32px (centered in slot)
- **Count Font**: Size 12, white with black outline
- **Background**: Wooden plank texture (similar to menu)
- **Slot Border**: 1px dark brown border around each slot
- **Position**: Bottom-right corner, 20px from edges

### Asset Requirements

**Existing Assets to Use**:
- Apple icon: From existing Apple.java texture
- Banana icon: From existing Banana.java texture
- BabyBamboo icon: From existing BabyBamboo.java texture
- BambooStack icon: From existing BambooStack.java texture
- WoodStack icon: From existing WoodStack.java texture

**New Assets to Create**:
- Wooden plank background texture (procedurally generated like menu)
- Slot border texture (can be drawn programmatically)

### Font Rendering

```java
// Count text rendering
countFont.setColor(Color.WHITE);
countFont.getData().setScale(1.0f);

// Draw with shadow for readability
countFont.setColor(Color.BLACK);
countFont.draw(batch, countText, x + 1, y - 1); // Shadow
countFont.setColor(Color.WHITE);
countFont.draw(batch, countText, x, y); // Main text
```

## Implementation Notes

### Performance Considerations

1. **UI Rendering**: Only render inventory UI when counts > 0 or always visible (TBD based on design preference)
2. **Auto-Consumption**: Check only when health decreases, not every frame
3. **Texture Loading**: Load item icons once during initialization, reuse for rendering

### Multiplayer Considerations

1. **Server Authority**: Server manages inventory state for all players
2. **Client Prediction**: Clients update local UI immediately for responsiveness
3. **Sync Messages**: Server sends inventory updates when items are collected/consumed
4. **Network Protocol**: Extend existing item pickup messages to include inventory updates

### Backwards Compatibility

1. **Old Save Files**: Gracefully handle save files without inventory data
2. **Default Values**: Initialize empty inventory when data is missing
3. **Migration Path**: No migration needed, old saves work with empty inventory

## Technical Constraints

1. **LibGDX Framework**: Must use LibGDX rendering APIs (SpriteBatch, Texture, BitmapFont)
2. **Existing Architecture**: Must integrate with existing Player, GameMenu, and item classes
3. **JSON Format**: Must maintain compatibility with existing save file format
4. **Network Protocol**: Must work with existing multiplayer networking (if applicable)

## Future Enhancements (Out of Scope)

1. Manual item consumption (hotkeys or UI buttons)
2. Item crafting or combining
3. Inventory capacity limits
4. Item dropping or trading
5. Inventory sorting or filtering
6. Tooltips showing item effects
7. Animated item collection effects
8. Sound effects for collection/consumption
