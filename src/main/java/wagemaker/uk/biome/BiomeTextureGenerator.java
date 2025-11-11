package wagemaker.uk.biome;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import java.util.Random;

/**
 * Generates procedural textures for different biome types.
 * This class creates realistic, tileable textures for grass, sand, and other ground types.
 */
public class BiomeTextureGenerator {
    
    /**
     * Generates a realistic grass texture with natural variation.
     * Extracted from MyGdxGame.createRealisticGrassTexture() for reusability.
     * 
     * @return A 64x64 tileable grass texture
     */
    public Texture generateGrassTexture() {
        Pixmap grassPixmap = new Pixmap(BiomeConfig.TEXTURE_SIZE, BiomeConfig.TEXTURE_SIZE, Pixmap.Format.RGBA8888);
        Random grassRandom = new Random(BiomeConfig.TEXTURE_SEED_GRASS);
        
        // Base grass colors from config
        float[] baseGreen = BiomeConfig.GRASS_BASE_COLOR;
        float[] lightGreen = BiomeConfig.GRASS_LIGHT_COLOR;
        float[] mediumGreen = {0.2f, 0.55f, 0.12f, 1.0f}; // Medium green
        float[] brownish = {0.3f, 0.4f, 0.1f, 1.0f}; // Brownish green (dirt patches)
        
        // Fill with base grass color
        grassPixmap.setColor(baseGreen[0], baseGreen[1], baseGreen[2], baseGreen[3]);
        grassPixmap.fill();
        
        // Add grass blade patterns and texture variations
        for (int x = 0; x < BiomeConfig.TEXTURE_SIZE; x++) {
            for (int y = 0; y < BiomeConfig.TEXTURE_SIZE; y++) {
                float noise = grassRandom.nextFloat();
                
                // Create grass blade patterns (vertical lines with variations)
                if (x % 3 == 0 && noise > 0.3f) {
                    // Lighter grass blades
                    grassPixmap.setColor(lightGreen[0], lightGreen[1], lightGreen[2], lightGreen[3]);
                    grassPixmap.drawPixel(x, y);
                    if (y > 0 && grassRandom.nextFloat() > 0.5f) {
                        grassPixmap.drawPixel(x, y - 1); // Extend blade
                    }
                } else if (x % 4 == 1 && noise > 0.6f) {
                    // Medium grass blades
                    grassPixmap.setColor(mediumGreen[0], mediumGreen[1], mediumGreen[2], mediumGreen[3]);
                    grassPixmap.drawPixel(x, y);
                } else if (noise > 0.85f) {
                    // Random dirt/brown patches for realism
                    grassPixmap.setColor(brownish[0], brownish[1], brownish[2], brownish[3]);
                    grassPixmap.drawPixel(x, y);
                } else if (noise > 0.75f) {
                    // Darker grass areas (shadows)
                    grassPixmap.setColor(baseGreen[0] * 0.8f, baseGreen[1] * 0.8f, baseGreen[2] * 0.8f, baseGreen[3]);
                    grassPixmap.drawPixel(x, y);
                }
            }
        }
        
        // Add some scattered small details (seeds, small stones, etc.)
        for (int i = 0; i < 8; i++) {
            int x = grassRandom.nextInt(BiomeConfig.TEXTURE_SIZE);
            int y = grassRandom.nextInt(BiomeConfig.TEXTURE_SIZE);
            
            if (grassRandom.nextFloat() > 0.5f) {
                // Small brown spots (seeds/dirt)
                grassPixmap.setColor(0.4f, 0.3f, 0.2f, 1.0f);
                grassPixmap.drawPixel(x, y);
            } else {
                // Tiny light spots (small flowers/highlights)
                grassPixmap.setColor(0.6f, 0.8f, 0.3f, 1.0f);
                grassPixmap.drawPixel(x, y);
            }
        }
        
        // Add some diagonal grass patterns for more natural look
        for (int i = 0; i < BiomeConfig.TEXTURE_SIZE; i += 8) {
            for (int j = 0; j < BiomeConfig.TEXTURE_SIZE; j += 6) {
                if (grassRandom.nextFloat() > 0.4f) {
                    // Diagonal grass blade pattern
                    grassPixmap.setColor(lightGreen[0], lightGreen[1], lightGreen[2], lightGreen[3]);
                    if (i + 1 < BiomeConfig.TEXTURE_SIZE && j + 1 < BiomeConfig.TEXTURE_SIZE) {
                        grassPixmap.drawPixel(i, j);
                        grassPixmap.drawPixel(i + 1, j + 1);
                    }
                }
            }
        }
        
        Texture texture = createTextureFromPixmap(grassPixmap);
        grassPixmap.dispose();
        return texture;
    }
    
