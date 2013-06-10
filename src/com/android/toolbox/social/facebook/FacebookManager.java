package com.android.toolbox.social.facebook;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.android.toolbox.Log;
import com.android.toolbox.social.facebook.FacebookUser.WallPost;
import com.android.toolbox.social.facebook.SessionEvents.AuthListener;
import com.android.toolbox.social.facebook.SessionEvents.LogoutListener;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;

public class FacebookManager {

	private static final String TAG = FacebookManager.class.getSimpleName();
	private static FacebookManager instance = null;
	private Facebook facebook = null;
	private Facebook facebookapp = null;
	private Context context;
	private String[] permissions;
	private Handler mHandler;
	private Activity activity;
	private SessionListener mSessionListener;
	private FacebookUser mFacebookLoggedUser;
	private Map<String, FacebookUser> mFacebookFriends;
	private FacebookUser mFacebookEuro2012User;
	private HashSet<FacebookGraphAPIRequestListener> mGraphAPIListeners;
	private enum Event {FACEBOOK_USER_INFO_RECEIVED, FACEBOOK_FRIENDS_CHECKINS_RECEIVED, FACEBOOK_EURO2012_WALL_POST_RECEIVED, FACEBOOK_POST_SENT, FACEBOOK_LIKE_DONE, FACEBOOK_PHOTO_UPLOADED, FACEBOOK_LOGGING_OUT_BEGIN, FACEBOOK_LOGGING_OUT_END, FACEBOOK_AUTH_FAILED}
	private static String EURO2012_ID = "uefaeuro2012";
	private boolean gotFriendsCheckin;
	//private static String EURO2012_ID = "301256806313";

	private FacebookManager(String appId, String[] permissions){
		this.facebookapp = new Facebook(appId);
		this.facebook = new Facebook(appId);
		this.mGraphAPIListeners = new HashSet<FacebookManager.FacebookGraphAPIRequestListener>();
		this.mSessionListener = new SessionListener();

		SessionEvents.addAuthListener(mSessionListener);
		SessionEvents.addLogoutListener(mSessionListener);

		this.mFacebookEuro2012User = new FacebookUser();
		this.permissions = permissions;
		mFacebookFriends =  new LinkedHashMap<String, FacebookUser>();

	}

	public static FacebookManager getInstance(Object... optionnalParams){
		if (instance == null)
			instance = new FacebookManager( (String)optionnalParams[0], (String[]) optionnalParams[1]);
		return instance;
	}

	public void init(Context context) {
		// restore session if one exists
		SessionStore.restore(facebook, context);
		this.context=context;
		this.activity = (Activity) context;
		this.mHandler = new Handler();
	}

	public void login() {
		if (!facebook.isSessionValid()) {
			//facebook.authorize(this.activity, this.permissions,new LoginDialogListener());
			facebook.authorize(this.activity, this.permissions,Facebook.FORCE_DIALOG_AUTH, new LoginDialogListener());
		}
	}

	public void logout() {
		SessionEvents.onLogoutBegin();
		AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(this.facebook);
		asyncRunner.logout(this.context, new LogoutRequestListener());
	}

	public String getFacebookAppAccessToken(String facebookAppId, String facebookAppSecret){
		return Utility.getAppToken(facebookAppId, facebookAppSecret);
	}

	public Map<String, FacebookUser> getFacebookFriends(){
		return mFacebookFriends;
	}

//	public void clearFriendsCheckins(){
//		for(FacebookUser friend: mFacebookFriends.values()){
//			friend.getCheckins().clear();
//		}
//	}

