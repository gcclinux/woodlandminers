package wagemaker.uk.respawn;

import wagemaker.uk.gdx.MyGdxGame;
import wagemaker.uk.network.TreeType;
import wagemaker.uk.network.GameServer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core component that manages all respawn logic for destroyed resources.
 * Tracks pending respawns, updates timers, and triggers resource recreation.
 * 
 * <h3>Threading Considerations:</h3>
 * <p>This class uses ConcurrentHashMap for thread-safe access to pending respawns.
 * Resource creation operations are deferred to the render thread using the
 * deferOperation pattern to ensure OpenGL thread safety.</p>
 */
public class RespawnManager {
    private Map<String, RespawnEntry> pendingRespawns;
    private Map<String, RespawnIndicator> activeIndicators;
    private RespawnConfig config;
    private MyGdxGame game;
    private boolean isServer;
    private GameServer gameServer;
    
    /**
     * Creates a new RespawnManager instance.
     * 
     * @param game Reference to the game instance for resource creation (can be null for testing)
     * @param isServer True if running as server or single-player, false for client
     */
    public RespawnManager(MyGdxGame game, boolean isServer) {
        try {
            this.game = game;
            this.isServer = isServer;
            this.pendingRespawns = new ConcurrentHashMap<>();
            this.activeIndicators = new ConcurrentHashMap<>();
            
            if (game == null) {
                System.err.println("[RespawnManager] WARNING: Initialized with null game instance (testing mode)");
            }
            
            // Load configuration with error handling
            try {
                this.config = RespawnConfig.load();
                System.out.println("[RespawnManager] Initialized with config: " + config);
            } catch (Exception e) {
                System.err.println("[RespawnManager] ERROR: Failed to load configuration, using defaults: " + e.getMessage());
                e.printStackTrace();
                this.config = RespawnConfig.getDefault();
            }
            
            System.out.println("[RespawnManager] Initialized successfully (isServer: " + isServer + ")");
            
        } catch (Exception e) {
            System.err.println("[RespawnManager] CRITICAL ERROR: Failed to initialize RespawnManager: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize RespawnManager", e);
        }
    }
    
