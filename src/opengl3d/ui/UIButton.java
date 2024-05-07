package opengl3d.ui;

public class UIButton extends UIComponent {
    private String text;

	public UIButton(String id, Point2D pos, Point2D size) {
		super(id, pos, size);
	}
	public UIButton(String id, int x, int y, int sx, int sy) {
		super(id, x, y, sx, sy);
	}

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void draw() {
        Point2D position = super.getPosition();
        Point2D size = super.getSize();
        UIRenderer.getFont().drawText(UIRenderer.getTextShader(), position.x, position.y, size.x, size.y, super.getRotation(), text, 0xFFFFFFFF);
        super.draw();
    }

}
