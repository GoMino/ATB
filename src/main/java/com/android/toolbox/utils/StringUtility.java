package com.android.toolbox.utils;

/**
 * @author amine.bezzarga@labgency.com
 */
public class StringUtility {
	
	/**
	 * @param stringToCheck
	 * @return true if the parameter is not null and not empty
	 * This function is usefull when parsing int, in order to avoid NullPointerException and or none int character
	 * We could Use StringUtils from android SDK, but it much heavier and will break the possibility to test the parser without running on android 
	 */
	public static boolean notEmpty(String stringToCheck) {
		 return (stringToCheck != null && stringToCheck.length() > 0);
	}
	
    public static boolean isInteger( String input )  
    {  
       try  
       {  
          Integer.parseInt( input );  
          return true;  
       }  
       catch( Exception e)  
       {  
          return false;  
       }  
    }  

}
