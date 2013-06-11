package com.android.toolbox.social.twitter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.android.toolbox.Log;
import com.android.toolbox.R;

import oauth.signpost.OAuth;
import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Paging;
import twitter4j.ResponseList;
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
import android.text.TextUtils;


public class TwitterManager {
	private static final String TAG 					= TwitterManager.class.getSimpleName();
	private String	mConsumerKey 						= null;
	private String	mConsumerSecret						= null;
	private String	mMediaProviderApiKey				= null;
	private String  mOAuthRequestCallBack				= null;
	private static final int TWEET_COUNT = 50;
	private static TwitterManager instance = null;
	private HashSet<ITwitterListener> mTwitterListener;
	private Twitter mTwitter;
	private TwitterUser mTwitterUser;
	private SharedPreferences prefs;
	private Context mContext;

	public enum Event {
		TWITTER_LOGGED_IN, TWITTER_LOGGIN_ERROR,  TWITTER_LOGGED_OUT, 
		TWITTER_SESSION_VALID, TWITTER_SESSION_INVALID, TWITTER_USER_INFO_RECEIVED, 
		TWITTER_TWEET_SENT, TWITTER_TWEET_ERROR, TWITTER_PHOTO_UPLOADED, 
		TWITTER_TIMELINE_RECEIVED, TWITTER_TIMELINE_ERROR
	}

	private TwitterManager(){
		this.mTwitterListener = new HashSet<ITwitterListener>();
		mTwitter = new TwitterFactory().getInstance(); 

	}

