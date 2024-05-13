package opengl3d.ui;

import opengl3d.utils.ModelReader;
import opengl3d.utils.Point2;
import opengl3d.utils.Shader;
import opengl3d.utils.text.Font;
import opengl3d.utils.text.Typeface;

public class UIRenderer {
    private static Font defaultFont;
	private static Shader textShader;
	private static Shader uiShader;
    private static ModelReader modelQuad;
    private static Point2 screenResolution;

    private UIRenderer() {}

    public static void init() {
        Typeface.loadFont("resources/fonts/Kosugi_Maru/KosugiMaru-Regular.ttf");
        defaultFont = Typeface.getFont("Kosugi Maru Regular");

        modelQuad = new ModelReader("resources/models/quad.obj");

        textShader = new Shader("resources/shaders/text_vertex.txt", "resources/shaders/text_fragment.txt");
		uiShader = new Shader("resources/shaders/ui_vertex.txt", "resources/shaders/ui_fragment.txt");
    }

    public static void setScreenResolution(Point2 resolution) {
        screenResolution = resolution;
    }

    public static void destroy() {
        modelQuad.deleteModel();
        textShader.delete();
		uiShader.delete();
        defaultFont.dispose();
    }

    public static Shader getTextShader() {
        return textShader;
    }
    public static Shader getUIShader() {
        return uiShader;
    }
    public static Font getFont() {
        return defaultFont;
    }
    public static ModelReader getQuadModel() {
        return modelQuad;
    }
    public static Point2 getScreenSize() {
        return screenResolution;
    }
}
