package wagemaker.uk.gdx;

import wagemaker.uk.client.PlayerConfig;
import wagemaker.uk.network.ConnectionAcceptedMessage;
import wagemaker.uk.network.DefaultMessageHandler;
import wagemaker.uk.network.InventorySyncMessage;
import wagemaker.uk.network.ItemPickupMessage;
import wagemaker.uk.network.ItemSpawnMessage;
import wagemaker.uk.network.ItemState;
import wagemaker.uk.network.PlayerHealthUpdateMessage;
import wagemaker.uk.network.PlayerJoinMessage;
import wagemaker.uk.network.PlayerLeaveMessage;
import wagemaker.uk.network.PlayerMovementMessage;
import wagemaker.uk.network.PongMessage;
import wagemaker.uk.network.PositionCorrectionMessage;
import wagemaker.uk.network.StoneCreatedMessage;
import wagemaker.uk.network.StoneDestroyedMessage;
import wagemaker.uk.network.StoneHealthUpdateMessage;
import wagemaker.uk.network.StoneState;
import wagemaker.uk.network.TreeCreatedMessage;
import wagemaker.uk.network.TreeDestroyedMessage;
import wagemaker.uk.network.TreeHealthUpdateMessage;
import wagemaker.uk.network.TreeRemovalMessage;
import wagemaker.uk.network.TreeState;
import wagemaker.uk.network.WorldState;
import wagemaker.uk.network.WorldStateMessage;
import wagemaker.uk.network.WorldStateUpdateMessage;
import wagemaker.uk.network.ResourceRespawnMessage;
import wagemaker.uk.network.RespawnStateMessage;
import wagemaker.uk.player.RemotePlayer;

/**
 * Custom message handler for MyGdxGame that processes network messages
 * and updates the game state accordingly.
 */
public class GameMessageHandler extends DefaultMessageHandler {
    private MyGdxGame game;
    
    /**
     * Creates a new GameMessageHandler.
     * @param game The game instance to update
     */
    public GameMessageHandler(MyGdxGame game) {
        this.game = game;
    }
    
    @Override
    protected void handleConnectionAccepted(ConnectionAcceptedMessage message) {
        super.handleConnectionAccepted(message);
        
        // Set the client ID
        if (game.getGameClient() != null) {
            game.getGameClient().setClientId(message.getAssignedClientId());
            
            // Handle planting range configuration and notify game instance
            game.getGameClient().handleConnectionAccepted(message, game);
        }
        
        // Save the server address for future connections
        String serverAddress = game.getLastConnectionAddress();
        if (serverAddress != null) {
            PlayerConfig config = PlayerConfig.load();
            config.saveLastServer(serverAddress);
        }
        
        System.out.println("Connected to server. Client ID: " + message.getAssignedClientId());
    }
    
    @Override
    protected void handleWorldState(WorldStateMessage message) {
        super.handleWorldState(message);
        
        // Sync the world state with the game
        WorldState worldState = new WorldState(message.getWorldSeed());
        worldState.setPlayers(message.getPlayers());
        worldState.setTrees(message.getTrees());
        worldState.setStones(message.getStones());
        worldState.setItems(message.getItems());
        worldState.setClearedPositions(message.getClearedPositions());
        worldState.setRainZones(message.getRainZones());
        
        game.syncWorldState(worldState);
        
        // Sync rain zones to the rain system
        if (message.getRainZones() != null && game.rainSystem != null) {
            game.rainSystem.syncRainZones(message.getRainZones());
        }
    }
    
    @Override
    protected void handleWorldStateUpdate(WorldStateUpdateMessage message) {
        super.handleWorldStateUpdate(message);
        
        // Process tree updates
        if (message.getUpdatedTrees() != null) {
            for (TreeState treeState : message.getUpdatedTrees().values()) {
                game.updateTreeFromState(treeState);
            }
        }
        
        // Process item updates
        if (message.getUpdatedItems() != null) {
            for (ItemState itemState : message.getUpdatedItems().values()) {
                game.updateItemFromState(itemState);
            }
        }
        
        // Process player updates (if needed in the future)
        if (message.getUpdatedPlayers() != null) {
            for (wagemaker.uk.network.PlayerState playerState : message.getUpdatedPlayers().values()) {
                // Update remote player if exists
                RemotePlayer remotePlayer = game.getRemotePlayers().get(playerState.getPlayerId());
                if (remotePlayer != null) {
                    remotePlayer.updatePosition(
                        playerState.getX(),
                        playerState.getY(),
                        playerState.getDirection(),
                        playerState.isMoving()
                    );
                    remotePlayer.updateHealth(playerState.getHealth());
                }
            }
        }
    }
    
