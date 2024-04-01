package opengl3d.ui;

import opengl3d.utils.Shader;

public class UIBox implements UIComponent {
	int[] position = new int[] {0,0};
	int[] size = new int[] {0,0};
	int rotation = 0;

	public UIBox(int[] pos, int[] size) {
		this.position = pos;
		this.size = size;
	}
	public UIBox(int x, int y, int sx, int sy) {
		this(new int[] {x,y}, new int[] {sx, sy});
	}
	
	@Override
	public void draw(Shader shader) {
		
	}

	@Override
	public void setPosition(int x, int y) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setSize(int x, int y) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setRotation(int x, int y) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onClick() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onHover() {
		// TODO Auto-generated method stub
		
	}
}
