package com.pictureair.photopass.customDialog;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.pictureair.photopass.R;

/**
 * 自定义对话框的确认按钮和取消按钮封装类
 *
 * @author bauer_bao
 */
public class PWDialogButton extends FrameLayout {
    private TextView btnNegative;
    private TextView btnPositive;
    private View view;
    private Context context;
    private OnPWDialogButtonClickCallBack onPWDialogButtonClickCallBack;

    public PWDialogButton(Context context) {
        super(context);
    }

    public PWDialogButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        View.inflate(getContext(), R.layout.group_button, this);
        btnNegative = (TextView) findViewById(R.id.btn_negative);
        btnNegative.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (onPWDialogButtonClickCallBack != null) {
                    onPWDialogButtonClickCallBack.onNegativeButtonClicked();
                }
            }
        });

        btnPositive = (TextView) findViewById(R.id.btn_positive);
        btnPositive.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (onPWDialogButtonClickCallBack != null) {
                    onPWDialogButtonClickCallBack.onPositiveButtonClicked();
                }
            }
        });

        view = findViewById(R.id.middle_line);
    }

    /**
     * 设置监听
     *
     * @param onPWDialogButtonClickCallBack
     */
    public void setOnClickListener(OnPWDialogButtonClickCallBack onPWDialogButtonClickCallBack) {
        this.onPWDialogButtonClickCallBack = onPWDialogButtonClickCallBack;
    }

    /**
     * 设置按钮的内容，如果内容为null，则不显示按钮
     *
     * @param positive
     * @param negative
     */
    public void setButtonText(String positive, String negative) {
        if (positive == null && negative == null) {
            setVisibility(View.GONE);
            return;
        }

        //设置取消按钮
        if (negative == null) {
            btnNegative.setVisibility(View.GONE);
        } else {
            btnNegative.setVisibility(View.VISIBLE);
            btnNegative.setText(negative);
        }

        //设置确定按钮
        if (positive == null) {
            btnPositive.setVisibility(View.GONE);
        } else {
            if (negative == null) {
                view.setVisibility(View.GONE);
            } else {
                view.setVisibility(View.VISIBLE);
            }
            btnPositive.setVisibility(View.VISIBLE);
            btnPositive.setText(positive);
        }
    }

    /**
     * 设置按钮的颜色
     *
     * @param positiveColorId
     * @param negativeColorId
     */
    public void setButtonTextColor(int positiveColorId, int negativeColorId) {
        //设置取消按钮颜色
        if (negativeColorId != 0 && btnNegative.isShown()) {
            btnNegative.setTextColor(ContextCompat.getColor(context, negativeColorId));
        }

        //设置确定按钮颜色
        if (positiveColorId != 0 && btnPositive.isShown()) {
            btnPositive.setTextColor(ContextCompat.getColor(context, positiveColorId));
        }
    }

    /**
     * 按钮回调
     */
    public interface OnPWDialogButtonClickCallBack {
        void onNegativeButtonClicked();

        void onPositiveButtonClicked();
    }
}