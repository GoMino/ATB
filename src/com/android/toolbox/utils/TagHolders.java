package com.android.toolbox.utils;

import java.util.HashMap;
import java.util.Map;

import android.support.v4.app.Fragment;
import android.view.View;

import com.android.toolbox.Log;

public class TagHolders<K extends View, T> {

	private Map<Integer, K> mHolderByPosition;
	private Map<Integer, K> mHolderByTagHash;

	public TagHolders() {
		mHolderByPosition = new HashMap<Integer, K>();
		mHolderByTagHash = new HashMap<Integer, K>();
	}

	public void addHolder(int tabId, K tab, T tag) {
		mHolderByPosition.put( tabId, tab );
		mHolderByTagHash.put(tag.hashCode(), tab);
		tab.setTag(new MetaTag<T>( tabId, tag));
		Log.d("position:" + tabId + " tagHash:"+ tag.hashCode());
	}

	public K getHolderByPosition( int position ) {
		return mHolderByPosition.get( position );
	}
	
	public K getHolderByFragment( int fragHashCode ) {
		Log.d("tagHash:" + fragHashCode); 
		return mHolderByTagHash.get( fragHashCode );
	}
	
	public static class MetaTag<J>{
		private int id;
		private J tag;
		private boolean enabled;

		public MetaTag(int id, J tag) {
			this.id = id;
			this.tag = tag;
			this.enabled = true;
		}

		public J getTag() {
			return tag;
		}

		public void toggleEnabled() {
			enabled = enabled ? false : true;
		}

		public boolean isEnabled() {
			return enabled;
		}
		
		public int getId(){
			return id;
		}
		
		public void setId(int value){
			id = value;
		}
	} 

}