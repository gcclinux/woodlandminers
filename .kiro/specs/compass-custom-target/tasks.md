# Implementation Plan

- [x] 1. Add localization keys for Player Location feature
  - Add "player_location" key to menu section in all localization files (en.json, de.json, nl.json, pl.json, pt.json)
  - Add "player_location_dialog" section with all required text keys (title, current_location, new_target, set_target, reset_origin, instructions, error messages)
  - _Requirements: 1.1, 2.1, 3.1, 4.1_

- [x] 2. Extend Compass class to support custom targets
  - Add private fields: targetX, targetY, useCustomTarget
  - Modify update() method to use custom target when useCustomTarget is true
  - Implement setCustomTarget(float x, float y) method
  - Implement resetToOrigin() method
  - Implement getter methods: hasCustomTarget(), getTargetX(), getTargetY()
  - _Requirements: 2.2, 3.2_

- [x] 3. Extend PlayerConfig class for compass target persistence
  - Add private fields: compassTargetX, compassTargetY (Float objects to allow null)
  - Implement getCompassTargetX() and getCompassTargetY() methods
  - Implement saveCompassTarget(Float x, Float y) method
  - Implement clearCompassTarget() method
  - Update load() method to parse compassTarget from JSON
  - Update save() method to include compassTarget in JSON output
  - Update updateJsonField() or create new method to handle nested compassTarget object
  - _Requirements: 5.1, 5.2, 5.3, 5.4_

- [x] 4. Create PlayerLocationDialog class
  - Create new file src/main/java/wagemaker/uk/ui/PlayerLocationDialog.java
  - Implement dialog structure following existing dialog patterns (ConnectDialog, NameDialog)
  - Add fields: isVisible, targetXInput, targetYInput, activeField, player, compass, playerConfig
  - Create wooden plank texture (400x300 pixels) using createWoodenPlank() pattern
  - Load Sancreek font for text rendering
  - _Requirements: 1.2, 4.2, 4.3_

- [x] 5. Implement PlayerLocationDialog input handling
  - Implement show(Player player, Compass compass, PlayerConfig config) method
  - Implement hide() method
  - Implement handleInput() method for keyboard input (Tab, Enter, ESC, Backspace, numeric input)
  - Implement field switching with Tab key (activeField toggle between 0 and 1)
  - Implement numeric input validation (digits, decimal point, minus sign)
  - Implement validateCoordinates() method to check if inputs are valid numbers
  - _Requirements: 2.1, 2.2, 2.4, 2.5_

- [x] 6. Implement PlayerLocationDialog rendering
  - Implement render(SpriteBatch batch, ShapeRenderer shapeRenderer, float camX, float camY) method
  - Render wooden plank background centered on screen
  - Render title "Player Coordinates" at top
  - Render "Current Location" section with player's current X and Y coordinates
  - Render "New Target Location" section with input fields for target X and Y
  - Render input fields with yellow highlight for active field, white for inactive
  - Render cursor (underscore) in active field
  - Render instructions at bottom
  - Render error message if validation fails
  - _Requirements: 1.3, 1.4, 4.1, 4.2, 4.3, 4.4_

- [x] 7. Implement PlayerLocationDialog action methods
  - Implement applyCustomTarget() method to parse inputs and call compass.setCustomTarget()
  - Implement resetToOrigin() method to call compass.resetToOrigin()
  - Save compass target to PlayerConfig when applying custom target
  - Clear compass target from PlayerConfig when resetting to origin
  - Close dialog after successful action
  - _Requirements: 2.2, 2.3, 3.2, 5.1_

- [x] 8. Integrate PlayerLocationDialog into GameMenu
  - Add PlayerLocationDialog instance field to GameMenu
  - Initialize PlayerLocationDialog in GameMenu constructor
  - Add "Player Location" menu item to singleplayerMenuItems array (after "Player Name")
  - Add "Player Location" menu item to multiplayerMenuItems array (after "Player Name")
  - Update updateMenuItems() to include localized "player_location" text
  - Add Compass field and setCompass(Compass compass) method to GameMenu
  - Add PlayerConfig field to GameMenu (if not already present)
  - _Requirements: 1.1_

- [x] 9. Implement PlayerLocationDialog menu integration
  - Add openPlayerLocationDialog() method to GameMenu
  - Add case in executeMenuItem() for "Player Location" menu item
  - Add PlayerLocationDialog handling in update() method (check isVisible, call handleInput)
  - Add PlayerLocationDialog rendering in render() method (check isVisible, call render)
  - Ensure dialog closes main menu when opened
  - Ensure main menu reopens when dialog is cancelled
  - _Requirements: 1.1, 1.2, 4.5_

- [x] 10. Initialize compass target on game start
  - Modify MyGdxGame.create() or initialization method
  - Load PlayerConfig at game start
  - Get compass target coordinates from PlayerConfig
  - If compass target exists (not null), call compass.setCustomTarget()
  - Pass Compass reference to GameMenu via setCompass()
  - Pass PlayerConfig reference to GameMenu (if needed)
  - _Requirements: 5.2, 5.3_

- [x] 11. Handle dialog lifecycle and state management
  - Implement isVisible() method in PlayerLocationDialog
  - Update GameMenu.isAnyMenuOpen() to include PlayerLocationDialog.isVisible()
  - Ensure PlayerLocationDialog blocks game input when visible
  - Ensure dialog state resets properly when closed
  - Clear input buffers when dialog is cancelled
  - _Requirements: 4.5_

- [x] 12. Add dispose() method to PlayerLocationDialog
  - Implement dispose() method to clean up textures and fonts
  - Call dispose() from GameMenu.dispose() if it exists
  - Ensure no memory leaks from dialog resources
  - _Requirements: 4.1_
