package com.bearya.robot.household.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.bearya.robot.household.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Random;

/**
 * 气泡悬浮动画
 *
 * @author gxchen
 */
@SuppressLint("DrawAllocation")
public class AnimPopHungView extends View {
    private int minOffSet = 10;
    public static final int offset = 30;
    private int mMinWidth;
    private ArrayList<CircleData> mDataList = new ArrayList<CircleData>();
    private Paint mCirclePaint,
            mNormalContentPaint;
    private OnCircleClickListener mOnCircleClickListener;
    private AnimThread mAnimThread;
    private final int Left = 0, Right = 1, Up = 2, Down = 3, LeftUP = 4,
            RightUp = 5, LeftDown = 6, RightDown = 7;
    Random random = new Random();
    private int animSpeed = 2000;
    private int mWidth, mHeight;
    private int mWidthMeasureSpec,mHeightMeasureSpec;

    public void setmOnCircleClickListener(
            OnCircleClickListener mOnCircleClickListener) {
        this.mOnCircleClickListener = mOnCircleClickListener;
    }

    /**
     * 设置颜色
     *
     * @param normalContentColor
     */
    public void setContentColor(
            int normalContentColor) {
        mNormalContentPaint.setColor(normalContentColor);
        invalidate();
    }

    /**
     * 设置文字大小
     *
     * @param normalTextSize
     */
    public void setContentSize(int normalTextSize) {
        mNormalContentPaint.setTextSize(normalTextSize);
        invalidate();
    }

    public static class CircleData {
        public int ox, oy;
        public int r;
        public String name;
        public String numble;
        public int ox_offSet;
        public int oy_offSet;
        public int direction;
        public int circleColor;
        public int centerDistance;
    }

    public void setDataList(ArrayList<CircleData> circleDatas, int width) {
        mDataList.clear();
        mDataList.addAll(circleDatas);
        this.mWidth = width;
        this.mHeight = width;
        mAnimThread = new AnimThread(mDataList);
        mAnimThread.start();
        setMeasuredDimension(measureWidth(mWidthMeasureSpec), measureWidth(mHeightMeasureSpec));
        invalidate();
    }


