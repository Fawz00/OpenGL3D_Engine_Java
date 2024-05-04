package opengl3d;

import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.lwjgl.openal.AL10;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;
import org.joml.Matrix4f;

import opengl3d.audio.AudioMaster;
import opengl3d.audio.AudioSource;
import opengl3d.engine.Input;
import opengl3d.ui.Point2D;
import opengl3d.ui.UIBox;
import opengl3d.ui.UIEvent;
import opengl3d.utils.ModelReader;
import opengl3d.utils.Shader;
import opengl3d.utils.text.Font;

public class Renderer {
	private static boolean isPaused = false;

	private AudioSource audioSourceSelf;
	private int audioBacksound;
	private int audioIntro;

	private Font textView;
	private Shader textShader;

	private Shader uiShader;

	private Shader mainShader;
	private ModelReader modelQuad;
	private GameRenderer gameTexture;
	public static Camera camera;

	private Matrix4f projectionMatrix = new Matrix4f();
	private float[] projectionMatrixF = new float[16];
	private float[] baseColor = new float[4];
	private static float colorInvisible[] = {1f, 1f, 1f, 0f};
	private static float colorVisible[] = {1f, 1f, 1f, 1f};
	private float toRad = 0.0174532925199f;

	int[] textures = new int[3];

	private long gameStartTime;
	private int[] screenResolution = new int[2];
	private static float sysTime = 0f;
	private static float gameTime = 0f;
	private static float timeOffset = 0f;
	private static float timeOffsetStart = 0f;
	private static float frameTime;
	private static float frameTimeStart;

	UIBox button;

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
	
