package com.android.toolbox.xml;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.android.toolbox.Log;

/**
 * Base class for all SAX handlers
 * @author Amine.Bezzarga@labgency.com
 * Keeping track of parent relationship from http://www.ibm.com/developerworks/xml/library/x-tipsaxdo2/index.html
 */
public abstract class ParentTrackingBaseDefaultHandler extends BaseDefaultHandler{
	
//	private final static String TAG = ParentTrackingBaseDefaultHandler.class.getSimpleName();
//	private String TAG;
	private String    m_nodeIx;        // tracks the current node
	private Stack<String>  m_parentStack;   // tracks its parent
	private String mParentNodeName;
	
	
	
	public ParentTrackingBaseDefaultHandler() {
		super();
//		TAG = getClass().getSimpleName();
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		m_parentStack = new Stack<String>();
		m_parentStack.push("");
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		m_nodeIx = localName;
		
		mParentNodeName = getAncester(0);
		if (isLogEnabled){
			Log.e(TAG, "startElement\t: parent node is " + mParentNodeName);
		}
		
		// tell the indexer (or whoever our client is)
		// about the current element and its parent

//		m_indexer.newNode( nameSpaceURI, ..., attrs, m_nodeIx, parentNodeIx );

		// this element in turn becomes the parent for subsequent routines

		m_parentStack.push( new String( m_nodeIx ) ); 
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
		mParentNodeName = getAncester(1);
		if (isLogEnabled){
			Log.e(TAG, "characters\t\t: parent node is " + mParentNodeName);
		}
		
	}

	public void endElement(String uri, String localName, String qName)throws SAXException {
		super.endElement(uri, localName, qName);
		m_parentStack.pop();
		mParentNodeName = getAncester(0);
		if (isLogEnabled){
			Log.e(TAG, "endElement\t\t: parent node is " + mParentNodeName);
		}
	}
	
	protected String getParentNodeName(){
		return mParentNodeName;
	}
	
	private String getAncester(int depth){
		if(depth==0){
			return (String)m_parentStack.peek();
		}else{
			return (String)m_parentStack.elementAt(m_parentStack.size()-1-depth);
		}
	}
	
}
