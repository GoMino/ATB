package com.android.toolbox;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import com.android.toolbox.Log.LogInterface;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.Toast;

public class ToolBox {
	
	private final static String TAG 					= ToolBox.class.getSimpleName();
	public static String SERIALIZED_DATA_PATH 	= Environment.getExternalStorageDirectory().getAbsolutePath() + "";
	public static enum IMAGETYPE{BACKEND, WEB};
	private ImageManagerInterface mImageManager;
	private static ToolBox sInstance;
	
	public static ToolBox getInstance() {
		if (sInstance == null)
			sInstance = new ToolBox();
		return sInstance;
	}
	
	public ImageManagerInterface getImageManager(){
		return mImageManager;
	}

	public void setImageManager(ImageManagerInterface instance, Context context, String appName) {
		mImageManager = instance;
		if(mImageManager!=null){
			mImageManager.initialize(context, appName);
		}
	}

	public interface ImageManagerInterface{
		public void initialize(Context context, String appName);
		public void pickupDefaultImage(int resourceId, ImageView view);
		public void downloadImage(String URL, final ImageView imageView, boolean thumbnailed, boolean animated, IMAGETYPE... type);
	}
	
	public static void pickupDefaultImage(int resourceId, ImageView view){
		if(getInstance().getImageManager()!=null){
			getInstance().getImageManager().pickupDefaultImage(resourceId, view);
		}
	}
	
	public static void downloadImage(String URL, ImageView imageView, IMAGETYPE... type){
		if(getInstance().getImageManager()!=null){
			getInstance().getImageManager().downloadImage(URL, imageView, false, true, type);
		}
	}
	
	public static void downloadImageWithoutFade(String URL, ImageView imageView, IMAGETYPE... type){
		if(getInstance().getImageManager()!=null){
			getInstance().getImageManager().downloadImage(URL, imageView, false, false, type);
		}
	}
	
	public static void downloadImage(String URL, final ImageView imageView, boolean thumbnailed, boolean animated, IMAGETYPE... type){
		if(getInstance().getImageManager()!=null){
			getInstance().getImageManager().downloadImage(URL, imageView, thumbnailed, animated, type);
		}
	}

