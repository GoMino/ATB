package com.android.toolbox.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class XmlPullParserUtil {
	
	private XmlPullParserFactory parserCreator;
	private XmlPullParser parser;
	private HashMap<String, String> hashMap;
	
	private String name;
	private String uri;
	
	public XmlPullParserUtil() throws XmlPullParserException {
		parserCreator = XmlPullParserFactory.newInstance();
		parser = parserCreator.newPullParser();
	}
	
	public XmlPullParserUtil(XmlPullParser xmlPullParser) throws XmlPullParserException {		
		parser = xmlPullParser;
	}
	
	public void setParserInput(InputStream inputStream)
			throws XmlPullParserException {
		parser.setInput(inputStream, null);
	}
	
	public void setParserInput(String context)
			throws XmlPullParserException {
		parser.setInput(new StringReader(context));
	}
	
	public void setParserInput(URL isText) throws XmlPullParserException,
			IOException {
		parser.setInput(isText.openStream(), null);
	}
	
	public HashMap<String, String> processDocument()
			throws XmlPullParserException, IOException {
		
		boolean startTag = false;
		boolean isEmpty = false;
		int beforeTag = 0;
		
		hashMap = new HashMap<String, String>();
		
		int eventType = parser.getEventType();
		do {
			if (eventType == XmlPullParser.START_DOCUMENT) {
				// Log.d("processDocument()", "Start document");
			} else if (eventType == XmlPullParser.END_DOCUMENT) {
				// Log.d("processDocument()", "End document");
			} else if (eventType == XmlPullParser.START_TAG) {
				processStartElement(parser);
				isEmpty = parser.isEmptyElementTag();
				startTag = true;
			} else if (eventType == XmlPullParser.END_TAG) {
				processEndElement(parser);
				if (beforeTag == XmlPullParser.START_TAG || isEmpty) {
					hashMap.put(name, "");
				}
				startTag = false;
			} else if (eventType == XmlPullParser.TEXT) {
				if (startTag) {
					hashMap.put(name, parser.getText().trim());
				}
			}
			beforeTag = eventType;
			eventType = parser.next();
		} while (eventType != XmlPullParser.END_DOCUMENT);

		return hashMap;
	}
	
	public void processStartElement(XmlPullParser xpp) {
		name = xpp.getName();
		uri = xpp.getNamespace();

		if ("".equals(uri)) {
			// Log.d("Start element", name);
		} else {
			// Log.d("Start element", "{" + uri + "}" + name);
		}
	}

	public void processEndElement(XmlPullParser xpp) {
		name = xpp.getName();
		uri = xpp.getNamespace();
		if ("".equals(uri)) {
			// Log.d("End element", name);
		} else {
			// Log.d("End element", "{" + uri + "}" + name);
		}

	}

}
