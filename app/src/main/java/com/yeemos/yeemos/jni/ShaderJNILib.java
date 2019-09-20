package com.yeemos.yeemos.jni;

/**
 * Created by gigabud on 15-12-4.
 */
public class ShaderJNILib {

    static {
        System.loadLibrary("filter");
    }

    public static native void setShaderType(int shaderType);

    public static native void setPlatform(int platform);

    public static native void init(int width, int height);

    public static native void setSrcSize(int srcWidth, int srcHeight);

    public static native void step();

    public static native void setTextureID(int textureID);

    public static native void resetXOffset(float xOffset, int filter);

    public static native void destroySource();

    public static native String getVertexSource();

    public static native String getFragmentSource();

    public static native void oneShaderinit(int width, int height);

    public static native void setOneShaderType(int shaderType,int filter);

    public static native void oneShaderStep();

    public static native void setOrginCurrentScreen(int orginScreenWidth, int orginScreenHeight,
                                                    int currentScreenWidth, int currentScreenHeight);

    public static native int convertPointX(int ponitX);

    public static native int convertPointY(int ponitY);
}