    /**
     * Generates a realistic sand texture with natural variation.
     * Creates sandy beige colors with grain, spots, and highlights.
     * 
     * @return A 64x64 tileable sand texture
     */
    public Texture generateSandTexture() {
        Pixmap sandPixmap = new Pixmap(BiomeConfig.TEXTURE_SIZE, BiomeConfig.TEXTURE_SIZE, Pixmap.Format.RGBA8888);
        Random sandRandom = new Random(BiomeConfig.TEXTURE_SEED_SAND);
        
        // Sand colors from config
        float[] baseColor = BiomeConfig.SAND_BASE_COLOR;
        float[] lightColor = BiomeConfig.SAND_LIGHT_COLOR;
        float[] darkColor = BiomeConfig.SAND_DARK_COLOR;
        
        // Fill with base sand color
        sandPixmap.setColor(baseColor[0], baseColor[1], baseColor[2], baseColor[3]);
        sandPixmap.fill();
        
        // Add natural variation patterns
        addSandGrain(sandPixmap, sandRandom, baseColor, darkColor);
        addSandSpots(sandPixmap, sandRandom, darkColor);
        addSandHighlights(sandPixmap, sandRandom, lightColor);
        
        Texture texture = createTextureFromPixmap(sandPixmap);
        sandPixmap.dispose();
        return texture;
    }
    
    /**
     * Adds fine grain texture to sand for realism.
     * Creates subtle variations in color across the texture.
     * 
     * @param pixmap The pixmap to modify
     * @param random Random number generator for variation
     * @param baseColor Base sand color
     * @param darkColor Darker sand color for grain
     */
    private void addSandGrain(Pixmap pixmap, Random random, float[] baseColor, float[] darkColor) {
        for (int x = 0; x < BiomeConfig.TEXTURE_SIZE; x++) {
            for (int y = 0; y < BiomeConfig.TEXTURE_SIZE; y++) {
                float noise = random.nextFloat();
                
                // Add subtle grain variation
                if (noise > 0.7f) {
                    // Slightly darker grain
                    float factor = 0.95f + random.nextFloat() * 0.05f;
                    pixmap.setColor(
                        baseColor[0] * factor,
                        baseColor[1] * factor,
                        baseColor[2] * factor,
                        baseColor[3]
                    );
                    pixmap.drawPixel(x, y);
                } else if (noise < 0.1f) {
                    // Occasional darker spots (small rocks/shadows)
                    pixmap.setColor(darkColor[0], darkColor[1], darkColor[2], darkColor[3]);
                    pixmap.drawPixel(x, y);
                }
            }
        }
    }
    
    /**
     * Adds darker spots to sand texture (small rocks, shadows).
     * 
     * @param pixmap The pixmap to modify
     * @param random Random number generator for variation
     * @param darkColor Darker sand color for spots
     */
    private void addSandSpots(Pixmap pixmap, Random random, float[] darkColor) {
        // Add scattered darker spots (small rocks, pebbles)
        int spotCount = 10 + random.nextInt(5);
        for (int i = 0; i < spotCount; i++) {
            int x = random.nextInt(BiomeConfig.TEXTURE_SIZE);
            int y = random.nextInt(BiomeConfig.TEXTURE_SIZE);
            int size = 1 + random.nextInt(2); // 1-2 pixel spots
            
            pixmap.setColor(darkColor[0], darkColor[1], darkColor[2], darkColor[3]);
            for (int dx = 0; dx < size; dx++) {
                for (int dy = 0; dy < size; dy++) {
                    int px = x + dx;
                    int py = y + dy;
                    if (px < BiomeConfig.TEXTURE_SIZE && py < BiomeConfig.TEXTURE_SIZE) {
                        pixmap.drawPixel(px, py);
                    }
                }
            }
        }
    }
    
    /**
     * Adds lighter highlights to sand texture (sun-bleached areas).
     * 
     * @param pixmap The pixmap to modify
     * @param random Random number generator for variation
     * @param lightColor Lighter sand color for highlights
     */
    private void addSandHighlights(Pixmap pixmap, Random random, float[] lightColor) {
        // Add lighter highlights (sun-bleached areas)
        int highlightCount = 8 + random.nextInt(4);
        for (int i = 0; i < highlightCount; i++) {
            int x = random.nextInt(BiomeConfig.TEXTURE_SIZE);
            int y = random.nextInt(BiomeConfig.TEXTURE_SIZE);
            
            pixmap.setColor(lightColor[0], lightColor[1], lightColor[2], lightColor[3]);
            pixmap.drawPixel(x, y);
            
            // Occasionally extend the highlight
            if (random.nextFloat() > 0.5f && x + 1 < BiomeConfig.TEXTURE_SIZE) {
                pixmap.drawPixel(x + 1, y);
            }
        }
        
        // Add some diagonal wind-swept patterns
        for (int i = 0; i < BiomeConfig.TEXTURE_SIZE; i += 12) {
            for (int j = 0; j < BiomeConfig.TEXTURE_SIZE; j += 10) {
                if (random.nextFloat() > 0.6f) {
                    // Diagonal pattern suggesting wind direction
                    pixmap.setColor(lightColor[0], lightColor[1], lightColor[2], lightColor[3]);
                    if (i + 1 < BiomeConfig.TEXTURE_SIZE && j + 1 < BiomeConfig.TEXTURE_SIZE) {
                        pixmap.drawPixel(i, j);
                        if (random.nextFloat() > 0.5f) {
                            pixmap.drawPixel(i + 1, j + 1);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Creates a LibGDX Texture from a Pixmap.
     * Sets the texture to repeat for seamless tiling.
     * 
     * @param pixmap The pixmap to convert
     * @return A texture with repeat wrapping enabled
     */
    private Texture createTextureFromPixmap(Pixmap pixmap) {
        Texture texture = new Texture(pixmap);
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        return texture;
    }
}
