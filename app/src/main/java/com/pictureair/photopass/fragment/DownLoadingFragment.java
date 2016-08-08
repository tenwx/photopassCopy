package com.pictureair.photopass.fragment;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.BaseFragment;
import com.pictureair.photopass.activity.MyPPActivity;
import com.pictureair.photopass.adapter.PhotoDownloadingAdapter;
import com.pictureair.photopass.entity.DownloadFileStatus;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.eventbus.TabIndicatorUpdateEvent;
import com.pictureair.photopass.service.DownloadService;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.widget.PWToast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import de.greenrobot.event.EventBus;

/**
 * Created by pengwu on 16/7/8.
 */
public class DownLoadingFragment extends BaseFragment implements View.OnClickListener {

    private ListView lv_loading;
    private DownloadService downloadService;
    private CopyOnWriteArrayList<DownloadFileStatus> downloadList;
    public static final int PHOTO_STATUS_UPDATE = 2222;
    public static final int PHOTO_REMOVE = 3333;
    public static final int SERVICE_LOAD_SUCCESS = 4444;
    public static final int DOWNLOAD_FINISH = 5555;
    public static final int REMOVE_FAILED_PHOTOS = 6666;
    private RelativeLayout rl_loading;
    private LinearLayout ll_loading;
    private Button btn_reconnect;
    private Button btn_clear_failed;
    private Button button;
    private PhotoDownloadingAdapter adapter;
    private PWToast myToast;

    private final Handler adapterHandler = new AdapterHandler(this);

    public static class AdapterHandler extends Handler{
        private final WeakReference<DownLoadingFragment> mActivity;

