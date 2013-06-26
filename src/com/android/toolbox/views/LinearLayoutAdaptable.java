package com.android.toolbox.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.LinearLayout;


public class LinearLayoutAdaptable extends LinearLayout{
	
    private Adapter mAdapter;
    private Observer mObserver = new Observer(this);
	private boolean mBlockRequestLayout = false;
	private Handler mHandler;
    //AdapterDataSetObserver mObserver = new AdapterDataSetObserver(this);
    
    /**
     * True if the data has changed since the last layout
     */
    boolean mDataChanged;
    
    /**
     * View to show if there are no items to show.
     */
    private View mEmptyView;
    
    /**
     * The listener that receives notifications when an item is clicked.
     */
    OnClickListener mOnClickListener;
    



    public LinearLayoutAdaptable(Context context)
    {
        super(context);
        mHandler = new Handler();
    }

    public LinearLayoutAdaptable(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mHandler = new Handler();
    }

    public LinearLayoutAdaptable(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mHandler = new Handler();
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
		}
	}
    
    
    public void setAdapter(Adapter adapter)
    {
        if (this.mAdapter != null)
            this.mAdapter.unregisterDataSetObserver(mObserver);

        this.mAdapter = adapter;
        adapter.registerDataSetObserver(mObserver);
//        mObserver.onChanged();
    }

    private class Observer extends DataSetObserver
    {
    	LinearLayoutAdaptable linearLayoutAdaptable;

        public Observer(LinearLayoutAdaptable linearLayout)
        {
            this.linearLayoutAdaptable = linearLayout;
        }

        @Override
        public void onChanged()
        {
//        	setBlockRequestLayout(true);
//        	Log.e("LinearLayoutAdaptable", "onChanged");
//        	new AddViewTask().execute(this);
        	
        	mDataChanged = true;
//            List<View> oldViews = new ArrayList<View>(linearLayoutAdaptable.getChildCount());
//
//            for (int i = 0; i < linearLayoutAdaptable.getChildCount(); i++)
//                oldViews.add(linearLayoutAdaptable.getChildAt(i));
//
//            Iterator<View> iter = oldViews.iterator();

        	
            linearLayoutAdaptable.removeAllViews();
            for (int i = 0; i < linearLayoutAdaptable.mAdapter.getCount(); i++)
            {
            	final int position = i;
//                final View convertView = iter.hasNext() ? iter.next() : null;
//            	View v = linearLayoutAdaptable.mAdapter.getView(position, convertView, linearLayoutAdaptable);
            	View v = linearLayoutAdaptable.mAdapter.getView(position, null, linearLayoutAdaptable);
		        //v.setOnClickListener(getOnClickListener());
		        linearLayoutAdaptable.addView(v);

            }
            
			updateEmptyStatus((mAdapter == null) || mAdapter.isEmpty());
//			setBlockRequestLayout(false);
			super.onChanged();
//			setBlockRequestLayout(true);
        }

        @Override
        public void onInvalidated()
        {
        	mDataChanged = true;
            linearLayoutAdaptable.removeAllViews();
            super.onInvalidated();
        }
        
        public void superOnChanged()
        {
        	super.onChanged();
        }
    }
    
    
    
    
//    @Override
//	public int getChildCount() {
//    	if (mAdapter!=null){
//    		return mAdapter.getCount();
//    	}else{
//    		return super.getChildCount();
//    	}
//	}

//    @Override
//	public View getChildAt(int position) {
////    	if (mAdapter!=null){
////    		return mAdapter.getItem(position);
////    	}else{
//    		return super.getChildAt(position);
////    	}
//	}
    
	/**
     * Sets the view to show if the adapter is empty
     */
    public void setEmptyView(View emptyView) {
        mEmptyView = emptyView;

        final Adapter adapter = getAdapter();
        final boolean empty = ((adapter == null) || adapter.isEmpty());
        updateEmptyStatus(empty);
    }
    
    

    private Adapter getAdapter() {
		// TODO Auto-generated method stub
		return mAdapter;
	}
    
