package opengl3d.ui;

import opengl3d.utils.Color4;
import opengl3d.utils.Point2D;
import opengl3d.utils.text.Font;

public class UIText extends UIComponent {
    private String text;
    private Font font;

    public UIText(String id, Point2D pos, Point2D size) {
		super(id, initStyle(), pos, size);
        init();
	}
	public UIText(String id, int x, int y, int sx, int sy) {
		super(id, initStyle(), x, y, sx, sy);
        init();
	}
    public static UIStyle initStyle() {
        UIStyle s = new UIStyle();
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
        Point2D position = super.getPosition();
        Point2D size = super.getSize();
        font.drawText(UIRenderer.getTextShader(), position.x, position.y, size.x, size.y, super.getRotation(), text, 0xFFFFFFFF);
        super.draw();
    }

}
