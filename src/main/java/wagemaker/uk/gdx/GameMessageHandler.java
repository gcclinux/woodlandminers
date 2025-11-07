package wagemaker.uk.gdx;

import wagemaker.uk.network.*;
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
        worldState.setItems(message.getItems());
        worldState.setClearedPositions(message.getClearedPositions());
        
        game.syncWorldState(worldState);
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
        game.removeTree(message.getTreeId());
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
}
