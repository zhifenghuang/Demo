package com.hzf.demo.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by gigabud on 16-3-29.
 */
public class CustomViewPager extends ViewPager {

    private boolean isCanScroll = true;
    private boolean isEditState = false;
    private boolean isSetXLimit = false;
    private int maxLimitX;
    private boolean isFingerUp;

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCanScroll(boolean isCanScroll) {
        this.isCanScroll = isCanScroll;
    }

    public boolean isCanScroll() {
        return isCanScroll;
    }

    public void setEditState(boolean isEditState) {
        this.isEditState = isEditState;
    }

    public boolean getEditState() {
        return isEditState;
    }

    public OnViewPagerTouch mOnViewPagerTouch;

    public void setScrollLimit(boolean isSetXLimit, int maxLimitX) {
        this.isSetXLimit = isSetXLimit;
        this.maxLimitX = maxLimitX;
    }

    @Override
    public void scrollTo(int x, int y) {
        if (isSetXLimit) {
            x = Math.min(x, maxLimitX);
            if (isFingerUp) {
                setCurrentItem(0, true);
            }
        }
        super.scrollTo(x, y);
    }

    @Override
    public void setCurrentItem(int item) {
        int currentItem = getCurrentItem();
        if (item != currentItem) {
            super.setCurrentItem(item);
        }
    }

    public void setOnViewPagerTouch(OnViewPagerTouch onViewPagerTouch) {
        mOnViewPagerTouch = onViewPagerTouch;
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        if (isCanScroll) {
            if (mOnViewPagerTouch != null) {
                mOnViewPagerTouch.onTouch(arg0);
            }
            if (isSetXLimit) {
                isFingerUp = arg0.getAction() == MotionEvent.ACTION_UP;
            }
            return super.onTouchEvent(arg0);
        } else {
            return false;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (isCanScroll) {
            return super.onInterceptTouchEvent(arg0);
        } else {
            return false;
        }
    }


    public interface OnViewPagerTouch {
        public void onTouch(MotionEvent event);
    }

}
