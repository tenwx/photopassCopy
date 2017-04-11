package com.pictureair.photopass.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.ListOfPPAdapter;
import com.pictureair.photopass.customDialog.PWDialog;
import com.pictureair.photopass.entity.JsonInfo;
import com.pictureair.photopass.entity.PPPinfo;
import com.pictureair.photopass.entity.PPinfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.greendao.PictureAirDbManager;
import com.pictureair.photopass.http.rxhttp.RxSubscribe;
import com.pictureair.photopass.util.API2;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.widget.PWToast;
import com.trello.rxlifecycle.android.ActivityEvent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import rx.android.schedulers.AndroidSchedulers;

/*
 * @author talon
 */
public class SelectPPActivity extends BaseActivity implements View.OnClickListener, PWDialog.OnPWDialogClickListener {
    private TextView ok;
    private ImageView back;
    private ArrayList<PPinfo> showPPCodeList;// 需要显示的List
    private PPPinfo dppp;
    private static final int BIND_TIP_DIALOG = 5555;
    private static final int WRONG_DATE_DIALOG = 6666;
    private JSONArray pps;

    private final Handler myPPHandler = new MyPPHandler(this);
    private ListOfPPAdapter listPPAdapter;
    private ListView listPP;
    private RelativeLayout noPhotoPassView;
    private PWToast myToast;
    private PWDialog pictureWorksDialog;
    private boolean isDailyPPP;

    private static class MyPPHandler extends Handler {
        private final WeakReference<SelectPPActivity> mActivity;

        public MyPPHandler(SelectPPActivity activity) {
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
     * 处理 message 的方法
     *
     * @param msg
     */
    private void dealHandler(Message msg) {
        switch (msg.what) {
            case 2:
                ok.setText(formaStringPPP(msg.arg1, dppp.capacity));
                if (msg.arg1 - dppp.bindInfo.size() == 0) {
                    ok.setEnabled(false);
                    ok.setTextColor(ContextCompat.getColor(this, R.color.gray_light5));
                } else {
                    ok.setEnabled(true);
                    ok.setTextColor(ContextCompat.getColor(this, R.color.pp_blue));
                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_pp);

        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(this);
        noPhotoPassView = (RelativeLayout) findViewById(R.id.no_photo_relativelayout);
        myToast = new PWToast(this);
        listPP = (ListView) findViewById(R.id.list_pp);
        pictureWorksDialog = new PWDialog(this)
                .setOnPWDialogClickListener(this)
                .pwDialogCreate();

        isDailyPPP = getIntent().getBooleanExtra("dailyppp", false);
        Bundle bundle = getIntent().getBundleExtra("bundle");
        if (bundle.getParcelable("ppp") != null) {
            dppp = bundle.getParcelable("ppp");
        }

        ok = (TextView) findViewById(R.id.ok);
        ok.setOnClickListener(this);
        ok.setEnabled(false);
        ok.setTextColor(ContextCompat.getColor(this, R.color.gray_light5));

        ok.setText(formaStringPPP(dppp.bindInfo.size(), dppp.capacity));
        View view = LayoutInflater.from(this).inflate(R.layout.pp_header, null);
        listPP.addHeaderView(view);
        showPPCodeList = new ArrayList<>();
        showPPCodeList.addAll(API2.PPlist);
        listPPAdapter = new ListOfPPAdapter(showPPCodeList, SelectPPActivity.this, myPPHandler, dppp);
        listPP.setAdapter(listPPAdapter);

        if (showPPCodeList.size() == 0) {
            ok.setEnabled(false);
            ok.setTextColor(ContextCompat.getColor(this, R.color.gray_light5));
            PictureAirLog.out("has not pp code");
            listPP.setVisibility(View.INVISIBLE);
            noPhotoPassView.setVisibility(View.VISIBLE);
        } else {
            noPhotoPassView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:// 返回按钮
                finish();
                break;

            case R.id.ok://确认绑定按钮
                SparseBooleanArray map = listPPAdapter.getMap();
                if (map.size() == 0) {
                    myToast.setTextAndShow(R.string.select_your_pp, Common.TOAST_SHORT_TIME);
                    return;
                }
                pps = new JSONArray();
                String selectedString = "";
                for (int j = 0; j < showPPCodeList.size(); j++) {
                    JSONObject jsonObject = new JSONObject();
                    if (map.get(j, false)) {
                        try {
                            PhotoInfo photoInfo = new PhotoInfo();
                            jsonObject.put("code", showPPCodeList.get(j).getPpCode());
                            jsonObject.put("bindDate", showPPCodeList.get(j).getShootDate());
                            photoInfo.setPhotoId(showPPCodeList.get(j).getPpCode());
                            photoInfo.setShootDate(showPPCodeList.get(j).getShootDate());
                            selectedString += String.format(getString(R.string.select_pp), showPPCodeList.get(j).getPpCode(), showPPCodeList.get(j).getShootDate());
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        pps.add(jsonObject);
                    }
                }
                PictureAirLog.d("pps--> " + pps.toJSONString());
                pictureWorksDialog.setPWDialogId(BIND_TIP_DIALOG)
                        .setPWDialogMessage(String.format(getString(isDailyPPP ? R.string.select_pp_right_date2 : R.string.select_pp_right_date), selectedString))
                        .setPWDialogNegativeButton(R.string.button_cancel)
                        .setPWDialogPositiveButton(R.string.button_ok)
                        .setPWDialogContentCenter(false)
                        .pwDilogShow();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (API2.PPPlist.size() != 0) {
            API2.PPPlist.clear();
        }
        super.onDestroy();
    }

    private String formaStringPPP(int count1, int count2) {
        return String.format(getString(R.string.pp_ok), count1, count2);
    }

    @Override
    public void onPWDialogButtonClicked(int which, int dialogId) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            switch (dialogId) {
                case BIND_TIP_DIALOG:
                    if (AppUtil.getGapCount(pps.getJSONObject(0).getString("bindDate"),
                            pps.getJSONObject(pps.size() - 1).getString("bindDate")) > 3) {
                        pictureWorksDialog.setPWDialogId(WRONG_DATE_DIALOG)
                                .setPWDialogMessage(R.string.select_pp_wrong_date)
                                .setPWDialogNegativeButton(null)
                                .setPWDialogPositiveButton(R.string.button_ok)
                                .setPWDialogContentCenter(true)
                                .pwDilogShow();
                    } else {
                        showPWProgressDialog();
                        bindPPsDateToPPP(pps, dppp.PPPCode);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void bindPPsDateToPPP(JSONArray jsonArray, String pppCode) {
        API2.bindPPsDateToPPP(jsonArray, pppCode)
                .compose(this.<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RxSubscribe<JSONObject>() {
                    @Override
                    public void _onNext(JSONObject jsonObject) {
                        SPUtils.put(SelectPPActivity.this, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.NEED_FRESH, true);
                        PictureAirDbManager.insertRefreshPPFlag(pps, JsonInfo.DAILY_PP_REFRESH_ALL_TYPE);
                    }

                    @Override
                    public void _onError(int status) {
                        dismissPWProgressDialog();
                        myToast.setTextAndShow(ReflectionUtil.getStringId(MyApplication.getInstance(), status), Common.TOAST_SHORT_TIME);
                    }

                    @Override
                    public void onCompleted() {
                        if (AppManager.getInstance().checkActivity(MyPPPActivity.class)) {
                            AppManager.getInstance().killActivity(MyPPPActivity.class);
                        }
                        MyApplication.getInstance().setMainTabIndex(0);
                        dismissPWProgressDialog();
                        finish();
                    }
                });
    }
}
