package wagemaker.co.uk.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;

import wagemaker.co.uk.gfx.Assets;

public class ResourceLoader
{
    private String filePath;

    public ResourceLoader(String filePath)
    {
        this.filePath = filePath;
    }

    public InputStream getResource() throws NoSuchFileException
    {
        ClassLoader classLoader = this.getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(filePath);

        if(inputStream == null)
        {
        	try {
				inputStream = new FileInputStream(new File(Assets.getMapFile()));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
        }

        return inputStream;
    }
}