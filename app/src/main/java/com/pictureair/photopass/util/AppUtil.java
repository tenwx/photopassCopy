package com.pictureair.photopass.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.model.LatLng;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.pictureair.photopass.R;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.DiscoverLocationItemInfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.entity.PhotoItemInfo;
import com.pictureair.photopass.widget.EditTextWithClear;

import net.sqlcipher.Cursor;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 公共类的方法
 *
 * @author bauer_bao & talon
 */
public class AppUtil {
    private final static String TAG = "AppUtil";

    /**
     * 没有网络
     */
    public static final int NETWORKTYPE_INVALID = 0;
    /**
     * 流量网络，或统称为快速网络
     */
    public static final int NETWORKTYPE_MOBILE = 1;
    /**
     * wifi网络
     */
    public static final int NETWORKTYPE_WIFI = 2;

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

    /**
     * 水平留白
     */
    public static final int HORIZONTAL_MARGIN = 9;

    /**
     * 垂直留白
     */
    public static final int VERTICAL_MARGIN = 10;

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
     *  检查电话号码是否符合规则,true为符合正确格式，false为不符合手机格式
     */
    public static boolean checkPhoneNumber(String phoneStr) {
        boolean tem = true;
//      Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Pattern p = Pattern.compile("^1(3|5|7|8)\\d{9}");
        Matcher matcher = p.matcher(phoneStr);
        tem = matcher.matches();

        return tem;
    }
    /**
     * 获取camera中最后的一张照片路径，用于显示预览
     *
     * @return
     */
    public static String findLatestPic() {
        File file = new File(Common.PHOTO_SAVE_PATH);
        File[] files = null;
        files = file.listFiles();
        long max = 0;
        String path = "";
        if (files == null || files.length <= 0) {
            return path;
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().endsWith(".JPG") && files[i].length() > 0) {
                if (files[i].lastModified() > max) {
                    max = files[i].lastModified();
                    path = files[i].getPath();
                }
            }
        }
        return path;
    }

    /**
     * 角度与弧度的转换
     *
     * @param d 角度
     * @return 弧度
     */
    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * 计算方位角pab
     *
     * @param lat_tar 目标地点
     * @param lng_tar
     * @param lat_cur 当前地点
     * @param lng_cur
     * @return
     */
    public static double gps2d(double lat_tar, double lng_tar, double lat_cur, double lng_cur) {
        double d;
        lat_tar = rad(lat_tar);
        lng_tar = rad(lng_tar);
        lat_cur = rad(lat_cur);
        lng_cur = rad(lng_cur);

        d = Math.sin(lat_tar) * Math.sin(lat_cur) + Math.cos(lat_tar)
                * Math.cos(lat_cur) * Math.cos(lng_tar - lng_cur);
        d = Math.sqrt(1 - d * d);
        if (d != 0) {
            d = Math.cos(lat_cur) * Math.sin(lng_tar - lng_cur) / d;
            d = Math.asin(d) * 180 / Math.PI;
        }
        if (lat_tar >= lat_cur) {//第一二象限，不需要做任何处理

        } else {
            if (lng_tar >= lng_cur) {//第四象限，测试通过
                d = 180 - d;
            } else {//第三象限，测试通过
                d = 180 - d;
            }
        }
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
     *
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
                switch (orientation) {
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
     *
     * @param bitmap
     * @return angle   角度，0 代表不旋转
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        ;
        matrix.postRotate(angle);
        PictureAirLog.out("angle2=" + angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    /**
     * 二维码生成函数
     *
     * @param str 需要生成二维码的字符串
     * @return 返回bitmap
     * @throws WriterException
     */
    public static Bitmap Create2DCode(String str) throws WriterException {
        PictureAirLog.out("start create a new QRcode bitmap" + str);
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
                if (matrix.get(x, y)) {
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
     *
     * @param i 需要转换的IP
     * @return
     */
    private static String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
    }

    /**
     * 获取GPRS的IP地址
     *
     * @return
     */
    private static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            PictureAirLog.e(TAG, ex.toString());
        }
        return null;
    }

    /**
     * 获取外网IP
     *
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
                // PictureAirLog.out("net ip before mathcer is "+ strber);
                //                Pattern pattern = Pattern
                //                        .compile("((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))");
                //                Matcher matcher = pattern.matcher(strber.toString());
                //                if (matcher.find()) {
                //                	PictureAirLog.out("------> net ip find");
                //                    ipLine = matcher.group();
                //                }
                //从反馈的结果中提取出IP地址
                //                PictureAirLog.out("-------->"+strber);
                int start = strber.indexOf("<code>");
                int end = strber.indexOf("</code>", start + 1);
                ipLine = strber.substring(start + 6, end);
                //                PictureAirLog.out("ipLine is "+ ipLine);
                //                return line;
            } else {
                //				ipLine = "failed";
                PictureAirLog.out("net ip failed");
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
                if (null != httpConnection)
                    httpConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ipLine;
    }


    /**
     * 根据之前的日期转换成更加人性化的日期
     *
     * @param compareDate 需要转化的日期，yyyy-mm-dd hh:mm:ss
     * @param context
     * @return 返回页面需要暂时的结果
     * @throws ParseException
     */
    public static String dateToSmartDate(String compareDate, Context context) throws ParseException {
        String date = compareDate.substring(0, 10);
        String time = compareDate.substring(11, 19);
        String result = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //将需要比较的日期转成calendar
        Calendar d1 = new GregorianCalendar();
        d1.setTime(sdf.parse(date));
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
//        PictureAirLog.d(TAG, "d1:" + d1.get(Calendar.YEAR) + "d2:" + y2);
        //判断相差几天
        if (days == 0) {//当天的，只需要显示hh:mm
            result = time.substring(0, 5);
        } else if (days == 1) {
            result = context.getString(R.string.yesterday);
        } else if (days == 2) {
            result = context.getString(R.string.two_days_ago);
        } else if (days == 3) {
            result = context.getString(R.string.three_days_ago);
        } else if (days == 4) {
            result = context.getString(R.string.four_days_ago);
        } else if (days == 5) {
            result = context.getString(R.string.five_days_ago);
        } else if (days == 6) {
            result = context.getString(R.string.six_days_ago);
        } else {
            result = date.replaceAll("-", "/");
            //需要判断是否刚刚过年，如果是，7天内依旧显示天数，超出7天，显示年份
            if (d1.get(Calendar.YEAR) == y2) {//不需要显示年份
                result = result.substring(5, 10);
            }
        }
//        PictureAirLog.d(TAG, date + "----------->" + result);
        return result;
    }

    /**
     * 根据距离转换成所需要的距离
     *
     * @param distance
     * @return
     */
    public static String getSmartDistance(double distance, NumberFormat distanceFormat) {
        String result = null;
        if (distance < 1000) {//小于1km，直接取整显示
            result = (int) distance + "m";
        } else if (distance < 100000) {//小于100km，保留1位小数，取km为单位
            result = distanceFormat.format(distance / 100 / 10.0) + "km";
        } else {
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
        String strPattern = "^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";
        Pattern p = Pattern.compile(strPattern);
        Matcher m = p.matcher(strEmail);
        return m.matches();
    }

    /**
     * 判断是否是全部数字
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /**
     * 判断是否是字母或者数字
     */
    public static boolean isNumOrLetters(String str) {
        String regEx = "^[A-Za-z0-9_]+$";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * 隐藏键盘
     *
     * @param v
     * @param event
     * @return
     */
    public static boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditTextWithClear)) {
            int[] leftTop = {0, 0};
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
     *
     * @param GTMDate
     * @return
     */
    public static String GTMToLocal(String GTMDate) {
//        PictureAirLog.d(TAG, "before translate = " + GTMDate);
        int tIndex = GTMDate.indexOf("T");
        String dateTemp = GTMDate.substring(0, tIndex);
        String timeTemp = GTMDate.substring(tIndex + 1, GTMDate.length() - 5);
        String convertString = dateTemp + " " + timeTemp;

        SimpleDateFormat format;
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        Date result_date;
        long result_time = 0;

        try {
            format.setTimeZone(TimeZone.getTimeZone("GMT00:00"));//设置0时区
            result_date = format.parse(convertString);
            result_time = result_date.getTime();//得到0时区的毫秒数
            format.setTimeZone(TimeZone.getDefault());//设置当前时区
            return format.format(result_time);//转换成当前时区的时间
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
                    if (isFirstBlackPoint == false) {
                        isFirstBlackPoint = true;
                        startX = x;
                        startY = y;
                        PictureAirLog.d("createQRCode", "x y = " + x + " " + y);
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
     *
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
     *
     * @param startLng 经度
     * @param startLat 纬度
     * @param endLng   经度
     * @param endLat   纬度
     * @return
     */
    public static double getDistance(double startLng, double startLat, double endLng,
                                     double endLat) {
        LatLng start = new LatLng(startLat, startLng);
        LatLng end = new LatLng(endLat, endLng);
        return AMapUtils.calculateLineDistance(start, end);
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
            PictureAirLog.out("NoSuchAlgorithmException caught!");
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
        PictureAirLog.out("md5 result------->" + md5StrBuff.toString());
        //32位
        return md5StrBuff.toString();
    }

    /**
     * 验证密码是否可用,必须按照此顺序
     *
     * @param pwd1 第一次输入的密码
     * @param pwd2 第二次输入的密码
     * @return
     */
    public static final int checkPwd(String pwd1, String pwd2) {
        if (pwd1.isEmpty()) {// 密码为空
            return PWD_EMPTY;
        } else if (!pwd1.isEmpty() && pwd1.trim().isEmpty()) {// 密码全部为空格
            return PWD_ALL_SAPCE;
        } else if (pwd1.trim().length() < pwd1.length()) {// 密码首尾有空格
            return PWD_HEAD_OR_FOOT_IS_SPACE;
        } else if (pwd1.length() < 6) {// 密码小于6位
            return PWD_SHORT;
        } else if (!pwd1.equals(pwd2)) {// 密码两次不一致
            return PWD_INCONSISTENCY;
        } else {// 密码可用
            return PWD_AVAILABLE;
        }
    }

    /**
     * 将photoItemInfo转成悬浮所需要的photoInfo的列表
     *
     * @param list
     * @return
     */
    public static ArrayList<PhotoInfo> startSortForPinnedListView(ArrayList<PhotoItemInfo> list) {
        ArrayList<PhotoInfo> tempInfos = new ArrayList<PhotoInfo>();
        PhotoInfo temp, temp2;
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.get(i).list.size(); j++) {
                temp2 = list.get(i).list.get(j);
                temp = new PhotoInfo();
                temp.sectionId = i;
                temp.photoId = temp2.photoId;
                temp.photoPathOrURL = temp2.photoPathOrURL;
                temp.photoThumbnail = temp2.photoThumbnail;
                temp.photoThumbnail_512 = temp2.photoThumbnail_512;
                temp.photoThumbnail_1024 = temp2.photoThumbnail_1024;
                temp.photoPassCode = temp2.photoPassCode;
                temp.isPayed = temp2.isPayed;
                temp.isChecked = temp2.isChecked;
                temp.isSelected = temp2.isSelected;
                temp.showMask = temp2.showMask;
                temp.lastModify = temp2.lastModify;
                temp.shootOn = temp2.shootOn;
                temp.shootTime = temp2.shootTime;
                temp.locationName = temp2.locationName;
                temp.onLine = temp2.onLine;
                temp.locationCountry = temp2.locationCountry;
                temp.isUploaded = temp2.isUploaded;
                temp.locationId = temp2.locationId;
                temp.isLove = temp2.isLove;
                temp.shareURL = temp2.shareURL;
                temp.isVideo = temp2.isVideo;
                temp.fileSize = temp2.fileSize;
                temp.videoWidth = temp2.videoWidth;
                temp.videoHeight = temp2.videoHeight;
                temp.isHasPreset = temp2.isHasPreset;
                temp.isEncrypted = temp2.isEncrypted;
                temp.isRefreshInfo = temp2.isRefreshInfo;
                temp.adURL = temp2.adURL;
                tempInfos.add(temp);
            }
        }
        return tempInfos;
    }

    /**
     * 按照地点快速排序
     *
     * @param list
     * @return
     */
    public static ArrayList<PhotoInfo> insertSortFavouritePhotos(ArrayList<PhotoInfo> list) {
        PictureAirLog.d("insert sort", list.size() + "");
        ArrayList<PhotoInfo> resultArrayList = new ArrayList<>();
        boolean findPosition = false;
        for (int i = 0; i < list.size(); i++) {
            if (resultArrayList.size() > 1) {//从第三个开始插入
                for (int j = 0; j < resultArrayList.size() - 1; j++) {//循环已排序好的列表
                    if (resultArrayList.get(j).locationName.equals(list.get(i).locationName) &&
                            resultArrayList.get(j).shootTime.equals(list.get(i).shootTime)) {
                        if (resultArrayList.get(j + 1).locationName.equals(list.get(i).locationName) &&
                                resultArrayList.get(j + 1).shootTime.equals(list.get(i).shootTime)) {

                        } else {
                            findPosition = true;
                            list.get(i).sectionId = resultArrayList.get(j).sectionId;
                            resultArrayList.add(j + 1, list.get(i));
                            break;
                        }
                    }
                }

                if (findPosition) {//找到
                    findPosition = false;
                } else {//没有找到，直接放在后面
                    if (resultArrayList.get(i - 1).shootTime.equals(list.get(i).shootTime) &&
                            resultArrayList.get(i - 1).locationName.equals(list.get(i).locationName)) {
                        list.get(i).sectionId = resultArrayList.get(i - 1).sectionId;
                    } else {
                        list.get(i).sectionId = resultArrayList.get(i - 1).sectionId + 1;
                    }
                    resultArrayList.add(list.get(i));
                }

            } else {//小于3个的时候
                if (i == 0) {
                    list.get(i).sectionId = 0;
                } else if (i == 1) {
                    if (resultArrayList.get(0).locationName.equals(list.get(i).locationName)
                            && resultArrayList.get(0).shootTime.equals(list.get(i).shootTime)) {
                        list.get(i).sectionId = resultArrayList.get(0).sectionId;
                    } else {
                        list.get(i).sectionId = resultArrayList.get(0).sectionId + 1;
                    }
                }
                resultArrayList.add(list.get(i));
            }
        }
        PictureAirLog.d("insert sort", resultArrayList.size() + "");
        return resultArrayList;
    }


    /**
     * 获取视频的缩略图
     *
     * @param url
     * @param width
     * @param height
     * @return
     */
    public static Bitmap createVideoThumbnail(String url, int width, int height) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        int kind = MediaStore.Video.Thumbnails.MINI_KIND;
        try {
            if (Build.VERSION.SDK_INT >= 14) {
                retriever.setDataSource(url, new HashMap<String, String>());
            } else {
                retriever.setDataSource(url);
            }
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        if (kind == MediaStore.Images.Thumbnails.MICRO_KIND && bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }


    /**
     * 字符串转日期
     *
     * @param strDate 2015-12-24 13:37:12
     * @return
     */
    public static Date getDateFromStr(String strDate) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return df.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 字符串转日期
     *
     * @param strDate 2015-12-24
     * @return
     */
    public static Date getDateFromStr1(String strDate) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return df.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取两个日期之间的间隔天数
     *
     * @return
     */
    public static int getGapCount(String startDate, String endDate) {
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(getDateFromStr1(startDate));
        fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        fromCalendar.set(Calendar.MILLISECOND, 0);

        Calendar toCalendar = Calendar.getInstance();
        toCalendar.setTime(getDateFromStr1(endDate));
        toCalendar.set(Calendar.HOUR_OF_DAY, 0);
        toCalendar.set(Calendar.MINUTE, 0);
        toCalendar.set(Calendar.SECOND, 0);
        toCalendar.set(Calendar.MILLISECOND, 0);

        int result = (int) ((toCalendar.getTime().getTime() - fromCalendar.getTime().getTime()) / (1000 * 60 * 60 * 24));
        return Math.abs(result);
    }

    /**
     * 清除转义字符
     *
     * @param sContent
     * @return
     */
    public static String ReplaceString(String sContent) {
        if (sContent == null) {
            return sContent;
        }
        if (sContent.contains("\\")) {
            sContent = sContent.replace("\\", "");
        }
        if (sContent.contains("\'")) {
            sContent = sContent.replace("\'", "");
        }
        if (sContent.contains("\"")) {
            sContent = sContent.replace("\"", "");
        }
        //去掉字符串的回车换行符
//        sContent = sContent.replace(sContent, "[\n\r]", "");
        sContent = sContent.trim();
        return sContent;

    }

    /**
     * 扩大View的触摸和点击响应范围,最大不超过其父View范围
     *
     * @param view
     * @param top
     * @param bottom
     * @param left
     * @param right
     */
    public static void expandViewTouchDelegate(final View view, final int top,
                                         final int bottom, final int left, final int right) {

        ((View) view.getParent()).post(new Runnable() {
            @Override
            public void run() {
                Rect bounds = new Rect();
                view.setEnabled(true);
                view.getHitRect(bounds);

                bounds.top -= top;
                bounds.bottom += bottom;
                bounds.left -= left;
                bounds.right += right;

                TouchDelegate touchDelegate = new TouchDelegate(bounds, view);

                if (View.class.isInstance(view.getParent())) {
                    ((View) view.getParent()).setTouchDelegate(touchDelegate);
                }
            }
        });
    }

    /**
     * 检查手动输入框是否处于编辑状态
     * @param codeCount 当前code长度的数组
     * @param cursorIndex 当前光标位置
     * @param inputIndex 当前输入的位置，从0开始计算
     * @return
     */
    public static boolean isInputCodeEditing(int[] codeCount, int cursorIndex, int inputIndex){
        int codeAllCount = 0;
        /**
         * 先判断前面的格子是否都已经满了
         */
        for (int i = 0; i < inputIndex; i++) {
            if (codeCount[i] != 4){//不满，则为编辑状态
                PictureAirLog.out("edit");
                return true;
            }
        }

        for (int i = 0; i < 4; i++) {
            codeAllCount += codeCount[i];
        }

        return cursorIndex + inputIndex * 4 != codeAllCount;//判断光标的位置和当前字符数一否一样
    }

    /**
     * 判断光标是否该自动切换
     * @param cursorIndex 当前光标位置
     * @param inputIndex 当前输入的位置，从0开始计算
     * @return 1往后切换，-1往前切换，0不切换
     */
    public static int inputCodeEditJump(int cursorIndex, int inputIndex) {
        if (cursorIndex == 4) {//在当前输入框的最后面
            if (inputIndex == 3) {//已经是最后一个，不需要移动
                return 0;
            } else {//需要往后移动
                return 1;
            }
        } else if (cursorIndex == 0) {//在当前输入框的最前面
            if (inputIndex == 0) {//已经是第一个输入框，不需要往前移动
                return 0;
            } else {//需要往前移动
                return -1;
            }
        } else {//不移动
            return 0;
        }
    }

    /**
     * 将简码转换成 国家名称
     */
    public static String getCountryByCountryCode(String countryCode,
                                                 Context context) {
        String[] codeStrings;
        String country = null;
        // 国家名称集合
        //		Map<String, String> countryMap = new HashMap<String, String>();
        /** 读取国家简码 */
        codeStrings = context.getResources().getStringArray(R.array.smssdk_country);
        if (null != codeStrings) {

            for (int i = 0; i < codeStrings.length; i++) {
                String bb[] = codeStrings[i].split(",");
                //bb[0]:国家
                //bb[1]:简码
                //				countryMap.put(bb[1].trim(), bb[0].trim());
                if (countryCode.trim().equals(bb[4].trim())) {
                    country = bb[0].trim();
                    break;
                }
            }
        }
        //		country = countryMap.get(countryCode.trim());
        return country;
    }


    /**
     * 计算文件md5
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    public static String getMd5ByFile(File file) throws FileNotFoundException {
        String value = null;
        FileInputStream in = new FileInputStream(file);
        MappedByteBuffer byteBuffer = null;
        MessageDigest md5 = null;
        BigInteger bi = null;
        try {
            byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            bi = new BigInteger(1, md5.digest());
            value = bi.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != in) {
                    in.close();
                }
                if (null != byteBuffer) {
                    byteBuffer.clear();
                    byteBuffer = null;
                }
                if (null != md5) {
                    md5 = null;
                }
                if (null != bi) {
                    bi = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return value;
    }

    /**
     * str过滤，验证输入str，不允许特殊字符
     * @param str
     */
    public static String inputTextFilter(String str) throws PatternSyntaxException {
        String regEx = "[/\\:*?<>|\"\n\t]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("");
    }

    /**
     * 获取manifest中meta值
     * @param context
     * @return
     * @throws PackageManager.NameNotFoundException
     */
    public static String getMetaData(Context context, String key){
        String result = "";
        try {
            if (context != null) {
                PackageManager packageManager = context.getPackageManager();
                if (packageManager != null) {
                    ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                    if (applicationInfo != null) {
                        if (applicationInfo.metaData != null) {
                            result = applicationInfo.metaData.getString(key);
                        }
                    }
                }
            }
            PictureAirLog.out("channel---->" + result);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 判断是否有sd卡
     * @return
     */
    public static boolean hasSDCard() {
        return Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    /**
     * 扫描本地文件夹
     * @param context
     * @param filePath
     * @param albumName
     * @return
     */
    public static ArrayList<PhotoInfo> getLocalPhotos(Context context, String filePath, String albumName) {
        PictureAirLog.out("---------->scan" + albumName);
        ArrayList<PhotoInfo> resultList = new ArrayList<>();
        PhotoInfo selectPhotoItemInfo;
        if (!hasSDCard()) {//如果SD卡不存在
            return resultList;
        }
        PictureAirLog.out("path---->" + filePath);
        filePath = filePath.substring(0, filePath.length() - 1);
        PictureAirLog.out("path---->" + filePath);
        File file = new File(filePath);
        if (!file.exists()) {//如果文件不存在，创建文件夹
            file.mkdirs();
            return  resultList;
        }
        File[] files = file.listFiles();
        Date date;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (null != files) {

            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().endsWith(".JPG") || files[i].getName().endsWith(".jpg")) {
                    if (files[i].length() > 0) {//扫描到文件
                        selectPhotoItemInfo = new PhotoInfo();
                        selectPhotoItemInfo.photoPathOrURL = files[i].getPath();
                        selectPhotoItemInfo.lastModify = files[i].lastModified();
                        date = new Date(selectPhotoItemInfo.lastModify);
                        selectPhotoItemInfo.shootOn = sdf.format(date);
                        selectPhotoItemInfo.shootTime = selectPhotoItemInfo.shootOn.substring(0, 10);
                        selectPhotoItemInfo.isChecked = 0;
                        selectPhotoItemInfo.isSelected = 0;
                        selectPhotoItemInfo.showMask = 0;
                        selectPhotoItemInfo.locationName = context.getString(R.string.story_tab_magic);
                        selectPhotoItemInfo.isPayed = 1;
                        selectPhotoItemInfo.onLine = 0;
                        selectPhotoItemInfo.isVideo = 0;
                        selectPhotoItemInfo.isHasPreset = 0;
                        selectPhotoItemInfo.isEncrypted = 0;
                        selectPhotoItemInfo.isRefreshInfo = 0;
                        resultList.add(selectPhotoItemInfo);
                        PictureAirLog.out("magic url =========>" + selectPhotoItemInfo.photoPathOrURL);
                    }
                }
            }
        }
        return resultList;
    }

    /**
     * 获取预览图片需要的图片列表
     * @param locationList
     * @param pictureAirDbManager
     * @param deleteTime
     * @param sdf
     * @param language
     * @return
     * @throws ParseException
     */
    public static ArrayList<PhotoInfo> getSortedPhotoPassPhotos(ArrayList<DiscoverLocationItemInfo> locationList,
                                                                PictureAirDbManager pictureAirDbManager, String deleteTime,
                                                                SimpleDateFormat sdf, String language, boolean isBought) throws ParseException {
        //从数据库获取图片
        ArrayList<PhotoInfo> photoList;
        if (isBought) {//获取已经购买的pp照片
            photoList = pictureAirDbManager.getPhotoFromPhotoPassInfo(deleteTime, true);
        } else {//获取全部照片
            photoList = pictureAirDbManager.getAllPhotoFromPhotoPassInfo(false, deleteTime);
        }
        //将图片按照地点组合
        ArrayList<PhotoItemInfo> photoItemInfoArrayList = getPhotoItemInfoList(locationList, photoList, sdf, language);
        //将新的数据进行排序
        Collections.sort(photoItemInfoArrayList);
        return AppUtil.startSortForPinnedListView(photoItemInfoArrayList);
    }

    /**
     * 获取预览页面的所有排序好的图片列表
     * @param context
     * @param locationList
     * @param targetList
     * @param pictureAirDbManager
     * @param deleteTime
     * @param sdf
     * @param language
     * @return
     * @throws ParseException
     */
    public static ArrayList<PhotoInfo> getSortedAllPhotos(Context context, ArrayList<DiscoverLocationItemInfo> locationList,
                                                          ArrayList<PhotoInfo> targetList, PictureAirDbManager pictureAirDbManager, String deleteTime,
                                                          SimpleDateFormat sdf, String language) throws ParseException {
        //从数据库获取图片
        ArrayList<PhotoInfo> photoList = pictureAirDbManager.getAllPhotoFromPhotoPassInfo(false, deleteTime);
        //将图片按照地点组合
        ArrayList<PhotoItemInfo> photoItemInfoArrayList = getPhotoItemInfoList(locationList, photoList, sdf, language);
        //将本地图片按照地点组合
        ArrayList<PhotoItemInfo> magicPhotoItemInfoArrayList = getMagicItemInfoList(context, sdf, targetList);

        ArrayList<PhotoItemInfo> allPhotoItemInfoArrayList = new ArrayList<>();
        //将组合后的列表连接
        allPhotoItemInfoArrayList.addAll(photoItemInfoArrayList);
        allPhotoItemInfoArrayList.addAll(magicPhotoItemInfoArrayList);

        //将新的数据进行排序
        Collections.sort(allPhotoItemInfoArrayList);
        return AppUtil.startSortForPinnedListView(allPhotoItemInfoArrayList);
    }

    /**
     * 遍历所有magic图片信息
     * 1.判断现有列表是否已经存在
     * 2.如果存在，直接添加在item列表后面，并且将shootOn的值更新为最大的
     * 3.如果不存在，新建item
     *
     * @throws ParseException
     */
    public static ArrayList<PhotoItemInfo> getMagicItemInfoList(Context context, SimpleDateFormat sdf, ArrayList<PhotoInfo> targetMagicPhotoList) throws ParseException {
        ArrayList<PhotoItemInfo> magicItemInfoList = new ArrayList<>();
        PictureAirLog.d(TAG, "----------->get magic photos" + targetMagicPhotoList.size() + "____" + magicItemInfoList.size());
        PhotoItemInfo photoItemInfo;
        boolean clone_contains = false;
        Date date1;
        Date date2;
        for (int i = 0; i < targetMagicPhotoList.size(); i++) {
            PictureAirLog.out("photo shoot time is " + targetMagicPhotoList.get(i).shootOn);
            for (int j = 0; j < magicItemInfoList.size(); j++) {
                if (targetMagicPhotoList.get(i).shootTime.equals(magicItemInfoList.get(j).shootTime)) {
                    magicItemInfoList.get(j).list.add(targetMagicPhotoList.get(i));
                    date1 = sdf.parse(targetMagicPhotoList.get(i).shootOn);
                    date2 = sdf.parse(magicItemInfoList.get(j).shootOn);
                    if (date1.after(date2)) {
                        magicItemInfoList.get(j).shootOn = targetMagicPhotoList.get(i).shootOn;
                    }
                    clone_contains = true;
                    break;
                }
            }
            //判断是否需要new
            if (!clone_contains) {//如果之前没有找到，说明需要new
                photoItemInfo = new PhotoItemInfo();
                PictureAirLog.out("shootTime:" + targetMagicPhotoList.get(i).shootTime);
                photoItemInfo.shootTime = targetMagicPhotoList.get(i).shootTime;
                photoItemInfo.place = context.getString(R.string.story_tab_magic);
                photoItemInfo.list.add(targetMagicPhotoList.get(i));
                photoItemInfo.shootOn = targetMagicPhotoList.get(i).shootOn;
                magicItemInfoList.add(photoItemInfo);
            } else {
                clone_contains = false;
            }
        }
        return magicItemInfoList;
    }

    /**
     * 将图片按照地点重新组合
     * @param locationList
     * @param photoList
     * @param sdf
     * @param language
     * @return
     * @throws ParseException
     */
    public static ArrayList<PhotoItemInfo> getPhotoItemInfoList(ArrayList<DiscoverLocationItemInfo> locationList, ArrayList<PhotoInfo> photoList,
                                                                SimpleDateFormat sdf, String language) throws ParseException {
        ArrayList<PhotoItemInfo> photoPassItemInfoList = new ArrayList<>();
        //遍历所有photopass信息
        PhotoItemInfo photoItemInfo;
        boolean clone_contains = false;
        Date date1;
        Date date2;
        //处理网络图片
        for (int l = 0; l < photoList.size(); l++) {
            PhotoInfo info = photoList.get(l);
            int resultPosition = findPositionInLocationList(info, locationList);
            if (resultPosition == -1) {//如果没有找到，说明是其他地点的照片
                resultPosition = locationList.size() - 1;
                info.locationId = "others";
            }
            if (resultPosition < 0 ) {
                resultPosition = 0;
            }
            //					PictureAirLog.d(TAG, "find the location");
            //如果locationid一样，需要判断是否已经存在此item，如果有，在按照时间分类，没有，新建一个item
            for (int j = 0; j < photoPassItemInfoList.size(); j++) {
                //						PictureAirLog.d(TAG, "weather already exists:"+j);
                if (info.shootTime.equals(photoPassItemInfoList.get(j).shootTime)
                        && (info.locationId.equals(photoPassItemInfoList.get(j).locationId) || photoPassItemInfoList.get(j).locationIds.contains(info.locationId))) {
                    info.locationName = photoPassItemInfoList.get(j).place;
                    photoPassItemInfoList.get(j).list.add(info);
                    date1 = sdf.parse(info.shootOn);
                    date2 = sdf.parse(photoPassItemInfoList.get(j).shootOn);
                    if (date1.after(date2)) {
                        photoPassItemInfoList.get(j).shootOn = info.shootOn;
                    }
                    clone_contains = true;
                    break;
                }
            }
            if (!clone_contains && locationList.size() > 0) {
                //初始化item的信息
                photoItemInfo = new PhotoItemInfo();
                photoItemInfo.locationId = locationList.get(resultPosition).locationId;
//                if (isOther) {
//                    photoItemInfo.locationIds = locationList.get(resultPosition).locationIds.toString() + info.locationId;
//                } else {
                    photoItemInfo.locationIds = locationList.get(resultPosition).locationIds.toString();
//                }
                photoItemInfo.shootTime = info.shootTime;
                if (language.equals(Common.SIMPLE_CHINESE)) {
                    photoItemInfo.place = locationList.get(resultPosition).placeCHName;
                    info.locationName = locationList.get(resultPosition).placeCHName;

                } else {
                    photoItemInfo.place = locationList.get(resultPosition).placeENName;
                    info.locationName = locationList.get(resultPosition).placeENName;

                }
                photoItemInfo.list.add(info);
                photoItemInfo.placeUrl = locationList.get(resultPosition).placeUrl;
                photoItemInfo.latitude = locationList.get(resultPosition).latitude;
                photoItemInfo.longitude = locationList.get(resultPosition).longitude;
                photoItemInfo.islove = 0;
                photoItemInfo.shootOn = info.shootOn;
                photoPassItemInfoList.add(photoItemInfo);
            } else {
                clone_contains = false;
            }
        }
        return photoPassItemInfoList;
    }

    /**
     * 获取地点列表
     * @param locationJson
     * @param showPhoto true显示照片，需要手动添加一个地点,false显示列表，不需要手动添加地点
     * @return
     */
    public static ArrayList<DiscoverLocationItemInfo> getLocation(Context context, String locationJson, boolean showPhoto) {
        ArrayList<DiscoverLocationItemInfo> result = new ArrayList<>();
        DiscoverLocationItemInfo locationInfo;
        try {
            JSONObject response = JSONObject.parseObject(locationJson);
            if (response != null) {
                JSONArray resultArray = response.getJSONArray("locations");
                for (int i = 0; i < resultArray.size(); i++) {
                    JSONObject object = resultArray.getJSONObject(i);
                    locationInfo = JsonUtil.getLocation(object);
                    if (!showPhoto) {//不需要显示照片
                        if (locationInfo.isShow == 1) {
                            result.add(locationInfo);
                        }
                    } else {
                        result.add(locationInfo);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (showPhoto) {//显示照片，需要手动添加一个点
            locationInfo = new DiscoverLocationItemInfo();
            locationInfo.locationId = "others";
            locationInfo.locationIds = "others";
            locationInfo.isShow = 0;

            locationInfo.placeCHName = context.getResources().getString(R.string.story_other_ch);
            locationInfo.placeENName = context.getResources().getString(R.string.story_other_en);
            locationInfo.placeUrl = "";
            locationInfo.latitude = 0;
            locationInfo.longitude = 0;
            locationInfo.placeDetailCHIntroduce = "";
            locationInfo.placeDetailENIntroduce = "";
            locationInfo.popularity = "";
            locationInfo.islove = 0;
            locationInfo.showDetail = 0;
            result.add(locationInfo);
        }
        return result;
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    public static Map<String, String> collectDeviceInfo(Context ctx) {
        Map<String, String> infos = new HashMap<String, String>();
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            PictureAirLog.e(TAG, "an error occured when collect package info" + e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                PictureAirLog.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                PictureAirLog.e(TAG, "an error occured when collect crash info" + e);
            }
        }
        return infos;
    }

    /**
     * 检查手机权限
     *
     * @param permission 权限
     * */
    public static boolean checkPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }else if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }else{
            return true;
        }
    }

    /**
     * 得到url中正确的文件名,根据 isVideo 获取是视频还是图片
     * 如果没有后缀名，说明是加密过的数据，文件名比较长，需要处理下，目前采用md5，将长度转成32位
     * @param url 原始url
     * @param isVideo 判断是照片还是视频，0 代表是照片。
     * @return 文件名
     */
    public static String getReallyFileName(String url,int isVideo) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        String filename = url;
        if (isVideo == 0){
            if (url.endsWith(".jpg") || url.endsWith(".JPG")) {
                filename = filename.substring(filename.lastIndexOf("/") + 1);
            } else {
                filename = filename.substring(filename.lastIndexOf("/") + 1) + ".jpg";
            }
        } else {//如果是视频数据，文件名统一经过md5处理
            if (url.endsWith(".mp4") || url.endsWith(".MP4")) {
                filename = AppUtil.md5(filename.substring(filename.lastIndexOf("/") + 1));
            } else {
                filename = AppUtil.md5(filename.substring(filename.lastIndexOf("/") + 1)) + ".mp4";
            }
        }
        return filename;
    }

    /**
     * 从cursor中获取photoInfo，不支持收藏表
     * @param cursor
     * @return
     */
    public static PhotoInfo getPhotoInfoFromCursor(Cursor cursor) {
        PhotoInfo sInfo = new PhotoInfo();
        sInfo.photoId = cursor.getString(cursor.getColumnIndex("photoId"));
        sInfo.photoPathOrURL = cursor.getString(cursor.getColumnIndex("originalUrl"));
        sInfo.photoThumbnail = cursor.getString(cursor.getColumnIndex("previewUrl"));
        sInfo.photoThumbnail_512 = cursor.getString(cursor.getColumnIndex("previewUrl_512"));
        sInfo.photoThumbnail_1024 = cursor.getString(cursor.getColumnIndex("previewUrl_1024"));
        sInfo.photoPassCode = cursor.getString(cursor.getColumnIndex("photoCode"));
        sInfo.locationId = cursor.getString(cursor.getColumnIndex("locationId"));
        sInfo.locationName = cursor.getString(cursor.getColumnIndex("locationName"));
        sInfo.locationCountry = cursor.getString(cursor.getColumnIndex("locationCountry"));
        sInfo.shootOn = cursor.getString(cursor.getColumnIndex("shootOn"));
        sInfo.shootTime = cursor.getString(cursor.getColumnIndex("shootTime"));
        sInfo.shareURL = cursor.getString(cursor.getColumnIndex("shareURL"));
        sInfo.isPayed = cursor.getInt(cursor.getColumnIndex("isPay"));
        sInfo.isVideo = cursor.getInt(cursor.getColumnIndex("isVideo"));
        sInfo.isLove = cursor.getInt(cursor.getColumnIndex("isLove"));
        sInfo.fileSize = cursor.getInt(cursor.getColumnIndex("fileSize"));
        sInfo.videoHeight = cursor.getInt(cursor.getColumnIndex("videoHeight"));
        sInfo.videoWidth = cursor.getInt(cursor.getColumnIndex("videoWidth"));
        sInfo.isHasPreset = cursor.getInt(cursor.getColumnIndex("isHasPreset"));
        sInfo.isEncrypted = cursor.getInt(cursor.getColumnIndex("enImg"));
        sInfo.adURL = cursor.getString(cursor.getColumnIndex("adURL"));
        sInfo.onLine = 1;
        sInfo.isChecked = 0;
        sInfo.isSelected = 0;
        sInfo.isUploaded = 0;
        sInfo.showMask = 0;
        sInfo.lastModify = 0l;
        sInfo.index = "";
        sInfo.isRefreshInfo = 0;
        return sInfo;
    }

    /**
     * 进入应用市场
     * @param paramContext
     * @param targetPackage 目前市场，如果为空，则让用户选择已安装的市场
     */
    public static void startMarketIntent(Context paramContext, String targetPackage) {
        StringBuilder localStringBuilder = new StringBuilder().append("market://details?id=");
        String str = paramContext.getPackageName();
        localStringBuilder.append(str);
        Uri localUri = Uri.parse(localStringBuilder.toString());
        Intent intent = new Intent(Intent.ACTION_VIEW, localUri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (!TextUtils.isEmpty(targetPackage)) {
            intent.setPackage(targetPackage);
        }
        paramContext.startActivity(intent);
    }

    /**
     * 提取目标结果
     * 1.找到对应的点(ffd9)
     * 2.找到后创建对应长度的数组
     * 3.对应的点之后的数据直接加入
     *
     * @param b
     * @return
     */
    public static byte[] getRealByte(byte[] b) {
        byte[] result = null;
        String stmp;//当前字节
        String lastStmp;//上一个字节

        for (int n = 1; n < b.length; n++) {
            lastStmp = Integer.toHexString(b[n - 1] & 0XFF);
            if (lastStmp.equals("ff")) {
                stmp = Integer.toHexString(b[n] & 0XFF);
                if (stmp.equals("d9")) {//创建新的字节数组
                    PictureAirLog.d(TAG, "the data----> n---->" + n + ";length--->" + b.length);
                    result = Arrays.copyOfRange(b, n + 1, b.length);
                    break;
                }
            }
        }

        boolean hasEncryptionData = false;
        if (result != null && result.length > 1) {
            stmp = Integer.toHexString(result[0] & 0XFF);
            if (stmp.equals("ff")) {
                String nextStmp = Integer.toHexString(result[1] & 0XFF);
                if (nextStmp.equals("d8")) {//有需要的内容
                    hasEncryptionData = true;
                }
            }
        }
        return hasEncryptionData ? result : null;
    }

    public static String getFormatCurrentTime(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String loadTime = df.format(new Date());
        return loadTime;
    }

    public static String formatData(double data){
        DecimalFormat decimalFormat=new DecimalFormat("0.00");
        String str = decimalFormat.format(data);
        return str;
    }

    /**
     * 检查是否需要解密数据
     * @param isEncrypted
     * @return
     */
    public static boolean isEncrypted(int isEncrypted) {
        return isEncrypted == 1;
    }

    /**
     * 字符串转日期
     *
     * @param strDate 2015-12-24 17:30
     * @return
     */
    public static Date getDateFromStr2(String strDate) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:MM");
        try {
            return df.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据宽高比例比较获取留白方向
     * @param targetW 比较的view
     * @param targetH
     * @param comparedW 被比较的view
     * @param comparedH
     * @return
     */
    public static int getOrientationMarginByAspectRatio(int targetW, int targetH, int comparedW, int comparedH) {
        if (targetH / (float) targetW > comparedH / (float) comparedW) {//左右会留白
            return HORIZONTAL_MARGIN;
        } else {//上下会留白
            return VERTICAL_MARGIN;
        }
    }

    /**
     * 判断是否是最新的数据 原图路径如果和 1024 512 128缩略图都不一样，表示原图路径是正确的，否则原图路径错误
     *
     * @param photoPathOrURL
     * @param photoThumbnail_1024
     * @param photoThumbnail_512
     * @param photoThumbnail
     * @return
     */
    public static boolean isOldVersionOfTheVideo(String photoPathOrURL, String photoThumbnail_1024, String photoThumbnail_512, String photoThumbnail){
        if (TextUtils.isEmpty(photoPathOrURL)) return true;

        if (!TextUtils.isEmpty(photoThumbnail_1024)){
            if (photoPathOrURL.equalsIgnoreCase(photoThumbnail_1024)){
                return true;
            }
        }

        if (!TextUtils.isEmpty(photoThumbnail_512)){//不要用equals，因为PHOTO_URL会改端口号
            if (photoPathOrURL.endsWith(photoThumbnail_512)){
                return true;
            }
        }

        if (!TextUtils.isEmpty(photoThumbnail)){
            if (photoPathOrURL.equalsIgnoreCase(photoThumbnail)){
                return true;
            }
        }
        return false;
    }

    /**
     * 在locationlist中查找指定的loactionid，如果有，返回对应的索引值，如果没有，则返回-1，表示是其他地点
     * @param info
     * @param locationList
     * @return
     */
    public static int findPositionInLocationList(PhotoInfo info, ArrayList<DiscoverLocationItemInfo> locationList) {
        int resultPosition = -1;
        //先挑选出相同的locationid信息
        for (int i = 0; i < locationList.size(); i++) {
            if (info.locationId.equals(locationList.get(i).locationId) || locationList.get(i).locationIds.contains(info.locationId)) {
                resultPosition = i;
                break;
            }
        }
        return resultPosition;
    }

    /**
     * 把服务器日期转换成手机当前时区时间
     * */
    public static Date getDateLocalFromStr(String date) throws ParseException{
        SimpleDateFormat localFromat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        localFromat.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        Date oldDate = localFromat.parse(date);
        Calendar oldCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
        oldCalendar.setTime(oldDate);
        localFromat.setTimeZone(TimeZone.getDefault());
        return localFromat.parse(localFromat.format(oldCalendar.getTime()));
    }
}