	public void getUserInfo() {
		if (facebook.isSessionValid()) {
			AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(this.facebook);
			Bundle params = new Bundle();
			params.putString("fields", "name, picture");
			asyncRunner.request("me", params, new UserInfoRequestListener());
			//asyncRunner.request("me", new UserInfoRequestListener());
		} else {
			login();
		}
	}
//TODO
//	public void getEuro2012WallPost() {
//		//		if (facebook.isSessionValid()) {
//		//			AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(this.facebook);
//		//			Bundle params = new Bundle();
//		//			params.putString("fields", "message, created_time, likes, comments, picture");
//		//			// get the posts made by the "EURO2012_ID" page
//		//			asyncRunner.request(EURO2012_ID+"/posts", params, new PageRequestListener());
//		//		} else {
//		new NetworkTask().execute();
//		//login();
//		//		}
//	}

//TODO
//	class NetworkTask extends AsyncTask<Void, Void, String>{	
//		@Override
//		protected String doInBackground(Void... params) {
//			return getFacebookAppAccessToken();
//		}
//		@Override
//		protected void onPostExecute(String token) {
//			facebookapp.setAccessToken(token);
//			AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(facebookapp);
//			Bundle params = new Bundle();
//			params.putString("fields", "message, created_time, name, likes, comments, picture");
//			asyncRunner.request(EURO2012_ID+"/posts", params, new PageRequestListener());
//		}	
//	}

//	public void getFriendsCheckin(){
//		if (facebook.isSessionValid()) {
//			if (!gotFriendsCheckin){
//				AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(this.facebook);
//
//				Bundle params = new Bundle();
//				JSONObject jsonFQL = new JSONObject();
//
//
//				try {
//					//#Query 1
//					jsonFQL.put("query1", "SELECT author_uid, coords, message FROM checkin WHERE author_uid IN (SELECT uid2 FROM friend WHERE uid1=me())");
//
//					//#Query 2, use author_uid from #Query1
//					jsonFQL.put("query2", "SELECT first_name, last_name, pic_small, uid FROM user WHERE uid IN (SELECT author_uid from #query1)");
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//
//
//				params.putString("method", "fql.multiquery");
//				params.putString("queries", jsonFQL.toString());
//				//			String response = facebook.request(params);
//
//				//			Bundle params = new Bundle();
//				//	        params.putString("method", "fql.query");
//				//	        params.putString("query", "SELECT author_uid, coords, message FROM checkin WHERE author_uid IN (SELECT uid2 FROM friend WHERE uid1=me())");
//				//	        SELECT first_name, pic_small, uid FROM user WHERE uid IN (SELECT author_uid from #query1)
//				//	        params.putString("query", "SELECT name, uid, pic_square FROM user WHERE uid IN (SELECT uid2 FROM friend WHERE uid1=me()) ORDER BY name");
//				asyncRunner.request(params, new FriendsCheckinRequestListener());
//			}else{
//				notifySucribers(Event.FACEBOOK_FRIENDS_CHECKINS_RECEIVED);
//			}
//
//		} else {
//
//			SessionEvents.AuthListener listener = new SessionEvents.AuthListener() {
//
//				@Override
//				public void onAuthSucceed() {
//					getFriendsCheckin();
//				}
//
//				@Override
//				public void onAuthFail(String error) {
//
//				}
//			};
//			SessionEvents.addAuthListener(listener);
//
//			login();
//		}
//	}


	public void postMessageOnWall(final String msg) {
		if (facebook.isSessionValid()) {
			AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(this.facebook);
			String graphPath = "me/feed";
			Bundle params = new Bundle();
			params.putString("message", msg);
			//new SendFacebookPostRequestTask(graphPath, params).execute();
			asyncRunner.request(graphPath, params, "POST", new PostRequestListener(), null);
			notifySucribers(Event.FACEBOOK_POST_SENT);
			//			try {
			//				String response = facebook.request(graphPath, params, "POST");
			//				System.out.println(response);
			//			} catch (IOException e) {
			//				e.printStackTrace();
			//			}
			//		    facebook.dialog(context, graphPath, params, new PostDialogListener());
		} else {

			SessionEvents.AuthListener listener = new SessionEvents.AuthListener() {

				@Override
				public void onAuthSucceed() {
					postMessageOnWall(msg);
				}

				@Override
				public void onAuthFail(String error) {

				}
			};
			SessionEvents.addAuthListener(listener);

			login();
		}
	}	

