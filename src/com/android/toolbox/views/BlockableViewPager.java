package com.android.toolbox.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

/**
 * @author abezzarg (amine.bezzarga@labgency.com)
 */
public class BlockableViewPager extends ViewPager {

	private boolean mBlockRequestLayout; 


	public BlockableViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public BlockableViewPager(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setPressed(boolean pressed) {
		// TODO Auto-generated method stub
		//		super.setPressed(pressed);
	}

	/**
	 * @return the mBlockRequestLayout
	 */
	public boolean isBlockRequestLayout() {
		return mBlockRequestLayout;
	}

	/**
	 * @param mBlockRequestLayout the mBlockRequestLayout to set
	 */
	public void setBlockRequestLayout(boolean mBlockRequestLayout) {
		this.mBlockRequestLayout = mBlockRequestLayout;
	}
	
	@Override
	public void requestLayout() {
		if (! mBlockRequestLayout){
			super.requestLayout();
		}else{
			layout(getLeft(), getTop(), getRight(), getBottom());
		}
		
	}
	
}
