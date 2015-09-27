package com.android.toolbox.social.facebook;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.android.toolbox.Log;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class FacebookUser {

	public static final String DEFAULT_PROFILE_PICTURE_URL = "http://profile.ak.fbcdn.net/static-ak/rsrc.php/v1/yi/r/odA9sNLrE86.jpg";
	private String mUserId;
	private String mName;
	private String mFirstName;
	private String mLastName;
	private String mPicUrl;
	private Map<String, WallPost> wallPosts;
	private Bitmap mPic;
//	private List<GeoPosition> mCheckinList;


	private String mParamToken;
	
	public FacebookUser(){
		wallPosts = new HashMap<String, WallPost>();
//		mCheckinList = new ArrayList<GeoPosition>();
	}
	
	public Bitmap getImage(URL url) {
		try {
//			Object content = url.getContent();
//			InputStream is = (InputStream) content;
			InputStream is = (InputStream) url.openConnection().getInputStream();
			Bitmap b = BitmapFactory.decodeStream(is);
			is.close();
			return b;
		} catch (MalformedURLException e) {
			Log.printStackTrace(e);
			return null;
		} catch (IOException e) {
			Log.printStackTrace(e);
			return null;
		}
	}

	/**
	 * @return the mUserId
	 */
	public String getUserId() {
		return mUserId;
	}

	/**
	 * @param mUserId the mUserId to set
	 */
	public void setUserId(String mUserId) {
		this.mUserId = mUserId;
	}

	/**
	 * @return the mName
	 */
	public String getName() {
		return mName;
	}

	/**
	 * @param mName the mName to set
	 */
	public void setName(String mName) {
		this.mName = mName;
	}

	/**
	 * @return the mFirstName
	 */
	public String getFirstName() {
		return mFirstName;
	}

	/**
	 * @param mFirstName the mFirstName to set
	 */
	public void setFirstName(String mFirstName) {
		this.mFirstName = mFirstName;
	}

	/**
	 * @return the mLastName
	 */
	public String getLastName() {
		return mLastName;
	}

	/**
	 * @param mLastName the mLastName to set
	 */
	public void setLastName(String mLastName) {
		this.mLastName = mLastName;
	}

	/**
	 * @return the mParamToken
	 */
	public String getParamToken() {
		return mParamToken;
	}

	/**
	 * @param paramToken the mParamToken to set
	 */
	public void setParamToken(String paramToken) {
		this.mParamToken = paramToken;
	}
	
	/**
	 * @return the mPicUrl
	 */
	public String getPicUrl() {
		return mPicUrl;
	}

	/**
	 * @param mPicUrl the mPicUrl to set
	 */
	public void setPicUrl(String mPicUrl) {
		this.mPicUrl = mPicUrl;
		try {
			mPic = getImage(new URL(mPicUrl));
		} catch (MalformedURLException e) {
			Log.printStackTrace(e);
		}
	}
	
	/**
	 * @return the user picture as bitmap
	 */
	public Bitmap getPic() {
		return mPic;
	}
	
//	public List<GeoPosition> getCheckins() {
//		return mCheckinList;
//	}
	
	public List<WallPost> getWallPost() {
		return new ArrayList(wallPosts.values());
	}

	public void addWallPost(String id, WallPost wallPost) {
		//System.err.println("adding wallpost " + wallPost);
		this.wallPosts.put(id, wallPost);
	}
	
	public void debugWallPost(){
		for(WallPost wallpost: wallPosts.values()){
			Log.err(wallpost.toString());
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "id: " + mUserId + " name: " + mName;
	}
	
	public class WallPost implements Comparable<WallPost>{
		private long postOwnerId;
		private long postId;
		private String title;
		private String message;
		private Date created_time;
		private String picUrl;
		private int likeCounter;
		private int commentCounter;
		
		public WallPost(String id, String title, String message, Date date, String picUrl, int likeCount, int commentCount){
			StringTokenizer st = new StringTokenizer(id, "_");
			this.postOwnerId = Long.parseLong(st.nextToken());
			this.postId = Long.parseLong(st.nextToken());
			this.title = title;
			this.message = message;
			this.created_time = date;
			this.likeCounter = likeCount;
			this.commentCounter = commentCount;
			this.picUrl = picUrl;
		}
		
		public String getTitle() {
			return title;
		}
		
		public String getMessage() {
			return message;
		}
		
		public int getLikeCounter() {
			return likeCounter;
		}
		
		public int getCommentCounter() {
			return commentCounter;
		}
		
		public long getId() {
			return postId;
		}
		
		public String getObjectId() {
			return postOwnerId+"_"+postId;
		}

		public void setMessage(String message) {
			this.message = message;
		}
		
		public Date getCreationDate() {
			return created_time;
		}

		public String getPicUrl() {
			return picUrl;
		}
		
		public String getFormattedPublishDate() {
			StringBuilder result = new StringBuilder(DateFormat.getDateInstance(DateFormat.MEDIUM).format(created_time));
			return result.toString();
		}
		
		public String toString(){
			return created_time + " : " + message;
		}
		
		@Override
		public int compareTo(WallPost anotherMatch) throws ClassCastException {
			long anotherPostDate = anotherMatch.getCreationDate().getTime();  
			//return (int) (this.date - anotherMatchDate);    //result of this operation can overflow
			
			return (this.created_time.getTime() < anotherPostDate ) ? -1: (this.created_time.getTime() > anotherPostDate) ? 1:0 ;
		}
	}
	
	
	
}
