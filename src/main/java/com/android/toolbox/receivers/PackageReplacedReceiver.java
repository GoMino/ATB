package com.android.toolbox.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.android.toolbox.Log;

/**
 * @author gomino (amine.bezzarga@labgency.com)
 */
public class PackageReplacedReceiver extends BroadcastReceiver {
	
	public final static String TAG = PackageReplacedReceiver.class.getSimpleName();
	private SharedPreferences mPrefs;
	
    /**
     * @see android.content.BroadcastReceiver#onReceive(Context,Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
            int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
            
            Log.d(TAG, "Package is Replaced: " + intent.getScheme());
            Log.d(TAG, "Package is Replaced: " + intent.getData().getSchemeSpecificPart());
            Log.d(TAG, "Package is Replaced: " + context.getPackageManager().getNameForUid(intent.getIntExtra(Intent.EXTRA_UID, -1)));
            
//            Log.d(TAG, "Force recreation of db");
            
//            Log.d(TAG, "Forcing activation of automatic download service");
//            mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            
//            int maxDownloadLimit = mPrefs.getInt(UVODManager.MAX_AUTOMATIC_DOWNLOAD_LIMIT_PREF_KEY, 3);
//            Editor edit = mPrefs.edit();
//    		edit.putBoolean(UVODManager.WANT_OFFEREDVIDEO_PREF_KEY, true);
//    		edit.putInt(UVODManager.MAX_AUTOMATIC_DOWNLOAD_LIMIT_PREF_KEY, maxDownloadLimit);
//    		edit.commit();
    }
}