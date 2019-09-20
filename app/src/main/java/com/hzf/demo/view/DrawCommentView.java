package com.hzf.demo.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;
import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.core.models.enums.MediaType;
import com.giphy.sdk.core.network.api.CompletionHandler;
import com.giphy.sdk.core.network.api.GPHApi;
import com.giphy.sdk.core.network.api.GPHApiClient;
import com.giphy.sdk.core.network.response.ListMediaResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hzf.demo.BaseApplication;
import com.hzf.demo.R;
import com.hzf.demo.activity.BaseActivity;
import com.hzf.demo.adapter.EmoAdapter;
import com.hzf.demo.adapter.GifAdapter;
import com.hzf.demo.fragment.BaseFragment;
import com.hzf.demo.fragment.EditPostFragment;
import com.hzf.demo.utils.Constants;
import com.hzf.demo.utils.Preferences;
import com.hzf.demo.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by gigabud on 17-1-17.
 */

public class DrawCommentView extends RelativeLayout implements View.OnClickListener {

    private Bitmap mCommentsBmp;
    private int mLastDrawColor = -1, mLastTextColor = Color.WHITE;
    private BaseFragment mBaseFragment;
    private WarterMarkView mWarterMarkView;
    private boolean mIsWartmarkShowCenter;
    private String mWaterMarkText;
    private ArrayList<View> mEmoViewList;
    private boolean isAnimation = false;
    private InputMethodManager mInputMethodManager;

    public static final int TYPE_COMMON_PEN = 0;
    public static final int TYPE_GLOW_PEN = 1;
    private int mCurrentPenType = TYPE_COMMON_PEN;

    public static final int TYPE_PEN_WIDTH_4 = 0;
    public static final int TYPE_PEN_WIDTH_10 = 1;
    public static final int TYPE_PEN_WIDTH_16 = 2;
    public static final int TYPE_PEN_WIDTH_22 = 3;
    private int mCurrentPenWidthType = TYPE_PEN_WIDTH_4;

