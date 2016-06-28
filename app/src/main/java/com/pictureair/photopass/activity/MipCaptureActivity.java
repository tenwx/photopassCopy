package com.pictureair.photopass.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.pictureair.photopass.R;
import com.pictureair.photopass.eventbus.ScanInfoEvent;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.DealCodeUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.PWToast;
import com.pictureair.photopass.zxing.camera.CameraManager;
import com.pictureair.photopass.zxing.decoding.CaptureActivityHandler;
import com.pictureair.photopass.zxing.decoding.InactivityTimer;
import com.pictureair.photopass.zxing.decoding.OnDealCodeListener;
import com.pictureair.photopass.zxing.view.ScanView;
import com.pictureair.photopass.zxing.view.ViewfinderView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Vector;

import de.greenrobot.event.EventBus;

/**
 * Initial the camera
 *
 * @author Talon
 */
public class MipCaptureActivity extends BaseActivity implements Callback,View.OnClickListener, OnDealCodeListener {
    private TextView tvCenterHint;
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
    private RelativeLayout.LayoutParams rlp;
    private SurfaceView surfaceView;
    private SharedPreferences sp;
    private String code;

    private PWToast newToast;

    private CustomProgressDialog dialog;
    private DealCodeUtil dealCodeUtil;
    private final Handler mipCaptureHandler = new MipCaptureHandler(this);

    private TextView tvScanQRCode ,tvScanPPPCode; //扫描QR码 和 PP+号码的按钮
    private int scanType = 1; //扫描方式。1，代表Qr码扫描。2，代表PP+卡扫描。  默认进来是扫描QR码
    private ViewfinderView viewfinder_view; // QR码 扫描的矩形
    private TextView tvScanQRcodeTips;// QR码的提示字体。
    private RelativeLayout rlMask,rlLight; //蒙版, 高亮部分
    private ScanView ocrScanView;

    private boolean mNoStoragePermission;
    private static final int REQUEST_CAMERA_PERMISSION = 3;
    private static final int FINISH_CURRENT_ACTIVITY = 4;
    private boolean mIsAskCameraPermission = false;
    // 点击响应方法
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_scan_qr_code:
                if (handler != null) {
                    scanType = 1;
                    handler.setScanType(scanType);
                } else {
                    return;
                }
                ocrScanView.setVisibility(View.GONE);
                viewfinder_view.setVisibility(View.VISIBLE);
                tvScanQRcodeTips.setVisibility(View.VISIBLE);
                rlMask.setVisibility(View.GONE);

