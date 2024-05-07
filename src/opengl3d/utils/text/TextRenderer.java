package opengl3d.utils.text;

import java.nio.FloatBuffer;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import opengl3d.ui.Point2D;
import opengl3d.utils.ModelReader;
import opengl3d.utils.Shader;

public class TextRenderer {
	private int FLOAT_BYTE_SIZE = Float.BYTES;
	private int faces = 6;
	private int stride = 2;
	private int vao;
	private int vbo;

	public TextRenderer() {
		FloatBuffer vertices = MemoryUtil.memAllocFloat(faces*stride);
		
		vertices.put(0f).put(0f);
		vertices.put(0f).put(1f);
		vertices.put(1f).put(1f);

		vertices.put(0f).put(0f);
		vertices.put(1f).put(1f);
		vertices.put(1f).put(0f);
		vertices.flip();
		
		// THE CODE
		vao = GL30.glGenVertexArrays();
		vbo = GL30.glGenBuffers();
		getModelData();
		GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertices, GL30.GL_STATIC_DRAW);
		MemoryUtil.memFree(vertices);

		GL30.glVertexAttribPointer(0, 2, GL30.GL_FLOAT, false, FLOAT_BYTE_SIZE*stride, 0);
		GL30.glEnableVertexAttribArray(0);

		ModelReader.resetModel();
	}

	public void getModelData(){
		GL30.glBindVertexArray(vao);
		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo);
	}
	
	public void render(Shader shader, int texture, Point2D res, int width, int height, int posX, int posY, int rotation, int textureWidth, int textureHeight, float x, float y, float regX, float regY, float regWidth, float regHeight, int color){
		shader.useShader();

		GL30.glDisable(GL30.GL_CULL_FACE);
		GL30.glEnable(GL30.GL_BLEND);
		GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);

		float[] rotationM = new float[16];
		Matrix4f tr = new Matrix4f()
			.rotate(new Quaternionf(new AxisAngle4f((float)Math.toRadians(-rotation), 0f,0f,1f)))
			;
		tr.get(rotationM);
		shader.setMat4("ROTATION_MATRIX", rotationM);

		shader.setInt("TEXTURE", 0);
		GL30.glActiveTexture(GL30.GL_TEXTURE0);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, texture);

		shader.setVec2("RESOLUTION", new float[] {res.x, res.y});
		shader.setVec2("OFFSET", new float[] {posX, posY});
		/* Texture coordinates */
		float s1 = regX / textureWidth;
		float t1 = regY / textureHeight;
		float s2 = (regX + regWidth) / textureWidth;
		float t2 = (regY + regHeight) / textureHeight;
		shader.setVec4("TEXCOORD", new float[] {s1, t1, s2, t2});
		float x1 = x;
		float y1 = y;
		float x2 = x + regWidth;
		float y2 = y + regHeight;
		shader.setVec4("POSITION", new float[] {x1, y1, x2, y2});
		float col_r = (float)((color >> 24) & 0xFF) / 255f;
		float col_g = (float)((color >> 16) & 0xFF) / 255f;
		float col_b = (float)((color >> 8) & 0xFF) / 255f;
		float col_a = (float)(color & 0xFF) / 255f;
		shader.setVec4("COLOR", new float[] {col_r, col_g, col_b, col_a});

		GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, faces);

		GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
	}

	public void renderText(Shader shader, int texture, Point2D res, int textureWidth, int textureHeight, float x, float y, float regX, float regY, float regWidth, float regHeight, int color){
		float x1 = x;
		float y1 = y;
		float x2 = x + regWidth;
		float y2 = y + regHeight;

		/* Texture coordinates */
		float s1 = regX / textureWidth;
		float t1 = regY / textureHeight;
		float s2 = (regX + regWidth) / textureWidth;
		float t2 = (regY + regHeight) / textureHeight;

		int faces = 6;
		int stride = 2+4+2; // position.xyz + texcoord.xy + color.rgba
		FloatBuffer vertices = MemoryUtil.memAllocFloat(faces*stride);

		float col_r = (float)((color >> 24) & 0xFF) / 255f;
		float col_g = (float)((color >> 16) & 0xFF) / 255f;
		float col_b = (float)((color >> 8) & 0xFF) / 255f;
		float col_a = (float)(color & 0xFF) / 255f;

		//kiri bawah
		//kiri atas
		//kanan atas
		
		//kiri bawah
		//kanan atas
		//kanan bawah
		vertices.put(x1).put(y1).put(col_r).put(col_g).put(col_b).put(col_a).put(s1).put(t1);
		vertices.put(x1).put(y2).put(col_r).put(col_g).put(col_b).put(col_a).put(s1).put(t2);
		vertices.put(x2).put(y2).put(col_r).put(col_g).put(col_b).put(col_a).put(s2).put(t2);

		vertices.put(x1).put(y1).put(col_r).put(col_g).put(col_b).put(col_a).put(s1).put(t1);
		vertices.put(x2).put(y2).put(col_r).put(col_g).put(col_b).put(col_a).put(s2).put(t2);
		vertices.put(x2).put(y1).put(col_r).put(col_g).put(col_b).put(col_a).put(s2).put(t1);
		vertices.flip();

		int vao = GL30.glGenVertexArrays();
		int vbo = GL30.glGenBuffers();

		GL30.glBindVertexArray(vao);
		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo);
		GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertices, GL30.GL_STATIC_DRAW);
		MemoryUtil.memFree(vertices);

		shader.useShader();

		shader.setAttr(0, 2, FLOAT_BYTE_SIZE*stride, 0);
		shader.setAttr(1, 4, FLOAT_BYTE_SIZE*stride, 2*FLOAT_BYTE_SIZE);
		shader.setAttr(2, 2, FLOAT_BYTE_SIZE*stride, 6*FLOAT_BYTE_SIZE);

		GL30.glDisable(GL30.GL_CULL_FACE);
		GL30.glEnable(GL30.GL_BLEND);
		GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);

		shader.setInt("TEXTURE", 0);
		GL30.glActiveTexture(GL30.GL_TEXTURE0);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, texture);

		shader.setVec2("RESOLUTION", new float[] {res.x, res.y});

		GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, faces);

		GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
		//shader.disAttr();

		GL30.glDeleteVertexArrays(vao);
		GL30.glDeleteBuffers(vbo);
	}

}