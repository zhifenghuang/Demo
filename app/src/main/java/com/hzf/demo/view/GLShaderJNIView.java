package com.hzf.demo.view;

/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import com.hzf.demo.record.MediaEncoder;
import com.hzf.demo.record.MediaMuxerWrapper;
import com.hzf.demo.record.MediaVideoEncoder;
import com.hzf.demo.record.OnCameraUseListener;
import com.hzf.demo.utils.BitmapUtil;
import com.hzf.demo.utils.Constants;
import com.hzf.demo.utils.Utils;
import com.yeemos.yeemos.jni.ShaderJNILib;

import java.io.IOException;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

/**
 * A simple GLSurfaceView sub-class that demonstrate how to perform OpenGL ES
 * 2.0 rendering into a GL Surface. Note the following important details:
 * <p/>
 * - The class must use a custom context factory to enable 2.0 rendering. See
 * ContextFactory class definition below.
 * <p/>
 * - The class must use a custom EGLConfigChooser to be able to select an
 * EGLConfig that supports 2.0. This is done by providing a config specification
 * to eglChooseConfig() that has the attribute EGL10.ELG_RENDERABLE_TYPE
 * containing the EGL_OPENGL_ES2_BIT flag set. See ConfigChooser class
 * definition below.
 * <p/>
 * - The class must select the surface's format, then choose an EGLConfig that
 * matches it exactly (with regards to red/green/blue/alpha channels bit
 * depths). Failure to do so would result in an EGL_BAD_MATCH error.
 */
@SuppressLint({"InlinedApi", "NewApi"})
public class GLShaderJNIView extends GLSurfaceView {
    private static String TAG = "GL2JNIView";
    private static final boolean DEBUG = false;
    private int mShaderFilterType;
    private MediaPlayer mMediaPlayer;
    private String mSrcPath;

    private MediaVideoEncoder mVideoEncoder;

    private int mTextureID;

    private Context mContext;


    public static final String KEY_SHADER_PICTURE = "shaderPicture";

    public interface OnSaveBmp {
        void getGLViewBmp(Bitmap bmp);
    }

    private OnSaveBmp mOnSaveBmp;
    private boolean mIsInReadToBmp;


    public GLShaderJNIView(Context context) {
        super(context);
        mContext = context;
        init(false, 0, 0);
    }

    public GLShaderJNIView(Context context, AttributeSet attr) {
        super(context, attr);
        mContext = context;
        init(false, 0, 0);
    }

    public GLShaderJNIView(Context context, boolean translucent, int depth,
                           int stencil) {
        super(context);
        mContext = context;
        init(translucent, depth, stencil);
    }

    public void setShaderFilterType(int filterType, String srcPath) {
        mShaderFilterType = filterType;
        mSrcPath = srcPath;
    }

    public void startGetGLBmp(OnSaveBmp onSaveBmp) {
        mOnSaveBmp = onSaveBmp;
        mIsInReadToBmp = true;
        requestRender();
    }

