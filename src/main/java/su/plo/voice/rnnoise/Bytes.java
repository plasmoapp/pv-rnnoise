package su.plo.voice.rnnoise;

public abstract class Bytes {
    public Bytes() {
    }

    public static byte[] toByteArray(float[] input) {
        byte[] ret = new byte[input.length * 2];

        for(int i = 0; i < input.length; ++i) {
            short x = Float.valueOf(input[i]).shortValue();
            ret[i * 2] = (byte)(x & 255);
            ret[i * 2 + 1] = (byte)((x & '\uff00') >> 8);
        }

        return ret;
    }

    public static float[] toFloatArray(byte[] input) {
        float[] ret = new float[input.length / 2];

        for(int i = 0; i < input.length / 2; ++i) {
            if ((input[i * 2 + 1] & 128) != 0) {
                ret[i] = (float)(-32768 + ((input[i * 2 + 1] & 127) << 8) | input[i * 2] & 255);
            } else {
                ret[i] = (float)(input[i * 2 + 1] << 8 & '\uff00' | input[i * 2] & 255);
            }
        }

        return ret;
    }
}
