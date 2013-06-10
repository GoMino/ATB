package com.android.toolbox.views;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;
import android.widget.TextView;

import com.android.toolbox.Log;

/**
 * @author gomino (amine.bezzarga@labgency.com)
 */
public class ScrollingTextView extends TextView {
	private final static String TAG = ScrollingTextView.class.getSimpleName();
	// scrolling feature
	private Scroller mSlr;

	// milliseconds for a round of scrolling
	private int mRndDuration = 10000;

	// the X offset when paused
	private int mXPaused = 0;

	// whether it's being paused
	private boolean mPaused = true;

	/*
	 * constructor
	 */
	public ScrollingTextView(Context context) {
		this(context, null);
		init();
	}

	/*
	 * constructor
	 */
	public ScrollingTextView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.textViewStyle);
		init();
	}

	/*
	 * constructor
	 */
	public ScrollingTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init(){
		// customize the TextView
		setSingleLine();
		setEllipsize(null);
		setVisibility(INVISIBLE);

			ViewTreeObserver vto = getViewTreeObserver();
			vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {

					startScroll();
					
					ViewTreeObserver obs = getViewTreeObserver();
					obs.removeGlobalOnLayoutListener(this);
				}
			});
	}

	/**
	 * begin to scroll the text from the original position
	 */
	public void startScroll() {
		// begin from the very right side
		mXPaused = -1 * getWidth();
		// assume it's paused
		mPaused = true;
		resumeScroll();
	}

	/**
	 * resume the scroll from the pausing point
	 */
	public void resumeScroll() {

		if (!mPaused)
			return;

		// Do not know why it would not scroll sometimes
		// if setHorizontallyScrolling is called in constructor.
		setHorizontallyScrolling(true);

		// use LinearInterpolator for steady scrolling
		mSlr = new Scroller(this.getContext(), new LinearInterpolator());
		setScroller(mSlr);

		int scrollingLen = calculateScrollingLen();
		int distance = scrollingLen - (getWidth() + mXPaused);
		
		int textLength = (scrollingLen - getWidth());
		Log.v(TAG, "[resumeScroll] " + textLength +  " vs width " + getWidth());
		if(textLength>getWidth()){
			int duration = (new Double(mRndDuration * distance * 1.00000 / scrollingLen)).intValue();
			
			mSlr.startScroll(mXPaused, 0, distance, 0, duration);
			mPaused = false;
		}else{
			pauseScroll();
		}
		setVisibility(VISIBLE);
	}

	/**
	 * calculate the scrolling length of the text in pixel
	 *
	 * @return the scrolling length in pixels
	 */
	private int calculateScrollingLen() {
		TextPaint tp = getPaint();
		Rect rect = new Rect();
		String strTxt = getText().toString();
		tp.getTextBounds(strTxt, 0, strTxt.length(), rect);
	
		int scrollingLen = rect.width() + getWidth();
		Log.v(TAG, "[calculateScrollingLen] " + (scrollingLen - getWidth()) +  " vs width " + getWidth());
		rect = null;
		return scrollingLen;
	}

	/**
	 * pause scrolling the text
	 */
	public void pauseScroll() {
		if (null == mSlr)
			return;

		if (mPaused)
			return;

		mPaused = true;

		// abortAnimation sets the current X to be the final X,
		// and sets isFinished to be true
		// so current position shall be saved
		mXPaused = mSlr.getCurrX();

		mSlr.abortAnimation();
	}

	@Override
	/*
	 * override the computeScroll to restart scrolling when finished so as that
	 * the text is scrolled forever
	 */
	public void computeScroll() {
		super.computeScroll();

		if (null == mSlr) return;

		if (mSlr.isFinished() && (!mPaused)) {
			this.startScroll();
		}
	}

	public int getRndDuration() {
		return mRndDuration;
	}

	public void setRndDuration(int duration) {
		this.mRndDuration = duration;
	}

	public boolean isPaused() {
		return mPaused;
	}
}
