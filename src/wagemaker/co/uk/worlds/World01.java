package wagemaker.co.uk.worlds;

import java.awt.Graphics;

import wagemaker.co.uk.entities.Entity;
import wagemaker.co.uk.entities.EntityManager;
import wagemaker.co.uk.entities.Player;
import wagemaker.co.uk.entities.Rock;
import wagemaker.co.uk.entities.TreeTall;
import wagemaker.co.uk.entities.TreeSmall;
import wagemaker.co.uk.entities.Zombie;
import wagemaker.co.uk.game.Handler;
import wagemaker.co.uk.gfx.Assets;
import wagemaker.co.uk.items.ItemManager;
import wagemaker.co.uk.settings.PlayerSettings;
import wagemaker.co.uk.tiles.Tile;
import wagemaker.co.uk.utils.Utils;

public class World01 {

	private Handler handler;
	private int width, height;
	private int spawnX, spawnY;
	private int worldSizeX, worldSizeY;
	private int[][] tiles;
	private int treeTallCount, rockCount, zombieCount, treeSmallCount;
	private int maxTreeTall , maxTreeSmall, maxRock, maxZombie;
	Player player;
	
	// Respawn timer
	private long lastAttackTimer, attackCooldown = 10000, attackTimer = attackCooldown;
	
	// Entities
	private EntityManager entityManager;
	
	// Item
	private ItemManager itemManager;
	int worldWidth;
	int worldHeight;
	
	public World01(Handler handler, String path){
		this.handler = handler;
		
		entityManager = new EntityManager(handler, new Player(handler, 0, 0, null), null);
		itemManager = new ItemManager(handler);

		//Loading the world
		loadWorld(path);
		
		worldWidth = Assets.getWorldSizeX();
		worldHeight = Assets.getWorldSizeY();
		
		maxTreeTall = (((worldWidth * worldHeight) / 1000 )* 3);
		maxTreeSmall = (((worldWidth * worldHeight) / 1000 )* 3);
		maxRock = (((worldWidth * worldHeight) / 1000 ) * 3);
		maxZombie = (5);
		
		for(int t = 0;t < maxTreeTall;t++){
			
			int x = ((int )(Math.random() * worldWidth + Math.random()));
			int y = ((int )(Math.random() * worldWidth + Math.random()));
			int lowY = (y + 1);
			
			if (getTile(x,y).getID() == 0 && getTile(x,lowY).getID() == 0){
				entityManager.addEntity(new TreeTall(handler, Tile.TILEWIDTH * x, Tile.TILEHEIGHT * y));
			}
		}
		
		for(int t = 0;t < maxTreeSmall;t++){
			
			int x = ((int )(Math.random() * worldWidth + Math.random()));
			int y = ((int )(Math.random() * worldWidth + Math.random()));
			int lowY = (y + 1);
			
			if (getTile(x,y).getID() == 0 && getTile(x,lowY).getID() == 0){
				entityManager.addEntity(new TreeSmall(handler, Tile.TILEWIDTH * x, Tile.TILEHEIGHT * y));
			}
		}
		
		for(int t = 0;t < maxRock;t++){
			
			int x = ((int )(Math.random() * worldWidth + Math.random()));
			int y = ((int )(Math.random() * worldHeight + Math.random()));
			
			if (getTile(x,y).getID() == 0){
				entityManager.addEntity(new Rock(handler, Tile.TILEWIDTH * x, Tile.TILEHEIGHT * y));
			}
		}
		
		for(int t = 0;t < maxZombie;t++){
			
			int x = ((int )(Math.random() * worldWidth + Math.random()));
			int y = ((int )(Math.random() * worldWidth + Math.random()));
			int lowY = (y + 1);
			
			if (getTile(x,y).getID() == 0 && getTile(x,lowY).getID() == 0){
				entityManager.addEntity(new Zombie(handler, Tile.TILEWIDTH * x, Tile.TILEHEIGHT * y, null));
			}
		}
		
		
		entityManager.getPlayer().setX(spawnX);
		entityManager.getPlayer().setY(spawnY);

	}

	public void tick(){
		entityManager.tick();
		itemManager.tick();
		itemRespawn();
	}
	
