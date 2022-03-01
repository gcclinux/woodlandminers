package wagemaker.co.uk.worlds;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import wagemaker.co.uk.gfx.Assets;

public class CheckMap implements Runnable{
	
	static int worldSizeY = Assets.getWorldSizeY();
	static int worldSizeX = Assets.getWorldSizeX();
	static int maxSand = ((worldSizeY / 10 * 2));
	static int maxDirt = ((worldSizeY / 10 * 3));
	
	static int[][] My2dArray = new int[worldSizeY][worldSizeX];
	
	private boolean running = false;
	private Thread thread;

	@Override
	public void run() {
		exec();	
	}

	private void exec() {
		if (!new File(Assets.getMapFile()).exists()){
			buildMap(worldSizeY, worldSizeX);
			addSand(worldSizeY, worldSizeX, maxSand);
			addDirt(worldSizeY, worldSizeX, maxDirt);
			writeMap(Assets.getMapFile(), My2dArray);
		}
		this.stop();
	}

	private void writeMap(String mapFile, int[][] my2dArray2) {
		try {
	        @SuppressWarnings("resource")
			BufferedWriter bw = new BufferedWriter(new FileWriter(mapFile));
			for(int i = 0; i < worldSizeY; i++){
			      for(int j = 0; j < worldSizeX; j++){
			    	  bw.write(my2dArray2[i][j] + " ");
			      }
			      bw.newLine();
			   }
	        bw.flush();
	    } catch (IOException e) {}
	}

	private void addDirt(int worldSizeY2, int worldSizeX2, int maxDirt2) {
	
		for (int m = 0; m < maxDirt2; m++){
			
			int randDirtX =  ThreadLocalRandom.current().nextInt(1, worldSizeY2);
			int randDirtY =  ThreadLocalRandom.current().nextInt(1, worldSizeY2 - 2);
			
			for (int i = 0; i < worldSizeX2; i++) {
				for (int j = 0; j < worldSizeY2; j++) {
					if ( (j == randDirtY && i == randDirtX) 
		    				|| (j == randDirtY && i == randDirtX +1)
		    				|| (j == randDirtY && i == randDirtX +2)
		    				|| (j == randDirtY && i == randDirtX +3)
		    				|| (j == randDirtY + 1 && i == randDirtX)
		    				|| (j == randDirtY + 1 && i == randDirtX +1)
		    				|| (j == randDirtY + 1 && i == randDirtX +2)
		    				|| (j == randDirtY + 1 && i == randDirtX +3)
		    				|| (j == randDirtY + 2 && i == randDirtX)
		    				|| (j == randDirtY + 2 && i == randDirtX +1)
		    				|| (j == randDirtY + 2 && i == randDirtX +2)
		    				|| (j == randDirtY + 2 && i == randDirtX +3)
		    				|| (j == randDirtY + 3 && i == randDirtX)
		    				|| (j == randDirtY + 3 && i == randDirtX +1)
		    				|| (j == randDirtY + 3 && i == randDirtX +2)
		    				|| (j == randDirtY + 3 && i == randDirtX +3)
		    				)
		    		{
		    			My2dArray[i][j] = 1;
		    		} 
		    		if (j == 0){
				    	My2dArray[i][j] = 2;
				    } else if (j == (worldSizeX2 - 1)){
				    	My2dArray[i][j] = 2;
				    } else if(i == 0){
				    	My2dArray[i][j] = 2;
				    } else if (i == (worldSizeY2 - 1)){
				    	My2dArray[i][j] = 2;
				    }
				}
			}
		}
	}

	private void addSand(int worldSizeY2, int worldSizeX2, int maxSand2) {
		
		for (int m = 0; m < maxSand2; m++){
			
			int randSandX2 =  ThreadLocalRandom.current().nextInt(1, worldSizeX2);
			int randSandY2 =  ThreadLocalRandom.current().nextInt(1, worldSizeY2 - 3);
			
			for (int i = 0; i < worldSizeX2; i++) {
				for (int j = 0; j < worldSizeY2; j++) {
					if ( (j == randSandY2 && i == randSandX2) 
		    				|| (j == randSandY2 && i == randSandX2 +1)
		    				|| (j == randSandY2 && i == randSandX2 +2)
		    				|| (j == randSandY2 && i == randSandX2 +3)
		    				|| (j == randSandY2 + 1 && i == randSandX2)
		    				|| (j == randSandY2 + 1 && i == randSandX2 +1)
		    				|| (j == randSandY2 + 1 && i == randSandX2 +2)
		    				|| (j == randSandY2 + 1 && i == randSandX2 +3)
		    				|| (j == randSandY2 + 2 && i == randSandX2)
		    				|| (j == randSandY2 + 2 && i == randSandX2 +1)
		    				|| (j == randSandY2 + 2 && i == randSandX2 +2)
		    				|| (j == randSandY2 + 2 && i == randSandX2 +3)
		    				|| (j == randSandY2 + 3 && i == randSandX2)
		    				|| (j == randSandY2 + 3 && i == randSandX2 +1)
		    				|| (j == randSandY2 + 3 && i == randSandX2 +2)
		    				|| (j == randSandY2 + 3 && i == randSandX2 +3)
		    				)
		    		{
		    			My2dArray[i][j] = 3;
		    		} 
		    		if (j == 0){
				    	My2dArray[i][j] = 3;
				    } else if (j == (worldSizeX2 - 1)){
				    	My2dArray[i][j] = 3;
				    } else if(i == 0){
				    	My2dArray[i][j] = 3;
				    } else if (i == (worldSizeY2 - 1)){
				    	My2dArray[i][j] = 3;
				    }
				}
			}
		}
	}

	private void buildMap(int worldSizeY2, int worldSizeX2) {
		
		for (int i = 0; i < worldSizeX2; i++) {
			for (int j = 0; j < worldSizeY2; j++) {
		    	if (j == 0){
		    		My2dArray[i][j] = 2;
		    	} else if (j == (worldSizeX2 - 1)){
		    		My2dArray[i][j] = 2;
		    	} else if(i == 0){
		    		My2dArray[i][j] = 2;
		    	} else if (i == (worldSizeY2 - 1)){
		    		My2dArray[i][j] = 2;
		    	} else {
		    			My2dArray[i][j] = 0;	
		    	}
		    }
		}	
	}

	public synchronized void start() {
		if(running)
			return;
		running = true;
		thread = new Thread(this);
		thread.start();
	}
	
	public synchronized void stop(){
		if(!running)
			return;
		running = false;
		Thread.currentThread().interrupt();
	}
}
