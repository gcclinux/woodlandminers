package wagemaker.co.uk.gfx;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;

import wagemaker.co.uk.settings.PlayerSettings;
import wagemaker.co.uk.utils.CreateConfig;

public class Assets {
	
	private static final int TileWidth = 64, TileHeight = 64;
	private static final int worldSizeY = 80;
	private static final int worldSizeX = 80;
	
	public static Font font28, font12;
	
	public static BufferedImage dirt, grass, stone, tree, water, rock, wood, sand, bones;
	public static BufferedImage[] player_down, player_up, player_left, player_right, player_still;
	public static BufferedImage[] zombie_down, zombie_up, zombie_left, zombie_right, zombie_still;
	public static BufferedImage[] single_btn, multi_btn, server_btn;
	public static BufferedImage inventoryScreen, menuScreen;
	public static Color ORANGE, GREEN;
	private static String mapFile = CreateConfig.getConfigPath()+PlayerSettings.getWorldName();;
	
	public static void init(){
		
		font28 = FontLoader.loadFont("fonts/slkscr.ttf", 28);
		font12 = new Font("Courier", Font.BOLD,12);
		
		ORANGE = new Color(255, 105, 23);
		GREEN = new Color(106, 168, 79);
	
		SpriteSheet sheet64 = new SpriteSheet(ImageLoader.loadImage("/textures/sprite_64.png"));	
		inventoryScreen = ImageLoader.loadImage("/textures/inventory.png");
		menuScreen = ImageLoader.loadImage("/textures/menu.png");
		SpriteSheet water64 = new SpriteSheet(ImageLoader.loadImage("/textures/water.png"));
		SpriteSheet sheet94 = new SpriteSheet(ImageLoader.loadImage("/textures/2009_sprite_94.png"));	

		single_btn = new BufferedImage[2];
		single_btn[0] =  sheet64.crop(0, TileHeight * 4, TileWidth * 3, TileHeight );
		single_btn[1] = sheet64.crop(0, TileHeight * 5, TileWidth * 3, TileHeight );
		
		multi_btn = new BufferedImage[2];
		multi_btn[0] =  sheet64.crop(0, TileHeight * 6, TileWidth * 3, TileHeight );
		multi_btn[1] = sheet64.crop(0, TileHeight * 7, TileWidth * 3, TileHeight );
		
		server_btn = new BufferedImage[2];
		server_btn[0] =  sheet64.crop(0, TileHeight * 8, TileWidth * 3, TileHeight );
		server_btn[1] = sheet64.crop(0, TileHeight * 9, TileWidth * 3, TileHeight );
		
		player_down = new BufferedImage[3];
		player_up = new BufferedImage[3];
		player_left = new BufferedImage[3];  // added image to buffer
		player_right = new BufferedImage[3];
		player_still = new BufferedImage[2];
		
		player_still[0] = sheet94.crop(94, 415, TileWidth, 96 );
		player_still[1] = sheet94.crop(94, 415, TileWidth, 96 );
		player_down[0] = sheet94.crop(32, 415, TileWidth, 96 );
		player_down[1] = sheet94.crop(96, 415, TileWidth, 96 );
		player_down[2] = sheet94.crop(160, 415, TileWidth, 96 );
		player_up[0] = sheet94.crop(256, 415, TileWidth, 96 );
		player_up[1] = sheet94.crop(320, 415, TileWidth, 96 );
		player_up[2] = sheet94.crop(384, 415, TileWidth, 96 );
		player_right[0] = sheet94.crop(256, 288, TileWidth, 96);
		player_right[1] = sheet94.crop(320, 288, TileWidth, 96);
		player_right[2] = sheet94.crop(384, 288, TileWidth, 96);
		player_left[0] = sheet94.crop(256, 160, TileWidth, 96);
		player_left[1] = sheet94.crop(320, 160, TileWidth, 96);
		player_left[2] = sheet94.crop(384, 160, TileWidth, 96);
		
	
		zombie_down = new BufferedImage[2];
		zombie_up = new BufferedImage[2];
		zombie_left = new BufferedImage[2];
		zombie_right = new BufferedImage[2];
		zombie_still = new BufferedImage[2];
		
		zombie_still[0] = sheet64.crop(TileWidth * 6, TileHeight * 7, TileWidth, TileHeight );
		zombie_still[1] = sheet64.crop(TileWidth * 7, TileHeight * 7, TileWidth, TileHeight );
		zombie_up[0] = sheet64.crop(TileWidth * 8, TileHeight * 8, TileWidth, TileHeight );
		zombie_up[1] = sheet64.crop(TileWidth * 9, TileHeight * 8, TileWidth, TileHeight );
		zombie_down[0] = sheet64.crop(TileWidth * 6, TileHeight * 8, TileWidth, TileHeight );
		zombie_down[1] = sheet64.crop(TileWidth * 7, TileHeight * 8, TileWidth, TileHeight );
		zombie_right[0] = sheet64.crop(TileWidth * 6, TileHeight * 9, TileWidth, TileHeight - 2);
		zombie_right[1] = sheet64.crop(TileWidth * 7, TileHeight * 9, TileWidth, TileHeight - 2);
		zombie_left[0] = sheet64.crop(TileWidth * 8, TileHeight * 9, TileWidth, TileHeight - 2);
		zombie_left[1] = sheet64.crop(TileWidth * 9, TileHeight * 9, TileWidth, TileHeight -2 );
		
		dirt = sheet64.crop(TileWidth, 0, TileWidth, TileHeight);
		bones = sheet64.crop(TileWidth, TileHeight, TileWidth, TileHeight);
		grass = sheet64.crop(TileWidth * 2, 0, TileWidth, TileHeight);
		stone = sheet64.crop(TileWidth * 3, 0, TileWidth, TileHeight);
		tree = sheet64.crop(0, 0, TileWidth, TileHeight);
		rock = sheet64.crop(TileWidth * 4, TileHeight, TileWidth, TileHeight);
		wood = sheet64.crop(TileWidth * 4, TileHeight * 2, TileWidth, TileHeight);
		sand = sheet64.crop(TileWidth, TileHeight * 3, TileWidth, TileHeight);
		
		water = water64.crop(0, 0, TileWidth * 5, TileHeight * 5);
	}

	public static Color getORANGE() {
		return ORANGE;
	}
	
	public static int getWorldSizeY() {
		return worldSizeY;
	}

	public static int getWorldSizeX() {
		return worldSizeX;
	}

	public static String getMapFile() {
		return mapFile;
	}

	public static int getTilewidth() {
		return TileWidth;
	}

	public static int getTileheight() {
		return TileHeight;
	}

}
