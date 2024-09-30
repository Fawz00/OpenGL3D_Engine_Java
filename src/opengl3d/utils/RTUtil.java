package opengl3d.utils;

import java.nio.FloatBuffer;

import org.lwjgl.system.MemoryUtil;

public class RTUtil {
    public static boolean enableRT = false;
    private static FloatBuffer modelData;

    public static void ModelData(FloatBuffer data) {
        modelData = data;
    }

    public static void ClearData() {
        MemoryUtil.memFree(modelData);
    }
}
