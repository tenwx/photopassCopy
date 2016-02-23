package com.pictureair.photopass.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.ListOfPPPAdapter;
import com.pictureair.photopass.customDialog.CustomDialog;
import com.pictureair.photopass.entity.CartItemInfo1;
import com.pictureair.photopass.entity.CartPhotosInfo1;
import com.pictureair.photopass.entity.GoodsInfo1;
import com.pictureair.photopass.entity.GoodsInfoJson;
import com.pictureair.photopass.entity.PPPinfo;
import com.pictureair.photopass.eventbus.BaseBusEvent;
import com.pictureair.photopass.eventbus.ScanInfoEvent;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.JsonTools;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;
import com.pictureair.photopass.widget.PPPPop;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.smssdk.gui.CustomProgressDialog;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

/**
 * 显示用户所有的PP+或是对应某个PP而言可使用的PP+
 */
public class MyPPPActivity extends BaseActivity implements OnClickListener {
    private static final String TAG = "MyPPPActivity";

    private ImageView setting;
    private ListView listPPP;
    private ImageView back;
    private Button button_buy_ppp,button_scan_ppp; // 无PP＋时 底部的两个按钮。
    private LinearLayout ll_button_area;//无PP＋时 底部的两个按钮的区域。

    //    private BannerView_PPPIntroduce nopppLayout;
    private LinearLayout nopppLayout;

    private CustomProgressDialog dialog;
    private MyToast newToast;
    private CustomDialog customdialog;

    private ListOfPPPAdapter listPPPAdapter;
    private ArrayList<PPPinfo> list1;// 绑定了pp的pp+
    private SharedPreferences sharedPreferences;

    private boolean hasOtherAvailablePPP = false;//判断是否还有其他可用的ppp
    private int currentPosition = 0;//记录选中的项的索引值

    private String errorMessage = "";
    private PPPPop pppPop;
    private NoNetWorkOrNoCountView netWorkOrNoCountView;
    private PPPinfo ppp;

    private List<GoodsInfo1> allGoodsList;//全部商品
    private GoodsInfo1 pppGoodsInfo;
    private String[] photoUrls;
    private String PPCode;

    private final Handler myPPPHandler = new MyPPPHandler(this);


    private static class MyPPPHandler extends Handler{
        private final WeakReference<MyPPPActivity> mActivity;

