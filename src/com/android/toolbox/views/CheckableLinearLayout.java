package com.android.toolbox.views;

import java.util.ArrayList;
import java.util.List;

import com.android.toolbox.Log;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class CheckableLinearLayout extends LinearLayout implements Checkable {
	private static final String TAG = "CheckableLinearLayout";
	private boolean isChecked;
	private List<Checkable> checkableViews;
	private boolean mBlocked = false;
	private boolean mBlockStateChanged	= false;
	
	public CheckableLinearLayout(Context context) {
		super(context);
		initialise(null);
	}

//	public CheckableLinearLayout(Context context, AttributeSet attrs, int defStyle) {
//		super(context, attrs, defStyle);
//		initialise(attrs);
//	}

	public CheckableLinearLayout(Context context, AttributeSet attrs) {
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
		this.isChecked = isChecked;
		refreshDrawableState();
	}

	/*
	 * @see android.widget.Checkable#toggle()
	 */
	public void toggle() {
		Log.i(TAG,"toggle");
		this.isChecked = !this.isChecked;
		for (Checkable c : checkableViews) {
			c.toggle();
		}
		refreshDrawableState();
	}
	
	public void setBlockStateChange(boolean block){
		mBlockStateChanged = block;
		refreshDrawableState();
	}
	
	@Override
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
	private void findCheckableChildren(View v) {
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
	
	@Override
	public void requestLayout() {
		if (!mBlocked)
			super.requestLayout();
	}

	public void setBlockRequestLayout(boolean blocked){
		mBlocked = blocked;
	}
}
