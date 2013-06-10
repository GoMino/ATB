package com.android.toolbox.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.DatePicker;

/**
 * @author gomino (amine.bezzarga@labgency.com)
 */
public class DatePickerScrollFix extends DatePicker {

	public DatePickerScrollFix(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public DatePickerScrollFix(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public DatePickerScrollFix(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		/*
		 * prevent parent view from stealing our events once we've gotten a touch down*/
		if(ev.getActionMasked() == MotionEvent.ACTION_DOWN){
			ViewParent p = getParent();
			if(p!=null){
				p.requestDisallowInterceptTouchEvent(true);
			}
		}
		return super.onInterceptTouchEvent(ev);
	}

	
	
}
