package wagemaker.co.uk.settings;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import wagemaker.co.uk.utils.CreateConfig;
import wagemaker.co.uk.utils.UpdateConfig;
public class PlayerSettings {
	
		String PlayerName = getPlayerName();
	
		private static ResourceBundle config() {
			FileInputStream configFILE;
			ResourceBundle configRes = null;
			try {
				configFILE = new FileInputStream(CreateConfig.getConfigFile());
				configRes = new PropertyResourceBundle(configFILE);
			} catch (Exception e) {
				e.printStackTrace();
			}		
			return configRes;	
		}

		final static ResourceBundle configRes = config();

		public static String getPlayerName() {
			String player = configRes.getString("playername");
			String name = "";
			if (player.equals("")) {
				while (name.equals("")) {
					name = JOptionPane.showInputDialog(null, "Player Name", null);
					if (name == null) {
						System.exit(0);
					}
				}
				try {
					UpdateConfig.changeProperty(CreateConfig.getConfigFile(), "playername", name);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				name = player;
			}
			return name;
		}
		
		public static String getPlayerStartX() {
			return configRes.getString("spawnX");
		}
		
		public static String getPlayerStartY() {
			return configRes.getString("spawnY");
			
		}
		
		public static String getWorldName() {
			return configRes.getString("world");
			
		}
		
		public void setPlayerName(String playerName) {
			PlayerName = playerName;
		}


}
