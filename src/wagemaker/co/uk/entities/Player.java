package wagemaker.co.uk.entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import wagemaker.co.uk.game.Handler;
import wagemaker.co.uk.gfx.Animation;
import wagemaker.co.uk.gfx.Assets;
import wagemaker.co.uk.gfx.Text;
import wagemaker.co.uk.inventory.Inventory;
import wagemaker.co.uk.inventory.Menu;
import wagemaker.co.uk.settings.PlayerSettings;
import wagemaker.co.uk.tiles.Tile;

public class Player extends Creature{
	
	//Animation
	private Animation animDown,animUp,animLeft,animRight,animStill;
	
	// Attach timer
	private long lastAttackTimer, attackCooldown = 80, attackTimer = attackCooldown;
	
	//Inventory
	private Inventory inventory;
	private Menu menu;
	
	// Player Details
	private String PlayerName = PlayerSettings.getPlayerName();
	private boolean input = true;
	
	// Current Health checked
	private int currentHealth;
	private long lastHealthTimer, healthCooldown = 2000, healthTimer = healthCooldown;

	public Player(Handler handler, float x, float y, String userName) {
		super(handler, x, y, Creature.DEFAULT_CREATURE_WIDTH, 
				(Creature.DEFAULT_CREATURE_HEIGHT + (int) 1.2f)); //Creature size
		
		bounds.x = 22;
		bounds.y = 32;
		bounds.width = 20;
		bounds.height = 32;
		
		liveLine.x = 0;
		liveLine.y = -10; // Distance above "0" - liveLine.height
		liveLine.width = width;
		liveLine.height = 5;
		
		attacked.x = 0;
		attacked.y = -10; // Distance above "0" - liveLine.height
		attacked.width = width;
		attacked.height = 5;
		
		//Animation
		
		animDown = new Animation(100, Assets.player_down);
		animUp = new Animation(100, Assets.player_up);
		animLeft = new Animation(100, Assets.player_left);
		animRight = new Animation(100, Assets.player_right);
		animStill = new Animation(500, Assets.player_still);
		
		inventory = new Inventory(handler);
		menu = new Menu(handler);
		
	}

	@Override
	public void tick() {
		//Animation
		animDown.tick();
		animUp.tick();
		animLeft.tick();
		animRight.tick();
		animStill.tick();
		
		//movement
		getInput();
		move();
		
		// Center Player
		handler.getGameCamera().centerOnEntity(this);

		//Attack
		checkAttacks();
		
		//Check life
		currentLive();
		
		//Inventory
		inventory.tick();
		menu.tick();
	}
	
	//Restoring current life if not attacked for a period of time
	private void currentLive() {
			healthTimer += System.currentTimeMillis() - lastHealthTimer;
			lastHealthTimer = System.currentTimeMillis();
			if(healthTimer < healthCooldown)
				return;

			for (Entity e : handler.getLevel01().getEntityManager().getEntities()){
				if(e.getHealth() != DEFAULT_HEALTH){
					if(e.equals(this)){
						if (e.getHealth() == currentHealth){
							e.setHealth(currentHealth + 1);
						} else {
						currentHealth = e.getHealth();
						}
					}
				}
			}
			healthTimer = 0;
	}

	private void checkAttacks(){
		attackTimer += System.currentTimeMillis() - lastAttackTimer;
		lastAttackTimer = System.currentTimeMillis();
		if(attackTimer < attackCooldown)
			return;
		
		//If inventory is open player can not be attacked
		if(inventory.isActive() || menu.isActive())
			return;
		
		Rectangle cb = getCollisionBounds(0,0);
		Rectangle ar = new Rectangle(); // Attack rectangle
		int arSize = 20;
		ar.width = arSize;
		ar.height = arSize;
		
		
		if(handler.getKeyManager().aUp){
			ar.x = cb.x + cb.width / 2 - arSize / 2;
			ar.y = cb.y - arSize;
		} else if(handler.getKeyManager().aDown){
			ar.x = cb.x + cb.width / 2 - arSize / 2;
			ar.y = cb.y + cb.height;
		} else if(handler.getKeyManager().aLeft){
			ar.x = cb.x - arSize;
			ar.y = cb.y + cb.height / 2 - arSize / 2;
		} else if(handler.getKeyManager().aRight){
			ar.x = cb.x + cb.width;
			ar.y = cb.y + cb.height / 2 - arSize / 2;
		} else {
			return;
		}
		
		attackTimer = 0;
		
		for (Entity e : handler.getLevel01().getEntityManager().getEntities()){
			if(e.equals(this))
				continue;
			if(e.getCollisionBounds(0, 0).intersects(ar)){
				e.hurt(1);
				return;
			}
		}
	}
	
