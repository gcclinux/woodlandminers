package wagemaker.uk.network;

import wagemaker.uk.respawn.RespawnEntry;
import java.util.ArrayList;
import java.util.List;

/**
 * Message sent to synchronize full respawn state with clients.
 * Typically sent when a client joins an ongoing multiplayer session
 * to ensure they have all pending respawn timers.
 */
public class RespawnStateMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private List<RespawnEntry> pendingRespawns;
    
    /**
     * Default constructor for serialization.
     */
    public RespawnStateMessage() {
        super();
        this.pendingRespawns = new ArrayList<>();
    }
    
    /**
     * Constructor for creating a respawn state message.
     * 
     * @param senderId The sender's unique identifier (typically server ID)
     * @param pendingRespawns List of all pending respawn entries
     */
    public RespawnStateMessage(String senderId, List<RespawnEntry> pendingRespawns) {
        super(senderId);
        this.pendingRespawns = pendingRespawns != null ? new ArrayList<>(pendingRespawns) : new ArrayList<>();
    }
    
    @Override
    public MessageType getType() {
        return MessageType.RESPAWN_STATE;
    }
    
    public List<RespawnEntry> getPendingRespawns() {
        return pendingRespawns;
    }
    
    public void setPendingRespawns(List<RespawnEntry> pendingRespawns) {
        this.pendingRespawns = pendingRespawns != null ? new ArrayList<>(pendingRespawns) : new ArrayList<>();
    }
    
    @Override
    public String toString() {
        return "RespawnStateMessage{" +
                "pendingRespawnsCount=" + (pendingRespawns != null ? pendingRespawns.size() : 0) +
                ", senderId='" + senderId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
