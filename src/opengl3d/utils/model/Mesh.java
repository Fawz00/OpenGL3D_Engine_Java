package opengl3d.utils.model;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Vector;

import org.lwjgl.opengl.GL30;

import opengl3d.utils.Point2;
import opengl3d.utils.Point3;
import opengl3d.utils.Shader;

public class Mesh {
	public static int MAX_BONE_INFLUENCE = 4;

    private Vector<Vertex> vertices;
    private Vector<Integer> indices;
    private Vector<Texture> textures;
    private int VAO;

	private int VBO, EBO;

    public Mesh(Vector<Vertex> vertices, Vector<Integer> indices, Vector<Texture> textures) {
        this.vertices = vertices;
        this.indices = indices;
        this.textures = textures;

        // now that we have all the required data, set the vertex buffers and its attribute pointers.
        setupMesh();
	}

    public void draw(Shader shader) 
    {
        // bind appropriate textures
        int diffuseNr  = 1;
        int specularNr = 1;
        int normalNr   = 1;
        int heightNr   = 1;
        for(int i = 0; i < textures.size(); i++)
        {
            GL30.glActiveTexture(GL30.GL_TEXTURE0 + i); // active proper texture unit before binding
            // retrieve texture number (the N in diffuse_textureN)
            String number;
            String name = textures.get(i).type;
            if(name == "texture_diffuse")
                number = Integer.toString(diffuseNr++);
            else if(name == "texture_specular")
                number = Integer.toString(specularNr++); // transfer unsigned int to string
            else if(name == "texture_normal")
                number = Integer.toString(normalNr++); // transfer unsigned int to string
             else if(name == "texture_height")
                number = Integer.toString(heightNr++); // transfer unsigned int to string

            // now set the sampler to the correct texture unit
//            GL30.glUniform1i(glGetUniformLocation(shader.ID, (name + number).c_str()), i);
            // and finally bind the texture
            GL30.glBindTexture(GL30.GL_TEXTURE_2D, textures.get(i).id);
        }
        
        // draw mesh
        GL30.glBindVertexArray(VAO);
//        GL30.glDrawElements(GL30.GL_TRIANGLES, static_cast<unsigned int>(indices.size()), GL30.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);

        // always good practice to set everything back to defaults once configured.
        GL30.glActiveTexture(GL30.GL_TEXTURE0);
    }

	private void setupMesh()
    {
        // create buffers/arrays
        VAO = GL30.glGenVertexArrays();
        VBO = GL30.glGenBuffers();
        EBO = GL30.glGenBuffers();

        GL30.glBindVertexArray(VAO);
        // load data into vertex buffers
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, VBO);
        // A great thing about structs is that their memory layout is sequential for all its items.
        // The effect is that we can simply pass a pointer to the struct and it translates perfectly to a glm::vec3/2 array which
        // again translates to 3/2 floats which translates to a byte array.
/*        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertices.size() * sizeof(Vertex), &vertices[0], GL30.GL_STATIC_DRAW);  

        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, EBO);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indices.size() * sizeof(unsigned int), &indices[0], GL30.GL_STATIC_DRAW);

        // set the vertex attribute pointers
        // vertex Positions
        GL30.glEnableVertexAttribArray(0);	
        GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, GL_FALSE, sizeof(Vertex), (void*)0);
        // vertex normals
        GL30.glEnableVertexAttribArray(1);	
        GL30.glVertexAttribPointer(1, 3, GL30.GL_FLOAT, GL_FALSE, sizeof(Vertex), (void*)offsetof(Vertex, Normal));
        // vertex texture coords
        GL30.glEnableVertexAttribArray(2);	
        GL30.glVertexAttribPointer(2, 2, GL30.GL_FLOAT, GL_FALSE, sizeof(Vertex), (void*)offsetof(Vertex, TexCoords));
        // vertex tangent
        GL30.glEnableVertexAttribArray(3);
        GL30.glVertexAttribPointer(3, 3, GL30.GL_FLOAT, GL_FALSE, sizeof(Vertex), (void*)offsetof(Vertex, Tangent));
        // vertex bitangent
        GL30.glEnableVertexAttribArray(4);
        GL30.glVertexAttribPointer(4, 3, GL30.GL_FLOAT, GL_FALSE, sizeof(Vertex), (void*)offsetof(Vertex, Bitangent));
		// ids
		GL30.glEnableVertexAttribArray(5);
		GL30.glVertexAttribIPointer(5, 4, GL30.GL_INT, sizeof(Vertex), (void*)offsetof(Vertex, m_BoneIDs));

		// weights
		GL30.glEnableVertexAttribArray(6);
		GL30.glVertexAttribPointer(6, 4, GL30.GL_FLOAT, GL_FALSE, sizeof(Vertex), (void*)offsetof(Vertex, m_Weights));
        GL30.glBindVertexArray(0);
		*/
    }
}
