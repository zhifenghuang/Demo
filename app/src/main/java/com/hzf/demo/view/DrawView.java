package com.hzf.demo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.hzf.demo.utils.Utils;

import java.util.ArrayList;

/**
 * Created by gigabud on 15-12-1.
 */
public class DrawView extends View {

    private PointF mPreviousPoint;
    private PointF mStartPoint;
    private PointF mCurrentPoint;
    private Paint mPaint, mMaskPaint;
    private Path mPath;

    private ArrayList<DrawLineInfo> mCommentDrawPath;   //表示已经存在的绘画评论

    private ArrayList<DrawLineInfo> mDrawPathsList;     //当前正在绘制的评论
    private DrawLineInfo mDrawLineInfo;
    public Canvas mCanvas,mGlowCanvas;

    private Bitmap mDrawBmp, mGlowBmp;

    private int mCurrentPenType;

    private int mCurrentPenWidthType = DrawCommentView.TYPE_PEN_WIDTH_10;

    private boolean mIsEnable = false;

    private int mPaintColor = Color.RED;

    public static class DrawLineInfo {   //手指一次按下抬起的绘画信息
        int color;
        int penType;
        int penWidthType;
        ArrayList<DrawPath> drawPaths;
        boolean isDrawInBmp = false;
    }


    public static class DrawPath {
        Path path;
        boolean isDrawPoint;
        Point point;
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPath = new Path();
        mDrawPathsList = new ArrayList<>();
        setBackgroundColor(Color.TRANSPARENT);
        mCurrentPenType = DrawCommentView.TYPE_COMMON_PEN;
        resetDrawPaint(mPaintColor, mCurrentPenWidthType, mCurrentPenType);
    }

    public void setCurrentPenType(int currentPenType) {
        mCurrentPenType = currentPenType;
    }

    public void setCurrentPenWidthType(int currentPenWidthType) {
        mCurrentPenWidthType = currentPenWidthType;
    }

    public void setCommentDrawPath(ArrayList<DrawLineInfo> commentDrawPath) {
        mCommentDrawPath = commentDrawPath;
        invalidate();
    }

    public ArrayList<DrawLineInfo> addDrawPathToComment() {
        if (mDrawPathsList == null || mDrawPathsList.isEmpty()) {
            return new ArrayList<>();
        }
        ArrayList<DrawLineInfo> list = (ArrayList<DrawLineInfo>) mDrawPathsList.clone();
        mDrawPathsList.clear();
        resetDrawView();
        return list;
    }

