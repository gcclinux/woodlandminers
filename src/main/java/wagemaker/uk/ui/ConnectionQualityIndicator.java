package wagemaker.uk.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import wagemaker.uk.network.GameClient;

/**
 * Displays connection quality information including latency and connection status.
 * Shows a colored indicator (green/yellow/red) based on latency thresholds.
 */
public class ConnectionQualityIndicator {
    private static final long GOOD_LATENCY_THRESHOLD = 100; // ms
    private static final long FAIR_LATENCY_THRESHOLD = 250; // ms
    
    private static final Color GOOD_COLOR = new Color(0.2f, 0.8f, 0.2f, 1.0f); // Green
    private static final Color FAIR_COLOR = new Color(0.9f, 0.9f, 0.2f, 1.0f); // Yellow
    private static final Color POOR_COLOR = new Color(0.9f, 0.2f, 0.2f, 1.0f); // Red
    private static final Color DISCONNECTED_COLOR = new Color(0.5f, 0.5f, 0.5f, 1.0f); // Gray
    
    private GameClient gameClient;
    private BitmapFont font;
    
    /**
     * Creates a new ConnectionQualityIndicator.
     * @param gameClient The game client to monitor
     * @param font The font to use for text rendering
     */
    public ConnectionQualityIndicator(GameClient gameClient, BitmapFont font) {
        this.gameClient = gameClient;
        this.font = font;
    }
    
    /**
     * Sets the game client to monitor.
     * @param gameClient The game client
     */
    public void setGameClient(GameClient gameClient) {
        this.gameClient = gameClient;
    }
    
    /**
     * Renders the connection quality indicator on the HUD.
     * @param batch The sprite batch for text rendering
     * @param shapeRenderer The shape renderer for indicator circle
     * @param screenX The X position on screen (top-right corner)
     * @param screenY The Y position on screen (top-right corner)
     */
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, float screenX, float screenY) {
        if (gameClient == null) {
            return;
        }
        
        boolean isConnected = gameClient.isConnected();
        long latency = gameClient.getAverageLatency();
        
        // Determine color based on connection status and latency
        Color indicatorColor;
        String statusText;
        
        if (!isConnected) {
            indicatorColor = DISCONNECTED_COLOR;
            statusText = "Connection Lost";
        } else if (latency == 0) {
            // No latency data yet
            indicatorColor = GOOD_COLOR;
            statusText = "Connecting...";
        } else if (latency <= GOOD_LATENCY_THRESHOLD) {
            indicatorColor = GOOD_COLOR;
            statusText = latency + " ms";
        } else if (latency <= FAIR_LATENCY_THRESHOLD) {
            indicatorColor = FAIR_COLOR;
            statusText = latency + " ms";
        } else {
            indicatorColor = POOR_COLOR;
            statusText = latency + " ms";
        }
        
        // Draw indicator circle
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(indicatorColor);
        shapeRenderer.circle(screenX - 15, screenY - 15, 8);
        shapeRenderer.end();
        
        // Draw status text
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, statusText, screenX - 80, screenY - 10);
        batch.end();
    }
    
    /**
     * Gets the color for the current connection quality.
     * @return The color representing connection quality
     */
    public Color getQualityColor() {
        if (gameClient == null || !gameClient.isConnected()) {
            return DISCONNECTED_COLOR;
        }
        
        long latency = gameClient.getAverageLatency();
        
        if (latency == 0 || latency <= GOOD_LATENCY_THRESHOLD) {
            return GOOD_COLOR;
        } else if (latency <= FAIR_LATENCY_THRESHOLD) {
            return FAIR_COLOR;
        } else {
            return POOR_COLOR;
        }
    }
    
    /**
     * Gets the current latency text.
     * @return The latency text to display
     */
    public String getLatencyText() {
        if (gameClient == null) {
            return "";
        }
        
        if (!gameClient.isConnected()) {
            return "Connection Lost";
        }
        
        long latency = gameClient.getAverageLatency();
        
        if (latency == 0) {
            return "Connecting...";
        }
        
        return latency + " ms";
    }
}
