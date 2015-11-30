package com.pictureAir;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pictureAir.util.API;
import com.pictureAir.util.AppManager;
import com.pictureAir.util.Common;
import com.pictureAir.util.HttpUtil;
import com.pictureAir.widget.CustomProgressDialog;
import com.pictureAir.widget.MyToast;
import com.pictureAir.zxing.camera.CameraManager;
import com.pictureAir.zxing.decoding.CaptureActivityHandler;
import com.pictureAir.zxing.decoding.InactivityTimer;
import com.pictureAir.zxing.view.ViewfinderView;
import com.umeng.analytics.MobclickAgent;
/**
 * Initial the camera
 * @author Talon
 */
public class MipCaptureActivity extends BaseActivity implements Callback {

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	private SurfaceView surfaceView;
	private SharedPreferences sp;
	private ImageView back;
	private String code;
	private String type;

	private final static int SCAN_SUCCESS = 11;
	private final static int SCAN_PPP_SUCCESS = 12;
	private final static int SCAN_FAILED = 15;
	private final static int MANUAL_INPUT_CODE = 13;
	private final static int CHECK_CODE = 14;

	private MyToast newToast;

	private MyApplication myApplication;
	private CustomProgressDialog dialog;

	private Handler handler2 = new Handler(){
		public void handleMessage(android.os.Message msg) {

			JSONObject response = null;
			switch (msg.what) {
			case SCAN_SUCCESS://pp
				try {
					response = (JSONObject) msg.obj;
					System.out.println(response+"----------");
					if (response.has("success")) {
						Editor editor = sp.edit();
						editor.putBoolean(Common.NEED_FRESH, true);
						editor.putInt(Common.PP_COUNT, sp.getInt(Common.PP_COUNT, 0) + 1);
						editor.commit();
						newToast.setTextAndShow(R.string.success1, Common.TOAST_SHORT_TIME);
						finish();
					} else{
						JSONObject errorString = response.getJSONObject("error");
						if (errorString.getString("type").equals("notLogin")) {
							Intent intent = new Intent(MipCaptureActivity.this,LoginActivity.class);
							startActivity(intent);
							finish();
						}else {
							if (errorString.getString("type").equals("incomplete")) {
								newToast.setTextAndShow("Incomplete", Common.TOAST_SHORT_TIME);
							}else if(errorString.getString("type").equals("notLogin")){
								newToast.setTextAndShow(R.string.please_login, Common.TOAST_SHORT_TIME);
							}else if(errorString.getString("type").equals("tokenExpired")){
								newToast.setTextAndShow(R.string.Tokenexpired, Common.TOAST_SHORT_TIME);
							}else if (errorString.getString("type").equals("PPHasBind")) {
								newToast.setTextAndShow(R.string.has_used, Common.TOAST_SHORT_TIME);
							}else if (errorString.getString("type").equals("invalidPP")) {
								newToast.setTextAndShow(R.string.invalidPP, Common.TOAST_SHORT_TIME);
							}else if (errorString.getString("type").equals("PPRepeatBound")) {
								newToast.setTextAndShow(R.string.repeatPPbind, Common.TOAST_SHORT_TIME);
							}
							finish();
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case SCAN_PPP_SUCCESS://ppp

				try {
					System.out.println(response+"----------");
					response = (JSONObject) msg.obj;
					if (response.has("success")) {
						if (getIntent().getStringExtra("type") != null) {//从ppp进入
							Intent intent2 = new Intent();
							intent2.putExtra("result", "pppOK");
							setResult(RESULT_OK, intent2);
						}else {

							newToast.setTextAndShow(R.string.success1, Common.TOAST_SHORT_TIME);
							Intent intent = new Intent(MipCaptureActivity.this,MyPPPActivity.class);
							API.PPPlist.clear();
							startActivity(intent);
						}
					} else{
						JSONObject errorString = response.getJSONObject("error");
						if (errorString.getString("type").equals("incomplete")) {
							newToast.setTextAndShow("Incomplete", Common.TOAST_SHORT_TIME);
						}else if(errorString.getString("type").equals("errQueryPPP")){
							newToast.setTextAndShow("ErrQueryPPP", Common.TOAST_SHORT_TIME);
						}else if(errorString.getString("type").equals("denyUsePPP")){
							newToast.setTextAndShow("DenyUsePPP", Common.TOAST_SHORT_TIME);
						}else if(errorString.getString("type").equals("PPPHasBind")){
							newToast.setTextAndShow(R.string.has_used, Common.TOAST_SHORT_TIME);
						}else if(errorString.getString("type").equals("invalidPPP")){
							newToast.setTextAndShow(R.string.invalidPPP, Common.TOAST_SHORT_TIME);
						}else if(errorString.getString("type").equals("errSavePPP")){
							newToast.setTextAndShow("ErrSavePPP", Common.TOAST_SHORT_TIME);
						}else if(errorString.getString("type").equals("notLogin")){
							newToast.setTextAndShow(R.string.please_login, Common.TOAST_SHORT_TIME);
						}else if(errorString.getString("type").equals("noPaidForPPP")) {
							newToast.setTextAndShow(R.string.please_pay_first, Common.TOAST_SHORT_TIME);
						}
					}
					finish();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case API.SUCCESS:
				Intent intent = new Intent(MipCaptureActivity.this, MyPPPActivity.class);
				API.PPPlist.clear();
				startActivity(intent);
				finish();
				break;
				
			case SCAN_FAILED:
			case API.FAILURE://网络异常
				newToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
				finish();
				break;
			case API.CHECK_CODE_FAILED://返回数据的tip提示，并且结束当前界面
				System.out.println("check code filed");
				if (dialog.isShowing()) {
					dialog.dismiss();
				}
				response = (JSONObject) msg.obj;
				try {
					if (response.getString("type").equals("incomplete")) {
					}else if (response.getString("type").equals("invalidCode")) {

					}else if (response.getString("type").equals("errQueryUser")) {

					}else if (response.getString("type").equals("PPHasBind")) {

					}else if (response.getString("type").equals("errQueryPPP")) {

					}else if (response.getString("type").equals("noPaidForPPP")) {

					}else if (response.getString("type").equals("invalidPPP")) {

					}else if (response.getString("type").equals("errQueryPhoto")) {

					}else if (response.getString("type").equals("PPPHasBind")) {
						
					}
					if (getIntent().getStringExtra("type") != null) {//如果从ppp页面过来，需要返回错误类型数据
						/**************************暂时注释************************************/
//						if (response.getString("type").equals("PPHasBind")) {//如果提示pphasbind，则可以添加
//							Intent intent2 = new Intent();
//							intent2.putExtra("result", code);//将pp码返回
//							intent2.putExtra("hasBind", true);
//							setResult(RESULT_OK, intent2);
//						}else {
							
							Intent intent2 = new Intent();
							intent2.putExtra("result", "failed");
							intent2.putExtra("errorType", response.getString("type"));
							setResult(RESULT_OK, intent2);
//						}
					}else {
						newToast.setTextAndShow(response.getString("message"), Common.TOAST_SHORT_TIME);
						
					}
				} catch (JSONException e1) {
					e1.printStackTrace();
				} 

				finish();
				break;
			case API.CHECK_CODE_SUCCESS://成功返回数据，判断是否已经登录，如果已经登录，则直接绑定，如果没有登录，调转到登录界面，将数据保存起来
				/**
				 * 1.返回数据处理
				 * 2.判断当前是否已经登录
				 * 3.如果已经登录，直接绑定
				 * 4.如果没有登录，将数据保存，并且跳转至登录界面，登录之后进行自动绑定（数据需要存放至公共变量中）
				 */
				if (dialog.isShowing()) {
					dialog.dismiss();
				}
				System.out.println("----------->check code success");
				response = (JSONObject) msg.obj;
				HashMap<String, String> map = new HashMap<String, String>();
				try {
					if (response.has("codeType")&&"photoPass".equals(response.getString("codeType"))) {
						System.out.println("check pp code success");
						map.put("photoPass", code);
						type = "pp";
					}else if (response.has("codeType")&&"photoPassPlus".equals(response.getString("codeType"))) {
						System.out.println("check pp+ code success");
						map.put("photoPassPlus", code);
						type = "ppp";
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (null == sp.getString(Common.USERINFO_ID, null)) {//没有登录，直接跳转至登录界面，并且保存数据
					//暂时是保持最后一次的数据
					System.out.println("--------------->need add to list");
					myApplication.clearCodeList();
					myApplication.addObject2CodeList(map);
					newToast.setTextAndShow(R.string.please_login, Common.TOAST_SHORT_TIME);
					Intent intent2 = new Intent(MipCaptureActivity.this,LoginActivity.class);
					startActivity(intent2);
					finish();
				}else {//已经登录，直接绑定到用户
					System.out.println("--------------->need not add to list");
					if (getIntent().getStringExtra("type") != null) {//如果从ppp页面进来，卡的类型不一致，直接返回，退出
						System.out.println("--------->need call back");
						if (!getIntent().getStringExtra("type").equals(type)) {//卡类型不一致
							Intent intent2 = new Intent();
							intent2.putExtra("result", "notSame");
							setResult(RESULT_OK, intent2);
							finish();
						}else {//类型一致。如果是ppp的话，直接绑定，如果是pp的话，提示并返回，让用户去确认
							if (type.equals("ppp")) {
								getInfo(code, type);
							}else {//如果是pp，返回信息
								Intent intent2 = new Intent();
								intent2.putExtra("result", code);//将pp码返回
								intent2.putExtra("hasBind", false);
								setResult(RESULT_OK, intent2);
								finish();
							}
						}
					}else {//其他页面
						System.out.println("-------->get info");
						getInfo(code, type);

					}
				}
				break;
			case CHECK_CODE:
				API.checkCodeAvailable(msg.obj.toString(), handler2);
				break;
			default:
				break;
			}
		};
	};
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capture);
		System.out.println("-----------create");
		AppManager.getInstance().addActivity(this);
		newToast = new MyToast(this);
		sp = getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
		surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		CameraManager.init(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);

		myApplication = (MyApplication) getApplication();
		back = (ImageView) findViewById(R.id.back);
		ImageView manual = (ImageView) findViewById(R.id.manual);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		manual.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				//跳转到输入  code 的界面。
				Intent i = new Intent(MipCaptureActivity.this, com.pictureAir.InputCodeActivity.class);
				i.putExtra("needCallBack", true);
				startActivityForResult(i, MANUAL_INPUT_CODE);
			}
		});
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		System.out.println("resume==============");

		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;

	}

	@Override
	protected void onPause() {
		super.onPause();
		System.out.println("----------pause");
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.out.println("-----------destroy");
		inactivityTimer.shutdown();
		if (mediaPlayer != null) {

			if (mediaPlayer.isPlaying()) {

				mediaPlayer.stop();
			}
			mediaPlayer.release();
		}
		mediaPlayer = null;
		AppManager.getInstance().killActivity(this);
	}

	/**
	 * 处理函数
	 * @param result
	 * @param barcode
	 */
	public void handleDecode(Result result, Bitmap barcode) {
		if (dialog != null && dialog.isShowing()) {//不处理扫描结果
			
		}else {
			inactivityTimer.onActivity();
			playBeepSoundAndVibrate();
			String resultString = result.getText();
			System.out.println("scan result = "+ resultString);
			if (resultString.equals("")) {
				//			Toast.makeText(MipcaActivityCapture.this, "Scan failed!", Toast.LENGTH_SHORT).show();
				newToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
			}else {
				
				code = resultString.substring(resultString.lastIndexOf("?")+1, resultString.length());  //截取字符串。
				Log.e("=====", "code：：："+code);
//				dialog = ProgressDialog.show(this, getString(R.string.loading___), getString(R.string.is_loading), true, false);
				dialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
				API.checkCodeAvailable(code, handler2);
			}
			
		}
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			System.out.println("meiyou dakai xiangji1");
			return;
		} catch (RuntimeException e) {
			newToast.setTextAndShow(R.string.camera_closed_jump_to_manual, Common.TOAST_SHORT_TIME);
			Intent intent = new Intent();
			intent.putExtra("needCallBack", false);
			intent.setClass(this, InputCodeActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		System.out.println("----------holder create");
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		System.out.println("---------holder destroy");
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

	private void getInfo(String code, final String type){
		RequestParams params = new RequestParams();
		System.out.println("scan result="+code+">>"+type);
		params.put(Common.USERINFO_TOKENID, sp.getString(Common.USERINFO_TOKENID, ""));
		String urlString = null;
		if ("pp".equals(type)) {
			if (null!=getIntent().getStringExtra("needbind")&&"false".equals(getIntent().getStringExtra("needbind"))) {//如果是通过pp界面扫描的时候，此处不需要绑定pp到用户
				JSONArray pps = new JSONArray();
				pps.put(code);

				API.bindPPsToPPP(sp.getString(Common.USERINFO_TOKENID, null),pps, getIntent().getStringExtra("binddate"), getIntent().getStringExtra("pppid"), handler2);
				System.out.println("return");
				return;
			}else {//其他界面过来的话，需要绑定到user
				System.out.println("pp");
				params.put(Common.CUSTOMERID, code);
				urlString = Common.BASE_URL+Common.ADD_CODE_TO_USER;
			}
		}else {
			System.out.println("ppp");
			params.put(Common.PPPCode, code);
			urlString = Common.BASE_URL+Common.BIND_PPP_TO_USER;
		}
		System.out.println("return32");

		HttpUtil.get(urlString, params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				if(statusCode == 200){
					if (null != response) {
						Message message = handler2.obtainMessage();
						if ("ppp".equals(type)) {
							message.what = SCAN_PPP_SUCCESS;
						}else if ("pp".equals(type)) {
							message.what = SCAN_SUCCESS;
						}
						message.obj = response;
						handler2.sendMessage(message);
					}else{
						newToast.setTextAndShow(R.string.retry, Common.TOAST_SHORT_TIME);
						finish();
					}
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				System.out.println("--------->scan failed");
				handler2.sendEmptyMessage(SCAN_FAILED);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			code = data.getStringExtra("code");
//			dialog = ProgressDialog.show(this, getString(R.string.loading___), getString(R.string.is_check_code), true, false);
			dialog = CustomProgressDialog.show(this, getString(R.string.is_check_code), false, null);
			//这里开启线程的目的，由于使用startactivityforresult模式所以一旦返回过来数据之后，surfaceView会在create前先destroy了，造成第二次进入的时候，initcamera不成功，所以加个线程，等待create成功
			new Thread(){
				public void run() {
					//while循环只用来等待surfaceview创建成功
					while (!hasSurface) {
						try {
							sleep(100);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					try {
						sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (hasSurface) {
						Message message = handler2.obtainMessage();
						message.what = CHECK_CODE;
						message.obj = code;
						System.out.println("------------------>"+code);
						handler2.sendMessage(message);
						
					}
				};
			}.start();
			
		}
	}
	
	
	
	
}