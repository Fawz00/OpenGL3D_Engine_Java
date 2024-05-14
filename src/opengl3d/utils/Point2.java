package opengl3d.utils;

public class Point2 {
    public float x;
    public float y;

    public Point2(double x, double y) {
        this.x = (float)x;
        this.y = (float)y;
    }
    public Point2(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public Point2(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public float[] toFloatArray() {
        return new float[]{x,y};
    }
}
