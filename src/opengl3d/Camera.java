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
	private float previousPivotPosition[] = new float[3];
	private float rotVel[] = new float[2];
	private float posVel[] = new float[3];
	private boolean isSmooth = false;
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

	public void update(float[] pos, float time) {
		isSmooth = Settings.smoothCamera;
		setCameraRotation(time);
		setPivotPosition(pos, time);
		rotation[0] = MatMat.repeat(rotation[0], -180f*toRad, 180f*toRad);
		rotation[1] = MatMat.clamp(rotation[1], -90f*toRad, 90f*toRad);
	}

	private void setPivotPosition(float[] pos, float time) {
		pivotPosition = pos;

		if(isSmooth) {
			float dx = pivotPosition[0] - previousPivotPosition[0];
			float dy = pivotPosition[1] - previousPivotPosition[1];
			float dz = pivotPosition[2] - previousPivotPosition[2];

			posVel[0] += 0.0001f*dx*time*time;
			posVel[1] += 0.0001f*dy*time*time;
			posVel[2] += 0.0001f*dz*time*time;

			float smoothness = 1000.0f;

			pivotPosition[0] += Math.min(posVel[0], smoothness);
			pivotPosition[1] += Math.min(posVel[1], smoothness);
			pivotPosition[2] += Math.min(posVel[2], smoothness);

			posVel[0]/=1.0025f;
			posVel[1]/=1.0025f;
			posVel[2]/=1.0025f;
		}

		previousPivotPosition = pivotPosition;
	}
	private void setCameraRotation(float time) {
		float x = (float) Input.getMouseX();
		float y = (float) Input.getMouseY();

		float dx = x - previousCursorPosition[0];
		float dy = y - previousCursorPosition[1];
		
		if(isSmooth) {
			rotVel[0] += Settings.cameraSensitivity*dx*time*time;
			rotVel[1] += Settings.cameraSensitivity*dy*time*time;

			float smoothness = 2f;

			rotation[0] += Math.min(rotVel[0], smoothness);
			rotation[1] += Math.min(rotVel[1], smoothness);

			rotVel[0]/=1.075f;
			rotVel[1]/=1.075f;
		} else {
			float smoothness = 0.25f;
			float lerpFactor = Math.min(1.0f, smoothness * time);

			float targetRotationX = rotation[0] + dx * Settings.cameraSensitivity;
			float targetRotationY = rotation[1] + dy * Settings.cameraSensitivity;

			rotation[0] = MatMat.lerp(rotation[0], targetRotationX, lerpFactor);
			rotation[1] = MatMat.lerp(rotation[1], targetRotationY, lerpFactor);
		}

		previousCursorPosition[0] = x; previousCursorPosition[1] = y;
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
				.rotate(rotation[0], 1f, 0f, 0f)
				.rotate(rotation[1], 0f, 1f, 0f)
				.rotate(rotation[2], 0f, 0f, 1f)
				.translate(0f, 0f, getDistance());
		a.mul(tr);

		return new float[] {a.y, a.x, a.z, a.w};
	}
	public float[] getPivotPosition() {
		return pivotPosition;
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
}
