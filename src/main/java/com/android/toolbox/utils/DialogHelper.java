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
		popupProgressDialog(mDefaultLoadingString, false);
	}

	public void popupProgressDialog(String message){
		popupProgressDialog(message, false);
	}

	public void popupProgressDialog(String message, boolean cancelable){
		popupProgressDialog(R.style.CustomDialogTheme_Base_Transparent, message, cancelable);
	}

	public void popupProgressDialog(int theme, String message, boolean cancelable){
		Log.d("popup progress dialog");
		if (mFragmentManager.findFragmentByTag(ProgressDialogFragment.class.getSimpleName()) != null){
			return;
		}
		try{
			ProgressDialogFragment progressFragment = ProgressDialogFragment.newInstance(message);
			progressFragment.setCancelable(cancelable);
			progressFragment.setStyle(DialogFragment.STYLE_NO_FRAME, theme);
			progressFragment.show(mFragmentManager, ProgressDialogFragment.class.getSimpleName());

			mFragmentManager.executePendingTransactions();
		}catch(Exception e){
			e.printStackTrace();
		}

	}

	public void dismissDialogIfPossiblebyTag(String tag){
		Log.d("dismiss dialog if possible, by tag : " + tag);
		try{
			mFragmentManager.executePendingTransactions();
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
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
