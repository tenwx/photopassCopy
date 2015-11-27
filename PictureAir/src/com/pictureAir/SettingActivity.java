package com.pictureAir;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureAir.db.PictureAirDBHelper;
import com.pictureAir.db.PictureAirDbManager;
import com.pictureAir.service.NotificationService;
import com.pictureAir.util.ACache;
import com.pictureAir.util.API;
import com.pictureAir.util.AppManager;
import com.pictureAir.util.Common;
import com.umeng.analytics.MobclickAgent;

/**用户功能设置*/
public class SettingActivity  extends Activity implements OnClickListener{
	private Configuration config;
	private DisplayMetrics dm;
	//	private Resources resources;
	private RelativeLayout feedback;
	private ImageView back;
	private Button logout;
	private TextView tvSettingLanguage;
	private MyApplication application;
	
	// 用于显示的 按钮。
	private ImageButton wifiDownload;
	private ImageButton autoDownload;
	
	private RelativeLayout rl_wifi_download;
	private RelativeLayout rl_auto_download;
	
	private boolean wifiSelected;
	private boolean autoSelected;
	
	private SharedPreferences sharedPreferences;
	private PictureAirDbManager pictureAirDbManager;
	//feedback
	private EditText eTFeedback;
	private TextView tVsend;
	private TextView tVCancel;
	private AlertDialog myFeedbackDialog;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case API.LOGOUT_FAILED:
			case API.LOGOUT_SUCCESS:
				//断开推送
				API.noticeSocketDisConnect(sharedPreferences.getString(Common.USERINFO_TOKENID, null));
				
				SharedPreferences sp = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
				Editor editor = sp.edit();
				editor.clear();
				editor.commit();
				
				ACache.get(SettingActivity.this).remove(Common.TOP_GOODS);
				ACache.get(SettingActivity.this).remove(Common.ALL_GOODS);
				ACache.get(SettingActivity.this).remove(Common.BANNER_GOODS);
				ACache.get(SettingActivity.this).remove(Common.PPP_GOOD);

				application.photoPassPicList.clear();
				application.setPushPhotoCount(0);
				application.scanMagicFinish = false;
				application.fragmentStoryLastSelectedTab = 0;

				//清空photopass数据库
//				PictureAirDBHelper dbHelper;
//				dbHelper = new PictureAirDBHelper(SettingActivity.this, Common.PHOTOPASS_INFO_NAME, Common.PHOTOPASS_INFO_VERSION);
				pictureAirDbManager.deleteAllInfoFromTable(Common.PHOTOPASS_INFO_TABLE);
//				SQLiteDatabase db = dbHelper.getWritableDatabase();
//				System.out.println("delete all data from table");
//				db.execSQL("delete from "+Common.PHOTOPASS_INFO_TABLE);
//				db.close();
				//取消通知
				Intent intent = new Intent(SettingActivity.this, NotificationService.class);
				intent.putExtra("status", "disconnect");
				startService(intent);

				Intent i = new Intent(SettingActivity.this,LoginActivity.class);
				finish();
				startActivity(i);

				AppManager.getInstance().AppExit(SettingActivity.this);
				break;

