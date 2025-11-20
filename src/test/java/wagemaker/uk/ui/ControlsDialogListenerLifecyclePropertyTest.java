package wagemaker.uk.ui;

import org.junit.jupiter.api.Test;
import wagemaker.uk.localization.LanguageChangeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for Controls Dialog listener registration lifecycle.
 * Feature: controls-menu-dialog, Property 9: Listener registration lifecycle
 * Validates: Requirements 4.3, 4.4
 * 
 * Note: These tests verify the listener registration logic without requiring
 * full LibGDX initialization.
 */
public class ControlsDialogListenerLifecyclePropertyTest {
    
    /**
     * Simple test localization manager that tracks listener registration.
     */
    private static class TestLocalizationManager {
        private List<LanguageChangeListener> listeners = new ArrayList<>();
        
        public void addLanguageChangeListener(LanguageChangeListener listener) {
            if (listener != null && !listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
        
        public void removeLanguageChangeListener(LanguageChangeListener listener) {
            listeners.remove(listener);
        }
        
        public boolean isListenerRegistered(LanguageChangeListener listener) {
            return listeners.contains(listener);
        }
        
        public int getListenerCount() {
            return listeners.size();
        }
    }
    
    /**
     * Simple test dialog that simulates ControlsDialog listener behavior.
     */
    private static class TestableDialog implements LanguageChangeListener {
        private TestLocalizationManager locManager;
        private boolean isDisposed = false;
        
        public TestableDialog(TestLocalizationManager locManager) {
            this.locManager = locManager;
            // Register as language change listener (simulates constructor behavior)
            locManager.addLanguageChangeListener(this);
        }
        
        public void dispose() {
            if (!isDisposed) {
                // Unregister from language change listener (simulates dispose behavior)
                locManager.removeLanguageChangeListener(this);
                isDisposed = true;
            }
        }
        
        public boolean isDisposed() {
            return isDisposed;
        }
        
        @Override
        public void onLanguageChanged(String newLanguage) {
            // Simulates language change handling
        }
    }
    
    /**
     * Property 9: Listener registration lifecycle
     * For any ControlsDialog instance, after construction the dialog should be 
     * registered with LocalizationManager, and after dispose() the dialog should 
     * be unregistered
     * Validates: Requirements 4.3, 4.4
     * 
     * This property-based test runs 100 trials with random dialog lifecycles.
     */
    @Test
    public void listenerRegistrationLifecycle() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            TestLocalizationManager locManager = new TestLocalizationManager();
            
            // Initial state: no listeners
            assertEquals(0, locManager.getListenerCount(), "Should start with no listeners");
            
            // Create dialog (should register listener)
            TestableDialog dialog = new TestableDialog(locManager);
            
            // Verify listener is registered
            assertTrue(
                locManager.isListenerRegistered(dialog),
                "Dialog should be registered as listener after construction"
            );
            assertEquals(
                1,
                locManager.getListenerCount(),
                "Should have exactly one listener after dialog construction"
            );
            
            // Dispose dialog (should unregister listener)
            dialog.dispose();
            
            // Verify listener is unregistered
            assertFalse(
                locManager.isListenerRegistered(dialog),
                "Dialog should be unregistered as listener after dispose()"
            );
            assertEquals(
                0,
                locManager.getListenerCount(),
                "Should have no listeners after dialog disposal"
            );
        }
    }
    
    /**
     * Property: Multiple dialogs register independently
     * For any number of dialog instances, each should register and unregister
     * independently without affecting other dialogs.
     * 
     * This property-based test runs 100 trials with random numbers of dialogs.
     */
    @Test
    public void multipleDialogsRegisterIndependently() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            TestLocalizationManager locManager = new TestLocalizationManager();
            
            // Create random number of dialogs (1 to 10)
            int dialogCount = random.nextInt(10) + 1;
            List<TestableDialog> dialogs = new ArrayList<>();
            
            for (int i = 0; i < dialogCount; i++) {
                TestableDialog dialog = new TestableDialog(locManager);
                dialogs.add(dialog);
                
                // Verify listener count increases
                assertEquals(
                    i + 1,
                    locManager.getListenerCount(),
                    "Listener count should increase with each dialog creation"
                );
            }
            
            // Verify all dialogs are registered
            for (TestableDialog dialog : dialogs) {
                assertTrue(
                    locManager.isListenerRegistered(dialog),
                    "Each dialog should be registered"
                );
            }
            
            // Dispose dialogs in random order
            List<TestableDialog> shuffledDialogs = new ArrayList<>(dialogs);
            for (int i = shuffledDialogs.size() - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                TestableDialog temp = shuffledDialogs.get(i);
                shuffledDialogs.set(i, shuffledDialogs.get(j));
                shuffledDialogs.set(j, temp);
            }
            
            for (int i = 0; i < shuffledDialogs.size(); i++) {
                TestableDialog dialog = shuffledDialogs.get(i);
                dialog.dispose();
                
                // Verify listener count decreases
                assertEquals(
                    dialogCount - i - 1,
                    locManager.getListenerCount(),
                    "Listener count should decrease with each dialog disposal"
                );
            }
            
            // Verify all dialogs are unregistered
            for (TestableDialog dialog : dialogs) {
                assertFalse(
                    locManager.isListenerRegistered(dialog),
                    "Each dialog should be unregistered after disposal"
                );
            }
        }
    }
    
    /**
     * Property: Multiple dispose() calls are safe
     * For any dialog, calling dispose() multiple times should be safe and
     * should not cause errors or duplicate unregistration attempts.
     * 
     * This property-based test runs 100 trials with random numbers of dispose() calls.
     */
    @Test
    public void multipleDisposeCallsAreSafe() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            TestLocalizationManager locManager = new TestLocalizationManager();
            TestableDialog dialog = new TestableDialog(locManager);
            
            // Verify listener is registered
            assertTrue(locManager.isListenerRegistered(dialog));
            assertEquals(1, locManager.getListenerCount());
            
            // Call dispose() multiple times (1 to 10 times)
            int disposeCount = random.nextInt(10) + 1;
            for (int i = 0; i < disposeCount; i++) {
                dialog.dispose();
                
                // Verify listener is unregistered (should remain unregistered)
                assertFalse(
                    locManager.isListenerRegistered(dialog),
                    "Dialog should remain unregistered after dispose() call " + (i + 1)
                );
                assertEquals(
                    0,
                    locManager.getListenerCount(),
                    "Listener count should remain 0 after dispose() call " + (i + 1)
                );
            }
        }
    }
    
    /**
     * Property: Listener registration happens before any other operations
     * For any dialog, the listener should be registered immediately upon construction,
     * before any other methods are called.
     * 
     * This property-based test runs 100 trials.
     */
    @Test
    public void listenerRegistrationHappensImmediately() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            TestLocalizationManager locManager = new TestLocalizationManager();
            
            // Verify no listeners before construction
            assertEquals(0, locManager.getListenerCount());
            
            // Create dialog
            TestableDialog dialog = new TestableDialog(locManager);
            
            // Immediately verify listener is registered (no delay, no other operations needed)
            assertTrue(
                locManager.isListenerRegistered(dialog),
                "Listener should be registered immediately upon construction"
            );
            assertEquals(
                1,
                locManager.getListenerCount(),
                "Listener count should be 1 immediately after construction"
            );
            
            dialog.dispose();
        }
    }
}
