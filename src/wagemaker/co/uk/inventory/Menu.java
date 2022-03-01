package wagemaker.co.uk.inventory;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import wagemaker.co.uk.game.Handler;
import wagemaker.co.uk.gfx.Assets;
import wagemaker.co.uk.gfx.Text;
import wagemaker.co.uk.items.Item;

public class Menu {
	
	private Handler handler;
	private boolean active = false;
	private ArrayList<Item> inventoryItems;
	
	// Inventory Item list "text"
	private int invX = 448, invY = 48;
	private int invWidth = 512, invHeight = 384;
	private int invListCenterX = invX + 171;
	private int invListCenterY = invY + invHeight / 2 + 5;
	private int invListSpacing = 30;
	
	// Inventory Item image "top right"
	private int invImageX = 451;
	private int invImageY = 90;
	private int invImageWidth = 64;
	private int invImageHeight = 64;
	
	// Inventory Item Count items "bellow image"
	private int invCountX = 484 - (int) 1.5f;
	private int invCountY = 179;
	
	// Inventory Index of the Item array list
	private int selectedItem = 0;
	
	

	public Menu(Handler handler){
		this.handler = handler;
		inventoryItems = new ArrayList<Item>();
	}
	
	public void tick(){
		if(handler.getKeyManager().keyJustPressed(KeyEvent.VK_ESCAPE))
			active = !active;
		if(!active)
			return;	
		
		if(handler.getKeyManager().keyJustPressed(KeyEvent.VK_UP))
			selectedItem--;
		if(handler.getKeyManager().keyJustPressed(KeyEvent.VK_DOWN))
			selectedItem++;
		
		if(selectedItem  < 0)
			selectedItem = inventoryItems.size() - 1;
		else if(selectedItem >= inventoryItems.size())
			selectedItem = 0;
		
	}
	
	public void render(Graphics g){
		if(!active)
			return;
		g.drawImage(Assets.menuScreen, invX, invY, invWidth, invHeight, null);
		
		int len = inventoryItems.size();
		if(len == 0)
			return;
		
		for(int i = -5;i <6;i++){
			if(selectedItem + i < 0 || selectedItem +i >= len)
				continue;
			if(i == 0){
				Text.drawString(g, ">> " + inventoryItems.get(selectedItem + i).getName() + " <<", invListCenterX, 
						invListCenterY + i * invListSpacing, true, Color.YELLOW, Assets.font28);
			} else {
				Text.drawString(g, inventoryItems.get(selectedItem + i).getName(), invListCenterX, 
						invListCenterY + i * invListSpacing, true, Color.WHITE, Assets.font28);
			}
		}
		
		Item item = inventoryItems.get(selectedItem);
		g.drawImage(item.getTexture(), invImageX, invImageY, invImageWidth, invImageHeight, null);
		Text.drawString(g, Integer.toString(item.getCount()), invCountX, invCountY, true, Color.WHITE, Assets.font28);	
	}
	
	//Inventory methods
	
	public void addItem(Item item){
		for(Item i : inventoryItems){
			if(i.getId() == item.getId()){
				i.setCount(i.getCount() + item.getCount());
				return;
			}
		}
		inventoryItems.add(item);
	}

	// Getters & setters
	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public boolean isActive() {
		return active;
	}
}
