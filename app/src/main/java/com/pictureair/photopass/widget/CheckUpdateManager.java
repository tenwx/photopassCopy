package com.pictureair.photopass.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.View;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.BaseCheckUpdate;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import cn.smssdk.gui.CustomProgressDialog;

public class CheckUpdateManager {
	private BaseCheckUpdate baseCheckUpdate;
	private Context context;
	private ArrayList<String> deviceInfos;
	private PictureWorksDialog pictureWorksDialog;
	private String downloadURL, forceUpdate, currentLanguage;
	private CustomProgressBarPop customProgressBarPop;
	private CustomProgressDialog customProgressDialog;
	private View parentView;
	private MyToast myToast;
	private String version;
	private File downloadAPKFile;
	private static final int INSTALL_APK = 201;
	private static final int GENERATE_APK_FAILED = 202;
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case API1.GET_TOKEN_ID_FAILED:
					break;

				case API1.GET_TOKEN_ID_SUCCESS:
					baseCheckUpdate.checkUpdate(context, handler, deviceInfos.get(1), currentLanguage);
					break;


			case API1.APK_NEED_NOT_UPDATE:
				PictureAirLog.out("apk need not update");
				break;
				
			case API1.APK_NEED_UPDATE:
				PictureAirLog.out("apk need update");
				//开始显示对话框
				String objsString[] = (String[]) msg.obj;
				downloadURL = objsString[3];
				forceUpdate = objsString[1];
				version = objsString[0];
				
				pictureWorksDialog = new PictureWorksDialog(context, String.format(context.getString(R.string.update_version), version), objsString[2],
						forceUpdate.equals("true") ? null : context.getString(R.string.cancel1), context.getString(R.string.down), false, handler);
				pictureWorksDialog.show();
				break;
				
			case DialogInterface.BUTTON_POSITIVE:
				PictureAirLog.out("apk yes");
				//开始下载
				downloadAPKFile = new File(Common.DOWNLOAD_APK_PATH + "pictureAir_"+ version +".apk");
				if (downloadAPKFile.exists()) {//文件已经存在
					PictureAirLog.out("apk exist");
					handler.sendEmptyMessage(INSTALL_APK);
				} else {//文件不存在，需要去下载
					//直线型进度条
					customProgressBarPop = new CustomProgressBarPop(context, parentView, CustomProgressBarPop.TYPE_DOWNLOAD);
					customProgressBarPop.show(0);
					baseCheckUpdate.downloadAPK(downloadURL, customProgressBarPop, version, handler);
				}
				break;
				
			case API1.DOWNLOAD_APK_FAILED:
				PictureAirLog.out("failed");
				customProgressBarPop.dismiss();
				myToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
				break;
				
			case API1.DOWNLOAD_APK_SUCCESS:
				//下载成功
				PictureAirLog.out("success");
				if (customProgressBarPop != null) {
					customProgressBarPop.dismiss();
				}
				customProgressDialog = CustomProgressDialog.show(context, context.getString(R.string.generate_apk), false, null);
				//准备生成apk
				final byte[] data = (byte[]) msg.obj;
				
				new Thread(){
					public void run() {
						
						File downloadAPKFile = new File(Common.DOWNLOAD_APK_PATH);
						if (!downloadAPKFile.exists()) {
							downloadAPKFile.mkdirs();
						}
						File downloadFile = new File(Common.DOWNLOAD_APK_PATH + "pictureAir_"+ version +".apk");
						try {
							downloadFile.createNewFile();
							
							FileOutputStream fos = new FileOutputStream(downloadFile);
							fos.write(data);
							fos.close();
							handler.sendEmptyMessage(INSTALL_APK);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							downloadFile.delete();
							handler.sendEmptyMessage(GENERATE_APK_FAILED);
						}
					};
				}.start();
				break;
				
			case INSTALL_APK:
				if (customProgressDialog != null) {
					customProgressDialog.dismiss();
				}
				//开始安装新版本
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
				intent.setDataAndType(Uri.fromFile(downloadAPKFile),
						"application/vnd.android.package-archive");
				context.startActivity(intent);
				AppManager.getInstance().AppExit(context);
				break;
				
			case GENERATE_APK_FAILED://生成apk失败
				if (customProgressDialog != null) {
					customProgressDialog.dismiss();
				}
				myToast.setTextAndShow(R.string.generate_apk_failed, Common.TOAST_SHORT_TIME);
				break;
				
			default:
				break;
			}
		};
	};
	
	/**
	 * 自动检查更新封装类
	 * 
	 * 怎么使用
	 * 1.申明变量
	 * 2.checkUpdateManager = new CheckUpdateManager(this, currentLanguage, linearLayout);
	 * 3.checkUpdateManager.startCheck();
	 * @param context
	 * @param currentLanguage 当前语言
	 * @param parent 进度条需要的父控件
	 */
	public CheckUpdateManager(Context context, String currentLanguage, View parent) {
		this.context = context;
		this.currentLanguage = currentLanguage;
		this.parentView = parent;
		myToast = new MyToast(context);
		baseCheckUpdate = CheckUpdate.getInstance();

	}
	
	/**
	 * 开始检查更新
	 */
	public void startCheck(){
		deviceInfos = AppUtil.getDeviceInfos(context);
		if (MyApplication.getTokenId() == null) {
			baseCheckUpdate.getTokenId(context, handler);
		} else {
			handler.sendEmptyMessage(API1.GET_TOKEN_ID_SUCCESS);
		}

	}
	
	/**
	 * 根据版本号，返回数组进行判断
	 */
	public static int[] verNameChangeInt(String versionName){
		int[] number = new int[3];
		String [] numberStr = versionName.split("\\.");
		int number0 = 0; 
		int number1 = 0;
		int number2 = 0;
		try {
			number0 = Integer.valueOf(numberStr[0]);
			number1 = Integer.valueOf(numberStr[1]);
			number2 = Integer.valueOf(numberStr[2]);
		} catch (Exception e) {
			
		}
		number[0] = number0;
		number[1] = number1;
		number[2] = number2;
		return number;
	}
	
}
