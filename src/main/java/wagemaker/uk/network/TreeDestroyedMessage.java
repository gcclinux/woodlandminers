package wagemaker.uk.network;

/**
 * Message broadcast when a tree is destroyed (health reaches zero).
 */
public class TreeDestroyedMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String treeId;
    private float x;
    private float y;
    
    public TreeDestroyedMessage() {
        super();
    }
    
    public TreeDestroyedMessage(String senderId, String treeId, float x, float y) {
        super(senderId);
        this.treeId = treeId;
        this.x = x;
        this.y = y;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.TREE_DESTROYED;
    }
    
    public String getTreeId() {
        return treeId;
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
}
