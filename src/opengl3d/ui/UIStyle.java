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
    public int paddingLeft;
    public int paddingRight;
    public int paddingTop;
    public int paddingBottom;
    public int marginLeft;
    public int marginRight;
    public int marginTop;
    public int marginBottom;
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
        setMargin(0);
        setPadding(0);
    }

    public void setMargin(int margin) {
        this.marginBottom = margin;
        this.marginLeft = margin;
        this.marginRight = margin;
        this.marginTop = margin;
    }
    public void setPadding(int padding) {
        this.paddingBottom = padding;
        this.paddingLeft = padding;
        this.paddingRight = padding;
        this.paddingTop = padding;
    }

}
