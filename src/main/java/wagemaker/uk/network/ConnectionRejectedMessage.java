package wagemaker.uk.network;

/**
 * Message sent by server to client when connection is rejected.
 */
public class ConnectionRejectedMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String reason;
    
    public ConnectionRejectedMessage() {
        super();
    }
    
    public ConnectionRejectedMessage(String senderId, String reason) {
        super(senderId);
        this.reason = reason;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.CONNECTION_REJECTED;
    }
    
    public String getReason() {
        return reason;
    }
}
