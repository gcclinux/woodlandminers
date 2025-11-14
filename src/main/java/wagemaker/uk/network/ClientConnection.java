package wagemaker.uk.network;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ClientConnection manages an individual client's connection to the server.
 * It handles message receiving, sending, heartbeat tracking, and timeout detection.
 */
public class ClientConnection implements Runnable {
    private static final long HEARTBEAT_INTERVAL = 5000; // 5 seconds
    private static final long CLIENT_TIMEOUT = 15000; // 15 seconds
    private static final int MESSAGE_RATE_LIMIT = 100; // messages per second
    private static final int MAX_MESSAGE_SIZE = 65536; // 64KB max message size
    private static final float MAX_SPEED = 500.0f; // pixels per second
    private static final float UPDATE_RATE = 20.0f; // updates per second
    private static final float MAX_DISTANCE_PER_UPDATE = MAX_SPEED / UPDATE_RATE * 2; // ~50 pixels with buffer
    private static final float ATTACK_RANGE = 100.0f; // pixels
    private static final float PICKUP_RANGE = 50.0f; // pixels
    private static final long PLAYER_ATTACK_COOLDOWN_MS = 500; // 500 milliseconds
    private static final float PLAYER_DAMAGE = 10.0f; // damage per attack
    private static final int MAX_GHOST_TREE_ATTACKS = 10; // Maximum ghost tree attacks before disconnect
    private static final long INVENTORY_SYNC_INTERVAL = 10000; // 10 seconds
    
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private String clientId;
    private PlayerState playerState;
    private GameServer server;
    private Thread receiveThread;
    private long lastHeartbeat;
    private long lastMessageTime;
    private int messageCount;
    private boolean running;
    private Map<String, Long> playerAttackCooldowns;
    private Map<String, Integer> ghostTreeAttempts;
    private boolean isFirstPositionUpdate = true;
    private long lastInventorySync;
    
    /**
     * Creates a new ClientConnection.
     * @param socket The client's socket
     * @param clientId The unique client identifier
     * @param server The game server instance
     * @throws IOException if streams cannot be created
     */
    public ClientConnection(Socket socket, String clientId, GameServer server) throws IOException {
        this.socket = socket;
        this.clientId = clientId;
        this.server = server;
        this.running = true;
        this.lastHeartbeat = System.currentTimeMillis();
        this.lastMessageTime = System.currentTimeMillis();
        this.messageCount = 0;
        this.playerAttackCooldowns = new HashMap<>();
        this.ghostTreeAttempts = new HashMap<>();
        this.lastInventorySync = System.currentTimeMillis();
        
        // Create output stream first (important for ObjectStream protocol)
        this.output = new ObjectOutputStream(socket.getOutputStream());
        this.output.flush();
        this.input = new ObjectInputStream(socket.getInputStream());
        
        // Initialize player state
        this.playerState = new PlayerState();
        this.playerState.setPlayerId(clientId);
        this.playerState.setPlayerName("Player_" + clientId.substring(0, 8));
        this.playerState.setX(0);
        this.playerState.setY(0);
        this.playerState.setDirection(Direction.DOWN);
        this.playerState.setHealth(100);
        this.playerState.setMoving(false);
    }
    
