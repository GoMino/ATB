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
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewDebug;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AbsSpinner;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * A derivation of {@link android.widget.AdapterView} which was needed to make
 * members accessable to sub-classes. I wish there were engineers with proper
 * knowledge of OO and best practices who were given responsibility to write this code.
 *
 * @author Ahmed Shakil
 * @since API 8
 */
public abstract class FriendAdapterView<T extends Adapter> extends AdapterView<T> {

	protected T mAdapter;

	/**
	 * The drawable used to draw the selector
	 */
	private Drawable mSelector;

	/**
	 * Indicates whether the list selector should be drawn on top of the children or behind
	 */
	private boolean mDrawSelectorOnTop = false;

	/**
	 * The current position of the selector in the list.
	 */
	private int mSelectorPosition = INVALID_POSITION;

	/**
	 * The select child's view (from the adapter's getView) is enabled.
	 */
	private boolean mIsChildViewEnabled;

	/**
	 * The currently selected item's child.
	 */
	protected View mSelectedChild;

	protected int mSelectionLeftPadding = 0;
	protected int mSelectionTopPadding = 0;
	protected int mSelectionRightPadding = 0;
	protected int mSelectionBottomPadding = 0;

	/**
	 * Defines the selector's location and dimension at drawing time
	 */
	private Rect mSelectorRect = new Rect();

	private DataSetObserver mDataSetObserver;

	/**
	 * The position of the first child displayed
	 */
	@ViewDebug.ExportedProperty(category = "scrolling")
	protected int mFirstPosition = 0;

	/**
	 * The offset in pixels from the top of the AdapterView to the top
	 * of the view to select during the next layout.
	 */
	protected int mSpecificTop;

	/**
	 * Position from which to start looking for mSyncRowId
	 */
	protected int mSyncPosition;

	/**
	 * Row id to look for when data has changed
	 */
	protected long mSyncRowId = INVALID_ROW_ID;

	/**
	 * Height of the view when mSyncPosition and mSyncRowId where set
	 */
	protected long mSyncHeight;

	/**
	 * True if we need to sync to mSyncRowId
	 */
	protected boolean mNeedSync = false;

	/**
	 * Indicates whether to sync based on the selection or position. Possible
	 * values are {@link #SYNC_SELECTED_POSITION} or
	 * {@link #SYNC_FIRST_POSITION}.
	 */
	protected int mSyncMode;

	/**
	 * Sync based on the selected child
	 */
	protected static final int SYNC_SELECTED_POSITION = 0;

	/**
	 * Sync based on the first child displayed
	 */
	protected static final int SYNC_FIRST_POSITION = 1;

	/**
	 * Maximum amount of time to spend in {@link #findSyncPosition()}
	 */
	protected static final int SYNC_MAX_DURATION_MILLIS = 100;

	/**
	 * Indicates that this view is currently being laid out.
	 */
	protected boolean mInLayout = false;

	/**
	 * True if the data has changed since the last layout
	 */
	protected boolean mDataChanged;

	/**
	 * The position within the adapter's data set of the item to select
	 * during the next layout.
	 */
	@ViewDebug.ExportedProperty(category = "list")
	protected int mNextSelectedPosition = INVALID_POSITION;

	/**
	 * The item id of the item to select during the next layout.
	 */
	protected long mNextSelectedRowId = INVALID_ROW_ID;

	/**
	 * The position within the adapter's data set of the currently selected item.
	 */
	@ViewDebug.ExportedProperty(category = "list")
	protected int mSelectedPosition = INVALID_POSITION;

	/**
	 * The item id of the currently selected item.
	 */
	protected long mSelectedRowId = INVALID_ROW_ID;

	/**
	 * Our height after the last layout
	 */
	private int mLayoutHeight;

	/**
	 * The number of items in the current adapter.
	 */
	@ViewDebug.ExportedProperty(category = "list")
	protected int mItemCount;

	/**
	 * The number of items in the adapter before a data changed event occurred.
	 */
	protected int mOldItemCount;

	/**
	 * The last selected position we used when notifying
	 */
	protected int mOldSelectedPosition = INVALID_POSITION;

	/**
	 * The id of the last selected position we used when notifying
	 */
	protected long mOldSelectedRowId = INVALID_ROW_ID;

	/**
	 * Indicates what focusable state is requested when calling setFocusable().
	 * In addition to this, this view has other criteria for actually
	 * determining the focusable state (such as whether its empty or the text
	 * filter is shown).
	 *
	 * @see #setFocusable(boolean)
	 * @see #checkFocus()
	 */
	private boolean mDesiredFocusableState;
	private boolean mDesiredFocusableInTouchModeState;

	/**
	 * If true, we have received the "invoke" (center or enter buttons) key
	 * down. This is checked before we action on the "invoke" key up, and is
	 * subsequently cleared.
	 */
	private boolean mReceivedInvokeKeyDown;

