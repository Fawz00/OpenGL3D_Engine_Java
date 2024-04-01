package opengl3d.utils;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.lwjgl.opengl.GL30;

import opengl3d.Settings;

public class Shader {
	private int attribute;
	private int program;

	public static String loadShader(String filePath) {
		try {
			byte[] shaderBytes = Files.readAllBytes(Paths.get(filePath));
			String shader = new String(shaderBytes, "UTF-8");
			shader = shader.replace("#define USE_SHADOW", "#define USE_SHADOW "+Settings.useShadow);
			shader = shader.replace("#define USE_PENUMBRA_SHADOW", "#define USE_PENUMBRA_SHADOW "+Settings.usePenumbraShadow);
			shader = shader.replace("#define USE_SKYBOX", "#define USE_SKYBOX "+Settings.useSkyBox);
			shader = shader.replace("#define USE_CLOUD", "#define USE_CLOUD "+Settings.useCloud);
			shader = shader.replace("#define USE_REFLECTION", "#define USE_REFLECTION "+Settings.useReflection);
			shader = shader.replace("#define USE_NORMAL_MAP", "#define USE_NORMAL_MAP "+Settings.useNormalMapping);
			shader = shader.replace("#define USE_PARALLAX", "#define USE_PARALLAX "+Settings.useParallaxMapping);
			shader = shader.replace("#define USE_FXAA", "#define USE_FXAA "+Settings.FXAA);
			return shader;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Shader(String vertexDir, String fragmentDir){
		String vertexShaderSource = loadShader(vertexDir);
		String fragmentShaderSource = loadShader(fragmentDir);

		int vertexShader = GL30.glCreateShader(GL30.GL_VERTEX_SHADER);
		GL30.glShaderSource(vertexShader, vertexShaderSource);
		GL30.glCompileShader(vertexShader);

		int fragmentShader = GL30.glCreateShader(GL30.GL_FRAGMENT_SHADER);
		GL30.glShaderSource(fragmentShader, fragmentShaderSource);
		GL30.glCompileShader(fragmentShader);

		program = GL30.glCreateProgram();
		GL30.glAttachShader(program, vertexShader);
		GL30.glAttachShader(program, fragmentShader);
		GL30.glLinkProgram(program);

		System.out.println("SHADER PROGRAM LOG (VERTEX_DIR: "+vertexDir+", FRAGMENT_DIR: "+fragmentDir+" ):\n" + GL30.glGetProgramInfoLog(program));
		int[] linkStatus = new int[1];
		GL30.glGetProgramiv(program, GL30.GL_LINK_STATUS, linkStatus);
		if(linkStatus[0] != GL30.GL_TRUE) {
			throw new RuntimeException(
				"\n\n"+
				"=============================\n"+
				"   S H A D E R   E R R O R   \n"+
				"=============================\n\n"+
				
				"VERTEX DIR: "+vertexDir+"\n"+
				"VERTEX LOG: "+GL30.glGetShaderInfoLog(vertexShader)+"\n"+
				"FRAGMENT DIR: "+fragmentDir+"\n"+
				"FRAGMENT LOG: "+GL30.glGetShaderInfoLog(fragmentShader)+"\n\n"+
				
				"ERROR CODE: "+GL30.glGetProgramInfoLog(program)+"\n\n"
			);
		}

		GL30.glDeleteShader(vertexShader);
		GL30.glDeleteShader(fragmentShader);
	}

	public void useShader() {
		GL30.glUseProgram(program);
	}
	public void resetShader() {
		GL30.glUseProgram(0);
	}

	public void delete() {
		if(program != 0) {
			int[] deleteStatus = new int[1];
			GL30.glGetProgramiv(program, GL30.GL_DELETE_STATUS, deleteStatus);
			if(deleteStatus[0] != GL30.GL_TRUE) GL30.glDeleteProgram(program);
		}
	}

	public void setAttr(String name, int stride, int offset) {
		attribute = GL30.glGetAttribLocation(program, name);
		GL30.glVertexAttribPointer(attribute, 3, GL30.GL_FLOAT, false, stride, offset);
		GL30.glEnableVertexAttribArray(attribute);
	}
	public void setAttr(int id, int dataLength, int stride, int offset) {
		GL30.glVertexAttribPointer(id, dataLength, GL30.GL_FLOAT, false, stride, offset);
		GL30.glEnableVertexAttribArray(id);
	}
	public void disAttr() {
		GL30.glDisableVertexAttribArray(attribute);
	}

	public void setInt(String name, int value) {
		int uniform = GL30.glGetUniformLocation(program, name);
		GL30.glUniform1i(uniform, value);
	}

	public void setFloat(String name, float data) {
		int uniform = GL30.glGetUniformLocation(program, name);
		GL30.glUniform1f(uniform, data);
	}

	public void setVec2(String name, float[] data) {
		int uniform = GL30.glGetUniformLocation(program, name);
		GL30.glUniform2fv(uniform, data);
	}

	public void setVec3(String name, float[] data) {
		int uniform = GL30.glGetUniformLocation(program, name);
		GL30.glUniform3fv(uniform, data);
	}

	public void setVec4(String name, float[] data) {
		int uniform = GL30.glGetUniformLocation(program, name);
		GL30.glUniform4fv(uniform, data);
	}

	public void setMat4(String name, float[] data) {
		int uniform = GL30.glGetUniformLocation(program, name);
		GL30.glUniformMatrix4fv(uniform, false, data);
	}

}
