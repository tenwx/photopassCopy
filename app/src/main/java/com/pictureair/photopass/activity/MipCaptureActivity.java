package com.pictureair.photopass.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.CouponInfo;
import com.pictureair.photopass.eventbus.ScanInfoEvent;
import com.pictureair.photopass.util.API2;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.CouponTool;
import com.pictureair.photopass.util.DealCodeUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.EditTextWithClear;
import com.pictureair.photopass.widget.PWToast;
import com.pictureair.photopass.zxing.camera.AmbientLightManager;
import com.pictureair.photopass.zxing.camera.BeepManager;
import com.pictureair.photopass.zxing.camera.CameraManager;
import com.pictureair.photopass.zxing.decoding.CaptureActivityHandler;
import com.pictureair.photopass.zxing.decoding.InactivityTimer;
import com.pictureair.photopass.zxing.decoding.OnDealCodeListener;
import com.pictureair.photopass.zxing.view.ViewfinderView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Vector;

import de.greenrobot.event.EventBus;

/**
 * Initial the camera
 *
 * @author Talon
 */
public class MipCaptureActivity extends BaseActivity implements Callback,View.OnClickListener, OnDealCodeListener {
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    private AmbientLightManager ambientLightManager;
    private SurfaceView surfaceView;
    private View navigationView;
    private String code;

    private int offset;

    private ImageView cursorImageView;//动画图片

    private EditTextWithClear inputCodeEdit;

    private TextView wordSpaceTextView, inputTipsTV;

    private Button ok;

    private PWToast newToast;

    private DealCodeUtil dealCodeUtil;
    private final Handler mipCaptureHandler = new MipCaptureHandler(this);

    private TextView tvScanQRCode ,tvScanPPPCode; //扫描QR码 和 PP+号码的按钮
    private int scanType = 1; //扫描方式。1，代表Qr码扫描。2，代表PP+卡扫描。  默认进来是扫描QR码
    private TextView tvScanQRcodeTips;// QR码的提示字体。
    private RelativeLayout manulTabRL;

    private boolean mNoStoragePermission;
    private static final int REQUEST_CAMERA_PERMISSION = 3;
    private static final int FINISH_CURRENT_ACTIVITY = 4;
    private boolean mIsAskCameraPermission = false;

