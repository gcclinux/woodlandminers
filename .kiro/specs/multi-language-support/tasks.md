# Implementation Plan

- [x] 1. Create LocalizationManager core infrastructure

  - Create LocalizationManager singleton class with language detection and file loading
  - Implement system locale detection with mapping to supported languages (en, pl, pt, nl)
  - Implement JSON language file loading with error handling and fallback to English
  - Implement text retrieval methods with parameter substitution support
  - Implement language preference persistence (save/load from player config)
  - Implement manual language switching with listener notification system
  - Implement LanguageChangeListener interface for UI components
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.4, 2.5, 3.1, 3.2, 3.3, 3.4, 3.5, 9.1, 9.2, 9.3, 9.4, 9.5, 10.6, 10.7, 11.1, 11.2, 11.3, 11.4, 11.5, 11.6_

- [x] 2. Create English language file with all translations

  - Extract all hardcoded text strings from UI components
  - Organize translation keys by component category (menu, dialogs, messages)
  - Add language selection dialog translations with native language names
  - Create complete en.json file with all identified text strings
  - Validate JSON structure and key naming conventions
  - _Requirements: 2.1, 2.2, 2.3, 8.1, 8.5, 10.2_

- [x] 3. Create Polish language file

  - Translate all English keys to Polish
  - Maintain same JSON structure as English file
  - Validate completeness against English file
  - _Requirements: 2.1, 2.2, 2.3, 8.2, 8.5_

- [x] 4. Create Portuguese language file

  - Translate all English keys to Portuguese
  - Maintain same JSON structure as English file
  - Validate completeness against English file
  - _Requirements: 2.1, 2.2, 2.3, 8.3, 8.5_

- [x] 5. Create Dutch language file

  - Translate all English keys to Dutch
  - Maintain same JSON structure as English file
  - Validate completeness against English file
  - _Requirements: 2.1, 2.2, 2.3, 8.4, 8.5_

- [x] 6. Create LanguageDialog component

  - Create LanguageDialog class with wooden plank style
  - Implement language list display with native names (English, Polski, Português, Nederlands)
  - Implement keyboard navigation (arrow keys, Enter, ESC)
  - Implement LanguageChangeListener to refresh dialog on language change
  - Integrate with LocalizationManager for language switching
  - _Requirements: 10.1, 10.2, 10.3_

- [x] 7. Integrate LocalizationManager into GameMenu

  - Add "Language" menu option to both singleplayer and multiplayer menus
  - Replace hardcoded menu item strings with localization calls
  - Implement LanguageChangeListener to refresh menu items on language change
  - Replace player name dialog strings with localization calls
  - Replace error messages with localization calls
  - Replace validation messages with localization calls
  - Wire up Language menu option to open LanguageDialog
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 10.1, 10.4, 10.5_

- [x] 8. Integrate LocalizationManager into MultiplayerMenu

  - Replace hardcoded menu option strings with localization calls
  - Replace menu title with localization call
  - Replace navigation instructions with localization calls
  - Implement LanguageChangeListener to refresh menu on language change
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 10.4_

- [x] 9. Integrate LocalizationManager into ConnectDialog

  - Replace dialog title with localization call
  - Replace label strings with localization calls
  - Replace instruction strings with localization calls
  - Replace validation messages with localization calls
  - Implement LanguageChangeListener to refresh dialog on language change
  - _Requirements: 6.1, 10.4_

- [x] 10. Integrate LocalizationManager into ServerHostDialog

  - Replace dialog title with localization call
  - Replace label strings with localization calls
  - Replace instruction strings with localization calls
  - Implement LanguageChangeListener to refresh dialog on language change
  - _Requirements: 6.2, 10.4_

- [x] 11. Integrate LocalizationManager into ErrorDialog

  - Replace dialog titles with localization calls
  - Replace button labels with localization calls
  - Replace instruction strings with localization calls
  - Support dynamic error messages with parameters
  - Implement LanguageChangeListener to refresh dialog on language change
  - _Requirements: 6.3, 7.1, 7.2, 7.3, 7.4, 10.4_

- [x] 12. Integrate LocalizationManager into WorldSaveDialog

  - Replace dialog title and labels with localization calls
  - Replace instruction strings with localization calls
  - Replace validation messages with localization calls
  - Replace confirmation prompts with localization calls
  - Support dynamic character count display with parameters
  - Implement LanguageChangeListener to refresh dialog on language change
  - _Requirements: 6.4, 7.1, 7.2, 7.3, 7.4, 10.4_

- [x] 13. Integrate LocalizationManager into WorldLoadDialog

  - Replace dialog title and labels with localization calls
  - Replace instruction strings with localization calls
  - Replace status messages with localization calls
  - Replace confirmation prompts with localization calls
  - Support dynamic save count display with parameters
  - Implement LanguageChangeListener to refresh dialog on language change
  - _Requirements: 6.5, 7.1, 7.2, 7.3, 7.4, 10.4_

- [x] 14. Integrate LocalizationManager into WorldManageDialog

  - Replace dialog title and labels with localization calls
  - Replace instruction strings with localization calls
  - Replace confirmation prompts with localization calls
  - Replace statistics labels with localization calls
  - Support dynamic statistics display with parameters
  - Implement LanguageChangeListener to refresh dialog on language change
  - _Requirements: 6.6, 7.1, 7.2, 7.3, 7.4, 10.4_

- [x] 15. Verify and test localization system



  - Test language detection for all supported locales
  - Test fallback behavior for unsupported locales
  - Test manual language selection from Language menu
  - Test language preference persistence across game restarts
  - Test all UI components display correct translations
  - Test all UI components refresh when language changes
  - Test parameter substitution in dynamic messages
  - Test error handling for missing keys and files
  - Verify translation completeness for all languages
  - _Requirements: 1.1, 1.2, 1.3, 3.1, 3.2, 3.3, 3.4, 9.1, 9.2, 9.3, 9.4, 9.5, 10.3, 10.4, 10.5, 10.6, 10.7_
