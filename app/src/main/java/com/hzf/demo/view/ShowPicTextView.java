package com.hzf.demo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.hzf.demo.utils.Utils;

/**
 * Created by gigabud on 15-12-24.
 * 显示图片和文字的View
 */
public class ShowPicTextView extends View {

    private Bitmap mBitmap;
    private String mText;
    private static Paint mPaint;
    private Rect mSrcRect;
    private RectF mDestRect;
    private boolean mIsNeedRestRects;

    public ShowPicTextView(Context context) {
        super(context);
        setBackgroundColor(Color.TRANSPARENT);
        mIsNeedRestRects = false;
    }

    public ShowPicTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);
        mIsNeedRestRects = false;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        mIsNeedRestRects = true;
        postInvalidate();
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setText(String text) {
        mText = text;
        postInvalidate();
    }

    private void resetRects() {
        if (mBitmap != null) {
            int bmpWidth = mBitmap.getWidth();
            int bmpHeight = mBitmap.getHeight();
            mSrcRect = new Rect(0, 0, bmpWidth, bmpHeight);
            float ratio1 = getWidth() * 1.0f / bmpWidth;
            float ratio2 = getHeight() * 1.0f / bmpHeight;
            if (ratio1 > ratio2) {
                float detalWidth = (getWidth() - bmpWidth * ratio2) / 2;
                mDestRect = new RectF(detalWidth, 0, getWidth() - detalWidth, getHeight());
            } else {
                float detalHeight = (getHeight() - bmpHeight * ratio1) / 2;
                mDestRect = new RectF(0, detalHeight, getWidth(), getHeight() - detalHeight);
            }
        }
    }

    private Paint getPaint() {
        if (mPaint == null) {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            int textSize = Utils.dip2px(getContext(), 15);
            mPaint.setTextSize(textSize);
            mPaint.setColor(Color.WHITE);
            mPaint.setTextAlign(Paint.Align.LEFT);
        }
        return mPaint;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBitmap != null && !mBitmap.isRecycled()) {
            if (mIsNeedRestRects || mSrcRect == null || mDestRect == null) {
                resetRects();
                mIsNeedRestRects = false;
            }
            canvas.drawBitmap(mBitmap, mSrcRect, mDestRect, null);
        }
        if (mText != null && mText.trim().length() > 0) {
            canvas.drawText(mText, 0, getHeight() / 2, getPaint());
        }

    }
}
