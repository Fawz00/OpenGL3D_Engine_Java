package opengl3d.ui;

import opengl3d.utils.Color4;
import opengl3d.utils.Point2;
import opengl3d.utils.text.Font;

public class UIText extends UIComponent {
    private String text;
    private Font font;

    public UIText(String id, int sx, int sy) {
		super(id, normalStyle(), null, new Point2(sx, sy));
        init();
	}
    public UIText(String id, Point2 size) {
		super(id, normalStyle(), null, size);
        init();
	}
    public UIText(String id, String text, int sx, int sy) {
		super(id, normalStyle(), null, new Point2(sx, sy));
        this.text = text;
        init();
	}
    public UIText(String id, String text, Point2 size) {
		super(id, normalStyle(), null, size);
        this.text = text;
        init();
	}
    public UIText(String id, String text, Point2 pos, Point2 size) {
		super(id, normalStyle(), pos, size);
        this.text = text;
        init();
	}
	public UIText(String id, String text, int x, int y, int sx, int sy) {
		super(id, normalStyle(), x, y, sx, sy);
        this.text = text;
        init();
	}
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
        Point2 position = this.getPosition();
        Point2 size = this.getSize();
        font.drawText(UIRenderer.getTextShader(), position.x, position.y, size.x, size.y, this.getRotation(), text, 0xFFFFFFFF);
        super.draw();
    }

}
