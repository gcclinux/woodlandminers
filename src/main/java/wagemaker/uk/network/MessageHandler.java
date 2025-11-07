package wagemaker.uk.network;

/**
 * Interface for handling incoming network messages on the client side.
 * Implementations should process different message types and update game state accordingly.
 */
public interface MessageHandler {
    /**
     * Handles an incoming network message.
     * @param message The message to handle
     */
    void handleMessage(NetworkMessage message);
}
