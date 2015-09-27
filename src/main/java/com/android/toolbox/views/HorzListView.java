/*
 * Copyright (c) 2001 - 2012 Sileria, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package com.android.toolbox.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.Scroller;

import com.android.toolbox.Log;


/**
 * Finally a mixture of Gallery and a ListView that actually works like a
 * horizontal list view, and thank God without being "center-locked".
 * As usual it was too hard for google engineers to make a small flag that
 * will let the list be <code>center_locked</code> or <code>!center-locked</code>.
 * <p/>
 * This list is very similar to Gallery but uses a <code>ListAdapter</code> and not
 * the <code>SpinnerAdapater</code>.
 * 
 * @author Ahmed Shakil
 * @date 19-May-2012
 * @since API 8
 * @note Incomplete selection and selector work. (Might have some other minor bugs)
 */
public class HorzListView extends FriendAdapterView<ListAdapter> implements GestureDetector.OnGestureListener {

	private final static String TAG = HorzListView.class.getSimpleName();
	private int mHeightMeasureSpec;
	private int mWidthMeasureSpec;

	protected final Rect mListPadding = new Rect();

	private static final boolean localLOGV = false;

	/**
	 * Duration in milliseconds from the start of a scroll during which we're
	 * unsure whether the user is scrolling or flinging.
	 */
	private static final int SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT = 250;

	/**
	 * Horizontal spacing between items.
	 */
	private int mSpacing = 0;

	/**
	 * Indicates whether to use pixels-based or position-based scrollbar
	 * properties.
	 */
	private boolean mSmoothScrollbarEnabled = true;

	/**
	 * How long the transition animation should run when a child view changes
	 * position, measured in milliseconds.
	 */
	private int mAnimationDuration = 400;

	/**
	 * Left most edge of a child seen so far during layout.
	 */
	private int mLeftMost;

	/**
	 * Right most edge of a child seen so far during layout.
	 */
	private int mRightMost;

	/**
	 * Gravity for the children layout.
	 */
	private int mGravity;

	/**
	 * Helper for detecting touch gestures.
	 */
	private GestureDetector mGestureDetector;

	/**
	 * The position of the item that received the user's down touch.
	 */
	private int mDownTouchPosition;

	/**
	 * The view of the item that received the user's down touch.
	 */
	private View mDownTouchView;

	/**
	 * Executes the delta scrolls from a fling or scroll movement.
	 */
	private final FlingRunnable mFlingRunnable = new FlingRunnable();

	/**
	 * Sets mSuppressSelectionChanged = false. This is used to set it to false
	 * in the future. It will also trigger a selection changed.
	 */
	private Runnable mDisableSuppressSelectionChangedRunnable = new Runnable() {
		public void run () {
			mSuppressSelectionChanged = false;
			selectionChanged();
		}
	};

	/**
	 * When fling runnable runs, it resets this to false. Any method along the
	 * path until the end of its run() can set this to true to abort any
	 * remaining fling. For example, if we've reached either the leftmost or
	 * rightmost item, we will set this to true.
	 */
	private boolean mShouldStopFling;

	/**
	 * Whether to continuously callback on the item selected listener during a
	 * fling.
	 */
	private boolean mShouldCallbackDuringFling = true;

	/**
	 * Whether to callback when an item that is not selected is clicked.
	 */
	private boolean mShouldCallbackOnUnselectedItemClick = true;

	/**
	 * If true, do not callback to item selected listener.
	 */
	private boolean mSuppressSelectionChanged;

	private AdapterContextMenuInfo mContextMenuInfo;

	/**
	 * If true, this onScroll is the first for this user's drag (remember, a
	 * drag sends many onScrolls).
	 */
	private boolean mIsFirstScroll;

	/**
	 * If true, mFirstPosition is the position of the rightmost child, and
	 * the children are ordered right to left.
	 */
	private boolean mIsRtl = true;

	/**
	 * Constructor, default.
	 * @param context Activity context.
	 */
	public HorzListView (Context context) {
		this( context, null );
	}

	/**
	 * Constructor with attribute set.
	 * @param context Activity context.
	 */
	public HorzListView (Context context, AttributeSet attrs) {
		this( context, attrs, android.R.attr.galleryStyle );
	}

	/**
	 * Constructor with attribute set and default style.
	 * @param context Activity context.
	 */
	public HorzListView (Context context, AttributeSet attrs, int defStyle) {
		super( context, attrs, defStyle );

		//setSelector( getResources().getDrawable( android.R.drawable.list_selector_background ) );
		if(!isInEditMode()){
			mGestureDetector = new GestureDetector( context, this );
			mGestureDetector.setIsLongpressEnabled( true );
		}

		// We draw the selected item last (because otherwise the item to the
		// right overlaps it)
		//mGroupFlags |= FLAG_USE_CHILD_DRAWING_ORDER;

		//mGroupFlags |= FLAG_SUPPORT_STATIC_TRANSFORMATIONS;
	}

	/**
	 * Whether or not to callback on any {@link #getOnItemSelectedListener()}
	 * while the items are being flinged. If false, only the final selected item
	 * will cause the callback. If true, all items between the first and the
	 * final will cause callbacks.
	 *
	 * @param shouldCallback Whether or not to callback on the listener while
	 *                       the items are being flinged.
	 */
	public void setCallbackDuringFling (boolean shouldCallback) {
		mShouldCallbackDuringFling = shouldCallback;
	}