    public AnimPopHungView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private class AnimThread extends Thread {
        private boolean needStop = false;
        private ArrayList<CircleData> circleDatas;

        public AnimThread(ArrayList<CircleData> circleDatas) {
            super();
            this.circleDatas = circleDatas;
        }

        public void stopAnim() {
            needStop = true;
        }

        @Override
        public void run() {
            int step = 0;
            resetDirections();
            CircleData circleData;
            while (!needStop) {
                step = (step + 1) % offset;
                // 动画自由度控制
                if (step == 0 || step == 2 * offset / 3 || step == offset / 3) {
                    resetDirections();
                }
                for (int i = 0; i < circleDatas.size(); i++) {
                    circleData = circleDatas.get(i);
                    if (circleData.direction == Left) {
                        circleData.ox_offSet = Math.max(
                                circleData.ox_offSet - 1, -offset);
                    } else if (circleData.direction == Right) {
                        circleData.ox_offSet = Math.min(
                                circleData.ox_offSet + 1, offset);
                    } else if (circleData.direction == Up) {
                        circleData.oy_offSet = Math.max(
                                circleData.oy_offSet - 1, -offset);
                    } else if (circleData.direction == Down) {
                        circleData.oy_offSet = Math.min(
                                circleData.oy_offSet + 1, offset);
                    } else if (circleData.direction == LeftUP) {
                        circleData.ox_offSet = Math.max(
                                circleData.ox_offSet - 1, -offset);
                        circleData.oy_offSet = Math.max(
                                circleData.oy_offSet - 1, -offset);
                    } else if (circleData.direction == RightUp) {
                        circleData.ox_offSet = Math.min(
                                circleData.ox_offSet + 1, offset);
                        circleData.oy_offSet = Math.max(
                                circleData.oy_offSet - 1, -offset);
                    } else if (circleData.direction == LeftDown) {
                        circleData.ox_offSet = Math.max(
                                circleData.ox_offSet - 1, -offset);
                        circleData.oy_offSet = Math.min(
                                circleData.oy_offSet + 1, offset);
                    } else if (circleData.direction == RightDown) {
                        circleData.ox_offSet = Math.min(
                                circleData.ox_offSet + 1, offset);
                        circleData.oy_offSet = Math.min(
                                circleData.oy_offSet + 1, offset);
                    }
                }
                postInvalidate();
                try {
                    sleep(animSpeed / offset);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            super.run();
        }

        private void resetDirections() {
            for (int i = 0; i < mDataList.size(); i++) {
                mDataList.get(i).direction = getCheckedDirection(mDataList
                        .get(i));
            }
        }

        private int getCheckedDirection(CircleData circleData) {
            int newDirection = random.nextInt(8);
            if (newDirection == Left) {
                if (circleData.ox_offSet > -offset) {
                    return newDirection;
                } else {
                    return getCheckedDirection(circleData);
                }
            } else if (newDirection == Right) {
                if (circleData.ox_offSet < offset) {
                    return newDirection;
                } else {
                    return getCheckedDirection(circleData);
                }
            } else if (newDirection == Up) {
                if (circleData.oy_offSet > -offset) {
                    return newDirection;
                } else {
                    return getCheckedDirection(circleData);
                }
            } else if (newDirection == Down) {
                if (circleData.oy_offSet < offset) {
                    return newDirection;
                } else {
                    return getCheckedDirection(circleData);
                }
            } else if (newDirection == LeftUP) {
                if (circleData.ox_offSet == -offset
                        && circleData.oy_offSet == -offset) {
                    return getCheckedDirection(circleData);
                } else {
                    return newDirection;
                }
            } else if (newDirection == LeftDown) {
                if (circleData.ox_offSet == -offset
                        && circleData.oy_offSet == offset) {
                    return getCheckedDirection(circleData);
                } else {
                    return newDirection;
                }
            } else if (newDirection == RightUp) {
                if (circleData.ox_offSet == offset
                        && circleData.oy_offSet == -offset) {
                    return getCheckedDirection(circleData);
                } else {
                    return newDirection;
                }
            } else if (newDirection == RightDown) {
                if (circleData.ox_offSet == offset
                        && circleData.oy_offSet == offset) {
                    return getCheckedDirection(circleData);
                } else {
                    return newDirection;
                }
            }
            return getCheckedDirection(circleData);
        }
    }

    private void init(Context context) {
        this.minOffSet = (int) CommonUtils.dip2px(context, minOffSet);
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mNormalContentPaint = new Paint();
    }

    public AnimPopHungView(Context context) {
        super(context);
        init(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        CircleData data;
        Rect bounds = new Rect();
        String name, num;
        for (int i = 0; i < mDataList.size(); i++) {
            data = mDataList.get(i);
            name = data.name != null ? data.name : "";
            num = data.numble != null ? data.numble : "";
            mCirclePaint.setColor(data.circleColor);
            canvas.drawCircle(data.ox + data.ox_offSet, data.oy
                    + data.oy_offSet, data.r, mCirclePaint);
            if(name.length()>3){
                String name1,name2;
                name1 = name.substring(0,2);
                name2 = name.substring(2,name.length());
                Rect bounds1 = new Rect(),bounds2 = new Rect();
                mNormalContentPaint.getTextBounds(name1, 0, name1.length(),
                        bounds1);
                mNormalContentPaint.getTextBounds(name2, 0, name2.length(),
                        bounds2);
                mNormalContentPaint.getTextBounds(num, 0, num.length(), bounds);
                canvas.drawText(name1, data.ox + data.ox_offSet - bounds1.width()
                                / 2, data.oy + data.oy_offSet - bounds1.height(),
                        mNormalContentPaint);
                canvas.drawText(name2, data.ox + data.ox_offSet - bounds2.width()
                                / 2, data.oy + data.oy_offSet + bounds2.height()/2,
                        mNormalContentPaint);
                canvas.drawText(num, data.ox + data.ox_offSet - bounds.width()
                                / 2, data.oy + data.oy_offSet + bounds.height() + bounds2.height(),
                        mNormalContentPaint);
            }else{
                mNormalContentPaint.getTextBounds(name, 0, name.length(),
                        bounds);
                canvas.drawText(name, data.ox + data.ox_offSet - bounds.width()
                                / 2, data.oy + data.oy_offSet - bounds.height() / 2,
                        mNormalContentPaint);
                mNormalContentPaint.getTextBounds(num, 0, num.length(), bounds);
                canvas.drawText(num, data.ox + data.ox_offSet - bounds.width()
                                / 2, data.oy + data.oy_offSet + bounds.height(),
                        mNormalContentPaint);
            }
        }
        super.onDraw(canvas);
    }

    public interface OnCircleClickListener {
        public void onCircleClicked(int clickPos);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            CircleData circleData;
            if (mOnCircleClickListener != null) {
                for (int i = 0; i < mDataList.size(); i++) {
                    circleData = mDataList.get(i);
                    if (Math.sqrt(Math.pow(event.getX()
                            - (circleData.ox + circleData.ox_offSet), 2)
                            + Math.pow(event.getY()
                            - (circleData.oy + circleData.oy_offSet), 2)) <= circleData.r) {
                        mOnCircleClickListener.onCircleClicked(i);
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.mWidthMeasureSpec = widthMeasureSpec;
        this.mHeightMeasureSpec = heightMeasureSpec;
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureWidth(heightMeasureSpec));
    }

    /**
     * 重新计算宽度
     *
     * @param widthMeasureSpec
     * @return
     */
    private int measureWidth(int widthMeasureSpec) {
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        if (specMode == MeasureSpec.AT_MOST) {
            return mWidth;
        } else if (specMode == MeasureSpec.EXACTLY) {
            return specSize;
        }
        return mWidth;
    }


    public void stopAnim() {
        if (mAnimThread != null) {
            mAnimThread.stopAnim();
        }
    }

    /**
     * 设置组件最小宽度
     *
     * @param width
     */
    public void setMinWidth(int width) {
        this.mMinWidth = width;
    }

}
