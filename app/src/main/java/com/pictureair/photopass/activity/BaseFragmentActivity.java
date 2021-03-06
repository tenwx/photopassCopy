package com.pictureair.photopass.activity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.UmengUtil;
import com.pictureair.photopass.widget.PWProgressDialog;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by milo on 15/12/16.
 */
public class BaseFragmentActivity extends RxAppCompatActivity {
    private Context context;

    //Top bar
    private TextView topLeftTv, topTitle, topRightTv;
    private ImageView topLeftIv, topRightIv;
    private LinearLayout topLeftView, topRightView;
    private PWProgressDialog pwProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.getInstance().addActivity(this);
        this.context = this;
        AppUtil.initLanguage(context);
        createPWProgressDialog();
        immersiveMode();
    }

    /**
     * 沉浸式状态栏设置
     */
    private void immersiveMode() {
        //在layout中设置,防止状态栏和内容重叠在一起
        //android:fitsSystemWindows="true"
        //android:clipToPadding="true"
        //设置状态栏和导航栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 统计页面、时长
        if (context.getClass().equals(MainTabActivity.class)) {
            UmengUtil.onResume(context, true);
        } else {
            UmengUtil.onResume(context, false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (context.getClass().equals(MainTabActivity.class)) {
            UmengUtil.onPause(context, true);
        } else {
            UmengUtil.onPause(context, false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.getInstance().killActivity(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    /**
     * 判断网络是否连接
     *
     * @param act
     * @return
     */
    public static boolean isNetWorkConnect(Context act) {
        ConnectivityManager manager = (ConnectivityManager) act.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();
        return !(networkinfo == null || !networkinfo.isAvailable());
    }

    /**
     * 设置头部左边的控件
     *
     * @param value 引用的资源文件。例如：R.string.text
     * @param isImg 图片：true；文字：false。
     */
    protected void setTopLeftValueAndShow(int value, Boolean isImg) {
        topLeftView = (LinearLayout) this.findViewById(R.id.topLeftView);
        if (isImg) {
            topLeftIv = (ImageView) this.findViewById(R.id.topLeft_iv);
            topLeftIv.setImageResource(value);
            topLeftIv.setVisibility(View.VISIBLE);
        } else {
            topLeftTv = (TextView) this.findViewById(R.id.topLeft_tv);
            topLeftTv.setText(value);
            topLeftTv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置头部右边的控件
     *
     * @param value 引用的资源文件。例如：R.string.text
     * @param isImg 图片：true；文字：false。
     */
    protected void setTopRightValueAndShow(int value, Boolean isImg) {
        topRightView = (LinearLayout) this.findViewById(R.id.topRightView);
        if (isImg) {
            topRightIv = (ImageView) this.findViewById(R.id.topRight_iv);
            topRightIv.setImageResource(value);
            topRightIv.setVisibility(View.VISIBLE);
        } else {
            topRightTv = (TextView) this.findViewById(R.id.topRight_tv);
            topRightTv.setText(value);
            topRightTv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置头部标题
     *
     * @param value 引用的资源文件或者直接字符串。例如：R.string.text
     */
    protected void setTopTitleShow(Object value) {
        if (null == topTitle) {
            topTitle = (TextView) this.findViewById(R.id.topTitle);
        }
        if (value instanceof String) {
            topTitle.setText((String) value);
            return;
        }
        if (value instanceof Integer) {
            topTitle.setText((Integer) value);
        }
    }

    /**
     * 获取右边控件的TextView
     */
    protected TextView getTopRightTextView() {
        if (null == topRightTv) {
            topRightTv = (TextView) this.findViewById(R.id.topRight_tv);
            topRightTv.setVisibility(View.VISIBLE);
        }
        return topRightTv;
    }

    /**
     * 获取右边控件的ImageView
     */
    protected ImageView getTopRightImageView() {
        if (null == topRightIv) {
            topRightIv = (ImageView) this.findViewById(R.id.topRight_iv);
            topRightIv.setVisibility(View.VISIBLE);
        }
        return topRightIv;
    }

    /**
     * 隐藏头部左边所有控件
     */
    protected void goneTopLeftView() {
        if (null != topLeftView) {
            topLeftView.setVisibility(View.GONE);
        }
    }

    /**
     *
     */
    protected void goneTopRightView() {
        if (null != topRightView) {
            topRightView.setVisibility(View.GONE);
        }
    }

    public void TopViewClick(View view) {
    }

    /**
     * dismiss pwProgressDialog
     */
    protected void dismissPWProgressDialog(){
        if (null != pwProgressDialog) {
            pwProgressDialog.pwProgressDialogDismiss();
        }
    }

    /**
     * dialog是否显示中
     * @return
     */
    protected boolean isPWProgressDialogShowing() {
        return null != pwProgressDialog && pwProgressDialog.isPWProgressDialogShowing();
    }

    /**
     * show pwProgressDialog
     * @param message
     */
    protected void showPWProgressDialog(String message){
        createPWProgressDialog();
        if (null != pwProgressDialog) {//更新message
            pwProgressDialog.setPWProgressDialogMessage(message);
        }
        pwProgressDialog.pwProgressDialogShow();
    }

    /**
     * show pwProgressDialog
     * @param message
     * @param cancelable
     */
    protected void showPWProgressDialog(String message, boolean cancelable){
        createPWProgressDialog();
        if (null != pwProgressDialog) {//更新message
            pwProgressDialog.setPWProgressDialogMessage(message)
                    .setPWProgressDialogCancelable(cancelable);
        }
        pwProgressDialog.pwProgressDialogShow();
    }

    /**
     * show pwProgressDialog
     */
    protected void showPWProgressDialog(){
        createPWProgressDialog();
        if (null != pwProgressDialog) {//更新message
            pwProgressDialog.setPWProgressDialogMessage(R.string.is_loading);
        }
        pwProgressDialog.pwProgressDialogShow();
    }

    /**
     * show pwProgressDialog
     * @param cancelable
     */
    protected void showPWProgressDialog(boolean cancelable){
        createPWProgressDialog();
        if (null != pwProgressDialog) {//更新message
            pwProgressDialog.setPWProgressDialogMessage(R.string.is_loading)
                    .setPWProgressDialogCancelable(cancelable);
        }
        pwProgressDialog.pwProgressDialogShow();
    }

    /**
     * show pwProgressDialog
     * @param messageId
     */
    protected void showPWProgressDialog(int messageId){
        createPWProgressDialog();
        if (null != pwProgressDialog) {//更新message
            pwProgressDialog.setPWProgressDialogMessage(messageId);
        }
        pwProgressDialog.pwProgressDialogShow();
    }

    /**
     * show pwProgressDialog
     * @param messageId
     * @param cancelable
     */
    protected void showPWProgressDialog(int messageId, boolean cancelable){
        createPWProgressDialog();
        if (null != pwProgressDialog) {//更新message
            pwProgressDialog.setPWProgressDialogMessage(messageId)
                    .setPWProgressDialogCancelable(cancelable);
        }
        pwProgressDialog.pwProgressDialogShow();
    }

    /**
     * 创建对话框
     */
    private void createPWProgressDialog(){
        if (null == pwProgressDialog) {//是否为空
            pwProgressDialog = new PWProgressDialog(this)
                    .pwProgressDialogCreate();
        }
    }

}