                tvScanQRCode.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(this, R.drawable.scan_qrcode_sel), null, null);
                tvScanQRCode.setTextColor(getResources().getColor(R.color.pp_blue));

                tvScanPPPCode.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(this, R.drawable.scan_pppcode_nor), null, null);
                tvScanPPPCode.setTextColor(getResources().getColor(R.color.white));
                break;
            case R.id.tv_scan_ppp_code:
                if (handler != null) {
                    scanType = 2;
                    handler.setScanType(scanType);
                } else {
                    return;
                }
                if (rlp == null) {
                    int height = ((surfaceView.getHeight() - ScreenUtil.dip2px(this, 52)) / 2 - tvScanPPPCode.getHeight() - 10) * 2;
                    int width = (int) (height / 85.0 * 54);
                    PictureAirLog.out("height---->" + height);
                    PictureAirLog.out("width---->" + width);
                    rlp = new RelativeLayout.LayoutParams(width, height);
                    rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
                    rlLight.setLayoutParams(rlp);
                    ViewGroup.LayoutParams layoutParams = tvCenterHint.getLayoutParams();
                    layoutParams.width = rlp.height - 20;
                    layoutParams.height = rlp.width - 20;
                    PictureAirLog.out("width---->" + layoutParams.width);
                    PictureAirLog.out("height---->" + layoutParams.height);
                    tvCenterHint.setLayoutParams(layoutParams);
                }

                ocrScanView.setVisibility(View.VISIBLE);
                rlMask.setVisibility(View.VISIBLE);
                viewfinder_view.setVisibility(View.GONE);
                tvScanQRcodeTips.setVisibility(View.GONE);

                tvScanPPPCode.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(this, R.drawable.scan_pppcode_sel), null, null);
                tvScanPPPCode.setTextColor(getResources().getColor(R.color.pp_blue));

                tvScanQRCode.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(this, R.drawable.scan_qrcode_nor), null, null);
                tvScanQRCode.setTextColor(getResources().getColor(R.color.white));
                break;
        }
    }

    @Override
    public void decodeSuccess(Result result, Bitmap bitmap) {
        if (dialog != null && dialog.isShowing()) {//不处理扫描结果

        } else {
            inactivityTimer.onActivity();
            playBeepSoundAndVibrate();
            String resultString = result.getText();
            PictureAirLog.out("scan result = " + resultString);
            if (resultString.contains("vid=")) { //包含vid
                code = resultString.substring(resultString.lastIndexOf("vid=") + 4, resultString.length());  //截取字符串。

            } else if (resultString.contains("VID=")) {//包含VID
                code = resultString.substring(resultString.lastIndexOf("VID=") + 4, resultString.length());  //截取字符串。

            } else if (resultString.length() >= 16 && resultString.length() <= 22 && AppUtil.isNumeric(resultString)) {//不包含vid，但是属于16-22位之间，并且都是纯数字
                code = resultString;

            } else {//无效的卡号
                newToast.setTextAndShow(R.string.http_error_code_6136, Common.TOAST_SHORT_TIME);
                mipCaptureHandler.sendEmptyMessageDelayed(FINISH_CURRENT_ACTIVITY, 200);
                return;
            }
            PictureAirLog.out("code：：：" + code);
            dialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
            dealCodeUtil.startDealCode(code);
        }
    }

    @Override
    public void decodeOCRSuccess(Bundle bundle) {
        if (bundle.getString("text") != null){ //跳转到确认的界面。
            Intent intent = new Intent();
            intent.putExtra("text", bundle.getString("text"));
            intent.putExtra("type", getIntent().getStringExtra("type"));
            intent.putExtra("bmpData", bundle.getByteArray("data"));
            intent.setClass(this, PPPCodeActivity.class);
            startActivity(intent);
        }
    }


    private static class MipCaptureHandler extends Handler{
        private final WeakReference<MipCaptureActivity> mActivity;

        public MipCaptureHandler(MipCaptureActivity activity){
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivity.get() == null) {
                return;
            }
            mActivity.get().dealHandler(msg);
        }
    }

    /**
     * 处理Message
     * @param msg
     */
    private void dealHandler(Message msg) {
        switch (msg.what) {
            case DealCodeUtil.DEAL_CODE_FAILED:
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                PictureAirLog.out("scan failed----->");
                if (msg.obj != null) {//从ppp,PP页面过来，需要返回
                    EventBus.getDefault().post(new ScanInfoEvent(Integer.valueOf(msg.obj.toString()), "failed", false, getIntent().getStringExtra("type")));
                    finish();
                } else {
                    mipCaptureHandler.sendEmptyMessageDelayed(FINISH_CURRENT_ACTIVITY, 200);
                }
                break;

            case DealCodeUtil.DEAL_CODE_SUCCESS:
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }

                if (msg.obj != null) {
                    Bundle bundle = (Bundle) msg.obj;
                    PictureAirLog.out("status-------->" + bundle.getInt("status"));
                    if (bundle.getInt("status") == DealCodeUtil.STATE_RETURN_MSG) {
                        EventBus.getDefault().post(new ScanInfoEvent(0, bundle.getString("result"), false, getIntent().getStringExtra("type")));

                    } else if (bundle.getInt("status") == DealCodeUtil.STATE_ADD_CODE_TO_USER_NOT_RETURN_SUCCESS) {
                        Intent intent2 = new Intent(MipCaptureActivity.this, MyPPPActivity.class);
                        API1.PPPlist.clear();
                        startActivity(intent2);

                    } else if (bundle.getInt("status") == DealCodeUtil.STATE_ADD_PP_TO_USER_NOT_RETURN_SUCCESS) {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putBoolean(Common.NEED_FRESH, true);
                        editor.putInt(Common.PP_COUNT, sp.getInt(Common.PP_COUNT, 0) + 1);
                        editor.commit();

                    }
                    finish();
                } else {
                    mipCaptureHandler.sendEmptyMessageDelayed(FINISH_CURRENT_ACTIVITY, 200);
                }
                break;

            case FINISH_CURRENT_ACTIVITY:
                finish();
                break;

            default:
                break;
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        scanType = 1; // 每次进入的时候 更改为扫描QR码的方式。
        checkStoragePermissionAndCopyData();
        ocrScanView = (ScanView) findViewById(R.id.scan_view_line_ocr);
        tvCenterHint = (TextView) findViewById(R.id.tv_center_hint);
//        tvCenterHint.setRotation(90);

        newToast = new PWToast(this);
        sp = getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, MODE_PRIVATE);
        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        CameraManager.init(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        setTopLeftValueAndShow(R.drawable.back_white, true);
        setTopTitleShow(R.string.auto);
        setTopRightValueAndShow(R.drawable.manual_input,true);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        dealCodeUtil = new DealCodeUtil(this, getIntent(), false, mipCaptureHandler);

        //OCR 识别需要用到的组件。
        viewfinder_view = (ViewfinderView) findViewById(R.id.viewfinder_view);
        tvScanQRcodeTips = (TextView) findViewById(R.id.tv_scan_qr_code_tips);
        tvScanQRCode = (TextView) findViewById(R.id.tv_scan_qr_code);
        tvScanPPPCode = (TextView) findViewById(R.id.tv_scan_ppp_code);
        tvScanQRCode.setOnClickListener(this);
        tvScanPPPCode.setOnClickListener(this);

        rlMask = (RelativeLayout) findViewById(R.id.rl_mask);
        rlLight = (RelativeLayout) findViewById(R.id.rl_light);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsAskCameraPermission) {
            mIsAskCameraPermission = false;
            return;
        }
        PictureAirLog.out("resume==============");

        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            requestCameraPermissionAndInit();
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
        PictureAirLog.out("----------pause");
        if (handler != null) {
            PictureAirLog.out("need quitSynchronously");
            handler.quitSynchronously();
            handler = null;
        }
        PictureAirLog.out("need closeDriver");
        CameraManager.get().closeDriver();
        PictureAirLog.out("pause -----> done");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PictureAirLog.out("-----------destroy");
        inactivityTimer.shutdown();
        if (mediaPlayer != null) {

            if (mediaPlayer.isPlaying()) {

                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }
        mediaPlayer = null;
        mipCaptureHandler.removeCallbacksAndMessages(null);
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            PictureAirLog.out("meiyou dakai xiangji1");
            return;
        } catch (RuntimeException e) {
            newToast.setTextAndShow(R.string.camera_closed_jump_to_manual, Common.TOAST_SHORT_TIME);
            Intent intent = new Intent();
            intent.setClass(this, InputCodeActivity.class);
            intent.putExtra("type", getIntent().getStringExtra("type"));
            startActivity(intent);
            finish();
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(getApplicationContext(), decodeFormats, characterSet, viewfinderView, scanType, mNoStoragePermission);
            handler.setOnDealCodeListener(this);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        PictureAirLog.out("----------holder create");
        if (!hasSurface) {
            hasSurface = true;
            requestCameraPermissionAndInit();

        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        PictureAirLog.out("---------holder destroy");
        hasSurface = false;

    }

    public Handler getHandler() {
        return handler;
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


    @Override
    public void TopViewClick(View view) {
        super.TopViewClick(view);
        switch (view.getId()) {
            case R.id.topLeftView:
                finish();
                break;
            case R.id.topRightView:
                //跳转到输入  code 的界面。
                Intent i = new Intent(MipCaptureActivity.this, InputCodeActivity.class);
                i.putExtra("type", getIntent().getStringExtra("type"));
                startActivity(i);
            default:
                break;
        }
    }

    /**
     * 复制文件 到 SD 卡中
     * @param strOutFileName
     * @throws IOException
     */
    private void copyDataToSD(String strOutFileName) throws IOException
    {
        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(strOutFileName);
        myInput = this.getAssets().open("ocrdata/eng.traineddata");
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while(length > 0)
        {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
    }

    private void copyOCRDataToStorage() {
        File tessdata = new File(Common.OCR_PATH); //创建文件夹。
        if (!tessdata.exists()){
            tessdata.mkdirs();
        }
        // 移动OCR 需要的data 到SD卡上。
        if (!(new File(Common.OCR_DATA_PATH)).exists()){
            try {
                copyDataToSD(Common.OCR_DATA_PATH);
            }catch (Exception e){

            }
        }
    }

    private void checkStoragePermissionAndCopyData() {
        if (AppUtil.checkPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            copyOCRDataToStorage();
            mNoStoragePermission = true;
        }else{
            mNoStoragePermission = false;
        }
    }

    private void requestCameraPermissionAndInit() {
        if (!AppUtil.checkPermission(getApplicationContext(), Manifest.permission.CAMERA)) {
            mIsAskCameraPermission = true;
            ActivityCompat.requestPermissions(MipCaptureActivity.this,new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            return;
        }
        initCamera(surfaceView.getHolder());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (Manifest.permission.CAMERA.equalsIgnoreCase(permissions[0]) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initCamera(surfaceView.getHolder());
                } else {
                    newToast.setTextAndShow(R.string.camera_closed_jump_to_manual, Common.TOAST_SHORT_TIME);
                    Intent intent = new Intent();
                    intent.setClass(this, InputCodeActivity.class);
                    intent.putExtra("type", getIntent().getStringExtra("type"));
                    startActivity(intent);
                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}