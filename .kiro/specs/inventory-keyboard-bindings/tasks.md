# Implementation Plan

- [x] 1. Add selection state to InventoryManager

  - Add `selectedSlot` field (int, default -1 for no selection)
  - Implement `setSelectedSlot(int slot)` method with validation (0-4 valid, else -1)
  - Implement `getSelectedSlot()` method to return current selection
  - Implement `clearSelection()` method to reset to -1
  - Implement `getSelectedItemType()` method to map slot index to ItemType enum
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [x] 2. Add keyboard input handling to Player class

  - Create `handleInventorySelection()` private method in Player class
  - Check for null inventoryManager before processing input
  - Use `Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)` through `NUM_5` to detect key presses
  - Call `inventoryManager.setSelectedSlot(0-4)` for each corresponding key
  - Call `handleInventorySelection()` from `Player.update()` method only when menu is not open
  - Add menu state check using `gameMenu.isAnyMenuOpen()` before processing input
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 5.1, 5.2, 5.3, 5.4_

- [x] 3. Add visual selection highlight to InventoryRenderer

  - Add ShapeRenderer field to InventoryRenderer class
  - Add selection highlight color constants (RGB: 1.0f, 0.84f, 0.0f, alpha: 0.8f)
  - Add highlight border width constant (3 pixels)
  - Modify `render()` method signature to accept `int selectedSlot` parameter
  - Modify `renderSlot()` method signature to accept `boolean isSelected` parameter
  - Implement selection highlight rendering in `renderSlot()` using ShapeRenderer
  - Draw highlight border (44x44) around selected slot before rendering slot content
  - Properly manage batch.end() and batch.begin() around ShapeRenderer usage
  - Pass selection state from render() to each renderSlot() call
  - Add ShapeRenderer disposal in dispose() method
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 4. Update MyGdxGame to pass selection state to renderer

  - Modify inventory rendering code in `MyGdxGame.render()` method
  - Get selected slot from inventoryManager using `getSelectedSlot()`
  - Pass selectedSlot as parameter to `inventoryRenderer.render()` call
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [x] 5. Add visual selection marker above selected inventory slot

  - Add marker rendering code to `renderSlot()` method in InventoryRenderer
  - Use ShapeRenderer in Filled mode to draw a downward-pointing triangle
  - Position marker centered horizontally above the selected slot (4 pixels above top edge)
  - Set marker size to 8x8 pixels (4 pixels on each side of center)
  - Use black color for better contrast (RGB: 0.0f, 0.0f, 0.0f) with full opacity
  - Calculate triangle vertices: top point at (centerX, topY + 8), bottom left at (centerX - 4, topY), bottom right at (centerX + 4, topY)
  - Properly manage batch.end() and batch.begin() around ShapeRenderer usage
  - Render marker after slot content to ensure it appears on top
  - _Requirements: 3.6, 3.7, 3.8, 3.9_

