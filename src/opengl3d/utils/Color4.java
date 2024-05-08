package opengl3d.utils;

public class Color4 {
    public int r;
    public int g;
    public int b;
    public int a;

    public Color4(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 255;
    }
    public Color4(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
    public Color4(float r, float g, float b) {
        this.r = (int)(r*255f);
        this.g = (int)(g*255f);
        this.b = (int)(b*255f);
        this.a = 255;
    }
    public Color4(float r, float g, float b, float a) {
        this.r = (int)(r*255f);
        this.g = (int)(g*255f);
        this.b = (int)(b*255f);
        this.a = (int)(a*255f);
    }
    public Color4(int color) {
        this.r = ((color >> 24) & 0xFF);
        this.g = ((color >> 16) & 0xFF);
        this.b = ((color >> 8) & 0xFF);
        this.a = ((color >> 0) & 0xFF);
    }

    public void setColor(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 255;
    }
    public void setColor(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
    public void setColor(float r, float g, float b) {
        this.r = (int)(r*255f);
        this.g = (int)(g*255f);
        this.b = (int)(b*255f);
        this.a = 255;
    }
    public void setColor(float r, float g, float b, float a) {
        this.r = (int)(r*255f);
        this.g = (int)(g*255f);
        this.b = (int)(b*255f);
        this.a = (int)(a*255f);
    }
    public void setColor(int color) {
        this.r = ((color >> 24) & 0xFF);
        this.g = ((color >> 16) & 0xFF);
        this.b = ((color >> 8) & 0xFF);
        this.a = ((color >> 0) & 0xFF);
    }

    public float[] getColor() {
        return new float[]{this.r/255f, this.g/255f, this.b/255f, this.a/255f};
    }
}