        public AdapterHandler(DownLoadingFragment activity){
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

    private void dealHandler(Message msg) {
        dismissPWProgressDialog();
        switch (msg.what) {
            case PHOTO_STATUS_UPDATE://更新listview
                PictureAirLog.v("dealHandler","PHOTO_STATUS_UPDATE");
                DownloadFileStatus fileStatus = (DownloadFileStatus)msg.obj;
                updateView();
                if (fileStatus != null) {
                    if (fileStatus.status == DownloadFileStatus.DOWNLOAD_STATE_FAILURE){
                        downloadService.sendAddDownLoadMessage();
                    }
                }
                break;
            case PHOTO_REMOVE://下载成功后删除
                PictureAirLog.v("DownLoadingFragment","PHOTO_REMOVE");
                DownloadFileStatus status = (DownloadFileStatus)msg.obj;
                if (downloadList != null) {
                    downloadList = downloadService.getDownloadList();
                    PictureAirLog.v("PHOTO_REMOVE","downloadList.size: "+downloadList.size());
                    adapter.setList(downloadList);
                    adapter.notifyDataSetChanged();
//                    PictureAirLog.v("DownLoadingFragment","dealHandler remove item success");
                    EventBus.getDefault().post(new TabIndicatorUpdateEvent(downloadList.size(),0,false));
                    AtomicInteger oldCount = downloadService.getDatabasePhotoCount();
                    PictureAirLog.v("PHOTO_REMOVE","oldCount: "+oldCount);
                    AtomicInteger downLoadCount=downloadService.getDowned_num();
                    PictureAirLog.v("PHOTO_REMOVE","downLoadCount: "+downLoadCount);
                    EventBus.getDefault().post(new TabIndicatorUpdateEvent(downLoadCount.get()+oldCount.get(),1,false));
                    downloadService.sendAddDownLoadMessage();
                }
                break;

            case SERVICE_LOAD_SUCCESS://DownloadService 绑定成功，更新页面，设置下载数量
                if (adapter == null) {
                    PictureAirLog.v("SERVICE_LOAD_SUCCESS","adapter= null ");
                    downloadList = downloadService.getDownloadList();
                    if (downloadList != null && downloadList.size() >0) {
                        ll_loading.setVisibility(View.VISIBLE);
                        rl_loading.setVisibility(View.GONE);
                        PictureAirLog.v("SERVICE_LOAD_SUCCESS","new adapter ");
                        adapter = new PhotoDownloadingAdapter(MyApplication.getInstance(),downloadList);
                        lv_loading.setAdapter(adapter);
                    }else{
                        PictureAirLog.v("SERVICE_LOAD_SUCCESS","nothing ");
                        ll_loading.setVisibility(View.GONE);
                        rl_loading.setVisibility(View.VISIBLE);
                    }
                }else{
                    PictureAirLog.v("SERVICE_LOAD_SUCCESS","adapter!= null ");
                    downloadList = downloadService.getDownloadList();
                    adapter.setList(downloadList);
                    adapter.notifyDataSetChanged();
                }
                EventBus.getDefault().post(new TabIndicatorUpdateEvent(downloadList.size(), 0,false));
                AtomicInteger oldCount = downloadService.getDatabasePhotoCount();
                PictureAirLog.v("SERVICE_LOAD_SUCCESS","oldCount: "+oldCount);
                AtomicInteger downLoadCount=downloadService.getDowned_num();
                PictureAirLog.v("SERVICE_LOAD_SUCCESS","downLoadCount: "+downLoadCount);
                EventBus.getDefault().post(new TabIndicatorUpdateEvent(downLoadCount.get()+oldCount.get(),1,false));
                downloadService.startDownload();
                break;
            case DOWNLOAD_FINISH://下载完成，此时downloadservice中已没有可下载任务，页面显示没有下载中的照片
                CopyOnWriteArrayList<DownloadFileStatus> list = downloadService.getDownloadList();
                if (list == null || list.size() == 0) {
                    ll_loading.setVisibility(View.GONE);
                    rl_loading.setVisibility(View.VISIBLE);
                }
                break;

            case REMOVE_FAILED_PHOTOS://表示已删除所有下载失败的照片，更新页面
                downloadList = downloadService.getDownloadList();
                if (downloadList != null && downloadList.size() > 0) {
                    adapter.setList(downloadList);
                    adapter.notifyDataSetChanged();
                }else{
                    ll_loading.setVisibility(View.GONE);
                    rl_loading.setVisibility(View.VISIBLE);

                }
                EventBus.getDefault().post(new TabIndicatorUpdateEvent(downloadList.size(),0,false));
                break;
            default:
                break;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_downloading,null);
        lv_loading = (ListView) view.findViewById(R.id.lv_downloading);
        rl_loading = (RelativeLayout) view.findViewById(R.id.rl_downloading);
        button = (Button) view.findViewById(R.id.downloading_btn_toload);
        btn_reconnect = (Button) view.findViewById(R.id.loading_connect);
        btn_clear_failed = (Button) view.findViewById(R.id.loading_clear_fail);
        ll_loading = (LinearLayout) view.findViewById(R.id.ll_downloading);
        myToast = new PWToast(MyApplication.getInstance());
        ll_loading.setVisibility(View.GONE);
        rl_loading.setVisibility(View.VISIBLE);
        button.setOnClickListener(this);
        btn_reconnect.setOnClickListener(this);
        btn_clear_failed.setOnClickListener(this);
        bindService();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DownloadService.PhotoBind photoBind = (DownloadService.PhotoBind)service;
            downloadService = photoBind.getService();
            if (downloadService != null) {
                downloadList = downloadService.getDownloadList();
                downloadService.setAdapterhandler(adapterHandler);
                if (AppUtil.checkPermission(MyApplication.getInstance(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showPWProgressDialog();
                }
                if (downloadList != null && downloadList.size() >0) {
                    ll_loading.setVisibility(View.VISIBLE);
                    rl_loading.setVisibility(View.GONE);
                    adapter = new PhotoDownloadingAdapter(getContext(), downloadList);
                    lv_loading.setAdapter(adapter);
                    EventBus.getDefault().post(new TabIndicatorUpdateEvent(downloadList.size(), 0,false));
                }else{//如果downloadservice中没有等待下载的数据，bindservice不去走onstartcommand，此时用startservice运行一遍onstartcommand
                    Intent intent = new Intent(MyApplication.getInstance(), DownloadService.class);
                    ArrayList<PhotoInfo> photos = new ArrayList<>();
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("photos", photos);
                    intent.putExtras(bundle);
                    MyApplication.getInstance().startService(intent);
                    PictureAirLog.v("onServiceConnected","false");
                }

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.downloading_btn_toload:
                Intent intent = new Intent();
                intent.setClass(MyApplication.getInstance(), MyPPActivity.class);
                startActivity(intent);
                getActivity().finish();
                break;

            case R.id.loading_connect:
                ArrayList<PhotoInfo> list = new ArrayList<PhotoInfo>();
                if (downloadList != null && downloadList.size() >0) {
                    for (int i=0;i<downloadList.size();i++) {
                        DownloadFileStatus fileStatus = downloadList.get(i);
                        if(fileStatus.status == DownloadFileStatus.DOWNLOAD_STATE_FAILURE) {
                            fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_RECONNECT;
                            PhotoInfo info = new PhotoInfo();
                            info.isVideo = fileStatus.isVideo();
                            info.photoPathOrURL = fileStatus.getUrl();
                            info.photoId = fileStatus.getPhotoId();
                            info.shootOn = fileStatus.getShootOn();
                            info.failedTime = fileStatus.getFailedTime();
                            list.add(info);
                        }
                    }
                    adapter.setList(downloadList);
                    adapter.notifyDataSetChanged();
                    Intent intent1 = new Intent(MyApplication.getInstance(), DownloadService.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("photos", list);
                    bundle.putInt("reconnect",1);
                    bundle.putInt("one",1);
                    intent1.putExtras(bundle);
                    MyApplication.getInstance().startService(intent1);
                }
                break;

            case R.id.loading_clear_fail:
                if (downloadService.isDownloading()){
                    myToast.setTextAndShow(R.string.photo_download_clear_tips, Common.TOAST_SHORT_TIME);
                }else {
                    showPWProgressDialog();
                    downloadService.sendClearFailedMsg();
                }
                break;
            default:
                break;
        }
    }

    private void bindService(){
        Intent intent = new Intent(getContext(),DownloadService.class);
        getContext().bindService(intent,conn,Context.BIND_AUTO_CREATE);
    }

    private void unBind(){
        getContext().unbindService(conn);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (downloadService != null){
            downloadService.setAdapterhandler(null);
            unBind();
        }
    }

    private void updateView(){
        PictureAirLog.v("updateView","updateView");
        downloadList = downloadService.getDownloadList();
        adapter.setList(downloadList);
        adapter.notifyDataSetChanged();
    }


}