	public void init(Context context, String appId, String appSecret, String mediaProviderApiKey, String oAuthRequestCallbackUrl) {
		mContext = context;
		mConsumerKey = appId;
		mConsumerSecret = appSecret;
		mMediaProviderApiKey = mediaProviderApiKey;
		mOAuthRequestCallBack = oAuthRequestCallbackUrl;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static TwitterManager getInstance(){
		if (instance == null)
			instance = new TwitterManager();
		return instance;
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

	public interface ITwitterListener {
		public void OnTwitterLoggedIn();
		public void OnTwitterLoggedOut();
		public void OnTwitterLogginError(String details);
		public void OnTwitterUserInfoReceived();
		public void OnTwitterTweetSent();
		public void OnTwitterTweetError(String details);
		public void OnTwitterUserTimelineReceived(List<Status> status);
		public void OnTwitterUserTimelineError(String error);
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
		Log.v("Notifying all TwitterManager listener");
		String error = null;
		for(ITwitterListener subscriber : mTwitterListener){

			switch (event) {

			case TWITTER_LOGGED_IN:
				subscriber.OnTwitterLoggedIn();
				break;

			case TWITTER_LOGGED_OUT:
				subscriber.OnTwitterLoggedOut();
				break;
				
			case TWITTER_LOGGIN_ERROR:
				error = (optionalParams!=null && optionalParams.length>0)?(String)optionalParams[0]:null;
				subscriber.OnTwitterLogginError(error);
				break;

			case TWITTER_USER_INFO_RECEIVED:
				subscriber.OnTwitterUserInfoReceived();
				break;
				
			case TWITTER_TWEET_SENT:
				subscriber.OnTwitterTweetSent();
				break;
				
			case TWITTER_TWEET_ERROR:
				error = (optionalParams!=null && optionalParams.length>0)?(String)optionalParams[0]:null;
				subscriber.OnTwitterTweetError(error);
				break;
				
			case TWITTER_TIMELINE_RECEIVED:
				subscriber.OnTwitterUserTimelineReceived((List<Status>) optionalParams[0]);
				break;

			case TWITTER_TIMELINE_ERROR:
				subscriber.OnTwitterUserTimelineError((String) optionalParams[0]);
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

		if (TextUtils.isEmpty(token) || TextUtils.isEmpty(secret)) {
			return false;
		}
		
		//		AccessToken a = new AccessToken(token,secret);

		Configuration conf = getTweetConfiguration(prefs);

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

	public void getUserInfo(){

		Configuration conf = getTweetConfiguration(prefs);

		TwitterListener listener = new TwitterAdapter(){	

			@Override
			public void verifiedCredentials(User user) {
				Log.e(TAG, "verifiedCredentials:" + user.getId());
				mTwitterUser = new TwitterUser(user);
				notifySucribers(Event.TWITTER_USER_INFO_RECEIVED);
			}

			@Override
			public void onException(TwitterException ex, TwitterMethod method) {
				Log.e(TAG, "onException");
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

	public void getPublicUserTimeline(final String username) {

		AsyncTwitter asyncTwitter = null;

		TwitterListener listener = new TwitterAdapter() {

			@Override
			public void gotUserTimeline(ResponseList<Status> statuses) {
				Log.e(TAG, "gotUserTimeline:" + username);
				notifySucribers(Event.TWITTER_TIMELINE_RECEIVED, statuses);
			}
			
			@Override
			public void onException(TwitterException ex, TwitterMethod method) {
				Log.e(TAG, "onException");
				// if (method == TwitterMethod.VERIFY_CREDENTIALS) {
				Log.printStackTrace(ex);
				// } else {
				// throw new AssertionError("Should not happen");
				// }
				notifySucribers(Event.TWITTER_TIMELINE_ERROR, ex.getMessage());
			}

		};

		if (getUser() != null && isAuthenticated(prefs)) {
			// authanticated user rate limit 350 request an hour base on token
			String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
			String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");

			Configuration conf = getTweetConfiguration(prefs);

			AsyncTwitterFactory factory = new AsyncTwitterFactory(conf, listener);
			asyncTwitter = factory.getInstance();
			Log.e(TAG, "getPublicUserTimeline authenticated");
		} else {
			// unauthenticated user rate limit 150 request an hour base on IP
			Configuration unauthenticatedConf = new ConfigurationBuilder().setIncludeEntitiesEnabled(true).build();
			AsyncTwitterFactory factory = new AsyncTwitterFactory(unauthenticatedConf, listener);
			asyncTwitter = factory.getInstance();
			Log.e(TAG, "getPublicUserTimeline unauthenticated");
		}

		// First param of Paging() is the page number, second is the number per
		// page (this is capped around 200 I think.
		Paging paging = new Paging(1, TWEET_COUNT);
		asyncTwitter.getUserTimeline(username, paging);
		// asyncTwitter.getUserListStatuses(username, id, paging);

	}

	public void getPublicUserListTimeline(final String username, int listId) {

		AsyncTwitter asyncTwitter = null;

		TwitterListener listener = new TwitterAdapter() {

			@Override
			public void gotUserListStatuses(ResponseList<Status> statuses) {
				Log.e(TAG, "gotUserTimeline:" + username);
				notifySucribers(Event.TWITTER_TIMELINE_RECEIVED, statuses);
			}

			@Override
			public void onException(TwitterException ex, TwitterMethod method) {
				Log.e(TAG, "onException");
				// if (method == TwitterMethod.VERIFY_CREDENTIALS) {
				Log.printStackTrace(ex);
				// } else {
				// //throw new AssertionError("Should not happen");
				// Log.e(TAG, "Should not happen");
				// }
				notifySucribers(Event.TWITTER_TIMELINE_ERROR, ex.getMessage());
			}

		};

		if (getUser() != null && isAuthenticated(prefs)) {
			// authanticated user rate limit 350 request an hour base on token

			Configuration conf = getTweetConfiguration(prefs);

			AsyncTwitterFactory factory = new AsyncTwitterFactory(conf, listener);
			asyncTwitter = factory.getInstance();
			Log.e(TAG, "getPublicUserListTimeline authenticated");
		} else {
			// unauthenticated user rate limit 150 request an hour base on IP
			Configuration unauthenticatedConf = new ConfigurationBuilder().setIncludeEntitiesEnabled(true).build();
			AsyncTwitterFactory factory = new AsyncTwitterFactory(unauthenticatedConf, listener);
			asyncTwitter = factory.getInstance();
			Log.e(TAG, "getPublicUserListTimeline unauthenticated");
		}

		// First param of Paging() is the page number, second is the number per
		// page (this is capped around 200 I think.
		Paging paging = new Paging(1, TWEET_COUNT);
		asyncTwitter.getUserListStatuses(username, listId, paging);

	}
	
	private Configuration getTweetConfiguration(SharedPreferences prefs){
		String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
		String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");

		return new ConfigurationBuilder().setMediaProviderAPIKey(mMediaProviderApiKey)
				.setOAuthConsumerKey(mConsumerKey).setOAuthConsumerSecret(mConsumerSecret)
				.setOAuthAccessToken(token).setOAuthAccessTokenSecret(secret).build();

	}

	public void sendTweet(SharedPreferences prefs,String msg) throws Exception {
		Configuration conf = getTweetConfiguration(prefs);

		Twitter twitter = new TwitterFactory(conf).getInstance();
		//		Twitter twitter = new TwitterFactory().getInstance();
		//		twitter.setOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
		//		twitter.setOAuthAccessToken(a);
		try {
			Status status = twitter.updateStatus(msg);
			Log.e(TAG, "tweet succesfully created at " + status.getCreatedAt());
			notifySucribers(Event.TWITTER_TWEET_SENT);
		}catch (TwitterException e){
			Log.printStackTrace(e);
			switch (e.getStatusCode()) {
			   case (-1):
			   		notifySucribers(Event.TWITTER_TWEET_ERROR, mContext.getResources().getString(R.string.tweet_error_network));
			   		break;
			   case(403):
			    	notifySucribers(Event.TWITTER_TWEET_ERROR, mContext.getResources().getString(R.string.tweet_error_duplicate));
			        break;
			   default:
			    	notifySucribers(Event.TWITTER_TWEET_ERROR);
			}
		} catch (Exception e) {
			Log.printStackTrace(e);
			notifySucribers(Event.TWITTER_TWEET_ERROR);
		}
	}	

	public void onLoginCanceled(){
		notifySucribers(Event.TWITTER_LOGGIN_ERROR);
	}


	public void sendTweetWithMedia(SharedPreferences prefs,String msg, String imageName, InputStream media) throws Exception {
		Configuration conf = getTweetConfiguration(prefs);

		ImageUpload upload = new ImageUploaderFactory(conf).getInstance(MediaProvider.TWITPIC);
		try {
			//String url = upload.upload( new File(media.getPath()) );
			String url = upload.upload(imageName, media, msg);
			Log.out("Successfully uploaded image to Twitpic at " + url);
			Twitter twitter = new TwitterFactory(conf).getInstance();
			twitter.updateStatus(msg + " " + url);
			notifySucribers(Event.TWITTER_TWEET_SENT);
		}catch (TwitterException e){
			Log.printStackTrace(e);
			Log.out("Failed to upload the image: " + e.getMessage());
			switch (e.getStatusCode()) {
			   case (-1):
			   		notifySucribers(Event.TWITTER_TWEET_ERROR, mContext.getResources().getString(R.string.tweet_error_network));
			   		break;
			   case(403):
			    	notifySucribers(Event.TWITTER_TWEET_ERROR, mContext.getResources().getString(R.string.tweet_error_duplicate));
			        break;
			   default:
			    	notifySucribers(Event.TWITTER_TWEET_ERROR);
			}
		}catch (Exception te) {
			notifySucribers(Event.TWITTER_TWEET_ERROR);
		}
	}	
	
	public void retweet(SharedPreferences prefs, long tweetId) {
		Configuration conf = getTweetConfiguration(prefs);
		
		Twitter twitter = new TwitterFactory(conf).getInstance();
		// Twitter twitter = new TwitterFactory().getInstance();
		// twitter.setOAuthConsumer(Constants.CONSUMER_KEY,
		// Constants.CONSUMER_SECRET);
		// twitter.setOAuthAccessToken(a);
		try {
			twitter.retweetStatus(tweetId);
			notifySucribers(Event.TWITTER_TWEET_SENT);
		} catch (TwitterException e){
			Log.printStackTrace(e);
//			Log.err(e.getMessage());
//			Log.err(e.toString());
//			Log.err("is caused by network issue:" + e.isCausedByNetworkIssue());
//			Log.err("status code:" + e.getStatusCode());
			switch (e.getStatusCode()) {

		    case (-1):
//		        showNotification("Twitter (unable to connect)", TWIT_FAIL);
		    	notifySucribers(Event.TWITTER_TWEET_ERROR, mContext.getResources().getString(R.string.tweet_error_network));
		        break;
		    case(403):
//		        showNotification("Twitter (duplicate tweet)", TWIT_FAIL);
		    	notifySucribers(Event.TWITTER_TWEET_ERROR, mContext.getResources().getString(R.string.tweet_error_duplicate));
		        break;
		    default:
		    	notifySucribers(Event.TWITTER_TWEET_ERROR);
			}
		} catch (Exception e) {
			Log.printStackTrace(e);
			notifySucribers(Event.TWITTER_TWEET_ERROR);
		}
	}
	
	
	public void sendTweetAsync(String tweet){
		new SendTweetTask().execute(tweet);
	}
	
	public void sendTweetWithMediaAsync(String tweet, String imageName, InputStream is){
		new SendTweetWithMediaTask(tweet, imageName, is).execute();
	}
	
	public void retweetAsync(long tweetId) {
		new SendRetweetTask().execute(tweetId);
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
	
	class SendRetweetTask extends AsyncTask<Long, Void, Void> {

		@Override
		protected Void doInBackground(Long... params) {
			/**
			 * Send a tweet. If the user hasn't authenticated to Tweeter yet,
			 * he'll be redirected via a browser to the twitter login page. Once
			 * the user authenticated, he'll authorize the Android application
			 * to send tweets on the users behalf.
			 */
			if (TwitterManager.getInstance().isAuthenticated(prefs)) {
				try {
					retweet(prefs, params[0]);
				} catch (Exception e) {
					Log.printStackTrace(e);
				}
			} else {
				Intent i = new Intent(mContext, PrepareRequestTokenActivity.class);
				// i.putExtra("tweet_msg", params[0]);
				i.putExtra("retweet_id", params[0]);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(i);
			}
			return null;
		}

	}
	
	
	class SendTweetWithMediaTask extends AsyncTask<String, Void, Void>{
		private InputStream is;
		private String text;
		private String imageName;

		public SendTweetWithMediaTask(String text, String imageName, InputStream inputStream){
			this.imageName = imageName;
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
					sendTweetWithMedia(prefs, text, imageName, is);
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
					i.putExtra("tweet_img_name", imageName);
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

	public String getConsumerKey(){
		return mConsumerKey;
	}
	
	public String getConsumerSecret(){
		return mConsumerSecret;
	}
	
	public String getMediaProviderApiKey(){
		return mMediaProviderApiKey;
	}
	
	public String getOAuthRequestCallBack(){
		return Constants.OAUTH_CALLBACK_SCHEME + "://" + mOAuthRequestCallBack;
	}
}
