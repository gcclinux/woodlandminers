package wagemaker.co.uk.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Utils {
	
	private static ResourceLoader loader;
	
	public static String loadFileAsString(String path){
		
		loader = new ResourceLoader(path);
		StringBuilder builder = new StringBuilder();
		
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(loader.getResource()));
			String line;
			while ((line = br.readLine()) != null)
				builder.append(line + "\n");
			br.close();
				
		} catch(IOException e){
			e.printStackTrace();
		}
		
		return builder.toString();
	}
	
	public static int parseInt(String number){
		try {
			return Integer.parseInt(number);
		} catch(NumberFormatException e){
			e.printStackTrace();
		}
		return 0;
	}

}
