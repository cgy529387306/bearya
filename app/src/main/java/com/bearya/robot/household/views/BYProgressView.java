package com.bearya.robot.household.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.bearya.robot.household.R;


/**
 * Created by yexifeng on 17/8/28.
 */

public class BYProgressView extends View {

    public static final float MAX_PROGRESS = 100;
    private int bgResId;
    private int fgResId;

    private ProgressListener listener;

    private Rect progressFgRect;
    private Rect progressBgRect;


    private Drawable progressFg;
    private Drawable progressBg;

    private Paint paint;


    private int pgHeight;

    private int progress = 0;

    private int fgDistinceBgBorder =3;

    public BYProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.BYProgressView);
        bgResId = array.getResourceId(R.styleable.BYProgressView_bgDrawable,R.mipmap.progress_bar_bg);
        fgResId = array.getResourceId(R.styleable.BYProgressView_progressDrawable,R.mipmap.progress_bar_fg);
        array.recycle();
        initResources(context);
    }

    public BYProgressView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BYProgressView(Context context) {
        this(context,null);
    }

    private void initResources(Context context) {

        progressFg = getResources().getDrawable(fgResId);
        progressBg = context.getResources().getDrawable(bgResId);
        pgHeight = progressBg.getIntrinsicHeight();

        progressBgRect = new Rect();
        progressFgRect = new Rect();


        paint = new Paint();
        paint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int chosenWidth = chooseWidthDimension(widthMode, widthSize);
        int chosenHeight = chooseHeightDimension(heightMode, heightSize);

        setMeasuredDimension(chosenWidth, chosenHeight);
    }

    private int chooseWidthDimension(int mode, int size) {
        if (mode == MeasureSpec.EXACTLY || mode == MeasureSpec.AT_MOST) {
            return size;
        } else {
            return size;
        }
    }

    private int chooseHeightDimension(int mode, int size) {
        if (mode == MeasureSpec.EXACTLY) {
            return size;
        } else {
            return pgHeight;
        }
    }

    public void setProgress(int progress) {
        if(progress < 0 ) {
            progress = 0;
        } else if(progress > MAX_PROGRESS) {
            progress = (int) MAX_PROGRESS;
        }
        this.progress = progress;
        if(this.listener != null) {
            listener.onProgressChanged(progress);
        }
        invalidate();
    }

    public int getProgress() {
        return this.progress;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawProgressBar(canvas);
    }

    private void drawProgressBar(Canvas canvas) {
        drawProgressBarBackground(canvas);
        drawProgressBarForeground(canvas);
    }

    private void drawProgressBarBackground(Canvas canvas) {
        progressBgRect.set(0, 0,getWidth(), pgHeight);
        progressBg.setBounds(progressBgRect);
        progressBg.draw(canvas);

    }

    private void drawProgressBarForeground(Canvas canvas) {
        int width = (int) (progressBgRect.width()*1.0f * progress / MAX_PROGRESS);
        progressFgRect.set(progressBgRect.left+fgDistinceBgBorder, progressBgRect.top+fgDistinceBgBorder, progressBgRect.left + width-fgDistinceBgBorder, progressBgRect.bottom-fgDistinceBgBorder);
        progressFg.setBounds(progressFgRect);
        progressFg.draw(canvas);
    }

    public void setListener(ProgressListener listener) {
        this.listener = listener;
    }

    public interface ProgressListener {
        public void onProgressChanged(int progress);

    }

}