	private SelectionNotifier mSelectionNotifier;

	/**
	 * When set to true, calls to requestLayout() will not propagate up the parent hierarchy.
	 * This is used to layout the children during a layout pass.
	 */
	protected boolean mBlockLayoutRequests = false;

	protected final RecycleBin mRecycler = new RecycleBin();

	public FriendAdapterView (Context context) {
		super( context );
		init();
	}

	public FriendAdapterView (Context context, AttributeSet attrs) {
		super( context, attrs );
		init();
	}

	public FriendAdapterView (Context context, AttributeSet attrs, int defStyle) {
		super( context, attrs, defStyle );
		init();
	}

	protected void init () {
		setClickable( true );
		setFocusableInTouchMode( true );    // also sets the focusable property to true
		setWillNotDraw( false );
		setAlwaysDrawnWithCacheEnabled( false );
	}

	/**
	 * Return the position of the currently selected item within the adapter's data set
	 *
	 * @return int Position (starting at 0), or {@link #INVALID_POSITION} if there is nothing selected.
	 */
	@ViewDebug.CapturedViewProperty
	public int getSelectedItemPosition () {
		return mNextSelectedPosition;
	}

	/**
	 * @return The id corresponding to the currently selected item, or {@link #INVALID_ROW_ID}
	 *         if nothing is selected.
	 */
	public long getSelectedItemId () {
		return mNextSelectedRowId;
	}

	/**
	 * @return The view corresponding to the currently selected item, or null
	 *         if nothing is selected
	 */
	public abstract View getSelectedView ();

	/**
	 * @return The data corresponding to the currently selected item, or
	 *         null if there is nothing selected.
	 */
	public Object getSelectedItem () {
		T adapter = getAdapter();
		int selection = getSelectedItemPosition();
		if (adapter != null && adapter.getCount() > 0 && selection >= 0) {
			return adapter.getItem( selection );
		}
		else {
			return null;
		}
	}

	/**
	 * @return The number of items owned by the Adapter associated with this
	 *         AdapterView. (This is the number of data items, which may be
	 *         larger than the number of visible views.)
	 */
	public int getCount () {
		return mItemCount;
	}

	/**
	 * Get the position within the adapter's data set for the view, where view is a an adapter item
	 * or a descendant of an adapter item.
	 *
	 * @param view an adapter item, or a descendant of an adapter item. This must be visible in this
	 *             AdapterView at the time of the call.
	 * @return the position within the adapter's data set of the view, or {@link #INVALID_POSITION}
	 *         if the view does not correspond to a list item (or it is not currently visible).
	 */
	public int getPositionForView (View view) {
		View listItem = view;
		try {
			View v;
			while (!(v = (View)listItem.getParent()).equals( this )) {
				listItem = v;
			}
		}
		catch (ClassCastException e) {
			// We made it up to the window without find this list view
			return INVALID_POSITION;
		}

		// Search the children for the list item
		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			if (getChildAt( i ).equals( listItem )) {
				return mFirstPosition + i;
			}
		}

