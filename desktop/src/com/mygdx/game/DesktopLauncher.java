package com.mygdx.game;



import edu.cornell.gdiac.backend.GDXApp;
import edu.cornell.gdiac.backend.GDXAppSettings;
import edu.cornell.gdiac.ui.GDXRoot;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
//		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
//		config.setForegroundFPS(60);
//		config.setTitle("My GDX Game");
//		//config.setWindowedMode(1280,720);
//
//		//config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
//		new Lwjgl3Application(new MyGdxGame(), config);

		GDXAppSettings config = new GDXAppSettings();
		config.width  = 1280;
		config.height = 720;
		config.fullscreen = false;
		config.resizable = true;
		new GDXApp(new GDXRoot(), config);
	}
}
