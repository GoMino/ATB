/*
 * Copyright 2010 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.toolbox.social.twitter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.android.toolbox.Log;
import com.android.toolbox.R;


public class TwitterLoginButton extends Button {
	private final static String TAG = TwitterLoginButton.class.getSimpleName();
	private String mLoginString;
	private String mLogoutString;
	
	public TwitterLoginButton(Context context) {
		super(context);
	}

	public TwitterLoginButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TwitterLoginButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void init(final Activity activity) {
		mLoginString = activity.getString(R.string.login_txt);
		mLogoutString = activity.getString(R.string.logout_txt);
		setBackgroundColor(Color.TRANSPARENT);
		setBackgroundResource(R.drawable.bt_twitter_connection_selector);

//		setText(TwitterManager.getInstance().isSessionValid() ? "Logout": "Login");
		setText(TwitterManager.getInstance().getUser() != null ? mLogoutString: mLoginString);
		drawableStateChanged();

		setOnClickListener(new ButtonOnClickListener());
	}

	private final class ButtonOnClickListener implements OnClickListener {
		@Override
		public void onClick(View arg0) {
			if (TwitterManager.getInstance().getUser() != null) {
				Log.e(TAG, "logout");
				TwitterManager.getInstance().logout();
			} else {
				Log.e(TAG, "login");
				TwitterManager.getInstance().login();
			}
		}
	}


}
