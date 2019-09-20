package com.hzf.demo.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.hzf.demo.R;

import java.util.ArrayList;

/**
 * Created by gigabud on 15-12-2.
 * 手动处理滑动的RelativeLayout
 */
public class HandleRelativeLayout extends RelativeLayout {

    private ArrayList<CustomFilterView> mViewList;
    private OnHandleRelativeLayoutEvent mOnHandleRelativeLayoutEvent;
    private boolean mIsViewPagerMove;

    private DisplayMetrics mDisplaymetrics;

    private Typeface mPFTypeFace, mOpenSansBoldTypeface, mOpenSansTypeface;

    private int mTotalFilters;

    private static final int VIEW_PAGER_COUNT = 4;

    private static final int FIX_FILTER_COUNT = 8;


    public interface OnHandleRelativeLayoutEvent {
        void onScroll(int pageNumber, float xOffset);

        void onClick();
    }

    private Typeface getPFTypeFace() {
        if (mPFTypeFace == null) {
            mPFTypeFace = Typeface.createFromAsset(getContext().getAssets(), "fonts/PFDinTextCompPro-Regular.ttf");
        }
        return mPFTypeFace;
    }

    private Typeface getOpenBoldTypeFace() {
        if (mOpenSansBoldTypeface == null) {
            mOpenSansBoldTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Bold.ttf");
        }
        return mOpenSansBoldTypeface;
    }

    private Typeface getOpenTypeFace() {
        if (mOpenSansTypeface == null) {
            mOpenSansTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Regular.ttf");
        }
        return mOpenSansTypeface;
    }

    public HandleRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initViewPager() {
        if (mViewList == null) {
            mViewList = new ArrayList<>();
        }
        mViewList.clear();
        for (int i = 0; i < VIEW_PAGER_COUNT; ++i) {
            CustomFilterView customFilterView = new CustomFilterView(getContext());
            customFilterView.setFilterType(CustomFilterView.FilterType.TYPE_NONE);
            mViewList.add(customFilterView);
        }
        mTotalFilters = FIX_FILTER_COUNT;

        final ViewPager viewPager = getViewPager();
        viewPager.setAdapter(mPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                              @Override
                                              public void onPageSelected(int arg0) {
                                                  int pageNo = arg0 % mTotalFilters;
                                                  CustomFilterView beforeView = mViewList.get((arg0 - 1) % VIEW_PAGER_COUNT);
                                                  CustomFilterView afterView = mViewList.get((arg0 + 1) % VIEW_PAGER_COUNT);
                                                  //                                                 if (mTotalFilters >= FIX_FILTER_COUNT + 1) {
                                                  if (pageNo == 0) {
                                                      beforeView.setFilterType(CustomFilterView.FilterType.TYPE_TEMP);
                                                      beforeView.setText(getPFTypeFace());
                                                      afterView.setFilterType(CustomFilterView.FilterType.TYPE_NONE);
                                                  } else if (pageNo == FIX_FILTER_COUNT - 3) {
                                                      afterView.setFilterType(CustomFilterView.FilterType.TYPE_TIME);
                                                      afterView.setText(getPFTypeFace());
                                                      beforeView.setFilterType(CustomFilterView.FilterType.TYPE_NONE);
                                                  } else if (pageNo == FIX_FILTER_COUNT - 2) {
                                                      afterView.setFilterType(CustomFilterView.FilterType.TYPE_DATE);
                                                      afterView.setText(getPFTypeFace(), getOpenBoldTypeFace(), getOpenTypeFace());
                                                      beforeView.setFilterType(CustomFilterView.FilterType.TYPE_NONE);
                                                  } else if (pageNo == FIX_FILTER_COUNT - 1) {
                                                      afterView.setFilterType(CustomFilterView.FilterType.TYPE_TEMP);
                                                      afterView.setText(getPFTypeFace());
                                                      beforeView.setFilterType(CustomFilterView.FilterType.TYPE_TIME);
                                                      beforeView.setText(getPFTypeFace());
                                                  } else if (pageNo == FIX_FILTER_COUNT) {
                                                      beforeView.setFilterType(CustomFilterView.FilterType.TYPE_DATE);
                                                      beforeView.setText(getPFTypeFace(), getOpenBoldTypeFace(), getOpenTypeFace());
                                                  } else if (pageNo > FIX_FILTER_COUNT) {
                                                      afterView.setFilterType(CustomFilterView.FilterType.TYPE_NONE);
                                                      beforeView.setFilterType(CustomFilterView.FilterType.TYPE_NONE);
                                                  } else {
                                                      afterView.setFilterType(CustomFilterView.FilterType.TYPE_NONE);
                                                      beforeView.setFilterType(CustomFilterView.FilterType.TYPE_NONE);
                                                  }
                                              }

                                              @Override
                                              public void onPageScrolled(int arg0, float arg1, int arg2) {
                                                  mIsViewPagerMove = true;
                                                  if (mOnHandleRelativeLayoutEvent != null) {
                                                      int pageNo = arg0 % mTotalFilters;
                                                      mOnHandleRelativeLayoutEvent.onScroll(pageNo, arg1);
                                                  }
                                              }

                                              @Override
                                              public void onPageScrollStateChanged(int arg0) {

                                              }
                                          }

        );
        viewPager.setCurrentItem(Integer.MAX_VALUE / 2 - (Integer.MAX_VALUE / 2) % mTotalFilters);
    }

    public CustomViewPager getViewPager() {
        return (CustomViewPager) findViewById(R.id.viewPager);
    }

    public void setCanScroll(boolean isCanScroll) {
        getViewPager().setCanScroll(isCanScroll);
    }


    private PagerAdapter mPagerAdapter = new PagerAdapter() {
        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViewList.get(position % mViewList.size()));
        }

        @Override
        public int getItemPosition(Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            int index = position % mViewList.size();
            View view = mViewList.get(index);
            if (view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
            }
            container.addView(mViewList.get(index));
            return mViewList.get(index);
        }

    };

    private long mTapTime;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsViewPagerMove = false;
                mTapTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                if (!mIsViewPagerMove && System.currentTimeMillis() - mTapTime < 200) {
                    if (mOnHandleRelativeLayoutEvent != null) {
                        mOnHandleRelativeLayoutEvent.onClick();
                    }
                }
                break;
        }
        return super.onInterceptTouchEvent(event);
    }

    public void setOnHandleRelativeLayoutEvent(OnHandleRelativeLayoutEvent onHandleRelativeLayoutEvent) {
        mOnHandleRelativeLayoutEvent = onHandleRelativeLayoutEvent;
    }

    public void destroyView() {
        mViewList.clear();
        mViewList = null;
    }

}

