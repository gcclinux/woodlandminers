package wagemaker.co.uk.game;

import java.awt.Graphics;
import java.awt.image.BufferStrategy;

import wagemaker.co.uk.entities.Entity;
import wagemaker.co.uk.gfx.Assets;
import wagemaker.co.uk.gfx.Display;
import wagemaker.co.uk.gfx.GameCamera;
import wagemaker.co.uk.input.KeyManager;
import wagemaker.co.uk.input.MouseManager;
import wagemaker.co.uk.states.GameState;
import wagemaker.co.uk.states.MenuState;
import wagemaker.co.uk.states.State;

public class Game implements Runnable{
	
	private Display display;
	private int width, height;
	public String title;
	
	private boolean running = false;
	private Thread thread;
	
	private BufferStrategy bs;
	private Graphics g;
	
	//States
	public State gameState;
	private State menuState;
	
	//Input
	private KeyManager keyManager;
	private MouseManager mouseManager;
	
	//Camera
	private GameCamera gameCamera;
	
	//Handle & MultiPlayer
	private Handler handler;
	
	public Game(String title, int width, int height){
		this.width = width;
		this.height = height;
		this.title = title;	
		keyManager = new KeyManager();
		mouseManager = new MouseManager();
	}
	
	private void init() {
		display = new Display(width, height);
		display.getFrame().addKeyListener(keyManager);
		display.getFrame().addMouseListener(mouseManager);
		display.getFrame().addMouseMotionListener(mouseManager);
		display.getCanvas().addMouseListener(mouseManager);
		display.getCanvas().addMouseMotionListener(mouseManager);
		Assets.init();
		
		handler = new Handler(this);
		gameCamera = new GameCamera(handler,0,0);
		
				
		gameState = new GameState(handler);
		menuState = new MenuState(handler);
	
		State.setState(menuState);	
	}

	private void tick(){
		keyManager.tick();
		
		if(State.getState() != null)
			State.getState().tick();
	}


	private void render(){
		bs = display.getCanvas().getBufferStrategy();
		if(bs == null){
			display.getCanvas().createBufferStrategy(3);
			return;
		}
		g = bs.getDrawGraphics();
		
		//Clear Screen
		
		g.clearRect(0, 0, width, height);
		g.setColor(Assets.GREEN);
		g.fillRect(0, 0, width, height);
		//Draw Here!
		
		if(State.getState() != null)
			State.getState().render(g);
		
		//End draw!
		bs.show();
		g.dispose();
	}
	public void run(){
		
		init();
		
		int fps = 60;
		double timePerTick = 1000000000 / fps;
		double delta = 0;
		long now;
		long lastTime = System.nanoTime();
		long timer = 0;
		@SuppressWarnings("unused")
		int ticks = 0;
		
		while(running){
			now = System.nanoTime();
			delta += (now - lastTime) / timePerTick;
			timer += now - lastTime;
			lastTime = now;
			int zombieCount = 0;
			
			if (delta >= 1){
				tick();
				render();
				ticks++;
				delta --;
			}
			
			if (timer >= 1000000000){
				for (Entity e : handler.getLevel01().getEntityManager().getEntities()){
					if(e.getClass().getSimpleName().equals("Zombie")){
						zombieCount++;
					} 
				}
				
				display.getFrame().setTitle(title+" - "	+(int)(handler.getLevel01().getEntityManager().getPlayer().getX())
						+"x"+(int)(handler.getLevel01().getEntityManager().getPlayer().getY())
						+" - "
						+ "Z: ("+zombieCount
						+ ") - "
						+ "H: ("+handler.getLevel01().getEntityManager().getPlayer().getHealth()
						+ ")"
						);
				ticks = 0;
				timer = 0;
			}
		}
		
		stop();
	}
	
	public KeyManager getKeyManager(){
		return keyManager;
	}
	
	public MouseManager getMouseManager(){
		return mouseManager;
	}
	
	public GameCamera getGameCamera(){
		return gameCamera;
	}
	
	public int getWidth(){
		return width;
	}
	
	public int getHeight(){
		return height;
	}

	public synchronized void start(){
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
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
