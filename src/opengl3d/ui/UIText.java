package opengl3d.ui;

import opengl3d.utils.text.Font;

public class UIText extends UIComponent {
    private String text;
    private Font font;

    public UIText(String id, Point2D pos, Point2D size) {
		super(id, pos, size);
        init();
	}
	public UIText(String id, int x, int y, int sx, int sy) {
		super(id, x, y, sx, sy);
        init();
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
