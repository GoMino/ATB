package com.android.toolbox.network.ssdp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.xmlpull.v1.XmlPullParserException;

import com.android.toolbox.Log;
import com.android.toolbox.network.http.HttpRequestUtil;
import com.android.toolbox.xml.XmlPullParserUtil;

import android.content.Context;

public class SSDPDiscovery extends Thread {
	public static boolean DEBUG = false;
	boolean flag;
	InetAddress group = null;
	MulticastSocket clientSocket = null;

	private Context mContext;
	private SSDPDIscoveryListener mListener;
	private String mSearchTarget;
	private int mTimeout;
	private int mLimit = -1; // default unlimited will search till timeout;
	private boolean mSearchForFriendlyName = true; // default true 
	
	public interface SSDPDIscoveryListener{
		public void onNewMessage(String message);
		public void onNewDeviceFound(SSDPDevice device);
		public void onNewFriendlyNameFound(SSDPDevice device);
		public void onDiscoveryEnd(List<SSDPDevice> devices);
	}
	
	public SSDPDiscovery(Context context, String searchTarget, int timeout){
		mContext = context;
		mSearchTarget = searchTarget;
		mTimeout = timeout;
	}
	
	public void setListener(SSDPDIscoveryListener listener){
		mListener = listener;
	}
	
	public void setLimit(int limit){
		mLimit = limit;
	}

	public void setSearForFriendlyName(boolean b){
		mSearchForFriendlyName = b;
	}
	
	@Override
	public void run() {

		flag = true;
		List<SSDPDevice> ssdpDevices = new ArrayList<SSDPDevice>();

		// host the hostName to be resolved to an address or null.
		try {
			group = InetAddress.getByName(SSDPUtil.SSDP_DISCORY_BROADCAST_ADDRESS);
		} catch (Exception e) {
			e.getMessage();
			e.printStackTrace();
		}

		// host can be null which means that an address of the loopback interface is returned.
		if (group == null) {
			log("getByName(): returns address of loopback interface.");
		}
		byte[] sendData;
		byte[] receiveData = new byte[512];

		String sentence = SSDPUtil.buildSearchRequest(mSearchTarget, mTimeout);
		log("build request =\r\n" + sentence);
		sendData = sentence.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, group, SSDPUtil.SSDP_DISCORY_BROADCAST_PORT);

		log("sent packet...");
		try {
			clientSocket = new MulticastSocket();
			clientSocket.send(sendPacket);
			clientSocket.setSoTimeout(mTimeout * 1000);
		} catch (Exception e) {
			e.getMessage();
			e.printStackTrace();
		}

		log("waiting response...");
		while (flag) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			try {
				boolean isc = clientSocket.isConnected();
				clientSocket.receive(receivePacket);
				log("received response");
			} catch (Exception Ex) {
				log("time out Exception");
			}
			if (receivePacket.getAddress() == null) {
				log("receivePacket.getAddress() == null");
				break;
			}
			
			log("senders Address : " + receivePacket.getAddress().getHostAddress());
			String controllerResponse = new String(receivePacket.getData());
			log("controllerResponse : " + controllerResponse);
			
			String deviceURL = SSDPUtil.parseHeaderValue(controllerResponse, "LOCATION");
			if(deviceURL!=null && deviceURL.length()>0){
				log("device LOCATION found = " + deviceURL);
				String deviceST = SSDPUtil.parseHeaderValue(controllerResponse, "ST");
				String deviceID = SSDPUtil.parseHeaderValue(controllerResponse, "USN");
//				String deviceIP = receivePacket.getAddress().getHostAddress();
//				String devicePort = ((InetSocketAddress)receivePacket.getSocketAddress()).getPort();
				List<String[]> regexMatches = findIpAndPortFromString(deviceURL);
				String[] address = regexMatches.get(0);
				String deviceIP = address[0];
				String devicePort = address[1];
				
				SSDPDevice device = new SSDPDevice().setId(deviceID).setLocation(deviceURL).setST(deviceST).setIP(deviceIP).setPort(devicePort);

				if(ssdpDevices.size()>0 && ssdpDevices.contains(device)){
					ssdpDevices.remove(device);
				}
				ssdpDevices.add(device);
				log("add usn:" + device.getId() + " url:"+device.getLocation() + " st:"+device.getST() + " listSize:" + ssdpDevices.size());
				if(mListener!=null){
					mListener.onNewDeviceFound(device);
				}

				if (mLimit!= -1 && mLimit-- > 0) {
					flag = false;
				}
			}

		} // end of while()

		log("clientSocket.close()");
		clientSocket.close();

		if(ssdpDevices.size()>0){
			if(mSearchForFriendlyName){
				for (final SSDPDevice device:ssdpDevices){
					log("search for friendlyname at Location:"+device.getLocation());
					//mTextView.append("\nfound Location:"+url);
					HttpResponse responseBody = HttpRequestUtil.doGetRequestNgetResponse(mContext, device.getLocation());
					String data  = HttpRequestUtil.getResponseBody(responseBody);
					try {
						XmlPullParserUtil parser = new XmlPullParserUtil();
						parser.setParserInput(data);
						Map<String, String> result = parser.processDocument();
						final String friendlyName = result.get("friendlyName");
						device.setFriendlyName(friendlyName);
						log("found device \"" + device.getFriendlyName() + "\" Location:"+device.getLocation() + " ST:" + device.getST());
						if(mListener!=null){
							mListener.onNewFriendlyNameFound(device);
						}
					} catch (XmlPullParserException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}else{
			log("no devices found");
		}

		if(mListener!=null){
			mListener.onDiscoveryEnd(ssdpDevices);
		}


	} // end run
	
	private List<String[]> findIpAndPortFromString(String input) {

		Pattern pattern = Pattern.compile("(\\d{1,3}(?:\\.\\d{1,3}){3})(?::(\\d{1,5}))?");
		Matcher matcher = pattern.matcher(input);

		List<String[]> listMatches = new ArrayList<String[]>();

		while (matcher.find()) {
			String ip = matcher.group(1);
			log("found ip:" + ip);
			
			String port = matcher.group(2);
			log("found port:" + port);
			String[] address = {ip,port};
			listMatches.add(address);
		}

		return listMatches;
	}
	
	public void log(String message){
		if(DEBUG) Log.d(message);
		if(mListener!=null){
			mListener.onNewMessage(message);
		}
	}
}
