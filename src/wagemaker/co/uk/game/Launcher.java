package wagemaker.co.uk.game;

import java.awt.Dimension;
import java.awt.Toolkit;

import wagemaker.co.uk.settings.GameSettings;
import wagemaker.co.uk.utils.CreateConfig;
import wagemaker.co.uk.worlds.CheckMap;

public class Launcher {

	public static void main(String[] args){
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		
		CreateConfig.main(null);
		
		CheckMap map = new CheckMap();
		map.start();
		
		Game game = new Game(GameSettings.getName(), ((dim.width / 4) * 3 ), ((dim.height / 4) * 3));
		game.start();

	}
}
