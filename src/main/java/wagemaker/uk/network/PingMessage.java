package wagemaker.uk.network;

/**
 * Message sent by the client to measure network latency.
 * The server responds with a PongMessage containing the same timestamp.
 */
public class PingMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new ping message.
     * @param senderId The client ID sending the ping
     */
    public PingMessage(String senderId) {
        super(senderId);
    }
    
    @Override
    public MessageType getType() {
        return MessageType.PING;
    }
}
