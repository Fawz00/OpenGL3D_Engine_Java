package opengl3d.ui;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL30;

import opengl3d.utils.ModelReader;
import opengl3d.utils.Point2D;
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
	private UIEvent defEvent;
	private UIStyle drawStyle;
	private UIStyle style;
	private UIStyle styleOnHover;
	private UIStyle styleOnClick;
	
	public UIComponent(String id, UIStyle style, Point2D pos, Point2D size) {
		this.id = id;
		this.position = pos;
		this.size = size;
		active = true;
		modelQuad = new ModelReader("resources/models/quad.obj");
		this.style = style;
		this.drawStyle = style;
		this.styleOnHover = style;
		this.styleOnClick = style;
		this.event = new UIEvent();
		this.defEvent = new UIEvent(){
			@Override
			public void runOnHover() {
				drawStyle = styleOnHover;
				event.runOnHover();
			}
			@Override
			public void runOnNotHover() {
				drawStyle = style;
				event.runOnNotHover();
			}
			@Override
			public void runOnClick() {
				drawStyle = styleOnClick;
				event.runOnClick();
			}
			@Override
			public void runOnRelease() {
				drawStyle = styleOnHover;
				event.runOnRelease();
			}
		};
	}
	public UIComponent(String id, UIStyle style, int x, int y, int sx, int sy) {
		this(id, style, new Point2D(x,y), new Point2D(sx, sy));
	}
	
	public void draw() {
		Shader shader = UIRenderer.getUIShader();
		Point2D resolution = UIRenderer.getScreenSize();

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
		shader.setVec2("SIZE", new float[] {(float)size.x, (float)size.y});
		shader.setVec2("RESOLUTION", new float[] {(float)resolution.x, (float)resolution.y});

		// Style
		shader.setVec4("BackgroundColor", drawStyle.backgroundColor.getColor());
		shader.setVec4("BorderColor", drawStyle.borderColor.getColor());
		shader.setFloat("BorderRadius", drawStyle.borderRadius);
		shader.setFloat("BorderWidth", drawStyle.borderWidth);

		shader.setInt("TEXTURE", 0);
		GL30.glActiveTexture(GL30.GL_TEXTURE0);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, backgroundTexture);

		modelQuad.drawModel();
		//ModelReader.resetModel();
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
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
		this.active = b;
	}
	public void setStyle(UIStyle style) {
		this.style = style;
	}
	public void setStyleOnHover(UIStyle style) {
		this.styleOnHover = style;
	}
	public void setStyleOnClick(UIStyle style) {
		this.styleOnClick = style;
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
		return defEvent;
	}
	public String getId() {
		return id;
	}
	public UIStyle getStyle() {
		return style;
	}
	public UIStyle getStyleOnHover() {
		return styleOnHover;
	}
	public UIStyle getStyleOnClick() {
		return styleOnClick;
	}
}
