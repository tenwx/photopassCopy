package com.pictureair.photopass.activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.SPUtils;

import java.util.HashMap;
import java.util.Map;

import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.config.UdeskConfig;

public class OpinionsActivity extends BaseActivity implements View.OnClickListener{


    private static final String SECRETKEY = "533032866a89ffdcb6340bf2ac0b9d6d";

    //需要更换公司的域名
    private static final String DOMAIN = "13818543871.udesk.cn";
    private RelativeLayout layout_question;
    private RelativeLayout layout_online;
    private RelativeLayout layout_feedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opinions);
        setTopLeftValueAndShow(R.drawable.back_blue, true);
        setTopTitleShow(R.string.mypage_customer);

        layout_question = (RelativeLayout) findViewById(R.id.opinions_questions);
        layout_online = (RelativeLayout) findViewById(R.id.opinions_online);
        layout_feedback = (RelativeLayout) findViewById(R.id.opinions_feedback);
        layout_question.setOnClickListener(this);
        layout_online.setOnClickListener(this);
        layout_feedback.setOnClickListener(this);

        UdeskSDKManager.getInstance().initApiKey(this,DOMAIN,SECRETKEY);
        UdeskSDKManager.getInstance().isShowLog(true);

        if (!SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ACCOUNT, "").equals("")) {// email
            String acount = SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ACCOUNT, "");
            Map<String, String> info = new HashMap<>();
            info.put(UdeskConst.UdeskUserInfo.USER_SDK_TOKEN, MyApplication.getTokenId());
            if (acount.contains("@")) {
                info.put(UdeskConst.UdeskUserInfo.EMAIL, acount);
            } else {
                info.put(UdeskConst.UdeskUserInfo.CELLPHONE, acount);
            }
            info.put(UdeskConst.UdeskUserInfo.NICK_NAME,SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_NICKNAME, ""));
            UdeskSDKManager.getInstance().setUserInfo(this, MyApplication.getTokenId(), info);
            UIStyle1();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.opinions_questions:
                UdeskSDKManager.getInstance().toLanuchHelperAcitivty(this);
                break;

            case R.id.opinions_online:
                UdeskSDKManager.getInstance().toLanuchChatAcitvity(this);
                break;

            case R.id.opinions_feedback:
                UdeskSDKManager.getInstance().goToForm(this);
                break;

        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            keyback();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void TopViewClick(View view) {
        super.TopViewClick(view);
        switch (view.getId()) {
            case R.id.topLeftView:
                keyback();
                break;
            default:
                break;
        }
    }

    private void keyback() {
        UdeskSDKManager.getInstance().disConnectXmpp();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UdeskSDKManager.getInstance().releaseDB();
    }

    private void UIStyle1(){
        // 标题栏TitleBar的背景色  通过颜色设置
        UdeskConfig.udeskTitlebarBgResId = R.color.white;
        // 标题栏TitleBar，左右两侧文字的颜色
        UdeskConfig.udeskTitlebarTextLeftRightResId = R.color.pp_blue;
        //IM界面，右侧文字的字体颜色
        UdeskConfig.udeskIMRightTextColorResId = R.color.udesk_color_im_text_right1;
        //IM界面，左侧文字的字体颜色
        UdeskConfig.udeskIMLeftTextColorResId = R.color.udesk_color_im_text_left1;
        //IM界面，左侧客服昵称文字的字体颜色
        UdeskConfig.udeskIMAgentNickNameColorResId = R.color.udesk_color_im_left_nickname1;
        //IM界面，时间文字的字体颜色
        UdeskConfig.udeskIMTimeTextColorResId = R.color.udesk_color_im_time_text1;
        // IM界面，提示语文字的字体颜色，比如客服转移
        UdeskConfig.udeskIMTipTextColorResId = R.color.udesk_color_im_tip_text1;
        // 返回箭头图标资源id
        UdeskConfig.udeskbackArrowIconResId = R.drawable.back_blue;
        // 咨询商品item的背景颜色
        UdeskConfig.udeskCommityBgResId = R.color.udesk_color_im_commondity_bg1;
        // 商品介绍Title的字样颜色
        UdeskConfig.udeskCommityTitleColorResId = R.color.udesk_color_im_commondity_title1;
        // 商品咨询页面中，商品介绍子Title的字样颜色
        UdeskConfig.udeskCommitysubtitleColorResId = R.color.udesk_color_im_commondity_subtitle1;
        // 商品咨询页面中，发送链接的字样颜色
        UdeskConfig.udeskCommityLinkColorResId = R.color.udesk_color_im_commondity_title1;
    }

}
