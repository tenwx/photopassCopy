
package com.pictureair.photopass.widget;


import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.photopass.R;

/**
 * 自定义的ProgressDialog
 *
 * @author bauer_bao
 */
public class CustomProgressDialog extends Dialog {
    private static ImageView imageView;
    private static TextView txt;

    public CustomProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    /**
     * 当窗口焦点改变时调用
     */
    public void onWindowFocusChanged(boolean hasFocus) {
        // 获取ImageView上的动画背景
        AnimationDrawable spinner = (AnimationDrawable) imageView.getBackground();
        // 开始动画
        spinner.start();
    }

//	/**
//	 * 设置message，显示上传的百分比
//	 * @param percent
//	 */
//	public void setMessage(String percent){
//		txt.setText(percent);
//	}

    /**
     * 弹出自定义ProgressDialog
     *
     * @param context        上下文
     * @param message        提示
     * @param cancelable     是否按返回键取消
     * @param cancelListener 按下返回键监听
     * @return
     */
    public static CustomProgressDialog show(Context context, CharSequence message, boolean cancelable, OnCancelListener cancelListener) {
        CustomProgressDialog dialog = new CustomProgressDialog(context, R.style.CustomProgressDialog);
        dialog.setTitle("");
        dialog.setContentView(R.layout.customprogressdialog);
        txt = (TextView) dialog.findViewById(R.id.id_tv_loadingmsg);
        imageView = (ImageView) dialog.findViewById(R.id.loadingImageView);
        LayoutParams params = imageView.getLayoutParams();
        params.width = context.getResources().getDisplayMetrics().widthPixels / 5;
        params.height = params.width;
        if (message == null || message.length() == 0) {
            txt.setVisibility(View.GONE);
        } else {
            txt.setText(message);
        }
        // 按返回键是否取消
        dialog.setCancelable(cancelable);
        // 监听返回键处理
        dialog.setOnCancelListener(cancelListener);
        // 设置居中
        dialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        dialog.show();
        return dialog;
    }

    /**
     * 弹出自定义ProgressDialog
     *
     * @param context        上下文
     * @param message        提示
     * @param cancelable     是否按返回键取消
     * @param cancelListener 按下返回键监听
     * @return
     */
    public static CustomProgressDialog create(Context context, CharSequence message, boolean cancelable, OnCancelListener cancelListener) {
        CustomProgressDialog dialog = new CustomProgressDialog(context, R.style.CustomProgressDialog);
        dialog.setTitle("");
        dialog.setContentView(R.layout.customprogressdialog);
        txt = (TextView) dialog.findViewById(R.id.id_tv_loadingmsg);
        imageView = (ImageView) dialog.findViewById(R.id.loadingImageView);
        LayoutParams params = imageView.getLayoutParams();
        params.width = context.getResources().getDisplayMetrics().widthPixels / 5;
        params.height = params.width;
        if (message == null || message.length() == 0) {
            txt.setVisibility(View.GONE);
        } else {
            txt.setText(message);
        }
        // 按返回键是否取消
        dialog.setCancelable(cancelable);
        // 监听返回键处理
        dialog.setOnCancelListener(cancelListener);
        // 设置居中
        dialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        return dialog;
    }
}
