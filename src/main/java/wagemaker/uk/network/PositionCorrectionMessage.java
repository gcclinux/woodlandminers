package wagemaker.uk.network;

/**
 * Message sent by the server to correct a client's position when desynchronization is detected.
 * The client should smoothly interpolate to the corrected position.
 */
public class PositionCorrectionMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private float correctedX;
    private float correctedY;
    private Direction correctedDirection;
    private String reason;
    
    public PositionCorrectionMessage() {
        super();
    }
    
    public PositionCorrectionMessage(String senderId, String playerId, float correctedX, float correctedY, Direction correctedDirection, String reason) {
        super(senderId);
        this.playerId = playerId;
        this.correctedX = correctedX;
        this.correctedY = correctedY;
        this.correctedDirection = correctedDirection;
        this.reason = reason;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.POSITION_CORRECTION;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
    public float getCorrectedX() {
        return correctedX;
    }
    
    public void setCorrectedX(float correctedX) {
        this.correctedX = correctedX;
    }
    
    public float getCorrectedY() {
        return correctedY;
    }
    
    public void setCorrectedY(float correctedY) {
        this.correctedY = correctedY;
    }
    
    public Direction getCorrectedDirection() {
        return correctedDirection;
    }
    
    public void setCorrectedDirection(Direction correctedDirection) {
        this.correctedDirection = correctedDirection;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}