    /**
     * Main run loop for handling client messages.
     * This method is executed in a thread pool.
     */
    @Override
    public void run() {
        try {
            // Send connection accepted message with client ID
            sendMessage(new ConnectionAcceptedMessage("server", clientId, "Welcome to the server!"));
            
            // Send initial world state
            WorldState snapshot = server.getWorldState().createSnapshot();
            sendMessage(new WorldStateMessage("server", 
                snapshot.getWorldSeed(),
                snapshot.getPlayers(),
                snapshot.getTrees(),
                snapshot.getStones(),
                snapshot.getItems(),
                snapshot.getClearedPositions(),
                snapshot.getRainZones()));
            
            // Add player to world state
            server.getWorldState().addOrUpdatePlayer(playerState);
            
            // Notify other clients about new player
            PlayerJoinMessage joinMessage = new PlayerJoinMessage(clientId, 
                playerState.getPlayerName(), playerState.getX(), playerState.getY());
            server.broadcastToAllExcept(joinMessage, clientId);
            
            // Start message receiving loop
            receiveMessages();
            
        } catch (Exception e) {
            System.err.println("Error in client connection " + clientId + ": " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    /**
     * Receives and processes messages from the client.
     */
    private void receiveMessages() {
        while (running && !socket.isClosed()) {
            try {
                // Check for timeout
                if (System.currentTimeMillis() - lastHeartbeat > CLIENT_TIMEOUT) {
                    System.out.println("Client " + clientId + " timed out");
                    logSecurityViolation("Connection timeout");
                    break;
                }
                
                // Check if inventory sync is needed
                long syncTime = System.currentTimeMillis();
                if (syncTime - lastInventorySync >= INVENTORY_SYNC_INTERVAL) {
                    sendInventorySync();
                    lastInventorySync = syncTime;
                }
                
                // Check rate limiting
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastMessageTime > 1000) {
                    // Reset counter every second
                    messageCount = 0;
                    lastMessageTime = currentTime;
                }
                
                if (messageCount >= MESSAGE_RATE_LIMIT) {
                    System.out.println("Client " + clientId + " exceeded rate limit");
                    logSecurityViolation("Rate limit exceeded: " + messageCount + " messages/sec");
                    break;
                }
                
                // Read message with size validation
                Object obj = input.readObject();
                
                // Validate message type
                if (!(obj instanceof NetworkMessage)) {
                    System.err.println("Invalid message type from " + clientId);
                    logSecurityViolation("Invalid message type: " + obj.getClass().getName());
                    continue;
                }
                
                NetworkMessage message = (NetworkMessage) obj;
                
                // Validate message size (approximate check using serialization)
                if (!validateMessageSize(message)) {
                    System.err.println("Message too large from " + clientId);
                    logSecurityViolation("Message size exceeds limit");
                    continue;
                }
                
                messageCount++;
                handleMessage(message);
                
            } catch (SocketException e) {
                // Socket closed, exit gracefully
                break;
            } catch (EOFException e) {
                // Client disconnected
                System.out.println("Client " + clientId + " disconnected");
                break;
            } catch (IOException e) {
                if (running) {
                    System.err.println("IO error receiving message from " + clientId + ": " + e.getMessage());
                }
                break;
            } catch (ClassNotFoundException e) {
                System.err.println("Unknown message type from " + clientId + ": " + e.getMessage());
                logSecurityViolation("Unknown message class: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Unexpected error from " + clientId + ": " + e.getMessage());
                logSecurityViolation("Unexpected error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handles an incoming message from the client.
     * @param message The message to handle
     */
    private void handleMessage(NetworkMessage message) {
        // Update heartbeat for any message
        lastHeartbeat = System.currentTimeMillis();
        
        switch (message.getType()) {
            case HEARTBEAT:
                // Just update heartbeat timestamp (already done above)
                break;
                
            case PLAYER_MOVEMENT:
                handlePlayerMovement((PlayerMovementMessage) message);
                break;
                
            case ATTACK_ACTION:
                handleAttackAction((AttackActionMessage) message);
                break;
                
            case ITEM_PICKUP:
                handleItemPickup((ItemPickupMessage) message);
                break;
                
            case PING:
                handlePing((PingMessage) message);
                break;
                
            case PLAYER_HEALTH_UPDATE:
                handlePlayerHealthUpdate((PlayerHealthUpdateMessage) message);
                break;
                
            case INVENTORY_UPDATE:
                handleInventoryUpdate((InventoryUpdateMessage) message);
                break;
                
            case BAMBOO_PLANT:
                handleBambooPlant((BambooPlantMessage) message);
                break;
                
            case BAMBOO_TRANSFORM:
                handleBambooTransform((BambooTransformMessage) message);
                break;
                
            default:
                System.out.println("Unhandled message type from " + clientId + ": " + message.getType());
                break;
        }
    }
    
    /**
     * Handles a player movement message.
     * @param message The movement message
     */
    private void handlePlayerMovement(PlayerMovementMessage message) {
        // Validate message data
        if (message == null) {
            logSecurityViolation("Null movement message");
            return;
        }
        
        // Validate position values (check for NaN, Infinity)
        if (!isValidPosition(message.getX(), message.getY())) {
            System.err.println("Invalid position values from " + clientId);
            logSecurityViolation("Invalid position: x=" + message.getX() + ", y=" + message.getY());
            return;
        }
        
        // Validate direction
        if (message.getDirection() == null) {
            System.err.println("Null direction from " + clientId);
            logSecurityViolation("Null direction in movement message");
            return;
        }
        
        // Validate position (speed check) - skip for first position update to allow saved position loading
        if (!isFirstPositionUpdate) {
            float dx = message.getX() - playerState.getX();
            float dy = message.getY() - playerState.getY();
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            
            if (distance > MAX_DISTANCE_PER_UPDATE) {
                // Possible cheating or desync, send correction
                System.out.println("Invalid movement from " + clientId + ", distance: " + distance + 
                                 " (max: " + MAX_DISTANCE_PER_UPDATE + ")");
                logSecurityViolation("Speed check failed: distance=" + distance);
                
                // Send position correction to client
                String reason = "Speed check failed: moved " + String.format("%.1f", distance) + " pixels";
                sendMessage(new PositionCorrectionMessage("server", clientId,
                    playerState.getX(), playerState.getY(), 
                    playerState.getDirection(), reason));
                return;
            }
        } else {
            // First position update - allow client to spawn at their saved position
            System.out.println("First position update from " + clientId + ": (" + message.getX() + ", " + message.getY() + ")");
            isFirstPositionUpdate = false;
        }
        
        // Update player state
        playerState.setX(message.getX());
        playerState.setY(message.getY());
        playerState.setDirection(message.getDirection());
        playerState.setMoving(message.isMoving());
        playerState.setLastUpdateTime(System.currentTimeMillis());
        
        // Update in world state
        server.getWorldState().addOrUpdatePlayer(playerState);
        
        // Broadcast to other clients
        server.broadcastToAllExcept(message, clientId);
    }
    
    /**
     * Handles an attack action message.
     * @param message The attack message
     */
    private void handleAttackAction(AttackActionMessage message) {
        // Validate message data
        if (message == null) {
            logSecurityViolation("Null attack message");
            return;
        }
        
        String targetId = message.getTargetId();
        
        // Validate target ID
        if (targetId == null || targetId.isEmpty()) {
            System.err.println("Invalid target ID from " + clientId);
            logSecurityViolation("Invalid target ID in attack message");
            return;
        }
        
        // Validate damage value
        float damage = message.getDamage();
        if (damage < 0 || damage > 100 || Float.isNaN(damage) || Float.isInfinite(damage)) {
            System.err.println("Invalid damage value from " + clientId + ": " + damage);
            logSecurityViolation("Invalid damage value: " + damage);
            return;
        }
        
        // Check if target is a player
        if (server.getWorldState().getPlayers().containsKey(targetId)) {
            // Target is a player, route to player attack handler
            handlePlayerAttack(message);
            return;
        }
        
        // Check if target is a stone
        StoneState stone = server.getWorldState().getStones().get(targetId);
        
        // If stone doesn't exist in server state, try to generate it deterministically
        if (stone == null) {
            // Parse stone coordinates from targetId (format: "x,y")
            try {
                String[] coords = targetId.split(",");
                if (coords.length == 2) {
                    int stoneX = Integer.parseInt(coords[0]);
                    int stoneY = Integer.parseInt(coords[1]);
                    
                    // Try to generate the stone using deterministic logic
                    stone = server.getWorldState().generateStoneAt(stoneX, stoneY);
                }
            } catch (Exception e) {
                System.err.println("Failed to parse stone coordinates from targetId: " + targetId);
            }
        }
        
        if (stone != null) {
            handleStoneAttack(message, stone);
            return;
        }
        
        // Target is not a player or stone, continue with existing tree attack logic
        TreeState tree = server.getWorldState().getTrees().get(targetId);
        
        // If tree doesn't exist in server state, try to generate it deterministically
        if (tree == null) {
            // Parse tree coordinates from targetId (format: "x,y")
            try {
                String[] coords = targetId.split(",");
                if (coords.length == 2) {
                    int treeX = Integer.parseInt(coords[0]);
                    int treeY = Integer.parseInt(coords[1]);
                    
                    // Try to generate the tree using deterministic logic
                    tree = server.getWorldState().generateTreeAt(treeX, treeY);
                }
            } catch (Exception e) {
                System.err.println("Failed to parse tree coordinates from targetId: " + targetId);
            }
            
            // If still null after generation attempt, it's a ghost tree
            if (tree == null) {
                // Log the ghost tree attempt and check if threshold exceeded
                boolean shouldContinue = logGhostTreeAttempt(targetId);
                
                if (!shouldContinue) {
                    // Client exceeded ghost tree attack limit, disconnect them
                    running = false;
                    return;
                }
                
                // Send tree removal message to client to remove the ghost tree
                TreeRemovalMessage removalMsg = new TreeRemovalMessage("server", targetId, 
                    "Tree does not exist in server world state");
                sendMessage(removalMsg);
                
                System.out.println("[GHOST_TREE] Sent TreeRemovalMessage to client " + clientId + " for tree " + targetId);
                return;
            }
        }
        
        // Check if tree was already destroyed
        if (!tree.isExists()) {
            // Tree already destroyed
            return;
        }
        
        // Validate tree position
        if (!isValidPosition(tree.getX(), tree.getY())) {
            System.err.println("Invalid tree position for " + targetId);
            return;
        }
        
        // Check attack range
        float dx = tree.getX() - playerState.getX();
        float dy = tree.getY() - playerState.getY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distance > ATTACK_RANGE) {
            System.out.println("Attack out of range from " + clientId + ": distance=" + distance);
            logSecurityViolation("Attack range check failed: distance=" + distance);
            return;
        }
        
        // Apply damage
        float newHealth = tree.getHealth() - damage;
        tree.setHealth(newHealth);
        
        if (newHealth <= 0) {
            // Tree destroyed
            server.getWorldState().removeTree(targetId);
            
            // Quantize positions to reduce message size
            float quantizedX = quantizePosition(tree.getX());
            float quantizedY = quantizePosition(tree.getY());
            
            TreeDestroyedMessage destroyMsg = new TreeDestroyedMessage("server", targetId, quantizedX, quantizedY);
            server.broadcastToAll(destroyMsg);
            
            // Spawn item(s) based on tree type
            if (tree.getType() == TreeType.APPLE || tree.getType() == TreeType.BANANA) {
                ItemType itemType = tree.getType() == TreeType.APPLE ? ItemType.APPLE : ItemType.BANANA;
                String itemId = UUID.randomUUID().toString();
                ItemState item = new ItemState(itemId, itemType, quantizedX, quantizedY, false);
                server.getWorldState().addOrUpdateItem(item);
                
                ItemSpawnMessage spawnMsg = new ItemSpawnMessage("server", itemId, itemType, quantizedX, quantizedY);
                server.broadcastToAll(spawnMsg);
                
                System.out.println("Item spawned: " + itemType + " at (" + quantizedX + ", " + quantizedY + ")");
            } else if (tree.getType() == TreeType.BAMBOO) {
                // Randomly choose drop pattern: 
                // 33% chance: 1 BambooStack + 1 BabyBamboo
                // 33% chance: 2 BambooStack
                // 33% chance: 2 BabyBamboo
                float dropRoll = (float) Math.random();
                
                if (dropRoll < 0.33f) {
                    // Drop 1 BambooStack + 1 BabyBamboo (original behavior)
                    String bambooStackId = UUID.randomUUID().toString();
                    ItemState bambooStack = new ItemState(bambooStackId, ItemType.BAMBOO_STACK, quantizedX, quantizedY, false);
                    server.getWorldState().addOrUpdateItem(bambooStack);
                    
                    ItemSpawnMessage bambooStackSpawnMsg = new ItemSpawnMessage("server", bambooStackId, ItemType.BAMBOO_STACK, quantizedX, quantizedY);
                    server.broadcastToAll(bambooStackSpawnMsg);
                    
                    String babyBambooId = UUID.randomUUID().toString();
                    float babyBambooX = quantizePosition(tree.getX() + 8);
                    ItemState babyBamboo = new ItemState(babyBambooId, ItemType.BABY_BAMBOO, babyBambooX, quantizedY, false);
                    server.getWorldState().addOrUpdateItem(babyBamboo);
                    
                    ItemSpawnMessage babyBambooSpawnMsg = new ItemSpawnMessage("server", babyBambooId, ItemType.BABY_BAMBOO, babyBambooX, quantizedY);
                    server.broadcastToAll(babyBambooSpawnMsg);
                    
                    System.out.println("Items spawned: BAMBOO_STACK at (" + quantizedX + ", " + quantizedY + "), BABY_BAMBOO at (" + babyBambooX + ", " + quantizedY + ")");
                } else if (dropRoll < 0.66f) {
                    // Drop 2 BambooStack
                    String bambooStackId1 = UUID.randomUUID().toString();
                    ItemState bambooStack1 = new ItemState(bambooStackId1, ItemType.BAMBOO_STACK, quantizedX, quantizedY, false);
                    server.getWorldState().addOrUpdateItem(bambooStack1);
                    
                    ItemSpawnMessage bambooStackSpawnMsg1 = new ItemSpawnMessage("server", bambooStackId1, ItemType.BAMBOO_STACK, quantizedX, quantizedY);
                    server.broadcastToAll(bambooStackSpawnMsg1);
                    
                    String bambooStackId2 = UUID.randomUUID().toString();
                    float bambooStack2X = quantizePosition(tree.getX() + 8);
                    ItemState bambooStack2 = new ItemState(bambooStackId2, ItemType.BAMBOO_STACK, bambooStack2X, quantizedY, false);
                    server.getWorldState().addOrUpdateItem(bambooStack2);
                    
                    ItemSpawnMessage bambooStackSpawnMsg2 = new ItemSpawnMessage("server", bambooStackId2, ItemType.BAMBOO_STACK, bambooStack2X, quantizedY);
                    server.broadcastToAll(bambooStackSpawnMsg2);
                    
                    System.out.println("Items spawned: 2x BAMBOO_STACK at (" + quantizedX + ", " + quantizedY + ")");
                } else {
                    // Drop 2 BabyBamboo
                    String babyBambooId1 = UUID.randomUUID().toString();
                    ItemState babyBamboo1 = new ItemState(babyBambooId1, ItemType.BABY_BAMBOO, quantizedX, quantizedY, false);
                    server.getWorldState().addOrUpdateItem(babyBamboo1);
                    
                    ItemSpawnMessage babyBambooSpawnMsg1 = new ItemSpawnMessage("server", babyBambooId1, ItemType.BABY_BAMBOO, quantizedX, quantizedY);
                    server.broadcastToAll(babyBambooSpawnMsg1);
                    
                    String babyBambooId2 = UUID.randomUUID().toString();
                    float babyBamboo2X = quantizePosition(tree.getX() + 8);
                    ItemState babyBamboo2 = new ItemState(babyBambooId2, ItemType.BABY_BAMBOO, babyBamboo2X, quantizedY, false);
                    server.getWorldState().addOrUpdateItem(babyBamboo2);
                    
                    ItemSpawnMessage babyBambooSpawnMsg2 = new ItemSpawnMessage("server", babyBambooId2, ItemType.BABY_BAMBOO, babyBamboo2X, quantizedY);
                    server.broadcastToAll(babyBambooSpawnMsg2);
                    
                    System.out.println("Items spawned: 2x BABY_BAMBOO at (" + quantizedX + ", " + quantizedY + ")");
                }
            } else if (tree.getType() == TreeType.SMALL) {
                // Spawn WoodStack at tree position
                String woodStackId = UUID.randomUUID().toString();
                ItemState woodStack = new ItemState(woodStackId, ItemType.WOOD_STACK, quantizedX, quantizedY, false);
                server.getWorldState().addOrUpdateItem(woodStack);
                
                ItemSpawnMessage woodStackSpawnMsg = new ItemSpawnMessage("server", woodStackId, ItemType.WOOD_STACK, quantizedX, quantizedY);
                server.broadcastToAll(woodStackSpawnMsg);
                
                System.out.println("Item spawned: WOOD_STACK at (" + quantizedX + ", " + quantizedY + ")");
            }
        } else {
            // Broadcast health update
            TreeHealthUpdateMessage healthMsg = new TreeHealthUpdateMessage("server", targetId, newHealth);
            server.broadcastToAll(healthMsg);
        }
    }
    
    /**
     * Handles a stone attack.
     * @param message The attack message
     * @param stone The stone being attacked
     */
    private void handleStoneAttack(AttackActionMessage message, StoneState stone) {
        String targetId = message.getTargetId();
        float damage = message.getDamage();
        
        // Validate stone position
        if (!isValidPosition(stone.getX(), stone.getY())) {
            System.err.println("Invalid stone position for " + targetId);
            return;
        }
        
        // Check attack range
        float dx = stone.getX() - playerState.getX();
        float dy = stone.getY() - playerState.getY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distance > ATTACK_RANGE) {
            System.out.println("Stone attack out of range from " + clientId + ": distance=" + distance);
            logSecurityViolation("Stone attack range check failed: distance=" + distance);
            return;
        }
        
        // Apply damage
        float newHealth = stone.getHealth() - damage;
        stone.setHealth(newHealth);
        
        if (newHealth <= 0) {
            // Stone destroyed
            server.getWorldState().removeStone(targetId);
            
            // Quantize positions to reduce message size
            float quantizedX = quantizePosition(stone.getX());
            float quantizedY = quantizePosition(stone.getY());
            
            StoneDestroyedMessage destroyMsg = new StoneDestroyedMessage("server", targetId, quantizedX, quantizedY);
            server.broadcastToAll(destroyMsg);
            
            // Spawn pebble at stone location
            String pebbleId = UUID.randomUUID().toString();
            ItemState pebble = new ItemState(pebbleId, ItemType.PEBBLE, quantizedX, quantizedY, false);
            server.getWorldState().addOrUpdateItem(pebble);
            
            ItemSpawnMessage spawnMsg = new ItemSpawnMessage("server", pebbleId, ItemType.PEBBLE, quantizedX, quantizedY);
            server.broadcastToAll(spawnMsg);
            
            System.out.println("Stone destroyed, pebble spawned at (" + quantizedX + ", " + quantizedY + ")");
        } else {
            // Broadcast health update
            StoneHealthUpdateMessage healthMsg = new StoneHealthUpdateMessage("server", targetId, newHealth);
            server.broadcastToAll(healthMsg);
        }
    }
    
    /**
     * Handles an item pickup message.
     * @param message The pickup message
     */
    private void handleItemPickup(ItemPickupMessage message) {
        // Validate message data
        if (message == null) {
            logSecurityViolation("Null pickup message");
            return;
        }
        
        String itemId = message.getItemId();
        
        // Validate item ID
        if (itemId == null || itemId.isEmpty()) {
            System.err.println("Invalid item ID from " + clientId);
            logSecurityViolation("Invalid item ID in pickup message");
            return;
        }
        
        ItemState item = server.getWorldState().getItems().get(itemId);
        
        if (item == null || item.isCollected()) {
            // Item doesn't exist or already collected
            return;
        }
        
        // Validate item position
        if (!isValidPosition(item.getX(), item.getY())) {
            System.err.println("Invalid item position for " + itemId);
            return;
        }
        
        // Validate pickup distance
        float dx = item.getX() - playerState.getX();
        float dy = item.getY() - playerState.getY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distance > PICKUP_RANGE) {
            System.out.println("Pickup out of range from " + clientId + ": distance=" + distance);
            logSecurityViolation("Pickup range check failed: distance=" + distance);
            return;
        }
        
        // Remove item
        server.getWorldState().removeItem(itemId);
        
        // Update player health (only for food items)
        float oldHealth = playerState.getHealth();
        if (item.getType() == ItemType.APPLE || item.getType() == ItemType.BANANA) {
            float healthRestore = 20; // Both restore 20 HP
            playerState.setHealth(Math.min(100, playerState.getHealth() + healthRestore));
            server.getWorldState().addOrUpdatePlayer(playerState);
            
            System.out.println("Player " + clientId + " picked up " + item.getType() + 
                             " (Health: " + oldHealth + " -> " + playerState.getHealth() + ")");
            
            // Broadcast health update
            PlayerHealthUpdateMessage healthMsg = new PlayerHealthUpdateMessage("server", clientId, playerState.getHealth());
            server.broadcastToAll(healthMsg);
        } else {
            // BambooStack or BabyBamboo - no health restoration
            System.out.println("Player " + clientId + " picked up " + item.getType());
        }
        
        // Broadcast pickup confirmation
        ItemPickupMessage pickupMsg = new ItemPickupMessage(clientId, itemId, clientId);
        server.broadcastToAll(pickupMsg);
    }
    
    /**
     * Handles a ping message by responding with a pong.
     * @param message The ping message
     */
    private void handlePing(PingMessage message) {
        // Respond with pong containing the original ping timestamp
        PongMessage pongMessage = new PongMessage("server", message.getTimestamp());
        sendMessage(pongMessage);
    }
    
    /**
     * Handles a player health update message.
     * @param message The health update message
     */
    private void handlePlayerHealthUpdate(PlayerHealthUpdateMessage message) {
        // Validate message data
        if (message == null) {
            logSecurityViolation("Null health update message");
            return;
        }
        
        // Validate health value
        float health = message.getHealth();
        if (health < 0 || health > 100 || Float.isNaN(health) || Float.isInfinite(health)) {
            System.err.println("Invalid health value from " + clientId + ": " + health);
            logSecurityViolation("Invalid health value: " + health);
            return;
        }
        
        // Update player state
        playerState.setHealth(health);
        server.getWorldState().addOrUpdatePlayer(playerState);
        
        // Broadcast health update to all clients
        server.broadcastToAll(message);
    }
    
    /**
     * Generates a cooldown key for tracking player attacks.
     * @param attackerId The ID of the attacking player
     * @param targetId The ID of the target player
     * @return A unique key in the format "attackerId:targetId"
     */
    private String getCooldownKey(String attackerId, String targetId) {
        return attackerId + ":" + targetId;
    }
    
    /**
     * Checks if a player attack is currently on cooldown.
     * @param attackerId The ID of the attacking player
     * @param targetId The ID of the target player
     * @return true if the attack is on cooldown, false otherwise
     */
    private boolean isPlayerAttackOnCooldown(String attackerId, String targetId) {
        String key = getCooldownKey(attackerId, targetId);
        Long lastAttackTime = playerAttackCooldowns.get(key);
        
        if (lastAttackTime == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastAttackTime) < PLAYER_ATTACK_COOLDOWN_MS;
    }
    
    /**
     * Updates the cooldown timestamp for a player attack.
     * @param attackerId The ID of the attacking player
     * @param targetId The ID of the target player
     */
    private void updatePlayerAttackCooldown(String attackerId, String targetId) {
        String key = getCooldownKey(attackerId, targetId);
        playerAttackCooldowns.put(key, System.currentTimeMillis());
    }
    
    /**
     * Handles a player-vs-player attack.
     * @param message The attack message
     */
    private void handlePlayerAttack(AttackActionMessage message) {
        String targetId = message.getTargetId();
        
        // Validate target player exists in world state
        PlayerState targetPlayer = server.getWorldState().getPlayers().get(targetId);
        if (targetPlayer == null) {
            System.err.println("Target player does not exist: " + targetId);
            logSecurityViolation("Attack on non-existent player: " + targetId);
            return;
        }
        
        // Check cooldown
        if (isPlayerAttackOnCooldown(clientId, targetId)) {
            System.out.println("Player attack on cooldown: " + clientId + " -> " + targetId);
            logSecurityViolation("Player attack on cooldown: " + clientId + " -> " + targetId);
            return;
        }
        
        // Calculate distance between attacker and target positions
        float dx = targetPlayer.getX() - playerState.getX();
        float dy = targetPlayer.getY() - playerState.getY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        // Validate distance is within ATTACK_RANGE (100 pixels)
        if (distance > ATTACK_RANGE) {
            System.out.println("Player attack out of range from " + clientId + ": distance=" + distance);
            logSecurityViolation("Player attack out of range: distance=" + distance);
            return;
        }
        
        // Apply 10 damage to target player's health
        float newHealth = targetPlayer.getHealth() - PLAYER_DAMAGE;
        
        // Clamp health to minimum of 0
        newHealth = Math.max(0, newHealth);
        
        // Update target player health in world state
        targetPlayer.setHealth(newHealth);
        server.getWorldState().addOrUpdatePlayer(targetPlayer);
        
        // Update cooldown timestamp
        updatePlayerAttackCooldown(clientId, targetId);
        
        // Broadcast PlayerHealthUpdateMessage to all clients
        PlayerHealthUpdateMessage healthMsg = new PlayerHealthUpdateMessage("server", targetId, newHealth);
        server.broadcastToAll(healthMsg);
        
        System.out.println("Player " + clientId + " attacked player " + targetId + 
                         " (Health: " + (newHealth + PLAYER_DAMAGE) + " -> " + newHealth + ")");
        
        // Check if target player died (health reached 0)
        if (newHealth <= 0) {
            // Respawn the target player at a random location
            respawnPlayer(targetPlayer);
        }
    }
    
    /**
     * Respawns a player at a random location with full health.
     * @param player The player to respawn
     */
    private void respawnPlayer(PlayerState player) {
        System.out.println("Player " + player.getPlayerId() + " died! Respawning...");
        
        // Reset health to 100
        player.setHealth(100);
        
        // Generate random respawn position (±1000 pixels from origin)
        java.util.Random random = new java.util.Random();
        float newX = (random.nextFloat() - 0.5f) * 2000; // ±1000px
        float newY = (random.nextFloat() - 0.5f) * 2000; // ±1000px
        
        // Update player position
        player.setX(newX);
        player.setY(newY);
        
        // Update in world state
        server.getWorldState().addOrUpdatePlayer(player);
        
        System.out.println("Player " + player.getPlayerId() + " respawned at (" + newX + ", " + newY + ") with full health");
        
        // Broadcast respawn to all clients (position update)
        PlayerMovementMessage respawnMsg = new PlayerMovementMessage(
            player.getPlayerId(), 
            newX, 
            newY, 
            player.getDirection(), 
            false
        );
        server.broadcastToAll(respawnMsg);
        
        // Broadcast health update (full health)
        PlayerHealthUpdateMessage healthMsg = new PlayerHealthUpdateMessage(
            "server", 
            player.getPlayerId(), 
            100
        );
        server.broadcastToAll(healthMsg);
    }
    
    /**
     * Sends a message to this client.
     * @param message The message to send
     */
    public void sendMessage(NetworkMessage message) {
        try {
            synchronized (output) {
                output.writeObject(message);
                output.flush();
                output.reset(); // Prevent memory leaks from object caching
            }
        } catch (IOException e) {
            System.err.println("Error sending message to " + clientId + ": " + e.getMessage());
            running = false;
        }
    }
    
    /**
     * Checks if the client connection is still alive.
     * @return true if the connection is alive, false otherwise
     */
    public boolean isAlive() {
        return running && !socket.isClosed() && 
               (System.currentTimeMillis() - lastHeartbeat < CLIENT_TIMEOUT);
    }
    
    /**
     * Closes the client connection and cleans up resources.
     */
    public void close() {
        running = false;
        cleanup();
    }
    
    /**
     * Validates that a position has valid numeric values.
     * @param x The x coordinate
     * @param y The y coordinate
     * @return true if the position is valid, false otherwise
     */
    private boolean isValidPosition(float x, float y) {
        return !Float.isNaN(x) && !Float.isInfinite(x) &&
               !Float.isNaN(y) && !Float.isInfinite(y);
    }
    
    /**
     * Quantizes a position value to the nearest pixel.
     * This reduces floating point precision and minimizes message size.
     * @param position The position value to quantize
     * @return The quantized position rounded to the nearest pixel
     */
    private float quantizePosition(float position) {
        return Math.round(position);
    }
    
    /**
     * Validates the size of a message.
     * @param message The message to validate
     * @return true if the message size is acceptable, false otherwise
     */
    private boolean validateMessageSize(NetworkMessage message) {
        try {
            // Estimate message size using serialization
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(message);
            oos.flush();
            int size = baos.size();
            oos.close();
            
            return size <= MAX_MESSAGE_SIZE;
        } catch (IOException e) {
            System.err.println("Error validating message size: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Logs a security violation for this client.
     * @param violation Description of the violation
     */
    private void logSecurityViolation(String violation) {
        System.err.println("[SECURITY] Client " + clientId + " - " + violation + 
                         " from " + socket.getInetAddress());
    }
    
    /**
     * Handles an inventory update message from the client.
     * Validates and updates the player's inventory state on the server.
     * @param message The inventory update message
     */
    private void handleInventoryUpdate(InventoryUpdateMessage message) {
        // Validate message data
        if (message == null) {
            logSecurityViolation("Null inventory update message");
            return;
        }
        
        // Validate inventory counts (must be non-negative and reasonable)
        if (!isValidInventoryCount(message.getAppleCount()) ||
            !isValidInventoryCount(message.getBananaCount()) ||
            !isValidInventoryCount(message.getBabyBambooCount()) ||
            !isValidInventoryCount(message.getBambooStackCount()) ||
            !isValidInventoryCount(message.getWoodStackCount())) {
            System.err.println("Invalid inventory counts from " + clientId);
            logSecurityViolation("Invalid inventory counts");
            return;
        }
        
        // Update player state with new inventory
        playerState.setAppleCount(message.getAppleCount());
        playerState.setBananaCount(message.getBananaCount());
        playerState.setBabyBambooCount(message.getBabyBambooCount());
        playerState.setBambooStackCount(message.getBambooStackCount());
        playerState.setWoodStackCount(message.getWoodStackCount());
        
        // Update in world state
        server.getWorldState().addOrUpdatePlayer(playerState);
        
        System.out.println("Inventory updated for player " + clientId + 
                         ": Apples=" + message.getAppleCount() +
                         ", Bananas=" + message.getBananaCount() +
                         ", BabyBamboo=" + message.getBabyBambooCount() +
                         ", BambooStack=" + message.getBambooStackCount() +
                         ", WoodStack=" + message.getWoodStackCount());
    }
    
    /**
     * Validates an inventory count value.
     * @param count The count to validate
     * @return true if the count is valid, false otherwise
     */
    private boolean isValidInventoryCount(int count) {
        // Counts must be non-negative and less than 10000 (reasonable limit)
        return count >= 0 && count < 10000;
    }
    
    /**
     * Sends an inventory sync message to the client with the authoritative server state.
     */
    private void sendInventorySync() {
        InventorySyncMessage syncMsg = new InventorySyncMessage(
            "server",
            clientId,
            playerState.getAppleCount(),
            playerState.getBananaCount(),
            playerState.getBabyBambooCount(),
            playerState.getBambooStackCount(),
            playerState.getWoodStackCount(),
            playerState.getPebbleCount()
        );
        sendMessage(syncMsg);
    }
    
    /**
     * Handles a bamboo plant message.
     * Validates the planting action and broadcasts to all clients.
     * @param message The bamboo plant message
     */
    private void handleBambooPlant(BambooPlantMessage message) {
        // Validate message data
        if (message == null) {
            logSecurityViolation("Null bamboo plant message");
            return;
        }
        
        String plantedBambooId = message.getPlantedBambooId();
        float x = message.getX();
        float y = message.getY();
        
        // Validate planted bamboo ID
        if (plantedBambooId == null || plantedBambooId.isEmpty()) {
            System.err.println("Invalid planted bamboo ID from " + clientId);
            logSecurityViolation("Invalid planted bamboo ID");
            return;
        }
        
        // Validate position
        if (!isValidPosition(x, y)) {
            System.err.println("Invalid bamboo plant position from " + clientId);
            logSecurityViolation("Invalid bamboo plant position");
            return;
        }
        
        // Validate planting distance (player must be near the planting location)
        float dx = x - playerState.getX();
        float dy = y - playerState.getY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distance > 128) { // Allow planting within 128 pixels
            System.out.println("Bamboo plant out of range from " + clientId + ": distance=" + distance);
            logSecurityViolation("Bamboo plant range check failed: distance=" + distance);
            return;
        }
        
        System.out.println("Player " + clientId + " planted bamboo at (" + x + ", " + y + ")");
        
        // Broadcast to all clients (including sender for confirmation)
        server.broadcastToAll(message);
    }
    
    /**
     * Handles a bamboo transform message.
     * This is sent by clients when a planted bamboo matures into a tree.
     * @param message The bamboo transform message
     */
    private void handleBambooTransform(BambooTransformMessage message) {
        // Validate message data
        if (message == null) {
            logSecurityViolation("Null bamboo transform message");
            return;
        }
        
        String plantedBambooId = message.getPlantedBambooId();
        String bambooTreeId = message.getBambooTreeId();
        float x = message.getX();
        float y = message.getY();
        
        // Validate IDs
        if (plantedBambooId == null || plantedBambooId.isEmpty() ||
            bambooTreeId == null || bambooTreeId.isEmpty()) {
            System.err.println("Invalid bamboo transform IDs from " + clientId);
            logSecurityViolation("Invalid bamboo transform IDs");
            return;
        }
        
        // Validate position
        if (!isValidPosition(x, y)) {
            System.err.println("Invalid bamboo transform position from " + clientId);
            logSecurityViolation("Invalid bamboo transform position");
            return;
        }
        
        // Create bamboo tree in world state
        TreeState bambooTree = new TreeState(bambooTreeId, TreeType.BAMBOO, x, y, 100.0f, true);
        server.getWorldState().addOrUpdateTree(bambooTree);
        
        System.out.println("Bamboo transformed: " + plantedBambooId + " -> " + bambooTreeId + " at (" + x + ", " + y + ")");
        
        // Broadcast to all clients
        server.broadcastToAll(message);
    }
    
    /**
     * Logs a ghost tree attack attempt and tracks repeated attempts.
     * If the client exceeds the maximum allowed ghost tree attacks, the connection is terminated.
     * @param treeId The position key of the ghost tree
     * @return true if the connection should continue, false if it should be terminated
     */
    private boolean logGhostTreeAttempt(String treeId) {
        int count = ghostTreeAttempts.getOrDefault(treeId, 0) + 1;
        ghostTreeAttempts.put(treeId, count);
        
        System.err.println("[GHOST_TREE] Client " + clientId + 
                          " attacked non-existent tree " + treeId + 
                          " (attempt #" + count + ")");
        
        // Calculate total ghost tree attacks across all positions
        int totalAttempts = ghostTreeAttempts.values().stream().mapToInt(Integer::intValue).sum();
        
        if (totalAttempts > MAX_GHOST_TREE_ATTACKS) {
            System.err.println("[SECURITY] Client " + clientId + 
                             " exceeded ghost tree attack limit (" + totalAttempts + " total attempts)");
            logSecurityViolation("Exceeded ghost tree attack limit: " + totalAttempts + " attempts");
            return false; // Signal to terminate connection
        }
        
        return true; // Connection should continue
    }
    
    /**
     * Cleans up connection resources.
     */
    private void cleanup() {
        running = false;
        
        // Remove player from world state
        server.getWorldState().removePlayer(clientId);
        
        // Notify other clients
        PlayerLeaveMessage leaveMsg = new PlayerLeaveMessage(clientId, playerState.getPlayerName());
        server.broadcastToAllExcept(leaveMsg, clientId);
        
        // Close streams and socket
        try {
            if (input != null) input.close();
        } catch (IOException e) {
            // Ignore
        }
        
        try {
            if (output != null) output.close();
        } catch (IOException e) {
            // Ignore
        }
        
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // Ignore
        }
        
        // Remove from server's client list
        server.disconnectClient(clientId);
    }
    
    /**
     * Gets the client ID.
     * @return The client ID
     */
    public String getClientId() {
        return clientId;
    }
    
    /**
     * Gets the player state for this client.
     * @return The player state
     */
    public PlayerState getPlayerState() {
        return playerState;
    }
}
