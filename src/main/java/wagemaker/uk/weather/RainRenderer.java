package wagemaker.uk.weather;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Handles rendering of rain particles using LibGDX ShapeRenderer.
 * Uses object pooling for performance optimization.
 * Manages particle lifecycle: spawning, updating, rendering, and recycling.
 * 
 * Configuration is centralized in RainConfig for easy tuning.
 */
public class RainRenderer {
    
    private List<RainParticle> particlePool;
    private ShapeRenderer shapeRenderer;
    private Random random;
    private float currentIntensity; // 0.0-1.0, affects active particle count
    
    /**
     * Creates a new RainRenderer with the specified ShapeRenderer.
     * 
     * @param shapeRenderer The ShapeRenderer to use for drawing rain particles
     */
    public RainRenderer(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
        this.random = new Random();
        this.currentIntensity = 0.0f;
        this.particlePool = new ArrayList<>();
    }
    
    /**
     * Initializes the rain renderer by pre-allocating the particle pool.
     * This method should be called once during setup to avoid garbage collection
     * during gameplay.
     */
    public void initialize() {
        // Pre-allocate MAX_PARTICLES for object pooling
        particlePool.clear();
        for (int i = 0; i < RainConfig.MAX_PARTICLES; i++) {
            particlePool.add(new RainParticle());
        }
    }
    
    /**
     * Updates the rain renderer state, including particle positions and spawning.
     * 
     * @param deltaTime Time elapsed since last update in seconds
     * @param camera The camera used to determine screen bounds
     * @param intensity Rain intensity from 0.0 to 1.0
     */
    public void update(float deltaTime, OrthographicCamera camera, float intensity) {
        this.currentIntensity = intensity;
        
        // Update existing particles
        updateParticles(deltaTime, camera);
        
        // Spawn new particles if needed
        int targetCount = getTargetParticleCount(intensity);
        int activeCount = getActiveParticleCount();
        
        // Spawn particles to reach target count
        while (activeCount < targetCount) {
            if (!spawnParticle(camera)) {
                break; // Pool exhausted
            }
            activeCount++;
        }
    }
    
    /**
     * Renders all active rain particles to the screen.
     * 
     * @param camera The camera used for projection
     */
    public void render(OrthographicCamera camera) {
        if (currentIntensity <= RainConfig.MIN_RENDER_INTENSITY) {
            return; // No rain to render
        }
        
        // Set up ShapeRenderer with camera projection
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Render each active particle
        for (RainParticle particle : particlePool) {
            if (particle.isActive()) {
                // Set color with alpha based on particle properties
                float alpha = particle.getAlpha() * currentIntensity;
                shapeRenderer.setColor(
                    RainConfig.RAIN_COLOR_RED,
                    RainConfig.RAIN_COLOR_GREEN,
                    RainConfig.RAIN_COLOR_BLUE,
                    alpha
                );
                
                // Draw particle as a vertical rectangle (raindrop)
                float x = particle.getX();
                float y = particle.getY();
                float length = particle.getLength();
                
                shapeRenderer.rect(x, y, RainConfig.PARTICLE_WIDTH, length);
            }
        }
        
        shapeRenderer.end();
    }
    
    /**
     * Cleans up resources used by the renderer.
     * Note: ShapeRenderer is managed externally and should not be disposed here.
     */
    public void dispose() {
        // Particle pool will be garbage collected
        // ShapeRenderer is managed by the main game class
        particlePool.clear();
    }
    
    /**
     * Calculates the target number of particles based on rain intensity.
     * Maps intensity (0.0-1.0) to particle count (MIN_PARTICLES to MAX_PARTICLES).
     * 
     * @param intensity Rain intensity from 0.0 to 1.0
     * @return Target particle count
     */
    private int getTargetParticleCount(float intensity) {
        if (intensity <= 0.0f) {
            return 0;
        }
        
        // Linear interpolation between MIN and MAX based on intensity
        int range = RainConfig.MAX_PARTICLES - RainConfig.MIN_PARTICLES;
        return RainConfig.MIN_PARTICLES + (int)(range * intensity);
    }
    
    /**
     * Spawns a new rain particle at the top of the screen with random properties.
     * 
     * @param camera The camera used to determine screen bounds
     * @return true if a particle was spawned, false if pool is exhausted
     */
    private boolean spawnParticle(OrthographicCamera camera) {
        // Find an inactive particle from the pool
        RainParticle particle = getInactiveParticle();
        if (particle == null) {
            return false; // Pool exhausted
        }
        
        // Calculate screen bounds
        float screenWidth = camera.viewportWidth;
        float screenHeight = camera.viewportHeight;
        float cameraLeft = camera.position.x - screenWidth / 2;
        float cameraTop = camera.position.y + screenHeight / 2;
        
        // Random X position across screen width
        float startX = cameraLeft + random.nextFloat() * screenWidth;
        
        // Start slightly above the top of the screen
        float startY = cameraTop + RainConfig.SPAWN_OFFSET_Y;
        
        // Random velocity within range
        float velocity = RainConfig.MIN_VELOCITY + random.nextFloat() * (RainConfig.MAX_VELOCITY - RainConfig.MIN_VELOCITY);
        
        // Random length within range
        float length = RainConfig.MIN_PARTICLE_LENGTH + random.nextFloat() * (RainConfig.MAX_PARTICLE_LENGTH - RainConfig.MIN_PARTICLE_LENGTH);
        
        // Reset particle with new properties
        particle.reset(startX, startY, velocity, length);
        
        // Set random alpha within range
        float alpha = RainConfig.MIN_ALPHA + random.nextFloat() * (RainConfig.MAX_ALPHA - RainConfig.MIN_ALPHA);
        particle.setAlpha(alpha);
        
        return true;
    }
    
    /**
     * Updates all active particles, animating their falling motion and recycling
     * particles that have fallen off screen.
     * 
     * @param deltaTime Time elapsed since last update in seconds
     * @param camera The camera used to determine screen bounds
     */
    private void updateParticles(float deltaTime, OrthographicCamera camera) {
        float screenHeight = camera.viewportHeight;
        float cameraBottom = camera.position.y - screenHeight / 2;
        
        for (RainParticle particle : particlePool) {
            if (particle.isActive()) {
                // Update particle position
                particle.update(deltaTime);
                
                // Check if particle has fallen off screen
                if (particle.isOffScreen(cameraBottom)) {
                    // Recycle particle by marking it inactive
                    particle.setActive(false);
                }
            }
        }
    }
    
    /**
     * Finds an inactive particle from the pool for reuse.
     * 
     * @return An inactive particle, or null if all particles are active
     */
    private RainParticle getInactiveParticle() {
        for (RainParticle particle : particlePool) {
            if (!particle.isActive()) {
                return particle;
            }
        }
        return null; // Pool exhausted
    }
    
    /**
     * Counts the number of currently active particles.
     * 
     * @return The number of active particles
     */
    private int getActiveParticleCount() {
        int count = 0;
        for (RainParticle particle : particlePool) {
            if (particle.isActive()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Gets the current rain intensity.
     * 
     * @return Current intensity from 0.0 to 1.0
     */
    public float getCurrentIntensity() {
        return currentIntensity;
    }
    
    /**
     * Gets the number of particles in the pool.
     * 
     * @return Total particle pool size
     */
    public int getPoolSize() {
        return particlePool.size();
    }
    
    @Override
    public String toString() {
        return String.format("RainRenderer[intensity=%.2f, active=%d/%d]",
            currentIntensity, getActiveParticleCount(), particlePool.size());
    }
}
