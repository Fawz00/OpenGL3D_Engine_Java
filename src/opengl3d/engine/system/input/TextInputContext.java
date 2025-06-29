package opengl3d.engine.system.input;

import org.lwjgl.glfw.GLFW;
import opengl3d.engine.ISubmitListener;

/**
 * TextInputContext manages the state and behavior of a text input field,
 * including user input, cursor position, text selection, and clipboard operations.
 * <p>
 * Features:
 * <ul>
 *   <li>Handles character insertion, deletion, and navigation (left, right, home, end).</li>
 *   <li>Supports text selection, copy, cut, and paste operations.</li>
 *   <li>Tracks cursor position and selection range.</li>
 *   <li>Supports editable and read-only modes.</li>
 *   <li>Can be activated or deactivated for input handling.</li>
 * </ul>
 *
 * Usage:
 * <pre>
 *     // Create a TextInputContext and set it up with a KeyboardManager
 *     TextInputContext context = new TextInputContext();
 * 
 *     TypingManager typingManager = new TypingManager(keyboardManager);
 *     GLFW.glfwSetCharCallback(window, typingManager.getCallback());
 * 
 *     typingManager.setInputContext(context);
 *     String text = context.getText();
 * </pre>
 */
public class TextInputContext {
    private final StringBuilder userInput = new StringBuilder();
    private int cursorPos; // Current position of the text cursor
    private int[] selectionIndex = new int[2]; // [start, end] of selection
    private ISubmitListener submitListener;

    private boolean isEditable = true; // Whether the text input is editable
    private boolean isMultiline = false; // Whether the text input supports multiple lines
    private boolean active = true; // Whether the text input is currently active

    public TextInputContext() {
        this(true); // Default to editable
    }
    public TextInputContext(boolean isEditable) {
        this.isEditable = isEditable;
        this.cursorPos = 0;
        clearSelection();
    }

    public void handleKey(long window, int codepoint, KeyboardManager keyboardManager) {
        if(!active) return; // If not active, do nothing
        int key = keyboardManager.getRawInput()[0];
        int keyAction = keyboardManager.getRawInput()[2];
        
        if(keyAction == GLFW.GLFW_PRESS) {
            if(keyboardManager.isCtrlDown()) {
                if (key == GLFW.GLFW_KEY_A) {
                    selectAll();
                    return;
                } else if (key == GLFW.GLFW_KEY_C) {
                    copySelection(window);
                    return;
                } else if (key == GLFW.GLFW_KEY_V) {
                    pasteFromClipboard(window);
                    return;
                } else if (key == GLFW.GLFW_KEY_X) {
                    cutSelection(window);
                    return;
                }
            }

            switch (key) {
                case GLFW.GLFW_KEY_BACKSPACE: {
                    if (hasSelection()) {
                        deleteSelectedIfAny();
                    } else if (cursorPos > 0) {
                        userInput.deleteCharAt(--cursorPos);
                    }
                    return;
                }

                case GLFW.GLFW_KEY_DELETE: {
                    if (hasSelection()) {
                        deleteSelectedIfAny();
                    } else if (cursorPos < userInput.length()) {
                        userInput.deleteCharAt(cursorPos);
                    }
                    return;
                }

                case GLFW.GLFW_KEY_LEFT: {
                    moveCursorLeft(keyboardManager.isShiftDown());
                    return;
                }

                case GLFW.GLFW_KEY_RIGHT: {
                    moveCursorRight(keyboardManager.isShiftDown());
                    return;
                }

                case GLFW.GLFW_KEY_HOME: {
                    setCursorPosition(0);
                    return;
                }

                case GLFW.GLFW_KEY_END: {
                    setCursorPosition(userInput.length());
                    return;
                }

                case GLFW.GLFW_KEY_ENTER: {
                    if (isMultiline && isEditable) {
                        deleteSelectedIfAny();
                        insertChar('\n');
                    } else {
                        // If not multiline, treat Enter as submit
                        if (submitListener != null) {
                            submitListener.onSubmit(getText());
                        }
                    }
                    return;
                }
            }
        }

        insertChar(codepoint);
    }

    public void insertChar(int codepoint) {
        if (!isEditable) return; // If not editable, do nothing
        char[] chars = Character.toChars(codepoint);
        deleteSelectedIfAny();
        userInput.insert(cursorPos, chars);
        cursorPos += chars.length;
    }

    public void selectAll() {
        selectionIndex[0] = 0;
        selectionIndex[1] = userInput.length();
        cursorPos = userInput.length();
    }

    private void clearSelection() {
        selectionIndex[0] = -1;
        selectionIndex[1] = -1;
    }

    private boolean hasSelection() {
        return selectionIndex[0] != selectionIndex[1];
    }

    private void deleteSelectedIfAny() {
        if (!isEditable) return; // If not editable, do nothing
        if (!hasSelection()) return;
        int start = Math.min(cursorPos, selectionIndex[0]);
        int end = Math.max(cursorPos, selectionIndex[1]);
        userInput.delete(start, end);
        cursorPos = start;
        clearSelection();
    }

    public void copySelection(long window) {
        if (!active) return; // If not active, do nothing
        if (!hasSelection()) return;
        int start = Math.min(selectionIndex[0], selectionIndex[1]);
        int end = Math.max(selectionIndex[0], selectionIndex[1]);
        String selectedText = userInput.substring(start, end);
        GLFW.glfwSetClipboardString(window, selectedText);
    }

    public void cutSelection(long window) {
        if (!active) return; // If not active, do nothing
        if (!hasSelection()) return;
        copySelection(window); // Copy to clipboard first
        deleteSelectedIfAny(); // Then delete the selection
    }

    public void pasteFromClipboard(long window) {
        if (!active) return; // If not active, do nothing
        if (!isEditable) return; // If not editable, do nothing
        String clipboardContent = GLFW.glfwGetClipboardString(window);
        if (clipboardContent == null) return; // If clipboard is empty, do nothing
        if (hasSelection()) deleteSelectedIfAny();
        userInput.insert(cursorPos, clipboardContent);
        cursorPos = cursorPos + clipboardContent.length();
        clearSelection();
    }

    private void moveCursorLeft(boolean shiftDown) {
        if (cursorPos <= 0) return;

        if (shiftDown) {
            int anchor = hasSelection() ? selectionIndex[0] : cursorPos;
            cursorPos--;
            selectionIndex[0] = Math.min(anchor, cursorPos);
            selectionIndex[1] = Math.max(anchor, cursorPos);
        } else {
            cursorPos--;
            clearSelection();
        }
    }

    private void moveCursorRight(boolean shiftDown) {
        if (cursorPos >= userInput.length()) return;

        if (shiftDown) {
            int anchor = hasSelection() ? selectionIndex[0] : cursorPos;
            cursorPos++;
            selectionIndex[0] = Math.min(anchor, cursorPos);
            selectionIndex[1] = Math.max(anchor, cursorPos);
        } else {
            cursorPos++;
            clearSelection();
        }
    }

    public String getText() {
        return userInput.toString();
    }

    public int[] getSelection() {
        return selectionIndex;
    }

    public void setCursorPosition(int position) {
        if (position < 0) {
            cursorPos = 0;
        } else if (position > userInput.length()) {
            cursorPos = userInput.length();
        } else {
            cursorPos = position;
        }
        clearSelection();
    }

    public void setSubmitListener(ISubmitListener listener) {
        this.submitListener = listener;
    }

    public int getCursorPosition() {
        return cursorPos;
    }
}
