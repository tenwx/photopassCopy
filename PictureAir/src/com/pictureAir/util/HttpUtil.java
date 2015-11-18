package com.pictureAir.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.os.StrictMode;

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

	public static String md5(String str) {
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("MD5");

			messageDigest.reset();

			messageDigest.update(str.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			System.out.println("NoSuchAlgorithmException caught!");
			System.exit(-1);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		byte[] byteArray = messageDigest.digest();

		StringBuffer md5StrBuff = new StringBuffer();

		for (int i = 0; i < byteArray.length; i++) {
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
				md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
			else
				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
		}
		// 16位加密，从第9位到25位
		return md5StrBuff.substring(8, 24).toString().toUpperCase();
	}

	public String getReallyFileName(String url) {  
	    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()  
	            .detectDiskReads().detectDiskWrites().detectNetwork() // 这里可以替换为detectAll()  
	                                                                  // 就包括了磁盘读写和网络I/O  
	            .penaltyLog() // 打印logcat，当然也可以定位到dropbox，通过文件保存相应的log  
	            .build());  
	    StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()  
	            .detectLeakedSqlLiteObjects() // 探测SQLite数据库操作  
	            .penaltyLog() // 打印logcat  
	            .penaltyDeath().build());  
	  
	    String filename = "";  
	    URL myURL;  
	    HttpURLConnection conn = null;  
	    if (url == null || url.length() < 1) {  
	        return null;  
	    }  
	  
	    try {  
	        myURL = new URL(url);  
	        conn = (HttpURLConnection) myURL.openConnection();  
	        conn.connect();  
	        conn.getResponseCode();  
	        URL absUrl = conn.getURL();// 获得真实Url  
	        filename = conn.getHeaderField("Content-Disposition");// 通过Content-Disposition获取文件名，这点跟服务器有关，需要灵活变通  
	        if (filename == null || filename.length() < 1) {  
	            filename = URLDecoder.decode(absUrl.getFile(), "UTF-8");  
	        }  
	    } catch (MalformedURLException e) {  
	        e.printStackTrace();  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    } finally {  
	        if (conn != null) {  
	            conn.disconnect();  
	            conn = null;  
	        }  
	    }  
	  
	    return filename;  
	}
}
