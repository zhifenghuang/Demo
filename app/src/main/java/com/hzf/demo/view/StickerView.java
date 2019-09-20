package com.hzf.demo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.hzf.demo.R;
import com.hzf.demo.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

import pl.droidsonroids.gif.GifDrawable;


public class StickerView extends View {

    public float MIN_SCALE_SIZE = 0.1f;
    private float[] mOriginPoints;
    private float[] mPoints;
    private RectF mOriginContentRect;
    private RectF mContentRect;
    private RectF mViewRect;

    private float mLastPointX, mLastPointY;

    private float mRotation;

    private Bitmap mBitmap;
    private Matrix mMatrix;

    private float mStickerScaleSize = 1.0f, mFirstStickerScaleSize = 1.0f;

    private ImageButton mBtnStricker;

    private int mBtnX, mBtnY;

    private int mInitWidth, mInitHeight;

    private int mStrickerIndex;

    private String mStrickerName;

    protected boolean mIsFix;  //是否固定不动

    public static final int EMO_STICKER = 0;//0.表示表情，
    public static final int TEXT_WARTERMARK = 1;//１表示文字水印
    public static final int GIF_STICKER = 2;//１表示gif

    private DeleteStickerListener mDeleteStickerListener;

    protected boolean mIsDownInStricker;

    private GifDrawable mGifDrawable;

    private long mGifStartTime;

    public static class StickerBean {
        public int centerX;
        public int centerY;
        public int initWidth;
        public int initHeight;
        public float rotationDegree;
        public float scale;
        public int stickerIndex;
        public String stickerName;
        public String text;
        public int color;
        public boolean isTextCenter;
        public int type;//0.表示表情，１表示文字水印
        public float widthRatio = 1.0f;//屏幕宽缩放比例
        public float heightRatio = 1.0f;//屏幕高缩放比例
    }

    /**
     * 模式 NONE：无 DRAG：拖拽. ZOOM:缩放
     */
    private enum MODE {
        NONE, DRAG, ZOOM
    }

    private MODE mode = MODE.NONE;// 默认模式

    public StickerView(Context context) {
        this(context, null);
    }

    public StickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setImageDrawable(GifDrawable drawable) {
        if (drawable == null || drawable == mGifDrawable) {
            return;
        }
        mGifStartTime = System.currentTimeMillis();
        mGifDrawable = drawable;
    }