    public void resetDrawPaint(int paintColor, int penWidthType, int penType) {
        int strokeWidth = getStrokeWidth(penWidthType);
        CornerPathEffect cornerPathEffect = new CornerPathEffect(Utils.dip2px(getContext(), 5));
        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.reset();
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
        }
        mPaint.setStrokeWidth(strokeWidth);   //Utils.dip2px(getContext(), 5)
        mPaint.setPathEffect(cornerPathEffect);
        if (penType == DrawCommentView.TYPE_COMMON_PEN) {
            mPaint.setColor(paintColor);
        } else {
            mPaint.setColor(Color.WHITE);
            if (mMaskPaint == null) {
                mMaskPaint = new Paint();
                mMaskPaint.reset();
                mMaskPaint.setAntiAlias(true);
                mMaskPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            }
            mMaskPaint.setShadowLayer((int) (strokeWidth * 1.2f + 0.5f), 0, 0, paintColor);
            mMaskPaint.setStrokeWidth(strokeWidth);
            mMaskPaint.setColor(paintColor);
            mMaskPaint.setPathEffect(cornerPathEffect);
        }
    }

    public void setEnable(boolean isEnable) {
        mIsEnable = isEnable;
    }

    public boolean isEnable() {
        return mIsEnable;
    }

    public void resetPaintColor(int color) {
        mPaintColor = color;
    }

    public ArrayList<DrawLineInfo> getDrawPathsList() {
        return mDrawPathsList;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsEnable) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDrawLineInfo = new DrawLineInfo();
                mDrawLineInfo.color = mPaintColor;
                mDrawLineInfo.penType = mCurrentPenType;
                mDrawLineInfo.penWidthType = mCurrentPenWidthType;
                resetDrawPaint(mPaintColor, mCurrentPenWidthType, mCurrentPenType);
                mDrawLineInfo.drawPaths = new ArrayList<>();
                mCurrentPoint = new PointF(event.getX(), event.getY());
                mPreviousPoint = mCurrentPoint;
                mStartPoint = mPreviousPoint;
                drawLine(mPreviousPoint, mStartPoint, mCurrentPoint, true);
                break;
            case MotionEvent.ACTION_MOVE:
                mStartPoint = mPreviousPoint;
                mPreviousPoint = mCurrentPoint;
                mCurrentPoint = new PointF(event.getX(), event.getY());
                drawLine(mPreviousPoint, mStartPoint, mCurrentPoint, false);
                break;
            case MotionEvent.ACTION_UP:
                mStartPoint = mPreviousPoint;
                mPreviousPoint = mCurrentPoint;
                mCurrentPoint = new PointF(event.getX(), event.getY());
                drawLine(mPreviousPoint, mStartPoint, mCurrentPoint, false);
                savePaintOperator();
                break;
            default:
                break;
        }
        return true;
    }


    public synchronized void unDoDraw() {
        if (isDrawPathsListEmpty()) {
            return;
        }
        mDrawPathsList.remove(mDrawPathsList.size() - 1);
        resetDrawView();
    }

    public void resetDrawView() {
        if (mDrawBmp != null && !mDrawBmp.isRecycled()) {
            mDrawBmp.recycle();
            mDrawBmp = null;
        }
        if (mGlowBmp != null && !mGlowBmp.isRecycled()) {
            mGlowBmp.recycle();
            mGlowBmp = null;
        }
        drawPathListInBitmap(mDrawPathsList);
        invalidate();
    }

    private void drawPathInBitmap(DrawPath dp) {
        if (dp == null) {
            return;
        }
        if(mCurrentPenType==DrawCommentView.TYPE_GLOW_PEN){
            if (mGlowBmp == null || mGlowBmp.isRecycled()) {
//                DisplayMetrics displayMetrics = new DisplayMetrics();
//                ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
                mGlowBmp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                mGlowCanvas = new Canvas(mGlowBmp);
                mGlowCanvas.drawBitmap(mGlowBmp, 0, 0, null);
            }
        }
        if (mDrawBmp == null || mDrawBmp.isRecycled()) {
//            DisplayMetrics displayMetrics = new DisplayMetrics();
//            ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
            mDrawBmp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mDrawBmp);
            mCanvas.drawBitmap(mDrawBmp, 0, 0, null);
        }

        if (mCurrentPenType == DrawCommentView.TYPE_GLOW_PEN) {
            if (dp.isDrawPoint) {
                mMaskPaint.setStrokeCap(Paint.Cap.ROUND);
                mGlowCanvas.drawPoint(dp.point.x, dp.point.y,
                        mMaskPaint);
            } else {
                mMaskPaint.setStrokeCap(Paint.Cap.BUTT);
                mGlowCanvas.drawPath(dp.path, mMaskPaint);
            }
        }
        if (dp.isDrawPoint) {
            mCanvas.drawPoint(dp.point.x, dp.point.y,
                    mPaint);
        } else {
            mCanvas.drawPath(dp.path, mPaint);
        }
    }

    public void drawPathListInBitmap(ArrayList<DrawLineInfo> drawPathList) {
        if (drawPathList != null && !drawPathList.isEmpty()) {
            if (mDrawBmp == null || mDrawBmp.isRecycled()) {
//                DisplayMetrics displayMetrics = new DisplayMetrics();
//                ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
                mDrawBmp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                mCanvas = new Canvas(mDrawBmp);
                mCanvas.drawBitmap(mDrawBmp, 0, 0, null);
                for (DrawLineInfo drawLineInfo : drawPathList) {
                    drawLineInfo.isDrawInBmp = false;
                }
            }

            for (DrawLineInfo drawLineInfo : drawPathList) {
                if (isDrawLineInfoInvalid(drawLineInfo) || drawLineInfo.isDrawInBmp) {
                    continue;
                }
                resetDrawPaint(drawLineInfo.color, drawLineInfo.penWidthType, drawLineInfo.penType);
                drawLineInfo.isDrawInBmp = true;
                if (drawLineInfo.penType == DrawCommentView.TYPE_GLOW_PEN) {
                    if (mGlowBmp == null || mGlowBmp.isRecycled()) {
//                        DisplayMetrics displayMetrics = new DisplayMetrics();
//                        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
                        mGlowBmp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                        mGlowCanvas = new Canvas(mGlowBmp);
                        mGlowCanvas.drawBitmap(mGlowBmp, 0, 0, null);
                    }
                    for (DrawPath dp : drawLineInfo.drawPaths) {
                        if (dp.isDrawPoint) {
                            mMaskPaint.setStrokeCap(Paint.Cap.ROUND);
                            mGlowCanvas.drawPoint(dp.point.x, dp.point.y,
                                    mMaskPaint);
                        } else {
                            mMaskPaint.setStrokeCap(Paint.Cap.BUTT);
                            mGlowCanvas.drawPath(dp.path, mMaskPaint);
                        }
                    }
                }
                for (DrawPath dp : drawLineInfo.drawPaths) {
                    if (dp.isDrawPoint) {
                        mCanvas.drawPoint(dp.point.x, dp.point.y,
                                mPaint);
                    } else {
                        mCanvas.drawPath(dp.path, mPaint);
                    }
                }
            }
            resetDrawPaint(mPaintColor, mCurrentPenWidthType, mCurrentPenType);
        }
    }

    public boolean isDrawPathsListEmpty() {
        if (mDrawPathsList == null || mDrawPathsList.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isDrawed() {
        if ((mDrawPathsList == null || mDrawPathsList.isEmpty()) && isDrawLineInfoInvalid(mDrawLineInfo)) {
            return false;
        } else {
            return true;
        }
    }


    /*
     * 保存住Operator方式，以便撤销
     */
    private void savePaintOperator() {
        if (!isDrawLineInfoInvalid(mDrawLineInfo)) {
            mDrawPathsList.add(mDrawLineInfo);
            mDrawLineInfo.isDrawInBmp = true;
            //drawPathListInBitmap(mDrawPathsList);
        }
        mDrawLineInfo = null;
    }

    public boolean isDrawLineInfoInvalid(DrawLineInfo drawLineInfo) {   //判断DrawLineInfo是否无效
        return drawLineInfo == null || drawLineInfo.drawPaths == null || drawLineInfo.drawPaths.isEmpty();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mCommentDrawPath != null && !mCommentDrawPath.isEmpty()) {
            for (DrawLineInfo drawLineInfo : mCommentDrawPath) {
                if (isDrawLineInfoInvalid(drawLineInfo)) {
                    continue;
                }
                resetDrawPaint(drawLineInfo.color, drawLineInfo.penWidthType, drawLineInfo.penType);
                if (drawLineInfo.penType == DrawCommentView.TYPE_GLOW_PEN) {
                    for (DrawPath dp : drawLineInfo.drawPaths) {
                        if (dp.isDrawPoint) {
                            mMaskPaint.setStrokeCap(Paint.Cap.ROUND);
                            canvas.drawPoint(dp.point.x, dp.point.y,
                                    mMaskPaint);
                        } else {
                            mMaskPaint.setStrokeCap(Paint.Cap.BUTT);
                            canvas.drawPath(dp.path, mMaskPaint);
                        }
                    }
                }
            }

            for (DrawLineInfo drawLineInfo : mCommentDrawPath) {
                if (isDrawLineInfoInvalid(drawLineInfo)) {
                    continue;
                }
                resetDrawPaint(drawLineInfo.color, drawLineInfo.penWidthType, drawLineInfo.penType);
                for (DrawPath dp : drawLineInfo.drawPaths) {
                    if (dp.isDrawPoint) {
                        canvas.drawPoint(dp.point.x, dp.point.y,
                                mPaint);
                    } else {
                        canvas.drawPath(dp.path, mPaint);
                    }
                }
            }
        }

        if (mGlowBmp != null && !mGlowBmp.isRecycled()) {
            canvas.drawBitmap(mGlowBmp, 0, 0, null);
        }

        if (mDrawBmp != null && !mDrawBmp.isRecycled()) {
            canvas.drawBitmap(mDrawBmp, 0, 0, null);
        }

//        if (!isDrawLineInfoInvalid(mDrawLineInfo)) {
//            resetDrawPaint(mDrawLineInfo.color, mDrawLineInfo.penWidthType, mDrawLineInfo.penType);
//            if (mDrawLineInfo.penType == DrawCommentView.TYPE_GLOW_PEN) {
//                for (DrawPath dp : mDrawLineInfo.drawPaths) {
//                    if (dp.isDrawPoint) {
//                        mMaskPaint.setStrokeCap(Paint.Cap.ROUND);
//                        canvas.drawPoint(dp.point.x, dp.point.y,
//                                mMaskPaint);
//                    } else {
//                        mMaskPaint.setStrokeCap(Paint.Cap.BUTT);
//                        canvas.drawPath(dp.path, mMaskPaint);
//                    }
//                }
//            }
//            for (DrawPath dp : mDrawLineInfo.drawPaths) {
//                if (dp.isDrawPoint) {
//                    canvas.drawPoint(dp.point.x, dp.point.y,
//                            mPaint);
//                } else {
//                    canvas.drawPath(dp.path, mPaint);
//                }
//            }
//        }
//        resetDrawPaint(mPaintColor, mCurrentPenWidthType, mCurrentPenType);
    }


    private void drawLine(PointF previous, PointF start, PointF end, boolean isDrawPoint) {
        DrawPath dp = new DrawPath();
        dp.point = new Point((int) (end.x + 0.5), (int) (end.y + 0.5));
        if (isDrawPoint) {
            dp.isDrawPoint = true;
        } else {
            PointF mid1 = midPoint(previous, start);
            PointF mid2 = midPoint(end, previous);
            mPath = new Path();
            mPath.reset();
            mPath.moveTo(mid1.x, mid1.y);
            mPath.quadTo(previous.x, previous.y, mid2.x, mid2.y);
            dp.path = mPath;
            dp.isDrawPoint = false;
        }
        mDrawLineInfo.drawPaths.add(dp);
        drawPathInBitmap(dp);
        invalidate();
    }

    private PointF midPoint(PointF p1, PointF p2) {
        return new PointF((p1.x + p2.x) * 0.5f, (p1.y + p2.y) * 0.5f);
    }


    public void recycleBmp() {
        if (mDrawBmp != null && !mDrawBmp.isRecycled()) {
            mDrawBmp.recycle();
            mDrawBmp = null;
        }
    }

    public int getStrokeWidth(int penWidthType) {
        int size;
        switch (penWidthType) {
            case DrawCommentView.TYPE_PEN_WIDTH_4:
                size = 4;
                break;
            case DrawCommentView.TYPE_PEN_WIDTH_10:
                size = 10;
                break;
            case DrawCommentView.TYPE_PEN_WIDTH_16:
                size = 16;
                break;
            case DrawCommentView.TYPE_PEN_WIDTH_22:
                size = 22;
                break;
            default:
                size = 10;
                break;
        }
        return Utils.dip2px(getContext(), size);
    }
}

