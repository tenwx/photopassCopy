package com.pictureAir.util;

import android.util.Log;

/**
 * log的封装类
 * @author bauer_bao
 *
 */
public class PictureAirLog {
	/**
	 * 是否开启debug模式，如果为true，打印log，如果为false，不打印log
	 */
	private final static boolean DEBUG = true;

	/**
	 * 
	 * @param tag
	 * @param log
	 */
	public static void d(String tag, String log){
		if (DEBUG) {
			Log.d(tag, log);
		}
	}

	/**
	 * 
	 * @param tag
	 * @param log
	 */
	public static void e(String tag, String log){
		if (DEBUG) {
			Log.e(tag, log);
		}
	}

	/**
	 * 
	 * @param tag
	 * @param log
	 */
	public static void i(String tag, String log){
		if (DEBUG) {
			Log.i(tag, log);
		}
	}

	/**
	 * 
	 * @param tag
	 * @param log
	 */
	public static void v(String tag, String log){
		if (DEBUG) {
			Log.v(tag, log);
		}
	}

	/**
	 * 
	 * @param tag
	 * @param log
	 */
	public static void w(String tag, String log){
		if (DEBUG) {
			Log.w(tag, log);
		}
	}
	
	/**
	 * 
	 * @param log
	 */
	public static void out(String log){
		if (DEBUG) {
			System.out.println(log);
		}
	}
	
	/**
	 * 
	 * @param log
	 */
	public static void err(String log){
		if (DEBUG) {
			System.err.println(log);
		}
	}
}
