package com.pictureair.photopass.activity;

import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.widget.VideoPlayerView;
import com.pictureair.photopass.widget.VideoPlayerView.MySizeChangeLinstener;
import com.pictureair.photopass.R;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;


/**
 * 播放视频（需要优化）
 * @author bass
 *
 */
public class VideoPlayerActivity extends Activity implements OnClickListener {
	private final static String TAG = "VideoPlayerActivity";
	private SeekBar seekBar = null;
	private TextView durationTextView = null;
	private TextView playedTextView = null;
	private ImageButton btnPlayOrStop = null;
	private RelativeLayout rlHead;
	private LinearLayout llEnd;
	
	private Context context;
	private LinearLayout llControler,llShow; 
	private VideoPlayerView vv = null;

	private int playedTime;// 最小化 保存播放时间
	private static int screenWidth = 0;
	private static int screenHeight = 0;
	private static int controlHeight = 0;
	private final static int TIME = 3000;
	private boolean isControllerShow = true;
	private boolean isPaused = false;
	private final static int PROGRESS_CHANGED = 0;
	private final static int HIDE_CONTROLER = 1;
	
	public boolean isOnline = Common.isOnline;//测试
	
	private Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PROGRESS_CHANGED:// 进度改变
				int i = vv.getCurrentPosition();
				seekBar.setProgress(i);
				if(isOnline){
					int j = vv.getBufferPercentage();
					seekBar.setSecondaryProgress(j * seekBar.getMax() / 100);
				}else{
					seekBar.setSecondaryProgress(0);
				}
				
				i /= 1000;
				int minute = i / 60;
				int hour = minute / 60;
				int second = i % 60;
				minute %= 60;
				// playedTextView.setText(String.format("%02d:%02d:%02d", hour,
				// minute, second));
				playedTextView.setText(String.format("%02d:%02d", minute,
						second));
				sendEmptyMessageDelayed(PROGRESS_CHANGED, 100);
				break;
			case HIDE_CONTROLER:
				hideController();
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_player);
		context = this;
		rlHead = (RelativeLayout) findViewById(R.id.ll_head);
		llEnd = (LinearLayout) findViewById(R.id.ll_end);
		llShow = (LinearLayout) findViewById(R.id.ll_show);
		llShow.setOnClickListener(this);
		// 控制栏的
		llControler = (LinearLayout) findViewById(R.id.ll_controler);
		durationTextView = (TextView) findViewById(R.id.duration);
		playedTextView = (TextView) findViewById(R.id.has_played);
		//
		btnPlayOrStop = (ImageButton)findViewById(R.id.btn_play_or_stop);
		seekBar = (SeekBar) findViewById(R.id.seekbar);
		vv = (VideoPlayerView) findViewById(R.id.vv);

		btnPlayOrStop.setImageResource(R.drawable.play);
		btnPlayOrStop.setAlpha(0xBB);

		myOnError();// 播放错误
		myVVSizeChangeLinstener();// 屏幕大小发生改变的情况
		initBtnEvent();// 所有按钮的事件
		getScreenSize();// 给底下菜单的布局
		initSeekBarEvent();// 进度条调整
