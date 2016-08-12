package com.pictureair.photopass.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.BaseFragment;
import com.pictureair.photopass.activity.DownloadPhotoPreviewActivity;
import com.pictureair.photopass.activity.MyPPActivity;
import com.pictureair.photopass.adapter.PhotoLoadSuccessAdapter;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.PhotoDownLoadInfo;
import com.pictureair.photopass.eventbus.DownLoadCountUpdateEvent;
import com.pictureair.photopass.eventbus.TabIndicatorUpdateEvent;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.SPUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.greenrobot.event.EventBus;

/**
 * Created by pengwu on 16/7/8.
 */
public class LoadSuccessFragment extends BaseFragment implements View.OnClickListener{

    private ListView lv_success;
    private boolean isLoading;
    private PictureAirDbManager pictureAirDbManager;
    private String userId = "";
    private final Handler photoLoadSuccessHandler= new PhotoLoadSuccessHandler(this);
    public static final int LOAD_FROM_DATABASE = 1111;
    public static final int DELETE_SUCCESS = 2233;
    public static final int GET_PHOTO_BACKGROUND = 3344;
    private List<PhotoDownLoadInfo> photos;
    private RelativeLayout rl_load_success;
    private LinearLayout ll_load_success;
    private Button button_toload;
    private Button btn_clear;
    private PhotoLoadSuccessAdapter adapter;
    private ExecutorService executorService;

    private static class PhotoLoadSuccessHandler extends Handler{
        private final WeakReference<LoadSuccessFragment> mActivity;

        public PhotoLoadSuccessHandler(LoadSuccessFragment fragment){
            mActivity = new WeakReference<LoadSuccessFragment>(fragment);
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

    private void dealHandler(Message msg){
        switch (msg.what){
            case LOAD_FROM_DATABASE:
                if (msg.obj != null) {
                    photos = (List<PhotoDownLoadInfo>)(msg.obj);
                    if (photos != null && photos.size() >0) {
                        ll_load_success.setVisibility(View.VISIBLE);
                        rl_load_success.setVisibility(View.GONE);
                        adapter = new PhotoLoadSuccessAdapter(MyApplication.getInstance(), photos);
                        lv_success.setAdapter(adapter);
                        EventBus.getDefault().post(new TabIndicatorUpdateEvent(photos.size(), 1,false));
                    }else{
                        rl_load_success.setVisibility(View.VISIBLE);
                        ll_load_success.setVisibility(View.GONE);
                        EventBus.getDefault().post(new TabIndicatorUpdateEvent(0, 1,false));
                    }
                }
                isLoading = false;
                dismissPWProgressDialog();
                break;
            case DELETE_SUCCESS:
                rl_load_success.setVisibility(View.VISIBLE);
                ll_load_success.setVisibility(View.GONE);
                int delCount = (int)msg.obj;
                EventBus.getDefault().post(new TabIndicatorUpdateEvent(0, 1,false));
                EventBus.getDefault().post(new DownLoadCountUpdateEvent(delCount));
                dismissPWProgressDialog();
                break;
            case GET_PHOTO_BACKGROUND:
                if (msg.obj != null) {
                    photos = (List<PhotoDownLoadInfo>)(msg.obj);
                    if (adapter != null){
                        ll_load_success.setVisibility(View.VISIBLE);
                        rl_load_success.setVisibility(View.GONE);
                        adapter.setPhotos(photos);
                        adapter.notifyDataSetChanged();
                    }else{
                        if (photos != null && photos.size()>0) {
                            ll_load_success.setVisibility(View.VISIBLE);
                            rl_load_success.setVisibility(View.GONE);
                            adapter = new PhotoLoadSuccessAdapter(MyApplication.getInstance(), photos);
                            lv_success.setAdapter(adapter);
                        }else{
                            rl_load_success.setVisibility(View.VISIBLE);
                            ll_load_success.setVisibility(View.GONE);
                        }

                    }
                }
                isLoading = false;
                break;
            default:
                break;
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_loadsuccess,null);
        lv_success = (ListView) view.findViewById(R.id.lv_load_success);
        rl_load_success = (RelativeLayout) view.findViewById(R.id.rl_load_success);
        ll_load_success = (LinearLayout) view.findViewById(R.id.ll_load_success);
        button_toload = (Button) view.findViewById(R.id.load_success_btn_toload);
        btn_clear = (Button) view.findViewById(R.id.load_success_clear);
        rl_load_success.setVisibility(View.GONE);
        ll_load_success.setVisibility(View.GONE);
        button_toload.setOnClickListener(this);
        btn_clear.setOnClickListener(this);
        lv_success.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (photos == null || photos.size() == 0) {
                    return;
                }
                Intent intent = new Intent(MyApplication.getInstance(), DownloadPhotoPreviewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("position",position);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        pictureAirDbManager = new PictureAirDbManager(getContext());
        if (TextUtils.isEmpty(userId)) {
            userId = SPUtils.getString(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ID, "");
        }
        executorService = Executors.newFixedThreadPool(1);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isLoading && photos == null) {
            isLoading = true;
            showPWProgressDialog();
            new Thread(){
                @Override
                public void run() {
                    loadPhotos(LOAD_FROM_DATABASE);
                }
            }.start();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.load_success_btn_toload:
                Intent i = new Intent();
                i.setClass(MyApplication.getInstance(), MyPPActivity.class);
                startActivity(i);
                getActivity().finish();
                break;
            case R.id.load_success_clear:
                showPWProgressDialog();
                new Thread(){
                    @Override
                    public void run() {
                        int res = pictureAirDbManager.deleteDownloadPhoto(userId);
                        Message msg = photoLoadSuccessHandler.obtainMessage(DELETE_SUCCESS);
                        msg.obj = res;
                        msg.sendToTarget();
                    }
                }.start();

                break;
            default:
                break;
        }
    }

    public void getDataBackground(){
        isLoading = true;
        if (executorService != null) {
            if (!executorService.isShutdown()) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        loadPhotos(GET_PHOTO_BACKGROUND);
                    }
                });
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void loadPhotos(int what){
        try{
            List<PhotoDownLoadInfo> photos = new ArrayList<PhotoDownLoadInfo>();
            if (pictureAirDbManager != null && !TextUtils.isEmpty(userId)) {
                photos = pictureAirDbManager.getPhotos(userId, true);
            }
            if (photoLoadSuccessHandler != null){
                photoLoadSuccessHandler.obtainMessage(what, photos).sendToTarget();
            }
        }catch (Exception e){

        }
    }

}
