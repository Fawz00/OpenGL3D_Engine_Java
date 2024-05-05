package opengl3d.ui;

public class UIButton extends UIComponent {
    private String text;

	public UIButton(String id, Point2D pos, Point2D size) {
		super(id, pos, size);
	}
	public UIButton(String id, int x, int y, int sx, int sy) {
		super(id, new Point2D(x,y), new Point2D(sx, sy));
	}

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public void draw() {
        Point2D position = super.getPosition();
        Point2D size = super.getSize();
        UIRenderer.getFont().drawText(UIRenderer.getTextShader(), position.x-size.x/2, position.y-size.y/2, size.x, size.y, text, 0xFFFFFFFF);
        super.draw();
    }

}