    /**
     * Registers a destroyed resource for respawn tracking.
     * 
     * @param resourceId Unique identifier for the resource
     * @param resourceType Type of resource (TREE or STONE)
     * @param x World X coordinate
     * @param y World Y coordinate
     * @param treeType Type of tree (null for stones)
     */
    public void registerDestruction(String resourceId, ResourceType resourceType, 
                                   float x, float y, TreeType treeType) {
        try {
            // Validate inputs
            if (resourceId == null || resourceId.trim().isEmpty()) {
                System.err.println("[RespawnManager] ERROR: Cannot register destruction with null or empty resource ID");
                return;
            }
            
            if (resourceType == null) {
                System.err.println("[RespawnManager] ERROR: Cannot register destruction with null resource type for: " + resourceId);
                return;
            }
            
            if (resourceType == ResourceType.TREE && treeType == null) {
                System.err.println("[RespawnManager] ERROR: Cannot register tree destruction with null tree type for: " + resourceId);
                return;
            }
            
            if (Float.isNaN(x) || Float.isInfinite(x) || Float.isNaN(y) || Float.isInfinite(y)) {
                System.err.println("[RespawnManager] ERROR: Cannot register destruction with invalid coordinates for: " + resourceId);
                return;
            }
            
            long currentTime = System.currentTimeMillis();
            long respawnDuration = config.getRespawnDuration(resourceType, treeType);
            
            if (respawnDuration <= 0) {
                System.err.println("[RespawnManager] ERROR: Invalid respawn duration (" + respawnDuration + 
                                 ") for " + resourceId + ", using default");
                respawnDuration = config.getDefaultRespawnDuration();
            }
            
            RespawnEntry entry = new RespawnEntry(
                resourceId,
                resourceType,
                x,
                y,
                currentTime,
                respawnDuration,
                treeType
            );
            
            pendingRespawns.put(resourceId, entry);
            
            System.out.println("[RespawnManager] Registered destruction: " + entry);
            
        } catch (Exception e) {
            System.err.println("[RespawnManager] ERROR: Failed to register destruction for " + resourceId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Updates all respawn timers and triggers respawns when timers expire.
     * Also manages visual indicators for resources about to respawn.
     * Should be called every frame from the render loop.
     * 
     * @param deltaTime Time elapsed since last frame
     */
    public void update(float deltaTime) {
        try {
            // Check for expired respawn timers and manage indicators
            List<String> toRespawn = new ArrayList<>();
            
            for (Map.Entry<String, RespawnEntry> entry : pendingRespawns.entrySet()) {
                try {
                    String resourceId = entry.getKey();
                    RespawnEntry respawnEntry = entry.getValue();
                    
                    if (respawnEntry == null) {
                        System.err.println("[RespawnManager] ERROR: Null respawn entry for resource: " + resourceId);
                        toRespawn.add(resourceId); // Remove corrupted entry
                        continue;
                    }
                    
                    if (respawnEntry.isReadyToRespawn()) {
                        toRespawn.add(resourceId);
                    } else if (config.isVisualIndicatorEnabled()) {
                        // Check if we should show an indicator
                        long remainingTime = respawnEntry.getRemainingTime();
                        if (remainingTime <= config.getVisualIndicatorThreshold() && 
                            !activeIndicators.containsKey(resourceId) && game != null) {
                            // Create indicator (deferred to render thread for texture creation)
                            game.deferOperation(() -> {
                                try {
                                    RespawnIndicator indicator = new RespawnIndicator(
                                        respawnEntry.getX(), 
                                        respawnEntry.getY()
                                    );
                                    activeIndicators.put(resourceId, indicator);
                                    System.out.println("[RespawnManager] Created indicator for: " + resourceId);
                                } catch (Exception e) {
                                    System.err.println("[RespawnManager] ERROR: Failed to create indicator for " + 
                                                     resourceId + ": " + e.getMessage());
                                    e.printStackTrace();
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[RespawnManager] ERROR: Exception processing respawn entry: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Update all active indicators
            for (RespawnIndicator indicator : activeIndicators.values()) {
                try {
                    if (indicator != null) {
                        indicator.update(deltaTime);
                    }
                } catch (Exception e) {
                    System.err.println("[RespawnManager] ERROR: Exception updating indicator: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Remove indicators that have completed fade-out
            List<String> indicatorsToRemove = new ArrayList<>();
            for (Map.Entry<String, RespawnIndicator> entry : activeIndicators.entrySet()) {
                try {
                    if (entry.getValue() != null && entry.getValue().isFadeOutComplete()) {
                        indicatorsToRemove.add(entry.getKey());
                    }
                } catch (Exception e) {
                    System.err.println("[RespawnManager] ERROR: Exception checking indicator fade-out: " + e.getMessage());
                    e.printStackTrace();
                    indicatorsToRemove.add(entry.getKey()); // Remove problematic indicator
                }
            }
            
            for (String resourceId : indicatorsToRemove) {
                try {
                    RespawnIndicator indicator = activeIndicators.remove(resourceId);
                    if (indicator != null && game != null) {
                        // Defer disposal to render thread
                        game.deferOperation(() -> {
                            try {
                                indicator.dispose();
                            } catch (Exception e) {
                                System.err.println("[RespawnManager] ERROR: Failed to dispose indicator: " + e.getMessage());
                                e.printStackTrace();
                            }
                        });
                    }
                } catch (Exception e) {
                    System.err.println("[RespawnManager] ERROR: Exception removing indicator: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Execute respawns for expired timers
            for (String resourceId : toRespawn) {
                try {
                    RespawnEntry entry = pendingRespawns.remove(resourceId);
                    if (entry != null) {
                        // Start fade-out animation for indicator if it exists
                        RespawnIndicator indicator = activeIndicators.get(resourceId);
                        if (indicator != null) {
                            try {
                                indicator.startFadeOut();
                            } catch (Exception e) {
                                System.err.println("[RespawnManager] ERROR: Failed to start indicator fade-out: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        
                        executeRespawn(entry);
                    }
                } catch (Exception e) {
                    System.err.println("[RespawnManager] ERROR: Exception executing respawn for " + 
                                     resourceId + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
        } catch (Exception e) {
            System.err.println("[RespawnManager] CRITICAL ERROR: Exception in update loop: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Executes the respawn of a resource by recreating it at its original location.
     * Uses deferred operations to ensure thread safety for OpenGL operations.
     * In multiplayer mode, broadcasts the respawn event to all clients.
     * 
     * @param entry The respawn entry containing resource information
     */
    private void executeRespawn(RespawnEntry entry) {
        try {
            if (entry == null) {
                System.err.println("[RespawnManager] ERROR: Cannot execute respawn with null entry");
                return;
            }
            
            System.out.println("[RespawnManager] Executing respawn: " + entry);
            
            if (entry.getResourceType() == ResourceType.TREE) {
                createTree(entry.getResourceId(), entry.getTreeType(), entry.getX(), entry.getY());
            } else if (entry.getResourceType() == ResourceType.STONE) {
                createStone(entry.getResourceId(), entry.getX(), entry.getY());
            } else {
                System.err.println("[RespawnManager] ERROR: Unknown resource type: " + entry.getResourceType());
            }
            
            // Broadcast respawn event in multiplayer mode
            if (isServer && gameServer != null) {
                try {
                    gameServer.broadcastResourceRespawn(
                        entry.getResourceId(),
                        entry.getResourceType(),
                        entry.getTreeType(),
                        entry.getX(),
                        entry.getY()
                    );
                } catch (Exception e) {
                    System.err.println("[RespawnManager] ERROR: Failed to broadcast respawn event: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
        } catch (Exception e) {
            System.err.println("[RespawnManager] ERROR: Failed to execute respawn: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates a tree at the specified location.
     * Uses deferOperation to ensure thread safety for texture creation.
     * 
     * @param treeId Unique identifier for the tree
     * @param treeType Type of tree to create
     * @param x World X coordinate
     * @param y World Y coordinate
     */
    private void createTree(String treeId, TreeType treeType, float x, float y) {
        if (treeType == null) {
            System.err.println("[RespawnManager] Cannot create tree with null type: " + treeId);
            return;
        }
        
        // Defer tree creation to render thread (texture creation requires OpenGL context)
        if (game != null) {
            game.deferOperation(() -> {
                try {
                    game.createTreeForRespawn(treeId, treeType, x, y);
                    System.out.println("[RespawnManager] Tree respawned: " + treeId + 
                                     " (type: " + treeType + ") at (" + x + ", " + y + ")");
                } catch (Exception e) {
                    System.err.println("[RespawnManager] Error creating tree: " + treeId);
                    e.printStackTrace();
                    
                    // Re-register for retry on next cycle
                    registerDestruction(treeId, ResourceType.TREE, x, y, treeType);
                }
            });
        } else {
            System.err.println("[RespawnManager] ERROR: Cannot create tree, game instance is null (testing mode)");
        }
    }
    
    /**
     * Creates a stone at the specified location.
     * Uses deferOperation to ensure thread safety for texture creation.
     * 
     * @param stoneId Unique identifier for the stone
     * @param x World X coordinate
     * @param y World Y coordinate
     */
    private void createStone(String stoneId, float x, float y) {
        // Defer stone creation to render thread (texture creation requires OpenGL context)
        if (game != null) {
            game.deferOperation(() -> {
                try {
                    game.createStoneForRespawn(stoneId, x, y);
                    System.out.println("[RespawnManager] Stone respawned: " + stoneId + 
                                     " at (" + x + ", " + y + ")");
                } catch (Exception e) {
                    System.err.println("[RespawnManager] Error creating stone: " + stoneId);
                    e.printStackTrace();
                    
                    // Re-register for retry on next cycle
                    registerDestruction(stoneId, ResourceType.STONE, x, y, null);
                }
            });
        } else {
            System.err.println("[RespawnManager] ERROR: Cannot create stone, game instance is null (testing mode)");
        }
    }
    
    /**
     * Gets the current respawn data for persistence.
     * Filters out any corrupted entries before saving.
     * 
     * @return List of all valid pending respawn entries
     */
    public List<RespawnEntry> getSaveData() {
        try {
            List<RespawnEntry> saveData = new ArrayList<>();
            int corruptedCount = 0;
            
            for (RespawnEntry entry : pendingRespawns.values()) {
                try {
                    if (isValidRespawnEntry(entry)) {
                        saveData.add(entry);
                    } else {
                        System.err.println("[RespawnManager] ERROR: Skipping corrupted entry in save data: " + 
                                         (entry != null ? entry.getResourceId() : "null"));
                        corruptedCount++;
                    }
                } catch (Exception e) {
                    System.err.println("[RespawnManager] ERROR: Exception validating entry for save: " + e.getMessage());
                    e.printStackTrace();
                    corruptedCount++;
                }
            }
            
            if (corruptedCount > 0) {
                System.err.println("[RespawnManager] WARNING: Excluded " + corruptedCount + 
                                 " corrupted entries from save data");
            }
            
            System.out.println("[RespawnManager] Prepared " + saveData.size() + " entries for save");
            return saveData;
            
        } catch (Exception e) {
            System.err.println("[RespawnManager] CRITICAL ERROR: Failed to get save data: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list on error
        }
    }
    
    /**
     * Loads respawn data from saved game.
     * Immediately respawns resources if their timer expired while game was closed.
     * Validates and filters out corrupted entries.
     * 
     * @param entries List of respawn entries to restore
     */
    public void loadFromSaveData(List<RespawnEntry> entries) {
        if (entries == null) {
            System.out.println("[RespawnManager] No respawn data to load (null entries)");
            return;
        }
        
        try {
            pendingRespawns.clear();
            
            int validCount = 0;
            int corruptedCount = 0;
            int expiredCount = 0;
            
            for (RespawnEntry entry : entries) {
                try {
                    // Validate entry
                    if (!isValidRespawnEntry(entry)) {
                        System.err.println("[RespawnManager] ERROR: Skipping corrupted respawn entry: " + 
                                         (entry != null ? entry.toString() : "null"));
                        corruptedCount++;
                        continue;
                    }
                    
                    if (entry.isReadyToRespawn()) {
                        // Timer expired while game was closed - respawn immediately
                        System.out.println("[RespawnManager] Timer expired during save, respawning immediately: " + 
                                         entry.getResourceId());
                        try {
                            executeRespawn(entry);
                            expiredCount++;
                        } catch (Exception e) {
                            System.err.println("[RespawnManager] ERROR: Failed to execute expired respawn for " + 
                                             entry.getResourceId() + ": " + e.getMessage());
                            e.printStackTrace();
                            // Re-register for retry
                            pendingRespawns.put(entry.getResourceId(), entry);
                        }
                    } else {
                        // Timer still active - restore to pending respawns
                        pendingRespawns.put(entry.getResourceId(), entry);
                        System.out.println("[RespawnManager] Restored pending respawn: " + entry);
                        validCount++;
                    }
                } catch (Exception e) {
                    System.err.println("[RespawnManager] ERROR: Exception processing respawn entry: " + e.getMessage());
                    e.printStackTrace();
                    corruptedCount++;
                }
            }
            
            System.out.println("[RespawnManager] Load summary: " + validCount + " valid, " + 
                             expiredCount + " expired (respawned), " + corruptedCount + " corrupted (skipped)");
            
        } catch (Exception e) {
            System.err.println("[RespawnManager] CRITICAL ERROR: Failed to load respawn data: " + e.getMessage());
            e.printStackTrace();
            pendingRespawns.clear();
        }
    }
    
    /**
     * Validates a respawn entry to ensure it contains valid data.
     * 
     * @param entry The entry to validate
     * @return true if valid, false if corrupted
     */
    private boolean isValidRespawnEntry(RespawnEntry entry) {
        if (entry == null) {
            System.err.println("[RespawnManager] ERROR: Null respawn entry");
            return false;
        }
        
        try {
            // Validate resource ID
            if (entry.getResourceId() == null || entry.getResourceId().trim().isEmpty()) {
                System.err.println("[RespawnManager] ERROR: Invalid resource ID in respawn entry");
                return false;
            }
            
            // Validate resource type
            if (entry.getResourceType() == null) {
                System.err.println("[RespawnManager] ERROR: Null resource type in respawn entry: " + entry.getResourceId());
                return false;
            }
            
            // Validate coordinates (basic sanity check)
            if (Float.isNaN(entry.getX()) || Float.isInfinite(entry.getX()) ||
                Float.isNaN(entry.getY()) || Float.isInfinite(entry.getY())) {
                System.err.println("[RespawnManager] ERROR: Invalid coordinates in respawn entry: " + entry.getResourceId());
                return false;
            }
            
            // Validate timestamps and durations
            if (entry.getDestructionTimestamp() <= 0) {
                System.err.println("[RespawnManager] ERROR: Invalid destruction timestamp in respawn entry: " + entry.getResourceId());
                return false;
            }
            
            if (entry.getRespawnDuration() <= 0) {
                System.err.println("[RespawnManager] ERROR: Invalid respawn duration in respawn entry: " + entry.getResourceId());
                return false;
            }
            
            // Validate tree type for tree resources
            if (entry.getResourceType() == ResourceType.TREE && entry.getTreeType() == null) {
                System.err.println("[RespawnManager] ERROR: Tree resource missing tree type: " + entry.getResourceId());
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("[RespawnManager] ERROR: Exception validating respawn entry: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Gets the number of pending respawns.
     * 
     * @return Count of resources waiting to respawn
     */
    public int getPendingRespawnCount() {
        return pendingRespawns.size();
    }
    
    /**
     * Gets the respawn configuration.
     * 
     * @return Current respawn configuration
     */
    public RespawnConfig getConfig() {
        return config;
    }
    
    /**
     * Checks if this manager is running as server/single-player.
     * 
     * @return True if server or single-player, false if client
     */
    public boolean isServer() {
        return isServer;
    }
    
    /**
     * Sets the game server for multiplayer respawn broadcasting.
     * 
     * @param gameServer The game server instance
     */
    public void setGameServer(GameServer gameServer) {
        this.gameServer = gameServer;
        System.out.println("[RespawnManager] GameServer set for respawn broadcasting");
    }
    
    /**
     * Gets a pending respawn entry by resource ID.
     * 
     * @param resourceId Resource identifier
     * @return RespawnEntry if found, null otherwise
     */
    public RespawnEntry getPendingRespawn(String resourceId) {
        return pendingRespawns.get(resourceId);
    }
    
    /**
     * Removes a pending respawn entry.
     * Used when a resource is manually recreated or removed.
     * 
     * @param resourceId Resource identifier
     * @return The removed entry, or null if not found
     */
    public RespawnEntry removePendingRespawn(String resourceId) {
        return pendingRespawns.remove(resourceId);
    }
    
    /**
     * Handles a resource respawn message from the server (client-side).
     * Creates the respawned resource in the game world.
     * 
     * @param resourceId Unique identifier for the resource
     * @param resourceType Type of resource (TREE or STONE)
     * @param treeType Type of tree (null for stones)
     * @param x World X coordinate
     * @param y World Y coordinate
     */
    public void handleResourceRespawn(String resourceId, wagemaker.uk.respawn.ResourceType resourceType,
                                     TreeType treeType, float x, float y) {
        try {
            // Validate inputs
            if (resourceId == null || resourceId.trim().isEmpty()) {
                System.err.println("[RespawnManager] ERROR: Received respawn message with null/empty resource ID");
                return;
            }
            
            if (resourceType == null) {
                System.err.println("[RespawnManager] ERROR: Received respawn message with null resource type for: " + resourceId);
                return;
            }
            
            if (Float.isNaN(x) || Float.isInfinite(x) || Float.isNaN(y) || Float.isInfinite(y)) {
                System.err.println("[RespawnManager] ERROR: Received respawn message with invalid coordinates for: " + resourceId);
                return;
            }
            
            System.out.println("[RespawnManager] Client received respawn: " + resourceId + 
                             " (type: " + resourceType + ") at (" + x + ", " + y + ")");
            
            // Remove from pending respawns if it exists
            pendingRespawns.remove(resourceId);
            
            // Create the resource
            if (resourceType == ResourceType.TREE) {
                if (treeType == null) {
                    System.err.println("[RespawnManager] ERROR: Received tree respawn with null tree type for: " + resourceId);
                    return;
                }
                createTree(resourceId, treeType, x, y);
            } else if (resourceType == ResourceType.STONE) {
                createStone(resourceId, x, y);
            } else {
                System.err.println("[RespawnManager] ERROR: Unknown resource type in respawn message: " + resourceType);
            }
            
        } catch (Exception e) {
            System.err.println("[RespawnManager] ERROR: Failed to handle resource respawn for " + 
                             resourceId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handles a respawn state synchronization message from the server (client-side).
     * Replaces the client's pending respawn state with the authoritative server state.
     * Validates and filters out corrupted entries.
     * 
     * @param respawnEntries List of pending respawn entries from the server
     */
    public void handleRespawnStateSync(List<RespawnEntry> respawnEntries) {
        try {
            if (respawnEntries == null) {
                System.out.println("[RespawnManager] Client received empty respawn state sync");
                return;
            }
            
            System.out.println("[RespawnManager] Client syncing respawn state: " + 
                             respawnEntries.size() + " pending respawns");
            
            // Clear existing pending respawns and load server state
            pendingRespawns.clear();
            
            int validCount = 0;
            int corruptedCount = 0;
            
            for (RespawnEntry entry : respawnEntries) {
                try {
                    // Validate entry
                    if (!isValidRespawnEntry(entry)) {
                        System.err.println("[RespawnManager] ERROR: Skipping corrupted entry in state sync: " + 
                                         (entry != null ? entry.toString() : "null"));
                        corruptedCount++;
                        continue;
                    }
                    
                    pendingRespawns.put(entry.getResourceId(), entry);
                    System.out.println("[RespawnManager] Synced pending respawn: " + entry);
                    validCount++;
                    
                } catch (Exception e) {
                    System.err.println("[RespawnManager] ERROR: Exception processing sync entry: " + e.getMessage());
                    e.printStackTrace();
                    corruptedCount++;
                }
            }
            
            System.out.println("[RespawnManager] State sync complete: " + validCount + " valid, " + 
                             corruptedCount + " corrupted (skipped)");
            
        } catch (Exception e) {
            System.err.println("[RespawnManager] CRITICAL ERROR: Failed to sync respawn state: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Renders all active respawn indicators within render distance.
     * Only renders indicators that are visible on screen.
     * 
     * @param batch SpriteBatch for rendering
     * @param deltaTime Time elapsed since last frame
     * @param cameraX Camera center X coordinate
     * @param cameraY Camera center Y coordinate
     * @param viewWidth Viewport width
     * @param viewHeight Viewport height
     */
    public void renderIndicators(SpriteBatch batch, float deltaTime, 
                                 float cameraX, float cameraY, 
                                 float viewWidth, float viewHeight) {
        if (!config.isVisualIndicatorEnabled()) {
            return;
        }
        
        // Calculate render bounds with some buffer
        float minX = cameraX - viewWidth / 2 - 100;
        float maxX = cameraX + viewWidth / 2 + 100;
        float minY = cameraY - viewHeight / 2 - 100;
        float maxY = cameraY + viewHeight / 2 + 100;
        
        // Render only indicators within render distance
        for (RespawnIndicator indicator : activeIndicators.values()) {
            float x = indicator.getX();
            float y = indicator.getY();
            
            // Check if indicator is within render distance
            if (x >= minX && x <= maxX && y >= minY && y <= maxY) {
                indicator.render(batch, deltaTime);
            }
        }
    }
    
    /**
     * Disposes of all active indicators.
     * Should be called when the game is shutting down or switching modes.
     * Must be called on the render thread.
     */
    public void disposeIndicators() {
        try {
            int disposedCount = 0;
            int failedCount = 0;
            
            for (RespawnIndicator indicator : activeIndicators.values()) {
                try {
                    if (indicator != null) {
                        indicator.dispose();
                        disposedCount++;
                    }
                } catch (Exception e) {
                    System.err.println("[RespawnManager] ERROR: Failed to dispose indicator: " + e.getMessage());
                    e.printStackTrace();
                    failedCount++;
                }
            }
            
            activeIndicators.clear();
            
            if (disposedCount > 0 || failedCount > 0) {
                System.out.println("[RespawnManager] Disposed " + disposedCount + " indicators" + 
                                 (failedCount > 0 ? " (" + failedCount + " failed)" : ""));
            }
            
        } catch (Exception e) {
            System.err.println("[RespawnManager] CRITICAL ERROR: Failed to dispose indicators: " + e.getMessage());
            e.printStackTrace();
            // Clear the map anyway to prevent memory leaks
            activeIndicators.clear();
        }
    }
}
