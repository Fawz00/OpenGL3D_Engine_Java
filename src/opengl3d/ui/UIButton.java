package opengl3d.ui;

import opengl3d.engine.Input;
import opengl3d.utils.Color4;
import opengl3d.utils.Point2;
import opengl3d.utils.text.Font;

public class UIButton extends UIComponent {
    private String text;
    private Font font;

	public UIButton(String id, Point2 pos, Point2 size) {
		super(id, normalStyle(), pos, size);
        init();
	}
	public UIButton(String id, int x, int y, int sx, int sy) {
		super(id, normalStyle(), x, y, sx, sy);
        init();
	}
    private void init() {
        font = UIRenderer.getFont();
        text = "";

        super.setStyleOnHover(onHoverStyle());
        super.setStyleOnClick(onClickStyle());
        Input.setOnHoverEventListener(this);
        Input.setOnClickEventListener(this);
        Input.setOnReleaseEventListener(this);
    }
    public static UIStyle normalStyle() {
        UIStyle s = new UIStyle();
        s.backgroundColor = new Color4(0xFFFFFF80);
        s.borderColor = new Color4(0xFFFFFFFF);
        s.borderRadius = 10;
        s.borderWidth = 4;
        return s;
    }
    public static UIStyle onHoverStyle() {
        UIStyle s = new UIStyle();
        s.backgroundColor = new Color4(0x80808080);
        s.borderColor = new Color4(0x808080FF);
        s.borderRadius = 10;
        s.borderWidth = 4;
        return s;
    }
    public static UIStyle onClickStyle() {
        UIStyle s = new UIStyle();
        s.backgroundColor = new Color4(0x00FF8080);
        s.borderColor = new Color4(0x00FF80FF);
        s.borderRadius = 10;
        s.borderWidth = 4;
        return s;
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
