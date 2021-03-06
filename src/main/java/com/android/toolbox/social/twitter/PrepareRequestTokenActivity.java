package com.android.toolbox.social.twitter;


import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.android.toolbox.Log;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;


/**
 * Prepares a OAuthConsumer and OAuthProvider 
 * 
 * OAuthConsumer is configured with the consumer key & consumer secret.
 * OAuthProvider is configured with the 3 OAuth endpoints.
 * 
 * Execute the OAuthRequestTokenTask to retrieve the request, and authorize the request.
 * 
 * After the request is authorized, a callback is made here.
 * 
 */
public class PrepareRequestTokenActivity extends Activity {

	final String TAG = getClass().getName();

	private OAuthConsumer consumer; 
	private OAuthProvider provider;
	private TwitterManager mTwitterManager = TwitterManager.getInstance();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			Log.e(TAG, "consumerKey:" + mTwitterManager.getConsumerKey() + " consumerSecret:" + mTwitterManager.getConsumerSecret());
			this.consumer = new CommonsHttpOAuthConsumer(mTwitterManager.getConsumerKey(), mTwitterManager.getConsumerSecret());
			this.provider = new CommonsHttpOAuthProvider(Constants.REQUEST_URL,Constants.ACCESS_URL,Constants.AUTHORIZE_URL);
		} catch (Exception e) {
			Log.e(TAG, "Error creating consumer / provider",e);
		}

		Log.i(TAG, "Starting task to retrieve request token.");
		new OAuthRequestTokenTask(this,consumer,provider).execute();
	}

	/**
	 * Called when the OAuthRequestTokenTask finishes (user has authorized the request token).
	 * The callback URL will be intercepted here.
	 */
	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent); 
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final Uri uri = intent.getData();
		if (uri != null && uri.getScheme().equals(Constants.OAUTH_CALLBACK_SCHEME)) {
			Log.i(TAG, "Callback received : " + uri);
			Log.i(TAG, "Retrieving Access Token");
			new RetrieveAccessTokenTask(this,consumer,provider,prefs).execute(uri);
			finish();
		}
	}

	public class RetrieveAccessTokenTask extends AsyncTask<Uri, Void, Void> {

		private Context	context;
		private OAuthProvider provider;
		private OAuthConsumer consumer;
		private SharedPreferences prefs;

		public RetrieveAccessTokenTask(Context context, OAuthConsumer consumer,OAuthProvider provider, SharedPreferences prefs) {
			this.context = context;
			this.consumer = consumer;
			this.provider = provider;
			this.prefs=prefs;
		}


		/**
		 * Retrieve the oauth_verifier, and store the oauth and oauth_token_secret 
		 * for future API calls.
		 */
		@Override
		protected Void doInBackground(Uri...params) {
			final Uri uri = params[0];
			final String oauth_verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);

			try {
				provider.retrieveAccessToken(consumer, oauth_verifier);

				final Editor edit = prefs.edit();
				edit.putString(OAuth.OAUTH_TOKEN, consumer.getToken());
				edit.putString(OAuth.OAUTH_TOKEN_SECRET, consumer.getTokenSecret());
				edit.commit();

				String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
				String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");

				consumer.setTokenWithSecret(token, secret);
				//				context.startActivity(new Intent(context, MainActivity.class));

				executeAfterAccessTokenRetrieval();
				TwitterManager.getInstance().notifySucribers(TwitterManager.Event.TWITTER_LOGGED_IN);
				TwitterManager.getInstance().getUserInfo();
				Log.i(TAG, "OAuth - Access Token Retrieved");

			} catch (Exception e) {
				Log.e(TAG, "OAuth - Access Token Retrieval Error", e);
			}

			return null;
		}


		private void executeAfterAccessTokenRetrieval() {
			Bundle bundle = getIntent().getExtras();
			if (bundle != null){
				long retweetId = bundle.getLong("retweet_id");
				String msg = bundle.getString("tweet_msg");
				byte[] byteArray = bundle.getByteArray("tweet_img");
				try {
					if (retweetId != 0) {
						mTwitterManager.retweet(prefs, retweetId);
					}else if (byteArray!=null){
						String imageName = bundle.getString("tweet_img_name");
						InputStream imgData = new ByteArrayInputStream(byteArray);
						Log.e(TAG,"sendTweetWithMedia");
						mTwitterManager.sendTweetWithMedia(prefs, msg, imageName, imgData);
					}else{
						Log.e(TAG,"sendTweet");
						mTwitterManager.sendTweet(prefs, msg);
					}
				} catch (Exception e) {
					Log.e(TAG, "OAuth - Error sending to Twitter", e);
				}
			}
		}
	}

	@Override
	protected void onRestart() {
		Log.e(TAG, "onRestart");
		//onRestart is called when we come back from the browser, using the back button, so we don't need to display this activity
		// onRestart is called when we come back from the browser, using the
		// back button, so we don't need to display this activity
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				mTwitterManager.onLoginCanceled();
				return null;
			}
		}.execute();
		
		finish();
		super.onRestart();
	}	



}
