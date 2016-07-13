package com.pictureair.photopass.customDialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.util.ScreenUtil;

/**
 * 自定义的dialog
 *
 * 功能：
 * 1.重载多种常用方法
 * 2.无需创建多个dialog，可支持动态设置message，title等大部分属性
 * 3.提供自定义Dialog的接口OnCustomerViewCallBack，可实现自定义的Dialog，并且只会初始化一次
 * 4.支持按钮的0个、单个 以及 两个排列，但是不支持3个及以上的按钮，也支持修改文案以及颜色
 * 5.message可设置对应方式，目前只支持居中和靠左对其方式
 * 6.根据dialogId识别不同的dialog，保证回调监听不会混淆
 * 7.默认点击对话框外部不可取消，也可以设置取消
 * 8.可设置是否显示title，设置title文案以及颜色
 * 9.支持链式设置属性
 *
 * 弊端：
 * 1.只支持一种默认模式，如果需要其他模式，只能使用自定义接口进行拓展
 * 2.不支持一个dialog对象一会用通用的，一会用自定义的，如果要用，请用两个dialog对象
 * 3.and so on
 *
 * 注意点：
 * 1.方法名和Dialog自带的不一样，调用时需要注意
 * 2.回调接口，不要使用匿名内部类的形式，不然回调太多，太冗余，dialogId也起不到作用
 * 3.title、positiveButton、negativeButton，如果内容为null，则不显示该控件
 *
 * 怎么使用：
 * eg：
 *
 * PWDialog pwdialog;
 * if (pwDialog == null) {
 *      pwDialog = new PWDialog(context, dialogId)
 *      .setPWDialogTitle("dialog1")
 *      .setPWDialogTitleColor(R.color.color_9)
 *      .setPWDialogMessage("message dialog")
 *      .setPWDialogContentGravity(true)
 *      .setOnPWDialogClickListener(this)
 *      .setPWDialogPositiveButton("yes")
 *      .setPWDialogPositiveButtonColor(R.color.color_5)
 *      .pwDialogCreate();
 * }
 * pwDialog.pwDilogShow();
 *
 * @author bauer_bao
 */
public class PWDialog extends Dialog {
    private Context mContext;
    /**
     * dialog对应的默认view
     */
    private View contentView;
    private TextView messageTV, titleTV;
    private RelativeLayout contentRL;
    private FrameLayout contentViewParent;
    private PWDialogButton btnGroup;

    private String titleStr;
    private String messageStr;
    private String positiveButtonStr;
    private String negativeButtonStr;

    /**
     * 文案是否居中，默认居中，false左对齐，暂时只支持两种排列模式
     */
    private boolean textCenter = true;

    /**
     * 点击dialog外部是否可以取消，默认不可以取消
     */
    private boolean cancelable = false;

    /**
     * 自定义view是否已经初始化
     */
    private boolean customerInited = false;

    /**
     * dialog的ID
     */
    private int dialogId;

    /**
     * title字体颜色
     */
    private int titleColorId;

    /**
     * message字体颜色
     */
    private int messageColorId;

    /**
     * 确定按钮字体颜色
     */
    private int positiveButtonColorId;

    /**
     * 取消按钮字体颜色
     */
    private int negativeButtonColorId;

    private OnDialogButtonListener dialogButtonListener;
    private OnCustomerViewCallBack customerViewCallBack;

    /**
     * 构造方法
     * @param context
     */
    public PWDialog(Context context) {
        super(context, R.style.Dialog);
        mContext = context;
    }

    public PWDialog(Context context, int dialogId, int theme) {
        super(context, theme);
    }

    /**
     * 构造方法
     * @param context
     * @param dialogId
     */
    public PWDialog(Context context, int dialogId) {
        super(context, R.style.Dialog);
        this.dialogId = dialogId;
        mContext = context;
    }

    /**
     * 设置dialogId
     * @param dialogId
     * @return
     */
    public PWDialog setPWDialogId(int dialogId) {
        this.dialogId = dialogId;
        return this;
    }

