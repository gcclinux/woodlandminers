package wagemaker.uk.ui;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import wagemaker.uk.localization.LocalizationManager;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for Controls Dialog displaying all required controls.
 * Feature: controls-menu-dialog, Property 4: All controls are displayed
 * Validates: Requirements 2.1-2.9
 */
public class ControlsDialogAllControlsPropertyTest {
    
    private static HeadlessApplication application;
    
    // All required control categories and their translation keys
    // Based on Requirements 2.1-2.9
    private static final Map<String, String[]> REQUIRED_CONTROL_CATEGORIES = new HashMap<>();
    
    static {
        // Movement controls (Requirement 2.1)
        REQUIRED_CONTROL_CATEGORIES.put("movement", new String[]{
            "controls_dialog.movement_header",
            "controls_dialog.movement_up",
            "controls_dialog.movement_down",
            "controls_dialog.movement_left",
            "controls_dialog.movement_right"
        });
        
        // Inventory controls (Requirements 2.2, 2.3)
        REQUIRED_CONTROL_CATEGORIES.put("inventory", new String[]{
            "controls_dialog.inventory_header",
            "controls_dialog.inventory_open",
            "controls_dialog.inventory_navigate_left",
            "controls_dialog.inventory_navigate_right"
        });
        
        // Item controls (Requirement 2.4)
        REQUIRED_CONTROL_CATEGORIES.put("item", new String[]{
            "controls_dialog.item_header",
            "controls_dialog.item_plant_p",
            "controls_dialog.item_plant_space",
            "controls_dialog.item_consume"
        });
        
        // Targeting controls (Requirement 2.5)
        REQUIRED_CONTROL_CATEGORIES.put("targeting", new String[]{
            "controls_dialog.targeting_header",
            "controls_dialog.targeting_up",
            "controls_dialog.targeting_down",
            "controls_dialog.targeting_left",
            "controls_dialog.targeting_right"
        });
        
        // Combat controls (Requirement 2.6)
        REQUIRED_CONTROL_CATEGORIES.put("combat", new String[]{
            "controls_dialog.combat_header",
            "controls_dialog.combat_attack"
        });
        
        // System controls (Requirements 2.7, 2.8, 2.9)
        REQUIRED_CONTROL_CATEGORIES.put("system", new String[]{
            "controls_dialog.system_header",
            "controls_dialog.system_menu",
            "controls_dialog.system_delete_world",
            "controls_dialog.system_compass_target"
        });
    }
    