	public void shareCommentOnPost(final String postId, final String message) {
		if (facebook.isSessionValid()) {
			AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(this.facebook);
			String graphPath = postId + "/comments";
			Bundle params = new Bundle();
			params.putString("message", message);
			//new SendFacebookPostRequestTask(graphPath, params).execute();
			asyncRunner.request(graphPath, params, "POST", new PostRequestListener(), null);
			notifySucribers(Event.FACEBOOK_POST_SENT);
			//			try {
			//				String response = facebook.request(graphPath, params, "POST");
			//				System.out.println(response);
			//			} catch (IOException e) {
			//				e.printStackTrace();
			//			}
			//		    facebook.dialog(context, graphPath, params, new PostDialogListener());
		} else {

			SessionEvents.AuthListener listener = new SessionEvents.AuthListener() {

				@Override
				public void onAuthSucceed() {
					shareCommentOnPost(postId, message);
				}

				@Override
				public void onAuthFail(String error) {

				}
			};
			SessionEvents.addAuthListener(listener);

			login();

		}
	}

	public void likePost(final String postId) {
		if (facebook.isSessionValid()) {
			AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(this.facebook);
			String graphPath = postId + "/likes";
			asyncRunner.request(graphPath, new Bundle(), "POST", new LikeRequestListener(), null);
			//new SendFacebookPostRequestTask(graphPath, new Bundle()).execute();
			//notifySucribers(Event.FACEBOOK_LIKE_DONE);
			//		    try {
			//				String response = facebook.request(graphPath, new Bundle(), "POST");
			//				System.out.println(response);
			//			} catch (IOException e) {
			//				e.printStackTrace();
			//			}
		} else {
			SessionEvents.AuthListener listener = new SessionEvents.AuthListener() {

				@Override
				public void onAuthSucceed() {
					likePost(postId);
				}

				@Override
				public void onAuthFail(String error) {

				}
			};
			SessionEvents.addAuthListener(listener);

			login();
		}
	}

	public void shareLinkOnWall(final String message, final String url) {
		if (facebook.isSessionValid()) {
			AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(this.facebook);
			String graphPath = "me/feed";
			Bundle params = new Bundle();
			params.putString("message", message);
			params.putString("link", url);
			//new SendFacebookPostRequestTask(graphPath, params).execute();
			asyncRunner.request(graphPath, params, "POST", new PostRequestListener(), null);
			notifySucribers(Event.FACEBOOK_POST_SENT);
			//			try {
			//				String response = facebook.request(graphPath, params, "POST");
			//				System.out.println(response);
			//			} catch (IOException e) {
			//				e.printStackTrace();
			//			}
			//		    facebook.dialog(context, graphPath, params, new PostDialogListener());
		} else {

			SessionEvents.AuthListener listener = new SessionEvents.AuthListener() {

				@Override
				public void onAuthSucceed() {
					shareLinkOnWall(message, url);
				}

				@Override
				public void onAuthFail(String error) {

				}
			};
			SessionEvents.addAuthListener(listener);

			login();
		}
	}

