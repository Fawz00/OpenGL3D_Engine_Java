package opengl3d.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileUtils {

	public static String loadAsString(String dir) {
		StringBuilder result = new StringBuilder();

		try(BufferedReader reader = new BufferedReader(new InputStreamReader(FileUtils.class.getResourceAsStream(dir)))) {
			String line = "";
			while((line = reader.readLine()) != null) {
				result.append(line).append("\n");
			}
		} catch(IOException e) {
			System.err.println("Couldn't find the file at " + dir);
		}
		return result.toString();
	}

}