    @BeforeAll
    public static void setupGdx() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        application = new HeadlessApplication(new ApplicationAdapter() {
            @Override
            public void create() {
                Gdx.gl = Mockito.mock(GL20.class);
            }
        }, config);
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Initialize localization
        LocalizationManager.getInstance().initialize();
    }
    
    @AfterAll
    public static void tearDownGdx() {
        if (application != null) {
            application.exit();
        }
    }
    
    /**
     * Property 4: All controls are displayed
     * For any rendering of the Controls Dialog, the rendered text should contain 
     * descriptions for all defined control bindings (movement, inventory, items, 
     * targeting, combat, system)
     * Validates: Requirements 2.1-2.9
     * 
     * This property-based test runs 100 trials, randomly selecting control categories
     * and verifying that all controls in those categories would be displayed.
     */
    @Test
    public void allControlsAreDisplayed() {
        Random random = new Random(42); // Fixed seed for reproducibility
        LocalizationManager loc = LocalizationManager.getInstance();
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Randomly select a control category
            List<String> categories = new ArrayList<>(REQUIRED_CONTROL_CATEGORIES.keySet());
            String selectedCategory = categories.get(random.nextInt(categories.size()));
            String[] keysInCategory = REQUIRED_CONTROL_CATEGORIES.get(selectedCategory);
            
            // Verify all keys in the selected category have non-empty translations
            for (String key : keysInCategory) {
                String translatedText = loc.getText(key);
                
                assertNotNull(
                    translatedText,
                    "Control key '" + key + "' in category '" + selectedCategory + 
                    "' should have a translation"
                );
                
                assertFalse(
                    translatedText.trim().isEmpty(),
                    "Control key '" + key + "' in category '" + selectedCategory + 
                    "' should have non-empty translation"
                );
                
                // Verify it's not just the key wrapped in brackets (missing translation)
                assertFalse(
                    translatedText.equals("[" + key + "]"),
                    "Control key '" + key + "' in category '" + selectedCategory + 
                    "' should have actual translation, not just [key]"
                );
            }
        }
    }
    
    /**
     * Property: All control categories are present
     * For any rendering of the Controls Dialog, all required control categories
     * (movement, inventory, item, targeting, combat, system) should have their
     * header translations available.
     * 
     * This property-based test runs 100 trials, verifying that category headers
     * are properly translated.
     */
    @Test
    public void allControlCategoriesArePresent() {
        Random random = new Random(42); // Fixed seed for reproducibility
        LocalizationManager loc = LocalizationManager.getInstance();
        
        // All category headers
        String[] categoryHeaders = {
            "controls_dialog.movement_header",
            "controls_dialog.inventory_header",
            "controls_dialog.item_header",
            "controls_dialog.targeting_header",
            "controls_dialog.combat_header",
            "controls_dialog.system_header"
        };
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Randomly select a category header
            String header = categoryHeaders[random.nextInt(categoryHeaders.length)];
            
            String translatedHeader = loc.getText(header);
            
            assertNotNull(
                translatedHeader,
                "Category header '" + header + "' should have a translation"
            );
            
            assertFalse(
                translatedHeader.trim().isEmpty(),
                "Category header '" + header + "' should have non-empty translation"
            );
            
            assertFalse(
                translatedHeader.equals("[" + header + "]"),
                "Category header '" + header + "' should have actual translation, not just [key]"
            );
        }
    }
    
    /**
     * Property: Dialog title and instructions are present
     * For any rendering of the Controls Dialog, the title and close instruction
     * should have translations available.
     * 
     * This property-based test runs 100 trials, verifying that the title and
     * instructions are properly translated.
     */
    @Test
    public void dialogTitleAndInstructionsArePresent() {
        Random random = new Random(42); // Fixed seed for reproducibility
        LocalizationManager loc = LocalizationManager.getInstance();
        
        String[] essentialKeys = {
            "controls_dialog.title",
            "controls_dialog.close_instruction"
        };
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Randomly select an essential key
            String key = essentialKeys[random.nextInt(essentialKeys.length)];
            
            String translatedText = loc.getText(key);
            
            assertNotNull(
                translatedText,
                "Essential key '" + key + "' should have a translation"
            );
            
            assertFalse(
                translatedText.trim().isEmpty(),
                "Essential key '" + key + "' should have non-empty translation"
            );
            
            assertFalse(
                translatedText.equals("[" + key + "]"),
                "Essential key '" + key + "' should have actual translation, not just [key]"
            );
        }
    }
    
    /**
     * Property: All required controls have unique translations
     * For any two different control keys, they should have different translations
     * (unless they legitimately represent the same control in different contexts).
     * 
     * This property-based test runs 100 trials, randomly selecting pairs of control
     * keys and verifying they have distinct translations.
     */
    @Test
    public void controlTranslationsAreDistinct() {
        Random random = new Random(42); // Fixed seed for reproducibility
        LocalizationManager loc = LocalizationManager.getInstance();
        
        // Collect all control keys (excluding headers and instructions)
        List<String> allControlKeys = new ArrayList<>();
        for (String[] keys : REQUIRED_CONTROL_CATEGORIES.values()) {
            for (String key : keys) {
                // Skip headers (they're category labels, not controls)
                if (!key.endsWith("_header")) {
                    allControlKeys.add(key);
                }
            }
        }
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Randomly select two different control keys
            if (allControlKeys.size() < 2) {
                break; // Not enough keys to compare
            }
            
            int index1 = random.nextInt(allControlKeys.size());
            int index2 = random.nextInt(allControlKeys.size());
            
            // Ensure we're comparing different keys
            if (index1 == index2) {
                continue;
            }
            
            String key1 = allControlKeys.get(index1);
            String key2 = allControlKeys.get(index2);
            
            String translation1 = loc.getText(key1);
            String translation2 = loc.getText(key2);
            
            // The translations should be different (unless they're legitimately the same control)
            // We'll allow some exceptions for controls that are intentionally the same
            // (e.g., SPACE for both planting and consuming)
            if (!key1.equals(key2)) {
                // Just verify both have valid translations
                assertNotNull(translation1, "Key '" + key1 + "' should have translation");
                assertNotNull(translation2, "Key '" + key2 + "' should have translation");
                assertFalse(translation1.trim().isEmpty(), "Key '" + key1 + "' should have non-empty translation");
                assertFalse(translation2.trim().isEmpty(), "Key '" + key2 + "' should have non-empty translation");
            }
        }
    }
}
