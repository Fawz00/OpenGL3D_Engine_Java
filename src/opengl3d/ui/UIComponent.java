package opengl3d.ui;

import opengl3d.utils.Shader;

public interface UIComponent {
	static int SCREEN_WIDTH = 0;
	static int SCREEN_HEIGHT = 0;
	
	public void setPosition(int x, int y);
	public void setSize(int x, int y);
	public void setRotation(int x, int y);
	public void draw(Shader shader);
	public void onClick();
	public void onHover();
}
