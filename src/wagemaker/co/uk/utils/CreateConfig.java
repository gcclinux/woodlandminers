package wagemaker.co.uk.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CreateConfig {
	
	private static String homeDir = System.getProperty("user.home");
	private static String GameDir = "wagemaker";
	private static String ConfigPath;
	private static String ConfigName = "config.db";
	@SuppressWarnings("unused")
	private static String ConfigFile;
	private static String osName = System.getProperty("os.name").toLowerCase();
	
	public static String main(String[] args){

		if (osName.indexOf("nux") >= 0 || osName.indexOf("nix") >= 0) {			
			File file = new File(homeDir +"/"+"."+GameDir+"/");
			if (!file.exists()) {
				if (file.mkdirs()) {
				} else {
					System.err.println(" @CreateConfig -> Failed to create directory! ("+file+")\n");
				}
			} else {
				ConfigPath = (homeDir +"/"+"."+GameDir+"/");
			}		
		} else if (osName.equals("windows xp")) {
			
			File file = new File(homeDir + "\\Application Data\\"+GameDir+"\\");
			if (!file.exists()) {
				if (file.mkdirs()) {
				} else {
					System.err.println(" @CreateConfig -> Failed to create directory! ("+file+")\n");
				}
			} else {
				ConfigPath = (homeDir + "\\Application Data\\"+GameDir+"\\"+ConfigName);
			}
		} else if (osName.equals("windows 7") || osName.equals("windows 8") || osName.equals("windows 8.1") || osName.equals("windows 10")) {
			
			File file = new File(homeDir + "\\AppData\\Roaming\\"+GameDir+"\\");

			if (!file.exists()) {
				if (file.mkdirs()) {
				} else {
					System.err.println(" @CreateConfig -> Failed to create directory! ("+file+")\n");
				}
			} else {
				ConfigPath = (homeDir + "\\AppData\\Roaming\\"+GameDir+"\\");
			}
			
		} else {
			ConfigPath = (homeDir + "\\"+GameDir+"\\");
		}
		return ConfigPath;
	}
	
	
	
	public static String getConfigPath() {
		if (osName.indexOf("nux") >= 0 || osName.indexOf("nix") >= 0) {
			return ConfigPath = (homeDir +"/"+"."+GameDir+"/");
		} else if (osName.equals("windows xp")){
			return ConfigPath = (homeDir + "\\Application Data\\"+GameDir+"\\");
		} else {
			return ConfigPath = (homeDir + "\\AppData\\Roaming\\"+GameDir+"\\");
		}
	}


	public static String getOsName() {
		return osName;
	}

	public static String getConfigFile() {

		if (new File(ConfigPath+ConfigName).exists()){
			return ConfigFile = ConfigPath+ConfigName;
		} else {
			try {
				@SuppressWarnings("resource")
				BufferedWriter bw = new BufferedWriter(new FileWriter(ConfigPath+ConfigName.toString()));
				bw.write("playername = ");
				bw.newLine();
				bw.write("spawnX = 1100");
				bw.newLine();
				bw.write("spawnY = 1200");
				bw.newLine();
				bw.write("world = default.world");
				bw.newLine();
				bw.write("server = 127.0.0.1");
				bw.newLine();
				bw.write("serverport = 1331");
				bw.newLine();
				bw.write("clientport = 1332");
				bw.flush();
			} catch (IOException e) {}
		}
		return ConfigFile = ConfigPath+ConfigName;
	}



	public static void setConfigFile(String configFile) {
		ConfigFile = configFile;
	}

}
