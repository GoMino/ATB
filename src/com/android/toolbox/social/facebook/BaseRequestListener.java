package com.android.toolbox.social.facebook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import com.android.toolbox.Log;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

/**
 * Skeleton base class for RequestListeners, providing default error handling.
 * Applications should handle these error conditions.
 */
public abstract class BaseRequestListener implements RequestListener {

    @Override
    public void onFacebookError(FacebookError e, final Object state) {
        Log.e("Facebook", e.getMessage());
        Log.printStackTrace(e);
    }

    @Override
    public void onFileNotFoundException(FileNotFoundException e, final Object state) {
        Log.e("Facebook", e.getMessage());
        Log.printStackTrace(e);
    }

    @Override
    public void onIOException(IOException e, final Object state) {
        Log.e("Facebook", e.getMessage());
        Log.printStackTrace(e);
    }

    @Override
    public void onMalformedURLException(MalformedURLException e, final Object state) {
        Log.e("Facebook", e.getMessage());
        Log.printStackTrace(e);
    }

}