			for (int y = height-1; y >= 0; y--){
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
	
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_NEAREST);
			GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_NEAREST);

			if (buffer != null) {
				GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA, width, height, 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, buffer);
			}
			MemoryUtil.memFree(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		};

		return textureId;
	}



	//=============================//
	//   V I D E O   E F F E C T   //
	//=============================//

	private static float[] fadeFx(float time, float startTime, float duration, float[] var, float[] colStart, float[] colEnd){
		float[] result = new float[4];
		if(time>=startTime && time<=startTime+duration){
			float currentDuration = time - startTime;
			float progress = currentDuration / duration;

			result[0] = colStart[0]*(1f-progress) + colEnd[0]*progress;
			result[1] = colStart[1]*(1f-progress) + colEnd[1]*progress;
			result[2] = colStart[2]*(1f-progress) + colEnd[2]*progress;
			result[3] = colStart[3]*(1f-progress) + colEnd[3]*progress;
			return result;
		} else return var;
	}
	private static void setTexture(float time, float startTime, int texture){
		if(time>=startTime){
			GL30.glBindTexture(GL30.GL_TEXTURE_2D, texture);
		}
	}
	private static float[] setColor(float time, float startTime, float[] var, float[] color){
		if(time>=startTime){
			return color;
		} else return var;
	}
	private static void setScale(float time, float startTime, Shader shader, Matrix4f matrix, float x, float y){
		if(time>=startTime){
			float[] matrixF = new float[16];
			matrix.scale(x,y,1f);
			matrix.get(matrixF);
			shader.setMat4("PROJ", matrixF);
			matrix.scale(1f/x,1f/y,1f);
		}
	}

	public Renderer() {
		audioSourceSelf = new AudioSource();

		audioIntro = AudioMaster.loadSound("resources/sound/intro.wav");
		AL10.alSourcei(audioIntro, AL10.AL_LOOPING, AL10.AL_FALSE);
		AL10.alSourcef(audioIntro, AL10.AL_GAIN, Settings.volumeMusic);

		audioBacksound = AudioMaster.loadSound("resources/sound/ambient.wav");
		AL10.alSourcei(audioBacksound, AL10.AL_LOOPING, AL10.AL_TRUE);
		AL10.alSourcef(audioBacksound, AL10.AL_GAIN, Settings.volumeMusic);
	}

	public void onCreate(int width, int height) {
		button = new UIBox("button", 0, 0, 1080, 1080);
		button.setEvent(new UIEvent(){
			@Override
			public void runOnClick() {
				System.out.println("Halo!");
				super.runOnClick();
			}
		});
		Input.setOnClickEventListener(button);
		mainShader = new Shader("resources/shaders/quad_vertex.txt", "resources/shaders/quad_fragment.txt");
		textShader = new Shader("resources/shaders/text_vertex.txt", "resources/shaders/text_fragment.txt");
		uiShader = new Shader("resources/shaders/ui_vertex.txt", "resources/shaders/ui_fragment.txt");

		modelQuad = new ModelReader("resources/models/quad.obj");
		textures[0] = loadTexture("resources/textures/ui/splash0.png");
		textures[1] = loadTexture("resources/textures/ui/splash1.png");
		textures[2] = loadTexture("resources/textures/ui/splash2.png");
		camera = new Camera(0, Settings.fov*toRad, new float[] {0f,0f,0f});

		try {
			InputStream fontData = new ByteArrayInputStream(Files.readAllBytes(Paths.get("resources/fonts/Kosugi_Maru/KosugiMaru-Regular.ttf")));
			textView = new Font(fontData, 38, true);
		} catch (FontFormatException e) {
			System.out.println("Error loading font, default value is used.");
			textView = new Font(38, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//textView = new Font(38, true);

		gameTexture = new GameRenderer(width, height);

		if(Settings.splashScreen) audioSourceSelf.play(audioIntro);
		frameTimeStart = (float)System.nanoTime();
		gameStartTime = System.currentTimeMillis();
	}

	public void onScreenSizeChanged(int width, int height) {
		float ratio = (float) width / (float) height;
		screenResolution[0] = width;
		screenResolution[1] = height;

		projectionMatrix.setOrtho(-ratio, ratio, -1f, 1f, -1f, 1f, false);
		gameTexture.onScreenSizeChanged(width, height);
	}

	public static void togglePauseStatus() {
		if(isPaused) {
			setResume();
		} else {
			setPaused();
		}
	}
	public static void setPaused() {
		timeOffsetStart = gameTime;
		camera.pause();
		isPaused = true;
	}
	public static void setResume() {
		camera.resume();
		frameTime = 0f;
		isPaused = false;
	}

	public static boolean isPaused() {
		return isPaused;
	}

	public void onLoop() {
		sysTime = ((float)(System.currentTimeMillis()-gameStartTime) / 1000f);
		if(isPaused) timeOffset = sysTime-timeOffsetStart;
		gameTime = sysTime-timeOffset;
		frameTime = ((float)(System.nanoTime()-frameTimeStart)/1000000000f);
		if(isPaused) frameTime = 0f;
		frameTimeStart = (float)System.nanoTime();

		if(sysTime >= 12 || !Settings.splashScreen) {
			int ALStatusIntro = AL10.alGetSourcei(audioIntro, AL10.AL_SOURCE_STATE);
			if(ALStatusIntro != AL10.AL_PLAYING) {
				audioSourceSelf.stop(audioIntro);
				audioSourceSelf.play(audioBacksound);
			}

			float delay = 12f;
			if(!Settings.splashScreen) delay = 0f;
			gameTexture.onLoop(camera, gameTime-delay, frameTime, isPaused);
		}

		////__Splash Screen__////
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
		GL11.glViewport(0, 0, screenResolution[0], screenResolution[1]);
		GL30.glClearColor(1f, 1f, 1f, 1f);
		GL30.glClear(GL30.GL_COLOR_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);
		mainShader.useShader();
		mainShader.setMat4("PROJ", projectionMatrixF);
		GL30.glActiveTexture(GL30.GL_TEXTURE0);

		if(Settings.splashScreen) {
			//effects
			setTexture(sysTime, 0f, textures[0]);
			setScale(sysTime, 0f, mainShader, projectionMatrix, 1f, 1f);
			baseColor = fadeFx(sysTime, 0f, 1f, baseColor, colorInvisible, colorVisible);
			baseColor = fadeFx(sysTime, 3f, 1f, baseColor, colorVisible, colorInvisible);
	
			setTexture(sysTime, 4f, textures[1]);
			setScale(sysTime, 4f, mainShader, projectionMatrix, 0.25f, 0.25f);
			baseColor = fadeFx(sysTime, 4f, 1f, baseColor, colorInvisible, colorVisible);
			baseColor = fadeFx(sysTime, 7f, 1f, baseColor, colorVisible, colorInvisible);
	
			setTexture(sysTime, 8f, textures[2]);
			setScale(sysTime, 8f, mainShader, projectionMatrix, 0.75f, 0.75f);
			baseColor = fadeFx(sysTime, 8f, 1f, baseColor, colorInvisible, colorVisible);
			baseColor = fadeFx(sysTime, 11f, 1f, baseColor, colorVisible, colorInvisible);
	
			if(sysTime >= 12f) {
				if(isPaused) {
					Main.window.showCursor(true);
					baseColor = setColor(sysTime, 0f, colorVisible, new float[] {0.5f,0.5f,0.5f,1f});
				} else {
					Main.window.showCursor(false);
					baseColor = setColor(sysTime, 0f, colorVisible, colorVisible);
				}
				setTexture(sysTime, 12f, gameTexture.getTextureId());
				setScale(sysTime, 12f, mainShader, projectionMatrix, (float)screenResolution[0]/(float)screenResolution[1], 1f);
				baseColor = fadeFx(sysTime, 12f, 3f, baseColor, colorInvisible, colorVisible);
			}
		} else {
			if(isPaused) {
				Main.window.showCursor(true);
				baseColor = setColor(sysTime, 0f, colorVisible, new float[] {0.5f,0.5f,0.5f,1f});
			} else {
				Main.window.showCursor(false);
				baseColor = setColor(sysTime, 0f, colorVisible, colorVisible);
			}
			setTexture(sysTime, 0f, gameTexture.getTextureId());
			setScale(sysTime, 0f, mainShader, projectionMatrix, (float)screenResolution[0]/(float)screenResolution[1], 1f);
			baseColor = fadeFx(sysTime, 0f, 3f, baseColor, colorInvisible, colorVisible);
		}

		//shader
		modelQuad.getModel();
		projectionMatrix.get(projectionMatrixF);
		mainShader.setFloat("TIME", sysTime);
		mainShader.setVec4("BASE_COLOR", baseColor);
		mainShader.setInt("TEXTURE_0", 0);

		GL30.glEnable(GL30.GL_DEPTH_TEST);
		GL30.glDepthMask(true);
		GL30.glDepthFunc(GL30.GL_LESS);
		GL30.glDisable(GL30.GL_CULL_FACE);
		GL30.glEnable(GL30.GL_BLEND);
		GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);

		modelQuad.drawModel();
		GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);
		// ModelReader.resetModel();

		String text = "しばらくお待ちください。\n第二次世界大戦では、1942年初頭に大日本帝国がBelandaオランダ植民地軍を破り東インドのほぼ全域を占領し、その後日本軍政当局がバタヴィアをジャカルタと改称した。以後、その名称は現在に至っている[7]。なお日本軍は市政からオランダ人を放逐した。日本軍の占領は1945年8月まで続いた。";
		String halo = "游戏剧情于虚构世界的提瓦特大陆上展开，该世界分成七个国家，每个国家分别以一种元素为主题，并由对应元素的神明所分管。\n"
				+ "游戏剧情的主角为“旅行者”，是一对在无数个世界中旅行的兄妹，因遭遇陌生神明阻拦在提瓦特被迫分離。玩家将扮演旅行者，为了寻找自己失散的唯一血亲，並與派蒙一同游历七国。\n"
				+ "\n"
				+ "괜찮아요.";
		String lorem = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed sit amet rutrum nulla, vel fermentum justo. Donec dictum enim massa, at sagittis urna consequat in. Proin tempus vitae lorem et hendrerit. Curabitur viverra in ipsum eu pulvinar. Praesent semper mi nunc, ac auctor leo ullamcorper eu. In tincidunt commodo sapien, sit amet euismod lorem ultrices id. Fusce vehicula leo id enim lobortis, vel vestibulum lectus pulvinar. Phasellus in dolor libero. Nunc rutrum cursus lectus in tristique. Vivamus eu nulla et diam tincidunt dignissim ut eu enim. Fusce eu eros ultricies, tempus nibh ut, blandit ante. Suspendisse sit amet sagittis est. Aenean rutrum convallis urna posuere mattis. Aenean et massa vehicula, aliquam dui faucibus, tempus nisl. Suspendisse nulla lectus, sagittis nec imperdiet at, commodo a tellus.";
		Input.isTyping(isPaused());
		String chat = "";
		for(int i=10; i>0; i--) {
			int index = Input.getLineCount()-i;
			chat += Input.getLine( index ) + "\n";
			// if(!Input.getLine( Input.getLineCount()-11 ).equals("")) Input.resetLines();
		}
		chat += Input.getRawLine() + "_";
		//textView.drawText(textShader, new int[] {screenResolution[0], screenResolution[1]}, 0, 0, screenResolution[0]/2, screenResolution[1], text +halo+ "\nFPS: "+Main.fpsLimiter.getFps() + "\n\n$c00eeffff========== C H A T ==========$cffffffff\n" + chat, 0xFF8800FF);
	
		textView.drawWord(textShader, new int[] {screenResolution[0], screenResolution[1]}, 0, 0, screenResolution[0]/2, screenResolution[1], "こんにちは、世界！ ꦱꦸꦒꦼꦁ​​ꦲꦺꦚ꧀ꦗꦁ​꧈​​ꦢꦺꦴꦚ​" + "\nFPS: "+Main.fpsLimiter.getFps() + "\n\n__________ C H A T __________\n" + chat, 0xFFFFFFFF);
		button.draw(uiShader, new Point2D(screenResolution[0], screenResolution[1]));

		gameTexture.deleteTextures();
	}

	public void onDestroy() {
		if(textures[0]!=0) GL30.glDeleteTextures(textures[0]);
		if(textures[1]!=0) GL30.glDeleteTextures(textures[1]);
		if(textures[2]!=0) GL30.glDeleteTextures(textures[2]);
		textShader.delete();
		mainShader.delete();
		modelQuad.deleteModel();
		gameTexture.onDestroy();
		audioSourceSelf.delete();
		textView.dispose();
		uiShader.delete();
		button.destroy();
	}

}
