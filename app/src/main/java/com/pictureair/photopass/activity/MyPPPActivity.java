package com.pictureair.photopass.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.ListOfPPPAdapter;
import com.pictureair.photopass.customDialog.CustomDialog;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.CartPhotosInfo;
import com.pictureair.photopass.entity.GoodsInfo;
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
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;
import com.pictureair.photopass.widget.PPPPop;
import com.pictureair.photopass.widget.PWToast;
import com.pictureair.photopass.widget.PictureWorksDialog;
import com.pictureair.photopass.widget.pullloadlayout.MyListView;
import com.pictureair.photopass.widget.pullloadlayout.OnRefreshListener;
import com.pictureair.photopass.widget.pullloadlayout.ReFreshLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

/**
 * 显示用户所有的PP+或是对应某个PP而言可使用的PP+
 */
public class MyPPPActivity extends BaseActivity implements OnClickListener,OnRefreshListener {
    private static final String TAG = "MyPPPActivity";
    private boolean isUseHavedPPP = false;
    private ImageView setting;
    private MyListView listPPP;
    private ImageView back;
    private Button button_buy_ppp, button_scan_ppp; // 无PP＋时 底部的两个按钮。
    private LinearLayout ll_button_area;//无PP＋时 底部的两个按钮的区域。

    //    private BannerView_PPPIntroduce nopppLayout;
    private LinearLayout nopppLayout;

    private CustomProgressDialog dialog;
    private PWToast newToast;
    private CustomDialog customdialog;

    private ListOfPPPAdapter listPPPAdapter;
    private ArrayList<PPPinfo> list1;// 绑定了pp的pp+
    private ArrayList<PPPinfo> listNormal;// 已激活未激活的pp+
    private ArrayList<PPPinfo> listNoUse;// 已过期已用完的pp+
    private PWToast myToast;

    private SharedPreferences sharedPreferences;

    private boolean hasOtherAvailablePPP = false;//判断是否还有其他可用的ppp
    private int currentPosition = 0;//记录选中的项的索引值

    private String errorMessage = "";
    private PPPPop pppPop;
    private NoNetWorkOrNoCountView netWorkOrNoCountView;
    private PPPinfo ppp;

    private List<GoodsInfo> allGoodsList;//全部商品
    private GoodsInfo pppGoodsInfo;
    private String[] photoUrls;
    private String PPCode;

    // 选择PP＋需要的组件
    private TextView ok;
    private String ppsStr;
    private TextView mTitle;
    private PictureWorksDialog pictureWorksDialog;
    private RelativeLayout menuLayout;

    private static final int SCAN_PPP_CODE_SUCCESS = 111;

    private boolean isOnResume = false;

    private ScanInfoEvent scanInfoEvent;

    private int status = 0;
    private final int normal = 1;
    private final int unUse = 2;
    private final int full = 3;
    private ReFreshLayout refreshLayout;

    private final Handler myPPPHandler = new MyPPPHandler(this);

    @Override
    public void onRefresh() {
        Message message = new Message();
        message.what = 3;
        myPPPHandler.sendMessageDelayed(message,1000);
    }


    private static class MyPPPHandler extends Handler {
        private final WeakReference<MyPPPActivity> mActivity;

