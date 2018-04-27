package com.bearya.robot.household.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.bearya.robot.household.R;

/**
 * Created by XiaoMai
 */
@SuppressLint("AppCompatCustomView")
public class CircleView extends TextView {

    private int radius;

    private int borderWidth;

    private int borderColor;

    private int fillColor;

    private Paint borderPaint;

    private Paint fillPaint;

    public CircleView(Context context) {
        this(context, null);
        init(context);
    }

    public CircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        borderWidth = 2;
        borderColor = context.getResources().getColor(R.color.colorPrimary);
        fillColor = context.getResources().getColor(R.color.colorWhite);
        borderPaint = new Paint();
        fillPaint = new Paint();
        setGravity(Gravity.CENTER);
    }

    public void setFillColor(int color) {
        fillColor = color;
        invalidate();
    }

    public void setBorderColor(int color){
        borderColor = color;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        int max = Math.max(measuredWidth, measuredHeight);
        setMeasuredDimension(max, max);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getWidth();
        // 半径
        radius = Math.min(width, height) / 2;
        if (borderWidth > 0) {
            borderPaint.setColor(borderColor);
            canvas.drawCircle(getWidth() / 2, getWidth() / 2, radius, borderPaint);
        }
        fillPaint.setColor(fillColor);
        canvas.drawCircle(getWidth() / 2, getWidth() / 2, radius - borderWidth, fillPaint);
        super.onDraw(canvas);
    }

}