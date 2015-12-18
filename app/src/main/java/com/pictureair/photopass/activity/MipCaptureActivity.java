package com.pictureair.photopass.activity;

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
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.DealCodeUtil;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.zxing.camera.CameraManager;
import com.pictureair.photopass.zxing.decoding.CaptureActivityHandler;
import com.pictureair.photopass.zxing.decoding.InactivityTimer;
import com.pictureair.photopass.zxing.view.ViewfinderView;

import java.io.IOException;
import java.util.Vector;

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
	private DealCodeUtil dealCodeUtil;

	private Handler handler2 = new Handler(){
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
				case DealCodeUtil.DEAL_CODE_FAILED:
					if (dialog.isShowing()) {
						dialog.dismiss();
					}
					if (msg.obj != null) {//从ppp页面过来，需要返回
						Intent intent2 = new Intent();
						intent2.putExtra("result", "failed");
						intent2.putExtra("errorType", Integer.valueOf(msg.obj.toString()));
						setResult(RESULT_OK, intent2);
					}
					finish();
					break;

				case DealCodeUtil.DEAL_CODE_SUCCESS:
					if (dialog.isShowing()) {
						dialog.dismiss();
					}

					if (msg.obj != null) {//从ppp过来
						Intent intent2 = new Intent();
						Bundle bundle = (Bundle) msg.obj;
						if (bundle.getInt("status") == 1) {
							intent2.putExtra("result", bundle.getString("result"));
							setResult(RESULT_OK, intent2);
						} else if (bundle.getInt("status") == 2) {//将pp码返回
							intent2.putExtra("result", bundle.getString("result"));
							intent2.putExtra("hasBind", bundle.getBoolean("hasBind"));
							setResult(RESULT_OK, intent2);
						} else if (bundle.getInt("status") == 3) {
							intent2.setClass(MipCaptureActivity.this, MyPPPActivity.class);
							API1.PPPlist.clear();
							startActivity(intent2);
						} else if (bundle.getInt("status") == 4){
							Editor editor = sp.edit();
							editor.putBoolean(Common.NEED_FRESH, true);
							editor.putInt(Common.PP_COUNT, sp.getInt(Common.PP_COUNT, 0) + 1);
							editor.commit();
						} else if (bundle.getInt("status") == 5){
							intent2.putExtra("result", bundle.getString("result"));
							setResult(RESULT_OK, intent2);
						}
					}

					finish();
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
		newToast = new MyToast(this);
		sp = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
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
				Intent i = new Intent(MipCaptureActivity.this, InputCodeActivity.class);
				i.putExtra("needCallBack", true);
				startActivityForResult(i, MANUAL_INPUT_CODE);
			}
		});
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		dealCodeUtil = new DealCodeUtil(this, getIntent(), handler2);
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
				dialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
				dealCodeUtil.startDealCode(code);
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

}