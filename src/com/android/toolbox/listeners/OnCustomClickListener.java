package com.android.toolbox.listeners;

import android.view.View;
import android.view.View.OnClickListener;



public class OnCustomClickListener implements OnClickListener {
	private int position;
	private OnCustomClickListenerInterface callback;

	// Pass in the callback (this'll be the activity) and the row position
	public OnCustomClickListener(OnCustomClickListenerInterface callback, int pos) {
		position = pos;
		this.callback = callback;
	}



	// The onClick method which has NO position information
	@Override
	public void onClick(View v) {
		// Let's call our custom callback with the position we added in the constructor
		callback.OnCustomClick(v, position);
	}
}
