package com.pictureair.photopass.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;

import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.HelpInfosAdapter;
import com.pictureair.photopass.entity.HelpInfo;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.JsonUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * 帮助
 * 每次进入都需要请求
 *
 * @author bass
 */
public class HelpActivity extends BaseActivity {
    private final String TAG = "HelpActivity ";
    private Context context;
//    private ImageView back;
    private NoNetWorkOrNoCountView noNetWorkOrNoCountView;
    private final int NOT_NETWORK = 111;
    private ArrayList<HelpInfo> helpInfos;
    private CustomProgressDialog customProgressDialog;
    private ListView mListView;
    private HelpInfosAdapter adapte;
    private MyToast myToast;


    private final Handler helpHandler = new HelpHandler(this);


    private static class HelpHandler extends Handler{
        private final WeakReference<HelpActivity> mActivity;

        public HelpHandler(HelpActivity activity){
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivity.get() == null) {
                return;
            }
            mActivity.get().dealHandler(msg);
        }
    }

    /**
     * 处理Message
     * @param msg
     */
    private void dealHandler(Message msg) {
        if (null != customProgressDialog && customProgressDialog.isShowing()) {
            customProgressDialog.dismiss();
        }
        switch (msg.what) {
            case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD://noView的按钮响应重新加载点击事件
                initData();
                break;
            case NOT_NETWORK:
                noNetWorkOrNoCountView.setResult(R.string.no_network, R.string.click_button_reload, R.string.reload, R.drawable.no_network, helpHandler, true);
                break;

            case API1.GET_HELP_SUCCESS:
                PictureAirLog.i(TAG, "===>case API1.GET_HELP_SUCCESS");
                helpInfos = JsonUtil.getHelpInfoList((JSONObject) msg.obj);
                adapte = new HelpInfosAdapter(helpInfos, context);
                mListView.setAdapter(adapte);
                break;

            case API1.GET_HELP_FAILED:
                PictureAirLog.e(TAG, "===>case API1.GET_HELP_FAILED");
                int errorInfo = ReflectionUtil.getStringId(context, msg.arg1);
                myToast.setTextAndShow(errorInfo, Common.TOAST_SHORT_TIME);
                break;

            default:
                break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        context = this;
        customProgressDialog = CustomProgressDialog.create(context, context.getString(R.string.is_loading), true, null);
        initView();
        initData();
    }

    /**
     * 初始化网络数据
     */
    private void initData() {
        if (!customProgressDialog.isShowing()) {
            customProgressDialog.show();
        }
        if (AppUtil.getNetWorkType(context) == AppUtil.NETWORKTYPE_INVALID) {
            mListView.setVisibility(View.GONE);
            noNetWorkOrNoCountView.setVisibility(View.VISIBLE);
            helpHandler.sendEmptyMessage(NOT_NETWORK);
        } else {
            mListView.setVisibility(View.VISIBLE);
            noNetWorkOrNoCountView.setVisibility(View.GONE);
            API1.getHelp(helpHandler);
        }
    }

    private void initView() {
        myToast = new MyToast(context);
        setTopLeftValueAndShow(R.drawable.back_white,true);
        setTopTitleShow(R.string.mypage_help);
        noNetWorkOrNoCountView = (NoNetWorkOrNoCountView) findViewById(R.id.storyNoNetWorkView);
        mListView = (ListView) findViewById(R.id.lv_helps);
    }

    @Override
    public void TopViewClick(View view) {
        super.TopViewClick(view);
        switch (view.getId()) {
            case R.id.topLeftView:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        helpHandler.removeCallbacksAndMessages(null);
    }
}