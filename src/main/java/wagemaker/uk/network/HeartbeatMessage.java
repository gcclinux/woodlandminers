package wagemaker.uk.network;

/**
 * Message sent periodically to maintain connection and detect timeouts.
 */
public class HeartbeatMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    public HeartbeatMessage() {
        super();
    }
    
    public HeartbeatMessage(String senderId) {
        super(senderId);
    }
    
    @Override
    public MessageType getType() {
        return MessageType.HEARTBEAT;
    }
}