        public MyPPPHandler(MyPPPActivity activity) {
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
     *
     * @param msg
     */
    private void dealHandler(Message msg) {
        Intent intent;
        switch (msg.what) {
            case SCAN_PPP_CODE_SUCCESS:
                if (isOnResume) {
                    dealPPPresult();
                } else {
                    myPPPHandler.sendEmptyMessageDelayed(SCAN_PPP_CODE_SUCCESS, 50);
                }
                break;

            case PPPPop.POP_BUY://购买PPP
                //购买PP+，先获取商品 然后进入订单界面
                if (!dialog.isShowing()) {
                    dialog.show();
                }
                //获取商品（以后从缓存中取）
                getGoods();
                if (pppPop.isShowing()) {
                    pppPop.dismiss();
                }
                break;

            case PPPPop.POP_SCAN://扫描
                intent = new Intent(MyPPPActivity.this, MipCaptureActivity.class);
                intent.putExtra("type", "ppp");//只扫描ppp
                startActivity(intent);
                if (pppPop.isShowing()) {
                    pppPop.dismiss();
                }
                break;

            case PPPPop.POP_INPUT://手动输入
                intent = new Intent(MyPPPActivity.this, InputCodeActivity.class);
                intent.putExtra("type", "ppp");//只扫描ppp
                startActivity(intent);
                if (pppPop.isShowing()) {
                    pppPop.dismiss();
                }
                break;

            case 1:
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                refreshLayout.setVisibility(View.VISIBLE);
                PictureAirLog.v(TAG, "list=========" + list1.size());
                listPPPAdapter = new ListOfPPPAdapter(list1, isUseHavedPPP, myPPPHandler,MyPPPActivity.this);
                listPPP.setAdapter(listPPPAdapter);
                break;

            case API1.GET_PPP_SUCCESS://成功获取ppp信息
                if (API1.PPPlist.size() == 0) {
                    refreshLayout.setVisibility(View.GONE);
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
                                + ppPinfo.PPPCode + "expericePPP===>" + ppPinfo.expericePPP + "capacity" + ppPinfo.capacity);
                    }
                    Collections.sort(list1);
                    PictureAirLog.v(TAG, "list-=--=" + list1.size());
                    for (int i = 0; i<list1.size(); i++) {
                        PPPinfo info = list1.get(i);
                        if (info.bindInfo.size() < info.capacity && info.expired == 0) {
                            if (listNormal == null) listNormal = new ArrayList<>();
                            listNormal.add(info);
                        } else {
                            if (listNoUse == null) listNoUse = new ArrayList<>();
                            listNoUse.add(info);
                        }
                    }
                    refreshLayout.setVisibility(View.VISIBLE);
                    nopppLayout.setVisibility(View.GONE);
                    ll_button_area.setVisibility(View.GONE);
                    if (listNormal != null && listNormal.size() > 0) {
                        status = normal;
                        listPPPAdapter = new ListOfPPPAdapter(listNormal, isUseHavedPPP, myPPPHandler, MyPPPActivity.this);
                    }else if (listNoUse != null && listNoUse.size() > 0) {
                        status = unUse;
                        listPPPAdapter = new ListOfPPPAdapter(listNoUse, isUseHavedPPP, myPPPHandler, MyPPPActivity.this);
                    }
                    listPPP.setAdapter(listPPPAdapter);
                }
                netWorkOrNoCountView.setVisibility(View.GONE);
                MyApplication.getInstance().setNeedRefreshPPPList(false);
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
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
                refreshLayout.setVisibility(View.INVISIBLE);
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
                intent = new Intent(MyPPPActivity.this, MyPPActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("ppp", ppp);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case API1.GET_PPS_BY_PPP_AND_DATE_FAILED:
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
                break;

            case API1.GET_GOODS_SUCCESS:
                GoodsInfoJson goodsInfoJson = JsonTools.parseObject(msg.obj.toString(), GoodsInfoJson.class);//GoodsInfoJson.getString()
                if (goodsInfoJson != null && goodsInfoJson.getGoods().size() > 0) {
                    allGoodsList = goodsInfoJson.getGoods();
                    PictureAirLog.v(TAG, "goods size: " + allGoodsList.size());
                }
                //获取PP+
                for (GoodsInfo goodsInfo : allGoodsList) {
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
                    ACache.get(MyApplication.getInstance()).put(Common.ALL_GOODS, msg.obj.toString(), ACache.TIME_DAY);
                }
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                //跳转到PP+详情页面
                Intent intent2 = new Intent(this, PPPDetailProductActivity.class);
                intent2.putExtra("goods", pppGoodsInfo);
                startActivity(intent2);
                break;

            case API1.GET_GOODS_FAILED:
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
                break;

            case API1.ADD_TO_CART_FAILED:
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);

                break;

            case API1.ADD_TO_CART_SUCCESS:
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                com.alibaba.fastjson.JSONObject jsonObject = (com.alibaba.fastjson.JSONObject) msg.obj;
                editor = sharedPreferences.edit();
                editor.putInt(Common.CART_COUNT, sharedPreferences.getInt(Common.CART_COUNT, 0) + 1);
                editor.commit();
                String cartId = jsonObject.getString("cartId");

                //生成订单
                Intent intent1 = new Intent(MyPPPActivity.this, SubmitOrderActivity.class);
                ArrayList<CartItemInfo> orderinfoArrayList = new ArrayList<>();
                CartItemInfo cartItemInfo = new CartItemInfo();
                cartItemInfo.setCartId(cartId);
                cartItemInfo.setProductName(pppGoodsInfo.getName());
                cartItemInfo.setProductNameAlias(pppGoodsInfo.getNameAlias());
                cartItemInfo.setUnitPrice(pppGoodsInfo.getPrice());
                cartItemInfo.setEmbedPhotos(new ArrayList<CartPhotosInfo>());
                cartItemInfo.setDescription(pppGoodsInfo.getDescription());
                cartItemInfo.setQty(1);
                cartItemInfo.setStoreId(pppGoodsInfo.getStoreId());
                cartItemInfo.setPictures(photoUrls);
                cartItemInfo.setPrice(pppGoodsInfo.getPrice());
                cartItemInfo.setCartProductType(3);

                orderinfoArrayList.add(cartItemInfo);
                intent1.putExtra("orderinfo", orderinfoArrayList);
                startActivity(intent1);
                break;

            case API1.ADD_CODE_TO_USER_SUCCESS:
                //绑定成功
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                JSONArray pps = new JSONArray();
                pps.add(PPCode);
                API1.bindPPsToPPP(MyApplication.getTokenId(), pps, "", list1.get(currentPosition).PPPCode, myPPPHandler);

                break;
            case API1.ADD_CODE_TO_USER_FAILED:
                //绑定失败
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);

