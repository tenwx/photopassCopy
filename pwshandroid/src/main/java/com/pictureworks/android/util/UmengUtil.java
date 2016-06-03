package com.pictureworks.android.util;import android.content.Context;import android.content.SharedPreferences;import android.support.v4.app.Fragment;import com.umeng.analytics.AnalyticsConfig;import com.umeng.analytics.MobclickAgent;import java.util.Map;/** * 友盟工具类：初始化 、 统计 *  * 统计项：页面启动、关闭、停留时长、有图片人数 * * 注：目前所有的事件都是 ，计数事件。 (循环纪录) *  * @author milo&Talon 2015-11-19 */public class UmengUtil {	private static final String TAG = "UmengUtil==>>";	private static SharedPreferences sharedPreferences;	private static Context mContext;	/**	 * 初始化友盟	 *	 * 说明:key如果放在代码中设置 反馈功能不能正常使用。	 */	public static void initUmeng(Context context) {		mContext = context;		if (sharedPreferences == null) {			sharedPreferences = context.getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME,					Context.MODE_PRIVATE);		}		// 设置activity + fragment(view)		openActivityDurationTrack(false);	}	/**	 * 禁止默认的页面统计方式，这样将不会再自动统计Activity。	 * 	 * @param isActivity	 */	public static void openActivityDurationTrack(boolean isActivity) {		MobclickAgent.openActivityDurationTrack(isActivity);	}	/**	 * onResume	 * 	 * @param context	 * @param isFragment	 */	public static void onResume(Context context, boolean isFragment) {		if (!isFragment) {			MobclickAgent.onPageStart(context.getClass().getName());// 统计页面跳转		}		MobclickAgent.onResume(context);// 统计应用时长	}	/**	 * fragment	 * 	 * @param fragment	 */	public static void onResume(Fragment fragment) {		MobclickAgent.onPageStart(fragment.getClass().getName());	}	/**	 * onPause	 * 	 * @param context	 * @param isFragment	 */	public static void onPause(Context context, boolean isFragment) {		if (!isFragment) {			MobclickAgent.onPageEnd(context.getClass().getName());// 统计页面跳转		}		MobclickAgent.onPause(context);	}	/**	 * fragment	 * 	 * @param fragment	 */	public static void onPause(Fragment fragment) {		MobclickAgent.onPageEnd(fragment.getClass().getName());	}	/**	 * 设置是否对日志信息进行加密, 默认false(不加密).	 */	public static void enableEncrypt(boolean enable) {		AnalyticsConfig.enableEncrypt(enable);	}	/**	 * 保存统计数据	 * 	 * @param context	 */	public static void onKillProcess(Context context) {		MobclickAgent.onKillProcess(context);	}	/******************************** 统计 ********************************/	/**	 * 计数事件	 * 	 * @param context	 * @param eventId	 */	public static void onEvent(Context context, String eventId) {		MobclickAgent.onEvent(context, eventId);	}	/**	 * 计数事件 数字变化	 * 	 * @param context	 * @param eventId	 * @param map	 */	public static void onEvent(Context context, String eventId,			Map<String, String> map) {		MobclickAgent.onEvent(context, eventId, map);	}}