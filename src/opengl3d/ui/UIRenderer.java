package opengl3d.ui;

import java.awt.FontFormatException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import opengl3d.utils.ModelReader;
import opengl3d.utils.Point2;
import opengl3d.utils.Shader;
import opengl3d.utils.text.Font;

public class UIRenderer {
    private static Font defaultFont;
	private static Shader textShader;
	private static Shader uiShader;
    private static ModelReader modelQuad;
    private static Point2 screenResolution;

    private UIRenderer() {}

    public static void init() {
        try {
			InputStream fontData = new ByteArrayInputStream(Files.readAllBytes(Paths.get("resources/fonts/Kosugi_Maru/KosugiMaru-Regular.ttf")));
			defaultFont = new Font(fontData, 38, true);
		} catch (FontFormatException e) {
			System.out.println("Error loading font, default value is used.");
			defaultFont = new Font(38, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
        //defaultFont = new Font(38, true);

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
