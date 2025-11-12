package wagemaker.uk.network;

/**
 * Enum representing all types of network messages that can be sent between client and server.
 */
public enum MessageType {
    PLAYER_MOVEMENT,
    PLAYER_JOIN,
    PLAYER_LEAVE,
    WORLD_STATE,
    WORLD_STATE_UPDATE,
    TREE_HEALTH_UPDATE,
    TREE_DESTROYED,
    TREE_REMOVAL,
    ITEM_SPAWN,
    ITEM_PICKUP,
    PLAYER_HEALTH_UPDATE,
    ATTACK_ACTION,
    CONNECTION_ACCEPTED,
    CONNECTION_REJECTED,
    HEARTBEAT,
    POSITION_CORRECTION,
    PING,
    PONG,
    INVENTORY_UPDATE,
    INVENTORY_SYNC
}
