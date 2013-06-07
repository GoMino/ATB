/**
 * 
 */
package com.android.toolbox.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Gallery;

/**
 * Special TextView that does not request layout each time setText is called on it
 * @author benoit.vannesson@labgency.com
 *
 */
public class BlockableGallery extends Gallery {
	
	private boolean mBlockRequestLayout; 
	
	public BlockableGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public BlockableGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public BlockableGallery(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
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
