package opengl3d.engine.system.input;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

public class MouseManager {
    private final boolean[] buttonDown = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST];
    private final boolean[] buttonPressed = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST];
    private final boolean[] buttonReleased = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST];

    private GLFWCursorPosCallback posCallback;
	private GLFWMouseButtonCallback buttonCallback;
	private GLFWScrollCallback scrollCallback;

    private double mouseX, mouseY;
    private double scrollX, scrollY;

    public MouseManager() {
        posCallback = new GLFWCursorPosCallback() {
			public void invoke(long window, double xpos, double ypos) {
				mouseX = xpos;
				mouseY = ypos;
			}
		};

        scrollCallback = new GLFWScrollCallback() {
			public void invoke(long window, double offsetx, double offsety) {
				scrollX += offsetx;
				scrollY += offsety;
			}
		};

        buttonCallback = new GLFWMouseButtonCallback() {
            public void invoke(long window, int button, int action, int mods) {
                if (button < 0 || button >= GLFW.GLFW_MOUSE_BUTTON_LAST) return;

                if (action == GLFW.GLFW_PRESS) {
                    if (!buttonDown[button]) {
                        buttonPressed[button] = true;  // True only in this frame
                    }
                    buttonDown[button] = true;
                } else if (action == GLFW.GLFW_RELEASE) {
                    buttonDown[button] = false;
                    buttonReleased[button] = true; // True only in this frame
                }
            }
        };
    }

    // note: Call this method after glfwPollEvents() and before rendering
    public void update() {
        // Reset pressed and released states for the next frame
        for (int i = 0; i < buttonDown.length; i++) {
            buttonPressed[i] = false;
            buttonReleased[i] = false;
        }
    }

    public boolean isButtonDown(int button) {
        return buttonDown[button];
    }

    public boolean isButtonPressed(int button) {
        return buttonPressed[button];
    }

    public boolean isButtonReleased(int button) {
        return buttonReleased[button];
    }

    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    public double getScrollX() {
        return scrollX;
    }

    public double getScrollY() {
        return scrollY;
    }

    public GLFWCursorPosCallback getPosCallback() {
        return posCallback;
    }

    public GLFWMouseButtonCallback getButtonCallback() {
        return buttonCallback;
    }

    public GLFWScrollCallback getScrollCallback() {
        return scrollCallback;
    }

    public void cleanup() {
        posCallback.free();
        buttonCallback.free();
        scrollCallback.free();
    }
}
