package opengl3d.utils;

public class Point3 {
    public float x;
    public float y;
    public float z;

    public Point3(double x, double y, double z) {
        this.x = (float)x;
        this.y = (float)y;
        this.z = (float)z;
    }
    public Point3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public Point3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public void set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float[] toFloatArray() {
        return new float[]{x,y,z};
    }
}
