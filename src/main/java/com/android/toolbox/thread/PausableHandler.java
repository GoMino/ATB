package com.android.toolbox.thread;

import java.util.Stack;
import java.util.Vector;

import com.android.toolbox.Log;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;


/**
 * Message Handler class that supports buffering up of messages when the
 * activity is paused i.e. in the background.
 * @author gomino (amine.bezzarga@labgency.com)
 */
abstract public class PausableHandler extends Handler {
	public final static String TAG = PausableHandler.class.getSimpleName();
	/**
	 * Message Queue Buffer
	 */
	final Stack<Message> messageQueueBuffer = new Stack<Message>();

	/**
	 * Flag indicating the pause state
	 */
	private boolean paused;

	public PausableHandler() {
		super();
	}
	
	public PausableHandler(Looper mainLooper) {
		super(mainLooper);
	}

	/**
	 * Resume the handler
	 */
	final public void resume() {
		paused = false;

//		while (messageQueueBuffer.size() > 0) {
//			final Message msg = messageQueueBuffer.elementAt(0);
//			messageQueueBuffer.removeElementAt(0);
//			sendMessage(msg);
//		}
		
		while (! messageQueueBuffer.empty())
        {

			Message msg = messageQueueBuffer.pop();
//			Looper.loop();
			boolean isEnqueued = sendMessageAtFrontOfQueue(msg);
			Log.v(TAG, "[resume] message " + msg + " has been enqueued ? " + isEnqueued + " and has callback ? " + msg.getCallback() + " and has target ? " + msg.getTarget());
            
        }  
	}

	/**
	 * Pause the handler
	 */
	final synchronized public void pause() {
		paused = true;
	}

	/**
	 * Notification that the message is about to be stored as the activity is
	 * paused. If not handled the message will be saved and replayed when the
	 * activity resumes.
	 * 
	 * @param message
	 *            the message which optional can be handled
	 * @return true if the message is to be stored
	 */
	protected abstract boolean storeMessage(Message message);

	/**
	 * Notification message to be processed. This will either be directly from
	 * handleMessage or played back from a saved message when the activity was
	 * paused.
	 * 
	 * @param message
	 *            the message to be handled
	 */
	protected abstract void processMessage(Message message);

	/** {@inheritDoc} */
	@Override
	public void handleMessage(Message msg) {
		Log.v(TAG, "[handleMessage] " + msg.toString());
		if (paused) {
			if (storeMessage(msg)) {
//				Message msgCopy = new Message();
//				msgCopy.copyFrom(msg);
//				messageQueueBuffer.add(msgCopy);
				messageQueueBuffer.push(Message.obtain(msg));
			}
		} else {
			processMessage(msg);
		}
		return;
	}
	
	 public final void pausablePost(Runnable r) {
		 if (paused) {
			 Message msg = Message.obtain(this,r);
			 if (storeMessage(msg)) {
				 messageQueueBuffer.push(msg);
			 }
		 } else {
			 post(r);
		 }
		 
	}
}
