# Design Document

## Overview

The Inventory Keyboard Binding System extends the existing Player Inventory System by adding keyboard shortcuts (keys 1-5) to select inventory items. This feature provides quick access to inventory items and lays the groundwork for future item-specific actions like planting bamboo on sand tiles.

### Key Design Principles

1. **Minimal Integration**: Add keyboard binding functionality without modifying core inventory storage or rendering logic
2. **Visual Feedback**: Provide clear visual indication of which item is currently selected
3. **Input Isolation**: Only process keyboard bindings during active gameplay, not in menus
4. **Extensibility**: Design the selection system to support future item-specific actions

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
│ - Movement   │  │               │
│ - Input      │  │ - SP/MP Inv   │
│ - NEW:       │  │ - NEW:        │
│   Inventory  │  │   Selected    │
│   Selection  │  │   Item        │
└──────────────┘  └───────┬───────┘
                          │
                  ┌───────▼───────┐
                  │   Inventory   │
                  │   Renderer    │
                  │               │
                  │ - UI Display  │
                  │ - NEW:        │
                  │   Selection   │
                  │   Highlight   │
                  └───────────────┘
```

### Data Flow

1. **Item Selection Flow**:
   - Player presses number key (1-5) during gameplay
   - Player.update() detects key press using Gdx.input.isKeyJustPressed()
   - Player calls inventoryManager.setSelectedSlot(slotIndex)
   - InventoryManager updates selectedSlot field
   - InventoryRenderer reads selectedSlot and renders highlight

2. **Visual Feedback Flow**:
   - InventoryRenderer.render() is called each frame
   - Renderer checks inventoryManager.getSelectedSlot()
   - If slot is selected, render highlight border around that slot
   - Highlight uses distinct color (yellow/gold) to stand out


## Components and Interfaces

### 1. Modified InventoryManager Class

Add selection state tracking to the existing InventoryManager.

```java
public class InventoryManager {
    // Existing fields...
    private Inventory singleplayerInventory;
    private Inventory multiplayerInventory;
    private Player player;
    private boolean isMultiplayerMode;
    
    // NEW: Selection state
    private int selectedSlot; // 0-4 for slots, -1 for no selection
    
    public InventoryManager(Player player) {
        // Existing initialization...
        this.selectedSlot = -1; // No selection by default
    }
    
    // NEW: Selection methods
    public void setSelectedSlot(int slot) {
        if (slot >= 0 && slot <= 4) {
            this.selectedSlot = slot;
        } else {
            this.selectedSlot = -1; // Clear selection
        }
    }
    
    public int getSelectedSlot() {
        return selectedSlot;
    }
    
    public void clearSelection() {
        this.selectedSlot = -1;
    }
    
    // NEW: Get selected item type (for future actions)
    public ItemType getSelectedItemType() {
        switch (selectedSlot) {
            case 0: return ItemType.APPLE;
            case 1: return ItemType.BANANA;
            case 2: return ItemType.BABY_BAMBOO;
            case 3: return ItemType.BAMBOO_STACK;
            case 4: return ItemType.WOOD_STACK;
            default: return null;
        }
    }
    
    // Existing methods remain unchanged...
}
```

### 2. Modified Player Class

Add keyboard input handling for inventory selection in the update() method.

**Changes to Player.java**:
- Add inventory selection input handling after movement input
- Use isKeyJustPressed() to detect single key presses (not continuous hold)
- Only process when game menu is not open

```java
// In Player.update() method, after movement handling
public void update(float deltaTime) {
    // ... existing movement code ...
    
    // Handle inventory selection (only when menu is not open)
    if (gameMenu != null && !gameMenu.isAnyMenuOpen()) {
        handleInventorySelection();
    }
    
    // ... rest of existing update code ...
}