	/**
	 * Whether or not to callback when an item that is not selected is clicked.
	 * If false, the item will become selected (and re-centered). If true, the
	 * {@link #getOnItemClickListener()} will get the callback.
	 *
	 * @param shouldCallback Whether or not to callback on the listener when a
	 *                       item that is not selected is clicked.
	 */
	public void setCallbackOnUnselectedItemClick (boolean shouldCallback) {
		mShouldCallbackOnUnselectedItemClick = shouldCallback;
	}

	/**
	 * Sets how long the transition animation should run when a child view
	 * changes position. Only relevant if animation is turned on.
	 *
	 * @param animationDurationMillis The duration of the transition, in
	 *                                milliseconds.
	 * @attr ref android.R.styleable#Gallery_animationDuration
	 */
	public void setAnimationDuration (int animationDurationMillis) {
		mAnimationDuration = animationDurationMillis;
	}

	/**
	 * Sets the spacing between items in a Gallery
	 *
	 * @param spacing The spacing in pixels between items in the Gallery
	 * @attr ref android.R.styleable#Gallery_spacing
	 */
	public void setSpacing (int spacing) {
		mSpacing = spacing;
	}

	/**
	 * When smooth scrollbar is enabled, the position and size of the scrollbar thumb
	 * is computed based on the number of visible pixels in the visible items. This
	 * however assumes that all list items have the same height. If you use a list in
	 * which items have different heights, the scrollbar will change appearance as the
	 * user scrolls through the list. To avoid this issue, you need to disable this
	 * property.
	 * <p/>
	 * When smooth scrollbar is disabled, the position and size of the scrollbar thumb
	 * is based solely on the number of items in the adapter and the position of the
	 * visible items inside the adapter. This provides a stable scrollbar as the user
	 * navigates through a list of items with varying heights.
	 *
	 * @param enabled Whether or not to enable smooth scrollbar.
	 * @attr ref android.R.styleable#AbsListView_smoothScrollbar
	 * @see #setSmoothScrollbarEnabled(boolean)
	 */
	public void setSmoothScrollbarEnabled (boolean enabled) {
		mSmoothScrollbarEnabled = enabled;
	}

	/**
	 * Returns the current state of the fast scroll feature.
	 *
	 * @return True if smooth scrollbar is enabled is enabled, false otherwise.
	 * @see #setSmoothScrollbarEnabled(boolean)
	 */
	@ViewDebug.ExportedProperty
	public boolean isSmoothScrollbarEnabled () {
		return mSmoothScrollbarEnabled;
	}

	@Override
	protected int computeHorizontalScrollExtent () {
		final int count = getChildCount();
		if (count > 0) {
			if (mSmoothScrollbarEnabled) {
				int extent = count * 100;

				View view = getChildAt( 0 );
				final int left = view.getLeft();
				int width = view.getWidth();
				if (width > 0) {
					extent += (left * 100) / width;
				}

				view = getChildAt( count - 1 );
				final int rigth = view.getRight();
				width = view.getWidth();
				if (width > 0) {
					extent -= ((rigth - getWidth()) * 100) / width;
				}

				return extent;
			}
			else
				return 1;
		}
		return 0;
	}

	@Override
	protected int computeHorizontalScrollOffset () {
		final int firstPosition = mFirstPosition;
		final int childCount = getChildCount();
		if (firstPosition >= 0 && childCount > 0) {
			if (mSmoothScrollbarEnabled) {
				final View view = getChildAt( 0 );
				final int left = view.getLeft();
				int width = view.getWidth();
				if (width > 0) {
					return Math.max( firstPosition * 100 - (left * 100) / width + (int)((float)getScrollX() / getWidth() * mItemCount * 100), 0 );
				}
			}
			else {
				int index;
				final int count = mItemCount;
				if (firstPosition == 0) {
					index = 0;
				}
				else if (firstPosition + childCount == count) {
					index = count;
				}
				else {
					index = firstPosition + childCount / 2;
				}
				return (int)(firstPosition + childCount * (index / (float)count));
			}
		}
		return 0;
	}

	@Override
	protected int computeHorizontalScrollRange () {
		int result;
		if (mSmoothScrollbarEnabled) {
			result = Math.max( mItemCount * 100, 0 );
			if (getScrollX() != 0) {
				// Compensate for overscroll
				result += Math.abs( (int)((float)getScrollX() / getWidth() * mItemCount * 100) );
			}
		}
		else {
			result = mItemCount;
		}
		return result;
	}

	@Override
	protected boolean checkLayoutParams (ViewGroup.LayoutParams p) {
		return p instanceof LayoutParams;
	}

