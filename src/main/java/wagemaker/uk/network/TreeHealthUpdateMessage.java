package wagemaker.uk.network;

/**
 * Message sent when a tree's health changes due to player attacks.
 */
public class TreeHealthUpdateMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String treeId;
    private float health;
    
    public TreeHealthUpdateMessage() {
        super();
    }
    
    public TreeHealthUpdateMessage(String senderId, String treeId, float health) {
        super(senderId);
        this.treeId = treeId;
        this.health = health;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.TREE_HEALTH_UPDATE;
    }
    
    public String getTreeId() {
        return treeId;
    }
    
    public float getHealth() {
        return health;
    }
}
