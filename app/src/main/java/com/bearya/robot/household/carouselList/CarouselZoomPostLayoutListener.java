package com.bearya.robot.household.carouselList;

import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

/**
 * Implementation of {@link com.azoft.carousellayoutmanager.CarouselLayoutManager.PostLayoutListener} that makes interesting scaling of items. <br />
 * We are trying to make items scaling quicker for closer items for center and slower for when they are far away.<br />
 * Tis implementation uses atan function for this purpose.
 */
public class CarouselZoomPostLayoutListener implements CarouselLayoutManager.PostLayoutListener {

    private int marginTop = 0;//向两递降的步值
    private float minScale = 0.1f;//最小缩小值

    public CarouselZoomPostLayoutListener() {

    }

    public CarouselZoomPostLayoutListener(int mt, float ms) {
        marginTop = mt;
        minScale = ms;
    }

    @Override
    public ItemTransformation transformChild(@NonNull final View child, final float itemPositionToCenterDiff, final int orientation) {
        float scale = (float) (2 * (2 * -StrictMath.atan(Math.abs(itemPositionToCenterDiff) + 1.0) / Math.PI + 1));
        Log.d("AllNewScale", "Go scale = "+scale);
        if (scale < minScale) {
            scale = minScale;
        }

        // because scaling will make view smaller in its center, then we should move this item to the top or bottom to make it visible
        final float translateY;
        final float translateX;
        Log.d("AllNewScale", "itemPositionToCenterDiff = "+itemPositionToCenterDiff);
        if (CarouselLayoutManager.VERTICAL == orientation) {
            final float translateYGeneral = child.getMeasuredHeight() * (1 - scale) / 2f;
            Log.d("AllNewScale", "translateYGeneral = "+translateYGeneral);
            translateY = Math.signum(itemPositionToCenterDiff) * translateYGeneral * 2.6f *  Math.abs(itemPositionToCenterDiff);
            translateX = Math.abs(itemPositionToCenterDiff) * marginTop;
        } else {
            final float translateXGeneral = child.getMeasuredWidth() * (1 - scale) / 2f;
            Log.d("AllNewScale", "translateXGeneral = "+translateXGeneral);
            translateX = Math.signum(itemPositionToCenterDiff) * translateXGeneral * 2.7f *  Math.abs(itemPositionToCenterDiff);
            translateY = Math.abs(itemPositionToCenterDiff) * marginTop;
        }
        Log.d("AllNewScale", "scale = "+scale+ " translateX = "+translateX+ " translateY = "+translateY);
        return new ItemTransformation(scale, scale, translateX, translateY);
    }
}