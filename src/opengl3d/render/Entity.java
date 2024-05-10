package opengl3d.render;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL30;

import opengl3d.Camera;
import opengl3d.Settings;
import opengl3d.utils.MatMat;
import opengl3d.utils.Matriks;
import opengl3d.utils.ModelReader;
import opengl3d.utils.Shader;
import opengl3d.utils.TextureReader;

public class Entity {
	private static boolean isPaused = false;

	private float[] position = new float[3];
	private float[] rotation = new float[3];
	private float[] velocity = new float[3];
	private float mass;

	private float drawDistance = Settings.entityRenderDistance;
	private float[] cameraPosition = new float[3];

	private ModelReader model;
	private TextureReader texture;

	public Entity(ModelReader model, TextureReader texture, float[] pos, float[] rot, float m) {
		position = pos;
		rotation = rot;
		mass = m;
		this.model = model;
		this.texture = texture;
		setVelocity(new float[]{0f,0f,0f});
	}

	public void setDrawDistance(float dst) {
		this.drawDistance = dst;
	}
	public void setTexture(TextureReader tex) {
		this.texture = tex;
	}
	public void setModel(ModelReader model) {
		this.model = model;
	}
	public void setCameraOffset(float[] pos) {
		cameraPosition = pos;
	}
	public void setPosition(float[] pos) {
		position = pos;
	}
	public void setRotation(float[] rot) {
		rotation = rot;
	}
	public void setVelocity(float[] vel) {
		velocity = vel;
		update();
	}

	public void setAcceleration(float[] vel, float time) {
		velocity[0] += (vel[0]*time);
		velocity[1] += (vel[1]*time);
		velocity[2] += (vel[2]*time);
		update();
	}

	private void update() {
		if(!isPaused) {
			position[0] += velocity[0];
			position[1] += velocity[1];
			position[2] += velocity[2];
		}
	}

	public void render(Shader shader, Camera camera) {
		float distance = Vector3f.distance(	camera.getPosition()[0],camera.getPosition()[1],camera.getPosition()[2],
											position[0],position[1],position[2]);
		if(distance <= drawDistance) {
			float[] transformM = new float[16];
			Matrix4f tr = new Matrix4f()
								.translate(position[0], position[1], position[2])
								.rotate(new Quaternionf(new AxisAngle4f((float)Math.toRadians(-rotation[0]), 1f,0f,0f)))
								.rotate(new Quaternionf(new AxisAngle4f((float)Math.toRadians(-rotation[1]), 0f,1f,0f)))
								.rotate(new Quaternionf(new AxisAngle4f((float)Math.toRadians(-rotation[2]), 0f,0f,1f)))
								.scale(1f);
			tr.get(transformM);
	
			shader.setMat4("MODEL_MATRIX", transformM);
			shader.setMat4("NORMAL_MATRIX", Matriks.RotasiKe(rotation[0],rotation[1],rotation[2]));
	
			shader.setFloat("INSTANCED", 0f);

			model.getModel();
			shader.setInt("TEXTURE", 0);
			GL30.glActiveTexture(GL30.GL_TEXTURE0);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, texture.getTextureColor());
			if(texture.hasNormal()){
				shader.setInt("TEXTURE_NORMAL", 1);
				GL30.glActiveTexture(GL30.GL_TEXTURE1);
				GL30.glBindTexture(GL30.GL_TEXTURE_2D, texture.getTextureNor());
				shader.setFloat("USE_NORMAL_TEXTURE", 1f);
			} else shader.setFloat("USE_NORMAL_TEXTURE", 0f);
			if(texture.hasParallax()){
				shader.setInt("TEXTURE_PARALLAX", 2);
				GL30.glActiveTexture(GL30.GL_TEXTURE2);
				GL30.glBindTexture(GL30.GL_TEXTURE_2D, texture.getTexturePar());
				shader.setFloat("USE_PARALLAX_TEXTURE", 1f);
			} else shader.setFloat("USE_PARALLAX_TEXTURE", 0f);
			if(texture.hasMer()){
				shader.setInt("TEXTURE_MER", 3);
				GL30.glActiveTexture(GL30.GL_TEXTURE3);
				GL30.glBindTexture(GL30.GL_TEXTURE_2D, texture.getTextureMer());
				shader.setFloat("USE_MER_TEXTURE", 1f);
			} else shader.setFloat("USE_MER_TEXTURE", 0f);
	
			model.drawModel();
	
			tr.identity();
			tr.get(transformM);
			shader.setMat4("MODEL_MATRIX", transformM);
		}
	}

	public static void setPaused(boolean a) {
		isPaused = a;
	}

	public float[] getCameraPosition() {
		Vector4f a = new Vector4f(0f, 0f, 0f, 1f);
		Matrix4f tr = new Matrix4f()
			.rotate((float)Math.toRadians(-rotation[0]), 1f, 0f, 0f)
			.rotate((float)Math.toRadians(-rotation[1]), 0f, 1f, 0f)
			.rotate((float)Math.toRadians(-rotation[2]), 0f, 0f, 1f)
			.translate(cameraPosition[0], cameraPosition[1], cameraPosition[2]);
		a.mul(tr);

		return MatMat.sum(position, new float[] {a.x, a.y, a.z});
	}
	public float[] getPosition() {
		return position;
	}
	public float[] getRotation() {
		return rotation;
	}
	public float[] getVelocity() {
		return velocity;
	}
	public float getMass() {
		return mass;
	}

}
