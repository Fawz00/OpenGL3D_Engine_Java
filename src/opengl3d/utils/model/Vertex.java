package opengl3d.utils.model;

import opengl3d.utils.Point2;
import opengl3d.utils.Point3;

public class Vertex {
    public Point3 position;
    public Point3 normal;
    public Point2 texcoord;
    public Point3 tangent;
    public Point3 bitangent;

    //bone indexes which will influence this vertex
    public int[] m_BoneIDs = new int[Mesh.MAX_BONE_INFLUENCE];
    //weights from each bone
    public float[] m_Weights = new float[Mesh.MAX_BONE_INFLUENCE];
}