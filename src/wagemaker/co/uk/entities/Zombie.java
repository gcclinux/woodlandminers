package wagemaker.co.uk.entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import wagemaker.co.uk.game.Handler;
import wagemaker.co.uk.gfx.Animation;
import wagemaker.co.uk.gfx.Assets;
import wagemaker.co.uk.inventory.Inventory;
import wagemaker.co.uk.inventory.Menu;
import wagemaker.co.uk.items.Item;

public class Zombie extends Creature{
	
	//Animation
	private Animation animDown,animUp,animLeft,animRight,animStill;
	
	// Attach timer
	private int zombieAttach = 300;
	private long lastAttackTimer, attackCooldown = zombieAttach, attackTimer = attackCooldown;
	
	//Inventory
	private Inventory inventory;
	private Menu menu;
	
	// Zombie Details
	private String ZombieName = "Green";
	private boolean input = true;
	private float zombieSpeed = 1.8f;
	
	// Current Health checked
	private int currentHealth;
	private long lastHealthTimer, healthCooldown = 1000, healthTimer = healthCooldown;
	
	public Zombie(Handler handler, float x, float y, String userName) {
		super(handler, x, y, 
				(Creature.DEFAULT_CREATURE_WIDTH / 3 * 2), 
				(Creature.DEFAULT_CREATURE_HEIGHT / 3 * 2)
				); //Creature size
		
		bounds.x = 11;
		bounds.y = 16;
		bounds.width = 20;
		bounds.height = 25;
		
		liveLine.x = 0;
		liveLine.y = -10; // Distance above "0" - liveLine.height
		liveLine.width = width;
		liveLine.height = 5;
		
		attacked.x = 0;
		attacked.y = -10; // Distance above "0" - liveLine.height
		attacked.width = width;
		attacked.height = 5;
		
		//Animation
		
		animDown = new Animation(500, Assets.zombie_down);
		animUp = new Animation(500, Assets.zombie_up);
		animLeft = new Animation(500, Assets.zombie_left);
		animRight = new Animation(500, Assets.zombie_right);
		animStill = new Animation(500, Assets.zombie_still);
		
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
		
		//Target
		int targetX = ((int)(handler.getLevel01().getEntityManager().getPlayer().getX()) + 12 );
		int targetY = ((int)(handler.getLevel01().getEntityManager().getPlayer().getY()) + 24 );
		
		attackTimer += System.currentTimeMillis() - lastAttackTimer;
		lastAttackTimer = System.currentTimeMillis();
		if(attackTimer < attackCooldown)
			return;
		
		//If inventory is open player can not be attacked
		if(inventory.isActive() || menu.isActive())
			return;

		attackTimer = 0;
		
		for (Entity e : handler.getLevel01().getEntityManager().getEntities()){
			if(e.equals(this))
				continue;
			if (
					((((int) x) == (targetX + 19) || ((int) x) == (targetX - 20) || ((int) x) == (targetX - 21) || ((int) x) == (targetX - 22)) 
							&& !(((int) y) > (targetY + 25) || ((int) y) < (targetY - 34)))
					||
				    ((((int) y) == (targetY + 24) || ((int) y) == (targetY + 25) || ((int) y) == (targetY - 33) || ((int) y) == (targetY - 34)) 
				    		&& !(((int) x) > (targetX + 20) || ((int) x) < (targetX - 22)))
				){
				handler.getLevel01().getEntityManager().getPlayer().hurt(1);
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
		
		//Target
		int targetX = ((int)(handler.getLevel01().getEntityManager().getPlayer().getX()) + 12 );
		int targetY = ((int)(handler.getLevel01().getEntityManager().getPlayer().getY()) + 24 );
		
		for (Entity e : handler.getLevel01().getEntityManager().getEntities()){
			if(e.equals(this)){
				
				if (
					   (int) x < targetX && (int) x > targetX - Assets.getTilewidth() * 16 
					|| (int) x > targetX && (int) x < targetX + Assets.getTilewidth() * 16
					|| (int) x == targetX && (int) x > targetX + Assets.getTilewidth() * 16
					|| (int) x == targetX && (int) x < targetX + Assets.getTilewidth() * 16
				 && 
				 	   (int) y < targetY && (int) y > targetY - Assets.getTilewidth() * 16 
				    || (int) y > targetY && (int) y < targetY + Assets.getTilewidth() * 16
				 	|| (int) y == targetY && (int) y > targetY - Assets.getTilewidth() * 16 
				    || (int) y == targetY && (int) y < targetY + Assets.getTilewidth() * 16				    
				) 
				{	
					if (((int) x > targetX) && ((int) y > targetY)){
						yMove = -zombieSpeed;
						xMove = -zombieSpeed;
					}
					if (((int) x > targetX) && ((int) y == targetY)){
						xMove = -zombieSpeed;
					}
					if (((int) x > targetX) && ((int) y < targetY)){
						yMove = zombieSpeed;
						xMove = -zombieSpeed;
					}
					if (((int) x < targetX) && ((int) y < targetY)){
						yMove = zombieSpeed;
						xMove = zombieSpeed;
					}
					if (((int) x < targetX) && ((int) y == targetY)){
						xMove = zombieSpeed;
					}
					if (((int) x < targetX) && ((int) y > targetY)){
						yMove = -zombieSpeed;
						xMove = zombieSpeed;
					}
					if (((int) x == targetX) && ((int) y > targetY)){
						yMove = -zombieSpeed;
					}
					if (((int) x == targetX) && ((int) y < targetY)){
						yMove = zombieSpeed;
					}
				} else {
					return;
				}
			}
		}
	}

	@Override
	public void render(Graphics g) {		
		//Draw Zombie	
		g.drawImage(getCurrentAnimationFrame(), 
				(int) (x - handler.getGameCamera().getxOffset()), 
				(int) (y - handler.getGameCamera().getyOffset()),
				width, height, null);
	
		// Draw Zombie life
		for (Entity e : handler.getLevel01().getEntityManager().getEntities()){

			if(e.getHealth() != DEFAULT_HEALTH){
				if(e.equals(this)){
				g.setColor(Color.GREEN);
				g.fillRect((int) (x + attacked.x - handler.getGameCamera().getxOffset()), 
						(int) (y + attacked.y - handler.getGameCamera().getyOffset()),
						attacked.width, attacked.height);
				
				//Need to get attack countws
				g.setColor(Color.RED);
				g.fillRect((int) (x + liveLine.x - handler.getGameCamera().getxOffset()), 
						(int) (y + liveLine.y - handler.getGameCamera().getyOffset()),
						liveLine.width - ((int) e.getWidth() / DEFAULT_HEALTH)*(e.getHealth()),
						liveLine.height);
				}
			}
		}	
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
		int live = handler.getLevel01().getEntityManager().getPlayer().getHealth();
		if ( live > 0 ) {
			handler.getLevel01().getItemManager().addItem(Item.boneItem.createNew((int) x + 64, (int) y - 128));
		}
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

	public void setInput(boolean input) {
		this.input = input;
	}

	public String getZombieName() {
		return ZombieName;
	}

	public void setZombieName(String zombieName) {
		ZombieName = zombieName;
	}

	public int getZombieAttach() {
		return zombieAttach;
	}

	public void setZombieAttach(int zombieAttach) {
		this.zombieAttach = zombieAttach;
	}

	public float getZombieSpeed() {
		return zombieSpeed;
	}

	public void setZombieSpeed(int zombieSpeed) {
		this.zombieSpeed = zombieSpeed;
	}
}
