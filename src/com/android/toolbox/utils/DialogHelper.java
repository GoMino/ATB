package com.android.toolbox.utils;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import com.android.toolbox.Log;
import com.android.toolbox.R;
import com.android.toolbox.fragments.ProgressDialogFragment;

public class DialogHelper {
	protected FragmentManager		mFragmentManager;
	private Context 				mContext;
	private String 					mDefaultLoadingString;
	public DialogHelper(Context context, FragmentManager fm, String defaultLoadingString){
		mFragmentManager = fm;
		mDefaultLoadingString = defaultLoadingString;
	}
	
	public void popupProgressDialog(){
		popupProgressDialog(mDefaultLoadingString);
	}
	
	public void popupProgressDialog(String message){
		Log.d("popup progress dialog");
		if (mFragmentManager.findFragmentByTag(ProgressDialogFragment.class.getSimpleName()) != null){
			return;
		}
		ProgressDialogFragment progressFragment = ProgressDialogFragment.newInstance(message);
		progressFragment.setCancelable(false);
		progressFragment.show(mFragmentManager, ProgressDialogFragment.class.getSimpleName());
	}
	
	public void dismissDialogIfPossiblebyTag(String tag){
		Log.d("dismiss dialog if possible, by tag : " + tag);
		try{
			mFragmentManager.executePendingTransactions();
			if (mFragmentManager.findFragmentByTag(tag) != null)
				((DialogFragment) mFragmentManager.findFragmentByTag(tag)).dismissAllowingStateLoss();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public void dismissProgressDialog(){
		dismissDialogIfPossiblebyTag(ProgressDialogFragment.class.getSimpleName());
	}
}
