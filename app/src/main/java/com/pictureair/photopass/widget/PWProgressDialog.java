package com.pictureair.photopass.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.util.ScreenUtil;

/**
 * 自定义的ProgressDialog
 *
 * 使用方法和PWDialog类似，具体请点击右侧link {@link com.pictureair.photopass.customDialog.PWDialog}
 *
 * 具体使用，已写入baseActivity，并且重载多重常用的显示方法
 *
 * 注意点，show和dismiss必须要在main线程中执行
 *
 * 加载动画，建议使用gif，然后使用glide加载。但是项目中的图片制作出来的gif边缘模糊，因此依旧使用帧动画实现
 *
 * @author bauer_bao
 */
public class PWProgressDialog extends Dialog {
    private Context mContext;
    /**
     * dialog对应的默认view
     */
    private ImageView imageView;
    private TextView messageTV;

    /**
     * 显示的文字
     */
    private String messageStr;

    private AnimationDrawable spinner;

    /**
     * 点击dialog外部是否可以取消，默认不可以取消
     */
    private boolean cancelable = false;

    public PWProgressDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
    }

    public PWProgressDialog(Context context) {
        super(context, R.style.CustomProgressDialog);
        mContext = context;
    }

    /**
     * 设置message
     * @param messageStr
     * @return
     */
    public PWProgressDialog setPWProgressDialogMessage(String messageStr) {
        this.messageStr = messageStr;
        return this;
    }

    /**
     * 设置message
     * @param messageId
     * @return
     */
    public PWProgressDialog setPWProgressDialogMessage(int messageId) {
        this.messageStr = mContext.getString(messageId);
        return this;
    }

    /**
     * 设置点击对话框外部是否取消，默认不可取消
     * @param cancelable
     * @return
     */
    public PWProgressDialog setPWProgressDialogCancelable(boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }

    /**
     * 创建dialog
     */
    public PWProgressDialog pwProgressDialogCreate() {
        View view = View.inflate(mContext, R.layout.customprogressdialog, null);
        messageTV = (TextView) view.findViewById(R.id.id_tv_loadingmsg);
        imageView = (ImageView) view.findViewById(R.id.loadingImageView);
        LayoutParams params = imageView.getLayoutParams();
        params.width = ScreenUtil.getPortraitScreenWidth(mContext) / 6;
        params.height = params.width;

        setContentView(view);

        // 设置居中
        getWindow().getAttributes().gravity = Gravity.CENTER;

        spinner = (AnimationDrawable) imageView.getBackground();
        return this;
    }

    /**
     * 显示dialog
     */
    public void pwProgressDialogShow(){
        checkContent();
        if (!isShowing()) {
            show();
        }
        if (spinner != null) {
            spinner.start();
        }
    }

    /**
     * dismiss dialog
     */
    public void pwProgressDialogDismiss(){
        if (isShowing()) {
            dismiss();
        }
        if (spinner != null) {
            spinner.stop();
        }
    }

    /**
     * 是否正在显示中
     * @return
     */
    public boolean isPWProgressDialogShowing() {
        return isShowing();
    }

    /**
     * 检查各个参数
     */
    private void checkContent() {
        if (!TextUtils.isEmpty(messageStr)) {
            messageTV.setText(messageStr);
            messageTV.setVisibility(View.VISIBLE);
        } else {
            messageTV.setVisibility(View.GONE);
        }
        setCancelable(cancelable);
    }
}