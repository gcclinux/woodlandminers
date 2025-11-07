package wagemaker.uk.network;

/**
 * Message sent by server to client when connection is accepted.
 */
public class ConnectionAcceptedMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String assignedClientId;
    private String welcomeMessage;
    
    public ConnectionAcceptedMessage() {
        super();
    }
    
    public ConnectionAcceptedMessage(String senderId, String assignedClientId, String welcomeMessage) {
        super(senderId);
        this.assignedClientId = assignedClientId;
        this.welcomeMessage = welcomeMessage;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.CONNECTION_ACCEPTED;
    }
    
    public String getAssignedClientId() {
        return assignedClientId;
    }
    
    public String getWelcomeMessage() {
        return welcomeMessage;
    }
}
