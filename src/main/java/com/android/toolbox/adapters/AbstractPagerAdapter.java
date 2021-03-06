package com.android.toolbox.adapters;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * @author gomino (amine.bezzarga@labgency.com)
 */
public abstract class AbstractPagerAdapter<T> extends PagerAdapter {

	protected List<T> mData;
	
	public AbstractPagerAdapter() {
		mData = new ArrayList<T>();
	}
	
	public void setData(List<T> data){
		if (mData!=null)
			mData.clear();
		if(data!=null){
			mData.addAll(data);
		}
		notifyDataSetChanged();
	}
	
	public List<T> getData(){
		return mData;
	}

	public void add(T item) {
		add(item, getCount()-1);
	}

	public void add(T item, int index) {
		if (item != null) {
			mData.add(index, item);
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
		return mData.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	
	public boolean isEmpty(){
		return mData.isEmpty();
	}
	
	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}
	
}
