package com.android.toolbox.adapters;

import java.util.ArrayList;
import java.util.List;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * @author gomino (amine.bezzarga@labgency.com)
 */
public abstract class AbstractBaseAdapter<T> extends BaseAdapter {

	public interface CheckItemsListener{
		public void updateOnCheck();
	}
	
	protected CheckItemsListener checkListener;

	protected List<T> mData;
	protected List<T> mCheckedList;
	
	public AbstractBaseAdapter() {
		mData = new ArrayList<T>();
	}
	
	public void setCheckListener(CheckItemsListener l){
		checkListener = l;
	}
	
	public void setData(List<T> data){
		if (mData!=null)
			mData.clear();
		if(data!=null){
			mData.addAll(data);
		}
		notifyDataSetChanged();
	}
	

	public void setCheckedData(List<T> list){
		mCheckedList = list;
	}

	public List<T> getCheckedData(){
		return mCheckedList;
	}
	
	public List<T> getData(){
		return mData;
	}
	
	public void add(T item) {
		if (item != null) {
			mData.add(item);
		}
		this.notifyDataSetChanged();
	}
	
	public void remove(T item) {
		if (item != null) {
			mData.remove(item);
		}
		this.notifyDataSetChanged();
	}
	
	public T getItemAtPosition(int position){
		T result = null;
		if(position>=0 && position<mData.size()){
			result = mData.get(position);
		}
		return result;
	}
	
	public boolean isEmpty(){
		return mData.isEmpty();
	}
	
	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}


}
