package opengl3d;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.imageio.ImageIO;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import opengl3d.engine.Input;
import opengl3d.render.ModelLoader;
import opengl3d.render.Entity;
import opengl3d.render.TextureLoader;
import opengl3d.utils.MatMat;
import opengl3d.utils.Matriks;
import opengl3d.utils.ModelReader;
import opengl3d.utils.Point3;
import opengl3d.utils.Shader;
import opengl3d.utils.TextureReader;
import opengl3d.utils.text.Font;
import opengl3d.utils.text.Typeface;

public class GameRenderer {
	//private static boolean firstFrame;
	public static boolean isRunning = false;
	private int frame;
	private Camera camera;

	private Entity entityPlayer;
	private Entity entityThing;
	public Entity entityCenter;

	private ModelLoader modelAll;
	private ModelReader modelCubemap;
	private ModelReader modelQuad;

	private Shader textShader;
	private Shader renderShader;
	private Shader skyBoxShader;
	private Shader shadowMapShader;
	private Shader reflectionShader;
	private Shader lightingShader;
	private Shader SSAOShader;
	private Shader SSGIShader;
	private Shader SSGIDenoiseShader;
	private Shader postProcessShader;

	private int cubemapTexture;
	private int cloudNoiseTexture;
	private int smallNoiseTexture;
	private int tempcolorTexture;
	private TextureLoader textureAll;

	private int renderColorTexture;
	private int renderDepthTexture;
	private int renderMerTexture;
	private int renderNormalTexture;
	private int shadowDepthTexture;
	private int reflectionColorTexture;
	private int reflectionMerTexture;
	private int lightingTexture;
	private int SSAOTexture;
	private int SSGITexture;
	private int SSGIDenoiseTexture;
	private int finalTextureId;

	private int renderFBO, renderRBO;
	private int shadowDepthFBO;
	private int reflectionFBO;
	private int lightingFBO;
	private int SSAOFBO;
	private int SSGIFBO;
	private int SSGIDenoiseFBO;
	private int postProcessFBO;

	private int treeCount = 250;
	private ModelReader treeInstance;
	private FloatBuffer randTreePos = MemoryUtil.memAllocFloat(treeCount*9);
	private float toRad = 0.0174532925199f;
	private float sceneExposure = 1.0f;

	private Font textView;

	private Matrix4f rotationMatrix = new Matrix4f();
	private Matrix4f translationMatrix = new Matrix4f();
	private Matrix4f transformationMatrix = new Matrix4f();
	private Matrix4f viewMatrix = new Matrix4f();
	private Matrix4f projectionMatrix = new Matrix4f();
	private Matrix4f VPMatrix = new Matrix4f();
	private Matrix4f MVPMatrix = new Matrix4f();
	private Matrix4f cubemapViewMatrix = new Matrix4f();
	private Matrix4f cubemapVPMatrix = new Matrix4f();
	private Matrix4f cubemapMVPMatrix = new Matrix4f();

	private Matrix4f shadowRotationMatrix = new Matrix4f();
	private Matrix4f shadowTransformationMatrix = new Matrix4f();
	private Matrix4f shadowProjectionMatrix = new Matrix4f();
	private Matrix4f shadowMVPMatrix = new Matrix4f();

	private float[] MVPMatrixF = new float[16];
	private float[] cubemapMVPMatrixF = new float[16];
	private float[] shadowMVPMatrixF = new float[16];
	private float[] shadowRotationMatrixF = new float[16];

	private float[] sunRotation = new float[3];
	private int[] screenResolution = new int[2];
	private int maxMipmapLevel;

	private Point3[] SSAOKernel = new Point3[64];



