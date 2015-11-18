package com.pictureAir;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import com.pictureAir.util.API;
import com.pictureAir.util.AppManager;
import com.pictureAir.util.Common;

/**
 * 开始的启动页面，如果第一次进入，则进入第一次的引导页，如果不是，则进入登录页面
 * @author bauer_bao
 *
 */
public class StartActivity extends Activity{
	private SharedPreferences spApp;
	private int code = 0;
	private String languageType;
	private Configuration config;
	private DisplayMetrics displayMetrics;
	private ImageView imageView1, imageView2;
	private static final String TAG = "StartActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		
		spApp = getSharedPreferences(Common.APP, MODE_PRIVATE);
		AppManager.getInstance().addActivity(this);
		config = getResources().getConfiguration();
		displayMetrics = getResources().getDisplayMetrics();
		
		//获取手机设置的语言
		languageType = spApp.getString(Common.LANGUAGE_TYPE, Common.ENGLISH);
		if(languageType.equals(Common.ENGLISH)){
			config.locale = Locale.US;
		}else if (languageType.equals(Common.SIMPLE_CHINESE)) {
			config.locale = Locale.SIMPLIFIED_CHINESE;
		}
		
		imageView1 = (ImageView)findViewById(R.id.img);
		imageView1.setImageResource(R.drawable.launching_logo);
		
		imageView2 = (ImageView)findViewById(R.id.launching_bottom);
		imageView2.setImageResource(R.drawable.launching_bottom);

		((MyApplication) this.getApplicationContext()).setLanguageType(languageType);
		getResources().updateConfiguration(config, displayMetrics);
		
		try {
			PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
			final int versionCode = info.versionCode;
			code = spApp.getInt(Common.APP_VERSION_CODE, 0);
			System.out.println("code="+code+";versioncode="+versionCode);

			if (code == versionCode) {// 启动app,如果不是第一次进入，则直接跳过引导页
				// 需要从服务器获取最新的照片信息，地点信息，个人用户信息，商品信息，购物车信息等等，然后存入到数据库中
				SharedPreferences sp = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
				final String _id = sp.getString(Common.USERINFO_ID, null);
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						Intent intent = null;
						if(_id != null){//判断是否已经登录
							//发送广播
							Log.d(TAG, "start push service");
							Intent intent2 = new Intent();
							intent2.setAction("com.receiver.AlertManagerRecriver");
							StartActivity.this.sendBroadcast(intent2);
							intent = new Intent(StartActivity.this, MainTabActivity.class);
						}else{
							intent = new Intent(StartActivity.this, LoginActivity.class);
						}
						finish();
						startActivity(intent);
					}
				}, 1000);
			} else {//进入引导页
				API.getTokenId(this);
				Editor editor = spApp.edit();
				editor.putInt(Common.APP_VERSION_CODE, versionCode);
				editor.commit();
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						Intent intent = new Intent(StartActivity.this, WelcomeActivity.class);
						finish();
						startActivity(intent);
					}
				}, 2000);
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		AppManager.getInstance().killActivity(this);
	}
}