    public DrawCommentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_draw_comment, this);
        findViewById(R.id.btnClose).setOnClickListener(this);
        findViewById(R.id.btnRemove).setOnClickListener(this);
        findViewById(R.id.btnRemove).setVisibility(View.GONE);
        findViewById(R.id.topView).bringToFront();
        findViewById(R.id.btnPen).setOnClickListener(this);
        findViewById(R.id.btnGlowPen).setOnClickListener(this);
        findViewById(R.id.btnWartMark).setOnClickListener(this);
        findViewById(R.id.btnSticker).setOnClickListener(this);
        findViewById(R.id.warterMarkParentView).setOnClickListener(this);
        findViewById(R.id.btnGlowPen).setVisibility(View.GONE);
        findViewById(R.id.llSelectPenWidth).setVisibility(View.GONE);
        findViewById(R.id.btnGlowPen).setVisibility(View.GONE);
        findViewById(R.id.llSelectPenWidth).setVisibility(View.GONE);

        final DrawView drawView = findViewById(R.id.drawView);
        drawView.setEnable(false);
        findViewById(R.id.ivColor).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_UP:
                        Bitmap bmp = ((BitmapDrawable) ((ImageView) v).getDrawable()).getBitmap();
                        int x = bmp.getWidth() / 2;
                        int y = (int) (event.getY() * bmp.getHeight() / v.getHeight() + 0.5f);
                        if (x >= bmp.getWidth()) {
                            x = bmp.getWidth() - 1;
                        } else if (x < 0) {
                            x = 0;
                        }
                        if (y >= bmp.getHeight()) {
                            y = bmp.getHeight() - 1;
                        } else if (y < 0) {
                            y = 0;
                        }
                        int color = bmp.getPixel(x, y);
                        if (color != Color.TRANSPARENT) {
                            drawView.resetPaintColor(color);
                            mLastDrawColor = color;
                            if (mCurrentPenType == TYPE_GLOW_PEN) {
                                GradientDrawable bgShape = (GradientDrawable) findViewById(R.id.btnGlowPenBg).getBackground();
                                bgShape.setColor(color);
                            } else {
                                GradientDrawable bgShape = (GradientDrawable) findViewById(R.id.btnPenBg).getBackground();
                                bgShape.setColor(color);
                            }
                        }
                        break;
                }
                return true;
            }
        });
        findViewById(R.id.ivTextColor).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_UP:
                        Bitmap bmp = ((BitmapDrawable) ((ImageView) v).getDrawable()).getBitmap();
                        int x = bmp.getWidth() / 2;
                        int y = (int) (event.getY() * bmp.getHeight() / v.getHeight() + 0.5f);
                        if (x >= bmp.getWidth()) {
                            x = bmp.getWidth() - 1;
                        } else if (x < 0) {
                            x = 0;
                        }
                        if (y >= bmp.getHeight()) {
                            y = bmp.getHeight() - 1;
                        } else if (y < 0) {
                            y = 0;
                        }
                        int color = bmp.getPixel(x, y);
                        if (color != Color.TRANSPARENT) {
                            mLastTextColor = color;
                            getEtPost().setTextColor(color);
                        }
                        break;
                }
                return true;
            }
        });
        drawView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (drawView.isEnable()) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            showOrHideWithAnim(findViewById(R.id.topView), false);
                            break;
                        case MotionEvent.ACTION_UP:
                            showOrHideWithAnim(findViewById(R.id.topView), true);
                            findViewById(R.id.btnRemove).setVisibility(drawView.isDrawed() ? View.VISIBLE : View.GONE);
                            break;
                    }
                }
                return false;
            }
        });

        initEditTextPost();
        onEditKeyListener(getEtPost());
    }

    private void onEditKeyListener(EditText et) {
        et.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                        case KeyEvent.KEYCODE_DPAD_UP:
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            return true;
                    }
                }
                return false;
            }
        });
    }

    public DrawCommentView(Context context) {
        this(context, null);
    }

    public void setParentFragment(BaseFragment fragment) {
        mBaseFragment = fragment;
        DrawView drawView = findViewById(R.id.drawView);
        initSelectPenWidthView((SelectPenWidthView) findViewById(R.id.penWidth_8), TYPE_PEN_WIDTH_4, drawView.getStrokeWidth(TYPE_PEN_WIDTH_4) + Utils.dip2px(getContext(), 2));
        initSelectPenWidthView((SelectPenWidthView) findViewById(R.id.penWidth_12), TYPE_PEN_WIDTH_10, drawView.getStrokeWidth(TYPE_PEN_WIDTH_10));
        initSelectPenWidthView((SelectPenWidthView) findViewById(R.id.penWidth_16), TYPE_PEN_WIDTH_16, drawView.getStrokeWidth(TYPE_PEN_WIDTH_16));
        initSelectPenWidthView((SelectPenWidthView) findViewById(R.id.penWidth_20), TYPE_PEN_WIDTH_22, drawView.getStrokeWidth(TYPE_PEN_WIDTH_22));
        findViewById(R.id.topView).bringToFront();
    }

    private void initSelectPenWidthView(SelectPenWidthView selectPenWidthView, int penWidthType, int penWidth) {
        selectPenWidthView.setOnClickListener(this);
        selectPenWidthView.setTag(penWidthType);
        selectPenWidthView.setRadius(penWidth);

    }


    private void resetSelectPenWidthView() {
        LinearLayout ll = findViewById(R.id.llSelectPenWidth);
        int childCount = ll.getChildCount();
        SelectPenWidthView selectPenWidthView;
        for (int i = 0; i < childCount; ++i) {
            selectPenWidthView = (SelectPenWidthView) ll.getChildAt(i);
            selectPenWidthView.setSelected((int) selectPenWidthView.getTag() == mCurrentPenWidthType);
        }
    }


    @Override
    public void onClick(View v) {
//        if (((EditPostFragment) mBaseFragment).getHandleRelativeLayout().isInEnableGPSPage()) {
//            return;
//        }
        switch (v.getId()) {
            case R.id.btnClose:
                removeDrawCommentView();
                break;
            case R.id.btnSticker:
                showStickersDialog();
                break;
            case R.id.penWidth_8:
            case R.id.penWidth_12:
            case R.id.penWidth_16:
            case R.id.penWidth_20:
                int penWidthType = (int) v.getTag();
                mCurrentPenWidthType = penWidthType;
                ((DrawView) findViewById(R.id.drawView)).setCurrentPenWidthType(penWidthType);
                resetSelectPenWidthView();
                break;
            case R.id.btnGlowPen:
                if (findViewById(R.id.reGlowPen).getVisibility() == View.VISIBLE && mCurrentPenType == TYPE_GLOW_PEN) {
                    showOrHidePenWithViewAndColorViewWithAnimation();
                    return;
                }
                mCurrentPenType = TYPE_GLOW_PEN;
                if (mLastDrawColor == -1) {
                    mLastDrawColor = Color.rgb(30, 161, 66);
                }
                showViewByCurrentPen();
                break;
            case R.id.btnPen:
                if (findViewById(R.id.reGlowPen).getVisibility() == View.VISIBLE && mCurrentPenType == TYPE_COMMON_PEN) {
                    showOrHidePenWithViewAndColorViewWithAnimation();
                    return;
                }
                mCurrentPenType = TYPE_COMMON_PEN;
                if (mLastDrawColor == -1) {
                    mLastDrawColor = Color.rgb(30, 161, 66);
                }
                showViewByCurrentPen();
                break;
            case R.id.warterMarkParentView:
                if (((DrawView) findViewById(R.id.drawView)).isEnable()) {
                    return;
                }
                if (!isPostEditViewVisibility()) {
                    findViewById(R.id.btnWartMark).performClick();
                }
                break;
            case R.id.btnWartMark:
                if (((DrawView) findViewById(R.id.drawView)).isEnable()) {
                    return;
                }
                if (!isPostEditViewVisibility()) {
                    if (isEmoViewVisibility()) {
                        hideEmoView();
                    }
                    removeMarkImageView();
                    getPostEditView().setVisibility(View.VISIBLE);
                    ((ImageView) findViewById(R.id.btnClose)).setImageDrawable(getResources().getDrawable(R.drawable.edit_back));
                    if (mWaterMarkText == null) {
                        mWaterMarkText = "";
                    }
                    getEtPost().setText(mWaterMarkText);
                    getEtPost().setTextSize(WarterMarkView.TEXT_SIZE);
                    getEtPost().setSelection(mWaterMarkText.length());
                    getEtPost().setTextColor(mLastTextColor);
                    getEtPost().setFocusable(true);
                    getEtPost().setFocusableInTouchMode(true);
                    getEtPost().requestFocus();
                    showOrHideSoftKey(true);
                    getEtPost().setMaxLines(100);
                    getEtPost().setHint("");
                    getEtPost().setTypeface(Typeface.DEFAULT_BOLD);
                    RelativeLayout.LayoutParams rl = (RelativeLayout.LayoutParams) getEtPost().getLayoutParams();
                    rl.height = getHeight() / 2;
                    getEtPost().requestLayout();
                    rl.topMargin = Utils.dip2px(getContext(), 30);
                    if (mIsWartmarkShowCenter) {
                        getEtPost().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
                    } else {
                        getEtPost().setGravity(Gravity.LEFT | Gravity.BOTTOM);
                    }
                    findViewById(R.id.rlDrawColor).setVisibility(View.GONE);
                    findViewById(R.id.btnPen).setVisibility(View.INVISIBLE);
                    findViewById(R.id.btnSticker).setVisibility(View.INVISIBLE);
                    findViewById(R.id.ivTextColor).setVisibility(View.VISIBLE);
                    findViewById(R.id.ivTextColorBg).setVisibility(View.VISIBLE);
                    getTopCoverView().setVisibility(View.VISIBLE);
                    findViewById(R.id.btnPenCoverView).setVisibility(View.VISIBLE);
                    findViewById(R.id.btnStickerCoverView).setVisibility(View.VISIBLE);
                    findViewById(R.id.btnWartMarkCoverView).setVisibility(View.INVISIBLE);
                } else {
                    mIsWartmarkShowCenter = !mIsWartmarkShowCenter;
                    if (mIsWartmarkShowCenter) {
                        getEtPost().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
                    } else {
                        getEtPost().setGravity(Gravity.LEFT | Gravity.BOTTOM);
                    }
                }
                break;
            case R.id.btnRemove:
                DrawView drawView = findViewById(R.id.drawView);
                drawView.unDoDraw();
                v.setVisibility(drawView.isDrawPathsListEmpty() ? View.GONE : View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private void showOrHidePenWithViewAndColorViewWithAnimation() {
        final View llSelectPenWidth = findViewById(R.id.llSelectPenWidth);
        final View rlDrawColor = findViewById(R.id.rlDrawColor);
        Animation animation;
        if (llSelectPenWidth.getVisibility() == View.VISIBLE) {
            animation = AnimationUtils.loadAnimation(getContext(), R.anim.scale_right_out);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    llSelectPenWidth.setVisibility(View.GONE);
                    rlDrawColor.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            llSelectPenWidth.startAnimation(animation);
            rlDrawColor.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.scale_top_out));
        } else {
            llSelectPenWidth.setVisibility(View.VISIBLE);
            rlDrawColor.setVisibility(View.VISIBLE);
            rlDrawColor.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.scale_top_in));
        }
    }

    private void showViewByCurrentPen() {
        ((ImageView) findViewById(R.id.btnClose)).setImageDrawable(getResources().getDrawable(R.drawable.edit_back));
        ((WarterMarkParentView) findViewById(R.id.warterMarkParentView)).setTouchEnable(false);
        DrawView drawView = findViewById(R.id.drawView);
        drawView.setCurrentPenType(mCurrentPenType);
        findViewById(R.id.btnRemove).setVisibility(drawView.isDrawPathsListEmpty() ? View.GONE : View.VISIBLE);
        drawView.setEnable(true);
        findViewById(R.id.reGlowPen).setVisibility(View.VISIBLE);
        findViewById(R.id.btnGlowPen).setVisibility(View.VISIBLE);
        resetSelectPenWidthView();
        drawView.setCurrentPenWidthType(mCurrentPenWidthType);
        drawView.resetPaintColor(mLastDrawColor);
        if (mCurrentPenType == TYPE_GLOW_PEN) {
            findViewById(R.id.btnPenBg).setVisibility(View.GONE);
            findViewById(R.id.btnGlowPenBg).setVisibility(View.VISIBLE);
            GradientDrawable bgShape = (GradientDrawable) findViewById(R.id.btnGlowPenBg).getBackground();
            bgShape.setColor(mLastDrawColor);
        } else {
            findViewById(R.id.btnSticker).setVisibility(View.GONE);
            findViewById(R.id.reText).setVisibility(View.GONE);
            getTopCoverView().setVisibility(View.GONE);
            findViewById(R.id.btnPenBg).setVisibility(View.VISIBLE);
            findViewById(R.id.btnGlowPenBg).setVisibility(View.GONE);
            GradientDrawable bgShape = (GradientDrawable) findViewById(R.id.btnPenBg).getBackground();
            bgShape.setColor(mLastDrawColor);
        }
        if (findViewById(R.id.rlDrawColor).getVisibility() == View.GONE) {
            showOrHidePenWithViewAndColorViewWithAnimation();
        }
    }

    public boolean isPostEditViewVisibility() {
        return getPostEditView().getVisibility() == View.VISIBLE;
    }

    public View getPostEditView() {
        return findViewById(R.id.postEditView);
    }

    protected EditText getEtPost() {
        return (EditText) findViewById(R.id.etPost);
    }


    public void initEditTextPost() {
        getEtPost().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                        case KeyEvent.KEYCODE_DPAD_UP:
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            return true;
                    }
                }
                return false;
            }
        });
    }

    protected void showView() {
        postEditView();
    }

    public void postEditView() {
        if (isPostEditViewVisibility()) {
            getPostEditView().setVisibility(View.GONE);
            findViewById(R.id.btnPen).setVisibility(View.VISIBLE);
            findViewById(R.id.btnWartMark).setVisibility(View.VISIBLE);
            findViewById(R.id.btnSticker).setVisibility(View.VISIBLE);
            findViewById(R.id.ivTextColor).setVisibility(View.GONE);
            findViewById(R.id.ivTextColorBg).setVisibility(View.GONE);
            getTopCoverView().setVisibility(View.GONE);

            mWaterMarkText = getEtPost().getText().toString();
            addWatermarkImageView(mWaterMarkText, mLastTextColor,
                    findViewById(R.id.btnWartMark), getPostEditView(), mIsWartmarkShowCenter);
            EditPostFragment editPostFragment = (EditPostFragment) mBaseFragment;
            editPostFragment.getHandleRelativeLayout().setCanScroll(true);
            ((ImageView) findViewById(R.id.btnClose)).setImageDrawable(getResources().getDrawable(R.drawable.edit_close));
        }
        showOrHideSoftKey(false);
    }

    private void showOrHideSoftKey(boolean isShow) {
        if (isShow) {
            getEtPost().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getInputMethodManager().toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }, 50);
        } else {
            if (getInputMethodManager().isActive()) {
                getInputMethodManager().hideSoftInputFromWindow(getEtPost().getWindowToken(), 0);
            }
        }
    }

    private InputMethodManager getInputMethodManager() {
        if (mInputMethodManager == null) {
            mInputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        return mInputMethodManager;
    }


    private void showOrHideWithAnim(View view, boolean isShow) {
        AlphaAnimation animation;
        if (isShow) {
            view.setVisibility(View.VISIBLE);
            animation = new AlphaAnimation(0, 1);
        } else {
            view.setVisibility(View.GONE);
            animation = new AlphaAnimation(1, 0);
        }
        animation.setDuration(500);//设置动画持续时间
        view.setAnimation(animation);
        animation.start();
    }

    private boolean isPenViewShow() {
        return findViewById(R.id.reGlowPen).getVisibility() == View.VISIBLE;
    }

    private void hidePenView() {
        ((WarterMarkParentView) findViewById(R.id.warterMarkParentView)).setTouchEnable(true);
        ((DrawView) findViewById(R.id.drawView)).setEnable(false);
        findViewById(R.id.llSelectPenWidth).setVisibility(View.GONE);
        findViewById(R.id.rlDrawColor).setVisibility(View.GONE);
        findViewById(R.id.btnPenBg).setVisibility(View.GONE);
        findViewById(R.id.reGlowPen).setVisibility(View.GONE);
        findViewById(R.id.btnRemove).setVisibility(View.GONE);
        findViewById(R.id.btnSticker).setVisibility(View.VISIBLE);
        findViewById(R.id.reText).setVisibility(View.VISIBLE);
        ((ImageView) findViewById(R.id.btnPen)).setImageDrawable(getResources().getDrawable(R.drawable.edit_pen));
        ((ImageView) findViewById(R.id.btnClose)).setImageDrawable(getResources().getDrawable(R.drawable.edit_close));
    }


    /**
     * 判断是否能退出
     *
     * @param
     */
    public void removeDrawCommentView() {
        if (isPenViewShow()) {
            hidePenView();
            return;
        }
        if (isPostEditViewVisibility()) {
            showView();
            return;
        }
        if (isEmoViewVisibility()) {
            hideEmoView();
            return;
        }
        recycleSource();
        mBaseFragment.goBack();
    }

    /**
     * 显示或隐藏表贴图页
     */
    private void showStickersDialog() {
        if (isEmoViewVisibility()) {
            hideEmoView();
            return;
        }
        showEmoView();
        initViewPager();
    }

    public boolean isEmoViewVisibility() {
        return getEmoView().getVisibility() == View.VISIBLE;
    }

    /**
     * 隐藏贴图页
     */
    public void hideEmoView() {
        if (isAnimation) {
            return;
        }
        isAnimation = true;
        ObjectAnimator animator = ObjectAnimator.ofFloat(getEmoView(), "translationY", 0.0F, getMeasuredHeight());
        animator.setDuration(500).start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                getEmoView().setVisibility(View.GONE);
                ((ImageView) findViewById(R.id.btnClose)).setImageDrawable(getResources().getDrawable(R.drawable.edit_close));
                getEmoView().clearAnimation();
                isAnimation = false;
            }
        });
        getTopCoverView().setVisibility(View.GONE);
        findViewById(R.id.btnPenCoverView).setVisibility(View.GONE);
        findViewById(R.id.ivTextColor).setVisibility(View.GONE);
        findViewById(R.id.ivTextColorBg).setVisibility(View.GONE);
        findViewById(R.id.btnWartMarkCoverView).setVisibility(View.GONE);
        findViewById(R.id.btnPen).setVisibility(View.VISIBLE);
        findViewById(R.id.btnWartMark).setVisibility(View.VISIBLE);
        findViewById(R.id.btnPen).setEnabled(true);
        findViewById(R.id.btnWartMark).setEnabled(true);
    }


    /**
     * 显示贴图页
     */
    private void showEmoView() {
        if (isAnimation) {
            return;
        }
        isAnimation = true;
        findViewById(R.id.btnPen).setEnabled(false);
        findViewById(R.id.btnWartMark).setEnabled(false);
        findViewById(R.id.rlDrawColor).setVisibility(View.GONE);
        getEmoView().setVisibility(View.VISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(getEmoView(), "translationY", getMeasuredHeight(), 0.0F);
        animator.setDuration(500).start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                getEmoView().clearAnimation();
                isAnimation = false;
                getTopCoverView().setVisibility(View.VISIBLE);
                findViewById(R.id.btnPenCoverView).setVisibility(View.VISIBLE);
                findViewById(R.id.btnWartMarkCoverView).setVisibility(View.VISIBLE);
                findViewById(R.id.btnPen).setVisibility(View.INVISIBLE);
                findViewById(R.id.btnWartMark).setVisibility(View.INVISIBLE);

            }
        });
        ((ImageView) findViewById(R.id.btnClose)).setImageDrawable(getResources().getDrawable(R.drawable.edit_back));
    }

    private GPHApi mClient;
    private long mUpFingerTime;

    private GPHApi getGPHApi() {
        if (mClient == null) {
            mClient = new GPHApiClient("PEn771J99GQwE5MG6JjZUyV8HNjMOtFY");
        }
        return mClient;
    }

    /**
     * 添加贴图页
     */
    private void initViewPager() {
        if (mEmoViewList == null) {
            mEmoViewList = new ArrayList<>();
        }
        if (mEmoViewList.isEmpty()) {
            for (int i = 0; i < Constants.STRICK_NAMES.length + 1; ++i) {
                mEmoViewList.add(LayoutInflater.from(getContext()).inflate(R.layout.layout_stickers_gridview, null));
            }
            getStickerView().setAdapter(new PagerAdapter() {
                @Override
                public boolean isViewFromObject(View arg0, Object arg1) {
                    return arg0 == arg1;
                }

                @Override
                public int getCount() {
                    return mEmoViewList.size();
                }

                @Override
                public void destroyItem(ViewGroup container, int position, Object object) {
                    container.removeView(mEmoViewList.get(position));
                }

                @Override
                public CharSequence getPageTitle(int position) {
                    return "";
                }

                @Override
                public Object instantiateItem(ViewGroup container, int position) {
                    final View parentView = mEmoViewList.get(position);
                    container.addView(parentView);
                    GridView gridView = parentView.findViewById(R.id.gridView);
                    if (position == 0) {
                        parentView.findViewById(R.id.llSearchGif).setVisibility(View.VISIBLE);
                        if (gridView.getAdapter() == null) {
                            final GifAdapter gifAdapter = new GifAdapter(getContext(), 3);
                            gridView.setAdapter(gifAdapter);
                            gridView.setNumColumns(3);
                            String urls = Preferences.getInstacne(getContext()).getValues("gif_urls", "");
                            if (!TextUtils.isEmpty(urls)) {
                                try {
                                    List<Media> list = new Gson().fromJson(urls, new TypeToken<List<Media>>() {
                                    }.getType());
                                    resetGifAdapter(gifAdapter, list);
                                }catch (Exception e){
                                    getGPHApi().trending(MediaType.sticker, 100, null, null, new CompletionHandler<ListMediaResponse>() {
                                        @Override
                                        public void onComplete(ListMediaResponse result, Throwable e) {
                                            resetGifAdapter(gifAdapter, result);
                                        }
                                    });
                                }
                            } else {
                                getGPHApi().trending(MediaType.sticker, 100, null, null, new CompletionHandler<ListMediaResponse>() {
                                    @Override
                                    public void onComplete(ListMediaResponse result, Throwable e) {
                                        resetGifAdapter(gifAdapter, result);
                                    }
                                });
                            }
                            resetSearchEditText(gifAdapter, (EditText) parentView.findViewById(R.id.etSearch));
                            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                    if (position >= gifAdapter.getGifCount()) {
                                        return;
                                    }
                                    final String gifUrl = gifAdapter.getItem(position);
                                    if (TextUtils.isEmpty(gifUrl)) {
                                        return;
                                    }
                                    final int width = view.getWidth();
                                    final int height = view.getHeight();
                                    final int centerX = view.getLeft() + width / 2;
                                    final int centerY = view.getTop() + Utils.dip2px(getContext(), 73)
                                            + height / 2 + parentView.findViewById(R.id.llSearchGif).getHeight();
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            final File file = getGifFile(gifUrl, Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
                                            if (file != null && file.exists()) {
                                                post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        hideEmoView();
                                                        ((BaseActivity) getContext()).hideLoadingDialog();
                                                        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(parentView.findViewById(R.id.etSearch).getWindowToken(), 0);
                                                        addGifEmo(gifUrl, file, position + 1,
                                                                centerX, centerY, width, height, findViewById(R.id.btnSticker));
                                                    }
                                                });
                                            }
                                        }
                                    }).start();

                                }
                            });


                        }
                    } else {
                        parentView.findViewById(R.id.llSearchGif).setVisibility(View.GONE);
                        final String[] resArrayList = Constants.STRICK_NAMES[position - 1];
                        if (position == 1 || position == 2) {
                            gridView.setNumColumns(2);
                        }
                        if (gridView.getAdapter() == null) {
                            final EmoAdapter emoAdapter = new EmoAdapter(getContext(), resArrayList, (position == 1 || position == 2) ? 2 : 5);
                            gridView.setAdapter(emoAdapter);
                            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    if (position >= resArrayList.length) {
                                        return;
                                    }
                                    hideEmoView();
                                    int width = view.getWidth();
                                    int height = view.getHeight();
                                    int centerX = view.getLeft() + width / 2;
                                    int centerY = view.getTop() + Utils.dip2px(getContext(), 73) + height / 2;
                                    addEmo(resArrayList[position], Utils.getDrawableIdByName(resArrayList[position]),
                                            position + 1, centerX, centerY, width, height, findViewById(R.id.btnSticker));
                                }
                            });
                        }
                    }
                    return mEmoViewList.get(position);
                }
            });
            getStickerView().setCurrentItem(0);
        }
    }

    /**
     * 添加表情
     *
     * @param gifUrl
     * @param index
     * @param centerX
     * @param centerY
     * @param width
     * @param btnStricker
     */
    private void addGifEmo(String gifUrl, File gifFile, int index, int centerX, int centerY, int width, int height, View btnStricker) {
        StickerView stickerView = new StickerView(getContext());
        try {
            GifDrawable gifDrawable = new GifDrawable(gifFile);
            stickerView.setImageDrawable(gifDrawable);
            Bitmap bmp = gifDrawable.seekToFrameAndGet(0);
            stickerView.setGifWaterMark(bmp, gifUrl, centerX, centerY,
                    Math.min(width * 1.0f / bmp.getWidth(), height * 1.0f / bmp.getHeight()), (ImageButton) btnStricker);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        ((RelativeLayout) findViewById(R.id.strickerParentView)).addView(stickerView);
        stickerView.setTag(StickerView.GIF_STICKER);
        stickerView.setDeleteStickerListener(new StickerView.DeleteStickerListener() {
            @Override
            public void deleteSticker(StickerView stickerView) {
                if (stickerView.getParent() != null) {
                    ((RelativeLayout) findViewById(R.id.strickerParentView)).removeView(stickerView);
                }
            }
        });
    }

    /**
     * 添加表情
     *
     * @param name
     * @param selectedEmoId
     * @param index
     * @param centerX
     * @param centerY
     * @param width
     * @param height
     * @param btnStricker
     */
    private void addEmo(String name, int selectedEmoId, int index, int centerX, int centerY, int width, int height, View btnStricker) {
        StickerView stickerView = new StickerView(getContext());
        Bitmap emoBmp;
        if (name.startsWith("svg_")) {
            emoBmp = getBitmapFromVectorDrawable(selectedEmoId);
        } else {
            emoBmp = BitmapFactory.decodeResource(getResources(), selectedEmoId);
        }
        stickerView.setWaterMark(emoBmp, name, index, centerX, centerY, width * 1.0f / emoBmp.getWidth(), (ImageButton) btnStricker);
        ((RelativeLayout) findViewById(R.id.strickerParentView)).addView(stickerView);
        stickerView.setTag(StickerView.EMO_STICKER);
        stickerView.setDeleteStickerListener(new StickerView.DeleteStickerListener() {
            @Override
            public void deleteSticker(StickerView stickerView) {
                if (stickerView.getParent() != null) {
                    ((RelativeLayout) findViewById(R.id.strickerParentView)).removeView(stickerView);
                }
            }
        });
    }

    private File getGifFile(String url, int width, int height) {
        FutureTarget<File> future = Glide.with(BaseApplication.getAppContext())
                .load(url).downloadOnly(width, height);
        try {
            File cacheFile = future.get();
            return new File(cacheFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap getBitmapFromVectorDrawable(int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }


    private void resetSearchEditText(final GifAdapter gifAdapter, final EditText editText) {
        editText.setHint("搜索GIPHY");
        editText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    resetGifByKeyWord(gifAdapter, editText.getText().toString());
                    return true;
                }
                return false;
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mUpFingerTime = System.currentTimeMillis();
                editText.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        resetGifByKeyWord(gifAdapter, editText.getText().toString());
                    }
                }, 1000);
            }
        });
    }

    private void resetGifByKeyWord(final GifAdapter gifAdapter, String keyWord) {
        if (System.currentTimeMillis() - mUpFingerTime < 1000) {  //输入间隔太紧凑不搜索
            return;
        }
        mUpFingerTime = System.currentTimeMillis();
        if (TextUtils.isEmpty(keyWord.trim())) {
            getGPHApi().trending(MediaType.sticker, 100, null, null, new CompletionHandler<ListMediaResponse>() {
                @Override
                public void onComplete(ListMediaResponse result, Throwable e) {
                    resetGifAdapter(gifAdapter, result);
                }
            });
            return;
        }
        getGPHApi().search(keyWord, MediaType.sticker, 100, null, null, null, new CompletionHandler<ListMediaResponse>() {
            @Override
            public void onComplete(ListMediaResponse result, Throwable e) {
                resetGifAdapter(gifAdapter, result);
            }
        });
    }


    private void resetGifAdapter(GifAdapter adapter, ListMediaResponse result) {
        if (result == null) {
            adapter.setDataList(null);
        } else {
            if (result.getData() != null && !result.getData().isEmpty()) {
                Preferences.getInstacne(getContext()).setValues("gif_urls", new Gson().toJson(result.getData()));
                adapter.setDataList(result.getData());
            } else {
                adapter.setDataList(null);
            }

        }
    }

    private void resetGifAdapter(GifAdapter adapter, List<Media> list) {
        adapter.setDataList(list);
    }


    public boolean isHadGifSticker() {
        RelativeLayout strickerParentView = findViewById(R.id.strickerParentView);
        int count = strickerParentView.getChildCount();
        View child;
        for (int i = 0; i < count; ++i) {
            child = strickerParentView.getChildAt(i);
            if (child.getTag() != null && (int) child.getTag() == StickerView.GIF_STICKER) {
                return true;
            }
        }
        return false;
    }


    /**
     * 添加水印
     *
     * @param postText
     * @param textColor
     * @param btnWartMark
     * @param editPostView
     * @param isTextCenter
     */
    public void addWatermarkImageView(String postText, int textColor, final View btnWartMark, final View editPostView, boolean isTextCenter) {
        if (TextUtils.isEmpty(postText)) {
            return;
        }
        removeMarkImageView();
        mWarterMarkView = new WarterMarkView(getContext());
        mWarterMarkView.setPostText(postText, textColor, isTextCenter);
        ((RelativeLayout) findViewById(R.id.warterMarkParentView)).addView(mWarterMarkView);
        mWarterMarkView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editPostView.getVisibility() != View.VISIBLE) {
                    btnWartMark.performClick();
                }
            }
        });
        mWarterMarkView.setTag(StickerView.TEXT_WARTERMARK);
    }

    /**
     * 移除水印
     */
    public void removeMarkImageView() {
        if (mWarterMarkView != null && mWarterMarkView.getParent() != null) {
            ((RelativeLayout) findViewById(R.id.warterMarkParentView)).removeView(mWarterMarkView);
        }
        if (mWarterMarkView != null) {
            mWarterMarkView.recycleBmp();
        }
        mWarterMarkView = null;
    }

    private ViewPager getStickerView() {
        return (ViewPager) findViewById(R.id.stickerView);
    }


    private View getTopCoverView() {
        return findViewById(R.id.rlCoverView);
    }

    public ShowPicTextView getShowPicTextView() {
        return (ShowPicTextView) findViewById(R.id.showPicTv);
    }

    private View getEmoView() {
        return findViewById(R.id.emoView);
    }

    /**
     * 将这个View上绘制的东西保存为Bmp
     *
     * @param glBmp
     */
    public void savePhotoToAlbum(Bitmap glBmp, View handleView) {
        DrawView drawView = findViewById(R.id.drawView);
        getShowPicTextView().setBitmap(glBmp);
        Bitmap bitmap = Bitmap.createBitmap(drawView.getWidth(), drawView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        findViewById(R.id.rlDrawWaterMarkView).draw(canvas);
        handleView.draw(canvas);
        int padding = Utils.dip2px(getContext(), 7);
        Bitmap wartMark = BitmapFactory.decodeResource(getResources(), R.drawable.white_water_mark);
        canvas.drawBitmap(wartMark, bitmap.getWidth() - wartMark.getWidth() - padding, bitmap.getHeight() - wartMark.getHeight() - padding, null);
        Utils.savePhotoToAlbum(bitmap, getContext());
        wartMark.recycle();
        bitmap.recycle();
        getShowPicTextView().setBitmap(null);
    }

    public Bitmap getBmpInVideo(View handleView) {
        DrawView drawView = findViewById(R.id.drawView);
        getShowPicTextView().setBitmap(null);
        Bitmap bitmap = Bitmap.createBitmap(drawView.getWidth(), drawView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        findViewById(R.id.rlDrawWaterMarkView).draw(canvas);
        handleView.draw(canvas);
        return bitmap;
    }

    private void recycleSource() {
        DrawView drawView = findViewById(R.id.drawView);
        drawView.recycleBmp();
        ((ImageView) findViewById(R.id.ivCommentsView)).setImageBitmap(null);
        if (mCommentsBmp != null && !mCommentsBmp.isRecycled()) {
            mCommentsBmp.recycle();
        }
        mCommentsBmp = null;
    }

}
