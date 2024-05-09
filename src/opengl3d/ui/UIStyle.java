package opengl3d.ui;

import opengl3d.utils.Color4;

public class UIStyle {
    public Color4 backgroundColor = null;
    public int backgroundTexture = 0;
    public Color4 textColor = null;
    public Color4 textOutlineColor = null;
    public Color4 borderColor = null;
    public int borderRadius = -1;
    public int borderWidth = -1;

    public UIStyle() {
    }

    public void init() {
        backgroundColor = new Color4(0x00000000);
        backgroundTexture = 0;
        textColor = new Color4(0xFFFFFFFF);
        textOutlineColor = new Color4(0x00000000);
        borderColor = new Color4(0x00000000);
        borderRadius = 0;
        borderWidth = 0;
    }
}