    public void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            } catch (Exception e) {
                Log.e("releaseMediaPlayer", "Exception: " + e.toString());
            }
        }
    }

    private void init(boolean translucent, int depth, int stencil) {
//        if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ) {
//            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//        }
        mIsInReadToBmp = false;
        /*
         * By default, GLSurfaceView() creates a RGB_565 opaque surface. If we
         * want a translucent one, we should change the surface's format here,
         * using PixelFormat.TRANSLUCENT for GL Surfaces is interpreted as any
         * 32-bit surface with alpha by SurfaceFlinger.
         */
        if (translucent) {
            this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        }

        /*
         * Setup the context factory for 2.0 rendering. See ContextFactory class
         * definition below
         */
        setEGLContextFactory(new ContextFactory());

        /*
         * We need to choose an EGLConfig that matches the format of our surface
         * exactly. This is going to be done in our custom config chooser. See
         * ConfigChooser class definition below.
         */
        setEGLConfigChooser(translucent ? new ConfigChooser(8, 8, 8, 8, depth,
                stencil) : new ConfigChooser(5, 6, 5, 0, depth, stencil));

        /* Set the renderer responsible for frame rendering */
        setRenderer(new Renderer());
        setBackgroundColor(Color.TRANSPARENT);
    }

    private static class ContextFactory implements
            EGLContextFactory {
        private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

        public EGLContext createContext(EGL10 egl, EGLDisplay display,
                                        EGLConfig eglConfig) {
            Log.w(TAG, "creating OpenGL ES 2.0 context");
            checkEglError("Before eglCreateContext", egl);
            int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};
            EGLContext context = egl.eglCreateContext(display, eglConfig,
                    EGL10.EGL_NO_CONTEXT, attrib_list);
            checkEglError("After eglCreateContext", egl);
            return context;
        }

        public void destroyContext(EGL10 egl, EGLDisplay display,
                                   EGLContext context) {
            egl.eglDestroyContext(display, context);
        }
    }

    private static void checkEglError(String prompt, EGL10 egl) {
        int error;
        while ((error = egl.eglGetError()) != EGL10.EGL_SUCCESS) {
            Log.e(TAG, String.format("%s: EGL error: 0x%x", prompt, error));
        }
    }

    private static class ConfigChooser implements
            EGLConfigChooser {

        public ConfigChooser(int r, int g, int b, int a, int depth, int stencil) {
            mRedSize = r;
            mGreenSize = g;
            mBlueSize = b;
            mAlphaSize = a;
            mDepthSize = depth;
            mStencilSize = stencil;
        }

        /*
         * This EGL config specification is used to specify 2.0 rendering. We
         * use a minimum size of 4 bits for red/green/blue, but will perform
         * actual matching in chooseConfig() below.
         */
        private static int EGL_OPENGL_ES2_BIT = 4;
        private static int[] s_configAttribs2 = {EGL10.EGL_RED_SIZE, 4,
                EGL10.EGL_GREEN_SIZE, 4, EGL10.EGL_BLUE_SIZE, 4,
                EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT, EGL10.EGL_NONE};

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {

            /*
             * Get the number of minimally matching EGL configurations
             */
            int[] num_config = new int[1];
            egl.eglChooseConfig(display, s_configAttribs2, null, 0, num_config);

            int numConfigs = num_config[0];

            if (numConfigs <= 0) {
                throw new IllegalArgumentException(
                        "No configs match configSpec");
            }

            /*
             * Allocate then read the array of minimally matching EGL configs
             */
            EGLConfig[] configs = new EGLConfig[numConfigs];
            egl.eglChooseConfig(display, s_configAttribs2, configs, numConfigs,
                    num_config);

            if (DEBUG) {
                printConfigs(egl, display, configs);
            }
            /*
             * Now return the "best" one
             */
            return chooseConfig(egl, display, configs);
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                                      EGLConfig[] configs) {
            for (EGLConfig config : configs) {
                int d = findConfigAttrib(egl, display, config,
                        EGL10.EGL_DEPTH_SIZE, 0);
                int s = findConfigAttrib(egl, display, config,
                        EGL10.EGL_STENCIL_SIZE, 0);

                // We need at least mDepthSize and mStencilSize bits
                if (d < mDepthSize || s < mStencilSize)
                    continue;

                // We want an *exact* match for red/green/blue/alpha
                int r = findConfigAttrib(egl, display, config,
                        EGL10.EGL_RED_SIZE, 0);
                int g = findConfigAttrib(egl, display, config,
                        EGL10.EGL_GREEN_SIZE, 0);
                int b = findConfigAttrib(egl, display, config,
                        EGL10.EGL_BLUE_SIZE, 0);
                int a = findConfigAttrib(egl, display, config,
                        EGL10.EGL_ALPHA_SIZE, 0);

                if (r == mRedSize && g == mGreenSize && b == mBlueSize
                        && a == mAlphaSize)
                    return config;
            }
            return null;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display,
                                     EGLConfig config, int attribute, int defaultValue) {

            if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
                return mValue[0];
            }
            return defaultValue;
        }

        private void printConfigs(EGL10 egl, EGLDisplay display,
                                  EGLConfig[] configs) {
            int numConfigs = configs.length;
            Log.w(TAG, String.format("%d configurations", numConfigs));
            for (int i = 0; i < numConfigs; i++) {
                Log.w(TAG, String.format("Configuration %d:\n", i));
                printConfig(egl, display, configs[i]);
            }
        }

        private void printConfig(EGL10 egl, EGLDisplay display, EGLConfig config) {
            int[] attributes = {EGL10.EGL_BUFFER_SIZE, EGL10.EGL_ALPHA_SIZE,
                    EGL10.EGL_BLUE_SIZE,
                    EGL10.EGL_GREEN_SIZE,
                    EGL10.EGL_RED_SIZE,
                    EGL10.EGL_DEPTH_SIZE,
                    EGL10.EGL_STENCIL_SIZE,
                    EGL10.EGL_CONFIG_CAVEAT,
                    EGL10.EGL_CONFIG_ID,
                    EGL10.EGL_LEVEL,
                    EGL10.EGL_MAX_PBUFFER_HEIGHT,
                    EGL10.EGL_MAX_PBUFFER_PIXELS,
                    EGL10.EGL_MAX_PBUFFER_WIDTH,
                    EGL10.EGL_NATIVE_RENDERABLE,
                    EGL10.EGL_NATIVE_VISUAL_ID,
                    EGL10.EGL_NATIVE_VISUAL_TYPE,
                    0x3030, // EGL10.EGL_PRESERVED_RESOURCES,
                    EGL10.EGL_SAMPLES,
                    EGL10.EGL_SAMPLE_BUFFERS,
                    EGL10.EGL_SURFACE_TYPE,
                    EGL10.EGL_TRANSPARENT_TYPE,
                    EGL10.EGL_TRANSPARENT_RED_VALUE,
                    EGL10.EGL_TRANSPARENT_GREEN_VALUE,
                    EGL10.EGL_TRANSPARENT_BLUE_VALUE,
                    0x3039, // EGL10.EGL_BIND_TO_TEXTURE_RGB,
                    0x303A, // EGL10.EGL_BIND_TO_TEXTURE_RGBA,
                    0x303B, // EGL10.EGL_MIN_SWAP_INTERVAL,
                    0x303C, // EGL10.EGL_MAX_SWAP_INTERVAL,
                    EGL10.EGL_LUMINANCE_SIZE, EGL10.EGL_ALPHA_MASK_SIZE,
                    EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RENDERABLE_TYPE,
                    0x3042 // EGL10.EGL_CONFORMANT
            };
            String[] names = {"EGL_BUFFER_SIZE", "EGL_ALPHA_SIZE",
                    "EGL_BLUE_SIZE", "EGL_GREEN_SIZE", "EGL_RED_SIZE",
                    "EGL_DEPTH_SIZE", "EGL_STENCIL_SIZE", "EGL_CONFIG_CAVEAT",
                    "EGL_CONFIG_ID", "EGL_LEVEL", "EGL_MAX_PBUFFER_HEIGHT",
                    "EGL_MAX_PBUFFER_PIXELS", "EGL_MAX_PBUFFER_WIDTH",
                    "EGL_NATIVE_RENDERABLE", "EGL_NATIVE_VISUAL_ID",
                    "EGL_NATIVE_VISUAL_TYPE", "EGL_PRESERVED_RESOURCES",
                    "EGL_SAMPLES", "EGL_SAMPLE_BUFFERS", "EGL_SURFACE_TYPE",
                    "EGL_TRANSPARENT_TYPE", "EGL_TRANSPARENT_RED_VALUE",
                    "EGL_TRANSPARENT_GREEN_VALUE",
                    "EGL_TRANSPARENT_BLUE_VALUE", "EGL_BIND_TO_TEXTURE_RGB",
                    "EGL_BIND_TO_TEXTURE_RGBA", "EGL_MIN_SWAP_INTERVAL",
                    "EGL_MAX_SWAP_INTERVAL", "EGL_LUMINANCE_SIZE",
                    "EGL_ALPHA_MASK_SIZE", "EGL_COLOR_BUFFER_TYPE",
                    "EGL_RENDERABLE_TYPE", "EGL_CONFORMANT"};
            int[] value = new int[1];
            for (int i = 0; i < attributes.length; i++) {
                int attribute = attributes[i];
                String name = names[i];
                if (egl.eglGetConfigAttrib(display, config, attribute, value)) {
                    Log.w(TAG, String.format("  %s: %d\n", name, value[0]));
                } else {
                    while (egl.eglGetError() != EGL10.EGL_SUCCESS)
                        ;
                }
            }
        }

        // Subclasses can adjust these values:
        protected int mRedSize;
        protected int mGreenSize;
        protected int mBlueSize;
        protected int mAlphaSize;
        protected int mDepthSize;
        protected int mStencilSize;
        private int[] mValue = new int[1];
    }

    private class Renderer implements GLSurfaceView.Renderer,
            SurfaceTexture.OnFrameAvailableListener {
        private SurfaceTexture mSurface;

        public void onDrawFrame(GL10 gl) {
            if (mIsPause) {
                return;
            }
            ShaderJNILib.step();
            if (mShaderFilterType != Constants.PIC_SHADER_FILTER) {
                mSurface.updateTexImage();
            }

            if (mIsInReadToBmp) {
                mIsInReadToBmp = false;
                Bitmap bitmap = createBitmapFromGLSurface(getWidth(), getHeight(), gl);
                if (mOnSaveBmp != null) {
                    mOnSaveBmp.getGLViewBmp(bitmap);
                }
            }

            if (mVideoEncoder != null) {
                mVideoEncoder.frameAvailableSoon();
            }
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            ShaderJNILib.init(width, height);
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            mTextureID = createTextureID();
            ShaderJNILib.setTextureID(mTextureID);
            if (mShaderFilterType != Constants.PIC_SHADER_FILTER) {
                mMediaPlayer = new MediaPlayer();
                try {
                    mMediaPlayer.setDataSource(mSrcPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mSurface = new SurfaceTexture(mTextureID);
                mSurface.setOnFrameAvailableListener(Renderer.this);
                Surface surface = new Surface(mSurface);
                mMediaPlayer.setSurface(surface);
                mMediaPlayer.setScreenOnWhilePlaying(true);
                surface.release();
                try {
                    mMediaPlayer.prepare();
                } catch (Exception e) {
                    Log.e(TAG, "media player prepare failed: "
                            + e.toString() + ", " + mSrcPath);
                    if (e instanceof IllegalStateException) {
                        Utils.deleteFile(mSrcPath);
                    }
                    releaseMediaPlayer();
                }
                mMediaPlayer.setLooping(true);
                mMediaPlayer.start();
            }
        }

        private int createTextureID() {
            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            if (mShaderFilterType == Constants.PIC_SHADER_FILTER) {
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                        GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
                Bitmap bmp = BitmapUtil.getBitmapFromFile(mSrcPath, getWidth(), getHeight());

                if (bmp != null) {
                    bmp = cutBitmapToFitView(bmp);
                    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
                    bmp.recycle();
                }
            } else {
                GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                        textures[0]);
                GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                        GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                        GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                        GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                        GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            }
            return textures[0];
        }

        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            // TODO Auto-generated method stub
            if (mShaderFilterType != Constants.PIC_SHADER_FILTER && !mIsPause) {
                requestRender();
            }
        }

        private Bitmap createBitmapFromGLSurface(int w, int h, GL10 gl) throws OutOfMemoryError {
            int bitmapBuffer[] = new int[w * h];
            int bitmapSource[] = new int[w * h];
            IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
            intBuffer.position(0);
            try {
                gl.glReadPixels(0, 0, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
                int offset1, offset2;
                for (int i = 0; i < h; i++) {
                    offset1 = i * w;
                    offset2 = (h - i - 1) * w;
                    for (int j = 0; j < w; j++) {
                        int texturePixel = bitmapBuffer[offset1 + j];
                        int blue = (texturePixel >> 16) & 0xff;
                        int red = (texturePixel << 16) & 0x00ff0000;
                        int pixel = (texturePixel & 0xff00ff00) | red | blue;
                        bitmapSource[offset2 + j] = pixel;
                    }
                }
            } catch (GLException e) {
                return null;
            }
            return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
        }
    }

    private String mStrVideoPath;
    private boolean mIsRecording;
    /**
     * muxer for audio/video recording
     */
    private MediaMuxerWrapper mMuxer;

    /**
     * 开始录制
     */
    public void startRecording() {
        mIsRecording = true;
        try {
            mStrVideoPath = Utils.createVideoPath(mContext);
            mMuxer = new MediaMuxerWrapper(mStrVideoPath);

            int width = 720;
            DisplayMetrics dis = new DisplayMetrics();
            ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dis);
            int height = (int) (width * 1.0f * dis.heightPixels / dis.widthPixels + 0.5f);
            new MediaVideoEncoder(mMuxer, mMediaEncoderListener, width, height);
            mMuxer.prepare();
            mMuxer.startRecording();
        } catch (final IOException e) {
            Log.e(TAG, "startCapture:", e);
        }
    }

    /**
     * 开始录制
     */
    public void startRecording(String path) {
        mIsRecording = true;
        try {
            mStrVideoPath = path;
            mMuxer = new MediaMuxerWrapper(mStrVideoPath);

            int width = 720;
            DisplayMetrics dis = new DisplayMetrics();
            ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dis);
            int height = (int) (width * 1.0f * dis.heightPixels / dis.widthPixels + 0.5f);
            new MediaVideoEncoder(mMuxer, mMediaEncoderListener, width, height);
            mMuxer.prepare();
            mMuxer.startRecording();
        } catch (final IOException e) {
            Log.e(TAG, "startCapture:", e);
        }
    }

    /**
     * 开始录制
     */
    public boolean isRecording() {
        return mIsRecording;
    }

    /**
     * 停止录制
     */
    public synchronized String stopRecording(OnCameraUseListener onCameraUseListener) {
        releaseResources();
        if (mMuxer == null) {
            return mStrVideoPath;
        }
        mMuxer.stopRecording();
        mMuxer = null;

        if (onCameraUseListener != null) {
            onCameraUseListener.recordingEnd(mStrVideoPath);
        }
        return mStrVideoPath;
    }

    /**
     * 释放资源
     */
    public void releaseResources() {
        mIsRecording = false;
    }

    public void setVideoEncoder(final MediaVideoEncoder encoder) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    if (encoder != null) {
                        encoder.initRenderHandler(EGL14.eglGetCurrentContext(), mTextureID, mShaderFilterType, getWidth(), getHeight());
                    }
                    mVideoEncoder = encoder;
                }
            }
        });
    }

    /**
     * callback methods from encoder
     */
    private final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
        @Override
        public void onPrepared(final MediaEncoder encoder) {
            if (encoder instanceof MediaVideoEncoder)
                setVideoEncoder((MediaVideoEncoder) encoder);
        }

        @Override
        public void onStopped(final MediaEncoder encoder) {
            if (encoder instanceof MediaVideoEncoder)
                setVideoEncoder(null);
        }
    };

    private boolean mIsPause;

    public void onMediaResume() {
        super.onResume();
        if (mMediaPlayer != null && mIsPause) {
            mMediaPlayer.start();
        }
        mIsPause = false;
    }

    public void onMediaPause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
        mIsPause = true;
    }

    public interface OnPlayMovieListener {
        public void onPlayMovie(long totalTime, long currentTime);
    }

    private Bitmap cutBitmapToFitView(Bitmap bmp) {
        float ratio1 = bmp.getHeight() * 1.0f / bmp.getWidth();
        float ratio2 = getHeight() * 1.0f / getWidth();
        if (Math.abs(ratio1 - ratio2) > 0.1f) {
            if (ratio1 < ratio2) {
                int newWidth = bmp.getHeight() * getWidth() / getHeight();
                if (newWidth <= 0) {
                    return bmp;
                }
                int startX = (int) ((bmp.getWidth() - newWidth) * 0.5);
                bmp = Bitmap.createBitmap(bmp, startX, 0, newWidth, bmp.getHeight(), null, false);
            } else {
                int newHeight = bmp.getWidth() * getHeight() / getWidth();
                int startY = (int) ((bmp.getHeight() - newHeight) * 0.5);
                if (newHeight <= 0) {
                    return bmp;
                }
                bmp = Bitmap.createBitmap(bmp, 0, startY, bmp.getWidth(), newHeight, null, false);
            }
        }
        return bmp;
    }
}

