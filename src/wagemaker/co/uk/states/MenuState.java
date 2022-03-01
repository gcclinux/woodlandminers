package wagemaker.co.uk.states;

import java.awt.Graphics;

import wagemaker.co.uk.game.Handler;
import wagemaker.co.uk.gfx.Assets;
import wagemaker.co.uk.tiles.Tile;
import wagemaker.co.uk.ui.ClickListener;
import wagemaker.co.uk.ui.UIImageButton;
import wagemaker.co.uk.ui.UIManager;

public class MenuState extends State{

	private UIManager uiManager;
	
	public MenuState(Handler handler){
		super(handler);
		uiManager = new UIManager(handler);
		handler.getMouseManager().setUIManager(uiManager);
		
		//Adding Game button & action
		uiManager.addObject(new UIImageButton(
				((int) handler.getWidth() / 2 - ((Tile.TILEWIDTH * 2) / 4) * 3),
				((int) handler.getHeight() / 2 - Tile.TILEHEIGHT),
				((int) Tile.TILEWIDTH * 3), 
				((int) Tile.TILEHEIGHT), Assets.single_btn, new ClickListener(){

			@Override
			public void onClick() {
				handler.getMouseManager().setUIManager(null);
				State.setState(handler.getGame().gameState);
			}}));
		
//		//Adding Multiplayer button & action
//		uiManager.addObject(new UIImageButton(
//				((int) handler.getWidth() / 2 - ((Tile.TILEWIDTH * 2) / 4) * 3), 
//				((int) handler.getHeight() / 2 - Tile.TILEHEIGHT), 
//				((int) Tile.TILEWIDTH * 3), 
//				((int) Tile.TILEHEIGHT), Assets.multi_btn, new ClickListener(){
//
//			@Override
//			public void onClick() {
//				handler.getMouseManager().setUIManager(null);
//				handler.getGame().setGameType(true);
//				State.setState(handler.getGame().gameState);
//			}}));
//		
//		//Adding server button & action
//		uiManager.addObject(new UIImageButton(
//				((int) handler.getWidth() / 2 - ((Tile.TILEWIDTH * 2) / 4) * 3), 
//				((int) handler.getHeight() / 2), 
//				((int) Tile.TILEWIDTH * 3), 
//				((int) Tile.TILEHEIGHT), Assets.server_btn, new ClickListener(){
//
//			@Override
//			public void onClick() {
//				handler.getMouseManager().setUIManager(null);
//				handler.getGame().setServerTrue(true);
//				State.setState(handler.getGame().gameState);
//			}}));
		
		
	}
	@Override
	public void tick() {
		uiManager.tick();
		
		//Remove this to click on Start before loading the map
//		if (handler.getMouseManager().isRightPressed()){
//			State.setState(handler.getGame().gameState);
//		}
	}

	@Override
	public void render(Graphics g) {
		
		g.setColor(Assets.GREEN);
		g.fillRect(0,0,handler.getWidth(),handler.getHeight());
		
		uiManager.render(g);
	}
}