	private void getInput(){

		xMove = 0;
		yMove = 0;
		
		//If inventory is open player can not move, has to be below xMove & yMove or player won't stop moving.
		if(inventory.isActive() || menu.isActive())
			return;
		
		if (handler.getKeyManager() != null){
			if(handler.getKeyManager().up)
				yMove = -speed;
			if(handler.getKeyManager().up && handler.getKeyManager().right)
				yMove = -speed / 2;
			if(handler.getKeyManager().up && handler.getKeyManager().left)
				yMove = -speed / 2;
			if(handler.getKeyManager().down)
				yMove = speed;
			if(handler.getKeyManager().down && handler.getKeyManager().right)
				yMove = speed / 2;
			if(handler.getKeyManager().down && handler.getKeyManager().left)
				yMove = speed / 2;
			if(handler.getKeyManager().left)
				xMove = -speed;
			if(handler.getKeyManager().right)
				xMove = speed;
		}
	}

	@Override
	public void render(Graphics g) {
		
		//Draw Player		
		g.drawImage(getCurrentAnimationFrame(), 
				(int) (x - handler.getGameCamera().getxOffset()), 
				(int) (y - handler.getGameCamera().getyOffset()),
				width, height, null);
	
		// Draw Player Name
		Text.drawString(g, PlayerName, 
				(int) (x - handler.getGameCamera().getxOffset() + (Tile.TILEWIDTH / 2)),
				(int) (y - handler.getGameCamera().getyOffset() - (Tile.TILEWIDTH / 6)),
				 true, Color.WHITE, Assets.font12);

		// Draw Player life
		Entity e = handler.getLevel01().getEntityManager().getPlayer();
		
		if(e.getHealth() != DEFAULT_HEALTH){
			if(e.equals(this)){
				g.setColor(Color.GREEN);
				g.fillRect(
						(int) (x + attacked.x - handler.getGameCamera().getxOffset()), 
						(int) (y + attacked.y - handler.getGameCamera().getyOffset() - (Tile.TILEWIDTH / 6)),
						attacked.width, attacked.height);
				
				g.setColor(Color.RED);
				int health = e.getHealth();
				if (health < 0) {
					health = 0;
				}
				g.fillRect(
						(int) (x + liveLine.x - handler.getGameCamera().getxOffset()), 
						(int) (y + liveLine.y - handler.getGameCamera().getyOffset() - (Tile.TILEWIDTH / 6)),
						liveLine.width - ((int) Tile.TILEWIDTH / DEFAULT_HEALTH)*(health), liveLine.height);
			}
			
		}
		
//		collision box
//		g.setColor(Color.RED);
//		g.fillRect((int) (x + bounds.x - handler.getGameCamera().getxOffset()), 
//				(int) (y + bounds.y - handler.getGameCamera().getyOffset()),
//				bounds.width, bounds.height);
		
		
	}
	
	public void postRender(Graphics g){
		inventory.render(g);
		menu.render(g);
	}
	
	private BufferedImage getCurrentAnimationFrame(){
		if (input == false){
			return animStill.getCurrentFrame();
		} else {
			if(xMove < 0) {
				return animLeft.getCurrentFrame();
			} else if (xMove > 0){
				return animRight.getCurrentFrame();
			} else if (yMove < 0 ){
				return animUp.getCurrentFrame();
			} else if (yMove > 0 ){
				return animDown.getCurrentFrame();
			} else {
				return animStill.getCurrentFrame();
			}
		}
	}

	@Override
	public void die() {
		int X = handler.getHeight();
		int Y = handler.getWidth();
		handler.getLevel01().getEntityManager().getPlayer().setX(X);
		handler.getLevel01().getEntityManager().getPlayer().setY(Y);
		handler.getLevel01().getEntityManager().getPlayer().setActive(true);
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}
	
	public Menu getMenu() {
		return menu;
	}

	public void setMenu(Menu menu) {
		this.menu = menu;
	}

	public String getPlayerName() {
		return PlayerName;
	}

	public void setInput(boolean input) {
		this.input = input;
	}
}
