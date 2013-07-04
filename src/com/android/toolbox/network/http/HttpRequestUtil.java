package com.android.toolbox.network.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;


public class HttpRequestUtil {

	/**
	 * @deprecated please use doGetRequestNgetResponseBody
	 * @param context
	 * @param url
	 * @param headerList
	 * @return
	 */
	@Deprecated
	public static String doGetRequest(Context context, String url, List<Header> headerList) {

		return doGetRequestNgetResponseBody(context, url, headerList);

	}
	
	/**
	 * @param context
	 * @param url
	 * @param headerList
	 * @return
	 */
	public static String doGetRequestNgetResponseBody(Context context, String url, List<Header> headerList) {

		URI uri = null;
		
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		HttpGet get = new HttpGet(uri);

		for (Header header : headerList) {
			get.addHeader(header);
		}

		return getResponseBody(context, get);

	}	
	
	/**
	 * @param context
	 * @param url
	 * @param headerList
	 * @return
	 */
	public static HttpResponse doGetRequestNgetResponse(Context context, String url, List<Header> headerList) {

		URI uri = null;
		
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		HttpGet get = new HttpGet(uri);

		for (Header header : headerList) {
			get.addHeader(header);
		}

		return getResponse(context, get);

	}	

	/**
	 * @param context
	 * @param url
	 * @return
	 */
	public static HttpResponse doGetRequestNgetResponse(Context context, String url) {

		URI uri = null;
		
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		HttpGet get = new HttpGet(uri);

		return getResponse(context, get);

	}	

	/**
	 * @deprecated please use doPostRequestNgetResponseBody
	 * @param context
	 * @param url
	 * @param params
	 * @return
	 */
	@Deprecated
	public static String doPostRequest(Context context, String url, List<BasicNameValuePair> params) {

		return doPostRequestNgetResponseBody( context, url, params);

	}
	
	/**
	 * @param context
	 * @param url
	 * @param params
	 * @return
	 */
	public static HttpResponse doPostRequestNgetResponse(Context context, String url) {
		
		HttpPost post = new HttpPost(url);

		return getResponse(context, post);

	}	

