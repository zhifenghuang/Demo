package com.hzf.demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by gigabud on 17-3-1.
 */

public class WarterMarkParentView extends RelativeLayout {

    private boolean mTouchEnable;
    private boolean mIsPointerInSticker;

    public WarterMarkParentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchEnable = true;
    }

    public void setTouchEnable(boolean touchEnable) {
        mTouchEnable = touchEnable;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!mTouchEnable) {
            return false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mIsPointerInSticker = false;
            int childCount = getChildCount();
            StickerView stickerView;
            for (int i = childCount - 1; i >= 0; --i) {
                stickerView = (StickerView) getChildAt(i);
                mIsPointerInSticker = stickerView.isPointerDownIn(event.getX(), event.getY());
                if (mIsPointerInSticker) {
                    getChildAt(childCount - 1).setFocusable(false);
                    getChildAt(i).setFocusable(true);
                    getChildAt(i).bringToFront();
                    break;
                }
            }
        }
        if (mIsPointerInSticker) {
            return super.dispatchTouchEvent(event);
        } else {
            return false;
        }
    }
}
