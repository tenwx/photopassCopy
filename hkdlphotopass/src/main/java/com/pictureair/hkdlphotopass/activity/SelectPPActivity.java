package com.pictureair.hkdlphotopass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.hkdlphotopass.R;
import com.pictureair.hkdlphotopass.adapter.ListOfPPAdapter;
import com.pictureair.jni.keygenerator.PWJniUtil;
import com.pictureworks.android.db.PictureAirDbManager;
import com.pictureworks.android.entity.BindPPInfo;
import com.pictureworks.android.entity.PPPinfo;
import com.pictureworks.android.entity.PPinfo;
import com.pictureworks.android.entity.PhotoInfo;
import com.pictureworks.android.util.API1;
import com.pictureworks.android.util.AppUtil;
import com.pictureworks.android.util.Common;
import com.pictureworks.android.util.PictureAirLog;
import com.pictureworks.android.widget.MyToast;
import com.pictureworks.android.widget.PictureWorksDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * @author talon
 *  这个类产生的原因。
 *  解决问题：
 *  在PP界面，进入单张照片预览（模糊状态），然后点 “使用已有的迪士尼乐拍通一卡通”，进入到选择PP 界面。 返回时 选择pp 界面 如果和 pp界面 是一个activity。就会冲突
 */
public class SelectPPActivity extends BaseActivity implements View.OnClickListener{
    private TextView tvTitle,ok;
    private ImageView back;
    private PictureAirDbManager pictureAirDbManager;
    private ArrayList<PPinfo> showPPCodeList;// 需要显示的List
    private PPPinfo dppp;
    private final int GET_SELECT_PP_SUCCESS = 2222;

    private final Handler myPPHandler = new MyPPHandler(this);
    private ListOfPPAdapter listPPAdapter;
    private ListView listPP;
    private RelativeLayout noPhotoPassView;
    private ArrayList<PhotoInfo> tempPhotoLists; //保存选中的 pp。 （准备升级PP＋的pp）
    private MyToast myToast;
    private PictureWorksDialog pictureWorksDialog;
    private static class MyPPHandler extends Handler{
        private final WeakReference<SelectPPActivity> mActivity;

        public MyPPHandler(SelectPPActivity activity){
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
     * @param msg
     */
    private void dealHandler(Message msg) {

        switch (msg.what){
            case GET_SELECT_PP_SUCCESS:
                listPPAdapter = new ListOfPPAdapter(showPPCodeList, SelectPPActivity.this, null, true, false, myPPHandler, dppp);
                listPP.setAdapter(listPPAdapter);

                if (showPPCodeList.size() == 0) {
                    ok.setEnabled(false);
                    ok.setTextColor(getResources().getColor(R.color.gray_light5));
                    PictureAirLog.out("has not pp code");
                    listPP.setVisibility(View.INVISIBLE);
                    noPhotoPassView.setVisibility(View.VISIBLE);
                } else {
                    noPhotoPassView.setVisibility(View.GONE);
                }
                break;
            case 2:
                ok.setText(formaStringPPP(msg.arg1, dppp.capacity));
                if (msg.arg1 - dppp.bindInfo.size() == 0) {
                    ok.setEnabled(false);
                    ok.setTextColor(getResources().getColor(R.color.gray_light5));
                } else {
                    ok.setEnabled(true);
                    ok.setTextColor(getResources().getColor(R.color.white));
                }
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
        pictureAirDbManager = new PictureAirDbManager(this, PWJniUtil.getSqlCipherKey(Common.APP_TYPE_HKDLPP));
        myToast = new MyToast(this);
        listPP = (ListView) findViewById(R.id.list_pp);
        tvTitle = (TextView) findViewById(R.id.mypp);

        String photoPassCode = this.getIntent().getStringExtra("photoPassCode");
        final String shootTime = this.getIntent().getStringExtra("shootTime");

        String[] photoCode = null;
        if (photoPassCode.contains(",")){
            photoCode = photoPassCode.split(",");
        }
        ok = (TextView) findViewById(R.id.ok);
        ok.setVisibility(View.VISIBLE);
        ok.setOnClickListener(this);
        ok.setText(formaStringPPP(0, 1));
        ok.setEnabled(false);
        ok.setTextColor(getResources().getColor(R.color.gray_light5));

        tvTitle.setText(R.string.selectionpp);  //选择PP界面

        final String[] finalPhotoCode = photoCode;
        new Thread() {
            public void run() {
                ArrayList<PPinfo> PPlist = new ArrayList<PPinfo>(); //创造一个List。
                for (int i = 0; i< finalPhotoCode.length; i++){
                    PPinfo pPinfo = new PPinfo();
                    pPinfo.setPpCode(finalPhotoCode[i]);
                    pPinfo.setShootDate(shootTime);
                    PPlist.add(pPinfo);
                }
                showPPCodeList = pictureAirDbManager.getPPCodeInfo1ByPPCodeList(PPlist, 2);

                dppp = new PPPinfo();
                dppp.capacity = 1;
                dppp.bindInfo = new ArrayList<BindPPInfo>();

                myPPHandler.sendEmptyMessage(GET_SELECT_PP_SUCCESS);
            }
        }.start();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.back:// 返回按钮
                this.finish();
                break;
            case R.id.ok://确认绑定按钮
                HashMap<Integer, Boolean> map = listPPAdapter.getMap();
                if (map.size() == 0) {
                    myToast.setTextAndShow(R.string.select_your_pp, Common.TOAST_SHORT_TIME);
                    return;
                }
                for (int i = 0; i < map.size(); i++) {
                    PictureAirLog.out("->" + map.get(i));
                }
                JSONArray pps = new JSONArray();
                tempPhotoLists = new ArrayList<>();
                //			String binddate = null;
                for (int j = 0; j < showPPCodeList.size(); j++) {
                    JSONObject jsonObject = new JSONObject();
                    if (null != map.get(j) && map.get(j)) {
                        try {
                            PhotoInfo photoInfo = new PhotoInfo();
                            jsonObject.put("code", showPPCodeList.get(j).getPpCode());
                            jsonObject.put("bindDate", showPPCodeList.get(j).getShootDate());
                            photoInfo.photoId = showPPCodeList.get(j).getPpCode();
                            photoInfo.shootTime = showPPCodeList.get(j).getShootDate();
                            tempPhotoLists.add(photoInfo);
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        pps.add(jsonObject);
                    }
                }
                if (AppUtil.getGapCount(pps.getJSONObject(0).getString("bindDate"),
                        pps.getJSONObject(pps.size() - 1).getString("bindDate")) > 3){
                    pictureWorksDialog = new PictureWorksDialog(SelectPPActivity.this, null,
                            getString(R.string.select_pp_wrong_date), null, getString(R.string.button_ok), true, myPPHandler);
                    pictureWorksDialog.show();
                } else {
                        Intent intent = new Intent(SelectPPActivity.this, MyPPPActivity.class);
                        intent.putExtra("ppsStr",pps.toString());
                        intent.putExtra("isUseHavedPPP", true);
                        startActivity(intent);
                }
                break;
        }
    }


    @Override
    protected void onDestroy() {
        if (API1.PPPlist.size() != 0){
            API1.PPPlist.clear();
        }
        super.onDestroy();
    }

    private String formaStringPPP(int count1, int count2) {
        return String.format(getString(R.string.pp_ok), count1, count2);
    }


}
