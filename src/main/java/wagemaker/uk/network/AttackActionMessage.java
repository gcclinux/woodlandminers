package wagemaker.uk.network;

/**
 * Message sent when a player attacks a tree.
 */
public class AttackActionMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private String targetId;
    private float damage;
    
    public AttackActionMessage() {
        super();
        this.damage = 10.0f; // Default damage
    }
    
    public AttackActionMessage(String senderId, String playerId, String targetId) {
        super(senderId);
        this.playerId = playerId;
        this.targetId = targetId;
        this.damage = 10.0f; // Default damage
    }
    
    public AttackActionMessage(String senderId, String playerId, String targetId, float damage) {
        super(senderId);
        this.playerId = playerId;
        this.targetId = targetId;
        this.damage = damage;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.ATTACK_ACTION;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public String getTargetId() {
        return targetId;
    }
    
    public float getDamage() {
        return damage;
    }
}
