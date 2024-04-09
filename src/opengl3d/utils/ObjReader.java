package opengl3d.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Vector;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

public final class ObjReader {

	public final int numFaces;
	public final int stride;
	public final IntBuffer index;
	public final FloatBuffer out;

	public ObjReader(String file) {

		Vector<Float> vertices = new Vector<>();
		Vector<Float> normals = new Vector<>();
		Vector<Float> textures = new Vector<>();
		Vector<String> faces = new Vector<>();

		BufferedReader reader = null;
		try {
			System.out.println("Loading model: "+file);
			InputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(Paths.get(file)));
			reader = new BufferedReader(new InputStreamReader(inputStream));

			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(" ");
				switch (parts[0]) {
					case "v":
						// vertices
						vertices.add(stringToFloat(parts[1], 0));
						vertices.add(stringToFloat(parts[2], 0));
						vertices.add(stringToFloat(parts[3], 0));
						break;
					case "vt":
						// textures
						textures.add(stringToFloat(parts[1], 1));
						textures.add(stringToFloat(parts[2], 1));
						break;
					case "vn":
						// normals
						normals.add(stringToFloat(parts[1], 2));
						normals.add(stringToFloat(parts[2], 2));
						normals.add(stringToFloat(parts[3], 2));
						break;
					case "f":
						// faces: vertex/texture/normal
						if(parts.length == 4){
							faces.add(parts[1]);
							faces.add(parts[2]);
							faces.add(parts[3]);
						} else if(parts.length == 5){
							faces.add(parts[1]);
							faces.add(parts[2]);
							faces.add(parts[3]);
							
							faces.add(parts[1]);
							faces.add(parts[3]);
							faces.add(parts[4]);
						}else if(parts.length > 5){
							faces.add(parts[1]);
							faces.add(parts[2]);
							faces.add(parts[3]);
							for(int i=1; i<=parts.length-4; i++){
								faces.add(parts[i+1]);
								faces.add(parts[i+2]);
								faces.add(parts[i+3]);
							}
						}
						break;
				}
			}
		} catch (IOException e) {
			System.out.println(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					System.out.println(e);
				}
			}
		}

		numFaces = faces.size();

		stride = 3+2+3+3+3;
		index = MemoryUtil.memAllocInt(numFaces*stride);
		out = MemoryUtil.memAllocFloat(numFaces*stride);

		for (String face : faces) {
			String[] parts = face.split("/");

			int posIndex = Integer.valueOf(parts[0]);
			int texIndex = Integer.valueOf(parts[1]);
			int norIndex = Integer.valueOf(parts[2]);
			index.put(posIndex).put(texIndex).put(norIndex);
		}

		int offA = 0;
		int offB = 0;
		int offC = 0;
		for (int i = 0; i < numFaces*3; i+=3) {
			if((i/3)%3==0) {
				offA = 0;
				offB = 3;
				offC = 6;
			} else if((i/3)%3==1) {
				offA = -3;
				offB = 0;
				offC = 3;
			} else if((i/3)%3==2) {
				offA = -6;
				offB = -3;
				offC = 0;
			}

			int positionIndex = index.get(i);
			int textureIndex = index.get(i+1);
			int normalIndex = index.get(i+2);

			//VERTICES X,Y,Z
			out.put( vertices.get((3*(positionIndex-1))) ).put( vertices.get(1+(3*(positionIndex-1))) ).put( vertices.get(2+(3*(positionIndex-1))) );
			//TEXTURE X,-Y
			out.put( textures.get((2*(textureIndex-1))) ).put( 1f-textures.get(1+(2*(textureIndex-1))) );
			//NORMAL X,Y,Z
			out.put( normals.get((3*(normalIndex-1))) ).put( normals.get(1+(3*(normalIndex-1))) ).put( normals.get(2+(3*(normalIndex-1))) );

			//TANGENT BITANGENT
			int positionIndexA = index.get(i+offA);
			Vector3f vertexA = new Vector3f(
					vertices.get( (3*(positionIndexA-1)) ),
					vertices.get( 1 + (3*(positionIndexA-1)) ),
					vertices.get( 2 + (3*(positionIndexA-1)) )
			);
			int positionIndexB = index.get(i+offB);
			Vector3f vertexB = new Vector3f(
					vertices.get( (3*(positionIndexB-1)) ),
					vertices.get( 1 + (3*(positionIndexB-1)) ),
					vertices.get( 2 + (3*(positionIndexB-1)) )
			);
			int positionIndexC = index.get(i+offC);
			Vector3f vertexC = new Vector3f(
					vertices.get( (3*(positionIndexC-1)) ),
					vertices.get( 1 + (3*(positionIndexC-1)) ),
					vertices.get( 2 + (3*(positionIndexC-1)) )
			);

			int textureIndexA = index.get(i+offA);
			Vector2f uvA = new Vector2f(
				textures.get( (2*(textureIndexA-1)) ) * 1000f,
				(1f-textures.get( 1 + (2*(textureIndexA-1)) )) * 1000f
			);
			int textureIndexB = index.get(i+offB);
			Vector2f uvB = new Vector2f(
					textures.get( (2*(textureIndexB-1)) ) * 1000f,
					(1f-textures.get( 1 + (2*(textureIndexB-1)) )) * 1000f
			);
			int textureIndexC = index.get(i+offC);
			Vector2f uvC = new Vector2f(
					textures.get( (2*(textureIndexC-1)) ) * 1000f,
					(1f-textures.get( 1 + (2*(textureIndexC-1)) )) * 1000f
			);

			Vector3f edgeA = new Vector3f();
			Vector3f edgeB = new Vector3f();
			Vector2f deltaUvA = new Vector2f();
			Vector2f deltaUvB = new Vector2f();

			vertexB.sub(vertexA, edgeA);
			vertexC.sub(vertexA, edgeB);
			uvB.sub(uvA, deltaUvA);
			uvC.sub(uvA, deltaUvB);

			float f = 1.0f / ((deltaUvA.x * deltaUvB.y) - (deltaUvB.x * deltaUvA.y));

			if(Float.isInfinite(f) || Float.isNaN(f)) {
				//Tangent
				out.put(-1f).put(0f).put(0f);
	
				//Bitangent
				out.put(0f).put(0f).put(0f);
			} else {
				//Tangent
				out.put( (deltaUvB.y * edgeA.x - deltaUvA.y * edgeB.x) * f )
					.put( (deltaUvB.y * edgeA.y - deltaUvA.y * edgeB.y) * f )
					.put( (deltaUvB.y * edgeA.z - deltaUvA.y * edgeB.z) * f );
	
				//Bitangent
				out.put( (deltaUvA.x * edgeB.x - deltaUvB.x * edgeA.x) * f )
				.put( (deltaUvA.x * edgeB.y - deltaUvB.x * edgeA.y) * f )
				.put( (deltaUvA.x * edgeB.z - deltaUvB.x * edgeA.z) * f );
			}
		}
		out.flip();
		MemoryUtil.memFree(index);

		// Recalculate
