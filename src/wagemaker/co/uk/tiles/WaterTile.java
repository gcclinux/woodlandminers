package wagemaker.co.uk.tiles;

import wagemaker.co.uk.gfx.Assets;

public class WaterTile extends Tile {

	public WaterTile(int id) {
		super(Assets.water, id);
	}
	
	@Override
	public boolean isSolid(){
		return true;
	}
}
