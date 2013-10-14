package com.android.toolbox.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.support.v4.app.Fragment;
import android.view.View;

import com.android.toolbox.Log;

public class TagHolders<K extends View, T> {

	private Map<Integer, K> mHolderByPosition;
	private Map<Integer, K> mHolderByTagHash;

	public TagHolders() {
		mHolderByPosition = new LinkedHashMap<Integer, K>();
		mHolderByTagHash = new LinkedHashMap<Integer, K>();
	}

	public void addHolder(int tabId, K holder, T tag, String name) {
		mHolderByPosition.put( tabId, holder );
		if(tag != null){
			mHolderByTagHash.put(tag.hashCode(), holder);
			Log.d("position:" + tabId + " tagHash:"+ tag.hashCode());
		}
		holder.setTag(new MetaTag<T>(tabId, tag, name));

	}
	
	public void addHolder(int tabId, K holder, T tag, String name, Map<String, Object> datas) {
		addHolder(tabId, holder, tag, name);
		holder.setTag(new MetaTag<T>( tabId, tag, name, datas));
	}

	public K getHolderByPosition( int position ) {
		return mHolderByPosition.get( position );
	}
	
	public K getHolderByFragment( int fragHashCode ) {
		Log.d("tagHash:" + fragHashCode); 
		return mHolderByTagHash.get( fragHashCode );
	}
	
	public Iterable<K> getTagHolders() {
		return mHolderByPosition.values();
	}
	
	public static class MetaTag<J>{
		private int id;
		private J tag;
		private boolean enabled;
		private String name;
		private Map<String, Object> customData;
		
		public MetaTag(int id, J tag, String name) {
			this.id = id;
			this.tag = tag;
			this.enabled = true;
			this.name = name;;
		}

		public MetaTag(int id, J tag, String name, Map<String, Object> datas) {
			this(id, tag, name);
			customData = new HashMap<String, Object>(datas);
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
		
		public String getName(){
			return name;
		}
		
		public void setName(String newName){
			name = newName;
		}
		
		public Object getCustomData(String key){
			return (customData!=null)?customData.get(key):null;
		}
		
		@Override
		public String toString(){
			return name;
		}
	} 

}