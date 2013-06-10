package com.android.toolbox.thread;

import com.android.toolbox.Log;

import android.os.AsyncTask;
import android.os.Build;

public abstract class ThreadPoolAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
	/*
	 * This is a helper method to allow ICS AsyncTask to run in a thread pool, 
	 * without break API compatability with pre-ICS devices.
	 * If you don't want a threadpool use the default AsyncTask since that is the 
	 * default for ICS.  If you don't want a threadpool for pre-ICS (API level < 13) 
	 * then you need to wrote your own AsyncTask. Use the AsyncTask.java as a good starting point.
	 * more info here https://groups.google.com/forum/?fromgroups#!topic/android-developers/8M0RTFfO7-M
	 */
	public void executeOnThreadPool(Params...params) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			Log.e("ThreadPoolAsyncTask", "executeOnExecutor");
			this.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		} else {
			Log.e("ThreadPoolAsyncTask", "execute");
			this.execute(params);
		}
	}
	
	
}
