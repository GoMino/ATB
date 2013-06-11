package com.android.toolbox.social.twitter;

import java.io.InputStream;

import com.android.toolbox.Log;

import oauth.signpost.OAuth;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.media.ImageUpload;
import twitter4j.media.ImageUploaderFactory;
import twitter4j.media.MediaProvider;
import android.content.SharedPreferences;
import android.os.AsyncTask;


public class TwitterUtils {

//	public static boolean isAuthenticated(SharedPreferences prefs) {
//
//		String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
//		String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
//		
////		AccessToken a = new AccessToken(token,secret);
//		
//		Configuration conf = new ConfigurationBuilder()
////		.setMediaProviderAPIKey( Constants.TWITPIC_API_KEY )
//    	.setOAuthConsumerKey( Constants.CONSUMER_KEY )
//    	.setOAuthConsumerSecret( Constants.CONSUMER_SECRET )
//    	.setOAuthAccessToken(token)
//    	.setOAuthAccessTokenSecret(secret)
//    	.build();
//		
//		Twitter twitter = new TwitterFactory(conf).getInstance();
////		twitter.setOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
////		twitter.setOAuthAccessToken(a);
//		
//		try {
//			twitter.getAccountSettings();
//			return true;
//		} catch (TwitterException e) {
//			return false;
//		}
//	}
//	
//	
//	public static class CheckIfConnected extends AsyncTask<SharedPreferences, Void, Boolean>{
//		
//		@Override
//		protected Boolean doInBackground(SharedPreferences... params) {
//			/**
//        	 * Send a tweet. If the user hasn't authenticated to Tweeter yet, he'll be redirected via a browser
//        	 * to the twitter login page. Once the user authenticated, he'll authorize the Android application to send
//        	 * tweets on the users behalf.
//        	 */
//            	if (TwitterUtils.isAuthenticated(params[0])) {
//            		return true;
//            	} else {
//                	return false;
//            	}
//		}
//		
//		
//	}
	
	
//	public static void sendTweet(SharedPreferences prefs,String msg) throws Exception {
//		String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
//		String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
//		
////		AccessToken a = new AccessToken(token,secret);
//		
//		Configuration conf = new ConfigurationBuilder()
////		.setMediaProviderAPIKey( Constants.TWITPIC_API_KEY )
//    	.setOAuthConsumerKey( Constants.CONSUMER_KEY )
//    	.setOAuthConsumerSecret( Constants.CONSUMER_SECRET )
//    	.setOAuthAccessToken(token)
//    	.setOAuthAccessTokenSecret(secret)
//    	.build();
//	
//		Twitter twitter = new TwitterFactory(conf).getInstance();
////		Twitter twitter = new TwitterFactory().getInstance();
////		twitter.setOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
////		twitter.setOAuthAccessToken(a);
//        twitter.updateStatus(msg);
//	}	
	
	
//	public static void sendTweetWithMedia(SharedPreferences prefs,String msg, InputStream media) throws Exception {
//		String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
//		String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
//		
//		Configuration conf = new ConfigurationBuilder()
//		.setMediaProviderAPIKey( Constants.TWITPIC_API_KEY )
//    	.setOAuthConsumerKey( Constants.CONSUMER_KEY )
//    	.setOAuthConsumerSecret( Constants.CONSUMER_SECRET )
//    	.setOAuthAccessToken(token)
//    	.setOAuthAccessTokenSecret(secret)
//    	.build();
//	
//
//        
//        ImageUpload upload = new ImageUploaderFactory(conf).getInstance(MediaProvider.TWITPIC);
//        try {
//        	//String url = upload.upload( new File(media.getPath()) );
//        	String url = upload.upload("testFileName", media, msg);
//            Log.out("Successfully uploaded image to Twitpic at " + url);
//    		Twitter twitter = new TwitterFactory(conf).getInstance();
//    		twitter.updateStatus(msg + " " + url);
//        } catch (TwitterException te) {
//        	Log.printStackTrace(te);
//            Log.out("Failed to upload the image: " + te.getMessage());
//        }
//        
//
//	}	
	
	//	public static String getMediaUrlForTweet(Status tweet, String jsonString) {
//		if (!tweet.getText().contains("http")) {
//			return null;
//		}
//		String jsonString = DataObjectFactory.getRawJSON(tweet);
//		Log.i("Twitter", "JSON " + jsonString);
//		if (jsonString == null){
//			Log.w(TAG, "tweet " + tweet.getId() + " not in the object factory, can't find it's mediaUrl");
//			return null;
//		}
//		try {
//			JSONObject json = new JSONObject(jsonString);
//
//			JSONObject entities = json.getJSONObject("entities");
//			if (entities == null) {
//				return null;
//			}
//			JSONArray medias = entities.getJSONArray("media");
//			for (int i=0; i<medias.length(); i++){
//				JSONObject media = medias.getJSONObject(i);
//				if (media != null){
//					String mediaUrl = media.getString("media_url");
//					if (mediaUrl != null){
//						return mediaUrl;
//					}
//				}
//			}
//			return null;
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}
	
}
