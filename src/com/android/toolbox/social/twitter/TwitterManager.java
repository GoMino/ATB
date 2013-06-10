package com.android.toolbox.social.twitter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import com.android.toolbox.Log;

import oauth.signpost.OAuth;
import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterListener;
import twitter4j.TwitterMethod;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.media.ImageUpload;
import twitter4j.media.ImageUploaderFactory;
import twitter4j.media.MediaProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;


public class TwitterManager {
	private static final String TAG = TwitterManager.class.getSimpleName();
	private static TwitterManager instance = null;
	private HashSet<ITwitterListener> mTwitterListener;
	private Twitter mTwitter;
	private TwitterUser mTwitterUser;
	private SharedPreferences prefs;
	private Context mContext;

	public enum Event {TWITTER_LOGGED_IN, TWITTER_LOGGED_OUT, TWITTER_SESSION_VALID, TWITTER_SESSION_INVALID, TWITTER_USER_INFO_RECEIVED, TWITTER_TWEET_SENT, TWITTER_TWEET_ERROR, TWITTER_PHOTO_UPLOADED}


	private TwitterManager(){
		this.mTwitterListener = new HashSet<ITwitterListener>();
		mTwitter = new TwitterFactory().getInstance(); 

	}

	public void init(Context context) {
		mContext=context;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static TwitterManager getInstance(){
		if (instance == null)
			instance = new TwitterManager();
		return instance;
	}

	public void getUserInfo(){

		String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
		String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");

		Configuration conf = new ConfigurationBuilder()
		.setOAuthConsumerKey( Constants.CONSUMER_KEY )
		.setOAuthConsumerSecret( Constants.CONSUMER_SECRET )
		.setOAuthAccessToken(token)
		.setOAuthAccessTokenSecret(secret)
		.build();

		TwitterListener listener = new TwitterAdapter(){	

			@Override
			public void verifiedCredentials(User user) {
				Log.e("TwitterManager", "verifiedCredentials:" + user.getId());
				mTwitterUser = new TwitterUser(user);
				notifySucribers(Event.TWITTER_USER_INFO_RECEIVED);
			}

			@Override
			public void onException(TwitterException ex, TwitterMethod method) {
				Log.e("TwitterManager", "onException");
				if (method == TwitterMethod.VERIFY_CREDENTIALS) {
					Log.printStackTrace(ex);
				} else {
					throw new AssertionError("Should not happen");
				}
			}

		};
		
		AsyncTwitterFactory factory = new AsyncTwitterFactory(conf,listener);
		AsyncTwitter asyncTwitter = factory.getInstance();
		asyncTwitter.verifyCredentials();
	}

	public TwitterUser getUser(){
		return mTwitterUser;
	}


	public void login() {
		try {
			if (!new CheckIfConnected().execute(prefs).get()) {
				Intent i = new Intent(mContext, PrepareRequestTokenActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Log.e(TAG, "login | startActivity");
				mContext.startActivity(i);
			}else{
				if(getUser() == null){
					Log.e(TAG, "user is already connected but we don't have his info we should reask them");
//					getUserInfo();
				}else{
					Log.e(TAG, "user is already connected");
				}
				
			}
		} catch (InterruptedException e) {
			Log.printStackTrace(e);
		} catch (ExecutionException e) {
			Log.printStackTrace(e);
		}
	}

	public void logout() {
		clearCredentials();
		mTwitterUser = null;
		notifySucribers(Event.TWITTER_LOGGED_OUT);
	}

	private void clearCredentials() {
		final Editor edit = prefs.edit();
		edit.remove(OAuth.OAUTH_TOKEN);
		edit.remove(OAuth.OAUTH_TOKEN_SECRET);
		edit.commit();
		
//		mTwitter.setOAuthAccessToken(null);
		mTwitter.shutdown();
//		CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
//		CookieManager cookieManager = CookieManager.getInstance();
//		cookieManager.removeAllCookie();
	}

	public interface ITwitterListener{
		public void OnTwitterLoggedIn();
		public void OnTwitterLoggedOut();
		public void OnTwitterUserInfoReceived();
		public void OnTwitterTweetSent();
		public void OnTwitterTweetError();
	}

	/**
	 * Register an news listener with this manager
	 * @param listener - the News to register
	 */
	public void addListener(ITwitterListener listener){
		mTwitterListener.add(listener);
	}

	/**
	 * Unregister an News listener
	 * @param listener - the listener to unregister
	 */
	public void removeListener(ITwitterListener listener){
		mTwitterListener.remove(listener);
	}

	public void notifySucribers(Event event, Object... optionalParams){
		Log.out("Notifying all GraphAPI listener");

		for(ITwitterListener subscriber : mTwitterListener){

			switch (event) {

			case TWITTER_LOGGED_IN:
				subscriber.OnTwitterLoggedIn();
				break;

			case TWITTER_LOGGED_OUT:
				subscriber.OnTwitterLoggedOut();
				break;

			case TWITTER_USER_INFO_RECEIVED:
				subscriber.OnTwitterUserInfoReceived();
				break;
				
			case TWITTER_TWEET_SENT:
				subscriber.OnTwitterTweetSent();
				break;
				
			case TWITTER_TWEET_ERROR:
				subscriber.OnTwitterTweetError();
				break;

			default:
				break;
			}
		}
	}




	/*Twitter methods*/

	public boolean isAuthenticated(SharedPreferences prefs) {

		String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
		String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");

		//		AccessToken a = new AccessToken(token,secret);

		Configuration conf = new ConfigurationBuilder()
		//		.setMediaProviderAPIKey( Constants.TWITPIC_API_KEY )
		.setOAuthConsumerKey( Constants.CONSUMER_KEY )
		.setOAuthConsumerSecret( Constants.CONSUMER_SECRET )
		.setOAuthAccessToken(token)
		.setOAuthAccessTokenSecret(secret)
		.build();

		Twitter twitter = new TwitterFactory(conf).getInstance();
		//		twitter.setOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
		//		twitter.setOAuthAccessToken(a);

		try {
			User user = twitter.verifyCredentials();
//			twitter.getAccountSettings();
			if(user!=null){
				return true;
			}
		} catch (TwitterException e) {
			Log.printStackTrace(e);
		} catch (Exception e){
			Log.printStackTrace(e);
		}
		return false;
	}


	public class CheckIfConnected extends AsyncTask<SharedPreferences, Void, Boolean>{

		@Override
		protected Boolean doInBackground(SharedPreferences... params) {
			/**
			 * Send a tweet. If the user hasn't authenticated to Tweeter yet, he'll be redirected via a browser
			 * to the twitter login page. Once the user authenticated, he'll authorize the Android application to send
			 * tweets on the users behalf.
			 */
			if (isAuthenticated(params[0])) {
				return true;
			} else {
				return false;
			}
		}
	}



	public boolean isSessionValid(){
		try {
			if (new CheckIfConnected().execute(prefs).get()) {
				//				getUserInfo();
				return true;
			} else {
				//				mTwitterUser = null;
				return false;
			}
		} catch (InterruptedException e) {
			Log.printStackTrace(e);
		} catch (ExecutionException e) {
			Log.printStackTrace(e);
		}
		return false;
	}


	public void sendTweet(SharedPreferences prefs,String msg) throws Exception {
		String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
		String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");

		//		AccessToken a = new AccessToken(token,secret);

		Configuration conf = new ConfigurationBuilder()
		//		.setMediaProviderAPIKey( Constants.TWITPIC_API_KEY )
		.setOAuthConsumerKey( Constants.CONSUMER_KEY )
		.setOAuthConsumerSecret( Constants.CONSUMER_SECRET )
		.setOAuthAccessToken(token)
		.setOAuthAccessTokenSecret(secret)
		.build();

		Twitter twitter = new TwitterFactory(conf).getInstance();
		//		Twitter twitter = new TwitterFactory().getInstance();
		//		twitter.setOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
		//		twitter.setOAuthAccessToken(a);
		try {
			Status status = twitter.updateStatus(msg);
			Log.e(TAG, "tweet succesfully created at " + status.getCreatedAt());
			notifySucribers(Event.TWITTER_TWEET_SENT);
		} catch (Exception e) {
			Log.printStackTrace(e);
			notifySucribers(Event.TWITTER_TWEET_ERROR);
		}
		
		
	}	


	public void sendTweetWithMedia(SharedPreferences prefs,String msg, InputStream media) throws Exception {
		String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
		String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");

		Configuration conf = new ConfigurationBuilder()
		.setMediaProviderAPIKey( Constants.TWITPIC_API_KEY )
		.setOAuthConsumerKey( Constants.CONSUMER_KEY )
		.setOAuthConsumerSecret( Constants.CONSUMER_SECRET )
		.setOAuthAccessToken(token)
		.setOAuthAccessTokenSecret(secret)
		.build();



		ImageUpload upload = new ImageUploaderFactory(conf).getInstance(MediaProvider.TWITPIC);
		try {
			//String url = upload.upload( new File(media.getPath()) );
			String url = upload.upload("testFileName", media, msg);
			Log.out("Successfully uploaded image to Twitpic at " + url);
			Twitter twitter = new TwitterFactory(conf).getInstance();
			twitter.updateStatus(msg + " " + url);
		} catch (TwitterException te) {
			Log.printStackTrace(te);
			Log.out("Failed to upload the image: " + te.getMessage());
		}
	}	
	
	public void sendTweetAsync(String tweet){
		new SendTweetTask().execute(tweet);
	}
	
	public void sendTweetWithMediaAsync(String tweet, InputStream is){
		new SendTweetWithMediaTask(tweet, is).execute();
	}
	
	class SendTweetTask extends AsyncTask<String, Void, Void>{

		@Override
		protected Void doInBackground(String... params) {
			/**
			 * Send a tweet. If the user hasn't authenticated to Tweeter yet, he'll be redirected via a browser
			 * to the twitter login page. Once the user authenticated, he'll authorize the Android application to send
			 * tweets on the users behalf.
			 */
			if (TwitterManager.getInstance().isAuthenticated(prefs)) {
				try {
					sendTweet(prefs, params[0]);
				} catch (Exception e) {
					Log.printStackTrace(e);
				}	
			} else {
				Intent i = new Intent(mContext, PrepareRequestTokenActivity.class);
				i.putExtra("tweet_msg", params[0]);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(i);
			}
			return null;
		}

	}
	
	
	class SendTweetWithMediaTask extends AsyncTask<String, Void, Void>{
		private InputStream is;
		private String text;

		public SendTweetWithMediaTask(String text, InputStream inputStream){
			this.is = inputStream;
			this.text = text;
		}

		@Override
		protected Void doInBackground(String... params) {
			/**
			 * Send a tweet. If the user hasn't authenticated to Tweeter yet, he'll be redirected via a browser
			 * to the twitter login page. Once the user authenticated, he'll authorize the Android application to send
			 * tweets on the users behalf.
			 */
			if (TwitterManager.getInstance().isAuthenticated(prefs)) {
				//            		sendTweet(params[0]);
				try {
					sendTweetWithMedia(prefs, text, is);
				} catch (Exception e) {
					Log.printStackTrace(e);
				}	
			} else {
				Intent i = new Intent(mContext, PrepareRequestTokenActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("tweet_msg", text);
				
				byte[] imgData;
				try {
					imgData = getBytes(is);
					i.putExtra("tweet_img", imgData);
				} catch (IOException e) {
					Log.e(TAG,"object hasn't been serialized");
					Log.printStackTrace(e);
				}

				mContext.startActivity(i);
			}
			return null;
		}

	}
	
	public byte[] getBytes(InputStream is) throws IOException {

	    int len;
	    int size = 1024;
	    byte[] buf;

	    if (is instanceof ByteArrayInputStream) {
	      size = is.available();
	      buf = new byte[size];
	      len = is.read(buf, 0, size);
	    } else {
	      ByteArrayOutputStream bos = new ByteArrayOutputStream();
	      buf = new byte[size];
	      while ((len = is.read(buf, 0, size)) != -1)
	        bos.write(buf, 0, len);
	      buf = bos.toByteArray();
	    }
	    return buf;
	  }

	
	public static byte[] serializeObj(Object obj) throws IOException {
		  ByteArrayOutputStream baOStream = new ByteArrayOutputStream();
		  ObjectOutputStream objOStream = new ObjectOutputStream(baOStream);

		  objOStream.writeObject(obj); 
		  objOStream.flush();
		  objOStream.close();
		  return baOStream.toByteArray(); 
		}

}
