package com.android.toolbox.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

import com.android.toolbox.Log;

/**
 * Broadcast receiver for connectivity events.
 * @author Benoit Vannesson (benoit.vannesson@labgency.com)
 * @author guillaume.hubert@labgency.com
 */

public class ConnectivityReceiver extends BroadcastReceiver{
	
	public static final String	TAG	= ConnectivityReceiver.class.getSimpleName();

	public static interface OnNetworkAvailableListener {
		public void onNetworkAvailabilityChanged(boolean isNetworkAvailable);
	}
	
	private OnNetworkAvailableListener onNetworkAvailableListener;
	private boolean mConnection = false;
	private final ConnectivityManager mConnectivityManager;
	private Context mContext;
	
	public ConnectivityReceiver(Context context){
		mContext = context;
		mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		checkConnectionOnDemand();
	}

	
	@Override
	public void onReceive(Context mContext, Intent intent) {
		Log.d(ConnectivityReceiver.class.getSimpleName(), "action: " + intent.getAction());	
		checkConnectionOnDemand(intent);
	}
	
	public void bind() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mContext.registerReceiver(this, filter);
		checkConnectionOnDemand();
	}

	public void unbind() {
		mContext.unregisterReceiver(this);
	}
	
	private void checkConnectionOnDemand() {
		final NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
		if (info == null || info.getState() != State.CONNECTED) {
			if (mConnection == true) {
				mConnection = false;
				if (onNetworkAvailableListener != null) onNetworkAvailableListener.onNetworkAvailabilityChanged(false);
			}
		}
		else {
			if (mConnection == false) {
				mConnection = true;
				if (onNetworkAvailableListener != null) onNetworkAvailableListener.onNetworkAvailabilityChanged(true);
			}
		}
	}
	
	private void checkConnectionOnDemand(Intent intent) {
		if (mConnection == true && intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
			mConnection = false;
			if (onNetworkAvailableListener != null) {
				onNetworkAvailableListener.onNetworkAvailabilityChanged(false);
			}
		}
		else if (mConnection == false && !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
			mConnection = true;
			if (onNetworkAvailableListener != null) {
				onNetworkAvailableListener.onNetworkAvailabilityChanged(true);
			}
		}
	}
	
	public boolean hasConnection() {
		return mConnection;
	}

	public void setOnNetworkAvailableListener(OnNetworkAvailableListener listener) {
		this.onNetworkAvailableListener = listener;
	}

}



