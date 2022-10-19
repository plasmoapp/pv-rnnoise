package su.plo.voice.rnnoise;

import com.sun.jna.*;

import java.io.Closeable;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class Denoiser implements Closeable {

    private static final int FRAME_SIZE = 480;
    private Pointer state;
    private final AtomicBoolean stopped = new AtomicBoolean(false);

    public Denoiser() {
        state = RNNoise.INSTANCE.rnnoise_create(null);
    }

    public void close() {
        if (stopped.getAndSet(true)) {
            return;
        }

        RNNoise.INSTANCE.rnnoise_destroy(state);
        state = null;
    }

    public float[] process(float[] floats) {
        if (floats.length % FRAME_SIZE != 0) {
            return null;
        }

        float[] chunk = new float[FRAME_SIZE];
        float[] processedChunk = new float[FRAME_SIZE];
        float[] processed = new float[floats.length];

        for (int i = 0; i < floats.length / FRAME_SIZE; i++) {
            System.arraycopy(floats, FRAME_SIZE * i, chunk, 0, FRAME_SIZE);

            RNNoise.INSTANCE.rnnoise_process_frame(state, processedChunk, chunk);

            System.arraycopy(processedChunk, 0, processed, FRAME_SIZE * i, FRAME_SIZE);
        }

        return processed;
    }

    private static Boolean platformSupported = null;

    private static String getExtension(String platform) {
        switch (platform) {
            case "darwin":
                return "dylib";
            case "win32-x86":
            case "win32-x86-64":
                return "dll";
            default:
                return "so";
        }
    }

    public static boolean platformSupported() {
        if (platformSupported != null) {
            return platformSupported;
        }

        try {
            platformSupported = RNNoise.INSTANCE != null;
            return platformSupported;
        } catch (UnsatisfiedLinkError ignored) {
            platformSupported = false;
            return false;
        }
    }

    public static String loadFromJar() {
        String platform = Platform.RESOURCE_PREFIX;
        return String.format("/natives/rnnoise/%s/rnnoise.%s", platform, getExtension(platform));
    }

    public interface RNNoise extends Library {

        RNNoise INSTANCE = Native.loadLibrary(NativeLibrary.getInstance(loadFromJar()).getFile().getAbsolutePath(), RNNoise.class);

        int rnnoise_get_size();

        int rnnoise_get_frame_size();

        int rnnoise_init(Pointer state, Pointer model);

        Pointer rnnoise_create(Pointer model);

        void rnnoise_destroy(Pointer state);

        float rnnoise_process_frame(Pointer state, float[] out, float[] in);

        Pointer rnnoise_model_from_file(File file);

        void rnnoise_model_free(Pointer model);
    }

}
