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

package com.android.toolbox.managers;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SessionEvents {

    protected ConcurrentLinkedQueue<AuthListener> mAuthListeners = new ConcurrentLinkedQueue<AuthListener>();
    protected ConcurrentLinkedQueue<LogoutListener> mLogoutListeners = new ConcurrentLinkedQueue<LogoutListener>();

    /**
     * Associate the given listener with this Facebook object. The listener's
     * callback interface will be invoked when authentication events occur.
     * 
     * @param listener
     *            The callback object for notifying the application when auth
     *            events happen.
     */
    public void addAuthListener(AuthListener listener) {
        mAuthListeners.add(listener);
    }

    /**
     * Remove the given listener from the list of those that will be notified
     * when authentication events occur.
     * 
     * @param listener
     *            The callback object for notifying the application when auth
     *            events happen.
     */
    public void removeAuthListener(AuthListener listener) {
        mAuthListeners.remove(listener);
    }

    /**
     * Associate the given listener with this Facebook object. The listener's
     * callback interface will be invoked when logout occurs.
     * 
     * @param listener
     *            The callback object for notifying the application when log out
     *            starts and finishes.
     */
    public void addLogoutListener(LogoutListener listener) {
        mLogoutListeners.add(listener);
    }

    /**
     * Remove the given listener from the list of those that will be notified
     * when logout occurs.
     * 
     * @param listener
     *            The callback object for notifying the application when log out
     *            starts and finishes.
     */
    public void removeLogoutListener(LogoutListener listener) {
        mLogoutListeners.remove(listener);
    }

    public void onLoginSuccess() {
        for (AuthListener listener : mAuthListeners) {
        	if(listener!=null){
        		listener.onAuthSucceed();
        	}
        }
    }

    public void onLoginError(String error) {
        for (AuthListener listener : mAuthListeners) {
        	if(listener!=null){
        		listener.onAuthFail(error);
        	}
        }
    }

    public void onLogoutBegin() {
        for (LogoutListener l : mLogoutListeners) {
        	if(l!=null){
        		l.onLogoutBegin();
        	}
        }
    }

    public void onLogoutFinish() {
        for (LogoutListener l : mLogoutListeners) {
        	if(l!=null){
        		l.onLogoutFinish();
        	}
        }
    }
    
    public void onLogoutError(String error) {
        for (LogoutListener l : mLogoutListeners) {
        	if(l!=null){
        		l.onLogoutError(error);
        	}
        }
    }


    /**
     * Callback interface for authorization events.
     */
    public static interface AuthListener {

        /**
         * Called when a auth flow completes successfully and a valid OAuth
         * Token was received. Executed by the thread that initiated the
         * authentication. API requests can now be made.
         */
        public void onAuthSucceed();

        /**
         * Called when a login completes unsuccessfully with an error.
         * 
         * Executed by the thread that initiated the authentication.
         */
        public void onAuthFail(String error);
        
//        /**
//         * Called when a auth flow completes successfully till continuum purchase has been retrieved
//         */
//        public void onContinuumReceived(boolean success);
    }

    /**
     * Callback interface for logout events.
     */
    public static interface LogoutListener {
        /**
         * Called when logout begins, before session is invalidated. Last chance
         * to make an API call. Executed by the thread that initiated the
         * logout.
         */
        public void onLogoutBegin();

        /**
         * Called when the session information has been cleared. UI should be
         * updated to reflect logged-out state.
         * 
         * Executed by the thread that initiated the logout.
         */
        public void onLogoutFinish();
        
        /**
         * Called when the session hasn't been cleared.
         * 
         * Executed by the thread that initiated the logout.
         */
        public void onLogoutError(String error);
    }

    
    public void clear(){
    	mAuthListeners.clear();
    	mLogoutListeners.clear();
    }
}