	public void sharePictureOnWall(final String message, final Bitmap bitmap) {
		if (facebook.isSessionValid()) {
			AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(this.facebook);
			//			String graphPath = "me/feed";
			Bundle params = new Bundle();
			params.putString("caption", message);
			params.putString("method", "photos.upload");


			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

			byte[] imageInByte = stream.toByteArray();
			params.putByteArray("picture", imageInByte);


			//			URL uploadFileUrl = null;
			//            try {
			//                uploadFileUrl = new URL(
			//                    "http://www.facebook.com/images/devsite/iphone_connect_btn.jpg");
			//            } catch (MalformedURLException e) {
			//            	e.printStackTrace();
			//            }
			//            try {
			//                HttpURLConnection conn= (HttpURLConnection)uploadFileUrl.openConnection();
			//                conn.setDoInput(true);
			//                conn.connect();
			//                int length = conn.getContentLength();
			//
			//                byte[] imgData =new byte[length];
			//                InputStream is = conn.getInputStream();
			//                is.read(imgData);
			//                params.putByteArray("picture", imgData);
			//
			//            } catch  (IOException e) {
			//                e.printStackTrace();
			//            }

			//            asyncRunner.request(null, params, "POST", new SampleUploadListener(), null);

			//new SendFacebookPostRequestTask(graphPath, params).execute();
			asyncRunner.request(null, params, "POST", new SampleUploadListener(), null);
			notifySucribers(Event.FACEBOOK_PHOTO_UPLOADED);
			//			try {
			//				String response = facebook.request(graphPath, params, "POST");
			//				System.out.println(response);
			//			} catch (IOException e) {
			//				e.printStackTrace();
			//			}
			//		    facebook.dialog(context, graphPath, params, new PostDialogListener());
		} else {

			SessionEvents.AuthListener listener = new SessionEvents.AuthListener() {

				@Override
				public void onAuthSucceed() {
					sharePictureOnWall(message, bitmap);
				}

				@Override
				public void onAuthFail(String error) {

				}
			};
			SessionEvents.addAuthListener(listener);

			login();
		}

	}

	public class PostRequestListener extends BaseRequestListener {
		public void onComplete(String response, final Object state) {
			// callback should be run in the original thread, 
			// not the background thread
			Log.e(TAG,"PostRequestListener complete");
			mHandler.post(new Runnable() {
				public void run() {
					//					Toast.makeText(context, "Comment posted !", Toast.LENGTH_LONG).show();
				}
			});
			notifySucribers(Event.FACEBOOK_POST_SENT);
		}
	}

	public class LikeRequestListener extends BaseRequestListener {
		public void onComplete(String response, final Object state) {
			// callback should be run in the original thread, not the background thread
			Log.e(TAG,"LikeRequestListener complete");
			mHandler.post(new Runnable() {
				public void run() {
					//					Toast.makeText(context, "You liked it!", Toast.LENGTH_LONG).show();
				}
			});
			notifySucribers(Event.FACEBOOK_LIKE_DONE);
		}
	}

	public class SampleUploadListener extends BaseRequestListener {

		public void onComplete(final String response, final Object state) {
			Log.e(TAG,"SampleUploadListener complete");
			try {
				// process the response here: (executed in background thread)
				Log.d("Facebook-Example", "Response: " + response.toString());
				JSONObject json = Util.parseJson(response);
				final String src = json.getString("src");

				// then post the processed result back to the UI thread
				// if we do not do this, an runtime exception will be generated
				// e.g. "CalledFromWrongThreadException: Only the original
				// thread that created a view hierarchy can touch its views."

				mHandler.post(new Runnable() {
					public void run() {
						//						Toast.makeText(context, "Your photo has been uploaded at \n" + src, Toast.LENGTH_LONG).show();
					}
				});
				notifySucribers(Event.FACEBOOK_PHOTO_UPLOADED);
			} catch (JSONException e) {
				Log.w("Facebook-Example", "JSON Error in response");
			} catch (FacebookError e) {
				Log.w("Facebook-Example", "Facebook Error: " + e.getMessage());
			}
		}
	}

	private final class LoginDialogListener implements DialogListener {
		public void onComplete(Bundle values) {
			SessionEvents.onLoginSuccess();
		}

		public void onFacebookError(FacebookError error) {
			SessionEvents.onLoginError(error.getMessage());
		}

		public void onError(DialogError error) {
			SessionEvents.onLoginError(error.getMessage());
		}

		public void onCancel() {
			SessionEvents.onLoginError("Action Canceled");
		}
	}

	public class LogoutRequestListener extends BaseRequestListener {
		public void onComplete(String response, final Object state) {
			// callback should be run in the original thread, 
			// not the background thread
			mHandler.post(new Runnable() {
				public void run() {
					SessionEvents.onLogoutFinish();
					gotFriendsCheckin = false; //so that the list of friends will be cleared when we change session
					Log.e(TAG, "friends checkins deleted");
				}
			});
		}
	}

	private class SessionListener implements AuthListener, LogoutListener {

