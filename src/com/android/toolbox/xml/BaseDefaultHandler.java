package com.android.toolbox.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.android.toolbox.Log;
import com.android.toolbox.utils.StringUtility;

/**
 * Base class for all SAX handlers
 * @author Amine.Bezzarga@labgency.com
 */
public abstract class BaseDefaultHandler extends DefaultHandler{
	

//	private final static String TAG = BaseDefaultHandler.class.getSimpleName();
	protected String TAG;
	/** 
	 * String builder for text content of xml nodes
	 * We use StringBuilder for better performance, because it's mutable
	 */
	protected StringBuilder mSb = new StringBuilder();
	protected boolean mIsLogEnabled = false;
	
	public BaseDefaultHandler() {
		super();
		TAG = getClass().getSimpleName();
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
//		mSb.delete(0, mSb.length());
		mSb.setLength(0);
		
		localName = (StringUtility.notEmpty(localName)) ? localName : qName;
		if(mIsLogEnabled){
			StringBuilder sb  = new StringBuilder();
			if(attributes!=null){
				
				for(int i=0; i<attributes.getLength();i++){
					sb.append(" " + attributes.getLocalName(i) + "=" + attributes.getValue(i));
				}
			}
			Log.v(TAG, localName + " " + sb.toString());
		}
		
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		mSb.append(ch, start, length);
		if(mIsLogEnabled && mSb.toString().trim().length()>0)
			Log.v(TAG, mSb.toString().trim());

	}

	public void endElement(String uri, String localName, String qName)throws SAXException {
		localName = (StringUtility.notEmpty(localName)) ? localName : qName;
		if(mIsLogEnabled)
			Log.v(TAG, localName);
		
	}
	
	public boolean isLogEnabled(){
		return mIsLogEnabled;
	}
	
	public void setIsLogEnabled(boolean b){
		mIsLogEnabled = b;
	}
}
