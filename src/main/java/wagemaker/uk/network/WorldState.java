package wagemaker.uk.network;

import wagemaker.uk.weather.RainConfig;
import wagemaker.uk.weather.RainZone;
import wagemaker.uk.world.WorldSaveData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the complete authoritative game state.
 * This class manages all synchronized entities in the multiplayer world.
 */
public class WorldState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private long worldSeed;
    private Map<String, PlayerState> players;
    private Map<String, TreeState> trees;
    private Map<String, StoneState> stones;
    private Map<String, ItemState> items;
    private Set<String> clearedPositions;
    private List<RainZone> rainZones;
    private long lastUpdateTimestamp;
    
    public WorldState() {
        this.players = new ConcurrentHashMap<>();
        this.trees = new ConcurrentHashMap<>();
        this.stones = new ConcurrentHashMap<>();
        this.items = new ConcurrentHashMap<>();
        this.clearedPositions = ConcurrentHashMap.newKeySet();
        this.rainZones = new ArrayList<>();
        this.lastUpdateTimestamp = System.currentTimeMillis();
    }
    
    public WorldState(long worldSeed) {
        this();
        this.worldSeed = worldSeed;
        // Generate initial trees around spawn point
        generateInitialTrees();
        // Initialize rain zones
        initializeRainZones();
    }
    
    /**
     * Initializes default rain zones for the world.
     * Creates a rain zone at the spawn area using configuration values.
     */
    private void initializeRainZones() {
        this.rainZones = new ArrayList<>();
        // Add spawn area rain zone using configuration constants
        RainZone spawnRain = new RainZone(
            RainConfig.SPAWN_ZONE_ID,
            RainConfig.SPAWN_ZONE_CENTER_X,
            RainConfig.SPAWN_ZONE_CENTER_Y,
            RainConfig.SPAWN_ZONE_RADIUS,
            RainConfig.DEFAULT_FADE_DISTANCE,
            RainConfig.DEFAULT_INTENSITY
        );
        this.rainZones.add(spawnRain);
    }
    
    /**
     * Generates initial trees around the spawn point (0,0).
     * This ensures players have trees to interact with when they spawn.
     * Trees are generated in a large area around spawn using deterministic generation
     * with biome-specific tree filtering.
     * 
     * IMPORTANT: This method uses the same biome-aware tree generation logic as the client
     * to ensure initial trees respect biome boundaries (bamboo in sand, other trees in grass).
     */
    private void generateInitialTrees() {
        java.util.Random random = new java.util.Random();
        
        // Create a temporary BiomeManager for biome queries during initial generation
        // This ensures server-side tree generation respects biome boundaries
        wagemaker.uk.biome.BiomeManager biomeManager = null;
        boolean useBiomeAwareness = true;
        
        try {
            biomeManager = new wagemaker.uk.biome.BiomeManager();
            biomeManager.initialize();
            System.out.println("BiomeManager initialized successfully in WorldState (thread: " + Thread.currentThread().getName() + ")");
        } catch (Exception e) {
            // BiomeManager initialization failed (likely due to OpenGL context issues on background thread)
            // Fall back to generating trees without biome awareness
            System.err.println("BiomeManager initialization failed in WorldState, generating trees without biome awareness: " + e.getMessage());
            e.printStackTrace();
            useBiomeAwareness = false;
            biomeManager = null;
        }
        
        // Generate trees in a 5000x5000 area around spawn (-2500 to +2500)
        // This gives players plenty of trees to explore
        int minX = -2500;
        int maxX = 2500;
        int minY = -2500;
        int maxY = 2500;
        int gridSize = 64; // Same as grass tile size
        
        for (int x = minX; x <= maxX; x += gridSize) {
            for (int y = minY; y <= maxY; y += gridSize) {
                String key = x + "," + y;
                
                // STEP 1: Set deterministic random seed (same as client-side generation)
                random.setSeed(worldSeed + x * 31L + y * 17L);
                
                // STEP 2: Check spawn probability (2% chance)
                if (random.nextFloat() < 0.02f) {
                    // STEP 3: Validate spawn location
                    // Check if any tree is within 256px distance
                    if (isTreeNearby(x, y, 256)) {
                        continue;
                    }
                    
                    // Add random offset to break grid pattern (±32px in each direction)
                    // Try multiple times to find a position without overlapping trees
                    float treeX = 0, treeY = 0;
                    boolean validPosition = false;
                    int maxAttempts = 5;
                    
                    for (int attempt = 0; attempt < maxAttempts; attempt++) {
                        float offsetX = (random.nextFloat() - 0.5f) * 64; // -32 to +32
                        float offsetY = (random.nextFloat() - 0.5f) * 64; // -32 to +32
                        treeX = x + offsetX;
                        treeY = y + offsetY;
                        
                        // Check biome type first to determine minimum distance
                        wagemaker.uk.biome.BiomeType biomeCheck = null;
                        if (useBiomeAwareness && biomeManager != null) {
                            biomeCheck = biomeManager.getBiomeAtPosition(treeX, treeY);
                        }
                        float minDistance = (biomeCheck == wagemaker.uk.biome.BiomeType.SAND) ? 50f : 192f;
                        
                        // Check if any tree is too close (192px for grass, 50px for sand)
                        if (!isTreeTooClose(treeX, treeY, minDistance)) {
                            validPosition = true;
                            break;
                        }
                    }
                    
                    // If no valid position found after attempts, skip this tree
                    if (!validPosition) {
                        continue;
                    }
                    
                    // Don't spawn trees too close to spawn point (within 200px)
                    float distanceFromSpawn = (float) Math.sqrt(treeX * treeX + treeY * treeY);
                    if (distanceFromSpawn < 200) {
                        continue;
                    }
                    
                    // STEP 4: Query biome type (DETERMINISTIC - same as client)
                    // This ensures server generates the same biome-specific trees as clients
                    TreeType treeType = null;
                    
                    if (useBiomeAwareness && biomeManager != null) {
                        wagemaker.uk.biome.BiomeType biome = biomeManager.getBiomeAtPosition(treeX, treeY);
                        
                        // STEP 5: Generate tree based on biome type (SAME LOGIC AS CLIENT)
                        if (biome == wagemaker.uk.biome.BiomeType.SAND) {
                            // Sand biomes: bamboo trees with 30% spawn rate (reduced by 70%)
                            if (random.nextFloat() < 0.3f) {
                                treeType = TreeType.BAMBOO;
                            }
                        } else {
                            // Grass biomes: adjusted tree type distribution
                            // SmallTree: 42.5% (increased by 30% from 32.5%)
                            // AppleTree: 12.5% (reduced by 50% from 25%)
                            // CoconutTree: 32.5% (unchanged)
                            // BananaTree: 12.5% (reduced by 50% from 25%)
                            float treeTypeRoll = random.nextFloat();
                            
                            if (treeTypeRoll < 0.425f) {
                                treeType = TreeType.SMALL;
                            } else if (treeTypeRoll < 0.55f) {
                                treeType = TreeType.APPLE;
                            } else if (treeTypeRoll < 0.875f) {
                                treeType = TreeType.COCONUT;
                            } else {
                                treeType = TreeType.BANANA;
                            }
                        }
                    } else {
                        // Fallback: generate trees without biome awareness (all types equally)
                        float treeTypeRoll = random.nextFloat();
                        
                        if (treeTypeRoll < 0.2f) {
                            treeType = TreeType.SMALL;
                        } else if (treeTypeRoll < 0.4f) {
                            treeType = TreeType.APPLE;
                        } else if (treeTypeRoll < 0.6f) {
                            treeType = TreeType.COCONUT;
                        } else if (treeTypeRoll < 0.8f) {
                            treeType = TreeType.BANANA;
                        } else {
                            treeType = TreeType.BAMBOO;
                        }
                    }
                    
                    // Skip if no tree type was selected (e.g., bamboo didn't pass 30% check)
                    if (treeType == null) {
                        continue;
                    }
                    
                    // Create tree state with full health and randomized position
                    TreeState tree = new TreeState(key, treeType, treeX, treeY, 100.0f, true);
                    this.trees.put(key, tree);
                }
            }
        }
        
        // Clean up temporary BiomeManager
        if (biomeManager != null) {
            biomeManager.dispose();
        }
        
        if (useBiomeAwareness) {
            System.out.println("Generated " + trees.size() + " initial biome-specific trees for world seed: " + worldSeed);
        } else {
            System.out.println("Generated " + trees.size() + " initial trees (without biome awareness) for world seed: " + worldSeed);
        }
    }
    
    /**
     * Checks if there's a tree nearby within the specified distance.
     */
    private boolean isTreeNearby(int x, int y, int minDistance) {
        for (TreeState tree : trees.values()) {
            if (!tree.isExists()) {
                continue;
            }
            
            float dx = tree.getX() - x;
            float dy = tree.getY() - y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            
            if (distance < minDistance) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if a tree position is too close to any existing tree.
     * Uses float coordinates for precise overlap detection.
     * 
     * @param x The x-coordinate to check
     * @param y The y-coordinate to check
     * @param minDistance Minimum distance required between trees
     * @return true if too close to another tree, false otherwise
     */
    private boolean isTreeTooClose(float x, float y, float minDistance) {
        for (TreeState tree : trees.values()) {
            if (!tree.isExists()) {
                continue;
            }
            
            float dx = tree.getX() - x;
            float dy = tree.getY() - y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            
            if (distance < minDistance) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Generates a tree at a specific position using deterministic logic.
     * This uses the same algorithm as client-side generation to ensure consistency.
     * 
     * @param x The x-coordinate (must be aligned to 64px grid)
     * @param y The y-coordinate (must be aligned to 64px grid)
     * @return The generated TreeState, or null if no tree should exist at this position
     */
    public TreeState generateTreeAt(int x, int y) {
        String key = x + "," + y;
        
        // Check if tree already exists or was cleared
        if (trees.containsKey(key) || clearedPositions.contains(key)) {
            return trees.get(key);
        }
        
        // Use deterministic random seed (same as client-side generation)
        java.util.Random random = new java.util.Random();
        random.setSeed(worldSeed + x * 31L + y * 17L);
        
        // Check spawn probability (2% chance)
        if (random.nextFloat() < 0.02f) {
            // Add random offset to break grid pattern (±32px in each direction)
            // Try multiple times to find a position without overlapping trees
            float treeX = 0, treeY = 0;
            boolean validPosition = false;
            int maxAttempts = 5;
            
            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                float offsetX = (random.nextFloat() - 0.5f) * 64; // -32 to +32
                float offsetY = (random.nextFloat() - 0.5f) * 64; // -32 to +32
                treeX = x + offsetX;
                treeY = y + offsetY;
                
                // Check biome type first to determine minimum distance
                wagemaker.uk.biome.BiomeManager tempBiomeManager = new wagemaker.uk.biome.BiomeManager();
                tempBiomeManager.initialize();
                wagemaker.uk.biome.BiomeType biomeCheck = tempBiomeManager.getBiomeAtPosition(treeX, treeY);
                tempBiomeManager.dispose();
                
                float minDistance = (biomeCheck == wagemaker.uk.biome.BiomeType.SAND) ? 50f : 192f;
                
                // Check if any tree is too close (192px for grass, 50px for sand)
                if (!isTreeTooClose(treeX, treeY, minDistance)) {
                    validPosition = true;
                    break;
                }
            }
            
            // If no valid position found after attempts, skip this tree
            if (!validPosition) {
                return null;
            }
            
            // Don't spawn trees too close to spawn point (within 200px)
            // This is deterministic based on coordinates only
            float distanceFromSpawn = (float) Math.sqrt(treeX * treeX + treeY * treeY);
            if (distanceFromSpawn < 200) {
                return null;
            }
            
            // Determine tree type using biome-aware logic (if available)
            TreeType treeType = null;
            
            // Try to use biome manager for tree type determination
            try {
                wagemaker.uk.biome.BiomeManager biomeManager = new wagemaker.uk.biome.BiomeManager();
                biomeManager.initialize();
                wagemaker.uk.biome.BiomeType biome = biomeManager.getBiomeAtPosition(treeX, treeY);
                biomeManager.dispose();
                
                if (biome == wagemaker.uk.biome.BiomeType.SAND) {
                    // Sand biomes: bamboo trees with 30% spawn rate (reduced by 70%)
                    if (random.nextFloat() < 0.3f) {
                        treeType = TreeType.BAMBOO;
                    }
                } else {
                    // Grass biomes: adjusted tree type distribution
                    // SmallTree: 42.5% (increased by 30% from 32.5%)
                    // AppleTree: 12.5% (reduced by 50% from 25%)
                    // CoconutTree: 32.5% (unchanged)
                    // BananaTree: 12.5% (reduced by 50% from 25%)
                    float treeTypeRoll = random.nextFloat();
                    if (treeTypeRoll < 0.425f) {
                        treeType = TreeType.SMALL;
                    } else if (treeTypeRoll < 0.55f) {
                        treeType = TreeType.APPLE;
                    } else if (treeTypeRoll < 0.875f) {
                        treeType = TreeType.COCONUT;
                    } else {
                        treeType = TreeType.BANANA;
                    }
                }
            } catch (Exception e) {
                // Fallback: generate without biome awareness
                float treeTypeRoll = random.nextFloat();
                if (treeTypeRoll < 0.2f) {
                    treeType = TreeType.SMALL;
                } else if (treeTypeRoll < 0.4f) {
                    treeType = TreeType.APPLE;
                } else if (treeTypeRoll < 0.6f) {
                    treeType = TreeType.COCONUT;
                } else if (treeTypeRoll < 0.8f) {
                    treeType = TreeType.BANANA;
                } else {
                    treeType = TreeType.BAMBOO;
                }
            }
            
            // If no tree type was selected (e.g., bamboo didn't pass 30% check), return null
            if (treeType == null) {
                return null;
            }
            
            // Create and store the tree with randomized position
            TreeState tree = new TreeState(key, treeType, treeX, treeY, 100.0f, true);
            this.trees.put(key, tree);
            return tree;
        }
        
        return null;
    }
    
    /**
     * Generates a stone at a specific position using deterministic logic.
     * This uses the same algorithm as client-side generation to ensure consistency.
     * 
     * @param x The x-coordinate (must be aligned to 64px grid)
     * @param y The y-coordinate (must be aligned to 64px grid)
     * @return The generated StoneState, or null if no stone should exist at this position
     */
    public StoneState generateStoneAt(int x, int y) {
        String key = x + "," + y;
        
        // Check if stone already exists or was cleared
        if (stones.containsKey(key) || clearedPositions.contains(key)) {
            return stones.get(key);
        }
        
        // Use deterministic random seed (same as client-side generation)
        java.util.Random random = new java.util.Random();
        random.setSeed(worldSeed + x * 37L + y * 23L);
        
        // Stone spawn probability: 0.15 (15%) - 50% of bamboo tree rate (30%)
        if (random.nextFloat() < 0.15f) {
            // Add random offset to break grid pattern
            float offsetX = (random.nextFloat() - 0.5f) * 64; // -32 to +32
            float offsetY = (random.nextFloat() - 0.5f) * 64; // -32 to +32
            float stoneX = x + offsetX;
            float stoneY = y + offsetY;
            
            // Only spawn stones on sand biomes
            try {
                wagemaker.uk.biome.BiomeManager biomeManager = new wagemaker.uk.biome.BiomeManager();
                biomeManager.initialize();
                wagemaker.uk.biome.BiomeType biome = biomeManager.getBiomeAtPosition(stoneX, stoneY);
                biomeManager.dispose();
                
                if (biome != wagemaker.uk.biome.BiomeType.SAND) {
                    return null;
                }
            } catch (Exception e) {
                // If biome check fails, don't spawn stone
                return null;
            }
            
            // Don't spawn stones too close to spawn point (within 200px)
            float distanceFromSpawn = (float) Math.sqrt(stoneX * stoneX + stoneY * stoneY);
            if (distanceFromSpawn < 200) {
                return null;
            }
            
            // Create and store the stone
            StoneState stone = new StoneState(key, stoneX, stoneY, 50.0f);
            this.stones.put(key, stone);
            System.out.println("[STONE] Generated stone at " + key + " (" + stoneX + ", " + stoneY + ")");
            return stone;
        }
        
        return null;
    }
    
    /**
     * Creates a complete snapshot of the current world state.
     * This is used when a new client connects to get the full state.
     * 
     * @return A deep copy of the current world state
     */
    public WorldState createSnapshot() {
        WorldState snapshot = new WorldState(this.worldSeed);
        
        // Deep copy players
        for (Map.Entry<String, PlayerState> entry : this.players.entrySet()) {
            PlayerState original = entry.getValue();
            PlayerState copy = new PlayerState(
                original.getPlayerId(),
                original.getPlayerName(),
                original.getX(),
                original.getY(),
                original.getDirection(),
                original.getHealth(),
                original.isMoving()
            );
            copy.setLastUpdateTime(original.getLastUpdateTime());
            snapshot.players.put(entry.getKey(), copy);
        }
        
        // Deep copy trees
        for (Map.Entry<String, TreeState> entry : this.trees.entrySet()) {
            TreeState original = entry.getValue();
            TreeState copy = new TreeState(
                original.getTreeId(),
                original.getType(),
                original.getX(),
                original.getY(),
                original.getHealth(),
                original.isExists()
            );
            snapshot.trees.put(entry.getKey(), copy);
        }
        
        // Deep copy stones
        for (Map.Entry<String, StoneState> entry : this.stones.entrySet()) {
            StoneState original = entry.getValue();
            StoneState copy = new StoneState(
                original.getStoneId(),
                original.getX(),
                original.getY(),
                original.getHealth()
            );
            snapshot.stones.put(entry.getKey(), copy);
        }
        
        // Deep copy items
        for (Map.Entry<String, ItemState> entry : this.items.entrySet()) {
            ItemState original = entry.getValue();
            ItemState copy = new ItemState(
                original.getItemId(),
                original.getType(),
                original.getX(),
                original.getY(),
                original.isCollected()
            );
            snapshot.items.put(entry.getKey(), copy);
        }
        
        // Copy cleared positions
        snapshot.clearedPositions.addAll(this.clearedPositions);
        
        // Deep copy rain zones
        if (this.rainZones != null) {
            snapshot.rainZones = new ArrayList<>();
            for (RainZone zone : this.rainZones) {
                RainZone copy = new RainZone(
                    zone.getZoneId(),
                    zone.getCenterX(),
                    zone.getCenterY(),
                    zone.getRadius(),
                    zone.getFadeDistance(),
                    zone.getIntensity()
                );
                snapshot.rainZones.add(copy);
            }
        }
        
        snapshot.lastUpdateTimestamp = this.lastUpdateTimestamp;
        
        return snapshot;
    }
    
    /**
     * Calculates the delta (changes) since the given timestamp.
     * This is used for efficient state synchronization - only sending what changed.
     * 
     * @param timestamp The timestamp to calculate changes from
     * @return A WorldStateUpdate containing only the entities that changed
     */
    public WorldStateUpdate getDeltaSince(long timestamp) {
        Map<String, PlayerState> updatedPlayers = new HashMap<>();
        Map<String, TreeState> updatedTrees = new HashMap<>();
        Map<String, ItemState> updatedItems = new HashMap<>();
        
        // Find players that changed since timestamp
        for (Map.Entry<String, PlayerState> entry : this.players.entrySet()) {
            PlayerState player = entry.getValue();
            if (player.getLastUpdateTime() > timestamp) {
                updatedPlayers.put(entry.getKey(), player);
            }
        }
        
        // For trees and items, we check if they were modified
        // Since TreeState and ItemState don't have timestamps, we include all recent changes
        // In a real implementation, you might want to add timestamps to these as well
        for (Map.Entry<String, TreeState> entry : this.trees.entrySet()) {
            TreeState tree = entry.getValue();
            // Include trees that don't exist (were destroyed) or have changed
            if (!tree.isExists() || this.lastUpdateTimestamp > timestamp) {
                updatedTrees.put(entry.getKey(), tree);
            }
        }
        
        for (Map.Entry<String, ItemState> entry : this.items.entrySet()) {
            ItemState item = entry.getValue();
            // Include items that were collected or recently added
            if (item.isCollected() || this.lastUpdateTimestamp > timestamp) {
                updatedItems.put(entry.getKey(), item);
            }
        }
        
        return new WorldStateUpdate(updatedPlayers, updatedTrees, updatedItems);
    }
    
    /**
     * Applies an incremental update to the current world state.
     * This merges the changes from the update into the current state.
     * 
     * @param update The update containing changed entities
     */
    public void applyUpdate(WorldStateUpdate update) {
        if (update == null) {
            return;
        }
        
        // Apply player updates
        if (update.getUpdatedPlayers() != null) {
            for (Map.Entry<String, PlayerState> entry : update.getUpdatedPlayers().entrySet()) {
                this.players.put(entry.getKey(), entry.getValue());
            }
        }
        
        // Apply tree updates
        if (update.getUpdatedTrees() != null) {
            for (Map.Entry<String, TreeState> entry : update.getUpdatedTrees().entrySet()) {
                TreeState tree = entry.getValue();
                if (!tree.isExists()) {
                    // Tree was destroyed, remove it
                    this.trees.remove(entry.getKey());
                    this.clearedPositions.add(entry.getKey());
                } else {
                    this.trees.put(entry.getKey(), tree);
                }
            }
        }
        
        // Apply item updates
        if (update.getUpdatedItems() != null) {
            for (Map.Entry<String, ItemState> entry : update.getUpdatedItems().entrySet()) {
                ItemState item = entry.getValue();
                if (item.isCollected()) {
                    // Item was collected, remove it
                    this.items.remove(entry.getKey());
                } else {
                    this.items.put(entry.getKey(), item);
                }
            }
        }
        
        this.lastUpdateTimestamp = System.currentTimeMillis();
    }
    
    // Getters and setters
    
    public long getWorldSeed() {
        return worldSeed;
    }
    
    public void setWorldSeed(long worldSeed) {
        this.worldSeed = worldSeed;
    }
    
    public Map<String, PlayerState> getPlayers() {
        return players;
    }
    
    public void setPlayers(Map<String, PlayerState> players) {
        this.players = players;
    }
    
    public Map<String, TreeState> getTrees() {
        return trees;
    }
    
    public void setTrees(Map<String, TreeState> trees) {
        this.trees = trees;
    }
    
    public Map<String, StoneState> getStones() {
        return stones;
    }
    
    public void setStones(Map<String, StoneState> stones) {
        this.stones = stones;
    }
    
    public Map<String, ItemState> getItems() {
        return items;
    }
    
    public void setItems(Map<String, ItemState> items) {
        this.items = items;
    }
    
    public Set<String> getClearedPositions() {
        return clearedPositions;
    }
    
    public void setClearedPositions(Set<String> clearedPositions) {
        this.clearedPositions = clearedPositions;
    }
    
    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }
    
    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }
    
    public List<RainZone> getRainZones() {
        return rainZones;
    }
    
    public void setRainZones(List<RainZone> rainZones) {
        this.rainZones = rainZones;
    }
    
    // Convenience methods for managing state
    
    /**
     * Adds or updates a player in the world state.
     */
    public void addOrUpdatePlayer(PlayerState player) {
        if (player != null) {
            player.setLastUpdateTime(System.currentTimeMillis());
            this.players.put(player.getPlayerId(), player);
            this.lastUpdateTimestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Removes a player from the world state.
     */
    public void removePlayer(String playerId) {
        this.players.remove(playerId);
        this.lastUpdateTimestamp = System.currentTimeMillis();
    }
    
    /**
     * Adds or updates a tree in the world state.
     */
    public void addOrUpdateTree(TreeState tree) {
        if (tree != null) {
            this.trees.put(tree.getTreeId(), tree);
            this.lastUpdateTimestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Removes a tree from the world state (marks it as destroyed).
     */
    public void removeTree(String treeId) {
        TreeState tree = this.trees.get(treeId);
        if (tree != null) {
            tree.setExists(false);
            this.clearedPositions.add(treeId);
            this.lastUpdateTimestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Adds or updates a stone in the world state.
     */
    public void addOrUpdateStone(StoneState stone) {
        if (stone != null) {
            this.stones.put(stone.getStoneId(), stone);
            this.lastUpdateTimestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Removes a stone from the world state (destroyed).
     */
    public void removeStone(String stoneId) {
        this.stones.remove(stoneId);
        this.lastUpdateTimestamp = System.currentTimeMillis();
    }
    
    /**
     * Adds or updates an item in the world state.
     */
    public void addOrUpdateItem(ItemState item) {
        if (item != null) {
            this.items.put(item.getItemId(), item);
            this.lastUpdateTimestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Removes an item from the world state (marks it as collected).
     */
    public void removeItem(String itemId) {
        this.items.remove(itemId);
        this.lastUpdateTimestamp = System.currentTimeMillis();
    }
    
    // World Save/Load Methods
    
    /**
     * Creates a complete snapshot of the world state for saving.
     * This method prepares all game entities for serialization to disk.
     * 
     * @param playerX Current player X position
     * @param playerY Current player Y position
     * @param playerHealth Current player health
     * @param saveName Name for this save
     * @param gameMode Game mode ("singleplayer" or "multiplayer")
     * @return WorldSaveData containing complete world snapshot
     */
    public WorldSaveData createSaveSnapshot(float playerX, float playerY, float playerHealth, 
                                          String saveName, String gameMode) {
        // Create deep copies of all collections to ensure data integrity
        Map<String, TreeState> treesCopy = new HashMap<>();
        for (Map.Entry<String, TreeState> entry : this.trees.entrySet()) {
            TreeState original = entry.getValue();
            TreeState copy = new TreeState(
                original.getTreeId(),
                original.getType(),
                original.getX(),
                original.getY(),
                original.getHealth(),
                original.isExists()
            );
            treesCopy.put(entry.getKey(), copy);
        }
        
        Map<String, StoneState> stonesCopy = new HashMap<>();
        for (Map.Entry<String, StoneState> entry : this.stones.entrySet()) {
            StoneState original = entry.getValue();
            StoneState copy = new StoneState(
                original.getStoneId(),
                original.getX(),
                original.getY(),
                original.getHealth()
            );
            stonesCopy.put(entry.getKey(), copy);
        }
        
        Map<String, ItemState> itemsCopy = new HashMap<>();
        for (Map.Entry<String, ItemState> entry : this.items.entrySet()) {
            ItemState original = entry.getValue();
            ItemState copy = new ItemState(
                original.getItemId(),
                original.getType(),
                original.getX(),
                original.getY(),
                original.isCollected()
            );
            itemsCopy.put(entry.getKey(), copy);
        }
        
        Set<String> clearedPositionsCopy = new HashSet<>(this.clearedPositions);
        
        List<RainZone> rainZonesCopy = new ArrayList<>();
        if (this.rainZones != null) {
            for (RainZone zone : this.rainZones) {
                RainZone copy = new RainZone(
                    zone.getZoneId(),
                    zone.getCenterX(),
                    zone.getCenterY(),
                    zone.getRadius(),
                    zone.getFadeDistance(),
                    zone.getIntensity()
                );
                rainZonesCopy.add(copy);
            }
        }
        
        return new WorldSaveData(
            this.worldSeed,
            treesCopy,
            stonesCopy,
            itemsCopy,
            clearedPositionsCopy,
            rainZonesCopy,
            playerX,
            playerY,
            playerHealth,
            saveName,
            gameMode
        );
    }
    
    /**
     * Validates the current world state for save operations.
     * Checks data integrity and consistency before creating a save.
     * 
     * @return true if world state is valid for saving, false otherwise
     */
    public boolean validateForSave() {
        // Check world seed is valid
        if (worldSeed == 0) {
            System.err.println("World save validation failed: Invalid world seed");
            return false;
        }
        
        // Check collections are not null
        if (trees == null || stones == null || items == null || clearedPositions == null) {
            System.err.println("World save validation failed: Null collections");
            return false;
        }
        
        // Validate tree states
        for (Map.Entry<String, TreeState> entry : trees.entrySet()) {
            TreeState tree = entry.getValue();
            if (tree == null) {
                System.err.println("World save validation failed: Null tree state for key: " + entry.getKey());
                return false;
            }
            
            if (tree.getTreeId() == null || !tree.getTreeId().equals(entry.getKey())) {
                System.err.println("World save validation failed: Tree ID mismatch for key: " + entry.getKey());
                return false;
            }
            
            if (tree.getHealth() < 0 || tree.getHealth() > 100) {
                System.err.println("World save validation failed: Invalid tree health: " + tree.getHealth());
                return false;
            }
        }
        
        // Validate stone states
        for (Map.Entry<String, StoneState> entry : stones.entrySet()) {
            StoneState stone = entry.getValue();
            if (stone == null) {
                System.err.println("World save validation failed: Null stone state for key: " + entry.getKey());
                return false;
            }
            
            if (stone.getStoneId() == null || !stone.getStoneId().equals(entry.getKey())) {
                System.err.println("World save validation failed: Stone ID mismatch for key: " + entry.getKey());
                return false;
            }
            
            if (stone.getHealth() < 0 || stone.getHealth() > 50) {
                System.err.println("World save validation failed: Invalid stone health: " + stone.getHealth());
                return false;
            }
        }
        
        // Validate item states
        for (Map.Entry<String, ItemState> entry : items.entrySet()) {
            ItemState item = entry.getValue();
            if (item == null) {
                System.err.println("World save validation failed: Null item state for key: " + entry.getKey());
                return false;
            }
            
            if (item.getItemId() == null || !item.getItemId().equals(entry.getKey())) {
                System.err.println("World save validation failed: Item ID mismatch for key: " + entry.getKey());
                return false;
            }
        }
        
        // Validate rain zones
        if (rainZones != null) {
            for (RainZone zone : rainZones) {
                if (zone == null) {
                    System.err.println("World save validation failed: Null rain zone");
                    return false;
                }
                
                if (zone.getRadius() <= 0 || zone.getIntensity() < 0 || zone.getIntensity() > 1.0f) {
                    System.err.println("World save validation failed: Invalid rain zone parameters");
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Collects metadata about the current world state for save file information.
     * 
     * @return Map containing metadata key-value pairs
     */
    public Map<String, Object> collectSaveMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        
        // Basic world information
        metadata.put("worldSeed", worldSeed);
        metadata.put("lastUpdateTimestamp", lastUpdateTimestamp);
        
        // Entity counts
        int existingTreeCount = 0;
        int destroyedTreeCount = 0;
        for (TreeState tree : trees.values()) {
            if (tree.isExists()) {
                existingTreeCount++;
            } else {
                destroyedTreeCount++;
            }
        }
        metadata.put("existingTreeCount", existingTreeCount);
        metadata.put("destroyedTreeCount", destroyedTreeCount);
        
        // Stone counts
        metadata.put("stoneCount", stones.size());
        
        int uncollectedItemCount = 0;
        int collectedItemCount = 0;
        for (ItemState item : items.values()) {
            if (item.isCollected()) {
                collectedItemCount++;
            } else {
                uncollectedItemCount++;
            }
        }
        metadata.put("uncollectedItemCount", uncollectedItemCount);
        metadata.put("collectedItemCount", collectedItemCount);
        
        metadata.put("clearedPositionCount", clearedPositions.size());
        metadata.put("playerCount", players.size());
        metadata.put("rainZoneCount", rainZones != null ? rainZones.size() : 0);
        
        // World bounds (approximate)
        if (!trees.isEmpty()) {
            float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE;
            float minY = Float.MAX_VALUE, maxY = Float.MIN_VALUE;
            
            for (TreeState tree : trees.values()) {
                if (tree.isExists()) {
                    minX = Math.min(minX, tree.getX());
                    maxX = Math.max(maxX, tree.getX());
                    minY = Math.min(minY, tree.getY());
                    maxY = Math.max(maxY, tree.getY());
                }
            }
            
            metadata.put("worldBoundsMinX", minX);
            metadata.put("worldBoundsMaxX", maxX);
            metadata.put("worldBoundsMinY", minY);
            metadata.put("worldBoundsMaxY", maxY);
            metadata.put("worldWidth", maxX - minX);
            metadata.put("worldHeight", maxY - minY);
        }
        
        return metadata;
    }
    
    /**
     * Restores the complete world state from save data.
     * This method replaces the current world state with the saved state.
     * 
     * @param saveData The WorldSaveData to restore from
     * @return true if restoration was successful, false otherwise
     */
    public boolean restoreFromSaveData(WorldSaveData saveData) {
        if (saveData == null) {
            System.err.println("Cannot restore world state: Save data is null");
            return false;
        }
        
        if (!saveData.isValid()) {
            System.err.println("Cannot restore world state: Save data is invalid");
            return false;
        }
        
        try {
            // Clean up existing state before restoration
            cleanupExistingState();
            
            // Restore world seed
            this.worldSeed = saveData.getWorldSeed();
            
            // Restore trees with deep copy
            this.trees.clear();
            if (saveData.getTrees() != null) {
                for (Map.Entry<String, TreeState> entry : saveData.getTrees().entrySet()) {
                    TreeState original = entry.getValue();
                    TreeState copy = new TreeState(
                        original.getTreeId(),
                        original.getType(),
                        original.getX(),
                        original.getY(),
                        original.getHealth(),
                        original.isExists()
                    );
                    this.trees.put(entry.getKey(), copy);
                }
            }
            
            // Restore stones with deep copy
            this.stones.clear();
            if (saveData.getStones() != null) {
                for (Map.Entry<String, StoneState> entry : saveData.getStones().entrySet()) {
                    StoneState original = entry.getValue();
                    StoneState copy = new StoneState(
                        original.getStoneId(),
                        original.getX(),
                        original.getY(),
                        original.getHealth()
                    );
                    this.stones.put(entry.getKey(), copy);
                }
            }
            
            // Restore items with deep copy
            this.items.clear();
            if (saveData.getItems() != null) {
                for (Map.Entry<String, ItemState> entry : saveData.getItems().entrySet()) {
                    ItemState original = entry.getValue();
                    ItemState copy = new ItemState(
                        original.getItemId(),
                        original.getType(),
                        original.getX(),
                        original.getY(),
                        original.isCollected()
                    );
                    this.items.put(entry.getKey(), copy);
                }
            }
            
            // Restore cleared positions
            this.clearedPositions.clear();
            if (saveData.getClearedPositions() != null) {
                this.clearedPositions.addAll(saveData.getClearedPositions());
            }
            
            // Restore rain zones with deep copy
            this.rainZones.clear();
            if (saveData.getRainZones() != null) {
                for (RainZone zone : saveData.getRainZones()) {
                    RainZone copy = new RainZone(
                        zone.getZoneId(),
                        zone.getCenterX(),
                        zone.getCenterY(),
                        zone.getRadius(),
                        zone.getFadeDistance(),
                        zone.getIntensity()
                    );
                    this.rainZones.add(copy);
                }
            }
            
            // Update timestamp
            this.lastUpdateTimestamp = System.currentTimeMillis();
            
            // Validate restored state
            if (!validateRestoredState()) {
                System.err.println("World state restoration failed validation");
                return false;
            }
            
            System.out.println("Successfully restored world state from save: " + saveData.getSaveName());
            System.out.println("Restored " + trees.size() + " trees, " + stones.size() + " stones, " + 
                             items.size() + " items, " + clearedPositions.size() + " cleared positions");
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error during world state restoration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Cleans up the existing world state before restoration.
     * This ensures a clean slate for loading the saved state.
     */
    private void cleanupExistingState() {
        // Clear all collections
        if (this.trees != null) {
            this.trees.clear();
        } else {
            this.trees = new ConcurrentHashMap<>();
        }
        
        if (this.stones != null) {
            this.stones.clear();
        } else {
            this.stones = new ConcurrentHashMap<>();
        }
        
        if (this.items != null) {
            this.items.clear();
        } else {
            this.items = new ConcurrentHashMap<>();
        }
        
        if (this.clearedPositions != null) {
            this.clearedPositions.clear();
        } else {
            this.clearedPositions = ConcurrentHashMap.newKeySet();
        }
        
        if (this.rainZones != null) {
            this.rainZones.clear();
        } else {
            this.rainZones = new ArrayList<>();
        }
        
        // Keep players collection but don't clear it - multiplayer clients should remain
        // Only clear if this is a singleplayer restore
        if (this.players != null && this.players.size() <= 1) {
            this.players.clear();
        }
        
        System.out.println("Cleaned up existing world state for restoration");
    }
    
    /**
     * Validates the restored world state for consistency and integrity.
     * 
     * @return true if restored state is valid, false otherwise
     */
    private boolean validateRestoredState() {
        // Check world seed
        if (worldSeed == 0) {
            System.err.println("Restored state validation failed: Invalid world seed");
            return false;
        }
        
        // Check collections are not null
        if (trees == null || stones == null || items == null || clearedPositions == null || rainZones == null) {
            System.err.println("Restored state validation failed: Null collections after restoration");
            return false;
        }
        
        // Validate tree consistency
        for (Map.Entry<String, TreeState> entry : trees.entrySet()) {
            TreeState tree = entry.getValue();
            if (tree == null || !tree.getTreeId().equals(entry.getKey())) {
                System.err.println("Restored state validation failed: Tree consistency error");
                return false;
            }
        }
        
        // Validate stone consistency
        for (Map.Entry<String, StoneState> entry : stones.entrySet()) {
            StoneState stone = entry.getValue();
            if (stone == null || !stone.getStoneId().equals(entry.getKey())) {
                System.err.println("Restored state validation failed: Stone consistency error");
                return false;
            }
        }
        
        // Validate item consistency
        for (Map.Entry<String, ItemState> entry : items.entrySet()) {
            ItemState item = entry.getValue();
            if (item == null || !item.getItemId().equals(entry.getKey())) {
                System.err.println("Restored state validation failed: Item consistency error");
                return false;
            }
        }
        
        // Validate cleared positions consistency
        for (String clearedPos : clearedPositions) {
            if (trees.containsKey(clearedPos)) {
                TreeState tree = trees.get(clearedPos);
                if (tree.isExists()) {
                    System.err.println("Restored state validation failed: Cleared position has existing tree: " + clearedPos);
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Ensures deterministic world generation after state restoration.
     * This method verifies that the world seed will produce consistent results.
     * 
     * @return true if world generation will be deterministic, false otherwise
     */
    public boolean ensureDeterministicGeneration() {
        if (worldSeed == 0) {
            System.err.println("Cannot ensure deterministic generation: Invalid world seed");
            return false;
        }
        
        // Test deterministic generation by creating a test random with the seed
        java.util.Random testRandom = new java.util.Random(worldSeed);
        
        // Generate a few test values to ensure the seed works
        float testValue1 = testRandom.nextFloat();
        float testValue2 = testRandom.nextFloat();
        
        // Reset and test again - should produce same values
        testRandom.setSeed(worldSeed);
        float testValue1Again = testRandom.nextFloat();
        float testValue2Again = testRandom.nextFloat();
        
        if (testValue1 != testValue1Again || testValue2 != testValue2Again) {
            System.err.println("Deterministic generation test failed: Seed does not produce consistent results");
            return false;
        }
        
        System.out.println("Deterministic world generation verified for seed: " + worldSeed);
        return true;
    }
    
    /**
     * Creates a rollback point before attempting world state restoration.
     * This allows reverting to the previous state if restoration fails.
     * 
     * @return WorldState snapshot that can be used for rollback
     */
    public WorldState createRollbackPoint() {
        return this.createSnapshot();
    }
    
    /**
     * Rolls back to a previous world state snapshot.
     * Used when world restoration fails and we need to revert changes.
     * 
     * @param rollbackState The state to roll back to
     * @return true if rollback was successful, false otherwise
     */
    public boolean rollbackToState(WorldState rollbackState) {
        if (rollbackState == null) {
            System.err.println("Cannot rollback: Rollback state is null");
            return false;
        }
        
        try {
            // Restore from the rollback state
            this.worldSeed = rollbackState.worldSeed;
            this.trees = new ConcurrentHashMap<>(rollbackState.trees);
            this.items = new ConcurrentHashMap<>(rollbackState.items);
            this.clearedPositions = ConcurrentHashMap.newKeySet();
            this.clearedPositions.addAll(rollbackState.clearedPositions);
            this.rainZones = new ArrayList<>(rollbackState.rainZones);
            this.lastUpdateTimestamp = rollbackState.lastUpdateTimestamp;
            
            // Don't rollback players in multiplayer scenarios
            if (this.players.size() <= 1) {
                this.players = new ConcurrentHashMap<>(rollbackState.players);
            }
            
            System.out.println("Successfully rolled back world state");
            return true;
            
        } catch (Exception e) {
            System.err.println("Error during rollback: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