        public MyPPPHandler(MyPPPActivity activity){
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
        Intent intent;
        switch (msg.what) {
            case 999://购买PPP
                //购买PP+，先获取商品 然后进入订单界面
                dialog = CustomProgressDialog.show(MyPPPActivity.this, getString(R.string.is_loading), false, null);
                //获取商品（以后从缓存中取）
                getGoods();
                if (pppPop.isShowing()) {
                    pppPop.dismiss();
                }
                break;

            case 888://扫描
                intent = new Intent(MyPPPActivity.this, MipCaptureActivity.class);
                intent.putExtra("type", "ppp");//只扫描ppp
                startActivity(intent);
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

            case API1.GET_PPP_SUCCESS://成功获取ppp信息
                if (API1.PPPlist.size() == 0) {
                    listPPP.setVisibility(View.GONE);
                    nopppLayout.setVisibility(View.VISIBLE);
                    ll_button_area.setVisibility(View.VISIBLE);

                } else {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(Common.PPP_COUNT, API1.PPPlist.size());
                    editor.commit();
                    for (int i = 0; i < API1.PPPlist.size(); i++) {
//						PPPinfo dayOfPPP = new PPPinfo();
                        PPPinfo ppPinfo = API1.PPPlist.get(i);
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
                    ll_button_area.setVisibility(View.GONE);
                    listPPPAdapter = new ListOfPPPAdapter(list1, MyPPPActivity.this);
                    listPPP.setAdapter(listPPPAdapter);
                }
                netWorkOrNoCountView.setVisibility(View.GONE);
                MyApplication.getInstance().setNeedRefreshPPPList(false);
                dialog.dismiss();
                break;

            case API1.BIND_PP_FAILURE://获取失败
                if (msg.obj != null && msg.obj.toString().equals("PPHasUpgraded")) {
                    PictureAirLog.v(TAG, "PP has upgraded");
                    newToast.setTextAndShow(R.string.select_pp_hasUpgraded, Common.TOAST_SHORT_TIME);
                } else {
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                }
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                break;

            case API1.GET_PPP_FAILED:
                if (msg.obj != null && msg.obj.toString().equals("PPHasUpgraded")) {
                    PictureAirLog.v(TAG, "PP has upgraded");
                    newToast.setTextAndShow(R.string.select_pp_hasUpgraded, Common.TOAST_SHORT_TIME);
                }
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                netWorkOrNoCountView.setVisibility(View.VISIBLE);
                netWorkOrNoCountView.setResult(R.string.no_network, R.string.click_button_reload, R.string.reload, R.drawable.no_network, myPPPHandler, true);
                listPPP.setVisibility(View.INVISIBLE);
                nopppLayout.setVisibility(View.GONE);
                ll_button_area.setVisibility(View.GONE);
                break;


            case API1.BIND_PP_SUCCESS://绑定成功
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(Common.NEED_FRESH, true);
                editor.commit();
                list1.clear();
                hasOtherAvailablePPP = false;
                API1.PPPlist.clear();
                getData();
                break;

            case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD://noView的按钮响应重新加载点击事件
                //重新加载购物车数据
                GetPPPList();
                break;
            case API1.GET_PPS_BY_PPP_AND_DATE_SUCCESS:
                PictureAirLog.e(TAG, "GET_PPS_BY_PPP_AND_DATE_SUCCESS");
                intent = new Intent(MyPPPActivity.this, MyPPActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("ppp", ppp);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case API1.GET_PPS_BY_PPP_AND_DATE_FAILED:
                PictureAirLog.e(TAG, "GET_PPS_BY_PPP_AND_DATE_FAILED");
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
                break;

            case API1.GET_GOODS_SUCCESS:
                GoodsInfoJson goodsInfoJson = JsonTools.parseObject(msg.obj.toString(), GoodsInfoJson.class);//GoodsInfoJson.getString()
                if (goodsInfoJson != null && goodsInfoJson.getGoods().size() > 0) {
                    allGoodsList = goodsInfoJson.getGoods();
                    PictureAirLog.v(TAG, "goods size: " + allGoodsList.size());
                }
                //获取PP+
                for (GoodsInfo1 goodsInfo : allGoodsList) {
                    if (goodsInfo.getName().equals(Common.GOOD_NAME_PPP)) {
                        pppGoodsInfo = goodsInfo;
                        //封装购物车宣传图
                        photoUrls = new String[goodsInfo.getPictures().size()];
                        for (int i = 0; i < goodsInfo.getPictures().size(); i++) {
                            photoUrls[i] = goodsInfo.getPictures().get(i).getUrl();
                        }
                        break;
                    }
                }
                //将数据保存到缓存中
                if (ACache.get(MyApplication.getInstance()).getAsString(Common.ALL_GOODS) == null || ACache.get(MyApplication.getInstance()).getAsString(Common.ALL_GOODS).equals("")) {
                    ACache.get(MyApplication.getInstance()).put(Common.ALL_GOODS, msg.obj.toString(), ACache.GOODS_ADDRESS_ACACHE_TIME);
                }
                API1.addToCart(pppGoodsInfo.getGoodsKey(), 1, true, null, myPPPHandler);
                break;
            case API1.GET_GOODS_FAILED:
                dialog.dismiss();
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
                break;

            case API1.ADD_TO_CART_FAILED:
                dialog.dismiss();
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);

                break;

            case API1.ADD_TO_CART_SUCCESS:
                dialog.dismiss();
                com.alibaba.fastjson.JSONObject jsonObject = (com.alibaba.fastjson.JSONObject) msg.obj;
                editor = sharedPreferences.edit();
                editor.putInt(Common.CART_COUNT, sharedPreferences.getInt(Common.CART_COUNT, 0) + 1);
                editor.commit();
                String cartId = jsonObject.getString("cartId");

                //生成订单
                Intent intent1 = new Intent(MyPPPActivity.this, SubmitOrderActivity.class);
                ArrayList<CartItemInfo1> orderinfoArrayList = new ArrayList<>();
                CartItemInfo1 cartItemInfo1 = new CartItemInfo1();
                cartItemInfo1.setCartId(cartId);
                cartItemInfo1.setProductName(pppGoodsInfo.getName());
                cartItemInfo1.setUnitPrice(pppGoodsInfo.getPrice());
                cartItemInfo1.setEmbedPhotos(new ArrayList<CartPhotosInfo1>());
                cartItemInfo1.setDescription(pppGoodsInfo.getDescription());
                cartItemInfo1.setQty(1);
                cartItemInfo1.setStoreId(pppGoodsInfo.getStoreId());
                cartItemInfo1.setPictures(photoUrls);
                cartItemInfo1.setPrice(pppGoodsInfo.getPrice());
                cartItemInfo1.setCartProductType(3);

                orderinfoArrayList.add(cartItemInfo1);
                intent1.putExtra("orderinfo", orderinfoArrayList);
                startActivity(intent1);
                break;

            case API1.ADD_CODE_TO_USER_SUCCESS:
                //绑定成功
                dialog.dismiss();
                JSONArray pps = new JSONArray();
                pps.add(PPCode);
                API1.bindPPsToPPP(sharedPreferences.getString(Common.USERINFO_TOKENID, null), pps, "", list1.get(currentPosition).PPPCode, myPPPHandler);

                break;
            case API1.ADD_CODE_TO_USER_FAILED:
                //绑定失败
                dialog.dismiss();
                newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);

                break;

            default:
                break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ppp);
        initView();
    }

