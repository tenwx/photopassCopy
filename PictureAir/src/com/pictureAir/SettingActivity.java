package com.pictureAir;

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureAir.db.PhotoInfoDBHelper;
import com.pictureAir.service.NotificationService;
import com.pictureAir.util.ACache;
import com.pictureAir.util.API;
import com.pictureAir.util.AppManager;
import com.pictureAir.util.Common;
import com.pictureAir.util.ScreenUtil;
import com.pictureAir.util.UmengUtil;
import com.umeng.fb.FeedbackAgent;

/**用户功能设置*/
public class SettingActivity  extends BaseActivity implements OnClickListener{
	private Configuration config;
	private DisplayMetrics dm;
	//	private Resources resources;
	private RelativeLayout feedback;
	private ImageView back;
	private Button logout;
	private TextView tvSettingLanguage;
	private MyApplication application;
	//feedback
	
	// 三个单选按钮
	private ImageButton gDownload;
	private ImageButton wifiDownload;
	private ImageButton autoDownload;
	boolean isAuto = false;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case API.LOGOUT_FAILED:
			case API.LOGOUT_SUCCESS:
				SharedPreferences sp = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
				Editor e = sp.edit();
				e.clear();
				e.commit();
				
				ACache.get(SettingActivity.this).remove(Common.TOP_GOODS);
				ACache.get(SettingActivity.this).remove(Common.ALL_GOODS);
				ACache.get(SettingActivity.this).remove(Common.BANNER_GOODS);
				ACache.get(SettingActivity.this).remove(Common.PPP_GOOD);

				application.photoPassPicList.clear();
				application.setPushPhotoCount(0);
				application.scanMagicFinish = false;
				application.fragmentStoryLastSelectedTab = 0;

				//清空photopass数据库
				PhotoInfoDBHelper dbHelper;
				dbHelper = new PhotoInfoDBHelper(SettingActivity.this, Common.PHOTOPASS_INFO_NAME, Common.PHOTOPASS_INFO_VERSION);
				SQLiteDatabase db = dbHelper.getWritableDatabase();
				System.out.println("delete all data from table");
				db.execSQL("delete from "+Common.PHOTOPASS_INFO_TABLE);
				db.close();
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
		setContentView(R.layout.setting);
		AppManager.getInstance().addActivity(this);
		initView();
	}

	private void initView(){
		config = getResources().getConfiguration();
		dm = getResources().getDisplayMetrics();
		logout = (Button) findViewById(R.id.logout);
		feedback = (RelativeLayout) findViewById(R.id.sub_opinions);
		back = (ImageView) findViewById(R.id.back);
		tvSettingLanguage = (TextView) findViewById(R.id.setting_language);
		application = (MyApplication) getApplication();
		
		gDownload = (ImageButton) findViewById(R.id.g_download);
		wifiDownload = (ImageButton) findViewById(R.id.wifi_download);
		autoDownload = (ImageButton) findViewById(R.id.auto_download);
		
		gDownload.setOnClickListener(this);
		wifiDownload.setOnClickListener(this);
		autoDownload.setOnClickListener(this);
		
		logout.setOnClickListener(this);
		feedback.setOnClickListener(this);
		back.setOnClickListener(this);
		tvSettingLanguage.setOnClickListener(this);
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
			//意见反馈弹出框
			UmengUtil.startFeedbackActivity(this);
			
			break;

		case R.id.setting_language:
			Intent intent=new Intent();
			intent.setClass(SettingActivity.this, SettingLanguageActivity.class);
			startActivity(intent);
//			String languageType = Common.ENGLISH; 
//			if(config.locale.equals(Locale.SIMPLIFIED_CHINESE)){
//				config.locale = Locale.US;
//				languageType = Common.ENGLISH;
//				Log.e(" == ", " new MyApplication().getLanguageType(): "+((MyApplication) this.getApplicationContext()).getLanguageType());
//			}else if(config.locale.equals(Locale.US)){
//				config.locale = Locale.SIMPLIFIED_CHINESE;
//				languageType = Common.SIMPLE_CHINESE;
//			}
//			((MyApplication) this.getApplicationContext()).setLanguageType(languageType);
//			getResources().updateConfiguration(config, dm);
//			//			onCreate(null);
//			Intent intent = new Intent(SettingActivity.this,MainTabActivity.class);
//			startActivity(intent);
//
//			//将语言类型写入数据库
//			SharedPreferences settingLanguage = this.getSharedPreferences(Common.APP,Context.MODE_PRIVATE);
//			Editor localEditor = settingLanguage.edit();
//			localEditor.putString(Common.LANGUAGE_TYPE, languageType);
//			localEditor.commit();

			break;
		case R.id.g_download:
			gDownload.setImageResource(R.drawable.sele);
			wifiDownload.setImageResource(R.drawable.nosele);
			break;
		case R.id.wifi_download:
			wifiDownload.setImageResource(R.drawable.sele);
			gDownload.setImageResource(R.drawable.nosele);
			break;
		case R.id.auto_download:
			
            if(isAuto){
            	autoDownload.setImageResource(R.drawable.nosele);
            	isAuto = false;
            }else{
            	autoDownload.setImageResource(R.drawable.sele);
            	isAuto = true;
            }
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
//		onCreate(null);
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
	
	
}
