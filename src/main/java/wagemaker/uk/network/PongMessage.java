package wagemaker.uk.network;

/**
 * Message sent by the server in response to a PingMessage.
 * Contains the original ping timestamp to allow the client to calculate round-trip time.
 */
public class PongMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private long pingTimestamp;
    
    /**
     * Creates a new pong message.
     * @param senderId The server ID
     * @param pingTimestamp The timestamp from the original ping message
     */
    public PongMessage(String senderId, long pingTimestamp) {
        super(senderId);
        this.pingTimestamp = pingTimestamp;
    }
    
    /**
     * Gets the original ping timestamp.
     * @return The ping timestamp in milliseconds
     */
    public long getPingTimestamp() {
        return pingTimestamp;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.PONG;
    }
}
