package com.pictureworks.android.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.pictureworks.android.R;


public class MyToast extends Toast {

    private Toast toast;
    private TextView textView;

    public MyToast(Context context) {
        super(context);
        toast = new Toast(context);
        // TODO Auto-generated constructor stub
        //获取LayoutInflater对象
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //由layout文件创建一个View对象
        View layout = inflater.inflate(R.layout.newtoast, null);
        //实例化ImageView和TextView对象
        textView = (TextView) layout.findViewById(R.id.toast_textview);
        toast.setView(layout);
    }

    /**
     * 设置内容，时间
     *
     * @param text
     */
    public void setTextAndShow(String text, int time) {
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);//设置toast位置
        toast.setDuration(time);
        textView.setText(text);
        toast.show();
    }

    /**
     * 设置内容，时间
     *
     * @param text
     */
    public void setTextAndShow(int text, int time) {
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);//设置toast位置
        toast.setDuration(time);
        if (text == 0) {
            text = R.string.http_error_code_401;
        }
        textView.setText(text);
        toast.show();
    }

    /**
     * 取消toast
     */
    public void cancelToast(){
        toast.cancel();
    }


}
