package com.android.toolbox.utils;

public class XMLEncoder {
	
	public static String encodeXML(String toEncode){
		String encoded = null;
		if(toEncode != null){
			encoded = toEncode.replace("&", "&amp;");
			encoded = encoded.replace("<", "&lt;");
			encoded = encoded.replace(">", "&gt;");
			encoded = encoded.replace("\"", "&quot;");
			encoded = encoded.replace("'", "&apos;");
		}
		return encoded;
	}
}