    public void setGifWaterMark(Bitmap bmp, String name, int centerX, int centerY, float scale, ImageButton btnStricker) {
        mBtnStricker = btnStricker;
        mIsFix = false;
        mFirstStickerScaleSize = mStickerScaleSize = scale;
        mStrickerName = name;
        mRotation = 0.0f;
        setFocusable(true);
        mInitWidth = bmp.getWidth();
        mInitHeight = bmp.getHeight();
        bmp.recycle();
        try {
            float px = mInitWidth;
            float py = mInitHeight;
            mOriginPoints = new float[]{0, 0, px, 0, px, py, 0, py, px / 2, py / 2};
            mOriginContentRect = new RectF(0, 0, px, py);
            mPoints = new float[10];
            mPoints[8] = centerX;
            mPoints[9] = centerY;
            mContentRect = new RectF();
            mMatrix = new Matrix();
            RectF rectF = new RectF();
            mMatrix.mapRect(rectF, mOriginContentRect);
            mMatrix.postTranslate(centerX - px / 2, centerY - py / 2);
            mMatrix.postScale(mStickerScaleSize, mStickerScaleSize, centerX, centerY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        postInvalidate();
    }

    public void setFixGifWaterMark(Bitmap bmp, String name, int centerX, int centerY, float rotationDegree, float scale) {
        mIsFix = true;
        mStickerScaleSize = scale;
        mStrickerName = name;
        mRotation = rotationDegree;
        setFocusable(false);
        mInitWidth = bmp.getWidth();
        mInitHeight = bmp.getHeight();
        try {
            float px = bmp.getWidth();
            float py = bmp.getHeight();
            mOriginPoints = new float[]{0, 0, px, 0, px, py, 0, py, px / 2, py / 2};
            mOriginContentRect = new RectF(0, 0, px, py);
            mPoints = new float[10];
            mPoints[8] = centerX;
            mPoints[9] = centerY;
            mContentRect = new RectF();
            mMatrix = new Matrix();
            mMatrix.postTranslate(centerX - px / 2, centerY - py / 2);
            mMatrix.postRotate(mRotation, centerX, centerY);
            mMatrix.postScale(mStickerScaleSize, mStickerScaleSize, centerX, centerY);

            RectF rectF = new RectF();
            mMatrix.mapRect(rectF, mOriginContentRect);
        } catch (Exception e) {
            e.printStackTrace();
        }
        bmp.recycle();
        postInvalidate();
    }

    public void setFixWarterMark(@NonNull Bitmap bitmap, int centerX, int centerY, float rotationDegree, float scale) {
        mIsFix = true;
        mBitmap = bitmap;
        mStickerScaleSize = scale;
        mRotation = rotationDegree;
        setFocusable(false);
        mInitWidth = mBitmap.getWidth();
        mInitHeight = mBitmap.getHeight();
        try {
            float px = mBitmap.getWidth();
            float py = mBitmap.getHeight();
            mOriginPoints = new float[]{0, 0, px, 0, px, py, 0, py, px / 2, py / 2};
            mOriginContentRect = new RectF(0, 0, px, py);
            mPoints = new float[10];
            mPoints[8] = centerX;
            mPoints[9] = centerY;
            mContentRect = new RectF();
            mMatrix = new Matrix();
            mMatrix.postTranslate(centerX - px / 2, centerY - py / 2);
            mMatrix.postRotate(mRotation, centerX, centerY);
            mMatrix.postScale(mStickerScaleSize, mStickerScaleSize, centerX, centerY);

            RectF rectF = new RectF();
            mMatrix.mapRect(rectF, mOriginContentRect);
        } catch (Exception e) {
            e.printStackTrace();
        }
        postInvalidate();
    }

    public void setFix(boolean isFix) {
        mIsFix = isFix;
    }

    public boolean isFix() {
        return mIsFix;
    }

    public void setWaterMark(@NonNull Bitmap bitmap, String strickerName, int strickerIndex, int centerX, int centerY, float scale, ImageButton btnStricker) {
        mIsFix = false;
        mBitmap = bitmap;
        mStrickerIndex = strickerIndex;
        mStrickerName = strickerName;
        mBtnStricker = btnStricker;
        mFirstStickerScaleSize = mStickerScaleSize = scale;
        mRotation = 0.0f;
        mStrickerIndex = strickerIndex;
        setFocusable(true);
        mInitWidth = mBitmap.getWidth();
        mInitHeight = mBitmap.getHeight();
        try {
            float px = mInitWidth;
            float py = mInitHeight;
            mOriginPoints = new float[]{0, 0, px, 0, px, py, 0, py, px / 2, py / 2};
            mOriginContentRect = new RectF(0, 0, px, py);
            mPoints = new float[10];
            mPoints[8] = centerX;
            mPoints[9] = centerY;
            mContentRect = new RectF();
            mMatrix = new Matrix();
            RectF rectF = new RectF();
            mMatrix.mapRect(rectF, mOriginContentRect);
            mMatrix.postTranslate(centerX - px / 2, centerY - py / 2);
            mMatrix.postScale(mStickerScaleSize, mStickerScaleSize, centerX, centerY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        postInvalidate();
    }

    public boolean isPointerDownIn(float x, float y) {
        if (mIsFix || mContentRect == null) {
            return false;
        }
        return mContentRect.contains(x, y);
    }

    @Override
    public void setFocusable(boolean focusable) {
        super.setFocusable(focusable);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mMatrix == null || (mGifDrawable == null && mBitmap == null)) {
            return;
        }
        mMatrix.mapPoints(mPoints, mOriginPoints);
        mMatrix.mapRect(mContentRect, mOriginContentRect);
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, mMatrix, null);
        } else if (mGifDrawable != null) {
            try {
                long currentIndex = (System.currentTimeMillis() - mGifStartTime) * (mGifDrawable.getNumberOfFrames() - 1) / mGifDrawable.getDuration();
                canvas.drawBitmap(mGifDrawable.seekToFrameAndGet((int) currentIndex % mGifDrawable.getNumberOfFrames()), mMatrix, null);
            } catch (Exception e) {
            }
            postInvalidate();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mIsDownInStricker = mContentRect.contains(event.getX(), event.getY());
        }
        if (!mIsDownInStricker) {
            return false;
        }
        if (!isFocusable() || mIsFix) {
            return super.dispatchTouchEvent(event);
        }
        if (mViewRect == null) {
            mViewRect = new RectF(0f, 0f, getMeasuredWidth(), getMeasuredHeight());
        }
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (mContentRect.contains(event.getX(), event.getY())) {
                    mLastPointX = event.getX();
                    mLastPointY = event.getY();
                    mode = MODE.DRAG;

                    if (mBtnStricker != null) {
                        int[] screen = new int[2];
                        mBtnStricker.getLocationInWindow(screen);
                        mBtnX = screen[0];
                        mBtnY = Utils.getStatusBarHeight(getContext());
                        mBtnStricker.setImageResource(R.drawable.white_rubbish_bin);
                        mBtnStricker.setAlpha(1.0f);
                    }
                }
                break;
            // 多点触摸
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    mode = MODE.ZOOM;
                    mLastPointX = event.getX(1);
                    mLastPointY = event.getY(1);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mode == MODE.DRAG && mBtnStricker != null) {
                    int left = mBtnX;
                    int top = mBtnStricker.getTop() + mBtnY;
                    int right = mBtnStricker.getRight() + mBtnX;
                    int bottom = mBtnStricker.getWidth() + mBtnY;
                    if (event.getRawX() > left && event.getRawX() < right && event.getRawY() > top && event.getRawY() < bottom) {
                        if (mDeleteStickerListener != null) {
                            mDeleteStickerListener.deleteSticker(this);
                            return true;
                        }
                    }
                }
                if (mBtnStricker != null) {
                    mBtnStricker.setImageResource(R.drawable.edit_sticker);
                }
                this.setAlpha(1.0f);
                mLastPointX = 0;
                mLastPointY = 0;
                mode = MODE.NONE;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = MODE.NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == MODE.ZOOM) {
                    if (event.getPointerCount() == 2) {
                        float rotation = rotation(event);
                        mRotation += rotation;
                        if (mRotation > 360) {
                            mRotation -= 360;
                        } else if (mRotation < -360) {
                            mRotation += 360;
                        }
                        mMatrix.postRotate(rotation, mPoints[8], mPoints[9]);
                        float nowLenght = calculateLength(mLastPointX, mLastPointY);
                        float touchLenght = calculateLength(event.getX(1), event.getY(1));
                        if (Math.sqrt((nowLenght - touchLenght) * (nowLenght - touchLenght)) > 0.0f) {
                            float scale = touchLenght / nowLenght;
                            float nowsc = mStickerScaleSize * scale;
                            if (nowsc >= MIN_SCALE_SIZE) {
                                mMatrix.postScale(scale, scale, mPoints[8], mPoints[9]);
                                mStickerScaleSize = nowsc;
                            }
                        }
                        postInvalidate();
                        mLastPointX = event.getX(1);
                        mLastPointY = event.getY(1);
                    }
                } else if (mode == MODE.DRAG) { //拖动的操作
                    float cX = event.getX() - mLastPointX;
                    float cY = event.getY() - mLastPointY;
                    if (Math.sqrt(cX * cX + cY * cY) > 2.0f && canStickerMove(cX, cY)) {
                        mMatrix.postTranslate(cX, cY);
                        postInvalidate();
                        mLastPointX = event.getX();
                        mLastPointY = event.getY();
                    }
                    if (mBtnStricker != null) {
                        int left = mBtnX;
                        int top = mBtnStricker.getTop() + mBtnY;
                        int right = mBtnStricker.getRight() + mBtnX;
                        int bottom = mBtnStricker.getWidth() + mBtnY;
                        if (event.getRawX() > left && event.getRawX() < right && event.getRawY() > top && event.getRawY() < bottom) {
                            this.setAlpha(0.5f);
                        } else {
                            this.setAlpha(1.0f);
                        }
                    }
                }
                break;
        }
        return true;
    }


    private boolean canStickerMove(float cx, float cy) {
        float px = cx + mPoints[8];
        float py = cy + mPoints[9];
        return mViewRect.contains(px, py);
    }


    private float calculateLength(float x, float y) {
        float ex = x - mPoints[8];
        float ey = y - mPoints[9];
        return (float) Math.sqrt(ex * ex + ey * ey);
    }


    private float rotation(MotionEvent event) {
        float originDegree = calculateDegree(mLastPointX, mLastPointY);
        float nowDegree = calculateDegree(event.getX(1), event.getY(1));
        return nowDegree - originDegree;
    }

    private float calculateDegree(float x, float y) {
        double delta_x = x - mPoints[8];
        double delta_y = y - mPoints[9];
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    protected String getStrickerRotationDegree() {
        DecimalFormat decimalFormat = new DecimalFormat(".000");
        String rotationStr = decimalFormat.format(mRotation + 0.005f);
        rotationStr = rotationStr.replace(",", ".");
        return rotationStr;
    }

    protected int getStrickerCenterX() {
        return (int) (mPoints[8] + 0.5f);
    }

    protected int getStrickerCenterY() {
        return (int) (mPoints[9] + 0.5f);
    }

    protected String getStrickerScale() {
        DecimalFormat decimalFormat = new DecimalFormat(".000");
        String scaleStr = decimalFormat.format(mStickerScaleSize / mFirstStickerScaleSize + 0.005f);
        scaleStr = scaleStr.replace(",", ".");
        return scaleStr;
    }

    protected int getStrickerInitWidth() {
        return (int) (mInitWidth * mFirstStickerScaleSize + 0.5f);
    }

    protected int getStrickerInitHeight() {
        return (int) (mInitHeight * mFirstStickerScaleSize + 0.5f);
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("x", getStrickerCenterX());
            jsonObject.put("y", getStrickerCenterY());
            jsonObject.put("w", getStrickerInitWidth());
            jsonObject.put("h", getStrickerInitHeight());
            jsonObject.put("rotation", getStrickerRotationDegree());
            jsonObject.put("scale", getStrickerScale());
            jsonObject.put("index", mStrickerIndex);
            jsonObject.put("s_name", mStrickerName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public StickerBean toStickerBean() {
        StickerBean stickerBean = new StickerBean();
        stickerBean.centerX = getStrickerCenterX();
        stickerBean.centerY = getStrickerCenterY();
        stickerBean.initWidth = getStrickerInitWidth();
        stickerBean.initHeight = getStrickerInitHeight();
        stickerBean.rotationDegree = Float.parseFloat(getStrickerRotationDegree());
        stickerBean.scale = Float.parseFloat(getStrickerScale());
        stickerBean.stickerIndex = mStrickerIndex;
        stickerBean.stickerName = mStrickerName;
        stickerBean.type = mStrickerName.startsWith("http") ? GIF_STICKER : EMO_STICKER;
        return stickerBean;
    }

    public void setDeleteStickerListener(DeleteStickerListener deleteStickerListener) {
        mDeleteStickerListener = deleteStickerListener;
    }

    public interface DeleteStickerListener {
        public void deleteSticker(StickerView stickerView);
    }
}