//		 myGestureDetectr();//手势检测器
		initVVEvent();// VideoPlayerView类的回调
		startVideo();// 开始播放视频
	}

	private void startVideo() {
		vv.setVideoPath(Common.DATA_VIDEO);
		cancelDelayHide();
		hideControllerDelay();
	}

	private void initVVEvent() {
		vv.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer arg0) {
				setVideoScale(SCREEN_DEFAULT);// 按比例（全屏）
				// setVideoScale(SCREEN_FULL);//全屏（会改变视频尺寸）
				if (isControllerShow) {
					showController();
				}
				int i = vv.getDuration();
				Log.d("onCompletion", "" + i);
				seekBar.setMax(i);
				i /= 1000;
				int minute = i / 60;
				int hour = minute / 60;
				int second = i % 60;
				minute %= 60;
				durationTextView.setText(String.format("%02d:%02d", minute,
						second));
				// durationTextView.setText(String.format("%02d:%02d:%02d",
				// hour, minute, second));

				vv.start();
				btnPlayOrStop.setImageResource(R.drawable.pause);
				hideControllerDelay();
				myHandler.sendEmptyMessage(PROGRESS_CHANGED);
			}
		});

		vv.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer arg0) {
//				vv.stopPlayback();
//				startVideo();
				vv.pause();
				btnPlayOrStop.setImageResource(R.drawable.play);
				cancelDelayHide();
				showController();
				isPaused = true;
			}
		});
	}

	private void initSeekBarEvent() {
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekbar, int progress,
					boolean fromUser) {
				if (fromUser) {
					if(!isOnline){
						vv.seekTo(progress);
					}
				}

			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				myHandler.removeMessages(HIDE_CONTROLER);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				myHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
			}
		});
	}

	private void initBtnEvent() {
//		btnPlayOrStop.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				cancelDelayHide();
//				if (isPaused) {
//					vv.start();
//					btnPlayOrStop.setImageResource(R.drawable.pause);
//					hideControllerDelay();
//				} else {
//					vv.pause();
//					btnPlayOrStop.setImageResource(R.drawable.play);
//				}
//				isPaused = !isPaused;
//			}
//		});
	}

	private void myVVSizeChangeLinstener() {
		vv.setMySizeChangeLinstener(new MySizeChangeLinstener() {
			@Override
			public void doMyThings() {
				setVideoScale(SCREEN_DEFAULT);
			}
		});
	}

	private void myOnError() {
		vv.setOnErrorListener(new OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				vv.stopPlayback();
				return false;
			}

		});
	}

	@Override
	protected void onPause() {
		playedTime = vv.getCurrentPosition();
		vv.pause();
		btnPlayOrStop.setImageResource(R.drawable.play);
		super.onPause();
	}

	@Override
	protected void onResume() {
		vv.seekTo(playedTime);
		vv.start();
		if (vv.isPlaying()) {
			btnPlayOrStop.setImageResource(R.drawable.pause);
			hideControllerDelay();
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		myHandler.removeMessages(PROGRESS_CHANGED);
		myHandler.removeMessages(HIDE_CONTROLER);

		if (vv.isPlaying()) {
			vv.stopPlayback();
		}
		super.onDestroy();
	}

	/**
	 * 给底下菜单的布局
	 */
	private void getScreenSize() {
		Display display = getWindowManager().getDefaultDisplay();
		screenHeight = display.getHeight();
		screenWidth = display.getWidth();
		controlHeight = screenHeight / 10;// 控制栏高的布局
	}

	private void hideControllerDelay() {
		myHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
	}
	
	private void hideController() {
		if (llControler.getVisibility() == View.VISIBLE) {
			llControler.setVisibility(View.GONE);
			btnPlayOrStop.setVisibility(View.GONE);
		}
	}

	private void showController() {
		llControler.setVisibility(View.VISIBLE);
		btnPlayOrStop.setVisibility(View.VISIBLE);
		isControllerShow = true;
	}

	private void cancelDelayHide() {
		myHandler.removeMessages(HIDE_CONTROLER);
	}

	private final static int SCREEN_FULL = 0;
	private final static int SCREEN_DEFAULT = 1;

	// 设置可以全屏
	private void setVideoScale(int flag) {
		switch (flag) {
		case SCREEN_FULL:
			Log.d(TAG, "screenWidth: " + screenWidth + " screenHeight: "
					+ screenHeight);
			vv.setVideoScale(screenWidth, screenHeight);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			break;

		case SCREEN_DEFAULT:
			int videoWidth = vv.getVideoWidth();
			int videoHeight = vv.getVideoHeight();
			int mWidth = screenWidth;
			int mHeight = screenHeight - 25;
			if (videoWidth > 0 && videoHeight > 0) {
				if (videoWidth * mHeight > mWidth * videoHeight) {
					mHeight = mWidth * videoHeight / videoWidth;
				} else if (videoWidth * mHeight < mWidth * videoHeight) {
					mWidth = mHeight * videoWidth / videoHeight;
				} else {

				}
			}
			vv.setVideoScale(screenWidth, screenHeight);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			break;
		}
	}
	
	/**
	 * 得到屏幕宽高
	 */
	int height;
	int width;
	public void getDisplayMetrics(){
		DisplayMetrics dm = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(dm);
	    width = dm.widthPixels;    //得到宽度
	    height = dm.heightPixels;  //得到高度
	}

	/**
	 * 当横竖屏切换的时候会直接调用onCreate方法中的 onConfigurationChanged方法
	 */

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		getScreenSize();
//		if (isControllerShow) {
//			cancelDelayHide();
//			hideController();
//			showController();
//			hideControllerDelay();
//		}
		getDisplayMetrics();
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			rlHead.setVisibility(View.GONE);
			llEnd.setVisibility(View.GONE);
			
			setVideoScale(SCREEN_FULL);
			vv.start();
			btnPlayOrStop.setImageResource(R.drawable.pause);
//			cancelDelayHide();
			hideControllerDelay();
//			Toast.makeText(context, " 横屏", 100).show();
		} else {
			rlHead.setVisibility(View.VISIBLE);
			llEnd.setVisibility(View.VISIBLE);
			setVideoScale(SCREEN_DEFAULT);
			vv.start();
			btnPlayOrStop.setImageResource(R.drawable.pause);
//			cancelDelayHide();
			hideControllerDelay();
			
//			Toast.makeText(context, " 竖屏", 100).show();
		}
		isPaused = false;
		
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.ll_show:
			// 单次处理
			if (isPaused) {
				vv.start();
				btnPlayOrStop.setImageResource(R.drawable.pause);
				cancelDelayHide();
				hideControllerDelay();
			} else {
				vv.pause();
				btnPlayOrStop.setImageResource(R.drawable.play);
				cancelDelayHide();
				showController();
			}
			isPaused = !isPaused;
			break;

		default:
			break;
		}
	}

	 /*****************************************************************************************************************
	 * 全屏点触事件****
	 */