	private void itemRespawn() {
		attackTimer += System.currentTimeMillis() - lastAttackTimer;
		lastAttackTimer = System.currentTimeMillis();
		if(attackTimer < attackCooldown)
			return;

		for (Entity e : handler.getLevel01().getEntityManager().getEntities()){
			if(e.getClass().getSimpleName().equals("Tree1")){
				treeTallCount++;
			} else if(e.getClass().getSimpleName().equals("Rock1")){
				rockCount++;
			} else if(e.getClass().getSimpleName().equals("Zombie")){
				zombieCount++;
			} else if (e.getClass().getSimpleName().equals("Tree2")){
				treeSmallCount++;
			} 
		}
		
		maxTreeTall = (((worldWidth * worldHeight) / 1000 ));
		maxTreeSmall = (((worldWidth * worldHeight) / 1000 ));
		maxRock = (((worldWidth * worldHeight) / 1000 ));
		
		for(int t = treeTallCount;t < maxTreeTall;t++){
			
			int x = ((int )(Math.random() * worldWidth + Math.random()));
			int y = ((int )(Math.random() * worldWidth + Math.random()));
			int lowY = (y + 1);
			
			if (getTile(x,y).getID() == 0 && getTile(x,lowY).getID() == 0){
				entityManager.addEntity(new TreeTall(handler, Tile.TILEWIDTH * x, Tile.TILEHEIGHT * y));
			}
		}
		
		for(int t = treeSmallCount;t < maxTreeSmall;t++){
			
			int x = ((int )(Math.random() * worldWidth + Math.random()));
			int y = ((int )(Math.random() * worldWidth + Math.random()));
			int lowY = (y + 1);
			
			if (getTile(x,y).getID() == 0 && getTile(x,lowY).getID() == 0){
				entityManager.addEntity(new TreeSmall(handler, Tile.TILEWIDTH * x, Tile.TILEHEIGHT * y));
			}
		}
		
		for(int t = rockCount;t < maxRock;t++){
			
			int x = ((int )(Math.random() * worldWidth + Math.random()));
			int y = ((int )(Math.random() * worldHeight + Math.random()));
			
			if (getTile(x,y).getID() == 0){
				entityManager.addEntity(new Rock(handler, Tile.TILEWIDTH * x, Tile.TILEHEIGHT * y));
			}
		}
		
		for(int t = zombieCount;t < maxZombie;t++){
			
			int x = ((int )(Math.random() * worldWidth + Math.random()));
			int y = ((int )(Math.random() * worldHeight + Math.random()));
			
			if (getTile(x,y).getID() == 0){
				entityManager.addEntity(new Zombie(handler, Tile.TILEWIDTH * x, Tile.TILEHEIGHT * y, null));
			}
		}
		
		treeTallCount = treeSmallCount = rockCount = zombieCount = (int) (attackTimer = 0);
	
	}

	public void render(Graphics g){
		
		int xStart = (int) Math.max(0, handler.getGameCamera().getxOffset() / Tile.TILEWIDTH);
		int xEnd = (int) Math.min(width, (handler.getGameCamera().getxOffset() + handler.getWidth()) / Tile.TILEWIDTH + 1);
		int yStart = (int) Math.max(0, handler.getGameCamera().getyOffset() / Tile.TILEHEIGHT);
		int yEnd = (int) Math.min(width, (handler.getGameCamera().getyOffset() + handler.getHeight()) / Tile.TILEHEIGHT + 1);
		
		for (int y = yStart; y < yEnd; y++){
			for (int x = xStart; x < xEnd; x++){
				getTile(x,y).render(g, (int) (x * Tile.TILEWIDTH - handler.getGameCamera().getxOffset()), 
					(int) (y * Tile.TILEHEIGHT - handler.getGameCamera().getyOffset()));
			}
		}
		
		//Item
		itemManager.render(g);
		
		//Entities		
		entityManager.render(g);
	}
	
	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public ItemManager getItemManager() {
		return itemManager;
	}

	public void setItemManager(ItemManager itemManager) {
		this.itemManager = itemManager;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public Tile getTile(int x, int y){

		if(x < 0 || y < 0 || x >= width || y >= height)
			return Tile.grassTile;
		
		Tile t = Tile.tiles[tiles[x][y]];
		
		if(t == null)
			return Tile.dirtTile;
		return t;
	}
	
	private void loadWorld(String path){
		String file = Utils.loadFileAsString(path);
		String[] tokens = file.split("\\s+");
		width = Assets.getWorldSizeX();
		height = Assets.getWorldSizeY();
		worldSizeY = width;
		worldSizeX = height;

		tiles = new int[worldSizeX][worldSizeY];
		for(int y = 0;y < worldSizeY;y++){
			for(int x = 0;x < worldSizeX;x++){
				tiles[x][y] = Utils.parseInt(tokens[(x + y * worldSizeX)]);
			}
		}
		
		// Set Spawn Position
		spawnX = Integer.parseInt(PlayerSettings.getPlayerStartX());
		spawnY = Integer.parseInt(PlayerSettings.getPlayerStartY());

	}
	
	public int getWidth(){
		return width;
	}
	
	public int getHeight(){
		return height;
	}
}