    private void initView() {
        pppPop = new PPPPop(this, myPPPHandler);
        //初始化
        newToast = new MyToast(this);
        sharedPreferences = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
        //找控件
        button_buy_ppp = (Button) findViewById(R.id.button_buy_ppp);
        button_scan_ppp = (Button) findViewById(R.id.button_scan_ppp);
        button_buy_ppp.setTypeface(MyApplication.getInstance().getFontBold());
        button_scan_ppp.setTypeface(MyApplication.getInstance().getFontBold());

        ll_button_area = (LinearLayout) findViewById(R.id.ll_button_area);
        back = (ImageView) findViewById(R.id.back);
        setting = (ImageView) findViewById(R.id.ppp_setting);
        nopppLayout = (LinearLayout) findViewById(R.id.nopppinfo);
        listPPP = (ListView) findViewById(R.id.list_ppp);
        netWorkOrNoCountView = (NoNetWorkOrNoCountView) findViewById(R.id.nonetwork_view);
        list1 = new ArrayList<>();
        //设置需要刷新PPPList
        MyApplication.getInstance().setNeedRefreshPPPList(true);
        button_buy_ppp.setOnClickListener(this);
        button_scan_ppp.setOnClickListener(this);

        nopppLayout.setVisibility(View.INVISIBLE);
        ll_button_area.setVisibility(View.GONE);
        listPPP.setVisibility(View.GONE);
//		optionImageView.setOnClickListener(this);
//		optoinTextView.setOnClickListener(this);
        back.setOnClickListener(this);
        setting.setOnClickListener(this);
        listPPP.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (list1.get(position).bindInfo.size() < list1.get(position).capacity && list1.get(position).expired == 0) {
                    if (list1.get(position).expericePPP == 1) {//体验卡
                        Intent intent = new Intent(MyPPPActivity.this, SelectPhotoActivity.class);
                        intent.putExtra("activity", "mypppactivity");
                        intent.putExtra("pppCode", list1.get(position).PPPCode);
                        intent.putExtra("photoCount", 1);
                        startActivity(intent);
                    } else {
                        PictureAirLog.v(TAG, "pppSize :" + list1.get(position).PPPCode);
                        ppp = list1.get(position);
                        API1.getPPsByPPPAndDate(ppp.PPPCode, myPPPHandler);
                    }
                }

            }

        });

    }


    //获取ppp数据
    private void GetPPPList() {
        dialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
        list1.clear();
        hasOtherAvailablePPP = false;
        API1.PPPlist.clear();//清空之前的list，从网络中重新获取
        getData();
    }

    /**
     * 获取数据
     */
    private void getData() {
        if (API1.PPPlist.size() == 0) {//没有数据，需要重新获取
            PictureAirLog.v(TAG, "ppp = 0");
            API1.getPPPSByUserId(sharedPreferences.getString(Common.USERINFO_TOKENID, null), myPPPHandler);
        } else {//有数据
            PictureAirLog.v(TAG, "ppp != 0");
            for (int i = 0; i < API1.PPPlist.size(); i++) {
                PictureAirLog.v(TAG, "load==========");
                PPPinfo ppPinfo = API1.PPPlist.get(i);
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
            myPPPHandler.obtainMessage(1);
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

            case R.id.button_buy_ppp:
                //购买PP+，先获取商品 然后进入订单界面
                if (!isNetWorkConnect(MyApplication.getInstance())) {
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    return;
                }
                dialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
                //获取商品（以后从缓存中取）
                getGoods();
                break;

            case R.id.button_scan_ppp:
                Intent intent = new Intent(MyPPPActivity.this, MipCaptureActivity.class);
                intent.putExtra("type", "ppp");//只扫描ppp
                startActivity(intent);
                break;

            default:
                break;
        }
    }

    /**
     * 初始化数据
     */
    public void getGoods() {
        //从缓层中获取数据
        String goodsByACache = ACache.get(MyPPPActivity.this).getAsString(Common.ALL_GOODS);
        if (goodsByACache != null && !goodsByACache.equals("")) {
            myPPPHandler.obtainMessage(API1.GET_GOODS_SUCCESS, goodsByACache).sendToTarget();
        } else {
            //从网络获取商品,先检查网络
            if (AppUtil.getNetWorkType(MyApplication.getInstance()) != 0) {
                API1.getGoods(myPPPHandler);
            } else {
                newToast.setTextAndShow(R.string.http_error_code_401, Toast.LENGTH_SHORT);
            }
        }

    }


    @Subscribe
    public void onUserEvent(BaseBusEvent baseBusEvent) {
        if (baseBusEvent instanceof ScanInfoEvent) {
            ScanInfoEvent scanInfoEvent = (ScanInfoEvent) baseBusEvent;
            String result = scanInfoEvent.getResult();
            PictureAirLog.out("myppp----->" + result);
            if (result.equals("pppOK")) {//ppp绑定成功，需要重新获取ppp信息
                GetPPPList();
            } else if (result.equals("failed")) {//扫描失败
                int id = scanInfoEvent.getErrorType();
                PictureAirLog.v(TAG, "------>" + id);
                switch (id) {
                    case R.string.http_error_code_6055:
                        errorMessage = getString(R.string.not_ppp_card);
                        break;

                    default:
                        errorMessage = getString(id);
                        break;

                }

                customdialog = new CustomDialog.Builder(MyPPPActivity.this)
                        .setMessage(errorMessage)
                        .setNegativeButton(null, new DialogOnClickListener(false, null, false))
                        .setPositiveButton(getResources().getString(R.string.dialog_ok1), new DialogOnClickListener(false, null, false))
                        .setCancelable(false)
                        .create();
                customdialog.show();
            } else if (result.equals("notSame")) {//卡片类型不一致
                //初始化dialog
                customdialog = new CustomDialog.Builder(MyPPPActivity.this)
                        .setMessage(getString(R.string.not_ppp_card))
                        .setNegativeButton(null, new DialogOnClickListener(false, null, false))
                        .setPositiveButton(getResources().getString(R.string.dialog_ok1), new DialogOnClickListener(false, null, false))
                        .setCancelable(false)
                        .create();
                customdialog.show();
            } else {//返回pp码，弹框，询问是否绑定

                customdialog = new CustomDialog.Builder(MyPPPActivity.this)
                        .setMessage(getString(R.string.bind_pp_now))
                        .setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogOnClickListener(true, result, scanInfoEvent.isHasBind()))
                        .setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogOnClickListener(true, result, scanInfoEvent.isHasBind()))
                        .setCancelable(false)
                        .create();
                customdialog.show();
            }
            EventBus.getDefault().removeStickyEvent(scanInfoEvent);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (MyApplication.getInstance().getNeedRefreshPPPList()) { //解决问题，从PPP页面扫描PP+成功之后不刷新的问题。
            GetPPPList();
        }
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        myPPPHandler.removeCallbacksAndMessages(null);
    }

    //对话框监听类
    private class DialogOnClickListener implements DialogInterface.OnClickListener {

        private boolean needBind;
        private boolean needBindToUser;

        public DialogOnClickListener(boolean needBind, String ppCode, boolean needBindToUser) {
            this.needBind = needBind;
            PPCode = ppCode;
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
                            pps.add(PPCode);
                            API1.bindPPsToPPP(sharedPreferences.getString(Common.USERINFO_TOKENID, null), pps, "", list1.get(currentPosition).PPPCode, myPPPHandler);
                        } else {
                            //没有被绑定，则先绑到user，再绑到ppp
                            API1.addCodeToUser(PPCode, myPPPHandler);
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
