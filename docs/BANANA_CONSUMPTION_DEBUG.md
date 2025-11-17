# Banana Consumption Debug Guide

## Issue RESOLVED ✅
User was unable to consume bananas when hungry because the targeting system was being activated for ALL items, including consumables.

## Root Cause
When selecting any item (including bananas and apples), the inventory selection logic was automatically activating the targeting system. This caused the space bar to attempt planting instead of consuming.

## Fix Applied
Modified `Player.handleInventorySelection()` to only activate targeting for **plantable items** (baby bamboo, bamboo stack, wood stack, pebbles). Consumable items (apples, bananas) now do NOT activate targeting, allowing space bar to consume them directly.

## Important Note
**Bananas reduce HUNGER, not restore health!**
- **Apple**: Restores 10% health (use when damaged)
- **Banana**: Reduces 5% hunger (use when hungry)

## How to Consume Items

1. **Select the item**: Press the number key for the inventory slot
   - Press `1` for slot 1 (first slot)
   - Press `2` for slot 2 (second slot)
   - etc.

2. **Consume the item**: Press `SPACE BAR`
   - The item will be consumed if you have it in inventory
   - You'll see a message in the console

## Debug Logging Added

I've added detailed debug logging to help diagnose the issue. When you press the space bar, you'll see:

```
[SPACEBAR] Space bar pressed!
[SPACEBAR] Targeting active: false
[SPACEBAR] Selected slot: 1
[SPACEBAR] Attempting to consume selected item
[CONSUME] Selected slot: 1
[CONSUME] Selected item type: BANANA
[CONSUME] Restores health: false
[CONSUME] Reduces hunger: true
[CONSUME] Attempting to consume item...
[CONSUME] Current health: 100.0, Current hunger: 10.0
[CONSUME] Successfully consumed item!
[CONSUME] New health: 100.0, New hunger: 5.0
Consumed BANANA - reduced 5% hunger
```

## Testing Steps

1. **Start the game**
2. **Wait for hunger to accumulate** (or use debug to set hunger)
3. **Collect a banana** (destroy a banana tree)
4. **Open your inventory** and note which slot the banana is in
5. **Select the banana** by pressing the number key (e.g., `2` for slot 2)
6. **Press SPACE BAR** to consume
7. **Check the console output** for debug messages

## Common Issues

### Issue 1: Targeting Mode Active
If you have a plantable item selected (baby bamboo, bamboo stack, etc.), the targeting system might be active. This causes space bar to plant instead of consume.

**Solution**: Press `ESC` to cancel targeting, then try consuming again.

### Issue 2: Wrong Slot Selected
Make sure you're pressing the correct number key for the slot containing the banana.

**Solution**: Check your inventory UI to see which slot has the banana, then press that number key.

### Issue 3: No Banana in Inventory
If you don't have a banana, consumption will fail silently.

**Solution**: Collect a banana first by destroying a banana tree.

### Issue 4: Menu Open
If any menu is open, inventory selection might not work.

**Solution**: Close all menus (press `ESC`) and try again.

## Expected Behavior

When you successfully consume a banana:
1. The banana count in your inventory decreases by 1
2. Your hunger decreases by 5%
3. The blue hunger bar (if visible) should shrink
4. Console shows success message

## What to Look For

Run the game and try to consume a banana. Send me the console output, especially:
- What happens when you press the number key to select the banana
- What happens when you press space bar
- Any error messages or unexpected behavior

This will help me identify exactly what's going wrong!
