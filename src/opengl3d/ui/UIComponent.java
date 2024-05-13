package opengl3d.ui;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL30;

import opengl3d.utils.Point2;
import opengl3d.utils.Shader;

public class UIComponent {
	private String id;
	private Point2 position;
	private Point2 size;
	private int rotation = 0;
	private boolean active;
	private boolean visible;
	private boolean drawVisible;
	private UIEvent event;
	private UIEvent defEvent;
	private UIStyle drawStyle;
	private UIStyle styleNormal;
	private UIStyle styleOnHover;
	private UIStyle styleOnClick;
	private UIComponent parent;

	public UIComponent(String id, UIStyle istyle, Point2 pos, Point2 size) {
		this.id = id;
		this.position = pos;
		this.size = size;
		visible = true;
		drawVisible = true;
		active = true;
		this.styleNormal = istyle;
		this.drawStyle = istyle;
		this.styleOnHover = istyle;
		this.styleOnClick = istyle;
		this.event = new UIEvent();
		this.defEvent = new UIEvent(){
			@Override
			public void runOnHover() {
				setDrawStyle(styleOnHover);
				event.runOnHover();
			}
			@Override
			public void runOnNotHover() {
				setDrawStyle(styleNormal);
				event.runOnNotHover();
			}
			@Override
			public void runOnClick() {
				setDrawStyle(styleOnClick);
				event.runOnClick();
			}
			@Override
			public void runOnRelease() {
				setDrawStyle(styleOnHover);
				event.runOnRelease();
			}
		};
	}
	public UIComponent(String id, UIStyle style, int x, int y, int sx, int sy) {
		this(id, style, new Point2(x,y), new Point2(sx, sy));
	}

	private void setDrawStyle(UIStyle s) {
		if(s != null) {
			this.drawStyle = s;
		} else {
			this.drawStyle = this.styleNormal;
		}
	}

	// private void getParentTransform(Matrix4f m, UIComponent p) {
	// 	if(p != null) {
	// 		Point2 pos = p.getPosition();
	// 		int rot = p.getRotation();
	// 		m
	// 			.translate(pos.x*2f, -pos.y*2f, 0f)
	// 			.rotate(new Quaternionf(new AxisAngle4f((float)Math.toRadians(rot), 0f,0f,1f)))
	// 			;

	// 		getParentTransform(m, p.parent);
	// 	}
	// }
	
	public void draw() {
		if(position == null || size == null) {
			System.out.println("UI DRAW() ERROR: position & size is null. UI Id: " + id);
		} if(visible && drawVisible) {
			Shader shader = UIRenderer.getUIShader();
			Point2 resolution = UIRenderer.getScreenSize();

			float[] rotationM = new float[16];
			Matrix4f tr = new Matrix4f()
				.translate(position.x*2f, -position.y*2f, 0f)
				.rotate(new Quaternionf(new AxisAngle4f((float)Math.toRadians(-rotation), 0f,0f,1f)))
				.scale(size.x, size.y, 1f)
				;
			//getParentTransform(tr, parent);
			tr.get(rotationM);

			shader.useShader();

			GL30.glDisable(GL30.GL_CULL_FACE);
			GL30.glEnable(GL30.GL_BLEND);
			GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
			GL30.glEnable(GL30.GL_DEPTH_TEST);
			GL30.glDepthFunc(GL30.GL_LEQUAL);

			UIRenderer.getQuadModel().getModel();
			shader.setMat4("MODEL_MATRIX", rotationM);
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
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, drawStyle.backgroundTexture);

			UIRenderer.getQuadModel().drawModel();
			//ModelReader.resetModel();
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
		}
	}
	public void destroy() {
		visible = false;
		drawVisible = false;
		active = false;
	}

	public void setPosition(int x, int y) {
		this.position.x = x;
		this.position.y = y;
	}
	public void setPosition(Point2 pos) {
		this.position = pos;
	}
	public void setSize(int x, int y) {
		this.size.x = x;
		this.size.y = y;
	}
	public void setSize(Point2 size) {
		this.size.x = size.x;
		this.size.y = size.y;
	}
	public void setRotation(int rotation) {
		this.rotation = rotation;
	}
	public void setBackgroundTexture(int id) {
		this.styleNormal.backgroundTexture = id;
	}
	public void setEvent(UIEvent e) {
		this.event = e;
	}
	public void setActive(boolean b) {
		this.active = b;
	}
	public void setVisible(boolean b) {
		this.visible = b;
	}
	public void setDrawVisible(boolean b) {
		this.drawVisible = b;
	}
	public void setStyle(UIStyle style) {
		if(style != null) {
			this.styleNormal = style;
			this.styleOnHover = style;
			this.styleOnClick = style;
			this.drawStyle = style;
		}
	}
	public void setStyleOnHover(UIStyle style) {
		if(style != null) this.styleOnHover = style;
		else this.styleOnHover = this.styleNormal;
	}
	public void setStyleOnClick(UIStyle style) {
		if(style != null) this.styleOnClick = style;
		else this.styleOnClick = this.styleNormal;
	}
	public void setParent(UIComponent ui) {
		parent = ui;
	}

	public boolean isActive() {
		return active;
	}
	public boolean isVisible() {
		return visible;
	}
	public boolean isDrawVisible() {
		return drawVisible;
	}

	public int getRotation() {
		return rotation;
	}
	public Point2 getPosition() {
		return position;
	}
	public Point2 getSize() {
		return size;
	}
	public UIEvent getEvent() {
		return defEvent;
	}
	public String getId() {
		return id;
	}
	public UIStyle getStyle() {
		return styleNormal;
	}
	public UIStyle getStyleOnHover() {
		return styleOnHover;
	}
	public UIStyle getStyleOnClick() {
		return styleOnClick;
	}
	public UIComponent parent() {
		return parent;
	}
}
