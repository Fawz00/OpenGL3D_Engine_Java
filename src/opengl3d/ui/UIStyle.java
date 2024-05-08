package opengl3d.ui;

import opengl3d.utils.Color4;

public class UIStyle {
    public Color4 backgroundColor;
    public int backgroundTexture;
    public Color4 textColor;
    public Color4 textOutlineColor;
    public Color4 borderColor;
    public int borderRadius;
    public int borderWidth;

    public UIStyle() {
        backgroundColor = new Color4(0x00000000);
        backgroundTexture = 0;
        textColor = new Color4(0xFFFFFFFF);
        textOutlineColor = new Color4(0x00000000);
        borderColor = new Color4(0x00000000);
        borderRadius = 0;
        borderWidth = 0;
    }
}
