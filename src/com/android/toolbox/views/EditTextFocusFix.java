package com.android.toolbox.views;


import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.android.toolbox.Log;
import com.android.toolbox.ToolBox;


/**
 * @author gomino (amine.bezzarga@labgency.com)
 */
public class EditTextFocusFix extends AutoCompleteTextView implements OnFocusChangeListener, OnClickListener {

	private String TAG = EditTextFocusFix.class.getSimpleName();
	private boolean isFocusable = false;
	private boolean isPhone = false;

	public EditTextFocusFix(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public EditTextFocusFix(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public EditTextFocusFix(Context context) {
		super(context);
		init();
	}

	void init(){
		TAG = getClass().getSimpleName()+ " " + getId();
		setOnFocusChangeListener(this);
		setOnClickListener(this);
		setEditTextFocus(false);
		isPhone = ToolBox.isPhone(getContext()) && !ToolBox.is7inchTablet(getContext());
	};

	public void setEditTextFocus(boolean isFocused)
	{
		isFocusable = isFocused;
		setCursorVisible(isFocused);
		setFocusable(isFocused);
		setFocusableInTouchMode(isFocused);

		if (isFocused)
		{
			requestFocus();
		}
	}
	

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		
		setEditTextFocus(false);
	}

//	@Override
//	protected void onConfigurationChanged(Configuration newConfig) {
//		super.onConfigurationChanged(newConfig);
//
//
//		// Checks whether a hardware keyboard is available
//		if (newConfig.hardKeyboardHidden == Configuration.KEYBOARDHIDDEN_NO) {
//			Log.e(TAG, "keyboard visible");
//			setEditTextFocus(true);
//		} else if (newConfig.hardKeyboardHidden == Configuration.KEYBOARDHIDDEN_YES) {
//			Log.e(TAG, "keyboard hidden");
//			setEditTextFocus(false);
//		}
//	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		Log.v(TAG, "onFocusChange : " + hasFocus + " isFocusable : " + isFocusable);
		if(isFocusable){

				if (hasFocus)
				{
					//open keyboard
					((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(this, InputMethodManager.SHOW_FORCED);
					setCursorVisible(true);
					setFocusable(true);
					setFocusableInTouchMode(true);
				}
				else
				{ //close keyboard
					((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getWindowToken(), WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
					setEditTextFocus(false);
				}

		}

	}

	@Override
	public void onClick(View v) {
		Log.v(TAG, "[onClick]");
		setEditTextFocus(true);
	}

	
	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		if(isPhone){
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				// User has pressed Back key. So hide the keyboard
				InputMethodManager mgr = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				mgr.hideSoftInputFromWindow(this.getWindowToken(), 0);
				setEditTextFocus(false);
			} else if (keyCode == KeyEvent.KEYCODE_MENU) {
				InputMethodManager mgr = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				mgr.hideSoftInputFromWindow(this.getWindowToken(), 0);
				setEditTextFocus(false);
				
				//pass the parent to the next receiver.
				return false;
			}
			//eat the event
			return true;
		}else{
			return super.onKeyPreIme(keyCode, event);
		}
	}

}