		public void onAuthSucceed() {
			SessionStore.save(facebook, context);
			getUserInfo();
		}

		public void onAuthFail(String error) {
			notifySucribers(Event.FACEBOOK_AUTH_FAILED);
		}

		public void onLogoutBegin() {   
			notifySucribers(Event.FACEBOOK_LOGGING_OUT_BEGIN);
		}

		public void onLogoutFinish() {
			SessionStore.clear(context);
			mFacebookLoggedUser = null;
			notifySucribers(Event.FACEBOOK_LOGGING_OUT_END);
		}
	}

	public Facebook getFacebook() {
		return this.facebook;
	}

	public String[] getPermission() {
		return this.permissions;
	}

	public FacebookUser getUser() {
		return this.mFacebookLoggedUser;
	}

	public FacebookUser getEuro2012User() {
		return this.mFacebookEuro2012User;
	}



	public class UserInfoRequestListener extends BaseRequestListener {
		public void onComplete(String response, final Object state) {

			Log.e(TAG,"graph/me complete");
			Log.i(TAG,"response: "+response);
			try {

				// process the response here: executed in background thread
				JSONObject json = Util.parseJson(response);

				mFacebookLoggedUser = new FacebookUser();

				mFacebookLoggedUser.setUserId(json.getString("id"));
				mFacebookLoggedUser.setName(json.getString("name"));
				//mFacebookUser.setFirstName(json.getString("first_name"));
				//mFacebookUser.setLastName(json.getString("last_name"));
				
				JSONObject pictureObject = json.getJSONObject("picture");
				JSONObject dataObject = pictureObject.getJSONObject("data");
//				for (int i = 0; i < dataObject.length(); i++) {
//					JSONObject row = dataObject.getJSONObject(i);
					Log.i(TAG,"dataObject : "+dataObject);              
					
					String url = null;
					boolean is_silhouette = false;

					if(dataObject.has("is_silhouette")){
						is_silhouette = dataObject.getBoolean("is_silhouette");
					}
					
					if(dataObject.has("url")){
						url = dataObject.getString("url");
						mFacebookLoggedUser.setPicUrl(url);
					}
//				}
				


				Log.e(TAG,"User: " + mFacebookLoggedUser + " picture " + mFacebookLoggedUser.getPicUrl());
				notifySucribers(Event.FACEBOOK_USER_INFO_RECEIVED);

			} catch (JSONException e) {
				Log.w(TAG, "JSON Error in response");
				Log.w(TAG,"cause: "+e.getCause());
				Log.w(TAG,"message: "+e.getMessage());
			} catch (FacebookError e) {
				Log.w(TAG, "Facebook Error: " + e.getMessage());
			}


		}
	}

//	public class FriendsCheckinRequestListener extends BaseRequestListener {
//		public void onComplete(String response, final Object state) {
//
//			Log.e(TAG,"FriendsCheckinRequestListener complete");
//			Log.i(TAG,"response: "+response);
//			response = "{\"data\":" + response + "}";
//			try {
//
//				JSONObject json = Util.parseJson( response );
//				JSONArray array = json.getJSONArray("data");
//				for (int i = 0; i < array.length(); i++) {
//
//					JSONObject queryRow = array.getJSONObject(i);
//					Log.i(TAG,"query row : "+queryRow);              
//
//
//					if(queryRow.has("name")){ 
//						String queryName = queryRow.getString("name");
//
//						if (queryName.equals("query1")){
//							if(queryRow.has("fql_result_set")){
//								JSONArray checkinArray = queryRow.getJSONArray("fql_result_set");
//
//								//delete all previous checkins
//								clearFriendsCheckins();
//
//								for (int j = 0; j < checkinArray.length(); j++) {
//									JSONObject checkinRow = checkinArray.getJSONObject(j);
//
//									String author_uid = null;
//									String messsage = null;
//									double latitude = 0;
//									double longitude = 0;
//
//									Log.i(TAG,"checkin row : "+checkinRow); 
//
//									if (checkinRow.has("message")){
//										messsage = checkinRow.getString("message");
//									}
//
//									if (checkinRow.has("author_uid")){
//										author_uid = Integer.toString(checkinRow.getInt("author_uid"));
//									}
//
//									if (checkinRow.has("coords")){
//										JSONObject coordsRow = checkinRow.getJSONObject("coords");
//
//										Log.i(TAG,"coords row : "+coordsRow); 
//										if (coordsRow.has("latitude")){
//											latitude = coordsRow.getDouble("latitude");
//										}
//
//										if (coordsRow.has("longitude")){
//											longitude = coordsRow.getDouble("longitude");
//										}
//									}
//
//									if (StringUtililty.notEmpty(author_uid)){
//										FacebookUser friendUser = null;
//										if (mFacebookFriends.containsKey(author_uid)){
//											friendUser =  mFacebookFriends.get(author_uid);
//										}else{
//											friendUser = new FacebookUser();
//											mFacebookFriends.put(author_uid, friendUser);
//										}
//
//										if (friendUser != null){
//											friendUser.setUserId(author_uid);
//											if (latitude!=0 && longitude!=0){
//												GeoPosition geoPoint = new GeoPosition(latitude, longitude);
//												friendUser.getCheckins().add(geoPoint);
//												geoPoint.setDescription(messsage);
//												geoPoint.setName(author_uid);
//												geoPoint.setTag(friendUser);
//
//												//now we have set all the immutable member, we can calculate the geoPoint Id;
//												geoPoint.setId(geoPoint.hashCode());
//											}
//										}
//
//									} 
//
//								}
//							}
//						}
//						else if (queryName.equals("query2")){
//							if(queryRow.has("fql_result_set")){
//								JSONArray friendArray = queryRow.getJSONArray("fql_result_set");
//								for (int j = 0; j < friendArray.length(); j++) {
//									JSONObject friendRow = friendArray.getJSONObject(j);
//
//									String userId = null;
//									String firstname = null;
//									String lastname = null;
//									String profilePictureLink = null;
//
//									Log.i(TAG,"friend row : "+friendRow); 
//
//									if (friendRow.has("first_name")){
//										firstname = friendRow.getString("first_name");
//									}
//
//									if (friendRow.has("last_name")){
//										lastname = friendRow.getString("last_name");
//									}
//
//									if (friendRow.has("uid")){
//										userId = Integer.toString(friendRow.getInt("uid"));
//									}
//
//									if (friendRow.has("pic_small")){
//										profilePictureLink = friendRow.getString("pic_small");
//									}
//
//									if (StringUtililty.notEmpty(userId)){
//										FacebookUser friendUser = null;
//										if (mFacebookFriends.containsKey(userId)){
//											friendUser =  mFacebookFriends.get(userId);
//										}else{
//											friendUser = new FacebookUser();
//											mFacebookFriends.put(userId, friendUser);
//										}
//
//										if (friendUser != null){
//											friendUser.setUserId(userId);
//											if (StringUtililty.notEmpty(firstname))
//												friendUser.setFirstName(firstname);
//											if (StringUtililty.notEmpty(lastname))
//												friendUser.setLastName(lastname);
//											if (StringUtililty.notEmpty(profilePictureLink))
//												friendUser.setPicUrl(profilePictureLink);
//										}
//
//
//									}
//								}
//							}
//						}
//					}
//				}
//				
//				gotFriendsCheckin = true;
//				notifySucribers(Event.FACEBOOK_FRIENDS_CHECKINS_RECEIVED);
//
//			}catch (JSONException e) {
//				Log.w(TAG, "JSON Error in response");
//				Log.w(TAG,"cause: "+e.getCause());
//				Log.w(TAG,"message: "+e.getMessage());
//			} catch (FacebookError e) {
//				Log.w(TAG, "Facebook Error: " + e.getMessage());
//			}
//
//
//		}
//	}

