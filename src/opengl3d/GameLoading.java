package opengl3d;

public class GameLoading {
	int maxScore = 0;
	int currentScore = 0;
	float percent = 0f;

	public GameLoading() {
		percent = 0;
	}

	
	public void addPoint() {
		currentScore++;
	}
	public void resetPoint() {
		currentScore = 0;
	}
	public void setMaxPoint(int max) {
		maxScore = max;
	}

}
