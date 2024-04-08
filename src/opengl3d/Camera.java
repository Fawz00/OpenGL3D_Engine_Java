package opengl3d;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import opengl3d.engine.Input;
import opengl3d.utils.MatMat;

public class Camera {
	private boolean isPaused = false;
	private int currentMode = 0;
	private float offset[] = new float[] {
			0.0125f,
			Settings.cameraDistance,
			10f*Settings.cameraDistance,
			-Settings.cameraDistance
	};
	private float[] pivotPosition = new float[3];
	private float[] rotation = new float[3];
	private float previousCursorPosition[] = new float[2];
	private float fovY;
	private float toRad = 0.0174532925199f;



	public Camera(int mode, float fov, float[] position, float[] rotation) {
		this.currentMode = mode;
		this.pivotPosition = position;
		this.rotation = rotation;
		this.fovY = fov;
	}

	public void pause() {
		isPaused = true;
	}

	public void resume() {
		isPaused = false;
		previousCursorPosition[0]=(float)Input.getMouseX();
		previousCursorPosition[1]=(float)Input.getMouseY();
	}

	public void update() {
		getCameraRotation();
		rotation[0] = MatMat.repeat(rotation[0], -180f*toRad, 180f*toRad);
		rotation[1] = MatMat.clamp(rotation[1], -90f*toRad, 90f*toRad);
	}

	public void setPivotPosition(float[] pos) {
		this.pivotPosition = pos;
	}
	public void setFovY(float a) {
		this.fovY = a;
	}
	public float getFovY() {
		return fovY;
	}
	public float getFovX(float ratio) {
		return (float) (2*Math.atan(Math.tan((fovY)/2)*ratio));
	}
	public float[] getPosition() {
		Vector4f a = new Vector4f(0f, 0f, 0f, 1f);
		Matrix4f tr = new Matrix4f()
				.translate(pivotPosition[1], pivotPosition[0], pivotPosition[2])
				.rotate(getRotation()[0], 1f, 0f, 0f)
				.rotate(getRotation()[1], 0f, 1f, 0f)
				.rotate(getRotation()[2], 0f, 0f, 1f)
				.translate(0f, 0f, getDistance());
		a.mul(tr);

		return new float[] {a.y, a.x, a.z, a.w};
	}

	public float[] getRotation() {
		return rotation;
	}

	public float getDistance() {
		return offset[currentMode];
	}

	public void toggleCameraMode() {
		if(!isPaused) {
			currentMode += 1;
			if(currentMode > offset.length-1) {
				currentMode=0;
			}
		}
	}
	private void getCameraRotation() {
		float x = (float) Input.getMouseX();
		float y = (float) Input.getMouseY();

		float dx = x - previousCursorPosition[0];
		float dy = y - previousCursorPosition[1];
		
		rotation[0] += dx * Settings.cameraSensitivity;
		rotation[1] += dy * Settings.cameraSensitivity;

		previousCursorPosition[0] = x; previousCursorPosition[1] = y;

	}
}
