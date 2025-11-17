# Item Consumption Fix

## Problem
Users were unable to consume apples and bananas because the targeting system was being activated for ALL selected items, causing the space bar to attempt planting instead of consuming.

## Root Cause
In `Player.handleInventorySelection()`, when any item was selected, the targeting system was automatically activated:

```java
// OLD CODE - BROKEN
if (newSelection != -1) {
    // Item selected - activate targeting at player position
    targetingSystem.activate(...);
}
```

This meant that selecting a banana or apple would activate targeting mode, and pressing space bar would try to plant the item (which fails with "Selected item cannot be placed") instead of consuming it.

## Solution
Modified the inventory selection logic to only activate targeting for **plantable items**:

```java
// NEW CODE - FIXED
if (newSelection != -1) {
    wagemaker.uk.inventory.ItemType selectedItemType = inventoryManager.getSelectedItemType();
    
    // Only activate targeting for plantable items (not consumables)
    boolean isPlantable = selectedItemType != null && 
                         !selectedItemType.restoresHealth() && 
                         !selectedItemType.reducesHunger();
    
    if (isPlantable) {
        // Plantable item - activate targeting
        targetingSystem.activate(...);
    } else {
        // Consumable item - deactivate targeting if active
        if (targetingSystem.isActive()) {
            targetingSystem.deactivate();
        }
    }
}
```

## Item Categories

### Consumable Items (No Targeting)
- **Apple**: Restores 10% health
- **Banana**: Reduces 5% hunger

When selected, these items do NOT activate targeting. Press SPACE to consume directly.

### Plantable Items (Activates Targeting)
- **Baby Bamboo**: Can be planted
- **Bamboo Stack**: Can be placed
- **Wood Stack**: Can be placed
- **Pebble**: Can be placed

When selected, these items activate the targeting system. Use WASD to move target, SPACE or P to place.

## How to Use

### Consuming Items
1. Press number key (1-6) to select apple or banana
2. Press SPACE BAR to consume
3. Item is consumed, health restored or hunger reduced

### Planting Items
1. Press number key (1-6) to select plantable item
2. Targeting system activates automatically
3. Use WASD to move the target cursor
4. Press SPACE or P to place the item
5. Press ESC to cancel

## Files Modified
- `src/main/java/wagemaker/uk/player/Player.java` - Fixed `handleInventorySelection()` method

## Testing
- [x] Select apple → targeting should NOT activate
- [x] Press space with apple selected → should consume apple
- [x] Select banana → targeting should NOT activate
- [x] Press space with banana selected → should consume banana
- [x] Select baby bamboo → targeting SHOULD activate
- [x] Press space with baby bamboo selected → should plant item
- [ ] Verify in multiplayer mode
- [ ] Verify consumption syncs correctly across clients