// NEW: Inventory selection input handler
private void handleInventorySelection() {
    if (inventoryManager == null) {
        return;
    }
    
    if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
        inventoryManager.setSelectedSlot(0); // Apple
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
        inventoryManager.setSelectedSlot(1); // Banana
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
        inventoryManager.setSelectedSlot(2); // BabyBamboo
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
        inventoryManager.setSelectedSlot(3); // BambooStack
    } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) {
        inventoryManager.setSelectedSlot(4); // WoodStack
    }
}
```


### 3. Modified InventoryRenderer Class

Add visual selection indicator rendering to the existing InventoryRenderer.

**Changes to InventoryRenderer.java**:
- Add selection highlight rendering in renderSlot() method
- Use ShapeRenderer to draw colored border around selected slot
- Render highlight before slot content for proper layering

```java
public class InventoryRenderer {
    // Existing fields...
    private Texture woodenBackground;
    private Texture appleIcon;
    // ... other textures ...
    private BitmapFont countFont;
    private ShapeRenderer shapeRenderer; // NEW: For selection highlight
    
    private static final int SLOT_SIZE = 40;
    private static final int SLOT_SPACING = 8;
    private static final int PANEL_PADDING = 8;
    
    // NEW: Selection highlight colors
    private static final float HIGHLIGHT_R = 1.0f;  // Yellow/Gold
    private static final float HIGHLIGHT_G = 0.84f;
    private static final float HIGHLIGHT_B = 0.0f;
    private static final float HIGHLIGHT_ALPHA = 0.8f;
    private static final int HIGHLIGHT_BORDER_WIDTH = 3;
    
    public InventoryRenderer() {
        // Existing initialization...
        this.shapeRenderer = new ShapeRenderer();
    }
    
    public void render(SpriteBatch batch, Inventory inventory, 
                      float camX, float camY, float viewWidth, float viewHeight,
                      int selectedSlot) { // NEW: selectedSlot parameter
        // Calculate bottom-right position
        float panelX = camX + viewWidth/2 - PANEL_WIDTH - 20;
        float panelY = camY - viewHeight/2 + 20;
        
        // Draw wooden background
        batch.draw(woodenBackground, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);
        
        // Draw each slot with icon and count
        float slotX = panelX + PANEL_PADDING;
        float slotY = panelY + PANEL_PADDING;
        
        renderSlot(batch, appleIcon, inventory.getAppleCount(), 
                  slotX, slotY, selectedSlot == 0);
        slotX += SLOT_SIZE + SLOT_SPACING;
        
        renderSlot(batch, bananaIcon, inventory.getBananaCount(), 
                  slotX, slotY, selectedSlot == 1);
        slotX += SLOT_SIZE + SLOT_SPACING;
        
        renderSlot(batch, babyBambooIcon, inventory.getBabyBambooCount(), 
                  slotX, slotY, selectedSlot == 2);
        slotX += SLOT_SIZE + SLOT_SPACING;
        
        renderSlot(batch, bambooStackIcon, inventory.getBambooStackCount(), 
                  slotX, slotY, selectedSlot == 3);
        slotX += SLOT_SIZE + SLOT_SPACING;
        
        renderSlot(batch, woodStackIcon, inventory.getWoodStackCount(), 
                  slotX, slotY, selectedSlot == 4);
    }
    
