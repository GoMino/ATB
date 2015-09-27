package com.android.toolbox.fragments;


import com.android.toolbox.R;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class ProgressDialogFragment extends DialogFragment {

	private static final String TAG 						= "ProgressDialogFragment";
	public final static String	BUNDLE_KEY_MESSAGE  		= "message";
	private String mMessage									= null;
	private TextView mTextMessage							= null;

	public static ProgressDialogFragment newInstance(String message){
		ProgressDialogFragment fragment = new ProgressDialogFragment();

	    Bundle args = new Bundle();
	    args.putString(BUNDLE_KEY_MESSAGE, message);
	    fragment.setArguments(args);

	    return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setStyle(DialogFragment.STYLE_NO_FRAME, R.style.TransparentCustomDialogTheme);
		
		Bundle args = getArguments();
		if (args!=null){
			mMessage = (String) args.getString(BUNDLE_KEY_MESSAGE);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.progress_dialog_fragment, container, false);
		mTextMessage = (TextView) view.findViewById(R.id.txt_message);
		if(mTextMessage!=null){
			mTextMessage.setText(mMessage);
		}
		return view;
	}

}
