package opengl3d.ui;

import opengl3d.utils.Color4;
import opengl3d.utils.Point2;
import opengl3d.utils.text.Font;

public class UIText extends UIComponent {
    private String text;
    private Font font;

    public UIText(String id, Point2 pos, Point2 size) {
		super(id, normalStyle(), pos, size);
        init();
	}
	public UIText(String id, int x, int y, int sx, int sy) {
		super(id, normalStyle(), x, y, sx, sy);
        init();
	}
    public static UIStyle normalStyle() {
        UIStyle s = new UIStyle();
        s.textColor = new Color4(0xFFFFFFFF);
        return s;
    }
    private void init() {
        font = UIRenderer.getFont();
        text = "";
    }

    public void setText(String text) {
        this.text = text;
    }
    public void setFont(Font font) {
        this.font = font;
    }

    @Override
    public void draw() {
        Point2 position = super.getPosition();
        Point2 size = super.getSize();
        font.drawText(UIRenderer.getTextShader(), position.x, position.y, size.x, size.y, super.getRotation(), text, 0xFFFFFFFF);
        super.draw();
    }

}
