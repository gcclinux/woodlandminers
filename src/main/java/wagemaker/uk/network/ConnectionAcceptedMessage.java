package wagemaker.uk.network;

/**
 * Message sent by server to client when connection is accepted.
 * Includes server configuration such as the maximum planting range.
 */
public class ConnectionAcceptedMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String assignedClientId;
    private String welcomeMessage;
    private int plantingMaxRange;
    
    public ConnectionAcceptedMessage() {
        super();
        this.plantingMaxRange = -1; // Default to unlimited for backward compatibility
    }
    
    public ConnectionAcceptedMessage(String senderId, String assignedClientId, String welcomeMessage) {
        super(senderId);
        this.assignedClientId = assignedClientId;
        this.welcomeMessage = welcomeMessage;
        this.plantingMaxRange = -1; // Default to unlimited for backward compatibility
    }
    
    public ConnectionAcceptedMessage(String senderId, String assignedClientId, String welcomeMessage, int plantingMaxRange) {
        super(senderId);
        this.assignedClientId = assignedClientId;
        this.welcomeMessage = welcomeMessage;
        this.plantingMaxRange = plantingMaxRange;
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
    
    public int getPlantingMaxRange() {
        return plantingMaxRange;
    }
    
    public void setPlantingMaxRange(int plantingMaxRange) {
        this.plantingMaxRange = plantingMaxRange;
    }
}
