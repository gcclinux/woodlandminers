package com.example.game.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.example.game.MyGdxGame;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Minimal libGDX 2D Example");
        config.setWindowedMode(800, 600);
        new Lwjgl3Application(new MyGdxGame(), config);
    }
}
