package opengl3d.ui;

public class UIBox extends UIComponent {

	public UIBox(String id, Point2D pos, Point2D size) {
		super(id, pos, size);
	}
	public UIBox(String id, int x, int y, int sx, int sy) {
		super(id, new Point2D(x,y), new Point2D(sx, sy));
	}

}