/*		for(int i=0; i<output.length; i+=14) {
			Vector3f A = new Vector3f(output[i], output[i+1], output[i+2]);
			for(int j=0; j<output.length; j+=14) {

				Vector3f B = new Vector3f(output[j], output[j+1], output[j+2]);
				if(A.distance(B)==0 && i!=j) {
					//Tangent
					Vector3f tA = new Vector3f(output[i+8], output[i+9], output[i+10]);
					Vector3f tB = new Vector3f(output[j+8], output[j+9], output[j+10]);
					Vector3f tC = tA.add(tB).div(2f);
					output[j+8] = tC.x;
					output[j+9] = tC.y;
					output[j+10] = tC.z;

					//Bitangent
					Vector3f bA = new Vector3f(output[i+11], output[i+12], output[i+13]);
					Vector3f bB = new Vector3f(output[j+11], output[j+12], output[j+13]);
					Vector3f bC = bA.add(bB).div(2f);
					output[j+11] = bC.x;
					output[j+12] = bC.y;
					output[j+13] = bC.z;
				}
			}
		}
*/
	}

	public void deleteBuffer() {
		MemoryUtil.memFree(out);
	}

	private float stringToFloat(String s, int i){
		if(s.contains(",")) s = s.replace(",", ".");
		float a = Float.valueOf(s);
		if(a>1f && i==1) a=a-(float)Math.floor(a);
		if(a>1f && i==2) {
			a = (a+1f)/2f;
			a=a-(float)Math.floor(a);
			a = (a*2f)-1f;
		}
		return a;
	}
}
