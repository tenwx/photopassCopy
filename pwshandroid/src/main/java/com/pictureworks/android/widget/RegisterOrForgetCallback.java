package com.pictureworks.android.widget;

/**
 * Created by bass on 16/4/28.
 */
public interface RegisterOrForgetCallback {
    void goneDialog();
    void showDialog();
    void onFai(int StringId);//操作失败
    void onFai(String StringId);//操作失败
    void onSuccess();//操作成功
    void countDown(int i);//倒计时
    void nextPageForget();
}