	@Override
	protected ViewGroup.LayoutParams generateLayoutParams (ViewGroup.LayoutParams p) {
		return new LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT );
	}

	@Override
	public ViewGroup.LayoutParams generateLayoutParams (AttributeSet attrs) {
		return new LayoutParams( getContext(), attrs );
	}

	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams () {
		return new LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT );
	}

	protected int getChildHeight (View child) {
		return child.getMeasuredHeight();
	}

	protected int getChildWidth (View child) {
		return child.getMeasuredWidth();
	}

	/**
	 * Tracks a motion scroll. In reality, this is used to do just about any
	 * movement to items (touch scroll, arrow-key scroll, set an item as selected).
	 *
	 * @param deltaX Change in X from the previous event.
	 */
	protected void trackMotionScroll (int deltaX) {
		if (getChildCount() == 0) return;

		boolean toLeft = deltaX < 0;

		int limitedDeltaX = getLimitedMotionScrollAmount( toLeft, deltaX );
		if (limitedDeltaX != deltaX) {
			// The above call returned a limited amount, so stop any scrolls/flings
			mFlingRunnable.endFling();
			onFinishedMovement();
		}

		offsetChildrenLeftAndRight( limitedDeltaX );

		detachOffScreenChildren( toLeft );

		if (toLeft) {
			// If moved left, there will be empty space on the right
			fillToGalleryRight();
		}
		else {
			// Similarly, empty space on the left
			fillToGalleryLeft();
		}

		// Clear unused views
		mRecycler.clear();

		updateSelection();
		updateSelector();

		onScrollChanged( 0, 0, 0, 0 ); // dummy values, View's implementation does not use these.

		invalidate();
	}

	protected int getLimitedMotionScrollAmount (boolean motionToLeft, int deltaX) {
		int extremeItemPosition = motionToLeft != mIsRtl ? mItemCount - 1 : 0;
		View extremeChild = getChildAt( extremeItemPosition - mFirstPosition );

		if (extremeChild == null) return deltaX;

		final int right = getWidth() - mListPadding.right;
		final int left = mListPadding.left;

		if (motionToLeft) {

			// The extreme child is past his boundary point!
			if (extremeChild.getRight() <= right) {
				if (getChildAt( 0 ).getLeft() >= left)   // list too small to scroll
					return 0;
				return right - extremeChild.getRight();
			}
		}
		else {
			// The extreme child is past his boundary point!
			if (extremeChild.getLeft() >= left)
				return left - extremeChild.getLeft();
		}

		int distance = distanceToView( extremeChild );
		return motionToLeft ? Math.max( distance, deltaX ) : Math.min( distance, deltaX );
	}

	private final Rect mTempRect = new Rect();

	/**
	 * Determine the distance to the nearest edge of a view in a particular
	 * direction.
	 *
	 * @param descendant A descendant of this list.
	 * @return The distance, or 0 if the nearest edge is already on screen.
	 */
	private int distanceToView (View descendant) {
		int distance = 0;
		descendant.getDrawingRect( mTempRect );
		offsetDescendantRectToMyCoords( descendant, mTempRect );
		final int listRight = getRight() - getLeft() - mListPadding.right;
		if (mTempRect.left < mListPadding.left) {
			distance = mListPadding.left - mTempRect.left;
		}
		else if (mTempRect.right > listRight) {
			distance = -(mTempRect.right - listRight);
		}
		return distance;
	}


	/**
	 * Jump directly to a specific item in the adapter data.
	 */
	public void setSelection (int position, boolean animate) {
		// Animate only if requested position is already on screen somewhere
		boolean shouldAnimate = animate && mFirstPosition <= position && position <= mFirstPosition + getChildCount() - 1;
		setSelectionInt( position, shouldAnimate );
	}

	@Override
	public void setSelection (int position) {
		setNextSelectedPositionInt( position );
		requestLayout();
		invalidate();
	}

	@Override
	public View getSelectedView () {
		if (mItemCount > 0 && mSelectedPosition >= 0) {
			return getChildAt( mSelectedPosition - mFirstPosition );
		}
		else {
			return null;
		}
	}

	/**
	 * Offset the horizontal location of all children of this view by the
	 * specified number of pixels.
	 *
	 * @param offset the number of pixels to offset
	 */
	private void offsetChildrenLeftAndRight (int offset) {
		for (int i = getChildCount() - 1; i >= 0; i--) {
			getChildAt( i ).offsetLeftAndRight( offset );
		}
	}

	/**
	 * Detaches children that are off the screen (i.e.: Gallery bounds).
	 *
	 * @param toLeft Whether to detach children to the left of the Gallery, or
	 *               to the right.
	 */
	private void detachOffScreenChildren (boolean toLeft) {
		int numChildren = getChildCount();
		int firstPosition = mFirstPosition;
		int start = 0;
		int count = 0;

		if (toLeft) {
			final int galleryLeft = getPaddingLeft();
			for (int i = 0; i < numChildren; i++) {
				int n = mIsRtl ? (numChildren - 1 - i) : i;
				final View child = getChildAt( n );
				if (child.getRight() >= galleryLeft) {
					break;
				}
				else {
					start = n;
					count++;
					mRecycler.put( firstPosition + n, child );
				}
			}
			if (!mIsRtl) {
				start = 0;
			}
		}
		else {
			final int galleryRight = getWidth() - getPaddingRight();
			for (int i = numChildren - 1; i >= 0; i--) {
				int n = mIsRtl ? numChildren - 1 - i : i;
				final View child = getChildAt( n );
				if (child.getLeft() <= galleryRight) {
					break;
				}
				else {
					start = n;
					count++;
					mRecycler.put( firstPosition + n, child );
				}
			}
			if (mIsRtl) {
				start = 0;
			}
		}

		detachViewsFromParent( start, count );

		if (toLeft != mIsRtl) {
			mFirstPosition += count;
		}
	}

	private void onFinishedMovement () {
		if (mSuppressSelectionChanged) {
			mSuppressSelectionChanged = false;

			// We haven't been callbacking during the fling, so do it now
			super.selectionChanged();
		}
		invalidate();
	}

	@Override
	protected void selectionChanged () {
		if (!mSuppressSelectionChanged) {
			super.selectionChanged();
		}
	}

	/**
	 * Looks for the child that is closest to the center and sets it as the
	 * selected child.
	 */
	private void updateSelection () {

		if (mSelectedChild == null) return;

		final int childrenLeft = mListPadding.left;
		final int childrenRight = getRight() - mListPadding.right;

		// Common case where the current selected position is correct
		View selView = mSelectedChild;

		final int selLeft = selView.getLeft();
		final int selRight = selView.getRight();

		if ((selLeft >= childrenLeft && selLeft < childrenRight) || selRight <= childrenRight && selRight > childrenLeft)
			return;

		final int center = selLeft + selView.getWidth() / 2;

		// todo better selection.

		//if (selLeft >= childrenLeft && selRight <= childrenRight) return;
		//final int center = selLeft ? selLeft + mSpacing : selRight - mSpacing;
		//final int center = leftIn ? selLeft - mSpacing - 1: selRight + mSpacing + 1;

		// FIXME: better search
		int closestEdgeDistance = Integer.MAX_VALUE;
		int newSelectedChildIndex = 0, childLeft, childRight;
		for (int i = getChildCount() - 1; i >= 0; i--) {

			View child = getChildAt( i );
			childLeft = child.getLeft();
			childRight = child.getRight();

			if (childLeft <= center && childRight >= center) {
				newSelectedChildIndex = i;  // This child is in the center
				break;
			}

			int childClosestEdgeDistance = Math.min( Math.abs( childLeft - center ), Math.abs( childRight - center ) );
			if (childClosestEdgeDistance < closestEdgeDistance) {
				closestEdgeDistance = childClosestEdgeDistance;
				newSelectedChildIndex = i;
			}
		}

		int newPos = mFirstPosition + newSelectedChildIndex;

		if (newPos != mSelectedPosition) {
			setSelectedPositionInt( newPos );
			setNextSelectedPositionInt( newPos );
			checkSelectionChanged();
		}
	}

	/**
	 * @see android.view.View#measure(int, int)
	 *      <p/>
	 *      Figure out the dimensions of this Spinner. The width comes from
	 *      the widthMeasureSpec as Spinnners can't have their width set to
	 *      UNSPECIFIED. The height is based on the height of the selected item
	 *      plus padding.
	 */
	@Override
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode( widthMeasureSpec );
		int widthSize;
		int heightSize;

		mListPadding.left = getPaddingLeft() > mSelectionLeftPadding ? getPaddingLeft() : mSelectionLeftPadding;
		mListPadding.top = getPaddingTop() > mSelectionTopPadding ? getPaddingTop() : mSelectionTopPadding;
		mListPadding.right = getPaddingRight() > mSelectionRightPadding ? getPaddingRight() : mSelectionRightPadding;
		mListPadding.bottom = getPaddingBottom() > mSelectionBottomPadding ? getPaddingBottom() : mSelectionBottomPadding;

		if (mDataChanged) {
			handleDataChanged();
		}

		int preferredHeight = 0;
		int preferredWidth = 0;
		boolean needsMeasuring = true;

		int selectedPosition = getSelectedItemPosition();
		if (selectedPosition >= 0 && mAdapter != null && selectedPosition < mAdapter.getCount()) {
			// Try looking in the recycler. (Maybe we were measured once already)
			View view = mRecycler.get( selectedPosition );
			if (view == null) {
				// Make a new one
				view = mAdapter.getView( selectedPosition, null, this );
			}

			if (view != null) {
				// Put in recycler for re-measuring and/or layout
				mRecycler.put( selectedPosition, view );
			}

			if (view != null) {
				if (view.getLayoutParams() == null) {
					mBlockLayoutRequests = true;
					view.setLayoutParams( generateDefaultLayoutParams() );
					mBlockLayoutRequests = false;
				}
				measureChild( view, widthMeasureSpec, heightMeasureSpec );

				preferredHeight = getChildHeight( view ) + mListPadding.top + mListPadding.bottom;
				preferredWidth = getChildWidth( view ) + mListPadding.left + mListPadding.right;

				needsMeasuring = false;
			}
		}

		if (needsMeasuring) {
			// No views -- just use padding
			preferredHeight = mListPadding.top + mListPadding.bottom;
			if (widthMode == MeasureSpec.UNSPECIFIED) {
				preferredWidth = mListPadding.left + mListPadding.right;
			}
		}

		preferredHeight = Math.max( preferredHeight, getSuggestedMinimumHeight() );
		preferredWidth = Math.max( preferredWidth, getSuggestedMinimumWidth() );

		heightSize = resolveSize( preferredHeight, heightMeasureSpec );
		widthSize = resolveSize( preferredWidth, widthMeasureSpec );
		//heightSize = resolveSizeAndState(preferredHeight, heightMeasureSpec, 0);
		//widthSize = resolveSizeAndState(preferredWidth, widthMeasureSpec, 0);

		setMeasuredDimension( widthSize, heightSize );
		mHeightMeasureSpec = heightMeasureSpec;
		mWidthMeasureSpec = widthMeasureSpec;
	}

	/**
	 * Creates and positions all views for this Gallery.
	 * <p/>
	 * We layout rarely, most of the time {@link #trackMotionScroll(int)} takes
	 * care of repositioning, adding, and removing children.
	 *
	 * @param delta Change in the selected position. +1 means the selection is
	 *              moving to the right, so views are scrolling to the left. -1
	 *              means the selection is moving to the left.
	 */
	@Override
	protected void layoutChildren (int delta, boolean animate, boolean changed) {

		mIsRtl = false;///isLayoutRtl();

		if (mDataChanged)
			handleDataChanged();

		// Handle an empty gallery by removing all views.
		if (mItemCount == 0) {
			resetList();
			return;
		}

		int leftOffset = mListPadding.left;

		// Update selection.
		if (mNextSelectedPosition >= 0) {
			View view = getSelectedView();
			if (view != null)
				leftOffset = view.getLeft();                   // Restore the previous scroll position.
			setSelectedPositionInt( mNextSelectedPosition );   // Update to the new selected position.
		}

		// All views go in recycler while we are in layout
		recycleAllViews();

		// Clear out old views
		// removeAllViewsInLayout();
		detachAllViewsFromParent();

		// These will be used to give initial positions to views entering the gallery as we scroll
		mRightMost = 0;
		mLeftMost = 0;

		// Make selected view and center it

		/*
		 * mFirstPosition will be decreased as we add views to the left later
		 * on. The 0 for x will be offset in a couple lines down.
		 */
		mFirstPosition = mSelectedPosition;
		View sel = makeAndAddView( mSelectedPosition, 0, leftOffset, true );

		// fill
		fillToGalleryRight();
		fillToGalleryLeft();

		// Flush any cached views that did not get reused above
		mRecycler.clear();

		invalidate();
		checkSelectionChanged();

		mDataChanged = false;
		mNeedSync = false;
		setNextSelectedPositionInt( mSelectedPosition );

		updateSelectedItemMetadata();
	}

	private void fixSelection (View sel) {

		final int childrenLeft = mListPadding.left;
		final int childrenRight = mListPadding.right;

		if (mSelectedPosition == 0) {
			if (sel.getLeft() > childrenLeft)
				sel.offsetLeftAndRight( childrenLeft - sel.getLeft() );
		}
		else if (mSelectedPosition == mItemCount - 1) {
			if (sel.getRight() < childrenRight)
				sel.offsetLeftAndRight( childrenRight - sel.getRight() );
		}
	}

	private void fillToGalleryLeftRtl () {
		int itemSpacing = mSpacing;
		int galleryLeft = getPaddingLeft();
		int numChildren = getChildCount();

		// Set state for initial iteration
		View prevIterationView = getChildAt( numChildren - 1 );
		int curPosition;
		int curRightEdge;

		if (prevIterationView != null) {
			curPosition = mFirstPosition + numChildren;
			curRightEdge = prevIterationView.getLeft() - itemSpacing;
		}
		else {
			// No children available!
			mFirstPosition = curPosition = mItemCount - 1;
			curRightEdge = getRight() - getLeft() - getPaddingRight();
			mShouldStopFling = true;
		}

		while (curRightEdge > galleryLeft && curPosition < mItemCount) {
			prevIterationView = makeAndAddView( curPosition, curPosition - mSelectedPosition, curRightEdge, false );

			// Set state for next iteration
			curRightEdge = prevIterationView.getLeft() - itemSpacing;
			curPosition++;
		}
	}

	private void fillToGalleryLeft () {
		if (mIsRtl)
			fillToGalleryLeftRtl();
		else
			fillToGalleryLeftLtr();
	}


	private void fillToGalleryRight () {
		if (mIsRtl)
			fillToGalleryRightRtl();
		else
			fillToGalleryRightLtr();
	}

	private void fillToGalleryLeftLtr () {
		int itemSpacing = mSpacing;
		int galleryLeft = getPaddingLeft();

		// Set state for initial iteration
		View prevIterationView = getChildAt( 0 );
		int curPosition;
		int curRightEdge;

		if (prevIterationView != null) {
			curPosition = mFirstPosition - 1;
			curRightEdge = prevIterationView.getLeft() - itemSpacing;
		}
		else {
			// No children available!
			curPosition = 0;
			curRightEdge = getRight() - getLeft() - getPaddingRight();
			mShouldStopFling = true;
		}

		while (curRightEdge > galleryLeft && curPosition >= 0) {
			prevIterationView = makeAndAddView( curPosition, curPosition - mSelectedPosition, curRightEdge, false );

			// Remember some state
			mFirstPosition = curPosition;

			// Set state for next iteration
			curRightEdge = prevIterationView.getLeft() - itemSpacing;
			curPosition--;
		}
	}

	private void fillToGalleryRightRtl () {
		int itemSpacing = mSpacing;
		int galleryRight = getRight() - getLeft() - getPaddingRight();

		// Set state for initial iteration
		View prevIterationView = getChildAt( 0 );
		int curPosition;
		int curLeftEdge;

		if (prevIterationView != null) {
			curPosition = mFirstPosition - 1;
			curLeftEdge = prevIterationView.getRight() + itemSpacing;
		}
		else {
			curPosition = 0;
			curLeftEdge = getPaddingLeft();
			mShouldStopFling = true;
		}

		while (curLeftEdge < galleryRight && curPosition >= 0) {
			prevIterationView = makeAndAddView( curPosition, curPosition - mSelectedPosition, curLeftEdge, true );

			// Remember some state
			mFirstPosition = curPosition;

			// Set state for next iteration
			curLeftEdge = prevIterationView.getRight() + itemSpacing;
			curPosition--;
		}
	}

	private void fillToGalleryRightLtr () {
		int itemSpacing = mSpacing;
		int galleryRight = getRight() - getLeft() - getPaddingRight();
		int numChildren = getChildCount();
		int numItems = mItemCount;

		// Set state for initial iteration
		View prevIterationView = getChildAt( numChildren - 1 );
		int curPosition;
		int curLeftEdge;

		if (prevIterationView != null) {
			curPosition = mFirstPosition + numChildren;
			curLeftEdge = prevIterationView.getRight() + itemSpacing;
		}
		else {
			mFirstPosition = curPosition = mItemCount - 1;
			curLeftEdge = getPaddingLeft();
			mShouldStopFling = true;
		}

		while (curLeftEdge < galleryRight && curPosition < numItems) {
			prevIterationView = makeAndAddView( curPosition, curPosition - mSelectedPosition, curLeftEdge, true );

			// Set state for next iteration
			curLeftEdge = prevIterationView.getRight() + itemSpacing;
			curPosition++;
		}
	}

	/**
	 * Obtain a view, either by pulling an existing view from the recycler or by
	 * getting a new one from the adapter. If we are animating, make sure there
	 * is enough information in the view's layout parameters to animate from the
	 * old to new positions.
	 *
	 * @param position Position in the gallery for the view to obtain
	 * @param offset   Offset from the selected position
	 * @param x		X-coordinate indicating where this view should be placed. This
	 *                 will either be the left or right edge of the view, depending on
	 *                 the fromLeft parameter
	 * @param fromLeft Are we positioning views based on the left edge? (i.e.,
	 *                 building from left to right)?
	 * @return A view that has been added to the gallery
	 */
	private View makeAndAddView (int position, int offset, int x, boolean fromLeft) {

		View child;
		if (!mDataChanged) {
			child = mRecycler.get( position );
			if (child != null) {
				// Can reuse an existing view
				int childLeft = child.getLeft();

				// Remember left and right edges of where views have been placed
				mRightMost = Math.max( mRightMost, childLeft + child.getMeasuredWidth() );
				mLeftMost = Math.min( mLeftMost, childLeft );

				// Position the view
				setUpChild( child, offset, x, fromLeft );

				return child;
			}
		}

		// Nothing found in the recycler -- ask the adapter for a view
		child = mAdapter.getView( position, null, this );

		// Position the view
		setUpChild( child, offset, x, fromLeft );

		return child;
	}

	/**
	 * Helper for makeAndAddView to set the position of a view and fill out its
	 * layout parameters.
	 *
	 * @param child	The view to position
	 * @param offset   Offset from the selected position
	 * @param x		X-coordinate indicating where this view should be placed. This
	 *                 will either be the left or right edge of the view, depending on
	 *                 the fromLeft parameter
	 * @param fromLeft Are we positioning views based on the left edge? (i.e.,
	 *                 building from left to right)?
	 */
	private void setUpChild (View child, int offset, int x, boolean fromLeft) {

		// Respect layout params that are already in the view. Otherwise
		// make some up...
		LayoutParams lp = (LayoutParams)child.getLayoutParams();
		if (lp == null) {
			lp = (LayoutParams)generateDefaultLayoutParams();
		}

		addViewInLayout( child, fromLeft != mIsRtl ? -1 : 0, lp );

		child.setSelected( offset == 0 );

		// Get measure specs
		int childHeightSpec = ViewGroup.getChildMeasureSpec( mHeightMeasureSpec, mListPadding.top + mListPadding.bottom, lp.height );
		int childWidthSpec = ViewGroup.getChildMeasureSpec( mWidthMeasureSpec, mListPadding.left + mListPadding.right, lp.width );

		// Measure child
		child.measure( childWidthSpec, childHeightSpec );

		int childLeft;
		int childRight;

		// Position vertically based on gravity setting
		int childTop = calculateTop( child, true );
		int childBottom = childTop + child.getMeasuredHeight();

		int width = child.getMeasuredWidth();
		if (fromLeft) {
			childLeft = x;
			childRight = childLeft + width;
		}
		else {
			childLeft = x - width;
			childRight = x;
		}

		child.layout( childLeft, childTop, childRight, childBottom );
	}

	/**
	 * Figure out vertical placement based on mGravity
	 *
	 * @param child Child to place
	 * @return Where the top of the child should be
	 */
	private int calculateTop (View child, boolean duringLayout) {
		int myHeight = duringLayout ? getMeasuredHeight() : getHeight();
		int childHeight = duringLayout ? child.getMeasuredHeight() : child.getHeight();

		int childTop = 0;

		switch (mGravity) {
			case Gravity.TOP:
				childTop = mListPadding.top;
				break;
			case Gravity.CENTER_VERTICAL:
				int availableSpace = myHeight - mListPadding.bottom - mListPadding.top - childHeight;
				childTop = mListPadding.top + (availableSpace / 2);
				break;
			case Gravity.BOTTOM:
				childTop = myHeight - mListPadding.bottom - childHeight;
				break;
		}
		return childTop;
	}

	private boolean dispatchLongPress (View view, int position, long id) {
		boolean handled = false;

		if (getOnItemLongClickListener() != null) {
			handled = getOnItemLongClickListener().onItemLongClick( this, mDownTouchView, mDownTouchPosition, id );
		}

		if (!handled) {
			mContextMenuInfo = new AdapterContextMenuInfo( view, position, id );
			handled = super.showContextMenuForChild( this );
		}

		if (handled) {
			performHapticFeedback( HapticFeedbackConstants.LONG_PRESS );
		}

		return handled;
	}

	/**
	 * Steal all key events.
	 */
	@Override
	public boolean dispatchKeyEvent (KeyEvent event) {
		return event.dispatch( this, null, null );
	}

	@Override
	public boolean onKeyDown (int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				if (movePrevious()) {
					playSoundEffect( SoundEffectConstants.NAVIGATION_LEFT );
					return true;
				}
				return false;

			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (moveNext()) {
					playSoundEffect( SoundEffectConstants.NAVIGATION_RIGHT );
					return true;
				}
				return false;
		}

		return super.onKeyDown( keyCode, event );
	}

	@Override
	public boolean onTouchEvent (MotionEvent event) {
//		Log.e(TAG, "onTouchEvent " + event.getAction());
		// Give everything to the gesture detector
		boolean retValue = false;
		if(event==null){
			Log.e(TAG, "[onTouchEvent] event is null, should not happen" );
			return retValue;
		}
		if(mGestureDetector!=null)
		{
			//Log.e(TAG, "[onTouchEvent] gestureDetector event : " + event.getAction() );
			try{
				retValue = mGestureDetector.onTouchEvent( event );
				//Log.e(TAG, "[onTouchEvent] gestureDetector retvalue " + retValue );
			}
			catch(Exception e){
				Log.e(TAG, "[onTouchEvent] gestureDetector exception " + e.getMessage() );
				return false;
			}
		}

		switch( event.getAction() ) {
			case MotionEvent.ACTION_UP:
				onUp(); // Helper method for lifted finger
				break;

			case MotionEvent.ACTION_CANCEL:
				onCancel();
				break;

		}

		return retValue;
	}

	private boolean _performScrollOnTap = true;
	public void setPerformScrollOnTap(boolean performScrollOnTap){
		_performScrollOnTap = performScrollOnTap;
	}
	public boolean onSingleTapUp (MotionEvent e) {

		if (mDownTouchPosition >= 0) {

			// An item tap should make it selected, so scroll to this child.
			if(_performScrollOnTap) scrollToSelect( mDownTouchPosition - mFirstPosition );

			// Also pass the click so the client knows, if it wants to.
			if (mShouldCallbackOnUnselectedItemClick || mDownTouchPosition == mSelectedPosition) {
				performItemClick( mDownTouchView, mDownTouchPosition, mAdapter.getItemId( mDownTouchPosition ) );
			}

			return true;
		}

		return false;
	}

	
	public boolean onFling (MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

		if (!mShouldCallbackDuringFling) {
			// We want to suppress selection changes

			// Remove any future code to set mSuppressSelectionChanged = false
			removeCallbacks( mDisableSuppressSelectionChangedRunnable );

			// This will get reset once we scroll into slots
			if (!mSuppressSelectionChanged) mSuppressSelectionChanged = true;
		}

		// Fling the gallery!
		mFlingRunnable.startUsingVelocity( (int)-velocityX );

		return true;
	}

	public boolean onScroll (MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

		if (localLOGV) Log.v( TAG, String.valueOf( e2.getX() - e1.getX() ) );

		/*
		 * Now's a good time to tell our parent to stop intercepting our events!
		 * The user has moved more than the slop amount, since GestureDetector
		 * ensures this before calling this method. Also, if a parent is more
		 * interested in this touch's events than we are, it would have
		 * intercepted them by now (for example, we can assume when a Gallery is
		 * in the ListView, a vertical scroll would not end up in this method
		 * since a ListView would have intercepted it by now).
		 */
		getParent().requestDisallowInterceptTouchEvent( true );

		// As the user scrolls, we want to callback selection changes so related-
		// info on the screen is up-to-date with the gallery's selection
		if (!mShouldCallbackDuringFling) {
			if (mIsFirstScroll) {
				/*
				 * We're not notifying the client of selection changes during
				 * the fling, and this scroll could possibly be a fling. Don't
				 * do selection changes until we're sure it is not a fling.
				 */
				if (!mSuppressSelectionChanged) mSuppressSelectionChanged = true;
				postDelayed( mDisableSuppressSelectionChangedRunnable, SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT );
			}
		}
		else {
			if (mSuppressSelectionChanged) mSuppressSelectionChanged = false;
		}

		// Track the motion
		trackMotionScroll( -1 * (int)distanceX );

		mIsFirstScroll = false;
		return true;
	}

	public boolean onDown (MotionEvent e) {

		// Kill any existing fling/scroll
		mFlingRunnable.stop();

		// Get the item's view that was touched
		mDownTouchPosition = pointToPosition( (int)e.getX(), (int)e.getY() );

		if (mDownTouchPosition >= 0) {
			mDownTouchView = getChildAt( mDownTouchPosition - mFirstPosition );
			mDownTouchView.setPressed( true );
		}

		// Reset the multiple-scroll tracking state
		mIsFirstScroll = true;

		// Must return true to get matching events for this down event.
		return true;
	}

	/**
	 * Called when a touch event's action is MotionEvent.ACTION_UP.
	 */
	protected void onUp () {
		dispatchUnpress();
	}

	/**
	 * Called when a touch event's action is MotionEvent.ACTION_CANCEL.
	 */
	protected void onCancel () {
		onUp();
	}

	public void onLongPress (MotionEvent e) {

		if (mDownTouchPosition < 0) {
			return;
		}

		performHapticFeedback( HapticFeedbackConstants.LONG_PRESS );
		long id = getItemIdAtPosition( mDownTouchPosition );
		dispatchLongPress( mDownTouchView, mDownTouchPosition, id );
	}

	// Unused methods from GestureDetector.OnGestureListener below

	public void onShowPress (MotionEvent e) {
	}

	/**
	 * Temporary frame to hold a child View's frame rectangle
	 */
	private Rect mTouchFrame;

	/**
	 * Maps a point to a position in the list.
	 *
	 * @param x X in local coordinate
	 * @param y Y in local coordinate
	 * @return The position of the item which contains the specified point, or
	 *         {@link #INVALID_POSITION} if the point does not intersect an item.
	 */
	public int pointToPosition (int x, int y) {
		Rect frame = mTouchFrame;
		if (frame == null) {
			mTouchFrame = new Rect();
			frame = mTouchFrame;
		}

		final int count = getChildCount();
		for (int i = count - 1; i >= 0; i--) {
			View child = getChildAt( i );
			if (child.getVisibility() == View.VISIBLE) {
				child.getHitRect( frame );
				if (frame.contains( x, y )) {
					return mFirstPosition + i;
				}
			}
		}
		return INVALID_POSITION;
	}

	// Unused methods from GestureDetector.OnGestureListener above

	@Override
	protected ContextMenu.ContextMenuInfo getContextMenuInfo () {
		return mContextMenuInfo;
	}

	@Override
	public boolean showContextMenuForChild (View originalView) {

		final int longPressPosition = getPositionForView( originalView );
		if (longPressPosition < 0) {
			return false;
		}

		final long longPressId = mAdapter.getItemId( longPressPosition );
		return dispatchLongPress( originalView, longPressPosition, longPressId );
	}

	@Override
	public boolean showContextMenu () {

		if (isPressed() && mSelectedPosition >= 0) {
			int index = mSelectedPosition - mFirstPosition;
			View v = getChildAt( index );
			return dispatchLongPress( v, mSelectedPosition, mSelectedRowId );
		}

		return false;
	}

	protected boolean movePrevious () {
		if (mItemCount > 0 && mSelectedPosition > 0) {
			scrollToSelect( mSelectedPosition - mFirstPosition - 1 );
			return true;
		}
		else {
			return false;
		}
	}

	protected boolean moveNext () {
		if (mItemCount > 0 && mSelectedPosition < mItemCount - 1) {
			scrollToSelect( mSelectedPosition - mFirstPosition + 1 );
			return true;
		}
		else {
			return false;
		}
	}

	private boolean scrollToSelect (int childIndex) {

		// check the actual selection against available children
		int selection = childIndex + mFirstPosition;

		// if the selection is not visible then call setselection
		if (selection < mFirstPosition || selection>=mFirstPosition + getChildCount()) {
			setSelectionInt( selection, true );
			return true;
		}
		// otherwise scroll selection to visible child

		View child = getChildAt( childIndex );

		final int right = getWidth() - mListPadding.bottom;
		final int left = mListPadding.left;

		setSelectedPositionInt( selection );

		if (child.getLeft() >= left && child.getRight() <= right)
			return false;

		mFlingRunnable.startUsingDistance( distanceToView( child ) );
		return true;
	}

	/**
	 * Describes how the child views are aligned.
	 *
	 * @attr ref android.R.styleable#Gallery_gravity
	 */
	public void setGravity (int gravity) {
		if (mGravity != gravity) {
			mGravity = gravity;
			requestLayout();
		}
	}

	@Override
	protected int getChildDrawingOrder (int childCount, int i) {
		int selectedIndex = mSelectedPosition - mFirstPosition;

		// Just to be safe
		if (selectedIndex < 0) return i;

		if (i == childCount - 1) {
			// Draw the selected child last
			return selectedIndex;
		}
		else if (i >= selectedIndex) {
			// Move the children after the selected child earlier one
			return i + 1;
		}
		else {
			// Keep the children before the selected child the same
			return i;
		}
	}

	/**
	 * Responsible for fling behavior. Use {@link #startUsingVelocity(int)} to
	 * initiate a fling. Each frame of the fling is handled in {@link #run()}.
	 * A FlingRunnable will keep re-posting itself until the fling is done.
	 */
	private class FlingRunnable implements Runnable {

		/**
		 * Tracks the decay of a fling scroll
		 */
		private Scroller mScroller;

		/**
		 * X value reported by mScroller on the previous fling
		 */
		private int mLastFlingX;

		public FlingRunnable () {
			mScroller = new Scroller( getContext() );
		}

		private void startCommon () {
			// Remove any pending flings
			removeCallbacks( this );
		}

		public void startUsingVelocity (int initialVelocity) {
			if (initialVelocity == 0) return;

			startCommon();

			int initialX = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
			mLastFlingX = initialX;
			mScroller.fling( initialX, 0, initialVelocity, 0, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE );
			post( this );
		}

		public void startUsingDistance (int distance) {
			if (distance == 0) return;

			startCommon();

			mLastFlingX = 0;
			mScroller.startScroll( 0, 0, -distance, 0, mAnimationDuration );
			post( this );
		}

		public void stop () {
			removeCallbacks( this );
			endFling();
		}

		private void endFling () {
			//Force the scroller's status to finished (without setting its position to the end)
			mScroller.forceFinished( true );
		}

		public void run () {

			if (mItemCount == 0) {
				endFling();
				return;
			}

			mShouldStopFling = false;

			final Scroller scroller = mScroller;
			boolean more = scroller.computeScrollOffset();
			final int x = scroller.getCurrX();

			// Flip sign to convert finger direction to list items direction
			// (e.g. finger moving down means list is moving towards the top)
			int delta = mLastFlingX - x;

			// Pretend that each frame of a fling scroll is a touch scroll
			if (delta > 0) {
				// Moving towards the left. Use leftmost view as mDownTouchPosition
				mDownTouchPosition = mIsRtl ? (mFirstPosition + getChildCount() - 1) : mFirstPosition;

				// Don't fling more than 1 screen
				delta = Math.min( getWidth() - getPaddingLeft() - getPaddingRight() - 1, delta );
			}
			else {
				// Moving towards the right. Use rightmost view as mDownTouchPosition
				int offsetToLast = getChildCount() - 1;
				mDownTouchPosition = mIsRtl ? mFirstPosition : (mFirstPosition + getChildCount() - 1);

				// Don't fling more than 1 screen
				delta = Math.max( -(getWidth() - getPaddingRight() - getPaddingLeft() - 1), delta );
			}

			trackMotionScroll( delta );

			if (more && !mShouldStopFling) {
				mLastFlingX = x;
				post( this );
			}
			else {
				endFling();
			}
		}

	}

	/**
	 * Gallery extends LayoutParams to provide a place to hold current
	 * Transformation information along with previous position/transformation
	 * info.
	 */
	public static class LayoutParams extends ViewGroup.LayoutParams {

		public LayoutParams (Context c, AttributeSet attrs) {
			super( c, attrs );
		}

		public LayoutParams (int w, int h) {
			super( w, h );
		}

		public LayoutParams (ViewGroup.LayoutParams source) {
			super( source );
		}
	}
}
