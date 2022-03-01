package wagemaker.co.uk.game;

import wagemaker.co.uk.gfx.GameCamera;
import wagemaker.co.uk.input.KeyManager;
import wagemaker.co.uk.input.MouseManager;
import wagemaker.co.uk.worlds.World01;

public class Handler {
	
	private Game game;
	private World01 level01;

	public Handler(Game game){
		this.game = game;
	}
	
	public KeyManager getKeyManager(){
		return game.getKeyManager();
	}
	
	public MouseManager getMouseManager(){
		return game.getMouseManager();
	}
	
	public GameCamera getGameCamera(){
		return game.getGameCamera();
	}
	public int getWidth(){
		return game.getWidth();
	}
	
	public int getHeight(){
		return game.getHeight();
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public World01 getLevel01() {
		return level01;
	}

	public void setLevel1(World01 level01) {
		this.level01 = level01;
	}
}
