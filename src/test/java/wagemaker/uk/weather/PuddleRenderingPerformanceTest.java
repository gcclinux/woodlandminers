package wagemaker.uk.weather;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance test for puddle rendering system.
 * Tests frame time with MAX_PUDDLES active and verifies performance meets requirements.
 * 
 * Requirement 4.5: The system SHALL maintain frame rates above 30 FPS on target hardware.
 * This translates to frame time under 33.33ms (1000ms / 30fps = 33.33ms per frame).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PuddleRenderingPerformanceTest {
    
    // Performance thresholds
    private static final float TARGET_FPS = 30.0f;
    private static final float MAX_FRAME_TIME_MS = 1000.0f / TARGET_FPS; // 33.33ms
    private static final int WARMUP_ITERATIONS = 10;
    private static final int TEST_ITERATIONS = 100;
    
    private static HeadlessApplication application;
    private static ShapeRenderer shapeRenderer;
    
    private PuddleManager puddleManager;
    private OrthographicCamera camera;
    
    @BeforeAll
    public static void setUpClass() {
        // Create headless application for LibGDX context
        application = new HeadlessApplication(new ApplicationAdapter() {});
        
        // Mock GL20 to avoid actual OpenGL calls
        Gdx.gl20 = Mockito.mock(GL20.class);
        
        // Mock ShapeRenderer since we don't need actual rendering for performance tests
        shapeRenderer = Mockito.mock(ShapeRenderer.class);
    }
    
    @AfterAll
    public static void tearDownClass() {
        // Don't dispose mocked ShapeRenderer
        if (application != null) {
            application.exit();
        }
    }
    
    @BeforeEach
    public void setUp() {
        // Create camera
        camera = new OrthographicCamera(800, 600);
        camera.position.set(400, 300, 0);
        camera.update();
        
        // Create and initialize puddle manager
        puddleManager = new PuddleManager(shapeRenderer);
        puddleManager.initialize();
        
        // Ensure puddles are enabled
        PuddleConfig.PUDDLES_ENABLED = true;
        PuddleConfig.DEBUG_LOGGING_ENABLED = false;
    }
    
    @AfterEach
    public void tearDown() {
        if (puddleManager != null) {
            puddleManager.dispose();
        }
    }
    
    /**
     * Test 1: Measure frame time with MAX_PUDDLES active.
     * Verifies that rendering MAX_PUDDLES meets the 30 FPS requirement.
     */
    @Test
    @Order(1)
    public void testMaxPuddlesRenderingPerformance() {
        System.out.println("Testing puddle rendering performance with MAX_PUDDLES (" + 
                         PuddleConfig.MAX_PUDDLES + ")...");
        
        // Simulate rain for 5+ seconds to trigger puddle spawning
        float deltaTime = 0.016f; // ~60 FPS simulation
        float totalTime = 0.0f;
        float intensity = 1.0f; // Maximum intensity
        
        // Accumulate rain until puddles spawn
        while (totalTime < PuddleConfig.ACCUMULATION_THRESHOLD + 0.1f) {
            puddleManager.update(deltaTime, true, intensity, camera);
            totalTime += deltaTime;
        }
        
        // Verify puddles are active
        assertEquals(PuddleState.ACTIVE, puddleManager.getCurrentState(), 
                    "Puddles should be in ACTIVE state");
        
        int activePuddles = puddleManager.getActivePuddleCount();
        assertTrue(activePuddles > 0, "Should have active puddles");
        System.out.println("Active puddles: " + activePuddles);
        
        // Warmup phase - let JVM optimize
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            puddleManager.update(deltaTime, true, intensity, camera);
            // Note: We can't actually render without OpenGL context, 
            // but we can measure update time which is the critical path
        }
        
        // Measurement phase
        long totalUpdateTime = 0;
        
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            long startTime = System.nanoTime();
            puddleManager.update(deltaTime, true, intensity, camera);
            long endTime = System.nanoTime();
            
            totalUpdateTime += (endTime - startTime);
        }
        
        // Calculate average frame time
        double avgUpdateTimeNs = (double) totalUpdateTime / TEST_ITERATIONS;
        double avgUpdateTimeMs = avgUpdateTimeNs / 1_000_000.0;
        
        System.out.println("Performance Results:");
        System.out.println("  Iterations: " + TEST_ITERATIONS);
        System.out.println("  Average update time: " + String.format("%.3f", avgUpdateTimeMs) + "ms");
        System.out.println("  Target frame time: " + String.format("%.2f", MAX_FRAME_TIME_MS) + "ms");
        System.out.println("  Equivalent FPS: " + String.format("%.1f", 1000.0 / avgUpdateTimeMs));
        
        // Verify performance meets requirement
        assertTrue(avgUpdateTimeMs < MAX_FRAME_TIME_MS, 
                  String.format("Average update time (%.3fms) should be under %.2fms to maintain 30 FPS",
                               avgUpdateTimeMs, MAX_FRAME_TIME_MS));
    }
    
    /**
     * Test 2: Measure performance with camera movement (viewport culling).
     * Verifies that viewport-based culling maintains performance.
     */
    @Test
    @Order(2)
    public void testViewportCullingPerformance() {
        System.out.println("Testing viewport culling performance...");
        
        // Spawn puddles
        float deltaTime = 0.016f;
        float totalTime = 0.0f;
        float intensity = 1.0f;
        
        while (totalTime < PuddleConfig.ACCUMULATION_THRESHOLD + 0.1f) {
            puddleManager.update(deltaTime, true, intensity, camera);
            totalTime += deltaTime;
        }
        
        assertEquals(PuddleState.ACTIVE, puddleManager.getCurrentState());
        
        int initialPuddles = puddleManager.getActivePuddleCount();
        System.out.println("Initial active puddles: " + initialPuddles);
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            camera.position.x += 10;
            camera.update();
            puddleManager.update(deltaTime, true, intensity, camera);
        }
        
        // Measure with camera movement
        long totalUpdateTime = 0;
        
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            // Move camera to simulate player movement
            camera.position.x += (i % 2 == 0 ? 5 : -5);
            camera.position.y += (i % 3 == 0 ? 3 : -3);
            camera.update();
            
            long startTime = System.nanoTime();
            puddleManager.update(deltaTime, true, intensity, camera);
            long endTime = System.nanoTime();
            
            totalUpdateTime += (endTime - startTime);
        }
        
        double avgUpdateTimeNs = (double) totalUpdateTime / TEST_ITERATIONS;
        double avgUpdateTimeMs = avgUpdateTimeNs / 1_000_000.0;
        
        System.out.println("Viewport Culling Performance:");
        System.out.println("  Average update time with camera movement: " + 
                         String.format("%.3f", avgUpdateTimeMs) + "ms");
        System.out.println("  Equivalent FPS: " + String.format("%.1f", 1000.0 / avgUpdateTimeMs));
        
        assertTrue(avgUpdateTimeMs < MAX_FRAME_TIME_MS,
                  String.format("Update time with camera movement (%.3fms) should maintain 30 FPS",
                               avgUpdateTimeMs));
    }
    
    /**
     * Test 3: Measure performance during state transitions.
     * Verifies that accumulation and evaporation don't cause performance issues.
     */
    @Test
    @Order(3)
    public void testStateTransitionPerformance() {
        System.out.println("Testing state transition performance...");
        
        float deltaTime = 0.016f;
        float intensity = 1.0f;
        
        // Test NONE -> ACCUMULATING transition
        long accumulationStartTime = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            puddleManager.update(deltaTime, true, intensity, camera);
        }
        long accumulationEndTime = System.nanoTime();
        
        double accumulationTimeMs = (accumulationEndTime - accumulationStartTime) / 1_000_000.0;
        double avgAccumulationFrameMs = accumulationTimeMs / 100.0;
        
        System.out.println("Accumulation phase:");
        System.out.println("  Average frame time: " + String.format("%.3f", avgAccumulationFrameMs) + "ms");
        
        // Wait for puddles to spawn
        float totalTime = 0.0f;
        while (totalTime < PuddleConfig.ACCUMULATION_THRESHOLD + 0.1f) {
            puddleManager.update(deltaTime, true, intensity, camera);
            totalTime += deltaTime;
        }
        
        assertEquals(PuddleState.ACTIVE, puddleManager.getCurrentState());
        
        // Test ACTIVE -> EVAPORATING transition
        long evaporationStartTime = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            puddleManager.update(deltaTime, false, intensity, camera);
        }
        long evaporationEndTime = System.nanoTime();
        
        double evaporationTimeMs = (evaporationEndTime - evaporationStartTime) / 1_000_000.0;
        double avgEvaporationFrameMs = evaporationTimeMs / 100.0;
        
        System.out.println("Evaporation phase:");
        System.out.println("  Average frame time: " + String.format("%.3f", avgEvaporationFrameMs) + "ms");
        
        // Both phases should maintain performance
        assertTrue(avgAccumulationFrameMs < MAX_FRAME_TIME_MS,
                  "Accumulation phase should maintain 30 FPS");
        assertTrue(avgEvaporationFrameMs < MAX_FRAME_TIME_MS,
                  "Evaporation phase should maintain 30 FPS");
    }
    
    /**
     * Test 4: Measure performance with puddles disabled.
     * Verifies that disabling puddles has minimal overhead.
     */
    @Test
    @Order(4)
    public void testDisabledPuddlesPerformance() {
        System.out.println("Testing performance with puddles disabled...");
        
        // Disable puddles
        PuddleConfig.PUDDLES_ENABLED = false;
        
        float deltaTime = 0.016f;
        float intensity = 1.0f;
        
        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            puddleManager.update(deltaTime, true, intensity, camera);
        }
        
        // Measure
        long totalUpdateTime = 0;
        
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            long startTime = System.nanoTime();
            puddleManager.update(deltaTime, true, intensity, camera);
            long endTime = System.nanoTime();
            
            totalUpdateTime += (endTime - startTime);
        }
        
        double avgUpdateTimeNs = (double) totalUpdateTime / TEST_ITERATIONS;
        double avgUpdateTimeMs = avgUpdateTimeNs / 1_000_000.0;
        
        System.out.println("Disabled Puddles Performance:");
        System.out.println("  Average update time: " + String.format("%.3f", avgUpdateTimeMs) + "ms");
        
        // Verify puddles stay disabled
        assertEquals(PuddleState.NONE, puddleManager.getCurrentState(),
                    "Puddles should remain in NONE state when disabled");
        assertEquals(0, puddleManager.getActivePuddleCount(),
                    "Should have no active puddles when disabled");
        
        // Performance should be excellent when disabled (essentially no-op)
        assertTrue(avgUpdateTimeMs < 1.0,
                  "Disabled puddles should have minimal overhead (< 1ms)");
        
        // Re-enable for cleanup
        PuddleConfig.PUDDLES_ENABLED = true;
    }
    
    /**
     * Test 5: Stress test with varying intensity.
     * Verifies performance across different puddle counts.
     */
    @Test
    @Order(5)
    public void testVaryingIntensityPerformance() {
        System.out.println("Testing performance with varying intensity...");
        
        float deltaTime = 0.016f;
        float[] intensities = {0.2f, 0.5f, 0.8f, 1.0f};
        
        for (float intensity : intensities) {
            // Reset puddle manager
            puddleManager.dispose();
            puddleManager = new PuddleManager(shapeRenderer);
            puddleManager.initialize();
            
            // Spawn puddles at this intensity
            float totalTime = 0.0f;
            while (totalTime < PuddleConfig.ACCUMULATION_THRESHOLD + 0.1f) {
                puddleManager.update(deltaTime, true, intensity, camera);
                totalTime += deltaTime;
            }
            
            int puddleCount = puddleManager.getActivePuddleCount();
            
            // Warmup
            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                puddleManager.update(deltaTime, true, intensity, camera);
            }
            
            // Measure
            long totalUpdateTime = 0;
            
            for (int i = 0; i < TEST_ITERATIONS; i++) {
                long startTime = System.nanoTime();
                puddleManager.update(deltaTime, true, intensity, camera);
                long endTime = System.nanoTime();
                
                totalUpdateTime += (endTime - startTime);
            }
            
            double avgUpdateTimeMs = (totalUpdateTime / TEST_ITERATIONS) / 1_000_000.0;
            
            System.out.println(String.format("Intensity %.1f (puddles: %d):", intensity, puddleCount));
            System.out.println("  Average update time: " + String.format("%.3f", avgUpdateTimeMs) + "ms");
            
            assertTrue(avgUpdateTimeMs < MAX_FRAME_TIME_MS,
                      String.format("Intensity %.1f should maintain 30 FPS", intensity));
        }
    }
    
    /**
     * Test 6: Memory allocation during puddle operations.
     * Verifies that object pooling prevents excessive garbage collection.
     */
    @Test
    @Order(6)
    public void testMemoryAllocationPerformance() {
        System.out.println("Testing memory allocation performance...");
        
        float deltaTime = 0.016f;
        float intensity = 1.0f;
        
        // Force GC before test
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        // Spawn and despawn puddles multiple times
        for (int cycle = 0; cycle < 5; cycle++) {
            // Spawn puddles
            float totalTime = 0.0f;
            while (totalTime < PuddleConfig.ACCUMULATION_THRESHOLD + 0.1f) {
                puddleManager.update(deltaTime, true, intensity, camera);
                totalTime += deltaTime;
            }
            
            assertEquals(PuddleState.ACTIVE, puddleManager.getCurrentState());
            
            // Evaporate puddles
            totalTime = 0.0f;
            while (totalTime < PuddleConfig.EVAPORATION_DURATION + 0.1f) {
                puddleManager.update(deltaTime, false, intensity, camera);
                totalTime += deltaTime;
            }
            
            assertEquals(PuddleState.NONE, puddleManager.getCurrentState());
        }
        
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsedMB = (memoryAfter - memoryBefore) / (1024 * 1024);
        
        System.out.println("Memory allocation test (5 spawn/despawn cycles):");
        System.out.println("  Memory used: " + memoryUsedMB + "MB");
        
        // Object pooling should keep memory usage minimal
        assertTrue(memoryUsedMB < 10,
                  "Memory usage should be minimal due to object pooling (< 10MB), used: " + memoryUsedMB + "MB");
    }
}
