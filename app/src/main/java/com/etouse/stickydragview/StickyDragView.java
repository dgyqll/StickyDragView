package com.etouse.stickydragview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Administrator on 2017/6/19.
 */

public class StickyDragView extends View {

    private Paint mPaint;               //画笔
    private PointF mDragCirclePoint;    //拖拽圆形
    private PointF mGooCirclePoint;     //Goo圆形
    private float mDragRadio;   //拖拽圆半径
    private float mGooRadio;    //Goo圆半径
    private float mOriginalGooRadio;    //Goo圆最初半径
    private PointF[] mDragPoints = new PointF[2];   //拖拽点
    private PointF[] mGooPoints = new PointF[2];    //Goo点
    private PointF contolPoint = new PointF();      //控制点

    private float maxDistance = 150f;// 两圆心的最大距离
    public StickyDragView(Context context) {
        this(context,null);
    }

    public StickyDragView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public StickyDragView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //初始化画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);


        mDragRadio = 15;
        mGooRadio = 15;
        mOriginalGooRadio = 15;
        mDragCirclePoint = new PointF(100f,100f);
        mGooCirclePoint = new PointF(100f,100f);

        mDragPoints = new PointF[]{new PointF(100f,85f),new PointF(100f,115f)};
        mGooPoints = new PointF[]{new PointF(100f, 85f),new PointF(100f,115f)};
        contolPoint = new PointF(100f,100f);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float offsetX = mGooCirclePoint.x - mDragCirclePoint.x;
        float offsetY = mGooCirclePoint.y - mDragCirclePoint.y;
        Double lineK = Double.valueOf(offsetY / offsetX);

        //求取两圆心之间的距离
        float distanceBetween2Points = GeometryUtil.getDistanceBetween2Points(mDragCirclePoint, mGooCirclePoint);
        float fration = distanceBetween2Points / maxDistance;
        //Goo圆半径随着拖拽的比例而动态改变
        mGooRadio = (1f - fration ) * mOriginalGooRadio;

        if (offsetX != 0) {
            //通过指定圆心，斜率为lineK的直线与圆的交点
            mDragPoints = GeometryUtil.getIntersectionPoints(mDragCirclePoint, mDragRadio, lineK);
            mGooPoints = GeometryUtil.getIntersectionPoints(mGooCirclePoint, mGooRadio, lineK);
            //控制点
            contolPoint = GeometryUtil.getMiddlePoint(mDragCirclePoint, mGooCirclePoint);
        }
        //画圆
        canvas.drawCircle(mGooCirclePoint.x,mGooCirclePoint.y,mGooRadio,mPaint);
        canvas.drawCircle(mDragCirclePoint.x,mDragCirclePoint.y,mDragRadio,mPaint);

        //两圆心之间的距离小于最大限制距离，绘制贝塞尔曲线
        if (distanceBetween2Points < maxDistance) {

            Path path = new Path();
            path.moveTo(mGooPoints[0].x, mGooPoints[0].y);
            path.quadTo(contolPoint.x,contolPoint.y,mDragPoints[0].x,mDragPoints[0].y);

            path.lineTo(mDragPoints[1].x,mDragPoints[1].y);
            path.quadTo(contolPoint.x,contolPoint.y,mGooPoints[1].x, mGooPoints[1].y);

            canvas.drawPath(path,mPaint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                mDragCirclePoint.set(event.getX(),event.getY());
                // 重新绘制
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                    reset(); //回弹
                break;

        }
        return true;
    }

    private void reset() {
        final PointF currentDragCenter = new PointF(mDragCirclePoint.x, mDragCirclePoint.y);
        final ValueAnimator animator = ValueAnimator.ofInt(1, 2);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = animator.getAnimatedFraction();// 获取当前动画执行的百分比
                 mDragCirclePoint = GeometryUtil.getPointByPercent(currentDragCenter, mGooCirclePoint, fraction);   //获取新的拖拽圆的圆心
                invalidate();
            }
        });
        animator.start();


    }

}
