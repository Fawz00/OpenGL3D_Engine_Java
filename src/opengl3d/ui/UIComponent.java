package opengl3d.ui;

import opengl3d.utils.ModelReader;
import opengl3d.utils.Shader;

public class UIComponent {
	private ModelReader modelQuad;

	String id;
	Point2D position;
	Point2D size;
	int rotation = 0;
	boolean active;
	
	public UIComponent(String id, Point2D pos, Point2D size) {
		this.id = id;
		this.position = pos;
		this.size = size;
		active = true;
		modelQuad = new ModelReader("resources/models/quad.obj");
	}
	public UIComponent(String id, int x, int y, int sx, int sy) {
		this(id, new Point2D(x,y), new Point2D(sx, sy));
	}
	
	public void draw(Shader shader) {
		
	}
	public void destroy() {
		active = false;
		modelQuad.deleteModel();
	}

	public void setPosition(int x, int y) {
		this.position.x = x;
		this.position.y = y;
		
	}
	public void setSize(int x, int y) {
		this.size.x = x;
		this.size.y = y;
		
	}
	public void setRotation(int rotation) {
		this.rotation = rotation;
	}

	public void onClick() {
		// TODO Auto-generated method stub
		
	}
	public void onHover() {
		// TODO Auto-generated method stub
		
	}
	public void setActive(boolean b) {
		active = b;
	}

	public boolean isActive() {
		return active;
	}
	public int getRotation() {
		return rotation;
	}
	public Point2D getPosition() {
		return position;
	}
	public Point2D getSize() {
		return size;
	}
}
