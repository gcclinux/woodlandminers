# Implementation Plan

- [x] 1. Add translation keys to all language files

  - Add all required translation keys to en.json, de.json, nl.json, pl.json, pt.json
  - Include keys for: menu item, dialog title, category headers, control descriptions, instructions
  - Ensure consistent formatting and proper character encoding for special characters
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_

- [x] 1.1 Write property test for translation key completeness

  - **Property 7: Translation keys exist in all languages**
  - **Validates: Requirements 5.1, 5.2-5.6**

- [x] 2. Create ControlsDialog class

  - Create new file `src/main/java/wagemaker/uk/ui/ControlsDialog.java`
  - Implement LanguageChangeListener interface
  - Add fields for texture, font, visibility state
  - Implement constructor with texture and font initialization
  - _Requirements: 4.1, 4.2_

- [x] 2.1 Implement wooden plank texture generation

  - Create createWoodenPlank() method matching existing dialog pattern
  - Use same dimensions (DIALOG_WIDTH = 700, DIALOG_HEIGHT = 600)
  - Apply wood grain lines and border styling
  - _Requirements: 3.1, 3.5_

- [x] 2.2 Implement font loading

  - Create createDialogFont() method using Sancreek-Regular.ttf
  - Set font size to 16 with border and shadow effects
  - Include extended character set for all supported languages
  - Implement fallback to default font on error
  - _Requirements: 3.2_

- [x] 2.3 Implement show/hide methods

  - Implement show() to set isVisible = true
  - Implement hide() to set isVisible = false
  - Implement isVisible() getter
  - _Requirements: 1.2, 1.4_

- [x] 2.4 Write property test for dialog visibility toggle


  - **Property 1: Dialog visibility toggles correctly**
  - **Validates: Requirements 1.2, 1.4**

- [x] 2.4 Implement input handling

  - Create handleInput() method
  - Check for ESC key press when dialog is visible
  - Call hide() when ESC is pressed
  - _Requirements: 1.4_

- [x] 2.5 Write property test for ESC key closes dialog

  - **Property 2: ESC key closes dialog**
  - **Validates: Requirements 1.4**

- [x] 2.5 Implement render method

  - Create render(SpriteBatch, ShapeRenderer, float camX, float camY) method
  - Calculate centered dialog position using camera coordinates
  - Draw wooden plank background
  - Retrieve all text from LocalizationManager
  - Render dialog title, category headers, and control descriptions
  - Apply consistent color scheme (white, yellow, light gray)
  - _Requirements: 2.1-2.9, 3.3, 3.4, 5.7_

- [x] 2.6 Write property test for dialog centering

  - **Property 5: Dialog centers on camera**
  - **Validates: Requirements 3.4**

- [x] 2.7 Write property test for no hardcoded strings

  - **Property 8: No hardcoded strings in rendering**
  - **Validates: Requirements 5.7**

- [x] 2.6 Implement localization support

  - Implement onLanguageChanged(String) method from LanguageChangeListener
  - Register with LocalizationManager in constructor
  - Unregister in dispose() method
  - _Requirements: 4.2, 4.3, 4.4, 4.5_

- [x] 2.8 Write property test for language change updates


  - **Property 6: Language changes update text**
  - **Validates: Requirements 1.5, 4.5**

- [x] 2.9 Write property test for listener registration lifecycle

  - **Property 9: Listener registration lifecycle**
  - **Validates: Requirements 4.3, 4.4**

- [x] 2.7 Implement dispose method

  - Dispose wooden plank texture with null check
  - Dispose dialog font with null check
  - Unregister from LocalizationManager
  - _Requirements: 4.4_

- [x] 3. Integrate ControlsDialog into GameMenu

  - Add ControlsDialog field to GameMenu class
  - Initialize ControlsDialog in GameMenu constructor
  - Add "Controls" to singleplayerMenuItems array (after Player Location, before Save World)
  - Add "Controls" to multiplayerMenuItems array (after Player Location, before Save World)
  - _Requirements: 1.1_

- [x] 3.1 Update GameMenu dimensions

  - Increase MENU_HEIGHT constant from 310 to 340
  - Verify menu items fit properly with new height
  - _Requirements: 1.6, 1.7_

- [x] 3.2 Add Controls dialog to update loop

  - Add controlsDialog.isVisible() check in update() method
  - Add controlsDialog.handleInput() call when visible
  - Return early to prevent game input when dialog is visible
  - _Requirements: 1.3_

- [x] 3.3 Write property test for dialog prevents game input


  - **Property 3: Dialog prevents game input**
  - **Validates: Requirements 1.3**

- [x] 3.3 Add Controls dialog to render methods

  - Add controlsDialog.render() call in GameMenu render methods
  - Pass batch, shapeRenderer, camX, camY parameters
  - Render after other dialogs to ensure proper z-ordering
  - _Requirements: 1.2_

- [x] 3.4 Add menu selection handler

  - Add case for "Controls" menu item in handleMenuSelection()
  - Call controlsDialog.show() when Controls is selected
  - _Requirements: 1.2_

- [x] 3.5 Add dialog disposal

  - Call controlsDialog.dispose() in GameMenu.dispose()
  - _Requirements: 4.4_

- [x] 3.6 Write unit test for menu integration


  - Verify "Controls" menu item exists in both singleplayer and multiplayer menus
  - Verify MENU_HEIGHT is increased to 340
  - _Requirements: 1.1, 1.6_

- [x] 4. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Write property test for all controls displayed

  - **Property 4: All controls are displayed**
  - **Validates: Requirements 2.1-2.9**

- [x] 6. Write integration tests

  - Test menu navigation to Controls dialog
  - Test Controls dialog with language changes
  - Test Controls dialog with other dialogs
  - _Requirements: 1.1, 1.2, 1.4, 1.5_

- [x] 7. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
