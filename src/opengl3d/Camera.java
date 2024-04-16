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
	private Float[] pivotPosition;
	private float[] rotation = new float[3];
	private float previousCursorPosition[] = new float[2];
	private float rotVel[] = new float[2];
	private float posVel[] = new float[3];
	private boolean isSmooth = false;
	private float fovY;
	private float toRad = 0.0174532925199f;



	public Camera(int mode, float fov, float[] rotation) {
		this.currentMode = mode;
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
		if(pivotPosition == null) pivotPosition = new Float[]{pos[0], pos[1], pos[2]};
		if(pivotPosition[0] == null || Float.isNaN(pivotPosition[0])) pivotPosition[0] = pos[0];
		if(pivotPosition[1] == null || Float.isNaN(pivotPosition[1])) pivotPosition[1] = pos[1];
		if(pivotPosition[2] == null || Float.isNaN(pivotPosition[2])) pivotPosition[2] = pos[2];
		if(isSmooth && currentMode != 0) {
			float dx = pos[0] - pivotPosition[0];
			float dy = pos[1] - pivotPosition[1];
			float dz = pos[2] - pivotPosition[2];

			posVel[0] += dx*time*time*4f;
			posVel[1] += dy*time*time*4f;
			posVel[2] += dz*time*time*4f;

			//System.out.println(MatMat.distance(pivotPosition[2], pos[2]));

			pivotPosition[0] += posVel[0];
			pivotPosition[1] += posVel[1];
			pivotPosition[2] += posVel[2];

			posVel[0] *= 0.98*MatMat.clamp((float) Math.pow(MatMat.distance(pos, new float[]{pivotPosition[0], pivotPosition[1], pivotPosition[2]}), 1f)/5f, 0.99f, 1.012f);
			posVel[1] *= 0.98*MatMat.clamp((float) Math.pow(MatMat.distance(pos, new float[]{pivotPosition[0], pivotPosition[1], pivotPosition[2]}), 1f)/5f, 0.99f, 1.012f);
			posVel[2] *= 0.98*MatMat.clamp((float) Math.pow(MatMat.distance(pos, new float[]{pivotPosition[0], pivotPosition[1], pivotPosition[2]}), 1f)/5f, 0.99f, 1.012f);
		} else {
			pivotPosition = new Float[]{pos[0], pos[1], pos[2]};
		}

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

			rotVel[0] /= 1.02f*(1f-0.2f*time);
			rotVel[1] /= 1.02f*(1f-0.2f*time);
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
		if(pivotPosition == null) return new float[]{0f, 0f, 0f};
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
		if(pivotPosition == null) return new float[]{0f, 0f, 0f};
		return new float[]{pivotPosition[0], pivotPosition[1], pivotPosition[2]};
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