    /**
     * 设置按钮监听
     * @param onPWDialogClickListener
     * @return
     */
    public PWDialog setOnPWDialogClickListener(OnPWDialogClickListener onPWDialogClickListener) {
        dialogButtonListener = new OnDialogButtonListener(onPWDialogClickListener);
        return this;
    }

    /**
     * 设置title，null则不显示title
     * @param titleStr
     * @return
     */
    public PWDialog setPWDialogTitle(String titleStr) {
        this.titleStr = titleStr;
        return this;
    }

    /**
     * 设置title
     * @param titleId
     * @return
     */
    public PWDialog setPWDialogTitle(int titleId) {
        this.titleStr = mContext.getString(titleId);
        return this;
    }

    /**
     * 设置title字体颜色
     * @param titleColorId
     * @return
     */
    public PWDialog setPWDialogTitleColor(int titleColorId){
        this.titleColorId = titleColorId;
        return this;
    }

    /**
     * 设置message
     * @param messageStr
     * @return
     */
    public PWDialog setPWDialogMessage(String messageStr) {
        this.messageStr = messageStr;
        return this;
    }

    /**
     * 设置message
     * @param messageId
     * @return
     */
    public PWDialog setPWDialogMessage(int messageId) {
        this.messageStr = mContext.getString(messageId);
        return this;
    }

    /**
     * 设置message字体颜色
     * @param messageColorId
     * @return
     */
    public PWDialog setPWDialogMessageColor(int messageColorId){
        this.messageColorId = messageColorId;
        return this;
    }

    /**
     * 设置文字居中属性， 默认居中
     *
     * @param gravity
     * @return
     */
    public PWDialog setPWDialogContentCenter(boolean gravity) {
        textCenter = gravity;
        return this;
    }

    /**
     * 设置确认按钮，null则不显示确认按钮
     * @param positiveButtonStr
     * @return
     */
    public PWDialog setPWDialogPositiveButton(String positiveButtonStr) {
        this.positiveButtonStr = positiveButtonStr;
        return this;
    }

    /**
     * 设置确认按钮
     * @param positiveId
     * @return
     */
    public PWDialog setPWDialogPositiveButton(int positiveId) {
        this.positiveButtonStr = mContext.getString(positiveId);
        return this;
    }

    /**
     * 设置确定按钮颜色
     * @param positiveButtonColorId
     * @return
     */
    public PWDialog setPWDialogPositiveButtonColor(int positiveButtonColorId) {
        this.positiveButtonColorId = positiveButtonColorId;
        return this;
    }

    /**
     * 设置取消按钮，null则不显示取消按钮
     * @param negativeId
     * @return
     */
    public PWDialog setPWDialogNegativeButton(int negativeId) {
        this.negativeButtonStr = mContext.getString(negativeId);
        return this;
    }

    /**
     * 设置取消按钮
     * @param negativeButtonStr
     * @return
     */
    public PWDialog setPWDialogNegativeButton(String negativeButtonStr) {
        this.negativeButtonStr = negativeButtonStr;
        return this;
    }

    /**
     * 设置取消按钮颜色
     * @param negativeButtonColorId
     * @return
     */
    public PWDialog setPWDialogNegativeButtonColor(int negativeButtonColorId) {
        this.negativeButtonColorId = negativeButtonColorId;
        return this;
    }

    /**
     * 设置点击对话框外部是否取消，默认不可取消
     * @param cancelable
     * @return
     */
    public PWDialog setPWDialogCancelable(boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }

    /**
     * 设置自定义布局
     * @param contentView 自定义布局View
     * @param customerViewCallBack 自定义布局接口回调
     * @return
     */
    public PWDialog setPWDialogContentView(View contentView, OnCustomerViewCallBack customerViewCallBack) {
        this.contentView = contentView;
        this.customerViewCallBack = customerViewCallBack;
        return this;
    }

