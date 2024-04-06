package opengl3d.engine;

import java.io.File;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import opengl3d.Main;
import opengl3d.Renderer;
import opengl3d.Settings;

public class Input {
	private static boolean[] keysDown = new boolean[GLFW.GLFW_KEY_LAST];
	private static boolean[] buttonsDown = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST];
	private static double mouseX, mouseY;
	private static double scrollX, scrollY;

	private static StringBuilder userInput = new StringBuilder();
	private static Vector<String> inputHistory = new Vector<>();
	private static int textCursorPos = 0;
	private static boolean isTyping;
	
	private static GLFWKeyCallback keyboard;
	private static GLFWCharCallback virtualKeyboard;
	private static GLFWCursorPosCallback mouseMove;
	private static GLFWMouseButtonCallback mouseButtons;
	private static GLFWScrollCallback mouseScroll;
	
	public Input() {
		isTyping = false;
		keyboard = new GLFWKeyCallback() {
			public void invoke(long window, int key, int scancode, int action, int mods) {
				//System.out.println( GLFW.glfwGetKeyName(key, scancode) );
				if(key >= 0 && key <= GLFW.GLFW_KEY_LAST) {
					//System.out.println( Integer.toHexString(key) );
					if(key == GLFW.GLFW_KEY_DELETE && action != GLFW.GLFW_RELEASE) {
						Main.window.setWindowShouldClose(true);
						return;
					}
					if(key == GLFW.GLFW_KEY_F11 && action == GLFW.GLFW_PRESS) {
						Main.window.toggleFullscreen();
						return;
					}
					if(key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS) {
						Renderer.togglePauseStatus();
						return;
					}
					// if(key == GLFW.GLFW_KEY_K && action == GLFW.GLFW_PRESS && Renderer.isPaused()) {
					// 	JFrame frame = new JFrame("Pilih File");
					// 	JFileChooser fileChooser = new JFileChooser();
					// 	fileChooser.setFileFilter(new FileNameExtensionFilter("File Text (*.txt)", "txt"));
					// 	fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
					// 	int result = fileChooser.showOpenDialog(frame);
					// 	if (result == JFileChooser.APPROVE_OPTION) {
					// 			String filePath = fileChooser.getSelectedFile().getAbsolutePath();
					// 			System.out.println("File yang dipilih: " + filePath);
					// 	} else {
					// 			System.out.println("Tidak ada file yang dipilih.");
					// 	}
					// 	return;
					// }

					if(isTyping) {
						if(action != GLFW.GLFW_RELEASE) handleTyping(key, false, scancode);
					} else {

						keysDown[key] = (action != GLFW.GLFW_RELEASE);
						if(key == Settings.keyToggleCamera && action == GLFW.GLFW_PRESS) Renderer.camera.toggleCameraMode();
						if(key == Settings.keyTogglePhysics && action == GLFW.GLFW_PRESS) {
							Settings.physics=!Settings.physics;
						}

					}
				} else {
					System.out.println("Unknow keyboard input detected: " + key);
				}
			}
		};

		virtualKeyboard = new GLFWCharCallback() {
			public void invoke(long window, int codepoint) {
				if(isTyping) handleTyping(0, true, codepoint);
			}
		};

		mouseMove = new GLFWCursorPosCallback() {
			public void invoke(long window, double xpos, double ypos) {
				mouseX = xpos;
				mouseY = ypos;
			}
		};
		
		mouseButtons = new GLFWMouseButtonCallback() {
			public void invoke(long window, int button, int action, int mods) {
				buttonsDown[button] = (action != GLFW.GLFW_RELEASE);
			}
		};
		
		mouseScroll = new GLFWScrollCallback() {
			public void invoke(long window, double offsetx, double offsety) {
				scrollX += offsetx;
				scrollY += offsety;
			}
		};
	}

	private static void handleTyping(int key, boolean isCharInput, int code) {
		if(!isCharInput) {
			if(key == GLFW.GLFW_KEY_ENTER) {
				inputHistory.add(userInput.toString());
				textCursorPos = 0;
				userInput.setLength(0);
			} else if(key == GLFW.GLFW_KEY_BACKSPACE && textCursorPos != 0) {
				userInput.deleteCharAt(textCursorPos - 1);
				if(textCursorPos!=0) textCursorPos--;
			} else if(key == GLFW.GLFW_KEY_LEFT && textCursorPos != 0) {
				textCursorPos--;
			} else if(key == GLFW.GLFW_KEY_RIGHT && textCursorPos != userInput.length()) {
				textCursorPos++;
			}
		} else {
//			String keyText = GLFW.glfwGetKeyName(key, code);
//			if(key == GLFW.GLFW_KEY_SPACE) keyText = " ";
//			if(keyText != null && !keyText.equals("")) userInput.append(keyText);
			char[] text = Character.toChars(code);
			userInput.insert(textCursorPos, text);
			textCursorPos += text.length;
		}
	}

	public static void isTyping(boolean a) {
		isTyping = a;
	}

	public static void clearAllLines() {
		if(inputHistory.size() > 0) inputHistory.clear();
	}

	public static String getLine(int index) {
		if(index >= 0 && index < inputHistory.size()) {
			return inputHistory.get(index);
		}
		return "";
	}

	public static int getTextCursorPosition() {
		return textCursorPos;
	}
	
	public static int getLineCount() {
		return inputHistory.size();
	}

	public static String getRawLine() {
		return userInput.toString();
	}

	public static boolean isKeyDown(int key) {
		return keysDown[key];
	}

	public static boolean isButtonDown(int button) {
		return buttonsDown[button];
	}

	public static void destroy() {
		keyboard.free();
		mouseMove.free();
		mouseButtons.free();
		mouseScroll.free();
	}

	public static double getMouseX() {
		return mouseX;
	}

	public static double getMouseY() {
		return mouseY;
	}
	
	public static double getScrollX() {
		return scrollX;
	}

	public static double getScrollY() {
		return scrollY;
	}

	public static GLFWKeyCallback getKeyboardCallback() {
		return keyboard;
	}

	public static GLFWCharCallback getVirtualKeyboardCallback() {
		return virtualKeyboard;
	}

	public static GLFWCursorPosCallback getMouseMoveCallback() {
		return mouseMove;
	}

	public static GLFWMouseButtonCallback getMouseButtonsCallback() {
		return mouseButtons;
	}
	
	public static GLFWScrollCallback getMouseScrollCallback() {
		return mouseScroll;
	}
}