	/**
	 * @param context
	 * @param url
	 * @param params
	 * @return
	 */
	public static String doPostRequestNgetResponseBody(Context context, String url, List<BasicNameValuePair> params) {
		
		HttpPost post = new HttpPost(url);

		UrlEncodedFormEntity ent = null;

		try {
			ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if (ent != null)
			post.setEntity(ent);

		return getResponseBody(context, post);

	}	
	
	/**
	 * @param context
	 * @param url
	 * @param params
	 * @return
	 */
	public static HttpResponse doPostRequestNgetResponse(Context context, String url, List<BasicNameValuePair> params) {
		
		HttpPost post = new HttpPost(url);

		UrlEncodedFormEntity ent = null;

		try {
			ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if (ent != null)
			post.setEntity(ent);

		return getResponse(context, post);

	}

	/**
	 * @deprecated please use doPostRequestNgetResponseBody
	 * @param context
	 * @param url
	 * @param jsonParams
	 * @return
	 */
	@Deprecated
	public static String doPostRequest(Context context, String url, JSONObject jsonParams) {

		return doPostRequestNgetResponseBody( context, url, jsonParams);

	}
	
	/**
	 * @param context
	 * @param url
	 * @param jsonParams
	 * @return
	 */
	public static String doPostRequestNgetResponseBody(Context context, String url, JSONObject jsonParams) {

		HttpPost post = new HttpPost(url);
		StringEntity ent = null;

		try {
			ent = new StringEntity(jsonParams.toString(), HTTP.UTF_8);
			ent.setContentType("application/json");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if (ent != null)
			post.setEntity(ent);

		return getResponseBody(context, post);

	}
	
	
	/**
	 * @param context
	 * @param url
	 * @param jsonParams
	 * @return
	 */
	public static HttpResponse doPostRequestNgetResponse(Context context, String url, JSONObject jsonParams) {

		HttpPost post = new HttpPost(url);
		StringEntity ent = null;

		try {
			ent = new StringEntity(jsonParams.toString(), HTTP.UTF_8);
			ent.setContentType("application/json");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if (ent != null)
			post.setEntity(ent);

		return getResponse(context, post);

	}
	
	/**
	 * @param context
	 * @param httpRequest
	 * @return
	 */
	private static String getResponseBody(Context context, HttpRequestBase httpRequest) {

		HttpResponse response = getResponse( context, httpRequest);

		return getResponseBody(response);
	}
	
	/**
	 * It returns response body string
	 * @param response
	 * @return
	 */
	public static String getResponseBody(HttpResponse response) {

		HttpEntity resEntity = null;
		if (response != null)
			resEntity = response.getEntity();

		if (resEntity != null) {
			try {
				return EntityUtils.toString(resEntity);
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return "";
	}

	/**
	 * @param context
	 * @param url
	 * @param headerList
	 * @return
	 */
	public static HashMap<String,String> doGetRequestNgetResponseHeader(Context context, String url, List<Header> headerList) {

		URI uri = null;
		
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		HttpGet get = new HttpGet(uri);

		for (Header header : headerList) {
			get.addHeader(header);
		}

		return getResponseHeader(context, get);

	}	

	/**
	 * @param context
	 * @param url
	 * @param params
	 * @return
	 */
	public static HashMap<String,String> doPostRequestNgetResponseHeader(Context context, String url, List<BasicNameValuePair> params) {

		HttpPost post = new HttpPost(url);

		UrlEncodedFormEntity ent = null;

		try {
			ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if (ent != null)
			post.setEntity(ent);

		return getResponseHeader(context, post);

	}	
	
	/**
	 * @param context
	 * @param url
	 * @param jsonParams
	 * @return
	 */
	public static HashMap<String,String> doPostRequestNgetResponseHeader(Context context, String url, JSONObject jsonParams) {

		HttpPost post = new HttpPost(url);
		StringEntity ent = null;

		try {
			ent = new StringEntity(jsonParams.toString(), HTTP.UTF_8);
			ent.setContentType("application/json");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if (ent != null)
			post.setEntity(ent);

		return getResponseHeader(context, post);

	}
	
	/**
	 * @param context
	 * @param httpRequest
	 * @return
	 */
	private static HashMap<String,String> getResponseHeader(Context context, HttpRequestBase httpRequest) {

		HttpResponse response = getResponse( context, httpRequest);
		
		return getResponseHeader(response);
	}
	
	/**
	 * @param response
	 * @param headerName
	 * @return
	 */
	public static String getResponseHeaderValue(HttpResponse response, String headerName){
		
		HashMap<String,String> header = getResponseHeader(response);
		return (String)header.get(headerName);
		
	}
	/**
	 * @param response
	 * @return
	 */
	public static HashMap<String,String> getResponseHeader(HttpResponse response) {

		if (response != null){
			Header[] headers = response.getAllHeaders();
			return converHeaders2Map(headers);
		}else{
			return new HashMap<String,String>();
		}
		
	}

	/**
	 * when response parameter is null, it will return 410 Gone status.
	 * @param response
	 * @return
	 */
	public static int getResponseCode(HttpResponse response) {

		if (response != null){
			return response.getStatusLine().getStatusCode();
		}else{
			return HttpStatus.SC_GONE;
		}
		
	}
	
	/**
	 * @param headers
	 * @return
	 */
	private static HashMap<String,String> converHeaders2Map(Header[] headers){
		
		HashMap<String,String> hashMap = new HashMap<String,String>();
		
		for(Header header: headers){
			hashMap.put(header.getName(), header.getValue());
		}
		
		return hashMap;
		
	}
	
	/**
	 * @param context
	 * @param httpRequest
	 * @return
	 */
	public static HttpResponse getResponse(Context context, HttpRequestBase httpRequest) {
		
		HttpClient httpClient = HttpClientUtil.getHttpClient(context);
		
		HttpResponse response = null;
		try {
			response = httpClient.execute(httpRequest);
		} catch (ClientProtocolException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		return response;
	}
}
