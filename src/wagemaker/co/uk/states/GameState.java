package wagemaker.co.uk.states;

import java.awt.Graphics;

import wagemaker.co.uk.game.Handler;
import wagemaker.co.uk.gfx.Assets;
import wagemaker.co.uk.worlds.World01;

public class GameState extends State{
	
	private World01 level01;
	
	public GameState(Handler handler){
		super(handler);
		level01 = new World01(handler, Assets.getMapFile());
		handler.setLevel1(level01);
	}
	public void tick() {
		level01.tick();
	}

	@Override
	public void render(Graphics g) {
		level01.render(g);
	}
}
