package ru.fierylynx.old43.desktop;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import ru.fierylynx.old43.Main;

/** Launches the desktop (LWJGL) application. */
public class DesktopLauncher {
    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = getDefaultConfiguration();
        boolean debug = false;
        for (String arg : args) {
            if (arg.equals("-debug"))
                debug = true;
            if (arg.contains("-w"))
                cfg.width = Integer.parseInt(arg.substring(2));
            if (arg.contains("-h"))
                cfg.height = Integer.parseInt(arg.substring(2));
            if (arg.equals("-f"))
                cfg.fullscreen = true;
        }
        new LwjglApplication(new Main(debug), cfg);
    }

    private static LwjglApplicationConfiguration getDefaultConfiguration() {
        LwjglApplicationConfiguration configuration = new LwjglApplicationConfiguration();
        configuration.title = "OLD43";
        configuration.width = 1280;
        configuration.height = 720;
        for (int size : new int[] { 128, 64, 32, 16 }) {
            configuration.addIcon("libgdx" + size + ".png", FileType.Internal);
        }
        return configuration;
    }
}