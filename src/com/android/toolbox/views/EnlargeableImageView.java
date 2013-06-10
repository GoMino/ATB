package com.android.toolbox.views;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Special version of ImageView which allow enlarge width of image if android:adjustViewBounds is true.
 * 
 * <p>This simulate HTML behaviour &lt;img src="" widh="100" /&gt;</p>
 * <p><a href="http://stackoverflow.com/questions/6202000/imageview-one-dimension-to-fit-free-space-and-second-evaluate-to-keep-aspect-rati">Stackoverflow question link</p>
 * 
 * @author Tomáš Procházka &lt;<a href="mailto:tomas.prochazka@atomsoft.cz">tomas.prochazka@atomsoft.cz</a>&gt;
 * @author Amine Bezzarga <amine.bezzarga@labgency.com>;
 * @version $Revision: 0$ ($Date: 6.6.2011 18:16:52$)
 */
public class EnlargeableImageView extends android.widget.ImageView {

	private final static String TAG = EnlargeableImageView.class.getSimpleName();
	private int mDrawableWidth;
	private int mDrawableHeight;
	private boolean mAdjustViewBounds;
	private int mMaxWidth;
	private int mMaxHeight;
	private int passLayout = 0; 

	public EnlargeableImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public EnlargeableImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EnlargeableImageView(Context context) {
		super(context);
	}

	public void setAdjustViewBounds(boolean adjustViewBounds) {
		super.setAdjustViewBounds(adjustViewBounds);
		mAdjustViewBounds = adjustViewBounds;
	}

	public void setMaxWidth(int maxWidth) {
		super.setMaxWidth(maxWidth);
		mMaxWidth = maxWidth;
	}