	public static boolean isPhone(Context context){
//		if(is7inchTablet(context)){
//			return false;
//		}
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) != Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}
	
	public static boolean is7inchTablet(Context context){
		
		 Configuration config = context.getResources().getConfiguration();
		//Indepedant from device density
		//480 5" phone
		//600 7" tablet
		//720 10" tablet
		int screenWidth = getScreenWidth(context);
		Log.v(TAG, "[is7inchTablet] screenwidth:"+screenWidth + ((Build.VERSION.SDK_INT>=13)?" smallestScreenWidthDp:"+config.smallestScreenWidthDp:""));
	     if (Build.VERSION.SDK_INT>=13 && config.smallestScreenWidthDp >= 600) {
	    	 int naturalOrientation = getDefaultOrientation(context);
				if(naturalOrientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT || naturalOrientation==ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT){
					return true;
				}
	     } 
//		else if(Build.VERSION.SDK_INT<13){
////			int screenWidth = UniversCine.getScreenWidth(context);
////			Log.e(TAG, "[is7inchTablet] screenwidth:"+screenWidth);
//			//736 = 7" tablet
////			//720 = 5" phone
//			if(screenWidth>=736){
//				int naturalOrientation = getDefaultOrientation(context);
//				if(naturalOrientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT || naturalOrientation==ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT){
//					return true;
//				}
//			}
//	     }
		
		
		return false;
	}
	
	public static int getDefaultOrientation(Context context){
		int naturalOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

	    Rect rec = getScreenDimension(context);
	    int width = rec.width();
	    int height = rec.height();

	    Log.v(TAG, "[getDefaultOrientation] width:"+width + " heith:"+height);
	    if(width > height){
	        Log.v(TAG, "[getDefaultOrientation] Natural Orientation is landscape");
	        if (Build.VERSION.SDK_INT > 9){
	        	naturalOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
			}else{
				naturalOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
			}	
	    } else {
	        Log.v(TAG, "[getDefaultOrientation] Natural Orientation is portrait");
	        if (Build.VERSION.SDK_INT > 9){
	        	naturalOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
	        }else{
				naturalOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			}
	    } 
	    return naturalOrientation;
	}

	
	public static void setDefaultOrientation(Activity activity){
	    //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
		int newOrientation = getDefaultOrientation(activity);
		if(newOrientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT || newOrientation==ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT){
			if (Build.VERSION.SDK_INT > 9){
				if(is7inchTablet(activity)){
					//we want 7inch tablet to display in landscape mode
					newOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
				}else{
					newOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
				}
			}else{
				newOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			}
		}
		
	    activity.setRequestedOrientation(newOrientation);
	}
	
	public static int getScreenConfiguration(Context context){
		return context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
	}
	
	public static int getScreenDensity(Context context){
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager w = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		w.getDefaultDisplay().getMetrics(metrics);
		int density = metrics.densityDpi;
		return density;
	}
	
	public static Rect getScreenDimension(Context context){
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	    Display display = wm.getDefaultDisplay();

	    int rotation = display.getOrientation();
	    int width = 0;
	    int height = 0;
	    switch (rotation) {
	    case Surface.ROTATION_0:
	    case Surface.ROTATION_180:
	        Log.v(TAG, "Rotation is: 0 or 180");
	        width = display.getWidth();
	        height = display.getHeight();
	        break;
	    case Surface.ROTATION_90:       
	    case Surface.ROTATION_270:
	        Log.v(TAG, "Rotation is: 90 or 270");
	        width = display.getHeight();
	        height = display.getWidth();
	        break;
	    default:
	        break;
	    }
	    Rect rec = new Rect(0, 0, width, height);
	    return rec;
	}
	
	/**
	 * 
	 * @param context
	 * @return the width of the device (orientation independent)
	 */
	public static int getScreenWidth(Context context){

		Rect rec = getScreenDimension(context);
		return rec.width();

	}
	
	/**
	 * 
	 * @param context
	 * @return the width of the window (orientation dependent)
	 */
	public static int getWindowWidth(Context context){
		int Measuredwidth = 0;
		int Measuredheight = 0;
		Point size = new Point();
		WindowManager w = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//		WindowManager w = getWindowManager();

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2){
			w.getDefaultDisplay().getSize(size);

			Measuredwidth = size.x;
			Measuredheight = size.y; 
		}else{
			DisplayMetrics metrics = new DisplayMetrics();
			w.getDefaultDisplay().getMetrics(metrics);

			Measuredheight = metrics.heightPixels;
			Measuredwidth = metrics.widthPixels;

			//Deprecated
			//Display d = w.getDefaultDisplay(); 
			//Measuredwidth = d.getWidth(); 
			//Measuredheight = d.getHeight(); 
			//		}
		}
		return Measuredwidth;
	}
	
	public static Drawable getDrawableFromView(Context context, View view){
		
		// get our bitmap from the cache
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();  
		Bitmap bitmapView= view.getDrawingCache();
		Log.v(TAG, "[getDrawableFromView] logo width:" + bitmapView.getWidth() + " logo height:" + bitmapView.getHeight());
		
		// for scaling
//		Matrix scale = new Matrix();
//		float scaleFactor = 1.0f;
//		scale.setScale(scaleFactor, scaleFactor);

		// our final drag bitmap
//		Bitmap scaledBitmap = Bitmap.createBitmap(bitmapView, 0, 0, view.getMeasuredWidth(), view.getMeasuredHeight(), scale, true);
		
		Drawable result = new BitmapDrawable(context.getResources(),bitmapView);
		Log.v(TAG, "[getDrawableFromView] logo  drawable width:" + result.getIntrinsicWidth() + " logo height:" + result.getIntrinsicHeight());
		result.setBounds(0, 0, result.getIntrinsicWidth(), result.getIntrinsicHeight());
		
		
		//Clean up
//		bitmapView.recycle();
//		view.destroyDrawingCache();
//		view.setDrawingCacheEnabled(false);
		return result;
	}
	
	public static String capitalizeFirstLetter(String word){
		final StringBuilder result = new StringBuilder(word.length());
		word = word.trim();
		String[] words = word.split("\\s");
		for(int i=0,l=words.length;i<l;++i) {
			Log.e(TAG, "[capitalizeFirstLetter] " + words[i]);
			if(i>0) result.append(" ");      
			result.append(Character.toUpperCase(words[i].charAt(0)))
		        .append(words[i].substring(1));

		}
		return result.toString();
	}
	
	public static void showDeviceScreenInfo(Context context){
		//Determine screen size
		if (getScreenConfiguration(context) == Configuration.SCREENLAYOUT_SIZE_LARGE) {     
			Toast.makeText(context, "Large screen",Toast.LENGTH_LONG).show();

		}
		else if (getScreenConfiguration(context) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {     
			Toast.makeText(context, "Normal sized screen" , Toast.LENGTH_LONG).show();

		} 
		else if (getScreenConfiguration(context) == Configuration.SCREENLAYOUT_SIZE_SMALL) {     
			Toast.makeText(context, "Small sized screen" , Toast.LENGTH_LONG).show();
		}
		else {
			Toast.makeText(context, "Screen size is neither large, normal or small" , Toast.LENGTH_LONG).show();
		}

		//Determine density
		int density = getScreenDensity(context);

		if (density==DisplayMetrics.DENSITY_HIGH) {
			Toast.makeText(context, "DENSITY_HIGH... Density is " + String.valueOf(density),  Toast.LENGTH_LONG).show();
		}
		else if (density==DisplayMetrics.DENSITY_MEDIUM) {
			Toast.makeText(context, "DENSITY_MEDIUM... Density is " + String.valueOf(density),  Toast.LENGTH_LONG).show();
		}
		else if (density==DisplayMetrics.DENSITY_LOW) {
			Toast.makeText(context, "DENSITY_LOW... Density is " + String.valueOf(density),  Toast.LENGTH_LONG).show();
		}
		else {
			Toast.makeText(context, "Density is neither HIGH, MEDIUM OR LOW.  Density is " + String.valueOf(density),  Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
	 * @param bytes
	 * @param si is in International Sytem of Units or Binary unit
	 * @return a Human readable string of size
	 */
	
	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	public static int getResourceIdentifier(Context context, String resourceName, String resourceType, String packageName){
		int resourceId = 0;
		if(resourceName!=null){
			resourceId = context.getResources().getIdentifier(resourceName, resourceType, packageName);
		}
		return resourceId;
	}
	
	public static int getColorRRGGBB(Context context, int colorResourceId){
		return (0xFFFFFF & context.getResources().getColor(colorResourceId));
	}
	
	/*
	 * start animation (any view)
	 */
	public static void startAnimation(final View v, final int resId, AnimationListener l){
		Animation anim = AnimationUtils.loadAnimation(v.getContext(),resId);
		startAnimation(v, anim, l);
	}
	
	public static void startAnimation(final View v, Animation anim, AnimationListener l){

		if(v!=null){    // can be null, after change of orientation
//			anim.setFillAfter(true);
//			anim.setFillEnabled(true);
//			anim.setInterpolator();
			v.setAnimation(anim);
			anim.setAnimationListener(l);
			v.startAnimation(anim);                 
		}
	}
	
	public static String MD5(String md5) {
		   try {
		        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
		        byte[] array = md.digest(md5.getBytes());
		        StringBuffer sb = new StringBuffer();
		        for (int i = 0; i < array.length; ++i) {
		          sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
		       }
		        return sb.toString();
		    } catch (java.security.NoSuchAlgorithmException e) {
		    }
		    return null;
		}
	
	public static void serializeToFile(Serializable object){
		Log.e(TAG, "serializeToFile | serializing " + object.getClass().getSimpleName());
		String basePath = SERIALIZED_DATA_PATH;
		String outFileName = java.io.File.separator  + MD5(object.getClass().getSimpleName()) + ".ser";
		FileOutputStream fos = null;
		ObjectOutput out = null;

		String fullpath = basePath + outFileName;
		if (!new File(fullpath).exists()){
			new File(fullpath).getParentFile().mkdirs();
			try {
				new File(fullpath).createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		File dir = new File(basePath);
//		if (!dir.exists())
//			dir.mkdir();

//		File file = new File(dir, outFileName);
		try {
//			fos = new FileOutputStream(file);
			fos = new FileOutputStream(fullpath);
			out = new ObjectOutputStream(fos);
			out.writeObject(object);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.e(TAG, "serializeToFile | serialization failed " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "serializeToFile | serialization failed " + e.getMessage());
		}finally{
			if (out!=null) closeQuietly((Closeable)out);
			if (fos!=null) closeQuietly((Closeable)fos);
		}	
		
	}
		 
	public static Object deSerializeFromFile(String className) {
		Log.e(TAG, "deSerializeFromFile | deserializing " + className);
		String serializedFileName = SERIALIZED_DATA_PATH + java.io.File.separator  + MD5(className) + ".ser";
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		Object deserializedObject = null;
		try {
			fis = new FileInputStream(serializedFileName);
			ois = new ObjectInputStream(fis);
			deserializedObject = ois.readObject();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "deSerializeFromFile | deserialization failed " + e.getMessage());
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "deSerializeFromFile | deserialization failed " + e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "deSerializeFromFile | deserialization failed " + e.getMessage());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "deSerializeFromFile | deserialization failed " + e.getMessage());
		}finally{
			if (ois!=null) closeQuietly((Closeable)ois);
			if (fis!=null) closeQuietly((Closeable)fis);
		}
		
		return deserializedObject;
	}
	
	public static void closeQuietly (Closeable closeable) {
	    try {
	      closeable.close();
	    } catch (IOException logAndContinue) {
	      //...
	    }
	}
	
	public boolean deleteSerializedFile(Object object){
		String SER_FILE_PATH = SERIALIZED_DATA_PATH + java.io.File.separator + MD5(object.getClass().getSimpleName()) + ".ser";
		java.io.File file = new java.io.File(SER_FILE_PATH);
		if (file.exists()){
			file.delete();
			return true;
		}
		return false;
	}
	
	public static boolean isSerialized(String className){
		String SER_FILE_PATH = SERIALIZED_DATA_PATH + java.io.File.separator +  MD5(className) + ".ser";
		java.io.File file = new java.io.File(SER_FILE_PATH);
		if (file.exists()){
			Log.e(TAG, "isSerialized " + SER_FILE_PATH + " => yes");
			return true;
		}
		Log.e(TAG, "isSerialized " + SER_FILE_PATH + " => no");
		return false;
	}
	
}
