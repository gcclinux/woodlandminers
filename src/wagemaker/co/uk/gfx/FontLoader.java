package wagemaker.co.uk.gfx;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import wagemaker.co.uk.utils.ResourceLoader;

public class FontLoader {
	
	private static ResourceLoader loader;

	public static Font loadFont(String path, float size){

	       try {
	    	   loader = new ResourceLoader(path);
	    	   return Font.createFont(Font.TRUETYPE_FONT, loader.getResource()).deriveFont(Font.PLAIN, size);
		} catch (NoSuchFileException e) {
			e.printStackTrace();
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;	
	}
}