    private void renderSlot(SpriteBatch batch, Texture icon, int count, 
                           float x, float y, boolean isSelected) {
        // NEW: Draw selection highlight if this slot is selected
        if (isSelected) {
            batch.end(); // End batch to use ShapeRenderer
            
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(HIGHLIGHT_R, HIGHLIGHT_G, HIGHLIGHT_B, HIGHLIGHT_ALPHA);
            Gdx.gl.glLineWidth(HIGHLIGHT_BORDER_WIDTH);
            
            // Draw highlight border around slot
            shapeRenderer.rect(x - 2, y - 2, SLOT_SIZE + 4, SLOT_SIZE + 4);
            
            shapeRenderer.end();
            Gdx.gl.glLineWidth(1); // Reset line width
            
            batch.begin(); // Resume batch rendering
        }
        
        // Draw slot border (existing)
        // Draw icon (existing)
        // Draw count text (existing)
        // ... existing rendering code ...
        
        // NEW: Draw selection marker arrow above slot if selected
        if (isSelected) {
            batch.end(); // End batch to use ShapeRenderer
            
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.0f, 0.0f, 0.0f, 1.0f); // Black for better contrast
            
            // Calculate marker position (centered above slot)
            float markerCenterX = x + SLOT_SIZE / 2;
            float markerTopY = y + SLOT_SIZE + 4; // 4 pixels above slot
            
            // Draw downward-pointing triangle (8x8 pixels)
            // Top point
            float topX = markerCenterX;
            float topY = markerTopY + 8;
            // Bottom left point
            float leftX = markerCenterX - 4;
            float leftY = markerTopY;
            // Bottom right point
            float rightX = markerCenterX + 4;
            float rightY = markerTopY;
            
            shapeRenderer.triangle(topX, topY, leftX, leftY, rightX, rightY);
            
            shapeRenderer.end();
            
            batch.begin(); // Resume batch rendering
        }
    }
    
    public void dispose() {
        // Existing disposal...
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
```


### 4. Modified MyGdxGame Class

Update the render() method to pass selection state to InventoryRenderer.

**Changes to MyGdxGame.java**:
- Pass selectedSlot to inventoryRenderer.render() call

```java
// In MyGdxGame.render() method
@Override
public void render() {
    // ... existing rendering code ...
    
    // render inventory UI (MODIFIED)
    if (inventoryRenderer != null && inventoryManager != null) {
        int selectedSlot = inventoryManager.getSelectedSlot();
        inventoryRenderer.render(batch, inventoryManager.getCurrentInventory(), 
                                camera.position.x, camera.position.y, 
                                viewport.getWorldWidth(), viewport.getWorldHeight(),
                                selectedSlot); // NEW: Pass selection state
    }
    
    // ... rest of rendering code ...
}
```

## Data Models

### Selection State

```java
// In InventoryManager
private int selectedSlot; // Values:
                         // -1: No selection
                         //  0: Apple (slot 1)
                         //  1: Banana (slot 2)
                         //  2: BabyBamboo (slot 3)
                         //  3: BambooStack (slot 4)
                         //  4: WoodStack (slot 5)
```

### Keyboard Mapping

| Key | Slot Index | Item Type |
|-----|-----------|-----------|
| 1   | 0         | Apple     |
| 2   | 1         | Banana    |
| 3   | 2         | BabyBamboo |
| 4   | 3         | BambooStack |
| 5   | 4         | WoodStack |

## Error Handling

### Input Validation

1. **Invalid Slot Index**: setSelectedSlot() validates slot is 0-4, otherwise clears selection
2. **Null InventoryManager**: Player checks for null before calling selection methods
3. **Menu Open State**: Selection input only processed when menu is closed

```java
// In Player.handleInventorySelection()
if (inventoryManager == null) {
    return; // Gracefully handle missing inventory manager
}

// In Player.update()
if (gameMenu != null && !gameMenu.isAnyMenuOpen()) {
    handleInventorySelection(); // Only when menu is closed
}
```

### Rendering Safety

1. **Null Checks**: InventoryRenderer checks for null inventory before rendering
2. **Invalid Selection**: Renderer handles selectedSlot = -1 (no highlight)
3. **ShapeRenderer State**: Properly end batch before using ShapeRenderer, resume after

```java
// In InventoryRenderer.render()
if (inventory == null) {
    return; // Don't render if no inventory
}

// In InventoryRenderer.renderSlot()
if (isSelected) {
    batch.end();
    // ... shape rendering ...
    batch.begin();
}
```

## Testing Strategy

### Unit Testing Focus Areas

1. **InventoryManager Selection**:
   - Test setSelectedSlot() with valid indices (0-4)
   - Test setSelectedSlot() with invalid indices (negative, > 4)
   - Test clearSelection() resets to -1
   - Test getSelectedItemType() returns correct ItemType for each slot

2. **Input Handling**:
   - Test each number key (1-5) sets correct slot
   - Test selection only works when menu is closed
   - Test isKeyJustPressed() prevents continuous selection


### Integration Testing Focus Areas

1. **Selection Flow**:
   - Press key 1 → Apple slot highlights
   - Press key 3 → BabyBamboo slot highlights (previous highlight clears)
   - Open menu → Press key 2 → No selection change
   - Close menu → Press key 2 → Banana slot highlights

2. **Visual Feedback**:
   - Verify highlight renders around correct slot
   - Verify highlight color is distinct (yellow/gold)
   - Verify only one slot highlighted at a time
   - Verify highlight clears when selection changes

3. **Mode Switching**:
   - Select item in SP mode → Switch to MP mode → Selection persists
   - Verify selection works in both SP and MP modes

### Manual Testing Checklist

- [ ] Press keys 1-5 → Correct slots highlight
- [ ] Press same key twice → Highlight remains on that slot
- [ ] Switch between slots → Highlight moves correctly
- [ ] Open menu → Number keys don't change selection
- [ ] Close menu → Number keys work again
- [ ] Highlight is visible and distinct from unselected slots
- [ ] Selection works with empty inventory slots (count = 0)
- [ ] Selection persists when collecting/consuming items
- [ ] Selection works in both single-player and multiplayer modes

## UI Design Specifications

### Selection Highlight

```
┌─────────────────────────────────────────────────────────────┐
│  Wooden Plank Background                                    │
│  ┌────────┬────────┬────────┬────────┬────────┐            │
│  │   3    │   5    │▼  12   │   8    │   15   │  ← Arrow   │
│  ├────────┼────────┼────────┼────────┼────────┤   Marker   │
│  │ [🍎]   │╔[🍌]══╗│ [🎋]   │ [🎍]   │ [🪵]   │  ← Slot 2  │
│  └────────┴╚══════╝┴────────┴────────┴────────┘   Selected │
└─────────────────────────────────────────────────────────────┘
```

### Visual Specifications

- **Highlight Color**: RGB(255, 214, 0) - Yellow/Gold
- **Highlight Alpha**: 0.8 (80% opacity)
- **Border Width**: 3 pixels
- **Border Style**: Solid line
- **Border Offset**: 2 pixels outside slot boundary (44x44 total)
- **Animation**: None (static highlight for simplicity)

- **Selection Marker**: Downward-pointing triangle (arrow)
- **Marker Color**: RGB(0, 0, 0) - Black for better contrast
- **Marker Alpha**: 1.0 (100% opacity for clarity)
- **Marker Size**: 8x8 pixels (small, unobtrusive)
- **Marker Position**: Centered horizontally above slot, 4 pixels above slot top edge
- **Marker Style**: Filled triangle pointing down toward selected item

### Rendering Order

1. Wooden background panel
2. Slot borders (dark brown)
3. **Selection highlight (if slot selected)** ← Existing
4. Item icons
5. Item count text
6. **Selection marker arrow (if slot selected)** ← NEW

## Implementation Notes

### Performance Considerations

1. **Input Polling**: Use isKeyJustPressed() instead of isKeyPressed() to avoid continuous triggering
2. **Rendering**: ShapeRenderer state changes (batch.end/begin) only when selection exists
3. **No Animation**: Static highlight avoids per-frame calculations

### LibGDX Input Keys

```java
// Number keys on main keyboard (not numpad)
Input.Keys.NUM_1  // Key "1"
Input.Keys.NUM_2  // Key "2"
Input.Keys.NUM_3  // Key "3"
Input.Keys.NUM_4  // Key "4"
Input.Keys.NUM_5  // Key "5"
```

### Future Extensibility

This design supports future item-specific actions:

```java
// Example: Future bamboo planting action
if (Gdx.input.isKeyJustPressed(Input.Keys.E)) { // Use item
    ItemType selectedItem = inventoryManager.getSelectedItemType();
    if (selectedItem == ItemType.BABY_BAMBOO) {
        // Check if player is on sand tile
        // Plant bamboo at player position
        // Start growth timer
    }
}
```

## Technical Constraints

1. **LibGDX Framework**: Must use LibGDX Input API for keyboard detection
2. **Existing Architecture**: Must integrate with existing Player, InventoryManager, and InventoryRenderer
3. **Menu System**: Must respect GameMenu.isAnyMenuOpen() state
4. **Rendering Pipeline**: Must properly manage SpriteBatch and ShapeRenderer state transitions

## Future Enhancements (Out of Scope)

1. Item usage actions (planting bamboo, consuming items manually)
2. Keyboard shortcuts for other actions (drop item, quick-consume)
3. Customizable key bindings
4. Visual feedback animations (pulse, glow)
5. Sound effects for selection
6. Tooltip showing selected item name
7. Hotbar-style quick access (drag items to slots)

