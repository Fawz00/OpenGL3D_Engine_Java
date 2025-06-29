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
	public static boolean smoothCamera			= true;
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

	public static boolean ASCIICharOnly			= false; // 0xFF=8bits; 0xFFFF=16bits; 0x0010FFFF=21bits;

	public static float fov						= 70f;
	public static float fovZoom					= 20f;
	public static float cameraDistance			= 5f;

	public static float drawDistance			= 10000f;
	public static float entityRenderDistance	= 120f;

	//GRAPHICS
	public static float screenQuality			= 0.5f; // Percent of display resolution
	public static byte FXAA						= 1; // 0: OFF; 1: ON

	public static float gamma					= 2.2f;
	public static byte useHDR					= 1;
	public static float HDRSpeed				= 0.025f;
	public static float sceneExposureMultiplier	= 0.125f;
	public static float sceneExposureRangeMax	= 2.2f;
	public static float sceneExposureRangeMin	= -2.2f;

	public static byte useSkyBox				= 1;
	public static byte useCloud					= 1; // 0=OFF 1=FAST

	public static byte useNormalMapping			= 1;
	public static byte useParallaxMapping		= 1;

	public static byte useShadow				= 1;
	public static int shadowResolution			= 4096;
	public static float shadowDistance			= 250;
	public static byte usePenumbraShadow		= 1;

	public static byte useReflection			= 1;
	public static float reflectionQuality		= 0.5f; // Percent of screenQuality

	public static byte useBloom					= 1;
	public static byte useLensFlare				= 0; // next update!

	public static byte useSSAO					= 1;
	public static int SSAOSample				= 24;
	public static float SSAOQuality				= 0.5f; // note: lower better

	public static byte useSSGI					= 1;
	public static float SSGIDensity				= 0.75f; // Sample density controls accuracy; 0.0 - 0.9; Default = 0.75
	public static float SSGIQuality				= 0.75f; // Percent of screenQuality
	public static int SSGIDenoise				= 0; // in development

}