                break;
            case 2:
                ok.setText(formaStringPPP(listPPPAdapter.getMap().size(),1));
                if (listPPPAdapter.getMap().size() >=1){
                    ok.setEnabled(true);
                    ok.setTextColor(getResources().getColor(R.color.white));
                }else{
                    ok.setEnabled(false);
                    ok.setTextColor(getResources().getColor(R.color.gray_light5));
                }
                break;
            case 3:
                if (status == normal) {
                    if (listNoUse == null || listNoUse.size() < 1) {
                        myToast.setTextAndShow(R.string.ppp_load_all);
                    } else {
                        listPPPAdapter.setArrayList(list1);
                    }
                    status = full;
                    finishLoad();
                    listPPPAdapter.notifyDataSetChanged();
                    listPPP.setSelection(listPPP.getLastVisiblePosition()+1);
                }else if (status == unUse) {
                    status = full;
                    myToast.setTextAndShow(R.string.ppp_load_all);
                    finishLoad();
                }else if (status == full){
                    myToast.setTextAndShow(R.string.ppp_load_all);
                    finishLoad();
                }
                break;
            case API1.BIND_PPS_DATE_TO_PP_SUCESS://绑定成功
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (API1.PPPlist.size() != 0 ){
                    API1.PPPlist.clear(); // 绑定成功 之后 清空API中的数据。
                }
                if (AppManager.getInstance().checkActivity(SelectPPActivity.class)){ //SelectPPActivity，就把这个类杀掉。
                    AppManager.getInstance().killActivity(SelectPPActivity.class);
                }

                SharedPreferences.Editor editor1 = sharedPreferences.edit();  //设置需要刷新 （其实可以不需要，不过保证数据同步，加上更保险）
                editor1.putBoolean(Common.NEED_FRESH, true);
                editor1.commit();

                if (AppManager.getInstance().checkActivity(PreviewPhotoActivity.class)){ //如果存在MyPPActivity，就把这个类杀掉。
                    AppManager.getInstance().killActivity(PreviewPhotoActivity.class);
                }