    @Override
    protected void handlePlayerMovement(PlayerMovementMessage message) {
        // Validate message
        if (message == null || message.getSenderId() == null) {
            System.err.println("Received invalid player movement message (null sender)");
            return;
        }
        
        // Don't process our own movement messages
        if (game.getGameClient() != null && 
            message.getSenderId().equals(game.getGameClient().getClientId())) {
            return;
        }
        
        // Update or create remote player
        String playerId = message.getSenderId();
        RemotePlayer remotePlayer = game.getRemotePlayers().get(playerId);
        
        if (remotePlayer != null) {
            remotePlayer.updatePosition(
                message.getX(), 
                message.getY(), 
                message.getDirection(), 
                message.isMoving()
            );
        } else {
            // Remote player doesn't exist yet - create it on-demand
            // This can happen if movement messages arrive before the join message is processed
            System.out.println("Creating remote player on-demand for: " + playerId);
            
            // Create a PlayerJoinMessage and queue it for main thread processing
            PlayerJoinMessage joinMessage = new PlayerJoinMessage(
                playerId,
                "Player_" + playerId.substring(0, 8), // Default name
                message.getX(),
                message.getY()
            );
            game.queuePlayerJoin(joinMessage);
        }
    }
    
    @Override
    protected void handlePlayerJoin(PlayerJoinMessage message) {
        super.handlePlayerJoin(message);
        
        // Queue the player join to be processed on the main thread
        // This is necessary because RemotePlayer creation involves OpenGL operations
        game.queuePlayerJoin(message);
    }
    
    @Override
    protected void handlePlayerLeave(PlayerLeaveMessage message) {
        super.handlePlayerLeave(message);
        
        // Queue the player leave to be processed on the main thread
        game.queuePlayerLeave(message.getPlayerId());
    }
    
    @Override
    protected void handleTreeHealthUpdate(TreeHealthUpdateMessage message) {
        TreeState treeState = new TreeState();
        treeState.setTreeId(message.getTreeId());
        treeState.setHealth(message.getHealth());
        treeState.setExists(true);
        
        game.updateTreeFromState(treeState);
    }
    
    @Override
    protected void handleTreeDestroyed(TreeDestroyedMessage message) {
        System.out.println("[DEBUG] Received TreeDestroyedMessage for: " + message.getTreeId());
        game.removeTree(message.getTreeId());
    }
    
    @Override
    protected void handleTreeRemoval(TreeRemovalMessage message) {
        System.out.println("Ghost tree removal requested: " + message.getTreeId());
        System.out.println("  Reason: " + message.getReason());
        game.removeTree(message.getTreeId());
    }
    
    @Override
    protected void handleStoneHealthUpdate(StoneHealthUpdateMessage message) {
        StoneState stoneState = new StoneState();
        stoneState.setStoneId(message.getStoneId());
        stoneState.setHealth(message.getHealth());
        
        game.updateStoneFromState(stoneState);
    }
    
    @Override
    protected void handleStoneDestroyed(StoneDestroyedMessage message) {
        game.removeStone(message.getStoneId());
    }
    
    @Override
    protected void handleItemSpawn(ItemSpawnMessage message) {
        ItemState itemState = new ItemState(
            message.getItemId(),
            message.getItemType(),
            message.getX(),
            message.getY(),
            false
        );
        
        game.updateItemFromState(itemState);
    }
    
    @Override
    protected void handleItemPickup(ItemPickupMessage message) {
        // If this pickup is for the local player, collect the item into inventory
        if (game.getGameClient() != null && 
            message.getPlayerId().equals(game.getGameClient().getClientId())) {
            
            // Get the item type before removing it
            wagemaker.uk.inventory.ItemType itemType = game.getItemType(message.getItemId());
            
            // Collect the item into inventory
            if (itemType != null && game.getInventoryManager() != null) {
                game.getInventoryManager().collectItem(itemType);
                System.out.println("Collected " + itemType + " into inventory");
            }
        }
        
        // Remove the item from the game world
        game.removeItem(message.getItemId());
    }
    
    @Override
    protected void handlePlayerHealthUpdate(PlayerHealthUpdateMessage message) {
        String playerId = message.getPlayerId();
        float health = message.getHealth();
        
        // Update local player health if it's for us
        if (game.getGameClient() != null && 
            playerId.equals(game.getGameClient().getClientId())) {
            game.getPlayer().setHealth(health);
            System.out.println("Taking damage! Health: " + health);
        }
        
        // Update remote player health
        RemotePlayer remotePlayer = game.getRemotePlayers().get(playerId);
        if (remotePlayer != null) {
            remotePlayer.updateHealth(health);
        }
    }
    
