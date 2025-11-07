package wagemaker.uk.network;

import java.io.Serializable;

/**
 * Abstract base class for all network messages exchanged between client and server.
 * All messages are serializable for transmission over network streams.
 */
public abstract class NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    protected long timestamp;
    protected String senderId;
    
    /**
     * Creates a new network message with current timestamp.
     */
    public NetworkMessage() {
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Creates a new network message with specified sender ID.
     * @param senderId The unique identifier of the sender
     */
    public NetworkMessage(String senderId) {
        this();
        this.senderId = senderId;
    }
    
    /**
     * Gets the type of this message.
     * @return The message type
     */
    public abstract MessageType getType();
    
    /**
     * Gets the timestamp when this message was created.
     * @return The timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Gets the sender ID of this message.
     * @return The sender's unique identifier
     */
    public String getSenderId() {
        return senderId;
    }
    
    /**
     * Sets the sender ID of this message.
     * @param senderId The sender's unique identifier
     */
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
}
