package wagemaker.uk.weather;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.List;

/**
 * Main weather system facade that coordinates rain zones and rendering.
 * Integrates with MyGdxGame's render loop to provide localized rain effects.
 * 
 * This class serves as the primary interface for the rain system, coordinating
 * between the RainZoneManager (which handles zone definitions and player position queries)
 * and the RainRenderer (which handles particle rendering).
 */
public class RainSystem {
    private RainZoneManager zoneManager;
    private RainRenderer renderer;
    private PuddleManager puddleManager;
    private boolean enabled;
    
    /**
     * Creates a new RainSystem with the specified ShapeRenderer.
     * 
     * @param shapeRenderer The ShapeRenderer to use for drawing rain particles
     */
    public RainSystem(ShapeRenderer shapeRenderer) {
        this.zoneManager = new RainZoneManager();
        this.renderer = new RainRenderer(shapeRenderer);
        this.puddleManager = new PuddleManager(shapeRenderer);
        this.enabled = true;
    }
    
    /**
     * Initializes the rain system by setting up the zone manager and renderer.
     * This method should be called once during game setup.
     * Pre-allocates particle pool to avoid garbage collection during gameplay.
     */
    public void initialize() {
        renderer.initialize();
        puddleManager.initialize();
    }
    
    /**
     * Updates the rain system state based on player position.
     * Checks if the player is in any rain zone, calculates the combined intensity,
     * and updates the rain renderer accordingly.
     * 
     * @param deltaTime Time elapsed since last update in seconds
     * @param playerX Player's X coordinate (center position)
     * @param playerY Player's Y coordinate (center position)
     * @param camera The camera used for rendering
     */
    public void update(float deltaTime, float playerX, float playerY, OrthographicCamera camera) {
        if (!enabled) {
            // When disabled, update with 0 intensity to fade out existing particles
            renderer.update(deltaTime, camera, 0.0f);
            puddleManager.update(deltaTime, false, 0.0f, camera);
            return;
        }
        
        // Calculate rain intensity at player's position
        float intensity = zoneManager.getRainIntensityAt(playerX, playerY);
        boolean isRaining = intensity > 0.0f;
        
        // Update renderer with calculated intensity
        renderer.update(deltaTime, camera, intensity);
        
        // Update puddle manager with rain state
        puddleManager.update(deltaTime, isRaining, intensity, camera);
    }
    
    /**
     * Renders the rain particles and puddles to the screen.
     * This should be called after batch.end() but before UI rendering
     * to ensure proper layering (rain above world, below UI).
     * 
     * Rendering order (bottom to top):
     * 1. Ground/terrain (rendered in batch)
     * 2. Water puddles (rendered here first)
     * 3. Rain particles (rendered here second)
     * 4. Player and objects (rendered in batch)
     * 5. UI elements (rendered after this method)
     * 
     * This ensures puddles appear above ground but below the player,
     * creating a realistic layering effect.
     * 
     * @param camera The camera used for projection
     */
    public void render(OrthographicCamera camera) {
        if (!enabled) {
            return;
        }
        
        // Render puddles first (above ground, below rain particles and player)
        puddleManager.render(camera);
        
        // Render rain particles on top of puddles
        renderer.render(camera);
    }
    
    /**
     * Cleans up resources used by the rain system.
     * Should be called when the game is shutting down.
     */
    public void dispose() {
        renderer.dispose();
        puddleManager.dispose();
    }
    
    /**
     * Enables or disables the rain system at runtime.
     * When disabled, rain will fade out gracefully.
     * 
     * @param enabled true to enable rain, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Checks if the rain system is currently enabled.
     * 
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Gets the rain zone manager for direct zone manipulation.
     * Useful for adding/removing zones or querying zone information.
     * 
     * @return The RainZoneManager instance
     */
    public RainZoneManager getZoneManager() {
        return zoneManager;
    }
    
    /**
     * Synchronizes rain zones from multiplayer server.
     * This method is called when the client receives world state from the server,
     * ensuring all clients have the same rain zone definitions.
     * 
     * @param zones The list of rain zones from the server
     */
    public void syncRainZones(List<RainZone> zones) {
        zoneManager.setRainZones(zones);
    }
    
    /**
     * Sets tree positions for puddle avoidance.
     * Puddles will not spawn within MIN_TREE_DISTANCE (400px) of any tree.
     * This should be called before update() each frame to ensure puddles avoid trees.
     * 
     * @param trees List of tree positions to avoid
     */
    public void setTreePositions(List<PuddleRenderer.TreePosition> trees) {
        puddleManager.setTreePositions(trees);
    }
    
    /**
     * Gets the current rain intensity at the player's last updated position.
     * 
     * @return Current rain intensity from the renderer (0.0 to 1.0)
     */
    public float getCurrentIntensity() {
        return renderer.getCurrentIntensity();
    }
    
    @Override
    public String toString() {
        return String.format("RainSystem[enabled=%s, zones=%d, intensity=%.2f]",
            enabled, zoneManager.getZoneCount(), renderer.getCurrentIntensity());
    }
}
