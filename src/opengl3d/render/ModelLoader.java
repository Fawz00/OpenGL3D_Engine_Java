package opengl3d.render;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

import opengl3d.utils.ModelReader;

public class ModelLoader {
	private ModelReader[] model;
	private int modelsTotal;
	private String jsonModel;
	private HashMap<String, Integer> modelsHash = new HashMap<String, Integer>();

	//===================//
	//     M O D E L     //
	//===================//

	public ModelLoader(){
		jsonModel = loadFile("resources/maps/models_loader.json");

		if(jsonModel != null) try{

			JSONObject jsonFile = new JSONObject(jsonModel);
			int version = jsonFile.getInt("version");
			if(version <= 1){

				JSONObject models = jsonFile.getJSONObject("models");
				modelsTotal = models.length();
				model = new ModelReader[modelsTotal];
				@SuppressWarnings("unchecked")
				Iterator<String> modelsList = models.keys();
				int modelsCounter = 0;
				while(modelsList.hasNext()) {
					String modelName = modelsList.next();
					model[modelsCounter] = new ModelReader("resources/" + models.getString(modelName));

					modelsHash.put(modelName, modelsCounter);
					modelsCounter++;
				}

			}

		} catch (final JSONException e) {
			System.out.println("TAG"+"\nJson parsing error at ModelLoader: " + e.getMessage());
		}

	}
	public void deleteModels(){
		for(int i=0; i<modelsTotal; i++){
			model[i].deleteModel();
		}
	}
	public ModelReader getModelData(int id){
		return model[id];
	}
	public int getModelId(String in){
		if(modelsHash.get(in) != null) return modelsHash.get(in);
		System.out.println("MODEL LOADER"+"\nError loading model: " + in);
		return 0;
	}

	private static String loadFile(String filePath) {
		try {
			byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
			return new String(fileBytes, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
