package com.hzf.demo.record;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.hzf.demo.utils.Constants;
import com.hzf.demo.view.CameraGLSurfaceView;
import com.yeemos.yeemos.jni.ShaderJNILib;

import java.io.IOException;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLException;

/**
 * Created by gigabud on 17-3-3.
 */

public class CameraRenderer implements
        GLSurfaceView.Renderer,
        SurfaceTexture.OnFrameAvailableListener {

    private final CameraGLSurfaceView mView;

    private Camera mCamera = null;
    private SurfaceTexture mSrfTex = null;
    private int mSrfTexId = -1;
    private boolean updateSurface = false;

    private boolean mIsBackCamera;

    private boolean mIsSwitchSuccessful;

    private OnCameraUseListener mOnTakePicture;
    private boolean mIsTakingPicture;

    private long mSwitchCameraTime;

    private MediaVideoEncoder mVideoEncoder;


    public CameraRenderer(CameraGLSurfaceView view) {
        mView = view;
    }

    public void setCamera(Camera camera, boolean isBackCamera) {
        mIsSwitchSuccessful = false;
        mCamera = camera;
        mIsBackCamera = isBackCamera;
        mIsTakingPicture = false;
        mSwitchCameraTime = System.currentTimeMillis();

        ShaderJNILib.setOneShaderType(isBackCamera ? Constants.VIDEO_DEGREE_90 : Constants.VIDEO_DEGREE_270,
                isBackCamera ? Constants.COMMON_SHADER_TYPE : Constants.BEAUTIFY_SKIN_SHADER_TYPE);

        if (mSrfTexId >= 0) {
            try {
                mCamera.setPreviewTexture(mSrfTex);
            } catch (IOException t) {
            }
            synchronized (this) {
                updateSurface = false;
            }
            mCamera.startPreview();
            mIsSwitchSuccessful = true;
        }
    }

    public void takePicture(boolean isTakingPicture, OnCameraUseListener onTakePicture) {
        mIsTakingPicture = isTakingPicture;
        mOnTakePicture = onTakePicture;
    }

    public void setVideoEncoder(final MediaVideoEncoder encoder) {
        mView.queueEvent(new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    if (encoder != null) {
                        encoder.initRenderHandler(EGL14.eglGetCurrentContext(), mSrfTexId,
                                mIsBackCamera ? Constants.VIDEO_DEGREE_90 : Constants.VIDEO_DEGREE_270, mView.getWidth(), mView.getHeight());
                    }
                    mVideoEncoder = encoder;
                }
            }
        });
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (this) {
            updateSurface = true;
        }
        mView.requestRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_BLEND);

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        mSrfTexId = textures[0];

        ShaderJNILib.setTextureID(mSrfTexId);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mSrfTexId);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        mSrfTex = new SurfaceTexture(mSrfTexId);
        mSrfTex.setOnFrameAvailableListener(this);
        try {
            mCamera.setPreviewTexture(mSrfTex);
            mCamera.startPreview();
        } catch (Exception t) {
            return;
        }
        synchronized (this) {
            updateSurface = false;
        }
        mIsSwitchSuccessful = true;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        ShaderJNILib.oneShaderinit(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (!mIsSwitchSuccessful) {
            return;
        }

        synchronized (this) {
            if (updateSurface) {
                mSrfTex.updateTexImage();
                updateSurface = false;
            }
        }

        if (System.currentTimeMillis() - mSwitchCameraTime > 500) {
            ShaderJNILib.oneShaderStep();

            if (mVideoEncoder != null) {
                mVideoEncoder.frameAvailableSoon();
            }
        }

        if (mIsTakingPicture) {
            mIsTakingPicture = false;
            Bitmap bitmap = createBitmapFromGLSurface(mView.getWidth(), mView.getHeight(), gl);
            if (mOnTakePicture != null) {
                mOnTakePicture.takePicture(bitmap);
            }
        }
    }

    private Bitmap createBitmapFromGLSurface(int w, int h, GL10 gl) {
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
        } catch (Exception e) {
            return null;
        }
        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
    }
}
