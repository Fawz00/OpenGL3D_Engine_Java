package opengl3d.utils;

import opengl3d.Settings;

public class FpsTool {
	private long interval;
	private long startTime;
	private long endTime;
	private long timePerFrame;
	private long sleepDuration;
	private double lastFps;

	public FpsTool(int fps) {
		lastFps = 0;
		setFps(fps);
		sleepDuration = 0;
		startTime = System.nanoTime();
	}

	public void setFps(int fps) {
		interval = 1000000000 / fps;
	}

	public double getFps() {
		if(timePerFrame != 0) lastFps = 1.0/((timePerFrame+sleepDuration)/1000000000.0);
		return lastFps;
	}

	public void end() {
		endTime = System.nanoTime();
		timePerFrame = (endTime - startTime) - sleepDuration;
		startTime = System.nanoTime();

		if(timePerFrame < interval && Settings.limitFps) {
			try {
				sleepDuration = interval-timePerFrame;
				Thread.sleep( sleepDuration/1000000 );
			} catch (InterruptedException e) {
				System.out.println("Error: " + e.getMessage());
			}
		} else{
			sleepDuration = 0;
		}
	}
}