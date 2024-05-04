package opengl3d.ui;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL30;

import opengl3d.utils.ModelReader;
import opengl3d.utils.Shader;

public class UIComponent {
	private ModelReader modelQuad;
	private int backgroundTexture;

	private String id;
	private Point2D position;
	private Point2D size;
	private int rotation = 0;
	private boolean active;
	private UIEvent event;
	
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
	
	public void draw(Shader shader, Point2D resolution) {
		float[] rotationM = new float[16];
		Matrix4f tr = new Matrix4f()
			.rotate(new Quaternionf(new AxisAngle4f((float)Math.toRadians(-rotation), 0f,0f,1f)));
		tr.get(rotationM);

		shader.useShader();

		GL30.glDisable(GL30.GL_CULL_FACE);
		GL30.glEnable(GL30.GL_BLEND);
		GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);

		modelQuad.getModel();
		shader.setMat4("ROTATION_MATRIX", rotationM);
		shader.setVec2("LOCATION", new float[] {(float)position.x, (float)-position.y});
		shader.setVec2("SCALE", new float[] {(float)size.x, (float)size.y});
		shader.setVec2("RESOLUTION", new float[] {(float)resolution.x, (float)resolution.y});

		shader.setInt("TEXTURE", 0);
		GL30.glActiveTexture(GL30.GL_TEXTURE0);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, backgroundTexture);

		modelQuad.drawModel();
		ModelReader.resetModel();
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
	public void setBackgroundTexture(int id) {
		this.backgroundTexture = id;
	}
	public void setEvent(UIEvent e) {
		this.event = e;
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
	public UIEvent getEvent() {
		return event;
	}
	public String getId() {
		return id;
	}
}
