package opengl3d.utils;

public class MatMat {

    public static float lerp(float a, float b, float t){
		return t*(b-a)+a;
	}

    public static float clamp(float x, float y, float z) {
		if(x>z) {
			return z-0.0001f;
		} else if(x<y) {
			return y+0.0001f;
		}
		return x;
	}

    public static float repeat(float x, float y, float z) {
		if(x>z) {
			return y;
		} else if(x<y) {
			return z;
		}
		return x;
	}

}
