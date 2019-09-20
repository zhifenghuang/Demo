package com.hzf.demo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.hzf.demo.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gigabud on 17-3-1.
 */

public class WarterMarkView extends StickerView {

    private TextPaint mPaint;
    private int mScreenW, mScreenHeight;
    private Bitmap mImgTemp;
    private long mToucDownTime;
    private float mDownX, mDownY;
    private String mPostText;
    private int mTextColor;
    private boolean mIsCenter;

    public static final int TEXT_SIZE = 60;  //水印文字大小，单位sp

    public WarterMarkView(Context context) {
        this(context, null);
    }

    public WarterMarkView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WarterMarkView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScreenW = Utils.getScreenWidth(context);
        mScreenHeight = Utils.getScreenHeight(context);
        init();
    }

    private void init() {
        mPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
        mPaint.setDither(true);
        mPaint.setFilterBitmap(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);

    }

    public void setFixPostText(StickerBean stickerBean) {
        mPostText = stickerBean.text;
        mTextColor = stickerBean.color;
        mIsCenter = stickerBean.isTextCenter;
        createDrawable(stickerBean.text, stickerBean.initWidth, stickerBean.initHeight, stickerBean.centerX,
                stickerBean.centerY, stickerBean.rotationDegree, stickerBean.scale, stickerBean.color,
                stickerBean.widthRatio, stickerBean.heightRatio, stickerBean.isTextCenter);
    }

    public void setPostText(String postText, int textColor, boolean isTextCenter) {
        mPostText = postText;
        mTextColor = textColor;
        mIsCenter = isTextCenter;
        createDrawable(postText, textColor, isTextCenter);
    }

    private void createDrawable(String letter, int width, int height, int centerX, int centerY, float rotationDegree, float scale, int textColor, float widthRatio, float heightRatio, boolean isTextCenter) {
        float textSize = Utils.sp2px(getContext(), TEXT_SIZE);
        if (widthRatio > heightRatio) {
            textSize = textSize * heightRatio / widthRatio;
        }
        mPaint.setTextSize(textSize);
        if (!Utils.isTextHadChar(letter) && letter.length() == 2) {   //判断是否是全是表情符号，且只有一个表情
            int newWidth = (int) (mPaint.measureText(letter, 0, letter.length()) + 0.5f);
            centerX -= (newWidth - width) / 2;
            width = newWidth;
        }
        StaticLayout layout = new StaticLayout(letter, mPaint, width,
                isTextCenter ? Layout.Alignment.ALIGN_CENTER : Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
        while (layout.getHeight() - height > textSize) {
            textSize -= Utils.sp2px(getContext(), 1);
            mPaint.setTextSize(textSize);
            layout = new StaticLayout(letter, mPaint, width,
                    isTextCenter ? Layout.Alignment.ALIGN_CENTER : Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
        }


        centerY -= (layout.getHeight() - height) / 2;
        mImgTemp = Bitmap.createBitmap(layout.getWidth(), layout.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mImgTemp);
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setFilterBitmap(false);
        mPaint.setColor(textColor);
        canvas.save();
        layout.draw(canvas);
        canvas.restore();
        setFixWarterMark(mImgTemp, centerX, centerY, rotationDegree, scale);
    }

    private void createDrawable(String letter, int textColor, boolean isTextCenter) {
        mPaint.setTextSize(Utils.sp2px(getContext(), TEXT_SIZE));
        int textLength = (int) (mPaint.measureText(letter, 0, letter.length()) + 0.5f);
        int maxWidth = mScreenW - Utils.dip2px(getContext(), 42);
        StaticLayout layout = new StaticLayout(letter, mPaint, maxWidth > textLength ? textLength : maxWidth,
                isTextCenter ? Layout.Alignment.ALIGN_CENTER : Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
        int width = layout.getWidth();
        int height = layout.getHeight();
        mImgTemp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mImgTemp);
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setFilterBitmap(false);
        mPaint.setColor(textColor);
        canvas.save();
        layout.draw(canvas);
        canvas.restore();
        if (isTextCenter) {
            setWaterMark(mImgTemp, "", -1, mScreenW / 2, mScreenHeight / 2, 1.0f, null);
        } else {
            setWaterMark(mImgTemp, "", -1, (Utils.dip2px(getContext(), 21) + mImgTemp.getWidth()) / 2,
                    mScreenHeight / 2, 1.0f, null);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mIsDownInStricker = isPointerDownIn(event.getX(), event.getY());
        }
        if (!mIsDownInStricker) {
            return false;
        }
        if (mIsFix) {
            return super.dispatchTouchEvent(event);
        }
        super.dispatchTouchEvent(event);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mToucDownTime = System.currentTimeMillis();
                mDownX = event.getX();
                mDownY = event.getY();
                break;
            // 多点触摸
            case MotionEvent.ACTION_POINTER_DOWN:
                mToucDownTime = 0;
                break;
            case MotionEvent.ACTION_UP:
                if (System.currentTimeMillis() - mToucDownTime < 100) {
                    int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
                    if (isPointerDownIn(event.getX(), event.getY()) &&
                            Math.abs(event.getX() - mDownX) < slop && Math.abs(event.getY() - mDownY) < slop) {
                        performClick();
                    }
                }
                break;
        }
        return true;
    }

    public void recycleBmp() {
        if (mImgTemp != null && !mImgTemp.isRecycled()) {
            mImgTemp.recycle();
        }
        mImgTemp = null;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("x", getStrickerCenterX());
            jsonObject.put("y", getStrickerCenterY());
            jsonObject.put("w", getStrickerInitWidth());
            jsonObject.put("h", getStrickerInitHeight());
            jsonObject.put("rotation", getStrickerRotationDegree());
            jsonObject.put("scale", getStrickerScale());
            jsonObject.put("text", mPostText);
            jsonObject.put("color", mTextColor);
            jsonObject.put("isCenter", mIsCenter ? 1 : 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public StickerBean toStickerBean() {
        StickerBean stickerBean = new StickerBean();
        stickerBean.centerX = getStrickerCenterX();
        stickerBean.centerY = getStrickerCenterY();
        stickerBean.initWidth = getStrickerInitWidth();
        stickerBean.initHeight = getStrickerInitHeight();
        stickerBean.rotationDegree = Float.parseFloat(getStrickerRotationDegree());
        stickerBean.scale = Float.parseFloat(getStrickerScale());
        stickerBean.text = mPostText;
        stickerBean.color = mTextColor;
        stickerBean.isTextCenter = mIsCenter;
        stickerBean.type = TEXT_WARTERMARK;
        return stickerBean;
    }
}
