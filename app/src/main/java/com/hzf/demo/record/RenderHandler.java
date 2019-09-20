package com.hzf.demo.record;
import android.graphics.SurfaceTexture;
import android.opengl.EGLContext;
import android.opengl.GLES20;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

/**
 * Helper class to draw texture to whole view on private thread
 */
public final class RenderHandler implements Runnable {
    private static final boolean DEBUG = true;    // TODO set false on release
    private static final String TAG = "RenderHandler";

    private final Object mSync = new Object();
    private EGLContext mShard_context;
    private boolean mIsRecordable;
    private Object mSurface;
    private int mTexId = -1;
    private int mTextureType;
    private int mViewWidth, mViewHeight;

    private boolean mRequestSetEglContext;
    private boolean mRequestRelease;
    private int mRequestDraw;

    public static final RenderHandler createHandler(final String name) {
        if (DEBUG) Log.v(TAG, "createHandler:");
        final RenderHandler handler = new RenderHandler();
        synchronized (handler.mSync) {
            new Thread(handler, !TextUtils.isEmpty(name) ? name : TAG).start();
            try {
                handler.mSync.wait();
            } catch (final InterruptedException e) {
            }
        }
        return handler;
    }

    public final void setEglContext(final EGLContext shared_context, final int tex_id, final Object surface, final boolean isRecordable,
                                    int textureType, int viewWidth, int viewHeight) {
        if (DEBUG) Log.i(TAG, "setEglContext:");
        if (!(surface instanceof Surface) && !(surface instanceof SurfaceTexture) && !(surface instanceof SurfaceHolder))
            throw new RuntimeException("unsupported window type:" + surface);
        synchronized (mSync) {
            if (mRequestRelease) return;
            mShard_context = shared_context;
            mTexId = tex_id;
            mTextureType = textureType;
            mViewWidth = viewWidth;
            mViewHeight = viewHeight;
            mSurface = surface;
            mIsRecordable = isRecordable;
            mRequestSetEglContext = true;
            mSync.notifyAll();
            try {
                mSync.wait();
            } catch (final InterruptedException e) {
            }
        }
    }

    public final void draw() {
        synchronized (mSync) {
            if (mRequestRelease) return;
            mRequestDraw++;
            mSync.notifyAll();
/*			try {
                mSync.wait();
			} catch (final InterruptedException e) {
			} */
        }
    }

    public boolean isValid() {
        synchronized (mSync) {
            return !(mSurface instanceof Surface) || ((Surface) mSurface).isValid();
        }
    }

    public final void release() {
        if (DEBUG) Log.i(TAG, "release:");
        synchronized (mSync) {
            if (mRequestRelease) return;
            mRequestRelease = true;
            mSync.notifyAll();
            try {
                mSync.wait();
            } catch (final InterruptedException e) {
            }
        }
    }

    //********************************************************************************
//********************************************************************************
    private EGLBase mEgl;
    private EGLBase.EglSurface mInputSurface;
    private ShaderViewDraw mDrawer;

    @Override
    public final void run() {
        if (DEBUG) Log.i(TAG, "RenderHandler thread started:");
        synchronized (mSync) {
            mRequestSetEglContext = mRequestRelease = false;
            mRequestDraw = 0;
            mSync.notifyAll();
        }
        boolean localRequestDraw;
        for (; ; ) {
            synchronized (mSync) {
                if (mRequestRelease) break;
                if (mRequestSetEglContext) {
                    mRequestSetEglContext = false;
                    internalPrepare();
                }
                localRequestDraw = mRequestDraw > 0;
                if (localRequestDraw) {
                    mRequestDraw--;
//					mSync.notifyAll();
                }
            }
            if (localRequestDraw) {
                if ((mEgl != null) && mTexId >= 0) {
                    mInputSurface.makeCurrent();
                    // clear screen with yellow color so that you can see rendering rectangle
                    GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);
                    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                    mDrawer.draw();
                    mInputSurface.swap();
                }
            } else {
                synchronized (mSync) {
                    try {
                        mSync.wait();
                    } catch (final InterruptedException e) {
                        break;
                    }
                }
            }
        }
        synchronized (mSync) {
            mRequestRelease = true;
            internalRelease();
            mSync.notifyAll();
        }
        if (DEBUG) Log.i(TAG, "RenderHandler thread finished:");
    }

    private final void internalPrepare() {
        if (DEBUG) Log.i(TAG, "internalPrepare:");
        internalRelease();
        mEgl = new EGLBase(mShard_context, false, mIsRecordable);

        mInputSurface = mEgl.createFromSurface(mSurface);

        mInputSurface.makeCurrent();
        mDrawer = new ShaderViewDraw();
        mDrawer.resetTextureID(mTexId, mTextureType, mViewWidth, mViewHeight);
        mSurface = null;
        mSync.notifyAll();
    }

    private final void internalRelease() {
        if (DEBUG) Log.i(TAG, "internalRelease:");
        if (mInputSurface != null) {
            mInputSurface.release();
            mInputSurface = null;
        }
        if (mDrawer != null) {
            mDrawer.release();
            mDrawer = null;
        }
        if (mEgl != null) {
            mEgl.release();
            mEgl = null;
        }
    }

}
