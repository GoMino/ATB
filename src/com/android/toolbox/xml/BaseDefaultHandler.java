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
	protected boolean isLogEnabled = false;
	
	
	public BaseDefaultHandler() {
		super();
		TAG = getClass().getSimpleName();
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
//		mSb.delete(0, mSb.length());
		mSb.setLength(0);
		localName = (StringUtility.notEmpty(localName)) ? localName : qName;
		if(isLogEnabled)
			Log.e(TAG, "startElement\t: " + localName);
		
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		mSb.append(ch, start, length);
		if(isLogEnabled)
			Log.e(TAG, "characters\t\t: " + mSb.toString().trim());
	}

	public void endElement(String uri, String localName, String qName)throws SAXException {
		localName = (StringUtility.notEmpty(localName)) ? localName : qName;
		if(isLogEnabled)
			Log.e(TAG, "endElement\t\t: " + localName);
	}
	
}