    @Override
    public void decodeSuccess(Result result) {
        if (!isPWProgressDialogShowing()) {//处理扫描结果
            inactivityTimer.onActivity();
            beepManager.playBeepSoundAndVibrate();
            String resultString = result.getText();
            PictureAirLog.out("scan result = " + resultString);
            if (resultString.contains("vid=")) { //包含vid
                code = resultString.substring(resultString.lastIndexOf("vid=") + 4, resultString.length());  //截取字符串。

            } else if (resultString.contains("VID=")) {//包含VID
                code = resultString.substring(resultString.lastIndexOf("VID=") + 4, resultString.length());  //截取字符串。

            } else if (resultString.contains("promoid=")) {//包含promoid   https://www.disneyphotopass.com.cn/?src=coupon&promoid=xxxxxxxxxxxxxxxx
                code = resultString.substring(resultString.lastIndexOf("promoid=") + 8, resultString.length());//promoid仅仅是抵用券的code

            } else if (resultString.length() >= 16 && resultString.length() <= 22 && AppUtil.isNumeric(resultString)) {//不包含vid，但是属于16-22位之间，并且都是纯数字
                code = resultString;

            } else if (getIntent().getStringExtra("type") == null && resultString.contains("chid=") && resultString.contains("uid=")) {//只有故事页面进入，才会判断递推号
                code = resultString.substring(resultString.lastIndexOf("chid=") + 5, resultString.length());  //截取字符串。
                code = code.replace("uid=", "");//这个时候拿到的数据就是：    chid&uid

            } else {//无效的卡号
                newToast.setTextAndShow(R.string.http_error_code_6136, Common.TOAST_SHORT_TIME);
                mipCaptureHandler.sendEmptyMessageDelayed(FINISH_CURRENT_ACTIVITY, 200);
                return;
            }
            PictureAirLog.out("code：：：" + code);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showPWProgressDialog();
                }
            });
            if (code.contains("&")) {//递推
                dealCodeUtil.startDealChidCode(code);
            } else {//正常code
                dealCodeUtil.startDealCode(code);
            }
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
                dismissPWProgressDialog();
                PictureAirLog.out("scan failed----->");
                if (scanType == 1) {//如果是扫描tab，则返回上一个页面
                    mipCaptureHandler.sendEmptyMessageDelayed(FINISH_CURRENT_ACTIVITY, 200);
                }
                break;

            case DealCodeUtil.DEAL_CODE_SUCCESS:
                dismissPWProgressDialog();

                if (msg.obj != null) {
                    Bundle bundle = (Bundle) msg.obj;
                    PictureAirLog.out("status-------->" + bundle.getInt("status"));
                    if (bundle.getInt("status") == DealCodeUtil.STATE_RETURN_MSG) {//获取抵用券对象，并且使用eventbus返回
                        EventBus.getDefault().post(new ScanInfoEvent(0, bundle.getString("result"), false, getIntent().getStringExtra("type"), (CouponInfo) bundle.getSerializable("coupon")));

                    } else if (bundle.getInt("status") == DealCodeUtil.STATE_ADD_PPP_TO_USER_NOT_RETURN_SUCCESS) {//扫描ppp并且成功绑定到用户
                        //进入ppp页面
                        Intent intent2 = new Intent(MipCaptureActivity.this, MyPPPActivity.class);
                        API2.PPPlist.clear();
                        intent2.putExtra("upgradePP", true);
                        startActivity(intent2);

                    } else if (bundle.getInt("status") == DealCodeUtil.STATE_ADD_PP_TO_USER_NOT_RETURN_SUCCESS) {//扫码pp并且成功绑定到用户
                        SPUtils.put(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.NEED_FRESH, true);
                        int currentPPCount = SPUtils.getInt(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.PP_COUNT, 0);
                        SPUtils.put(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.PP_COUNT, currentPPCount + 1);
                        if (!TextUtils.isEmpty(getIntent().getStringExtra("from"))) {//从一日通过来
                            AppManager.getInstance().killActivity(MyPPPActivity.class);
                            MyApplication.getInstance().setMainTabIndex(0);
                            //为了触发story页面刷新，因此需要eventbus
                            EventBus.getDefault().post(new ScanInfoEvent(0, "", false, "", null));
                        }

                    } else if (bundle.getInt("status") == DealCodeUtil.STATE_ADD_COUPON_TO_USER_NOT_RETURN_SUCCESS) {//扫码coupon并且成功绑定到用户
                        //进入coupon页面
                        Intent intent2 = new Intent(MipCaptureActivity.this, CouponActivity.class);
                        intent2.putExtra(CouponTool.ACTIVITY_ME, CouponTool.ACTIVITY_ME);
                        startActivity(intent2);
                    }
                    finish();
//                } else {
//                    mipCaptureHandler.sendEmptyMessageDelayed(FINISH_CURRENT_ACTIVITY, 200);
                }
                break;

            case DealCodeUtil.DEAL_CHID_CODE_SUCCESS:
                dismissPWProgressDialog();
                Bundle bundle = (Bundle) msg.obj;
                Intent intent = new Intent(MipCaptureActivity.this, SubmitOrderActivity.class);
                intent.putExtra("orderinfo", bundle.getSerializable("orderinfo"));
                intent.putExtra("chid", code);
                startActivity(intent);
                finish();
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//保存屏幕不休眠
        //保证相机以全屏的方式显示，保证图像不会拉伸
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//使用沉浸式
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏（虚拟按键）
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        } else {//全屏设置
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_capture);
        String mode = getIntent().getStringExtra("mode");
        if (TextUtils.isEmpty(mode)) {
            scanType = 1; // 每次进入的时候 更改为扫描QR码的方式。
        } else {
            scanType = 2;
        }
        checkStoragePermissionAndCopyData();

        newToast = new PWToast(this);
        surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        CameraManager.init(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        navigationView = findViewById(R.id.bottom_status_bar_view);
        setTopLeftValueAndShow(R.drawable.back_blue, true);
        setTopTitleShow(R.string.auto);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        ambientLightManager = new AmbientLightManager(this);
        dealCodeUtil = new DealCodeUtil(this, getIntent(), false, mipCaptureHandler);

        manulTabRL = (RelativeLayout) findViewById(R.id.manul_input_rl);
        tvScanQRcodeTips = (TextView) findViewById(R.id.tv_scan_qr_code_tips);
        tvScanQRCode = (TextView) findViewById(R.id.tv_scan_qr_code);
        tvScanPPPCode = (TextView) findViewById(R.id.tv_scan_ppp_code);
        inputTipsTV = (TextView) findViewById(R.id.tv_manul_input_intro);
        tvScanQRCode.setOnClickListener(this);
        tvScanPPPCode.setOnClickListener(this);

        cursorImageView = (ImageView) findViewById(R.id.cursor);
        offset = ScreenUtil.getScreenWidth(this) / 2;// 获取分辨率宽度

        inputCodeEdit = (EditTextWithClear) findViewById(R.id.input_manaul_edittext);
        wordSpaceTextView = (TextView) findViewById(R.id.scancodetextview);
        wordSpaceTextView.setTypeface(MyApplication.getInstance().getFontBold());
        wordSpaceTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

        ok = (Button) findViewById(R.id.sure);
        ok.setOnClickListener(this);

        inputCodeEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                String editString = "";
                for (int i = 0; i < s.length(); i++) {
                    editString += " " + s.charAt(i);
                }
//                PictureAirLog.out("editString---->" + editString);
                if (0 == inputCodeEdit.getText().toString().length()) {
                    if (wordSpaceTextView.isShown()) {
                        wordSpaceTextView.scrollTo(0, 0);//保证放大的textview正常显示
                        wordSpaceTextView.setVisibility(View.INVISIBLE);
                        wordSpaceTextView.setText(editString.trim());
                    }
                }else {
                    if (!wordSpaceTextView.isShown()) {
                        wordSpaceTextView.setVisibility(View.VISIBLE);
                    }
                    wordSpaceTextView.setText(editString.trim());
                }

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });

        if (!TextUtils.isEmpty(getIntent().getStringExtra("type"))
                && getIntent().getStringExtra("type").equals("coupon")) {//coupon
            tvScanQRcodeTips.setText(R.string.scan_coupon_intro);
            inputTipsTV.setText(R.string.scan_coupon_intro2);
        }

        //设置顶部状态栏和底部虚拟按键的对应View的高度
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View topStatusBar = findViewById(R.id.status_bar_view);
            ViewGroup.LayoutParams params = topStatusBar.getLayoutParams();
            params.height = ScreenUtil.getStatusBarHeight(this);
            topStatusBar.setLayoutParams(params);
        }

        int navigationHeight = ScreenUtil.getNavigationHeight(this);
        if (navigationHeight > 0) {
            ViewGroup.LayoutParams navigationParams = navigationView.getLayoutParams();
            navigationParams.height = navigationHeight;
            navigationView.setLayoutParams(navigationParams);
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setScanMode(scanType);
    }

    private void setScanMode(int mode){
        if (mode == 1) {
            viewfinderView.setVisibility(View.VISIBLE);
            tvScanQRcodeTips.setVisibility(View.VISIBLE);
            navigationView.setVisibility(View.VISIBLE);
            manulTabRL.setVisibility(View.GONE);

            tvScanQRCode.setBackgroundColor(ContextCompat.getColor(this, R.color.pp_light_gray_background));
            tvScanPPPCode.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
            navigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
        } else {
            manulTabRL.setVisibility(View.VISIBLE);
            viewfinderView.setVisibility(View.GONE);
            tvScanQRcodeTips.setVisibility(View.GONE);

            tvScanPPPCode.setBackgroundColor(ContextCompat.getColor(this, R.color.pp_light_gray_background));
            tvScanQRCode.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
            navigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        }

    }

    private void changeIndexTab(int mode) {
        //mode 为1，说明是第0个位置，是从第1个位置过来
        //mode 为2，说明是第1个位置，是从第0个位置过来
        //mode ，说明是第（mode - 1）个位置，是从第（Math.abs(1 - (mode - 1))）个位置过来
        Animation animation = new TranslateAnimation(offset * Math.abs(1 - (mode - 1)), offset * (mode - 1), 0, 0);
        animation.setFillAfter(true);
        animation.setDuration(300);
        cursorImageView.startAnimation(animation);
    }

    private void hideInputMethodManager(View v) {
        // TODO Auto-generated method stub
        /*隐藏软键盘*/
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
    }

    // 点击响应方法
    @Override
    public void onClick(View view) {
        if (handler != null) {
            switch (view.getId()) {
                case R.id.tv_scan_qr_code:
                    scanType = 1;
                    handler.setScanType(scanType);
                    setScanMode(scanType);
                    changeIndexTab(scanType);
                    break;

                case R.id.tv_scan_ppp_code:
                    scanType = 2;
                    handler.setScanType(scanType);
                    setScanMode(scanType);
                    changeIndexTab(scanType);
                    break;

                case R.id.sure://手动输入页面的确定
                    if ("".equals(inputCodeEdit.getText().toString())) {
                        newToast.setTextAndShow(R.string.http_error_code_6136, Common.TOAST_SHORT_TIME);
                    } else {
                        //如果有键盘显示，把键盘取消掉
                        hideInputMethodManager(view);
                        showPWProgressDialog();
                        dealCodeUtil.startDealCode(inputCodeEdit.getText().toString().toUpperCase(), true);
                    }
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsAskCameraPermission) {
            mIsAskCameraPermission = false;
            return;
        }
        PictureAirLog.out("resume==============");

        beepManager.updatePrefs();
        ambientLightManager.start(CameraManager.get());
        inactivityTimer.onResume();
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            requestCameraPermissionAndInit();
        } else {
            surfaceHolder.addCallback(this);
        }
        decodeFormats = null;
        characterSet = null;
    }

    @Override
    protected void onPause() {
        PictureAirLog.out("----------pause");
        if (handler != null) {
            PictureAirLog.out("need quitSynchronously");
            handler.quitSynchronously();
            handler = null;
        }

        inactivityTimer.onPause();
        ambientLightManager.stop();
        CameraManager.get().closeDriver();
        if (!hasSurface) {
            surfaceView.getHolder().removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PictureAirLog.out("-----------destroy");
        inactivityTimer.shutdown();
        mipCaptureHandler.removeCallbacksAndMessages(null);
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            PictureAirLog.out("no surfaceholder");
            return;
        }
        if (CameraManager.get().isOpen()) {
            PictureAirLog.out("camera is open");
            return;
        }
        try {
            CameraManager.get().openDriver(surfaceHolder);
        } catch (IOException ioe) {
            PictureAirLog.out("meiyou dakai xiangji1");
            return;
        } catch (RuntimeException e) {
            newToast.setTextAndShow(R.string.camera_closed, Common.TOAST_SHORT_TIME);
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

    @Override
    public void TopViewClick(View view) {
        super.TopViewClick(view);
        switch (view.getId()) {
            case R.id.topLeftView:
                finish();
                break;
            default:
                break;
        }
    }

    private void checkStoragePermissionAndCopyData() {
        mNoStoragePermission = AppUtil.checkPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
                    newToast.setTextAndShow(R.string.camera_closed, Common.TOAST_SHORT_TIME);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}