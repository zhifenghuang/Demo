package com.hzf.demo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.hzf.demo.utils.Utils;

/**
 * Created by gigabud on 17-3-30.
 */

public class SelectPenWidthView extends View {
    private Paint mPaint;
    private int mRadius;

    public SelectPenWidthView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private Paint getPain() {
        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setColor(Color.WHITE);
        }
        return mPaint;
    }

    public void setRadius(int radius) {
        mRadius = radius;
    }

    public void setSelected(boolean isSelected) {
        getPain().setStyle(isSelected ? Paint.Style.FILL : Paint.Style.STROKE);
        getPain().setStrokeWidth(isSelected ? 0 : Utils.dip2px(getContext(), 2));
        invalidate();
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mRadius <= 0) {
            return;
        }
        canvas.drawCircle(getWidth() * 0.5f, getHeight() * 0.5f, mRadius*0.5f, getPain());
    }


}
