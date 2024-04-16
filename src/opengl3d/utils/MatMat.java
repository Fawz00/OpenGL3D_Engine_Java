package opengl3d.utils;

public class MatMat {

    public static float lerp(float a, float b, float t){
		return t*(b-a)+a;
	}

	public static float mix(float a, float b, float t){
		return a*(1f-t) + b*t;
	}

	public static float[] sub(float[] a, float[] b){
		if(a.length != b.length) return null;
		float[] c = new float[a.length];
		for(int i=0; i<a.length; i++)
			c[i] = a[i]-b[i];
		return c;
	}

	public static float[] sum(float[] a, float[] b){
		if(a.length != b.length) return null;
		float[] c = new float[a.length];
		for(int i=0; i<a.length; i++)
			c[i] = a[i]+b[i];
		return c;
	}

	public static float[] mul(float[] a, float[] b){
		if(a.length != b.length) return null;
		float[] c = new float[a.length];
		for(int i=0; i<a.length; i++)
			c[i] = a[i]*b[i];
		return c;
	}

	public static float[] div(float[] a, float[] b){
		if(a.length != b.length) return null;
		float[] c = new float[a.length];
		for(int i=0; i<a.length; i++)
			c[i] = a[i]/b[i];
		return c;
	}

	public static float distance(float[] a){
		float b = 0f;
		for(float c : a)
			b += c*c;
		return (float) Math.sqrt(b);
	}

	public static float distance(float[] a, float[] b){
		if(a.length != b.length) return 0f;
		float[] c = new float[a.length];
		for(int i=0; i<a.length; i++)
			c[i] = Math.max(a[i],b[i])-Math.min(a[i],b[i]);
		float d = 0f;
		for(float e : c)
			d += e*e;
		return (float) Math.sqrt(d);
	}

	public static float distance(float a, float b){
		return (float) Math.max(a,b)-Math.min(a,b);
	}

	public static float[] normalize(float[] a){
		float b = distance(a);
		for(int i=0; i<a.length; i++)
			a[i] /= b;
		return a;
	}

    public static float clamp(float x, float a, float b) {
		if(x>b) {
			return b;
		} else if(x<a) {
			return a;
		}
		return x;
	}

    public static float repeat(float x, float a, float b) {
		if(x>b) {
			return a;
		} else if(x<a) {
			return b;
		}
		return x;
	}

}
