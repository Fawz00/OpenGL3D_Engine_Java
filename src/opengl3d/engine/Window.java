package opengl3d.engine;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowFocusCallbackI;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLUtil;

import opengl3d.Settings;

public class Window {
	private long window;
	private int WINDOW_WIDTH;
	private int WINDOW_HEIGHT;
	private int WINDOW_LAST_WIDTH = 0;
	private int WINDOW_LAST_HEIGHT = 0;
	private int WINDOW_LAST_POSX = 0;
	private int WINDOW_LAST_POSY = 0;
	private String title;

	private int[] windowPos = new int[2];
	private boolean isFullscreen = false;
	private boolean isResized = false;
	private boolean isFocused = true;

	private GLFWWindowFocusCallbackI windowFocusCallback;
	private GLFWWindowSizeCallback windowSizeCallback;

	public Window(int width, int height, String title) {
		this.title = title;
		this.WINDOW_WIDTH = width;
		this.WINDOW_HEIGHT = height;
	}

	public void create() {
		if(!GLFW.glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		if(Settings.maximized) GLFW.glfwWindowHint(GLFW.GLFW_MAXIMIZED, GLFW.GLFW_TRUE);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE);

		window = GLFW.glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, title, 0, 0);
		if(window == 0) {
			throw new IllegalStateException("Failed to create window");
		}

		if(Settings.maximized==false) {
			GLFWVidMode videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
			windowPos[0] = (videoMode.width() - WINDOW_WIDTH) / 2;
			windowPos[1] = (videoMode.height() - WINDOW_HEIGHT) / 2;
			GLFW.glfwSetWindowPos(window, windowPos[0], windowPos[1]);
		}
		if(Settings.fullscreen) toggleFullscreen();

		loadCursor("resources/textures/ui/cursor.png");

		GLFW.glfwSetWindowSizeLimits(window, Settings.windowSize[0], Settings.windowSize[1], -1, -1);
		GLFW.glfwMakeContextCurrent(window);
		GL.createCapabilities();
		GLFW.glfwSwapInterval(Settings.vsync ? 1 : 0);

		if(Settings.enableGLDebug) GLUtil.setupDebugMessageCallback();

		new Input();

		windowFocusCallback = new GLFWWindowFocusCallbackI() {
			@Override
			public void invoke(long window, boolean focus) {
				isFocused = focus;
			}
		};
		windowSizeCallback = new GLFWWindowSizeCallback() {
			public void invoke(long window, int w, int h) {
				WINDOW_WIDTH = w;
				WINDOW_HEIGHT = h;
				isResized = true;
			}
		};

