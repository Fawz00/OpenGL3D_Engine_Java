package opengl3d.engine.system.input;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

public class KeyboardManager {
    private final boolean[] keyDown = new boolean[GLFW.GLFW_KEY_LAST];
    private final boolean[] keyPressed = new boolean[GLFW.GLFW_KEY_LAST];
    private final boolean[] keyReleased = new boolean[GLFW.GLFW_KEY_LAST];

    private boolean isCtrlDown = false;
    private boolean isShiftDown = false;
    private boolean isAltDown = false;

    private GLFWKeyCallback callback;

    private int rawKey; // For raw key input, if needed
    private int rawScancode; // For raw scancode input, if needed
    private int rawAction; // For raw action input, if needed
    private int rawMods; // For raw modifier input, if needed

    public KeyboardManager() {
        callback = new GLFWKeyCallback() {
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key < 0 || key >= GLFW.GLFW_KEY_LAST) return;

                // Store raw input if needed
                rawKey = key;
                rawScancode = scancode;
                rawAction = action;
                rawMods = mods;

                // Update modifier states
                isCtrlDown = (mods & GLFW.GLFW_MOD_CONTROL) != 0;
                isShiftDown = (mods & GLFW.GLFW_MOD_SHIFT) != 0;
                isAltDown = (mods & GLFW.GLFW_MOD_ALT) != 0;

                if (action == GLFW.GLFW_PRESS) {
                    if (!keyDown[key]) {
                        keyPressed[key] = true;  // True only in this frame
                    }
                    keyDown[key] = true;
                } else if (action == GLFW.GLFW_RELEASE) {
                    keyDown[key] = false;
                    keyReleased[key] = true; // True only in this frame
                }
            }
        };
    }

    // note: Call this method after glfwPollEvents() and before rendering
    public void update() {
        // Reset pressed and released states for the next frame
        for (int i = 0; i < keyDown.length; i++) {
            keyPressed[i] = false;
            keyReleased[i] = false;
        }
    }

    public boolean isKeyDown(int key) {
        return keyDown[key];
    }

    public boolean isKeyPressed(int key) {
        return keyPressed[key];
    }

    public boolean isKeyReleased(int key) {
        return keyReleased[key];
    }

    public GLFWKeyCallback getCallback() {
        return callback;
    }

    /**
     * Returns the raw keyboard input data as an array of integers.
     * <p>
     * The returned array contains the following elements in order:
     * <ul>
     *   <li>rawKey - the key code of the key event</li>
     *   <li>rawScancode - the hardware scancode of the key</li>
     *   <li>rawAction - the action performed (e.g., press, release)</li>
     *   <li>rawMods - the modifier keys active during the event</li>
     * </ul>
     *
     * @return an array of integers representing the raw keyboard input data
     */
    public int[] getRawInput() {
        return new int[]{rawKey, rawScancode, rawAction, rawMods};
    }

    public void cleanup() {
        callback.free();
    }

    public boolean isCtrlDown() {
        return isCtrlDown;
    }

    public boolean isShiftDown() {
        return isShiftDown;
    }

    public boolean isAltDown() {
        return isAltDown;
    }
}
