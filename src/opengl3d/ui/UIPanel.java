package opengl3d.ui;

import java.util.Vector;

import org.lwjgl.opengl.GL30;

import opengl3d.utils.Point2;

public class UIPanel extends UIComponent {
	private Vector<UIComponent> components = new Vector<>();
	private String type = "vertical";
	
	public UIPanel(String id, Point2 pos, Point2 size) {
		super(id, normalStyle(), pos, size);
	}
	public UIPanel(String id, int x, int y, int sx, int sy) {
		super(id, normalStyle(), x, y, sx, sy);
	}
	public static UIStyle normalStyle() {
        UIStyle s = new UIStyle();
        return s;
    }

	@Override
	public void draw() {
		super.draw();

		Point2 childPos;
		if(type.equals("vertical")) {
			childPos = new Point2(this.getPosition().x, this.getPosition().y-(this.getSize().y/2));
		} else if(type.equals("horizontal")) {
			childPos = new Point2(this.getPosition().x-(this.getSize().x/2), this.getPosition().y);
		} else {
			childPos = new Point2(this.getPosition().x-(this.getSize().x/2), this.getPosition().y-(this.getSize().y/2));
		};

		for (UIComponent ui : components) {
			if(type.equals("vertical")) {
				if(childPos.y > this.getPosition().y+(this.getSize().y/2)) {
					ui.setDrawVisible(false);
					break;
				} else ui.setDrawVisible(true);
				ui.setPosition(new Point2(childPos.x, childPos.y+ui.getSize().y/2f));
				childPos.y += ui.getSize().y;

			} else if(type.equals("horizontal")) {
				if(childPos.x > this.getPosition().x+(this.getSize().x/2)) {
					ui.setDrawVisible(false);
					break;
				} else ui.setDrawVisible(true);
				ui.setPosition(new Point2(childPos.x+ui.getSize().x/2f, childPos.y));
				childPos.x += ui.getSize().x;
			}

			GL30.glEnable(GL30.GL_SCISSOR_TEST);
			Point2 pos = this.getPosition();
			Point2 size = this.getSize();
			GL30.glScissor((int)(pos.x-(size.x/2f)), (int)(pos.y+(size.y/2f)), (int)size.x, (int)size.y);
			ui.draw();
			GL30.glDisable(GL30.GL_SCISSOR_TEST);
		}
	}

	@Override
	public void destroy() {
		for(UIComponent ui: components) {
			ui.destroy();
		}
		super.destroy();
	}
	// @Override
	// public void setRotation(int ang) {
	// 	for(UIComponent ui: components) {
	// 		//ui.setRotation(ang);
	// 	}
	// 	super.setRotation(ang);
	// }

	public void put(UIComponent ui) {
		ui.setParent(this);
		components.add(ui);
	}
	public void setPanelType(String type) {
		this.type = type;
	}

	public UIComponent getComponentById(String id) {
		for(UIComponent ui: components) {
			if(ui.getId() == id) return ui;
		}
		return null;
	}
}