                intent = new Intent(MyPPPActivity.this, PreviewPhotoActivity.class);
                Bundle bundle1 = new Bundle();
                bundle1.putInt("position", -2);  //代表从 pp＋绑定PP
                bundle1.putString("tab", sharedPreferences.getString("tabName",""));
                bundle1.putString("ppsStr",ppsStr);
                intent.putExtra("bundle", bundle1);
                startActivity(intent);
                this.finish();
                break;
            case API1.BIND_PPS_DATE_TO_PP_FAILED: //绑定失败。
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                newToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), msg.arg1), Common.TOAST_SHORT_TIME);
                break;
            case DialogInterface.BUTTON_POSITIVE:
                if (listPPPAdapter.getMap().size() == 1){
                    if (!dialog.isShowing()) {
                        dialog.show();
                    }
                    API1.bindPPsDateToPPP(JSONArray.parseArray(ppsStr), API1.PPPlist.get(listPPPAdapter.getOnclickPosition()).PPPCode, myPPPHandler);
                }else{
                    newToast.setTextAndShow(R.string.select_your_ppp, Common.TOAST_SHORT_TIME);
                }
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                break;
            case ListOfPPPAdapter.HEAD_CLICK:
                int position = (int)msg.obj;
                if (list1.get(position).bindInfo.size() < list1.get(position).capacity && list1.get(position).expired == 0) {
                    if (list1.get(position).expericePPP == 1) {//体验卡
                        Intent intent3 = new Intent(MyPPPActivity.this, SelectPhotoActivity.class);
                        intent3.putExtra("activity", "mypppactivity");
                        intent3.putExtra("pppCode", list1.get(position).PPPCode);
                        intent3.putExtra("photoCount", 1);
                        startActivity(intent3);
                    } else {
                        PictureAirLog.v(TAG, "pppSize :" + list1.get(position).PPPCode);
                        ppp = list1.get(position);
                        API1.getPPsByPPPAndDate(ppp.PPPCode, myPPPHandler);
                    }
                }

                break;

            default:
                break;
        }
    }

    private void finishLoad() {
        refreshLayout.finishRefreshing();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ppp);
        initViewCommon();
        isUseHavedPPP =  getIntent().getBooleanExtra("isUseHavedPPP",false);
        if (isUseHavedPPP){
            ppsStr = getIntent().getStringExtra("ppsStr");
            initViewUseHavedPPP();
        }else{
            initView();
        }
    }
    private void initViewCommon(){
        pppPop = new PPPPop(this, myPPPHandler, PPPPop.MENU_TYPE_PPP);
        //初始化
        newToast = new PWToast(this);
        sharedPreferences = getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, MODE_PRIVATE);

        dialog = CustomProgressDialog.create(this, getString(R.string.is_loading), false, null);

        ll_button_area = (LinearLayout) findViewById(R.id.ll_button_area);
        back = (ImageView) findViewById(R.id.back);
        menuLayout = (RelativeLayout) findViewById(R.id.ppp_rl);
        setting = (ImageView) findViewById(R.id.ppp_setting);
        nopppLayout = (LinearLayout) findViewById(R.id.nopppinfo);
        listPPP = (MyListView) findViewById(R.id.list_ppp);
        refreshLayout = (ReFreshLayout) findViewById(R.id.ppp_refresh);
        netWorkOrNoCountView = (NoNetWorkOrNoCountView) findViewById(R.id.nonetwork_view);
        nopppLayout.setVisibility(View.INVISIBLE);
        ll_button_area.setVisibility(View.GONE);
        refreshLayout.setVisibility(View.GONE);
        back.setOnClickListener(this);
        menuLayout.setOnClickListener(this);
    }

    private void initViewUseHavedPPP(){
        mTitle = (TextView) findViewById(R.id.myppp);
        mTitle.setText(R.string.select_ppp_title);
        // 显示右上角 ok 按钮。隐藏 ＋ 号
        ok = (TextView) findViewById(R.id.ok);
        ok.setVisibility(View.VISIBLE);
        ok.setText(formaStringPPP(0, 1));
        ok.setOnClickListener(this);
        setting.setVisibility(View.GONE);
        ok.setEnabled(false);
        ok.setTextColor(getResources().getColor(R.color.gray_light5));
        refreshLayout.setVisibility(View.VISIBLE);
        listPPPAdapter = new ListOfPPPAdapter(API1.PPPlist, isUseHavedPPP, myPPPHandler,MyPPPActivity.this);
        listPPP.setAdapter(listPPPAdapter);
    }

    private void initView() {
        //找控件
        button_buy_ppp = (Button) findViewById(R.id.button_buy_ppp);
        button_scan_ppp = (Button) findViewById(R.id.button_scan_ppp);
        button_buy_ppp.setTypeface(MyApplication.getInstance().getFontBold());
        button_scan_ppp.setTypeface(MyApplication.getInstance().getFontBold());
        list1 = new ArrayList<>();
        //设置需要刷新PPPList
        MyApplication.getInstance().setNeedRefreshPPPList(true);
        button_buy_ppp.setOnClickListener(this);
        button_scan_ppp.setOnClickListener(this);
//		optionImageView.setOnClickListener(this);
//		optoinTextView.setOnClickListener(this);
        myToast = new PWToast(this);
        refreshLayout.setListView(listPPP);
        refreshLayout.setOnRefreshListener(this);

    }

    //获取ppp数据
    private void GetPPPList() {
        if (!dialog.isShowing()) {
            dialog.show();
        }
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
            API1.getPPPSByUserId(MyApplication.getTokenId(), myPPPHandler);
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

            case R.id.ppp_rl://设置按钮   + 按钮
                pppPop.showAsDropDown(setting, 0, ScreenUtil.dip2px(MyPPPActivity.this, 15) - 10);
                break;

            case R.id.button_buy_ppp:
                //购买PP+，先获取商品 然后进入订单界面
                if (!isNetWorkConnect(MyApplication.getInstance())) {
                    newToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
                    return;
                }
                if (!dialog.isShowing()) {
                    dialog.show();
                }
                //获取商品（以后从缓存中取）
                getGoods();
                break;

            case R.id.button_scan_ppp:
                Intent intent = new Intent(MyPPPActivity.this, MipCaptureActivity.class);
                intent.putExtra("type", "ppp");//只扫描ppp
                startActivity(intent);
                break;
            case R.id.ok: // 确定选择之后
                createDialog();
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
            scanInfoEvent = (ScanInfoEvent) baseBusEvent;
            String result = scanInfoEvent.getResult();
            PictureAirLog.out("myppp----->" + result);
            if (scanInfoEvent.getCodeType().equals("ppp")) {
                myPPPHandler.sendEmptyMessageDelayed(SCAN_PPP_CODE_SUCCESS, 50);
            }
            EventBus.getDefault().removeStickyEvent(scanInfoEvent);
        }
    }

    /**
     * 处理扫描得到的ppp
     */
    private void dealPPPresult() {
        String pppResultStr = scanInfoEvent.getResult();
        if (pppResultStr.equals("pppOK")) {//ppp绑定成功，需要重新获取ppp信息
            GetPPPList();
        } else if (pppResultStr.equals("failed")) {//扫描失败
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
        } else if (pppResultStr.equals("notSame")) {//卡片类型不一致
            //初始化dialog
            customdialog = new CustomDialog.Builder(MyPPPActivity.this)
                    .setMessage(getString(R.string.not_ppp_card))
                    .setNegativeButton(null, new DialogOnClickListener(false, null, false))
                    .setPositiveButton(getResources().getString(R.string.dialog_ok1), new DialogOnClickListener(false, null, false))
                    .setCancelable(false)
                    .create();
            customdialog.show();
        } else {//返回pp码，弹框，询问是否绑定，目前已经没有这个流程

            customdialog = new CustomDialog.Builder(MyPPPActivity.this)
                    .setMessage(getString(R.string.bind_pp_now))
                    .setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogOnClickListener(true, pppResultStr, scanInfoEvent.isHasBind()))
                    .setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogOnClickListener(true, pppResultStr, scanInfoEvent.isHasBind()))
                    .setCancelable(false)
                    .create();
            customdialog.show();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        PictureAirLog.out(MyPPPActivity.class.getSimpleName() + "------>onreusme");
        isOnResume = true;
        if (isUseHavedPPP){

        }else{
            if (MyApplication.getInstance().getNeedRefreshPPPList()) { //解决问题，从PPP页面扫描PP+成功之后不刷新的问题。
                PictureAirLog.out("start get ppplist");
                GetPPPList();
            }
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        PictureAirLog.out(MyPPPActivity.class.getSimpleName() + "------>onreusme");
        if (isUseHavedPPP){
//            API1.PPPlist.clear();
        }else{
            if (EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().unregister(this);
            }
            myPPPHandler.removeCallbacksAndMessages(null);
        }
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
                        if (!dialog.isShowing()) {
                            dialog.show();
                        }
                        if (needBindToUser) {//是否已经绑定，如果已经绑定，则直接绑定到ppp，如果没有绑定，先绑定到user，在绑定到ppp
                            //已经被绑定了，所以直接绑定ppp
                            JSONArray pps = new JSONArray();
                            pps.add(PPCode);
                            API1.bindPPsToPPP(MyApplication.getTokenId(), pps, "", list1.get(currentPosition).PPPCode, myPPPHandler);
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
        isOnResume = false;
    }

    private String formaStringPPP(int count1, int count2) {
        return String.format(getString(R.string.pp_ok), count1, count2);
    }


    // 没有保存的时候的对话框
    private void createDialog() {
        if (pictureWorksDialog == null) {
            pictureWorksDialog = new PictureWorksDialog(this, null, getString(R.string.update_ppp_msg), getString(R.string.update_ppp_cancel), getString(R.string.update_ppp_ok), true, myPPPHandler);
        }
        pictureWorksDialog.show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                Log.e("MYPPPActiivty","ACTION_UP");
        }
        return super.onTouchEvent(event);
    }

}
