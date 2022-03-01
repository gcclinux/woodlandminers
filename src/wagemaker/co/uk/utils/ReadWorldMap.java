package wagemaker.co.uk.utils;

import java.io.File;
import java.util.Scanner;

public class ReadWorldMap {

	@SuppressWarnings("resource")
	public static int[][] create2DIntMatrixFromFile(String filename) throws Exception {
		int[][] matrix = {{1}, {2}};

		File inFile = new File(filename);
		Scanner in = new Scanner(inFile);

		int intLength = 0;
		String[] length = in.nextLine().trim().split("\\s+");
		  for (int i = 0; i < length.length; i++) {
		    intLength++;
		  }

		in.close();

		matrix = new int[intLength][intLength];
		in = new Scanner(inFile);

		int lineCount = 0;
		while (in.hasNextLine()) {
		  String[] currentLine = in.nextLine().trim().split("\\s+"); 
		     for (int i = 0; i < currentLine.length; i++) {
		        matrix[lineCount][i] = Integer.parseInt(currentLine[i]);    
		            }
		  lineCount++;
		 }                                 
		 return matrix;
		}
}
