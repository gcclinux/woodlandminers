package wagemaker.uk.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import wagemaker.uk.MyGdxGame;

public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Woodlander");
        config.setWindowedMode(1920, 1080);
        new Lwjgl3Application(new MyGdxGame(), config);
    }
}