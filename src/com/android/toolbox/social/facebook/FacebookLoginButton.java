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

package com.android.toolbox.social.facebook;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.android.toolbox.R;
import com.android.toolbox.social.facebook.SessionEvents.AuthListener;
import com.android.toolbox.social.facebook.SessionEvents.LogoutListener;
import com.facebook.android.Facebook;

public class FacebookLoginButton extends Button {

    private Facebook mFb;
    private Handler mHandler;
    private SessionListener mSessionListener = new SessionListener();
    private String[] mPermissions;
    private Activity mActivity;
    private int mActivityCode;
    private String mLoginString;
    private String mLogoutString;

    public FacebookLoginButton(Context context) {
        super(context);
    }

    public FacebookLoginButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FacebookLoginButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void init(final Activity activity, final int activityCode, final Facebook fb) {
        init(activity, activityCode, fb, new String[] {});
    }

    public void init(final Activity activity, final int activityCode, final Facebook fb, final String[] permissions) {
        mActivity = activity;
        mActivityCode = activityCode;
        mFb = fb;
        mPermissions = permissions;
        mHandler = new Handler();
        mLoginString = mActivity.getString(R.string.login_txt);
        mLogoutString = mActivity.getString(R.string.logout_txt);

        setBackgroundColor(Color.TRANSPARENT);
//        setImageResource(fb.isSessionValid() ? R.drawable.logout_button : R.drawable.login_button);
        setBackgroundResource(R.drawable.bt_facebook_connection_selector);
    	setText(fb.isSessionValid() ? mLogoutString: mLoginString);
        drawableStateChanged();

        SessionEvents.addAuthListener(mSessionListener);
        SessionEvents.addLogoutListener(mSessionListener);
        setOnClickListener(new ButtonOnClickListener());
    }

    private final class ButtonOnClickListener implements OnClickListener {
        /*
         * Source Tag: login_tag
         */
        @Override
        public void onClick(View arg0) {
            if (mFb.isSessionValid()) {
                FacebookManager.getInstance().logout();
            } else {
                FacebookManager.getInstance().login();
            }
        }
    }

    private class SessionListener implements AuthListener, LogoutListener {

        @Override
        public void onAuthSucceed() {
//            setBackgroundResource(R.drawable.logout_button);
        	setText(mLogoutString);
            SessionStore.save(mFb, getContext());
        }

        @Override
        public void onAuthFail(String error) {
        }

        @Override
        public void onLogoutBegin() {
        }

        @Override
        public void onLogoutFinish() {
            SessionStore.clear(getContext());
//            setImageResource(R.drawable.login_button);
        	setText(mLoginString);
        }
    }

}
