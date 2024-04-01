package opengl3d;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import opengl3d.audio.AudioMaster;
import opengl3d.engine.Window;
import opengl3d.utils.FpsTool;

public class Main {
	public static Window window;
	public static FpsTool fpsLimiter;
	private static Renderer renderer;

	public static void main(String[] args) {
		window = new Window(Settings.windowSize[0], Settings.windowSize[1], "OpenGL 3D Game");
		window.create();

		System.out.println(GL30.glGetString(GL30.GL_VENDOR));
		System.out.println(GL30.glGetString(GL30.GL_RENDERER));
		System.out.print("\n");

		//=============================//
		//     G A M E   L O G I C     //
		//=============================//
		AudioMaster.init();
		AudioMaster.setListenerData();
		boolean firstFrame = true;
		renderer = new Renderer();
		renderer.onCreate(window.getWidth(), window.getHeight());

		fpsLimiter = new FpsTool(Settings.fpsLimit);
		while (!window.windowShouldClose()) {
			if(window.isResized() || firstFrame) {
				renderer.onScreenSizeChanged(window.getWidth(), window.getHeight());
				window.setResizedStatus(false);
			}

			GL11.glViewport(0, 0, window.getWidth(), window.getHeight());
			GL11.glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

			renderer.onLoop();
			if(!window.isFocused()) Renderer.setPaused();
			window.update();
			firstFrame = false;
			//if(Renderer.isPaused()) fpsLimiter.setFps(60);
			fpsLimiter.end();
		}

		renderer.onDestroy();
		AudioMaster.cleanUp();
		window.destroy();
	}

}