//    private class AddViewTask extends AsyncTask<Observer, Void, Observer>{
//    	
//		@Override
//		protected Observer doInBackground(Observer... observer) {
//			final LinearLayoutAdaptable linearLayoutAdaptable = observer[0].linearLayoutAdaptable;
//			mDataChanged = true;
//            List<View> oldViews = new ArrayList<View>(linearLayoutAdaptable.getChildCount());
//
//            for (int i = 0; i < linearLayoutAdaptable.getChildCount(); i++)
//                oldViews.add(linearLayoutAdaptable.getChildAt(i));
//
//            Iterator<View> iter = oldViews.iterator();
//
//            linearLayoutAdaptable.removeAllViews();
//
//            for (int i = 0; i < linearLayoutAdaptable.mAdapter.getCount(); i++)
//            {
//            	final int position = i;
//                final View convertView = iter.hasNext() ? iter.next() : null;
//                mHandler.post(new Runnable() {
//					@Override
//					public void run() {
//		                View v = linearLayoutAdaptable.mAdapter.getView(position, convertView, linearLayoutAdaptable);
//		            	//v.setOnClickListener(getOnClickListener());
//		                linearLayoutAdaptable.addView(v);
//					}
//				});
//
//            }
//            
//            mHandler.post(new Runnable() {
//				@Override
//				public void run() {
//					updateEmptyStatus((mAdapter == null) || mAdapter.isEmpty());
//				}
//			});
//            
//			return observer[0];
//		}
//
//		@Override
//		protected void onPostExecute(Observer observer) {
//			observer.superOnChanged();
//		}
//    	
//    }

	/**
     * When the current adapter is empty, the AdapterView can display a special view
     * call the empty view. The empty view is used to provide feedback to the user
     * that no data is available in this AdapterView.
     *
     * @return The view to show if the adapter is empty.
     */
    public View getEmptyView() {
        return mEmptyView;
    }

    
    /**
     * Update the status of the list based on the empty parameter.  If empty is true and
     * we have an empty view, display it.  In all the other cases, make sure that the listview
     * is VISIBLE and that the empty view is GONE (if it's not null).
     */
    @SuppressLint("WrongCall")
	private void updateEmptyStatus(boolean empty) {
        if (isInFilterMode()) {
            empty = false;
        }

        if (empty) {
            if (mEmptyView != null) {
                mEmptyView.setVisibility(View.VISIBLE);
                setVisibility(View.GONE);
            } else {
                // If the caller just removed our empty view, make sure the list view is visible
                setVisibility(View.VISIBLE);
            }

            // We are now GONE, so pending layouts will not be dispatched.
            // Force one here to make sure that the state of the list matches
            // the state of the adapter.
            if (mDataChanged) {           
                this.onLayout(false, getLeft(), getTop(), getRight(), getBottom()); 
            }
        } else {
            if (mEmptyView != null) mEmptyView.setVisibility(View.GONE);
            setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Indicates whether this view is in filter mode. Filter mode can for instance
     * be enabled by a user when typing on the keyboard.
     *
     * @return True if the view is in filter mode, false otherwise.
     */
    boolean isInFilterMode() {
        return false;
    }
    
    
    /**
     * Register a callback to be invoked when an item in this AdapterView has
     * been clicked.
     *
     * @param listener The callback that will be invoked.
     */
    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }
    
    /**
     * @return The callback to be invoked with an item in this AdapterView has
     *         been clicked, or null id no callback has been set.
     */
    public final OnClickListener getOnClickListener() {
        return mOnClickListener;
    }
    
    /**
     * Call the OnItemClickListener, if it is defined.
     *
     * @param view The view within the AdapterView that was clicked.
     * @param position The position of the view in the adapter.
     * @param id The row id of the item that was clicked.
     * @return True if there was an assigned OnItemClickListener that was
     *         called, false otherwise is returned.
     */
//    public boolean performItemClick(View view, int position, long id) {
//        if (mOnItemClickListener != null) {
//            playSoundEffect(SoundEffectConstants.CLICK);
//            mOnItemClickListener.onItemClick(this, view, position, id);
//            return true;
//        }
//
//        return false;
//    }
    
//    /**
//     * Interface definition for a callback to be invoked when an item in this
//     * AdapterView has been clicked.
//     */
//    public interface OnItemClickListener {
//
//        /**
//         * Callback method to be invoked when an item in this AdapterView has
//         * been clicked.
//         * <p>
//         * Implementers can call getItemAtPosition(position) if they need
//         * to access the data associated with the selected item.
//         *
//         * @param linearLayoutAdaptable The AdapterView where the click happened.
//         * @param view The view within the AdapterView that was clicked (this
//         *            will be a view provided by the adapter)
//         * @param position The position of the view in the adapter.
//         * @param id The row id of the item that was clicked.
//         */
//        void onItemClick(LinearLayoutAdaptable linearLayoutAdaptable, View view, int position, long id);
//    }
	
	//-------------------------------------------------------------------
    
//    /**
//     * Defines the choice behavior for the List. By default, Lists do not have any choice behavior
//     * ({@link #CHOICE_MODE_NONE}). By setting the choiceMode to {@link #CHOICE_MODE_SINGLE}, the
//     * List allows up to one item to  be in a chosen state. By setting the choiceMode to
//     * {@link #CHOICE_MODE_MULTIPLE}, the list allows any number of items to be chosen.
//     *
//     * @param choiceMode One of {@link #CHOICE_MODE_NONE}, {@link #CHOICE_MODE_SINGLE}, or
//     * {@link #CHOICE_MODE_MULTIPLE}
//     */
//    public void setChoiceMode(int choiceMode) {
//        mChoiceMode = choiceMode;
//        if (mChoiceMode != CHOICE_MODE_NONE) {
//            if (mCheckStates == null) {
//                mCheckStates = new SparseBooleanArray();
//            }
//            if (mCheckedIdStates == null && mAdapter != null && mAdapter.hasStableIds()) {
//                mCheckedIdStates = new LongSparseArray<Boolean>();
//            }
//        }
//    }
}