    @Override
    protected void handlePositionCorrection(PositionCorrectionMessage message) {
        // Only process corrections for our own player
        if (game.getGameClient() == null || 
            !message.getPlayerId().equals(game.getGameClient().getClientId())) {
            return;
        }
        
        // Log the desynchronization event
        System.err.println("Position correction received: " + message.getReason());
        System.err.println("Correcting position to: (" + message.getCorrectedX() + ", " + message.getCorrectedY() + ")");
        
        // Apply correction to local player with smooth interpolation
        game.correctPlayerPosition(
            message.getCorrectedX(),
            message.getCorrectedY(),
            message.getCorrectedDirection()
        );
        
        // Display notification to player
        game.displayNotification("Position corrected");
    }
    
    @Override
    protected void handlePong(PongMessage message) {
        // Pass pong to game client for latency calculation
        if (game.getGameClient() != null) {
            game.getGameClient().handlePong(message);
        }
    }
    
    @Override
    protected void handleInventorySync(InventorySyncMessage message) {
        // Only process sync for our own player
        if (game.getGameClient() == null || 
            !message.getPlayerId().equals(game.getGameClient().getClientId())) {
            return;
        }
        
        // Update inventory from server sync
        if (game.getInventoryManager() != null) {
            game.getInventoryManager().syncFromServer(
                message.getAppleCount(),
                message.getBananaCount(),
                message.getBabyBambooCount(),
                message.getBambooStackCount(),
                message.getWoodStackCount(),
                message.getPebbleCount()
            );
        }
    }
    
    @Override
    protected void handleBambooPlant(wagemaker.uk.network.BambooPlantMessage message) {
        String myClientId = game.getGameClient() != null ? game.getGameClient().getClientId() : "null";
        String senderPlayerId = message.getPlayerId();
        
        System.out.println("[GameMessageHandler] Received BambooPlantMessage:");
        System.out.println("  - My Client ID: " + myClientId);
        System.out.println("  - Sender Player ID: " + senderPlayerId);
        System.out.println("  - Planted Bamboo ID: " + message.getPlantedBambooId());
        System.out.println("  - Position: (" + message.getX() + ", " + message.getY() + ")");
        
        // Don't process our own planting messages (already handled locally)
        if (game.getGameClient() != null && 
            message.getPlayerId().equals(game.getGameClient().getClientId())) {
            System.out.println("  - SKIPPING: This is my own planting message");
            return;
        }
        
        System.out.println("  - PROCESSING: This is a remote player's planting");
        // Queue bamboo planting to be processed on the main thread
        game.queueBambooPlant(message);
    }
    
    @Override
    protected void handleBambooTransform(wagemaker.uk.network.BambooTransformMessage message) {
        // Queue bamboo transformation to be processed on the main thread
        game.queueBambooTransform(message);
    }
    
    @Override
    protected void handleTreeCreated(wagemaker.uk.network.TreeCreatedMessage message) {
        // Create tree state from message
        TreeState treeState = new TreeState(
            message.getTreeId(),
            message.getTreeType(),
            message.getX(),
            message.getY(),
            message.getHealth(),
            true
        );
        
        // Update game with new tree
        game.updateTreeFromState(treeState);
    }
    
    @Override
    protected void handleStoneCreated(wagemaker.uk.network.StoneCreatedMessage message) {
        // Create stone state from message
        StoneState stoneState = new StoneState(
            message.getStoneId(),
            message.getX(),
            message.getY(),
            message.getHealth()
        );
        
        // Update game with new stone
        game.updateStoneFromState(stoneState);
    }
    
    @Override
    protected void handleResourceRespawn(wagemaker.uk.network.ResourceRespawnMessage message) {
        // Pass the respawn message to the respawn manager
        if (game.getRespawnManager() != null) {
            game.getRespawnManager().handleResourceRespawn(
                message.getResourceId(),
                message.getResourceType(),
                message.getTreeType(),
                message.getX(),
                message.getY()
            );
        } else {
            System.err.println("Cannot handle resource respawn: RespawnManager not initialized");
        }
    }
    
    @Override
    protected void handleRespawnState(wagemaker.uk.network.RespawnStateMessage message) {
        // Pass the respawn state to the respawn manager for synchronization
        if (game.getRespawnManager() != null) {
            game.getRespawnManager().handleRespawnStateSync(message.getPendingRespawns());
        } else {
            System.err.println("Cannot handle respawn state: RespawnManager not initialized");
        }
    }
}
