package wagemaker.co.uk.entities;

import wagemaker.co.uk.game.Handler;

public abstract class StaticEntity extends Entity{

	public StaticEntity(Handler handler, float x, float y, int width, int height) {
		super(handler, x, y, width, height);
	}

}
