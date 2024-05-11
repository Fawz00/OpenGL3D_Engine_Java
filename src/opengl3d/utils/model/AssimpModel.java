package opengl3d.utils.model;

// import java.nio.FloatBuffer;
// import java.nio.IntBuffer;
// import java.util.ArrayList;
// import java.util.List;
import java.util.Vector;

// import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIFace;
// import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AIScene;
// import org.lwjgl.assimp.AIString;
// import org.lwjgl.assimp.AITexture;
// import org.lwjgl.assimp.AIPropertyStore;
import org.lwjgl.assimp.Assimp;

import opengl3d.utils.Point2;
import opengl3d.utils.Point3;

public class AssimpModel {
    public boolean gammaCorrection;
    public String directory;
    public Vector<Mesh> meshes = new Vector<>();
    public Vector<Texture> textures_loaded;

    public AssimpModel(String path) {
        AIScene scene = Assimp.aiImportFile(path,
            Assimp.aiProcess_JoinIdenticalVertices
            | Assimp.aiProcess_Triangulate
            | Assimp.aiProcess_FixInfacingNormals
            | Assimp.aiProcess_PreTransformVertices
        );

        if(scene == null || (scene.mFlags() & Assimp.AI_SCENE_FLAGS_INCOMPLETE) != 0 || scene.mRootNode() == null)
            throw new RuntimeException("Tidak bisa memuat model: " + path);

        directory = path.substring(0, path.lastIndexOf('/'));
        processNode(scene.mRootNode(), scene);
    }

    private void processNode(AINode node, AIScene scene) {
        // Process all the node's meshes (if any)
        int numMeshes = node.mNumMeshes();
        for (int i = 0; i < numMeshes; i++) {
            AIMesh mesh = AIMesh.createSafe(scene.mMeshes().get(node.mMeshes().get(i)));
            meshes.add(processMesh(mesh, scene));
        }

        // Then do the same for each of its children
        int numChildren = node.mNumChildren();
        for (int i = 0; i < numChildren; i++) {
            AINode childNode = AINode.createSafe(node.mChildren().get(i));
            processNode(childNode, scene);
        }
    }

    Mesh processMesh(AIMesh mesh, AIScene scene) {
        // data to fill
        Vector<Vertex> vertices = new Vector<>();
        Vector<Integer> indices = new Vector<>();
        Vector<Texture> textures = new Vector<>();

        // walk through each of the mesh's vertices
        for(int i = 0; i < mesh.mNumVertices(); i++)
        {
            Vertex vertex = new Vertex();
            Point3 vector = new Point3(0f, 0f, 0f); // we declare a placeholder vector since assimp uses its own vector class that doesn't directly convert to glm's vec3 class so we transfer the data to this placeholder glm::vec3 first.
            // positions
            vector.x = mesh.mVertices().get(i).x();
            vector.y = mesh.mVertices().get(i).y();
            vector.z = mesh.mVertices().get(i).z();
            vertex.position = vector;
            // normals
            if (!mesh.mNormals().get(0).isNull(0))
            {
                vector.x = mesh.mNormals().get(i).x();
                vector.y = mesh.mNormals().get(i).y();
                vector.z = mesh.mNormals().get(i).z();
                vertex.normal = vector;
            }
            // texture coordinates
            if(!mesh.mTextureCoords(0).get(0).isNull(0)) // does the mesh contain texture coordinates?
            {
                Point2 vec = new Point2(0f, 0f);
                // a vertex can contain up to 8 different texture coordinates. We thus make the assumption that we won't 
                // use models where a vertex can have multiple texture coordinates so we always take the first set (0).
                vec.x = mesh.mTextureCoords(0).get(i).x(); 
                vec.y = mesh.mTextureCoords(0).get(i).y();
                vertex.texcoord = vec;
                // tangent
                vector.x = mesh.mTangents().get(i).x();
                vector.y = mesh.mTangents().get(i).y();
                vector.z = mesh.mTangents().get(i).z();
                vertex.tangent = vector;
                // bitangent
                vector.x = mesh.mBitangents().get(i).x();
                vector.y = mesh.mBitangents().get(i).y();
                vector.z = mesh.mBitangents().get(i).z();
                vertex.bitangent = vector;
            }
            else
                vertex.texcoord = new Point2(0.0f, 0.0f);

            vertices.addLast(vertex);
        }
        // now wak through each of the mesh's faces (a face is a mesh its triangle) and retrieve the corresponding vertex indices.
        for(int i = 0; i < mesh.mNumFaces(); i++)
        {
            AIFace face = mesh.mFaces().get(i);
            // retrieve all indices of the face and store them in the indices vector
            for(int j = 0; j < face.mNumIndices(); j++)
                indices.addLast(face.mIndices().get(j));        
        }
        // process materials
        long material = scene.mMaterials().get(mesh.mMaterialIndex());    
        // we assume a convention for sampler names in the shaders. Each diffuse texture should be named
        // as 'texture_diffuseN' where N is a sequential number ranging from 1 to MAX_SAMPLER_NUMBER. 
        // Same applies to other texture as the following list summarizes:
        // diffuse: texture_diffuseN
        // specular: texture_specularN
        // normal: texture_normalN

        // 1. diffuse maps
        Vector<Texture> diffuseMaps = loadMaterialTextures(material, Assimp.aiTextureType_DIFFUSE, "texture_diffuse");
        textures.addAll(diffuseMaps);
        // 2. specular maps
        Vector<Texture> specularMaps = loadMaterialTextures(material, Assimp.aiTextureType_SPECULAR, "texture_specular");
        textures.addAll(specularMaps);
        // 3. normal maps
        Vector<Texture> normalMaps = loadMaterialTextures(material, Assimp.aiTextureType_HEIGHT, "texture_normal");
        textures.addAll(normalMaps);
        // 4. height maps
        Vector<Texture> heightMaps = loadMaterialTextures(material, Assimp.aiTextureType_AMBIENT, "texture_height");
        textures.addAll(heightMaps);
        
        // return a mesh object created from the extracted mesh data
        return new Mesh(vertices, indices, textures);
    }

    private Vector<Texture> loadMaterialTextures(long mat, int type, String typeName)
    {
        Vector<Texture> textures = new Vector<>();
        for(int i = 0; i < Assimp.naiGetMaterialTextureCount(mat, type); i++)
        {
            // long str;
            // Assimp.naiGetMaterialTexture(mat, type, i, str, 0, 0, 0, 0, 0, 0);
            // // check if texture was loaded before and if so, continue to next iteration: skip loading a new texture
            // boolean skip = false;
            // for(int j = 0; j < textures_loaded.size(); j++)
            // {
            //     if(textures_loaded.get(j).path.compareTo(Assimp.aiGetPath) str.C_Str()) == 0)
            //     {
            //         textures.push_back(textures_loaded[j]);
            //         skip = true; // a texture with the same filepath has already been loaded, continue to next one. (optimization)
            //         break;
            //     }
            // }
            // if(!skip)
            // {   // if texture hasn't been loaded already, load it
            //     Texture texture;
            //     texture.id = TextureFromFile(str.C_Str(), this->directory);
            //     texture.type = typeName;
            //     texture.path = str.C_Str();
            //     textures.push_back(texture);
            //     textures_loaded.push_back(texture);  // store it as texture loaded for entire model, to ensure we won't unnecessary load duplicate textures.
            // }
        }
        return textures;
    }
}
