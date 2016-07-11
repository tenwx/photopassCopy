package com.pictureair.photopass.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.BaseFragment;
import com.pictureair.photopass.activity.LoadManageActivity;
import com.pictureair.photopass.adapter.PhotoLoadSuccessAdapter;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.PhotoDownLoadInfo;
import com.pictureair.photopass.eventbus.TabIndicatorUpdateEvent;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.widget.CustomProgressDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by pengwu on 16/7/8.
 */
public class LoadSuccessFragment extends BaseFragment {

    private ListView lv_success;
    PhotoLoadSuccessAdapter photoLoadSuccessAdapter;
    private CustomProgressDialog dialog;
    private boolean isLoading;
    private PictureAirDbManager pictureAirDbManager;
    private SharedPreferences sPreferences;
    private String userId = "";
    private final Handler photoLoadSuccessHandler= new PhotoLoadSuccessHandler(this);
    public static  final int LOAD_FROM_DATABASE = 1111;

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
                    List<PhotoDownLoadInfo> photos = (List<PhotoDownLoadInfo>)(msg.obj);
                    PhotoLoadSuccessAdapter adapter = new PhotoLoadSuccessAdapter(getContext(),photos);
                    lv_success.setAdapter(adapter);
                    if (photos != null) {
                        EventBus.getDefault().post(new TabIndicatorUpdateEvent(photos.size(),0));
                    }else{
                        EventBus.getDefault().post(0);
                    }
                }
                isLoading = false;
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
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
        dialog = CustomProgressDialog.create(getContext(), getString(R.string.is_loading), false, null);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        pictureAirDbManager = new PictureAirDbManager(getContext());
        sPreferences = getContext().getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isLoading) {
            isLoading = true;
            if (!dialog.isShowing()) {
                dialog.show();
            }
            new Thread(){
                @Override
                public void run() {
                    userId = sPreferences.getString(Common.USERINFO_ID, "");
                    List<PhotoDownLoadInfo> photos = pictureAirDbManager.getLoadSuccessPhotos(userId);
                    photoLoadSuccessHandler.obtainMessage(LOAD_FROM_DATABASE,photos).sendToTarget();
                }
            }.start();
        }
    }


}
