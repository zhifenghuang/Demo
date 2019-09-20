package com.hzf.demo.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.hzf.demo.R;

/**
 * Created by gigabud on 17-3-7.
 */

/**
 * 按比例的ImageView，以宽为基数
 */
@SuppressLint("AppCompatCustomView")
public class RatioImageView extends ImageView {

    private float mRatio;

    public RatioImageView(Context context) {
        super(context);
    }

    public RatioImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatioImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ImageViewRatio);
            mRatio = array.getFloat(R.styleable.ImageViewRatio_ratio, 1.0f);
            array.recycle();
        }
        if (mRatio <= 0.0f) {
            mRatio = 1.0f;
        }
    }

    public void setRatio(float ratio) {
        mRatio = ratio;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
        int childWidthSize = getMeasuredWidth();
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (childWidthSize * mRatio + 0.5f), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
