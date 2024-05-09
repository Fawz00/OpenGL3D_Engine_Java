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
	public static boolean fontDebug				= false;
	public static boolean printLog				= true; // Not implemented yet


	//GAME
	public static boolean splashScreen			= false;
	public static boolean physics				= false;
	public static float playerSpeed				= 50f/3.6f;

	//CONTROL MAPPING
	public static float cameraSensitivity		= 1.0f;
	public static boolean smoothCamera			= false;
	public static int buttonPrimary				= GLFW.GLFW_MOUSE_BUTTON_LEFT;
	public static int keyForward				= GLFW.GLFW_KEY_SPACE;
	public static int keyToggleCamera			= GLFW.GLFW_KEY_C;
	public static int keyZoom					= GLFW.GLFW_KEY_Z;
	public static int keyTogglePhysics			= GLFW.GLFW_KEY_P;

	//AUDIO CONFIG
	public static float volumeMusic				= 1.00f;

	//RENDER CONFIG
	public static boolean limitFps				= false;
	public static int fpsLimit					= 60;
	public static boolean vsync					= true;

	public static boolean ASCIICharOnly			= true; // 0xFF=8bits; 0xFFFF=16bits; 0x10FFFF=21bits;

	public static float fov						= 70f;
	public static float fovZoom					= 20f;
	public static float cameraDistance			= 5f;

	public static float drawDistance			= 10000f;
	public static float entityRenderDistance	= 120f;

	//GRAPHICS
	public static float screenQuality			= 1.00f; // Percent of display resolution
	public static int FXAA						= 1; // 0: OFF; 1: ON

	public static float gamma					= 2.2f;
	public static int useHDR					= 1;
	public static float HDRSpeed				= 0.025f;
	public static float sceneExposureMultiplier	= 0.125f;
	public static float sceneExposureRangeMax	= 2.2f;
	public static float sceneExposureRangeMin	= -2.2f;

	public static int useSkyBox					= 1;
	public static int useCloud					= 1; // 0=LOW 1=MEDIUM 2=ULTRA

	public static int useNormalMapping			= 1;
	public static int useParallaxMapping		= 1;

	public static int useShadow					= 1;
	public static int shadowResolution			= 2024;
	public static float shadowDistance			= 250;
	public static int usePenumbraShadow			= 1;

	public static int useReflection				= 1;
	public static float reflectionQuality		= 0.5f; // Percent of screenQuality

	public static int useBloom					= 1; // 1=FAST; 2=QUALITY
	public static int useLensFlare				= 0;

	public static int useSSAO					= 0; // next update!

	public static int useSSGI					= 1;
	public static float SSGIQuality				= 0.75f; // Percent of screenQuality
	public static int SSGIDenoise				= 0; // in development

}
