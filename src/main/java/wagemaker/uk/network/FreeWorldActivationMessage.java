package wagemaker.uk.network;

/**
 * Message to broadcast Free World mode activation from host to all clients.
 */
public class FreeWorldActivationMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    private boolean activated;
    
    public FreeWorldActivationMessage() {
        super("server");
    }
    
    public FreeWorldActivationMessage(boolean activated) {
        super("server");
        this.activated = activated;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.FREE_WORLD_ACTIVATION;
    }
    
    public boolean isActivated() {
        return activated;
    }
}
