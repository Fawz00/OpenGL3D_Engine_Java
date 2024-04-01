package opengl3d.utils;

import java.lang.Math;

public class Matriks {

	private static float sin(float x){
		return (float)Math.sin(Math.toRadians(x));
	}
	private static float cos(float x){
		return (float)Math.cos(Math.toRadians(x));
	}

	public static float getArrayAt(float[] a, int b){
		if(b>=a.length) return a[a.length-1];
		if(b<0) return a[0];
		return a[b];
	}

	public static float[] Perkalian(float[] a, float[] b){
		float[] out = {	((a[0]*b[0])+(a[1]*b[4])+(a[2]*b[8])+(a[3]*b[12])), ((a[0]*b[1])+(a[1]*b[5])+(a[2]*b[9])+(a[3]*b[13])), ((a[0]*b[2])+(a[1]*b[6])+(a[2]*b[10])+(a[3]*b[14])), ((a[0]*b[3])+(a[1]*b[7])+(a[2]*b[11])+(a[3]*b[15])),
						((a[4]*b[0])+(a[5]*b[4])+(a[6]*b[8])+(a[7]*b[12])), ((a[4]*b[1])+(a[5]*b[5])+(a[6]*b[9])+(a[7]*b[13])), ((a[4]*b[2])+(a[5]*b[6])+(a[6]*b[10])+(a[7]*b[14])), ((a[4]*b[3])+(a[5]*b[7])+(a[6]*b[11])+(a[7]*b[15])),
						((a[8]*b[0])+(a[9]*b[4])+(a[10]*b[8])+(a[11]*b[12])), ((a[8]*b[1])+(a[9]*b[5])+(a[10]*b[9])+(a[11]*b[13])), ((a[8]*b[2])+(a[9]*b[6])+(a[10]*b[10])+(a[11]*b[14])), ((a[8]*b[3])+(a[9]*b[7])+(a[10]*b[11])+(a[11]*b[15])),
						((a[12]*b[0])+(a[13]*b[4])+(a[14]*b[8])+(a[15]*b[12])), ((a[12]*b[1])+(a[13]*b[5])+(a[14]*b[9])+(a[15]*b[13])), ((a[12]*b[2])+(a[13]*b[6])+(a[14]*b[10])+(a[15]*b[14])), ((a[12]*b[3])+(a[13]*b[7])+(a[14]*b[11])+(a[15]*b[15]))
					};
		return out;
	}

	public static float[] PerkalianV4M4(float[] u, float[] v){
		float[] out = {	(v[12]*u[3]) + (v[0]*u[0]) + (v[8]*u[2]) + (u[1]*v[4]),
						(v[13]*u[3]) + (v[1]*u[0]) + (v[5]*u[1]) + (v[9]*u[2]),
						(v[14]*u[3]) + (v[2]*u[0]) + (v[6]*u[1]) + (v[10]*u[2]),
						(v[15]*u[3]) + (v[3]*u[0]) + (v[7]*u[1]) + (v[11]*u[2])
					};
		return out;
	}
	public static float[] PenjumlahanV4V4(float[] a, float[] b){
		float[] out = {a[0]+b[0], a[1]+b[1], a[2]+b[2], a[3]+b[3]};
		return out;
	}

