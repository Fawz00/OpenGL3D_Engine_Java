package opengl3d.utils;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

public class ModelReader {

	private int faces = -1;
	private int stride = -1;

	private static int FLOAT_BYTE_SIZE = Float.BYTES;

	private int vao;
	private int vbo;
	private int instanceVbo;

	public ModelReader(String modelDir){
		ObjReader modelOBJ = new ObjReader(modelDir);
		faces = modelOBJ.numFaces;
		stride = modelOBJ.stride;

		vao = GL30.glGenVertexArrays();
		vbo = GL30.glGenBuffers();
		getModel();
		GL30.glBufferData(GL30.GL_ARRAY_BUFFER, modelOBJ.out, GL30.GL_STATIC_DRAW);
		modelOBJ.deleteBuffer();
		setAttributes();
		resetModel();
	}
	public ModelReader(String modelDir, FloatBuffer data){
		ObjReader modelOBJ = new ObjReader(modelDir);
		faces = modelOBJ.numFaces;
		stride = modelOBJ.stride;

		setInstanceData(data);

		vao = GL30.glGenVertexArrays();
		vbo = GL30.glGenBuffers();
		getModel();
		GL30.glBufferData(GL30.GL_ARRAY_BUFFER, modelOBJ.out, GL30.GL_STATIC_DRAW);
		modelOBJ.deleteBuffer();
		setAttributes();
		bindInstanceData();

		setAttr(5, 3, Float.BYTES*9, 0);
		setAttrAsInstance(5);
		setAttr(6, 3, Float.BYTES*9, Float.BYTES*3);
		setAttrAsInstance(6);
		setAttr(7, 3, Float.BYTES*9, Float.BYTES*6);
		setAttrAsInstance(7);

		resetModel();
	}
	public ModelReader(String modelDir, float[] data){
		ObjReader modelOBJ = new ObjReader(modelDir);
		faces = modelOBJ.numFaces;
		stride = modelOBJ.stride;

		setInstanceData(data);
		
		vao = GL30.glGenVertexArrays();
		vbo = GL30.glGenBuffers();
		getModel();
		GL30.glBufferData(GL30.GL_ARRAY_BUFFER, modelOBJ.out, GL30.GL_STATIC_DRAW);
		modelOBJ.deleteBuffer();
		setAttributes();
		bindInstanceData();
		
		setAttr(5, 3, Float.BYTES*9, 0);
		setAttrAsInstance(5);
		setAttr(6, 3, Float.BYTES*9, 3);
		setAttrAsInstance(6);
		setAttr(7, 3, Float.BYTES*9, 6);
		setAttrAsInstance(7);

		resetModel();
	}

	public void setInstanceData(FloatBuffer data){
		instanceVbo = GL30.glGenBuffers();
		bindInstanceData();
		GL30.glBufferData(GL30.GL_ARRAY_BUFFER, data, GL30.GL_STATIC_DRAW);
		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
	}
	public void setInstanceData(float[] data){
		instanceVbo = GL30.glGenBuffers();
		bindInstanceData();
		GL30.glBufferData(GL30.GL_ARRAY_BUFFER, data, GL30.GL_STATIC_DRAW);
		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
	}
	public void bindInstanceData() {
		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, instanceVbo);
	}
	public void setAttrAsInstance(int id) {
		GL33.glVertexAttribDivisor(id, 1);
	}

	public void getModel(){
		GL30.glBindVertexArray(vao);
		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo);
	}

	public void setAttributes(){
		setAttr(0, 3, FLOAT_BYTE_SIZE*stride, 0);
		setAttr(1, 2, FLOAT_BYTE_SIZE*stride, 3*FLOAT_BYTE_SIZE);
		setAttr(2, 3, FLOAT_BYTE_SIZE*stride, 5*FLOAT_BYTE_SIZE);
		setAttr(3, 3, FLOAT_BYTE_SIZE*stride, 8*FLOAT_BYTE_SIZE);
		setAttr(4, 3, FLOAT_BYTE_SIZE*stride, 11*FLOAT_BYTE_SIZE);
	}
	public void setAttributes(Shader shader){
		shader.setAttr("POSITION", 3 * FLOAT_BYTE_SIZE*stride, 0);
		shader.setAttr("TEXCOORD", 2 * FLOAT_BYTE_SIZE*stride, 3*FLOAT_BYTE_SIZE);
		shader.setAttr("NORMAL", 3 * FLOAT_BYTE_SIZE*stride, 5*FLOAT_BYTE_SIZE);
		shader.setAttr("TANGENT", 3 * FLOAT_BYTE_SIZE*stride, 8*FLOAT_BYTE_SIZE);
		shader.setAttr("BITANGENT", 3 * FLOAT_BYTE_SIZE*stride, 11*FLOAT_BYTE_SIZE);
	}

	public void setAttr(int id, int dataLength, int stride, int offset) {
		GL30.glVertexAttribPointer(id, dataLength, GL30.GL_FLOAT, false, stride, offset);
		GL30.glEnableVertexAttribArray(id);
	}

	public void drawModel(){
		GL30.glDrawArrays(GL30.GL_TRIANGLES, 0, faces);
	}
	public void drawModelInstanced(int size){
		GL33.glDrawArraysInstanced(GL30.GL_TRIANGLES, 0, faces, size);
	}

	public static void resetModel(){
		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
		GL30.glBindVertexArray(0);
	}

	public void deleteModel(){
		if(vao != 0) GL30.glDeleteVertexArrays(vao);
		if(vbo != 0) GL30.glDeleteBuffers(vbo);
		if(instanceVbo != 0) GL30.glDeleteBuffers(instanceVbo);
	}

}