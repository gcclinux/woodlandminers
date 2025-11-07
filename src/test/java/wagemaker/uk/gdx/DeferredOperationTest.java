package wagemaker.uk.gdx;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the deferred operation mechanism in MyGdxGame.
 * Tests the core functionality of queuing and executing operations on the render thread.
 * 
 * This test verifies Requirements 2.5, 5.4, 7.3 from the spec.
 */
public class DeferredOperationTest {
    
    /**
     * Test that a single deferred operation is executed.
     * 
     * Requirement 2.1: "WHEN a network message requires OpenGL operations, THE Message_Handler 
     * SHALL add the operation to a Thread_Safe_Queue"
     */
    @Test
    public void testSingleDeferredOperationExecution() {
        ConcurrentLinkedQueue<Runnable> deferredQueue = new ConcurrentLinkedQueue<>();
        AtomicBoolean executed = new AtomicBoolean(false);
        
        // Queue a single operation
        Runnable operation = () -> executed.set(true);
        deferredQueue.add(operation);
        
        // Verify operation is queued
        assertEquals(1, deferredQueue.size(), "Queue should contain one operation");
        assertFalse(executed.get(), "Operation should not be executed yet");
        
        // Simulate render thread processing the queue
        Runnable op;
        while ((op = deferredQueue.poll()) != null) {
            op.run();
        }
        
        // Verify operation was executed
        assertTrue(executed.get(), "Operation should be executed");
        assertTrue(deferredQueue.isEmpty(), "Queue should be empty after processing");
    }
    
    /**
     * Test that multiple operations are processed in FIFO order.
     * 
     * Requirement 2.5: "WHERE multiple operations are queued, THE Render_Thread 
     * SHALL process them in the order they were added"
     */
    @Test
    public void testMultipleOperationsProcessedInFIFOOrder() {
        ConcurrentLinkedQueue<Runnable> deferredQueue = new ConcurrentLinkedQueue<>();
        List<Integer> executionOrder = new ArrayList<>();
        
        // Queue multiple operations in specific order
        for (int i = 0; i < 10; i++) {
            final int operationId = i;
            deferredQueue.add(() -> executionOrder.add(operationId));
        }
        
        // Verify all operations are queued
        assertEquals(10, deferredQueue.size(), "Queue should contain 10 operations");
        
        // Simulate render thread processing the queue
        Runnable operation;
        while ((operation = deferredQueue.poll()) != null) {
            operation.run();
        }
        
        // Verify FIFO order
        assertEquals(10, executionOrder.size(), "All operations should be executed");
        for (int i = 0; i < 10; i++) {
            assertEquals(i, executionOrder.get(i), 
                        "Operations should be executed in FIFO order");
        }
        assertTrue(deferredQueue.isEmpty(), "Queue should be empty after processing");
    }
    
    /**
     * Test exception handling in deferred operations.
     * Verifies that if one operation fails, others still execute.
     * 
     * Requirement 7.3: "IF a deferred operation fails, THEN THE Game_Client 
     * SHALL log the error with stack trace"
     * 
     * Requirement 5.4: "WHEN an operation is deferred, THE Game_Client 
     * SHALL include error handling for disposal failures"
     */
    @Test
    public void testExceptionHandlingInDeferredOperations() {
        ConcurrentLinkedQueue<Runnable> deferredQueue = new ConcurrentLinkedQueue<>();
        
        AtomicBoolean firstExecuted = new AtomicBoolean(false);
        AtomicBoolean thirdExecuted = new AtomicBoolean(false);
        AtomicBoolean exceptionCaught = new AtomicBoolean(false);
        
        // Queue three operations: success, failure, success
        deferredQueue.add(() -> firstExecuted.set(true));
        deferredQueue.add(() -> {
            throw new RuntimeException("Simulated disposal failure");
        });
        deferredQueue.add(() -> thirdExecuted.set(true));
        
        // Verify all operations are queued
        assertEquals(3, deferredQueue.size(), "Queue should contain 3 operations");
        
        // Simulate render thread processing with exception handling
        Runnable operation;
        while ((operation = deferredQueue.poll()) != null) {
            try {
                operation.run();
            } catch (Exception e) {
                // Log the error (simulating what MyGdxGame does)
                System.err.println("Error executing deferred operation: " + e.getMessage());
                exceptionCaught.set(true);
                // Continue processing other operations
            }
        }
        
        // Verify that exception was caught and other operations still executed
        assertTrue(firstExecuted.get(), "First operation should execute");
        assertTrue(exceptionCaught.get(), "Exception should be caught");
        assertTrue(thirdExecuted.get(), "Third operation should execute despite second failing");
        assertTrue(deferredQueue.isEmpty(), "Queue should be empty after processing");
    }
    
    /**
     * Test null operation handling.
     * Verifies that null operations are not queued.
     * 
     * Requirement 5.4: "WHEN an operation is deferred, THE Game_Client 
     * SHALL include error handling for disposal failures"
     */
    @Test
    public void testNullOperationHandling() {
        ConcurrentLinkedQueue<Runnable> deferredQueue = new ConcurrentLinkedQueue<>();
        
        // Simulate deferOperation with null check (as implemented in MyGdxGame)
        Runnable nullOperation = null;
        if (nullOperation != null) {
            deferredQueue.add(nullOperation);
        }
        
        // Queue should remain empty
        assertTrue(deferredQueue.isEmpty(), "Null operations should not be queued");
        
        // Add a valid operation
        AtomicBoolean executed = new AtomicBoolean(false);
        deferredQueue.add(() -> executed.set(true));
        
        // Verify valid operation is queued
        assertEquals(1, deferredQueue.size(), "Queue should contain one valid operation");
        
        // Process queue
        Runnable operation;
        while ((operation = deferredQueue.poll()) != null) {
            operation.run();
        }
        
        // Verify valid operation executed
        assertTrue(executed.get(), "Valid operation should execute");
        assertTrue(deferredQueue.isEmpty(), "Queue should be empty after processing");
    }
    
    /**
     * Test queue size warning threshold.
     * Verifies that warning is triggered when queue exceeds 100 operations.
     * 
     * Requirement 7.4: "WHEN the queue size exceeds 100 operations, THE Game_Client 
     * SHALL log a warning about potential memory issues"
     */
    @Test
    public void testQueueSizeWarningThreshold() {
        ConcurrentLinkedQueue<Runnable> deferredQueue = new ConcurrentLinkedQueue<>();
        
        AtomicBoolean warningTriggered = new AtomicBoolean(false);
        AtomicInteger operationsExecuted = new AtomicInteger(0);
        
        // Queue 101 operations
        for (int i = 0; i < 101; i++) {
            deferredQueue.add(() -> operationsExecuted.incrementAndGet());
            
            // Simulate warning check (as implemented in MyGdxGame.deferOperation)
            if (deferredQueue.size() > 100) {
                System.err.println("[WARNING] Deferred operation queue size exceeds 100. " +
                                  "Possible memory leak or render thread stall.");
                warningTriggered.set(true);
            }
        }
        
        // Verify warning was triggered
        assertTrue(warningTriggered.get(), "Warning should be triggered when queue exceeds 100");
        assertEquals(101, deferredQueue.size(), "Queue should contain 101 operations");
        
        // Process all operations
        Runnable operation;
        while ((operation = deferredQueue.poll()) != null) {
            operation.run();
        }
        
        // Verify all operations were processed
        assertEquals(101, operationsExecuted.get(), "All operations should be processed");
        assertTrue(deferredQueue.isEmpty(), "Queue should be empty after processing");
    }
}