	public static int loadTexture(String filePath) {
		int textureId = 0, width, height;
		try {
			ByteBuffer buffer;
			BufferedImage image = ImageIO.read(new File(filePath));

			width = image.getWidth();
			height = image.getHeight();

			int[] pixels = new int[width * height];
			image.getRGB(0, 0, width, height, pixels, 0, width);

			buffer = MemoryUtil.memAlloc(width * height * 4);

			for (int y = 0; y < height; y++){
				for (int x = 0; x < width; x++){
					int pixel = pixels[y * width + x];

					buffer.put((byte) ((pixel >> 16) & 0xFF));  // red
					buffer.put((byte) ((pixel >> 8) & 0xFF));   // green
					buffer.put((byte) (pixel & 0xFF));          // blue
					buffer.put((byte) ((pixel >> 24) & 0xFF));  // alpha
				}
			}
			buffer.flip();

			textureId = GL30.glGenTextures();
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, textureId);
			GL30.glPixelStorei(GL30.GL_UNPACK_ALIGNMENT, 1);

			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_REPEAT);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_REPEAT);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);

			if (buffer != null) {
				GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA, width, height, 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, buffer);
			}
			MemoryUtil.memFree(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		};

		return textureId;
	}
	public static int loadCubemapTexture(String[] dir) {
		if(dir != null && dir.length >= 6) {
			int[] type = new int[] {GL30.GL_TEXTURE_CUBE_MAP_POSITIVE_X,
									GL30.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
									GL30.GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
									GL30.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
									GL30.GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
									GL30.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z};

			int textureId;

			try {
				textureId = GL30.glGenTextures();
				if(textureId != 0) {
					GL30.glBindTexture(GL30.GL_TEXTURE_CUBE_MAP, textureId);
					GL30.glPixelStorei(GL30.GL_UNPACK_ALIGNMENT, 1);

					GL30.glTexParameteri(GL30.GL_TEXTURE_CUBE_MAP, GL30.GL_TEXTURE_WRAP_S, GL30.GL_REPEAT);
					GL30.glTexParameteri(GL30.GL_TEXTURE_CUBE_MAP, GL30.GL_TEXTURE_WRAP_T, GL30.GL_REPEAT);
					GL30.glTexParameteri(GL30.GL_TEXTURE_CUBE_MAP, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
					GL30.glTexParameteri(GL30.GL_TEXTURE_CUBE_MAP, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);

					ByteBuffer buffer;
					for(int i=0; i<dir.length; i++) {
						BufferedImage image = ImageIO.read(new File(dir[i]));
						int width, height;

						width = image.getWidth();
						height = image.getHeight();

						int[] pixels = new int[width * height];
						image.getRGB(0, 0, width, height, pixels, 0, width);

						buffer = MemoryUtil.memAlloc(width * height * 4);

						for (int y = 0; y < height; y++){
							for (int x = 0; x < width; x++){
								int pixel = pixels[y * width + x];

								buffer.put((byte) ((pixel >> 16) & 0xFF));  // red
								buffer.put((byte) ((pixel >> 8) & 0xFF));   // green
								buffer.put((byte) (pixel & 0xFF));          // blue
								buffer.put((byte) ((pixel >> 24) & 0xFF));  // alpha
							}
						}
						buffer.flip();

						if (buffer != null) GL30.glTexImage2D(type[i], 0, GL30.GL_RGBA, width, height, 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, buffer);
						MemoryUtil.memFree(buffer);
					}
					return textureId;
				}
			} catch (IOException e) {
				e.printStackTrace();
			};
		}
		System.out.println("TEXTURE_CUBE LOADER ERROR");
		return 0;
	}

	private float mathSign(float a) {
		if(a<0f) {
			return -1f;
		} else return 1f;
	}

	private void renderModel(	Shader shader, ModelReader model, TextureReader texture,
		float Px,float Py,float Pz, float Rx,float Ry,float Rz, float Sx,float Sy,float Sz) {
		float[] transformM = new float[16];
		Matrix4f tr = new Matrix4f()
							.translate(Px, Py, Pz)
							.rotate(new Quaternionf(new AxisAngle4f((float)Math.toRadians(-Rx), 1f,0f,0f)))
							.rotate(new Quaternionf(new AxisAngle4f((float)Math.toRadians(-Ry), 0f,1f,0f)))
							.rotate(new Quaternionf(new AxisAngle4f((float)Math.toRadians(-Rz), 0f,0f,1f)))
							.scale(Sx, Sy, Sz);
		tr.get(transformM);

		shader.setMat4("MODEL_MATRIX", transformM);
		shader.setMat4("NORMAL_MATRIX", Matriks.RotasiKe(Rx,Ry,Rz));

		model.getModel();
		shader.setFloat("INSTANCED", 0f);
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
		ModelReader.resetModel();

		// tr.identity();
		// tr.get(transformM);
		// shader.setMat4("MODEL_MATRIX", transformM);
	}
	private void renderModelInstanced(
		Shader shader, ModelReader model, TextureReader texture, int count
	) {
		shader.setMat4("MODEL_MATRIX", Matriks.IdentityM4());
		shader.setMat4("NORMAL_MATRIX", Matriks.IdentityM4());

		model.getModel();
		model.bindInstanceData();
		shader.setFloat("INSTANCED", 1f);

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

		model.drawModelInstanced(count);
		ModelReader.resetModel();
	}

	private void renderObjects(Shader shader) {
		GL30.glEnable(GL30.GL_CULL_FACE);
		GL30.glCullFace(GL30.GL_BACK);

		entityPlayer.render(shader, camera);
		entityThing.render(shader, camera);
		entityCenter.render(shader, camera);

		renderModel(shader,
				modelAll.getModelData(modelAll.getModelId("simple")),
				textureAll.getTextureData(textureAll.getTextureId("white")),
				300f,67f,200f,  0f,0f,0f,  1f,1f,1f);
		renderModel(shader,
				modelAll.getModelData(modelAll.getModelId("terrain")),
				textureAll.getTextureData(textureAll.getTextureId("terrain")),
				-2560f,-5f,2560f,  0f,0f,0f,  5f,4f,5f);
		// renderModel(shader,
		// 		modelAll.getModelData(modelAll.getModelId("lighting")),
		// 		textureAll.getTextureData(textureAll.getTextureId("colors")),
		// 		100f,90f,50f,  0f,90f,0f,  5f,5f,5f);
		renderModel(shader,
				modelAll.getModelData(modelAll.getModelId("slum")),
				textureAll.getTextureData(textureAll.getTextureId("slum")),
				0f,90f,0f,  0f,0f,0f,  1f,1f,1f);
		renderModel(shader,
				modelAll.getModelData(modelAll.getModelId("wolf")),
				textureAll.getTextureData(textureAll.getTextureId("metalic_silver")),
				2f,195f,1f,  0f,0f,0f,  1f,1f,1f);
		renderModel(shader,
				modelAll.getModelData(modelAll.getModelId("simple")),
				textureAll.getTextureData(textureAll.getTextureId("toy_box")),
				0f,400f,0f,  0f,0f,0f,  1f,1f,1f);
		renderModel(shader,
				modelAll.getModelData(modelAll.getModelId("sponza")),
				textureAll.getTextureData(textureAll.getTextureId("metalic_silver")),
				300f,75,100f,  0f,0f,0f,  1f,1f,1f);
		renderModel(shader,
				modelAll.getModelData(modelAll.getModelId("box")),
				textureAll.getTextureData(textureAll.getTextureId("stone")),
				300f,85,120f,  0f,0f,0f,  60f,40f,1f);
		renderModel(shader,
				modelAll.getModelData(modelAll.getModelId("box")),
				textureAll.getTextureData(textureAll.getTextureId("red_light")),
				300f,72,150f,  0f,0f,0f,  0.5f,0.5f,0.5f);
		renderModel(shader,
				modelAll.getModelData(modelAll.getModelId("box")),
				textureAll.getTextureData(textureAll.getTextureId("green_light")),
				310f,72,150f,  0f,0f,0f,  0.5f,0.5f,0.5f);
		renderModel(shader,
				modelAll.getModelData(modelAll.getModelId("box")),
				textureAll.getTextureData(textureAll.getTextureId("blue_light")),
				320f,72,150f,  0f,0f,0f,  0.5f,0.5f,0.5f);

		GL30.glDisable(GL30.GL_CULL_FACE);
			// renderModel(shader,
			// 		modelAll.getModelData(modelAll.getModelId("box")),
			// 		textureAll.getTextureData(textureAll.getTextureId("blending")),
			// 		300f,200f,200f,  0f,0f,0f,  100f,100f,100f);

			renderModelInstanced(
						shader,
						treeInstance,
						textureAll.getTextureData(textureAll.getTextureId("pine_tree")),
						treeCount);

		renderModel(shader,
				modelAll.getModelData(modelAll.getModelId("wolf")),
				textureAll.getTextureData(textureAll.getTextureId("red_light")),
				200f,70f,100f,  0f,0f,0f,  1f,1f,1f);

		GL30.glEnable(GL30.GL_CULL_FACE);
		GL30.glCullFace(GL30.GL_BACK);

	}

	//===============================================//
	//                                               //
	//          I N I T I A L I Z A T I O N          //
	//                                               //
	//===============================================//

	public GameRenderer(int width, int height) {
		frame = 0;
		onCreate(width, height);
	}

	public void onCreate(int width, int height) {
		//firstFrame=true;
		textView = Typeface.getFont("Kosugi Maru Regular");

		modelAll = new ModelLoader();
		modelCubemap = new ModelReader("resources/models/box.obj");
		modelQuad = new ModelReader("resources/models/quad.obj");

		String[] skyBoxSource = new String[] {
				"resources/shaders/sample/cubemap/stars/cubemap_0.png",
				"resources/shaders/sample/cubemap/stars/cubemap_2.png",
				"resources/shaders/sample/cubemap/stars/cubemap_4.png",
				"resources/shaders/sample/cubemap/stars/cubemap_5.png",
				"resources/shaders/sample/cubemap/stars/cubemap_3.png",
				"resources/shaders/sample/cubemap/stars/cubemap_1.png"
		};
		if(Settings.useSkyBox == 1) cubemapTexture = loadCubemapTexture(skyBoxSource);
		smallNoiseTexture = loadTexture("resources/shaders/sample/small_rgb_noise.png");
		cloudNoiseTexture = loadTexture("resources/shaders/sample/cloud_noise.png");
		tempcolorTexture = loadTexture("resources/shaders/sample/atmosphere_gradient.png");
		textureAll = new TextureLoader();

		textShader = new Shader("resources/shaders/text_vertex.txt", "resources/shaders/text_fragment.txt");
		renderShader = new Shader("resources/shaders/render_vertex.txt", "resources/shaders/render_fragment.txt");
		skyBoxShader = new Shader("resources/shaders/skybox_vertex.txt", "resources/shaders/skybox_fragment.txt");
		postProcessShader = new Shader("resources/shaders/quad_vertex.txt", "resources/shaders/postprocess_fragment.txt");

		entityPlayer = new Entity(
			modelAll.getModelData(modelAll.getModelId("sphere")),
			textureAll.getTextureData(textureAll.getTextureId("stone")),
			new float[] {300f, 67.5f, 200f},
			new float[] {0f,0f,0f},
			1f);
		entityPlayer.setCameraOffset(new float[] {0f,0f,0.1f});
		entityThing = new Entity(
			modelAll.getModelData(modelAll.getModelId("wolf")),
			textureAll.getTextureData(textureAll.getTextureId("wood")),
			new float[] {200f,100f,100f},
			new float[] {0f,0f,0f},
			1f);
		entityCenter = new Entity(
			modelAll.getModelData(modelAll.getModelId("box")),
			textureAll.getTextureData(textureAll.getTextureId("red_light")),
			new float[] {300f, 67.5f, 200f},
			new float[] {0f,0f,0f},
			1f);
		entityCenter.setDrawDistance(Settings.drawDistance);

		screenResolution[0] = (int)Math.ceil((float)width * Settings.screenQuality);
		screenResolution[1] = (int)Math.ceil((float)height * Settings.screenQuality);
		maxMipmapLevel = (int) (Math.log(Math.max(screenResolution[0], screenResolution[1])) / Math.log(2));

		renderFBO = GL30.glGenFramebuffers();
		renderRBO = GL30.glGenRenderbuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, renderFBO);

		renderColorTexture = GL30.glGenTextures();
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderColorTexture);
		//ByteBuffer bb = BufferUtils.createByteBuffer(screenResolution[0]*screenResolution[1]*3);
		GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGB, screenResolution[0], screenResolution[1], 0, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		//if(!firstFrame) GL30.glTexSubImage2D(GL30.GL_TEXTURE_2D, 0, 0, 0, screenResolution[0], screenResolution[1], GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE, bb);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_NEAREST);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_NEAREST);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, renderColorTexture, 0);

		renderDepthTexture = GL30.glGenTextures();
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderDepthTexture);
		GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RG32F, screenResolution[0], screenResolution[1], 0, GL30.GL_RG, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
	//	GL30.glTexSubImage2D(GL30.GL_TEXTURE_2D, 0, 0, 0, screenResolution[0], screenResolution[1], GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_NEAREST);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_NEAREST);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT1, GL30.GL_TEXTURE_2D, renderDepthTexture, 0);

		renderMerTexture = GL30.glGenTextures();
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderMerTexture);
		GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGB, screenResolution[0], screenResolution[1], 0, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
	//	GL30.glTexSubImage2D(GL30.GL_TEXTURE_2D, 0, 0, 0, screenResolution[0], screenResolution[1], GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_NEAREST);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_NEAREST);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT2, GL30.GL_TEXTURE_2D, renderMerTexture, 0);

		renderNormalTexture = GL30.glGenTextures();
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderNormalTexture);
		GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGB, screenResolution[0], screenResolution[1], 0, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
	//	GL30.glTexSubImage2D(GL30.GL_TEXTURE_2D, 0, 0, 0, screenResolution[0], screenResolution[1], GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_NEAREST);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_NEAREST);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT3, GL30.GL_TEXTURE_2D, renderNormalTexture, 0);

		int[] renderDrawBuffers = new int[]{GL30.GL_COLOR_ATTACHMENT0, GL30.GL_COLOR_ATTACHMENT1, GL30.GL_COLOR_ATTACHMENT2, GL30.GL_COLOR_ATTACHMENT3};
		GL30.glDrawBuffers(renderDrawBuffers);

		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, renderRBO);
		GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH_COMPONENT, screenResolution[0], screenResolution[1]);
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, renderRBO);

		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
		
		postProcessFBO = GL30.glGenFramebuffers();
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, postProcessFBO);

		finalTextureId = GL30.glGenTextures();
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, finalTextureId);
		GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGB, screenResolution[0], screenResolution[1], 0, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
	//	GL30.glTexSubImage2D(GL30.GL_TEXTURE_2D, 0, 0, 0, screenResolution[0], screenResolution[1], GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
		GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, finalTextureId, 0);

		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);

		// if(Settings.useHDR==1 || Settings.useBloom==1 || Settings.useSSGI==1) {
			lightingShader = new Shader("resources/shaders/quad_vertex.txt", "resources/shaders/lighting_fragment.txt");

			lightingFBO = GL30.glGenFramebuffers();
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, lightingFBO);
			
			lightingTexture = GL30.glGenTextures();
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, lightingTexture);
			GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGB, screenResolution[0], screenResolution[1], 0, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR_MIPMAP_LINEAR);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, lightingTexture, 0);

			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
		// }
		// if(Settings.useSSAO==1) {
			int[] ssaoResolution = new int[]{(int)Math.ceil((float)screenResolution[0]*Settings.SSAOQuality), (int)Math.ceil((float)screenResolution[1]*Settings.SSAOQuality)};
			SSAOShader = new Shader("resources/shaders/quad_vertex.txt", "resources/shaders/ssao_fragment.txt");

			SSAOFBO = GL30.glGenFramebuffers();
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, SSAOFBO);
			
			SSAOTexture = GL30.glGenTextures();
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, SSAOTexture);
			GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGB, ssaoResolution[0], ssaoResolution[1], 0, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, SSAOTexture, 0);

			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);

			for(int i=0; i<64; i++) {
				Point3 sample = new Point3(Math.random()*2.0-1.0, Math.random()*2.0-1.0, Math.random()*2.0-1.0);
				sample.x *= Math.random();
				sample.y *= Math.random();
				sample.z *= Math.random();
				float scale = (float)i / 64.0f;
				scale = MatMat.lerp(0.1f, 1.0f, scale * scale);
				sample.x *= scale;
				sample.y *= scale;
				sample.z *= scale;
				SSAOKernel[i] = sample;
			}
		// }
		// if(Settings.useSSGI==1) {
			int[] ssgiResolution = new int[]{(int)Math.ceil((float)screenResolution[0]*Settings.SSGIQuality), (int)Math.ceil((float)screenResolution[1]*Settings.SSGIQuality)};
			SSGIShader = new Shader("resources/shaders/quad_vertex.txt", "resources/shaders/ssgi_fragment.txt");

			SSGIFBO = GL30.glGenFramebuffers();
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, SSGIFBO);
			
			SSGITexture = GL30.glGenTextures();
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, SSGITexture);
			GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGB, ssgiResolution[0], ssgiResolution[1], 0, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, SSGITexture, 0);

			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
			// if(Settings.SSGIDenoise==1) {
				SSGIDenoiseShader = new Shader("resources/shaders/quad_vertex.txt", "resources/shaders/ssgi_denoise_fragment.txt");

				SSGIDenoiseFBO = GL30.glGenFramebuffers();
				GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, SSGIDenoiseFBO);
				
				SSGIDenoiseTexture = GL30.glGenTextures();
				GL30.glBindTexture(GL30.GL_TEXTURE_2D, SSGIDenoiseTexture);
				GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGB, ssgiResolution[0], ssgiResolution[1], 0, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
				GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
				GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
				GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
				GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
				GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, SSGIDenoiseTexture, 0);

				GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
				GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
			// }
		// }
		// if(Settings.useShadow==1) {
			shadowMapShader = new Shader("resources/shaders/shadowMap_vertex.txt", "resources/shaders/shadowMap_fragment.txt");

			shadowDepthFBO = GL30.glGenFramebuffers();
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowDepthFBO);
			
			shadowDepthTexture = GL30.glGenTextures();
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, shadowDepthTexture);
			GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_DEPTH_COMPONENT24, Settings.shadowResolution, Settings.shadowResolution, 0, GL30.GL_DEPTH_COMPONENT, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_NEAREST);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_NEAREST);
			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_TEXTURE_2D, shadowDepthTexture, 0);

			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
		// }
		// if(Settings.useReflection==1) {
			int[] reflectionResolution = new int[]{(int)Math.ceil((float)screenResolution[0]*Settings.reflectionQuality), (int)Math.ceil((float)screenResolution[1]*Settings.reflectionQuality)};
			reflectionShader = new Shader("resources/shaders/quad_vertex.txt", "resources/shaders/reflection_fragment.txt");

			reflectionFBO = GL30.glGenFramebuffers();
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, reflectionFBO);
			
			reflectionColorTexture = GL30.glGenTextures();
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, reflectionColorTexture);
			GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA, reflectionResolution[0], reflectionResolution[1], 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		//	GL30.glTexSubImage2D(GL30.GL_TEXTURE_2D, 0, 0, 0, screenResolution[0], screenResolution[1], GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, reflectionColorTexture, 0);

			reflectionMerTexture = GL30.glGenTextures();
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, reflectionMerTexture);
			GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGB, reflectionResolution[0], reflectionResolution[1], 0, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT1, GL30.GL_TEXTURE_2D, reflectionMerTexture, 0);

			int[] reflectionDrawBuffers = new int[]{GL30.GL_COLOR_ATTACHMENT0, GL30.GL_COLOR_ATTACHMENT1};
			GL30.glDrawBuffers(reflectionDrawBuffers);

			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
		// }

		for(int i=0; i<treeCount; i++){
			randTreePos
				.put(100f*(2f*(float)Math.random()-1f)+300f)
				.put(0.5f*(float)Math.random()+67f)
				.put(100f*(2f*(float)Math.random()-1f)+200f)
				.put(0.0f)
				.put(180f*(2f*(float)Math.random()-1f))
				.put(0.0f)
				.put(1.0f)
				.put(1.0f)
				.put(1.0f);
		}
		randTreePos.flip();
		treeInstance = new ModelReader("resources/models/terrain/pines.obj", randTreePos);
		int error = (error = GL30.glGetError()) != GL30.GL_NO_ERROR ? error : 0; if (error != 0) System.out.println("Kesalahan OpenGL terdeteksi: " + error);
		MemoryUtil.memFree(randTreePos);
	}

	public void onScreenSizeChanged(int width, int height) {
		screenResolution[0] = (int)Math.ceil((float)width * Settings.screenQuality);
		screenResolution[1] = (int)Math.ceil((float)height * Settings.screenQuality);
		maxMipmapLevel = (int) (Math.log(Math.max(screenResolution[0], screenResolution[1])) / Math.log(2));
	}

	public void onSettingsChanged() {
		textShader.refreshShader();
		renderShader.refreshShader();
		skyBoxShader.refreshShader();
		shadowMapShader.refreshShader();
		reflectionShader.refreshShader();
		lightingShader.refreshShader();
		SSAOShader.refreshShader();
		SSGIShader.refreshShader();
		SSGIDenoiseShader.refreshShader();
		postProcessShader.refreshShader();
	}

	public void onLoop(Camera cam, float time, float frameTime, boolean isPaused) {
		frame++;
		time = time * 6.0f;
		isRunning = true;
		camera = cam;
		Entity.setPaused(isPaused);

		entityPlayer.setRotation(new float[] {0f,180f+(float)Math.toDegrees(camera.getRotation()[0]),0f});
		if(time<5f) entityThing.setAcceleration(new float[] {0f,frameTime*10f,0f},frameTime);

		float playerMovement = 0f;
		if(Input.isKeyDown(Settings.keyForward) && !isPaused) {playerMovement = -Settings.playerSpeed;}else playerMovement = 0f;

		// if((int)time%10 == 0) {
		// 	entityPlayer.setPosition(new float[]{
		// 		180f*(2f*(float)Math.random()-1f),
		// 		180f*(float)Math.random(),
		// 		180f*(2f*(float)Math.random()-1f)
		// 	});
		// 	entityPlayer.setVelocity(new float[]{0f,0f,0f});
		// }

		if(Settings.physics){
			entityPlayer.setAcceleration(new float[]{	
												frameTime*playerMovement*((float)Math.sin(-camera.getRotation()[0]) * (float)Math.cos(camera.getRotation()[1])),
												frameTime*playerMovement*((float)Math.sin(camera.getRotation()[1])),
												frameTime*playerMovement*((float)Math.cos(camera.getRotation()[0]) * (float)Math.cos(camera.getRotation()[1]))
												},
												frameTime);
			entityPlayer.setAcceleration(new float[]{
												0f,
												frameTime*-9.80665f,
												0f },
												frameTime);

			//Floor collision
			float planePos=67.5f;
			if(entityPlayer.getPosition()[1]<planePos){
				entityPlayer.setPosition(new float[]{entityPlayer.getPosition()[0],planePos,entityPlayer.getPosition()[2]});
//				if(playerCol.getVelocity()[1]>=0.01625){
					entityPlayer.setVelocity(new float[]{entityPlayer.getVelocity()[0]/2f,-entityPlayer.getVelocity()[1]/2f,entityPlayer.getVelocity()[2]/2f});
/*				}else{
					playerCol.setVelocity(new float[]{playerCol.getVelocity()[0]/1.28f,0f,playerCol.getVelocity()[2]/1.28f});
					playerCol.setPosition(new float[]{playerCol.getPosition()[0],planePos,playerCol.getPosition()[2]});
				}
*/			}
		}else{
			entityPlayer.setVelocity(new float[]{	
											frameTime*playerMovement*((float)Math.sin(-camera.getRotation()[0]) * (float)Math.cos(camera.getRotation()[1])),
											frameTime*playerMovement*((float)Math.sin(camera.getRotation()[1])),
											frameTime*playerMovement*((float)Math.cos(camera.getRotation()[0]) * (float)Math.cos(camera.getRotation()[1]))
			} );
		}

		// float sunOffset = 0.25f*0.0174532925199f*2f*((float)Math.random()-1f);
		sunRotation[0] = toRad*(time/4f);
		sunRotation[1] = 0f;
		sunRotation[2] = toRad*30f;

		Vector4f sunDir = new Vector4f(0f, 0f, 0f, 1f);
		Matrix4f tr = new Matrix4f()
				.rotate(-sunRotation[2], 0f, 0f, 1f)
				.rotate(-sunRotation[1], 0f, 1f, 0f)
				.rotate(-sunRotation[0], 1f, 0f, 0f)
				.translate(0f, 0f, 1f);
		sunDir.mul(tr);

		//RENDER MATRIX
		if(!isPaused) {
			camera.update(new float[]{entityPlayer.getCameraPosition()[0], entityPlayer.getCameraPosition()[1], entityPlayer.getCameraPosition()[2]}, frameTime);
		}
		float[] cameraPosition = camera.getPosition();
		float daylight = (float)Math.sin((time/4f)*toRad);
		float ratio = (float) screenResolution[0] / (float) screenResolution[1];

		camera.setFovY((Input.isKeyDown(Settings.keyZoom) && !isPaused) ? Settings.fovZoom*toRad : Settings.fov*toRad);
		projectionMatrix.identity();
		projectionMatrix.perspective(camera.getFovY(), ratio, 0.0125f, Settings.drawDistance);

		translationMatrix.identity();
		translationMatrix.translate(-camera.getPivotPosition()[0], -camera.getPivotPosition()[1], -camera.getPivotPosition()[2]);
		rotationMatrix.identity();
		rotationMatrix.rotate(camera.getRotation()[1], 1f, 0f, 0f).rotate(camera.getRotation()[0], 0f, 1f, 0f);
		rotationMatrix.mul(translationMatrix, transformationMatrix);

		viewMatrix.identity();
		viewMatrix.lookAt(0f, 0f, camera.getDistance(), 0f, 0f, 0f, 0f, 1f, 0f);
		projectionMatrix.mul(viewMatrix, VPMatrix);
		VPMatrix.mul(transformationMatrix, MVPMatrix);
		MVPMatrix.get(MVPMatrixF);

		//CUBE MAP
		cubemapViewMatrix.identity();
		cubemapViewMatrix.lookAt(0f, 0f, mathSign(camera.getDistance())*0.0001f, 0f, 0f, 0f, 0f, 1f, 0f);
		projectionMatrix.mul(cubemapViewMatrix, cubemapVPMatrix);
		cubemapVPMatrix.mul(rotationMatrix, cubemapMVPMatrix);
		cubemapMVPMatrix.get(cubemapMVPMatrixF);

		//SHADOW MATRIX
		shadowRotationMatrix.identity();
		shadowRotationMatrix.rotate(sunRotation[0], 1f, 0f, 0f).rotate(sunRotation[1], 0f, 1f, 0f).rotate(sunRotation[2], 0f, 0f, 1f);
		shadowRotationMatrix.get(shadowRotationMatrixF);

		if(Settings.useShadow==1) {
			shadowProjectionMatrix.identity();
			shadowProjectionMatrix.ortho(-Settings.shadowDistance*0.5f, Settings.shadowDistance*0.5f, -Settings.shadowDistance*0.5f, Settings.shadowDistance*0.5f, -Settings.shadowDistance*2f, Settings.shadowDistance*2f);

			shadowRotationMatrix.mul(translationMatrix, shadowTransformationMatrix);
	
			shadowProjectionMatrix.mul(shadowTransformationMatrix, shadowMVPMatrix);
			shadowMVPMatrix.get(shadowMVPMatrixF);
		}



		//=============================//
		//     S H A D O W   M A P     //
		//=============================//
		if(Settings.useShadow==1) {
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowDepthFBO);
	
			if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) System.out.println("FrameBuffer error");
	
			GL30.glViewport(0, 0, Settings.shadowResolution, Settings.shadowResolution);
			GL30.glClearColor(1f, 1f, 1f, 1f);
			GL30.glClearDepth(1.0f);
			GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
	
			shadowMapShader.useShader();
			shadowMapShader.setMat4("MVP_MATRIX", shadowMVPMatrixF);
			shadowMapShader.setFloat("TIME", time);

			GL30.glEnable(GL30.GL_DEPTH_TEST);
			GL30.glDepthMask(true);
			GL30.glDepthFunc(GL30.GL_LESS);
			GL30.glEnable(GL30.GL_CULL_FACE);
			GL30.glCullFace(GL30.GL_FRONT);
			GL30.glDisable(GL30.GL_BLEND);

			renderObjects(shadowMapShader);

			GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
		}



		//===============================//
		//     M A I N   R E N D E R     //
		//===============================//
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, renderFBO);

		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, renderRBO);
		GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH_COMPONENT, screenResolution[0], screenResolution[1]);

		GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderColorTexture);
		GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGB, screenResolution[0], screenResolution[1], 0, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
		GL30.glGenerateMipmap(GL30.GL_TEXTURE_2D);

		GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderDepthTexture);
		GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RG32F, screenResolution[0], screenResolution[1], 0, GL30.GL_RG, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);

		GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderMerTexture);
		GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGB, screenResolution[0], screenResolution[1], 0, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);

		GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderNormalTexture);
		GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGB, screenResolution[0], screenResolution[1], 0, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);

		if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) System.out.println("FrameBuffer error");

		GL30.glViewport(0, 0, screenResolution[0], screenResolution[1]);
		GL30.glClearColor(0f, 0f, 0f, 1f);
		GL30.glClearDepth(1.0f);
		GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

		// CUBE MAP
		skyBoxShader.useShader();

		modelCubemap.getModel();
		skyBoxShader.setMat4("MVP_MATRIX", cubemapMVPMatrixF);
		skyBoxShader.setMat4("SHADOW_ROTATION_MATRIX", shadowRotationMatrixF);
		skyBoxShader.setVec3("SUN_DIR", new float[] {sunDir.x,sunDir.y,sunDir.z});
		skyBoxShader.setFloat("TIME", time);
		skyBoxShader.setFloat("DAYLIGHT", daylight);
		skyBoxShader.setFloat("DRAW_DISTANCE", Settings.drawDistance);

		GL30.glActiveTexture(GL30.GL_TEXTURE0);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, cloudNoiseTexture);
		skyBoxShader.setInt("TEXTURE_NOISE", 0);
		GL30.glActiveTexture(GL30.GL_TEXTURE1);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, tempcolorTexture);
		skyBoxShader.setInt("TEXTURE_TEMPCOLOR", 1);
		GL30.glActiveTexture(GL30.GL_TEXTURE2);
		GL30.glBindTexture(GL30.GL_TEXTURE_CUBE_MAP, cubemapTexture);
		skyBoxShader.setInt("TEXTURE_SKYNIGHT", 2);

		GL30.glDisable(GL30.GL_CULL_FACE);
		GL30.glEnable(GL30.GL_DEPTH_TEST);
		GL30.glDepthMask(true);
		GL30.glDepthFunc(GL30.GL_LEQUAL);

		modelCubemap.drawModel();

		//MAIN RENDER
		renderShader.useShader();
		renderShader.setMat4("MVP_MATRIX", MVPMatrixF);
		renderShader.setVec4("VIEW_POSITION", cameraPosition);
		renderShader.setFloat("TIME", time);
		renderShader.setFloat("DAYLIGHT", daylight);
		renderShader.setVec3("SUN_DIR", new float[] {sunDir.x,sunDir.y,sunDir.z});

		if(Settings.useShadow==1) {
			renderShader.setMat4("SHADOW_MVP_MATRIX", shadowMVPMatrixF);
			renderShader.setFloat("SHADOW_SIZE", Settings.shadowDistance);
			renderShader.setFloat("SHADOW_RESOLUTION", (float)Settings.shadowResolution);

			renderShader.setInt("DEPTH_MAP", 4);
			GL30.glActiveTexture(GL30.GL_TEXTURE4);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, shadowDepthTexture);
		}

		GL30.glEnable(GL30.GL_DEPTH_TEST);
		GL30.glDepthMask(true);
		GL30.glDepthFunc(GL30.GL_LESS);
		GL30.glEnable(GL30.GL_CULL_FACE);
		GL30.glCullFace(GL30.GL_BACK);
		GL30.glEnable(GL30.GL_BLEND);
		GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);

		renderObjects(renderShader);

		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);



		//=============================//
		//     R E F L E C T I O N     //
		//=============================//
		int[] reflectionResolution = new int[]{(int)Math.ceil((float)screenResolution[0]*Settings.reflectionQuality), (int)Math.ceil((float)screenResolution[1]*Settings.reflectionQuality)};
		float camDir = mathSign(camera.getDistance())==-1f?0f:(float)-Math.PI;
		float[] rotationMatrixF = new float[16];
		float[] viewMatrixF = new float[16];
		float[] projectionMatrixF = new float[16];
		Matrix4f cameraRotation = new Matrix4f()
				.rotate(-mathSign(camDir)*camera.getRotation()[1], 1f, 0f, 0f)
				.rotate(camDir+camera.getRotation()[0], 0f, 1f, 0f)
				.rotate(camera.getRotation()[2], 0f, 0f, 1f);
		cameraRotation.get(rotationMatrixF);
		viewMatrix.get(viewMatrixF);
		projectionMatrix.get(projectionMatrixF);
		if(Settings.useReflection==1) {
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, reflectionFBO);

			GL30.glBindTexture(GL30.GL_TEXTURE_2D, reflectionColorTexture);
			GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA, reflectionResolution[0], reflectionResolution[1], 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);

			GL30.glBindTexture(GL30.GL_TEXTURE_2D, reflectionMerTexture);
			GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA, reflectionResolution[0], reflectionResolution[1], 0, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);

			if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) System.out.println("FrameBuffer error");

			GL30.glViewport(0, 0, reflectionResolution[0], reflectionResolution[1]);
			GL30.glClearColor(0f, 0f, 0f, 0f);
			GL30.glClearDepth(1.0f);
			GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

			reflectionShader.useShader();

			modelQuad.getModel();
			reflectionShader.setMat4("ROTATION_MATRIX", rotationMatrixF);
			reflectionShader.setMat4("PROJECTION_MATRIX", projectionMatrixF);
			reflectionShader.setMat4("PROJ", Matriks.IdentityM4());
			reflectionShader.setVec2("RESOLUTION", new float[]{(float)reflectionResolution[0], (float)reflectionResolution[1]});

			GL30.glActiveTexture(GL30.GL_TEXTURE0);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderColorTexture);
			reflectionShader.setInt("TEXTURE_0", 0);
			GL30.glActiveTexture(GL30.GL_TEXTURE1);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderMerTexture);
			reflectionShader.setInt("TEXTURE_MER", 1);
			GL30.glActiveTexture(GL30.GL_TEXTURE2);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderDepthTexture);
			reflectionShader.setInt("TEXTURE_DEPTH", 2);
			GL30.glActiveTexture(GL30.GL_TEXTURE3);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderNormalTexture);
			reflectionShader.setInt("TEXTURE_NORMAL", 3);

			GL30.glDisable(GL30.GL_CULL_FACE);
			GL30.glDisable(GL30.GL_DEPTH_TEST);

			modelQuad.drawModel();
			GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
		}



		//=========================//
		//     L I G H T I N G     //
		//=========================//
		if(Settings.useHDR==1 || Settings.useBloom==1 || Settings.useSSGI==1) {
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, lightingFBO);

			GL30.glBindTexture(GL30.GL_TEXTURE_2D, lightingTexture);
			GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGB, screenResolution[0], screenResolution[1], 0, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
			GL30.glGenerateMipmap(GL30.GL_TEXTURE_2D);

			if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) System.out.println("FrameBuffer error");

			GL30.glViewport(0, 0, screenResolution[0], screenResolution[1]);
			GL30.glClearColor(0f, 0f, 0f, 1f);
			GL30.glClearDepth(1.0f);
			GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

			lightingShader.useShader();

			modelQuad.getModel();
			lightingShader.setMat4("PROJ", Matriks.IdentityM4());
			lightingShader.setVec2("RESOLUTION", new float[]{(float)screenResolution[0], (float)screenResolution[1]});
			lightingShader.setFloat("TIME", time);
			lightingShader.setFloat("gamma", Settings.gamma);

			GL30.glActiveTexture(GL30.GL_TEXTURE0);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderColorTexture);
			lightingShader.setFloat("exposure", sceneExposure);
			lightingShader.setInt("TEXTURE_0", 0);
			GL30.glActiveTexture(GL30.GL_TEXTURE1);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderMerTexture);
			lightingShader.setInt("TEXTURE_MER", 1);
			GL30.glActiveTexture(GL30.GL_TEXTURE2);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, reflectionMerTexture);
			lightingShader.setInt("TEXTURE_REFLECTION_MER", 2);
			GL30.glActiveTexture(GL30.GL_TEXTURE3);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, reflectionColorTexture);
			lightingShader.setInt("TEXTURE_REFLECTION", 3);

			GL30.glDisable(GL30.GL_CULL_FACE);
			GL30.glDisable(GL30.GL_DEPTH_TEST);

			modelQuad.drawModel();
			GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
		}



		//=====================================================================//
		//     S C R E E N   S P A C E   A M B I E N T   O C C L U S I O N     //
		//=====================================================================//
		int[] ssaoResolution = new int[]{(int)Math.ceil((float)screenResolution[0]*Settings.SSAOQuality), (int)Math.ceil((float)screenResolution[1]*Settings.SSAOQuality)};
		if(Settings.useSSAO==1) {
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, SSAOFBO);

			GL30.glBindTexture(GL30.GL_TEXTURE_2D, SSAOTexture);
			GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGB, ssaoResolution[0], ssaoResolution[1], 0, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
			GL30.glGenerateMipmap(GL30.GL_TEXTURE_2D);

			if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) System.out.println("FrameBuffer error");

			GL30.glViewport(0, 0, ssaoResolution[0], ssaoResolution[1]);
			GL30.glClearDepth(1.0f);
			GL30.glClear(GL30.GL_DEPTH_BUFFER_BIT);

			SSAOShader.useShader();

			modelQuad.getModel();
			SSAOShader.setMat4("PROJ", Matriks.IdentityM4());
			SSAOShader.setMat4("PROJECTION_MATRIX", projectionMatrixF);
			SSAOShader.setVec2("RESOLUTION", new float[]{(float)ssaoResolution[0], (float)ssaoResolution[1]});
			SSAOShader.setMat4("MVP_MATRIX", MVPMatrixF);
			SSAOShader.setInt("sampleCount", Settings.SSAOSample);
			for(int i=0; i<Settings.SSAOSample; i++)
				SSAOShader.setVec3("samples[" + i + "]", SSAOKernel[i].toFloatArray());

			GL30.glActiveTexture(GL30.GL_TEXTURE0);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, SSAOTexture);
			SSAOShader.setInt("TEXTURE_0", 0);
			GL30.glActiveTexture(GL30.GL_TEXTURE1);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, smallNoiseTexture);
			SSAOShader.setInt("TEXTURE_NOISE", 1);
			GL30.glActiveTexture(GL30.GL_TEXTURE2);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderDepthTexture);
			SSAOShader.setInt("TEXTURE_DEPTH", 2);
			GL30.glActiveTexture(GL30.GL_TEXTURE3);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderNormalTexture);
			SSAOShader.setInt("TEXTURE_NORMAL", 3);
			GL30.glActiveTexture(GL30.GL_TEXTURE4);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, lightingTexture);
			SSAOShader.setInt("TEXTURE_LIGHT", 4);

			GL30.glDisable(GL30.GL_CULL_FACE);
			GL30.glDisable(GL30.GL_DEPTH_TEST);

			modelQuad.drawModel();
			GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
		}



		//=========================================================================//
		//     S C R E E N   S P A C E   G L O B A L   I L L U M I N A T I O N     //
		//=========================================================================//
		int[] ssgiResolution = new int[]{(int)Math.ceil((float)screenResolution[0]*Settings.SSGIQuality), (int)Math.ceil((float)screenResolution[1]*Settings.SSGIQuality)};
		if(Settings.useSSGI==1) {
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, SSGIFBO);

			GL30.glBindTexture(GL30.GL_TEXTURE_2D, SSGITexture);
			GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGB, ssgiResolution[0], ssgiResolution[1], 0, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
			GL30.glGenerateMipmap(GL30.GL_TEXTURE_2D);

			if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) System.out.println("FrameBuffer error");

			GL30.glViewport(0, 0, ssgiResolution[0], ssgiResolution[1]);
			GL30.glClearDepth(1.0f);
			GL30.glClear(GL30.GL_DEPTH_BUFFER_BIT);

			SSGIShader.useShader();

			modelQuad.getModel();
			SSGIShader.setMat4("PROJ", Matriks.IdentityM4());
			SSGIShader.setVec2("RESOLUTION", new float[]{(float)ssgiResolution[0], (float)ssgiResolution[1]});
			SSGIShader.setFloat("TIME", time);
			SSGIShader.setInt("FRAME", frame);
			SSGIShader.setFloat("FOV", cam.getFovY());
			SSGIShader.setFloat("DRAW_DISTANCE", Settings.drawDistance);
			SSGIShader.setFloat("SSGI_DENSITY", Settings.SSGIDensity);
			SSGIShader.setVec4("VIEW_POSITION", cameraPosition);
			SSGIShader.setMat4("MVP_MATRIX", MVPMatrixF);

			GL30.glActiveTexture(GL30.GL_TEXTURE0);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, SSGITexture);
			SSGIShader.setInt("TEXTURE_0", 0);
			GL30.glActiveTexture(GL30.GL_TEXTURE1);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderColorTexture);
			SSGIShader.setInt("TEXTURE_COLOR", 1);
			GL30.glActiveTexture(GL30.GL_TEXTURE2);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderDepthTexture);
			SSGIShader.setInt("TEXTURE_DEPTH", 2);
			GL30.glActiveTexture(GL30.GL_TEXTURE3);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderNormalTexture);
			SSGIShader.setInt("TEXTURE_NORMAL", 3);
			GL30.glActiveTexture(GL30.GL_TEXTURE4);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, lightingTexture);
			SSGIShader.setInt("TEXTURE_LIGHT", 4);

			GL30.glDisable(GL30.GL_CULL_FACE);
			GL30.glDisable(GL30.GL_DEPTH_TEST);

			modelQuad.drawModel();
			GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
		}

		//=========================================================================================//
		//     S C R E E N   S P A C E   G L O B A L   I L L U M I N A T I O N   D E N O I S E     //
		//=========================================================================================//
		if(Settings.SSGIDenoise==1) {
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, SSGIDenoiseFBO);

			GL30.glBindTexture(GL30.GL_TEXTURE_2D, SSGIDenoiseTexture);
			GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGB, ssgiResolution[0], ssgiResolution[1], 0, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);
			GL30.glGenerateMipmap(GL30.GL_TEXTURE_2D);

			if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) System.out.println("FrameBuffer error");

			GL30.glViewport(0, 0, ssgiResolution[0], ssgiResolution[1]);
			GL30.glClearDepth(1.0f);
			GL30.glClear(GL30.GL_DEPTH_BUFFER_BIT);

			SSGIDenoiseShader.useShader();

			modelQuad.getModel();
			SSGIDenoiseShader.setMat4("PROJ", Matriks.IdentityM4());
			SSGIDenoiseShader.setVec2("RESOLUTION", new float[]{(float)ssgiResolution[0], (float)ssgiResolution[1]});
			SSGIDenoiseShader.setFloat("FOV", cam.getFovY());
			SSGIDenoiseShader.setVec4("VIEW_POSITION", cameraPosition);

			GL30.glActiveTexture(GL30.GL_TEXTURE0);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, SSGIDenoiseTexture);
			SSGIDenoiseShader.setInt("TEXTURE_0", 0);
			GL30.glActiveTexture(GL30.GL_TEXTURE1);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, SSGITexture);
			SSGIDenoiseShader.setInt("TEXTURE_1", 1);
			GL30.glActiveTexture(GL30.GL_TEXTURE2);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderDepthTexture);
			SSGIDenoiseShader.setInt("TEXTURE_DEPTH", 2);
			GL30.glActiveTexture(GL30.GL_TEXTURE3);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderNormalTexture);
			SSGIDenoiseShader.setInt("TEXTURE_NORMAL", 3);
			GL30.glActiveTexture(GL30.GL_TEXTURE4);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, lightingTexture);
			SSGIDenoiseShader.setInt("TEXTURE_LIGHT", 4);

			GL30.glDisable(GL30.GL_CULL_FACE);
			GL30.glDisable(GL30.GL_DEPTH_TEST);

			modelQuad.drawModel();
			GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
			GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
		}



		//===============================//
		//     P O S T P R O C E S S     //
		//===============================//
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, postProcessFBO);

		GL30.glBindTexture(GL30.GL_TEXTURE_2D, finalTextureId);
		GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGB, screenResolution[0], screenResolution[1], 0, GL30.GL_RGB, GL30.GL_UNSIGNED_BYTE, (ByteBuffer) null);

		if(GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) System.out.println("FrameBuffer error");

		GL30.glViewport(0, 0, screenResolution[0], screenResolution[1]);
		GL30.glClearColor(0f, 0f, 0f, 1f);
		GL30.glClearDepth(1.0f);
		GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

		postProcessShader.useShader();

		modelQuad.getModel();
		postProcessShader.setMat4("PROJ", Matriks.IdentityM4());
		postProcessShader.setMat4("MVP_MATRIX", MVPMatrixF);
		postProcessShader.setVec2("MAIN_RESOLUTION", new float[]{(float)screenResolution[0], (float)screenResolution[1]});
		postProcessShader.setVec2("REFLECTION_RESOLUTION", new float[]{(float)reflectionResolution[0], (float)reflectionResolution[1]});
		postProcessShader.setVec2("SSGI_RESOLUTION", new float[]{(float)ssgiResolution[0], (float)ssgiResolution[1]});
		postProcessShader.setVec2("SSAO_RESOLUTION", new float[]{(float)ssaoResolution[0], (float)ssaoResolution[1]});
		postProcessShader.setVec4("VIEW_POSITION", cameraPosition);
		postProcessShader.setFloat("TIME", time);
		postProcessShader.setFloat("gamma", Settings.gamma);

		GL30.glActiveTexture(GL30.GL_TEXTURE0);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderColorTexture);
		postProcessShader.setInt("TEXTURE_0", 0);
		GL30.glActiveTexture(GL30.GL_TEXTURE1);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderNormalTexture);
		postProcessShader.setInt("TEXTURE_NORMAL", 1);
		GL30.glActiveTexture(GL30.GL_TEXTURE2);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderMerTexture);
		postProcessShader.setInt("TEXTURE_MER", 2);
		GL30.glActiveTexture(GL30.GL_TEXTURE3);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, renderDepthTexture);
		postProcessShader.setInt("TEXTURE_DEPTH", 3);
		GL30.glActiveTexture(GL30.GL_TEXTURE4);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, reflectionColorTexture);
		postProcessShader.setInt("TEXTURE_REFLECTION", 4);
		GL30.glActiveTexture(GL30.GL_TEXTURE5);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, lightingTexture);
		if(Settings.useHDR == 1) {
			float[] luminescence = new float[3];
			GL30.glGetTexImage(GL30.GL_TEXTURE_2D, maxMipmapLevel, GL30.GL_RGB, GL30.GL_FLOAT, luminescence);
			float lum = 0.2126f * luminescence[0] + 0.7152f * luminescence[1] + 0.0722f * luminescence[2];

			sceneExposure = MatMat.lerp(sceneExposure, 0.5f / lum * Settings.sceneExposureMultiplier, Settings.HDRSpeed);
			sceneExposure = MatMat.clamp(sceneExposure, Settings.sceneExposureRangeMin, Settings.sceneExposureRangeMax);

			postProcessShader.setFloat("exposure", sceneExposure);
		}
		postProcessShader.setInt("TEXTURE_LIGHTING", 5);
		GL30.glActiveTexture(GL30.GL_TEXTURE6);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, SSGITexture);
		postProcessShader.setInt("TEXTURE_SSGI", 6);
		GL30.glActiveTexture(GL30.GL_TEXTURE7);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, SSGIDenoiseTexture);
		postProcessShader.setInt("TEXTURE_SSGI_DENOISE", 7);
		GL30.glActiveTexture(GL30.GL_TEXTURE8);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, SSAOTexture);
		postProcessShader.setInt("TEXTURE_SSAO", 8);

		GL30.glActiveTexture(GL30.GL_TEXTURE9);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, tempcolorTexture);
		postProcessShader.setInt("TEXTURE_TEMPCOLOR", 9);

		GL30.glDisable(GL30.GL_CULL_FACE);
		GL30.glDisable(GL30.GL_DEPTH_TEST);

		modelQuad.drawModel();
		textView.drawText(textShader, screenResolution[0]/4, screenResolution[1]/4, screenResolution[0]/2, screenResolution[1]/2, 0,
				"FPS: " + Main.fpsLimiter.getFps() +"\n"+
				"XYZ: " + String.format("%.6f", entityPlayer.getPosition()[0]) + ", " + String.format("%.6f", entityPlayer.getPosition()[1]) + ", " + String.format("%.6f", entityPlayer.getPosition()[2]) +"\n"+
				"View: " + String.format("%.6f", camera.getRotation()[0]*57.295779513082) + ", " + String.format("%.6f", camera.getRotation()[1]*57.295779513082) + ", " + String.format("%.6f", camera.getRotation()[2]*57.295779513082) +"\n"+
				"FOV Y: " + String.format("%.6f", camera.getFovY()*57.295779513082) + "\n" +
				"Exposure: " + sceneExposure
			, 0xFFFFFFFF);
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);

		//firstFrame=false;
	}

	public int getTextureId() {
		return finalTextureId;
	}
	public void deleteTextures() {
		// NOTHING
	}

	public void onDestroy() {
		isRunning = false;

		textView.dispose();

		textShader.delete();
		renderShader.delete();
		skyBoxShader.delete();
		postProcessShader.delete();

		GL30.glDeleteTextures(renderColorTexture);
		GL30.glDeleteTextures(renderDepthTexture);
		GL30.glDeleteTextures(renderMerTexture);
		GL30.glDeleteTextures(renderNormalTexture);
		GL30.glDeleteTextures(finalTextureId);

		GL30.glDeleteFramebuffers(renderFBO); GL30.glDeleteRenderbuffers(renderRBO);
		GL30.glDeleteFramebuffers(postProcessFBO);
		// if(Settings.useBloom==1 || Settings.useSSGI==1) {
			lightingShader.delete();
			GL30.glDeleteFramebuffers(lightingFBO);
			GL30.glDeleteTextures(lightingTexture);
		// }
		// if(Settings.useSSAO==1) {
			SSAOShader.delete();
			GL30.glDeleteFramebuffers(SSAOFBO);
			GL30.glDeleteTextures(SSAOTexture);
		// }
		// if(Settings.useSSGI==1) {
			SSGIShader.delete();
			GL30.glDeleteFramebuffers(SSGIFBO);
			GL30.glDeleteTextures(SSGITexture);
			// if(Settings.SSGIDenoise==1) {
				SSGIDenoiseShader.delete();
				GL30.glDeleteFramebuffers(SSGIDenoiseFBO);
				GL30.glDeleteTextures(SSGIDenoiseTexture);
			// }
		// }
		// if(Settings.useReflection==1) {
			reflectionShader.delete();
			GL30.glDeleteFramebuffers(reflectionFBO);
			GL30.glDeleteTextures(reflectionColorTexture);
			GL30.glDeleteTextures(reflectionMerTexture);
		// }
		// if(Settings.useShadow==1) {
			shadowMapShader.delete();
			GL30.glDeleteFramebuffers(shadowDepthFBO);
			GL30.glDeleteTextures(shadowDepthTexture);
		// }

		modelCubemap.deleteModel();
		modelQuad.deleteModel();
		modelAll.deleteModels();
		treeInstance.deleteModel();

		if(Settings.useSkyBox==1 || cubemapTexture != 0) GL30.glDeleteTextures(cubemapTexture);
		GL30.glDeleteTextures(smallNoiseTexture);
		GL30.glDeleteTextures(cloudNoiseTexture);
		GL30.glDeleteTextures(tempcolorTexture);
		textureAll.deleteTextures();
	}

}
