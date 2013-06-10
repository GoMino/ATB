package com.android.toolbox.views;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public class CheckableRelativeLayout extends RelativeLayout implements Checkable {

	private static final String TAG = "CheckableRelativeLayout";
	private boolean isChecked;
	private List<Checkable> checkableViews;
	private boolean mBlockStateChanged	= false;
	private boolean mLayoutBlocked = false;
	
	public CheckableRelativeLayout(Context context) {
		super(context);
		initialise(null);
	}

	public CheckableRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialise(attrs);
	}

	public CheckableRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialise(attrs);
	}

	/*
	 * @see android.widget.Checkable#isChecked()
	 */
	public boolean isChecked() {
		return isChecked;
	}

	/*
	 * @see android.widget.Checkable#setChecked(boolean)
	 */
	public void setChecked(boolean isChecked) {
//		Log.i(TAG,"setChecked: "+isChecked);
		
		for (Checkable c : checkableViews) {
			c.setChecked(isChecked);
		}
//		Log.e(TAG, "set checked, we have " + checkableViews.size() + " checkable children");
		this.isChecked = isChecked;
		refreshDrawableState();
	}

	/*
	 * @see android.widget.Checkable#toggle()
	 */
	public void toggle() {
		this.isChecked = !this.isChecked;
		for (Checkable c : checkableViews) {
			c.toggle();
		}
		refreshDrawableState();
	}

//	@Override
//	protected void drawableStateChanged() {
//		super.drawableStateChanged();
//	}
//	
    protected int[] onCreateDrawableState(int extraSpace) {
    	if (mBlockStateChanged && !isPressed()){
    		final int[] drawableState = new int[extraSpace];
    		return drawableState;
    	}else{
	    	final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
	    	if (isChecked()) {
	    		 mergeDrawableStates(drawableState, new int[]{android.R.attr.state_checked});
	    	}
	    	return drawableState;
    	}
    }


	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		final int childCount = this.getChildCount();
		for (int i = 0; i < childCount; ++i) {
			findCheckableChildren(this.getChildAt(i));
		}
	}
	
//	public void setBlockStateChange(boolean block){
//		mBlockStateChanged = block;
//		refreshDrawableState();
//	}
	

	/**
	 * Read the custom XML attributes
	 */
	private void initialise(AttributeSet attrs) {
		this.isChecked = false;
		this.checkableViews = new ArrayList<Checkable>();
	}

	/**
	 * Add to our checkable list all the children of the view that implement the
	 * interface Checkable
	 */
	protected void findCheckableChildren(View v) {
		if (v instanceof Checkable && v.isDuplicateParentStateEnabled()) {
			this.checkableViews.add((Checkable) v);
		}

		if (v instanceof ViewGroup) {
			final ViewGroup vg = (ViewGroup) v;
			final int childCount = vg.getChildCount();
			for (int i = 0; i < childCount; ++i) {
				findCheckableChildren(vg.getChildAt(i));
			}
		}
	}
	
	public void setBlockRequestLayout(boolean blocked){
		mLayoutBlocked = blocked;
	}

	@Override
	public void requestLayout() {
		if (!mLayoutBlocked)
			super.requestLayout();
	}
}