	public class PageRequestListener extends BaseRequestListener {
		public void onComplete(String response, final Object state) {

			Log.e(TAG,"euro2012 graph/feed complete");
			Log.i(TAG,"response: "+response);
			try {

				// process the response here: executed in background thread
				JSONObject json = Util.parseJson(response);
				//JSONArray array = new JSONArray(response);
				JSONArray array = json.getJSONArray("data");
				for (int i = 0; i < array.length(); i++) {
					JSONObject row = array.getJSONObject(i);
					//Log.i(TAG,"row : "+row);              
					String id = row.getString("id");


					String name = null;
					String message = null;
					String created_time = null;
					String pictureUrl = null;
					int commentCount = 0;
					int likeCount = 0;

					if(row.has("name")){
						name = row.getString("name");
					}

					if(row.has("message")){
						message = row.getString("message");
					}

					if(row.has("created_time")){
						created_time = row.getString("created_time");
					}

					if (row.has("picture")){
						pictureUrl = row.getString("picture"); 
						Log.err("pictureUrl ================================> " + pictureUrl);
					}

					if (row.has("comments")){
						JSONObject commentsJson = row.getJSONObject("comments");
						commentCount = commentsJson.getInt("count");
						//							JSONArray commentsArray = json.getJSONArray("data");
						//							for (int j = 0; j < commentsArray.length(); j++) {
						//								JSONObject commentRow = commentsArray.getJSONObject(j);
						//								commentCount = commentRow.getInt("count");
						//							}
					}

					if (row.has("likes")){
						JSONObject likesJson = row.getJSONObject("likes");
						likeCount = likesJson.getInt("count");
						//							JSONArray likesArray = json.getJSONArray("data");
						//							for (int j = 0; j < likesArray.length(); j++) {
						//								JSONObject likeRow = likesArray.getJSONObject(j);
						//								likeCount = likeRow.getInt("count");
						//							}
					}

					if (!TextUtils.isEmpty(message) && !TextUtils.isEmpty(name)){
						WallPost wallPost = mFacebookEuro2012User.new WallPost(id, name, message, stringToDateHelper(created_time), pictureUrl, likeCount, commentCount);
						mFacebookEuro2012User.addWallPost(id, wallPost);                 
					}
				}

				notifySucribers(Event.FACEBOOK_EURO2012_WALL_POST_RECEIVED);

			} catch (JSONException e) {
				Log.w(TAG, "JSON Error in response");
				Log.w(TAG,"cause: "+e.getCause());
				Log.w(TAG,"message: "+e.getMessage());
				//showError(getString(R.string.general_error));
			} catch (FacebookError e) {
				Log.w(TAG, "Facebook Error: " + e.getMessage());
				//showError(getString(R.string.general_error));
			}		

		}
	}


