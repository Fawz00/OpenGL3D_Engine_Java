package opengl3d.ui;

import java.util.Vector;

import opengl3d.utils.Point2D;

public class UIContainer extends UIComponent {
	private Vector<UIComponent> elements = new Vector<>();
	
	public UIContainer(String id, Point2D pos, Point2D size) {
		super(id, initStyle(), pos, size);
	}
	public UIContainer(String id, int x, int y, int sx, int sy) {
		super(id, initStyle(), x, y, sx, sy);
	}

	public static UIStyle initStyle() {
        UIStyle s = new UIStyle();
        return s;
    }

	public UIComponent getElementById(String id) {
		for(UIComponent ui: elements) {
			if(ui.getId() == id) return ui;
		}
		return null;
	}
}