			default:
				break;
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		AppManager.getInstance().addActivity(this);
		initView();
	}

	private void initView(){
		pictureAirDbManager = new PictureAirDbManager(this);
		sharedPreferences = getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
		boolean isSync = pictureAirDbManager.checkFirstBuyPhoto(Common.SETTING_SYNC, sharedPreferences.getString(Common.USERINFO_ID, ""));
		boolean isWifiDownload = pictureAirDbManager.checkFirstBuyPhoto(Common.SETTING_WIFI, sharedPreferences.getString(Common.USERINFO_ID, ""));
		config = getResources().getConfiguration();
		dm = getResources().getDisplayMetrics();
		logout = (Button) findViewById(R.id.logout);
		feedback = (RelativeLayout) findViewById(R.id.sub_opinions);
		back = (ImageView) findViewById(R.id.back);
		tvSettingLanguage = (TextView) findViewById(R.id.setting_language);
		application = (MyApplication) getApplication();
		
		wifiDownload = (ImageButton) findViewById(R.id.wifi_download);
		autoDownload = (ImageButton) findViewById(R.id.auto_download);
		
		if (isSync) {
			autoSelected = true;
			autoDownload.setImageResource(R.drawable.sele);
		}else{
			autoSelected = false;
		}
		
		if (isWifiDownload) {
			wifiSelected = true;
			wifiDownload.setImageResource(R.drawable.sele);
		}else{
			wifiSelected = false;
		}
		
		rl_wifi_download = (RelativeLayout) findViewById(R.id.rl_wifi_download);
		rl_auto_download = (RelativeLayout) findViewById(R.id.rl_auto_download);
		
		rl_wifi_download.setOnClickListener(this);
		rl_auto_download.setOnClickListener(this);
		
		logout.setOnClickListener(this);
		feedback.setOnClickListener(this);
		back.setOnClickListener(this);
		tvSettingLanguage.setOnClickListener(this);
		
		wifiDownload.setOnClickListener(this);
		autoDownload.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back:
			Intent intent1 = new Intent(this,MainTabActivity.class);
			startActivity(intent1);
			finish();
			break;

		case R.id.logout:
			// logout 之后，清空上个用户的数据。
			application.setLast_tab(0);   // 设置 进入 app为主页
			API.Logout(this, handler);
			break;

		case R.id.sub_opinions://消息回馈按钮
			diaLogFeedBack();
//			final EditText inputServer = new EditText(this);
//			inputServer.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
//			inputServer.setGravity(Gravity.TOP);
//			inputServer.setSingleLine(false);
//			inputServer.setHorizontallyScrolling(false);
//			inputServer.setWidth(ScreenUtil.getScreenWidth(this));
//			inputServer.setHeight(ScreenUtil.getScreenHeight(this)*2/3);
//			AlertDialog.Builder builder = new AlertDialog.Builder(this);
//			builder.setTitle(R.string.submit_opinions).setIcon(android.R.drawable.ic_dialog_info).setView(inputServer).setNegativeButton(R.string.button_cancel, null);
//			builder.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int which) {
//					//把inputServer中的文字传到服务器
//					try {
//						String opinions = new String(inputServer.getText().toString().getBytes(),"utf-8");
//					} catch (UnsupportedEncodingException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
////					RequestParams params = new RequestParams();
//				}
//			});
//			builder.show();
			break;

		case R.id.setting_language:
			Intent intent=new Intent();
			intent.setClass(SettingActivity.this, SettingLanguageActivity.class);
			startActivity(intent);

			break;
		case R.id.rl_wifi_download:
		case R.id.wifi_download:
			if (wifiSelected) {
				wifiDownload.setImageResource(R.drawable.nosele);
				wifiSelected = false;
				pictureAirDbManager.deleteSettingStatus(Common.SETTING_WIFI, sharedPreferences.getString(Common.USERINFO_ID, ""));
			}else{
				wifiDownload.setImageResource(R.drawable.sele);
				wifiSelected = true;
				pictureAirDbManager.insertSettingStatus(Common.SETTING_WIFI, sharedPreferences.getString(Common.USERINFO_ID, ""));
			}
			break;
		case R.id.rl_auto_download:
		case R.id.auto_download:
			if (autoSelected) {
				autoDownload.setImageResource(R.drawable.nosele);
				autoSelected = false;
				pictureAirDbManager.deleteSettingStatus(Common.SETTING_SYNC, sharedPreferences.getString(Common.USERINFO_ID, ""));
		    }else{
				autoDownload.setImageResource(R.drawable.sele);
				autoSelected = true;
				pictureAirDbManager.insertSettingStatus(Common.SETTING_SYNC, sharedPreferences.getString(Common.USERINFO_ID, ""));
			}
			break;
		// Feedback
		case R.id.tVCancel:
			myFeedbackDialog.dismiss();
			break;
		case R.id.tVsend:
			myFeedbackDialog.dismiss();
			String feedbackStr = eTFeedback.getText().toString();
			System.out.println("-------:" + feedbackStr);
			break;

		default:
			break;
		}

	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		AppManager.getInstance().killActivity(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onPageStart("SettingActivity");
		MobclickAgent.onResume(this);
		onCreate(null);
	}
	
	//监听返回键
	 @Override 
	    public boolean onKeyDown(int keyCode, KeyEvent event) { 
	        if ((keyCode == KeyEvent.KEYCODE_BACK)) { 
	        	Intent intent1 = new Intent(this,MainTabActivity.class);
				startActivity(intent1);
				finish();
	             return false; 
	        }else { 
	            return super.onKeyDown(keyCode, event); 
	        } 
	           
	    } 
	
	 private void diaLogFeedBack() {
			View v = LayoutInflater.from(this).inflate(
					R.layout.custom_dialog_feedback, null);
			AlertDialog.Builder myBuilder = new AlertDialog.Builder(this);
			myFeedbackDialog = myBuilder.create();
			myFeedbackDialog.setView(new EditText(this));//自定义的dialog，必须在show（）之前加入此行，不然显示不了软键盘
			myFeedbackDialog.show();
			myFeedbackDialog.getWindow().setContentView(v);
			eTFeedback = (EditText) v.findViewById(R.id.eTFeedback);
			tVsend = (TextView) v.findViewById(R.id.tVsend);
			tVCancel = (TextView) v.findViewById(R.id.tVCancel);
			tVsend.setOnClickListener(this);
			tVCancel.setOnClickListener(this);
			eTFeedback.setOnClickListener(this);

		}
	 @Override
		protected void onPause() {
			// TODO Auto-generated method stub
			super.onPause();
			MobclickAgent.onPageEnd("SettingActivity");
			MobclickAgent.onPause(this);
		}
	 
}
