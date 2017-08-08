package com.pictureair.photopassCopy.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.photopassCopy.R;
import com.pictureair.photopassCopy.util.ReflectionUtil;
import com.pictureair.photopassCopy.util.ScreenUtil;

/**
 * 自定义的ProgressBarDialog
 *
 * 注意点，show和dismiss必须要在main线程中执行
 *
 * 怎么使用：
 * PWProgressBarDialog pwDialog = new PWProgressBarDialog(context).pwProgressBarDialogCreate(PWProgressBarDialog.TYPE);
 * pwDialog.pwProgressBarDialogShow();
 *
 * @author bauer_bao
 */
public class PWProgressBarDialog extends Dialog {
    private Context mContext;
    /**
     * dialog对应的默认view
     */
    private ImageView imageView;
    private TextView messageTV;

    /**
     * 点击dialog外部是否可以取消，默认不可以取消
     */
    private boolean cancelable = false;

    /**
     * 进度条类型
     */
    private int showType;

    /**
     * 加载
     */
    public static final int TYPE_LOADING = 0;

    /**
     * 上传
     */
    public static final int TYPE_UPLOAD = 1;

    /**
     * 删除
     */
    public static final int TYPE_DELETE = 2;

    /**
     * 下载
     */
    public static final int TYPE_DOWNLOAD = 3;

    public PWProgressBarDialog(Context context, int theme) {
        super(context, theme);
        mContext = context;
    }

    public PWProgressBarDialog(Context context) {
        super(context, R.style.CustomProgressDialog);
        mContext = context;
    }

    /**
     * 设置点击对话框外部是否取消，默认不可取消
     * @param cancelable
     * @return
     */
    public PWProgressBarDialog setPWProgressBarDialogCancelable(boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }

    /**
     * 创建dialog
     */
    public PWProgressBarDialog pwProgressBarDialogCreate(int showType) {
        this.showType = showType;
        View view = View.inflate(mContext, R.layout.customprogressdialog, null);
        messageTV = (TextView) view.findViewById(R.id.id_tv_loadingmsg);
        imageView = (ImageView) view.findViewById(R.id.loadingImageView);
        imageView.setBackgroundColor(Color.TRANSPARENT);
        LayoutParams params = imageView.getLayoutParams();
        params.width = ScreenUtil.getScreenWidth(mContext) / 6;
        params.height = params.width;

        setContentView(view);
        setCancelable(cancelable);

        // 设置居中
        getWindow().getAttributes().gravity = Gravity.CENTER;
        return this;
    }

    /**
     * 显示dialog
     */
    public void pwProgressBarDialogShow(){
        setProgress(0, 0);
        if (!isShowing()) {
            show();
        }
    }

    /**
     * dismiss dialog
     */
    public void pwProgressBarDialogDismiss(){
        if (isShowing()) {
            dismiss();
        }
    }

    /**
     * 是否正在显示中
     * @return
     */
    public boolean isPWProgressBarDialogShowing() {
        return isShowing();
    }

    /**
     * 设置进度
     *
     * @param progress
     * @param totalCount
     */
    private void setProgressText(long progress, long totalCount) {
        String showText;
        switch (showType) {
            case TYPE_UPLOAD:
                // 上传
                if (progress > 0 && totalCount > 0) {
                    showText = String.format(mContext.getString(R.string.loading_percent), progress * 100 / totalCount) + "%";
                } else {
                    showText = String.format(mContext.getString(R.string.loading_percent), 0) + "%";
                }
                break;

            case TYPE_DELETE:
                // 删除
                showText = String.format(mContext.getString(R.string.delete_percent), progress, totalCount);
                break;

            case TYPE_DOWNLOAD:
                // 下载
                if (progress > 0 && totalCount > 0) {
                    showText = String.format(mContext.getString(R.string.downloading_percent), progress * 100 / totalCount) + "%";
                } else {
                    showText = String.format(mContext.getString(R.string.downloading_percent), 0) + "%";
                }
                break;

            default:
                // 加载 TYPE_LOADING
                showText = String.format(mContext.getString(R.string.loading_percent), 0) + "%";
                break;
        }
        messageTV.setText(showText);
    }

    /**
     * 更新进度条
     *
     * @param progress
     */
    public void setProgress(long progress, long total) {
        long currentProgress = 0;
        if (total != 0) {
            currentProgress = progress * 100 / total;
        }

        int result = ReflectionUtil.getDrawableId(mContext, "loading_" + (currentProgress / 8));

        imageView.setImageResource(result);
        setProgressText(progress, total);
    }
}