	public void setMaxHeight(int maxHeight) {
		super.setMaxHeight(maxHeight);
		mMaxHeight = maxHeight;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		passLayout ++ ;
//		Log.e(TAG, "onMeasure pass " + passLayout + " widthMeasureSpec=" + widthMeasureSpec + " heightMeasureSpec=" + heightMeasureSpec);

		int w = 0;
		int h = 0;

		// Desired aspect ratio of the view's contents (not including padding)
		float desiredAspect = 0.0f;

		// We are allowed to change the view's width
		boolean resizeWidth = false;

		// We are allowed to change the view's height
		boolean resizeHeight = false;
		
		int widthSize;
		int heightSize;
		
		if (getDrawable() == null){
			w = Math.max(w, getSuggestedMinimumWidth());
			h = Math.max(h, getSuggestedMinimumHeight());

			widthSize = resolveSize(w, widthMeasureSpec);
			heightSize = resolveSize(h, heightMeasureSpec);
			setMeasuredDimension(widthSize, heightSize);
			return;
		}

		mDrawableWidth = getDrawable().getIntrinsicWidth();
		mDrawableHeight = getDrawable().getIntrinsicHeight();

		

		if (mDrawableWidth > 0 || mDrawableHeight > 0) {
			w = mDrawableWidth;
			h = mDrawableHeight;
			if (w <= 0) w = 1;
			if (h <= 0) h = 1;

			// We are supposed to adjust view bounds to match the aspect
			// ratio of our drawable. See if that is possible.
			if (mAdjustViewBounds) {

				int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
				int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

				resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
				resizeHeight = heightSpecMode != MeasureSpec.EXACTLY;
				
//				Log.e(TAG, MeasureSpec.UNSPECIFIED + "");
//				Log.e(TAG, MeasureSpec.AT_MOST +  "");
//				Log.e(TAG, MeasureSpec.EXACTLY + "");

				
				desiredAspect = (float) w / (float) h;

//				Log.e(TAG, "onMeasure pass " + passLayout + " resizeWidth:"+resizeWidth + " resizeHeight:"+resizeHeight + " desiredAspect:" + desiredAspect + " widthSpecMode=" + widthSpecMode + " heightSpecMode=" + heightSpecMode);
			}
		}

		int pleft = getPaddingLeft();
		int pright = getPaddingRight();
		int ptop = getPaddingTop();
		int pbottom = getPaddingBottom();

		if (resizeWidth || resizeHeight) {
			/* If we get here, it means we want to resize to match the
			    drawables aspect ratio, and we have the freedom to change at
			    least one dimension. 
			 */

			// Get the max possible width given our constraints
			widthSize = resolveAdjustedSize(w + pleft + pright,
					mMaxWidth, widthMeasureSpec);
			
//			Log.e(TAG, "onMeasure pass " + passLayout + " temporary info for widthSize:" + MeasureSpec.getSize(widthMeasureSpec) + " vs " + w);

			// Get the max possible height given our constraints

			//TODO find a better fix
//			int specSize = MeasureSpec.getSize(MeasureSpec.getSize(heightMeasureSpec));
//			int height =  resolveSize(size, measureSpec)
			int specMode = MeasureSpec.getMode(heightMeasureSpec);
			if(!resizeHeight && specMode == MeasureSpec.EXACTLY){
				int fixedH = Math.max(h, getSuggestedMinimumHeight());
				int fixedSpecHeight = resolveSize(h, heightMeasureSpec);
//				Log.e(TAG, "onMeasure pass " + passLayout + " temporary fix for heightSize:" + fixedSpecHeight + " vs " + h);
				heightMeasureSpec = MeasureSpec.makeMeasureSpec(fixedSpecHeight, MeasureSpec.AT_MOST);
			}

			heightSize = resolveAdjustedSize(h + ptop + pbottom,
					mMaxHeight, heightMeasureSpec);

			if (desiredAspect != 0.0f) {
				// See what our actual aspect ratio is
				float actualAspect = (float) (widthSize - pleft - pright) /
						(heightSize - ptop - pbottom);

//				Log.e(TAG, "widthSize:"+widthSize + " heightSize:"+heightSize + " actualAspect:" + actualAspect +"  desiredAspect =" + desiredAspect);
				if (Math.abs(actualAspect - desiredAspect) > 0.0000001) {

					boolean done = false;

					// Try adjusting width to be proportional to height
					if (resizeWidth) {

						int newWidth = (int) (desiredAspect *
								(heightSize - ptop - pbottom))
								+ pleft + pright;

//						Log.e(TAG, "resizeWitdh | old width  = " + widthSize + " new width  = " + newWidth + " height =" + heightSize);
						//if (newWidth <= widthSize) {
						widthSize = newWidth;
						done = true;
						//}
					}

					// Try adjusting height to be proportional to width
					if (!done && resizeHeight) {
						int newHeight = (int) ((widthSize - pleft - pright)
								/ desiredAspect) + ptop + pbottom;
						//						Log.e(TAG, "resizeHeight  | old height  = " + heightSize + " new height  = " + newHeight + " actualAspect =" + actualAspect +" desiredRatio =" + desiredAspect + " width =" + widthSize);
						//if (newHeight <= heightSize) {
						heightSize = newHeight;
						//} 
					}
				}
			}



		} else {
			/* We are either don't want to preserve the drawables aspect ratio,
			   or we are not allowed to change view dimensions. Just measure in
			   the normal way.
			 */
//			Log.e(TAG, "onMeasure pass " + passLayout + " | don't preserve ratio widthMeasureSpec=" + widthMeasureSpec + " heightMeasureSpec " + heightMeasureSpec);
			w += pleft + pright;
			h += ptop + pbottom;

			w = Math.max(w, getSuggestedMinimumWidth());
			h = Math.max(h, getSuggestedMinimumHeight());

			widthSize = resolveSize(w, widthMeasureSpec);
			heightSize = resolveSize(h, heightMeasureSpec);
		}

//		Log.e(TAG, "onMeasure pass " + passLayout+ " | widthSize  = " + widthSize + " heightSize  = " + heightSize);
		setMeasuredDimension(widthSize, heightSize);

	}

	private int resolveAdjustedSize(int desiredSize, int maxSize,	int measureSpec) {
		int result = desiredSize;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);
		switch (specMode) {
		case MeasureSpec.UNSPECIFIED:
			/* Parent says we can be as big as we want. Just don't be larger
			than max size imposed on ourselves.
			 */
			result = Math.min(desiredSize, maxSize);
//			Log.e(TAG, "resolveAdjustedSize pass" + passLayout + " |  MeasureSpec.UNSPECIFIED  result = min(" + desiredSize + "," + maxSize + "=" +  result + ")");
			break;
		case MeasureSpec.AT_MOST:
			// Parent says we can be as big as we want, up to specSize. 
			// Don't be larger than specSize, and don't be larger than 
			// the max size imposed on ourselves.
			result = Math.min(Math.min(desiredSize, specSize), maxSize);
//			Log.e(TAG, "resolveAdjustedSize pass" + passLayout + " |  MeasureSpec.AT_MOST  result = min((" + desiredSize + "," + specSize + ")," + maxSize + ")=" +  result + ")");
			break;
		case MeasureSpec.EXACTLY:
			// No choice. Do what we are told.
			result = specSize;
//			Log.e(TAG, "resolveAdjustedSize pass" + passLayout + "  MeasureSpec.EXACTLY  result = " + result + " desired = " + desiredSize + " maxSize = " + maxSize);
			break;
		}
		return result;
	}
}
