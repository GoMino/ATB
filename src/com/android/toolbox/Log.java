package com.android.toolbox;




/**
 * A wrapper for log-output. Useful to disable log output before shipping the
 * application (the debug information can still be collected internally and send
 * to the developer when an error appears)
 * 
 * @author Spobo
 * @author Gomino
 * 
 */
public class Log {
	public static final int VERBOSE = android.util.Log.VERBOSE;
	public static final int DEBUG = android.util.Log.DEBUG;
	public static final int ERROR = android.util.Log.ERROR;
	public static final int INFO = android.util.Log.INFO;
	public static final int WARN = android.util.Log.WARN;
	public static final int ASSERT = android.util.Log.ASSERT;
	public static boolean isLogEnabled = BuildConfig.DEBUG; 
	public static boolean isDebug = BuildConfig.DEBUG; 
	public static boolean isLogMethodName = BuildConfig.DEBUG; 

	public interface LogInterface {

		void d(String logTag, String logText);
		void d(String logText);
		void d();

		void e(String logTag, String logText);
		void e(String logTag, String logText, Throwable exception);
		void e(String logText);
		void e();

		void w(String logTag, String logText);
		void w(String logTag, String logText, Throwable exception);
		void w(String logText);
		void w();
		
		void v(String logTag, String logText);
		void v(String logText);
		void v();

		void i(String logTag, String logText);
		void i(String logText);
		void i();

	}



	/**
	 * @param callDepth
	 *            Normally 2 (or 1 if directly used). This method is calles from
	 *            a util class like a Log class you dont want the name of the
	 *            log method but the method which called the log method, so pass
	 *            2 as the call depth! (1 would be the Log class and 0 would be
	 *            the Thread.getStackTrace() method)
	 * @return
	 */
	public static StackTraceElement getStackTraceElement(int callDepth) {
//		StackTraceElement x = Thread.currentThread().getStackTrace()[callDepth];
		StackTraceElement caller = new Throwable().getStackTrace()[callDepth]; 
		return caller;
	}
	
	public static String getCurrentMethod(StackTraceElement caller){
		return "[" + caller.getMethodName() + ":"+ caller.getLineNumber() +"]";
	}
	
	public static String getCurrentClassName(StackTraceElement caller){
		String name = caller.getFileName();
		if(name != null){
			name = name.replaceAll(".java", "");
		}
		return name;
	}

	private static LogInterface instance;

	public static LogInterface getInstance() {
		if (instance == null)
			instance = newDefaultAndroidLog();
		return instance;
	}

	public static void setInstance(LogInterface instance) {
		Log.instance = instance;
	}

	private static LogInterface newDefaultAndroidLog() {
		return new LogInterface() {

			@Override
			public void w(String logTag, String logText) {
				if (isLogEnabled){
					logText = addMethodNameAndLineNumberIfEnable(logText);
					android.util.Log.w(logTag, logText);
				}
			}

			@Override
			public void w(String logTag, String logText, Throwable exception) {
				if (isLogEnabled){
					logText = addMethodNameAndLineNumberIfEnable(logText);
					android.util.Log.w(logTag, logText, exception);
				}
			}
			
			public void w(String logText) {
				if (isLogEnabled){
					StackTraceElement caller = getStackTraceElement(3);
					logText = getCurrentMethod(caller)+" "+logText; 
					android.util.Log.w(getCurrentClassName(caller), logText);
				}
			}
			
			public void w() {
				if (isLogEnabled){
					StackTraceElement caller = getStackTraceElement(3);
					String logText = getCurrentMethod(caller); 
					android.util.Log.w(getCurrentClassName(caller), logText);
				}
			}

			@Override
			public void v(String logTag, String logText) {
				if (isLogEnabled){
					logText = addMethodNameAndLineNumberIfEnable(logText);
					android.util.Log.v(logTag, logText);
				}
			}

			public void v(String logText) {
				if (isLogEnabled){
					StackTraceElement caller = getStackTraceElement(3);
					logText = getCurrentMethod(caller)+" "+logText; 
					android.util.Log.v(getCurrentClassName(caller), logText);
				}
			}
			
			public void v() {
				if (isLogEnabled){
					StackTraceElement caller = getStackTraceElement(3);
					String logText = getCurrentMethod(caller); 
					android.util.Log.v(getCurrentClassName(caller), logText);
				}
			}

			@Override
			public void i(String logTag, String logText) {
				if (isLogEnabled){
					logText = addMethodNameAndLineNumberIfEnable(logText);
					android.util.Log.i(logTag, logText);
				}
			}
			
			public void i(String logText) {
				if (isLogEnabled){
					StackTraceElement caller = getStackTraceElement(3);
					logText = getCurrentMethod(caller)+" "+logText; 
					android.util.Log.i(getCurrentClassName(caller), logText);
				}
			}
			
			public void i() {
				if (isLogEnabled){
					StackTraceElement caller = getStackTraceElement(3);
					String logText = getCurrentMethod(caller); 
					android.util.Log.i(getCurrentClassName(caller), logText);
				}
			}

			@Override
			public void e(String logTag, String logText) {
				if (isLogEnabled){
					logText = addMethodNameAndLineNumberIfEnable(logText);
					android.util.Log.e(logTag, logText);
				}
			}

			@Override
			public void e(String logTag, String logText, Throwable exception) {
				if (isLogEnabled){
					logText = addMethodNameAndLineNumberIfEnable(logText);
					android.util.Log.e(logTag, logText, exception);
				}
			}

			public void e(String logText) {
				if (isLogEnabled){
					StackTraceElement caller = getStackTraceElement(3);
					logText = getCurrentMethod(caller)+" "+logText; 
					android.util.Log.e(getCurrentClassName(caller), logText);
				}
			}
			
			
			public void e() {
				if (isLogEnabled){
					StackTraceElement caller = getStackTraceElement(3);
					String logText = getCurrentMethod(caller); 
					android.util.Log.e(getCurrentClassName(caller), logText);
				}
			}
			
			@Override
			public void d(String logTag, String logText) {
				if (isLogEnabled){
					logText = addMethodNameAndLineNumberIfEnable(logText);
					android.util.Log.d(logTag, logText);
				}
			}
			
			public void d(String logText) {
				if (isLogEnabled){
					StackTraceElement caller = getStackTraceElement(3);
					logText = getCurrentMethod(caller)+" "+logText; 
					android.util.Log.d(getCurrentClassName(caller), logText);
				}
			}
			
			
			public void d() {
				if (isLogEnabled){
					StackTraceElement caller = getStackTraceElement(3);
					String logText = getCurrentMethod(caller); 
					android.util.Log.d(getCurrentClassName(caller), logText);
				}
			}
			
			private String addMethodNameAndLineNumberIfEnable(String logText) {
				if(isLogMethodName){
					StackTraceElement caller = getStackTraceElement(4);
					logText = getCurrentMethod(caller)+" "+logText;
				}
				return logText;
			}

		};
	}

