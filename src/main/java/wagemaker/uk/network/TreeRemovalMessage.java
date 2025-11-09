package wagemaker.uk.network;

import java.io.Serializable;

/**
 * Message sent by the server to clients when a ghost tree needs to be removed.
 * A ghost tree is a tree that exists in the client's local world state but not
 * in the server's authoritative world state.
 */
public class TreeRemovalMessage extends NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String treeId;
    private String reason;
    
    /**
     * Default constructor for serialization.
     */
    public TreeRemovalMessage() {
        super();
    }
    
    /**
     * Creates a new tree removal message.
     * @param senderId The unique identifier of the sender (typically "server")
     * @param treeId The position key of the tree to remove (format: "x,y")
     * @param reason Diagnostic reason for the removal
     */
    public TreeRemovalMessage(String senderId, String treeId, String reason) {
        super(senderId);
        this.treeId = treeId;
        this.reason = reason;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.TREE_REMOVAL;
    }
    
    /**
     * Gets the tree ID (position key) of the tree to remove.
     * @return The tree ID in format "x,y"
     */
    public String getTreeId() {
        return treeId;
    }
    
    /**
     * Gets the diagnostic reason for removing this tree.
     * @return The reason string for logging purposes
     */
    public String getReason() {
        return reason;
    }
}
