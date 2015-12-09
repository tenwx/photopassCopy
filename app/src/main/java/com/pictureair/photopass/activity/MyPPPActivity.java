package com.pictureair.photopass.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.ListOfPPPAdapter;
import com.pictureair.photopass.customDialog.CustomDialog;
import com.pictureair.photopass.entity.PPPinfo;
import com.pictureair.photopass.entity.PPinfo;
import com.pictureair.photopass.util.API;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.HttpUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.widget.BannerView_PPPIntroduce;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;
import com.pictureair.photopass.widget.PPPPop;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import cz.msebera.android.httpclient.Header;

/**
 * 显示用户所有的PP+或是对应某个PP而言可使用的PP+
 */
public class MyPPPActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = "MyPPPActivity";
    private final static int PPP_CODE = 1;
    private final static int PP_CODE = 2;

    private ImageView setting;
    private ListView listPPP;
    private ImageView back;
    private ImageView optionImageView;
    private TextView optoinTextView, text_instruction;

    private BannerView_PPPIntroduce nopppLayout;

    private CustomProgressDialog dialog;
    private MyToast newToast;
    private CustomDialog customdialog;

    private ListOfPPPAdapter listPPPAdapter;
    private ArrayList<PPPinfo> list1;// 绑定了pp的pp+
    private ArrayList<PPinfo> list3;// 对应pp+可以绑定的pp
    private SharedPreferences sharedPreferences;

    private boolean hasOtherAvailablePPP = false;//判断是否还有其他可用的ppp
    private int currentPosition = 0;//记录选中的项的索引值

    private String errorMessage = "";
    private PPPPop pppPop;
    private NoNetWorkOrNoCountView netWorkOrNoCountView;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Intent intent;
            switch (msg.what) {
                case 999://购买PP
                    intent = new Intent(MyPPPActivity.this, PPPDetailProductActivity.class);
                    startActivity(intent);
                    if (pppPop.isShowing()) {
                        pppPop.dismiss();
                    }
                    break;

                case 888://扫描
                    intent = new Intent(MyPPPActivity.this, MipCaptureActivity.class);
                    intent.putExtra("type", "ppp");//只扫描ppp
                    startActivityForResult(intent, PPP_CODE);
                    if (pppPop.isShowing()) {
                        pppPop.dismiss();
                    }
                    break;

                case 1:
                    dialog.dismiss();
                    listPPP.setVisibility(View.VISIBLE);
                    PictureAirLog.v(TAG, "list=========" + list1.size());
                    listPPPAdapter = new ListOfPPPAdapter(list1, MyPPPActivity.this);
                    listPPP.setAdapter(listPPPAdapter);
                    break;

                case API.GET_PPP_SUCCESS://成功获取ppp信息
                    PictureAirLog.v(TAG, "成功获取PPP信息");
                    if (API.PPPlist.size() == 0) {
                        listPPP.setVisibility(View.GONE);
                        nopppLayout.setVisibility(View.VISIBLE);
                        text_instruction.setVisibility(View.VISIBLE);

                    } else {
                        Editor editor = sharedPreferences.edit();
                        editor.putInt(Common.PPP_COUNT, API.PPPlist.size());
                        editor.commit();
                        for (int i = 0; i < API.PPPlist.size(); i++) {
//						PPPinfo dayOfPPP = new PPPinfo();
                            PPPinfo ppPinfo = API.PPPlist.get(i);
//						dayOfPPP.time = ppPinfo.ownOn;
//						dayOfPPP.pppId = ppPinfo.PPPCode;
//						dayOfPPP.amount = ppPinfo.capacity;
                            //判断是否有可用的ppp
//						dayOfPPP.usedNumber = ppPinfo.bindInfo.size();
                            if (!hasOtherAvailablePPP) {
                                if (ppPinfo.bindInfo.size() < ppPinfo.capacity) {// 有空位的ppp
                                    hasOtherAvailablePPP = true;
                                }
                            }
                            // 需要对ppp进行排序
                            list1.add(ppPinfo);
                            PictureAirLog.v(TAG, "the ppp code ====>"
                                    + ppPinfo.PPPCode);
                        }
                        Collections.sort(list1);
                        PictureAirLog.v(TAG, "list-=--=" + list1.size());
                        listPPP.setVisibility(View.VISIBLE);
                        nopppLayout.setVisibility(View.GONE);
                        text_instruction.setVisibility(View.GONE);
                        listPPPAdapter = new ListOfPPPAdapter(list1, MyPPPActivity.this);
                        listPPP.setAdapter(listPPPAdapter);
                    }
                    netWorkOrNoCountView.setVisibility(View.GONE);
                    MyApplication.getInstance().setNeedRefreshPPPList(false);
                    dialog.dismiss();
                    break;

                case API.FAILURE://获取失败
                    if (msg.obj != null && msg.obj.toString().equals("PPHasUpgraded")) {
                        PictureAirLog.v(TAG, "PP has upgraded");
                        newToast.setTextAndShow(R.string.select_pp_hasUpgraded, Common.TOAST_SHORT_TIME);
                    } else {
                        newToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
                    }
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    break;

                case API.GET_PPP_FAILED:
                    if (msg.obj != null && msg.obj.toString().equals("PPHasUpgraded")) {
                        PictureAirLog.v(TAG, "PP has upgraded");
                        newToast.setTextAndShow(R.string.select_pp_hasUpgraded, Common.TOAST_SHORT_TIME);
                    }
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    netWorkOrNoCountView.setVisibility(View.VISIBLE);
                    netWorkOrNoCountView.setResult(R.string.no_network, R.string.click_button_reload, R.string.reload, R.drawable.no_network, mHandler, true);
                    listPPP.setVisibility(View.INVISIBLE);
                    nopppLayout.setVisibility(View.GONE);
                    text_instruction.setVisibility(View.GONE);
                    break;


                case API.SUCCESS://绑定成功
                    Editor editor = sharedPreferences.edit();
                    editor.putBoolean(Common.NEED_FRESH, true);
                    editor.commit();
                    list1.clear();
                    hasOtherAvailablePPP = false;
                    API.PPPlist.clear();
                    getData();
                    break;

                case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD://noView的按钮响应重新加载点击事件
                    //重新加载购物车数据
                    GetPPPList();
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ppp);
        initView();
    }

    private void initView() {
        pppPop = new PPPPop(this, mHandler);
        //初始化
        AppManager.getInstance().addActivity(this);
        newToast = new MyToast(this);
        sharedPreferences = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
        //找控件
        text_instruction = (TextView) findViewById(R.id.text_instruction);
        back = (ImageView) findViewById(R.id.back);
        setting = (ImageView) findViewById(R.id.ppp_setting);
        nopppLayout = (BannerView_PPPIntroduce) findViewById(R.id.nopppinfo);
        listPPP = (ListView) findViewById(R.id.list_ppp);
        netWorkOrNoCountView = (NoNetWorkOrNoCountView) findViewById(R.id.nonetwork_view);
        list3 = new ArrayList<>();
        list1 = new ArrayList<>();
        //设置需要刷新PPPList
        MyApplication.getInstance().setNeedRefreshPPPList(true);
        text_instruction.setOnClickListener(this);

        nopppLayout.setVisibility(View.INVISIBLE);
        listPPP.setVisibility(View.GONE);
//		optionImageView.setOnClickListener(this);
//		optoinTextView.setOnClickListener(this);
        back.setOnClickListener(this);
        setting.setOnClickListener(this);
        listPPP.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (list1.get(position).bindInfo.size() < list1.get(position).capacity) {
                    PictureAirLog.v(TAG, "pppSize :" + list1.get(position).PPPCode);
                    //是没用完的ppp  跳转到选择日期的界面。
                    Intent intent = new Intent(MyPPPActivity.this, MyPPActivity.class);
                    // 选择 页面 和 pp＋页面的标识
                    intent.putExtra("isSeletePP", true);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("ppp", list1.get(position));
                    intent.putExtras(bundle);
                    startActivity(intent);


                } else {
                    //用完了的PPP  弹出窗口提示
                    customdialog = new CustomDialog.Builder(MyPPPActivity.this).setMessage(getResources().getString(R.string.buy_ppp_tips))
                            .setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    // TODO Auto-generated method stub
                                    customdialog.dismiss();
                                }
                            })
                            .setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    // TODO Auto-generated method stub
                                    Intent intent = new Intent(MyPPPActivity.this, PPPDetailProductActivity.class);
                                    startActivity(intent);
                                    customdialog.dismiss();
                                }
                            })
                            .setCancelable(false)
                            .create();
                    customdialog.show();
                }

            }

        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MyApplication.getInstance().getNeedRefreshPPPList()) {
            GetPPPList();
        }
    }


    //获取ppp数据
    private void GetPPPList() {
        dialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
        list1.clear();
        hasOtherAvailablePPP = false;
        API.PPPlist.clear();//清空之前的list，从网络中重新获取
        getData();
    }

    /**
     * 设置window的透明度
     *
     * @param bgAlpha 0-1   透明---不透明
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getWindow().setAttributes(lp);
    }

    /**
     * 获取数据
     */
    private void getData() {
        if (API.PPPlist.size() == 0) {//没有数据，需要重新获取
            PictureAirLog.v(TAG, "ppp = 0");
            API.getPPPSByUserId(sharedPreferences.getString(Common.USERINFO_TOKENID, null), mHandler);
        } else {//有数据
            PictureAirLog.v(TAG, "ppp != 0");
            for (int i = 0; i < API.PPPlist.size(); i++) {
                PictureAirLog.v(TAG, "load==========");
                PPPinfo ppPinfo = API.PPPlist.get(i);
                String bindddateString = ppPinfo.bindInfo.get(0).bindDate;
                PictureAirLog.v(TAG, bindddateString);
                bindddateString = bindddateString.replace("[", "").replace("]", "").replaceAll("\"", "").trim();
                PictureAirLog.v(TAG, bindddateString);
                String[] timeStrings = bindddateString.split(",");
                if (timeStrings.length > 0) {
                    ppPinfo.ownOn = timeStrings[0];

                } else {
                    ppPinfo.ownOn = "";
                }
                String pplistString = ppPinfo.bindInfo.get(0).customerId;
                PictureAirLog.v(TAG, pplistString);
                String[] ppStrings = pplistString.split(",");

                for (int j = 0; j < ppStrings.length; j++) {
                    if (j == 0) {
                        PictureAirLog.v(TAG, j + ppStrings[0]);
                        if (null != ppStrings[0] && !"".equals(ppStrings[0])) {

                            ppPinfo.pp1 = ppStrings[0];
                        }
                    } else if (j == 1) {
                        if (null != ppStrings[1] && !"".equals(ppStrings[1])) {
                            PictureAirLog.v(TAG, j + ppStrings[1]);
                            ppPinfo.pp2 = ppStrings[1];
                        }
                    } else if (j == 2) {
                        if (null != ppStrings[2] && !"".equals(ppStrings[2])) {

                            PictureAirLog.v(TAG, j + ppStrings[2]);
                            ppPinfo.pp3 = ppStrings[2];
                        }
                    }
                }
                list1.add(ppPinfo);
            }
            Collections.sort(list1);
            mHandler.obtainMessage(1);
            MyApplication.getInstance().setNeedRefreshPPPList(false);
        }
    }

    //退出app进行的判断，判断是否是栈中的唯一一个app，如果是，启动主页
    private void doBack() {
        // TODO Auto-generated method stub
        if (AppManager.getInstance().getActivityCount() == 1) {//一个activity的时候
            Intent intent = new Intent(this, MainTabActivity.class);
            startActivity(intent);
        }
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            doBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.back://后退按钮
                doBack();
                break;

            case R.id.ppp_setting://设置按钮   + 按钮
                int[] location = new int[2];
                setting.getLocationOnScreen(location);
                pppPop.showAsDropDown(setting);
                break;

            case R.id.text_instruction:
                Intent intent = new Intent(MyPPPActivity.this, PPPDetailProductActivity.class);
                startActivity(intent);
                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        // 判断扫描返回的结果
        if (resultCode != RESULT_OK) {
            return;
        }
        if (data.getStringExtra("result").equals("pppOK")) {//ppp绑定成功，需要重新获取ppp信息
            GetPPPList();
        } else if (data.getStringExtra("result").equals("failed")) {//扫描失败
            PictureAirLog.v(TAG, "------>" + data.getStringExtra("errorType"));
            if (data.getStringExtra("errorType").equals("incomplete")) {
                errorMessage = getString(R.string.select_bind_pp_faile);
            } else if (data.getStringExtra("errorType").equals("invalidCode")) {
                errorMessage = getString(R.string.select_bind_pp_faile);
            } else if (data.getStringExtra("errorType").equals("errQueryUser")) {
                errorMessage = getString(R.string.select_bind_pp_faile);
            } else if (data.getStringExtra("errorType").equals("PPHasBind")) {
                if (requestCode == PPP_CODE) {
                    errorMessage = getString(R.string.not_ppp_card);
                } else {
                    errorMessage = getString(R.string.select_pp_hasUpgraded);
                }
            } else if (data.getStringExtra("errorType").equals("errQueryPPP")) {
                errorMessage = getString(R.string.select_bind_pp_faile);
            } else if (data.getStringExtra("errorType").equals("noPaidForPPP")) {
                errorMessage = getString(R.string.select_bind_pp_faile);
            } else if (data.getStringExtra("errorType").equals("invalidPPP")) {
                errorMessage = getString(R.string.select_bind_pp_faile);
            } else if (data.getStringExtra("errorType").equals("errQueryPhoto")) {
                errorMessage = getString(R.string.select_bind_pp_faile);
            } else if (data.getStringExtra("errorType").equals("PPPHasBind")) {
                if (requestCode == PP_CODE) {
                    errorMessage = getString(R.string.not_pp_card);
                } else {
                    errorMessage = getString(R.string.select_bind_pp_faile);
                }
            }
            customdialog = new CustomDialog.Builder(MyPPPActivity.this)
                    .setMessage(errorMessage)
                    .setNegativeButton(null, new DialogOnClickListener(false, null, false))
                    .setPositiveButton(getResources().getString(R.string.dialog_ok1), new DialogOnClickListener(false, null, false))
                    .setCancelable(false)
                    .create();
            customdialog.show();
        } else if (data.getStringExtra("result").equals("notSame")) {//卡片类型不一致
            //初始化dialog
            customdialog = new CustomDialog.Builder(MyPPPActivity.this)
                    .setMessage((requestCode == PP_CODE) ? getString(R.string.not_pp_card) : getString(R.string.not_ppp_card))
                    .setNegativeButton(null, new DialogOnClickListener(false, null, false))
                    .setPositiveButton(getResources().getString(R.string.dialog_ok1), new DialogOnClickListener(false, null, false))
                    .setCancelable(false)
                    .create();
            customdialog.show();
        } else {//返回pp码，弹框，询问是否绑定

            customdialog = new CustomDialog.Builder(MyPPPActivity.this)
                    .setMessage(getString(R.string.bind_pp_now))
                    .setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogOnClickListener(true, data.getStringExtra("result"), data.getBooleanExtra("hasBind", false)))
                    .setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogOnClickListener(true, data.getStringExtra("result"), data.getBooleanExtra("hasBind", false)))
                    .setCancelable(false)
                    .create();
            customdialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        AppManager.getInstance().killActivity(this);
    }

    //对话框监听类
    private class DialogOnClickListener implements DialogInterface.OnClickListener {

        private boolean needBind;
        private String PPCode;
        private boolean needBindToUser;

        public DialogOnClickListener(boolean needBind, String ppCode, boolean needBindToUser) {
            this.needBind = needBind;
            this.PPCode = ppCode;
            this.needBindToUser = needBindToUser;
        }

        @Override
        public void onClick(DialogInterface dialog1, int which) {
            // TODO Auto-generated method stub
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    PictureAirLog.v(TAG, "ok");
                    //如果点击ok，则自动绑定，首先要绑定要user上，然后再绑定到ppp上
                    if (needBind) {
//					dialog = ProgressDialog.show(MyPPPActivity.this, getString(R.string.loading___), getString(R.string.is_loading), false, false);
                        dialog = CustomProgressDialog.show(MyPPPActivity.this, getString(R.string.is_loading), false, null);
                        if (needBindToUser) {//是否已经绑定，如果已经绑定，则直接绑定到ppp，如果没有绑定，先绑定到user，在绑定到ppp
                            //已经被绑定了，所以直接绑定ppp
                            JSONArray pps = new JSONArray();
                            pps.put(PPCode);
                            API.bindPPsToPPP(sharedPreferences.getString(Common.USERINFO_TOKENID, null), pps, "", list1.get(currentPosition).PPPCode, mHandler);
                        } else {
                            //没有被绑定，则先绑到user，再绑到ppp
                            RequestParams params = new RequestParams();
                            params.put(Common.USERINFO_TOKENID, sharedPreferences.getString(Common.USERINFO_TOKENID, ""));
                            params.put(Common.CUSTOMERID, PPCode);
                            HttpUtil.get(Common.BASE_URL + Common.ADD_CODE_TO_USER, params, new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                    // TODO Auto-generated method stub
                                    super.onSuccess(statusCode, headers, response);
                                    if (statusCode == 200) {
                                        //绑定成功
                                        JSONArray pps = new JSONArray();
                                        pps.put(PPCode);
                                        API.bindPPsToPPP(sharedPreferences.getString(Common.USERINFO_TOKENID, null), pps, "", list1.get(currentPosition).PPPCode, mHandler);
                                    }
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                    // TODO Auto-generated method stub
                                    super.onFailure(statusCode, headers, throwable, errorResponse);
                                    //绑定失败
                                    dialog.dismiss();
                                    newToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
                                }
                            });
                        }
                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    PictureAirLog.v(TAG, "no");
                    break;

                default:
                    break;
            }
            dialog1.dismiss();
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }


}
