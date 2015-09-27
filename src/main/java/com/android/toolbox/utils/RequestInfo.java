package com.android.toolbox.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gomino
 * This class is use to handle request at base manager sublass level
 *
 */
public class RequestInfo {
	
	private int id;
	private String name;
	private Map<String,Object> params;
	public final static String RESPONSE_FORMAT_KEY = "responseFormatKey";
	
	public RequestInfo(String  name){
		this.name = name;
		params = new HashMap<String,Object>();
	}
	
//	public RequestInfo(String name, String C2DMtoken){
//		this.name = name;
//		params = new HashMap<String,Object>();
//	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setId(int id) {
		this.id = id;
	}
	
//	public String getRegisrtationId() {
//		return registrationId;
//	}
	
	public void addParam(String paramName, Object paramValue){
		if (paramName != null && paramValue != null){
			params.put(paramName, paramValue);
		}
	}
	
	public Object getParam(String key){
		return params.get(key);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(Object obj: params.values()){
			if (obj instanceof int[]){
				int[] intArray = (int[]) obj;
				for(int i=0; i<intArray.length;i++){
					sb.append(intArray[i]);
				}
			}
		}
		return "requestInfo :" + name + "("+ id+")" + " :" + params.toString() + " ["+sb+"]";
	}
	
	
}
