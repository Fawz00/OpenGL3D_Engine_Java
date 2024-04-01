package opengl3d.audio;

import java.util.List;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;

public class AudioMaster {
	private static long ALDevice;
	private static long ALContext;
	private static List<Integer> buffers = new ArrayList<Integer>();

	public static void init() {
		ALDevice = ALC10.alcOpenDevice(ALC10.alcGetString(0, ALC10.ALC_DEVICE_SPECIFIER));
		if(ALDevice == 0L){
			System.out.println("Audio device not found.");
			throw new IllegalStateException("Failed to open the default OpenAL device.");
		} else System.out.println("Audio connected to: " + ALC10.alcGetString(ALDevice, ALC10.ALC_DEFAULT_DEVICE_SPECIFIER));

		ALCCapabilities alcCaps = ALC.createCapabilities(ALDevice);
		ALContext = ALC10.alcCreateContext(ALDevice, (IntBuffer) null);
		ALC10.alcMakeContextCurrent(ALContext);
		AL.createCapabilities(alcCaps);
	}

	public static void setListenerData() {
		AL10.alListener3f(AL10.AL_POSITION, 0f, 0f, 0f);
		AL10.alListener3f(AL10.AL_VELOCITY, 0f, 0f, 0f);
	}

	public static int loadSound(String file) {
		int buffer = AL10.alGenBuffers();
		buffers.add(buffer);
		WaveData waveFile = WaveData.create(file);
		AL10.alBufferData(buffer, waveFile.format, waveFile.data, waveFile.samplerate);
		waveFile.dispose();
		return buffer;
	}

	public static void cleanUp() {
		for (int buffer : buffers) {
			AL10.alDeleteBuffers(buffer);
		}
		ALC10.alcCloseDevice(ALDevice);
		ALC.destroy();
	}

}
