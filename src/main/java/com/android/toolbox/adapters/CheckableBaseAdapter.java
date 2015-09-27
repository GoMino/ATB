package com.android.toolbox.adapters;

import java.util.ArrayList;
import java.util.List;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * @author gomino (amine.bezzarga@labgency.com)
 */
public abstract class CheckableBaseAdapter<T> extends AbstractBaseAdapter<T> {

	public interface CheckItemsListener{
		public void updateOnCheck();
	}
	
	protected CheckItemsListener checkListener;

	protected List<T> mCheckedList;
	
	public CheckableBaseAdapter() {
		mData = new ArrayList<T>();
	}
	
	public void setCheckListener(CheckItemsListener l){
		checkListener = l;
	}
	
	public void setCheckedData(List<T> list){
		mCheckedList = list;
	}

	public List<T> getCheckedData(){
		return mCheckedList;
	}

}