	public static float[] IdentityM4(){
		float[] out =	{1f, 0f, 0f, 0f,
						 0f, 1f, 0f, 0f,
						 0f, 0f, 1f, 0f,
						 0f, 0f, 0f, 1f
						};
		return out;
	}
	public static float[] IdentityM3(){
		float[] out =	{1f, 0f, 0f,
						 0f, 1f, 0f,
						 0f, 0f, 1f
						};
		return out;
	}
	public static float[] TranslasiKe(float x, float y, float z){
		float[] out =	{1f, 0f, 0f, x,
						 0f, 1f, 0f, y,
						 0f, 0f, 1f, z,
						 0f, 0f, 0f, 1f
						};
		return out;
	}
	public static float[] DilatasiKe(float x, float y, float z){
		float[] out =	{x, 0f, 0f, 0f,
						 0f, y, 0f, 0f,
						 0f, 0f, z, 0f,
						 0f, 0f, 0f, 1f
						};
		return out;
	}
	public static float[] RotasiKe(float x, float y, float z){
		if(x!=0f && y==0f && z==0f){
			float[] Rx ={1f, 0f, 0f, 0f,
						 0f, cos(x), -sin(x), 0f,
						 0f, sin(x), cos(x), 0f,
						 0f, 0f, 0f, 1f
						};
			return Rx;
		}else if(x==0f && y!=0f && z==0f){
			float[] Ry ={cos(y), 0f, sin(y), 0f,
						 0f, 1f, 0f, 0f,
						 -sin(y), 0f, cos(y), 0f,
						 0f, 0f, 0f, 1f
						};
			return Ry;
		}else if(x==0f && y==0f && z!=0f){
			float[] Rz ={cos(z), -sin(z), 0f, 0f,
						 sin(z), cos(z), 0f, 0f,
						 0f, 0f, 1f, 0f,
						 0f, 0f, 0f, 1f
						};
			return Rz;
		}else if(x==0f && y!=0f && z!=0f){
			float[] Ry ={cos(y), 0f, sin(y), 0f,
						 0f, 1f, 0f, 0f,
						 -sin(y), 0f, cos(y), 0f,
						 0f, 0f, 0f, 1f
						};
			float[] Rz ={cos(z), -sin(z), 0f, 0f,
						 sin(z), cos(z), 0f, 0f,
						 0f, 0f, 1f, 0f,
						 0f, 0f, 0f, 1f
						};
			return Perkalian(Ry,Rz);
		}else if(x!=0f && y==0f && z!=0f){
			float[] Rx ={1f, 0f, 0f, 0f,
						 0f, cos(x), -sin(x), 0f,
						 0f, sin(x), cos(x), 0f,
						 0f, 0f, 0f, 1f
						};
			float[] Rz ={cos(z), -sin(z), 0f, 0f,
						 sin(z), cos(z), 0f, 0f,
						 0f, 0f, 1f, 0f,
						 0f, 0f, 0f, 1f
						};
			return Perkalian(Rx,Rz);
		}else if(x!=0f && y!=0f && z==0f){
			float[] Rx ={1f, 0f, 0f, 0f,
						 0f, cos(x), -sin(x), 0f,
						 0f, sin(x), cos(x), 0f,
						 0f, 0f, 0f, 1f
						};
			float[] Ry ={cos(y), 0f, sin(y), 0f,
						 0f, 1f, 0f, 0f,
						 -sin(y), 0f, cos(y), 0f,
						 0f, 0f, 0f, 1f
						};
			return Perkalian(Rx,Ry);
		}
		float[] Rx ={1f, 0f, 0f, 0f,
					 0f, cos(x), -sin(x), 0f,
					 0f, sin(x), cos(x), 0f,
					 0f, 0f, 0f, 1f
					};
		float[] Ry ={cos(y), 0f, sin(y), 0f,
					 0f, 1f, 0f, 0f,
					 -sin(y), 0f, cos(y), 0f,
					 0f, 0f, 0f, 1f
					};
		float[] Rz ={cos(z), -sin(z), 0f, 0f,
					 sin(z), cos(z), 0f, 0f,
					 0f, 0f, 1f, 0f,
					 0f, 0f, 0f, 1f
					};
		float [] out = Perkalian(Rx, Perkalian(Ry,Rz));
		return out;
	}

	public static float[] Transformasi(float Px,float Py,float Pz, float Rx,float Ry,float Rz, float Sx,float Sy,float Sz){
		float[] p = TranslasiKe(Px,Py,Pz);
		float[] r = RotasiKe(Rx,Ry,Rz);
		float[] s = DilatasiKe(Sx,Sy,Sz);
		float [] out = Perkalian(p, Perkalian(s,r));
		return out;
	}

	public static float[] Mat4ToMat3(float[] a){
		float[] b = {
						a[0], a[1], a[2],
						a[4], a[5], a[6],
						a[8], a[9], a[10]
					};
		return b;
	}

	public static float[] Mat3ToMat4(float[] a){
		float[] b = {
						a[0], a[1], a[2], 0f,
						a[3], a[4], a[5], 0f,
						a[6], a[7], a[8], 0f,
						0f, 0f, 0f, 0f
					};
		return b;
	}

	public static float[] HapusTranslasi(float[] a){
		return Mat3ToMat4(Mat4ToMat3(a));
	}

}