package com.pictureAir.util;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/** 连接方式 */
public class HttpUtil {
	private static AsyncHttpClient client = new AsyncHttpClient();
	
	static {
		client.setTimeout(30000); // 设置链接超时，如果不设置，默认为10s
		client.setMaxConnections(10);//设置最大的链接数量
	}

	public static void post(String urlString, RequestParams params, JsonHttpResponseHandler res) {
		client.post(urlString, params, res);
	}

	public static void post(String urlString, JsonHttpResponseHandler res) {
		client.post(urlString, res);
	}

	public static void get(String urlString, RequestParams params, JsonHttpResponseHandler res) // 带参数，获取json对象或者数组
	{
		client.get(urlString, params, res);
	}

	public static void get(String urlString, BinaryHttpResponseHandler bHandler) // 下载数据使用，会返回byte数据
	{
		client.get(urlString, bHandler);
	}

	public static void get(String urlString, RequestParams params, BinaryHttpResponseHandler bHandler) {//用户下载照片时使用
		client.get(urlString, params, bHandler);
	}
	
	public static void destoryallrequests(boolean arg){
		client.cancelAllRequests(arg);
	}

	public static AsyncHttpClient getClient() {
		return client;
	}


}
