package wagemaker.uk.network;

/**
 * Default implementation of MessageHandler that dispatches messages to type-specific handlers.
 * Subclasses can override individual handler methods to implement custom behavior.
 */
public class DefaultMessageHandler implements MessageHandler {
    
    /**
     * Dispatches the message to the appropriate handler method based on message type.
     * @param message The message to handle
     */
    @Override
    public void handleMessage(NetworkMessage message) {
        if (message == null) {
            System.err.println("Received null message");
            return;
        }
        
        try {
            switch (message.getType()) {
                case CONNECTION_ACCEPTED:
                    handleConnectionAccepted((ConnectionAcceptedMessage) message);
                    break;
                    
                case CONNECTION_REJECTED:
                    handleConnectionRejected((ConnectionRejectedMessage) message);
                    break;
                    
                case WORLD_STATE:
                    handleWorldState((WorldStateMessage) message);
                    break;
                    
                case WORLD_STATE_UPDATE:
                    handleWorldStateUpdate((WorldStateUpdateMessage) message);
                    break;
                    
                case PLAYER_MOVEMENT:
                    handlePlayerMovement((PlayerMovementMessage) message);
                    break;
                    
                case PLAYER_JOIN:
                    handlePlayerJoin((PlayerJoinMessage) message);
                    break;
                    
                case PLAYER_LEAVE:
                    handlePlayerLeave((PlayerLeaveMessage) message);
                    break;
                    
                case PLAYER_HEALTH_UPDATE:
                    handlePlayerHealthUpdate((PlayerHealthUpdateMessage) message);
                    break;
                    
                case TREE_HEALTH_UPDATE:
                    handleTreeHealthUpdate((TreeHealthUpdateMessage) message);
                    break;
                    
                case TREE_DESTROYED:
                    handleTreeDestroyed((TreeDestroyedMessage) message);
                    break;
                    
                case ITEM_SPAWN:
                    handleItemSpawn((ItemSpawnMessage) message);
                    break;
                    
                case ITEM_PICKUP:
                    handleItemPickup((ItemPickupMessage) message);
                    break;
                    
                case ATTACK_ACTION:
                    handleAttackAction((AttackActionMessage) message);
                    break;
                    
                case HEARTBEAT:
                    handleHeartbeat((HeartbeatMessage) message);
                    break;
                    
                case POSITION_CORRECTION:
                    handlePositionCorrection((PositionCorrectionMessage) message);
                    break;
                    
                case PING:
                    handlePing((PingMessage) message);
                    break;
                    
                case PONG:
                    handlePong((PongMessage) message);
                    break;
                    
                default:
                    System.err.println("Unknown message type: " + message.getType());
            }
        } catch (ClassCastException e) {
            System.err.println("Message type mismatch: expected " + message.getType() + 
                             " but got " + message.getClass().getSimpleName());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error handling message of type " + message.getType() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handles CONNECTION_ACCEPTED message.
     * Override this method to implement custom behavior.
     */
    protected void handleConnectionAccepted(ConnectionAcceptedMessage message) {
        System.out.println("Connection accepted. Client ID: " + message.getAssignedClientId());
    }
    
    /**
     * Handles CONNECTION_REJECTED message.
     * Override this method to implement custom behavior.
     */
    protected void handleConnectionRejected(ConnectionRejectedMessage message) {
        System.out.println("Connection rejected");
    }
    
    /**
     * Handles WORLD_STATE message containing complete world snapshot.
     * Override this method to apply world state to game.
     */
    protected void handleWorldState(WorldStateMessage message) {
        System.out.println("Received world state with seed: " + message.getWorldSeed());
        System.out.println("  Players: " + message.getPlayers().size());
        System.out.println("  Trees: " + message.getTrees().size());
        System.out.println("  Items: " + message.getItems().size());
    }
    
    /**
     * Handles WORLD_STATE_UPDATE message containing incremental changes.
     * Override this method to apply state updates to game.
     */
    protected void handleWorldStateUpdate(WorldStateUpdateMessage message) {
        if (message.getUpdatedPlayers() != null && !message.getUpdatedPlayers().isEmpty()) {
            System.out.println("Updated players: " + message.getUpdatedPlayers().size());
        }
        if (message.getUpdatedTrees() != null && !message.getUpdatedTrees().isEmpty()) {
            System.out.println("Updated trees: " + message.getUpdatedTrees().size());
        }
        if (message.getUpdatedItems() != null && !message.getUpdatedItems().isEmpty()) {
            System.out.println("Updated items: " + message.getUpdatedItems().size());
        }
    }
    
    /**
     * Handles PLAYER_MOVEMENT message.
     * Override this method to update remote player positions.
     */
    protected void handlePlayerMovement(PlayerMovementMessage message) {
        System.out.println("Player " + message.getSenderId() + " moved to (" + 
                         message.getX() + ", " + message.getY() + ")");
    }
    
    /**
     * Handles PLAYER_JOIN message.
     * Override this method to add new remote players.
     */
    protected void handlePlayerJoin(PlayerJoinMessage message) {
        System.out.println("Player joined: " + message.getPlayerName() + " (ID: " + message.getPlayerId() + ")");
    }
    
    /**
     * Handles PLAYER_LEAVE message.
     * Override this method to remove remote players.
     */
    protected void handlePlayerLeave(PlayerLeaveMessage message) {
        System.out.println("Player left: " + message.getPlayerName() + " (ID: " + message.getPlayerId() + ")");
    }
    
    /**
     * Handles PLAYER_HEALTH_UPDATE message.
     * Override this method to update player health displays.
     */
    protected void handlePlayerHealthUpdate(PlayerHealthUpdateMessage message) {
        System.out.println("Player " + message.getPlayerId() + " health: " + message.getHealth());
    }
    
    /**
     * Handles TREE_HEALTH_UPDATE message.
     * Override this method to update tree health bars.
     */
    protected void handleTreeHealthUpdate(TreeHealthUpdateMessage message) {
        System.out.println("Tree " + message.getTreeId() + " health: " + message.getHealth());
    }
    
    /**
     * Handles TREE_DESTROYED message.
     * Override this method to remove trees from game world.
     */
    protected void handleTreeDestroyed(TreeDestroyedMessage message) {
        System.out.println("Tree destroyed: " + message.getTreeId() + " at (" + 
                         message.getX() + ", " + message.getY() + ")");
    }
    
    /**
     * Handles ITEM_SPAWN message.
     * Override this method to spawn items in game world.
     */
    protected void handleItemSpawn(ItemSpawnMessage message) {
        System.out.println("Item spawned: " + message.getItemType() + " (ID: " + message.getItemId() + 
                         ") at (" + message.getX() + ", " + message.getY() + ")");
    }
    
    /**
     * Handles ITEM_PICKUP message.
     * Override this method to remove items from game world.
     */
    protected void handleItemPickup(ItemPickupMessage message) {
        System.out.println("Item picked up: " + message.getItemId() + " by player " + message.getPlayerId());
    }
    
    /**
     * Handles ATTACK_ACTION message.
     * Override this method to display attack animations.
     */
    protected void handleAttackAction(AttackActionMessage message) {
        System.out.println("Player " + message.getPlayerId() + " attacked " + message.getTargetId());
    }
    
    /**
     * Handles HEARTBEAT message.
     * Override this method to implement heartbeat logic.
     */
    protected void handleHeartbeat(HeartbeatMessage message) {
        // Heartbeats are typically silent
    }
    
    /**
     * Handles POSITION_CORRECTION message.
     * Override this method to correct player position when desynchronization is detected.
     */
    protected void handlePositionCorrection(PositionCorrectionMessage message) {
        System.err.println("Position correction for player " + message.getPlayerId() + 
                         ": " + message.getReason());
        System.err.println("Corrected position: (" + message.getCorrectedX() + ", " + 
                         message.getCorrectedY() + ")");
    }
    
    /**
     * Handles PING message.
     * Override this method to implement ping logic (typically server-side).
     */
    protected void handlePing(PingMessage message) {
        // Pings are typically handled by the server
    }
    
    /**
     * Handles PONG message.
     * Override this method to calculate latency (typically client-side).
     */
    protected void handlePong(PongMessage message) {
        // Pongs are typically handled by the client
    }
}
