package com.android.toolbox.social.twitter;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import com.android.toolbox.Log;

import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.User;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class TwitterUser {
	private User mUser;
	private String mPicUrl;
	private Bitmap mPic;
	
	public TwitterUser(User user) {
		this.mUser=user;
		mPicUrl = user.getProfileImageURL().toString();
		mPic=getImage(user.getProfileImageURL());
	}
	
	public Bitmap getImage(String link) {
		try {
			//http://stackoverflow.com/questions/573184/java-convert-string-to-valid-uri-object
			String urlStr = link;
			URL url = new URL(urlStr);
			URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
			url = uri.toURL();
			
			Object content = url.getContent();
			InputStream is = (InputStream) content;
			Bitmap b = BitmapFactory.decodeStream(is);
			is.close();
			return b;
		} catch (MalformedURLException e) {
			Log.printStackTrace(e);
		} catch (IOException e) {
			Log.printStackTrace(e);
		} catch (URISyntaxException e) {
			Log.printStackTrace(e);
		}
		return null;
	}

	public String getDescription() {
		return mUser.getDescription();
	}

	public long getId() {
		return mUser.getId();
	}
	
	public Date getCreatedAt() {
		return mUser.getCreatedAt();
	}
	public String getName() {
		return mUser.getName();
	}

	public Bitmap getPic() {
		return mPic;
	}
	
	public String getPicUrl() {
		return mPicUrl;
	}

	public void setPic(Bitmap picture) {
		this.mPic = picture;
	}

	public int getFavouritesCount() {
		return mUser.getFavouritesCount();
	}

	public int getFollowersCount() {
		return mUser.getFollowersCount();
	}

	public int getFriendsCount() {
		return mUser.getFriendsCount();
	}

	public String getLocation() {
		return mUser.getLocation();
	}

	public String getProfileBackgroundImageUrl() {
		return mUser.getProfileBackgroundImageUrl();
	}

	public String getProfileImageURL() {
		return mUser.getProfileImageURL();
	}

	public String getScreenName() {
		return mUser.getScreenName();
	}

	public Status getStatus() {
		return mUser.getStatus();
	}

	public int getStatusesCount() {
		return mUser.getStatusesCount();
	}

	public String getTimeZone() {
		return mUser.getTimeZone();
	}

	public String getURL() {
		return mUser.getURL();
	}

	public int getUtcOffset() {
		return mUser.getUtcOffset();
	}

	public boolean isGeoEnabled() {
		return mUser.isGeoEnabled();
	}

	public boolean isProfileBackgroundTiled() {
		return mUser.isProfileBackgroundTiled();
	}

	public boolean isProtected() {
		return mUser.isProtected();
	}


	public boolean isVerified() {
		return mUser.isVerified();
	}

	public RateLimitStatus getRateLimitStatus() {
		return mUser.getRateLimitStatus();
	}
	
	public static TwitterUser getUser(List<TwitterUser> list,int id) {
		for(TwitterUser u:list) {
			if(u.getId()==id) {
				return u;
			}
		}
		return null;
	}
	
	
}
