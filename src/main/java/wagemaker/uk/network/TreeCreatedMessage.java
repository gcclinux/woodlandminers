package wagemaker.uk.network;

public class TreeCreatedMessage extends NetworkMessage {
    private String treeId;
    private TreeType treeType;
    private float x;
    private float y;
    private float health;
    
    public TreeCreatedMessage(String senderId, String treeId, TreeType treeType, float x, float y, float health) {
        super(senderId);
        this.treeId = treeId;
        this.treeType = treeType;
        this.x = x;
        this.y = y;
        this.health = health;
    }
    
    public String getTreeId() {
        return treeId;
    }
    
    public TreeType getTreeType() {
        return treeType;
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    
    public float getHealth() {
        return health;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.TREE_CREATED;
    }
}
