package com.pictureAir.util;

import java.util.Iterator;
import java.util.Stack;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

/**
 * @author Tau.Chen 陈涛
 *
 * @email tauchen1990@gmail.com,1076559197@qq.com
 *
 * @date 2013年9月12日
 *
 * @version V_1.0.0
 *
 * @description 应用程序Activity的管理类
 *
 */
public class AppManager {
	private static Stack<Activity> mActivityStack;
	private static AppManager mAppManager;
	
	private static String TAG = "AppManager";

	private AppManager() {
	}

	/**
	 * 单一实例
	 */
	public static AppManager getInstance() {
		if (mAppManager == null) {
			mAppManager = new AppManager();
		}
		return mAppManager;
	}

	/**
	 * 添加Activity到堆栈
	 */
	public void addActivity(Activity activity) {
		if (mActivityStack == null) {
			mActivityStack = new Stack<Activity>();
		}
		
		mActivityStack.add(activity);
		Log.d(TAG, "mactivitystack size = "+mActivityStack.size());
		for (int i = 0; i < mActivityStack.size(); i++) {
			
			Log.d(TAG, "mactivitystack name = "+mActivityStack.get(i).toString());
		}
	}

	/**
	 * 获取栈顶Activity（堆栈中最后一个压入的）
	 */
	public Activity getTopActivity() {
		Activity activity = mActivityStack.lastElement();
		return activity;
	}
	
	/**
	 * 获取在栈中的activity的总数量
	 * @return
	 */
	public int getActivityCount() {
		return mActivityStack.size();
	}
	
	/**
	 * 检查是否有对应的Activity
	 * @param cls
	 * @return
	 */
	public boolean checkActivity(Class<?> cls){
		for (Activity act : mActivityStack) {
			if (act.getClass().equals(cls)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 结束栈顶Activity（堆栈中最后一个压入的）
	 */
	public void killTopActivity() {
		Activity activity = mActivityStack.lastElement();
		killActivity(activity);
		Log.d(TAG, "mactivitystack size = "+mActivityStack.size());
//		System.out.println("mactivitystack size = "+mActivityStack.size());
	}

	/**
	 * 结束指定的Activity
	 */
	public void killActivity(Activity activity) {
		if (activity != null) {
			mActivityStack.remove(activity);
			if (!activity.isFinishing()) {//如果正在finish的话，就不需要再finish
				activity.finish();
			}
			System.out.println("finished-----"+ activity.toString());
			activity = null;
		}
	}

	/**
	 * 结束指定类名的Activity
	 */
	public void killActivity(Class<?> cls) {
		System.out.println("kill activity"+cls+"==========="+mActivityStack.size());
		for (Activity activity : mActivityStack) {
			System.out.println("in mactivitystack = "+ activity.getClass());
			if (activity.getClass().equals(cls)) {
				killActivity(activity);
				break;
			}
		}
	}

	/**
	 * 结束所有Activity
	 */
	public void killAllActivity() {
		for (int i = 0, size = mActivityStack.size(); i < size; i++) {
			if (null != mActivityStack.get(i)) {
				mActivityStack.get(i).finish();
			}
		}
		mActivityStack.clear();
	}
	
	/**
	 * 结束除了specialActivity之外的其他的activity
	 * @param specialActivity 指定的activity
	 */
	public void killOtherActivity(Class<?> specialActivity){
//		for (int i = 0; i < mActivityStack.size(); i++) {
//			System.out.println("activity is "+ mActivityStack.get(i).toString());
//		}
		Iterator<Activity> iterator = mActivityStack.iterator();
		while (iterator.hasNext()) {
			Activity activity = (Activity) iterator.next();
//			System.out.println("current activity is "+ specialActivity+"--------"+activity.getClass());
			if (!activity.getClass().equals(specialActivity)) {
				activity.finish();
				iterator.remove();
			}
		}
//		for (int i = 0; i < mActivityStack.size(); i++) {
//			System.out.println("after delete activity is "+ mActivityStack.get(i).toString());
//		}
	}

	/**
	 * 退出应用程序
	 */
	public void AppExit(Context context) {
		try {
			killAllActivity();
			ActivityManager activityMgr = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			activityMgr.restartPackage(context.getPackageName());
			System.exit(0);
		} catch (Exception e) {
		}
	}
}
