package opengl3d.ui;

import opengl3d.utils.Color4;

public class UIStyle {
    public static int ALIGN_BEGIN = 0;
    public static int ALIGN_CENTER = 0;
    public static int ALIGN_END = 0;

    public Color4 backgroundColor;
    public int backgroundTexture;
    public Color4 textColor;
    public Color4 textOutlineColor;
    public Color4 borderColor;
    public int borderRadius;
    public int borderWidth;
    public int align;
    public int textAlign;

    public UIStyle() {
        backgroundColor = new Color4(0x00000000);
        backgroundTexture = 0;
        textColor = new Color4(0x00000000);
        textOutlineColor = new Color4(0x00000000);
        borderColor = new Color4(0x00000000);
        borderRadius = 0;
        borderWidth = 0;
        align = ALIGN_BEGIN;
        textAlign = ALIGN_BEGIN;
    }

}
