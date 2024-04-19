package opengl3d;

import org.lwjgl.glfw.GLFW;

public class Settings {

	//=========================//
	//     S E T T I N G S     //
	//=========================//

	//CREATE WINDOW
	public static int[] windowSize				= new int[] {1080, 720};
	public static boolean fullscreen			= false;
	public static boolean maximized				= false;

	//DEBUGGING
	public static boolean enableGLDebug			= false;

	//GAME
	public static boolean splashScreen			= false;
	public static boolean physics				= true;
	public static float playerSpeed				= 50f/3.6f;

	//CONTROL MAPPING
	public static float cameraSensitivity		= 0.9f;
	public static boolean smoothCamera			= true;
	public static int keyForward				= GLFW.GLFW_KEY_SPACE;
	public static int keyToggleCamera			= GLFW.GLFW_KEY_C;
	public static int keyZoom					= GLFW.GLFW_KEY_Z;
	public static int keyTogglePhysics			= GLFW.GLFW_KEY_P;

	//AUDIO CONFIG
	public static float volumeMusic				= 0.00f;

	//RENDER CONFIG
	public static boolean limitFps				= true;
	public static int fpsLimit					= 160;
	public static boolean vsync					= true;

	public static boolean ASCIICharOnly			= true; // 0xFF=8bits; 0xFFFF=16bits; 0x10FFFF=21bits;

	public static float fov						= 70f;
	public static float fovZoom					= 20f;
	public static float cameraDistance			= 3f;

	public static float drawDistance			= 10000f;
	public static float entityRenderDistance	= 120f;

	//GRAPHICS
	public static float screenQuality			= 1.00f; // Percent of display resolution
	public static int FXAA						= 1; // 0: OFF; 1: ON

	public static float gamma					= 1.2f;
	public static int useHDR					= 1;
	public static float HDRSpeed				= 0.05f;
	public static float sceneExposureMultiplier	= 1.1f;
	public static float sceneExposureRangeMax	= 2.0f;
	public static float sceneExposureRangeMin	= -2.2f;

	public static int useSkyBox					= 1;
	public static int useCloud					= 1; // 0=LOW 1=MEDIUM 2=ULTRA

	public static int useNormalMapping			= 1;
	public static int useParallaxMapping		= 1;

	public static int useShadow					= 1;
	public static int shadowResolution			= 1024;
	public static float shadowDistance			= 25;
	public static int usePenumbraShadow			= 0;

	public static int useReflection				= 1;
	public static float reflectionQuality		= 0.5f; // Percent of screenQuality

	public static int useBloom					= 1;

	public static int useSSAO					= 0; // in development

	public static int useSSGI					= 1; // in development
	public static float SSGIQuality				= 1.00f; // Percent of screenQuality
	public static int SSGIDenoise				= 0; // in development

}
