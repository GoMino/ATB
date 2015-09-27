package com.android.toolbox.thread;

import com.android.toolbox.Log;

import android.annotation.SuppressLint;
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

	@SuppressLint("NewApi")
	public static <P, T extends AsyncTask<P, ?, ?>> void execute(T task, P... params) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		} else {
			task.execute(params);
		}
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		Log.w(getClass().getSimpleName(),"");
	}

	@Override
	protected void onPostExecute(Result result) {
		super.onPostExecute(result);
		Log.w(getClass().getSimpleName(),"");
	}

	@Override
	protected Result doInBackground(Params... paramses) {
		Log.w(getClass().getSimpleName(),"");
		return null;
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		Log.w(getClass().getSimpleName(),"");
	}

	@Override
	protected void onCancelled(Result result) {
		super.onCancelled(result);
		Log.w(getClass().getSimpleName(),"");
	}
	
	
}
