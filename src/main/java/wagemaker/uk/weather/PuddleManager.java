package wagemaker.uk.weather;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Manages the lifecycle of water puddles based on rain state.
 * Implements a state machine that transitions between:
 * NONE → ACCUMULATING → ACTIVE → EVAPORATING → NONE
 * 
 * Puddles appear after 5 seconds of continuous rain and fade out
 * over 5 seconds after rain stops.
 * 
 * <h3>Tree Avoidance:</h3>
 * To prevent puddles from appearing under tree foliage, call
 * {@link #setTreePositions(java.util.List)} before puddles spawn.
 * 
 * <pre>{@code
 * // Example usage in game loop:
 * List<PuddleRenderer.TreePosition> trees = new ArrayList<>();
 * for (SmallTree tree : gameTrees.values()) {
 *     trees.add(new PuddleRenderer.TreePosition(tree.getX(), tree.getY()));
 * }
 * puddleManager.setTreePositions(trees);
 * puddleManager.update(deltaTime, isRaining, intensity, camera);
 * }</pre>
 */
public class PuddleManager {
    
    private PuddleRenderer puddleRenderer;
    private PuddleState currentState;
    private float accumulationTimer;
    private float evaporationTimer;
    
    // Performance monitoring
    private float logTimer;
    private static final float LOG_INTERVAL = 5.0f; // Log every 5 seconds
    
    /**
     * Creates a new PuddleManager with the specified ShapeRenderer.
     * 
     * @param shapeRenderer The ShapeRenderer to use for rendering puddles
     */
    public PuddleManager(ShapeRenderer shapeRenderer) {
        this.puddleRenderer = new PuddleRenderer(shapeRenderer);
        this.currentState = PuddleState.NONE;
        this.accumulationTimer = 0.0f;
        this.evaporationTimer = 0.0f;
        this.logTimer = 0.0f;
    }
    
    /**
     * Initializes the puddle manager by setting up the renderer.
     * Should be called once during setup.
     */
    public void initialize() {
        puddleRenderer.initialize();
    }
    
    /**
     * Sets the tree positions for puddle avoidance.
     * Puddles will not spawn within MIN_TREE_DISTANCE of any tree.
     * Should be called before update() to ensure trees are avoided.
     * 
     * @param trees List of tree positions to avoid
     */
    public void setTreePositions(java.util.List<PuddleRenderer.TreePosition> trees) {
        puddleRenderer.setTreePositions(trees);
    }
    
    /**
     * Updates the puddle system based on rain state.
     * Manages state transitions and timing for accumulation and evaporation.
     * 
     * @param deltaTime Time elapsed since last update in seconds
     * @param isRaining Whether rain is currently active
     * @param intensity Rain intensity from 0.0 to 1.0
     * @param camera The camera for viewport calculations
     */
    public void update(float deltaTime, boolean isRaining, float intensity, OrthographicCamera camera) {
        // Check if puddles are enabled
        if (!PuddleConfig.PUDDLES_ENABLED) {
            // If puddles are disabled, ensure we're in NONE state and clear any existing puddles
            if (currentState != PuddleState.NONE) {
                currentState = PuddleState.NONE;
                puddleRenderer.clearAllPuddles();
                accumulationTimer = 0.0f;
                evaporationTimer = 0.0f;
            }
            return;
        }
        
        // Validate deltaTime to prevent negative timer values
        if (deltaTime < 0.0f) {
            deltaTime = 0.0f;
        }
        
        // Performance monitoring and logging
        if (PuddleConfig.DEBUG_LOGGING_ENABLED) {
            logTimer += deltaTime;
            if (logTimer >= LOG_INTERVAL) {
                logPuddleStatus();
                logTimer = 0.0f;
            }
        }
        PuddleState previousState = currentState;
        
        switch (currentState) {
            case NONE:
                if (isRaining) {
                    // Rain started, begin accumulation
                    currentState = PuddleState.ACCUMULATING;
                    accumulationTimer = deltaTime; // Start accumulating immediately
                    logStateTransition(previousState, currentState);
                }
                break;
                
            case ACCUMULATING:
                if (!isRaining) {
                    // Rain stopped before threshold, return to NONE
                    currentState = PuddleState.NONE;
                    accumulationTimer = 0.0f;
                    logStateTransition(previousState, currentState);
                } else {
                    // Continue accumulating
                    accumulationTimer += deltaTime;
                    
                    if (accumulationTimer >= PuddleConfig.ACCUMULATION_THRESHOLD) {
                        // Threshold reached, spawn puddles and transition to ACTIVE
                        currentState = PuddleState.ACTIVE;
                        spawnPuddles(camera, intensity);
                        accumulationTimer = 0.0f; // Reset timer after transition
                        logStateTransition(previousState, currentState);
                    }
                }
                break;
                
            case ACTIVE:
                if (!isRaining) {
                    // Rain stopped, begin evaporation
                    currentState = PuddleState.EVAPORATING;
                    evaporationTimer = 0.0f;
                    logStateTransition(previousState, currentState);
                } else {
                    // Update puddle alpha based on intensity while rain is active
                    float alphaMultiplier = calculateAlphaMultiplier(intensity);
                    // Clamp alpha multiplier to valid range [0.0, 1.0]
                    alphaMultiplier = Math.max(0.0f, Math.min(1.0f, alphaMultiplier));
                    puddleRenderer.updatePuddles(deltaTime, alphaMultiplier);
                }
                // Puddles remain stable during rain
                break;
                
            case EVAPORATING:
                if (isRaining) {
                    // Rain restarted during evaporation, restore puddles
                    currentState = PuddleState.ACTIVE;
                    evaporationTimer = 0.0f;
                    logStateTransition(previousState, currentState);
                    // Restore full alpha (clamped to valid range)
                    float alphaMultiplier = Math.max(0.0f, Math.min(1.0f, 1.0f));
                    puddleRenderer.updatePuddles(deltaTime, alphaMultiplier);
                } else {
                    // Continue evaporation
                    evaporationTimer += deltaTime;
                    
                    if (evaporationTimer >= PuddleConfig.EVAPORATION_DURATION) {
                        // Evaporation complete, clear puddles and return to NONE
                        currentState = PuddleState.NONE;
                        puddleRenderer.clearAllPuddles();
                        evaporationTimer = 0.0f;
                        logStateTransition(previousState, currentState);
                    } else {
                        // Update puddle alpha based on evaporation progress
                        float alphaMultiplier = 1.0f - (evaporationTimer / PuddleConfig.EVAPORATION_DURATION);
                        // Clamp alpha multiplier to valid range [0.0, 1.0]
                        alphaMultiplier = Math.max(0.0f, Math.min(1.0f, alphaMultiplier));
                        puddleRenderer.updatePuddles(deltaTime, alphaMultiplier);
                    }
                }
                break;
        }
    }
    
    /**
     * Renders all active puddles.
     * 
     * @param camera The camera for projection
     */
    public void render(OrthographicCamera camera) {
        if (currentState == PuddleState.ACTIVE || currentState == PuddleState.EVAPORATING) {
            puddleRenderer.render(camera);
        }
    }
    
    /**
     * Cleans up resources used by the puddle manager.
     */
    public void dispose() {
        puddleRenderer.dispose();
    }
    
    /**
     * Gets the current state of the puddle system.
     * 
     * @return The current PuddleState
     */
    public PuddleState getCurrentState() {
        return currentState;
    }
    
    /**
     * Gets the accumulation progress as a value from 0.0 to 1.0.
     * 
     * @return Accumulation progress (0.0 = just started, 1.0 = threshold reached)
     */
    public float getAccumulationProgress() {
        if (currentState != PuddleState.ACCUMULATING) {
            return 0.0f;
        }
        // Ensure timer is non-negative and clamp result to [0.0, 1.0]
        float progress = Math.max(0.0f, accumulationTimer) / PuddleConfig.ACCUMULATION_THRESHOLD;
        return Math.max(0.0f, Math.min(1.0f, progress));
    }
    
    /**
     * Gets the evaporation progress as a value from 0.0 to 1.0.
     * 
     * @return Evaporation progress (0.0 = just started, 1.0 = complete)
     */
    public float getEvaporationProgress() {
        if (currentState != PuddleState.EVAPORATING) {
            return 0.0f;
        }
        // Ensure timer is non-negative and clamp result to [0.0, 1.0]
        float progress = Math.max(0.0f, evaporationTimer) / PuddleConfig.EVAPORATION_DURATION;
        return Math.max(0.0f, Math.min(1.0f, progress));
    }
    
    /**
     * Gets the number of currently active puddles.
     * 
     * @return The number of active puddles
     */
    public int getActivePuddleCount() {
        return puddleRenderer.getActivePuddleCount();
    }
    
    /**
     * Spawns puddles within the camera viewport.
     * Puddle count is based on rain intensity.
     * 
     * @param camera The camera for viewport calculations
     * @param intensity Rain intensity from 0.0 to 1.0
     */
    private void spawnPuddles(OrthographicCamera camera, float intensity) {
        int puddleCount = calculatePuddleCount(intensity);
        puddleRenderer.spawnPuddles(camera, puddleCount);
    }
    
    /**
     * Calculates the number of puddles to spawn based on rain intensity.
     * Linearly interpolates between MIN_PUDDLES and MAX_PUDDLES.
     * 
     * @param intensity Rain intensity from 0.0 to 1.0
     * @return Number of puddles to spawn
     */
    private int calculatePuddleCount(float intensity) {
        // Clamp intensity to valid range
        float clampedIntensity = Math.max(0.0f, Math.min(1.0f, intensity));
        
        // Linear interpolation between MIN and MAX
        int range = PuddleConfig.MAX_PUDDLES - PuddleConfig.MIN_PUDDLES;
        int count = PuddleConfig.MIN_PUDDLES + (int)(range * clampedIntensity);
        
        return count;
    }
    
    /**
     * Calculates the alpha multiplier based on rain intensity.
     * Higher intensity results in more visible puddles.
     * 
     * @param intensity Rain intensity from 0.0 to 1.0
     * @return Alpha multiplier from 0.5 to 1.0
     */
    private float calculateAlphaMultiplier(float intensity) {
        // Clamp intensity to valid range
        float clampedIntensity = Math.max(0.0f, Math.min(1.0f, intensity));
        
        // Map intensity to alpha multiplier range [0.5, 1.0]
        // Even at low intensity, puddles should be somewhat visible
        return 0.5f + (0.5f * clampedIntensity);
    }
    
    /**
     * Logs the current puddle system status for performance monitoring.
     * Includes state, active puddle count, and pool utilization.
     */
    private void logPuddleStatus() {
        int activePuddles = getActivePuddleCount();
        int poolSize = puddleRenderer.getPoolSize();
        float utilization = poolSize > 0 ? (activePuddles * 100.0f / poolSize) : 0.0f;
        
        Gdx.app.log("PuddleManager", String.format(
            "State: %s | Active: %d/%d (%.1f%%) | Accumulation: %.2f | Evaporation: %.2f",
            currentState,
            activePuddles,
            poolSize,
            utilization,
            getAccumulationProgress(),
            getEvaporationProgress()
        ));
    }
    
    /**
     * Logs state transitions for debugging purposes.
     * Only logs when debug logging is enabled.
     * 
     * @param from Previous state
     * @param to New state
     */
    private void logStateTransition(PuddleState from, PuddleState to) {
        if (PuddleConfig.DEBUG_LOGGING_ENABLED) {
            int activePuddles = getActivePuddleCount();
            Gdx.app.log("PuddleManager", String.format(
                "State transition: %s -> %s | Active puddles: %d",
                from,
                to,
                activePuddles
            ));
        }
    }
}
