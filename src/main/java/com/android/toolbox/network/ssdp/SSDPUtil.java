package com.android.toolbox.network.ssdp;

import java.util.Scanner;

public class SSDPUtil {
	
	final static String SSDP_DISCORY_BROADCAST_ADDRESS = "239.255.255.250";
	final static int SSDP_DISCORY_BROADCAST_PORT = 1900;

	public static String buildSearchRequest(String searchTarget, int second){
		String result = "M-SEARCH * HTTP/1.1\r\n"
				+ "HOST: " +SSDP_DISCORY_BROADCAST_ADDRESS+ ":" + SSDP_DISCORY_BROADCAST_PORT + "\r\n"
				+ "MAN: \"ssdp:discover\"\r\n" 
				+ "MX: " + second + "\r\n"              //Maximum time (in seconds) to wait for response of host
				+ "ST: " + searchTarget + "\r\n"        //URN value of service to search
				+ "\r\n";
		return result;
	} 

	public static String parseHeaderValue(String content, String headerName) {
		Scanner s = new Scanner(content);
		s.nextLine(); // Skip the start line

		while (s.hasNextLine()) {
			String line = s.nextLine();
			int index = line.indexOf(':');
			try{
				String header = line.substring(0, index);
				if (headerName.equalsIgnoreCase(header.trim())) {
					return line.substring(index + 1).trim();
				}
			}catch (IndexOutOfBoundsException e){
				e.printStackTrace();
			}

		}

		return null;
	}

	public static String parseStartLine(String content) {
		Scanner s = new Scanner(content);
		return s.nextLine();
	}


}