    /**
     * 设置新的布局
     * @param layoutResId 自定义布局View
     * @param customerViewCallBack 自定义布局接口回调
     * @return
     */
    public PWDialog setPWDialogContentView(int layoutResId, OnCustomerViewCallBack customerViewCallBack) {
        this.contentView = View.inflate(mContext, layoutResId, null);
        this.customerViewCallBack = customerViewCallBack;
        return this;
    }

    /**
     * 创建dialog
     */
    public PWDialog pwDialogCreate() {
        View view = View.inflate(mContext, R.layout.layout_custom_dialog, null);
        titleTV = (TextView) view.findViewById(R.id.dia_title);
        contentRL = (RelativeLayout) view.findViewById(R.id.dia);
        messageTV = (TextView) view.findViewById(R.id.tv_message);
        contentViewParent = (FrameLayout) view.findViewById(R.id.content_view);
        btnGroup = (PWDialogButton) view.findViewById(R.id.btn_group);
        btnGroup.setOnClickListener(new PWDialogButton.OnPWDialogButtonClickCallBack() {
            @Override
            public void onPositiveButtonClicked() {
                dialogButtonListener.onClick(PWDialog.this, DialogInterface.BUTTON_POSITIVE);
            }

            @Override
            public void onNegativeButtonClicked() {
                dialogButtonListener.onClick(PWDialog.this, DialogInterface.BUTTON_NEGATIVE);
            }
        });

        setContentView(view);
        setCancelable(cancelable);

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams params = dialogWindow.getAttributes();
        params.width = (int) (ScreenUtil.getScreenWidth(mContext) * 0.9);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialogWindow.setAttributes(params);
        return this;
    }

    /**
     * 显示dialog
     */
    public void pwDilogShow(){
        checkContent();
        if (!isShowing()) {
            show();
        }
    }

    /**
     * 检查各个组件
     */
    private void checkContent(){
        //检查title
        if (TextUtils.isEmpty(titleStr)) {
            titleTV.setVisibility(View.GONE);
            contentRL.setBackgroundResource(R.drawable.bg_dialog_white_content_gender);
        } else {
            titleTV.setVisibility(View.VISIBLE);
            titleTV.setText(titleStr);
            if (titleColorId != 0) {
                titleTV.setTextColor(ContextCompat.getColor(mContext, titleColorId));
            }
            contentRL.setBackgroundColor(Color.WHITE);
        }

        //检查内容
        if (contentView != null) {//检查自定义布局

            if (!customerInited) {//没有初始化，则需要初始化
                customerInited = true;
                contentViewParent.removeAllViews();
                contentViewParent.addView(this.contentView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                customerViewCallBack.initCustomerView(contentView, dialogId);
            }

        } else {//检查message
            if (!TextUtils.isEmpty(messageStr)) {
                messageTV.setText(messageStr);
                messageTV.setGravity(textCenter ? Gravity.CENTER : Gravity.START | Gravity.CENTER_VERTICAL);
            }
            if (messageColorId != 0) {
                messageTV.setTextColor(ContextCompat.getColor(mContext, messageColorId));
            }
        }

        btnGroup.setButtonText(positiveButtonStr, negativeButtonStr);
        btnGroup.setButtonTextColor(positiveButtonColorId, negativeButtonColorId);
    }

    /**
     * 按钮监听
     */
    private class OnDialogButtonListener implements OnClickListener {
        private OnPWDialogClickListener onPWDialogClickListener;

        public OnDialogButtonListener(OnPWDialogClickListener onPWDialogClickListener) {
            this.onPWDialogClickListener = onPWDialogClickListener;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            dismiss();
            onPWDialogClickListener.onPWDialogButtonClicked(which, dialogId);
        }
    }

    public interface OnPWDialogClickListener {
        void onPWDialogButtonClicked(int which, int dialogId);
    }

    public interface OnCustomerViewCallBack {
        void initCustomerView(View view, int dialogId);
    }
}