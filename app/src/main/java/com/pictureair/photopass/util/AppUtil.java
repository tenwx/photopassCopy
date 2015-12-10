package com.pictureair.photopass.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.core.CoordinateConvert;
import com.amap.api.location.core.GeoPoint;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.pictureair.photopass.R;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.smssdk.gui.EditTextWithClear;

/**
 * 公共类的方法
 * @author bauer_bao & talon
 *
 */
public class AppUtil {
	private final static String TAG = "AppUtil";
	
		/**
	 * 密码为空
	 */
	public static final int PWD_EMPTY = 3;
	/**
	 * 密码长度小于6位
	 */
	public static final int PWD_SHORT = 4;
	/**
	 * 密码两次输入不一致
	 */
	public static final int PWD_INCONSISTENCY = 5;
	/**
	 * 密码不能全部为空格
	 */
	public static final int PWD_ALL_SAPCE = 6;
	/**
	 * 密码可用
	 */
	public static final int PWD_AVAILABLE = 7;
	
	/**
	 * 密码首尾不能为空格
	 */
	public static final int PWD_HEAD_OR_FOOT_IS_SPACE = 8;

	
	
	/** 没有网络 */
	public static final int NETWORKTYPE_INVALID = 0;
	/** 流量网络，或统称为快速网络 */
	public static final int NETWORKTYPE_MOBILE = 1;
	/** wifi网络 */
	public static final int NETWORKTYPE_WIFI = 2;

	//当前网络类型
	public static int mNetWorkType;

	public static int getNetWorkType(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();  
		if (networkInfo != null && networkInfo.isConnected()) {  
			String type = networkInfo.getTypeName();  
			if (type.equalsIgnoreCase("WIFI")) {  
				mNetWorkType = NETWORKTYPE_WIFI;  
			} else if (type.equalsIgnoreCase("MOBILE")) {  
				mNetWorkType = NETWORKTYPE_MOBILE;
			}  
		} else {  
			mNetWorkType = NETWORKTYPE_INVALID;  
		}  
		return mNetWorkType;  
	} 
	/**
	 * 获取camera中最后的一张照片路径，用于显示预览
	 * @return
	 */
	public static String findLatestPic(){
		File file = new File(Common.PHOTO_SAVE_PATH);
		File[] files  = null;
		files = file.listFiles();
		long max = 0;
		String path = "";
		for (int i = 0; i < files.length; i++) {
			if(files[i].getName().endsWith(".JPG") && files[i].length() > 0){
				if(files[i].lastModified()>max){
					max = files[i].lastModified();
					path = files[i].getPath();
				}
			}
		}
		return path;
	}

	/**
	 * 角度与弧度的转换
	 * @param d 角度
	 * @return 弧度
	 */
	private static double rad(double d){  
		return d * Math.PI / 180.0;  
	} 

	/**
	 * 计算方位角pab
	 * @param lat_a
	 * @param lng_a
	 * @param lat_b
	 * @param lng_b
	 * @return
	 */
	public static double gps2d(double lat_a, double lng_a, double lat_b, double lng_b) {
		double d = 0;
		lat_a = rad(lat_a);
		lng_a = rad(lng_a);
		lat_b = rad(lat_b);
		lng_b = rad(lng_b);

		d = Math.sin(lat_a) * Math.sin(lat_b) + Math.cos(lat_a)
				* Math.cos(lat_b) * Math.cos(lng_b - lng_a);
		d = Math.sqrt(1 - d * d);
		d = Math.cos(lat_b) * Math.sin(lng_b - lng_a) / d;
		d = Math.asin(d) * 180 / Math.PI;
		return d;
	}

	/**
	 * 计算两个坐标点之间的距离
	 * @param lat_a
	 * @param lng_a
	 * @param lat_b
	 * @param lng_b
	 * @return
	 */
	//	public static double gps2m(double lat_a, double lng_a, double lat_b, double lng_b) {
	//		return DistanceUtil.getDistance(new LatLng(lat_a, lng_a), new LatLng(lat_b, lng_b));
	//	}