		// Child not found!
		return INVALID_POSITION;
	}

	/**
	 * Returns the position within the adapter's data set for the first item
	 * displayed on screen.
	 *
	 * @return The position within the adapter's data set
	 */
	public int getFirstVisiblePosition () {
		return mFirstPosition;
	}

	/**
	 * Returns the position within the adapter's data set for the last item
	 * displayed on screen.
	 *
	 * @return The position within the adapter's data set
	 */
	public int getLastVisiblePosition () {
		return mFirstPosition + getChildCount() - 1;
	}

	@Override
	protected void onFocusChanged (boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged( gainFocus, direction, previouslyFocusedRect );

		/*
		 * The gallery shows focus by focusing the selected item. So, give
		 * focus to our selected item instead. We steal keys from our
		 * selected item elsewhere.
		 */
		if (gainFocus && mSelectedChild != null) {
			mSelectedChild.requestFocus( direction );
			mSelectedChild.setSelected( true );
		}

	}
	protected void checkFocus () {
		final T adapter = getAdapter();
		final boolean focusable = !(adapter == null || adapter.getCount() == 0);
		// The order in which we set focusable in touch mode/focusable may matter
		// for the client, see View.setFocusableInTouchMode() comments for more
		// details
		super.setFocusableInTouchMode( focusable && mDesiredFocusableInTouchModeState );
		super.setFocusable( focusable && mDesiredFocusableState );
		if (getEmptyView() != null) {
			updateEmptyStatus( (adapter == null) || adapter.isEmpty() );
		}
	}

	@Override
	protected void onLayout (boolean changed, int l, int t, int r, int b) {

		mLayoutHeight = getHeight();

		super.onLayout( changed, l, t, r, b );

		// Remember that we are in layout to prevent more layout request from being generated.
		mInLayout = true;
		layoutChildren( 0, false, changed );
		mInLayout = false;
	}


	protected abstract void layoutChildren (int delta, boolean animate, boolean changed);

	/**
	 * Override to prevent spamming ourselves with layout requests
	 * as we place views
	 *
	 * @see android.view.View#requestLayout()
	 */
	@Override
	public void requestLayout () {
		if (!mBlockLayoutRequests) {
			super.requestLayout();
		}
	}

	/**
	 * Controls whether the selection highlight drawable should be drawn on top of the item or
	 * behind it.
	 *
	 * @param onTop If true, the selector will be drawn on the item it is highlighting. The default
	 *              is false.
	 * @attr ref android.R.styleable#AbsListView_drawSelectorOnTop
	 */
	public void setDrawSelectorOnTop (boolean onTop) {
		mDrawSelectorOnTop = onTop;
	}

	/**
	 * Set a Drawable that should be used to highlight the currently selected item.
	 *
	 * @param resID A Drawable resource to use as the selection highlight.
	 * @attr ref android.R.styleable#AbsListView_listSelector
	 */
	public void setSelector (int resID) {
		setSelector( getResources().getDrawable( resID ) );
	}

	public void setSelector (Drawable sel) {

		if (mSelector != null) {
			mSelector.setCallback(null);
			unscheduleDrawable(mSelector);
		}

		mSelector = sel;
		Rect padding = new Rect();
		sel.getPadding(padding);
		mSelectionLeftPadding = padding.left;
		mSelectionTopPadding = padding.top;
		mSelectionRightPadding = padding.right;
		mSelectionBottomPadding = padding.bottom;
		sel.setCallback(this);
		updateSelectorState();
	}

	/**
	 * Indicates whether this view is in a state where the selector should be drawn. This will
	 * happen if we have focus but are not in touch mode, or we are in the middle of displaying
	 * the pressed state for an item.
	 *
	 * @return True if the selector should be shown
	 */
	protected boolean shouldShowSelector () {
		return (hasFocus() && !isInTouchMode()) || isPressed();
	}

	/**
	 * Overridden to draw the selector image.
	 *
	 * @param canvas Canvas object
	 */
	@Override
	protected void dispatchDraw (Canvas canvas) {

		if (mSelectorRect.isEmpty()) {
			super.dispatchDraw( canvas );
			return;
		}

		//final boolean clipToPadding = (mGroupFlags & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK;
		//if (clipToPadding) {
		int saveCount = canvas.save();
		final int scrollX = getScrollX();
		final int scrollY = getScrollY();
		canvas.clipRect( scrollX + getPaddingLeft(), scrollY + getPaddingTop(),
				scrollX + getRight() - getLeft() - getPaddingRight(),
				scrollY + getBottom() - getTop() - getPaddingBottom() );
		//mGroupFlags &= ~CLIP_TO_PADDING_MASK;
		//}

		final boolean drawSelectorOnTop = mDrawSelectorOnTop;
		if (!drawSelectorOnTop) {
			drawSelector( canvas );
		}

		super.dispatchDraw( canvas );

		if (drawSelectorOnTop) {
			drawSelector( canvas );
		}

		//if (clipToPadding) {
		canvas.restoreToCount( saveCount );
		//mGroupFlags |= CLIP_TO_PADDING_MASK;
		//}
	}

	/**
	 * Override this method if you need to draw a custom selector.
	 *
	 * @param canvas Canvas instance.
	 */
	protected void drawSelector (Canvas canvas) {
		if (mSelector != null && !mSelectorRect.isEmpty()) {
			final Drawable selector = mSelector;
			selector.setBounds( mSelectorRect );
			selector.draw( canvas );
		}
	}

	protected void updateSelector () {
		if (!isInTouchMode() && mSelectedPosition != INVALID_POSITION)
			positionSelector(mSelectedPosition, getSelectedView());
		else if (mSelectorPosition != INVALID_POSITION)
			positionSelector(INVALID_POSITION, getSelectedView());
		else
			mSelectorRect.setEmpty();
	}

	private static final int[] STATE_NOTHING = new int[]{0};

	private void updateSelectorState () {
		if (mSelector != null) {
			if (shouldShowSelector())
				mSelector.setState( getDrawableState() );
			else
				mSelector.setState( STATE_NOTHING );
		}
	}

	private void positionSelector (int position, View sel) {

		if (position != INVALID_POSITION) {
			mSelectorPosition = position;
		}

		if (sel == null) return;

		final Rect selectorRect = mSelectorRect;
		selectorRect.set( sel.getLeft(), sel.getTop(), sel.getRight(), sel.getBottom() );
		positionSelector( selectorRect.left, selectorRect.top, selectorRect.right, selectorRect.bottom );

		final boolean isChildViewEnabled = mIsChildViewEnabled;
		if (sel.isEnabled() != isChildViewEnabled) {
			mIsChildViewEnabled = !isChildViewEnabled;
			if (getSelectedItemPosition() != INVALID_POSITION) {
				refreshDrawableState();
			}
		}
	}

	private void positionSelector (int l, int t, int r, int b) {
		mSelectorRect.set( l - mSelectionLeftPadding, t - mSelectionTopPadding, r + mSelectionRightPadding, b + mSelectionBottomPadding );
	}

	protected void updateSelectedItemMetadata () {

		updateSelector();

		View oldSelectedChild = mSelectedChild;

		View child = mSelectedChild = getChildAt( mSelectedPosition - mFirstPosition );
		if (child == null) {
			return;
		}

		child.setSelected( true );
		child.setFocusable( true );

		if (hasFocus()) {
			child.requestFocus();
		}

		// We unfocus the old child down here so the above hasFocus check
		// returns true
		if (oldSelectedChild != null && oldSelectedChild != child) {

			// Make sure its drawable state doesn't contain 'selected'
			oldSelectedChild.setSelected( false );

			// Make sure it is not focusable anymore, since otherwise arrow keys
			// can make this one be focused
			oldSelectedChild.setFocusable( false );
		}
	}

	@Override
	protected void drawableStateChanged () {
		super.drawableStateChanged();
		updateSelectorState();
	}

	@Override
	protected int[] onCreateDrawableState (int extraSpace) {
		// If the child view is enabled then do the default behavior.
		if (mIsChildViewEnabled) {
			// Common case
			return super.onCreateDrawableState( extraSpace );
		}

		// The selector uses this View's drawable state. The selected child view
		// is disabled, so we need to remove the enabled state from the drawable
		// states.
		final int enabledState = ENABLED_STATE_SET[0];

		// If we don't have any extra space, it will return one of the static state arrays,
		// and clearing the enabled state on those arrays is a bad thing!  If we specify
		// we need extra space, it will create+copy into a new array that safely mutable.
		int[] state = super.onCreateDrawableState( extraSpace + 1 );
		int enabledPos = -1;
		for (int i = state.length - 1; i >= 0; i--) {
			if (state[i] == enabledState) {
				enabledPos = i;
				break;
			}
		}

		// Remove the enabled state
		if (enabledPos >= 0)
			System.arraycopy( state, enabledPos + 1, state, enabledPos, state.length - enabledPos - 1 );

		return state;
	}

	@Override
	public boolean verifyDrawable (Drawable dr) {
		return mSelector == dr || super.verifyDrawable( dr );
	}

	/**
	 * Update the status of the list based on the empty parameter.  If empty is true and
	 * we have an empty view, display it.  In all the other cases, make sure that the listview
	 * is VISIBLE and that the empty view is GONE (if it's not null).
	 */
	private void updateEmptyStatus (boolean empty) {
		if (empty) {
			View mEmptyView = getEmptyView();
			if (mEmptyView != null) {
				mEmptyView.setVisibility( View.VISIBLE );
				setVisibility( View.GONE );
			}
			else {
				// If the caller just removed our empty view, make sure the list view is visible
				setVisibility( View.VISIBLE );
			}

			// We are now GONE, so pending layouts will not be dispatched.
			// Force one here to make sure that the state of the list matches
			// the state of the adapter.
			if (mDataChanged) {
				onLayout( false, getLeft(), getTop(), getRight(), getBottom() );
			}
		}
		else {
			View mEmptyView = getEmptyView();
			if (mEmptyView != null) mEmptyView.setVisibility( View.GONE );
			setVisibility( View.VISIBLE );
		}
	}

	/**
	 * Gets the data associated with the specified position in the list.
	 *
	 * @param position Which data to get
	 * @return The data associated with the specified position in the list
	 */
	public Object getItemAtPosition (int position) {
		return (mAdapter == null || position < 0) ? null : mAdapter.getItem( position );
	}

	public long getItemIdAtPosition (int position) {
		return (mAdapter == null || position < 0) ? INVALID_ROW_ID : mAdapter.getItemId( position );
	}

	@Override
	public void setOnClickListener (OnClickListener l) {
		throw new RuntimeException( "Don't call setOnClickListener for an AdapterView. "
				+ "You probably want setOnItemClickListener instead" );
	}

	/**
	 * Clear out all children from the list
	 */
	protected void resetList () {
		mDataChanged = false;
		mNeedSync = false;

		removeAllViewsInLayout();
		mOldSelectedPosition = INVALID_POSITION;
		mOldSelectedRowId = INVALID_ROW_ID;

		mSelectorPosition = INVALID_POSITION;
		mSelectorRect.setEmpty();

		setSelectedPositionInt( INVALID_POSITION );
		setNextSelectedPositionInt( INVALID_POSITION );
		invalidate();
	}

	protected void recycleAllViews () {
		final int childCount = getChildCount();
		final RecycleBin recycleBin = mRecycler;
		final int position = mFirstPosition;

		// All views go in recycler
		for (int i = 0; i < childCount; i++) {
			View v = getChildAt( i );
			int index = position + i;
			recycleBin.put( index, v );
		}
	}

	/**
	 * Override to prevent freezing of any views created by the adapter.
	 */
	@Override
	protected void dispatchSaveInstanceState (SparseArray<Parcelable> container) {
		dispatchFreezeSelfOnly( container );
	}

	/**
	 * Override to prevent thawing of any views created by the adapter.
	 */
	@Override
	protected void dispatchRestoreInstanceState (SparseArray<Parcelable> container) {
		dispatchThawSelfOnly( container );
	}

	protected DataSetObserver createDataSetObserver () {
		return new AdapterDataSetObserver();
	}

	protected class AdapterDataSetObserver extends DataSetObserver {

		private Parcelable mInstanceState = null;

		@Override
		public void onChanged () {
			mDataChanged = true;
			mOldItemCount = mItemCount;
			mItemCount = mAdapter.getCount();

			// Detect the case where a cursor that was previously invalidated has
			// been repopulated with new data.
			if (mAdapter.hasStableIds() && mInstanceState != null && mOldItemCount == 0 && mItemCount > 0) {
				onRestoreInstanceState( mInstanceState );
				mInstanceState = null;
			}
			else {
				rememberSyncState();
			}
			checkFocus();
			requestLayout();
		}

		@Override
		public void onInvalidated () {
			mDataChanged = true;

			if (mAdapter.hasStableIds()) {
				// Remember the current state for the case where our hosting activity is being
				// stopped and later restarted
				mInstanceState = onSaveInstanceState();
			}

			// Data is invalid so we should reset our state
			mOldItemCount = mItemCount;
			mItemCount = 0;
			mSelectedPosition = INVALID_POSITION;
			mSelectedRowId = INVALID_ROW_ID;
			mNextSelectedPosition = INVALID_POSITION;
			mNextSelectedRowId = INVALID_ROW_ID;
			mNeedSync = false;

			checkFocus();
			requestLayout();
		}

		public void clearSavedState () {
			mInstanceState = null;
		}
	}

	@Override
	protected void onDetachedFromWindow () {
		super.onDetachedFromWindow();
		removeCallbacks( mSelectionNotifier );
	}

	private class SelectionNotifier implements Runnable {

		public void run () {
			if (mDataChanged) {
				// Data has changed between when this SelectionNotifier
				// was posted and now. We need to wait until the AdapterView
				// has been synched to the new data.
				if (getAdapter() != null) {
					post( this );
				}
			}
			else {
				fireOnSelected();
			}
		}
	}

	protected void selectionChanged () {

		updateSelector();

		if (getOnItemSelectedListener() != null) {
			if (mInLayout || mBlockLayoutRequests) {
				// If we are in a layout traversal, defer notification
				// by posting. This ensures that the view tree is
				// in a consistent state and is able to accomodate
				// new layout or invalidate requests.
				if (mSelectionNotifier == null) {
					mSelectionNotifier = new SelectionNotifier();
				}
				post( mSelectionNotifier );
			}
			else {
				fireOnSelected();
			}
		}

		// we fire selection events here not in View
		if (mSelectedPosition != ListView.INVALID_POSITION && isShown() && !isInTouchMode()) {
			sendAccessibilityEvent( AccessibilityEvent.TYPE_VIEW_SELECTED );
		}
	}

	private void fireOnSelected () {
		if (getOnItemSelectedListener() == null)
			return;

		int selection = this.getSelectedItemPosition();
		if (selection >= 0) {
			View v = getSelectedView();
			getOnItemSelectedListener().onItemSelected( this, v, selection, getAdapter().getItemId( selection ) );
		}
		else {
			getOnItemSelectedListener().onNothingSelected( this );
		}
	}

	@Override
	public boolean dispatchPopulateAccessibilityEvent (AccessibilityEvent event) {
		View selectedView = getSelectedView();
		return selectedView != null && selectedView.getVisibility() == VISIBLE && selectedView.dispatchPopulateAccessibilityEvent( event );
	}

	@Override
	public boolean onRequestSendAccessibilityEvent (View child, AccessibilityEvent event) {
		if (super.onRequestSendAccessibilityEvent( child, event )) {
			// Add a record for ourselves as well.
			AccessibilityEvent record = AccessibilityEvent.obtain();
			onInitializeAccessibilityEvent( record );
			// Populate with the text of the requesting child.
			child.dispatchPopulateAccessibilityEvent( record );
			event.appendRecord( record );
			return true;
		}
		return false;
	}

	@Override
	public void onInitializeAccessibilityNodeInfo (AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo( info );
		info.setScrollable( isScrollableForAccessibility() );
		View selectedView = getSelectedView();
		if (selectedView != null) {
			info.setEnabled( selectedView.isEnabled() );
		}
	}

	@Override
	public void onInitializeAccessibilityEvent (AccessibilityEvent event) {
		super.onInitializeAccessibilityEvent( event );
		event.setScrollable( isScrollableForAccessibility() );
		View selectedView = getSelectedView();
		if (selectedView != null) {
			event.setEnabled( selectedView.isEnabled() );
		}
		event.setCurrentItemIndex( getSelectedItemPosition() );
		event.setFromIndex( getFirstVisiblePosition() );
		event.setToIndex( getLastVisiblePosition() );
		event.setItemCount( getCount() );
	}

	private boolean isScrollableForAccessibility () {
		T adapter = getAdapter();
		if (adapter != null) {
			final int itemCount = adapter.getCount();
			return itemCount > 0
					&& (getFirstVisiblePosition() > 0 || getLastVisiblePosition() < itemCount - 1);
		}
		return false;
	}

	@Override
	protected boolean canAnimate () {
		return getLayoutAnimation() != null && mItemCount > 0;
	}

	protected void handleDataChanged () {
		final int count = mItemCount;
		boolean found = false;

		if (count > 0) {

			int newPos;

			// Find the row we are supposed to sync to
			if (mNeedSync) {
				// Update this first, since setNextSelectedPositionInt inspects
				// it
				mNeedSync = false;

				// See if we can find a position in the new data with the same
				// id as the old selection
				newPos = findSyncPosition();
				if (newPos >= 0) {
					// Verify that new selection is selectable
					int selectablePos = lookForSelectablePosition( newPos, true );
					if (selectablePos == newPos) {
						// Same row id is selected
						setNextSelectedPositionInt( newPos );
						found = true;
					}
				}
			}
			if (!found) {
				// Try to use the same position if we can't find matching data
				newPos = getSelectedItemPosition();

				// Pin position to the available range
				if (newPos >= count) {
					newPos = count - 1;
				}
				if (newPos < 0) {
					newPos = 0;
				}

				// Make sure we select something selectable -- first look down
				int selectablePos = lookForSelectablePosition( newPos, true );
				if (selectablePos < 0) {
					// Looking down didn't work -- try looking up
					selectablePos = lookForSelectablePosition( newPos, false );
				}
				if (selectablePos >= 0) {
					setNextSelectedPositionInt( selectablePos );
					checkSelectionChanged();
					found = true;
				}
			}
		}
		if (!found) {
			// Nothing is selected
			mSelectedPosition = INVALID_POSITION;
			mSelectedRowId = INVALID_ROW_ID;
			mNextSelectedPosition = INVALID_POSITION;
			mNextSelectedRowId = INVALID_ROW_ID;
			mNeedSync = false;
			checkSelectionChanged();
		}
	}

	@Override
	public T getAdapter () {
		return mAdapter;
	}

	/**
	 * The Adapter is used to provide the data which backs this Spinner.
	 * It also provides methods to transform spinner items based on their position
	 * relative to the selected item.
	 *
	 * @param adapter The SpinnerAdapter to use for this Spinner
	 */
	@Override
	public void setAdapter (T adapter) {
		if (null != mAdapter) {
			mAdapter.unregisterDataSetObserver( mDataSetObserver );
			resetList();
		}

		mAdapter = adapter;

		mOldSelectedPosition = INVALID_POSITION;
		mOldSelectedRowId = INVALID_ROW_ID;

		if (mAdapter != null) {
			mOldItemCount = mItemCount;
			mItemCount = mAdapter.getCount();
			checkFocus();

			mDataSetObserver = createDataSetObserver();
			mAdapter.registerDataSetObserver( mDataSetObserver );

			int position = mItemCount > 0 ? 0 : INVALID_POSITION;

			setSelectedPositionInt( position );
			setNextSelectedPositionInt( position );

			if (mItemCount == 0) {
				// Nothing selected
				checkSelectionChanged();
			}

		}
		else {
			checkFocus();
			resetList();
			// Nothing selected
			checkSelectionChanged();
		}

		requestLayout();
	}

	protected void checkSelectionChanged () {
		if ((mSelectedPosition != mOldSelectedPosition) || (mSelectedRowId != mOldSelectedRowId)) {
			selectionChanged();
			mOldSelectedPosition = mSelectedPosition;
			mOldSelectedRowId = mSelectedRowId;
		}
	}

	/**
	 * Searches the adapter for a position matching mSyncRowId. The search starts at mSyncPosition
	 * and then alternates between moving up and moving down until 1) we find the right position, or
	 * 2) we run out of time, or 3) we have looked at every position
	 *
	 * @return Position of the row that matches mSyncRowId, or {@link #INVALID_POSITION} if it can't
	 *         be found
	 */
	private int findSyncPosition () {
		int count = mItemCount;

		if (count == 0) {
			return INVALID_POSITION;
		}

		long idToMatch = mSyncRowId;
		int seed = mSyncPosition;

		// If there isn't a selection don't hunt for it
		if (idToMatch == INVALID_ROW_ID) {
			return INVALID_POSITION;
		}

		// Pin seed to reasonable values
		seed = Math.max( 0, seed );
		seed = Math.min( count - 1, seed );

		long endTime = SystemClock.uptimeMillis() + SYNC_MAX_DURATION_MILLIS;

		long rowId;

		// first position scanned so far
		int first = seed;

		// last position scanned so far
		int last = seed;

		// True if we should move down on the next iteration
		boolean next = false;

		// True when we have looked at the first item in the data
		boolean hitFirst;

		// True when we have looked at the last item in the data
		boolean hitLast;

		// Get the item ID locally (instead of getItemIdAtPosition), so
		// we need the adapter
		T adapter = getAdapter();
		if (adapter == null) {
			return INVALID_POSITION;
		}

		while (SystemClock.uptimeMillis() <= endTime) {
			rowId = adapter.getItemId( seed );
			if (rowId == idToMatch) {
				// Found it!
				return seed;
			}

			hitLast = last == count - 1;
			hitFirst = first == 0;

			if (hitLast && hitFirst) {
				// Looked at everything
				break;
			}

			if (hitFirst || (next && !hitLast)) {
				// Either we hit the top, or we are trying to move down
				last++;
				seed = last;
				// Try going up next time
				next = false;
			}
			else if (hitLast || (!next && !hitFirst)) {
				// Either we hit the bottom, or we are trying to move up
				first--;
				seed = first;
				// Try going down next time
				next = true;
			}

		}

		return INVALID_POSITION;
	}

	/**
	 * Find a position that can be selected (i.e., is not a separator).
	 *
	 * @param position The starting position to look at.
	 * @param lookDown Whether to look down for other positions.
	 * @return The next selectable position starting at position and then searching either up or
	 *         down. Returns {@link #INVALID_POSITION} if nothing can be found.
	 */
	protected int lookForSelectablePosition (int position, boolean lookDown) {
		return position;
	}

	/**
	 * Utility to keep mSelectedPosition and mSelectedRowId in sync
	 *
	 * @param position Our current position
	 */
	protected void setSelectedPositionInt (int position) {
		mSelectedPosition = position;
		mSelectedRowId = getItemIdAtPosition( position );

		// Updates any metadata we keep about the selected item.
		updateSelectedItemMetadata();
	}

	/**
	 * Makes the item at the supplied position selected.
	 *
	 * @param position Position to select
	 * @param animate  Should the transition be animated
	 */
	protected void setSelectionInt (int position, boolean animate) {
		if (position != mOldSelectedPosition) {
			mBlockLayoutRequests = true;
			int delta = position - mSelectedPosition;
			setNextSelectedPositionInt( position );
			layoutChildren( delta, animate, false );
			mBlockLayoutRequests = false;
		}
	}

	/**
	 * Utility to keep mNextSelectedPosition and mNextSelectedRowId in sync
	 *
	 * @param position Intended value for mSelectedPosition the next time we go
	 *                 through layout
	 */
	protected void setNextSelectedPositionInt (int position) {
		mNextSelectedPosition = position;
		mNextSelectedRowId = getItemIdAtPosition( position );
		// If we are trying to sync to the selection, update that too
		if (mNeedSync && mSyncMode == SYNC_SELECTED_POSITION && position >= 0) {
			mSyncPosition = position;
			mSyncRowId = mNextSelectedRowId;
		}
	}

	/**
	 * Remember enough information to restore the screen state when the data has changed.
	 */
	protected void rememberSyncState () {
		if (getChildCount() > 0) {
			mNeedSync = true;
			mSyncHeight = mLayoutHeight;
			if (mSelectedPosition >= 0) {
				// Sync the selection state
				View v = getChildAt( mSelectedPosition - mFirstPosition );
				mSyncRowId = mNextSelectedRowId;
				mSyncPosition = mNextSelectedPosition;
				if (v != null) {
					mSpecificTop = v.getTop();
				}
				mSyncMode = SYNC_SELECTED_POSITION;
			}
			else {
				// Sync the based on the offset of the first view
				View v = getChildAt( 0 );
				T adapter = getAdapter();
				if (mFirstPosition >= 0 && mFirstPosition < adapter.getCount()) {
					mSyncRowId = adapter.getItemId( mFirstPosition );
				}
				else {
					mSyncRowId = NO_ID;
				}
				mSyncPosition = mFirstPosition;
				if (v != null) {
					mSpecificTop = v.getTop();
				}
				mSyncMode = SYNC_FIRST_POSITION;
			}
		}
	}

	protected void dispatchPress (View child) {

		if (child != null) {
			child.setPressed( true );
		}

		setPressed( true );
	}

	protected void dispatchUnpress () {

		for (int i = getChildCount() - 1; i >= 0; i--) {
			getChildAt( i ).setPressed( false );
		}

		setPressed( false );
	}

	/**
	 * We don't want to pass the selected state given from its parent to its
	 * children since this widget itself has a selected state to give to its
	 * children.
	 */
	@Override
	public void dispatchSetSelected (boolean selected) {
	}

	/**
	 * Show the pressed state on the selected child.
	 */
	@Override
	protected void dispatchSetPressed (boolean pressed) {
		if (mSelectedChild != null) {
			mSelectedChild.setPressed( pressed );
		}
	}

	/**
	 * Handles left, right, and clicking
	 *
	 * @see android.view.View#onKeyDown
	 */
	@Override
	public boolean onKeyDown (int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				mReceivedInvokeKeyDown = true;
				// fallthrough to default handling
		}

		return super.onKeyDown( keyCode, event );
	}

	@Override
	public boolean onKeyUp (int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER: {

				if (mReceivedInvokeKeyDown) {
					if (mItemCount > 0) {

						dispatchPress( mSelectedChild );
						postDelayed( new Runnable() {
							public void run () {
								dispatchUnpress();
							}
						}, ViewConfiguration.getPressedStateDuration() );

						int selectedIndex = mSelectedPosition - mFirstPosition;
						performItemClick( getChildAt( selectedIndex ), mSelectedPosition, getAdapter().getItemId( mSelectedPosition ) );
					}

					// Clear the flag
					mReceivedInvokeKeyDown = false;
					return true;
				}
				return false;
			}
		}

		return super.onKeyUp( keyCode, event );
	}

	protected class RecycleBin {

		private final SparseArray<View> mScrapHeap = new SparseArray<View>();

		public void put (int position, View v) {
			mScrapHeap.put( position, v );
		}

		public View get (int position) {
			// System.out.print("Looking for " + position);
			View result = mScrapHeap.get( position );
			if (result != null) {
				// System.out.println(" HIT");
				mScrapHeap.delete( position );
			}
			else {
				// System.out.println(" MISS");
			}
			return result;
		}

		public void clear () {
			final SparseArray<View> scrapHeap = mScrapHeap;
			final int count = scrapHeap.size();
			for (int i = 0; i < count; i++) {
				final View view = scrapHeap.valueAt( i );
				if (view != null) {
					removeDetachedView( view, true );
				}
			}
			scrapHeap.clear();
		}
	}

	protected static class SavedState extends BaseSavedState {

		long selectedId;
		int position;

		/**
		 * Constructor called from {@link AbsSpinner#onSaveInstanceState()}
		 */
		SavedState (Parcelable superState) {
			super( superState );
		}

		/**
		 * Constructor called from {@link #CREATOR}
		 */
		private SavedState (Parcel in) {
			super( in );
			selectedId = in.readLong();
			position = in.readInt();
		}

		@Override
		public void writeToParcel (Parcel out, int flags) {
			super.writeToParcel( out, flags );
			out.writeLong( selectedId );
			out.writeInt( position );
		}

		@Override
		public String toString () {
			return "FriendAdapterView.SavedState{"
					+ Integer.toHexString( System.identityHashCode( this ) )
					+ " selectedId=" + selectedId
					+ " position=" + position + "}";
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel (Parcel in) {
				return new SavedState( in );
			}

			public SavedState[] newArray (int size) {
				return new SavedState[size];
			}
		};
	}

	@Override
	public Parcelable onSaveInstanceState () {
		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState( superState );
		ss.selectedId = getSelectedItemId();
		if (ss.selectedId >= 0) {
			ss.position = getSelectedItemPosition();
		}
		else {
			ss.position = INVALID_POSITION;
		}
		return ss;
	}

	@Override
	public void onRestoreInstanceState (Parcelable state) {
		SavedState ss = (SavedState)state;

		super.onRestoreInstanceState( ss.getSuperState() );

		if (ss.selectedId >= 0) {
			mDataChanged = true;
			mNeedSync = true;
			mSyncRowId = ss.selectedId;
			mSyncPosition = ss.position;
			mSyncMode = SYNC_SELECTED_POSITION;
			requestLayout();
		}
	}
}