	public interface FacebookGraphAPIRequestListener{
	}

	public interface FacebookSessionEventListener extends FacebookGraphAPIRequestListener{
		public void OnFacebookAuthFailed();
		public void OnFacebookUserInfoReceived();
		public void OnFacebookLoggingOutBegin();
		public void OnFacebookLoggingOutEnd();
		public void OnFacebookFriendsCheckinReceived();
	}

	public interface FacebookEventListener extends FacebookGraphAPIRequestListener{
		public void OnFacebookUniverscineWallPostReceived();
		public void OnFacebookPostSent();
		public void OnFacebookLikeDone();
		public void OnFacebookPhotoUploaded();
	}

	/**
	 * Register an news listener with this manager
	 * @param listener - the News to register
	 */
	public void addListener(FacebookGraphAPIRequestListener listener){
		mGraphAPIListeners.add(listener);
	}

	/**
	 * Unregister an News listener
	 * @param listener - the listener to unregister
	 */
	public void removeListener(FacebookGraphAPIRequestListener listener){
		mGraphAPIListeners.remove(listener);
	}

	public void notifySucribers(Event event, Object... optionalParams){
		Log.out("Notifying all GraphAPI listener");

		for(FacebookGraphAPIRequestListener subscriber : mGraphAPIListeners){
			FacebookGraphAPIRequestListener listener;

			switch (event) {

			case FACEBOOK_USER_INFO_RECEIVED:
				listener = (FacebookGraphAPIRequestListener) subscriber;
				if (listener instanceof FacebookSessionEventListener){
					((FacebookSessionEventListener) listener).OnFacebookUserInfoReceived();
				}
				break;

			case FACEBOOK_FRIENDS_CHECKINS_RECEIVED:
				listener = (FacebookGraphAPIRequestListener) subscriber;
				if (listener instanceof FacebookSessionEventListener){
					((FacebookSessionEventListener) listener).OnFacebookFriendsCheckinReceived();
				}
				break;

			case FACEBOOK_AUTH_FAILED:
				listener = (FacebookGraphAPIRequestListener) subscriber;
				if (listener instanceof FacebookSessionEventListener){
					((FacebookSessionEventListener) listener).OnFacebookAuthFailed();
				}
				break;

			case FACEBOOK_LOGGING_OUT_BEGIN:
				listener = (FacebookGraphAPIRequestListener) subscriber;
				if (listener instanceof FacebookSessionEventListener){
					((FacebookSessionEventListener) listener).OnFacebookLoggingOutBegin();
				}
				break;

			case FACEBOOK_LOGGING_OUT_END:
				listener = (FacebookGraphAPIRequestListener) subscriber;
				if (listener instanceof FacebookSessionEventListener){
					((FacebookSessionEventListener) listener).OnFacebookLoggingOutEnd();
				}
				break;

			case FACEBOOK_EURO2012_WALL_POST_RECEIVED:
				listener = (FacebookGraphAPIRequestListener) subscriber;
				if (listener instanceof FacebookEventListener){
					((FacebookEventListener) listener).OnFacebookUniverscineWallPostReceived();
				}
				break;

			case FACEBOOK_POST_SENT:
				listener = (FacebookGraphAPIRequestListener) subscriber;
				if (listener instanceof FacebookEventListener){
					((FacebookEventListener) listener).OnFacebookPostSent();
				}
				break;

			case FACEBOOK_LIKE_DONE:
				listener = (FacebookGraphAPIRequestListener) subscriber;
				if (listener instanceof FacebookEventListener){
					((FacebookEventListener) listener).OnFacebookLikeDone();
				}
				break;

			case FACEBOOK_PHOTO_UPLOADED:
				listener = (FacebookGraphAPIRequestListener) subscriber;
				if (listener instanceof FacebookEventListener){
					((FacebookEventListener) listener).OnFacebookPhotoUploaded();
				}
				break;



			default:
				break;
			}
		}
	}


