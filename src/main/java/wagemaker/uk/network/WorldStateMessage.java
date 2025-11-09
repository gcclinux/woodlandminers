package wagemaker.uk.network;

import wagemaker.uk.weather.RainZone;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Message containing the complete world state snapshot.
 * Sent to clients when they first connect to synchronize the world.
 */
public class WorldStateMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private long worldSeed;
    private Map<String, PlayerState> players;
    private Map<String, TreeState> trees;
    private Map<String, ItemState> items;
    private Set<String> clearedPositions;
    private List<RainZone> rainZones;
    
    public WorldStateMessage() {
        super();
    }
    
    public WorldStateMessage(String senderId, long worldSeed, 
                            Map<String, PlayerState> players,
                            Map<String, TreeState> trees,
                            Map<String, ItemState> items,
                            Set<String> clearedPositions,
                            List<RainZone> rainZones) {
        super(senderId);
        this.worldSeed = worldSeed;
        this.players = players;
        this.trees = trees;
        this.items = items;
        this.clearedPositions = clearedPositions;
        this.rainZones = rainZones;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.WORLD_STATE;
    }
    
    public long getWorldSeed() {
        return worldSeed;
    }
    
    public Map<String, PlayerState> getPlayers() {
        return players;
    }
    
    public Map<String, TreeState> getTrees() {
        return trees;
    }
    
    public Map<String, ItemState> getItems() {
        return items;
    }
    
    public Set<String> getClearedPositions() {
        return clearedPositions;
    }
    
    public List<RainZone> getRainZones() {
        return rainZones;
    }
}
