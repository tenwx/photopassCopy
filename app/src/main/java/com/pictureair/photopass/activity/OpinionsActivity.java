package com.pictureair.photopass.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;

import java.util.HashMap;
import java.util.Map;

import cn.udesk.UdeskConst;
import cn.udesk.UdeskSDKManager;
import cn.udesk.config.UdeskConfig;
import cn.udesk.messagemanager.UdeskMessageManager;
import cn.udesk.model.MsgNotice;
import cn.udesk.model.UdeskCommodityItem;

public class OpinionsActivity extends BaseActivity implements View.OnClickListener{


    private static final String SECRETKEY = "533032866a89ffdcb6340bf2ac0b9d6d";
    private static final String DOMAIN = "13818543871.udesk.cn";
    private Button btn_man;
    private Button btn_help;
    private Button btn_commodity;
    private Button btn_agent;
    private Button btn_group;
    private Button btn_conversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opinions);

        btn_man = (Button) findViewById(R.id.btn_man);
        btn_help = (Button) findViewById(R.id.btn_help);
        btn_commodity = (Button) findViewById(R.id.btn_commodity_item);
        btn_agent = (Button) findViewById(R.id.btn_agent);
        btn_group = (Button) findViewById(R.id.btn_group);
        btn_conversation = (Button) findViewById(R.id.btn_conversation);
        btn_man.setOnClickListener(this);
        btn_help.setOnClickListener(this);
        btn_commodity.setOnClickListener(this);
        btn_agent.setOnClickListener(this);
        btn_group.setOnClickListener(this);
        btn_conversation.setOnClickListener(this);

        UdeskSDKManager.getInstance().initApiKey(this,DOMAIN,SECRETKEY);
        UdeskSDKManager.getInstance().isShowLog(true);

        if (!SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ACCOUNT, "").equals("")) {// email
            String acount = SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ACCOUNT, "");
            Map<String, String> info = new HashMap<String, String>();
            info.put(UdeskConst.UdeskUserInfo.USER_SDK_TOKEN, MyApplication.getTokenId());
            if (acount.contains("@")) {
                info.put(UdeskConst.UdeskUserInfo.EMAIL, acount);
            } else {
                info.put(UdeskConst.UdeskUserInfo.CELLPHONE, acount);
            }
            info.put(UdeskConst.UdeskUserInfo.NICK_NAME,SPUtils.getString(this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_NICKNAME, ""));
            UdeskSDKManager.getInstance().setUserInfo(this, MyApplication.getTokenId(), info);
            boolean  res = UdeskMessageManager.getInstance().event_OnNewMsgNotice.bind(this, "OnNewMsgNotice");
            PictureAirLog.e("bind",res ? "true" : "false");
            UIStyle1();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_man:
                UdeskSDKManager.getInstance().toLanuchChatAcitvity(this);
                break;

            case R.id.btn_help:
                UdeskSDKManager.getInstance().toLanuchHelperAcitivty(this);
                break;

            case R.id.btn_commodity_item:
//                Map<String, String> info = new HashMap<String, String>();
//                info.put(UdeskConst.UdeskUserInfo.CELLPHONE, "15601925037");
//                info.put(UdeskConst.UdeskUserInfo.NICK_NAME,"嚣张");
//                UdeskSDKManager.getInstance().setUpdateUserinfo(info);

                UdeskCommodityItem item = new UdeskCommodityItem();
                item.setTitle("木林森男鞋新款2016夏季透气网鞋男士休闲鞋网面韩版懒人蹬潮鞋子");// 商品主标题
                item.setSubTitle("¥ 99.00");//商品描述
                item.setThumbHttpUrl("http://211.95.27.34:4000/ProductGallery/gift-8r-1.png");//左侧图片
                item.setCommodityUrl("https://detail.tmall.com/item.htm?spm=a1z10.3746-b.w4946-14396547293.1.4PUcgZ&id=529634221064&sku_properties=-1:-1");// 商品网络链接
                UdeskSDKManager.getInstance().setCommodity(item);
                break;
            case R.id.btn_agent:

                break;
            case R.id.btn_group:

                break;
            case R.id.btn_conversation:

                break;
        }
    }

    public void OnNewMsgNotice(MsgNotice msgNotice) {
        if (msgNotice != null) {
            notifyMsg(this, msgNotice.getContent());
        }
    }

    private void notifyMsg(Context context, String content) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Notification notification =new  NotificationCompat.Builder(context).setSmallIcon(R.drawable.pp_icon).setAutoCancel(true).setContentTitle(getString(R.string.app_name))
                .setContentText(content).build();

        manager.notify(1001, notification);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            UdeskSDKManager.getInstance().disConnectXmpp();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UdeskMessageManager.getInstance().event_OnNewMsgNotice.unBind(this);
    }

    private void UIStyle1(){
        // 标题栏TitleBar的背景色  通过颜色设置
        UdeskConfig.udeskTitlebarBgResId = R.color.udesk_titlebar_bg1;
        // 标题栏TitleBar，左右两侧文字的颜色
        UdeskConfig.udeskTitlebarTextLeftRightResId = R.color.udesk_color_navi_text1;
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
        UdeskConfig.udeskbackArrowIconResId = R.drawable.udesk_titlebar_back;
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