	/**
	 * 将GPS设备采集的原始GPS坐标转换成百度坐标 
	 * @param object json对象
	 * @throws JSONException 
	 * @throws NumberFormatException 
	 * @return 转换后的经纬度
	 */
	//	public static LatLng converterFromGPS2BD(JSONObject obj) throws NumberFormatException, JSONException{
	//		CoordinateConverter converter  = new CoordinateConverter();  
	//		converter.from(CoordType.GPS); 
	//		double lat = Double.valueOf(obj.getString("GPSLatitude"));
	//		double lng = Double.valueOf(obj.getString("GPSLongitude"));
	//		LatLng latLng = new LatLng(lat, lng);
	//		converter.coord(latLng);  //原始点
	//		return converter.convert();
	//	}

	/**
	 * 获取图片的角度，确定是否需要旋转。
	 * @param filepath filepath
	 */
	public static int getExifOrientation(String filepath) {
		int degree = 0;
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(filepath);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		if (exif != null) {
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
			if (orientation != -1) {
				switch(orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					degree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					degree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					degree = 270;
					break;
				}
			}
		}
		return degree;
	}

	/**
	 * 移动角度
	 * @param bitmap
	 * @return angle   角度，0 代表不旋转
	 */
	public static Bitmap rotaingImageView(int angle , Bitmap bitmap) {  
		//旋转图片 动作  
		Matrix matrix = new Matrix();;  
		matrix.postRotate(angle);  
		System.out.println("angle2=" + angle);  
		// 创建新的图片  
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,  
				bitmap.getWidth(), bitmap.getHeight(), matrix, true);  
		return resizedBitmap;  
	}

	/**
	 * 二维码生成函数
	 * @param str  需要生成二维码的字符串
	 * @return 返回bitmap
	 * @throws WriterException
	 */
	public static Bitmap Create2DCode(String str) throws WriterException {
		System.out.println("start create a new QRcode bitmap"+str);
		Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		//图像数据转换，使用了矩阵转换
		BitMatrix matrix = new QRCodeWriter().encode(str, BarcodeFormat.QR_CODE, 400, 400, hints);

		int width = matrix.getWidth();       
		int height = matrix.getHeight();       
		//二维矩阵转为一维像素数组,也就是一直横着排了       
		int[] pixels = new int[width * height];       
		for (int y = 0; y < height; y++) {       
			for (int x = 0; x < width; x++) {       
				if(matrix.get(x, y)){       
					pixels[y * width + x] = 0xff000000;       
				}       

			}       
		}       
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);       
		//通过像素数组生成bitmap,具体参考api    
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);       
		return bitmap;       
	}

	//获取ip地址，如果打开wifi的话，就可以获取到了
	public static String getHostIP(Context context) {
		String hostIP;
		//获取wifi服务  
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);  
		//判断wifi是否开启  
		if (!wifiManager.isWifiEnabled()) { //没有打开wifi的时候，获取gprs的ip地址 
			//wifiManager.setWifiEnabled(true); 
			hostIP = AppUtil.getLocalIpAddress();
		} else {//获取wifi的ip地址
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();       
			int ipAddress = wifiInfo.getIpAddress();   
			hostIP = intToIp(ipAddress); 
		} 
		return hostIP;
	}

	/**
	 * 转换为ip地址格式
	 * @param i 需要转换的IP
	 * @return
	 */
	private static String intToIp(int i) {       
		return (i & 0xFF ) + "." + ((i >> 8 ) & 0xFF) + "." +       
				((i >> 16 ) & 0xFF) + "." + ( i >> 24 & 0xFF) ;  
	} 

	/**
	 * 获取GPRS的IP地址
	 * @return
	 */
	private static String getLocalIpAddress()  
	{  
		try  
		{  
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)  
			{  
				NetworkInterface intf = en.nextElement();  
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)  
				{  
					InetAddress inetAddress = enumIpAddr.nextElement();  
					if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress()))  
					{  
						return inetAddress.getHostAddress().toString();  
					}  
				}  
			}  
		}  
		catch (SocketException ex)  
		{  
			Log.e(TAG, ex.toString());  
		}  
		return null;  
	}

	/**
	 * 获取外网IP
	 * @return
	 */
	public static String GetNetIp() {
		URL infoUrl = null;
		InputStream inStream = null;
		String ipLine = "";
		HttpURLConnection httpConnection = null;
		try {
			//        	infoUrl = new URL("http://ip168.com/");
			//        	infoUrl = new URL("http://www.ip138.com/");
			infoUrl = new URL("http://ip.cn/");
			//            infoUrl = new URL("http://iframe.ip138.com/ic.asp");
			URLConnection connection = infoUrl.openConnection();
			httpConnection = (HttpURLConnection) connection;
			int responseCode = httpConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				inStream = httpConnection.getInputStream();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inStream, "utf-8"));
				StringBuilder strber = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null)
					strber.append(line + "\n");
				// System.out.println("net ip before mathcer is "+ strber);
				//                Pattern pattern = Pattern
				//                        .compile("((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))");
				//                Matcher matcher = pattern.matcher(strber.toString());
				//                if (matcher.find()) {
				//                	System.out.println("------> net ip find");
				//                    ipLine = matcher.group();
				//                }
				//从反馈的结果中提取出IP地址  
				//                System.out.println("-------->"+strber);
				int start = strber.indexOf("<code>");  
				int end = strber.indexOf("</code>", start + 1);  
				ipLine = strber.substring(start + 6, end);  
				//                System.out.println("ipLine is "+ ipLine);
				//                return line;  
			}else {
				//				ipLine = "failed";
				System.out.println("net ip failed");
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (inStream != null) {

					inStream.close();
				}
				httpConnection.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ipLine;
	}


	/**
	 * 根据之前的日期转换成更加人性化的日期
	 * @param compareDate 需要转化的日期
	 * @param context
	 * @return 返回页面需要暂时的结果
	 * @throws ParseException
	 */
	public static String dateToSmartDate(String compareDate, Context context) throws ParseException{
		String result = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		//将需要比较的日期转成calendar
		Calendar d1 = new GregorianCalendar();
		d1.setTime(sdf.parse(compareDate));
		//获取今天的calendar
		Calendar d2 = new GregorianCalendar();
		d2.setTime(new Date());
		//得到相差多少天
		int days = d2.get(Calendar.DAY_OF_YEAR) - d1.get(Calendar.DAY_OF_YEAR);
		int y2 = d2.get(Calendar.YEAR);  
		//比较是否是同一年，如果不是，则加当年的实际天数，只需要比较一年就行了
		if (d1.get(Calendar.YEAR) != y2) {  
			days += d1.getActualMaximum(Calendar.DAY_OF_YEAR);  
		}
		Log.d(TAG, "d1:"+d1.get(Calendar.YEAR)+"d2:"+y2);
		//判断相差几天
		if (days == 0) {
			result = context.getString(R.string.today);
		}else if (days == 1) {
			result = context.getString(R.string.yesterday);
		}else if (days == 2) {
			result = context.getString(R.string.two_days_ago);
		}else if (days == 3) {
			result = context.getString(R.string.three_days_ago);
		}else if (days == 4) {
			result = context.getString(R.string.four_days_ago);
		}else if (days == 5) {
			result = context.getString(R.string.five_days_ago);
		}else if (days == 6) {
			result = context.getString(R.string.six_days_ago);
		}else {
			result = compareDate.replaceAll("-", "/");
			//需要判断是否刚刚过年，如果是，7天内依旧显示天数，超出7天，显示年份
			if (d1.get(Calendar.YEAR) == y2) {//不需要显示年份
				result = result.substring(5, 10);
			}
		}
		Log.d(TAG, compareDate+"----------->"+result);
		return result;
	}

	/**
	 * 根据距离转换成所需要的距离
	 * @param distance
	 * @return
	 */
	public static String getSmartDistance(double distance, NumberFormat distanceFormat){
		String result = null;
		if (distance < 1000) {//小于1km，直接取整显示
			result = (int)distance + "m";
		}else if (distance < 100000) {//小于100km，保留1位小数，取km为单位
			result = distanceFormat.format(distance/100/10.0) + "km";
		}else {
			result = ">100km";
		}
		return result;
	}

	/**
	 * 验证邮箱输入是否合法
	 *
	 * @param strEmail
	 * @return
	 */
	public static boolean isEmail(String strEmail) {
		// String strPattern =
		// "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		String strPattern = "^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";
		Pattern p = Pattern.compile(strPattern);
		Matcher m = p.matcher(strEmail);
		return m.matches();
	}

	/**
	 * 隐藏键盘
	 * @param v
	 * @param event
	 * @return
	 */
	public static boolean isShouldHideInput(View v, MotionEvent event) {  
		if (v != null && (v instanceof EditTextWithClear)) {
			int[] leftTop = { 0, 0 };  
			//获取输入框当前的location位置  
			v.getLocationInWindow(leftTop);  
			int left = leftTop[0];  
			int top = leftTop[1];  
			int bottom = top + v.getHeight();  
			int right = left + v.getWidth();  
			if (event.getX() > left && event.getX() < right  
					&& event.getY() > top && event.getY() < bottom) {  
				// 点击的是输入框区域，保留点击EditText的事件  
				return false;  
			} else {  
				return true;  
			}  
		}  
		return false;  
	} 

	/**
	 * 将时间转换成标准时间
	 * @param GTMDate
	 * @return
	 */
	public static String GTMToLocal(String GTMDate) {
		Log.d(TAG, "before translate = "+ GTMDate);
		int tIndex = GTMDate.indexOf("T");
		String dateTemp = GTMDate.substring(0, tIndex);
		String timeTemp = GTMDate.substring(tIndex + 1, GTMDate.length() - 6);
		String convertString = dateTemp + " " + timeTemp;

		SimpleDateFormat format;
		format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
		Date result_date;
		long result_time = 0;

		try {
			format.setTimeZone(TimeZone.getTimeZone("GMT00:00"));
			result_date = format.parse(convertString);
			result_time = result_date.getTime();
			format.setTimeZone(TimeZone.getDefault());
			return format.format(result_time);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return GTMDate;
	} 


	//生成二维码，不留空白区域
	private static final int BLACK = 0xff000000;
	private static final int PADDING_SIZE_MIN = 5; // 最小留白长度, 单位: px
	public static Bitmap createQRCode(String str, int widthAndHeight) throws WriterException {
		Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		BitMatrix matrix = new MultiFormatWriter().encode(str,
				BarcodeFormat.QR_CODE, widthAndHeight, widthAndHeight, hints);

		int width = matrix.getWidth();
		int height = matrix.getHeight();
		int[] pixels = new int[width * height];

		boolean isFirstBlackPoint = false;
		int startX = 0;
		int startY = 0;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (matrix.get(x, y)) {
					if (isFirstBlackPoint == false)
					{
						isFirstBlackPoint = true;
						startX = x;
						startY = y;
						Log.d("createQRCode", "x y = " + x + " " + y);
					}
					pixels[y * width + x] = BLACK;
				}
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

		// 剪切中间的二维码区域，减少padding区域
		if (startX <= PADDING_SIZE_MIN) return bitmap;

		int x1 = startX - PADDING_SIZE_MIN;
		int y1 = startY - PADDING_SIZE_MIN;
		if (x1 < 0 || y1 < 0) return bitmap;

		int w1 = width - x1 * 2;
		int h1 = height - y1 * 2;

		Bitmap bitmapQR = Bitmap.createBitmap(bitmap, x1, y1, w1, h1);

		return bitmapQR;
	}

	/**
	 * 获取当月的天数
	 * @param year
	 * @param month
	 * @return
	 */
	public static int getDay(int year, int month) {
		int day = 30;
		boolean flag = false;
		switch (year % 4) {
		case 0:
			flag = true;
			break;
		default:
			flag = false;
			break;
		}
		switch (month) {
		case 1:
		case 3:
		case 5:
		case 7:
		case 8:
		case 10:
		case 12:
			day = 31;
			break;
		case 2:
			day = flag ? 29 : 28;
			break;
		default:
			day = 30;
			break;
		}
		return day;
	}

	/**
	 * 计算两地距离 
	 * @param startLng 经度
	 * @param startLat 纬度
	 * @param endLng 经度
	 * @param endLat 纬度
	 * @return
	 */
	public static double getDistance(double startLng, double startLat, double endLng,
			double endLat) {
		LatLng start = new LatLng(startLat, startLng);
		LatLng end = new LatLng(endLat, endLng);
		return AMapUtils.calculateLineDistance(start, end);
	}

	/**
	 * 将GPS设备采集的原始GPS坐标转换成高德坐标 
	 * @param obj json对象
	 * @throws JSONException 
	 * @throws NumberFormatException 
	 * @return 转换后的经纬度
	 */
	public static LatLng converterFromGPS2AMAP(JSONObject obj) throws NumberFormatException, JSONException {
				double lat = Double.valueOf("31.1616667");//我的座位
				double lng = Double.valueOf("121.7083333");
//		double lat = Double.valueOf(obj.getString("GPSLatitude"));
//		double lng = Double.valueOf(obj.getString("GPSLongitude"));
		GeoPoint geoPoint = CoordinateConvert.fromGpsToAMap(lat, lng);
		LatLng latLng = new LatLng(geoPoint.getLatitudeE6()*1e-6, geoPoint.getLongitudeE6()*1e-6);

		System.out.println("latlng:"+lat+"___"+lng);
		System.out.println("latlng:"+Double.valueOf(obj.getString("GPSLatitude"))+"____"+Double.valueOf(obj.getString("GPSLongitude")));
		System.out.println("latlng:"+latLng.toString());
		return latLng;
	}
	
		/**
	 * 获取应用的版本号
	 * 
	 * @param context
	 */
	public static ArrayList<String> getDeviceInfos(Context context) {
		ArrayList<String> deviceInfo = new ArrayList<String>();
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(),
					0);
			deviceInfo.add(info.versionCode + "");
			deviceInfo.add(info.versionName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return deviceInfo;
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
//		return md5StrBuff.substring(8, 24).toString().toUpperCase();
		System.out.println("md5 password------->"+md5StrBuff.toString());
		//32位
		return md5StrBuff.toString();
	}

	/**
	 * 验证密码是否可用
	 * 
	 * @param pwd1
	 *            第一次输入的密码
	 * @param pwd2
	 *            第二次输入的密码
	 * @return
	 */
	public static final int checkPwd(String pwd1, String pwd2) {
		if (pwd1.isEmpty() || pwd2.isEmpty()) {// 密码为空
			return PWD_EMPTY;
		} else if (pwd1.length() < 6 || pwd2.length() < 6) {// 密码小于6位
			return PWD_SHORT;
		} else if (!pwd1.equals(pwd2)) {// 密码两次不一致
			return PWD_INCONSISTENCY;
		} else if (!pwd1.isEmpty() && pwd1.trim().isEmpty()) {// 密码全部为空格
			return PWD_ALL_SAPCE;
		} else if (pwd1.trim().length() < pwd1.length()) {// 密码首尾有空格
			return PWD_HEAD_OR_FOOT_IS_SPACE;
		} else {// 密码可用
			return PWD_AVAILABLE;
		}
	}

}
