package opengl3d;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.BiConsumer;

import opengl3d.utils.LoadingProgress;

public class GameLoading {
	private static int lastId = 0;
	private static LoadingProgress allProgress = new LoadingProgress();
	private static HashMap<Integer, LoadingProgress> progressMap = new HashMap<Integer, LoadingProgress>();

	private GameLoading() {}

	public static int genProgressId(int total) {
		int id = lastId;
		lastId++;
		LoadingProgress data = new LoadingProgress();
		data.total = total;
		progressMap.put(id, data);
		return id;
	}
	public static int genProgressId() {
		int id = lastId;
		lastId++;
		progressMap.put(id, new LoadingProgress());
		return id;
	}
	public static void setProgressCount(int id, int count) {
		progressMap.get(id).total = count;
	}
	public static void setProgress(int id, int count) {
		progressMap.get(id).current = count;
	}

	private static void getTotalProgress() {
		Collection<LoadingProgress> data = progressMap.values();
		for(LoadingProgress curr: data) {
			allProgress.total += curr.total;
			allProgress.current += curr.current;
		}
	}

}
