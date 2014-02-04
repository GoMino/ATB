package com.android.toolbox.views;

import com.android.toolbox.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class ATBBoundedLinearLayout extends LinearLayout {

    private final int mBoundedWidth;

    private final int mBoundedHeight;

    public ATBBoundedLinearLayout(Context context) {
        super(context);
        mBoundedWidth = 0;
        mBoundedHeight = 0;
    }

    public ATBBoundedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BoundedView);
        mBoundedWidth = a.getDimensionPixelSize(R.styleable.BoundedView_bounded_width, 0);
        mBoundedHeight = a.getDimensionPixelSize(R.styleable.BoundedView_bounded_height, 0);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Adjust width as necessary
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        if(mBoundedWidth > 0 && mBoundedWidth < measuredWidth) {
            int measureMode = MeasureSpec.getMode(widthMeasureSpec);
            if (measureMode != MeasureSpec.UNSPECIFIED) {
            	widthMeasureSpec = MeasureSpec.makeMeasureSpec(mBoundedWidth, measureMode);
            }
        }
        // Adjust height as necessary
        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        if(mBoundedHeight > 0 && mBoundedHeight < measuredHeight) {
            int measureMode = MeasureSpec.getMode(heightMeasureSpec);
            if (measureMode != MeasureSpec.UNSPECIFIED) {
            	heightMeasureSpec = MeasureSpec.makeMeasureSpec(mBoundedHeight, measureMode);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}