	public static void d(String logTag, String logText) {
		if (isLogEnabled){
			getInstance().d(logTag, logText);
		}
	}
	
	public static void d(String logText) {
		if (isLogEnabled){
			getInstance().d(logText);
		}
	}
	
	public static void d() {
		if (isLogEnabled){
			getInstance().d();
		}
	}

	public static void e(String logTag, String logText) {
		if (isLogEnabled){
			getInstance().e(logTag, logText);
		}
	}

	public static void e(String logTag, String logText, Throwable exception) {
		if (isLogEnabled){
			getInstance().e(logTag, logText, exception);
		}
	}
	
	public static void e(String logText) {
		if (isLogEnabled){
			getInstance().e(logText);
		}
	}
	
	public static void e() {
		if (isLogEnabled){
			getInstance().e();
		}
	}

	public static void w(String logTag, String logText) {
		if (isLogEnabled){
			getInstance().w(logTag, logText);
		}
	}

	public static void w(String logTag, String logText, Throwable exception) {
		if (isLogEnabled){
			getInstance().w(logTag, logText, exception);
		}
	}
	
	public static void w(String logText) {
		if (isLogEnabled){
			getInstance().w(logText);
		}
	}
	
	public static void w() {
		if (isLogEnabled){
			getInstance().w();
		}
	}

	public static void v(String logTag, String logText) {
		if (isLogEnabled){
			getInstance().v(logTag, logText);
		}
	}
	
	public static void v(String logText) {
		if (isLogEnabled){
			getInstance().v(logText);
		}
	}
	
	public static void v() {
		if (isLogEnabled){
			getInstance().v();
		}
	}

	public static void i(String logTag, String logText) {
		if (isLogEnabled){
			getInstance().i(logTag, logText);
		}
	}
	
	public static void i(String logText) {
		if (isLogEnabled){
			getInstance().i(logText);
		}
	}
	
	public static void i() {
		if (isLogEnabled){
			getInstance().i();
		}
	}
	
	public static void printStackTrace(Throwable exception) {
		if (isLogEnabled){
			exception.printStackTrace();
		}
	}
	
	public static void out(String message) {
		if (isLogEnabled){
			System.out.println(message);
		}
	}
	
	public static void err(String message) {
		if (isLogEnabled){
			System.err.println(message);
		}
	}
	
	public static void logVeryLongString(String sb){
		if (sb.length() > 4000) {
		    Log.d("sb.length = " + sb.length());
		    int chunkCount = sb.length() / 4000;     // integer division
		    for (int i = 0; i <= chunkCount; i++) {
		        int max = 4000 * (i + 1);
		        if (max >= sb.length()) {
		            Log.d("chunk " + i + " of " + chunkCount + ":" + sb.substring(4000 * i));
		        } else {
		            Log.d("chunk " + i + " of " + chunkCount + ":" + sb.substring(4000 * i, max));
		        }
		    }
		}
	}
}
