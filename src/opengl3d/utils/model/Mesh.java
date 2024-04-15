package opengl3d.utils.model;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

public class Mesh {
    private int vao;
	private int vbo;
	private int ebo;
    private int instanceVbo;
    private IntBuffer indices;

    public Mesh(FloatBuffer vertices, IntBuffer indices) {
        this.indices = indices;

		vao = GL30.glGenVertexArrays();
		vbo = GL30.glGenBuffers();
		ebo = GL30.glGenBuffers();

		getModel();
		GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertices, GL30.GL_STATIC_DRAW);
		GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indices, GL30.GL_STATIC_DRAW);

		setAttributes();

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
        int stride = (Float.BYTES*15) + Integer.BYTES;
		setAttrF(0, 3, stride, 0);
		setAttrF(1, 3, stride, 3);
		setAttrF(2, 2, stride, 6);
		setAttrF(3, 3, stride, 8);
		setAttrF(4, 3, stride, 11);
            setAttrI(5, 1, stride, 14);
            setAttrF(6, 1, stride, 15);
	}

    public void setAttrF(int id, int dataLength, int stride, int offset) {
		GL30.glVertexAttribPointer(id, dataLength, GL30.GL_FLOAT, false, Float.BYTES*stride, Float.BYTES*offset);
		GL30.glEnableVertexAttribArray(id);
	}
    public void setAttrI(int id, int dataLength, int stride, int offset) {
		GL30.glVertexAttribPointer(id, dataLength, GL30.GL_INT, false, Integer.BYTES*stride, Integer.BYTES*offset);
		GL30.glEnableVertexAttribArray(id);
	}

	public void drawModel(){
		GL30.glDrawElements(GL30.GL_TRIANGLES, indices);
	}
	public void drawModelInstanced(int size){
		GL33.glDrawElementsInstanced(GL30.GL_TRIANGLES, indices, size);
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
