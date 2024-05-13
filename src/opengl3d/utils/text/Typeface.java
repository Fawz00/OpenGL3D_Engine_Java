package opengl3d.utils.text;

import java.awt.FontFormatException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class Typeface {
    private static HashMap<String, Font> typefaces = new HashMap<String, Font>();

    private Typeface(){}
    public static void init() {
        loadFont(new Font(38, true));
    }

    public static void loadFont(Font font) {
        typefaces.put(font.getFontName(), font);
    }
    public static void loadFont(String path) {
         try {
			InputStream fontData = new ByteArrayInputStream(Files.readAllBytes(Paths.get(path)));
			Font font = new Font(fontData, 38, true);
            typefaces.put(font.getFontName(), font);
		} catch (FontFormatException e) {
			System.out.println("TYPEFACE: Error loading font " + path);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public static Font getFont(String name) {
        Font result = typefaces.get(name);
        if(result == null) return typefaces.get("Monospaced.plain");
        return result;
    }

    public static void removeFont(String name) {
        typefaces.remove(name);
    }
    public static void clearAll() {
        typefaces.clear();
    }
}
