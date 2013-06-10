package com.android.toolbox.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author gomino (amine.bezzarga@labgency.com)
 */
public class DuplicateParentStateAwareRelativeLayout extends CheckableRelativeLayout {

	public DuplicateParentStateAwareRelativeLayout(Context context) {
		super(context);
	}

	public DuplicateParentStateAwareRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public DuplicateParentStateAwareRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/*
	 * By default ViewGroup call setPressed on each child view, this take into account duplicateparentstate parameter
	 */
	 @Override
	 protected void  dispatchSetPressed(boolean pressed) {
		 for (int i = 0; i < getChildCount(); i++) {
			 View child = getChildAt(i);
			 if (child.isDuplicateParentStateEnabled()){
				 getChildAt(i).setPressed(pressed);
			 }
		 }
	 }

	
	
}
