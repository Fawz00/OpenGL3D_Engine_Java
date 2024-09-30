package opengl3d;

import java.io.File;

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
		window = new Window(Settings.windowSize[0], Settings.windowSize[1], "OpenGL 3D Viewer");
		window.create();

		System.out.println(GL30.glGetString(GL30.GL_VENDOR));
		System.out.println(GL30.glGetString(GL30.GL_RENDERER));
		System.out.print("\n");

		String userDirectory = System.getProperty("user.home");
		System.out.println("Direktori Pengguna: " + userDirectory);
		listFilesAndFolders("resources/");

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

	public static void listFilesAndFolders(String directoryPath) {
		File directory = new File(directoryPath);

		// Pastikan direktori yang diberikan adalah direktori yang valid
		if (!directory.isDirectory()) {
			System.out.println("Path yang diberikan bukanlah sebuah direktori.");
			return;
		}

		// Tampilkan semua file dan folder dalam direktori
		System.out.println("Files and folders in directory: " + directoryPath);
		File[] filesAndFolders = directory.listFiles();
		if (filesAndFolders != null) {
			for (File fileOrFolder : filesAndFolders) {
				if (fileOrFolder.isDirectory()) {
					System.out.println("Folder: " + fileOrFolder.getName());
				} else {
					System.out.println("File: " + fileOrFolder.getName());
				}
			}
		} else {
			System.out.println("Tidak ada file atau folder dalam direktori.");
		}
		System.out.println();
	}

}
