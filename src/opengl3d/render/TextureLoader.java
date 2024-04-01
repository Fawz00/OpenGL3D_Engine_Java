package opengl3d.render;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.opengl.GL30;

import opengl3d.utils.TextureReader;

public class TextureLoader {
	private TextureReader[] texture;
	private int texturesTotal;
	private String jsonTexture;
	private boolean hasColor = true;
	private boolean hasNormal = false;
	private boolean hasParallax = false;
	private boolean hasMER = false;
	private boolean hasAO = false;
	private HashMap<String, Integer> texturesHash = new HashMap<String, Integer>();

	//=======================//
	//     T E X T U R E     //
	//=======================//

	public TextureLoader(){
		jsonTexture = loadFile("resources/maps/textures_loader.json");

		if(jsonTexture != null) try{

			JSONObject jsonFile = new JSONObject(jsonTexture);
			int version = jsonFile.getInt("version");
			if(version <= 1){

				JSONObject textures = jsonFile.getJSONObject("textures");
				texturesTotal = textures.length();
				texture = new TextureReader[texturesTotal];
				@SuppressWarnings("unchecked")
				Iterator<String> texturesList = textures.keys();
				int texturesCounter = 0;
				while(texturesList.hasNext()) {
					String textureName = texturesList.next();
					JSONObject textureParent = textures.getJSONObject(textureName);

					boolean blur = false;
					int filter = 10497;
					String textureColorPath = "source/textures/terrain/error.png";
					String textureNorPath = null;
					String textureParPath = null;
					String textureMerPath = null;
					String textureAoPath = null;

					if(textureParent.has("blur")) blur = textureParent.getBoolean("blur");
					if(textureParent.has("filter")){
						String filterName = textureParent.getString("filter");
						if(filterName == "GL_REPEAT") filter = GL30.GL_REPEAT;
						if(filterName == "GL_MIRRORED_REPEAT") filter = GL30.GL_MIRRORED_REPEAT;
						if(filterName == "GL_CLAMP_TO_EDGE") filter = GL30.GL_CLAMP_TO_EDGE;
					}
					JSONObject textureType = textureParent.getJSONObject("texture");

					JSONObject textureColor = textureType.getJSONObject("color");
					hasColor = true;
					textureColorPath = "resources/" + textureColor.getString("path");
					if(textureColor.has("animation_frames") && textureColor.has("frame_duration")){
						//nothing
					}
					if(textureType.has("nor")){
						JSONObject textureNor = textureType.getJSONObject("nor");
						hasNormal = true;
						textureNorPath = "resources/" + textureNor.getString("path");
						if(textureNor.has("animation_frames") && textureNor.has("frame_duration")){
							//nothing
						}
					} else hasNormal = false;
					if(textureType.has("par")){
						JSONObject texturePar = textureType.getJSONObject("par");
						hasParallax = true;
						textureParPath = "resources/" + texturePar.getString("path");
						if(texturePar.has("animation_frames") && texturePar.has("frame_duration")){
							//nothing
						}
					} else hasParallax = false;
					if(textureType.has("mer")){
						JSONObject textureMer = textureType.getJSONObject("mer");
						hasMER = true;
						textureMerPath = "resources/" + textureMer.getString("path");
						if(textureMer.has("animation_frames") && textureMer.has("frame_duration")){
							//nothing
						}
					} else hasMER = false;
					if(textureType.has("ao")){
						JSONObject textureAO = textureType.getJSONObject("ao");
						hasAO = true;
						textureAoPath = "resources/" + textureAO.getString("path");
						if(textureAO.has("animation_frames") && textureAO.has("frame_duration")){
							//nothing
						}
					} else hasAO = false;

					texture[texturesCounter] = new TextureReader(hasColor, hasNormal, hasParallax, hasMER, hasAO, blur, filter);
					texture[texturesCounter].setTextureColor(textureColorPath);
					texture[texturesCounter].setTextureNor(textureNorPath);
					texture[texturesCounter].setTexturePar(textureParPath);
					texture[texturesCounter].setTextureMer(textureMerPath);
					texture[texturesCounter].setTextureAo(textureAoPath);

					texturesHash.put(textureName, texturesCounter);
					texturesCounter++;
				}

			}

		} catch (final JSONException e) {
			System.out.println("TEXTURE LOADER"+"\nJson parsing error at TextureLoader: " + e.getMessage());
		}

	}

	public void deleteTextures(){
		for(int i=0; i<texturesTotal; i++){
			texture[i].deleteTextures();
		}
	}
	public TextureReader getTextureData(int id){
		return texture[id];
	}
	public int getTextureId(String in){
		if(texturesHash.get(in) != null && texturesHash.containsKey(in)) return texturesHash.get(in);
		System.out.println("TEXTURE LOADER"+"\nError loading texture: " + in);
		return texturesHash.get("error");
	}

	private static String loadFile(String filePath) {
		try {
			byte[] shaderBytes = Files.readAllBytes(Paths.get(filePath));
			return new String(shaderBytes, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