		GLFW.glfwSetInputMode(window, GLFW.GLFW_STICKY_KEYS, GLFW.GLFW_TRUE);
		GLFW.glfwSetInputMode(window, GLFW.GLFW_STICKY_MOUSE_BUTTONS, GLFW.GLFW_TRUE);
		GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);

		GLFW.glfwSetKeyCallback(window, Input.getKeyboardCallback());
		GLFW.glfwSetCharCallback(window, Input.getVirtualKeyboardCallback());
		GLFW.glfwSetCursorPosCallback(window, Input.getMouseMoveCallback());
		GLFW.glfwSetMouseButtonCallback(window, Input.getMouseButtonsCallback());
		GLFW.glfwSetScrollCallback(window, Input.getMouseScrollCallback());
		GLFW.glfwSetWindowFocusCallback(window, windowFocusCallback);
		GLFW.glfwSetWindowSizeCallback(window, windowSizeCallback);

		//GLFW.glfwSetCursorPos(window, WINDOW_WIDTH/2, WINDOW_HEIGHT/2);

		IntBuffer WINDOW_WIDTH_BUFFER = BufferUtils.createIntBuffer(1);
		IntBuffer WINDOW_HEIGHT_BUFFER = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetWindowSize(window, WINDOW_WIDTH_BUFFER, WINDOW_HEIGHT_BUFFER);
		WINDOW_WIDTH = WINDOW_WIDTH_BUFFER.get(0);
		WINDOW_HEIGHT = WINDOW_HEIGHT_BUFFER.get(0);
	}

	public void update() {
		GLFW.glfwPollEvents();
		GLFW.glfwSwapBuffers(window);
	}

	public void destroy() {
		Input.destroy();
		windowSizeCallback.free();
		GLFW.glfwDestroyWindow(window);
		GLFW.glfwTerminate();
	}

	public boolean windowShouldClose() {
		return GLFW.glfwWindowShouldClose(window);
	}
	public void setWindowShouldClose(boolean b) {
		GLFW.glfwSetWindowShouldClose(window, true);
	}
	public boolean isFocused() {
		return isFocused;
	}
	public boolean isResized() {
		return isResized;
	}
	public void setResizedStatus(boolean b) {
		isResized = b;
	}
	public void showCursor(boolean b) {
		if(b) {
			GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
		} else {
			GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
		}
	}
	public int getWidth() {
		return WINDOW_WIDTH;
	}
	public int getHeight() {
		return WINDOW_HEIGHT;
	}

	public void toggleFullscreen() {
		GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());

		IntBuffer WINDOW_WIDTH_BUFFER = BufferUtils.createIntBuffer(1);
		IntBuffer WINDOW_HEIGHT_BUFFER = BufferUtils.createIntBuffer(1);

		IntBuffer WINDOW_POSX_BUFFER = BufferUtils.createIntBuffer(1);
		IntBuffer WINDOW_POSY_BUFFER = BufferUtils.createIntBuffer(1);
		if(!isFullscreen) {

			GLFW.glfwGetWindowSize(window, WINDOW_WIDTH_BUFFER, WINDOW_HEIGHT_BUFFER);
			WINDOW_LAST_WIDTH = WINDOW_WIDTH_BUFFER.get(0);
			WINDOW_LAST_HEIGHT = WINDOW_HEIGHT_BUFFER.get(0);

			GLFW.glfwGetWindowPos(window, WINDOW_POSX_BUFFER, WINDOW_POSY_BUFFER);
			WINDOW_LAST_POSX = WINDOW_POSX_BUFFER.get(0);
			WINDOW_LAST_POSY = WINDOW_POSY_BUFFER.get(0);
		}

		if (isFullscreen) {
			GLFW.glfwSetWindowMonitor(window, 0, WINDOW_LAST_POSX, WINDOW_LAST_POSY, WINDOW_LAST_WIDTH, WINDOW_LAST_HEIGHT, 0);
			isFullscreen = false;
		} else {
			GLFW.glfwSetWindowMonitor(window, GLFW.glfwGetPrimaryMonitor(), 0, 0, vidmode.width(), vidmode.height(), vidmode.refreshRate());
			isFullscreen = true;
		}
	}

	private void loadCursor(String path) {
		try {
			BufferedImage image = ImageIO.read(new File(path));

			int width = image.getWidth();
			int height = image.getHeight();
	
			int[] pixels = new int[width * height];
			image.getRGB(0, 0, width, height, pixels, 0, width);
	
			ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
	
			for (int y = 0; y < height; y++){
				for (int x = 0; x < width; x++){
					int pixel = pixels[y * width + x];
	
					buffer.put((byte) ((pixel >> 16) & 0xFF));  // red
					buffer.put((byte) ((pixel >> 8) & 0xFF));   // green
					buffer.put((byte) (pixel & 0xFF));          // blue
					buffer.put((byte) ((pixel >> 24) & 0xFF));  // alpha
				}
			}
			buffer.flip();
	
			GLFWImage cursorImg= GLFWImage.create();
			cursorImg.width(width);
			cursorImg.height(height);
			cursorImg.pixels(buffer);
	
			int hotspotX = 0;
			int hotspotY = 1;
	
			long cursorID = GLFW.glfwCreateCursor(cursorImg, hotspotX , hotspotY);
	
			GLFW.glfwSetCursor(window, cursorID);

		} catch (IOException e) {
			e.printStackTrace();
		};
	}

}
