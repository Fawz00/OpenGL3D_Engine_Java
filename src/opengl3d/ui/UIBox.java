package opengl3d.ui;

import opengl3d.utils.Color4;
import opengl3d.utils.Point2D;

public class UIBox extends UIComponent {

	public UIBox(String id, Point2D pos, Point2D size) {
		super(id, initStyle(), pos, size);
	}
	public UIBox(String id, int x, int y, int sx, int sy) {
		super(id, initStyle(), x, y, sx, sy);
	}
	public static UIStyle initStyle() {
        UIStyle s = new UIStyle();
        s.backgroundColor = new Color4(0x00FF8080);
        return s;
    }

}