//	private void myGestureDetectr() {
//		mGestureDetector = new GestureDetector(new SimpleOnGestureListener() {
//
//			@Override
//			public boolean onDoubleTap(MotionEvent e) {
//				// 按两次
//				if (isFullScreen) {
//					setVideoScale(SCREEN_DEFAULT);
//				} else {
//					setVideoScale(SCREEN_FULL);
//				}
//				isFullScreen = !isFullScreen;
//				Log.d(TAG, "onDoubleTap");
//
//				if (isControllerShow) {
//					showController();
//				}
//				return true;
//			}
//
//			@Override
//			public boolean onSingleTapConfirmed(MotionEvent e) {
//				// 单次处理
//				if (!isControllerShow) {
//					showController();
//					hideControllerDelay();
//				} else {
//					cancelDelayHide();
//					hideController();
//				}
//				return true;
//			}
//
//			@Override
//			public void onLongPress(MotionEvent e) {
//				// 长按处理
//				if (isPaused) {
//					vv.start();
//					btnPlayOrStop.setImageResource(R.drawable.pause);
//					cancelDelayHide();
//					hideControllerDelay();
//				} else {
//					vv.pause();
//					btnPlayOrStop.setImageResource(R.drawable.play);
//					cancelDelayHide();
//					showController();
//				}
//				isPaused = !isPaused;
//			}
//		});
//	}
//
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//
//		boolean result = mGestureDetector.onTouchEvent(event);
//
//		if (!result) {
//			if (event.getAction() == MotionEvent.ACTION_UP) {
//
//			}
//			result = super.onTouchEvent(event);
//		}
//		return result;
//	}
	 /*****************************************************************************************************/

}
