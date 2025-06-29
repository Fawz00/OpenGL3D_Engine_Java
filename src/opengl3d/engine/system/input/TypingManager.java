package opengl3d.engine.system.input;

import org.lwjgl.glfw.GLFWCharCallback;

public class TypingManager {
    private TextInputContext textInputContext;
    private GLFWCharCallback callback;

    public TypingManager(KeyboardManager keyboardManager) {
        textInputContext = new TextInputContext();
        callback = new GLFWCharCallback() {
            @Override
            public void invoke(long window, int codepoint) {
                if (codepoint == 0) return; // Ignore null characters
                textInputContext.handleKey(window, codepoint, keyboardManager);
            }
        };
    }

    public void setInputContext(TextInputContext context) {
        textInputContext = context;
    }

    public TextInputContext getTextInputContext() {
        return textInputContext;
    }

    public GLFWCharCallback getCallback() {
        return callback;
    }

    public void cleanup() {
        if (callback != null) {
            callback.free();
            callback = null;
        }
        textInputContext = null;
    }
}
