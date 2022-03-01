package wagemaker.co.uk.entities;

import java.awt.Color;
import java.awt.Graphics;

import wagemaker.co.uk.game.Handler;
import wagemaker.co.uk.gfx.Assets;
import wagemaker.co.uk.items.Item;
import wagemaker.co.uk.tiles.Tile;

public class TreeTall extends StaticEntity{
	
	// Attach timer
	private long lastAttackTimer, attackCooldown = 2500, attackTimer = attackCooldown;
	
	// Current Health checked
	private int currentHealth;
	

	public TreeTall(Handler handler, float x, float y) {
		super(handler, x, y, Tile.TILEWIDTH, Tile.TILEHEIGHT * 2);
		
		bounds.x = 12;
		bounds.y = (int) (height / 1.5f);
		bounds.width = width - 32;
		bounds.height = (int)(height - height / 1.5f);
		
		liveLine.x = 0;
		liveLine.y = -10; // Distance above "0" - liveLine.height
		liveLine.width = width;
		liveLine.height = 5;
		
		attacked.x = 0;
		attacked.y = -10; // Distance above "0" - liveLine.height
		attacked.width = width;
		attacked.height = 5;
		
	}

	@Override
	public void tick() {
		currentLive();
	}

	//Restoring current life if not attacked for a period of time
	private void currentLive() {
			attackTimer += System.currentTimeMillis() - lastAttackTimer;
			lastAttackTimer = System.currentTimeMillis();
			if(attackTimer < attackCooldown)
				return;

			for (Entity e : handler.getLevel01().getEntityManager().getEntities()){
				if(e.getHealth() != Entity.DEFAULT_HEALTH){	
					if(e.equals(this)){
						if (e.getHealth() == currentHealth){
							e.setHealth(currentHealth + 1);
						} else {
						currentHealth = e.getHealth();
						}
					}
				}
			}
			
			attackTimer = 0;
		
	}

	@Override
	public void render(Graphics g) {
		
		g.drawImage(Assets.tree, (int) (x - handler.getGameCamera().getxOffset()), (int) (y - handler.getGameCamera().getyOffset()), width, height, null);
			
		for (Entity e : handler.getLevel01().getEntityManager().getEntities()){

			if(e.getHealth() != Entity.DEFAULT_HEALTH){	
				if(e.equals(this)){
				g.setColor(Color.GREEN);
				g.fillRect((int) (x + attacked.x - handler.getGameCamera().getxOffset()), 
						(int) (y + attacked.y - handler.getGameCamera().getyOffset()),
						attacked.width, attacked.height);
				
				//Need to get attack count
				g.setColor(Color.RED);
				g.fillRect((int) (x + liveLine.x - handler.getGameCamera().getxOffset()), 
						(int) (y + liveLine.y - handler.getGameCamera().getyOffset()),
						liveLine.width - ((int) Tile.TILEWIDTH /Entity.DEFAULT_HEALTH )*(e.getHealth()), 
						liveLine.height);
				}
			}
		}
	}
	
	@Override
	public void die() {
		int live = handler.getLevel01().getEntityManager().getPlayer().getHealth();
		if ( live > 0 ) {
			handler.getLevel01().getItemManager().addItem(Item.wood1Item.createNew((int) x + Tile.TILEWIDTH, (int) y - Tile.TILEHEIGHT));
		}		
	}

}
