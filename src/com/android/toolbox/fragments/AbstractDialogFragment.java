/**
 * 
 */
package com.android.toolbox.fragments;

import java.util.HashMap;
import java.util.Random;

import com.android.toolbox.Log;
import com.android.toolbox.listeners.DismissDialogListener;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;



public abstract class AbstractDialogFragment extends DialogFragment implements OnClickListener{

	private static final String TAG 							= AbstractDialogFragment.class.getSimpleName();
	
	private static final String KEY_TITLE 						= "title";
	private static final String KEY_MESSAGE						= "message";
	private static final String KEY_OK							= "ok";
	private static final String KEY_CANCEL						= "cancel";
	private static final String KEY_LISTENERS 					= "listeners";
		
	protected View 				mView;
	protected Button 			mOkButton;
	protected Button 			mCancelButton;
	protected TextView 			mTitleView;
	protected TextView 			mDescriptionView;
	
	private String 				mTitle 							= null;
	private String 				mDescription 					= null;
	private String 				mOkText 						= null;
	private String 				mCancelText 					= null;
	
	protected AbstractDialogFragment.OnClickListener mOkListener = null;
	protected AbstractDialogFragment.OnClickListener mCancelListener = null;
	protected DismissDialogListener mDismissListener;
	
	private static HashMap<Integer, Object[]> mSavedListeners = new HashMap<Integer, Object[]>();

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.e(TAG, "onCreate savedInstanceState:" + savedInstanceState);
		super.onCreate(savedInstanceState);
		restoreState(savedInstanceState);

	}

//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		mView = inflater.inflate(R.layout.dialog_fragment_alert, container, false);
//		
//		mTitleView = (TextView) mView.findViewById(R.id.txt_title);
//		mDescriptionView = (TextView) mView.findViewById(R.id.txt_description);
//		mOkButton = (Button) mView.findViewById(R.id.btn_ok);
//		mCancelButton = (Button) mView.findViewById(R.id.btn_cancel);
//		
//		setData();
//		
//		return mView;
//	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title){
		mTitle = title;
		setData();
	}
	
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description){
		mDescription = description;
		setData();
	}
	
	/**
	 * @param okText the text to set
	 * @param okListener the listener to set on the button
	 */
	public void setOkButton(String okText, AbstractDialogFragment.OnClickListener okListener){
		mOkText = okText;
		setOKClickListener(okListener);
		setData();
	}

	public void setOKClickListener(AbstractDialogFragment.OnClickListener okListener){
		mOkListener = okListener;
	}
	
	public AbstractDialogFragment.OnClickListener getOKClickListener(){
		return mOkListener;
	}
	
	/**
	 * @param cancelText the text to set
	 * @param cancelListener the listener to set on the button
	 */
	public void setCancelButton(String cancelText, AbstractDialogFragment.OnClickListener cancelListener){
		mCancelText = cancelText;
		setCancelClickListener(cancelListener);
		setData();
	}
	
	public void setCancelClickListener(AbstractDialogFragment.OnClickListener cancelListener){
		mCancelListener = cancelListener;
	}

	
	public AbstractDialogFragment.OnClickListener getCancelClickListener(){
		return mCancelListener;
	}

	/**
	 * Set the datas on the views
	 */
	public void setData(){
		if (mView == null){
			return;
		}
		if (mTitle != null && mTitleView != null){
			mTitleView.setText(mTitle);
			mTitleView.setVisibility(View.VISIBLE);
		}
		if (mDescription != null && mDescriptionView != null){
			mDescriptionView.setText(Html.fromHtml(mDescription));
			mDescriptionView.setMovementMethod(LinkMovementMethod.getInstance());
//			mDescriptionView.setLinkTextColor(Color.DKGRAY);
			mDescriptionView.setVisibility(View.VISIBLE);
		}
		
		if (mOkButton != null){
			
			if (!TextUtils.isEmpty(mOkText)){
				mOkButton.setVisibility(View.VISIBLE);
				mOkButton.setText(mOkText);
			}else{
				mOkButton.setVisibility(View.GONE);
			}
			
			mOkButton.setOnClickListener(this);
		}
		
		if (mCancelButton != null){
			
			if (!TextUtils.isEmpty(mCancelText)){
				mCancelButton.setVisibility(View.VISIBLE);
				mCancelButton.setText(mCancelText);
			}else{
				mCancelButton.setVisibility(View.GONE);
			}
			
			mCancelButton.setOnClickListener(this);
		}
		
		
	}	
	
	private void restoreState(Bundle state){
		if (state == null){
			return;
		}
		mTitle = state.getString(KEY_TITLE);
		mDescription = state.getString(KEY_MESSAGE);
		mOkText = state.getString(KEY_OK);
		mCancelText = state.getString(KEY_CANCEL);
		int listeners = state.getInt(KEY_LISTENERS, 0);
		if (listeners != 0){
			Object[] objects = mSavedListeners.remove(listeners);
			if (objects != null){
				mOkListener = (OnClickListener) objects[0];
				mCancelListener = (OnClickListener) objects[1];
			}
		}
		Log.e(TAG, "restoreState mOkListener " + mOkListener + " mCancelListener" + mCancelListener);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(KEY_TITLE, mTitle);
		outState.putString(KEY_MESSAGE, mDescription);
		outState.putString(KEY_OK, mOkText);
		outState.putString(KEY_CANCEL, mCancelText);
		Random random = new Random(System.currentTimeMillis());
		int key = random.nextInt();
		outState.putInt(KEY_LISTENERS, key);
		mSavedListeners.put(key, new Object[]{mOkListener, mCancelListener});
		Log.e(TAG, "onSaveInstanceState " + mSavedListeners);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onClick(View v) {
		if(v.getId()== mOkButton.getId()){
			if(mOkListener!=null) mOkListener.onClick(this, v.getId());
			dismiss();
		}else if (v.getId()== mCancelButton.getId()){
			if(mCancelListener!=null) mCancelListener.onClick(this, v.getId());
//			dismiss();
			getDialog().cancel();
		}
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		if(mDismissListener!=null) mDismissListener.onDialogCancelled();
		super.onCancel(dialog);
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		Log.e(TAG,"onDismiss activity:"+getActivity() +  " view:" +getView());
		if(getActivity()!=null && getView()!=null){
			getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		}
		if(mDismissListener!=null) mDismissListener.onDialogDismiss();
		super.onDismiss(dialog);
	}
	
	public void setDismissListener(DismissDialogListener listener){
		mDismissListener = listener;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
//		Log.e(TAG,"onDestroyView activity:"+getActivity() +  " view:" +getView());
//		if(getActivity()!=null && getView()!=null){
//			((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getView().getWindowToken(), WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//		}
		
		//Show soft-keyboard:
//		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		//hide keyboard :
//		 getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	public interface OnClickListener{
		public void onClick(DialogFragment fragment, int id);
	}
}