	public Date stringToDateHelper(String dateString){
		//facebook date are 2011-03-03T00:02:15+0000 
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+SSSS"); 
		Date date = null;
		try {
			date = df.parse(dateString);
			//			String newDateString = df.format(startDate);
			//			System.out.println(newDateString);
		} catch (ParseException e) {
			Log.printStackTrace(e);
		}

		//		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("India"),Locale.getDefault());
		//
		//		calendar.set(Calendar.DATE,date.getDate());
		//		calendar.set(Calendar.MONTH,date.getMonth());
		//		calendar.set(Calendar.YEAR, date.getYear()+1900);
		//		calendar.set(Calendar.HOUR,date.getHours());
		//		calendar.set(Calendar.MINUTE,date.getMinutes());
		//		calendar.set(Calendar.SECOND,date.getSeconds());

		//		final long current = System.currentTimeMillis();
		//		final long update = calendar.getTimeInMillis();
		//		final long timeago = Math.abs(current-update);


		Calendar calendar = Calendar.getInstance();
		TimeZone timezone = TimeZone.getDefault();
		TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");

		int currentGMTOffset = timezone.getOffset(calendar.getTimeInMillis());
		int gmtOffset = utcTimeZone.getOffset(calendar.getTimeInMillis());

		calendar.setTimeInMillis(calendar.getTimeInMillis() + (gmtOffset - currentGMTOffset));

		final long current = calendar.getTimeInMillis();
		final long update = date.getTime();
		final long timeago = Math.abs(current-update);

		return date;

	}

}
